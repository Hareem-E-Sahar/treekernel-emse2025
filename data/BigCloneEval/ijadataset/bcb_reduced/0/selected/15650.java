package org.susan.java.string;

public class Palindrome {

    public static boolean isPalindrome(String stringToTest) {
        String workingCopyString = removeJunk(stringToTest);
        String reversedCopyString = reverse(stringToTest);
        return reversedCopyString.equalsIgnoreCase(workingCopyString);
    }

    protected static String removeJunk(String string) {
        int i, len = string.length();
        StringBuilder destStringBuilder = new StringBuilder(string);
        char tempChar;
        for (i = (len - 1); i >= 0; i--) {
            tempChar = string.charAt(i);
            if (Character.isLetterOrDigit(tempChar)) {
                destStringBuilder.append(tempChar);
            }
        }
        return destStringBuilder.toString();
    }

    protected static String reverse(String string) {
        StringBuilder builder = new StringBuilder(string);
        return builder.reverse().toString();
    }

    public static void main(String args[]) {
        String strOne = "ABCDEEDCBA";
        String strTwo = "Madam, I'm Adam.";
        System.out.println("strOne is palindrome:" + isPalindrome(strOne));
        System.out.println("strTwo is palindrome:" + isPalindrome(strTwo));
    }
}
