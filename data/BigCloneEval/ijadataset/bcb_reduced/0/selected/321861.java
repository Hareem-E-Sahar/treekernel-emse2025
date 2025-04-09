package org.enerj.apache.commons.collections.map;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.enerj.apache.commons.collections.Unmodifiable;
import org.enerj.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.enerj.apache.commons.collections.keyvalue.AbstractMapEntryDecorator;
import org.enerj.apache.commons.collections.set.AbstractSetDecorator;

/**
 * Decorates a map entry <code>Set</code> to ensure it can't be altered.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 155406 $ $Date: 2005-02-26 12:55:26 +0000 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public final class UnmodifiableEntrySet extends AbstractSetDecorator implements Unmodifiable {

    /**
     * Factory method to create an unmodifiable set of Map Entry objects.
     * 
     * @param set  the set to decorate, must not be null
     * @throws IllegalArgumentException if set is null
     */
    public static Set decorate(Set set) {
        if (set instanceof Unmodifiable) {
            return set;
        }
        return new UnmodifiableEntrySet(set);
    }

    /**
     * Constructor that wraps (not copies).
     * 
     * @param set  the set to decorate, must not be null
     * @throws IllegalArgumentException if set is null
     */
    private UnmodifiableEntrySet(Set set) {
        super(set);
    }

    public boolean add(Object object) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection coll) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection coll) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection coll) {
        throw new UnsupportedOperationException();
    }

    public Iterator iterator() {
        return new UnmodifiableEntrySetIterator(collection.iterator());
    }

    public Object[] toArray() {
        Object[] array = collection.toArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = new UnmodifiableEntry((Map.Entry) array[i]);
        }
        return array;
    }

    public Object[] toArray(Object array[]) {
        Object[] result = array;
        if (array.length > 0) {
            result = (Object[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }
        result = collection.toArray(result);
        for (int i = 0; i < result.length; i++) {
            result[i] = new UnmodifiableEntry((Map.Entry) result[i]);
        }
        if (result.length > array.length) {
            return result;
        }
        System.arraycopy(result, 0, array, 0, result.length);
        if (array.length > result.length) {
            array[result.length] = null;
        }
        return array;
    }

    /**
     * Implementation of an entry set iterator.
     */
    static final class UnmodifiableEntrySetIterator extends AbstractIteratorDecorator {

        protected UnmodifiableEntrySetIterator(Iterator iterator) {
            super(iterator);
        }

        public Object next() {
            Map.Entry entry = (Map.Entry) iterator.next();
            return new UnmodifiableEntry(entry);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Implementation of a map entry that is unmodifiable.
     */
    static final class UnmodifiableEntry extends AbstractMapEntryDecorator {

        protected UnmodifiableEntry(Map.Entry entry) {
            super(entry);
        }

        public Object setValue(Object obj) {
            throw new UnsupportedOperationException();
        }
    }
}
