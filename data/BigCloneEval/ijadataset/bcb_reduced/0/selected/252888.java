package com.lucene.util;

/**
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching).  It also contains a static factory that allows
 * arrays to be viewed as Lists.
 *
 * @author  Josh Bloch
 * @version 1.17 03/18/98
 * @since   JDK1.2
 */
public class Arrays {

    /**
     * Sorts the specified array of objects into ascending order, according
     * to the <i>natural comparison method</i> of its elements.  All
     * elements in the array must implement the Comparable interface.
     * Furthermore, all elements in the array must be <i>mutually
     * comparable</i> (that is, e1.compareTo(e2) must not throw a
     * typeMismatchException for any elements e1 and e2 in the array).
     * <p>
     * This sort is guaranteed to be <em>stable</em>:  equal elements will
     * not be reordered as a result of the sort.
     * <p>
     * The sorting algorithm is a modified mergesort (in which the merge is
     * omitted if the highest element in the low sublist is less than the
     * lowest element in the high sublist).  This algorithm offers guaranteed
     * n*log(n) performance, and can approach linear performance on nearly
     * sorted lists.
     * 
     * @param a the array to be sorted.
     * @exception ClassCastException array contains elements that are not
     *		  <i>mutually comparable</i> (for example, Strings and
     *		  Integers).
     * @see Comparable
     */
    public static void sort(String[] a) {
        String aux[] = (String[]) a.clone();
        mergeSort(aux, a, 0, a.length);
    }

    private static void mergeSort(String src[], String dest[], int low, int high) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; i++) for (int j = i; j > low && (dest[j - 1]).compareTo(dest[j]) > 0; j--) swap(dest, j, j - 1);
            return;
        }
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid);
        mergeSort(dest, src, mid, high);
        if ((src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && (src[p]).compareTo(src[q]) <= 0) dest[i] = src[p++]; else dest[i] = src[q++];
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(String x[], int a, int b) {
        String t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
