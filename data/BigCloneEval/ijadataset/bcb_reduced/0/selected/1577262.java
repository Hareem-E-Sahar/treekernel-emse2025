package com.tinywebgears.tuatara.framework.common;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;

public class ArrayHelper implements Serializable {

    public static Object[] convert(Object[] from, Object[] to) {
        if (to.length < from.length) {
            to = (Object[]) Array.newInstance(to.getClass().getComponentType(), from.length);
        }
        System.arraycopy(from, 0, to, 0, from.length);
        if (to.length > from.length) {
            to[from.length] = null;
        }
        return to;
    }

    public static byte[] toByteArray(Collection<Byte> src) {
        byte[] result = new byte[src.size()];
        int i = 0;
        for (Byte b : src) {
            result[i] = b.byteValue();
            i++;
        }
        return result;
    }

    public static byte[] toByteArrayNullSafe(Collection<Byte> list) {
        if (list == null || list.size() == 0) return new byte[0];
        return toByteArray(list);
    }

    public static short[] toShortArray(Collection<Short> c) {
        short[] result = new short[c.size()];
        int i = 0;
        for (Short s : c) result[i++] = s;
        return result;
    }

    public static Short[] toShortArray(short[] list) {
        Short[] result = new Short[list.length];
        for (int i = 0; i < list.length; ++i) result[i] = list[i];
        return result;
    }

    public static Integer[] toIntegerArray(short[] list) {
        Integer[] result = new Integer[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = new Integer(list[i]);
        }
        return result;
    }

    public static Integer[] toIntegerArray(int[] list) {
        Integer[] result = new Integer[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = new Integer(list[i]);
        }
        return result;
    }

    public static int[] toIntArray(Integer[] list) {
        int[] result = new int[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = list[i];
        }
        return result;
    }

    public static int[] toIntArray(Collection<Integer> list) {
        int[] result = new int[list.size()];
        int i = 0;
        for (Integer val : list) {
            result[i] = val;
            i++;
        }
        return result;
    }

    public static boolean[] toBooleanArray(Collection<Boolean> c) {
        boolean[] result = new boolean[c.size()];
        int i = 0;
        for (Boolean val : c) {
            result[i] = val;
            i++;
        }
        return result;
    }

    public static <T> String toString(T[] array, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) sb.append(sep);
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
