package org.grailrtls.util;

public class CubicSpline {

    private int noOfPoints = 0;

    private double[] x = null;

    private double[] y = null;

    private double[] d2ydx2 = null;

    private double yp1;

    private double ypn;

    private boolean derivativeCalculated = false;

    public CubicSpline(double[] x, double[] y) {
        this.noOfPoints = x.length;
        if (this.noOfPoints != y.length) throw new IllegalArgumentException("Arrays x and y are of different length " + this.noOfPoints + " " + y.length);
        if (this.noOfPoints < 3) throw new IllegalArgumentException("A minimum of three data points is needed");
        this.x = new double[this.noOfPoints];
        this.y = new double[this.noOfPoints];
        this.d2ydx2 = new double[this.noOfPoints];
        for (int i = 0; i < this.noOfPoints; i++) {
            this.x[i] = x[i];
            this.y[i] = y[i];
        }
        setDerivativeLimits();
        checkForDuplicatePoints();
    }

    public void setDerivativeLimits() {
        this.yp1 = Double.NaN;
        this.ypn = Double.NaN;
    }

    private void checkForDuplicatePoints() {
        this.sort();
        this.averageDuplicates();
    }

    private void sort() {
        for (int i = 0; i < this.x.length; i++) {
            for (int j = (this.x.length) - 1; j > i; j--) {
                if (this.x[j] < this.x[i]) {
                    double tmpx = this.x[j];
                    this.x[j] = this.x[i];
                    this.x[i] = tmpx;
                    double tmpy = this.y[j];
                    this.y[j] = this.y[i];
                    this.y[i] = tmpy;
                }
            }
        }
    }

    private void averageDuplicates() {
        int cnt = 0;
        double val = this.x[0];
        for (int i = 1; i < this.x.length; i++) {
            if (this.x[i] == val) cnt++;
            val = this.x[i];
        }
        double[] tempX = new double[this.x.length - cnt];
        double[] tempY = new double[this.x.length - cnt];
        int j = 0;
        int count = 1;
        double accum = this.y[0];
        val = this.x[0];
        for (int i = 1; i < this.x.length; i++) {
            if (this.x[i] == val) {
                count++;
                accum = accum + this.y[i];
            } else {
                tempY[j] = accum / count;
                tempX[j] = val;
                j++;
                count = 1;
                accum = this.y[i];
                val = this.x[i];
            }
        }
        tempY[j] = accum / count;
        tempX[j] = val;
        this.x = tempX;
        this.y = tempY;
        this.noOfPoints = this.x.length;
    }

    public void calcDerivative() {
        double p = 0.0D, qn = 0.0D, sig = 0.0D, un = 0.0D;
        double[] u = new double[this.noOfPoints];
        if (this.yp1 != this.yp1) {
            this.d2ydx2[0] = u[0] = 0.0;
        } else {
            this.d2ydx2[0] = -0.5;
            u[0] = (3.0 / (this.x[1] - this.x[0])) * ((this.y[1] - this.y[0]) / (this.x[1] - this.x[0]) - this.yp1);
        }
        for (int i = 1; i <= this.noOfPoints - 2; i++) {
            sig = (this.x[i] - this.x[i - 1]) / (this.x[i + 1] - this.x[i - 1]);
            p = sig * this.d2ydx2[i - 1] + 2.0;
            this.d2ydx2[i] = (sig - 1.0) / p;
            u[i] = (this.y[i + 1] - this.y[i]) / (this.x[i + 1] - this.x[i]) - (this.y[i] - this.y[i - 1]) / (this.x[i] - this.x[i - 1]);
            u[i] = (6.0 * u[i] / (this.x[i + 1] - this.x[i - 1]) - sig * u[i - 1]) / p;
        }
        if (this.ypn != this.ypn) {
            qn = un = 0.0;
        } else {
            qn = 0.5;
            un = (3.0 / (this.x[this.noOfPoints - 1] - this.x[this.noOfPoints - 2])) * (this.ypn - (this.y[this.noOfPoints - 1] - this.y[this.noOfPoints - 2]) / (this.x[this.noOfPoints - 1] - this.x[this.noOfPoints - 2]));
        }
        this.d2ydx2[this.noOfPoints - 1] = (un - qn * u[this.noOfPoints - 2]) / (qn * this.d2ydx2[this.noOfPoints - 2] + 1.0);
        for (int k = this.noOfPoints - 2; k >= 0; k--) {
            this.d2ydx2[k] = this.d2ydx2[k] * this.d2ydx2[k + 1] + u[k];
        }
        this.derivativeCalculated = true;
    }

    public double interpolate(double xx) {
        if (xx < this.x[0] || xx > this.x[this.noOfPoints - 1]) {
            System.out.print("x(" + xx + ") is outside the range");
            return Double.NaN;
        }
        if (!this.derivativeCalculated) this.calcDerivative();
        double h = 0.0D, b = 0.0D, a = 0.0D, yy = 0.0D;
        int k = 0;
        int klo = 0;
        int khi = this.noOfPoints - 1;
        while (khi - klo > 1) {
            k = (khi + klo) / 2;
            if (this.x[k] > xx) {
                khi = k;
            } else {
                klo = k;
            }
        }
        h = this.x[khi] - this.x[klo];
        a = (this.x[khi] - xx) / h;
        b = (xx - this.x[klo]) / h;
        yy = a * this.y[klo] + b * this.y[khi] + ((a * a * a - a) * this.d2ydx2[klo] + (b * b * b - b) * this.d2ydx2[khi]) * (h * h) / 6.0;
        return yy;
    }

    public double getMinX() {
        return this.x[0];
    }

    public double getMaxX() {
        return this.x[this.noOfPoints - 1];
    }
}
