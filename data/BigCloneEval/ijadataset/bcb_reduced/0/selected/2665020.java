package net.sf.beanlib.utils.range;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Basically cloned from and identical to {@link java.util.TreeMap}, 
 * except
 * <ul>
 * <li>the class is made abstract;</li>
 * <li>the access modifier of some member fields are modified
 * (eg from private to protected); and</li>
 * <li>the internal static inner class Entry is renamed to NodeEntry to avoid ambiguity.</li>
 * </ul>  
 * 
 * The purpose is to facilitate further subclassing.
 *
 * @author Joe D. Velopar
 */
public abstract class ExtensibleTreeMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V>, Cloneable, java.io.Serializable {

    /**
     * The Comparator used to maintain order in this TreeMap, or null if this
     * TreeMap uses its elements natural ordering.
     * 
     * @serial
     */
    private Comparator<? super K> comparator = null;

    protected transient NodeEntry<K, V> root = null;

    /**
     * The number of entries in the tree
     */
    private transient int size = 0;

    /**
     * The number of structural modifications to the tree.
     */
    private transient int modCount = 0;

    private void incrementSize() {
        modCount++;
        size++;
    }

    private void decrementSize() {
        modCount++;
        size--;
    }

    /**
     * Constructs a new, empty map, sorted according to the keys' natural order.
     * All keys inserted into the map must implement the <tt>Comparable</tt>
     * interface. Furthermore, all such keys must be <i>mutually comparable</i>:
     * <tt>k1.compareTo(k2)</tt> must not throw a ClassCastException for any
     * elements <tt>k1</tt> and <tt>k2</tt> in the map. If the user attempts
     * to put a key into the map that violates this constraint (for example, the
     * user attempts to put a string key into a map whose keys are integers),
     * the <tt>put(Object key, Object
     * value)</tt> call will throw a
     * <tt>ClassCastException</tt>.
     * 
     * @see Comparable
     */
    public ExtensibleTreeMap() {
    }

    /**
     * Constructs a new, empty map, sorted according to the given comparator.
     * All keys inserted into the map must be <i>mutually comparable</i> by the
     * given comparator: <tt>comparator.compare(k1, k2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any keys <tt>k1</tt> and <tt>k2</tt>
     * in the map. If the user attempts to put a key into the map that violates
     * this constraint, the <tt>put(Object key, Object
     * value)</tt> call will
     * throw a <tt>ClassCastException</tt>.
     * 
     * @param c
     *            the comparator that will be used to sort this map. A
     *            <tt>null</tt> value indicates that the keys' <i>natural
     *            ordering</i> should be used.
     */
    public ExtensibleTreeMap(Comparator<? super K> c) {
        this.comparator = c;
    }

    /**
     * Constructs a new map containing the same mappings as the given map,
     * sorted according to the keys' <i>natural order</i>. All keys inserted
     * into the new map must implement the <tt>Comparable</tt> interface.
     * Furthermore, all such keys must be <i>mutually comparable</i>:
     * <tt>k1.compareTo(k2)</tt> must not throw a <tt>ClassCastException</tt>
     * for any elements <tt>k1</tt> and <tt>k2</tt> in the map. This method
     * runs in n*log(n) time.
     * 
     * @param m
     *            the map whose mappings are to be placed in this map.
     * @throws ClassCastException
     *             the keys in t are not Comparable, or are not mutually
     *             comparable.
     * @throws NullPointerException
     *             if the specified map is null.
     */
    public ExtensibleTreeMap(Map<? extends K, ? extends V> m) {
        putAll(m);
    }

    /**
     * Constructs a new map containing the same mappings as the given
     * <tt>SortedMap</tt>, sorted according to the same ordering. This method
     * runs in linear time.
     * 
     * @param m
     *            the sorted map whose mappings are to be placed in this map,
     *            and whose comparator is to be used to sort this map.
     * @throws NullPointerException
     *             if the specified sorted map is null.
     */
    public ExtensibleTreeMap(SortedMap<K, ? extends V> m) {
        comparator = m.comparator();
        try {
            buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * 
     * @param key
     *            key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     * @throws ClassCastException
     *             if the key cannot be compared with the keys currently in the
     *             map.
     * @throws NullPointerException
     *             key is <tt>null</tt> and this map uses natural ordering, or
     *             its comparator does not tolerate <tt>null</tt> keys.
     */
    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value. More formally, returns <tt>true</tt> if and only if
     * this map contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>. This operation
     * will probably require time linear in the Map size for most
     * implementations of Map.
     * 
     * @param value
     *            value whose presence in this Map is to be tested.
     * @return <tt>true</tt> if a mapping to <tt>value</tt> exists;
     *         <tt>false</tt> otherwise.
     * @since 1.2
     */
    @Override
    public boolean containsValue(Object value) {
        return (root == null ? false : (value == null ? valueSearchNull(root) : valueSearchNonNull(root, value)));
    }

    private boolean valueSearchNull(NodeEntry n) {
        if (n.value == null) return true;
        return (n.left != null && valueSearchNull(n.left)) || (n.right != null && valueSearchNull(n.right));
    }

    private boolean valueSearchNonNull(NodeEntry n, Object value) {
        if (value.equals(n.value)) return true;
        return (n.left != null && valueSearchNonNull(n.left, value)) || (n.right != null && valueSearchNonNull(n.right, value));
    }

    /**
     * Returns the value to which this map maps the specified key. Returns
     * <tt>null</tt> if the map contains no mapping for this key. A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>. The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     * 
     * @param key
     *            key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <tt>null</tt> if the map contains no mapping for the key.
     * @throws ClassCastException
     *             key cannot be compared with the keys currently in the map.
     * @throws NullPointerException
     *             key is <tt>null</tt> and this map uses natural ordering, or
     *             its comparator does not tolerate <tt>null</tt> keys.
     * 
     * @see #containsKey(Object)
     */
    @Override
    public V get(Object key) {
        NodeEntry<K, V> p = getEntry(key);
        return (p == null ? null : p.value);
    }

    /**
     * Returns the comparator used to order this map, or <tt>null</tt> if this
     * map uses its keys' natural order.
     * 
     * @return the comparator associated with this sorted map, or <tt>null</tt>
     *         if it uses its keys' natural sort method.
     */
    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * Returns the first (lowest) key currently in this sorted map.
     * 
     * @return the first (lowest) key currently in this sorted map.
     * @throws NoSuchElementException
     *             Map is empty.
     */
    public K firstKey() {
        return key(firstEntry());
    }

    /**
     * Returns the last (highest) key currently in this sorted map.
     * 
     * @return the last (highest) key currently in this sorted map.
     * @throws NoSuchElementException
     *             Map is empty.
     */
    public K lastKey() {
        return key(lastEntry());
    }

    /**
     * Copies all of the mappings from the specified map to this map. These
     * mappings replace any mappings that this map had for any of the keys
     * currently in the specified map.
     * 
     * @param map
     *            mappings to be stored in this map.
     * @throws ClassCastException
     *             class of a key or value in the specified map prevents it from
     *             being stored in this map.
     * 
     * @throws NullPointerException
     *             if the given map is <tt>null</tt> or this map does not
     *             permit <tt>null</tt> keys and a key in the specified map is
     *             <tt>null</tt>.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        int mapSize = map.size();
        if (size == 0 && mapSize != 0 && map instanceof SortedMap) {
            Comparator c = ((SortedMap) map).comparator();
            if (c == comparator || (c != null && c.equals(comparator))) {
                ++modCount;
                try {
                    buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
                } catch (java.io.IOException cannotHappen) {
                } catch (ClassNotFoundException cannotHappen) {
                }
                return;
            }
        }
        super.putAll(map);
    }

    /**
     * Returns this map's entry for the given key, or <tt>null</tt> if the map
     * does not contain an entry for the key.
     * 
     * @return this map's entry for the given key, or <tt>null</tt> if the map
     *         does not contain an entry for the key.
     * @throws ClassCastException
     *             if the key cannot be compared with the keys currently in the
     *             map.
     * @throws NullPointerException
     *             key is <tt>null</tt> and this map uses natural order, or
     *             its comparator does not tolerate * <tt>null</tt> keys.
     */
    private NodeEntry<K, V> getEntry(Object key) {
        NodeEntry<K, V> p = root;
        K k = (K) key;
        while (p != null) {
            int cmp = compare(k, p.key);
            if (cmp == 0) return p; else if (cmp < 0) p = p.left; else p = p.right;
        }
        return null;
    }

    /**
     * Gets the entry corresponding to the specified key; if no such entry
     * exists, returns the entry for the least key greater than the specified
     * key; if no such entry exists (i.e., the greatest key in the Tree is less
     * than the specified key), returns <tt>null</tt>.
     */
    private NodeEntry<K, V> getCeilEntry(K key) {
        NodeEntry<K, V> p = root;
        if (p == null) return null;
        while (true) {
            int cmp = compare(key, p.key);
            if (cmp == 0) {
                return p;
            } else if (cmp < 0) {
                if (p.left != null) p = p.left; else return p;
            } else {
                if (p.right != null) {
                    p = p.right;
                } else {
                    NodeEntry<K, V> parent = p.parent;
                    NodeEntry<K, V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
    }

    /**
     * Returns the entry for the greatest key less than the specified key; if no
     * such entry exists (i.e., the least key in the Tree is greater than the
     * specified key), returns <tt>null</tt>.
     */
    private NodeEntry<K, V> getPrecedingEntry(K key) {
        NodeEntry<K, V> p = root;
        if (p == null) return null;
        while (true) {
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right != null) p = p.right; else return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    NodeEntry<K, V> parent = p.parent;
                    NodeEntry<K, V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
    }

    /**
     * Returns the key corresponding to the specified Entry. Throw
     * NoSuchElementException if the Entry is <tt>null</tt>.
     */
    private static <K> K key(NodeEntry<K, ?> e) {
        if (e == null) throw new NoSuchElementException();
        return e.key;
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for this key, the old value is
     * replaced.
     * 
     * @param key
     *            key with which the specified value is to be associated.
     * @param value
     *            value to be associated with the specified key.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     * @throws ClassCastException
     *             key cannot be compared with the keys currently in the map.
     * @throws NullPointerException
     *             key is <tt>null</tt> and this map uses natural order, or
     *             its comparator does not tolerate <tt>null</tt> keys.
     */
    @Override
    public V put(K key, V value) {
        NodeEntry<K, V> t = root;
        if (t == null) {
            incrementSize();
            root = new NodeEntry<K, V>(key, value, null);
            return null;
        }
        while (true) {
            int cmp = compare(key, t.key);
            if (cmp == 0) {
                return t.setValue(value);
            } else if (cmp < 0) {
                if (t.left != null) {
                    t = t.left;
                } else {
                    incrementSize();
                    t.left = new NodeEntry<K, V>(key, value, t);
                    fixAfterInsertion(t.left);
                    return null;
                }
            } else {
                if (t.right != null) {
                    t = t.right;
                } else {
                    incrementSize();
                    t.right = new NodeEntry<K, V>(key, value, t);
                    fixAfterInsertion(t.right);
                    return null;
                }
            }
        }
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     * 
     * @param key
     *            key for which mapping should be removed
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     * 
     * @throws ClassCastException
     *             key cannot be compared with the keys currently in the map.
     * @throws NullPointerException
     *             key is <tt>null</tt> and this map uses natural order, or
     *             its comparator does not tolerate <tt>null</tt> keys.
     */
    @Override
    public V remove(Object key) {
        NodeEntry<K, V> p = getEntry(key);
        if (p == null) return null;
        V oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }

    /**
     * Removes all mappings from this TreeMap.
     */
    @Override
    public void clear() {
        modCount++;
        size = 0;
        root = null;
    }

    /**
     * Returns a shallow copy of this <tt>TreeMap</tt> instance. (The keys and
     * values themselves are not cloned.)
     * 
     * @return a shallow copy of this Map.
     */
    @Override
    public Object clone() {
        ExtensibleTreeMap<K, V> clone = null;
        try {
            clone = (ExtensibleTreeMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        clone.root = null;
        clone.size = 0;
        clone.modCount = 0;
        clone.entrySet = null;
        try {
            clone.buildFromSorted(size, entrySet().iterator(), null, null);
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
        return clone;
    }

    /**
     * This field is initialized to contain an instance of the entry set view
     * the first time this view is requested. The view is stateless, so there's
     * no reason to create more than one.
     */
    private transient volatile Set<Map.Entry<K, V>> entrySet = null;

    /**
     * Returns a Set view of the keys contained in this map. The set's iterator
     * will return the keys in ascending order. The map is backed by this
     * <tt>TreeMap</tt> instance, so changes to this map are reflected in the
     * Set, and vice-versa. The Set supports element removal, which removes the
     * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     * 
     * @return a set view of the keys contained in this TreeMap.
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<K>() {

                @Override
                public Iterator<K> iterator() {
                    return new KeyIterator();
                }

                @Override
                public int size() {
                    return ExtensibleTreeMap.this.size();
                }

                @Override
                public boolean contains(Object o) {
                    return containsKey(o);
                }

                @Override
                public boolean remove(Object o) {
                    int oldSize = size;
                    ExtensibleTreeMap.this.remove(o);
                    return size != oldSize;
                }

                @Override
                public void clear() {
                    ExtensibleTreeMap.this.clear();
                }
            };
        }
        return keySet;
    }

    /**
     * Returns a collection view of the values contained in this map. The
     * collection's iterator will return the values in the order that their
     * corresponding keys appear in the tree. The collection is backed by this
     * <tt>TreeMap</tt> instance, so changes to this map are reflected in the
     * collection, and vice-versa. The collection supports element removal,
     * which removes the corresponding mapping from the map through the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     * 
     * @return a collection view of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new AbstractCollection<V>() {

                @Override
                public Iterator<V> iterator() {
                    return new ValueIterator();
                }

                @Override
                public int size() {
                    return ExtensibleTreeMap.this.size();
                }

                @Override
                public boolean contains(Object o) {
                    for (NodeEntry<K, V> e = firstEntry(); e != null; e = successor(e)) if (valEquals(e.getValue(), o)) return true;
                    return false;
                }

                @Override
                public boolean remove(Object o) {
                    for (NodeEntry<K, V> e = firstEntry(); e != null; e = successor(e)) {
                        if (valEquals(e.getValue(), o)) {
                            deleteEntry(e);
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void clear() {
                    ExtensibleTreeMap.this.clear();
                }
            };
        }
        return values;
    }

    /**
     * Returns a set view of the mappings contained in this map. The set's
     * iterator returns the mappings in ascending key order. Each element in the
     * returned set is a <tt>Map.Entry</tt>. The set is backed by this map,
     * so changes to this map are reflected in the set, and vice-versa. The set
     * supports element removal, which removes the corresponding mapping from
     * the TreeMap, through the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     * 
     * @return a set view of the mappings contained in this map.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet<Map.Entry<K, V>>() {

                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new EntryIterator();
                }

                @Override
                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry)) return false;
                    Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                    V value = entry.getValue();
                    NodeEntry<K, V> p = getEntry(entry.getKey());
                    return p != null && valEquals(p.getValue(), value);
                }

                @Override
                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry)) return false;
                    Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                    V value = entry.getValue();
                    NodeEntry<K, V> p = getEntry(entry.getKey());
                    if (p != null && valEquals(p.getValue(), value)) {
                        deleteEntry(p);
                        return true;
                    }
                    return false;
                }

                @Override
                public int size() {
                    return ExtensibleTreeMap.this.size();
                }

                @Override
                public void clear() {
                    ExtensibleTreeMap.this.clear();
                }
            };
        }
        return entrySet;
    }

    /**
     * Returns a view of the portion of this map whose keys range from
     * <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive. (If
     * <tt>fromKey</tt> and <tt>toKey</tt> are equal, the returned sorted
     * map is empty.) The returned sorted map is backed by this map, so changes
     * in the returned sorted map are reflected in this map, and vice-versa. The
     * returned sorted map supports all optional map operations.
     * <p>
     * 
     * The sorted map returned by this method will throw an
     * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
     * less than <tt>fromKey</tt> or greater than or equal to <tt>toKey</tt>.
     * <p>
     * 
     * Note: this method always returns a <i>half-open range</i> (which
     * includes its low endpoint but not its high endpoint). If you need a
     * <i>closed range</i> (which includes both endpoints), and the key type
     * allows for calculation of the successor a given key, merely request the
     * subrange from <tt>lowEndpoint</tt> to <tt>successor(highEndpoint)</tt>.
     * For example, suppose that <tt>m</tt> is a sorted map whose keys are
     * strings. The following idiom obtains a view containing all of the
     * key-value mappings in <tt>m</tt> whose keys are between <tt>low</tt>
     * and <tt>high</tt>, inclusive:
     * 
     * <pre>
     * SortedMap sub = m.submap(low, high + &quot;\0&quot;);
     * </pre>
     * 
     * A similar technique can be used to generate an <i>open range</i> (which
     * contains neither endpoint). The following idiom obtains a view containing
     * all of the key-value mappings in <tt>m</tt> whose keys are between
     * <tt>low</tt> and <tt>high</tt>, exclusive:
     * 
     * <pre>
     * SortedMap sub = m.subMap(low + &quot;\0&quot;, high);
     * </pre>
     * 
     * @param fromKey
     *            low endpoint (inclusive) of the subMap.
     * @param toKey
     *            high endpoint (exclusive) of the subMap.
     * 
     * @return a view of the portion of this map whose keys range from
     *         <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.
     * 
     * @throws ClassCastException
     *             if <tt>fromKey</tt> and <tt>toKey</tt> cannot be compared
     *             to one another using this map's comparator (or, if the map
     *             has no comparator, using natural ordering).
     * @throws IllegalArgumentException
     *             if <tt>fromKey</tt> is greater than <tt>toKey</tt>.
     * @throws NullPointerException
     *             if <tt>fromKey</tt> or <tt>toKey</tt> is <tt>null</tt>
     *             and this map uses natural order, or its comparator does not
     *             tolerate <tt>null</tt> keys.
     */
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return new SubMap(fromKey, toKey);
    }

    /**
     * Returns a view of the portion of this map whose keys are strictly less
     * than <tt>toKey</tt>. The returned sorted map is backed by this map, so
     * changes in the returned sorted map are reflected in this map, and
     * vice-versa. The returned sorted map supports all optional map operations.
     * <p>
     * 
     * The sorted map returned by this method will throw an
     * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
     * greater than or equal to <tt>toKey</tt>.
     * <p>
     * 
     * Note: this method always returns a view that does not contain its (high)
     * endpoint. If you need a view that does contain this endpoint, and the key
     * type allows for calculation of the successor a given key, merely request
     * a headMap bounded by <tt>successor(highEndpoint)</tt>. For example,
     * suppose that suppose that <tt>m</tt> is a sorted map whose keys are
     * strings. The following idiom obtains a view containing all of the
     * key-value mappings in <tt>m</tt> whose keys are less than or equal to
     * <tt>high</tt>:
     * 
     * <pre>
     * SortedMap head = m.headMap(high + &quot;\0&quot;);
     * </pre>
     * 
     * @param toKey
     *            high endpoint (exclusive) of the headMap.
     * @return a view of the portion of this map whose keys are strictly less
     *         than <tt>toKey</tt>.
     * 
     * @throws ClassCastException
     *             if <tt>toKey</tt> is not compatible with this map's
     *             comparator (or, if the map has no comparator, if
     *             <tt>toKey</tt> does not implement <tt>Comparable</tt>).
     * @throws IllegalArgumentException
     *             if this map is itself a subMap, headMap, or tailMap, and
     *             <tt>toKey</tt> is not within the specified range of the
     *             subMap, headMap, or tailMap.
     * @throws NullPointerException
     *             if <tt>toKey</tt> is <tt>null</tt> and this map uses
     *             natural order, or its comparator does not tolerate
     *             <tt>null</tt> keys.
     */
    public SortedMap<K, V> headMap(K toKey) {
        return new SubMap(toKey, true);
    }

    /**
     * Returns a view of the portion of this map whose keys are greater than or
     * equal to <tt>fromKey</tt>. The returned sorted map is backed by this
     * map, so changes in the returned sorted map are reflected in this map, and
     * vice-versa. The returned sorted map supports all optional map operations.
     * <p>
     * 
     * The sorted map returned by this method will throw an
     * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
     * less than <tt>fromKey</tt>.
     * <p>
     * 
     * Note: this method always returns a view that contains its (low) endpoint.
     * If you need a view that does not contain this endpoint, and the element
     * type allows for calculation of the successor a given value, merely
     * request a tailMap bounded by <tt>successor(lowEndpoint)</tt>. For
     * example, suppose that <tt>m</tt> is a sorted map whose keys are
     * strings. The following idiom obtains a view containing all of the
     * key-value mappings in <tt>m</tt> whose keys are strictly greater than
     * <tt>low</tt>:
     * 
     * <pre>
     * SortedMap tail = m.tailMap(low + &quot;\0&quot;);
     * </pre>
     * 
     * @param fromKey
     *            low endpoint (inclusive) of the tailMap.
     * @return a view of the portion of this map whose keys are greater than or
     *         equal to <tt>fromKey</tt>.
     * @throws ClassCastException
     *             if <tt>fromKey</tt> is not compatible with this map's
     *             comparator (or, if the map has no comparator, if
     *             <tt>fromKey</tt> does not implement <tt>Comparable</tt>).
     * @throws IllegalArgumentException
     *             if this map is itself a subMap, headMap, or tailMap, and
     *             <tt>fromKey</tt> is not within the specified range of the
     *             subMap, headMap, or tailMap.
     * @throws NullPointerException
     *             if <tt>fromKey</tt> is <tt>null</tt> and this map uses
     *             natural order, or its comparator does not tolerate
     *             <tt>null</tt> keys.
     */
    public SortedMap<K, V> tailMap(K fromKey) {
        return new SubMap(fromKey, false);
    }

    private class SubMap extends AbstractMap<K, V> implements SortedMap<K, V>, java.io.Serializable {

        private static final long serialVersionUID = -6520786458950516097L;

        /**
         * fromKey is significant only if fromStart is false. Similarly, toKey
         * is significant only if toStart is false.
         */
        private boolean fromStart = false, toEnd = false;

        private K fromKey, toKey;

        SubMap(K fromKey, K toKey) {
            if (compare(fromKey, toKey) > 0) throw new IllegalArgumentException("fromKey > toKey");
            this.fromKey = fromKey;
            this.toKey = toKey;
        }

        SubMap(K key, boolean headMap) {
            compare(key, key);
            if (headMap) {
                fromStart = true;
                toKey = key;
            } else {
                toEnd = true;
                fromKey = key;
            }
        }

        SubMap(boolean fromStart, K fromKey, boolean toEnd, K toKey) {
            this.fromStart = fromStart;
            this.fromKey = fromKey;
            this.toEnd = toEnd;
            this.toKey = toKey;
        }

        @Override
        public boolean isEmpty() {
            return entrySet.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return inRange((K) key) && ExtensibleTreeMap.this.containsKey(key);
        }

        @Override
        public V get(Object key) {
            if (!inRange((K) key)) return null;
            return ExtensibleTreeMap.this.get(key);
        }

        @Override
        public V put(K key, V value) {
            if (!inRange(key)) throw new IllegalArgumentException("key out of range");
            return ExtensibleTreeMap.this.put(key, value);
        }

        public Comparator<? super K> comparator() {
            return comparator;
        }

        public K firstKey() {
            ExtensibleTreeMap.NodeEntry<K, V> e = fromStart ? firstEntry() : getCeilEntry(fromKey);
            K first = key(e);
            if (!toEnd && compare(first, toKey) >= 0) throw (new NoSuchElementException());
            return first;
        }

        public K lastKey() {
            ExtensibleTreeMap.NodeEntry<K, V> e = toEnd ? lastEntry() : getPrecedingEntry(toKey);
            K last = key(e);
            if (!fromStart && compare(last, fromKey) < 0) throw (new NoSuchElementException());
            return last;
        }

        private transient Set<Map.Entry<K, V>> entrySet = new EntrySetView();

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return entrySet;
        }

        private class EntrySetView extends AbstractSet<Map.Entry<K, V>> {

            private transient int size = -1, sizeModCount;

            @Override
            public int size() {
                if (size == -1 || sizeModCount != ExtensibleTreeMap.this.modCount) {
                    size = 0;
                    sizeModCount = ExtensibleTreeMap.this.modCount;
                    Iterator i = iterator();
                    while (i.hasNext()) {
                        size++;
                        i.next();
                    }
                }
                return size;
            }

            @Override
            public boolean isEmpty() {
                return !iterator().hasNext();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                K key = entry.getKey();
                if (!inRange(key)) return false;
                ExtensibleTreeMap.NodeEntry node = getEntry(key);
                return node != null && valEquals(node.getValue(), entry.getValue());
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                K key = entry.getKey();
                if (!inRange(key)) return false;
                ExtensibleTreeMap.NodeEntry<K, V> node = getEntry(key);
                if (node != null && valEquals(node.getValue(), entry.getValue())) {
                    deleteEntry(node);
                    return true;
                }
                return false;
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new SubMapEntryIterator((fromStart ? firstEntry() : getCeilEntry(fromKey)), (toEnd ? null : getCeilEntry(toKey)));
            }
        }

        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            if (!inRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
            if (!inRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
            return new SubMap(fromKey, toKey);
        }

        public SortedMap<K, V> headMap(K toKey) {
            if (!inRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
            return new SubMap(fromStart, fromKey, false, toKey);
        }

        public SortedMap<K, V> tailMap(K fromKey) {
            if (!inRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
            return new SubMap(false, fromKey, toEnd, toKey);
        }

        private boolean inRange(K key) {
            return (fromStart || compare(key, fromKey) >= 0) && (toEnd || compare(key, toKey) < 0);
        }

        private boolean inRange2(K key) {
            return (fromStart || compare(key, fromKey) >= 0) && (toEnd || compare(key, toKey) <= 0);
        }
    }

    /**
     * TreeMap Iterator.
     */
    private abstract class PrivateEntryIterator<T> implements Iterator<T> {

        private int expectedModCount = ExtensibleTreeMap.this.modCount;

        private NodeEntry<K, V> lastReturned = null;

        NodeEntry<K, V> next;

        PrivateEntryIterator() {
            next = firstEntry();
        }

        PrivateEntryIterator(NodeEntry<K, V> first) {
            next = first;
        }

        public boolean hasNext() {
            return next != null;
        }

        final NodeEntry<K, V> nextEntry() {
            if (next == null) throw new NoSuchElementException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            lastReturned = next;
            next = successor(next);
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (lastReturned.left != null && lastReturned.right != null) next = lastReturned;
            deleteEntry(lastReturned);
            expectedModCount++;
            lastReturned = null;
        }
    }

    private class EntryIterator extends PrivateEntryIterator<Map.Entry<K, V>> {

        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    private class KeyIterator extends PrivateEntryIterator<K> {

        public K next() {
            return nextEntry().key;
        }
    }

    private class ValueIterator extends PrivateEntryIterator<V> {

        public V next() {
            return nextEntry().value;
        }
    }

    private class SubMapEntryIterator extends PrivateEntryIterator<Map.Entry<K, V>> {

        private final K firstExcludedKey;

        SubMapEntryIterator(NodeEntry<K, V> first, NodeEntry<K, V> firstExcluded) {
            super(first);
            firstExcludedKey = (firstExcluded == null ? null : firstExcluded.key);
        }

        @Override
        public boolean hasNext() {
            return next != null && next.key != firstExcludedKey;
        }

        public Map.Entry<K, V> next() {
            if (next == null || next.key == firstExcludedKey) throw new NoSuchElementException();
            return nextEntry();
        }
    }

    /**
     * Compares two keys using the correct comparison method for this TreeMap.
     */
    private int compare(K k1, K k2) {
        return (comparator == null ? ((Comparable<K>) k1).compareTo(k2) : comparator.compare((K) k1, (K) k2));
    }

    /**
     * Test two values for equality. Differs from o1.equals(o2) only in that it
     * copes with <tt>null</tt> o1 properly.
     */
    private static boolean valEquals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }

    private static final boolean RED = false;

    private static final boolean BLACK = true;

    /**
     * Node in the Tree. Doubles as a means to pass key-value pairs back to user
     * (see Map.Entry).
     */
    static class NodeEntry<K, V> implements Map.Entry<K, V> {

        K key;

        V value;

        NodeEntry<K, V> left = null;

        NodeEntry<K, V> right = null;

        NodeEntry<K, V> parent;

        boolean color = BLACK;

        /**
         * Make a new cell with given key, value, and parent, and with
         * <tt>null</tt> child links, and BLACK color.
         */
        NodeEntry(K key, V value, NodeEntry<K, V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        /**
         * Returns the key.
         * 
         * @return the key.
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value associated with the key.
         * 
         * @return the value associated with the key.
         */
        public V getValue() {
            return value;
        }

        /**
         * Replaces the value currently associated with the key with the given
         * value.
         * 
         * @return the value associated with the key before this method was
         *         called.
         */
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
        }

        @Override
        public int hashCode() {
            int keyHash = (key == null ? 0 : key.hashCode());
            int valueHash = (value == null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function). Returns null if the TreeMap is empty.
     */
    private NodeEntry<K, V> firstEntry() {
        NodeEntry<K, V> p = root;
        if (p != null) while (p.left != null) p = p.left;
        return p;
    }

    /**
     * Returns the last Entry in the TreeMap (according to the TreeMap's
     * key-sort function). Returns null if the TreeMap is empty.
     */
    private NodeEntry<K, V> lastEntry() {
        NodeEntry<K, V> p = root;
        if (p != null) while (p.right != null) p = p.right;
        return p;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    private NodeEntry<K, V> successor(NodeEntry<K, V> t) {
        if (t == null) return null; else if (t.right != null) {
            NodeEntry<K, V> p = t.right;
            while (p.left != null) p = p.left;
            return p;
        } else {
            NodeEntry<K, V> p = t.parent;
            NodeEntry<K, V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Balancing operations.
     * 
     * Implementations of rebalancings during insertion and deletion are
     * slightly different than the CLR version. Rather than using dummy
     * nilnodes, we use a set of accessors that deal properly with null. They
     * are used to avoid messiness surrounding nullness checks in the main
     * algorithms.
     */
    private static <K, V> boolean colorOf(NodeEntry<K, V> p) {
        return (p == null ? BLACK : p.color);
    }

    private static <K, V> NodeEntry<K, V> parentOf(NodeEntry<K, V> p) {
        return (p == null ? null : p.parent);
    }

    private static <K, V> void setColor(NodeEntry<K, V> p, boolean c) {
        if (p != null) p.color = c;
    }

    private static <K, V> NodeEntry<K, V> leftOf(NodeEntry<K, V> p) {
        return (p == null) ? null : p.left;
    }

    private static <K, V> NodeEntry<K, V> rightOf(NodeEntry<K, V> p) {
        return (p == null) ? null : p.right;
    }

    /** From CLR * */
    private void rotateLeft(NodeEntry<K, V> p) {
        NodeEntry<K, V> r = p.right;
        p.right = r.left;
        if (r.left != null) r.left.parent = p;
        r.parent = p.parent;
        if (p.parent == null) root = r; else if (p.parent.left == p) p.parent.left = r; else p.parent.right = r;
        r.left = p;
        p.parent = r;
    }

    /** From CLR * */
    private void rotateRight(NodeEntry<K, V> p) {
        NodeEntry<K, V> l = p.left;
        p.left = l.right;
        if (l.right != null) l.right.parent = p;
        l.parent = p.parent;
        if (p.parent == null) root = l; else if (p.parent.right == p) p.parent.right = l; else p.parent.left = l;
        l.right = p;
        p.parent = l;
    }

    /** From CLR * */
    private void fixAfterInsertion(NodeEntry<K, V> x) {
        x.color = RED;
        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                NodeEntry<K, V> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    if (parentOf(parentOf(x)) != null) rotateRight(parentOf(parentOf(x)));
                }
            } else {
                NodeEntry<K, V> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    if (parentOf(parentOf(x)) != null) rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(NodeEntry<K, V> p) {
        decrementSize();
        if (p.left != null && p.right != null) {
            NodeEntry<K, V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        }
        NodeEntry<K, V> replacement = (p.left != null ? p.left : p.right);
        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null) root = replacement; else if (p == p.parent.left) p.parent.left = replacement; else p.parent.right = replacement;
            p.left = p.right = p.parent = null;
            if (p.color == BLACK) fixAfterDeletion(replacement);
        } else if (p.parent == null) {
            root = null;
        } else {
            if (p.color == BLACK) fixAfterDeletion(p);
            if (p.parent != null) {
                if (p == p.parent.left) p.parent.left = null; else if (p == p.parent.right) p.parent.right = null;
                p.parent = null;
            }
        }
    }

    /** From CLR * */
    private void fixAfterDeletion(NodeEntry<K, V> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                NodeEntry<K, V> sib = rightOf(parentOf(x));
                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }
                if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else {
                NodeEntry<K, V> sib = leftOf(parentOf(x));
                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }
                if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }
        setColor(x, BLACK);
    }

    private static final long serialVersionUID = 919286545866124006L;

    /**
     * Save the state of the <tt>TreeMap</tt> instance to a stream (i.e.,
     * serialize it).
     * 
     * @serialData The <i>size</i> of the TreeMap (the number of key-value
     *             mappings) is emitted (int), followed by the key (Object) and
     *             value (Object) for each key-value mapping represented by the
     *             TreeMap. The key-value mappings are emitted in key-order (as
     *             determined by the TreeMap's Comparator, or by the keys'
     *             natural ordering if the TreeMap has no Comparator).
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        for (Iterator<Map.Entry<K, V>> i = entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<K, V> e = i.next();
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    /**
     * Reconstitute the <tt>TreeMap</tt> instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        buildFromSorted(size, null, s, null);
    }

    /** Intended to be called only from TreeSet.readObject * */
    void readTreeSet(int size, java.io.ObjectInputStream s, V defaultVal) throws java.io.IOException, ClassNotFoundException {
        buildFromSorted(size, null, s, defaultVal);
    }

    /** Intended to be called only from TreeSet.addAll * */
    void addAllForTreeSet(SortedSet<Map.Entry<K, V>> set, V defaultVal) {
        try {
            buildFromSorted(set.size(), set.iterator(), null, defaultVal);
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
    }

    /**
     * Linear time tree building algorithm from sorted data. Can accept keys
     * and/or values from iterator or stream. This leads to too many parameters,
     * but seems better than alternatives. The four formats that this method
     * accepts are:
     * 
     * 1) An iterator of Map.Entries. (it != null, defaultVal == null). 2) An
     * iterator of keys. (it != null, defaultVal != null). 3) A stream of
     * alternating serialized keys and values. (it == null, defaultVal == null).
     * 4) A stream of serialized keys. (it == null, defaultVal != null).
     * 
     * It is assumed that the comparator of the TreeMap is already set prior to
     * calling this method.
     * 
     * @param size
     *            the number of keys (or key-value pairs) to be read from the
     *            iterator or stream.
     * @param it
     *            If non-null, new entries are created from entries or keys read
     *            from this iterator.
     * @param str
     *            If non-null, new entries are created from keys and possibly
     *            values read from this stream in serialized form. Exactly one
     *            of it and str should be non-null.
     * @param defaultVal
     *            if non-null, this default value is used for each value in the
     *            map. If null, each value is read from iterator or stream, as
     *            described above.
     * @throws IOException
     *             propagated from stream reads. This cannot occur if str is
     *             null.
     * @throws ClassNotFoundException
     *             propagated from readObject. This cannot occur if str is null.
     */
    private void buildFromSorted(int size, Iterator it, java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException {
        this.size = size;
        root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
    }

    /**
     * Recursive "helper method" that does the real work of the of the previous
     * method. Identically named parameters have identical definitions.
     * Additional parameters are documented below. It is assumed that the
     * comparator and size fields of the TreeMap are already set prior to
     * calling this method. (It ignores both fields.)
     * 
     * @param level
     *            the current level of tree. Initial call should be 0.
     * @param lo
     *            the first element index of this subtree. Initial should be 0.
     * @param hi
     *            the last element index of this subtree. Initial should be
     *            size-1.
     * @param redLevel
     *            the level at which nodes should be red. Must be equal to
     *            computeRedLevel for tree of this size.
     */
    private final NodeEntry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, Iterator it, java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException {
        if (hi < lo) return null;
        int mid = (lo + hi) / 2;
        NodeEntry<K, V> left = null;
        if (lo < mid) left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);
        K key;
        V value;
        if (it != null) {
            if (defaultVal == null) {
                Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
                key = entry.getKey();
                value = entry.getValue();
            } else {
                key = (K) it.next();
                value = defaultVal;
            }
        } else {
            key = (K) str.readObject();
            value = (defaultVal != null ? defaultVal : (V) str.readObject());
        }
        NodeEntry<K, V> middle = new NodeEntry<K, V>(key, value, null);
        if (level == redLevel) middle.color = RED;
        if (left != null) {
            middle.left = left;
            left.parent = middle;
        }
        if (mid < hi) {
            NodeEntry<K, V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
            middle.right = right;
            right.parent = middle;
        }
        return middle;
    }

    /**
     * Find the level down to which to assign all nodes BLACK. This is the last
     * `full' level of the complete binary tree produced by buildTree. The
     * remaining nodes are colored RED. (This makes a `nice' set of color
     * assignments wrt future insertions.) This level number is computed by
     * finding the number of splits needed to reach the zeroeth node. (The
     * answer is ~lg(N), but in any case must be computed by same quick O(lg(N))
     * loop.)
     */
    private static int computeRedLevel(int sz) {
        int level = 0;
        for (int m = sz - 1; m >= 0; m = m / 2 - 1) level++;
        return level;
    }
}
