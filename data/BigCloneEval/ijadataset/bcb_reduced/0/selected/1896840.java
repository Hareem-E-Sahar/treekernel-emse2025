package ca.compsci.opent.ide;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;

public class HighlightedDocument extends DefaultStyledDocument {

    private Element rootElement;

    private HashMap<String, HighlightStyle> keywords;

    private MutableAttributeSet style;

    private HighlightStyle defaultStyle = new HighlightStyle(Color.black, false, false);

    private HighlightStyle commentStyle = new HighlightStyle(new Color(180, 0, 0), false, true);

    private Pattern singleCommentDelim = Pattern.compile("%");

    private Pattern multiCommentDelimStart = Pattern.compile("/\\*");

    private Pattern multiCommentDelimEnd = Pattern.compile("\\*/");

    public HighlightedDocument() {
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        rootElement = getDefaultRootElement();
        keywords = new HashMap<String, HighlightStyle>();
        keywords.put("\\bput\\b", new HighlightStyle(new Color(0, 128, 0), true, false));
        keywords.put("\\bget\\b", new HighlightStyle(new Color(0, 128, 0), true, false));
        keywords.put("\\bvar\\b", new HighlightStyle(Color.DARK_GRAY, true, false));
        keywords.put("\\bint\\b", new HighlightStyle(Color.orange, true, false));
        keywords.put("\\bboolean\\b", new HighlightStyle(Color.orange, true, false));
        keywords.put("\\bstring\\b", new HighlightStyle(Color.orange, true, false));
        keywords.put("\\breal\\b", new HighlightStyle(Color.orange, true, false));
        keywords.put("\\belse\\b", new HighlightStyle(Color.black, true, false));
        keywords.put("\\bfor\\b", new HighlightStyle(Color.black, true, false));
        keywords.put("\\bif\\b", new HighlightStyle(Color.black, true, false));
        keywords.put("\\belsif\\b", new HighlightStyle(Color.black, true, false));
        keywords.put("\\bend\\b", new HighlightStyle(Color.black, true, false));
        keywords.put("\\bthen\\b", new HighlightStyle(Color.black, true, false));
        keywords.put("[0-9]+", new HighlightStyle(Color.BLUE, false, false));
        keywords.put("\"(.*)\"", new HighlightStyle(new Color(0, 0, 200), true, false));
        style = new SimpleAttributeSet();
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        super.insertString(offset, str, attr);
        processChangedLines(offset, str.length());
    }

    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        processChangedLines(offset, length);
    }

    public void processChangedLines(int offset, int length) throws BadLocationException {
        String text = getText(0, getLength());
        highlightString(0, getLength(), defaultStyle);
        Set<String> keyw = keywords.keySet();
        for (String keyword : keyw) {
            Pattern p = Pattern.compile(keyword);
            Matcher m = p.matcher(text);
            while (m.find()) {
                highlightString(m.start(), m.end() - m.start(), keywords.get(keyword));
            }
        }
        Matcher mlcStart = multiCommentDelimStart.matcher(text);
        Matcher mlcEnd = multiCommentDelimEnd.matcher(text);
        while (mlcStart.find()) {
            if (mlcEnd.find(mlcStart.end())) {
                highlightString(mlcStart.start(), (mlcEnd.end() - mlcStart.start()), commentStyle);
            } else {
                highlightString(mlcStart.start(), getLength(), commentStyle);
            }
        }
        Matcher slc = singleCommentDelim.matcher(text);
        while (slc.find()) {
            int line = rootElement.getElementIndex(slc.start());
            int endOffset = rootElement.getElement(line).getEndOffset() - 1;
            highlightString(slc.start(), (endOffset - slc.start()), commentStyle);
        }
    }

    public void highlightString(int begin, int length, HighlightStyle textStyle) {
        StyleConstants.setForeground(style, textStyle.clr);
        StyleConstants.setBold(style, textStyle.isBold);
        StyleConstants.setItalic(style, textStyle.isItalic);
        setCharacterAttributes(begin, length, style, true);
    }
}
