package org.expasy.jpl.utils.sort;

public final class JPLQuickSorter<T extends Comparable<T>> {

    private static int INDEX_NUMBER = 200;

    private static int[] indices = new int[INDEX_NUMBER];

    private T[] objects;

    public JPLQuickSorter(T[] objects) {
        this.objects = objects;
    }

    public final void sort() {
        if (objects.length > 0) {
            sort(0, objects.length - 1);
        }
    }

    /**
     * Reimplementation of quicksort for performance gain.
     * 
     * @param objects the array of object to sort.
     * @param from the lower limit.
     * @param to the upper limit.
     */
    public final void sort(int from, int to) {
        if (from >= to) {
            return;
        }
        int mid = (from + to) / 2;
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
            while (left < right && objects[left].compareTo(middle) <= 0) {
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
        sort(from, left);
        sort(left + 1, to);
    }

    public final void sortIndices() {
        if (objects.length > 0) {
            if (objects.length > indices.length + 1) {
                indices = new int[objects.length + 1];
            }
            for (int i = 0; i < objects.length; i++) {
                indices[i] = i;
            }
            indices[objects.length] = -1;
            sortIndices(0, objects.length - 1);
        }
    }

    /**
     * Reimplementation of quicksort for performance gain.
     * 
     * @param objects the array of object to sort.
     * @param from the lower limit.
     * @param to the upper limit.
     */
    public final void sortIndices(int from, int to) {
        if (from >= to) {
            return;
        }
        int pivotIndex = (from + to) / 2;
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
            while (left < right && objects[indices[left]].compareTo(objects[middle]) <= 0) {
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
        sortIndices(from, left);
        sortIndices(left + 1, to);
        return;
    }

    public final int[] getIndices() {
        int[] dest = new int[objects.length];
        System.arraycopy(indices, 0, dest, 0, objects.length);
        return dest;
    }
}
