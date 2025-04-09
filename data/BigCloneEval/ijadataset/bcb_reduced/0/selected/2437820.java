package org.silentsquare.codejam.y2009.round1a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Random;

/**
 * INCORRECT
 * @author wf
 *
 */
public class CollectCard {

    private static final String PATH = "./src/org/silentsquare/codejam/y2009/round1a/";

    private BufferedReader in;

    private PrintWriter out;

    private int C;

    private int N;

    private double e = 0.00001;

    private int totalPacks;

    private int experiments;

    public void solveSmall() throws IOException {
        _solve("small");
    }

    public void solveLarge() throws IOException {
        _solve("large");
    }

    public void _solve(String name) throws IOException {
        System.out.println("Solving the " + name + " dataset ...");
        long begin = System.currentTimeMillis();
        in = new BufferedReader(new FileReader(PATH + "C-" + name + "-practice.in"));
        out = new PrintWriter(new BufferedWriter(new FileWriter(PATH + "C-" + name + "-practice.out")));
        int tests = Integer.parseInt(in.readLine().trim());
        for (int i = 0; i < tests; i++) {
            readTest();
            expect();
            out.println("Case #" + (i + 1) + ": " + (double) totalPacks / experiments);
        }
        in.close();
        out.close();
        System.out.println("Solving the " + name + " dataset: " + (System.currentTimeMillis() - begin) + "ms");
    }

    private void readTest() throws IOException {
        String line = in.readLine().trim();
        String[] ss = line.split(" ");
        C = Integer.parseInt(ss[0]);
        N = Integer.parseInt(ss[1]);
    }

    private BitSet collected;

    private BitSet pack;

    private void expect() {
        collected = new BitSet(C);
        pack = new BitSet(C);
        for (int i = 0; i < 1000000; i++) {
            collectAll();
        }
        double lastExpected;
        do {
            lastExpected = (double) totalPacks / experiments;
            for (int i = 0; i < 100; i++) {
                collectAll();
            }
        } while (Math.abs((double) totalPacks / experiments - lastExpected) >= e);
    }

    private void collectAll() {
        collected.set(0, C, false);
        int packs = 0;
        do {
            generateRandomPack(pack);
            packs++;
            collected.or(pack);
        } while (collected.cardinality() < C);
        totalPacks += packs;
        experiments++;
    }

    private Random random = new Random();

    private void generateRandomPack(BitSet pack) {
        pack.set(0, C, false);
        for (int i = 0, needed = N; i < C; i++) {
            if (random.nextDouble() < (double) needed / (C - i)) {
                pack.set(i);
                needed--;
            }
        }
    }

    private void generateRandomPack2(BitSet pack) {
        pack.set(0, N, true);
        pack.set(N, C, false);
        for (int i = 0; i < N; i++) {
            int j = i + random.nextInt(C - i);
            if (i != j) {
                boolean temp = pack.get(i);
                pack.set(i, pack.get(j));
                pack.set(j, temp);
            }
        }
    }

    /**
	 * @param args
	 * @throws IOException 
	 */
    public static void main(String[] args) throws IOException {
        new CollectCard().solveSmall();
    }
}
