package de.peathal.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class SortAlgos<T extends Comparable<T>> {

    List<T> field;

    int fieldSize;

    /**
     * We do not use here the List interface, because then it also could be
     * a LinkedList. And we get a n<sup>2</sup> log(n) time.
     */
    public SortAlgos(ArrayList<T> list) {
        if (list.size() == 0) throw new UnsupportedOperationException("size should be greater then zero");
        field = new ArrayList<T>(list);
        fieldSize = field.size();
    }

    public static void main(String arg[]) {
        ArrayList<Integer> al = new ArrayList<Integer>();
        Random rand = new Random();
        int MAX = 1000000;
        int N = 100;
        for (int arraySize = 100; arraySize < MAX; arraySize += 500) {
            for (int i = 0; i < arraySize; i++) {
                al.add(rand.nextInt(arraySize));
            }
            System.out.print(arraySize + "\t");
            startClock();
            for (int i = 0; i < N; i++) {
                new SortAlgos<Integer>(al).quickSort();
            }
            stopClock();
            mem();
            startClock();
            for (int i = 0; i < N; i++) {
                new SortAlgos<Integer>(al).mergeSort();
            }
            stopClock();
            mem();
            startClock();
            for (int i = 0; i < N; i++) {
                new SortAlgos<Integer>(al).heapSort();
            }
            stopClock();
            mem();
            System.out.print("\n");
        }
    }

    private static long current;

    static void startClock() {
        current = System.currentTimeMillis();
    }

    static void stopClock() {
        System.out.print((System.currentTimeMillis() - current) + "\t");
    }

    static void mem() {
        Runtime rt = Runtime.getRuntime();
        System.out.print((rt.totalMemory() - rt.freeMemory()) + "\t");
        System.gc();
    }

    /**
     * This method sorts any list of any type, which implements the Comparable
     * interface.
     * Implementation: Divide and conquer, see the "Algorithm" - book from E.Horowitz
     *
     * @return sorted list
     */
    public List<T> quickSort() {
        qs(0, field.size() - 1);
        return field;
    }

    /**
     * Quicksort
     */
    public final void qs(int from, int to) {
        if (from < to) {
            T partElem = field.get(from);
            int j = to + 1, i = from;
            while (true) {
                do {
                    i++;
                } while (i < fieldSize && field.get(i).compareTo(partElem) < 0);
                do {
                    j--;
                } while (field.get(j).compareTo(partElem) > 0);
                if (i < j) {
                    field.set(i, field.set(j, field.get(i)));
                } else break;
            }
            field.set(from, field.set(j, partElem));
            qs(from, j - 1);
            qs(j + 1, to);
        }
    }

    /********* MERGE SORT **************/
    ArrayList<T> helperList;

    public List<T> mergeSort() {
        helperList = new ArrayList<T>();
        for (int i = 0; i < fieldSize; i++) {
            helperList.add((T) null);
        }
        ms(0, field.size() - 1);
        return field;
    }

    public final void ms(int from, int to) {
        if (from < to) {
            int middle = (from + to) / 2;
            ms(from, middle);
            ms(middle + 1, to);
            sortedInsert(from, middle, to);
        }
    }

    public final void sortedInsert(int from, int middle, int to) {
        int h = from, i = from, j = middle + 1;
        while (h <= middle && j <= to) {
            if (field.get(h).compareTo(field.get(j)) <= 0) {
                helperList.set(i, field.get(h));
                h++;
            } else {
                helperList.set(i, field.get(j));
                j++;
            }
            i++;
        }
        int k;
        if (h > middle) {
            for (k = j; k <= to; k++, i++) {
                helperList.set(i, field.get(k));
            }
        } else {
            for (k = h; k <= middle; k++, i++) {
                helperList.set(i, field.get(k));
            }
        }
        for (k = from; k <= to; k++) {
            field.set(k, helperList.get(k));
        }
    }

    /********* HEAP SORT **************/
    public List<T> heapSort() {
        for (int i = fieldSize / 2 - 1; i > -1; i--) {
            heapify(i, fieldSize - 1);
        }
        for (int i = fieldSize - 2; i > -1; i--) {
            field.set(i + 1, field.set(0, field.get(i + 1)));
            heapify(0, i);
        }
        return field;
    }

    /**
     * This method inserts the i-th value into the rest-heap [i+1, m)
     */
    private void heapify(int i, int m) {
        T t = field.get(i);
        int j = 2 * i + 1;
        while (j <= m) {
            if (j < m && field.get(j).compareTo(field.get(j + 1)) < 0) j++;
            if (t.compareTo(field.get(j)) >= 0) {
                break;
            }
            field.set((j - 1) / 2, field.set(j, t));
            j = 2 * j + 1;
        }
        field.set((j - 1) / 2, t);
    }

    static final void swap(final List<Comparable> l, final int i, final int j) {
        l.set(i, l.set(j, l.get(i)));
    }
}
