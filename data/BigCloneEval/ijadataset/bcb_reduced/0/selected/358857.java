package com.oat.utils;

import java.util.LinkedList;
import java.util.Random;
import com.oat.AlgorithmRunException;
import com.oat.Solution;

/**
 * Type: RandomUtils<br/>
 * Date: 11/03/2006<br/>
 * <br/>
 * Description: General utilities that use random numbers
 * <br/>
 * @author Jason Brownlee
 * 
 * <pre>
 * Change History
 * ----------------------------------------------------------------------------
 * 15/01/2007   JBrownlee   Fixed bug in generateUniform2DPattern() that ensures only
 *                          the specified number of points are created.                 
 * </pre>
 */
public class RandomUtils {

    public static final Random RAND = new Random();

    public static final Random randomService() {
        return RAND;
    }

    public static final int MIN = 0, MAX = 1;

    /**
     * Implementation of the Cauchy-Lorentz distribution
     * Taken from: Evolutionary programming made faster (1999)
     * More info: http://en.wikipedia.org/wiki/Cauchy_distribution
     * @param r
     * @return
     */
    public static double nextCauchy(Random r) {
        double x = 2 * r.nextDouble() - 1;
        return 1 / 2 + 1 / Math.PI * Math.atan(x / 1);
    }

    /**
     * Convience method for Gaussian random number with a specified mean and standard deviation
     * 
     * @param mean
     * @param stdev
     * @param r
     * @return
     */
    public static double randomGaussian(double mean, double stdev, Random r) {
        return mean + stdev * r.nextGaussian();
    }

    /**
     * Generate a random coordinate within the specified range
     * @param r - random number generator
     * @param minmax - bounds of the coordinate space [] = each dimension [i][2] = {min,max} format
     * @return
     */
    public static double[] randomPointInRange(Random r, double[][] minmax) {
        double[] coord = new double[minmax.length];
        for (int i = 0; i < coord.length; i++) {
            double range = minmax[i][MAX] - minmax[i][MIN];
            coord[i] = minmax[i][MIN] + (range * r.nextDouble());
        }
        return coord;
    }

    public static double[] randomPointSmallPositiveDoubles(Random r, int length) {
        double[] coord = new double[length];
        for (int i = 0; i < coord.length; i++) {
            coord[i] = r.nextDouble();
        }
        return coord;
    }

    /**
     * Generate a uniform 2D pattern using the specified number of points, in the specified bounds
     * Assumes the domain is two dimensional
     * 
     * @param numPoints - does not have to be a square
     * @param minmax - bounds of the coordinate space [] = each dimension [i][2] = {min,max} format
     * @return numPoints points 
     */
    public static double[][] generateUniform2DPattern(int numPoints, double[][] minmax) {
        if (minmax.length != 2) {
            throw new AlgorithmRunException("Only supports 2D domains: " + minmax.length);
        }
        double[][] points = new double[numPoints][2];
        double xRange = (minmax[0][MAX] - minmax[0][MIN]);
        double yRange = (minmax[1][MAX] - minmax[1][MIN]);
        int numPerDim = (int) Math.ceil(Math.sqrt(numPoints));
        double xIncrement = (1.0 / numPerDim) * xRange;
        double yIncrement = (1.0 / numPerDim) * yRange;
        int count = 0;
        for (int yy = 0; count < numPoints && yy < numPerDim; yy++) {
            for (int xx = 0; count < numPoints && xx < numPerDim; xx++, count++) {
                points[count][0] = minmax[0][MIN] + ((xx * xIncrement) + (xIncrement / 2));
                points[count][1] = minmax[1][MIN] + ((yy * yIncrement) + (yIncrement / 2));
            }
        }
        return points;
    }

    /**
     * Generate a random bit string with the length (numParameters*precisionPerParameter)
     * Convience method when working with real numbers in binary format
     * 
     * @param r
     * @param numParameters
     * @param precisionPerParameter
     * @return
     */
    public static boolean[] randomBitString(Random r, int numParameters, int precisionPerParameter) {
        int totalBits = numParameters * precisionPerParameter;
        return RandomUtils.randomBitString(r, totalBits);
    }

    /**
     * Generate a random bit string of the specified length
     * @param r
     * @param length
     * @return
     */
    public static boolean[] randomBitString(Random r, int length) {
        boolean[] b = new boolean[length];
        for (int i = 0; i < length; i++) {
            b[i] = r.nextBoolean();
        }
        return b;
    }

    public static boolean[][] randomBitStringSet(Random r, int length, int numPatterns, int minDistance) {
        boolean[][] patterns = new boolean[numPatterns][];
        for (int i = 0; i < patterns.length; i++) {
            boolean isSuitablePattern = false;
            boolean[] potential = null;
            do {
                potential = RandomUtils.randomBitString(r, length);
                isSuitablePattern = true;
                for (int j = 0; isSuitablePattern && j < i - 1; j++) {
                    double distance = BitStringUtils.hammingDistance(potential, patterns[j]);
                    if (distance < minDistance) {
                        isSuitablePattern = false;
                    }
                }
            } while (!isSuitablePattern);
            patterns[i] = potential;
        }
        return patterns;
    }

    /**
     * Swap two randomly selected elements in the provided vector, permitting re-selection
     * @param v
     * @param r
     */
    public static final void randomSwap(int[] v, Random r) {
        int s1 = r.nextInt(v.length);
        int s2 = r.nextInt(v.length);
        int a = v[s1];
        v[s1] = v[s2];
        v[s2] = a;
    }

    /**
     * The Fisher-Yates shuffle
     * @param v
     * @param r
     */
    public static final void randomShuffle(int[] v, Random r) {
        int n = v.length;
        while (--n > 0) {
            int k = r.nextInt(n + 1);
            int temp = v[n];
            v[n] = v[k];
            v[k] = temp;
        }
    }

    /**
     * Generate list of integers from 0 to(length-1), then use the shuffle function to randomise the list
     * zero offset values: [0, length)
     * @param length
     * @param r
     * @return
     */
    public static final int[] generateRandomVector(int length, Random r) {
        int[] v = new int[length];
        for (int i = 0; i < v.length; i++) {
            v[i] = i;
        }
        RandomUtils.randomShuffle(v, r);
        return v;
    }

    /**
     * Select a random sample from the provided pop without re-selection of selected elements
     * @param s - population to draw the sample from
     * @param size - sample size
     * @param r - random number to use
     * @return - random sample of solutions
     */
    public static final <T extends Object> LinkedList<T> randomSampleWithOutReselection(LinkedList<T> s, int size, Random r) {
        LinkedList<T> n = new LinkedList<T>();
        if (size > s.size()) {
            throw new AlgorithmRunException("Invalid sample size [" + size + "], larger than pop size [" + s.size() + "].");
        } else if (size == s.size()) {
            n.addAll(s);
        } else {
            LinkedList<T> tmp = new LinkedList<T>();
            tmp.addAll(s);
            while (n.size() < size) {
                n.add(tmp.remove(r.nextInt(tmp.size())));
            }
        }
        return n;
    }

    /**
     * Select a random sample with re-selection, size can be larger than the population
     * @param s
     * @param size
     * @param r
     * @return
     */
    public static final <T extends Object> LinkedList<T> randomSampleWithReselection(LinkedList<T> s, int size, Random r) {
        LinkedList<T> n = new LinkedList<T>();
        while (n.size() < size) {
            int index = r.nextInt(s.size());
            n.add(s.get(index));
        }
        return n;
    }
}
