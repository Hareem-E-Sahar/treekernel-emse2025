package coho.lp.solver;

import java.util.*;
import coho.common.matrix.*;
import coho.common.number.*;

public class BasicCohoMatrix<V extends CohoNumber> extends BasicMatrix<V> implements CohoMatrix {

    protected final int[][] pos;

    protected final boolean isDual;

    public boolean isDual() {
        return isDual;
    }

    ;

    protected static boolean isValid(Matrix m, boolean isDual) {
        if (isDual) {
            for (int col = 0; col < m.ncols(); col++) {
                int count = 0;
                for (int row = 0; row < m.nrows(); row++) {
                    if (!m.V(row, col).equals(m.elementType().zero())) count++;
                }
                if (count < 1 || count > 2) return false;
            }
        } else {
            for (int row = 0; row < m.nrows(); row++) {
                int count = 0;
                for (int col = 0; col < m.ncols(); col++) {
                    if (!m.V(row, col).equals(m.elementType().zero())) count++;
                }
                if (count < 1 || count > 2) return false;
            }
        }
        return true;
    }

    protected BasicCohoMatrix(V v, V[][] data, int[][] pos, int nrows, int ncols, boolean isDual) {
        super(v.type(), nrows, ncols, data);
        this.pos = pos;
        this.isDual = isDual;
    }

    protected BasicCohoMatrix(V v, int nrows, int ncols, boolean isDual) {
        super(v.type(), nrows, ncols, (V[][]) v.createArray(isDual ? 2 : nrows, isDual ? ncols : 2));
        pos = new int[isDual ? 2 : nrows][isDual ? ncols : 2];
        this.isDual = isDual;
    }

    public BasicCohoMatrix(V v, Matrix m, boolean isDual) {
        this(v, m.nrows(), m.ncols(), isDual);
        if (m instanceof BasicCohoMatrix && ((BasicCohoMatrix) m).isDual() == isDual) {
            BasicCohoMatrix cohoMatrix = (BasicCohoMatrix) m;
            for (int i = 0; i < cohoMatrix.data.length; i++) {
                for (int j = 0; j < cohoMatrix.data[0].length; j++) {
                    if (cohoMatrix.data[i][j] != null) {
                        data[i][j] = (V) v.convert(cohoMatrix.data[i][j]);
                    } else {
                        data[i][j] = null;
                    }
                    pos[i][j] = cohoMatrix.pos[i][j];
                }
            }
        } else {
            if (!isValid(m, isDual)) {
                throw new MatrixError("CohoIntervalMatrix:create: the input matrix" + m + " is not valid CohoMatrix");
            }
            assign(m);
        }
    }

    public BasicCohoMatrix<V> convert(Matrix m, boolean isDual) {
        if (m instanceof BasicCohoMatrix) {
            if (((BasicCohoMatrix) m).isDual() == isDual && m.elementType() == elementType()) {
                return (BasicCohoMatrix<V>) m;
            }
        }
        return new BasicCohoMatrix<V>(zero(), m, isDual);
    }

    @Override
    public V V(int row, int col) {
        if (isDual) {
            if (data[0][col] != null && pos[0][col] == row) {
                return data[0][col];
            } else if (data[1][col] != null && pos[1][col] == row) {
                return data[1][col];
            } else {
                return zero();
            }
        } else {
            if (data[row][0] != null && pos[row][0] == col) {
                return data[row][0];
            } else if (data[row][1] != null && pos[row][1] == col) {
                return data[row][1];
            } else {
                return zero();
            }
        }
    }

    @Override
    public BasicCohoMatrix<V> assign(CohoNumber v, int row, int col) {
        V value = elementConvert(v);
        if (isDual) {
            if (value.eq(zero())) {
                if (data[0][col] != null && pos[0][col] == row) {
                    if (data[1][col] == null) {
                        throw new MatrixError("CohoMatrix.assign: The result" + " is not a CohoMatrix after set the " + row + " row and " + col + " column as zero: There is no non-zero element for " + col + " column");
                    }
                    data[0][col] = data[1][col];
                    pos[0][col] = pos[1][col];
                    data[1][col] = null;
                } else if (data[1][col] != null && pos[1][col] == row) {
                    data[1][col] = null;
                } else {
                }
            } else {
                if ((data[0][col] == null) || (pos[0][col] == row)) {
                    data[0][col] = value;
                    pos[0][col] = row;
                } else if ((data[1][col] == null) || (pos[1][col] == row)) {
                    data[1][col] = value;
                    pos[1][col] = row;
                } else {
                    throw new MatrixError("CohoRationalMatrix.assign: The result" + "is not a CohoMatrix after assigning" + v.toString() + "to row: " + row + " and column: " + col);
                }
            }
        } else {
            if (value.eq(zero())) {
                if (data[row][0] != null && pos[row][0] == col) {
                    if (data[row][1] == null) {
                        throw new MatrixError("CohoMatrix.assign: The result" + " is not a CohoMatrix after set the " + row + " row and " + col + " column as zero: There is no non-zero element for " + row + " row");
                    }
                    data[row][0] = data[row][1];
                    pos[row][0] = pos[row][1];
                    data[row][1] = null;
                } else if (data[row][1] != null && pos[row][1] == col) {
                    data[row][1] = null;
                } else {
                }
            } else {
                if ((data[row][0] == null) || (pos[row][0] == col)) {
                    data[row][0] = value;
                    pos[row][0] = col;
                } else if ((data[row][1] == null) || (pos[row][1] == col)) {
                    data[row][1] = value;
                    pos[row][1] = col;
                } else {
                    throw new MatrixError("CohoMatrix.assign: The result" + "is not a CohoMatrix after assigning" + v.toString() + "to row: " + row + " and column: " + col);
                }
            }
        }
        return this;
    }

    @Override
    public V sum() {
        V r = zero();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (data[i][j] != null) r = (V) r.add(data[i][j]);
            }
        }
        return r;
    }

    @Override
    public V prod() {
        V r = one();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (data[i][j] != null) r = (V) r.mult(data[i][j]);
            }
        }
        return r;
    }

    @Override
    public V norm() {
        V r = zero();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (data[i][j] != null) r = (V) r.add(data[i][j].mult(data[i][j]));
            }
        }
        return (V) r.sqrt();
    }

    @Override
    public BasicCohoMatrix<V> transpose() {
        V[][] d = createArray(data[0].length, data.length);
        int[][] p = new int[pos[0].length][pos.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                d[j][i] = data[i][j];
                p[j][i] = pos[i][j];
            }
        }
        BasicCohoMatrix<V> result = new BasicCohoMatrix(type.zero(), d, p, ncols, nrows, !isDual);
        return result;
    }

    @Override
    public BasicCohoMatrix<V> negate() {
        V[][] d = createArray(data.length, data[0].length);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                d[i][j] = data[i][j] == null ? null : (V) data[i][j].negate();
            }
        }
        return new BasicCohoMatrix(type.zero(), d, pos, nrows, ncols, isDual);
    }

    @Override
    public BasicCohoMatrix<V> abs() {
        V[][] d = createArray(data.length, data[0].length);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                d[i][j] = data[i][j] == null ? null : (V) data[i][j].abs();
            }
        }
        return new BasicCohoMatrix(type.zero(), d, pos, nrows, ncols, isDual);
    }

    @Override
    public BasicMatrix mult(Matrix m) {
        BasicMatrix result = null;
        if (isDual && (m instanceof CohoMatrix) && ((CohoMatrix) m).isDual()) {
            if (ncols != m.nrows()) throw new MatrixError("Dimension error for multiply of matrix");
            result = new BasicMatrix(elementType().promote(m.elementType()).zero(), nrows, m.ncols());
            for (int i = 0; i < result.nrows(); i++) {
                for (int j = 0; j < result.ncols(); j++) {
                    result.assign(m.col(j).dotProd(row(i)), i, j);
                }
            }
        } else {
            result = super.mult(m);
        }
        return result;
    }

    @Override
    public CohoNumber dotProd(Matrix m) {
        CohoNumber result = null;
        if (!isDual) {
            result = data[0][0].mult(m.V(pos[0][0]));
            if (data[0][1] != null) result = data[0][1].mult(m.V(pos[0][1])).add(result);
        } else {
            result = data[0][0].mult(m.V(pos[0][0]));
            if (data[1][0] != null) result = data[1][0].mult(m.V(pos[1][0])).add(result);
        }
        return result;
    }

    public BasicCohoMatrix<V> trim(IntegerMatrix basis) {
        if (isDual) {
            return (BasicCohoMatrix<V>) col(basis);
        } else {
            return (BasicCohoMatrix<V>) row(basis);
        }
    }

    public BasicCohoMatrix<V> trim(BooleanMatrix basis) {
        return trim(basis.find());
    }

    public BasicMatrix<V> expand() {
        BasicMatrix<V> result = new BasicMatrix(type.zero(), nrows, ncols);
        result.assign(this);
        return result;
    }

    public BasicCohoMatrix<V> randoms(int nrows, int ncols) {
        return randoms(nrows, ncols, false);
    }

    public BasicCohoMatrix<V> randoms(int nrows, int ncols, boolean isDual) {
        BasicCohoMatrix<V> result = new BasicCohoMatrix<V>(zero(), nrows, ncols, isDual);
        if (isDual) {
            for (int j = 0; j < ncols; j++) {
                V temp = random();
                while (temp.eq(zero())) {
                    temp = random();
                }
                int row = Math.round((float) (Math.random()) * (ncols - 1));
                result.assign(temp, row, j);
                if (Math.random() > 0.5) {
                    do {
                        temp = random();
                    } while (temp.eq(zero()));
                    int oldRow = row;
                    do {
                        row = Math.round((float) (Math.random()) * (ncols - 1));
                    } while (row == oldRow);
                    result.assign(temp, row, j);
                }
            }
        } else {
            for (int i = 0; i < nrows; i++) {
                V temp = random();
                while (temp.eq(zero())) {
                    temp = random();
                }
                int col = Math.round((float) (Math.random()) * (ncols - 1));
                result.assign(temp, i, col);
                if (Math.random() > 0.5) {
                    do {
                        temp = random();
                    } while (temp.eq(zero()));
                    int oldRow = col;
                    do {
                        col = Math.round((float) (Math.random()) * (ncols - 1));
                    } while (col == oldRow);
                    result.assign(temp, i, col);
                }
            }
        }
        return result;
    }

    /**
	 * Override the default implementation of DoubleIntervalMatrix
	 * Return a CohoMatrix as possible
	 */
    @Override
    public BasicMatrix<V> row(int row) {
        if (isDual) {
            return super.row(row);
        } else {
            V[][] d = createArray(1, 2);
            int[][] p = new int[1][2];
            d[0][0] = data[row][0];
            d[0][1] = data[row][1];
            p[0][0] = pos[row][0];
            p[0][1] = pos[row][1];
            return new BasicCohoMatrix(type.zero(), d, p, 1, ncols, false);
        }
    }

    @Override
    public BasicMatrix<V> row(Range row) {
        if (isDual) {
            return super.row(row);
        } else {
            int newRows = row.length();
            V[][] d = createArray(newRows, 2);
            int[][] p = new int[newRows][2];
            for (int i = 0; i < newRows; i++) {
                int index = row.lo() + i;
                d[i][0] = data[index][0];
                d[i][1] = data[index][1];
                p[i][0] = pos[index][0];
                p[i][1] = pos[index][1];
            }
            return new BasicCohoMatrix(type.zero(), d, p, newRows, ncols, false);
        }
    }

    @Override
    public BasicMatrix<V> row(IntegerMatrix _pos) {
        if (isDual) {
            return super.row(_pos);
        } else {
            int newRows = _pos.length();
            V[][] d = createArray(newRows, 2);
            int[][] p = new int[newRows][2];
            for (int i = 0; i < newRows; i++) {
                int index = _pos.V(i).intValue();
                d[i][0] = data[index][0];
                d[i][1] = data[index][1];
                p[i][0] = pos[index][0];
                p[i][1] = pos[index][1];
            }
            return new BasicCohoMatrix(type.zero(), d, p, newRows, ncols, false);
        }
    }

    @Override
    public BasicMatrix<V> col(int col) {
        if (isDual) {
            V[][] d = createArray(2, 1);
            int[][] p = new int[2][1];
            d[0][0] = data[0][col];
            d[1][0] = data[1][col];
            p[0][0] = pos[0][col];
            p[1][0] = pos[1][col];
            return new BasicCohoMatrix(type.zero(), d, p, nrows, 1, true);
        } else {
            return super.col(col);
        }
    }

    @Override
    public BasicMatrix<V> col(Range col) {
        if (isDual) {
            int newCols = col.length();
            V[][] d = createArray(2, newCols);
            int[][] p = new int[2][newCols];
            for (int i = 0; i < newCols; i++) {
                int index = col.lo() + i;
                d[0][i] = data[0][index];
                d[1][i] = data[1][index];
                p[0][i] = pos[0][index];
                p[1][i] = pos[1][index];
            }
            return new BasicCohoMatrix(type.zero(), d, p, nrows, newCols, true);
        } else {
            return super.col(col);
        }
    }

    @Override
    public BasicMatrix<V> col(IntegerMatrix _pos) {
        if (isDual) {
            int newCols = _pos.length();
            V[][] d = createArray(2, newCols);
            int[][] p = new int[2][newCols];
            for (int j = 0; j < newCols; j++) {
                int index = _pos.V(j).intValue();
                d[0][j] = data[0][index];
                d[1][j] = data[1][index];
                p[0][j] = pos[0][index];
                p[1][j] = pos[1][index];
            }
            return new BasicCohoMatrix(type.zero(), d, p, nrows, newCols, true);
        } else {
            return super.col(_pos);
        }
    }

    /**
	 * is the elemeent non zero?
	 */
    public boolean isZero(int row, int col) {
        if (isDual) {
            return !((data[0][col] != null && pos[0][col] == row) || (data[1][col] != null && pos[1][col] == row));
        } else {
            return !((data[row][0] != null && pos[row][0] == col) || (data[row][1] != null && pos[row][1] == col));
        }
    }

    /**
	 * return a BooleanMatrix. It's true if the element of this pos
	 * is non-zero
	 */
    public BooleanMatrix nonZero() {
        BooleanMatrix result = new BooleanMatrix(nrows, ncols);
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                result.assign((!isZero(i, j)), i, j);
            }
        }
        return result;
    }

    /**
	 *  get the number of non-zero elements of a given row
	 */
    public int nonZeroNumOfRow(int row) {
        if (isDual) {
            return nonZeroNumL(row);
        } else {
            return nonZeroNumS(row);
        }
    }

    /**
	 * get the number of non-zero elements of a given column
	 */
    public int nonZeroNumOfCol(int col) {
        if (isDual) {
            return nonZeroNumS(col);
        } else {
            return nonZeroNumL(col);
        }
    }

    /**
	 * get the number of non-zero elements 
	 * of given row if it's coho standard matrix
	 * of given column if it's coho transpose matrix
	 */
    protected int nonZeroNumS(int pos) {
        if (isDual) {
            return (data[0][pos] == null) ? 0 : ((data[1][pos] == null) ? 1 : 2);
        } else {
            return (data[pos][0] == null) ? 0 : ((data[pos][1] == null) ? 1 : 2);
        }
    }

    /**
	 * get the number of non-zero elements 
	 * of given column if it's coho standard matrix
	 * of given row if it's coho transpose matrix
	 */
    protected int nonZeroNumL(int pos) {
        int count = 0;
        if (isDual) {
            for (int i = 0; i < ncols; i++) {
                if ((data[0][i] != null && this.pos[0][i] == pos) || (data[1][i] != null && this.pos[1][i] == pos)) {
                    count++;
                }
            }
        } else {
            for (int i = 0; i < nrows; i++) {
                if ((data[i][0] != null && this.pos[i][0] == pos) || (data[i][1] != null && this.pos[i][1] == pos)) {
                    count++;
                }
            }
        }
        return count;
    }

    public ArrayList<Integer>[] colsAtRow() {
        ArrayList<Integer>[] colsAtRow = new ArrayList[nrows];
        for (int i = 0; i < nrows; i++) colsAtRow[i] = new ArrayList<Integer>();
        if (isDual) {
            for (int col = 0; col < ncols; col++) {
                colsAtRow[pos[0][col]].add(new Integer(col));
                if (data[1][col] != null) colsAtRow[pos[1][col]].add(new Integer(col));
            }
        } else {
            for (int row = 0; row < nrows; row++) {
                colsAtRow[row].add(new Integer(pos[row][0]));
                if (data[row][1] != null) colsAtRow[row].add(new Integer(pos[row][1]));
            }
        }
        return colsAtRow;
    }

    public ArrayList<Integer>[] rowsAtCol() {
        ArrayList<Integer>[] rowsAtCol = new ArrayList[ncols];
        for (int i = 0; i < ncols; i++) rowsAtCol[i] = new ArrayList<Integer>();
        if (isDual) {
            for (int col = 0; col < ncols; col++) {
                rowsAtCol[col].add(new Integer(pos[0][col]));
                if (data[1][col] != null) rowsAtCol[col].add(new Integer(pos[1][col]));
            }
        } else {
            for (int row = 0; row < nrows; row++) {
                rowsAtCol[pos[row][0]].add(new Integer(row));
                if (data[row][1] != null) rowsAtCol[pos[row][1]].add(new Integer(row));
            }
        }
        return rowsAtCol;
    }

    protected static void checkDims(Matrix A, Matrix b) {
        if (!A.isSquare() || A.nrows() != b.length()) {
            throw new MatrixError("CohoMatrix.getSolution: dimension error");
        }
    }

    @Override
    public BasicMatrix<V> leftDiv(Matrix m) throws SingularMatrixException {
        return getSolution(m);
    }

    public BasicMatrix<V> getSolution(Matrix m) throws SingularMatrixException {
        BasicMatrix<V> b = convert(m.nrows(), m.ncols());
        b.assign(m);
        checkDims(this, b);
        if (b.ncols() != 1) b = b.transpose();
        BasicMatrix<V> result = b.zeros();
        try {
            BooleanMatrix isVarSolved = BooleanMatrix.create(new boolean[1][ncols]);
            BooleanMatrix isRowSolved = BooleanMatrix.create(new boolean[nrows][1]);
            int solvedVars = this.solveDirect(b, result, isVarSolved, isRowSolved);
            if (solvedVars == ncols) {
                return result;
            }
            BasicCohoMatrix<V> reducedMatrix = this;
            if (solvedVars > 0) {
                BasicMatrix<V> temp = this.row(isRowSolved.negate()).col(isVarSolved.negate());
                reducedMatrix = convert(temp, isDual);
                b = b.row(isRowSolved.negate());
            }
            BooleanMatrix isVarMoved = BooleanMatrix.create(new boolean[1][reducedMatrix.ncols]);
            BooleanMatrix isRowMoved = BooleanMatrix.create(new boolean[reducedMatrix.nrows][1]);
            BasicMatrix<V> reducedResult = b.zeros();
            reducedMatrix.markOneNZCols(isVarMoved, isRowMoved);
            BasicMatrix<V> cycleResult;
            if (isRowMoved.any()) {
                BasicMatrix<V> cycleMatrix = reducedMatrix.row(isRowMoved.negate()).col(isVarMoved.negate());
                if (cycleMatrix.nrows() != cycleMatrix.ncols() || !BasicCohoMatrix.isValid(cycleMatrix, isDual) || !BasicCohoMatrix.isValid(cycleMatrix, !isDual)) {
                    throw new SingularMatrixException(Double.POSITIVE_INFINITY);
                }
                BasicCohoMatrix<V> cyclesA = convert(cycleMatrix, isDual);
                BasicMatrix<V> cycleB = b.row(isRowMoved.negate());
                cycleResult = cyclesA.solveCycles(cycleB);
            } else {
                if (!BasicCohoMatrix.isValid(reducedMatrix, !isDual)) {
                    throw new SingularMatrixException(Double.POSITIVE_INFINITY);
                }
                cycleResult = reducedMatrix.solveCycles(b);
            }
            reducedMatrix.mergeResult(reducedResult, cycleResult, isVarMoved.negate());
            if (isRowMoved.any()) {
                BasicCohoMatrix<V> remainderA = convert(reducedMatrix.row(isRowMoved), isDual);
                remainderA.solveRemainder(b.row(isRowMoved), reducedResult, isVarMoved);
            }
            mergeResult(result, reducedResult, isVarSolved.negate());
            return result;
        } catch (ArithmeticException e) {
            throw new SingularMatrixException(Double.POSITIVE_INFINITY);
        }
    }

    protected int solveDirect(BasicMatrix<V> b, BasicMatrix<V> result, BooleanMatrix isVarSolved, BooleanMatrix isRowSolved) {
        ArrayList<Integer>[] colsAtRow = colsAtRow();
        ArrayList<Integer>[] rowsAtCol = rowsAtCol();
        LinkedList<Integer> oneNZRow = new LinkedList<Integer>();
        for (int row = 0; row < nrows; row++) {
            if (colsAtRow[row].size() == 1) {
                oneNZRow.add(row);
            }
        }
        int solvedVars = 0;
        for (; !oneNZRow.isEmpty(); solvedVars++) {
            int row = oneNZRow.poll();
            int col = colsAtRow[row].get(0);
            result.assign(b.V(row).div(V(row, col)), col);
            isVarSolved.assign(true, col);
            isRowSolved.assign(true, row);
            ArrayList<Integer> rows = rowsAtCol[col];
            for (int i = 0; i < rows.size(); i++) {
                int otherRow = rows.get(i);
                if (otherRow == row) continue;
                b.assign(b.V(otherRow).sub(result.V(col).mult(V(otherRow, col))), otherRow);
                colsAtRow[otherRow].remove(new Integer(col));
                if (colsAtRow[otherRow].size() == 1) {
                    oneNZRow.add(otherRow);
                }
                if (colsAtRow[otherRow].size() == 0) {
                    oneNZRow.remove(new Integer(otherRow));
                }
            }
        }
        return solvedVars;
    }

    protected void markOneNZCols(BooleanMatrix isColMarked, BooleanMatrix isRowMarked) throws SingularMatrixException {
        ArrayList<Integer>[] rowsAtCol = rowsAtCol();
        ArrayList<Integer>[] colsAtRow = colsAtRow();
        LinkedList<Integer> oneNZCol = new LinkedList<Integer>();
        for (int col = 0; col < ncols; col++) {
            if (rowsAtCol[col].size() == 1) oneNZCol.add(col);
        }
        while (!oneNZCol.isEmpty()) {
            int col = oneNZCol.poll();
            int row = rowsAtCol[col].get(0);
            isColMarked.assign(true, col);
            isRowMarked.assign(true, row);
            ArrayList<Integer> cols = colsAtRow[row];
            for (int i = 0; i < cols.size(); i++) {
                int otherCol = cols.get(i);
                if (otherCol == col) continue;
                rowsAtCol[otherCol].remove(new Integer(row));
                if (rowsAtCol[otherCol].size() == 0) {
                    throw new SingularMatrixException(Double.POSITIVE_INFINITY);
                }
                if (rowsAtCol[otherCol].size() == 1) oneNZCol.add(otherCol);
            }
        }
        return;
    }

    protected BasicMatrix<V> solveCycles(BasicMatrix<V> b) throws SingularMatrixException {
        BasicMatrix<V> result = convert(1, ncols).zeros();
        ArrayList<Integer>[] colsAtRow = colsAtRow();
        ArrayList<Integer>[] rowsAtCol = rowsAtCol();
        boolean[] isVisited = new boolean[ncols];
        for (int startCol = 0; ; startCol++) {
            while (isVisited[startCol]) {
                startCol++;
                if (startCol == ncols) return result;
            }
            ArrayList<V> alpha = new ArrayList<V>();
            ArrayList<V> cycleB = new ArrayList<V>();
            ArrayList<Integer> permutation = new ArrayList<Integer>();
            int nextCol = startCol;
            int nextRow = rowsAtCol[nextCol].get(0);
            do {
                int currRow = nextRow;
                int currCol = nextCol;
                isVisited[currCol] = true;
                permutation.add(currCol);
                nextCol = colsAtRow[currRow].get(0);
                if (nextCol == currCol) nextCol = colsAtRow[currRow].get(1);
                nextRow = rowsAtCol[nextCol].get(0);
                if (nextRow == currRow) nextRow = rowsAtCol[nextCol].get(1);
                alpha.add((V) V(currRow, nextCol).div(V(currRow, currCol)).negate());
                cycleB.add((V) b.V(currRow).div(V(currRow, currCol)));
            } while (nextCol != startCol);
            BasicMatrix<V> cycleResult = solveCycle(convert(alpha.toArray(createVector(alpha.size()))), convert(cycleB.toArray(createVector(cycleB.size()))));
            for (int i = 0; i < permutation.size(); i++) {
                result.assign(cycleResult.V(i), permutation.get(i));
            }
        }
    }

    /**
	 * Solve cycle matrix by recursive method. 
	 * assume the length of alpha and y is the same. 
	 * This algorithm should be O(n).
	 */
    protected BasicMatrix<V> solveCycle(BasicMatrix<V> alpha, BasicMatrix<V> y) throws SingularMatrixException {
        int n = y.length();
        V[] P = createVector(n);
        P[0] = alpha.V(0);
        for (int i = 1; i < n; i++) {
            P[i] = (V) (alpha.V(i).mult(P[i - 1]));
        }
        V[] partialSum = createVector(n);
        partialSum[0] = y.V(0);
        for (int i = 1; i < n; i++) {
            partialSum[i] = (V) (y.V(i).mult(P[i - 1]).add(partialSum[i - 1]));
        }
        BasicMatrix<V> result = convert(1, n);
        result.assign(partialSum[n - 1].div(one().sub(P[n - 1])), 0);
        for (int i = 1; i < n; i++) {
            result.assign(result.V(0).sub(partialSum[i - 1]).div(P[i - 1]), i);
        }
        return result;
    }

    protected void solveRemainder(BasicMatrix<V> b, BasicMatrix<V> result, BooleanMatrix unSolvedVars) {
        ArrayList<Integer>[] colsAtRow = colsAtRow();
        while (unSolvedVars.any()) {
            boolean solvedNew = false;
            EACHROW: for (int row = 0; row < nrows; row++) {
                ArrayList<Integer> cols = colsAtRow[row];
                int var = -1;
                for (int i = 0; i < cols.size(); i++) {
                    int col = cols.get(i);
                    if (unSolvedVars.V(col).booleanValue()) {
                        if (var == -1) var = col; else continue EACHROW;
                    }
                }
                if (var == -1) continue;
                solvedNew = true;
                V varValue = b.V(row);
                for (int i = 0; i < cols.size(); i++) {
                    int col = cols.get(i);
                    if (col == var) continue; else varValue = (V) varValue.sub(V(row, col).mult(result.V(col)));
                }
                result.assign(varValue.div(V(row, var)), var);
                unSolvedVars.assign(false, var);
            }
            if (!solvedNew) throw new RuntimeException("BasicCohoMatrix.solveRemainder: Algorithm error. Infinity loop found");
        }
    }

    private void mergeResult(BasicMatrix<V> result, BasicMatrix<V> partialResult, BooleanMatrix pos) {
        for (int col = 0, partialCol = 0; col < ncols; col++) {
            if (pos.V(col).booleanValue()) {
                result.assign(partialResult.V(partialCol), col);
                partialCol++;
            }
        }
    }

    public static void main(String[] args) {
        try {
            double[][] d = { { 0.006158298846188838, 0.20588518286825708, 0.03771949601406854 }, { -0.1765857200515727, 0.0, 0.0 }, { 0.0, 1.7982115791070896E-5, -0.18071686247054142 } };
            double[] b = { 0.005189955384505362, 0.2462016176440764, -0.14809907650681123 };
            BasicCohoMatrix<CohoAPR> A = new BasicCohoMatrix(CohoAPR.zero, APRMatrix.create(d), true);
            APRMatrix B = APRMatrix.create(b);
            System.out.println(A.transpose().getSolution(B).transpose());
        } catch (SingularMatrixException e) {
            System.out.println("exception" + e.toString());
        }
    }
}
