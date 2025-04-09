package org.tigr.util;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Jama = Java Matrix class.
 * <P>
 * The Java Matrix Class provides the fundamental operations of numerical
 * linear algebra.  Various constructors create Matrices from two dimensional
 * arrays of double precision floating point numbers.  Various "gets" and
 * "sets" provide access to submatrices and matrix elements.  Several methods
 * implement basic matrix arithmetic, including matrix addition and
 * multiplication, matrix norms, and element-by-element array operations.
 * Methods for reading and printing matrices are also included.  All the
 * operations in this version of the Matrix Class involve real matrices.
 * Complex matrices may be handled in a future version.
 * <P>
 * Five fundamental matrix decompositions, which consist of pairs or triples
 * of matrices, permutation vectors, and the like, produce results in five
 * decomposition classes.  These decompositions are accessed by the Matrix
 * class to compute solutions of simultaneous linear equations, determinants,
 * inverses and other matrix functions.  The five decompositions are:
 * <P><UL>
 * <LI>Cholesky Decomposition of symmetric, positive definite matrices.
 * <LI>LU Decomposition of rectangular matrices.
 * <LI>QR Decomposition of rectangular matrices.
 * <LI>Singular Value Decomposition of rectangular matrices.
 * <LI>Eigenvalue Decomposition of both symmetric and nonsymmetric square matrices.
 * </UL>
 * <DL>
 * <DT><B>Example of use:</B></DT>
 * <P>
 * <DD>Solve a linear system A x = b and compute the residual norm, ||b - A x||.
 * <P><PRE>
 * double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,10.}};
 * Matrix A = new Matrix(vals);
 * Matrix b = Matrix.random(3,1);
 * Matrix x = A.solve(b);
 * Matrix r = A.times(x).minus(b);
 * double rnorm = r.normInf();
 * </PRE></DD>
 * </DL>
 *
 * @author The MathWorks, Inc. and the National Institute of Standards and Technology.
 * @version 5 August 1998
 */
public class FloatMatrix implements Cloneable {

    /** Array for internal storage of elements.
     * @serial internal array storage.
     */
    public float[][] A;

    /** Row and column dimensions.
     * @serial row dimension.
     * @serial column dimension.
     */
    public int m, n;

    /** Construct an m-by-n matrix of zeros.
     * @param m    Number of rows.
     * @param n    Number of colums.
     */
    public FloatMatrix(int m, int n) {
        this.m = m;
        this.n = n;
        A = new float[m][n];
    }

    /** Construct an m-by-n constant matrix.
     * @param m    Number of rows.
     * @param n    Number of colums.
     * @param s    Fill the matrix with this scalar value.
     */
    public FloatMatrix(int m, int n, float s) {
        this.m = m;
        this.n = n;
        A = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s;
            }
        }
    }

    /** Construct a matrix from a 2-D array.
     * @param A    Two-dimensional array of doubles.
     * @exception  IllegalArgumentException All rows must have the same length
     * @see        #constructWithCopy
     */
    public FloatMatrix(float[][] A) {
        m = A.length;
        n = A[0].length;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        this.A = A;
    }

    /** Construct a matrix quickly without checking arguments.
     * @param A    Two-dimensional array of doubles.
     * @param m    Number of rows.
     * @param n    Number of colums.
     */
    public FloatMatrix(float[][] A, int m, int n) {
        this.A = A;
        this.m = m;
        this.n = n;
    }

    /** Construct a matrix from a one-dimensional packed array
     * @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m    Number of rows.
     * @exception  IllegalArgumentException Array length must be a multiple of m.
     */
    public FloatMatrix(float vals[], int m) {
        this.m = m;
        n = (m != 0 ? vals.length / m : 0);
        if (m * n != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        A = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = vals[i + j * m];
            }
        }
    }

    public static String[] getPersistenceDelegateArgs() {
        return new String[] { "array", "rowDimension", "columnDimension" };
    }

    /** Construct a matrix from a copy of a 2-D array.
     * @param A    Two-dimensional array of doubles.
     * @exception  IllegalArgumentException All rows must have the same length
     */
    public static FloatMatrix constructWithCopy(float[][] A) {
        int m = A.length;
        int n = A[0].length;
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }

    /** Make a deep copy of a matrix
     */
    public FloatMatrix copy() {
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }

    /** Clone the Matrix object.
     */
    public Object clone() {
        return this.copy();
    }

    /** Access the internal two-dimensional array.
     * @return     Pointer to the two-dimensional array of matrix elements.
     */
    public float[][] getArray() {
        return A;
    }

    /** Copy the internal two-dimensional array.
     * @return     Two-dimensional array copy of matrix elements.
     */
    public float[][] getArrayCopy() {
        float[][] C = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return C;
    }

    /** Make a one-dimensional column packed copy of the internal array.
     * @return     Matrix elements packed in a one-dimensional array by columns.
     */
    public float[] getColumnPackedCopy() {
        float[] vals = new float[m * n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                vals[i + j * m] = A[i][j];
            }
        }
        return vals;
    }

    /** Make a one-dimensional row packed copy of the internal array.
     * @return     Matrix elements packed in a one-dimensional array by rows.
     */
    public float[] getRowPackedCopy() {
        float[] vals = new float[m * n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                vals[i * n + j] = A[i][j];
            }
        }
        return vals;
    }

    /** Get row dimension.
     * @return     m, the number of rows.
     */
    public int getRowDimension() {
        return m;
    }

    /** Get column dimension.
     * @return     n, the number of columns.
     */
    public int getColumnDimension() {
        return n;
    }

    /** Get a single element.
     * @param i    Row index.
     * @param j    Column index.
     * @return     A(i,j)
     * @exception  ArrayIndexOutOfBoundsException
     */
    public float get(int i, int j) {
        return A[i][j];
    }

    /** Get a submatrix.
     * @param i0   Initial row index
     * @param i1   Final row index
     * @param j0   Initial column index
     * @param j1   Final column index
     * @return     A(i0:i1,j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public FloatMatrix getMatrix(int i0, int i1, int j0, int j1) {
        FloatMatrix X = new FloatMatrix(i1 - i0 + 1, j1 - j0 + 1);
        float[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i - i0][j - j0] = A[i][j];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /** Get a submatrix.
     * @param r    Array of row indices.
     * @param c    Array of column indices.
     * @return     A(r(:),c(:))
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public FloatMatrix getMatrix(int[] r, int[] c) {
        FloatMatrix X = new FloatMatrix(r.length, c.length);
        float[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    B[i][j] = A[r[i]][c[j]];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /** Get a submatrix.
     * @param i0   Initial row index
     * @param i1   Final row index
     * @param c    Array of column indices.
     * @return     A(i0:i1,c(:))
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public FloatMatrix getMatrix(int i0, int i1, int[] c) {
        FloatMatrix X = new FloatMatrix(i1 - i0 + 1, c.length);
        float[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    B[i - i0][j] = A[i][c[j]];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /** Get a submatrix.
     * @param r    Array of row indices.
     * @param i0   Initial column index
     * @param i1   Final column index
     * @return     A(r(:),j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public FloatMatrix getMatrix(int[] r, int j0, int j1) {
        FloatMatrix X = new FloatMatrix(r.length, j1 - j0 + 1);
        float[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i][j - j0] = A[r[i]][j];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /** Set a single element.
     * @param i    Row index.
     * @param j    Column index.
     * @param s    A(i,j).
     * @exception  ArrayIndexOutOfBoundsException
     */
    public void set(int i, int j, float s) {
        A[i][j] = s;
    }

    /** Set a submatrix.
     * @param i0   Initial row index
     * @param i1   Final row index
     * @param j0   Initial column index
     * @param j1   Final column index
     * @param X    A(i0:i1,j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int i0, int i1, int j0, int j1, FloatMatrix X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    A[i][j] = X.get(i - i0, j - j0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /** Set a submatrix.
     * @param r    Array of row indices.
     * @param c    Array of column indices.
     * @param X    A(r(:),c(:))
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int[] r, int[] c, FloatMatrix X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    A[r[i]][c[j]] = X.get(i, j);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /** Set a submatrix.
     * @param r    Array of row indices.
     * @param j0   Initial column index
     * @param j1   Final column index
     * @param X    A(r(:),j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int[] r, int j0, int j1, FloatMatrix X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    A[r[i]][j] = X.get(i, j - j0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /** Set a submatrix.
     * @param i0   Initial row index
     * @param i1   Final row index
     * @param c    Array of column indices.
     * @param X    A(i0:i1,c(:))
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int i0, int i1, int[] c, FloatMatrix X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                    A[i][c[j]] = X.get(i - i0, j);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /** Matrix transpose.
     * @return    A'
     */
    public FloatMatrix transpose() {
        FloatMatrix X = new FloatMatrix(n, m);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return X;
    }

    /** One norm
     * @return    maximum column sum.
     */
    public float norm1() {
        float f = 0;
        for (int j = 0; j < n; j++) {
            float s = 0;
            for (int i = 0; i < m; i++) {
                s += Math.abs(A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /** Infinity norm
     * @return    maximum row sum.
     */
    public float normInf() {
        float f = 0;
        for (int i = 0; i < m; i++) {
            float s = 0;
            for (int j = 0; j < n; j++) {
                s += Math.abs(A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /** Frobenius norm
     * @return    sqrt of sum of squares of all elements.
     */
    public float normF() {
        float f = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                f = (float) Maths.hypot(f, A[i][j]);
            }
        }
        return f;
    }

    /**  Unary minus
     * @return    -A
     */
    public FloatMatrix uminus() {
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = -A[i][j];
            }
        }
        return X;
    }

    /** C = A + B
     * @param B    another matrix
     * @return     A + B
     */
    public FloatMatrix plus(FloatMatrix B) {
        checkMatrixDimensions(B);
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return X;
    }

    /** A = A + B
     * @param B    another matrix
     * @return     A + B
     */
    public FloatMatrix plusEquals(FloatMatrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return this;
    }

    /** C = A - B
     * @param B    another matrix
     * @return     A - B
     */
    public FloatMatrix minus(FloatMatrix B) {
        checkMatrixDimensions(B);
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return X;
    }

    /** A = A - B
     * @param B    another matrix
     * @return     A - B
     */
    public FloatMatrix minusEquals(FloatMatrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return this;
    }

    /** Element-by-element multiplication, C = A.*B
     * @param B    another matrix
     * @return     A.*B
     */
    public FloatMatrix arrayTimes(FloatMatrix B) {
        checkMatrixDimensions(B);
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return X;
    }

    /** Element-by-element multiplication in place, A = A.*B
     * @param B    another matrix
     * @return     A.*B
     */
    public FloatMatrix arrayTimesEquals(FloatMatrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return this;
    }

    /** Element-by-element right division, C = A./B
     * @param B    another matrix
     * @return     A./B
     */
    public FloatMatrix arrayRightDivide(FloatMatrix B) {
        checkMatrixDimensions(B);
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return X;
    }

    /** Element-by-element right division in place, A = A./B
     * @param B    another matrix
     * @return     A./B
     */
    public FloatMatrix arrayRightDivideEquals(FloatMatrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return this;
    }

    /** Element-by-element left division, C = A.\B
     * @param B    another matrix
     * @return     A.\B
     */
    public FloatMatrix arrayLeftDivide(FloatMatrix B) {
        checkMatrixDimensions(B);
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = B.A[i][j] / A[i][j];
            }
        }
        return X;
    }

    /** Element-by-element left division in place, A = A.\B
     * @param B    another matrix
     * @return     A.\B
     */
    public FloatMatrix arrayLeftDivideEquals(FloatMatrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = B.A[i][j] / A[i][j];
            }
        }
        return this;
    }

    /** Multiply a matrix by a scalar, C = s*A
     * @param s    scalar
     * @return     s*A
     */
    public FloatMatrix times(float s) {
        FloatMatrix X = new FloatMatrix(m, n);
        float[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = s * A[i][j];
            }
        }
        return X;
    }

    /** Multiply a matrix by a scalar in place, A = s*A
     * @param s    scalar
     * @return     replace A by s*A
     */
    public FloatMatrix timesEquals(float s) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s * A[i][j];
            }
        }
        return this;
    }

    /** Linear algebraic matrix multiplication, A * B
     * @param B    another matrix
     * @return     Matrix product, A * B
     * @exception  IllegalArgumentException Matrix inner dimensions must agree.
     */
    public FloatMatrix times(FloatMatrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        FloatMatrix X = new FloatMatrix(m, B.n);
        float[][] C = X.getArray();
        float[] Bcolj = new float[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.A[k][j];
            }
            for (int i = 0; i < m; i++) {
                float[] Arowi = A[i];
                float s = 0;
                for (int k = 0; k < n; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    /** Matrix trace.
     * @return     sum of the diagonal elements.
     */
    public float trace() {
        float t = 0;
        for (int i = 0; i < Math.min(m, n); i++) {
            t += A[i][i];
        }
        return t;
    }

    /** Generate matrix with random elements
     * @param m    Number of rows.
     * @param n    Number of colums.
     * @return     An m-by-n matrix with uniformly distributed random elements.
     */
    public static FloatMatrix random(int m, int n) {
        FloatMatrix A = new FloatMatrix(m, n);
        float[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (float) Math.random();
            }
        }
        return A;
    }

    /** Generate identity matrix
     * @param m    Number of rows.
     * @param n    Number of colums.
     * @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */
    public static FloatMatrix identity(int m, int n) {
        FloatMatrix A = new FloatMatrix(m, n);
        float[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (float) (i == j ? 1.0 : 0.0);
            }
        }
        return A;
    }

    /** Print the matrix to stdout.   Line the elements up in columns
     * with a Fortran-like 'Fw.d' style format.
     * @param w    Column width.
     * @param d    Number of digits after the decimal.
     */
    public void print(int w, int d) {
        print(new PrintWriter(System.out, true), w, d);
    }

    /** Print the matrix to the output stream.   Line the elements up in
	 * columns with a Fortran-like 'Fw.d' style format.
	 * @param output Output stream.
	 * @param w      Column width.
	 * @param d      Number of digits after the decimal.
	 */
    public void print(PrintWriter output, int w, int d) {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(d);
        format.setMinimumFractionDigits(d);
        format.setGroupingUsed(false);
        print(output, format, w + 2);
    }

    /** Print the matrix to stdout.  Line the elements up in columns.
	 * Use the format object, and right justify within columns of width
	 * characters.
	 * @param format A  Formatting object for individual elements.
	 * @param width     Field width for each column.
	 */
    public void print(NumberFormat format, int width) {
        print(new PrintWriter(System.out, true), format, width);
    }

    /** Print the matrix to the output stream.  Line the elements up in columns.
	     * Use the format object, and right justify within columns of width
	     * characters.
	     * @param output the output stream.
	     * @param format A formatting object to format the matrix elements
	     * @param width  Column width.
	     */
    public void print(PrintWriter output, NumberFormat format, int width) {
        output.println();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                String s = format.format(A[i][j]);
                int padding = Math.max(1, width - s.length());
                for (int k = 0; k < padding; k++) output.print(' ');
                output.print(s);
            }
            output.println();
        }
        output.println();
    }

    /** Read a matrix from a stream.  The format is the same the print method,
	     * so printed matrices can be read back in.  Elements are separated by
	     * whitespace, all the elements for each row appear on a single line,
	     * the last row is followed by a blank line.
	     * @param input the input stream.
	     */
    public static FloatMatrix read(BufferedReader input) throws java.io.IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(input);
        tokenizer.resetSyntax();
        tokenizer.wordChars(0, 255);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.eolIsSignificant(true);
        java.util.Vector v = new java.util.Vector();
        while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) ;
        if (tokenizer.ttype == StreamTokenizer.TT_EOF) throw new java.io.IOException("Unexpected EOF on matrix read.");
        do {
            v.addElement(Float.valueOf(tokenizer.sval));
        } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
        int n = v.size();
        float row[] = new float[n];
        for (int j = 0; j < n; j++) row[j] = ((Float) v.elementAt(j)).floatValue();
        v.removeAllElements();
        v.addElement(row);
        while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
            v.addElement(row = new float[n]);
            int j = 0;
            do {
                if (j >= n) throw new java.io.IOException("Row " + v.size() + " is too long.");
                row[j++] = Float.valueOf(tokenizer.sval).floatValue();
            } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
            if (j < n) throw new java.io.IOException("Row " + v.size() + " is too short.");
        }
        int m = v.size();
        float[][] A = new float[m][];
        v.copyInto(A);
        return new FloatMatrix(A);
    }

    /** Check if size(A) == size(B) **/
    private void checkMatrixDimensions(FloatMatrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

    /** Get a sub-matrix .
     * @return    A'
     */
    public FloatMatrix getSubMatrix(int displayInterval) {
        if (displayInterval <= 0) return this;
        int sub_m = m / displayInterval;
        int sub_n = n / displayInterval;
        FloatMatrix X = new FloatMatrix(sub_m, sub_n);
        float[][] C = X.getArray();
        for (int i = 0, k = 0; i < m && k < sub_m; i += displayInterval, k++) {
            for (int j = 0, l = 0; j < n && l < sub_n; j += displayInterval, l++) {
                C[k][l] = A[i][j];
            }
        }
        return X;
    }
}
