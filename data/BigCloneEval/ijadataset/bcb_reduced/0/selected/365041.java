package org.mili.jmibs.examples;

import org.mili.jmibs.impl.*;

/**
 * This class defines a benchmark to test performance Fibonacci numbers recursive.
 *
 * @author Michael Lieshoff
 * @version 1.2 16.06.2010
 * @since 1.0
 * @changed ML 15.06.2010 - changed to implement {@link AbstractIntervalBenchmark}.
 * @changed ML 16.06.2010 - changed to static public fib method.
 */
public class FibonacciRecursiveBenchmark extends AbstractIntervalBenchmark<IntegerInterval> {

    /**
     * creates a new recursive fib benchmark.
     */
    public FibonacciRecursiveBenchmark() {
        super();
        this.setName("Fibonacci: recursive");
    }

    /**
     * computes the fib of a.
     *
     * @param a number.
     * @return the fib of a.
     */
    public static int fib(int a) {
        if (a <= 0) {
            return 0;
        }
        if (a == 1 || a == 2) {
            return 1;
        } else {
            return fib(a - 1) + fib(a - 2);
        }
    }

    @Override
    public void execute() {
        fib(this.getInterval().getValue());
    }

    @Override
    public void prepare() {
    }
}
