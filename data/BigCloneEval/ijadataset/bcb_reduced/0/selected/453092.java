package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

/**
 * Implementation of Student's t-distribution.
 *
 * @see "<a href='http://en.wikipedia.org/wiki/Student&apos;s_t-distribution'>Student's t-distribution (Wikipedia)</a>"
 * @see "<a href='http://mathworld.wolfram.com/Studentst-Distribution.html'>Student's t-distribution (MathWorld)</a>"
 * @version $Id: TDistribution.java 1244107 2012-02-14 16:17:55Z erans $
 */
public class TDistribution extends AbstractRealDistribution {

    /**
     * Default inverse cumulative probability accuracy.
     * @since 2.1
     */
    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;

    /** Serializable version identifier */
    private static final long serialVersionUID = -5852615386664158222L;

    /** The degrees of freedom. */
    private final double degreesOfFreedom;

    /** Inverse cumulative probability accuracy. */
    private final double solverAbsoluteAccuracy;

    /**
     * Create a t distribution using the given degrees of freedom and the
     * specified inverse cumulative probability absolute accuracy.
     *
     * @param degreesOfFreedom Degrees of freedom.
     * @param inverseCumAccuracy the maximum absolute error in inverse
     * cumulative probability estimates
     * (defaults to {@link #DEFAULT_INVERSE_ABSOLUTE_ACCURACY}).
     * @throws NotStrictlyPositiveException if {@code degreesOfFreedom <= 0}
     * @since 2.1
     */
    public TDistribution(double degreesOfFreedom, double inverseCumAccuracy) throws NotStrictlyPositiveException {
        if (degreesOfFreedom <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.DEGREES_OF_FREEDOM, degreesOfFreedom);
        }
        this.degreesOfFreedom = degreesOfFreedom;
        solverAbsoluteAccuracy = inverseCumAccuracy;
    }

    /**
     * Create a t distribution using the given degrees of freedom.
     *
     * @param degreesOfFreedom Degrees of freedom.
     * @throws NotStrictlyPositiveException if {@code degreesOfFreedom <= 0}
     */
    public TDistribution(double degreesOfFreedom) throws NotStrictlyPositiveException {
        this(degreesOfFreedom, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    /**
     * Access the degrees of freedom.
     *
     * @return the degrees of freedom.
     */
    public double getDegreesOfFreedom() {
        return degreesOfFreedom;
    }

    /**
     * {@inheritDoc}
     *
     * For this distribution {@code P(X = x)} always evaluates to 0.
     *
     * @return 0
     */
    public double probability(double x) {
        return 0.0;
    }

    /** {@inheritDoc} */
    public double density(double x) {
        final double n = degreesOfFreedom;
        final double nPlus1Over2 = (n + 1) / 2;
        return FastMath.exp(Gamma.logGamma(nPlus1Over2) - 0.5 * (FastMath.log(FastMath.PI) + FastMath.log(n)) - Gamma.logGamma(n / 2) - nPlus1Over2 * FastMath.log(1 + x * x / n));
    }

    /** {@inheritDoc} */
    public double cumulativeProbability(double x) {
        double ret;
        if (x == 0) {
            ret = 0.5;
        } else {
            double t = Beta.regularizedBeta(degreesOfFreedom / (degreesOfFreedom + (x * x)), 0.5 * degreesOfFreedom, 0.5);
            if (x < 0.0) {
                ret = 0.5 * t;
            } else {
                ret = 1.0 - 0.5 * t;
            }
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }

    /**
     * {@inheritDoc}
     *
     * For degrees of freedom parameter {@code df}, the mean is
     * <ul>
     *  <li>if {@code df > 1} then {@code 0},</li>
     * <li>else undefined ({@code Double.NaN}).</li>
     * </ul>
     */
    public double getNumericalMean() {
        final double df = getDegreesOfFreedom();
        if (df > 1) {
            return 0;
        }
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     *
     * For degrees of freedom parameter {@code df}, the variance is
     * <ul>
     *  <li>if {@code df > 2} then {@code df / (df - 2)},</li>
     *  <li>if {@code 1 < df <= 2} then positive infinity
     *  ({@code Double.POSITIVE_INFINITY}),</li>
     *  <li>else undefined ({@code Double.NaN}).</li>
     * </ul>
     */
    public double getNumericalVariance() {
        final double df = getDegreesOfFreedom();
        if (df > 2) {
            return df / (df - 2);
        }
        if (df > 1 && df <= 2) {
            return Double.POSITIVE_INFINITY;
        }
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always negative infinity no matter the
     * parameters.
     *
     * @return lower bound of the support (always
     * {@code Double.NEGATIVE_INFINITY})
     */
    public double getSupportLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity no matter the
     * parameters.
     *
     * @return upper bound of the support (always
     * {@code Double.POSITIVE_INFINITY})
     */
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    public boolean isSupportLowerBoundInclusive() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isSupportUpperBoundInclusive() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    public boolean isSupportConnected() {
        return true;
    }
}
