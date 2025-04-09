package shellkk.qiq.math.ml.mse;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import shellkk.qiq.math.StopException;
import shellkk.qiq.math.StopHandle;
import shellkk.qiq.math.matrix.Matrix;
import shellkk.qiq.math.ml.ArraySampleSet;
import shellkk.qiq.math.ml.LearnMachine;
import shellkk.qiq.math.ml.Sample;
import shellkk.qiq.math.ml.SampleSet;
import shellkk.qiq.math.ml.TrainException;

public class SimpleRidgeRegression implements LearnMachine, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(SimpleRidgeRegression.class);

    protected double lambda = 0.0;

    protected double[] weight;

    protected double bias;

    protected double squareNorm;

    protected double computeSquareNorm() {
        double ww = 0;
        for (int i = 0; i < weight.length; i++) {
            ww += weight[i] * weight[i];
        }
        return ww;
    }

    public double getY(Object t) {
        if (weight == null) {
            return 0;
        }
        double[] x = (double[]) t;
        double v = 0;
        for (int i = 0; i < weight.length; i++) {
            v += x[i] * weight[i];
        }
        return v + bias;
    }

    public void train(SampleSet sampleSet, StopHandle stopHandle) throws StopException, TrainException {
        log.info("initializing...");
        try {
            sampleSet.open();
            double[] x0 = (double[]) sampleSet.next().x;
            int l = x0.length;
            double[][] m = new double[l + 1][l + 1];
            double[] xy = new double[l + 1];
            sampleSet.first();
            while (sampleSet.hasNext()) {
                if (stopHandle != null && stopHandle.isStoped()) {
                    throw new StopException();
                }
                Sample sample = sampleSet.next();
                double[] x = (double[]) sample.x;
                double y = sample.y;
                double w = sample.weight;
                for (int i = 0; i < l; i++) {
                    for (int j = 0; j < i; j++) {
                        double xixj = x[i] * x[j];
                        m[i][j] += w * xixj;
                    }
                    m[i][i] += w * x[i] * x[i];
                    m[i][l] += w * x[i];
                    xy[i] += w * x[i] * y;
                }
                m[l][l] += w;
                xy[l] += w * y;
            }
            double q = 1.0d / m[l][l];
            for (int i = 0; i < l; i++) {
                for (int j = 0; j < i; j++) {
                    m[i][j] *= q;
                    m[j][i] = m[i][j];
                }
                m[i][i] = q * m[i][i] + lambda;
                m[i][l] *= q;
                m[l][i] = m[i][l];
                xy[i] *= q;
            }
            m[l][l] = 1;
            xy[l] *= q;
            Matrix M = new Matrix(m);
            Matrix XY = new Matrix(xy, xy.length);
            Matrix W = M.inverse().times(XY);
            weight = new double[l];
            for (int i = 0; i < l; i++) {
                weight[i] = W.get(i, 0);
            }
            bias = W.get(l, 0);
            squareNorm = computeSquareNorm();
            StringBuffer msg = new StringBuffer();
            msg.append("weight = ");
            for (int i = 0; i < l; i++) {
                if (i > 0) {
                    msg.append(",");
                }
                msg.append(weight[i]);
            }
            log.info(msg.toString());
            log.info("bias = " + bias);
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

    public double getSquareNorm() {
        return squareNorm;
    }

    public void setSquareNorm(double squareNorm) {
        this.squareNorm = squareNorm;
    }

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("log4j.properties");
            Sample[] samples = new Sample[250];
            for (int i = 0; i < samples.length; i++) {
                double[] xi = randomX();
                double yi = foo(xi);
                double[] Xi = xi;
                samples[i] = new Sample(Xi, yi);
                samples[i].setWeight(Math.random());
            }
            SimpleRidgeRegression reg = new SimpleRidgeRegression();
            reg.setLambda(0.000001);
            long begin = System.currentTimeMillis();
            reg.train(new ArraySampleSet(samples), null);
            long end = System.currentTimeMillis();
            System.out.println("seconds used by training:" + ((end - begin) / 1000));
            double err1 = 0;
            for (int i = 0; i < samples.length; i++) {
                double yi = samples[i].y;
                double pi = reg.getY(samples[i].x);
                double ei = Math.abs(pi - yi);
                err1 += ei;
            }
            err1 = err1 / samples.length;
            System.out.println("emp risk:" + err1);
            double err = 0;
            for (int i = 0; i < 1000; i++) {
                double[] xi = randomX();
                double yi = foo(xi);
                double[] X = xi;
                double pi = reg.getY(X);
                double ei = Math.abs(pi - yi);
                err += ei;
            }
            err = err / 1000;
            System.out.println("expect risk:" + err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[] randomX() {
        double[] x = new double[50];
        for (int i = 0; i < x.length; i++) {
            x[i] = Math.random();
        }
        return x;
    }

    private static double foo(double[] x) {
        double v = 0;
        for (int i = 0; i < x.length; i++) {
            v += x[i] * (i + 1);
        }
        return v + 1 + Math.random() * 0.1;
    }
}
