package org.berlin.math.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Quick Sort.
 * 
 * @author bbrown
 *
 */
public class QuickSort {

    private final Comparable<Object>[] array;

    public QuickSort(final Comparable ar[]) {
        this.array = ar;
    }

    public Comparable[] get() {
        return this.array;
    }

    protected int selectPivotIndex(final int left, final int right) {
        int midIndex = (left + right) / 2;
        int lowIndex = left;
        if (array[lowIndex].compareTo(array[midIndex]) >= 0) {
            lowIndex = midIndex;
            midIndex = left;
        }
        if (array[right].compareTo(array[lowIndex]) <= 0) {
            return lowIndex;
        } else if (array[right].compareTo(array[midIndex]) <= 0) {
            return midIndex;
        }
        return right;
    }

    protected int partition(final int left, final int right, final int pivotIndex) {
        final Comparable<Object> pivot = array[pivotIndex];
        Comparable<Object> tmp;
        tmp = array[right];
        array[right] = array[pivotIndex];
        array[pivotIndex] = tmp;
        int store = left;
        for (int idx = left; idx < right; idx++) {
            if (array[idx].compareTo(pivot) <= 0) {
                tmp = array[idx];
                array[idx] = array[store];
                array[store] = tmp;
                store++;
            }
        }
        tmp = array[right];
        array[right] = array[store];
        array[store] = tmp;
        return store;
    }

    public void quickSort(final int left, final int right) {
        if (right <= left) {
            return;
        }
        int pivotIndex = selectPivotIndex(left, right);
        pivotIndex = partition(left, right, pivotIndex);
        quickSort(left, pivotIndex - 1);
        quickSort(pivotIndex + 1, right);
    }

    protected void insertionSort(final int low, final int high) {
        if (high <= low) {
            return;
        }
        for (int t = low; t < high; t++) {
            for (int i = t + 1; i <= high; i++) {
                if (array[i].compareTo(array[t]) < 0) {
                    final Comparable<Object> c = array[t];
                    array[t] = array[i];
                    array[i] = c;
                }
            }
        }
    }

    public Comparable[] sort() {
        this.quickSort(0, this.array.length - 1);
        return this.array;
    }

    /**
	 * Unit Test Case.
	 * @param args
	 */
    public static void main(final String[] args) {
        System.out.println("Running");
        final Integer[] arr = { 20, 23, 14, 12 };
        for (final Comparable<Object> o : new QuickSort(arr).sort()) {
            System.out.println(o);
        }
        System.out.println("Done");
        final int size = 20000;
        int trialCount = 0;
        for (int j = size; j <= (size * 100); j += size) {
            final List<Integer> l = new ArrayList<Integer>();
            final Random r = new Random(System.currentTimeMillis());
            for (int i = 0; i < j; i++) {
                l.add(20 + r.nextInt(10) + r.nextInt(5000));
            }
            final long n1 = System.currentTimeMillis();
            final QuickSort qq = new QuickSort(l.toArray(new Integer[] {}));
            for (final Comparable<Object> o : qq.sort()) {
                o.toString();
            }
            final long n2 = System.currentTimeMillis();
            System.out.println("---- Trial :: " + trialCount);
            System.out.println("Size: " + l.size());
            System.out.println("Diff: " + (n2 - n1) + " ms ");
            for (int z = 0; z < 5; z++) {
                System.out.print(qq.get()[z] + ",  ");
            }
            System.out.println("==== ");
            System.out.println();
            trialCount++;
        }
        System.out.println("Done");
    }
}
