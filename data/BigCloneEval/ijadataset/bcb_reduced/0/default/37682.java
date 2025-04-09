import java.util.Random;

public class SomeOtherObject implements MediaObject {

    public long fib(int n) {
        if (n <= 1) return n; else return fib(n - 1) + fib(n - 2);
    }

    public void start(Object lock, int itra) {
        fib(19);
    }
}
