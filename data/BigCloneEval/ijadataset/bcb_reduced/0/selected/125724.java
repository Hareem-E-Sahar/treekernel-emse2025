package algs.model.problems;

import java.util.*;
import algs.model.IInterval;
import algs.model.interval.SegmentTree;
import algs.model.interval.StoredIntervalsNode;

/**
 * Given a set S of n intervals and a query point q, report all of those intervals
 * that contain q. That is, find a subset F of S such that:
 * <pre>
 *  F = {I_n | I_n.left <= q <= I_n.right } 
 * </pre>
 * 
 * @author George Heineman
 * @version 1.0, 6/15/08
 * @since 1.0
 */
public class EnclosingIntervalSearch {

    /**
	 * Return the computed set.
	 * 
	 * @param tree    Segment tree containing the intervals to be searched
	 * @param q       the target point to be used to check intersection with intervals stored by the tree.
	 * @return        the Set of {@link IInterval} segments that intersect point q.
	 */
    public static Set<IInterval> compute(SegmentTree<StoredIntervalsNode<?>> tree, int q) {
        Set<IInterval> F = new LinkedHashSet<IInterval>();
        compute(tree.getRoot(), q, F);
        return F;
    }

    /**
	 * Helper method for compute.
	 * <p> 
	 * Naturally divides the tree even though ranges of the interior nodes may 
	 * overlap. We rely on the 'midpoint' of the node to determine whether to search
	 * left or right.
	 * 
	 * @param tree
	 * @param q
	 * @param F
	 */
    private static void compute(StoredIntervalsNode<?> node, int q, Set<IInterval> F) {
        int left, right;
        if (node == null || q < (left = node.getLeft()) || q > (right = node.getRight())) {
            return;
        }
        F.addAll(node.intervals());
        int mid = (left + right) / 2;
        if (q < mid) {
            compute((StoredIntervalsNode<?>) node.getLeftSon(), q, F);
        } else if (q >= mid) {
            compute((StoredIntervalsNode<?>) node.getRightSon(), q, F);
        }
    }
}
