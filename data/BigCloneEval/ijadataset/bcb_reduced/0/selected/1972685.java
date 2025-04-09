package org.apache.commons.math3.linear;

import java.util.Arrays;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.FastMath;

/**
 * Calculates the QR-decomposition of a matrix.
 * <p>The QR-decomposition of a matrix A consists of two matrices Q and R
 * that satisfy: A = QR, Q is orthogonal (Q<sup>T</sup>Q = I), and R is
 * upper triangular. If A is m&times;n, Q is m&times;m and R m&times;n.</p>
 * <p>This class compute the decomposition using Householder reflectors.</p>
 * <p>For efficiency purposes, the decomposition in packed form is transposed.
 * This allows inner loop to iterate inside rows, which is much more cache-efficient
 * in Java.</p>
 * <p>This class is based on the class with similar name from the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA</a> library, with the
 * following changes:</p>
 * <ul>
 *   <li>a {@link #getQT() getQT} method has been added,</li>
 *   <li>the {@code solve} and {@code isFullRank} methods have been replaced
 *   by a {@link #getSolver() getSolver} method and the equivalent methods
 *   provided by the returned {@link DecompositionSolver}.</li>
 * </ul>
 *
 * @see <a href="http://mathworld.wolfram.com/QRDecomposition.html">MathWorld</a>
 * @see <a href="http://en.wikipedia.org/wiki/QR_decomposition">Wikipedia</a>
 *
 * @version $Id: QRDecomposition.java 1244107 2012-02-14 16:17:55Z erans $
 * @since 1.2 (changed to concrete class in 3.0)
 */
public class QRDecomposition {

    /**
     * A packed TRANSPOSED representation of the QR decomposition.
     * <p>The elements BELOW the diagonal are the elements of the UPPER triangular
     * matrix R, and the rows ABOVE the diagonal are the Householder reflector vectors
     * from which an explicit form of Q can be recomputed if desired.</p>
     */
    private double[][] qrt;

    /** The diagonal elements of R. */
    private double[] rDiag;

    /** Cached value of Q. */
    private RealMatrix cachedQ;

    /** Cached value of QT. */
    private RealMatrix cachedQT;

    /** Cached value of R. */
    private RealMatrix cachedR;

    /** Cached value of H. */
    private RealMatrix cachedH;

    /** Singularity threshold. */
    private final double threshold;

    /**
     * Calculates the QR-decomposition of the given matrix.
     * The singularity threshold defaults to zero.
     *
     * @param matrix The matrix to decompose.
     *
     * @see #QRDecomposition(RealMatrix,double)
     */
    public QRDecomposition(RealMatrix matrix) {
        this(matrix, 0d);
    }

    /**
     * Calculates the QR-decomposition of the given matrix.
     *
     * @param matrix The matrix to decompose.
     * @param threshold Singularity threshold.
     */
    public QRDecomposition(RealMatrix matrix, double threshold) {
        this.threshold = threshold;
        final int m = matrix.getRowDimension();
        final int n = matrix.getColumnDimension();
        qrt = matrix.transpose().getData();
        rDiag = new double[FastMath.min(m, n)];
        cachedQ = null;
        cachedQT = null;
        cachedR = null;
        cachedH = null;
        for (int minor = 0; minor < FastMath.min(m, n); minor++) {
            final double[] qrtMinor = qrt[minor];
            double xNormSqr = 0;
            for (int row = minor; row < m; row++) {
                final double c = qrtMinor[row];
                xNormSqr += c * c;
            }
            final double a = (qrtMinor[minor] > 0) ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
            rDiag[minor] = a;
            if (a != 0.0) {
                qrtMinor[minor] -= a;
                for (int col = minor + 1; col < n; col++) {
                    final double[] qrtCol = qrt[col];
                    double alpha = 0;
                    for (int row = minor; row < m; row++) {
                        alpha -= qrtCol[row] * qrtMinor[row];
                    }
                    alpha /= a * qrtMinor[minor];
                    for (int row = minor; row < m; row++) {
                        qrtCol[row] -= alpha * qrtMinor[row];
                    }
                }
            }
        }
    }

    /**
     * Returns the matrix R of the decomposition.
     * <p>R is an upper-triangular matrix</p>
     * @return the R matrix
     */
    public RealMatrix getR() {
        if (cachedR == null) {
            final int n = qrt.length;
            final int m = qrt[0].length;
            double[][] ra = new double[m][n];
            for (int row = FastMath.min(m, n) - 1; row >= 0; row--) {
                ra[row][row] = rDiag[row];
                for (int col = row + 1; col < n; col++) {
                    ra[row][col] = qrt[col][row];
                }
            }
            cachedR = MatrixUtils.createRealMatrix(ra);
        }
        return cachedR;
    }

    /**
     * Returns the matrix Q of the decomposition.
     * <p>Q is an orthogonal matrix</p>
     * @return the Q matrix
     */
    public RealMatrix getQ() {
        if (cachedQ == null) {
            cachedQ = getQT().transpose();
        }
        return cachedQ;
    }

    /**
     * Returns the transpose of the matrix Q of the decomposition.
     * <p>Q is an orthogonal matrix</p>
     * @return the Q matrix
     */
    public RealMatrix getQT() {
        if (cachedQT == null) {
            final int n = qrt.length;
            final int m = qrt[0].length;
            double[][] qta = new double[m][m];
            for (int minor = m - 1; minor >= FastMath.min(m, n); minor--) {
                qta[minor][minor] = 1.0d;
            }
            for (int minor = FastMath.min(m, n) - 1; minor >= 0; minor--) {
                final double[] qrtMinor = qrt[minor];
                qta[minor][minor] = 1.0d;
                if (qrtMinor[minor] != 0.0) {
                    for (int col = minor; col < m; col++) {
                        double alpha = 0;
                        for (int row = minor; row < m; row++) {
                            alpha -= qta[col][row] * qrtMinor[row];
                        }
                        alpha /= rDiag[minor] * qrtMinor[minor];
                        for (int row = minor; row < m; row++) {
                            qta[col][row] += -alpha * qrtMinor[row];
                        }
                    }
                }
            }
            cachedQT = MatrixUtils.createRealMatrix(qta);
        }
        return cachedQT;
    }

    /**
     * Returns the Householder reflector vectors.
     * <p>H is a lower trapezoidal matrix whose columns represent
     * each successive Householder reflector vector. This matrix is used
     * to compute Q.</p>
     * @return a matrix containing the Householder reflector vectors
     */
    public RealMatrix getH() {
        if (cachedH == null) {
            final int n = qrt.length;
            final int m = qrt[0].length;
            double[][] ha = new double[m][n];
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < FastMath.min(i + 1, n); ++j) {
                    ha[i][j] = qrt[j][i] / -rDiag[j];
                }
            }
            cachedH = MatrixUtils.createRealMatrix(ha);
        }
        return cachedH;
    }

    /**
     * Get a solver for finding the A &times; X = B solution in least square sense.
     * @return a solver
     */
    public DecompositionSolver getSolver() {
        return new Solver(qrt, rDiag, threshold);
    }

    /** Specialized solver. */
    private static class Solver implements DecompositionSolver {

        /**
         * A packed TRANSPOSED representation of the QR decomposition.
         * <p>The elements BELOW the diagonal are the elements of the UPPER triangular
         * matrix R, and the rows ABOVE the diagonal are the Householder reflector vectors
         * from which an explicit form of Q can be recomputed if desired.</p>
         */
        private final double[][] qrt;

        /** The diagonal elements of R. */
        private final double[] rDiag;

        /** Singularity threshold. */
        private final double threshold;

        /**
         * Build a solver from decomposed matrix.
         *
         * @param qrt Packed TRANSPOSED representation of the QR decomposition.
         * @param rDiag Diagonal elements of R.
         * @param threshold Singularity threshold.
         */
        private Solver(final double[][] qrt, final double[] rDiag, final double threshold) {
            this.qrt = qrt;
            this.rDiag = rDiag;
            this.threshold = threshold;
        }

        /** {@inheritDoc} */
        public boolean isNonSingular() {
            for (double diag : rDiag) {
                if (FastMath.abs(diag) <= threshold) {
                    return false;
                }
            }
            return true;
        }

        /** {@inheritDoc} */
        public RealVector solve(RealVector b) {
            final int n = qrt.length;
            final int m = qrt[0].length;
            if (b.getDimension() != m) {
                throw new DimensionMismatchException(b.getDimension(), m);
            }
            if (!isNonSingular()) {
                throw new SingularMatrixException();
            }
            final double[] x = new double[n];
            final double[] y = b.toArray();
            for (int minor = 0; minor < FastMath.min(m, n); minor++) {
                final double[] qrtMinor = qrt[minor];
                double dotProduct = 0;
                for (int row = minor; row < m; row++) {
                    dotProduct += y[row] * qrtMinor[row];
                }
                dotProduct /= rDiag[minor] * qrtMinor[minor];
                for (int row = minor; row < m; row++) {
                    y[row] += dotProduct * qrtMinor[row];
                }
            }
            for (int row = rDiag.length - 1; row >= 0; --row) {
                y[row] /= rDiag[row];
                final double yRow = y[row];
                final double[] qrtRow = qrt[row];
                x[row] = yRow;
                for (int i = 0; i < row; i++) {
                    y[i] -= yRow * qrtRow[i];
                }
            }
            return new ArrayRealVector(x, false);
        }

        /** {@inheritDoc} */
        public RealMatrix solve(RealMatrix b) {
            final int n = qrt.length;
            final int m = qrt[0].length;
            if (b.getRowDimension() != m) {
                throw new DimensionMismatchException(b.getRowDimension(), m);
            }
            if (!isNonSingular()) {
                throw new SingularMatrixException();
            }
            final int columns = b.getColumnDimension();
            final int blockSize = BlockRealMatrix.BLOCK_SIZE;
            final int cBlocks = (columns + blockSize - 1) / blockSize;
            final double[][] xBlocks = BlockRealMatrix.createBlocksLayout(n, columns);
            final double[][] y = new double[b.getRowDimension()][blockSize];
            final double[] alpha = new double[blockSize];
            for (int kBlock = 0; kBlock < cBlocks; ++kBlock) {
                final int kStart = kBlock * blockSize;
                final int kEnd = FastMath.min(kStart + blockSize, columns);
                final int kWidth = kEnd - kStart;
                b.copySubMatrix(0, m - 1, kStart, kEnd - 1, y);
                for (int minor = 0; minor < FastMath.min(m, n); minor++) {
                    final double[] qrtMinor = qrt[minor];
                    final double factor = 1.0 / (rDiag[minor] * qrtMinor[minor]);
                    Arrays.fill(alpha, 0, kWidth, 0.0);
                    for (int row = minor; row < m; ++row) {
                        final double d = qrtMinor[row];
                        final double[] yRow = y[row];
                        for (int k = 0; k < kWidth; ++k) {
                            alpha[k] += d * yRow[k];
                        }
                    }
                    for (int k = 0; k < kWidth; ++k) {
                        alpha[k] *= factor;
                    }
                    for (int row = minor; row < m; ++row) {
                        final double d = qrtMinor[row];
                        final double[] yRow = y[row];
                        for (int k = 0; k < kWidth; ++k) {
                            yRow[k] += alpha[k] * d;
                        }
                    }
                }
                for (int j = rDiag.length - 1; j >= 0; --j) {
                    final int jBlock = j / blockSize;
                    final int jStart = jBlock * blockSize;
                    final double factor = 1.0 / rDiag[j];
                    final double[] yJ = y[j];
                    final double[] xBlock = xBlocks[jBlock * cBlocks + kBlock];
                    int index = (j - jStart) * kWidth;
                    for (int k = 0; k < kWidth; ++k) {
                        yJ[k] *= factor;
                        xBlock[index++] = yJ[k];
                    }
                    final double[] qrtJ = qrt[j];
                    for (int i = 0; i < j; ++i) {
                        final double rIJ = qrtJ[i];
                        final double[] yI = y[i];
                        for (int k = 0; k < kWidth; ++k) {
                            yI[k] -= yJ[k] * rIJ;
                        }
                    }
                }
            }
            return new BlockRealMatrix(n, columns, xBlocks, false);
        }

        /** {@inheritDoc} */
        public RealMatrix getInverse() {
            return solve(MatrixUtils.createRealIdentityMatrix(rDiag.length));
        }
    }
}
