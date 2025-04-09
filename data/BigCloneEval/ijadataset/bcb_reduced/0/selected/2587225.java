package com.jme3.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Quick and merge sort implementations that create no garbage, unlike {@link
 * Arrays#sort}. The merge sort is stable, the quick sort is not.
 */
public class SortUtil {

    /** 
     * The size at or below which we will use insertion sort because it's
     * probably faster. 
     */
    private static final int INSERTION_SORT_THRESHOLD = 7;

    /**
 procedure optimizedGnomeSort(a[])
    pos := 1
    last := 0
    while pos < length(a)
        if (a[pos] >= a[pos-1])
            if (last != 0)
                pos := last
                last := 0
            end if
            pos := pos + 1
        else
            swap a[pos] and a[pos-1]
            if (pos > 1)
                if (last == 0)
                    last := pos
                end if
                pos := pos - 1
            else
                pos := pos + 1
            end if
        end if
    end while
end procedure
     */
    public static void gsort(Object[] a, Comparator comp) {
        int pos = 1;
        int last = 0;
        int length = a.length;
        while (pos < length) {
            if (comp.compare(a[pos], a[pos - 1]) >= 0) {
                if (last != 0) {
                    pos = last;
                    last = 0;
                }
                pos++;
            } else {
                Object tmp = a[pos];
                a[pos] = a[pos - 1];
                a[pos - 1] = tmp;
                if (pos > 1) {
                    if (last == 0) {
                        last = pos;
                    }
                    pos--;
                } else {
                    pos++;
                }
            }
        }
    }

    private static void test(Float[] original, Float[] sorted, Comparator<Float> ic) {
        long time, dt;
        time = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            System.arraycopy(original, 0, sorted, 0, original.length);
            gsort(sorted, ic);
        }
        dt = System.nanoTime() - time;
        System.out.println("GSort " + (dt / 1000000.0) + " ms");
        time = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            System.arraycopy(original, 0, sorted, 0, original.length);
            qsort(sorted, ic);
        }
        dt = System.nanoTime() - time;
        System.out.println("QSort " + (dt / 1000000.0) + " ms");
        time = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            System.arraycopy(original, 0, sorted, 0, original.length);
            msort(original, sorted, ic);
        }
        dt = System.nanoTime() - time;
        System.out.println("MSort " + (dt / 1000000.0) + " ms");
        time = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            System.arraycopy(original, 0, sorted, 0, original.length);
            Arrays.sort(sorted, ic);
        }
        dt = System.nanoTime() - time;
        System.out.println("ASort " + (dt / 1000000.0) + " ms");
    }

    public static void main(String[] args) {
        Comparator<Float> ic = new Comparator<Float>() {

            public int compare(Float o1, Float o2) {
                return (int) (o1 - o2);
            }
        };
        Float[] original = new Float[] { 2f, 1f, 5f, 3f, 4f, 6f, 8f, 9f, 11f, 10f, 12f, 13f, 14f, 15f, 7f, 19f, 20f, 18f, 16f, 17f, 21f, 23f, 22f, 24f, 25f, 27f, 26f, 29f, 28f, 30f, 31f };
        Float[] sorted = new Float[original.length];
        while (true) {
            test(original, sorted, ic);
        }
    }

    /**
     * Quick sorts the supplied array using the specified comparator.
     */
    public static void qsort(Object[] a, Comparator comp) {
        qsort(a, 0, a.length - 1, comp);
    }

    /**
     * Quick sorts the supplied array using the specified comparator.
     *
     * @param lo0 the index of the lowest element to include in the sort.
     * @param hi0 the index of the highest element to include in the sort.
     */
    @SuppressWarnings("unchecked")
    public static void qsort(Object[] a, int lo0, int hi0, Comparator comp) {
        if (hi0 <= lo0) {
            return;
        }
        Object t;
        if (hi0 - lo0 == 1) {
            if (comp.compare(a[hi0], a[lo0]) < 0) {
                t = a[lo0];
                a[lo0] = a[hi0];
                a[hi0] = t;
            }
            return;
        }
        Object mid = a[(lo0 + hi0) / 2];
        int lo = lo0 - 1, hi = hi0 + 1;
        for (; ; ) {
            while (comp.compare(a[++lo], mid) < 0) ;
            while (comp.compare(mid, a[--hi]) < 0) ;
            if (hi > lo) {
                t = a[lo];
                a[lo] = a[hi];
                a[hi] = t;
            } else {
                break;
            }
        }
        if (lo0 < lo - 1) {
            qsort(a, lo0, lo - 1, comp);
        }
        if (hi + 1 < hi0) {
            qsort(a, hi + 1, hi0, comp);
        }
    }

    public static void qsort(int[] a, int lo0, int hi0, Comparator comp) {
        if (hi0 <= lo0) {
            return;
        }
        int t;
        if (hi0 - lo0 == 1) {
            if (comp.compare(a[hi0], a[lo0]) < 0) {
                t = a[lo0];
                a[lo0] = a[hi0];
                a[hi0] = t;
            }
            return;
        }
        int mid = a[(lo0 + hi0) / 2];
        int lo = lo0 - 1, hi = hi0 + 1;
        for (; ; ) {
            while (comp.compare(a[++lo], mid) < 0) ;
            while (comp.compare(mid, a[--hi]) < 0) ;
            if (hi > lo) {
                t = a[lo];
                a[lo] = a[hi];
                a[hi] = t;
            } else {
                break;
            }
        }
        if (lo0 < lo - 1) {
            qsort(a, lo0, lo - 1, comp);
        }
        if (hi + 1 < hi0) {
            qsort(a, hi + 1, hi0, comp);
        }
    }

    /**
     * Merge sort
     */
    public static void msort(Object[] src, Object[] dest, Comparator comp) {
        msort(src, dest, 0, src.length - 1, comp);
    }

    /**
     * Merge sort
     * 
     * @param src Source array
     * @param dest Destination array
     * @param low Index of beginning element
     * @param high Index of end element
     * @param comp Comparator
     */
    public static void msort(Object[] src, Object[] dest, int low, int high, Comparator comp) {
        if (low < high) {
            int center = (low + high) / 2;
            msort(src, dest, low, center, comp);
            msort(src, dest, center + 1, high, comp);
            merge(src, dest, low, center + 1, high, comp);
        }
    }

    private static void merge(Object[] src, Object[] dest, int low, int middle, int high, Comparator comp) {
        int leftEnd = middle - 1;
        int pos = low;
        int numElements = high - low + 1;
        while (low <= leftEnd && middle <= high) {
            if (comp.compare(src[low], src[middle]) <= 0) {
                dest[pos++] = src[low++];
            } else {
                dest[pos++] = src[middle++];
            }
        }
        while (low <= leftEnd) {
            dest[pos++] = src[low++];
        }
        while (middle <= high) {
            dest[pos++] = src[middle++];
        }
        for (int i = 0; i < numElements; i++, high--) {
            src[high] = dest[high];
        }
    }
}
