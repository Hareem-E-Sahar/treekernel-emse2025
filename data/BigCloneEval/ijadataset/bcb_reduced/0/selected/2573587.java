package mipt.gui.graph.function;

/**
 * Default interpolator includes basic functionality of the interpolator.
 * It implements {@link #getDataTable()}, {@link #setDataTable(double[][])} method of the {@link Interpolator}
 * interface. Base functionality of {@link #getValue(double)} also implemented: the method
 * checks if the data table defined and if the desired argument value is in the defined data interval.
 * Specific functionality of the specific interpolator should be implemented in
 *  {@link #calculateReturnValue(double)} method.
 * Computation of min and max Y can be affected by call of setSkipCountForMinMaxY()
 * Note: if data is sent to constructor, setSkipCountForMinMaxY() will heva no effect!
 * @author Zhmurov
 */
public abstract class AbstractInterpolator implements Interpolator {

    private int minMaxDeltaY = 1;

    protected double maxX, maxY, minX, minY;

    protected double data[][];

    protected double x0, x1, y0, y1;

    protected int lastN, realSize;

    /**
	 * 
	 */
    public AbstractInterpolator() {
        this(null);
    }

    /**
	 * @param data
	 */
    public AbstractInterpolator(double[][] data) {
        setDataTable(data);
    }

    public final int getSkipCountForMinMaxY() {
        return minMaxDeltaY - 1;
    }

    public void setSkipCountForMinMaxY(int skipCountForMinMax) {
        this.minMaxDeltaY = skipCountForMinMax + 1;
    }

    /**
	 * Returns the maximal argument value.
	 * @return Max argument value, defined in the grid function array.
	 */
    public final double getMaxX() {
        return maxX;
    }

    /**
	 * Returns the maximal function value.
	 * @return Max function value, defined in the grid function array.
	 */
    public final double getMaxY() {
        return maxY;
    }

    /**
	 * Returns the minimal argument value.
	 * @return Min argument value, defined in the grid function array.
	 */
    public final double getMinX() {
        return minX;
    }

    /**
	 * Returns the minimal function value.
	 * @return Min function value, defined in the grid function array.
	 */
    public final double getMinY() {
        return minY;
    }

    /**
	 * Main interpolator method. Returns interpolated value at point x. X must be between minimal and maximal
	 * arguments defined in grid function array.
	 * @return Interpolated function value.
	 * @param x argument value where to interpolate.
	 * @throws FunctionException if the specified agument falls outside the interval on which grid function
	 * specified or if grid function array is unspecified (null).
	 */
    public double getValue(double x) throws FunctionException {
        if (data == null || data.length == 0) {
            throw new FunctionException("no data in interpolator");
        }
        if (x > maxX || x < minX) {
            throw new FunctionException("x is out of the function's definition");
        }
        boolean isOut = x < x0 || x > x1;
        if (isOut) lastN = findMinorIndex(x);
        if (isOut || x0 == x1) {
            x0 = data[lastN][0];
            y0 = data[lastN][1];
            x1 = data[lastN + 1][0];
            y1 = data[lastN + 1][1];
        }
        return calculateReturnValue(x);
    }

    /**
	 * Finds i: data[i][0]<x<data[i][0]. Called if (x<x0 || x>x1).
	 * Implementation: lastN is used as initial guess but if 3 closest intervals
	 *  does not match, uses bisection algorithm.
	 */
    protected final int findMinorIndex(double x) {
        int maxTrials = 2, n = data.length, i = n - 1, i1 = 0, i2 = i;
        while (data[i] == null) {
            i = i1 + (i2 - i1) / 2;
            if (data[i] == null) i2 = i; else {
                i1 = i;
                do {
                    i = i2 - (i2 - i) / 2;
                } while (data[i] != null && i < i2);
                if (i == i2) {
                    i = i2 - 1;
                } else i2 = i;
            }
        }
        n = i + 1;
        i = 0;
        if (x > x1) {
            i = lastN + 2;
            int iMax = i + maxTrials;
            if (iMax > n) iMax = n;
            while (i < iMax && x > data[i][0]) i++;
            if (x <= data[i][0]) return i - 1;
            i--;
        } else if (x < x0) {
            i = lastN - 1;
            int iMin = i - maxTrials;
            if (iMin < -1) iMin = -1;
            while (i > iMin && x < data[i][0]) i--;
            if (x >= data[i][0]) return i;
        }
        i1 = x > x1 ? i : 0;
        i2 = x > x1 ? n - 1 : i;
        double d = i2 - i1;
        while (d > 1.) {
            i = i1 + (int) Math.round(d * 0.5);
            if (x > data[i][0]) i1 = i; else i2 = i;
            d = i2 - i1;
        }
        return i1;
    }

    /**
	 * Returns interpolated value at point x. Used by {@link #getValue(double)} method
	 * after it tests if the argument inside the function definition interval and if
	 * the grid function data array is defined.
	 * Implementation must use x0,x1,y0,y1 fields - borders of the integral found. 
	 * @return Interpolated function value.
	 * @param x argument value where to interpolate.
	 */
    protected abstract double calculateReturnValue(double x);

    /**
	 * Sets the grid function.
	 * @see Interpolator#setDataTable(double[][]).
	 * @param table the grid function as double[][] array.
	 */
    public void setDataTable(double[][] table) throws InterpolatorException {
        int i;
        data = table;
        lastN = 0;
        if (data == null) {
            data = new double[0][];
        }
        realSize = data.length;
        if (data.length >= 2) {
            minX = x0 = data[0][0];
            minY = maxY = y0 = data[0][1];
            maxX = data[data.length - 1][0];
            x1 = data[1][0];
            y1 = data[1][1];
            for (i = minMaxDeltaY; i < data.length; i += minMaxDeltaY) {
                double value = data[i][1];
                if (Double.isInfinite(value) || Double.isNaN(value)) continue;
                if (value > maxY) {
                    maxY = value;
                } else if (value < minY) {
                    minY = value;
                }
            }
            if (minX == maxX) maxX += 1.e-15;
            if (minY == maxY) maxY += 1.e-15;
        } else {
            minX = 0.0;
            minY = 0.0;
            maxX = 0.0;
            maxY = 0.0;
        }
    }

    /**
	 * Returns the grid function as <code>double[][]</code> array.
	 * @return Array of grid function.
	 * @see mipt.gui.graph.function.Interpolator#getDataTable()
	 */
    public final double[][] getDataTable() {
        return data;
    }

    /**
	 * Returns the number of nodes in grid function.
	 * @return The size of grid function (number of grid nodes).
	 */
    public int getRealSize() {
        return realSize;
    }

    /**
	 * 
	 * @param lastPoint double[]
	 */
    public void addPoint(double[] lastPoint) {
        if (data == null) {
            data = new double[10][];
            realSize = 0;
        } else if (realSize == data.length) {
            double temp[][] = data;
            data = new double[(realSize == 0 ? 10 : realSize * 2)][];
            System.arraycopy(temp, 0, data, 0, temp.length);
            temp = null;
        }
        data[realSize] = lastPoint;
        if (realSize == 0) {
            minX = maxX = x0 = x1 = lastPoint[0];
            minY = maxY = y0 = y1 = lastPoint[1];
        } else if (realSize == 1) {
            x1 = lastPoint[0];
            y1 = lastPoint[1];
        }
        int index = realSize;
        realSize++;
        if (lastPoint[0] < minX) minX = lastPoint[0];
        if (lastPoint[0] > maxX) maxX = lastPoint[0];
        if (minMaxDeltaY > 1 && index % minMaxDeltaY != 0) return;
        double value = lastPoint[1];
        if (Double.isInfinite(value) || Double.isNaN(value)) return;
        if (value < minY) minY = value;
        if (value > maxY) maxY = value;
    }
}
