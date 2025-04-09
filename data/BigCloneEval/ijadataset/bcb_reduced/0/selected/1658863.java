package chapter1;

import edu.princeton.cs.stdlib.StdOut;

/**
 * Andreas Bok Andersen Mandatory Assignment 1.1.19
 * 
 *
 */
public class RecursiveFibonacciHelper {

    private static long[] fibArray;

    public static long F(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return F(n - 1) + F(n - 2);
    }

    /**
	 * 
	 * @param n The number of Fibonacci numbers to be calculated 
	 */
    public static void runRecursiveBufferedFibonacci(int n) {
        fibArray = new long[n];
        for (int i = 1; i <= n; i++) {
            StdOut.println(fibRecursiveBuffered(i));
        }
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
