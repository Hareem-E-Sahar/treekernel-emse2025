package org.tigr.util;

public class Sort {

    public static int[] mergeSort(int[] s) {
        return Sort.mergeSort(s, 0, s.length - 1);
    }

    public static int[] mergeSort(int[] s, int f, int l) {
        if (f < l) {
            int m = (f + l) / 2;
            Sort.mergeSort(s, f, m);
            Sort.mergeSort(s, m + 1, l);
            merge(s, f, m, l);
        }
        return s;
    }

    public static void merge(int[] s, int f, int m, int l) {
        int[] temp = new int[s.length];
        int f1 = f;
        int l1 = m;
        int f2 = m + 1;
        int l2 = l;
        int index = f1;
        for (; (f1 <= l1) && (f2 <= l2); ++index) {
            if (s[f1] < s[f2]) {
                temp[index] = s[f1];
                ++f1;
            } else {
                temp[index] = s[f2];
                ++f2;
            }
        }
        for (; f1 <= l1; ++f1, ++index) temp[index] = s[f1];
        for (; f2 <= l2; ++f2, ++index) temp[index] = s[f2];
        for (index = f; index <= l; ++index) s[index] = temp[index];
    }
}
