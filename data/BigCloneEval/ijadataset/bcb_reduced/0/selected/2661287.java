package com.abb.util.booleanmatrix;

import java.awt.Dimension;

/** Implements a matrix of boolean values. Multiplication is defined as AND,
    addition as OR.

    @author Axel Uhl
    @version $Id: BooleanMatrix.java,v 1.4 2001/01/06 18:57:12 aul Exp $
  */
public class BooleanMatrix {

    /** creates an n x m matrix and sets all elements to <tt>false</tt> */
    public BooleanMatrix(int n, int m) {
        theArray = new boolean[n][m];
    }

    /** creates an n x m matrix and initializes it with the given array
        @param a the array to initialize the matrix with
      */
    public BooleanMatrix(boolean[][] a) {
        theArray = a;
    }

    /** creates a matrix the same size as the one given and takes over the values
	from the given one (kind of a copy constructor)
	
	@param m the matrix to initialize from
      */
    public BooleanMatrix(BooleanMatrix m) {
        theArray = new boolean[m.getDim().width][m.getDim().height];
        for (int i = 0; i < theArray.length; i++) System.arraycopy(m.theArray[i], 0, theArray[i], 0, m.theArray[i].length);
    }

    /** @return the dimensions of the matrix */
    public Dimension getDim() {
        int y = 0;
        if (theArray.length > 0) y = theArray[0].length;
        return new Dimension(theArray.length, y);
    }

    /** sets an element of the matrix
        @param row the row where to set an element, starting with 0
        @param col the column where to set an element, starting with 0
        @param value the value to set at the specified position
        @exception booleanMatrix.MatrixException in case the indices are outside
                   of the matrix array boundaries
      */
    public synchronized void set(int row, int col, boolean value) throws MatrixException {
        try {
            theArray[row][col] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MatrixException(e.getMessage());
        }
    }

    /** returns the value of the specified matrix element
        @param row the row where to look, starting with 0
        @param col the column where to look, starting with 0
        @return the element at the specified position
        @exception booleanMatrix.MatrixException in case the indices are outside
                   of the matrix array boundaries
      */
    public synchronized boolean get(int row, int col) throws MatrixException {
        boolean result = false;
        try {
            result = theArray[row][col];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MatrixException(e.getMessage());
        }
        return result;
    }

    /** transposes the matrix by mirroring it at its diagonal
	@exception booleanMatrix.MatrixException in case the matrix is not square
      */
    public synchronized void transpose() throws MatrixException {
        if (getDim().width != getDim().height) throw new MatrixException("transpose only for square matrixes");
        boolean[][] newArray = new boolean[getDim().width][getDim().height];
        for (int i = 0; i < theArray.length; i++) for (int j = 0; j < theArray[i].length; j++) newArray[j][i] = theArray[i][j];
        theArray = newArray;
    }

    /** squares the matrix
        @exception booleanMatrix.MatrixException in case the matrix is
                   not square (n x n)
      */
    public synchronized void square() throws MatrixException {
        multiply(this);
    }

    /** multiplies the given matrix to this matrix
    	@param the matrix to multiply with, has to be m x p
        @exception booleanMatrix.MatrixException in case the dimensions don't
                   match
      */
    public synchronized void multiply(BooleanMatrix m) throws MatrixException {
        if (getDim().width != m.getDim().height) throw new MatrixException("trying to multiply " + getDim().height + "x" + getDim().width + " with " + m.getDim().height + "x" + m.getDim().width);
        boolean[][] resultArray = new boolean[getDim().height][m.getDim().width];
        for (int i = 0; i < getDim().height; i++) {
            for (int j = 0; j < m.getDim().width; j++) {
                for (int k = 0; k < getDim().width; k++) resultArray[i][j] |= theArray[i][k] && m.theArray[k][j];
            }
        }
        theArray = resultArray;
    }

    /** adds the given matrix to this matrix
    	@param the matrix to add, has to be n x m
        @exception booleanMatrix.MatrixException in case the dimensions don't
                   match
      */
    public synchronized void add(BooleanMatrix m) throws MatrixException {
        if (getDim().width != m.getDim().height) throw new MatrixException("trying to multiply " + getDim().height + "x" + getDim().width + " with " + m.getDim().height + "x" + m.getDim().width);
        boolean[][] resultArray = new boolean[getDim().height][getDim().width];
        for (int i = 0; i < getDim().height; i++) {
            for (int j = 0; j < getDim().width; j++) resultArray[i][j] = theArray[i][j] || m.theArray[i][j];
        }
        theArray = resultArray;
    }

    /** transitive closure: repeat squaring the matrix until the
        result doesn't change anymore

        @exception booleanMatrix.MatrixException in case the matrix is not
                   of square format (n x x)
      */
    public synchronized void transitiveClosure() throws MatrixException {
        boolean[][] oldArray;
        do {
            oldArray = theArray;
            square();
        } while (!arraysEqual(oldArray, theArray));
    }

    /** compares the contents of two arrays. The arrays are considered
        equal if both have equal dimensions and the contents are equal.
      */
    private boolean arraysEqual(boolean[][] a1, boolean[][] a2) {
        int y1 = 0;
        int y2 = 0;
        if (a1.length > 0) y1 = a1[0].length;
        if (a2.length > 0) y2 = a2[0].length;
        if (a1.length == a2.length && y1 == y2) {
            for (int i = 0; i < a1.length; i++) for (int j = 0; j < a1[0].length; j++) if (a1[i][j] != a2[i][j]) return false;
            return true;
        } else return false;
    }

    /** outputs the matrix to the screen. <tt>true</tt> is represented
        by "1", <tt>false</tt> by "0".
      */
    public synchronized String toString() {
        StringBuffer resultBuffer = new StringBuffer();
        for (int i = 0; i < getDim().height; i++) {
            for (int j = 0; j < getDim().width; j++) resultBuffer.append(" " + (theArray[i][j] ? "1" : "0"));
            resultBuffer.append("\n");
        }
        return resultBuffer.toString();
    }

    /** the array holding the matrix elements */
    private boolean[][] theArray;
}
