package de.enough.polish.ui.texteffects;

import de.enough.polish.android.lcdui.Font;
import de.enough.polish.android.lcdui.Graphics;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextEffect;
import de.enough.polish.util.BitMapFont;
import de.enough.polish.util.BitMapFontViewer;

/**
 * <p>Renders texts with a given bitmap font.</p>
 * <p>Activate the bitmap font text effect by specifying <code>text-effect: bitmap;</code> in your polish.css file.
 * <!--
 *    You can finetune the effect with following attributes:
 *    -->
 * </p>
 * <!--
 * <ul>
 * 	 <li><b>font-bitmap</b>: the URL of the font.</li>
 * </ul>
 *    -->
 *
 * <p>Copyright (c) 2009 Enough Software</p>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class BitmapFontTextEffect extends TextEffect {

    protected transient BitMapFont font;

    protected transient BitMapFontViewer viewer;

    private String[] lastText;

    /**
	 * Creates a text with smileys
	 */
    public BitmapFontTextEffect() {
        this.isTextSensitive = true;
    }

    public int getFontHeight() {
        if (this.font == null) {
            return super.getFontHeight();
        }
        return this.font.getFontHeight();
    }

    public int stringWidth(String str) {
        if (this.font == null) {
            return super.stringWidth(str);
        }
        return this.font.stringWidth(str);
    }

    public int charWidth(char c) {
        if (this.font == null) {
            return super.charWidth(c);
        }
        return this.font.charWidth(c);
    }

    public String[] wrap(String text, int textColor, Font fnt, int firstLineWidth, int lineWidth) {
        if (this.font == null) {
            return super.wrap(text, textColor, fnt, firstLineWidth, lineWidth);
        }
        this.viewer = this.font.getViewer(text, textColor);
        if (this.viewer == null) {
            return super.wrap(text, textColor, fnt, firstLineWidth, lineWidth);
        }
        int pv = 1;
        int anchor = Graphics.LEFT;
        if (this.style != null) {
            pv = this.style.getPaddingVertical(lineWidth);
            anchor = this.style.getAnchorHorizontal();
        }
        this.viewer.layout(firstLineWidth, lineWidth, pv, anchor);
        String[] wrappedText = this.viewer.wrap(text);
        this.lastText = wrappedText;
        return wrappedText;
    }

    public void setStyle(Style style) {
        super.setStyle(style);
    }

    /**
	 * Set the bitmap font used in this text effect
	 * @param font the font
	 */
    public void setFont(BitMapFont font) {
        this.font = font;
    }

    public void drawStrings(String[] textLines, int textColor, int x, int y, int leftBorder, int rightBorder, int lineHeight, int maxWidth, int layout, Graphics g) {
        if (textLines != this.lastText && this.font != null) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < textLines.length; i++) {
                buffer.append(textLines[i]);
                if (i != textLines.length - 1) {
                    buffer.append('\n');
                }
            }
            this.viewer = this.font.getViewer(buffer.toString(), textColor);
            this.lastText = textLines;
        }
        if (this.viewer == null) {
            super.drawStrings(textLines, textColor, x, y, leftBorder, rightBorder, lineHeight, maxWidth, layout, g);
        } else {
            if ((layout & Item.LAYOUT_CENTER) == Item.LAYOUT_CENTER) {
                x = leftBorder + (rightBorder - leftBorder) / 2;
            } else if ((layout & Item.LAYOUT_RIGHT) == Item.LAYOUT_RIGHT) {
                x = rightBorder;
            }
            if ((layout & Item.LAYOUT_BOTTOM) == Item.LAYOUT_BOTTOM) {
                int fontHeight = this.font.getFontHeight();
                if ((layout & Item.LAYOUT_VCENTER) == Item.LAYOUT_VCENTER) {
                    y -= fontHeight / 2;
                } else {
                    y -= fontHeight;
                }
            }
            this.viewer.paint(x, y, g);
        }
    }

    public void drawString(String text, int textColor, int x, int y, int orientation, Graphics g) {
        g.drawString(text, x, y, orientation);
    }

    public void drawChar(char c, int x, int y, int anchor, Graphics g) {
        if (this.font == null) {
            super.drawChar(c, x, y, anchor, g);
        }
        this.font.drawChar(c, x, y, anchor, g);
    }
}
