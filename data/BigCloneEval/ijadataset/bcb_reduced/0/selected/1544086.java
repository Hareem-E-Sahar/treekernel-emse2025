package edu.emory.mathcs.csparsej.tdouble;

import java.util.Random;

/**
 * Random permutation.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class Dcs_randperm {

    /**
     * Returns a random permutation vector, the identity perm, or p = n-1:-1:0.
     * seed = -1 means p = n-1:-1:0. seed = 0 means p = identity. otherwise p =
     * random permutation.
     * 
     * @param n
     *            length of p
     * @param seed
     *            0: natural, -1: reverse, random p oterwise
     * @return p, null on error or for natural order
     */
    public static int[] cs_randperm(int n, int seed) {
        int p[], k, j, t;
        if (seed == 0) return (null);
        p = new int[n];
        for (k = 0; k < n; k++) p[k] = n - k - 1;
        if (seed == -1) return (p);
        Random r = new Random(seed);
        for (k = 0; k < n; k++) {
            j = k + r.nextInt(n - k);
            t = p[j];
            p[j] = p[k];
            p[k] = t;
        }
        return (p);
    }
}
