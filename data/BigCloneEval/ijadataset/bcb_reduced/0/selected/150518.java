package com.mindbright.util;

public final class ArraySort {

    public static interface Comparable {

        public int compareTo(Comparable other);
    }

    public static void sort(Comparable[] arr) {
        sort(arr, 0, arr.length);
    }

    public static void sort(Comparable[] arr, int start, int end) {
        Comparable aux[] = (Comparable[]) arr.clone();
        mergeSort(aux, arr, start, end);
    }

    private static void mergeSort(Comparable[] src, Comparable[] dest, int low, int high) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; i++) for (int j = i; j > low && dest[j - 1].compareTo(dest[j]) > 0; j--) swap(dest, j, j - 1);
            return;
        }
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid);
        mergeSort(dest, src, mid, high);
        if (src[mid - 1].compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && src[p].compareTo(src[q]) <= 0) dest[i] = src[p++]; else dest[i] = src[q++];
        }
    }

    private static void swap(Object list[], int a, int b) {
        Object tmp = list[a];
        list[a] = list[b];
        list[b] = tmp;
    }
}
