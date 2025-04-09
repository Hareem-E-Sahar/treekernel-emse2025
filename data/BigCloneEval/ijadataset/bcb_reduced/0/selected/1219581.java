package esra.math;

import contrib.PrintfFormat;

/**
 * Provides basic linear algebra routines such as vector additions,
 * multiplications, inner, outer products, basic matrix operations such
 * as transposition, inversion etc.
 *
 * @version 0.1, April 2005
 * @author  Vincent Kraeutler and Mika Kastenholz
 */
public class BLA {

    public static final double TOLERANCE = 1.0E-7;

    /**
	 * @param lower limit of the range (inclusive)
	 * @param upper limit of the range (exclusive)
	 * @return the range of integers [lower, upper)
	 * @throws IllegalArgumentException
	 */
    public static int[] range(final int lower, final int upper) throws IllegalArgumentException {
        if (upper <= 0) throw new IllegalArgumentException("Upper limit must be a natural number.\n");
        if (lower < 0) throw new IllegalArgumentException("Lower limit must be a natural number or zero.\n");
        if (lower > upper) throw new IllegalArgumentException("Lower limit must be smaller than upper limit.\n" + "Lower: " + lower + "\n" + "Upper: " + upper + "\n");
        final int[] aa = new int[upper - lower];
        for (int jj = lower; jj < upper; jj++) aa[jj - lower] = jj;
        return aa;
    }

    /**
	 * @param j the upper limit of the range (exclusive).
	 * @return the range [0, j)
	 */
    public static int[] range(final int j) {
        return range(0, j);
    }

    /**
	 * concatenates two int[]
	 * 
	 * @param aa the first one
	 * @param bb	the second one
	 * @return the concatenated one
	 */
    public static int[] append(final int[] aa, final int[] bb) {
        final int[] cc = new int[aa.length + bb.length];
        for (int ii = 0; ii < aa.length; ii++) cc[ii] = aa[ii];
        for (int ii = 0; ii < bb.length; ii++) cc[ii + aa.length] = bb[ii];
        return cc;
    }

    /**
	 * appends an int at the end of an int[].
	 * evil!! evil!! but handy!!
	 * 
	 * @param aa the array
	 * @param bb	the int to be appended
	 * @return the concatenated one
	 */
    public static int[] append(final int[] aa, final int bb) {
        final int[] cc = new int[aa.length + 1];
        for (int ii = 0; ii < aa.length; ii++) cc[ii] = aa[ii];
        cc[aa.length + 1] = bb;
        return cc;
    }

    /**
	 * return the unique elements of the (sorted) array. 
	 * 
	 * @param aa the array
	 * @return the concatenated one
	 */
    public static int[] unique(final int[] aa) {
        final int[] cc = new int[aa.length];
        int jj = 0;
        for (int ii = 0; ii < aa.length; ii++) {
            cc[ii] = aa[ii];
            while (jj++ < aa.length && aa[jj] == cc[ii]) {
            }
        }
        final int[] dd = new int[jj];
        for (int ii = 0; ii < jj; ii++) dd[ii] = cc[ii];
        return dd;
    }

    /**
	 * initialize a vector of length ii with values value
	 * 
	 * @param ii
	 * @param value
	 * @return the new vector {value, value, ..., value}
	 */
    public static int[] same(final int ii, final int value) {
        int[] aa = new int[ii];
        for (int jj = 0; jj < ii; jj++) aa[jj] = value;
        return aa;
    }

    /**
	 * initialize a vector of length ii with values value
	 * 
	 * @param ii
	 * @param value
	 * @return the new vector {value, value, value, ..., value}
	 */
    public static double[] same(final int ii, final double value) {
        double[] aa = new double[ii];
        for (int jj = 0; jj < ii; jj++) aa[jj] = value;
        return aa;
    }

    /**
	 * @param ii the size of the vector
	 * @return a zero-vector (double[] )of the appropriate size
	 * @throws IllegalArgumentException
	 */
    public static double[] zeroes(final int ii) throws IllegalArgumentException {
        return same(ii, 0.0);
    }

    /**
	 * {x1, x2, -breakiterator , xn} -> {x(n - m), x(n - m + 1), breakiterator , x1, x2, breakiterator , x(n - m - 1)}
	 * @param vector
	 * @param offset
	 * @return the permuted vector
	 */
    public static double[] circularPermutation(final double[] vector, final int offset) {
        final int ii = vector.length;
        double[] aa = new double[ii];
        int jj, kk = 0;
        for (jj = 0; jj < ii - offset; jj++) aa[jj] = vector[jj + offset];
        for (kk = jj; kk < ii; kk++) aa[jj] = vector[kk - jj];
        return aa;
    }

    /**
	 * @param aa
	 * @param bb
	 * @return true if the vectors have the same length and identical elements (to within TOLERANCE)
	 * 
	 * @see BLA#TOLERANCE
	 */
    public static boolean equals(double[] aa, double[] bb) {
        if (aa.length != bb.length) return false;
        for (int ii = 0; ii < aa.length; ii++) if (Math.abs(aa[ii] - bb[ii]) > TOLERANCE) return false;
        return true;
    }

    /**
	 * add two vectors.
	 * 
	 * @param aa
	 * @param bb
	 * @return aa + bb
	 * @throws IllegalArgumentException
	 */
    public static double[] add(final double aa[], final double bb[]) {
        return add(aa, bb, new double[aa.length]);
    }

    /**
	 * add two vectors.
	 * 
	 * @param aa
	 * @param bb
	 * @return aa + bb
	 */
    public static double[] add(final double aa[], final double bb[], final double cc[]) {
        if (aa.length != bb.length) throw new RuntimeException("Vectors must have same length.\n");
        for (int ii = 0; ii < aa.length; ii++) {
            cc[ii] = aa[ii] + bb[ii];
        }
        return cc;
    }

    /**
	 * 
	 * subtract two vectors (bb - aa).
	 * 
	 * @param aa
	 * @param bb
	 * @return the difference of the two vectors (bb - aa)
	 * @throws IllegalArgumentException
	 */
    public static double[] subtract(final double[] aa, final double[] bb) {
        final double[] cc = new double[aa.length];
        return subtract(aa, bb, cc);
    }

    /**
	 * 
	 * subtract two vectors (bb - aa).
	 * 
	 * @param aa
	 * @param bb
	 * @return the difference of the two vectors (bb - aa)
	 * @throws IllegalArgumentException
	 */
    public static double[] subtract(final double[] aa, final double[] bb, final double[] cc) {
        if (aa.length != bb.length) throw new RuntimeException("Vectors must have same length.\n");
        for (int ii = 0; ii < aa.length; ii++) {
            cc[ii] = bb[ii] - aa[ii];
        }
        return cc;
    }

    /**
	 * element-wise multiplication of two vectors.
	 * {aa1, aa2, ...} * {ww1, ww2, ...} = {aa1 * ww1, aa2 * ww2, ...}
	 * @param aa
	 * @param weights
	 * @return {aa1 * ww1, aa2 * ww2, ...}
	 * @throws IllegalArgumentException
	 */
    public static double[] multiplyElements(final double aa[], final double weights[], final double[] result) {
        if (aa.length != weights.length || result.length != aa.length) throw new RuntimeException("Vectors must have the same length.\n");
        for (int ii = 0; ii < aa.length; ii++) {
            result[ii] = aa[ii] * weights[ii];
        }
        return result;
    }

    /**
	 * multiply all elements of a vector by some fixed number.
	 * cc {aa1, aa2, ...} = {cc * aa1, cc * aa2, ...}
	 * @param aa
	 * @param cc
	 * @return the new vector aa * cc
	 */
    public static double[] scale(final double aa[], final double cc) {
        double[] bb = zeroes(aa.length);
        return scale(aa, cc, bb);
    }

    /**
	 * multiply all elements of a vector by some fixed number.
	 * cc {aa1, aa2, ...} = {cc * aa1, cc * aa2, ...}
	 * @param aa
	 * @param cc
	 * @param bb overwritten with cc * aa
	 * @return bb
	 */
    public static double[] scale(final double aa[], final double cc, final double bb[]) {
        for (int ii = 0; ii < aa.length; ii++) {
            bb[ii] = cc * aa[ii];
        }
        return bb;
    }

    /**
	 * @param aa
	 * @return a (inefficient) copy of aa
	 */
    public static double[] copy(final double[] aa) {
        return BLA.scale(aa, 1.0);
    }

    /**
	 * copy the contents of src to dest
	 * 
	 * @param src
	 * @param dest
	 * @return dest
	 */
    public static double[] copyOver(final double[] src, final double[] dest) {
        for (int ii = 0; ii < src.length; ii++) dest[ii] = src[ii];
        return dest;
    }

    public static double[] addProduct(final double[] aa, final double[] bb, final double[] result) {
        for (int ii = 0; ii < aa.length; ii++) result[ii] += aa[ii] * bb[ii];
        return result;
    }

    /**
	 * {aa1, aa2, -breakiterator} -> { 1 / aa1, 1 / aa2, ...}
	 * @param aa
	 * @return {1 / aa1, 1 / aa2, ...}
	 */
    public static double[] invert(final double aa[]) {
        double[] bb = zeroes(aa.length);
        for (int ii = 0; ii < aa.length; ii++) {
            bb[ii] = 1 / aa[ii];
        }
        return bb;
    }

    /**
	 * Add a N-vector change to each row of the M x N - Matrix
	 * positions.
	 * 
	 * @param positions
	 * @param change
	 * @return a new vector positions + change
	 */
    public static double[][] shift(final double[][] positions, final double[] change) {
        double[][] newPositions = new double[positions.length][positions[0].length];
        for (int ii = 0; ii < positions.length; ii++) newPositions[ii] = add(positions[ii], change);
        return newPositions;
    }

    /**
	 * the cos(angle) between two vectors.
	 * 
	 * @param a
	 * @param b
	 * @return cos(angle)
	 */
    public static double cosab(final double a[], final double b[]) {
        return dot(a, b) / (Math.abs(norm(a)) * Math.abs(norm(b)));
    }

    /**
	 * the (minimum) angle between two vectors.
	 * 
	 * remark:
	 * typically, the norm of a vector is just a tiny bit
	 * bigger than one due to roundoff errors. in this case
	 * (i.e. when determining the angle between a vector and
	 * itself), the current java implementation of Math.acos
	 * returns NaN. We try to handle that gracefully, by setting
	 * cosines which are in the range (1, 1 + eps) to 1 before
	 * performing the acos operation.
	 * 
	 * @param i
	 * @param j
	 * @return the angle (radians)
	 */
    public static final double angle(double i[], double j[]) {
        double cab = cosab(i, j);
        final double acab = Math.abs(cab);
        if (acab > 1.0) {
            if (acab < 1 + TOLERANCE) {
                if (cab > 0) cab = 1; else cab = -1;
            } else {
                throw new RuntimeException("cosab(i, j) is larger than 1 + TOLERANCE.\n" + "Math.acos can't be performed.\n" + "Actually, this shouldn't be possible.");
            }
        }
        return Math.acos(cab);
    }

    /**
	 * the distance between two vectors.
	 * 
	 * @param i
	 * @param j
	 * @return the distance
	 */
    public static final double distance(double i[], double j[]) {
        return BLA.norm(BLA.subtract(i, j));
    }

    /**
	 * the dot product of two vectors
	 * 
	 * @param r1
	 * @param r2
	 * @return r1 . r2
	 * @throws IllegalArgumentException
	 */
    public static final double dot(final double r1[], final double r2[]) throws IllegalArgumentException {
        if (r1.length != r2.length) throw new IllegalArgumentException("Vectors must have the same length." + "Found: " + r1.length + " " + r2.length);
        double dd = 0.0;
        for (int ii = 0; ii < r1.length; ii++) dd += r1[ii] * r2[ii];
        return dd;
    }

    /**
	 * the squared euclidean norm of a vector
	 * @param r
	 * @return dot(r, r)
	 */
    public static final double norm2(double r[]) {
        return dot(r, r);
    }

    /**
	 * the euclidean norm of a vector
	 * @param r
	 * @return Math.sqrt(norm2(r))
	 */
    public static final double norm(double r[]) {
        return Math.sqrt(norm2(r));
    }

    /**
	 * normalize a vector by scaling it by its inverse norm.
	 * if the vector is the origin (i.e. the zero-vector),
	 * a copy of the origin is returned.
	 * 
	 * @param r
	 * @return the normalized vector
	 */
    public static final double[] normalize(final double r[]) {
        final double nn = norm(r);
        if (nn != 0) return scale(r, 1 / norm(r)); else return zeroes(r.length);
    }

    /**
	 * Compute the outer product of two vectors r1, r2.
	 * The vectors must have the same length.
	 * @param  r1 Vector 
	 * @param  r2 Vector 
	 * @return double[r1.length][r1.length], the outer product matrix
	 */
    public static final double[][] outer(double r1[], double r2[]) throws IllegalArgumentException {
        if (r1.length != r2.length) throw new IllegalArgumentException("Vectors must have the same length");
        double[][] outerMatrix = new double[r1.length][r1.length];
        for (int ii = 0; ii < r1.length; ii++) outerMatrix[ii] = scale(r1, r2[ii]);
        return outerMatrix;
    }

    /**
	 * the cross product of two 3-vectors. <BR>
	 * 
	 * maybe replace this with mathml?
	 * [\mathord{\buildrel{\lower3pt\hbox{$\scriptscriptstyle\rightharpoonup$}} 
	 * \over r} _{cross_{ij} }  = \mathord{\buildrel{\lower3pt\hbox{$\scriptscriptstyle\rightharpoonup$}} 
	 * \over r} _i  \times \mathord{\buildrel{\lower3pt\hbox{$\scriptscriptstyle\rightharpoonup$}} 
	 * \over r} _j
	 * 
	 * @param  r1 Vector (double[3]) 1 for the cross product calculation
	 * @param  r2 Vector (double[3]) 2 for the cross product calculation
	 * @return double[3], the cross product vector
	 */
    public static final double[] cross(final double r1[], final double r2[]) {
        if (r1.length != 3 || r2.length != 3) throw new IllegalArgumentException("Vectors must have length 3.\n");
        double cross[] = new double[3];
        cross[0] = r1[1] * r2[2] - r1[2] * r2[1];
        cross[1] = r1[2] * r2[0] - r1[0] * r2[2];
        cross[2] = r1[0] * r2[1] - r1[1] * r2[0];
        return cross;
    }

    public static final int[][] combinations(final int[] aa, final int[] bb) {
        int[][] comb = new int[aa.length * bb.length][2];
        int kk = 0;
        for (int ii = 0; ii < aa.length; ii++) {
            for (int jj = 0; jj < bb.length; jj++) {
                comb[kk][0] = aa[ii];
                comb[kk][1] = bb[jj];
                kk++;
            }
        }
        return comb;
    }

    /**
	 * returns a selected part of the array
	 * 
	 * @param array
	 * @param selection
	 * @return the new array
	 */
    public static final int[] select(final int[] array, final int[] selection) {
        if (selection.length > array.length) {
            throw new IllegalArgumentException("Selection must be smaller than original.");
        }
        final int[] out = new int[selection.length];
        for (int ii = 0; ii < selection.length; ii++) {
            out[ii] = array[selection[ii]];
        }
        return out;
    }

    /**
	 * returns a selected part of the array
	 * 
	 * @param array
	 * @param selection
	 * @return the new array
	 * 
	 * @see BLA#select(int[], int[])
	 * @see BLA#select(double[][], int[])
	 */
    public static final double[] select(final double[] array, final int[] selection) {
        if (selection.length > array.length) {
            throw new IllegalArgumentException("Selection must be smaller than original.");
        }
        final double[] out = new double[selection.length];
        for (int ii = 0; ii < selection.length; ii++) {
            out[ii] = array[selection[ii]];
        }
        return out;
    }

    /**
	 * @param r
	 * @return the sum of elements contained in the vector
	 */
    public static final double sum(final double r[]) {
        double ss = 0.0;
        for (int ii = 0; ii < r.length; ii++) ss += r[ii];
        return ss;
    }

    /**
	 * @param ii
	 * @param jj
	 * @return	a zero-filled matrix of size ii x jj
	 */
    public static final double[][] zeroes(final int ii, final int jj) {
        double[][] mat = new double[ii][jj];
        for (int kk = 0; kk < ii; kk++) for (int ll = 0; ll < jj; ll++) mat[kk][ll] = 0.0;
        return mat;
    }

    /**
	 * @param ii
	 * @param jj
	 * @return	a zero-filled matrix of size ii x jj
	 */
    public static final double[][] zeroes(final double[][] matrix) {
        for (int kk = 0; kk < matrix.length; kk++) for (int ll = 0; ll < matrix[0].length; ll++) matrix[kk][ll] = 0.0;
        return matrix;
    }

    /**
	 * generate a diagonal matrix with the elements of diag
	 * on its diagonal.
	 * 
	 * @param diag
	 * @return the diagonal matrix
	 */
    public static final double[][] diagonal(final double[] diag) {
        double[][] mat = new double[diag.length][diag.length];
        for (int kk = 0; kk < diag.length; kk++) {
            for (int ll = 0; ll < diag.length; ll++) {
                if (kk == ll) {
                    mat[kk][ll] = diag[kk];
                } else {
                    mat[kk][ll] = 0.0;
                }
            }
        }
        return mat;
    }

    /**
	 * @param matrix
	 * @return a vector
	 */
    public static final double[] flatten(final double[][] matrix) {
        final double[] vector = new double[matrix.length * matrix[0].length];
        int kk = 0;
        for (int ii = 0; ii < matrix.length; ii++) for (int jj = 0; jj < matrix[ii].length; jj++, kk++) vector[kk] = matrix[ii][jj];
        return vector;
    }

    /**
	 * @param tensor a rank3 tensor
	 * @return a vector
	 */
    public static final double[][] flatten(final double[][][] tensor) {
        final double[][] matrix = new double[tensor.length][0];
        for (int ii = 0; ii < tensor.length; ii++) matrix[ii] = flatten(tensor[ii]);
        return matrix;
    }

    /**
	 * add two matrices.
	 * 
	 * @param m1
	 * @param m2
	 * @return		the sum matrix
	 */
    public static final double[][] add(final double m1[][], final double m2[][]) {
        return add(m1, m2, new double[m1.length][m1[0].length]);
    }

    /**
	 * add two matrices.
	 * 
	 * @param m1
	 * @param m2
	 * @param m3 overwritten with m1+m2
	 * @return m3
	 */
    public static final double[][] add(final double m1[][], final double m2[][], final double m3[][]) {
        if (m1.length != m2.length || m1[0].length != m2[0].length) throw new IllegalArgumentException("Matrices must have the same dimensions");
        for (int ii = 0; ii < m1.length; ii++) m3[ii] = add(m1[ii], m2[ii]);
        return m3;
    }

    /**
	 * add two matrices.
	 * 
	 * @param m1
	 * @param m2
	 * @param m3 overwritten with m1+m2
	 * @return m3
	 */
    public static final double[][] addWithOffset(final double m1[][], final double m2[][], final double m3[][], final int offset) {
        if (m1.length + offset != m3.length || m2.length != m3.length || m1[0].length != m2[0].length) throw new IllegalArgumentException("Matrices must have the same dimensions");
        for (int ii = 0; ii < m1.length; ii++) add(m1[ii], m2[ii + offset], m3[ii + offset]);
        return m3;
    }

    /**
	 * same as the other add's, but for tensors of rank 3.
	 * 
	 * @param t1
	 * @param t2
	 * @return the sum
	 */
    public static final double[][][] add(final double t1[][][], final double t2[][][]) {
        if (t1.length != t2.length || t1[0].length != t2[0].length) throw new IllegalArgumentException("Matrices must have the same dimensions");
        double[][][] t3 = new double[t1.length][t1[0].length][t1[0][0].length];
        for (int ii = 0; ii < t1.length; ii++) t3[ii] = add(t1[ii], t2[ii]);
        return t3;
    }

    /**
	 * Scale a 3rd order tensor by some scalar.
	 * 
	 * @param tt
	 * @param dd
	 * @return the scaled tensor
	 */
    public static final double[][][] scale(final double[][][] tt, final double dd) {
        final double[][][] rr = new double[tt.length][0][0];
        for (int ii = 0; ii < tt.length; ii++) rr[ii] = scale(tt[ii], dd);
        return rr;
    }

    /**
	 * subtract two matrices.
	 * 
	 * @param m1
	 * @param m2
	 * @return m2 - m1
	 */
    public static final double[][] subtract(final double m1[][], final double m2[][]) {
        return add(BLA.scale(m1, -1.0), m2);
    }

    /** 
	 * @param m1
	 * @return	the transpose of m1
	 */
    public static final int[][] transpose(final int m1[][]) {
        int[][] m3 = new int[m1[0].length][m1.length];
        for (int ii = 0; ii < m1.length; ii++) for (int jj = 0; jj < m1[ii].length; jj++) m3[jj][ii] = m1[ii][jj];
        return m3;
    }

    /**
	 * @param m1
	 * @return	the transpose of m1
	 */
    public static final double[][] transpose(final double m1[][]) {
        double[][] m3 = new double[m1[0].length][m1.length];
        for (int ii = 0; ii < m1.length; ii++) for (int jj = 0; jj < m1[ii].length; jj++) m3[jj][ii] = m1[ii][jj];
        return m3;
    }

    /**
	 * @param a
	 * @return the determinant of a 3x3 matrix
	 */
    public static final double det3x3(final double[][] a) {
        return a[0][0] * (a[1][1] * a[2][2] - a[1][2] * a[2][1]) + a[1][0] * (a[2][1] * a[0][2] - a[0][1] * a[2][2]) + a[2][0] * (a[0][1] * a[1][2] - a[1][1] * a[0][2]);
    }

    /**
	 * multiplication of an n x m - matrix with a m x k-matrix,
	 * resulting in an n x k - matrix.
	 * 
	 * @param m1
	 * @param m2
	 * @return m1 . m2
	 */
    public static final double[][] matmul(final double m1[][], final double m2[][]) {
        double[][] m2t = BLA.transpose(m2);
        final int nColumns = m1.length;
        final int nRows = m2t.length;
        double[][] m3 = new double[nColumns][nRows];
        for (int ii = 0; ii < nColumns; ii++) for (int jj = 0; jj < nRows; jj++) m3[ii][jj] = dot(m1[ii], m2t[jj]);
        return m3;
    }

    /**
	 * fuctionally equivalent to 
	 * matmul(diagonal(vector), matrix)
	 * but better performance
	 * @param vector
	 * @param matrix
	 * @return matmul(diagonal(vector), matrix)
	 */
    public static final double[][] diagonalMatmul(final double vector[], final double matrix[][]) {
        final double[][] out = copy(matrix);
        return diagonalMatmul(vector, matrix, out);
    }

    /**
	 * fuctionally equivalent to 
	 * matmul(diagonal(vector), matrix)
	 * but better performance
	 * @param vector
	 * @param matrix
	 * @return matmul(diagonal(vector), matrix)
	 */
    public static final double[][] diagonalMatmul(final double vector[], final double matrix[][], final double[][] result) {
        if (vector.length != matrix.length || vector.length != result.length) throw new IllegalArgumentException("Vector and matrix must have same dimension.");
        for (int ii = 0; ii < result.length; ii++) scale(matrix[ii], vector[ii], result[ii]);
        return result;
    }

    /**
	 * multiplication of an m x n matrix with an n-vector.
	 * @param mm
	 * @param vv
	 * @return mm . vv
	 */
    public static final double[] matmul(final double mm[][], final double vv[]) {
        if (vv.length != mm[0].length) throw new IllegalArgumentException("Vector and matrix must have same dimension.");
        double[] xx = new double[vv.length];
        for (int ii = 0; ii < xx.length; ii++) xx[ii] = dot(mm[ii], vv);
        return xx;
    }

    /**
	 * @param mm
	 * @param vv
	 * @return the new matrix mm * vv
	 */
    public static final double[][] scale(final double mm[][], final double vv) {
        double[][] nn = new double[mm.length][mm[0].length];
        return scale(mm, vv, nn);
    }

    /**
	 * @param mm
	 * @param vv
	 * @param nn overwritten with vv * mm
	 * @return nn
	 */
    public static final double[][] scale(final double mm[][], final double vv, final double nn[][]) {
        for (int ii = 0; ii < mm.length; ii++) scale(mm[ii], vv, nn[ii]);
        return nn;
    }

    /**
	 * @param matrix
	 * @return a deep copy of matrix
	 */
    public static final double[][] copy(final double matrix[][]) {
        final int nColumns = matrix.length;
        final int nRows = matrix[0].length;
        double[][] copy = new double[nColumns][nRows];
        for (int ii = 0; ii < nColumns; ii++) {
            for (int jj = 0; jj < nRows; jj++) {
                copy[ii][jj] = matrix[ii][jj];
            }
        }
        return copy;
    }

    public static final double[][] copyOver(final double[][] source, final double[][] dest) {
        for (int ii = 0; ii < source.length; ii++) {
            for (int jj = 0; jj < source[ii].length; jj++) {
                dest[ii][jj] = source[ii][jj];
            }
        }
        return dest;
    }

    /**
	 * returns a selected number of rows of the matrix
	 * 
	 * @param matrix
	 * @param selection
	 * @return the new array
	 */
    public static final double[][] select(final double[][] matrix, final int[] selection) {
        return select(matrix, selection, new double[selection.length][matrix[0].length]);
    }

    /**
	 * returns a selected number of rows of the matrix
	 * 
	 * @param matrix
	 * @param selection
	 * @param result -- overwritten with the selection
	 * @return a reference to result
	 */
    public static final double[][] select(final double[][] matrix, final int[] selection, final double[][] result) {
        if (selection.length > matrix.length) {
            throw new IllegalArgumentException("Selection must be smaller than original.");
        }
        if (selection.length != result.length) {
            throw new IllegalArgumentException("Selection and result matrix must have the same length.");
        }
        for (int ii = 0; ii < selection.length; ii++) {
            result[ii] = copyOver(matrix[selection[ii]], result[ii]);
        }
        return result;
    }

    /**
	 * @param matrix
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return part of matrix ranging from x1 to x2 (exclusive) and y1 to y2 (exclusive)
	 */
    public static final double[][] block(final double matrix[][], final int x1, final int y1, final int x2, final int y2) {
        if (matrix.length < x1 || matrix.length < x2 || matrix[0].length < y1 || matrix[0].length < y2) throw new IllegalArgumentException("Indices outside of matrix.");
        double[][] copy = new double[x2 - x1][y2 - y1];
        for (int ii = x1; ii < x2; ii++) for (int jj = y1; jj < y2; jj++) copy[ii - x1][jj - y1] = matrix[ii][jj];
        return copy;
    }

    /**
	 * Naive and slow (but simple) matrix inversion routine 
	 * ("Complete Gauss-Jordan"). 
	 * 
	 * @param matrix
	 * @return inverse
	 */
    public static double[][] invert(final double matrix[][]) {
        if (matrix.length != matrix[0].length) throw new IllegalArgumentException("Only square matrices can be inverted.");
        final int n = matrix.length;
        double cc[][] = BLA.copy(matrix);
        double DD[][] = BLA.diagonal(BLA.same(n, 1.0));
        for (int i = 0; i < n; ++i) {
            final double alpha = cc[i][i];
            if (alpha == 0.0) {
                throw new RuntimeException("Matrix is singular.");
            }
            cc[i] = BLA.scale(cc[i], 1 / alpha);
            DD[i] = BLA.scale(DD[i], 1 / alpha);
            for (int k = 0; k < n; ++k) {
                if (k != i) {
                    final double beta = -cc[k][i];
                    cc[k] = BLA.add(BLA.scale(cc[i], beta), cc[k]);
                    DD[k] = BLA.add(BLA.scale(DD[i], beta), DD[k]);
                }
            }
        }
        return DD;
    }

    /**
	 * public Method diagonalizeSymmetric diagonalizes a symmetric matrix
	 * by first reducing it to tridiagonal form (private void method: tred2)
	 * and then evaluating eigenvalues and eigenvectors using the QL algorithm
	 * (private void method tqli).
	 * (see: Numerical Recipes in C. 2nd edition. page 469ff.)
	 * @param matrix symmetric matrix double[][]
	 * @param eigenvalues an array of eigenvalues double[]
	 */
    public static void diagonalizeSymmetric(double matrix[][], double eigenvalues[]) {
        double e[] = new double[matrix.length];
        tred2(matrix, eigenvalues, e);
        tqli(matrix, eigenvalues, e);
        sort(matrix, eigenvalues);
    }

    /**
	 * Householder reduction of a real, symmetric matrix (double mat[][])	 
	 * @param matrix			real, symmetric matrix double[][], replaced by its orthogonal 
	 * 						matrix upon return
	 * @param diagonal 		returns the diagonal elements of the tridiagonal matrix
	 * @param offdiagonal 	returns the offdiagonal elements of the tridiagonal matrix
	 */
    private static void tred2(double matrix[][], double diagonal[], double offdiagonal[]) {
        int l, k, j, i;
        int n = matrix.length;
        double scale, hh, h, g, f;
        for (i = n - 1; i > 0; i--) {
            l = i - 1;
            h = scale = 0.0;
            if (l > 0) {
                for (k = 0; k <= l; k++) scale += Math.abs(matrix[i][k]);
                if (scale == 0.0) offdiagonal[i] = matrix[i][l]; else {
                    for (k = 0; k <= l; k++) {
                        matrix[i][k] /= scale;
                        h += matrix[i][k] * matrix[i][k];
                    }
                    f = matrix[i][l];
                    g = (f >= 0.0 ? -Math.sqrt(h) : Math.sqrt(h));
                    offdiagonal[i] = scale * g;
                    h -= f * g;
                    matrix[i][l] = f - g;
                    f = 0.0;
                    for (j = 0; j <= l; j++) {
                        matrix[j][i] = matrix[i][j] / h;
                        g = 0.0;
                        for (k = 0; k <= j; k++) g += matrix[j][k] * matrix[i][k];
                        for (k = j + 1; k <= l; k++) g += matrix[k][j] * matrix[i][k];
                        offdiagonal[j] = g / h;
                        f += offdiagonal[j] * matrix[i][j];
                    }
                    hh = f / (h + h);
                    for (j = 0; j <= l; j++) {
                        f = matrix[i][j];
                        offdiagonal[j] = g = offdiagonal[j] - hh * f;
                        for (k = 0; k <= j; k++) matrix[j][k] -= (f * offdiagonal[k] + g * matrix[i][k]);
                    }
                }
            } else offdiagonal[i] = matrix[i][l];
            diagonal[i] = h;
        }
        diagonal[0] = 0.0;
        offdiagonal[0] = 0.0;
        for (i = 0; i < n; i++) {
            l = i - 1;
            if (diagonal[i] != 0) {
                for (j = 0; j <= l; j++) {
                    g = 0.0;
                    for (k = 0; k <= l; k++) g += matrix[i][k] * matrix[k][j];
                    for (k = 0; k <= l; k++) matrix[k][j] -= g * matrix[k][i];
                }
            }
            diagonal[i] = matrix[i][i];
            matrix[i][i] = 1.0;
            for (j = 0; j <= l; j++) matrix[j][i] = matrix[i][j] = 0.0;
        }
    }

    /**
	 * QL algorithm with implicit shifts, to determine the eigenvalues and eigenvectors 
	 * of a real, symmetric, tridiagonal matrix
	 * 
	 * @param matrix			reduced matrix from householder reduction routine tred2
	 * @param diagonal		diagonal elements of the tridiagonal matrix; replaced the
	 * 						eigenvalues upon return.
	 * @param subdiagonal		subdiagonal elements of the tridiagonal matrix
	 */
    public static void tqli(double matrix[][], double diagonal[], double subdiagonal[]) {
        if (matrix.length != diagonal.length || matrix.length != subdiagonal.length) throw new IllegalArgumentException("tQLI: Need same number of column elements " + "in diagonal and subdiagonal vectors.");
        int m, l, iter, i, k, n = matrix.length;
        double s, r, p, g, f, dd, c, b;
        for (i = 1; i < n; i++) subdiagonal[i - 1] = subdiagonal[i];
        subdiagonal[n - 1] = 0.0;
        for (l = 0; l < n; l++) {
            iter = 0;
            do {
                for (m = l; m < n - 1; m++) {
                    dd = Math.abs(diagonal[m]) + Math.abs(diagonal[m + 1]);
                    if ((Math.abs(subdiagonal[m]) + dd) == dd) break;
                }
                if (m != l) {
                    if (iter++ == 300) {
                        throw new RuntimeException("Too many iterations in tqli. " + "m and l: " + m + " " + l);
                    }
                    g = (diagonal[l + 1] - diagonal[l]) / (2.0 * subdiagonal[l]);
                    r = sqrt_sum(g, 1.0);
                    g = diagonal[m] - diagonal[l] + subdiagonal[l] / (g + ((g >= 0) ? Math.abs(r) : -Math.abs(r)));
                    s = c = 1.0;
                    p = 0.0;
                    for (i = m - 1; i >= l; i--) {
                        f = s * subdiagonal[i];
                        b = c * subdiagonal[i];
                        subdiagonal[i + 1] = (r = sqrt_sum(f, g));
                        if (r == 0.0) {
                            diagonal[i + 1] -= p;
                            subdiagonal[m] = 0.0;
                            break;
                        }
                        s = f / r;
                        c = g / r;
                        g = diagonal[i + 1] - p;
                        r = (diagonal[i] - g) * s + 2.0 * c * b;
                        diagonal[i + 1] = g + (p = s * r);
                        g = c * r - b;
                        for (k = 0; k < n; k++) {
                            f = matrix[k][i + 1];
                            matrix[k][i + 1] = s * matrix[k][i] + c * f;
                            matrix[k][i] = c * matrix[k][i] - s * f;
                        }
                    }
                    if (r == 0.0 && i >= l) continue;
                    diagonal[l] -= p;
                    subdiagonal[l] = g;
                    subdiagonal[m] = 0.0;
                }
            } while (m != l);
        }
    }

    /**
	 * Return the (a^2 + b^2)^(1/2) without over- or underflow
	 * 
	 * @param a
	 * @param b
	 * @return (a^2 + b^2)^(1/2)
	 */
    public static double sqrt_sum(double a, double b) {
        double absa, absb;
        absa = Math.abs(a);
        absb = Math.abs(b);
        if (absa > absb) return absa * Math.sqrt(1.0 + (absb / absa) * (absb / absa));
        return (absb == 0.0 ? 0.0 : absb * Math.sqrt(1.0 + (absa / absb) * (absa / absb)));
    }

    public static void sort(double mat[][], double eigenvalues[]) {
        int min;
        double temp;
        for (int i = 0; i < mat.length - 1; i++) {
            min = i;
            for (int j = i + 1; j < mat.length; j++) {
                if (eigenvalues[j] > eigenvalues[min]) min = j;
            }
            temp = eigenvalues[min];
            eigenvalues[min] = eigenvalues[i];
            eigenvalues[i] = temp;
            swapColumns(mat, i, min);
        }
    }

    public static void swapColumns(double mat[][], int i, int j) {
        double temp;
        for (int k = 0; k < mat.length; k++) {
            temp = mat[k][i];
            mat[k][i] = mat[k][j];
            mat[k][j] = temp;
        }
    }

    /**
	 * public Method floyd calculates the shortest path through an adjacency matrix.
	 * COMMUNICATIONS OF THE ACM 5 (6): 345-345 1962<br>
	 * This is of O(n^3), so watch out!
	 * @f[d^{\left( k \right)}  = \left\{ \begin{array}{l}
	 k = 0 \Rightarrow w\left( {v_i ,v_r } \right) \\ 
	 k \ge 1 \Rightarrow \min \left\{ {d_{i,j}^{\left( {k - 1} \right)} 
	 ,d_{i,k}^{\left( {k - 1} \right)} ,d_{k,j}^{\left( {k - 1} \right)} } \right\} \\ 
	 \end{array} \right. @f] 
	 *
	 * @param  matrix A double[Nnodes][Nnodes] adjacency matrix containing some distances.
	 * @param  Nnodes number of nodes in the matrix
	 * @return the original matrix now containing the shortest path distances
	 */
    public static void floyd(int matrix[][], int Nnodes) {
        for (int i = 0; i < Nnodes; ++i) {
            for (int j = 0; j < Nnodes; ++j) {
                for (int k = 0; k < Nnodes; ++k) {
                    if ((matrix[j][i] + matrix[i][k]) < matrix[j][k]) {
                        matrix[j][k] = matrix[j][i] + matrix[i][k];
                    }
                }
            }
        }
    }

    public static void heapsort(double values[], int n, int key[]) {
        for (int i = 0; i < n; ++i) key[i] = i;
        int k = n / 2 + 1;
        int index = n;
        double lists;
        int keys;
        do {
            if (k > 1) {
                k = k - 1;
                lists = values[k - 1];
                keys = key[k - 1];
            } else {
                lists = values[index - 1];
                keys = key[index - 1];
                values[index - 1] = values[0];
                key[index - 1] = key[0];
                index = index - 1;
            }
            if (index <= 1) {
                values[0] = lists;
                key[0] = keys;
                return;
            }
            int i = k;
            int j = k + k;
            do {
                if (j < index) {
                    if (values[j - 1] < values[j]) ++j;
                }
                if (lists < values[j - 1]) {
                    values[i - 1] = values[j - 1];
                    key[i - 1] = key[j - 1];
                    i = j;
                    j = j + j;
                } else {
                    j = index + 1;
                }
            } while (j <= index);
            values[i - 1] = lists;
            key[i - 1] = keys;
        } while (n > 1);
    }

    public static String toString(final double[][] mm, final String format) {
        String ss = "";
        final PrintfFormat ff = new PrintfFormat(format);
        for (int ii = 0; ii < mm.length; ii++) {
            for (int jj = 0; jj < mm[ii].length; jj++) {
                ss += ff.sprintf(mm[ii][jj]);
            }
            ss += '\n';
        }
        return ss;
    }

    public static String toString(final double[][] mm) {
        return toString(mm, "%7.3f");
    }
}
