public class Test {    @Constraint("post: s.get().size>0")
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
}