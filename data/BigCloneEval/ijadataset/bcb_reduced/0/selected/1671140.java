package org.jfonia.util;

/**
 * @author wijnand.schepens@gmail.com
 *
 */
public class Math {

    /**
	 * Calculates the greatest common divisor of two positive integers.
	 * Using Euler's algorithm.
	 * 
	 * @param a integer strictly greater than 0
	 * @param b integer strictly greater than 0
	 * @return the greatest common divisor of a and b, i.e. the largest integer k for which a%k==0 and b%k==0
	 */
    public static int calculateGCD(int a, int b) {
        while (b != 0) {
            int t = a;
            a = b;
            b = t % b;
        }
        return a;
    }
}
