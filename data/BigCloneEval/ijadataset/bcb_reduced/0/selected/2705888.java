package VMatrixLib;

/**
    An implementation of basic matrix.
 */
public class BasicMatrix implements BasicMatrixInterface {

    /** Number of rows */
    protected int numRows;

    /** Number of cols */
    protected int numCols;

    /** Matrix's data */
    protected double[][] mData;

    /** Constructor */
    public BasicMatrix(int rows, int cols) {
        numRows = rows;
        numCols = cols;
    }

    /** Constructor */
    public BasicMatrix(int rows, int cols, double[][] data) {
        numRows = rows;
        numCols = cols;
        setData(data);
    }

    /** Constructor */
    public BasicMatrix(double[][] data) {
        numRows = data.length;
        numCols = data[0].length;
        setData(data);
    }

    /** Returns number of rows */
    public int rows() {
        return numRows;
    }

    /** Returns number of cols */
    public int cols() {
        return numCols;
    }

    /** Sets matrix's data */
    public void setData(double[][] data) {
        mData = data;
    }

    /** Sets a cell's value */
    public void set(int row, int col, double val) {
        mData[row][col] = val;
    }

    /** Returns cell's ('row','col') value */
    public double get(int row, int col) {
        return mData[row][col];
    }

    /** Returns true if rows == cols */
    public boolean isSquared() {
        return numRows == numCols;
    }

    /** Compute the product of this matrix with constant 'k' locally */
    public void localMul(double k) {
        double[][] data2 = mData;
        for (int i = 0; i < data2.length; i++) for (int j = 0; j < data2[i].length; j++) data2[i][j] *= k;
        mData = data2;
    }

    /** Compute the product of this matrix with matrix 'mat' locally */
    public void localMul(BasicMatrixInterface mat) {
        if (cols() != mat.rows()) return;
        int resR = rows();
        int resC = mat.cols();
        double data[][] = new double[resR][resC];
        for (int i = 0; i < resR; i++) for (int j = 0; j < resC; j++) for (int k = 0; k < cols(); k++) data[i][j] += get(i, k) * mat.get(k, j);
        mData = data;
        numRows = resR;
        numCols = resC;
    }

    /** Transposes the matrix locally */
    public void localTranspose() {
        double[][] data = new double[cols()][rows()];
        for (int i = 0; i < rows(); i++) for (int j = 0; j < cols(); j++) data[j][i] = mData[i][j];
        int t = rows();
        numRows = cols();
        numCols = t;
        mData = data;
    }

    /** Compute the sum with matrix 'm2' locally */
    public void localSum(BasicMatrixInterface m2) {
        if (m2.rows() != numRows || m2.cols() != numCols) return;
        double d[][] = mData;
        for (int i = 0; i < d.length; i++) for (int j = 0; j < d[i].length; j++) d[i][j] += m2.get(i, j);
        mData = d;
    }

    /** 
     * Computes the product of this matrix with constant 'k'
     * and returns the resulting matrix
     */
    public BasicMatrix mul(double k) {
        BasicMatrix m = new BasicMatrix(mData);
        m.localMul(k);
        return m;
    }

    /**
     * Computes the product of the matrix with the matrix 'm2' and
     * returns the resulting matrix
     */
    public BasicMatrix mul(BasicMatrixInterface m2) {
        if (cols() != m2.rows()) return null;
        BasicMatrix m = new BasicMatrix(mData);
        m.localMul(m2);
        return m;
    }

    /**
     * Computes the sum of the matrix with the matrix 'm2' and
     * returns the resulting matrix
     */
    public BasicMatrix sum(BasicMatrixInterface m2) {
        if (m2.rows() != numRows || m2.cols() != numCols) return null;
        BasicMatrix m = new BasicMatrix(mData);
        m.localSum(m2);
        return m;
    }

    /** Returns the current matrix transposed */
    public BasicMatrix transpose() {
        BasicMatrix m = new BasicMatrix(mData);
        m.localTranspose();
        return m;
    }
}
