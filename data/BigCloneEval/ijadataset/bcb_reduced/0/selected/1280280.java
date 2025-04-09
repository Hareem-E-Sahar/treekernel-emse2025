package jaxlib.col.ref;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jaxlib.array.ObjectArrays;
import jaxlib.closure.Function;
import jaxlib.col.AbstractXCollection;
import jaxlib.col.AbstractXMap;
import jaxlib.col.AbstractXSet;
import jaxlib.col.DefaultMapEntry;
import jaxlib.col.XCollection;
import jaxlib.col.XIterator;
import jaxlib.col.XSet;
import jaxlib.col.concurrent.ConcurrentXMap;
import jaxlib.jaxlib_private.CheckArg;
import jaxlib.lang.Objects;
import jaxlib.thread.lock.MultiLock;
import jaxlib.util.Strings;

/**
 * A hash table with weakly referenced keys supporting full concurrency of retrievals and 
 * adjustable expected concurrency for updates.
 * <p>
 * This class combines the functionality and behaviour of {@link java.util.concurrent.ConcurrentHashMap} 
 * and {@link java.util.WeakHashMap}. Additionaly a single high priority daemon thread clears the values of
 * the entries of all {@code ConcurrentWeakKeyHashMap} instances once the key of an entry has been cleared by
 * the garbage collector. However, there is no guarantee about the duration how long a value is kept after 
 * the associated key has been cleared by the vm. The duration depends on how long it takes between clearance
 * and enqueueing of the {@link java.lang.ref.WeakReference} instance, as well as on the duration between
 * enqueueing of the reference and waking up the thread which polls the {@link java.lang.ref.ReferenceQueue}.
 * </p><p>
 * Retrieval operations (e.g. {@link #get(Object)} generally do not block, so may overlap with update 
 * operations (e.g. {@link #put(Object,Object)} and {@link #remove(Object)}). Retrievals reflect the results 
 * of the most recently completed update operations holding upon their onset. For aggregate operations such 
 * as {@link #putAll(Map)} and {@link #clear()}, concurrent retrievals may reflect insertion or removal of 
 * only some entries. Similarly, iterators return elements reflecting the state of the hash table at some 
 * point at or since the creation of the iterator/enumeration. They do not throw 
 * {@link java.util.ConcurrentModificationException}. However, iterators are designed to be used by only one 
 * thread at a time, as usual. 
 * </p><p>
 * The maximum concurrency among update operations is guided by the optional <i>concurrencyLevel</i> 
 * constructor argument (default 16), which is used as a hint for internal sizing. The table is internally 
 * partitioned to try to permit the indicated number of concurrent updates without contention. Because 
 * placement in hash tables is essentially random, the actual concurrency will vary. Ideally, you should 
 * choose a value to accommodate as many threads as will ever concurrently modify the table. Using a 
 * significantly higher value than you need can waste some memory as well as some time for iterations, and a 
 * significantly lower value can lead to thread contention. But overestimates and underestimates within an 
 * order of magnitude do not usually have much noticeable impact. A value of one is appropriate when it is 
 * known that only one thread will modify and all others will only read. Also, resizing this or any other 
 * kind of hash table is an expensive operation, so, when possible, it is a good idea to provide estimates of
 * expected table sizes in constructors.  
 * </p><p>
 * In difference to {@code java.util.concurrent.ConcurrentHashMap} this class throws no 
 * {@link NullPointerException} if a query or remove operation receives a {@code null} key or value. However, 
 * all {@code put} operations are throwing an exception if a key or value argument is {@code null}.
 * </p><p>
 * Because entries may be cleared concurrently by the garbage collector iterators over an {@link #entrySet()}
 * may deliver {@code null} values (but not {@code null} keys).
 * </p><p>
 * Please refer to the documentation of {@link java.util.WeakHashMap} and {@link java.util.ConcurrentHashMap}
 * for more in detail documentation.
 * </p>
 *
 * @author  <a href="mailto:joerg.wassmer@web.de">J�rg Wa�mer</a>
 * @since   JaXLib 1.0
 * @version $Id: ConcurrentWeakKeyHashMap.java 1511 2005-12-11 14:13:40Z joerg_wassmer $
 */
public class ConcurrentWeakKeyHashMap<K, V> extends AbstractXMap<K, V> implements ConcurrentXMap<K, V> {

    /**
   * The default initial number of table slots for this table.
   * Used when not otherwise specified in constructor.
   */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
   * The maximum capacity, used if a higher value is implicitly
   * specified by either of the constructors with arguments.  MUST
   * be a power of two <= 1<<30 to ensure that entries are indexible
   * using ints.
   */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
   * The default load factor for this table.  
   * Used when not otherwise specified in constructor.
   */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
   * The default number of concurrency control segments.
   **/
    static final int DEFAULT_SEGMENTS = 16;

    /**
   * The maximum number of segments to allow; used to bound
   * constructor arguments.
   */
    static final int MAX_SEGMENTS = 1 << 16;

    /**
   * Returns a hash code for non-null Object x.
   * Uses the same hash code spreader as most other java.util hash tables.
   * @param x the object serving as a key
   * @return the hash code
   */
    static int hash(Object o) {
        int h = o.hashCode();
        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        return h ^ (h >>> 10);
    }

    /**
   * Mask value for indexing into segments. The upper bits of a
   * key's hash code are used to choose the segment.
   **/
    final int segmentMask;

    /**
   * Shift value for indexing within segments.
   **/
    final int segmentShift;

    /**
   * The segments, each of which is a specialized hash table
   */
    final Segment<K, V>[] segments;

    private XSet<Map.Entry<K, V>> entrySet;

    /**
   * Used by ReferenceQueueThread.
   * The thread starts if neccessary when a map s beeing constructed.
   * The thread stops if all references to all maps have been cleared and enqueued by the gc.
   */
    private final ReferenceQueueThread.Registration phantom;

    private final Lock completeWriteLock;

    /**
   * Creates a new, empty map with initial capacity {@code 16}, load factor {@code 0.75} and 
   * concurrencyLevel {@code 16}.
   *
   * @since JaXLib 1.0
   */
    public ConcurrentWeakKeyHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
   * Creates a new, empty map with the specified initial capacity, load factor, and concurrency level.
   *
   * @param initialCapacity 
   *  the initial capacity. The implementation performs internal sizing to accommodate this many elements.
   *
   * @param loadFactor  
   *  the load factor threshold, used to control resizing. Resizing may be performed when the average number 
   *  of elements per bin exceeds this threshold.
   *
   * @param concurrencyLevel 
   *  the estimated number of concurrently updating threads. The implementation performs internal sizing
   *  to try to accommodate this many threads.
   *
   * @throws IllegalArgumentException 
   *  if {@code (initialCapacity < 0) || !(loadFactor >= 0) || (concurrencyLevel < 0)}.
   *
   * @since JaXLib 1.0
   */
    public ConcurrentWeakKeyHashMap(int initialCapacity, final float loadFactor, int concurrencyLevel) {
        super();
        CheckArg.initialCapacity(initialCapacity);
        CheckArg.positive(loadFactor, "loadFactor");
        CheckArg.positive(concurrencyLevel, "concurrencyLevel");
        if (concurrencyLevel > MAX_SEGMENTS) concurrencyLevel = MAX_SEGMENTS;
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            sshift++;
            ssize <<= 1;
        }
        if (initialCapacity > MAXIMUM_CAPACITY) initialCapacity = MAXIMUM_CAPACITY;
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity) c++;
        int cap = 1;
        while (cap < c) cap <<= 1;
        this.segmentShift = 32 - sshift;
        this.segmentMask = ssize - 1;
        this.segments = new Segment[ssize];
        this.phantom = ReferenceQueueThread.register(this);
        ReferenceQueue<Object> queue = this.phantom.queue;
        assert (queue != null);
        for (int i = 0; i < ssize; i++) this.segments[i] = new Segment<K, V>(cap, loadFactor, queue);
        this.completeWriteLock = (ssize == 1) ? this.segments[0] : new MultiLock(this.segments, false);
    }

    /**
   * Creates a new map containing the same mappings as the given map. 
   * The map is created with a capacity of twice the number of mappings in the given map or 11 (whichever is 
   * greater), load factor {@code 0.75} and concurrencyLevel {@code 16}.
   *
   * @param src 
   *  the source map.
   *
   * @throws NullPointerException
   *  if {@code src == null}.
   */
    public ConcurrentWeakKeyHashMap(Map<? extends K, ? extends V> src) {
        this(Math.max((int) (src.size() / DEFAULT_LOAD_FACTOR) + 1, 11), DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
        putAll(src);
    }

    @Override
    protected void finalize() throws Throwable {
        for (int i = this.segments.length; --i >= 0; ) {
            Segment<K, V> segment = this.segments[i];
            segment.dispose();
        }
        this.phantom.enqueue();
        super.finalize();
    }

    final XIterator<K> keys() {
        return new KeyIterator();
    }

    /**
   * Returns the segment that should be used for key with given hash
   * @param hash the hash code for the key
   *
   * @return the segment
   */
    final Segment<K, V> segmentFor(int hash) {
        return this.segments[(hash >>> this.segmentShift) & this.segmentMask];
    }

    @Override
    public final int capacity() {
        long capacity = 0;
        for (int i = this.segments.length; --i >= 0; ) capacity += this.segments[i].table.length;
        return (capacity <= Integer.MAX_VALUE) ? (int) capacity : Integer.MAX_VALUE;
    }

    @Override
    public void clear() {
        for (int i = this.segments.length; --i >= 0; ) this.segments[i].clear();
    }

    @Override
    public final boolean containsKey(final Object key) {
        if (key != null) {
            final int hash = hash(key);
            return segmentFor(hash).containsKey(key, hash);
        } else {
            return false;
        }
    }

    @Override
    public final boolean containsValue(final Object value) {
        if (value != null) {
            for (int i = this.segments.length; --i >= 0; ) {
                if (this.segments[i].containsValue(value)) return true;
            }
        }
        return false;
    }

    /**
   * Returns the value associated with the specified key or creates and inserts a new one.
   * <p>
   * The {@code ConcurrentHashXMap} instance guarantees that multiple threads will not concurrently call the 
   * factory with an equal key as argument. 
   * </p><p>
   * This call locks one segment of this {@code ConcurrentHashXMap} if the specified key is absent. Thus
   * the specified factory should operate as fast as possible.
   * </p>
   *
   * @return
   *  the value associated with the specified key after this call.
   *
   * @param key
   *  the key of the mapping.
   * @param factory
   *  the factory to call with the specified key as argument if the key is absent.
   *
   * @throws IllegalArgumentException
   *  if the specified factory returns {@code null}.
   * @throws NullPointerException
   *  if {@code (key == null) || (factory == null)}.
   *
   * @since JaXLib 1.0
   */
    public V createIfAbsent(K key, Function<? super K, ? extends V> factory) {
        if (factory == null) throw new NullPointerException("factory");
        final int hash = hash(key);
        try {
            return segmentFor(hash).createIfAbsent(key, hash, factory, null);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
   * Returns the value associated with the specified key or creates and inserts a new one.
   * <p>
   * This call locks one segment of this {@code ConcurrentHashXMap} if the specified key is absent. Thus
   * the specified factory should operate as fast as possible.
   * </p>
   *
   * @return
   *  the value associated with the specified key after this call.
   *
   * @param key
   *  the key of the mapping.
   * @param factory
   *  the factory to call if the specified key is absent.
   *
   * @throws IllegalArgumentException
   *  if the specified factory returns {@code null}.
   * @throws NullPointerException
   *  if {@code (key == null) || (factory == null)}.
   *
   * @since JaXLib 1.0
   */
    public V createIfAbsent(K key, Callable<? extends V> factory) throws Exception {
        if (factory == null) throw new NullPointerException("factory");
        final int hash = hash(key);
        return segmentFor(hash).createIfAbsent(key, hash, null, factory);
    }

    public XSet<Map.Entry<K, V>> entrySet() {
        XSet<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = (XSet<Map.Entry<K, V>>) (XSet) new EntrySet());
    }

    @Override
    public final V get(final Object key) {
        if (key != null) {
            final int hash = hash(key);
            return segmentFor(hash).get(key, hash);
        } else {
            return null;
        }
    }

    /**
   * Returns the maximum number of threads which can modify this map concurrently.
   *
   * @return
   *  the concurrency level, {@code >= 1}.
   *
   * @since JaXLib 1.0
   */
    public final int getConcurrencyLevel() {
        return this.segments.length;
    }

    public final float getLoadFactor() {
        return this.segments[0].loadFactor;
    }

    @Override
    public final V getValueOfIdentity(final Object key) {
        if (key != null) {
            final int hash = hash(key);
            return segmentFor(hash).getValueOfIdentity(key, hash);
        } else {
            return null;
        }
    }

    @Override
    public final boolean isEmpty() {
        final Segment<K, V>[] segments = this.segments;
        long size = 0;
        for (int i = segments.length; --i >= 0; ) {
            final Segment<K, V> segment = segments[i];
            segment.tryPurge();
            if (segment.count != 0) return false;
        }
        return true;
    }

    @Override
    public XSet<K> keySet() {
        XSet<K> ks = keySet;
        return (ks != null) ? ks : (this.keySet = new KeySet<K>(this));
    }

    /**
   * Removes cleared reference instances from this map.
   * This is also done automatically by put and remove operations. 
   *
   * @see java.lang.ref.WeakReference
   *
   * @since JaXLib 1.0
   */
    public void purge() {
        for (int i = this.segments.length; --i >= 0; ) {
            Segment<K, V> segment = this.segments[i];
            if (segment.firstEnqueued.get() != null) {
                try {
                    segment.lockAndPurge();
                } finally {
                    segment.unlock();
                }
            }
        }
    }

    @Override
    public final V put(K key, V value) {
        if (value == null) throw new NullPointerException();
        int hash = hash(key);
        return segmentFor(hash).put(key, hash, value, false);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> src) {
        if (src != this) {
            Iterator<? extends Map.Entry<? extends K, ? extends V>> it = src.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<? extends K, ? extends V> e = it.next();
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
   * Associates the specified key with the specified value if and only if this map is not already containing
   * the specified key.
   *
   * @return
   *  The value associated with the specified key before this call.
   *
   * @throws NullPointerException
   *  if {@code (key == null) || (value == null)}.
   */
    @Override
    public final V putIfAbsent(K key, V value) {
        if (value == null) throw new NullPointerException("value");
        int hash = hash(key);
        return segmentFor(hash).put(key, hash, value, true);
    }

    /**
   * Associates the specified key with the specified value if and only if this map is not already containing
   * the specified key.
   * <p>
   * This operation works similar to {@link #putIfAbsent(Object,Object) putIfAbsent(key, value)} but returns
   * the value associated with the specified key after this call, instead of the previous value.
   * </p>
   *
   * @return
   *   The value associated with the specified key after this call.
   *
   * @throws NullPointerException
   *  if {@code (key == null) || (value == null)}.
   *
   * @since JaXLib 1.0
   */
    public final V putIfAbsentAndGet(K key, V value) {
        V existing = putIfAbsent(key, value);
        return (existing == null) ? value : existing;
    }

    @Override
    public final V remove(Object key) {
        if (key != null) {
            int hash = hash(key);
            return segmentFor(hash).remove(key, hash);
        } else {
            return null;
        }
    }

    @Override
    public final boolean remove(Object key, Object value) {
        if ((key != null) && (value != null)) {
            int hash = hash(key);
            return segmentFor(hash).removePair(key, hash, value) != null;
        } else {
            return false;
        }
    }

    @Override
    public final V removeValueOfIdentity(Object key) {
        if (key != null) {
            int hash = hash(key);
            return segmentFor(hash).removeValueOfIdentity(key, hash);
        } else {
            return null;
        }
    }

    @Override
    public final V replace(K key, V value) {
        if (value == null) throw new NullPointerException("value");
        if (key != null) {
            int hash = hash(key);
            return segmentFor(hash).replace(key, hash, value);
        } else {
            return null;
        }
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        if (newValue == null) throw new NullPointerException("newValue");
        if ((key != null) && (oldValue != null)) {
            final int hash = hash(key);
            return segmentFor(hash).replace(key, hash, oldValue, newValue);
        } else {
            return false;
        }
    }

    @Override
    public final int size() {
        final Segment<K, V>[] segments = this.segments;
        long size = 0;
        for (int i = segments.length; --i >= 0; ) {
            final Segment<K, V> segment = segments[i];
            segment.tryPurge();
            size += segment.count;
        }
        return (size < Integer.MAX_VALUE) ? (int) size : Integer.MAX_VALUE;
    }

    @Override
    public int trimCapacity(int newCapacity) {
        CheckArg.notNegative(newCapacity, "newCapacity");
        int capacity = capacity();
        if (capacity > newCapacity) {
            long c = 0;
            for (int i = this.segments.length; --i >= 0; ) c += this.segments[i].trimToSize();
            return (c <= Integer.MAX_VALUE) ? (int) c : Integer.MAX_VALUE;
        } else {
            return capacity;
        }
    }

    @Override
    public void trimToSize() {
        for (int i = this.segments.length; --i >= 0; ) this.segments[i].trimToSize();
    }

    @Override
    public XCollection<V> values() {
        XCollection<V> vs = this.values;
        return (vs != null) ? vs : (this.values = new Values());
    }

    /**
   * Returns a reentrant {@code Lock} instance which allows to block all write operations on this map.
   * <p>
   * If a threads owns the returned lock then all other threads calling a method which modifies this map
   * will be blocked until the owning thread releases the lock. Please note that the size of the map may
   * shrink even while a thread is owning the lock, because references to keys may be concurrently cleared by
   * the garbage collector.
   * </p><p>
   * The time required to aquire the lock of the returned {@code Lock} instance increases with the
   * {@link #getConcurrencyLevel() concurrencyLevel} of this map and with the number of threads which are
   * modifying this map concurrently.
   * </p><p>
   * Subsequent calls to this method are returning the same {@code Lock} object. The lock supports 
   * {@link Lock#newCondition() conditions} if and only if the 
   * {@link #getConcurrencyLevel() concurrencyLevel} of this map is equal to 1.
   * </p>
   *
   * @see jaxlib.thread.lock.MultiLock
   *
   * @since JaXLib 1.0
   */
    public Lock writeLock() {
        return this.completeWriteLock;
    }

    final class EntryIterator extends HashIterator implements Map.Entry<K, V>, Iterator<Entry<K, V>>, XIterator<Entry<K, V>> {

        EntryIterator() {
            super();
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this) return true;
            K key = this.key;
            if ((key == null) || !(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            return Objects.equals(key, e.getKey()) && Objects.equals(ConcurrentWeakKeyHashMap.this.get(key), e.getValue());
        }

        @Override
        public final int hashCode() {
            K key = this.key;
            if (key == null) return super.hashCode();
            return Objects.hashCode(key) ^ Objects.hashCode(ConcurrentWeakKeyHashMap.this.get(key));
        }

        public final Map.Entry<K, V> next() {
            nextKey();
            return this;
        }

        public final Map.Entry<K, V> nextElement() {
            nextKey();
            return this;
        }

        public final K getKey() {
            K key = this.key;
            if (key != null) return key; else throw new IllegalStateException();
        }

        public final V getValue() {
            K key = this.key;
            if (key != null) return ConcurrentWeakKeyHashMap.this.get(key); else throw new IllegalStateException();
        }

        public final V setValue(V value) {
            K key = this.key;
            if (key != null) return ConcurrentWeakKeyHashMap.this.put(key, value); else throw new IllegalStateException();
        }

        @Override
        public final String toString() {
            K key = this.key;
            if (key == null) return super.toString(); else return Strings.concat(key, "=", ConcurrentWeakKeyHashMap.this.get(key));
        }
    }

    private final class EntrySet extends AbstractXMap<K, V>.AbstractEntrySet<K, V, Map.Entry<K, V>> {

        EntrySet() {
            super();
        }

        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            V v = ConcurrentWeakKeyHashMap.this.get(e.getKey());
            return (v != null) && v.equals(e.getValue());
        }

        @Override
        public final boolean isEmpty() {
            return ConcurrentWeakKeyHashMap.this.isEmpty();
        }

        @Override
        public final boolean isSizeStable() {
            return false;
        }

        @Override
        public final Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public final boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            return ConcurrentWeakKeyHashMap.this.remove(e.getKey(), e.getValue());
        }

        @Override
        public final int size() {
            return ConcurrentWeakKeyHashMap.this.size();
        }
    }

    static final class HashEntry<K, V> extends ReferenceQueueThread.WeakEntry<K> {

        final int hash;

        volatile V value;

        final HashEntry<K, V> next;

        volatile HashEntry<K, V> nextEnqueued;

        volatile WeakReference<Segment<K, V>> segment;

        HashEntry(K key, int hash, HashEntry<K, V> next, V value, Segment<K, V> segment) {
            super(key, segment.queue);
            this.value = value;
            this.segment = segment.self;
            this.hash = hash;
            this.next = next;
        }

        /**
     * Called by ReferenceQueueThread
     */
        final void referenceCleared() {
            this.value = null;
            WeakReference<Segment<K, V>> segmentRef = this.segment;
            if (segmentRef != null) {
                Segment<K, V> segment = segmentRef.get();
                if (segment != null) {
                    this.nextEnqueued = segment.firstEnqueued.get();
                    segment.firstEnqueued.set(this);
                }
            }
        }

        final void removeFromChain(ConcurrentWeakKeyHashMap.Segment<K, V> segment, HashEntry<K, V> first, int count, HashEntry<K, V>[] table, int index) {
            int removed = 0;
            if (this.segment != null) {
                this.segment = null;
                this.value = null;
                clear();
                removed = 1;
            }
            HashEntry<K, V> newFirst = this.next;
            for (HashEntry<K, V> p = first; p != this; p = p.next) {
                if (p.segment != null) {
                    K key = p.get();
                    V value = p.value;
                    p.segment = null;
                    p.value = null;
                    if (key != null) {
                        p.clear();
                        newFirst = new HashEntry<K, V>(key, p.hash, newFirst, value, segment);
                        continue;
                    } else {
                        removed++;
                        continue;
                    }
                }
            }
            table[index] = newFirst;
            segment.count = count - removed;
        }
    }

    /**
   * Superclass for entry and key iterator.
   */
    abstract class HashIterator extends Object {

        final Segment<K, V>[] segments;

        int nextSegmentIndex;

        int nextTableIndex;

        HashEntry<K, V>[] currentTable;

        HashEntry<K, V> nextEntry;

        K key;

        K nextKey;

        HashIterator() {
            super();
            this.segments = ConcurrentWeakKeyHashMap.this.segments;
            nextSegmentIndex = this.segments.length - 1;
            this.nextTableIndex = -1;
            advance(null);
        }

        final void advance(HashEntry<K, V> nextEntry) {
            if ((nextEntry != null) && (nextEntry = nextEntry.next) != null) {
                K key = nextEntry.get();
                if (key != null) {
                    this.nextEntry = nextEntry;
                    this.nextKey = key;
                    return;
                } else {
                    nextEntry.value = null;
                }
            }
            HashEntry<K, V>[] currentTable = this.currentTable;
            int nextTableIndex = this.nextTableIndex;
            while (nextTableIndex >= 0) {
                if ((nextEntry = currentTable[nextTableIndex--]) != null) {
                    K key = nextEntry.get();
                    if (key != null) {
                        this.nextEntry = nextEntry;
                        this.nextTableIndex = nextTableIndex;
                        this.nextKey = key;
                        return;
                    } else {
                        nextEntry.value = null;
                    }
                }
            }
            int nextSegmentIndex = this.nextSegmentIndex;
            while (nextSegmentIndex >= 0) {
                final Segment<K, V> seg = this.segments[nextSegmentIndex--];
                if (seg.count != 0) {
                    currentTable = seg.table;
                    for (int i = currentTable.length - 1; i >= 0; --i) {
                        if ((nextEntry = currentTable[i]) != null) {
                            K key = nextEntry.get();
                            if (key != null) {
                                this.currentTable = currentTable;
                                this.nextEntry = nextEntry;
                                this.nextSegmentIndex = nextSegmentIndex;
                                this.nextTableIndex = i - 1;
                                this.nextKey = key;
                                return;
                            } else {
                                nextEntry.value = null;
                            }
                        }
                    }
                }
            }
            this.currentTable = null;
            this.nextEntry = null;
            this.nextKey = null;
        }

        public final boolean hasMoreElements() {
            return this.nextEntry != null;
        }

        public final boolean hasNext() {
            return this.nextEntry != null;
        }

        final K nextKey() {
            final HashEntry<K, V> nextEntry = this.nextEntry;
            if (nextEntry != null) {
                K key = this.nextKey;
                this.key = key;
                advance(nextEntry);
                return key;
            } else {
                throw new NoSuchElementException();
            }
        }

        public final void remove() {
            K key = this.key;
            if (key != null) {
                ConcurrentWeakKeyHashMap.this.remove(key);
                this.key = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K>, Enumeration<K>, XIterator<K> {

        KeyIterator() {
            super();
        }

        public K next() {
            return nextKey();
        }

        public K nextElement() {
            return nextKey();
        }
    }

    static final class KeySet<E> extends ConcurrentWeakHashSet<E> {

        KeySet(ConcurrentWeakKeyHashMap<E, ?> map) {
            super((Void) null, map);
        }

        @Override
        public final boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final E internalize(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean replace(Object oldElement, E newElement) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean replaceIdentical(Object oldElement, E newElement) {
            throw new UnsupportedOperationException();
        }
    }

    static final class Segment<K, V> extends ReentrantLock {

        /**
     * The number of elements in this segment's region.
     */
        volatile int count;

        /**
     * The table is rehashed when its size exceeds this threshold.
     * (The value of this field is always (int)(capacity * loadFactor).)
     *
     * No need to be volatile.
     */
        int threshold;

        /**
     * The hash table of this segment.
     */
        volatile HashEntry<K, V>[] table;

        /**
     * The load factor for the hash table.  Even though this value
     * is same for all segments, it is replicated to avoid needing
     * links to outer object.
     */
        final float loadFactor;

        /**
     * The queue handled by ReferenceQueueThread.
     */
        final ReferenceQueue<Object> queue;

        /**
     * Input from ReferenceQueueThread via HashEntry.referenceCleared().
     * Ridden and set to null by lockAndPurge().
     */
        final AtomicReference<HashEntry<K, V>> firstEnqueued;

        /**
     * Reference to this segment itself.
     * Used for HashEntry.segment.
     */
        final WeakReference<Segment<K, V>> self;

        @SuppressWarnings("unchecked")
        Segment(int initialCapacity, float lf, ReferenceQueue<Object> queue) {
            super();
            this.loadFactor = lf;
            this.queue = queue;
            this.firstEnqueued = new AtomicReference<HashEntry<K, V>>();
            this.self = new WeakReference<Segment<K, V>>(this, queue);
            setTable(new HashEntry[initialCapacity]);
        }

        final void dispose() {
            this.self.clear();
            this.table = null;
            this.count = 0;
            while (this.firstEnqueued.getAndSet(null) != null) {
            }
        }

        final void clear() {
            lock();
            try {
                if (this.count != 0) {
                    final HashEntry<K, V>[] table = this.table;
                    for (int i = table.length; --i >= 0; ) {
                        HashEntry<K, V> e = table[i];
                        if (e != null) {
                            table[i] = null;
                            while (e != null) {
                                e.nextEnqueued = null;
                                e.value = null;
                                e.segment = null;
                                e.clear();
                                e = e.next;
                            }
                        }
                    }
                    this.count = 0;
                }
                while (this.firstEnqueued.getAndSet(null) != null) {
                }
            } finally {
                unlock();
            }
        }

        final boolean containsIdenticalKey(Object key, int hash) {
            if (this.count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if (key == k) return true;
                        } else {
                            e.value = null;
                        }
                    }
                    e = e.next;
                }
            }
            return false;
        }

        final boolean containsKey(Object key, int hash) {
            if (this.count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if ((key == k) || key.equals(k)) return true;
                        } else {
                            e.value = null;
                        }
                    }
                    e = e.next;
                }
            }
            return false;
        }

        final boolean containsValue(Object value) {
            if (this.count != 0) {
                final HashEntry<K, V>[] table = this.table;
                for (HashEntry<K, V> e : table) {
                    while (e != null) {
                        V v = e.value;
                        if ((v != null) && value.equals(v)) return true;
                        e = e.next;
                    }
                }
            }
            return false;
        }

        final V createIfAbsent(final K key, final int hash, Function<? super K, ? extends V> factory1, Callable<? extends V> factory2) throws Exception {
            V newValue = get(key, hash);
            if (newValue != null) return newValue;
            try {
                lockAndPurge();
                int c = this.count;
                HashEntry<K, V>[] table = this.table;
                int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                HashEntry<K, V> e = first;
                while (e != null) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if ((key == k) || key.equals(k)) return e.value;
                        } else {
                            e.value = null;
                            if (e == first) {
                                e.segment = null;
                                this.count = --c;
                                table[index] = first = e.next;
                            }
                        }
                    }
                    e = e.next;
                }
                if (c > this.threshold) {
                    c = rehash(-1);
                    table = this.table;
                    index = hash & (table.length - 1);
                    first = table[index];
                }
                newValue = (factory1 == null) ? factory2.call() : factory1.apply(key);
                if (newValue != null) {
                    table[index] = new HashEntry<K, V>(key, hash, first, newValue, this);
                    this.count = c + 1;
                }
            } finally {
                unlock();
            }
            if (newValue != null) {
                return newValue;
            } else {
                throw new IllegalArgumentException(Strings.concat("Specified factory returned null:" + "\n  factory = ", ((factory1 == null) ? factory2 : factory1).toString(), "\n  key     = ", key.toString()));
            }
        }

        final V get(final Object key, final int hash) {
            if (this.count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if ((key == k) || key.equals(k)) return e.value;
                        } else {
                            e.value = null;
                        }
                    }
                    e = e.next;
                }
            }
            return null;
        }

        /**
     * Used by ConcurrentWeakHashSet
     */
        final K getEqualKey(final Object key, final int hash) {
            if (this.count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if ((key == k) || key.equals(k)) return k;
                        } else {
                            e.value = null;
                        }
                    }
                    e = e.next;
                }
            }
            return null;
        }

        /**
     * Return properly casted first entry of bin for given hash
     */
        final HashEntry<K, V> getFirst(int hash) {
            HashEntry[] table = this.table;
            return table[hash & (table.length - 1)];
        }

        final V getValueOfIdentity(final Object key, final int hash) {
            if (this.count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if (key == k) return e.value;
                        } else {
                            e.value = null;
                        }
                    }
                    e = e.next;
                }
            }
            return null;
        }

        /**
     * Used by ConcurrentWeakHashSet
     */
        final K internalizeKey(final K key, final int hash, final V value) {
            try {
                lockAndPurge();
                int c = this.count;
                HashEntry<K, V>[] table = this.table;
                int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K oldKey = e.get();
                        if (oldKey != null) {
                            if ((key == oldKey) || key.equals(oldKey)) return oldKey;
                        } else {
                            e.value = null;
                            if (e == first) {
                                e.segment = null;
                                this.count = --c;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
                if (c > this.threshold) {
                    c = rehash(-1);
                    table = this.table;
                    index = hash & (table.length - 1);
                    first = table[index];
                }
                table[index] = new HashEntry<K, V>(key, hash, first, value, this);
                this.count = c + 1;
                return key;
            } finally {
                unlock();
            }
        }

        final void lockAndPurge() {
            lock();
            purge();
        }

        final void purge() {
            HashEntry<K, V>[] table = null;
            int count = 0;
            int sizeMask = 0;
            DEQUEUE: while (true) {
                HashEntry<K, V> re = this.firstEnqueued.getAndSet(null);
                if (re == null) {
                    return;
                } else {
                    do {
                        if (re.segment != null) {
                            re.segment = null;
                            if (table == null) {
                                table = this.table;
                                count = this.count;
                                sizeMask = table.length - 1;
                            }
                            if (count != 0) {
                                final int index = re.hash & sizeMask;
                                final HashEntry<K, V> first = table[index];
                                HashEntry<K, V> e = first;
                                REMOVE: while (e != null) {
                                    if (e == re) {
                                        int removed = 1;
                                        HashEntry<K, V> newFirst = re.next;
                                        for (HashEntry<K, V> p = first; p != re; p = p.next) {
                                            if (p.segment != null) {
                                                p.segment = null;
                                                K key = p.get();
                                                if (key != null) {
                                                    newFirst = new HashEntry<K, V>(key, p.hash, newFirst, p.value, this);
                                                    key = null;
                                                } else {
                                                    p.value = null;
                                                    removed++;
                                                }
                                            }
                                        }
                                        table[index] = newFirst;
                                        this.count = (count -= removed);
                                        break REMOVE;
                                    } else {
                                        e = e.next;
                                    }
                                }
                            }
                        }
                        HashEntry<K, V> next = re.nextEnqueued;
                        re.nextEnqueued = null;
                        re = next;
                    } while (re != null);
                }
            }
        }

        final V put(final K key, final int hash, final V value, final boolean onlyIfAbsent) {
            try {
                lockAndPurge();
                int c = this.count;
                HashEntry<K, V>[] table = this.table;
                int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K oldKey = e.get();
                        if (oldKey != null) {
                            if ((key == oldKey) || key.equals(oldKey)) {
                                V oldValue = e.value;
                                if (!onlyIfAbsent) e.value = value;
                                return oldValue;
                            }
                        } else {
                            e.value = null;
                            if (e == first) {
                                e.segment = null;
                                this.count = --c;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
                if (c > this.threshold) {
                    c = rehash(-1);
                    table = this.table;
                    index = hash & (table.length - 1);
                    first = table[index];
                }
                table[index] = new HashEntry<K, V>(key, hash, first, value, this);
                this.count = c + 1;
            } finally {
                unlock();
            }
            return null;
        }

        final int rehash(int newCapacity) {
            final HashEntry<K, V>[] oldTable = this.table;
            final int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY) return this.count;
            if (newCapacity < 0) {
                int removed = 0;
                for (int i = oldTable.length; --i >= 0; ) {
                    HashEntry<K, V> e = oldTable[i];
                    if (e != null) {
                        while ((e != null) && (e.get() == null)) {
                            removed++;
                            e.segment = null;
                            e.value = null;
                            e = e.next;
                        }
                        oldTable[i] = e;
                    }
                }
                if (removed > 0) {
                    int newCount = this.count - removed;
                    this.count = newCount;
                    if (newCount == 0) this.firstEnqueued.set(null);
                    return newCount;
                } else {
                    int oldCount = this.count;
                    Thread.yield();
                    purge();
                    int newCount = this.count;
                    if (newCount < oldCount) return newCount;
                }
                newCapacity = oldCapacity << 1;
            }
            final HashEntry<K, V>[] newTable = new HashEntry[newCapacity];
            final int sizeMask = newTable.length - 1;
            int newCount = 0;
            ARRAY: for (int oldIndex = oldCapacity; --oldIndex >= 0; ) {
                HashEntry<K, V> e = oldTable[oldIndex];
                while ((e != null) && (e.get() == null)) {
                    e.segment = null;
                    e.value = null;
                    e = e.next;
                }
                while (e != null) {
                    final HashEntry<K, V> next = e.next;
                    final int newIndex = e.hash & sizeMask;
                    final HashEntry<K, V> newFirst = newTable[newIndex];
                    if (newFirst == null) {
                        HashEntry<K, V> n = next;
                        int chainLength = 1;
                        while ((n != null) && ((n.hash & sizeMask) == newIndex)) {
                            n = n.next;
                            chainLength++;
                        }
                        if (n == null) {
                            newTable[newIndex] = e;
                            newCount += chainLength;
                            continue ARRAY;
                        }
                    }
                    e.segment = null;
                    K key = e.get();
                    if (key != null) {
                        newTable[newIndex] = new HashEntry<K, V>(key, e.hash, newFirst, e.value, this);
                        key = null;
                        newCount++;
                    } else {
                        e.value = null;
                    }
                    e = next;
                    continue;
                }
            }
            this.count = newCount;
            setTable(newTable);
            if (newCount == 0) this.firstEnqueued.set(null);
            return newCount;
        }

        final V remove(final Object key, final int hash) {
            try {
                lockAndPurge();
                int count = this.count;
                final HashEntry<K, V>[] table = this.table;
                final int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if ((key == k) || key.equals(k)) {
                                k = null;
                                V oldValue = e.value;
                                e.removeFromChain(this, first, count, table, index);
                                return oldValue;
                            }
                        } else {
                            e.value = null;
                            if ((e == first) && (e.segment != null)) {
                                e.segment = null;
                                this.count = --count;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
            } finally {
                unlock();
            }
            return null;
        }

        /**
     * Used by ConcurrentHashXSet
     */
        final K removeEqualKey(final Object key, final int hash) {
            try {
                lockAndPurge();
                int count = this.count;
                final HashEntry<K, V>[] table = this.table;
                final int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K oldKey = e.get();
                        if (oldKey != null) {
                            if ((key == oldKey) || key.equals(oldKey)) {
                                e.removeFromChain(this, first, count, table, index);
                                return oldKey;
                            }
                        } else {
                            e.value = null;
                            if ((e == first) && (e.segment != null)) {
                                e.segment = null;
                                this.count = --count;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
            } finally {
                unlock();
            }
            return null;
        }

        final V removePair(final Object key, final int hash, final Object value) {
            try {
                lockAndPurge();
                int count = this.count;
                final HashEntry<K, V>[] table = this.table;
                final int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K oldKey = e.get();
                        if (oldKey != null) {
                            if ((key == oldKey) || key.equals(oldKey)) {
                                V oldValue = e.value;
                                if ((value == oldValue) || value.equals(oldValue)) {
                                    oldKey = null;
                                    e.removeFromChain(this, first, count, table, index);
                                    return oldValue;
                                }
                            }
                        } else {
                            e.value = null;
                            if ((e == first) && (e.segment != null)) {
                                e.segment = null;
                                this.count = --count;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
            } finally {
                unlock();
            }
            return null;
        }

        final V removeValueOfIdentity(final Object key, final int hash) {
            try {
                lockAndPurge();
                int count = this.count;
                final HashEntry<K, V>[] table = this.table;
                final int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if (key == k) {
                                V oldValue = e.value;
                                e.removeFromChain(this, first, count, table, index);
                                return oldValue;
                            }
                        } else {
                            e.value = null;
                            if ((e == first) && (e.segment != null)) {
                                e.segment = null;
                                this.count = --count;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
            } finally {
                unlock();
            }
            return null;
        }

        final boolean replace(K key, int hash, V oldValue, V newValue) {
            try {
                lockAndPurge();
                int count = this.count;
                final HashEntry<K, V>[] table = this.table;
                final int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K k = e.get();
                        if (k != null) {
                            if ((key == k) || key.equals(k)) {
                                V value = e.value;
                                if ((oldValue == value) || oldValue.equals(value)) e.value = newValue;
                                return true;
                            }
                        } else {
                            e.value = null;
                            if ((e == first) && (e.segment != null)) {
                                e.segment = null;
                                this.count = --count;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
            } finally {
                unlock();
            }
            return false;
        }

        final V replace(final K key, final int hash, final V newValue) {
            try {
                lockAndPurge();
                int count = this.count;
                final HashEntry<K, V>[] table = this.table;
                final int index = hash & (table.length - 1);
                HashEntry<K, V> first = table[index];
                for (HashEntry<K, V> e = first; e != null; e = e.next) {
                    if (e.hash == hash) {
                        K oldKey = e.get();
                        if (oldKey != null) {
                            if ((key == oldKey) || key.equals(oldKey)) {
                                V oldValue = e.value;
                                e.value = newValue;
                                return oldValue;
                            }
                        } else {
                            e.value = null;
                            if (e == first) {
                                e.segment = null;
                                this.count = --count;
                                table[index] = first = e.next;
                            }
                        }
                    }
                }
            } finally {
                unlock();
            }
            return null;
        }

        /**
     * Set table to new HashEntry array.
     * Call only while holding lock or in constructor.
     */
        final void setTable(final HashEntry<K, V>[] newTable) {
            this.threshold = (int) (newTable.length * this.loadFactor);
            this.table = newTable;
        }

        final int trimToSize() {
            try {
                lockAndPurge();
                int c = (int) (this.count * this.loadFactor);
                int newCapacity = 1;
                while (newCapacity < c) newCapacity <<= 1;
                int capacity = this.table.length;
                if (newCapacity < capacity) {
                    rehash(newCapacity);
                    return newCapacity;
                } else {
                    return capacity;
                }
            } finally {
                unlock();
            }
        }

        final void tryPurge() {
            if ((this.firstEnqueued.get() != null) && tryLock()) {
                try {
                    purge();
                } finally {
                    unlock();
                }
            }
        }
    }

    /**
   * Superclass for entry and key iterator.
   */
    final class ValueIterator extends Object implements Iterator<V>, XIterator<V> {

        final Segment<K, V>[] segments;

        int nextSegmentIndex;

        int nextTableIndex;

        HashEntry<K, V>[] currentTable;

        HashEntry<K, V> nextEntry;

        HashEntry<K, V> lastReturned;

        V nextValue;

        V value;

        ValueIterator() {
            super();
            this.segments = ConcurrentWeakKeyHashMap.this.segments;
            nextSegmentIndex = this.segments.length - 1;
            this.nextTableIndex = -1;
            advance(null);
        }

        final void advance(HashEntry<K, V> nextEntry) {
            if ((nextEntry != null) && (nextEntry = nextEntry.next) != null) {
                V value = nextEntry.value;
                if (value != null) {
                    this.nextEntry = nextEntry;
                    this.nextValue = value;
                    return;
                }
            }
            HashEntry<K, V>[] currentTable = this.currentTable;
            int nextTableIndex = this.nextTableIndex;
            while (nextTableIndex >= 0) {
                if ((nextEntry = currentTable[nextTableIndex--]) != null) {
                    V value = nextEntry.value;
                    if (value != null) {
                        this.nextEntry = nextEntry;
                        this.nextTableIndex = nextTableIndex;
                        this.nextValue = value;
                        return;
                    }
                }
            }
            int nextSegmentIndex = this.nextSegmentIndex;
            while (nextSegmentIndex >= 0) {
                final Segment<K, V> seg = this.segments[nextSegmentIndex--];
                if (seg.count != 0) {
                    currentTable = seg.table;
                    for (int i = currentTable.length - 1; i >= 0; --i) {
                        if ((nextEntry = currentTable[i]) != null) {
                            V value = nextEntry.value;
                            if (value != null) {
                                this.currentTable = currentTable;
                                this.nextEntry = nextEntry;
                                this.nextSegmentIndex = nextSegmentIndex;
                                this.nextTableIndex = i - 1;
                                this.nextValue = value;
                                return;
                            }
                        }
                    }
                }
            }
            this.currentTable = null;
            this.nextEntry = null;
            this.nextValue = null;
        }

        public final boolean hasMoreElements() {
            return this.nextEntry != null;
        }

        public final boolean hasNext() {
            return this.nextEntry != null;
        }

        public final V next() {
            final HashEntry<K, V> nextEntry = this.nextEntry;
            if (nextEntry != null) {
                this.lastReturned = nextEntry;
                V value = lastReturned.value;
                if (value == null) value = this.nextValue;
                this.value = value;
                advance(nextEntry);
                return value;
            } else {
                throw new NoSuchElementException();
            }
        }

        public final V nextElement() {
            return next();
        }

        public final void remove() {
            final HashEntry<K, V> lastReturned = this.lastReturned;
            if (lastReturned != null) {
                this.lastReturned = null;
                V value = this.value;
                this.value = null;
                K key = lastReturned.get();
                if (key != null) ConcurrentWeakKeyHashMap.this.remove(key, value); else lastReturned.value = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private final class Values extends AbstractXMap<K, V>.DefaultValueCollection<V> {

        Values() {
            super();
        }

        @Override
        public final boolean isEmpty() {
            return ConcurrentWeakKeyHashMap.this.isEmpty();
        }

        @Override
        public final boolean isSizeStable() {
            return false;
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public final int size() {
            return ConcurrentWeakKeyHashMap.this.size();
        }

        @Override
        public final Object[] toArray() {
            return toArray(new Object[size()], true);
        }

        @Override
        public final Object[] toArray(Object[] dest) {
            return toArray(dest, false);
        }

        private Object[] toArray(Object[] dest, boolean copied) {
            int destIndex = 0;
            for (final Segment<K, V> segment : ConcurrentWeakKeyHashMap.this.segments) {
                for (HashEntry<K, V> e : segment.table) {
                    while (e != null) {
                        final V value = e.value;
                        if (value != null) {
                            if (destIndex == dest.length) {
                                final Object[] newDest = (Object[]) Array.newInstance(dest.getClass().getComponentType(), (destIndex == 0) ? 16 : (destIndex << 1));
                                System.arraycopy(dest, 0, newDest, 0, destIndex);
                                dest = newDest;
                                copied = true;
                            }
                            dest[destIndex++] = value;
                        }
                        e = e.next;
                    }
                }
            }
            if (destIndex < dest.length) {
                if (copied) {
                    final Object[] newDest = new Object[destIndex];
                    System.arraycopy(dest, 0, newDest, 0, destIndex);
                    dest = newDest;
                } else {
                    dest[destIndex] = null;
                }
            }
            return dest;
        }

        @Override
        public final int toArray(final Object[] dest, int destIndex) {
            CheckArg.copyCount(0, dest.length, destIndex);
            final int firstIndex = destIndex;
            if (destIndex < dest.length) {
                for (final Segment<K, V> segment : ConcurrentWeakKeyHashMap.this.segments) {
                    for (HashEntry<K, V> e : segment.table) {
                        while (e != null) {
                            final V value = e.value;
                            if (value != null) {
                                dest[destIndex] = value;
                                if (++destIndex == dest.length) return destIndex - firstIndex;
                            }
                            e = e.next;
                        }
                    }
                }
            }
            return destIndex - firstIndex;
        }
    }
}
