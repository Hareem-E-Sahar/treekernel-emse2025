import jeliot.io.*;

class RecuFibonacci2 {

    static void main() {
        int n = 10;
        Output.println(fibo(n));
    }

    static int fibo(int n) {
        if (n < 2) {
            return 1;
        } else {
            return fibo(n - 1) + fibo(n - 2);
        }
    }
}
