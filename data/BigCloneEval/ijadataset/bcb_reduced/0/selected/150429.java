package com.ialimuzaffar.iamlighlighting.editor;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import com.ialimuzaffar.iamlighlighting.HighlightingStyleLoader;

public class SyntaxHighlightingDemo extends JFrame {

    MyTextPane textPane = null;

    private static Style defaultStyle = null;

    public SyntaxHighlightingDemo() throws IOException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textPane = new MyTextPane();
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.getViewport().add(textPane, null);
        add(jScrollPane);
        textPane.setText(" ");
        pack();
        setSize(400, 400);
        setVisible(true);
        defaultStyle = textPane.getStyledDocument().addStyle("DEFAULT_STYLE", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public static void main(String[] args) throws Exception {
        SyntaxHighlightingDemo txt = new SyntaxHighlightingDemo();
        final JTextPane pane = txt.textPane;
        StyledDocument doc = pane.getStyledDocument();
        final HighlightingStyleLoader styler = new HighlightingStyleLoader(new File("d:/stylefile.txt"), doc);
        final Pattern p = Pattern.compile("\\W");
        pane.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                colorize(pane, styler);
            }
        });
        System.out.println(styler.toString());
        colorize(pane, styler);
    }

    public static void colorize(JTextPane pane, HighlightingStyleLoader styler) {
        StyledDocument doc = pane.getStyledDocument();
        int caretPos = pane.getCaretPosition();
        String word = pane.getText();
        pane.setText(word);
        pane.setCaretPosition(caretPos);
        doc.setParagraphAttributes(0, word.length(), defaultStyle, true);
        processTokens(doc, styler, word);
        processComments(doc, styler, word);
        processStrings(doc, styler, word);
    }

    private static void processComments(StyledDocument doc, HighlightingStyleLoader styler, String word) {
        Style commentStyle = doc.addStyle("COMMENT", null);
        StyleConstants.setForeground(commentStyle, new Color(0, 255, 0));
        Pattern p1 = Pattern.compile("(([/]{2}.*[^\"])\\n)|(([/]{2}.*[^\"])$)");
        Matcher m1 = p1.matcher(word);
        int start = 0;
        while (m1.find(start)) {
            doc.setCharacterAttributes(m1.start(), m1.end() - m1.start(), commentStyle, true);
            start = m1.end();
        }
        Pattern p2 = Pattern.compile("/\\*.*\\*/");
        Matcher m2 = p2.matcher(word);
        start = 0;
        while (m2.find(start)) {
            doc.setCharacterAttributes(m2.start(), m2.end() - m2.start(), commentStyle, true);
            start = m2.end();
        }
    }

    public static void processStrings(StyledDocument doc, HighlightingStyleLoader styler, String word) {
        Style commentStyle = doc.addStyle("STRINGS", null);
        StyleConstants.setForeground(commentStyle, new Color(0, 255, 0));
        Pattern p1 = Pattern.compile("(\".*\")|(\'.*\')");
        Matcher m1 = p1.matcher(word);
        int start = 0;
        while (m1.find(start)) {
            System.out.println("MATCH start=" + m1.start() + " end=" + m1.end());
            doc.setCharacterAttributes(m1.start(), m1.end() - m1.start(), commentStyle, true);
            start = m1.end();
        }
    }

    public static void processTokens(StyledDocument doc, HighlightingStyleLoader styler, String word) {
        String delim = "[\\W]";
        Pattern p = Pattern.compile(delim);
        Matcher m = p.matcher(word);
        int start = 0;
        while (m.find(start)) {
            start = m.start();
            int end = m.end() - start;
            if (m.group().trim().length() > 0) {
                Style applyStyle = styler.getStyle(m.group());
                if (applyStyle != null) {
                    doc.setCharacterAttributes(start, end, applyStyle, true);
                }
            }
            start = m.end();
        }
        String[] t = word.split(delim);
        int startpt = 0;
        for (String tkn : t) {
            if (tkn.length() < 1) continue;
            startpt = word.indexOf(tkn, startpt);
            int endpt_orig = startpt + tkn.length();
            int endpt = tkn.length();
            Style applyStyle = styler.getStyle(word.substring(startpt, endpt_orig));
            if (applyStyle != null) {
                doc.setCharacterAttributes(startpt, endpt, applyStyle, true);
            } else {
                doc.setCharacterAttributes(startpt, endpt, defaultStyle, true);
            }
            startpt = endpt_orig;
        }
    }
}
