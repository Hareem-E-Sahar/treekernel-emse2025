package com.tensegrity.palobrowser.util;

/**
 * <code>ArrayListInt</code> implements a java.util.List alike container
 * for efficient storing of a single primitive java-type, avoiding
 * the overhead of storing wrapper objects. The interfaces Collection
 * and List are not implemented intentionally, since this class doesn't
 * work with subtypes of Object. Nevertheless the method signatures and
 * the semantics are the same. An advantage of not implementing the interface
 * is that we don't have to use virtual invocation and can declare methods
 * as final, which gives us superior performance.
 *
 * Things not implemented that can be found in the standard ArrayList include:
 *
 *   <code>public ArrayList(Collection);</code>
 * In addition to the above methods, the provided
 * ArrayListInt.Iterator and ArrayListInt.ListIterator do not
 * have a <code>remove()</code> method. You can use the container
 * structure itself in case you need to remove elements.
 *
 * The iterators will throw the runtime-exception
 * <code>ListModifiedException</code> in case they detect themselves
 * that they were modified since the iterator was instanciated. This is
 * <strong>NOT RELIABLE</strong> in the context of a multithreaded
 * application. Only proper synchronization provides memory-barriers in
 * the java programming language. Thus this is merely a helpful technique
 * that might detect errors in concurrent programming. Only in a serialized
 * programming context, the programmer can rely on the exception being
 * generated each time the array is illegaly modified.
 *
 * @version $Id$
 * @author  S.Rutz
 */
public final class ArrayListInt {

    /** capacity of the arraylist */
    int capacity;

    /** array that stores data, its length is equal to the
     * capacity. */
    int data[];

    /** size of the arraylist, can be less or equal to the
     * capacity. */
    int size;

    /** modification counter which is incremented each time the
     * array changes size or structure. */
    int stamp;

    /**
     * Constructs a new arraylist with the given initial capacity.
     * @param initialcapacity the initial capacity of the array.
     * @exception InvalidArgumentException if the initialcapacity
     * specified is less than 1.
     */
    public ArrayListInt(final int initialcapacity) {
        if (initialcapacity < 1) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.0") + initialcapacity + UtilMessages.getString("ArrayListInt.1"));
        data = new int[initialcapacity];
        capacity = initialcapacity;
        size = 0;
        stamp = 0;
    }

    /**
     * Constructs a new arraylist with a default capacity of 8 
     * elements.
     */
    public ArrayListInt() {
        this(8);
    }

    /**
     * Constructs a new arraylist and fills it with <tt>n</tt> copies
     * of the specified element.
     * @param n number of entries to fill with the default element
     * @param element default element
     */
    public ArrayListInt(int n, int element) {
        this(n == 0 ? 1 : n);
        for (int i = 0; i < n; ++i) {
            add(element);
        }
    }

    /**
     * Constructs a new arraylist and initializes the elements
     * with the contents from the array passed as argument.
     * The capacity of the arraylist is set to exactly the length
     * of the source array.
     * @param array the array from which the arraylist is initialized.
     */
    public ArrayListInt(final int array[]) {
        this(Math.max(1, array.length));
        System.arraycopy(array, 0, this.data, 0, array.length);
        this.size = array.length;
    }

    /**
     * Internal resize method
     */
    private final void resize(final int newcapacity) {
        if (capacity != newcapacity) {
            int newdata[] = new int[newcapacity];
            System.arraycopy(data, 0, newdata, 0, size);
            this.capacity = newcapacity;
            this.data = newdata;
            ++stamp;
        }
    }

    /**
     * Trims this array's capacity to its actual size. This means
     * the internal storage is adjusted to be just large enough
     * hold as many elements as there are in the arraylist.
     * The lower limit on the array's capacity is 1. Invoking
     * this method on an empty list will not reduce the internal
     * capacity below the threshold value of 1.
     */
    public final void trimToSize() {
        resize(Math.max(1, size));
    }

    /**
     * Restructure this arraylist so that its internal capacity
     * is equal or larger than the passed argument.
     * @param newcapacity the new capacity of the arraylist.
     * Specifying a negative cacacity will cause this method to have
     * no effect.
     */
    public final void ensureCapacity(final int newcapacity) {
        if (this.capacity < newcapacity) {
            int desired = (this.capacity * 3) / 2 + 1;
            if (desired < newcapacity) desired = newcapacity;
            resize(desired);
        }
    }

    /**
     * Queries the size of this arraylist. The size denotes the number
     * of elements that are stored inside the datastructure.
     * @return the size of this arraylist.
     */
    public final int size() {
        return size;
    }

    /**
     * Tests whether this arraylist is empty or not.
     * @return true if this arraylist has 0 elements and false
     * if this arraylist has a size greater than 0.
     */
    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks whether an element is contained in the arraylist.
     * @param element element to look for in the arraylist.
     * @return true if the element was found inside the list,
     * otherwise false is returned.
     */
    public final boolean contains(final int element) {
        return indexOf(element) != -1;
    }

    /**
     * Forward search for an element in the arraylist starting at index 0.
     * @param element element to look for in the arraylist.
     * @return index counted from the beginning of the arraylist 
     * where the element was found. In case the element is not found
     * in the arraylist the value -1 is returned.
     */
    public final int indexOf(final int element) {
        for (int i = 0, len = size; i < len; ++i) {
            if (data[i] == element) return i;
        }
        return -1;
    }

    /**
     * Seeks an element backwards from the end of the arraylist.
     * @param element element to look for in the arraylist.
     * @return index counted from the beginning of the arraylist 
     * where the element was found. In case the element is not found
     * in the arraylist the value -1 is returned.
     */
    public final int lastIndexOf(final int element) {
        for (int i = size - 1; i >= 0; --i) {
            if (data[i] == element) return i;
        }
        return -1;
    }

    /**
     * Retrieves the element at the given position in the arraylist.
     * For efficiency considerations there is no custom exception
     * thrown. If an illegal element index is specified a
     * java.lang.ArrayIndexOutofBoundsException will be thrown.
     * @param index the index of the element that should be returned.
     * @return the value at the given index.
     * @exception InvalidArgumentException thrown if
     * the argument points to an illegal index less than 0 or
     * equal-or-greater than the current size of the arraylist.
     */
    public final int get(final int index) {
        if (index >= size || index < 0) throw new IndexOutOfBoundsException(UtilMessages.getString("ArrayListInt.2") + index + UtilMessages.getString("ArrayListInt.3"));
        return data[index];
    }

    /**
     * Sets the element at the given position in the arraylist.
     * @param index the index of the element that should be returned.
     * @param element the element to set at the specified position
     * @return the value previously stored at the given index.
     * @exception IllegalElementException thrown if
     * the argument points to an illegal index less than 0 or
     * equal-or-greater than the current size of the arraylist.
     */
    public final int set(final int index, final int element) {
        if ((index < 0) || (index >= size)) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.4") + index + UtilMessages.getString("ArrayListInt.5"));
        int prev = data[index];
        data[index] = element;
        return prev;
    }

    /**
     * Appends the element at the end of the arraylist.
     * @param element the element to append to the arraylist
     * @return always <code>true</code> by contract.
     */
    public final boolean add(final int element) {
        ensureCapacity(this.size + 1);
        data[size] = element;
        ++size;
        ++stamp;
        return true;
    }

    /**
     * Appends all of the elements of another list after the end of 
     * this arraylist. If the other list is null or has zero-size,
     * then this arraylist remains unchanged.
     * @param otherlist the list whose elements are to be appended.
     * @return true if this list was changed by the call.
     */
    public final boolean addAll(final ArrayListInt otherlist) {
        if ((otherlist == null) || (otherlist.size() == 0)) return false;
        ensureCapacity(this.size + otherlist.size);
        int d[] = this.data;
        int o[] = otherlist.data;
        for (int i = this.size, j = 0; j < otherlist.size; ++i, ++j) {
            d[i] = o[j];
        }
        size += otherlist.size;
        ++stamp;
        return true;
    }

    /**
     * Insert the element into the arraylist at the specified
     * position.
     * @param index the index where to insert.
     * @param element the element to insert into the arraylist
     * @exception IllegalElementException thrown if
     * the argument points to an illegal index less than 0 or
     * greater than the current size of the arraylist.
     */
    public final void add(final int index, final int element) {
        if ((index < 0) || (index > size)) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.6") + index + UtilMessages.getString("ArrayListInt.7"));
        ensureCapacity(this.size + 1);
        if (index == size) {
            data[size] = element;
        } else {
            System.arraycopy(data, index, data, index + 1, size - index);
            data[index] = element;
        }
        ++size;
        ++stamp;
    }

    /**
     * Removes the elements whose index is between from (inclusive) and 
     * to(exclusive). <code>from</code> must be less than <code>to</code> 
     * with one exception: if <code>from</code> is equal to <code>to</code>
     * then the method has no effect.
     * @param from starting point for removal (inclusive)
     * @param to end point for removal (exclusive)
     */
    public final void removeRange(final int from, final int to) {
        if ((from < 0) || (from >= size)) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.8") + from + UtilMessages.getString("ArrayListInt.9"));
        if ((to < 0) || (to > size)) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.10") + to + UtilMessages.getString("ArrayListInt.11"));
        if (from > to) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.12") + from + UtilMessages.getString("ArrayListInt.13") + to + UtilMessages.getString("ArrayListInt.14"));
        if (from == to) return;
        if (to != size) {
            System.arraycopy(data, to, data, from, size - to);
        }
        size -= (to - from);
    }

    /**
     * Removes the element at the specified position. The elements
     * that follow the removed element are moved one position forward in
     * the arraylist. The size of the arraylist will be one less after
     * invoking this method.
     * @param index position of the element that should be removed.
     * @return the element that was just removed from the arraylist.
     * @exception com.tensegrity.generic.util.IllegalElementException
     * thrown if the index is not pointing to an element in
     * the arraylist.
     */
    public final int remove(final int index) {
        if ((index < 0) || (index >= size)) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.15") + index + UtilMessages.getString("ArrayListInt.16"));
        final int prev;
        if (index == size - 1) {
            prev = data[index];
            --size;
        } else {
            prev = data[index];
            System.arraycopy(data, index + 1, data, index, data.length - index - 1);
            --size;
        }
        ++stamp;
        return prev;
    }

    /**
     * Removes all elements from the arraylist.
     */
    public final void clear() {
        size = 0;
        ++stamp;
    }

    /**
     * Makes a shallow copy of arraylist. That means that the elements
     * are not copied, but only their references. Note: If this particular
     * ArrayList happens to store java primitive types like int or long,
     * then a shallow and a deep copy are the same thing.
     * @return a (shallow) copy of this arraylist.
     */
    public Object clone() {
        ArrayListInt l = new ArrayListInt(this.capacity);
        System.arraycopy(this.data, 0, l.data, 0, this.size);
        l.size = this.size;
        return l;
    }

    /**
     * Allocates an array whose length is equal to the size() of this
     * arraylist and then copies the contents of the arraylist into it.
     * @return the newly allocated array with a copy of the contents of 
     * this arraylist.
     */
    public final int[] toArray() {
        final int la[] = new int[this.size];
        System.arraycopy(this.data, 0, la, 0, this.size);
        return la;
    }

    /**
     * This copies the elements from this array to the passed array-reference.
     * If the argument is an array that is large enough to hold all of the 
     * elements of this arraylist then the contents of this arraylist are copied
     * into it and the passed array is also the one being returned to the caller.
     * If the argument is null then a new array as large as the size of the
     * arraylist is allocatted, filled with the elements and then returned.
     * If the destination array is not at least as large as this arraylist
     * then a new array is allocated, filled and returned to the caller.
     * The copy that is made is shallow if Object or subtype is the element type
     * of the array. Otherwise in case primitive types are stored in the array,
     * it doesn't make sense to make a difference between shallow and deep copies
     * anyhow.
     * @param dest the array to copy the contents into if possible.
     * @return the potentially allocated array with a copy of the contents of 
     * this arraylist or the passed in array if it was large enough.
     */
    public final int[] toArray(int dest[]) {
        if ((dest == null) || (dest.length < this.size)) {
            return toArray();
        }
        System.arraycopy(this.data, 0, dest, 0, this.size);
        if (dest.length > this.size) {
            dest[size] = 0;
        }
        return dest;
    }

    /**
     * Returns a string-representation of the array.
     * @return array contents concatenated into a string
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(256);
        sb.append("ArrayListInt [");
        for (int i = 0, l = size; i < l; ++i) {
            sb.append(data[i]);
            if (i != l - 1) sb.append(',');
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Sorts the array in ascending order using quicksort. 
     * Worst case time complexity is O(n^2).
     * Overall quicksort is the fastest sorting for random data 
     * though. The partioning strategy used is plain halfway partitioning.
     */
    public final void sort() {
        sort(false);
    }

    /**
     * Sorts the array in ascending or descending order using quicksort. 
     * Worst case time complexity is O(n^2).
     * Overall quicksort is the fastest sorting for random data 
     * though. The partioning strategy used is plain halfway partitioning.
     * @param descending flag that determines the sorting order. If set to
     * true, then the sorting will be done in descending order.
     */
    public final void sort(boolean descending) {
        if (descending) qsort_desc(0, size - 1); else qsort_asc(0, size - 1);
    }

    /**
     * Internal ascending quicksort method
     */
    private final void qsort_asc(final int low, final int high) {
        int l = low;
        int h = high;
        int mid;
        if (low <= high) {
            mid = data[(low + high) / 2];
            while (l <= h) {
                while ((l < high) && (data[l] < mid)) ++l;
                while ((h > low) && (data[h] > mid)) --h;
                if (l <= h) {
                    int tmp = data[l];
                    data[l] = data[h];
                    data[h] = tmp;
                    ++l;
                    --h;
                }
            }
            if (low < h) qsort_asc(low, h);
            if (high >= l) qsort_asc(l, high);
        }
    }

    /**
     * Internal descending quicksort method
     */
    private final void qsort_desc(final int low, final int high) {
        int l = low;
        int h = high;
        int mid;
        if (low <= high) {
            mid = data[(low + high) / 2];
            while (l <= h) {
                while ((l < high) && (data[l] > mid)) ++l;
                while ((h > low) && (data[h] < mid)) --h;
                if (l <= h) {
                    int tmp = data[l];
                    data[l] = data[h];
                    data[h] = tmp;
                    ++l;
                    --h;
                }
            }
            if (low < h) qsort_desc(low, h);
            if (high >= l) qsort_desc(l, high);
        }
    }

    /**
     * Performs a binary search on the array. For this to work
     * the array MUST BE SORTED IN ASCENDING ORDER. If this is
     * not the case then the result of the binary search is not
     * defined. If the target element is in the arraylist multiple
     * times, then it is undefined which one is found. The method
     * returns -1 if the element is not found.
     * @param element the element to search for
     * @return index of any occurence of the element in the arraylist
     * or -1 if the element was not found.
     */
    public final int binarySearch(final int element) {
        int l, h;
        for (l = 0, h = size - 1; l <= h; ) {
            int middle = (h + l) / 2;
            int v = data[middle];
            if (v == element) return middle; else if (v < element) l = middle + 1; else if (v > element) h = middle - 1;
        }
        return -1;
    }

    /**
     * Replaces all occurences of <tt>element</tt> by
     * <tt>newElement</tt>. 
     * @param element the element to replace.
     * @param newElement the replacement for the old element.
     * @return true if one or more elements were replaced.
     */
    public final boolean replaceAll(final int element, final int newElement) {
        boolean retval = false;
        int d[] = this.data;
        for (int i = 0, s = size; i < s; ++i) {
            if (d[i] == element) {
                retval = true;
                d[i] = newElement;
            }
        }
        return retval;
    }

    /**
     * Reverses the order of the arraylist.
     */
    public final void reverse() {
        final int middle = size / 2;
        final int d[] = this.data;
        for (int i = 0, j = size - 1; i < middle; ++i, --j) {
            int tmp = d[i];
            d[i] = d[j];
            d[j] = tmp;
        }
    }

    /**
     * Returns a simple forward iterator to the caller which
     * can be used to retrieve the contents of the arraylist
     * like this:
     * <pre>
     * ArrayListInt.Iterator it = alist.iterator();
     * while (it.hasNext()) {
     *   int l = li.previous ();
     *   System.out.println (" : " + l);
     * }
     * </pre>
     * For backwards iteration @see ListIterator.
     * @return a forward iterator for this arraylist.
     */
    public final Iterator iterator() {
        return new ListIterator(0);
    }

    /**
     * Returns a simple forward iterator to the caller which
     * can be used to retrieve the contents of the arraylist
     * like this:
     * <pre>
     * ArrayListInt.ListIterator it = alist.listIterator();
     * while (it.hasNext()) {
     *   int l = li.next ();
     *   System.out.println (" : " + l);
     * }
     * </pre>
     * Additionally the list iterator is capable of backwards iteration
     * using the methods @see previous and @see hasPrevious
     * @return a two-directional iterator for this arraylist.
     */
    public final ListIterator listIterator() {
        return new ListIterator(0);
    }

    /**
     * Returns a simple forward iterator with user-specified
     * starting position which
     * can be used to retrieve the contents of the arraylist
     * in reverse order like this:
     * <pre>
     * ArrayListInt.ListIterator it = alist.listIterator(alist.size());
     * while (it.hasPrevious()) {
     *   int l = li.previous ();
     *   System.out.println (" : " + l);
     * }
     * </pre>
     * Additionally the list iterator is capable of backwards iteration
     * using the methods @see previous and @see hasPrevious
     * 
     * @param index the initial position of the iterator.
     * @return a two-directional iterator for this arraylist.
     * @exception com.tensegrity.generic.util.IllegalElementException
     * thrown if the index is not pointing to an element in
     * the arraylist.
     */
    public final ListIterator listIterator(int index) {
        return new ListIterator(index);
    }

    /**
     * ArrayListInt.Iterator provides a simple forware iterator
     * with basic versioning.
     * This versioning functionality will warn when the arraylist
     * the iterator is associated with has been modified since the
     * point in time when the iterator was instanciated. However this
     * mechanism implemented by a simple counter is not sufficient for
     * detecting shared-memory modifications done by concurrently executing
     * threads. This is only possible to achieve by using synchronization
     * constructs throughout the usage of the list/iterator pair, as the
     * synchronization constructs are the only way of making sure that the
     * independant working memory of each thread is flushed back into main
     * memory.
     */
    public class Iterator {

        /** current position of the iterator */
        protected int pos;

        /** modification stamp value frozen at instanciation
         * time of the iterator */
        protected final int frozen_stamp;

        /**
         * Constructs a simple forware iterator.
         */
        Iterator() {
            pos = 0;
            frozen_stamp = stamp;
        }

        /**
         * Tests whether there is a next element after the 
         * current element that the iterator points to.
         * @return true if there is an element next after the 
         * current element.
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public boolean hasNext() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            return pos != size;
        }

        /**
         * Returns the previous element from the iterator.
         * @return the previous element
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public int next() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            int l = data[pos++];
            return l;
        }
    }

    /**
     * ArrayListInt.ListIterator is a bidirectional iterator
     * with basic versioning.
     * This versioning functionality will warn when the arraylist
     * the iterator is associated with has been modified since the
     * point in time when the iterator was instanciated. However this
     * mechanism implemented by a simple counter is not sufficient for
     * detecting shared-memory modifications done by concurrently executing
     * threads. This is only possible to achieve by using synchronization
     * constructs throughout the usage of the list/iterator pair, as the
     * synchronization constructs are the only way of making sure that the
     * independant working memory of each thread is flushed back into main
     * memory.
     * The ArrayListInt.ListIterator iterator is capable of iterating
     * backwards and forwards through the arraylist it is associated with.
     */
    public class ListIterator extends Iterator {

        /** Constructs a listiterator which points
         * to the specified element.
         * @param index the index of the element the iterator is initially
         * pointing at.
         * @exception com.tensegrity.generic.util.IllegalElementException
         * thrown if the index is not pointing to an element in
         * the arraylist.
         */
        ListIterator(final int index) {
            if ((index < 0) || (index > size())) throw new IllegalArgumentException(UtilMessages.getString("ArrayListInt.19") + index + UtilMessages.getString("ArrayListInt.20"));
            pos = index;
        }

        /**
         * Returns the index of the next element in the arraylist.
         * @return next index
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public int nextIndex() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            if (pos == size) return size;
            return pos + 1;
        }

        /**
         * Returns the index of the previous element in the arraylist.
         * @return previous index
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public int previousIndex() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            return pos - 1;
        }

        /**
         * Tests whether there is a previous element before the 
         * current element that the iterator points to.
         * @return true if there is an element previous before the 
         * current element.
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public boolean hasPrevious() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            return pos > 0;
        }

        /**
         * Tests whether there is a next element after the 
         * current element that the iterator points to.
         * @return true if there is an element next after the 
         * current element.
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public boolean hasNext() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            return pos < size;
        }

        /**
         * Returns the previous element from the iterator.
         * @return the previous element
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public int previous() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            if (hasPrevious()) return data[--pos]; else throw new IllegalArgumentException();
        }

        /**
         * Returns the next element from the iterator.
         * @return the next element
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public int next() {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            if (hasNext()) return data[pos++]; else throw new IllegalArgumentException();
        }

        /**
         * Sets the element at which the iterator is currently pointing.
         * @param element the value to set at the current position.
         * @exception ListModifiedException thrown if a 
         * modification of the array was detected.
         */
        public void set(final int element) {
            if (stamp != frozen_stamp) throw new ListModifiedException();
            data[pos] = element;
        }
    }
}
