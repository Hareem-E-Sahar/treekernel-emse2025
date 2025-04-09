package org.skuebeck.ooc.examples.parallel;

import org.skuebeck.ooc.ConcurrentObjects;

public class Fibonacci implements ParallelFibonacci {

    private static final int TRESHOLD = 13;

    private int result;

    public static ParallelFibonacci newInstance() {
        return ConcurrentObjects.newInstance(Fibonacci.class);
    }

    static int calculate(int n) {
        ParallelFibonacci fib = newInstance();
        fib.fork(n);
        return fib.getResult();
    }

    @Override
    public void fork(int n) {
        if (n <= TRESHOLD) {
            result = calcSequential(n);
        } else {
            ParallelFibonacci fib1 = newInstance();
            fib1.fork(n - 1);
            ParallelFibonacci fib2 = newInstance();
            fib2.fork(n - 2);
            result = fib1.getResult() + fib2.getResult();
        }
    }

    static int calcSequential(int n) {
        if (n <= 1) {
            return n;
        } else {
            return calcSequential(n - 1) + calcSequential(n - 2);
        }
    }

    @Override
    public int getResult() {
        return result;
    }
}
