package org.ozoneDB.core.storage.gammaStore;

import java.io.Serializable;

/**
 * <p>Provides a primitive mapping-like meganism in which there is only a key (a
 * primitive <code>long</code> in this case). This key can be put in and
 * retrieved by <code>getKey(long)</code> and <code>putKey(long)</code>. Both
 * methods return a handle, which can be used as a handle or magic cookie, to
 * find the key. Implementing classes can and should provide their own get and
 * put methods for each and every object or primitive that is mapped to the key.
 * </p>
 * <p>Extending classes should extend <code>move(int, int)</code></p>
 *
 * <p>This implementation is very fast when every value passed to <code>putKey(int)</code>
 * is bigger than all previous passed values (O(1)). If not, it is quite slow
 * (max O(size)). Because this class is used to store object ids
 * this means the greatest performance benefits occur in normal 'everyday' use.
 * Only when the database or OS has crashed (powerfailure) and the index has to
 * be rebuild does the speed disadvantage make itself known.</p>
 *
 * @author <a href="mailto:leoATmekenkampD0Tcom">Leo Mekenkamp (mind the anti sp@m)</a>
 * @version $Id: Loc.java,v 1.4 2005/01/05 16:58:27 leomekenkamp Exp $
 */
public class Loc implements Serializable {

    private static final long serialVersionUID = 1L;

    protected long[] keys;

    private int[] inUse;

    private int capacity;

    private int size;

    private int handleToLast;

    public Loc(int capacity, int slack) {
        this.capacity = capacity;
        keys = new long[capacity + slack];
        int inUseSize = keys.length / 32;
        if (keys.length % 32 != 0) {
            inUseSize++;
        }
        inUse = new int[inUseSize];
        handleToLast = -1;
    }

    public Loc(int capacity, float relSlack) {
        this(capacity, (int) (relSlack * capacity) > 0 ? (int) (relSlack * capacity) : 1);
    }

    /**
     * Returns a handle to the given value. If there is no such value, then
     * the return value is negative. The returned handle is only valid until
     * the next call to <code>putKey(long)</code> or <code>compress()</code>.
     */
    public int getKeyPos(long key) {
        int result = search(key);
        if (!isInUse(result) || key != keys[result]) {
            result = -1;
        }
        return result;
    }

    /** Returns the key found at the specified location.
     * @param pos position to get the key from
     * @return key at that location
     * @throws IllegalArgumentException the specified position is not in use
     */
    public long getKey(int pos) {
        if (!isInUse(pos)) {
            throw new IllegalArgumentException("position not in use");
        }
        return keys[pos];
    }

    /**
     * Returns a handle to the given key, or, if that key does not exist, to
     * the smallest key larger than specified key. If there is no such value,
     * then the return value is negative. The returned handle is only valid
     * until the next call to <code>putKey(long)</code> or <code>compress()</code>.
     */
    public int getKeyPosOrNearestGreater(long key) {
        int result = -1;
        for (int handle = search(key); handle <= handleToLast; handle++) {
            if (isInUse(handle) && keys[handle] >= key) {
                result = handle;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a handle to the given key, or, if that key does not exist, to
     * the largest key smaller than specified key. If there is no such value,
     * then the return value is negative. The returned handle is only valid
     * until the next call to <code>putKey(long)</code> or <code>compress()</code>.
     */
    public int getKeyPosOrNearestSmaller(long key) {
        int result = -1;
        for (int handle = search(key); handle >= 0; handle--) {
            if (isInUse(handle) && keys[handle] <= key) {
                result = handle;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a handle to the given value and removes that key. If there is no
     * such key, then the return value is negative. The returned handle is only
     * valid until the next call to <code>putKey(long)</code> or <code>compress()</code>.
     * The returned handle can also never be retrieved again.
     */
    public int removeKey(long key) {
        int result = getKeyPos(key);
        if (result < 0) {
            return -1;
        }
        removePos(result);
        return result;
    }

    /**
     * <p>Inserts (or overwrites) a key and returns a handle to it. The returned
     * handle is only valid until the next call to <code>putKey(long)</code> or
     * <code>compress()</code>.</p>
     * <p>This method is highly optimized for keys that are bigger than any
     * other key in this instance; performance is then O(1). When this is not
     * the case, thus the keys are inserted in random fashion (2,7,4,8,9,1 
     * instead of 1,2,4,7,8,9) performance can be up to O(size)</p>
     *
     * @throws IndexOutOfBoundsException maximum size has already been reached
     */
    public int putKey(long key) {
        if (handleToLast == -1 || keys[handleToLast] < key) {
            if (handleToLast == keys.length - 1) {
                compress();
            }
            if (size >= capacity) {
                throw new IndexOutOfBoundsException("cannot grow any larger");
            }
            handleToLast++;
            keys[handleToLast] = key;
            setInUse(handleToLast, true);
            size++;
            return handleToLast;
        }
        compress();
        int result = getKeyPosOrNearestGreater(key);
        if (result >= 0) {
            if (isInUse(result) && keys[result] == key) {
                return result;
            }
        } else {
            result = handleToLast >= 0 ? handleToLast : 0;
        }
        if (size >= capacity) {
            throw new IndexOutOfBoundsException("cannot grow any larger (size = " + size + ", capacity = " + capacity);
        }
        for (int i = handleToLast; i >= result; i--) {
            move(i, i + 1);
        }
        handleToLast++;
        keys[result] = key;
        setInUse(result, true);
        size++;
        return result;
    }

    /**
     * Returns the handle to the key that is the smallest of all larger keys.
     * Returns < 0 if there is no such key. Note that if handle <0 then the
     * first handle in use is returned (which may of course be < 0 if there is
     * no key in this instance
     */
    public int next(int handle) {
        if (handle >= 0 && !isInUse(handle)) {
            throw new IllegalArgumentException("handle is not in use");
        }
        handle++;
        int result;
        for (result = -1; result < 0 && handle <= handleToLast; handle++) {
            if (isInUse(handle)) {
                result = handle;
            }
        }
        return result;
    }

    /**
     * Returns true iff the given handle is in use
     */
    protected final boolean isInUse(int handle) {
        if (handle < 0 || handle >= keys.length) {
            throw new IllegalArgumentException("handle should be >= 0 and < " + keys.length + ", is " + handle);
        }
        int pos = handle / 32;
        int mask = 1 << (handle % 32);
        return (inUse[pos] & mask) != 0;
    }

    /**
     * Returns the previous value.
     */
    private boolean setInUse(int handle, boolean inUse) {
        int pos = handle / 32;
        int mask = 1 << (handle % 32);
        boolean result = (this.inUse[pos] | mask) != 0;
        if (inUse) {
            this.inUse[pos] |= mask;
        } else {
            this.inUse[pos] &= ~mask;
        }
        return result;
    }

    /**
     * Deletes the entry at given position.
     * @throws IllegalArgumentException if already invalidated/removed
     */
    public void removePos(int handle) {
        if (!setInUse(handle, false)) {
            throw new IllegalArgumentException("pos " + handle + " has already been removed");
        }
        size--;
        while (handleToLast >= 0 && !isInUse(handleToLast)) {
            handleToLast--;
        }
    }

    /**
     * Searches for the given value. If the value could not be found then one of
     * the handles of the nearest value is returned. Note that the returned
     * handle may not be in use.
     */
    private int search(long key) {
        return (handleToLast < 0) ? 0 : search(key, 0, handleToLast);
    }

    /**
     * note: searching beyond high > index is NOT supported and may yield
     * strange results. Search may return a handle to either 1) the specified
     * key, or if that key does not exist: 2) the greatest key smaller
     * than the specified key; or: 3) the smallest key greater than
     * the specified key. Returned handle may be invalid!
     */
    private int search(long key, int low, int high) {
        if (low > high) {
            throw new IllegalArgumentException("low should be <= high; low == " + low + ", high == " + high + ", key == " + key + ", handleToLast == " + handleToLast);
        }
        for (; ; ) {
            int result = (low + high) / 2;
            if (low >= high || keys[result] == key) {
                return result;
            }
            if (key < keys[result]) {
                high = result - 1;
            } else {
                low = result + 1;
            }
        }
    }

    /**
     * Moves all keys to remove unused (deleted) slots.
     */
    private void compress() {
        handleToLast = -1;
        for (int i = 0; i < keys.length; i++) {
            if (isInUse(i)) {
                handleToLast++;
                if (i > handleToLast) {
                    move(i, handleToLast);
                }
            } else {
                keys[i] = 0;
            }
        }
    }

    /**
     * Extending classes _must_ implement their own move() and call this one
     * as well.
     */
    protected void move(int handleFrom, int handleTo) {
        if (!isInUse(handleFrom)) {
            throw new IllegalArgumentException("handleFrom (" + handleFrom + ") must be in use");
        }
        if (isInUse(handleTo)) {
            throw new IllegalArgumentException("handleTo (" + handleTo + ") must not be in use");
        }
        keys[handleTo] = keys[handleFrom];
        keys[handleFrom] = 0;
        setInUse(handleFrom, false);
        setInUse(handleTo, true);
    }

    public String toString() {
        StringBuffer result = new StringBuffer("handleToLast=");
        result.append(handleToLast).append("; [");
        for (int i = 0; i < keys.length; i++) {
            result.append(keys[i]);
            if (!isInUse(i)) {
                result.append("D");
            }
            if (i < keys.length - 1) {
                result.append(", ");
            } else {
                result.append("]");
            }
        }
        return result.toString();
    }

    /**
     * Returns the number of keys in use.
     */
    public int size() {
        return size;
    }

    public long getMinKey() {
        return keys[getMinPos()];
    }

    public long getMaxKey() {
        return keys[getMaxPos()];
    }

    public int getMinPos() {
        for (int i = 0; i < keys.length; i++) {
            if (isInUse(i)) {
                return i;
            }
        }
        throw new RuntimeException("may not call getMinKey() when empty");
    }

    public int getMaxPos() {
        for (int i = handleToLast; i >= 0; i--) {
            if (isInUse(i)) {
                return i;
            }
        }
        throw new RuntimeException("may not call getMaxKey() when empty");
    }
}
