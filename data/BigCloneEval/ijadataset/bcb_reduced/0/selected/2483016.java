package org.jmathematics;

import java.math.BigDecimal;

public class MathUtils {

    public static final Double DOUBLE_0 = new Double(0.0);

    public static final Double DOUBLE_1 = new Double(1.0);

    public static final Double DOUBLE_15_PERCENT = new Double(0.15);

    public static final Double DOUBLE_4_PERCENT = new Double(0.04);

    public static final BigDecimal BIGDECIMAL_0 = BigDecimal.ZERO;

    public static final BigDecimal BIGDECIMAL_1 = BigDecimal.ONE;

    public static final BigDecimal BIGDECIMAL_1_PERCENT = new BigDecimal("0.01");

    public static final BigDecimal BIGDECIMAL_3_PERCENT = new BigDecimal("0.03");

    public static final BigDecimal BIGDECIMAL_6_PERCENT = new BigDecimal("0.06");

    public static final BigDecimal BIGDECIMAL_7_PERCENT = new BigDecimal("0.07");

    public static final BigDecimal BIGDECIMAL_8_PERCENT = new BigDecimal("0.08");

    public static final BigDecimal BIGDECIMAL_10_PERCENT = new BigDecimal("0.1");

    public static final BigDecimal BIGDECIMAL_12_PERCENT = new BigDecimal("0.12");

    public static final BigDecimal BIGDECIMAL_15_PERCENT = new BigDecimal("0.15");

    public static final BigDecimal BIGDECIMAL_16_PERCENT = new BigDecimal("0.16");

    public static final BigDecimal BIGDECIMAL_18_PERCENT = new BigDecimal("0.18");

    public static final BigDecimal BIGDECIMAL_20_PERCENT = new BigDecimal("0.2");

    public static final BigDecimal BIGDECIMAL_24_PERCENT = new BigDecimal("0.24");

    public static final BigDecimal BIGDECIMAL_25_PERCENT = new BigDecimal("0.25");

    public static final BigDecimal BIGDECIMAL_35_PERCENT = new BigDecimal("0.35");

    public static final BigDecimal BIGDECIMAL_50_PERCENT = new BigDecimal("0.5");

    public static final BigDecimal BIGDECIMAL_60_PERCENT = new BigDecimal("0.6");

    public static final BigDecimal BIGDECIMAL_75_PERCENT = new BigDecimal("0.75");

    public static final BigDecimal BIGDECIMAL_100_PERCENT = BIGDECIMAL_1;

    public static final BigDecimal BIGDECIMAL_106_PERCENT = new BigDecimal("1.06");

    public static final BigDecimal BIGDECIMAL_150_PERCENT = new BigDecimal("1.5");

    public static final BigDecimal BIGDECIMAL_250_PERCENT = new BigDecimal("2.5");

    public static final BigDecimal BIGDECIMAL_350_PERCENT = new BigDecimal("3.5");

    public static final BigDecimal BIGDECIMAL_425_PERCENT = new BigDecimal("4.25");

    public static final BigDecimal BIGDECIMAL_650_PERCENT = new BigDecimal("6.5");

    public static final BigDecimal BIGDECIMAL_1250_PERCENT = new BigDecimal("12.5");

    public static final BigDecimal BIGDECIMAL_35 = new BigDecimal(35);

    public static final BigDecimal BIGDECIMAL_50 = new BigDecimal(50);

    public static boolean isZero(BigDecimal value) {
        return BIGDECIMAL_0.compareTo(value) == 0;
    }

    public static Double fraction(BigDecimal dividend, BigDecimal divisor) {
        if (isZero(dividend)) return DOUBLE_0;
        if (divisor.compareTo(dividend) == 0) return DOUBLE_1;
        return new Double(dividend.doubleValue() / divisor.doubleValue());
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) return b;
        return a;
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) return a;
        return b;
    }

    private static double incompleteBetaFunction(double x, double a, double b) {
        if ((x < 0.0) || (x > 1.0)) throw new IllegalArgumentException(x + " not in 0.0 <= x <= 1.0");
        if (a < 0) throw new IllegalArgumentException(a + " not in 0.0 <= a");
        if (a < 0) throw new IllegalArgumentException(b + " not in 0.0 <= b");
        double sum = 1.0 / a;
        double value;
        double dn;
        double zaehler = 1.0;
        double nennerfakultaet = 1.0;
        double multiplier = 1.0;
        boolean stop = false;
        for (int n = 1; (!stop) && n <= nMax; n++) {
            dn = (double) n;
            zaehler *= (dn - b);
            nennerfakultaet *= dn;
            multiplier *= x;
            value = multiplier * (zaehler / (nennerfakultaet * (a + dn)));
            stop = Double.isNaN(value) || Double.isInfinite(value) || ((n > 100) && (Math.abs(value) < Epsilon));
            if (!stop) sum += value;
        }
        return Math.pow(x, a) * sum;
    }

    private static double betaFunction(double a, double b) {
        return gammaFunction(a) * gammaFunction(b) / gammaFunction(a + b);
    }

    private static final double Euler_Macheroni_constant = 0.577215664901532860606512090082402431042;

    private static final double Epsilon = 0.000000001;

    private static final int nMax = Integer.MAX_VALUE;

    private static double gammaFunction(double x) {
        double product = 1.0;
        double value;
        double dn;
        boolean stop = false;
        for (int n = 1; (!stop) && n < nMax; n++) {
            dn = (double) n;
            value = ((1.0 + x / dn) * Math.exp(-x / dn));
            stop = Double.isNaN(value) || Double.isInfinite(value) || ((n > 100) && (Math.abs(1.0 - Math.abs(value)) < Epsilon));
            if (!stop) product *= value;
        }
        return 1.0 / (x * Math.exp(Euler_Macheroni_constant * x) * product);
    }

    private static double cumulativeBetaDistribution(double x, double a, double b) {
        if ((x < 0.0) || (x > 1.0)) throw new IllegalArgumentException(x + " not in 0.0 <= x <= 1.0");
        if (a < 0) throw new IllegalArgumentException(a + " not in 0.0 <= a");
        if (b < 0) throw new IllegalArgumentException(b + " not in 0.0 <= b");
        if (x < Epsilon) return 0.0;
        if (x > (1.0 - Epsilon)) return 1.0;
        return incompleteBetaFunction(x, a, b) / betaFunction(a, b);
    }

    public static double BETADIST(double x, double a, double b) {
        return cumulativeBetaDistribution(x, a, b);
    }

    private static final double sqrt2pi = Math.sqrt(2.0 * Math.PI);

    private static double standardNormalVerteilung(double x) {
        return Math.exp(-Math.pow(x, 2.0) / 2.0) / sqrt2pi;
    }

    private static final double snv_intervall = 0.00001;

    private static final double snv_one = 8.0;

    private static final int snv_cache_length = (int) (snv_one / snv_intervall);

    private static final double[] snv_cache = new double[snv_cache_length];

    static {
        double sum = 0.5;
        double lastValue = standardNormalVerteilung(0);
        double value;
        double midValue;
        for (int i = 0; i < snv_cache_length; i++) {
            value = standardNormalVerteilung(((double) (i + 1)) * snv_intervall);
            midValue = (value + lastValue) / 2.0;
            lastValue = value;
            snv_cache[i] = sum += midValue * snv_intervall;
        }
    }

    public static double kumulativeStandardNormalVerteilung(double x) {
        if (x == 0.0) return 0.5;
        if (x < 0.0) return 1.0 - kumulativeStandardNormalVerteilung(-x);
        if (x >= snv_one) return 1.0;
        int i = (int) (x / snv_intervall);
        if (i >= snv_cache_length) return 1.0;
        return snv_cache[i];
    }

    private static double findInverse(double x, int lower, int higher) {
        while (lower < higher - 1) {
            if (x == snv_cache[lower]) higher = lower + 1; else if (x == snv_cache[higher]) lower = higher - 1; else {
                int mid = (lower + higher) / 2;
                if (x < snv_cache[mid]) higher = mid; else lower = mid;
            }
        }
        if (x == snv_cache[lower]) return lower * snv_intervall;
        if (x == snv_cache[higher]) return higher * snv_intervall;
        double lower_part = (snv_cache[higher] - x) / (snv_cache[higher] - snv_cache[lower]);
        return lower * lower_part + higher * (1.0 - lower_part);
    }

    public static double inverseKumulativeStandardNormalVerteilung(double x) {
        if (x == 0.5) return 0.0;
        if (x < 0.5) return -inverseKumulativeStandardNormalVerteilung(1.0 - x);
        if (x > snv_cache[snv_cache_length - 1]) return snv_one;
        return findInverse(x, 0, snv_cache_length - 1);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) System.out.println("BETADIST(0." + i + ";0.3,0.7)=" + BETADIST(((double) i) / 10.0, 0.3, 0.7));
        System.out.println("BETADIST(1.0;0.3,0.7)=" + BETADIST(1.0, 0.3, 0.7));
    }

    private MathUtils() {
    }
}
