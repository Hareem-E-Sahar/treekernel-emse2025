package jtableaux.query.highlight;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * 
 * @author Leonardo Oliveira Moreira
 *
 * Classe que implementa um document para colorir palavras reservadas 
 */
public class HighlightKeywordDocument extends DefaultStyledDocument {

    /**
     *
     */
    private static final long serialVersionUID = 8158281978255098198L;

    private Element rootElement;

    private HashMap<String, Color> keywords;

    private MutableAttributeSet style;

    private Color commentColor = Color.magenta;

    private Color stringColor = Color.gray;

    private Color variableColor = Color.red;

    private Color attributeColor = Color.orange;

    private Pattern singleLineCommentDelimiter = Pattern.compile("//");

    private Pattern multiLineCommentDelimiterStart = Pattern.compile("\\(:");

    private Pattern multiLineCommentDelimiterEnd = Pattern.compile(":\\)");

    private Pattern stringPattern = Pattern.compile("\"");

    private Pattern variablePattern = Pattern.compile("\\$[a-zA-Z_0-9]+");

    private Pattern attributePattern = Pattern.compile("\\@[a-zA-Z_0-9]+");

    private Pattern numberPattern = Pattern.compile("[0-9]+");

    public HighlightKeywordDocument() {
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        rootElement = getDefaultRootElement();
        keywords = new HashMap<String, Color>();
        try {
            SAXBuilder parser = new SAXBuilder();
            Document doc = parser.build(getClass().getResourceAsStream("highlight.xml"));
            org.jdom.Element root = doc.getRootElement();
            List keywordsList = root.getChild("keywords").getChildren();
            Iterator keywordsIterator = keywordsList.iterator();
            while (keywordsIterator.hasNext()) {
                org.jdom.Element keyword = (org.jdom.Element) keywordsIterator.next();
                keywords.put(keyword.getValue().trim(), Color.blue);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            if (mlcEnd.find(mlcStart.end())) {
                highlightString(commentColor, mlcStart.start(), (mlcEnd.end() - mlcStart.start()), true, true);
            } else {
                highlightString(commentColor, mlcStart.start(), getLength(), true, true);
            }
        }
        Matcher slc = singleLineCommentDelimiter.matcher(text);
        while (slc.find()) {
            int line = rootElement.getElementIndex(slc.start());
            int endOffset = rootElement.getElement(line).getEndOffset() - 1;
            highlightString(commentColor, slc.start(), (endOffset - slc.start()), true, true);
        }
        int initial = -1;
        Matcher stringMatcher = stringPattern.matcher(text);
        while (stringMatcher.find()) {
            if (initial == -1) {
                initial = stringMatcher.start();
            } else {
                highlightString(stringColor, initial, ((stringMatcher.start() + (stringMatcher.end() - stringMatcher.start())) - initial), true, true);
                initial = -1;
            }
        }
        if (initial >= 0) {
            highlightString(stringColor, initial, getLength(), true, true);
            initial = -1;
        }
        Matcher variableMatcher = variablePattern.matcher(text);
        while (variableMatcher.find()) {
            highlightString(variableColor, variableMatcher.start(), variableMatcher.end() - variableMatcher.start(), true, true);
        }
        Matcher attributeMatcher = attributePattern.matcher(text);
        while (attributeMatcher.find()) {
            highlightString(attributeColor, attributeMatcher.start(), attributeMatcher.end() - attributeMatcher.start(), true, true);
        }
        Matcher numberMatcher = numberPattern.matcher(text);
        while (numberMatcher.find()) {
            highlightString(Color.BLUE, numberMatcher.start(), numberMatcher.end() - numberMatcher.start(), true, true);
        }
    }

    public void highlightString(Color col, int begin, int length, boolean flag, boolean bold) {
        StyleConstants.setForeground(style, col);
        StyleConstants.setBold(style, bold);
        setCharacterAttributes(begin, length, style, flag);
    }
}
