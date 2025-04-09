package ararat.util;

import java.util.List;

/**
 * A class that contains several sorting routines,
 * implemented as static methods.
 * Arrays are rearranged with smallest item first,
 * using compareTo.
 * 
 * <p>This class is from "Data Structures and Algorithm Analysis in Java",
 * by Mark Allen Weiss. The source code is available online at
 * http://www.cs.fiu.edu/~weiss/dsaajava/code/DataStructures/</p>
 * 
 * <p>The original version of this class worked with arrays of
 * type <code>java.lang.Comparable</code>. This version has
 * been changed for sorting <code>java.util.List</code>s.</p>
 * 
 * @author Mark Allen Weiss
 */
public final class SortList {

    /**
     * Simple insertion sort.
     * @param a an array of Comparable items.
     */
    public static void insertionSort(List a) {
        int j;
        for (int p = 1; p < a.size(); p++) {
            Comparable tmp = (Comparable) a.get(p);
            for (j = p; j > 0 && tmp.compareTo(a.get(j - 1)) < 0; j--) a.set(j, a.get(j - 1));
            a.set(j, tmp);
        }
    }

    /**
     * Shellsort, using Shell's (poor) increments.
     * @param a an array of Comparable items.
     */
    public static void shellsort(List a) {
        int j;
        for (int gap = a.size() / 2; gap > 0; gap /= 2) for (int i = gap; i < a.size(); i++) {
            Comparable tmp = (Comparable) a.get(i);
            for (j = i; j >= gap && tmp.compareTo(a.get(j - gap)) < 0; j -= gap) a.set(j, a.get(j - gap));
            a.set(j, tmp);
        }
    }

    /**
     * Standard heapsort.
     * @param a an array of Comparable items.
     */
    public static void heapsort(List a) {
        for (int i = a.size() / 2; i >= 0; i--) percDown(a, i, a.size());
        for (int i = a.size() - 1; i > 0; i--) {
            swapReferences(a, 0, i);
            percDown(a, 0, i);
        }
    }

    /**
     * Internal method for heapsort.
     * @param i the index of an item in the heap.
     * @return the index of the left child.
     */
    private static int leftChild(int i) {
        return 2 * i + 1;
    }

    /**
     * Internal method for heapsort that is used in
     * deleteMax and buildHeap.
     * @param a an array of Comparable items.
     * @index i the position from which to percolate down.
     * @int n the logical size of the binary heap.
     */
    private static void percDown(List a, int i, int n) {
        int child;
        Comparable tmp;
        for (tmp = (Comparable) a.get(i); leftChild(i) < n; i = child) {
            child = leftChild(i);
            if (child != n - 1 && ((Comparable) a.get(child)).compareTo(a.get(child + 1)) < 0) child++;
            if (tmp.compareTo(a.get(child)) < 0) a.set(i, a.get(child)); else break;
        }
        a.set(i, tmp);
    }

    /**
     * Mergesort algorithm.
     * @param a an array of Comparable items.
     */
    public static void mergeSort(List a) {
        Comparable[] tmpArray = new Comparable[a.size()];
        mergeSort(a, tmpArray, 0, a.size() - 1);
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void mergeSort(List a, Comparable[] tmpArray, int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(a, tmpArray, left, center);
            mergeSort(a, tmpArray, center + 1, right);
            merge(a, tmpArray, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     */
    private static void merge(List a, Comparable[] tmpArray, int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;
        while (leftPos <= leftEnd && rightPos <= rightEnd) if (((Comparable) a.get(leftPos)).compareTo(a.get(rightPos)) <= 0) tmpArray[tmpPos++] = (Comparable) a.get(leftPos++); else tmpArray[tmpPos++] = (Comparable) a.get(rightPos++);
        while (leftPos <= leftEnd) tmpArray[tmpPos++] = (Comparable) a.get(leftPos++);
        while (rightPos <= rightEnd) tmpArray[tmpPos++] = (Comparable) a.get(rightPos++);
        for (int i = 0; i < numElements; i++, rightEnd--) a.set(rightEnd, tmpArray[rightEnd]);
    }

    /**
     * Quicksort algorithm.
     * @param a an array of Comparable items.
     */
    public static void quicksort(List a) {
        quicksort(a, 0, a.size() - 1);
    }

    private static final int CUTOFF = 10;

    /**
     * Method to swap to elements in an array.
     * @param a an array of objects.
     * @param index1 the index of the first object.
     * @param index2 the index of the second object.
     */
    public static final void swapReferences(List a, int index1, int index2) {
        Object tmp = a.get(index1);
        a.set(index1, a.get(index2));
        a.set(index2, tmp);
    }

    /**
     * Return median of left, center, and right.
     * Order these and hide the pivot.
     */
    private static Comparable median3(List a, int left, int right) {
        int center = (left + right) / 2;
        if (((Comparable) a.get(center)).compareTo(a.get(left)) < 0) swapReferences(a, left, center);
        if (((Comparable) a.get(right)).compareTo(a.get(left)) < 0) swapReferences(a, left, right);
        if (((Comparable) a.get(right)).compareTo(a.get(center)) < 0) swapReferences(a, center, right);
        swapReferences(a, center, right - 1);
        return (Comparable) a.get(right - 1);
    }

    /**
     * Internal quicksort method that makes recursive calls.
     * Uses median-of-three partitioning and a cutoff of 10.
     * @param a an array of Comparable items.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void quicksort(List a, int left, int right) {
        if (left + CUTOFF <= right) {
            Comparable pivot = median3(a, left, right);
            int i = left, j = right - 1;
            for (; ; ) {
                while (((Comparable) a.get(++i)).compareTo(pivot) < 0) {
                }
                while (((Comparable) a.get(--j)).compareTo(pivot) > 0) {
                }
                if (i < j) swapReferences(a, i, j); else break;
            }
            swapReferences(a, i, right - 1);
            quicksort(a, left, i - 1);
            quicksort(a, i + 1, right);
        } else insertionSort(a, left, right);
    }

    /**
     * Internal insertion sort routine for subarrays
     * that is used by quicksort.
     * @param a an array of Comparable items.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void insertionSort(List a, int left, int right) {
        for (int p = left + 1; p <= right; p++) {
            Comparable tmp = (Comparable) a.get(p);
            int j;
            for (j = p; j > left && tmp.compareTo(a.get(j - 1)) < 0; j--) a.set(j, a.get(j - 1));
            a.set(j, tmp);
        }
    }

    /**
     * Quick selection algorithm.
     * Places the kth smallest item in a[k-1].
     * @param a an array of Comparable items.
     * @param k the desired rank (1 is minimum) in the entire array.
     */
    public static void quickSelect(List a, int k) {
        quickSelect(a, 0, a.size() - 1, k);
    }

    /**
     * Internal selection method that makes recursive calls.
     * Uses median-of-three partitioning and a cutoff of 10.
     * Places the kth smallest item in a[k-1].
     * @param a an array of Comparable items.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     * @param k the desired index (1 is minimum) in the entire array.
     */
    private static void quickSelect(List a, int left, int right, int k) {
        if (left + CUTOFF <= right) {
            Comparable pivot = median3(a, left, right);
            int i = left, j = right - 1;
            for (; ; ) {
                while (((Comparable) a.get(++i)).compareTo(pivot) < 0) {
                }
                while (((Comparable) a.get(--j)).compareTo(pivot) > 0) {
                }
                if (i < j) swapReferences(a, i, j); else break;
            }
            swapReferences(a, i, right - 1);
            if (k <= i) quickSelect(a, left, i - 1, k); else if (k > i + 1) quickSelect(a, i + 1, right, k);
        } else insertionSort(a, left, right);
    }

    private static void checkSort(Integer[] a) {
        for (int i = 0; i < a.length; i++) if (a[i].intValue() != i) System.out.println("Error at " + i);
        System.out.println("Finished checksort");
    }
}
