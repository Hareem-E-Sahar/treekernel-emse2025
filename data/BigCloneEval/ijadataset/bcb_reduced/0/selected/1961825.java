package moduledefault.classify.naivebayes;

/*************************************************************************
 *  Compilation:  javac Gaussian.java
 *  Execution:    java Gaussian x mu sigma
 *
 *  Function to compute the Gaussian pdf (probability density function)
 *  and the Gaussian cdf (cumulative density function)
 *
 *  % java Gaussian 820 1019 209
 *  0.17050966869132111
 *
 *  % java Gaussian 1500 1019 209
 *  0.9893164837383883
 *
 *  % java Gaussian 1500 1025 231
 *  0.9801220907365489
 *
 *  The approximation is accurate to absolute error less than 8 * 10^(-16).
 *  Reference: Evaluating the Normal Distribution by George Marsaglia.
 *  http://www.jstatsoft.org/v11/a04/paper
 *
 *************************************************************************/
public class Gaussian {

    public static double phi(double x) {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    public static double phi(double x, double mu, double sigma) {
        return (sigma == 0) ? deltaDirac(x - mu) : (phi((x - mu) / sigma) / sigma);
    }

    public static double deltaDirac(double sigma) {
        if (sigma == 0) return Double.POSITIVE_INFINITY; else return 0.0;
    }

    public static double Phi(double z) {
        if (z < -8.0) return 0.0;
        if (z > 8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * phi(z);
    }

    public static double Phi(double z, double mu, double sigma) {
        return Phi((z - mu) / sigma);
    }

    public static double PhiInverse(double y) {
        return PhiInverse(y, .00000001, -8, 8);
    }

    private static double PhiInverse(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (Phi(mid) > y) return PhiInverse(y, delta, lo, mid); else return PhiInverse(y, delta, mid, hi);
    }
}
