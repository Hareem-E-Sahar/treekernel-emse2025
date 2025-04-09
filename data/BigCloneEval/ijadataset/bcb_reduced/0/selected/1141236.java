package org.jenetics.util;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version $Id: ArrayList.java,v 1.2 2009-03-11 21:27:36 fwilhelm Exp $
 */
final class ArrayList<T> extends AbstractList<T> implements RandomAccess, java.io.Serializable {

    private static final long serialVersionUID = -3687635182118067928L;

    private final Array<T> _array;

    public ArrayList(final Array<T> array) {
        Validator.notNull(array, "Array");
        _array = array;
    }

    @Override
    public T get(final int index) {
        return _array.get(index);
    }

    @Override
    public T set(int index, T element) {
        final T old = _array.get(index);
        _array.set(index, element);
        return old;
    }

    @Override
    public int size() {
        return _array.length();
    }

    @Override
    public int indexOf(final Object element) {
        return _array.indexOf(element);
    }

    @Override
    public boolean contains(final Object element) {
        return indexOf(element) != -1;
    }

    @Override
    public Object[] toArray() {
        final Object[] array = new Object[_array.length()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = _array.get(i);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(final E[] array) {
        if (array.length < _array.length()) {
            final E[] copy = (E[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), _array.length());
            for (int i = 0; i < _array.length(); ++i) {
                copy[i] = (E) _array.get(i);
            }
            return copy;
        }
        System.arraycopy(_array._array, _array._start, array, 0, array.length);
        return array;
    }
}
