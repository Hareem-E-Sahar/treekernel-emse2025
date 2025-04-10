package net.community.chest.util.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.IntegersComparator;
import net.community.chest.util.map.entries.IntegersMapEntry;

/**
 * Copyright (C) 2007 according to GPLv2
 *
 * <P>An efficient map of <I>int</I>/{@link Integer} to any {@link Object}.
 * Basically, this is done by maintaining 2 arrays:</P></BR>
 * <UL>
 * 		<LI>
 * 		keys array - <U>sorted</U> <I>int</I> array containing the integer
 * 		keys - where places 0-{@link #size()} are valid, and the rest (empty)
 * 		are marked with {@link Integer#MAX_VALUE}. This is done so that
 * 		sorting and binary search(es) will "push" the empty entries to the
 * 		end of the array. <B>Note:</B> this also means that the {@link Integer#MAX_VALUE}
 * 		value is not allowed to serve as a key (but {@link Integer#MIN_VALUE} is...)
 * 		</LI>
 * 
 * 		<LI>
 * 		objects array - the object at index <I>N</I> simply matches the integer
 * 		value at the same index of the keys array. In order to simplify matters,
 * 		places 0-{@link #size()} contain the objects, while the rest are null.
 * 		<B>Note:</B> this also means that <I>null</I> is NOT allowed as an
 * 		{@link Object} reference.
 * 		</LI>
 * </UL>
 *
 * <P>The map itself may grow automatically according to the amount specified
 * by {@link #getGrowSize()}/{@link #setGrowSize(int)}. It can also be
 * "forced" to grow by either call to {@link #grow()}/{@link #grow(int)}</P>
 *
 * @param <V> Type of mapped value
 * @author Lyor G.
 * @since Jun 10, 2007 2:22:36 PM
 */
public class IntegersMap<V> extends NumbersMap<Integer, V> implements PubliclyCloneable<IntegersMap<V>> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2262130276691585255L;

    /**
	 * @param location location indication
	 * @param fromKey start key in range (inclusive)
	 * @param toKey end key in range (exclusive)
	 * @return {@link #getExceptionLocation(String)} + "[" + from + "-" + to + "]
	 */
    protected String getRangeExceptionLocation(final String location, final int fromKey, final int toKey) {
        return getExceptionLocation(location) + "[" + fromKey + "-" + toKey + "]";
    }

    /**
	 * Current key values array - empty places are marked with {@link Integer#MAX_VALUE}
	 */
    private int[] _keyVals;

    protected int[] getKeys() {
        return _keyVals;
    }

    @Override
    protected void markEmptySpots(final int fromIndex) throws IllegalStateException, IllegalArgumentException {
        final int[] k = getKeys();
        final int numKeys = (null == k) ? 0 : k.length;
        final V[] o = getObjects();
        final int numObjects = (null == o) ? 0 : o.length;
        if (numKeys != numObjects) throw new IllegalStateException(getExceptionLocation("markEmptySpots") + "(" + fromIndex + ") keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");
        if ((fromIndex < 0) || (fromIndex > numKeys) || (fromIndex > numObjects)) throw new IllegalArgumentException(getExceptionLocation("markEmptySpots") + "(" + fromIndex + ") bad/illegal index (range=0-" + numKeys + ")");
        for (int i = fromIndex; i < numKeys; i++) {
            k[i] = Integer.MAX_VALUE;
            o[i] = null;
        }
    }

    public Collection<V> values(final int fromKey, final int toKey) {
        final Collection<? extends Entry<Integer, V>> eSet = entrySet(fromKey, toKey);
        if ((null == eSet) || (eSet.size() <= 0)) return null;
        Collection<V> ret = null;
        for (final Entry<Integer, V> e : eSet) {
            final V value = (null == e) ? null : e.getValue();
            if (null == value) continue;
            if (null == ret) ret = new LinkedList<V>();
            ret.add(value);
        }
        return ret;
    }

    @Override
    public Collection<V> values() {
        return values(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
	 * Constructor
	 * @param objClass class of object to be used (required for creating
	 * arrays) - may NOT be null
	 * @param initialSize allocated room initially - may be zero provided grow
	 * size is non-zero. May NOT be negative
	 * @param growSize how much to make room automatically if needed - may be
	 * zero provided initial size is non-zero (in which case, any attempt to
	 * grow beyond the limits will cause an exception - unless subsequent
	 * call(s) to {@link #setGrowSize(int)} or {@link #grow(int)}
	 * @throws IllegalArgumentException if no class, negative values or both zero
	 */
    public IntegersMap(final Class<V> objClass, final int initialSize, final int growSize) throws IllegalArgumentException {
        super(Integer.class, objClass, initialSize, growSize);
        if (initialSize > 0) _keyVals = new int[initialSize];
        markEmptySpots();
    }

    @Override
    public void grow(final int growSize) throws IllegalArgumentException, IllegalStateException {
        if (growSize < 0) throw new IllegalArgumentException(getGrowExceptionLocation(growSize) + " negative requested size");
        if (growSize > 0) {
            final int[] k = getKeys();
            final int numKeys = (null == k) ? 0 : k.length;
            final V[] o = getObjects();
            final int numObjects = (null == o) ? 0 : o.length;
            if (numKeys != numObjects) throw new IllegalStateException(getGrowExceptionLocation(growSize) + " keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");
            final int newSize = numKeys + growSize, curSize = size();
            _keyVals = new int[newSize];
            if (curSize > 0) System.arraycopy(k, 0, _keyVals, 0, curSize);
            growObjects(newSize);
            markEmptySpots();
        }
    }

    /**
	 * Useful string for {@link #findEntryIndex(int)} exceptions text
	 * @param key key value requested for mapping
	 * @return {@link #getExceptionLocation(String)} + "(" + key + ")"
	 */
    protected String getFindEntryIndexExceptionLocation(int key) {
        return getExceptionLocation("findEntryIndex") + "(" + key + ")";
    }

    /**
	 * Attempts to find the location for the key
	 * @param key key whose location is requested
	 * @return >=0 if successful, -(insertionPoint)-1 otherwise
	 * @throws IllegalArgumentException if key is {@link Integer#MAX_VALUE}
	 * @throws IllegalStateException if {@link #size()} greater than keys
	 * array length (which should not happen) or found match beyond the
	 * {@link #size()} - which means that {@link Integer#MAX_VALUE} was
	 * found
	 * @see Arrays#binarySearch(int[], int)
	 */
    protected int findEntryIndex(final int key) throws IllegalArgumentException, IllegalStateException {
        if (Integer.MAX_VALUE == key) throw new IllegalArgumentException(getFindEntryIndexExceptionLocation(key) + " illegal key value");
        final int curSize = size();
        if (curSize <= 0) return (-1);
        final int[] k = getKeys();
        final int numKeys = (null == k) ? 0 : k.length;
        if (curSize > numKeys) throw new IllegalStateException(getFindEntryIndexExceptionLocation(key) + " mismatched size(" + curSize + ")/keys(" + numKeys + ") values");
        final int eIndex = Arrays.binarySearch(k, key);
        if (eIndex >= curSize) throw new IllegalStateException(getFindEntryIndexExceptionLocation(key) + " found entry at index=" + eIndex + " beyond current size (" + curSize + ")");
        return eIndex;
    }

    /**
	 * Copyright 2007 as per GPLv2
	 * 
	 * Compares 2 entries based on their {@link Integer} key
	 * 
	 * @author Lyor G.
	 * @since Jun 10, 2007 5:11:51 PM
	 */
    public static class IntegerEntriesComparator extends NumberEntriesComparator {

        /**
		 * 
		 */
        private static final long serialVersionUID = -330563119580127067L;

        public IntegerEntriesComparator() {
            super();
        }

        @Override
        protected int compareNumbers(Number n1, Number n2) {
            return IntegersComparator.compare((null == n1) ? 0 : n1.intValue(), (null == n2) ? 0 : n2.intValue());
        }

        public static final IntegerEntriesComparator DEFAULT = new IntegerEntriesComparator();
    }

    @Override
    public Comparator<Entry<? extends Number, ?>> getEntryComparator() {
        return IntegerEntriesComparator.DEFAULT;
    }

    public Set<Entry<Integer, V>> entrySet(final int fromKey, final int toKey) {
        if (fromKey > toKey) throw new IllegalArgumentException(getRangeExceptionLocation("entrySet", fromKey, toKey) + " inverted range");
        final int numItems = size();
        if (numItems <= 0) return null;
        final int[] ks = getKeys();
        if ((null == ks) || (ks.length < numItems)) throw new IllegalStateException(getRangeExceptionLocation("entrySet", fromKey, toKey) + " mismatched size (" + numItems + ") vs. key set size (" + ((null == ks) ? 0 : ks.length) + ")");
        final V[] vs = getObjects();
        if ((null == vs) || (vs.length < numItems)) throw new IllegalStateException(getRangeExceptionLocation("entrySet", fromKey, toKey) + " mismatched size (" + numItems + ") vs. values set size (" + ((null == vs) ? 0 : vs.length) + ")");
        TreeSet<Entry<Integer, V>> ts = null;
        for (int eIndex = 0; eIndex < numItems; eIndex++) {
            final int k = ks[eIndex];
            if (k >= toKey) break;
            if (k >= fromKey) {
                final IntegersMapEntry<V> e = new IntegersMapEntry<V>(Integer.valueOf(k), vs[eIndex]);
                if (null == ts) ts = new TreeSet<Entry<Integer, V>>(getEntryComparator());
                ts.add(e);
            }
        }
        return ts;
    }

    @Override
    public Set<Entry<Integer, V>> entrySet() {
        return entrySet(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
	 * Useful string for {@link #put(int, Object)} exceptions
	 * @param key key value requested for mapping
	 * @param obj object value requested for mapping
	 * @return {@link #getExceptionLocation(String)} + "(" + key + "=>" + String.valueOf(obj) + ")"
	 */
    protected String getPutExceptionLocation(final int key, final V obj) {
        return getExceptionLocation("put") + "(" + key + "=>" + String.valueOf(obj) + ")";
    }

    /**
	 * Maps an <I>int</I> key to a (non-null) {@link Object}
	 * @param key key value - may NOT be {@link Integer#MAX_VALUE}
	 * @param obj object value - may NOT be <I>null</I>
	 * @return previous object - <I>null</I> if object mapped for the first time
	 * @throws NullPointerException if null object
	 * @throws IllegalArgumentException bad/illegal key
	 * @throws IllegalStateException cannot grow, null previous mapping, etc.
	 */
    public V put(final int key, final V obj) throws NullPointerException, IllegalArgumentException, IllegalStateException {
        if (Integer.MAX_VALUE == key) throw new IllegalArgumentException(getPutExceptionLocation(key, obj) + " bad/illegal key");
        if (null == obj) throw new NullPointerException(getPutExceptionLocation(key, obj) + " bad/illegal object");
        final int eIndex = findEntryIndex(key);
        V[] o = getObjects();
        int numObjects = (null == o) ? 0 : o.length;
        if (eIndex >= 0) {
            if (eIndex >= numObjects) throw new IllegalStateException(getPutExceptionLocation(key, obj) + " previous index (" + eIndex + ") beyond objects arrays length (" + numObjects + ")");
            final V prev = o[eIndex];
            if (null == prev) throw new IllegalStateException(getPutExceptionLocation(key, obj) + " empty previous object at index=" + eIndex);
            o[eIndex] = obj;
            return prev;
        } else {
            final int nIndex = (-1) - eIndex, curSize = size();
            if ((nIndex >= numObjects) || (curSize >= numObjects)) {
                final int gSize = getGrowSize();
                if (gSize <= 0) throw new IllegalStateException(getPutExceptionLocation(key, obj) + " cannot grow by " + gSize + " for index=" + nIndex);
                grow(gSize);
                o = getObjects();
                numObjects = (null == o) ? 0 : o.length;
            }
            final int[] k = getKeys();
            final int numKeys = (null == k) ? 0 : k.length;
            if (nIndex < curSize) {
                if (numKeys != numObjects) throw new IllegalStateException(getPutExceptionLocation(key, obj) + " keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");
                for (int i = numKeys - 1; i > nIndex; i--) {
                    k[i] = k[i - 1];
                    o[i] = o[i - 1];
                }
                k[nIndex] = Integer.MAX_VALUE;
                o[nIndex] = null;
            } else if (nIndex != curSize) {
                throw new IllegalStateException(getPutExceptionLocation(key, obj) + " recommended location (" + nIndex + ") beyond end of array(s)=" + curSize);
            }
            k[nIndex] = key;
            o[nIndex] = obj;
            updateSize(1);
            return null;
        }
    }

    @Override
    public V put(final Integer key, final V value) throws IllegalArgumentException, IllegalStateException {
        if (null == key) throw new NullPointerException(getExceptionLocation("put") + "(" + value + ") null key not allowed");
        return put(key.intValue(), value);
    }

    /**
	 * Useful string for {@link #remove(int)} exceptions text
	 * @param key key value requested for removal
	 * @return {@link #getExceptionLocation(String)} + "(" + key + ")"
	 */
    protected String getRemoveExceptionLocation(final int key) {
        return getExceptionLocation("remove") + "(" + key + ")";
    }

    /**
	 * Removes specified key (if exists)
	 * @param key key to be removed - cannot be {@link Integer#MAX_VALUE}
	 * @return removed object - <I>null</I> if not in the map to begin with
	 * @throws IllegalArgumentException bad/illegal key
	 * @throws IllegalStateException null previous mapping, etc.
	 */
    public V remove(final int key) throws IllegalArgumentException, IllegalStateException {
        final int eIndex = findEntryIndex(key);
        if (eIndex < 0) return null;
        final int[] k = getKeys();
        final int numKeys = (null == k) ? 0 : k.length;
        final V[] o = getObjects();
        final int numObjects = (null == o) ? 0 : o.length;
        if (numKeys != numObjects) throw new IllegalStateException(getRemoveExceptionLocation(key) + " keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");
        final V prev = o[eIndex];
        if ((null == prev) || (Integer.MAX_VALUE == k[eIndex])) throw new IllegalStateException(getRemoveExceptionLocation(key) + " empty previous object at index=" + eIndex);
        for (int i = eIndex; i < (numKeys - 1); i++) {
            k[i] = k[i + 1];
            o[i] = o[i + 1];
        }
        k[numKeys - 1] = Integer.MAX_VALUE;
        o[numObjects - 1] = null;
        updateSize(-1);
        return prev;
    }

    @Override
    public V remove(Object key) {
        if (key instanceof Number) return remove(((Number) key).intValue());
        return null;
    }

    public Collection<V> remove(final int fromKey, final int toKey) {
        if (fromKey > toKey) throw new IllegalArgumentException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " inverted range");
        final int numItems = size();
        if (numItems <= 0) return null;
        final int[] ks = getKeys();
        if ((null == ks) || (ks.length < 1)) throw new IllegalStateException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " no keys values");
        Collection<V> vals = null;
        for (final int k : ks) {
            if (k >= toKey) break;
            if (k >= fromKey) {
                final V v = remove(k);
                if (v != null) {
                    if (null == vals) vals = new LinkedList<V>();
                    vals.add(v);
                }
            }
        }
        return vals;
    }

    /**
	 * Useful string for {@link #get(int)} exceptions text
	 * @param key key value requested for removal
	 * @return {@link #getExceptionLocation(String)} + "(" + key + ")"
	 */
    protected String getGetExceptionLocation(int key) {
        return getExceptionLocation("remove") + "(" + key + ")";
    }

    /**
	 * Checks if specified key exists in map
	 * @param key key to be checked - may NOT be {@link Integer#MAX_VALUE}
	 * @return found object (null if not found)
	 * @throws IllegalArgumentException if illegal key
	 * @throws IllegalStateException internal array(s) length(s) mismatches
	 */
    public V get(final int key) throws IllegalArgumentException, IllegalStateException {
        final int eIndex = findEntryIndex(key);
        if (eIndex < 0) return null;
        final V[] o = getObjects();
        final int numObjects = (null == o) ? 0 : o.length, curSize = size();
        if ((eIndex >= numObjects) || (eIndex >= curSize)) throw new IllegalStateException(getGetExceptionLocation(key) + " entry index (" + eIndex + ") beyond objects array length (" + numObjects + ") or current size (" + curSize + ")");
        final V res = o[eIndex];
        if (null == res) throw new IllegalStateException(getGetExceptionLocation(key) + " null object at index=" + eIndex);
        return res;
    }

    @Override
    public V get(final Object key) throws IllegalArgumentException, IllegalStateException {
        if (key instanceof Number) return get(((Number) key).intValue());
        return null;
    }

    public Set<Integer> keySet(final int fromKey, final int toKey) {
        final Collection<? extends Entry<Integer, V>> eSet = entrySet(fromKey, toKey);
        if ((null == eSet) || (eSet.size() <= 0)) return null;
        Set<Integer> ret = null;
        for (final Entry<Integer, V> e : eSet) {
            final Integer k = (null == e) ? null : e.getKey();
            if (null == k) continue;
            if (null == ret) ret = new TreeSet<Integer>(comparator());
            ret.add(k);
        }
        return ret;
    }

    @Override
    public Set<Integer> keySet() {
        return keySet(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return IntegersComparator.ASCENDING;
    }

    public Integer firstKey(final int fromKey, final int toKey) throws NoSuchElementException {
        final int numItems = size();
        if (numItems <= 0) throw new NoSuchElementException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " empty map");
        final int[] ks = getKeys();
        if ((null == ks) || (ks.length < 1)) throw new IllegalStateException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " no keys values");
        for (final int k : ks) {
            if (k >= toKey) break;
            if (k >= fromKey) {
                if (Integer.MAX_VALUE == k) throw new IllegalStateException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " marked empty spot key value");
                return Integer.valueOf(k);
            }
        }
        throw new NoSuchElementException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " no match found");
    }

    @Override
    public Integer firstKey() throws NoSuchElementException {
        return firstKey(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public Integer lastKey(final int fromKey, final int toKey) throws NoSuchElementException {
        final int numItems = size();
        if (numItems <= 0) throw new NoSuchElementException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " empty map");
        final int[] ks = getKeys();
        final int numKeys = (null == ks) ? 0 : ks.length;
        if (numKeys < numItems) throw new IllegalStateException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " no keys values");
        for (int kIndex = numKeys - 1; kIndex >= 0; kIndex--) {
            final int k = ks[kIndex];
            if (k < fromKey) break;
            if (k < toKey) {
                if (Integer.MAX_VALUE == k) throw new IllegalStateException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " marked empty spot key value");
                return Integer.valueOf(k);
            }
        }
        throw new NoSuchElementException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " no match found");
    }

    @Override
    public Integer lastKey() throws NoSuchElementException {
        return lastKey(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int countKeysInRange(final int fromKey, final int toKey) {
        if (fromKey > toKey) throw new IllegalArgumentException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " inverted range");
        final int numItems = size();
        if (numItems <= 0) return 0;
        final int[] k = getKeys();
        if ((null == k) || (k.length < numItems)) throw new IllegalStateException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " no keys values");
        int numKeys = 0;
        for (final int kv : k) {
            if (kv >= toKey) return numKeys;
            if (kv >= fromKey) numKeys++;
        }
        return numKeys;
    }

    @Override
    public int countKeysInRange(final Integer fromKey, final Integer toKey) {
        if ((null == fromKey) || (null == toKey)) throw new NullPointerException(getExceptionLocation("countKeysInRange") + " null from(" + fromKey + ")/to(" + toKey + ") key(s)");
        return countKeysInRange(fromKey.intValue(), toKey.intValue());
    }

    @Override
    public SortedMap<Integer, V> subMap(final Integer fromKey, final Integer toKey) {
        if ((null == fromKey) || (null == toKey)) throw new NullPointerException(getExceptionLocation("subMap") + " null from(" + fromKey + ")/to(" + toKey + ") key(s)");
        final int fkValue = fromKey.intValue(), tkValue = toKey.intValue();
        if (fkValue > tkValue) throw new IllegalArgumentException(getExceptionLocation("subMap") + " inverted range: [" + fromKey + " - " + toKey + "]");
        final int numItems = size();
        if (numItems <= 0) return null;
        final int[] ks = getKeys();
        if ((null == ks) || (ks.length < numItems)) throw new IllegalStateException(getExceptionLocation("subMap") + "[" + fromKey + " - " + toKey + "] mismatched keys array size");
        final V[] vs = getObjects();
        if ((null == vs) || (vs.length < numItems)) throw new IllegalStateException(getExceptionLocation("subMap") + "[" + fromKey + " - " + toKey + "] mismatched objects array size");
        IntegersMap<V> res = null;
        for (int eIndex = 0; eIndex < numItems; eIndex++) {
            final int key = ks[eIndex];
            if (key < fkValue) continue;
            if (key > tkValue) break;
            if (null == res) res = new IntegersMap<V>(getValuesClass(), numItems, getGrowSize());
            res.put(key, vs[eIndex]);
        }
        return res;
    }

    public static final Integer MIN_KEY = Integer.valueOf(Integer.MIN_VALUE), MAX_KEY = Integer.valueOf(Integer.MAX_VALUE - 1), TAIL_KEY = Integer.valueOf(MAX_KEY.intValue() + 1);

    @Override
    public SortedMap<Integer, V> headMap(final Integer toKey) {
        return subMap(MIN_KEY, toKey);
    }

    @Override
    public SortedMap<Integer, V> tailMap(final Integer fromKey) {
        return subMap(fromKey, TAIL_KEY);
    }

    @Override
    @CoVariantReturn
    public IntegersMap<V> clone() throws CloneNotSupportedException {
        @SuppressWarnings("unchecked") final IntegersMap<V> cm = (IntegersMap<V>) super.clone();
        final int[] ck = cm.getKeys();
        if (ck != null) {
            cm._keyVals = new int[ck.length];
            System.arraycopy(ck, 0, cm._keyVals, 0, ck.length);
        }
        final V[] cv = cm.getObjects();
        if (cv != null) {
            final V[] newVals = allocateValuesArray(cv.length);
            System.arraycopy(cv, 0, newVals, 0, cv.length);
            cm.setObjects(newVals);
        }
        return cm;
    }
}
