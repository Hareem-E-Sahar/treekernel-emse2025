package com.android.lifestyleandtravel.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

    /**
	 * インスタンスを構築します。
	 */
    protected ArrayUtil() {
    }

    /**
	 * 配列にオブジェクトを追加します。
	 * 
	 * @param array
	 * @param obj
	 * @return オブジェクトが追加された結果の配列
	 */
    public static Object[] add(final Object[] array, final Object obj) {
        final Object[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = obj;
        return newArray;
    }

    /**
	 * intの配列にintを追加します。
	 * 
	 * @param array
	 * @param value
	 * @return オブジェクトが追加された結果の配列
	 */
    public static int[] add(final int[] array, final int value) {
        final int[] newArray = (int[]) Array.newInstance(int.class, array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = value;
        return newArray;
    }

    /**
	 * 配列に配列を追加します。
	 * 
	 * @param a
	 * @param b
	 * @return 配列が追加された結果の配列
	 */
    public static Object[] add(final Object[] a, final Object[] b) {
        if (a != null && b != null) {
            if (a.length != 0 && b.length != 0) {
                final Object[] array = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
                System.arraycopy(a, 0, array, 0, a.length);
                System.arraycopy(b, 0, array, a.length, b.length);
                return array;
            } else if (b.length == 0) {
                return a;
            } else {
                return b;
            }
        } else if (b == null) {
            return a;
        } else {
            return b;
        }
    }

    /**
	 * 配列中のオブジェクトのindexを返します。
	 * 
	 * @param array
	 * @param obj
	 * @return 配列中のオブジェクトのindex
	 */
    public static int indexOf(final Object[] array, final Object obj) {
        if (array != null) {
            final int length = array.length;
            for (int i = 0; i < length; ++i) {
                final Object o = array[i];
                if (o != null) {
                    if (o.equals(obj)) {
                        return i;
                    }
                } else if (obj == null) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
	 * 配列中のcharのindexを返します。
	 * 
	 * @param array
	 * @param ch
	 * @return 配列中のcharのindex
	 */
    public static int indexOf(final char[] array, final char ch) {
        if (array != null) {
            final int length = array.length;
            for (int i = 0; i < length; ++i) {
                final char c = array[i];
                if (ch == c) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
	 * 配列中から対象のオブジェクトを削除します。
	 * 
	 * @param array
	 * @param obj
	 * @return 削除後の配列
	 */
    public static Object[] remove(final Object[] array, final Object obj) {
        final int index = indexOf(array, obj);
        if (index < 0) {
            return array;
        }
        final Object[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        if (index > 0) {
            System.arraycopy(array, 0, newArray, 0, index);
        }
        if (index < array.length - 1) {
            System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
        }
        return newArray;
    }

    /**
	 * 配列が空かどうかを返します。
	 * 
	 * @param arrays
	 * @return 配列が空かどうか
	 */
    public static boolean isEmpty(final Object[] arrays) {
        return arrays == null || arrays.length == 0;
    }

    /**
	 * 配列にオブジェクトが含まれているかどうかを返します。
	 * 
	 * @param array
	 * @param obj
	 * @return 配列にオブジェクトが含まれているかどうか
	 */
    public static boolean contains(final Object[] array, final Object obj) {
        return -1 < indexOf(array, obj);
    }

    /**
	 * 配列にcharが含まれているかどうかを返します。
	 * 
	 * @param array
	 * @param ch
	 * @return 配列にcharが含まれているかどうか
	 */
    public static boolean contains(final char[] array, final char ch) {
        return -1 < indexOf(array, ch);
    }

    /**
	 * 順番は無視して2つの配列が等しいかどうかを返します。
	 * 
	 * @param array1
	 * @param array2
	 * @return 順番は無視して2つの配列が等しいかどうか
	 */
    public static boolean equalsIgnoreSequence(final Object[] array1, final Object[] array2) {
        if (array1 == null && array2 == null) {
            return true;
        } else if (array1 == null || array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        final List<Object> list = Arrays.asList(array2);
        final int length = array1.length;
        for (int i = 0; i < length; i++) {
            final Object o1 = array1[i];
            if (!list.contains(o1)) {
                return false;
            }
        }
        return true;
    }
}
