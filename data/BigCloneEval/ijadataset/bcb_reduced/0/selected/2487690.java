package ti.series.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;
import ti.exceptions.ProgrammingErrorException;
import ti.mcore.u.FileUtil;
import ti.series.Region;

/**
 * An implementation of a B+ Tree.
 * <p>
 * Note: currently the key's and value's are stored as 32bit, but handled in 
 * memory and on the interface as 64bit long's.  Maybe someday I'll enhance
 * this to work with either 32b or 64b keys and values, because in some cases
 * we do need 64b keys, but in other cases we don't want to pay the price of
 * larger disk accesses..
 * <p>
 * Note: the implementation is rather simplified by not needing to support
 * key removal.. series doesn't need this, and it avoids needing to also
 * implement support to re-balance the tree.
 * <p>
 * Note: the tree can tolerate having multiple identical keys, but the
 * {@link #lookup} operation will only return the first.  But duplicate
 * keys should have unique values, making the key/value pair unique.  
 * Otherwise the {@link #replaceValue(long, long, long)} operation might be
 * confused about what to do.
 * <p>
 * TBD, possibly add some sort of <code>getRange()</code> operation.. this
 * would be useful for {@link Region}
 * <p>
 * Note: normal usage of inserting entries more or less in order by key value
 * is not optimal efficiency, as it leads to mostly half-full nodes.  The
 * performance is still an improvement over a sparse <code>s2i</code> lookup
 * table, so I'm leaving this as-is for now.  Borrowing some ideas from a 
 * B* Tree or B# Tree would help here, ie. shuffling key/values to the left
 * neighbor, and then only splitting a node when it is both full and also
 * it's left neighbor is full.  I'm not sure yet if this should only be
 * done at the leaf level, or also at branch nodes in the tree.
 * <p>
 * References:
 * <ul>
 *   <li> <a href="http://en.wikipedia.org/wiki/B%2B_tree">B+ Tree</a> on 
 *     wikipedia has a good overview.  Compared to what is described on that
 *     page, this implementation stores the minimum key of the child node in
 *     the parent (rather than maximum), because in general we expect keys
 *     to be inserted in numerically increasing order, and this avoids
 *     needing to touch the parent node on trivial key insertion (ie. when
 *     the child node isn't split)
 *   <li> <a href="http://en.wikipedia.org/wiki/B-tree">B Tree</a> page on 
 *     wikipedia has a better description of the insert algorithm and node
 *     splitting.  A B+ Tree, which is what is implemented by this class,
 *     has some differences because values are only stored in the leaf nodes,
 *     but this page is still helpful to read to understand the concept. 
 * </ul>
 * 
 * @author robclark
 */
class BPTree {

    private File file;

    private RandomAccessFile dataFile;

    /**
   * Always keep root node in RAM for faster access
   */
    private Node root;

    /**
   * Number of key/value pairs per node
   */
    private final int maxEntriesPerNode;

    private final int keySize;

    private final int valueSize;

    /**
   * Class Constructor
   * 
   * @param name
   * @param keySize     the size of the key (4 or 8) in bytes
   * @param valueSize   the size of the value (4 or 8) in bytes
   * @throws DiskStoreException
   */
    BPTree(String name, int keySize, int valueSize) throws DiskStoreException {
        try {
            if ((keySize != 4) && (keySize != 8)) throw new IllegalArgumentException("invalid keySize");
            if ((valueSize != 4) && (valueSize != 8)) throw new IllegalArgumentException("invalid valueSize");
            maxEntriesPerNode = (NODE_SIZE - HEADER_SIZE) / (keySize + valueSize);
            this.keySize = keySize;
            this.valueSize = valueSize;
            file = FileUtil.createTempFile("bptree", name);
            DiskSeries.LOGGER.dbg("DiskSeries: B+ Tree file: " + file);
            dataFile = new RandomAccessFile(file, "rw");
            root = getNode(-1);
        } catch (IOException e) {
            throw new DiskStoreException(e);
        } finally {
            flush();
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    /**
   * Cleanup temporary files created by this table.  This should only
   * be called when the table is no longer needed.
   */
    synchronized void dispose() throws IOException {
        dataFile.close();
        file.delete();
    }

    /**
   * Lookup key value.  If we don't find an exact match, return the closest
   * match.
   * 
   * @param key
   * @return
   */
    public synchronized long lookup(long key) throws DiskStoreException {
        try {
            Node node = findNode(root, key, null);
            int idx = node.findInsertionPoint(key) - 1;
            if (idx < 0) return -1;
            long idxKey = node.getKey(idx);
            Node nextNode = node;
            int nextIdx = idx + 1;
            if (idx + 1 == node.getSize()) {
                long nextNodeOffset = node.getNext();
                if (nextNodeOffset == NULL_OFFSET) {
                    nextNode = null;
                } else {
                    nextNode = getNode(nextNodeOffset);
                    nextIdx = 0;
                }
            }
            if (nextNode != null) {
                long nextKey = nextNode.getKey(nextIdx);
                if (Math.abs(key - idxKey) > Math.abs(key - nextKey)) return nextNode.getValue(nextIdx);
            }
            return node.getValue(idx);
        } catch (IOException e) {
            throw new DiskStoreException(e);
        } finally {
            flush();
        }
    }

    /**
   * Add a key, value pair
   * 
   * @param key
   * @param value
   */
    public synchronized void insert(long key, long value) throws DiskStoreException {
        try {
            ArrayList<Node> stack = new ArrayList<Node>();
            Node node = findNode(root, key, stack);
            stack.add(node);
            insertRecursively(stack, stack.size() - 1, key, value);
        } catch (IOException e) {
            throw new DiskStoreException(e);
        } finally {
            flush();
        }
    }

    private void insertRecursively(ArrayList<Node> stack, int stackIdx, final long key, final long value) throws IOException {
        Node node = stack.get(stackIdx);
        int idx = node.insert(key, value);
        if (idx == 0) {
            System.err.println("new minimum key");
            int parentIdx = stackIdx - 1;
            while (parentIdx >= 0) {
                Node parent = stack.get(parentIdx);
                int pidx = parent.findInsertionPoint(key) - 1;
                System.err.println("pidx=" + pidx);
                if (pidx < 0) pidx = 0;
                parent.setKey(pidx, key);
                if (pidx != 0) break;
                parentIdx--;
            }
        } else if (idx == -1) {
            Node newNode = getNode(-1);
            node.splitInto(newNode);
            if (stackIdx == 0) {
                System.err.println("new root! " + (root == node));
                Node newRoot = getNode(-1);
                newRoot.insert(node.getKey(0), node.off);
                newRoot.insert(newNode.getKey(0), newNode.off);
                root = newRoot;
            } else {
                insertRecursively(stack, stackIdx - 1, newNode.getKey(0), newNode.off);
            }
            if (key >= newNode.getKey(0)) stack.set(stackIdx, newNode);
            insertRecursively(stack, stackIdx, key, value);
        }
    }

    /**
   * Replace the specified key/value mapping with a new value.  Both
   * the existing value (<code>oldValue</code>) and the new value
   * (<code>newValue</code>) must be specified, because there could
   * be duplicate keys and we want to be sure to update the proper
   * mapping.
   * 
   * @param key      the key mapping to the value to increment
   * @param oldValue the existing value of the specific key/value mapping
   *    that should be updated
   * @param newValue the replacement value
   */
    public synchronized void replaceValue(long key, long oldValue, long newValue) throws DiskStoreException {
        try {
            Node node = findNode(root, key, null);
            int idx = node.findInsertionPoint(key) - 1;
            if (idx < 0) return;
            while ((idx >= 0) && (node.getKey(idx) == key)) {
                if (node.getValue(idx) == oldValue) {
                    node.setValue(idx, newValue);
                    return;
                }
                idx--;
                if (idx < 0) {
                    long prevNodeOffset = node.getPrev();
                    if (prevNodeOffset != NULL_OFFSET) {
                        node = getNode(prevNodeOffset);
                        idx = node.getSize() - 1;
                    }
                }
            }
            DiskSeries.LOGGER.logError(new Exception("we didn't find a match!"));
        } catch (IOException e) {
            throw new DiskStoreException(e);
        } finally {
            flush();
        }
    }

    /**
   * Helper to find the leaf node containing a key, or at least the node which 
   * would properly contain the specified key if we were going to insert the
   * key
   * 
   * @param node   the point in the tree at which to start the search
   * @param key
   * @param stack  if not <code>null</code>, this can be used to track the
   *   path from {@link #root}.  This can be used during insertion, when we
   *   potentially need to update the resulting leaf node's parent(s) (if
   *   there is a split)
   * @return the target node
   */
    private Node findNode(Node node, long key, ArrayList<Node> stack) throws IOException {
        while (!node.isLeaf()) {
            if (stack != null) stack.add(node);
            int idx = node.findInsertionPoint(key) - 1;
            if (idx < 0) idx = 0;
            node = getNode(node.getValue(idx));
        }
        return node;
    }

    /**
   * things get confusing if we have multiple {@link Node} objects 
   * representing the same range of disk.. so we cache nodes until
   * {@link #flush()} is called at the end of an access to the tree
   */
    private Hashtable<Long, Node> nodeCache = new Hashtable<Long, Node>();

    /**
   * To speed-up access, we keep {@link WeakReference}s to nodes
   * that we no longer require to have in memory (ie. they've already
   * been {@link #flush()}ed) so if they aren't GC'd we can avoid
   * reading them back from disk.
   */
    private Hashtable<Long, Reference<Node>> nodeRefCache = new Hashtable<Long, Reference<Node>>();

    private boolean flushDisabled = false;

    private int nflushed;

    private int nwritten;

    /**
   * This ensures that all nodes in-memory (ie. whatever is accessed since
   * the last <code>flush()</code>) have any changes flushed to disk.
   */
    private synchronized void flush() throws DiskStoreException {
        if (flushDisabled) return;
        try {
            nflushed = nwritten = 0;
            if (nodeCache.size() > 0) {
                for (Entry<Long, Node> e : nodeCache.entrySet()) {
                    Node node = e.getValue();
                    node.flush();
                    nflushed++;
                    nodeRefCache.put(e.getKey(), new WeakReference<Node>(node));
                }
                nodeCache.clear();
                nodeCache.put(root.off, root);
            }
        } catch (IOException e) {
            throw new DiskStoreException(e);
        }
    }

    /**
   * This enables the user of this class to temporarily disable flushes
   * (which will keep touched nodes in memory).  This is useful if it
   * is expected to perform a lot of operations in a short time, to avoid
   * excess disk writes.  It should be used like:
   * <pre>
   *   try
   *   {
   *      tree.enableFlush(false);
   *      ... lots of insert()/replaceValue()/etc ...
   *   }
   *   finally
   *   {
   *     tree.enableFlush(true);
   *   }
   * </pre>
   * The purpose of putting this in a <code>try</code>/<code>finally</code>
   * is to avoid leaving the tree with flushes disabled.
   * 
   * @param enable
   * @throws DiskStoreException
   */
    public void enableFlush(boolean enable) throws DiskStoreException {
        flushDisabled = !enable;
        if (enable) flush();
    }

    /**
   * Don't call Node constructor directly, but instead use this helper.  This
   * is where we deal with caching, to avoid multiple copies of the same node
   * <p>
   * Construct a new node, or read an existing node from disk
   * 
   * @param off     <code>-1</code> to allocate a new node, otherwise
   *   the offset of an existing node within the file
   * @throws IOException 
   */
    private Node getNode(long off) throws IOException {
        Node node = null;
        node = nodeCache.get(off);
        if (node == null) {
            Reference<Node> ref = nodeRefCache.get(off);
            if (ref != null) node = ref.get();
            if (node == null) node = new Node(off);
            nodeCache.put(node.off, node);
        }
        return node;
    }

    /**
   * Size of a node's header fields.
   */
    private static final int HEADER_SIZE = 8;

    /**
   * Size of the node on disk, in bytes.. should be a multiple of the
   * page size.
   */
    private static final int NODE_SIZE = 4096;

    /** 
   * A special offset field value used for next/prev pointers of branch nodes,
   * which is basically a cheap way to identify branch nodes without adding an
   * extra field in the header, and without being clever about storing file
   * offsets in less than 4 bytes..
   */
    private static final long INVALID_OFFSET = 0xFFFFFFL * NODE_SIZE;

    /**
   * A special offset field value used for next/prev pointers of branch nodes,
   * having the same meaning as <code>null</code> (ie, no next/prev).  But
   * since the first created node has a file offset of zero, we can't use
   * zero to indicate <code>null</code>.
   */
    private static final long NULL_OFFSET = 0xFFFFFEL * NODE_SIZE;

    /**
   * Helper class to manage reading/writing fields within a node on disk.
   * The layout of a node on disk is:
   * <pre>
   *   HEADER:  (total: 8 bytes)
   *     SIZE   (2 bytes)  -  number of mappings in this page
   *     NEXT   (3 bytes)  -  offset in file of the next sibling node
   *       for leaf nodes or <code>INVALID_OFFSET</code> for non-leaf nodes
   *       (offset stored in multiples <code>NODE_SIZE</code>)
   *     PREV   (3 bytes)  -  offset in file of the prev sibling node
   *       for leaf nodes or <code>INVALID_OFFSET</code> for non-leaf nodes
   *       (offset stored in multiples <code>NODE_SIZE</code>)
   *   PAYLOAD: (repeat: (NODE_SIZE-HEADER_SIZE)/(keySize+valueSize) times)
   *     KEY    (keySize bytes)
   *     VALUE  (valueSize bytes)
   * </pre>
   * The PAYLOAD is maintained sorted by increasing KEY value to allow for
   * O(lg n) search.
   * <p>
   * Note: I'm not sure the use-case for the NEXT pointer for branch nodes
   * so for now I re-use it to differentiate between leaf and branch nodes.
   * But this could easily be stored as a special bit in the SIZE field
   * (which will have a max value of <code>(NODE_SIZE-HEADER_SIZE)/8</code>) 
   * if I come up with a use-case for this..
   */
    class Node {

        final long off;

        private byte[] data = new byte[NODE_SIZE];

        private boolean dirty = false;

        /**
     * @see BPTree#getNode(long)
     */
        Node(long off) throws IOException {
            boolean create = false;
            if (off == -1) {
                off = dataFile.length();
                create = true;
            }
            this.off = off;
            if (!create) {
                dataFile.seek(off);
                dataFile.read(data);
            } else {
                setSize(0);
                setNext((off == 0) ? NULL_OFFSET : INVALID_OFFSET);
                setPrev((off == 0) ? NULL_OFFSET : INVALID_OFFSET);
                dataFile.setLength(off + NODE_SIZE);
            }
        }

        /**
     * flush nodes contents to disk (if dirty)
     */
        void flush() throws IOException {
            if (dirty) {
                dataFile.seek(off);
                dataFile.write(data);
                dirty = false;
                nwritten++;
            }
        }

        private final long read(int sz, int off) {
            long val = 0;
            switch(sz) {
                case 8:
                    val = (val << 8) | (data[off++] & 0xff);
                    val = (val << 8) | (data[off++] & 0xff);
                    val = (val << 8) | (data[off++] & 0xff);
                    val = (val << 8) | (data[off++] & 0xff);
                case 4:
                    val = (val << 8) | (data[off++] & 0xff);
                case 3:
                    val = (val << 8) | (data[off++] & 0xff);
                case 2:
                    val = (val << 8) | (data[off++] & 0xff);
                    val = (val << 8) | (data[off++] & 0xff);
                    return val;
                default:
                    throw new ProgrammingErrorException("invalid size: " + sz);
            }
        }

        private final void write(int sz, int off, long val) {
            off += sz;
            switch(sz) {
                case 8:
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                case 4:
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                case 3:
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                case 2:
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                    data[--off] = (byte) (val & 0xff);
                    val >>>= 8;
                    dirty = true;
                    return;
                default:
                    throw new ProgrammingErrorException("invalid size: " + sz);
            }
        }

        private void setSize(int val) {
            write(2, 0, val);
        }

        int getSize() {
            return (int) read(2, 0);
        }

        void setNext(long val) {
            write(3, 2, val / NODE_SIZE);
        }

        long getNext() {
            return read(3, 2) * NODE_SIZE;
        }

        void setPrev(long val) {
            write(3, 5, val / NODE_SIZE);
        }

        long getPrev() {
            return read(3, 5) * NODE_SIZE;
        }

        boolean isLeaf() {
            return getNext() != INVALID_OFFSET;
        }

        long getKey(int idx) {
            return read(keySize, HEADER_SIZE + (idx * (keySize + valueSize)));
        }

        void setKey(int idx, long val) {
            write(keySize, HEADER_SIZE + (idx * (keySize + valueSize)), val);
        }

        long getValue(int idx) {
            return read(valueSize, HEADER_SIZE + (idx * (keySize + valueSize)) + keySize);
        }

        void setValue(int idx, long val) {
            write(valueSize, HEADER_SIZE + (idx * (keySize + valueSize)) + keySize, val);
        }

        /**
     * Find the index of the first key/value entry in this node which has a 
     * key value greater than the key, ie. the insertion point if we were
     * going to insert this key into this node.
     */
        int findInsertionPoint(long key) {
            int sz = getSize();
            int start = 0;
            int end = sz - 1;
            int mid = 0;
            long mkey = 0;
            boolean found = false;
            while ((start <= end) && (start >= 0) && (end < maxEntriesPerNode) && !found) {
                mid = (start + end) / 2;
                mkey = getKey(mid);
                if (key > mkey) start = mid + 1; else if (key < mkey) end = mid - 1; else found = true;
            }
            if (found) {
                while ((mkey == key) && (++mid < sz)) mkey = getKey(mid);
            } else {
                if ((mkey < key) && (mid < sz)) mid++;
            }
            return mid;
        }

        /**
     * Insert a new key/value into this node.
     * @return the index within this node where the new entry was inserted,
     *   or <code>-1</code> if the node is full.  If the new entry is
     *   inserted at index <code>0</code>, then our parent needs updating
     *   too (because now we have a new minimum key value).
     */
        int insert(long key, long value) {
            int sz = getSize();
            if (sz == maxEntriesPerNode) return -1;
            int idx = findInsertionPoint(key);
            int off = HEADER_SIZE + (idx * (keySize + valueSize));
            System.arraycopy(data, off, data, off + keySize + valueSize, (keySize + valueSize) * (sz - idx));
            dirty = false;
            setKey(idx, key);
            setValue(idx, value);
            setSize(getSize() + 1);
            return idx;
        }

        /**
     * Copy our upper half of key-value pairs into a new node, as part of a 
     * split operation
     */
        void splitInto(Node newNode) throws IOException {
            int sz = getSize();
            int idx = sz / 2;
            int off = HEADER_SIZE + (idx * (keySize + valueSize));
            System.arraycopy(data, off, newNode.data, HEADER_SIZE, (keySize + valueSize) * (sz - idx));
            dirty = false;
            setSize(idx);
            newNode.setSize(sz - idx);
            if (isLeaf()) {
                long nextOff = getNext();
                newNode.setNext(nextOff);
                setNext(newNode.off);
                newNode.setPrev(this.off);
                if (nextOff != NULL_OFFSET) {
                    getNode(nextOff).setPrev(newNode.off);
                }
            }
        }

        /** for easier debugging */
        public String toString() {
            int sz = getSize();
            int last = (sz > 0) ? sz - 1 : sz;
            long next = getNext();
            long prev = getPrev();
            long mink = getKey(0);
            long maxk = getKey(last);
            return "{sz=" + sz + ", off=" + off + ", next=" + next + ", prev=" + prev + ", keys=[" + mink + ".." + maxk + "]}";
        }

        /** mainly for debugging at this point */
        public Collection<Long> keys() {
            int sz = getSize();
            ArrayList<Long> keyList = new ArrayList<Long>(sz);
            for (int i = 0; i < sz; i++) keyList.add(getKey(i));
            return keyList;
        }

        /** mainly for debugging at this point */
        public Collection<Long> values() {
            int sz = getSize();
            ArrayList<Long> keyList = new ArrayList<Long>(sz);
            for (int i = 0; i < sz; i++) keyList.add(getValue(i));
            return keyList;
        }
    }

    /**
   * Test code..
   */
    public static void main(String[] args) throws DiskStoreException, IOException {
        BPTree tree = null;
        long[] vectors = new long[10240];
        for (int i = 0; i < vectors.length; i++) vectors[i] = (long) (Math.random() * Long.MAX_VALUE);
        try {
            long t;
            tree = new BPTree("test1", 8, 4);
            t = System.currentTimeMillis();
            for (int i = 0; i < vectors.length; i++) tree.insert(vectors[i], i);
            t = System.currentTimeMillis() - t;
            System.err.println("inserted " + vectors.length + " keys in " + t + "ms");
            System.err.println("file is " + (tree.dataFile.length() / 1024) + "k");
            t = System.currentTimeMillis();
            for (int i = 0; i < vectors.length; i++) if (tree.lookup(vectors[i]) != i) System.err.println(">>> lookup failed.. " + tree.lookup(vectors[i]) + " vs. " + i);
            t = System.currentTimeMillis() - t;
            System.err.println("looked up " + vectors.length + " keys in " + t + "ms");
            tree.dispose();
            tree = new BPTree("test2", 4, 4);
            final int COUNT = tree.maxEntriesPerNode * tree.maxEntriesPerNode * tree.maxEntriesPerNode;
            t = System.currentTimeMillis();
            for (int i = 0; i < COUNT; i++) tree.insert(i * 3, i * 3);
            t = System.currentTimeMillis() - t;
            System.err.println("inserted " + COUNT + " keys in " + t + "ms");
            {
                Node n = tree.root;
                int level = 1;
                while (!n.isLeaf()) {
                    n = tree.getNode(n.getValue(0));
                    level++;
                }
                System.err.println("tree is " + level + " levels");
            }
            System.err.println("file is " + (tree.dataFile.length() / 1024) + "k");
            t = System.currentTimeMillis();
            for (int i = 0; i < COUNT; i++) {
                if (tree.lookup(3 * i) != (3 * i)) System.err.println(">>> exact lookup failed: " + tree.lookup(3 * i) + " vs " + (3 * i));
                if (tree.lookup(3 * i + 1) != (3 * i)) System.err.println(">>> closest key greater-than lookup failed: " + tree.lookup(3 * i + 1) + " vs " + (3 * i));
                if (tree.lookup(3 * i + 2) != (3 * i + 3)) System.err.println(">>> closest key less-than lookup failed: " + tree.lookup(3 * i + 2) + " vs " + (3 * i + 3));
            }
            t = System.currentTimeMillis() - t;
            System.err.println("looked up " + (3 * COUNT) + " keys in " + t + "ms");
            tree.dispose();
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
