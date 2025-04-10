package org.apache.commons.math3.analysis.interpolation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.util.MathArrays;

/**
 * Computes a natural (also known as "free", "unclamped") cubic spline interpolation for the data set.
 * <p>
 * The {@link #interpolate(double[], double[])} method returns a {@link PolynomialSplineFunction}
 * consisting of n cubic polynomials, defined over the subintervals determined by the x values,
 * x[0] < x[i] ... < x[n].  The x values are referred to as "knot points."</p>
 * <p>
 * The value of the PolynomialSplineFunction at a point x that is greater than or equal to the smallest
 * knot point and strictly less than the largest knot point is computed by finding the subinterval to which
 * x belongs and computing the value of the corresponding polynomial at <code>x - x[i] </code> where
 * <code>i</code> is the index of the subinterval.  See {@link PolynomialSplineFunction} for more details.
 * </p>
 * <p>
 * The interpolating polynomials satisfy: <ol>
 * <li>The value of the PolynomialSplineFunction at each of the input x values equals the
 *  corresponding y value.</li>
 * <li>Adjacent polynomials are equal through two derivatives at the knot points (i.e., adjacent polynomials
 *  "match up" at the knot points, as do their first and second derivatives).</li>
 * </ol></p>
 * <p>
 * The cubic spline interpolation algorithm implemented is as described in R.L. Burden, J.D. Faires,
 * <u>Numerical Analysis</u>, 4th Ed., 1989, PWS-Kent, ISBN 0-53491-585-X, pp 126-131.
 * </p>
 *
 * @version $Id: SplineInterpolator.java 1244107 2012-02-14 16:17:55Z erans $
 *
 */
public class SplineInterpolator implements UnivariateInterpolator {

    /**
     * Computes an interpolating function for the data set.
     * @param x the arguments for the interpolation points
     * @param y the values for the interpolation points
     * @return a function which interpolates the data set
     * @throws DimensionMismatchException if {@code x} and {@code y}
     * have different sizes.
     * @throws org.apache.commons.math3.exception.NonMonotonicSequenceException
     * if {@code x} is not sorted in strict increasing order.
     * @throws NumberIsTooSmallException if the size of {@code x} is smaller
     * than 3.
     */
    public PolynomialSplineFunction interpolate(double x[], double y[]) {
        if (x.length != y.length) {
            throw new DimensionMismatchException(x.length, y.length);
        }
        if (x.length < 3) {
            throw new NumberIsTooSmallException(LocalizedFormats.NUMBER_OF_POINTS, x.length, 3, true);
        }
        int n = x.length - 1;
        MathArrays.checkOrder(x);
        double h[] = new double[n];
        for (int i = 0; i < n; i++) {
            h[i] = x[i + 1] - x[i];
        }
        double mu[] = new double[n];
        double z[] = new double[n + 1];
        mu[0] = 0d;
        z[0] = 0d;
        double g = 0;
        for (int i = 1; i < n; i++) {
            g = 2d * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / g;
            z[i] = (3d * (y[i + 1] * h[i - 1] - y[i] * (x[i + 1] - x[i - 1]) + y[i - 1] * h[i]) / (h[i - 1] * h[i]) - h[i - 1] * z[i - 1]) / g;
        }
        double b[] = new double[n];
        double c[] = new double[n + 1];
        double d[] = new double[n];
        z[n] = 0d;
        c[n] = 0d;
        for (int j = n - 1; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (y[j + 1] - y[j]) / h[j] - h[j] * (c[j + 1] + 2d * c[j]) / 3d;
            d[j] = (c[j + 1] - c[j]) / (3d * h[j]);
        }
        PolynomialFunction polynomials[] = new PolynomialFunction[n];
        double coefficients[] = new double[4];
        for (int i = 0; i < n; i++) {
            coefficients[0] = y[i];
            coefficients[1] = b[i];
            coefficients[2] = c[i];
            coefficients[3] = d[i];
            polynomials[i] = new PolynomialFunction(coefficients);
        }
        return new PolynomialSplineFunction(x, polynomials);
    }
}
