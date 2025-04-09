package cn.edu.thss.iise.beehivez.server.util;

import java.math.BigInteger;
import java.util.Random;

/**
 * @author Tao Jin
 * 
 */
public class MathUtil {

    public static String UNIFORMDISTRIBUTION = "uniform";

    public static String BINOMIALDISTRIBUTION = "binomial";

    public static boolean[][] twoDimensionalArrayClone(boolean[][] a) {
        boolean[][] b = new boolean[a.length][];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i].clone();
        }
        return b;
    }

    public static long[][] getDistribution(String distribution, int min, int max, long total) {
        if (distribution.equals(UNIFORMDISTRIBUTION)) {
            return getDiscreteUniformDistribution(min, max, total);
        } else if (distribution.equals(BINOMIALDISTRIBUTION)) {
            return getBinomialDistribution(min, max, total);
        } else {
            return null;
        }
    }

    public static long[][] getBinomialDistribution(int min, int max, long total) {
        Random rand = new Random(System.currentTimeMillis());
        int n = max - min;
        long[][] ret = new long[2][n + 1];
        int mean = (n + 1) / 2;
        float p = 1;
        if (n > 0) {
            p = (float) mean / (float) n;
        }
        long count = 0;
        for (int i = 0; i <= n; i++) {
            double p_i = MathUtil.combination(n, i) * Math.pow(p, i) * Math.pow((1 - p), (n - i));
            long count_i = (long) (total * p_i);
            ret[0][i] = i + min;
            ret[1][i] = count_i;
            count += count_i;
        }
        while (count < total) {
            int i = rand.nextInt(n + 1);
            ret[1][i]++;
            count++;
        }
        return ret;
    }

    public static boolean satisfyTriIneq(double x, double y, double z) {
        x = 1 - x;
        y = 1 - y;
        z = 1 - z;
        if (x == 0 || y == 0 || z == 0) return true;
        if (x + y > z && x + z > y && y + z > x) return true;
        return false;
    }

    public static long[][] getDiscreteUniformDistribution(int min, int max, long total) {
        Random rand = new Random(System.currentTimeMillis());
        int span = max - min + 1;
        long avg = total / span;
        long[][] ret = new long[2][span];
        if (avg >= 1) {
            long count = 0;
            for (int i = 0; i < span; i++) {
                ret[0][i] = min + i;
                ret[1][i] = avg;
                count += avg;
            }
            if (count < total) {
                avg = span / (total - count);
                int a = (int) avg;
                int k = 0;
                while (count < total) {
                    int i = rand.nextInt(span);
                    ret[1][a * k]++;
                    k++;
                    count++;
                }
            }
        } else {
            for (int i = 0; i < span; i++) {
                ret[0][i] = min + i;
            }
            long count = 0;
            avg = span / total;
            while (count < total) {
                long k = count * avg;
                int kk = (int) k;
                ret[1][kk] = 1;
                count++;
            }
        }
        return ret;
    }

    public static BigInteger permutation(int n, int k) {
        BigInteger ret = new BigInteger(String.valueOf(1));
        while (k > 0) {
            ret = ret.multiply(new BigInteger(String.valueOf(n)));
            n--;
            k--;
        }
        return ret;
    }

    public static double combination(int n, int k) {
        double ret = 1;
        while (k > 0) {
            ret = ret * ((double) n / (double) k);
            k--;
            n--;
        }
        return ret;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        int min = 1;
        int max = 100;
        int total = 10;
        long[][] dis = getDiscreteUniformDistribution(min, max, total);
        long count = 0;
        for (int i = 0; i < dis[1].length; i++) {
            count += dis[1][i];
            if (dis[1][i] > 0) {
                System.out.println(dis[0][i] + " ---- " + dis[1][i]);
            }
        }
        System.out.println("the count is: " + count);
    }
}
