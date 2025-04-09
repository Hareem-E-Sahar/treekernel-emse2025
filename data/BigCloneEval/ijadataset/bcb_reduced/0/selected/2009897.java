package flanagan.integration;

import java.util.*;
import flanagan.math.Fmath;

public class Integration {

    private IntegralFunction integralFunc = null;

    private boolean setFunction = false;

    private double lowerLimit = Double.NaN;

    private double upperLimit = Double.NaN;

    private boolean setLimits = false;

    private int glPoints = 0;

    private boolean setGLpoints = false;

    private int nIntervals = 0;

    private boolean setIntervals = false;

    private double integralSum = 0.0D;

    private boolean setIntegration = false;

    private static ArrayList<Integer> gaussQuadIndex = new ArrayList<Integer>();

    private static ArrayList<double[]> gaussQuadDistArrayList = new ArrayList<double[]>();

    private static ArrayList<double[]> gaussQuadWeightArrayList = new ArrayList<double[]>();

    private double requiredAccuracy = 0.0D;

    private double trapeziumAccuracy = 0.0D;

    private static double trapAccuracy = 0.0D;

    private int maxIntervals = 0;

    private int trapeziumIntervals = 1;

    private static int trapIntervals = 1;

    public Integration() {
    }

    public Integration(IntegralFunction intFunc) {
        this.integralFunc = intFunc;
        this.setFunction = true;
    }

    public Integration(IntegralFunction intFunc, double lowerLimit, double upperLimit) {
        this.integralFunc = intFunc;
        this.setFunction = true;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.setLimits = true;
    }

    public void setIntegrationFunction(IntegralFunction intFunc) {
        this.integralFunc = intFunc;
        this.setFunction = true;
    }

    public void setLimits(double lowerLimit, double upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.setLimits = true;
    }

    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
        if (!Fmath.isNaN(this.upperLimit)) this.setLimits = true;
    }

    public void setlowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
        if (!Fmath.isNaN(this.upperLimit)) this.setLimits = true;
    }

    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
        if (!Fmath.isNaN(this.lowerLimit)) this.setLimits = true;
    }

    public void setupperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
        if (!Fmath.isNaN(this.lowerLimit)) this.setLimits = true;
    }

    public void setGLpoints(int nPoints) {
        this.glPoints = nPoints;
        this.setGLpoints = true;
    }

    public void setNintervals(int nIntervals) {
        this.nIntervals = nIntervals;
        this.setIntervals = true;
    }

    public double getIntegralSum() {
        if (!this.setIntegration) throw new IllegalArgumentException("No integration has been performed");
        return this.integralSum;
    }

    public double gaussQuad() {
        if (!this.setGLpoints) throw new IllegalArgumentException("Number of points not set");
        if (!this.setLimits) throw new IllegalArgumentException("One limit or both limits not set");
        if (!this.setFunction) throw new IllegalArgumentException("No integral function has been set");
        double[] gaussQuadDist = new double[glPoints];
        double[] gaussQuadWeight = new double[glPoints];
        double sum = 0.0D;
        double xplus = 0.5D * (upperLimit + lowerLimit);
        double xminus = 0.5D * (upperLimit - lowerLimit);
        double dx = 0.0D;
        boolean test = true;
        int k = -1, kn = -1;
        if (!this.gaussQuadIndex.isEmpty()) {
            for (k = 0; k < this.gaussQuadIndex.size(); k++) {
                Integer ki = this.gaussQuadIndex.get(k);
                if (ki.intValue() == this.glPoints) {
                    test = false;
                    kn = k;
                }
            }
        }
        if (test) {
            Integration.gaussQuadCoeff(gaussQuadDist, gaussQuadWeight, glPoints);
            Integration.gaussQuadIndex.add(new Integer(glPoints));
            Integration.gaussQuadDistArrayList.add(gaussQuadDist);
            Integration.gaussQuadWeightArrayList.add(gaussQuadWeight);
        } else {
            gaussQuadDist = gaussQuadDistArrayList.get(kn);
            gaussQuadWeight = gaussQuadWeightArrayList.get(kn);
        }
        for (int i = 0; i < glPoints; i++) {
            dx = xminus * gaussQuadDist[i];
            sum += gaussQuadWeight[i] * this.integralFunc.function(xplus + dx);
        }
        this.integralSum = sum * xminus;
        this.setIntegration = true;
        return this.integralSum;
    }

    public double gaussQuad(int glPoints) {
        this.glPoints = glPoints;
        this.setGLpoints = true;
        return this.gaussQuad();
    }

    public static double gaussQuad(IntegralFunction intFunc, double lowerLimit, double upperLimit, int glPoints) {
        Integration intgrtn = new Integration(intFunc, lowerLimit, upperLimit);
        return intgrtn.gaussQuad(glPoints);
    }

    public static void gaussQuadCoeff(double[] gaussQuadDist, double[] gaussQuadWeight, int n) {
        double z = 0.0D, z1 = 0.0D;
        double pp = 0.0D, p1 = 0.0D, p2 = 0.0D, p3 = 0.0D;
        double eps = 3e-11;
        double x1 = -1.0D;
        double x2 = 1.0D;
        int m = (n + 1) / 2;
        double xm = 0.5D * (x2 + x1);
        double xl = 0.5D * (x2 - x1);
        for (int i = 1; i <= m; i++) {
            z = Math.cos(Math.PI * (i - 0.25D) / (n + 0.5D));
            do {
                p1 = 1.0D;
                p2 = 0.0D;
                for (int j = 1; j <= n; j++) {
                    p3 = p2;
                    p2 = p1;
                    p1 = ((2.0D * j - 1.0D) * z * p2 - (j - 1.0D) * p3) / j;
                }
                pp = n * (z * p1 - p2) / (z * z - 1.0D);
                z1 = z;
                z = z1 - p1 / pp;
            } while (Math.abs(z - z1) > eps);
            gaussQuadDist[i - 1] = xm - xl * z;
            gaussQuadDist[n - i] = xm + xl * z;
            gaussQuadWeight[i - 1] = 2.0 * xl / ((1.0 - z * z) * pp * pp);
            gaussQuadWeight[n - i] = gaussQuadWeight[i - 1];
        }
    }

    public double trapezium() {
        if (!this.setIntervals) throw new IllegalArgumentException("Number of intervals not set");
        if (!this.setLimits) throw new IllegalArgumentException("One limit or both limits not set");
        if (!this.setFunction) throw new IllegalArgumentException("No integral function has been set");
        double y1 = 0.0D;
        double interval = (this.upperLimit - this.lowerLimit) / this.nIntervals;
        double x0 = this.lowerLimit;
        double x1 = this.lowerLimit + interval;
        double y0 = this.integralFunc.function(x0);
        this.integralSum = 0.0D;
        for (int i = 0; i < nIntervals; i++) {
            if (x1 > this.upperLimit) {
                x1 = this.upperLimit;
                interval -= (x1 - this.upperLimit);
            }
            y1 = this.integralFunc.function(x1);
            this.integralSum += 0.5D * (y0 + y1) * interval;
            x0 = x1;
            y0 = y1;
            x1 += interval;
        }
        this.setIntegration = true;
        return this.integralSum;
    }

    public double trapezium(int nIntervals) {
        this.nIntervals = nIntervals;
        this.setIntervals = true;
        return this.trapezium();
    }

    public static double trapezium(IntegralFunction intFunc, double lowerLimit, double upperLimit, int nIntervals) {
        Integration intgrtn = new Integration(intFunc, lowerLimit, upperLimit);
        return intgrtn.trapezium(nIntervals);
    }

    public double trapezium(double accuracy, int maxIntervals) {
        this.requiredAccuracy = accuracy;
        this.maxIntervals = maxIntervals;
        this.trapeziumIntervals = 1;
        double summ = this.trapezium(this.integralFunc, this.lowerLimit, this.upperLimit, 1);
        double oldSumm = summ;
        int i = 2;
        for (i = 2; i <= this.maxIntervals; i++) {
            summ = this.trapezium(this.integralFunc, this.lowerLimit, this.upperLimit, i);
            this.trapeziumAccuracy = Math.abs((summ - oldSumm) / oldSumm);
            if (this.trapeziumAccuracy <= this.requiredAccuracy) break;
            oldSumm = summ;
        }
        if (i > this.maxIntervals) {
            System.out.println("accuracy criterion was not met in Integration.trapezium - current sum was returned as result.");
            this.trapeziumIntervals = this.maxIntervals;
        } else {
            this.trapeziumIntervals = i;
        }
        Integration.trapIntervals = this.trapeziumIntervals;
        Integration.trapAccuracy = this.trapeziumAccuracy;
        return summ;
    }

    public static double trapezium(IntegralFunction intFunc, double lowerLimit, double upperLimit, double accuracy, int maxIntervals) {
        Integration intgrtn = new Integration(intFunc, lowerLimit, upperLimit);
        return intgrtn.trapezium(accuracy, maxIntervals);
    }

    public int getTrapeziumIntervals() {
        return this.trapeziumIntervals;
    }

    public static int getTrapIntervals() {
        return Integration.trapIntervals;
    }

    public double getTrapeziumAccuracy() {
        return this.trapeziumAccuracy;
    }

    public static double getTrapAccuracy() {
        return Integration.trapAccuracy;
    }

    public double backward() {
        if (!this.setIntervals) throw new IllegalArgumentException("Number of intervals not set");
        if (!this.setLimits) throw new IllegalArgumentException("One limit or both limits not set");
        if (!this.setFunction) throw new IllegalArgumentException("No integral function has been set");
        double interval = (this.upperLimit - this.lowerLimit) / this.nIntervals;
        double x = this.lowerLimit + interval;
        double y = this.integralFunc.function(x);
        this.integralSum = 0.0D;
        for (int i = 0; i < this.nIntervals; i++) {
            if (x > this.upperLimit) {
                x = this.upperLimit;
                interval -= (x - this.upperLimit);
            }
            y = this.integralFunc.function(x);
            this.integralSum += y * interval;
            x += interval;
        }
        this.setIntegration = true;
        return this.integralSum;
    }

    public double backward(int nIntervals) {
        this.nIntervals = nIntervals;
        this.setIntervals = true;
        return this.backward();
    }

    public static double backward(IntegralFunction intFunc, double lowerLimit, double upperLimit, int nIntervals) {
        Integration intgrtn = new Integration(intFunc, lowerLimit, upperLimit);
        return intgrtn.backward(nIntervals);
    }

    public double forward() {
        double interval = (this.upperLimit - this.lowerLimit) / this.nIntervals;
        double x = this.lowerLimit;
        double y = this.integralFunc.function(x);
        this.integralSum = 0.0D;
        for (int i = 0; i < this.nIntervals; i++) {
            if (x > this.upperLimit) {
                x = this.upperLimit;
                interval -= (x - this.upperLimit);
            }
            y = this.integralFunc.function(x);
            this.integralSum += y * interval;
            x += interval;
        }
        this.setIntegration = true;
        return this.integralSum;
    }

    public double forward(int nIntervals) {
        this.nIntervals = nIntervals;
        this.setIntervals = true;
        return this.forward();
    }

    public static double forward(IntegralFunction integralFunc, double lowerLimit, double upperLimit, int nIntervals) {
        Integration intgrtn = new Integration(integralFunc, lowerLimit, upperLimit);
        return intgrtn.forward(nIntervals);
    }

    public static double foreward(IntegralFunction integralFunc, double lowerLimit, double upperLimit, int nIntervals) {
        Integration intgrtn = new Integration(integralFunc, lowerLimit, upperLimit);
        return intgrtn.forward(nIntervals);
    }
}
