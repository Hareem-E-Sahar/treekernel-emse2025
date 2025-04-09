import java.util.ArrayList;
import java.lang.InterruptedException;
import java.util.concurrent.*;

public class Sort<T extends Comparable<T>> implements Callable<Boolean> {

    private ArrayList<T> array;

    private ExecutorService pool;

    private int start;

    private int end;

    private Sort(ArrayList<T> array, ExecutorService pool, int s, int e) {
        this.pool = pool;
        this.array = array;
        this.start = s;
        this.end = e;
    }

    public Sort(ArrayList<T> array, ExecutorService pool) {
        this(array, pool, 0, array.size());
    }

    private void merge(int middle) {
        ArrayList<T> cpy = new ArrayList<T>(end - start);
        for (int i = start; i < end; ++i) {
            cpy.add(array.get(i));
        }
        int i = 0;
        int j = middle - start;
        int index = start;
        while (i < middle - start && j < end - start) {
            if (cpy.get(i).compareTo(cpy.get(j)) < 0) {
                array.set(index, cpy.get(i));
                ++i;
            } else {
                array.set(index, cpy.get(j));
                ++j;
            }
            ++index;
        }
        while (i < middle - start) {
            array.set(index, cpy.get(i));
            ++i;
            ++index;
        }
        while (j < end - start) {
            array.set(index, cpy.get(j));
            ++j;
            ++index;
        }
    }

    private void sort() throws Exception {
        if (end - start <= 1) {
            return;
        }
        int middle = start + (end - start) / 2;
        Future<Boolean> left = pool.submit(new Sort<T>(array, pool, start, middle));
        Future<Boolean> right = pool.submit(new Sort<T>(array, pool, middle, end));
        if (left.get() && right.get()) {
            merge(middle);
        }
    }

    public Boolean call() {
        try {
            sort();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
