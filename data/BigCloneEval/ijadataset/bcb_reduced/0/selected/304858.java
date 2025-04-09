package jaxlib.col;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import jaxlib.closure.Closure;
import jaxlib.util.BiDi;
import jaxlib.closure.Filter;
import jaxlib.lang.Objects;
import jaxlib.lang.UnexpectedError;
import jaxlib.jaxlib_private.CheckArg;
import jaxlib.util.AccessType;
import jaxlib.util.AccessTypeInfo;
import jaxlib.util.AccessTypeSet;

/**
 * Red-Black-Tree implementation of the <tt>XSortedMap</tt> interface.
 * <p>
 * The behaviour of this class is the same as of {@link java.util.TreeMap}.
 * </p>
 *
 * @author  <a href="mailto:joerg.wassmer@web.de">J�rg Wa�mer</a>
 * @since   JaXLib 1.0
 * @version $Id: RBMap.java 1044 2004-04-06 16:37:29Z joerg_wassmer $
 */
public class RBMap<K, V> extends AbstractOrderedMap<K, V> implements XSortedMap<K, V>, Cloneable, Serializable {

    /**
   * @since JaXLib 1.0
   */
    private static final long serialVersionUID = 1L;

    static final boolean RED = false;

    static final boolean BLACK = true;

    static final int ENTRIES = 0;

    static final int KEYS = 1;

    static final int VALUES = 2;

    /**
   * Recursive "helper method" that does the real work of the
   * of the previous method.  Identically named parameters have
   * identical definitions.  Additional parameters are documented below.
   * It is assumed that the comparator and size fields of the RBMap are
   * already set prior to calling this method.  (It ignores both fields.)
   *
   * @param level the current level of tree. Initial call should be 0.
   * @param lo the first element index of this subtree. Initial should be 0.
   * @param hi the last element index of this subtree.  Initial should be
   *              size-1.
   * @param redLevel the level at which nodes should be red. 
   *        Must be equal to computeRedLevel for tree of this size.
   */
    private static Entry buildFromSorted(int level, int lo, int hi, int redLevel, Iterator it, java.io.ObjectInputStream str, Object defaultVal) throws IOException, ClassNotFoundException {
        if (hi < lo) return null;
        int mid = (lo + hi) / 2;
        Entry left = null;
        if (lo < mid) left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);
        Object key;
        Object value;
        if (it != null) {
            if (defaultVal == null) {
                Map.Entry entry = (Map.Entry) it.next();
                key = entry.getKey();
                value = entry.getValue();
            } else {
                key = it.next();
                value = defaultVal;
            }
        } else {
            key = str.readObject();
            value = (defaultVal != null ? defaultVal : str.readObject());
        }
        Entry middle = new Entry(key, value, null);
        if (level == redLevel) middle.color = RED;
        if (left != null) {
            middle.left = left;
            left.parent = middle;
        }
        if (mid < hi) {
            Entry right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
            middle.right = right;
            right.parent = middle;
        }
        return middle;
    }

    static boolean colorOf(Entry p) {
        return (p == null ? BLACK : p.color);
    }

    /**
   * Find the level down to which to assign all nodes BLACK.  This is the
   * last `full' level of the complete binary tree produced by
   * buildTree. The remaining nodes are colored RED. (This makes a `nice'
   * set of color assignments wrt future insertions.) This level number is
   * computed by finding the number of splits needed to reach the zeroeth
   * node.  (The answer is ~lg(N), but in any case must be computed by same
   * quick O(lg(N)) loop.)
   */
    static int computeRedLevel(int sz) {
        int level = 0;
        for (int m = sz - 1; m >= 0; m = m / 2 - 1) level++;
        return level;
    }

    static <K, V> Entry<K, V> parentOf(Entry<K, V> p) {
        return (p == null ? null : p.parent);
    }

    static void setColor(Entry p, boolean c) {
        if (p != null) p.color = c;
    }

    static <K, V> Entry<K, V> leftOf(Entry<K, V> p) {
        return (p == null) ? null : p.left;
    }

    static <K, V> Entry<K, V> rightOf(Entry<K, V> p) {
        return (p == null) ? null : p.right;
    }

    /**
   * Returns the key corresonding to the specified Entry.  
   * Throws NoSuchElementException if the entry is <tt>null</tt>.
   */
    static <K, V> K key(Entry<K, V> entry) {
        if (entry == null) throw new NoSuchElementException();
        return entry.key;
    }

    /**
   * Test two values  for equality.  Differs from o1.equals(o2) only in
   * that it copes with with <tt>null</tt> o1 properly.
   */
    static boolean valEquals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }

    /**
   * The Comparator used to maintain order in this RBMap, or
   * null if this RBMap uses its elements natural ordering.
   *
   * @serial
   * @since JaXLib 1.0
   */
    final Comparator<K> comparator;

    transient Entry root = null;

    /**
   * The number of entries in the tree
   */
    transient int size = 0;

    /**
   * The number of structural modifications to the tree.
   */
    transient int modCount = 0;

    private transient EntryList entryList = null;

    private transient RBSet<K> keySet = null;

    private transient XList<V> values = null;

    /**
   * Constructs a new, empty map, sorted according to the keys' natural
   * order.  All keys inserted into the map must implement the
   * <tt>Comparable</tt> interface.  Furthermore, all such keys must be
   * <i>mutually comparable</i>: <tt>k1.compareTo(k2)</tt> must not throw a
   * ClassCastException for any elements <tt>k1</tt> and <tt>k2</tt> in the
   * map.  If the user attempts to put a key into the map that violates this
   * constraint (for example, the user attempts to put a string key into a
   * map whose keys are integers), the <tt>put(Object key, Object
   * value)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @see Comparable
   */
    public RBMap() {
        super();
        this.comparator = null;
    }

    /**
   * Constructs a new, empty map, sorted according to the given comparator.
   * All keys inserted into the map must be <i>mutually comparable</i> by
   * the given comparator: <tt>comparator.compare(k1, k2)</tt> must not
   * throw a <tt>ClassCastException</tt> for any keys <tt>k1</tt> and
   * <tt>k2</tt> in the map.  If the user attempts to put a key into the
   * map that violates this constraint, the <tt>put(Object key, Object
   * value)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @param c the comparator that will be used to sort this map.  A
   *        <tt>null</tt> value indicates that the keys' <i>natural
   *        ordering</i> should be used.
   */
    public RBMap(Comparator<K> c) {
        super();
        this.comparator = c;
    }

    /**
   * Constructs a new map containing the same mappings as the given map,
   * sorted according to the keys' <i>natural order</i>.  All keys inserted
   * into the new map must implement the <tt>Comparable</tt> interface.
   * Furthermore, all such keys must be <i>mutually comparable</i>:
   * <tt>k1.compareTo(k2)</tt> must not throw a <tt>ClassCastException</tt>
   * for any elements <tt>k1</tt> and <tt>k2</tt> in the map.  This method
   * runs in n*log(n) time.
   *
   * @param  source the map whose mappings are to be placed in this map.
   *
   * @throws ClassCastException the keys in t are not Comparable, or
   *         are not mutually comparable.
   * @throws NullPointerException if the specified map is null.
   */
    public RBMap(Map<K, V> source) {
        this();
        putAll(source);
    }

    /**
   * Constructs a new map containing the same mappings as the given
   * <tt>SortedMap</tt>, sorted according to the same ordering.  This method
   * runs in linear time.
   *
   * @param  source the sorted map whose mappings are to be placed in this map,
   *         and whose comparator is to be used to sort this map.
   *
   * @throws NullPointerException if the specified sorted map is null.
   */
    public RBMap(SortedMap<K, V> source) {
        this((Comparator<K>) source.comparator());
        try {
            buildFromSorted(source.size(), source.entrySet().iterator(), null, null);
        } catch (IOException ex) {
            throw new UnexpectedError(ex);
        } catch (ClassNotFoundException ex) {
            throw new UnexpectedError(ex);
        }
    }

    /**
   * Linear time tree building algorithm from sorted data.  Can accept keys
   * and/or values from iterator or stream. This leads to too many
   * parameters, but seems better than alternatives.  The four formats
   * that this method accepts are:
   *
   *    1) An iterator of Map.Entries.  (it != null, defaultVal == null).
   *    2) An iterator of keys.         (it != null, defaultVal != null).
   *    3) A stream of alternating serialized keys and values.
   *                                   (it == null, defaultVal == null).
   *    4) A stream of serialized keys. (it == null, defaultVal != null).
   *
   * It is assumed that the comparator of the RBMap is already set prior
   * to calling this method.
   *
   * @param size the number of keys (or key-value pairs) to be read from
   *        the iterator or stream.
   * @param it If non-null, new entries are created from entries
   *        or keys read from this iterator.
   * @param it If non-null, new entries are created from keys and
   *        possibly values read from this stream in serialized form.
   *        Exactly one of it and str should be non-null.
   * @param defaultVal if non-null, this default value is used for
   *        each value in the map.  If null, each value is read from
   *        iterator or stream, as described above.
   * @throws IOException propagated from stream reads. This cannot
   *         occur if str is null.
   * @throws ClassNotFoundException propagated from readObject. 
   *         This cannot occur if str is null.
   */
    final void buildFromSorted(int size, Iterator it, java.io.ObjectInputStream str, Object defaultVal) throws IOException, ClassNotFoundException {
        this.size = size;
        root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
    }

    /**
   * Compares two keys using the correct comparison method for this RBMap.
   */
    final int compare(K k1, K k2) {
        return Objects.compare(k1, k2, this.comparator);
    }

    final int forEachKey(Closure<? super K> procedure, int fromIndex, int toIndex, BiDi dir) {
        CheckArg.range(this.size, fromIndex, toIndex);
        int remaining = toIndex - fromIndex;
        if (remaining == 0) return 0;
        int count = 0;
        for (Entry<K, V> entry = firstEntry(fromIndex, toIndex, dir); remaining-- > 0; entry = entry.next(dir)) {
            if (procedure.proceed(entry.key)) count++; else return count;
        }
        return count;
    }

    final void incrementSize() {
        this.modCount++;
        this.size++;
    }

    final void decrementSize() {
        this.modCount++;
        this.size--;
    }

    /**
   * Delete node p, and then rebalance the tree.
   */
    void deleteEntry(Entry p) {
        decrementSize();
        if (p.left != null && p.right != null) {
            Entry s = p.successor();
            p.key = s.key;
            p.setValue(s.getValue());
            p = s;
        }
        final Entry replacement = (p.left != null ? p.left : p.right);
        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null) this.root = replacement; else if (p == p.parent.left) p.parent.left = replacement; else p.parent.right = replacement;
            p.left = p.right = p.parent = null;
            if (p.color == BLACK) fixAfterDeletion(replacement);
        } else if (p.parent == null) {
            this.root = null;
        } else {
            if (p.color == BLACK) fixAfterDeletion(p);
            if (p.parent != null) {
                if (p == p.parent.left) p.parent.left = null; else if (p == p.parent.right) p.parent.right = null;
                p.parent = null;
            }
        }
    }

    /**
   * Returns the first Entry in the RBMap (according to the RBMap's
   * key-sort function).  Returns null if the RBMap is empty.
   */
    final Entry<K, V> firstEntry() {
        return (this.root == null) ? null : this.root.leftmost();
    }

    final void fixAfterDeletion(Entry x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry sib = rightOf(parentOf(x));
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
                Entry sib = leftOf(parentOf(x));
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

    final void fixAfterInsertion(Entry x) {
        x.color = RED;
        while ((x != null) && (x != this.root) && (x.parent.color == RED)) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry y = rightOf(parentOf(parentOf(x)));
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
                Entry y = leftOf(parentOf(parentOf(x)));
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
        this.root.color = BLACK;
    }

    final ListIterator<K> keyIterator() {
        return (ListIterator) new IteratorImpl(KEYS);
    }

    final ListIterator<K> keyIterator(int index) {
        return (ListIterator) new IteratorImpl(KEYS, index);
    }

    final void keysToArray(int fromIndex, int toIndex, Object[] dest, int destIndex) {
        CheckArg.range(this.size, fromIndex, toIndex);
        int remaining = toIndex - fromIndex;
        CheckArg.copyCount(remaining, dest.length, destIndex);
        if (remaining == 0) return;
        for (Entry<K, V> entry = getEntry(fromIndex); remaining-- > 0; entry = entry.successor()) dest[destIndex++] = entry.key;
    }

    /**
   * Returns the last Entry in the RBMap (according to the RBMap's
   * key-sort function).  Returns null if the RBMap is empty.
   */
    final Entry<K, V> lastEntry() {
        return (this.root == null) ? null : this.root.rightmost();
    }

    final Entry<K, V> firstEntry(int fromIndex, int toIndex, BiDi dir) {
        return getEntry(dir.forward ? fromIndex : toIndex - 1);
    }

    /**
   * Gets the entry corresponding to the specified key; if no such entry
   * exists, returns the entry for the least key greater than the specified
   * key; if no such entry exists (i.e., the greatest key in the Tree is less
   * than the specified key), returns <tt>null</tt>.
   */
    final Entry<K, V> getCeilEntry(K key) {
        Entry<K, V> p = this.root;
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
                    Entry<K, V> parent = p.parent;
                    Entry<K, V> ch = p;
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
   * Returns the entry for the greatest key less than the specified key; if
   * no such entry exists (i.e., the least key in the Tree is greater than
   * the specified key), returns <tt>null</tt>.
   */
    final Entry<K, V> getPrecedingEntry(K key) {
        Entry<K, V> p = this.root;
        if (p == null) return null;
        while (true) {
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right != null) p = p.right; else return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    Entry<K, V> parent = p.parent;
                    Entry<K, V> ch = p;
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
   * Returns this map's entry for the given key, or <tt>null</tt> if the map
   * does not contain an entry for the key.
   *
   * @return this map's entry for the given key, or <tt>null</tt> if the map
   *                does not contain an entry for the key.
   * @throws ClassCastException if the key cannot be compared with the keys
   *                  currently in the map.
   * @throws NullPointerException key is <tt>null</tt> and this map uses
   *                  natural order, or its comparator does not tolerate *
   *                  <tt>null</tt> keys.
   */
    @Overrides
    protected final RBMap.Entry<K, V> getEntry(Object key) {
        Entry<K, V> p = this.root;
        while (p != null) {
            int cmp = compare((K) key, (K) p.key);
            if (cmp == 0) return p; else if (cmp < 0) p = p.left; else p = p.right;
        }
        return null;
    }

    final RBMap.Entry<K, V> getEntry(int index) {
        CheckArg.range(this.size, index);
        if (index <= this.size >> 1) {
            Entry<K, V> n = firstEntry();
            for (int r = index; --r >= 0; ) n = n.successor();
            return n;
        } else {
            Entry<K, V> n = lastEntry();
            for (int r = this.size - index; --r > 0; ) n = n.predecessor();
            return n;
        }
    }

    final int removeMatchingKeys(boolean iF, int maxCount, boolean stopOnDismatch, Filter<? super K> condition, int fromIndex, int toIndex, BiDi dir) {
        CheckArg.maxCount(maxCount);
        CheckArg.range(this.size, fromIndex, toIndex);
        if ((maxCount == 0) || (fromIndex == toIndex)) return 0;
        int count = 0;
        int remaining = toIndex - fromIndex;
        for (Entry<K, V> entry = getEntry(dir.forward ? fromIndex : --toIndex); remaining-- > 0; ) {
            if (condition.accept(entry.key) == iF) {
                Entry<K, V> next = entry.nextAfterDeletion(dir);
                deleteEntry(entry);
                if (++count == maxCount) return count;
                entry = next;
            } else if (stopOnDismatch) return -(count + 1); else entry = entry.next(dir);
        }
        return count;
    }

    final void rotateLeft(final Entry p) {
        final Entry r = p.right;
        p.right = r.left;
        if (r.left != null) r.left.parent = p;
        r.parent = p.parent;
        if (p.parent == null) this.root = r; else if (p.parent.left == p) p.parent.left = r; else p.parent.right = r;
        r.left = p;
        p.parent = r;
    }

    final void rotateRight(final Entry p) {
        final Entry l = p.left;
        p.left = l.right;
        if (l.right != null) l.right.parent = p;
        l.parent = p.parent;
        if (p.parent == null) this.root = l; else if (p.parent.right == p) p.parent.right = l; else p.parent.left = l;
        l.right = p;
        p.parent = l;
    }

    final boolean valueSearchNull(Entry n) {
        if (n.value == null) return true;
        return (n.left != null && valueSearchNull(n.left)) || (n.right != null && valueSearchNull(n.right));
    }

    final boolean valueSearchNonNull(Entry n, Object value) {
        if (value.equals(n.value)) return true;
        return (n.left != null && valueSearchNonNull(n.left, value)) || (n.right != null && valueSearchNonNull(n.right, value));
    }

    @Overrides
    public final AccessTypeSet accessTypes() {
        return AccessTypeSet.ALL;
    }

    @Overrides
    public void clear() {
        this.modCount++;
        this.size = 0;
        this.root = null;
    }

    final void clear(int fromIndex, int toIndex) {
        CheckArg.range(this.size, fromIndex, toIndex);
        if (fromIndex == toIndex) return; else if (fromIndex == 0 && toIndex == size) clear(); else if (toIndex - fromIndex == 1) deleteEntry(getEntry(fromIndex)); else {
            Entry e = getEntry(fromIndex);
            do {
                Entry n = e.nextAfterDeletion(BiDi.FORWARD);
                deleteEntry(e);
                e = n;
            } while (++fromIndex < toIndex);
        }
    }

    @Overrides
    public RBMap<K, V> clone() {
        return (RBMap) cloneImpl();
    }

    Object cloneImpl() {
        RBMap<K, V> clone;
        try {
            clone = (RBMap<K, V>) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnexpectedError(ex);
        }
        clone.root = null;
        clone.size = 0;
        clone.modCount = 0;
        try {
            clone.buildFromSorted(size, entryList().iterator(), null, null);
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
        return clone;
    }

    public final Comparator<K> comparator() {
        return this.comparator;
    }

    @Overrides
    public final boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    @Overrides
    public final boolean containsValue(Object value) {
        return (this.root == null) ? false : (value == null) ? valueSearchNull(this.root) : valueSearchNonNull(this.root, value);
    }

    @Overrides
    public XList<Map.Entry<K, V>> entryList() {
        if (this.entryList == null) this.entryList = new EntryList();
        return entryList;
    }

    public final K firstKey() {
        return key(firstEntry());
    }

    @Overrides
    public final V get(Object key) {
        Entry<K, V> entry = getEntry(key);
        return (entry == null) ? null : entry.getValue();
    }

    public XSortedMap<K, V> headMap(K toKey) {
        return headMapImpl(toKey);
    }

    final SubMap headMapImpl(K toKey) {
        return new SubMap(toKey, true);
    }

    @Overrides
    public final boolean isEmpty() {
        return this.size == 0;
    }

    @Overrides
    public SortedList<K> keyList() {
        return keySet().list();
    }

    @Overrides
    public RBSet<K> keySet() {
        if (this.keySet == null) this.keySet = new RBSet(this, true);
        return keySet;
    }

    public final K lastKey() {
        return key(lastEntry());
    }

    @Overrides
    public V put(K key, V value) {
        return put(key, value, false);
    }

    final V put(K key, V value, boolean allowDupes) {
        Entry<K, V> t = this.root;
        if (t == null) {
            incrementSize();
            this.root = new Entry(key, value, null);
            return null;
        }
        while (true) {
            int cmp = compare(key, t.key);
            if (cmp == 0 && !allowDupes) {
                return t.setValue(value);
            } else if (cmp < 0) {
                if (t.left != null) {
                    t = t.left;
                } else {
                    incrementSize();
                    t.left = new Entry(key, value, t);
                    fixAfterInsertion(t.left);
                    return null;
                }
            } else {
                if (t.right != null) {
                    t = t.right;
                } else {
                    incrementSize();
                    t.right = new Entry(key, value, t);
                    fixAfterInsertion(t.right);
                    return null;
                }
            }
        }
    }

    @Overrides
    public void putAll(Map<? extends K, ? extends V> source) {
        putAllImpl(source);
    }

    void putAllImpl(Map source) {
        if (source == this) return;
        int sourceSize = source.size();
        if ((this.size == 0) && (sourceSize != 0) && (source instanceof SortedMap)) {
            Comparator c = ((SortedMap) source).comparator();
            if ((c == this.comparator) || (c != null && c.equals(this.comparator))) {
                this.modCount++;
                try {
                    buildFromSorted(sourceSize, source.entrySet().iterator(), null, null);
                } catch (java.io.IOException cannotHappen) {
                } catch (ClassNotFoundException cannotHappen) {
                }
                return;
            }
        }
        super.putAll(source);
    }

    @Overrides
    public V remove(Object key) {
        Entry<K, V> p = getEntry(key);
        if (p == null) return null;
        V oldValue = p.getValue();
        deleteEntry(p);
        return oldValue;
    }

    @Overrides
    public final int size() {
        return this.size;
    }

    public XSortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMapImpl(fromKey, toKey);
    }

    final SubMap subMapImpl(K fromKey, K toKey) {
        return new SubMap(fromKey, toKey);
    }

    public XSortedMap<K, V> tailMap(K fromKey) {
        return tailMapImpl(fromKey);
    }

    final SubMap tailMapImpl(K fromKey) {
        return new SubMap(fromKey, false);
    }

    @Overrides
    public XList<V> valueList() {
        if (this.values == null) this.values = new Values();
        return this.values;
    }

    private void readObject(final java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        s.readByte();
        int size = s.readInt();
        buildFromSorted(size, null, s, null);
    }

    /**
   * Save the state of the <tt>RBMap</tt> instance to a stream (i.e.,
   * serialize it).
   *
   * @serialData The <i>size</i> of the RBMap (the number of key-value
   *             mappings) is emitted (int), followed by the key (Object)
   *             and value (Object) for each key-value mapping represented
   *             by the RBMap. The key-value mappings are emitted in
   *             key-order (as determined by the RBMap's Comparator,
   *             or by the keys' natural ordering if the RBMap has no
   *             Comparator).
   */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeByte(0);
        s.writeInt(size);
        for (Iterator i = entrySet().iterator(); i.hasNext(); ) {
            Entry e = (Entry) i.next();
            s.writeObject(e.key);
            s.writeObject(e.value);
        }
    }

    static class Entry<K, V> implements Map.Entry<K, V> {

        K key;

        V value;

        Entry<K, V> left = null;

        Entry<K, V> right = null;

        Entry<K, V> parent;

        boolean color = BLACK;

        /**
     * Make a new cell with given key, value, and parent, and with 
     * <tt>null</tt> child links, and BLACK color. 
     */
        Entry(K key, V value, Entry<K, V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry b = (Map.Entry) o;
            return Objects.equals(this.key, b.getKey()) && Objects.equals(this.getValue(), b.getValue());
        }

        final Object getComponent(int mode) {
            switch(mode) {
                case ENTRIES:
                    return this;
                case KEYS:
                    return this.key;
                case VALUES:
                    return this.getValue();
                default:
                    throw new AssertionError(mode);
            }
        }

        final Entry<K, V> leftmost() {
            Entry<K, V> n = this;
            while (n.left != null) n = n.left;
            return n;
        }

        final Entry<K, V> nextAfterDeletion(BiDi dir) {
            return (this.left != null && this.right != null) ? this : dir.forward ? successor() : predecessor();
        }

        final Entry<K, V> next(BiDi dir) {
            return dir.forward ? successor() : predecessor();
        }

        final Entry<K, V> predecessor() {
            if (this.left != null) return this.left.rightmost(); else {
                Entry<K, V> p = this.parent;
                Entry<K, V> ch = this;
                while (p != null && ch == p.left) {
                    ch = p;
                    p = p.parent;
                }
                return p;
            }
        }

        final Entry<K, V> rightmost() {
            Entry<K, V> n = this;
            while (n.right != null) n = n.right;
            return n;
        }

        final Entry<K, V> successor() {
            if (this.right != null) {
                return this.right.leftmost();
            } else {
                Entry<K, V> p = this.parent;
                Entry<K, V> ch = this;
                while (p != null && ch == p.right) {
                    ch = p;
                    p = p.parent;
                }
                return p;
            }
        }

        public final K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public int hashCode() {
            return Maps.entryHashCode(this.key, this.getValue());
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public final String toString() {
            String a = String.valueOf(this.key);
            String b = String.valueOf(this.getValue());
            StringBuffer sb = new StringBuffer(a.length() + b.length() + 1);
            return sb.append(a).append('=').append(b).toString();
        }
    }

    private class IteratorImpl extends Object implements ListIterator, AccessTypeInfo {

        int modCount = RBMap.this.modCount;

        Entry<K, V> lastReturned = null;

        Entry<K, V> next;

        int nextIndex = 0;

        final int mode;

        IteratorImpl(int mode) {
            super();
            this.mode = mode;
            this.next = firstEntry();
        }

        IteratorImpl(int mode, int index) {
            super();
            CheckArg.rangeForIterator(RBMap.this.size, index);
            this.mode = mode;
            this.nextIndex = index;
            if (index != RBMap.this.size) this.next = RBMap.this.getEntry(index);
        }

        IteratorImpl(int mode, Entry<K, V> first) {
            super();
            this.mode = mode;
            this.next = first;
        }

        public AccessTypeSet accessTypes() {
            if (this.mode == VALUES) return AccessTypeSet.get(AccessType.READ, AccessType.REMOVE, AccessType.SET); else return AccessTypeSet.get(AccessType.READ, AccessType.REMOVE);
        }

        public final void add(Object e) {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }

        final Entry<K, V> nextEntry() {
            if (this.next == null) throw new NoSuchElementException();
            if (this.modCount != RBMap.this.modCount) throw new ConcurrentModificationException();
            this.lastReturned = this.next;
            this.next = this.next.successor();
            this.nextIndex++;
            return lastReturned;
        }

        public Object next() {
            if (this.next == null) throw new NoSuchElementException();
            if (this.modCount != RBMap.this.modCount) throw new ConcurrentModificationException();
            this.lastReturned = this.next;
            this.next = this.next.successor();
            this.nextIndex++;
            switch(this.mode) {
                case ENTRIES:
                    return this.lastReturned;
                case KEYS:
                    return this.lastReturned.key;
                case VALUES:
                    return this.lastReturned.getValue();
                default:
                    throw new AssertionError(this.mode);
            }
        }

        public final int nextIndex() {
            return this.nextIndex;
        }

        final Entry<K, V> previousEntry() {
            if (this.nextIndex <= 0) throw new NoSuchElementException();
            if (this.modCount != RBMap.this.modCount) throw new ConcurrentModificationException();
            Entry<K, V> entry = this.next;
            if (entry == null) this.next = entry = RBMap.this.lastEntry(); else this.next = entry = entry.predecessor();
            this.lastReturned = entry;
            this.nextIndex--;
            return entry;
        }

        public Object previous() {
            return previousEntry().getComponent(this.mode);
        }

        public final int previousIndex() {
            return this.nextIndex - 1;
        }

        public final void remove() {
            if (this.lastReturned == null) throw new IllegalStateException();
            if (this.modCount != RBMap.this.modCount) throw new ConcurrentModificationException();
            this.next = this.lastReturned.nextAfterDeletion(BiDi.FORWARD);
            deleteEntry(lastReturned);
            this.modCount++;
            this.lastReturned = null;
        }

        public void set(Object e) {
            if (this.mode != VALUES) throw new UnsupportedOperationException();
            if (this.lastReturned == null) throw new IllegalStateException();
            if (this.modCount != RBMap.this.modCount) throw new ConcurrentModificationException();
            this.lastReturned.value = (V) e;
        }
    }

    private final class EntryList extends AbstractOrderedMap.AbstractEntryList<K, V, Map.Entry<K, V>> {

        EntryList() {
            super();
        }

        @Overrides
        public OrderedSet<Map.Entry<K, V>> set() {
            return RBMap.this.entrySet();
        }

        @Overrides
        public void clear() {
            RBMap.this.clear();
        }

        @Overrides
        public void clear(int index) {
            RBMap.this.clear(index, index + 1);
        }

        @Overrides
        public void clear(int fromIndex, int toIndex) {
            RBMap.this.clear(fromIndex, toIndex);
        }

        @Overrides
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry entry = (Map.Entry) o;
            Entry<K, V> p = RBMap.this.getEntry(entry.getKey());
            return (p != null) && Objects.equals(entry.getValue(), p.getValue());
        }

        @Overrides
        public boolean containsIdentical(Object identity) {
            if (!(identity instanceof Entry)) return false;
            Entry entry = (Entry) identity;
            return RBMap.this.getEntry(entry.key) == entry;
        }

        @Overrides
        public Map.Entry<K, V> get(int index) {
            return RBMap.this.getEntry(index);
        }

        @Overrides
        public int indexOf(int fromIndex, int toIndex, Object e) {
            int size = this.size();
            CheckArg.range(size, fromIndex, toIndex);
            if (fromIndex == toIndex || !(e instanceof Map.Entry)) return -1;
            Map.Entry me = (Map.Entry) e;
            Entry entry = RBMap.this.getEntry(me.getKey());
            if ((entry == null) || !Objects.equals(me.getValue(), entry.getValue())) return -1; else {
                for (Entry n = RBMap.this.getEntry(fromIndex); fromIndex < toIndex; fromIndex++, n = n.successor()) {
                    if (n == entry) return fromIndex;
                }
                return -1;
            }
        }

        @Overrides
        public int indexOfIdentical(int fromIndex, int toIndex, Object identity) {
            int size = RBMap.this.size;
            CheckArg.range(size, fromIndex, toIndex);
            if (fromIndex == toIndex || !(identity instanceof Entry)) return -1;
            Entry me = (Entry) identity;
            Entry entry = RBMap.this.getEntry(me.getKey());
            if (entry != me) return -1; else {
                for (Entry n = RBMap.this.getEntry(fromIndex); fromIndex < toIndex; fromIndex++, n = n.successor()) {
                    if (n == entry) return fromIndex;
                }
                return -1;
            }
        }

        @Overrides
        public int lastIndexOf(int fromIndex, int toIndex, Object e) {
            int size = this.size();
            CheckArg.range(size, fromIndex, toIndex);
            if (fromIndex == toIndex || !(e instanceof Map.Entry)) return -1;
            Map.Entry me = (Map.Entry) e;
            Entry entry = RBMap.this.getEntry(me.getKey());
            if ((entry == null) || !Objects.equals(me.getValue(), entry.getValue())) return -1; else {
                for (Entry n = RBMap.this.getEntry(--toIndex); fromIndex >= toIndex; toIndex--, n = n.successor()) {
                    if (n == entry) return toIndex;
                }
                return -1;
            }
        }

        @Overrides
        public int lastIndexOfIdentical(int fromIndex, int toIndex, Object identity) {
            int size = RBMap.this.size;
            CheckArg.range(size, fromIndex, toIndex);
            if (fromIndex == toIndex || !(identity instanceof Entry)) return -1;
            Entry me = (Entry) identity;
            Entry entry = RBMap.this.getEntry(me.getKey());
            if (entry != me) return -1; else {
                for (Entry n = RBMap.this.getEntry(--toIndex); fromIndex >= toIndex; toIndex--, n = n.successor()) {
                    if (n == entry) return toIndex;
                }
                return -1;
            }
        }

        @Overrides
        public ListIterator<Map.Entry<K, V>> listIterator(int index) {
            CheckArg.rangeForIterator(RBMap.this.size, index);
            return (ListIterator) new IteratorImpl(ENTRIES, index);
        }

        @Overrides
        public Map.Entry<K, V> remove(int index) {
            Entry<K, V> entry = RBMap.this.getEntry(index);
            RBMap.this.deleteEntry(entry);
            return entry;
        }

        @Overrides
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry entry = (Map.Entry) o;
            Entry<K, V> p = RBMap.this.getEntry(entry.getKey());
            if ((p != null) && Objects.equals(entry.getValue(), p.getValue())) {
                RBMap.this.deleteEntry(p);
                return true;
            } else return false;
        }

        @Overrides
        public int size() {
            return RBMap.this.size;
        }
    }

    private final class Values extends AbstractOrderedMap.DefaultValueList<V> {

        Values() {
            super();
        }

        final Entry valueSearch(Entry n, Object e) {
            if (e.equals(n.getValue())) return n;
            if (n.left != null) {
                Entry entry = valueSearch(n.left, e);
                if (entry != null) return entry;
            }
            if (n.right != null) {
                Entry entry = valueSearch(n.right, e);
                if (entry != null) return entry;
            }
            return null;
        }

        final Entry valueSearchIdentical(Entry n, Object identity) {
            if (identity == n.getValue()) return n;
            if (n.left != null) {
                Entry entry = valueSearchIdentical(n.left, identity);
                if (entry != null) return entry;
            }
            if (n.right != null) {
                Entry entry = valueSearchIdentical(n.right, identity);
                if (entry != null) return entry;
            }
            return null;
        }

        @Overrides
        public void clear() {
            RBMap.this.clear();
        }

        @Overrides
        public void clear(int index) {
            RBMap.this.clear(index, index + 1);
        }

        @Overrides
        public void clear(int fromIndex, int toIndex) {
            RBMap.this.clear(fromIndex, toIndex);
        }

        @Overrides
        public boolean contains(int fromIndex, int toIndex, Object e) {
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            if (fromIndex == toIndex) return false; else if (fromIndex == 0 && toIndex == RBMap.this.size) return RBMap.this.containsValue(e); else return indexOf(fromIndex, toIndex, e) >= 0;
        }

        @Overrides
        public boolean containsIdentical(int fromIndex, int toIndex, Object identity) {
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            if (fromIndex == toIndex) return false; else if (fromIndex == 0 && toIndex == RBMap.this.size) return valueSearchIdentical(RBMap.this.root, identity) != null; else return indexOfIdentical(fromIndex, toIndex, identity) >= 0;
        }

        @Overrides
        public final int forEach(int fromIndex, int toIndex, BiDi dir, Closure<? super V> procedure) {
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            int remaining = toIndex - fromIndex;
            if (remaining == 0) return 0;
            int count = 0;
            for (Entry<K, V> entry = RBMap.this.firstEntry(fromIndex, toIndex, dir); remaining-- > 0; entry = entry.next(dir)) {
                if (procedure.proceed(entry.getValue())) count++; else return count;
            }
            return count;
        }

        @Overrides
        public V get(int index) {
            return RBMap.this.getEntry(index).getValue();
        }

        @Overrides
        public V getEqual(Object e) {
            if (e == null || RBMap.this.root == null) return null;
            Entry<K, V> entry = valueSearch(RBMap.this.root, e);
            return entry == null ? null : entry.getValue();
        }

        @Overrides
        public V getEqual(int fromIndex, int toIndex, Object e) {
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            if (fromIndex == toIndex || e == null) return null;
            if (fromIndex == 0 && toIndex == RBMap.this.size) return getEqual(e);
            return super.getEqual(fromIndex, toIndex, e);
        }

        @Overrides
        public int indexOf(int fromIndex, int toIndex, Object e) {
            return indexOf(e, fromIndex, toIndex, false);
        }

        @Overrides
        public int indexOfIdentical(int fromIndex, int toIndex, Object identity) {
            return indexOf(identity, fromIndex, toIndex, false);
        }

        private int indexOf(Object e, int fromIndex, int toIndex, boolean identical) {
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            if (fromIndex == toIndex) return -1;
            identical = identical || e == null;
            Entry<K, V> entry = getEntry(fromIndex);
            while (true) {
                if ((e == entry.getValue()) || (!identical && e.equals(entry.getValue()))) return fromIndex; else if (++fromIndex == toIndex) return -1; else entry = entry.successor();
            }
        }

        @Overrides
        public int lastIndexOf(int fromIndex, int toIndex, Object e) {
            return lastIndexOf(e, fromIndex, toIndex, false);
        }

        @Overrides
        public int lastIndexOfIdentical(int fromIndex, int toIndex, Object identity) {
            return lastIndexOf(identity, fromIndex, toIndex, false);
        }

        private int lastIndexOf(Object e, int fromIndex, int toIndex, boolean identical) {
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            if (fromIndex == toIndex) return -1;
            identical = identical || e == null;
            Entry<K, V> entry = getEntry(--toIndex);
            while (true) {
                if ((e == entry.getValue()) || (!identical && e.equals(entry.getValue()))) return toIndex; else if (--toIndex < fromIndex) return -1; else entry = entry.predecessor();
            }
        }

        @Overrides
        public ListIterator<V> listIterator(int index) {
            return (ListIterator) new IteratorImpl(VALUES, index);
        }

        @Overrides
        public V remove(int index) {
            Entry<K, V> entry = RBMap.this.getEntry(index);
            V value = entry.getValue();
            RBMap.this.deleteEntry(entry);
            return value;
        }

        @Overrides
        public int removeMatches(int fromIndex, int toIndex, BiDi dir, Filter<? super V> condition, boolean iF, int maxCount, boolean stopOnDismatch) {
            CheckArg.maxCount(maxCount);
            CheckArg.range(RBMap.this.size, fromIndex, toIndex);
            if ((maxCount == 0) || (fromIndex == toIndex)) return 0;
            int count = 0;
            int remaining = toIndex - fromIndex;
            boolean fw = dir.forward;
            for (Entry<K, V> entry = RBMap.this.getEntry(fw ? fromIndex : --toIndex); remaining-- > 0; ) {
                if (condition.accept(entry.getValue()) == iF) {
                    Entry<K, V> next = entry.nextAfterDeletion(dir);
                    RBMap.this.deleteEntry(entry);
                    if (++count == maxCount) return count;
                    entry = next;
                } else if (stopOnDismatch) return -(count + 1); else entry = fw ? entry.successor() : entry.predecessor();
            }
            return count;
        }

        @Overrides
        public V set(int index, V value) {
            return RBMap.this.getEntry(index).setValue(value);
        }

        @Overrides
        public int size() {
            return RBMap.this.size;
        }
    }

    final class SubMap extends AbstractXMap<K, V> implements XSortedMap<K, V>, Serializable {

        private static final long serialVersionUID = 1L;

        /**
     * fromKey is significant only if fromStart is false.  Similarly,
     * toKey is significant only if toStart is false.
     */
        boolean fromStart = false;

        boolean toEnd = false;

        K fromKey;

        K toKey;

        private transient RBSet.SubSet<K> keySet;

        private final SubEntrySet entrySet = new SubEntrySet();

        SubMap(K fromKey, K toKey) {
            super();
            if (compare(fromKey, toKey) > 0) throw new IllegalArgumentException("fromKey > toKey");
            this.fromKey = fromKey;
            this.toKey = toKey;
        }

        SubMap(K key, boolean headMap) {
            super();
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
            super();
            this.fromStart = fromStart;
            this.fromKey = fromKey;
            this.toEnd = toEnd;
            this.toKey = toKey;
        }

        RBMap<K, V> getOuterMap() {
            return RBMap.this;
        }

        Iterator<K> keyIterator() {
            return (Iterator) new SubMapIterator(KEYS, (fromStart ? firstEntry() : getCeilEntry(fromKey)), (toEnd ? null : getCeilEntry(toKey)));
        }

        boolean inRange(K key) {
            return (fromStart || compare(key, fromKey) >= 0) && (toEnd || compare(key, toKey) < 0);
        }

        boolean inRange2(K key) {
            return (fromStart || compare(key, fromKey) >= 0) && (toEnd || compare(key, toKey) <= 0);
        }

        public Comparator<K> comparator() {
            return RBMap.this.comparator;
        }

        @Overrides
        public boolean containsKey(Object key) {
            return inRange((K) key) && RBMap.this.containsKey(key);
        }

        public XList<Map.Entry<K, V>> entryList() {
            XList<K> keys = RBMap.this.keyList();
            int a = Lists.binarySearchFirst(keys, this.fromKey, this.comparator());
            int b = Lists.binarySearchLast(keys, this.toKey, this.comparator());
            a = (a < 0) ? ~a : a;
            b = (b < 0) ? ~b : (b + 1);
            return RBMap.this.entryList().subList(a, b);
        }

        @Overrides
        public XSet<Map.Entry<K, V>> entrySet() {
            return this.entrySet;
        }

        public K firstKey() {
            K first = key(this.fromStart ? firstEntry() : getCeilEntry(this.fromKey));
            if (!this.toEnd && compare(first, this.toKey) >= 0) throw new NoSuchElementException();
            return first;
        }

        @Overrides
        public V get(Object key) {
            if (!inRange((K) key)) return null;
            return RBMap.this.get(key);
        }

        public SubMap headMap(K toKey) {
            if (!inRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
            return new SubMap(fromStart, fromKey, false, toKey);
        }

        @Overrides
        public boolean isEmpty() {
            return entrySet.isEmpty();
        }

        public XList<K> keyList() {
            XList<K> keys = RBMap.this.keyList();
            int a = Lists.binarySearchFirst(keys, this.fromKey, this.comparator());
            int b = Lists.binarySearchLast(keys, this.toKey, this.comparator());
            a = (a < 0) ? ~a : a;
            b = (b < 0) ? ~b : (b + 1);
            return keys.subList(a, b);
        }

        @Overrides
        public RBSet.SubSet<K> keySet() {
            if (this.keySet == null) this.keySet = new RBSet.SubSet(this, true);
            return this.keySet;
        }

        public K lastKey() {
            K last = key(this.toEnd ? lastEntry() : getPrecedingEntry(this.toKey));
            if (!this.fromStart && compare(last, this.fromKey) < 0) throw new NoSuchElementException();
            return last;
        }

        @Overrides
        public V put(K key, V value) {
            if (!inRange(key)) throw new IllegalArgumentException("key out of range");
            return RBMap.this.put(key, value);
        }

        @Overrides
        public int size() {
            return this.entrySet().size();
        }

        public SubMap subMap(K fromKey, K toKey) {
            if (!inRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
            if (!inRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
            return new SubMap(fromKey, toKey);
        }

        public SubMap tailMap(K fromKey) {
            if (!inRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
            return new SubMap(false, fromKey, toEnd, toKey);
        }

        public XList<V> valueList() {
            XList<K> keys = RBMap.this.keyList();
            int a = Lists.binarySearchFirst(keys, this.fromKey, this.comparator());
            int b = Lists.binarySearchLast(keys, this.toKey, this.comparator());
            a = (a < 0) ? ~a : a;
            b = (b < 0) ? ~b : (b + 1);
            return RBMap.this.valueList().subList(a, b);
        }

        private final class SubEntrySet extends AbstractXMap.AbstractEntrySet<K, V, Map.Entry<K, V>> {

            transient int size = -1;

            transient int sizeModCount;

            SubEntrySet() {
                super();
            }

            @Overrides
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry entry = (Map.Entry) o;
                Object key = entry.getKey();
                if (!inRange((K) key)) return false;
                RBMap.Entry<K, V> node = RBMap.this.getEntry(key);
                return node != null && Objects.equals(entry.getValue(), node.value);
            }

            @Overrides
            public boolean isEmpty() {
                return !this.iterator().hasNext();
            }

            @Overrides
            public Iterator<Map.Entry<K, V>> iterator() {
                return (Iterator) new SubMapIterator(ENTRIES, (fromStart ? firstEntry() : getCeilEntry(fromKey)), (toEnd ? null : getCeilEntry(toKey)));
            }

            @Overrides
            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry entry = (Map.Entry) o;
                Object key = entry.getKey();
                if (!inRange((K) key)) return false;
                RBMap.Entry node = RBMap.this.getEntry(key);
                if (node != null && Objects.equals(entry.getValue(), node.value)) {
                    RBMap.this.deleteEntry(node);
                    return true;
                }
                return false;
            }

            @Overrides
            public int size() {
                if (this.size == -1 || this.sizeModCount != RBMap.this.modCount) {
                    this.size = 0;
                    this.sizeModCount = RBMap.this.modCount;
                    Iterator<Map.Entry<K, V>> it = this.iterator();
                    while (it.hasNext()) {
                        this.size++;
                        it.next();
                    }
                }
                return this.size;
            }
        }

        private final class SubMapIterator extends IteratorImpl {

            private final K firstExcludedKey;

            SubMapIterator(int mode, RBMap.Entry<K, V> first, RBMap.Entry<K, V> firstExcluded) {
                super(mode, first);
                this.firstExcludedKey = (firstExcluded == null ? null : firstExcluded.key);
            }

            public boolean hasNext() {
                return this.next != null && this.next.key != this.firstExcludedKey;
            }

            public Object next() {
                if (this.next == null || this.next.key == this.firstExcludedKey) throw new NoSuchElementException();
                return nextEntry().getComponent(this.mode);
            }
        }
    }
}
