package mipt.crec.lab.compmath.interp.math.methods;

import mipt.math.Number;
import mipt.math.sys.num.Method;

/**
 *
 * @author Lebedeva
 * @author Loginov Pavel
 */
public abstract class AbstractInterpolationMethod implements Method {

    protected Number[][] dots;

    protected Double[] interval;

    public abstract void prepareData();

    public abstract Number calcValue(Number x);

    public void setParam(Object param) {
    }

    public void setDots(Number[][] dots) {
        this.dots = dots;
    }

    protected int findGridIndex(Number x) {
        return findGridIndex(x, 0, -1 + dots.length);
    }

    /**
	 * ���������� ���� ������ ���� ����� ��� ��������� �.
	 * ������ ����������� ��������� �(startPos)<=�<�(lastPos).
	 * ������������ �������� - �(startPos)  ���   lastPos-startPos=1.
	 */
    private int findGridIndex(Number x, int startPos, int lastPos) {
        int diff = lastPos - startPos;
        if (diff <= 0) return -1;
        int compareStartPos = x.compareTo(dots[startPos][0]);
        int compareLastPos = x.compareTo(dots[lastPos][0]);
        if (compareStartPos == -1 || compareLastPos == 1) return -1;
        if (compareStartPos == 0 || diff == 1) return startPos;
        if (compareLastPos == 0) return lastPos - 1;
        int middlePos = (startPos + lastPos) / 2;
        int compareMiddlePos = x.compareTo(dots[middlePos][0]);
        if (compareMiddlePos == 0) return middlePos;
        if (compareMiddlePos == -1) return findGridIndex(x, startPos, middlePos);
        return findGridIndex(x, middlePos, lastPos);
    }

    public void setInterval(Double[] interval) {
        this.interval = interval;
    }

    public Double[] getInterval() {
        return this.interval;
    }
}
