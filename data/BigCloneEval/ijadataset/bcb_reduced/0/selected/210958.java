package net.sf.balm.common.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author dz
 */
@SuppressWarnings("unchecked")
public abstract class ArrayUtils extends org.apache.commons.lang.ArrayUtils {

    /**
     * @param targets
     * @param value2Find
     * @return
     */
    public static boolean contains(String[] targets, String value2Find) {
        for (String target : targets) {
            if (target.equals(value2Find)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param a
     * @param c
     * @return
     */
    public static Object[] sort(Object[] a, Comparator c) {
        Class componentType = a.getClass().getComponentType();
        Object[] target = (Object[]) Array.newInstance(componentType, a.length);
        System.arraycopy(a, 0, target, 0, a.length);
        Arrays.sort(target, c);
        return target;
    }

    /**
     * @param left
     * @param right
     * @return
     */
    public static Object[] remove(Object[] array1, Object[] array2) {
        List<Object> result = new ArrayList<Object>(array1.length);
        for (int i = 0; i < array1.length; i++) {
            if (indexOf(array2, array1[i]) < 0) {
                result.add(array1[i]);
            }
        }
        return result.toArray();
    }

    /**
     * @param left
     * @param right
     * @return
     */
    public static String[] remove(String[] array1, String[] array2) {
        List<String> result = new ArrayList<String>(array1.length);
        for (int i = 0; i < array1.length; i++) {
            if (indexOf(array2, array1[i]) < 0) {
                result.add(array1[i]);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * @param left
     * @param right
     * @return
     */
    public static Integer[] remove(Integer[] array1, Integer[] array2) {
        List<Integer> result = new ArrayList<Integer>(array1.length);
        for (int i = 0; i < array1.length; i++) {
            if (indexOf(array2, array1[i]) < 0) {
                result.add(array1[i]);
            }
        }
        return result.toArray(new Integer[0]);
    }

    /**
     * @param left
     * @param right
     * @return
     */
    public static Long[] remove(Long[] array1, Long[] array2) {
        List<Long> result = new ArrayList<Long>(array1.length);
        for (int i = 0; i < array1.length; i++) {
            if (indexOf(array2, array1[i]) < 0) {
                result.add(array1[i]);
            }
        }
        return result.toArray(new Long[0]);
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static String[] concat(String[] left, String[] right) {
        String[] target = new String[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static Object[] concat(Object[] left, Object[] right) {
        Object[] target = new Object[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static int[] concat(int[] left, int[] right) {
        int[] target = new int[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static long[] concat(long[] left, long[] right) {
        long[] target = new long[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static short[] concat(short[] left, short[] right) {
        short[] target = new short[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static char[] concat(char[] left, char[] right) {
        char[] target = new char[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static byte[] concat(byte[] left, byte[] right) {
        byte[] target = new byte[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static float[] concat(float[] left, float[] right) {
        float[] target = new float[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static double[] concat(double[] left, double[] right) {
        double[] target = new double[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }

    /**
     * 连接两个数组
     * 
     * @param left
     * @param right
     * @return
     */
    public static boolean[] concat(boolean[] left, boolean[] right) {
        boolean[] target = new boolean[(left.length + right.length)];
        System.arraycopy(left, 0, target, 0, left.length);
        System.arraycopy(right, 0, target, left.length, right.length);
        return target;
    }
}
