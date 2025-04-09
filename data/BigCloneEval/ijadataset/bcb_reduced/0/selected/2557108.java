package preprocessing.methods.FeatureSelection.StatClassesLib;

import java.util.*;

/**
 *
 * @author pilnya1
 */
public abstract class CorrelationBase {

    protected Map mapUnits;

    public CorrelationBase() {
        mapUnits = new HashMap<Integer, SpecUnit>();
    }

    protected void createSpecUnits(double[][] data) {
        double[][] transposedInputColumns = getTransponColumns(data);
        for (int i = 0; i < transposedInputColumns.length; i++) {
            mapUnits.put(i, new SpecUnit(Integer.toString(i), transposedInputColumns[i]));
        }
    }

    /**
     * transpon whole input data table - in each row will be one attribute values
     * @param data in rows
     * @return 2 dim. double array withl values of atributes in rows
     */
    private double[][] getTransponColumns(double[][] data) {
        double[][] columns = new double[data[0].length][data.length];
        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < columns[0].length; j++) {
                columns[i][j] = data[j][i];
            }
        }
        return columns;
    }

    /**
     * Compute correlation between x and y
     * @param x SpecUnit
     * @param y SpecUnit
     * @return   Correlation of x and y variables
     */
    public double computeCorrelation(SpecUnit x, SpecUnit y) {
        double r;
        r = computeSxy(x, y) / Math.sqrt(x.getSxx() * y.getSxx());
        return r;
    }

    /**
     * Compute correlation between inputs nr. a and nr. b
     * @param a is input data column nr. a
     * @param b is input data column nr. b
     * @return   Correlation of inputs nr. a and nr. b 
     */
    public double computeCorrelation(int a, int b) {
        double r;
        SpecUnit x, y;
        x = (SpecUnit) mapUnits.get(Integer.toString(a));
        y = (SpecUnit) mapUnits.get(Integer.toString(b));
        r = computeSxy(x, y) / Math.sqrt(x.getSxx() * y.getSxx());
        return r;
    }

    /**
     *
     * @param x Array for computing of support variable for correlation
     * @param sumX  is sum of all elements in x array
     * @return support variable for correlation
     */
    protected double computeSxx(double[] x, double sumX) {
        double sxx;
        double sumQuadr = 0;
        for (double aX : x) {
            sumQuadr += aX * aX;
        }
        sxx = sumQuadr - (sumX * sumX / x.length);
        return sxx;
    }

    /**
     *
     * @param a SpecUnit with stored data for Sxy computation
     * @param b SpecUnit with stored data for Sxy computation
     * @return Sxy - support variable for correlation
     */
    double computeSxy(SpecUnit a, SpecUnit b) {
        double sxy;
        double sumMult = 0;
        for (int i = 0; i < a.getX().length; i++) {
            sumMult += a.getX()[i] * b.getX()[i];
        }
        sxy = sumMult - (a.getSumX() * b.getSumX() / a.getX().length);
        return sxy;
    }

    /**
     * Compute and return sum of input double array
     * @param x double[] of responses
     * @return double, SUM(x)
     */
    private double computeSumX(double[] x) {
        double sumX = 0;
        for (double aX : x) {
            sumX += aX;
        }
        return sumX;
    }

    /**
     *  Class for storring of unit parrameters
     */
    public class SpecUnit {

        protected String name;

        protected double[] x;

        protected double sumX;

        protected double sxx;

        /**
         *
         * @param name - name of the unit
         * @param x    - double[] of unit responses on input data
         */
        private SpecUnit(String name, double[] x) {
            this.name = name;
            this.x = x;
            sumX = computeSumX(x);
            sxx = computeSxx(x, sumX);
        }

        public String getName() {
            return name;
        }

        public double[] getX() {
            return x;
        }

        public double getSumX() {
            return sumX;
        }

        public double getSxx() {
            return sxx;
        }
    }
}
