package net.sourceforge.webcompmath.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import net.sourceforge.webcompmath.awt.Computable;
import net.sourceforge.webcompmath.data.Constant;
import net.sourceforge.webcompmath.data.Function;
import net.sourceforge.webcompmath.data.Parser;
import net.sourceforge.webcompmath.data.SimpleFunction;
import net.sourceforge.webcompmath.data.Value;
import net.sourceforge.webcompmath.data.Variable;
import net.sourceforge.webcompmath.draw.RiemannSumRects;

/**
 * A RiemannSlice shows one slice in a Riemann sum, useful for area and volume
 * applets. It can show a rectangle (for area Riemann sums), a washer (for
 * volumes of revolution), various geometric shapes (for volumes of known cross
 * section) or a segment of a circle for polar area. The volume types are
 * designed to give a 3d look.
 * <p>
 * Washers and some known cross section shapes come in two halves. You add the
 * rear half to a DisplayCanvas before the other drawables, then add the front
 * half at the end. This improves the 3D effect. You can use a partly
 * transparent fill color (which is the default) to help enhance things.
 * <p>
 * The shape type and method type constants are defined in RiemannSumRects. In
 * general you should not use the TRAPEZOID method for the 3D volume
 * shapes, as the drawing algorithm doesn't really work well in this case.
 */
public class RiemannSlice extends Drawable implements Computable {

    private static final long serialVersionUID = -5673548881859233319L;

    private Color fillColor = new Color(192, 192, 192, 200);

    private Color outlineColor = Color.BLACK;

    private Function upperFunction, upperDeriv, lowerFunction, lowerDeriv;

    private double[] param = new double[1];

    private boolean inverse = false;

    private Value coordinate = new Constant(0);

    private float sliceWidth = 1;

    private Value xMin, xMax;

    private Value axis;

    /**
	 * The width, in pixels, of the outlines of the rects. It is restricted to
	 * being an integer in the range from 0 to 10. A value of 0 means that lines
	 * won't be drawn at all; this would only be useful for a filled rectangle
	 * that has a colored interior.
	 */
    private int lineWidth = 1;

    private int method = RiemannSumRects.LEFTENDPOINT;

    private int shape = RiemannSumRects.RECTANGLE;

    private float aspect = 0.3f;

    private float slant = 0.1f;

    /**
	 * Get the current fill color
	 * 
	 * @return fill color
	 */
    public Color getFillColor() {
        return fillColor;
    }

    /**
	 * Set the fill color. The default color is a light grey, partly
	 * transparent. RiemannSlice only supports one color (i.e., does not show a
	 * separate color for negative areas, like RiemannSumRects can do)
	 * 
	 * @param c
	 *            fill color
	 */
    public void setFillColor(Color c) {
        if (c != null) {
            fillColor = c;
            needsRedraw();
        }
    }

    /**
	 * Set the color that will be used to draw outlines around the rects. If
	 * this is null, then no outlines are drawn. The default is black.
	 * 
	 * @param c
	 *            outline color
	 */
    public void setOutlineColor(Color c) {
        outlineColor = c;
        needsRedraw();
    }

    /**
	 * Get the color that is used to draw outlines around the rects. If this is
	 * null, then no outlines are drawn.
	 * 
	 * @return outline color
	 */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
	 * Set the upper function that defines the top of the slice. If null,
	 * nothing is drawn. The function, if non-null, must have arity 1, or an
	 * IllegalArgumentException is thrown.
	 * 
	 * For POLAR shapes, this gives r as a function of theta.
	 * 
	 * @param func
	 *            function for top of slice
	 */
    public void setUpperFunction(Function func) {
        if (func != null && func.getArity() != 1) throw new IllegalArgumentException("Function for Riemann slice must have arity 1.");
        this.upperFunction = func;
        upperDeriv = (func == null) ? null : func.derivative(1);
        needsRedraw();
    }

    /**
	 * Returns the upper function for the Riemann slice. Can be null.
	 * 
	 * @return upper function
	 */
    public Function getUpperFuction() {
        return upperFunction;
    }

    /**
	 * Set the lower function, when computing area between curves. If null, then
	 * the horizontal axis is used. The function, if non-null, must have arity
	 * 1, or an IllegalArgumentException is thrown. Not used for POLAR shapes.
	 * 
	 * @param func
	 *            lower function
	 */
    public void setLowerFunction(Function func) {
        if (func != null && func.getArity() != 1) throw new IllegalArgumentException("Function for Riemann slice must have arity 1.");
        if (func == null) {
            lowerFunction = new SimpleFunction(new Parser().parse("0"), new Variable("x"));
        } else {
            lowerFunction = func;
        }
        lowerDeriv = lowerFunction.derivative(1);
        needsRedraw();
    }

    /**
	 * Returns the lower function. Can be null.
	 * 
	 * @return lower function
	 */
    public Function getLowerFuction() {
        return lowerFunction;
    }

    /**
	 * Set the method used to draw the slice.
	 * 
	 * @param m
	 *            can be: RiemannSumRects.LEFTENDPOINT, RIGHTENDPOINT, MIDPOINT,
	 *            CIRCUMSCRIBED, INSCRIBED or TRAPEZOID (these are integers
	 *            ranging from 0 to 5, respectively) TRAPEZOID is not valid for
	 *            POLAR.
	 */
    public void setMethod(int m) {
        method = m;
        needsRedraw();
    }

    /**
	 * Return the current method used to draw the slice
	 * 
	 * @return can be: RiemannSumRects.LEFTENDPOINT, RIGHTENDPOINT, MIDPOINT,
	 *         CIRCUMSCRIBED, INSCRIBED or TRAPEZOID (these are integers ranging
	 *         from 0 to 5, respectively)
	 */
    public int getMethod() {
        return method;
    }

    /**
	 * Get the coordinate where the slice is to be drawn.
	 * 
	 * @return the coordinate
	 */
    public Value getCoordinate() {
        return coordinate;
    }

    /**
	 * Set the coordinate where the slice is to be drawn.
	 * 
	 * @param coordinate
	 *            the coordinate to set
	 */
    public void setCoordinate(Value coordinate) {
        this.coordinate = coordinate;
        needsRedraw();
    }

    /**
	 * Get the shape for this slice.
	 * 
	 * @return the shape
	 */
    public int getShape() {
        return shape;
    }

    /**
	 * Set the shape for this slice. The default is RiemannSumRects.RECTANGLE.
	 * Can be one of: RiemannSumRects.RECTANGLE or POLAR for areas, WASHER_FRONT
	 * or WASHER_REAR for volumes of revolution, and SQUARE_SIDE,
	 * SQUARE_DIAG_FRONT, SQUARE_DIAG_REAR, SEMICIRCLE, CIRCLE_FRONT,
	 * CIRCLE_REAR, EQUILATERAL, ISOSCELES_HYP, or ISOSCELES_LEG for volumes of
	 * known cross section.
	 * 
	 * @param shape
	 *            the shape to set
	 */
    public void setShape(int shape) {
        this.shape = shape;
        needsRedraw();
    }

    /**
	 * Get the width of the slice, in pixels.
	 * 
	 * @return the width
	 */
    public float getSliceWidth() {
        return sliceWidth;
    }

    /**
	 * Set the width of the slice. For all shapes except POLAR this is in pixels
	 * and must be > 0. If the width is 1, then the slice will be drawn with no
	 * width (i.e., just a line for rectangles, just a very thin washer).
	 * 
	 * For POLAR shapes, this sets the width in degrees.
	 * 
	 * @param width
	 *            the width to set
	 */
    public void setSliceWidth(float width) {
        this.sliceWidth = width;
        needsRedraw();
    }

    /**
	 * Set the width, in pixels, of outline lines. Must be from 0 to 10. If set
	 * to 0, the thinnest possible line is drawn.
	 * 
	 * @param width
	 *            line width
	 */
    public void setLineWidth(int width) {
        if (width != lineWidth) {
            lineWidth = width;
            if (lineWidth > 10) {
                lineWidth = 10;
            } else if (lineWidth < 0) {
                lineWidth = 0;
            }
            needsRedraw();
        }
    }

    /**
	 * Get the width, in pixels, of outline lines.
	 * 
	 * @return line width
	 */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
	 * Get whether to display inverse version
	 * 
	 * @return true if inverse
	 */
    public boolean isInverse() {
        return inverse;
    }

    /**
	 * Set whether to show as inverse. Not applicable for POLAR shapes.
	 * 
	 * @param inverse
	 *            true for inverse
	 */
    public void setInverse(boolean inverse) {
        this.inverse = inverse;
        needsRedraw();
    }

    /**
	 * Get the max x value
	 * 
	 * @return Returns the xMax.
	 */
    public Value getXMax() {
        return xMax;
    }

    /**
	 * Set the max x value. If Double.NaN, then use the current xMax of the
	 * graph
	 * 
	 * @param max
	 *            The xMax to set.
	 */
    public void setXMax(Value max) {
        xMax = max;
        needsRedraw();
    }

    /**
	 * Get the min x value
	 * 
	 * @return Returns the xMin.
	 */
    public Value getXMin() {
        return xMin;
    }

    /**
	 * Set the min x value. If Double.NaN, then use the current xMin of the
	 * graph
	 * 
	 * @param min
	 *            The xMin to set.
	 */
    public void setXMin(Value min) {
        xMin = min;
        needsRedraw();
    }

    /**
	 * Get the axis of revolution
	 * 
	 * @return the axis
	 */
    public Value getAxis() {
        return axis;
    }

    /**
	 * Set the axis of revolution. Only valid for washer shapes.
	 * 
	 * @param axis
	 *            the axis to set
	 */
    public void setAxis(Value axis) {
        this.axis = axis;
        needsRedraw();
    }

    /**
	 * Get the aspect ratio
	 * 
	 * @return the aspect
	 */
    public float getAspect() {
        return aspect;
    }

    /**
	 * Set the aspect ration (width to height). Default is 0.3. Only used for
	 * 3d-style shapes.
	 * 
	 * @param aspect
	 *            the aspect to set
	 */
    public void setAspect(float aspect) {
        this.aspect = aspect;
        needsRedraw();
    }

    /**
	 * Get the amount of slant offset
	 * 
	 * @return the slant
	 */
    public float getSlant() {
        return slant;
    }

    /**
	 * Set the slant offset. This is from 0 to 1, with 0 being no slant and 1
	 * being 45 degrees. The default is 0.1. Only used for 3d-style shapes.
	 * 
	 * @param slant
	 *            the slant to set
	 */
    public void setSlant(float slant) {
        this.slant = slant;
        needsRedraw();
    }

    /**
	 * This is generally called by a Controller. Indicates that all data should
	 * be recomputed because input values that the data depends on might have
	 * changed.
	 */
    public void compute() {
        needsRedraw();
    }

    /**
	 * Construct a RiemannSumRects object that initially has nothing to draw.
	 */
    public RiemannSlice() {
        this(null, null, null, RiemannSumRects.LEFTENDPOINT, RiemannSumRects.RECTANGLE);
    }

    /**
	 * Construct a new RiemannSlice object.
	 * 
	 * @param uf
	 *            upper function
	 * @param lf
	 *            the lower function, may be null to use the horizontal axis
	 * @param coord
	 *            a Value object representing the coordinate where the slice is
	 *            to be drawn
	 * 
	 * @param method
	 *            the sum method; default is RiemannSumRects.LEFTENDPOINT
	 * 
	 * @param shape
	 *            the slice shape; default is RiemannSumRects.RECTANGLE
	 * 
	 */
    public RiemannSlice(Function uf, Function lf, Value coord, int method, int shape) {
        if (coord != null) {
            coordinate = coord;
        }
        upperFunction = uf;
        lowerFunction = lf;
        if (uf != null) upperDeriv = upperFunction.derivative(1);
        if (lf == null) {
            lowerFunction = new SimpleFunction(new Parser().parse("0"), new Variable("x"));
        } else {
            lowerFunction = lf;
        }
        lowerDeriv = lowerFunction.derivative(1);
        this.method = method;
        this.shape = shape;
    }

    /**
	 * Draw the Rieman slice. This is generally called by an object of class
	 * CoordinateRect. It is also called by RiemannSumRects when that class is
	 * drawing multiple 3D slices.
	 * 
	 * @param g
	 *            graphics context
	 * @param coordsChanged
	 *            true or false
	 */
    @Override
    public void draw(Graphics g, boolean coordsChanged) {
        if (upperFunction == null || coords == null) return;
        double[] xp = new double[4];
        double[] yp = new double[4];
        double x = coordinate.getVal();
        float px = xToPixel(x);
        double xMinVal, xMaxVal;
        if (xMin != null) {
            xMinVal = xMin.getVal();
            if (Double.isNaN(xMinVal) || Double.isInfinite(xMinVal)) {
                if (!inverse) {
                    xMinVal = coords.getXmin();
                } else {
                    xMinVal = coords.getYmin();
                }
            }
        } else {
            if (!inverse) {
                xMinVal = coords.getXmin();
            } else {
                xMinVal = coords.getYmin();
            }
        }
        if (xMax != null) {
            xMaxVal = xMax.getVal();
            if (Double.isNaN(xMaxVal) || Double.isInfinite(xMaxVal)) {
                if (!inverse) {
                    xMaxVal = coords.getXmax();
                } else {
                    xMaxVal = coords.getYmax();
                }
            }
        } else {
            if (!inverse) {
                xMaxVal = coords.getXmax();
            } else {
                xMaxVal = coords.getYmax();
            }
        }
        if (xMinVal > xMaxVal) {
            double temp = xMinVal;
            xMinVal = xMaxVal;
            xMaxVal = temp;
        }
        if (shape != RiemannSumRects.POLAR) {
            if (sliceWidth == 1) {
                if (x > xMaxVal) {
                    x = xMaxVal;
                } else if (x < xMinVal) {
                    x = xMinVal;
                }
                xp[0] = xp[1] = xp[2] = xp[3] = x;
            } else {
                double xminus = pixelToX(px - (!inverse ? sliceWidth : -sliceWidth) / 2);
                double xplus = pixelToX(px + (!inverse ? sliceWidth : -sliceWidth) / 2);
                if (xplus <= xMaxVal && xminus >= xMinVal) {
                    xp[0] = xp[3] = xminus;
                    xp[1] = xp[2] = xplus;
                } else if (xplus > xMaxVal) {
                    xp[1] = xp[2] = xMaxVal;
                    xp[0] = xp[3] = Math.max(pixelToX(xToPixel(xMaxVal) - (!inverse ? sliceWidth : -sliceWidth)), xMinVal);
                } else {
                    xp[0] = xp[3] = xMinVal;
                    xp[1] = xp[2] = Math.min(pixelToX(xToPixel(xMinVal) + (!inverse ? sliceWidth : -sliceWidth)), xMaxVal);
                }
            }
            switch(method) {
                case RiemannSumRects.LEFTENDPOINT:
                    param[0] = xp[0];
                    yp[0] = yp[1] = lowerFunction.getVal(param);
                    yp[2] = yp[3] = upperFunction.getVal(param);
                    break;
                case RiemannSumRects.RIGHTENDPOINT:
                    param[0] = xp[1];
                    yp[0] = yp[1] = lowerFunction.getVal(param);
                    yp[2] = yp[3] = upperFunction.getVal(param);
                    break;
                case RiemannSumRects.MIDPOINT:
                    param[0] = (xp[0] + xp[1]) / 2;
                    yp[0] = yp[1] = lowerFunction.getVal(param);
                    yp[2] = yp[3] = upperFunction.getVal(param);
                    break;
                case RiemannSumRects.CIRCUMSCRIBED:
                    yp[0] = yp[1] = searchMax(lowerFunction, lowerDeriv, xp[0], xp[1], 1);
                    yp[2] = yp[3] = searchMax(upperFunction, upperDeriv, xp[3], xp[2], 1);
                    break;
                case RiemannSumRects.INSCRIBED:
                    yp[0] = yp[1] = searchMin(lowerFunction, lowerDeriv, xp[0], xp[1], 1);
                    yp[2] = yp[3] = searchMin(upperFunction, upperDeriv, xp[3], xp[2], 1);
                    break;
                case RiemannSumRects.TRAPEZOID:
                    param[0] = xp[0];
                    yp[0] = lowerFunction.getVal(param);
                    param[0] = xp[1];
                    yp[1] = lowerFunction.getVal(param);
                    param[0] = xp[2];
                    yp[2] = upperFunction.getVal(param);
                    param[0] = xp[3];
                    yp[3] = upperFunction.getVal(param);
                    break;
                default:
                    break;
            }
        } else {
            double dtheta = sliceWidth / 180 * Math.PI;
            double xminus = x - dtheta / 2;
            double xplus = x + dtheta / 2;
            if (xplus <= xMaxVal && xminus >= xMinVal) {
                xp[0] = xp[3] = xminus;
                xp[1] = xp[2] = xplus;
            } else if (xplus > xMaxVal) {
                xp[1] = xp[2] = xMaxVal;
                xp[0] = xp[3] = xMaxVal - dtheta;
            } else {
                xp[0] = xp[3] = xMinVal;
                xp[1] = xp[2] = xMinVal + dtheta;
            }
            switch(method) {
                case RiemannSumRects.LEFTENDPOINT:
                    param[0] = xp[0];
                    yp[0] = yp[1] = yp[2] = yp[3] = upperFunction.getVal(param);
                    break;
                case RiemannSumRects.RIGHTENDPOINT:
                    param[0] = xp[1];
                    yp[0] = yp[1] = yp[2] = yp[3] = upperFunction.getVal(param);
                    break;
                case RiemannSumRects.MIDPOINT:
                    param[0] = (xp[0] + xp[1]) / 2;
                    yp[0] = yp[1] = yp[2] = yp[3] = upperFunction.getVal(param);
                    break;
                case RiemannSumRects.CIRCUMSCRIBED:
                    yp[0] = yp[1] = yp[2] = yp[3] = searchMax(upperFunction, upperDeriv, xp[3], xp[2], 1);
                    break;
                case RiemannSumRects.INSCRIBED:
                    yp[0] = yp[1] = yp[2] = yp[3] = searchMin(upperFunction, upperDeriv, xp[3], xp[2], 1);
                    break;
                case RiemannSumRects.TRAPEZOID:
                    yp[0] = yp[1] = yp[2] = yp[3] = 0;
                    break;
                default:
                    break;
            }
        }
        float[] xPixel = new float[4];
        float[] yPixel = new float[4];
        for (int i = 0; i < 4; i++) {
            xPixel[i] = xToPixel(xp[i]);
            yPixel[i] = yToPixel(yp[i]);
        }
        Graphics2D g2 = (Graphics2D) g;
        Color saveColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();
        RenderingHints oldHints = g2.getRenderingHints();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        switch(shape) {
            case RiemannSumRects.RECTANGLE:
                GeneralPath gp = new GeneralPath();
                Line2D line = new Line2D.Float();
                if (sliceWidth != 1) {
                    if (!inverse) {
                        g2.setColor(fillColor);
                        gp.moveTo(xPixel[0], yPixel[0]);
                        gp.lineTo(xPixel[1], yPixel[1]);
                        gp.lineTo(xPixel[2], yPixel[2]);
                        gp.lineTo(xPixel[3], yPixel[3]);
                        gp.closePath();
                        g2.fill(gp);
                        if (outlineColor != null) {
                            g2.setColor(outlineColor);
                            g2.draw(gp);
                        }
                    } else {
                        g2.setColor(fillColor);
                        gp.moveTo(yPixel[0], xPixel[0]);
                        gp.lineTo(yPixel[1], xPixel[1]);
                        gp.lineTo(yPixel[2], xPixel[2]);
                        gp.lineTo(yPixel[3], xPixel[3]);
                        gp.closePath();
                        g2.fill(gp);
                        if (outlineColor != null) {
                            g2.setColor(outlineColor);
                            g2.draw(gp);
                        }
                    }
                } else if (outlineColor != null) {
                    g2.setColor(outlineColor);
                    if (!inverse) {
                        line.setLine(xPixel[0], yPixel[0], xPixel[3], yPixel[3]);
                        g2.draw(line);
                    } else {
                        line.setLine(yPixel[0], xPixel[0], yPixel[3], xPixel[3]);
                        g2.draw(line);
                    }
                }
                break;
            case RiemannSumRects.WASHER_FRONT:
                {
                    double axisVal = axis != null ? axis.getVal() : 0;
                    Arc2D outerArc = makeOuterArc(xp[0], yp[0], yp[3], axisVal, shape);
                    Arc2D innerArc = makeInnerArc(xp[0], yp[0], yp[3], axisVal, shape);
                    Area area = new Area();
                    if (innerArc != null) {
                        area = new Area(outerArc);
                        area.subtract(new Area(innerArc));
                        g2.setColor(fillColor);
                        g2.fill(area);
                        g2.setColor(outlineColor);
                        g2.draw(innerArc);
                        if (sliceWidth > 1) {
                            Arc2D outerRightArc = makeOuterArc(xp[1], yp[1], yp[2], axisVal, shape);
                            Arc2D innerRightArc = makeInnerArc(xp[1], yp[1], yp[2], axisVal, shape);
                            drawEdge(innerArc, innerRightArc, g2);
                            drawEdge(outerArc, outerRightArc, g2);
                            g2.setColor(fillColor);
                            area = new Area(outerRightArc);
                            area.subtract(new Area(innerRightArc));
                            g2.fill(area);
                            g2.setColor(outlineColor);
                            g2.draw(outerRightArc);
                            g2.draw(innerRightArc);
                        }
                        g2.draw(outerArc);
                    } else {
                        g2.setColor(fillColor);
                        g2.fill(outerArc);
                        g2.setColor(outlineColor);
                        if (sliceWidth > 1) {
                            Arc2D outerRightArc = makeOuterArc(xp[1], yp[1], yp[2], axisVal, shape);
                            drawEdge(outerArc, outerRightArc, g2);
                            g2.setColor(fillColor);
                            g2.fill(outerRightArc);
                            g2.setColor(outlineColor);
                            g2.draw(outerRightArc);
                        }
                        g2.draw(outerArc);
                    }
                }
                break;
            case RiemannSumRects.WASHER_REAR:
                {
                    double axisVal = axis != null ? axis.getVal() : 0;
                    Arc2D outerArc = makeOuterArc(xp[0], yp[0], yp[3], axisVal, shape);
                    Arc2D innerArc = makeInnerArc(xp[0], yp[0], yp[3], axisVal, shape);
                    if (innerArc != null) {
                        g2.setColor(fillColor);
                        Area area = new Area(outerArc);
                        area.subtract(new Area(innerArc));
                        g2.fill(area);
                        g2.setColor(outlineColor);
                        g2.draw(outerArc);
                        if (sliceWidth > 1) {
                            Arc2D outerRightArc = makeOuterArc(xp[1], yp[1], yp[2], axisVal, shape);
                            Arc2D innerRightArc = makeInnerArc(xp[1], yp[1], yp[2], axisVal, shape);
                            drawEdge(outerRightArc, outerArc, g2);
                            drawEdge(innerRightArc, innerArc, g2);
                            g2.setColor(fillColor);
                            area = new Area(outerRightArc);
                            area.subtract(new Area(innerRightArc));
                            g2.fill(area);
                            g2.setColor(outlineColor);
                            g2.draw(outerRightArc);
                            g2.draw(innerRightArc);
                        }
                        g2.draw(innerArc);
                    } else {
                        g2.setColor(fillColor);
                        g2.fill(outerArc);
                        g2.setColor(outlineColor);
                        g2.draw(outerArc);
                        if (sliceWidth > 1) {
                            Arc2D outerRightArc = makeOuterArc(xp[1], yp[1], yp[2], axisVal, shape);
                            drawEdge(outerRightArc, outerArc, g2);
                            g2.setColor(fillColor);
                            g2.fill(outerRightArc);
                            g2.setColor(outlineColor);
                            g2.draw(outerRightArc);
                        }
                    }
                }
                break;
            case RiemannSumRects.SQUARE_SIDE:
                {
                    GeneralPath leftSquare = makeSquare(xPixel[0], yPixel[0], yPixel[3]);
                    g2.setColor(fillColor);
                    g2.fill(leftSquare);
                    g2.setColor(outlineColor);
                    g2.draw(leftSquare);
                    if (sliceWidth > 1) {
                        GeneralPath rightSquare = makeSquare(xPixel[1], yPixel[1], yPixel[2]);
                        drawGPEdges(leftSquare, rightSquare, g2, 4, true);
                        g2.setColor(fillColor);
                        g2.fill(rightSquare);
                        g2.setColor(outlineColor);
                        g2.draw(rightSquare);
                    }
                }
                break;
            case RiemannSumRects.SQUARE_DIAG_FRONT:
            case RiemannSumRects.SQUARE_DIAG_REAR:
                {
                    GeneralPath leftTriangle = makeTriangle(xp[3], yp[3], xp[0], yp[0], shape);
                    g2.setColor(fillColor);
                    g2.fill(leftTriangle);
                    drawTriangleSides(leftTriangle, g2);
                    if (sliceWidth > 1) {
                        GeneralPath rightTriangle = makeTriangle(xp[2], yp[2], xp[1], yp[1], shape);
                        drawGPEdges(leftTriangle, rightTriangle, g2, 3, false);
                        g2.setColor(fillColor);
                        g2.fill(rightTriangle);
                        drawTriangleSides(rightTriangle, g2);
                    }
                }
                break;
            case RiemannSumRects.SEMICIRCLE:
            case RiemannSumRects.CIRCLE_FRONT:
            case RiemannSumRects.CIRCLE_REAR:
                {
                    Arc2D leftArc = makeSemiCircle(xp[0], yp[0], yp[3], shape);
                    g2.setColor(fillColor);
                    g2.fill(leftArc);
                    if (sliceWidth > 1) {
                        Arc2D rightArc = makeSemiCircle(xp[1], yp[1], yp[2], shape);
                        drawEdge(leftArc, rightArc, g2);
                        g2.setColor(fillColor);
                        g2.fill(rightArc);
                        g2.setColor(outlineColor);
                        if (shape != RiemannSumRects.CIRCLE_REAR) {
                            g2.draw(leftArc);
                        }
                        g2.draw(rightArc);
                        if (shape == RiemannSumRects.SEMICIRCLE) {
                            g2.draw(makeLine(xp[1], yp[1], xp[1], yp[2]));
                        }
                    }
                }
                break;
            case RiemannSumRects.EQUILATERAL:
            case RiemannSumRects.ISOSCELES_HYP:
            case RiemannSumRects.ISOSCELES_LEG:
                {
                    GeneralPath leftTriangle = makeTriangle(xp[3], yp[3], xp[0], yp[0], shape);
                    g2.setColor(fillColor);
                    g2.fill(leftTriangle);
                    g2.setColor(outlineColor);
                    g2.draw(leftTriangle);
                    if (sliceWidth > 1) {
                        GeneralPath rightTriangle = makeTriangle(xp[2], yp[2], xp[1], yp[1], shape);
                        drawGPEdges(leftTriangle, rightTriangle, g2, 3, true);
                        g2.setColor(fillColor);
                        g2.fill(rightTriangle);
                        g2.setColor(outlineColor);
                        g2.draw(rightTriangle);
                    }
                }
                break;
            case RiemannSumRects.POLAR:
                double radius = yp[2];
                double theta1 = xp[0] * 180 / Math.PI;
                if (radius < 0) {
                    radius *= -1;
                    theta1 += 180;
                }
                float originX = coords.xToPixelF(0);
                float originY = coords.yToPixelF(0);
                float radiusX = coords.xToPixelF(radius) - originX;
                float radiusY = originY - coords.yToPixelF(radius);
                Arc2D arc = new Arc2D.Float();
                arc.setArc(originX - radiusX, originY - radiusY, radiusX * 2, radiusY * 2, theta1, sliceWidth, Arc2D.PIE);
                g2.setColor(fillColor);
                g2.fill(arc);
                g2.setColor(outlineColor);
                g2.draw(arc);
                break;
            default:
                break;
        }
        g2.setStroke(oldStroke);
        g2.setRenderingHints(oldHints);
        g2.setColor(saveColor);
    }

    private Arc2D makeInnerArc(double x, double yBot, double yTop, double axisVal, int shape) {
        double halfHeight = Math.min(Math.abs(yBot - axisVal), Math.abs(yTop - axisVal));
        if (halfHeight == 0) {
            return null;
        } else if (yBot - axisVal > 0 && yTop - axisVal < 0 || yBot - axisVal < 0 && yTop - axisVal > 0) {
            return null;
        } else {
            double y = axisVal + halfHeight;
            float xPixel = xToPixel(x);
            float yPixel = yToPixel(y);
            float height = Math.abs(yPixel - yToPixel(y - 2 * halfHeight));
            float width = height * aspect;
            if (!inverse) {
                return new Arc2D.Double(xPixel - width / 2, yPixel, width, height, (shape == RiemannSumRects.WASHER_FRONT ? 90 : 270), 180, Arc2D.OPEN);
            } else {
                return new Arc2D.Double(yPixel - height, xPixel - width / 2, height, width, (shape == RiemannSumRects.WASHER_FRONT ? 180 : 0), 180, Arc2D.OPEN);
            }
        }
    }

    private Arc2D makeOuterArc(double x, double yBot, double yTop, double axisVal, int shape) {
        double halfHeight = Math.max(Math.abs(yBot - axisVal), Math.abs(yTop - axisVal));
        double y = axisVal + halfHeight;
        float xPixel = xToPixel(x);
        float yPixel = yToPixel(y);
        float height = Math.abs(yPixel - yToPixel(y - 2 * halfHeight));
        float width = height * aspect;
        if (!inverse) {
            return new Arc2D.Double(xPixel - width / 2, yPixel, width, height, (shape == RiemannSumRects.WASHER_FRONT ? 90 : 270), 180, Arc2D.OPEN);
        } else {
            return new Arc2D.Double(yPixel - height, xPixel - width / 2, height, width, (shape == RiemannSumRects.WASHER_FRONT ? 180 : 0), 180, Arc2D.OPEN);
        }
    }

    private void drawEdge(Arc2D left, Arc2D right, Graphics2D g2) {
        if (left == null || right == null || g2 == null) {
            return;
        }
        Area area = new Area(left);
        GeneralPath gp = new GeneralPath();
        Line2D line = new Line2D.Double();
        if (!inverse) {
            gp.moveTo((float) (left.getX() + left.getWidth() / 2), (float) left.getY());
            gp.lineTo((float) (left.getX() + left.getWidth() / 2), (float) (left.getY() + left.getHeight()));
            gp.lineTo((float) (right.getX() + right.getWidth() / 2), (float) (right.getY() + right.getHeight()));
            gp.lineTo((float) (right.getX() + right.getWidth() / 2), (float) (right.getY()));
            gp.closePath();
            area.add(new Area(gp));
        } else {
            gp.moveTo((float) (left.getX()), (float) (left.getY() + left.getHeight() / 2));
            gp.lineTo((float) (left.getX() + left.getWidth()), (float) (left.getY() + left.getHeight() / 2));
            gp.lineTo((float) (right.getX() + right.getWidth()), (float) (right.getY() + right.getHeight() / 2));
            gp.lineTo((float) (right.getX()), (float) (right.getY() + right.getHeight() / 2));
            gp.closePath();
            area.add(new Area(gp));
        }
        area.subtract(new Area(right));
        g2.setColor(fillColor);
        g2.fill(area);
        g2.setColor(outlineColor);
        if (!inverse) {
            line.setLine(left.getX() + left.getWidth() / 2, left.getY(), right.getX() + right.getWidth() / 2, right.getY());
            g2.draw(line);
            line.setLine(left.getX() + left.getWidth() / 2, left.getY() + left.getHeight(), right.getX() + right.getWidth() / 2, right.getY() + right.getHeight());
            g2.draw(line);
        } else {
            line.setLine(left.getX(), left.getY() + left.getHeight() / 2, right.getX(), right.getY() + right.getHeight() / 2);
            g2.draw(line);
            line.setLine(left.getX() + left.getWidth(), left.getY() + left.getHeight() / 2, right.getX() + right.getWidth(), right.getY() + right.getHeight() / 2);
            g2.draw(line);
        }
    }

    private Arc2D makeSemiCircle(double x, double yBot, double yTop, int shape) {
        float xPixel = xToPixel(x);
        float yPixelTop = yToPixel(yTop);
        float yPixelBottom = yToPixel(yBot);
        float height = Math.abs(yPixelTop - yPixelBottom);
        float width = height * aspect;
        float y = Math.min(yPixelBottom, yPixelTop);
        if (!inverse) {
            switch(shape) {
                case RiemannSumRects.SEMICIRCLE:
                    return new Arc2D.Double(xPixel - width / 2, y, width, height, 90, 180, Arc2D.OPEN);
                case RiemannSumRects.CIRCLE_FRONT:
                    return new Arc2D.Double(xPixel - width / 2, y, width, height, 90, 180, Arc2D.OPEN);
                case RiemannSumRects.CIRCLE_REAR:
                    return new Arc2D.Double(xPixel - width / 2, y, width, height, 270, 180, Arc2D.OPEN);
                default:
                    return null;
            }
        } else {
            switch(shape) {
                case RiemannSumRects.SEMICIRCLE:
                    return new Arc2D.Double(y, xPixel - width / 2, height, width, 180, 180, Arc2D.OPEN);
                case RiemannSumRects.CIRCLE_FRONT:
                    return new Arc2D.Double(y, xPixel - width / 2, height, width, 180, 180, Arc2D.OPEN);
                case RiemannSumRects.CIRCLE_REAR:
                    return new Arc2D.Double(y, xPixel - width / 2, height, width, 0, 180, Arc2D.OPEN);
                default:
                    return null;
            }
        }
    }

    private Line2D makeLine(double x1, double y1, double x2, double y2) {
        float x1F = xToPixel(x1);
        float y1F = yToPixel(y1);
        float x2F = xToPixel(x2);
        float y2F = yToPixel(y2);
        if (!inverse) {
            return (new Line2D.Float(x1F, y1F, x2F, y2F));
        } else {
            return (new Line2D.Float(y1F, x1F, y2F, x2F));
        }
    }

    private GeneralPath makeSquare(float xBot, float yBot, float yTop) {
        float y = Math.min(yBot, yTop);
        float height = Math.abs(yTop - yBot);
        float width = height * aspect;
        float slantPixels = height * slant;
        GeneralPath gp = new GeneralPath();
        if (!inverse) {
            gp.moveTo(xBot, y);
            gp.lineTo(xBot, y + height);
            gp.lineTo(xBot - width, y + height - slantPixels);
            gp.lineTo(xBot - width, y - slantPixels);
            gp.closePath();
        } else {
            gp.moveTo(y, xBot);
            gp.lineTo(y + height, xBot);
            gp.lineTo(y + height - slantPixels, xBot + width);
            gp.lineTo(y - slantPixels, xBot + width);
            gp.closePath();
        }
        return gp;
    }

    private GeneralPath makeTriangle(double x1, double y1, double x2, double y2, int shape) {
        float xPixel = xToPixel(x1);
        float yPixelTop = yToPixel(y1);
        float yPixelBottom = yToPixel(y2);
        float height = Math.abs(yPixelTop - yPixelBottom);
        float xMid, yMid;
        switch(shape) {
            case RiemannSumRects.SQUARE_DIAG_FRONT:
                if (!inverse) {
                    xMid = xPixel - height / 2;
                } else {
                    xMid = xPixel + height / 2;
                }
                yMid = (yPixelBottom + yPixelTop) / 2;
                break;
            case RiemannSumRects.SQUARE_DIAG_REAR:
                if (!inverse) {
                    xMid = xPixel + height / 2;
                } else {
                    xMid = xPixel - height / 2;
                }
                yMid = (yPixelBottom + yPixelTop) / 2;
                break;
            case RiemannSumRects.EQUILATERAL:
                if (!inverse) {
                    xMid = (float) (xPixel - height * Math.sqrt(3) / 2);
                } else {
                    xMid = (float) (xPixel + height * Math.sqrt(3) / 2);
                }
                yMid = (yPixelBottom + yPixelTop) / 2;
                break;
            case RiemannSumRects.ISOSCELES_HYP:
                if (!inverse) {
                    xMid = xPixel - height / 2;
                } else {
                    xMid = xPixel + height / 2;
                }
                yMid = (yPixelBottom + yPixelTop) / 2;
                break;
            case RiemannSumRects.ISOSCELES_LEG:
                if (!inverse) {
                    xMid = xPixel - height;
                } else {
                    xMid = xPixel + height;
                }
                yMid = yPixelBottom;
                break;
            default:
                return null;
        }
        float width = Math.abs(xPixel - xMid);
        GeneralPath gp = new GeneralPath();
        if (!inverse) {
            gp.moveTo(xPixel, yPixelTop);
            gp.lineTo(xPixel, yPixelBottom);
            if (shape != RiemannSumRects.SQUARE_DIAG_REAR) {
                gp.lineTo(xMid + (1 - aspect) * width, yMid - height * slant);
            } else {
                gp.lineTo(xMid - (1 - aspect) * width, yMid + height * slant);
            }
            gp.closePath();
        } else {
            gp.moveTo(yPixelTop, xPixel);
            gp.lineTo(yPixelBottom, xPixel);
            if (shape != RiemannSumRects.SQUARE_DIAG_REAR) {
                gp.lineTo(yMid - height * slant, xMid - (1 - aspect) * width);
            } else {
                gp.lineTo(yMid + height * slant, xMid + (1 - aspect) * width);
            }
            gp.closePath();
        }
        return gp;
    }

    private void drawGPEdges(GeneralPath left, GeneralPath right, Graphics2D g2, int numSides, boolean drawFirstEdge) {
        PathIterator leftPI = left.getPathIterator(null);
        PathIterator rightPI = right.getPathIterator(null);
        if (leftPI.isDone() || rightPI.isDone()) {
            return;
        }
        Point2D[] leftPoints = new Point2D.Double[numSides];
        Point2D[] rightPoints = new Point2D.Double[numSides];
        float[] p = new float[6];
        for (int i = 0; i < numSides; i++) {
            leftPoints[i] = new Point2D.Double();
            rightPoints[i] = new Point2D.Double();
            leftPI.currentSegment(p);
            leftPoints[i].setLocation(p[0], p[1]);
            leftPI.next();
            rightPI.currentSegment(p);
            rightPoints[i].setLocation(p[0], p[1]);
            rightPI.next();
        }
        GeneralPath edge = new GeneralPath();
        int[] order = new int[numSides];
        if (numSides == 4) {
            order[0] = 0;
            order[1] = 1;
            order[2] = 3;
            order[3] = 2;
        } else if (numSides == 3) {
            order[0] = 0;
            order[1] = 1;
            order[2] = 2;
            double s = -(rightPoints[2].getY() - leftPoints[2].getY()) / (rightPoints[2].getX() - leftPoints[2].getX());
            double sre = -(rightPoints[0].getY() - rightPoints[2].getY()) / (rightPoints[0].getX() - rightPoints[2].getX());
            if (inverse) {
                s = 1 / s;
                sre = 1 / sre;
            }
            if (!inverse && (rightPoints[0].getY() > rightPoints[1].getY()) || inverse && (rightPoints[0].getX() < rightPoints[1].getX())) {
                s = -s;
                sre = -sre;
            }
            boolean sBigger = s > sre;
            boolean sPos = s > 0;
            boolean srePos = sre > 0;
            boolean sToLeft = !inverse ? (leftPoints[2].getX() < rightPoints[2].getX()) : (leftPoints[2].getY() > rightPoints[2].getY());
            boolean flip = false;
            if (srePos) {
                if (sToLeft && sBigger && sPos || !sToLeft && !sBigger) {
                    flip = true;
                }
            } else {
                if (!sToLeft && !sPos && !sBigger || sToLeft && sBigger) {
                    flip = true;
                }
            }
            if (flip) {
                order[1] = 2;
                order[2] = 1;
            }
        }
        for (int i = 0; i < order.length; i++) {
            int firstIndex = order[i];
            int secondIndex = order[i] + 1;
            if (secondIndex == numSides) {
                secondIndex = 0;
            }
            if (firstIndex != 0 || drawFirstEdge) {
                edge.reset();
                edge.moveTo((float) rightPoints[firstIndex].getX(), (float) rightPoints[firstIndex].getY());
                edge.lineTo((float) rightPoints[secondIndex].getX(), (float) rightPoints[secondIndex].getY());
                edge.lineTo((float) leftPoints[secondIndex].getX(), (float) leftPoints[secondIndex].getY());
                edge.lineTo((float) leftPoints[firstIndex].getX(), (float) leftPoints[firstIndex].getY());
                edge.closePath();
                g2.setColor(fillColor);
                g2.fill(edge);
                g2.setColor(outlineColor);
                g2.draw(edge);
            }
        }
    }

    private void drawTriangleSides(GeneralPath t, Graphics2D g2) {
        PathIterator pi = t.getPathIterator(null);
        float[] topPoint = new float[6];
        float[] botPoint = new float[6];
        float[] midPoint = new float[6];
        Line2D line = new Line2D.Float();
        pi.currentSegment(topPoint);
        pi.next();
        pi.currentSegment(botPoint);
        pi.next();
        pi.currentSegment(midPoint);
        g2.setColor(outlineColor);
        line.setLine(botPoint[0], botPoint[1], midPoint[0], midPoint[1]);
        g2.draw(line);
        line.setLine(midPoint[0], midPoint[1], topPoint[0], topPoint[1]);
        g2.draw(line);
    }

    private float xToPixel(double coord) {
        if (!inverse) {
            return coords.xToPixelF(coord);
        } else {
            return coords.yToPixelF(coord);
        }
    }

    private float yToPixel(double coord) {
        if (!inverse) {
            return coords.yToPixelF(coord);
        } else {
            return coords.xToPixelF(coord);
        }
    }

    private double pixelToX(float p) {
        if (!inverse) {
            return coords.pixelToX(p);
        } else {
            return coords.pixelToY(p);
        }
    }

    private double searchMin(Function func, Function deriv, double x1, double x2, int depth) {
        double mid = (x1 + x2) / 2;
        param[0] = mid;
        if (depth >= 13) return func.getVal(param);
        double slope = deriv.getVal(param);
        if (slope < 0) return searchMin(func, deriv, mid, x2, depth + 1); else return searchMin(func, deriv, x1, mid, depth + 1);
    }

    private double searchMax(Function func, Function deriv, double x1, double x2, int depth) {
        double mid = (x1 + x2) / 2;
        param[0] = mid;
        if (depth >= 13) return func.getVal(param);
        double slope = deriv.getVal(param);
        if (slope > 0) return searchMax(func, deriv, mid, x2, depth + 1); else return searchMax(func, deriv, x1, mid, depth + 1);
    }
}
