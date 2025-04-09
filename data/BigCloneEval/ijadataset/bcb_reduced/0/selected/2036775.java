package weekthree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

;

public class FifteenFourteen {

    /**
	 * (Palindromes) A palindrome is a string that is spelled the same way forwards 
	 * and backwards. Some examples of palindromes are "radar," "able was i ere i 
	 * saw elba" and (if spaces are ignored) "a man a plan a canal panama." Write a 
	 * recursive method testPalindrome that returns boolean value true if the string 
	 * stored in the array is a palindrome and false otherwise. The method should 
	 * ignore spaces and punctuation in the string.
	 */
    public static boolean isPalindrome(String word) {
        boolean result = false;
        if (word.length() <= 1) result = true; else if (word.charAt(0) == word.charAt(word.length() - 1)) result = isPalindrome(word.substring(1, word.length() - 1));
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        FifteenEight.blank("Is \"radar\" a palindrome, True or False? " + isPalindrome("radar"));
        FifteenEight.blank();
        FifteenEight.blank("Is \"dogs and cats\" a palindrome, True or False? " + isPalindrome("dogs and cats"));
        FifteenEight.blank();
        FifteenEight.blank("Please enter a word or phrase to check for palindromishness: ");
        String temp = (in.readLine());
        if (isPalindrome(temp) == false) FifteenEight.blank("That is not a palindrome."); else FifteenEight.blank("Yes! That is a palindrome!");
        FifteenEight.blank();
        FifteenEight.blank("Thank you for playing! eop.");
    }
}
