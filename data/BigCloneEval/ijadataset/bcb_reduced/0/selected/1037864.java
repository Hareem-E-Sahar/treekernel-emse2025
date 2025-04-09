package com.lts.util;

import java.util.Vector;

public class VectorSorter {

    public static void sort(Vector v, CompareMethod c) {
        if (null == v || v.size() <= 1) return;
        quickSort(v, c, 0, v.size() - 1);
    }

    public static void quickSort(Vector v, CompareMethod c, int start, int stop) {
        if (start >= stop) return;
        int low = start;
        int high = stop;
        int mid = (start + stop) / 2;
        Object pivot = v.elementAt(mid);
        v.setElementAt(v.elementAt(stop), mid);
        v.setElementAt(pivot, stop);
        while (low < high) {
            Object o = v.elementAt(low);
            int theResult = c.compare(o, pivot);
            while (low < high && CompareMethod.ELEMENT1_AFTER_ELEMENT2 != theResult) {
                low++;
                o = v.elementAt(low);
                theResult = c.compare(o, pivot);
            }
            o = v.elementAt(high);
            theResult = c.compare(o, pivot);
            while (low < high && CompareMethod.ELEMENT1_BEFORE_ELEMENT2 != theResult) {
                high--;
                o = v.elementAt(high);
                theResult = c.compare(o, pivot);
            }
            if (low < high) {
                Object temp = v.elementAt(low);
                v.setElementAt(v.elementAt(high), low);
                v.setElementAt(temp, high);
            }
        }
        v.setElementAt(v.elementAt(high), stop);
        v.setElementAt(pivot, high);
        quickSort(v, c, start, low - 1);
        quickSort(v, c, high + 1, stop);
    }
}
