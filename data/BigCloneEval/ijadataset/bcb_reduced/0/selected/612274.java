package net.jini.core.constraint;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An immutable set backed by an array. Any attempts to modify the set result in
 * UnsupportedOperationException being thrown.
 * 
 * @author Sun Microsystems, Inc.
 * 
 */
final class ArraySet implements Set {

    /**
	 * The array.
	 */
    private final Object[] elements;

    /**
	 * Creates an instance from an array. The array is not copied.
	 */
    ArraySet(Object[] elements) {
        this.elements = elements;
    }

    public int size() {
        return elements.length;
    }

    public boolean isEmpty() {
        return elements.length == 0;
    }

    public boolean contains(Object o) {
        for (int i = elements.length; --i >= 0; ) {
            if (elements[i].equals(o)) {
                return true;
            }
        }
        return false;
    }

    public Iterator iterator() {
        return new Iter();
    }

    /**
	 * Simple iterator.
	 */
    private final class Iter implements Iterator {

        /**
		 * Index into the array.
		 */
        private int idx = 0;

        Iter() {
        }

        public boolean hasNext() {
            return idx < elements.length;
        }

        public Object next() {
            if (idx < elements.length) {
                return elements[idx++];
            }
            throw new NoSuchElementException();
        }

        /**
		 * Always throws UnsupportedOperationException.
		 */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Object[] toArray() {
        Object[] a = new Object[elements.length];
        System.arraycopy(elements, 0, a, 0, elements.length);
        return a;
    }

    public Object[] toArray(Object a[]) {
        if (a.length < elements.length) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(), elements.length);
        }
        System.arraycopy(elements, 0, a, 0, elements.length);
        if (a.length > elements.length) {
            a[elements.length] = null;
        }
        return a;
    }

    /**
	 * Always throws UnsupportedOperationException.
	 */
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
	 * Always throws UnsupportedOperationException.
	 */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection c) {
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            if (!contains(iter.next())) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Always throws UnsupportedOperationException.
	 */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
	 * Always throws UnsupportedOperationException.
	 */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
	 * Always throws UnsupportedOperationException.
	 */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
	 * Always throws UnsupportedOperationException.
	 */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
        return (this == o || (o instanceof Set && ((Collection) o).size() == elements.length && containsAll((Collection) o)));
    }

    public int hashCode() {
        return Constraint.hash(elements);
    }

    public String toString() {
        return Constraint.toString(elements);
    }
}
