package com.butterfill.opb.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * Resizable-array implementation of the <tt>List</tt> interface that considers
 * objects to be equal only if they are the same instance (for all object
 * comparissons).
 * Implements all optional list operations, and permits all elements, including
 * <tt>null</tt>. In addition to implementing the <tt>List</tt> interface,
 * this class provides methods to manipulate the size of the array that is
 * used internally to store the list.
 * <p>
 *
 * An application can increase the capacity of an <tt>OpbExactMatchList</tt>
 * instance before adding a large number of elements using the
 * <tt>ensureCapacity</tt> operation.
 * This may reduce the amount of incremental reallocation.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access an <tt>OpbExactMatchList</tt> instance
 * concurrently, and at least one of the threads modifies the list
 * structurally, it must be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or
 * more elements, or explicitly resizes the backing array; merely setting the
 * value of an element is not a structural modification.)
 * This is typically accomplished by synchronizing on some object that
 * naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * Collections#synchronizedList method.
 *
 * <p>The iterators returned by this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in any way
 * except through the iterator's own <tt>remove</tt> or <tt>add</tt> methods,
 * the iterator will throw a ConcurrentModificationException. 
 * Thus, in the face of concurrent modification, the iterator fails quickly and 
 * cleanly, rather than risking arbitrary, non-deterministic behavior at an 
 * undetermined time in the future.<p>
 *
 * Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification. Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i><p>
 *
 * @param <E> The type of element in this list.
 *
 * @author Peter Butterfill
 */
public class OpbExactMatchList<E> extends AbstractList<E> implements List<E>, RandomAccess {

    /**
     * The array buffer into which the elements of this List are stored.
     * The capacity of the OpbExactMatchList is the length of this array buffer.
     */
    private Object[] elementData;

    /**
     * The current size of this List (the number of elements it contains).
     */
    private int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity 
     *   The initial capacity of the list.
     * @throws IllegalArgumentException
     *   If the specified initial capacity is negative.
     */
    public OpbExactMatchList(final int initialCapacity) throws IllegalArgumentException {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public OpbExactMatchList() {
        this(10);
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c
     *   The collection whose elements are to be placed into this list.
     * @throws NullPointerException
     *   If the specified collection is null.
     */
    public OpbExactMatchList(final Collection<? extends E> c) throws NullPointerException {
        elementData = c.toArray();
        size = elementData.length;
        if (elementData.getClass() != Object[].class) {
            Object[] elementDataTemp = new Object[size];
            System.arraycopy(elementData, 0, elementDataTemp, 0, size);
            elementData = elementDataTemp;
        }
    }

    /**
     * Trims the capacity of this list to be the list's current size.
     * An application can use this operation to minimize the storage of an
     * <tt>OpbExactMatchList</tt> instance.
     */
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            Object[] elementDataTemp = new Object[size];
            System.arraycopy(elementData, 0, elementDataTemp, 0, size);
            elementData = elementDataTemp;
        }
    }

    /**
     * Increases the capacity of this <tt>OpbExactMatchList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param minCapacity
     *   The desired minimum capacity.
     */
    public void ensureCapacity(final int minCapacity) {
        modCount++;
        if (minCapacity > elementData.length) {
            final int oldCapacity = elementData.length;
            int newCapacity = ((oldCapacity * 3) / 2) + 1;
            if (minCapacity > newCapacity) {
                newCapacity = minCapacity;
            }
            final Object[] elementDataTemp = new Object[newCapacity];
            System.arraycopy(elementData, 0, elementDataTemp, 0, size);
            elementData = elementDataTemp;
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return 
     *   The number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that <tt>(o == e)</tt>.
     *
     * @param o 
     *   Element whose presence in this list is to be tested.
     * @return 
     *   <tt>true</tt> if this list contains the specified element.
     */
    @Override
    public boolean contains(final Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o == get(i))</tt>, or -1 if there is no such index.
     * 
     * @param o
     *   The specified element.
     * @return
     *   The index of the first occurrence of the specified element in this 
     *   list.
     */
    @Override
    public int indexOf(final Object o) {
        for (int i = 0; i < size; i++) {
            if (o == elementData[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o == get(i))</tt>, or -1 if there is no such index.
     * 
     * @param o
     *   The specified element.
     * @return
     *   The index of the last occurrence of the specified element in this list.
     */
    @Override
    public int lastIndexOf(final Object o) {
        for (int i = size - 1; i >= 0; i--) {
            if (o == elementData[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array). 
     * The caller is thus free to modify the returned array.
     * <p>
     * 
     * @return
     *   An array containing all of the elements in this list in proper 
     *   sequence.
     */
    @Override
    public Object[] toArray() {
        Object[] elementDataTemp = new Object[size];
        System.arraycopy(elementData, 0, elementDataTemp, 0, size);
        return elementDataTemp;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array.
     * If the list fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this list.
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e. the array has more elements than the list) the element in the
     * array <em>immediately following</em> the end of the collection is set to
     * <tt>null</tt>. (This is useful in determining the length of the
     * list <i>only</i> if the caller knows that the list does not contain
     * any null elements.)
     *
     * @param <T> 
     *   The component type of the array that is returned.
     * @param a 
     *   The array into which the elements of the list are to be stored, if it 
     *   is big enough; otherwise, a new array of the same runtime type is 
     *   allocated for this purpose.
     * @return 
     *   An array containing the elements of the list.
     * @throws ArrayStoreException 
     *   If the runtime type of the specified array is not a supertype of the 
     *   runtime type of every element in this list.
     * @throws NullPointerException 
     *   If the specified array is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) throws ArrayStoreException, NullPointerException {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index
     *   The index of the element to return.
     * @return
     *   The element at the specified position in this list.
     * @throws IndexOutOfBoundsException
     *   If index is out of range (index &lt; 0 || index &gt;= size()).
     */
    @SuppressWarnings("unchecked")
    public E get(final int index) throws IndexOutOfBoundsException {
        assertIndexInBounds(index);
        return (E) elementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index
     *   The index of the element to replace.
     * @param element
     *   The element to be stored at the specified position.
     * @return
     *   The element previously at the specified position.
     * @throws IndexOutOfBoundsException
     *   If index is out of range (index &lt; 0 || index &gt;= size()).
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set(final int index, final E element) throws IndexOutOfBoundsException {
        assertIndexInBounds(index);
        E oldValue = (E) elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e
     *   The element to be appended to this list.
     * @return 
     *   <tt>true</tt> if the element is added.
     */
    @Override
    public boolean add(final E e) {
        ensureCapacity(size + 1);
        elementData[size++] = e;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index
     *   The index at which the specified element is to be inserted.
     * @param element
     *   The element to be inserted.
     * @throws IndexOutOfBoundsException
     *   If index is out of range (index &lt; 0 || index &gt;= size()).
     */
    @Override
    public void add(final int index, final E element) throws IndexOutOfBoundsException {
        assertIndexInBounds(index);
        ensureCapacity(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @return 
     *   The element that was removed from the list.
     * @param index 
     *   The index of the element to be removed.
     * @throws IndexOutOfBoundsException
     *   If index is out of range (index &lt; 0 || index &gt;= size()).
     */
    @SuppressWarnings("unchecked")
    @Override
    public E remove(final int index) throws IndexOutOfBoundsException {
        assertIndexInBounds(index);
        modCount++;
        E oldValue = (E) elementData[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, (index + 1), elementData, index, numMoved);
        }
        elementData[--size] = null;
        return oldValue;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  
     * If the list does not contain the element, it is unchanged.  
     * More formally, removes the element with the lowest index
     * <tt>i</tt> such that <tt>(o == get(i))</tt> (if such an element exists).
     * Returns <tt>true</tt> if this list contained the specified element
     * (or equivalently, if this list changed as a result of the call).
     *
     * @param o
     *   The element to be removed from this list, if present.
     * @return 
     *   <tt>true</tt> If this list contained the specified element.
     */
    @Override
    public boolean remove(final Object o) {
        for (int i = 0; i < size; i++) {
            if (o == elementData[i]) {
                modCount++;
                int numMoved = size - i - 1;
                if (numMoved > 0) {
                    System.arraycopy(elementData, i + 1, elementData, i, numMoved);
                }
                elementData[--size] = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all of the elements from this list.  
     * The list will be empty after this call returns.
     */
    @Override
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }
        size = 0;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  
     * (This implies that the behavior of this call is undefined if the 
     * specified collection is this list, and this list is nonempty.)
     *
     * @param c
     *   The collection containing elements to be added to this list.
     * @return
     *   <tt>true</tt> If this list changed as a result of the call.
     * @throws NullPointerException
     *   If the specified collection is null.
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) throws NullPointerException {
        Object[] a = c.toArray();
        ensureCapacity(size + a.length);
        System.arraycopy(a, 0, elementData, size, a.length);
        size += a.length;
        return a.length != 0;
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index
     *   The index at which to insert the first element from the specified
     *   collection.
     * @param c
     *   The collection containing elements to be added to this list.
     * @return
     *   <tt>true</tt> If this list changed as a result of the call.
     * @throws IndexOutOfBoundsException
     *   If index is out of range (index &lt; 0 || index &gt;= size()).
     * @throws NullPointerException
     *   If the specified collection is null.
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) throws IndexOutOfBoundsException, NullPointerException {
        assertIndexInBounds(index);
        Object[] a = c.toArray();
        ensureCapacity(size + a.length);
        int numMoved = size - index;
        System.arraycopy(elementData, index, elementData, (index + a.length), numMoved);
        System.arraycopy(a, 0, elementData, index, a.length);
        size += a.length;
        return a.length != 0;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * This is a no-op if <tt>toIndex == fromIndex</tt>.
     *
     * @param fromIndex
     *   The index of first element to be removed.
     * @param toIndex
     *   The index after last element to be removed.
     * @throws IndexOutOfBoundsException
     *   If (fromIndex &lt; 0 || fromIndex &gt;= size() ||
     *   toIndex &gt; size() || toIndex &lt; fromIndex).
     */
    @Override
    protected void removeRange(final int fromIndex, final int toIndex) throws IndexOutOfBoundsException {
        assertIndexInBounds(fromIndex);
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
        int newSize = size - (toIndex - fromIndex);
        while (size != newSize) {
            elementData[--size] = null;
        }
    }

    /**
     * Checks if the given index is in range.
     * If not, throws an IndexOutOfBoundsException.
     * 
     * @param index
     *   The index of the element to remove.
     * @throws IndexOutOfBoundsException
     *   If the given index is not in range.
     */
    private void assertIndexInBounds(final int index) throws IndexOutOfBoundsException {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
