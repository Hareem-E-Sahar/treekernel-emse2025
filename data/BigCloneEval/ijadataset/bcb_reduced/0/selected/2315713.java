package jumbo.euclid;

/**
<P>
 IntSquareMatrix - square matrix class
<P>
 IntSquareMatrix represents a square m-x-m  matrix.
 The basic matrix algebra for square matrices is represented here
 Check out the exciting member functions, which are supported by
 Exceptions where appropriate.  (NB.  No attempt has been made to 
 provide numerical robustness and inversion, diagonalisation, etc
 are as you find them.)
<P>
@author (C) P. Murray-Rust, 1996
*/
public class IntSquareMatrix extends IntMatrix {

    /** This gives a default matrix, with cols = rows = 0.
*/
    public IntSquareMatrix() {
        super();
    }

    /** This gives a null matrix
*/
    public IntSquareMatrix(int rows) {
        super(rows, rows);
    }

    /** special types of matrix (Outerproduct, Diagonal, etc)
*/
    public static IntSquareMatrix outerProduct(IntArray f) {
        int rows = f.size();
        IntSquareMatrix temp = new IntSquareMatrix(rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows; j++) {
                temp.flmat[i][j] = f.elementAt(i) * f.elementAt(j);
            }
        }
        return temp;
    }

    /** make diagonal matrix 
*/
    public static IntSquareMatrix diagonal(IntArray f) {
        int rows = f.size();
        IntSquareMatrix temp = new IntSquareMatrix(rows);
        for (int i = 0; i < rows; i++) {
            temp.flmat[i][i] = f.elementAt(i);
        }
        return temp;
    }

    /** Formed by feeding in an existing array to a colsXcols matrix. 
THE COLUMN IS THE FASTEST MOVING INDEX, i.e. the matrix is filled
as mat(0,0), mat(0,1) ... C-LIKE
@exception BadArgumentException <TT>array</TT> size must be multiple of <TT>rows</TT>
*/
    public IntSquareMatrix(int rows, int[] array) throws BadArgumentException {
        super(rows, rows, array);
    }

    /** initalises all elements in the array with a given int[] 
*/
    public IntSquareMatrix(int rows, int f) {
        super(rows, rows, f);
    }

    /** submatrix of another matrix
@exception BadArgumentException lowrow, lowcol or rows are not consistent with size of <TT>m</TT>
*/
    public IntSquareMatrix(IntMatrix m, int lowrow, int lowcol, int rows) throws BadArgumentException {
        super(m, lowrow, lowrow + rows - 1, lowcol, lowcol + rows - 1);
    }

    public IntSquareMatrix(IntSquareMatrix m) {
        super(m);
    }

    /** assign a IntMatrix - i.e. NOT copied 
@exception NonSquareException <TT>m</TT> must be square (i.e. cols = rows)
*/
    public IntSquareMatrix(IntMatrix m) throws NonSquareException {
        super(m.rows, m.cols);
        if (m.cols != m.rows) {
            throw new NonSquareException();
        }
        this.flmat = m.flmat;
    }

    /** form from a Java 2-D array (it holds row and column count) 
@exception MatrixShapeException <TT>matrix</TT> is not square (might even not be rectangular!)
*/
    public IntSquareMatrix(int[][] matrix) throws MatrixShapeException {
        super(matrix);
        if (cols != rows) {
            throw new NonSquareException();
        }
    }

    /** shallowCopy an existing object 
@exception UnequalMatricesException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public void shallowCopy(IntSquareMatrix m) throws UnequalMatricesException {
        super.shallowCopy((IntMatrix) m);
    }

    /** are two matrices identical? 
@exception UnequalMatricesException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public boolean equals(IntSquareMatrix r) throws UnequalMatricesException {
        return super.equals((IntMatrix) r);
    }

    /** matrix addition - adds conformable matrices
@exception MatrixShapeException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public IntSquareMatrix plus(IntSquareMatrix m) throws MatrixShapeException {
        IntMatrix temp = super.plus((IntMatrix) m);
        IntSquareMatrix sqm = new IntSquareMatrix(temp);
        return sqm;
    }

    /** matrix subtraction - subtracts conformable matrices
@exception MatrixShapeException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public IntSquareMatrix subtract(IntSquareMatrix m) throws MatrixShapeException {
        IntMatrix temp = super.subtract((IntMatrix) m);
        IntSquareMatrix sqm = new IntSquareMatrix(temp);
        return sqm;
    }

    /** matrix multiplication - multiplies conformable matrices;
  result is <TT>this*m </TT>
@exception MatrixShapeException <TT>m</TT> must have the same number of rows as <TT>this</TT> has cols
*/
    public IntSquareMatrix multiply(IntSquareMatrix m) throws MatrixShapeException {
        IntMatrix temp = super.multiply((IntMatrix) m);
        IntSquareMatrix sqm = new IntSquareMatrix(temp);
        return sqm;
    }

    /** determinant - only goes up to order 3 at present :-( 
@exception UnimplementedException I have only written this for <TT>this.rows</TT> up to 3.  If anyone can find a determinant routine, this will disappear ... -(
*/
    public int determinant() throws UnimplementedException {
        int det = 0;
        if (rows == 1) {
            det = flmat[0][0];
        } else if (rows == 2) {
            det = flmat[0][0] * flmat[1][1] - flmat[1][0] * flmat[0][1];
        } else if (rows == 3) {
            det = flmat[0][0] * (flmat[1][1] * flmat[2][2] - flmat[1][2] * flmat[2][1]) + flmat[0][1] * (flmat[1][2] * flmat[2][0] - flmat[1][0] * flmat[2][2]) + flmat[0][2] * (flmat[1][0] * flmat[2][1] - flmat[1][1] * flmat[2][0]);
        } else {
            throw new UnimplementedException("Sorry; determinants only up to 3x3 matrices");
        }
        return det;
    }

    /** trace
*/
    public int trace() {
        int trace = 0;
        for (int i = 0; i < rows; i++) {
            trace += flmat[i][i];
        }
        return trace;
    }

    /** is it a unit matrix?
*/
    public boolean isUnit() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows; j++) {
                int f = flmat[i][j];
                if ((f != 0 && (i != j)) || (f != 1 && (i == j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** is matrix symmetric?
*/
    public boolean isSymmetric() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (flmat[i][j] != flmat[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** is matrix UpperTriangular?
*/
    public boolean isUpperTriangular() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (flmat[i][j] != 0) return false;
            }
        }
        return true;
    }

    /** is matrix lower triangular (including diagonal)?
*/
    public boolean isLowerTriangular() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (flmat[i][j] != 0) return false;
            }
        }
        return true;
    }

    private int rowDotproduct(int row1, int row2) {
        int sum = 0;
        for (int i = 0; i < cols; i++) {
            sum += flmat[row1][i] * flmat[row2][i];
        }
        return sum;
    }

    /** copy upper triangle into lower triangle (i.e. make symmetric)
*/
    public IntSquareMatrix copyUpperToLower() {
        for (int i = 0; i < cols - 1; i++) {
            for (int j = i + 1; j < cols; j++) {
                flmat[j][i] = flmat[i][j];
            }
        }
        return this;
    }

    /** copy lower triangle into upper triangle (i.e. make symmetric)
*/
    public IntSquareMatrix copyLowerToUpper() {
        for (int i = 0; i < cols - 1; i++) {
            for (int j = i + 1; j < cols; j++) {
                flmat[i][j] = flmat[j][i];
            }
        }
        return this;
    }

    /** copy lower triangle into linear array; order: 0,0; 1,0; 1,1; 2,0 ..
*/
    public IntArray lowerTriangle() {
        int n = rows;
        IntArray triangle = new IntArray((n * (n + 1)) / 2);
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <= i; j++) {
                triangle.setElementAt(count++, flmat[i][j]);
            }
        }
        return triangle;
    }

    /** transpose - MODIFIES matrix 
*/
    public void transpose() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < i; j++) {
                int t = flmat[i][j];
                flmat[i][j] = flmat[j][i];
                flmat[j][i] = t;
            }
        }
    }
}
