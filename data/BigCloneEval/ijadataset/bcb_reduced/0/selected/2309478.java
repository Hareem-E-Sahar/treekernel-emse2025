package util;

public class Matrix implements Cloneable {

    public double[][] elements;

    public Matrix(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("number of rows and columns must be positive");
        }
        elements = new double[rows][columns];
    }

    public Matrix(double[][] _elements) {
        elements = _elements;
        if (rows() <= 0 || columns() <= 0) {
            throw new IllegalArgumentException("number of rows and columns must be positive");
        }
    }

    public Matrix(int rows, int columns, double... _elements) {
        elements = new double[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                elements[r][c] = _elements[r * columns + c];
            }
        }
    }

    public int rows() {
        return elements.length;
    }

    public int columns() {
        return elements[0].length;
    }

    public Boolean isTrivial() {
        for (int i = 0; i < elements.length; i++) {
            for (int k = 0; k < elements[0].length; k++) {
                if (elements[i][k] != 0) return false;
            }
        }
        return true;
    }

    public geoShapes.VectorShape toVector() {
        return new geoShapes.VectorShape(0, 0, 0);
    }

    public Matrix transpose() {
        Matrix result = new Matrix(columns(), rows());
        for (int i = 0; i < rows(); i++) {
            for (int k = 0; k < columns(); k++) {
                result.elements[k][i] = elements[i][k];
            }
        }
        return result;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (columns() > 1) {
            for (int r = 0; r < rows(); r++) {
                for (int c = 0; c < columns(); c++) {
                    buffer.append(elements[r][c] + "\t");
                }
                buffer.append("\n");
            }
        } else {
            buffer.append("(");
            for (int r = 0; r < rows(); r++) {
                buffer.append(elements[r][0] + (r == rows() - 1 ? "" : "\t"));
            }
            buffer.append(")");
        }
        return buffer.toString();
    }

    public Matrix clone() {
        Matrix clone = new Matrix(rows(), columns());
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < columns(); c++) {
                clone.elements[r][c] = elements[r][c];
            }
        }
        return clone;
    }

    public Matrix multiply(Matrix other) {
        Matrix result = new Matrix(this.rows(), other.columns());
        for (int r = 0; r < result.rows(); r++) {
            for (int c = 0; c < result.columns(); c++) {
                for (int k = 0; k < this.columns(); k++) {
                    result.elements[r][c] += this.elements[r][k] * other.elements[k][c];
                }
            }
        }
        return result;
    }

    public Matrix add(Matrix other) {
        Matrix result = new Matrix(this.rows(), this.columns());
        for (int r = 0; r < result.rows(); r++) {
            for (int c = 0; c < result.columns(); c++) {
                result.elements[r][c] = this.elements[r][c] + other.elements[r][c];
            }
        }
        return result;
    }
}
