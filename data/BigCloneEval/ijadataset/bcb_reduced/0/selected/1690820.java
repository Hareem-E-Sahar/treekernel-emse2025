package basicSim;

/**
 * Matrix of double precision floating point numbers.
 * 
 * Supports basic matrix algebra : add, multiply, invert. All operations
 * return a new matrix and leave this matrix unchanged, except for
 * operations toZero, toId.
 *  
 */
public class Matrix {

    protected int nLines;

    protected int nCols;

    protected double cell[][];

    public Matrix() {
        nLines = 0;
        nCols = 0;
        cell = null;
    }

    public Matrix(int lines, int cols) {
        nLines = lines;
        nCols = cols;
        cell = new double[nLines][nCols];
        toZero();
    }

    public Matrix(int lines, int cols, double[][] cell) {
        nLines = lines;
        nCols = cols;
        this.cell = cell;
    }

    public String toString() {
        String str = "[";
        int i, j;
        for (i = 0; i < nLines; i++) {
            if (i > 0) str = str.concat(",");
            str = str.concat("[");
            for (j = 0; j < nCols; j++) {
                if (j > 0) str = str.concat(",");
                str = str.concat((new Double(cell[i][j])).toString());
            }
            str = str.concat("]");
        }
        str = str.concat("]");
        return str;
    }

    public double[][] getCell() {
        return cell;
    }

    public Matrix setCell(double[][] cell) {
        this.cell = cell;
        return this;
    }

    public int getNCols() {
        return nCols;
    }

    public int getNLines() {
        return nLines;
    }

    public boolean isZero() {
        int i, j;
        for (i = 0; i < this.nLines; i++) {
            for (j = 0; j < this.nCols; j++) {
                if (this.cell[i][j] != 0) return false;
            }
        }
        return true;
    }

    public Matrix toZero() {
        int i, j;
        for (i = 0; i < nLines; i++) {
            for (j = 0; j < nCols; j++) {
                cell[i][j] = 0;
            }
        }
        return this;
    }

    public Matrix toId() {
        int i;
        toZero();
        for (i = 0; i < nLines; i++) {
            cell[i][i] = 1;
        }
        return this;
    }

    /**
	 * @param aMatrix
	 * @return mResult = this * aMatrix
	 */
    public Matrix mul(Matrix aMatrix) {
        int i, j, k;
        Matrix mResult = new Matrix(this.nLines, aMatrix.nCols);
        for (i = 0; i < mResult.nLines; i++) {
            for (j = 0; j < mResult.nCols; j++) {
                mResult.cell[i][j] = 0;
                for (k = 0; k < this.nCols; k++) {
                    mResult.cell[i][j] += this.cell[i][k] * aMatrix.cell[k][j];
                }
            }
        }
        return mResult;
    }

    /**
	 * @param aMatrix
	 * @return mResult = this + aMatrix
	 */
    public Matrix add(Matrix aMatrix) {
        int i, j;
        Matrix mResult = new Matrix(this.nLines, this.nCols);
        for (i = 0; i < mResult.nLines; i++) {
            for (j = 0; j < mResult.nCols; j++) {
                mResult.cell[i][j] = this.cell[i][j] + aMatrix.cell[i][j];
            }
        }
        return mResult;
    }

    /**
	 * @param aMatrix
	 * @return this = this * aMatrix
	 */
    public Matrix addThis(Matrix aMatrix) {
        int i, j;
        for (i = 0; i < this.nLines; i++) {
            for (j = 0; j < this.nCols; j++) {
                this.cell[i][j] = this.cell[i][j] + aMatrix.cell[i][j];
            }
        }
        return this;
    }

    /**
	 * @param aNumber
	 * @return mResult = aNumber * this
	 */
    public Matrix mulNum(double aNumber) {
        int i, j;
        Matrix mResult = new Matrix(this.nLines, this.nCols);
        for (i = 0; i < mResult.nLines; i++) {
            for (j = 0; j < mResult.nCols; j++) {
                mResult.cell[i][j] = this.cell[i][j] * aNumber;
            }
        }
        return mResult;
    }

    /**
	 * @param aNumber
	 * @return mResult = aNumber + this
	 */
    public Matrix addNum(double aNumber) {
        int i, j;
        Matrix mResult = new Matrix(this.nLines, this.nCols);
        for (i = 0; i < mResult.nLines; i++) {
            for (j = 0; j < mResult.nCols; j++) {
                mResult.cell[i][j] = this.cell[i][j] + aNumber;
            }
        }
        return mResult;
    }

    /**
	 * @param aNumber
	 * @return this = aNumber * this
	 */
    public Matrix mulNumThis(double aNumber) {
        int i, j;
        for (i = 0; i < this.nLines; i++) {
            for (j = 0; j < this.nCols; j++) {
                this.cell[i][j] = this.cell[i][j] * aNumber;
            }
        }
        return this;
    }

    /**
	 * @param aNumber
	 * @return this = aNumber + this
	 */
    public Matrix addNumThis(double aNumber) {
        int i, j;
        for (i = 0; i < this.nLines; i++) {
            for (j = 0; j < this.nCols; j++) {
                this.cell[i][j] = this.cell[i][j] + aNumber;
            }
        }
        return this;
    }

    /**
	 * @param aNumber
	 * @return this += aNumber * aMatrix
	 */
    public Matrix addMulNumThis(Matrix aMatrix, double aNumber) {
        int i, j;
        for (i = 0; i < this.nLines; i++) {
            for (j = 0; j < this.nCols; j++) {
                this.cell[i][j] += aMatrix.cell[i][j] * aNumber;
            }
        }
        return this;
    }

    public Matrix transpose() {
        int i, j;
        Matrix mResult = new Matrix(this.nCols, this.nLines);
        for (i = 0; i < mResult.nLines; i++) {
            for (j = 0; j < mResult.nCols; j++) {
                mResult.cell[i][j] = cell[j][i];
            }
        }
        return mResult;
    }

    public Matrix minor(int iM, int jM) {
        int i, j;
        Matrix mResult = new Matrix(this.nLines - 1, this.nCols - 1);
        for (i = 0; i < iM; i++) {
            for (j = 0; j < jM; j++) {
                mResult.cell[i][j] = this.cell[i][j];
            }
            for (j = jM + 1; j < this.nCols; j++) {
                mResult.cell[i][j - 1] = this.cell[i][j];
            }
        }
        for (i = iM + 1; i < this.nLines; i++) {
            for (j = 0; j < jM; j++) {
                mResult.cell[i - 1][j] = this.cell[i][j];
            }
            for (j = jM + 1; j < this.nCols; j++) {
                mResult.cell[i - 1][j - 1] = this.cell[i][j];
            }
        }
        return mResult;
    }

    public double det() {
        if (this.nLines == 2) return this.cell[0][0] * this.cell[1][1] - this.cell[0][1] * this.cell[1][0];
        if (this.nLines == 1) return this.cell[0][0];
        Matrix minorIJ;
        double detIJ, determinant = 0, sign = 1;
        int j;
        for (j = 0; j < this.nCols; j++) {
            minorIJ = this.minor(0, j);
            detIJ = minorIJ.det();
            determinant += sign * detIJ * this.cell[0][j];
            sign = -sign;
        }
        return determinant;
    }

    public Matrix cofactors() {
        if (this.nLines == 1) return this;
        int i, j;
        double detIJ, sign = 1;
        Matrix minorIJ, mResult = new Matrix(this.nCols, this.nLines);
        for (i = 0; i < mResult.nLines; i++) {
            for (j = 0; j < mResult.nCols; j++) {
                minorIJ = this.minor(i, j);
                detIJ = minorIJ.det();
                mResult.cell[i][j] = sign * detIJ;
                sign = -sign;
            }
        }
        return mResult;
    }

    public Matrix inv() {
        double determinant = this.det();
        Matrix mResult = this.cofactors();
        mResult = mResult.transpose();
        mResult = mResult.mulNum(1 / determinant);
        return mResult;
    }
}
