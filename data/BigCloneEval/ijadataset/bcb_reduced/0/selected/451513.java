package org.apache.batik.ext.awt.image.codec.tiff;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.Deflater;
import org.apache.batik.ext.awt.image.codec.ImageEncodeParam;
import org.apache.batik.ext.awt.image.codec.ImageEncoderImpl;
import org.apache.batik.ext.awt.image.codec.SeekableOutputStream;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGQTable;

/**
 * A baseline TIFF writer. The writer outputs TIFF images in either Bilevel,
 * Greyscale, Palette color or Full Color modes.
 * 
 */
public class TIFFImageEncoder extends ImageEncoderImpl {

    private static final int TIFF_UNSUPPORTED = -1;

    private static final int TIFF_BILEVEL_WHITE_IS_ZERO = 0;

    private static final int TIFF_BILEVEL_BLACK_IS_ZERO = 1;

    private static final int TIFF_GRAY = 2;

    private static final int TIFF_PALETTE = 3;

    private static final int TIFF_RGB = 4;

    private static final int TIFF_CMYK = 5;

    private static final int TIFF_YCBCR = 6;

    private static final int TIFF_CIELAB = 7;

    private static final int TIFF_GENERIC = 8;

    private static final int COMP_NONE = 1;

    private static final int COMP_JPEG_TTN2 = 7;

    private static final int COMP_PACKBITS = 32773;

    private static final int COMP_DEFLATE = 32946;

    private static final int TIFF_JPEG_TABLES = 347;

    private static final int TIFF_YCBCR_SUBSAMPLING = 530;

    private static final int TIFF_YCBCR_POSITIONING = 531;

    private static final int TIFF_REF_BLACK_WHITE = 532;

    private static final int EXTRA_SAMPLE_UNSPECIFIED = 0;

    private static final int EXTRA_SAMPLE_ASSOCIATED_ALPHA = 1;

    private static final int EXTRA_SAMPLE_UNASSOCIATED_ALPHA = 2;

    private static final int DEFAULT_ROWS_PER_STRIP = 8;

    public TIFFImageEncoder(OutputStream output, ImageEncodeParam param) {
        super(output, param);
        if (this.param == null) {
            this.param = new TIFFEncodeParam();
        }
    }

    /**
     * Encodes a RenderedImage and writes the output to the
     * OutputStream associated with this ImageEncoder.
     */
    public void encode(RenderedImage im) throws IOException {
        writeFileHeader();
        TIFFEncodeParam encodeParam = (TIFFEncodeParam) param;
        Iterator iter = encodeParam.getExtraImages();
        if (iter != null) {
            int ifdOffset = 8;
            RenderedImage nextImage = im;
            TIFFEncodeParam nextParam = encodeParam;
            boolean hasNext;
            do {
                hasNext = iter.hasNext();
                ifdOffset = encode(nextImage, nextParam, ifdOffset, !hasNext);
                if (hasNext) {
                    Object obj = iter.next();
                    if (obj instanceof RenderedImage) {
                        nextImage = (RenderedImage) obj;
                        nextParam = encodeParam;
                    } else if (obj instanceof Object[]) {
                        Object[] o = (Object[]) obj;
                        nextImage = (RenderedImage) o[0];
                        nextParam = (TIFFEncodeParam) o[1];
                    }
                }
            } while (hasNext);
        } else {
            encode(im, encodeParam, 8, true);
        }
    }

    private int encode(RenderedImage im, TIFFEncodeParam encodeParam, int ifdOffset, boolean isLast) throws IOException {
        int compression = encodeParam.getCompression();
        boolean isTiled = encodeParam.getWriteTiled();
        int minX = im.getMinX();
        int minY = im.getMinY();
        int width = im.getWidth();
        int height = im.getHeight();
        SampleModel sampleModel = im.getSampleModel();
        int sampleSize[] = sampleModel.getSampleSize();
        for (int i = 1; i < sampleSize.length; i++) {
            if (sampleSize[i] != sampleSize[0]) {
                throw new Error("TIFFImageEncoder0");
            }
        }
        int numBands = sampleModel.getNumBands();
        if ((sampleSize[0] == 1 || sampleSize[0] == 4) && numBands != 1) {
            throw new Error("TIFFImageEncoder1");
        }
        int dataType = sampleModel.getDataType();
        switch(dataType) {
            case DataBuffer.TYPE_BYTE:
                if (sampleSize[0] != 1 && sampleSize[0] == 4 && sampleSize[0] != 8) {
                    throw new Error("TIFFImageEncoder2");
                }
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                if (sampleSize[0] != 16) {
                    throw new Error("TIFFImageEncoder3");
                }
                break;
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_FLOAT:
                if (sampleSize[0] != 32) {
                    throw new Error("TIFFImageEncoder4");
                }
                break;
            default:
                throw new Error("TIFFImageEncoder5");
        }
        boolean dataTypeIsShort = dataType == DataBuffer.TYPE_SHORT || dataType == DataBuffer.TYPE_USHORT;
        ColorModel colorModel = im.getColorModel();
        if (colorModel != null && colorModel instanceof IndexColorModel && dataType != DataBuffer.TYPE_BYTE) {
            throw new Error("TIFFImageEncoder6");
        }
        IndexColorModel icm = null;
        int sizeOfColormap = 0;
        char colormap[] = null;
        int imageType = TIFF_UNSUPPORTED;
        int numExtraSamples = 0;
        int extraSampleType = EXTRA_SAMPLE_UNSPECIFIED;
        if (colorModel instanceof IndexColorModel) {
            icm = (IndexColorModel) colorModel;
            int mapSize = icm.getMapSize();
            if (sampleSize[0] == 1 && numBands == 1) {
                if (mapSize != 2) {
                    throw new IllegalArgumentException("TIFFImageEncoder7");
                }
                byte r[] = new byte[mapSize];
                icm.getReds(r);
                byte g[] = new byte[mapSize];
                icm.getGreens(g);
                byte b[] = new byte[mapSize];
                icm.getBlues(b);
                if ((r[0] & 0xff) == 0 && (r[1] & 0xff) == 255 && (g[0] & 0xff) == 0 && (g[1] & 0xff) == 255 && (b[0] & 0xff) == 0 && (b[1] & 0xff) == 255) {
                    imageType = TIFF_BILEVEL_BLACK_IS_ZERO;
                } else if ((r[0] & 0xff) == 255 && (r[1] & 0xff) == 0 && (g[0] & 0xff) == 255 && (g[1] & 0xff) == 0 && (b[0] & 0xff) == 255 && (b[1] & 0xff) == 0) {
                    imageType = TIFF_BILEVEL_WHITE_IS_ZERO;
                } else {
                    imageType = TIFF_PALETTE;
                }
            } else if (numBands == 1) {
                imageType = TIFF_PALETTE;
            }
        } else if (colorModel == null) {
            if (sampleSize[0] == 1 && numBands == 1) {
                imageType = TIFF_BILEVEL_BLACK_IS_ZERO;
            } else {
                imageType = TIFF_GENERIC;
                if (numBands > 1) {
                    numExtraSamples = numBands - 1;
                }
            }
        } else {
            ColorSpace colorSpace = colorModel.getColorSpace();
            switch(colorSpace.getType()) {
                case ColorSpace.TYPE_CMYK:
                    imageType = TIFF_CMYK;
                    break;
                case ColorSpace.TYPE_GRAY:
                    imageType = TIFF_GRAY;
                    break;
                case ColorSpace.TYPE_Lab:
                    imageType = TIFF_CIELAB;
                    break;
                case ColorSpace.TYPE_RGB:
                    if (compression == COMP_JPEG_TTN2 && encodeParam.getJPEGCompressRGBToYCbCr()) {
                        imageType = TIFF_YCBCR;
                    } else {
                        imageType = TIFF_RGB;
                    }
                    break;
                case ColorSpace.TYPE_YCbCr:
                    imageType = TIFF_YCBCR;
                    break;
                default:
                    imageType = TIFF_GENERIC;
                    break;
            }
            if (imageType == TIFF_GENERIC) {
                numExtraSamples = numBands - 1;
            } else if (numBands > 1) {
                numExtraSamples = numBands - colorSpace.getNumComponents();
            }
            if (numExtraSamples == 1 && colorModel.hasAlpha()) {
                extraSampleType = colorModel.isAlphaPremultiplied() ? EXTRA_SAMPLE_ASSOCIATED_ALPHA : EXTRA_SAMPLE_UNASSOCIATED_ALPHA;
            }
        }
        if (imageType == TIFF_UNSUPPORTED) {
            throw new Error("TIFFImageEncoder8");
        }
        if (compression == COMP_JPEG_TTN2) {
            if (imageType == TIFF_PALETTE) {
                throw new Error("TIFFImageEncoder11");
            } else if (!(sampleSize[0] == 8 && (imageType == TIFF_GRAY || imageType == TIFF_RGB || imageType == TIFF_YCBCR))) {
                throw new Error("TIFFImageEncoder9");
            }
        }
        int photometricInterpretation = -1;
        switch(imageType) {
            case TIFF_BILEVEL_WHITE_IS_ZERO:
                photometricInterpretation = 0;
                break;
            case TIFF_BILEVEL_BLACK_IS_ZERO:
                photometricInterpretation = 1;
                break;
            case TIFF_GRAY:
            case TIFF_GENERIC:
                photometricInterpretation = 1;
                break;
            case TIFF_PALETTE:
                photometricInterpretation = 3;
                icm = (IndexColorModel) colorModel;
                sizeOfColormap = icm.getMapSize();
                byte r[] = new byte[sizeOfColormap];
                icm.getReds(r);
                byte g[] = new byte[sizeOfColormap];
                icm.getGreens(g);
                byte b[] = new byte[sizeOfColormap];
                icm.getBlues(b);
                int redIndex = 0, greenIndex = sizeOfColormap;
                int blueIndex = 2 * sizeOfColormap;
                colormap = new char[sizeOfColormap * 3];
                for (int i = 0; i < sizeOfColormap; i++) {
                    colormap[redIndex++] = (char) (((r[i] << 8) | r[i]) & 0xffff);
                    colormap[greenIndex++] = (char) (((g[i] << 8) | g[i]) & 0xffff);
                    colormap[blueIndex++] = (char) (((b[i] << 8) | b[i]) & 0xffff);
                }
                sizeOfColormap *= 3;
                break;
            case TIFF_RGB:
                photometricInterpretation = 2;
                break;
            case TIFF_CMYK:
                photometricInterpretation = 5;
                break;
            case TIFF_YCBCR:
                photometricInterpretation = 6;
                break;
            case TIFF_CIELAB:
                photometricInterpretation = 8;
                break;
            default:
                throw new Error("TIFFImageEncoder8");
        }
        int tileWidth;
        int tileHeight;
        if (isTiled) {
            tileWidth = encodeParam.getTileWidth() > 0 ? encodeParam.getTileWidth() : im.getTileWidth();
            tileHeight = encodeParam.getTileHeight() > 0 ? encodeParam.getTileHeight() : im.getTileHeight();
        } else {
            tileWidth = width;
            tileHeight = encodeParam.getTileHeight() > 0 ? encodeParam.getTileHeight() : DEFAULT_ROWS_PER_STRIP;
        }
        JPEGEncodeParam jep = null;
        if (compression == COMP_JPEG_TTN2) {
            jep = encodeParam.getJPEGEncodeParam();
            int maxSubH = jep.getHorizontalSubsampling(0);
            int maxSubV = jep.getVerticalSubsampling(0);
            for (int i = 1; i < numBands; i++) {
                int subH = jep.getHorizontalSubsampling(i);
                if (subH > maxSubH) {
                    maxSubH = subH;
                }
                int subV = jep.getVerticalSubsampling(i);
                if (subV > maxSubV) {
                    maxSubV = subV;
                }
            }
            int factorV = 8 * maxSubV;
            tileHeight = (int) ((float) tileHeight / (float) factorV + 0.5F) * factorV;
            if (tileHeight < factorV) {
                tileHeight = factorV;
            }
            if (isTiled) {
                int factorH = 8 * maxSubH;
                tileWidth = (int) ((float) tileWidth / (float) factorH + 0.5F) * factorH;
                if (tileWidth < factorH) {
                    tileWidth = factorH;
                }
            }
        }
        int numTiles;
        if (isTiled) {
            numTiles = ((width + tileWidth - 1) / tileWidth) * ((height + tileHeight - 1) / tileHeight);
        } else {
            numTiles = (int) Math.ceil((double) height / (double) tileHeight);
        }
        long tileByteCounts[] = new long[numTiles];
        long bytesPerRow = (long) Math.ceil((sampleSize[0] / 8.0) * tileWidth * numBands);
        long bytesPerTile = bytesPerRow * tileHeight;
        for (int i = 0; i < numTiles; i++) {
            tileByteCounts[i] = bytesPerTile;
        }
        if (!isTiled) {
            long lastStripRows = height - (tileHeight * (numTiles - 1));
            tileByteCounts[numTiles - 1] = lastStripRows * bytesPerRow;
        }
        long totalBytesOfData = bytesPerTile * (numTiles - 1) + tileByteCounts[numTiles - 1];
        long tileOffsets[] = new long[numTiles];
        SortedSet fields = new TreeSet();
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_IMAGE_WIDTH, TIFFField.TIFF_LONG, 1, new long[] { width }));
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_IMAGE_LENGTH, TIFFField.TIFF_LONG, 1, new long[] { height }));
        char[] shortSampleSize = new char[numBands];
        for (int i = 0; i < numBands; i++) shortSampleSize[i] = (char) sampleSize[i];
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_BITS_PER_SAMPLE, TIFFField.TIFF_SHORT, numBands, shortSampleSize));
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_COMPRESSION, TIFFField.TIFF_SHORT, 1, new char[] { (char) compression }));
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_PHOTOMETRIC_INTERPRETATION, TIFFField.TIFF_SHORT, 1, new char[] { (char) photometricInterpretation }));
        if (!isTiled) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_STRIP_OFFSETS, TIFFField.TIFF_LONG, numTiles, tileOffsets));
        }
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_SAMPLES_PER_PIXEL, TIFFField.TIFF_SHORT, 1, new char[] { (char) numBands }));
        if (!isTiled) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_ROWS_PER_STRIP, TIFFField.TIFF_LONG, 1, new long[] { tileHeight }));
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_STRIP_BYTE_COUNTS, TIFFField.TIFF_LONG, numTiles, tileByteCounts));
        }
        if (colormap != null) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_COLORMAP, TIFFField.TIFF_SHORT, sizeOfColormap, colormap));
        }
        if (isTiled) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_WIDTH, TIFFField.TIFF_LONG, 1, new long[] { tileWidth }));
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_LENGTH, TIFFField.TIFF_LONG, 1, new long[] { tileHeight }));
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_OFFSETS, TIFFField.TIFF_LONG, numTiles, tileOffsets));
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_BYTE_COUNTS, TIFFField.TIFF_LONG, numTiles, tileByteCounts));
        }
        if (numExtraSamples > 0) {
            char[] extraSamples = new char[numExtraSamples];
            for (int i = 0; i < numExtraSamples; i++) {
                extraSamples[i] = (char) extraSampleType;
            }
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_EXTRA_SAMPLES, TIFFField.TIFF_SHORT, numExtraSamples, extraSamples));
        }
        if (dataType != DataBuffer.TYPE_BYTE) {
            char[] sampleFormat = new char[numBands];
            if (dataType == DataBuffer.TYPE_FLOAT) {
                sampleFormat[0] = 3;
            } else if (dataType == DataBuffer.TYPE_USHORT) {
                sampleFormat[0] = 1;
            } else {
                sampleFormat[0] = 2;
            }
            for (int b = 1; b < numBands; b++) {
                sampleFormat[b] = sampleFormat[0];
            }
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_SAMPLE_FORMAT, TIFFField.TIFF_SHORT, numBands, sampleFormat));
        }
        com.sun.image.codec.jpeg.JPEGEncodeParam jpegEncodeParam = null;
        com.sun.image.codec.jpeg.JPEGImageEncoder jpegEncoder = null;
        int jpegColorID = 0;
        if (compression == COMP_JPEG_TTN2) {
            jpegColorID = com.sun.image.codec.jpeg.JPEGDecodeParam.COLOR_ID_UNKNOWN;
            switch(imageType) {
                case TIFF_GRAY:
                case TIFF_PALETTE:
                    jpegColorID = com.sun.image.codec.jpeg.JPEGDecodeParam.COLOR_ID_GRAY;
                    break;
                case TIFF_RGB:
                    jpegColorID = com.sun.image.codec.jpeg.JPEGDecodeParam.COLOR_ID_RGB;
                    break;
                case TIFF_YCBCR:
                    jpegColorID = com.sun.image.codec.jpeg.JPEGDecodeParam.COLOR_ID_YCbCr;
                    break;
            }
            Raster tile00 = im.getTile(0, 0);
            jpegEncodeParam = com.sun.image.codec.jpeg.JPEGCodec.getDefaultJPEGEncodeParam(tile00, jpegColorID);
            modifyEncodeParam(jep, jpegEncodeParam, numBands);
            jpegEncodeParam.setImageInfoValid(false);
            jpegEncodeParam.setTableInfoValid(true);
            ByteArrayOutputStream tableStream = new ByteArrayOutputStream();
            jpegEncoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(tableStream, jpegEncodeParam);
            jpegEncoder.encode(tile00);
            byte[] tableData = tableStream.toByteArray();
            fields.add(new TIFFField(TIFF_JPEG_TABLES, TIFFField.TIFF_UNDEFINED, tableData.length, tableData));
            jpegEncoder = null;
        }
        if (imageType == TIFF_YCBCR) {
            char subsampleH = 1;
            char subsampleV = 1;
            if (compression == COMP_JPEG_TTN2) {
                subsampleH = (char) jep.getHorizontalSubsampling(0);
                subsampleV = (char) jep.getVerticalSubsampling(0);
                for (int i = 1; i < numBands; i++) {
                    char subH = (char) jep.getHorizontalSubsampling(i);
                    if (subH > subsampleH) {
                        subsampleH = subH;
                    }
                    char subV = (char) jep.getVerticalSubsampling(i);
                    if (subV > subsampleV) {
                        subsampleV = subV;
                    }
                }
            }
            fields.add(new TIFFField(TIFF_YCBCR_SUBSAMPLING, TIFFField.TIFF_SHORT, 2, new char[] { subsampleH, subsampleV }));
            fields.add(new TIFFField(TIFF_YCBCR_POSITIONING, TIFFField.TIFF_SHORT, 1, new int[] { compression == COMP_JPEG_TTN2 ? 1 : 2 }));
            long[][] refbw;
            if (compression == COMP_JPEG_TTN2) {
                refbw = new long[][] { { 0, 1 }, { 255, 1 }, { 128, 1 }, { 255, 1 }, { 128, 1 }, { 255, 1 } };
            } else {
                refbw = new long[][] { { 15, 1 }, { 235, 1 }, { 128, 1 }, { 240, 1 }, { 128, 1 }, { 240, 1 } };
            }
            fields.add(new TIFFField(TIFF_REF_BLACK_WHITE, TIFFField.TIFF_RATIONAL, 6, refbw));
        }
        TIFFField[] extraFields = encodeParam.getExtraFields();
        if (extraFields != null) {
            ArrayList extantTags = new ArrayList(fields.size());
            Iterator fieldIter = fields.iterator();
            while (fieldIter.hasNext()) {
                TIFFField fld = (TIFFField) fieldIter.next();
                extantTags.add(new Integer(fld.getTag()));
            }
            int numExtraFields = extraFields.length;
            for (int i = 0; i < numExtraFields; i++) {
                TIFFField fld = extraFields[i];
                Integer tagValue = new Integer(fld.getTag());
                if (!extantTags.contains(tagValue)) {
                    fields.add(fld);
                    extantTags.add(tagValue);
                }
            }
        }
        int dirSize = getDirectorySize(fields);
        tileOffsets[0] = ifdOffset + dirSize;
        OutputStream outCache = null;
        byte[] compressBuf = null;
        File tempFile = null;
        int nextIFDOffset = 0;
        boolean skipByte = false;
        Deflater deflater = null;
        boolean jpegRGBToYCbCr = false;
        if (compression == COMP_NONE) {
            int numBytesPadding = 0;
            if (sampleSize[0] == 16 && tileOffsets[0] % 2 != 0) {
                numBytesPadding = 1;
                tileOffsets[0]++;
            } else if (sampleSize[0] == 32 && tileOffsets[0] % 4 != 0) {
                numBytesPadding = (int) (4 - tileOffsets[0] % 4);
                tileOffsets[0] += numBytesPadding;
            }
            for (int i = 1; i < numTiles; i++) {
                tileOffsets[i] = tileOffsets[i - 1] + tileByteCounts[i - 1];
            }
            if (!isLast) {
                nextIFDOffset = (int) (tileOffsets[0] + totalBytesOfData);
                if ((nextIFDOffset & 0x01) != 0) {
                    nextIFDOffset++;
                    skipByte = true;
                }
            }
            writeDirectory(ifdOffset, fields, nextIFDOffset);
            if (numBytesPadding != 0) {
                for (int padding = 0; padding < numBytesPadding; padding++) {
                    output.write((byte) 0);
                }
            }
        } else {
            if ((output instanceof SeekableOutputStream)) {
                ((SeekableOutputStream) output).seek(tileOffsets[0]);
            } else {
                outCache = output;
                try {
                    tempFile = File.createTempFile("jai-SOS-", ".tmp");
                    tempFile.deleteOnExit();
                    RandomAccessFile raFile = new RandomAccessFile(tempFile, "rw");
                    output = new SeekableOutputStream(raFile);
                } catch (Exception e) {
                    output = new ByteArrayOutputStream((int) totalBytesOfData);
                }
            }
            int bufSize = 0;
            switch(compression) {
                case COMP_PACKBITS:
                    bufSize = (int) (bytesPerTile + ((bytesPerRow + 127) / 128) * tileHeight);
                    break;
                case COMP_JPEG_TTN2:
                    bufSize = 0;
                    if (imageType == TIFF_YCBCR && colorModel != null && colorModel.getColorSpace().getType() == ColorSpace.TYPE_RGB) {
                        jpegRGBToYCbCr = true;
                    }
                case COMP_DEFLATE:
                    bufSize = (int) bytesPerTile;
                    deflater = new Deflater(encodeParam.getDeflateLevel());
                    break;
                default:
                    bufSize = 0;
            }
            if (bufSize != 0) {
                compressBuf = new byte[bufSize];
            }
        }
        int[] pixels = null;
        float[] fpixels = null;
        boolean checkContiguous = ((sampleSize[0] == 1 && sampleModel instanceof MultiPixelPackedSampleModel && dataType == DataBuffer.TYPE_BYTE) || (sampleSize[0] == 8 && sampleModel instanceof ComponentSampleModel));
        byte[] bpixels = null;
        if (compression != COMP_JPEG_TTN2) {
            if (dataType == DataBuffer.TYPE_BYTE) {
                bpixels = new byte[tileHeight * tileWidth * numBands];
            } else if (dataTypeIsShort) {
                bpixels = new byte[2 * tileHeight * tileWidth * numBands];
            } else if (dataType == DataBuffer.TYPE_INT || dataType == DataBuffer.TYPE_FLOAT) {
                bpixels = new byte[4 * tileHeight * tileWidth * numBands];
            }
        }
        int lastRow = minY + height;
        int lastCol = minX + width;
        int tileNum = 0;
        for (int row = minY; row < lastRow; row += tileHeight) {
            int rows = isTiled ? tileHeight : Math.min(tileHeight, lastRow - row);
            int size = rows * tileWidth * numBands;
            for (int col = minX; col < lastCol; col += tileWidth) {
                Raster src = im.getData(new Rectangle(col, row, tileWidth, rows));
                boolean useDataBuffer = false;
                if (compression != COMP_JPEG_TTN2) {
                    if (checkContiguous) {
                        if (sampleSize[0] == 8) {
                            ComponentSampleModel csm = (ComponentSampleModel) src.getSampleModel();
                            int[] bankIndices = csm.getBankIndices();
                            int[] bandOffsets = csm.getBandOffsets();
                            int pixelStride = csm.getPixelStride();
                            int lineStride = csm.getScanlineStride();
                            if (pixelStride != numBands || lineStride != bytesPerRow) {
                                useDataBuffer = false;
                            } else {
                                useDataBuffer = true;
                                for (int i = 0; useDataBuffer && i < numBands; i++) {
                                    if (bankIndices[i] != 0 || bandOffsets[i] != i) {
                                        useDataBuffer = false;
                                    }
                                }
                            }
                        } else {
                            MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel) src.getSampleModel();
                            if (mpp.getNumBands() == 1 && mpp.getDataBitOffset() == 0 && mpp.getPixelBitStride() == 1) {
                                useDataBuffer = true;
                            }
                        }
                    }
                    if (!useDataBuffer) {
                        if (dataType == DataBuffer.TYPE_FLOAT) {
                            fpixels = src.getPixels(col, row, tileWidth, rows, fpixels);
                        } else {
                            pixels = src.getPixels(col, row, tileWidth, rows, pixels);
                        }
                    }
                }
                int index;
                int pixel = 0;
                int k = 0;
                switch(sampleSize[0]) {
                    case 1:
                        if (useDataBuffer) {
                            byte[] btmp = ((DataBufferByte) src.getDataBuffer()).getData();
                            MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel) src.getSampleModel();
                            int lineStride = mpp.getScanlineStride();
                            int inOffset = mpp.getOffset(col - src.getSampleModelTranslateX(), row - src.getSampleModelTranslateY());
                            if (lineStride == (int) bytesPerRow) {
                                System.arraycopy(btmp, inOffset, bpixels, 0, (int) bytesPerRow * rows);
                            } else {
                                int outOffset = 0;
                                for (int j = 0; j < rows; j++) {
                                    System.arraycopy(btmp, inOffset, bpixels, outOffset, (int) bytesPerRow);
                                    inOffset += lineStride;
                                    outOffset += (int) bytesPerRow;
                                }
                            }
                        } else {
                            index = 0;
                            for (int i = 0; i < rows; i++) {
                                for (int j = 0; j < tileWidth / 8; j++) {
                                    pixel = (pixels[index++] << 7) | (pixels[index++] << 6) | (pixels[index++] << 5) | (pixels[index++] << 4) | (pixels[index++] << 3) | (pixels[index++] << 2) | (pixels[index++] << 1) | pixels[index++];
                                    bpixels[k++] = (byte) pixel;
                                }
                                if (tileWidth % 8 > 0) {
                                    pixel = 0;
                                    for (int j = 0; j < tileWidth % 8; j++) {
                                        pixel |= (pixels[index++] << (7 - j));
                                    }
                                    bpixels[k++] = (byte) pixel;
                                }
                            }
                        }
                        if (compression == COMP_NONE) {
                            output.write(bpixels, 0, rows * ((tileWidth + 7) / 8));
                        } else if (compression == COMP_PACKBITS) {
                            int numCompressedBytes = compressPackBits(bpixels, rows, (int) bytesPerRow, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == COMP_DEFLATE) {
                            int numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                    case 4:
                        index = 0;
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < tileWidth / 2; j++) {
                                pixel = (pixels[index++] << 4) | pixels[index++];
                                bpixels[k++] = (byte) pixel;
                            }
                            if ((tileWidth % 2) == 1) {
                                pixel = pixels[index++] << 4;
                                bpixels[k++] = (byte) pixel;
                            }
                        }
                        if (compression == COMP_NONE) {
                            output.write(bpixels, 0, rows * ((tileWidth + 1) / 2));
                        } else if (compression == COMP_PACKBITS) {
                            int numCompressedBytes = compressPackBits(bpixels, rows, (int) bytesPerRow, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == COMP_DEFLATE) {
                            int numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                    case 8:
                        if (compression != COMP_JPEG_TTN2) {
                            if (useDataBuffer) {
                                byte[] btmp = ((DataBufferByte) src.getDataBuffer()).getData();
                                ComponentSampleModel csm = (ComponentSampleModel) src.getSampleModel();
                                int inOffset = csm.getOffset(col - src.getSampleModelTranslateX(), row - src.getSampleModelTranslateY());
                                int lineStride = csm.getScanlineStride();
                                if (lineStride == (int) bytesPerRow) {
                                    System.arraycopy(btmp, inOffset, bpixels, 0, (int) bytesPerRow * rows);
                                } else {
                                    int outOffset = 0;
                                    for (int j = 0; j < rows; j++) {
                                        System.arraycopy(btmp, inOffset, bpixels, outOffset, (int) bytesPerRow);
                                        inOffset += lineStride;
                                        outOffset += (int) bytesPerRow;
                                    }
                                }
                            } else {
                                for (int i = 0; i < size; i++) {
                                    bpixels[i] = (byte) pixels[i];
                                }
                            }
                        }
                        if (compression == COMP_NONE) {
                            output.write(bpixels, 0, size);
                        } else if (compression == COMP_PACKBITS) {
                            int numCompressedBytes = compressPackBits(bpixels, rows, (int) bytesPerRow, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == COMP_JPEG_TTN2) {
                            long startPos = getOffset(output);
                            if (jpegEncoder == null || jpegEncodeParam.getWidth() != src.getWidth() || jpegEncodeParam.getHeight() != src.getHeight()) {
                                jpegEncodeParam = com.sun.image.codec.jpeg.JPEGCodec.getDefaultJPEGEncodeParam(src, jpegColorID);
                                modifyEncodeParam(jep, jpegEncodeParam, numBands);
                                jpegEncoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(output, jpegEncodeParam);
                            }
                            if (jpegRGBToYCbCr) {
                                WritableRaster wRas = null;
                                if (src instanceof WritableRaster) {
                                    wRas = (WritableRaster) src;
                                } else {
                                    wRas = src.createCompatibleWritableRaster();
                                    wRas.setRect(src);
                                }
                                if (wRas.getMinX() != 0 || wRas.getMinY() != 0) {
                                    wRas = wRas.createWritableTranslatedChild(0, 0);
                                }
                                BufferedImage bi = new BufferedImage(colorModel, wRas, false, null);
                                jpegEncoder.encode(bi);
                            } else {
                                jpegEncoder.encode(src.createTranslatedChild(0, 0));
                            }
                            long endPos = getOffset(output);
                            tileByteCounts[tileNum++] = (int) (endPos - startPos);
                        } else if (compression == COMP_DEFLATE) {
                            int numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                    case 16:
                        int ls = 0;
                        for (int i = 0; i < size; i++) {
                            short value = (short) pixels[i];
                            bpixels[ls++] = (byte) ((value & 0xff00) >> 8);
                            bpixels[ls++] = (byte) (value & 0x00ff);
                        }
                        if (compression == COMP_NONE) {
                            output.write(bpixels, 0, size * 2);
                        } else if (compression == COMP_PACKBITS) {
                            int numCompressedBytes = compressPackBits(bpixels, rows, (int) bytesPerRow, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == COMP_DEFLATE) {
                            int numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                    case 32:
                        if (dataType == DataBuffer.TYPE_INT) {
                            int li = 0;
                            for (int i = 0; i < size; i++) {
                                int value = pixels[i];
                                bpixels[li++] = (byte) ((value & 0xff000000) >> 24);
                                bpixels[li++] = (byte) ((value & 0x00ff0000) >> 16);
                                bpixels[li++] = (byte) ((value & 0x0000ff00) >> 8);
                                bpixels[li++] = (byte) (value & 0x000000ff);
                            }
                        } else {
                            int lf = 0;
                            for (int i = 0; i < size; i++) {
                                int value = Float.floatToIntBits(fpixels[i]);
                                bpixels[lf++] = (byte) ((value & 0xff000000) >> 24);
                                bpixels[lf++] = (byte) ((value & 0x00ff0000) >> 16);
                                bpixels[lf++] = (byte) ((value & 0x0000ff00) >> 8);
                                bpixels[lf++] = (byte) (value & 0x000000ff);
                            }
                        }
                        if (compression == COMP_NONE) {
                            output.write(bpixels, 0, size * 4);
                        } else if (compression == COMP_PACKBITS) {
                            int numCompressedBytes = compressPackBits(bpixels, rows, (int) bytesPerRow, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == COMP_DEFLATE) {
                            int numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                            tileByteCounts[tileNum++] = numCompressedBytes;
                            output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                }
            }
        }
        if (compression == COMP_NONE) {
            if (skipByte) {
                output.write((byte) 0);
            }
        } else {
            int totalBytes = 0;
            for (int i = 1; i < numTiles; i++) {
                int numBytes = (int) tileByteCounts[i - 1];
                totalBytes += numBytes;
                tileOffsets[i] = tileOffsets[i - 1] + numBytes;
            }
            totalBytes += (int) tileByteCounts[numTiles - 1];
            nextIFDOffset = isLast ? 0 : ifdOffset + dirSize + totalBytes;
            if ((nextIFDOffset & 0x01) != 0) {
                nextIFDOffset++;
                skipByte = true;
            }
            if (outCache == null) {
                if (skipByte) {
                    output.write((byte) 0);
                }
                SeekableOutputStream sos = (SeekableOutputStream) output;
                long savePos = sos.getFilePointer();
                sos.seek(ifdOffset);
                writeDirectory(ifdOffset, fields, nextIFDOffset);
                sos.seek(savePos);
            } else if (tempFile != null) {
                FileInputStream fileStream = new FileInputStream(tempFile);
                output.close();
                output = outCache;
                writeDirectory(ifdOffset, fields, nextIFDOffset);
                byte[] copyBuffer = new byte[8192];
                int bytesCopied = 0;
                while (bytesCopied < totalBytes) {
                    int bytesRead = fileStream.read(copyBuffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    output.write(copyBuffer, 0, bytesRead);
                    bytesCopied += bytesRead;
                }
                fileStream.close();
                tempFile.delete();
                if (skipByte) {
                    output.write((byte) 0);
                }
            } else if (output instanceof ByteArrayOutputStream) {
                ByteArrayOutputStream memoryStream = (ByteArrayOutputStream) output;
                output = outCache;
                writeDirectory(ifdOffset, fields, nextIFDOffset);
                memoryStream.writeTo(output);
                if (skipByte) {
                    output.write((byte) 0);
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return nextIFDOffset;
    }

    /**
     * Calculates the size of the IFD.
     */
    private int getDirectorySize(SortedSet fields) {
        int numEntries = fields.size();
        int dirSize = 2 + numEntries * 12 + 4;
        Iterator iter = fields.iterator();
        while (iter.hasNext()) {
            TIFFField field = (TIFFField) iter.next();
            int valueSize = field.getCount() * sizeOfType[field.getType()];
            if (valueSize > 4) {
                dirSize += valueSize;
            }
        }
        return dirSize;
    }

    private void writeFileHeader() throws IOException {
        output.write('M');
        output.write('M');
        output.write(0);
        output.write(42);
        writeLong(8);
    }

    private void writeDirectory(int thisIFDOffset, SortedSet fields, int nextIFDOffset) throws IOException {
        int numEntries = fields.size();
        long offsetBeyondIFD = thisIFDOffset + 12 * numEntries + 4 + 2;
        ArrayList tooBig = new ArrayList();
        writeUnsignedShort(numEntries);
        Iterator iter = fields.iterator();
        while (iter.hasNext()) {
            TIFFField field = (TIFFField) iter.next();
            int tag = field.getTag();
            writeUnsignedShort(tag);
            int type = field.getType();
            writeUnsignedShort(type);
            int count = field.getCount();
            int valueSize = getValueSize(field);
            writeLong(type == TIFFField.TIFF_ASCII ? valueSize : count);
            if (valueSize > 4) {
                writeLong(offsetBeyondIFD);
                offsetBeyondIFD += valueSize;
                tooBig.add(field);
            } else {
                writeValuesAsFourBytes(field);
            }
        }
        writeLong(nextIFDOffset);
        for (int i = 0; i < tooBig.size(); i++) {
            writeValues((TIFFField) tooBig.get(i));
        }
    }

    /**
     * Determine the number of bytes in the value portion of the field.
     */
    private static final int getValueSize(TIFFField field) {
        int type = field.getType();
        int count = field.getCount();
        int valueSize = 0;
        if (type == TIFFField.TIFF_ASCII) {
            for (int i = 0; i < count; i++) {
                byte[] stringBytes = field.getAsString(i).getBytes();
                valueSize += stringBytes.length;
                if (stringBytes[stringBytes.length - 1] != (byte) 0) {
                    valueSize++;
                }
            }
        } else {
            valueSize = count * sizeOfType[type];
        }
        return valueSize;
    }

    private static final int[] sizeOfType = { 0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8 };

    private void writeValuesAsFourBytes(TIFFField field) throws IOException {
        int dataType = field.getType();
        int count = field.getCount();
        switch(dataType) {
            case TIFFField.TIFF_BYTE:
                byte bytes[] = field.getAsBytes();
                if (count > 4) count = 4;
                for (int i = 0; i < count; i++) output.write(bytes[i]);
                for (int i = 0; i < (4 - count); i++) output.write(0);
                break;
            case TIFFField.TIFF_SHORT:
                char chars[] = field.getAsChars();
                if (count > 2) count = 2;
                for (int i = 0; i < count; i++) writeUnsignedShort(chars[i]);
                for (int i = 0; i < (2 - count); i++) writeUnsignedShort(0);
                break;
            case TIFFField.TIFF_LONG:
                long longs[] = field.getAsLongs();
                for (int i = 0; i < count; i++) {
                    writeLong(longs[i]);
                }
                break;
        }
    }

    private void writeValues(TIFFField field) throws IOException {
        int dataType = field.getType();
        int count = field.getCount();
        switch(dataType) {
            case TIFFField.TIFF_BYTE:
            case TIFFField.TIFF_SBYTE:
            case TIFFField.TIFF_UNDEFINED:
                byte bytes[] = field.getAsBytes();
                for (int i = 0; i < count; i++) {
                    output.write(bytes[i]);
                }
                break;
            case TIFFField.TIFF_SHORT:
                char chars[] = field.getAsChars();
                for (int i = 0; i < count; i++) {
                    writeUnsignedShort(chars[i]);
                }
                break;
            case TIFFField.TIFF_SSHORT:
                short shorts[] = field.getAsShorts();
                for (int i = 0; i < count; i++) {
                    writeUnsignedShort(shorts[i]);
                }
                break;
            case TIFFField.TIFF_LONG:
            case TIFFField.TIFF_SLONG:
                long longs[] = field.getAsLongs();
                for (int i = 0; i < count; i++) {
                    writeLong(longs[i]);
                }
                break;
            case TIFFField.TIFF_FLOAT:
                float[] floats = field.getAsFloats();
                for (int i = 0; i < count; i++) {
                    int intBits = Float.floatToIntBits(floats[i]);
                    writeLong(intBits);
                }
                break;
            case TIFFField.TIFF_DOUBLE:
                double[] doubles = field.getAsDoubles();
                for (int i = 0; i < count; i++) {
                    long longBits = Double.doubleToLongBits(doubles[i]);
                    writeLong(longBits >>> 32);
                    writeLong(longBits & 0xffffffff);
                }
                break;
            case TIFFField.TIFF_RATIONAL:
            case TIFFField.TIFF_SRATIONAL:
                long rationals[][] = field.getAsRationals();
                for (int i = 0; i < count; i++) {
                    writeLong(rationals[i][0]);
                    writeLong(rationals[i][1]);
                }
                break;
            case TIFFField.TIFF_ASCII:
                for (int i = 0; i < count; i++) {
                    byte[] stringBytes = field.getAsString(i).getBytes();
                    output.write(stringBytes);
                    if (stringBytes[stringBytes.length - 1] != (byte) 0) {
                        output.write((byte) 0);
                    }
                }
                break;
            default:
                throw new Error("TIFFImageEncoder10");
        }
    }

    private void writeUnsignedShort(int s) throws IOException {
        output.write((s & 0xff00) >>> 8);
        output.write(s & 0x00ff);
    }

    private void writeLong(long l) throws IOException {
        output.write((int) ((l & 0xff000000) >>> 24));
        output.write((int) ((l & 0x00ff0000) >>> 16));
        output.write((int) ((l & 0x0000ff00) >>> 8));
        output.write(((int) l & 0x000000ff));
    }

    /**
     * Returns the current offset in the supplied OutputStream.
     * This method should only be used if compressing data.
     */
    private long getOffset(OutputStream out) throws IOException {
        if (out instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) out).size();
        } else if (out instanceof SeekableOutputStream) {
            return ((SeekableOutputStream) out).getFilePointer();
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Performs PackBits compression on a tile of data.
     */
    private static int compressPackBits(byte[] data, int numRows, int bytesPerRow, byte[] compData) {
        int inOffset = 0;
        int outOffset = 0;
        for (int i = 0; i < numRows; i++) {
            outOffset = packBits(data, inOffset, bytesPerRow, compData, outOffset);
            inOffset += bytesPerRow;
        }
        return outOffset;
    }

    /**
     * Performs PackBits compression for a single buffer of data.
     * This should be called for each row of each tile. The returned
     * value is the offset into the output buffer after compression.
     */
    private static int packBits(byte[] input, int inOffset, int inCount, byte[] output, int outOffset) {
        int inMax = inOffset + inCount - 1;
        int inMaxMinus1 = inMax - 1;
        while (inOffset <= inMax) {
            int run = 1;
            byte replicate = input[inOffset];
            while (run < 127 && inOffset < inMax && input[inOffset] == input[inOffset + 1]) {
                run++;
                inOffset++;
            }
            if (run > 1) {
                inOffset++;
                output[outOffset++] = (byte) (-(run - 1));
                output[outOffset++] = replicate;
            }
            run = 0;
            int saveOffset = outOffset;
            while (run < 128 && ((inOffset < inMax && input[inOffset] != input[inOffset + 1]) || (inOffset < inMaxMinus1 && input[inOffset] != input[inOffset + 2]))) {
                run++;
                output[++outOffset] = input[inOffset++];
            }
            if (run > 0) {
                output[saveOffset] = (byte) (run - 1);
                outOffset++;
            }
            if (inOffset == inMax) {
                if (run > 0 && run < 128) {
                    output[saveOffset]++;
                    output[outOffset++] = input[inOffset++];
                } else {
                    output[outOffset++] = (byte) 0;
                    output[outOffset++] = input[inOffset++];
                }
            }
        }
        return outOffset;
    }

    private static int deflate(Deflater deflater, byte[] inflated, byte[] deflated) {
        deflater.setInput(inflated);
        deflater.finish();
        int numCompressedBytes = deflater.deflate(deflated);
        deflater.reset();
        return numCompressedBytes;
    }

    private static void modifyEncodeParam(JPEGEncodeParam src, JPEGEncodeParam dst, int nbands) {
        dst.setDensityUnit(src.getDensityUnit());
        dst.setXDensity(src.getXDensity());
        dst.setYDensity(src.getYDensity());
        dst.setRestartInterval(src.getRestartInterval());
        for (int i = 0; i < 4; i++) {
            JPEGQTable tbl = src.getQTable(i);
            if (tbl != null) dst.setQTable(i, tbl);
        }
    }
}
