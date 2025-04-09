package org.openscience.cdk.modeling.forcefield;

import javax.vecmath.GVector;

/**
 * 
 *
 * @author     vlabarta
 *@cdk.module     forcefield
 * @cdk.svnrev  $Revision: 12604 $
 * 
 */
public class LineSearchForTheWolfeConditions {

    private GVector x = null;

    private double linearFunctionInAlpha0;

    private GVector dfx = null;

    private GVector direction = null;

    private double linearFunctionDerivativeInAlpha0;

    private IPotentialFunction pf = null;

    private double alphaInitialStep;

    private double[] alpha = new double[3];

    private double[] linearFunctionInAlpha = new double[3];

    private double[] linearFunctionDerivativeInAlpha = new double[3];

    private GVector[] dfInAlpha = new GVector[3];

    private double[] brentStep = new double[3];

    private final double c1 = 0.0001;

    private double c2;

    private double linearFunctionAlphaInterpolation;

    public boolean derivativeSmallEnough = true;

    public double alphaOptimum;

    public double linearFunctionInAlphaOptimum;

    public GVector dfOptimum = null;

    private double alphaj;

    private double linearFunctionInAlphaj;

    private double linearFunctionDerivativeInAlphaj;

    private GVector dfInAlphaj;

    private int functionEvaluationNumber;

    private GVector xAlpha = null;

    private double a;

    private double b;

    private double alphaTemporal;

    private double linearFunctionInAlphaTemporal;

    private double linearFunctionDerivativeInAlphaTemporal;

    private double d1;

    private double d2;

    private double alphaiplus1;

    public LineSearchForTheWolfeConditions(IPotentialFunction pfUser, String method) {
        this.pf = pfUser;
        if ((method == "sdm") | (method == "cgm")) {
            c2 = 0.07;
        } else {
            c2 = 0.9;
        }
    }

    public void initialize(GVector xUser, double fxUser, GVector dfxUser, GVector directionUser, double linearFunctionDerivativeUser, double alphaInitialStepUser) {
        this.x = xUser;
        this.linearFunctionInAlpha0 = fxUser;
        this.dfx = dfxUser;
        this.direction = directionUser;
        this.linearFunctionDerivativeInAlpha0 = linearFunctionDerivativeUser;
        this.alphaOptimum = 0;
        this.linearFunctionInAlphaOptimum = linearFunctionInAlpha0;
        dfOptimum = this.dfx;
        this.alphaInitialStep = alphaInitialStepUser;
        this.derivativeSmallEnough = false;
        this.xAlpha = new GVector(x.getSize());
    }

    public void lineSearchAlgorithm(double alphaMax) {
        alpha[0] = 0.0;
        linearFunctionInAlpha[0] = linearFunctionInAlpha0;
        linearFunctionDerivativeInAlpha[0] = linearFunctionDerivativeInAlpha0;
        dfInAlpha[0] = this.dfx;
        alpha[1] = this.alphaInitialStep;
        brentStep[0] = alpha[0];
        brentStep[1] = alpha[1];
        int i = 1;
        this.functionEvaluationNumber = 0;
        if (alpha[1] > alphaMax) {
            alpha[1] = alphaMax;
        }
        try {
            do {
                if (alpha[1] == 0) {
                    System.out.println("alpha[1] == 0");
                    break;
                }
                linearFunctionInAlpha[i] = evaluateEnergyFunction(alpha[i]);
                if ((linearFunctionInAlpha[i] > linearFunctionInAlpha[0] + c1 * alpha[i] * linearFunctionDerivativeInAlpha[0]) | ((linearFunctionInAlpha[i] >= linearFunctionInAlpha[i - 1]) & (i > 1))) {
                    zoom(alpha[i - 1], linearFunctionInAlpha[i - 1], linearFunctionDerivativeInAlpha[i - 1], dfInAlpha[i - 1], alpha[i], linearFunctionInAlpha[i]);
                    break;
                }
                dfInAlpha[i] = evaluateEnergyFunctionDerivative(alpha[i]);
                linearFunctionDerivativeInAlpha[i] = dfInAlpha[i].dot(direction);
                if (Math.abs(linearFunctionDerivativeInAlpha[i]) <= -c2 * linearFunctionDerivativeInAlpha[0]) {
                    alphaOptimum = alpha[i];
                    linearFunctionInAlphaOptimum = linearFunctionInAlpha[i];
                    dfOptimum = dfInAlpha[i];
                    this.derivativeSmallEnough = true;
                    break;
                }
                if (linearFunctionDerivativeInAlpha[i] >= 0) {
                    zoom(alpha[i - 1], linearFunctionInAlpha[i - 1], linearFunctionDerivativeInAlpha[i - 1], dfInAlpha[i - 1], alpha[i], linearFunctionInAlpha[i]);
                    break;
                }
                if (alpha[i] == alphaMax) {
                    alphaOptimum = alpha[i];
                    linearFunctionInAlphaOptimum = linearFunctionInAlpha[i];
                    dfOptimum = dfInAlpha[i];
                    break;
                }
                functionEvaluationNumber = functionEvaluationNumber + 1;
                if (functionEvaluationNumber == 10) {
                    alphaOptimum = alpha[i];
                    linearFunctionInAlphaOptimum = linearFunctionInAlpha[i];
                    dfOptimum = dfInAlpha[i];
                    break;
                }
                if (i > 1) {
                    brentStep[0] = brentStep[1];
                    brentStep[1] = brentStep[2];
                    alpha[1] = alpha[2];
                    linearFunctionInAlpha[1] = linearFunctionInAlpha[2];
                    linearFunctionDerivativeInAlpha[1] = linearFunctionDerivativeInAlpha[2];
                    dfInAlpha[1] = dfInAlpha[2];
                }
                brentStep[2] = brentStep[1] + 1.618 * (brentStep[1] - brentStep[0]);
                if (brentStep[2] > alphaMax) {
                    brentStep[2] = alphaMax;
                }
                alpha[2] = brentStep[2];
                i = 2;
            } while ((alpha[2] <= alphaMax) & (alpha[1] < alpha[2]) & (functionEvaluationNumber < 10));
        } catch (Exception exception) {
            System.out.println("Line search for the strong wolfe conditions: " + exception.getMessage());
            System.out.println(exception);
        }
    }

    private void zoom(double alphaLow, double linearFunctionInAlphaLow, double linearFunctionDerivativeInAlphaLow, GVector dfInAlphaLow, double alphaHigh, double linearFunctionInAlphaHigh) {
        functionEvaluationNumber = 0;
        do {
            alphaj = this.interpolation(alphaLow, linearFunctionInAlphaLow, linearFunctionDerivativeInAlphaLow, alphaHigh, linearFunctionInAlphaHigh);
            linearFunctionInAlphaj = this.linearFunctionAlphaInterpolation;
            if ((linearFunctionInAlphaj > linearFunctionInAlpha0 + c1 * alphaj * linearFunctionDerivativeInAlpha0) | (linearFunctionInAlphaj >= linearFunctionInAlphaLow)) {
                alphaHigh = alphaj;
                linearFunctionInAlphaHigh = linearFunctionInAlphaj;
            } else {
                dfInAlphaj = evaluateEnergyFunctionDerivative(alphaj);
                linearFunctionDerivativeInAlphaj = dfInAlphaj.dot(direction);
                if (Math.abs(linearFunctionDerivativeInAlphaj) <= -c2 * linearFunctionDerivativeInAlpha0) {
                    this.derivativeSmallEnough = true;
                    alphaOptimum = alphaj;
                    linearFunctionInAlphaOptimum = linearFunctionInAlphaj;
                    dfOptimum = dfInAlphaj;
                    break;
                }
                if (linearFunctionDerivativeInAlphaj * (alphaHigh - alphaLow) >= 0) {
                    alphaHigh = alphaLow;
                    linearFunctionInAlphaHigh = linearFunctionInAlphaLow;
                }
                alphaLow = alphaj;
                linearFunctionInAlphaLow = linearFunctionInAlphaj;
                linearFunctionDerivativeInAlphaLow = linearFunctionDerivativeInAlphaj;
                dfInAlphaLow = dfInAlphaj;
            }
            functionEvaluationNumber = functionEvaluationNumber + 1;
            if ((functionEvaluationNumber == 10) | (Math.abs(linearFunctionInAlphaHigh - linearFunctionInAlphaLow) <= 0.000001) | (Math.abs(alphaLow - alphaHigh) <= 0.000000000001)) {
                this.alphaOptimum = alphaLow;
                this.linearFunctionInAlphaOptimum = linearFunctionInAlphaLow;
                this.dfOptimum = dfInAlphaLow;
                break;
            }
        } while ((Math.abs(linearFunctionInAlphaHigh - linearFunctionInAlphaLow) > 0.000001) & (functionEvaluationNumber < 10) & (Math.abs(alphaLow - alphaHigh) > 0.000000000001));
        return;
    }

    public double cubicInterpolation(double alphai, double linearFunctionInAlphai, double linearFunctionDerivativeInAlphai, double alphaiMinus1, double linearFunctionInAlphaiMinus1, double linearFunctionDerivativeInAlphaiMinus1, double a, double b) {
        if (alphai < alphaiMinus1) {
            this.alphaTemporal = alphai;
            this.linearFunctionInAlphaTemporal = linearFunctionInAlphai;
            this.linearFunctionDerivativeInAlphaTemporal = linearFunctionDerivativeInAlphai;
            alphai = alphaiMinus1;
            linearFunctionInAlphai = linearFunctionInAlphaiMinus1;
            linearFunctionDerivativeInAlphai = linearFunctionDerivativeInAlphaiMinus1;
            alphaiMinus1 = this.alphaTemporal;
            linearFunctionInAlphaiMinus1 = this.linearFunctionInAlphaTemporal;
            linearFunctionDerivativeInAlphaiMinus1 = this.linearFunctionDerivativeInAlphaTemporal;
        }
        this.d1 = linearFunctionDerivativeInAlphaiMinus1 + linearFunctionDerivativeInAlphai - 3 * ((linearFunctionInAlphaiMinus1 - linearFunctionInAlphai) / (alphaiMinus1 - alphai));
        this.d2 = Math.sqrt(Math.abs(Math.pow(d1, 2) - linearFunctionDerivativeInAlphaiMinus1 * linearFunctionDerivativeInAlphai));
        this.alphaiplus1 = alphai - (alphai - alphaiMinus1) * ((linearFunctionDerivativeInAlphai + d2 - d1) / (linearFunctionDerivativeInAlphai - linearFunctionDerivativeInAlphaiMinus1 + 2 * d2));
        if (alphaiplus1 < a) {
            alphaiplus1 = a;
        }
        if (alphaiplus1 > b) {
            alphaiplus1 = b;
        }
        if (Math.abs(alphaiplus1 - alphai) < 0.000000001) {
            alphaiplus1 = (alphaiMinus1 + alphai) / 2;
        } else {
            if (alphaiplus1 < (alphai - 9 * (alphai - alphaiMinus1) / 10)) {
                alphaiplus1 = (alphaiMinus1 + alphai) / 2;
                ;
            }
        }
        return alphaiplus1;
    }

    private double interpolation(double alphaLow, double linearFunctionInAlphaLow, double linearFunctionDerivativeInAlphaLow, double alphaHigh, double linearFunctionInAlphaHigh) {
        double minAlpha = Math.min(alphaLow, alphaHigh);
        double alphaDiff = Math.abs(alphaHigh - alphaLow);
        double alphaInterpolation;
        double alpha1 = -1 * ((linearFunctionDerivativeInAlphaLow * Math.pow(alphaDiff, 2)) / (2 * (linearFunctionInAlphaHigh - linearFunctionInAlphaLow - linearFunctionDerivativeInAlphaLow * alphaDiff)));
        if ((alpha1 > alphaDiff) | (Math.abs(alpha1 - alphaDiff) < 0.000000001)) {
            if (alpha1 < 1E-7) {
            } else {
                alpha1 = alphaDiff / 2;
            }
        } else {
            if ((alpha1 < 0) & (alpha1 < (alphaDiff - 9 * alphaDiff / 10))) {
                if (alpha1 < 1E-7) {
                } else {
                    alpha1 = alphaDiff / 2;
                }
            }
        }
        alphaInterpolation = minAlpha + alpha1;
        this.linearFunctionAlphaInterpolation = this.evaluateEnergyFunction(alphaInterpolation);
        if (this.linearFunctionAlphaInterpolation <= this.linearFunctionInAlpha0 + this.c1 * (alphaInterpolation) * this.linearFunctionDerivativeInAlpha0) {
        } else {
            double alphai;
            a = 1 / (Math.pow(alphaDiff, 2) * Math.pow(alpha1, 2) * (alpha1 - alphaDiff));
            b = a;
            a = a * (Math.pow(alphaDiff, 2) * (this.linearFunctionAlphaInterpolation - linearFunctionInAlphaLow - linearFunctionDerivativeInAlphaLow * alpha1) + (-Math.pow(alpha1, 2)) * (linearFunctionInAlphaHigh - linearFunctionInAlphaLow - linearFunctionDerivativeInAlphaLow * alphaDiff));
            b = b * (-Math.pow(alphaDiff, 3) * (this.linearFunctionAlphaInterpolation - linearFunctionInAlphaLow - linearFunctionDerivativeInAlphaLow * alpha1) + Math.pow(alpha1, 3) * (linearFunctionInAlphaHigh - linearFunctionInAlphaLow - linearFunctionDerivativeInAlphaLow * alphaDiff));
            alphai = (-b + Math.sqrt(Math.pow(b, 2) - 3 * a * linearFunctionDerivativeInAlphaLow)) / (3 * a);
            if (Math.abs(alphai - alpha1) < 0.000000001) {
                alphai = alpha1 / 2;
            } else {
                if (alphai < (alpha1 - 9 * alpha1 / 10)) {
                    alphai = alpha1 / 2;
                }
            }
            alphaInterpolation = minAlpha + alphai;
            this.linearFunctionAlphaInterpolation = this.evaluateEnergyFunction(alphaInterpolation);
        }
        return alphaInterpolation;
    }

    /**Evaluate the energy function from an alpha value, using the current coordinates and the current direction.
	 * 
	 * @param alpha	
	 * @return			Energy function value.
	 */
    private double evaluateEnergyFunction(double alpha) {
        this.xAlpha.set(this.x);
        GVector directionStep = direction;
        xAlpha.scaleAdd(alpha, directionStep, xAlpha);
        double fxAlpha = pf.energyFunction(xAlpha);
        return fxAlpha;
    }

    /**Evaluate the gradient of the energy function from an alpha value, 
	 * using the current coordinates and the current direction.
	 * 
	 * @param alpha		Alpha value for the one-dimensional problem generate from the current coordinates and the current direction.
	 * @return				Gradient of the energy function at alpha. 
	 */
    private GVector evaluateEnergyFunctionDerivative(double alpha) {
        this.xAlpha.set(this.x);
        GVector directionStep = direction;
        xAlpha.scaleAdd(alpha, directionStep, xAlpha);
        pf.setEnergyGradient(xAlpha);
        GVector dfxAlpha = pf.getEnergyGradient();
        return dfxAlpha;
    }
}
