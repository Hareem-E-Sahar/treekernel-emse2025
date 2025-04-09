package com.dukesoftware.utils.math;

import java.util.Random;

public class Shuffler {

    public static void shuffle(int[] a) {
        Random random = new Random();
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
    }

    public static void shuffle(double[] a) {
        Random random = new Random();
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
    }

    public static void shuffle(long[] a) {
        Random random = new Random();
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            long swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
    }

    public static <T> void shuffle(T[] a) {
        Random random = new Random();
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T swap = a[i];
            a[i] = a[j];
            a[j] = swap;
        }
    }
}
