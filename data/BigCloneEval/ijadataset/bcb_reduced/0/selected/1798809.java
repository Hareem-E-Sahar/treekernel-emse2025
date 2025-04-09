package algoritms;

import java.util.concurrent.*;

/**
 * @author Sergiy Doroshenko
 *         email:webserg@gmail.com
 *         Date: 3/12/11 7:02 PM
 */
public class FibonachiByForkJoinAlgorithm extends RecursiveAction {

    static final int threshold = 13;

    volatile int number;

    FibonachiByForkJoinAlgorithm(int n) {
        number = n;
    }

    int getAnswer() {
        if (!isDone()) throw new IllegalStateException();
        return number;
    }

    public void compute() {
        int n = number;
        if (n <= threshold) number = seqFib(n); else {
            FibonachiByForkJoinAlgorithm f1 = new FibonachiByForkJoinAlgorithm(n - 1);
            FibonachiByForkJoinAlgorithm f2 = new FibonachiByForkJoinAlgorithm(n - 2);
            invokeAll(f1, f2);
            number = f1.number + f2.number;
        }
    }

    public static void main(String[] args) {
        int groupSize = 2;
        ForkJoinPool group = new ForkJoinPool(groupSize);
        FibonachiByForkJoinAlgorithm f = new FibonachiByForkJoinAlgorithm(35);
        group.invoke(f);
        int result = f.getAnswer();
        System.out.println("Answer: " + result);
    }

    int seqFib(int n) {
        if (n <= 1) return n; else return seqFib(n - 1) + seqFib(n - 2);
    }
}
