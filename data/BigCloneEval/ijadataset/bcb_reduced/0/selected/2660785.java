package nts.tfm;

import java.util.Vector;

/**
 * |IndexMultimap| can store and retrieve |int| values associated to
 * particular |int| key. There can be more values associated to the same key.
 * This class can be replaced by any generic associative container which
 * provides one to many mapping.
 */
public class IndexMultimap {

    /**
     * The internal representation of (key, value) pair.
     */
    protected static class Pair {

        /** The key */
        int key;

        /** The value */
        int val;

        /**
	 * Makes new |Pair| with given key and value.
	 * @param	k the key
	 * @param	v the value
	 */
        Pair(int k, int v) {
            key = k;
            val = v;
        }
    }

    /** Internal storage of (key, value) pairs */
    private Vector data = new Vector();

    /**
     * The number of (key, value) pairs kept.
     * @return	the number of stored pairs.
     */
    protected final int size() {
        return data.size();
    }

    /**
     * (key, value) pair at given position.
     * @param	i the position of pair to be examined.
     * @return	the pair at given position.
     */
    protected final Pair at(int i) {
        return (Pair) data.elementAt(i);
    }

    /**
     * Insert a (key, value) pair at the given position.
     * @param	i the pair to be inserted.
     * @param	i the position to be inserted to.
     */
    protected final void insert(Pair p, int i) {
        data.insertElementAt(p, i);
    }

    /**
     * Gives the position where a (key, value) pair with given key is stored
     * or where it should be stored if there is no such pair.
     * @param	key the key searched for.
     * @return	the position.
     */
    protected final int search(int key) {
        int beg = 0;
        int end = size();
        while (beg < end) {
            int med = (beg + end) / 2;
            Pair p = at(med);
            if (key < p.key) end = med; else if (key > p.key) beg = med + 1; else return med;
        }
        return beg;
    }

    /**
     * Adds a new (key, value) pair.
     * @param	key the key of the new pair.
     * @param	val the value of the new pair.
     */
    public void add(int key, int val) {
        synchronized (data) {
            int pos = search(key);
            while (pos < size() && at(pos).key == key) pos++;
            insert(new Pair(key, val), pos);
        }
    }

    /**
     * Class |Enum| provides the sequence of all values associated to
     * particular key.
     */
    public class Enum {

        /** the current position in the sequence of pairs */
        private int pos;

        /** the key for which the values are required */
        private final int key;

        private Enum(int k) {
            synchronized (data) {
                key = k;
                pos = search(key);
                while (pos > 0 && at(pos - 1).key == key) pos--;
            }
        }

        /**
	 * Tests if there is another associated value.
	 * @return	|true| if next value is available, |false| otherwise.
	 */
        public final boolean hasMore() {
            return (pos < size() && at(pos).key == key);
        }

        /**
	 * Gives the next value from the sequence of associated values.
	 * @return	the next value.
	 */
        public final int next() {
            return at(pos++).val;
        }
    }

    /**
     * Gives the sequence of all keys associated to the given key.
     * @param	key the given key.
     * @return	the object representing the sequence of associated values.
     */
    public Enum forKey(int key) {
        return new Enum(key);
    }
}
