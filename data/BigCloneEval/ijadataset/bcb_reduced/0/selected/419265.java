package org.silentsquare.codejam.y2008.round2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Algorithm
 * 
 * @author wjfang
 *
 */
public class TriangleAreas2 {

    private static final String PATH = "./src/org/silentsquare/codejam/y2008/round2/";

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
        in = new Scanner(new File(PATH + "B-" + name + "-practice.in"));
        out = new PrintWriter(new BufferedWriter(new FileWriter(PATH + "B-" + name + "-practice.out")));
        int tests = in.nextInt();
        for (int i = 0; i < tests; i++) {
            readCase();
            String answer = solveCase();
            out.printf("Case #%d: %s\n", (i + 1), answer);
            System.out.printf("Case #%d: %s\n", (i + 1), answer);
        }
        in.close();
        out.close();
        System.out.println("Solving the " + name + " dataset: " + (System.currentTimeMillis() - begin) + "ms");
    }

    int n, m, a;

    private void readCase() {
        n = in.nextInt();
        m = in.nextInt();
        a = in.nextInt();
    }

    class Coordinate {

        int x, y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private String solveCase() {
        if (a > n * m) return "IMPOSSIBLE";
        for (int i = 0; i <= n; i++) for (int j = 0; j <= m; j++) {
            Coordinate co = search(i, j, n, m, i * j + a);
            if (co != null) return "0 0 " + co.x + " " + j + " " + i + " " + co.y;
        }
        return "IMPOSSIBLE2";
    }

    private Coordinate search(int x1, int y1, int x2, int y2, int key) {
        if (x2 < x1 || y2 < y1 || x1 * y1 > key || x2 * y2 < key) return null;
        if (x1 * y1 == key) return new Coordinate(x1, y1);
        if (x2 * y2 == key) return new Coordinate(x2, y2);
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;
        if (x * y == key) return new Coordinate(x, y); else if (x * y > key) return search(x1, y1, x, y, key); else {
            if (x + 1 <= n) {
                Coordinate co = search(x + 1, y1, x2, y2, key);
                if (co != null) return co;
            }
            if (y + 1 <= m) return search(x1, y + 1, x, y2, key);
        }
        return null;
    }

    /**
	 * @param args
	 * @throws IOException 
	 */
    public static void main(String[] args) throws IOException {
        new TriangleAreas2().solveSmall();
        new TriangleAreas2().solveLarge();
    }
}
