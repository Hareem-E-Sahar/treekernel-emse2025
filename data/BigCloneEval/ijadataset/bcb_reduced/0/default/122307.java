import java.util.*;

public class ThreadFibo implements Runnable {

    private int n;

    private Vector result;

    private Thread t;

    public ThreadFibo(int n) {
        this.n = n;
        t = new Thread(this);
        t.start();
        result = new Vector();
    }

    public void run() {
        fib(n);
    }

    public int fib(int n) {
        if (n == 0 || n == 1) return n; else return fib(n - 1) + fib(n - 2);
    }

    public Thread getThread() {
        return t;
    }

    public Vector getResult() {
        return result;
    }

    public String toString() {
        return result.toString();
    }
}
