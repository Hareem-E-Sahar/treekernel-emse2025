package utils.misc;

import exceptions.*;

public class Matrix {

    private double[][] Values;

    private int Cols;

    private int Rows;

    private Matrix Transposed = null;

    private Matrix Inverted = null;

    private double Determinant = Double.NaN;

    public Matrix(double[][] vals) {
        Values = vals;
        Rows = vals.length;
        Cols = vals[0].length;
    }

    public Matrix(int rows, int cols) {
        Rows = rows;
        Cols = cols;
        Values = new double[Rows][Cols];
    }

    public Matrix(int rows, int cols, double defVal, boolean whole) {
        Rows = rows;
        Cols = cols;
        Values = new double[Rows][Cols];
        if (whole) {
            for (int i = 0; i < Rows; ++i) {
                for (int j = 0; j < Cols; ++j) {
                    Values[i][j] = defVal;
                }
            }
        } else {
            for (int i = 0; i < Rows; ++i) {
                Values[i][i] = defVal;
            }
        }
    }

    public Matrix(double[] col) {
        Rows = col.length;
        Cols = 1;
        Values = new double[Rows][1];
        for (int i = 0; i < Rows; ++i) {
            Values[i][0] = col[i];
        }
    }

    private double[][] copyValues(double[][] vals) {
        double[][] res = new double[vals.length][vals[0].length];
        for (int i = 0; i < vals.length; ++i) {
            for (int j = 0; j < vals[i].length; ++j) {
                res[i][j] = vals[i][j];
            }
        }
        return res;
    }

    public int getCols() {
        return Cols;
    }

    public int getRows() {
        return Rows;
    }

    public double[][] getValues() {
        return Values;
    }

    public double[] getRow(int index) {
        return Values[index];
    }

    public double[] getCol(int index) {
        double[] res = new double[Rows];
        for (int i = 0; i < Rows; ++i) {
            res[i] = Values[i][index];
        }
        return res;
    }

    public Matrix add(Matrix operand) throws MatricesNotMatch {
        if ((Cols != operand.Cols) || (Rows != operand.Rows)) throw new MatricesNotMatch();
        Matrix result = new Matrix(Rows, Cols);
        for (int i = 0; i < Rows; ++i) {
            for (int j = 0; j < Cols; ++j) {
                result.Values[i][j] = Values[i][j] + operand.Values[i][j];
            }
        }
        return result;
    }

    public Matrix sub(Matrix operand) throws MatricesNotMatch {
        if ((Cols != operand.Cols) || (Rows != operand.Rows)) throw new MatricesNotMatch();
        Matrix result = new Matrix(Rows, Cols);
        for (int i = 0; i < Rows; ++i) {
            for (int j = 0; j < Cols; ++j) {
                result.Values[i][j] = Values[i][j] - operand.Values[i][j];
            }
        }
        return result;
    }

    public Matrix multiplyNumber(double value) {
        Matrix result = new Matrix(Rows, Cols);
        for (int i = 0; i < Rows; ++i) {
            for (int j = 0; j < Cols; ++j) {
                result.Values[i][j] = Values[i][j] * value;
            }
        }
        return result;
    }

    public Matrix multiply(Matrix operand) throws MatricesNotMatch {
        if (Cols != operand.Rows) throw new MatricesNotMatch();
        Matrix result = new Matrix(Rows, operand.Cols);
        double cellVal = 0;
        for (int i = 0; i < Rows; ++i) {
            for (int j = 0; j < operand.Cols; ++j) {
                cellVal = 0;
                for (int k = 0; k < Cols; ++k) {
                    cellVal += Values[i][k] * operand.Values[k][j];
                }
                result.Values[i][j] = cellVal;
            }
        }
        return result;
    }

    public Matrix getTransposed() {
        if (Transposed != null) return Transposed;
        Matrix result = new Matrix(Cols, Rows);
        for (int i = 0; i < Rows; ++i) {
            for (int j = 0; j < Cols; ++j) {
                result.Values[j][i] = Values[i][j];
            }
        }
        Transposed = result;
        result.Transposed = this;
        return result;
    }

    public double getDeterminant() throws MatrixNotSquare {
        if (!Double.isNaN(Determinant)) return Determinant;
        if (Cols != Rows) throw new MatrixNotSquare();
        if (Rows == 1) {
            Determinant = Values[0][0];
            return Values[0][0];
        }
        if (Rows == 2) {
            Determinant = Values[0][0] * Values[1][1] - Values[1][0] * Values[0][1];
            return Determinant;
        }
        double result = 0.0;
        Matrix tempMatrix;
        double tempDet;
        for (int i = 0; i < Rows; ++i) {
            if (Values[0][i] != 0) {
                tempMatrix = new Matrix(Rows - 1, Rows - 1);
                for (int j = 0; j < Rows; ++j) {
                    for (int k = 0; k < Rows; ++k) {
                        if ((i != j) && (i != k)) {
                            if (j > i) {
                                if (k > i) tempMatrix.Values[j - 1][k - 1] = Values[j][k]; else tempMatrix.Values[j - 1][k] = Values[j][k];
                            } else {
                                if (k > i) tempMatrix.Values[j][k - 1] = Values[j][k]; else tempMatrix.Values[j][k] = Values[j][k];
                            }
                        }
                    }
                }
                tempDet = tempMatrix.getDeterminant();
                if ((i % 2) == 0) result += Values[0][i] * tempDet; else result -= Values[0][i] * tempDet;
            }
        }
        Determinant = result;
        return result;
    }

    public Matrix getInvertible() throws MatrixNotSquare, MatrixZeroDeterminant {
        if (Inverted != null) return Inverted;
        if (Cols != Rows) throw new MatrixNotSquare();
        if (this.getDeterminant() == 0.0) throw new MatrixZeroDeterminant();
        Matrix tempCopy = new Matrix(this.copyValues(Values));
        Matrix result = new Matrix(Cols, Rows, 1.0, false);
        double[] tempVal;
        double tempNum;
        for (int i = 0; i < tempCopy.Cols; ++i) {
            if (tempCopy.Values[i][i] == 0.0) {
                for (int j = 0; j < tempCopy.Rows; ++j) {
                    if (tempCopy.Values[j][i] != 0.0) {
                        tempVal = tempCopy.Values[j];
                        tempCopy.Values[j] = tempCopy.Values[i];
                        tempCopy.Values[i] = tempVal;
                        tempVal = result.Values[j];
                        result.Values[j] = result.Values[i];
                        result.Values[i] = tempVal;
                        break;
                    }
                }
            }
            tempNum = tempCopy.Values[i][i];
            for (int j = 0; j < Cols; ++j) {
                tempCopy.Values[i][j] = tempCopy.Values[i][j] / tempNum;
                result.Values[i][j] = result.Values[i][j] / tempNum;
            }
            for (int j = i + 1; j < tempCopy.Rows; ++j) {
                tempNum = tempCopy.Values[j][i];
                for (int k = 0; k < tempCopy.Cols; ++k) {
                    tempCopy.Values[j][k] -= tempCopy.Values[i][k] * tempNum;
                    result.Values[j][k] -= result.Values[i][k] * tempNum;
                }
            }
        }
        for (int i = tempCopy.Rows - 2; i >= 0; --i) {
            for (int j = 0; j <= i; ++j) {
                tempNum = tempCopy.Values[j][i + 1];
                for (int k = 0; k < tempCopy.Cols; ++k) {
                    tempCopy.Values[j][k] -= tempCopy.Values[i + 1][k] * tempNum;
                    result.Values[j][k] -= result.Values[i + 1][k] * tempNum;
                }
            }
        }
        Inverted = result;
        result.Inverted = this;
        return result;
    }

    public Matrix copy() {
        Matrix result = new Matrix(Rows, Cols);
        for (int i = 0; i < Rows; ++i) {
            for (int j = 0; j < Cols; ++j) {
                result.Values[i][j] = Values[i][j];
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Rows; i++) {
            for (int j = 0; j < Cols; j++) {
                sb.append(Values[i][j] + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
