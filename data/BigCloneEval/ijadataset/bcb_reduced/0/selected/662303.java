package ch.unibe.inkml.util;

public class MatrixCalculator {

    private int iDF = 0;

    public double[][] AddMatrix(double[][] a, double[][] b) throws Exception {
        int tms = a.length;
        int tmsB = b.length;
        if (tms != tmsB) {
            throw new IllegalArgumentException("Matrix Size Mismatch");
        }
        double[][] matrix = new double[tms][tms];
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            matrix[i][j] = a[i][j] + b[i][j];
        }
        return matrix;
    }

    public double[][] MultiplyMatrix(final double[][] a, final double[][] b) throws Exception {
        if (a[0].length != b.length) throw new Exception("Matrices incompatible for multiplication");
        double matrix[][] = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) for (int j = 0; j < b[i].length; j++) matrix[i][j] = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = calculateRowColumnProduct(a, i, b, j);
            }
        }
        return matrix;
    }

    public float calculateRowColumnProduct(final double[][] A, int row, final double[][] B, int col) {
        float product = 0;
        for (int i = 0; i < A[row].length; i++) product += A[row][i] * B[i][col];
        return product;
    }

    public double[][] Transpose(final double[][] a) {
        double m[][] = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) for (int j = 0; j < a[i].length; j++) m[j][i] = a[i][j];
        return m;
    }

    public double[][] Inverse(final double[][] a) {
        int tms = a.length;
        double m[][] = new double[tms][tms];
        double mm[][] = Adjoint(a);
        double det = Determinant(a);
        double dd = 0;
        if (det == 0) {
            throw new IllegalArgumentException("Determinant Equals 0, Not Invertible.");
        } else {
            dd = 1 / det;
        }
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            m[i][j] = dd * mm[i][j];
        }
        return m;
    }

    public double[][] Adjoint(final double[][] a) {
        int tms = a.length;
        double m[][] = new double[tms][tms];
        int ii, jj, ia, ja;
        double det;
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            ia = ja = 0;
            double ap[][] = new double[tms - 1][tms - 1];
            for (ii = 0; ii < tms; ii++) {
                for (jj = 0; jj < tms; jj++) {
                    if ((ii != i) && (jj != j)) {
                        ap[ia][ja] = a[ii][jj];
                        ja++;
                    }
                }
                if ((ii != i) && (jj != j)) {
                    ia++;
                }
                ja = 0;
            }
            det = Determinant(ap);
            m[i][j] = (float) Math.pow(-1, i + j) * det;
        }
        m = Transpose(m);
        return m;
    }

    public double[][] UpperTriangle(final double[][] matrix) {
        double f1 = 0;
        double temp = 0;
        int tms = matrix.length;
        double[][] m = clone(matrix);
        int v = 1;
        iDF = 1;
        for (int col = 0; col < tms - 1; col++) {
            for (int row = col + 1; row < tms; row++) {
                v = 1;
                outahere: while (m[col][col] == 0) {
                    if (col + v >= tms) {
                        iDF = 0;
                        break outahere;
                    } else {
                        for (int c = 0; c < tms; c++) {
                            temp = m[col][c];
                            m[col][c] = m[col + v][c];
                            m[col + v][c] = temp;
                        }
                        v++;
                        iDF = iDF * -1;
                    }
                }
                if (m[col][col] != 0) {
                    f1 = (-1) * m[row][col] / m[col][col];
                    for (int i = col; i < tms; i++) {
                        m[row][i] = f1 * m[col][i] + m[row][i];
                    }
                }
            }
        }
        return m;
    }

    private double[][] clone(final double[][] matrix) {
        double[][] res = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                res[i][j] = matrix[i][j];
            }
        }
        return res;
    }

    public double Determinant(final double[][] matrix) {
        int tms = matrix.length;
        double det = 1;
        double[][] m = UpperTriangle(matrix);
        for (int i = 0; i < tms; i++) {
            det = det * m[i][i];
        }
        det = det * iDF;
        return det;
    }
}
