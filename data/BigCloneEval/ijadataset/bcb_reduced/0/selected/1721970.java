package DrawControls;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.*;
import DrawControls.VirtualList;
import DrawControls.ListItem;

class TextItem {

    public Image image;

    public String text;

    private int fontAndColor = 0;

    private int itemHeigthAndWidth = 0;

    public int getHeight(int fontSize) {
        if (image != null) return image.getHeight();
        if (text == null) return 0;
        if ((itemHeigthAndWidth & 0xFFFF) == 0) {
            Font font = Font.getFont(Font.FACE_SYSTEM, (fontAndColor >> 24) & 0xFF, fontSize);
            itemHeigthAndWidth = (itemHeigthAndWidth & 0xFFFF0000) | font.getHeight();
        }
        return itemHeigthAndWidth & 0xFFFF;
    }

    public int getWidth(int fontSize) {
        if (image != null) {
            return image.getWidth();
        }
        if (text == null) return 0;
        if ((itemHeigthAndWidth & 0xFFFF0000) == 0) {
            Font font = Font.getFont(Font.FACE_SYSTEM, (fontAndColor >> 24) & 0xFF, fontSize);
            itemHeigthAndWidth = (itemHeigthAndWidth & 0x0000FFFF) | (font.stringWidth(text) << 16);
        }
        return (itemHeigthAndWidth & 0xFFFF0000) >> 16;
    }

    public int getColor() {
        return fontAndColor & 0xFFFFFF;
    }

    public void setColor(int value) {
        fontAndColor = (fontAndColor & 0xFF000000) | (value & 0x00FFFFFF);
    }

    public int getFontStyle() {
        return (fontAndColor & 0xFF000000) >> 24;
    }

    public void setFontStyle(int value) {
        fontAndColor = (fontAndColor & 0x00FFFFFF) | ((value & 0xFF) << 24);
    }
}

class TextLine {

    private Vector items = new Vector();

    int height = -1;

    int bigTextIndex = -1;

    char last_charaster;

    TextItem elementAt(int index) {
        return (TextItem) items.elementAt(index);
    }

    void add(TextItem item) {
        items.addElement(item);
    }

    int getHeight(int fontSize) {
        if (height == -1) {
            height = fontSize;
            for (int i = items.size() - 1; i >= 0; i--) {
                int currHeight = elementAt(i).getHeight(fontSize);
                if (currHeight > height) height = currHeight;
            }
        }
        return height;
    }

    int getWidth(int fontSize) {
        int width = 0;
        for (int i = items.size() - 1; i >= 0; i--) width += elementAt(i).getWidth(fontSize);
        return width;
    }

    void setItemColor(int value) {
        for (int i = items.size() - 1; i >= 0; i--) {
            TextItem listItem = elementAt(i);
            listItem.setColor(value);
        }
    }

    void paint(int xpos, int ypos, Graphics g, int fontSize, VirtualList vl) {
        int count = items.size();
        int intemHeight = getHeight(fontSize);
        for (int i = 0; i < count; i++) {
            TextItem item = elementAt(i);
            int drawYPos = ypos + (intemHeight - item.getHeight(fontSize)) / 2;
            if (item.image != null) {
                g.drawImage(item.image, xpos, drawYPos, Graphics.TOP | Graphics.LEFT);
            } else if (item.text != null) {
                g.setColor(item.getColor());
                g.setFont(vl.getQuickFont(item.getFontStyle()));
                g.drawString(item.text, xpos, drawYPos, Graphics.TOP | Graphics.LEFT);
            }
            xpos += item.getWidth(fontSize);
        }
    }

    int size() {
        return items.size();
    }

    void readText(StringBuffer buffer) {
        for (int i = 0; i < items.size(); i++) buffer.append(elementAt(i).text);
    }
}

public class TextList extends VirtualList {

    public TextList(String capt, int capTextColor, int backColor, int fontSize, int cursorMode) {
        super(capt, capTextColor, backColor, fontSize, cursorMode);
    }

    public TextList(String capt) {
        super(capt);
    }

    private Vector lines = new Vector();

    public int getSize() {
        if (lines.isEmpty()) return 0;
        int size = lines.size();
        return (((TextLine) lines.lastElement()).size() == 0) ? size - 1 : size;
    }

    private TextLine getLine(int index) {
        return (TextLine) lines.elementAt(index);
    }

    protected boolean isItemSelected(int index) {
        int selIndex = getCurrIndex();
        int textIndex = (selIndex >= lines.size()) ? -1 : getLine(selIndex).bigTextIndex;
        if (textIndex == -1) return false;
        return (getLine(index).bigTextIndex == textIndex);
    }

    protected void get(int index, ListItem item) {
        TextLine listItem = getLine(index);
        item.clear();
        if (listItem.size() == 0) return;
        TextItem titem = listItem.elementAt(0);
        item.text = titem.text;
        item.color = titem.getColor();
        item.fontStyle = titem.getFontStyle();
    }

    public void clear() {
        lines.removeAllElements();
        setCurrentItem(0);
        invalidate();
    }

    public void add(String text, int color, int imageIndex) {
        internAdd(text, color, imageIndex, Font.STYLE_PLAIN, -1, true, '\0');
        invalidate();
    }

    public void add(String text, int color, int imageIndex, int fontStyle) {
        internAdd(text, color, imageIndex, fontStyle, -1, true, '\0');
        invalidate();
    }

    private void internAdd(String text, int color, int imageIndex, int fontStyle, int textIndex, boolean doCRLF, char last_charaster) {
        TextItem newItem = new TextItem();
        newItem.text = text;
        newItem.setColor(color);
        newItem.setFontStyle(fontStyle);
        if (lines.isEmpty()) lines.addElement(new TextLine());
        TextLine textLine = (TextLine) lines.lastElement();
        textLine.add(newItem);
        textLine.bigTextIndex = textIndex;
        if (doCRLF) {
            textLine.last_charaster = last_charaster;
            TextLine newLine = new TextLine();
            newLine.bigTextIndex = textIndex;
            lines.addElement(newLine);
        }
    }

    public void add(String text) {
        add(text, this.getTextColor(), -1);
    }

    public int getItemHeight(int itemIndex) {
        if (getCursorMode() != CURSOR_MODE_DISABLED) return super.getItemHeight(itemIndex);
        if (itemIndex >= lines.size()) return 1;
        return getLine(itemIndex).getHeight(getFontSize());
    }

    protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight) {
        if (getCursorMode() != CURSOR_MODE_DISABLED) {
            super.drawItemData(g, index, x1, y1, x2, y2, fontHeight);
            return;
        }
        TextLine line = getLine(index);
        line.paint(borderWidth, y1, g, getFontSize(), this);
    }

    protected void moveCursor(int step, boolean moveTop) {
        int size, changeCounter = 0, currTextIndex, i, halfSize = getVisCount() / 2;
        switch(step) {
            case -1:
            case 1:
                currTextIndex = getCurrTextIndex();
                size = getSize();
                storelastItemIndexes();
                for (i = 0; i < halfSize; ) {
                    currItem += step;
                    if ((currItem < 0) || (currItem >= size)) {
                        if (changeCounter != 0) currItem -= step;
                        break;
                    }
                    TextLine item = getLine(currItem);
                    if (currTextIndex != item.bigTextIndex) {
                        currTextIndex = item.bigTextIndex;
                        changeCounter++;
                        if ((changeCounter == 2) || (!visibleItem(currItem) && (i > 0))) {
                            currItem -= step;
                            break;
                        }
                    }
                    if (!visibleItem(currItem) || (changeCounter != 0)) i++;
                }
                checkCurrItem();
                checkTopItem();
                checkTopItem();
                repaintIfLastIndexesChanged();
                break;
            default:
                super.moveCursor(step, moveTop);
                return;
        }
    }

    public String getTextByIndex(int offset, boolean wholeText, int textIndex) {
        StringBuffer result = new StringBuffer();
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            TextLine line = getLine(i);
            if (wholeText || (textIndex == -1) || (line.bigTextIndex == textIndex)) {
                line.readText(result);
                if (line.last_charaster != '\0') {
                    if (line.last_charaster == '\n') result.append("\n"); else result.append(line.last_charaster);
                }
            }
        }
        if (result.length() == 0) return null;
        String resultText = result.toString();
        int len = resultText.length();
        if (offset > len) return null;
        return resultText.substring(offset, len);
    }

    public void selectTextByIndex(int textIndex) {
        if (textIndex == -1) return;
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            if (getLine(i).bigTextIndex == textIndex) {
                setCurrentItem(i);
                break;
            }
        }
    }

    public String getCurrText(int offset, boolean wholeText) {
        return getTextByIndex(offset, wholeText, getCurrTextIndex());
    }

    public int getCurrTextIndex() {
        int currItemIndex = getCurrIndex();
        if ((currItemIndex < 0) || (currItemIndex >= lines.size())) return -1;
        return getLine(currItemIndex).bigTextIndex;
    }

    public void setColors(int capTxt, int capbk, int bkgrnd, int cursor, int text, int crsFrame) {
        Enumeration allLines = lines.elements();
        while (allLines.hasMoreElements()) ((TextLine) allLines.nextElement()).setItemColor(text);
        super.setColors(capTxt, capbk, bkgrnd, cursor, text, crsFrame);
    }

    public TextList doCRLF(int blockTextIndex) {
        if (lines.size() != 0) ((TextLine) lines.lastElement()).last_charaster = '\n';
        TextLine newLine = new TextLine();
        newLine.bigTextIndex = blockTextIndex;
        lines.addElement(newLine);
        return this;
    }

    public TextList addImage(Image image, String altarnateText, int blockTextIndex) {
        if (lines.isEmpty()) lines.addElement(new TextLine());
        TextLine textLine = (TextLine) lines.lastElement();
        textLine.bigTextIndex = blockTextIndex;
        if ((textLine.getWidth(getFontSize()) + image.getWidth()) > getTextAreaWidth()) {
            doCRLF(blockTextIndex);
            textLine = (TextLine) lines.lastElement();
        }
        TextItem newItem = new TextItem();
        newItem.image = image;
        newItem.text = altarnateText;
        textLine.add(newItem);
        return this;
    }

    private int getTextAreaWidth() {
        return getWidthInternal() - scrollerWidth - borderWidth * 2;
    }

    private static String replace(String text, String from, String to) {
        int fromSize = from.length();
        for (; ; ) {
            int pos = text.indexOf(from);
            if (pos == -1) break;
            text = text.substring(0, pos) + to + text.substring(pos + fromSize, text.length());
        }
        return text;
    }

    private void addBigTextInternal(String text, int color, int fontStyle, int textIndex, int trueWidth) {
        Font font;
        int textLen, curPos, lastWordEnd, startPos, width, testStringWidth = 0;
        char curChar;
        boolean lineBreak, wordEnd, textEnd, divideLineToWords;
        String testString = null;
        text = replace(text, "\r\n", "\n");
        text = replace(text, "\r\n", "\n");
        font = getQuickFont(fontStyle);
        width = lines.isEmpty() ? trueWidth : trueWidth - ((TextLine) lines.lastElement()).getWidth(getFontSize());
        startPos = 0;
        lastWordEnd = -1;
        textLen = text.length();
        for (curPos = 0; curPos < textLen; ) {
            curChar = text.charAt(curPos);
            wordEnd = (curChar == ' ');
            lineBreak = (curChar == '\n');
            textEnd = (curPos == (textLen - 1));
            divideLineToWords = false;
            if (textEnd && (!lineBreak)) curPos++;
            if (lineBreak || textEnd || wordEnd) {
                testString = text.substring(startPos, curPos);
                testStringWidth = font.stringWidth(testString);
            }
            if ((lineBreak || textEnd) && (testStringWidth <= width)) {
                internAdd(testString, color, -1, fontStyle, textIndex, lineBreak, lineBreak ? '\n' : ' ');
                width = trueWidth;
                curPos++;
                startPos = curPos;
                lastWordEnd = -1;
                continue;
            }
            if ((lineBreak || textEnd || wordEnd) && (testStringWidth > width)) {
                if ((testStringWidth < trueWidth) && (lastWordEnd != -1)) {
                    divideLineToWords = true;
                } else if ((trueWidth != width) && (lastWordEnd == -1)) {
                    doCRLF(textIndex);
                    curPos = startPos;
                    width = trueWidth;
                    lastWordEnd = -1;
                    continue;
                }
            }
            if ((lineBreak || textEnd || wordEnd) && (testStringWidth > trueWidth) && (!divideLineToWords)) {
                if (lastWordEnd == -1) {
                    for (; curPos >= 1; curPos--) {
                        testString = text.substring(startPos, curPos);
                        if (font.stringWidth(testString) <= width) break;
                    }
                    internAdd(testString, color, -1, fontStyle, textIndex, true, '\0');
                    width = trueWidth;
                    startPos = curPos;
                    lastWordEnd = -1;
                    continue;
                } else {
                    divideLineToWords = true;
                }
            }
            if (divideLineToWords) {
                String insString = text.substring(startPos, lastWordEnd);
                internAdd(insString, color, -1, fontStyle, textIndex, true, ' ');
                curPos = lastWordEnd + 1;
                startPos = curPos;
                width = trueWidth;
                lastWordEnd = -1;
                continue;
            }
            if (wordEnd) lastWordEnd = curPos;
            curPos++;
        }
    }

    public TextList addBigText(String text, int color, int fontStyle, int textIndex) {
        addBigTextInternal(text, color, fontStyle, textIndex, getTextAreaWidth());
        invalidate();
        return this;
    }

    public static int getLineNumbers(String s, int width, int fontSize, int fontStyle, int textColor) {
        TextList paintList = new TextList(null);
        paintList.setFontSize(fontSize);
        paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);
        return (paintList.getSize());
    }

    public static void showText(Graphics g, String s, int x, int y, int width, int height, int fontSize, int fontStyle, int textColor) {
        TextList paintList = new TextList(null);
        paintList.setFontSize(fontSize);
        paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);
        int line, textHeight = 0;
        int linesCount = paintList.getSize();
        for (line = 0; line < linesCount; line++) textHeight += paintList.getLine(line).getHeight(fontSize);
        int top = y + (height - textHeight) / 2;
        for (line = 0; line < linesCount; line++) {
            paintList.getLine(line).paint(x, top, g, fontSize, paintList);
            top += paintList.getLine(line).getHeight(fontSize);
        }
    }
}
