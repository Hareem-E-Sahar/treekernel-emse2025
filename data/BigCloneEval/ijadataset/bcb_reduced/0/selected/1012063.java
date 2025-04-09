package org.expasy.jpl.commons.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math.util.MathUtils;
import org.expasy.jpl.commons.collection.sorter.AbstractDoublesArrayIndexComparator;
import org.expasy.jpl.commons.collection.sorter.AbstractIntsArrayIndexComparator;
import org.expasy.jpl.commons.collection.sorter.ArrayIndexComparator;
import org.expasy.jpl.commons.collection.sorter.ArrayIndexSorter;

/**
 * Provides static generic methods on primitive arrays.
 * 
 * @author nikitin
 * 
 * @version 1.0
 * 
 */
public final class PrimitiveArrayUtils {

    /** simply reverse the array */
    public static void reverse(int[] b) {
        int left = 0;
        int right = b.length - 1;
        while (left < right) {
            int temp = b[left];
            b[left] = b[right];
            b[right] = temp;
            left++;
            right--;
        }
    }

    /**
	 * Map the objects found at given indices.
	 * 
	 * @param objects the objects to map.
	 * @param indices the indices of objects array.
	 * @return an array of mapped {@code Object}s.
	 */
    public static Object[] mapping(final Object[] objects, final int[] indices) {
        if (indices.length > objects.length) {
            throw new IllegalArgumentException("too many indices");
        }
        final Object[] values = new Object[indices.length];
        int i = 0;
        for (final int index : indices) {
            values[i++] = objects[index];
        }
        return values;
    }

    /**
	 * Map the integers found at given indices.
	 * 
	 * @param array the integers to map.
	 * @param indices the indices of objects array.
	 * @return an array of mapped {@code int}s.
	 */
    public static int[] mapping(final int[] array, final int[] indices) {
        if (indices.length > array.length) {
            throw new IllegalArgumentException("too many indices");
        }
        final int[] values = new int[indices.length];
        int i = 0;
        for (final int index : indices) {
            values[i++] = array[index];
        }
        return values;
    }

    /**
	 * Map the doubles found at given indices.
	 * 
	 * @param vals the values to map.
	 * @param indices the indices of objects array.
	 * @return an array of mapped {@code double}s.
	 */
    public static double[] mapping(final double[] vals, final int[] indices) {
        if (vals == null || vals.length == 0) {
            return null;
        }
        if (indices.length > vals.length) {
            throw new IllegalArgumentException("too many indices");
        }
        final double[] values = new double[indices.length];
        int i = 0;
        for (final int index : indices) {
            values[i++] = vals[index];
        }
        return values;
    }

    /**
	 * Map the values found in the given interval.
	 * 
	 * @param vals the values to map.
	 * @param from the first index.
	 * @param to the last index (excluded).
	 * 
	 * @return an array of {@code double}s.
	 */
    public static double[] mapping(final double[] vals, int from, int to) {
        if (vals == null) {
            throw new IllegalArgumentException("undefined values to map!");
        }
        if (vals.length == 0) {
            return new double[0];
        }
        if (to <= from) {
            throw new IllegalArgumentException(from + " <= " + to + ": illegal bounds!");
        }
        final double[] values = new double[to - from];
        int i = 0;
        for (int index = from; index < to; index++) {
            values[i++] = vals[index];
        }
        return values;
    }

    public static int[] mapping(final int[] vals, int from, int to) {
        if (vals == null) {
            throw new IllegalArgumentException("undefined values to map!");
        }
        if (vals.length == 0) {
            return new int[0];
        }
        if (to <= from) {
            throw new IllegalArgumentException(from + " <= " + to + ": illegal bounds!");
        }
        final int[] values = new int[to - from];
        int i = 0;
        for (int index = from; index < to; index++) {
            values[i++] = vals[index];
        }
        return values;
    }

    /**
	 * Convert objects to ints.
	 * 
	 * @param objects the objects to convert.
	 */
    public static int[] convert2ints(final Object[] objects) {
        final int[] values = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            values[i] = Integer.parseInt(objects[i].toString());
        }
        return values;
    }

    /**
	 * Convert ints to array of Integers.
	 * 
	 * @param ints the ints to convert.
	 */
    public static Integer[] ints2Integers(final int[] ints) {
        final Integer[] values = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            values[i] = ints[i];
        }
        return values;
    }

    /**
	 * Convert ints to List of Integers.
	 * 
	 * @param ints the ints to convert.
	 */
    public static List<Integer> asIntList(final int[] ints) {
        final List<Integer> l = new ArrayList<Integer>();
        for (int i : ints) {
            l.add(i);
        }
        return l;
    }

    /**
	 * Convert arrays of Ts to List of Ts.
	 * 
	 * @param os the object array to convert.
	 */
    public static <T> List<T> asList(final T[] os) {
        final List<T> l = new ArrayList<T>();
        Collections.addAll(l, os);
        return l;
    }

    /**
	 * Convert array of doubles to array of Doubles.
	 * 
	 * @param dbls the array to convert.
	 */
    public static Double[] doubles2Doubles(final double[] dbls) {
        final Double[] values = new Double[dbls.length];
        for (int i = 0; i < dbls.length; i++) {
            values[i] = dbls[i];
        }
        return values;
    }

    /**
	 * Sort int values in ascending order.
	 * 
	 * @param array the int array to sort.
	 * @return the sorted array.
	 */
    public static int[] sortUp(final int[] array) {
        Arrays.sort(array);
        return array;
    }

    /**
	 * Sort int values in descending order.
	 * 
	 * @param array the int array to sort.
	 * @return the sorted array.
	 */
    public static int[] sortDown(final int[] array) {
        Arrays.sort(array);
        final int halfLen = array.length / 2;
        final int lastIdx = array.length - 1;
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
    public static double[] sortUp(final double[] array) {
        Arrays.sort(array);
        return array;
    }

    /**
	 * Sort double values in descending order.
	 * 
	 * @param array the double array to sort.
	 * @return the sorted array.
	 */
    public static double[] sortDown(final double[] array) {
        Arrays.sort(array);
        final int halfLen = array.length / 2;
        final int lastIdx = array.length - 1;
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
    public static int[] sortIndexesUp(final int[] array) {
        final ArrayIndexComparator idxComp = new AbstractIntsArrayIndexComparator() {

            @Override
            public int compare(final Integer i1, final Integer i2) {
                if (getArray()[i1] < getArray()[i2]) {
                    return -1;
                }
                if (getArray()[i1] > getArray()[i2]) {
                    return 1;
                }
                return 0;
            }
        };
        final ArrayIndexSorter sortIndex = new ArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    /**
	 * Sort int value indices in descending order.
	 * 
	 * @param array the int array to sort indices.
	 * @return the sorted array.
	 */
    public static int[] sortIndexesDown(final int[] array) {
        final ArrayIndexComparator idxComp = new AbstractIntsArrayIndexComparator() {

            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (getArray()[i2] - getArray()[i1]);
            }
        };
        final ArrayIndexSorter sortIndex = new ArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    public static int[] sortIndexesUp(final double[] array) {
        final ArrayIndexComparator idxComp = new AbstractDoublesArrayIndexComparator() {

            @Override
            public int compare(final Integer i1, final Integer i2) {
                if (getArray()[i1] < getArray()[i2]) {
                    return -1;
                }
                if (getArray()[i1] > getArray()[i2]) {
                    return 1;
                }
                return 0;
            }
        };
        final ArrayIndexSorter sortIndex = new ArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    public static int[] sortIndexesDown(final double[] array) {
        final ArrayIndexComparator idxComp = new AbstractDoublesArrayIndexComparator() {

            @Override
            public int compare(final Integer i1, final Integer i2) {
                if (getArray()[i1] < getArray()[i2]) {
                    return 1;
                }
                if (getArray()[i1] > getArray()[i2]) {
                    return -1;
                }
                return 0;
            }
        };
        final ArrayIndexSorter sortIndex = new ArrayIndexSorter(array, idxComp);
        return sortIndex.sort();
    }

    /**
	 * Merge two sorted arrays (ascending or up) in one
	 * 
	 * @param array1 the first sorted array
	 * @param array2 the second sorted array
	 * @return array the sorted merged array
	 */
    public static double[] mergeUp(final double[] array1, final double[] array2) {
        final double[] array = new double[array1.length + array2.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while ((i < array1.length) && (j < array2.length)) {
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

    public static final int[] sortIndices(final double[] objects) {
        if (objects.length > 0) {
            final int[] indices = new int[objects.length];
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
    public static final int[] sortIndices(final double[] objects, final int[] indices, final int from, final int to) {
        if (from >= to) {
            return indices;
        }
        final int pivotIndex = (from + to) / 2;
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
            while ((left < right) && (objects[indices[left]] <= objects[middle])) {
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
	 * sort (increasing) the list of arrays based on the values contained in the
	 * first one (think of masses, intensities...)
	 * 
	 * @param arrays
	 * @throws IllegalArgumentException if 0 arrays are passed of if length
	 *         differs
	 */
    public static void sortUpArrraysOnFirst(final double[][] arrays) {
        final int nbArrays = arrays.length;
        if (nbArrays == 0) {
            throw new IllegalArgumentException("cannot pass empty list of arays");
        }
        final int size = arrays[0].length;
        final int[] sidx = PrimitiveArrayUtils.sortIndexesUp(arrays[0]);
        for (int i = 0; i < nbArrays; i++) {
            if (arrays[i].length != size) {
                throw new IllegalArgumentException("size of arrays 0 and " + i + " differ : " + size + " vs " + arrays[i]);
            }
            final double[] tmp = arrays[i].clone();
            for (int j = 0; j < tmp.length; j++) {
                arrays[i][j] = tmp[sidx[j]];
            }
        }
    }

    public static void getNthHighestElement(final double[] array, final int rank) {
    }

    public static double[] floats2doubles(final float[] fArray) {
        final double[] dArray = new double[fArray.length];
        for (int i = 0; i < fArray.length; i++) {
            dArray[i] = fArray[i];
        }
        return dArray;
    }

    /**
	 * Get the index range of {@code values} where each is contained in the
	 * given interval.
	 * 
	 * @param values the values to get the range from.
	 * @param start the interval included start.
	 * @param end the interval excluded end.
	 * @return a range of indices
	 */
    public static int[] getIndexRange(double[] values, double start, double end) {
        IntegerSequence seq = getIndexRange(values, new Interval.Builder(start, end).build());
        if (seq.isEmpty()) {
            return new int[] {};
        } else {
            return new int[] { seq.getFrom(), seq.getTo() };
        }
    }

    /**
	 * Get an interval of index in sorted values.
	 * 
	 * @param interval of definition.
	 * 
	 * @return an interval on values index [imin, imax[.
	 * 
	 * @throws IllegalArgumentException if values are not sorted.
	 */
    public static IntegerSequence getIndexRange(double[] values, final Interval interval) {
        int imin = -1;
        int imax = -1;
        MathUtils.checkOrder(values, 1, false);
        double lowerBound = interval.getLowerBound();
        double upperBound = interval.getUpperBound();
        for (int index = 0; index < values.length; index++) {
            if (values[index] < lowerBound || (values[index] == lowerBound && !interval.isLowerBoundIncluded())) {
                imin = index + 1;
                continue;
            }
            if (values[index] < upperBound || (values[index] == upperBound && interval.isUpperBoundIncluded())) {
                imax = index;
            } else {
                break;
            }
        }
        if (imax == -1) {
            return IntegerSequence.emptyInstance();
        }
        if (imin == -1) {
            imin = 0;
        }
        return new IntegerSequence.Builder(imin, imax + 1).build();
    }

    /**
	 * Search the given query intervals into the target values.
	 * 
	 * @param values the sorted values to find indices in.
	 * @param intervalCenters the sorted interval centers query.
	 * @param halfIntervalRange the half-range interval for all query value.
	 * 
	 * @return a list of indices found in at least one query interval.
	 * 
	 * @throws IllegalArgumentException - if the arrays are not sorted.
	 */
    public static List<Integer> searchIndices(double[] values, double[] intervalCenters, double halfIntervalRange) {
        return searchIndices(values, intervalCenters, halfIntervalRange, 0);
    }

    /**
	 * Search the given query intervals into the target values.
	 * 
	 * @param values the sorted values to find indices in.
	 * @param intervalCenters the sorted interval centers query.
	 * @param halfIntervalRange the half-range interval for all query value.
	 * @param from the begin index to search into values.
	 * 
	 * @return a list of indices found in at least one query interval.
	 * 
	 * @throws IllegalArgumentException - if the arrays are not sorted.
	 */
    public static List<Integer> searchIndices(double[] values, double[] intervalCenters, double halfIntervalRange, int from) {
        MathUtils.checkOrder(values, 1, false);
        MathUtils.checkOrder(intervalCenters, 1, false);
        final List<Integer> indices = new ArrayList<Integer>();
        int intervalIndex = 0;
        int i = from;
        while (i < values.length && intervalIndex < intervalCenters.length) {
            i = Arrays.binarySearch(values, i, values.length, intervalCenters[intervalIndex] - halfIntervalRange);
            if (i < 0) {
                i = -i - 1;
            }
            while (i < values.length && values[i] <= intervalCenters[intervalIndex] + halfIntervalRange) {
                indices.add(i++);
            }
            intervalIndex++;
        }
        return indices;
    }

    public static double[] getSubArray(double[] values, int from, int to) {
        return Arrays.copyOfRange(values, from, to);
    }

    /**
	 * Convert an integer to an array of bytes.
	 * 
	 * http://snippets.dzone.com/posts/show/93
	 * 
	 * @param value the integer to convert.
	 */
    public static final byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    /**
	 * Convert an array of bytes into integer value.
	 * 
	 * http://snippets.dzone.com/posts/show/93
	 * 
	 * @param bytes the bytes to convert into integer.
	 */
    public static final int byteArrayToInt(byte[] bytes) {
        return (bytes[0] << 24) + ((bytes[1] & 0xFF) << 16) + ((bytes[2] & 0xFF) << 8) + (bytes[3] & 0xFF);
    }

    /**
	 * Convert a list of {@code Double}s to an array of doubles.
	 * 
	 * @param list the list to convert.
	 * @return an array of doubles.
	 */
    public static final double[] toDoubleArray(List<Double> list) {
        final double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
	 * Convert a list of {@code Integer}s to an array of ints.
	 * 
	 * @param list the list to convert.
	 * @return an array of ints.
	 */
    public static final int[] toIntArray(List<Integer> list) {
        final int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static final int[] loadIntArray(int[] src, int[] dest) {
        return loadIntArray(src, dest, 0);
    }

    public static final int[] loadIntArray(int[] src, int[] dest, int destPos) {
        if (src == null) {
            throw new NullPointerException("undefined src array to load into dest!");
        }
        if (dest == null || dest.length < src.length) {
            dest = new int[src.length];
        }
        System.arraycopy(src, 0, dest, destPos, src.length);
        return dest;
    }

    public static final double[] loadDoubleArray(double[] src, double[] dest) {
        return loadDoubleArray(src, dest, 0);
    }

    public static final double[] loadDoubleArray(double[] src, double[] dest, int destPos) {
        if (src == null) {
            throw new NullPointerException("undefined src array to load into dest!");
        }
        if (dest == null || dest.length < src.length) {
            dest = new double[src.length];
        }
        System.arraycopy(src, 0, dest, destPos, src.length);
        return dest;
    }

    public static final double min(double[] values) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > 0 && values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }
}
