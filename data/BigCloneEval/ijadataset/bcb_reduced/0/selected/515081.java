package org.jmathematics.calc;

import java.util.Arrays;

public final class MatrixUtils {

    public static void swapRows(int from, int to, double[][] matrix) {
        double[] x = Arrays.copyOf(matrix[from], matrix[from].length);
        matrix[from] = matrix[to];
        matrix[to] = x;
    }

    private static void pivotRow(int row, int pivotColumn, double[][] matrix) {
        double pivot = matrix[row][pivotColumn];
        if (pivot == 0.0) throw new RuntimeException("DivisionByZero");
        if (pivot == 1.0) return;
        VectorUtils.divide(matrix[row], pivot);
    }

    private static void pivotColumn(int pivotRow, int pivotColumn, double[][] matrix) {
        int rows = matrix.length;
        for (int row = 0; row < rows; row++) if (row != pivotRow && matrix[row][pivotColumn] != 0.0) VectorUtils.subtract(matrix[row], matrix[row][pivotColumn], matrix[pivotRow]);
    }

    public static void pivot(int row, int column, double[][] matrix) {
        pivotRow(row, column, matrix);
        pivotColumn(row, column, matrix);
    }

    public static double[][] invert(double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int row = 0; row < matrix.length; row++) for (int col = 0; col < matrix[row].length; col++) result[col][row] = result[row][col];
        return result;
    }

    public static double[][] appendRow(double[][] matrix, double[] row) {
        double[][] result = Arrays.copyOf(matrix, matrix.length + 1);
        result[matrix.length] = row;
        return result;
    }

    public static double[][] appendColumn(double[][] matrix, double[] column) {
        double[][] result = new double[matrix.length][];
        for (int row = 0; row < result.length; row++) result[row] = VectorUtils.append(matrix[row], column[row]);
        return result;
    }

    public static double[] getColumn(double[][] matrix, int column) {
        double[] result = new double[matrix.length];
        for (int row = 0; row < result.length; row++) result[row] = matrix[row][column];
        return result;
    }
}
