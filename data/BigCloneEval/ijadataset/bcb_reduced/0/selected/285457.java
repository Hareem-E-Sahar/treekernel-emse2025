package org.enerj.apache.commons.collections.map;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.enerj.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.enerj.apache.commons.collections.keyvalue.AbstractMapEntryDecorator;
import org.enerj.apache.commons.collections.set.AbstractSetDecorator;

/**
 * An abstract base class that simplifies the task of creating map decorators.
 * <p>
 * The Map API is very difficult to decorate correctly, and involves implementing
 * lots of different classes. This class exists to provide a simpler API.
 * <p>
 * Special hook methods are provided that are called when objects are added to
 * the map. By overriding these methods, the input can be validated or manipulated.
 * In addition to the main map methods, the entrySet is also affected, which is
 * the hardest part of writing map implementations.
 * <p>
 * This class is package-scoped, and may be withdrawn or replaced in future
 * versions of Commons Collections.
 *
 * @since Commons Collections 3.1
 * @version $Revision: 155406 $ $Date: 2005-02-26 12:55:26 +0000 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
abstract class AbstractInputCheckedMapDecorator extends AbstractMapDecorator {

    /**
     * Constructor only used in deserialization, do not use otherwise.
     */
    protected AbstractInputCheckedMapDecorator() {
        super();
    }

    /**
     * Constructor that wraps (not copies).
     * 
     * @param map  the map to decorate, must not be null
     * @throws IllegalArgumentException if map is null
     */
    protected AbstractInputCheckedMapDecorator(Map map) {
        super(map);
    }

    /**
     * Hook method called when a value is being set using <code>setValue</code>.
     * <p>
     * An implementation may validate the value and throw an exception
     * or it may transform the value into another object.
     * <p>
     * This implementation returns the input value.
     * 
     * @param value  the value to check
     * @throws UnsupportedOperationException if the map may not be changed by setValue
     * @throws IllegalArgumentException if the specified value is invalid
     * @throws ClassCastException if the class of the specified value is invalid
     * @throws NullPointerException if the specified value is null and nulls are invalid
     */
    protected abstract Object checkSetValue(Object value);

    /**
     * Hook method called to determine if <code>checkSetValue</code> has any effect.
     * <p>
     * An implementation should return false if the <code>checkSetValue</code> method
     * has no effect as this optimises the implementation.
     * <p>
     * This implementation returns <code>true</code>.
     * 
     * @return true always
     */
    protected boolean isSetValueChecking() {
        return true;
    }

    public Set entrySet() {
        if (isSetValueChecking()) {
            return new EntrySet(map.entrySet(), this);
        } else {
            return map.entrySet();
        }
    }

    /**
     * Implementation of an entry set that checks additions via setValue.
     */
    static class EntrySet extends AbstractSetDecorator {

        /** The parent map */
        private final AbstractInputCheckedMapDecorator parent;

        protected EntrySet(Set set, AbstractInputCheckedMapDecorator parent) {
            super(set);
            this.parent = parent;
        }

        public Iterator iterator() {
            return new EntrySetIterator(collection.iterator(), parent);
        }

        public Object[] toArray() {
            Object[] array = collection.toArray();
            for (int i = 0; i < array.length; i++) {
                array[i] = new MapEntry((Map.Entry) array[i], parent);
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
                result[i] = new MapEntry((Map.Entry) result[i], parent);
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
    }

    /**
     * Implementation of an entry set iterator that checks additions via setValue.
     */
    static class EntrySetIterator extends AbstractIteratorDecorator {

        /** The parent map */
        private final AbstractInputCheckedMapDecorator parent;

        protected EntrySetIterator(Iterator iterator, AbstractInputCheckedMapDecorator parent) {
            super(iterator);
            this.parent = parent;
        }

        public Object next() {
            Map.Entry entry = (Map.Entry) iterator.next();
            return new MapEntry(entry, parent);
        }
    }

    /**
     * Implementation of a map entry that checks additions via setValue.
     */
    static class MapEntry extends AbstractMapEntryDecorator {

        /** The parent map */
        private final AbstractInputCheckedMapDecorator parent;

        protected MapEntry(Map.Entry entry, AbstractInputCheckedMapDecorator parent) {
            super(entry);
            this.parent = parent;
        }

        public Object setValue(Object value) {
            value = parent.checkSetValue(value);
            return entry.setValue(value);
        }
    }
}
