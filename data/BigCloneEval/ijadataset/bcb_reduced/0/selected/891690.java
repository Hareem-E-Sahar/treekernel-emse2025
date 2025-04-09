package com.objectwave.utility;

import java.util.*;

/**
 * This is a vector implementation that stores the objects in an ordered way.
 * Access to objects can be in logrithmic time.
 * @author $Author: dave_hoag $
 * @version $Date: 2005/02/21 02:56:28 $ $Revision: 2.1 $
 */
public class OrderedVector {

    private Object[] array;

    private int size = 0;

    private SorterComparisonIF comparison = null;

    private static class OrderedVectorEnumerator implements Enumeration {

        private OrderedVector ov;

        private int idx = 0;

        public OrderedVectorEnumerator(OrderedVector ov) {
            this.ov = ov;
        }

        public Object nextElement() {
            return (idx >= ov.size()) ? null : ov.array[idx++];
        }

        public boolean hasMoreElements() {
            return (idx < ov.size());
        }
    }

    /**
	 * Get may seem like an odd function...I mean, if you have the object for the get
	 * param, I shouldn't need to get the object.  But wait...If I have a custom
	 * comparisonIF object, than the objec as the parameter may be a different instance
	 * than the one in the Vector.
	 */
    public Object get(Object obj) {
        int i = indexOf(obj);
        if (i < 0) return null;
        return elementAt(i);
    }

    /**
	 */
    public OrderedVector() {
        array = new Object[10];
    }

    public OrderedVector(int n) {
        array = new Object[n];
    }

    public void addElement(Object value) {
        insertElementAt(value, insertPos(value));
    }

    /**
 * 
 * @author Steven Sinclair
 * @return int
 */
    public int capacity() {
        return array.length;
    }

    public void clear() {
        array = new Object[10];
        size = 0;
    }

    /**
	 * @return 0 if objects are equal.
	 */
    private int compare(Object lhs, Object rhs) {
        if (comparison != null) return comparison.compare(lhs, rhs); else return lhs.hashCode() - rhs.hashCode();
    }

    /**
	 * Return true if object is in collection.
	 */
    public boolean contains(Object value) {
        if (size() < 30) {
            for (int i = 0; i < size(); ++i) if (value == array[i]) return true;
            return false;
        }
        return find(value) >= 0;
    }

    /**
	 */
    public Enumeration elements() {
        return new OrderedVectorEnumerator(this);
    }

    /**
     * 
     * @author Steven Sinclair
     * 
     * 
     * @param minCapacity int
     */
    public void ensureCapacity(int minCapacity) {
        if (array.length < minCapacity) {
            Object[] newArray = new Object[minCapacity];
            for (int i = 0; i < array.length; ++i) newArray[i] = array;
            array = newArray;
        }
    }

    private int find(Object value) {
        int here = insertPos(value);
        if (here < 0 || here >= size()) return -1;
        Object found = array[here];
        if (compare(found, value) != 0) return -1;
        if (found == value) return here;
        for (int i = here + 1; i < size(); ++i) {
            found = array[i];
            if (found == value) return i;
            if (compare(found, value) != 0) break;
        }
        for (int i = here - 1; i > -1; --i) {
            found = array[i];
            if (found == value) return i;
            if (compare(found, value) != 0) break;
        }
        return here;
    }

    /**
     * 
     * @author Steven Sinclair
     * @return com.objectwave.utility.SorterComparisonIF
     */
    public SorterComparisonIF getComparison() {
        return comparison;
    }

    /**
     */
    public Object elementAt(int idx) {
        return array[idx];
    }

    /**
     */
    public int indexOf(Object value) {
        return find(value);
    }

    /**
    * 
    * @author Steven Sinclair
    * 
    * 
    * @param element java.lang.Object
    * @param pos int
    */
    private void insertElementAt(Object element, int pos) {
        Object[] array = this.array;
        if (size() + 1 == array.length) {
            array = new Object[size + 10];
            for (int i = 0; i < pos; ++i) array[i] = this.array[i];
        }
        for (int i = size(); i > pos; --i) array[i] = this.array[i - 1];
        array[pos] = element;
        this.array = array;
        ++size;
    }

    /**
     */
    private int insertPos(Object value) {
        if (size() == 0) return 0;
        int begin = 0;
        int end = size();
        for (; ; ) {
            int mid = begin + (end - begin) / 2;
            Object midObj = array[mid];
            int cmp = compare(value, midObj);
            if (cmp == 0) return mid; else if (cmp < 0) {
                if (mid == begin) return begin;
                end = mid;
            } else {
                if (mid == end - 1) return end;
                begin = mid;
            }
        }
    }

    /**
	 */
    public static void main(String args[]) {
        Random rnd = new Random();
        int n = 100;
        try {
            n = Integer.parseInt(args[0]);
        } catch (Throwable ex) {
        }
        testOrderedVector(n, rnd);
        testVector(n, rnd);
    }

    /**
	 */
    public Object remove(Object value) {
        Object res = get(value);
        removeElement(value);
        return res;
    }

    /**
	 */
    public boolean removeElement(Object value) {
        int here = find(value);
        if (here < 0) return false;
        for (int i = here + 1; i < array.length; ++i) array[i - 1] = array[i];
        --size;
        return true;
    }

    /**
    * 
    * @author Steven Sinclair
    * @param newValue com.objectwave.utility.SorterComparisonIF
    */
    public void setComparison(SorterComparisonIF newValue) {
        this.comparison = newValue;
    }

    public int size() {
        return size;
    }

    private static void testOrderedVector(int n, Random rnd) {
        OrderedVector v = new OrderedVector(n);
        System.out.println("Adding " + n + " elements to an ordered vector.");
        int i = 0;
        Integer keys[] = new Integer[n];
        for (i = 0; i < n; ++i) keys[i] = new Integer(Math.abs(rnd.nextInt()));
        Date begin = new Date();
        for (i = 0; i < n; ++i) v.addElement(keys[i]);
        Date end = new Date();
        long ms = end.getTime() - begin.getTime();
        System.out.println(n + " ordered vector insertions: " + ms + "ms");
        System.out.println("Testing element access time (find every element 500 times)...");
        begin = new Date();
        for (int ii = 0; ii < 500; ++ii) {
            for (i = 0; i < n; ++i) {
                v.contains(keys[i]);
            }
        }
        end = new Date();
        System.out.println("Finished access timing: accessed " + (n * 500) + " elements in " + (end.getTime() - begin.getTime()) + "ms.");
        System.out.println("Testing key ordering...");
        Object lastValue = null;
        Enumeration enumer = v.elements();
        while (enumer.hasMoreElements()) {
            Object key = enumer.nextElement();
            if (lastValue != null && key.hashCode() < lastValue.hashCode()) {
                System.out.println("*** Value @ idx " + i + " is less than that at " + (i - 1) + ":");
                System.out.println("***    [ ... " + lastValue.hashCode() + ", " + key.hashCode() + " ... ]");
            }
            lastValue = key;
        }
        System.out.println("Finished testing key ordering.");
    }

    private static void testVector(int n, Random rnd) {
        Vector v = new Vector(n);
        System.out.println("Adding " + n + " elements to an vector.");
        int i = 0;
        Integer keys[] = new Integer[n];
        for (i = 0; i < n; ++i) keys[i] = new Integer(Math.abs(rnd.nextInt()));
        Date begin = new Date();
        for (i = 0; i < n; ++i) v.addElement(keys[i]);
        Date end = new Date();
        long ms = end.getTime() - begin.getTime();
        System.out.println(n + " vector insertions: " + ms + "ms");
        System.out.println("Testing element access time (find every element 500 times)...");
        begin = new Date();
        for (int ii = 0; ii < 500; ++ii) {
            for (i = 0; i < n; ++i) {
                v.contains(keys[i]);
            }
        }
        end = new Date();
        System.out.println("Finished access timing: accessed " + (n * 500) + " elements in " + (end.getTime() - begin.getTime()) + "ms.");
    }

    public Vector toVector() {
        Vector result = new Vector(size());
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            result.addElement(e.nextElement());
        }
        return result;
    }

    /**
 * 
 * @author Steven Sinclair
 * 
 * 
 */
    public void trimToSize() {
        if (size != array.length) {
            Object[] newArray = new Object[size];
            for (int i = 0; i < size(); ++i) newArray[i] = array[i];
            array = newArray;
        }
    }
}
