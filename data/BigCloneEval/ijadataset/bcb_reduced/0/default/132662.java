import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Design implements Serializable {

    private static final long serialVersionUID = -1428638605128121687L;

    protected Color background;

    protected List<DesignBounds> subDesigns;

    protected transient Area subDesignArea;

    protected DesignTemplate template;

    protected transient List<ActionListener> listeners;

    protected Design(DesignTemplate t) {
        subDesigns = new LinkedList<DesignBounds>();
        subDesignArea = new Area();
        template = t;
        bigSmallCutoff = -1;
    }

    public List<DesignBounds> getSubdesigns() {
        return subDesigns;
    }

    public void addActionListener(ActionListener l) {
        listeners().add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners().remove(l);
    }

    public void onChange() {
        for (ActionListener l : listeners()) {
            l.actionPerformed(new ActionEvent(this, 0, "Design change"));
        }
    }

    public void addSubdesign(DesignBounds shape) {
        setRightScale(shape);
        subDesignArea.add(shape.computeArea());
        subDesigns.add(shape);
        computeBigSmallCutoff();
        onChange();
    }

    public void removeSubdesign(DesignBounds shape) {
        subDesigns.remove(shape);
        subDesignArea.subtract(shape.computeArea());
        computeBigSmallCutoff();
        onChange();
    }

    protected List<ActionListener> listeners() {
        if (listeners == null) {
            listeners = new LinkedList<ActionListener>();
        }
        return listeners;
    }

    protected Area subDesignArea() {
        if (subDesignArea == null) {
            subDesignArea = new Area();
            for (DesignBounds sub : subDesigns) {
                subDesignArea.add(sub.computeArea());
            }
        }
        return subDesignArea;
    }

    public boolean isBig(DesignBounds sub) {
        if (sub.getScale() > bigSmallCutoff) {
            return true;
        }
        return false;
    }

    double bigSmallCutoff;

    protected double computeBigSmallCutoff() {
        if (subDesigns.size() < 2) {
            bigSmallCutoff = -1;
            return -1;
        }
        Collections.sort(subDesigns);
        double startRange = 0;
        double bigDifference = 0;
        double lastScale = 0;
        for (DesignBounds sub : subDesigns) {
            if (lastScale != 0) {
                double curDiff = sub.getScale() - lastScale;
                if (curDiff > bigDifference) {
                    bigDifference = curDiff;
                    startRange = lastScale;
                }
            }
            lastScale = sub.getScale();
        }
        bigSmallCutoff = startRange + bigDifference / 2;
        return bigSmallCutoff;
    }

    public boolean fits(DesignBounds shape) {
        Area overall = template.getArea();
        Area area = shape.computeArea();
        area.subtract(overall);
        if (!area.isEmpty()) return false;
        area = shape.computeArea();
        area.intersect(subDesignArea());
        if (!area.isEmpty()) return false;
        return true;
    }

    public DesignBounds subDesignUnder(Point2D point) {
        for (DesignBounds sub : subDesigns) {
            if (sub.computeArea().contains(point)) {
                return sub;
            }
        }
        return null;
    }

    public void setRightScale(DesignBounds shape) {
        if (fits(shape)) {
            return;
        }
        double smallestFailure = shape.getScale();
        double largestSuccess = 0;
        for (int i = 0; i < 15; i++) {
            double newScale = (smallestFailure + largestSuccess) / 2;
            shape.setScale(newScale);
            if (fits(shape)) {
                largestSuccess = newScale;
            } else {
                smallestFailure = newScale;
            }
        }
        shape.setScale(largestSuccess);
    }

    public void transformSubdesign(DesignBounds design) {
        setRightScale(design);
    }

    public DesignTemplate getTemplate() {
        return template;
    }
}
