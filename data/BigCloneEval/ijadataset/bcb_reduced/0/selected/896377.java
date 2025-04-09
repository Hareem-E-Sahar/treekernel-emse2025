package org.apache.myfaces.trinidadinternal.image.encode;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import org.apache.myfaces.trinidadinternal.image.painter.ImageLoader;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;

/**
 * Generates a PNG graphics file given pixel data.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/image/encode/PNGEncoder.java#0 $) $Date: 10-nov-2005.19:05:21 $
 * @since 0.1.4
 */
final class PNGEncoder {

    /**
   * Encodes the Image to the specified OutputStream in PNG format
   */
    public static void encode(Image image, OutputStream out) throws IOException {
        ImageLoader loader = new ImageLoader(image);
        loader.start();
        if (!loader.waitFor()) {
            throw new IllegalArgumentException(_LOG.getMessage("PROBLEM_LOADING"));
        }
        int width = image.getWidth(loader);
        int height = image.getHeight(loader);
        int[] pixels = new int[width * height];
        PixelGrabber grabber = new PixelGrabber(image.getSource(), 0, 0, width, height, pixels, 0, width);
        try {
            grabber.grabPixels();
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(_LOG.getMessage("GRABBING_PIXELS"));
        }
        if ((grabber.getStatus() & ImageObserver.ABORT) != 0) {
            throw new IllegalArgumentException(_LOG.getMessage("ERROR_FETCHING_IMAGE", new Object[] { pixels.length, width, height }));
        }
        Hashtable<Color, Integer> colors = new Hashtable<Color, Integer>();
        int count = 0;
        Color lastColor = null;
        int lastPixel = -2;
        Color firstColor = _createColor(pixels[0]);
        Color transparentColor = null;
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            if ((pixel != lastPixel) || (lastColor == null)) {
                Color color = _createColor(pixel);
                if (!colors.containsKey(color)) {
                    if ((color.getAlpha() == 0) && (transparentColor == null)) {
                        colors.put(color, 0);
                        colors.put(firstColor, count);
                        transparentColor = color;
                    } else {
                        colors.put(color, count);
                    }
                    count++;
                }
                lastPixel = pixel;
                lastColor = color;
            }
        }
        boolean transparent = (transparentColor != null);
        _writeSignature(out);
        if (count <= 256) _writePaletteImage(width, height, pixels, colors, transparent, out); else _writeRGBImage(width, height, pixels, out);
        _writeEnd(out);
    }

    private PNGEncoder() {
    }

    private static void _writePaletteImage(int width, int height, int[] pixels, Map<Color, Integer> colors, boolean transparent, OutputStream out) throws IOException {
        int count = colors.size();
        assert (count <= 256);
        int depth = 8;
        _writeHeader(width, height, (byte) depth, (byte) 3, out);
        _writePalette(colors, out);
        if (transparent) {
            _writeTransparency(out);
        }
        byte[] data = _getIndexedData(width, height, depth, pixels, colors);
        _writeData(data, out);
    }

    private static void _writeRGBImage(int width, int height, int[] pixels, OutputStream out) throws IOException {
        _writeHeader(width, height, (byte) 8, (byte) 2, out);
        byte[] data = new byte[(pixels.length * 3) + height];
        int sourceLine = 0;
        int targetLine = 0;
        for (int i = 0; i < height; i++) {
            data[targetLine] = (byte) 0;
            for (int j = 0; j < width; j++) {
                int pixel = pixels[sourceLine + j];
                int target = targetLine + (j * 3) + 1;
                data[target] = _getRed(pixel);
                data[target + 1] = _getGreen(pixel);
                data[target + 2] = _getBlue(pixel);
            }
            sourceLine += width;
            targetLine += ((width * 3) + 1);
        }
        _writeData(data, out);
    }

    private static void _writeData(byte[] data, OutputStream out) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream deflater = new DeflaterOutputStream(baos);
        deflater.write(data);
        deflater.flush();
        deflater.close();
        _writeChunk(_IDAT, baos.toByteArray(), out);
    }

    private static void _writeEnd(OutputStream out) throws IOException {
        _writeChunk(_IEND, null, out);
    }

    private static void _writeHeader(int width, int height, byte depth, byte type, OutputStream out) throws IOException {
        byte[] data = new byte[13];
        _writeInt(width, 0, data);
        _writeInt(height, 4, data);
        data[8] = depth;
        data[9] = type;
        data[10] = (byte) 0;
        data[11] = (byte) 0;
        data[12] = (byte) 0;
        _writeChunk(_IHDR, data, out);
    }

    private static void _writePalette(Map<Color, Integer> colors, OutputStream out) throws IOException {
        int count = colors.size();
        byte[] data = new byte[count * 3];
        for (Map.Entry<Color, Integer> entry : colors.entrySet()) {
            Color color = entry.getKey();
            int index = entry.getValue().intValue() * 3;
            int rgb = color.getRGB();
            data[index] = _getRed(rgb);
            data[index + 1] = _getGreen(rgb);
            data[index + 2] = _getBlue(rgb);
        }
        _writeChunk(_PLTE, data, out);
    }

    private static void _writeSignature(OutputStream out) throws IOException {
        out.write(_SIGNATURE);
    }

    private static void _writeTransparency(OutputStream out) throws IOException {
        _writeChunk(_tRNS, _TRANSPARENT_DATA, out);
    }

    private static void _writeChunk(int type, byte[] data, OutputStream out) throws IOException {
        int length = (data == null) ? 0 : data.length;
        _writeInt(length, out);
        _writeInt(type, out);
        if (data != null) out.write(data);
        _write32(_getCRC(type, data), out);
    }

    private static void _writeInt(int i, OutputStream out) throws IOException {
        out.write((i >> 24) & 0x000000ff);
        out.write((i >> 16) & 0x000000ff);
        out.write((i >> 8) & 0x000000ff);
        out.write(i & 0x000000ff);
    }

    private static void _writeInt(int i, int offset, byte[] buffer) throws IOException {
        buffer[offset] = (byte) ((i >> 24) & 0x000000ff);
        buffer[offset + 1] = (byte) ((i >> 16) & 0x000000ff);
        buffer[offset + 2] = (byte) ((i >> 8) & 0x000000ff);
        buffer[offset + 3] = (byte) (i & 0x000000ff);
    }

    private static void _write32(long l, OutputStream out) throws IOException {
        out.write((int) (l >> 24) & 0x000000ff);
        out.write((int) (l >> 16) & 0x000000ff);
        out.write((int) (l >> 8) & 0x000000ff);
        out.write((int) l & 0x000000ff);
    }

    private static long _getCRC(int type, byte[] data) {
        CRC32 crc = new CRC32();
        crc.update((type >> 24) & 0x000000ff);
        crc.update((type >> 16) & 0x000000ff);
        crc.update((type >> 8) & 0x000000ff);
        crc.update(type & 0x000000ff);
        if (data != null) crc.update(data);
        return crc.getValue();
    }

    private static byte[] _getIndexedData(int width, int height, int depth, int[] pixels, Map<Color, Integer> colors) {
        assert (depth == 8);
        return _getIndexedData8(width, height, pixels, colors);
    }

    private static byte[] _getIndexedData8(int width, int height, int[] pixels, Map<Color, Integer> colors) {
        byte[] data = new byte[pixels.length + height];
        int sourceLine = 0;
        int targetLine = 0;
        Color lastColor = null;
        int lastPixel = -2;
        for (int i = 0; i < height; i++) {
            data[targetLine] = (byte) 0;
            for (int j = 0; j < width; j++) {
                int pixel = pixels[sourceLine + j];
                Color color;
                if ((pixel == lastPixel) && (lastColor != null)) color = lastColor; else color = _createColor(pixel);
                int index = colors.get(color).intValue();
                data[targetLine + j + 1] = (byte) index;
                lastPixel = pixel;
                lastColor = color;
            }
            sourceLine += width;
            targetLine += (width + 1);
        }
        return data;
    }

    private static final Color _createColor(int argb) {
        if ((argb & 0xff000000) != 0) return new Color(argb | 0xff000000);
        int a = ((argb >> 24) & 0xff);
        int r = ((argb >> 16) & 0xff);
        int g = ((argb >> 8) & 0xff);
        int b = (argb & 0xff);
        return new Color(r, g, b, a);
    }

    private static byte _getRed(int c) {
        return (byte) ((c >> 16) & 255);
    }

    private static byte _getGreen(int c) {
        return (byte) ((c >> 8) & 255);
    }

    private static byte _getBlue(int c) {
        return (byte) (c & 255);
    }

    private static final byte[] _SIGNATURE = { (byte) 137, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10 };

    private static final int _IHDR = 0x49484452;

    private static final int _PLTE = 0x504c5445;

    private static final int _IDAT = 0x49444154;

    private static final int _IEND = 0x49454e44;

    private static final int _tRNS = 0x74524e53;

    private static final byte[] _TRANSPARENT_DATA = new byte[] { (byte) 0 };

    private static final TrinidadLogger _LOG = TrinidadLogger.createTrinidadLogger(PNGEncoder.class);
}
