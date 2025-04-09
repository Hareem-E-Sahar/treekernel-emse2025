package org.expasy.jpl.utils.sort;

import java.util.Arrays;

/**
 * Provides index- and value- sorting operations on primitive numeric arrays.
 */
public class SimpleTypeArray {

    /**
	 * Map the objects found at given indices
	 * 
	 * @param objects the objects to map.
	 * @param indices the indices of objects array.
	 * @return an array of mapped {@code Object}s.
	 */
    public static Object[] mapping(Object[] objects, int[] indices) {
        if (indices.length > objects.length) {
            throw new IllegalArgumentException("too many indices");
        }
        Object[] values = new Object[indices.length];
        int i = 0;
        for (int index : indices) {
            values[i++] = objects[index];
        }
        return values;
    }

    public static double[] mapping(double[] ints, int[] indices) {
        if (indices.length > ints.length) {
            throw new IllegalArgumentException("too many indices");
        }
        double[] values = new double[indices.length];
        int i = 0;
        for (int index : indices) {
            values[i++] = ints[index];
        }
        return values;
    }

    public static int[] mapping(int[] array, int[] indices) {
        if (indices.length > array.length) {
            throw new IllegalArgumentException("too many indices");
        }
        int[] values = new int[indices.length];
        int i = 0;
        for (int index : indices) {
            values[i++] = array[index];
        }
        return values;
    }

    public static int[] convert2ints(Object[] objects) {
        int[] values = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            values[i] = Integer.parseInt(objects[i].toString());
        }
        return values;
    }

    public static Integer[] ints2Integers(int[] ints) {
        Integer[] values = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            values[i] = new Integer(ints[i]);
        }
        return values;
    }

    public static Double[] doubles2Doubles(double[] dbls) {
        Double[] values = new Double[dbls.length];
        for (int i = 0; i < dbls.length; i++) {
            values[i] = new Double(dbls[i]);
        }
        return values;
    }

    /**
	 * Sort int values in ascending order.
	 * 
	 * @param array the int array to sort.
	 * @return the sorted array.
	 */
    public static int[] sortUp(int[] array) {
        Arrays.sort(array);
        return array;
    }

    /**
	 * Sort int values in descending order.
	 * 
	 * @param array the int array to sort.
	 * @return the sorted array.
	 */
    public static int[] sortDown(int[] array) {
        Arrays.sort(array);
        int halfLen = array.length / 2;
        int lastIdx = array.length - 1;
        int temp;
        for (int i = 0; i < halfLen; i++) {
            temp = array[i];
            array[i] = array[lastIdx - i];
            array[lastIdx - i] = temp;
        }
        return array;
    }

    /**
	 * Sort double values in ascending order.
	 * 
	 * @param array the double array to sort.
	 * @return the sorted array.
	 */
    public static double[] sortUp(double[] array) {
        Arrays.sort(array);
        return array;
    }

    /**
	 * Sort double values in descending order.
	 * 
	 * @param array the double array to sort.
	 * @return the sorted array.
	 */
    public static double[] sortDown(double[] array) {
        Arrays.sort(array);
        int halfLen = array.length / 2;
        int lastIdx = array.length - 1;
        double temp;
        for (int i = 0; i < halfLen; i++) {
            temp = array[i];
            array[i] = array[lastIdx - i];
            array[lastIdx - i] = temp;
        }
        return array;
    }

    /**
	 * Sort int value indices in ascending order.
	 * 
	 * @param array the int array to sort indices.
	 * @return the sorted array.
	 */
    public static int[] sortIndexesUp(int[] array) {
        JPLIArrayIndexComparator idxComp = new JPLAbstractIntsArrayIndexComparator() {

            public int compare(Integer i1, Integer i2) {
                if (getArray()[i1] < getArray()[i2]) {
                    return -1;
                }
                if (getArray()[i1] > getArray()[i2]) {
                    return 1;
                }
                return 0;
            }
        };
        JPLArrayIndexSorter sortIndex = new JPLArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    public static int[] sortIndexesDown(int[] array) {
        JPLIArrayIndexComparator idxComp = new JPLAbstractIntsArrayIndexComparator() {

            public int compare(Integer i1, Integer i2) {
                return (getArray()[i2] - getArray()[i1]);
            }
        };
        JPLArrayIndexSorter sortIndex = new JPLArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    public static int[] sortIndexesUp(double[] array) {
        JPLIArrayIndexComparator idxComp = new JPLAbstractDoublesArrayIndexComparator() {

            public int compare(Integer i1, Integer i2) {
                if (getArray()[i1] < getArray()[i2]) {
                    return -1;
                }
                if (getArray()[i1] > getArray()[i2]) {
                    return 1;
                }
                return 0;
            }
        };
        JPLArrayIndexSorter sortIndex = new JPLArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    public static int[] sortIndexesDown(double[] array) {
        JPLIArrayIndexComparator idxComp = new JPLAbstractDoublesArrayIndexComparator() {

            public int compare(Integer i1, Integer i2) {
                if (getArray()[i1] < getArray()[i2]) {
                    return 1;
                }
                if (getArray()[i1] > getArray()[i2]) {
                    return -1;
                }
                return 0;
            }
        };
        JPLArrayIndexSorter sortIndex = new JPLArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    /**
	 * Merge two sorted arrays (ascending or up) in one
	 * @param array1 the first sorted array
	 * @param array2 the second sorted array
	 * @return array the sorted merged array
	 */
    public static double[] mergeUp(double[] array1, double[] array2) {
        double[] array = new double[array1.length + array2.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < array1.length && j < array2.length) {
            if (array1[i] < array2[j]) {
                array[k] = array1[i];
                i++;
            } else {
                array[k] = array2[j];
                j++;
            }
            k++;
        }
        while (i < array1.length) {
            array[k] = array1[i];
            i++;
            k++;
        }
        while (j < array2.length) {
            array[k] = array2[j];
            j++;
            k++;
        }
        return array;
    }

    public static final int[] sortIndices(double[] objects) {
        if (objects.length > 0) {
            int[] indices = new int[objects.length];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i;
            }
            return sortIndices(objects, indices, 0, objects.length - 1);
        }
        return null;
    }

    /**
	 * Reimplementation of quicksort for performance gain.
	 * 
	 * @param objects the array of object to sort.
	 * @param from the lower limit.
	 * @param to the upper limit.
	 */
    public static final int[] sortIndices(double[] objects, int[] indices, int from, int to) {
        if (from >= to) {
            return indices;
        }
        int pivotIndex = (from + to) / 2;
        int tmp;
        int middle = indices[pivotIndex];
        if (objects[indices[from]] > objects[middle]) {
            indices[pivotIndex] = indices[from];
            indices[from] = middle;
            middle = indices[pivotIndex];
        }
        if (objects[middle] > objects[indices[to]]) {
            indices[pivotIndex] = indices[to];
            indices[to] = middle;
            middle = indices[pivotIndex];
            if (objects[indices[from]] > objects[middle]) {
                indices[pivotIndex] = indices[from];
                indices[from] = middle;
                middle = indices[pivotIndex];
            }
        }
        int left = from + 1;
        int right = to - 1;
        if (left >= right) {
            return indices;
        }
        for (; ; ) {
            while (objects[indices[right]] > objects[middle]) {
                right--;
            }
            while (left < right && objects[indices[left]] <= objects[middle]) {
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
        sortIndices(objects, indices, from, left);
        sortIndices(objects, indices, left + 1, to);
        return indices;
    }

    /**
	 * sort (increasing) the list of arrays based on the values contained in the first one (think of masses, intensities...)
	 * @param arrays
	 * @throws IllegalArgumentException if 0 arrays are passed of if length differs
	 */
    public static void sortUpArrraysOnFirst(double[][] arrays) {
        int nbArrays = arrays.length;
        if (nbArrays == 0) throw new IllegalArgumentException("cannot pass empty list of arays");
        int size = arrays[0].length;
        int[] sidx = SimpleTypeArray.sortIndexesUp(arrays[0]);
        for (int i = 0; i < nbArrays; i++) {
            if (arrays[i].length != size) throw new IllegalArgumentException("size of arrays 0 and " + i + " differ : " + size + " vs " + arrays[i]);
            double[] tmp = arrays[i].clone();
            for (int j = 0; j < tmp.length; j++) {
                arrays[i][j] = tmp[sidx[j]];
            }
        }
    }

    public static void getNthHighestElement(double[] array, int rank) {
    }
}
