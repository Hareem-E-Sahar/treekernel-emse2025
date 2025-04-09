package net.sourceforge.jcpusim.ui;

import net.sourceforge.jcpusim.ui.tabs.Document;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.graphics.Color;
import net.sourceforge.jcpusim.Core;

public class SyntaxHighlighter implements LineStyleListener, LineBackgroundListener, TraverseListener, MouseListener {

    /** shared resources and methods */
    protected Core core;

    /** document */
    protected Document document;

    /**
	 * 
	 * @param core - shared resources and methods
	 * @param document
	 */
    public SyntaxHighlighter(Core core, Document document) {
        this.core = core;
        this.document = document;
    }

    public void lineGetStyle(LineStyleEvent e) {
        java.util.List styles = new java.util.ArrayList();
        Pattern pattern;
        Matcher matcher;
        if (e.lineText.trim().length() > 0) {
            if (e.lineText.trim().charAt(0) != ';') {
                for (int i = 0; i < core.getSyntaxParser().getStyleCount(); i++) {
                    if (i != core.getSyntaxParser().TYPE_COMMENT) {
                        pattern = Pattern.compile(core.getSyntaxParser().getRegularExpression(i), Pattern.CASE_INSENSITIVE);
                        matcher = pattern.matcher("\n" + e.lineText.toUpperCase().split(";")[0] + "\n");
                        while (matcher.find()) {
                            styles.add(new StyleRange(e.lineOffset + matcher.start() - 1, matcher.end() - matcher.start(), core.getSyntaxParser().getColor(i), null, core.getSyntaxParser().getStyle(i)));
                        }
                    }
                }
            }
            pattern = Pattern.compile(core.getSyntaxParser().getRegularExpression(core.getSyntaxParser().TYPE_COMMENT));
            matcher = pattern.matcher(e.lineText);
            if (matcher.find()) styles.add(new StyleRange(e.lineOffset + matcher.start(), e.lineText.length() - matcher.start(), core.getSyntaxParser().getColor(core.getSyntaxParser().TYPE_COMMENT), null, core.getSyntaxParser().getStyle(core.getSyntaxParser().TYPE_COMMENT)));
            e.styles = (StyleRange[]) styles.toArray(new StyleRange[0]);
        }
    }

    public void lineGetBackground(LineBackgroundEvent e) {
        if (e.lineOffset < document.getText().getCaretOffset() + 1 && document.getText().getCaretOffset() - 1 < e.lineOffset + e.lineText.length()) e.lineBackground = new Color(core.getDisplay(), 220, 220, 255); else e.lineBackground = document.getText().getBackground();
        document.updateLineNumbers();
    }

    public void keyTraversed(TraverseEvent e) {
        document.getText().setBackground(null);
    }

    public void mouseDown(org.eclipse.swt.events.MouseEvent arg0) {
        document.getText().setBackground(null);
    }

    public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent arg0) {
    }

    public void mouseUp(org.eclipse.swt.events.MouseEvent arg0) {
    }
}
