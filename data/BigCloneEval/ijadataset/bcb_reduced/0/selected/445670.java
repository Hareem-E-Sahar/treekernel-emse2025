package com.wwflgames.za.util;

import java.util.Random;

/**
 * An enlargeable continuous shuffled sequence generator. Basically it's like a <tt>List</tt>,
 * <tt>Collections.shuffle()</tt>, <tt>remove()</tt>, and refilling/reshuffling once it's empty.
 * However, it only uses a single array and the shuffling happens on the go (each <tt>next</tt>
 * operation swapps two elements). Additionally, it's easier to use and less error prone.<p>
 *
 * The <tt>size</tt>, <tt>capacity</tt>, and <tt>next</tt> operations run in constant time. The
 * <tt>add</tt> operation runs in <i>amortized constant time</i> (adding n elements requires O(n)
 * time).<p>
 *
 * Single elements can only reoccur once this pass is finished. So, if an element was only added
 * once it can be at most be drawn twice - at the end of one pass and at the beginning of the next
 * pass.
 *
 * @author  Jos Hirth, kaioa.com
 * @see IntShuffleBag
 * @see ShuffleBag
 */
public class GShuffleBag<E> {

    private Random gen;

    private int capacity;

    private Object[] data;

    private int size = 0;

    private int cursor = -1;

    /**
	 * Constructs an empty bag with an initial capacity of 10 and the default source of randomness.
	 *
	 * @exception IllegalArgumentException if the specified initial capacity is negative
	 */
    public GShuffleBag() {
        this(10, new Random());
    }

    /**
	 * Constructs an empty bag with an initial capacity of 10 and the specified source of randomness.
	 *
	 * @param gen the source of randomness to use to shuffle the bag
	 * @exception IllegalArgumentException if the specified initial capacity is negative
	 */
    public GShuffleBag(Random gen) {
        this(10, gen);
    }

    /**
	 * Constructs an empty bag with the specified initial capacity and the default source of randomness.
	 *
	 * @param capacity the initial capacity of this bag
	 * @exception IllegalArgumentException if the specified initial capacity is negative
     */
    public GShuffleBag(int capacity) {
        this(capacity, new Random());
    }

    /**
	 * Constructs an empty bag with the specified initial capacity and the specified source of randomness.
	 *
	 * @param capacity the initial capacity of this bag
	 * @param gen the source of randomness to use to shuffle this bag
	 * @exception IllegalArgumentException if the specified initial capacity is negative
     */
    public GShuffleBag(int capacity, Random gen) {
        if (capacity < 0) throw new IllegalArgumentException("Illegal Capacity: " + capacity);
        this.capacity = capacity;
        this.gen = gen;
        data = new Object[capacity];
    }

    /**
	 * Puts the specified element into the bag and sets the cursor to the last position.
	 * Resetting the cursor makes elements which were added on the go as likely as older elements.
	 *
	 * @param e element to be added
     */
    public void add(E e) {
        add(e, 1);
    }

    /**
	 * Puts the specified element several times into the bag and sets the cursor to the last position.
	 * Resetting the cursor makes elements which were added on the go as likely as older elements.
	 *
	 * @param e element to be added
	 * @param quantity the quantity
     */
    public void add(E e, int quantity) {
        if (quantity > 0) {
            int pos = size;
            size += quantity;
            if (size > capacity) {
                capacity = (capacity * 3) / 2 + 1;
                if (size > capacity) capacity = size;
                Object[] oldData = data;
                data = new Object[capacity];
                System.arraycopy(oldData, 0, data, 0, size - quantity);
            }
            for (int i = 0; i < quantity; i++) data[pos + i] = e;
            cursor = size - 1;
        } else {
            throw new IllegalArgumentException("quantity < 1");
        }
    }

    /**
	 * Returns the next item from this bag.<p>
	 *
	 * The cursor moves from back to front. If the cursor position is smaller than 1 the element at
	 * index 0 is returned and the curor is reset to size-1.<p>
	 *
	 * Otherwise randomly picked elements from index 0 to the cursor position (inclusive) are
	 * swapped with the element at the cursor position. Afterwards the cursor gets moved one step
	 * and the element at the old cursor position is returned.
	 *
	 * @return the next item from this bag
     */
    public E next() {
        if (cursor < 1) {
            cursor = size - 1;
            return (E) data[0];
        }
        int grab = gen.nextInt(cursor + 1);
        E temp = (E) data[grab];
        data[grab] = data[cursor];
        data[cursor] = temp;
        cursor--;
        return temp;
    }

    /**
	 * Trims the capacity of this <tt>GShuffleBag</tt> instance to be the
	 * bag's current size. Use this to minimize
	 * the storage of a <tt>GShuffleBag</tt> instance.
     */
    public void trimToSize() {
        if (size < capacity) {
            Object[] oldData = data;
            data = new Object[size];
            System.arraycopy(oldData, 0, data, 0, size);
            capacity = size;
        }
    }

    /**
	 * Returns the capacity of this bag.
	 *
	 * @return the capacity of this bag
     */
    public int capacity() {
        return capacity;
    }

    /**
	 * Returns the number of elements in this bag.
	 *
	 * @return the number of elements in this bag
     */
    public int size() {
        return size;
    }
}
