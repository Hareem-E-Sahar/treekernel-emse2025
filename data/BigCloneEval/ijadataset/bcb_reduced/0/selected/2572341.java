package PowersetViewer;

import java.util.ArrayList;
import java.util.Iterator;
import tools.Debug;
import AccordionPowersetDrawer.RangeInPowerset;
import AccordionPowersetDrawer.RangeList;

/**
 * A class representing a two-dimensional orthogonal range tree.
 * RangeTree is a class that can answer range query in time (log n)^2. 
 * It implements the data structure of 2D range tree.
 *
 * Nodes of this tree are objects of type RangeNode.
 * 
 * @author  Yunhong Zhou
 * 
 * @see RangeNode
 * @see Tree2Tree
 * @see AccordionDrawer.Tree
 * @see AccordionDrawer.TreeNode
 *
 */
public class RangeTree {

    RangeNode root = null;

    int element = 0;

    /**
     * Constructor: from a RangeList
     */
    public RangeTree(RangeList theRL) {
        ArrayList pointsAL = new ArrayList();
        Iterator iterRL = theRL.getRanges().iterator();
        while (iterRL.hasNext()) {
            RangeInPowerset nextRange = (RangeInPowerset) iterRL.next();
            Debug.print("RangeTree::Constructor", 3);
            Debug.print("\tAdding Range bewteen " + nextRange.getMin() + " and " + nextRange.getMax(), 3);
            for (int i = nextRange.getMin(); i <= nextRange.getMax(); i++) {
                pointsAL.add(new Integer(i));
            }
        }
        Point[] points = new Point[pointsAL.size()];
        for (int i = 0; i < pointsAL.size(); i++) {
            points[i] = new Point(0, ((Integer) pointsAL.get(i)).intValue());
        }
        Debug.print("RangeTree::Constructor adding " + pointsAL.size() + " points.", 3);
        if (pointsAL.size() > 0) Build2DRangeTree(points, 1);
    }

    /**
     * Constructor: calls RangeTreeBuild to build the tree
     * @param dim dim = 0 represents a 2D range tree, dim = 1 represents a
     * 1D range tree. This notation is not intuitive. 
     * @param A an array of points
     */
    public RangeTree(Point[] A, int dim) {
        Build2DRangeTree(A, dim);
    }

    /**
     * @param dim dim = 0 represents a 2D range tree, dim = 1 represents a
     * 1D range tree. This notation is not intuitive. 
     * @param A an array of points
     */
    void Build2DRangeTree(Point[] A, int dim) {
        if (dim == 0) root = build2D(A, 0, A.length - 1); else root = build1D(A, 0, A.length - 1);
        root.setElement(A[A.length / 2].get(dim));
    }

    public boolean isEmpty() {
        return root == null;
    }

    /** @return root */
    public RangeNode getRoot() {
        return root;
    }

    /** @return element */
    public int getElement() {
        return root.getElement();
    }

    /**
     * builds a 1D range tree using points from A[start] to A[end].
     * Assume the A is sorted by Y coordinate.
     * This will set the elemnt of the leaf(Y coordinate).
     *
     * @param A a list of points
     * @param start the start position from array A
     * @param end the end position from array A
     */
    RangeNode build1D(Point[] A, int start, int end) {
        int mid = (start + end) / 2;
        RangeNode tmp = new RangeNode(A[mid]);
        tmp.setElement(A[mid].get(1));
        tmp.setLeafCount(end - start + 1);
        if (start != end) {
            tmp.setLeft(build1D(A, start, mid));
            tmp.setRight(build1D(A, mid + 1, end));
        }
        return tmp;
    }

    /** 
     * builds a 2D range tree using points from A[start] to A[end].
     * First build an array for the associated 1D tree, and then sort the
     * array by Y coordinate, and create an associated tree by build1D.
     * Seting children by calling the function recursively.
     *
     * @param A an array of points
     * @param start the start position from the array
     * @param end the end position from the array
     */
    public RangeNode build2D(Point[] A, int start, int end) {
        int mid = (start + end) / 2;
        Point[] B = new Point[end - start + 1];
        for (int i = start; i <= end; i++) B[i - start] = (Point) A[i].clone();
        mergeSort(B, 1);
        RangeNode tmp = new RangeNode(new RangeTree(B, 1));
        tmp.setElement(A[mid].get(0));
        if (start != end) {
            tmp.setLeft(build2D(A, start, mid));
            tmp.setRight(build2D(A, mid + 1, end));
        }
        return tmp;
    }

    /** 
     * Find a RangeNode with <code>element</code>  in the range [low, high].
     * This function is used for query process.
     * @param low the lowest value of the range
     * @param high the highest value of the range
     */
    private RangeNode findSplitRangeNode(int low, int high) {
        RangeNode v = root;
        if (v != null) {
            while (!v.childless() && (v.getElement() < low || v.getElement() > high)) {
                if (v.getElement() < low) v = v.getRc(); else v = v.getLc();
            }
        }
        return v;
    }

    /** 
     * Query2D returns the total number of points contained in the window
     * [xlow, xhigh] X [ylow, yhigh].
     * If find a childless point in [xlow, xhigh], then check its Y coordinate.
     * First check left side of split leaf, then the right side.
     * If left is good, then all the right side is good too. 
     * if left is not good, go a little right.
     *
     * @return the total number of points contained in the window range
     * @param xlow the leftmost value of x coordinate
     * @param xhigh the rightmost value of x coordinate
     * @param ylow the lowest value of y coordinate
     * @param yhigh the highest value of y coordinate
     */
    public int query2D(int xlow, int xhigh, int ylow, int yhigh) {
        if (root == null) return 0;
        int count = 0;
        RangeNode v = findSplitRangeNode(xlow, xhigh);
        if (v.childless()) {
            RangeTree t = (RangeTree) (v.getData());
            if ((v.getElement() >= xlow) && (v.getElement() <= xhigh)) {
                count += t.query1D(ylow, yhigh);
            }
        } else {
            RangeTree t;
            RangeNode l = v.getLc();
            while (!l.childless()) {
                if (l.getElement() >= xlow) {
                    t = (RangeTree) (l.getRc().getData());
                    count += t.query1D(ylow, yhigh);
                    l = l.getLc();
                } else l = l.getRc();
            }
            t = (RangeTree) (l.getData());
            if ((l.getElement() >= xlow) && (l.getElement() <= xhigh)) {
                count += t.query1D(ylow, yhigh);
            }
            l = v.getRc();
            while (!l.childless()) {
                if (l.getElement() <= xhigh) {
                    t = (RangeTree) (l.getLc().getData());
                    count += t.query1D(ylow, yhigh);
                    l = l.getRc();
                } else l = l.getLc();
            }
            t = (RangeTree) (l.getData());
            if ((l.getElement() >= xlow) && (l.getElement() <= xhigh)) {
                count += t.query1D(ylow, yhigh);
            }
        }
        return count;
    }

    /** 
     * the function queries in Y coordinate and return the total number of
     * points in a given interval range. 
     * @see #query2D(int, int, int, int)
     * @return the total number of points in the range [ylow, yhigh]
     * @param ylow the lowest value of Y coordinate
     * @param yhigh the highest value of Y coordinate
     */
    public int query1D(int ylow, int yhigh) {
        if (root == null) return 0;
        int count = 0;
        RangeNode v = findSplitRangeNode(ylow, yhigh);
        Debug.print("RangeTree::query1D::low is " + ylow + "\thigh is " + yhigh, 3);
        Debug.print("RangeTree::query1D::v is " + v.getElement(), 3);
        if (v.childless() && (v.getElement() <= yhigh) && (v.getElement() >= ylow)) {
            count++;
        }
        if (!v.childless()) {
            RangeNode l = v.getLc();
            while (!l.childless()) {
                if (l.getElement() >= ylow) {
                    count += reportSubRangeTree(l.getRc());
                    l = l.getLc();
                } else l = l.getRc();
            }
            if (l.getElement() >= ylow) {
                count += reportSubRangeTree(l);
            }
            l = v.getRc();
            while (!l.childless()) {
                if (l.getElement() <= yhigh) {
                    count += reportSubRangeTree(l.getLc());
                    l = l.getRc();
                } else l = l.getLc();
            }
            if (l.getElement() <= yhigh) {
                count += reportSubRangeTree(l);
            }
        }
        return count;
    }

    /** 
     * will report all the leaves decedant to RangeNode <code>l</code>.
     * This function is called during the query process when we find that
     * the whole subtree under <code>l</code> is contained in the query window.
     * @param l should be 1D RangeNode 
     */
    private static int reportSubRangeTree(RangeNode l) {
        return l.getLeafCount();
    }

    /**
     * This implements a regular merge sort algorithm.
     * It is added as a static method, so no additional class is needed.
     * @param A an array to be sorted
     * @param dim the corresponding coordinate that sorting is based on
     */
    public static void mergeSort(Point[] A, int dim) {
        ms_divide(A, 0, A.length / 2, dim);
        ms_divide(A, (A.length / 2) + 1, A.length - 1, dim);
        ms_conq(A, 0, A.length / 2, A.length - 1, dim);
    }

    /**
     * the divide procedure divides the array into two parts, sort them
     * separately, and then merge the two sorted parts together. 
     *
     * @param A the array to be sorted
     * @param start the start position of the array
     * @param end the end position of the array
     * @param dim the corresponding coordinate that sorting is based on
     */
    private static void ms_divide(Point[] A, int start, int end, int dim) {
        if (start < end) {
            ms_divide(A, start, (start + end) / 2, dim);
            ms_divide(A, ((start + end) / 2) + 1, end, dim);
            ms_conq(A, start, (start + end) / 2, end, dim);
        }
    }

    /**
     * the conquer procedure merges two sorted parts together. The mid
     * value partitions the array into two parts: [low, mid] and [mid+1,
     * end], each part is already sorted. 
     *
     * @param A the array to be sorted
     * @param start the start position of the array
     * @param end the end position of the array
     * @param mid the mid value 
     * @param dim the corresponding coordinate that sorting is based on
     */
    private static void ms_conq(Point[] A, int start, int mid, int end, int dim) {
        Point[] tmp = new Point[end - start + 1];
        int a = start;
        int b = mid + 1;
        int c = 0;
        for (int i = 0; i < tmp.length; i++) {
            if ((b > end) || ((a <= mid) && (A[a].get(dim) < A[b].get(dim)))) {
                tmp[i] = (Point) A[a].clone();
                a++;
            } else {
                tmp[i] = (Point) A[b].clone();
                b++;
            }
        }
        for (int i = 0; i < tmp.length; i++) A[i + start] = (Point) tmp[i].clone();
    }
}

;
