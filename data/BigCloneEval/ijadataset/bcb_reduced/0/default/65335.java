public class Handler1 {

    public Handler1() {
    }

    static int sequentialThreshold = 13;

    public void compute(Fib f1, Fib f2) {
        int number = f1.number;
        if (number == 1) {
            f1.result = 1;
            return;
        }
        if (number <= sequentialThreshold) {
            f1.result = seqFib(number);
            return;
        }
        Fib f3 = new Fib(number - 1);
        Fib f4 = new Fib(number - 2);
        compute(f3, f4);
        f1.result = f3.result + f4.result;
    }

    static long seqFib(int n) {
        if (n <= 1) return n; else return seqFib(n - 1) + seqFib(n - 2);
    }

    static class Fib {

        final int number;

        long result;

        public Fib(int number) {
            this.number = number;
        }
    }
}
