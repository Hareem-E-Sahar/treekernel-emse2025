package util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

/**
 * @class AssociativeMap
 *
 * @date 08-20-2001
 * @author Eric Crahen
 * @version 1.0
 *
 * This is a simple Map implementation that works just like a normal
 * Map with a few small changes. This Map can have more than one
 * mapping per key. It works by using an internal map which maps
 * each key to an ArrayList which holds all the mapped values.
 * This faster for associating groups of values by key and not
 * neccessarily for searching for specifc values.
 */
public class AssociativeMap extends AbstractMap implements MultiMap, Serializable {

    private HashMap keyMap = new HashMap();

    private ArrayList collectionList = new ArrayList();

    private Object[] copyList = new Object[4];

    /**
   * Removes all mappings from this map
   */
    public void clear() {
        keyMap.clear();
    }

    /**
   * Returns true if this map maps the key to this value.
   *
   * @param Object key
   * @param Object value
   *
   * @return boolean
   */
    public boolean contains(Object key, Object value) {
        if (key == null || value == null) return false;
        ArrayList list = (ArrayList) keyMap.get(key);
        return list.contains(value);
    }

    /**
   * Returns true if this map contains a mapping for the specified key.
   *
   * @param Object key
   * @return boolean
   */
    public boolean containsKey(Object key) {
        return keyMap.containsKey(key);
    }

    /**
   * Returns true if this map maps one or more keys to this value.
   *
   * @param Object value
   * @return boolean
   */
    public boolean containsValue(Object value) {
        for (int i = 0; i < collectionList.size(); i++) if (((ArrayList) collectionList.get(i)).contains(value)) return true;
        return false;
    }

    /**
   * Returns a set view of the keys contained in this map.
   *
   * @return Set
   */
    public Set keySet() {
        return keyMap.keySet();
    }

    /**
   * Returns the first value to which this map maps the specified key.
   *
   * @param Object key
   * @return Object
   */
    public Object get(Object key) {
        if (key == null) throw new IllegalArgumentException();
        ArrayList list = (ArrayList) keyMap.get(key);
        if (list == null) return null;
        if (list.size() > 0) return list.get(0);
        removeEntryList(key, list);
        return null;
    }

    /**
   * Returns true if this map contains no key-value mappings
   *
   * @return boolean
   */
    public boolean isEmpty() {
        if (!keyMap.isEmpty()) {
            for (int i = 0; i < collectionList.size(); i++) if (((ArrayList) collectionList.get(i)).size() != 0) return false;
        }
        return true;
    }

    /**
   * Associates the specified value with the specified key in this map. Does
   * not check for dupiclates. Returns the value added or null
   *
   * @param Object key
   * @param Object value
   * @return Object
   */
    public Object put(Object key, Object value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        return getEntryList(key).add(value) ? value : null;
    }

    /**
   * Get the entry set for the given key.
   */
    private final ArrayList getEntryList(Object key) {
        ArrayList list = (ArrayList) keyMap.get(key);
        if (list == null) {
            list = new ArrayList();
            keyMap.put(key, list);
            collectionList.add(list);
        }
        return list;
    }

    /**
   * Remove a keys collection from the control structures
   */
    private final void removeEntryList(Object key, ArrayList list) {
        keyMap.remove(key);
        collectionList.remove(list);
        list.clear();
    }

    /**
   * Removes the first mapping for this key from this map if present.
   *
   * @param Object key
   * @return Object
   */
    public Object remove(Object key) {
        ArrayList list = (ArrayList) keyMap.get(key);
        Object value;
        if (list == null || ((value = list.remove(0)) == null)) return null;
        if (list.size() == 0) removeEntryList(key, list);
        return value;
    }

    /**
   * Removes a particular mapping, or all mappings to the given
   * value if the key is null.
   *
   * @param Object key or all keys if null
   * @param Object value
   */
    public void remove(Object key, Object value) {
        if (key == null) removeAllValues(value); else {
            ArrayList list = (ArrayList) keyMap.get(key);
            int index;
            if (list != null && ((index = list.indexOf(value)) >= 0)) {
                list.remove(index);
                if (list.size() == 0) removeEntryList(key, list);
            }
        }
    }

    private final void removeAllValues(Object value) {
        int index;
        for (int i = 0; i < collectionList.size(); i++) {
            ArrayList list = (ArrayList) collectionList.get(i);
            if ((index = list.indexOf(value)) > -1) list.remove(index);
        }
    }

    /**
   * Removes the all mappings for this key from this map
   *
   * @param Object key
   */
    public void removeAll(Object key) {
        ArrayList list = (ArrayList) keyMap.get(key);
        if (list != null) removeEntryList(key, list);
    }

    /**
   * Copies all of the mappings from the specified map to this map
   *
   * @param Map
   */
    public void putAll(Map m) {
        for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
   * Create all of the mappings from the specified key to the elements in
   * the given Collection.
   *
   * @param Object
   * @param Collection
   */
    public void putAll(Object key, Collection c) {
        for (Iterator i = c.iterator(); i.hasNext(); ) put(key, i.next());
    }

    /**
   * Create all of the mappings from the specified key to the elements in
   * the given Collection.
   *
   * @param Object
   * @param Object[]
   */
    public void putAll(Object key, Object[] o) {
        for (int i = 0; i < o.length; i++) if (o[i] != null) put(key, o[i]);
    }

    /**
   * Add this item to the collection
   *
   * @param Object
   */
    public void put(Object value) {
        put(keyMap, value);
    }

    /**
   * Get all the values mapped to the given key
   *
   * @param Object key
   * @param Object[] avoid extra allocation
   */
    public Object[] getAll(Object key, Object[] array) {
        if (key == null) {
            Class t = (array == null) ? Object.class : array.getClass().getComponentType();
            int sz = size();
            if (array.length < sz) array = (Object[]) java.lang.reflect.Array.newInstance(t, sz);
            for (int x = 0, i = 0; i < collectionList.size(); i++) {
                ArrayList list = (ArrayList) collectionList.get(i);
                copyList = list.toArray(copyList);
                int z = list.size();
                System.arraycopy(copyList, 0, array, x, z);
                x += z;
            }
            if (array.length > sz) java.util.Arrays.fill(array, sz, array.length, null);
            java.util.Arrays.fill(copyList, null);
            return array;
        }
        ArrayList list = (ArrayList) keyMap.get(key);
        return list.toArray(array);
    }

    /**
   * Returns the number of key-value mappings in this map.
   *
   * @return int
   */
    public int size() {
        int sz = 0;
        for (int i = 0; i < collectionList.size(); i++) sz += ((ArrayList) collectionList.get(i)).size();
        return sz;
    }

    /**
   * Returns a set view of the mappings contained in this map. Using the
   * entrySet() is not optimized in any way.
   *
   * @return Set
   */
    public Set entrySet() {
        return new AbstractSet() {

            public Iterator iterator() {
                return new EntryIterator();
            }

            public int size() {
                return AssociativeMap.this.size();
            }
        };
    }

    /**
   * Returns a collection view of the values contained in this map.
   *
   * The set returned is read-only. The Collection returned uses a
   * single Iterator instance which is reset with each iterator()
   * invocation. This makes it convienent to cache an instance of the
   * values collection at any point, and simply call iterator() to
   * walk over the current values of the MultiMap.
   *
   * @return Collection
   */
    public Collection values() {
        return new AbstractCollection() {

            ValueIterator iter = new ValueIterator();

            public Iterator iterator() {
                iter.reset();
                return iter;
            }

            public int size() {
                return AssociativeMap.this.size();
            }
        };
    }

    /**
   * Returns a collection view of the values contained in this map
   * which map to a certain key.
   *
   * The set returned is read-only. The Collection returned uses a
   * single Iterator instance which is reset with each iterator()
   * invocation. This makes it convienent to cache an instance of the
   * values collection at any point, and simply call iterator() to
   * walk over the current values of the MultiMap for the requested
   * key.
   *
   * @param Object key
   * @return Collection
   */
    public Collection values(Object key) {
        if (key == null) throw new IllegalArgumentException();
        final Object k = key;
        return new AbstractCollection() {

            ValueIterator iter = new ValueIterator();

            public Iterator iterator() {
                iter.reset();
                iter.key = k;
                return iter;
            }

            public int size() {
                return (iter.list == null) ? 0 : iter.list.size();
            }
        };
    }

    /**
   * @class ValueIterator
   *
   * Creates an iterator that will walk over the elements in each
   * ArrayList that is mapped by some key.
   */
    protected class ValueIterator implements Iterator {

        int n = 0, m = 0;

        ArrayList list = null;

        Object key = null;

        /**
     * Set the key for this map
     *
     * @return Object
     */
        public void setKey(Object key) {
            this.key = key;
        }

        /**
     * Test for the existance of the next element
     *
     * @return boolean
     */
        public boolean hasNext() {
            while (list != null || n < collectionList.size()) {
                if (list != null && m < list.size()) return true;
                if (list == null) {
                    list = (ArrayList) collectionList.get(n++);
                    m = 0;
                    if (key == null || keyMap.get(key) == list) return true;
                }
                list = null;
            }
            key = null;
            return false;
        }

        /**
     * Get the next item in the iteration
     *
     * @return Object
     */
        public Object next() {
            return list.get(m++);
        }

        /**
     * Not implemented
     */
        public void remove() {
        }

        /**
     * Reset the Iterator.
     */
        public void reset() {
            n = 0;
            m = 0;
            list = null;
        }
    }

    /**
   * @class EntryIterator
   *
   * Creates an iterator that will walk over the elements in each
   * ArrayList that is mapped by some key. It will present them in
   * an entrySet (Map.Entry items) view
   */
    protected class EntryIterator implements Iterator {

        Iterator v = keyMap.entrySet().iterator();

        Iterator i;

        MultiMap.Entry e = new MultiMap.Entry();

        /**
     * Test for the existance of the next element
     *
     * @return boolean
     */
        public boolean hasNext() {
            while (i == null || !i.hasNext()) {
                if (!v.hasNext()) return false;
                Map.Entry entry = (Map.Entry) v.next();
                ArrayList list = (ArrayList) entry.getValue();
                if (list.size() > 0) {
                    e.key = entry.getKey();
                    i = list.iterator();
                }
            }
            return true;
        }

        /**
     * Get the next item in the iteration
     *
     * @return Object
     */
        public Object next() {
            e.val = i.next();
            return e;
        }

        /**
     * Not implemented
     */
        public void remove() {
        }

        /**
     * Reset the Iterator.
     */
        public void reset() {
            v = keyMap.entrySet().iterator();
            i = null;
        }
    }

    public static void main(String[] args) {
        MultiMap m = new AssociativeMap();
        m.put("A", "1");
        m.put("A", "2");
        m.put("B", "3");
        printAll(m);
        m.removeAll("A");
        printAll(m);
    }

    protected static void printAll(MultiMap m) {
        System.err.println("VALUES:");
        for (Iterator i = m.values("A").iterator(); i.hasNext(); ) System.err.println(i.next());
        System.err.println("ENTRIES:");
        for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) System.err.println(i.next());
    }
}
