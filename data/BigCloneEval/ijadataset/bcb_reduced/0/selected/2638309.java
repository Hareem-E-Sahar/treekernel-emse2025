package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * A PriorityQueue holds elements on a priority heap, which orders the elements
 * according to their natural order or according to the comparator specified at
 * construction time. If the queue uses natural ordering, only elements that are
 * comparable are permitted to be inserted into the queue.
 * <p>
 * The least element of the specified ordering is stored at the head of the
 * queue and the greatest element is stored at the tail of the queue.
 * <p>
 * A PriorityQueue is not synchronized. If multiple threads will have to access
 * it concurrently, use the {@link java.util.concurrent.PriorityBlockingQueue}.
 */
public class PriorityQueue<E> extends AbstractQueue<E> implements Serializable {

    private static final long serialVersionUID = -7720805057305804111L;

    private static final int DEFAULT_CAPACITY = 11;

    private static final double DEFAULT_INIT_CAPACITY_RATIO = 1.1;

    private static final int DEFAULT_CAPACITY_RATIO = 2;

    private int size;

    private Comparator<? super E> comparator;

    private transient E[] elements;

    /**
     * Constructs a priority queue with an initial capacity of 11 and natural
     * ordering.
     */
    public PriorityQueue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a priority queue with the specified capacity and natural
     * ordering.
     * 
     * @param initialCapacity
     *            the specified capacity.
     * @throws IllegalArgumentException
     *             if the initialCapacity is less than 1.
     */
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Constructs a priority queue with the specified capacity and comparator.
     * 
     * @param initialCapacity
     *            the specified capacity.
     * @param comparator
     *            the specified comparator. If it is null, the natural ordering
     *            will be used.
     * @throws IllegalArgumentException
     *             if the initialCapacity is less than 1.
     */
    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        elements = newElementArray(initialCapacity);
        this.comparator = comparator;
    }

    /**
     * Constructs a priority queue that contains the elements of a collection.
     * The constructed priority queue has the initial capacity of 110% of the
     * size of the collection. The queue uses natural ordering to order its
     * elements.
     * 
     * @param c
     *            the collection whose elements will be added to the priority
     *            queue to be constructed.
     * @throws ClassCastException
     *             if any of the elements in the collection are not comparable.
     * @throws NullPointerException
     *             if any of the elements in the collection are null.
     */
    public PriorityQueue(Collection<? extends E> c) {
        if (c instanceof PriorityQueue) {
            getFromPriorityQueue((PriorityQueue<? extends E>) c);
        } else if (c instanceof SortedSet) {
            getFromSortedSet((SortedSet<? extends E>) c);
        } else {
            initSize(c);
            addAll(c);
        }
    }

    /**
     * Constructs a priority queue that contains the elements of another
     * priority queue. The constructed priority queue has the initial capacity
     * of 110% of the specified one. Both priority queues have the same
     * comparator.
     * 
     * @param c
     *            the priority queue whose elements will be added to the
     *            priority queue to be constructed.
     */
    public PriorityQueue(PriorityQueue<? extends E> c) {
        getFromPriorityQueue(c);
    }

    /**
     * Constructs a priority queue that contains the elements of a sorted set.
     * The constructed priority queue has the initial capacity of 110% of the
     * size of the sorted set. The priority queue will have the same comparator
     * as the sorted set.
     * 
     * @param c
     *            the sorted set whose elements will be added to the priority
     *            queue to be constructed.
     */
    public PriorityQueue(SortedSet<? extends E> c) {
        getFromSortedSet(c);
    }

    /**
     * Gets the iterator of the priority queue, which will not return elements
     * in any specified ordering.
     * 
     * @return the iterator of the priority queue.
     */
    @Override
    public Iterator<E> iterator() {
        return new PriorityIterator();
    }

    /**
     * Gets the size of the priority queue. If the size of the queue is greater
     * than the Integer.MAX, then it returns Integer.MAX.
     * 
     * @return the size of the priority queue.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Removes all the elements of the priority queue.
     */
    @Override
    public void clear() {
        Arrays.fill(elements, null);
        size = 0;
    }

    /**
     * Inserts the element to the priority queue.
     * 
     * @param o
     *            the element to add to the priority queue.
     * @return always true
     * @throws ClassCastException
     *             if the element cannot be compared with the elements in the
     *             priority queue using the ordering of the priority queue.
     * @throws NullPointerException
     *             if {@code o} is {@code null}.
     */
    public boolean offer(E o) {
        if (null == o) {
            throw new NullPointerException();
        }
        growToSize(size + 1);
        elements[size] = o;
        siftUp(size++);
        return true;
    }

    /**
     * Gets and removes the head of the queue.
     * 
     * @return the head of the queue or null if the queue is empty.
     */
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E result = elements[0];
        removeAt(0);
        return result;
    }

    /**
     * Gets but does not remove the head of the queue.
     * 
     * @return the head of the queue or null if the queue is empty.
     */
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return elements[0];
    }

    /**
     * Gets the comparator of the priority queue.
     * 
     * @return the comparator of the priority queue or null if the natural
     *         ordering is used.
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * Removes the specified object from the priority queue.
     * 
     * @param o
     *            the object to be removed.
     * @return true if the object was in the priority queue, false if the object
     *         was not in the priority queue.
     */
    @Override
    public boolean remove(Object o) {
        if (o == null || size == 0) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (o.equals(elements[i])) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the specified object to the priority queue.
     * 
     * @param o
     *            the object to be added.
     * @return always true.
     * @throws ClassCastException
     *             if the element cannot be compared with the elements in the
     *             priority queue using the ordering of the priority queue.
     * @throws NullPointerException
     *             if {@code o} is {@code null}.
     */
    @Override
    public boolean add(E o) {
        return offer(o);
    }

    /**
     * Answers if there is an element in this queue equals to the object.
     * 
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object object) {
        if (object == null) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (object.equals(elements[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all the elements in an array. The result is a copy of all the
     * elements.
     * 
     * @return the Array of all the elements
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Object[] toArray() {
        return newArray(new Object[size()]);
    }

    /**
     * Returns all the elements in an array, and the type of the result array is
     * the type of the argument array. If the argument array is big enough, the
     * elements from the queue will be stored in it(element immediately
     * following the end of the queue is set to null, if any); otherwise, it
     * will return a new array with the size of the argument array and size of
     * the queue.
     * 
     * @param <T>
     *            the type of elements in the array
     * @param array
     *            the array stores all the elements from the queue, if it has
     *            enough space; otherwise, a new array of the same type and the
     *            size of the queue will be used
     * @return the Array of all the elements
     * @throws ArrayStoreException
     *             if the type of the argument array is not compatible with
     *             every element in the queue
     * @throws NullPointerException
     *             if the argument array is null
     * @see java.util.AbstractCollection#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] array) {
        return newArray(array);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] newArray(T[] array) {
        if (size > array.length) {
            Class<?> clazz = array.getClass().getComponentType();
            array = (T[]) Array.newInstance(clazz, size);
        }
        System.arraycopy(elements, 0, array, 0, size);
        if (size < array.length) {
            array[size] = null;
        }
        return array;
    }

    private class PriorityIterator implements Iterator<E> {

        private int currentIndex = -1;

        private boolean allowRemove = false;

        public boolean hasNext() {
            return currentIndex < size - 1;
        }

        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            allowRemove = true;
            return elements[++currentIndex];
        }

        public void remove() {
            if (!allowRemove) {
                throw new IllegalStateException();
            }
            allowRemove = false;
            removeAt(currentIndex--);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int capacity = in.readInt();
        elements = newElementArray(capacity);
        for (int i = 0; i < size; i++) {
            elements[i] = (E) in.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    private E[] newElementArray(int capacity) {
        return (E[]) new Object[capacity];
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(elements.length);
        for (int i = 0; i < size; i++) {
            out.writeObject(elements[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private void getFromPriorityQueue(PriorityQueue<? extends E> c) {
        initSize(c);
        comparator = (Comparator<? super E>) c.comparator();
        System.arraycopy(c.elements, 0, elements, 0, c.size());
        size = c.size();
    }

    @SuppressWarnings("unchecked")
    private void getFromSortedSet(SortedSet<? extends E> c) {
        initSize(c);
        comparator = (Comparator<? super E>) c.comparator();
        Iterator<? extends E> iter = c.iterator();
        while (iter.hasNext()) {
            elements[size++] = iter.next();
        }
    }

    private void removeAt(int index) {
        size--;
        elements[index] = elements[size];
        siftDown(index);
        elements[size] = null;
    }

    private int compare(E o1, E o2) {
        if (null != comparator) {
            return comparator.compare(o1, o2);
        }
        return ((Comparable<? super E>) o1).compareTo(o2);
    }

    private void siftUp(int childIndex) {
        E target = elements[childIndex];
        int parentIndex;
        while (childIndex > 0) {
            parentIndex = (childIndex - 1) / 2;
            E parent = elements[parentIndex];
            if (compare(parent, target) <= 0) {
                break;
            }
            elements[childIndex] = parent;
            childIndex = parentIndex;
        }
        elements[childIndex] = target;
    }

    private void siftDown(int rootIndex) {
        E target = elements[rootIndex];
        int childIndex;
        while ((childIndex = rootIndex * 2 + 1) < size) {
            if (childIndex + 1 < size && compare(elements[childIndex + 1], elements[childIndex]) < 0) {
                childIndex++;
            }
            if (compare(target, elements[childIndex]) <= 0) {
                break;
            }
            elements[rootIndex] = elements[childIndex];
            rootIndex = childIndex;
        }
        elements[rootIndex] = target;
    }

    private void initSize(Collection<? extends E> c) {
        if (null == c) {
            throw new NullPointerException();
        }
        if (c.isEmpty()) {
            elements = newElementArray(1);
        } else {
            int capacity = (int) Math.ceil(c.size() * DEFAULT_INIT_CAPACITY_RATIO);
            elements = newElementArray(capacity);
        }
    }

    private void growToSize(int size) {
        if (size > elements.length) {
            E[] newElements = newElementArray(size * DEFAULT_CAPACITY_RATIO);
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            elements = newElements;
        }
    }
}
