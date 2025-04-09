import java.util.HashMap;

class P353_39643 {

    public static void main(String[] args) {
        String line = readLn();
        while (line != null) {
            int i = process(line);
            System.out.println("The string '" + line + "' contains " + i + " palindromes.");
            line = readLn();
        }
    }

    private static int process(String line) {
        HashMap<String, String> isPalindrome = new HashMap<String, String>();
        for (int k = 0; k < line.length(); k++) {
            for (int j = k + 1; j <= line.length(); j++) {
                String sub = line.substring(k, j);
                if (isPalindrome.get(sub) == null) {
                    if (isPalindrome(sub)) {
                        isPalindrome.put(sub, sub);
                    }
                }
            }
        }
        return isPalindrome.size();
    }

    private static boolean isPalindrome(String sub) {
        int length = sub.length() / 2;
        for (int k = 0; k < length; k++) {
            if (sub.charAt(k) != sub.charAt(sub.length() - k - 1)) return false;
        }
        return true;
    }

    static String readLn() {
        String newLine = System.getProperty("line.separator");
        StringBuilder buffer = new StringBuilder();
        int car = -1;
        try {
            car = System.in.read();
            while ((car > 0) && (car != newLine.charAt(0))) {
                buffer.append((char) car);
                car = System.in.read();
            }
            if (car == newLine.charAt(0)) System.in.skip(newLine.length() - 1);
        } catch (java.io.IOException e) {
            return (null);
        }
        if ((car < 0) && (buffer.length() == 0)) return (null);
        return (buffer.toString()).trim();
    }
}
