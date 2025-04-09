package uk.ac.rdg.resc.ncwms.metadata;

import com.sleepycat.persist.model.Persistent;
import java.util.Collections;
import java.util.Vector;
import org.apache.log4j.Logger;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.unidata.geoloc.LatLonPoint;

/**
 * A one-dimensional coordinate axis, whose values are not equally spaced.
 *
 * @author Jon Blower
 * $Revision$
 * $Date$
 * $Log$
 */
@Persistent
public class Irregular1DCoordAxis extends OneDCoordAxis {

    private static final Logger logger = Logger.getLogger(Irregular1DCoordAxis.class);

    /**
     * Maps axis values to their indices along the axis, sorted in ascending order
     * of value.  This level of
     * elaboration is necessary because some axis values might be NaNs if they
     * are latitude values outside the range -90:90 (possible for some model data).
     * These NaNs are not stored here.
     */
    private Vector<AxisValue> axisVals;

    /**
     * Simple class mapping axis values to indices.  Longitudes are always
     * stored in range 0->360
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
    }

    /**
     * Creates a new instance of Irregular1DCoordAxis
     */
    public Irregular1DCoordAxis(CoordinateAxis1D axis1D) {
        super(axis1D);
        double[] vals = axis1D.getCoordValues();
        this.axisVals = new Vector<AxisValue>(vals.length);
        for (int i = 0; i < vals.length; i++) {
            if (!Double.isNaN(vals[i])) {
                this.axisVals.add(new AxisValue(vals[i], i));
            }
        }
        Collections.sort(this.axisVals);
        if (this.isLongitude) {
            logger.debug("Checking for longitude axis wrapping...");
            double lastVal = this.axisVals.lastElement().value;
            double dx = lastVal - this.axisVals.get(this.axisVals.size() - 2).value;
            double nextVal = lastVal + dx;
            logger.debug("lastVal = {}, nextVal = {}", lastVal, nextVal);
            AxisValue firstVal = this.axisVals.firstElement();
            Longitude firstValLon = new Longitude(firstVal.value);
            Longitude lastValLon = new Longitude(lastVal);
            Longitude nextValLon = new Longitude(nextVal);
            if (firstValLon.isBetween(lastVal, nextVal) || lastValLon.getClockwiseDistanceTo(nextVal) > nextValLon.getClockwiseDistanceTo(firstVal.value)) {
                logger.debug("Axis wraps, creating new point with lon = {}", (firstVal.value + 360));
                this.axisVals.add(new AxisValue(firstVal.value + 360, firstVal.index));
            }
        }
        logger.debug("Created irregular {} axis", (this.isLongitude ? "longitude" : "latitude"));
    }

    /**
     * Uses a binary search algorithm to find the index of the point on the axis
     * whose value is closest to the given one.
     * @param point The {@link LatLonPoint}, which will have lon in range
     * [-180,180] and lat in range [-90,90]
     * @return the index that is nearest to this point, or -1 if the point is
     * out of range for the axis
     */
    public int getIndex(LatLonPoint point) {
        double target = this.isLongitude ? point.getLongitude() : point.getLatitude();
        logger.debug("Finding index for {} {} ...", this.isLongitude ? "lon" : "lat", target);
        int index = this.findNearest(target);
        if (index < 0 && this.isLongitude && target < 0) {
            index = this.findNearest(target + 360);
        }
        logger.debug("   ...index= {}", index);
        return index;
    }

    /**
     * Performs a binary search to find the index of the element of the array
     * whose value is closest to the target
     * @param values The array to search
     * @param target The value to search for
     * @return the index of the element in values whose value is closest to target,
     * or -1 if the target is out of range
     */
    private int findNearest(double target) {
        if (target < this.axisVals.firstElement().value || target > this.axisVals.lastElement().value) {
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
}
