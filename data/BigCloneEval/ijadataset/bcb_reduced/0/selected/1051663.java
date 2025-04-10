package org.apache.commons.math.random;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

/**
 * Implements <code>EmpiricalDistribution</code> interface.  This implementation
 * uses what amounts to the
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
 * @version $Revision: 746578 $ $Date: 2009-02-21 21:01:14 +0100 (Sa, 21 Feb 2009) $
 */
public class EmpiricalDistributionImpl implements Serializable, EmpiricalDistribution {

    /** Serializable version identifier */
    private static final long serialVersionUID = 5729073523949762654L;

    /** List of SummaryStatistics objects characterizing the bins */
    private List<SummaryStatistics> binStats = null;

    /** Sample statistics */
    private SummaryStatistics sampleStats = null;

    /** number of bins */
    private int binCount = 1000;

    /** is the distribution loaded? */
    private boolean loaded = false;

    /** upper bounds of subintervals in (0,1) "belonging" to the bins */
    private double[] upperBounds = null;

    /** RandomData instance to use in repeated calls to getNext() */
    private RandomData randomData = new RandomDataImpl();

    /**
     * Creates a new EmpiricalDistribution with the default bin count.
     */
    public EmpiricalDistributionImpl() {
        binStats = new ArrayList<SummaryStatistics>();
    }

    /**
     * Creates a new EmpiricalDistribution  with the specified bin count.
     * 
     * @param binCount number of bins
     */
    public EmpiricalDistributionImpl(int binCount) {
        this.binCount = binCount;
        binStats = new ArrayList<SummaryStatistics>();
    }

    /**
     * Computes the empirical distribution from the provided
     * array of numbers.
     * 
     * @param in the input data array
     */
    public void load(double[] in) {
        DataAdapter da = new ArrayDataAdapter(in);
        try {
            da.computeStats();
            fillBinStats(in);
        } catch (Exception e) {
            throw new MathRuntimeException(e);
        }
        loaded = true;
    }

    /**
     * Computes the empirical distribution using data read from a URL.
     * @param url  url of the input file
     * 
     * @throws IOException if an IO error occurs
     */
    public void load(URL url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        try {
            DataAdapter da = new StreamDataAdapter(in);
            try {
                da.computeStats();
            } catch (IOException ioe) {
                throw ioe;
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception e) {
                throw MathRuntimeException.createIOException(e);
            }
            if (sampleStats.getN() == 0) {
                throw MathRuntimeException.createEOFException("URL {0} contains no data", url);
            }
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            fillBinStats(in);
            loaded = true;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Computes the empirical distribution from the input file.
     * 
     * @param file the input file
     * @throws IOException if an IO error occurs
     */
    public void load(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
            DataAdapter da = new StreamDataAdapter(in);
            try {
                da.computeStats();
            } catch (IOException ioe) {
                throw ioe;
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception e) {
                throw MathRuntimeException.createIOException(e);
            }
            in = new BufferedReader(new FileReader(file));
            fillBinStats(in);
            loaded = true;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
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
         * @param min minimum value
         * @param delta  grid size
         * @throws Exception  if an error occurs computing bin stats
         */
        public abstract void computeBinStats(double min, double delta) throws Exception;

        /**
         * Compute sample statistics.
         * 
         * @throws Exception if an error occurs computing sample stats
         */
        public abstract void computeStats() throws Exception;
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
                throw new IllegalArgumentException("Input data comes from the" + " unsupported source");
            }
        }
    }

    /**
     * <code>DataAdapter</code> for data provided through some input stream
     */
    private class StreamDataAdapter extends DataAdapter {

        /** Input stream providng access to the data */
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

        /**
         * Computes binStats
         * 
         * @param min  minimum value
         * @param delta  grid size
         * @throws IOException if an IO error occurs
         */
        public void computeBinStats(double min, double delta) throws IOException {
            String str = null;
            double val = 0.0d;
            while ((str = inputStream.readLine()) != null) {
                val = Double.parseDouble(str);
                SummaryStatistics stats = binStats.get(findBin(min, val, delta));
                stats.addValue(val);
            }
            inputStream.close();
            inputStream = null;
        }

        /**
         * Computes sampleStats
         * 
         * @throws IOException if an IOError occurs
         */
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
         */
        public ArrayDataAdapter(double[] in) {
            super();
            inputArray = in;
        }

        /**
         * Computes sampleStats
         * 
         * @throws IOException if an IO error occurs
         */
        public void computeStats() throws IOException {
            sampleStats = new SummaryStatistics();
            for (int i = 0; i < inputArray.length; i++) {
                sampleStats.addValue(inputArray[i]);
            }
        }

        /**
         * Computes binStats
         * 
         * @param min  minimum value
         * @param delta  grid size
         * @throws IOException  if an IO error occurs
         */
        public void computeBinStats(double min, double delta) throws IOException {
            for (int i = 0; i < inputArray.length; i++) {
                SummaryStatistics stats = binStats.get(findBin(min, inputArray[i], delta));
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
        double min = sampleStats.getMin();
        double max = sampleStats.getMax();
        double delta = (max - min) / (Double.valueOf(binCount)).doubleValue();
        double[] binUpperBounds = new double[binCount];
        binUpperBounds[0] = min + delta;
        for (int i = 1; i < binCount - 1; i++) {
            binUpperBounds[i] = binUpperBounds[i - 1] + delta;
        }
        binUpperBounds[binCount - 1] = max;
        if (!binStats.isEmpty()) {
            binStats.clear();
        }
        for (int i = 0; i < binCount; i++) {
            SummaryStatistics stats = new SummaryStatistics();
            binStats.add(i, stats);
        }
        DataAdapterFactory aFactory = new DataAdapterFactory();
        DataAdapter da = aFactory.getAdapter(in);
        try {
            da.computeBinStats(min, delta);
        } catch (IOException ioe) {
            throw ioe;
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw MathRuntimeException.createIOException(e);
        }
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
     * @param min  the minimum value
     * @param value  the value whose bin we are trying to find
     * @param delta  the grid size
     * @return the index of the bin containing the value
     */
    private int findBin(double min, double value, double delta) {
        return Math.min(Math.max((int) Math.ceil((value - min) / delta) - 1, 0), binCount - 1);
    }

    /**
     * Generates a random value from this distribution.
     * 
     * @return the random value.
     * @throws IllegalStateException if the distribution has not been loaded
     */
    public double getNextValue() throws IllegalStateException {
        if (!loaded) {
            throw MathRuntimeException.createIllegalStateException("distribution not loaded");
        }
        double x = Math.random();
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
        throw new MathRuntimeException("no bin selected");
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
     * Returns (a fresh copy of) the array of upper bounds for the bins.
       Bins are: <br/>
     * [min,upperBounds[0]],(upperBounds[0],upperBounds[1]],...,
     *  (upperBounds[binCount-1],max]
     * 
     * @return array of bin upper bounds
     */
    public double[] getUpperBounds() {
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
}
