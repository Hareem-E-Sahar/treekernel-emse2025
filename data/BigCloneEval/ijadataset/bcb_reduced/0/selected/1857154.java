package maths.matrix.real;

/**
 * @author Lars
 *
 */
public class MathRealMatrix {

    private int M;

    private int N;

    private double[][] Data;

    public static final int ONE_MATRIX = 1;

    public static final int UNIFIED_MATRIX = 2;

    /**
	 * creates a new 0-Matrix with gives size 
	 * @param m
	 * @param n
	 */
    protected MathRealMatrix() {
    }

    public MathRealMatrix(int m, int n) {
        this.Data = new double[m][n];
        this.M = m;
        this.N = n;
    }

    /**
	 * creates a new specified Matrix, like 1 Matrix or, unified Matrix
	 * @param m
	 * @param n
	 * @param SPEC
	 */
    public MathRealMatrix(int m, int n, int SPEC) throws IllegalArgumentException {
        this.M = m;
        this.N = n;
        if (SPEC == ONE_MATRIX) {
            this.Data = new double[m][n];
            for (int ni = 0; ni < n; ni++) {
                for (int mi = 0; mi < m; mi++) {
                    this.Data[mi][ni] = 1;
                }
            }
        } else if (SPEC == UNIFIED_MATRIX) {
            if (!this.isQuadratic()) {
                throw new IllegalArgumentException("Matrix must be quadratic");
            } else {
                this.Data = new double[m][n];
                for (int i = 0; i < m; i++) {
                    this.Data[i][i] = 1;
                }
            }
        } else {
            throw new IllegalArgumentException("SPEC must be one of the given constants");
        }
    }

    /**
	 * 
	 * @param data
	 */
    public MathRealMatrix(double[][] data) {
        this.setM(data.length);
        this.setN(data[0].length);
        this.setData(data);
    }

    /**
	 * 
	 * @param m
	 * @param n
	 * @return Item at position m|n
	 */
    public double getItem(int m, int n) {
        return this.Data[m][n];
    }

    /**
	 * 
	 * @param m
	 * @return Row Vector at position m
	 */
    public double[] getRowVector(int m) {
        double[] rowVector = new double[this.N];
        for (int n = 0; n < this.N; n++) {
            rowVector[n] = this.Data[m][n];
        }
        return rowVector;
    }

    /**
	 * 
	 * @param n
	 * @return Column Vector at position n
	 */
    public double[] getColumnVector(int n) {
        double[] lineVector = new double[this.M];
        for (int m = 0; m < this.M; m++) {
            lineVector[m] = this.Data[m][n];
        }
        return lineVector;
    }

    public double[][] getAllItems() {
        return this.Data;
    }

    /**
	 * @return the m
	 */
    public int getM() {
        return M;
    }

    /**
	 * @return the n
	 */
    public int getN() {
        return N;
    }

    /**
	 * 
	 * @param matrixA
	 * @param matrixB
	 * @return addition matrix with another one
	 * @throws IllegalArgumentException
	 */
    public MathRealMatrix add(MathRealMatrix matrixB) throws IllegalArgumentException {
        if (this.getM() != matrixB.getM() && this.N != matrixB.getN()) {
            throw new IllegalArgumentException("Matrices must have equal dimensions, for adding");
        } else {
            int m = this.M;
            int n = this.N;
            double[][] dataA = this.Data;
            double[][] dataB = matrixB.getAllItems();
            double[][] newData = new double[m][n];
            for (int ni = 0; ni < n; ni++) {
                for (int mi = 0; mi < m; mi++) {
                    newData[mi][ni] = dataA[mi][ni] + dataB[mi][ni];
                }
            }
            return new MathRealMatrix(newData);
        }
    }

    /**
	 * 
	 * @param scalar
	 * @return mutliplication of matrix with a scalar
	 */
    public MathRealMatrix scalarMultiplication(double scalar) {
        int m = this.M;
        int n = this.N;
        double[][] data = this.Data;
        double[][] newData = new double[m][n];
        for (int ni = 0; ni < n; ni++) {
            for (int mi = 0; mi < m; mi++) {
                newData[mi][ni] = data[mi][ni] * scalar;
            }
        }
        return new MathRealMatrix(newData);
    }

    /**
	 * 
	 * @param matrixB
	 * @return multiplikation of matrix with another matrix
	 * @throws IllegalArgumentException
	 */
    public MathRealMatrix matrixMultiplication(MathRealMatrix matrixB) throws IllegalArgumentException {
        if (this.N != matrixB.getM()) {
            throw new IllegalArgumentException("MatrixB must have the same count of rows as the count of coloumns of MatrixA");
        } else {
            int am = this.M;
            int bn = matrixB.getN();
            int nm = am;
            int nn = bn;
            double[][] newData = new double[nm][nn];
            for (int nni = 0; nni < nn; nni++) {
                for (int nmi = 0; nmi < nm; nmi++) {
                    newData[nmi][nni] = MultiplyVectors(this.getRowVector(nmi), matrixB.getColumnVector(nni));
                }
            }
            return new MathRealMatrix(newData);
        }
    }

    /**
	 * 
	 * @param matrix
	 * @return transposition of matrix
	 */
    public MathRealMatrix transpose() {
        int m = this.M;
        int n = this.N;
        double[][] mData = this.Data;
        double[][] newData = new double[n][m];
        for (int ni = 0; ni < n; ni++) {
            for (int mi = 0; mi < m; mi++) {
                newData[ni][mi] = mData[mi][ni];
            }
        }
        return new MathRealMatrix(newData);
    }

    /**
	 * @param rowVector
	 * @param coloumnVector
	 * @return Sum of vr[i]*vc[i]
	 */
    private double MultiplyVectors(double[] rowVector, double[] coloumnVector) {
        double result = 0;
        for (int i = 0; i < rowVector.length; i++) {
            result += rowVector[i] * coloumnVector[i];
        }
        return result;
    }

    /**
	 * @param m the m to set
	 */
    private void setM(int m) {
        this.M = m;
    }

    /**
	 * @param n the n to set
	 */
    private void setN(int n) {
        this.N = n;
    }

    /**
	 * @param data the data to set
	 */
    private void setData(double[][] data) {
        this.Data = data;
    }

    /**
	 * @return the quadratic
	 */
    public boolean isQuadratic() {
        return this.M == this.N;
    }

    /**
	 * 
	 * @return true if matrix is symmetric, otherwise false
	 * @throws IllegalArgumentException
	 */
    public boolean isSymmetric() throws IllegalArgumentException {
        if (!this.isQuadratic()) {
            throw new IllegalArgumentException("Matrix must be quadratic!");
        } else {
            for (int ni = 0; ni < this.N; ni++) {
                for (int mi = 0; mi < this.M; mi++) {
                    if (mi == ni) {
                        continue;
                    } else if (this.Data[mi][ni] != this.Data[ni][mi]) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
	 *
	 * @return true if matrix is skew-symmetric, otherwise false
	 * @throws IllegalArgumentException
	 */
    public boolean isSkewSymmetric() throws IllegalArgumentException {
        if (!this.isQuadratic()) {
            throw new IllegalArgumentException("Matrix must be quadratic");
        } else {
            for (int ni = 0; ni < this.N; ni++) {
                for (int mi = 0; mi < this.M; mi++) {
                    if (mi == ni) {
                        continue;
                    } else if (this.Data[mi][ni] != -this.Data[ni][mi]) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public boolean equals(MathRealMatrix matrixToCompare) {
        boolean result = true;
        double[][] compData = matrixToCompare.getAllItems();
        if (this.M != matrixToCompare.getM() && this.N != matrixToCompare.getN()) {
            return false;
        } else {
            for (int ni = 0; ni < this.N; ni++) {
                for (int mi = 0; mi < this.M; mi++) {
                    if (this.Data[mi][ni] != compData[mi][ni]) {
                        result = false;
                        return false;
                    } else {
                        result = true;
                    }
                }
            }
            return result;
        }
    }

    /**
	 * calculates the Hadamard-Product of this an a second matrix
	 * @param matrix
	 * @return resulting matrix
	 * @throws IllegalArgumentException
	 */
    public MathRealMatrix hadamardProduct(MathRealMatrix matrix) throws IllegalArgumentException {
        if (matrix.getM() != this.M || matrix.getN() != this.N) {
            throw new IllegalArgumentException("Matrices must have the same domensions!");
        } else {
            double[][] matrixData = matrix.getAllItems();
            double[][] newData = new double[this.M][this.N];
            for (int ni = 0; ni < this.N; ni++) {
                for (int mi = 0; mi < this.M; mi++) {
                    newData[mi][ni] = this.Data[mi][ni] * matrixData[mi][ni];
                }
            }
            MathRealMatrix resultMatrix = new MathRealMatrix(newData);
            return resultMatrix;
        }
    }

    /**
	 * calculates the Kronecker-Product of this and a second matrix;
	 * @param matrix
	 * @return resulting matrix
	 * @throws IllegalArgumentException
	 */
    public MathRealMatrix kroneckerProduct(MathRealMatrix matrix) throws IllegalArgumentException {
        MathRealMatrix resultMatrix = new MathRealMatrix((this.M * matrix.M), (this.N * matrix.N));
        double[][] newData = resultMatrix.getAllItems();
        double[][] matrixData = matrix.getAllItems();
        System.out.println(this.toString());
        System.out.println(matrix.toString());
        int x = 0;
        int y = 0;
        int s = 0;
        int t = 0;
        for (int mi = 0; mi < resultMatrix.getM(); mi++) {
            for (int ni = 0; ni < resultMatrix.getN(); ni++) {
                newData[mi][ni] = this.Data[y][x] * matrixData[s][t];
                t++;
                if (t == matrix.N) {
                    t = 0;
                    x++;
                }
                if (x == matrix.N) {
                    x = 0;
                    s++;
                }
                if (s == this.M) {
                    s = 0;
                    y++;
                }
                if (y == this.M) {
                    y = 0;
                }
            }
        }
        resultMatrix = new MathRealMatrix(newData);
        return resultMatrix;
    }

    /**
	 * calculates the rank of this
	 * @return rank
	 */
    public int rank() {
        return 0;
    }

    /**
	 * calculates the trace of this
	 * @return
	 * @throws IllegalArgumentException
	 */
    public double trace() throws IllegalArgumentException {
        if (!this.isQuadratic()) {
            throw new IllegalArgumentException("Matrix must be quadratic");
        } else {
            double result = 0;
            for (int i = 0; i < this.M; i++) {
                result += this.Data[i][i];
            }
            return result;
        }
    }

    /**
	 * calculates the determinant of this
	 * @return
	 * @throws IllegalArgumentException
	 */
    public double determinant() throws IllegalArgumentException {
        if (!this.isQuadratic()) {
            throw new IllegalArgumentException("Matrix must be quadratic");
        } else {
            if (this.M == 2) {
                return ((this.Data[0][0] * this.Data[1][1]) - (this.Data[0][1] * this.Data[1][0]));
            } else if (this.M == 3) {
                double result = 0;
                result += this.Data[0][0] * this.Data[1][1] * this.Data[2][2];
                result += this.Data[0][1] * this.Data[1][2] * this.Data[2][0];
                result += this.Data[0][2] * this.Data[1][0] * this.Data[2][1];
                result -= this.Data[0][2] * this.Data[1][1] * this.Data[2][0];
                result -= this.Data[0][1] * this.Data[1][0] * this.Data[2][2];
                result -= this.Data[0][0] * this.Data[1][2] * this.Data[2][1];
                return result;
            } else {
                return 0;
            }
        }
    }

    protected void setItem(int m, int n, double value) {
        this.Data[m][n] = value;
    }

    public String toString() {
        String result = "";
        for (int mi = 0; mi < this.M; mi++) {
            for (int ni = 0; ni < this.N; ni++) {
                if (this.Data[mi][ni] >= 0) {
                    result += "  ";
                } else {
                    result += " ";
                }
                result += this.Data[mi][ni];
            }
            result += "\n";
        }
        return result;
    }
}
