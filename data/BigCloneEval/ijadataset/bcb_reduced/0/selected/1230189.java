package net.matmas.pneditor.DynSound;

import java.util.ArrayList;

/**
 *
 * @author martinko
 */
public final class InvariantsMatrix {

    private int[][] data;

    private final boolean isVector;

    private int columnCount;

    private int rowCount;

    public InvariantsMatrix(int[][] data) throws Exception {
        this.setColumnCount(data[0].length);
        this.setRowCount(data.length);
        this.data = new int[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, columnCount);
        }
        this.isVector = false;
    }

    public InvariantsMatrix(int rowCount, int columnCount) {
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.data = new int[rowCount][columnCount];
        this.isVector = false;
    }

    public InvariantsMatrix() {
        isVector = false;
        columnCount = 0;
        rowCount = 0;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int[][] getData() {
        return data;
    }

    public void setData(int[][] data) {
        this.data = data;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public InvariantsMatrix(int[] data) throws Exception {
        this.setColumnCount(data.length);
        this.setRowCount(1);
        this.data = new int[1][columnCount];
        System.arraycopy(data, 0, this.data[0], 0, data.length);
        this.isVector = true;
    }

    public int width() {
        return data[0].length;
    }

    public int height() {
        if (isVector) {
            return 1;
        } else {
            return rowCount;
        }
    }

    public void print() {
        for (int i = 0; i < height(); i++) {
            for (int j = 0; j < width(); j++) {
                System.out.print(data[i][j] + " ");
            }
            System.out.println();
        }
    }

    public InvariantsMatrix transpose() throws Exception {
        int[][] tmpData = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                tmpData[i][j] = data[j][i];
            }
        }
        return new InvariantsMatrix(tmpData);
    }

    public int[] getRow(int index) {
        int[] result = new int[width()];
        System.arraycopy(data[index], 0, result, 0, width());
        return result;
    }

    public InvariantsMatrix getRowM(int index) {
        InvariantsMatrix result = new InvariantsMatrix(height(), 1);
        for (int i = 0; i < height(); i++) {
            result.setPositionValue(i, 0, data[i][index]);
        }
        return result;
    }

    public InvariantsMatrix addRow(int[] row) throws Exception {
        if (row.length != width()) {
            return null;
        }
        int[][] tmpData = new int[height() + 1][width()];
        for (int i = 0; i < height(); i++) {
            System.arraycopy(data[i], 0, tmpData[i], 0, width());
        }
        System.arraycopy(row, 0, tmpData[height()], 0, width());
        return new InvariantsMatrix(tmpData);
    }

    public InvariantsMatrix addRow() throws Exception {
        int[][] tmpData = new int[height() + 1][width()];
        for (int i = 0; i < height(); i++) {
            System.arraycopy(data[i], 0, tmpData[i], 0, width());
        }
        for (int j = 0; j < width(); j++) {
            tmpData[height()][j] = 0;
        }
        return new InvariantsMatrix(tmpData);
    }

    public InvariantsMatrix removeRow(int rowIndex) throws Exception {
        if (this.height() == 1) {
            return new InvariantsMatrix();
        }
        int[][] tmpData = new int[height() - 1][width()];
        for (int i = 0; i < rowIndex; i++) {
            System.arraycopy(data[i], 0, tmpData[i], 0, width());
        }
        for (int i = rowIndex; i < height() - 1; i++) {
            System.arraycopy(data[i + 1], 0, tmpData[i], 0, width());
        }
        return new InvariantsMatrix(tmpData);
    }

    public void multiplyRow(int index, int x) {
        for (int i = 0; i < width(); i++) {
            this.data[index][i] = this.data[index][i] * x;
        }
    }

    public void divideRow(int index, int x) {
        for (int i = 0; i < width(); i++) {
            this.data[index][i] = this.data[index][i] / x;
        }
    }

    public InvariantsMatrix removeColumn(int columnIndex) throws Exception {
        int[][] tmpData = new int[height()][width() - 1];
        for (int j = 0; j < height(); j++) {
            System.arraycopy(data[j], 0, tmpData[j], 0, columnIndex);
            for (int i = columnIndex; i < width() - 1; i++) {
                tmpData[j][i] = data[j][i + 1];
            }
        }
        return new InvariantsMatrix(tmpData);
    }

    public InvariantsMatrix addColumn() throws Exception {
        int[][] tmpData = new int[height()][width() + 1];
        for (int j = 0; j < height(); j++) {
            System.arraycopy(data[j], 0, tmpData[j], 0, width());
            tmpData[j][width()] = 0;
        }
        return new InvariantsMatrix(tmpData);
    }

    public void convertToIdentityMatrix(int dimension) throws Exception {
        columnCount = dimension;
        rowCount = dimension;
        data = new int[dimension][dimension];
        for (int i = 0; i < height(); i++) {
            for (int j = 0; j < width(); j++) {
                if (i == j) {
                    data[j][i] = 1;
                } else {
                    data[j][i] = 0;
                }
            }
        }
    }

    public InvariantsMatrix appendMatrix(int[][] apMatrix) throws Exception {
        if (apMatrix.length != height()) {
            return null;
        }
        int[][] tmpData = new int[height()][width() + apMatrix[0].length];
        for (int i = 0; i < height(); i++) {
            System.arraycopy(data[i], 0, tmpData[i], 0, width());
            System.arraycopy(apMatrix[i], 0, tmpData[i], width(), apMatrix[0].length);
        }
        return new InvariantsMatrix(tmpData);
    }

    public void setPositionValue(int i, int j, int value) {
        this.data[i][j] = value;
    }

    public int getPositionValue(int i, int j) {
        return this.data[i][j];
    }

    public boolean minusMatrix(int[][] minMatrix) {
        if (width() != minMatrix[0].length || height() != minMatrix.length) {
            return false;
        } else {
            for (int i = 0; i < height(); i++) {
                for (int j = 0; j < width(); j++) {
                    data[i][j] -= minMatrix[i][j];
                }
            }
            return true;
        }
    }

    public InvariantsMatrix minusMatrix(InvariantsMatrix b) {
        if (this.height() != b.height() || this.width() != b.width()) {
            return null;
        } else {
            InvariantsMatrix result = new InvariantsMatrix(this.height(), this.width());
            for (int i = 0; i < this.height(); i++) {
                for (int j = 0; j < this.width(); j++) {
                    result.setPositionValue(i, j, this.getPositionValue(i, j) - b.getPositionValue(i, j));
                }
            }
            return result;
        }
    }

    public InvariantsMatrix plusMatrix(InvariantsMatrix b) {
        if (this.height() != b.height() || this.width() != b.width()) {
            return null;
        } else {
            InvariantsMatrix result = new InvariantsMatrix(this.height(), this.width());
            for (int i = 0; i < this.height(); i++) {
                for (int j = 0; j < this.width(); j++) {
                    result.setPositionValue(i, j, this.getPositionValue(i, j) + b.getPositionValue(i, j));
                }
            }
            return result;
        }
    }

    public boolean equals(InvariantsMatrix b) {
        if (this.height() != b.height() || this.width() != b.width()) {
            return false;
        }
        int i = 0;
        int j = 0;
        boolean b2 = true;
        for (i = 0; i < this.width(); i++) {
            b2 = true;
            for (j = 0; j < this.height(); j++) {
                if (this.getPositionValue(j, i) != b.getPositionValue(j, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<InvariantsMatrix> getColumns() {
        ArrayList<InvariantsMatrix> result = new ArrayList<InvariantsMatrix>();
        InvariantsMatrix pom;
        for (int i = 0; i < this.width(); i++) {
            pom = new InvariantsMatrix(this.height(), 1);
            for (int j = 0; j < this.height(); j++) {
                pom.setPositionValue(j, 0, this.getPositionValue(j, i));
            }
            result.add(pom);
        }
        return result;
    }

    public boolean isPositive() {
        for (int i = 0; i < this.height(); i++) {
            for (int j = 0; j < this.width(); j++) {
                if (this.getPositionValue(i, j) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNull() {
        for (int i = 0; i < this.height(); i++) {
            for (int j = 0; j < this.width(); j++) {
                if (this.getPositionValue(i, j) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
