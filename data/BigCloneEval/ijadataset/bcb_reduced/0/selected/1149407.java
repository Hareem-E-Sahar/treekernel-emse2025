package com.myjavatools.lib;

import java.util.zip.CRC32;

public abstract class Bytes {

    /**
   * Converts char array to byte array (per-element casting)
   *
   * @param from char array
   * @return byte array
   *
   * <br><br><b>Example</b>:
   * <li><code>toBytes(new char[] {0x0123, 0x4567, 0x89ab, 0xcdef})</code>
   * returns {0x23, 0x67, (byte)0xab, (byte)0xef}.</li>
   */
    public static final byte[] toBytes(final char[] from) {
        final byte[] result = new byte[from.length];
        for (int i = 0; i < from.length; i++) {
            result[i] = (byte) from[i];
        }
        return result;
    }

    /**
   * Converts byte array to char array (per-element casting)
   * @param from byte array
   * @return char array
   *
   * <br><br><b>Example</b>:
   * <li><code>toChars(new byte[] {0x23, 0x67, (byte)0xab, (byte)0xef})</code>
   * returns new char[] {0x23, 0x67, 0xab, 0xef}.</li>
   */
    public static final char[] toChars(final byte[] from) {
        final char[] result = new char[from.length];
        for (int i = 0; i < from.length; i++) {
            result[i] = (char) (0xff & from[i]);
        }
        return result;
    }

    /**
   * Calculates crc32 on a byte array
   *
   * @param data source bytes
   * @return its crc32
   *
   * <br><br><b>Example</b>:
   * <li><code>crc32(new byte[] {1, 2, 3})</code>
   * returns 1438416925.</li>
   */
    public static final long crc32(final byte[] data) {
        final CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }

    /**
   * Calculates crc32 on a byte array
   *
   * @param data source bytes
   * @param off offset in the array
   * @param len length of the area to crc
   * @return its crc32
   *
   * <br><br><b>Example</b>:
   * <li><code>crc32(new byte[] {0, 1, 2, 3, 4}, 1, 3)</code>
   * returns 1438416925.</li>
   */
    public static final long crc32(final byte[] data, final int off, final int len) {
        final CRC32 crc32 = new CRC32();
        crc32.update(data, off, len);
        return crc32.getValue();
    }

    /**
   * Converts long to byte array (lower bytes first)
   *
   * @param from the long value
   * @return byte array
   *
   * <br><br><b>Example</b>:
   * <li><code>toBytes(0x0123456789abcdefl)</code>
   * returns {(byte)0xef, (byte)0xcd, (byte)0xab, (byte)0x89, 0x67, 0x45, 0x23, 0x01}.</li>
   */
    public static final byte[] toBytes(long from) {
        final byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) from;
            from >>= 8;
        }
        return result;
    }
}
