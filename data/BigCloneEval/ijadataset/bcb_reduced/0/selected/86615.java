package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.DummyStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;

/**
 * This class implements a Gragg-Bulirsch-Stoer integrator for
 * Ordinary Differential Equations.
 *
 * <p>The Gragg-Bulirsch-Stoer algorithm is one of the most efficient
 * ones currently available for smooth problems. It uses Richardson
 * extrapolation to estimate what would be the solution if the step
 * size could be decreased down to zero.</p>
 *
 * <p>
 * This method changes both the step size and the order during
 * integration, in order to minimize computation cost. It is
 * particularly well suited when a very high precision is needed. The
 * limit where this method becomes more efficient than high-order
 * embedded Runge-Kutta methods like {@link DormandPrince853Integrator
 * Dormand-Prince 8(5,3)} depends on the problem. Results given in the
 * Hairer, Norsett and Wanner book show for example that this limit
 * occurs for accuracy around 1e-6 when integrating Saltzam-Lorenz
 * equations (the authors note this problem is <i>extremely sensitive
 * to the errors in the first integration steps</i>), and around 1e-11
 * for a two dimensional celestial mechanics problems with seven
 * bodies (pleiades problem, involving quasi-collisions for which
 * <i>automatic step size control is essential</i>).
 * </p>
 *
 * <p>
 * This implementation is basically a reimplementation in Java of the
 * <a
 * href="http://www.unige.ch/math/folks/hairer/prog/nonstiff/odex.f">odex</a>
 * fortran code by E. Hairer and G. Wanner. The redistribution policy
 * for this code is available <a
 * href="http://www.unige.ch/~hairer/prog/licence.txt">here</a>, for
 * convenience, it is reproduced below.</p>
 * </p>
 *
 * <table border="0" width="80%" cellpadding="10" align="center" bgcolor="#E0E0E0">
 * <tr><td>Copyright (c) 2004, Ernst Hairer</td></tr>
 *
 * <tr><td>Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 *  <li>Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.</li>
 *  <li>Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.</li>
 * </ul></td></tr>
 *
 * <tr><td><strong>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</strong></td></tr>
 * </table>
 *
 * @author E. Hairer and G. Wanner (fortran version)
 * @version $Revision: 676615 $ $Date: 2008-07-14 17:08:51 +0200 (Mo, 14 Jul 2008) $
 * @since 1.2
 */
public class GraggBulirschStoerIntegrator extends AdaptiveStepsizeIntegrator {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 7364884082146325264L;

    /** Integrator method name. */
    private static final String METHOD_NAME = "Gragg-Bulirsch-Stoer";

    /** Simple constructor.
   * Build a Gragg-Bulirsch-Stoer integrator with the given step
   * bounds. All tuning parameters are set to their default
   * values. The default step handler does nothing.
   * @param minStep minimal step (must be positive even for backward
   * integration), the last step can be smaller than this
   * @param maxStep maximal step (must be positive even for backward
   * integration)
   * @param scalAbsoluteTolerance allowed absolute error
   * @param scalRelativeTolerance allowed relative error
   */
    public GraggBulirschStoerIntegrator(final double minStep, final double maxStep, final double scalAbsoluteTolerance, final double scalRelativeTolerance) {
        super(METHOD_NAME, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
        denseOutput = requiresDenseOutput() || (!eventsHandlersManager.isEmpty());
        setStabilityCheck(true, -1, -1, -1);
        setStepsizeControl(-1, -1, -1, -1);
        setOrderControl(-1, -1, -1);
        setInterpolationControl(true, -1);
    }

    /** Simple constructor.
   * Build a Gragg-Bulirsch-Stoer integrator with the given step
   * bounds. All tuning parameters are set to their default
   * values. The default step handler does nothing.
   * @param minStep minimal step (must be positive even for backward
   * integration), the last step can be smaller than this
   * @param maxStep maximal step (must be positive even for backward
   * integration)
   * @param vecAbsoluteTolerance allowed absolute error
   * @param vecRelativeTolerance allowed relative error
   */
    public GraggBulirschStoerIntegrator(final double minStep, final double maxStep, final double[] vecAbsoluteTolerance, final double[] vecRelativeTolerance) {
        super(METHOD_NAME, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
        denseOutput = requiresDenseOutput() || (!eventsHandlersManager.isEmpty());
        setStabilityCheck(true, -1, -1, -1);
        setStepsizeControl(-1, -1, -1, -1);
        setOrderControl(-1, -1, -1);
        setInterpolationControl(true, -1);
    }

    /** Set the stability check controls.
   * <p>The stability check is performed on the first few iterations of
   * the extrapolation scheme. If this test fails, the step is rejected
   * and the stepsize is reduced.</p>
   * <p>By default, the test is performed, at most during two
   * iterations at each step, and at most once for each of these
   * iterations. The default stepsize reduction factor is 0.5.</p>
   * @param performTest if true, stability check will be performed,
     if false, the check will be skipped
   * @param maxIter maximal number of iterations for which checks are
   * performed (the number of iterations is reset to default if negative
   * or null)
   * @param maxChecks maximal number of checks for each iteration
   * (the number of checks is reset to default if negative or null)
   * @param stabilityReduction stepsize reduction factor in case of
   * failure (the factor is reset to default if lower than 0.0001 or
   * greater than 0.9999)
   */
    public void setStabilityCheck(final boolean performTest, final int maxIter, final int maxChecks, final double stabilityReduction) {
        this.performTest = performTest;
        this.maxIter = (maxIter <= 0) ? 2 : maxIter;
        this.maxChecks = (maxChecks <= 0) ? 1 : maxChecks;
        if ((stabilityReduction < 0.0001) || (stabilityReduction > 0.9999)) {
            this.stabilityReduction = 0.5;
        } else {
            this.stabilityReduction = stabilityReduction;
        }
    }

    /** Set the step size control factors.

   * <p>The new step size hNew is computed from the old one h by:
   * <pre>
   * hNew = h * stepControl2 / (err/stepControl1)^(1/(2k+1))
   * </pre>
   * where err is the scaled error and k the iteration number of the
   * extrapolation scheme (counting from 0). The default values are
   * 0.65 for stepControl1 and 0.94 for stepControl2.</p>
   * <p>The step size is subject to the restriction:
   * <pre>
   * stepControl3^(1/(2k+1))/stepControl4 <= hNew/h <= 1/stepControl3^(1/(2k+1))
   * </pre>
   * The default values are 0.02 for stepControl3 and 4.0 for
   * stepControl4.</p>
   * @param stepControl1 first stepsize control factor (the factor is
   * reset to default if lower than 0.0001 or greater than 0.9999)
   * @param stepControl2 second stepsize control factor (the factor
   * is reset to default if lower than 0.0001 or greater than 0.9999)
   * @param stepControl3 third stepsize control factor (the factor is
   * reset to default if lower than 0.0001 or greater than 0.9999)
   * @param stepControl4 fourth stepsize control factor (the factor
   * is reset to default if lower than 1.0001 or greater than 999.9)
   */
    public void setStepsizeControl(final double stepControl1, final double stepControl2, final double stepControl3, final double stepControl4) {
        if ((stepControl1 < 0.0001) || (stepControl1 > 0.9999)) {
            this.stepControl1 = 0.65;
        } else {
            this.stepControl1 = stepControl1;
        }
        if ((stepControl2 < 0.0001) || (stepControl2 > 0.9999)) {
            this.stepControl2 = 0.94;
        } else {
            this.stepControl2 = stepControl2;
        }
        if ((stepControl3 < 0.0001) || (stepControl3 > 0.9999)) {
            this.stepControl3 = 0.02;
        } else {
            this.stepControl3 = stepControl3;
        }
        if ((stepControl4 < 1.0001) || (stepControl4 > 999.9)) {
            this.stepControl4 = 4.0;
        } else {
            this.stepControl4 = stepControl4;
        }
    }

    /** Set the order control parameters.
   * <p>The Gragg-Bulirsch-Stoer method changes both the step size and
   * the order during integration, in order to minimize computation
   * cost. Each extrapolation step increases the order by 2, so the
   * maximal order that will be used is always even, it is twice the
   * maximal number of columns in the extrapolation table.</p>
   * <pre>
   * order is decreased if w(k-1) <= w(k)   * orderControl1
   * order is increased if w(k)   <= w(k-1) * orderControl2
   * </pre>
   * <p>where w is the table of work per unit step for each order
   * (number of function calls divided by the step length), and k is
   * the current order.</p>
   * <p>The default maximal order after construction is 18 (i.e. the
   * maximal number of columns is 9). The default values are 0.8 for
   * orderControl1 and 0.9 for orderControl2.</p>
   * @param maxOrder maximal order in the extrapolation table (the
   * maximal order is reset to default if order <= 6 or odd)
   * @param orderControl1 first order control factor (the factor is
   * reset to default if lower than 0.0001 or greater than 0.9999)
   * @param orderControl2 second order control factor (the factor
   * is reset to default if lower than 0.0001 or greater than 0.9999)
   */
    public void setOrderControl(final int maxOrder, final double orderControl1, final double orderControl2) {
        if ((maxOrder <= 6) || (maxOrder % 2 != 0)) {
            this.maxOrder = 18;
        }
        if ((orderControl1 < 0.0001) || (orderControl1 > 0.9999)) {
            this.orderControl1 = 0.8;
        } else {
            this.orderControl1 = orderControl1;
        }
        if ((orderControl2 < 0.0001) || (orderControl2 > 0.9999)) {
            this.orderControl2 = 0.9;
        } else {
            this.orderControl2 = orderControl2;
        }
        initializeArrays();
    }

    /** {@inheritDoc} */
    public void addStepHandler(final StepHandler handler) {
        super.addStepHandler(handler);
        denseOutput = requiresDenseOutput() || (!eventsHandlersManager.isEmpty());
        initializeArrays();
    }

    /** {@inheritDoc} */
    public void addEventHandler(final EventHandler function, final double maxCheckInterval, final double convergence, final int maxIterationCount) {
        super.addEventHandler(function, maxCheckInterval, convergence, maxIterationCount);
        denseOutput = requiresDenseOutput() || (!eventsHandlersManager.isEmpty());
        initializeArrays();
    }

    /** Initialize the integrator internal arrays. */
    private void initializeArrays() {
        final int size = maxOrder / 2;
        if ((sequence == null) || (sequence.length != size)) {
            sequence = new int[size];
            costPerStep = new int[size];
            coeff = new double[size][];
            costPerTimeUnit = new double[size];
            optimalStep = new double[size];
        }
        if (denseOutput) {
            for (int k = 0; k < size; ++k) {
                sequence[k] = 4 * k + 2;
            }
        } else {
            for (int k = 0; k < size; ++k) {
                sequence[k] = 2 * (k + 1);
            }
        }
        costPerStep[0] = sequence[0] + 1;
        for (int k = 1; k < size; ++k) {
            costPerStep[k] = costPerStep[k - 1] + sequence[k];
        }
        for (int k = 0; k < size; ++k) {
            coeff[k] = (k > 0) ? new double[k] : null;
            for (int l = 0; l < k; ++l) {
                final double ratio = ((double) sequence[k]) / sequence[k - l - 1];
                coeff[k][l] = 1.0 / (ratio * ratio - 1.0);
            }
        }
    }

    /** Set the interpolation order control parameter.
   * The interpolation order for dense output is 2k - mudif + 1. The
   * default value for mudif is 4 and the interpolation error is used
   * in stepsize control by default.

   * @param useInterpolationError if true, interpolation error is used
   * for stepsize control
   * @param mudif interpolation order control parameter (the parameter
   * is reset to default if <= 0 or >= 7)
   */
    public void setInterpolationControl(final boolean useInterpolationError, final int mudif) {
        this.useInterpolationError = useInterpolationError;
        if ((mudif <= 0) || (mudif >= 7)) {
            this.mudif = 4;
        } else {
            this.mudif = mudif;
        }
    }

    /** Update scaling array.
   * @param y1 first state vector to use for scaling
   * @param y2 second state vector to use for scaling
   * @param scale scaling array to update
   */
    private void rescale(final double[] y1, final double[] y2, final double[] scale) {
        if (vecAbsoluteTolerance == null) {
            for (int i = 0; i < scale.length; ++i) {
                final double yi = Math.max(Math.abs(y1[i]), Math.abs(y2[i]));
                scale[i] = scalAbsoluteTolerance + scalRelativeTolerance * yi;
            }
        } else {
            for (int i = 0; i < scale.length; ++i) {
                final double yi = Math.max(Math.abs(y1[i]), Math.abs(y2[i]));
                scale[i] = vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * yi;
            }
        }
    }

    /** Perform integration over one step using substeps of a modified
   * midpoint method.
   * @param equations differential equations to integrate
   * @param t0 initial time
   * @param y0 initial value of the state vector at t0
   * @param step global step
   * @param k iteration number (from 0 to sequence.length - 1)
   * @param scale scaling array
   * @param f placeholder where to put the state vector derivatives at each substep
   *          (element 0 already contains initial derivative)
   * @param yMiddle placeholder where to put the state vector at the middle of the step
   * @param yEnd placeholder where to put the state vector at the end
   * @param yTmp placeholder for one state vector
   * @return true if computation was done properly,
   *         false if stability check failed before end of computation
   * @throws DerivativeException this exception is propagated to the caller if the
   * underlying user function triggers one
   */
    private boolean tryStep(final FirstOrderDifferentialEquations equations, final double t0, final double[] y0, final double step, final int k, final double[] scale, final double[][] f, final double[] yMiddle, final double[] yEnd, final double[] yTmp) throws DerivativeException {
        final int n = sequence[k];
        final double subStep = step / n;
        final double subStep2 = 2 * subStep;
        double t = t0 + subStep;
        for (int i = 0; i < y0.length; ++i) {
            yTmp[i] = y0[i];
            yEnd[i] = y0[i] + subStep * f[0][i];
        }
        equations.computeDerivatives(t, yEnd, f[1]);
        for (int j = 1; j < n; ++j) {
            if (2 * j == n) {
                System.arraycopy(yEnd, 0, yMiddle, 0, y0.length);
            }
            t += subStep;
            for (int i = 0; i < y0.length; ++i) {
                final double middle = yEnd[i];
                yEnd[i] = yTmp[i] + subStep2 * f[j][i];
                yTmp[i] = middle;
            }
            equations.computeDerivatives(t, yEnd, f[j + 1]);
            if (performTest && (j <= maxChecks) && (k < maxIter)) {
                double initialNorm = 0.0;
                for (int l = 0; l < y0.length; ++l) {
                    final double ratio = f[0][l] / scale[l];
                    initialNorm += ratio * ratio;
                }
                double deltaNorm = 0.0;
                for (int l = 0; l < y0.length; ++l) {
                    final double ratio = (f[j + 1][l] - f[0][l]) / scale[l];
                    deltaNorm += ratio * ratio;
                }
                if (deltaNorm > 4 * Math.max(1.0e-15, initialNorm)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < y0.length; ++i) {
            yEnd[i] = 0.5 * (yTmp[i] + yEnd[i] + subStep * f[n][i]);
        }
        return true;
    }

    /** Extrapolate a vector.
   * @param offset offset to use in the coefficients table
   * @param k index of the last updated point
   * @param diag working diagonal of the Aitken-Neville's
   * triangle, without the last element
   * @param last last element
   */
    private void extrapolate(final int offset, final int k, final double[][] diag, final double[] last) {
        for (int j = 1; j < k; ++j) {
            for (int i = 0; i < last.length; ++i) {
                diag[k - j - 1][i] = diag[k - j][i] + coeff[k + offset][j - 1] * (diag[k - j][i] - diag[k - j - 1][i]);
            }
        }
        for (int i = 0; i < last.length; ++i) {
            last[i] = diag[0][i] + coeff[k + offset][k - 1] * (diag[0][i] - last[i]);
        }
    }

    /** {@inheritDoc} */
    public double integrate(final FirstOrderDifferentialEquations equations, final double t0, final double[] y0, final double t, final double[] y) throws DerivativeException, IntegratorException {
        sanityChecks(equations, t0, y0, t, y);
        final boolean forward = (t > t0);
        final double[] yDot0 = new double[y0.length];
        final double[] y1 = new double[y0.length];
        final double[] yTmp = new double[y0.length];
        final double[] yTmpDot = new double[y0.length];
        final double[][] diagonal = new double[sequence.length - 1][];
        final double[][] y1Diag = new double[sequence.length - 1][];
        for (int k = 0; k < sequence.length - 1; ++k) {
            diagonal[k] = new double[y0.length];
            y1Diag[k] = new double[y0.length];
        }
        final double[][][] fk = new double[sequence.length][][];
        for (int k = 0; k < sequence.length; ++k) {
            fk[k] = new double[sequence[k] + 1][];
            fk[k][0] = yDot0;
            for (int l = 0; l < sequence[k]; ++l) {
                fk[k][l + 1] = new double[y0.length];
            }
        }
        if (y != y0) {
            System.arraycopy(y0, 0, y, 0, y0.length);
        }
        double[] yDot1 = null;
        double[][] yMidDots = null;
        if (denseOutput) {
            yDot1 = new double[y0.length];
            yMidDots = new double[1 + 2 * sequence.length][];
            for (int j = 0; j < yMidDots.length; ++j) {
                yMidDots[j] = new double[y0.length];
            }
        } else {
            yMidDots = new double[1][];
            yMidDots[0] = new double[y0.length];
        }
        final double[] scale = new double[y0.length];
        rescale(y, y, scale);
        final double tol = (vecRelativeTolerance == null) ? scalRelativeTolerance : vecRelativeTolerance[0];
        final double log10R = Math.log(Math.max(1.0e-10, tol)) / Math.log(10.0);
        int targetIter = Math.max(1, Math.min(sequence.length - 2, (int) Math.floor(0.5 - 0.6 * log10R)));
        AbstractStepInterpolator interpolator = null;
        if (denseOutput || (!eventsHandlersManager.isEmpty())) {
            interpolator = new GraggBulirschStoerStepInterpolator(y, yDot0, y1, yDot1, yMidDots, forward);
        } else {
            interpolator = new DummyStepInterpolator(y, forward);
        }
        interpolator.storeTime(t0);
        stepStart = t0;
        double hNew = 0;
        double maxError = Double.MAX_VALUE;
        boolean previousRejected = false;
        boolean firstTime = true;
        boolean newStep = true;
        boolean lastStep = false;
        boolean firstStepAlreadyComputed = false;
        for (StepHandler handler : stepHandlers) {
            handler.reset();
        }
        costPerTimeUnit[0] = 0;
        while (!lastStep) {
            double error;
            boolean reject = false;
            if (newStep) {
                interpolator.shift();
                if (!firstStepAlreadyComputed) {
                    equations.computeDerivatives(stepStart, y, yDot0);
                }
                if (firstTime) {
                    hNew = initializeStep(equations, forward, 2 * targetIter + 1, scale, stepStart, y, yDot0, yTmp, yTmpDot);
                    if (!forward) {
                        hNew = -hNew;
                    }
                }
                newStep = false;
            }
            stepSize = hNew;
            if ((forward && (stepStart + stepSize > t)) || ((!forward) && (stepStart + stepSize < t))) {
                stepSize = t - stepStart;
            }
            final double nextT = stepStart + stepSize;
            lastStep = forward ? (nextT >= t) : (nextT <= t);
            int k = -1;
            for (boolean loop = true; loop; ) {
                ++k;
                if (!tryStep(equations, stepStart, y, stepSize, k, scale, fk[k], (k == 0) ? yMidDots[0] : diagonal[k - 1], (k == 0) ? y1 : y1Diag[k - 1], yTmp)) {
                    hNew = Math.abs(filterStep(stepSize * stabilityReduction, forward, false));
                    reject = true;
                    loop = false;
                } else {
                    if (k > 0) {
                        extrapolate(0, k, y1Diag, y1);
                        rescale(y, y1, scale);
                        error = 0;
                        for (int j = 0; j < y0.length; ++j) {
                            final double e = Math.abs(y1[j] - y1Diag[0][j]) / scale[j];
                            error += e * e;
                        }
                        error = Math.sqrt(error / y0.length);
                        if ((error > 1.0e15) || ((k > 1) && (error > maxError))) {
                            hNew = Math.abs(filterStep(stepSize * stabilityReduction, forward, false));
                            reject = true;
                            loop = false;
                        } else {
                            maxError = Math.max(4 * error, 1.0);
                            final double exp = 1.0 / (2 * k + 1);
                            double fac = stepControl2 / Math.pow(error / stepControl1, exp);
                            final double pow = Math.pow(stepControl3, exp);
                            fac = Math.max(pow / stepControl4, Math.min(1 / pow, fac));
                            optimalStep[k] = Math.abs(filterStep(stepSize * fac, forward, true));
                            costPerTimeUnit[k] = costPerStep[k] / optimalStep[k];
                            switch(k - targetIter) {
                                case -1:
                                    if ((targetIter > 1) && !previousRejected) {
                                        if (error <= 1.0) {
                                            loop = false;
                                        } else {
                                            final double ratio = ((double) sequence[k] * sequence[k + 1]) / (sequence[0] * sequence[0]);
                                            if (error > ratio * ratio) {
                                                reject = true;
                                                loop = false;
                                                targetIter = k;
                                                if ((targetIter > 1) && (costPerTimeUnit[targetIter - 1] < orderControl1 * costPerTimeUnit[targetIter])) {
                                                    --targetIter;
                                                }
                                                hNew = optimalStep[targetIter];
                                            }
                                        }
                                    }
                                    break;
                                case 0:
                                    if (error <= 1.0) {
                                        loop = false;
                                    } else {
                                        final double ratio = ((double) sequence[k + 1]) / sequence[0];
                                        if (error > ratio * ratio) {
                                            reject = true;
                                            loop = false;
                                            if ((targetIter > 1) && (costPerTimeUnit[targetIter - 1] < orderControl1 * costPerTimeUnit[targetIter])) {
                                                --targetIter;
                                            }
                                            hNew = optimalStep[targetIter];
                                        }
                                    }
                                    break;
                                case 1:
                                    if (error > 1.0) {
                                        reject = true;
                                        if ((targetIter > 1) && (costPerTimeUnit[targetIter - 1] < orderControl1 * costPerTimeUnit[targetIter])) {
                                            --targetIter;
                                        }
                                        hNew = optimalStep[targetIter];
                                    }
                                    loop = false;
                                    break;
                                default:
                                    if ((firstTime || lastStep) && (error <= 1.0)) {
                                        loop = false;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            double hInt = getMaxStep();
            if (denseOutput && !reject) {
                for (int j = 1; j <= k; ++j) {
                    extrapolate(0, j, diagonal, yMidDots[0]);
                }
                equations.computeDerivatives(stepStart + stepSize, y1, yDot1);
                final int mu = 2 * k - mudif + 3;
                for (int l = 0; l < mu; ++l) {
                    final int l2 = l / 2;
                    double factor = Math.pow(0.5 * sequence[l2], l);
                    int middleIndex = fk[l2].length / 2;
                    for (int i = 0; i < y0.length; ++i) {
                        yMidDots[l + 1][i] = factor * fk[l2][middleIndex + l][i];
                    }
                    for (int j = 1; j <= k - l2; ++j) {
                        factor = Math.pow(0.5 * sequence[j + l2], l);
                        middleIndex = fk[l2 + j].length / 2;
                        for (int i = 0; i < y0.length; ++i) {
                            diagonal[j - 1][i] = factor * fk[l2 + j][middleIndex + l][i];
                        }
                        extrapolate(l2, j, diagonal, yMidDots[l + 1]);
                    }
                    for (int i = 0; i < y0.length; ++i) {
                        yMidDots[l + 1][i] *= stepSize;
                    }
                    for (int j = (l + 1) / 2; j <= k; ++j) {
                        for (int m = fk[j].length - 1; m >= 2 * (l + 1); --m) {
                            for (int i = 0; i < y0.length; ++i) {
                                fk[j][m][i] -= fk[j][m - 2][i];
                            }
                        }
                    }
                }
                if (mu >= 0) {
                    final GraggBulirschStoerStepInterpolator gbsInterpolator = (GraggBulirschStoerStepInterpolator) interpolator;
                    gbsInterpolator.computeCoefficients(mu, stepSize);
                    if (useInterpolationError) {
                        final double interpError = gbsInterpolator.estimateError(scale);
                        hInt = Math.abs(stepSize / Math.max(Math.pow(interpError, 1.0 / (mu + 4)), 0.01));
                        if (interpError > 10.0) {
                            hNew = hInt;
                            reject = true;
                        }
                    }
                    if (!reject) {
                        interpolator.storeTime(stepStart + stepSize);
                        if (eventsHandlersManager.evaluateStep(interpolator)) {
                            reject = true;
                            hNew = Math.abs(eventsHandlersManager.getEventTime() - stepStart);
                        }
                    }
                }
                if (!reject) {
                    firstStepAlreadyComputed = true;
                    System.arraycopy(yDot1, 0, yDot0, 0, y0.length);
                }
            }
            if (!reject) {
                final double nextStep = stepStart + stepSize;
                System.arraycopy(y1, 0, y, 0, y0.length);
                eventsHandlersManager.stepAccepted(nextStep, y);
                if (eventsHandlersManager.stop()) {
                    lastStep = true;
                }
                interpolator.storeTime(nextStep);
                for (StepHandler handler : stepHandlers) {
                    handler.handleStep(interpolator, lastStep);
                }
                stepStart = nextStep;
                if (eventsHandlersManager.reset(stepStart, y) && !lastStep) {
                    firstStepAlreadyComputed = false;
                }
                int optimalIter;
                if (k == 1) {
                    optimalIter = 2;
                    if (previousRejected) {
                        optimalIter = 1;
                    }
                } else if (k <= targetIter) {
                    optimalIter = k;
                    if (costPerTimeUnit[k - 1] < orderControl1 * costPerTimeUnit[k]) {
                        optimalIter = k - 1;
                    } else if (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[k - 1]) {
                        optimalIter = Math.min(k + 1, sequence.length - 2);
                    }
                } else {
                    optimalIter = k - 1;
                    if ((k > 2) && (costPerTimeUnit[k - 2] < orderControl1 * costPerTimeUnit[k - 1])) {
                        optimalIter = k - 2;
                    }
                    if (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[optimalIter]) {
                        optimalIter = Math.min(k, sequence.length - 2);
                    }
                }
                if (previousRejected) {
                    targetIter = Math.min(optimalIter, k);
                    hNew = Math.min(Math.abs(stepSize), optimalStep[targetIter]);
                } else {
                    if (optimalIter <= k) {
                        hNew = optimalStep[optimalIter];
                    } else {
                        if ((k < targetIter) && (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[k - 1])) {
                            hNew = filterStep(optimalStep[k] * costPerStep[optimalIter + 1] / costPerStep[k], forward, false);
                        } else {
                            hNew = filterStep(optimalStep[k] * costPerStep[optimalIter] / costPerStep[k], forward, false);
                        }
                    }
                    targetIter = optimalIter;
                }
                newStep = true;
            }
            hNew = Math.min(hNew, hInt);
            if (!forward) {
                hNew = -hNew;
            }
            firstTime = false;
            if (reject) {
                lastStep = false;
                previousRejected = true;
            } else {
                previousRejected = false;
            }
        }
        return stepStart;
    }

    /** maximal order. */
    private int maxOrder;

    /** step size sequence. */
    private int[] sequence;

    /** overall cost of applying step reduction up to iteration k+1,
   *  in number of calls.
   */
    private int[] costPerStep;

    /** cost per unit step. */
    private double[] costPerTimeUnit;

    /** optimal steps for each order. */
    private double[] optimalStep;

    /** extrapolation coefficients. */
    private double[][] coeff;

    /** stability check enabling parameter. */
    private boolean performTest;

    /** maximal number of checks for each iteration. */
    private int maxChecks;

    /** maximal number of iterations for which checks are performed. */
    private int maxIter;

    /** stepsize reduction factor in case of stability check failure. */
    private double stabilityReduction;

    /** first stepsize control factor. */
    private double stepControl1;

    /** second stepsize control factor. */
    private double stepControl2;

    /** third stepsize control factor. */
    private double stepControl3;

    /** fourth stepsize control factor. */
    private double stepControl4;

    /** first order control factor. */
    private double orderControl1;

    /** second order control factor. */
    private double orderControl2;

    /** dense outpute required. */
    private boolean denseOutput;

    /** use interpolation error in stepsize control. */
    private boolean useInterpolationError;

    /** interpolation order control parameter. */
    private int mudif;
}
