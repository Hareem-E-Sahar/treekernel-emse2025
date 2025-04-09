package com.bones.core.utils;

public class ArrayUtils extends org.apache.commons.lang.ArrayUtils {

    public static Object[][] dimTransform(Object[][] obj) {
        if ((obj == null) || (obj.length <= 0)) {
            return null;
        }
        Object[][] newArr = new Object[obj[0].length][obj.length];
        for (int i = 0; i < newArr.length; ++i) {
            for (int j = 0; j < obj.length; ++j) {
                newArr[i][j] = obj[j][i];
            }
        }
        return newArr;
    }

    public static String[][] dimTransform(String[][] obj) {
        if ((obj == null) || (obj.length <= 0)) {
            return null;
        }
        String[][] newArr = new String[obj[0].length][obj.length];
        for (int i = 0; i < newArr.length; ++i) {
            for (int j = 0; j < obj.length; ++j) {
                newArr[i][j] = obj[j][i];
            }
        }
        return newArr;
    }

    public static int[] sort(int[] a) {
        if ((a == null) || (a.length <= 0)) {
            return null;
        }
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < a.length; ++j) {
                if (a[i] >= a[j]) continue;
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }
        }
        return a;
    }

    public static String[] sort(String[] a) {
        if ((a == null) || (a.length <= 0)) {
            return null;
        }
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < a.length; ++j) {
                if (new Integer(a[i]).intValue() >= new Integer(a[j]).intValue()) continue;
                String tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }
        }
        return a;
    }

    public static String toClearString(Object[] arr) {
        String result = toString(arr);
        result = result.substring(1, result.length() - 1);
        return result;
    }

    public static String toCustomString(Object[] array, String split) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            str.append(array[i]);
            if (i < array.length - 1) str.append(".");
        }
        return str.toString();
    }

    public static boolean isInArray(String[] array, String str) {
        if (array == null) return false;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) continue;
            if (array[i].equals(str)) return true;
        }
        return false;
    }
}
