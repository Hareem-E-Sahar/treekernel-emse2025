package net.cachapa.weightwatch;

import java.util.Vector;

/**
 * Implements the quicksort algorithm.
 * Sorts Vectors of objects that implement the Comparable interface.
 * The sort is based on Comparable.compare(obj) method.
 * @see Comparable
 */
public class QuickSorter {

    /**
   * performs the sort.
   *
   * @param vect the Objects to be sorted.
   */
    public static void sort(Vector vect) {
        quickSort(vect, 0, vect.size() - 1);
        insertionSort(vect);
    }

    static void swap(Vector vect, int i, int j) {
        Value x = (Value) vect.elementAt(i);
        vect.setElementAt(vect.elementAt(j), i);
        vect.setElementAt(x, j);
    }

    static void quickSort(Vector vect, int el, int yu) {
        if (yu - el < 16) {
            return;
        }
        int m, pivot, other;
        m = (yu + el) / 2;
        if (((Value) vect.elementAt(el)).compareTo(vect.elementAt(yu)) < 0) {
            pivot = el;
            other = yu;
        } else {
            pivot = yu;
            other = el;
        }
        if (((Value) vect.elementAt(pivot)).compareTo(vect.elementAt(m)) < 0) {
            if (((Value) vect.elementAt(m)).compareTo(vect.elementAt(other)) < 0) {
                pivot = m;
            } else {
                pivot = other;
            }
        }
        swap(vect, el, pivot);
        int i, j;
        i = el + 1;
        j = yu - 1;
        while (true) {
            while (((Value) vect.elementAt(el)).compareTo(vect.elementAt(i)) < 0) {
                i++;
            }
            while (((Value) vect.elementAt(el)).compareTo(vect.elementAt(j)) > 0) {
                j--;
            }
            if (i >= j) {
                break;
            }
            swap(vect, i, j);
            i++;
            j--;
        }
        swap(vect, el, j);
        if (j - el < yu - i) {
            quickSort(vect, el, j - 1);
            quickSort(vect, i, yu);
        } else {
            quickSort(vect, i, yu);
            quickSort(vect, el, j - 1);
        }
    }

    static void insertionSort(Vector vect) {
        int i, j;
        for (i = 1; i < vect.size(); i++) {
            Value x = (Value) vect.elementAt(i);
            for (j = i; (j > 0) && ((Value) vect.elementAt(j - 1)).compareTo(x) > 0; j--) {
                vect.setElementAt(vect.elementAt(j - 1), j);
            }
            vect.setElementAt(x, j);
        }
    }
}
