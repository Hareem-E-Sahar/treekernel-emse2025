package ararat.util;

/**
 * A class that contains several sorting routines,
 * implemented as static methods.
 * Arrays are rearranged with smallest item first,
 * using compareTo.
 * 
 * This class is from "Data Structures and Algorithm Analysis in Java",
 * by Mark Allen Weiss. The source code is available online at
 * http://www.cs.fiu.edu/~weiss/dsaajava/code/DataStructures/
 * 
 * @author Mark Allen Weiss
 */
public final class Sort {

    /**
     * Simple insertion sort.
     * @param a an array of Comparable items.
     */
    public static void insertionSort(Comparable[] a) {
        int j;
        for (int p = 1; p < a.length; p++) {
            Comparable tmp = a[p];
            for (j = p; j > 0 && tmp.compareTo(a[j - 1]) < 0; j--) a[j] = a[j - 1];
            a[j] = tmp;
        }
    }

    /**
     * Shellsort, using Shell's (poor) increments.
     * @param a an array of Comparable items.
     */
    public static void shellsort(Comparable[] a) {
        int j;
        for (int gap = a.length / 2; gap > 0; gap /= 2) for (int i = gap; i < a.length; i++) {
            Comparable tmp = a[i];
            for (j = i; j >= gap && tmp.compareTo(a[j - gap]) < 0; j -= gap) a[j] = a[j - gap];
            a[j] = tmp;
        }
    }

    /**
     * Standard heapsort.
     * @param a an array of Comparable items.
     */
    public static void heapsort(Comparable[] a) {
        for (int i = a.length / 2; i >= 0; i--) percDown(a, i, a.length);
        for (int i = a.length - 1; i > 0; i--) {
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
    private static void percDown(Comparable[] a, int i, int n) {
        int child;
        Comparable tmp;
        for (tmp = a[i]; leftChild(i) < n; i = child) {
            child = leftChild(i);
            if (child != n - 1 && a[child].compareTo(a[child + 1]) < 0) child++;
            if (tmp.compareTo(a[child]) < 0) a[i] = a[child]; else break;
        }
        a[i] = tmp;
    }

    /**
     * Mergesort algorithm.
     * @param a an array of Comparable items.
     */
    public static void mergeSort(Comparable[] a) {
        Comparable[] tmpArray = new Comparable[a.length];
        mergeSort(a, tmpArray, 0, a.length - 1);
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void mergeSort(Comparable[] a, Comparable[] tmpArray, int left, int right) {
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
    private static void merge(Comparable[] a, Comparable[] tmpArray, int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;
        while (leftPos <= leftEnd && rightPos <= rightEnd) if (a[leftPos].compareTo(a[rightPos]) <= 0) tmpArray[tmpPos++] = a[leftPos++]; else tmpArray[tmpPos++] = a[rightPos++];
        while (leftPos <= leftEnd) tmpArray[tmpPos++] = a[leftPos++];
        while (rightPos <= rightEnd) tmpArray[tmpPos++] = a[rightPos++];
        for (int i = 0; i < numElements; i++, rightEnd--) a[rightEnd] = tmpArray[rightEnd];
    }

    /**
     * Quicksort algorithm.
     * @param a an array of Comparable items.
     */
    public static void quicksort(Comparable[] a) {
        quicksort(a, 0, a.length - 1);
    }

    private static final int CUTOFF = 10;

    /**
     * Method to swap to elements in an array.
     * @param a an array of objects.
     * @param index1 the index of the first object.
     * @param index2 the index of the second object.
     */
    public static final void swapReferences(Object[] a, int index1, int index2) {
        Object tmp = a[index1];
        a[index1] = a[index2];
        a[index2] = tmp;
    }

    /**
     * Return median of left, center, and right.
     * Order these and hide the pivot.
     */
    private static Comparable median3(Comparable[] a, int left, int right) {
        int center = (left + right) / 2;
        if (a[center].compareTo(a[left]) < 0) swapReferences(a, left, center);
        if (a[right].compareTo(a[left]) < 0) swapReferences(a, left, right);
        if (a[right].compareTo(a[center]) < 0) swapReferences(a, center, right);
        swapReferences(a, center, right - 1);
        return a[right - 1];
    }

    /**
     * Internal quicksort method that makes recursive calls.
     * Uses median-of-three partitioning and a cutoff of 10.
     * @param a an array of Comparable items.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void quicksort(Comparable[] a, int left, int right) {
        if (left + CUTOFF <= right) {
            Comparable pivot = median3(a, left, right);
            int i = left, j = right - 1;
            for (; ; ) {
                while (a[++i].compareTo(pivot) < 0) {
                }
                while (a[--j].compareTo(pivot) > 0) {
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
    private static void insertionSort(Comparable[] a, int left, int right) {
        for (int p = left + 1; p <= right; p++) {
            Comparable tmp = a[p];
            int j;
            for (j = p; j > left && tmp.compareTo(a[j - 1]) < 0; j--) a[j] = a[j - 1];
            a[j] = tmp;
        }
    }

    /**
     * Quick selection algorithm.
     * Places the kth smallest item in a[k-1].
     * @param a an array of Comparable items.
     * @param k the desired rank (1 is minimum) in the entire array.
     */
    public static void quickSelect(Comparable[] a, int k) {
        quickSelect(a, 0, a.length - 1, k);
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
    private static void quickSelect(Comparable[] a, int left, int right, int k) {
        if (left + CUTOFF <= right) {
            Comparable pivot = median3(a, left, right);
            int i = left, j = right - 1;
            for (; ; ) {
                while (a[++i].compareTo(pivot) < 0) {
                }
                while (a[--j].compareTo(pivot) > 0) {
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
