package com.vividsolutions.jts.index.chain;

import com.vividsolutions.jts.geom.*;

/**
 * Monotone Chains are a way of partitioning the segments of a linestring to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is equal to the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * <p>
 * Property 2 allows
 * an efficient binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * <p>
 * One of the goals of this implementation of MonotoneChains is to be
 * as space and time efficient as possible. One design choice that aids this
 * is that a MonotoneChain is based on a subarray of a list of points.
 * This means that new arrays of points (potentially very large) do not
 * have to be allocated.
 * <p>
 *
 * MonotoneChains support the following kinds of queries:
 * <ul>
 * <li>Envelope select: determine all the segments in the chain which
 * intersect a given envelope
 * <li>Overlap: determine all the pairs of segments in two chains whose
 * envelopes overlap
 * </ul>
 *
 * This implementation of MonotoneChains uses the concept of internal iterators
 * to return the resultsets for the above queries.
 * This has time and space advantages, since it
 * is not necessary to build lists of instantiated objects to represent the segments
 * returned by the query.
 * However, it does mean that the queries are not thread-safe.
 *
 * @version 1.7
 */
public class MonotoneChain {

    private Coordinate[] pts;

    private int start, end;

    private Envelope env = null;

    private Object context = null;

    private int id;

    public MonotoneChain(Coordinate[] pts, int start, int end, Object context) {
        this.pts = pts;
        this.start = start;
        this.end = end;
        this.context = context;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Object getContext() {
        return context;
    }

    public Envelope getEnvelope() {
        if (env == null) {
            Coordinate p0 = pts[start];
            Coordinate p1 = pts[end];
            env = new Envelope(p0, p1);
        }
        return env;
    }

    public int getStartIndex() {
        return start;
    }

    public int getEndIndex() {
        return end;
    }

    public void getLineSegment(int index, LineSegment ls) {
        ls.p0 = pts[index];
        ls.p1 = pts[index + 1];
    }

    /**
   * Return the subsequence of coordinates forming this chain.
   * Allocates a new array to hold the Coordinates
   */
    public Coordinate[] getCoordinates() {
        Coordinate coord[] = new Coordinate[end - start + 1];
        int index = 0;
        for (int i = start; i <= end; i++) {
            coord[index++] = pts[i];
        }
        return coord;
    }

    /**
   * Determine all the line segments in the chain whose envelopes overlap
   * the searchEnvelope, and process them
   */
    public void select(Envelope searchEnv, MonotoneChainSelectAction mcs) {
        computeSelect(searchEnv, start, end, mcs);
    }

    private void computeSelect(Envelope searchEnv, int start0, int end0, MonotoneChainSelectAction mcs) {
        Coordinate p0 = pts[start0];
        Coordinate p1 = pts[end0];
        mcs.tempEnv1.init(p0, p1);
        if (end0 - start0 == 1) {
            mcs.select(this, start0);
            return;
        }
        if (!searchEnv.intersects(mcs.tempEnv1)) return;
        int mid = (start0 + end0) / 2;
        if (start0 < mid) {
            computeSelect(searchEnv, start0, mid, mcs);
        }
        if (mid < end0) {
            computeSelect(searchEnv, mid, end0, mcs);
        }
    }

    public void computeOverlaps(MonotoneChain mc, MonotoneChainOverlapAction mco) {
        computeOverlaps(start, end, mc, mc.start, mc.end, mco);
    }

    private void computeOverlaps(int start0, int end0, MonotoneChain mc, int start1, int end1, MonotoneChainOverlapAction mco) {
        Coordinate p00 = pts[start0];
        Coordinate p01 = pts[end0];
        Coordinate p10 = mc.pts[start1];
        Coordinate p11 = mc.pts[end1];
        if (end0 - start0 == 1 && end1 - start1 == 1) {
            mco.overlap(this, start0, mc, start1);
            return;
        }
        mco.tempEnv1.init(p00, p01);
        mco.tempEnv2.init(p10, p11);
        if (!mco.tempEnv1.intersects(mco.tempEnv2)) return;
        int mid0 = (start0 + end0) / 2;
        int mid1 = (start1 + end1) / 2;
        if (start0 < mid0) {
            if (start1 < mid1) computeOverlaps(start0, mid0, mc, start1, mid1, mco);
            if (mid1 < end1) computeOverlaps(start0, mid0, mc, mid1, end1, mco);
        }
        if (mid0 < end0) {
            if (start1 < mid1) computeOverlaps(mid0, end0, mc, start1, mid1, mco);
            if (mid1 < end1) computeOverlaps(mid0, end0, mc, mid1, end1, mco);
        }
    }
}
