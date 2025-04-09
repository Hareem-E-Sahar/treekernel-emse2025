package org.systemsbiology.util;

import java.util.Iterator;
import java.util.Vector;

public class OrderedVector {

    /**
     * The vector of values.  Values are stored in increasing order
     */
    protected Vector data;

    /**
     * Construct an empty ordered vector
     *
     * @post constructs an empty, ordered vector
     */
    public OrderedVector() {
        data = new Vector();
    }

    public Vector getVector() {
        return data;
    }

    /**
     * Add a comparable value to an ordered vector
     *
     * @pre value is non-null
     * @post inserts value, leaves vector in order
     * 
     * @param value The comparable value to be added to the ordered vector
     */
    public void add(Object value) {
        int position = indexOf((Comparable) value);
        data.add(position, value);
    }

    /**
     * Determine if a comparable value is a member of the ordered vector
     *
     * @pre value is non-null
     * @post returns true if the value is in the vector
     * 
     * @param value The comparable value sought
     * @return True if the value is found within the ordered vector
     */
    public boolean contains(Object value) {
        int position = indexOf((Comparable) value);
        if (!(position < size())) return false; else return (checkEqualityObjects(data.get(position), value));
    }

    public boolean checkEqualityObjects(Object a, Object b) {
        return (a.toString().equals(b.toString()));
    }

    /**
     * Remove a comparable value from an ordered vector
     * At most one value is removed
     *
     * @pre value is non-null
     * @post removes one instance of value, if found in vector
     * 
     * @param value The comparable value to be removed
     * @return The actual comparable removed
     */
    public Object remove(Object value) {
        if (contains(value)) {
            int position = indexOf((Comparable) value);
            Object target = data.get(position);
            data.remove(position);
            return target;
        }
        return null;
    }

    /**
     * Determine if the ordered vector is empty.	
     *
     * @post returns true if the OrderedVector is empty
     * 
     * @return True iff the ordered vector is empty
     */
    public boolean isEmpty() {
        return data.size() == 0;
    }

    /**
     * Removes all the values from a an ordered vector
     *
     * @post vector is emptied
     */
    public void clear() {
        data.setSize(0);
    }

    /**
     * Determine the number of elements within the ordered vector
     *
     * @post returns the number of elements in vector
     * 
     * @return The number of elements within the ordered vector
     */
    public int size() {
        return data.size();
    }

    /**
     * Construct an iterator to traverse the ordered vector in ascending
     * order
     *
     * @post returns an iterator for traversing vector
     * 
     * @return An iterator to traverse the ordered vector
     */
    public Iterator iterator() {
        return data.iterator();
    }

    protected int indexOf(Comparable target) {
        Comparable midValue;
        int low = 0;
        int high = data.size();
        int mid = (low + high) / 2;
        while (low < high) {
            midValue = (Comparable) data.get(mid);
            if (midValue.compareTo(target) < 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
            mid = (low + high) / 2;
        }
        return low;
    }

    /**
     * Construct a string representation of an ordered vector
     *
     * @pre returns string representation of ordered vector
     * 
     * @return The string representing the ordered vector
     */
    public String toString() {
        return "<OrderedVector: " + data + ">";
    }
}
