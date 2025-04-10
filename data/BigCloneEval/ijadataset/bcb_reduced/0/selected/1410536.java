package net.sf.refactorit.common.util.png;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

/**
 * Class representing a single PNG chunk.
 */
class Chunk {

    public static final String CHUNK_IHDR = "IHDR";

    public static final String CHUNK_PLTE = "PLTE";

    public static final String CHUNK_TRNS = "tRNS";

    public static final String CHUNK_IDAT = "IDAT";

    public static final String CHUNK_IEND = "IEND";

    /** Data for chunk */
    private ByteArrayOutputStream data;

    /** Type of chunk */
    private String type;

    /**
   * Constructor.
   * @param type Chunk type (4 character string)
   */
    Chunk(String type) {
        this.type = type;
        if (type == null || type.length() != 4) {
            throw new IllegalArgumentException("Invalid PNG chunk type: " + type);
        }
        this.data = new ByteArrayOutputStream();
    }

    /**
   * Writes an integer to the internal data stream.
   */
    void write(int value) {
        data.write(intToBytes(value), 0, 4);
    }

    /**
   * Writes an integer to the internal data stream.
   */
    void writeInt(int value) {
        write(value);
    }

    /**
   * Writes a short integer to the internal data stream.
   */
    void write(short value) {
        data.write((value & 0xff) >>> 8);
        data.write(value & 0xff);
    }

    /**
   * Writes a short integer to the internal data stream.
   */
    void writeShort(int value) {
        write((short) value);
    }

    /**
   * Writes a byte to the internal data stream.
   */
    void write(byte value) {
        data.write(value);
    }

    /**
   * Writes a byte to the internal data stream.
   */
    void writeByte(int value) {
        write((byte) value);
    }

    /**
   * Writes a block of data to the internal data stream.
   */
    void write(byte[] block) {
        data.write(block, 0, block.length);
    }

    /**
   * Returns the data stream. The returned output stream
   * should <b>not</b> be closed by the caller.
   */
    ByteArrayOutputStream getDataStream() {
        return data;
    }

    /**
   * Writes the entire chunk to a given output stream.
   * This does not close either the output stream or the
   * internal data stream, so multiple calls to this method
   * may be made if necessary.
   */
    void output(OutputStream out) throws IOException {
        CRC32 crc = new CRC32();
        byte[] dataBytes = data.toByteArray();
        byte[] lenBytes = intToBytes(dataBytes.length);
        out.write(lenBytes);
        for (int i = 0; i < 4; i++) {
            crc.update(type.charAt(i));
            out.write(type.charAt(i));
        }
        crc.update(dataBytes);
        out.write(dataBytes);
        out.write(intToBytes((int) crc.getValue()));
    }

    private static byte[] intToBytes(int value) {
        byte[] ret = new byte[4];
        ret[0] = (byte) ((value & 0xff000000) >>> 24);
        ret[1] = (byte) ((value & 0x00ff0000) >>> 16);
        ret[2] = (byte) ((value & 0x0000ff00) >>> 8);
        ret[3] = (byte) ((value & 0x000000ff));
        return ret;
    }

    /**
   * Closes the chunk's internal data stream. After this
   * method has been called, no other methods may be called.
   */
    void close() {
        if (data != null) {
            try {
                data.close();
            } catch (IOException e) {
            }
        }
        data = null;
    }
}
