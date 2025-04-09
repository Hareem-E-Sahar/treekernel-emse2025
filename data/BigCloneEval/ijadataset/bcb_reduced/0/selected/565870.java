package main.util;

public class MathUtils {

    public static int divide(int num, int denum) {
        if (denum != 0) {
            return (int) num / denum;
        } else return 0;
    }

    public static double divide(double num, double denum) {
        if (denum != 0) {
            return num / denum;
        } else return 0.0;
    }

    public static double atan(double x) {
        double a, b;
        a = 1 / Math.sqrt(1 + (x * x));
        b = 1;
        for (int n = 1; n <= 15; n++) {
            a = (a + b) / 2;
            b = Math.sqrt(a * b);
        }
        return x / (Math.sqrt(1 + (x * x)) * a);
    }

    public static double positiveValue(double x) {
        if (x < 0) return -x;
        return x;
    }

    public static int nearestInt(double x) {
        int i = (int) x;
        if ((x - i) > 0.5) i++;
        return i;
    }
}
