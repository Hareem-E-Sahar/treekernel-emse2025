package net.sourceforge.webcompmath.draw.beans;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import net.sourceforge.webcompmath.awt.Computable;
import net.sourceforge.webcompmath.data.Constant;
import net.sourceforge.webcompmath.data.Value;
import net.sourceforge.webcompmath.draw.CoordinateRect;
import net.sourceforge.webcompmath.draw.DisplayCanvas;
import net.sourceforge.webcompmath.draw.Drawable;

/**
 * This convenience class provides a dimension or label for an arc of an oval.
 * You specify the center of the oval arc to be measured/labeled as (x1,y1). The
 * amount the oval extends in each direction is given by x2,y2 or by h,v. (Thus,
 * x2 or h is the HALF-width and y2 or v is the HALF-height.)You specify the
 * start and end angles as arcStart and arcEnd. The dimension arc has arrow
 * heads and a centered label, both of which can be turned off. The label is
 * drawn on the arc at an angle half way between arcStart and arcEnd; if your
 * arc is not part of the circle, the label may appear somewhat offset from the
 * center of the arc, as measured by curve length.
 * 
 * @author Tom Downey
 * 
 */
public class WCMDimensionedArcBean extends Drawable implements Computable {

    private static final long serialVersionUID = 3820854222341659106L;

    /**
	 * The x coordinate of the center point.
	 */
    protected Value x1 = new Constant(0);

    /**
	 * The y coordinate of the center point.
	 */
    protected Value y1 = new Constant(0);

    /**
	 * The half-width of the oval, in coordinate units. If null (the default),
	 * then h is used.
	 */
    protected Value x2 = null;

    /**
	 * The half-height of the oval, in coordinate units. If null (the default),
	 * then w is used.
	 */
    protected Value y2 = null;

    /**
	 * The half-width (horizontal) of the oval, in pixles. Ignored if x2 is not
	 * null.
	 */
    protected int h = 20;

    /**
	 * The half-height (vertical) of the oval, in pixles. Ignored if y2 is not
	 * null.
	 */
    protected int v = 20;

    /**
	 * Color of the dimension arc. Color will be black if this is null. This is
	 * used for the arc, arrow heads, and font.
	 */
    protected Color color = Color.black;

    /**
	 * Color of the background for the label. The default is white.
	 */
    protected Color bgColor = Color.white;

    /**
	 * The width, in pixels, of the dimension arc and arrow heads.
	 */
    protected int lineWidth = 1;

    /**
	 * The line style, WCMDrawGeometricBean.SOLID_STYLE or
	 * WCMDrawGeometricBean.DASHED_STYLE
	 */
    protected int lineStyle = WCMDrawGeometricBean.SOLID_STYLE;

    /**
	 * The type of arrow to add..
	 */
    protected int arrowType = WCMDrawGeometricBean.ARROW_FIXED_OPEN;

    /**
	 * Whether to put arrow heads on one, both, or neither end.
	 */
    protected int arrowDirection = WCMDrawGeometricBean.ARROW_BOTH;

    /**
	 * The size of the arrow head. Indicates the length of the "wings" in pixels
	 * for fixed type heads.
	 */
    protected float arrowLength = 5;

    /**
	 * Font for drawing string. If null, get font from graphics context.
	 */
    protected Font font;

    /**
	 * Indicates whether or not to show the label. Default is true.
	 */
    protected boolean labelShown = true;

    /**
	 * Number of decimal digits
	 */
    protected int numDecDigits = 2;

    /**
	 * String, possibly with #. This is used as a base to get the actual string
	 * that is drawn for the label or measure. # is used to indicate the arc
	 * extent in radians or some other value.
	 */
    protected String baseString = "#";

    /**
	 * Value to be substituted for # in the baseString.
	 */
    protected Value labelValue = new LV();

    /**
	 * Internal value that handles rounding.
	 */
    protected RV roundValue = new RV(labelValue, numDecDigits);

    /**
	 * The starting point of the arc, in radians.
	 */
    protected Value arcStart = new Constant(0);

    /**
	 * The ending point of the arc, in radians.
	 */
    protected Value arcEnd = new Constant(2 * Math.PI);

    private WCMDrawGeometricBean dimArc;

    private WCMDrawStringBean label;

    private boolean changed = true;

    /**
	 * Default constructor.
	 */
    public WCMDimensionedArcBean() {
        dimArc = new WCMDrawGeometricBean();
        dimArc.setShape(WCMDrawGeometricBean.ARC_CENTERED);
        dimArc.setArrowDirection(arrowDirection);
        dimArc.setArrowLength(arrowLength);
        dimArc.setArrowType(arrowType);
        dimArc.setColor(color);
        dimArc.setLineStyle(lineStyle);
        dimArc.setLineWidth(lineWidth);
        dimArc.setX1(x1);
        dimArc.setY1(y1);
        dimArc.setX2(x2);
        dimArc.setY2(y2);
        dimArc.setH(h);
        dimArc.setV(v);
        dimArc.setArcStart(arcStart);
        dimArc.setArcEnd(arcEnd);
        label = new WCMDrawStringBean();
        label.setColor(color);
        label.setBackgroundColor(bgColor);
        label.setPositioning(WCMDrawStringBean.CENTER_CENTER);
        label.setString(baseString);
        label.setVisible(labelShown);
        label.setReferencePoint(new AV(true), new AV(false));
        label.setValue1(roundValue);
        label.setClamp(false);
    }

    /**
	 * Get the arrow direction.
	 * 
	 * @return the arrowDirection
	 */
    public int getArrowDirection() {
        return arrowDirection;
    }

    /**
	 * Set the direction of the arrow head(s). One of
	 * WCMDrawGeometricBean.ARROW_NONE, ARROW_HEAD, ARROW_TAIL, or ARROW_BOTH.
	 * Default is ARROW_BOTH.
	 * 
	 * @param arrowDirection
	 *            the arrowDirection to set
	 */
    public void setArrowDirection(int arrowDirection) {
        this.arrowDirection = arrowDirection;
        dimArc.setArrowDirection(arrowDirection);
        needsRedraw();
    }

    /**
	 * Get the arrow head length.
	 * 
	 * @return the arrowLength
	 */
    public float getArrowLength() {
        return arrowLength;
    }

    /**
	 * Set the size of the arrow head in pixels (default is 10).
	 * 
	 * @param arrowLength
	 *            the arrowLength to set
	 */
    public void setArrowLength(float arrowLength) {
        this.arrowLength = arrowLength;
        dimArc.setArrowLength(arrowLength);
        needsRedraw();
    }

    /**
	 * Get the arrow type.
	 * 
	 * @return the arrowType
	 */
    public int getArrowType() {
        return arrowType;
    }

    /**
	 * Set the type of arrow head. One of WCMDrawGeometricBean.ARROW_FIXED_OPEN
	 * or ARROW_FIXED_CLOSED. ARROW_SCALED is not recommended.
	 * 
	 * @param arrowType
	 *            the arrowType to set
	 */
    public void setArrowType(int arrowType) {
        this.arrowType = arrowType;
        dimArc.setArrowType(arrowType);
        needsRedraw();
    }

    /**
	 * Get the string to display as the label or dimension.
	 * 
	 * @return the baseString
	 */
    public String getBaseString() {
        return baseString;
    }

    /**
	 * Set the string to display. Can include a #, which will be replaced by the
	 * current value of labelValue.
	 * 
	 * @param baseString
	 *            the baseString to set
	 */
    public void setBaseString(String baseString) {
        this.baseString = baseString;
        label.setString(baseString);
        needsRedraw();
    }

    /**
	 * Get the background color.
	 * 
	 * @return the color
	 */
    public Color getBackgroundColor() {
        return bgColor;
    }

    /**
	 * Set the background color for the label.
	 * 
	 * @param color
	 *            the color to set
	 */
    public void setBackgroundColor(Color color) {
        this.bgColor = color;
        label.setBackgroundColor(color);
        needsRedraw();
    }

    /**
	 * Get the color.
	 * 
	 * @return the color
	 */
    public Color getColor() {
        return color;
    }

    /**
	 * Set the color for the line and font.
	 * 
	 * @param color
	 *            the color to set
	 */
    public void setColor(Color color) {
        this.color = color;
        dimArc.setColor(color);
        label.setColor(color);
        needsRedraw();
    }

    /**
	 * Get the font.
	 * 
	 * @return the font
	 */
    @Override
    public Font getFont() {
        return font;
    }

    /**
	 * Set the font. If null, the font from the graphics context is used.
	 * 
	 * @param font
	 *            the font to set
	 */
    @Override
    public void setFont(Font font) {
        this.font = font;
        label.setFont(font);
        needsRedraw();
    }

    /**
	 * Get whether to show the label.
	 * 
	 * @return the labelShown
	 */
    public boolean isLabelShown() {
        return labelShown;
    }

    /**
	 * Set whether to show the label. Default is true;
	 * 
	 * @param labelShown
	 *            the labelShown to set
	 */
    public void setLabelShown(boolean labelShown) {
        this.labelShown = labelShown;
        label.setVisible(labelShown && isVisible());
        needsRedraw();
    }

    /**
	 * Get the label value.
	 * 
	 * @return the labelValue
	 */
    public Value getLabelValue() {
        return labelValue;
    }

    /**
	 * Set the label value. The default is the extent of the arc in radians
	 * (i.e., arcEnd - arcStart).
	 * 
	 * @param labelValue
	 *            the labelValue to set
	 */
    public void setLabelValue(Value labelValue) {
        this.labelValue = labelValue;
        roundValue.setValue(labelValue);
        needsRedraw();
    }

    /**
	 * Get the line style.
	 * 
	 * @return the lineStyle
	 */
    public int getLineStyle() {
        return lineStyle;
    }

    /**
	 * Set the line style. Default is WCMDrawGeometricBean.SOLID_STYLE.
	 * 
	 * @param lineStyle
	 *            the lineStyle to set
	 */
    public void setLineStyle(int lineStyle) {
        this.lineStyle = lineStyle;
        dimArc.setLineStyle(lineStyle);
        needsRedraw();
    }

    /**
	 * Get the line width.
	 * 
	 * @return the lineWidth
	 */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
	 * Set the line width, in pixels. If zero, the smallest line width will be
	 * drawn. Default is 1.
	 * 
	 * @param lineWidth
	 *            the lineWidth to set
	 */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        dimArc.setLineWidth(lineWidth);
    }

    /**
	 * Get the number of decimal digits.
	 * 
	 * @return the numDecDigits
	 */
    public int getDecDigits() {
        return numDecDigits;
    }

    /**
	 * Set the number of digits after the decimal point in the value. If 0, will
	 * round to the nearest integer. If negative, rounds off to positive powers
	 * of 10 (i.e., -2 will round to the nearest 100).
	 * 
	 * @param numDecDigits
	 *            the number of digits
	 */
    public void setDecDigits(int numDecDigits) {
        this.numDecDigits = numDecDigits;
        roundValue.setDigits(numDecDigits);
        needsRedraw();
    }

    /**
	 * Get the center point x coordinate.
	 * 
	 * @return the x1
	 */
    public Value getX1() {
        return x1;
    }

    /**
	 * Set the center point x coordinate.
	 * 
	 * @param x1
	 *            the x1 to set
	 */
    public void setX1(Value x1) {
        this.x1 = x1;
        dimArc.setX1(x1);
        needsRedraw();
    }

    /**
	 * Get the center point y coordinate.
	 * 
	 * @return the y1
	 */
    public Value getY1() {
        return y1;
    }

    /**
	 * Set the center point y coordinate.
	 * 
	 * @param y1
	 *            the y1 to set
	 */
    public void setY1(Value y1) {
        this.y1 = y1;
        dimArc.setY1(y1);
        needsRedraw();
    }

    /**
	 * Get the half-width in coordinate units.
	 * 
	 * @return the x2
	 */
    public Value getX2() {
        return x2;
    }

    /**
	 * Set the half-width in coordinate units. If null, h is used instead. The
	 * default is null.
	 * 
	 * @param x2
	 *            the x2 to set
	 */
    public void setX2(Value x2) {
        this.x2 = x2;
        dimArc.setX2(x2);
        needsRedraw();
    }

    /**
	 * Get the half-height in coordinate units.
	 * 
	 * @return the y2
	 */
    public Value getY2() {
        return y2;
    }

    /**
	 * Set the half-height in coordinate units. If null, v is used instead. The
	 * default is null.
	 * 
	 * @param y2
	 *            the y2 to set
	 */
    public void setY2(Value y2) {
        this.y2 = y2;
        dimArc.setY2(y2);
        needsRedraw();
    }

    /**
	 * Get the half-width in pixels.
	 * 
	 * @return the h
	 */
    public int getH() {
        return h;
    }

    /**
	 * Set the half-width (horizontal) in pixels. Sets x2 to null.
	 * 
	 * @param h
	 *            the h to set
	 */
    public void setH(int h) {
        this.h = h;
        dimArc.setH(h);
        setX2(null);
        needsRedraw();
    }

    /**
	 * Get the half-height in pixels.
	 * 
	 * @return the v
	 */
    public int getV() {
        return v;
    }

    /**
	 * Set the half-height (vertical) in pixels. Sets y2 to null.
	 * 
	 * @param v
	 *            the v to set
	 */
    public void setV(int v) {
        this.v = v;
        dimArc.setV(v);
        setY2(null);
        needsRedraw();
    }

    /**
	 * Get the ending point for arcs.
	 * 
	 * @return the arcEnd
	 */
    public Value getArcEnd() {
        return arcEnd;
    }

    /**
	 * Set tne ending point for arcs, in radians. The default is 2pi.
	 * 
	 * @param arcEnd
	 *            the arcEnd to set
	 */
    public void setArcEnd(Value arcEnd) {
        this.arcEnd = arcEnd;
        dimArc.setArcEnd(arcEnd);
        needsRedraw();
    }

    /**
	 * Get the starting point for arcs.
	 * 
	 * @return the arcStart
	 */
    public Value getArcStart() {
        return arcStart;
    }

    /**
	 * Set the starting point for arcs, in radians. The default is 0.
	 * 
	 * @param arcStart
	 *            the arcStart to set
	 */
    public void setArcStart(Value arcStart) {
        this.arcStart = arcStart;
        dimArc.setArcStart(arcStart);
        needsRedraw();
    }

    /**
	 * Draw the dimension arc and label. Does nothing, as the actual embedded
	 * Drawables handle the work.
	 * 
	 * @see net.sourceforge.webcompmath.draw.Drawable#draw(java.awt.Graphics,
	 *      boolean)
	 */
    @Override
    public void draw(Graphics g, boolean coordsChanged) {
    }

    /**
	 * Recompute the values.
	 */
    public void compute() {
        changed = true;
        needsRedraw();
    }

    /**
	 * X coordinate of the label. This is re-computed when the doValues() method
	 * is called.
	 */
    protected double labelX = Double.NaN;

    /**
	 * Y coordinate of the label. This is re-computed when the doValues() method
	 * is called.
	 */
    protected double labelY = Double.NaN;

    /**
	 * Value of arcStart. This is re-computed when the doValues() method is
	 * called.
	 * 
	 */
    protected double asVal = Double.NaN;

    /**
	 * Value of arcEnd. This is re-computed when the doValues() method is
	 * called.
	 * 
	 */
    protected double aeVal = Double.NaN;

    private void doValues() {
        double x1Val = 0, x2Val = 0, y1Val = 0, y2Val = 0;
        double width, height;
        if (x1 != null) {
            x1Val = x1.getVal();
        }
        if (y1 != null) {
            y1Val = y1.getVal();
        }
        if (x2 != null) {
            x2Val = x2.getVal();
            width = x2Val;
        } else {
            width = coords.pixelToX(coords.xToPixelF(x1Val) + h) - x1Val;
        }
        if (y2 != null) {
            y2Val = y2.getVal();
            height = y2Val;
        } else {
            height = coords.pixelToY(coords.yToPixelF(y1Val) - v) - y1Val;
        }
        if (arcStart != null) {
            asVal = arcStart.getVal();
        }
        if (arcEnd != null) {
            aeVal = arcEnd.getVal();
        }
        double angle = (aeVal + asVal) / 2;
        double radius = height * width / Math.sqrt(Math.pow(width, 2) * Math.pow(Math.sin(angle), 2) + Math.pow(height, 2) * Math.pow(Math.cos(angle), 2));
        labelX = radius * Math.cos(angle) + x1Val;
        labelY = radius * Math.sin(angle) + y1Val;
        changed = false;
    }

    private class AV implements Value {

        private static final long serialVersionUID = 3906931162256586034L;

        private boolean isXCoord;

        AV(boolean isXCoord) {
            this.isXCoord = isXCoord;
        }

        /**
		 * Get the value.
		 * 
		 * @see net.sourceforge.webcompmath.data.Value#getVal()
		 */
        public double getVal() {
            if (changed) {
                doValues();
            }
            if (isXCoord) {
                return labelX;
            } else {
                return labelY;
            }
        }
    }

    private class LV implements Value {

        private static final long serialVersionUID = 3906931162256586034L;

        /**
		 * Get the value.
		 * 
		 * @see net.sourceforge.webcompmath.data.Value#getVal()
		 */
        public double getVal() {
            if (changed) {
                doValues();
            }
            return aeVal - asVal;
        }
    }

    private class RV implements Value {

        private static final long serialVersionUID = -5190293174188692833L;

        private Value lv;

        int digits;

        RV(Value lv, int digits) {
            this.lv = lv;
            this.digits = digits;
        }

        /**
		 * @param lv
		 */
        public void setValue(Value lv) {
            this.lv = lv;
        }

        /**
		 * @param digits
		 */
        public void setDigits(int digits) {
            this.digits = digits;
        }

        /**
		 * @see net.sourceforge.webcompmath.data.Value#getVal()
		 */
        public double getVal() {
            double d = Math.pow(10, digits);
            return Math.round(lv.getVal() * d) / d;
        }
    }

    /**
	 * Override to set the owner data for the embedded drawables and add them to
	 * a coordinate rect.
	 * 
	 * @see net.sourceforge.webcompmath.draw.Drawable#setOwnerData(net.sourceforge.webcompmath.draw.DisplayCanvas,
	 *      net.sourceforge.webcompmath.draw.CoordinateRect)
	 */
    @Override
    protected void setOwnerData(DisplayCanvas canvas, CoordinateRect coords) {
        super.setOwnerData(canvas, coords);
        if (coords != null) {
            coords.add(dimArc);
            coords.add(label);
            doValues();
        } else {
            coords.remove(dimArc);
            coords.remove(label);
        }
    }

    /**
	 * Set the visibility of this WCMDimensionedArcBean. If show is false, then
	 * it is hidden. If it is true, it is shown.
	 * 
	 * @see net.sourceforge.webcompmath.draw.Drawable#setVisible(boolean)
	 */
    @Override
    public void setVisible(boolean show) {
        super.setVisible(show);
        dimArc.setVisible(show);
        label.setVisible(show && labelShown);
    }
}
