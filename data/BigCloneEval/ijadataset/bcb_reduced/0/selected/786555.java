package org.plide;

import java.awt.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

class SyntaxHighlighter implements Runnable {

    private int mOffset;

    private int mLength;

    private EditorPane mEdtPane;

    private static final String KEYWORD_SYTLE = "kw";

    private static final String REGULAR_STYLE = "reg";

    private static final String SINGLE_COMMENT_STYLE = "scmt";

    private static final String MULTIPLE_COMMENT_STYLE = "mcmt";

    private static final String STRING_STYLE = "str";

    private static SyntaxHighlighter instance = null;

    public static SyntaxHighlighter getInstance(EditorPane ep, int oset, int len) {
        if (instance == null) instance = new SyntaxHighlighter();
        instance.mEdtPane = ep;
        instance.mOffset = oset;
        instance.mLength = len;
        return instance;
    }

    private SyntaxHighlighter() {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        Style kwStyle = sc.addStyle(SyntaxHighlighter.KEYWORD_SYTLE, null);
        kwStyle.addAttribute(StyleConstants.Foreground, Color.red);
        kwStyle.addAttribute(StyleConstants.Bold, true);
        Style regStyle = sc.addStyle(SyntaxHighlighter.REGULAR_STYLE, null);
        regStyle.addAttribute(StyleConstants.Foreground, Color.black);
        Style sCmtStyle = sc.addStyle(SyntaxHighlighter.SINGLE_COMMENT_STYLE, null);
        sCmtStyle.addAttribute(StyleConstants.Foreground, Color.gray);
        sc.addStyle(SyntaxHighlighter.MULTIPLE_COMMENT_STYLE, sCmtStyle);
        Style strStyle = sc.addStyle(SyntaxHighlighter.STRING_STYLE, null);
        strStyle.addAttribute(StyleConstants.Foreground, Color.blue);
    }

    public void run() {
        StyledDocument doc = mEdtPane.getStyledDocument();
        mEdtPane.removeCaretListener(mEdtPane);
        String text = null;
        try {
            text = doc.getText(mOffset, mLength);
        } catch (BadLocationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        StyleContext sc = StyleContext.getDefaultStyleContext();
        Pattern p1 = Pattern.compile(".");
        Matcher m = p1.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(mOffset + m.start(), m.end() - m.start(), (AttributeSet) sc.getStyle(SyntaxHighlighter.REGULAR_STYLE), true);
        }
        Pattern p = Pattern.compile("begin|end");
        m = p.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(mOffset + m.start(), m.end() - m.start(), (AttributeSet) sc.getStyle(SyntaxHighlighter.KEYWORD_SYTLE), true);
        }
        Pattern strp = Pattern.compile("\".*\"");
        m = strp.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(mOffset + m.start(), m.end() - m.start(), (AttributeSet) sc.getStyle(SyntaxHighlighter.STRING_STYLE), true);
        }
        Pattern sp = Pattern.compile("--.*");
        m = sp.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(mOffset + m.start(), m.end() - m.start(), (AttributeSet) sc.getStyle(SyntaxHighlighter.SINGLE_COMMENT_STYLE), true);
        }
        Pattern mp = Pattern.compile("/\\*(.|[\n\r])*\\*/\\s*");
        m = mp.matcher(text);
        while (m.find()) {
            System.err.println("MATCHED");
            doc.setCharacterAttributes(mOffset + m.start(), m.end() - m.start(), (AttributeSet) sc.getStyle(SyntaxHighlighter.MULTIPLE_COMMENT_STYLE), true);
        }
        mEdtPane.addCaretListener(mEdtPane);
    }
}
