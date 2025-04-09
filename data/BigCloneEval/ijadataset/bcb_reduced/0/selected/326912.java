package s24;

import java.util.List;
import java.util.Vector;

public class BST<E extends Comparable<E>> {

    protected BTree<E> t = new BTree<E>();

    protected int crtSize = 0;

    public BST() {
    }

    public BST(E[] tab) {
        t = optimalBST(tab, 0, tab.length - 1);
        crtSize = tab.length;
    }

    protected BTreeItr<E> locate(E e) {
        BTreeItr<E> ti = t.root();
        while (!ti.isBottom()) {
            E c = ti.consult();
            if (e.compareTo(c) == 0) break;
            if (e.compareTo(c) < 0) ti = ti.left(); else ti = ti.right();
        }
        return ti;
    }

    public void add(E e) {
        BTreeItr<E> ti = locate(e);
        if (!ti.isBottom()) return;
        ti.insert(e);
        crtSize++;
    }

    public void remove(E e) {
        BTreeItr<E> ti = locate(e);
        if (ti.isBottom()) return;
        crtSize--;
        while (ti.hasRight()) {
            ti.rotateLeft();
            ti = ti.left();
        }
        BTree<E> l = ti.left().cut();
        ti.cut();
        ti.paste(l);
    }

    public boolean contains(E e) {
        BTreeItr<E> ti = locate(e);
        return !ti.isBottom();
    }

    public List<E> inRange(E from, E to) {
        Vector<E> v = new Vector<E>();
        inRange(t.root(), from, to, v);
        return v;
    }

    public int size() {
        return crtSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public E minElt() {
        return (t.root().leftMost().up().consult());
    }

    public E maxElt() {
        return (t.root().rightMost().up().consult());
    }

    public String toString() {
        return "" + t;
    }

    private BTree<E> optimalBST(E[] sorted, int left, int right) {
        BTree<E> r = new BTree<E>();
        BTreeItr<E> ri = r.root();
        if (left > right) return r;
        int mid = (left + right) / 2;
        ri.insert(sorted[mid]);
        ri.left().paste(optimalBST(sorted, left, mid - 1));
        ri.right().paste(optimalBST(sorted, mid + 1, right));
        return r;
    }

    private void inRange(BTreeItr<E> w, E a, E b, Vector<E> v) {
        if (w.isBottom()) return;
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
