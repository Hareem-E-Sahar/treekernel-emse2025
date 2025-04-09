package com.mtgi.analytics.aop;

import org.junit.Test;
import com.mtgi.analytics.aop.BehaviorAdviceTest.ServiceA;
import com.mtgi.analytics.test.AbstractPerformanceTestCase;
import com.mtgi.analytics.test.AbstractSpringTestCase;

/**
 * Performs some timed tests to verify that behavior tracking doesn't
 * interfere too much with application performance.
 */
public class PerformanceTest extends AbstractPerformanceTestCase {

    private static final long AVERAGE_OVERHEAD_NS = 100000;

    private static final long WORST_OVERHEAD_NS = 10000;

    private static final long TIME_BASIS = 100000;

    private static final String[] BASIS_CONFIG = { "com/mtgi/analytics/aop/PerformanceTest-basis.xml" };

    private static final String[] TEST_CONFIG = { "com/mtgi/analytics/aop/PerformanceTest-basis.xml", "com/mtgi/analytics/aop/PerformanceTest-tracking.xml" };

    public PerformanceTest() {
        super(5, 100, TIME_BASIS, AVERAGE_OVERHEAD_NS, WORST_OVERHEAD_NS);
    }

    @Test
    public void testPerformance() throws Throwable {
        TestJob basisJob = new TestJob(BASIS_CONFIG);
        TestJob testJob = new TestJob(TEST_CONFIG);
        testPerformance(basisJob, testJob);
    }

    public static class TestJob extends AbstractSpringTestCase<ServiceA> {

        private static final long serialVersionUID = 6599513817152651866L;

        public TestJob(String[] configFiles) {
            super("serviceA", ServiceA.class, configFiles);
        }

        public void run() {
            fib(20);
            bean.getTracked("sleepy");
        }

        public int fib(int n) {
            if (n == 0 || n == 1) return 1;
            return fib(n - 1) + fib(n - 2);
        }
    }
}
