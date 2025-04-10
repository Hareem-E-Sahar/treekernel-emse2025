package nl.BobbinWork.grids.PolarGridModel;

/**
 * A circle in a chain with increasing diameters.
 *
 * @author J. Pol
 */
public class ConcentricCircle extends DoubleChainedObject {

    private double diameter;

    /** Gets the diameter of the previous circle, or zero if there is no previous. */
    private double getInner() {
        ConcentricCircle sibling;
        if (this.previousAvailable()) {
            sibling = (ConcentricCircle) this.getPrevious();
            return sibling.getDiameter();
        } else {
            return 0D;
        }
    }

    public double getCircumference() {
        return this.diameter * Math.PI;
    }

    public void setCircumference(double circumference) {
        this.setDiameter(circumference / Math.PI, 0D);
    }

    public void setCircumference(String circumference) {
        this.setCircumference(Double.valueOf(circumference).doubleValue());
    }

    /** Gets the actual diameter.
     * The value may be different from the argument
     * passed to a constructor or setDiameter
     * to keep the rings in the chain concentric.
     */
    public double getDiameter() {
        return this.diameter;
    }

    /** Sets the diameter for the circle keeping the value between its neighbours. */
    public void setDiameter(double diameter) {
        this.setDiameter(diameter, 0D);
    }

    /** Sets the diameter for the circle keeping the value between its neighbours. */
    public void setDiameter(String diameter) {
        this.setDiameter(Double.valueOf(diameter).doubleValue(), 0D);
    }

    /** Sets the diameter for the circle keeping the value between its neighbours.
     * If the neighbours are closer than twice the minimumDistance,
     * the diameter is set to the average diameter of the neighbours,
     * unless the requested diameter lies between its neigbours.
     */
    public void setDiameter(double diameter, double minimumDistance) {
        ConcentricCircle sibling;
        double outer;
        double inner = this.getInner();
        minimumDistance = Math.abs(minimumDistance);
        diameter = Math.abs(diameter);
        if (this.nextAvailable()) {
            sibling = (ConcentricCircle) this.getNext();
            outer = sibling.getDiameter();
        } else {
            outer = Double.MAX_VALUE;
        }
        if ((inner + minimumDistance) > (outer - minimumDistance)) {
            if (minimumDistance < inner || minimumDistance > outer) {
                this.diameter = (inner + outer) / 2;
            } else {
                this.diameter = diameter;
            }
        } else {
            this.diameter = Math.max(Math.abs(diameter), inner + minimumDistance);
            this.diameter = Math.min(this.diameter, outer - minimumDistance);
        }
        if (this.diameter < 0.1D) {
            this.diameter = 0.1D;
        }
    }

    /** Creates a new circle in an existing chain of concentric circles.
     * The diameter will be the average of the sibling circles,
     * or half/double of the one and only sibling.
     */
    public ConcentricCircle(boolean before, ConcentricCircle x) {
        super(before, x);
        ConcentricCircle sibling;
        double outer;
        double inner = this.getInner();
        if (this.nextAvailable()) {
            sibling = (ConcentricCircle) this.getNext();
            outer = sibling.getDiameter();
        } else {
            sibling = (ConcentricCircle) this.getPrevious();
            outer = sibling.getDiameter() * 4;
        }
        this.diameter = (inner + outer) / 2;
    }

    /** Creates a new circle in an existing chain of concentric circles.
     * See setDiameter for constraints.
     */
    public ConcentricCircle(boolean before, ConcentricCircle x, double diameter) {
        super(before, x);
        this.setDiameter(diameter);
    }

    /** Creates the first circle for a new chain of concentric circles. */
    public ConcentricCircle(double diameter) {
        super();
        this.diameter = Math.abs(diameter);
    }
}
