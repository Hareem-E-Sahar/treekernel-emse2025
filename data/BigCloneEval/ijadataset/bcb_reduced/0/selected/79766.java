package etri;

/**
 * Matrix class
 * 
 * @author Doosik Kim
 */
public class Matrix {

    /**
	 * interface to be used at Matrix.pairFunction()
	 * @author Doosik Kim
	 * @see
	 * {@link Matrix#pairFunction(Matrix, Matrix.function)} 
	 */
    public interface function {

        /**
		 * function f to be used at Matrix.pairFunction()
		 * @param x input value
		 * @return output value
		 */
        public double f(double x);
    }

    /**
	 * logger
	 */
    static Logger _logger = Logger.getLogger(Matrix.class);

    /**
	 * return identity matrix
	 * @param size matrix size
	 * @return identity matrix
	 */
    public static Matrix identity(int size) {
        Matrix m = new Matrix(size, size);
        m.setIdentity();
        return m;
    }

    /**
	 * main function
	 * @param args
	 */
    public static void main(String[] args) {
        _logger.info("main()");
        test();
    }

    public static void test() {
        testParse();
        testCopy();
        testCalculus();
        testFunction();
    }

    public static void testCalculus() {
        _logger.info("testCalculus()");
        try {
            Matrix m1 = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
            _logger.debug("m1 = " + m1.toString());
            double[][] data = { { 4, 3 }, { 2, 1 } };
            Matrix m2 = new Matrix(data);
            _logger.debug("m2 = " + m2.toString());
            Matrix result = new Matrix();
            result.add(m1, m2);
            _logger.debug("m1 + m2 = " + result.toString());
            result.subtract(m1, m2);
            _logger.debug("m1 - m2 = " + result.toString());
            result.multiply(m1, m2);
            _logger.debug("m1 * m2 = " + result.toString());
            result.multiply(100.0f, m1);
            _logger.debug("100 * m1 = " + result.toString());
            result.transpose(m1);
            _logger.debug("t(m1) = " + result.toString());
            result.setValue(1.0f);
            _logger.debug("zero = " + result.toString());
            result.setIdentity();
            _logger.debug("i = " + result.toString());
            _logger.debug("m1 = " + m1.toString());
            Matrix rowVector = new Matrix("1,100");
            result.multiplyColumn(m1, rowVector);
            _logger.debug("mul-col(m1) = " + result.toString());
            Matrix columnVector = new Matrix("1;100");
            result.multiplyRow(m1, columnVector);
            _logger.debug("mul-row(m1) = " + result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testCopy() {
        _logger.info("testCopy()");
        try {
            Matrix m1 = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
            Matrix m2 = m1;
            Matrix m3 = new Matrix(m1);
            m1.cell()[0][0] = 100.0f;
            _logger.debug("c1 = " + m1.toString());
            _logger.debug("c2 = " + m2.toString());
            _logger.debug("c3 = " + m3.toString());
            Matrix ex = m1.extract(1, 1);
            _logger.debug("ex = " + ex.toString());
            ex = m1.extract(5, 1);
            _logger.debug("ex = " + ex.toString());
            ex = m1.extract(1, 5);
            _logger.debug("ex = " + ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testFunction() {
        _logger.info("testFunction()");
        try {
            Matrix m = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
            _logger.debug("m = " + m.toString());
            Matrix result = new Matrix();
            Matrix.function f = new Matrix.function() {

                public double f(double x) {
                    return x * x;
                }
            };
            result.pairFunction(f, m);
            _logger.debug("f(m) = " + result.toString());
            result.pairFunction(m, new Matrix.function() {

                public double f(double x) {
                    return -x * x;
                }
            });
            _logger.debug("f(m) = " + result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testInverse() {
        try {
            Matrix m = new Matrix(2, 2);
            _logger.debug("m = " + m.toString());
            Matrix result = new Matrix();
            result.inverse(m);
            _logger.debug("m' = " + result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testParse() {
        _logger.info("testParse()");
        try {
            Matrix m = new Matrix(2, 2);
            m.parse("1,2;3,4");
            _logger.debug("matrix = " + m.toString());
            m = new Matrix(2, 2, "-1,-2;-3,-4");
            _logger.debug("matrix = " + m.toString());
            m = new Matrix("-1,-2;3,4");
            _logger.debug("matrix = " + m.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * matrix row
	 */
    private int _row = 0;

    /**
	 * matrix column
	 */
    private int _column = 0;

    /**
	 * matrix data
	 */
    private double[][] _cell = null;

    /**
	 * contructor
	 */
    Matrix() {
    }

    /**
	 * constructor
	 * @param value double value to be initialized (two dimensional array)
	 */
    Matrix(double[][] value) {
        setCell(value);
    }

    /**
	 * constructor
	 * @param row matrix row
	 * @param column matrix column
	 */
    Matrix(int row, int column) {
        setSize(row, column);
    }

    /**
	 * constructor
	 * @param row matrix row
	 * @param column matrix column
	 * @param str matrix data of string format
	 * @throws Exception when parsin failed
	 */
    Matrix(int row, int column, String str) throws Exception {
        parse(row, column, str);
    }

    /**
	 * copy constructor
	 * @param m input matrix
	 */
    Matrix(Matrix m) {
        copy(m);
    }

    /**
	 * constructor
	 * @param str matrix data of string format
	 * @throws Exception when parsin failed
	 */
    Matrix(String str) throws Exception {
        parse(str);
    }

    /**
	 * add matrix
	 * @param a
	 * @param b
	 * @throws Exception
	 */
    public void add(Matrix a, Matrix b) throws Exception {
        if (a._row != b._row || a._column != b._column) throw new Exception("invalid size: " + a.getSizeInfo() + "+" + b.getSizeInfo());
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] = a._cell[row][column] + b._cell[row][column];
            }
        }
    }

    /**
	 * add the value of rowVector to each corresponding column of matrix a 
	 * @param a matrix
	 * @param rowVector matrix of row one
	 * @throws Exception
	 */
    public void addColumn(Matrix a, Matrix rowVector) throws Exception {
        if (a._column != rowVector._column || rowVector._row != 1) throw new Exception("invalid size");
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] += rowVector._cell[0][column];
            }
        }
    }

    /**
	 * add the value of columnVector to each corresponding row of matrix a 
	 * @param a matrix
	 * @param columnVector matrix of column one
	 * @throws Exception
	 */
    public void addRow(Matrix a, Matrix columnVector) throws Exception {
        if (a._row != columnVector._row || columnVector._column != 1) throw new Exception("invalid size");
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] += columnVector._cell[row][0];
            }
        }
    }

    /**
	 * attach matrix a and b vertically
	 * @param a matrix
	 * @param b matrix
	 * @throws Exception
	 */
    public void attachColumn(Matrix a, Matrix b) throws Exception {
        if (this == a || this == b) throw new Exception("self");
        if (a._row != b._row) throw new Exception("invalid size");
        setSize(a._row, a._column + b._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] = a._cell[row][column];
            }
        }
        for (int row = 0; row < b._row; row++) {
            for (int column = 0; column < b._column; column++) {
                _cell[row][column + a._column] = b._cell[row][column];
            }
        }
    }

    /**
	 * attach matrix a and b horizontally
	 * @param a matrix
	 * @param b matrix
	 * @throws Exception
	 */
    public void attachRow(Matrix a, Matrix b) throws Exception {
        if (this == a || this == b) throw new Exception("self");
        if (a._column != b._column) throw new Exception("invalid size");
        setSize(a._row + b._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] = a._cell[row][column];
            }
        }
        for (int row = 0; row < b._row; row++) {
            for (int column = 0; column < b._column; column++) {
                _cell[row + a._row][column] = b._cell[row][column];
            }
        }
    }

    /**
	 * get matrix data reference
	 * @return matrix data reference
	 */
    public double[][] cell() {
        return _cell;
    }

    /**
	 * get column of matrix
	 * @return column
	 */
    public int column() {
        return _column;
    }

    /**
	 * copy matrix
	 * @param m matrix
	 */
    public void copy(Matrix m) {
        setSize(m._row, m._column);
        setCell(m._cell);
    }

    /**
	 * extract part of matrix
	 * @param newRow new row
	 * @param newColumn new column
	 * @return
	 */
    public Matrix extract(int newRow, int newColumn) {
        Matrix result = new Matrix(newRow, newColumn);
        if (newRow > _row || newColumn > _column) result.setValue(0.0f);
        if (_row < newRow) newRow = _row;
        if (_column < newColumn) newColumn = _column;
        for (int row = 0; row < newRow; row++) {
            for (int column = 0; column < newColumn; column++) {
                result._cell[row][column] = _cell[row][column];
            }
        }
        return result;
    }

    /**
	 * get mean square of error of two matrix
	 * @param a matrix
	 * @return mean squre error
	 */
    public double getMSE(Matrix a) {
        if (this == a) return 0.0f;
        int row = (_row < a._row) ? _row : a._row;
        int column = (_column < a._column) ? _column : a._column;
        if (row <= 0 || column <= 0) return 0.0f;
        double sum = 0.0f;
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < column; c++) {
                double diff = _cell[r][c] - a._cell[r][c];
                sum += diff * diff;
            }
        }
        return sum / (row * column);
    }

    /**
	 * get matrix size with string format like "(100,200)"
	 * @return matrix size with string format
	 */
    public String getSizeInfo() {
        return "(" + ((Integer) _row).toString() + "," + ((Integer) _column).toString() + ")";
    }

    /**
	 * get inverse of matrix (not implemented) 
	 * @param a matrix
	 * @throws Exception
	 */
    public void inverse(Matrix a) throws Exception {
        throw new Exception("not implemented");
    }

    /**
	 * scalar multiply
	 * @param value scalar to multiply
	 * @param a matrix
	 */
    public void multiply(double value, Matrix a) {
        setSize(a._row, a._column);
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                _cell[row][column] *= value;
            }
        }
    }

    /**
	 * matrix multiplication
	 * @param a matrix
	 * @param b matrix
	 * @throws Exception
	 */
    public void multiply(Matrix a, Matrix b) throws Exception {
        if (this == a || this == b) throw new Exception("self");
        if (a._column != b._row) throw new Exception("invalid size: " + a.getSizeInfo() + "x" + b.getSizeInfo());
        setSize(a._row, b._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < b._column; column++) {
                double sum = 0.0f;
                for (int i = 0; i < a._column; i++) {
                    sum += a._cell[row][i] * b._cell[i][column];
                }
                _cell[row][column] = sum;
            }
        }
    }

    /**
	 * multiply the value of rowVector to each corresponding column of matrix a
	 * @param a matrix
	 * @param rowVector matrix of row one
	 * @throws Exception
	 */
    public void multiplyColumn(Matrix a, Matrix rowVector) throws Exception {
        if (a._column != rowVector._column || rowVector._row != 1) throw new Exception("invalid size");
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] *= rowVector._cell[0][column];
            }
        }
    }

    /**
	 * pair-wise matrix multiplication
	 * @param a matrix
	 * @param b matrix
	 * @throws Exception
	 */
    public void multiplyPair(Matrix a, Matrix b) throws Exception {
        if (a._row != b._row || a._column != b._column) throw new Exception("invalid size: " + a.getSizeInfo() + "*" + b.getSizeInfo());
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < b._column; column++) {
                _cell[row][column] = a._cell[row][column] * b._cell[row][column];
            }
        }
    }

    /**
	 * multiply the value of columnVector to each corresponding row of matrix a
	 * @param a matrix
	 * @param columnVector matrix of column one
	 * @throws Exception
	 */
    public void multiplyRow(Matrix a, Matrix columnVector) throws Exception {
        if (a._row != columnVector._row || columnVector._column != 1) throw new Exception("invalid size");
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] *= columnVector._cell[row][0];
            }
        }
    }

    /**
	 * ececute function f to each cell of matrix a. the result will be written onto matrix a itself.
	 * @param f function to be executed
	 * @param a matrix
	 * @throws Exception
	 */
    public void pairFunction(function f, Matrix a) throws Exception {
        pairFunction(a, f);
    }

    /**
	 * same as
	 * {@link Matrix#pairFunction(Matrix.function, Matrix)}
	 * @param a matrix
	 * @param f function to be executed
	 * @throws Exception
	 */
    public void pairFunction(Matrix a, function f) throws Exception {
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] = f.f(a._cell[row][column]);
            }
        }
    }

    /**
	 * parse string and set the value of matrix 
	 * @param row matrix row
	 * @param column matrix column
	 * @param str string to be set of the form like "1,2;3,4" 
	 * @throws Exception
	 */
    public void parse(int row, int column, String str) throws Exception {
        if (row <= 0 || column <= 0) throw new Exception("invalid size");
        setSize(row, column);
        String[] rows = str.split(";");
        if (_row != rows.length) throw new Exception("invalid size");
        for (row = 0; row < _row; row++) {
            String[] cols = rows[row].split(",");
            if (_column != cols.length) throw new Exception("invalid size");
            for (column = 0; column < _column; column++) {
                _cell[row][column] = Float.parseFloat(cols[column]);
            }
        }
    }

    /**
	 * @see
	 * {@link Matrix#parse(String)}
	 * @param str string
	 * @throws Exception
	 */
    public void parse(String str) throws Exception {
        String[] rows = str.split(";");
        int newRow = rows.length;
        if (newRow <= 0) throw new Exception("invalid size");
        String[] cols = rows[0].split(",");
        int newColumn = cols.length;
        parse(newRow, newColumn, str);
    }

    /**
	 * randomize the matrix value
	 * @param min minimum bound of random number
	 * @param max maximun bound of random number
	 */
    public void randomize(double min, double max) {
        java.util.Random g = new java.util.Random();
        double diff = max - min;
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                _cell[row][column] = g.nextFloat() * diff + min;
            }
        }
    }

    /**
	 * report matrix value to console with the form like "(2,2) => 1,2;3,4" 
	 */
    public void reportDetail() {
        System.out.println(getSizeInfo() + " => " + toString());
    }

    /**
	 * get row of matrix
	 * @return row
	 */
    public int row() {
        return _row;
    }

    /**
	 * set all the cell value of matrix 
	 * @param value value to set
	 */
    public void setCell(double value) {
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                _cell[row][column] = value;
            }
        }
    }

    /**
	 * set all the cell value with specified two dimensional array value
	 * @param value two dimensional array
	 */
    public void setCell(double[][] value) {
        int row = value.length;
        if (row <= 0) {
            setSize(0, 0);
            return;
        }
        int column = value[0].length;
        setSize(row, column);
        for (row = 0; row < _row; row++) {
            for (column = 0; column < _column; column++) {
                _cell[row][column] = value[row][column];
            }
        }
    }

    /**
	 * set to identity matrix
	 */
    public void setIdentity() {
        setValue(0.0f);
        int size = (_row < _column) ? _row : _column;
        for (int i = 0; i < size; i++) {
            _cell[i][i] = 1.0f;
        }
    }

    /**
	 * set matrix size
	 * @param row matrix row
	 * @param column matrix column
	 */
    public void setSize(int row, int column) {
        if (row <= 0 || column <= 0) {
            _row = _column = 0;
            _cell = null;
            _logger.warn("setSize(), zero size");
        } else if (_row != row || _column != column) {
            _row = row;
            _column = column;
            _cell = new double[_row][_column];
        }
    }

    /**
	 * set the value of all the cell with specified value
	 * @param value value to set
	 */
    public void setValue(double value) {
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                _cell[row][column] = value;
            }
        }
    }

    /**
	 * subtract matrix
	 * @param a matrix
	 * @param b matrix
	 * @throws Exception
	 */
    public void subtract(Matrix a, Matrix b) throws Exception {
        if (a._row != b._row || a._column != b._column) throw new Exception("invalid size");
        setSize(a._row, a._column);
        for (int row = 0; row < a._row; row++) {
            for (int column = 0; column < a._column; column++) {
                _cell[row][column] = a._cell[row][column] - b._cell[row][column];
            }
        }
    }

    public String toString() {
        String str = "";
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                str += ((Double) _cell[row][column]).toString();
                if (column < _column - 1) str += ","; else if (row < _row - 1) str += ";";
            }
        }
        return str;
    }

    /**
	 * transpose matrix.
	 * the result will be set onto itself.
	 */
    public void transpose() {
        Matrix t = new Matrix(_column, _row);
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                t._cell[column][row] = _cell[row][column];
            }
        }
        _row = t._row;
        _column = t._column;
        _cell = t._cell;
    }

    /**
	 * transpose matrix a. the value of matrix a will not be changed.
	 * @param a matrix
	 * @throws Exception
	 */
    public void transpose(Matrix a) throws Exception {
        if (this == a) throw new Exception("self");
        setSize(a._column, a._row);
        for (int row = 0; row < _row; row++) {
            for (int column = 0; column < _column; column++) {
                _cell[row][column] = a._cell[column][row];
            }
        }
    }
}
