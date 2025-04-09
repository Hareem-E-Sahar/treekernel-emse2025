package com.starlight;

import java.lang.reflect.Array;

/**
 * Contains static methods for working with arrays and a bunch of empty array static
 * instances.
 *
 * @author reden
 */
public final class ArrayKit {

    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static final short[] EMPTY_SHORT_ARRAY = new short[0];

    public static final int[] EMPTY_INT_ARRAY = new int[0];

    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
	 * Prevent creation of objects of this class
	 */
    private ArrayKit() {
    }

    /**
	 * Make a copy of the given array.
	 */
    public static boolean[] clone(boolean[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_BOOLEAN_ARRAY;
        boolean[] tmp = new boolean[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static byte[] clone(byte[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_BYTE_ARRAY;
        byte[] tmp = new byte[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static short[] clone(short[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_SHORT_ARRAY;
        short[] tmp = new short[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static int[] clone(int[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_INT_ARRAY;
        int[] tmp = new int[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static long[] clone(long[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_LONG_ARRAY;
        long[] tmp = new long[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static float[] clone(float[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_FLOAT_ARRAY;
        float[] tmp = new float[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static double[] clone(double[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_DOUBLE_ARRAY;
        double[] tmp = new double[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static char[] clone(char[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_CHAR_ARRAY;
        char[] tmp = new char[array.length];
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Make a copy of the given array.
	 */
    public static <T> T[] clone(T[] array) {
        if (array == null) return null;
        if (array.length == 0) {
            return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }
        T[] tmp = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length);
        System.arraycopy(array, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
	 * Return a string representation of an array
	 */
    public static <T> String toString(T[] array) {
        return toString(array, 0, array.length);
    }

    /**
	 * Return a string representation of an array
	 */
    public static <T> String toString(T[] array, int offset, int length) {
        if (array == null) return "<null>"; else {
            int limit = offset + length;
            StringBuffer buf = new StringBuffer("[");
            for (int i = offset; i < limit; i++) {
                if (i != 0) buf.append(" ");
                buf.append(array[i]);
            }
            buf.append("]");
            return buf.toString();
        }
    }

    /**
	 * Return a string representation of an int array
	 */
    public static String toString(int[] array) {
        return toString(array, 0, array.length, false);
    }

    /**
	 * Return a string representation of an array
	 */
    public static String toString(int[] array, int offset, int length, boolean hex) {
        if (array == null) return "<null>"; else {
            int limit = offset + length;
            StringBuffer buf = new StringBuffer("[");
            for (int i = offset; i < limit; i++) {
                if (i != 0) buf.append(" ");
                if (hex) {
                    buf.append(Integer.toHexString(array[i]));
                } else {
                    buf.append(array[i]);
                }
            }
            buf.append("]");
            return buf.toString();
        }
    }

    /**
	 * Return a string representation of a byte array
	 */
    public static String toString(byte[] array) {
        return toString(array, 0, array.length, false);
    }

    /**
	 * Return a string representation of a byte array
	 */
    public static String toString(byte[] array, int offset, int length) {
        return toString(array, offset, length, false);
    }

    /**
	 * Return a string representation of a byte array
	 */
    public static String toString(byte[] array, int offset, int length, boolean hex) {
        if (array == null) return "<null>"; else {
            int limit = offset + length;
            StringBuffer buf = new StringBuffer("[");
            for (int i = offset; i < limit; i++) {
                if (i != 0) buf.append(" ");
                if (hex) {
                    int value = 0xff & (int) array[i];
                    buf.append(Integer.toHexString(Integer.valueOf(value)));
                } else {
                    buf.append(array[i]);
                }
            }
            buf.append("]");
            return buf.toString();
        }
    }

    /**
	 * Returns true if the array contains the given string.
	 */
    public static boolean contains(String[] array, String string, boolean ignore_case) {
        if (array == null) return false;
        for (String element : array) {
            if (ignore_case ? string.equalsIgnoreCase(element) : string.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Returns true if the array contains the given object.
	 */
    public static boolean contains(Object[] array, Object object) {
        if (array == null) return false;
        for (Object element : array) {
            if (MiscKit.equal(element, object)) return true;
        }
        return false;
    }

    public static <T> T[] of(T... elements) {
        return elements;
    }

    public static <T, S extends T> T[] combine(Class<T> type, S[]... arrays) {
        int size = 0;
        for (S[] array : arrays) {
            size += array.length;
        }
        T[] to_return = (T[]) Array.newInstance(type, size);
        int index = 0;
        for (S[] array : arrays) {
            System.arraycopy(array, 0, to_return, index, array.length);
            index += array.length;
        }
        return to_return;
    }
}
