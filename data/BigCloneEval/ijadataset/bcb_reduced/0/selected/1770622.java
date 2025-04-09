package net.jfellow.common.util.find;

import java.util.regex.*;

/**
 *
 * @author  @author@
 */
public class ExpressionFinder {

    private String text;

    /** Creates a new instance of ExpressionFinder */
    public ExpressionFinder(String text) {
        this.text = text;
    }

    public FoundExpressions getFoundExpressions(String regExpr) {
        FoundExpressions expressions = null;
        Pattern p = Pattern.compile(regExpr);
        Matcher m = p.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (expressions == null) {
                expressions = new FoundExpressions();
            }
            Selection selection = new Selection();
            selection.setStart(start);
            selection.setEnd(end);
            expressions.add(selection);
        }
        return expressions;
    }
}
