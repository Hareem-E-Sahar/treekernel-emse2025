package ch.ethz.mxquery.sms.btree;

import java.util.Vector;
import ch.ethz.mxquery.datamodel.StructuralIdentifier;
import ch.ethz.mxquery.datamodel.adm.LinguisticToken;
import ch.ethz.mxquery.util.ObjectObjectPair;

/**
 * Represents a b-tree directory node.
 * 
 * @author jens/marcos extended by christian/julia
 *  
 *  adapted to be CLDC conform: jimhof
 */
public class InternalNode implements BTreeNode {

    public boolean isOffsetNode;

    protected InternalNodeArrayMap entries;

    protected int k;

    protected double f;

    protected InternalNode nextNode;

    protected InternalNode parentNode;

    protected StructuralIdentifier lowestKey;

    /**
	 * Constructors for the Internal Node
	 */
    public InternalNode(int k, InternalNodeArrayMap entries) {
        this.k = k;
        this.entries = entries;
        lowestKey = getNode(0).getLowestKey();
    }

    public InternalNode(int k, InternalNodeArrayMap entries, InternalNode parent) {
        this(k, entries);
        this.parentNode = parent;
    }

    public InternalNode(BTreeNode leftChild, StructuralIdentifier pivot, BTreeNode rightChild, int k) {
        this.k = k;
        this.entries = new InternalNodeArrayMap(2 * k);
        entries.nodes = addElementAtPos(entries.nodes, 0, leftChild);
        entries.put(pivot, rightChild);
        leftChild.setParent(this);
        rightChild.setParent(this);
        lowestKey = leftChild.getLowestKey();
    }

    public InternalNode(int k) {
        this.k = k;
    }

    /**
	 * Add (generally updates) is not supported in the BTree
	 */
    public SplitInfo add(StructuralIdentifier key, LinguisticToken value, StructuralIdentifier lowKey, StructuralIdentifier highKey, LeafCarrier leafCarrier) {
        System.out.println("BTree permits no updates");
        return null;
    }

    /**
	 * Remove (generally updates) is not supported in the BTree
	 */
    public void remove(StructuralIdentifier key, LinguisticToken value, StructuralIdentifier lowKey, StructuralIdentifier highKey) {
        System.out.println("BTree permits no updates");
    }

    public String toString() {
        if (isOffsetNode) return "[" + ((InternalNodeArrayOffsetMap) entries).toString() + "]"; else return "[" + entries.toString() + "]";
    }

    /**
	 * Obtains all values mapped to the given key range (low and high,
	 * inclusive)
	 * 
	 * @param lowKey
	 * @param highKey
	 * @param results
	 */
    public void queryRange(StructuralIdentifier lowKey, StructuralIdentifier highKey, BtreePushOperator results) {
        BTreeNode next = getBTreeNode(lowKey);
        next.queryRange(lowKey, highKey, results);
    }

    public boolean isLeaf() {
        return false;
    }

    public boolean isEmpty() {
        return entries.size() == 0 && getNode(0) == null;
    }

    /**
	 * adds a data pair (key,BTreeNode)
	 * if the internal node is full, create new internal node and return value and internal node
	 * else insert key into internal node
	 */
    public ObjectObjectPair bulkAdd(ObjectObjectPair data, int k, double F) {
        StructuralIdentifier key = (StructuralIdentifier) data.getFirst();
        BTreeNode rightNode = (BTreeNode) data.getSecond();
        InternalNode newNode;
        ObjectObjectPair newPair;
        f = F;
        if (entries.size() >= 2 * k * F) {
            newNode = new InternalNode(getNode(entries.size()), key, rightNode, k);
            newPair = new ObjectObjectPair(newNode.getLowestKey(), newNode);
            entries.deleteAtPos(entries.size());
            if (((BTreeNode) this.entries.nodes.elementAt(0)).isLeaf()) {
                isOffsetNode = true;
            }
            if (parentNode == null) {
                new InternalNode(this, newNode.getLowestKey(), newNode, k);
            } else parentNode.bulkAdd(newPair, k, F);
        } else {
            entries.addAtPos(key, entries.currentSize, rightNode);
            rightNode.setParent(this);
        }
        return null;
    }

    public BTreeNode getRoot() {
        if (parentNode == null) {
            return this;
        }
        return parentNode.getRoot();
    }

    public void setParent(InternalNode parentNode) {
        this.parentNode = parentNode;
    }

    public StructuralIdentifier getLowestKey() {
        return lowestKey;
    }

    public boolean isFull() {
        return (entries.size() >= 2 * k * f);
    }

    /**
	 * Returns the node at a given position
	 * @param pos
	 * @return
	 */
    protected BTreeNode getNode(int pos) {
        if (entries.nodes.elementAt(0) instanceof Leaf) {
            return (Leaf) entries.nodes.elementAt(pos);
        } else {
            return (InternalNode) entries.nodes.elementAt(pos);
        }
    }

    public BTreeNode getBTreeNode(StructuralIdentifier key) {
        int l = 0;
        int r = entries.size();
        int m = (l + r) / 2;
        int prevm = 0;
        int prevprevm = 0;
        StructuralIdentifier did = (StructuralIdentifier) entries.keys.elementAt(m);
        while (m < entries.size() && m > 0 && prevm != m && prevprevm != m) {
            did = (StructuralIdentifier) entries.keys.elementAt(m);
            if ((did).compare(key) < 0) {
                prevprevm = prevm;
                prevm = m;
                r = m;
            } else if ((did).compare(key) > 0) {
                prevprevm = prevm;
                prevm = m;
                l = m;
            } else {
                return this.getNode(m + 1);
            }
            m = (l + r) / 2;
        }
        if (m == 0) {
            did = (StructuralIdentifier) entries.keys.elementAt(0);
            if (key.compare(did) == 1) {
                return this.getNode(0);
            } else if (did.compare(key) == 1) {
                return this.getNode(1);
            } else {
                return this.getNode(1);
            }
        }
        if (prevm == m) {
            if (key.compare(did) == 1) {
                return this.getNode(m);
            } else if (did.compare(key) == 1) {
                return this.getNode(m + 1);
            }
        }
        if (prevprevm == m) {
            did = (StructuralIdentifier) entries.keys.elementAt(m);
            if (key.compare(did) == 1) {
                return this.getNode(m);
            } else if (did.compare(key) == 1) {
                return this.getNode(m + 1);
            }
        }
        return null;
    }

    public void getFirstKeyAfter(StructuralIdentifier key, Leaf[] inLeaf, int[] atPos) {
    }

    public void getLastKeyBefore(StructuralIdentifier key, Leaf[] inLeaf, int[] atPos) {
    }

    private Vector addElementAtPos(Vector a, int pos, Object element) {
        Vector temp = new Vector();
        for (int i = 0; i < pos; i++) {
            temp.addElement(a.elementAt(i));
        }
        temp.addElement(element);
        for (int i = pos; i < a.size(); i++) {
            temp.addElement(a.elementAt(i));
        }
        return temp;
    }
}
