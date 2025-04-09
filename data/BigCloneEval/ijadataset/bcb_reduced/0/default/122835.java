import jeliot.io.*;

public class MyClass {

    public static void main() {
        System.out.println(fibo(Input.readInt()));
    }

    public static int fibo(int n) {
        if (n <= 1) {
            return 1;
        }
        return fibo(n - 1) + fibo(n - 2);
    }
}
