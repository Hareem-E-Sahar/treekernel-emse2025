package la4j.matrix.dense;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import la4j.factory.DenseFactory;
import la4j.factory.Factory;
import la4j.matrix.AbstractMatrix;
import la4j.matrix.Matrix;
import la4j.vector.Vector;

public class DenseMatrix extends AbstractMatrix implements Matrix {

    private static final long serialVersionUID = 1L;

    private double self[][];

    public DenseMatrix() {
        this(0, 0);
    }

    public DenseMatrix(double array[][]) {
        super(new DenseFactory());
        this.self = array;
        this.rows = array.length;
        this.columns = array[0].length;
    }

    public DenseMatrix(int rows, int columns) {
        super(new DenseFactory());
        this.self = new double[rows][columns];
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public double get(int i, int j) {
        if (i >= rows || i < 0 || j >= columns || j < 0) throw new IndexOutOfBoundsException();
        return self[i][j];
    }

    @Override
    public void set(int i, int j, double value) {
        if (i >= rows || i < 0 || j >= columns || j < 0) throw new IndexOutOfBoundsException();
        self[i][j] = value;
    }

    @Override
    public void resize(int rows, int columns) {
        if (rows < 0 || columns < 0) throw new IllegalArgumentException();
        if (this.rows == rows && this.columns == columns) return;
        if (this.rows >= rows && this.columns >= columns) {
            this.rows = rows;
            this.columns = columns;
            return;
        }
        double newSelf[][] = new double[rows][columns];
        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(self[i], 0, newSelf[i], 0, this.columns);
        }
        this.rows = rows;
        this.columns = columns;
        self = newSelf;
    }

    @Override
    public double[][] toArray() {
        return self;
    }

    @Override
    public double[][] toArrayCopy() {
        double copy[][] = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(self[i], 0, copy[i], 0, columns);
        }
        return copy;
    }

    @Override
    public void swapRows(int i, int j) {
        if (i >= rows || i < 0 || j >= rows || j < 0) throw new IndexOutOfBoundsException();
        if (i == j) return;
        double dd[] = self[i];
        self[i] = self[j];
        self[j] = dd;
    }

    @Override
    public void swapColumns(int i, int j) {
        if (i >= columns || i < 0 || j >= columns || j < 0) throw new IndexOutOfBoundsException();
        if (i == j) return;
        for (int ii = 0; ii < rows; ii++) {
            double d = self[ii][i];
            self[ii][i] = self[ii][j];
            self[ii][j] = d;
        }
    }

    @Override
    public int nonzero() {
        int result = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Math.abs(self[i][j]) > EPS) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public Matrix transpose(Factory factory) {
        if (factory == null) throw new NullPointerException();
        double result[][] = new double[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j][i] = self[i][j];
            }
        }
        return factory.createMatrix(result);
    }

    @Override
    public Vector getRow(int i) {
        if (i >= rows || i < 0) throw new IndexOutOfBoundsException();
        double result[] = new double[columns];
        for (int j = 0; j < columns; j++) {
            result[j] = self[i][j];
        }
        return factory.createVector(result);
    }

    @Override
    public Vector getColumn(int i) {
        if (i >= columns || i < 0) throw new IndexOutOfBoundsException();
        double result[] = new double[rows];
        for (int j = 0; j < rows; j++) {
            result[j] = self[j][i];
        }
        return factory.createVector(result);
    }

    @Override
    public void setColumn(int i, Vector column) {
        if (i >= columns || i < 0) throw new IndexOutOfBoundsException();
        if (rows != column.length()) throw new IllegalArgumentException();
        for (int j = 0; j < column.length(); j++) {
            self[j][i] = column.get(j);
        }
    }

    @Override
    public void setRow(int i, Vector row) {
        if (i >= rows || i < 0) throw new IndexOutOfBoundsException();
        if (columns != row.length()) throw new IllegalArgumentException();
        for (int j = 0; j < row.length(); j++) {
            self[i][j] = row.get(j);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(rows);
        out.writeInt(columns);
        out.writeByte(META_MARKER);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                out.writeDouble(self[i][j]);
                out.writeByte(ELEMENT_MARKER);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        columns = in.readInt();
        in.readByte();
        self = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                self[i][j] = in.readDouble();
                in.readByte();
            }
        }
    }

    @Override
    public Matrix clone() {
        DenseMatrix dolly = (DenseMatrix) super.clone();
        dolly.self = self.clone();
        for (int i = 0; i < rows; i++) {
            dolly.self[i] = self[i].clone();
        }
        return dolly;
    }
}
