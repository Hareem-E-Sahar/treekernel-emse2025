package org.myjerry.maer.util;

import java.math.BigInteger;

/**
 * 
 * @author Sandeep Gupta
 * @since Jan 6, 2011
 */
public class PalindromeUtil {

    public static boolean checkPalindrome(long number) {
        String num = String.valueOf(number);
        return checkPalindrome(num);
    }

    public static long reverseSum(long number) {
        String num = String.valueOf(number);
        String reverse = new StringBuilder(num).reverse().toString();
        long rev = Long.parseLong(reverse);
        return number + rev;
    }

    public static boolean checkPalindrome(String string) {
        if (string == null) {
            return false;
        }
        String reverse = new StringBuilder(string).reverse().toString();
        if (string.equals(reverse)) {
            return true;
        }
        return false;
    }

    /**
	 * @param currentSum
	 * @return
	 */
    public static boolean checkPalindrome(BigInteger currentSum) {
        if (currentSum == null) {
            return false;
        }
        return checkPalindrome(currentSum.toString());
    }

    /**
	 * @param currentSum
	 * @return
	 */
    public static BigInteger reverse(BigInteger currentSum) {
        String num = currentSum.toString();
        String rev = new StringBuilder(num).reverse().toString();
        BigInteger revNum = new BigInteger(rev);
        return revNum;
    }
}
