package jdbm.btree;

import jdbm.helper.Serializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Page of a Btree.
 * <p>
 * The page contains a number of key-value pairs.  Keys are ordered to allow
 * dichotomic search.
 * <p>
 * If the page is a leaf page, the keys and values are user-defined and
 * represent entries inserted by the user.
 * <p>
 * If the page is non-leaf, each key represents the greatest key in the
 * underlying BPages and the values are recids pointing to the children BPages.
 * The only exception is the rightmost BPage, which is considered to have an
 * "infinite" key value, meaning that any insert will be to the left of this
 * pseudo-key
 *
 * @author <a href="mailto:boisvert@intalio.com">Alex Boisvert</a>
 * @version $Id: BPage.java,v 1.6 2003/09/21 15:46:59 boisvert Exp $
 */
public final class BPage implements Serializer {

    private static final boolean DEBUG = false;

    /**
     * Version id for serialization.
     */
    static final long serialVersionUID = 1L;

    /**
     * Parent B+Tree.
     */
    transient BTree _btree;

    /**
     * This BPage's record ID in the PageManager.
     */
    protected transient long _recid;

    /**
     * Flag indicating if this is a leaf BPage.
     */
    protected boolean _isLeaf;

    /**
     * Keys of children nodes
     */
    protected Object[] _keys;

    /**
     * Values associated with keys.  (Only valid if leaf BPage)
     */
    protected Object[] _values;

    /**
     * Children pages (recids) associated with keys.  (Only valid if non-leaf BPage)
     */
    protected long[] _children;

    /**
     * Index of first used item at the page
     */
    protected int _first;

    /**
     * Previous leaf BPage (only if this BPage is a leaf)
     */
    protected long _previous;

    /**
     * Next leaf BPage (only if this BPage is a leaf)
     */
    protected long _next;

    /**
     * No-argument constructor used by serialization.
     */
    public BPage() {
    }

    /**
     * Root page overflow constructor
     */
    BPage(BTree btree, BPage root, BPage overflow) throws IOException {
        _btree = btree;
        _isLeaf = false;
        _first = _btree._pageSize - 2;
        _keys = new Object[_btree._pageSize];
        _keys[_btree._pageSize - 2] = overflow.getLargestKey();
        _keys[_btree._pageSize - 1] = root.getLargestKey();
        _children = new long[_btree._pageSize];
        _children[_btree._pageSize - 2] = overflow._recid;
        _children[_btree._pageSize - 1] = root._recid;
        _recid = _btree._recman.insert(this, this);
    }

    /**
     * Root page (first insert) constructor.
     */
    BPage(BTree btree, Object key, Object value) throws IOException {
        _btree = btree;
        _isLeaf = true;
        _first = btree._pageSize - 2;
        _keys = new Object[_btree._pageSize];
        _keys[_btree._pageSize - 2] = key;
        _keys[_btree._pageSize - 1] = null;
        _values = new Object[_btree._pageSize];
        _values[_btree._pageSize - 2] = value;
        _values[_btree._pageSize - 1] = null;
        _recid = _btree._recman.insert(this, this);
    }

    /**
     * Overflow page constructor.  Creates an empty BPage.
     */
    BPage(BTree btree, boolean isLeaf) throws IOException {
        _btree = btree;
        _isLeaf = isLeaf;
        _first = _btree._pageSize / 2;
        _keys = new Object[_btree._pageSize];
        if (isLeaf) {
            _values = new Object[_btree._pageSize];
        } else {
            _children = new long[_btree._pageSize];
        }
        _recid = _btree._recman.insert(this, this);
    }

    /**
     * Get largest key under this BPage.  Null is considered to be the
     * greatest possible key.
     */
    Object getLargestKey() {
        return _keys[_btree._pageSize - 1];
    }

    /**
     * Return true if BPage is empty.
     */
    boolean isEmpty() {
        if (_isLeaf) {
            return (_first == _values.length - 1);
        } else {
            return (_first == _children.length - 1);
        }
    }

    /**
     * Return true if BPage is full.
     */
    boolean isFull() {
        return (_first == 0);
    }

    /**
     * Find the object associated with the given key.
     *
     * @param height Height of the current BPage (zero is leaf page)
     * @param key The key
     * @return TupleBrowser positionned just before the given key, or before
     *                      next greater key if key isn't found.
     */
    TupleBrowser find(int height, Object key) throws IOException {
        int index = findChildren(key);
        height -= 1;
        if (height == 0) {
            return new Browser(this, index);
        } else {
            BPage child = childBPage(index);
            return child.find(height, key);
        }
    }

    /**
     * Find first entry and return a browser positioned before it.
     *
     * @return TupleBrowser positionned just before the first entry.
     */
    TupleBrowser findFirst() throws IOException {
        if (_isLeaf) {
            return new Browser(this, _first);
        } else {
            BPage child = childBPage(_first);
            return child.findFirst();
        }
    }

    /**
     * Insert the given key and value.
     * <p>
     * Since the Btree does not support duplicate entries, the caller must
     * specify whether to replace the existing value.
     *
     * @param height Height of the current BPage (zero is leaf page)
     * @param key Insert key
     * @param value Insert value
     * @param replace Set to true to replace the existing value, if one exists.
     * @return Insertion result containing existing value OR a BPage if the key
     *         was inserted and provoked a BPage overflow.
     */
    InsertResult insert(int height, Object key, Object value, boolean replace) throws IOException {
        InsertResult result;
        long overflow;
        int index = findChildren(key);
        height -= 1;
        if (height == 0) {
            result = new InsertResult();
            overflow = -1;
            if (DEBUG) {
                System.out.println("Bpage.insert() Insert on leaf Bpage key=" + key + " value=" + value + " index=" + index);
            }
            if (compare(key, _keys[index]) == 0) {
                if (DEBUG) {
                    System.out.println("Bpage.insert() Key already exists.");
                }
                result._existing = _values[index];
                if (replace) {
                    _values[index] = value;
                    _btree._recman.update(_recid, this, this);
                }
                return result;
            }
        } else {
            BPage child = childBPage(index);
            result = child.insert(height, key, value, replace);
            if (result._existing != null) {
                return result;
            }
            if (result._overflow == null) {
                return result;
            }
            if (DEBUG) {
                System.out.println("BPage.insert() Overflow page: " + result._overflow._recid);
            }
            key = result._overflow.getLargestKey();
            overflow = result._overflow._recid;
            _keys[index] = child.getLargestKey();
            result._overflow = null;
        }
        if (!isFull()) {
            if (height == 0) {
                insertEntry(this, index - 1, key, value);
            } else {
                insertChild(this, index - 1, key, overflow);
            }
            _btree._recman.update(_recid, this, this);
            return result;
        }
        int half = _btree._pageSize >> 1;
        BPage newPage = new BPage(_btree, _isLeaf);
        if (index < half) {
            if (DEBUG) {
                System.out.println("Bpage.insert() move lower-half of entries to overflow BPage, including new entry.");
            }
            if (height == 0) {
                copyEntries(this, 0, newPage, half, index);
                setEntry(newPage, half + index, key, value);
                copyEntries(this, index, newPage, half + index + 1, half - index - 1);
            } else {
                copyChildren(this, 0, newPage, half, index);
                setChild(newPage, half + index, key, overflow);
                copyChildren(this, index, newPage, half + index + 1, half - index - 1);
            }
        } else {
            if (DEBUG) {
                System.out.println("Bpage.insert() move lower-half of entries to overflow BPage. New entry stays");
            }
            if (height == 0) {
                copyEntries(this, 0, newPage, half, half);
                copyEntries(this, half, this, half - 1, index - half);
                setEntry(this, index - 1, key, value);
            } else {
                copyChildren(this, 0, newPage, half, half);
                copyChildren(this, half, this, half - 1, index - half);
                setChild(this, index - 1, key, overflow);
            }
        }
        _first = half - 1;
        for (int i = 0; i < _first; i++) {
            if (height == 0) {
                setEntry(this, i, null, null);
            } else {
                setChild(this, i, null, -1);
            }
        }
        if (_isLeaf) {
            newPage._previous = _previous;
            newPage._next = _recid;
            if (_previous != 0) {
                BPage previous = loadBPage(_previous);
                previous._next = newPage._recid;
                _btree._recman.update(_previous, previous, this);
            }
            _previous = newPage._recid;
        }
        _btree._recman.update(_recid, this, this);
        _btree._recman.update(newPage._recid, newPage, this);
        result._overflow = newPage;
        return result;
    }

    /**
     * Remove the entry associated with the given key.
     *
     * @param height Height of the current BPage (zero is leaf page)
     * @param key Removal key
     * @return Remove result object
     */
    RemoveResult remove(int height, Object key) throws IOException {
        RemoveResult result;
        int half = _btree._pageSize / 2;
        int index = findChildren(key);
        height -= 1;
        if (height == 0) {
            if (compare(_keys[index], key) != 0) {
                throw new IllegalArgumentException("Key not found: " + key);
            }
            result = new RemoveResult();
            result._value = _values[index];
            removeEntry(this, index);
            _btree._recman.update(_recid, this, this);
        } else {
            BPage child = childBPage(index);
            result = child.remove(height, key);
            _keys[index] = child.getLargestKey();
            _btree._recman.update(_recid, this, this);
            if (result._underflow) {
                if (child._first != half + 1) {
                    throw new IllegalStateException("Error during underflow [1]");
                }
                if (index < _children.length - 1) {
                    BPage brother = childBPage(index + 1);
                    int bfirst = brother._first;
                    if (bfirst < half) {
                        int steal = (half - bfirst + 1) / 2;
                        brother._first += steal;
                        child._first -= steal;
                        if (child._isLeaf) {
                            copyEntries(child, half + 1, child, half + 1 - steal, half - 1);
                            copyEntries(brother, bfirst, child, 2 * half - steal, steal);
                        } else {
                            copyChildren(child, half + 1, child, half + 1 - steal, half - 1);
                            copyChildren(brother, bfirst, child, 2 * half - steal, steal);
                        }
                        for (int i = bfirst; i < bfirst + steal; i++) {
                            if (brother._isLeaf) {
                                setEntry(brother, i, null, null);
                            } else {
                                setChild(brother, i, null, -1);
                            }
                        }
                        _keys[index] = child.getLargestKey();
                        _btree._recman.update(_recid, this, this);
                        _btree._recman.update(brother._recid, brother, this);
                        _btree._recman.update(child._recid, child, this);
                    } else {
                        if (brother._first != half) {
                            throw new IllegalStateException("Error during underflow [2]");
                        }
                        brother._first = 1;
                        if (child._isLeaf) {
                            copyEntries(child, half + 1, brother, 1, half - 1);
                        } else {
                            copyChildren(child, half + 1, brother, 1, half - 1);
                        }
                        _btree._recman.update(brother._recid, brother, this);
                        if (_isLeaf) {
                            copyEntries(this, _first, this, _first + 1, index - _first);
                            setEntry(this, _first, null, null);
                        } else {
                            copyChildren(this, _first, this, _first + 1, index - _first);
                            setChild(this, _first, null, -1);
                        }
                        _first += 1;
                        _btree._recman.update(_recid, this, this);
                        if (child._previous != 0) {
                            BPage prev = loadBPage(child._previous);
                            prev._next = child._next;
                            _btree._recman.update(prev._recid, prev, this);
                        }
                        if (child._next != 0) {
                            BPage next = loadBPage(child._next);
                            next._previous = child._previous;
                            _btree._recman.update(next._recid, next, this);
                        }
                        _btree._recman.delete(child._recid);
                    }
                } else {
                    BPage brother = childBPage(index - 1);
                    int bfirst = brother._first;
                    if (bfirst < half) {
                        int steal = (half - bfirst + 1) / 2;
                        brother._first += steal;
                        child._first -= steal;
                        if (child._isLeaf) {
                            copyEntries(brother, 2 * half - steal, child, half + 1 - steal, steal);
                            copyEntries(brother, bfirst, brother, bfirst + steal, 2 * half - bfirst - steal);
                        } else {
                            copyChildren(brother, 2 * half - steal, child, half + 1 - steal, steal);
                            copyChildren(brother, bfirst, brother, bfirst + steal, 2 * half - bfirst - steal);
                        }
                        for (int i = bfirst; i < bfirst + steal; i++) {
                            if (brother._isLeaf) {
                                setEntry(brother, i, null, null);
                            } else {
                                setChild(brother, i, null, -1);
                            }
                        }
                        _keys[index - 1] = brother.getLargestKey();
                        _btree._recman.update(_recid, this, this);
                        _btree._recman.update(brother._recid, brother, this);
                        _btree._recman.update(child._recid, child, this);
                    } else {
                        if (brother._first != half) {
                            throw new IllegalStateException("Error during underflow [3]");
                        }
                        child._first = 1;
                        if (child._isLeaf) {
                            copyEntries(brother, half, child, 1, half);
                        } else {
                            copyChildren(brother, half, child, 1, half);
                        }
                        _btree._recman.update(child._recid, child, this);
                        if (_isLeaf) {
                            copyEntries(this, _first, this, _first + 1, index - 1 - _first);
                            setEntry(this, _first, null, null);
                        } else {
                            copyChildren(this, _first, this, _first + 1, index - 1 - _first);
                            setChild(this, _first, null, -1);
                        }
                        _first += 1;
                        _btree._recman.update(_recid, this, this);
                        if (brother._previous != 0) {
                            BPage prev = loadBPage(brother._previous);
                            prev._next = brother._next;
                            _btree._recman.update(prev._recid, prev, this);
                        }
                        if (brother._next != 0) {
                            BPage next = loadBPage(brother._next);
                            next._previous = brother._previous;
                            _btree._recman.update(next._recid, next, this);
                        }
                        _btree._recman.delete(brother._recid);
                    }
                }
            }
        }
        result._underflow = _first > half;
        return result;
    }

    /**
     * Find the first children node with a key equal or greater than the given
     * key.
     *
     * @return index of first children with equal or greater key.
     */
    private int findChildren(Object key) {
        int left = _first;
        int right = _btree._pageSize - 1;
        while (left < right) {
            int middle = (left + right) / 2;
            if (compare(_keys[middle], key) < 0) {
                left = middle + 1;
            } else {
                right = middle;
            }
        }
        return right;
    }

    /**
     * Insert entry at given position.
     */
    private static void insertEntry(BPage page, int index, Object key, Object value) {
        Object[] keys = page._keys;
        Object[] values = page._values;
        int start = page._first;
        int count = index - page._first + 1;
        System.arraycopy(keys, start, keys, start - 1, count);
        System.arraycopy(values, start, values, start - 1, count);
        page._first -= 1;
        keys[index] = key;
        values[index] = value;
    }

    /**
     * Insert child at given position.
     */
    private static void insertChild(BPage page, int index, Object key, long child) {
        Object[] keys = page._keys;
        long[] children = page._children;
        int start = page._first;
        int count = index - page._first + 1;
        System.arraycopy(keys, start, keys, start - 1, count);
        System.arraycopy(children, start, children, start - 1, count);
        page._first -= 1;
        keys[index] = key;
        children[index] = child;
    }

    /**
     * Remove entry at given position.
     */
    private static void removeEntry(BPage page, int index) {
        Object[] keys = page._keys;
        Object[] values = page._values;
        int start = page._first;
        int count = index - page._first;
        System.arraycopy(keys, start, keys, start + 1, count);
        keys[start] = null;
        System.arraycopy(values, start, values, start + 1, count);
        values[start] = null;
        page._first++;
    }

    /**
     * Set the entry at the given index.
     */
    private static void setEntry(BPage page, int index, Object key, Object value) {
        page._keys[index] = key;
        page._values[index] = value;
    }

    /**
     * Set the child BPage recid at the given index.
     */
    private static void setChild(BPage page, int index, Object key, long recid) {
        page._keys[index] = key;
        page._children[index] = recid;
    }

    /**
     * Copy entries between two BPages
     */
    private static void copyEntries(BPage source, int indexSource, BPage dest, int indexDest, int count) {
        System.arraycopy(source._keys, indexSource, dest._keys, indexDest, count);
        System.arraycopy(source._values, indexSource, dest._values, indexDest, count);
    }

    /**
     * Copy child BPage recids between two BPages
     */
    private static void copyChildren(BPage source, int indexSource, BPage dest, int indexDest, int count) {
        System.arraycopy(source._keys, indexSource, dest._keys, indexDest, count);
        System.arraycopy(source._children, indexSource, dest._children, indexDest, count);
    }

    /**
     * Return the child BPage at given index.
     */
    BPage childBPage(int index) throws IOException {
        return loadBPage(_children[index]);
    }

    /**
     * Load the BPage at the given recid.
     */
    private BPage loadBPage(long recid) throws IOException {
        BPage child = (BPage) _btree._recman.fetch(recid, this);
        child._recid = recid;
        child._btree = _btree;
        return child;
    }

    private final int compare(Object value1, Object value2) {
        if (value1 == null) {
            return 1;
        }
        if (value2 == null) {
            return -1;
        }
        return _btree._comparator.compare(value1, value2);
    }

    static byte[] readByteArray(ObjectInput in) throws IOException {
        int len = in.readInt();
        if (len < 0) {
            return null;
        }
        byte[] buf = new byte[len];
        in.readFully(buf);
        return buf;
    }

    static void writeByteArray(ObjectOutput out, byte[] buf) throws IOException {
        if (buf == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(buf.length);
            out.write(buf);
        }
    }

    /**
     * Dump the structure of the tree on the screen.  This is used for debugging
     * purposes only.
     */
    private void dump(int height) {
        String prefix = "";
        for (int i = 0; i < height; i++) {
            prefix += "    ";
        }
        System.out.println(prefix + "-------------------------------------- BPage recid=" + _recid);
        System.out.println(prefix + "first=" + _first);
        for (int i = 0; i < _btree._pageSize; i++) {
            if (_isLeaf) {
                System.out.println(prefix + "BPage [" + i + "] " + _keys[i] + " " + _values[i]);
            } else {
                System.out.println(prefix + "BPage [" + i + "] " + _keys[i] + " " + _children[i]);
            }
        }
        System.out.println(prefix + "--------------------------------------");
    }

    /**
     * Recursively dump the state of the BTree on screen.  This is used for
     * debugging purposes only.
     */
    void dumpRecursive(int height, int level) throws IOException {
        height -= 1;
        level += 1;
        if (height > 0) {
            for (int i = _first; i < _btree._pageSize; i++) {
                if (_keys[i] == null) break;
                BPage child = childBPage(i);
                child.dump(level);
                child.dumpRecursive(height, level);
            }
        }
    }

    /**
     * Assert the ordering of the keys on the BPage.  This is used for testing
     * purposes only.
     */
    private void assertConsistency() {
        for (int i = _first; i < _btree._pageSize - 1; i++) {
            if (compare((byte[]) _keys[i], (byte[]) _keys[i + 1]) >= 0) {
                dump(0);
                throw new Error("BPage not ordered");
            }
        }
    }

    /**
     * Recursively assert the ordering of the BPage entries on this page
     * and sub-pages.  This is used for testing purposes only.
     */
    void assertConsistencyRecursive(int height) throws IOException {
        assertConsistency();
        if (--height > 0) {
            for (int i = _first; i < _btree._pageSize; i++) {
                if (_keys[i] == null) break;
                BPage child = childBPage(i);
                if (compare((byte[]) _keys[i], child.getLargestKey()) != 0) {
                    dump(0);
                    child.dump(0);
                    throw new Error("Invalid child subordinate key");
                }
                child.assertConsistencyRecursive(height);
            }
        }
    }

    /**
     * Deserialize the content of an object from a byte array.
     *
     * @param serialized Byte array representation of the object
     * @return deserialized object
     *
     */
    public Object deserialize(byte[] serialized) throws IOException {
        ByteArrayInputStream bais;
        ObjectInputStream ois;
        BPage bpage;
        bpage = new BPage();
        bais = new ByteArrayInputStream(serialized);
        ois = new ObjectInputStream(bais);
        bpage._isLeaf = ois.readBoolean();
        if (bpage._isLeaf) {
            bpage._previous = ois.readLong();
            bpage._next = ois.readLong();
        }
        bpage._first = ois.readInt();
        bpage._keys = new Object[_btree._pageSize];
        try {
            for (int i = bpage._first; i < _btree._pageSize; i++) {
                if (_btree._keySerializer == null) {
                    bpage._keys[i] = ois.readObject();
                } else {
                    serialized = readByteArray(ois);
                    if (serialized != null) {
                        bpage._keys[i] = _btree._keySerializer.deserialize(serialized);
                    }
                }
            }
        } catch (ClassNotFoundException except) {
            throw new IOException(except.getMessage());
        }
        if (bpage._isLeaf) {
            bpage._values = new Object[_btree._pageSize];
            try {
                for (int i = bpage._first; i < _btree._pageSize; i++) {
                    if (_btree._valueSerializer == null) {
                        bpage._values[i] = ois.readObject();
                    } else {
                        serialized = readByteArray(ois);
                        if (serialized != null) {
                            bpage._values[i] = _btree._valueSerializer.deserialize(serialized);
                        }
                    }
                }
            } catch (ClassNotFoundException except) {
                throw new IOException(except.getMessage());
            }
        } else {
            bpage._children = new long[_btree._pageSize];
            for (int i = bpage._first; i < _btree._pageSize; i++) {
                bpage._children[i] = ois.readLong();
            }
        }
        ois.close();
        bais.close();
        return bpage;
    }

    /** 
     * Serialize the content of an object into a byte array.
     *
     * @param obj Object to serialize
     * @return a byte array representing the object's state
     *
     */
    public byte[] serialize(Object obj) throws IOException {
        byte[] serialized;
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;
        BPage bpage;
        byte[] data;
        bpage = (BPage) obj;
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeBoolean(bpage._isLeaf);
        if (bpage._isLeaf) {
            oos.writeLong(bpage._previous);
            oos.writeLong(bpage._next);
        }
        oos.writeInt(bpage._first);
        for (int i = bpage._first; i < _btree._pageSize; i++) {
            if (_btree._keySerializer == null) {
                oos.writeObject(bpage._keys[i]);
            } else {
                if (bpage._keys[i] != null) {
                    serialized = _btree._keySerializer.serialize(bpage._keys[i]);
                    writeByteArray(oos, serialized);
                } else {
                    writeByteArray(oos, null);
                }
            }
        }
        if (bpage._isLeaf) {
            for (int i = bpage._first; i < _btree._pageSize; i++) {
                if (_btree._valueSerializer == null) {
                    oos.writeObject(bpage._values[i]);
                } else {
                    if (bpage._values[i] != null) {
                        serialized = _btree._valueSerializer.serialize(bpage._values[i]);
                        writeByteArray(oos, serialized);
                    } else {
                        writeByteArray(oos, null);
                    }
                }
            }
        } else {
            for (int i = bpage._first; i < _btree._pageSize; i++) {
                oos.writeLong(bpage._children[i]);
            }
        }
        oos.flush();
        data = baos.toByteArray();
        oos.close();
        baos.close();
        return data;
    }

    /** STATIC INNER CLASS
     *  Result from insert() method call
     */
    static class InsertResult {

        /**
         * Overflow page.
         */
        BPage _overflow;

        /**
         * Existing value for the insertion key.
         */
        Object _existing;
    }

    /** STATIC INNER CLASS
     *  Result from remove() method call
     */
    static class RemoveResult {

        /**
         * Set to true if underlying pages underflowed
         */
        boolean _underflow;

        /**
         * Removed entry value
         */
        Object _value;
    }

    /** PRIVATE INNER CLASS
     * Browser to traverse leaf BPages.
     */
    static class Browser extends TupleBrowser {

        /**
         * Current page.
         */
        private BPage _page;

        /**
         * Current index in the page.  The index positionned on the next
         * tuple to return.
         */
        private int _index;

        /**
         * Create a browser.
         *
         * @param page Current page
         * @param index Position of the next tuple to return.
         */
        Browser(BPage page, int index) {
            _page = page;
            _index = index;
        }

        public boolean getNext(Tuple tuple) throws IOException {
            if (_index < _page._btree._pageSize) {
                if (_page._keys[_index] == null) {
                    return false;
                }
            } else if (_page._next != 0) {
                _page = _page.loadBPage(_page._next);
                _index = _page._first;
            }
            tuple.setKey(_page._keys[_index]);
            tuple.setValue(_page._values[_index]);
            _index++;
            return true;
        }

        public boolean getPrevious(Tuple tuple) throws IOException {
            if (_index == _page._first) {
                if (_page._previous != 0) {
                    _page = _page.loadBPage(_page._previous);
                    _index = _page._btree._pageSize;
                } else {
                    return false;
                }
            }
            _index--;
            tuple.setKey(_page._keys[_index]);
            tuple.setValue(_page._values[_index]);
            return true;
        }
    }
}
