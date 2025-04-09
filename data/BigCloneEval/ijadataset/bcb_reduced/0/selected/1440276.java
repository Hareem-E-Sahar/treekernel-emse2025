package ProPesca.main;

import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alberto Casagrande
 */
public class Statistic {

    public static double[][] PCA(double[][] X) {
        Algebra a = new Algebra();
        DoubleMatrix2D D_X, P, cov;
        D_X = a.transpose(new DenseDoubleMatrix2D(X));
        double[][] Z = normalize_wrt_mean_stddev(D_X.toArray());
        cov = new DenseDoubleMatrix2D(covariance(Z));
        EigenvalueDecomposition e = new EigenvalueDecomposition(cov);
        DenseDoubleMatrix2D e_vector = sort_eigenvectors_by_eigenvalues(e);
        P = a.mult(a.transpose(e_vector), new DenseDoubleMatrix2D(Z));
        double[][] new_Z = normalize_wrt_mean_stddev(P.toArray());
        D_X = new DenseDoubleMatrix2D(covariance(Z, new_Z));
        return a.transpose(D_X).toArray();
    }

    private static DenseDoubleMatrix2D sort_eigenvectors_by_eigenvalues(EigenvalueDecomposition e) {
        double[] evalues = (e.getRealEigenvalues()).toArray();
        ValuePos[] vp = new ValuePos[evalues.length];
        for (int i = 0; i < evalues.length; i++) {
            vp[i] = new ValuePos(evalues[i], i);
        }
        Comparator<ValuePos> byValue = new ValuePosComp();
        Arrays.sort(vp, byValue);
        double[][] evector = (e.getV()).toArray();
        double[][] output = new double[evector.length][evector[0].length];
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[i].length; j++) {
                output[i][j] = evector[i][vp[j].pos];
            }
        }
        return new DenseDoubleMatrix2D(output);
    }

    public static double[][] Kendall(double[][] X) {
        double[][] output = new double[X.length][X.length];
        try {
            for (int i = 0; i < X.length; i++) {
                output[i][i] = 1.0;
                for (int j = 0; j < i; j++) {
                    output[i][j] = GeneUtils.kendalltau(X[i], X[j]);
                    output[j][i] = output[i][j];
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    public static double[][] Spearman(double[][] X) {
        double[][] output = new double[X.length][X.length];
        try {
            for (int i = 0; i < X.length; i++) {
                output[i][i] = 1.0;
                for (int j = 0; j < i; j++) {
                    output[i][j] = GeneUtils.spearmanrho(X[i], X[j]);
                    output[j][i] = output[i][j];
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    public static double[][] Pearson(double[][] X) {
        double[][] output = new double[X.length][X.length];
        try {
            for (int i = 0; i < X.length; i++) {
                output[i][i] = 1.0;
                for (int j = 0; j < i; j++) {
                    output[i][j] = GeneUtils.pearson(X[i], X[j]);
                    output[j][i] = output[i][j];
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    public static void print(double[] X) {
        System.out.println(X.length);
        System.out.print(X[0]);
        for (int i = 1; i < X.length; i++) {
            System.out.print(" " + X[i]);
        }
        System.out.println();
    }

    public static void print(double[][] X) {
        System.out.println(X.length + " " + X[0].length);
        for (int i = 0; i < X.length; i++) {
            System.out.print(X[i][0]);
            for (int j = 1; j < X[i].length; j++) {
                System.out.print(" " + X[i][j]);
            }
            System.out.println();
        }
    }

    private static double[][] covariance(double[][] X) {
        double[][] Y = new double[X.length][X.length];
        for (int i = 0; i < Y.length; i++) {
            for (int j = 0; j < Y[0].length; j++) {
                double sum = 0;
                for (int k = 0; k < X[i].length; k++) {
                    sum = sum + (X[i][k] * X[j][k]);
                }
                Y[i][j] = sum / (X[i].length - 1);
            }
        }
        return Y;
    }

    private static double[][] covariance(double[][] X, double[][] Y) {
        double[][] Z = new double[X.length][Y.length];
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < Y.length; j++) {
                double sum = 0;
                for (int k = 0; k < X[i].length; k++) {
                    sum = sum + (X[i][k] * Y[j][k]);
                }
                Z[i][j] = sum / (X[i].length - 1);
            }
        }
        return Z;
    }

    private static boolean equivalent(double[][] X, double[][] Y) {
        if (X.length != Y.length) {
            return false;
        }
        for (int i = 0; i < X.length; i++) {
            if (X[i].length != Y[i].length) {
                return false;
            }
            for (int j = 0; j < X[i].length; j++) {
                if (X[i][j] != Y[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double[][] transpose(double[][] M) {
        double[][] O = new double[M[0].length][M.length];
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < M[0].length; j++) {
                O[j][i] = M[i][j];
            }
        }
        return O;
    }

    private static double[][] normalize_wrt_mean_stddev(double[][] X) {
        double[] meanX = mean(X);
        double[] stdevX = stddeviation(X, meanX);
        return normalize_wrt_mean_stddev(X, meanX, stdevX);
    }

    private static double[][] normalize_wrt_mean_stddev(double[][] X, double[] meanX, double[] stdevX) {
        double[][] Y = new double[X.length][X[0].length];
        for (int y = 0; y < Y.length; y++) {
            for (int x = 0; x < Y[y].length; x++) {
                Y[y][x] = (X[y][x] - meanX[y]) / stdevX[y];
            }
        }
        return Y;
    }

    private static double[] mean(double[][] X) {
        double[] Y = new double[X.length];
        for (int y = 0; y < X.length; y++) {
            double sum = 0;
            for (int x = 0; x < X[y].length; x++) {
                sum = sum + X[y][x];
            }
            Y[y] = sum / X[y].length;
        }
        return Y;
    }

    private static double[] stddeviation(double[][] X, double[] meanX) {
        double[] Y = new double[X.length];
        for (int y = 0; y < X.length; y++) {
            double sum = 0;
            double mean = meanX[y];
            for (int x = 0; x < X[y].length; x++) {
                sum = sum + ((mean - X[y][x]) * (mean - X[y][x]));
            }
            Y[y] = java.lang.Math.sqrt(sum / (X[y].length - 1));
        }
        return Y;
    }
}
