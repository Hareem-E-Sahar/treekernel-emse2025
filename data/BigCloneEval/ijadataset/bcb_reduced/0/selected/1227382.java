package ch.ethz.mxquery.sms.btree;

import java.util.Vector;
import ch.ethz.mxquery.datamodel.StructuralIdentifier;
import ch.ethz.mxquery.datamodel.adm.LinguisticToken;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.sms.ftstore.PhraseIterator;
import ch.ethz.mxquery.util.ObjectObjectPair;

/**
 * A simple btree implementation. (key equal or less than pivot -> go right) Has
 * to be taylored to different key and value types manually as Generics would
 * use Complex type (inefficent) instead of native types. We have pointers among
 * leaves and also only store key/value mappings on the leaves. Therefore, this
 * is a B+-tree implementation. The implementation allows us to store duplicate
 * keys in the leaf level of the tree. However, a strict tree interval invariant
 * is kept. To handle duplicate keys, this means that overflows due to
 * duplication are handled by adding special overflow leaves that are not
 * pointed to by any parent node. These leaves are created via splitting,
 * however no pivot is promoted on such a split. The delete strategy implemented
 * in this tree is to free at empty. Thus, there is no logic to merge at nodes
 * at half occupation and nodes may become underutilized. This may be monitored
 * by calculating the utilization at the leaf level.
 * 
 * @author jens / marcos extended by christian / julia
 * 
 * adapted to be CLDC conform: jimhof
 * 
 */
public class BTree {

    /** the root of the b-tree */
    protected BTreeNode root = null;

    /** the left-most leaf on the b-tree */
    private Leaf firstLeaf = null;

    /** the degree of the b-tree (internal nodes) */
    protected int k;

    /** the degree of the leaves */
    protected int k_star;

    /** invalid value for atPos index returned from getFirstKeyAfter */
    public static final int INVALID_KEY_INDEX = -1;

    /**
	 * Instantiates a new BTree. Does not create persister. Has to be set
	 * manually afterwards, since every BTree must have a persister.
	 * <p>
	 * TODO remove this constructor.
	 * 
	 * @param k
	 * @param k_star
	 */
    public BTree(int k, int k_star) {
        this.k = k;
        this.k_star = k_star;
    }

    /**
	 * Bulkloads an empty BTree fill leaves from left to right until certain
	 * percentage. If percentage reached begin next leaf and create parent
	 * InternalNode if needed.
	 * 
	 * @param input as Pulloperator of <key,value> Tuples, sorted by keys
	 * @param F denoting the leaf usage
	 * @throws MXQueryException 
	 */
    public BTreeNode bulkLoad(PhraseIterator input, int F) throws MXQueryException {
        Leaf curLeaf;
        ObjectObjectPair curInput;
        ObjectObjectPair overflowInfo;
        int F_help = 2 * k_star * F;
        Vector inputPairs = new Vector();
        curLeaf = new Leaf(k_star);
        firstLeaf = curLeaf;
        while (input.hasNext()) {
            for (int i = 0; i < F_help; ++i) {
                if (input.hasNext()) {
                    LinguisticToken next = (LinguisticToken) input.next();
                    inputPairs.addElement(new ObjectObjectPair(next.getDeweyId(), next));
                } else {
                    break;
                }
            }
            if (inputPairs.size() >= k_star) {
                for (int i = 0; i < inputPairs.size(); i++) {
                    overflowInfo = curLeaf.bulkAdd((ObjectObjectPair) inputPairs.elementAt(i), k, F);
                    if (overflowInfo != null) curLeaf = (Leaf) overflowInfo.getSecond();
                }
                inputPairs = new Vector();
            }
        }
        root = curLeaf.getRoot();
        if (!inputPairs.isEmpty()) {
            for (int i = 0; i < inputPairs.size(); i++) {
                curInput = (ObjectObjectPair) inputPairs.elementAt(i);
                overflowInfo = curLeaf.bulkAdd(curInput, k, F);
                if (overflowInfo != null) curLeaf = (Leaf) overflowInfo.getSecond();
            }
        }
        InternalNode curParent = curLeaf.parentNode;
        if (curParent == null) {
        }
        curLeaf.nextOffset = 2;
        while (curParent != null) {
            if (curParent.getNode(0).isLeaf()) {
                curParent.isOffsetNode = true;
            }
            curParent = curParent.parentNode;
        }
        root = curLeaf.getRoot();
        return root;
    }

    /**
	 * Adds a mapping from key to value in the b-tree. Duplicate mappings are
	 * allowed.
	 * 
	 * @param key
	 * @param value
	 * @param leafCarrier
	 */
    public void add(StructuralIdentifier key, LinguisticToken value, LeafCarrier leafCarrier) {
        System.out.println("BTree add is not supported");
    }

    private class TravResult {

        public TravResult(Leaf l, int p, boolean f) {
            leaf = l;
            pos = p;
            found = f;
        }

        Leaf leaf;

        int pos;

        boolean found;
    }

    private TravResult searchToLeafPos(StructuralIdentifier key, BTreeNode currentRoot) {
        BTreeNode node = currentRoot.getBTreeNode(key);
        while (node instanceof InternalNode) {
            node = node.getBTreeNode(key);
        }
        Leaf leaf = (Leaf) node;
        Vector keys = leaf.entries.keys;
        int l = 0;
        int r = keys.size();
        int m = (l + r) / 2;
        int prevm = 0;
        int prevprevm = 0;
        while (m < keys.size() && m >= 0 && prevm != m && prevprevm != m) {
            StructuralIdentifier mdid = (StructuralIdentifier) keys.elementAt(m);
            int posC = mdid.compare(key);
            if (posC > 0) {
                prevprevm = prevm;
                prevm = m;
                l = m;
            } else if (posC < 0) {
                prevprevm = prevm;
                prevm = m;
                r = m;
            } else {
                return new TravResult(leaf, m, true);
            }
            m = (l + r) / 2;
        }
        return new TravResult(leaf, m, false);
    }

    /**
	 * Gets the value currently mapped to the given key. If no value is found, the next value is returned
	 * 
	 * @param key
	 * @param currentRoot
	 */
    public LinguisticToken get(StructuralIdentifier key, BTreeNode currentRoot) {
        TravResult leafAndPos = searchToLeafPos(key, currentRoot);
        if (leafAndPos.found) return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos); else {
            StructuralIdentifier mdid = (StructuralIdentifier) leafAndPos.leaf.entries.keys.elementAt(leafAndPos.pos);
            if (key.compare(mdid) == 1) {
                return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos);
            } else if (mdid.compare(key) == 1) {
                return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos + 1);
            }
        }
        return null;
    }

    public LinguisticToken getNext(StructuralIdentifier key, BTreeNode currentRoot) {
        TravResult leafAndPos = searchToLeafPos(key, currentRoot);
        if (!leafAndPos.found && ((StructuralIdentifier) leafAndPos.leaf.entries.keys.elementAt(leafAndPos.pos)).compare(key) < 0) {
            return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos);
        } else if (leafAndPos.pos == leafAndPos.leaf.entries.values.size() - 1) {
            if (leafAndPos.leaf.getNextLeaf() != null) {
                return (LinguisticToken) leafAndPos.leaf.getNextLeaf().entries.values.elementAt(0);
            } else {
                return null;
            }
        } else {
            return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos + 1);
        }
    }

    public LinguisticToken getPrev(StructuralIdentifier key, BTreeNode currentRoot) {
        TravResult leafAndPos = searchToLeafPos(key, currentRoot);
        if (!leafAndPos.found && ((StructuralIdentifier) leafAndPos.leaf.entries.keys.elementAt(leafAndPos.pos)).compare(key) > 0) {
            return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos);
        } else if (leafAndPos.pos == 0) {
            if (leafAndPos.leaf.getPrevLeaf() != null) {
                Leaf prev = leafAndPos.leaf.getPrevLeaf();
                return (LinguisticToken) prev.entries.values.elementAt(prev.entries.values.size() - 1);
            } else {
                return null;
            }
        } else {
            return (LinguisticToken) leafAndPos.leaf.entries.values.elementAt(leafAndPos.pos - 1);
        }
    }

    public Vector getSiblings(StructuralIdentifier from, StructuralIdentifier parent, BTreeNode currentRoot, StructuralIdentifier[] ignoreId) {
        TravResult leafAndPos = searchToLeafPos(from, currentRoot);
        int m = leafAndPos.pos;
        int startPos;
        Leaf leaf = leafAndPos.leaf;
        if (m == leaf.entries.values.size() - 1) {
            if (leaf.getNextLeaf() != null) {
                startPos = 1;
                leaf = leaf.getNextLeaf();
            } else {
                return new Vector();
            }
        } else {
            startPos = m + 1;
        }
        Vector siblings = new Vector();
        boolean isSibling = true;
        while (leaf != null && isSibling) {
            while (startPos < leaf.entries.values.size()) {
                LinguisticToken sibling = (LinguisticToken) leaf.entries.values.elementAt(startPos);
                StructuralIdentifier did = sibling.getDeweyId();
                if (parent.isAncestorOf(did)) {
                    boolean isIgnored = false;
                    for (int i = 0; i < ignoreId.length; i++) {
                        if (ignoreId[i].isAncestorOf(did)) isIgnored = true;
                    }
                    if (!isIgnored) {
                        siblings.addElement(sibling);
                    }
                    startPos++;
                    if (startPos == leaf.entries.values.size()) {
                        leaf = leaf.getNextLeaf();
                        startPos = 0;
                    }
                    if (leaf == null) {
                        break;
                    }
                } else {
                    isSibling = false;
                    break;
                }
            }
            if (leaf != null) {
                leaf = leaf.getNextLeaf();
            }
        }
        if (leaf != null) {
            if (leaf.getNextLeaf() == null && startPos < leaf.entries.values.size() && isSibling) {
                while (startPos < leaf.entries.values.size()) {
                    LinguisticToken sibling = (LinguisticToken) leaf.entries.values.elementAt(startPos);
                    StructuralIdentifier did = sibling.getDeweyId();
                    if (parent.isAncestorOf(did)) {
                        boolean isIgnored = false;
                        for (int i = 0; i < ignoreId.length; i++) {
                            if (ignoreId[i].isAncestorOf(did)) isIgnored = true;
                        }
                        if (!isIgnored) {
                            siblings.addElement(sibling);
                        }
                        startPos++;
                    } else {
                        break;
                    }
                }
            }
        }
        return siblings;
    }

    /**
	 * Gets the key value in the given leaf at the given position
	 * 
	 * @param l leaf in which key is looked up
	 * @param pos position inside leaf where key is at
	 * @return the identifier at l, pos
	 */
    public StructuralIdentifier getKey(Leaf l, int pos) {
        if (l == null) {
            return null;
        }
        return (StructuralIdentifier) l.entries.keys.elementAt(pos);
    }

    /**
	 * This method takes two arrays as out parameters and inserts in them
	 * pointers to the leaf and position of the smallest key currently in tree
	 * which is either equal to <code>key</code> (if <code>key</code> is
	 * present in btree) or smallest key larger than <code>key</code> (if
	 * <code>key</code> is not present in btree):<br>
	 * 
	 * @param key
	 * @param inLeaf points to the leaf where to get the key
	 * @param atPos points to the position of the key inside the leaf
	 */
    public void getFirstKeyAfter(StructuralIdentifier key, Leaf[] inLeaf, int[] atPos) {
        if (key.compare(this.getLastKey()) > 0) {
            return;
        }
        root.getFirstKeyAfter(key, inLeaf, atPos);
    }

    /**
	 * This method takes two arrays as out parameters and inserts in them
	 * pointers to the leaf and position of the largest key currently in tree
	 * which is either equal to <code>key</code> (if <code>key</code> is
	 * present in btree) or largest key smaller than <code>key</code> (if
	 * <code>key</code> is not present in btree):<br>
	 * 
	 * @param key the search key
	 * @param inLeaf points to the leaf where to get the key
	 * @param atPos points to the position of the key inside the leaf
	 */
    public void getLastKeyBefore(StructuralIdentifier key, Leaf[] inLeaf, int[] atPos) {
        if (key.compare(this.getFirstKey()) < 0) {
            return;
        }
        root.getLastKeyBefore(key, inLeaf, atPos);
    }

    /**
	 * This method takes two arrays as out parameters and inserts in them
	 * pointers to the leaf and position of the largest key currently in tree
	 * which is either equal to <code>key</code> (if <code>key</code> is
	 * present in btree) or largest key smaller than <code>key</code> (if
	 * <code>key</code> is not present in btree). This method implements the
	 * same functionality as getLastKeyBefore but uses leaf traversal to find
	 * the position of the key.<br>
	 * 
	 * @param toKey the search key
	 * @param startLeaf
	 * @param startPos
	 * @param inLeaf points to the leaf where to get the key
	 * @param atPos points to the position of the key inside the leaf
	 */
    public void getLastKeyBeforeLeafTraversal(StructuralIdentifier toKey, Leaf startLeaf, int startPos, Leaf[] inLeaf, int[] atPos) {
        Leaf currentLeaf = startLeaf;
        StructuralIdentifier currentKey = (StructuralIdentifier) currentLeaf.entries.keys.elementAt(startPos);
        int posInLeafKeys = startPos;
        while (currentKey.compare(toKey) < 0) {
            posInLeafKeys++;
            if (posInLeafKeys == currentLeaf.entries.currentSize) {
                posInLeafKeys = 0;
                currentLeaf = currentLeaf.getNextLeaf();
            }
            currentKey = (StructuralIdentifier) currentLeaf.entries.keys.elementAt(posInLeafKeys);
        }
        atPos[0] = posInLeafKeys;
        inLeaf[0] = currentLeaf;
    }

    /**
	 * Removes all mappings corresponding to the given key from the b-tree.
	 * 
	 * @param key
	 */
    public void remove(StructuralIdentifier key) {
        root.remove(key, null, null, null);
    }

    /**
	 * Removes one instance of the given key-value mapping from the b-tree. Note
	 * that even if multiple instances of that mapping exist, only a single
	 * instance will be removed.
	 * 
	 * @param key
	 * @param value
	 */
    public void remove(StructuralIdentifier key, LinguisticToken value) {
        root.remove(key, value, null, null);
    }

    /**
	 * Returns all the values mapped in the given key range through the provided
	 * push operator. We include values that correspond to the lowKey and also
	 * include values that correspond to the highKey.
	 * 
	 * @param lowKey
	 * @param highKey
	 * @param results PushOperator to be filled.
	 */
    public void queryRange(StructuralIdentifier lowKey, StructuralIdentifier highKey, BtreePushOperator results) {
        root.queryRange(lowKey, highKey, results);
    }

    /**
	 * Prints the root of the tree as a string.
	 */
    public String toString() {
        if (root == null) {
            return "b-tree not initialized!";
        } else {
            return root.toString();
        }
    }

    /**
	 * Close this b-tree. That is make it persistent first.
	 */
    public void close() {
    }

    /**
	 * prints statistical infos about the tree
	 */
    public void printStats() {
    }

    /**
	 * @return number of keys in an initialized this b-tree.
	 */
    public int size() {
        int elemCount = 0;
        if ((root != null) && (!root.isEmpty())) {
            Leaf currentLeaf = getFirstLeaf();
            while (currentLeaf != null) {
                elemCount += currentLeaf.entries.size();
                currentLeaf = currentLeaf.getNextLeaf();
            }
        }
        return elemCount;
    }

    /**
	 * Gets the root of the btree
	 * 
	 * @return the root of the btree
	 */
    public BTreeNode getRoot() {
        return root;
    }

    /**
	 * gets the first leaf of the btree
	 * 
	 * @return the first leaf of the btree or null if no leaf exists
	 */
    public Leaf getFirstLeaf() {
        if (firstLeaf != null) {
            return firstLeaf;
        }
        return null;
    }

    public Leaf getLastLeaf() {
        BTreeNode currentNode = root;
        while (!currentNode.isLeaf()) {
            InternalNode currentInternalNode = (InternalNode) currentNode;
            currentNode = currentInternalNode.getNode(currentInternalNode.entries.currentSize);
        }
        Leaf leaf = (Leaf) currentNode;
        return leaf;
    }

    /**
	 * This method returns the last(largest) key present in the tree
	 * 
	 * @return the largest key in the tree
	 */
    public StructuralIdentifier getLastKey() {
        Leaf leaf = getLastLeaf();
        return (StructuralIdentifier) leaf.entries.keys.elementAt(leaf.entries.currentSize - 1);
    }

    /**
	 * This method returns the first(smallest) currently in the tree. If the
	 * tree is empty the method returns null.
	 * 
	 * @return the smaller key
	 */
    public StructuralIdentifier getFirstKey() {
        Leaf leaf = getFirstLeaf();
        if (leaf != null) return (StructuralIdentifier) leaf.entries.keys.elementAt(0); else return null;
    }
}
