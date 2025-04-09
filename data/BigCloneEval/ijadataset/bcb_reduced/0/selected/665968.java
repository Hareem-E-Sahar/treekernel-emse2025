package iclab.utils;

import iclab.exceptions.ICParameterException;
import iclab.math.ICRandomGenerator;
import java.util.ArrayList;

public class ICUtils {

    /**
	 * Method to obtain the transposed matrix.
	 * @param matrix - Matrix to be transposed
	 * @return - Transposed matrix
	 */
    public static double[][] transpose(double[][] matrix) throws ICParameterException {
        double[][] r;
        if (matrix == null) {
            r = null;
        } else {
            r = new double[matrix[0].length][matrix.length];
            for (int i = 0; i < matrix.length; i++) for (int j = 0; j < matrix[0].length; j++) r[j][i] = matrix[i][j];
        }
        return r;
    }

    /**
	 * Method to obtain the transposed matrix.
	 * @param matrix - Matrix to be transposed
	 * @return - Transposed matrix
	 */
    public static int[][] transpose(int[][] matrix) throws ICParameterException {
        int[][] r;
        if (matrix == null) {
            r = null;
        } else {
            r = new int[matrix[0].length][matrix.length];
            for (int i = 0; i < matrix.length; i++) for (int j = 0; j < matrix[0].length; j++) r[j][i] = matrix[i][j];
        }
        return r;
    }

    /**
	 * Method to sort a vector of doubles in ascending or descending order.
	 * @param vector - Vector to sort
	 * @param descending - If true the result will have the biggest value at position 0. If false the smallest
	 * value will be at position 0
	 * @return - Sorted vector
	 */
    public static double[] sort(double[] vector, boolean descending) {
        double[] r = vector.clone();
        boolean finished = false;
        while (!finished) {
            finished = true;
            for (int i = 1; i < vector.length; i++) {
                if ((vector[i] < vector[i - 1] && !descending) || (vector[i] > vector[i - 1] && descending)) {
                    double a = vector[i];
                    vector[i] = vector[i - 1];
                    vector[i - 1] = a;
                    finished = false;
                }
            }
        }
        return r;
    }

    /**
	 * Method to sort a vector of integers in ascending or descending order.
	 * @param vector - Vector to sort
	 * @param descending - If true the result will have the biggest value at position 0. If false the smallest
	 * value will be at position 0
	 * @return - Sorted vector
	 */
    public static int[] sort(int[] vector, boolean descending) {
        int[] r = vector.clone();
        boolean finished = false;
        while (!finished) {
            finished = true;
            for (int i = 1; i < vector.length; i++) {
                if ((r[i] < r[i - 1] && !descending) || (r[i] > r[i - 1] && descending)) {
                    int a = r[i];
                    r[i] = r[i - 1];
                    r[i - 1] = a;
                    finished = false;
                }
            }
        }
        return r;
    }

    /**
   * Method to obtain the indexes of a double vector sorted according to the value in the vector
   * @param values List of values to be sorted
   * @param descending  If true the result will have the biggest value at position 0. If false the smallest
   * @return An array of integer containing the indexes of "values" sorted
   */
    public static int[] sortIndexes(double[] values, boolean descending) {
        int[] sortedIndexes = new int[values.length];
        for (int i = 0; i < sortedIndexes.length; i++) sortedIndexes[i] = i;
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = 1; i < sortedIndexes.length; i++) {
                if ((values[sortedIndexes[i - 1]] < values[sortedIndexes[i]] && descending) || (values[sortedIndexes[i - 1]] > values[sortedIndexes[i]] && !descending)) {
                    sorted = false;
                    int aux2 = sortedIndexes[i - 1];
                    sortedIndexes[i - 1] = sortedIndexes[i];
                    sortedIndexes[i] = aux2;
                }
            }
        }
        return sortedIndexes;
    }

    /**
   * Returns a random permutation of elements from 0 to n-1
   * @param n Number of elements in the permutation
   * @return Permutation of integers from 0 to n-1
   */
    public static int[] getRandomPermutation(int n) {
        int[] permutatio = new int[n];
        for (int i = 0; i < permutatio.length; i++) permutatio[i] = i;
        shuffle(permutatio, 3);
        return (permutatio);
    }

    /**
   * Method to shuffle an array
   * @param array Array to be shuffled
   * @param times Number of random movements, defined as the ratio with respect to the size of the array
   */
    public static void shuffle(int[] array, double times) {
        if (array != null) {
            for (int i = 0; i < array.length * times; i++) {
                int i1 = (int) Math.round(ICRandomGenerator.nextUniform(0, array.length - 1));
                int i2 = (int) Math.round(ICRandomGenerator.nextUniform(0, array.length - 1));
                int aux = array[i1];
                array[i1] = array[i2];
                array[i2] = aux;
            }
        }
    }

    /**
   * Method to transform an ArrayList of integer into a int[]-type object
   * @param arrayList List of integers
   * @return Array of int
   */
    public static int[] arrayListToArray(ArrayList<Integer> arrayList) {
        int[] result = null;
        if (arrayList != null) {
            result = new int[arrayList.size()];
            for (int p = 0; p < result.length; p++) result[p] = arrayList.get(p);
        }
        return result;
    }

    /**
   * Method to transform an ArrayList of doubles into a double[]-type object
   * @param arrayList List of doubles
   * @return Array of double
   */
    public static double[] arrayListToArray(ArrayList<Double> arrayList) {
        double[] result = null;
        if (arrayList != null) {
            result = new double[arrayList.size()];
            for (int p = 0; p < result.length; p++) result[p] = arrayList.get(p);
        }
        return result;
    }

    public static int[] append(int[] array1, int[] array2) {
        int[] newArray = null;
        if (array1 != null) {
            if (array2 != null) {
                newArray = new int[array1.length + array2.length];
                for (int i = 0; i < array1.length; i++) newArray[i] = array1[i];
                for (int i = array1.length; i < newArray.length; i++) newArray[i] = array2[i - array1.length];
            } else {
                newArray = new int[array1.length];
                for (int i = 0; i < array1.length; i++) newArray[i] = array1[i];
            }
        }
        return newArray;
    }
}
