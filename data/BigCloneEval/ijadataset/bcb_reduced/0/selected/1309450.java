package s10;

import java.util.Vector;

public class BST {

    protected BTree t = new BTree();

    protected int crtSize = 0;

    public BST() {
    }

    public BST(Comparable[] tab) {
        Comparable[] tabcpy = new Comparable[tab.length];
        System.arraycopy(tab, 0, tabcpy, 0, tab.length);
        t = optimalBST(tabcpy, 0, tab.length - 1);
        crtSize = tab.length;
    }

    protected BTreeItr locate(Comparable e) {
        Comparable c;
        BTreeItr ti = t.root();
        while (!ti.isBottomArc()) {
            c = (Comparable) (ti.consult());
            if (e.compareTo(c) == 0) break;
            if (e.compareTo(c) < 0) ti = ti.left(); else ti = ti.right();
        }
        return ti;
    }

    public void add(Comparable e) {
        BTreeItr ti = locate(e);
        if (!ti.isBottomArc()) return;
        ti.insert(e);
        crtSize++;
    }

    public void remove(Comparable e) {
        BTreeItr ti = locate(e);
        if (ti.isBottomArc()) return;
        crtSize--;
        while (ti.hasRight()) {
            ti.rotateLeft();
            ti = ti.left();
        }
        BTree l = ti.left().cut();
        ti.cut();
        ti.paste(l);
    }

    public boolean contains(Comparable e) {
        BTreeItr ti = locate(e);
        return !ti.isBottomArc();
    }

    public Comparable[] inRange(Comparable from, Comparable to) {
        Vector v = new Vector();
        inRange(t.root(), from, to, v);
        Object[] tab = v.toArray(new Comparable[0]);
        return (Comparable[]) tab;
    }

    public int size() {
        return crtSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Comparable[] sortedElts() {
        Comparable[] res = new Comparable[size()];
        BTree.collectInorder(t.root(), (Object[]) res);
        return res;
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
        if (left > right) return r;
        int mid = (left + right) / 2;
        ri.insert(sorted[mid]);
        ri.left().paste(optimalBST(sorted, left, mid - 1));
        ri.right().paste(optimalBST(sorted, mid + 1, right));
        return r;
    }

    private static void inRange(BTreeItr w, Comparable a, Comparable b, Vector v) {
        if (w.isBottomArc()) return;
        if (b.compareTo(w.consult()) < 0) {
            inRange(w.left(), a, b, v);
            return;
        }
        if (a.compareTo(w.consult()) > 0) {
            inRange(w.right(), a, b, v);
            return;
        }
        v.add(w.consult());
        inRange(w.left(), a, b, v);
        inRange(w.right(), a, b, v);
    }
}
