package maiabee.mtai.numbers.fibonacci;

class RecursiveFibonacciSeries implements FibonacciSeries {

    public int getFibonacci(int n) {
        if (n < 2) return 1; else return getFibonacci(n - 1) + getFibonacci(n - 2);
    }
}
