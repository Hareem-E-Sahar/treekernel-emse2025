package jaxlib.col.ref;

import java.lang.reflect.Array;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import jaxlib.closure.Closure;
import jaxlib.closure.Filter;
import jaxlib.col.AbstractXSet;
import jaxlib.col.XSet;
import jaxlib.jaxlib_private.CheckArg;

/**
 * @author  <a href="mailto:joerg.wassmer@web.de">J�rg Wa�mer</a>
 * @since   JaXLib 1.0
 * @version $Id: ConcurrentWeakHashSet.java 1511 2005-12-11 14:13:40Z joerg_wassmer $
 */
public class ConcurrentWeakHashSet<E> extends AbstractXSet<E> implements XSet<E> {

    /**
   * @serial
   * @since JaXLib 1.0
   */
    private final ConcurrentWeakKeyHashMap<E, Object> map;

    /**
   * @serial
   * @since JaXLib 1.0
   */
    private final Object value;

    public ConcurrentWeakHashSet() {
        super();
        this.map = new ConcurrentWeakKeyHashMap<E, Object>();
        this.value = Boolean.TRUE;
    }

    public ConcurrentWeakHashSet(final int initialCapacity, final float loadFactor, final int concurrencyLevel) {
        super();
        this.map = new ConcurrentWeakKeyHashMap<E, Object>(initialCapacity, loadFactor, concurrencyLevel);
        this.value = Boolean.TRUE;
    }

    public ConcurrentWeakHashSet(final Iterable<? extends E> src) {
        this((src instanceof Collection) ? Math.max((int) (((Collection) src).size() / ConcurrentWeakKeyHashMap.DEFAULT_LOAD_FACTOR) + 1, 11) : ConcurrentWeakKeyHashMap.DEFAULT_INITIAL_CAPACITY, ConcurrentWeakKeyHashMap.DEFAULT_LOAD_FACTOR, ConcurrentWeakKeyHashMap.DEFAULT_SEGMENTS);
        addRemaining(src.iterator());
    }

    <V> ConcurrentWeakHashSet(final ConcurrentWeakKeyHashMap<E, ? super V> map, final V value) {
        super();
        if (map == null) throw new NullPointerException("map");
        if (value == null) throw new NullPointerException("value");
        this.map = (ConcurrentWeakKeyHashMap<E, Object>) map;
        this.value = value;
    }

    /**
   * Constructor for ConcurrentWeakKeyHashMap.KeySet
   */
    ConcurrentWeakHashSet(final Void dummy, final ConcurrentWeakKeyHashMap<E, ?> map) {
        super();
        this.map = (ConcurrentWeakKeyHashMap<E, Object>) map;
        this.value = null;
    }

    @Override
    public boolean add(final E e) {
        return this.map.putIfAbsent(e, this.value) == null;
    }

    @Override
    public final int capacity() {
        return this.map.capacity();
    }

    @Override
    public final boolean contains(final Object e) {
        return this.map.containsKey(e);
    }

    @Override
    public final boolean containsIdentical(final Object e) {
        if (e != null) {
            final int hash = ConcurrentWeakKeyHashMap.hash(e);
            return this.map.segmentFor(hash).containsIdenticalKey(e, hash);
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public E findMatch(Filter<? super E> condition, boolean iF) {
        for (ConcurrentWeakKeyHashMap.Segment<E, ?> segment : this.map.segments) {
            ConcurrentWeakKeyHashMap.HashEntry<E, ?>[] table = segment.table;
            for (ConcurrentWeakKeyHashMap.HashEntry<E, ?> e : table) {
                while (e != null) {
                    E key = e.get();
                    if (key != null) {
                        if (condition.accept(key) == iF) return key;
                    } else {
                        e.value = null;
                    }
                    e = e.next;
                }
            }
            table = null;
        }
        return null;
    }

    @Override
    public int forEach(Closure<? super E> procedure) {
        int count = 0;
        for (ConcurrentWeakKeyHashMap.Segment<E, ?> segment : this.map.segments) {
            ConcurrentWeakKeyHashMap.HashEntry<E, ?>[] table = segment.table;
            for (ConcurrentWeakKeyHashMap.HashEntry<E, ?> e : table) {
                while (e != null) {
                    E key = e.get();
                    if (key != null) {
                        if (procedure.proceed(key)) count++; else return count;
                    } else {
                        e.value = null;
                    }
                    e = e.next;
                }
            }
            table = null;
        }
        return count;
    }

    /**
   * Returns the maximum number of threads which can modify this set concurrently.
   *
   * @return
   *  the concurrency level, {@code >= 1}.
   *
   * @since JaXLib 1.0
   */
    public final int getConcurrencyLevel() {
        return this.map.getConcurrencyLevel();
    }

    @Override
    public final E getEqual(final Object e) {
        if (e == null) throw new NullPointerException("element");
        final int hash = ConcurrentWeakKeyHashMap.hash(e);
        return this.map.segmentFor(hash).getEqualKey(e, hash);
    }

    public final float getLoadFactor() {
        return this.map.getLoadFactor();
    }

    @Override
    public E internalize(final E e) {
        if (e == null) throw new NullPointerException("element");
        final int hash = ConcurrentWeakKeyHashMap.hash(e);
        return this.map.segmentFor(hash).internalizeKey(e, hash, this.value);
    }

    @Override
    public final boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public final boolean isSizeStable() {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return this.map.keys();
    }

    /**
   * Removes cleared reference instances from this set.
   * This is also done automatically by add and remove operations. 
   *
   * @see java.lang.ref.WeakReference
   *
   * @since JaXLib 1.0
   */
    public void purge() {
        this.map.purge();
    }

    @Override
    public final boolean remove(final Object e) {
        return this.map.remove(e) != null;
    }

    @Override
    public final E removeEqual(final Object e) {
        if (e != null) {
            final int hash = ConcurrentWeakKeyHashMap.hash(e);
            return this.map.segmentFor(hash).removeEqualKey(e, hash);
        } else {
            return null;
        }
    }

    @Override
    public final boolean removeIdentical(final Object e) {
        return this.map.removeValueOfIdentity(e) != null;
    }

    @Override
    public final int size() {
        return this.map.size();
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
        for (ConcurrentWeakKeyHashMap.Segment<E, ?> segment : this.map.segments) {
            ConcurrentWeakKeyHashMap.HashEntry<E, ?>[] table = segment.table;
            for (ConcurrentWeakKeyHashMap.HashEntry<E, ?> e : table) {
                while (e != null) {
                    E key = e.get();
                    if (key != null) {
                        if (destIndex == dest.length) {
                            Object[] newDest = (Object[]) Array.newInstance(dest.getClass().getComponentType(), (destIndex == 0) ? 16 : (destIndex << 1));
                            System.arraycopy(dest, 0, newDest, 0, destIndex);
                            dest = newDest;
                            copied = true;
                        }
                        dest[destIndex++] = key;
                    } else {
                        e.value = null;
                    }
                    e = e.next;
                }
            }
            table = null;
        }
        if (destIndex < dest.length) {
            if (copied) {
                Object[] newDest = new Object[destIndex];
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
            for (final ConcurrentWeakKeyHashMap.Segment<E, ?> segment : this.map.segments) {
                ConcurrentWeakKeyHashMap.HashEntry<E, ?>[] table = segment.table;
                for (ConcurrentWeakKeyHashMap.HashEntry<E, ?> e : table) {
                    while (e != null) {
                        E key = e.get();
                        if (key != null) {
                            dest[destIndex] = key;
                            if (++destIndex == dest.length) return destIndex - firstIndex;
                        } else {
                            e.value = null;
                        }
                        e = e.next;
                        continue;
                    }
                }
                table = null;
            }
        }
        return destIndex - firstIndex;
    }

    @Override
    public int trimCapacity(final int newCapacity) {
        return this.map.trimCapacity(newCapacity);
    }

    @Override
    public void trimToSize() {
        this.map.trimToSize();
    }
}
