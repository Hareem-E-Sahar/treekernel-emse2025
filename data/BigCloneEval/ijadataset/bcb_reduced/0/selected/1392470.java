package org.ocl4java.ocl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.ocl4java.Constraint;
import org.ocl4java.jdt.JDTSourceIntrospector;

/**
 * This is a helper-class to rewrite some of the generated code
 * after the abstract syntax tree has already been converted
 * to source-code.
 * @author <a href="Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public final class CodeGenerationBeautifier {

    /**
     * all business-methods are static so we are private.
     */
    private CodeGenerationBeautifier() {
        super();
    }

    /**
     * Turn the given modified Compilationunit into a modified source-code
     * with the help of the given unmodified source-code.
     * @param origSource unmodified source to modify acording to the recorded changes made to unit
     * @param unit modified parsed source
     * @throws BadLocationException if the compilation-unit's changes cannot be applied to origSource
     */
    public static void compilationUnitToSource(@Constraint("post: origSource.get().size<=origSource@pre.get().size") final IDocument origSource, @Constraint("pre: unit.getLength()>0") final CompilationUnit unit) throws BadLocationException {
        IDocument tempStr = JDTSourceIntrospector.compilationUnitToDocument(origSource, unit);
        removeNewLinesForPREDefinition(tempStr);
        removeNewlinesForAssertStyleJava(tempStr);
        removeNewlinesForAssertStyleException(tempStr);
        removeNewlinesForAssertStyleSystemErr(tempStr);
        removeNewlinesForAssertStyleCommonsLogging(tempStr);
        removeNewlinesForAssertStyleHandler(tempStr);
    }

    /**
     * Remove the newlines in the definitions of the PRE-variable.
     * @param s the code to modify
     * @return the modified code
     *
     */
    @Constraint("post: s.get().size>0")
    protected static void removeNewLinesForPREDefinition(@Constraint("post: s.get().size<=s@pre.get().size") final IDocument s) {
        Pattern pattern = Pattern.compile("java.util.Map<String, tudresden.ocl.lib.OclRoot> PRE = new java.util.HashMap<String, tudresden.ocl.lib.OclRoot>();\n", Pattern.LITERAL);
        Matcher matcher = pattern.matcher(s.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "java.util.Map<String, tudresden.ocl.lib.OclRoot> PRE = new java.util.HashMap<String, tudresden.ocl.lib.OclRoot>();";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                s.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the newlines in the calls to assertion-methods.
     * @param s the code to modify
     * @return the modified code
     */
    @Constraint("post: s.get().size>0")
    protected static void removeNewlinesForAssertStyleJava(@Constraint("post: s.get().size<=s@pre.get().size") final IDocument s) {
        Pattern pattern = Pattern.compile("assert\\s*assert(Pre|Post)Condition_(\\w*)_for_method_(\\w*)\\s*\\(([^\\)]*)\\)\\s*:" + "\\s*\"(.*)\"\\s*;" + "\\s*");
        Matcher matcher = pattern.matcher(s.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "assert assert" + Matcher.quoteReplacement(matcher.group(1)) + "Condition_" + Matcher.quoteReplacement(matcher.group(2)) + "_for_method_" + Matcher.quoteReplacement(matcher.group(3)) + "(" + Matcher.quoteReplacement(matcher.group(4)) + ") : \"" + Matcher.quoteReplacement(matcher.group(5)) + "\";";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                s.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the newlines in the calls to assertion-methods.
     * @param s the code to modify
     * @return the modified code
     */
    @Constraint("post: s.get().size>0")
    protected static void removeNewlinesForAssertStyleException(@Constraint("post: s.get().size<=s@pre.get().size") final IDocument s) {
        String checkMethodName = "assert(Pre|Post)Condition_(\\w*)_for_method_(\\w*)";
        String checkMethodParameters = "([^\\)]*)";
        Pattern pattern = Pattern.compile("if\\s*\\(\\s*!\\s*" + checkMethodName + "\\s*\\(" + checkMethodParameters + "\\)\\s*\\)\\s*\\{" + "\\s*throw\\s*new\\s*Illegal(State|Argument)Exception\\(\\s*\"(.*)\"\\s*\\)\\s*;" + "\\s*}\\s*");
        Matcher matcher = pattern.matcher(s.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "if(!assert" + Matcher.quoteReplacement(matcher.group(1)) + "Condition_" + Matcher.quoteReplacement(matcher.group(2)) + "_for_method_" + Matcher.quoteReplacement(matcher.group(3)) + "(" + Matcher.quoteReplacement(matcher.group(4)) + ")){throw new Illegal" + Matcher.quoteReplacement(matcher.group(5)) + "Exception(\"" + Matcher.quoteReplacement(matcher.group(6)) + "\");}";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                s.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the newlines in the calls to assertion-methods.
     * @param s the code to modify
     * @return the modified code
     */
    @Constraint("post: s.get().size>0")
    protected static void removeNewlinesForAssertStyleHandler(@Constraint("post: s.get().size<=s@pre.get().size") final IDocument s) {
        String checkMethodName = "assert(Pre|Post)Condition_(\\w*)_for_method_(\\w*)";
        String checkMethodParameters = "([^\\)]*)";
        Pattern pattern = Pattern.compile("if\\s*\\(\\s*!\\s*" + checkMethodName + "\\s*\\(" + checkMethodParameters + "\\)\\s*\\)\\s*\\{" + "\\s*org.ocl4java.ConstraintFailedHandlerManager.handleConstraintFailed\\(\\s*(.*)\\s*\\)\\s*;" + "\\s*}\\s*");
        Matcher matcher = pattern.matcher(s.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "if(!assert" + Matcher.quoteReplacement(matcher.group(1)) + "Condition_" + Matcher.quoteReplacement(matcher.group(2)) + "_for_method_" + Matcher.quoteReplacement(matcher.group(3)) + "(" + Matcher.quoteReplacement(matcher.group(4)) + ")){org.ocl4java.ConstraintFailedHandlerManager.handleConstraintFailed(" + Matcher.quoteReplacement(matcher.group(5)) + ");}";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                s.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the newlines in the calls to assertion-methods.
     * @param s the code to modify
     * @return the modified code
     */
    @Constraint("post: s.get().size>0")
    protected static void removeNewlinesForAssertStyleSystemErr(@Constraint("post: s.get().size<=s@pre.get().size") final IDocument s) {
        Pattern pattern = Pattern.compile("if\\s*\\(\\s*!\\s*assert(Pre|Post)Condition_(\\w*)_for_method_(\\w*)\\s*\\(([^\\)]*)\\)\\s*\\)\\s*\\{" + "\\s*System.err.println\\(\\s*\"(.*)\"\\s*\\)\\s*;" + "\\s*}\\s*");
        Matcher matcher = pattern.matcher(s.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "if(!assert" + Matcher.quoteReplacement(matcher.group(1)) + "Condition_" + Matcher.quoteReplacement(matcher.group(2)) + "_for_method_" + Matcher.quoteReplacement(matcher.group(3)) + "(" + Matcher.quoteReplacement(matcher.group(4)) + ")){System.err.println(\"" + Matcher.quoteReplacement(matcher.group(5)) + "\");}";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                s.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the newlines in the calls to assertion-methods.
     * @param s the code to modify
     * @return the modified code
     */
    @Constraint("post: s.get().size>0")
    protected static void removeNewlinesForAssertStyleCommonsLogging(@Constraint("post: s.get().size<=s@pre.get().size") final IDocument s) {
        Pattern pattern = Pattern.compile("if\\s*\\(\\s*!\\s*assert(Pre|Post)Condition_(\\w*)_for_method_(\\w*)\\s*\\(([^\\)]*)\\)\\s*\\)\\s*\\{" + "\\s*org.apache.commons.logging.LogFactory.getLog\\s*\\(\\s*this.getClass\\s*\\(\\s*\\)\\s*\\)\\s*.\\s*(\\w*)\\s*\\(\\s*\"(.*)\"\\s*\\)\\s*;" + "\\s*}\\s*");
        Matcher matcher = pattern.matcher(s.get());
        try {
            int lengthDifference = 0;
            while (matcher.find()) {
                String replacement = "if(!assert" + Matcher.quoteReplacement(matcher.group(1)) + "Condition_" + Matcher.quoteReplacement(matcher.group(2)) + "_for_method_" + Matcher.quoteReplacement(matcher.group(3)) + "(" + Matcher.quoteReplacement(matcher.group(4)) + ")){org.apache.commons.logging.LogFactory.getLog(this.getClass())." + Matcher.quoteReplacement(matcher.group(5)) + "(\"" + Matcher.quoteReplacement(matcher.group(6)) + "\");}";
                int lengthOfMatchedString = matcher.end() - matcher.start();
                s.replace(lengthDifference + matcher.start(), lengthOfMatchedString, replacement);
                lengthDifference -= lengthOfMatchedString - replacement.length();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
