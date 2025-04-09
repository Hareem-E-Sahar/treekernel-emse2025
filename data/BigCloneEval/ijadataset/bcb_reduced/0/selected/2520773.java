package annas.math;

import java.text.DecimalFormat;
import java.util.concurrent.Semaphore;

public class Matrix {

    private boolean DEBUG = false;

    private boolean INFO = false;

    public boolean useParallel = false;

    public int MAXTHREADS = 10;

    private int iDF = 0;

    private float[][] matrix;

    public Matrix(Matrix m) {
        this.matrix = m.getMatrix();
    }

    public Matrix(int size) {
        matrix = new float[size][size];
    }

    public Matrix(int sizeX, int sizeY) {
        matrix = new float[sizeY][sizeX];
    }

    public Matrix(float[][] m) {
        matrix = m;
    }

    public Matrix(double[][] m) {
        this.matrix = new float[m.length][m[0].length];
        if (m.length == matrix.length && m[0].length == matrix[0].length) {
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    this.matrix[i][j] = (float) m[i][j];
                }
            }
        }
    }

    public Matrix(int[][] m) {
        this.matrix = new float[m.length][m[0].length];
        if (m.length == matrix.length && m[0].length == matrix[0].length) {
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    this.matrix[i][j] = m[i][j];
                }
            }
        }
    }

    public void SetMatrix(float[][] m) {
        if (m.length == matrix.length && m[0].length == matrix[0].length) {
            this.matrix = m;
        }
    }

    public float[][] getMatrix() {
        return this.matrix;
    }

    public static Matrix createIdentity(int i) {
        float[][] g = new float[i][i];
        for (int j = 0; j < i; j++) {
            for (int k = 0; k < i; k++) {
                if (j == k) {
                    g[j][k] = 1;
                } else {
                    g[j][k] = 0;
                }
            }
        }
        return new Matrix(g);
    }

    public void print() {
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("{");
            for (int j = 0; j < matrix[0].length; j++) {
                if (j < matrix[0].length - 1) {
                    System.out.print(" " + matrix[i][j] + ",");
                } else {
                    System.out.print(" " + matrix[i][j]);
                }
            }
            System.out.println(" }");
        }
    }

    public void printformatted(String Pattern) {
        DecimalFormat Formatter = new DecimalFormat(Pattern);
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("{");
            for (int j = 0; j < matrix[0].length; j++) {
                if (j < matrix[0].length - 1) {
                    System.out.print(" " + Formatter.format(matrix[i][j]) + ",");
                } else {
                    System.out.print(" " + Formatter.format(matrix[i][j]));
                }
            }
            System.out.println(" }");
        }
    }

    public Matrix MultiplyMatrix(Matrix mb) throws Exception {
        if (this.useParallel) {
            return this.ParMultiplyMatrix(mb, 100, 100);
        } else {
            return this.SeqMultiplyMatrix(mb);
        }
    }

    public Matrix pow(int power) {
        Matrix retval = new Matrix(this.matrix);
        for (int i = 0; i < power; i++) {
            try {
                retval = retval.MultiplyMatrix(retval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retval;
    }

    private Matrix ParMultiplyMatrix(Matrix Mb, int granX, int granY) throws Exception {
        float[][] b = Mb.getMatrix();
        float[][] a = this.matrix;
        if (a[0].length != b.length) throw new Exception("Matrices incompatible for multiplication");
        float matrix[][] = new float[a.length][b[0].length];
        for (int i = 0; i < a.length; i = i + granX) for (int j = 0; j < b[i].length; j = j + granY) matrix[i][j] = 0;
        Matrix res = new Matrix(matrix);
        Semaphore s = new Semaphore(matrix.length);
        for (int i = 0; i < matrix.length / granX; i++) {
            for (int j = 0; j < matrix[0].length / granY; j++) {
                Thread f = new Product(a, i * granX, b, j * granY, res, s, granX, granY);
                f.start();
            }
        }
        s.acquire(matrix.length);
        return new Matrix(matrix);
    }

    private Matrix SeqMultiplyMatrix(Matrix Mb) throws Exception {
        float[][] b = Mb.getMatrix();
        float[][] a = this.matrix;
        if (a[0].length != b.length) throw new Exception("Matrices incompatible for multiplication");
        float matrix[][] = new float[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) for (int j = 0; j < b[i].length; j++) matrix[i][j] = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = calculateRowColumnProduct(a, i, b, j);
            }
        }
        return new Matrix(matrix);
    }

    public float calculateRowColumnProduct(float[][] A, int row, float[][] B, int col) {
        float product = 0;
        for (int i = 0; i < A[row].length; i++) product += A[row][i] * B[i][col];
        return product;
    }

    public Matrix Transpose() {
        float[][] a = this.matrix;
        if (INFO) {
            System.out.println("Performing Transpose...");
        }
        float m[][] = new float[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) for (int j = 0; j < a[i].length; j++) m[j][i] = a[i][j];
        return new Matrix(m);
    }

    private float[][] Transpose(float[][] a) {
        if (INFO) {
            System.out.println("Performing Transpose...");
        }
        float m[][] = new float[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) for (int j = 0; j < a[i].length; j++) m[j][i] = a[i][j];
        return m;
    }

    public Matrix Inverse() throws Exception {
        float[][] a = this.copyfloatArray(this.matrix);
        if (INFO) {
            System.out.println("Performing Inverse...");
        }
        int tms = a.length;
        float m[][] = new float[tms][tms];
        float mm[][] = Adjoint(a);
        float det = Determinant(a);
        float dd = 0;
        if (det == 0) {
            if (INFO) {
                System.out.println("Determinant Equals 0, Not Invertible.");
            }
        } else {
            dd = 1 / det;
        }
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            m[i][j] = dd * mm[i][j];
        }
        return new Matrix(m);
    }

    public Matrix Adjoint() throws Exception {
        float[][] a = this.matrix;
        if (INFO) {
            System.out.println("Performing Adjoint...");
        }
        int tms = a.length;
        float m[][] = new float[tms][tms];
        int ii, jj, ia, ja;
        float det;
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            ia = ja = 0;
            float ap[][] = new float[tms - 1][tms - 1];
            for (ii = 0; ii < tms; ii++) {
                for (jj = 0; jj < tms; jj++) {
                    if ((ii != i) && (jj != j)) {
                        ap[ia][ja] = a[ii][jj];
                        ja++;
                    }
                }
                if ((ii != i) && (jj != j)) {
                    ia++;
                }
                ja = 0;
            }
            det = Determinant(ap);
            m[i][j] = (float) Math.pow(-1, i + j) * det;
        }
        m = Transpose(m);
        return new Matrix(m);
    }

    private float[][] Adjoint(float[][] a) throws Exception {
        if (INFO) {
            System.out.println("Performing Adjoint...");
        }
        int tms = a.length;
        float m[][] = new float[tms][tms];
        int ii, jj, ia, ja;
        float det;
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            ia = ja = 0;
            float ap[][] = new float[tms - 1][tms - 1];
            for (ii = 0; ii < tms; ii++) {
                for (jj = 0; jj < tms; jj++) {
                    if ((ii != i) && (jj != j)) {
                        ap[ia][ja] = a[ii][jj];
                        ja++;
                    }
                }
                if ((ii != i) && (jj != j)) {
                    ia++;
                }
                ja = 0;
            }
            det = Determinant(ap);
            m[i][j] = (float) Math.pow(-1, i + j) * det;
        }
        m = Transpose(m);
        return m;
    }

    public float[][] UpperTriangle() {
        float[][] m = this.matrix;
        if (INFO) {
            System.out.println("Converting to Upper Triangle...");
        }
        float f1 = 0;
        float temp = 0;
        int tms = m.length;
        int v = 1;
        iDF = 1;
        for (int col = 0; col < tms - 1; col++) {
            for (int row = col + 1; row < tms; row++) {
                v = 1;
                outahere: while (m[col][col] == 0) {
                    if (col + v >= tms) {
                        iDF = 0;
                        break outahere;
                    } else {
                        for (int c = 0; c < tms; c++) {
                            temp = m[col][c];
                            m[col][c] = m[col + v][c];
                            m[col + v][c] = temp;
                        }
                        v++;
                        iDF = iDF * -1;
                    }
                }
                if (m[col][col] != 0) {
                    if (DEBUG) {
                        System.out.println("tms = " + tms + "   col = " + col + "   row = " + row);
                    }
                    try {
                        f1 = (-1) * m[row][col] / m[col][col];
                        for (int i = col; i < tms; i++) {
                            m[row][i] = f1 * m[col][i] + m[row][i];
                        }
                    } catch (Exception e) {
                        System.out.println("Still Here!!!");
                    }
                }
            }
        }
        return m;
    }

    private float[][] UpperTriangle(float[][] m) {
        if (INFO) {
            System.out.println("Converting to Upper Triangle...");
        }
        float f1 = 0;
        float temp = 0;
        int tms = m.length;
        int v = 1;
        iDF = 1;
        for (int col = 0; col < tms - 1; col++) {
            for (int row = col + 1; row < tms; row++) {
                v = 1;
                outahere: while (m[col][col] == 0) {
                    if (col + v >= tms) {
                        iDF = 0;
                        break outahere;
                    } else {
                        for (int c = 0; c < tms; c++) {
                            temp = m[col][c];
                            m[col][c] = m[col + v][c];
                            m[col + v][c] = temp;
                        }
                        v++;
                        iDF = iDF * -1;
                    }
                }
                if (m[col][col] != 0) {
                    if (DEBUG) {
                        System.out.println("tms = " + tms + "   col = " + col + "   row = " + row);
                    }
                    try {
                        f1 = (-1) * m[row][col] / m[col][col];
                        for (int i = col; i < tms; i++) {
                            m[row][i] = f1 * m[col][i] + m[row][i];
                        }
                    } catch (Exception e) {
                        System.out.println("Still Here!!!");
                    }
                }
            }
        }
        return m;
    }

    public float Determinant() {
        float[][] matrix = this.matrix;
        if (INFO) {
            System.out.println("Getting Determinant...");
        }
        int tms = matrix.length;
        float det = 1;
        matrix = UpperTriangle(matrix);
        for (int i = 0; i < tms; i++) {
            det = det * matrix[i][i];
        }
        det = det * iDF;
        if (INFO) {
            System.out.println("Determinant: " + det);
        }
        return det;
    }

    private float Determinant(float[][] matrix) {
        if (INFO) {
            System.out.println("Getting Determinant...");
        }
        int tms = matrix.length;
        float det = 1;
        matrix = UpperTriangle(matrix);
        for (int i = 0; i < tms; i++) {
            det = det * matrix[i][i];
        }
        det = det * iDF;
        if (INFO) {
            System.out.println("Determinant: " + det);
        }
        return det;
    }

    public Matrix addMatrix(Matrix m) throws Exception {
        if ((m.matrix.length == this.matrix.length) && (m.matrix[0].length == this.matrix[0].length)) {
            Matrix retval = new Matrix(this.matrix.length);
            for (int i = 0; i < this.matrix[0].length; i++) {
                for (int j = 0; j < this.matrix.length; j++) {
                    retval.matrix[i][j] = this.matrix[i][j] + m.matrix[i][j];
                }
            }
            return retval;
        } else {
            throw new Exception();
        }
    }

    public Matrix subtractMatrix(Matrix m) throws Exception {
        if ((m.matrix.length == this.matrix.length) && (m.matrix[0].length == this.matrix[0].length)) {
            Matrix retval = new Matrix(this.matrix.length);
            for (int i = 0; i < this.matrix[0].length; i++) {
                for (int j = 0; j < this.matrix.length; j++) {
                    retval.matrix[i][j] = this.matrix[i][j] - m.matrix[i][j];
                }
            }
            return retval;
        } else {
            throw new Exception();
        }
    }

    public float Trace() {
        float retval = 0;
        if (this.matrix.length == this.matrix[0].length) {
            for (int i = 0; i < this.matrix.length; i++) {
                retval += this.matrix[i][i];
            }
        } else {
            throw new RuntimeException("Matrix must be square");
        }
        return retval;
    }

    public Matrix divide(Matrix j) {
        try {
            Matrix mi = this.Inverse();
            mi = mi.MultiplyMatrix(j);
            return mi;
        } catch (Exception e) {
            throw new RuntimeException("Invalid Matrix operation.");
        }
    }

    /**
	 * Performs a deep equals
	 * @param B
	 * @return
	 */
    public boolean eq(Matrix B) {
        return this.eq(B, 0.0);
    }

    /**
	 * Performs a deep equals
	 * @param B
	 * @return
	 */
    public boolean eq(Matrix B, double tolerance) {
        Matrix A = this;
        if (B.getMatrix().length != getMatrix().length || B.getMatrix()[0].length != getMatrix()[0].length) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < this.getMatrix().length; i++) for (int j = 0; j < this.getMatrix()[0].length; j++) if (Math.abs(A.getMatrix()[i][j] - B.getMatrix()[i][j]) > tolerance) return false;
        return true;
    }

    private float[][] copyfloatArray(float[][] nums) {
        float[][] copy = new float[nums.length][];
        for (int i = 0; i < nums.length; i++) {
            copy[i] = new float[nums[i].length];
            for (int j = 0; j < nums[i].length; j++) {
                copy[i][j] = nums[i][j];
            }
        }
        return copy;
    }

    public void zerodiag() {
        if (this.matrix.length == this.matrix[0].length) {
            for (int i = 0; i < this.matrix.length; i++) {
                this.matrix[i][i] = 0;
            }
        }
    }

    public class Product extends Thread {

        private float[][] A;

        private int row;

        private float[][] B;

        private int col;

        private Matrix res;

        private Semaphore s;

        private int width;

        private int height;

        /**
		 * @param a
		 * @param row
		 * @param b
		 * @param col
		 * @param res
		 */
        public Product(float[][] a, int row, float[][] b, int col, Matrix res, Semaphore s, int width, int height) {
            super();
            this.A = a;
            this.row = row;
            this.B = b;
            this.col = col;
            this.res = res;
            this.s = s;
            this.width = width;
            this.height = height;
            try {
                this.s.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            for (int i = this.row; i < this.row + this.width; i++) {
                for (int j = this.col; j < this.col + this.height; j++) {
                    this.res.matrix[i][j] = this.calculateRowColumnProduct(A, i, B, j);
                }
            }
            this.s.release();
        }

        public float calculateRowColumnProduct(float[][] A, int row, float[][] B, int col) {
            float product = 0;
            for (int i = 0; i < A[row].length; i++) product += A[row][i] * B[i][col];
            return product;
        }
    }

    @Override
    public String toString() {
        String retval = "";
        for (int i = 0; i < matrix.length; i++) {
            retval = retval + "{ ";
            for (int j = 0; j < matrix[0].length; j++) {
                if (j < matrix[0].length - 1) {
                    retval = retval + (" " + matrix[i][j] + ",");
                } else {
                    retval = retval + (" " + matrix[i][j]);
                }
            }
            retval = retval + (" }\n");
        }
        return retval;
    }
}
