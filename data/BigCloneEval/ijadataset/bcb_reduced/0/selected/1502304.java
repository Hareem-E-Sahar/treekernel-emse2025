package cunei.sort;

import java.util.Comparator;

public class MergeSort {

    private static int lower(int from, int to, int val, Comparator<Integer> comparator) {
        int len = to - from, half;
        while (len > 0) {
            half = len / 2;
            int mid = from + half;
            if (comparator.compare(mid, val) < 0) {
                from = mid + 1;
                len = len - half - 1;
            } else len = half;
        }
        return from;
    }

    private static void merge(int from, int pivot, int to, int len1, int len2, Comparator<Integer> comparator, Swapable swapper) {
        if (len1 == 0 || len2 == 0) return;
        if (len1 + len2 == 2) {
            if (comparator.compare(pivot, from) < 0) swapper.swap(pivot, from);
            return;
        }
        int firstCut, secondCut;
        int len11, len22;
        if (len1 > len2) {
            len11 = len1 / 2;
            firstCut = from + len11;
            secondCut = lower(pivot, to, firstCut, comparator);
            len22 = secondCut - pivot;
        } else {
            len22 = len2 / 2;
            secondCut = pivot + len22;
            firstCut = upper(from, pivot, secondCut, comparator);
            len11 = firstCut - from;
        }
        rotate(firstCut, pivot, secondCut, swapper);
        int newMid = firstCut + len22;
        merge(from, firstCut, newMid, len11, len22, comparator, swapper);
        merge(newMid, secondCut, to, len1 - len11, len2 - len22, comparator, swapper);
    }

    private static void reverse(int from, int to, Swapable swapper) {
        while (from < to) swapper.swap(from++, to--);
    }

    private static void rotate(int from, int mid, int to, Swapable swapper) {
        reverse(from, mid - 1, swapper);
        reverse(mid, to - 1, swapper);
        reverse(from, to - 1, swapper);
    }

    public static void sort(int from, int to, Comparator<Integer> comparator, Swapable swapper) {
        sortr(from, to - 1, comparator, swapper);
    }

    private static void sortr(int from, int to, Comparator<Integer> comparator, Swapable swapper) {
        if (to - from < 8) {
            InsertionSort.sort(from, to + 1, comparator, swapper);
            return;
        }
        int middle = (from + to) / 2;
        sortr(from, middle, comparator, swapper);
        sortr(middle, to, comparator, swapper);
        merge(from, middle, to, middle - from, to - middle, comparator, swapper);
    }

    private static int upper(int from, int to, int val, Comparator<Integer> comparator) {
        int len = to - from, half;
        while (len > 0) {
            half = len / 2;
            int mid = from + half;
            if (comparator.compare(val, mid) < 0) len = half; else {
                from = mid + 1;
                len = len - half - 1;
            }
        }
        return from;
    }
}
