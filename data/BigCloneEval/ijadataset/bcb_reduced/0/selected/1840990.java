package de.grogra.xl.util;

import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * A <code>ObjectList</code> represents a list of <code>Object</code> values.
 * It provides list- and stack-oriented methods for insertion, addition,
 * and removal, of values. The methods are not thread-safe.
 * 
 * @author Ole Kniemeyer
 */
public class ObjectList<E> extends java.util.AbstractList<E> implements RandomAccess, Cloneable, java.io.Serializable, de.grogra.xl.lang.ObjectConsumer<E>, de.grogra.xl.lang.VoidToObjectGenerator<E> {

    private static final long serialVersionUID = -2672494796732927877L;

    /**
	 * The array holding the elements. Only the components from 0
	 * to <code>size - 1</code> are valid. Direct operation on this
	 * array has to be designed carefully to avoid inconsistencies.
	 */
    public transient Object[] elements;

    /**
	 * The size of this list. Direct operation on this
	 * field has to be designed carefully to avoid inconsistencies.
	 */
    public int size = 0;

    /**
	 * Constructs a new <code>ObjectList</code> with a given initial capacity.
	 *
	 * @param capacity the initial capacity
	 */
    public ObjectList(int capacity) {
        elements = new Object[Math.max(capacity, 8)];
    }

    /**
	 * Constructs a new <code>ObjectList</code>.
	 */
    public ObjectList() {
        this(16);
    }

    /**
	 * Constructs a new <code>ObjectList</code> whose elements are
	 * a copy of <code>elements</code>.
	 *
	 * @param elements the initial elements of the list
	 */
    public ObjectList(E[] elements) {
        this(elements.length);
        arraycopy(elements, 0, this.elements, 0, size = elements.length);
    }

    @Override
    public Object clone() {
        try {
            ObjectList<E> c = (ObjectList<E>) super.clone();
            c.elements = new Object[Math.max(size, 8)];
            arraycopy(elements, 0, c.elements, 0, size);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
	 * Ensures a capacity of the internal array of at least
	 * <code>capacity</code>.
	 *
	 * @param capacity the desired minimum capacity
	 */
    public void ensureCapacity(int capacity) {
        if (capacity > elements.length) {
            Object[] a = new Object[Math.max(capacity, elements.length * 2)];
            arraycopy(elements, 0, a, 0, size);
            elements = a;
        }
    }

    private static final Object[] EMPTY = new Object[0];

    /**
     * Trims the capacity of this list to be its current size.
	 */
    public void trimToSize() {
        if (size < elements.length) {
            if (size == 0) {
                elements = EMPTY;
            } else {
                arraycopy(elements, 0, elements = new Object[size], 0, size);
            }
        }
    }

    /**
	 * Pushes <code>o</code> on top of this list, i.e., as last element.
	 *
	 * @param o the value to push
	 * @return this list
	 */
    public final ObjectList<E> push(E o) {
        add(o);
        return this;
    }

    /**
	 * Pushes <code>o1</code> and <code>o2</code> on top of this list,
	 * i.e., as last elements. The effect is the same as the invocation
	 * <code>list.push(o1).push(o2)</code>.
	 *
	 * @param o1 the first value to push
	 * @param o2 the second value to push
	 * @return this list
	 */
    public final ObjectList<E> push(E o1, E o2) {
        add(o1);
        add(o2);
        return this;
    }

    /**
	 * Pushes <code>o1 ... o3</code> on top of this list,
	 * i.e., as last elements. The effect is the same as the invocation
	 * <code>list.push(o1).push(o2).push(o3)</code>.
	 *
	 * @param o1 the first value to push
	 * @param o2 the second value to push
	 * @param o3 the third value to push
	 * @return this list
	 */
    public final ObjectList<E> push(E o1, E o2, E o3) {
        add(o1);
        add(o2);
        add(o3);
        return this;
    }

    /**
	 * Adds <code>o</code> as last element to this list.
	 *
	 * @param o the value to add
	 * @return <code>true</code>
	 */
    public boolean add(E o) {
        modCount++;
        if (size == elements.length) {
            ensureCapacity(size + 1);
        }
        elements[size] = o;
        size++;
        return true;
    }

    /**
	 * Inserts <code>o</code> at position <code>index</code> to this list.
	 * If <code>index</code> is not less than <code>size</code>, the
	 * list is enlarged and filled with <code>null</code>-values before.
	 *
	 * @param index the insert position
	 * @param o the value to insert
	 */
    public void add(int index, E o) {
        modCount++;
        if (index >= size) {
            ensureCapacity(index + 1);
        } else {
            ensureCapacity(size + 1);
            arraycopy(elements, index, elements, index + 1, size - index);
        }
        elements[index] = o;
        size++;
    }

    /**
	 * Adds <code>o</code> as last element to this list if is not
	 * yet contained as reported by {@link #contains(Object)}.
	 *
	 * @param o the value to add
	 */
    public void addIfNotContained(E o) {
        if (!contains(o)) {
            add(o);
        }
    }

    /**
	 * Appends all elements of <code>v</code> to this list.
	 *
	 * @param v the list of elements to add
	 * @return this list
	 */
    public ObjectList<E> addAll(ObjectList<? extends E> v) {
        return addAll(v.elements, 0, v.size);
    }

    /**
	 * Appends <code>length</code> components of <code>v</code> to this list,
	 * starting at index <code>begin</code>.
	 *
	 * @param v the array of elements to add
	 * @param begin the array index to begin with
	 * @param length the number of elements to add
	 * @return this list
	 */
    public ObjectList<E> addAll(Object[] v, int begin, int length) {
        if (length <= 0) {
            return this;
        }
        modCount++;
        ensureCapacity(size + length);
        arraycopy(v, begin, elements, size, length);
        size += length;
        return this;
    }

    /**
	 * Removes the element at position <code>index</code>.
	 *
	 * @param index the position of the element to be removed
	 * @return the value of the removed element
	 */
    public E removeAt(int index) {
        modCount++;
        E o = (E) elements[index];
        arraycopy(elements, index + 1, elements, index, --size - index);
        elements[size] = null;
        return o;
    }

    /**
	 * Removes the element at position <code>index</code>.
	 *
	 * @param index the position of the element to be removed
	 * @return the value of the removed element
	 */
    public E remove(int index) {
        modCount++;
        E o = (E) elements[index];
        arraycopy(elements, index + 1, elements, index, --size - index);
        elements[size] = null;
        return o;
    }

    /**
	 * Removes the element <code>o</code>. The last occurence of
	 * <code>o</code> in this list is removed.
	 *
	 * @param o the element to be removed
	 * @return <code>true</code> iff <code>o</code> was found and removed from the list
	 */
    public boolean remove(Object o) {
        for (int i = size - 1; i >= 0; i--) {
            if ((((o) == (elements[i])) || (useEquals && ((o) != null) && (o).equals(elements[i])))) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
	 * Sets the element at position <code>index</code> to <code>o</code>.
	 * If <code>index</code> is not less than <code>size</code>, the
	 * list is enlarged and filled with <code>null</code>-values before.
	 *
	 * @param index the position
	 * @param o the new value
	 * @return the old value at <code>index</code>
	 */
    public E set(int index, E o) {
        E before;
        if (index >= size) {
            ensureCapacity(index + 1);
            before = (E) elements[index];
            elements[index] = o;
            size = index + 1;
        } else {
            before = (E) elements[index];
            elements[index] = o;
        }
        return before;
    }

    /**
     * Returns the list element at <code>index</code>. If
     * <code>index</code> is not less than <code>size</code>,
     * <code>null</code> is returned.
     *
     * @param index the position
     * @return the value at <code>index</code>
     */
    public E get(int index) {
        return (index < size) ? (E) elements[index] : null;
    }

    /**
     * Returns the list element at <code>index</code> as seen from
     * the top, i.e., at absolute position <code>size - index</code>.
     * Thus, the topmost element has index 1.
     *
     * @param index the position as seen from the top
     * @return the value at that position
     */
    public E peek(int index) {
        return (index > 0) ? (E) elements[size - index] : null;
    }

    /**
     * Returns <code>true</code> iff this list contains the given
     * element <code>o</code>.
     *
     * @param o a value
     * @return <code>true</code> iff <code>o</code> is contained
     */
    public boolean contains(Object o) {
        for (int i = size - 1; i >= 0; i--) {
            if ((((o) == (elements[i])) || (useEquals && ((o) != null) && (o).equals(elements[i])))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of <code>o</code> in this list.
     *
     * @param o a value
     * @return the index of <code>o</code>, or -1 of <code>o</code> is not contained
     */
    public int indexOf(Object o) {
        for (int i = 0; i < size; i++) {
            if ((((o) == (elements[i])) || (useEquals && ((o) != null) && (o).equals(elements[i])))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the last index of <code>o</code> in this list.
     *
     * @param o a value
     * @return the index of <code>o</code>, or -1 of <code>o</code> is not contained
     */
    public int lastIndexOf(Object o) {
        for (int i = size - 1; i >= 0; i--) {
            if ((((o) == (elements[i])) || (useEquals && ((o) != null) && (o).equals(elements[i])))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determines whether comparisons between objects should be based
     * on the <code>equals</code>-method or on the equality operator
     * <code>==</code>.
     */
    public boolean useEquals = true;

    /**
	 * Constructs a new <code>ObjectList</code> with a given initial capacity.
	 *
	 * @param capacity the initial capacity
	 * @param useEquals the value for {@link #useEquals}
	 */
    public ObjectList(int capacity, boolean useEquals) {
        this(capacity);
        this.useEquals = useEquals;
    }

    /**
     * Removes all of the elements from this list. The list will
     * be empty after this call returns.
     */
    public void clear() {
        modCount++;
        clear(elements, 0, size);
        size = 0;
    }

    /**
	 * Inserts <code>o</code> into this ordered list, based on <code>c</code>.
	 * This list has to be sorted in ascending order as defined by
	 * <code>c</code>. <code>o</code> will then be added at a position
	 * according to this order.
	 *
	 * @param o the value to add
	 * @param c the comparator which defines the order
	 */
    public void addInOrder(E o, java.util.Comparator<? super E> c) {
        int i = 0;
        while ((i < size) && (c.compare(o, (E) elements[i]) > 0)) {
            i++;
        }
        add(i, o);
    }

    /**
	 * Inserts <code>o</code> into this ordered list.
	 * This list has to be sorted in ascending order as defined by
	 * <code>o</code>. <code>o</code> will then be added at a position
	 * according to this order.
	 *
	 * @param o the value to add
	 */
    public void addInOrder(Comparable<? super E> o) {
        int i;
        for (i = size; i > 0; i--) {
            if (o.compareTo((E) elements[i - 1]) >= 0) {
                break;
            }
        }
        add(i, (E) o);
    }

    /**
	 * Appends all elements of <code>v</code> to this list.
	 *
	 * @param v the list of elements to add
	 * @return <code>true</code> iff this list changed as a result of the invocation
	 */
    public boolean addAll(java.util.Collection<? extends E> v) {
        if (v instanceof ObjectList) {
            if (v.isEmpty()) {
                return false;
            } else {
                addAll(((ObjectList) v).elements, 0, v.size());
                return true;
            }
        } else {
            return super.addAll(v);
        }
    }

    public static void arraycopy(Object[] src, int srcIndex, Object[] dest, int destIndex, int length) {
        if (length < 20) {
            if ((dest != src) || (destIndex > srcIndex)) {
                while (--length >= 0) {
                    dest[destIndex + length] = src[srcIndex + length];
                }
            } else {
                for (int i = 0; i < length; i++) {
                    dest[destIndex + i] = src[srcIndex + i];
                }
            }
        } else {
            System.arraycopy(src, srcIndex, dest, destIndex, length);
        }
    }

    private static final Object[] OBJECT_NULL = new Object[1024];

    public static void clear(Object[] array, int index, int length) {
        if (length < 20) {
            while (--length >= 0) {
                array[index + length] = null;
            }
        } else {
            while (length > 0) {
                int n = (length > 1024) ? 1024 : length;
                arraycopy(OBJECT_NULL, 0, array, index, n);
                length -= n;
                index += n;
            }
        }
    }

    /**
     * Removes and returns the object at the top of this list. 
     *
     * @return the removed object from the top of this list
     */
    public E pop() {
        modCount++;
        E o = (E) elements[--size];
        elements[size] = null;
        return o;
    }

    /**
     * Sets the size of this list to the given value. If the new size
     * is greater than the old size, the new elements are initialized
     * with <code>null</code>-values. 
     *
     * @param size the new size
     */
    public void setSize(int size) {
        modCount++;
        if (size > this.size) {
            ensureCapacity(size);
        } else {
            clear(elements, size, this.size - size);
        }
        this.size = size;
    }

    /**
     * Returns the size of this list.
     *
     * @return the size
     */
    public final int size() {
        return size;
    }

    /**
     * Returns if this list is empty, i.e., if its size is zero.
     *
     * @return <code>true</code> iff this list is empty
     */
    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns an array containing the elements of this list.
     *
     * @return an array copy of this list
     */
    public Object[] toArray() {
        Object[] a = new Object[size];
        arraycopy(elements, 0, a, 0, size);
        return a;
    }

    /**
     * Returns an array containing the elements of this list. The type
     * of the returned array is that of the specified <code>array</code>. If this
     * list fits in the specified <code>array</code>,
     * it is returned therein. Otherwise,
     * a new array is allocated whose length is the size of this list's size,
     * the values of this list are copied into the new array, and this
     * array is returned.
     * <p>
     * If there is room for an additional element in the <code>array</code>,
     * a <code>null</code>-value is written behind the last copied element.
     *
     * @param array an array to use 
     * @return an array copy of this list
     */
    public <T> T[] toArray(T[] array) {
        int l;
        if ((l = array.length) > size) {
            array[size] = null;
        } else if (l < size) {
            array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        }
        arraycopy(elements, 0, array, 0, size);
        return array;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        List list = (List) o;
        if (list instanceof RandomAccess) {
            if (size != list.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!(((elements[i]) == (list.get(i))) || (useEquals && ((elements[i]) != null) && (elements[i]).equals(list.get(i))))) {
                    return false;
                }
            }
            return true;
        } else {
            ListIterator li = list.listIterator();
            for (int i = 0; i < size; i++) {
                if (!li.hasNext()) {
                    return false;
                }
                Object n = li.next();
                if (!(((n) == (elements[i])) || (useEquals && ((n) != null) && (n).equals(elements[i])))) {
                    return false;
                }
            }
            return !li.hasNext();
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            Object o;
            hashCode = 31 * hashCode + (((o = elements[i]) != null) ? o.hashCode() : 0);
        }
        return hashCode;
    }

    public void consume(E value) {
        add(value);
    }

    /**
	 * This generator method yields all values to <code>cons</code>.
	 *
	 * @param cons the consumer which receives the values
	 */
    public void evaluateObject(de.grogra.xl.lang.ObjectConsumer<? super E> cons) {
        for (int i = 0; i < size; i++) {
            cons.consume((E) elements[i]);
        }
    }

    /**
	 * This method is an alias for {@link #evaluateObject}.
	 */
    public void values(de.grogra.xl.lang.ObjectConsumer<? super E> cons) {
        evaluateObject(cons);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        for (int i = 0; i < size; i++) {
            s.writeObject(elements[i]);
        }
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        Object[] a = elements = new Object[size];
        for (int i = 0; i < size; i++) {
            a[i] = s.readObject();
        }
    }
}
