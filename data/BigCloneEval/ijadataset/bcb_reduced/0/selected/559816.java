package gov.llnl.text.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author David Buttler
 *
 */
public class Ring<E> implements Collection<E> {

    E[] mArray;

    int first = -1;

    int last = -1;

    int mSize;

    /**
 * create a ring of a given size
 * @param size
 */
    @SuppressWarnings("unchecked")
    public Ring(int size) {
        mArray = (E[]) new Object[size];
        mSize = size;
    }

    /**
 * Add an element to the ring
 * @param val
 */
    public boolean add(E val) {
        last = (last + 1) % mSize;
        if (last == first) first = (first + 1) % mSize;
        mArray[last] = val;
        if (first < 0) {
            first = 0;
        }
        return true;
    }

    public E get(int i) {
        if (i >= size()) throw new IndexOutOfBoundsException("index " + i + " is larger than ring size: " + size());
        int index = (first + i) % mSize;
        return mArray[index];
    }

    /**
 * Get the first item from the right
 * @return
 */
    public E getFirst() {
        if (first < 0) {
            throw new IndexOutOfBoundsException("Ring is empty");
        }
        return mArray[first];
    }

    /**
 * Get the last item from the ring
 * @return
 */
    public E getLast() {
        if (last < 0) {
            throw new IndexOutOfBoundsException("Ring is empty");
        }
        return mArray[last];
    }

    public boolean addAll(Collection<? extends E> c) {
        for (E i : c) {
            add(i);
        }
        return true;
    }

    public void clear() {
        first = -1;
        last = -1;
    }

    public boolean contains(Object o) {
        for (E i : this) {
            if (o.equals(i)) return true;
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Iterator<E> iterator() {
        RingIterator<E> i = new RingIterator<E>(this);
        return i;
    }

    public boolean remove(Object o) {
        throw new RuntimeException("sorry not implemented");
    }

    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("sorry not implemented");
    }

    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("sorry not implemented");
    }

    public int size() {
        if (first < 0) {
            return 0;
        }
        if ((last + 1) % mSize == first) return mSize;
        return last - first + 1;
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        E[] r = (E[]) new Object[size()];
        int i = 0;
        for (E e : this) {
            r[i++] = e;
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
        E[] elementData = (E[]) toArray();
        System.arraycopy(elementData, 0, a, 0, size());
        if (a.length > size()) a[size()] = null;
        return a;
    }

    class RingIterator<T> implements Iterator<T> {

        private Ring<T> mRing;

        int mIndex = 0;

        public RingIterator(Ring<T> r) {
            mRing = r;
            mIndex = 0;
        }

        public boolean hasNext() {
            return mIndex < mRing.size();
        }

        public T next() {
            if (hasNext()) {
                return mRing.get(mIndex++);
            }
            return null;
        }

        public void remove() {
            throw new RuntimeException("Sorry, not implemented");
        }
    }
}
