package org.opensourcephysics.numerics;

/**
 * Class Root defines various root finding algorithms.
 *
 * This class cannot be subclassed or instantiated because all methods are static.
 *
 * @author Wolfgang Christian
 */
public class Root {

    static final int MAX_ITERATIONS = 15;

    private Root() {
    }

    /**
   * Solves for the real roots of the quadratic equation
   * ax<sup>2</sup>+bx+c=0.
   *
   * @param a double quadratic term coefficient
   * @param b double linear term coefficient
   * @param c double constant term
   * @return double[] an array containing the two roots.
   */
    public static double[] quadraticReal(final double a, final double b, final double c) {
        final double roots[] = new double[2];
        final double q = -0.5 * (b + ((b < 0.0) ? -1.0 : 1.0) * Math.sqrt(b * b - 4.0 * a * c));
        roots[0] = q / a;
        roots[1] = c / q;
        return roots;
    }

    /**
   * Solves for the complex roots of the quadratic equation
   * ax<sup>2</sup>+bx+c=0.
   *
   * @param a double quadratic term coefficient
   * @param b double linear term coefficient
   * @param c double constant term
   * @return double[] an array containing the two roots.
   */
    public static double[][] quadratic(final double a, final double b, final double c) {
        final double roots[][] = new double[2][2];
        double disc = b * b - 4.0 * a * c;
        if (disc < 0) {
            roots[1][0] = roots[0][0] = -b / 2 / a;
            roots[1][1] -= roots[0][1] = Math.sqrt(-disc) / 2 / a;
            ;
            return roots;
        }
        final double q = -0.5 * (b + ((b < 0.0) ? -1.0 : 1.0) * Math.sqrt(disc));
        roots[0][0] = q / a;
        roots[1][0] = c / q;
        return roots;
    }

    /**
   * Solves for the roots of the cubic equation
   * ax<sup>3</sup>+bx<sup>2</sup>+cx+d=0.
   *
   * @param a double cubic term coefficient
   * @param b double quadratic term coefficient
   * @param c double linear term coefficient
   * @param d double constant term
   * @return double[] an array containing the two roots.
   */
    public static double[][] cubic(final double a, final double b, final double c, final double d) {
        final double roots[][] = new double[3][2];
        double A = b / a, B = c / a, C = d / a;
        double A2 = A * A;
        double Q = (3 * B - A2) / 9;
        double R = (9 * A * B - 27 * C - 2 * A * A2) / 54;
        double D = Q * Q * Q + R * R;
        if (D == 0) {
            double S = (R < 0) ? -Math.pow(-R, 1.0 / 3) : Math.pow(R, 1.0 / 3);
            roots[0][0] = -A / 3 + 2 * S;
            roots[2][0] = roots[1][0] = -A / 3 - S;
        } else if (D > 0) {
            D = Math.sqrt(D);
            double S = (R + D < 0) ? -Math.pow(-R - D, 1.0 / 3) : Math.pow(R + D, 1.0 / 3);
            double T = (R - D < 0) ? -Math.pow(-R + D, 1.0 / 3) : Math.pow(R - D, 1.0 / 3);
            roots[0][0] = -A / 3 + S + T;
            roots[2][0] = roots[1][0] = -A / 3 - (S + T) / 2;
            roots[2][1] -= roots[1][1] = Math.sqrt(3) * (S - T) / 2;
        } else {
            Q = -Q;
            double theta = Math.acos(R / Math.sqrt(Q * Q * Q)) / 3;
            Q = 2 * Math.sqrt(Q);
            A = A / 3;
            roots[0][0] = Q * Math.cos(theta) - A;
            roots[1][0] = Q * Math.cos(theta + 2 * Math.PI / 3) - A;
            roots[2][0] = Q * Math.cos(theta + 4 * Math.PI / 3) - A;
        }
        return roots;
    }

    /**
   * Implements Newton's method for finding the root of a function.
   * The derivative is calculated numerically using the central difference approximation.
   *
   * @param f Function the function
   * @param x double guess the root
   * @param tol double computation tolerance
   * @return double the root or NaN if root not found.
   */
    public static double newton(final Function f, double x, final double tol) {
        int count = 0;
        while (count < MAX_ITERATIONS) {
            double xold = x;
            double df = 0;
            try {
                df = fxprime(f, x, tol);
            } catch (NumericMethodException ex) {
                return Double.NaN;
            }
            x -= f.evaluate(x) / df;
            if (Util.relativePrecision(Math.abs(x - xold), x) < tol) {
                return x;
            }
            count++;
        }
        NumericsLog.fine(count + " newton root trials made - no convergence achieved");
        return Double.NaN;
    }

    /**
   * Implements Newton's method for finding the root of a function.
   *
   * @param f Function the function
   * @param df Function the derivative of the function
   * @param x double guess the root
   * @param tol double computation tolerance
   * @return double the root or NaN if root not found.
   */
    public static double newton(final Function f, final Function df, double x, final double tol) {
        int count = 0;
        while (count < MAX_ITERATIONS) {
            double xold = x;
            x -= f.evaluate(x) / df.evaluate(x);
            if (Util.relativePrecision(Math.abs(x - xold), x) < tol) {
                return x;
            }
            count++;
        }
        NumericsLog.fine(count + " newton root trials made - no convergence achieved");
        return Double.NaN;
    }

    /**
   * Implements the bisection method for finding the root of a function.
   * @param f Function the function
   * @param x1 double lower
   * @param x2 double upper
   * @param tol double computation tolerance
   * @return double the root or NaN if root not found
   */
    public static double bisection(final Function f, double x1, double x2, final double tol) {
        int count = 0;
        int maxCount = (int) (Math.log(Math.abs(x2 - x1) / tol) / Math.log(2));
        maxCount = Math.max(MAX_ITERATIONS, maxCount) + 2;
        double y1 = f.evaluate(x1), y2 = f.evaluate(x2);
        if (y1 * y2 > 0) {
            NumericsLog.fine(count + " bisection root - interval endpoints must have opposite sign");
            return Double.NaN;
        }
        while (count < maxCount) {
            double x = (x1 + x2) / 2;
            double y = f.evaluate(x);
            if (Util.relativePrecision(Math.abs(x1 - x2), x) < tol) {
                return x;
            }
            if (y * y1 > 0) {
                x1 = x;
                y1 = y;
            } else {
                x2 = x;
                y2 = y;
            }
            count++;
        }
        NumericsLog.fine(count + " bisection root trials made - no convergence achieved");
        return Double.NaN;
    }

    /**
   * Implements Newton's method for finding the root but switches to the bisection method if the
   * the estimate is not between xleft and xright.
   *
   * Method contributed by: J E Hasbun
   *
   * A Newton Raphson result is accepted if it is within the known bounds,
   * else a bisection step is taken.
   * Ref: Computational Physics by P. L. Devries (J. Wiley, 1993)
   * input: [xleft,xright] is the interval wherein fx() has a root, icmax is the
   * maximum iteration number, and tol is the tolerance level
   * output: returns xbest as the value of the function
   * Reasonable values of icmax and tol are 25, 5e-3.
   *
   * Returns the root or NaN if root not found.
   *
   * @param xleft double
   * @param xright double
   * @param tol double tolerance
   * @param icmax int number of trials
   * @return double the root
   */
    public static double newtonBisection(Function f, double xleft, double xright, double tol, int icmax) {
        double rtest = 10 * tol;
        double xbest, fleft, fright, fbest, derfbest, delta;
        int icount = 0, iflag = 0;
        fleft = f.evaluate(xleft);
        fright = f.evaluate(xright);
        if (fleft * fright >= 0) {
            iflag = 1;
        }
        switch(iflag) {
            case 1:
                System.out.println("No solution possible");
                break;
        }
        if (Math.abs(fleft) <= Math.abs(fright)) {
            xbest = xleft;
            fbest = fleft;
        } else {
            xbest = xright;
            fbest = fright;
        }
        derfbest = fxprime(f, xbest, tol);
        while ((icount < icmax) && (rtest > tol)) {
            icount++;
            if ((derfbest * (xbest - xleft) - fbest) * (derfbest * (xbest - xright) - fbest) <= 0) {
                delta = -fbest / derfbest;
                xbest = xbest + delta;
            } else {
                delta = (xright - xleft) / 2;
                xbest = (xleft + xright) / 2;
            }
            rtest = Math.abs(delta / xbest);
            if (rtest <= tol) {
            } else {
                fbest = f.evaluate(xbest);
                derfbest = fxprime(f, xbest, tol);
                if (fleft * fbest <= 0) {
                    xright = xbest;
                    fright = fbest;
                } else {
                    xleft = xbest;
                    fleft = fbest;
                }
            }
        }
        if ((icount > icmax) || (rtest > tol)) {
            NumericsLog.fine(icmax + " Newton and bisection trials made - no convergence achieved");
            return Double.NaN;
        }
        return xbest;
    }

    public static double newtonMultivar(VectorFunction feqs, double xx[], int max, double tol) {
        int Ndim = xx.length;
        double[] xxn = new double[Ndim];
        double[] F = new double[Ndim];
        int Iterations = 0;
        double err, relerr;
        err = 9999.;
        relerr = 9999.;
        while ((err > tol * 1.e-6) && (relerr > tol * 1.e-6) && (Iterations < max)) {
            Iterations++;
            LUPDecomposition lu = new LUPDecomposition(getJacobian(feqs, Ndim, xx, tol / 100.));
            F = feqs.evaluate(xx, F);
            xxn = lu.solve(F);
            for (int i = 0; i < Ndim; i++) {
                xxn[i] = xx[i] - xxn[i];
            }
            err = (xx[0] - xxn[0]) * (xx[0] - xxn[0]);
            relerr = xx[0] * xx[0];
            xx[0] = xxn[0];
            for (int i = 1; i < Ndim; i++) {
                err = err + (xx[i] - xxn[i]) * (xx[i] - xxn[i]);
                relerr = relerr + xx[i] * xx[i];
                xx[i] = xxn[i];
            }
            err = Math.sqrt(err);
            relerr = err / (relerr + tol);
        }
        return err;
    }

    /**
 * Computes the Jacobian using a finite difference approximation.
 * Contributed to OSP by J E Hasbun 2007.
 *
 * @param feqs VectorFunction - the function containing n equations
 * @param n int - number of equations
 * @param xx double[] - the variable array at which the Jacobian is calculated
 * @param tol double - the small change to find the derivatives
 * @return double[][]  J - the square matrix containing the Jacobian
 */
    public static double[][] getJacobian(VectorFunction feqs, int n, double xx[], double tol) {
        double[][] J = new double[n][n];
        double[][] xxp = new double[n][n];
        double[][] xxm = new double[n][n];
        double[][] fp = new double[n][n];
        double[][] fm = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                xxp[i][j] = xx[j];
                xxm[i][j] = xx[j];
            }
            xxp[i][i] = xxp[i][i] + tol;
            xxm[i][i] = xxm[i][i] - tol;
        }
        for (int i = 0; i < n; i++) {
            fp[i] = feqs.evaluate(xxp[i], fp[i]);
            fm[i] = feqs.evaluate(xxm[i], fm[i]);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                J[i][j] = (fp[j][i] - fm[j][i]) / tol / 2.;
            }
        }
        return J;
    }

    /**
   * Central difference approximation to the derivative for use in Newton's mehtod.
   * @param f Function
   * @param x double
   * @param tol double
   * @return double
   */
    static final double fxprime(Function f, double x, double tol) {
        double del = tol / 10;
        double der = (f.evaluate(x + del) - f.evaluate(x - del)) / del / 2.0;
        return der;
    }
}
