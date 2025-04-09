package org.sweetinsanity.portablog;

import java.util.regex.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Highlights HTML syntax in a Document.
 * Inspired by Adam Wilson's HighlightingStyledDocument class
 * (http://coweb.cc.gatech.edu/mediaComp-plan/uploads/95/HighlightingStyledDocument.1.java).
 * @author Nathan Piper
 * @version $Id: HighlightingStyledDocument.java,v 1.1.1.1 2004/06/10 01:17:39 npiper Exp $
 */
public class HighlightingStyledDocument extends DefaultStyledDocument {

    private SimpleAttributeSet tagStyle = new SimpleAttributeSet();

    public HighlightingStyledDocument() {
        super();
        SimpleAttributeSet keyStyle = new SimpleAttributeSet();
        StyleConstants.setBold(keyStyle, true);
        tagStyle = keyStyle;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offs, str, a);
        colourText(offs, str.length());
    }

    protected void fireRemoveUpdate(DocumentEvent e) {
        int offset = e.getOffset();
        int length = e.getLength();
        colourText(offset - 1, 0);
        super.fireRemoveUpdate(e);
    }

    public void colourText(int offset, int length) {
        try {
            Element root = this.getDefaultRootElement();
            int startElementIndex = root.getElementIndex(offset);
            int endElementIndex = root.getElementIndex(offset + length);
            int startOff = root.getElement(startElementIndex).getStartOffset();
            int endOff = root.getElement(endElementIndex).getEndOffset();
            String text = getText(startOff, endOff - startOff);
            this.setCharacterAttributes(startOff, endOff - startOff, new SimpleAttributeSet(), true);
            Pattern tagReg = Pattern.compile("<(.|\\n)*?>");
            Matcher matches = tagReg.matcher(text);
            while (matches.find()) {
                this.setCharacterAttributes(startOff + matches.start(), matches.end() - matches.start(), tagStyle, true);
            }
        } catch (Exception e) {
        }
    }
}
