package org.knopflerfish.util.sort;

import java.io.File;

public class Sort {

    /** No public default constructor in this class. */
    private Sort() throws Exception {
        throw new Exception("The Sort class is not instanciable.");
    }

    /**
     * Sorts the specified array of Integer objects into ascending order,
     * according to the natural ordering of its elements.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     */
    public static void sortIntegerArray(Integer[] a) {
        sortIntegerArray(a, 0, a.length);
    }

    /**
     * Sorts the specified range of the specified array of Integer objects into
     * ascending order, according to the natural ordering of its elements. The
     * range to be sorted extends from index fromIndex, inclusive, to index
     * toIndex, exclusive.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     * @param fromIndex
     *            The index of the first element (inclusive) to be sorted.
     * @param toIndex
     *            The index of the last element (exclusive) to be sorted.
     */
    public static void sortIntegerArray(Integer[] a, int fromIndex, int toIndex) {
        int middle;
        if (a == null) return;
        if (fromIndex + 1 < toIndex) {
            middle = (fromIndex + toIndex) / 2;
            sortIntegerArray(a, fromIndex, middle);
            sortIntegerArray(a, middle, toIndex);
            mergeIntegerArray(a, fromIndex, toIndex);
        }
    }

    /**
     * Implementation of the merge-part of the mergesort algorithm for Integer
     * objects.
     * 
     * @param a
     *            The array to be merged.
     * @param fromIndex
     *            The index of the first element (inclusive) to be merged.
     * @param toIndex
     *            The index of the last element (exclusive) to be merged.
     */
    private static void mergeIntegerArray(Integer[] a, int fromIndex, int toIndex) {
        int i, j, k, middle, n;
        n = toIndex - fromIndex;
        Integer[] b = new Integer[n];
        k = 0;
        middle = (fromIndex + toIndex) / 2;
        for (i = fromIndex; i < middle; i++) b[k++] = a[i];
        for (j = toIndex - 1; j >= middle; j--) b[k++] = a[j];
        i = 0;
        j = n - 1;
        k = fromIndex;
        while (i <= j) {
            if (b[i].intValue() <= b[j].intValue()) a[k++] = b[i++]; else a[k++] = b[j--];
        }
    }

    /**
     * Sorts the specified array of String objects into ascending order,
     * according to the natural ordering of its elements.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     */
    public static void sortStringArray(String[] a) {
        sortStringArray(a, 0, a.length);
    }

    /**
     * Sorts the specified range of the specified array of String objects into
     * ascending order, according to the natural ordering of its elements. The
     * range to be sorted extends from index fromIndex, inclusive, to index
     * toIndex, exclusive.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     * @param fromIndex
     *            The index of the first element (inclusive) to be sorted.
     * @param toIndex
     *            The index of the last element (exclusive) to be sorted.
     */
    public static void sortStringArray(String[] a, int fromIndex, int toIndex) {
        int middle;
        if (a == null) return;
        if (fromIndex + 1 < toIndex) {
            middle = (fromIndex + toIndex) / 2;
            sortStringArray(a, fromIndex, middle);
            sortStringArray(a, middle, toIndex);
            mergeStringArray(a, fromIndex, toIndex);
        }
    }

    /**
     * Implementation of the merge-part of the mergesort algorithm for String
     * objects.
     * 
     * @param a
     *            The array to be merged.
     * @param fromIndex
     *            The index of the first element (inclusive) to be merged.
     * @param toIndex
     *            The index of the last element (exclusive) to be merged.
     */
    private static void mergeStringArray(String[] a, int fromIndex, int toIndex) {
        int i, j, k, middle, n;
        n = toIndex - fromIndex;
        String[] b = new String[n];
        k = 0;
        middle = (fromIndex + toIndex) / 2;
        for (i = fromIndex; i < middle; i++) b[k++] = a[i];
        for (j = toIndex - 1; j >= middle; j--) b[k++] = a[j];
        i = 0;
        j = n - 1;
        k = fromIndex;
        while (i <= j) {
            if (b[i].compareTo(b[j]) < 0) a[k++] = b[i++]; else a[k++] = b[j--];
        }
    }

    /**
     * Sorts the specified array of File objects into ascending order, according
     * to the natural ordering of its elements.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     */
    public static void sortFileArray(File[] a) {
        sortFileArray(a, 0, a.length);
    }

    /**
     * Sorts the specified range of the specified array of File objects into
     * ascending order, according to the natural ordering of its elements. The
     * range to be sorted extends from index fromIndex, inclusive, to index
     * toIndex, exclusive.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     * @param fromIndex
     *            The index of the first element (inclusive) to be sorted.
     * @param toIndex
     *            The index of the last element (exclusive) to be sorted.
     */
    public static void sortFileArray(File[] a, int fromIndex, int toIndex) {
        int middle;
        if (a == null) return;
        if (fromIndex + 1 < toIndex) {
            middle = (fromIndex + toIndex) / 2;
            sortFileArray(a, fromIndex, middle);
            sortFileArray(a, middle, toIndex);
            mergeFileArray(a, fromIndex, toIndex);
        }
    }

    /**
     * Implementation of the merge-part of the mergesort algorithm for File
     * objects.
     * 
     * @param a
     *            The array to be merged.
     * @param fromIndex
     *            The index of the first element (inclusive) to be merged.
     * @param toIndex
     *            The index of the last element (exclusive) to be merged.
     */
    private static void mergeFileArray(File[] a, int fromIndex, int toIndex) {
        int i, j, k, middle, n;
        n = toIndex - fromIndex;
        File[] b = new File[n];
        k = 0;
        middle = (fromIndex + toIndex) / 2;
        for (i = fromIndex; i < middle; i++) b[k++] = a[i];
        for (j = toIndex - 1; j >= middle; j--) b[k++] = a[j];
        i = 0;
        j = n - 1;
        k = fromIndex;
        while (i <= j) {
            if (b[i].getName().compareTo(b[j].getName()) < 0) a[k++] = b[i++]; else a[k++] = b[j--];
        }
    }

    /**
     * Sorts the specified array of Float objects into ascending order,
     * according to the natural ordering of its elements.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     */
    public static void sortFloatArray(Float[] a) {
        sortFloatArray(a, 0, a.length);
    }

    /**
     * Sorts the specified range of the specified array of Float objects into
     * ascending order, according to the natural ordering of its elements. The
     * range to be sorted extends from index fromIndex, inclusive, to index
     * toIndex, exclusive.
     * 
     * The sorting algorithm is mergesort. This algorithm offers n*log(n)
     * performance.
     * 
     * @param a
     *            The array to be sorted.
     * @param fromIndex
     *            The index of the first element (inclusive) to be sorted.
     * @param toIndex
     *            The index of the last element (exclusive) to be sorted.
     */
    public static void sortFloatArray(Float[] a, int fromIndex, int toIndex) {
        int middle;
        if (a == null) return;
        if (fromIndex + 1 < toIndex) {
            middle = (fromIndex + toIndex) / 2;
            sortFloatArray(a, fromIndex, middle);
            sortFloatArray(a, middle, toIndex);
            mergeFloatArray(a, fromIndex, toIndex);
        }
    }

    /**
     * Implementation of the merge-part of the mergesort algorithm for Float
     * objects.
     * 
     * @param a
     *            The array to be merged.
     * @param fromIndex
     *            The index of the first element (inclusive) to be merged.
     * @param toIndex
     *            The index of the last element (exclusive) to be merged.
     */
    private static void mergeFloatArray(Float[] a, int fromIndex, int toIndex) {
        int i, j, k, middle, n;
        n = toIndex - fromIndex;
        Float[] b = new Float[n];
        k = 0;
        middle = (fromIndex + toIndex) / 2;
        for (i = fromIndex; i < middle; i++) b[k++] = a[i];
        for (j = toIndex - 1; j >= middle; j--) b[k++] = a[j];
        i = 0;
        j = n - 1;
        k = fromIndex;
        while (i <= j) {
            if (b[i].floatValue() <= b[j].floatValue()) a[k++] = b[i++]; else a[k++] = b[j--];
        }
    }
}
