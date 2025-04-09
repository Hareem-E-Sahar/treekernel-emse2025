package org.kf.grid;

import java.util.Arrays;

public class ArrayGrid implements Grid, Cloneable {

    class StdInsertionIterator implements GridInsertionIterator {

        public int insertNext(double value, double correspondingFrequencyToAdd) {
            return updateFrequencyForValue(value, correspondingFrequencyToAdd);
        }
    }

    class SortedValuesInsertionIterator implements GridInsertionIterator {

        int lastIndex = 0;

        double lastValue = 0;

        public int insertNext(double value, double correspondingFrequencyToAdd) {
            int i = search_in_array(grid, lastIndex, actual_grid_length, value);
            frequencies[i] += correspondingFrequencyToAdd;
            lastIndex = i;
            lastValue = value;
            return i;
        }
    }

    double[] grid;

    int actual_grid_length = 0;

    double[] frequencies;

    /**
	 * Instantiates a new array empty grid using the same grid as the supplied instance
	 *
	 * @param grid the grid
	 */
    public ArrayGrid(ArrayGrid grid) {
        this(grid.grid, true);
    }

    /**
	 * Instantiates a new array grid.
	 *
	 * @param values the values : elements MUST be in the [0;1] interval
	 * @param areValuesSorted true if the values array is already sorted
	 */
    public ArrayGrid(double values[], boolean areValuesSorted) {
        double[] t = new double[values.length];
        grid = new double[values.length + 2];
        for (int i = 0; i < values.length; i++) t[i] = values[i];
        if (!areValuesSorted) Arrays.sort(t);
        int count = 0;
        grid[count++] = 0;
        double prev = 0;
        double current = 0;
        for (int i = 0; i < t.length; i++) {
            current = t[i];
            if (current != prev && current > 0 && current <= 1) grid[count++] = current;
            prev = current;
        }
        if (grid[count - 1] < 1.0) grid[count++] = 1.0;
        actual_grid_length = count;
        frequencies = new double[actual_grid_length];
    }

    /**
	 * Create an Array Grid based on values under a given threshold (valueMax)
	 * and completed beyond that threshold by log grid values of the given resolution
	 *
	 * <br><b>N.B : </b> if <b>valueMax</b> is >= 1, no log grid values are used, instead a plain
	 * standard array grid is used
	 *
	 * @param values the values
	 * @param pvalueMax the pvalue max
	 * @param logResolution the log resolution
	 *
	 * @return the array grid
	 */
    public static ArrayGrid createLogHybridGrid(double[] values, final double valueMax, final int logResolution) {
        if (valueMax >= 1) return new ArrayGrid(values, false);
        LogGrid lg = new LogGrid(logResolution);
        int nbLower = 0;
        for (double d : values) if (d <= valueMax && d >= 0) nbLower++;
        final int lgSize = lg.getSize();
        int j = 0;
        while (j < lgSize && lg.getGridValue(j) <= valueMax) j++;
        int nbHigher = lgSize - j;
        final int totalSize = nbLower + nbHigher;
        double[] allValues = new double[totalSize];
        int nb = 0;
        for (int i = 0; i < values.length; i++) {
            double e = values[i];
            if (e <= valueMax && e >= 0) allValues[nb++] = e;
        }
        assert (nb == nbLower);
        for (; j < lgSize; j++) allValues[nb++] = lg.getGridValue(j);
        assert (nb == totalSize);
        return new ArrayGrid(allValues, false);
    }

    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
        return o;
    }

    public double getDensityByGridIndex(int index) {
        return frequencies[index];
    }

    public double[] getDensities() {
        return frequencies;
    }

    public void updateFrequency(int index, double frequencyToAdd) {
        frequencies[index] += frequencyToAdd;
    }

    /**
	 * Update frequency of the grid interval containing value by frequencyToAdd
	 *
	 * @param value the value
	 * @param frequencyToAdd the frequency to add
	 *
	 * @return the index of the grid interval
	 */
    public int updateFrequencyForValue(double value, double frequencyToAdd) {
        int i = computeIndexForValue(value);
        frequencies[i] += frequencyToAdd;
        return i;
    }

    public int computeIndexForValue(double value) {
        return search_in_array(grid, 0, actual_grid_length, value);
    }

    public static int search_in_array(double[] t, int from, int length, double value) {
        int a = from + 1;
        int b = length - 1;
        int m = 0;
        if (t[b] < value) return -1;
        if (value <= t[from]) return from;
        while (true) {
            if (b < a) {
                return -2;
            }
            m = (a + b) / 2;
            if (value <= t[m]) {
                if (value > t[m - 1]) return m;
                b = m - 1;
            } else a = m + 1;
        }
    }

    public double getGridValue(int index) {
        return grid[index];
    }

    public int getSize() {
        return actual_grid_length;
    }

    public GridInsertionIterator getSortedValuesIterator() {
        return new SortedValuesInsertionIterator();
    }

    public GridInsertionIterator getStandardIterator() {
        return new StdInsertionIterator();
    }

    public void clear() {
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = 0;
        }
    }
}
