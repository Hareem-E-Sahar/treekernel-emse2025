package org.myjerry.maer.page2;

import java.math.BigInteger;
import org.myjerry.maer.util.PalindromeUtil;

/**
 * Problem 55 on Project Euler, http://projecteuler.net/index.php?section=problems&id=55
 * 
 * @author Sandeep Gupta
 * @since Jan 6, 2011
 */
public class Problem55 {

    private static final int eulerLimit = 10000;

    private static final int iterationLimit = 60;

    private static final Integer[] attempts = new Integer[eulerLimit + 1];

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        for (int i = 1; i <= eulerLimit; i++) {
            if (attempts[i] == null) {
                int iterations = tryIterations(i, BigInteger.valueOf(i), 1);
                if (iterations != -1) {
                    attempts[i] = iterations;
                }
            }
        }
        int count = 0;
        for (int i = 1; i <= eulerLimit; i++) {
            if (attempts[i] == null || attempts[i] == -1) {
                System.out.print(i + ", ");
                ++count;
            }
        }
        System.out.println("\n\nTotal numbers: " + count);
    }

    /**
	 * @param i
	 */
    private static int tryIterations(int number, BigInteger currentSum, int iterationNumber) {
        if (iterationNumber > iterationLimit) {
            return -1;
        }
        if (PalindromeUtil.checkPalindrome(currentSum) && currentSum.compareTo(BigInteger.valueOf(9)) > 0 && iterationNumber != 1) {
            return iterationNumber;
        }
        BigInteger reverse = PalindromeUtil.reverse(currentSum);
        BigInteger reverseSum = currentSum.add(reverse);
        if (PalindromeUtil.checkPalindrome(reverseSum) && currentSum.compareTo(BigInteger.valueOf(9)) > 0) {
            return iterationNumber;
        }
        int attempt = tryIterations(number, reverseSum, iterationNumber + 1);
        if (attempt != -1) {
            if (reverseSum.compareTo(BigInteger.valueOf(10001l)) < 0) {
                int ival = reverseSum.intValue();
                attempts[ival] = (attempt - iterationNumber);
            }
        }
        return attempt;
    }
}
