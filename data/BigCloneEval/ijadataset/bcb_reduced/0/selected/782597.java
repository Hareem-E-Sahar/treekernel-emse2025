package AccordionBacDrawer;

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
public class ColorTree {

    ColorNode root = null;

    int element = 0;

    /**
     * Constructor: calls RangeTreeBuild to build the tree
     * @param dim dim = 0 represents a 2D range tree, dim = 1 represents a
     * 1D range tree. This notation is not intuitive. 
     * @param A an array of points
     */
    public ColorTree(RangeList list) {
        RangeInBac[] array = new RangeInBac[list.getNumRanges()];
        array = (RangeInBac[]) list.getRanges().toArray(array);
        root = build1D(array, 0, array.length - 1);
    }

    public boolean isEmpty() {
        return root == null;
    }

    /** @return root */
    public ColorNode getRoot() {
        return root;
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
    ColorNode build1D(RangeInBac[] A, int start, int end) {
        if (A.length == 0) return null;
        int mid = (start + end) / 2;
        ColorNode tmp = new ColorNode(A[mid], null, null);
        if (end - start >= 2) tmp.setLeft(build1D(A, start, mid - 1));
        if (end - start >= 1) tmp.setRight(build1D(A, mid + 1, end));
        return tmp;
    }

    /** 
     * Find a RangeInBac
     */
    public RangeInBac findSplitRangeNode(ColorNode v, RangeInBac range) {
        RangeInBac curr = null;
        if (v != null) {
            curr = v.getData();
            if (range.compareTo(curr) < 0) return findSplitRangeNode(v.getLc(), range);
            if (range.compareTo(curr) > 0) return findSplitRangeNode(v.getRc(), range);
            if (range.min > curr.max || range.max < curr.min) curr = null;
        }
        return curr;
    }

    public String toString() {
        return root.toString();
    }
}

;
