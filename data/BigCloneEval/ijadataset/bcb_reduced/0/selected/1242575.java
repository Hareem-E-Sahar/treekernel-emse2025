package com.cdai.studio.math;

public class Sqrt {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        float num = 2;
        float e = 0.01F;
        sqrt(num, e);
        sqrtNewton(num, e);
        num = 2;
        e = 0.0001F;
        sqrt(num, e);
        sqrtNewton(num, e);
        num = 2;
        e = 0.00001F;
        sqrt(num, e);
        sqrtNewton(num, e);
    }

    private static float sqrt(float num, float e) {
        float low = 0F;
        float high = num;
        float guess, e0;
        int count = 0;
        do {
            guess = (low + high) / 2;
            if (guess * guess > num) {
                high = guess;
                e0 = guess * guess - num;
            } else {
                low = guess;
                e0 = num - guess * guess;
            }
            count++;
            System.out.printf("Try %f, e: %f\n", guess, e0);
        } while (e0 > e);
        System.out.printf("Try %d times, result: %f\n", count, guess);
        return guess;
    }

    private static float sqrtNewton(float num, float e) {
        float guess = num / 2;
        float e0;
        int count = 0;
        do {
            guess = (guess + num / guess) / 2;
            e0 = guess * guess - num;
            count++;
            System.out.printf("Try %f, e: %f\n", guess, e0);
        } while (e0 > e);
        System.out.printf("Try %d times, result: %f\n", count, guess);
        return guess;
    }
}
