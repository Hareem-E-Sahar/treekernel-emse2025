package org.amino.alg.sort;

import java.util.Random;

/**
 * This class implements the basic sequential quicksort algorithm. Provides
 * functions to sort in both ascending and descending order.
 */
public class QuickSorter extends DefaultSorter {

    /**
     * Threshold if used to decide if quick sort is needed. If the length of the
     * array section is small, use an insertion sort.
     */
    protected static final int IS_THRESHOLD = 64;

    private static final int PIVOT_ALG = 3;

    private Random rand = new Random(System.currentTimeMillis());

    /**
     * Insert sorter.
     */
    protected Sorter is = new InsertionSorter();

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is byte array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(byte[] data, int ax, int bx, int cx) {
        final byte a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is charater array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(char[] data, int ax, int bx, int cx) {
        final char a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is short array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(short[] data, int ax, int bx, int cx) {
        final short a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is int array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(int[] data, int ax, int bx, int cx) {
        final int a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is long array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(long[] data, int ax, int bx, int cx) {
        final long a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is float array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(float[] data, int ax, int bx, int cx) {
        final float a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is double array to be sorted
     * @param ax
     *            is the first index
     * @param bx
     *            is the mid index
     * @param cx
     *            is the last index
     * @return the median number
     */
    protected static int median(double[] data, int ax, int bx, int cx) {
        final double a = data[ax], b = data[bx], c = data[cx];
        if (a < b) {
            if (b < c) return bx;
            return (a < c ? cx : ax);
        } else {
            if (c < b) return bx;
            return (a < c ? ax : cx);
        }
    }

    /**
     * <b> Routine to find pivot element. </b>
     * 
     * @param data
     *            is int array to be sorted
     * @param first
     *            is the first index
     * @param mid
     *            is the mid index
     * @param last
     *            is the last index
     * @return the median number
     */
    protected static int ninther(int[] data, int first, int mid, int last) {
        int s = (last - first + 1) / 8;
        return median(data, median(data, first, first + s, first + s * 2), median(data, mid - s, mid, mid + s), median(data, last - 2 * s, last - s, last));
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(int[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        final int size = toIndex - fromIndex;
        switch(PIVOT_ALG) {
            case 1:
                return mid;
            case 2:
                return Math.abs(rand.nextInt()) % size + first;
            case 3:
                return median(a, first, mid, last);
            case 4:
                return ninther(a, first, mid, last);
            case 5:
                if (size > 100) {
                    if (size > 1000) return ninther(a, first, mid, last); else return median(a, first, mid, last);
                } else return mid;
            default:
                return 0;
        }
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(byte[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        return median(a, first, mid, last);
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(char[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        return median(a, first, mid, last);
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(short[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        return median(a, first, mid, last);
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(long[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        return median(a, first, mid, last);
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(float[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        return median(a, first, mid, last);
    }

    /**
     * Select a pivot element from the given array section.
     * 
     * @param a
     *            the array to be sorted
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted
     * 
     * @return index of a pivot element
     */
    protected int selectPivot(double[] a, int fromIndex, int toIndex) {
        final int first = fromIndex, last = toIndex - 1, mid = (fromIndex + toIndex) / 2;
        return median(a, first, mid, last);
    }

    private void qsortUp(final byte[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        byte x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                byte t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortUp(final char[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        char x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                char t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortUp(final short[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        short x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                short t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortUp(final int[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        int x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortUp(final long[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        long x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                long t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortUp(final float[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        float x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                float t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortUp(final double[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.sort(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        double x = a[pivot];
        do {
            while (a[i] < x) i++;
            while (a[j] > x) j--;
            if (i <= j) {
                double t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortUp(a, from, hi);
        if (to - lo > 1) qsortUp(a, lo, to);
    }

    private void qsortDown(final byte[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        byte x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                byte t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    private void qsortDown(final char[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        char x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                char t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    private void qsortDown(final short[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        short x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                short t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    private void qsortDown(final int[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        int x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    private void qsortDown(final long[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        long x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                long t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    private void qsortDown(final float[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        float x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                float t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    private void qsortDown(final double[] a, final int from, final int to) {
        if (to - from < IS_THRESHOLD) {
            is.reverse(a, from, to);
            return;
        }
        int pivot = selectPivot(a, from, to);
        int i = from, j = to - 1;
        double x = a[pivot];
        do {
            while (a[i] > x) i++;
            while (a[j] < x) j--;
            if (i <= j) {
                double t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        final int hi = j + 1, lo = i;
        if (hi - from > 1) qsortDown(a, from, hi);
        if (to - lo > 1) qsortDown(a, lo, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(byte[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(char[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(short[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(int[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(long[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(float[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void sort(double[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortUp(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(byte[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(char[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(short[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(int[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(long[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(float[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }

    /**
     * {@inheritDoc}
     */
    public void reverse(double[] a, int from, int to) {
        if (from > to) throw new IllegalArgumentException();
        qsortDown(a, from, to);
    }
}
