package org.gvt.util;

import java.util.List;

/**
 * This class implements a generic quick sort. To use it, simply extend this
 * class and provide a comparison method.
 *
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public abstract class QuickSort {

    private List<Object> objectList;

    private Object[] objectArray;

    boolean fromList;

    public QuickSort(List<Object> objectList) {
        this.objectList = objectList;
        this.fromList = true;
    }

    public QuickSort(Object[] objectArray) {
        this.objectArray = objectArray;
        this.fromList = false;
    }

    public void quicksort() {
        int endIndex;
        if (fromList) {
            endIndex = objectList.size() - 1;
        } else {
            endIndex = objectArray.length - 1;
        }
        if (endIndex >= 0) {
            quicksort(0, endIndex);
        }
    }

    public void quicksort(int lo, int hi) {
        int i = lo;
        int j = hi;
        Object temp;
        int middleIndex = (lo + hi) / 2;
        Object middle = getObjectAt(middleIndex);
        do {
            while (compare(getObjectAt(i), middle)) i++;
            while (compare(middle, getObjectAt(j))) j--;
            if (i <= j) {
                temp = getObjectAt(i);
                setObjectAt(i, getObjectAt(j));
                setObjectAt(j, temp);
                i++;
                j--;
            }
        } while (i <= j);
        if (lo < j) quicksort(lo, j);
        if (i < hi) quicksort(i, hi);
    }

    private Object getObjectAt(int i) {
        if (fromList) {
            return objectList.get(i);
        } else {
            return objectArray[i];
        }
    }

    private void setObjectAt(int i, Object o) {
        if (fromList) {
            objectList.set(i, o);
        } else {
            objectArray[i] = o;
        }
    }

    public abstract boolean compare(Object a, Object b);
}
