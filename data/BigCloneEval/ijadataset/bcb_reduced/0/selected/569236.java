package org.josef.math;

import org.josef.util.CDebug;

/**
 * Finds the roots of a function within a defined interval, using the Bisection
 * method.
 * <br>To find the root of a function f , f should at least be continuous.
 * <br>More information on this topic can be found in the WikiPedia by clicking
 * <a href="http://en.wikipedia.org/wiki/Bisection_method">here</a> or on
 * MathWorld by clicking
 * <a href="http://mathworld.wolfram.com/Bisection.html">here</a>.
 * @author Kees Schotanus
 * @version 1.0 $Revision: 648 $
*/
public final class Bisection extends RootFinder {

    /**
     * Private constructor prevents creation of an instance outside this class.
     */
    private Bisection() {
    }

    /**
     * Computes the root of a function within the supplied interval that is
     * bounded by the supplied left and right values for x.
     * <br>This method calculates the number of iterations that have to be
     * performed to reach the supplied accuracy so this method will never
     * 'hang'. When the desired accuracy has been reached before the calculated
     * number or iterations have been processed, this method will bail out.
     * @param left x value of the left side of the interval.
     *  <br>The y value, that is the value of function(left) should have a sign
     *  that is opposite to that of function(right).
     * @param right x value of the right side of the interval.
     *  <br>The y value, that is the value of function(right) should have a sign
     *  that is opposite to that of function(left).
     * @param function The function for which the root should be computed.
     * @param accuracy The accuracy of the calculated root.
     * @return The root of the supplied method that lies between the supplied
     *  interval.
     *  <br>The returned root is guaranteed to be accurate to the supplied
     *  accuracy.
     * @throws IllegalArgumentException when the accuracy is less than or equal
     *  to zero or when left equals right or when both function(left) and
     *  function(right) are of equal sign or when either function(left) or
     *  function(right) is "Not a Number".
     * @throws NullPointerException when the supplied function is null.
     */
    public static double computeRoot(final double left, final double right, final SingleParameterFunction function, final double accuracy) {
        CDebug.checkParameterNotNull(function, "function");
        CDebug.checkParameterTrue(accuracy > 0, "Accuracy must be positive but is: " + accuracy);
        double a = left < right ? left : right;
        double b = right > left ? right : left;
        checkIntervalForRoot(a, function.f(a), b, function.f(b));
        double c = (a + b) / 2;
        double fc = function.f(c);
        final int iterations = 1 + (int) ((Math.log(b - a) - Math.log(accuracy)) / Math.log(2));
        for (int i = 1; i <= iterations && Math.abs(fc) > accuracy; ++i) {
            if (function.f(a) * fc > 0.0) {
                a = c;
            } else {
                b = c;
            }
            c = (a + b) / 2;
            fc = function.f(c);
        }
        return c;
    }

    /**
     * Computes the root of a function within the supplied interval that is
     * bounded by the supplied left and right values for x.
     * @param left x value of the left side of the interval.
     *  <br>The y value, that is the value of function(left) should have a sign
     *  that is opposite to that of function(right).
     * @param right x value of the right side of the interval.
     *  <br>The y value, that is the value of function(right) should have a sign
     *  that is opposite to that of function(left).
     * @param function The function for which the root should be computed.
     * @param iterations The number of iterations to perform.
     *  <br>When the root has accurately be found this method will bail out
     *  before the supplied number of iterations have been performed.
     * @return The root of the supplied method that lies between the supplied
     *  interval.
     * @throws IllegalArgumentException when the number of iterations is less
     *  than one or when left equals right or when both function(left) and
     *  function(right) are of equal sign or when either function(left) or
     *  function(right) is "Not a Number".
     * @throws NullPointerException when the supplied function is null.
     */
    public static double computeRoot(final double left, final double right, final SingleParameterFunction function, final int iterations) {
        CDebug.checkParameterNotNull(function, "function");
        CDebug.checkParameterTrue(iterations > 0, "The number of iterations must be positive but is: " + iterations);
        double a = left < right ? left : right;
        double b = right > left ? right : left;
        checkIntervalForRoot(a, function.f(a), b, function.f(b));
        double c = (a + b) / 2;
        double fc = function.f(c);
        for (int i = 1; i < iterations && fc != 0.0; ++i) {
            if (function.f(a) * fc > 0.0) {
                a = c;
            } else {
                b = c;
            }
            c = (a + b) / 2;
            fc = function.f(c);
        }
        return c;
    }

    /**
     * For test purposes only.
     * @param args Not used.
     */
    public static void main(final String[] args) {
        final double a = Math.PI * 0.5;
        final double b = Math.PI * -0.75;
        final double accuracy = 0.00000000001D;
        double root = Bisection.computeRoot(a, b, new SingleParameterFunction() {

            public double f(final double x) {
                return Math.sin(x);
            }
        }, accuracy);
        System.out.println("Root:" + root);
    }
}
