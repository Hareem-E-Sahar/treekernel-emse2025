import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        Random rand = new Random();
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (int i = 0; i < 10000; ++i) {
            array.add(rand.nextInt());
        }
        ParallelRuntime runtime = new ParallelRuntime();
        MergeSortActivity<Integer> rootSorter = new MergeSortActivity<Integer>(0, array.size(), array, runtime);
        try {
            runtime.exec(rootSorter);
        } catch (Exception e) {
            System.err.print("Sorry, error occured\n");
            e.printStackTrace();
            System.exit(-1);
        }
        for (int i = 0; i < 10000; ++i) {
            if (i % 10 == 0) {
                System.out.print("\n");
            }
            System.out.print(rootSorter.getResult().get(i));
            System.out.print(" ");
            if (i < 9999 && rootSorter.getResult().get(i).compareTo(rootSorter.getResult().get(i + 1)) > 0) {
                System.err.print("Sort has error");
            }
        }
        if (rootSorter.getResult().size() != 10000) {
            System.err.print("Sort has error");
        }
    }
}

final class ParallelRuntime {

    public ParallelRuntime() {
        super();
        this.usedThreads = new Integer(0);
        this.maxThreadCount = Runtime.getRuntime().availableProcessors() * 10;
        this.executor = Executors.newFixedThreadPool(maxThreadCount);
    }

    public void exec(Runnable rootSorter) throws InterruptedException, ExecutionException {
        synchronized (usedThreads) {
            ++usedThreads;
        }
        executor.submit(rootSorter).get();
        synchronized (usedThreads) {
            --usedThreads;
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public boolean reserveTwoThreads() {
        synchronized (usedThreads) {
            if ((usedThreads + 2) < maxThreadCount) {
                usedThreads += 2;
                return true;
            }
        }
        return false;
    }

    public void releaseThread() {
        synchronized (usedThreads) {
            --usedThreads;
        }
    }

    private Integer usedThreads;

    private final ExecutorService executor;

    private final int maxThreadCount;
}

final class MergeSortActivity<T extends Comparable<T>> implements Runnable {

    private static final int MIN_MERGE_LEN = 20;

    public MergeSortActivity(int startIndex, int endIndex, List<T> list, ParallelRuntime runtime) {
        super();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.list = list;
        this.runtime = runtime;
        this.sortedList = new ArrayList<T>();
    }

    public List<T> getResult() {
        return sortedList;
    }

    @Override
    public void run() {
        doMergeSort();
    }

    private void doInsertSort() {
        for (int i = startIndex; i < endIndex; ++i) {
            int min = i;
            for (int j = i; j < endIndex; ++j) {
                if (list.get(min).compareTo(list.get(j)) > 0) {
                    min = j;
                }
            }
            T temp = list.get(i);
            list.set(i, list.get(min));
            list.set(min, temp);
        }
        for (int i = startIndex; i < endIndex; ++i) {
            sortedList.add(list.get(i));
        }
    }

    private void doMergeSort() {
        if ((endIndex - startIndex) < MIN_MERGE_LEN) {
            doInsertSort();
            return;
        }
        int midIndex = startIndex + (endIndex - startIndex) / 2;
        MergeSortActivity<T> leftSorter = new MergeSortActivity<T>(startIndex, midIndex, list, runtime);
        MergeSortActivity<T> rightSorter = new MergeSortActivity<T>(midIndex, endIndex, list, runtime);
        if (runtime.reserveTwoThreads()) {
            Future<?> lFuture = runtime.getExecutor().submit(leftSorter);
            Future<?> rFuture = runtime.getExecutor().submit(rightSorter);
            try {
                lFuture.get();
                runtime.releaseThread();
                rFuture.get();
                runtime.releaseThread();
            } catch (Exception e) {
                System.err.print("Sorry, error occured\n");
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            leftSorter.run();
            rightSorter.run();
        }
        int lIndex = 0;
        int rIndex = 0;
        while (lIndex != leftSorter.getResult().size() || rIndex != rightSorter.getResult().size()) {
            T nextElement = null;
            if (rIndex == rightSorter.getResult().size()) {
                nextElement = leftSorter.getResult().get(lIndex++);
            } else if (lIndex == leftSorter.getResult().size()) {
                nextElement = rightSorter.getResult().get(rIndex++);
            } else {
                if (leftSorter.getResult().get(lIndex).compareTo(rightSorter.getResult().get(rIndex)) < 0) {
                    nextElement = leftSorter.getResult().get(lIndex++);
                } else {
                    nextElement = rightSorter.getResult().get(rIndex++);
                }
            }
            sortedList.add(nextElement);
        }
        return;
    }

    private final int startIndex;

    private final int endIndex;

    private final List<T> list;

    private final ParallelRuntime runtime;

    private final List<T> sortedList;
}
