package math.statistics;

import math.GammaFunc;

/**
 * @author umezawa
 *
 * �Ŗސ�����s��
 */
public class MaximunLikelihood implements Regression {

    double mean;

    double[] data;

    double tmp;

    GammaFunc gamma;

    GammaDistribution gd;

    static double STEP = 0.25;

    public MaximunLikelihood() {
        gamma = new GammaFunc(100, 10000);
    }

    public double[] doRegression(double[] tmpdata, double tmp2, double tmp3) {
        this.data = tmpdata;
        double a = 1.05;
        sumlog();
        double lf = f(a);
        double rf, mf;
        double la, ra, ma;
        ma = la = a;
        rf = lf;
        for (ra = la; lf * rf > 0; la = ra, ra += STEP) {
            rf = f(ra);
            if (ra > 1000) {
                return null;
            }
        }
        for (int i = 0; i < 10; ++i) {
            ma = (ra + la) / 2;
            if ((mf = f(ma)) * rf < 0) {
                lf = mf;
                la = ma;
            } else {
                rf = mf;
                ra = ma;
            }
        }
        System.out.println(ma);
        System.out.println(mean);
        gd = new GammaDistribution(ma, ma / mean, 1000, 10000);
        return null;
    }

    private double calcMean() {
        double sum = 0;
        for (int i = 0; i < data.length; ++i) {
            sum += data[i];
        }
        mean = sum / data.length;
        System.out.println(" mean " + mean);
        return mean;
    }

    private double sumlog() {
        tmp = 0;
        calcMean();
        for (int i = 0; i < data.length; ++i) {
            tmp += Math.log(data[i] / mean);
        }
        System.out.println("yuudo " + tmp);
        return tmp;
    }

    private double f(double x) {
        return (data.length * Math.log(x) + tmp - data.length * (gamma.calc(x + gamma.getDx()) - gamma.calc(x - gamma.getDx())) / (gamma.calc(x) * 2 * gamma.getDx()));
    }

    public GammaDistribution getGD() {
        return gd;
    }
}
