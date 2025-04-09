package org.jmlspecs.jir.internal.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {

    public static <T> boolean containsInArray(final T[] array, final T element) {
        for (final T element2 : array) {
            if (element2.equals(element)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T[] getComplement(final T[] array, final int... indices) {
        final List<T> list = new ArrayList<T>();
        out: for (int i = 0; i < array.length; i++) {
            for (final int index : indices) {
                if (i == index) {
                    continue out;
                }
            }
            list.add(array[i]);
        }
        @SuppressWarnings("unchecked") final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - indices.length);
        list.toArray(newArray);
        return newArray;
    }

    public static <T> T[] getComplement(final T[] array, final List<Integer> indices) {
        final List<T> list = new ArrayList<T>();
        out: for (int i = 0; i < array.length; i++) {
            for (final int index : indices) {
                if (i == index) {
                    continue out;
                }
            }
            list.add(array[i]);
        }
        @SuppressWarnings("unchecked") final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - indices.size());
        list.toArray(newArray);
        return newArray;
    }

    public static <T> T[] getExtendedArray(final T[] array, final int index, final T element) {
        @SuppressWarnings("unchecked") final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, newArray.length - (index + 1));
        return newArray;
    }

    public static <T> T[] union(final T[] array1, final T[] array2) {
        @SuppressWarnings("unchecked") final T[] newArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);
        return newArray;
    }
}
