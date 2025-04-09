import java.util.Random;

/**
 * A test thread that will simulate a thread that runs for a given amount of time.
 * 
 * The time is configured by minRunTime, maxRunTime, and a number randomly between
 * them is chosen to represent the time the thread will sleep.
 * 
 * @author ddaniels
 *
 */
public class TestThread implements Runnable {

    private int actualRunTime;

    private boolean done = false;

    private boolean interrupted = false;

    public TestThread(int runTime) {
        actualRunTime = runTime;
    }

    /**
 * The time is configured by minRunTime, maxRunTime, and a number randomly between
 * them is chosen to represent the time the thread will sleep.
 * 
 * @param minRunTime - minimum amount of time to run
 * @param maxRunTime - maximum amount of time to run
 */
    public TestThread(int minRunTime, int maxRunTime) {
        Random r = new Random();
        int min = minRunTime;
        int max = maxRunTime;
        actualRunTime = min + r.nextInt(max - min);
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized boolean isInterrupted() {
        return interrupted;
    }

    /**
     * All this thread does it sleep for a given amount of time
     * to simulate a thread doing real computations.
     */
    public void run() {
        try {
            Thread.currentThread().sleep(actualRunTime);
        } catch (InterruptedException e) {
            System.err.println(Thread.currentThread().getName() + " : TestThread interrupted!");
            interrupted = true;
            Thread.currentThread().interrupt();
            return;
        }
        done = true;
    }
}
