package gem.util;

import java.util.*;

/**
 * Kolmogorov-Smirnov test.
 *
 * @author Ozgun Babur
 */
public class Kolmogorov {

    public static double calcDStat(List<Double> set1, List<Double> set2) {
        return getFarthestD(getSortedArray(set1), getSortedArray(set2));
    }

    public static double[] getMinMaxD(List<Double> set1, List<Double> set2) {
        Double[] s1 = getSortedArray(set1);
        Double[] s2 = getSortedArray(set2);
        return new double[] { getMinD(s1, s2), getMaxD(s1, s2) };
    }

    private static Double[] getSortedArray(List<Double> list) {
        Collections.sort(list);
        return list.toArray(new Double[list.size()]);
    }

    private static double getFarthestD(Double[] vals1, Double[] vals2) {
        double far = 0;
        int i = 0, j = 0;
        while (i < vals1.length - 1 && j < vals2.length - 1) {
            int nexti = i + 1;
            int nextj = j + 1;
            if (vals1[nexti] <= vals2[nextj]) i++;
            if (vals1[nexti] >= vals2[nextj]) j++;
            double d = (i / (double) vals1.length) - (j / (double) vals2.length);
            if (Math.abs(d) > Math.abs(far)) far = d;
        }
        return far;
    }

    private static double getMaxD(Double[] vals1, Double[] vals2) {
        double max = 0;
        int i = 0, j = 0;
        while (i < vals1.length - 1 && j < vals2.length - 1) {
            int nexti = i + 1;
            int nextj = j + 1;
            if (vals1[nexti] <= vals2[nextj]) i++;
            if (vals1[nexti] >= vals2[nextj]) j++;
            double d = (i / (double) vals1.length) - (j / (double) vals2.length);
            if (d > max) max = d;
        }
        return max;
    }

    private static double getMinD(Double[] vals1, Double[] vals2) {
        double min = 0;
        int i = 0, j = 0;
        while (i < vals1.length - 1 && j < vals2.length - 1) {
            int nexti = i + 1;
            int nextj = j + 1;
            if (vals1[nexti] <= vals2[nextj]) i++;
            if (vals1[nexti] >= vals2[nextj]) j++;
            double d = (i / (double) vals1.length) - (j / (double) vals2.length);
            if (d < min) min = d;
        }
        return min;
    }

    public static double calcPval(double d, int n1, int n2) {
        if (d < 0) d = -d;
        double chs = (4 * d * d * n1 * n2) / (n1 + n2);
        double pval = chiSq(chs, 2);
        return pval;
    }

    private static double chiSq(double x, int n) {
        if (n == 1 && x > 1000) return 0;
        if (x > 1000 || n > 1000) {
            double q = chiSq((x - n) * (x - n) / (2 * n), 1) / 2;
            if (x > n) return q;
            return 1 - q;
        }
        double p = Math.exp(-0.5 * x);
        if ((n % 2) == 1) p = p * Math.sqrt(2 * x / Math.PI);
        double k = n;
        while (k >= 2) {
            p = p * x / k;
            k = k - 2;
        }
        double t = p;
        double a = n;
        while (t > 0.0000000001 * p) {
            a = a + 2;
            t = t * x / a;
            p = p + t;
        }
        return 1 - p;
    }

    public static void plotMovingD(Set<Point> set1, Set<Point> set2, double from, double to, double width, double step) {
        for (double i = from; i <= to; i += step) {
            double border = i + width;
            double mid = (i + border) / 2;
            List<Double> s1 = new ArrayList<Double>();
            List<Double> s2 = new ArrayList<Double>();
            for (Point p : set1) if (p.x >= i && p.x <= border) s1.add(p.y);
            for (Point p : set2) if (p.x >= i && p.x <= border) s2.add(p.y);
            double[] d = getMinMaxD(s1, s2);
            double s2rat = s2.size() / (double) (s1.size() + s2.size());
            System.out.println(mid + "\t" + d[1] + "\t" + d[0] + "\t" + s2rat);
        }
    }

    public static void plotMovingD(Set<Point> set, double from, double to, double width, double step) {
        for (double i = from; i <= to; i += step) {
            double border = i + width;
            double mid = (i + border) / 2;
            List<Double> list = new ArrayList<Double>();
            for (Point p : set) if (p.x >= i && p.x <= border) {
                list.add(p.y);
            }
            Double[] ys = list.toArray(new Double[list.size()]);
            Arrays.sort(ys);
            double median = ys[ys.length / 2];
            List<Double> s1 = new ArrayList<Double>();
            List<Double> s2 = new ArrayList<Double>();
            for (Point p : set) if (p.x >= i && p.x <= border) {
                if (p.y < median) s1.add(p.z); else s2.add(p.z);
            }
            double d = calcDStat(s1, s2);
            System.out.println(mid + "\t" + d + "\t" + median);
        }
    }

    private static double mean(List<Double> list) {
        double mean = 0;
        for (Double v : list) {
            mean += v;
        }
        mean /= list.size();
        return mean;
    }

    private static void printDensities(Set<Double> set1, Set<Double> set2) {
        Histogram h1 = new Histogram(0.1);
        Histogram h2 = new Histogram(0.1);
        for (Double v : set1) {
            h1.count(v);
        }
        for (Double v : set2) {
            h2.count(v);
        }
        System.out.println("\t\t-------");
        h1.printDensity();
        System.out.println("-------");
        h2.printDensity();
        System.out.println("\t\t-------");
    }
}
