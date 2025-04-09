package org.occ.hadoop.mapreduce.tiff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import org.apache.hadoop.fs.FSDataInputStream;

public class CustomeTIFFMetaReader {

    protected static final int NUM_PIXEL_CHANNELS = 3;

    protected static final int TAG_IMAGE_WIDTH = 256;

    protected static final int TAG_IMAGE_LENGTH = 257;

    protected static final int TAG_TILE_WIDTH = 322;

    protected static final int TAG_TILE_LENGTH = 323;

    protected static final int TAG_TILE_OFFSET = 324;

    protected static final int TAG_MODEL_TIE_POINT_TAG = 33922;

    protected static final int TAG_MODEL_PIXEL_SCALE_TAG = 33550;

    protected static final int HEADER_ENTRY_COUNT_SIZE = 2;

    protected static final int IFD_ENTRY_SIZE = 12;

    protected static final int IFD_ENTRY_TAG_SIZE = 2;

    protected static final int IFD_ENTRY_TYPE_SIZE = 2;

    protected static final int IFD_ENTRY_COUNT_SIZE = 4;

    protected static final int IFD_VALUE_OFFSET_SIZE = 4;

    protected static final int UINT_SIZE = 4;

    protected RandomAccessFile raf;

    protected boolean littleEndian;

    protected final byte[] data = new byte[8];

    protected final long firstIFDOffset;

    public CustomeTIFFMetaReader(RandomAccessFile raf) throws IOException {
        this.raf = raf;
        littleEndian = (raf.readShort() == 0x4949);
        raf.seek(4);
        firstIFDOffset = readUnsignedInt();
    }

    public long getImageWidth(long directory) throws IOException {
        return getUnsignedInt(directory, TAG_IMAGE_WIDTH);
    }

    public long getImageLength(long directory) throws IOException {
        return getUnsignedInt(directory, TAG_IMAGE_LENGTH);
    }

    public long getTileWidth(long directory) throws IOException {
        return getUnsignedInt(directory, TAG_TILE_WIDTH);
    }

    public double[] getModelPixelScale(long directory) throws IOException {
        return getDoubles(directory, TAG_MODEL_PIXEL_SCALE_TAG);
    }

    public double[] getModelTiePointTag(long directory) throws IOException {
        return getDoubles(directory, TAG_MODEL_TIE_POINT_TAG);
    }

    protected long[] getTileOffset(long directory, long tileNum) throws IOException {
        long tileOffsetsOffset = getFieldOffset(directory, TAG_TILE_OFFSET);
        raf.seek(tileOffsetsOffset + IFD_ENTRY_TAG_SIZE + IFD_ENTRY_TYPE_SIZE);
        long tileOffsetCount = readUnsignedInt();
        long startOffset = readUnsignedInt();
        long[] vals = new long[NUM_PIXEL_CHANNELS];
        raf.seek(startOffset + UINT_SIZE * tileNum);
        vals[0] = readUnsignedInt();
        raf.seek(startOffset + UINT_SIZE * tileNum + UINT_SIZE * (tileOffsetCount / NUM_PIXEL_CHANNELS));
        vals[1] = readUnsignedInt();
        raf.seek(startOffset + UINT_SIZE * tileNum + UINT_SIZE * 2 * (tileOffsetCount / NUM_PIXEL_CHANNELS));
        vals[2] = readUnsignedInt();
        return vals;
    }

    public Iterator<Long> getDirectoryIDs() {
        return new Iterator<Long>() {

            protected long count = 0;

            protected long lastIFDOffset = firstIFDOffset;

            public boolean hasNext() {
                return (lastIFDOffset != 0);
            }

            public Long next() {
                if (hasNext()) {
                    long offset = lastIFDOffset;
                    try {
                        raf.seek(offset);
                        int numFields = readUnsignedShort();
                        raf.seek(offset + HEADER_ENTRY_COUNT_SIZE + IFD_ENTRY_SIZE * numFields);
                        lastIFDOffset = readUnsignedInt();
                    } catch (IOException e) {
                        return -1L;
                    }
                    return count++;
                }
                return -1L;
            }

            public void remove() {
            }
        };
    }

    @Override
    public void finalize() {
        try {
            raf.close();
        } catch (IOException e) {
        }
    }

    public long getTileLength(long directory) throws IOException {
        return getUnsignedInt(directory, TAG_TILE_LENGTH);
    }

    protected long getUnsignedInt(long directory, int tag) throws IOException {
        long offset = getFieldOffset(directory, tag);
        raf.seek(offset + IFD_ENTRY_TAG_SIZE + IFD_ENTRY_TYPE_SIZE + IFD_ENTRY_COUNT_SIZE);
        return readUnsignedInt();
    }

    protected double[] getDoubles(long directory, int tag) throws IOException {
        long offset = getFieldOffset(directory, tag);
        raf.seek(offset + IFD_ENTRY_TAG_SIZE + IFD_ENTRY_TYPE_SIZE);
        double[] points = new double[(int) readUnsignedInt()];
        raf.seek(readUnsignedInt());
        for (int i = 0; i < points.length; i++) {
            points[i] = readDouble();
        }
        return points;
    }

    protected long getFieldOffset(long directory, int tag) throws IOException {
        int currDir = 0;
        long currOffset = firstIFDOffset;
        int numFields;
        while (currDir != 0) {
            raf.seek(currOffset);
            numFields = readUnsignedShort();
            raf.seek(HEADER_ENTRY_COUNT_SIZE + IFD_ENTRY_SIZE * numFields);
            currOffset = readUnsignedInt();
            currDir++;
        }
        raf.seek(currOffset);
        numFields = readUnsignedShort();
        int first = 0;
        int last = numFields - 1;
        while (first <= last) {
            int mid = (first + last) / 2;
            long entryOffset = currOffset + HEADER_ENTRY_COUNT_SIZE + IFD_ENTRY_SIZE * mid;
            raf.seek(entryOffset);
            int val = readUnsignedShort();
            if (tag > val) {
                first = mid + 1;
            } else if (tag < val) {
                last = mid - 1;
            } else {
                return entryOffset;
            }
        }
        return -1;
    }

    protected double readDouble() throws IOException {
        if (littleEndian) {
            raf.read(data);
            long val = 0;
            for (int i = data.length - 1; i >= 0; i--) {
                val |= data[i] & 0xff;
                if (i != 0) {
                    val <<= 8;
                }
            }
            return Double.longBitsToDouble(val);
        } else {
            return raf.readDouble();
        }
    }

    protected int readUnsignedShort() throws IOException {
        if (littleEndian) {
            raf.read(data, 0, 2);
            return ((((int) data[1] & 0x000000ff) << 8) | (((int) data[0] & 0x000000ff))) & 0xffff;
        } else {
            return raf.readShort();
        }
    }

    protected long readUnsignedInt() throws IOException {
        if (littleEndian) {
            raf.read(data, 0, 4);
            return ((((int) data[3] & 0x000000ff) << 24) | (((int) data[2] & 0x000000ff) << 16) | (((int) data[1] & 0x000000ff) << 8) | ((int) data[0] & 0x000000ff)) & 0xffffffffl;
        } else {
            return raf.readInt();
        }
    }
}
