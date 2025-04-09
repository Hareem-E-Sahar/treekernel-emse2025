package com.wrupple.muba.common.shared.services.impl;

import java.io.PrintStream;
import com.wrupple.muba.common.shared.services.StatisticsCalculator;

public class MubaStatisticsCalculator implements StatisticsCalculator {

    @Override
    public double getMean(double[] nums) {
        return mean(nums);
    }

    @Override
    public double cumulativeProbability(double bucketValueStart, double bucketValueEnd, double sampleAverage, double stdDeviation) {
        double x = (bucketValueStart + bucketValueEnd) / 2;
        return Gaussian.phi(x, sampleAverage, stdDeviation);
    }

    public double sqr(double x) {
        return x * x;
    }

    /**
	   * Returns the average of an array of double.
	   */
    public double mean(double[] v) {
        double tot = 0.0;
        for (int i = 0; i < v.length; i++) tot += v[i];
        return tot / v.length;
    }

    /**
	   * @param v
	   *            - sample
	   * @return the average of an array of int.
	   */
    public double mean(int[] v) {
        double tot = 0.0;
        for (int i = 0; i < v.length; i++) tot += v[i];
        return tot / v.length;
    }

    /**
	   * Returns the sample standard deviation of an array of double.
	   */
    public double sdev(double[] v) {
        return Math.sqrt(variance(v));
    }

    /**
	   * Returns the standard error of an array of double, where this is defined
	   * as the standard deviation of the sample divided by the square root of the
	   * sample size.
	   */
    public double stderr(double[] v) {
        return sdev(v) / Math.sqrt(v.length);
    }

    /**
	   * this alternative version was used to check correctness
	   */
    private double variance2(double[] v) {
        double mu = mean(v);
        double sumsq = 0.0;
        for (int i = 0; i < v.length; i++) sumsq += sqr(v[i]);
        System.out.println(sumsq + " : " + mu);
        double diff = (sumsq - v.length * sqr(mu));
        System.out.println("Diff = " + diff);
        return diff / (v.length);
    }

    /**
	   * Returns the covariance of the paired arrays of double.
	   */
    public double covar(double[] v1, double[] v2) {
        double m1 = mean(v1);
        double m2 = mean(v2);
        double sumsq = 0.0;
        for (int i = 0; i < v1.length; i++) sumsq += (m1 - v1[i]) * (m2 - v2[i]);
        return sumsq / (v1.length);
    }

    public double correlation(double[] v1, double[] v2) {
        return covar(v1, v2) / (sdev(v1) * sdev(v2));
    }

    public double correlation2(double[] v1, double[] v2) {
        return sqr(covar(v1, v2)) / (covar(v1, v1) * covar(v2, v2));
    }

    /**
	   * Returns the maximum value in the array.
	   */
    public double max(double[] v) {
        double m = v[0];
        for (int i = 1; i < v.length; i++) m = Math.max(m, v[i]);
        return m;
    }

    /**
	   * Returns the minimum value in the array.
	   */
    public double min(double[] v) {
        double m = v[0];
        for (int i = 1; i < v.length; i++) m = Math.min(m, v[i]);
        return m;
    }

    /**
	   * Prints the means and standard deviation of the data to the standard
	   * output.
	   */
    public void analyse(double[] v) {
        analyse(v, System.out);
    }

    /**
	   * Prints the means and standard deviation of the data to the specified
	   * PrintStream
	   * 
	   * @param v
	   *            contains the data
	   * @param s
	   *            is the corresponding PrintStream
	   */
    public void analyse(double[] v, PrintStream s) {
        s.println("Average = " + mean(v) + "  sd = " + sdev(v));
    }

    /**
	   * @param v
	   *            contains the data
	   * @return A String summary of the with the mean and standard deviation of
	   *         the data.
	   */
    public String analysisString(double[] v) {
        return "Average = " + mean(v) + "  sd = " + sdev(v) + "  min = " + min(v) + "  max = " + max(v);
    }

    /**
	   * Returns a string that compares the root mean square of the data with the
	   * standard deviation of the data. This is probably too specialised to be of
	   * much general use.
	   * 
	   * @param v
	   *            contains the data
	   * @return root mean square = <...> standard deviation = <...>
	   */
    public String rmsString(double[] v) {
        double[] tv = new double[v.length];
        for (int i = 0; i < v.length; i++) tv[i] = v[i] * v[i];
        return "rms = " + mean(tv) + " sd = " + sdev(v) + "\n";
    }

    @Override
    public double variance(double[] v) {
        double mu = mean(v);
        double sumsq = 0.0;
        for (int i = 0; i < v.length; i++) sumsq += sqr(mu - v[i]);
        return sumsq / (v.length);
    }
}
