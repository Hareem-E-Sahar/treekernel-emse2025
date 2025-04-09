class P11221_39643 {

    public static void main(String[] args) {
        int cases = Integer.parseInt(readLn1());
        for (int caseNumber = 0; caseNumber < cases; caseNumber++) {
            System.out.println("Case #" + (caseNumber + 1) + ":");
            String line = readLn();
            int k = (int) Math.sqrt(line.length());
            if (k * k != line.length()) System.out.print("No magic :("); else {
                if (!checkPalindrome(line)) {
                    System.out.print("No magic :(");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < k; j++) {
                        for (int l = 0; l < k; l++) {
                            sb.append(line.charAt(l * k + j));
                        }
                    }
                    if (!sb.toString().equals(line)) {
                        System.out.print("No magic :(");
                    } else {
                        System.out.print(k);
                    }
                }
            }
            System.out.println();
        }
    }

    private static boolean checkPalindrome(String string) {
        int length = string.length() / 2;
        int fullLength = string.length();
        for (int k = 0; k < length; k++) {
            if (string.charAt(k) != string.charAt(fullLength - 1 - k)) return false;
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
                if (car >= 'a' && car <= 'z') buffer.append((char) car);
                car = System.in.read();
            }
            if (car == newLine.charAt(0)) System.in.skip(newLine.length() - 1);
        } catch (java.io.IOException e) {
            return (null);
        }
        if ((car < 0) && (buffer.length() == 0)) return (null);
        return (buffer.toString()).trim();
    }

    static String readLn1() {
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
