package shellkk.qiq.math.ml.fisher;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import shellkk.qiq.math.StopException;
import shellkk.qiq.math.StopHandle;
import shellkk.qiq.math.matrix.Matrix;
import shellkk.qiq.math.ml.ArraySampleSet;
import shellkk.qiq.math.ml.Classifier;
import shellkk.qiq.math.ml.Sample;
import shellkk.qiq.math.ml.SampleSet;
import shellkk.qiq.math.ml.TrainException;

public class SimpleBinaryPatternFisher implements Classifier, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(SimpleBinaryPatternFisher.class);

    protected double lambda = 0.0;

    protected double[] weight;

    protected double bias;

    protected double positiveProbability;

    protected double positiveMean;

    protected double positiveVar;

    protected double negativeProbability;

    protected double negativeMean;

    protected double negativeVar;

    protected double squareNorm;

    public int getPatternCount() {
        return 2;
    }

    public double getProbabilityAt(Object x, int pattern) {
        return getProbabilityArray(x)[pattern];
    }

    public double[] getProbabilityArray(Object x) {
        double y = getYValue(x);
        double dn = y - negativeMean;
        double pn = negativeProbability * Math.sqrt(1.0D / negativeVar) * Math.exp(-dn * dn / (2.0D * negativeVar));
        double dp = y - positiveMean;
        double pp = positiveProbability * Math.sqrt(1.0D / positiveVar) * Math.exp(-dp * dp / (2.0D * positiveVar));
        double all = pn + pp;
        double[] p = new double[2];
        p[0] = all > 0 ? pn / all : 0.5;
        p[1] = 1 - p[0];
        return p;
    }

    public double getY(Object t) {
        double v = getYValue(t);
        return v > 0 ? 1 : 0;
    }

    protected double computeWX(Object t) {
        double[] x = (double[]) t;
        double v = 0;
        for (int i = 0; i < weight.length; i++) {
            v += x[i] * weight[i];
        }
        return v;
    }

    public double getYValue(Object t) {
        double v = computeWX(t);
        v += bias;
        return v;
    }

    protected double computeB(double up, double un, double sp, double sn, double wp, double wn) {
        if (sp == sn) {
            double k = up - un;
            double y = 0.5D * (up * up - un * un) + sp * Math.log(wn / wp);
            return -y / k;
        } else {
            double _a = 0.5D * (1.0D / sn - 1.0D / sp);
            double _b = up / sp - un / sn;
            double q = Math.sqrt(sn / sp) * wp / wn;
            double _c = un * un / (2.0D * sn) - up * up / (2.0D * sp) + Math.log(q);
            double k2 = _b * _b - 4 * _a * _c;
            double k = k2 > 0 ? Math.sqrt(k2) : 0;
            double x = (-_b + k) / (2.0D * _a);
            if (x >= un && x <= up) {
                return -x;
            } else {
                x = (-_b - k) / (2.0D * _a);
                return -x;
            }
        }
    }

    protected void regularize(double w1, double w0, double[] m1, double[] m0, double[][] var1, double[][] var0) {
        double u1 = 0;
        double u0 = 0;
        double delta = 0;
        double s1 = 0;
        double s0 = 0;
        Matrix W = new Matrix(weight, 1);
        Matrix Wt = W.transpose();
        Matrix M1 = new Matrix(m1, m1.length);
        Matrix M0 = new Matrix(m0, m0.length);
        Matrix A1 = new Matrix(var1);
        Matrix A0 = new Matrix(var0);
        u1 = W.times(M1).get(0, 0);
        u0 = W.times(M0).get(0, 0);
        delta = u1 - u0;
        s1 = W.times(A1).times(Wt).get(0, 0);
        s0 = W.times(A0).times(Wt).get(0, 0);
        double dd = delta * delta;
        s1 = s1 / dd;
        s0 = s0 / dd;
        if (s1 <= 0.0001) {
            s1 = 0.0001;
        }
        if (s0 <= 0.0001) {
            s0 = 0.0001;
        }
        for (int i = 0; i < weight.length; i++) {
            weight[i] = weight[i] / delta;
        }
        u1 = u1 / delta;
        u0 = u0 / delta;
        bias = computeB(u1, u0, s1, s0, w1, w0);
        double ww = 0;
        for (int i = 0; i < weight.length; i++) {
            ww += weight[i] * weight[i];
        }
        squareNorm = ww;
        positiveVar = s1;
        negativeVar = s0;
        positiveMean = u1 + bias;
        negativeMean = u0 + bias;
        positiveProbability = w1 / (w1 + w0);
        negativeProbability = w0 / (w1 + w0);
    }

    public void train(SampleSet sampleSet, StopHandle stopHandle) throws StopException, TrainException {
        try {
            log.info("initializing...");
            sampleSet.open();
            double[] x0 = (double[]) sampleSet.next().x;
            int l = x0.length;
            double[] m1 = new double[l];
            double[] m0 = new double[l];
            double w1 = 0;
            double w0 = 0;
            double[][] s1 = new double[l][l];
            double[][] s0 = new double[l][l];
            while (sampleSet.hasNext()) {
                if (stopHandle != null && stopHandle.isStoped()) {
                    throw new StopException();
                }
                Sample sample = sampleSet.next();
                double[] x = (double[]) sample.x;
                int y = (int) sample.y;
                double w = sample.weight;
                if (y > 0) {
                    w1 += w;
                    for (int i = 0; i < l; i++) {
                        m1[i] += w * x[i];
                    }
                    for (int i = 0; i < l; i++) {
                        for (int j = 0; j < i; j++) {
                            s1[i][j] += w * x[i] * x[j];
                        }
                        s1[i][i] += w * x[i] * x[i];
                    }
                } else {
                    w0 += w;
                    for (int i = 0; i < l; i++) {
                        m0[i] += w * x[i];
                    }
                    for (int i = 0; i < l; i++) {
                        for (int j = 0; j < i; j++) {
                            s0[i][j] += w * x[i] * x[j];
                        }
                        s0[i][i] += w * x[i] * x[i];
                    }
                }
            }
            double q1 = 1.0d / w1;
            double q0 = 1.0d / w0;
            double q = 1.0d / (w1 + w0);
            for (int i = 0; i < l; i++) {
                m1[i] *= q1;
                m0[i] *= q0;
            }
            for (int i = 0; i < l; i++) {
                for (int j = 0; j < i; j++) {
                    s1[i][j] *= q1;
                    s1[j][i] = s1[i][j];
                    s0[i][j] *= q0;
                    s0[j][i] = s0[i][j];
                }
                s1[i][i] *= q1;
                s0[i][i] *= q0;
            }
            double[][] s = new double[l][l];
            for (int i = 0; i < l; i++) {
                for (int j = 0; j < i; j++) {
                    s[i][j] = q * (w1 * s1[i][j] + w0 * s0[i][j] - w1 * m1[i] * m1[j] - w0 * m0[i] * m0[j]);
                    s[j][i] = s[i][j];
                }
                s[i][i] = q * (w1 * s1[i][i] + w0 * s0[i][i] - w1 * m1[i] * m1[i] - w0 * m0[i] * m0[i]) + lambda;
            }
            double[] m = new double[l];
            for (int i = 0; i < l; i++) {
                m[i] = m1[i] - m0[i];
            }
            log.info("solving...");
            Matrix S = new Matrix(s);
            Matrix M = new Matrix(m, m.length);
            Matrix W = S.inverse().times(M);
            weight = W.getRowPackedCopy();
            log.info("solved!");
            regularize(w1, w0, m1, m0, s1, s0);
            log.info("mean[+1]=" + positiveMean + " var[+1]=" + positiveVar);
            log.info("mean[-1]=" + negativeMean + " var[-1]=" + negativeVar);
            StringBuffer msg = new StringBuffer();
            msg.append("weight = ");
            for (int i = 0; i < l; i++) {
                if (i > 0) {
                    msg.append(",");
                }
                msg.append(weight[i]);
            }
            log.info(msg.toString());
            log.info("bias=" + bias);
        } catch (StopException e) {
            throw e;
        } catch (Exception e) {
            throw new TrainException(e);
        } finally {
            try {
                sampleSet.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double[] getWeight() {
        return weight;
    }

    public void setWeight(double[] weight) {
        this.weight = weight;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getPositiveProbability() {
        return positiveProbability;
    }

    public void setPositiveProbability(double positiveProbability) {
        this.positiveProbability = positiveProbability;
    }

    public double getPositiveMean() {
        return positiveMean;
    }

    public void setPositiveMean(double positiveMean) {
        this.positiveMean = positiveMean;
    }

    public double getPositiveVar() {
        return positiveVar;
    }

    public void setPositiveVar(double positiveVar) {
        this.positiveVar = positiveVar;
    }

    public double getNegativeProbability() {
        return negativeProbability;
    }

    public void setNegativeProbability(double negativeProbability) {
        this.negativeProbability = negativeProbability;
    }

    public double getNegativeMean() {
        return negativeMean;
    }

    public void setNegativeMean(double negativeMean) {
        this.negativeMean = negativeMean;
    }

    public double getNegativeVar() {
        return negativeVar;
    }

    public void setNegativeVar(double negativeVar) {
        this.negativeVar = negativeVar;
    }

    public double getSquareNorm() {
        return squareNorm;
    }

    public void setSquareNorm(double squareNorm) {
        this.squareNorm = squareNorm;
    }

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("log4j.properties");
            double[] w = { 0.1, -0.1, 0.15, -0.15, 0.2, -0.2 };
            Sample[] samples = new Sample[500];
            for (int i = 0; i < samples.length; i++) {
                double[] xi = genrate(6);
                int yi = foo(xi, w, 0);
                samples[i] = new Sample(xi, yi);
                samples[i].setWeight(Math.random());
            }
            SimpleBinaryPatternFisher fisher = new SimpleBinaryPatternFisher();
            fisher.setLambda(0);
            long begin = System.currentTimeMillis();
            fisher.train(new ArraySampleSet(samples), null);
            long end = System.currentTimeMillis();
            System.out.println("seconds used by training:" + ((end - begin) / 1000));
            double err1 = 0;
            for (int i = 0; i < samples.length; i++) {
                int yi = (int) samples[i].y;
                int pi = (int) fisher.getY(samples[i].x);
                err1 += yi == pi ? 0 : 1;
            }
            err1 = err1 / samples.length;
            System.out.println("emp risk:" + err1);
            double err = 0;
            for (int i = 0; i < 1000; i++) {
                double[] xi = genrate(6);
                int yi = foo(xi, w, 0);
                int pi = (int) fisher.getY(xi);
                err += yi == pi ? 0 : 1;
            }
            err = err / 1000;
            System.out.println("expect risk:" + err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[] genrate(int l) {
        double[] x = new double[l];
        for (int i = 0; i < l; i++) {
            x[i] = Math.random();
        }
        return x;
    }

    private static int foo(double[] x, double[] w, double b) {
        double y = 0;
        for (int i = 0; i < x.length; i++) {
            y += x[i] * w[i];
        }
        return y > b ? 1 : 0;
    }
}
