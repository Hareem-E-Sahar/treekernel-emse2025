package net.sf.joafip.java.util.support.arraylist;

import java.io.Serializable;
import java.util.ListIterator;
import net.sf.joafip.AssertNotNull;
import net.sf.joafip.StorableClass;
import net.sf.joafip.StoreNotUseStandardSerialization;
import net.sf.joafip.java.util.support.IEqualsHashComparator;
import net.sf.joafip.java.util.support.IListSupport;
import net.sf.joafip.java.util.support.ISupportListener;
import net.sf.joafip.store.service.proxy.IInstanceFactory;

/**
 * support for persistent array list<br>
 * 
 * @author luc peuvrier
 * 
 * @param <E>
 */
@StoreNotUseStandardSerialization
@StorableClass
public class ArrayListSupport<E> implements IListSupport<E>, Iterable<E>, Cloneable, Serializable {

    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    private static final String UNCHECKED = "unchecked";

    /**
	 * 
	 */
    private static final long serialVersionUID = 6396398099424354925L;

    /**
	 * the data array <br>
	 * not transient for persistence<br>
	 */
    @AssertNotNull
    private E[] array;

    /**
	 * number of element in the array, may be less than array size not transient
	 * for persistence<br>
	 */
    private int size = 0;

    /**
	 * current element index for {@link #contains(Object)}<br>
	 * not transient for persistence<br>
	 */
    private int currentIndex;

    /**
	 * comparator to use instead of {@link Object#equals(Object)}, null if none.
	 */
    private final IEqualsHashComparator comparator;

    private int modificationCount = 0;

    private ISupportListener<E> listener;

    private final transient IInstanceFactory instanceFactory;

    public ArrayListSupport(final IEqualsHashComparator comparator) {
        this(null, comparator);
    }

    protected ArrayListSupport(final IInstanceFactory instanceFactory, final IEqualsHashComparator comparator) {
        this(instanceFactory, DEFAULT_INITIAL_CAPACITY, comparator);
    }

    @SuppressWarnings("rawtypes")
    public static ArrayListSupport newInstance(final IInstanceFactory instanceFactory, final IEqualsHashComparator comparator) {
        ArrayListSupport newInstance;
        if (instanceFactory == null) {
            newInstance = new ArrayListSupport(comparator);
        } else {
            newInstance = (ArrayListSupport) instanceFactory.newInstance(ArrayListSupport.class, new Class[] { IEqualsHashComparator.class }, new Object[] { comparator });
        }
        return newInstance;
    }

    protected ArrayListSupport(final int initialCapacity, final IEqualsHashComparator comparator) {
        this(null, initialCapacity, comparator);
    }

    @SuppressWarnings(UNCHECKED)
    protected ArrayListSupport(final IInstanceFactory instanceFactory, final int initialCapacity, final IEqualsHashComparator comparator) {
        super();
        this.instanceFactory = instanceFactory;
        array = (E[]) new Object[initialCapacity];
        this.comparator = comparator;
    }

    @SuppressWarnings("rawtypes")
    public static ArrayListSupport newInstance(final IInstanceFactory instanceFactory, final int initialCapacity, final IEqualsHashComparator comparator) {
        ArrayListSupport newInstance;
        if (instanceFactory == null) {
            newInstance = new ArrayListSupport(initialCapacity, comparator);
        } else {
            newInstance = (ArrayListSupport) instanceFactory.newInstance(ArrayListSupport.class, new Class[] { int.class, IEqualsHashComparator.class }, new Object[] { initialCapacity, comparator });
        }
        return newInstance;
    }

    public int getModificationCount() {
        return modificationCount;
    }

    @Override
    public void setListener(final ISupportListener<E> listener) {
        this.listener = listener;
    }

    @Override
    public void removeListener() {
        listener = null;
    }

    private void fireAdded(final E added) {
        if (listener != null) {
            listener.notifyAdded(added);
        }
    }

    private void fireRemoved(final E removed) {
        if (listener != null) {
            listener.notifyRemoved(removed);
        }
    }

    @Override
    public E set(final E element, final int position) {
        if (position >= size) {
            throw new IndexOutOfBoundsException();
        }
        final E previousValue = array[position];
        fireRemoved(previousValue);
        array[position] = element;
        fireAdded(element);
        return previousValue;
    }

    @Override
    public E addAtEnd(final E element) {
        size++;
        checkArrayLength();
        array[size - 1] = element;
        fireAdded(element);
        modificationCount++;
        return null;
    }

    @SuppressWarnings(UNCHECKED)
    private void checkArrayLength() {
        if (size > array.length) {
            final Object[] oldArray = array;
            array = (E[]) new Object[oldArray.length << 1];
            System.arraycopy(oldArray, 0, array, 0, oldArray.length);
        }
    }

    @Override
    public E addAtBegin(final E element) {
        size++;
        checkArrayLength();
        for (int index = size - 1; index > 0; index--) {
            array[index] = array[index - 1];
        }
        array[0] = element;
        fireAdded(element);
        modificationCount++;
        return null;
    }

    @Override
    public E add(final int addIndex, final E element) {
        if (addIndex < 0 || addIndex > size) {
            throw new IndexOutOfBoundsException();
        }
        size++;
        checkArrayLength();
        System.arraycopy(array, addIndex, array, addIndex + 1, size - addIndex - 1);
        array[addIndex] = element;
        fireAdded(element);
        modificationCount++;
        return null;
    }

    @Override
    public E addReplace(final E element) {
        E previousValue = null;
        final boolean found = contains(element);
        if (found) {
            previousValue = (E) array[currentIndex];
            fireRemoved(previousValue);
            array[currentIndex] = element;
            fireAdded(element);
        } else {
            addAtEnd(element);
        }
        return previousValue;
    }

    @Override
    public E remove(final Object object) {
        E previousValue = null;
        final boolean found = contains(object);
        if (found) {
            previousValue = (E) array[currentIndex];
            remove(currentIndex);
        }
        return previousValue;
    }

    @Override
    public E remove(final int index) {
        final E removed = (E) array[index];
        for (int position = index; position < size - 1; position++) {
            array[position] = array[position + 1];
        }
        array[--size] = null;
        fireRemoved(removed);
        modificationCount++;
        return removed;
    }

    @Override
    public E removeFirst() {
        return remove(0);
    }

    @Override
    public E removeLast() {
        return remove(size - 1);
    }

    @Override
    public E getFirst() {
        final E first;
        if (size < 1) {
            first = null;
        } else {
            first = array[0];
        }
        return first;
    }

    @Override
    public E getLast() {
        final E last;
        if (size < 1) {
            last = null;
        } else {
            last = array[size - 1];
        }
        return last;
    }

    @Override
    public E get(final int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return array[index];
    }

    @Override
    public E get(final Object object) {
        final boolean found = contains(object);
        return found ? array[currentIndex] : null;
    }

    @Override
    public boolean contains(final Object object) {
        boolean found = false;
        for (currentIndex = 0; !found && currentIndex < size; currentIndex++) {
            final Object element = array[currentIndex];
            if (element == null) {
                found = object == null;
            } else if (comparator == null) {
                found = element.equals(object);
            } else {
                found = comparator.equals(element, object);
            }
        }
        currentIndex--;
        return found;
    }

    @Override
    public void clear() {
        if (listener != null) {
            for (int index = 0; index < size; index++) {
                fireRemoved(array[index]);
            }
        }
        size = 0;
        modificationCount++;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings(UNCHECKED)
    public void trimToSize() {
        if (size != array.length) {
            final E[] oldArray = array;
            array = (E[]) new Object[size];
            System.arraycopy(oldArray, 0, array, 0, size);
        }
    }

    @SuppressWarnings(UNCHECKED)
    public void ensureCapacity(final int minCapacity) {
        if (minCapacity > array.length) {
            final E[] oldArray = array;
            array = (E[]) new Object[minCapacity];
            System.arraycopy(oldArray, 0, array, 0, size);
        }
    }

    public int indexOf(final Object object) {
        final int index;
        if (contains(object)) {
            index = currentIndex;
        } else {
            index = -1;
        }
        return index;
    }

    public int lastIndexOf(final Object object) {
        boolean found = false;
        for (currentIndex = size - 1; !found && currentIndex >= 0; currentIndex--) {
            final Object element = array[currentIndex];
            if (element == null) {
                found = object == null;
            } else if (comparator == null) {
                found = element.equals(object);
            } else {
                found = comparator.equals(element, object);
            }
        }
        final int index;
        if (found) {
            index = currentIndex + 1;
        } else {
            index = -1;
        }
        return index;
    }

    @Override
    public ListIterator<E> iterator() {
        return new ArrayListSupportIterator<E>(this);
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public ListIterator<E> iterator(final int index) {
        return ArrayListSupportIterator.newInstance(instanceFactory, this, index);
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public ListIterator<E> descendingIterator() {
        return ArrayListSupportDescendingIterator.newInstance(instanceFactory, this);
    }

    @SuppressWarnings(UNCHECKED)
    public ArrayListSupportIterator<E> arrayListSupportIterator() {
        return ArrayListSupportIterator.newInstance(instanceFactory, this);
    }

    public Object[] toArray() {
        final Object[] array = new Object[size];
        System.arraycopy(this.array, 0, array, 0, size);
        return array;
    }

    @SuppressWarnings(UNCHECKED)
    public <T> T[] toArray(final T[] elements) {
        final T[] toArray;
        if (elements.length >= size) {
            toArray = elements;
        } else {
            toArray = (T[]) java.lang.reflect.Array.newInstance(elements.getClass().getComponentType(), size);
        }
        System.arraycopy(array, 0, toArray, 0, size);
        return toArray;
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public ArrayListSupport<E> clone() {
        final ArrayListSupport<E> arrayListSupport;
        try {
            arrayListSupport = (ArrayListSupport<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        final E[] newArray = (E[]) new Object[size];
        arrayListSupport.setArray(newArray);
        for (int index = 0; index < size; index++) {
            newArray[index] = array[index];
        }
        return arrayListSupport;
    }

    public void setArray(final E[] array) {
        this.array = array;
    }

    public E[] getArray() {
        return array;
    }

    @Override
    public boolean equals(final Object obj) {
        boolean equals;
        if (obj == this) {
            equals = true;
        } else if (obj == null) {
            equals = false;
        } else if (obj instanceof ArrayListSupport) {
            @SuppressWarnings("rawtypes") final ArrayListSupport other = (ArrayListSupport) obj;
            if (size == other.getSize()) {
                final Object[] otherArray = other.getArray();
                equals = true;
                if (comparator == null) {
                    for (int index = 0; equals && index < size; index++) {
                        equals = array[index].equals(otherArray[index]);
                    }
                } else {
                    for (int index = 0; equals && index < size; index++) {
                        equals = comparator.equals(array[index], otherArray[index]);
                    }
                }
            } else {
                equals = false;
            }
        } else {
            equals = false;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        if (comparator == null) {
            for (int index = 0; index < size; index++) {
                final E elt = array[index];
                hashCode = 31 * hashCode + (elt == null ? 0 : elt.hashCode());
            }
        } else {
            for (int index = 0; index < size; index++) {
                final E elt = array[index];
                hashCode = 31 * hashCode + (elt == null ? 0 : comparator.hashCodeOf(elt));
            }
        }
        return hashCode;
    }

    public IEqualsHashComparator getComparator() {
        return comparator;
    }
}
