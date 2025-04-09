package com.bluebrim.collection.shared;

import java.io.*;
import java.util.*;

/**
 * <p>Near-exact copy of ArrayList. The only difference is that elementData is
 * now <code>protected non-transient</code> instead of <code>private
 * transient</code>. The change to protected enables a reasonable way to
 * implement the CoReorderable interface. The non-transient change and
 * corresponding removal of (de-)serialization code enables GemStone/J storage.</p>
 * <p>Now, removeRange has been made public too. Used to be protected.</p>
 *
 * @author Markus Persson 1999-04-13
 * @author Markus Persson 1999-10-19
 * @author Markus Persson 2001-05-03
 *
 * <!-- Sun original comments follow --> 
 * 
 * Resizable-array implementation of the <tt>List</tt> interface.  Implements
 * all optional list operations, and permits all elements, including
 * <tt>null</tt>.  In addition to implementing the <tt>List</tt> interface,
 * this class provides methods to manipulate the size of the array that is
 * used internally to store the list.  (This class is roughly equivalent to
 * <tt>Vector</tt>, except that it is unsynchronized.)<p>
 *
 * The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant
 * time.  The <tt>add</tt> operation runs in <i>amortized constant time</i>,
 * that is, adding n elements requires O(n) time.  All of the other operations
 * run in linear time (roughly speaking).  The constant factor is low compared
 * to that for the <tt>LinkedList</tt> implementation.<p>
 *
 * Each <tt>ArrayList</tt> instance has a <i>capacity</i>.  The capacity is
 * the size of the array used to store the elements in the list.  It is always
 * at least as large as the list size.  As elements are added an ArrayList,
 * its capacity grows automatically.  The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized
 * time cost.<p> 
 *
 * An application can increase the capacity of an <tt>ArrayList</tt> instance
 * before adding a large number of elements using the <tt>ensureCapacity</tt>
 * operation.  This may reduce the amount of incremental reallocation.<p>
 *
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an <tt>ArrayList</tt> instance concurrently, and at
 * least one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation that
 * adds or deletes one or more elements, or explicitly resizes the backing
 * array; merely setting the value of an element is not a structural
 * modification.)  This is typically accomplished by synchronizing on some
 * object that naturally encapsulates the list.  If no such object exists, the
 * list should be "wrapped" using the <tt>Collections.synchronizedList</tt>
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:
 * <pre>
 *	List list = Collections.synchronizedList(new ArrayList(...));
 * </pre><p>
 *
 * The iterators returned by this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if list is structurally
 * modified at any time after the iterator is created, in any way except
 * through the iterator's own remove or add methods, the iterator will throw a
 * ConcurrentModificationException.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * @author  Josh Bloch
 * @version 1.17 09/30/98
 * @see	    Collection
 * @see	    List
 * @see	    LinkedList
 * @see	    Vector
 * @see	    Collections#synchronizedList(List)
 * @since JDK1.2
 */
public class CoArrayList extends AbstractList implements List, Cloneable, Serializable {

    /**
	 * The array buffer into which the elements of the ArrayList are stored.
	 * The capacity of the ArrayList is the length of this array buffer.
	 */
    protected Object elementData[];

    /**
	 * The size of the ArrayList (the number of elements it contains).
	 *
	 * @serial
	 */
    private int size;

    private static final long serialVersionUID = 152047731822007244L;

    /**
	 * Constructs an empty list.
	 */
    public CoArrayList() {
        this(10);
    }

    /**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param   initialCapacity   the initial capacity of the list.
	 */
    public CoArrayList(int initialCapacity) {
        super();
        this.elementData = new Object[initialCapacity];
    }

    /**
	 * Constructs a list containing the elements of the specified
	 * collection, in the order they are returned by the collection's
	 * iterator.  The <tt>ArrayList</tt> instance has an initial capacity of
	 * 110% the size of the specified collection.
	 */
    public CoArrayList(Collection c) {
        this((c.size() * 110) / 100);
        Iterator i = c.iterator();
        while (i.hasNext()) elementData[size++] = i.next();
    }

    /**
	 * Inserts the specified element at the specified position in this
	 * list. Shifts the element currently at that position (if any) and
	 * any subsequent elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted.
	 * @param element element to be inserted.
	 * @throws    IndexOutOfBoundsException if index is out of range
	 *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
	 */
    public void add(int index, Object element) {
        if (index > size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        ensureCapacity(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    /**
	 * Appends the specified element to the end of this list.
	 *
	 * @param o element to be appended to this list.
	 * @return <tt>true</tt> (as per the general contract of Collection.add).
	 */
    public boolean add(Object o) {
        ensureCapacity(size + 1);
        elementData[size++] = o;
        return true;
    }

    /**
	 * Inserts all of the elements in the specified Collection into this
	 * list, starting at the specified position.  Shifts the element
	 * currently at that position (if any) and any subsequent elements to
	 * the right (increases their indices).  The new elements will appear
	 * in the list in the order that they are returned by the
	 * specified Collection's iterator.
	 *
	 * @param index index at which to insert first element
	 *		    from the specified collection.
	 * @param c elements to be inserted into this list.
	 * @throws    IndexOutOfBoundsException if index out of range <tt>(index
	 *		  &lt; 0 || index &gt; size())</tt>.
	 */
    public boolean addAll(int index, Collection c) {
        if (index > size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        int numNew = c.size();
        ensureCapacity(size + numNew);
        int numMoved = size - index;
        if (numMoved > 0) System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) elementData[index++] = e.next();
        size += numNew;
        return numNew != 0;
    }

    /**
	 * Appends all of the elements in the specified Collection to the end of
	 * this list, in the order that they are returned by the
	 * specified Collection's Iterator.  The behavior of this operation is
	 * undefined if the specified Collection is modified while the operation
	 * is in progress.  (This implies that the behavior of this call is
	 * undefined if the specified Collection is this list, and this
	 * list is nonempty.)
	 *
	 * @param index index at which to insert first element
	 *			  from the specified collection.
	 * @param c elements to be inserted into this list.
	 * @throws    IndexOutOfBoundsException if index out of range <tt>(index
	 *		  &lt; 0 || index &gt; size())</tt>.
	 */
    public boolean addAll(Collection c) {
        modCount++;
        int numNew = c.size();
        ensureCapacity(size + numNew);
        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) elementData[size++] = e.next();
        return numNew != 0;
    }

    /**
	 * Removes all of the elements from this list.  The list will
	 * be empty after this call returns.
	 */
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++) elementData[i] = null;
        size = 0;
    }

    /**
	 * Returns a shallow copy of this <tt>CoArrayList</tt> instance.  (The
	 * elements themselves are not copied.)
	 *
	 * @return  a clone of this <tt>CoArrayList</tt> instance.
	 */
    public Object clone() {
        try {
            CoArrayList v = (CoArrayList) super.clone();
            v.elementData = new Object[size];
            System.arraycopy(elementData, 0, v.elementData, 0, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
	 * Returns <tt>true</tt> if this list contains the specified element.
	 *
	 * @param o element whose presence in this List is to be tested.
	 */
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    /**
	 * Increases the capacity of this <tt>ArrayList</tt> instance, if
	 * necessary, to ensure  that it can hold at least the number of elements
	 * specified by the minimum capacity argument. 
	 *
	 * @param   minCapacity   the desired minimum capacity.
	 */
    public void ensureCapacity(int minCapacity) {
        modCount++;
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            Object oldData[] = elementData;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            elementData = new Object[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }

    /**
	 * Returns the element at the specified position in this list.
	 *
	 * @param  index index of element to return.
	 * @return the element at the specified position in this list.
	 * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
	 * 		  &lt; 0 || index &gt;= size())</tt>.
	 */
    public Object get(int index) {
        RangeCheck(index);
        return elementData[index];
    }

    /**
	 * Searches for the first occurence of the given argument, testing 
	 * for equality using the <tt>equals</tt> method. 
	 *
	 * @param   elem   an object.
	 * @return  the index of the first occurrence of the argument in this
	 *          list; returns <tt>-1</tt> if the object is not found.
	 * @see     Object#equals(Object)
	 */
    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++) if (elementData[i] == null) return i;
        } else {
            for (int i = 0; i < size; i++) if (elem.equals(elementData[i])) return i;
        }
        return -1;
    }

    /**
	 * Tests if this list has no elements.
	 *
	 * @return  <tt>true</tt> if this list has no elements;
	 *          <tt>false</tt> otherwise.
	 */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
	 * Returns the index of the last occurrence of the specified object in
	 * this list.
	 *
	 * @param   elem   the desired element.
	 * @return  the index of the last occurrence of the specified object in
	 *          this list; returns -1 if the object is not found.
	 */
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--) if (elementData[i] == null) return i;
        } else {
            for (int i = size - 1; i >= 0; i--) if (elem.equals(elementData[i])) return i;
        }
        return -1;
    }

    /**
	 * Check if the given index is in range.  If not, throw an appropriate
	 * runtime exception.
	 */
    private void RangeCheck(int index) {
        if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    /**
	 * Removes the element at the specified position in this list.
	 * Shifts any subsequent elements to the left (subtracts one from their
	 * indices).
	 *
	 * @param index the index of the element to removed.
	 * @return the element that was removed from the list.
	 * @throws    IndexOutOfBoundsException if index out of range <tt>(index
	 * 		  &lt; 0 || index &gt;= size())</tt>.
	 */
    public Object remove(int index) {
        RangeCheck(index);
        modCount++;
        Object oldValue = elementData[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--size] = null;
        return oldValue;
    }

    /**
	 * Removes from this List all of the elements whose index is between
	 * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
	 * elements to the left (reduces their index).
	 * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
	 * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
	 *
	 * @param fromIndex index of first element to be removed.
	 * @param fromIndex index after last element to be removed.
	 */
    public void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
        int newSize = size - (toIndex - fromIndex);
        while (size != newSize) elementData[--size] = null;
    }

    /**
	 * Replaces the element at the specified position in this list with
	 * the specified element.
	 *
	 * @param index index of element to replace.
	 * @param element element to be stored at the specified position.
	 * @return the element previously at the specified position.
	 * @throws    IndexOutOfBoundsException if index out of range
	 *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
	 */
    public Object set(int index, Object element) {
        RangeCheck(index);
        Object oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    /**
	 * Returns the number of elements in this list.
	 *
	 * @return  the number of elements in this list.
	 */
    public int size() {
        return size;
    }

    /**
	 * Returns an array containing all of the elements in this list
	 * in the correct order.
	 *
	 * @return an array containing all of the elements in this list
	 * 	       in the correct order.
	 */
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(elementData, 0, result, 0, size);
        return result;
    }

    /**
	 * Returns an array containing all of the elements in this list in the
	 * correct order.  The runtime type of the returned array is that of the
	 * specified array.  If the list fits in the specified array, it is
	 * returned therein.  Otherwise, a new array is allocated with the runtime
	 * type of the specified array and the size of this list.<p>
	 *
	 * If the list fits in the specified array with room to spare (i.e., the
	 * array has more elements than the list), the element in the array
	 * immediately following the end of the collection is set to
	 * <tt>null</tt>.  This is useful in determining the length of the list
	 * <i>only</i> if the caller knows that the list does not contain any
	 * <tt>null</tt> elements.
	 *
	 * @param a the array into which the elements of the list are to
	 *		be stored, if it is big enough; otherwise, a new array of the
	 * 		same runtime type is allocated for this purpose.
	 * @return an array containing the elements of the list.
	 * @throws ArrayStoreException if the runtime type of a is not a supertype
	 *         of the runtime type of every element in this list.
	 */
    public Object[] toArray(Object a[]) {
        if (a.length < size) a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size) a[size] = null;
        return a;
    }

    /**
	 * Trims the capacity of this <tt>ArrayList</tt> instance to be the
	 * list's current size.  An application can use this operation to minimize
	 * the storage of an <tt>ArrayList</tt> instance.
	 */
    public void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (size < oldCapacity) {
            Object oldData[] = elementData;
            elementData = new Object[size];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }
}
