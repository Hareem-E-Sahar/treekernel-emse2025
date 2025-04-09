package s04;

public class BST {

    protected BTree t = new BTree();

    protected int crtSize = 0;

    public BST() {
    }

    public BST(Comparable[] tab) {
        t = optimalBST(tab, 0, tab.length - 1);
        crtSize = tab.length;
    }

    protected BTreeItr locate(Comparable e) {
        return locate(new BTreeItr(t), e);
    }

    /**
     * @return itr at element or where the element should be if not existent
     */
    private BTreeItr locate(BTreeItr itr, Comparable e) {
        if (itr.isBottom()) {
            return itr;
        }
        int diff = e.compareTo(itr.consult());
        if (diff == 0) {
            return itr;
        } else {
            return locate((diff > 0 ? itr.right() : itr.left()), e);
        }
    }

    public void add(Comparable e) {
        BTreeItr itr = locate(e);
        if (itr.isBottom()) {
            itr.insert(e);
            crtSize++;
        }
    }

    public void remove(Comparable e) {
        BTreeItr itr = locate(e);
        if (!itr.isBottom()) {
            while (itr.hasRight()) {
                itr.rotateLeft();
                itr = itr.left();
            }
            itr.paste(itr.left().cut());
            crtSize--;
        }
    }

    public boolean contains(Comparable e) {
        BTreeItr ti = locate(e);
        return !ti.isBottom();
    }

    public int size() {
        return crtSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Comparable minElt() {
        return (Comparable) (t.root().leftMost().up().consult());
    }

    public Comparable maxElt() {
        return (Comparable) (t.root().rightMost().up().consult());
    }

    public String toString() {
        return "" + t;
    }

    private static BTree optimalBST(Comparable[] sorted, int left, int right) {
        BTree r = new BTree();
        BTreeItr ri = r.root();
        int middleIndex = (right + left) / 2;
        ri.insert(sorted[middleIndex]);
        if (left < middleIndex) {
            ri.left().paste(optimalBST(sorted, left, middleIndex - 1));
        }
        if (right > middleIndex) {
            ri.right().paste(optimalBST(sorted, middleIndex + 1, right));
        }
        return r;
    }
}
