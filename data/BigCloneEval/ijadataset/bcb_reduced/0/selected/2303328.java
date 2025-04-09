package com.tsadom.util;

/**
 * @author Uriel Chemouni
 */
public class LangUtils {

    public static int[] realloc(int[] data, int newSize) {
        if (newSize == data.length) return data;
        int[] result = new int[newSize];
        System.arraycopy(data, 0, result, 0, newSize < data.length ? newSize : data.length);
        return result;
    }

    public static char[] realloc(char[] data, int newSize) {
        if (newSize == data.length) return data;
        char[] result = new char[newSize];
        System.arraycopy(data, 0, result, 0, newSize < data.length ? newSize : data.length);
        return result;
    }

    public static Object[] realloc(Object[] data, int newSize) {
        if (newSize == data.length) return data;
        Object[] result = (Object[]) java.lang.reflect.Array.newInstance(data.getClass().getComponentType(), newSize);
        System.arraycopy(data, 0, result, 0, newSize < data.length ? newSize : data.length);
        return result;
    }

    public static Object[] merge(Object[] data1, Object[] data2) {
        if (data2.length == 0) return data1;
        if (data1.length == 0) return data2;
        Object[] result = (Object[]) java.lang.reflect.Array.newInstance(data1.getClass().getComponentType(), data1.length + data2.length);
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);
        return result;
    }

    public static int[] merge(int[] data1, int[] data2) {
        if (data2.length == 0) return data1;
        if (data1.length == 0) return data2;
        int[] result = new int[data1.length + data2.length];
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);
        return result;
    }
}
