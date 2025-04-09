package org.tigr.microarray.mev.cluster.gui.impl.util;

public class IntSorter {

    /**
     * Sorts an array with specified comparator.
     *
     * @param a the array of integers to be sorted.
     * @param c the <code>IntComparator</code> interface implementation.
     * @see IntComparator
     */
    public static void sort(int[] a, IntComparator c) {
        int[] aux = cloneArray(a);
        mergeSort(aux, a, 0, a.length, c);
    }

    private static int[] cloneArray(int[] a) {
        int[] clone = new int[a.length];
        for (int i = a.length; --i >= 0; ) {
            clone[i] = a[i];
        }
        return clone;
    }

    private static void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void mergeSort(int[] src, int[] dest, int low, int high, IntComparator c) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; i++) for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--) swap(dest, j, j - 1);
            return;
        }
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid, c);
        mergeSort(dest, src, mid, high, c);
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) dest[i] = src[p++]; else dest[i] = src[q++];
        }
    }
}
