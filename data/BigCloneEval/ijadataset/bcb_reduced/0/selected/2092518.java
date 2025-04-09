package net.sourceforge.webcompmath.draw;

import net.sourceforge.webcompmath.data.Value;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;
import net.sourceforge.webcompmath.awt.*;

/**
 * A CoordinateRect represents a rectagular region in the xy-plane, specified by
 * values xmin,xmax,ymin,ymax. The conditions ymin < ymax and xmin < xmax are
 * enforced. (Values are swapped if necessary, and if min==max, they are reset
 * to -1 and +1. If any of the values are set to an infinite or NaN value, then
 * the coordinate rect won't display anything except the message "Error:
 * undefined limits".)
 * <P>
 * When the Rect is mapped onto the screen, there can be a gap of a specified
 * number of pixels between the min,max values and the edges of the rectangle on
 * the screen. If the gap is non-zero, then the actual range of coordinates on
 * the rect is larger than the range from the specifed min to max. (This is done
 * mainly so I could have axes that don't quite reach the edges of the rect.)
 * <P>
 * A CoordinateRect maintains a list of Drawable items. When the Rect's draw()
 * method is called, it calls the draw() method of each of the Drawable items it
 * contains. When its compute() method is called, it calls the compute() method
 * of any Drawable that is a Computable. When its checkInput() method is called,
 * it calls the checkInput() method of any Drawable that is an InputObject.
 * <P>
 * A CoordinateRect represents a rectangular region in a DisplayCanvas. It has a
 * reference to that Canvas, which is set automatically when it is added to the
 * canvas. If the size, range, or gap on the CoordinateRect change, it will ask
 * the Canvas to redraw the area it occupies.
 * 
 * <P>
 * The values of xmin, xmax, ymin, ymax are exported as Value objects, which can
 * be used elsewhere in your program. The Value objects can be obtained by
 * calling getValueObject(). If you do this, you should add the objects that
 * depend on those values to a Controller and register the Controller to listen
 * for changes from this CoordinateRect by calling the
 * CoordinateRect.setOnChange(Controller) method.
 */
public class CoordinateRect implements Tieable, Limits, Computable, InputObject {

    private static final long serialVersionUID = 4419019162918666007L;

    private double xmin, xmax, ymin, ymax;

    private int gap = 5;

    /**
	 * Drawable items contained in this CoordinateRect
	 */
    protected Vector drawItems = new Vector();

    /**
	 * Set to true when one of the limits or the gap has changed.
	 */
    protected boolean changed;

    private long serialNumber;

    /**
	 * This contains other Limit objects with which the CoordinateRect is
	 * synchronizing. This is ordinarily managed by a LimitControlPanel, so you
	 * don't have to worry about it. (However, you can also sync several
	 * CoordinateRects even in the absense of a LimitControlPanel. To do so,
	 * create the Tie that ties the CoordinateRect and pass it to the
	 * setSyncWith() method of each CoordinateRect. It is NOT necessary to add
	 * the Tie to a Controller. Synchronization is handled by the
	 * CoordinateRects themselves.
	 */
    protected Tie syncWith;

    private boolean syncX = true, syncY = true;

    /**
	 * Create a CoordinateRect with default limits: -5, 5, -5, 5.
	 */
    public CoordinateRect() {
        this(-5, 5, -5, 5);
    }

    /**
	 * Create a CoordinateRect with specified limits.
	 * 
	 * @param xmin
	 *            minimum x coord
	 * @param xmax
	 *            mzximum x coord
	 * @param ymin
	 *            minimum y coord
	 * @param ymax
	 *            maximum y coord
	 */
    public CoordinateRect(double xmin, double xmax, double ymin, double ymax) {
        setLimits(xmin, xmax, ymin, ymax);
        serialNumber = 0;
        setRestoreBuffer();
    }

    /**
	 * Get the mimimum x-coordinate.
	 * 
	 * @return min x coord
	 */
    public double getXmin() {
        return xmin;
    }

    /**
	 * Get the maximum x-coordinate.
	 * 
	 * @return max x coord
	 */
    public double getXmax() {
        return xmax;
    }

    /**
	 * Get the mimimum y-coordinate.
	 * 
	 * @return min y coord
	 */
    public double getYmin() {
        return ymin;
    }

    /**
	 * Get the maximum x-coordinate.
	 * 
	 * @return max y coord
	 */
    public double getYmax() {
        return ymax;
    }

    /**
	 * Get the gap, in pixels, between the edges of the CoordinateRect and the
	 * limits specified by xmin, xmax, ymin, and ymax.
	 * 
	 * @return gap in pixels
	 */
    public int getGap() {
        return gap;
    }

    /**
	 * Set the gap. This is ignored if g is less than zero. This gap is the
	 * number of pixels between the edges of the CoordinateRect and the limits
	 * specified by xmin, xmax, ymin, and ymax. The default value is 5.
	 * 
	 * @param g
	 *            gap in pixels
	 */
    public void setGap(int g) {
        if (g >= 0 && gap != g) {
            gap = g;
            changed = true;
            serialNumber++;
            needsRedraw();
        }
    }

    /**
	 * Get an array containing the limits on the CoordinateRect in the order
	 * xmin, xmax, ymin, ymax.
	 * 
	 * @return limits of CoordinateRect
	 */
    public double[] getLimits() {
        return new double[] { xmin, xmax, ymin, ymax };
    }

    /**
	 * Set the limits on the CoordinteRect
	 * 
	 * @param xmin
	 *            the minimum x-coordinate on the CoordinateRect
	 * @param xmax
	 *            the maximum x-coordinate on the CoordinateRect
	 * @param ymin
	 *            the minimum y-coordinate on the CoordinateRect
	 * @param ymax
	 *            the maximum y-coordinate on the CoordinateRect
	 */
    public void setLimits(double xmin, double xmax, double ymin, double ymax) {
        double[] oldLimits = getLimits();
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        checkLimits();
        double[] newLimits = getLimits();
        if (oldLimits[0] == newLimits[0] && oldLimits[1] == newLimits[1] && oldLimits[2] == newLimits[2] && oldLimits[3] == newLimits[3]) return;
        changed = true;
        serialNumber++;
        if (syncWith != null) syncWith.check();
        if (onChange != null) onChange.compute();
        needsRedraw();
    }

    /**
	 * Set the limits on the CoordinteRect, but don't call compute. Intended for
	 * use when initializing a javabean displaycanvas in a gui builder.
	 * 
	 * @param xmin
	 *            the minimum x-coordinate on the CoordinateRect
	 * @param xmax
	 *            the maximum x-coordinate on the CoordinateRect
	 * @param ymin
	 *            the minimum y-coordinate on the CoordinateRect
	 * @param ymax
	 *            the maximum y-coordinate on the CoordinateRect
	 */
    public void setLimitsLazy(double xmin, double xmax, double ymin, double ymax) {
        double[] oldLimits = getLimits();
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        checkLimits();
        double[] newLimits = getLimits();
        if (oldLimits[0] == newLimits[0] && oldLimits[1] == newLimits[1] && oldLimits[2] == newLimits[2] && oldLimits[3] == newLimits[3]) return;
        changed = true;
        serialNumber++;
    }

    /**
	 * Set the coordinate limits from array; extra elements in array are
	 * ignored. This is ignored if the array is null or has fewer than 4
	 * members. The order of values in the array is xmin, xmax, ymin, ymax.
	 * 
	 * @param d
	 *            limits of CoordinateRect
	 */
    public void setLimits(double[] d) {
        if (d != null && d.length >= 4) setLimits(d[0], d[1], d[2], d[3]);
    }

    /**
	 * Set the coordinate limits, and also set the restore buffer.
	 * 
	 * @param d
	 *            the limits to set
	 */
    public void setLimitsAndRestoreBuffer(double[] d) {
        setLimits(d);
        setRestoreBuffer(d);
    }

    /**
	 * Specify a controller to be notified when the limits on this
	 * CoordinateRect change.
	 * 
	 * @param c
	 *            controller to use
	 */
    public void setOnChange(Controller c) {
        onChange = c;
    }

    /**
	 * Get the controller that is notified when the limits on this
	 * CoordinateRect change. This can be null.
	 * 
	 * @return controller
	 */
    public Controller getOnChange() {
        return onChange;
    }

    /**
	 * Get a Value object representing one of the limits on this CoordinateRect.
	 * The parameter should be one of the constants CoordinateRect.XMIN,
	 * CoordinateRect.XMAX, CoordinateRect.YMIN, or CoordinateRect.YMAX. (If
	 * not, it is treated the same as YMAX).
	 * 
	 * @param which
	 *            specifies one of XMIN, XMAX, YMIN, YMAX
	 * @return value of the limit
	 */
    public Value getValueObject(final int which) {
        return new Value() {

            private static final long serialVersionUID = 4501905089082145218L;

            public double getVal() {
                switch(which) {
                    case XMIN:
                        return getXmin();
                    case XMAX:
                        return getXmax();
                    case YMIN:
                        return getYmin();
                    default:
                        return getYmax();
                }
            }
        };
    }

    /**
	 * Return the serial number of the CoordinateRect, which is incremented each
	 * time the limits change. Part of the Tieable interface. Not meant to be
	 * called directly.
	 * 
	 * @return serial number
	 */
    public long getSerialNumber() {
        return serialNumber;
    }

    /**
	 * Set the Tie object that is used to synchronize this CoordinareRect with
	 * other objects. This is ordinarily called by a LimitControlPanel, so you
	 * don't have to worry about it.
	 * 
	 * @param tie
	 *            the tie for synchronization
	 */
    public void setSyncWith(Tie tie) {
        syncWith = tie;
    }

    /**
	 * Part of the Tieable interface. Not meant to be called directly.
	 * 
	 * @param tie
	 *            the tie to use
	 * @param newest
	 *            the object to synch with
	 */
    public void sync(Tie tie, Tieable newest) {
        if (newest != this) {
            if (!(newest instanceof Limits)) throw new IllegalArgumentException("Internal programming error:  A CoordinateRect can only be tied to a Limits object.");
            double[] d = ((Limits) newest).getLimits();
            if (d != null && d.length >= 4) {
                double[] oldLimits = getLimits();
                if (d[0] == oldLimits[0] && d[1] == oldLimits[1] && d[2] == oldLimits[2] && d[3] == oldLimits[3]) return;
                if (syncX) {
                    xmin = d[0];
                    xmax = d[1];
                }
                if (syncY) {
                    ymin = d[2];
                    ymax = d[3];
                }
                checkLimits();
                serialNumber = newest.getSerialNumber();
                changed = true;
                if (onChange != null) onChange.compute();
                needsRedraw();
            }
        }
    }

    private void checkLimits() {
        if (xmin == xmax) {
            xmin -= 1;
            xmax += 1;
        } else if (xmin > xmax) {
            double temp = xmin;
            xmin = xmax;
            xmax = temp;
        }
        if (ymin == ymax) {
            ymin -= 1;
            ymax += 1;
        }
        if (ymin > ymax) {
            double temp = ymin;
            ymin = ymax;
            ymax = temp;
        }
    }

    /**
	 * A constant for use with the getValueObject() method to specify which
	 * Value is to be returned. XMIN specifies that the Value is the minimum
	 * x-coordinate on the CoordinateRect.
	 */
    public static final int XMIN = 0;

    /**
	 * A constant for use with the getValueObject() method to specify which
	 * Value is to be returned. XMAX specifies that the Value is the maximum
	 * x-coordinate on the CoordinateRect.
	 */
    public static final int XMAX = 1;

    /**
	 * A constant for use with the getValueObject() method to specify which
	 * Value is to be returned. YMIN specifies that the Value is the minimum
	 * y-coordinate on the CoordinateRect.
	 */
    public static final int YMIN = 2;

    /**
	 * A constant for use with the getValueObject() method to specify which
	 * Value is to be returned. YMAX specifies that the Value is the maximum
	 * y-coordinate on the CoordinateRect.
	 */
    public static final int YMAX = 3;

    /**
	 * If non-null, this is the Controller that is notified when the limits
	 * change.
	 */
    protected Controller onChange;

    private int left, top, width = -1, height = -1;

    /**
	 * Get the left edge of this CoordinateRect in the DisplayCanvas that
	 * contains it. (This is only valid when the CoordinateRect has actually
	 * been displayed. It is meant mainly to be used by Drawables in this
	 * CoordinateRect.)
	 * 
	 * @return left edge
	 */
    public int getLeft() {
        return left;
    }

    /**
	 * Get the width in pixels of this CoordinateRect in the DisplayCanvas that
	 * contains it. (This is only valid when the CoordinateRect has actually
	 * been displayed. It is meant mainly to be used by Drawables in this
	 * CoordinateRect.)
	 * 
	 * @return width in pixels
	 */
    public int getWidth() {
        return width;
    }

    /**
	 * Get the top edge of this CoordinateRect in the DisplayCanvas that
	 * contains it. (This is only valid when the CoordinateRect has actually
	 * been displayed. It is meant mainly to be used by Drawables in this
	 * CoordinateRect.)
	 * 
	 * @return top edge
	 */
    public int getTop() {
        return top;
    }

    /**
	 * Get the height in pixels of this CoordinateRect in the DisplayCanvas that
	 * contains it. (This is only valid when the CoordinateRect has actually
	 * been displayed. It is meant mainly to be used by Drawables in this
	 * CoordinateRect.)
	 * 
	 * @return height in pixels
	 */
    public int getHeight() {
        return height;
    }

    /**
	 * Return the width of one pixel in this coordinate system. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @return width of one pixel
	 */
    public double getPixelWidth() {
        return (xmax - xmin) / (width - 2 * gap - 1);
    }

    /**
	 * Return the width of one pixel in the specified coordinate system. (It is
	 * meant mainly to be used by Drawables doing setup on the WcmWorker thread)
	 * 
	 * @param xmin
	 * @param xmax
	 * @param width
	 * @param gap
	 * 
	 * @return width of one pixel
	 */
    public static double getPixelWidth(double xmin, double xmax, int width, int gap) {
        return (xmax - xmin) / (width - 2 * gap - 1);
    }

    /**
	 * Return the height of one pixel in this coordinate system. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @return height of one pixel
	 */
    public double getPixelHeight() {
        return (ymax - ymin) / (height - 2 * gap - 1);
    }

    /**
	 * Return the height of one pixel in the specified coordinate system. (It is
	 * meant mainly to be used by Drawables doing setup on the WcmWorker thread)
	 * 
	 * @param ymin
	 * @param ymax
	 * @param height
	 * @param gap
	 * 
	 * @return height of one pixel
	 */
    public static double getPixelHeight(double ymin, double ymax, int height, int gap) {
        return (ymax - ymin) / (height - 2 * gap - 1);
    }

    /**
	 * Convert an x-coodinate into a horizontal pixel coordinate. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param x
	 *            x coord to convert
	 * @return pixel coord
	 */
    public int xToPixel(double x) {
        return Math.round(xToPixelF(x));
    }

    /**
	 * Convert an x-coodinate into a horizontal pixel coordinate. This version
	 * returns a float, for use with the newer Java 2D graphics. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param x
	 *            x coord to convert
	 * @return pixel coord
	 */
    public float xToPixelF(double x) {
        float xInt = (float) (left + gap + ((x - xmin) / (xmax - xmin) * (width - 2 * gap - 1)));
        if (xInt < -32000) return -32000; else if (xInt > 32000) return 32000; else return xInt;
    }

    /**
	 * Convert an x-coodinate into a horizontal pixel coordinate. This version
	 * returns a float, for use with the newer Java 2D graphics. (It is meant
	 * mainly to be used by Drawables doing setup on the WcmWorker thread)
	 * 
	 * @param x
	 *            x coord to convert
	 * @param xmin
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 * @param left
	 * @param width
	 * @param gap
	 * @return pixel coord
	 */
    public static float xToPixelF(double x, double xmin, double xmax, double ymin, double ymax, int left, int width, int gap) {
        float xInt = (float) (left + gap + ((x - xmin) / (xmax - xmin) * (width - 2 * gap - 1)));
        if (xInt < -32000) return -32000; else if (xInt > 32000) return 32000; else return xInt;
    }

    /**
	 * Convert a y-coodinate into a vertical pixel coordinate. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param y
	 *            y coord to conver
	 * @return pixel coord
	 */
    public int yToPixel(double y) {
        return Math.round(yToPixelF(y));
    }

    /**
	 * Convert a y-coodinate into a vertical pixel coordinate. This version
	 * returns a float, for use with the newer Java 2D graphics. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param y
	 *            y coord to conver
	 * @return pixel coord
	 */
    public float yToPixelF(double y) {
        float yInt = (float) (top + gap + ((ymax - y) / (ymax - ymin) * (height - 2 * gap - 1)));
        if (yInt < -32000) return -32000; else if (yInt > 32000) return 32000; else return yInt;
    }

    /**
	 * Convert a y-coodinate into a vertical pixel coordinate. This version
	 * returns a float, for use with the newer Java 2D graphics. (It is meant
	 * mainly to be used by Drawables doing setup on the WcmWorker thread)
	 * 
	 * @param y
	 *            y coord to conver
	 * @param ymin
	 * @param ymax
	 * @param top
	 * @param height
	 * @param gap
	 * @return pixel coord
	 */
    public static float yToPixelF(double y, double ymin, double ymax, int top, int height, int gap) {
        float yInt = (float) (top + gap + ((ymax - y) / (ymax - ymin) * (height - 2 * gap - 1)));
        if (yInt < -32000) return -32000; else if (yInt > 32000) return 32000; else return yInt;
    }

    /**
	 * Convert a horizontal pixel coordinate into an x-coordinate. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param h
	 *            pixel coord to convert
	 * @return x coord
	 */
    public double pixelToX(int h) {
        return xmin + ((h - left - gap) * (xmax - xmin)) / (width - 2 * gap - 1);
    }

    /**
	 * Convert a horizontal pixel coordinate into an x-coordinate. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param h
	 *            pixel coord to convert
	 * @return x coord
	 */
    public double pixelToX(float h) {
        return xmin + ((h - left - gap) * (xmax - xmin)) / (width - 2 * gap - 1);
    }

    /**
	 * Convert a horizontal pixel coordinate into an x-coordinate. (It is meant
	 * mainly to be used by Drawables doing setup on the WcmWorker thread)
	 * 
	 * @param h
	 *            pixel coord to convert
	 * @param xmin
	 * @param xmax
	 * @param left
	 * @param width
	 * @param gap
	 * @return x coord
	 */
    public static double pixelToX(float h, double xmin, double xmax, int left, int width, int gap) {
        return xmin + ((h - left - gap) * (xmax - xmin)) / (width - 2 * gap - 1);
    }

    /**
	 * Convert a vertical pixel coordinate into a y-coordinate. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param y
	 *            pixel coord to convert
	 * @return y coord
	 */
    public double pixelToY(int y) {
        return ymax - ((y - top - gap) * (ymax - ymin)) / (height - 2 * gap - 1);
    }

    /**
	 * Convert a vertical pixel coordinate into a y-coordinate. (This is only
	 * valid when the CoordinateRect has actually been displayed. It is meant
	 * mainly to be used by Drawables in this CoordinateRect.)
	 * 
	 * @param y
	 *            pixel coord to convert
	 * @return y coord
	 */
    public double pixelToY(float y) {
        return ymax - ((y - top - gap) * (ymax - ymin)) / (height - 2 * gap - 1);
    }

    /**
	 * Convert a vertical pixel coordinate into a y-coordinate. (It is meant
	 * mainly to be used by Drawables doing setup on the WcmWorker thread)
	 * 
	 * @param y
	 *            pixel coord to convert
	 * @param ymin
	 * @param ymax
	 * @param top
	 * @param height
	 * @param gap
	 * @return y coord
	 */
    public static double pixelToY(float y, double ymin, double ymax, int top, int height, int gap) {
        return ymax - ((y - top - gap) * (ymax - ymin)) / (height - 2 * gap - 1);
    }

    private double restore_xmin = Double.NaN, restore_xmax, restore_ymin, restore_ymax;

    /**
	 * A CoordinateRect can store its current limits in a buffer. These limits
	 * can be restored by a call to this method. Only one level of save/restore
	 * is provided. If limits have not been saved, then nothing happens. The
	 * original limits on the CoordinateRect are saves automatically when the
	 * CoordinateRect is first created.
	 * 
	 * @return an array containing new limits.
	 */
    public double[] restore() {
        if (Double.isNaN(restore_xmin)) return null;
        setLimits(restore_xmin, restore_xmax, restore_ymin, restore_ymax);
        return getLimits();
    }

    /**
	 * A CoordinateRect can store its current limits in a buffer. This method
	 * clears that buffer.
	 */
    public void clearRestoreBuffer() {
        restore_xmin = Double.NaN;
    }

    /**
	 * Save current limits in buffer. They can be restored later by a call to
	 * the restore() method. Only one level of save/restore is provided.
	 */
    public void setRestoreBuffer() {
        if (badData()) return;
        checkLimits();
        restore_xmin = xmin;
        restore_xmax = xmax;
        restore_ymin = ymin;
        restore_ymax = ymax;
    }

    /**
	 * Save specific limits in buffer. They can be restored later by a call to
	 * the restore() method. Only one level of save/restore is provided.
	 * 
	 * @param limits
	 *            the limits to set
	 */
    public void setRestoreBuffer(double[] limits) {
        double newXmin = limits[0];
        double newXmax = limits[1];
        double newYmin = limits[2];
        double newYmax = limits[3];
        if (Double.isNaN(newXmin) || Double.isInfinite(newXmin) || Double.isNaN(newYmin) || Double.isInfinite(newYmin) || Double.isNaN(newXmax) || Double.isInfinite(newXmax) || Double.isNaN(newYmax) || Double.isInfinite(newYmax)) return;
        if (newXmin == newXmax) {
            newXmin -= 1;
            newXmax += 1;
        } else if (newXmin > newXmax) {
            double temp = newXmin;
            newXmin = newXmax;
            newXmax = temp;
        }
        if (newYmin == newYmax) {
            newYmin -= 1;
            newYmax += 1;
        }
        if (newYmin > newYmax) {
            double temp = newYmin;
            newYmin = newYmax;
            newYmax = temp;
        }
        restore_xmin = newXmin;
        restore_xmax = newXmax;
        restore_ymin = newYmin;
        restore_ymax = newYmax;
    }

    /**
	 * Get an array containing the limits for the restore buffer in the order
	 * xmin, xmax, ymin, ymax.
	 * 
	 * @return limits of restore buffer
	 */
    public double[] getRestoreBuffer() {
        return new double[] { restore_xmin, restore_xmax, restore_ymin, restore_ymax };
    }

    /**
	 * Used to test if any of the limit data are infinite or NaN.
	 */
    private boolean badData() {
        return Double.isNaN(xmin) || Double.isInfinite(xmin) || Double.isNaN(ymin) || Double.isInfinite(ymin) || Double.isNaN(xmax) || Double.isInfinite(xmax) || Double.isNaN(ymax) || Double.isInfinite(ymax);
    }

    /**
	 * Change limits to zoom in by a factor of 2. A maximal zoom is enforced.
	 * The center of the rectangle does not move.
	 * 
	 * @return an array of the new limits, or null if limits don't change.
	 */
    public double[] zoomIn() {
        if (badData()) return getLimits();
        double halfwidth = (xmax - xmin) / 4.0;
        double halfheight = (ymax - ymin) / 4.0;
        double centerx = (xmin + xmax) / 2.0;
        double centery = (ymin + ymax) / 2.0;
        if (Math.abs(halfheight) < 1e-100 || Math.abs(halfwidth) < 1e-100) return null;
        setLimits(centerx - halfwidth, centerx + halfwidth, centery - halfheight, centery + halfheight);
        return getLimits();
    }

    /**
	 * Change limits to zoom out by a factor of 2. A maximal zoom is enforced.
	 * The center of the rectangle does not move.
	 * 
	 * @return an array of the new limits, or null if limits don't change.
	 */
    public double[] zoomOut() {
        if (badData()) return getLimits();
        double halfwidth = (xmax - xmin);
        double halfheight = (ymax - ymin);
        double centerx = (xmin + xmax) / 2.0;
        double centery = (ymin + ymax) / 2.0;
        if (Math.abs(halfwidth) > 1e100 || Math.abs(halfheight) > 1e100) return null;
        setLimits(centerx - halfwidth, centerx + halfwidth, centery - halfheight, centery + halfheight);
        return getLimits();
    }

    /**
	 * Change limits to zoom in by a factor of 2, centered on a specified point.
	 * A maximal zoom is enforced. The point does not move. Only valid when
	 * CoordinateRect is displayed in a rectangle on the screen.
	 * 
	 * @param x
	 *            the horizontal pixel coordinate of the center point of the
	 *            zoom
	 * @param y
	 *            the vertical pixel coordinate of the center point of the zoom
	 * 
	 * @return an array of the new limits, or null if limits don't change.
	 */
    public double[] zoomInOnPixel(int x, int y) {
        if (badData()) return getLimits();
        double halfwidth = (xmax - xmin) / 4.0;
        double halfheight = (ymax - ymin) / 4.0;
        if (Math.abs(halfheight) < 1e-100 || Math.abs(halfwidth) < 1e-100) return null;
        double xclick = pixelToX(x);
        double yclick = pixelToY(y);
        double centerx = (xmin + xmax) / 2;
        double centery = (ymin + ymax) / 2;
        double newCenterx = (centerx + xclick) / 2;
        double newCentery = (centery + yclick) / 2;
        setLimits(newCenterx - halfwidth, newCenterx + halfwidth, newCentery - halfheight, newCentery + halfheight);
        return getLimits();
    }

    /**
	 * Change limits to zoom out by a factor of 2, centered on a specified
	 * point. A maximal zoom is enforced. The point (x,y) does not move. Valid
	 * only if CoordinateRect has been drawn.
	 * 
	 * @param x
	 *            the horizontal pixel coordinate of the center point of the
	 *            zoom
	 * @param y
	 *            the vertical pixel coordinate of the center point of the zoom
	 * 
	 * @return an array of the new limits, or null if limits don't change.
	 */
    public double[] zoomOutFromPixel(int x, int y) {
        if (badData()) return getLimits();
        double halfwidth = (xmax - xmin);
        double halfheight = (ymax - ymin);
        if (Math.abs(halfwidth) > 1e100 || Math.abs(halfheight) > 1e100) return null;
        double xclick = pixelToX(x);
        double yclick = pixelToY(y);
        double centerx = (xmin + xmax) / 2;
        double centery = (ymin + ymax) / 2;
        double newCenterx = 2 * centerx - xclick;
        double newCentery = 2 * centery - yclick;
        setLimits(newCenterx - halfwidth, newCenterx + halfwidth, newCentery - halfheight, newCentery + halfheight);
        return getLimits();
    }

    /**
	 * Reset limits, if necessary, so scales on the axes are the same. Only
	 * valid of the CoordinateRect has been drawn.
	 * 
	 * @return an array with the new limits, or null if limits don't change.
	 */
    public double[] equalizeAxes() {
        if (badData()) return getLimits();
        double w = xmax - xmin;
        double h = ymax - ymin;
        double pixelWidth = w / (width - 2 * gap - 1);
        double pixelHeight = h / (height - 2 * gap - 1);
        double newXmin, newXmax, newYmin, newYmax;
        if (pixelWidth < pixelHeight) {
            double centerx = (xmax + xmin) / 2;
            double halfwidth = w / 2 * pixelHeight / pixelWidth;
            newXmax = centerx + halfwidth;
            newXmin = centerx - halfwidth;
            newYmin = ymin;
            newYmax = ymax;
        } else if (pixelWidth > pixelHeight) {
            double centery = (ymax + ymin) / 2;
            double halfheight = h / 2 * pixelWidth / pixelHeight;
            newYmax = centery + halfheight;
            newYmin = centery - halfheight;
            newXmin = xmin;
            newXmax = xmax;
        } else return null;
        setLimits(newXmin, newXmax, newYmin, newYmax);
        return getLimits();
    }

    private DisplayCanvas canvas;

    /**
	 * This is meant to be called only by the DisplayCanvas class, when this
	 * CoordinateRect is added to ta DisplayCanvas.
	 * 
	 */
    void setOwner(DisplayCanvas canvas) {
        this.canvas = canvas;
    }

    private void needsRedraw() {
        if (canvas != null) canvas.doRedraw(this);
    }

    /**
	 * When this is called, the CoordinateRect will call the checkInput method
	 * of any Drawable it contains that is also an InputObject. This is
	 * ordinarly only called by a DisplayCanvas.
	 */
    public void checkInput() {
        int ct = drawItems.size();
        for (int i = 0; i < ct; i++) if (drawItems.elementAt(i) instanceof InputObject) ((InputObject) drawItems.elementAt(i)).checkInput();
    }

    /**
	 * When this is called, the CoordinateRect will call the compute method of
	 * any Drawable it contains that is also a Computable. This is ordinarly
	 * only called by a DisplayCanvas.
	 */
    public void compute() {
        int ct = drawItems.size();
        for (int i = 0; i < ct; i++) if (drawItems.elementAt(i) instanceof Computable) ((Computable) drawItems.elementAt(i)).compute();
    }

    /**
	 * Method required by InputObject interface; in this class, it calls the
	 * same method recursively on any input objects containted in this
	 * CoordinateRect. This is meant to be called by JCMPanel.gatherInputs().
	 * 
	 * @param c
	 *            the controller
	 */
    public void notifyControllerOnChange(Controller c) {
        int ct = drawItems.size();
        for (int i = 0; i < ct; i++) if (drawItems.elementAt(i) instanceof InputObject) ((InputObject) drawItems.elementAt(i)).notifyControllerOnChange(c);
    }

    /**
	 * Add a drawable item to the CoordinateRect.
	 * 
	 * @param d
	 *            item to add
	 */
    @SuppressWarnings("unchecked")
    public synchronized void add(Drawable d) {
        if (d != null && !drawItems.contains(d)) {
            d.setOwnerData(canvas, this);
            drawItems.addElement(d);
        }
    }

    /**
	 * Remove the given Drawable item, if present in this CoordinateRect.
	 * 
	 * @param d
	 *            item to remove
	 */
    public synchronized void remove(Drawable d) {
        if (d != null && drawItems.removeElement(d)) d.setOwnerData(null, null);
    }

    /**
	 * Returns the number of Drawable items that are in this CoordinateRect.
	 * 
	 * @return number of items
	 */
    public int getDrawableCount() {
        return (drawItems == null) ? 0 : drawItems.size();
    }

    /**
	 * Get the i-th Drawable in this Rect, or null if i is less than zero or
	 * greater than or equal to the number of items.
	 * 
	 * @param i
	 *            The number of the item to be returned, where the first item is
	 *            number zero.
	 * @return desired item
	 */
    public Drawable getDrawable(int i) {
        if (drawItems != null && i >= 0 && i < drawItems.size()) return (Drawable) drawItems.elementAt(i); else return null;
    }

    /**
	 * Check whether a mouse click (as specified in the MouseEvent parameter) is
	 * a click on a Draggable item that wants to be dragged. If so, return that
	 * item. If not, return null. This is meant to be called only by
	 * DisplayCanvas.
	 */
    Draggable checkDraggables(java.awt.event.MouseEvent evt) {
        int top = drawItems.size();
        for (int i = top - 1; i >= 0; i--) if (drawItems.elementAt(i) instanceof Draggable) {
            if (((Draggable) drawItems.elementAt(i)).startDrag(evt)) return (Draggable) drawItems.elementAt(i);
        }
        return null;
    }

    /**
	 * Draw in rect with upperleft corner (0,0) and specified width,height. This
	 * is not ordinarily called directly.
	 * 
	 * @param g
	 *            graphics context
	 * @param width
	 *            width to draw in
	 * @param height
	 *            height to draw in
	 */
    public void draw(Graphics g, int width, int height) {
        draw(g, 0, 0, width, height);
    }

    /**
	 * Draw in specified rect. This is not ordinarily called directly.
	 * 
	 * @param g
	 *            graphics context
	 * @param left
	 *            left edge
	 * @param top
	 *            top edge
	 * @param width
	 *            width of rect
	 * @param height
	 *            height of rect
	 */
    public synchronized void draw(Graphics g, int left, int top, int width, int height) {
        if (badData()) {
            g.setColor(Color.red);
            g.drawRect(left, top, width - 1, height - 1);
            g.drawString("(undefined limits)", left + 6, top + 15);
        }
        if (changed || this.left != left || this.top != top || this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            this.left = left;
            this.top = top;
            checkLimits();
            changed = true;
        }
        doDraw(g);
        changed = false;
    }

    /**
	 * Draw all the Drawable items. This is called by the draw() method and is
	 * not meant to be called directly. However, it might be overridden in a
	 * subclass.
	 * 
	 * @param g
	 *            graphics context
	 */
    protected void doDraw(Graphics g) {
        int ct = drawItems.size();
        for (int i = 0; i < ct; i++) {
            Drawable d = (Drawable) drawItems.elementAt(i);
            if (d.getVisible()) d.draw(g, changed);
        }
    }

    /**
	 * Get whether to sync xmin and xmax with limits
	 * 
	 * @return the syncX
	 */
    public boolean isSyncX() {
        return syncX;
    }

    /**
	 * Set whether to sync xmin and xmax with limits
	 * 
	 * @param syncX
	 *            the syncX to set
	 */
    public void setSyncX(boolean syncX) {
        this.syncX = syncX;
    }

    /**
	 * Get whether to sync ymin and ymax with limits
	 * 
	 * @return the syncY
	 */
    public boolean isSyncY() {
        return syncY;
    }

    /**
	 * Get whether to sync ymin and ymax with limits
	 * 
	 * @param syncY
	 *            the syncY to set
	 */
    public void setSyncY(boolean syncY) {
        this.syncY = syncY;
    }
}
