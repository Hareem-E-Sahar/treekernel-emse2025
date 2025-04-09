package org.jenetics.util;

import static org.jenetics.util.object.nonNull;
import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version $Id: ArraySeqList.java 1409 2012-03-21 20:18:35Z fwilhelm $
 */
class ArraySeqList<T> extends AbstractList<T> implements RandomAccess {

    final ArraySeq<T> _array;

    public ArraySeqList(final ArraySeq<T> array) {
        _array = nonNull(array, "ArrayBase");
    }

    @Override
    public T get(final int index) {
        return _array.get(index);
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
        return _array.toArray();
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
