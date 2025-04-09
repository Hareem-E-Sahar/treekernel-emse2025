package jgnash.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * This class is used to insert and remove objects from a sorted array of objects
 *
 * $Author: ccavanaugh $
 * 
 * $Id: SortedArray.java 675 2008-06-17 01:36:01Z ccavanaugh $
 */
public class SortedArray {

    public static final Object[] insert(Object[] array, Object o, int index) {
        Object[] tArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, tArray, 0, array.length);
        System.arraycopy(tArray, index, tArray, index + 1, array.length - index);
        tArray[index] = o;
        return tArray;
    }

    public static final Object[] insert(Object[] array, Object o) {
        int index = Arrays.binarySearch(array, o);
        if (index < 0) {
            index = -index - 1;
        } else {
            index = index + 1;
        }
        return insert(array, o, index);
    }

    public static final Object[] remove(Object[] array, int index) {
        Object[] tArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        System.arraycopy(array, 0, tArray, 0, index);
        System.arraycopy(array, index + 1, tArray, index, array.length - index - 1);
        return tArray;
    }

    public static final Object[] remove(Object[] array, Object o) {
        int index = java.util.Arrays.binarySearch(array, o);
        if (index >= 0) {
            return remove(array, index);
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] == o) {
                return remove(array, i);
            }
        }
        return array;
    }

    public static final boolean contains(Object[] array, Object o) {
        if (array != null) {
            if (Arrays.binarySearch(array, o) >= 0) {
                return true;
            }
        }
        return false;
    }
}
