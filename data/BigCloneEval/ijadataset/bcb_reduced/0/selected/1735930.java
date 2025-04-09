package org.exist.dom;

import java.util.Iterator;
import org.exist.util.FastQSort;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.SequenceIterator;
import org.w3c.dom.Node;

public class ArraySet extends AbstractNodeSetBase {

    protected int counter = 0;

    protected int length;

    protected NodeProxy nodes[];

    protected boolean sorted = false;

    private DocumentOrderComparator docOrderComparator = new DocumentOrderComparator();

    /**
	 *  Constructor for the ArraySet object
	 *
	 *@param  initialCapacity  Description of the Parameter
	 */
    public ArraySet(int initialCapacity) {
        nodes = new NodeProxy[initialCapacity];
        length = initialCapacity;
    }

    private static final boolean getParentSet(NodeProxy[] nl, int len) {
        int level;
        boolean foundValid = false;
        long pid;
        NodeProxy node;
        for (int i = 0; i < len; i++) {
            node = nl[i];
            if (node == null) continue;
            if (node.gid < 0) {
                nl[i] = null;
                continue;
            }
            pid = XMLUtil.getParentId(node);
            node.gid = pid;
            foundValid = true;
        }
        return foundValid;
    }

    /**
	 *  BinarySearch algorithm
	 *
	 *@param  items    Description of the Parameter
	 *@param  low      Description of the Parameter
	 *@param  high     Description of the Parameter
	 *@param  cmpItem  Description of the Parameter
	 *@return          Description of the Return Value
	 */
    private static final int search(NodeProxy[] items, int low, int high, NodeProxy cmpItem) {
        int mid;
        int cmp;
        while (low <= high) {
            mid = (low + high) / 2;
            cmp = items[mid].compareTo(cmpItem);
            if (cmp == 0) return mid;
            if (cmp > 0) high = mid - 1; else low = mid + 1;
        }
        return -1;
    }

    /**
		 *  BinarySearch algorithm
		 *
		 *@param  items    Description of the Parameter
		 *@param  low      Description of the Parameter
		 *@param  high     Description of the Parameter
		 *@param  cmpItem  Description of the Parameter
		 *@return          Description of the Return Value
		 */
    private static final int search(NodeProxy[] items, int low, int high, DocumentImpl cmpDoc, long gid) {
        int mid;
        int cmp;
        while (low <= high) {
            mid = (low + high) / 2;
            if (items[mid].getDocument().docId == cmpDoc.docId) {
                if (items[mid].gid == gid) return mid; else if (items[mid].gid > gid) high = mid - 1; else low = mid + 1;
            } else if (items[mid].getDocument().docId > cmpDoc.docId) high = mid - 1; else low = mid + 1;
        }
        return -1;
    }

    private static final NodeSet searchRange(NodeProxy[] items, int low, int high, NodeProxy lower, NodeProxy upper) {
        ArraySet result = new ArraySet(100);
        return searchRange(result, items, low, high, lower, upper);
    }

    /**
	 *  get all nodes contained in the set, which are greater or equal to lower
	 *  and less or equal to upper. This is basically needed by all functions
	 *  that determine the position of a node in the node set.
	 *
	 *@param  items  Description of the Parameter
	 *@param  low    Description of the Parameter
	 *@param  high   Description of the Parameter
	 *@param  lower  Description of the Parameter
	 *@param  upper  Description of the Parameter
	 *@return        Description of the Return Value
	 */
    private static final NodeSet searchRange(ArraySet result, NodeProxy[] items, int low, int high, NodeProxy lower, NodeProxy upper) {
        int mid = 0;
        int max = high;
        int cmp;
        while (low <= high) {
            mid = (low + high) / 2;
            cmp = items[mid].compareTo(lower);
            if (cmp == 0) break;
            if (cmp > 0) high = mid - 1; else low = mid + 1;
        }
        while (mid > 0 && items[mid].compareTo(lower) > 0) mid--;
        if (items[mid].compareTo(lower) < 0) mid++;
        while (mid <= max && items[mid].compareTo(upper) <= 0) result.add(items[mid++]);
        result.setIsSorted(true);
        return result;
    }

    public void add(NodeProxy proxy) {
        if (proxy == null) return;
        if (counter < length) nodes[counter++] = proxy; else {
            int grow = (length < 10) ? 50 : length >> 1;
            NodeProxy temp[] = new NodeProxy[length + grow];
            System.arraycopy(nodes, 0, temp, 0, length);
            length = length + grow;
            nodes = temp;
            nodes[counter++] = proxy;
        }
        sorted = false;
    }

    public void addAll(NodeSet other) {
        for (Iterator i = other.iterator(); i.hasNext(); ) add((NodeProxy) i.next());
    }

    public boolean hasIndex() {
        for (int i = 0; i < counter; i++) if (!nodes[i].hasIndex()) return false;
        return true;
    }

    public boolean contains(DocumentImpl doc, long nodeId) {
        sort();
        NodeProxy p = new NodeProxy(doc, nodeId);
        return contains(p);
    }

    public boolean contains(NodeProxy proxy) {
        sort();
        return -1 < search(nodes, 0, counter - 1, proxy);
    }

    public NodeProxy get(DocumentImpl doc, long nodeId) {
        sort();
        int pos = search(nodes, 0, counter - 1, doc, nodeId);
        if (pos < 0) {
            return null;
        }
        return nodes[pos];
    }

    public NodeProxy get(NodeProxy p) {
        sort();
        int pos = search(nodes, 0, counter - 1, p);
        if (pos < 0) {
            return null;
        }
        return nodes[pos];
    }

    public NodeProxy get(int pos) {
        if (pos >= counter || pos < 0) return null;
        sort();
        return nodes[pos];
    }

    public Item itemAt(int pos) {
        return get(pos);
    }

    public NodeSet getChildrenX(NodeSet ancestors, int mode, boolean rememberContext) {
        if (!(ancestors instanceof ArraySet)) return super.selectParentChild(ancestors, mode, rememberContext);
        ArraySet al = (ArraySet) ancestors;
        if (al.counter == 0 || counter == 0) {
            return new ArraySet(1);
        }
        long start = System.currentTimeMillis();
        sort();
        al.sort();
        final ArraySet result = new ArraySet(al.counter);
        NodeProxy[] dl = null;
        if (mode == DESCENDANT) {
            dl = copyNodeSet(al, this);
            result.sorted = true;
        } else dl = nodes;
        int ax = 0;
        int dx = 0;
        final int dlen = dl.length;
        int cmp;
        getParentSet(dl, dlen);
        while (dx < dlen) {
            while (dl[dx] == null && ++dx < dlen) ;
            if (dx == dlen) break;
            cmp = dl[dx].compareTo(al.nodes[ax]);
            if (cmp > 0) {
                if (ax < al.counter - 1) ax++; else break;
            } else if (cmp < 0) dx++; else {
                switch(mode) {
                    case ANCESTOR:
                        al.nodes[ax].addMatches(dl[dx]);
                        if (rememberContext) al.nodes[ax].addContextNode(dl[dx]); else al.nodes[ax].copyContext(dl[dx]);
                        result.add(al.nodes[ax]);
                        break;
                    case DESCENDANT:
                        nodes[dx].addMatches(al.nodes[ax]);
                        if (rememberContext) nodes[dx].addContextNode(al.nodes[ax]); else nodes[dx].copyContext(al.nodes[ax]);
                        result.add(nodes[dx]);
                        break;
                }
                dx++;
            }
        }
        LOG.debug("getChildren found " + result.getLength() + " in " + (System.currentTimeMillis() - start) + "ms.");
        return result;
    }

    public NodeSet selectAncestorDescendant(NodeSet other, int mode) {
        return selectAncestorDescendant(other, mode, false);
    }

    public NodeSet selectAncestorDescendant(NodeSet other, int mode, boolean includeSelf) {
        return selectAncestorDescendant(other, mode, includeSelf, false);
    }

    /**
	 *  For a given set of potential ancestor nodes, get the
	 * descendants in this node set
	 *
	 *@param  al    Description of the Parameter
	 *@param  mode  Description of the Parameter
	 *@return       The descendants value
	 */
    public NodeSet getDescendantsX(NodeSet other, int mode, boolean includeSelf, boolean rememberContext) {
        if (!(other instanceof ArraySet)) return super.selectAncestorDescendant(other, mode, includeSelf, rememberContext);
        ArraySet al = (ArraySet) other;
        if (al.counter == 0 || counter == 0) return NodeSet.EMPTY_SET;
        long start = System.currentTimeMillis();
        al.sort();
        sort();
        NodeProxy[] dl = null;
        if (mode == DESCENDANT) {
            dl = copyNodeSet(al, this);
        } else dl = nodes;
        ArraySet result = new ArraySet(dl.length);
        int ax;
        int dx;
        int cmp;
        NodeProxy node;
        final int dlen = dl.length;
        boolean more = includeSelf ? true : getParentSet(dl, dlen);
        while (more) {
            ax = 0;
            dx = 0;
            while (dx < dlen) {
                if (dl[dx] == null) {
                    dx++;
                    continue;
                }
                cmp = dl[dx].compareTo(al.nodes[ax]);
                if (cmp > 0) {
                    if (ax < al.counter - 1) ax++; else break;
                } else if (cmp < 0) dx++; else {
                    switch(mode) {
                        case ANCESTOR:
                            al.nodes[ax].addMatches(dl[dx]);
                            if (rememberContext) al.nodes[ax].addContextNode(nodes[dx]); else al.nodes[ax].copyContext(nodes[dx]);
                            result.add(al.nodes[ax]);
                            break;
                        case DESCENDANT:
                            nodes[dx].addMatches(al.nodes[ax]);
                            if (rememberContext) nodes[dx].addContextNode(al.nodes[ax]); else nodes[dx].copyContext(al.nodes[ax]);
                            result.add(nodes[dx]);
                            break;
                    }
                    dx++;
                }
            }
            more = getParentSet(dl, dlen);
        }
        LOG.debug("getDescendants found " + result.getLength() + " in " + (System.currentTimeMillis() - start) + "ms.");
        return result;
    }

    /**
		 *  For a given set of potential ancestor nodes, get the
		 * descendants in this node set
		 *
		 *@param  al    Description of the Parameter
		 *@param  mode  Description of the Parameter
		 *@return       The descendants value
		 */
    public NodeSet selectAncestors(NodeSet other, boolean includeSelf, boolean rememberContext) {
        if (!(other instanceof ArraySet)) return super.selectAncestors(other, includeSelf, rememberContext);
        ArraySet al = (ArraySet) other;
        if (al.counter == 0 || counter == 0) return new ArraySet(1);
        long start = System.currentTimeMillis();
        al.sort();
        sort();
        NodeProxy[] dl = copyNodeSet(al, this);
        NodeSet result = new ArraySet(getLength());
        NodeProxy temp;
        int ax;
        int dx;
        int cmp;
        final int dlen = dl.length;
        boolean more = includeSelf ? true : getParentSet(dl, dlen);
        while (more) {
            ax = 0;
            dx = 0;
            while (dx < dlen) {
                if (dl[dx] == null) {
                    dx++;
                    System.out.println("skipping " + dx);
                    continue;
                }
                cmp = dl[dx].compareTo(al.nodes[ax]);
                if (cmp > 0) {
                    if (ax < al.counter - 1) ax++; else break;
                } else if (cmp < 0) dx++; else {
                    if ((temp = result.get(al.nodes[ax])) == null) {
                        al.nodes[ax].addMatches(nodes[dx]);
                        if (rememberContext) al.nodes[ax].addContextNode(nodes[dx]); else al.nodes[ax].copyContext(nodes[dx]);
                        result.add(al.nodes[ax]);
                    } else if (rememberContext) temp.addContextNode(nodes[dx]);
                    dx++;
                }
            }
            more = getParentSet(dl, dlen);
        }
        LOG.debug("getAncestors found " + result.getLength() + " in " + (System.currentTimeMillis() - start) + "ms.");
        return result;
    }

    public int getLength() {
        return counter;
    }

    public NodeSet getRange(NodeProxy lower, NodeProxy upper) {
        sort();
        return searchRange(nodes, 0, counter - 1, lower, upper);
    }

    public NodeSet getRange(DocumentImpl doc, long lower, long upper) {
        return getRange(new NodeProxy(doc, lower), new NodeProxy(doc, upper));
    }

    protected boolean isSorted() {
        return sorted;
    }

    public Node item(int pos) {
        if (pos >= counter || pos < 0) return null;
        sort();
        NodeProxy p = nodes[pos];
        return p.getNode();
    }

    public Iterator iterator() {
        sort();
        return new ArraySetIterator();
    }

    public SequenceIterator iterate() {
        sortInDocumentOrder();
        return new ArraySequenceIterator();
    }

    public SequenceIterator unorderedIterator() {
        sort();
        return new ArraySequenceIterator();
    }

    public int position(NodeImpl test) {
        sort();
        NodeProxy p = new NodeProxy(test.ownerDocument, test.getGID());
        return search(nodes, 0, counter - 1, p);
    }

    public int position(NodeProxy proxy) {
        sort();
        return search(nodes, 0, counter - 1, proxy);
    }

    public void remove(NodeProxy node) {
        long start = System.currentTimeMillis();
        int pos = search(nodes, 0, counter - 1, node);
        if (pos < 0) return;
        NodeProxy[] temp = new NodeProxy[counter];
        System.arraycopy(nodes, 0, temp, 0, pos - 2);
        System.arraycopy(nodes, pos + 1, temp, pos + 1, temp.length - pos - 1);
        nodes = temp;
        counter--;
        LOG.debug("removal of node took " + (System.currentTimeMillis() - start));
    }

    public DocumentSet getDocumentSet() {
        DocumentSet docs = new DocumentSet();
        DocumentImpl lastDoc = null;
        for (int i = 0; i < counter; i++) {
            if (lastDoc == null || lastDoc.getDocId() != nodes[i].getDocument().getDocId()) {
                docs.add(nodes[i].getDocument(), false);
            }
            lastDoc = nodes[i].getDocument();
        }
        return docs;
    }

    public void setIsSorted(boolean sorted) {
    }

    public void sort() {
        if (this.sorted || counter < 2) return;
        FastQSort.sort(nodes, 0, counter - 1);
        removeDuplicateNodes();
        this.sorted = true;
    }

    public void sortInDocumentOrder() {
        if (counter < 2) return;
        FastQSort.sort(nodes, docOrderComparator, 0, counter - 1);
        removeDuplicateNodes();
        this.sorted = false;
    }

    private final void removeDuplicateNodes() {
        int j = 0;
        for (int i = 1; i < counter; i++) {
            if (nodes[i].compareTo(nodes[j]) != 0) {
                if (i != ++j) nodes[j] = nodes[i];
            }
        }
        counter = ++j;
    }

    private static final NodeProxy[] copyNodeSet(ArraySet al, ArraySet dl) {
        int ax = 0, dx = 0;
        int ad = al.nodes[ax].getDocument().docId, dd = dl.nodes[dx].getDocument().docId;
        final int alen = al.counter - 1, dlen = dl.counter - 1;
        final NodeProxy[] ol = new NodeProxy[dl.counter];
        while (true) {
            if (ad < dd) {
                if (ax < alen) {
                    ++ax;
                    ad = al.nodes[ax].getDocument().docId;
                } else break;
            } else if (ad > dd) {
                if (dx < dlen) {
                    ol[dx] = null;
                    ++dx;
                    dd = dl.nodes[dx].getDocument().docId;
                } else break;
            } else {
                ol[dx] = new NodeProxy(dl.nodes[dx]);
                if (dx < dlen) {
                    ++dx;
                    dd = dl.nodes[dx].getDocument().docId;
                } else break;
            }
        }
        return ol;
    }

    private static final void trimNodeSet(ArraySet al, ArraySet dl) {
        int ax = 0, dx = 0;
        int ad = al.nodes[ax].getDocument().docId, dd = dl.nodes[dx].getDocument().docId;
        int count = 0;
        final int alen = al.counter - 1, dlen = dl.counter - 1;
        while (true) {
            if (ad < dd) {
                if (ax < alen) {
                    ++ax;
                    ad = al.nodes[ax].getDocument().docId;
                } else break;
            } else if (ad > dd) {
                if (dx < dlen) {
                    ++dx;
                    dd = dl.nodes[dx].getDocument().docId;
                } else break;
            } else {
                if (dx < dlen) {
                    ++dx;
                    count++;
                    dd = dl.nodes[dx].getDocument().docId;
                } else break;
            }
        }
        System.out.println("dl = " + dlen + "; copy = " + count);
    }

    private class ArraySetIterator implements Iterator {

        private int pos = 0;

        public boolean hasNext() {
            return (pos < counter) ? true : false;
        }

        public Object next() {
            return hasNext() ? nodes[pos++] : null;
        }

        public void remove() {
        }
    }

    private class ArraySequenceIterator implements SequenceIterator {

        private int pos = 0;

        public boolean hasNext() {
            return (pos < counter) ? true : false;
        }

        public Item nextItem() {
            return (pos < counter) ? nodes[pos++] : null;
        }
    }

    public int compare(int a, int b) {
        NodeProxy anode = nodes[a], bnode = nodes[b];
        if (anode.getDocument().docId == bnode.getDocument().docId) {
            return anode.gid == bnode.gid ? 0 : (anode.gid < bnode.gid ? -1 : 1);
        }
        return anode.getDocument().docId < bnode.getDocument().docId ? -1 : 1;
    }

    public void swap(int a, int b) {
        NodeProxy t = nodes[a];
        nodes[a] = nodes[b];
        nodes[b] = nodes[a];
    }

    public Comparable[] array() {
        return nodes;
    }
}
