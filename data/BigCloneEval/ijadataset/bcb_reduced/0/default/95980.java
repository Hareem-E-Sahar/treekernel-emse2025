import java.math.BigInteger;

public class problem55 {

    public static boolean isPalindrome(BigInteger number) {
        return number.toString().equals(new StringBuffer(number.toString()).reverse().toString());
    }

    public static boolean isLychrel(BigInteger number) {
        BigInteger numberTemp = number;
        for (int i = 0; i < 50; i++) {
            BigInteger sum = numberTemp.add(new BigInteger(new StringBuffer(numberTemp.toString()).reverse().toString()));
            if (isPalindrome(sum)) {
                System.out.println(number + " " + sum);
                return false;
            } else {
                numberTemp = sum;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int total = 0;
        for (int i = 0; i < 10000; i++) {
            if (isLychrel(BigInteger.valueOf(i))) {
                total++;
            }
        }
        System.out.println(total);
    }
}
