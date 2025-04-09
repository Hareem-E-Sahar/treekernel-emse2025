package de.iqcomputing.util;

import java.util.*;

/** Provides utility methods for sorting arrays and Lists. */
public final class Sort {

    /**
   * Implements a <code>{@link Comparator Comparator}</code> that uses natural comparison based on
   * <code>{@link Comparable#compareTo Comparable.compareTo}</code>.
   */
    public static final class NaturalComparator implements Comparator {

        /**
     * Compares two objects. This method returns the same as <code>o1.compareTo(o2)</code>. This
     * implies that <code>o1</code> must implement the <code>{@link Comparable Comparable}</code>
     * interface.
     */
        public final int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    }

    /** Holds information about a quicksort partition range. */
    private static final class Range {

        private int start;

        private int end;

        private Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /** Sorts an array using an iterative quicksort algorithm. The array will be sorted naturally. */
    public static final void quicksort(int[] array) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        int pivotVal;
        int tmpVal;
        stack.push(new Range(0, array.length - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) pivot = ((array[start] < array[pivot]) ? ((array[pivot] < array[end]) ? pivot : ((array[start] < array[end]) ? end : start)) : ((array[pivot] > array[end]) ? pivot : ((array[start] > array[end]) ? end : start)));
                pivotVal = array[pivot];
                low = start;
                high = end;
                while (low < high) {
                    while ((array[low] < pivotVal) && (low <= end)) low++;
                    while ((array[high] > pivotVal) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) {
                            tmpVal = array[low];
                            array[low] = array[high];
                            array[high] = tmpVal;
                        }
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }

    /** Sorts an array using an iterative quicksort algorithm. The array will be sorted naturally. */
    public static final void quicksort(long[] array) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        long pivotVal;
        long tmpVal;
        stack.push(new Range(0, array.length - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) pivot = ((array[start] < array[pivot]) ? ((array[pivot] < array[end]) ? pivot : ((array[start] < array[end]) ? end : start)) : ((array[pivot] > array[end]) ? pivot : ((array[start] > array[end]) ? end : start)));
                pivotVal = array[pivot];
                low = start;
                high = end;
                while (low < high) {
                    while ((array[low] < pivotVal) && (low <= end)) low++;
                    while ((array[high] > pivotVal) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) {
                            tmpVal = array[low];
                            array[low] = array[high];
                            array[high] = tmpVal;
                        }
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }

    /** Sorts an array using an iterative quicksort algorithm. The array will be sorted naturally. */
    public static final void quicksort(short[] array) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        short pivotVal;
        short tmpVal;
        stack.push(new Range(0, array.length - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) pivot = ((array[start] < array[pivot]) ? ((array[pivot] < array[end]) ? pivot : ((array[start] < array[end]) ? end : start)) : ((array[pivot] > array[end]) ? pivot : ((array[start] > array[end]) ? end : start)));
                pivotVal = array[pivot];
                low = start;
                high = end;
                while (low < high) {
                    while ((array[low] < pivotVal) && (low <= end)) low++;
                    while ((array[high] > pivotVal) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) {
                            tmpVal = array[low];
                            array[low] = array[high];
                            array[high] = tmpVal;
                        }
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }

    /** Sorts an array using an iterative quicksort algorithm. The array will be sorted naturally. */
    public static final void quicksort(byte[] array) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        byte pivotVal;
        byte tmpVal;
        stack.push(new Range(0, array.length - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) pivot = ((array[start] < array[pivot]) ? ((array[pivot] < array[end]) ? pivot : ((array[start] < array[end]) ? end : start)) : ((array[pivot] > array[end]) ? pivot : ((array[start] > array[end]) ? end : start)));
                pivotVal = array[pivot];
                low = start;
                high = end;
                while (low < high) {
                    while ((array[low] < pivotVal) && (low <= end)) low++;
                    while ((array[high] > pivotVal) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) {
                            tmpVal = array[low];
                            array[low] = array[high];
                            array[high] = tmpVal;
                        }
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }

    /** Sorts an array using an iterative quicksort algorithm. The array will be sorted naturally. */
    public static final void quicksort(char[] array) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        char pivotVal;
        char tmpVal;
        stack.push(new Range(0, array.length - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) pivot = ((array[start] < array[pivot]) ? ((array[pivot] < array[end]) ? pivot : ((array[start] < array[end]) ? end : start)) : ((array[pivot] > array[end]) ? pivot : ((array[start] > array[end]) ? end : start)));
                pivotVal = array[pivot];
                low = start;
                high = end;
                while (low < high) {
                    while ((array[low] < pivotVal) && (low <= end)) low++;
                    while ((array[high] > pivotVal) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) {
                            tmpVal = array[low];
                            array[low] = array[high];
                            array[high] = tmpVal;
                        }
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }

    /**
   * Sorts an array using an iterative quicksort algorithm. Calling this method is the same as
   * calling <code>quicksort(array, new {@link NaturalComparator NaturalComparator}())</code>
   */
    public static final void quicksort(Object[] array) {
        quicksort(array, new NaturalComparator());
    }

    /**
   * Sorts an array using an iterative quicksort algorithm. The array will be sorted using a
   * <code>{@link Comparator Comparator}</code>. This does not imply that every element of the array
   * has to implement the <code>{@link Comparable Comparable}</code> interface, but it usually turns
   * out that this is required by the <code>Comparator</code> in use.
   */
    public static final void quicksort(Object[] array, Comparator comp) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        Object pivotVal;
        Object startVal;
        Object endVal;
        Object tmpVal;
        stack.push(new Range(0, array.length - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) {
                    startVal = array[start];
                    endVal = array[end];
                    pivotVal = array[pivot];
                    pivot = ((comp.compare(startVal, pivotVal) < 0) ? ((comp.compare(pivotVal, endVal) < 0) ? pivot : ((comp.compare(startVal, endVal) < 0) ? end : start)) : ((comp.compare(pivotVal, endVal) > 0) ? pivot : ((comp.compare(startVal, endVal) > 0) ? end : start)));
                }
                pivotVal = array[pivot];
                low = start;
                high = end;
                while (low < high) {
                    while ((comp.compare(array[low], pivotVal) < 0) && (low <= end)) low++;
                    while ((comp.compare(array[high], pivotVal) > 0) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) {
                            tmpVal = array[low];
                            array[low] = array[high];
                            array[high] = tmpVal;
                        }
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }

    /**
   * Sorts a List using an iterative quicksort algorithm. Calling this method is the same as
   * calling <code>quicksort(list, new {@link NaturalComparator NaturalComparator}())</code>
   */
    public static final void quicksort(List list) {
        quicksort(list, new NaturalComparator());
    }

    /**
   * Sorts a List using an iterative quicksort algorithm. The array will be sorted using a
   * <code>{@link Comparator Comparator}</code>. This does not imply that every element of the List
   * has to implement the <code>{@link Comparable Comparable}</code> interface, but it usually turns
   * out that this is required by the <code>Comparator</code> in use.
   */
    public static final void quicksort(List list, Comparator comp) {
        int start = 0;
        int end = -1;
        Stack stack = new Stack();
        int low;
        int high;
        Range range;
        int pivot;
        Object pivotVal;
        Object startVal;
        Object endVal;
        int tmpVal;
        stack.push(new Range(0, list.size() - 1));
        while (stack.size() > 0) {
            if (start >= end) {
                range = (Range) stack.pop();
                start = range.start;
                end = range.end;
            }
            if (start < end) {
                pivot = start + (end - start) / 2;
                if ((end - start) > 7) {
                    startVal = list.get(start);
                    endVal = list.get(end);
                    pivotVal = list.get(pivot);
                    pivot = ((comp.compare(startVal, pivotVal) < 0) ? ((comp.compare(pivotVal, endVal) < 0) ? pivot : ((comp.compare(startVal, endVal) < 0) ? end : start)) : ((comp.compare(pivotVal, endVal) > 0) ? pivot : ((comp.compare(startVal, endVal) > 0) ? end : start)));
                }
                pivotVal = list.get(pivot);
                low = start;
                high = end;
                while (low < high) {
                    while ((comp.compare(list.get(low), pivotVal) < 0) && (low <= end)) low++;
                    while ((comp.compare(list.get(high), pivotVal) > 0) && (high >= start)) high--;
                    if (low <= high) {
                        if (low < high) list.set(high, list.set(low, list.get(high)));
                        low++;
                        high--;
                    }
                }
                if ((high - start) > (end - low)) {
                    stack.push(new Range(low, end));
                    end = high;
                } else {
                    stack.push(new Range(start, high));
                    start = low;
                }
            }
        }
    }
}
