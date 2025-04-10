package javathreads.examples.ch14;

import java.util.*;

public class ModVector<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    /**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer, 
     * and is at least large enough to contain all the vector's elements.<p>
     *
     * Any array elements following the last element in the ModVector are null.
     *
     * @serial
     */
    protected Object[] elementData;

    /**
     * The number of valid components in this <tt>ModVector</tt> object. 
     * Components <tt>elementData[0]</tt> through 
     * <tt>elementData[elementCount-1]</tt> are the actual items.
     *
     * @serial
     */
    protected int elementCount;

    /**
     * The amount by which the capacity of the vector is automatically 
     * incremented when its size becomes greater than its capacity.  If 
     * the capacity increment is less than or equal to zero, the capacity
     * of the vector is doubled each time it needs to grow.
     *
     * @serial
     */
    protected int capacityIncrement;

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = -2767605614048989439L;

    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment. 
     *
     * @param   initialCapacity     the initial capacity of the vector.
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public ModVector(int initialCapacity, int capacityIncrement) {
        super();
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs an empty vector with the specified initial capacity and 
     * with its capacity increment equal to zero.
     *
     * @param   initialCapacity   the initial capacity of the vector.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public ModVector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    /**
     * Constructs an empty vector so that its internal data array 
     * has size <tt>10</tt> and its standard capacity increment is 
     * zero. 
     */
    public ModVector() {
        this(10);
    }

    /**
     * Constructs a vector containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this
     *       vector.
     * @throws NullPointerException if the specified collection is null.
     * @since   1.2
     */
    public ModVector(Collection<? extends E> c) {
        elementCount = c.size();
        elementData = new Object[(int) Math.min((elementCount * 110L) / 100, Integer.MAX_VALUE)];
        c.toArray(elementData);
    }

    /**
     * Copies the components of this vector into the specified array. The 
     * item at index <tt>k</tt> in this vector is copied into component 
     * <tt>k</tt> of <tt>anArray</tt>. The array must be big enough to hold 
     * all the objects in this vector, else an 
     * <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param   anArray   the array into which the components get copied.
     * @throws  NullPointerException if the given array is null.
     */
    public void copyInto(Object[] anArray) {
        System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    /**
     * Trims the capacity of this vector to be the vector's current 
     * size. If the capacity of this vector is larger than its current 
     * size, then the capacity is changed to equal the size by replacing 
     * its internal data array, kept in the field <tt>elementData</tt>, 
     * with a smaller one. An application can use this operation to 
     * minimize the storage of a vector. 
     */
    public void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (elementCount < oldCapacity) {
            Object oldData[] = elementData;
            elementData = new Object[elementCount];
            System.arraycopy(oldData, 0, elementData, 0, elementCount);
        }
    }

    /**
     * Increases the capacity of this vector, if necessary, to ensure 
     * that it can hold at least the number of components specified by 
     * the minimum capacity argument.
     *
     * <p>If the current capacity of this vector is less than
     * <tt>minCapacity</tt>, then its capacity is increased by replacing its
     * internal data array, kept in the field <tt>elementData</tt>, with a
     * larger one.  The size of the new data array will be the old size plus
     * <tt>capacityIncrement</tt>, unless the value of
     * <tt>capacityIncrement</tt> is less than or equal to zero, in which case
     * the new capacity will be twice the old capacity; but if this new size
     * is still smaller than <tt>minCapacity</tt>, then the new capacity will
     * be <tt>minCapacity</tt>.
     *
     * @param minCapacity the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
        modCount++;
        ensureCapacityHelper(minCapacity);
    }

    /**
     * This implements the unsynchronized semantics of ensureCapacity.
     * Synchronized methods in this class can internally call this 
     * method for ensuring capacity without incurring the cost of an 
     * extra synchronization.
     *
     * @see java.util.ModVector#ensureCapacity(int)
     */
    private void ensureCapacityHelper(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            Object[] oldData = elementData;
            int newCapacity = (capacityIncrement > 0) ? (oldCapacity + capacityIncrement) : (oldCapacity * 2);
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = new Object[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, elementCount);
        }
    }

    /**
     * Sets the size of this vector. If the new size is greater than the 
     * current size, new <code>null</code> items are added to the end of 
     * the vector. If the new size is less than the current size, all 
     * components at index <code>newSize</code> and greater are discarded.
     *
     * @param   newSize   the new size of this vector.
     * @throws  ArrayIndexOutOfBoundsException if new size is negative.
     */
    public void setSize(int newSize) {
        modCount++;
        if (newSize > elementCount) {
            ensureCapacityHelper(newSize);
        } else {
            for (int i = newSize; i < elementCount; i++) {
                elementData[i] = null;
            }
        }
        elementCount = newSize;
    }

    /**
     * Returns the current capacity of this vector.
     *
     * @return  the current capacity (the length of its internal 
     *          data array, kept in the field <tt>elementData</tt> 
     *          of this vector).
     */
    public int capacity() {
        return elementData.length;
    }

    /**
     * Returns the number of components in this vector.
     *
     * @return  the number of components in this vector.
     */
    public int size() {
        return elementCount;
    }

    /**
     * Tests if this vector has no components.
     *
     * @return  <code>true</code> if and only if this vector has 
     *          no components, that is, its size is zero;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns an enumeration of the components of this vector. The 
     * returned <tt>Enumeration</tt> object will generate all items in 
     * this vector. The first item generated is the item at index <tt>0</tt>, 
     * then the item at index <tt>1</tt>, and so on. 
     *
     * @return  an enumeration of the components of this vector.
     * @see     Enumeration
     * @see     Iterator
     */
    public Enumeration<E> elements() {
        return new Enumeration<E>() {

            int count = 0;

            public boolean hasMoreElements() {
                return count < elementCount;
            }

            public E nextElement() {
                synchronized (ModVector.this) {
                    if (count < elementCount) {
                        return (E) elementData[count++];
                    }
                }
                throw new NoSuchElementException("ModVector Enumeration");
            }
        };
    }

    /**
     * Tests if the specified object is a component in this vector.
     *
     * @param   elem   an object.
     * @return  <code>true</code> if and only if the specified object 
     * is the same as a component in this vector, as determined by the 
     * <tt>equals</tt> method; <code>false</code> otherwise.
     */
    public boolean contains(Object elem) {
        return indexOf(elem, 0) >= 0;
    }

    /**
     * Searches for the first occurence of the given argument, testing 
     * for equality using the <code>equals</code> method. 
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          vector, that is, the smallest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k])</tt> is <tt>true</tt>; 
     *          returns <code>-1</code> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
        return indexOf(elem, 0);
    }

    /**
     * Searches for the first occurence of the given argument, beginning 
     * the search at <code>index</code>, and testing for equality using 
     * the <code>equals</code> method. 
     *
     * @param   elem    an object.
     * @param   index   the non-negative index to start searching from.
     * @return  the index of the first occurrence of the object argument in
     *          this vector at position <code>index</code> or later in the
     *          vector, that is, the smallest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k]) && (k &gt;= index)</tt> is 
     *          <tt>true</tt>; returns <code>-1</code> if the object is not 
     *          found. (Returns <code>-1</code> if <tt>index</tt> &gt;= the
     *          current size of this <tt>ModVector</tt>.)
     * @exception  IndexOutOfBoundsException  if <tt>index</tt> is negative.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem, int index) {
        if (elem == null) {
            for (int i = index; i < elementCount; i++) if (elementData[i] == null) return i;
        } else {
            for (int i = index; i < elementCount; i++) if (elem.equals(elementData[i])) return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this vector.
     *
     * @param   elem   the desired component.
     * @return  the index of the last occurrence of the specified object in
     *          this vector, that is, the largest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k])</tt> is <tt>true</tt>; 
     *          returns <code>-1</code> if the object is not found.
     */
    public int lastIndexOf(Object elem) {
        return lastIndexOf(elem, elementCount - 1);
    }

    /**
     * Searches backwards for the specified object, starting from the 
     * specified index, and returns an index to it. 
     *
     * @param  elem    the desired component.
     * @param  index   the index to start searching from.
     * @return the index of the last occurrence of the specified object in this
     *          vector at position less than or equal to <code>index</code> in
     *          the vector, that is, the largest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k]) && (k &lt;= index)</tt> is 
     *          <tt>true</tt>; <code>-1</code> if the object is not found.
     *          (Returns <code>-1</code> if <tt>index</tt> is negative.)
     * @exception  IndexOutOfBoundsException  if <tt>index</tt> is greater
     *             than or equal to the current size of this vector.
     */
    public int lastIndexOf(Object elem, int index) {
        if (index >= elementCount) throw new IndexOutOfBoundsException(index + " >= " + elementCount);
        if (elem == null) {
            for (int i = index; i >= 0; i--) if (elementData[i] == null) return i;
        } else {
            for (int i = index; i >= 0; i--) if (elem.equals(elementData[i])) return i;
        }
        return -1;
    }

    /**
     * Returns the component at the specified index.<p>
     *
     * This method is identical in functionality to the get method
     * (which is part of the List interface).
     *
     * @param      index   an index into this vector.
     * @return     the component at the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the <tt>index</tt> 
     *             is negative or not less than the current size of this 
     *             <tt>ModVector</tt> object.
     *             given.
     * @see	   #get(int)
     * @see	   List
     */
    public E elementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }
        return (E) elementData[index];
    }

    /**
     * Returns the first component (the item at index <tt>0</tt>) of 
     * this vector.
     *
     * @return     the first component of this vector.
     * @exception  NoSuchElementException  if this vector has no components.
     */
    public E firstElement() {
        if (elementCount == 0) {
            throw new NoSuchElementException();
        }
        return (E) elementData[0];
    }

    /**
     * Returns the last component of the vector.
     *
     * @return  the last component of the vector, i.e., the component at index
     *          <code>size()&nbsp;-&nbsp;1</code>.
     * @exception  NoSuchElementException  if this vector is empty.
     */
    public E lastElement() {
        if (elementCount == 0) {
            throw new NoSuchElementException();
        }
        return (E) elementData[elementCount - 1];
    }

    /**
     * Sets the component at the specified <code>index</code> of this 
     * vector to be the specified object. The previous component at that 
     * position is discarded.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the set method
     * (which is part of the List interface). Note that the set method reverses
     * the order of the parameters, to more closely match array usage.  Note
     * also that the set method returns the old value that was stored at the
     * specified position.
     *
     * @param      obj     what the component is to be set to.
     * @param      index   the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see        List
     * @see	   #set(int, java.lang.Object)
     */
    public void setElementAt(E obj, int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }
        elementData[index] = obj;
    }

    /**
     * Deletes the component at the specified index. Each component in 
     * this vector with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this vector 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the remove method
     * (which is part of the List interface).  Note that the remove method
     * returns the old value that was stored at the specified position.
     *
     * @param      index   the index of the object to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #remove(int)
     * @see	   List
     */
    public void removeElementAt(int index) {
        modCount++;
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, j);
        }
        elementCount--;
        elementData[elementCount] = null;
    }

    /**
     * Inserts the specified object as a component in this vector at the 
     * specified <code>index</code>. Each component in this vector with 
     * an index greater or equal to the specified <code>index</code> is 
     * shifted upward to have an index one greater than the value it had 
     * previously. <p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than or equal to the current size of the vector. (If the
     * index is equal to the current size of the vector, the new element
     * is appended to the ModVector.)<p>
     *
     * This method is identical in functionality to the add(Object, int) method
     * (which is part of the List interface). Note that the add method reverses
     * the order of the parameters, to more closely match array usage.
     *
     * @param      obj     the component to insert.
     * @param      index   where to insert the new component.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #add(int, Object)
     * @see	   List
     */
    public void insertElementAt(E obj, int index) {
        modCount++;
        if (index > elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " > " + elementCount);
        }
        ensureCapacityHelper(elementCount + 1);
        System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
        elementData[index] = obj;
        elementCount++;
    }

    /**
     * Adds the specified component to the end of this vector, 
     * increasing its size by one. The capacity of this vector is 
     * increased if its size becomes greater than its capacity. <p>
     *
     * This method is identical in functionality to the add(Object) method
     * (which is part of the List interface).
     *
     * @param   obj   the component to be added.
     * @see	   #add(Object)
     * @see	   List
     */
    public void addElement(E obj) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this vector. If the object is found in this vector, each 
     * component in the vector with an index greater or equal to the 
     * object's index is shifted downward to have an index one smaller 
     * than the value it had previously.<p>
     *
     * This method is identical in functionality to the remove(Object) 
     * method (which is part of the List interface).
     *
     * @param   obj   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this
     *          vector; <code>false</code> otherwise.
     * @see	List#remove(Object)
     * @see	List
     */
    public boolean removeElement(Object obj) {
        modCount++;
        int i = indexOf(obj);
        if (i >= 0) {
            removeElementAt(i);
            return true;
        }
        return false;
    }

    /**
     * Removes all components from this vector and sets its size to zero.<p>
     *
     * This method is identical in functionality to the clear method
     * (which is part of the List interface).
     *
     * @see	#clear
     * @see	List
     */
    public void removeAllElements() {
        modCount++;
        for (int i = 0; i < elementCount; i++) elementData[i] = null;
        elementCount = 0;
    }

    /**
     * Returns a clone of this vector. The copy will contain a
     * reference to a clone of the internal data array, not a reference 
     * to the original internal data array of this <tt>ModVector</tt> object. 
     *
     * @return  a clone of this vector.
     */
    public Object clone() {
        try {
            ModVector<E> v = (ModVector<E>) super.clone();
            v.elementData = new Object[elementCount];
            System.arraycopy(elementData, 0, v.elementData, 0, elementCount);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Returns an array containing all of the elements in this ModVector
     * in the correct order.
     *
     * @since 1.2
     */
    public Object[] toArray() {
        Object[] result = new Object[elementCount];
        System.arraycopy(elementData, 0, result, 0, elementCount);
        return result;
    }

    /**
     * Returns an array containing all of the elements in this ModVector in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the ModVector fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this ModVector.<p>
     *
     * If the ModVector fits in the specified array with room to spare
     * (i.e., the array has more elements than the ModVector),
     * the element in the array immediately following the end of the
     * ModVector is set to null.  This is useful in determining the length
     * of the ModVector <em>only</em> if the caller knows that the ModVector
     * does not contain any null elements.
     *
     * @param a the array into which the elements of the ModVector are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the ModVector.
     * @exception ArrayStoreException the runtime type of a is not a supertype
     * of the runtime type of every element in this ModVector.
     * @throws NullPointerException if the given array is null.
     * @since 1.2
     */
    public <T> T[] toArray(T[] a) {
        if (a.length < elementCount) a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), elementCount);
        System.arraycopy(elementData, 0, a, 0, elementCount);
        if (a.length > elementCount) a[elementCount] = null;
        return a;
    }

    /**
     * Returns the element at the specified position in this ModVector.
     *
     * @param index index of element to return.
     * @return object at the specified index
     * @exception ArrayIndexOutOfBoundsException index is out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     * @since 1.2
     */
    public E get(int index) {
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index);
        return (E) elementData[index];
    }

    /**
     * Replaces the element at the specified position in this ModVector with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @exception ArrayIndexOutOfBoundsException index out of range
     *		  (index &lt; 0 || index &gt;= size()).
     * @since 1.2
     */
    public E set(int index, E element) {
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index);
        Object oldValue = elementData[index];
        elementData[index] = element;
        return (E) oldValue;
    }

    /**
     * Appends the specified element to the end of this ModVector.
     *
     * @param o element to be appended to this ModVector.
     * @return true (as per the general contract of Collection.add).
     * @since 1.2
     */
    public boolean add(E o) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = o;
        return true;
    }

    /**
     * Removes the first occurrence of the specified element in this ModVector
     * If the ModVector does not contain the element, it is unchanged.  More
     * formally, removes the element with the lowest index i such that
     * <code>(o==null ? get(i)==null : o.equals(get(i)))</code> (if such
     * an element exists).
     *
     * @param o element to be removed from this ModVector, if present.
     * @return true if the ModVector contained the specified element.
     * @since 1.2
     */
    public boolean remove(Object o) {
        return removeElement(o);
    }

    /**
     * Inserts the specified element at the specified position in this ModVector.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @exception ArrayIndexOutOfBoundsException index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     * @since 1.2
     */
    public void add(int index, E element) {
        insertElementAt(element, index);
    }

    /**
     * Removes the element at the specified position in this ModVector.
     * shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the ModVector.
     *
     * @exception ArrayIndexOutOfBoundsException index out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     * @param index the index of the element to removed.
     * @return element that was removed
     * @since 1.2
     */
    public E remove(int index) {
        modCount++;
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index);
        Object oldValue = elementData[index];
        int numMoved = elementCount - index - 1;
        if (numMoved > 0) System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--elementCount] = null;
        return (E) oldValue;
    }

    /**
     * Removes all of the elements from this ModVector.  The ModVector will
     * be empty after this call returns (unless it throws an exception).
     *
     * @since 1.2
     */
    public void clear() {
        removeAllElements();
    }

    /**
     * Returns true if this ModVector contains all of the elements in the
     * specified Collection.
     *
     * @param   c a collection whose elements will be tested for containment
     *          in this ModVector
     * @return true if this ModVector contains all of the elements in the
     *	       specified collection.
     * @throws NullPointerException if the specified collection is null.
     */
    public boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    /**
     * Appends all of the elements in the specified Collection to the end of
     * this ModVector, in the order that they are returned by the specified
     * Collection's Iterator.  The behavior of this operation is undefined if
     * the specified Collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified Collection is this ModVector, and this ModVector is nonempty.)
     *
     * @param c elements to be inserted into this ModVector.
     * @return <tt>true</tt> if this ModVector changed as a result of the call.
     * @throws NullPointerException if the specified collection is null.
     * @since 1.2
     */
    public boolean addAll(Collection<? extends E> c) {
        modCount++;
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);
        System.arraycopy(a, 0, elementData, elementCount, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    /**
     * Removes from this ModVector all of its elements that are contained in the
     * specified Collection.
     *
     * @param c a collection of elements to be removed from the ModVector
     * @return true if this ModVector changed as a result of the call.
     * @throws NullPointerException if the specified collection is null.
     * @since 1.2
     */
    public boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    /**
     * Retains only the elements in this ModVector that are contained in the
     * specified Collection.  In other words, removes from this ModVector all
     * of its elements that are not contained in the specified Collection. 
     *
     * @param c a collection of elements to be retained in this ModVector
     *          (all other elements are removed)
     * @return true if this ModVector changed as a result of the call.
     * @throws NullPointerException if the specified collection is null.
     * @since 1.2
     */
    public boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    /**
     * Inserts all of the elements in in the specified Collection into this
     * ModVector at the specified position.  Shifts the element currently at
     * that position (if any) and any subsequent elements to the right
     * (increases their indices).  The new elements will appear in the ModVector  
     * in the order that they are returned by the specified Collection's
     * iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c elements to be inserted into this ModVector.
     * @return <tt>true</tt> if this ModVector changed as a result of the call.
     * @exception ArrayIndexOutOfBoundsException index out of range (index
     *		  &lt; 0 || index &gt; size()).
     * @throws NullPointerException if the specified collection is null.
     * @since 1.2
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        modCount++;
        if (index < 0 || index > elementCount) throw new ArrayIndexOutOfBoundsException(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);
        int numMoved = elementCount - index;
        if (numMoved > 0) System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        System.arraycopy(a, 0, elementData, index, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    /**
     * Compares the specified Object with this ModVector for equality.  Returns
     * true if and only if the specified Object is also a List, both Lists
     * have the same size, and all corresponding pairs of elements in the two
     * Lists are <em>equal</em>.  (Two elements <code>e1</code> and
     * <code>e2</code> are <em>equal</em> if <code>(e1==null ? e2==null :
     * e1.equals(e2))</code>.)  In other words, two Lists are defined to be
     * equal if they contain the same elements in the same order.
     *
     * @param o the Object to be compared for equality with this ModVector.
     * @return true if the specified Object is equal to this ModVector
     */
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Returns the hash code value for this ModVector.
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a string representation of this ModVector, containing
     * the String representation of each element.
     */
    public String toString() {
        return super.toString();
    }

    /**
     * Returns a view of the portion of this List between fromIndex,
     * inclusive, and toIndex, exclusive.  (If fromIndex and ToIndex are
     * equal, the returned List is empty.)  The returned List is backed by this
     * List, so changes in the returned List are reflected in this List, and
     * vice-versa.  The returned List supports all of the optional List
     * operations supported by this List.<p>
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).   Any operation that expects
     * a List can be used as a range operation by operating on a subList view
     * instead of a whole List.  For example, the following idiom
     * removes a range of elements from a List:
     * <pre>
     *	    list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for indexOf and lastIndexOf,
     * and all of the algorithms in the Collections class can be applied to
     * a subList.<p>
     *
     * The semantics of the List returned by this method become undefined if
     * the backing list (i.e., this List) is <i>structurally modified</i> in
     * any way other than via the returned List.  (Structural modifications are
     * those that change the size of the List, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this List.
     * @throws IndexOutOfBoundsException endpoint index value out of range
     *         <code>(fromIndex &lt; 0 || toIndex &gt; size)</code>
     * @throws IllegalArgumentException endpoint indices out of order
     *	       <code>(fromIndex &gt; toIndex)</code>
     */
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the ArrayList by (toIndex - fromIndex) elements.  (If
     * toIndex==fromIndex, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = elementCount - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
        int newElementCount = elementCount - (toIndex - fromIndex);
        while (elementCount != newElementCount) elementData[--elementCount] = null;
    }

    /**
     * Save the state of the <tt>ModVector</tt> instance to a stream (that
     * is, serialize it).  This method is present merely for synchronization.
     * It just calls the default readObject method.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
    }
}
