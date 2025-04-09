package com.threecam.math;

public class Matrix {

    int rows;

    int cols;

    double[][] contents;

    public Matrix(int rows, int cols) {
        this.contents = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    public Matrix(double[][] contents) {
        this.contents = contents;
        this.rows = contents.length;
        this.cols = contents[0].length;
    }

    public void set(int row, int col, double value) {
        contents[row - 1][col - 1] = value;
    }

    public double get(int row, int col) {
        return contents[row - 1][col - 1];
    }

    public Matrix transpose() {
        Matrix output = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                output.contents[j][i] = contents[i][j];
            }
        }
        return output;
    }

    public Matrix multiply(Matrix two) {
        if (cols != two.rows) {
            System.out.println("BUG WARNING:  Attempted to multiply " + rows + "x" + cols + " matrix with " + two.rows + "x" + two.cols + " matrix!");
            return null;
        }
        Matrix output = new Matrix(rows, two.cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < two.cols; j++) {
                double value = 0;
                for (int k = 0; k < cols; k++) {
                    value += contents[i][k] * two.contents[k][j];
                }
                output.contents[i][j] = value;
            }
        }
        return output;
    }
}
