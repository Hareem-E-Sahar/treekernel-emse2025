public class problem4 {

    public static boolean isPalindrome(String numberString) {
        if (numberString.length() % 2 == 0) {
            String firstHalf = numberString.substring(0, numberString.length() / 2);
            String secondHalf = numberString.substring(numberString.length() / 2);
            String secondHalfReverse = (new StringBuffer(secondHalf)).reverse().toString();
            return firstHalf.equals(secondHalfReverse);
        }
        return false;
    }

    public static void main(String[] args) {
        int largestPalindrome = 0;
        for (int i = 100; i < 1000; i++) {
            for (int j = 100; j < 1000; j++) {
                int product = i * j;
                if (isPalindrome(String.valueOf(product))) {
                    System.out.println(product);
                    if (product > largestPalindrome) {
                        largestPalindrome = product;
                    }
                }
            }
        }
        System.out.println(largestPalindrome);
    }
}
