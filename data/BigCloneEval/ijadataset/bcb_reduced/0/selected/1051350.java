package shu.math;

import java.text.*;
import java.util.*;

/**
 * <p>Title: Colour Management System</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author cms.shu.edu.tw
 * @version 1.0
 */
public final class DoubleArray {

    public static double[][] fill(int m, int n, double c) {
        return org.math.array.DoubleArray.fill(m, n, c);
    }

    public static final String toString(double[]... v) {
        return org.math.array.DoubleArray.toString(v).trim();
    }

    public static final String toString(String decimalFormat, double[]... v) {
        return toString(new DecimalFormat(decimalFormat), v);
    }

    public static final String toString(DecimalFormat df, double[]... v) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                if (j == v[i].length - 1) {
                    str.append(df.format(v[i][j]));
                } else {
                    str.append(df.format(v[i][j]) + " ");
                }
            }
            if (i < v.length - 1) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    public static double[][] getRowsRangeCopy(double[][] M, int i1, int i2) {
        return org.math.array.DoubleArray.getRowsRangeCopy(M, i1, i2);
    }

    public static double[][] getColumnsRangeCopy(double[][] M, int j1, int j2) {
        return org.math.array.DoubleArray.getColumnsRangeCopy(M, j1, j2);
    }

    public static void main(String[] args) {
        double[] a = new double[] { 6, 8, 2, 3, 4, 5, 1, 3, 5, 2, 3 };
        System.out.println(DoubleArray.toString(DoubleArray.getRangeCopy(a, 1, 3)));
    }

    /**
   * ��m�ᬰ����ίx�}
   * @param a double[]
   * @return double[][]
   */
    public static final double[][] transposeSquare(double[] a) {
        int am = a.length;
        int an = a.length;
        double[][] result = new double[an][am];
        for (int j = 0; j < an; j++) {
            result[j][0] = a[j];
        }
        return result;
    }

    public static final double[][] transpose(double[] a) {
        int an = a.length;
        double[][] result = new double[an][1];
        for (int j = 0; j < an; j++) {
            result[j][0] = a[j];
        }
        return result;
    }

    public static final double[][] transpose(double[][] a) {
        int am = a.length;
        int an = a[0].length;
        double[][] result = new double[an][am];
        for (int i = 0; i < am; i++) {
            for (int j = 0; j < an; j++) {
                result[j][i] = a[i][j];
            }
        }
        return result;
    }

    public static final double[][] to2DDoubleArray(double[] doubleArray, int width) {
        int height = doubleArray.length / width;
        double[][] result = new double[height][width];
        for (int x = 0; x < height; x++) {
            System.arraycopy(doubleArray, x * width, result[x], 0, width);
        }
        return result;
    }

    public static final double[] to1DDoubleArray(double[][] doubleArray) {
        int mSize = doubleArray.length;
        int nSize = doubleArray[0].length;
        double[] result = new double[mSize * nSize];
        int index = 0;
        for (int x = 0; x < mSize; x++) {
            for (int y = 0; y < nSize; y++) {
                result[index++] = doubleArray[x][y];
            }
        }
        return result;
    }

    public static final double[][] diagonal(double... c) {
        int size = c.length;
        double[][] m = new double[size][size];
        return diagonal(m, c);
    }

    public static final double[][] diagonal(double[][] array, double... c) {
        for (int i = 0; i < c.length; i++) {
            array[i][i] = c[i];
        }
        return array;
    }

    public static final double det(double[][] array) {
        return new Matrix(array).det();
    }

    public static final String dimension(double[][] array) {
        return array.length + "x" + array[0].length;
    }

    public static final double[][] times(double[][] a, double value) {
        int size = a.length;
        double[][] result = new double[size][];
        for (int x = 0; x < size; x++) {
            result[x] = times(a[x], value);
        }
        return result;
    }

    public static final double[] times(double[] a, double value) {
        int size = a.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] * value;
        }
        return result;
    }

    public static final double divide(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("a.length != b.length");
        }
        int size = a.length;
        double result = 0;
        for (int x = 0; x < size; x++) {
            result += a[x] / b[x];
        }
        return result;
    }

    /**
   * ��˼�
   * @param a double[]
   * @return double[]
   */
    public static final double[] reciprocal(double[] a) {
        int size = a.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = 1. / a[x];
        }
        return result;
    }

    public static final int[] times(int[] a, int value) {
        int size = a.length;
        int[] result = new int[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] * value;
        }
        return result;
    }

    public static final double times(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("a.length != b.length");
        }
        int size = a.length;
        double result = 0;
        for (int x = 0; x < size; x++) {
            result += a[x] * b[x];
        }
        return result;
    }

    public static final double[] minus(double[] a, double[] b) {
        int size = a.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] - b[x];
        }
        return result;
    }

    public static final double[] modulus(double[] a, double[] b) {
        int size = a.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] % b[x];
        }
        return result;
    }

    public static final double[] minus(final double[] a, final double value) {
        return plus(a, -value);
    }

    public static final int[] plus(int[] a, int value) {
        int size = a.length;
        int[] result = new int[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] + value;
        }
        return result;
    }

    public static final double[] plus(final double[] a, final double value) {
        int size = a.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] + value;
        }
        return result;
    }

    public static final double[] plus(double[] a, double[] b) {
        int size = a.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = a[x] + b[x];
        }
        return result;
    }

    public static final double[][] plus(double[][] a, double[][] b) {
        int am = a.length;
        int an = a[0].length;
        double[][] result = new double[am][an];
        for (int i = 0; i < am; i++) {
            for (int j = 0; j < an; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }

    public static final double[][] minus(double[][] a, double[][] b) {
        int am = a.length;
        int an = a[0].length;
        double[][] result = new double[am][an];
        for (int i = 0; i < am; i++) {
            for (int j = 0; j < an; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }

    /**
   * ���� 3x3 * 3x1 ���x�}�ۭ��ֳt�B��
   * @param a double[][]
   * @param b double[]
   * @return double[]
   */
    public static final double[] timesFast(double[][] a, double[] b) {
        if (b.length != a.length || a.length != 3) {
            throw new IllegalArgumentException("inner dimensions must 3.");
        }
        double[] result = new double[3];
        result[0] = a[0][0] * b[0] + a[0][1] * b[1] + a[0][2] * b[2];
        result[1] = a[1][0] * b[0] + a[1][1] * b[1] + a[1][2] * b[2];
        result[2] = a[2][0] * b[0] + a[2][1] * b[1] + a[2][2] * b[2];
        return result;
    }

    /**
   * ���� 3x3 * 3x1 ���x�}�ۭ��B��
   * @param a double[][]
   * @param b double[]
   * @return double[]
   */
    public static final double[] times(double[][] a, double[] b) {
        return timesFast(a, b);
    }

    /**
   * ���� 1x3 * 3x3 ���x�}�ۭ��B��
   * @param a double[]
   * @param b double[][]
   * @return double[]
   */
    public static final double[] times(double[] a, double[][] b) {
        return timesFast(a, b);
    }

    /**
   * ���� 1x3 * 3x3 ���x�}�ۭ��ֳt�B��
   * ���ϥΰj��,�i�H��ֹB�⪺�ɶ��ΪŶ�
   * @param a double[]
   * @param b double[][]
   * @return double[]
   */
    public static final double[] timesFast(double[] a, double[][] b) {
        if (b.length != a.length || a.length != 3) {
            throw new IllegalArgumentException("inner dimensions must 3.");
        }
        double[] result = new double[3];
        result[0] = a[0] * b[0][0] + a[1] * b[1][0] + a[2] * b[2][0];
        result[1] = a[0] * b[0][1] + a[1] * b[1][1] + a[2] * b[2][1];
        result[2] = a[0] * b[0][2] + a[1] * b[1][2] + a[2] * b[2][2];
        return result;
    }

    public static final double[][] times(double[][] a, double[][] b) {
        if (b.length != a[0].length) {
            throw new IllegalArgumentException("inner dimensions must agree.");
        }
        int am = a.length;
        int an = a[0].length;
        int bn = b[0].length;
        double[][] result = new double[am][bn];
        double[] Bcolj = new double[an];
        for (int j = 0; j < bn; j++) {
            for (int k = 0; k < an; k++) {
                Bcolj[k] = b[k][j];
            }
            for (int i = 0; i < am; i++) {
                double[] Arowi = a[i];
                double s = 0;
                for (int k = 0; k < an; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                result[i][j] = s;
            }
        }
        return result;
    }

    public static final double[][] inverse(double[][] a) {
        Matrix m = new Matrix(a);
        return m.inverse().getArray();
    }

    /**
   *
   * @param a double[][]
   * @return double[][]
   */
    public static final double[][] pseudoInverse(double[][] a) {
        if (!isFullRank(a)) {
            double[][] t = DoubleArray.transpose(a);
            return DoubleArray.transpose(inverse(t));
        } else {
            return inverse(a);
        }
    }

    public static final int rank(double[][] a) {
        return new Jama.Matrix(a).rank();
    }

    public static final boolean isFullRank(double[][] a) {
        Jama.QRDecomposition qr = new Jama.QRDecomposition(new Jama.Matrix(a));
        return qr.isFullRank();
    }

    public static final boolean isNonsingular(double[][] a) {
        Jama.LUDecomposition lu = new Jama.LUDecomposition(new Jama.Matrix(a));
        return lu.isNonsingular();
    }

    public static double[][] mergeRows(double[]... array) {
        return org.math.array.DoubleArray.mergeRows(array);
    }

    public static double[][] mergeColumns(double[]... array) {
        return org.math.array.DoubleArray.mergeColumns(array);
    }

    public static double[] merge(double[]... array) {
        int size = array.length;
        int totalSize = 0;
        for (int x = 0; x < size; x++) {
            totalSize += array[x].length;
        }
        double[] merge = new double[totalSize];
        int index = 0;
        for (int x = 0; x < size; x++) {
            System.arraycopy(array[x], 0, merge, index, array[x].length);
            index += array[x].length;
        }
        return merge;
    }

    public static final double[][] mergeRows(double[][] array1, double[][] array2) {
        if (array1[0].length != array2[0].length) {
            throw new IllegalArgumentException("array1[0].length != array2[0].length");
        }
        int height = array1.length + array2.length;
        int width = array1[0].length;
        double[][] merge = new double[height][width];
        for (int x = 0; x < array1.length; x++) {
            System.arraycopy(array1[x], 0, merge[x], 0, width);
        }
        for (int x = 0; x < array2.length; x++) {
            System.arraycopy(array2[x], 0, merge[x + array1.length], 0, width);
        }
        return merge;
    }

    public static double[] copy(double[] M) {
        return org.math.array.DoubleArray.copy(M);
    }

    public static void copy(double[] source, double[] destination) {
        if (source == null || destination == null || source.length != destination.length) {
            throw new IllegalArgumentException("");
        }
        System.arraycopy(source, 0, destination, 0, source.length);
    }

    public static double[][] copy(double[][] M) {
        return org.math.array.DoubleArray.copy(M);
    }

    public static double[] abs(double[] M) {
        int size = M.length;
        for (int x = 0; x < size; x++) {
            M[x] = Math.abs(M[x]);
        }
        return M;
    }

    public static double[][] abs(double[][] M) {
        int m = M.length;
        int n = M[0].length;
        for (int x = 0; x < m; x++) {
            for (int y = 0; y < n; y++) {
                M[x][y] = Math.abs(M[x][y]);
            }
        }
        return M;
    }

    public static final double[] getCopyByIndex(double[] M, int[] indexes) {
        int size = indexes.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = M[indexes[x]];
        }
        return result;
    }

    public static double[] getRangeCopy(double[] M, int j1, int j2) {
        return org.math.array.DoubleArray.getRangeCopy(M, j1, j2);
    }

    public static double[] toDoubleArray(int[] intArray) {
        int size = intArray.length;
        double[] doubleArray = new double[size];
        for (int x = 0; x < size; x++) {
            doubleArray[x] = intArray[x];
        }
        return doubleArray;
    }

    public static double[][] toDoubleArray(int[][] intArray) {
        int m = intArray.length;
        int n = intArray[0].length;
        double[][] doubleArray = new double[m][n];
        for (int y = 0; y < m; y++) {
            for (int x = 0; x < n; x++) {
                doubleArray[y][x] = intArray[y][x];
            }
        }
        return doubleArray;
    }

    public static final boolean hasNegative(double[] m) {
        int size = m.length;
        for (int x = 0; x < size; x++) {
            if (m[x] < 0) {
                return true;
            }
        }
        return false;
    }

    public static double[][] toDoubleArray(float[][] array) {
        int height = array.length;
        int width = array[0].length;
        double[][] result = new double[height][width];
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                result[x][y] = array[x][y];
            }
        }
        return result;
    }

    public static float[] toFloatArray(double[] array) {
        int size = array.length;
        float[] result = new float[size];
        for (int x = 0; x < size; x++) {
            result[x] = (float) array[x];
        }
        return result;
    }

    public static double[] toDoubleArray(float[] array) {
        int size = array.length;
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = array[x];
        }
        return result;
    }

    public static double[][] toDoubleArray(float[] array, int width, int height) {
        if (array.length != width * height) {
            throw new IllegalArgumentException("array.length != width * height");
        }
        double[][] result = new double[height][width];
        int index = 0;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                result[x][y] = array[index++];
            }
        }
        return result;
    }

    public static int[] toIntArray(double[] array) {
        int size = array.length;
        int[] result = new int[size];
        for (int x = 0; x < size; x++) {
            result[x] = (int) Math.floor(array[x]);
        }
        return result;
    }

    /**
   * ��array���@�ֿn�ۥ[�p��
   * @param array double[]
   * @return double[]
   */
    public static final double[] accumulate(double[] array) {
        double[] accumulate = DoubleArray.copy(array);
        int size = accumulate.length;
        for (int x = 1; x < size; x++) {
            accumulate[x] = accumulate[x] + accumulate[x - 1];
        }
        return accumulate;
    }

    /**
   * Generate a two column matrix. Second column is just the values in <em>Y</em>
   * The first column is a uniform sequence of values from Xmin to Xmax. Step size
   * is automatic. Useful for generating values for an x axis when y is already
   * defined and bundling the pairs into a matrix.
   * Example:<br>
   * <code>
   * double[] y = {0.0, 1.0, 4.0, 9.0, 16.0};<br>
   * double[][] xy = buildXY(0.0, 4.0, y);<br>
   *
   * result:<br>
   * 0.0   0.0<br>
   * 1.0   1.0<br>
   * 2.0   4.0<br>
   * 3.0   9.0<br>
   * 4.0   16.0<br>
   * </code>
   *
   * @param Xmin double The first value in the first column
   * @param Xmax double The last value in the first column
   * @param Y double[] An array that will fill the second column.
   * @return double[][] nx2 array of values where n = length of y.
   */
    public static double[][] buildXY(double Xmin, double Xmax, final double[] Y) {
        return org.math.array.DoubleArray.buildXY(Xmin, Xmax, Y);
    }

    public static double[] buildX(double Xmin, double Xmax, int n) {
        if (Xmax < Xmin) {
            throw new IllegalArgumentException("First argument must be less than second");
        }
        double[] X = new double[n];
        for (int i = 0; i < n; i++) {
            X[i] = Xmin + (Xmax - Xmin) * (double) i / (double) (n - 1);
        }
        return X;
    }

    public static double[][] list2DoubleArray(List<double[]> list) {
        int size = list.size();
        double[][] doubleArray = new double[size][];
        for (int x = 0; x < size; x++) {
            doubleArray[x] = list.get(x);
        }
        return doubleArray;
    }

    public static double[] list2DoubleArray(List<Double> list) {
        int size = list.size();
        double[] result = new double[size];
        for (int x = 0; x < size; x++) {
            result[x] = list.get(x);
        }
        return result;
    }
}
