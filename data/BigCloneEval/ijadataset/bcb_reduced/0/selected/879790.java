package util;

import java.util.HashMap;
import java.util.Random;

public class Gaussian {

    public static final int DEFAULT_GAUSSIAN_SIZE = 100;

    private HashMap<Double, Double> randomVartoPDF = new HashMap<Double, Double>();

    private double pdfMultiplier = 1.00;

    private int numValues;

    /**
	 * Creates default, empty Gaussian.
	 */
    public Gaussian() {
        pdfMultiplier = 1.00;
        numValues = DEFAULT_GAUSSIAN_SIZE;
        randomVartoPDF = null;
    }

    /**
	 * Makes Gaussian of default size, default scaling, and given standard deviation, and convenient mean.
	 * @param stdev
	 */
    public Gaussian(double stdev) {
        this.pdfMultiplier = 1;
        numValues = DEFAULT_GAUSSIAN_SIZE;
        double mean = 1 + stdev * 3;
        int counter = 0;
        while (counter <= numValues) {
            double randomVar = counter * (((3 * stdev) + mean) / numValues);
            randomVartoPDF.put(randomVar, pdfMultiplier * Gaussian.phi(randomVar, mean, stdev));
            counter++;
        }
    }

    /**
	 * Constructs a Gaussian object around given zscores
	 * @param zscores
	 */
    public Gaussian(double[] zscores) {
        this.pdfMultiplier = 1;
        numValues = zscores.length;
        double stdev = Util.standardDeviation(zscores);
        double mean = Util.arrayMean(zscores);
        double temp = mean - 3 * stdev;
        mean = (temp < 0) ? (mean - temp) : mean;
        for (int x = 0; x < numValues; x++) {
            double randomVar = x * (((3 * stdev) + mean) / numValues);
            randomVartoPDF.put(randomVar, zscores[x]);
        }
    }

    /**
	 * Generates a positive-valued Gaussian with given parameters.
	 * 
	 * Calculates equally-spaced values within 3 standard deviations of given mean,
	 * but if those values are negative, the mean is shifted so that all values are 
	 * positive.
	 * 
	 * @param size - the number of values to be interpolated
	 * @param mean - the pseudo-mean used to generate the Gaussian PDF
	 * @param stdev - the true standard deviation used to generate the PDF
	 * @param pdfMultiplier - the value multiplier used to scale the PDF
	 */
    public Gaussian(int size, double mean, double stdev, double pdfMultiplier) {
        this.pdfMultiplier = pdfMultiplier;
        numValues = size;
        double temp = mean - 3 * stdev;
        mean = (temp < 0) ? (mean - temp) : mean;
        int counter = 0;
        while (counter <= size) {
            double randomVar = counter * (((3 * stdev) + mean) / size);
            randomVartoPDF.put(randomVar, pdfMultiplier * Gaussian.phi(randomVar, mean, stdev));
            counter++;
        }
    }

    /**
     * Makes Gaussian of set size, mean, and stdev. Default scaling is used.
     * 
     * @param size
     * @param mean
     * @param stdev
     */
    public Gaussian(int size, double mean, double stdev) {
        this.pdfMultiplier = 1;
        numValues = size;
        double temp = mean - 3 * stdev;
        mean = (temp < 0) ? (mean - temp) : mean;
        int counter = 0;
        while (counter <= size) {
            double randomVar = counter * (((3 * stdev) + mean) / size);
            randomVartoPDF.put(randomVar, pdfMultiplier * Gaussian.phi(randomVar, mean, stdev));
            counter++;
        }
    }

    /**
     * Makes Gaussian of set size, stdev, with convenient mean.
     * 
     * For reference, a conveninent mean is one s.t. 99.1% of all
     * values in the Gaussian are positively-valued, i.e. 99.1% of
     * all values to the right and left of the mean > 0.
     * 
     * @param size
     * @param stdev
     */
    public Gaussian(int size, double stdev) {
        this.pdfMultiplier = 1;
        numValues = size;
        double mean = 1 + stdev * 3;
        int counter = 0;
        while (counter <= size) {
            double randomVar = counter * (((3 * stdev) + mean) / size);
            randomVartoPDF.put(randomVar, pdfMultiplier * Gaussian.phi(randomVar, mean, stdev));
            counter++;
        }
    }

    /**
     * Makes Gaussian of set size, stdev, using custom scaling.
     * 
     * @param pdfMultiplier - the scaling scalar
     * @param size
     * @param stdev
     */
    public Gaussian(double pdfMultiplier, int size, double stdev) {
        this.pdfMultiplier = pdfMultiplier;
        numValues = size;
        double mean = 1 + stdev * 3;
        int counter = 0;
        while (counter <= size) {
            double randomVar = counter * (((3 * stdev) + mean) / size);
            randomVartoPDF.put(randomVar, pdfMultiplier * Gaussian.phi(randomVar, mean, stdev));
            counter++;
        }
    }

    /**
     * Returns safe (cloned) copy of random variable for this Gaussian
     * 
     * @return
     */
    public double[] getRandomVar() {
        if (this.randomVartoPDF == null) return null;
        Object[] temp = randomVartoPDF.keySet().toArray();
        double[] temp2 = new double[temp.length];
        for (int x = 0; x < temp.length; x++) {
            temp2[x] = ((Double) (temp[x])).doubleValue();
        }
        return temp2;
    }

    /**
     * Returns safe (cloned) copy of this Gaussian's PDF value list
     * 
     * @return clone of PDF value list
     */
    public double[] getPDF() {
        if (this.randomVartoPDF == null) return null;
        double[] temp = this.getRandomVar();
        double[] temp2 = new double[temp.length];
        for (int x = 0; x < temp.length; x++) {
            temp2[x] = this.randomVartoPDF.get(temp[x]).doubleValue();
        }
        return temp2;
    }

    /**
     * Returns an unique randomly-distributed number of z-scores
     * from the distribution this Gaussian represents.
     * 
     * Returns double array of exactly two rows, e.g.:
     * 
     *  Random Var: [ 0 , 1 , 2 , 3 , 4 , 5 , 6 ]
     *  Z-Scores  : [ 11, 25, 38, 56, 64, 72, 89]
     * 
     * FIXME No Duplicate (RandomVar,ZScores) Elements
     * 
     * @param num - the number of values to extract from this Gaussian
     * 
     * @return 	randomly-distributed number of z-scores in a 2D array 
     * 				OR 
     * 			null if this Gaussian is empty
     */
    public double[][] getNumRandomZScores(int num) throws IllegalArgumentException {
        if (num > numValues) throw new IllegalArgumentException("Not enough values in distribution"); else if (this.randomVartoPDF == null) return null;
        double[][] xySeries = new double[2][num];
        Random r = new Random();
        for (int x = 0; x < num; x++) {
            int randomIndex = r.nextInt(this.numValues);
            xySeries[0][x] = (Double) this.randomVartoPDF.keySet().toArray()[randomIndex];
            xySeries[1][x] = this.randomVartoPDF.get(xySeries[0][x]);
        }
        return xySeries;
    }

    /**
     * Returns a random value from this Gaussian's PDF value list
     * 
     * @return
     */
    public double getRandomValue() {
        Random r = new Random();
        double[] holder = this.getPDF();
        return holder[r.nextInt(holder.length)];
    }

    /**
     * Returns true iff Gaussian is empty, i.e. has no value
     * @return
     */
    public boolean isEmpty() {
        return (this.randomVartoPDF == null);
    }

    /**
     * Does In-place scaling of Z-Scores.
     * 
     * NOTE: This modifies the internal HashMap itself by overriding 
     * all key-value pairs with key-(value*scale) pairs.
     * 
     * @param scale - the scale to multiply z-scores by
     */
    public void scaleZScores(double scale) {
        this.pdfMultiplier *= scale;
        for (double randomVar : this.getRandomVar()) {
            double prevZScore = this.randomVartoPDF.get(randomVar);
            this.randomVartoPDF.put(randomVar, prevZScore * scale);
        }
    }

    /**
     * Gets mean of PDF by calculating it.
     * @return Mean of PDF, OR NaN if PDF is null
     */
    public double getMean() {
        return (isEmpty()) ? (Double.NaN) : (Util.arrayMean(getPDF()));
    }

    /**
     * Gets stored standard deviation
     * @return
     */
    public double getStandardDeviation() {
        return Util.standardDeviation(getPDF());
    }

    public int getSize() {
        return (this.isEmpty()) ? (0) : (this.numValues);
    }

    public static double phi(double x) {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    public static double phi(double x, double mu, double sigma) {
        return phi((x - mu) / sigma) / sigma;
    }

    public static double Phi(double z) {
        if (z < -8.0) return 0.0;
        if (z > 8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * phi(z);
    }

    public static double Phi(double z, double mu, double sigma) {
        return Phi((z - mu) / sigma);
    }

    public static double PhiInverse(double y) {
        return PhiInverse(y, .00000001, -8, 8);
    }

    private static double PhiInverse(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (Phi(mid) > y) return PhiInverse(y, delta, lo, mid); else return PhiInverse(y, delta, mid, hi);
    }

    /**
	 * Returns a pseudorandom value from a specified normal (Gaussian) curve
	 * 
	 * Deprecated: Prefer usage of
	 * 
	 * @param mean - the mean of the Gaussian
	 * @param stdev - the standard deviation of the Gaussian
	 * @return value from specified gaussian
	 */
    public static double getGaussianValue(double mean, double stdev) {
        return mean + (stdev * ((new Random()).nextGaussian()));
    }
}
