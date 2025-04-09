package ru.ifmo.optimization.util.vectors;

public class Matrix {

    public static double[][] mult(double[][] m1, double[][] m2) {
        assert (m1.length > 0);
        int m = m1.length;
        int n = m1[0].length;
        assert (n == m2.length);
        assert (n > 0);
        int k = m2[0].length;
        double[][] ret = new double[m][k];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < k; j++) {
                ret[i][j] = 0;
                for (int l = 0; l < n; l++) {
                    ret[i][j] += m1[i][l] * m2[l][j];
                }
            }
        }
        return ret;
    }

    public static double[] mult(double[][] m1, double[] X) {
        assert (m1.length > 0);
        int m = m1.length;
        int n = m1[0].length;
        assert (n == X.length);
        assert (n > 0);
        double[] ret = new double[m];
        for (int i = 0; i < m; i++) {
            ret[i] = 0;
            for (int l = 0; l < n; l++) {
                ret[i] += m1[i][l] * X[l];
            }
        }
        return ret;
    }

    public static double[][] transp(double[][] m1) {
        int m = m1.length;
        int n = m1[0].length;
        double[][] ret = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ret[j][i] = m1[i][j];
            }
        }
        return ret;
    }

    public static double[][] inverse(double[][] m1) {
        int m = m1.length;
        int n = m1[0].length;
        assert (n == m);
        assert (n == 2);
        double[][] ret = new double[2][2];
        ret[0][0] = m1[1][1];
        ret[0][1] = -m1[0][1];
        ret[1][0] = -m1[1][0];
        ret[1][1] = m1[0][0];
        double a = m1[0][0] * m1[1][1] - m1[0][1] * m1[1][0];
        return mult(ret, 1 / a);
    }

    public static double[][] mult(double[][] m1, double a) {
        int m = m1.length;
        int n = m1[0].length;
        double[][] ret = new double[n][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ret[i][j] = m1[i][j] * a;
            }
        }
        return ret;
    }

    public static double maxDiag(double[][] m1) {
        int m = m1.length;
        int n = m1[0].length;
        assert (m == n);
        double max = m1[0][0];
        for (int i = 1; i < n; i++) {
            if (m1[i][i] > max) {
                max = m1[i][i];
            }
        }
        return max;
    }

    public static double[][] add(double[][] m1, double[][] m2) {
        int m = m1.length;
        int n = m1[0].length;
        assert (m == m2.length);
        assert (n == m2[0].length);
        double[][] ret = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ret[i][j] = m1[i][j] + m2[i][j];
            }
        }
        return ret;
    }
}
