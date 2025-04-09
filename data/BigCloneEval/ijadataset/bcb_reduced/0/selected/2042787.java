package org.opensourcephysics.numerics;

public class HessianMinimize {

    int Iterations;

    double[][] H;

    double[] xp;

    double[] xm;

    double[] xpp;

    double[] xpm;

    double[] xmp;

    double[] xmm;

    private double rmsd_tmp, rmsd;

    private double[] xtmp;

    public double minimize(MultiVarFunction Veq, double[] x, int max, double tol) {
        int m = x.length;
        double[] xxn = new double[m];
        double[] D = new double[m];
        double[] dx = new double[m];
        xtmp = new double[m];
        System.arraycopy(x, 0, xtmp, 0, m);
        rmsd_tmp = Veq.evaluate(x);
        rmsd = 0;
        crudeGuess(Veq, x);
        check_rmsd(Veq, xtmp, x, m);
        for (int i = 0; i < m; i++) {
            dx[i] = (Math.abs(x[i]) + 1.0) / 1e5;
        }
        double err, relerr;
        err = 9999.;
        relerr = 9999.;
        Iterations = 0;
        while (err > tol * 1.e-6 && relerr > tol * 1.e-6 && Iterations < max) {
            Iterations++;
            LUPDecomposition lu = new LUPDecomposition(getHessian(Veq, x, D, dx));
            xxn = lu.solve(D);
            for (int i = 0; i < m; i++) {
                xxn[i] = xxn[i] + x[i];
            }
            err = (x[0] - xxn[0]) * (x[0] - xxn[0]);
            relerr = x[0] * x[0];
            x[0] = xxn[0];
            for (int i = 1; i < m; i++) {
                err = err + (x[i] - xxn[i]) * (x[i] - xxn[i]);
                relerr = relerr + x[i] * x[i];
                x[i] = xxn[i];
            }
            err = Math.sqrt(err);
            relerr = err / (relerr + tol);
        }
        check_rmsd(Veq, xtmp, x, m);
        return err;
    }

    private void allocateArrays(int m) {
        H = new double[m][m];
        xp = new double[m];
        xm = new double[m];
        xpp = new double[m];
        xpm = new double[m];
        xmp = new double[m];
        xmm = new double[m];
    }

    void crudeGuess(MultiVarFunction Veq, double[] x) {
        double sp, s0, sm;
        int m = x.length;
        int Nc = 5;
        double f = 0.35;
        int n = 0;
        double[] xp = new double[m];
        double[] xm = new double[m];
        double[] dx = new double[m];
        for (int i = 0; i < m; i++) {
            dx[i] = (Math.abs(x[i]) + 1.0) / 1.e3;
        }
        while (n < Nc) {
            n++;
            for (int i = 0; i < m; i++) {
                for (int k = 0; k < m; k++) {
                    if (k == i) {
                        xp[i] = x[i] + dx[i];
                        xm[i] = x[i] - dx[i];
                    } else {
                        xp[k] = x[k];
                        xm[k] = x[k];
                    }
                }
                sp = Veq.evaluate(xp);
                s0 = Veq.evaluate(x);
                sm = Veq.evaluate(xm);
                x[i] = x[i] - f * 0.5 * dx[i] * (sp - sm) / (sp - 2.0 * s0 + sm);
                dx[i] = 0.5 * dx[i];
            }
        }
    }

    void check_rmsd(MultiVarFunction Veq, double[] xtmp, double[] xx, int mx) {
        if (java.lang.Double.isNaN(ArrayLib.sum(xx))) {
            rmsd = rmsd_tmp;
            System.arraycopy(xtmp, 0, xx, 0, mx);
        } else {
            rmsd = Veq.evaluate(xx);
            if (rmsd <= rmsd_tmp) {
                rmsd_tmp = rmsd;
                System.arraycopy(xx, 0, xtmp, 0, mx);
            } else {
                rmsd = rmsd_tmp;
                System.arraycopy(xtmp, 0, xx, 0, mx);
            }
        }
    }

    public int getIterations() {
        return Iterations;
    }

    public double[][] getHessian(MultiVarFunction Veq, double[] x, double[] D, double[] dx) {
        int m = x.length;
        if (xp == null || xp.length != m) {
            allocateArrays(m);
        }
        for (int i = 0; i < m; i++) {
            for (int j = i; j < m; j++) {
                if (i == j) {
                    for (int k = 0; k < m; k++) {
                        xp[k] = x[k];
                        xm[k] = x[k];
                    }
                    xp[i] = x[i] + dx[i];
                    xm[i] = x[i] - dx[i];
                    H[i][i] = (Veq.evaluate(xp) - 2.0 * Veq.evaluate(x) + Veq.evaluate(xm)) / (dx[i] * dx[i]);
                } else {
                    for (int k = 0; k < m; k++) {
                        xpp[k] = x[k];
                        xpm[k] = x[k];
                        xmp[k] = x[k];
                        xmm[k] = x[k];
                    }
                    xpp[i] = x[i] + dx[i];
                    xpp[j] = x[j] + dx[j];
                    xpm[i] = x[i] + dx[i];
                    xpm[j] = x[j] - dx[j];
                    xmp[i] = x[i] - dx[i];
                    xmp[j] = x[j] + dx[j];
                    xmm[i] = x[i] - dx[i];
                    xmm[j] = x[j] - dx[j];
                    H[i][j] = ((Veq.evaluate(xpp) - Veq.evaluate(xpm)) / (2.0 * dx[j]) - (Veq.evaluate(xmp) - Veq.evaluate(xmm)) / (2.0 * dx[j])) / (2.0 * dx[i]);
                    H[j][i] = H[i][j];
                }
            }
        }
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < m; k++) {
                xp[k] = x[k];
                xm[k] = x[k];
            }
            xp[i] = x[i] + dx[i];
            xm[i] = x[i] - dx[i];
            D[i] = -(Veq.evaluate(xp) - Veq.evaluate(xm)) / (2.0 * dx[i]);
        }
        return H;
    }
}
