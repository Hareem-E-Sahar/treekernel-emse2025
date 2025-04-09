package geovista.touchgraph.interaction;

import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JScrollBar;
import geovista.touchgraph.GraphListener;
import geovista.touchgraph.TGAbstractLens;
import geovista.touchgraph.TGPanel;
import geovista.touchgraph.TGPoint2D;

/**
 * HyperScroll. Responsible for producing that neat hyperbolic effect. (Which
 * isn't really hyperbolic, but just non-linear). Demonstrates the usefulness of
 * Lenses.
 * 
 * @author Alexander Shapiro
 * 
 */
public class HyperScroll implements GraphListener {

    private final transient JScrollBar hyperSB;

    private final transient TGPanel tgPanel;

    transient HyperLens hyperLens;

    transient double inverseArray[] = new double[200];

    transient double width;

    public HyperScroll(TGPanel tgp) {
        tgPanel = tgp;
        hyperSB = new JScrollBar(Adjustable.HORIZONTAL, 0, 8, 0, 108);
        hyperSB.addAdjustmentListener(new hyperAdjustmentListener());
        hyperLens = new HyperLens();
        width = 2000;
        updateInverseArray();
        tgPanel.addGraphListener(this);
    }

    public JScrollBar getHyperSB() {
        return hyperSB;
    }

    public HyperLens getLens() {
        return hyperLens;
    }

    public void graphMoved() {
    }

    public void graphReset() {
        hyperSB.setValue(0);
    }

    private class hyperAdjustmentListener implements AdjustmentListener {

        public void adjustmentValueChanged(AdjustmentEvent e) {
            updateInverseArray();
            tgPanel.repaintAfterMove();
        }
    }

    double rawHyperDist(double dist) {
        if (hyperSB.getValue() == 0) {
            return dist;
        }
        double hyperV = hyperSB.getValue();
        return Math.log(dist / (Math.pow(1.5, (70 - hyperV) / 40) * 80) + 1);
    }

    double hyperDist(double dist) {
        double hyperV = hyperSB.getValue();
        double hyperD = rawHyperDist(dist) / rawHyperDist(250) * 250;
        double fade = hyperV;
        double fadeAdjust = 100;
        hyperD = hyperD * fade / fadeAdjust + dist * (fadeAdjust - fade) / fadeAdjust;
        return hyperD;
    }

    void updateInverseArray() {
        double x;
        for (int i = 0; i < 200; i++) {
            x = width * i / 200;
            inverseArray[i] = hyperDist(x);
        }
    }

    int findInd(int min, int max, double dist) {
        int mid = (min + max) / 2;
        if (inverseArray[mid] < dist) {
            if (max - mid == 1) {
                return max;
            }
            return findInd(mid, max, dist);
        } else if (mid - min == 1) {
            return mid;
        } else {
            return findInd(min, mid, dist);
        }
    }

    double invHyperDist(double dist) {
        if (dist == 0) {
            return 0;
        }
        int i;
        if (inverseArray[199] < dist) {
            i = 199;
        } else {
            i = findInd(0, 199, dist);
        }
        double x2 = inverseArray[i];
        double x1 = inverseArray[i - 1];
        double j = (dist - x1) / (x2 - x1);
        return ((i + j - 1) / 200.0 * width);
    }

    class HyperLens extends TGAbstractLens {

        @Override
        protected void applyLens(TGPoint2D p) {
            double dist = Math.sqrt(p.x * p.x + p.y * p.y);
            if (dist > 0) {
                p.x = p.x / dist * hyperDist(dist);
                p.y = p.y / dist * hyperDist(dist);
            } else {
                p.x = 0;
                p.y = 0;
            }
        }

        @Override
        protected void undoLens(TGPoint2D p) {
            double dist = Math.sqrt(p.x * p.x + p.y * p.y);
            if (dist > 0) {
                p.x = p.x / dist * invHyperDist(dist);
                p.y = p.y / dist * invHyperDist(dist);
            } else {
                p.x = 0;
                p.y = 0;
            }
        }
    }
}
