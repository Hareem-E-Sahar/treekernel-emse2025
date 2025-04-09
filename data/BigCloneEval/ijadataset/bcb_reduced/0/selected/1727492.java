package com.elibera.m.xml;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.elibera.m.display.HelperDisplay;
import com.elibera.m.rms.HelperRMSStoreMLibera;
import com.elibera.m.utils.HelperApp;

/**
 * Implements the functionallity to draw a string with a custom bitmap font
 * if you want to use custom fonts use this class for all string operations 
 * (width/height calculation and drawing)
 * @author matthias
 * Parts of the code are from http://j2me-mwt.sourceforge.net/ here is the original note: Copyright (C) 2007 Lucas Domanico - lucazd@gmail.com
 */
public class FontCustom {

    private static final char[] charsetImgs = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '^', '?', '´', '°', '!', '"', '§', '$', '%', '&', '/', '(', ')', '=', '?', '`', 'ü', '+', 'ö', 'ä', '#', ',', '.', '-', '<', '>', ';', ':', '_', 'Ö', 'Ä', '\'', 'Ü', '*', '|', '{', '[', ']', '}', '\\', '€' };

    private static final int charSpacingImgs = 0;

    private static final int[] widthsImgRegular = { 13, 13, 14, 14, 13, 12, 16, 13, 6, 10, 13, 11, 17, 13, 16, 13, 16, 14, 13, 12, 13, 13, 19, 13, 14, 12, 11, 11, 10, 11, 11, 6, 11, 10, 4, 4, 10, 4, 16, 10, 11, 11, 11, 7, 10, 6, 10, 9, 15, 9, 10, 9, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 8, 11, 7, 8, 6, 7, 11, 11, 18, 13, 6, 7, 7, 12, 11, 7, 10, 12, 11, 11, 11, 6, 6, 7, 12, 12, 6, 6, 11, 16, 13, 4, 13, 8, 6, 7, 6, 6, 7, 6, 11 };

    private static final int[] widthsImgBold = { 13, 13, 14, 14, 13, 12, 16, 13, 6, 10, 13, 11, 17, 13, 16, 13, 16, 14, 13, 12, 13, 13, 19, 13, 14, 12, 11, 11, 10, 11, 11, 6, 11, 10, 4, 4, 10, 4, 16, 10, 11, 11, 11, 7, 10, 6, 10, 9, 15, 9, 10, 9, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 8, 11, 7, 8, 6, 7, 11, 11, 18, 13, 6, 7, 7, 12, 11, 7, 10, 12, 11, 11, 11, 6, 6, 7, 12, 12, 6, 6, 11, 16, 13, 4, 13, 8, 6, 7, 6, 6, 7, 6, 11 };

    private static final int[] widthsImgItalic = { 13, 13, 14, 14, 13, 12, 16, 13, 6, 10, 13, 11, 17, 13, 16, 13, 16, 14, 13, 12, 13, 13, 19, 13, 14, 12, 11, 11, 10, 11, 11, 6, 11, 10, 4, 4, 10, 4, 16, 10, 11, 11, 11, 7, 10, 6, 10, 9, 15, 9, 10, 9, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 8, 11, 7, 8, 6, 7, 11, 11, 18, 13, 6, 7, 7, 12, 11, 7, 10, 12, 11, 11, 11, 6, 6, 7, 12, 12, 6, 6, 11, 16, 13, 4, 13, 8, 6, 7, 6, 6, 7, 6, 11 };

    private static final int[] widthsImgBoldSmall = null, widthsImgBoldLarge = null;

    private static final int[] widthsImgItalicSmall = null, widthsImgItalicLarge = null;

    private static final int[] widthsImgRegularSmall = null, widthsImgRegularLarge = null;

    private static int maxColorCache = 3;

    private static int maxSizeCache = 3;

    public static boolean useCustomFonts = false;

    private static String pathImgItalic = "/_font_italic";

    private static String pathImgBold = "/_font_bold";

    private static String pathImgRegular = "/_font_regular";

    private static String pathImgItalicSmall = "/_small_font_italic";

    private static String pathImgBoldSmall = "/_small_font_bold";

    private static String pathImgRegularSmall = "/_small_font_regular";

    private static String pathImgItalicLarge = "/_large_font_italic";

    private static String pathImgBoldLarge = "/_large_font_bold";

    private static String pathImgRegularLarge = "/_large_font_regular";

    private static FontCustom fontStd, fontBar, fontH1;

    public static void drawString(Graphics g, String text, int x, int y, int anchor) {
        drawString(g, text, x, y, anchor, null);
    }

    public static void drawString(Graphics g, String text, int x, int y, int anchor, Font font) {
        char[] s = text.toCharArray();
        drawChars(g, s, 0, s.length, x, y, anchor, font);
    }

    public static void drawChar(Graphics g, char text, int x, int y, int anchor, Font font) {
        drawChars(g, new char[] { text }, 0, 1, x, y, anchor, font);
    }

    public static void drawChars(Graphics g, char[] text, int offset, int length, int x, int y, int anchor, Font font) {
        if (font == null) font = g.getFont();
        if (!useCustomFonts) g.drawChars(text, offset, length, x, y, anchor); else {
            FontCustom f = getCustomFont(font.getStyle(), font.getSize(), true);
            f.write(g, text, offset, length, x, y, anchor);
        }
    }

    public static int charWidth(Font font, char ch) {
        if (!useCustomFonts) return font.charWidth(ch);
        return getCustomFont(font.getStyle(), font.getSize(), false)._getWidth(ch);
    }

    public static int charsWidth(Font font, char[] ch, int offset, int length) {
        if (!useCustomFonts) return font.charsWidth(ch, offset, length);
        return getCustomFont(font.getStyle(), font.getSize(), false)._getWidth(ch, offset, length);
    }

    public static int stringWidth(Font font, String text) {
        char[] s = text.toCharArray();
        return charsWidth(font, s, 0, s.length);
    }

    public static int getHeight(Font font) {
        if (!useCustomFonts) return font.getHeight();
        return getCustomFont(font.getStyle(), font.getSize(), false).getFontHeight();
    }

    private static FontCustom getCustomFont(int style, int size, boolean forDrawing) {
        if (style == HelperDisplay.barFont.getStyle() && size == HelperDisplay.barFont.getSize()) {
            if (fontBar == null || style != HelperDisplay.barFont.getStyle() || size != HelperDisplay.barFont.getSize()) fontBar = createCustomFont(null, null, style, size);
            return fontBar;
        }
        if (style == Font.STYLE_PLAIN && size != HelperRMSStoreMLibera.appDataFontSize) {
            if (fontStd == null || fontStd.fontSize != size) fontStd = createCustomFont(null, null, style, size);
            return fontStd;
        }
        if (style == Font.STYLE_BOLD && size == XMLTagFont.FONT_LARGE) {
            if (fontH1 == null || style != Font.STYLE_BOLD || size != XMLTagFont.FONT_LARGE) fontH1 = createCustomFont(null, null, style, size);
            return fontH1;
        }
        FontCustom ct = FontCustom.getCachedSizeFontCustom(style, size);
        if (ct != null) return ct;
        if (ct == null && style == Font.STYLE_UNDERLINED) {
            FontCustom ct2 = getCustomFont(Font.STYLE_PLAIN, size, true);
            if (!forDrawing) return ct2;
            if (size == Font.SIZE_SMALL) ct = new FontCustom(ct2.strip, charsetImgs, widthsImgRegularSmall, ct2.charSpacing); else if (size == Font.SIZE_MEDIUM) ct = new FontCustom(ct2.strip, charsetImgs, widthsImgRegular, ct2.charSpacing); else ct = new FontCustom(ct2.strip, charsetImgs, widthsImgRegularLarge, ct2.charSpacing);
            ct.fontSize = size;
            ct.fontStyle = style;
            ct.style_underlined = true;
            return ct;
        }
        ct = createCustomFont(null, null, style, size);
        cacheFontCustomSize(style, size, ct);
        return ct;
    }

    private static FontCustom createCustomFont(String imgPath, int[] widths, int fontStyle, int fontSize) {
        try {
            if (imgPath == null) {
                if (fontSize == Font.SIZE_SMALL) {
                    if (fontStyle == Font.STYLE_BOLD) {
                        imgPath = pathImgBoldSmall;
                        widths = widthsImgBoldSmall;
                    } else if (fontStyle == Font.STYLE_ITALIC) {
                        imgPath = pathImgItalicSmall;
                        widths = widthsImgItalicSmall;
                    } else {
                        imgPath = pathImgRegularSmall;
                        widths = widthsImgRegularSmall;
                    }
                } else if (fontSize == Font.SIZE_LARGE) {
                    if (fontStyle == Font.STYLE_BOLD) {
                        imgPath = pathImgBoldLarge;
                        widths = widthsImgBoldLarge;
                    } else if (fontStyle == Font.STYLE_ITALIC) {
                        imgPath = pathImgItalicLarge;
                        widths = widthsImgItalicLarge;
                    } else {
                        imgPath = pathImgRegularLarge;
                        widths = widthsImgRegularLarge;
                    }
                } else {
                    if (fontStyle == Font.STYLE_BOLD) {
                        imgPath = pathImgBold;
                        widths = widthsImgBold;
                    } else if (fontStyle == Font.STYLE_ITALIC) {
                        imgPath = pathImgItalic;
                        widths = widthsImgItalic;
                    } else {
                        imgPath = pathImgRegular;
                        widths = widthsImgRegular;
                    }
                }
            }
            Image img = HelperApp.getJarImage(imgPath + ".png");
            if (img == null && fontSize != Font.SIZE_MEDIUM) return createCustomFont(null, null, fontStyle, Font.SIZE_MEDIUM);
            if (img == null && fontStyle != Font.STYLE_PLAIN) return createCustomFont(null, null, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
            FontCustom fc = new FontCustom(img, charsetImgs, widths, charSpacingImgs);
            fc.fontSize = fontSize;
            fc.fontStyle = fontStyle;
            if (fontStyle == Font.STYLE_UNDERLINED) fc.style_underlined = true;
            return fc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int type;

    private int color;

    private int size;

    private int fontStyle;

    private int fontSize;

    private int charSpacing;

    private int spaceWidth;

    private char[] charset;

    private Image strip;

    private int[] widths;

    private int[] offsets;

    private boolean style_underlined = false;

    public static final int TYPE_STRIP = 2;

    /**
	 * Creates a font-strip.
	 * @param img
	 * @param widths array of each char width
	 * @param charset
	 * @param charSpacing
	 * @since 1.2
	 */
    public FontCustom(Image img, char[] charset, int[] widths, int charSpacing) {
        _Font(img, widths, charset, charSpacing);
    }

    private final void _Font(Image img, int[] widths, char[] charset, int charSpacing) {
        this.strip = img;
        this.size = img.getHeight();
        this.widths = new int[widths.length];
        for (int i = 0; i < widths.length; i++) this.widths[i] = widths[i];
        this.charset = new char[charset.length];
        for (int i = 0; i < charset.length; i++) this.charset[i] = charset[i];
        this.spaceWidth = this.size / 2;
        this.charSpacing = charSpacing;
        this.offsets = new int[widths.length];
        for (int i = 1; i < widths.length; i++) {
            this.offsets[i] = this.offsets[i - 1] + widths[i - 1];
        }
        this.type = TYPE_STRIP;
        for (int i = 0; i < this.charset.length; i++) for (int j = i + 1; j < this.charset.length; j++) if (this.charset[j] < this.charset[i]) {
            final char tc = this.charset[i];
            this.charset[i] = this.charset[j];
            this.charset[j] = tc;
            final int ti = this.widths[i];
            this.widths[i] = this.widths[j];
            this.widths[j] = ti;
            final int to = this.offsets[i];
            this.offsets[i] = this.offsets[j];
            this.offsets[j] = to;
        }
    }

    /** Gets the color of the font. */
    public final int getColor() {
        return color;
    }

    /** Sets the color of the font if it's not a bitmap font */
    public final void setColor(int color) {
        this.color = color;
    }

    /** Gets the size of the font. */
    public final int getSize() {
        return size;
    }

    /**
	 * Gets the charset length or -1 if it is not available.<br>
	 * The default implementation, returns -1 if this is a system native font,
	 * otherwise this returns the charset length. 
	 */
    public int getCharsetLength() {
        return charset.length;
    }

    /**
	 * Copies the charset into the given buffer at the current offset.<br>
	 * If {@link #getCharsetLength()} returns -1, this does nothing.
	 */
    public void getCharset(char[] buffer, int offset, int length) {
        System.arraycopy(charset, 0, buffer, offset, length);
    }

    /** Gets the font height. */
    public int getFontHeight() {
        return size;
    }

    /**
	 * Writes the string into the given graphics object.<br>
	 * It uses the width and height parameters to fit the string according to the anchor.<br>
	 * The anchor value must be a <a href="./Component.html#align">Component ALIGN constant</a>.
	 * This method uses {@link #write(Graphics, String, int, int, int)}.
	 */
    public final void write(Graphics g, char[] text, int offset, int length, int x, int y, int anchor) {
        char[] s = new char[length];
        System.arraycopy(text, offset, s, 0, length);
        int width = this.getWidth(s);
        if ((anchor & Graphics.HCENTER) != 0) x += width / 2; else if ((anchor & Graphics.RIGHT) != 0) x += width;
        Image oldImage = this.strip;
        if (g.getColor() != 0) {
            strip = getCachedColorImage(oldImage, g.getColor());
            if (strip == null) {
                System.out.println("new color:" + g.getColor() + "," + oldImage + "," + oldImage.hashCode());
                strip = imageColor(oldImage, g.getColor());
                cacheImageColor(oldImage, g.getColor(), strip);
            }
        }
        int ytop = y;
        if ((anchor & Graphics.VCENTER) != 0) {
            ytop = y + getFontHeight() / 2;
            _write(g, s, x, ytop, Graphics.TOP | (anchor & 13));
        } else if ((anchor & Graphics.BOTTOM) != 0) {
            ytop = y - getFontHeight();
            _write(g, s, x, ytop, Graphics.TOP | (anchor & 13));
        } else _write(g, s, x, ytop, anchor);
        if (style_underlined) {
            ytop += getFontHeight();
            g.drawLine(x, ytop, x + this._getWidth(text, offset, length), ytop);
        }
        this.strip = oldImage;
    }

    private final int _getIndex(char c) {
        int high = charset.length, low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (charset[probe] < c) low = probe; else high = probe;
        }
        if (!(high >= charset.length || charset[high] != c)) return high;
        return _getIndex('_');
    }

    private final int getWidth(String s) {
        return _getWidth(s.toCharArray(), 0, s.length());
    }

    private final int getWidth(char[] s) {
        return _getWidth(s, 0, s.length);
    }

    private final int _getWidth(char c) {
        if (c == ' ') return spaceWidth;
        switch(type) {
            case TYPE_STRIP:
                return widths[_getIndex(c)];
            default:
                return 0;
        }
    }

    private final int _getWidth(final char[] s, int offset, int length) {
        if (s.length == 0) return 0;
        int len = 0;
        for (int i = offset; i < offset + length && i < s.length; i++) len += _getWidth(s[i]) + charSpacing;
        if (s.length > 0) len -= charSpacing;
        return len;
    }

    private final void _write(final Graphics g, final char[] s, int x, int y, int anchor) {
        int cx = 0, cy = 0, cw = 0, ch = 0;
        if (type == TYPE_STRIP) {
            cx = g.getClipX();
            cy = g.getClipY();
            cw = g.getClipWidth();
            ch = g.getClipHeight();
        }
        if ((anchor & Graphics.RIGHT) != 0) {
            for (int i = s.length - 1; i >= 0; i--) {
                if (s[i] == ' ') x -= (spaceWidth + charSpacing); else {
                    final int index = _getIndex(s[i]);
                    x -= widths[index];
                    g.clipRect(x, y, widths[index], size);
                    g.drawImage(strip, x - offsets[index], y - 0, 0);
                    g.setClip(cx, cy, cw, ch);
                    x -= charSpacing;
                }
            }
        } else if ((anchor & Graphics.HCENTER) != 0) {
            final int w = getWidth(s);
            for (int i = 0; i < s.length; i++) {
                if (s[i] == ' ') x += spaceWidth + charSpacing; else {
                    final int index = _getIndex(s[i]);
                    g.clipRect(x - w, y, widths[index], size);
                    g.drawImage(strip, x - offsets[index] - w, y - 0, 0);
                    x += widths[index] + charSpacing;
                    g.setClip(cx, cy, cw, ch);
                }
            }
        } else if ((anchor & Graphics.LEFT) != 0 || anchor == 0) {
            for (int i = 0; i < s.length; i++) {
                if (s[i] == ' ') x += spaceWidth + charSpacing; else {
                    final int index = _getIndex(s[i]);
                    g.clipRect(x, y, widths[index], size);
                    g.drawImage(strip, x - offsets[index], y - 0, 0);
                    x += widths[index] + charSpacing;
                    g.setClip(cx, cy, cw, ch);
                }
            }
        } else throw new IllegalArgumentException();
    }

    public static final void imageColor(int ai[], int i) {
        for (int j = 0; j < ai.length; j++) {
            int a = (int) ((ai[j] & 0xFF000000) >>> 24);
            if (a == 0) continue;
            ai[j] = a << 24 | i;
        }
    }

    public static final Image imageColor(Image image, int newcolor) {
        int ai[] = new int[image.getWidth() * image.getHeight()];
        image.getRGB(ai, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        imageColor(ai, newcolor);
        return Image.createRGBImage(ai, image.getWidth(), image.getHeight(), true);
    }

    private static int[] chacheColorId = new int[maxColorCache];

    private static int[] chacheColorHash = new int[maxColorCache];

    private static Image[] chacheColor = new Image[maxColorCache];

    private static int lastCacheImgColorPos = -1;

    private static void cacheImageColor(Image imgOriginal, int color, Image newImg) {
        if (maxColorCache <= 0) return;
        int pos = -1;
        for (int i = 0; i < chacheColorHash.length; i++) {
            if (chacheColor[i] == null) {
                pos = i;
                break;
            }
        }
        if (pos < 0) pos = lastCacheImgColorPos + 1;
        if (pos >= chacheColor.length) pos = 0;
        lastCacheImgColorPos = pos;
        chacheColorId[pos] = color;
        chacheColor[pos] = newImg;
        chacheColorHash[pos] = imgOriginal.hashCode();
    }

    private static Image getCachedColorImage(Image imgOriginal, int color) {
        int hash = imgOriginal.hashCode();
        for (int i = 0; i < chacheColorHash.length; i++) {
            if (chacheColorHash[i] == hash && chacheColorId[i] == color) return chacheColor[i];
        }
        return null;
    }

    private static int[] chacheSizeSize = new int[maxSizeCache];

    private static int[] chacheSizeStyle = new int[maxSizeCache];

    private static FontCustom[] chacheSize = new FontCustom[maxSizeCache];

    private static int lastCacheImgSizePos = -1;

    private static void cacheFontCustomSize(int style, int size, FontCustom newImg) {
        if (maxSizeCache <= 0) return;
        int pos = -1;
        for (int i = 0; i < chacheSize.length; i++) {
            if (chacheSize[i] == null) {
                pos = i;
                break;
            }
        }
        if (pos < 0) pos = lastCacheImgSizePos + 1;
        if (pos >= chacheSize.length) pos = 0;
        lastCacheImgSizePos = pos;
        chacheSizeSize[pos] = size;
        chacheSize[pos] = newImg;
        chacheSizeStyle[pos] = style;
    }

    private static FontCustom getCachedSizeFontCustom(int style, int size) {
        for (int i = 0; i < chacheSize.length; i++) {
            if (chacheSizeStyle[i] == style && chacheSizeSize[i] == size) return chacheSize[i];
        }
        return null;
    }
}
