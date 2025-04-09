package net.maizegenetics.jGLiM;

import java.util.Random;

public class BasicShuffler {

    static long seed = 111;

    static Random randomSource = new Random();

    private BasicShuffler() {
    }

    public static synchronized <T> void shuffle(T[] anArray) {
        int n = anArray.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = randomSource.nextInt(i + 1);
            T temp = anArray[j];
            anArray[j] = anArray[i];
            anArray[i] = temp;
        }
    }

    public static synchronized void shuffle(int[] anArray) {
        int n = anArray.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = randomSource.nextInt(i + 1);
            int temp = anArray[j];
            anArray[j] = anArray[i];
            anArray[i] = temp;
        }
    }

    public static synchronized void shuffle(double[] anArray) {
        int n = anArray.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = randomSource.nextInt(i + 1);
            double temp = anArray[j];
            anArray[j] = anArray[i];
            anArray[i] = temp;
        }
    }

    public static synchronized void shuffle(float[] anArray) {
        int n = anArray.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = randomSource.nextInt(i + 1);
            float temp = anArray[j];
            anArray[j] = anArray[i];
            anArray[i] = temp;
        }
    }

    public static synchronized void shuffle(char[] anArray) {
        int n = anArray.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = randomSource.nextInt(i + 1);
            char temp = anArray[j];
            anArray[j] = anArray[i];
            anArray[i] = temp;
        }
    }

    public static synchronized void shuffle(byte[] anArray) {
        int n = anArray.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = randomSource.nextInt(i + 1);
            byte temp = anArray[j];
            anArray[j] = anArray[i];
            anArray[i] = temp;
        }
    }

    public static synchronized void setSeed(long seed) {
        BasicShuffler.seed = seed;
        BasicShuffler.reset();
    }

    public static synchronized void reset() {
        BasicShuffler.randomSource = new Random(seed);
    }
}
