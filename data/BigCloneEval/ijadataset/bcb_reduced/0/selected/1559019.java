package org.eclipse.osgi.framework.internal.core;

/**
 * This class contains utility functions.
 */
public class Util {

    /**
	 * Performs a quicksort of the given objects
	 * by their string representation in ascending order.
	 * <p> 
	 *
	 * @param array	The array of objects to sort
	 */
    public static void sort(Object[] array) {
        qsort(array, 0, array.length - 1);
    }

    /**
	 * Sorts the array of objects by their string representation
	 * in ascending order.
	 * <p>
	 * This is a version of C.A.R Hoare's Quick Sort algorithm.
	 *
	 * @param array	the	array of objects to sort
	 * @param start	the start index to begin sorting
	 * @param stop		the end index to stop sorting
	 * 
	 * @exception	ArrayIndexOutOfBoundsException when <code>start < 0</code>
	 *				or <code>end >= array.length</code>
	 */
    public static void qsort(Object[] array, int start, int stop) {
        if (start >= stop) return;
        int left = start;
        int right = stop;
        Object temp;
        String mid = String.valueOf(array[(start + stop) / 2]);
        while (left <= right) {
            while ((left < stop) && (String.valueOf(array[left]).compareTo(mid) < 0)) {
                ++left;
            }
            while ((right > start) && (mid.compareTo(String.valueOf(array[right])) < 0)) {
                --right;
            }
            if (left <= right) {
                temp = array[left];
                array[left] = array[right];
                array[right] = temp;
                ++left;
                --right;
            }
        }
        if (start < right) {
            qsort(array, start, right);
        }
        if (left < stop) {
            qsort(array, left, stop);
        }
    }

    /**
	 * Sorts the specified range in the array in ascending order.
	 *
	 * @param		array	the Object array to be sorted
	 * @param		start	the start index to sort
	 * @param		end		the last + 1 index to sort
	 *
	 * @exception	ClassCastException when an element in the array does not
	 *				implement Comparable or elements cannot be compared to each other
	 * @exception	IllegalArgumentException when <code>start > end</code>
	 * @exception	ArrayIndexOutOfBoundsException when <code>start < 0</code>
	 *				or <code>end > array.size()</code>
	 */
    public static void sort(Object[] array, int start, int end) {
        int middle = (start + end) / 2;
        if (start + 1 < middle) sort(array, start, middle);
        if (middle + 1 < end) sort(array, middle, end);
        if (start + 1 >= end) return;
        if (((Comparable) array[middle - 1]).compareTo(array[middle]) <= 0) return;
        if (start + 2 == end) {
            Object temp = array[start];
            array[start] = array[middle];
            array[middle] = temp;
            return;
        }
        int i1 = start, i2 = middle, i3 = 0;
        Object[] merge = new Object[end - start];
        while (i1 < middle && i2 < end) {
            merge[i3++] = ((Comparable) array[i1]).compareTo(array[i2]) <= 0 ? array[i1++] : array[i2++];
        }
        if (i1 < middle) System.arraycopy(array, i1, merge, i3, middle - i1);
        System.arraycopy(merge, 0, array, start, i2 - start);
    }

    /**
	 * Sorts the specified range in the array in descending order.
	 *
	 * @param		array	the Object array to be sorted
	 * @param		start	the start index to sort
	 * @param		end		the last + 1 index to sort
	 *
	 * @exception	ClassCastException when an element in the array does not
	 *				implement Comparable or elements cannot be compared to each other
	 * @exception	IllegalArgumentException when <code>start > end</code>
	 * @exception	ArrayIndexOutOfBoundsException when <code>start < 0</code>
	 *				or <code>end > array.size()</code>
	 */
    public static void dsort(Object[] array, int start, int end) {
        sort(array, start, end);
        swap(array);
    }

    /**
	 *  Reverse the elements in the array.
	 *  
	 * @param		array	the Object array to be reversed
	 */
    public static void swap(Object[] array) {
        int start = 0;
        int end = array.length - 1;
        while (start < end) {
            Object temp = array[start];
            array[start++] = array[end];
            array[end--] = temp;
        }
    }

    /**
	 * Returns a string representation of the object
	 * in the given length.
	 * If the string representation of the given object
	 * is longer then it is truncated.
	 * If it is shorter then it is padded with the blanks
	 * to the given total length.
	 * If the given object is a number then the padding
	 * is done on the left, otherwise on the right.
	 *
	 * @param	object	the object to convert
	 * @param	length	the length the output string
	 */
    public static String toString(Object object, int length) {
        boolean onLeft = object instanceof Number;
        return toString(object, length, ' ', onLeft);
    }

    /**
	 * Returns a string representation of the object
	 * in the given length.
	 * If the string representation of the given object
	 * is longer then it is truncated.
	 * If it is shorter then it is padded to the left or right
	 * with the given character to the given total length.
	 *
	 * @param	object	the object to convert
	 * @param	length	the length the output string
	 * @param	pad		the pad character
	 * @param	onLeft	if <code>true</code> pad on the left, otherwise an the right
	 */
    public static String toString(Object object, int length, char pad, boolean onLeft) {
        String input = String.valueOf(object);
        int size = input.length();
        if (size >= length) {
            int start = (onLeft) ? size - length : 0;
            return input.substring(start, length);
        }
        StringBuffer padding = new StringBuffer(length - size);
        for (int i = size; i < length; i++) padding.append(pad);
        StringBuffer stringBuffer = new StringBuffer(length);
        if (onLeft) stringBuffer.append(padding.toString());
        stringBuffer.append(input);
        if (!onLeft) stringBuffer.append(padding.toString());
        return stringBuffer.toString();
    }
}
