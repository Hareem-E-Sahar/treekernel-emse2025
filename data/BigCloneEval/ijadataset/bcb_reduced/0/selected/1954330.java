package uk.ac.rdg.resc.ncwms.coords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.constants.AxisType;

/**
 * A one-dimensional coordinate axis, whose values are not equally spaced.
 *
 * @author Jon Blower
 * $Revision$
 * $Date$
 * $Log$
 */
class Irregular1DCoordAxis extends OneDCoordAxis {

    private static final Logger logger = LoggerFactory.getLogger(Irregular1DCoordAxis.class);

    /**
     * Maps axis values to their indices along the axis, sorted in ascending order
     * of value.  This level of
     * elaboration is necessary because some axis values might be NaNs if they
     * are latitude values outside the range -90:90 (possible for some model data).
     * These NaNs are not stored here.
     */
    private List<AxisValue> axisVals;

    /**
     * Simple class mapping axis values to indices.
     */
    private static final class AxisValue implements Comparable<AxisValue> {

        private double value;

        private int index;

        public AxisValue(double value, int index) {
            this.value = value;
            this.index = index;
        }

        /**
         * Sorts based on the axis value, not the index
         */
        public int compareTo(AxisValue otherVal) {
            return Double.compare(this.value, otherVal.value);
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof AxisValue)) return false;
            AxisValue otherAxisVal = (AxisValue) obj;
            return this.value == otherAxisVal.value && this.index == otherAxisVal.index;
        }
    }

    /**
     * Creates a new instance of Irregular1DCoordAxis
     */
    public Irregular1DCoordAxis(double[] coordValues, AxisType axisType) {
        super(axisType, coordValues.length);
        this.axisVals = new ArrayList<AxisValue>(coordValues.length);
        for (int i = 0; i < coordValues.length; i++) {
            if (!Double.isNaN(coordValues[i])) {
                this.axisVals.add(new AxisValue(coordValues[i], i));
            }
        }
        Collections.sort(this.axisVals);
        if (this.isLongitude()) {
            logger.debug("Checking for longitude axis wrapping...");
            double lastVal = this.axisVals.get(this.axisVals.size() - 1).value;
            double dx = lastVal - this.axisVals.get(this.axisVals.size() - 2).value;
            double nextVal = lastVal + dx;
            logger.debug("lastVal = {}, nextVal = {}", lastVal, nextVal);
            AxisValue firstVal = this.axisVals.get(0);
            Longitude firstValLon = new Longitude(firstVal.value);
            Longitude lastValLon = new Longitude(lastVal);
            Longitude nextValLon = new Longitude(nextVal);
            if (firstValLon.isBetween(lastVal, nextVal) || lastValLon.getClockwiseDistanceTo(nextVal) > nextValLon.getClockwiseDistanceTo(firstVal.value)) {
                logger.debug("Axis wraps, creating new point with lon = {}", (firstVal.value + 360));
                this.axisVals.add(new AxisValue(firstVal.value + 360, firstVal.index));
            }
        }
        logger.debug("Created irregular {} axis", this.getAxisType());
    }

    /**
     * Uses a binary search algorithm to find the index of the point on the axis
     * whose value is closest to the given one.
     * @param coordValue The value along this coordinate axis
     * @return the index that is nearest to this point, or -1 if the point is
     * out of range for the axis
     */
    public int getIndex(double coordValue) {
        logger.debug("Finding index for {} {} ...", this.getAxisType(), coordValue);
        int index = this.findNearest(coordValue);
        if (index < 0 && this.isLongitude() && coordValue < 0) {
            index = this.findNearest(coordValue + 360);
        }
        logger.debug("   ...index= {}", index);
        return index;
    }

    /**
     * Performs a binary search to find the index of the element of the array
     * whose value is closest to the target
     * @param target The value to search for
     * @return the index of the element in values whose value is closest to target,
     * or -1 if the target is out of range
     */
    private int findNearest(double target) {
        if (target < this.axisVals.get(0).value || target > this.axisVals.get(this.axisVals.size() - 1).value) {
            return -1;
        }
        int low = 0;
        int high = this.axisVals.size() - 1;
        while (high > low + 1) {
            int mid = (low + high) / 2;
            AxisValue midVal = this.axisVals.get(mid);
            if (midVal.value == target) return midVal.index; else if (midVal.value < target) low = mid; else high = mid;
        }
        AxisValue lowVal = this.axisVals.get(low);
        AxisValue highVal = this.axisVals.get(high);
        return (Math.abs(target - lowVal.value) < Math.abs(target - highVal.value)) ? lowVal.index : highVal.index;
    }

    /**
     * Gets the <i>i</i>th coordinate value along this axis. The index will
     * already have been checked for validity
     * @param index the index along the axis
     * @return the <i>i</i>th coordinate value along this axis
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= this.getSize()}
     */
    public double getCoordValue(int index) {
        return this.axisVals.get(index).value;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Irregular1DCoordAxis)) return false;
        Irregular1DCoordAxis otherAxis = (Irregular1DCoordAxis) obj;
        if (this.axisVals.size() != otherAxis.axisVals.size()) return false;
        if (this.getAxisType() != otherAxis.getAxisType()) return false;
        for (int i = 0; i < this.axisVals.size(); i++) {
            if (!this.axisVals.get(i).equals(otherAxis.axisVals.get(i))) {
                return false;
            }
        }
        return true;
    }
}
