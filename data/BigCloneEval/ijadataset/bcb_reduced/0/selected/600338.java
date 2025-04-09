package algs.model.interval;

import java.util.ArrayList;
import java.util.Collection;
import algs.model.IBinaryTreeNode;
import algs.model.IInterval;
import algs.model.interval.SegmentTreeNode;

/**
 * When a Segment Tree uses StoredIntervalsNode as the base node type, then a 
 * reference to the actual Intervals is stored (in no specific order) with each
 * node in the tree.
 *   
 * @author George Heineman
 * @version 1.0, 6/15/08
 * @since 1.0
 */
public class StoredIntervalsNode<T extends IBinaryTreeNode<T>> extends SegmentTreeNode<T> {

    /** Store Interval. */
    protected ArrayList<IInterval> intervals;

    /** Constructor to use with this node type. */
    private static IConstructor<?> constructor;

    /**
	 * Store additional information with each SegmentTreeNode
	 * 
	 * @param left
	 * @param right
	 */
    public StoredIntervalsNode(int left, int right) {
        super(left, right);
    }

    /**
	 * Algorithms over SegmentTrees often store additional information with 
	 * each node, and may perform complex computations on insert. 
	 * 
	 * This method is overridden by subclasses as required.
	 * <p>
	 * Just append to the end.
	 * 
 	 * @param interval  interval segment being updated.
	 */
    protected void update(IInterval interval) {
        if (intervals == null) {
            intervals = new ArrayList<IInterval>();
        }
        intervals.add(interval);
    }

    /** 
	 * Determine the matching test.
	 * 
	 * Defaults to equality of the node based upon the interval. Subclasses can override, for 
	 * example, to determine if the interval is contained within the node's information.
	 * 
	 * @param interval     the interval with whom we wish to match Test.
	 */
    public boolean equals(Object interval) {
        if (intervals == null) {
            return false;
        }
        return intervals.contains(interval);
    }

    /**
	 * Gather the set of stored intervals that are in common with the given target
	 * interval.
	 *
	 * @exception   IllegalArgumentException if target is ill-formed.
	 */
    public Collection<IInterval> gather(IInterval target) {
        checkInterval(target);
        return gather0(target);
    }

    /**
	 * Gather the set of stored intervals that are in common with the given target
	 * interval.
	 */
    private Collection<IInterval> gather0(IInterval target) {
        int begin = target.getLeft();
        int end = target.getRight();
        if (begin <= left && right <= end) {
            return intervals();
        } else {
            int mid = (left + right) / 2;
            Collection<IInterval> col1 = new ArrayList<IInterval>();
            Collection<IInterval> col2 = new ArrayList<IInterval>();
            if (begin < mid) {
                col1 = ((StoredIntervalsNode<T>) lson).gather0(target);
            }
            if (mid < end) {
                col2 = ((StoredIntervalsNode<T>) rson).gather0(target);
            }
            for (IInterval iiv : col2) {
                if (!col1.contains(iiv)) {
                    col1.add(iiv);
                }
            }
            for (IInterval iiv : intervals()) {
                if (!col1.contains(iiv)) {
                    col1.add(iiv);
                }
            }
            return col1;
        }
    }

    /**
	 * Algorithms over SegmentTrees often store additional information with 
	 * each node, and may wish to clear information and/or perform computations
	 * when a segment is deleted. 
	 * 
	 * This method is overridden by subclasses as required.
	 * 
	 * For dynamic reasons, search from end to front.
	 * 
 	 * @param interval  interval segment being disposed of.
	 */
    protected void dispose(IInterval interval) {
        if (intervals == null) {
            return;
        }
        for (int i = intervals.size() - 1; i >= 0; i--) {
            IInterval ival = intervals.get(i);
            if ((interval.getLeft() == ival.getLeft()) && (interval.getRight() == ival.getRight())) {
                intervals.remove(i);
                break;
            }
        }
    }

    /**
	 * Return all {@link IInterval} objects for this node as a collection.
	 * 
	 * @return {@link Collection} of {@link IInterval} objects stored for this node.
	 */
    public Collection<IInterval> intervals() {
        if (intervals == null) {
            return new ArrayList<IInterval>();
        }
        return intervals;
    }

    /**
	 * Reasonable extension to toString() method.
	 */
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("<");
        if (intervals != null) {
            for (IInterval i : intervals) {
                sb.append(i);
                sb.append(',');
            }
        }
        sb.append(">");
        return sb.toString();
    }

    /**
	 * Return singleton default constructor for this type of node
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static IConstructor<?> getConstructor() {
        if (constructor == null) {
            constructor = new IConstructor() {

                public SegmentTreeNode construct(int left, int right) {
                    return new StoredIntervalsNode(left, right);
                }
            };
        }
        return constructor;
    }
}
