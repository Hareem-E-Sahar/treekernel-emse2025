package jopt.csp.util;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.ArrayIntList;

/**
 * A map that uses integer keys and double values
 */
public class IntToDoubleMap implements Cloneable {

    protected ArrayIntList keys;

    protected ArrayDoubleList values;

    /**
     *  Creates a new map
     */
    public IntToDoubleMap() {
        keys = new ArrayIntList();
        values = new ArrayDoubleList();
    }

    /**
     * Copy constructor
     * @param im    IntMap to be copied.
     */
    public IntToDoubleMap(IntToDoubleMap im) {
        keys = new ArrayIntList(im.keys);
        values = new ArrayDoubleList(im.values);
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
        keys.clear();
        values.clear();
    }

    /**
     *  Returns index of a key in the list
     */
    private int indexOfKey(int key) {
        if (keys.size() == 0) return -1;
        int lowIdx = 0;
        int highIdx = keys.size() - 1;
        while (lowIdx <= highIdx) {
            int idx = (lowIdx + highIdx) / 2;
            int k = keys.get(idx);
            if (k == key) return idx;
            if (key < k) highIdx = idx - 1; else lowIdx = idx + 1;
        }
        return -(lowIdx + 1);
    }

    /**
     * Adds a key to set
     */
    public void put(int key, double val) {
        int idx = indexOfKey(key);
        if (idx < 0) {
            idx = -idx - 1;
            keys.add(idx, key);
            values.add(idx, val);
        } else {
            values.set(idx, val);
        }
    }

    /**
     * Removes a key from the map
     */
    public void remove(int key) {
        int idx = indexOfKey(key);
        if (idx >= 0) {
            keys.removeElement(key);
            values.removeElementAt(idx);
        }
    }

    /**
     * Returns the value associated with a key
     * 
     * @param key			Key to retrieve associated value
     * @param defaultValue	Value to return if key is not in map
     */
    public double get(int key, double defaultValue) {
        int idx = indexOfKey(key);
        if (idx >= 0) return values.get(idx); else return defaultValue;
    }

    /**
     * Returns true if this map contains a given key
     * 
     * @param key	Key to verify existence
     */
    public boolean containsKey(int key) {
        int idx = indexOfKey(key);
        if (idx >= 0) return true; else return false;
    }

    /**
     * Returns an array of the keys currently in this map.
     * The returned array is independent of the actual keys.
     * 
     * @return int[] of keys
     */
    public int[] keySet() {
        return keys.toArray();
    }

    public Object clone() {
        return new IntToDoubleMap(this);
    }
}
