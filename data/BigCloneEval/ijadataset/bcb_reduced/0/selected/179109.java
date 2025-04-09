package jumbo.euclid;

import jumbo.xml.util.Util;

/**
<P>
 RealSquareMatrix - square matrix class
<P>
 RealSquareMatrix represents a square m-x-m  matrix.
 The basic matrix algebra for square matrices is represented here
 Check out the exciting member functions, which are supported by
 Exceptions where appropriate.  (NB.  No attempt has been made to 
 provide numerical robustness and inversion, diagonalisation, etc
 are as you find them.)
<P>
@author (C) P. Murray-Rust, 1996
*/
public class RealSquareMatrix extends RealMatrix {

    /** This gives a default matrix, with cols = rows = 0.
*/
    public RealSquareMatrix() {
        super();
    }

    /** This gives a null matrix
*/
    public RealSquareMatrix(int rows) {
        super(rows, rows);
    }

    /** special types of matrix (Outerproduct, Diagonal, etc)
*/
    public static RealSquareMatrix outerProduct(RealArray f) {
        int rows = f.size();
        RealSquareMatrix temp = new RealSquareMatrix(rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows; j++) {
                temp.flmat[i][j] = f.elementAt(i) * f.elementAt(j);
            }
        }
        return temp;
    }

    /** make diagonal matrix 
*/
    public static RealSquareMatrix diagonal(RealArray f) {
        int rows = f.size();
        RealSquareMatrix temp = new RealSquareMatrix(rows);
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
    public RealSquareMatrix(int rows, double[] array) throws BadArgumentException {
        super(rows, rows, array);
    }

    /** initalises all elements in the array with a given double[] 
*/
    public RealSquareMatrix(int rows, double f) {
        super(rows, rows, f);
    }

    /** submatrix of another matrix
@exception BadArgumentException lowrow, lowcol or rows are not consistent with size of <TT>m</TT>
*/
    public RealSquareMatrix(RealMatrix m, int lowrow, int lowcol, int rows) throws BadArgumentException {
        super(m, lowrow, lowrow + rows - 1, lowcol, lowcol + rows - 1);
    }

    public RealSquareMatrix(RealSquareMatrix m) {
        super(m);
    }

    /** assign a RealMatrix - i.e. NOT copied 
@exception NonSquareException <TT>m</TT> must be square (i.e. cols = rows)
*/
    public RealSquareMatrix(RealMatrix m) throws NonSquareException {
        super(m.rows, m.cols);
        if (m.cols != m.rows) {
            throw new NonSquareException();
        }
        this.flmat = m.flmat;
    }

    /** form from a Java 2-D array (it holds row and column count) 
@exception MatrixShapeException <TT>matrix</TT> is not square (might even not be rectangular!)
*/
    public RealSquareMatrix(double[][] matrix) throws MatrixShapeException {
        super(matrix);
        if (cols != rows) {
            throw new NonSquareException();
        }
    }

    /** shallowCopy an existing object 
@exception UnequalMatricesException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public void shallowCopy(RealSquareMatrix m) throws UnequalMatricesException {
        super.shallowCopy((RealMatrix) m);
    }

    /** are two matrices identical? 
@exception UnequalMatricesException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public boolean equals(RealSquareMatrix r) throws UnequalMatricesException {
        return super.equals((RealMatrix) r);
    }

    /** matrix addition - adds conformable matrices
@exception MatrixShapeException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public RealSquareMatrix plus(RealSquareMatrix m) throws MatrixShapeException {
        RealMatrix temp = super.plus((RealMatrix) m);
        RealSquareMatrix sqm = new RealSquareMatrix(temp);
        return sqm;
    }

    /** matrix subtraction - subtracts conformable matrices
@exception MatrixShapeException <TT>m</TT> must have the same number of rows and cols as <TT>this</TT>
*/
    public RealSquareMatrix subtract(RealSquareMatrix m) throws MatrixShapeException {
        RealMatrix temp = super.subtract((RealMatrix) m);
        RealSquareMatrix sqm = new RealSquareMatrix(temp);
        return sqm;
    }

    /** matrix multiplication - multiplies conformable matrices;
  result is <TT>this*m </TT>
@exception MatrixShapeException <TT>m</TT> must have the same number of rows as <TT>this</TT> has cols
*/
    public RealSquareMatrix multiply(RealSquareMatrix m) throws MatrixShapeException {
        RealMatrix temp = super.multiply((RealMatrix) m);
        RealSquareMatrix sqm = new RealSquareMatrix(temp);
        return sqm;
    }

    /** determinant - only goes up to order 3 at present :-( 
@exception UnimplementedException I have only written this for <TT>this.rows</TT> up to 3.  If anyone can find a determinant routine, this will disappear ... -(
*/
    public double determinant() throws UnimplementedException {
        double det = 0.0;
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
    public double trace() {
        double trace = 0.0;
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
                double f = flmat[i][j];
                if ((!Real.isZero(f) && (i != j)) || (!Real.isEqual(f, 1.0) && (i == j))) {
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
                if (!Real.isEqual(flmat[i][j], flmat[j][i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** orthonormalise matrix (only works for 3x3 at present)
@exception UnimplementedException I have only written this for <TT>this.rows</TT> up to 3.  If anyone can find a routine, this will disappear ... -(
*/
    public RealSquareMatrix orthonormalise() throws UnimplementedException {
        if (cols == 3) {
            Vector3 v0 = new Vector3(extractRowData(0));
            Vector3 v1 = new Vector3(extractRowData(1));
            Vector3 v2 = new Vector3(extractRowData(2));
            double det = v0.getScalarTripleProduct(v1, v2);
            v0.normalise();
            v2 = v0.cross(v1);
            v2.normalise();
            v1 = v2.cross(v0);
            if (det < 0.0) v2.negative();
            try {
                replaceRowData(0, v0.getArray());
                replaceRowData(1, v1.getArray());
                replaceRowData(2, v2.getArray());
            } catch (UnequalMatricesException e) {
                Util.bug(e);
            }
        } else {
            throw new UnimplementedException("Sorry: orthonormalise only up to 3x3 matrices");
        }
        return this;
    }

    /** is matrix unitary (orthonormal)? (synonym for isUnitary())
*/
    public boolean isOrthonormal() {
        return isUnitary();
    }

    /** is matrix UpperTriangular?
*/
    public boolean isUpperTriangular() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (!Real.isZero(flmat[i][j])) return false;
            }
        }
        return true;
    }

    /** is matrix lower triangular (including diagonal)?
*/
    public boolean isLowerTriangular() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (!Real.isZero(flmat[i][j])) return false;
            }
        }
        return true;
    }

    double rowDotproduct(int row1, int row2) {
        double sum = 0.0;
        for (int i = 0; i < cols; i++) {
            sum += flmat[row1][i] * flmat[row2][i];
        }
        return sum;
    }

    /** is matrix orthogonal? (rowwise calculation) 
*/
    public boolean isOrthogonal() {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (!Real.isZero(rowDotproduct(i, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** is matrix an improper rotation?
*/
    public boolean isImproperRotation() {
        double f;
        try {
            f = determinant();
        } catch (UnimplementedException e) {
            return false;
        }
        return (Real.isEqual(f, -1.0) && isOrthogonal());
    }

    public boolean isUnitary() {
        double f;
        try {
            f = determinant();
        } catch (UnimplementedException e) {
            return false;
        }
        double fa = Math.abs(f);
        return (Real.isEqual(fa, 1.0) && isOrthogonal());
    }

    /** copy upper triangle into lower triangle (i.e. make symmetric)
*/
    public RealSquareMatrix copyUpperToLower() {
        for (int i = 0; i < cols - 1; i++) {
            for (int j = i + 1; j < cols; j++) {
                flmat[j][i] = flmat[i][j];
            }
        }
        return this;
    }

    /** copy lower triangle into upper triangle (i.e. make symmetric)
*/
    public RealSquareMatrix copyLowerToUpper() {
        for (int i = 0; i < cols - 1; i++) {
            for (int j = i + 1; j < cols; j++) {
                flmat[i][j] = flmat[j][i];
            }
        }
        return this;
    }

    /** copy lower triangle into linear array; order: 0,0; 1,0; 1,1; 2,0 ..
*/
    public RealArray lowerTriangle() {
        int n = rows;
        RealArray triangle = new RealArray((n * (n + 1)) / 2);
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
                double t = flmat[i][j];
                flmat[i][j] = flmat[j][i];
                flmat[j][i] = t;
            }
        }
    }

    /** diagonalisation - returns eigenvalues and vectors as MODIFIED arguments;
 <TT>this </TT> is NOT affected 
<P>
Note that IllCondMatrixException is RETURNED and not thrown
@exception ArrayTooSmallException must have at least order 2
*/
    public int diagonaliseAndReturnRank(RealArray eigenvalues, RealSquareMatrix eigenvectors, IllCondMatrixException illCond) throws ArrayTooSmallException {
        RealArray lowert = this.lowerTriangle();
        double[] lower77 = new double[lowert.size() + 1];
        System.arraycopy(lowert.getArray(), 0, lower77, 1, lowert.size());
        int order = rows;
        if (rows < 2) {
            throw new ArrayTooSmallException();
        }
        double[] eigenval77 = new double[rows + 1];
        double[] eigenvect77 = new double[rows * rows + 1];
        illCond = null;
        int rank = Diagonalise.vneigl(order, lower77, eigenval77, eigenvect77, illCond);
        double[] eigenval = new double[rows];
        System.arraycopy(eigenval77, 1, eigenval, 0, rows);
        double[] eigenvect = new double[rows * rows];
        System.arraycopy(eigenvect77, 1, eigenvect, 0, rows * rows);
        eigenvalues.shallowCopy(new RealArray(eigenval));
        try {
            eigenvectors.shallowCopy(new RealSquareMatrix(rows, eigenvect));
        } catch (UnequalMatricesException e) {
        } catch (BadArgumentException e) {
            Util.bug(e);
        }
        return rank;
    }

    /** orthogonalise matrix (only works for 3x3 at present); MODIFIES
  matrix
@exception UnimplementedException I have only written this for <TT>this.rows</TT> up to 3.  If anyone can find a routine, this will disappear ... -(
*/
    public void orthogonalise() throws UnimplementedException {
        if (cols == 2) {
            Vector3 v0 = new Vector3(extractRowData(0));
            Vector3 v1 = new Vector3(extractRowData(1));
            Vector3 v2 = new Vector3(extractRowData(2));
            double l0 = v0.getLength();
            double l1 = v1.getLength();
            double l2 = v2.getLength();
            double det = v0.getScalarTripleProduct(v1, v2);
            v0.normalise();
            v2 = v0.cross(v1);
            v2.normalise();
            v1 = v2.cross(v0);
            if (det < 0.0) v2 = v2.negative();
            v0 = v0.multiplyBy(l0);
            v1 = v1.multiplyBy(l1);
            v2 = v2.multiplyBy(l2);
            try {
                replaceRowData(0, v0.getArray());
                replaceRowData(1, v1.getArray());
                replaceRowData(2, v2.getArray());
            } catch (UnequalMatricesException e) {
                Util.bug(e);
            }
        } else {
            throw new UnimplementedException("Sorry: orthogonalise only up to 3x3 matrices");
        }
    }

    /** craete orthogonlisation matrix from cell lengths and angles (in degrees)
	Rollett "Computing Methods in Crystallography" Pergamon 1965 p.23
	*/
    public static RealSquareMatrix getCrystallographicOrthogonalisation(double[] celleng, double[] angle) {
        RealSquareMatrix orthMat = new RealSquareMatrix(3);
        double dtor = Math.PI / 180.0;
        double sina = Math.sin(dtor * angle[0]);
        double cosa = Math.cos(dtor * angle[0]);
        double sinb = Math.sin(dtor * angle[1]);
        double cosb = Math.cos(dtor * angle[1]);
        double cosg = Math.cos(dtor * angle[2]);
        double cosgstar = (cosa * cosb - cosg) / (sina * sinb);
        double singstar = Math.sqrt(1.0 - cosgstar * cosgstar);
        orthMat.setElementAt(0, 0, celleng[0] * sinb * singstar);
        orthMat.setElementAt(0, 1, 0.0);
        orthMat.setElementAt(0, 2, 0.0);
        orthMat.setElementAt(1, 0, -celleng[0] * sinb * cosgstar);
        orthMat.setElementAt(1, 1, celleng[1] * sina);
        orthMat.setElementAt(1, 2, 0.0);
        orthMat.setElementAt(2, 0, celleng[0] * cosb);
        orthMat.setElementAt(2, 1, celleng[1] * cosa);
        orthMat.setElementAt(2, 2, celleng[2]);
        return orthMat;
    }

    /** inversion of matrix - creates NEW matrix 
@exception SingMatrixException singular matrix (or worse!)
*/
    public RealSquareMatrix getInverse() throws SingMatrixException {
        double[][] temp = getMatrix();
        double[][] inv = new double[rows][rows];
        matinv(temp, inv, cols);
        RealSquareMatrix temp1 = new RealSquareMatrix();
        try {
            temp1 = new RealSquareMatrix(inv);
        } catch (MatrixShapeException e) {
            Util.bug(e);
        }
        return temp1;
    }

    /** invert a square matrix
 from Hansen (The C++ answer Book - pp 114-5)
*/
    private boolean dopivot(double[][] A, double[][] I, int diag, int nelem) {
        if (A[diag][diag] != 0.0) return true;
        int i;
        for (i = diag + 1; i < nelem; i++) {
            if (A[i][diag] != 0.0) {
                double[] t;
                t = A[diag];
                A[diag] = A[i];
                A[i] = t;
                t = I[diag];
                I[diag] = I[i];
                I[i] = t;
                break;
            }
        }
        return i < nelem;
    }

    private void matinv(double[][] A, double[][] I, int nelem) throws SingMatrixException {
        for (int i = 0; i < nelem; i++) {
            for (int j = 0; j < nelem; j++) {
                I[i][j] = 0.0;
            }
            I[i][i] = 1.0;
        }
        for (int diag = 0; diag < nelem; diag++) {
            if (!dopivot(A, I, diag, nelem)) {
                throw new SingMatrixException();
            }
            double div = A[diag][diag];
            if (div != 1.0) {
                A[diag][diag] = 1.0;
                for (int j = diag + 1; j < nelem; j++) A[diag][j] /= div;
                for (int j = 0; j < nelem; j++) I[diag][j] /= div;
            }
            for (int i = 0; i < nelem; i++) {
                if (i == diag) continue;
                double sub = A[i][diag];
                if (sub != 0.0) {
                    A[i][diag] = 0.0;
                    for (int j = diag + 1; j < nelem; j++) A[i][j] -= sub * A[diag][j];
                    for (int j = 0; j < nelem; j++) I[i][j] -= sub * I[diag][j];
                }
            }
        }
    }

    /**tests RealSquareMatrix routines
*/
    public static void main(String args[]) {
        RealSquareMatrix m0 = null;
        RealSquareMatrix m1 = null;
        RealSquareMatrix m2 = null;
        RealSquareMatrix m3 = null;
        RealSquareMatrix m4 = null;
        RealSquareMatrix m5 = null;
        RealSquareMatrix m6 = null;
        RealSquareMatrix m7 = null;
        RealSquareMatrix m8 = null;
        RealSquareMatrix m9 = null;
        RealSquareMatrix m10 = null;
        RealSquareMatrix m11 = null;
        RealSquareMatrix m12 = null;
        RealSquareMatrix m13 = null;
        RealSquareMatrix m14 = null;
        RealSquareMatrix m20 = null;
        RealSquareMatrix m21 = null;
        RealSquareMatrix iv = null;
        RealSquareMatrix tempm = null;
        RealSquareMatrix trp = null;
        RealSquareMatrix xz = null;
        System.out.println("---------Testing RealSquareMatrix---------\n");
        int i, j;
        boolean b;
        System.out.println("................................................\n");
        m0 = new RealSquareMatrix();
        System.out.println("m0: " + m0 + "\n");
        System.out.println("................................................\n");
        m2 = new RealSquareMatrix(4);
        System.out.println("m2: " + m2 + "\n");
        System.out.println("................................................\n");
        b = m2.isUnit();
        System.out.println(" isUnit: " + b + "\n");
        System.out.println("................................................\n");
        double[] temp3 = { 1., 2., 3., 4. };
        RealArray fa = new RealArray(4, temp3);
        m20 = RealSquareMatrix.diagonal(fa);
        System.out.println("m20: " + m20 + "\n");
        System.out.println("................................................\n");
        m21 = RealSquareMatrix.outerProduct(fa);
        System.out.println("m21: " + m21 + "\n");
        System.out.println("................................................\n");
        double temp4[] = { 1., 2., 3., 4. };
        double temp5[] = { -1, -2, 1., 3. };
        RealSquareMatrix sb = null;
        RealSquareMatrix sc = null;
        try {
            sb = new RealSquareMatrix(2, temp4);
            sc = new RealSquareMatrix(2, temp5);
        } catch (BadArgumentException e) {
            Util.bug(e);
        }
        try {
            System.out.println(" sb, sc, sb*sc " + (sb.plus(sc)) + " " + sb.multiply(sc));
            System.out.println("................................................\n");
        } catch (MatrixShapeException e) {
            Util.bug(e);
        }
        double[][] mat;
        double[][] temp;
        temp = mat = new double[4][4];
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                temp[i][j] = 10 * i + j;
            }
        }
        try {
            m3 = new RealSquareMatrix(mat);
            System.out.println("m3: " + m3 + "\n");
            System.out.println("................................................\n");
            m4 = new RealSquareMatrix(m3);
            System.out.println("m4: " + m4 + "\n");
            System.out.println("................................................\n");
            m12 = new RealSquareMatrix(m3);
            System.out.println("m12:" + m12 + "\n");
            System.out.println("................................................\n");
            m11 = new RealSquareMatrix(m3);
            System.out.println("m11:" + m11 + "\n");
        } catch (MatrixShapeException e) {
            Util.bug(e);
        }
        System.out.println("................................................\n");
        double f = m4.elementAt(2, 3);
        System.out.println(" f: " + f + "\n");
        System.out.println("................................................\n");
        RealSquareMatrix zz = new RealSquareMatrix(m4);
        zz.setElementAt(0, 0, 100.);
        zz.setElementAt(1, 1, 3.0);
        zz.setElementAt(2, 2, 51.0);
        zz.setElementAt(3, 3, 22.0);
        System.out.println("zz: " + zz + "\n");
        System.out.println("................................................\n");
        try {
            iv = zz.getInverse();
        } catch (SingMatrixException e) {
            Util.bug(e);
        }
        System.out.println("iv: " + iv + "\n");
        System.out.println("................................................\n");
        try {
            xz = iv.multiply(zz);
        } catch (MatrixShapeException e) {
            Util.bug(e);
        }
        System.out.println("xz: " + xz + "\n");
        System.out.println("................................................\n");
        f = m4.trace();
        System.out.println(" f: " + f + "\n");
        System.out.println("................................................\n");
        b = m4.isUpperTriangular();
        System.out.println(" isUpperTriangular: " + b + "\n");
        System.out.println("................................................\n");
        b = m4.isLowerTriangular();
        System.out.println(" isLowerTriangular: " + b + "\n");
        System.out.println("................................................\n");
        b = m4.isSymmetric();
        System.out.println(" isSymmetric: " + b + "\n");
        System.out.println("................................................\n");
        System.out.println("................................................\n");
        double[] xx = { 10.0, 2.0, 3.0, 2.0, 12.0, 3.0, 3.0, 3.0, 16.0 };
        try {
            m14 = new RealSquareMatrix(3, xx);
        } catch (BadArgumentException e) {
            Util.bug(e);
        }
        System.out.println("diagonalising :" + m14 + "\n");
        RealSquareMatrix evect = new RealSquareMatrix(3);
        RealArray eval = new RealArray(3);
        IllCondMatrixException illCond = null;
        int rank = 0;
        try {
            rank = m14.diagonaliseAndReturnRank(eval, evect, illCond);
        } catch (ArrayTooSmallException e) {
            Util.bug(e);
        }
        System.out.println("Eigenvalues :" + eval + "\n");
        System.out.println("Eigenvectors :" + evect + "\n");
        System.out.println("rank:" + rank + "\n");
        if (illCond != null) {
            System.out.println("Diagonalisation warning: " + illCond);
        }
        for (i = 0; i < 3; i++) {
            eval.setElementAt(i, Math.sqrt(eval.elementAt(i)));
        }
        System.out.println("sqrteval :" + eval + "\n");
        System.out.println("checking evect(T) * eval**(1/2) * (eval**(1/2)  * evect) \n");
        RealSquareMatrix diag = RealSquareMatrix.diagonal(eval);
        try {
            tempm = diag.multiply(evect);
            System.out.println("Axes :" + tempm);
            trp = new RealSquareMatrix(tempm);
            trp.transpose();
            tempm = trp.multiply(tempm);
        } catch (MatrixShapeException e) {
            Util.bug(e);
        }
        System.out.println("Axes(T)*axes :" + tempm);
        System.out.println("................................................\n");
        System.out.println("................................................\n");
        System.out.println("m4: " + m4 + "\n");
        try {
            m9 = new RealSquareMatrix(m4, 0, 1, 3);
        } catch (BadArgumentException e) {
            Util.bug(e);
        }
        System.out.println("m9: " + m9 + "\n");
        System.out.println("................................................\n");
        m10 = new RealSquareMatrix(m9);
        System.out.println("m10:" + m10 + "\n");
        System.out.println("................................................\n");
        f = m9.trace();
        System.out.println(" f: " + f + "\n");
        System.out.println("................................................\n");
        m9.setElementAt(1, 0, 0.0);
        m9.setElementAt(2, 0, 0.0);
        m9.setElementAt(2, 1, 0.0);
        System.out.println(m9);
        System.out.println("................................................\n");
        b = m9.isUpperTriangular();
        System.out.println(" isUpperTriangular: " + b + "\n");
        System.out.println("................................................\n");
        m9.copyUpperToLower();
        System.out.println("m9: " + m9 + "\n");
        System.out.println("................................................\n");
        System.out.println("testing : " + m9);
        System.out.println("................................................\n");
        b = m9.isSymmetric();
        System.out.println(" isSymmetric: " + b + "\n");
        System.out.println("................................................\n");
        try {
            f = m9.determinant();
        } catch (UnimplementedException e) {
            Util.bug(e);
        }
        System.out.println(" determinant: " + f + "\n");
        System.out.println("................................................\n");
        m9 = m10;
        m9.setElementAt(0, 1, 0.0);
        m9.setElementAt(0, 2, 0.0);
        m9.setElementAt(1, 2, 0.0);
        b = m9.isLowerTriangular();
        System.out.println(" isLowerTriangular: " + b + "\n");
        System.out.println("................................................\n");
        m9 = m10;
        m4.setElementAt(2, 3, 67.0);
        int rows = m4.getRows();
        int cols = m4.getCols();
        System.out.println("................................................\n");
        RealArray r2 = m4.extractRowData(1);
        System.out.println("r2:\n" + r2 + "\n");
        System.out.println("................................................\n");
        RealArray c3 = m4.extractColumnData(2);
        System.out.println("c3:\n" + c3 + "\n");
        System.out.println("................................................\n");
        f = m4.elementAt(2, 3);
        System.out.println(" f: " + f + "\n");
        System.out.println("................................................\n");
        try {
            b = (m3.equals(m4));
            System.out.println(" m3.equals(m4): " + b + "\n");
            System.out.println("................................................\n");
            m3.negative();
            System.out.println("m3: " + m3 + "\n");
            System.out.println("................................................\n");
            m3.multiplyBy(-1.01);
            System.out.println("m3: " + m3 + "\n");
            System.out.println("................................................\n");
            m8 = m3.plus(m4);
            System.out.println("m8: " + m8 + "\n");
            System.out.println("................................................\n");
            m3 = m3.plus(m4);
            System.out.println("m3: " + m3 + "\n");
            System.out.println("................................................\n");
            m3 = m3.subtract(m4);
            System.out.println("m3: " + m3 + "\n");
            System.out.println("................................................\n");
            m2 = new RealSquareMatrix(m3);
            m2.transpose();
            System.out.println("m2: " + m2 + "\n");
            System.out.println("................................................\n");
            m4.clearMatrix();
            System.out.println("m4: " + m4 + "\n");
            System.out.println("................................................\n");
            m4.setAllElements(23.);
            System.out.println("m4: " + m4 + "\n");
            System.out.println("................................................\n");
            m7 = m2.multiply(m3);
            System.out.println("m7: " + m7 + "\n");
            System.out.println("................................................\n");
            m7 = m3.multiply(m2);
        } catch (MatrixShapeException e) {
            Util.bug(e);
        }
        System.out.println("m7: " + m7 + "\n");
        System.out.println("................................................\n");
        b = m7.isOrthogonal();
        System.out.println(" isOrthogonal: " + b + "\n");
        System.out.println("................................................\n");
        double[] t = { 1.0, 2., 3., -3., 0., 1., 2., -10., 6. };
        try {
            m13 = new RealSquareMatrix(3, t);
        } catch (BadArgumentException e) {
            Util.bug(e);
        }
        System.out.println(m13);
        System.out.println("................................................\n");
        b = m13.isOrthogonal();
        System.out.println(" isOrthogonal: " + b + "\n");
        System.out.println("................................................\n");
    }
}

class RealSquareMatrixType {

    public static final int UPPER_TRIANGLE = 1;

    public static final int LOWER_TRIANGLE = 2;

    public static final int SYMMETRIC = 3;

    public static final int DIAGONAL = 4;

    public static final int OUTER_PRODUCT = 5;

    public static final int UNKNOWN = 6;
}
