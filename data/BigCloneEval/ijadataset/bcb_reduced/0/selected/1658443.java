package algs.model.kdtree;

import java.util.Comparator;
import algs.model.IMultiPoint;
import algs.model.IPoint;
import algs.model.array.Selection;
import algs.model.nd.Hypercube;
import algs.model.twod.TwoDPoint;

/**
 * Produces a KD-tree from a given input set using recursive median approach.
 * <p>
 * Note that we take care to construct the associated {@link Hypercube} regions with each
 * node to ensure the integrity of the regions; without these regions being properly
 * set, there is no way to "drain" the children of a subtree when a query wholly 
 * contains a subtree's region.  
 * 
 * @author George Heineman
 * @version 1.0, 6/15/08
 * @since 1.0
 */
public class KDFactory {

    /** 
	 * Known comparators for partitioning points along dimensional axes.
	 * <p>
	 * Uses 1-based access for ease of programming, so always is one larger
	 * than necessary.
	 */
    private static Comparator<IMultiPoint> comparators[] = null;

    /**
	 * Generate a KDTree from the given array of points.
	 * <p>
	 * If points is null, then return null since the dimensionality
	 * is unknown.
	 * <p>
	 * All points must have the same dimensionality, otherwise strange
	 * behavior may occur. Also, this method is not re-entrant, since the 
	 * comparators array is regenerated upon each invocation, thus we mark
	 * the method as 'synchronized'.
	 * 
	 * @param points    points which are to be used as source to generate KDTree.
	 */
    @SuppressWarnings("unchecked")
    public static synchronized KDTree generate(IMultiPoint[] points) {
        if (points.length == 0) {
            return null;
        }
        int maxD = points[0].dimensionality();
        KDTree tree = new KDTree(maxD);
        comparators = new Comparator[maxD + 1];
        for (int i = 1; i <= maxD; i++) {
            comparators[i] = new DimensionalComparator(i);
        }
        tree.setRoot(generate(1, maxD, points, 0, points.length - 1));
        return tree;
    }

    /**
	 * Generate a KDTree from the given array of IPoints.
     *
     * If underlying objects in points[] already implement IMultiPoint, then good. Otherwise
     * a new array of TwoDPoint objects is constructed to be passed into the generate method
     * on an IMultiPoint[] array (where the values for these TwoDPoints are extracted
     * from the X and Y coordinate values of the IPoint objects).
     *
	 * @param points
	 */
    public static synchronized KDTree generate(IPoint[] points) {
        if (points.length == 0) {
            return null;
        }
        IMultiPoint others[] = new IMultiPoint[points.length];
        for (int i = 0; i < points.length; i++) {
            if (points[i] instanceof IMultiPoint) {
                others[i] = (IMultiPoint) points[i];
            } else {
                others[i] = new TwoDPoint(points[i].getX(), points[i].getY());
            }
        }
        return generate(others);
    }

    /** Helper method for IMultiPoint. */
    private static DimensionalNode generate(int d, int maxD, IMultiPoint points[], int left, int right) {
        if (right < left) {
            return null;
        }
        if (right == left) {
            return new DimensionalNode(d, points[left]);
        }
        int m = 1 + (right - left) / 2;
        Selection.select(points, m, left, right, comparators[d]);
        DimensionalNode dm = new DimensionalNode(d, points[left + m - 1]);
        if (++d > maxD) {
            d = 1;
        }
        dm.setBelow(generate(d, maxD, points, left, left + m - 2));
        dm.setAbove(generate(d, maxD, points, left + m, right));
        return dm;
    }
}
