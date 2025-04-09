package jopt.csp.util;

import org.apache.commons.collections.primitives.ArrayFloatList;
import org.apache.commons.collections.primitives.FloatIterator;

/**
 * A sorted set of individual float values
 */
public class FloatSparseSet extends FloatSet {

    protected ArrayFloatList values;

    protected FloatSparseSetListener listener;

    protected int callback;

    /**
     *  Creates a new set
     */
    public FloatSparseSet() {
        values = new ArrayFloatList();
    }

    /**
     * Returns the size of the set
     */
    public final int size() {
        return values.size();
    }

    /**
     * Removes all values from the set
     */
    public void clear() {
        while (values.size() > 0) {
            float val = values.removeElementAt(0);
            if (listener != null) listener.valueRemoved(callback, val);
        }
    }

    /**
     * Returns minimum value for set
     */
    public float getMin() {
        if (values.size() == 0) return Float.MAX_VALUE;
        return values.get(0);
    }

    /**
     * Returns maximum value for set
     */
    public float getMax() {
        if (values.size() == 0) return -Float.MAX_VALUE;
        return values.get(values.size() - 1);
    }

    /**
     * Returns index of a value in the list or a negative value indicating where it
     * should be if it were to be added (see details).
     * 
     * @return Index of value in list, if it exists.  If not, then it returns a negative value
     *         such that the index to insert the new element is (-returnvalue - 1)
     */
    private int indexOfValue(float val) {
        if (values.size() == 0) return -1;
        int lowIdx = 0;
        int highIdx = values.size() - 1;
        while (lowIdx <= highIdx) {
            int idx = (lowIdx + highIdx) / 2;
            float v = values.get(idx);
            if (val == v) return idx;
            if (val < v) highIdx = idx - 1; else lowIdx = idx + 1;
        }
        return -(lowIdx + 1);
    }

    /**
     *  Returns true if value is contained in set
     */
    public boolean contains(float val) {
        return indexOfValue(val) >= 0;
    }

    /**
     * Adds a value to set
     */
    public void add(float val) {
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
    public void add(float start, float end) {
        throw new UnsupportedOperationException("cannot insert a range of real numbers into a sparse set");
    }

    /**
     * Removes a value from the set
     */
    public void remove(float val) {
        int idx = indexOfValue(val);
        if (idx >= 0) {
            values.removeElementAt(idx);
            if (listener != null) listener.valueRemoved(callback, val);
        }
    }

    /**
     * Removes a range of values from the set
     */
    public void remove(float start, float end) {
        if (values.size() == 0) return;
        int idx = indexOfValue(start);
        if (idx < 0) idx = -idx - 1;
        while (idx < values.size()) {
            float val = values.get(idx);
            if (end > val) break;
            if (start < val) values.removeElementAt(idx); else idx++;
        }
    }

    /**
     * Removes all values above and including given value
     */
    public void removeStartingFrom(float val) {
        if (values.size() == 0) return;
        float max = values.get(values.size() - 1);
        remove(val, max);
    }

    public void removeStartingAfter(float val) {
        removeStartingFrom(FloatUtil.next(val));
    }

    /**
     * Removes all values below and including given value
     */
    public void removeEndingAt(float val) {
        if (values.size() == 0) return;
        float min = values.get(0);
        remove(min, val);
    }

    public void removeEndingBefore(float val) {
        removeEndingAt(FloatUtil.previous(val));
    }

    /**
     * Returns the next higher value in the domain or current value if none
     * exists
     */
    public float getNextHigher(float val) {
        if (values.size() == 0) return val;
        float min = values.get(0);
        if (val < min) return min;
        float max = values.get(values.size() - 1);
        if (val >= max) return val;
        float next = val + 1;
        int idx = indexOfValue(next);
        if (idx >= 0) return next;
        idx = -idx - 1;
        return values.get(idx);
    }

    /**
     * Returns the next lower value in the domain or current value if none
     * exists
     */
    public float getNextLower(float val) {
        if (values.size() == 0) return val;
        float max = values.get(values.size() - 1);
        if (val > max) return max;
        float min = values.get(0);
        if (val <= min) return val;
        float prev = val - 1;
        int idx = indexOfValue(prev);
        if (idx >= 0) return prev;
        idx = -idx - 1;
        return values.get(idx);
    }

    /**
     * Creates a duplicate of this set
     */
    public Object clone() {
        FloatSparseSet set = createEmptySet();
        for (int i = 0; i < values.size(); i++) {
            set.values.add(values.get(i));
        }
        return set;
    }

    /**
     * Creates a new empty set
     */
    protected FloatSparseSet createEmptySet() {
        return new FloatSparseSet();
    }

    /**
     * Sets listener for set to be notified of changes
     */
    public void setListener(FloatSparseSetListener listener, int callback) {
        this.listener = listener;
        this.callback = callback;
    }

    /**
     * Returns listener that is currently assigned to set
     */
    public FloatSparseSetListener getListener() {
        return listener;
    }

    /**
     * Creates a new number for use during iteration
     */
    protected Number createNumber(float val) {
        return new Float(val);
    }

    /** 
     * returns an iterator containing all numbers in the set
     */
    public NumberIterator values() {
        return new FloatSparseIterator();
    }

    public String toString() {
        return values.toString();
    }

    /**
     * Iterator for float values
     */
    private class FloatSparseIterator extends NumberIterator {

        private static final long serialVersionUID = 1L;

        private FloatIterator iterator;

        private float n;

        public FloatSparseIterator() {
            this.iterator = values.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Number next() {
            n = iterator.next();
            return this;
        }

        public int intValue() {
            return (int) n;
        }

        public long longValue() {
            return (long) n;
        }

        public float floatValue() {
            return n;
        }

        public double doubleValue() {
            return n;
        }
    }
}
