package chapter1;

import edu.princeton.cs.stdlib.StdOut;

public class Exercise_1_1_19 {

    private static long[] fibArray;

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        int fibnums = 30;
        fibArray = new long[fibnums];
        for (int n = 1; n < fibnums; n++) {
            long t1 = System.nanoTime();
            long f = F(n);
            long t2 = System.nanoTime();
            StdOut.println("F1: " + n + ";" + ((t2 - t1)) + ";");
            t1 = System.nanoTime();
            long g = fibRecursiveBuffered(n);
            t2 = System.nanoTime();
            StdOut.println("F2: " + n + ";" + ((t2 - t1)) + ";");
        }
    }

    /***
	 * Calculates Fibonacci numbers using recursion
	 * @param n n'th Fibonacci number
	 * @return 
	 */
    public static long F(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return F(n - 1) + F(n - 2);
    }

    /**
	 * Calcutes a Fibonacci number using recursion and an array for 
	 * already calculated numbers 
	 * @param n
	 * @return Return the i'th fibonacci number 
	 */
    public static long fibRecursiveBuffered(int n) {
        if (n <= 2) {
            fibArray[n - 1] = 1;
            return 1;
        } else {
            fibArray[n - 1] = fibRecursiveBuffered(n - 1);
            return fibArray[n - 2] + fibArray[n - 1];
        }
    }
}
