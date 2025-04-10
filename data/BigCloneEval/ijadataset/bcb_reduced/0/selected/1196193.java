package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;

/**
 * Base class for integer-valued discrete distributions.  Default
 * implementations are provided for some of the methods that do not vary
 * from distribution to distribution.
 *  
 * @version $Revision: 670469 $ $Date: 2008-06-23 10:01:38 +0200 (Mo, 23 Jun 2008) $
 */
public abstract class AbstractIntegerDistribution extends AbstractDistribution implements IntegerDistribution, Serializable {

    /** Serializable version identifier */
    private static final long serialVersionUID = -1146319659338487221L;

    /**
     * Default constructor.
     */
    protected AbstractIntegerDistribution() {
        super();
    }

    /**
     * For a random variable X whose values are distributed according
     * to this distribution, this method returns P(X &le; x).  In other words,
     * this method represents the  (cumulative) distribution function, or
     * CDF, for this distribution.
     * <p>
     * If <code>x</code> does not represent an integer value, the CDF is 
     * evaluated at the greatest integer less than x.
     * 
     * @param x the value at which the distribution function is evaluated.
     * @return cumulative probability that a random variable with this
     * distribution takes a value less than or equal to <code>x</code>
     * @throws MathException if the cumulative probability can not be
     * computed due to convergence or other numerical errors.
     */
    public double cumulativeProbability(double x) throws MathException {
        return cumulativeProbability((int) Math.floor(x));
    }

    /**
     * For a random variable X whose values are distributed according
     * to this distribution, this method returns P(x0 &le; X &le; x1).
     * 
     * @param x0 the (inclusive) lower bound
     * @param x1 the (inclusive) upper bound
     * @return the probability that a random variable with this distribution
     * will take a value between <code>x0</code> and <code>x1</code>,
     * including the endpoints.
     * @throws MathException if the cumulative probability can not be
     * computed due to convergence or other numerical errors.
     * @throws IllegalArgumentException if <code>x0 > x1</code>
     */
    public double cumulativeProbability(double x0, double x1) throws MathException {
        if (x0 > x1) {
            throw new IllegalArgumentException("lower endpoint must be less than or equal to upper endpoint");
        }
        if (Math.floor(x0) < x0) {
            return cumulativeProbability(((int) Math.floor(x0)) + 1, (int) Math.floor(x1));
        } else {
            return cumulativeProbability((int) Math.floor(x0), (int) Math.floor(x1));
        }
    }

    /**
     * For a random variable X whose values are distributed according
     * to this distribution, this method returns P(X &le; x).  In other words,
     * this method represents the probability distribution function, or PDF,
     * for this distribution.
     * 
     * @param x the value at which the PDF is evaluated.
     * @return PDF for this distribution. 
     * @throws MathException if the cumulative probability can not be
     *            computed due to convergence or other numerical errors.
     */
    public abstract double cumulativeProbability(int x) throws MathException;

    /**
     * For a random variable X whose values are distributed according
     * to this distribution, this method returns P(X = x). In other words, this
     * method represents the probability mass function,  or PMF, for the distribution.
     * <p>
     * If <code>x</code> does not represent an integer value, 0 is returned.
     * 
     * @param x the value at which the probability density function is evaluated
     * @return the value of the probability density function at x
     */
    public double probability(double x) {
        double fl = Math.floor(x);
        if (fl == x) {
            return this.probability((int) x);
        } else {
            return 0;
        }
    }

    /**
    * For a random variable X whose values are distributed according
     * to this distribution, this method returns P(x0 &le; X &le; x1).
     * 
     * @param x0 the inclusive, lower bound
     * @param x1 the inclusive, upper bound
     * @return the cumulative probability. 
     * @throws MathException if the cumulative probability can not be
     *            computed due to convergence or other numerical errors.
     * @throws IllegalArgumentException if x0 > x1
     */
    public double cumulativeProbability(int x0, int x1) throws MathException {
        if (x0 > x1) {
            throw new IllegalArgumentException("lower endpoint must be less than or equal to upper endpoint");
        }
        return cumulativeProbability(x1) - cumulativeProbability(x0 - 1);
    }

    /**
     * For a random variable X whose values are distributed according
     * to this distribution, this method returns the largest x, such
     * that P(X &le; x) &le; <code>p</code>.
     *
     * @param p the desired probability
     * @return the largest x such that P(X &le; x) <= p
     * @throws MathException if the inverse cumulative probability can not be
     *            computed due to convergence or other numerical errors.
     * @throws IllegalArgumentException if p < 0 or p > 1
     */
    public int inverseCumulativeProbability(final double p) throws MathException {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("p must be between 0 and 1.0 (inclusive)");
        }
        int x0 = getDomainLowerBound(p);
        int x1 = getDomainUpperBound(p);
        double pm;
        while (x0 < x1) {
            int xm = x0 + (x1 - x0) / 2;
            pm = cumulativeProbability(xm);
            if (pm > p) {
                if (xm == x1) {
                    --x1;
                } else {
                    x1 = xm;
                }
            } else {
                if (xm == x0) {
                    ++x0;
                } else {
                    x0 = xm;
                }
            }
        }
        pm = cumulativeProbability(x0);
        while (pm > p) {
            --x0;
            pm = cumulativeProbability(x0);
        }
        return x0;
    }

    /**
     * Access the domain value lower bound, based on <code>p</code>, used to
     * bracket a PDF root.  This method is used by
     * {@link #inverseCumulativeProbability(double)} to find critical values.
     * 
     * @param p the desired probability for the critical value
     * @return domain value lower bound, i.e.
     *         P(X &lt; <i>lower bound</i>) &lt; <code>p</code> 
     */
    protected abstract int getDomainLowerBound(double p);

    /**
     * Access the domain value upper bound, based on <code>p</code>, used to
     * bracket a PDF root.  This method is used by
     * {@link #inverseCumulativeProbability(double)} to find critical values.
     * 
     * @param p the desired probability for the critical value
     * @return domain value upper bound, i.e.
     *         P(X &lt; <i>upper bound</i>) &gt; <code>p</code> 
     */
    protected abstract int getDomainUpperBound(double p);
}
