class Example073 {

    public static void main(String[] args) {
        if (args.length != 1) System.out.println("Usage:  Example73 <double>\n"); else {
            double[] arr = { 2, 3, 5, 5, 7, 11, 13 };
            int k = binarySearch(arr, Double.parseDouble(args[0]));
            if (k >= 0) System.out.format("Found at %d%n", k); else System.out.format("Not found, belongs at %d%n", ~k);
        }
    }

    public static int binarySearch(double[] arr, double x) {
        int a = 0, b = arr.length - 1;
        while (a <= b) {
            int i = (a + b) / 2;
            if (arr[i] < x) a = i + 1; else if (arr[i] > x) b = i - 1; else return i;
        }
        return ~a;
    }
}
