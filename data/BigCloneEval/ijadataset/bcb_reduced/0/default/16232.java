public class Algorithms {

    /**
     * Indicates if a given string is a palindrome
     * @param str String to detect if it's a palindrome
     * @return Boolean indicating if the string is a palindrome
     */
    public static boolean isPalindrome(String str) {
        int begin = 0;
        int end = str.length() - 1;
        if (str == null) return false;
        while (begin < (int) (str.length() / 2)) {
            if (str.charAt(begin) != str.charAt(end)) return false; else {
                begin++;
                end--;
            }
        }
        return true;
    }

    /**
     * Reverses a string
     * @param str String to reverse
     * @return Reversed representation of a string
     */
    public static String reverseString(String str) {
        if (str == null) return null;
        char[] newstr = new char[str.length()];
        for (int x = 0, y = newstr.length - 1; x < str.length(); x++, y--) newstr[x] = str.charAt(y);
        return new String(newstr);
    }
}
