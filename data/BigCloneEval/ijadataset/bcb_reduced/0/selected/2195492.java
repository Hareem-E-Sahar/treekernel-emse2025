package net.sf.saxon.sort;

import net.sf.saxon.om.FastStringBuffer;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Set of int values. This implementation of IntSet uses a sorted array
 * of integer ranges.
 *
 * @author Michael Kay
 */
public class IntRangeSet implements Serializable, IntSet {

    private int[] startPoints;

    private int[] endPoints;

    private int used = 0;

    private int hashCode = -1;

    private int size = 0;

    /**
     *  Create an empty set
     */
    public IntRangeSet() {
        startPoints = new int[4];
        endPoints = new int[4];
        used = 0;
        size = 0;
        hashCode = -1;
    }

    /**
     * Create one IntRangeSet as a copy of another
     * @param input the IntRangeSet to be copied
     */
    public IntRangeSet(IntRangeSet input) {
        startPoints = new int[input.used];
        endPoints = new int[input.used];
        used = input.used;
        System.arraycopy(input.startPoints, 0, startPoints, 0, used);
        System.arraycopy(input.endPoints, 0, endPoints, 0, used);
        hashCode = input.hashCode;
    }

    /**
     * Create an IntRangeSet given the start points and end points of the integer ranges.
     * The two arrays must be the same length; each must be in ascending order; and the n'th end point
     * must be greater than the n'th start point, and less than the n+1'th start point, for all n.
     * @param startPoints the start points of the integer ranges
     * @param endPoints the end points of the integer ranges
     * @throws IllegalArgumentException if the two arrays are different lengths. Other error conditions
     * in the input are not currently detected.
     */
    public IntRangeSet(int[] startPoints, int[] endPoints) {
        if (startPoints.length != endPoints.length) {
            throw new IllegalArgumentException("Array lengths differ");
        }
        this.startPoints = startPoints;
        this.endPoints = endPoints;
        used = startPoints.length;
        for (int i = 0; i < used; i++) {
            size += (endPoints[i] - startPoints[i] + 1);
        }
    }

    public void clear() {
        startPoints = new int[4];
        endPoints = new int[4];
        used = 0;
        hashCode = -1;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(int value) {
        if (value > endPoints[used - 1]) {
            return false;
        }
        if (value < startPoints[0]) {
            return false;
        }
        int i = 0;
        int j = used;
        do {
            int mid = i + (j - i) / 2;
            if (endPoints[mid] < value) {
                i = Math.max(mid, i + 1);
            } else if (startPoints[mid] > value) {
                j = Math.min(mid, j - 1);
            } else {
                return true;
            }
        } while (i != j);
        return false;
    }

    public boolean remove(int value) {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Add an integer to the set
     * @param value the integer to be added
     * @return true if the integer was added, false if it was already present
     */
    public boolean add(int value) {
        hashCode = -1;
        if (used == 0) {
            ensureCapacity(1);
            startPoints[used - 1] = value;
            endPoints[used - 1] = value;
            size++;
            return true;
        }
        if (value > endPoints[used - 1]) {
            if (value == endPoints[used - 1] + 1) {
                endPoints[used - 1]++;
            } else {
                ensureCapacity(used + 1);
                startPoints[used - 1] = value;
                endPoints[used - 1] = value;
            }
            size++;
            return true;
        }
        if (value < startPoints[0]) {
            if (value == startPoints[0] - 1) {
                startPoints[0]--;
            } else {
                ensureCapacity(used + 1);
                System.arraycopy(startPoints, 0, startPoints, 1, used - 1);
                System.arraycopy(endPoints, 0, endPoints, 1, used - 1);
                startPoints[0] = value;
                endPoints[0] = value;
            }
            size++;
            return true;
        }
        int i = 0;
        int j = used;
        do {
            int mid = i + (j - i) / 2;
            if (endPoints[mid] < value) {
                i = Math.max(mid, i + 1);
            } else if (startPoints[mid] > value) {
                j = Math.min(mid, j - 1);
            } else {
                return false;
            }
        } while (i != j);
        if (i > 0 && endPoints[i - 1] + 1 == value) {
            i--;
        } else if (i < used - 1 && startPoints[i + 1] - 1 == value) {
            i++;
        }
        if (endPoints[i] + 1 == value) {
            if (value == startPoints[i + 1] - 1) {
                endPoints[i] = endPoints[i + 1];
                System.arraycopy(startPoints, i + 2, startPoints, i + 1, used - i - 2);
                System.arraycopy(endPoints, i + 2, endPoints, i + 1, used - i - 2);
                used--;
            } else {
                endPoints[i]++;
            }
            size++;
            return true;
        } else if (startPoints[i] - 1 == value) {
            if (value == endPoints[i - 1] + 1) {
                endPoints[i - 1] = endPoints[i];
                System.arraycopy(startPoints, i + 1, startPoints, i, used - i - 1);
                System.arraycopy(endPoints, i + 1, endPoints, i, used - i - 1);
                used--;
            } else {
                startPoints[i]--;
            }
            size++;
            return true;
        } else {
            if (value > endPoints[i]) {
                i++;
            }
            ensureCapacity(used + 1);
            try {
                System.arraycopy(startPoints, i, startPoints, i + 1, used - i - 1);
                System.arraycopy(endPoints, i, endPoints, i + 1, used - i - 1);
            } catch (Exception err) {
                err.printStackTrace();
            }
            startPoints[i] = value;
            endPoints[i] = value;
            size++;
            return true;
        }
    }

    private void ensureCapacity(int n) {
        if (startPoints.length < n) {
            int[] s = new int[startPoints.length * 2];
            int[] e = new int[startPoints.length * 2];
            System.arraycopy(startPoints, 0, s, 0, used);
            System.arraycopy(endPoints, 0, e, 0, used);
            startPoints = s;
            endPoints = e;
        }
        used = n;
    }

    /**
     * Get an iterator over the values
     */
    public IntIterator iterator() {
        return new IntRangeSetIterator();
    }

    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(used * 8);
        for (int i = 0; i < used; i++) {
            sb.append(startPoints[i] + "-" + endPoints[i] + ",");
        }
        return sb.toString();
    }

    /**
     * Test whether this set has exactly the same members as another set. Note that
     * IntRangeSet values are <b>NOT</b> comparable with other implementations of IntSet
     */
    public boolean equals(Object other) {
        if (other instanceof IntRangeSet) {
            return used == ((IntRangeSet) other).used && Arrays.equals(startPoints, ((IntRangeSet) other).startPoints) && Arrays.equals(endPoints, ((IntRangeSet) other).endPoints);
        }
        return containsAll((IntSet) other);
    }

    /**
     * Construct a hash key that supports the equals() test
     */
    public int hashCode() {
        if (hashCode == -1) {
            int h = 0x836a89f1;
            for (int i = 0; i < used; i++) {
                h ^= startPoints[i] + (endPoints[i] << 3);
            }
            hashCode = h;
        }
        return hashCode;
    }

    /**
     * Test if this set is a superset of another set
     */
    public boolean containsAll(IntSet other) {
        IntIterator it = other.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a range of integers to the set.
     * This is optimized for the case where these are all greater than any existing integer
     * in the set.
     * @param low the low end of the new range
     * @param high the high end of the new range
     */
    public void addRange(int low, int high) {
        hashCode = -1;
        if (used == 0) {
            ensureCapacity(1);
            startPoints[used - 1] = low;
            endPoints[used - 1] = high;
            size += (high - low + 1);
        } else if (low > endPoints[used - 1]) {
            if (low == endPoints[used - 1] + 1) {
                endPoints[used - 1] = high;
            } else {
                ensureCapacity(used + 1);
                startPoints[used - 1] = low;
                endPoints[used - 1] = high;
            }
            size += (high - low + 1);
        } else {
            for (int i = low; i <= high; i++) {
                add(i);
            }
        }
    }

    /**
     * Get the start points of the ranges
     */
    public int[] getStartPoints() {
        return startPoints;
    }

    /**
     * Get the end points of the ranges
     */
    public int[] getEndPoints() {
        return endPoints;
    }

    /**
     * Get the number of ranges actually in use
     */
    public int getNumberOfRanges() {
        return used;
    }

    /**
     * Iterator class
     */
    private class IntRangeSetIterator implements IntIterator, Serializable {

        private int i = 0;

        private int current = 0;

        public IntRangeSetIterator() {
            i = -1;
            current = Integer.MIN_VALUE;
        }

        public boolean hasNext() {
            if (i < 0) {
                return size > 0;
            } else {
                return current < endPoints[used - 1];
            }
        }

        public int next() {
            if (i < 0) {
                i = 0;
                current = startPoints[0];
                return current;
            }
            if (current == endPoints[i]) {
                current = startPoints[++i];
                return current;
            } else {
                return ++current;
            }
        }
    }
}
