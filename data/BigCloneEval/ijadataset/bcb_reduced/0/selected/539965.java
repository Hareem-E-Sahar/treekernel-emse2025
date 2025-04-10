package midpcalc;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

class GFontData {

    private int charHeight, charMaxWidth;

    private byte[] charBits;

    private String charSet;

    byte[] charWidth;

    byte[] charXOff;

    byte[] charItalicOffset;

    private boolean bgr;

    private boolean sizeX2;

    private Image charCache;

    private Graphics charCacheGraphics;

    private boolean largeCache;

    private int canvasWidth, canvasHeight;

    private int cacheWidth, cacheHeight, cacheSize;

    private int bucketSize;

    private int[] cacheHash;

    private short[] cacheTime;

    private short time;

    private int refCount;

    private int largeCacheRefCount;

    private int[] lineBuf1;

    private int[] lineBuf2;

    private static final int BOLD = 1;

    private static final int ITALIC = 2;

    private static GFontData smallFontData;

    private static GFontData mediumFontData;

    private static GFontData largeFontData;

    private static GFontData xlargeFontData;

    private static GFontData xxlargeFontData;

    private static GFontData xxxlargeFontData;

    public static GFontData getGFontData(int style, boolean largeCache, CanvasAccess canvas) {
        GFontData data = null;
        int size = style & UniFont.SIZE_MASK;
        try {
            if (size == UniFont.SMALL) {
                if (smallFontData == null) {
                    smallFontData = new GFontData(style, largeCache, canvas);
                }
                data = smallFontData;
            } else if (size == UniFont.MEDIUM) {
                if (mediumFontData == null) {
                    mediumFontData = new GFontData(style, largeCache, canvas);
                }
                data = mediumFontData;
            } else if (size == UniFont.LARGE) {
                if (largeFontData == null) {
                    largeFontData = new GFontData(style, largeCache, canvas);
                }
                data = largeFontData;
            } else if (size == UniFont.XLARGE) {
                if (xlargeFontData == null) {
                    xlargeFontData = new GFontData(style, largeCache, canvas);
                }
                data = xlargeFontData;
            } else if (size == UniFont.XXLARGE) {
                if (xxlargeFontData == null) {
                    xxlargeFontData = new GFontData(style, largeCache, canvas);
                }
                data = xxlargeFontData;
            } else {
                if (xxxlargeFontData == null) {
                    xxxlargeFontData = new GFontData(style, largeCache, canvas);
                }
                data = xxxlargeFontData;
            }
            if (largeCache && !data.hasLargeCache()) {
                data.setLargeCache(largeCache);
            }
            data.refCount++;
            if (largeCache) data.largeCacheRefCount++;
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize font");
        } catch (OutOfMemoryError e) {
            if (data != null) data.close(largeCache);
            throw e;
        }
    }

    public void close(boolean neededLargeCache) {
        refCount--;
        if (neededLargeCache) largeCacheRefCount--;
        if (refCount <= 0) {
            charBits = null;
            charSet = null;
            charXOff = null;
            charWidth = null;
            charCache = null;
            charCacheGraphics = null;
            cacheHash = null;
            cacheTime = null;
            if (this == smallFontData) {
                smallFontData = null;
            } else if (this == mediumFontData) {
                mediumFontData = null;
            } else if (this == largeFontData) {
                largeFontData = null;
            } else if (this == xlargeFontData) {
                xlargeFontData = null;
            } else if (this == xxlargeFontData) {
                xxlargeFontData = null;
            } else if (this == xxxlargeFontData) {
                xxxlargeFontData = null;
            }
        } else if (largeCache && largeCacheRefCount <= 0) {
            setLargeCache(false);
        }
    }

    private GFontData(int style, boolean largeCache, CanvasAccess canvas) throws IOException {
        String charBitsResource = null;
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        int size = style & UniFont.SIZE_MASK;
        bgr = (style & UniFont.BGR_ORDER) != 0;
        sizeX2 = false;
        switch(size) {
            case UniFont.SMALL:
            default:
                charMaxWidth = GFontBase.smallCharMaxWidth;
                charHeight = GFontBase.smallCharHeight;
                charBitsResource = GFontBase.smallCharBitsResource;
                charSet = GFontBase.smallCharSet;
                break;
            case UniFont.MEDIUM:
                charMaxWidth = GFontBase.mediumCharMaxWidth;
                charHeight = GFontBase.mediumCharHeight;
                charBitsResource = GFontBase.mediumCharBitsResource;
                charSet = GFontBase.mediumCharSet;
                break;
            case UniFont.LARGE:
            case UniFont.XXLARGE:
                charMaxWidth = GFontBase.largeCharMaxWidth;
                charHeight = GFontBase.largeCharHeight;
                charBitsResource = GFontBase.largeCharBitsResource;
                charSet = GFontBase.largeCharSet;
                sizeX2 = size == UniFont.XXLARGE;
                break;
            case UniFont.XLARGE:
            case UniFont.XXXLARGE:
                charMaxWidth = GFontBase.xlargeCharMaxWidth;
                charHeight = GFontBase.xlargeCharHeight;
                charBitsResource = GFontBase.xlargeCharBitsResource;
                charSet = GFontBase.xlargeCharSet;
                sizeX2 = size == UniFont.XXXLARGE;
                break;
        }
        InputStream in = getClass().getResourceAsStream(charBitsResource);
        charBits = readFully(in, charSet.length() * ((charHeight * charMaxWidth * 2 * 2 + 7) / 8));
        byte[] charXOffRes = readFully(in, charSet.length());
        byte[] charWidthRes = readFully(in, charSet.length());
        byte[] charItalicOffsetRes = readFully(in, charSet.length());
        in.close();
        charXOff = new byte[256];
        charWidth = new byte[256];
        charItalicOffset = new byte[256];
        for (int i = 0; i < 256; i++) {
            int pos = charSet.indexOf(i);
            if (pos < 0) {
                pos = charSet.indexOf('?');
            }
            charXOff[i] = charXOffRes[pos];
            charWidth[i] = charWidthRes[pos];
            charItalicOffset[i] = charItalicOffsetRes[pos];
        }
        lineBuf1 = new int[charMaxWidth * 2];
        if (sizeX2) {
            lineBuf2 = new int[charMaxWidth * 2];
            charMaxWidth *= 2;
            charHeight *= 2;
            for (int i = 0; i < 256; i++) {
                charXOff[i] *= 2;
                charWidth[i] *= 2;
            }
        }
        setLargeCache(largeCache);
    }

    private byte[] readFully(InputStream in, int size) throws IOException {
        byte[] bits = new byte[size];
        for (int pos = 0; pos < bits.length; ) pos += in.read(bits, pos, size - pos);
        return bits;
    }

    public boolean hasLargeCache() {
        return largeCache;
    }

    void setLargeCache(boolean largeCache) {
        cacheHash = null;
        cacheTime = null;
        charCache = null;
        charCacheGraphics = null;
        this.largeCache = largeCache;
        cacheWidth = largeCache ? 16 : 10;
        if (cacheWidth * charMaxWidth > canvasWidth) {
            cacheWidth = canvasWidth / charMaxWidth;
        }
        cacheHeight = (largeCache ? 128 : 40) / cacheWidth;
        if (cacheHeight * charHeight > canvasHeight) {
            cacheHeight = canvasHeight / charHeight;
        }
        cacheSize = cacheWidth * cacheHeight;
        bucketSize = 4;
        cacheHash = new int[cacheSize];
        cacheTime = new short[cacheSize];
        time = 0;
        charCache = Image.createImage(charMaxWidth * cacheWidth, charHeight * cacheHeight);
        charCacheGraphics = charCache.getGraphics();
    }

    int getIndex(char ch, int fg, int bg, int flags) {
        int i, i2, j;
        if (++time < 0) {
            for (i = 0; i < cacheSize; i++) cacheTime[i] = 0;
            time = 1;
        }
        int hash = (ch + (flags << 8) + (fg << 10) + (bg << (10 + Colors.colorBits))) * 239;
        int bucketStart = hash % cacheSize;
        for (i = i2 = bucketStart; i < bucketStart + bucketSize; i++, i2++) {
            if (i2 >= cacheSize) i2 -= cacheSize;
            if (cacheHash[i2] == hash) {
                cacheTime[i2] = time;
                return i2;
            }
        }
        j = bucketStart;
        for (i = i2 = bucketStart + 1; i < bucketStart + bucketSize; i++, i2++) {
            if (i2 >= cacheSize) i2 -= cacheSize;
            if (cacheTime[i2] < cacheTime[j]) j = i2;
        }
        cacheHash[j] = hash;
        cacheTime[j] = time;
        return -j - 1;
    }

    void unpackLine(int[] lineBuf, int resIndex, int ch, int y, int flags) {
        int w = sizeX2 ? charMaxWidth / 2 : charMaxWidth;
        int h = sizeX2 ? charHeight / 2 : charHeight;
        int xOff = 0;
        int xOff1 = 0;
        if ((flags & ITALIC) != 0) {
            xOff = -h / 4 + y / 2 + charItalicOffset[ch];
            xOff1 = y & 1;
        }
        if (y >= 0 && y < h) {
            int bitPosBase = ((resIndex * h + y) * w) * 4;
            for (int x = 0; x < w * 2; x++) {
                int x2 = x + xOff;
                int a = 0;
                if (x2 >= 0 && x2 < w * 2) {
                    int bitPos = bitPosBase + x2 * 2;
                    a = ((charBits[bitPos / 8] >> (bitPos & 7)) & 3) * 85;
                }
                if (xOff1 != 0) {
                    int b = 0;
                    x2++;
                    if (x2 >= 0 && x2 < w * 2) {
                        int bitPos = bitPosBase + x2 * 2;
                        b = ((charBits[bitPos / 8] >> (bitPos & 7)) & 3) * 85;
                    }
                    a = (a + b) / 2;
                }
                lineBuf[x] = a;
            }
        } else {
            for (int x = 0; x < w * 2; x++) {
                lineBuf[x] = 0;
            }
        }
    }

    void renderChar(int cacheX, int cacheY, int resIndex, int ch, int fg, int bg, int flags) {
        int fg_r = (fg >> 16) & 0xff;
        fg_r += (fg_r >> 7);
        int fg_g = (fg >> 8) & 0xff;
        fg_g += (fg_g >> 7);
        int fg_b = fg & 0xff;
        fg_b += (fg_b >> 7);
        int bg_r = (bg >> 16) & 0xff;
        bg_r += (bg_r >> 7);
        int bg_g = (bg >> 8) & 0xff;
        bg_g += (bg_g >> 7);
        int bg_b = bg & 0xff;
        bg_b += (bg_b >> 7);
        Graphics g = charCacheGraphics;
        g.setColor(bg);
        g.fillRect(cacheX, cacheY, charMaxWidth, charHeight);
        if (sizeX2) {
            unpackLine(lineBuf2, resIndex, ch, -1, flags);
            for (int y2 = -2; y2 < charHeight; y2 += 2) {
                int[] lbTmp = lineBuf1;
                lineBuf1 = lineBuf2;
                lineBuf2 = lbTmp;
                unpackLine(lineBuf2, resIndex, ch, y2 / 2 + 1, flags);
                for (int x2 = 0; x2 < charMaxWidth; x2++) {
                    int gray1, gray2, gray3, gray4, gray5, gray6;
                    if (x2 > 0) {
                        gray1 = lineBuf1[x2 - 1];
                        gray2 = lineBuf2[x2 - 1];
                    } else {
                        gray1 = gray2 = 0;
                    }
                    gray3 = lineBuf1[x2];
                    gray4 = lineBuf2[x2];
                    if (x2 + 1 < charMaxWidth) {
                        gray5 = lineBuf1[x2 + 1];
                        gray6 = lineBuf2[x2 + 1];
                    } else {
                        gray5 = gray6 = 0;
                    }
                    if (gray1 + gray2 + gray3 + gray4 + gray5 + gray6 != 0) {
                        int red, green, blue, tmp;
                        tmp = gray3;
                        gray3 = gray1 + tmp * 3;
                        gray1 = gray1 * 3 + tmp;
                        gray5 = tmp * 3 + gray5;
                        tmp = gray4;
                        gray4 = gray2 + tmp * 3;
                        gray2 = gray2 * 3 + tmp;
                        gray6 = tmp * 3 + gray6;
                        if (y2 + 1 >= 0) {
                            red = (gray1 * 3 + gray2) / 8 - 128;
                            green = (gray3 * 3 + gray4) / 8 - 128;
                            blue = (gray5 * 3 + gray6) / 8 - 128;
                            if (red < 0) red = 0;
                            if (red > 255) red = 255;
                            if (green < 0) green = 0;
                            if (green > 255) green = 255;
                            if (blue < 0) blue = 0;
                            if (blue > 255) blue = 255;
                            if (bgr) {
                                tmp = blue;
                                blue = red;
                                red = tmp;
                            }
                            g.setColor((red * fg_r + (255 - red) * bg_r) >> 8, (green * fg_g + (255 - green) * bg_g) >> 8, (blue * fg_b + (255 - blue) * bg_b) >> 8);
                            g.fillRect(cacheX + x2, cacheY + y2 + 1, 1, 1);
                        }
                        if (y2 + 2 < charHeight) {
                            red = (gray1 + gray2 * 3) / 8 - 128;
                            green = (gray3 + gray4 * 3) / 8 - 128;
                            blue = (gray5 + gray6 * 3) / 8 - 128;
                            if (red < 0) red = 0;
                            if (red > 255) red = 255;
                            if (green < 0) green = 0;
                            if (green > 255) green = 255;
                            if (blue < 0) blue = 0;
                            if (blue > 255) blue = 255;
                            if (bgr) {
                                tmp = blue;
                                blue = red;
                                red = tmp;
                            }
                            g.setColor((red * fg_r + (255 - red) * bg_r) >> 8, (green * fg_g + (255 - green) * bg_g) >> 8, (blue * fg_b + (255 - blue) * bg_b) >> 8);
                            g.fillRect(cacheX + x2, cacheY + y2 + 2, 1, 1);
                        }
                    }
                }
            }
        } else {
            for (int y2 = 0; y2 < charHeight; y2++) {
                unpackLine(lineBuf1, resIndex, ch, y2, flags);
                for (int x2 = 0; x2 < charMaxWidth; x2++) {
                    int red, green, blue;
                    red = (x2 > 0) ? lineBuf1[x2 * 2 - 1] : 0;
                    green = lineBuf1[x2 * 2];
                    blue = lineBuf1[x2 * 2 + 1];
                    if (red + green + blue != 0) {
                        if (bgr) {
                            int tmp = blue;
                            blue = red;
                            red = tmp;
                        }
                        g.setColor((red * fg_r + (255 - red) * bg_r) >> 8, (green * fg_g + (255 - green) * bg_g) >> 8, (blue * fg_b + (255 - blue) * bg_b) >> 8);
                        g.fillRect(cacheX + x2, cacheY + y2, 1, 1);
                    }
                }
            }
        }
    }

    private int clipX1, clipX2, clipY1, clipY2;

    void getClip(Graphics g) {
        clipX1 = g.getClipX();
        clipY1 = g.getClipY();
        clipX2 = clipX1 + g.getClipWidth();
        clipY2 = clipY1 + g.getClipHeight();
    }

    void restoreClip(Graphics g) {
        g.setClip(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1);
    }

    int drawGFontChar(Graphics g, int x, int y, char ch, int fg, int bg, boolean monospaced, boolean bold, boolean italic) {
        int flags = (bold ? BOLD : 0) + (italic ? ITALIC : 0);
        int index = getIndex(ch, fg, bg, flags);
        boolean cacheMiss = false;
        if (index < 0) {
            index = -index - 1;
            cacheMiss = true;
        }
        int cacheX = charMaxWidth * (index % cacheWidth);
        int cacheY = charHeight * (index / cacheWidth);
        if (cacheMiss) {
            int resIndex = charSet.indexOf(ch);
            if (resIndex < 0) resIndex = charSet.indexOf('?');
            renderChar(cacheX, cacheY, resIndex, ch, Colors.c[fg], Colors.c[bg], flags);
        }
        int xOff = 0;
        int width = charMaxWidth;
        if (!monospaced) {
            xOff = charXOff[ch];
            width = charWidth[ch];
        }
        int cx1 = clipX1 > x ? clipX1 : x;
        int cx2 = clipX2 < x + width ? clipX2 : x + width;
        int cy1 = clipY1 > y ? clipY1 : y;
        int cy2 = clipY2 < y + charHeight ? clipY2 : y + charHeight;
        if (cx1 < cx2 && cy1 < cy2) {
            g.setClip(cx1, cy1, cx2 - cx1, cy2 - cy1);
            g.drawImage(charCache, x - cacheX - xOff, y - cacheY, Graphics.TOP | Graphics.LEFT);
        }
        return width;
    }
}
