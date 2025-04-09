package net.sf.ninjakore.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.ninjakore.utils.perl.PerlUtils;

public class Utils {

    /**
	 * Convert the byte array to an int.
	 * 
	 * @param b
	 *            the byte array to be converted
	 * @return the integer converted from an int
	 */
    public static final int byteArrayToBigEndianInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    /**
	 * Convert an int to a byte array.
	 * 
	 * @param value
	 *            the int to be converted
	 * @return the byte array converted to a short
	 */
    public static final byte[] littleEndianShortToByteArray(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    /**
	 * Convert an int to a byte array.
	 * 
	 * @param value
	 *            the int to be converted
	 * @return the byte array converted to a short
	 */
    public static final byte[] littleEndianIntToByteArray(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /**
	 * Convert the byte array to an int.
	 * 
	 * @param b
	 *            the byte array to be converted
	 * @return the integer converted from a short
	 */
    public static final short byteArrayToLittleEndianShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    /**
	 * Convert an int to a byte array.
	 * 
	 * @param value
	 *            the int to be converted
	 * @return the byte array converted to an int
	 */
    public static final byte[] bigEndianIntToByteArray(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
    }

    /**
	 * Left-pads a {@link String} with the specified characers, up to the
	 * specified with (inclusive of the original string). If the original string
	 * is null or greater than the specified width, it will return the original
	 * string.
	 * 
	 * @param original
	 * @param padChar
	 * @param width
	 * @return
	 */
    public static final String padLeft(String original, char padChar, int width) {
        if (original == null) {
            return original;
        }
        int padding = width - original.length();
        if (padding <= 0) {
            return original;
        }
        StringBuilder sb = new StringBuilder(original);
        char[] ch = new char[padding];
        Arrays.fill(ch, padChar);
        sb.insert(0, ch);
        return sb.toString();
    }

    /**
	 * This function returns a {@link Map} with a {@link String} key and a value
	 * of {@link Object}. The keys are provided either as an array of strings,
	 * or a varargs of strings; the assumption being that the order of elements
	 * (or declaration in case of varargs) corresponds to the order of the
	 * template types. The caller is also responsible for properly casting the
	 * object retrieved from the map.
	 */
    public static Map<String, Object> unpackToMap(String template, byte[] scalar, String... keys) {
        Object[] values = PerlUtils.unpack(template, scalar);
        Map<String, Object> struct = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; ++i) {
            struct.put(keys[i], values[i]);
        }
        return struct;
    }

    public static byte[] packFromMap(String template, Map<String, Object> fields) {
        return null;
    }

    /**
	 * Finds all occurrences in a string using the specified regex
	 * 
	 * @param original
	 * @param regex
	 * @return
	 */
    public static List<String> findInString(String original, String regex) {
        List<String> matches = new ArrayList<String>();
        Pattern number = Pattern.compile(regex);
        Matcher matcher = number.matcher(original);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start == end) continue;
            matches.add(original.substring(matcher.start(), matcher.end()));
        }
        return matches;
    }

    public static void main(String[] args) {
    }

    public static <T> T[] concatArrays(List<T[]> arrays) {
        int totalLength = 0;
        for (T[] array : arrays) totalLength += array.length;
        T[] first = arrays.get(0);
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : arrays) {
            if (array.equals(first)) continue;
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static <T> T[] concatArrays(T[]... arrays) {
        return concatArrays(Arrays.asList(arrays));
    }

    public static byte[] concatArrays(List<byte[]> arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) totalLength += array.length;
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static byte[] concatArrays(byte[]... arrays) {
        return concatArrays(Arrays.asList(arrays));
    }

    public static Object hexdump(byte[] stream) {
        return null;
    }

    public static int objectToInt(Object obj) {
        return obj != null ? ((Number) obj).intValue() : 0;
    }

    public static short objectToShort(Object obj) {
        return obj != null ? ((Number) obj).shortValue() : 0;
    }

    public static byte objectToByte(Object obj) {
        return obj != null ? ((Number) obj).byteValue() : 0;
    }
}
