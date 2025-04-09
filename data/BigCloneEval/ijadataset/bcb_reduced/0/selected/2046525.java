package GA;

import java.io.*;
import java.util.*;

public class EA {

    int POP_SIZE = 100;

    int CHROMOSOME_SIZE = 16;

    float pop[][] = new float[POP_SIZE][CHROMOSOME_SIZE];

    float fitness[] = new float[POP_SIZE];

    float bestPopFitness;

    Random r;

    public EA(long seed) {
        r = new Random(seed);
        initPop();
    }

    public int tournament(int tSize) {
        int i, j;
        float bestFit = 0;
        int best = 0;
        for (i = 0; i < tSize; i++) {
            j = r.nextInt(POP_SIZE);
            if (fitness[j] > bestFit) {
                bestFit = fitness[j];
                best = j;
            }
        }
        return (best);
    }

    public void crossover(int c1, int c2, float[] kid) {
        int i, cut1;
        cut1 = 1 + r.nextInt(CHROMOSOME_SIZE - 1);
        for (i = 0; i < cut1; i++) kid[i] = pop[c1][i];
        for (i = cut1; i < CHROMOSOME_SIZE; i++) kid[i] = pop[c2][i];
    }

    public void mutate(float[] kid) {
        for (int i = 0; i < CHROMOSOME_SIZE; i++) if (r.nextInt(CHROMOSOME_SIZE) == 1) kid[i] = r.nextFloat();
    }

    public void insert(float[] kid, float val) {
        int worst;
        float worstFit;
        worstFit = fitness[0];
        worst = 0;
        for (int i = 0; i < POP_SIZE; i++) {
            if (worstFit > fitness[i]) {
                worst = i;
                worstFit = fitness[i];
            }
        }
        if (val > worstFit) {
            for (int i = 0; i < CHROMOSOME_SIZE; i++) pop[worst][i] = kid[i];
            fitness[worst] = val;
        }
    }

    public float evaluate(float[] chromosome) {
        float value = 0;
        for (int i = 0; i < CHROMOSOME_SIZE; i++) value += chromosome[i];
        return value;
    }

    public void run(int gensLimit) {
        int c1, c2;
        float[] kid = new float[CHROMOSOME_SIZE];
        float val;
        for (int i = 0; i < gensLimit; i++) {
            c1 = tournament(2);
            c2 = tournament(2);
            crossover(c1, c2, kid);
            mutate(kid);
            val = evaluate(kid);
            if (val > bestPopFitness) bestPopFitness = val;
            insert(kid, val);
            if (i % 100 == 0) System.out.println("# " + i + " " + bestPopFitness);
        }
    }

    public void reportResult() {
        int best;
        float bestFit;
        bestFit = 0;
        best = 0;
        for (int i = 0; i < POP_SIZE; i++) {
            if (fitness[i] > bestFit) {
                bestFit = fitness[i];
                best = i;
            }
        }
        for (int i = 0; i < CHROMOSOME_SIZE; i++) System.out.println(i + " " + pop[best][i]);
        System.out.println("# Best Value = " + bestFit + " at " + best);
    }

    public void initPop() {
        for (int c = 0; c < POP_SIZE; c++) {
            for (int g = 0; g < CHROMOSOME_SIZE; g++) {
                pop[c][g] = r.nextFloat();
            }
            fitness[c] = evaluate(pop[c]);
        }
    }

    public static void main(String[] args) {
        long seed;
        int generations = 100;
        if (args.length < 1) {
            System.out.print("needs a seed value, ");
            System.out.println("and maybe a generations limit");
            System.exit(0);
        }
        seed = Long.parseLong(args[0]);
        if (args.length > 1) {
            generations = Integer.parseInt(args[1]);
        }
        EA myEA = new EA(seed);
        myEA.run(generations);
        myEA.reportResult();
    }
}
