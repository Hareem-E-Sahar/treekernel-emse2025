package com.touchgraph.graphlayout.interaction;

import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import com.touchgraph.graphlayout.GraphListener;
import com.touchgraph.graphlayout.TGAbstractLens;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.TGPoint2D;

/**
 * HyperScroll. Responsible for producing that neat hyperbolic effect. (Which isn't really hyperbolic, but just non-linear). Demonstrates the usefulness of Lenses.
 * 
 * @author Alexander Shapiro
 * @version 1.22-jre1.1 $Id: HyperScroll.java,v 1.2 2004/10/25 06:57:32 donv70 Exp $
 */
public class HyperScroll implements GraphListener {

    private Scrollbar hyperSB;

    private TGPanel tgPanel;

    HyperLens hyperLens;

    double inverseArray[] = new double[200];

    double width;

    public HyperScroll(TGPanel tgp) {
        tgPanel = tgp;
        hyperSB = new Scrollbar(Scrollbar.HORIZONTAL, 100, 8, 0, 108);
        hyperSB.addAdjustmentListener(new hyperAdjustmentListener());
        hyperLens = new HyperLens();
        width = 2000;
        updateInverseArray();
        tgPanel.addGraphListener(this);
    }

    public Scrollbar getHyperSB() {
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

    ;

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
            double dist = Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
            if (dist > 0) {
                p.setX(p.getX() / dist * hyperDist(dist));
                p.setY(p.getY() / dist * hyperDist(dist));
            } else {
                p.setX(0);
                p.setY(0);
            }
        }

        @Override
        protected void undoLens(TGPoint2D p) {
            double dist = Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
            if (dist > 0) {
                p.setX(p.getX() / dist * invHyperDist(dist));
                p.setY(p.getY() / dist * invHyperDist(dist));
            } else {
                p.setX(0);
                p.setY(0);
            }
        }
    }
}
