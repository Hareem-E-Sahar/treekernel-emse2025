package com.onehao.middle.l4;

import java.util.Arrays;

public class MyBinarySearch {

    public static int search(int[] array, int value) {
        int end = array.length - 1;
        int start = 0;
        int middle;
        while (start < end) {
            middle = (start + end) / 2;
            if (value > array[middle]) {
                start = middle + 1;
            } else if (value < array[middle]) {
                end = middle - 1;
            } else {
                return middle;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] a = new int[] { 2, 3, 4, 7, 8, 9 };
        int i = search(a, 8);
        System.out.println(i);
    }
}
