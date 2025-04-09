package com.ait.actors;

import junit.framework.Assert;
import junit.framework.TestCase;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;
import java.util.ArrayList;

public class SchedulerTest extends TestCase {

    private static final int COUNT = 100;

    public void testExecute() throws Exception {
        final Integer res = (Integer) Scheduler.execute(new Callable() {

            public Object call() throws Exception {
                return 239;
            }
        });
        assertEquals(239, res.intValue());
    }

    public void testExecuteMany() throws Exception {
        class Task implements Callable {

            private final int value;

            Task(int value) {
                this.value = value;
            }

            public Object call() throws Exception {
                return value;
            }

            public String toString() {
                return "Task-" + value;
            }
        }
        Callable[] tasks = new Callable[4];
        for (int i = 0; i != 4; ++i) tasks[i] = new Task(i);
        Scheduler.execute(tasks);
    }

    public void testScheduler() throws Exception {
        final AtomicInteger i = new AtomicInteger();
        Scheduler.execute(action(i));
        assertEquals(2 * COUNT - 1, i.get());
    }

    public void testFib() throws Exception {
        for (int i = 10; i <= 30; ++i) {
            final long start = System.nanoTime();
            final int fib = slowFib(i);
            final long slow = System.nanoTime();
            assertEquals(fib, fib(i));
            final long end = System.nanoTime();
            System.out.println("fib(" + i + ") = " + fib + "\t\t\t" + (slow - start) / 1000000.0D + "ms\t" + (end - slow) / 100000.0D + "ms");
        }
    }

    public void testMultiFib() throws Exception {
        ArrayList<Callable> call = new ArrayList<Callable>();
        for (int i = 10; i <= 30; ++i) {
            call.add(fibActor(i));
        }
        final long start = System.currentTimeMillis();
        Scheduler.execute(call.toArray(new Callable[call.size()]));
        final long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
    }

    private Integer slowFib(int i) {
        if (i <= 0) return 0;
        if (i == 1) return 1;
        return slowFib(i - 1) + slowFib(i - 2);
    }

    private int fib(int i) throws Exception {
        if (i < 10) return slowFib(i);
        final Object res = fibActor(i).execute();
        return (Integer) res;
    }

    private Callable action(final AtomicInteger i) {
        return new Callable() {

            public Object call() throws Exception {
                final Thread thread = Thread.currentThread();
                if (i.incrementAndGet() < COUNT) {
                    Scheduler.execute(action(i), action(i));
                }
                return null;
            }
        };
    }

    private Continuation<Integer> fibActor(final int i) {
        class Task extends Continuation<Integer> {

            int i;

            Task(int i) {
                this.i = i;
            }

            public Integer doCall() throws Exception {
                if (i < 3) {
                    return slowFib(i);
                }
                if (isNestedCompleted()) {
                    return (Integer) results[0] + (Integer) results[1];
                }
                schedule(new Callable[] { fibActor(i - 1), fibActor(i - 2) });
                return null;
            }

            public String toString() {
                return "fib(" + i + ")";
            }
        }
        return new Task(i);
    }
}
