package javautil.collections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javautil.io.IOUtil;
import javautil.io.LogUtil;

/**
 *
 * @author stewari1
 */
public class ArrayUtil {

    public static void main(String[] args) {
    }

    public static boolean equals(int[] array1, int[] array2) {
        boolean equal = true;
        if (array1.length != array2.length) return false;
        for (int i = 0; i < array2.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return equal;
    }

    /**
     * If the string being searched is not found, error code -1  is sent.
     */
    public static int searchStringIgnoreCase(String str, String[] stringArray) {
        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * startPos is not index but position. 
     * creates subarray from array starting at startPos to endPos
     * @param array 
     * @param clazz 
     * @param startPos 
     * @param endPos
     * @return array
     */
    public static <T> T[] getSubArray(T[] array, Class<T> clazz, int startPos, int endPos) {
        T[] result = (T[]) Array.newInstance(clazz, (endPos - startPos + 1));
        for (int i = startPos - 1; i < endPos; i++) {
            result[i - startPos + 1] = array[i];
        }
        return result;
    }

    public static int[][] transpose(int[][] array) {
        int[][] result = new int[array[0].length][array.length];
        for (int i = 0; i < array[0].length; i++) {
            for (int j = 0; j < array.length; j++) {
                result[i][j] = array[j][i];
            }
        }
        return result;
    }

    public static int[] sort(int[] array) {
        int[] cloneArray = array.clone();
        Arrays.sort(cloneArray);
        return cloneArray;
    }

    public static int[][] deleteRows(int[][] array, int[] rowIndices) {
        int[][] result = new int[array.length - rowIndices.length][];
        if (findMax(rowIndices) > array.length - 1) {
            System.out.println("from deleteRows() in Arrayutil: rowIndex to be deleted is outside of range");
        }
        int count = 0;
        for (int index = 0; index < array.length; index++) {
            if (!contains(rowIndices, null, index)) {
                result[count] = array[index];
                count++;
            }
        }
        return result;
    }

    /**
     * get the array of objects for the selected indices,.if an index is out
     * of bound then returns null
     */
    public static Object[] selectedIndices(Object[] sourceArray, int[] indexArray) {
        if (findMax(indexArray) > sourceArray.length - 1) {
            return null;
        }
        Object[] resultArray = new Object[indexArray.length];
        for (int index = 0; index < indexArray.length; index++) {
            resultArray[index] = sourceArray[indexArray[index]];
        }
        return resultArray;
    }

    public static <T> T[] removeIndices(T[] array, int[] indices) {
        List<T> list = ListUtil.createArrayList(array);
        for (int index : indices) {
            list.remove(array[index]);
        }
        return list.toArray(array);
    }

    public static int[] removeIndices(int[] array, int... val) {
        Vector vec = new Vector();
        for (int i = 0; i < array.length; i++) {
            if (!contains(val, null, i)) {
                vec.add(new Integer(array[i]));
            }
        }
        return ListUtil.vector1DToInt1D(vec);
    }

    /**
     * Matrix Array ( rows are of same size) for int
     */
    public static int[][] get2DIntMArray(File file, boolean headerPresent, String delim) throws FileNotFoundException, RaggedArrayException, IOException, NumberFormatException {
        int[][] result = get2DIntArray(file, headerPresent, delim);
        if (isRagged(result)) {
            throw new RaggedArrayException();
        }
        return result;
    }

    /**
     * returns int[][]
     */
    public static int[][] get2DIntArray(File file, boolean headerPresent, String delim) throws FileNotFoundException, IOException, NumberFormatException {
        String[][] data = ReadRaggedStr2DArray(file, headerPresent, delim);
        int[][] result = new int[data.length][];
        for (int rowIndex = 0; rowIndex < data.length; rowIndex++) {
            result[rowIndex] = new int[data[rowIndex].length];
            for (int colIndex = 0; colIndex < data[rowIndex].length; colIndex++) {
                result[rowIndex][colIndex] = Integer.parseInt(data[rowIndex][colIndex]);
            }
        }
        return result;
    }

    public static String[][] ReadRaggedStr2DArray(File file, boolean headerPresent, String delim) throws FileNotFoundException, IOException {
        String[][] data = new String[IOUtil.countObs(file, headerPresent)][];
        BufferedReader in = new BufferedReader(new FileReader(file));
        if (headerPresent) {
            in.readLine();
        }
        for (int index = 0; index < data.length; index++) {
            data[index] = IOUtil.getTokens(in.readLine(), delim);
        }
        return data;
    }

    /**
     * @param data int[][] array tested for true ragged array
     * @return false if the array is not a true ragged array
     */
    public static boolean isRagged(int[][] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i].length != data[0].length) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRagged(int[][][] data) {
        for (int[][] array : data) {
            if (isRagged(array)) return true;
        }
        return false;
    }

    /**
     * 
     * @param array
     * @param i position not index
     * @return
     */
    public static int[] getCol(int[][] array, int i) {
        int[] temp = new int[array.length];
        for (int index = 0; index < array.length; index++) {
            temp[index] = array[index][i - 1];
        }
        return temp;
    }

    public static double[] getCol(double[][] array, int i) {
        double[] temp = new double[array.length];
        for (int index = 0; index < array.length; index++) {
            temp[index] = array[index][i - 1];
        }
        return temp;
    }

    /**
     * 
     * @param n
     * @param startAtZero
     * @return 
     */
    public static int[] IntegerSequence(int n, boolean startAtZero) {
        int[] temp = new int[n];
        int start = 1;
        if (startAtZero) start = 0;
        for (int index = 0; index < temp.length; index++) {
            temp[index] = start;
            start++;
        }
        return temp;
    }

    /**
     * true if subArray is totally contained in superArray
     */
    public static boolean contains(int[] superArray, int[] subArray) {
        for (int val : subArray) {
            if (!contains(superArray, null, val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks if the key is present in the subOrder of the array
     * if subOrder is null, it searches the entire array.
     */
    public static boolean contains(int[] array, int[] subOrder, int key) {
        if (subOrder == null) {
            for (int index = 0; index < array.length; index++) {
                if (array[index] == key) {
                    return true;
                }
            }
            return false;
        }
        for (int index = 0; index < subOrder.length; index++) {
            if (array[subOrder[index] - 1] == key) {
                return true;
            }
        }
        return false;
    }

    public static int[][] convertDoubleToIntegerArray(double[][] array) {
        int[][] result = new int[array.length][];
        for (int rowIndex = 0; rowIndex < result.length; rowIndex++) {
            result[rowIndex] = new int[array[rowIndex].length];
            for (int colIndex = 0; colIndex < result[rowIndex].length; colIndex++) {
                result[rowIndex][colIndex] = (int) array[rowIndex][colIndex];
            }
        }
        return result;
    }

    public static Integer[][] getIntegerArray(int[][] array) {
        Integer[][] result = new Integer[array.length][];
        for (int i = 0; i < result.length; i++) {
            result[i] = getIntegerArray(array[i]);
        }
        return result;
    }

    public static Double[][] getDoubleArray(double[][] array) {
        Double[][] result = new Double[array.length][];
        for (int i = 0; i < result.length; i++) {
            result[i] = getDoubleArray(array[i]);
        }
        return result;
    }

    public static double[] getDoubleArray(int[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (double) array[i];
        }
        return result;
    }

    public static double[][] getDoubleArray(int[][] array) {
        double[][] result = new double[array.length][];
        for (int i = 0; i < result.length; i++) {
            result[i] = getDoubleArray(array[i]);
        }
        return result;
    }

    public static Integer[] getIntegerArray(int[] array) {
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Integer(array[i]);
        }
        return result;
    }

    public static int[] getIntegerArray(double[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) array[i];
        }
        return result;
    }

    public static int[] getIntegerArray(Integer[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].intValue();
        }
        return result;
    }

    public static Double[] getDoubleArray(double[] array) {
        Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Double(array[i]);
        }
        return result;
    }

    public static double[] getDoubleArray(Double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].doubleValue();
        }
        return result;
    }

    public static Boolean[] getBooleanArray(boolean[] array) {
        Boolean[] result = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Boolean(array[i]);
        }
        return result;
    }

    public static boolean[] getBooleanArray(Boolean[] array) {
        boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].booleanValue();
        }
        return result;
    }

    public static int[][] addCol(int[] array) {
        int[][] temp = new int[array.length][1];
        for (int index = 0; index < array.length; index++) {
            temp[index][0] = array[index];
        }
        return temp;
    }

    public static int[][] addCol(int[][] array1, int[] array2) {
        int[][] temp = new int[array1.length][array1[0].length + 1];
        for (int index1 = 0; index1 < array1.length; index1++) {
            for (int index2 = 0; index2 < array1[0].length; index2++) {
                temp[index1][index2] = array1[index1][index2];
            }
        }
        for (int index = 0; index < array1.length; index++) {
            temp[index][array1[0].length] = array2[index];
        }
        return temp;
    }

    public static int[] addElement(int[] array, int i) {
        int[] temp = new int[array.length + 1];
        for (int index = 0; index < array.length; index++) {
            temp[index] = array[index];
        }
        temp[array.length] = i;
        return temp;
    }

    public static double[] addElement(double[] array, double d) {
        double[] temp = new double[array.length + 1];
        for (int index = 0; index < array.length; index++) {
            temp[index] = array[index];
        }
        temp[array.length] = d;
        return temp;
    }

    public static double[][] addRow(double[] array) {
        double[][] result = new double[1][array.length];
        for (int index = 0; index < array.length; index++) {
            result[0][index] = array[index];
        }
        return result;
    }

    public static double[][] addRow(double[][] self, double[] array) {
        double[][] result = new double[self.length + 1][array.length];
        if (self[0].length != array.length) {
            System.out.println(" Check function addRow: columns not matching !");
        }
        for (int index1 = 0; index1 < self.length; index1++) {
            for (int index2 = 0; index2 < self[0].length; index2++) {
                result[index1][index2] = self[index1][index2];
            }
        }
        for (int index = 0; index < array.length; index++) {
            result[self.length][index] = array[index];
        }
        return result;
    }

    public static int findMax(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static int findMin(int[] array) {
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }
}
