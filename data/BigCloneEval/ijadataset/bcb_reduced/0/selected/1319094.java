package edu.cmu.cs.bungee.piccoloUtils.gui;

import java.util.Iterator;
import javax.swing.SwingUtilities;
import edu.cmu.cs.bungee.javaExtensions.Labeled;
import edu.cmu.cs.bungee.javaExtensions.Util;

public class PriorityLabeler {

    private final Labeled labeled;

    private static final double epsilon = 1.0e-10;

    private int iVisibleWidth;

    /**
	 * Our logical width, of which floor(leftEdge) - floor(leftEdge +
	 * visibleWidth) is visible. logicalWidth>=visibleWidth();
	 * 
	 * visibleWidth = w - epsilon bars are placed at floor(0 - logicalWidth)
	 */
    private double logicalWidth = 0;

    /**
	 * offset into logicalWidth of the leftmost visible pixel. Rightmost visible
	 * pixel is leftEdge + w - epsilon 0<=leftEdge<logicalWidth-visibleWidth;
	 */
    private double leftEdge = 0;

    private int labelHprojectionW;

    /**
	 * The facet of the label at a given x coordinate. All points are recorded.
	 */
    private Object[] labelXs;

    private final int totalCount;

    public PriorityLabeler(Labeled labeled, int width, int labelW) {
        this.labeled = labeled;
        iVisibleWidth = width;
        logicalWidth = width;
        labelHprojectionW = labelW;
        int total = 0;
        for (Iterator it = labeled.getChildIterator(); it.hasNext(); ) {
            Object o = it.next();
            total += labeled.count(o);
        }
        totalCount = total;
        drawLabels();
    }

    double visibleWidth() {
        return iVisibleWidth - epsilon;
    }

    private int getCount(Object child) {
        return labeled.count(child);
    }

    private int cumCountInclusive(Object child) {
        return labeled.cumCountInclusive(child);
    }

    private int cumCountExclusive(Object child) {
        return cumCountInclusive(child) - getCount(child);
    }

    private int priority(int x) {
        if (labelXs[x] == null) return -1; else return labeled.priority(labelXs[x]);
    }

    private double barWidthRatio() {
        return logicalWidth / totalCount;
    }

    Iterator slowVisibleChildIterator() {
        double divisor = barWidthRatio();
        int minCount = (int) Math.ceil(leftEdge / divisor + epsilon);
        int maxCount = (int) ((leftEdge + visibleWidth()) / divisor - epsilon);
        return labeled.cumCountChildIterator(minCount, maxCount);
    }

    private void drawLabels() {
        assert SwingUtilities.isEventDispatchThread();
        assert totalCount > 0;
        labelXs = new Object[iVisibleWidth + 1];
        double divisor = barWidthRatio();
        for (Iterator it = slowVisibleChildIterator(); it.hasNext(); ) {
            Object child = it.next();
            int iMaxX = maxBarPixel(child, divisor);
            int iMinX = minBarPixel(child, divisor);
            assert iMaxX >= 0 && iMinX <= iVisibleWidth : printBar(child, iMinX, iMaxX, "label");
            int iMidX = (iMaxX + iMinX) / 2;
            if (getCount(child) > priority(iMidX)) labelXs[iMidX] = child;
        }
        drawComputedLabel(null, divisor);
    }

    /**
	 * March along pixels, finding the child Objects to draw. At each pixel, you
	 * draw the child with the highest count at that pixel, which was computed
	 * above, unless another child with a higher count has a label that would
	 * occlude it, unless IT would be occluded. So you get a recusive test,
	 * where a conflict on the rightmost label can propagate all the way back to
	 * the left. At each call, you know there are no conflicts with
	 * leftCandidate from the left. You look for a conflict on the right (or
	 * failing that, the next non-conflict on the right) and recurse on that to
	 * get the next labeled Object to the right. You draw leftCandidate iff it
	 * doesn't conflict with that next label.
	 */
    private Object drawComputedLabel(Object leftCandidate, double divisor) {
        Object result = null;
        int x1 = -1;
        int x0 = -1;
        int threshold = -1;
        if (leftCandidate != null) {
            x0 = midLabelPixel(leftCandidate, divisor);
            threshold = getCount(leftCandidate);
            x1 = x0 + labelHprojectionW;
        }
        for (int x = x0 + 1; x < iVisibleWidth && result == null; x++) {
            if (x > x1) threshold = -1;
            Object rightCandidate = labelXs[x];
            if (rightCandidate != null && getCount(rightCandidate) > threshold) {
                Object nextDrawn = drawComputedLabel(rightCandidate, divisor);
                if (nextDrawn != null && midLabelPixel(nextDrawn, divisor) <= midLabelPixel(rightCandidate, divisor) + labelHprojectionW) {
                    result = nextDrawn;
                } else {
                    result = rightCandidate;
                    maybeDrawLabel(result);
                }
            }
        }
        return result;
    }

    private int maybeDrawLabel(Object v) {
        int nPixelsOccluded = 0;
        if (getCount(v) > 0) {
            double divisor = barWidthRatio();
            int midX = midLabelPixel(v, divisor);
            assert midX >= 0 && midX < iVisibleWidth : v + " " + midX + " " + iVisibleWidth;
            if (labelXs[midX] == null || labelXs[midX] == v) {
                int minX = Util.constrain(midX - labelHprojectionW, 0, iVisibleWidth);
                int maxX = Util.constrain(midX + labelHprojectionW, 0, iVisibleWidth);
                for (int i = minX; i <= maxX; i++) {
                    if (labelXs[i] == null) nPixelsOccluded++;
                    labelXs[i] = v;
                }
                labeled.drawLabel(v, minBarPixel(v, divisor), maxBarPixel(v, divisor));
            }
        }
        return nPixelsOccluded;
    }

    /**
	 * @param child
	 * @param divisor
	 * @return x offset relative to leftEdge. return w-1 if offset would be off
	 *         the screen.
	 */
    private int maxBarPixel(Object child, double divisor) {
        double dPixel = cumCountInclusive(child) * divisor - leftEdge;
        return (int) Math.min(visibleWidth(), dPixel);
    }

    /**
	 * @param child
	 * @param divisor
	 * @return x offset relative to left edge. return 0 if offset would be
	 *         negative.
	 */
    private int minBarPixel(Object child, double divisor) {
        double dPixel = cumCountExclusive(child) * divisor - leftEdge;
        return Math.max(0, (int) dPixel);
    }

    /**
	 * @param child
	 * @param divisor
	 * @return x offset of middle of label relative to leftEdge
	 */
    private int midLabelPixel(Object child, double divisor) {
        return (minBarPixel(child, divisor) + maxBarPixel(child, divisor)) / 2;
    }

    private String printBar(Object child, int iMinX, int iMaxX, String what) {
        Util.print("Adding " + what + " for " + child + ": totalCount=" + getCount(child) + " range=" + cumCountExclusive(child) + "-" + cumCountInclusive(child) + " iMinX-iMaxX=" + iMinX + "-" + iMaxX + " left/logical=" + leftEdge + "/" + logicalWidth);
        return "";
    }
}
