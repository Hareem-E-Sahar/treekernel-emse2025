package util.misc;

import org.apache.log4j.Logger;

public class Matrix {

    private static Logger logger = IseeLogger.getLogger(Matrix.class);

    public static double[][] multiply(double[][] A, double[][] B) {
        int _dimension = A.length;
        double[][] C = new double[_dimension][_dimension];
        double summe;
        if (logger.isDebugEnabled()) {
            debug("Matrix A:", A);
            debug("Matrix B:", B);
        }
        for (int i = 0; i < _dimension; i++) {
            for (int k = 0; k < _dimension; k++) {
                C[i][k] = 0;
                for (int j = 0; j < _dimension; j++) {
                    C[i][k] += A[i][j] * B[j][k];
                }
            }
        }
        if (logger.isDebugEnabled()) {
            debug("C = A * B:", C);
        }
        return C;
    }

    public static double[][] transpose(double[][] A) {
        if (logger.isDebugEnabled()) {
            debug("Matrix A:", A);
        }
        int _dimension = A.length;
        double[][] B = new double[_dimension][_dimension];
        for (int i = 0; i < _dimension; i++) {
            for (int k = 0; k < _dimension; k++) {
                B[k][i] = A[i][k];
            }
        }
        if (logger.isDebugEnabled()) {
            debug("B = transpose(A):", B);
        }
        return B;
    }

    public static double[][] sum(double[][] A, double[][] B) {
        if (logger.isDebugEnabled()) {
            debug("Matrix A:", A);
            debug("Matrix B:", B);
        }
        int _dimension = A.length;
        double[][] C = new double[_dimension][_dimension];
        for (int i = 0; i < _dimension; i++) {
            for (int k = 0; k < _dimension; k++) {
                C[i][k] = B[i][k] + A[i][k];
            }
        }
        if (logger.isDebugEnabled()) {
            debug("C = A + B:", C);
        }
        return C;
    }

    public static double[][] copy(double[][] A) {
        int _dimension = A.length;
        double[][] B = new double[_dimension][_dimension];
        for (int i = 0; i < _dimension; i++) {
            for (int k = 0; k < _dimension; k++) {
                B[i][k] = A[i][k];
            }
        }
        return B;
    }

    public static void copy(double[][] A, double[][] B) {
        int _dimension = A.length;
        for (int i = 0; i < _dimension; i++) {
            for (int k = 0; k < _dimension; k++) {
                B[i][k] = A[i][k];
            }
        }
    }

    public static double[][] QR(double[][] A, double[][] Q) {
        int _dimension = A.length;
        double[] x = new double[_dimension];
        double[][] v = new double[_dimension][_dimension];
        double[][] vt = new double[_dimension][_dimension];
        double[][] u = new double[_dimension][_dimension];
        double[][] c = new double[_dimension][_dimension];
        double[][] Ps = new double[_dimension][_dimension];
        double[][] Psc = new double[_dimension][_dimension];
        double[][] P = new double[_dimension][_dimension];
        double[][] E = new double[_dimension][_dimension];
        double[][] R = new double[_dimension][_dimension];
        double[][] Qp = new double[_dimension][_dimension];
        for (int i = 0; i < _dimension; i++) {
            for (int j = 0; j < _dimension; j++) {
                Qp[i][j] = 0.0;
            }
            Qp[i][i] = 1.0;
        }
        for (int k = 0; k < _dimension; k++) {
            double bx = 0.0;
            for (int j = 0; j < _dimension - k; j++) {
                x[j] = A[k + j][k];
                bx = bx + x[j] * x[j];
                u[j][0] = x[j];
            }
            bx = Math.sqrt(bx);
            u[0][0] = x[0] - bx;
            double h = (bx * bx - x[0] * x[0] + u[0][0] * u[0][0]) / 2.0;
            if (h == 0.0) h = 1.0;
            for (int j = 0; j < _dimension - k; j++) {
                v[j][0] = -1.0 / h * u[j][0];
            }
            vt = Matrix.transpose(v);
            Ps = Matrix.multiply(u, vt);
            for (int i = 0; i < _dimension - k; i++) {
                for (int j = 0; j < _dimension - k; j++) {
                    E[i][j] = 0.0;
                }
                E[i][i] = 1.0;
            }
            Psc = Matrix.sum(E, Ps);
            for (int i = 0; i < _dimension; i++) {
                for (int j = 0; j < _dimension; j++) {
                    P[i][j] = 0.0;
                }
                if (i < k) P[i][i] = 1.0;
            }
            for (int i = k; i < _dimension; i++) {
                for (int j = k; j < _dimension; j++) {
                    P[i][j] = Psc[i - k][j - k];
                }
            }
            c = Matrix.multiply(P, A);
            A = Matrix.copy(c);
            c = Matrix.multiply(Qp, P);
            Qp = Matrix.copy(c);
        }
        Matrix.copy(A, R);
        Matrix.copy(Qp, Q);
        return R;
    }

    public static void initialiseDiagonalMatrix(double[][] A) {
        int _dimension = A.length;
        for (int i = 0; i < _dimension; i++) {
            for (int j = 0; j < _dimension; j++) {
                A[i][j] = 0.0;
            }
        }
        for (int i = 0; i < _dimension; i++) {
            A[i][i] = 1.0;
        }
    }

    public static void initialiseZeroMatrix(double[][] A) {
        int _dimension = A.length;
        for (int i = 0; i < _dimension; i++) {
            for (int j = 0; j < _dimension; j++) {
                A[i][j] = 0.0;
            }
        }
    }

    public static void debug(String string, double[][] A) {
        int _dimension = A.length;
        logger.debug(string);
        for (int i = 0; i < _dimension; i++) {
            String s = "";
            for (int j = 0; j < _dimension; j++) {
                s = s.concat("  " + A[i][j]);
            }
            logger.debug(s);
        }
    }
}
