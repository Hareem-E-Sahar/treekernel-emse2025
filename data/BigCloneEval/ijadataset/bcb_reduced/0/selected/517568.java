package de.offis.faint.recognition.plugins.eigenfaces;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is used to build up a covariance matrix and
 * extract the eigenvalues and eigenvectors of it. Portions
 * of the code are taken from the Java Matrix Package (JAMA),
 * a cooperative product of The MathWorks and the National
 * Institute of Standards and Technology (NIST).
 * 
 * @author maltech
 *
 */
public class CovarianceMatrix {

    private EigenValueAndVector[] eigenValueAndVectors;

    private double[][] smallMatrix;

    private int dimension;

    private double[] eig, e;

    /**
	 * Constructor that builds up the matrix and calculates
	 * the Eigenvalues and Vectors internally.
	 * 
	 * @param vectors
	 */
    public CovarianceMatrix(short[][] vectors) {
        int vectorLength = vectors[0].length;
        dimension = vectors.length;
        smallMatrix = new double[dimension][dimension];
        for (int rowIndex = 0; rowIndex < smallMatrix.length; rowIndex++) {
            for (int colIndex = 0; colIndex < rowIndex + 1; colIndex++) {
                smallMatrix[rowIndex][colIndex] = 0;
                for (int i = 0; i < vectorLength; i++) {
                    smallMatrix[rowIndex][colIndex] += vectors[rowIndex][i] * vectors[colIndex][i];
                }
                smallMatrix[colIndex][rowIndex] = smallMatrix[rowIndex][colIndex];
            }
        }
        this.tridiagonalize();
        this.performQLalgorithm();
        ArrayList<EigenValueAndVector> tempList = new ArrayList<EigenValueAndVector>(dimension);
        for (int i = 0; i < dimension; i++) {
            double[] eigenVector = new double[vectorLength];
            double length = 0;
            for (int j = 0; j < vectorLength; j++) {
                double value = 0;
                for (int k = 0; k < vectors.length; k++) {
                    value += (vectors[k][j]) * (smallMatrix[k][i]);
                }
                eigenVector[j] = value;
                length += value * value;
            }
            length = Math.sqrt(length);
            for (int j = 0; j < eigenVector.length; j++) {
                eigenVector[j] /= length;
            }
            EigenValueAndVector evv = new EigenValueAndVector();
            evv.eigenVector = eigenVector;
            evv.eigenValue = eig[i];
            tempList.add(evv);
        }
        Collections.sort(tempList);
        eigenValueAndVectors = new EigenValueAndVector[dimension];
        tempList.toArray(eigenValueAndVectors);
    }

    /**
	 * Calculates elements of the "bigger" matrix - used for testing purpose only.
	 * 
	 * @param row
	 * @param col
	 * @param vectors
	 * @return
	 */
    @SuppressWarnings("unused")
    private double getBigMatrixElement(int row, int col, short[][] vectors) {
        double result = 0;
        int vectorLength = vectors.length;
        for (int i = 0; i < vectorLength; i++) {
            result += vectors[i][row] * vectors[i][col];
        }
        return result;
    }

    /**
	 * Print method used for debugging.
	 */
    @SuppressWarnings("unused")
    private void print() {
        String whitespace = "                            ";
        for (int row = 0; row < smallMatrix.length; row++) {
            for (int col = 0; col < smallMatrix.length; col++) {
                String number = "" + smallMatrix[row][col];
                number = whitespace.substring(number.length()) + number;
                System.out.print(number);
            }
            System.out.println();
            System.out.println();
        }
    }

    /**
	 * Returns the Eigenvalue specified by a given number.
	 * The Values are sorted biggest-first.
	 * 
	 * @param number
	 * @return
	 */
    public double getEigenValue(int number) {
        return this.eigenValueAndVectors[number].eigenValue;
    }

    /**
	 * Returns the Eigenvector specified by a given number.
	 * The Eigenvectors are sorted by their Eigenvalues.
	 * 
	 * @param number
	 * @return
	 */
    public double[] getEigenVector(int number) {
        return this.eigenValueAndVectors[number].eigenVector;
    }

    /**
	 *  Symmetric Householder reduction to tridiagonal form as
	 *  used in JAMA Package.
	 */
    private void tridiagonalize() {
        eig = new double[dimension];
        e = new double[dimension];
        for (int j = 0; j < dimension; j++) {
            eig[j] = smallMatrix[dimension - 1][j];
        }
        for (int i = dimension - 1; i > 0; i--) {
            double scale = 0.0;
            double h = 0.0;
            for (int k = 0; k < i; k++) {
                scale = scale + Math.abs(eig[k]);
            }
            if (scale == 0.0) {
                e[i] = eig[i - 1];
                for (int j = 0; j < i; j++) {
                    eig[j] = smallMatrix[i - 1][j];
                    smallMatrix[i][j] = 0.0;
                    smallMatrix[j][i] = 0.0;
                }
            } else {
                for (int k = 0; k < i; k++) {
                    eig[k] /= scale;
                    h += eig[k] * eig[k];
                }
                double f = eig[i - 1];
                double g = Math.sqrt(h);
                if (f > 0) {
                    g = -g;
                }
                e[i] = scale * g;
                h = h - f * g;
                eig[i - 1] = f - g;
                for (int j = 0; j < i; j++) {
                    e[j] = 0.0;
                }
                for (int j = 0; j < i; j++) {
                    f = eig[j];
                    smallMatrix[j][i] = f;
                    g = e[j] + smallMatrix[j][j] * f;
                    for (int k = j + 1; k <= i - 1; k++) {
                        g += smallMatrix[k][j] * eig[k];
                        e[k] += smallMatrix[k][j] * f;
                    }
                    e[j] = g;
                }
                f = 0.0;
                for (int j = 0; j < i; j++) {
                    e[j] /= h;
                    f += e[j] * eig[j];
                }
                double hh = f / (h + h);
                for (int j = 0; j < i; j++) {
                    e[j] -= hh * eig[j];
                }
                for (int j = 0; j < i; j++) {
                    f = eig[j];
                    g = e[j];
                    for (int k = j; k <= i - 1; k++) {
                        smallMatrix[k][j] -= (f * e[k] + g * eig[k]);
                    }
                    eig[j] = smallMatrix[i - 1][j];
                    smallMatrix[i][j] = 0.0;
                }
            }
            eig[i] = h;
        }
        for (int i = 0; i < dimension - 1; i++) {
            smallMatrix[dimension - 1][i] = smallMatrix[i][i];
            smallMatrix[i][i] = 1.0;
            double h = eig[i + 1];
            if (h != 0.0) {
                for (int k = 0; k <= i; k++) {
                    eig[k] = smallMatrix[k][i + 1] / h;
                }
                for (int j = 0; j <= i; j++) {
                    double g = 0.0;
                    for (int k = 0; k <= i; k++) {
                        g += smallMatrix[k][i + 1] * smallMatrix[k][j];
                    }
                    for (int k = 0; k <= i; k++) {
                        smallMatrix[k][j] -= g * eig[k];
                    }
                }
            }
            for (int k = 0; k <= i; k++) {
                smallMatrix[k][i + 1] = 0.0;
            }
        }
        for (int j = 0; j < dimension; j++) {
            eig[j] = smallMatrix[dimension - 1][j];
            smallMatrix[dimension - 1][j] = 0.0;
        }
        smallMatrix[dimension - 1][dimension - 1] = 1.0;
        e[0] = 0.0;
    }

    /**
	 * Symmetric tridiagonal QL algorithm as used in JAMA Package.
	 */
    private void performQLalgorithm() {
        for (int i = 1; i < dimension; i++) {
            e[i - 1] = e[i];
        }
        e[dimension - 1] = 0.0;
        double f = 0.0;
        double tst1 = 0.0;
        double eps = Math.pow(2.0, -52.0);
        for (int l = 0; l < dimension; l++) {
            tst1 = Math.max(tst1, Math.abs(eig[l]) + Math.abs(e[l]));
            int m = l;
            while (m < dimension) {
                if (Math.abs(e[m]) <= eps * tst1) {
                    break;
                }
                m++;
            }
            if (m > l) {
                int iter = 0;
                do {
                    iter = iter + 1;
                    double g = eig[l];
                    double p = (eig[l + 1] - g) / (2.0 * e[l]);
                    double r = hypot(p, 1.0);
                    if (p < 0) {
                        r = -r;
                    }
                    eig[l] = e[l] / (p + r);
                    eig[l + 1] = e[l] * (p + r);
                    double dl1 = eig[l + 1];
                    double h = g - eig[l];
                    for (int i = l + 2; i < dimension; i++) {
                        eig[i] -= h;
                    }
                    f = f + h;
                    p = eig[m];
                    double c = 1.0;
                    double c2 = c;
                    double c3 = c;
                    double el1 = e[l + 1];
                    double s = 0.0;
                    double s2 = 0.0;
                    for (int i = m - 1; i >= l; i--) {
                        c3 = c2;
                        c2 = c;
                        s2 = s;
                        g = c * e[i];
                        h = c * p;
                        r = hypot(p, e[i]);
                        e[i + 1] = s * r;
                        s = e[i] / r;
                        c = p / r;
                        p = c * eig[i] - s * g;
                        eig[i + 1] = h + s * (c * g + s * eig[i]);
                        for (int k = 0; k < dimension; k++) {
                            h = smallMatrix[k][i + 1];
                            smallMatrix[k][i + 1] = s * smallMatrix[k][i] + c * h;
                            smallMatrix[k][i] = c * smallMatrix[k][i] - s * h;
                        }
                    }
                    p = -s * s2 * c3 * el1 * e[l] / dl1;
                    e[l] = s * p;
                    eig[l] = c * p;
                } while (Math.abs(e[l]) > eps * tst1);
            }
            eig[l] = eig[l] + f;
            e[l] = 0.0;
        }
        for (int i = 0; i < dimension - 1; i++) {
            int k = i;
            double p = eig[i];
            for (int j = i + 1; j < dimension; j++) {
                if (eig[j] < p) {
                    k = j;
                    p = eig[j];
                }
            }
            if (k != i) {
                eig[k] = eig[i];
                eig[i] = p;
                for (int j = 0; j < dimension; j++) {
                    p = smallMatrix[j][i];
                    smallMatrix[j][i] = smallMatrix[j][k];
                    smallMatrix[j][k] = p;
                }
            }
        }
    }

    /**
	 * Sqrt(a^2 + b^2) without under/overflow as used in JAMA Package.
	 */
    private static double hypot(double a, double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b / a;
            r = Math.abs(a) * Math.sqrt(1 + r * r);
        } else if (b != 0) {
            r = a / b;
            r = Math.abs(b) * Math.sqrt(1 + r * r);
        } else {
            r = 0.0;
        }
        return r;
    }

    /**
	 * Sortable structure holding eigenvalues and their
	 * corresponding eigenvectors.
	 */
    private static class EigenValueAndVector implements Comparable {

        private double eigenValue;

        private double[] eigenVector;

        public int compareTo(Object o) {
            EigenValueAndVector that = (EigenValueAndVector) o;
            if (this.eigenValue < that.eigenValue) return 1;
            if (this.eigenValue > that.eigenValue) return -1;
            return 0;
        }
    }
}
