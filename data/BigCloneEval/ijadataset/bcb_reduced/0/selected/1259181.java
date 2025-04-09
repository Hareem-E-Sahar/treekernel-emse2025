package edu.mit.aero.foamcut;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class implements a 2D cubic spline passing through a set of x,y coordinates.
 * The x coordinate must be monotonically increasing.
 * @author mschafer
 */
public class Spline implements Serializable {

    /** The x coordinate of the control points. */
    double xControl[];

    /** The y coordinate. */
    double yControl[];

    /** The slope dy/dx at each point.  Found by solving tri-diagonal system. */
    double y_x[];

    /**
     * This class encapsulates data for returning from spline functions.
     */
    public static class Point implements Serializable {

        public double x;

        public double y;

        public double y_x;

        public double y_xx;

        public int interval;
    }

    /**
     * Copy constructor.  Creates a new Spline that is a deep copy of another.
     * @param cpy The Spline to copy.
     */
    public Spline(Spline cpy) {
        xControl = Arrays.copyOf(cpy.xControl, cpy.xControl.length);
        yControl = Arrays.copyOf(cpy.yControl, cpy.yControl.length);
        y_x = Arrays.copyOf(cpy.y_x, cpy.y_x.length);
    }

    /** 
     * Creates a new instance of Spline passing through the given points.
     * Note that x must be monotonic.
     * @param x x coords for spline.
     * @param y y coords for spline.
     */
    public Spline(double x[], double y[]) {
        xControl = x.clone();
        yControl = y.clone();
        calc();
    }

    public int getNumberOfControlPoints() {
        return xControl.length;
    }

    /**
     * @return A copy of the x coordinate of the control points.
     */
    public double[] getXControl() {
        return xControl.clone();
    }

    /**
     * @return A copy of the y coordinate of the control points.
     */
    public double[] getYControl() {
        return yControl.clone();
    }

    /**
     * @return A copy of the slope (dy/dx) at the control points.
     */
    public double[] getDyDx() {
        return y_x.clone();
    }

    public Point getControlPoint(int i) {
        if (i < 0 || i >= xControl.length) {
            IllegalArgumentException iax = new IllegalArgumentException("Point index is out of range");
            throw (iax);
        }
        return evaluateAll(xControl[i]);
    }

    /** Evaluate the spline.
     * @param x The spline coordinate to be evaluated.
     * @return The value (y) of the spline at x.  Note that the evaluation will
     * extrapolate if x is outside the spline.
     */
    public double evaluate(double x) {
        int i;
        i = findInterval(x);
        double dx = xControl[i] - xControl[i - 1];
        double t = (x - xControl[i - 1]) / dx;
        double cy1 = dx * y_x[i - 1] - yControl[i] + yControl[i - 1];
        double cy2 = dx * y_x[i] - yControl[i] + yControl[i - 1];
        double y = t * yControl[i] + (1. - t) * yControl[i - 1] + (t - t * t) * ((1. - t) * cy1 - t * cy2);
        return y;
    }

    /** 
     * Evaluate the spline at multiple points.
     * @param x Array of spline coordinates to be evaluated.
     * @return Array of evaluated spline points.
     */
    public double[] evaluate(double x[]) {
        int i;
        double y[] = new double[x.length];
        for (i = 0; i < x.length; i++) {
            y[i] = evaluate(x[i]);
        }
        return y;
    }

    /** 
     * Evaluate the Spline and its first and second derivative at a point.
     * @param x The spline coordinate to evaluate.
     * @return A Spline.Point evaluated at x.
     */
    public Spline.Point evaluateAll(double x) {
        Spline.Point sp = new Spline.Point();
        sp.x = x;
        int iseg = findInterval(x);
        sp.interval = iseg;
        double dx = xControl[iseg] - xControl[iseg - 1];
        double t = (x - xControl[iseg - 1]) / dx;
        double f0 = yControl[iseg - 1];
        double f1 = dx * y_x[iseg - 1];
        double f2 = -dx * (2. * y_x[iseg - 1] + y_x[iseg]) + 3. * (yControl[iseg] - yControl[iseg - 1]);
        double f3 = dx * (y_x[iseg - 1] + y_x[iseg]) - 2. * (yControl[iseg] - yControl[iseg - 1]);
        sp.y = f0 + t * (f1 + t * (f2 + t * f3));
        sp.y_x = f1 + t * (2. * f2 + t * 3. * f3);
        sp.y_xx = 2. * f2 + t * 6. * f3;
        sp.y_x /= dx;
        sp.y_xx /= dx * dx;
        return sp;
    }

    /** 
     * Evaluate the Spline and its first and second derivative at an array
     * of points.
     * @param x The spline coordinate to evaluate.
     * @return An array of Spline.Point evaluate at x. 
     */
    public Spline.Point[] evaluateAll(double x[]) {
        Spline.Point sp[] = new Spline.Point[x.length];
        int i;
        for (i = 0; i < x.length; i++) {
            sp[i] = evaluateAll(x[i]);
        }
        return sp;
    }

    /**
     * Inserts a new control point that is already on the spline.  The x coordinate
     * of the new point is specified and the y coordinate is determined by evaluating
     * the spline.  Extrapolation will be used if xp is outside the range of the
     * existing spline.
     * @param xp The x coordinate where the new control point will be inserted.
     * @return The index where the point was inserted.
     */
    public int insertControlPoint(double xp) {
        int ip;
        if (xp < xControl[0]) {
            ip = 0;
        } else if (xp > xControl[xControl.length - 1]) {
            ip = xControl.length;
        } else {
            ip = findInterval(xp);
            if (xControl[ip - 1] == xp) return ip - 1;
        }
        Point p = evaluateAll(xp);
        int n = xControl.length;
        double x[] = new double[n + 1];
        double y[] = new double[n + 1];
        double yx[] = new double[n + 1];
        for (int i = 0; i < n + 1; i++) {
            if (i < ip) {
                x[i] = xControl[i];
                y[i] = yControl[i];
                yx[i] = y_x[i];
            } else if (i == ip) {
                x[i] = p.x;
                y[i] = p.y;
                yx[i] = p.y_x;
            } else {
                x[i] = xControl[i - 1];
                y[i] = yControl[i - 1];
                yx[i] = y_x[i - 1];
            }
        }
        xControl = x;
        yControl = y;
        y_x = yx;
        return ip;
    }

    public void replaceEndPoint(double xEnd, boolean isStart) {
        int ip = insertControlPoint(xEnd);
        if (isStart) {
            if (ip != 0) {
                xControl = Arrays.copyOfRange(xControl, ip, xControl.length);
                yControl = Arrays.copyOfRange(yControl, ip, yControl.length);
                y_x = Arrays.copyOfRange(y_x, ip, y_x.length);
            }
        } else {
            if (ip != xControl.length - 1) {
                xControl = Arrays.copyOfRange(xControl, 0, ip + 1);
                yControl = Arrays.copyOfRange(yControl, 0, ip + 1);
                y_x = Arrays.copyOfRange(y_x, 0, ip + 1);
            }
        }
    }

    /** 
     * Invert the spline.  Find the x value that corresponds to the y argument.
     * This is not a black box routine since the spline may be multi-valued or
     * undefined at the specified y value.  Therefore a good initial guess is
     * required.
     * @param xi Initial guess for x.
     * @param y Y coordinate for which the inverse spline value is desired.
     * @return The x value of the spline inverse.
     */
    public double invert(double xi, double y) {
        double x = xi;
        double yv = evaluate(x);
        double dy = y - yv;
        int iter = 0;
        double dyTol = 1.e-6;
        if (Math.abs(y) > 1) dyTol = 1.e-6 * Math.abs(y);
        boolean converged = false;
        while (!converged && iter < 20) {
            Point d = evaluateAll(x);
            double dx = dy / d.y_x;
            x += dx;
            yv = evaluate(x);
            dy = y - yv;
            if (Math.abs(dy) < dyTol) converged = true;
        }
        if (converged) return x; else {
            IllegalArgumentException ex = new IllegalArgumentException("Spline invert failed to converge");
            throw (ex);
        }
    }

    /** 
     * Calculate the derivatives for the Spline. 
     * This functions needs to be called whenever the x,y values change.
     */
    protected void calc() {
        assert (xControl.length == yControl.length);
        int n = xControl.length;
        y_x = new double[n];
        double a[] = new double[n];
        double b[] = new double[n];
        double c[] = new double[n];
        double dxm, dxp;
        int i;
        for (i = 1; i < n - 1; i++) {
            dxm = xControl[i] - xControl[i - 1];
            dxp = xControl[i + 1] - xControl[i];
            b[i] = dxp;
            a[i] = 2. * (dxm + dxp);
            c[i] = dxm;
            y_x[i] = 3. * ((yControl[i + 1] - yControl[i]) * dxm / dxp + (yControl[i] - yControl[i - 1]) * dxp / dxm);
        }
        a[0] = 2.;
        c[0] = 1.;
        y_x[0] = 3. * (yControl[1] - yControl[0]) / (xControl[1] - xControl[0]);
        b[n - 1] = 1.;
        a[n - 1] = 2.;
        y_x[n - 1] = 3. * (yControl[n - 1] - yControl[n - 2]) / (xControl[n - 1] - xControl[n - 2]);
        trisol(a, b, c, y_x);
    }

    /**
     * Return the segment containing the specified value of x. 
     * @param x The x coordinate being searched for.
     * @return The index of the first point greater than (not ==) x up to a
     * maximum of length-1.
     * @todo Optimize this by remembering the last segment found and starting
     * search from there.
     */
    int findInterval(double x) {
        int ilow, imid, i, n;
        n = xControl.length;
        ilow = 0;
        i = n - 1;
        while ((i - ilow) > 1) {
            imid = (i + ilow) / 2;
            if (x < xControl[imid]) {
                i = imid;
            } else {
                ilow = imid;
            }
        }
        return i;
    }

    /** 
     * Solve a tridiagonal system in the form:
     * a c           d
     * b a c         d
     *   b a c       d
     *     b a       d
     */
    public static void trisol(double a[], double b[], double c[], double d[]) {
        assert (a.length == b.length && a.length == c.length && a.length == d.length);
        int n = a.length;
        int k, km;
        for (k = 1; k < n; k++) {
            km = k - 1;
            c[km] = c[km] / a[km];
            d[km] = d[km] / a[km];
            a[k] = a[k] - b[k] * c[km];
            d[k] = d[k] - b[k] * d[km];
        }
        d[n - 1] = d[n - 1] / a[n - 1];
        for (k = n - 2; k >= 0; k--) {
            d[k] = d[k] - c[k] * d[k + 1];
        }
    }
}
