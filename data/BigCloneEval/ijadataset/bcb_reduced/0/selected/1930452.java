package ru.formula.util;

import java.lang.reflect.Array;

/**
 * Helper class for work with arrays.
 * @author udav
 */
public class ArrayHelper {

    /**
	 * Searches the specified array of ints for the specified value using enumerative technique.
	 * @param val the value to be searched for.
	 * @param array the array to be searched.
	 * @return index of the search value, if it is contained in array; otherwise -1.
	 */
    public static int indexOf(int val, int[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (val == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * Searches the specified array of Objects for the specified value using enumerative technique.
	 * @param val the value to be searched for. May be null.
	 * @param array the array to be searched. Array can contain null values.
	 * @return index of the search value, if it is contained in array; otherwise return -1.
	 */
    public static int indexOf(Object val, Object[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (val == array[i] || val != null && val.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * Checks array of ints for specified size. If array`s size is less or equal specified size,
	 * then returns the specified array; otherwise returns new array of specified size
	 * with old arrays's content.
	 * @param array the array to be checket.
	 * @param newCapacity the size to be checked.
	 * @return If array's size is less or equals newCapacity, returns the array;
	 * otherwise returns new array of the size equals newCapacity in which first
	 * <code>array.length</code> elements are copied from the array.
	 */
    public static int[] ensureCapacity(int[] array, int newCapacity) {
        if (array.length >= newCapacity) {
            return array;
        }
        int[] newArray = new int[newCapacity];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
	 * Checks array of doubles for specified size. If array`s size is less or equal specified size,
	 * then returns the specified array; otherwise returns new array of specified size
	 * with old arrays's content.
	 * @param array the array to be checket.
	 * @param newCapacity the size to be checked.
	 * @return If array's size is less or equals newCapacity, returns the array;
	 * otherwise returns new array of the size equals newCapacity in which first
	 * <code>array.length</code> elements are copied from the array.
	 */
    public static double[] ensureCapacity(double[] array, int newCapacity) {
        if (array.length >= newCapacity) {
            return array;
        }
        double[] newArray = new double[newCapacity];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
	 * Checks array for specified size. If array`s size is less or equal specified size,
	 * then returns the specified array; otherwise returns new array of specified size
	 * with old arrays's content.
	 * @param array the array to be checket.
	 * @param newCapacity the size to be checked.
	 * @return If array's size is less or equals newCapacity, returns the array;
	 * otherwise returns new array of the size equals newCapacity in which first
	 * <code>array.length</code> elements are copied from the array.
	 */
    public static <T> T[] ensureCapacity(T[] array, int newCapacity) {
        if (array.length >= newCapacity) {
            return array;
        }
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), newCapacity);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }
}
