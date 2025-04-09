package org.furthurnet.furi;

import java.util.*;

public class SortUtil {

    public static void qsort(String[] array, boolean ascending) {
        qsort(array, 0, array.length - 1, ascending);
    }

    private static void qsort(String[] array, int beginIndex, int endIndex, boolean ascending) {
        if (beginIndex >= endIndex) return;
        swap(array, beginIndex, (beginIndex + endIndex) / 2);
        int last = beginIndex;
        for (int i = beginIndex + 1; i <= endIndex; i++) {
            if (ascending) {
                if (array[i].compareTo(array[beginIndex]) < 0) {
                    swap(array, ++last, i);
                }
            } else {
                if (array[i].compareTo(array[beginIndex]) > 0) {
                    swap(array, ++last, i);
                }
            }
        }
        swap(array, beginIndex, last);
        qsort(array, beginIndex, last - 1, ascending);
        qsort(array, last + 1, endIndex, ascending);
    }

    private static void swap(String[] array, int i, int j) {
        String obj = array[i];
        array[i] = array[j];
        array[j] = obj;
    }

    public static void qsort(Vector array, boolean ascending) {
        qsort(array, 0, array.size() - 1, ascending);
    }

    private static void qsort(Vector array, int beginIndex, int endIndex, boolean ascending) {
        if (beginIndex >= endIndex) return;
        swap(array, beginIndex, (beginIndex + endIndex) / 2);
        int last = beginIndex;
        for (int i = beginIndex + 1; i <= endIndex; i++) {
            IComparable obj1 = (IComparable) array.elementAt(i);
            IComparable obj2 = (IComparable) array.elementAt(beginIndex);
            if (ascending) {
                if (obj1.compares(obj2) < 0) {
                    swap(array, ++last, i);
                }
            } else {
                if (obj1.compares(obj2) > 0) {
                    swap(array, ++last, i);
                }
            }
        }
        swap(array, beginIndex, last);
        qsort(array, beginIndex, last - 1, ascending);
        qsort(array, last + 1, endIndex, ascending);
    }

    private static void swap(Vector array, int i, int j) {
        Object obj = array.elementAt(i);
        array.setElementAt(array.elementAt(j), i);
        array.setElementAt(obj, j);
    }

    public static void orderedInsert(Vector orderedArray, IComparable obj) {
        int size = orderedArray.size();
        if (size == 0) {
            orderedArray.addElement(obj);
            return;
        }
        int begin = 0;
        int end = size - 1;
        int mid = 0, cmp = 0;
        while (begin <= end) {
            mid = (end + begin) / 2;
            IComparable obj2 = (IComparable) orderedArray.elementAt(mid);
            cmp = obj.compares(obj2);
            if (cmp == 0) {
                orderedArray.insertElementAt(obj, mid);
                return;
            } else if (cmp < 0) {
                end = mid - 1;
            } else {
                begin = mid + 1;
            }
        }
        if (cmp < 0) {
            orderedArray.insertElementAt(obj, mid);
        } else {
            orderedArray.insertElementAt(obj, mid + 1);
        }
    }

    public static int orderedFind(Vector orderedArray, IComparable obj) {
        int size = orderedArray.size();
        if (size == 0) {
            return -1;
        }
        int begin = 0;
        int end = size - 1;
        int mid, cmp;
        while (begin <= end) {
            mid = (end + begin) / 2;
            IComparable obj2 = (IComparable) orderedArray.elementAt(mid);
            cmp = obj.compares(obj2);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                end = mid - 1;
            } else {
                begin = mid + 1;
            }
        }
        return -1;
    }

    public static void main(String args[]) {
        Vector a = new Vector();
        a.addElement("22");
        a.addElement("5555");
        a.addElement("11");
        a.addElement("222");
        a.addElement("111");
        a.addElement("1");
        System.out.println(a);
        qsort(a, true);
        System.out.println(a);
        qsort(a, false);
        System.out.println(a);
    }
}
