package de.telekom.laboratories.multitouch.util;

import java.util.Arrays;

/**
 * A class which can compute all (derived) moments of a two-dimensional shape up to a specified degree upon the following formulas:<br/>
 * <br/>
 * <b>Standard:</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <code>M<sub>m,n</sub> = &sum; &sum; x<sup>m</sup> * y<sup>n</sup> * I(x,y)</code>
 * <br/><br/>
 * <b>Relative to P(x,y):</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <code>U<sub>m,n</sub> = &sum; &sum; (x-x<sub>p</sub>)<sup>m</sup> * (y-y<sub>p</sub>)<sup>n</sup> * I(x,y)</code>
 * <br/><br/>
 * <b>Normalized:</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <code>N<sub>m,n</sub> = (M<sub>m,n</sub> or U<sub>m,n</sub>) / (&sum; &sum; I(x,y))<sup>(m+n+2)/2</sup></code>
 * <br/><br/>
 * Note: Besides <i>scaling invariance</i> through normalization, <i>translation invariance</i> can be acchieved by calculating the moments relative to the <code>Pivot( M<sub>1,0</sub> / M<sub>0,0</sub>, M<sub>,1</sub> / M<sub>0,0</sub> )</code>.
 * @author Michael Nischt
 * @version 0.1
 */
public class Moments2D implements Cloneable {

    private final double[][] moments;

    private final double[] tmp;

    /**
     * Creates a new instance which only consistis of the 0th-moment.
     */
    public Moments2D() {
        this(0);
    }

    /**
     * Creates a new instance for all moments up to the specified degree.
     * @param degree the maximal degree of the moments
     */
    public Moments2D(int degree) {
        degree++;
        moments = new double[degree][];
        for (int i = 0; i < moments.length; ) {
            moments[i] = new double[++i];
        }
        tmp = new double[degree];
    }

    private Moments2D(Moments2D other) {
        moments = other.moments.clone();
        for (int i = 0; i < moments.length; i++) {
            moments[i] = other.moments[i].clone();
        }
        tmp = new double[moments.length];
    }

    /**
     * Returns the highest degree of all the moments.
     * @return the highest degree of all the moments.
     */
    public int getDegrees() {
        return moments.length - 1;
    }

    /**
     * Returns the moment with the specified derivation degrees for the two directions (x and y).<br/>
     * Note that: <code>total-degree(x,y) == x + y</code>
     * @param x the derivation degree for the x-direction
     * @param y the derivation degree for the y-direction
     * @return the moment with the specified derivation degrees for the two directions (x and y).
     * @throws java.lang.IllegalArgumentException if <code>x</code> or <code>y</code> is smaller than <code>0</code>
     * or <code>x + y</code> is greater than the maximal {@link Moments2D#getDegrees degree}.
     */
    public double getMoment(int x, int y) throws IllegalArgumentException {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException();
        }
        final int degree = x + y;
        if (degree > getDegrees()) {
            throw new IllegalArgumentException();
        }
        return moments[degree][y];
    }

    public void clear() {
        final double zero = 0.0;
        for (double[] moment : moments) {
            Arrays.fill(moment, zero);
        }
    }

    public void point(double x, double y) {
        point(x, y, 1.0f);
    }

    public void point(double x, double y, double intensity) {
        for (int j = 1; j < tmp.length; j++) {
            tmp[j] = 0.0;
        }
        moments[0][0] += (tmp[0] = intensity);
        for (int j = 1; j < moments.length; j++) {
            final double[] ithMoments = moments[j];
            for (int i = ithMoments.length - 1; i > 0; i--) {
                ithMoments[i] += tmp[i] = (tmp[i - 1] * y);
            }
            ithMoments[0] += (tmp[0] *= x);
        }
    }

    public void normalize() {
        final double sum = moments[0][0];
        for (int degree = 0; degree < moments.length; degree++) {
            final double pow = (degree + 2) / 2;
            final double factor = 1.0 / Math.pow(sum, pow);
            final double[] ithMoments = moments[degree];
            for (int i = 0; i < ithMoments.length; i++) {
                ithMoments[i] *= factor;
            }
        }
    }

    @Override
    public Moments2D clone() {
        return new Moments2D(this);
    }
}
