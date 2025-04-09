package org.tigr.microarray.util;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.cluster.gui.IData;

public class SlideDataSorter {

    public static final int SORT_BY_LOCATION = 9000;

    public static final int SORT_BY_RATIO = 9001;

    private ISlideData slideData;

    /**
     * Constructs a <code>SlideDataSorter</code>
     */
    public SlideDataSorter() {
    }

    /**
     * Constructs a <code>SlideDataSorter</code> with specified data.
     * @see ISlideData
     */
    public SlideDataSorter(ISlideData slideData) {
        if (slideData == null) {
            throw new IllegalArgumentException("SlideData is null.");
        }
        this.slideData = slideData;
    }

    /**
     * Sets a microarray data for this sorter.
     */
    public void setSlideData(ISlideData slideData) {
        this.slideData = slideData;
    }

    /**
     * Applies sorting an array of indices according to
     * a wrapped microarray data and type of sort.
     */
    public int[] sort(int[] indices, int type) {
        sort(indices, new IndicesComparator(type));
        return indices;
    }

    /**
     * The class to compare a microarray elements by its indices
     * and sort type.
     */
    private class IndicesComparator {

        private int type;

        /**
         * Constructs an <code>IndicesComparator</code> with specified type of sort.
         */
        public IndicesComparator(int type) {
            this.type = type;
        }

        /**
         * Compare two elements with specified indices.
         */
        public int compare(int index1, int index2) {
            switch(type) {
                case SORT_BY_LOCATION:
                    {
                        ISlideMetaData meta = slideData.getSlideMetaData();
                        final int columns = meta.getColumns();
                        if (meta.getRow(index1) * columns + meta.getColumn(index1) < meta.getRow(index2) * columns + meta.getColumn(index2)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                case SORT_BY_RATIO:
                    {
                        float value1, value2;
                        value1 = slideData.getRatio(index1, IData.LINEAR);
                        value2 = slideData.getRatio(index2, IData.LINEAR);
                        if (Float.isNaN(value1) && Float.isNaN(value2)) return -1;
                        if (Float.isNaN(value1) && !Float.isNaN(value2)) return -1;
                        if (!Float.isNaN(value1) && Float.isNaN(value2)) return 1;
                        if (value1 < value2) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                default:
                    {
                        ISlideMetaData meta = slideData.getSlideMetaData();
                        String value1 = meta.getValueAt(index1, type);
                        String value2 = meta.getValueAt(index2, type);
                        return value1.compareTo(value2);
                    }
            }
        }
    }

    public static void sort(int[] a, IndicesComparator c) {
        int[] aux = cloneArray(a);
        mergeSort(aux, a, 0, a.length, c);
    }

    private static int[] cloneArray(int[] a) {
        int[] clone = new int[a.length];
        for (int i = a.length; --i >= 0; ) {
            clone[i] = a[i];
        }
        return clone;
    }

    private static void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void mergeSort(int[] src, int[] dest, int low, int high, IndicesComparator c) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; i++) for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--) swap(dest, j, j - 1);
            return;
        }
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid, c);
        mergeSort(dest, src, mid, high, c);
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) dest[i] = src[p++]; else dest[i] = src[q++];
        }
    }
}
