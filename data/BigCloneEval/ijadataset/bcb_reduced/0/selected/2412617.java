package org.silentsquare.codejam.y2008.qualification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Algorithm
 * 
 * 
 * @author wjfang
 *
 */
public class FlySwatter3 {

    private static final String PATH = "./src/org/silentsquare/codejam/y2008/qualification/";

    private Scanner in;

    private PrintWriter out;

    public void solveSmall() throws IOException {
        _solve("small");
    }

    public void solveLarge() throws IOException {
        _solve("large");
    }

    public void _solve(String name) throws IOException {
        System.out.println("Solving the " + name + " dataset ...");
        long begin = System.currentTimeMillis();
        in = new Scanner(new File(PATH + "C-" + name + "-practice.in"));
        out = new PrintWriter(new BufferedWriter(new FileWriter(PATH + "C-" + name + "-practice.out")));
        int tests = in.nextInt();
        for (int i = 0; i < tests; i++) {
            readTestCase();
            double p = solve();
            out.printf("Case #%d: %f\n", (i + 1), p);
            System.out.printf("Case #%d: %f\n", (i + 1), p);
        }
        in.close();
        out.close();
        System.out.println("Solving the " + name + " dataset: " + (System.currentTimeMillis() - begin) + "ms");
    }

    double f, R, t, r, g;

    private void readTestCase() throws IOException {
        f = in.nextDouble();
        R = in.nextDouble();
        t = in.nextDouble();
        r = in.nextDouble();
        g = in.nextDouble();
    }

    private double solve() {
        double quarter = Math.PI * R * R * 0.25;
        double safe = 0;
        for (double x1 = r + f; x1 < R - t - f; x1 += g + 2 * r) {
            for (double y1 = r + f; y1 < R - t - f; y1 += g + 2 * r) {
                double x2 = x1 + g - 2 * f;
                double y2 = y1 + g - 2 * f;
                if (x1 >= x2 || y1 >= y2) continue;
                safe += intersection2(x1, y1, x2, y2);
            }
        }
        return 1.0 - safe / quarter;
    }

    private double intersection2(double x1, double y1, double x2, double y2) {
        double a = R - t - f;
        double b = g - 2 * f;
        if (x1 * x1 + y1 * y1 > a * a) {
            return 0;
        } else if (x2 * x2 + y2 * y2 <= a * a) {
            return b * b;
        } else {
            if (x1 * x1 + y2 * y2 >= a * a && x2 * x2 + y1 * y1 >= a * a) {
                double x3 = Math.sqrt(a * a - y1 * y1);
                double y3 = Math.sqrt(a * a - x1 * x1);
                double c = Math.acos((2 * a * a - (x3 - x1) * (x3 - x1) - (y1 - y3) * (y1 - y3)) / (2 * a * a));
                return 0.5 * (y3 - y1) * (x3 - x1) + 0.5 * a * a * (c - Math.sin(c));
            } else if (x1 * x1 + y2 * y2 < a * a && x2 * x2 + y1 * y1 >= a * a) {
                double x3 = Math.sqrt(a * a - y2 * y2);
                double x4 = Math.sqrt(a * a - y1 * y1);
                double c = Math.acos((2 * a * a - (x3 - x4) * (x3 - x4) - b * b) / (2 * a * a));
                return 0.5 * (x4 - x1) * b + 0.5 * (x3 - x1) * b + 0.5 * a * a * (c - Math.sin(c));
            } else if (x1 * x1 + y2 * y2 >= a * a && x2 * x2 + y1 * y1 < a * a) {
                double y3 = Math.sqrt(a * a - x2 * x2);
                double y4 = Math.sqrt(a * a - x1 * x1);
                double c = Math.acos((2 * a * a - (y3 - y4) * (y3 - y4) - b * b) / (2 * a * a));
                return 0.5 * (y4 - y1) * b + 0.5 * (y3 - y1) * b + 0.5 * a * a * (c - Math.sin(c));
            } else {
                double x3 = Math.sqrt(a * a - y2 * y2);
                double y3 = Math.sqrt(a * a - x2 * x2);
                double c = Math.acos((2 * a * a - (x3 - x2) * (x3 - x2) - (y2 - y3) * (y2 - y3)) / (2 * a * a));
                return 0.5 * b * b + 0.5 * (y3 - y1) * b + 0.5 * (x3 - x1) * (y2 - y3) + 0.5 * a * a * (c - Math.sin(c));
            }
        }
    }

    double intersection(double x1, double y1, double x2, double y2) {
        if (x1 * x1 + y1 * y1 > (R - t - f) * (R - t - f)) {
            return 0;
        }
        if (x2 * x2 + y2 * y2 < (R - t - f) * (R - t - f)) {
            return (x2 - x1) * (y2 - y1);
        }
        if ((x2 - x1) * (y2 - y1) < (R - t - f) * (R - t - f) * 0.000000000001) {
            return (x2 - x1) * (y2 - y1) / 2;
        }
        double mx = (x1 + x2) / 2;
        double my = (y1 + y2) / 2;
        return intersection(x1, y1, mx, my) + intersection(mx, y1, x2, my) + intersection(x1, my, mx, y2) + intersection(mx, my, x2, y2);
    }

    /**
	 * @param args
	 * @throws IOException 
	 */
    public static void main(String[] args) throws IOException {
        new FlySwatter3().solveSmall();
        new FlySwatter3().solveLarge();
    }
}
