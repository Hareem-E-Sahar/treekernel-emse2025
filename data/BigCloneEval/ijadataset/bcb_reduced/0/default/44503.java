class P10223 {

    public static void main(String[] args) {
        long[] array = new long[21];
        array[1] = 1;
        array[2] = 2;
        array[3] = 5;
        array[4] = 14;
        array[5] = 42;
        array[6] = 132;
        array[7] = 429;
        array[8] = 1430;
        array[9] = 4862;
        array[10] = 16796;
        array[11] = 58786;
        array[12] = 208012;
        array[13] = 742900;
        array[14] = 2674440;
        array[15] = 9694845;
        array[16] = 35357670;
        array[17] = 129644790;
        array[18] = 477638700;
        array[19] = 1767263190;
        array[20] = 6564120420l;
        String line = readLn();
        while (line != null) {
            long l = Long.parseLong(line);
            int value = -1;
            for (int k = 0; k < 21; k++) {
                if (array[k] == l) {
                    value = k;
                    break;
                }
                if (array[k] > l) {
                    value = k - 1;
                    break;
                }
            }
            System.out.println(value);
            line = readLn();
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
