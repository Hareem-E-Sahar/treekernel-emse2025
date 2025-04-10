package org.apache.commons.math3.random;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

/**
 * Represents an <a href="http://http://en.wikipedia.org/wiki/Empirical_distribution_function">
 * empirical probability distribution</a> -- a probability distribution derived
 * from observed data without making any assumptions about the functional form
 * of the population distribution that the data come from.<p>
 * An <code>EmpiricalDistribution</code> maintains data structures, called
 * <i>distribution digests</i>, that describe empirical distributions and
 * support the following operations: <ul>
 * <li>loading the distribution from a file of observed data values</li>
 * <li>dividing the input data into "bin ranges" and reporting bin frequency
 *     counts (data for histogram)</li>
 * <li>reporting univariate statistics describing the full set of data values
 *     as well as the observations within each bin</li>
 * <li>generating random values from the distribution</li>
 * </ul>
 * Applications can use <code>EmpiricalDistribution</code> to build grouped
 * frequency histograms representing the input data or to generate random values
 * "like" those in the input file -- i.e., the values generated will follow the
 * distribution of the values in the file.</p>
 * <p>The implementation uses what amounts to the
 * <a href="http://nedwww.ipac.caltech.edu/level5/March02/Silverman/Silver2_6.html">
 * Variable Kernel Method</a> with Gaussian smoothing:<p>
 * <strong>Digesting the input file</strong>
 * <ol><li>Pass the file once to compute min and max.</li>
 * <li>Divide the range from min-max into <code>binCount</code> "bins."</li>
 * <li>Pass the data file again, computing bin counts and univariate
 *     statistics (mean, std dev.) for each of the bins </li>
 * <li>Divide the interval (0,1) into subintervals associated with the bins,
 *     with the length of a bin's subinterval proportional to its count.</li></ol>
 * <strong>Generating random values from the distribution</strong><ol>
 * <li>Generate a uniformly distributed value in (0,1) </li>
 * <li>Select the subinterval to which the value belongs.
 * <li>Generate a random Gaussian value with mean = mean of the associated
 *     bin and std dev = std dev of associated bin.</li></ol></p><p>
 *<strong>USAGE NOTES:</strong><ul>
 *<li>The <code>binCount</code> is set by default to 1000.  A good rule of thumb
 *    is to set the bin count to approximately the length of the input file divided
 *    by 10. </li>
 *<li>The input file <i>must</i> be a plain text file containing one valid numeric
 *    entry per line.</li>
 * </ul></p>
 *
 * @version $Id: EmpiricalDistribution.java 1244107 2012-02-14 16:17:55Z erans $
 */
public class EmpiricalDistribution implements Serializable {

    /** Default bin count */
    public static final int DEFAULT_BIN_COUNT = 1000;

    /** Serializable version identifier */
    private static final long serialVersionUID = 5729073523949762654L;

    /** List of SummaryStatistics objects characterizing the bins */
    private final List<SummaryStatistics> binStats;

    /** Sample statistics */
    private SummaryStatistics sampleStats = null;

    /** Max loaded value */
    private double max = Double.NEGATIVE_INFINITY;

    /** Min loaded value */
    private double min = Double.POSITIVE_INFINITY;

    /** Grid size */
    private double delta = 0d;

    /** number of bins */
    private final int binCount;

    /** is the distribution loaded? */
    private boolean loaded = false;

    /** upper bounds of subintervals in (0,1) "belonging" to the bins */
    private double[] upperBounds = null;

    /** RandomDataImpl instance to use in repeated calls to getNext() */
    private final RandomDataImpl randomData;

    /**
     * Creates a new EmpiricalDistribution with the default bin count.
     */
    public EmpiricalDistribution() {
        this(DEFAULT_BIN_COUNT, new RandomDataImpl());
    }

    /**
     * Creates a new EmpiricalDistribution with the specified bin count.
     *
     * @param binCount number of bins
     */
    public EmpiricalDistribution(int binCount) {
        this(binCount, new RandomDataImpl());
    }

    /**
     * Creates a new EmpiricalDistribution with the specified bin count using the
     * provided {@link RandomGenerator} as the source of random data.
     *
     * @param binCount number of bins
     * @param generator random data generator (may be null, resulting in default JDK generator)
     * @since 3.0
     */
    public EmpiricalDistribution(int binCount, RandomGenerator generator) {
        this.binCount = binCount;
        randomData = new RandomDataImpl(generator);
        binStats = new ArrayList<SummaryStatistics>();
    }

    /**
     * Creates a new EmpiricalDistribution with default bin count using the
     * provided {@link RandomGenerator} as the source of random data.
     *
     * @param generator random data generator (may be null, resulting in default JDK generator)
     * @since 3.0
     */
    public EmpiricalDistribution(RandomGenerator generator) {
        this(DEFAULT_BIN_COUNT, generator);
    }

    /**
     * Creates a new EmpiricalDistribution with the specified bin count using the
     * provided {@link RandomDataImpl} instance as the source of random data.
     *
     * @param binCount number of bins
     * @param randomData random data generator (may be null, resulting in default JDK generator)
     * @since 3.0
     */
    public EmpiricalDistribution(int binCount, RandomDataImpl randomData) {
        this.binCount = binCount;
        this.randomData = randomData;
        binStats = new ArrayList<SummaryStatistics>();
    }

    /**
     * Creates a new EmpiricalDistribution with default bin count using the
     * provided {@link RandomDataImpl} as the source of random data.
     *
     * @param randomData random data generator (may be null, resulting in default JDK generator)
     * @since 3.0
     */
    public EmpiricalDistribution(RandomDataImpl randomData) {
        this(DEFAULT_BIN_COUNT, randomData);
    }

    /**
     * Computes the empirical distribution from the provided
     * array of numbers.
     *
     * @param in the input data array
     * @exception NullArgumentException if in is null
     */
    public void load(double[] in) throws NullArgumentException {
        DataAdapter da = new ArrayDataAdapter(in);
        try {
            da.computeStats();
            fillBinStats(in);
        } catch (IOException e) {
            throw new MathIllegalStateException(e, LocalizedFormats.SIMPLE_MESSAGE, e.getLocalizedMessage());
        }
        loaded = true;
    }

    /**
     * Computes the empirical distribution using data read from a URL.
     * @param url  url of the input file
     *
     * @throws IOException if an IO error occurs
     * @throws NullArgumentException if url is null
     */
    public void load(URL url) throws IOException, NullArgumentException {
        MathUtils.checkNotNull(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        try {
            DataAdapter da = new StreamDataAdapter(in);
            da.computeStats();
            if (sampleStats.getN() == 0) {
                throw new ZeroException(LocalizedFormats.URL_CONTAINS_NO_DATA, url);
            }
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            fillBinStats(in);
            loaded = true;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Computes the empirical distribution from the input file.
     *
     * @param file the input file
     * @throws IOException if an IO error occurs
     * @throws NullArgumentException if file is null
     */
    public void load(File file) throws IOException, NullArgumentException {
        MathUtils.checkNotNull(file);
        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
            DataAdapter da = new StreamDataAdapter(in);
            da.computeStats();
            in = new BufferedReader(new FileReader(file));
            fillBinStats(in);
            loaded = true;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Provides methods for computing <code>sampleStats</code> and
     * <code>beanStats</code> abstracting the source of data.
     */
    private abstract class DataAdapter {

        /**
         * Compute bin stats.
         *
         * @throws IOException  if an error occurs computing bin stats
         */
        public abstract void computeBinStats() throws IOException;

        /**
         * Compute sample statistics.
         *
         * @throws IOException if an error occurs computing sample stats
         */
        public abstract void computeStats() throws IOException;
    }

    /**
     * Factory of <code>DataAdapter</code> objects. For every supported source
     * of data (array of doubles, file, etc.) an instance of the proper object
     * is returned.
     */
    private class DataAdapterFactory {

        /**
         * Creates a DataAdapter from a data object
         *
         * @param in object providing access to the data
         * @return DataAdapter instance
         */
        public DataAdapter getAdapter(Object in) {
            if (in instanceof BufferedReader) {
                BufferedReader inputStream = (BufferedReader) in;
                return new StreamDataAdapter(inputStream);
            } else if (in instanceof double[]) {
                double[] inputArray = (double[]) in;
                return new ArrayDataAdapter(inputArray);
            } else {
                throw new MathIllegalArgumentException(LocalizedFormats.INPUT_DATA_FROM_UNSUPPORTED_DATASOURCE, in.getClass().getName(), BufferedReader.class.getName(), double[].class.getName());
            }
        }
    }

    /**
     * <code>DataAdapter</code> for data provided through some input stream
     */
    private class StreamDataAdapter extends DataAdapter {

        /** Input stream providing access to the data */
        private BufferedReader inputStream;

        /**
         * Create a StreamDataAdapter from a BufferedReader
         *
         * @param in BufferedReader input stream
         */
        public StreamDataAdapter(BufferedReader in) {
            super();
            inputStream = in;
        }

        /** {@inheritDoc} */
        @Override
        public void computeBinStats() throws IOException {
            String str = null;
            double val = 0.0d;
            while ((str = inputStream.readLine()) != null) {
                val = Double.parseDouble(str);
                SummaryStatistics stats = binStats.get(findBin(val));
                stats.addValue(val);
            }
            inputStream.close();
            inputStream = null;
        }

        /** {@inheritDoc} */
        @Override
        public void computeStats() throws IOException {
            String str = null;
            double val = 0.0;
            sampleStats = new SummaryStatistics();
            while ((str = inputStream.readLine()) != null) {
                val = Double.valueOf(str).doubleValue();
                sampleStats.addValue(val);
            }
            inputStream.close();
            inputStream = null;
        }
    }

    /**
     * <code>DataAdapter</code> for data provided as array of doubles.
     */
    private class ArrayDataAdapter extends DataAdapter {

        /** Array of input  data values */
        private double[] inputArray;

        /**
         * Construct an ArrayDataAdapter from a double[] array
         *
         * @param in double[] array holding the data
         * @throws NullArgumentException if in is null
         */
        public ArrayDataAdapter(double[] in) throws NullArgumentException {
            super();
            MathUtils.checkNotNull(in);
            inputArray = in;
        }

        /** {@inheritDoc} */
        @Override
        public void computeStats() throws IOException {
            sampleStats = new SummaryStatistics();
            for (int i = 0; i < inputArray.length; i++) {
                sampleStats.addValue(inputArray[i]);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void computeBinStats() throws IOException {
            for (int i = 0; i < inputArray.length; i++) {
                SummaryStatistics stats = binStats.get(findBin(inputArray[i]));
                stats.addValue(inputArray[i]);
            }
        }
    }

    /**
     * Fills binStats array (second pass through data file).
     *
     * @param in object providing access to the data
     * @throws IOException  if an IO error occurs
     */
    private void fillBinStats(Object in) throws IOException {
        min = sampleStats.getMin();
        max = sampleStats.getMax();
        delta = (max - min) / (Double.valueOf(binCount)).doubleValue();
        if (!binStats.isEmpty()) {
            binStats.clear();
        }
        for (int i = 0; i < binCount; i++) {
            SummaryStatistics stats = new SummaryStatistics();
            binStats.add(i, stats);
        }
        DataAdapterFactory aFactory = new DataAdapterFactory();
        DataAdapter da = aFactory.getAdapter(in);
        da.computeBinStats();
        upperBounds = new double[binCount];
        upperBounds[0] = ((double) binStats.get(0).getN()) / (double) sampleStats.getN();
        for (int i = 1; i < binCount - 1; i++) {
            upperBounds[i] = upperBounds[i - 1] + ((double) binStats.get(i).getN()) / (double) sampleStats.getN();
        }
        upperBounds[binCount - 1] = 1.0d;
    }

    /**
     * Returns the index of the bin to which the given value belongs
     *
     * @param value  the value whose bin we are trying to find
     * @return the index of the bin containing the value
     */
    private int findBin(double value) {
        return FastMath.min(FastMath.max((int) FastMath.ceil((value - min) / delta) - 1, 0), binCount - 1);
    }

    /**
     * Generates a random value from this distribution.
     * <strong>Preconditions:</strong><ul>
     * <li>the distribution must be loaded before invoking this method</li></ul>
     * @return the random value.
     * @throws MathIllegalStateException if the distribution has not been loaded
     */
    public double getNextValue() throws MathIllegalStateException {
        if (!loaded) {
            throw new MathIllegalStateException(LocalizedFormats.DISTRIBUTION_NOT_LOADED);
        }
        double x = randomData.nextUniform(0, 1);
        for (int i = 0; i < binCount; i++) {
            if (x <= upperBounds[i]) {
                SummaryStatistics stats = binStats.get(i);
                if (stats.getN() > 0) {
                    if (stats.getStandardDeviation() > 0) {
                        return randomData.nextGaussian(stats.getMean(), stats.getStandardDeviation());
                    } else {
                        return stats.getMean();
                    }
                }
            }
        }
        throw new MathIllegalStateException(LocalizedFormats.NO_BIN_SELECTED);
    }

    /**
     * Returns a {@link StatisticalSummary} describing this distribution.
     * <strong>Preconditions:</strong><ul>
     * <li>the distribution must be loaded before invoking this method</li></ul>
     *
     * @return the sample statistics
     * @throws IllegalStateException if the distribution has not been loaded
     */
    public StatisticalSummary getSampleStats() {
        return sampleStats;
    }

    /**
     * Returns the number of bins.
     *
     * @return the number of bins.
     */
    public int getBinCount() {
        return binCount;
    }

    /**
     * Returns a List of {@link SummaryStatistics} instances containing
     * statistics describing the values in each of the bins.  The list is
     * indexed on the bin number.
     *
     * @return List of bin statistics.
     */
    public List<SummaryStatistics> getBinStats() {
        return binStats;
    }

    /**
     * <p>Returns a fresh copy of the array of upper bounds for the bins.
     * Bins are: <br/>
     * [min,upperBounds[0]],(upperBounds[0],upperBounds[1]],...,
     *  (upperBounds[binCount-2], upperBounds[binCount-1] = max].</p>
     *
     * <p>Note: In versions 1.0-2.0 of commons-math, this method
     * incorrectly returned the array of probability generator upper
     * bounds now returned by {@link #getGeneratorUpperBounds()}.</p>
     *
     * @return array of bin upper bounds
     * @since 2.1
     */
    public double[] getUpperBounds() {
        double[] binUpperBounds = new double[binCount];
        binUpperBounds[0] = min + delta;
        for (int i = 1; i < binCount - 1; i++) {
            binUpperBounds[i] = binUpperBounds[i - 1] + delta;
        }
        binUpperBounds[binCount - 1] = max;
        return binUpperBounds;
    }

    /**
     * <p>Returns a fresh copy of the array of upper bounds of the subintervals
     * of [0,1] used in generating data from the empirical distribution.
     * Subintervals correspond to bins with lengths proportional to bin counts.</p>
     *
     * <p>In versions 1.0-2.0 of commons-math, this array was (incorrectly) returned
     * by {@link #getUpperBounds()}.</p>
     *
     * @since 2.1
     * @return array of upper bounds of subintervals used in data generation
     */
    public double[] getGeneratorUpperBounds() {
        int len = upperBounds.length;
        double[] out = new double[len];
        System.arraycopy(upperBounds, 0, out, 0, len);
        return out;
    }

    /**
     * Property indicating whether or not the distribution has been loaded.
     *
     * @return true if the distribution has been loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Reseeds the random number generator used by {@link #getNextValue()}.
     *
     * @param seed random generator seed
     * @since 3.0
     */
    public void reSeed(long seed) {
        randomData.reSeed(seed);
    }
}
