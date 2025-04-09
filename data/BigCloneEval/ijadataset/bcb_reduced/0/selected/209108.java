package peersim.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class adds the ability to retrieve the median element to the
 * {@link IncrementalStats} class. Note that this class actually stores all
 * the elements, so (unlike in its superclass) storage requirements depend
 * on the number of items processed.
 * 
 * @author giampa
 */
public class MedianStats extends IncrementalStats {

    /** Structure to store each entry. */
    private final ArrayList<Double> data = new ArrayList<Double>();

    /** Calls {@link #reset}. */
    public MedianStats() {
        reset();
    }

    /**
 * Retrieves the median in the current data collection.
 * 
 * @return The current median value.
 */
    public double getMedian() {
        double result;
        if (data.isEmpty()) throw new IllegalStateException("Data vector is empty!");
        Collections.sort(data);
        if (data.size() % 2 != 0) {
            result = data.get(data.size() / 2);
        } else {
            double a = data.get(data.size() / 2);
            double b = data.get(data.size() / 2 - 1);
            result = (a + b) / 2;
        }
        return result;
    }

    public void add(double item, int k) {
        for (int i = 0; i < k; ++i) {
            super.add(item, 1);
            data.add(new Double(item));
        }
    }

    public void reset() {
        super.reset();
        if (data != null) data.clear();
    }

    public static void main(String[] args) {
        MedianStats s = new MedianStats();
        for (int i = 0; i < args.length; i++) s.add(Double.parseDouble(args[i]));
        System.out.println("Average: " + s.getAverage());
        System.out.println("Median: " + s.getMedian());
    }
}
