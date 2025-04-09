package org.jmol.viewer;

import java.lang.reflect.Array;

final class Util {

    static Object ensureLength(Object array, int minimumLength) {
        if (array != null && Array.getLength(array) >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    static String[] ensureLength(String[] array, int minimumLength) {
        if (array != null && array.length >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    static float[] ensureLength(float[] array, int minimumLength) {
        if (array != null && array.length >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    static int[] ensureLength(int[] array, int minimumLength) {
        if (array != null && array.length >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    static short[] ensureLength(short[] array, int minimumLength) {
        if (array != null && array.length >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    static byte[] ensureLength(byte[] array, int minimumLength) {
        if (array != null && array.length >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    static Object doubleLength(Object array) {
        return setLength(array, (array == null ? 16 : 2 * Array.getLength(array)));
    }

    static String[] doubleLength(String[] array) {
        return setLength(array, (array == null ? 16 : 2 * array.length));
    }

    static float[] doubleLength(float[] array) {
        return setLength(array, (array == null ? 16 : 2 * array.length));
    }

    static int[] doubleLength(int[] array) {
        return setLength(array, (array == null ? 16 : 2 * array.length));
    }

    static short[] doubleLength(short[] array) {
        return setLength(array, (array == null ? 16 : 2 * array.length));
    }

    static byte[] doubleLength(byte[] array) {
        return setLength(array, (array == null ? 16 : 2 * array.length));
    }

    static boolean[] doubleLength(boolean[] array) {
        return setLength(array, (array == null ? 16 : 2 * array.length));
    }

    static Object setLength(Object array, int newLength) {
        Object t = Array.newInstance(array.getClass().getComponentType(), newLength);
        int oldLength = Array.getLength(array);
        System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        return t;
    }

    static String[] setLength(String[] array, int newLength) {
        String[] t = new String[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static float[] setLength(float[] array, int newLength) {
        float[] t = new float[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static int[] setLength(int[] array, int newLength) {
        int[] t = new int[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static short[] setLength(short[] array, int newLength) {
        short[] t = new short[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static byte[] setLength(byte[] array, int newLength) {
        byte[] t = new byte[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static boolean[] setLength(boolean[] array, int newLength) {
        boolean[] t = new boolean[newLength];
        if (array != null) {
            int oldLength = array.length;
            System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        }
        return t;
    }

    static void swap(short[] array, int indexA, int indexB) {
        short t = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = t;
    }

    static void swap(int[] array, int indexA, int indexB) {
        int t = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = t;
    }

    static void swap(float[] array, int indexA, int indexB) {
        float t = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = t;
    }
}
