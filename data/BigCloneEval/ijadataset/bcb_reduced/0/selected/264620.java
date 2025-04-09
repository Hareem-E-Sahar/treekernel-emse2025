package org.myjerry.maer.page1;

import org.myjerry.maer.util.PalindromeUtil;

/**
 * Problem 36 on Project Euler, http://projecteuler.net/index.php?section=problems&id=36
 *
 * @author Sandeep Gupta
 * @since Jan 18, 2011
 */
public class Problem36 {

    public static void main(String[] args) {
        long sum = 0;
        for (int number = 1; number < 1000000; number++) {
            if (PalindromeUtil.checkPalindrome(number)) {
                String binary = Integer.toString(number, 2);
                if (PalindromeUtil.checkPalindrome(binary)) {
                    System.out.println(number + "\t" + binary);
                    sum += number;
                }
            }
        }
        System.out.println("Sum of all numbers: " + sum);
    }
}
