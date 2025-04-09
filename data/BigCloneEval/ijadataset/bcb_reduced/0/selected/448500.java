package org.renjin.stats;

import org.renjin.eval.EvalException;
import org.renjin.sexp.DoubleVector;

public class Approx {

    public static class appr_meth {

        double ylow;

        double yhigh;

        double f1;

        double f2;

        int kind;
    }

    public static double approx1(double v, double x[], double y[], int n, appr_meth Meth) {
        int i, j, ij;
        if (n == 0) return Double.NaN;
        i = 0;
        j = n - 1;
        if (v < x[i]) return Meth.ylow;
        if (v > x[j]) return Meth.yhigh;
        while (i < j - 1) {
            ij = (i + j) / 2;
            if (v < x[ij]) j = ij; else i = ij;
        }
        if (v == x[j]) return y[j];
        if (v == x[i]) return y[i];
        if (Meth.kind == 1) {
            return y[i] + (y[j] - y[i]) * ((v - x[i]) / (x[j] - x[i]));
        } else {
            return y[i] * Meth.f1 + y[j] * Meth.f2;
        }
    }

    public static void R_approx(double x[], double y[], int nxy, double xout[], int nout, int method, double yleft, double yright, double f) {
        int i;
        appr_meth M = new appr_meth();
        switch(method) {
            case 1:
                break;
            case 2:
                if (!DoubleVector.isFinite(f) || f < 0 || f > 1) throw new EvalException("approx(): invalid f value");
                M.f2 = f;
                M.f1 = 1 - f;
                break;
            default:
                throw new EvalException("approx(): invalid interpolation method");
        }
        for (i = 0; i < nxy; i++) if (DoubleVector.isNA(x[i]) || DoubleVector.isNA(y[i])) throw new EvalException("approx(): attempted to interpolate NA values");
        M.kind = method;
        M.ylow = yleft;
        M.yhigh = yright;
        for (i = 0; i < nout; i++) if (!DoubleVector.isNA(xout[i])) xout[i] = approx1(xout[i], x, y, nxy, M);
    }

    public static void R_approxtest(double x[], double y[], int nxy, int method, double f) {
        int i;
        switch(method) {
            case 1:
                break;
            case 2:
                if (!DoubleVector.isFinite(f) || f < 0 || f > 1) throw new EvalException("approx(): invalid f value");
                break;
            default:
                throw new EvalException("approx(): invalid interpolation method");
        }
        for (i = 0; i < nxy; i++) if (DoubleVector.isNA(x[i]) || DoubleVector.isNA(y[i])) throw new EvalException("approx(): attempted to interpolate NA values");
    }

    public static void R_approxfun(double x[], double y[], int nxy, double xout[], int nout, int method, double yleft, double yright, double f) {
        int i;
        appr_meth M = new appr_meth();
        M.f2 = f;
        M.f1 = 1 - f;
        M.kind = method;
        M.ylow = yleft;
        M.yhigh = yright;
        for (i = 0; i < nout; i++) if (!DoubleVector.isNA(xout[i])) xout[i] = approx1(xout[i], x, y, nxy, M);
    }
}
