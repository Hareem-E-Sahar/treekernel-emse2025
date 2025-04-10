package org.luaj.vm;

import java.util.Vector;
import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;

/**
 * Simple implementation of table structure for Lua VM. Maintains both an array
 * part and a hash part. Does not attempt to achieve the same performance as the
 * C version.
 * 
 * Java code can put values in the table or get values out (bypassing the
 * metatable, if there is one) using put() and get(). There are specializations
 * of put() and get() for integers and Strings to avoid allocating wrapper
 * objects when possible.
 * 
 * remove() methods are private: setting a key's value to nil is the correct way
 * to remove an entry from the table.
 * 
 * 
 */
public class LTable extends LValue {

    protected Object[] array;

    protected LValue[] hashKeys;

    protected Object[] hashValues;

    private int hashEntries;

    private LTable m_metatable;

    private static final int MIN_HASH_CAPACITY = 2;

    private static final LValue[] NONE = {};

    /** Construct an empty LTable with no initial capacity. */
    public LTable() {
        array = NONE;
        hashKeys = NONE;
    }

    /**
	 * Construct an empty LTable that is expected to contain entries with keys
	 * in the range 1 .. narray and nhash non-integer keys.
	 */
    public LTable(int narray, int nhash) {
        if (nhash > 0 && nhash < MIN_HASH_CAPACITY) nhash = MIN_HASH_CAPACITY;
        array = new Object[narray];
        hashKeys = new LValue[nhash];
        hashValues = new Object[nhash];
    }

    public boolean isTable() {
        return true;
    }

    /** Get capacity of hash part */
    public int getArrayCapacity() {
        return array.length;
    }

    /** Get capacity of hash part */
    public int getHashCapacity() {
        return hashKeys.length;
    }

    /**
	 * Return total number of keys mapped to non-nil values. Not to be confused
	 * with luaLength, which returns some number n such that the value at n+1 is
	 * nil.
	 * 
	 * @deprecated this is not scalable.  Does a linear search through the table.  Use luaLength() instead.  
	 */
    public int size() {
        int count = 0;
        for (int i = array.length; --i >= 0; ) if (array[i] != null) count++;
        for (int i = hashKeys.length; --i >= 0; ) if (hashKeys[i] != null) count++;
        return count;
    }

    /**
	 * Generic put method for all types of keys, but does not use the metatable.
	 */
    public void put(LValue key, LValue val) {
        if (key.isInteger()) {
            int pos = key.toJavaInt() - 1;
            int n = array.length;
            if (pos >= 0 && pos <= n) {
                if (pos == n) expandArrayPart();
                array[pos] = normalizePut(val);
                return;
            }
        }
        hashSet(key, normalizePut(val));
    }

    /**
	 * Method for putting an integer-keyed value. Bypasses the metatable, if
	 * any.
	 */
    public void put(int key, LValue val) {
        int pos = key - 1;
        int n = array.length;
        if (pos >= 0 && pos <= n) {
            if (pos == n) expandArrayPart();
            array[pos] = normalizePut(val);
        } else {
            hashSet(LInteger.valueOf(key), normalizePut(val));
        }
    }

    /**
	 * Utility method for putting a string-keyed value directly, typically for
	 * initializing a table. Bypasses the metatable, if any.
	 */
    public void put(String key, LValue val) {
        hashSet(LString.valueOf(key), normalizePut(val));
    }

    /**
	 * Utility method for putting a string key, int value directly, typically for
	 * initializing a table. Bypasses the metatable, if any.
	 */
    public void put(String key, int val) {
        hashSet(LString.valueOf(key), LInteger.valueOf(val));
    }

    /** 
	 * Expand the array part of the backing for more values to fit in.
	 */
    private void expandArrayPart() {
        int n = array.length;
        int m = Math.max(2, n * 2);
        arrayExpand(m);
        for (int i = n; i < m; i++) {
            LInteger k = LInteger.valueOf(i + 1);
            Object v = hashGet(k);
            if (v != null) {
                hashSet(k, null);
                array[i] = v;
            }
        }
    }

    /** Check for nil, and convert to null or leave alone
	 */
    protected Object normalizePut(LValue val) {
        return val == LNil.NIL ? null : val;
    }

    /**
	 * Utility method to directly get the value in a table, without metatable
	 * calls. Must never return null, use LNil.NIL instead.
	 */
    public LValue get(LValue key) {
        if (key.isInteger()) {
            int ikey = key.toJavaInt();
            if (ikey > 0 && ikey <= array.length) return normalizeGet(array[ikey - 1]);
        }
        return normalizeGet(hashGet(key));
    }

    /** Utility method for retrieving an integer-keyed value */
    public LValue get(int key) {
        return normalizeGet(key > 0 && key <= array.length ? array[key - 1] : hashGet(LInteger.valueOf(key)));
    }

    /** Check for null, and convert to nilor leave alone
	 */
    protected LValue normalizeGet(Object val) {
        return val == null ? LNil.NIL : (LValue) val;
    }

    /**
	 * Return true if the table contains an entry with the given key, 
	 * false if not. Ignores the metatable.
	 */
    public boolean containsKey(LValue key) {
        if (key.isInteger()) {
            int ikey = key.toJavaInt();
            if (ikey > 0 && ikey <= array.length) return null != array[ikey - 1];
        }
        return null != hashGet(key);
    }

    /**
	 * Return true if the table contains an entry with the given integer-valued key, 
	 * false if not. Ignores the metatable.
	 */
    public boolean containsKey(int key) {
        return (key > 0 && key <= array.length ? array[key - 1] != null : (hashKeys.length > 0 && hashKeys[hashFindSlot(LInteger.valueOf(key))] != null));
    }

    private static final int MAX_KEY = 0x3fffffff;

    /**
	* Try to find a boundary in table `t'. A `boundary' is an integer index
	* such that t[i] is non-nil and t[i+1] is nil (and 0 if t[1] is nil).
	*/
    public int luaLength() {
        int i = 0;
        int j = array.length;
        if (j <= 0 || containsKey(j)) {
            if (hashKeys.length == 0) return j;
            for (++j; containsKey(j) && j < MAX_KEY; j *= 2) i = j;
        }
        while (j - i > 1) {
            int m = (i + j) / 2;
            if (!containsKey(m)) j = m; else i = m;
        }
        return i;
    }

    /** Valid for tables */
    public LTable luaGetMetatable() {
        return this.m_metatable;
    }

    /** Valid for tables */
    public LTable luaSetMetatable(LValue metatable) {
        if (m_metatable != null && m_metatable.containsKey(TM_METATABLE)) throw new LuaErrorException("cannot change a protected metatable");
        if (metatable == null || metatable.isNil()) this.m_metatable = null; else if (metatable.luaGetType() == Lua.LUA_TTABLE) {
            org.luaj.vm.LTable t = (org.luaj.vm.LTable) metatable;
            LValue m = t.get(TM_MODE);
            if (m.isString() && m.toJavaString().indexOf('v') >= 0) {
                LTable w = new LWeakTable(this);
                w.m_metatable = t;
                return w;
            }
            this.m_metatable = t;
        } else {
            throw new LuaErrorException("not a table: " + metatable.luaGetTypeName());
        }
        return this;
    }

    public String toJavaString() {
        return "table: " + id();
    }

    public int luaGetType() {
        return Lua.LUA_TTABLE;
    }

    /**
	 * Helper method to get all the keys in this table in an array. Meant to be
	 * used instead of keys() (which returns an enumeration) when an array is
	 * more convenient. Note that for a very large table, getting an Enumeration
	 * instead would be more space efficient.
	 * 
	 * @deprecated this is not scalable.  Does a linear search through the table.  
	 */
    public LValue[] getKeys() {
        int n = array.length;
        int o = hashKeys.length;
        LValue k;
        Vector v = new Vector();
        for (int pos = 0; pos < n; ++pos) {
            if (array[pos] != null) v.addElement(LInteger.valueOf(pos + 1));
        }
        for (int pos = 0; pos < o; pos++) {
            if (null != (k = hashKeys[pos])) v.addElement(k);
        }
        LValue[] keys = new LValue[v.size()];
        v.copyInto(keys);
        return keys;
    }

    /**
	 * Insert element at a position in the list, shifting contiguous elements up.
	 * @pos index to insert at, or 0 to insert at end.
	 */
    public void luaInsertPos(int ikey, LValue value) {
        if (ikey == 0) ikey = luaLength() + 1;
        do {
            LValue tmp = get(ikey);
            put(ikey++, value);
            value = tmp;
        } while (!value.isNil());
    }

    /**
	 * Remove an element from the list, moving contiguous elements down
	 * @param pos position to remove, or 0 to remove last element
	 */
    public LValue luaRemovePos(int ikey) {
        int n = luaLength();
        if (ikey == 0) ikey = n;
        if (ikey <= 0 || ikey > n) return LNil.NIL;
        LValue removed = get(ikey);
        LValue replaced;
        do {
            put(ikey, replaced = get(ikey + 1));
            ikey++;
        } while (!replaced.isNil());
        return removed;
    }

    /**
	 * Returns the largest positive numerical index of the given table, 
	 * or zero if the table has no positive numerical indices. 
	 * (To do its job this function does a linear traversal of the whole table.)
	 * @return LValue that is the largest int 
	 */
    public LValue luaMaxN() {
        int n = array.length;
        int m = hashKeys.length;
        int r = Integer.MIN_VALUE;
        for (int i = n; --i >= 0; ) {
            if (array[i] != null) {
                r = i + 1;
                break;
            }
        }
        for (int i = 0; i < m; i++) {
            LValue key = hashKeys[i];
            if (key != null && key.isInteger()) {
                int k = key.toJavaInt();
                if (k > r) r = k;
            }
        }
        return LInteger.valueOf(r == Integer.MIN_VALUE ? 0 : r);
    }

    public void luaSort(LuaState vm, LValue compare) {
        heapSort(luaLength(), vm, compare);
    }

    private void heapSort(int count, LuaState vm, LValue cmpfunc) {
        heapify(count, vm, cmpfunc);
        for (int end = count - 1; end > 0; ) {
            swap(end, 0);
            siftDown(0, --end, vm, cmpfunc);
        }
    }

    private void heapify(int count, LuaState vm, LValue cmpfunc) {
        for (int start = count / 2 - 1; start >= 0; --start) siftDown(start, count - 1, vm, cmpfunc);
    }

    private void siftDown(int start, int end, LuaState vm, LValue cmpfunc) {
        for (int root = start; root * 2 + 1 <= end; ) {
            int child = root * 2 + 1;
            if (child < end && compare(child, child + 1, vm, cmpfunc)) ++child;
            if (compare(root, child, vm, cmpfunc)) {
                swap(root, child);
                root = child;
            } else return;
        }
    }

    private boolean compare(int i, int j, LuaState vm, LValue cmpfunc) {
        LValue a = get(i + 1);
        LValue b = get(j + 1);
        if (a.isNil() || b.isNil()) return false;
        if (!cmpfunc.isNil()) {
            vm.pushlvalue(cmpfunc);
            vm.pushlvalue(a);
            vm.pushlvalue(b);
            vm.call(2, 1);
            boolean result = vm.toboolean(-1);
            vm.resettop();
            return result;
        } else {
            return b.luaBinCmpUnknown(Lua.OP_LT, a);
        }
    }

    private void swap(int i, int j) {
        LValue a = get(i + 1);
        put(i + 1, get(j + 1));
        put(j + 1, a);
    }

    /**
	 * Leave key,value pair on top, or nil if at end of list.
	 * @param vm the LuaState to leave the values on
	 * @param indexedonly TODO
	 * @param index index to start search
	 * @return true if next exists, false if at end of list
	 */
    public boolean next(LuaState vm, LValue key, boolean indexedonly) {
        int n = array.length;
        int m = (indexedonly ? -1 : hashKeys.length);
        int i = findindex(key, n, m);
        if (i < 0) vm.error("invalid key to 'next'");
        for (; i < n; ++i) {
            Object a = array[i];
            if (a != null) {
                vm.pushinteger(i + 1);
                vm.pushlvalue(normalizeGet(a));
                return true;
            } else if (indexedonly) {
                vm.pushnil();
                return false;
            }
        }
        if ((!indexedonly)) {
            for (i -= n; i < m; ++i) {
                Object v = hashValues[i];
                if (v != null) {
                    LValue k = hashKeys[i];
                    vm.pushlvalue(k);
                    vm.pushlvalue(normalizeGet(v));
                    return true;
                }
            }
        }
        vm.pushnil();
        return false;
    }

    private int findindex(LValue key, int n, int m) {
        if (key.isNil()) return 0;
        if (key.isInteger()) {
            int i = key.toJavaInt();
            if ((0 < i) && (i <= n)) {
                if (array[i - 1] == null) return -1;
                return i;
            }
        }
        if (m < 0) return n;
        if (m == 0) return -1;
        int slot = hashFindSlot(key);
        if (hashKeys[slot] == null) return -1;
        return n + slot + 1;
    }

    /**
	 * Executes the given f over all elements of table. For each element, f is
	 * called with the index and respective value as arguments. If f returns a
	 * non-nil value, then the loop is broken, and this value is returned as the
	 * final value of foreach.
	 * 
	 * @param vm
	 * @param function
	 * @param indexedonly is a table.foreachi() call, not a table.foreach() call
	 * @return
	 */
    public LValue foreach(LuaState vm, LFunction function, boolean indexedonly) {
        LValue key = LNil.NIL;
        while (true) {
            vm.resettop();
            vm.pushlvalue(function);
            if (!next(vm, key, indexedonly)) return LNil.NIL;
            key = vm.topointer(2);
            vm.call(2, 1);
            if (!vm.isnil(-1)) return vm.poplvalue();
        }
    }

    public void arrayExpand(int newLength) {
        Object[] v = new Object[newLength];
        System.arraycopy(array, 0, v, 0, array.length);
        array = v;
    }

    public void arrayPresize(int minSize) {
        if (array.length < minSize) arrayExpand(minSize);
    }

    public void hashSet(LValue key, Object value) {
        if (value == null) hashRemove(key); else {
            if (checkLoadFactor()) rehash();
            int slot = hashFindSlot(key);
            if (hashFillSlot(slot, value)) return;
            hashKeys[slot] = key;
            hashValues[slot] = value;
        }
    }

    public Object hashGet(LValue key) {
        if (hashKeys.length <= 0) return null;
        return hashValues[hashFindSlot(key)];
    }

    public int hashFindSlot(LValue key) {
        int i = (key.hashCode() & 0x7FFFFFFF) % hashKeys.length;
        LValue k;
        while ((k = hashKeys[i]) != null && !k.luaBinCmpUnknown(Lua.OP_EQ, key)) {
            i = (i + 1) % hashKeys.length;
        }
        return i;
    }

    private boolean hashFillSlot(int slot, Object value) {
        hashValues[slot] = value;
        if (hashKeys[slot] != null) {
            return true;
        } else {
            ++hashEntries;
            return false;
        }
    }

    protected void hashRemove(LValue key) {
        if (hashKeys.length > 0) {
            int slot = hashFindSlot(key);
            hashClearSlot(slot);
        }
    }

    protected void hashClearSlot(int i) {
        if (hashKeys[i] != null) {
            int j = i;
            int n = hashKeys.length;
            while (hashKeys[j = ((j + 1) % n)] != null) {
                final int k = ((hashKeys[j].hashCode()) & 0x7FFFFFFF) % n;
                if ((j > i && (k <= i || k > j)) || (j < i && (k <= i && k > j))) {
                    hashKeys[i] = hashKeys[j];
                    hashValues[i] = hashValues[j];
                    i = j;
                }
            }
            --hashEntries;
            hashKeys[i] = null;
            hashValues[i] = null;
            if (hashEntries == 0) {
                hashKeys = NONE;
                hashValues = null;
            }
        }
    }

    protected boolean checkLoadFactor() {
        final int hashCapacity = hashKeys.length;
        return (hashCapacity >> 1) >= (hashCapacity - hashEntries);
    }

    protected void rehash() {
        final int oldCapacity = hashKeys.length;
        final int newCapacity = (oldCapacity > 0) ? 2 * oldCapacity : MIN_HASH_CAPACITY;
        final LValue[] oldKeys = hashKeys;
        final Object[] oldValues = hashValues;
        hashKeys = new LValue[newCapacity];
        hashValues = new Object[newCapacity];
        for (int i = 0; i < oldCapacity; ++i) {
            final LValue k = oldKeys[i];
            if (k != null) {
                final Object v = oldValues[i];
                final int slot = hashFindSlot(k);
                hashKeys[slot] = k;
                hashValues[slot] = v;
            }
        }
    }
}
