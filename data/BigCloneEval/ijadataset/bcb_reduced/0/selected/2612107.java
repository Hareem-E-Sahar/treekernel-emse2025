package gullsview;

public class PoorMath {

    public static final double LOG2 = 0.6931471805599453;

    public static double ipow(double d, int exp) {
        double ret = d;
        for (int i = 1; i < exp; i++) ret *= d;
        return ret;
    }

    public static double atan(double x) {
        if (Math.abs(x) > 1) return 2 * atan(x / (1 + Math.sqrt(1 + (x * x))));
        double ret = 0;
        for (int i = 0; i < 100; i++) {
            int e = 2 * i + 1;
            double member = ipow(x, e) / e;
            ret += (i % 2 == 0) ? member : -member;
        }
        return ret;
    }

    public static long factorial(int i) {
        long ret = i;
        for (int j = i - 1; j > 1; j--) ret *= j;
        return ret;
    }

    public static double dfactorial(int i) {
        double ret = i;
        for (int j = i - 1; j > 1; j--) ret *= j;
        return ret;
    }

    public static double exp(double x) {
        double x2 = x * x;
        double v = 1;
        for (int i = 50; i > 0; i--) v = (4 * i + 2) + (x2 / v);
        return 1 + (2 * x / ((2 - x) + (x2 / v)));
    }

    public static double agmean(double x, double y) {
        for (int i = 0; i < 10; i++) {
            double a = (x + y) / 2;
            double g = Math.sqrt(x * y);
            x = a;
            y = g;
        }
        return x;
    }

    public static double log(double x) {
        int m = 128;
        double s = x;
        for (int i = 0; i < m; i++) s *= 2;
        return Math.PI / (2 * agmean(1, 4D / s)) - (m * LOG2);
    }

    public static double acos(double x) {
        return 2 * atan(Math.sqrt(1 - (x * x)) / (1 + x));
    }
}
