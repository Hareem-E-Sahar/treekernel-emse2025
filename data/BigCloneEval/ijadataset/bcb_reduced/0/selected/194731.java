package annas.math.stats;

import java.util.ArrayList;

/**
 * 
 * @author Edward Wilson
 * @author Sam Wilson
 * 
 */
public class Statistics {

    private ArrayList<Double> dataset;

    public Statistics() {
        this.dataset = new ArrayList<Double>();
    }

    public boolean add(double value) {
        this.dataset.add(value);
        return true;
    }

    public boolean add(float value) {
        String floatNumberInString;
        double floatNumberInDouble;
        floatNumberInString = String.valueOf(value);
        floatNumberInDouble = Double.parseDouble(floatNumberInString);
        this.dataset.add(floatNumberInDouble);
        return true;
    }

    public boolean add(double value, int frequency) {
        for (int i = 0; i < frequency; i++) {
            this.dataset.add(value);
        }
        return true;
    }

    public boolean add(double[] value) {
        for (int index = 0; index < value.length; index++) {
            this.dataset.add(value[index]);
        }
        return true;
    }

    public boolean add(float[] value) {
        String floatNumberInString;
        double floatNumberInDouble;
        for (int index = 0; index < value.length; index++) {
            floatNumberInString = String.valueOf(value[index]);
            floatNumberInDouble = Double.parseDouble(floatNumberInString);
            this.dataset.add(floatNumberInDouble);
        }
        return true;
    }

    public double valueAt(int index) {
        return dataset.get(index);
    }

    public int datasetSize() {
        return dataset.size();
    }

    public boolean clearData() {
        dataset.clear();
        if (dataset.size() != 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean datasetIsEmpty() {
        return dataset.isEmpty();
    }

    public boolean remove(int index) {
        dataset.remove(index);
        return true;
    }

    public boolean remove(double value) {
        int index = dataset.indexOf(value);
        if (index != -1) {
            dataset.remove(index);
        } else {
            return false;
        }
        return true;
    }

    public double sum() {
        double runningTotal = 0;
        for (int index = 0; index < this.datasetSize(); index++) {
            runningTotal = runningTotal + dataset.get(index);
        }
        return runningTotal;
    }

    public double mean() {
        if (dataset.size() < 1) {
            return -1;
        } else {
            return (sum() / this.datasetSize());
        }
    }

    public double median() {
        ArrayList<Double> temp = new ArrayList<Double>();
        temp = sort();
        if ((dataset.size() % 2) != 0) {
            int index = dataset.size() / 2;
            return temp.get(index);
        } else {
            if (datasetSize() == 2) {
                double left = temp.get(0);
                double right = temp.get(1);
                double median = (left + right) / 2;
                return median;
            } else {
                double left = temp.get(dataset.size() / 2);
                double right = temp.get((dataset.size() / 2) + 1);
                double median = (left + right) / 2;
                return median;
            }
        }
    }

    public double midRange() {
        return (max() + min() / 2);
    }

    public double range() {
        return (max() - min());
    }

    public double deviation(int index) {
        return (this.dataset.get(index) - mean());
    }

    public double deviation(double value) {
        int index = this.dataset.indexOf(value);
        if (index != -1) {
            return deviation(index);
        } else {
            return 0;
        }
    }

    public double madfm() {
        double runningTotal = 0;
        System.out.println("from madfm running total = " + runningTotal);
        for (int index = 0; index < dataset.size(); index++) {
            double temp = Math.sqrt(Math.pow(deviation(index), 2.0));
            runningTotal = (runningTotal + temp);
        }
        return runningTotal * 1 / (double) (dataset.size());
    }

    public double max() {
        double temp = dataset.get(0);
        for (int index = 1; index < dataset.size(); index++) {
            if (temp < dataset.get(index)) {
                temp = dataset.get(index);
            }
        }
        return temp;
    }

    public double min() {
        double temp = dataset.get(0);
        for (int index = 1; index < dataset.size(); index++) {
            if (temp > dataset.get(index)) {
                temp = dataset.get(index);
            }
        }
        return temp;
    }

    public int indexMin() {
        return (dataset.indexOf(this.min()));
    }

    public int indexMax() {
        return (dataset.indexOf(this.max()));
    }

    public double ssm() {
        double runningTotal = 0;
        for (int index = 0; index < datasetSize(); index++) {
            runningTotal = runningTotal + Math.pow(deviation(index), 2.0);
        }
        return runningTotal;
    }

    public double msd() {
        return ssm() / datasetSize();
    }

    public double rmsd() {
        return Math.sqrt(msd());
    }

    public double variance() {
        return (ssm() / (datasetSize() - 1));
    }

    public double sd() {
        return Math.sqrt(variance());
    }

    public boolean isOutliers(int index) {
        if (((mean() - this.dataset.get(index)) > (sd() * 2))) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Double> outliers() {
        ArrayList<Double> outliersList = new ArrayList<Double>();
        for (int index = 0; index < datasetSize(); index++) {
            if (isOutliers(index)) {
                outliersList.add(dataset.get(index));
            }
        }
        return outliersList;
    }

    public ArrayList<Double> sort() {
        ArrayList<Double> sorted = new ArrayList<Double>();
        sorted = pivotSort(dataset);
        return sorted;
    }

    private static ArrayList<Double> pivotSort(ArrayList<Double> a) {
        if (a.size() <= 0) return a;
        ArrayList<Double> smaller = new ArrayList<Double>();
        ArrayList<Double> greater = new ArrayList<Double>();
        Double pivot = a.get(0);
        for (int i = 1; i < a.size(); i++) {
            Double n = a.get(i);
            if (n.compareTo(pivot) < 0) smaller.add(n); else greater.add(n);
        }
        smaller = pivotSort(smaller);
        greater = pivotSort(greater);
        smaller.add(pivot);
        smaller.addAll(greater);
        return smaller;
    }

    public double pAnd(double A, double B) {
        if (A > 1 || A <= 0 || B > 1 || A <= 0 || (A + B) > 1) {
            return -1;
        }
        return A * B;
    }

    public double pOR(double A, double B) {
        if (A > 1 || A <= 0 || B > 1 || A <= 0) {
            return -1;
        }
        return A + B;
    }

    public double nmee(double A, double B) {
        if (A > 1 || A <= 0 || B > 1 || A <= 0) {
            return -1;
        }
        return pOR(A, B) - pAnd(A, B);
    }

    public double cp(double A, double B) {
        if (A > 1 || A <= 0 || B > 1 || A <= 0) {
            return -1;
        }
        return (pAnd(A, B) / B);
    }

    public static int permutation(int n, int r) {
        if (r < n) {
            return -1;
        }
        return (factorial(n) / factorial((n - r)));
    }

    /**
	 * 'N choose R'
	 * 
	 * @param n
	 * @param r
	 * @return
	 */
    public static int combination(int n, int r) {
        return (factorial(n) / (factorial(r) * factorial(n - r)));
    }

    /**
	 * n!
	 * 
	 * @param n
	 * @return
	 */
    public static int factorial(int n) {
        int total = n;
        while (n > 2) {
            total = total * (n - 1);
            n--;
        }
        return total;
    }
}
