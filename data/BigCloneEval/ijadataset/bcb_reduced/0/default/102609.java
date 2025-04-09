class P10945_39643 {

    public static void main(String[] args) {
        while (true) {
            String line = readLn();
            if ("DONE".equals(line)) break;
            process(line);
        }
    }

    private static void process(String line) {
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < line.length(); k++) {
            char c = line.charAt(k);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                if ((c >= 'a' && c <= 'z')) {
                    int n = c - 'a';
                    c = (char) (n + 'A');
                }
                sb.append(c);
            }
        }
        if (isPalindrome(sb.toString())) {
            System.out.println("You won't be eaten!");
        } else {
            System.out.println("Uh oh..");
        }
    }

    private static boolean isPalindrome(String string) {
        for (int k = 0; k < string.length() / 2; k++) {
            if (string.charAt(k) != string.charAt(string.length() - (k + 1))) return false;
        }
        return true;
    }

    static String readLn() {
        String newLine = System.getProperty("line.separator");
        StringBuffer buffer = new StringBuffer();
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
