package net.jfellow.common.util.find;

import java.util.regex.*;

/**
 *
 * @author  @author@
 */
public class ExpressionFinderThread extends Thread {

    private String text;

    private String regExpr;

    private boolean stopped;

    private FoundExpressions expressions;

    private ISearchable caller;

    /** Creates a new instance of ExpressionFinder */
    public ExpressionFinderThread(String text, String regExpr, ISearchable caller) {
        this.text = text;
        this.regExpr = regExpr;
        this.caller = caller;
    }

    public void run() {
        this.find();
        if (!stopped) {
            this.caller.updateFoundExpressions(this.expressions);
        }
    }

    public void stopFinding() {
        this.stopped = true;
    }

    private void find() {
        this.expressions = null;
        Pattern p = Pattern.compile(regExpr);
        Matcher m = p.matcher(text);
        while (m.find()) {
            if (this.stopped) {
                System.out.println("Finder was stopped.");
                this.expressions = null;
                return;
            }
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
    }
}
