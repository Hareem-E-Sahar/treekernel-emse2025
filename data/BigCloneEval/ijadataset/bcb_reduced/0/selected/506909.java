package net.borderwars.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Eric
 *         Date: Mar 18, 2004
 *         Time: 10:21:35 AM
 */
public class QuadTree<Value> implements Serializable {

    private static final Logger log = Logger.getLogger("net.borderwars.util.QuadTree");

    private static final int maxDepth = 10;

    private static final int maxPerCell = 5;

    private static final int minPerCell = 1;

    private static int gid = 0;

    int id = gid++;

    private int totalSize = 0;

    private QuadTree<Value> root;

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    private int x1, x2, y1, y2;

    private boolean isLeaf = true;

    private QuadTree children[] = null;

    private ArrayList<HasXY> contained = new ArrayList<HasXY>();

    private QuadTree parent = null;

    private int depth = 0;

    private int width = 0;

    private int height = 0;

    public QuadTree(int width, int height) {
        this(-1 * width >> 1, height >> 1, width >> 1, -1 * height >> 1);
    }

    public QuadTree(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        width = Math.max(x2, x1) - Math.min(x2, x1);
        height = Math.max(y2, y1) - Math.min(y2, y1);
        if (root == null) {
            root = this;
        }
    }

    protected QuadTree(int x1, int y1, int x2, int y2, QuadTree parent) {
        this(x1, y1, x2, y2);
        this.parent = parent;
        this.depth = parent.depth + 1;
    }

    public String toString() {
        return (id + ": (" + x1 + "," + y1 + ")" + "-(" + x2 + "," + y2 + ")" + "--(" + width + "," + height + ") " + isLeaf);
    }

    public interface HasXY {

        public int x();

        public int y();
    }

    public class Wrapper implements HasXY {

        int x, y;

        Value v;

        public Wrapper(int x, int y, Value v) {
            this.x = x;
            this.y = y;
            this.v = v;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Wrapper wrapper = (Wrapper) o;
            if (x != wrapper.x) return false;
            if (y != wrapper.y) return false;
            if (!v.equals(wrapper.v)) return false;
            return true;
        }

        public int hashCode() {
            int result;
            result = x;
            result = 31 * result + y;
            result = 31 * result + v.hashCode();
            return result;
        }

        public int x() {
            return (x);
        }

        public int y() {
            return (y);
        }

        public Value v() {
            return (v);
        }
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void add(int x, int y, Value o) {
        add(new Wrapper(x, y, o));
    }

    public void add(HasXY m) {
        totalSize++;
        if (isLeaf) {
            if (contained.size() == maxPerCell && depth < maxDepth) {
                expandThisNode();
            } else {
                contained.add(m);
                return;
            }
        }
        QuadTree child = whichChild(m);
        child.add(m);
    }

    private QuadTree whichChild(HasXY m) {
        return (whichChild(m.x(), m.y()));
    }

    private void expandThisNode() {
        int sx = x1 + (width >> 1);
        int sy = y1 - (height >> 1);
        children = new QuadTree[4];
        children[0] = new QuadTree(x1, y1, sx, sy, this);
        children[1] = new QuadTree(sx + 1, y1, x2, sy, this);
        children[2] = new QuadTree(x1, sy + 1, sx, y2, this);
        children[3] = new QuadTree(sx + 1, sy + 1, x2, y2, this);
        isLeaf = false;
        for (HasXY aContained : contained) {
            totalSize--;
            this.add(aContained);
        }
        contained.clear();
    }

    private QuadTree whichChild(int x, int y) {
        for (QuadTree lvChild : children) {
            if (lvChild.isInMe(x, y)) {
                return (lvChild);
            }
        }
        throw new UnsupportedOperationException(x + "," + y + " not found in " + x1 + "," + y1 + "," + x2 + "," + y2);
    }

    public int getHeight() {
        return (height);
    }

    public int getWidth() {
        return (width);
    }

    List<HasXY> findInLine(int x, int y, int xx, int yy) {
        QuadTree a = whichChild(x, y);
        QuadTree b = whichChild(xx, yy);
        List<HasXY> items = new ArrayList<HasXY>();
        if (!a.equals(b)) {
            items.addAll(a.contained);
        } else {
            items.addAll(a.contained);
        }
        return (items);
    }

    void findInLine(QuadTree a, int x, int y, QuadTree b, int xx, int yy, List<HasXY> items) {
        if (a.equals(b)) {
            return;
        }
        items.addAll(a.contained);
        items.addAll(b.contained);
        int mx = (x + xx) / 2;
        int my = (y + yy) / 2;
        QuadTree c = whichChild(mx, my);
        findInLine(a, x, y, c, mx, my, items);
        findInLine(b, xx, yy, c, mx, my, items);
    }

    private boolean isInMe(int x, int y) {
        if (x >= x1 && x <= x2) {
            if (y <= y1 && y >= y2) {
                return true;
            }
        }
        return (false);
    }

    public QuadTree[] getChildren() {
        return (children);
    }

    public boolean isLeaf() {
        return (isLeaf);
    }
}
