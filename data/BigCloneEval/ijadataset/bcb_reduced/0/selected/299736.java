package de.javatt.tools;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Sorted Collection is a Collection that stores it's member in a sorted
 * order.
 * The Elements will be sorted, using the Quicksort algorithm.
 *
 * @author Matthis Kempa
 */
public class SortedCollection implements Collection {

    /**
     * The Iterator that iterates over the array.
     *
     * @author Matthis Kempa
     */
    private class SCIterator implements Iterator {

        /**
         * Recent iterator position.
         */
        private int myCursor = -1;

        /**
         * Does the Iterator have a next object?
         */
        public boolean hasNext() {
            return (SortedCollection.this.size() > (myCursor + 1));
        }

        /**
         * returns the next object of the Collection.
         */
        public Object next() {
            if (hasNext()) {
                myCursor++;
                return SortedCollection.this.myArray[myCursor];
            } else {
                throw new NoSuchElementException("No next element in Iteration.");
            }
        }

        /**
         * Not supported yet.
         */
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    /**
     * size of the collection.
     */
    private int mySize = 0;

    /**
     * Comparator to find out the correct order or elements.
     */
    private Comparator myComp;

    /**
     * The Collection's data.
     */
    private Object[] myArray = new Object[] {};

    /**
     * Constructor
     */
    public SortedCollection(Comparator comp) {
        super();
        myComp = comp;
    }

    public boolean add(Object o) {
        if (myArray.length == mySize) {
            enlargeArray(10);
        }
        myArray[size()] = o;
        mySize++;
        quicksort(0, mySize - 1);
        return true;
    }

    public boolean addAll(Collection coll) {
        int spaceleft = myArray.length - mySize;
        if (spaceleft < coll.size()) {
            enlargeArray(coll.size());
        }
        for (Iterator it = coll.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (!add(obj)) {
                return false;
            }
        }
        return true;
    }

    public boolean remove(Object o) {
        synchronized (myArray) {
            boolean removed = false;
            Object[] newArray = new Object[myArray.length - 1];
            for (int i = 0; i < myArray.length; i++) {
                if (!(myArray[i] == o)) {
                    newArray[i] = myArray[i];
                } else {
                    removed = true;
                }
            }
            if (removed) {
                myArray = newArray;
                mySize--;
            }
            return removed;
        }
    }

    public int size() {
        return mySize;
    }

    public Object[] toArray() {
        synchronized (myArray) {
            Object[] copyArray = new Object[myArray.length];
            for (int i = 0; i < myArray.length; i++) {
                Object toCopy = myArray[i];
                copyArray[i] = toCopy;
            }
            return copyArray;
        }
    }

    public Object[] toArray(Object[] array) {
        return toArray();
    }

    public boolean contains(Object o) {
        synchronized (myArray) {
            for (int i = 0; i < myArray.length; i++) {
                if (myArray[i] == o) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean removeAll(Collection coll) {
        for (Iterator it = coll.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (!remove(obj)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAll(Collection coll) {
        for (Iterator it = coll.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (!contains(obj)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        synchronized (myArray) {
            myArray = new Object[] {};
            mySize = 0;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sorts all elements in the Collection, using the Quick sort
     * algorithm.
     * 
     * @param leftIndex where to start sorting (index of the array)
     * @param rightIndex where to stop sorting (index of the array)
     */
    private void quicksort(int leftIndex, int rightIndex) {
        int leftToRightIndex;
        int rightToLeftIndex;
        Object leftToRightValue;
        Object rightToLeftValue;
        int pivotIndex;
        Object pivotValue;
        int newLeftIndex;
        int newRightIndex;
        leftToRightIndex = leftIndex;
        rightToLeftIndex = rightIndex;
        pivotIndex = (leftToRightIndex + rightToLeftIndex) / 2;
        pivotValue = myArray[pivotIndex];
        newLeftIndex = rightIndex + 1;
        newRightIndex = leftIndex - 1;
        while ((newRightIndex + 1) < newLeftIndex) {
            leftToRightValue = myArray[leftToRightIndex];
            while (leftToRightIndex < newLeftIndex & myComp.compare(leftToRightValue, pivotValue) < 0) {
                newRightIndex = leftToRightIndex;
                leftToRightIndex++;
                leftToRightValue = myArray[leftToRightIndex];
            }
            rightToLeftValue = myArray[rightToLeftIndex];
            while (newRightIndex <= rightToLeftIndex & myComp.compare(rightToLeftValue, pivotValue) > 0) {
                newLeftIndex = rightToLeftIndex;
                rightToLeftIndex--;
                rightToLeftValue = myArray[rightToLeftIndex];
            }
            if (leftToRightIndex == rightToLeftIndex) {
                newRightIndex = leftToRightIndex;
            } else if (leftToRightIndex < rightToLeftIndex) {
                if (myComp.compare(leftToRightValue, rightToLeftValue) >= 0) {
                    Object temp = leftToRightValue;
                    myArray[leftToRightIndex] = rightToLeftValue;
                    myArray[rightToLeftIndex] = temp;
                    newLeftIndex = rightToLeftIndex;
                    newRightIndex = leftToRightIndex;
                    leftToRightIndex++;
                    rightToLeftIndex--;
                }
            }
        }
        if (leftIndex < newRightIndex) {
            quicksort(leftIndex, newRightIndex);
        }
        if (newLeftIndex < rightIndex) {
            quicksort(newLeftIndex, rightIndex);
        }
    }

    public Iterator iterator() {
        return new SCIterator();
    }

    /**
     * Creates a copy of the data array which has additional space for
     * adding elements.
     * @param additionalSpace the number of spaces in the array.
     */
    private void enlargeArray(int additionalSpace) {
        Object[] newArray = new Object[myArray.length + additionalSpace];
        synchronized (myArray) {
            for (int i = 0; i < mySize - 1; i++) {
                Object toCopy = myArray[i];
                newArray[i] = toCopy;
            }
            myArray = newArray;
        }
    }
}
