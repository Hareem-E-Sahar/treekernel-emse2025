package ev.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class Matrix<T> {

    public static final double TOL = .00000001;

    public static double[][] identity(final int n) {
        final double[][] res = new double[n][n];
        for (int i = 0; i < n; i++) {
            res[i][i] = 1.0;
        }
        return res;
    }

    public static double[][] matrix(final int rows, final int cols, final double value) {
        final double[][] res = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res[i][j] = value;
            }
        }
        return res;
    }

    public static double max(final double[] vec) {
        double max = Double.MIN_VALUE;
        for (final double d : vec) {
            if (!Double.isNaN(d) && d > max) {
                max = d;
            }
        }
        return max;
    }

    public static int max(final int[] vec) {
        int max = Integer.MIN_VALUE;
        for (final int d : vec) {
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    public static double min(final double[] vec) {
        double min = Double.MAX_VALUE;
        for (final double d : vec) {
            if (!Double.isNaN(d) && d < min) {
                min = d;
            }
        }
        return min;
    }

    public static double max(final ArrayList<Double> vec) {
        double max = Double.MIN_VALUE;
        for (final double d : vec) {
            if (!Double.isNaN(d) && d > max) {
                max = d;
            }
        }
        return max;
    }

    public static double min(final ArrayList<Double> vec) {
        double min = Double.MAX_VALUE;
        for (final double d : vec) {
            if (!Double.isNaN(d) && d < min) {
                min = d;
            }
        }
        return min;
    }

    public static double variance(final double[] vec) {
        final double mean = average(vec);
        double var = 0.0;
        int counter = 0;
        for (int i = 0; i < vec.length; i++) {
            if (!Double.isNaN(vec[i])) {
                var += Math.pow((vec[i] - mean), 2.0);
                counter++;
            }
        }
        var /= (counter - 1.0);
        return var;
    }

    public static double covariance(final double[] vec1, final double[] vec2) {
        final double mean1 = average(vec1);
        final double mean2 = average(vec2);
        double cvar = 0;
        int counter = 0;
        for (int i = 0; i < vec1.length; i++) {
            if (!Double.isNaN(vec1[i]) && !Double.isNaN(vec2[i])) {
                cvar += (vec1[i] - mean1) * (vec2[i] - mean2);
                counter++;
            }
        }
        cvar /= (counter);
        return cvar;
    }

    public static double variance(final Vector<Double> vec) {
        final double mean = average(vec);
        double var = 0;
        int counter = 0;
        for (int i = 0; i < vec.size(); i++) {
            if (!Double.isNaN(vec.get(i))) {
                var += Math.pow((vec.get(i) - mean), 2.0);
                counter++;
            }
        }
        var /= (counter - 1.0);
        return var;
    }

    public static double average(final Vector<Double> vec) {
        double mean = 0;
        int counter = 0;
        for (int i = 0; i < vec.size(); i++) {
            if (!Double.isNaN(vec.get(i))) {
                mean += vec.get(i);
                counter++;
            }
        }
        if (counter == 0) {
            return Double.NaN;
        }
        mean /= (counter);
        return mean;
    }

    public static double average(final double[] vec) {
        double mean = 0.0;
        int counter = 0;
        for (int i = 0; i < vec.length; i++) {
            if (!Double.isNaN(vec[i]) & !Double.isInfinite(vec[i])) {
                mean += vec[i];
                counter++;
            }
        }
        mean /= (counter);
        return mean;
    }

    public static double sem(final double[] vals) {
        return Math.sqrt(variance(vals) / (vals.length));
    }

    public static double median(final double[] vec) {
        Arrays.sort(vec);
        if (vec.length % 2 == 0) {
            return (vec[vec.length / 2 - 1] + vec[vec.length / 2]) / 2.0;
        } else {
            return vec[vec.length / 2];
        }
    }

    public static double[] getCol(final double[][] mat, final int c) {
        final double[] column = new double[mat.length];
        for (int i = 0; i < mat.length; i++) {
            column[i] = mat[i][c];
        }
        return column;
    }

    public static float[] getCol(final float[][] mat, final int c) {
        final float[] column = new float[mat.length];
        for (int i = 0; i < mat.length; i++) {
            column[i] = mat[i][c];
        }
        return column;
    }

    /**
     * Returns averages of t's columns
     * 
     * @param t
     *            matrix
     * @return
     */
    public static double[] getColAverages(final double[][] mat) {
        final double[] avgs = new double[mat[0].length];
        for (int i = 0; i < avgs.length; i++) {
            avgs[i] = average(getCol(mat, i));
        }
        return avgs;
    }

    /**
     * Returns global mean of c1 & c2
     * 
     * @return
     */
    public static double[] getGMean(final double[][] c1, final double[][] c2, final double ap1, final double ap2) {
        final double mean1[] = new double[c1[0].length];
        final double mean2[] = new double[c2[0].length];
        for (int i = 0; i < c1[0].length; i++) {
            for (int j = 0; j < c1.length; j++) {
                mean1[i] += c1[j][i];
            }
            mean1[i] /= c1.length;
            mean1[i] *= ap1;
        }
        for (int i = 0; i < c2[0].length; i++) {
            for (int j = 0; j < c2.length; j++) {
                mean2[i] += c2[j][i];
            }
            mean2[i] /= c2.length;
            mean2[i] *= ap2;
        }
        return sum(mean1, mean2);
    }

    /**
     * Returns mahalanobis distance between x and class center mean
     * 
     * @param x
     * @param mean
     * @param cov
     *            INVERTED covariance matrix
     * @return
     */
    public static double mahalanobis(final double[] x, final double[] mean, final double[][] icov) {
        final double res = matrixProd(matrixProd(diff(x, mean), icov), diff(x, mean));
        return res;
    }

    /**
     * Set zero center
     * 
     * @param ech
     * @return
     */
    public static double[][] setzerocenter(final double[][] m) {
        final double[] mean = getColAverages(m);
        final double[][] zeroc = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                zeroc[i][j] = m[i][j] - mean[j];
            }
        }
        return zeroc;
    }

    /**
     * Returns the covariance matrix of data matrix m, where rows are samples
     * and colums are features
     * 
     * @param m
     * @return
     */
    public static double[][] covariance(final double[][] m) {
        final double[][] zeroc = setzerocenter(m);
        final double[][] cov = matrixProd(transpose(zeroc), zeroc);
        for (int i = 0; i < cov[0].length; i++) {
            for (int j = 0; j < cov.length; j++) {
                cov[i][j] = cov[i][j] / (m.length - 1);
            }
        }
        return cov;
    }

    /**
     * Returns within-class scatter matrix
     * 
     * @param c1
     *            covariance matrix for class 1
     * @param c2
     *            covariance matrix for class 2
     * @param ap1
     *            a priori probability of class 1
     * @param ap2
     *            a priori probability of class 2
     * @return
     */
    public static double[][] sw(final double[][] c1, final double[][] c2, final double ap1, final double ap2) {
        double[][] swi;
        swi = sum(mul(c1, ap1), mul(c2, ap2));
        return swi;
    }

    /**
     * Between-class scatter matrix
     * 
     * @param c1
     *            class 1 data matrix
     * @param c2
     *            class 2 data matrix
     * @param ap1
     *            a priori probability of class 1
     * @param ap2
     *            a priori probability of class 2
     * @return
     */
    public static double[][] sb(final double[][] c1, final double[][] c2, final double ap1, final double ap2) {
        double[][] sbi;
        final double[] gmean = getGMean(c1, c2, ap1, ap2);
        double[] cmean1, cmean2;
        cmean1 = getColAverages(c1);
        cmean2 = getColAverages(c2);
        sbi = sum(mul(matrixProd2(diff(cmean1, gmean), diff(cmean1, gmean)), ap1), mul(matrixProd2(diff(cmean2, gmean), diff(cmean2, gmean)), ap2));
        return sbi;
    }

    /**
     * Mixture scatter matrix
     * 
     * @param sw
     * @param sb
     * @return
     */
    public static double[][] sm(final double[][] sw, final double[][] sb) {
        return sum(sw, sb);
    }

    public static double[][] submatrix(final double[][] mat, final int m, final int n) {
        final double[][] res = new double[mat.length - 1][mat[0].length - 1];
        for (int i = 0; i < m - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                res[i][j] = mat[i][j];
            }
            for (int j = n; j < mat[0].length; j++) {
                res[i][j - 1] = mat[i][j];
            }
        }
        for (int i = m; i < mat.length; i++) {
            for (int j = 0; j < n - 1; j++) {
                res[i - 1][j] = mat[i][j];
            }
            for (int j = n; j < mat[0].length; j++) {
                res[i - 1][j - 1] = mat[i][j];
            }
        }
        return res;
    }

    public static double[][] submatrix(final double[][] mat, final int[] rowis, final int c1, final int cn) {
        final double[][] res = new double[rowis.length][cn - c1 + 1];
        for (int i = 0; i < rowis.length; i++) {
            for (int j = c1; j <= cn; j++) {
                res[i][j - c1] = mat[rowis[i]][j];
            }
        }
        return res;
    }

    public static double[] subvector(final double[] vec, final int[] indices) {
        final double[] res = new double[indices.length];
        int cnt = 0;
        for (final int i : indices) {
            res[cnt++] = vec[i];
        }
        return res;
    }

    /**
     * Returns product of m1 and m2
     * 
     * @param mat1
     * @param mat2
     * @return
     */
    public static double[][] matrixProd(final double[][] m1, final double[][] m2) {
        if (m1[0].length != m2.length) {
            throw new RuntimeException("Dimension");
        }
        final double[][] res = new double[m1.length][m2[0].length];
        double s;
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m2[0].length; j++) {
                s = 0;
                for (int k = 0; k < m2.length; k++) {
                    s += m1[i][k] * m2[k][j];
                }
                res[i][j] = s;
            }
        }
        return res;
    }

    /**
     * Returns (product of m1 and m2^T)^T
     * 
     * @param mat1
     * @param mat2
     * @return
     */
    public static double[] matrixProd(final double[][] m1, final double[] m2) {
        if (m1[0].length != m2.length) {
            throw new RuntimeException("Dimension");
        }
        final double[] res = new double[m1.length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m2.length; j++) {
                res[i] += m1[i][j] * m2[j];
            }
        }
        return res;
    }

    /**
     * Returns product of m1 and m2^T
     * 
     * @param mat1
     * @param mat2
     * @return
     */
    public static double matrixProd(final double[] m1, final double[] m2) {
        double res = 0.0;
        for (int i = 0; i < m1.length; i++) {
            res += m1[i] * m2[i];
        }
        return res;
    }

    /**
     * Returns product of m1^T and m2
     * 
     * @param mat1
     * @param mat2
     * @return
     */
    public static double[] matrixProd(final double[] m1, final double[][] m2) {
        final double[] res = new double[m2[0].length];
        for (int i = 0; i < m2[0].length; i++) {
            for (int k = 0; k < m1.length; k++) {
                res[i] += m1[k] * m2[k][i];
            }
        }
        return res;
    }

    /**
     * Returns product of m1 and m2^T
     * 
     * @param mat1
     * @param mat2
     * @return
     */
    public static double[][] matrixProd2(final double[] m1, final double[] m2) {
        final double[][] res = new double[m1.length][m2.length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m2.length; j++) {
                res[i][j] = m1[i] * m2[j];
            }
        }
        return res;
    }

    /**
     * Returns transpose of m
     * 
     * @param m
     * @return
     */
    public static double[][] transpose(final double[][] m) {
        final double[][] mt = new double[m[0].length][m.length];
        for (int i = 0; i < mt.length; i++) {
            for (int j = 0; j < mt[i].length; j++) {
                mt[i][j] = m[j][i];
            }
        }
        return mt;
    }

    /**
     * Sum of matrices
     * 
     * @param m1
     * @param m2
     * @return
     */
    public static double[][] sum(final double[][] m1, final double[][] m2) {
        if ((m1.length != m2.length) || (m1[0].length != m2[0].length)) {
            throw new RuntimeException("Sum: size difference");
        }
        final double[][] s = new double[m1.length][m1[0].length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m1[i].length; j++) {
                s[i][j] = m1[i][j] + m2[i][j];
            }
        }
        return s;
    }

    /**
     * Difference of matrices m1 - m2
     * 
     * @param m1
     * @param m2
     * @return
     */
    public static double[][] diff(final double[][] m1, final double[][] m2) {
        if ((m1.length != m2.length) || (m1[0].length != m2[0].length)) {
            throw new RuntimeException("diff: size difference");
        }
        final double[][] s = new double[m1.length][m1[0].length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m1[i].length; j++) {
                s[i][j] = m1[i][j] - m2[i][j];
            }
        }
        return s;
    }

    /**
     * Sum of vectors
     * 
     * @param v1
     * @param v2
     * @return
     */
    public static double[] sum(final double[] v1, final double[] v2) {
        final double[] v = new double[v1.length];
        for (int i = 0; i < v.length; i++) {
            v[i] = v1[i] + v2[i];
        }
        return v;
    }

    /**
     * Difference of vectors (v1 - v2)
     * 
     * @param v1
     * @param v2
     * @return
     */
    public static double[] diff(final double[] v1, final double[] v2) {
        final double[] v = new double[v1.length];
        for (int i = 0; i < v.length; i++) {
            v[i] = v1[i] - v2[i];
        }
        return v;
    }

    public static double dist(final double[] v1, final double[] v2) {
        double d = 0;
        for (int i = 0; i < v1.length; i++) {
            d += Math.pow(v1[i] - v2[i], 2.0);
        }
        return Math.sqrt(d);
    }

    /**
     * Scalar multiply
     * 
     * @param m
     * @param p
     * @return
     */
    public static double[][] mul(final double[][] m, final double p) {
        final double[][] s = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                s[i][j] = p * m[i][j];
            }
        }
        return s;
    }

    /**
     * Scalar multiply
     * 
     * @param m
     * @param p
     * @return
     */
    public static double[][] mul(final double[][] m, final double[] p) {
        final double[][] s = new double[m.length][m[0].length];
        if (p.length != m.length) {
            System.out.println("Matrix mul: error");
        }
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                s[i][j] = p[i] * m[i][j];
            }
        }
        return s;
    }

    /**
     * Scalar multiply
     * 
     * @param m
     * @param p
     * @return
     */
    public static double[] mul(final double[] v, final double[] p) {
        final double[] s = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            s[i] = v[i] * p[i];
        }
        return s;
    }

    public static boolean equals(final double[][] mat, final double val) {
        final int m = mat.length;
        final int n = mat[0].length;
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (Math.abs(mat[r][c] - val) > TOL) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double dot(final double[][] m1, final double[][] m2) {
        final int m = m1.length;
        final int n = m1[0].length;
        double sum = 0;
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                sum += m1[r][c] * m2[r][c];
            }
        }
        return sum;
    }

    public static double dot(final double[] v1, final double[] v2) {
        final int m = v1.length;
        double sum = 0;
        for (int r = 0; r < m; r++) {
            sum += v1[r] * v2[r];
        }
        return sum;
    }

    public static double[] diag(final double[][] mat) {
        final int m = mat.length;
        final int n = mat[0].length;
        final double[] diag = new double[Math.min(m, n)];
        for (int i = 0; i < diag.length; i++) {
            diag[i] = mat[i][i];
        }
        return diag;
    }

    public static double trace(final double[][] m) {
        double tr = 0.0;
        for (int i = 0; i < Math.min(m.length, m[0].length); i++) {
            tr += m[i][i];
        }
        return tr;
    }

    /**
     * Find indexes of val from vec for dirs: -1 vec[i] < val 0 vec[i] = val 1
     * vec[i] > val
     */
    public static double[] find(final double[] vec, final double val, final int dir) {
        final double[] res = new double[count(vec, val, dir)];
        int cnt = 0;
        for (int i = 0; i < vec.length; i++) {
            if (((dir == -1) && (vec[i] < val)) || ((dir == 0) && (vec[i] == val)) || ((dir == 1) && (vec[i] > val))) {
                res[cnt] = vec[i];
                ++cnt;
            }
        }
        return res;
    }

    /**
     * Count indexes of val from vec for dirs: -1 vec[i] < val 0 vec[i] = val 1
     * vec[i] > val
     */
    public static int count(final double[] vec, final double val, final int dir) {
        int count = 0;
        for (int i = 0; i < vec.length; i++) {
            if (((dir == -1) && (vec[i] < val)) || ((dir == 0) && (vec[i] == val)) || ((dir == 1) && (vec[i] > val))) {
                ++count;
            }
        }
        return count;
    }

    public static void print(final float[][] mat) {
        for (int i = 0; i < mat.length; i++) {
            System.out.print("[");
            for (int j = 0; j < mat[0].length; j++) {
                System.out.print(" " + mat[i][j] + " ");
            }
            System.out.print("]");
            System.out.println();
        }
    }

    public static void print(final double[][] mat) {
        for (int i = 0; i < mat.length; i++) {
            System.out.print("[");
            for (int j = 0; j < mat[0].length; j++) {
                System.out.print(" " + mat[i][j] + " ");
            }
            System.out.print("]");
            System.out.println();
        }
    }

    public static void print(final double[] mat) {
        System.out.print("[");
        for (int i = 0; i < mat.length; i++) {
            System.out.print(" " + mat[i] + " ");
        }
        System.out.print("]");
        System.out.println();
    }

    public static void print(final float[] mat) {
        System.out.print("[");
        for (int i = 0; i < mat.length; i++) {
            System.out.print(mat[i] + " ");
        }
        System.out.println("]");
    }

    public static double[][] toDouble(final BigDecimal[][] m1) {
        final double[][] res = new double[m1.length][m1[0].length];
        for (int j = 0; j < m1.length; j++) {
            for (int i = 0; i < m1[0].length; i++) {
                res[j][i] = m1[j][i].doubleValue();
            }
        }
        return res;
    }

    public static double[][] toDouble(final float[][] m1) {
        final double[][] res = new double[m1.length][m1[0].length];
        for (int j = 0; j < m1.length; j++) {
            for (int i = 0; i < m1[0].length; i++) {
                res[j][i] = m1[j][i];
            }
        }
        return res;
    }

    public static float[][] toFloat(final double[][] m1) {
        final float[][] res = new float[m1.length][m1[0].length];
        for (int j = 0; j < m1.length; j++) {
            for (int i = 0; i < m1[0].length; i++) {
                res[j][i] = (float) m1[j][i];
            }
        }
        return res;
    }

    /**
     * Fast Moore-Penrose pseudoinverse Java implementation of Pierre Courrier's
     * matlab code Neural Information Processing - Letters and Reviews 8(2),
     * 2005
     */
    public static double[][] pseudoinverse(final double[][] mat) {
        int r;
        final int m = mat.length;
        int n = mat[0].length;
        double[][] Y, A;
        boolean transpose = false;
        if (m < n) {
            transpose = true;
            A = matrixProd(mat, transpose(mat));
            n = m;
        } else {
            A = matrixProd(transpose(mat), mat);
        }
        final double[] dA = diag(A);
        final double tol = minGTzero(dA) * 0.000000001;
        double[][] L = matrix(A.length, A[0].length, 0);
        r = -1;
        for (int k = 0; k < n; k++) {
            ++r;
            double[] tmp;
            if (r == 0) {
                tmp = submatrixC(A, k, n - 1, k);
            } else {
                tmp = diff(submatrixC(A, k, n - 1, k), matrixProd(submatrixRC(L, k, n - 1, 0, r - 1), submatrixR(L, k, 0, r - 1)));
            }
            for (int i = k; i < n; i++) {
                L[i][r] = tmp[i - k];
            }
            if (L[k][r] > tol) {
                L[k][r] = Math.sqrt(L[k][r]);
                if (k < n - 1) {
                    for (int i = k + 1; i < n; i++) {
                        L[i][r] = L[i][r] / L[k][r];
                    }
                }
            } else {
                --r;
            }
        }
        L = submatrixRC(L, 0, L.length - 1, 0, r);
        final double[][] M = inverse(matrixProd(transpose(L), L));
        if (transpose) {
            Y = matrixProd(transpose(mat), matrixProd(L, matrixProd(M, matrixProd(M, transpose(L)))));
        } else {
            Y = matrixProd(L, matrixProd(M, matrixProd(M, matrixProd(transpose(L), transpose(mat)))));
        }
        return Y;
    }

    static double minGTzero(final double[] vec) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] > 0 && vec[i] < min) {
                min = vec[i];
            }
        }
        return min;
    }

    public static double[][] inverse(final double[][] mat) {
        final int m = mat.length;
        final int n = mat[0].length;
        int r, c;
        int k = 1;
        final double[][] ak = new double[m][1];
        double[][] dk, ck, bk;
        double[][] R_plus;
        for (r = 0; r < m; r++) {
            ak[r][0] = mat[r][0];
        }
        if (!equals(ak, 0.0)) {
            R_plus = mul(transpose(ak), (1.0 / (dot(ak, ak))));
        } else {
            R_plus = new double[1][m];
        }
        while (k < n) {
            for (r = 0; r < m; r++) {
                ak[r][0] = mat[r][k];
            }
            dk = matrixProd(R_plus, ak);
            final double[][] T = new double[m][k];
            for (r = 0; r < m; r++) {
                for (c = 0; c < k; c++) {
                    T[r][c] = mat[r][c];
                }
            }
            ck = diff(ak, matrixProd(T, dk));
            if (!equals(ck, 0.0)) {
                bk = mul(transpose(ck), 1.0 / dot(ck, ck));
            } else {
                bk = matrixProd(mul(transpose(dk), 1.0 / (1.0 + dot(dk, dk))), R_plus);
            }
            final double[][] N = diff(R_plus, matrixProd(dk, bk));
            R_plus = new double[N.length + 1][N[0].length];
            for (r = 0; r < N.length; r++) {
                for (c = 0; c < N[0].length; c++) {
                    R_plus[r][c] = N[r][c];
                }
            }
            for (c = 0; c < N[0].length; c++) {
                R_plus[R_plus.length - 1][c] = bk[0][c];
            }
            k++;
        }
        return R_plus;
    }

    /**
     * Submatrix firstr = first preserved row etc.
     */
    public static double[][] submatrixRC(final double[][] mat, final int firstr, final int lastr, final int firstc, final int lastc) {
        int icnt = 0, jcnt = 0;
        final double[][] res = new double[(lastr - firstr + 1)][(lastc - firstc + 1)];
        for (int i = firstr; i <= lastr; i++) {
            for (int j = firstc; j <= lastc; j++) {
                res[icnt][jcnt++] = mat[i][j];
            }
            ++icnt;
            jcnt = 0;
        }
        return res;
    }

    /**
     * Submatrix
     */
    public static double[] submatrixR(final double[][] mat, final int onlyrow, final int firstc, final int lastc) {
        int jcnt = 0;
        final double[] res = new double[(lastc - firstc + 1)];
        for (int j = firstc; j <= lastc; j++) {
            res[jcnt++] = mat[onlyrow][j];
        }
        return res;
    }

    /**
     * Submatrix
     */
    public static double[] submatrixC(final double[][] mat, final int firstr, final int lastr, final int onlycol) {
        int jcnt = 0;
        final double[] res = new double[(lastr - firstr + 1)];
        for (int j = firstr; j <= lastr; j++) {
            res[jcnt++] = mat[j][onlycol];
        }
        return res;
    }

    public static void main(final String[] args) {
    }
}
