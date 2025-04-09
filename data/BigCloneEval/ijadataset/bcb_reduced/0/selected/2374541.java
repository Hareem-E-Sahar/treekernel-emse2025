package org.processmining.analysis.traceclustering.util;

import java.util.ArrayList;

/**
 * @author christian
 * 
 */
public class SparseArray<T> {

    protected ArrayList<Integer> indices;

    protected ArrayList<T> values;

    protected T sparseValue;

    public SparseArray(T aSparseValue) {
        indices = new ArrayList<Integer>();
        values = new ArrayList<T>();
        sparseValue = aSparseValue;
    }

    public void set(int index, T value) {
        int localIndex = translateIndex(index);
        if (value.equals(sparseValue)) {
            if (localIndex >= 0) {
                indices.remove(localIndex);
            }
            return;
        } else if (localIndex < 0) {
            localIndex = insertIndex(index);
        } else {
            values.remove(localIndex);
        }
        values.add(localIndex, value);
    }

    public T get(int index) {
        int localIndex = translateIndex(index);
        if (localIndex < 0) {
            return sparseValue;
        } else {
            return values.get(localIndex);
        }
    }

    protected int translateIndex(int virtualIndex) {
        int currentIndex = findIndex(virtualIndex);
        if (currentIndex >= 0) {
            return currentIndex;
        } else {
            return -1;
        }
    }

    protected int findIndex(int virtualIndex) {
        int size = indices.size();
        if (size == 0) {
            return -1;
        } else if (size == 1) {
            if (indices.get(0) == virtualIndex) {
                return 0;
            } else {
                return -1;
            }
        }
        int low = 0;
        int high = size - 1;
        int mid, current;
        while (low <= high) {
            mid = (low + high) / 2;
            current = indices.get(mid);
            if (current > virtualIndex) {
                high = mid - 1;
            } else if (current < virtualIndex) {
                low = mid + 1;
            } else {
                return -1;
            }
        }
        return -1;
    }

    protected int insertIndex(int index) {
        for (int i = indices.size(); i > 0; i--) {
            int lowerIndex = indices.get(i - 1);
            if (lowerIndex < index) {
                indices.add(i, index);
                return i;
            }
        }
        indices.add(0, index);
        return 0;
    }
}
