package huf.data;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Various utility methods associated with data containers
 * also for interfacing <code>huf.data</code> containers with
 * <code>java.util</code> collections.
 */
public class DataUtils {

    /** No instantiation. */
    private DataUtils() {
    }

    /**
	 * Merge two arrays of any type.
	 *
	 * <p>
	 * This method accept both arrays of objects and primitives.
	 * </p>
	 *
	 * <p>
	 * Array elements are not modified in any way, and are not cloned or in any other way duplicated.
	 * </p>
	 *
	 * @param arr1 first source array
	 * @param arr2 second source array
	 * @return array with merged contents of argument arrays
	 */
    public static Object mergeArrays(Object arr1, Object arr2) {
        if (arr1 == null && arr2 == null) {
            throw new IllegalArgumentException("Both arr1 and arr2 may not be null");
        }
        Class<?> type1 = arr1 != null ? arr1.getClass().getComponentType() : null;
        if (arr1 != null && type1 == null) {
            throw new IllegalArgumentException("Argument arr1 of type " + arr1.getClass().getName() + " is not an array");
        }
        Class<?> type2 = arr2 != null ? arr2.getClass().getComponentType() : null;
        if (arr2 != null && type2 == null) {
            throw new IllegalArgumentException("Argument arr2 of type " + arr2.getClass().getName() + " is not an array");
        }
        if (arr1 != null && arr2 != null && !type1.equals(type2)) {
            throw new IllegalArgumentException("Incompatible array types: " + arr1.getClass().getComponentType().getName() + " and " + arr2.getClass().getComponentType().getName());
        }
        Class<?> type = arr1 != null ? type1 : type2;
        int len1 = arr1 != null ? Array.getLength(arr1) : 0;
        int len2 = arr2 != null ? Array.getLength(arr2) : 0;
        Object merged = Array.newInstance(type, len1 + len2);
        if (arr1 != null) {
            System.arraycopy(arr1, 0, merged, 0, len1);
        }
        if (arr2 != null) {
            System.arraycopy(arr2, 0, merged, len1, len2);
        }
        return merged;
    }

    /**
	 * Copy an array of any type.
	 *
	 * <p>
	 * This method accept both arrays of objects and primitives.
	 * </p>
	 *
	 * <p>
	 * Array elements are not modified in any way, and are not cloned or in any other way duplicated.
	 * For each array element <code>assert source[i] == copied[i];</code>.
	 * </p>
	 *
	 * @param arr source array
	 * @return copy of the arr array
	 */
    public static Object copy(Object arr) {
        if (arr == null) {
            throw new IllegalArgumentException("Argument arr may not be null");
        }
        Class<?> type = arr.getClass().getComponentType();
        if (type == null) {
            throw new IllegalArgumentException("Argument of type " + arr.getClass().getName() + " is not an array");
        }
        return copy(arr, type, Array.getLength(arr));
    }

    /**
	 * Copy an array of any type.
	 *
	 * <p>
	 * This method accept both arrays of objects and primitives.
	 * </p>
	 *
	 * <p>
	 * Array elements are not modified in any way, and are not cloned or in any other way duplicated.
	 * For each array element <code>assert source[i] == copied[i];</code>.
	 * </p>
	 *
	 * @param arr source array
	 * @param type type or array element (eg. to get a <code>String[]</code> result pass <code>String.class</code> here)
	 * @param size resulting array size 
	 * @return copy of the arr array
	 */
    public static Object copy(Object arr, Class<?> type, int size) {
        if (arr == null) {
            throw new IllegalArgumentException("Argument arr may not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Argument type may not be null");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size value: " + size);
        }
        Object copy = Array.newInstance(type, size);
        System.arraycopy(arr, 0, copy, 0, size);
        return copy;
    }

    public static Object[] array(Iterator<?> it, int size) {
        return array(it, size, new Object[size]);
    }

    public static Object[] array(Iterator<?> it, int size, Class<?> arrayType) {
        return array(it, size, (Object[]) Array.newInstance(arrayType, size));
    }

    public static Object[] array(Iterator<?> it, Object[] array) {
        return array(it, array.length, array);
    }

    /**
	 * Fill array with data read from iterator.
	 *
	 * @param it source iterator
	 * @param size number of elements expected in iterator; -1 disables size checking
	 * @param array target array
	 * @return array filled with data from iterator; it's the same object as <code>array</code> parameter
	 */
    public static Object[] array(Iterator<?> it, int size, Object[] array) {
        if (it == null) {
            throw new IllegalArgumentException("Source iterator may not be null");
        }
        if (array == null) {
            throw new IllegalArgumentException("Target array may not be null");
        }
        if (size > array.length) {
            throw new IllegalArgumentException("Declared size (" + size + ") may not exceed array length (" + array.length + ")");
        }
        int max = size > 0 ? (size > array.length ? array.length : size) : array.length;
        int i = 0;
        for (; it.hasNext() && i < max; i++) {
            array[i] = it.next();
        }
        if (size > 0) {
            if (i == array.length && it.hasNext()) {
                throw new IndexOutOfBoundsException("Array size too small: " + array.length);
            }
            if (i < size) {
                throw new IndexOutOfBoundsException("Insufficient number of elements read from iterator, read only " + i + " of declared " + size);
            }
        }
        return array;
    }

    public static Object[] array(Iterator<?> it) {
        return array(it, Object.class);
    }

    public static Object[] array(Iterator<?> it, Class<?> arrayType) {
        if (it == null) {
            throw new IllegalArgumentException("Source iterator may not be null");
        }
        if (arrayType == null) {
            throw new IllegalArgumentException("Array type argument may not be null");
        }
        if (!it.hasNext()) {
            return (Object[]) Array.newInstance(arrayType, 0);
        }
        Object[] arr = new Object[10];
        int idx = 0;
        while (it.hasNext()) {
            arr[idx++] = it.next();
            if (idx == arr.length && it.hasNext()) {
                Object[] newArr = new Object[arr.length < 100000 ? arr.length * 2 : arr.length + 100000];
                System.arraycopy(arr, 0, newArr, 0, arr.length);
                arr = newArr;
            }
        }
        if (arr.length == idx && arrayType == Object.class) {
            return arr;
        }
        Object[] finalArr = (Object[]) Array.newInstance(arrayType, idx);
        System.arraycopy(arr, 0, finalArr, 0, idx);
        return finalArr;
    }

    /**
	 * Fill a collection with contents of an iterator.
	 *
	 * @param <T> type of items processed
	 * @param collection target collection
	 * @param iterator source iterator
	 * @return target collection
	 */
    public static <T> Collection<T> fill(Collection<T> collection, Iterator<T> iterator) {
        if (collection == null) {
            throw new IllegalArgumentException("Argument collection may not be null");
        }
        if (iterator == null) {
            throw new IllegalArgumentException("Argument iterator may not be null");
        }
        while (iterator.hasNext()) {
            collection.add(iterator.next());
        }
        return collection;
    }

    /**
	 * Fill an array with contents of an iterator.
	 *
	 * @param array target array
	 * @param iterator source iterator
	 * @return target array
	 */
    public static Object[] fill(Object[] array, Iterator<?> iterator) {
        return fill(array, 0, iterator);
    }

    /**
	 * Fill an array with contents of an iterator.
	 *
	 * @param array target array
	 * @param offset initial offset
	 * @param iterator source iterator
	 * @return target array
	 */
    public static Object[] fill(Object[] array, int offset, Iterator<?> iterator) {
        if (array == null) {
            throw new IllegalArgumentException("Argument array may not be null");
        }
        if (offset < 0 || offset >= array.length) {
            throw new IllegalArgumentException("Invalid offset value: " + offset);
        }
        if (iterator == null) {
            throw new IllegalArgumentException("Argument iterator may not be null");
        }
        for (int i = offset; iterator.hasNext(); i++) {
            array[i] = iterator.next();
        }
        return array;
    }

    /**
	 * Convert {@link java.util.Iterator} to {@link huf.data.Iterator} iterator.
	 * 
	 * @param <T> Type of objects iterated by source and target iterators
	 * @param utilIterator source <code>java.util.Iterator</code>
	 * @return resulting <code>huf.data.Iterator</code>
	 */
    public static <T> Iterator<T> iterator(java.util.Iterator<T> utilIterator) {
        return utilIterator == null ? new EmptyIterator<T>() : new HufFromJdkIterator<T>(utilIterator);
    }

    private static class HufFromJdkIterator<T> implements Iterator<T> {

        /** Source iterator. */
        private final java.util.Iterator<T> utilIterator;

        /**
		 * Create new iterator adapter.
		 *
		 * @param utilIterator source iterator
		 */
        HufFromJdkIterator(java.util.Iterator<T> utilIterator) {
            this.utilIterator = utilIterator;
        }

        /** Helper variable for providing get() method functionality. */
        private T last = null;

        /**
		 * Checks if container has any more elements that may be returned by
		 * {@link huf.data.Iterator#next() next()} method.
		 *
		 * @return true if container has any more elements, false otherwise
		 */
        @Override
        public boolean hasNext() {
            return utilIterator.hasNext();
        }

        /**
		 * Returns next element from container.
		 *
		 * <p>
		 * If there are no more elements to return then <code>IndexOutOfBoundsException</code> is thrown.
		 * </p>
		 *
		 * @return next element from container
		 * @throws IndexOutOfBoundsException when there are no more elements to return
		 */
        @Override
        public T next() {
            last = utilIterator.next();
            return last;
        }

        /**
		 * Returns last element returned by {@link #next() next()}.
		 *
		 * <p>
		 * <b>This method throws <code>ArrayIndexOfBoundsException</code> before first call to {@link #next() next()} or
		 * when iterator goes back to the beginning because {@link #remove() remove()} was called one or more times.
		 * </p>
		 *
		 * <p>
		 * <b>Note:</b> subsequent calls to this method should always return the same object.
		 * </p>
		 *
		 * @return last element returned by <code>next()</code> or <code>null</code> in some situations (see above)
		 * @throws NoSuchElementException when <code>next()</code> method hasn't been called yet and there's no
		 *         element to return
		 */
        @Override
        public T get() {
            if (last == null) {
                throw new NoSuchElementException("Before first iterator element");
            }
            return last;
        }

        /**
		 * Removes last element returned by {@link #next() next()}.
		 *
		 * <p>
		 * After this method is called, iterator points to element just before the removed one.
		 * So subsequent calls to <code>remove()</code> without {@link #next() next()}
		 * would remove all elements from current position to the beginning of the container.
		 * Of course unsafe to do so :-).
		 * </p>
		 *
		 * @throws UnsupportedOperationException if the <code>Iterator</code> implementation don't support element removing.
		 *         This should be avoided and all <code>Iterator</code> should support this method.
		 */
        @Override
        public void remove() {
            utilIterator.remove();
        }

        /**
		 * Return iterator, as required by {@link Iterable} interface.
		 *
		 * @return this iterator
		 */
        @Override
        public Iterator<T> iterator() {
            return this;
        }
    }

    public static <T> Iterator<T> iterator(final Enumeration<T> en) {
        return en == null ? new EmptyIterator<T>() : new HufIteratorFromEnumeration<T>(en);
    }

    private static class HufIteratorFromEnumeration<T> implements Iterator<T> {

        /** Source enumeration. */
        private final Enumeration<T> en;

        /**
		 * Create new enumeration to HUF iterator adapter.
		 *
		 * @param en source enumeration
		 */
        HufIteratorFromEnumeration(Enumeration<T> en) {
            this.en = en;
        }

        /** Helper variable for providing get() method functionality. */
        private T last = null;

        /**
		 * Checks if container has any more elements that may be returned by
		 * {@link huf.data.Iterator#next() next()} method.
		 *
		 * @return true if container has any more elements, false otherwise
		 */
        @Override
        public boolean hasNext() {
            return en.hasMoreElements();
        }

        /**
		 * Returns next element from container.
		 *
		 * <p>
		 * If there are no more elements to return then
		 * <code>IndexOutOfBoundsException</code> is thrown.
		 * </p>
		 *
		 * @return next element from container
		 * @throws NoSuchElementException when <code>next()</code> method hasn't
		 *         been called yet and there's no element to return
		 */
        @Override
        public T next() {
            last = en.nextElement();
            return last;
        }

        /**
		 * Returns last element returned by {@link #next() next()}.
		 *
		 * <p>
		 * <b>This method throws <code>ArrayIndexOfBoundsException</code> before
		 * first call to {@link #next() next()} or when iterator goes back to the
		 * beginning because {@link #remove() remove()} was called one or more times.
		 * </p>
		 *
		 * <p>
		 * <b>Note:</b> subsequent calls to this method should always return the
		 * same object.
		 * </p>
		 *
		 * @return last element returned by <code>next()</code> or <code>null</code>
		 *         in some situations (see above)
		 * @throws NoSuchElementException when <code>next()</code> method hasn't been
		 *         called yet and there's no element to return
		 */
        @Override
        public T get() {
            if (last == null) {
                throw new NoSuchElementException("Before first iterator element");
            }
            return last;
        }

        /**
		 * Remove method is not supported in this bridge class.
		 *
		 * @throws UnsupportedOperationException since this method is not
		 *         implementable (it's a bridge class only)
		 */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Underlying java.util.Enumeration does not support removing");
        }

        /**
		 * Return iterator, as required by {@link Iterable} interface.
		 *
		 * @return this iterator
		 */
        @Override
        public Iterator<T> iterator() {
            return this;
        }
    }
}
