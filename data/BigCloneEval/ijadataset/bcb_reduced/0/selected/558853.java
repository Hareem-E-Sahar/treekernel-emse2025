package edu.ucsd.ncmir.spl.numerical;

/**
 *
 * @author spl
 */
public class SplineInterpolator extends Interpolator {

    private double[] _coeff;

    public SplineInterpolator(double[][] xy) {
        super(xy);
        this.generateCoefficients();
    }

    public SplineInterpolator(double[] x, double[] y) {
        super(x, y);
        this.generateCoefficients();
    }

    private void generateCoefficients() {
        double[] u = new double[this._x.length];
        this._coeff = new double[this._x.length];
        this._coeff[0] = u[0] = 0.0;
        for (int i = 1; i < this._x.length - 1; i++) {
            double sig = (super._x[i] - super._x[i - 1]) / (super._x[i + 1] - super._x[i - 1]);
            double p = sig * this._coeff[i - 1] + 2.0;
            this._coeff[i] = (sig - 1.0) / p;
            u[i] = (6.0 * ((super._y[i + 1] - super._y[i]) / (super._x[i + 1] - super._x[i]) - (super._y[i] - super._y[i - 1]) / (super._x[i] - super._x[i - 1])) / (super._x[i + 1] - super._x[i - 1]) - sig * u[i - 1]) / p;
        }
        double qn = 0.0;
        double un = 0.0;
        this._coeff[this._x.length - 1] = (un - qn * u[this._x.length - 2]) / (qn * this._coeff[this._x.length - 2] + 1.0);
        for (int k = this._x.length - 2; k >= 0; k--) this._coeff[k] = this._coeff[k] * this._coeff[k + 1] + u[k];
    }

    public double evaluate(double xt) {
        int klo = 0;
        int khi = this._x.length - 1;
        while ((khi - klo) > 1) {
            int k = (khi + klo) / 2;
            if (this._x[k] > xt) khi = k; else klo = k;
        }
        double h;
        double r;
        if ((h = this._x[khi] - this._x[klo]) == 0.0) r = this._y[khi]; else {
            double a;
            double b;
            a = (this._x[khi] - xt) / h;
            b = (xt - this._x[klo]) / h;
            r = a * this._y[klo] + b * this._y[khi] + (((a * a * a) - a) * this._coeff[klo] + ((b * b * b) - b) * this._coeff[khi]) * (h * h) / 6.0;
        }
        return r;
    }
}
