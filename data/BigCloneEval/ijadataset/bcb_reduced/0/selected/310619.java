package ua.org.groovy.gs.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class GroovyDocument extends DefaultStyledDocument {

    private Element rootElement;

    private HashMap<String, Color> keywords;

    private MutableAttributeSet style;

    private Color commentColor = new Color(0x3F7F5F);

    private Color quoteColor = Color.yellow;

    private Pattern singleLineCommentDelimter = Pattern.compile("//");

    private Pattern multiLineCommentDelimiterStart = Pattern.compile("/\\*");

    private Pattern multiLineCommentDelimiterEnd = Pattern.compile("\\*/");

    private Pattern quoteDelimiter = Pattern.compile("\"");

    public GroovyDocument() {
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        rootElement = getDefaultRootElement();
        keywords = new HashMap<String, Color>();
        keywords.put("abstract", new Color(0x7F0055));
        keywords.put("interface", new Color(0x7F0055));
        keywords.put("class", new Color(0x7F0055));
        keywords.put("extends", new Color(0x7F0055));
        keywords.put("implements", new Color(0x7F0055));
        keywords.put("package", new Color(0x7F0055));
        keywords.put("import", new Color(0x7F0055));
        keywords.put("private", new Color(0x7F0055));
        keywords.put("protected", new Color(0x7F0055));
        keywords.put("public", new Color(0x7F0055));
        keywords.put("void", new Color(0x7F0055));
        keywords.put("boolean", new Color(0x7F0055));
        keywords.put("char", new Color(0x7F0055));
        keywords.put("byte", new Color(0x7F0055));
        keywords.put("float", new Color(0x7F0055));
        keywords.put("double", new Color(0x7F0055));
        keywords.put("long", new Color(0x7F0055));
        keywords.put("short", new Color(0x7F0055));
        keywords.put("int", new Color(0x7F0055));
        keywords.put("true", new Color(0x7F0055));
        keywords.put("false", new Color(0x7F0055));
        keywords.put("const", new Color(0x7F0055));
        keywords.put("null", new Color(0x7F0055));
        keywords.put("break", new Color(0x7F0055));
        keywords.put("case", new Color(0x7F0055));
        keywords.put("catch", new Color(0x7F0055));
        keywords.put("continue", new Color(0x7F0055));
        keywords.put("default", new Color(0x7F0055));
        keywords.put("do", new Color(0x7F0055));
        keywords.put("else", new Color(0x7F0055));
        keywords.put("final", new Color(0x7F0055));
        keywords.put("finally", new Color(0x7F0055));
        keywords.put("for", new Color(0x7F0055));
        keywords.put("if", new Color(0x7F0055));
        keywords.put("instanceof", new Color(0x7F0055));
        keywords.put("native", new Color(0x7F0055));
        keywords.put("new", new Color(0x7F0055));
        keywords.put("return", new Color(0x7F0055));
        keywords.put("static", new Color(0x7F0055));
        keywords.put("super", new Color(0x7F0055));
        keywords.put("switch", new Color(0x7F0055));
        keywords.put("synchronized", new Color(0x7F0055));
        keywords.put("this", new Color(0x7F0055));
        keywords.put("throw", new Color(0x7F0055));
        keywords.put("throws", new Color(0x7F0055));
        keywords.put("transient", new Color(0x7F0055));
        keywords.put("try", new Color(0x7F0055));
        keywords.put("volatile", new Color(0x7F0055));
        keywords.put("while", new Color(0x7F0055));
        keywords.put("def", new Color(0x7F0055));
        keywords.put("in", new Color(0x7F0055));
        keywords.put("as", new Color(0x7F0055));
        keywords.put("assert", new Color(0x7F0055));
        keywords.put("print", new Color(0x0099FF));
        keywords.put("println", new Color(0x0099FF));
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
        highlightString(Color.black, 0, getLength(), true, false);
        Set<String> keyw = keywords.keySet();
        for (String keyword : keyw) {
            Color col = keywords.get(keyword);
            Pattern p = Pattern.compile("\\b" + keyword + "\\b");
            Matcher m = p.matcher(text);
            while (m.find()) {
                highlightString(col, m.start(), keyword.length(), true, true);
            }
        }
        Matcher mlcStart = multiLineCommentDelimiterStart.matcher(text);
        Matcher mlcEnd = multiLineCommentDelimiterEnd.matcher(text);
        while (mlcStart.find()) {
            if (mlcEnd.find(mlcStart.end())) highlightString(commentColor, mlcStart.start(), (mlcEnd.end() - mlcStart.start()), true, false); else highlightString(commentColor, mlcStart.start(), getLength(), true, false);
        }
        Matcher slc = singleLineCommentDelimter.matcher(text);
        while (slc.find()) {
            int line = rootElement.getElementIndex(slc.start());
            int endOffset = rootElement.getElement(line).getEndOffset() - 1;
            highlightString(commentColor, slc.start(), (endOffset - slc.start()), true, true);
        }
    }

    public void highlightString(Color col, int begin, int length, boolean flag, boolean bold) {
        StyleConstants.setForeground(style, col);
        StyleConstants.setBold(style, bold);
        setCharacterAttributes(begin, length, style, flag);
    }

    public String getLineString(String content, int line) {
        Element lineElement = rootElement.getElement(line);
        return content.substring(lineElement.getStartOffset(), lineElement.getEndOffset() - 1);
    }
}
