package client.presentation.map;

public class CoordCalculator implements IFieldConstants {

    private int iMapWidth;

    private int iMapHeight;

    private int iRowLen;

    private int iColumnLen;

    private int iYOffset;

    private int iXOffset;

    public Object clone() {
        CoordCalculator ret = new CoordCalculator(iRowLen, iColumnLen, iXOffset, iYOffset);
        ret.iMapHeight = this.iMapHeight;
        ret.iMapWidth = this.iMapWidth;
        ret.iRowLen = this.iRowLen;
        ret.iColumnLen = this.iColumnLen;
        ret.iXOffset = this.iXOffset;
        ret.iYOffset = this.iYOffset;
        return ret;
    }

    public void setXOffset(int _iX) {
        iXOffset = _iX;
    }

    public int getXOffset() {
        return iXOffset;
    }

    public void setYOffset(int _iY) {
        iYOffset = _iY;
    }

    public int getYOffset() {
        return iYOffset;
    }

    ;

    public int getRowLen() {
        return iRowLen;
    }

    public int getColumnLen() {
        return iColumnLen;
    }

    public int getRow(int _iIndex) {
        return _iIndex / this.iRowLen;
    }

    public int getCol(int _iIndex) {
        return _iIndex % this.iRowLen;
    }

    public CoordCalculator(int _iRowLen, int _iColumnLen, int _iXOffset, int _iYOffset) {
        this.iRowLen = _iRowLen;
        this.iColumnLen = _iColumnLen;
        this.iYOffset = _iYOffset;
        this.iXOffset = _iXOffset;
        this.iMapWidth = (_iRowLen + _iColumnLen) * FIELD_WIDTH / 2 + iXOffset;
        this.iMapHeight = this.getY(_iRowLen * _iColumnLen - 1) + FIELD_HEIGHT + iYOffset;
    }

    public int getWidth() {
        return this.iMapWidth;
    }

    public int getHeight() {
        return this.iMapHeight;
    }

    public int getX(int _iIndex) {
        int iCol = this.getCol(_iIndex);
        int iRow = this.getRow(_iIndex);
        return (iColumnLen - 1) * FIELD_WIDTH / 2 + (iCol - iRow) * (FIELD_WIDTH / 2) + iXOffset;
    }

    public int getY(int _iIndex) {
        int iCol = this.getCol(_iIndex);
        int iRow = this.getRow(_iIndex);
        return this.iYOffset + (iRow + iCol) * (FIELD_HEIGHT / 2);
    }

    public int getIndex(int _iX, int _iY) {
        int iXn;
        int iYn;
        int iXf;
        int iYf;
        int iColumnNr;
        int iRowNr;
        iXn = (_iX - getX(0)) / (FIELD_WIDTH / 2);
        if (_iX < getX(0)) {
            iXn--;
        }
        iYn = (_iY - getY(0)) / (FIELD_HEIGHT / 2);
        if (_iY < getY(0)) {
            iYn--;
        }
        iXf = _iX - getX(0) - iXn * (FIELD_WIDTH / 2);
        iYf = _iY - getY(0) - iYn * (FIELD_HEIGHT / 2);
        if ((iXn + iYn) % 2 != 0) {
            if (2 * iYf < iXf) {
                iYn--;
            } else {
                iXn--;
            }
        } else {
            if (2 * iYf + iXf < (FIELD_WIDTH / 2)) {
                iXn--;
                iYn--;
            }
        }
        iColumnNr = (iXn + iYn) / 2;
        iRowNr = (iYn - iXn) / 2;
        if (((iColumnNr >= this.iRowLen) || (iRowNr >= this.iColumnLen)) || (iColumnNr < 0) || (iRowNr < 0)) {
            return -1;
        }
        return iColumnNr + iRowNr * this.iRowLen;
    }
}

;
