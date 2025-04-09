import java.util.StringTokenizer;

class P10812 {

    public static void main(String[] args) {
        int numberOfCases = Integer.parseInt(readLn());
        for (int k = 0; k < numberOfCases; k++) {
            String line = readLn();
            StringTokenizer stringTokenizer = new StringTokenizer(line);
            int sum = Integer.parseInt(stringTokenizer.nextToken());
            int difference = Integer.parseInt(stringTokenizer.nextToken());
            int a = (sum + difference) / 2;
            int b = a - difference;
            if (a + b == sum && a - b == difference && b >= 0) {
                System.out.println(a + " " + b);
            } else {
                System.out.println("impossible");
            }
        }
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
