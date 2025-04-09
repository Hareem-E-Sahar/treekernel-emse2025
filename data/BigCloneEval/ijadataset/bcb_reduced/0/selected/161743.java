package se.uu.it.cats.brick.filter;

import lejos.util.Matrix;
import se.uu.it.cats.brick.Logger;

/*************************************************************************
 *  Compilation:  javac Cholesky.java
 *  Execution:    java Cholesky
 * 
 *  Compute Cholesky decomposition of symmetric positive definite
 *  matrix A = LL^T.
 *
 *  % java Cholesky
 *  2.00000  0.00000  0.00000 
 *  0.50000  2.17945  0.00000 
 *  0.50000  1.26179  3.62738 
 *
 *************************************************************************/
public class Cholesky {

    private static final double EPSILON = 1e-10;

    private static final int DEBUG = 0;

    public static boolean isSymmetric(double[][] A) throws Exception {
        debug("A = ");
        debug(Matlab.MatrixToString(new Matrix(A)));
        int N = A.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
                if (A[i][j] == Double.NaN || A[j][i] == Double.NaN) {
                    Logger.println("Error in Cholesky: Matrix contains NaN");
                    debug("A = ");
                    debug(Matlab.MatrixToString(new Matrix(A)));
                    throw new Exception("Matrix contains NaN");
                }
                if (A[i][j] - A[j][i] > EPSILON) return false; else A[i][j] = A[j][i];
            }
        }
        return true;
    }

    public static boolean isSquare(double[][] A) {
        int N = A.length;
        for (int i = 0; i < N; i++) {
            if (A[i].length != N) return false;
        }
        return true;
    }

    public static double[][] cholesky(double[][] A) throws Exception {
        debug("Entering Cholesky");
        if (!isSquare(A)) {
            Logger.println("Error in Cholesky: Matrix is not square");
            debug("A = ");
            debug(Matlab.MatrixToString(new Matrix(A)));
            throw new Exception("Matrix is not square");
        }
        if (!isSymmetric(A)) {
            Logger.println("Error in Cholesky: Matrix is not symmetric");
            debug("A = ");
            debug(Matlab.MatrixToString(new Matrix(A)));
            throw new Exception("Matrix is not symmetric");
        }
        int N = A.length;
        double[][] L = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                for (int k = 0; k < j; k++) {
                    sum += L[i][k] * L[j][k];
                }
                if (i == j) L[i][i] = Math.sqrt(A[i][i] - sum); else L[i][j] = 1.0 / L[j][j] * (A[i][j] - sum);
            }
            if (L[i][i] <= 0) {
                Logger.println("Error in Cholesky: Matrix not positive definite: L[" + i + "][" + i + "] = " + L[i][i] + " <= 0");
                debug("L = ");
                debug(Matlab.MatrixToString(new Matrix(L)));
                debug("A = ");
                debug(Matlab.MatrixToString(new Matrix(A)));
                throw new Exception("Matrix not positive definite");
            }
        }
        checkForNaN(L);
        debug("Leaving Cholesky");
        return L;
    }

    private static void checkForNaN(double[][] A) throws Exception {
        int N = A.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
                if (Double.isNaN(A[i][j])) {
                    Logger.println("Error in Cholesky: Matrix contains NaN");
                    debug("A = ");
                    debug(Matlab.MatrixToString(new Matrix(A)));
                    throw new Exception("Matrix contains NaN");
                }
            }
        }
    }

    private static void debug(String s) {
        if (DEBUG == 0) return;
        if (DEBUG == 1) {
            System.out.println(s);
        } else Logger.println(s);
    }
}
