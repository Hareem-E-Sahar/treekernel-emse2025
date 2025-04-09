package net.sf.salmon.util;

import java.util.*;

/**
 * <p>
 * An array-based implementation of priority queue.
 * </p>
 * 
 * @author Cedric Shih
 * @version 1.0
 */
public class PriorityQueueImp extends SequenceImp implements PriorityQueue {

    public PriorityQueueImp() {
    }

    public PriorityQueueImp(Enumeration enumeration) throws UncomparableException {
        while (enumeration.hasMoreElements()) {
            addElement((Comparable) enumeration.nextElement());
        }
    }

    public PriorityQueueImp(Vector vector) throws UncomparableException {
        this(vector.elements());
    }

    public void addElement(Comparable comparable) throws UncomparableException {
        if (seq.isEmpty()) {
            seq.addElement(comparable);
        } else if (comparable.compareTo((Comparable) seq.lastElement()) > 0) {
            seq.addElement(comparable);
        } else {
            int i = 0;
            for (Comparable comparable1 = (Comparable) elementAt(0); comparable.compareTo(comparable1) > 0; comparable1 = (Comparable) elementAt(i)) {
                i++;
            }
            seq.insertElementAt(comparable, i);
        }
    }

    public Object binarySearch(String s) {
        if (s == null) {
            return null;
        }
        return binarySearch(s, 0, size());
    }

    private Object binarySearch(String s, int i, int j) {
        try {
            if (i > j) {
                return null;
            }
            int k = (i + j) / 2;
            if (s.equals(elementAt(k).toString())) {
                return elementAt(k);
            }
            if (s.compareTo(elementAt(k).toString()) < 0) {
                return binarySearch(s, i, k - 1);
            }
            return binarySearch(s, k + 1, j);
        } catch (IndexOutOfBoundsException indexoutofboundsexception) {
            return null;
        }
    }

    public Object elementAt(int i) {
        return seq.elementAt(i);
    }

    public void insertElementAt(Object obj, int i) {
    }

    public void insertFirst(Object obj) {
    }

    public void insertLast(Object obj) {
    }

    public int size() {
        return super.size();
    }

    public void swap(int i, int j) throws IndexOutOfBoundsException {
    }
}
