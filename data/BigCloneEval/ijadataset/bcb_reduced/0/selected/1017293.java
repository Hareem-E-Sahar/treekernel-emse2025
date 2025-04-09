package org.xebra.dcm.io.adaptors;

import java.awt.image.DataBuffer;
import java.io.IOException;
import org.dcm4che2.data.Tag;
import org.xebra.dcm.exception.DCMParsingException;
import org.xebra.dcm.util.ValueRange;

/**
 * The <code>RGBColorAdaptor</code> is used to convert DICOM Files with having a 
 * RGB Color Photometric Interpretation.
 * 
 * @author Rafael Chargel
 * @version $Revision: 1.21 $
 */
public class RGBColorAdaptor extends ImageAdaptor {

    /**
     * Constructor for <code>RGBColorAdaptor</code>.
     *
     */
    RGBColorAdaptor() {
        super();
    }

    private int[] getDataFromShort() throws IOException {
        int[] data = new int[super.pixelGroupLength];
        short[] pixels = getAttrs().getShorts(Tag.PixelData);
        int index = super.imageIndex * super.pixelGroupLength;
        if (pixels.length < (index + data.length)) {
            return getDataFromByte();
        }
        arrayCopy(pixels, index, data, 0, data.length);
        return data;
    }

    private int[] getDataFromByte() throws IOException {
        int[] data = new int[super.pixelGroupLength];
        byte[] pixels = getAttrs().getBytes(Tag.PixelData);
        int index = super.imageIndex * super.pixelGroupLength;
        if (pixels.length < (index + data.length)) {
            throw new IOException("Found " + pixels.length + " but requested pixels " + index + " through " + (index + super.pixelGroupLength));
        }
        arrayCopy(pixels, index, data, 0, data.length);
        return data;
    }

    /**
     * Finishes the necessary image processing.  The final processing is always
     * specific to the photometric interpretation defined by the DICOM Object.
     *
     * @throws IOException Thrown if there is an error during processing.
     *
     * @see org.xebra.dcm.io.adaptors.ImageAdaptor#finishSpecificProcessing()
     */
    protected void finishSpecificProcessing() throws IOException {
        int[] data = new int[super.pixelGroupLength];
        try {
            if (super.raster.getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
                data = getDataFromByte();
            } else {
                data = getDataFromShort();
            }
        } catch (DCMParsingException exc) {
            throw new IOException("Could not capture pixel data", exc);
        }
        if (data == null) throw new IOException("Could not capture pixel data");
        try {
            int imgBits = DataBuffer.getDataTypeSize(super.img.getSampleModel().getDataType());
            boolean needsRescale = imgBits < super.depth;
            ValueRange srcRange = null;
            ValueRange dstRange = null;
            try {
                if (super.signed) {
                    srcRange = new ValueRange((int) (Math.pow(2, super.depth - 1) - 1) * -1, (int) Math.pow(2, super.depth - 1));
                } else {
                    srcRange = new ValueRange(0, (int) Math.pow(2, super.depth) - 1);
                }
                dstRange = new ValueRange(0, (int) Math.pow(2, imgBits) - 1);
            } catch (Throwable t) {
                throw new IOException("Could not set src/dst ranges", t);
            }
            if (super.srcBands == null) {
                super.srcBands = new int[3];
                super.srcBands[0] = 0;
                super.srcBands[1] = 1;
                super.srcBands[2] = 2;
            }
            if (super.dstBands != null) {
                try {
                    super.raster = super.raster.createWritableChild(super.raster.getMinX(), super.raster.getMinY(), super.raster.getWidth(), super.raster.getHeight(), 0, 0, dstBands);
                } catch (Throwable t) {
                    throw new IOException("Could not create compatible raster", t);
                }
            }
            int[] pixel = new int[super.raster.getNumBands()];
            int dstX = super.dstOffset.x;
            int dstY = super.dstOffset.y;
            boolean byplane = getAttrs().getInt(Tag.PlanarConfiguration, 0) == 1;
            int depth = super.raster.getNumBands();
            int srcPX = (int) (super.srcRegion.x) * depth;
            int tWidth = (int) (super.reader.getWidth(0) * super.raster.getNumDataElements());
            srcPX = tWidth - srcPX;
            if (byplane) {
                int pixDataIndex = 0;
                for (int b = 0; b < super.raster.getNumBands(); b++) {
                    for (int srcY = super.srcRegion.y; dstY < super.raster.getHeight() && srcY < super.srcRegion.getMaxY(); srcY += super.srcYSub) {
                        for (int srcX = super.srcRegion.x; dstX < super.raster.getWidth() && srcX < super.srcRegion.getMaxX(); srcX += super.srcXSub) {
                            try {
                                super.raster.getPixel(dstX, dstY, pixel);
                                pixel[b] = data[pixDataIndex];
                                if (super.signed) {
                                    if ((pixel[b] & super.signBit) != 0) {
                                        pixel[b] *= -1;
                                    }
                                }
                                pixel[b] &= super.mask;
                                if (needsRescale) {
                                    pixel[b] = srcRange.scaleTo(dstRange, pixel[b]);
                                }
                                if (pixel.length > depth) {
                                    throw new ArrayIndexOutOfBoundsException("Pixel depth [" + pixel.length + "] is >= raster depth [" + depth + "]");
                                }
                                super.raster.setPixel(dstX, dstY, pixel);
                                dstX++;
                                pixDataIndex++;
                            } catch (Throwable t) {
                                throw new IOException("Could not process byplane pixel data [pixel = " + b + " : index = " + pixDataIndex + " : source = " + dstX + "/" + dstY + "/" + pixel.length + "]", t);
                            }
                        }
                        dstX = super.dstOffset.x;
                        dstY++;
                    }
                    dstX = super.dstOffset.x;
                    dstY = super.dstOffset.y;
                }
            } else {
                int pixDataIndex = 0;
                for (int srcY = 0; srcY < super.srcRegion.getMaxY() && srcY < super.height; srcY += super.srcYSub) {
                    for (int srcX = 0; srcX < super.srcRegion.getMaxX() && srcX < super.width; srcX += super.srcXSub) {
                        for (int b = 0; b < super.raster.getNumBands(); b++) {
                            if (pixDataIndex == data.length) {
                                throw new IOException("Pixel Index '" + pixDataIndex + "' is too high: Y = " + srcY + "/" + super.height + " : X = " + srcX + "/" + super.width + " : Band = " + b + "/" + super.samples);
                            }
                            int pval = 0;
                            try {
                                pval = data[pixDataIndex];
                            } catch (Throwable t) {
                                throw new IOException("Could not pull pixel from data [length = " + data.length + " : pixel = " + pixDataIndex + "]", t);
                            }
                            try {
                                pixel[b] = pval;
                            } catch (Throwable t) {
                                throw new IOException("Could not add pixel to dest [length = " + pixel.length + " : pixel = " + b + "]", t);
                            }
                            try {
                                if (super.signed) {
                                    if ((pixel[b] & super.signBit) != 0) {
                                        pixel[b] *= -1;
                                    }
                                }
                                pixel[b] &= super.mask;
                                if (pixel[b] < 0) {
                                    System.err.println("Pixel at (" + dstX + "," + dstY + ") still negative after bit logic!");
                                }
                                pixDataIndex++;
                            } catch (Throwable t) {
                                throw new IOException("Could not process pixel data [pixel = " + b + " : index = " + pixDataIndex + " : source = " + dstX + "/" + dstY + "/" + pixel.length + " : data = " + data.length + "]", t);
                            }
                        }
                        try {
                            if (needsRescale) {
                                for (int b = 0; b < pixel.length; b++) {
                                    pixel[b] = srcRange.scaleTo(dstRange, pixel[b]);
                                }
                            }
                        } catch (Throwable t) {
                            throw new IOException("Could not rescale image: [range = " + dstRange.getMax() + "/" + dstRange.getMin() + "]", t);
                        }
                        if (pixel.length > depth) {
                            throw new ArrayIndexOutOfBoundsException("Pixel depth [" + pixel.length + "] is >= raster depth [" + depth + "]");
                        }
                        try {
                            super.raster.setPixel(dstX, dstY, pixel);
                        } catch (Throwable t) {
                            throw new IOException("Error setting pixel data: " + dstX + "/" + dstY + "/" + pixel, t);
                        }
                        dstX++;
                    }
                    dstX = super.dstOffset.x;
                    dstY++;
                }
            }
        } catch (IOException exc) {
            throw exc;
        } catch (Throwable t) {
            throw new IOException("Could not process RGB pixel data", t);
        }
    }

    private static final void arrayCopy(Object src, int srcPos, int[] dst, int dstPos, int length) {
        if (src.getClass().isArray()) {
            if (src.getClass().getComponentType() == short.class) {
                short[] srcArray = (short[]) src;
                for (int i = 0; i < length; i++) {
                    dst[dstPos + i] = srcArray[srcPos + i];
                }
            } else if (src.getClass().getComponentType() == byte.class) {
                byte[] srcArray = (byte[]) src;
                for (int i = 0; i < length; i++) {
                    dst[dstPos + i] = srcArray[srcPos + i];
                }
            }
        }
    }
}
