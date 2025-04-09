package com.vividsolutions.jts.geomgraph.index;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geomgraph.SnappingEdge;

public class SnapMonotoneChainEdge extends MonotoneChainEdge {

    SnappingEdge e;

    int[] startIndex;

    double snapTolerance;

    public SnapMonotoneChainEdge(SnappingEdge e, double snapTolerance) {
        super(e);
        this.e = e;
        startIndex = super.getStartIndexes();
        this.snapTolerance = snapTolerance;
    }

    public SnappingEdge getEdge() {
        return e;
    }

    public void computeIntersects(MonotoneChainEdge mce, SegmentIntersector si) {
        if (!(mce instanceof SnapMonotoneChainEdge)) throw new IllegalArgumentException("Requiere MCE de snap");
        for (int i = 0; i < startIndex.length - 1; i++) {
            for (int j = 0; j < mce.getStartIndexes().length - 1; j++) {
                computeIntersectsForChain(i, mce, j, si);
            }
        }
    }

    public void computeIntersectsForChain(int chainIndex0, MonotoneChainEdge mce, int chainIndex1, SegmentIntersector si) {
        if (!(mce instanceof SnapMonotoneChainEdge)) throw new IllegalArgumentException("Requiere MCE de snap");
        computeIntersectsForChain(startIndex[chainIndex0], startIndex[chainIndex0 + 1], mce, mce.getStartIndexes()[chainIndex1], mce.getStartIndexes()[chainIndex1 + 1], si);
    }

    private void computeIntersectsForChain(int start0, int end0, MonotoneChainEdge mce, int start1, int end1, SegmentIntersector ei) {
        if (!(mce instanceof SnapMonotoneChainEdge)) throw new IllegalArgumentException("Requiere MCE de snap");
        SnapMonotoneChainEdge snapMce = (SnapMonotoneChainEdge) mce;
        Coordinate p00 = super.getCoordinates()[start0];
        Coordinate p01 = super.getCoordinates()[end0];
        Coordinate p10 = mce.getCoordinates()[start1];
        Coordinate p11 = mce.getCoordinates()[end1];
        if (end0 - start0 == 1 && end1 - start1 == 1) {
            ei.addIntersections(e, start0, snapMce.getEdge(), start1);
            return;
        }
        Envelope env1 = new Envelope(p00, p01);
        double newMinX = env1.getMinX() - snapTolerance;
        double newMaxX = env1.getMaxX() + snapTolerance;
        double newMinY = env1.getMinY() - snapTolerance;
        double newMaxY = env1.getMaxY() + snapTolerance;
        env1 = new Envelope(newMinX, newMaxX, newMinY, newMaxY);
        Envelope env2 = new Envelope(p10, p11);
        newMinX = env1.getMinX() - snapTolerance;
        newMaxX = env1.getMaxX() + snapTolerance;
        newMinY = env1.getMinY() - snapTolerance;
        newMaxY = env1.getMaxY() + snapTolerance;
        env2 = new Envelope(newMinX, newMaxX, newMinY, newMaxY);
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
