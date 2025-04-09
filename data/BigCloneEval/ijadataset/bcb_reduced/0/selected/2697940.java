package com.elitost.utils;

import java.lang.reflect.Method;
import java.util.zip.CRC32;

public final class Utils {

    private Utils() {
    }

    /**
    * In a "creative", anti-scientific way checks whether a string or a
    * container is empty. <br>
    * Accepts a <code>Collection</code>, a <code>Map</code>, an array, a
    * <code>String</code>.
    * 
    * @param data a Collection or a Map or an array or a string to check
    * @return true if data is empty <br>
    *         <br>
    *         <b>Examples</b>:
    *         <li><code>isEmpty(""), isEmpty(null), isEmpty(new HashMap())</code>
    *         all return <b>true</b>;</li>
    *         <li><code>isEmpty(" "), isEmpty(new int[] {0})</code> returns
    *         <b>false</b>.</li>
    */
    public static final <T> boolean isEmpty(T data) {
        if (data == null) return true;
        if (data instanceof Object[]) return ((Object[]) data).length == 0;
        try {
            Method isEmpty = data.getClass().getMethod("isEmpty");
            if (isEmpty != null) {
                return ((Boolean) isEmpty.invoke(data)).booleanValue();
            }
        } catch (Exception e) {
        }
        try {
            Method size = data.getClass().getMethod("size");
            if (size != null) {
                return ((Integer) size.invoke(data)).intValue() == 0;
            }
        } catch (Exception e) {
        }
        return (data.toString().length() == 0) || "null".equals(data.toString());
    }

    /**
    * Calculates crc32 on a byte array
    * 
    * @param data source bytes
    * @return its crc32 <br>
    *         <br>
    *         <b>Example</b>:
    *         <li><code>crc32(new byte[] {1, 2, 3})</code> returns
    *         1438416925.</li>
    */
    public static final long crc32(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }

    /**
    * Calculates crc32 on a byte array
    * 
    * @param data source bytes
    * @param off offset in the array
    * @param len length of the area to crc
    * @return its crc32 <br>
    *         <br>
    *         <b>Example</b>:
    *         <li><code>crc32(new byte[] {0, 1, 2, 3, 4}, 1, 3)</code>
    *         returns 1438416925.</li>
    */
    public static final long crc32(byte[] data, int off, int len) {
        CRC32 crc32 = new CRC32();
        crc32.update(data, off, len);
        return crc32.getValue();
    }

    /**
    * Converts char array to byte array (per-element casting)
    * 
    * @param from char array
    * @return byte array <br>
    *         <br>
    *         <b>Example</b>:
    *         <li><code>toBytes(new char[] {0x0123, 0x4567, 0x89ab, 0xcdef})</code>
    *         returns {0x23, 0x67, (byte)0xab, (byte)0xef}.</li>
    */
    public static final byte[] toBytes(char[] from) {
        byte[] result = new byte[from.length];
        for (int i = 0; i < from.length; i++) {
            result[i] = (byte) from[i];
        }
        return result;
    }
}
