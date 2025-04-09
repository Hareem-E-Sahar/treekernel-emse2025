package Entities.Estimate;

import Entities.Entity;
import Entities.XMLRepresentation;
import XML.XMLBuilder;
import java.util.Vector;

/**
 * This class represents a scale related to a quality.
 * 
 * It may be used to
 * temporarily store the highest and lowest values reported to it, to be able
 * to calculate a fraction representing where on the scale a value are, but
 * it will not associate with values and keep track on them.
 *
 * Is also used for cost scales, but this should probably get its own class,
 * some time...
 * @author pontuslp
 */
public class Scale implements XMLRepresentation {

    String unit;

    int significantDigits;

    boolean higherIsBetter;

    double lowEnd;

    double highEnd;

    public Scale(String unit, int significantDigits, boolean higherIsBetter) {
        this.unit = unit;
        this.significantDigits = significantDigits;
        this.higherIsBetter = higherIsBetter;
        lowEnd = Double.MAX_VALUE;
        highEnd = Double.MIN_VALUE;
    }

    /**
	 * Checks the parameter to the current boundaries and updates the closest
	 * boundary if it's outside the interval.
	 * @param value the value to update the boundaries with
	 */
    public void updateBoundaries(double value) {
        lowEnd = Math.min(lowEnd, value);
        highEnd = Math.max(highEnd, value);
    }

    /**
	 * Attempts to make the boundaries fit for display. To be used if a roadmap
	 * is to be generated with 11 evenly spaces markers containing the numbers
	 * instead of the markers for estimations.
	 */
    public void finalizeBoundaries() {
        if (highEnd - lowEnd == 0) {
            return;
        }
        double diff = highEnd - lowEnd;
        double diffPower = Math.floor(Math.log10(diff));
        double mean = (highEnd + lowEnd) / 2;
        double meanPower = Math.floor(Math.log10(mean));
        mean = mean / (Math.pow(10, meanPower - significantDigits + 1));
        diff = diff / (Math.pow(10, diffPower - significantDigits + 1));
        mean = Math.round(mean);
        diff = Math.ceil(diff);
        mean = mean * (Math.pow(10, meanPower - significantDigits + 1));
        diff = diff * (Math.pow(10, diffPower - significantDigits + 1));
        lowEnd = mean - (diff / 2);
        highEnd = mean + (diff / 2);
    }

    /**
	 * Calculates a double between 0 and 1 representing the values position on
	 * this scale. 1 indicates that it is the "best" value on the scale.
	 * @param value 
	 * @return (value-lowEnd)/(highEnd-lowEnd), or 0 if denominator is 0
	 */
    public double calculateFraction(double value) {
        if (lowEnd == highEnd) return 0;
        return higherIsBetter ? (value - lowEnd) / (highEnd - lowEnd) : (highEnd - value) / (highEnd - lowEnd);
    }

    /**
	 * Resets the scale for recalculations of the boundaries.
	 */
    public void resetBoundaries() {
        lowEnd = Double.MAX_VALUE;
        highEnd = Double.MIN_VALUE;
    }

    public String getUnit() {
        return unit;
    }

    public int getSignificantDigits() {
        return significantDigits;
    }

    public boolean isHigherBetter() {
        return higherIsBetter;
    }

    /**
	 * Returns the value on the scale considered as the best
	 * @return the best value
	 */
    public double getBadEnd() {
        return higherIsBetter ? lowEnd : highEnd;
    }

    /**
	 * Returns the value on the scale considered as the worst
	 * @return the worst value
	 */
    public double getGoodEnd() {
        return higherIsBetter ? highEnd : lowEnd;
    }

    @Override
    public String toString() {
        return "Scale";
    }

    @Override
    public Scale clone() {
        return new Scale(unit.substring(0), significantDigits, higherIsBetter);
    }

    public boolean equals(Scale s) {
        return this.unit.equals(s.unit) && this.significantDigits == s.significantDigits && (this.higherIsBetter == s.higherIsBetter);
    }

    @Override
    public XMLBuilder toXML(XMLBuilder xb) {
        xb.append("<Scale unit=\"");
        xb.appendField(unit);
        xb.append("\" significantDigits=\"");
        xb.appendField("" + significantDigits);
        xb.append("\" higherIsBetter=\"");
        xb.appendField("" + higherIsBetter);
        xb.append("\" />");
        return xb;
    }
}
