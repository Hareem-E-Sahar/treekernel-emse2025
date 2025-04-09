package net.sf.appomatox.bibliothek.algebra;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import jscl.text.ParseException;
import net.sf.appomatox.bibliothek.analysis.Expression;

/**
 * 
 * @author http://www.torsten-horn.de/techdocs/java-approximationsfunktionen.htm
 */
public class Approximationsfunktionen {

    private static final int SP = 4;

    /**
	 * Lineare Regression.
	 * <pre>y = a + b * x</pre>
	 * @param xyArr
	 * @return
	 */
    public RegressionResult calculateLinearRegression(Collection<Point2D> xyArr) {
        if (xyArr == null || xyArr.size() < 1) {
            return null;
        }
        int n = xyArr.size();
        double xs = 0;
        double ys = 0;
        double xqs = 0;
        double yqs = 0;
        double xys = 0;
        for (Point2D pnk : xyArr) {
            xs += pnk.getX();
            ys += pnk.getY();
            xqs += pnk.getX() * pnk.getX();
            yqs += pnk.getY() * pnk.getY();
            xys += pnk.getX() * pnk.getY();
        }
        RegressionResult abr = new RegressionResult();
        double xm = xs / n;
        double ym = ys / n;
        double xv = xqs / n - (xm * xm);
        double yv = yqs / n - (ym * ym);
        double kv = xys / n - (xm * ym);
        abr.bestimmtheitsmass = Math.min((kv * kv) / (xv * yv), 1);
        abr.b = kv / xv;
        abr.a = ym - abr.b * xm;
        try {
            abr.expression = Expression.getInstance(roundSignificant(abr.a, SP) + " + " + roundSignificant(abr.b, SP) + " * x");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        abr.approxFunction = new ApproxFunction() {

            public double execute(double a, double b, double x) {
                return a + b * x;
            }
        };
        return abr;
    }

    /**
	 * Potenzielle Regression
	 * <pre>y = a * x^b</pre>
	 * Regression �ber: <pre>ln(y) = ln(a) + b * ln(x)</pre>
	 * @param xyArr
	 * @return
	 */
    public RegressionResult calculatePowerRegression(Collection<Point2D> xyArr) {
        if (xyArr == null || xyArr.size() < 1) {
            return null;
        }
        Collection<Point2D> xyArrConv = new ArrayList<Point2D>();
        for (Point2D pnk : xyArr) {
            if (pnk.getX() <= 0 || pnk.getY() <= 0) {
                return null;
            }
            Point2D pnkConv = new Point2D.Double(Math.log(pnk.getX()), Math.log(pnk.getY()));
            xyArrConv.add(pnkConv);
        }
        RegressionResult abr = calculateLinearRegression(xyArrConv);
        if (abr == null) {
            return null;
        }
        abr.a = Math.exp(abr.a);
        try {
            abr.expression = Expression.getInstance(roundSignificant(abr.a, SP) + " * x ^ " + roundSignificant(abr.b, SP));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        abr.approxFunction = new ApproxFunction() {

            public double execute(double a, double b, double x) {
                return a * Math.pow(x, b);
            }
        };
        return abr;
    }

    /**
	 * Logarithmische Regression.
	 * <pre>y = a + b * ln(x)</pre>
	 * @param xyArr
	 * @return
	 */
    public RegressionResult calculateLogarithmicRegression(Collection<Point2D> xyArr) {
        if (xyArr == null || xyArr.size() < 1) {
            return null;
        }
        Collection<Point2D> xyArrConv = new ArrayList<Point2D>();
        for (Point2D pnk : xyArr) {
            if (pnk.getX() <= 0) {
                return null;
            }
            Point2D pnkConv = new Point2D.Double(Math.log(pnk.getX()), pnk.getY());
            xyArrConv.add(pnkConv);
        }
        RegressionResult abr = calculateLinearRegression(xyArrConv);
        if (abr == null) {
            return null;
        }
        try {
            abr.expression = Expression.getInstance(roundSignificant(abr.a, SP) + " + " + roundSignificant(abr.b, SP) + " * log(x)");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        abr.approxFunction = new ApproxFunction() {

            public double execute(double a, double b, double x) {
                return a + b * Math.log(x);
            }
        };
        return abr;
    }

    /**
	 * Exponentielle Regression
	 * <pre>y = a * e^(b * x)</pre>
	 * Regression �ber: <pre>ln(y) = ln(a) + b * x</pre>
	 * @param xyArr
	 * @return
	 */
    public RegressionResult calculateExponentialRegression(Collection<Point2D> xyArr) {
        if (xyArr == null || xyArr.size() < 1) {
            return null;
        }
        Collection<Point2D> xyArrConv = new ArrayList<Point2D>();
        for (Point2D pnk : xyArr) {
            if (pnk.getY() <= 0) {
                return null;
            }
            Point2D pnkConv = new Point2D.Double(pnk.getX(), Math.log(pnk.getY()));
            xyArrConv.add(pnkConv);
        }
        RegressionResult abr = calculateLinearRegression(xyArrConv);
        if (abr == null) {
            return null;
        }
        abr.a = Math.exp(abr.a);
        try {
            abr.expression = Expression.getInstance(roundSignificant(abr.a, SP) + " * e ^ (" + roundSignificant(abr.b, SP) + " * x)");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        abr.approxFunction = new ApproxFunction() {

            public double execute(double a, double b, double x) {
                return a * Math.exp(b * x);
            }
        };
        return abr;
    }

    /**
	 * Gespiegelte und verschobene exponentielle Regression
	 * <pre>y = a * ( 1 - e^(-b * x) )</pre>
	 * Approximationsfunktion beginnt bei 0 und strebt gegen den Grenzwert "limit".
	 * Falls "limit" nicht bekannt ist: Iterativ naehern.
	 * @param xyArr
	 * @param limit
	 * @return
	 */
    public RegressionResult calculateOneMinusExponentialRegression(Collection<Point2D> xyArr, double limit) {
        Collection<Point2D> xyArrTest = new ArrayList<Point2D>();
        for (Point2D pnk : xyArr) {
            Point2D pnkConv = new Point2D.Double(-pnk.getX(), limit - pnk.getY());
            xyArrTest.add(pnkConv);
        }
        RegressionResult abr = calculateExponentialRegression(xyArrTest);
        if (abr == null) {
            return null;
        }
        abr.a = limit;
        return abr;
    }

    /**
	 * Gespiegelte und verschobene exponentielle Regression
	 * <pre> y = a * ( 1 - e^(-b * x) )</pre>
	 * Approximationsfunktion beginnt bei 0 und strebt gegen den Grenzwert "limit".
	 * @param xyArr
	 * @return
	 */
    public RegressionResult calculateOneMinusExponentialRegression(Collection<Point2D> xyArr) {
        final double INCR_FACTOR = 1.001;
        double yMax = 0;
        if (xyArr == null || xyArr.size() < 1) {
            return null;
        }
        for (Point2D pnk : xyArr) {
            yMax = Math.max(yMax, pnk.getX());
        }
        double lim = searchMaximumFromFunctionFromX(yMax, INCR_FACTOR, xyArr, new FunctionFromX() {

            public double execute(double x, Collection<Point2D> helpObject) {
                RegressionResult abr = calculateOneMinusExponentialRegression(helpObject, x);
                if (abr == null) {
                    return 0;
                }
                return abr.bestimmtheitsmass;
            }
        });
        RegressionResult abr = calculateOneMinusExponentialRegression(xyArr, lim);
        if (abr == null) {
            return null;
        }
        try {
            abr.expression = Expression.getInstance(roundSignificant(abr.a, SP) + " * ( 1 - e ^ (-" + roundSignificant(abr.b, SP) + " * x) )");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        abr.approxFunction = new ApproxFunction() {

            public double execute(double a, double b, double x) {
                return a * (1 - Math.exp(-b * x));
            }
        };
        return abr;
    }

    /**
	 * Suche den x-Wert fuer den die "FunctionFromX" ein Maximum hat
	 * @param xStart
	 * @param incrFactor
	 * @param helpObject
	 * @param functionFromX
	 * @return
	 */
    public double searchMaximumFromFunctionFromX(double xStart, double incrFactor, Collection<Point2D> helpObject, FunctionFromX functionFromX) {
        double x1, x2, xTest;
        double y1, y2, yTest;
        x1 = x2 = xTest = xStart;
        y1 = y2 = yTest = functionFromX.execute(xTest, helpObject);
        for (int i = 0; i < 1000000; i++) {
            xTest *= incrFactor;
            yTest = functionFromX.execute(xTest, helpObject);
            if (yTest < y1) {
                x1 = xTest;
                y1 = yTest;
                break;
            }
            x2 = x1;
            x1 = xTest;
            y2 = y1;
            y1 = yTest;
        }
        for (int i = 0; i < 1000000; i++) {
            xTest = (x1 + x2) / 2;
            yTest = functionFromX.execute(xTest, helpObject);
            if (y2 >= y1) {
                x1 = xTest;
                y1 = yTest;
            } else {
                x2 = xTest;
                y2 = yTest;
            }
            if (i > 10 && Math.abs(y2 - y1) < 1.0E-12) {
                break;
            }
        }
        return xTest;
    }

    private double roundSignificant(double d, int significantPrecision) {
        if (d == 0 || significantPrecision < 1 || significantPrecision > 14) {
            return d;
        }
        double mul10 = 1;
        double minVal = Math.pow(10, significantPrecision - 1);
        while (Math.abs(d) < minVal) {
            mul10 *= 10;
            d *= 10;
        }
        return Math.round(d) / mul10;
    }

    public static class RegressionResult {

        double a;

        double b;

        public double bestimmtheitsmass;

        public Expression expression;

        ApproxFunction approxFunction;
    }

    interface ApproxFunction {

        double execute(double a, double b, double x);
    }

    interface FunctionFromX {

        double execute(double x, Collection<Point2D> helpObject);
    }
}
