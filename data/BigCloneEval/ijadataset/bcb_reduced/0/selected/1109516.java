package com.dukesoftware.utils.data;

import java.io.PrintStream;

/**
 * 
 * 
 * 
 */
public class Matrix {

    private double[][] matrix;

    public Matrix(double[][] m) {
        int row = m[0].length;
        int line = m.length;
        this.matrix = new double[line][row];
        for (int i = 0; i < line; i++) {
            for (int j = 0; j < row; j++) {
                this.matrix[i][j] = m[i][j];
            }
        }
    }

    public Matrix(int m, int n) {
        this.matrix = new double[m][n];
    }

    public double getElem(int y, int x) {
        return matrix[y][x];
    }

    public void setElem(int y, int x, double val) {
        matrix[y][x] = val;
    }

    public int getLineLength() {
        return matrix.length;
    }

    public int getRowLength() {
        return matrix[0].length;
    }

    public static double[][] convertMatrixToArray(Matrix in) {
        final int row = in.getRowLength();
        final int line = in.getLineLength();
        double[][] ans = new double[line][row];
        for (int i = 0; i < line; i++) {
            for (int j = 0; j < row; j++) {
                ans[i][j] = in.matrix[i][j];
            }
        }
        return ans;
    }

    public static Matrix convertArrayTomatrix(double[][] in) {
        return new Matrix(in);
    }

    public static double[][] product(double[][] a, double[][] b) {
        final int arow = a[0].length;
        final int aline = a.length;
        final int brow = b[0].length;
        final int bline = b.length;
        double[][] ans = new double[aline][brow];
        if (arow == bline) {
            for (int i = 0; i < aline; i++) {
                for (int j = 0; j < brow; j++) {
                    for (int k = 0; k < arow; k++) {
                        ans[i][j] += (a[i][k] * b[k][j]);
                    }
                }
            }
            return ans;
        } else return null;
    }

    public static Matrix product(Matrix a, Matrix b) {
        final int arow = a.getRowLength();
        final int aline = a.getLineLength();
        final int brow = b.getRowLength();
        final int bline = b.getLineLength();
        Matrix ans = new Matrix(brow, aline);
        if (arow == bline) {
            for (int i = 0; i < aline; i++) {
                for (int j = 0; j < brow; j++) {
                    double tmp = ans.getElem(i, j);
                    for (int k = 0; k < arow; k++) {
                        tmp += (a.getElem(i, k) * b.getElem(k, j));
                    }
                    ans.setElem(i, j, tmp);
                }
            }
            return ans;
        } else return null;
    }

    public static void addOperationToMatrix(double[][] matrix, int y, int x, double val) {
        if (checkRange(matrix, y, x)) matrix[y][x] += val;
    }

    public static void productOperationToMatrix(double[][] matrix, int y, int x, double val) {
        if (checkRange(matrix, y, x)) matrix[y][x] *= val;
    }

    private static boolean checkRange(double[][] matrix, int y, int x) {
        int row = matrix[0].length;
        int line = matrix.length;
        return x >= 0 && x < row && y >= 0 && y < line;
    }

    public static double[][] addition(double[][] a, double[][] b) {
        int arow = a[0].length;
        int aline = a.length;
        int brow = b[0].length;
        int bline = b.length;
        if (arow == brow && aline == bline) {
            double[][] ans = new double[aline][arow];
            for (int i = 0; i < aline; i++) {
                for (int j = 0; j < arow; j++) {
                    ans[i][j] = a[i][j] + b[i][j];
                }
            }
            return ans;
        } else {
            return null;
        }
    }

    public static double[][] constProduct(double c, double[][] matrix) {
        int row = matrix[0].length;
        int line = matrix.length;
        double[][] ans = new double[line][row];
        for (int i = 0; i < line; i++) {
            for (int j = 0; j < row; j++) {
                ans[i][j] = matrix[i][j] * c;
            }
        }
        return ans;
    }

    public static double[][] transposed(double[][] matrix) {
        int row = matrix[0].length;
        int line = matrix.length;
        double[][] ans = new double[row][line];
        for (int i = 0; i < line; i++) {
            for (int j = 0; j < row; j++) {
                ans[j][i] = matrix[i][j];
            }
        }
        return ans;
    }

    public static double[][] convertVectorToMatrix(double[] vector) {
        int line = vector.length;
        double[][] ans = new double[line][1];
        for (int i = 0; i < line; i++) ans[i][0] = vector[i];
        return ans;
    }

    public static double[] convertMatrixToVector(double[][] matrix) {
        int line = matrix.length;
        double[] ans = new double[line];
        for (int i = 0; i < line; i++) ans[i] = matrix[i][0];
        return ans;
    }

    /**
	 * Method for calculate inverse matrix.<br>
	 * 
	 * Warning: 
	 * This method douesn't check whether inverse matrix exsists or not.
	 */
    public static double[][] inverseMatrix(double[][] matrix) {
        int l = matrix.length;
        if (l == matrix[0].length) {
            double[][] c = new double[l][l];
            for (int i = 0; i < l; i++) {
                for (int j = i + 1; j < l; j++) {
                    matrix[j][i] /= matrix[i][i];
                    for (int k = i + 1; k < l; k++) {
                        matrix[j][k] -= matrix[i][k] * matrix[j][i];
                    }
                }
            }
            for (int k = 0; k < l; k++) {
                for (int i = 0; i < l; i++) {
                    if (i == k) c[i][k] = 1; else c[i][k] = 0;
                }
                for (int i = 0; i < l; i++) {
                    for (int j = i + 1; j < l; j++) {
                        c[j][k] -= c[i][k] * matrix[j][i];
                    }
                }
                for (int i = l - 1; i >= 0; i--) {
                    for (int j = i + 1; j < l; j++) {
                        c[i][k] -= matrix[i][j] * c[j][k];
                    }
                    c[i][k] /= matrix[i][i];
                }
            }
            return c;
        } else {
            return null;
        }
    }

    public static double[][] inverseMatrix3x3(double[][] a) {
        if (a.length == 3 && a[0].length == 3) {
            double det = a[0][0] * a[1][1] * a[2][2] + a[1][0] * a[2][1] * a[0][2] + a[2][0] * a[0][1] * a[1][2] - a[0][0] * a[2][1] * a[1][2] - a[2][0] * a[1][1] * a[0][2] - a[1][0] * a[0][1] * a[2][2];
            if (det != 0) {
                double[][] c = { { a[1][1] * a[2][2] - a[1][2] * a[2][1], a[0][2] * a[2][1] - a[0][1] * a[2][2], a[0][1] * a[1][2] - a[0][2] * a[1][1] }, { a[1][2] * a[2][0] - a[1][0] * a[2][2], a[0][0] * a[2][2] - a[0][2] * a[2][0], a[0][2] * a[1][0] - a[0][0] * a[1][2] }, { a[1][0] * a[2][1] - a[1][1] * a[2][0], a[0][2] * a[2][0] - a[0][0] * a[2][1], a[0][0] * a[1][1] - a[0][2] * a[1][0] } };
                return (Matrix.constProduct(1 / det, c));
            }
        }
        return null;
    }

    public void printMatrix(PrintStream out) {
        printMatrix(out);
    }

    public static void printMatrix(double[][] matrix, PrintStream out) {
        int row = matrix[0].length;
        int line = matrix.length;
        for (int i = 0; i < line; i++) {
            for (int j = 0; j < row; j++) {
                out.print(matrix[i][j]);
                if (j != row - 1) System.out.print(", ");
            }
            out.println();
        }
    }
}
