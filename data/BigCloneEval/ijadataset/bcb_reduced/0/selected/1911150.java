package org.fhi.pgps.math;

/**
 * @version 0.1
 * @author Julien Eberle
 * 
 * This class represent a matrix (array of number)
 * It implements also some of the most common operations.
 * Most functions return a new matrix and don't modify
 * the current instance. It avoids border effects and allows 
 * writing more functionnal code as for example :
 * Matrix m = new Matrix(new double[][]{{1,2},{3,4},{5,6}});
 * Matrix m1 = m.multiply(3).multiply(m.transpose()).inverse();
 *
 */
public class Matrix {

    /** 
	 * This is the internal representation of the matrix
	 * Be careful when chosing an element: m[column][row].
	 * for example <code>new double[][]{{1,2},{3,4},{5,6}}</code> get
	 *   1 3 5
	 *   2 4 6 
	 */
    private double[][] m = { { 0 } };

    /**
	 * main function used for testing
	 * @param args
	 */
    public static void main(String[] args) {
        Matrix mm = new Matrix(new double[][] { { 2, 3 }, { 4, 5 }, { 6, 7 } });
        System.out.println(mm);
        System.out.println(mm.multiply(2));
        System.out.println(mm.transpose());
        System.out.println(mm.add(new Matrix(new double[][] { { 1, 1 }, { 1, 1 }, { 1, 2 } })));
        System.out.println(mm.multiply(Matrix.identity(3).multiply(3)));
        System.out.println(mm.multiply(3).multiply(mm.transpose()).inverse());
        Matrix m2 = new Matrix(new double[][] { { 4, 0, 8, 0 }, { 5, 0, 0, 0 }, { 12, 0, 0, 234 }, { 89, 4, 9, 0 } });
        Matrix m3 = new Matrix(4, 4);
        for (int i = 0; i < m2.m.length; i++) {
            for (int j = 0; j < m2.m[0].length; j++) {
                m3.m[i][j] = m2.cofactor(i, j);
            }
        }
        System.out.println(m3);
        System.out.println(m2.inverse());
    }

    /**
	 * Constructor. Get a new matrix given its value
	 * @param d must be at least a 1x1 array
	 */
    public Matrix(double[][] d) {
        if (d.length == 0 || d[0].length == 0) {
            throw new MatrixException("invalid matrix dimension : " + d.length + "x" + (d.length == 0 ? 0 : d[0].length));
        }
        m = new double[d.length][d[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] = d[i][j];
            }
        }
    }

    /**
	 * Constructor. Get a new matrix given its dimensions, filled with zeros
	 * @param row must be greater than 0
	 * @param col must be greater than 0
	 */
    public Matrix(int row, int col) {
        if (col <= 0 || row <= 0) {
            throw new MatrixException("invalid matrix dimension : " + row + "x" + col);
        }
        m = new double[col][row];
    }

    /**
	 * The default constructor is private to avoid 
	 * unconsistancies with the size of the array m.
	 */
    private Matrix() {
    }

    /**
	 * static function to get an identity matrix
	 * @param size of the square matrix
	 * @return the identity matrix
	 */
    public static Matrix identity(int size) {
        Matrix matrix = new Matrix();
        if (size <= 0) {
            throw new MatrixException("invalid size : " + size + " ( <= 0)");
        } else {
            matrix.m = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix.m[i][j] = (i == j) ? 1 : 0;
                }
            }
        }
        return matrix;
    }

    /**
	 * add two matrices
	 * @param a
	 * @return the addition of the two matrices
	 */
    public Matrix add(Matrix a) {
        if (m.length != a.m.length || m[0].length != a.m[0].length) {
            throw new MatrixException("uncompatible matrix for addition: " + a.m.length + "x" + a.m[0].length + " and " + m.length + "x" + m[0].length);
        }
        Matrix matrix = new Matrix();
        matrix.m = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                matrix.m[i][j] = m[i][j] + a.m[i][j];
            }
        }
        return matrix;
    }

    /**
	 * substract two matrices
	 * @param a
	 * @return the substraction of the two matrices
	 */
    public Matrix substract(Matrix a) {
        if (m.length != a.m.length || m[0].length != a.m[0].length) {
            throw new MatrixException("uncompatible matrix for substraction: " + a.m.length + "x" + a.m[0].length + " and " + m.length + "x" + m[0].length);
        }
        Matrix matrix = new Matrix();
        matrix.m = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                matrix.m[i][j] = m[i][j] - a.m[i][j];
            }
        }
        return matrix;
    }

    /**
	 * multiply a matrix by a number
	 * @param a
	 * @return the multiplied matrix
	 */
    public Matrix multiply(double a) {
        Matrix matrix = new Matrix();
        matrix.m = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                matrix.m[i][j] = m[i][j] * a;
            }
        }
        return matrix;
    }

    /**
	 * multiply two matrices
	 * @param a
	 * @return the multiplication of the two matrices
	 */
    public Matrix multiply(Matrix a) {
        Matrix matrix = new Matrix();
        if (a.m[0].length != m.length) {
            throw new MatrixException("uncompatible matrix for multiplication: " + m.length + "x" + m[0].length + " and " + a.m.length + "x" + a.m[0].length);
        } else {
            matrix.m = new double[a.m.length][m[0].length];
            for (int i = 0; i < m[0].length; i++) {
                for (int j = 0; j < a.m.length; j++) {
                    double sum = 0;
                    for (int k = 0; k < m.length; k++) {
                        sum = sum + m[k][i] * a.m[j][k];
                    }
                    matrix.m[j][i] = sum;
                }
            }
        }
        return matrix;
    }

    /**
	 * transpose the matrix 
	 * @return the transposed matrix
	 */
    public Matrix transpose() {
        Matrix matrix = new Matrix();
        matrix.m = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                matrix.m[j][i] = m[i][j];
            }
        }
        return matrix;
    }

    /**
	 * calculate the determinant of the square matrix using the cofactors method
	 * @return the determinant
	 */
    public double det() {
        if (m.length != m[0].length) {
            throw new MatrixException("determinant of a non-square Matrix: " + m.length + "x" + m[0].length);
        }
        double sum = 0;
        if (m.length == 1) {
            sum = m[0][0];
        } else {
            for (int i = 0; i < m.length; i++) {
                sum += m[i][0] * cofactor(0, i);
            }
        }
        return sum;
    }

    /**
	 * calculate the inverse matrix using the cofactors method
	 * the matrix must be square and non-singular
	 * @return the inverse matrix
	 */
    public Matrix inverse() {
        if (m.length != m[0].length) {
            throw new MatrixException("inverse of a non-square Matrix: " + m.length + "x" + m[0].length);
        }
        double d = det();
        if (d == 0) {
            throw new MatrixException("inverse of a singular Matrix: " + m.length + "x" + m[0].length);
        }
        Matrix matrix = new Matrix();
        matrix.m = new double[m.length][m.length];
        if (m.length == 1) {
            matrix.m[0][0] = 1 / m[0][0];
            return matrix;
        } else {
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    matrix.m[i][j] = cofactor(j, i);
                }
            }
            return matrix.multiply(1 / d);
        }
    }

    /**
	 * Get the value of the element at the given location
	 * @param row 
	 * @param col
	 * @return the value
	 */
    public double get(int row, int col) {
        if (row >= 0 && col >= 0 && row < m[0].length && col < m.length) {
            return m[col][row];
        } else {
            throw new MatrixException("index out of range : " + row + "x" + col + " in " + m[0].length + "x" + m.length);
        }
    }

    /**
	 * Set the value of the element at the given location
	 * @param row
	 * @param col
	 * @param value
	 */
    public void set(int row, int col, double value) {
        if (row >= 0 && col >= 0 && row < m[0].length && col < m.length) {
            m[col][row] = value;
        } else {
            throw new MatrixException("index out of range : " + row + "x" + col + " in " + m[0].length + "x" + m.length);
        }
    }

    /**
	 * Get the number of rows
	 * @return number of rows
	 */
    public int getRowCount() {
        return m[0].length;
    }

    /**
	 * Get the number of columns
	 * @return number of columns
	 */
    public int getColCount() {
        return m.length;
    }

    /**
	 * return a simple representation of the matrix
	 */
    public String toString() {
        String s = "Matrix(" + m[0].length + "x" + m.length + "): ";
        for (int i = 0; i < m[0].length; i++) {
            s += "[ ";
            for (int j = 0; j < m.length; j++) {
                s += m[j][i] + " ";
            }
            s += "] ";
        }
        return s;
    }

    /**
	 * deep copy of the matrix
	 * @return a new matrix
	 */
    public Matrix clone() {
        Matrix matrix = new Matrix();
        matrix.m = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                matrix.m[i][j] = m[i][j];
            }
        }
        return matrix;
    }

    /**
	 * calculate the cofactor of the given element
	 * Never call <code>cofactor</code> on a Matrix with only 1 column or only 1 row
	 * @param row
	 * @param col
	 * @return the cofactor
	 */
    private double cofactor(int row, int col) {
        Matrix matrix = new Matrix();
        matrix.m = new double[m.length - 1][m[0].length - 1];
        int k = -1;
        for (int i = 0; i < m.length - 1; i++) {
            k = (k == col - 1) ? k + 2 : k + 1;
            int l = -1;
            for (int j = 0; j < m[0].length - 1; j++) {
                l = (l == row - 1) ? l + 2 : l + 1;
                matrix.m[i][j] = m[k][l];
            }
        }
        return matrix.det() * (((row + col) % 2 == 0) ? 1 : -1);
    }
}
