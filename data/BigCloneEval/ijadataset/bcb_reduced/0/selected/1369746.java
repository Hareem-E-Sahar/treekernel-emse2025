package com.inetmon.jn.vlananalyzerview.tools;

import java.lang.reflect.Array;
import java.util.Comparator;

/**
 * A modified version of Java's Merge Sort
 *  
 * @author Muhammad Fermi Pasha
 * 
 */
public class jNetmonArrays {

    private jNetmonArrays() {
    }

    /**
	 * Tuning parameter: list size at or below which insertion sort will be used
	 * in preference to mergesort or quicksort.
	 */
    private static final int INSERTIONSORT_THRESHOLD = 7;

    /**
	 * Clones an array within the specified bounds. This method assumes that a
	 * is an array.
	 */
    private static <T> T[] cloneSubarray(T[] a, int from, int to) {
        int n = to - from;
        T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), n);
        System.arraycopy(a, from, result, 0, n);
        return result;
    }

    /**
	 * An alternative soring method applying a modified quick sort algorithm.
	 * 
	 * @param a
	 * @param c
	 */
    public static void sortQuick(Object[] a, Comparator c) {
        sortQuick(a, 0, a.length, c);
    }

    public static void sortQuick(Object[] a, int fromIndex, int toIndex, Comparator c) {
        if (fromIndex > toIndex) throw new IllegalArgumentException("fromIndex " + fromIndex + " > toIndex " + toIndex);
        if (fromIndex < 0) throw new ArrayIndexOutOfBoundsException();
        for (int chunk = fromIndex; chunk < toIndex; chunk += 6) {
            int end = Math.min(chunk + 6, toIndex);
            for (int i = chunk + 1; i < end; i++) {
                if (compare(a[i - 1], a[i], c) > 0) {
                    int j = i;
                    Object elem = a[j];
                    do {
                        a[j] = a[j - 1];
                        j--;
                    } while (j > chunk && compare(a[j - 1], elem, c) > 0);
                    a[j] = elem;
                }
            }
        }
        int len = toIndex - fromIndex;
        if (len <= 6) return;
        Object[] src = a;
        Object[] dest = new Object[len];
        Object[] t = null;
        int srcDestDiff = -fromIndex;
        for (int size = 6; size < len; size <<= 1) {
            for (int start = fromIndex; start < toIndex; start += size << 1) {
                int mid = start + size;
                int end = Math.min(toIndex, mid + size);
                if (mid >= end || compare(src[mid - 1], src[mid], c) <= 0) {
                    System.arraycopy(src, start, dest, start + srcDestDiff, end - start);
                } else if (compare(src[start], src[end - 1], c) > 0) {
                    System.arraycopy(src, start, dest, end - size + srcDestDiff, size);
                    System.arraycopy(src, mid, dest, start + srcDestDiff, end - mid);
                } else {
                    int p1 = start;
                    int p2 = mid;
                    int i = start + srcDestDiff;
                    while (p1 < mid && p2 < end) {
                        dest[i++] = src[(compare(src[p1], src[p2], c) <= 0 ? p1++ : p2++)];
                    }
                    if (p1 < mid) System.arraycopy(src, p1, dest, i, mid - p1); else System.arraycopy(src, p2, dest, i, end - p2);
                }
            }
            t = src;
            src = dest;
            dest = t;
            fromIndex += srcDestDiff;
            toIndex += srcDestDiff;
            srcDestDiff = -srcDestDiff;
        }
        if (src != a) {
            System.arraycopy(src, 0, a, srcDestDiff, toIndex);
        }
    }

    private static final int compare(Object o1, Object o2, Comparator c) {
        return c == null ? ((Comparable) o1).compareTo(o2) : c.compare(o1, o2);
    }

    /**
	 * Sorts the specified array of objects according to the order induced by
	 * the specified comparator. All elements in the array must be <i>mutually
	 * comparable</i> by the specified comparator (that is,
	 * <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt>
	 * for any elements <tt>e1</tt> and <tt>e2</tt> in the array).
	 * <p>
	 * 
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be
	 * reordered as a result of the sort.
	 * <p>
	 * 
	 * The sorting algorithm is a modified mergesort (in which the merge is
	 * omitted if the highest element in the low sublist is less than the lowest
	 * element in the high sublist). This algorithm offers guaranteed n*log(n)
	 * performance.
	 * 
	 * @param a
	 *            the array to be sorted.
	 * @param c
	 *            the comparator to determine the order of the array. A
	 *            <tt>null</tt> value indicates that the elements' <i>natural
	 *            ordering</i> should be used.
	 * @throws ClassCastException
	 *             if the array contains elements that are not <i>mutually
	 *             comparable</i> using the specified comparator.
	 * @see Comparator
	 */
    public static <T> void sort(T[] a, int[] mapToSort, Comparator<? super T> c) {
        T[] aux = (T[]) a.clone();
        int[] mapClone = (int[]) mapToSort.clone();
        if (c == null) mergeSort(aux, a, mapClone, mapToSort, 0, a.length, 0); else mergeSort(aux, a, mapClone, mapToSort, 0, a.length, 0, c);
    }

    /**
	 * Src is the source array that starts at index 0 Dest is the (possibly
	 * larger) array destination with a possible offset low is the index in dest
	 * to start sorting high is the end index in dest to end sorting off is the
	 * offset to generate corresponding low, high in src
	 */
    private static void mergeSort(Object[] src, Object[] dest, int[] srcMap, int[] dstMap, int low, int high, int off) {
        int length = high - low;
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--) {
                    swap(dest, j, j - 1);
                    swapMap(dstMap, j, j - 1);
                }
            }
            return;
        }
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >> 1;
        mergeSort(dest, src, dstMap, srcMap, low, mid, -off);
        mergeSort(dest, src, dstMap, srcMap, mid, high, -off);
        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            System.arraycopy(srcMap, low, dstMap, destLow, length);
            return;
        }
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && ((Comparable) src[p]).compareTo(src[q]) <= 0) {
                dest[i] = src[p];
                dstMap[i] = srcMap[p];
                p++;
            } else {
                dest[i] = src[q];
                dstMap[i] = srcMap[q];
                q++;
            }
        }
    }

    /**
	 * Src is the source array that starts at index 0 Dest is the (possibly
	 * larger) array destination with a possible offset low is the index in dest
	 * to start sorting high is the end index in dest to end sorting off is the
	 * offset into src corresponding to low in dest
	 */
    private static void mergeSort(Object[] src, Object[] dest, int[] srcMap, int[] dstMap, int low, int high, int off, Comparator c) {
        int length = high - low;
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++) for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--) {
                swap(dest, j, j - 1);
                swapMap(dstMap, j, j - 1);
            }
            return;
        }
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >> 1;
        mergeSort(dest, src, dstMap, srcMap, low, mid, -off, c);
        mergeSort(dest, src, dstMap, srcMap, mid, high, -off, c);
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            System.arraycopy(srcMap, low, dstMap, destLow, length);
            return;
        }
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) {
                dest[i] = src[p];
                dstMap[i] = srcMap[p];
                p++;
            } else {
                dest[i] = src[q];
                dstMap[i] = srcMap[q];
                q++;
            }
        }
    }

    /**
	 * Swaps x[a] with x[b].
	 */
    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void swapMap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
