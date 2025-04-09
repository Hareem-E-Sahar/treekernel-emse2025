package petrieditor.modules.invariants;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class Matrix {

    /**
     * Array for internal storage of elements.
     * @serial internal array storage.
     */
    private int[][] A;

    /**
     * Row and column dimensions.
     * @serial row dimension.
     * @serial column dimension.
     */
    private int m, n;

    /**
     * Construct an m-by-n matrix of zeros.
     * @param m    Number of rows.
     * @param n    Number of colums.
     */
    public Matrix(int m, int n) {
        this.m = m;
        this.n = n;
        A = new int[m][n];
    }

    /**
     * Construct an m-by-n constant matrix.
     * @param m Number of rows.
     * @param n Number of colums.
     * @param s Fill the matrix with this scalar value.
     */
    public Matrix(int m, int n, int s) {
        this.m = m;
        this.n = n;
        A = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s;
            }
        }
    }

    /**
     * Construct a matrix from a 2-D array.
     * @param A Two-dimensional array of integers.
     * @exception IllegalArgumentException All rows must have the same length
     * @see #constructWithCopy
     */
    public Matrix(int[][] A) {
        if (A != null) {
            m = A.length;
            if (A.length >= 1) {
                n = A[0].length;
                for (int i = 0; i < m; i++) {
                    if (A[i].length != n) {
                        throw new IllegalArgumentException("All rows must have the same length.");
                    }
                }
                this.A = A;
            }
        }
    }

    /**
     * Construct a matrix quickly without checking arguments.
     * @param A Two-dimensional array of integers.
     * @param m Number of rows.
     * @param n Number of colums.
     */
    public Matrix(int[][] A, int m, int n) {
        this.A = A;
        this.m = m;
        this.n = n;
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     * @param vals One-dimensional array of integers, packed by columns (ala Fortran).
     * @param m Number of rows.
     * @exception IllegalArgumentException Array length must be a multiple of m.
     */
    public Matrix(int vals[], int m) {
        this.m = m;
        n = (m != 0 ? vals.length / m : 0);
        if (m * n != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        A = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = vals[i + j * m];
            }
        }
    }

    public Matrix mul(Matrix a, Matrix b) {
        if (a.getColumnDimension() != b.getRowDimension()) {
            System.out.println(" zle wymiary ");
            return null;
        }
        Matrix res = new Matrix(a.getRowDimension(), b.getColumnDimension());
        for (int i = 0; i < a.getRowDimension(); i++) for (int j = 0; j < b.getColumnDimension(); j++) for (int k = 0; k < a.getColumnDimension(); k++) res.set(i, j, res.get(i, j) + a.get(i, k) * b.get(k, j));
        return res;
    }

    public int[] getRow(int r) {
        return this.A[r];
    }

    public void setRow(int r, int row[]) {
        for (int i = 0; i < getColumnDimension(); i++) A[r][i] = row[i];
    }

    public int[] getRowCopy(int r) {
        int row[] = new int[getColumnDimension()];
        for (int i = 0; i < getColumnDimension(); i++) row[i] = A[r][i];
        return row;
    }

    /**
     * Construct a matrix from a copy of a 2-D array.
     * @param A Two-dimensional array of integers.
     * @return The copied matrix.
     * @exception IllegalArgumentException All rows must have the same length
     */
    public static Matrix constructWithCopy(int[][] A) {
        int m = A.length;
        int n = A[0].length;
        Matrix X = new Matrix(m, n);
        int[][] C = X.getArray();
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

    public Matrix eliminateRow(int row) {
        int m = getRowDimension(), n = getColumnDimension();
        int[] rows = new int[m - 1];
        int count = 0;
        for (int i = 0; i < m; i++) {
            if (i != row) rows[count++] = i;
        }
        Matrix reduced = getMatrix(rows, 0, n - 1);
        return reduced;
    }

    /**
     * Access the internal two-dimensional array.
     * @return Pointer to the two-dimensional array of matrix elements.
     */
    public int[][] getArray() {
        return A;
    }

    /**
     * Copy the internal two-dimensional array.
     * @return Two-dimensional array copy of matrix elements.
     */
    public int[][] getArrayCopy() {
        int[][] C = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return C;
    }

    /**
     * Get row dimension.
     * @return The number of rows.
     */
    public int getRowDimension() {
        return m;
    }

    /**
     * Get column dimension.
     * @return The number of columns.
     */
    public int getColumnDimension() {
        return n;
    }

    /**
     * Get a single element.
     * @param i Row index.
     * @param j Column index.
     * @return A(i,j)
     * @exception ArrayIndexOutOfBoundsException
     */
    public int get(int i, int j) {
        return A[i][j];
    }

    /** Get a submatrix.
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(i0:i1,j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int i0, int i1, int j0, int j1) {
        Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
        int[][] B = X.getArray();
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

    /**
     * Get a submatrix.
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @return A(r(:),c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int[] r, int[] c) {
        Matrix X = new Matrix(r.length, c.length);
        int[][] B = X.getArray();
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

    /**
     * Get a submatrix.
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c Array of column indices.
     * @return A(i0:i1,c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int i0, int i1, int[] c) {
        Matrix X = new Matrix(i1 - i0 + 1, c.length);
        int[][] B = X.getArray();
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

    /**
     * Get a submatrix.
     * @param r Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(r(:),j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int[] r, int j0, int j1) {
        Matrix X = new Matrix(r.length, j1 - j0 + 1);
        int[][] B = X.getArray();
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

    /**
     * Set a single element.
     * @param i    Row index.
     * @param j    Column index.
     * @param s    A(i,j).
     * @exception  ArrayIndexOutOfBoundsException
     */
    public void set(int i, int j, int s) {
        A[i][j] = s;
    }

    /**
     * Set a submatrix.
     * @param i0   Initial row index
     * @param i1   Final row index
     * @param j0   Initial column index
     * @param j1   Final column index
     * @param X    A(i0:i1,j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int i0, int i1, int j0, int j1, Matrix X) {
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

    /**
     * Set a submatrix.
     * @param r    Array of row indices.
     * @param c    Array of column indices.
     * @param X    A(r(:),c(:))
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int[] r, int[] c, Matrix X) {
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

    /**
     * Set a submatrix.
     * @param r    Array of row indices.
     * @param j0   Initial column index
     * @param j1   Final column index
     * @param X    A(r(:),j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int[] r, int j0, int j1, Matrix X) {
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

    /**
     * Set a submatrix.
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c Array of column indices.
     * @param X A(i0:i1,c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int i0, int i1, int[] c, Matrix X) {
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

    /**
    * Check if a matrix is all zeros.
    * @return true if all zeros, false otherwise
    */
    public boolean isZeroMatrix() {
        int m = getRowDimension(), n = getColumnDimension();
        boolean isZero = true;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (get(i, j) != 0) return false;
            }
        }
        return isZero;
    }

    /**
     * Matrix transpose.
     * @return    A'
     */
    public Matrix transpose() {
        Matrix X = new Matrix(n, m);
        int[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return X;
    }

    /**
     * Generate identity matrix]
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */
    public static Matrix identity(int m, int n) {
        Matrix A = new Matrix(m, n);
        int[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (i == j ? 1 : 0);
            }
        }
        return A;
    }

    /**
     * Print the matrix to stdout.   Line the elements up in columns
     * with a Fortran-like 'Fw.d' style format.
     * @param w    Column width.
     * @param d    Number of digits after the decimal.
     */
    public void print(int w, int d) {
        print(new PrintWriter(System.out, true), w, d);
    }

    /**
     * Print the matrix to the output stream.   Line the elements up in
     * columns with a Fortran-like 'Fw.d' style format.
     * @param output Output stream.
     * @param w Column width.
     * @param d Number of digits after the decimal.
     */
    public void print(PrintWriter output, int w, int d) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.UK));
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(d);
        format.setMinimumFractionDigits(d);
        format.setGroupingUsed(false);
        print(output, format, w + 2);
    }

    /**
     * Print the matrix to stdout.  Line the elements up in columns.
     * Use the format object, and right justify within columns of width
     * characters.
     * Note that if the matrix is to be read back in, you probably will want
     * to use a NumberFormat that is set to UK Locale.
     * @param format A Formatting object for individual elements.
     * @param width Field width for each column.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */
    public void print(NumberFormat format, int width) {
        print(new PrintWriter(System.out, true), format, width);
    }

    /**
     * Print the matrix to the output stream.  Line the elements up in columns.
     * Use the format object, and right justify within columns of width
     * characters.
     * Note that is the matrix is to be read back in, you probably will want
     * to use a NumberFormat that is set to US Locale.
     * @param output the output stream.
     * @param format A formatting object to format the matrix elements
     * @param width Column width.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
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

    /**
     * Throws IllegalArgumentException if dimensions of A and B differ.
     *  @param   B   The matrix to check the dimensions.
     */
    private void checkMatrixDimensions(Matrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

    /**
     * Used to display intermediate results for checking
     * @param a  The array of integers to print.
     */
    public void printArray(int[] a) {
        int n = a.length;
    }
}
