package org.jquantlib.math.integrals;

import org.jquantlib.QL;
import org.jquantlib.math.Ops;

/**
 * Integral of a 1-dimensional function using the Gauss-Kronrod methods
 * <p>
 * This class provide a non-adaptive integration procedure which uses fixed Gauss-Kronrod abscissae to sample the integrand at a
 * maximum of 87 points. It is provided for fast integration of smooth functions.
 * <p>
 * This function applies the Gauss-Kronrod 10-point, 21-point, 43-point and 87-point integration rules in succession until an
 * estimate of the integral of f over (a, b) is achieved within the desired absolute and relative error limits, epsabs and epsrel.
 * The function returns the final approximation, result, an estimate of the absolute error, abserr and the number of function
 * evaluations used, neval. The Gauss-Kronrod rules are designed in such a way that each rule uses all the results of its
 * predecessors, in order to minimize the total number of function evaluations.
 *
 * @author Ueli Hofstetter
 */
public class GaussKronrodAdaptive extends KronrodIntegral {

    private static final double g7w[] = { 0.417959183673469, 0.381830050505119, 0.279705391489277, 0.129484966168870 };

    private static final double k15w[] = { 0.209482141084728, 0.204432940075298, 0.190350578064785, 0.169004726639267, 0.140653259715525, 0.104790010322250, 0.063092092629979, 0.022935322010529 };

    private static final double k15t[] = { 0.000000000000000, 0.207784955007898, 0.405845151377397, 0.586087235467691, 0.741531185599394, 0.864864423359769, 0.949107912342758, 0.991455371120813 };

    public GaussKronrodAdaptive(final double absoluteAccuracy, final int maxEvaluations) {
        super(absoluteAccuracy, maxEvaluations);
        QL.require(maxEvaluations >= 15, "required maxEvaluations must be >= 15");
    }

    @Override
    protected double integrate(final Ops.DoubleOp f, final double a, final double b) {
        return integrateRecursively(f, a, b, absoluteAccuracy());
    }

    private double integrateRecursively(final Ops.DoubleOp f, final double a, final double b, final double tolerance) {
        final double halflength = (b - a) / 2;
        final double center = (a + b) / 2;
        double g7;
        double k15;
        double t, fsum;
        final double fc = f.op(center);
        g7 = fc * g7w[0];
        k15 = fc * k15w[0];
        int j, j2;
        for (j = 1, j2 = 2; j < 4; j++, j2 += 2) {
            t = halflength * k15t[j2];
            fsum = f.op(center - t) + f.op(center + t);
            g7 += fsum * g7w[j];
            k15 += fsum * k15w[j2];
        }
        for (j2 = 1; j2 < 8; j2 += 2) {
            t = halflength * k15t[j2];
            fsum = f.op(center - t) + f.op(center + t);
            k15 += fsum * k15w[j2];
        }
        g7 = halflength * g7;
        k15 = halflength * k15;
        increaseNumberOfEvaluations(15);
        if (Math.abs(k15 - g7) < tolerance) return k15; else {
            QL.require(numberOfEvaluations() + 30 <= maxEvaluations(), "maximum number of function evaluations exceeded");
            return integrateRecursively(f, a, center, tolerance / 2) + integrateRecursively(f, center, b, tolerance / 2);
        }
    }
}
