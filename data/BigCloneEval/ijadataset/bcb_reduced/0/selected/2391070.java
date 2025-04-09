package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;
import com.google.gwt.dev.js.JsParser;
import com.google.gwt.dev.js.JsParserException;
import com.google.gwt.dev.js.JsParserException.SourceDetail;
import com.google.gwt.dev.js.ast.JsExprStmt;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsParameter;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsStatement;
import com.google.gwt.dev.util.collect.IdentityHashMap;
import com.google.gwt.dev.util.collect.IdentityMaps;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * Adapts compilation units containing JSNI-accessible code by rewriting the
 * source.
 */
public class JsniCollector {

    /**
   * Represents a logical interval of text.
   */
    public static class Interval {

        public final int end;

        public final int start;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static final class JsniMethodImpl extends JsniMethod {

        private final JsFunction func;

        private final String name;

        public JsniMethodImpl(String name, JsFunction func) {
            this.name = name;
            this.func = func;
        }

        @Override
        public JsFunction function() {
            return func;
        }

        @Override
        public int line() {
            return func.getSourceInfo().getStartLine();
        }

        @Override
        public String location() {
            return func.getSourceInfo().getFileName();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String[] paramNames() {
            List<JsParameter> params = func.getParameters();
            String[] result = new String[params.size()];
            for (int i = 0; i < result.length; ++i) {
                result[i] = params.get(i).getName().getIdent();
            }
            return result;
        }

        @Override
        public JsProgram program() {
            return func.getScope().getProgram();
        }

        @Override
        public String toString() {
            return func.toString();
        }
    }

    private static class Visitor extends MethodVisitor {

        private final Map<AbstractMethodDeclaration, JsniMethod> jsniMethods;

        private final JsProgram jsProgram;

        private final String source;

        public Visitor(String source, JsProgram program, Map<AbstractMethodDeclaration, JsniMethod> jsniMethods) {
            this.jsProgram = program;
            this.jsniMethods = jsniMethods;
            this.source = source;
        }

        @Override
        protected boolean interestingMethod(AbstractMethodDeclaration method) {
            return method.isNative();
        }

        @Override
        protected void processMethod(TypeDeclaration typeDecl, AbstractMethodDeclaration method, String enclosingType, String loc) {
            JsFunction jsFunction = parseJsniFunction(method, source, enclosingType, loc, jsProgram);
            if (jsFunction != null) {
                String jsniSignature = getJsniSignature(enclosingType, method);
                jsniMethods.put(method, new JsniMethodImpl(jsniSignature, jsFunction));
            }
        }
    }

    public static final String JSNI_BLOCK_END = "}-*/";

    public static final String JSNI_BLOCK_START = "/*-{";

    public static Map<AbstractMethodDeclaration, JsniMethod> collectJsniMethods(final CompilationUnitDeclaration cud, final String source, final JsProgram program) {
        Map<AbstractMethodDeclaration, JsniMethod> jsniMethods = new IdentityHashMap<AbstractMethodDeclaration, JsniMethod>();
        new Visitor(source, program, jsniMethods).collect(cud);
        return IdentityMaps.normalizeUnmodifiable(jsniMethods);
    }

    public static JsFunction parseJsniFunction(AbstractMethodDeclaration method, String unitSource, String enclosingType, String fileName, JsProgram jsProgram) {
        CompilationResult compResult = method.compilationResult;
        int[] indexes = compResult.lineSeparatorPositions;
        int startLine = Util.getLineNumber(method.sourceStart, indexes, 0, indexes.length - 1);
        SourceInfo info = SourceOrigin.create(method.sourceStart, method.bodyEnd, startLine, fileName);
        String jsniCode = unitSource.substring(method.bodyStart, method.bodyEnd + 1);
        int startPos = jsniCode.indexOf("/*-{");
        int endPos = jsniCode.lastIndexOf("}-*/");
        if (startPos < 0 && endPos < 0) {
            reportJsniError(info, method, "Native methods require a JavaScript implementation enclosed with /*-{ and }-*/");
            return null;
        }
        if (startPos < 0) {
            reportJsniError(info, method, "Unable to find start of native block; begin your JavaScript block with: /*-{");
            return null;
        }
        if (endPos < 0) {
            reportJsniError(info, method, "Unable to find end of native block; terminate your JavaScript block with: }-*/");
            return null;
        }
        startPos += 3;
        endPos += 1;
        jsniCode = jsniCode.substring(startPos, endPos);
        StringBuilder functionSource = new StringBuilder("function (");
        boolean first = true;
        if (method.arguments != null) {
            for (Argument arg : method.arguments) {
                if (first) {
                    first = false;
                } else {
                    functionSource.append(',');
                }
                functionSource.append(arg.binding.name);
            }
        }
        functionSource.append(") ");
        int functionHeaderLength = functionSource.length();
        functionSource.append(jsniCode);
        StringReader sr = new StringReader(functionSource.toString());
        int absoluteJsStartPos = method.bodyStart + startPos;
        int absoluteJsEndPos = absoluteJsStartPos + jsniCode.length();
        int jsStartPos = absoluteJsStartPos - functionHeaderLength;
        int jsEndPos = absoluteJsEndPos - functionHeaderLength;
        int jsLine = info.getStartLine() + countLines(indexes, info.getStartPos(), absoluteJsStartPos);
        SourceInfo jsInfo = SourceOrigin.create(jsStartPos, jsEndPos, jsLine, info.getFileName());
        try {
            List<JsStatement> result = JsParser.parse(jsInfo, jsProgram.getScope(), sr);
            JsExprStmt jsExprStmt = (JsExprStmt) result.get(0);
            return (JsFunction) jsExprStmt.getExpression();
        } catch (IOException e) {
            throw new InternalCompilerException("Internal error parsing JSNI in '" + enclosingType + '.' + method.toString() + '\'', e);
        } catch (JsParserException e) {
            int problemCharPos = computeAbsoluteProblemPosition(indexes, e.getSourceDetail());
            SourceInfo errorInfo = SourceOrigin.create(problemCharPos, problemCharPos, e.getSourceDetail().getLine(), info.getFileName());
            reportJsniError(errorInfo, method, e.getMessage());
            return null;
        }
    }

    public static void reportJsniError(SourceInfo info, AbstractMethodDeclaration method, String msg) {
        reportJsniProblem(info, method, msg, ProblemSeverities.Error);
    }

    public static void reportJsniWarning(SourceInfo info, MethodDeclaration method, String msg) {
        reportJsniProblem(info, method, msg, ProblemSeverities.Warning);
    }

    /**
   * JS reports the error as a line number, to find the absolute position in the
   * real source stream, we have to walk from the absolute JS start position
   * until we have counted down enough lines. Then we use the column position to
   * find the exact spot.
   */
    private static int computeAbsoluteProblemPosition(int[] indexes, SourceDetail detail) {
        int line = detail.getLine() - 1;
        if (line == 0) {
            return detail.getLineOffset() - 1;
        }
        int result = indexes[line - 1] + detail.getLineOffset();
        assert line >= indexes.length || result < indexes[line];
        return result;
    }

    private static int countLines(int[] indexes, int p1, int p2) {
        assert p1 >= 0;
        assert p2 >= 0;
        assert p1 <= p2;
        int p1line = findLine(p1, indexes, 0, indexes.length);
        int p2line = findLine(p2, indexes, 0, indexes.length);
        return p2line - p1line;
    }

    private static int findLine(int pos, int[] indexes, int lo, int tooHi) {
        assert (lo < tooHi);
        if (lo == tooHi - 1) {
            return lo;
        }
        int mid = lo + (tooHi - lo) / 2;
        assert (lo < mid);
        if (pos < indexes[mid]) {
            return findLine(pos, indexes, lo, mid);
        } else {
            return findLine(pos, indexes, mid, tooHi);
        }
    }

    /**
   * Gets a unique name for this method and its signature (this is used to
   * determine whether one method overrides another).
   */
    private static String getJsniSignature(String enclosingType, AbstractMethodDeclaration method) {
        return '@' + enclosingType + "::" + MethodVisitor.getMemberSignature(method);
    }

    private static void reportJsniProblem(SourceInfo info, AbstractMethodDeclaration methodDeclaration, String message, int problemSeverity) {
        HelpInfo jsniHelpInfo = null;
        CompilationResult compResult = methodDeclaration.compilationResult();
        int startColumn = Util.searchColumnNumber(compResult.getLineSeparatorPositions(), info.getStartLine(), info.getStartPos());
        GWTProblem.recordProblem(info, startColumn, compResult, message, jsniHelpInfo, problemSeverity);
    }

    private JsniCollector() {
    }

    public static JsniMethod restoreJsniMethod(String name, String functionSource, SourceInfo jsInfo, JsProgram jsProgram) throws Exception {
        List<JsStatement> result = JsParser.parse(jsInfo, jsProgram.getScope(), new StringReader(functionSource));
        JsExprStmt jsExprStmt = (JsExprStmt) result.get(0);
        JsFunction func = (JsFunction) jsExprStmt.getExpression();
        return func != null ? new JsniMethodImpl(name, func) : null;
    }
}
