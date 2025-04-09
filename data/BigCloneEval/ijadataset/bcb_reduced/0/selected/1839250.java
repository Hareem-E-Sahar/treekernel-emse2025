package org.expasy.jpl.commons.collection.sorter;

/**
 * {@code ArraySorter} can sort any generic arrays or get sorted indices.
 * 
 * @author nikitin
 * 
 * @param <T> type of element.
 * 
 * @version 1.0
 * 
 */
public final class ArraySorterImpl<T extends Comparable<T>> implements ArraySorter<T> {

    private int[] indices;

    private ArraySorterImpl() {
    }

    public static <T extends Comparable<T>> ArraySorterImpl<T> newInstance() {
        return new ArraySorterImpl<T>();
    }

    public final void sort(T[] objects) {
        if (objects.length > 0) {
            sort(objects, 0, objects.length - 1);
        }
    }

    /**
	 * Reimplementation of quicksort for performance gain.
	 * 
	 * @param objects the array of object to sort.
	 * @param from the lower limit.
	 * @param to the upper limit.
	 */
    public final void sort(T[] objects, final int from, final int to) {
        if (from >= to) {
            return;
        }
        final int mid = (from + to) / 2;
        T tmp;
        T middle = objects[mid];
        if (objects[from].compareTo(middle) > 0) {
            objects[mid] = objects[from];
            objects[from] = middle;
            middle = objects[mid];
        }
        if (middle.compareTo(objects[to]) > 0) {
            objects[mid] = objects[to];
            objects[to] = middle;
            middle = objects[mid];
            if (objects[from].compareTo(middle) > 0) {
                objects[mid] = objects[from];
                objects[from] = middle;
                middle = objects[mid];
            }
        }
        int left = from + 1;
        int right = to - 1;
        if (left >= right) {
            return;
        }
        for (; ; ) {
            while (objects[right].compareTo(middle) > 0) {
                right--;
            }
            while ((left < right) && (objects[left].compareTo(middle) <= 0)) {
                left++;
            }
            if (left < right) {
                tmp = objects[left];
                objects[left] = objects[right];
                objects[right] = tmp;
                right--;
            } else {
                break;
            }
        }
        sort(objects, from, left);
        sort(objects, left + 1, to);
    }

    private void initIndices(T[] objects) {
        indices = new int[] {};
        if (objects.length > 0) {
            indices = new int[objects.length + 1];
            for (int i = 0; i < objects.length; i++) {
                indices[i] = i;
            }
            indices[objects.length] = -1;
        }
    }

    public final int[] sortIndices(T[] objects) {
        initIndices(objects);
        if (objects.length > 0) {
            sortIndices(objects, 0, objects.length - 1);
        }
        final int[] dest = new int[objects.length];
        System.arraycopy(indices, 0, dest, 0, objects.length);
        return dest;
    }

    /**
	 * Reimplementation of quicksort for performance gain.
	 * 
	 * @param objects the array of object to sort.
	 * @param from the lower limit.
	 * @param to the upper limit.
	 */
    private final void sortIndices(T[] objects, final int from, final int to) {
        if (from >= to) {
            return;
        }
        final int pivotIndex = (from + to) / 2;
        int tmp;
        int middle = indices[pivotIndex];
        if (objects[indices[from]].compareTo(objects[middle]) > 0) {
            indices[pivotIndex] = indices[from];
            indices[from] = middle;
            middle = indices[pivotIndex];
        }
        if (objects[middle].compareTo(objects[indices[to]]) > 0) {
            indices[pivotIndex] = indices[to];
            indices[to] = middle;
            middle = indices[pivotIndex];
            if (objects[indices[from]].compareTo(objects[middle]) > 0) {
                indices[pivotIndex] = indices[from];
                indices[from] = middle;
                middle = indices[pivotIndex];
            }
        }
        int left = from + 1;
        int right = to - 1;
        if (left >= right) {
            return;
        }
        for (; ; ) {
            while (objects[indices[right]].compareTo(objects[middle]) > 0) {
                right--;
            }
            while ((left < right) && (objects[indices[left]].compareTo(objects[middle]) <= 0)) {
                left++;
            }
            if (left < right) {
                tmp = indices[left];
                indices[left] = indices[right];
                indices[right] = tmp;
                right--;
            } else {
                break;
            }
        }
        sortIndices(objects, from, left);
        sortIndices(objects, left + 1, to);
    }
}
