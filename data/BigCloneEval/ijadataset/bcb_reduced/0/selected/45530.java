package unbfuzzy.math;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.RealMatrixImpl;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Class that implements some numerical methods for matrices
 * (<code>double[][]</code>)..
 * Some methods uses linear algebra library <b>JAMA</b>
 * (<a href="http://math.nist.gov/javanumerics/jama">
 * http://math.nist.gov/javanumerics/jama</a>). See JAMA documentation
 * for details.
 * 
 * @version $Revision: 1.20 $ - $Date: 2006/12/04 22:32:19 $
 * 
 * @author <a href="mailto:rodbra@pop.com.br">Rodrigo C. M. Coimbra</a>
 * @author <a href="mailto:hugoiver@yahoo.com.br">Hugo Iver V. Gon&ccedil;alves</a>
 */
public class MatrixMath {

    /**
	 * Prints a matrix on screen.
	 * 
	 * @param matrix
	 *            Matrix to be printed.
	 */
    public static void print(double[][] matrix) {
        if (matrix != null) {
            Matrix m = new Matrix(matrix);
            m.print(10, 20);
        }
    }

    /**
	 * Obtains the diagonal matrix of a matrix.
	 * 
	 * @param matrix
	 *            Matrix to obtain the diagonal matrix.
	 * 
	 * @return Diagonal matrix of the parameter.
	 */
    public static double[][] diagonal(double[][] matrix) {
        double[][] diagonal = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < diagonal.length; i++) {
            diagonal[i][i] = matrix[i][i];
        }
        return diagonal;
    }

    /**
	 * Obtains an array with the values on the diagonal of a matrix.
	 * 
	 * @param matrix
	 *            Matrix to get the diagonal.
	 * 
	 * @return Values on the diagonal on an array.
	 */
    public static double[] diagonalOnArray(double[][] matrix) {
        double[] diagonalOnArray;
        int mLength = matrix.length;
        diagonalOnArray = new double[mLength];
        for (int i = 0; i < mLength; i++) {
            diagonalOnArray[i] = matrix[i][i];
        }
        return diagonalOnArray;
    }

    /**
	 * Calculates the product of a transposed matrix by the original.
	 * 
	 * @param matrix
	 *            Matrix to calculate.
	 * 
	 * @return Product of a transposed matrix by the original.
	 */
    public static double[][] transposeProduct(double[][] matrix) {
        double element;
        double[][] product = new double[matrix[0].length][matrix[0].length];
        for (int column1 = 0; column1 < matrix[0].length; column1++) {
            element = 0;
            for (int column2 = 0; column2 < matrix[0].length; column2++) {
                element = 0;
                for (int line = 0; line < matrix.length; line++) {
                    element = element + matrix[line][column1] * matrix[line][column2];
                }
                product[column1][column2] = element;
            }
        }
        return product;
    }

    /**
	 * Obtains the transposed matrix of a matrix.
	 * 
	 * @param matrix
	 *            Matrix to obtain the transposed matrix.
	 * 
	 * @return Transposed matrix.
	 */
    public static double[][] transpose(double[][] matrix) {
        double[][] transposed = new double[matrix[0].length][matrix.length];
        int mLength = matrix.length, m0Length = matrix[0].length;
        for (int i = 0; i < mLength; i++) {
            for (int j = 0; j < m0Length; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    /**
	 * Calculates the sum of two matrices.
	 * 
	 * @param matrix1
	 *            First matrix to sum.
	 * @param matrix2
	 *            Second matrix to sum.
	 * 
	 * @return The sum of the two matrices.
	 */
    public static double[][] sum(double[][] matrix1, double[][] matrix2) {
        double[][] sum;
        int mLength, m0Length;
        if ((matrix1.length == matrix2.length) && (matrix1[0].length == matrix2[0].length)) {
            mLength = matrix1.length;
            m0Length = matrix1[0].length;
            sum = new double[mLength][m0Length];
            for (int i = 0; i < mLength; i++) {
                for (int j = 0; j < m0Length; j++) {
                    sum[i][j] = matrix1[i][j] + matrix2[i][j];
                }
            }
        } else {
            throw new MathException("Matrices must have same dimension!");
        }
        return sum;
    }

    /**
	 * Calculates the difference between two matrices; first matrix minus second
	 * matrix.
	 * 
	 * @param matrix1
	 *            First matrix.
	 * @param matrix2
	 *            Second matrix.
	 * 
	 * @return The difference between the two matrices.
	 */
    public static double[][] subtract(double[][] matrix1, double[][] matrix2) {
        double[][] difference;
        if ((matrix1.length == matrix2.length) && (matrix1[0].length == matrix2[0].length)) {
            difference = new double[matrix1.length][matrix1[0].length];
            for (int i = 0; i < matrix1.length; i++) {
                for (int j = 0; j < matrix1[0].length; j++) {
                    difference[i][j] = matrix1[i][j] - matrix2[i][j];
                }
            }
        } else {
            throw new MathException("Matrices must have same dimension!");
        }
        return difference;
    }

    /**
	 * Sums an array with the diagonal of a matrix.
	 * 
	 * @param matrix
	 *            Matrix.
	 * @param diagonal
	 *            Array to sum with the diagonal.
	 * 
	 * @return Them sum of an array with the diagonal of a matrix.
	 */
    public static double[][] sumDiagonal(double[][] matrix, double[] diagonal) {
        double[][] newMatrix;
        int mLength;
        if (matrix.length == diagonal.length) {
            mLength = matrix.length;
            newMatrix = (double[][]) matrix.clone();
            for (int i = 0; i < mLength; i++) {
                newMatrix[i][i] = diagonal[i] + newMatrix[i][i];
            }
        } else {
            throw new MathException("Matrix and array must have compatible dimensions!");
        }
        return newMatrix;
    }

    /**
	 * Sums a constant to the diagonal of a matrix.
	 * 
	 * @param constant
	 *            Constant to sum.
	 * @param matrix
	 *            Matrix.
	 * 
	 * @return A matrix with the diagonal added by a constant.
	 */
    public static double[][] sumConstantDiagonal(double constant, double[][] matrix) {
        double[][] newMatrix;
        int mLength = matrix.length;
        newMatrix = (double[][]) matrix.clone();
        for (int i = 0; i < mLength; i++) {
            newMatrix[i][i] = constant + newMatrix[i][i];
        }
        return newMatrix;
    }

    /**
	 * Calculates the multiplicative inverse of a matrix.
	 * 
	 * @param matrix
	 *            Matrix to invert.
	 * 
	 * @return Inverse matrix.
	 */
    public static final double[][] inverse(double[][] matrix) {
        double[][] result = null;
        DenseMatrix A = new DenseMatrix(matrix);
        DenseMatrix I = Matrices.identity(matrix.length);
        DenseMatrix AI = I.copy();
        no.uib.cipr.matrix.Matrix inv = A.solve(I, AI);
        result = Matrices.getArray(inv);
        return result;
    }

    public static final void testEVDcolt(double[][] matrix) {
        DoubleMatrix2D m = DoubleFactory2D.dense.make(matrix);
        EigenvalueDecomposition evd = new EigenvalueDecomposition(m);
        print(evd.getD().toArray());
        print(evd.getV().toArray());
    }

    public static final void testEVDjama(double[][] matrix) {
        Matrix m = new Matrix(matrix);
        Jama.EigenvalueDecomposition evd = new Jama.EigenvalueDecomposition(m);
        print(evd.getD().getArray());
        print(evd.getV().getArray());
        double[][] dt = transpose(evd.getV().getArray());
        double[][] p = new double[1][dt[0].length];
        p[0] = dt[0];
        double[][] pt = MatrixMath.transpose(p);
        print(pt);
        print(MatrixMath.product(matrix, pt));
        print(MatrixMath.constantTimes(evd.getD().getArray()[0][0], pt));
    }

    /**
	 * Calculates the product of two matrices, 'first' times 'second'. This
	 * method uses the Kahan's summation algorithm for the computation of an
	 * element of the product matrix.
	 * 
	 * @param first
	 *            First matrix of the product.
	 * @param second
	 *            Second matrix of the product.
	 * 
	 * @return The product of the two matrices.
	 */
    public static final double[][] product(double[][] first, double[][] second) {
        double sum = 0;
        double correction = 0;
        double correctedAddend = 0;
        double tempSum = 0;
        double[][] product = null;
        int firstLength, first0Length, second0Length;
        double A[] = new double[first.length];
        double B[] = new double[second[0].length];
        if (first[0].length == second.length) {
            firstLength = first.length;
            first0Length = first[0].length;
            second0Length = second[0].length;
            product = new double[first.length][second[0].length];
            for (int row = 0; row < firstLength; row++) {
                for (int col = 0; col < second0Length; col++) {
                    sum = 0;
                    correction = 0;
                    correctedAddend = 0;
                    for (int i = 0; i < first0Length; i++) {
                        correctedAddend = first[row][i] * second[i][col] + correction;
                        tempSum = sum + correctedAddend;
                        correction = correctedAddend - (tempSum - sum);
                        sum = tempSum;
                    }
                    product[row][col] = sum;
                }
            }
        } else {
            throw new MathException("Matrices are not multiplication compatible!");
        }
        return product;
    }

    /**
	 * Calculates the product of a matrix by a constant.
	 * 
	 * @param constant
	 *            Constant of the product.
	 * @param matrix
	 *            Matrix of the product.
	 * 
	 * @return Product of a matrix by a constant.
	 */
    public static final double[][] constantTimes(double constant, double[][] matrix) {
        double[][] result = new double[matrix.length][matrix[0].length];
        int mLength = matrix.length, m0Length = matrix[0].length;
        for (int i = 0; i < mLength; i++) for (int j = 0; j < m0Length; j++) result[i][j] = constant * matrix[i][j];
        return result;
    }

    /**
	 * Calculates the product of an array by a constant generating a matrix.
	 * 
	 * @param constant
	 *            Constant of the product.
	 * @param matrix
	 *            Array of the product.
	 * 
	 * @return Product of a array by a constant generating a matrix.
	 */
    public static final double[][] constantTimes(double constant, double[] matrix) {
        double[][] result = new double[matrix.length][1];
        for (int i = 0; i < matrix.length; i++) {
            result[i][0] = constant * matrix[i];
        }
        return result;
    }

    /**
	 * Calculates the product of a diagonal of a matrix by a constant.
	 * 
	 * @param constant
	 *            Constant of the product.
	 * @param matrix
	 *            Matrix of the product.
	 * 
	 * @return Product of a diagonal of a matrix by a constant.
	 */
    public static final double[][] constantTimesDiagonal(double constant, double[][] matrix) {
        double[][] newMatrix;
        newMatrix = (double[][]) matrix.clone();
        for (int i = 0; i < newMatrix.length; i++) {
            newMatrix[i][i] = constant * newMatrix[i][i];
        }
        return newMatrix;
    }

    /**
	 * Creates a identity matrix with dimensions <i>i</i> and <i>j</i>.
	 * 
	 * @param i
	 *            Lines dimension.
	 * @param j
	 *            Columns dimension.
	 * 
	 * @return Identity matrix.
	 */
    public static final double[][] identity(int i, int j) {
        return Matrix.identity(i, j).getArray();
    }

    /**
	 * Solves a linear system <i>ax = b </i>.
	 * 
	 * @param a
	 *            Matrix <i>a</i>.
	 * @param b
	 *            Matrix <i>b</i>.
	 * 
	 * @return The linear system solution.
	 */
    public static final double[][] solve(double[][] a, double[][] b) {
        double[][] result = null;
        Matrix A = new Matrix(a);
        Matrix B = new Matrix(b);
        Matrix X;
        X = A.solve(B);
        result = X.getArray();
        return result;
    }
}
