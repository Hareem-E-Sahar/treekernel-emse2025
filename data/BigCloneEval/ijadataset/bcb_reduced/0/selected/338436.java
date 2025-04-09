package net.yura.mobile.gui.components;

import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Font;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.gui.layout.XHTMLLoader;
import net.yura.mobile.gui.plaf.Style;

/**
 * @author Yura Mamyrin
 * @see javax.swing.text.TextPane
 */
public class TextPane extends Component {

    private String text = "";

    private Vector sortedElemsList = new Vector();

    private Vector lineFragments = new Vector();

    private Vector focusableElems = new Vector();

    private int lastLineX;

    private int focusComponentIdx;

    private ActionListener actionListener;

    protected String getDefaultName() {
        return "TextPane";
    }

    public void paintComponent(Graphics2D g) {
        TextStyle focusElem = getFocusElementStyle();
        int bgColor = getBackground();
        int topClipY = g.getClipY();
        int bottomClipY = topClipY + g.getClipHeight();
        int numLineFrags = lineFragments.size();
        for (int i = 0; i < numLineFrags; i++) {
            LineFragment lineFrag = (LineFragment) lineFragments.elementAt(i);
            if (lineFrag.y + lineFrag.h < topClipY || lineFrag.y > bottomClipY) {
                continue;
            }
            TextStyle style = lineFrag.style;
            Icon icon = style.getIcon();
            int fragState = (style == focusElem) ? Style.FOCUSED : Style.ALL;
            int bgStyleColor = style.getBackground(fragState);
            Border boder = style.getBorder(fragState);
            if (boder != null) {
                g.translate(lineFrag.x, lineFrag.y);
                boder.paintBorder(this, g, lineFrag.w, lineFrag.h);
                g.translate(-lineFrag.x, -lineFrag.y);
            }
            if (bgColor != bgStyleColor && bgStyleColor != Style.NO_COLOR) {
                g.setColor(bgStyleColor);
                g.fillRect(lineFrag.x, lineFrag.y, lineFrag.w, lineFrag.h);
            }
            if (icon == null) {
                String str = text.substring(lineFrag.startOffset, lineFrag.endOffset);
                if (str.length() > 0) {
                    g.setColor(style.getForeground(fragState));
                    Font f = getFont(style, fragState);
                    g.setFont(f);
                    g.drawString(str, lineFrag.x, lineFrag.y);
                }
            } else {
                icon.paintIcon(this, g, lineFrag.x, lineFrag.y);
            }
        }
    }

    protected void workoutMinimumSize() {
        if (getPreferredWidth() != -1) {
            width = getPreferredWidth();
            if (width != widthUsed) {
                height = doLayout();
                heightUsed = height;
            }
            height = heightUsed;
        } else {
            width = 10;
            height = (widthUsed < 0) ? 10 : heightUsed;
        }
    }

    int widthUsed = -1;

    int heightUsed = -1;

    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (width != widthUsed) {
            int oldh = height;
            height = doLayout();
            heightUsed = height;
            if (oldh != height) {
                DesktopPane.mySizeChanged(this);
            }
        }
    }

    public String getText() {
        return text;
    }

    /**
     * like Swing this method IS thread safe
     * @param text1 the text to set, if the text starts with &lt;html&gt; then it will be passed as html
     * @see javax.swing.JEditorPane#setText(java.lang.String) JEditorPane.setText
     */
    public void setText(String text1) {
        sortedElemsList.removeAllElements();
        lineFragments.removeAllElements();
        focusableElems.removeAllElements();
        widthUsed = -1;
        heightUsed = -1;
        if (text1.startsWith("<html>")) {
            text = "";
            XHTMLLoader loader = new XHTMLLoader();
            loader.gotResult(this, text1);
        } else {
            text = text1;
        }
    }

    public void setValue(Object obj) {
        if (obj instanceof String) {
            setText((String) obj);
        }
    }

    public void append(String text1) {
        widthUsed = -1;
        if (text1.startsWith("<html>")) {
            XHTMLLoader loader = new XHTMLLoader();
            loader.gotResult(this, text1);
        } else {
            text = text + text1;
        }
    }

    public void setCharacterAttributes(int offset, int length, TextStyle style) {
        if (offset >= 0 && length >= 1 && text != null && offset < text.length()) {
            int endOffset = Math.min(offset + length, text.length());
            Element elem = new Element(style, offset, endOffset);
            insertSortedElement(sortedElemsList, elem);
        }
    }

    public void setParagraphAttributes(int offset, int length, TextStyle style) {
        if (offset >= 0 && length >= 0 && text != null && offset < text.length()) {
            int lowerLfIdx = 0;
            int higherLfIdx = text.length();
            int lineFeedIdx = 0;
            while (true) {
                lineFeedIdx = text.indexOf('\n', lineFeedIdx);
                if (lineFeedIdx <= 0) {
                    break;
                } else if (lineFeedIdx < offset) {
                    lowerLfIdx = lineFeedIdx;
                } else if (lineFeedIdx > offset + length) {
                    higherLfIdx = lineFeedIdx + 1;
                    break;
                }
                lineFeedIdx++;
            }
            Element elem = new Element(style, lowerLfIdx, higherLfIdx);
            elem.isParagraph = true;
            insertSortedElement(sortedElemsList, elem);
        }
    }

    public void setActionListener(ActionListener l) {
        this.actionListener = l;
    }

    public void focusLost() {
        super.focusLost();
        repaint();
    }

    public void focusGained() {
        super.focusGained();
        makeVisible(focusComponentIdx, false);
    }

    public boolean processKeyEvent(KeyEvent event) {
        int key = event.getIsDownKey();
        if (key == 0) {
            return false;
        }
        int action = event.getKeyAction(key);
        if (action == Canvas.FIRE) {
            TextStyle style = getFocusElementStyle();
            if (style != null && actionListener != null) {
                actionListener.actionPerformed(style.getAction());
            }
            return true;
        }
        int next = focusComponentIdx;
        next = (action == Canvas.DOWN || action == Canvas.RIGHT) ? next + 1 : (action == Canvas.UP || action == Canvas.LEFT) ? next - 1 : next;
        next = (next < 0) ? 0 : (next >= focusableElems.size()) ? focusableElems.size() - 1 : next;
        if (next != focusComponentIdx) {
            if (makeVisible(next, true)) {
                focusComponentIdx = next;
            }
            repaint();
            return true;
        }
        return false;
    }

    public void updateUI() {
        super.updateUI();
        if (ta != null) ta.updateUI();
        if (sortedElemsList != null) {
            for (int i = 0; i < sortedElemsList.size(); i++) {
                Element elem = (Element) sortedElemsList.elementAt(i);
                elem.style.updateUI();
            }
            widthUsed = -1;
            heightUsed = -1;
        }
    }

    private boolean makeVisible(int styleIdx, boolean smart) {
        if (focusableElems.size() == 0) {
            return false;
        }
        int MAX = Integer.MAX_VALUE;
        int leftX = MAX, rightX = 0;
        int topY = MAX, bottomY = 0;
        TextStyle style = (TextStyle) focusableElems.elementAt(styleIdx);
        for (int i = 0; i < lineFragments.size(); i++) {
            LineFragment frag = (LineFragment) lineFragments.elementAt(i);
            if (frag.style == style) {
                leftX = Math.min(leftX, frag.x);
                rightX = Math.max(rightX, frag.x + frag.w);
                topY = Math.min(topY, frag.y);
                bottomY = Math.max(bottomY, frag.y + frag.h);
            } else if (leftX < MAX) {
                break;
            }
        }
        return scrollRectToVisible(leftX, topY, rightX - leftX, bottomY - topY, smart);
    }

    public void processMouseEvent(int type, int x, int y, KeyEvent keys) {
        super.processMouseEvent(type, x, y, keys);
        if (type == DesktopPane.PRESSED && actionListener != null && isFocusOwner()) {
            for (int i = 0; i < lineFragments.size(); i++) {
                LineFragment frag = (LineFragment) lineFragments.elementAt(i);
                if (x >= frag.x && x <= frag.x + frag.w && y >= frag.y && y <= frag.y + frag.h) {
                    pressLink(frag);
                    break;
                }
            }
        }
    }

    /**
     * not swing
     */
    public boolean pressLink(String linkText) {
        if (actionListener != null) {
            for (int i = 0; i < lineFragments.size(); i++) {
                LineFragment frag = (LineFragment) lineFragments.elementAt(i);
                String txt = text.substring(frag.startOffset, frag.endOffset);
                if (linkText.equalsIgnoreCase(txt)) {
                    if (pressLink(frag)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean pressLink(LineFragment frag) {
        TextStyle style = frag.style;
        if (style.getAction() != null) {
            int focusIdx = focusableElems.indexOf(style);
            makeVisible(focusIdx, false);
            if (focusIdx != focusComponentIdx) {
                focusComponentIdx = focusIdx;
                repaint();
            }
            actionListener.actionPerformed(style.getAction());
            return true;
        }
        return false;
    }

    private TextStyle getFocusElementStyle() {
        int idx = focusComponentIdx;
        return (idx < 0 || idx >= focusableElems.size() || !isFocusOwner()) ? null : (TextStyle) focusableElems.elementAt(idx);
    }

    private int doLayout() {
        widthUsed = width;
        lineFragments.removeAllElements();
        focusableElems.removeAllElements();
        if (text == null || text.length() == 0 || width <= 0) {
            return 0;
        }
        lastLineX = 0;
        Vector elemStyleSortedStack = new Vector();
        int startFragIdx = 0;
        int numElems = sortedElemsList.size();
        for (int i = 0; i < numElems; i++) {
            Element currentElem = (Element) sortedElemsList.elementAt(i);
            int nextStartFragIdx = currentElem.startOffset;
            if (nextStartFragIdx == startFragIdx) {
                elemStyleSortedStack.addElement(currentElem);
                continue;
            }
            startFragIdx = addClosingLineFragments(elemStyleSortedStack, startFragIdx, nextStartFragIdx);
            startFragIdx = addLineFragments(elemStyleSortedStack, startFragIdx, nextStartFragIdx);
            insertSortedElement(elemStyleSortedStack, currentElem);
        }
        startFragIdx = addClosingLineFragments(elemStyleSortedStack, startFragIdx, text.length());
        addLineFragments(elemStyleSortedStack, startFragIdx, text.length());
        focusable = (focusableElems.size() > 0);
        return layoutVerticaly();
    }

    private int addClosingLineFragments(Vector elemStyleSortedStack, int startFragIdx, int nextStartFragIdx) {
        int nClosesFound;
        do {
            nClosesFound = 0;
            int smallerEndOffset = nextStartFragIdx;
            int smallerEndOffsetIdx = -1;
            for (int j = elemStyleSortedStack.size() - 1; j >= 0; j--) {
                Element elem = (Element) elemStyleSortedStack.elementAt(j);
                int elemEndOffset = elem.endOffset;
                if (elemEndOffset <= nextStartFragIdx) {
                    nClosesFound++;
                }
                if (elemEndOffset <= smallerEndOffset) {
                    smallerEndOffset = elemEndOffset;
                    smallerEndOffsetIdx = j;
                }
            }
            if (nClosesFound > 0) {
                startFragIdx = addLineFragments(elemStyleSortedStack, startFragIdx, smallerEndOffset);
                elemStyleSortedStack.removeElementAt(smallerEndOffsetIdx);
            }
        } while (nClosesFound > 1);
        return startFragIdx;
    }

    private int addLineFragments(Vector currentElemStack, int startIndex, int endIndex) {
        if (startIndex >= endIndex) {
            return startIndex;
        }
        TextStyle style = getCombinedStyle(currentElemStack);
        int nOldFragments = lineFragments.size();
        String elemText = text.substring(startIndex, endIndex);
        if (style.getIcon() == null) {
            addLineTextFragments(elemText, style, startIndex);
        } else {
            addLineImageFragments(style);
        }
        if (style.getAction() != null && nOldFragments != lineFragments.size()) {
            focusableElems.addElement(style);
        }
        return endIndex;
    }

    private void addLineTextFragments(String elemText, TextStyle style, int startIndex) {
        Font f = getFont(style, Style.ALL);
        int borderH = getBorderHeight(style);
        int borderW = getBorderWidth(style);
        int fragH = f.getHeight() + borderH;
        int[] lines = getLines(elemText, f, lastLineX, width - borderW);
        int startFragIdx = startIndex;
        for (int j = 0; j < lines.length + 1; j++) {
            int endFragIdx = (j == lines.length) ? elemText.length() : lines[j];
            endFragIdx += startIndex;
            if (endFragIdx > startFragIdx) {
                String lastLineText = text.substring(startFragIdx, endFragIdx);
                if ("\n".equals(lastLineText)) {
                    if (lastLineX <= 0) {
                        lineFragments.addElement(new LineFragment(0, 0, 0, fragH, style, 0, 0));
                    }
                    lastLineX = 0;
                } else {
                    boolean trimSpace = (j < lines.length);
                    lastLineText = trimStringRightSide(lastLineText, trimSpace);
                    int fragW = f.getWidth(lastLineText);
                    if (fragW > 0) {
                        fragW += borderW;
                        LineFragment lineFrag = new LineFragment(lastLineX, 0, fragW, fragH, style, startFragIdx, startFragIdx + lastLineText.length());
                        lineFragments.addElement(lineFrag);
                    }
                    lastLineX = (j == lines.length) ? lastLineX + fragW : 0;
                }
            } else {
                lastLineX = 0;
            }
            startFragIdx = endFragIdx;
        }
    }

    private int getBorderWidth(TextStyle style) {
        Border b = style.getBorder(Style.ALL);
        return (b == null) ? 0 : (b.getLeft() + b.getRight());
    }

    private int getBorderHeight(TextStyle style) {
        Border b = style.getBorder(Style.ALL);
        return (b == null) ? 0 : (b.getTop() + b.getBottom());
    }

    private void addLineImageFragments(TextStyle style) {
        Icon icon = style.getIcon();
        int imgW = icon.getIconWidth();
        int imgH = icon.getIconHeight();
        if (imgH > 0 && imgW > 0) {
            imgW += getBorderWidth(style);
            imgH += getBorderHeight(style);
            if (lastLineX + imgW > width) {
                lastLineX = 0;
            }
            LineFragment lineFrag = new LineFragment(lastLineX, 0, imgW, imgH, style, 0, 0);
            lineFragments.addElement(lineFrag);
            lastLineX += imgW;
            if (lastLineX >= width || imgW >= width) {
                lastLineX = 0;
            }
        }
    }

    private int layoutVerticaly() {
        int numFrags = lineFragments.size();
        if (numFrags == 0) {
            return 0;
        }
        int padding = 2;
        int lineY = -padding;
        int lineH = 0;
        int lineW = 0;
        int startLineFragIdx = 0;
        LineFragment lineFrag = null;
        for (int i = 0; i <= numFrags; i++) {
            if (i < numFrags) {
                lineFrag = (LineFragment) lineFragments.elementAt(i);
            }
            if (lineFrag.x <= 0 || i == numFrags) {
                int lineX = 0;
                for (int j = startLineFragIdx; j < i; j++) {
                    LineFragment frag = (LineFragment) lineFragments.elementAt(j);
                    int leftPadding = getParagraphLeftPadding(frag.style, lineW);
                    int fragW = frag.w;
                    frag.y = lineY + (lineH - frag.h);
                    frag.x = lineX + leftPadding;
                    Border b = frag.style.getBorder(Style.ALL);
                    if (b != null) {
                        frag.y += b.getTop();
                        frag.x += b.getLeft();
                        frag.h -= b.getTop() + b.getBottom();
                        frag.w -= b.getLeft() + b.getRight();
                    }
                    lineX += fragW;
                }
                lineY += lineH + padding;
                startLineFragIdx = i;
                lineH = lineFrag.h;
                lineW = lineFrag.w;
            } else {
                lineH = Math.max(lineH, lineFrag.h);
                lineW += lineFrag.w;
            }
        }
        return lineY;
    }

    private int getParagraphLeftPadding(TextStyle style, int lineW) {
        int align = style.getAlignment();
        return (align == TextStyle.ALIGN_CENTER) ? (width - lineW) / 2 : (align == TextStyle.ALIGN_RIGHT) ? (width - lineW) : 0;
    }

    private String trimStringRightSide(String str, boolean trimSpace) {
        int startLen = str.length();
        int endLen = startLen;
        if (endLen > 0 && str.charAt(endLen - 1) == '\n') {
            endLen--;
        }
        if (trimSpace && endLen > 0 && str.charAt(endLen - 1) == ' ') {
            endLen--;
        }
        return (endLen != startLen) ? str.substring(0, endLen) : str;
    }

    /**
     * Takes a list of Elements and uses its styles to produce the "merged"
     * result.
     */
    private TextStyle getCombinedStyle(Vector elemList) {
        TextStyle paragStyle = new TextStyle();
        TextStyle charsStyle = new TextStyle();
        paragStyle.setBackground(getBackground());
        paragStyle.setForeground(getForeground());
        paragStyle.setAlignment(TextStyle.ALIGN_LEFT);
        for (int i = 0; i < elemList.size(); i++) {
            Element elem = (Element) elemList.elementAt(i);
            TextStyle s = (elem.isParagraph) ? paragStyle : charsStyle;
            s.putAll(elem.style);
        }
        paragStyle.setIcon(null);
        int align = paragStyle.getAlignment();
        paragStyle.putAll(charsStyle);
        paragStyle.setAlignment(align);
        return paragStyle;
    }

    TextArea ta = new TextArea();

    private int[] getLines(String str, Font f, int startX, int w) {
        return ta.getLines(str, f, 0, w - startX, w);
    }

    /**
     * Inserts the element inside the vector, sorted in growing order by
     * element startOffeset. If there is already an element with the same
     * startOffeset, it will add it with the highest index as possible.
     * @param v Vector used to insert the new element. Vector must be empty or
     * sorted.
     * @param elem New element to insert.
     */
    private void insertSortedElement(Vector v, Element elem) {
        int low = 0;
        int high = v.size();
        while (low < high) {
            int midle = (low + high) / 2;
            Element midElem = (Element) v.elementAt(midle);
            if (elem.startOffset < midElem.startOffset) {
                high = midle;
            } else {
                low = midle + 1;
            }
        }
        v.insertElementAt(elem, low);
    }

    private Font getFont(TextStyle ts, int state) {
        Font f = ts.getFont(state);
        if (f != null) {
            return f;
        }
        f = theme.getFont(state);
        int face = javax.microedition.lcdui.Font.FACE_SYSTEM;
        int size = javax.microedition.lcdui.Font.SIZE_MEDIUM;
        javax.microedition.lcdui.Font sysf = f.getFont();
        if (sysf != null) {
            face = sysf.getFace();
            size = sysf.getSize();
        }
        int style = javax.microedition.lcdui.Font.STYLE_PLAIN;
        if (ts.isBold()) {
            style |= javax.microedition.lcdui.Font.STYLE_BOLD;
        }
        if (ts.isItalic()) {
            style |= javax.microedition.lcdui.Font.STYLE_ITALIC;
        }
        if (ts.isUnderline()) {
            style |= javax.microedition.lcdui.Font.STYLE_UNDERLINED;
        }
        if (style != javax.microedition.lcdui.Font.STYLE_PLAIN) {
            return new Font(face, style, size);
        }
        return f;
    }

    /**
    * @see javax.TextStyle.text.AttributeSet
    */
    public static class TextStyle extends Style {

        private int alignment = -1;

        private byte textStyle;

        private Icon icon;

        private String action;

        private String name;

        /**
         * @see javax.swing.text.StyleConstants#ALIGN_CENTER StyleConstants.ALIGN_CENTER
         */
        public static final int ALIGN_CENTER = 1;

        /**
         * @see javax.swing.text.StyleConstants#ALIGN_JUSTIFIED StyleConstants.ALIGN_JUSTIFIED
         */
        public static final int ALIGN_JUSTIFIED = 3;

        /**
         * @see javax.swing.text.StyleConstants#ALIGN_LEFT StyleConstants.ALIGN_LEFT
         */
        public static final int ALIGN_LEFT = 0;

        /**
         * @see javax.swing.text.StyleConstants#ALIGN_RIGHT StyleConstants.ALIGN_RIGHT
         */
        public static final int ALIGN_RIGHT = 2;

        /**
         * @see javax.swing.text.StyleConstants#getAlignment() StyleConstants.getAlignment
         */
        public int getAlignment() {
            return alignment;
        }

        /**
         * @see javax.swing.text.StyleConstants#getAlignment() StyleConstants.getAlignment
         */
        public void setAlignment(int alignment) {
            if (alignment < 0 || alignment > 3) {
                throw new IllegalArgumentException("setAlignment");
            }
            this.alignment = alignment;
        }

        public int getBackground() {
            return getBackground(ALL);
        }

        public void setBackground(int c) {
            addBackground(c, ALL);
        }

        public int getForeground() {
            return getForeground(ALL);
        }

        public void setForeground(int c) {
            addForeground(c, ALL);
        }

        private boolean isBitSet(byte b, int pos) {
            return ((b >> pos) & 0x01) == 0x01;
        }

        private byte setBit(byte b, int pos, boolean val) {
            byte bit = (byte) (1 << pos);
            return (val) ? (byte) (b | bit) : (byte) ((b ^ 0xff) & bit);
        }

        public boolean isBold() {
            return isBitSet(textStyle, 0);
        }

        public void setBold(boolean b) {
            textStyle = setBit(textStyle, 0, b);
        }

        public boolean isItalic() {
            return isBitSet(textStyle, 1);
        }

        public void setItalic(boolean b) {
            textStyle = setBit(textStyle, 1, b);
        }

        public boolean isUnderline() {
            return isBitSet(textStyle, 2);
        }

        public void setUnderline(boolean b) {
            textStyle = setBit(textStyle, 2, b);
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void putAll(Style attributes) {
            super.putAll(attributes);
            if (attributes instanceof TextStyle) {
                TextStyle a = (TextStyle) attributes;
                textStyle |= a.textStyle;
                if (a.alignment != -1) {
                    alignment = a.alignment;
                }
                if (a.action != null) {
                    action = a.action;
                }
                if (a.icon != null) {
                    icon = a.icon;
                }
                if (a.name != null) {
                    name = a.name;
                }
            }
        }

        public void updateUI() {
            if (name != null) {
                Style newStyle = DesktopPane.getDesktopPane().getLookAndFeel().getStyle(name);
                if (newStyle != null) {
                    reset();
                    super.putAll(newStyle);
                }
            }
        }
    }

    /**
     * @see javax.swing.text.Element
     */
    private static class Element {

        boolean isParagraph;

        private TextStyle style;

        private int startOffset;

        private int endOffset;

        public Element(TextStyle style, int startOffset, int endOffset) {
            this.style = style;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }

    private static class LineFragment {

        int x, y, w, h;

        TextStyle style;

        int startOffset;

        int endOffset;

        public LineFragment(int x, int y, int w, int h, TextStyle style, int startOffset, int endOffset) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.style = style;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }
}
