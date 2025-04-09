class FibonacciRec {

    public static void main(String[] s) {
        int num = 20;
        System.out.print("Serie De Fibonacci: ");
        for (int i = 1; i < num; i++) {
            System.out.print(fib(i));
            System.out.print(" ");
        }
    }

    public static int fib(int i) {
        if (i <= 2) return 1;
        return fib(i - 1) + fib(i - 2);
    }
}
