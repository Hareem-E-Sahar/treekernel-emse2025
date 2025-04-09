package mosi.logic.simulation.statistics;

import java.util.Vector;

/**
 * Computes or Stores from a vector (SimulationResult) statistics such as Mean,
 * Median, and Standard Deviation. Additional sorting methods (Quicksort) are
 * implemented, too.
 * 
 * @author Veit K�ppen
 * 
 */
public class Statistics {

    private double[] sortedField;

    private double mean = Double.NaN, sd = Double.NaN, median = Double.NaN;

    public Statistics(double mean, double median, double sd) {
        this.mean = mean;
        this.median = median;
        this.sd = sd;
    }

    public Statistics(Vector<Double> d) {
        this.sortedField = new double[d.size()];
        for (int i = 0; i < sortedField.length; i++) {
            sortedField[i] = d.elementAt(i).doubleValue();
        }
        quickSort(sortedField, 0, sortedField.length - 1);
    }

    /**
	 * Sorts the Field of double Elements and stores the sorted field for
	 * computational purposes of Median.
	 * 
	 * @param feld
	 *            double[] unsorted Sample
	 * @param links
	 *            int Quicksort's lower bound (recursive usage)
	 * @param rechts
	 *            int Quicksort's upper bound (recursive usage)
	 */
    private void quickSort(double[] feld, int links, int rechts) {
        if (links >= rechts) return;
        int q = (links + rechts) / 2;
        int l = links, r = rechts, h;
        do {
            while (l < q && feld[l] <= feld[q]) l++;
            while (r > q && feld[q] <= feld[r]) r--;
            if (l == r) break;
            swap(feld, l, r);
            if (r == q) q = l; else if (l == q) q = r;
        } while (true);
        quickSort(feld, links, q - 1);
        quickSort(feld, q + 1, rechts);
    }

    public static void swap(double[] feld, int i, int j) {
        double dummy = feld[i];
        feld[i] = feld[j];
        feld[j] = dummy;
    }

    /**
	 * Returns the median of the sample.
	 * 
	 * @return median double
	 */
    public double median() {
        if (this.median == Double.NaN) {
            double median = 0.;
            if (sortedField.length == 0) return 0. / 0.;
            if (sortedField.length % 2 != 0) {
                median = (sortedField[sortedField.length / 2] + sortedField[(sortedField.length / 2) + 1]) / 2.;
            } else median = sortedField[(sortedField.length / 2) + 1];
            this.median = median;
        }
        return this.median;
    }

    /**
	 * Returns the Mean of the Sample
	 * 
	 * @return mean double
	 */
    public double mean() {
        if (this.mean == Double.NaN) {
            double calculatedMean = 0;
            for (int i = 0; i < sortedField.length; i++) {
                calculatedMean += sortedField[i];
            }
            this.mean = calculatedMean / sortedField.length;
        }
        return this.mean;
    }

    /**
	 * Returns the standard deviation (sd) of the sample
	 * 
	 * @return sd double
	 */
    public double sd() {
        if (this.sd == Double.NaN) {
            mean();
            double calculatedSd = 0;
            for (int i = 0; i < sortedField.length; i++) {
                calculatedSd += (sortedField[i] - this.mean) * (sortedField[i] - this.mean);
            }
            calculatedSd = calculatedSd / (sortedField.length - 1);
            this.sd = Math.sqrt(calculatedSd);
        }
        return this.sd;
    }

    /**
	 * Returns the sorted Field (for further usage in histogram for instance).
	 * 
	 * @return sortedField double[]
	 */
    public double[] getSortedField() {
        return sortedField;
    }

    public void setMean(double m) {
        this.mean = m;
    }

    public void setMedian(double m) {
        this.median = m;
    }

    public void setSd(double s) {
        this.sd = s;
    }
}
