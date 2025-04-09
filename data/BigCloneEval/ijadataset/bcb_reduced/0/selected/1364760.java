package org.apache.harmony.awt.gl.font;

import java.awt.font.GlyphMetrics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.Math;
import org.apache.harmony.awt.ContextStorage;
import org.apache.harmony.awt.nativebridge.linux.*;
import org.apache.harmony.awt.nativebridge.Int16Pointer;
import org.apache.harmony.awt.nativebridge.Int8Pointer;
import org.apache.harmony.awt.gl.font.Glyph;
import org.apache.harmony.awt.gl.font.LinuxNativeFont;
import org.apache.harmony.awt.wtk.linux.LinuxWindowFactory;
import org.apache.harmony.awt.nativebridge.linux.LinuxNativeFontWrapper;

/**
 * Linux implementation of the Glyph class
 */
public class LinuxGlyph extends Glyph {

    private final Xft xft = Xft.getInstance();

    private final LinuxNativeFontWrapper lnfw = LinuxNativeFontWrapper.getInstance();

    LinuxNativeFontWrapper.GlyphBitmap gBmp = null;

    /** 
     * Constructor
     */
    public LinuxGlyph(long pFnt, int fntSize, char c, int glyphIndex) {
        int[] pxlMetrics = new int[6];
        this.pFont = pFnt;
        this.fontSize = fntSize;
        long display = ((LinuxWindowFactory) ContextStorage.getWindowFactory()).getDisplay();
        switch(c) {
            case '\t':
            case '\r':
            case '\n':
                break;
            default:
                pxlMetrics = LinuxNativeFont.getGlyphPxlInfoNative(display, this.pFont, c);
                if (pxlMetrics == null) {
                    pxlMetrics = new int[6];
                }
                break;
        }
        this.glCode = glyphIndex;
        this.glChar = c;
        Rectangle rct = new Rectangle(pxlMetrics[0], -pxlMetrics[1], pxlMetrics[4], pxlMetrics[5]);
        this.glPointMetrics = new GlyphMetrics(pxlMetrics[2], rct, (byte) 1);
        this.glMetrics = new GlyphMetrics((float) Math.ceil(pxlMetrics[2]), rct, (byte) 0);
    }

    /**
     * Default Glyph constructor
     */
    public LinuxGlyph(char c, int glyphIndex) {
        float metrics[] = new float[6];
        int[] pxlMetrics = new int[6];
        this.pFont = 0;
        this.fontSize = 0;
        Rectangle2D.Float rect = new Rectangle2D.Float(metrics[0], -metrics[1], metrics[4], metrics[5]);
        this.glMetrics = new GlyphMetrics((float) Math.ceil(metrics[2]), rect, (byte) 0);
        this.glCode = glyphIndex;
        this.glChar = c;
        Rectangle rct = new Rectangle(pxlMetrics[0], -pxlMetrics[1], pxlMetrics[4], pxlMetrics[5]);
        this.glPointMetrics = new GlyphMetrics(pxlMetrics[2], rct, (byte) 1);
    }

    /**
     * Returns cached bitmap of the glyph's bitmap. Returns null if this
     * Glyph object has height or width equal to zero.  
     */
    public byte[] getBitmap() {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) {
            return null;
        }
        if (this.bitmap == null) {
            initFTBitmap();
        }
        return this.bitmap;
    }

    /**
     * Returns cached GlyphBitmap object representing bitmap data of this glyph.
     * If cached value is null - bitmap data is to be obtained from native code.
     * @return GlyphBitmap data object
     */
    public LinuxNativeFontWrapper.GlyphBitmap initFTBitmap() {
        if (this.gBmp == null) {
            long ptr = LinuxNativeFont.NativeInitGlyphBitmap(this.pFont, this.glChar);
            if (ptr != 0) {
                this.gBmp = lnfw.createGlyphBitmap(ptr);
                Xft.FT_Bitmap ft_bitmap = gBmp.get_bitmap();
                Int8Pointer buffer = ft_bitmap.get_buffer();
                this.bmp_left = gBmp.get_left();
                this.bmp_top = gBmp.get_top();
                this.bmp_pitch = ft_bitmap.get_pitch();
                this.bmp_rows = ft_bitmap.get_rows();
                this.bmp_width = ft_bitmap.get_width();
                int bufSize = bmp_pitch * bmp_rows;
                bitmap = new byte[bufSize];
                buffer.get(bitmap, 0, bufSize);
                LinuxNativeFont.NativeFreeGlyphBitmap(ptr);
            }
        }
        return this.gBmp;
    }

    public BufferedImage getImage() {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) {
            return null;
        }
        byte[] pixels;
        int alignedWidth;
        int width;
        int height;
        if (this.image == null) {
            pixels = getBitmap();
            DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);
            height = (int) this.glPointMetrics.getBounds2D().getHeight();
            alignedWidth = (pixels.length / height) << 3;
            width = (int) this.glPointMetrics.getBounds2D().getWidth();
            WritableRaster wr = Raster.createPackedRaster(dataBuffer, alignedWidth, height, 1, null);
            byte[] blackWhite = new byte[] { 0, (byte) 0xff };
            IndexColorModel colorModel = new IndexColorModel(1, 2, blackWhite, blackWhite, blackWhite);
            this.image = new BufferedImage(colorModel, wr.createWritableChild(0, 0, width, height, 0, 0, null), false, null);
        }
        return this.image;
    }

    public Shape initOutline(char c) {
        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) {
            return gp;
        }
        Shape shape = null;
        long ptr;
        ptr = LinuxNativeFont.getGlyphOutline(this.pFont, c);
        if (ptr == 0) {
            return gp;
        }
        Xft.FT_Outline outline = xft.createFT_Outline(ptr);
        int n_contours = outline.get_n_contours();
        if (n_contours == 0) {
            LinuxNativeFont.freeGlyphOutline(ptr);
            return gp;
        }
        Xft.FT_Vector pPoints = outline.get_points();
        long pPointsPtr = pPoints.lock();
        pPoints.unlock();
        int size = outline.get_n_points();
        float points[] = LinuxNativeFont.getPointsFromFTVector(pPointsPtr, size);
        Int16Pointer pContours = outline.get_contours();
        Int8Pointer pTags = outline.get_tags();
        int index = 0;
        int tag;
        float x_start;
        float y_start;
        float x_finish;
        float y_finish;
        for (int i = 0; i < n_contours; i++) {
            short end = pContours.get(i);
            x_start = points[index * 2];
            y_start = points[index * 2 + 1];
            x_finish = points[end * 2];
            y_finish = points[end * 2 + 1];
            tag = pTags.get(index);
            if (tag == LinuxNativeFontWrapper.FT_CURVE_TAG_CONIC) {
                tag = pTags.get(end);
                if ((tag & LinuxNativeFontWrapper.FT_CURVE_TAG_ON) == 0) {
                    x_start = x_finish;
                    y_start = y_finish;
                    end--;
                } else {
                    x_start = (x_start + x_finish) / 2;
                    y_start = (y_start + y_finish) / 2;
                    x_finish = x_start;
                    y_finish = y_start;
                    index--;
                }
            }
            gp.moveTo(x_start, y_start);
            while (index < end) {
                index++;
                tag = pTags.get(index);
                switch((tag & 3)) {
                    case (LinuxNativeFontWrapper.FT_CURVE_TAG_ON):
                        float x = points[index * 2];
                        float y = points[index * 2 + 1];
                        gp.lineTo(x, y);
                        break;
                    case (LinuxNativeFontWrapper.FT_CURVE_TAG_CONIC):
                        float x1 = points[index * 2];
                        float y1 = points[index * 2 + 1];
                        float x2;
                        float y2;
                        while (index < end) {
                            index++;
                            tag = pTags.get(index);
                            x2 = points[index * 2];
                            y2 = points[index * 2 + 1];
                            if ((tag & LinuxNativeFontWrapper.FT_CURVE_TAG_ON) != 0) {
                                gp.quadTo(x1, y1, x2, y2);
                                break;
                            } else {
                                gp.quadTo(x1, y1, (x1 + x2) / 2, (y1 + y2) / 2);
                                x1 = x2;
                                y1 = y2;
                            }
                        }
                        if ((index == end) && ((tag & LinuxNativeFontWrapper.FT_CURVE_TAG_ON) == 0)) {
                            gp.quadTo(x1, y1, x_start, y_start);
                        }
                        break;
                    case (LinuxNativeFontWrapper.FT_CURVE_TAG_CUBIC):
                        x1 = points[index * 2];
                        y1 = points[index * 2 + 1];
                        index++;
                        x2 = points[index * 2];
                        y2 = points[index * 2 + 1];
                        if (index < end) {
                            index++;
                            float x3 = points[index * 2];
                            float y3 = points[index * 2 + 1];
                            gp.curveTo(x1, y1, x2, y2, x3, y3);
                        } else {
                            gp.curveTo(x1, y1, x2, y2, x_start, y_start);
                        }
                        break;
                    default:
                        LinuxNativeFont.freeGlyphOutline(ptr);
                        return new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                }
            }
            gp.lineTo(x_start, y_start);
            index++;
        }
        shape = gp;
        LinuxNativeFont.freeGlyphOutline(ptr);
        return shape;
    }
}
