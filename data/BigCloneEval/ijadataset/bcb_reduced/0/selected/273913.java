package com.bluemarsh.graphmaker.core.util;

import java.lang.reflect.Array;

/**
 * Utility methods for arrays.
 *
 * @author Nathan Fiedler
 */
public class Arrays {

    /**
     * Creates a new instance of Arrays.
     */
    private Arrays() {
    }

    /**
     * Joins two arrays of objects of the same type. If one is null, the other
     * is returned. If both are null, null is returned. Otherwise, a new array
     * of size equal to the length of both arrays is returned, with the elements
     * of arr1 appearing before the elements of arr2.
     *
     * @param  arr1  first array.
     * @param  arr2  second array.
     * @return  joined arrays, or null if both arrays are null.
     */
    public static Object[] join(Object[] arr1, Object[] arr2) {
        if (arr1 == null && arr2 != null) {
            return arr2;
        } else if (arr2 == null) {
            return arr1;
        } else {
            int size = arr1.length + arr2.length;
            Object[] arr = (Object[]) Array.newInstance(arr1.getClass().getComponentType(), size);
            System.arraycopy(arr1, 0, arr, 0, arr1.length);
            System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
            return arr;
        }
    }
}
