package org.apache.batik.ext.awt.geom;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * RectListManager is a class to manage a list of rectangular regions.
 * This class contains methods to add new rectangles to the List, to
 * merge rectangles in the list (based on a cost function), and
 * functions to subract one RectListManager from another.  The main
 * purpose of this class is to manage dirty regions on a display (for
 * this reason it uses Rectangle not Rectangle2D).
 *
 * @author <a href="mailto:deweese@apache.org">Thomas DeWeese</a>
 * @version $Id: RectListManager.java,v 1.1 2005/11/21 09:51:24 dev Exp $ */
public class RectListManager implements Collection {

    Rectangle[] rects = null;

    int size = 0;

    Rectangle bounds = null;

    /**
     * The comparator used to sort the elements of this List.
     * Sorts on x value of Rectangle.
     */
    public static Comparator comparator = new RectXComparator();

    /**
     * Construct a <tt>RectListManager</tt> from a Collection of Rectangles
     * @param rects Collection that must only contain rectangles.
     */
    public RectListManager(Collection rects) {
        this.rects = new Rectangle[rects.size()];
        Iterator i = rects.iterator();
        int j = 0;
        while (i.hasNext()) this.rects[j++] = (Rectangle) i.next();
        this.size = this.rects.length;
        Arrays.sort(this.rects, comparator);
    }

    /**
     * Construct a <tt>RectListManager</tt> from an Array of 
     * <tt>Rectangles</tt>
     * @param rects Array of <tt>Rectangles</tt>, must not contain
     *              any null entries.
     */
    public RectListManager(Rectangle[] rects) {
        this(rects, 0, rects.length);
    }

    /**
     * Construct a <tt>RectListManager</tt> from an Array of 
     * <tt>Rectangles</tt>
     * @param rects Array of <tt>Rectangles</tt>, must not contain
     *              any null entries in the range [off, off+sz-1].
     * @param off   The offset to start copying from in rects.
     * @param sz    The number of entries to copy from rects.
     */
    public RectListManager(Rectangle[] rects, int off, int sz) {
        this.size = sz;
        this.rects = new Rectangle[sz];
        System.arraycopy(rects, off, this.rects, 0, sz);
        Arrays.sort(this.rects, comparator);
    }

    /**
     * Construct a <tt>RectListManager</tt> from another 
     * <tt>RectListManager</tt> (data is copied).
     * @param rlm RectListManager to copy.
     */
    public RectListManager(RectListManager rlm) {
        this(rlm.rects);
    }

    /**
     * Construct a <tt>RectListManager</tt> with one rectangle
     * @param rect The rectangle to put in this rlm.
     */
    public RectListManager(Rectangle rect) {
        this();
        add(rect);
    }

    /**
     * Construct an initially empty <tt>RectListManager</tt>.
     */
    public RectListManager() {
        this.rects = new Rectangle[10];
        size = 0;
    }

    /**
     * Construct an initially empty <tt>RectListManager</tt>,
     * with initial <tt>capacity</tt>.
     * @param capacity The inital capacity for the list.  Setting
     *                 this appropriately can save reallocations.
     */
    public RectListManager(int capacity) {
        this.rects = new Rectangle[capacity];
    }

    public Rectangle getBounds() {
        if (bounds != null) return bounds;
        if (size == 0) return null;
        bounds = new Rectangle(rects[0]);
        for (int i = 1; i < size; i++) {
            Rectangle r = rects[i];
            if (r.x < bounds.x) {
                bounds.width = bounds.x + bounds.width - r.x;
                bounds.x = r.x;
            }
            if (r.y < bounds.y) {
                bounds.height = bounds.y + bounds.height - r.y;
                bounds.y = r.y;
            }
            if (r.x + r.width > bounds.x + bounds.width) bounds.width = r.x + r.width - bounds.x;
            if (r.y + r.height > bounds.y + bounds.height) bounds.height = r.y + r.height - bounds.y;
        }
        return bounds;
    }

    /**
     * Standard <tt>Object</tt> clone method.
     */
    public Object clone() {
        return copy();
    }

    /**
     * Similar to clone only strongly typed
     */
    public RectListManager copy() {
        return new RectListManager(rects);
    }

    /**
     * Returns the number of elements currently stored in this collection.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this collection contains no elements.
     */
    public boolean isEmpty() {
        return (size == 0);
    }

    public void clear() {
        for (int i = 0; i < size; i++) rects[i] = null;
        size = 0;
    }

    /**
     * Returns an iterator over the elements in this collection
     */
    public Iterator iterator() {
        return new RLMIterator();
    }

    /**
     * Returns a list iterator of the elements in this list 
     * (in proper sequence).
     */
    public ListIterator listIterator() {
        return new RLMIterator();
    }

    public Object[] toArray() {
        Object[] ret = new Rectangle[size];
        System.arraycopy(rects, 0, ret, 0, size);
        return ret;
    }

    public Object[] toArray(Object[] a) {
        Class t = a.getClass().getComponentType();
        if ((t != Object.class) & (t != Rectangle.class)) {
            for (int i = 0; i < a.length; i++) a[i] = null;
            return a;
        }
        if (a.length < size) a = new Rectangle[size];
        System.arraycopy(rects, 0, a, 0, size);
        for (int i = size; i < a.length; i++) a[i] = null;
        return a;
    }

    public boolean add(Object o) {
        add((Rectangle) o);
        return true;
    }

    /**
     * Ensures that this collection contains the specified element
     * @param rect The rectangle to add
     */
    public void add(Rectangle rect) {
        add(rect, 0, size - 1);
    }

    /**
     * Ensures that this collection contains the specified element
     * l is the lower bound index for insertion r is upper
     * bound index for insertion.
     * @param rect The rectangle to add
     * @param l the lowest possible index for a rect with
     *          greater 'x' coord.
     * @param r the highest possible index for a rect with
     *          greater 'x' coord.
     */
    protected void add(Rectangle rect, int l, int r) {
        ensureCapacity(size + 1);
        int idx = l;
        while (l <= r) {
            idx = (l + r) / 2;
            while ((rects[idx] == null) && (idx < r)) idx++;
            if (rects[idx] == null) {
                r = (l + r) / 2;
                idx = (l + r) / 2;
                if (l > r) idx = l;
                while ((rects[idx] == null) && (idx > l)) idx--;
                if (rects[idx] == null) {
                    rects[idx] = rect;
                    return;
                }
            }
            if (rect.x == rects[idx].x) break;
            if (rect.x < rects[idx].x) {
                if (idx == 0) break;
                if ((rects[idx - 1] != null) && (rect.x >= rects[idx - 1].x)) break;
                r = idx - 1;
            } else {
                if (idx == size - 1) {
                    idx++;
                    break;
                }
                if ((rects[idx + 1] != null) && (rect.x <= rects[idx + 1].x)) {
                    idx++;
                    break;
                }
                l = idx + 1;
            }
        }
        if (idx < size) {
            System.arraycopy(rects, idx, rects, idx + 1, size - idx);
        }
        rects[idx] = rect;
        size++;
    }

    public boolean addAll(Collection c) {
        if (c instanceof RectListManager) {
            add((RectListManager) c);
        } else {
            add(new RectListManager(c));
        }
        return (c.size() != 0);
    }

    public boolean contains(Object o) {
        Rectangle rect = (Rectangle) o;
        int l = 0, r = size - 1, idx = 0;
        while (l <= r) {
            idx = (l + r) / 2;
            if (rect.x == rects[idx].x) break;
            if (rect.x < rects[idx].x) {
                if (idx == 0) break;
                if (rect.x >= rects[idx - 1].x) break;
                r = idx - 1;
            } else {
                if (idx == size - 1) {
                    idx++;
                    break;
                }
                if (rect.x <= rects[idx + 1].x) {
                    idx++;
                    break;
                }
                l = idx + 1;
            }
        }
        if (rects[idx].x != rect.x) return false;
        for (int i = idx; i >= 0; i--) {
            if (rects[idx].equals(rect)) return true;
            if (rects[idx].x != rect.x) break;
        }
        for (int i = idx + 1; i < size; i++) {
            if (rects[idx].equals(rect)) return true;
            if (rects[idx].x != rect.x) break;
        }
        return false;
    }

    /** 
     * Returns true if this collection contains all of the elements in
     * the specified collection.
     */
    public boolean containsAll(Collection c) {
        if (c instanceof RectListManager) return containsAll((RectListManager) c);
        return containsAll(new RectListManager(c));
    }

    public boolean containsAll(RectListManager rlm) {
        int x, xChange = 0;
        for (int j = 0, i = 0; j < rlm.size; j++) {
            i = xChange;
            while (rects[i].x < rlm.rects[j].x) {
                i++;
                if (i == size) return false;
            }
            xChange = i;
            x = rects[i].x;
            while (!rlm.rects[j].equals(rects[i])) {
                i++;
                if (i == size) return false;
                if (x != rects[i].x) return false;
            }
        }
        return true;
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present.
     * @param o Object to remove an matching instance of.
     */
    public boolean remove(Object o) {
        return remove((Rectangle) o);
    }

    /**
     * Removes a single instance of the specified Rectangle from this
     * collection, if it is present.
     * @param rect Rectangle to remove an matching instance of.
     */
    public boolean remove(Rectangle rect) {
        int l = 0, r = size - 1, idx = 0;
        while (l <= r) {
            idx = (l + r) / 2;
            if (rect.x == rects[idx].x) break;
            if (rect.x < rects[idx].x) {
                if (idx == 0) break;
                if (rect.x >= rects[idx - 1].x) break;
                r = idx - 1;
            } else {
                if (idx == size - 1) {
                    idx++;
                    break;
                }
                if (rect.x <= rects[idx + 1].x) {
                    idx++;
                    break;
                }
                l = idx + 1;
            }
        }
        if (rects[idx].x != rect.x) return false;
        for (int i = idx; i >= 0; i--) {
            if (rects[idx].equals(rect)) {
                System.arraycopy(rects, idx + 1, rects, idx, size - idx);
                size--;
                return true;
            }
            if (rects[idx].x != rect.x) break;
        }
        for (int i = idx + 1; i < size; i++) {
            if (rects[idx].equals(rect)) {
                System.arraycopy(rects, idx + 1, rects, idx, size - idx);
                size--;
                return true;
            }
            if (rects[idx].x != rect.x) break;
        }
        return false;
    }

    public boolean removeAll(Collection c) {
        if (c instanceof RectListManager) return removeAll((RectListManager) c);
        return removeAll(new RectListManager(c));
    }

    public boolean removeAll(RectListManager rlm) {
        int x, xChange = 0;
        boolean ret = false;
        for (int j = 0, i = 0; j < rlm.size; j++) {
            i = xChange;
            while ((rects[i] == null) || (rects[i].x < rlm.rects[j].x)) {
                i++;
                if (i == size) break;
            }
            if (i == size) break;
            xChange = i;
            x = rects[i].x;
            while (true) {
                if (rects[i] == null) {
                    i++;
                    if (i == size) break;
                    continue;
                }
                if (rlm.rects[j].equals(rects[i])) {
                    rects[i] = null;
                    ret = true;
                }
                i++;
                if (i == size) break;
                if (x != rects[i].x) break;
            }
        }
        if (ret) {
            int j = 0, i = 0;
            while (i < size) {
                if (rects[i] != null) rects[j++] = rects[i];
                i++;
            }
            size = j;
        }
        return ret;
    }

    public boolean retainAll(Collection c) {
        if (c instanceof RectListManager) return retainAll((RectListManager) c);
        return retainAll(new RectListManager(c));
    }

    public boolean retainAll(RectListManager rlm) {
        int x, xChange = 0;
        boolean ret = false;
        for (int j = 0, i = 0; j < size; j++) {
            i = xChange;
            while (rlm.rects[i].x < rects[j].x) {
                i++;
                if (i == rlm.size) break;
            }
            if (i == rlm.size) {
                ret = true;
                for (int k = j; k < size; k++) rects[k] = null;
                size = j;
                break;
            }
            xChange = i;
            x = rlm.rects[i].x;
            while (true) {
                if (rects[j].equals(rlm.rects[i])) break;
                i++;
                if ((i == rlm.size) || (x != rlm.rects[i].x)) {
                    rects[j] = null;
                    ret = true;
                    break;
                }
            }
        }
        if (ret) {
            int j = 0, i = 0;
            while (i < size) {
                if (rects[i] != null) rects[j++] = rects[i];
                i++;
            }
            size = j;
        }
        return ret;
    }

    /**
     * Adds the contents of <tt>rlm</tt> to this RectListManager.  No
     * collapsing of rectangles is done here the contents are simply
     * added (you should generally call 'mergeRects' some time after
     * this operation before using the contents of this
     * RectListManager.
     * @param rlm The RectListManager to add the contents of.  */
    public void add(RectListManager rlm) {
        if (rlm.size == 0) return;
        Rectangle[] dst = rects;
        if (rects.length < (size + rlm.size)) {
            dst = new Rectangle[size + rlm.size];
        }
        if (size == 0) {
            System.arraycopy(rlm.rects, 0, dst, size, rlm.size);
            size = rlm.size;
            return;
        }
        Rectangle[] src1 = rlm.rects;
        int src1Sz = rlm.size;
        int src1I = src1Sz - 1;
        Rectangle[] src2 = rects;
        int src2Sz = size;
        int src2I = src2Sz - 1;
        int dstI = size + rlm.size - 1;
        int x1 = src1[src1I].x;
        int x2 = src2[src2I].x;
        while (dstI >= 0) {
            if (x1 <= x2) {
                dst[dstI] = src2[src2I];
                if (src2I == 0) {
                    System.arraycopy(src1, 0, dst, 0, src1I + 1);
                    break;
                }
                src2I--;
                x2 = src2[src2I].x;
            } else {
                dst[dstI] = src1[src1I];
                if (src1I == 0) {
                    System.arraycopy(src2, 0, dst, 0, src2I + 1);
                    break;
                }
                src1I--;
                x1 = src1[src1I].x;
            }
            dstI--;
        }
        rects = dst;
        size += rlm.size;
    }

    public void mergeRects(int overhead, int lineOverhead) {
        if (size == 0) return;
        Rectangle r, cr, mr;
        int cost1, cost2, cost3;
        mr = new Rectangle();
        Rectangle[] splits = new Rectangle[4];
        for (int j, i = 0; i < size; i++) {
            r = rects[i];
            if (r == null) continue;
            cost1 = (overhead + (r.height * lineOverhead) + (r.height * r.width));
            do {
                int maxX = r.x + r.width + overhead / r.height;
                for (j = i + 1; j < size; j++) {
                    cr = rects[j];
                    if ((cr == null) || (cr == r)) continue;
                    if (cr.x >= maxX) {
                        j = size;
                        break;
                    }
                    cost2 = (overhead + (cr.height * lineOverhead) + (cr.height * cr.width));
                    mr = r.union(cr);
                    cost3 = (overhead + (mr.height * lineOverhead) + (mr.height * mr.width));
                    if (cost3 <= cost1 + cost2) {
                        r = rects[i] = mr;
                        rects[j] = null;
                        cost1 = cost3;
                        j = -1;
                        break;
                    }
                    if (!r.intersects(cr)) continue;
                    splitRect(cr, r, splits);
                    int splitCost = 0;
                    int l = 0;
                    for (int k = 0; k < 4; k++) {
                        if (splits[k] != null) {
                            Rectangle sr = splits[k];
                            if (k < 3) splits[l++] = sr;
                            splitCost += (overhead + (sr.height * lineOverhead) + (sr.height * sr.width));
                        }
                    }
                    if (splitCost >= cost2) continue;
                    if (l == 0) {
                        rects[j] = null;
                        if (splits[3] != null) add(splits[3], j, size - 1);
                        continue;
                    }
                    rects[j] = splits[0];
                    if (l > 1) insertRects(splits, 1, j + 1, l - 1);
                    if (splits[3] != null) add(splits[3], j, size - 1);
                }
            } while (j != size);
        }
        int j = 0, i = 0;
        float area = 0;
        while (i < size) {
            if (rects[i] != null) {
                r = rects[i];
                rects[j++] = r;
                area += overhead + (r.height * lineOverhead) + (r.height * r.width);
            }
            i++;
        }
        size = j;
        r = getBounds();
        if (r == null) return;
        if (overhead + (r.height * lineOverhead) + (r.height * r.width) < area) {
            rects[0] = r;
            size = 1;
        }
    }

    public void subtract(RectListManager rlm, int overhead, int lineOverhead) {
        Rectangle r, sr;
        int cost;
        int jMin = 0;
        Rectangle[] splits = new Rectangle[4];
        for (int i = 0; i < size; i++) {
            r = rects[i];
            cost = (overhead + (r.height * lineOverhead) + (r.height * r.width));
            for (int j = jMin; j < rlm.size; j++) {
                sr = rlm.rects[j];
                if (sr.x + sr.width < r.x) {
                    if (j == jMin) jMin++;
                    continue;
                }
                if (sr.x > r.x + r.width) break;
                if (!r.intersects(sr)) continue;
                splitRect(r, sr, splits);
                int splitCost = 0;
                Rectangle tmpR;
                for (int k = 0; k < 4; k++) {
                    tmpR = splits[k];
                    if (tmpR != null) splitCost += (overhead + (tmpR.height * lineOverhead) + (tmpR.height * tmpR.width));
                }
                if (splitCost >= cost) continue;
                int l = 0;
                for (int k = 0; k < 3; k++) {
                    if (splits[k] != null) splits[l++] = splits[k];
                }
                if (l == 0) {
                    rects[i].width = 0;
                    if (splits[3] != null) add(splits[3], i, size - 1);
                    break;
                }
                r = splits[0];
                rects[i] = r;
                cost = (overhead + (r.height * lineOverhead) + (r.height * r.width));
                if (l > 1) insertRects(splits, 1, i + 1, l - 1);
                if (splits[3] != null) add(splits[3], i + l, size - 1);
            }
        }
        int j = 0, i = 0;
        while (i < size) {
            if (rects[i].width == 0) rects[i] = null; else rects[j++] = rects[i];
            i++;
        }
        size = j;
    }

    protected void splitRect(Rectangle r, Rectangle sr, Rectangle[] splits) {
        int rx0 = r.x;
        int rx1 = rx0 + r.width - 1;
        int ry0 = r.y;
        int ry1 = ry0 + r.height - 1;
        int srx0 = sr.x;
        int srx1 = srx0 + sr.width - 1;
        int sry0 = sr.y;
        int sry1 = sry0 + sr.height - 1;
        if ((ry0 < sry0) && (ry1 >= sry0)) {
            splits[0] = new Rectangle(rx0, ry0, r.width, sry0 - ry0);
            ry0 = sry0;
        } else {
            splits[0] = null;
        }
        if ((ry0 <= sry1) && (ry1 > sry1)) {
            splits[1] = new Rectangle(rx0, sry1 + 1, r.width, ry1 - sry1);
            ry1 = sry1;
        } else {
            splits[1] = null;
        }
        if ((rx0 < srx0) && (rx1 >= srx0)) {
            splits[2] = new Rectangle(rx0, ry0, srx0 - rx0, ry1 - ry0 + 1);
        } else {
            splits[2] = null;
        }
        if ((rx0 <= srx1) && (rx1 > srx1)) {
            splits[3] = new Rectangle(srx1 + 1, ry0, rx1 - srx1, ry1 - ry0 + 1);
        } else {
            splits[3] = null;
        }
    }

    protected void insertRects(Rectangle[] rects, int srcPos, int dstPos, int len) {
        if (len == 0) return;
        ensureCapacity(size + len);
        for (int i = size - 1; i >= dstPos; i--) this.rects[i + len] = this.rects[i];
        for (int i = 0; i < len; i++) this.rects[i + dstPos] = rects[i + srcPos];
        size += len;
    }

    public void ensureCapacity(int sz) {
        if (sz <= rects.length) return;
        int nSz = rects.length + (rects.length >> 1) + 1;
        while (nSz < sz) nSz += (nSz >> 1) + 1;
        Rectangle[] nRects = new Rectangle[nSz];
        System.arraycopy(rects, 0, nRects, 0, size);
        rects = nRects;
    }

    /**
     * Comparator for ordering rects in X.
     *
     * Note: this comparator imposes orderings that are inconsistent
     *       with equals.
     */
    private static class RectXComparator implements Comparator, Serializable {

        RectXComparator() {
        }

        public final int compare(Object o1, Object o2) {
            return ((Rectangle) o1).x - ((Rectangle) o2).x;
        }
    }

    private class RLMIterator implements ListIterator {

        int idx = 0;

        boolean removeOk = false;

        boolean forward = true;

        RLMIterator() {
        }

        public boolean hasNext() {
            return idx < size;
        }

        public int nextIndex() {
            return idx;
        }

        public Object next() {
            if (idx >= size) throw new NoSuchElementException("No Next Element");
            forward = true;
            removeOk = true;
            return rects[idx++];
        }

        public boolean hasPrevious() {
            return idx > 0;
        }

        public int previousIndex() {
            return idx - 1;
        }

        public Object previous() {
            if (idx <= 0) throw new NoSuchElementException("No Previous Element");
            forward = false;
            removeOk = true;
            return rects[--idx];
        }

        public void remove() {
            if (!removeOk) throw new IllegalStateException("remove can only be called directly after next/previous");
            if (forward) idx--;
            if (idx != size - 1) System.arraycopy(rects, idx + 1, rects, idx, size - (idx + 1));
            size--;
            rects[size] = null;
            removeOk = false;
        }

        public void set(Object o) {
            Rectangle r = (Rectangle) o;
            if (!removeOk) throw new IllegalStateException("set can only be called directly after next/previous");
            if (forward) idx--;
            if (idx + 1 < size) {
                if (rects[idx + 1].x < r.x) throw new UnsupportedOperationException("RectListManager entries must be sorted");
            }
            if (idx >= 0) {
                if (rects[idx - 1].x > r.x) throw new UnsupportedOperationException("RectListManager entries must be sorted");
            }
            rects[idx] = r;
            removeOk = false;
        }

        public void add(Object o) {
            Rectangle r = (Rectangle) o;
            if (idx < size) {
                if (rects[idx].x < r.x) throw new UnsupportedOperationException("RectListManager entries must be sorted");
            }
            if (idx != 0) {
                if (rects[idx - 1].x > r.x) throw new UnsupportedOperationException("RectListManager entries must be sorted");
            }
            ensureCapacity(size + 1);
            if (idx != size) System.arraycopy(rects, idx, rects, idx + 1, size - idx);
            rects[idx] = r;
            idx++;
            removeOk = false;
        }
    }
}
