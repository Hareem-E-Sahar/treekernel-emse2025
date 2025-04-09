package de.helwich.linalg;

import java.util.Arrays;

/**
 * A collection of real number matrix operations.
 * 
 * @author Hendrik Helwich
 */
public class MatrixUtil {

    private MatrixUtil() {
    }

    /**
	 * Return a new matrix buffer if the given matrix buffer is
	 * <code>null</code> or smaller than the given matrix dimension.
	 * If needed, the returned matrix buffer can ensured to have a zero value
	 * in every element.
	 * 
	 * @param  buf
	 *         A matrix buffer or <code>null</code>.
	 * @param  height
	 *         The minimal height of the returned matrix buffer.
	 * @param  width
	 *         The minimal width of the returned matrix buffer.
	 * @param  clear
	 *         If this is <code>true</code> and the given buffer is not
	 *         <code>null</code>, the given buffer will be filled with zero
	 *         values for the given matrix dimension.
	 *         This can be used to ensure that the returned buffer always holds
	 *         zero values.
	 * @return The given matrix buffer or a new buffer if the given buffer is
	 *         <code>null</code> or smaller than the given matrix dimension.
	 */
    public static double[][] create(double[][] buf, int height, int width, boolean clear) {
        if (buf == null || buf.length < height || buf[0].length < width) buf = new double[height][width]; else if (clear) fill(buf, height, width, 0);
        return buf;
    }

    /**
	 * Returns a square identity matrix with the given size.
	 * 
	 * @param  buf
	 *         A matrix buffer with the specified size or <code>null</code>.
	 *         If this parameter is not <code>null</code> its content will be
	 *         overwritten.
	 * @param  size
	 *         The height and width of the returned identity matrix.
	 * @return A square identity matrix with the given size.
	 */
    public static double[][] createOnes(double[][] buf, int size) {
        buf = create(buf, size, size, true);
        for (int i = 0; i < size; i++) buf[i][i] = 1;
        return buf;
    }

    /**
	 * Copy the content of the given source matrix <code>A</code> to a target
	 * matrix buffer and return it.
	 * 
	 * @param  buf
	 *         A buffer which can be used as target matrix. If this is
	 *         <code>null</code>, a new matrix array is created.
	 * @param  A
	 *         The source matrix which content will be copied to the target
	 *         matrix. It must have the given height and width.
	 * @param  height
	 *         The height of the give source matrix, the given target buffer
	 *         (if not <code>null</code>) and the return matrix.
	 * @param  width
	 *         The width of the give source matrix, the given target buffer
	 *         (if not <code>null</code>) and the return matrix.
	 * @return A copy of the given source matrix <code>A</code>.
	 */
    public static double[][] copy(double[][] buf, double[][] A, int height, int width) {
        if (buf == null) buf = new double[height][width]; else if (buf == A) return buf;
        for (int i = 0; i < height; i++) for (int j = 0; j < width; j++) buf[i][j] = A[i][j];
        return buf;
    }

    /**
	 * Set all elements in a matrix to a given value.
	 * 
	 * @param  A
	 *         A matrix which elements will be set to a given value.
	 * @param  height
	 *         The height of the given matrix <code>A</code>.
	 * @param  width
	 *         The width of the given matrix <code>A</code>.
	 * @param  value
	 *         A value which will be stored in each element of the given matrix.
	 */
    public static void fill(double[][] A, int height, int width, double value) {
        for (int i = 0; i < height; i++) Arrays.fill(A[i], 0, width, value);
    }

    /**
	 * Return <code>true</code> if both given matrices have equal content.
	 * 
	 * @param  A
	 *         matrix to be compared with matrix <code>B</code>
	 * @param  B
	 *         matrix to be compared with matrix <code>A</code>
	 * @param  height
	 *         Height of matrix <code>A</code> and <code>B</code>
	 * @param  width
	 *         Width of matrix <code>A</code> and <code>B</code>
	 * @param  e
	 *         positive maximum difference between two corresponding elements
	 *         of the two given matrices. Exact equality is evaluated if this 
	 *         value is set two <code>0</code>.
	 * @return <code>true</code> if both given matrices have equal content.
	 */
    public static boolean isEqual(double[][] A, double[][] B, int height, int width, double e) {
        for (int i = 0; i < height; i++) for (int j = 0; j < width; j++) if (Math.abs(A[i][j] - B[i][j]) > e) return false;
        return true;
    }

    /**
	 * Return <code>true</code> if both given matrices have equal content.
	 * Same as calling
	 * {@link #isEqual(double[][], double[][], int, int, double)} with setting
	 * parameter <code>e</code> to <code>0</code>.
	 * 
	 * @param  A
	 *         matrix to be compared with matrix <code>B</code>
	 * @param  B
	 *         matrix to be compared with matrix <code>A</code>
	 * @param  height
	 *         Height of matrix <code>A</code> and <code>B</code>
	 * @param  width
	 *         Width of matrix <code>A</code> and <code>B</code>
	 * @return <code>true</code> if both given matrices have equal content.
	 */
    public static boolean isEqual(double[][] A, double[][] B, int height, int width) {
        return isEqual(A, B, height, width, 0);
    }

    /**
	 * Transpose the given matrix <code>A</code>.
	 * 
	 * @param  buf
	 *         A matrix buffer to store the result matrix or <code>null</code>.
	 *         If this parameter is not <code>null</code> it must at least have
	 *         the height <code>widthA</code> and the width
	 *         <code>heightA</code>.
	 *         If this parameter is <code>null</code>, a new matrix buffer with
	 *         height <code>widthA</code> and the width <code>heightA</code> will
	 *         be created.
	 *         If this parameter is equal to the parameter <code>A</code>, the
	 *         transposition is done in place.
	 * @param  A
	 * @param  heightA
	 *         Height of matrix <code>A</code>.
	 * @param  widthA
	 *         Width of matrix <code>A</code>.
	 * @return The transposed matrix of the given matrix <code>A</code>.
	 */
    public static double[][] transpose(double[][] buf, double[][] A, int heightA, int widthA) {
        buf = create(buf, widthA, heightA, false);
        if (buf == A) {
            double t;
            for (int i = 0; i < widthA; i++) for (int j = i + 1; j < heightA; j++) {
                t = A[i][j];
                A[i][j] = A[j][i];
                A[j][i] = t;
            }
        } else for (int i = 0; i < widthA; i++) for (int j = 0; j < heightA; j++) buf[i][j] = A[j][i];
        return buf;
    }

    /**
	 * Returns the matrix product <code>A*B</code> of two given matrices
	 * <code>A</code> and <code>B</code>.
	 * The height and width of the returned matrix depend on the size of both
	 * input matrices and the values of parameter <code>at</code> and
	 * <code>bt</code>.
	 * Matrices <code>A</code> and <code>B</code> can be the same instances, but
	 * must both be different to the given matrix buffer <code>buf</code>.
	 * 
	 * @param  buf
	 *         A buffer matrix to hold the resulting matrix product or
	 *         <code>null</code>.
	 *         If this parameter is <code>null</code>, a new matrix buffer will
	 *         be created.
	 * @param  A
	 *         Left input matrix.
	 * @param  B
	 *         right input matrix.
	 * @param  heightA
	 *         Height of matrix <code>A</code>.
	 * @param  widthA
	 *         Width of matrix <code>A</code>.
	 * @param  heightB
	 *         Height of matrix <code>B</code>.
	 * @param  widthB
	 *         Width of matrix <code>B</code>.
	 * @param  at
	 *         <code>true</code> if the matrix <code>A</code> should be
	 *         transposed before multiplication.
	 * @param  bt
	 *         <code>true</code> if the matrix <code>B</code> should be
	 *         transposed before multiplication.
	 * @return the matrix product <code>A*B</code> of two given matrices
	 *         <code>A</code> and <code>B</code>.
	 */
    public static double[][] multiply(double[][] buf, double[][] A, double[][] B, int heightA, int widthA, int heightB, int widthB, boolean at, boolean bt) {
        if (buf == A || buf == B) throw new IllegalArgumentException("buffer is equal to source matrix");
        int am = heightA, an = widthA;
        int bm = heightB, bn = widthB;
        if ((at ? am : an) != (bt ? bn : bm)) throw new IllegalArgumentException("unmatching matrix dimensions");
        buf = create(buf, at ? an : am, bt ? bm : bn, true);
        if (at) if (bt) for (int m = 0; m < an; m++) for (int n = 0; n < bm; n++) for (int i = 0; i < am; i++) buf[m][n] += A[i][m] * B[n][i]; else for (int m = 0; m < an; m++) for (int n = 0; n < bn; n++) for (int i = 0; i < am; i++) buf[m][n] += A[i][m] * B[i][n]; else if (bt) for (int m = 0; m < am; m++) for (int n = 0; n < bm; n++) for (int i = 0; i < an; i++) buf[m][n] += A[m][i] * B[n][i]; else for (int m = 0; m < am; m++) for (int n = 0; n < bn; n++) for (int i = 0; i < an; i++) buf[m][n] += A[m][i] * B[i][n];
        return buf;
    }

    /**
	 * Returns the sum of two matrices. 
	 * 
	 * @param  buf
	 *         A matrix buffer to hold the resulting matrix sum or
	 *         <code>null</code>.
	 *         If this parameter is <code>null</code>, a new matrix buffer with
	 *         the size given by the <code>height</code> and <code>width</code>
	 *         parameters will be created.
	 *         This parameter can also be the same instance as <code>A</code>
	 *         or <code>B</code> in which case the sum will be calculated in
	 *         place
	 * @param  A
	 *         An input matrix for the matrix vector sum
	 * @param  B
	 *         An input matrix for the matrix vector sum
	 * @param  height
	 *         The height of both matrices
	 * @param  width
	 *         The width of both matrices
	 * @return The sum of two matrices
	 */
    public static double[][] sum(double[][] buf, double[][] A, double[][] B, int height, int width) {
        buf = create(buf, height, width, false);
        for (int i = 0; i < height; i++) for (int j = 0; j < width; j++) buf[i][j] = A[i][j] + B[i][j];
        return buf;
    }

    /**
	 * Returns the matrix product <code>A*B</code> of two given matrices
	 * <code>A</code> and <code>B</code>.
	 * The height of the returned matrix will be <code>heightA</code> and the
	 * width will be <code>widthB</code>.
	 * Matrices <code>A</code> and <code>B</code> can be the same instances, but
	 * must both be different to the given matrix buffer <code>buf</code>.
	 * The height of matrix <code>B</code> must be equal to <code>widthA</code>.
	 * 
	 * @param  buf
	 *         A buffer matrix to hold the resulting matrix product or
	 *         <code>null</code>.
	 *         If this parameter is not <code>null</code> it must at least have
	 *         the height <code>heightA</code> and the width
	 *         <code>widthB</code>.
	 *         If this parameter is <code>null</code>, a new matrix buffer with
	 *         height <code>widthA</code> and the width <code>widthB</code> will
	 *         be created.
	 * @param  A
	 *         Left input matrix.
	 * @param  B
	 *         right input matrix.
	 * @param  heightA
	 *         Height of matrix <code>A</code>.
	 * @param  widthA
	 *         Width of matrix <code>A</code>.
	 * @param  widthB
	 *         Width of matrix <code>B</code>.
	 * @return the matrix product <code>A*B</code> of two given matrices
	 *         <code>A</code> and <code>B</code>.
	 */
    public static double[][] multiply(double[][] buf, double[][] A, double[][] B, int heightA, int widthA, int widthB) {
        return multiply(buf, A, B, heightA, widthA, widthA, widthB, false, false);
    }

    /**
	 * Create a column vector matrix from a single dimensional array vector
	 * 
	 * @param  buf
	 * @param  v
	 * @param  size
	 * @return
	 */
    public static double[][] getColumnVector(double[][] buf, double[] v, int size) {
        buf = create(buf, size, 1, false);
        for (int i = 0; i < size; i++) buf[i][0] = v[i];
        return buf;
    }

    /**
	 * Extract a column from the given matrix to a single dimensional array 
	 * 
	 * @param buf
	 * @param A
	 * @param height
	 * @param column
	 * @return
	 */
    public static double[] extractColumn(double[] buf, double[][] A, int height, int column) {
        if (buf == null || buf.length < height) buf = new double[height];
        for (int i = 0; i < height; i++) buf[i] = A[i][column];
        return buf;
    }

    /**
	 * Calculates A-B
	 * 
	 * @param buf
	 * @param A
	 * @param B
	 * @param height
	 * @param width
	 * @return
	 */
    public static double[][] subtract(double[][] buf, double[][] A, double[][] B, int height, int width) {
        if (buf == null) buf = create(buf, height, width, false);
        for (int i = 0; i < height; i++) for (int j = 0; j < width; j++) buf[i][j] = A[i][j] - B[i][j];
        return buf;
    }

    public static String toString(double[][] A, int height, int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) sb.append(Arrays.toString(A[i])).append('\n');
        return sb.toString();
    }
}
