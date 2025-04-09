package uk.ac.imperial.ma.metric.mathematics.numerics;

import uk.ac.imperial.ma.metric.computerese.classic.Parser;
import uk.ac.imperial.ma.metric.computerese.classic.ComplexParser;

public class RootFinder {

    public RootFinder() {
    }

    public static final int REAL = 0;

    public static final int IMAGINARY = 1;

    public static double secantMethod(Parser funcParser, double seed1, double seed2, double precision) throws Exception {
        double x1 = seed1;
        double x2 = seed2;
        double y1;
        double y2;
        do {
            String[] vars = funcParser.getVariables();
            double[] x1s = { x1 };
            double[] x2s = { x2 };
            y1 = funcParser.getValue(x1s);
            y2 = funcParser.getValue(x2s);
            double x = (x1 * y2 - x2 * y1) / (y2 - y1);
            x1 = x2;
            x2 = x;
        } while (Math.abs(x2 - x1) > precision);
        return x2;
    }

    public static double secantMethod(Parser funcParser, String variable, double seed1, double seed2, double precision, double[] otherValues) throws Exception {
        double x1 = seed1;
        double x2 = seed2;
        double y1;
        double y2;
        do {
            String[] vars = funcParser.getVariables();
            double[] x1s = new double[vars.length];
            double[] x2s = new double[vars.length];
            int j = -1;
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].equals(variable)) {
                    x1s[i] = x1;
                    x2s[i] = x2;
                } else {
                    j++;
                    x1s[i] = otherValues[j];
                    x2s[i] = otherValues[j];
                }
            }
            y1 = funcParser.getValue(x1s);
            y2 = funcParser.getValue(x2s);
            double x = (x1 * y2 - x2 * y1) / (y2 - y1);
            x1 = x2;
            x2 = x;
        } while (Math.abs(x2 - x1) > precision);
        return x2;
    }

    public static double newtonRaphson(Parser funcParser, Parser derivParser, double seed, double precision) throws Exception {
        double x0;
        double x1 = seed;
        ;
        double y;
        double yPrime;
        do {
            x0 = x1;
            String[] vars = funcParser.getVariables();
            double[] xs = { x0 };
            y = funcParser.getValue(xs);
            yPrime = derivParser.getValue(xs);
            x1 = x0 - y / yPrime;
        } while (Math.abs(x1 - x0) > precision);
        return x1;
    }

    public static double newtonRaphson(Parser funcParser, Parser derivParser, String variable, double seed, double precision, double[] otherValues) throws Exception {
        double x0;
        double x1 = seed;
        double y;
        double yPrime;
        do {
            x0 = x1;
            String[] vars = funcParser.getVariables();
            double[] xs = new double[vars.length];
            int j = -1;
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].equals(variable)) {
                    xs[i] = x0;
                } else {
                    j++;
                    xs[i] = otherValues[j];
                }
            }
            y = funcParser.getValue(xs);
            yPrime = derivParser.getValue(xs);
            x1 = x0 - y / yPrime;
        } while (Math.abs(x1 - x0) > precision);
        return x1;
    }

    public static boolean rootIndex(Parser funcParser, double x1, double x2) throws Exception {
        double y1 = funcParser.getValue(x1);
        double y2 = funcParser.getValue(x2);
        return (y1 * y2 < 0);
    }

    public static int rootCount(Parser funcParser, double xMin, double xRange, double xStep) throws Exception {
        double x1 = xMin;
        int count = 0;
        int nSteps = (int) (xRange / xStep);
        for (int i = 0; i < nSteps; i++) {
            double x2 = x1 + xStep;
            count += (rootIndex(funcParser, x1, x2) ? 1 : 0);
            x1 = x2;
        }
        return count;
    }

    public static double[] intervalBisection(Parser funcParser, double seed1, double seed2, double precision) throws Exception {
        double x1 = seed1;
        double x2 = seed2;
        double x3;
        double y1;
        double y2;
        double y3;
        do {
            String[] vars = funcParser.getVariables();
            double[] x1s = { x1 };
            double[] x2s = { x2 };
            y1 = funcParser.getValue(x1s);
            y2 = funcParser.getValue(x2s);
            if (y1 * y2 > 0) throw new Exception(); else {
                x3 = (x1 + x2) / 2;
                double[] x3s = { x3 };
                y3 = funcParser.getValue(x3s);
                if (y1 * y3 > 0) x1 = x3; else x2 = x3;
            }
        } while (Math.abs(x2 - x1) > precision);
        double[] answer = { x1, x2 };
        return answer;
    }

    public static double[] intervalBisection(Parser funcParser, String variable, double seed1, double seed2, double precision, double[] otherValues) throws Exception {
        double x1 = seed1;
        double x2 = seed2;
        double x3;
        double y1;
        double y2;
        double y3;
        do {
            String[] vars = funcParser.getVariables();
            double[] x1s = new double[vars.length];
            double[] x2s = new double[vars.length];
            int j = -1;
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].equals(variable)) {
                    x1s[i] = x1;
                    x2s[i] = x2;
                } else {
                    j++;
                    x1s[i] = otherValues[j];
                    x2s[i] = otherValues[j];
                }
            }
            y1 = funcParser.getValue(x1s);
            y2 = funcParser.getValue(x2s);
            if (y1 * y2 > 0) throw new Exception(); else {
                x3 = (x1 + x2) / 2;
                double[] x3s = new double[vars.length];
                j = -1;
                for (int i = 0; i < vars.length; i++) {
                    if (vars[i].equals(variable)) {
                        x3s[i] = x3;
                    } else {
                        j++;
                        x3s[i] = otherValues[j];
                    }
                }
                y3 = funcParser.getValue(x3s);
                if (y1 * y3 > 0) x1 = x3; else x2 = x3;
            }
        } while (Math.abs(x2 - x1) > precision);
        double[] answer = { x1, x2 };
        return answer;
    }

    public static double[] intervalBisection(ComplexParser funcParser, int part, double seed1, double seed2, double precision, double otherValue) throws Exception {
        double x1 = seed1;
        double x2 = seed2;
        double x3;
        double w1;
        double w2;
        double w3;
        do {
            double[][] z1s = new double[1][2];
            double[][] z2s = new double[1][2];
            z1s[0][part] = x1;
            z2s[0][part] = x2;
            z1s[0][1 - part] = otherValue;
            z2s[0][1 - part] = otherValue;
            int j = -1;
            w1 = funcParser.getRealValue(z1s);
            w2 = funcParser.getRealValue(z2s);
            if (w1 * w2 > 0) throw new Exception(); else {
                x3 = (x1 + x2) / 2;
                double[][] z3s = new double[1][2];
                z3s[0][part] = x3;
                z3s[0][1 - part] = otherValue;
                w3 = funcParser.getRealValue(z3s);
                if (w1 * w3 > 0) x1 = x3; else x2 = x3;
            }
        } while (Math.abs(x2 - x1) > precision);
        double[] answer = { x1, x2 };
        return answer;
    }
}
