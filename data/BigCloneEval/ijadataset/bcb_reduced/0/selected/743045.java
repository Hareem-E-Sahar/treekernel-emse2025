package org.ocl4java.ocl;

import java.text.ParsePosition;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.ocl4java.jdt.JDTSourceIntrospector;

/**
 * This is a helper-Class to remove the code that
 * OCLCodeGenerator would insert.
 * TODO: fails in eclipse-plugin on assertionstyle "assert"
 * OCLCodeGenerator.cleanSourceToCompilationUnit contains a workaround using a regexp on the source for the moment
 */
public final class OCLCodeUnGenerator extends ASTVisitor {

    /**
     * our singleton-instance.
     */
    private static final ASTVisitor INSTANCE = new OCLCodeUnGenerator();

    /**
     * private-constructor because business-methods are static.
     *
     */
    private OCLCodeUnGenerator() {
    }

    /**
     * OCLCodeUnGenerator fails on asserts.
     * This bug could not be fixed.
     * so we remove them by a regexp.
     * @param source original source
     * @return source without calls to assertion-methods in assertionstyle "java"
     */
    protected static void assertBugInUnGeneratorWorkAround(final IDocument source) {
        Pattern pattern = Pattern.compile("assert\\s*assert(Pre|Post)Condition_(\\w*)_for_method_(\\w*)(\\s*)\\(([^\\)]*)\\)(\\s*):" + "(\\s*)\"([^\"]*|\\\\\")*\";");
        Matcher matcher = pattern.matcher(source.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                source.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a source-code with OCL-contrains in
     * annotations and removes all methods and calls to check
     * these contrains.<br/>
     * @param source the source-code to parse
     * @return the modified source-code
     */
    public static void cleanSource(final IDocument source) throws BadLocationException {
        assertBugInUnGeneratorWorkAround(source);
        CompilationUnit unit = cleanSourceToCompilationUnit(source);
        CodeGenerationBeautifier.compilationUnitToSource(source, unit);
    }

    /**
     * Takes a source-code with OCL-contrains in
     * annotations and removes all methods and calls to check
     * these contrains.<br/>
     * @param source the source-code to parse
     * @param cunit (may be null!) Optional existing compilation-unit representing source
     * @return the modified source-code
     */
    public static void cleanSource(final IDocument source, final ICompilationUnit cunit) throws BadLocationException {
        assertBugInUnGeneratorWorkAround(source);
        CompilationUnit unit = cleanSourceToCompilationUnit(cunit);
        CodeGenerationBeautifier.compilationUnitToSource(source, unit);
    }

    /**
     * Takes a source-code with OCL-contrains in
     * annotations and removed methods and calls to check
     * these contrains (of any ${@link #ASSERTIONSTYLE}).
     * @param source the source-code to parse
     * @return the modified CompilationUnit
     */
    public static CompilationUnit cleanSourceToCompilationUnit(final IDocument source) {
        assertBugInUnGeneratorWorkAround(source);
        CompilationUnit unit = JDTSourceIntrospector.getCompilationUnit(source.get().toCharArray());
        unit.recordModifications();
        removeAssertionChecks(unit);
        return unit;
    }

    public static CompilationUnit cleanSourceToCompilationUnit(final ICompilationUnit source) {
        CompilationUnit unit = JDTSourceIntrospector.getCompilationUnit(source);
        unit.recordModifications();
        removeAssertionChecks(unit);
        return unit;
    }

    /**
     * @param unit unit to work on
     */
    protected static void removeAssertionChecks(final CompilationUnit unit) {
        unit.accept(INSTANCE);
    }

    /**
     * Remove the methods-declarations matching
     * ${@link AbstractOCLCodeGenerator#ASSERTMETHODNAMEFORMAT} and
     * ${@link AbstractOCLCodeGenerator#ASSERTRESULTMETHODNAMEFORMAT} .
     * @see ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
     */
    @Override
    public boolean visit(final MethodDeclaration node) {
        String methodName = node.getName().toString();
        if (methodName == null) return true;
        ParsePosition ppos = new ParsePosition(0);
        AbstractOCLCodeGenerator.ASSERTMETHODNAMEFORMAT.parse(methodName, ppos);
        if (ppos.getIndex() == methodName.length()) {
            removeMethod(node);
            return false;
        }
        ppos = new ParsePosition(0);
        AbstractOCLCodeGenerator.ASSERTRESULTMETHODNAMEFORMAT.parse(methodName, ppos);
        if (ppos.getIndex() == methodName.length()) {
            removeMethod(node);
            return false;
        }
        return true;
    }

    /**
     * @param node method-declaration to remove
     */
    public void removeMethod(final MethodDeclaration node) {
        ASTNode parent = node.getParent();
        if (parent instanceof TypeDeclaration) {
            TypeDeclaration type = (TypeDeclaration) parent;
            type.bodyDeclarations().remove(node);
            return;
        }
        System.err.println("ERROR: OCLCodeUnGenerator: MethodDeclaration '" + node + "' has parent of unknown type [" + parent.getClass().getName() + "] " + "expected TypeDeclaration");
    }

    /**
     * @see ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
     */
    @Override
    public boolean visit(final MethodInvocation node) {
        String methodName = node.getName().toString();
        if (methodName == null) return false;
        ParsePosition ppos = new ParsePosition(0);
        AbstractOCLCodeGenerator.ASSERTMETHODNAMEFORMAT.parse(methodName, ppos);
        if (ppos.getIndex() == methodName.length()) {
            removeMethodInvocation(node);
            return false;
        }
        ppos = new ParsePosition(0);
        AbstractOCLCodeGenerator.ASSERTRESULTMETHODNAMEFORMAT.parse(methodName, ppos);
        if (ppos.getIndex() == methodName.length()) {
            removeMethodInvocation(node);
            return false;
        }
        if (methodName.startsWith("assertP")) {
            System.err.println("DEBUG:" + getClass().getName() + ": method-name does not match! name='" + methodName + "'");
        }
        return false;
    }

    /**
     * @param node the statement to remove from it's parent.
     */
    private void removeStatement(final Statement node) {
        ASTNode parent = node.getParent();
        if (parent instanceof Block) {
            Block block = (Block) parent;
            List<Statement> statements = (List<Statement>) block.statements();
            if (node instanceof AssertStatement) {
                int index = statements.indexOf(node);
                if (index != -1 && statements.size() > (index + 1)) {
                    Statement nextStatement = statements.get(index + 1);
                    if (nextStatement instanceof EmptyStatement) {
                        statements.remove(nextStatement);
                    }
                }
            }
            statements.remove(node);
            block.accept(this);
            return;
        } else {
            System.err.println("ERROR: OCLCodeUnGenerator: Statement '" + node + "' has parent of unknown type [" + (parent == null ? "null" : parent.getClass().getName()) + "] " + "expected block");
        }
    }

    /**
     *
     * @param invocation  the check-method-invocation to remove
     * @param node the eturn-statement to change
     */
    private void removeReturnStatement(final MethodInvocation invocation, final ReturnStatement node) {
        List<Expression> arguments = (List<Expression>) invocation.arguments();
        node.setExpression((Expression) ASTNode.copySubtree(node.getAST(), arguments.get(arguments.size() - 1)));
        node.accept(this);
    }

    /**
     * Remove the parent-statement of node if the MethodInvocation
     * is inside an 'assert' or 'if'.
     * @param node the method-invocation to parse
     */
    private void removeMethodInvocation(final MethodInvocation node) {
        ASTNode parent = node.getParent();
        if (parent == null) return;
        if (parent instanceof ExpressionStatement) {
            parent = parent.getParent();
        }
        if (parent instanceof PrefixExpression) {
            parent = parent.getParent();
        }
        if (parent instanceof IfStatement) {
            removeStatement((Statement) parent);
            return;
        }
        if (parent instanceof AssertStatement) {
            removeStatement((Statement) parent);
            return;
        }
        if (parent instanceof ReturnStatement) {
            removeReturnStatement(node, (ReturnStatement) parent);
            return;
        }
        System.err.println("ERROR: OCLCodeUnGenerator: MethodInvocation '" + node + "' has parent of unknown type [" + parent.getClass().getName() + "] " + "expected assert or if");
    }
}
