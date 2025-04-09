package com.vividsolutions.jts.geomgraph.index;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geomgraph.Edge;

/**
 * MonotoneChains are a way of partitioning the segments of an edge to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain will never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is simply the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * Property 2 allows
 * binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * @version 1.7
 */
public class MonotoneChainEdge {

    Edge e;

    Coordinate[] pts;

    int[] startIndex;

    Envelope env1 = new Envelope();

    Envelope env2 = new Envelope();

    public MonotoneChainEdge(Edge e) {
        this.e = e;
        pts = e.getCoordinates();
        MonotoneChainIndexer mcb = new MonotoneChainIndexer();
        startIndex = mcb.getChainStartIndices(pts);
    }

    public Coordinate[] getCoordinates() {
        return pts;
    }

    public int[] getStartIndexes() {
        return startIndex;
    }

    public double getMinX(int chainIndex) {
        double x1 = pts[startIndex[chainIndex]].x;
        double x2 = pts[startIndex[chainIndex + 1]].x;
        return x1 < x2 ? x1 : x2;
    }

    public double getMaxX(int chainIndex) {
        double x1 = pts[startIndex[chainIndex]].x;
        double x2 = pts[startIndex[chainIndex + 1]].x;
        return x1 > x2 ? x1 : x2;
    }

    public void computeIntersects(MonotoneChainEdge mce, SegmentIntersector si) {
        for (int i = 0; i < startIndex.length - 1; i++) {
            for (int j = 0; j < mce.startIndex.length - 1; j++) {
                computeIntersectsForChain(i, mce, j, si);
            }
        }
    }

    public void computeIntersectsForChain(int chainIndex0, MonotoneChainEdge mce, int chainIndex1, SegmentIntersector si) {
        computeIntersectsForChain(startIndex[chainIndex0], startIndex[chainIndex0 + 1], mce, mce.startIndex[chainIndex1], mce.startIndex[chainIndex1 + 1], si);
    }

    private void computeIntersectsForChain(int start0, int end0, MonotoneChainEdge mce, int start1, int end1, SegmentIntersector ei) {
        Coordinate p00 = pts[start0];
        Coordinate p01 = pts[end0];
        Coordinate p10 = mce.pts[start1];
        Coordinate p11 = mce.pts[end1];
        if (end0 - start0 == 1 && end1 - start1 == 1) {
            ei.addIntersections(e, start0, mce.e, start1);
            return;
        }
        env1.init(p00, p01);
        env2.init(p10, p11);
        if (!env1.intersects(env2)) return;
        int mid0 = (start0 + end0) / 2;
        int mid1 = (start1 + end1) / 2;
        if (start0 < mid0) {
            if (start1 < mid1) computeIntersectsForChain(start0, mid0, mce, start1, mid1, ei);
            if (mid1 < end1) computeIntersectsForChain(start0, mid0, mce, mid1, end1, ei);
        }
        if (mid0 < end0) {
            if (start1 < mid1) computeIntersectsForChain(mid0, end0, mce, start1, mid1, ei);
            if (mid1 < end1) computeIntersectsForChain(mid0, end0, mce, mid1, end1, ei);
        }
    }
}
