package jopt.csp.util;

/**
 * A sorted set of integer values
 */
public class IntSparseSet extends IntSet {

    protected SortableIntList values;

    protected int callback;

    protected IntSparseSetListener listener;

    private IntSparseIterator valueIterator;

    /**
     *  Creates a new set
     */
    public IntSparseSet() {
        values = new SortableIntList();
    }

    /**
     * Cloning constructor
     */
    private IntSparseSet(IntSparseSet set) {
        values = new SortableIntList(set.values);
    }

    /**
     * Returns the size of the set
     */
    public int size() {
        return values.size();
    }

    /**
     * Removes all values from the set
     */
    public void clear() {
        while (values.size() > 0) {
            int val = values.removeElementAt(0);
            if (listener != null) listener.valueRemoved(callback, val);
        }
    }

    /**
     * Returns minimum value for set
     */
    public int getMin() {
        if (values.size() == 0) return Integer.MAX_VALUE;
        return values.get(0);
    }

    /**
     * Returns maximum value for set
     */
    public int getMax() {
        if (values.size() == 0) return Integer.MIN_VALUE;
        return values.get(values.size() - 1);
    }

    /**
     *  Returns index of a value in the list
     */
    private int indexOfValue(int val) {
        if (values.size() == 0) return -1;
        int lowIdx = 0;
        int highIdx = values.size() - 1;
        while (lowIdx <= highIdx) {
            int idx = (lowIdx + highIdx) / 2;
            int v = values.get(idx);
            if (v == val) return idx;
            if (val < v) highIdx = idx - 1; else lowIdx = idx + 1;
        }
        return -(lowIdx + 1);
    }

    /**
     *  Returns true if value is contained in set
     */
    public boolean contains(int val) {
        return indexOfValue(val) >= 0;
    }

    /**
     * Adds a value to set
     */
    public void add(int val) {
        int idx = indexOfValue(val);
        if (idx < 0) {
            idx = -idx - 1;
            values.add(idx, val);
            if (listener != null) listener.valueAdded(callback, val);
        }
    }

    /**
     * Adds a range of values to set
     */
    public void add(int start, int end) {
        for (int i = start; i <= end; i++) add(i);
    }

    /**
     * Removes a value from the set
     */
    public void remove(int val) {
        int idx = indexOfValue(val);
        if (idx >= 0) {
            values.removeElementAt(idx);
            if (listener != null) listener.valueRemoved(callback, val);
        }
    }

    /**
     * Removes a range of values from the set
     */
    public void remove(int start, int end) {
        if (values.size() == 0) return;
        for (int i = start; i <= end; i++) remove(i);
    }

    /**
     * Removes all values above and including given value
     */
    public void removeStartingFrom(int val) {
        if (values.size() == 0) return;
        int max = values.get(values.size() - 1);
        remove(val, max);
    }

    /**
     * Removes all values below and including given value
     */
    public void removeEndingAt(int val) {
        if (values.size() == 0) return;
        int min = values.get(0);
        remove(min, val);
    }

    /**
     * Returns the next higher value in the domain or current value if none
     * exists
     */
    public int getNextHigher(int val) {
        if (values.size() == 0) return val;
        int min = values.get(0);
        if (val < min) return min;
        int max = values.get(values.size() - 1);
        if (val >= max) return val;
        int next = val + 1;
        int idx = indexOfValue(next);
        if (idx >= 0) return next;
        idx = -idx - 1;
        return values.get(idx);
    }

    /**
     * Returns the next lower value in the domain or current value if none
     * exists
     */
    public int getNextLower(int val) {
        if (values.size() == 0) return val;
        int max = values.get(values.size() - 1);
        if (val > max) return max;
        int min = values.get(0);
        if (val <= min) return val;
        int prev = val - 1;
        int idx = indexOfValue(prev);
        if (idx >= 0) return prev;
        idx = -idx - 1;
        return values.get(idx);
    }

    /**
     * Sets listener for set to be notified of changes
     */
    public void setListener(IntSparseSetListener listener, int callback) {
        this.listener = listener;
        this.callback = callback;
    }

    /**
     * Returns listener that is currently assigned to set
     */
    public IntSparseSetListener getListener() {
        return listener;
    }

    /**
     * Creates a duplicate of this set
     */
    public Object clone() {
        return new IntSparseSet(this);
    }

    /** 
     * returns an iterator containing all numbers in the set
     */
    public NumberIterator values() {
        if (valueIterator == null) valueIterator = new IntSparseIterator(); else valueIterator.reset();
        return valueIterator;
    }

    public String toString() {
        return values.toString();
    }

    /**
     * Iterator for integer values
     */
    private class IntSparseIterator extends NumberIterator {

        private static final long serialVersionUID = 1L;

        private int idx;

        public IntSparseIterator() {
            this.idx = -1;
        }

        public void reset() {
            idx = -1;
        }

        public boolean hasNext() {
            int s = values.size() - 1;
            return s >= 0 && idx < s;
        }

        public Number next() {
            idx++;
            return this;
        }

        public int intValue() {
            return values.get(idx);
        }

        public long longValue() {
            return values.get(idx);
        }

        public float floatValue() {
            return values.get(idx);
        }

        public double doubleValue() {
            return values.get(idx);
        }
    }
}
