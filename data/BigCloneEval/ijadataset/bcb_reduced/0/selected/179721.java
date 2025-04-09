package name.huzhenbo.java.algorithm.search;

import java.util.Arrays;

/**
 * Insert random int into the array, and the array should be in the asc order.
 */
class IntSet {

    private int currentEnd = 0;

    private int initSize = 10;

    private int[] values;

    public IntSet(int maxVal) {
        values = new int[initSize + 1];
        values[0] = maxVal;
    }

    public void insert(int item) {
        if (values.length <= currentEnd + 1) extend();
        insertAt(item, search(item));
    }

    private void insertAt(int item, int loc) {
        if (item == values[loc]) return;
        int insertLoc = item > values[loc] ? loc + 1 : loc;
        for (int i = currentEnd; i >= insertLoc; i--) {
            values[i + 1] = values[i];
        }
        values[insertLoc] = item;
        currentEnd++;
    }

    private void extend() {
        this.values = Arrays.copyOf(values, values.length + initSize);
    }

    public int size() {
        return currentEnd;
    }

    private int search(int item) {
        int start = 0;
        int end = currentEnd;
        while (end > start) {
            int m = (start + end) / 2;
            if (item == values[m]) {
                return m;
            } else if (item > values[m]) {
                start = m + 1;
            } else {
                end = m - 1;
            }
        }
        return start;
    }

    public void print() {
        for (int i = 0; i < currentEnd; i++) {
            System.out.println(values[i]);
        }
    }
}
