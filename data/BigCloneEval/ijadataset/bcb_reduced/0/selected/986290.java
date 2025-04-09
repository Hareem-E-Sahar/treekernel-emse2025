package s24;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

class MyIndexComparator implements Comparator<Integer> {

    final Point[] xArray;

    public MyIndexComparator(Point[] xArray) {
        this.xArray = xArray;
    }

    public int compare(Integer a, Integer b) {
        return Double.compare(xArray[a].y, xArray[b].y);
    }
}

@SuppressWarnings("serial")
class BSTElt extends Point implements Comparable<BSTElt> {

    public BSTElt(Point a) {
        super(a);
    }

    public int compareTo(BSTElt b) {
        return PointComparator.VERTICALLY.compare(this, b);
    }
}

@SuppressWarnings("serial")
class BSTEltY extends BSTElt implements Comparable<BSTElt> {

    public BSTEltY(Point a) {
        super(a);
    }

    public int compareTo(BSTElt b) {
        return PointComparator.HORIZONTALLY.compare(this, b);
    }
}

class RangeTreeElt {

    BST<BSTElt> yTree;

    Point xMedian;

    public RangeTreeElt(BST<BSTElt> t, Point x) {
        yTree = t;
        xMedian = x;
    }

    public String toString() {
        return "" + xMedian.x;
    }
}

public class RangeTree {

    protected BTree<RangeTreeElt> xTree;

    public RangeTree(Point[] points) {
        int n = points.length;
        Point[] xP = new Point[n];
        for (int i = 0; i < n; i++) xP[i] = new BSTElt(points[i]);
        Arrays.sort(xP, PointComparator.HORIZONTALLY);
        List<Integer> yOrder = new Vector<Integer>(n);
        for (int i = 0; i < n; i++) {
            yOrder.add(i);
        }
        Collections.sort(yOrder, new MyIndexComparator(xP));
        xTree = build2dRangeTree(xP, 0, n - 1, yOrder);
    }

    public Point[] search(Point bottomLeft, Point topRight) {
        List<Point> v = query2D(xTree.root(), bottomLeft, topRight, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return v.toArray(new Point[0]);
    }

    public String toString() {
        return "" + xTree.toReadableString();
    }

    private static List<Point> query1D(BST<BSTElt> ty, int yFrom, int yTo) {
        BSTElt pMin = new BSTElt(new Point(Integer.MIN_VALUE, yFrom));
        BSTElt pMax = new BSTElt(new Point(Integer.MAX_VALUE, yTo));
        return new ArrayList<Point>(ty.inRange(pMin, pMax));
    }

    private static List<Point> query2D(BTreeItr<RangeTreeElt> tx, Point bottomLeft, Point topRight, int xMin, int xMax) {
        int xFrom = bottomLeft.x;
        int xTo = topRight.x;
        int yFrom = bottomLeft.y;
        int yTo = topRight.y;
        Vector<Point> v = new Vector<Point>();
        if (tx.isBottom()) return v;
        RangeTreeElt m = tx.consult();
        int mx = m.xMedian.x;
        int my = m.xMedian.y;
        if (mx > xTo) {
            return query2D(tx.left(), bottomLeft, topRight, xMin, mx);
        } else if (mx < xFrom) {
            return query2D(tx.right(), bottomLeft, topRight, mx, xMax);
        }
        if (xMin >= xFrom && xMax <= xTo) {
            return query1D(m.yTree, yFrom, yTo);
        }
        if (my >= yFrom && my <= yTo) {
            v.add(m.xMedian);
        }
        v.addAll(query2D(tx.left(), bottomLeft, topRight, xMin, mx));
        v.addAll(query2D(tx.right(), bottomLeft, topRight, mx, xMax));
        return v;
    }

    private static BTree<RangeTreeElt> build2dRangeTree(Point[] xP, int left, int right, List<Integer> yOrder) {
        int n = right + 1 - left;
        BTree<RangeTreeElt> t = new BTree<RangeTreeElt>();
        BTreeItr<RangeTreeElt> ti = t.root();
        if (n == 0) return t;
        BSTElt[] yElts = new BSTElt[n];
        for (int i = 0; i < n; i++) yElts[i] = new BSTEltY(xP[yOrder.get(i)]);
        BST<BSTElt> ty = new BST<BSTElt>(yElts);
        int mid = (right + left) / 2;
        ti.insert(new RangeTreeElt(ty, xP[mid]));
        if (n == 1) return t;
        List<Integer> yLeft = new ArrayList<Integer>();
        List<Integer> yRight = new ArrayList<Integer>();
        for (int i : yOrder) {
            if (i < mid) {
                yLeft.add(i);
            } else if (i > mid) {
                yRight.add(i);
            }
        }
        if (right - left > 2) {
            Collections.sort(yLeft, new MyIndexComparator(xP));
            BTree<RangeTreeElt> leftTree = build2dRangeTree(xP, left, mid - 1, yLeft);
            ti.left().paste(leftTree);
        }
        Collections.sort(yRight, new MyIndexComparator(xP));
        BTree<RangeTreeElt> rigthTree = build2dRangeTree(xP, mid + 1, right, yRight);
        ti.right().paste(rigthTree);
        return t;
    }

    private static int naiveMatching(Point[] t, int x1, int x2, int y1, int y2) {
        int count = 0;
        for (Point p : t) if (p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2) count++;
        return count;
    }

    public static Point[] rndPointSet(Random r, int n, int maxCoord) {
        Point[] t = new Point[n];
        Point p;
        HashSet<Point> h = new HashSet<Point>();
        Iterator<Point> itr;
        while (h.size() < n) {
            p = new Point(r.nextInt(maxCoord), r.nextInt(maxCoord));
            h.add(p);
        }
        itr = h.iterator();
        for (int i = 0; i < n; i++) {
            p = (Point) (itr.next());
            t[i] = p;
        }
        return t;
    }

    public static void main(String[] args) {
        long t1, t2;
        int i;
        int n = 100000;
        int x1 = (int) (Math.sqrt(n));
        int y1 = x1;
        int x2 = 5 * x1;
        int y2 = x2;
        int queryRepetitions = 1000;
        if (args.length == 6) {
            n = Integer.parseInt(args[0]);
            x1 = Integer.parseInt(args[1]);
            x2 = Integer.parseInt(args[2]);
            y1 = Integer.parseInt(args[3]);
            y2 = Integer.parseInt(args[4]);
            queryRepetitions = Integer.parseInt(args[5]);
        }
        System.out.print("Find in range [" + x1 + ".." + x2 + "][" + y1 + ".." + y2 + "]");
        Random r = new Random();
        Point[] t = rndPointSet(r, n, n);
        System.out.println(" among " + n + " points of coords [0.." + n + "]");
        t1 = System.currentTimeMillis();
        RangeTree rt = new RangeTree(t);
        t2 = System.currentTimeMillis();
        System.out.println("Range Tree built; time[ms]= " + (t2 - t1));
        if (n < 50) System.out.println("" + rt);
        Point[] res = null;
        t1 = System.currentTimeMillis();
        for (int j = 0; j < queryRepetitions; j++) res = rt.search(new Point(x1, y1), new Point(x2, y2));
        t2 = System.currentTimeMillis();
        System.out.print("- Range Tree, " + queryRepetitions + " queries;");
        System.out.println(" average time [ms]:" + 1.0 * (t2 - t1) / queryRepetitions);
        System.out.println("   Nb of points : " + res.length);
        int count = 0;
        t1 = System.currentTimeMillis();
        for (int j = 0; j < queryRepetitions; j++) count = naiveMatching(t, x1, x2, y1, y2);
        t2 = System.currentTimeMillis();
        System.out.print("- Naive search, " + queryRepetitions + " queries;");
        System.out.println(" average time [ms]:" + 1.0 * (t2 - t1) / queryRepetitions);
        if (res.length < 10) for (i = 0; i < res.length; i++) {
            System.out.println("     " + res[i]);
        }
        ok(count == res.length);
    }

    static void ok(boolean b) {
        if (b) return;
        throw new RuntimeException("property not verified");
    }
}
