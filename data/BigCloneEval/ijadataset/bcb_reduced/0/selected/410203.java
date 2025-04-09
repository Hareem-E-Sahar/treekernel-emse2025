package com.casheen.projecteuler;

import org.apache.commons.lang.StringUtils;

public class FindTheLargestPalindromeProductOfTwo3DigitNumbers {

    protected int findTheLargestPalindromeProductOfTwo3DigitNumbers() {
        int palindrome = 0;
        int max3DigitNumber = 999;
        int min3DigitNumber = 900;
        int max = max3DigitNumber * max3DigitNumber;
        int min = min3DigitNumber * min3DigitNumber;
        for (int i = max; i >= min; i--) {
            if (isPalindrome(i)) {
                for (int j = min3DigitNumber; j <= max3DigitNumber; j++) {
                    if (i % j == 0 && i / j <= max3DigitNumber) {
                        return i;
                    }
                }
            }
        }
        return palindrome;
    }

    protected int findTheLargestPalindromeProductOfTwo3DigitNumbers2() {
        int palindrome = 0;
        for (int a = 9; a > 0; a--) {
            for (int b = 9; b >= 0; b--) {
                for (int c = 9; c >= 0; c--) {
                    palindrome = 100001 * a + 10010 * b + 1100 * c;
                    for (int k = 90; k >= 10; k--) {
                        if (palindrome % k == 0) {
                            int n = palindrome / k / 11;
                            if (n >= 100 && n <= 999) {
                                return palindrome;
                            }
                        }
                    }
                }
            }
        }
        return palindrome;
    }

    private boolean isPalindrome(int i) {
        String s = String.valueOf(i);
        return s.equals(StringUtils.reverse(s));
    }

    public static void main(String[] args) {
        FindTheLargestPalindromeProductOfTwo3DigitNumbers sample = new FindTheLargestPalindromeProductOfTwo3DigitNumbers();
        int palindrome = sample.findTheLargestPalindromeProductOfTwo3DigitNumbers2();
        System.out.println(palindrome);
    }
}
