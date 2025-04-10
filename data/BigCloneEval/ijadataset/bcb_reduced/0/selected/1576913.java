package JSci.maths;

import JSci.maths.algebras.CStarAlgebra;

/**
* The ComplexSquareMatrix class provides an object for encapsulating square matrices containing complex numbers.
* @version 2.2
* @author Mark Hale
*/
public class ComplexSquareMatrix extends ComplexMatrix implements CStarAlgebra.Member {

    protected transient ComplexSquareMatrix LU[];

    protected transient int LUpivot[];

    /**
        * Constructs a matrix.
        */
    protected ComplexSquareMatrix(final int size, final int storeID) {
        super(size, size, storeID);
    }

    /**
        * Constructs an empty matrix.
        * @param size the number of rows/columns
        */
    public ComplexSquareMatrix(final int size) {
        super(size, size);
    }

    /**
        * Constructs a matrix by wrapping two arrays.
        * @param arrayRe an array of real values
        * @param arrayIm an array of imaginary values
        * @exception MatrixDimensionException If the array is not square.
        */
    public ComplexSquareMatrix(final double arrayRe[][], final double arrayIm[][]) {
        super(arrayRe, arrayIm);
        if (arrayRe.length != arrayRe[0].length && arrayIm.length != arrayIm[0].length) {
            matrixRe = null;
            matrixIm = null;
            throw new MatrixDimensionException("The arrays are not square.");
        }
    }

    /**
        * Constructs a matrix from an array.
        * @param array an assigned value
        * @exception MatrixDimensionException If the array is not square.
        */
    public ComplexSquareMatrix(final Complex array[][]) {
        super(array);
        if (array.length != array[0].length) {
            matrixRe = null;
            matrixIm = null;
            throw new MatrixDimensionException("The array is not square.");
        }
    }

    /**
        * Constructs a matrix from an array of vectors.
        * The vectors form columns in the matrix.
        * @param array an assigned value.
        * @exception MatrixDimensionException If the array is not square.
        */
    public ComplexSquareMatrix(final ComplexVector array[]) {
        super(array);
        if (array.length != array[0].dimension()) {
            matrixRe = null;
            matrixIm = null;
            throw new MatrixDimensionException("The array does not form a square matrix.");
        }
    }

    /**
        * Returns true if this matrix is hermitian.
        */
    public boolean isHermitian() {
        return this.equals(this.hermitianAdjoint());
    }

    /**
        * Returns true if this matrix is unitary.
        */
    public boolean isUnitary() {
        return this.multiply(this.hermitianAdjoint()).equals(ComplexDiagonalMatrix.identity(numRows));
    }

    /**
        * Returns the determinant.
        */
    public Complex det() {
        if (numRows == 2) {
            return new Complex(matrixRe[0][0] * matrixRe[1][1] - matrixIm[0][0] * matrixIm[1][1] - matrixRe[0][1] * matrixRe[1][0] + matrixIm[0][1] * matrixIm[1][0], matrixRe[0][0] * matrixIm[1][1] + matrixIm[0][0] * matrixRe[1][1] - matrixRe[0][1] * matrixIm[1][0] - matrixIm[0][1] * matrixRe[1][0]);
        } else {
            final ComplexSquareMatrix lu[] = this.luDecompose(null);
            double tmp;
            double detRe = lu[1].matrixRe[0][0];
            double detIm = lu[1].matrixIm[0][0];
            for (int i = 1; i < numRows; i++) {
                tmp = detRe * lu[1].matrixRe[i][i] - detIm * lu[1].matrixIm[i][i];
                detIm = detRe * lu[1].matrixIm[i][i] + detIm * lu[1].matrixRe[i][i];
                detRe = tmp;
            }
            return new Complex(detRe * LUpivot[numRows], detIm * LUpivot[numRows]);
        }
    }

    /**
        * Returns the trace.
        */
    public Complex trace() {
        double trRe = matrixRe[0][0];
        double trIm = matrixIm[0][0];
        for (int i = 1; i < numRows; i++) {
            trRe += matrixRe[i][i];
            trIm += matrixIm[i][i];
        }
        return new Complex(trRe, trIm);
    }

    /**
        * Returns the C<sup>*</sup> norm.
        */
    public double norm() {
        try {
            return operatorNorm();
        } catch (MaximumIterationsExceededException e) {
            return 0.0;
        }
    }

    /**
        * Returns the operator norm.
        * @exception MaximumIterationsExceededException If it takes more than 50 iterations to determine an eigenvalue.
        */
    public double operatorNorm() throws MaximumIterationsExceededException {
        return Math.sqrt(ArrayMath.max(LinearMath.eigenvalueSolveHermitian((ComplexSquareMatrix) (this.hermitianAdjoint().multiply(this)))));
    }

    /**
        * Returns the addition of this matrix and another.
        * @param m a complex matrix
        * @exception MatrixDimensionException If the matrices are different sizes.
        */
    public ComplexMatrix add(final ComplexMatrix m) {
        switch(m.storageFormat) {
            case ARRAY_2D:
                return rawAdd(m);
            default:
                if (numRows == m.rows() && numCols == m.columns()) {
                    final double arrayRe[][] = new double[numRows][numCols];
                    final double arrayIm[][] = new double[numRows][numCols];
                    for (int j, i = 0; i < numRows; i++) {
                        arrayRe[i][0] = matrixRe[i][0] + m.getElement(i, 0).real();
                        arrayIm[i][0] = matrixIm[i][0] + m.getElement(i, 0).imag();
                        for (j = 1; j < numCols; j++) {
                            arrayRe[i][j] = matrixRe[i][j] + m.getElement(i, j).real();
                            arrayIm[i][j] = matrixIm[i][j] + m.getElement(i, j).imag();
                        }
                    }
                    return new ComplexSquareMatrix(arrayRe, arrayIm);
                } else throw new MatrixDimensionException("Matrices are different sizes.");
        }
    }

    private ComplexSquareMatrix rawAdd(final ComplexMatrix m) {
        if (numRows == m.numRows && numCols == m.numCols) {
            final double arrayRe[][] = new double[numRows][numCols];
            final double arrayIm[][] = new double[numRows][numCols];
            for (int j, i = 0; i < numRows; i++) {
                arrayRe[i][0] = matrixRe[i][0] + m.matrixRe[i][0];
                arrayIm[i][0] = matrixIm[i][0] + m.matrixIm[i][0];
                for (j = 1; j < numCols; j++) {
                    arrayRe[i][j] = matrixRe[i][j] + m.matrixRe[i][j];
                    arrayIm[i][j] = matrixIm[i][j] + m.matrixIm[i][j];
                }
            }
            return new ComplexSquareMatrix(arrayRe, arrayIm);
        } else throw new MatrixDimensionException("Matrices are different sizes.");
    }

    /**
        * Returns the addition of this matrix and another.
        * @param m a complex square matrix
        * @exception MatrixDimensionException If the matrices are different sizes.
        */
    public ComplexSquareMatrix add(final ComplexSquareMatrix m) {
        switch(m.storageFormat) {
            case ARRAY_2D:
                return rawAdd(m);
            default:
                if (numRows == m.rows()) {
                    final double arrayRe[][] = new double[numRows][numCols];
                    final double arrayIm[][] = new double[numRows][numCols];
                    for (int j, i = 0; i < numRows; i++) {
                        arrayRe[i][0] = matrixRe[i][0] + m.getElement(i, 0).real();
                        arrayIm[i][0] = matrixIm[i][0] + m.getElement(i, 0).imag();
                        for (j = 1; j < numCols; j++) {
                            arrayRe[i][j] = matrixRe[i][j] + m.getElement(i, j).real();
                            arrayIm[i][j] = matrixIm[i][j] + m.getElement(i, j).imag();
                        }
                    }
                    return new ComplexSquareMatrix(arrayRe, arrayIm);
                } else throw new MatrixDimensionException("Matrices are different sizes.");
        }
    }

    /**
        * Returns the subtraction of this matrix by another.
        * @param m a complex matrix
        * @exception MatrixDimensionException If the matrices are different sizes.
        */
    public ComplexMatrix subtract(final ComplexMatrix m) {
        switch(m.storageFormat) {
            case ARRAY_2D:
                return rawSubtract(m);
            default:
                if (numRows == m.rows() && numCols == m.columns()) {
                    final double arrayRe[][] = new double[numRows][numCols];
                    final double arrayIm[][] = new double[numRows][numCols];
                    for (int j, i = 0; i < numRows; i++) {
                        arrayRe[i][0] = matrixRe[i][0] - m.getElement(i, 0).real();
                        arrayIm[i][0] = matrixIm[i][0] - m.getElement(i, 0).imag();
                        for (j = 1; j < numCols; j++) {
                            arrayRe[i][j] = matrixRe[i][j] - m.getElement(i, j).real();
                            arrayIm[i][j] = matrixIm[i][j] - m.getElement(i, j).imag();
                        }
                    }
                    return new ComplexSquareMatrix(arrayRe, arrayIm);
                } else throw new MatrixDimensionException("Matrices are different sizes.");
        }
    }

    private ComplexSquareMatrix rawSubtract(final ComplexMatrix m) {
        if (numRows == m.numRows && numCols == m.numCols) {
            final double arrayRe[][] = new double[numRows][numCols];
            final double arrayIm[][] = new double[numRows][numCols];
            for (int j, i = 0; i < numRows; i++) {
                arrayRe[i][0] = matrixRe[i][0] - m.matrixRe[i][0];
                arrayIm[i][0] = matrixIm[i][0] - m.matrixIm[i][0];
                for (j = 1; j < numCols; j++) {
                    arrayRe[i][j] = matrixRe[i][j] - m.matrixRe[i][j];
                    arrayIm[i][j] = matrixIm[i][j] - m.matrixIm[i][j];
                }
            }
            return new ComplexSquareMatrix(arrayRe, arrayIm);
        } else throw new MatrixDimensionException("Matrices are different sizes.");
    }

    /**
        * Returns the subtraction of this matrix by another.
        * @param m a complex square matrix
        * @exception MatrixDimensionException If the matrices are different sizes.
        */
    public ComplexSquareMatrix subtract(final ComplexSquareMatrix m) {
        switch(m.storageFormat) {
            case ARRAY_2D:
                return rawSubtract(m);
            default:
                if (numRows == m.rows()) {
                    final double arrayRe[][] = new double[numRows][numCols];
                    final double arrayIm[][] = new double[numRows][numCols];
                    for (int j, i = 0; i < numRows; i++) {
                        arrayRe[i][0] = matrixRe[i][0] - m.getElement(i, 0).real();
                        arrayIm[i][0] = matrixIm[i][0] - m.getElement(i, 0).imag();
                        for (j = 1; j < numCols; j++) {
                            arrayRe[i][j] = matrixRe[i][j] - m.getElement(i, j).real();
                            arrayIm[i][j] = matrixIm[i][j] - m.getElement(i, j).imag();
                        }
                    }
                    return new ComplexSquareMatrix(arrayRe, arrayIm);
                } else throw new MatrixDimensionException("Matrices are different sizes.");
        }
    }

    /**
        * Returns the multiplication of this matrix by a scalar.
        * @param z a complex number
        * @return a complex square matrix
        */
    public ComplexMatrix scalarMultiply(final Complex z) {
        final double real = z.real();
        final double imag = z.imag();
        final double arrayRe[][] = new double[numRows][numCols];
        final double arrayIm[][] = new double[numRows][numCols];
        for (int j, i = 0; i < numRows; i++) {
            arrayRe[i][0] = matrixRe[i][0] * real - matrixIm[i][0] * imag;
            arrayIm[i][0] = matrixRe[i][0] * imag + matrixIm[i][0] * real;
            for (j = 1; j < numCols; j++) {
                arrayRe[i][j] = matrixRe[i][j] * real - matrixIm[i][j] * imag;
                arrayIm[i][j] = matrixRe[i][j] * imag + matrixIm[i][j] * real;
            }
        }
        return new ComplexSquareMatrix(arrayRe, arrayIm);
    }

    /**
        * Returns the multiplication of this matrix by a scalar.
        * @param x a double
        * @return a complex square matrix
        */
    public ComplexMatrix scalarMultiply(final double x) {
        final double arrayRe[][] = new double[numRows][numCols];
        final double arrayIm[][] = new double[numRows][numCols];
        for (int j, i = 0; i < numRows; i++) {
            arrayRe[i][0] = x * matrixRe[i][0];
            arrayIm[i][0] = x * matrixIm[i][0];
            for (j = 1; j < numCols; j++) {
                arrayRe[i][j] = x * matrixRe[i][j];
                arrayIm[i][j] = x * matrixIm[i][j];
            }
        }
        return new ComplexSquareMatrix(arrayRe, arrayIm);
    }

    /**
        * Returns the multiplication of a vector by this matrix.
        * @param v a complex vector
        * @exception DimensionException If the matrix and vector are incompatible.
        */
    public ComplexVector multiply(final ComplexVector v) {
        if (numCols == v.dimension()) {
            final double arrayRe[] = new double[numRows];
            final double arrayIm[] = new double[numRows];
            Complex comp;
            for (int j, i = 0; i < numRows; i++) {
                comp = v.getComponent(0);
                arrayRe[i] = (matrixRe[i][0] * comp.real() - matrixIm[i][0] * comp.imag());
                arrayIm[i] = (matrixIm[i][0] * comp.real() + matrixRe[i][0] * comp.imag());
                for (j = 1; j < numCols; j++) {
                    comp = v.getComponent(j);
                    arrayRe[i] += (matrixRe[i][j] * comp.real() - matrixIm[i][j] * comp.imag());
                    arrayIm[i] += (matrixIm[i][j] * comp.real() + matrixRe[i][j] * comp.imag());
                }
            }
            return new ComplexVector(arrayRe, arrayIm);
        } else throw new DimensionException("Matrix and vector are incompatible.");
    }

    /**
        * Returns the multiplication of this matrix and another.
        * @param m a complex square matrix
        * @exception MatrixDimensionException If the matrices are incompatible.
        */
    public ComplexSquareMatrix multiply(final ComplexSquareMatrix m) {
        switch(m.storageFormat) {
            case ARRAY_2D:
                return rawMultiply(m);
            default:
                if (numCols == m.rows()) {
                    final double arrayRe[][] = new double[numRows][numCols];
                    final double arrayIm[][] = new double[numRows][numCols];
                    int n, k;
                    Complex elem;
                    for (int j = 0; j < numRows; j++) {
                        for (k = 0; k < numCols; k++) {
                            elem = m.getElement(0, k);
                            arrayRe[j][k] = (matrixRe[j][0] * elem.real() - matrixIm[j][0] * elem.imag());
                            arrayIm[j][k] = (matrixIm[j][0] * elem.real() + matrixRe[j][0] * elem.imag());
                            for (n = 1; n < numCols; n++) {
                                elem = m.getElement(n, k);
                                arrayRe[j][k] += (matrixRe[j][n] * elem.real() - matrixIm[j][n] * elem.imag());
                                arrayIm[j][k] += (matrixIm[j][n] * elem.real() + matrixRe[j][n] * elem.imag());
                            }
                        }
                    }
                    return new ComplexSquareMatrix(arrayRe, arrayIm);
                } else throw new MatrixDimensionException("Incompatible matrices.");
        }
    }

    private ComplexSquareMatrix rawMultiply(final ComplexSquareMatrix m) {
        if (numCols == m.numRows) {
            int n, k;
            final double arrayRe[][] = new double[numRows][numCols];
            final double arrayIm[][] = new double[numRows][numCols];
            for (int j = 0; j < numRows; j++) {
                for (k = 0; k < numCols; k++) {
                    arrayRe[j][k] = (matrixRe[j][0] * m.matrixRe[0][k] - matrixIm[j][0] * m.matrixIm[0][k]);
                    arrayIm[j][k] = (matrixIm[j][0] * m.matrixRe[0][k] + matrixRe[j][0] * m.matrixIm[0][k]);
                    for (n = 1; n < numCols; n++) {
                        arrayRe[j][k] += (matrixRe[j][n] * m.matrixRe[n][k] - matrixIm[j][n] * m.matrixIm[n][k]);
                        arrayIm[j][k] += (matrixIm[j][n] * m.matrixRe[n][k] + matrixRe[j][n] * m.matrixIm[n][k]);
                    }
                }
            }
            return new ComplexSquareMatrix(arrayRe, arrayIm);
        } else throw new MatrixDimensionException("Incompatible matrices.");
    }

    /**
        * Returns the involution of this matrix.
        */
    public CStarAlgebra.Member involution() {
        return (ComplexSquareMatrix) hermitianAdjoint();
    }

    /**
        * Returns the hermitian adjoint of this matrix.
        * @return a complex square matrix
        */
    public ComplexMatrix hermitianAdjoint() {
        final double arrayRe[][] = new double[numCols][numRows];
        final double arrayIm[][] = new double[numCols][numRows];
        for (int j, i = 0; i < numRows; i++) {
            arrayRe[0][i] = matrixRe[i][0];
            arrayIm[0][i] = -matrixIm[i][0];
            for (j = 1; j < numCols; j++) {
                arrayRe[j][i] = matrixRe[i][j];
                arrayIm[j][i] = -matrixIm[i][j];
            }
        }
        return new ComplexSquareMatrix(arrayRe, arrayIm);
    }

    /**
        * Returns the complex conjugate of this matrix.
        * @return a complex square matrix
        */
    public ComplexMatrix conjugate() {
        final double arrayIm[][] = new double[numRows][numCols];
        for (int j, i = 0; i < numRows; i++) {
            arrayIm[i][0] = -matrixIm[i][0];
            for (j = 1; j < numCols; j++) arrayIm[i][j] = -matrixIm[i][j];
        }
        return new ComplexSquareMatrix(matrixRe, arrayIm);
    }

    /**
        * Returns the transpose of this matrix.
        * @return a complex square matrix
        */
    public Matrix transpose() {
        final double arrayRe[][] = new double[numCols][numRows];
        final double arrayIm[][] = new double[numCols][numRows];
        for (int j, i = 0; i < numRows; i++) {
            arrayRe[0][i] = matrixRe[i][0];
            arrayIm[0][i] = matrixIm[i][0];
            for (j = 1; j < numCols; j++) {
                arrayRe[j][i] = matrixRe[i][j];
                arrayIm[j][i] = matrixIm[i][j];
            }
        }
        return new ComplexSquareMatrix(arrayRe, arrayIm);
    }

    /**
        * Returns the inverse of this matrix.
        * @return a complex square matrix
        */
    public ComplexSquareMatrix inverse() {
        int i, j, k;
        final int N = numRows;
        final double arrayLRe[][] = new double[N][N];
        final double arrayLIm[][] = new double[N][N];
        final double arrayURe[][] = new double[N][N];
        final double arrayUIm[][] = new double[N][N];
        final ComplexSquareMatrix lu[] = this.luDecompose(null);
        double denom;
        denom = lu[0].matrixRe[0][0] * lu[0].matrixRe[0][0] + lu[0].matrixIm[0][0] * lu[0].matrixIm[0][0];
        arrayLRe[0][0] = lu[0].matrixRe[0][0] / denom;
        arrayLIm[0][0] = -lu[0].matrixIm[0][0] / denom;
        denom = lu[1].matrixRe[0][0] * lu[1].matrixRe[0][0] + lu[1].matrixIm[0][0] * lu[1].matrixIm[0][0];
        arrayURe[0][0] = lu[1].matrixRe[0][0] / denom;
        arrayUIm[0][0] = -lu[1].matrixIm[0][0] / denom;
        for (i = 1; i < N; i++) {
            denom = lu[0].matrixRe[i][i] * lu[0].matrixRe[i][i] + lu[0].matrixIm[i][i] * lu[0].matrixIm[i][i];
            arrayLRe[i][i] = lu[0].matrixRe[i][i] / denom;
            arrayLIm[i][i] = -lu[0].matrixIm[i][i] / denom;
            denom = lu[1].matrixRe[i][i] * lu[1].matrixRe[i][i] + lu[1].matrixIm[i][i] * lu[1].matrixIm[i][i];
            arrayURe[i][i] = lu[1].matrixRe[i][i] / denom;
            arrayUIm[i][i] = -lu[1].matrixIm[i][i] / denom;
        }
        double tmpLRe, tmpLIm;
        double tmpURe, tmpUIm;
        for (i = 0; i < N - 1; i++) {
            for (j = i + 1; j < N; j++) {
                tmpLRe = tmpLIm = 0.0;
                tmpURe = tmpUIm = 0.0;
                for (k = i; k < j; k++) {
                    tmpLRe -= (lu[0].matrixRe[j][k] * arrayLRe[k][i] - lu[0].matrixIm[j][k] * arrayLIm[k][i]);
                    tmpLIm -= (lu[0].matrixIm[j][k] * arrayLRe[k][i] + lu[0].matrixRe[j][k] * arrayLIm[k][i]);
                    tmpURe -= (arrayURe[i][k] * lu[1].matrixRe[k][j] - arrayUIm[i][k] * lu[1].matrixIm[k][j]);
                    tmpUIm -= (arrayUIm[i][k] * lu[1].matrixRe[k][j] + arrayURe[i][k] * lu[1].matrixIm[k][j]);
                }
                denom = lu[0].matrixRe[j][j] * lu[0].matrixRe[j][j] + lu[0].matrixIm[j][j] * lu[0].matrixIm[j][j];
                arrayLRe[j][i] = (tmpLRe * lu[0].matrixRe[j][j] + tmpLIm * lu[0].matrixIm[j][j]) / denom;
                arrayLIm[j][i] = (tmpLIm * lu[0].matrixRe[j][j] - tmpLRe * lu[0].matrixIm[j][j]) / denom;
                denom = lu[1].matrixRe[j][j] * lu[1].matrixRe[j][j] + lu[1].matrixIm[j][j] * lu[1].matrixIm[j][j];
                arrayURe[i][j] = (tmpURe * lu[1].matrixRe[j][j] + tmpUIm * lu[1].matrixIm[j][j]) / denom;
                arrayUIm[i][j] = (tmpUIm * lu[1].matrixRe[j][j] - tmpURe * lu[1].matrixIm[j][j]) / denom;
            }
        }
        final double invRe[][] = new double[N][N];
        final double invIm[][] = new double[N][N];
        for (i = 0; i < N; i++) {
            for (j = 0; j < i; j++) {
                for (k = i; k < N; k++) {
                    invRe[i][LUpivot[j]] += (arrayURe[i][k] * arrayLRe[k][j] - arrayUIm[i][k] * arrayLIm[k][j]);
                    invIm[i][LUpivot[j]] += (arrayUIm[i][k] * arrayLRe[k][j] + arrayURe[i][k] * arrayLIm[k][j]);
                }
            }
            for (j = i; j < N; j++) {
                for (k = j; k < N; k++) {
                    invRe[i][LUpivot[j]] += (arrayURe[i][k] * arrayLRe[k][j] - arrayUIm[i][k] * arrayLIm[k][j]);
                    invIm[i][LUpivot[j]] += (arrayUIm[i][k] * arrayLRe[k][j] + arrayURe[i][k] * arrayLIm[k][j]);
                }
            }
        }
        return new ComplexSquareMatrix(invRe, invIm);
    }

    /**
        * Returns the LU decomposition of this matrix.
        * @return an array with [0] containing the L-matrix and [1] containing the U-matrix.
        */
    public ComplexSquareMatrix[] luDecompose(int pivot[]) {
        if (LU != null) {
            if (pivot != null) System.arraycopy(LUpivot, 0, pivot, 0, pivot.length);
            return LU;
        }
        int i, j, k, pivotrow;
        final int N = numRows;
        final double arrayLRe[][] = new double[N][N];
        final double arrayLIm[][] = new double[N][N];
        final double arrayURe[][] = new double[N][N];
        final double arrayUIm[][] = new double[N][N];
        final double buf[] = new double[N];
        double tmp, tmpRe, tmpIm;
        double max;
        if (pivot == null) pivot = new int[N + 1];
        for (i = 0; i < N; i++) pivot[i] = i;
        pivot[N] = 1;
        for (j = 0; j < N; j++) {
            for (i = 0; i < j; i++) {
                tmpRe = matrixRe[pivot[i]][j];
                tmpIm = matrixIm[pivot[i]][j];
                for (k = 0; k < i; k++) {
                    tmpRe -= (arrayURe[i][k] * arrayURe[k][j] - arrayUIm[i][k] * arrayUIm[k][j]);
                    tmpIm -= (arrayUIm[i][k] * arrayURe[k][j] + arrayURe[i][k] * arrayUIm[k][j]);
                }
                arrayURe[i][j] = tmpRe;
                arrayUIm[i][j] = tmpIm;
            }
            max = 0.0;
            pivotrow = j;
            for (i = j; i < N; i++) {
                tmpRe = matrixRe[pivot[i]][j];
                tmpIm = matrixIm[pivot[i]][j];
                for (k = 0; k < j; k++) {
                    tmpRe -= (arrayURe[i][k] * arrayURe[k][j] - arrayUIm[i][k] * arrayUIm[k][j]);
                    tmpIm -= (arrayUIm[i][k] * arrayURe[k][j] + arrayURe[i][k] * arrayUIm[k][j]);
                }
                arrayURe[i][j] = tmpRe;
                arrayUIm[i][j] = tmpIm;
                tmp = tmpRe * tmpRe + tmpIm * tmpIm;
                if (tmp > max) {
                    max = tmp;
                    pivotrow = i;
                }
            }
            if (pivotrow != j) {
                System.arraycopy(arrayURe[j], 0, buf, 0, j + 1);
                System.arraycopy(arrayURe[pivotrow], 0, arrayURe[j], 0, j + 1);
                System.arraycopy(buf, 0, arrayURe[pivotrow], 0, j + 1);
                System.arraycopy(arrayUIm[j], 0, buf, 0, j + 1);
                System.arraycopy(arrayUIm[pivotrow], 0, arrayUIm[j], 0, j + 1);
                System.arraycopy(buf, 0, arrayUIm[pivotrow], 0, j + 1);
                k = pivot[j];
                pivot[j] = pivot[pivotrow];
                pivot[pivotrow] = k;
                pivot[N] = -pivot[N];
            }
            tmpRe = arrayURe[j][j];
            tmpIm = arrayUIm[j][j];
            double a, denom;
            if (Math.abs(tmpRe) < Math.abs(tmpIm)) {
                a = tmpRe / tmpIm;
                denom = tmpRe * a + tmpIm;
                for (i = j + 1; i < N; i++) {
                    tmp = (arrayURe[i][j] * a + arrayUIm[i][j]) / denom;
                    arrayUIm[i][j] = (arrayUIm[i][j] * a - arrayURe[i][j]) / denom;
                    arrayURe[i][j] = tmp;
                }
            } else {
                a = tmpIm / tmpRe;
                denom = tmpRe + tmpIm * a;
                for (i = j + 1; i < N; i++) {
                    tmp = (arrayURe[i][j] + arrayUIm[i][j] * a) / denom;
                    arrayUIm[i][j] = (arrayUIm[i][j] - arrayURe[i][j] * a) / denom;
                    arrayURe[i][j] = tmp;
                }
            }
        }
        for (j = 0; j < N; j++) {
            arrayLRe[j][j] = 1.0;
            for (i = j + 1; i < N; i++) {
                arrayLRe[i][j] = arrayURe[i][j];
                arrayLIm[i][j] = arrayUIm[i][j];
                arrayURe[i][j] = 0.0;
                arrayUIm[i][j] = 0.0;
            }
        }
        LU = new ComplexSquareMatrix[2];
        LU[0] = new ComplexSquareMatrix(arrayLRe, arrayLIm);
        LU[1] = new ComplexSquareMatrix(arrayURe, arrayUIm);
        LUpivot = new int[pivot.length];
        System.arraycopy(pivot, 0, LUpivot, 0, pivot.length);
        return LU;
    }

    /**
        * Returns the polar decomposition of this matrix.
        */
    public ComplexSquareMatrix[] polarDecompose() {
        final int N = numRows;
        final ComplexVector evec[] = new ComplexVector[N];
        double eval[];
        try {
            eval = LinearMath.eigenSolveHermitian(this, evec);
        } catch (MaximumIterationsExceededException e) {
            return null;
        }
        final double tmpaRe[][] = new double[N][N];
        final double tmpaIm[][] = new double[N][N];
        final double tmpmRe[][] = new double[N][N];
        final double tmpmIm[][] = new double[N][N];
        double abs;
        Complex comp;
        for (int i = 0; i < N; i++) {
            abs = Math.abs(eval[i]);
            comp = evec[i].getComponent(0).conjugate();
            tmpaRe[i][0] = eval[i] * comp.real() / abs;
            tmpaIm[i][0] = eval[i] * comp.imag() / abs;
            tmpmRe[i][0] = abs * comp.real();
            tmpmIm[i][0] = abs * comp.imag();
            for (int j = 1; j < N; j++) {
                comp = evec[i].getComponent(j).conjugate();
                tmpaRe[i][j] = eval[i] * comp.real() / abs;
                tmpaIm[i][j] = eval[i] * comp.imag() / abs;
                tmpmRe[i][j] = abs * comp.real();
                tmpmIm[i][j] = abs * comp.imag();
            }
        }
        final double argRe[][] = new double[N][N];
        final double argIm[][] = new double[N][N];
        final double modRe[][] = new double[N][N];
        final double modIm[][] = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                comp = evec[0].getComponent(i);
                argRe[i][j] = (tmpaRe[0][j] * comp.real() - tmpaIm[0][j] * comp.imag());
                argIm[i][j] = (tmpaIm[0][j] * comp.real() + tmpaRe[0][j] * comp.imag());
                modRe[i][j] = (tmpmRe[0][j] * comp.real() - tmpmIm[0][j] * comp.imag());
                modIm[i][j] = (tmpmIm[0][j] * comp.real() + tmpmRe[0][j] * comp.imag());
                for (int k = 1; k < N; k++) {
                    comp = evec[k].getComponent(i);
                    argRe[i][j] += (tmpaRe[k][j] * comp.real() - tmpaIm[k][j] * comp.imag());
                    argIm[i][j] += (tmpaIm[k][j] * comp.real() + tmpaRe[k][j] * comp.imag());
                    modRe[i][j] += (tmpmRe[k][j] * comp.real() - tmpmIm[k][j] * comp.imag());
                    modIm[i][j] += (tmpmIm[k][j] * comp.real() + tmpmRe[k][j] * comp.imag());
                }
            }
        }
        final ComplexSquareMatrix us[] = new ComplexSquareMatrix[2];
        us[0] = new ComplexSquareMatrix(argRe, argIm);
        us[1] = new ComplexSquareMatrix(modRe, modIm);
        return us;
    }

    /**
        * Applies a function on all the matrix elements.
        * @param f a user-defined function
        * @return a complex square matrix
        */
    public ComplexMatrix mapElements(final ComplexMapping f) {
        final Complex array[][] = new Complex[numRows][numCols];
        for (int j, i = 0; i < numRows; i++) {
            array[i][0] = f.map(matrixRe[i][0], matrixIm[i][0]);
            for (j = 1; j < numCols; j++) array[i][j] = f.map(matrixRe[i][j], matrixIm[i][j]);
        }
        return new ComplexSquareMatrix(array);
    }
}
