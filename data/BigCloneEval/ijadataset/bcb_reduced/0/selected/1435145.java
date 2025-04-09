package de.enough.polish.ui;

import de.enough.polish.android.lcdui.Font;
import de.enough.polish.android.lcdui.Graphics;
import de.enough.polish.util.TextUtil;

/**
 * An item that can contain a string.
 * 
 * A <code>StringItem</code> is display-only; the user
 * cannot edit the contents. Both the label and the textual content of a
 * <code>StringItem</code> may be modified by the application. The
 * visual representation
 * of the label may differ from that of the textual contents.
 * 
 * @author Robert Virkus, robert@enough.de
 */
public class StringItem extends Item {

    private static final int DIRECTION_BACK_AND_FORTH = 0;

    private static final int DIRECTION_LEFT = 1;

    private static final int DIRECTION_RIGHT = 2;

    protected String text;

    protected String[] textLines;

    protected int textColor;

    protected Font font;

    /**
	 * Creates a new <code>StringItem</code> object.  Calling this
	 * constructor is equivalent to calling
	 * 
	 * <pre><code>
	 * StringItem(label, text, Item.PLAIN, null);     
	 * </code></pre>
	 * 
	 * @param label the Item label
	 * @param text the text contents
	 * @see #StringItem(String, String, int, Style)
	 */
    public StringItem(String label, String text) {
        this(label, text, PLAIN);
    }

    /**
	 * Creates a new <code>StringItem</code> object.  Calling this
	 * constructor is equivalent to calling
	 * 
	 * <pre><code>
	 * StringItem(label, text, Item.PLAIN, style);     
	 * </code></pre>
	 * 
	 * @param label the Item label
	 * @param text the text contents
	 * @param style the style
	 * @see #StringItem(String, String, int, Style)
	 */
    public StringItem(String label, String text, Style style) {
        this(label, text, PLAIN, style);
    }

    /**
	 * Creates a new <code>StringItem</code> object with the given label,
	 * textual content, and appearance mode.
	 * Either label or text may be present or <code>null</code>.
	 * 
	 * <p>The <code>appearanceMode</code> parameter
	 * (see <a href="Item.html#appearance">Appearance Modes</a>)
	 * is a hint to the platform of the application's intended use
	 * for this <code>StringItem</code>.  To provide hyperlink- or
	 * button-like behavior,
	 * the application should associate a default <code>Command</code> with this
	 * <code>StringItem</code> and add an
	 * <code>ItemCommandListener</code> to this
	 * <code>StringItem</code>.
	 * 
	 * <p>Here is an example showing the use of a
	 * <code>StringItem</code> as a button: </p>
	 * <pre><code>
	 * StringItem strItem = new StringItem("Default: ", "Set", Item.BUTTON);
	 * strItem.setDefaultCommand(
	 * new Command("Set", Command.ITEM, 1);
	 * // icl is ItemCommandListener
	 * strItem.setItemCommandListener(icl);     
	 * </code></pre>
	 * 
	 * @param label the StringItem's label, or null if no label
	 * @param text the StringItem's text contents, or null if the contents are initially empty
	 * @param appearanceMode the appearance mode of the StringItem, one of Item.PLAIN, Item.HYPERLINK, or Item.BUTTON
	 * @throws IllegalArgumentException if appearanceMode invalid
	 * @since  MIDP 2.0
	 */
    public StringItem(String label, String text, int appearanceMode) {
        this(label, text, appearanceMode, null);
    }

    /**
	 * Creates a new <code>StringItem</code> object with the given label,
	 * textual content, and appearance mode.
	 * Either label or text may be present or <code>null</code>.
	 * 
	 * <p>The <code>appearanceMode</code> parameter
	 * (see <a href="Item.html#appearance">Appearance Modes</a>)
	 * is a hint to the platform of the application's intended use
	 * for this <code>StringItem</code>.  To provide hyperlink- or
	 * button-like behavior,
	 * the application should associate a default <code>Command</code> with this
	 * <code>StringItem</code> and add an
	 * <code>ItemCommandListener</code> to this
	 * <code>StringItem</code>.
	 * 
	 * <p>Here is an example showing the use of a
	 * <code>StringItem</code> as a button: </p>
	 * <pre><code>
	 * StringItem strItem = new StringItem("Default: ", "Set", Item.BUTTON);
	 * strItem.setDefaultCommand(
	 * new Command("Set", Command.ITEM, 1);
	 * // icl is ItemCommandListener
	 * strItem.setItemCommandListener(icl);     
	 * </code></pre>
	 * 
	 * @param label the StringItem's label, or null if no label
	 * @param text the StringItem's text contents, or null if the contents are initially empty
	 * @param appearanceMode the appearance mode of the StringItem, one of Item.PLAIN, Item.HYPERLINK, or Item.BUTTON
	 * @param style the style for this item
	 * @throws IllegalArgumentException if appearanceMode invalid
	 * @since  MIDP 2.0
	 */
    public StringItem(String label, String text, int appearanceMode, Style style) {
        super(label, LAYOUT_DEFAULT, appearanceMode, style);
        this.text = text;
    }

    /**
	 * Gets the text contents of the <code>StringItem</code>, or
	 * <code>null</code> if the <code>StringItem</code> is
	 * empty.
	 * 
	 * @return a string with the content of the item
	 * @see #setText(java.lang.String)
	 */
    public String getText() {
        return this.text;
    }

    /**
	 * Sets the text contents of the <code>StringItem</code>. 
	 * If text
	 * is <code>null</code>,
	 * the <code>StringItem</code>
	 * is set to be empty.
	 * 
	 * @param text the new content
	 * @see #getText()
	 */
    public void setText(String text) {
        setText(text, null);
    }

    /**
	 * Sets the text contents of the <code>StringItem</code> along with a style. 
	 * If text is <code>null</code>,
	 * the <code>StringItem</code>
	 * is set to be empty.
	 * 
	 * @param text the new content
	 * @param style the new style, is ignored when null
	 * @see #getText()
	 */
    public void setText(String text, Style style) {
        if (style != null) {
            setStyle(style);
        }
        if (text != this.text) {
            this.text = text;
            if (text == null) {
                this.textLines = null;
            }
            requestInit();
        }
    }

    /**
	 * Sets the text color for contents of the <code>StringItem</code>.
	 *
	 * @param color the new color for the content
	 */
    public void setTextColor(int color) {
        this.textColor = color;
    }

    /**
	 * Sets the application's preferred font for
	 * rendering this <code>StringItem</code>.
	 * The font is a hint, and the implementation may disregard
	 * the application's preferred font.
	 * 
	 * <p> The <code>font</code> parameter must be a valid <code>Font</code>
	 * object or <code>null</code>. If the <code>font</code> parameter is
	 * <code>null</code>, the implementation must use its default font
	 * to render the <code>StringItem</code>.</p>
	 * 
	 * @param font - the preferred font to use to render this StringItem
	 * @see #getFont()
	 * @since  MIDP 2.0
	 */
    public void setFont(Font font) {
        this.font = font;
        setInitialized(false);
    }

    /**
	 * Gets the application's preferred font for
	 * rendering this <code>StringItem</code>. The
	 * value returned is the font that had been set by the application,
	 * even if that value had been disregarded by the implementation.
	 * If no font had been set by the application, or if the application
	 * explicitly set the font to <code>null</code>, the value is the default
	 * font chosen by the implementation.
	 * 
	 * @return the preferred font to use to render this StringItem
	 * @see #setFont(javax.microedition.lcdui.Font)
	 * @since  MIDP 2.0
	 */
    public Font getFont() {
        if (this.font == null) {
            if (this.style != null) {
                this.font = this.style.getFont();
            }
            if (this.font == null) {
                this.font = Font.getDefaultFont();
            }
        }
        return this.font;
    }

    public void paintContent(int x, int y, int leftBorder, int rightBorder, Graphics g) {
        String[] lines = this.textLines;
        if (lines != null) {
            g.setFont(this.font);
            g.setColor(this.textColor);
            int lineHeight = getFontHeight() + this.paddingVertical;
            int centerX = 0;
            boolean isCenter;
            boolean isRight;
            isCenter = this.isLayoutCenter;
            isRight = this.isLayoutRight;
            if (isCenter) {
                centerX = leftBorder + (rightBorder - leftBorder) / 2;
            }
            int lineX = x;
            int lineY = y;
            int orientation;
            if (isRight) {
                lineX = rightBorder;
                orientation = Graphics.RIGHT;
            } else if (isCenter) {
                lineX = centerX;
                orientation = Graphics.HCENTER;
            } else {
                orientation = Graphics.LEFT;
            }
            orientation = Graphics.TOP | orientation;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                g.drawString(line, lineX, lineY, orientation);
                lineY += lineHeight;
            }
        }
    }

    /**
	 * Calculates the width of the given text.
	 * When a bitmap font is used, the calculation is forwarded to it.
	 * When a texteffect is used, the calculation is forwared to it.
	 * In other cases font.stringWidth(text) is returned.
	 * 
	 * @param str the text of which the width should be determined
	 * @return the width of the text
	 */
    public int stringWidth(String str) {
        return getFont().stringWidth(str);
    }

    /**
	 * Retrieves the width of the given char
	 * @param c the char
	 * @return the width of that char
	 */
    public int charWidth(char c) {
        return this.font.charWidth(c);
    }

    /**
	 * Retrieves the height necessary for displaying a row of text without the padding-vertical.
	 * 
	 * @return the font height (either from the bitmap, the text-effect or the font used)
	 */
    public int getFontHeight() {
        return getFont().getHeight();
    }

    protected void initContent(int firstLineWidth, int availWidth, int availHeight) {
        String body = this.text;
        if (body != null && this.font == null) {
            this.font = Font.getDefaultFont();
        }
        if ((body == null)) {
            this.contentHeight = 0;
            this.contentWidth = 0;
            this.textLines = null;
            return;
        }
        String[] lines = wrap(body, firstLineWidth, availWidth);
        int fontHeight = getFontHeight();
        this.contentHeight = (lines.length * (fontHeight + this.paddingVertical)) - this.paddingVertical;
        int maxWidth = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int width = stringWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        this.contentWidth = maxWidth;
        this.textLines = lines;
    }

    String[] wrap(String body, int firstLineWidth, int availWidth) {
        String[] result;
        result = TextUtil.wrap(body, this.font, firstLineWidth, availWidth);
        return result;
    }

    public void setStyle(Style style) {
        super.setStyle(style);
    }

    public void setStyle(Style style, boolean resetStyle) {
        super.setStyle(style, resetStyle);
        if (resetStyle) {
            this.textColor = style.getFontColor();
            this.font = style.getFont();
        }
        Color textColorObj = style.getColorProperty(-17);
        if (textColorObj != null) {
            this.textColor = textColorObj.getColor();
        }
    }

    public String toString() {
        return "StringItem " + super.toString() + ": \"" + this.getText() + "\"";
    }

    public void releaseResources() {
        super.releaseResources();
    }
}
