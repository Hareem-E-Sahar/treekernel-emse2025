package de.enough.polish.ui;

import de.enough.polish.util.BitMapFont;
import de.enough.polish.util.BitMapFontViewer;
import de.enough.polish.util.TextUtil;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Meant for classes that want to be compatible with javax.microedition.lcdui.CustomItem for IDEs only while extending de.enough.polish.ui.StringItem in reality.
 * 
 * <p>Subclasses can change the hierarchy with preprocessing like this:
 * <pre>
 * public class MyCustomItem
 * //#if polish.LibraryBuild
 * 		 extends FakeStringCustomItem
 * //#else
 * 		//# extends StringItem
 * //#endif	 
 * </pre>
 * </p>
 * <p>This allows subclasses to access all fields and methods of the J2ME Polish item class.</p>
 * <p>Note that this class can never be used in reality. Ever.</p>
 * 
 * @since J2ME Polish 1.3
 * @author Robert Virkus, robert@enough.de
 */
public class FakeStringCustomItem extends FakeCustomItem {

    protected String text;

    protected String[] textLines;

    protected int textColor;

    protected Font font;

    protected BitMapFont bitMapFont;

    protected BitMapFontViewer bitMapFontViewer;

    protected boolean useSingleLine;

    protected boolean clipText;

    protected int xOffset;

    private int textWidth;

    private boolean isHorizontalAnimationDirectionRight;

    private boolean isSkipHorizontalAnimation;

    protected int textHorizontalAdjustment;

    protected int textVerticalAdjustment;

    protected TextEffect textEffect;

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
	 * @see StringItem#StringItem(String, String, int, Style)
	 */
    public FakeStringCustomItem(String label, String text) {
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
	 * @see StringItem#StringItem(String, String, int, Style)
	 */
    public FakeStringCustomItem(String label, String text, Style style) {
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
    public FakeStringCustomItem(String label, String text, int appearanceMode) {
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
    public FakeStringCustomItem(String label, String text, int appearanceMode, Style style) {
        super(label, LAYOUT_DEFAULT, appearanceMode, style);
        this.text = text;
    }

    public boolean animate() {
        boolean animated = super.animate();
        if (this.textEffect != null) {
            animated |= this.textEffect.animate();
        }
        if (this.useSingleLine && this.clipText) {
            if (this.isSkipHorizontalAnimation) {
                this.isSkipHorizontalAnimation = false;
            } else {
                if (this.isHorizontalAnimationDirectionRight) {
                    this.xOffset++;
                    if (this.xOffset >= 0) {
                        this.isHorizontalAnimationDirectionRight = false;
                    }
                } else {
                    this.xOffset--;
                    if (this.xOffset + this.textWidth < this.contentWidth) {
                        this.isHorizontalAnimationDirectionRight = true;
                    }
                }
                animated = true;
                this.isSkipHorizontalAnimation = true;
            }
        }
        return animated;
    }

    protected void defocus(Style originalStyle) {
        super.defocus(originalStyle);
        if (this.clipText) {
            this.xOffset = 0;
        }
    }

    protected void hideNotify() {
        if (this.textEffect != null) {
            this.textEffect.hideNotify();
        }
        super.hideNotify();
    }

    protected void showNotify() {
        if (this.textEffect != null) {
            this.textEffect.showNotify();
        }
        super.showNotify();
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
        System.out.println("StringItem: setText( \"" + text + "\" )");
        if (style != null) {
            setStyle(style);
        }
        this.text = text;
        if (text == null) {
            this.textLines = null;
        }
        requestInit();
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
        this.isInitialized = false;
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
        return this.font;
    }

    public void paintContent(int x, int y, int leftBorder, int rightBorder, Graphics g) {
        if (this.text != null) {
            y += this.textVerticalAdjustment;
            if (this.bitMapFontViewer != null) {
                if (this.isLayoutCenter) {
                    x = leftBorder + (rightBorder - leftBorder) / 2;
                    x += this.textHorizontalAdjustment;
                } else if (this.isLayoutRight) {
                    x = rightBorder;
                    x += this.textHorizontalAdjustment;
                }
                y += this.textVerticalAdjustment;
                this.bitMapFontViewer.paint(x, y, g);
                return;
            }
            g.setFont(this.font);
            g.setColor(this.textColor);
            int lineHeight = this.font.getHeight() + this.paddingVertical;
            int centerX = 0;
            if (this.isLayoutCenter) {
                centerX = leftBorder + (rightBorder - leftBorder) / 2;
                centerX += this.textHorizontalAdjustment;
            }
            x += this.textHorizontalAdjustment;
            leftBorder += this.textHorizontalAdjustment;
            rightBorder += this.textHorizontalAdjustment;
            int clipX = 0;
            int clipY = 0;
            int clipWidth = 0;
            int clipHeight = 0;
            if (this.useSingleLine && this.clipText) {
                clipX = g.getClipX();
                clipY = g.getClipY();
                clipWidth = g.getClipWidth();
                clipHeight = g.getClipHeight();
                g.clipRect(x, y, this.contentWidth, this.contentHeight);
            }
            if (this.textEffect != null) {
                this.textEffect.drawStrings(this.textLines, this.textColor, x, y, leftBorder, rightBorder, lineHeight, this.contentWidth, this.layout, g);
            } else {
                for (int i = 0; i < this.textLines.length; i++) {
                    String line = this.textLines[i];
                    int lineX = x;
                    int lineY = y;
                    int orientation;
                    if (this.isLayoutRight) {
                        lineX = rightBorder;
                        orientation = Graphics.TOP | Graphics.RIGHT;
                    } else if (this.isLayoutCenter) {
                        lineX = centerX;
                        orientation = Graphics.TOP | Graphics.HCENTER;
                    } else {
                        orientation = Graphics.TOP | Graphics.LEFT;
                    }
                    if (this.clipText) {
                        lineX += this.xOffset;
                    }
                    g.drawString(line, lineX, lineY, orientation);
                    x = leftBorder;
                    y += lineHeight;
                }
            }
            if (this.useSingleLine && this.clipText) {
                g.setClip(clipX, clipY, clipWidth, clipHeight);
            }
        }
    }

    protected void initContent(int firstLineWidth, int lineWidth) {
        if (this.text != null && this.font == null) {
            this.font = Font.getDefaultFont();
        }
        if (this.text == null) {
            this.contentHeight = 0;
            this.contentWidth = 0;
            return;
        }
        if (this.bitMapFont != null) {
            int orientation = Graphics.LEFT;
            if (this.isLayoutCenter) {
                orientation = Graphics.HCENTER;
            } else if (this.isLayoutRight) {
                orientation = Graphics.RIGHT;
            }
            this.bitMapFontViewer = this.bitMapFont.getViewer(this.text);
            this.bitMapFontViewer.layout(firstLineWidth, lineWidth, this.paddingVertical, orientation);
            this.contentHeight = this.bitMapFontViewer.getHeight();
            this.contentWidth = this.bitMapFontViewer.getWidth();
            return;
        }
        if (this.useSingleLine) {
            this.textLines = new String[] { this.text };
            int myTextWidth = this.font.stringWidth(this.text);
            if (myTextWidth > lineWidth) {
                this.clipText = true;
                this.textWidth = myTextWidth;
                this.isHorizontalAnimationDirectionRight = false;
                this.contentWidth = lineWidth;
            } else {
                this.clipText = false;
                this.contentWidth = myTextWidth;
            }
            this.contentHeight = this.font.getHeight();
        } else {
            String[] lines = TextUtil.wrap(this.text, this.font, firstLineWidth, lineWidth);
            int fontHeight = this.font.getHeight();
            this.contentHeight = (lines.length * (fontHeight + this.paddingVertical)) - this.paddingVertical;
            int maxWidth = 0;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int width = this.font.stringWidth(line);
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
            this.contentWidth = maxWidth;
            this.textLines = lines;
        }
    }

    public void setStyle(Style style) {
        super.setStyle(style);
        this.textColor = style.getFontColor();
        this.font = style.font;
        String bitMapUrl = style.getProperty("font-bitmap");
        if (bitMapUrl != null) {
            this.bitMapFont = BitMapFont.getInstance(bitMapUrl);
        } else {
            this.bitMapFont = null;
            this.bitMapFontViewer = null;
        }
        Integer textHorizontalAdjustmentInt = style.getIntProperty("text-horizontal-adjustment");
        if (textHorizontalAdjustmentInt != null) {
            this.textHorizontalAdjustment = textHorizontalAdjustmentInt.intValue();
        }
        Integer textVerticalAdjustmentInt = style.getIntProperty("text-vertical-adjustment");
        if (textVerticalAdjustmentInt != null) {
            this.textVerticalAdjustment = textVerticalAdjustmentInt.intValue();
        }
        TextEffect effect = (TextEffect) style.getObjectProperty("text-effect");
        if (effect != null) {
            this.textEffect = effect;
            effect.setStyle(style);
        } else {
            this.textEffect = null;
        }
        Boolean textWrapBool = style.getBooleanProperty("text-wrap");
        if (textWrapBool != null) {
            this.useSingleLine = !textWrapBool.booleanValue();
        }
    }

    protected String createCssSelector() {
        if (this.appearanceMode == BUTTON) {
            return "button";
        } else if (this.appearanceMode == HYPERLINK) {
            return "a";
        } else {
            return "p";
        }
    }

    public String toString() {
        return "StringItem " + super.toString() + ": \"" + this.getText() + "\"";
    }

    public void releaseResources() {
        super.releaseResources();
        if (this.textEffect != null) {
            this.textEffect.releaseResources();
        }
    }
}
