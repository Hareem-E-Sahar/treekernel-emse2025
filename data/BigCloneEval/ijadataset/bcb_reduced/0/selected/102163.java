package vademecum.data;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class ArrayUtils {

    public static double[] DoubleArray2doubleArray(Double[] a) {
        double[] newarray = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            newarray[i] = a[i].doubleValue();
        }
        return newarray;
    }

    /**
	 * Returns XY-Array while putting the X-Values to result[i][0] and the Y-Values to result[i][1],
	 * with  <tt>1 &lt;= i &lt;= n</tt>.
	 * @param x
	 * @param y
	 * @return
	 */
    public static double[][] packArrays(double[] x, double[] y) {
        double[][] res = new double[x.length][2];
        for (int i = 0; i < res.length; i++) {
            res[i][0] = x[i];
            res[i][1] = y[i];
        }
        return res;
    }

    /**
	 * Sorting two arrays simultaneously (QuickSort implementation)
	 * Specific Application: PDEPlot - ordering X and Density Informations gained from ParetoDensity for MathTool's Lineplot
	 * @param unsorted the array with order criterion
	 * @param referenced the array wich indices will be reordered in the same manner as array "unsorted"
	 * @return
	 */
    public static double[][] quickSortAndPackReferencedArrays(double[] unsorted, double[] referenced) {
        int hi = unsorted.length - 1;
        recursiveQSort(unsorted, 0, hi, referenced);
        return packArrays(unsorted, referenced);
    }

    /**
	 * Perform QuickSort on two double arrays
	 * @param A the array which will be sorted
	 * @param lo QuickSort's lower bound
	 * @param hi QuickSort's upper bound
	 * @param R the array which indices will be simultanously in same order than A 
	 */
    private static void recursiveQSort(double[] A, int lo, int hi, double[] R) {
        int left = lo;
        int right = hi;
        int middle = (left + right) / 2;
        if (A[left] > A[middle]) refSwap(A, left, middle, R);
        if (A[middle] > A[right]) refSwap(A, middle, right, R);
        if (A[left] > A[middle]) refSwap(A, left, middle, R);
        if ((right - left) > 2) {
            double w = A[middle];
            do {
                while (A[left] < w) left++;
                while (w < A[right]) right--;
                if (left <= right) {
                    refSwap(A, left, right, R);
                    left++;
                    right--;
                }
            } while (left <= right);
            if (lo < right) recursiveQSort(A, lo, right, R);
            if (left < hi) recursiveQSort(A, left, hi, R);
        }
    }

    private static void refSwap(double[] A, int pos1, int pos2, double[] R) {
        double tmp = A[pos1];
        A[pos1] = A[pos2];
        A[pos2] = tmp;
        tmp = R[pos1];
        R[pos1] = R[pos2];
        R[pos2] = tmp;
    }

    public static Double[] getUniqueValues(Double[] a) {
        Vector<Double> v = new Vector<Double>();
        for (int i = 0; i < a.length; i++) {
            Double x = a[i];
            if (!v.contains(x)) {
                v.add(x);
            }
        }
        Double[] da = new Double[v.size()];
        for (int i = 0; i < v.size(); i++) {
            da[i] = v.get(i);
        }
        return da;
    }

    /**
	 * Sample data randomly to rowlength 'size'
	 * @param data
	 * @param size - number of rows of the sample
	 * @return
	 */
    public static Double[][] randomSampling(Double[][] data, int size) {
        int numcols = data[0].length;
        int numrows = data.length;
        Double[][] rdata = new Double[size][numcols];
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < numrows; i++) {
            indexList.add(i);
        }
        Random rand = new Random();
        int cnt = 0;
        while (cnt < size) {
            int index = rand.nextInt(indexList.size());
            int ival = indexList.remove(index);
            System.out.println("got index : " + ival);
            for (int j = 0; j < numcols; j++) {
                double val = data[ival][j];
                System.out.println("fetched : " + val);
                rdata[cnt][j] = val;
            }
            indexList.trimToSize();
            System.out.println("indexlist size : " + indexList.size());
            cnt++;
        }
        return rdata;
    }

    public static double[][] resize(Double[][] data, int numrows, int numcols) {
        double[][] output = new double[numrows][numcols];
        int g1rows = data.length;
        int g1cols = data[0].length;
        int ratioX = (int) (numcols / g1cols);
        int ratioY = (int) (numrows / g1rows);
        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                output[i][j] = data[i % ratioY][j % ratioX];
            }
        }
        return output;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
    }
}
