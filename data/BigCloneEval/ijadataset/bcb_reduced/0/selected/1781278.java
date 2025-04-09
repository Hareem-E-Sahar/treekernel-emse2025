package de.ios.framework.gui;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import de.ios.framework.basic.Debug;

/**
 * V(irtual)Graphics.
 * Graphics-Implementation wifht scaling-capabilities for
 * printing or canvas-output.<br>
 * Example 1:
 * <pre>
 * class MyCanvas extends Canvas {
 *   public void paint(Graphics g) {
 *      g.drawRect(0,0,100,100);
 *   }
 * };
 * 
 * class ex1 extends MyCanvas {
 *  ....
 *  public void paint(Graphics g) {
 *     VGraphics vg( g, getSize(), getSize(), g.toolkit().getScreenResolution());
 *     super.paint(vg);
 *  }
 * }
 * </pre>
 * Above examples behaves like a normal graphics. No scaling is used.<br>
 * Example 2:
 * <pre>
 * class ex2 extends MyCanvas {
 *  ....
 *  public void paint(Graphics g) {
 *     Dimension d = getSize();
 *     d.width  *= 2;
 *     d.height *= 2;
 *     VGraphics vg( g, getSize(), d, g.toolkit().getScreenResolution());
 *     super.paint(vg);
 *  }
 * }
 * </pre>
 * The last example use scaling. All output from MyCanvas is display
 * at half size.<br>
 * Don't use Graphics.getClipXXX()-Methods for calculating the dimension
 * for the output-device. On screens this size is often only a part of the
 * drawing-area.
 *
 */
public class VGraphics extends Graphics {

    /**
   * Constructor. Do not use!
   */
    protected VGraphics() {
        super();
    }

    /**
   * Dump information about the actual state to Debug-Channel.
   */
    public void dumpInfo(int channel) {
        Rectangle r;
        FontMetrics fm;
        Debug.println(channel, this, "Scaling " + (hasScaling() ? "ON" : "OFF"));
        if (hasScaling()) {
            Debug.println(channel, this, "X-Scale " + xScale);
            Debug.println(channel, this, "Y-Scale " + yScale);
        }
        Debug.println(channel, this, "Original graphics");
        r = gd.getClipBounds();
        if (r != null) Debug.println(channel, this, "  ClipBounds from (" + r.x + "," + r.y + ") size (" + r.width + "," + r.height + ")"); else Debug.println(channel, this, "  ClipBounds not available.");
        fm = gd.getFontMetrics();
        Debug.println(channel, this, "  Actual FontMetrics height=" + fm.getHeight() + " width 'ABC' =" + fm.stringWidth("ABC"));
    }

    /**
   * Constructor.
   * @param g  Original graphics for output.
   * @param rd Real dimensions of output-device.
   * @param vd Virtual dimension to scale to.
   * @param _dpi Dots per Inch on output device.
   *             (for screen use Toolkit.getScreenResolution()).
   *             Only used for "getDPI()".
   */
    public VGraphics(Graphics g, Dimension rd, Dimension vd, int _dpi) {
        gd = g;
        dpi = _dpi;
        if (rd == null) {
            Rectangle r;
            r = g.getClipBounds();
            rd = new Dimension(r.width, r.height);
        }
        if (vd == null) vd = new Dimension(rd.width, rd.height);
        xScale = ((float) rd.width) / ((float) vd.width);
        yScale = ((float) rd.height) / ((float) vd.height);
        avgScale = (xScale + yScale) / 2;
        scaling = ((xScale != 1.0) || (yScale != 1.0));
        rWidth = rd.width;
        rHeight = rd.height;
        vWidth = vd.width;
        vHeight = vd.height;
    }

    /**
   * Get actual dpi.
   */
    public int getDPI() {
        return dpi;
    }

    /**
   * Activates automatic correction for rectangle-operations.
   * @param mode new mode to set.
   * @return Mode bevor call.
   */
    public boolean setCorrection(boolean mode) {
        boolean oldMode = correction;
        correction = mode;
        return oldMode;
    }

    /**
   * Gets scalingmode.
   * @return true, is VGraphics perform scaling.
   */
    public boolean hasScaling() {
        return scaling;
    }

    /**
   * Mathematical rounding used for scaling-calculations.
   */
    public static final int round(double v) {
        return (int) (v + 0.001);
    }

    /**
   * Scales a point.
   */
    public Point getScaledPoint(int x, int y) {
        return new Point(round(x * xScale), round(y * yScale));
    }

    /**
   * scales a dimension.
   */
    public Dimension getScaledDim(int width, int height) {
        return new Dimension(round(width * xScale), round(height * yScale));
    }

    /**
   * scales a rectangle.
   */
    public Rectangle getScaledRectangle(int x, int y, int width, int height) {
        return new Rectangle(round(x * xScale), round(y * yScale), round(width * xScale), round(height * yScale));
    }

    /**
   * Scales an array of X-coordinates.
   * @param a Original coordinates (is not modified).
   * @param n Number of elements to scale.
   * @return New array with scaled coordinates.
   */
    public int[] getScaledX(int[] a, int n) {
        if (scaling) {
            int r[] = new int[n];
            while (n > 0) {
                n--;
                r[n] = round(xScale * a[n]);
            }
            return r;
        } else return a;
    }

    /**
   * Scales an array of Y-coordinates.
   * @param a Original coordinates (is not modified).
   * @param n Number of elements to scale.
   * @return New array with scaled coordinates.
   */
    public int[] getScaledY(int[] a, int n) {
        if (scaling) {
            int r[] = new int[n];
            while (n > 0) {
                n--;
                r[n] = round(yScale * a[n]);
            }
            return r;
        } else return a;
    }

    /**
   * Creates a copy.
   */
    public Graphics create() {
        return new VGraphics(gd, new Dimension(vWidth, vHeight), new Dimension(rWidth, rHeight), dpi);
    }

    /**
   * Translates the origin of the graphics context to the point (x, y) 
   * in the current coordinate system. All coordinates used in subsequent
   * rendering operations on this graphics context will be relative to 
   * this new origin. 
   */
    public void translate(int x, int y) {
        Point p = getScaledPoint(x, y);
        gd.translate(p.x, p.y);
    }

    /**
   * Get actual color.
   */
    public Color getColor() {
        return gd.getColor();
    }

    /**
   * Set actual color.
   */
    public void setColor(Color c) {
        gd.setColor(c);
    }

    /**
   * Set actual mode to paintmode.
   */
    public void setPaintMode() {
        gd.setPaintMode();
    }

    /**
   * Set actual mode to XOR-mode.
   */
    public void setXORMode(Color c1) {
        gd.setXORMode(c1);
    }

    /**
   * Get actual font.
   */
    public Font getFont() {
        if (actFontIdx >= 0) return (Font) fontCache.elementAt(actFontIdx); else return null;
    }

    /** Original graphics for output. */
    protected Graphics gd = null;

    /** Vector of scaled fonts. */
    protected Vector scaledFontCache = new Vector();

    /** Vector of fonts specified by caller. */
    protected Vector fontCache = new Vector();

    /** Index of actual font. */
    protected int actFontIdx = -1;

    /** Auto-correction-mode for rectangles. */
    protected boolean correction = false;

    /** True, if scaling is active. */
    protected boolean scaling;

    /** Virtual width. */
    protected int vWidth = 0;

    /** Virtual heigth. */
    protected int vHeight = 0;

    /** Width of original output-graphics. */
    protected int rWidth = 0;

    /** Height of original output-graphics. */
    protected int rHeight = 0;

    /** Average scale-factor. */
    protected float avgScale = 1;

    /** Scale-factor in X-direction */
    protected float xScale = 1;

    /** Scale-factor in Y-direction */
    protected float yScale = 1;

    /** Dots per Inch on output-device. */
    protected int dpi = 72;

    /** Defaulttoolkit. */
    protected Toolkit tk = Toolkit.getDefaultToolkit();

    /**
   * Sets the actual font.
   */
    public void setFont(Font font) {
        int idx;
        idx = fontCache.indexOf(font);
        if (idx < 0) {
            idx = fontCache.size();
            fontCache.addElement(font);
            if (scaling) {
                font = new Font(font.getName(), font.getStyle(), round(xScale * font.getSize()));
            }
            scaledFontCache.addElement(font);
        } else font = (Font) scaledFontCache.elementAt(idx);
        actFontIdx = idx;
        gd.setFont(font);
    }

    /**
   * Get fontmetrics for font.
   */
    public FontMetrics getFontMetrics(Font f) {
        return tk.getFontMetrics(f);
    }

    /**
   * Get fontmetrics for the actual phyical font.
   */
    public FontMetrics getPhysicalFontMetrics() {
        return gd.getFontMetrics();
    }

    /**
   * Get actual clipping-bounds.
   */
    public Rectangle getClipBounds() {
        Rectangle r = gd.getClipBounds();
        if (r == null) return null; else return new Rectangle(round(((float) r.x) / xScale), round(((float) r.y) / yScale), round(((float) r.width) / xScale), round(((float) r.height) / yScale));
    }

    /**
   * Clipping.
   */
    public void clipRect(int x, int y, int width, int height) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.clipRect(r.x, r.y, r.width, r.height);
    }

    /**
   * Clipping.
   */
    public void setClip(int x, int y, int width, int height) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.clipRect(r.x, r.y, r.width, r.height);
    }

    /**
   * Clipping.
   */
    public Shape getClip() {
        return gd.getClip();
    }

    /**
   * Clipping.
   */
    public void setClip(Shape clip) {
        Rectangle r = clip.getBounds();
        r = getScaledRectangle(r.x, r.y, r.width, r.height);
        gd.setClip(r.x, r.y, r.width, r.height);
    }

    /**
   * Copy area.
   */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        Dimension d = getScaledDim(dx, dy);
        gd.copyArea(r.x, r.y, r.width, r.height, d.width, d.height);
    }

    /**
   * Draw a dot with physical resolution.
   * Warning: This Methode doesn't exist at Graphics!
   */
    public void drawRawDot(int x, int y) {
        if (scaling) {
            Point p = getScaledPoint(x, y);
            gd.drawRect(p.x, p.y, 0, 0);
        } else gd.drawRect(x, y, 0, 0);
    }

    /**
   * Draw cross with physical resolution.
   * Warning: This Methode doesn't exist at Graphics!
   */
    public void drawRawCross(int x, int y, int w, int h) {
        if (scaling) {
            Point p = getScaledPoint(x, y);
            gd.drawLine(p.x - (w - 1) / 2, p.y, p.x + w / 2, p.y);
            gd.drawLine(p.x, p.y - (h - 1) / 2, p.x, p.y + h / 2);
        } else {
            gd.drawLine(x - (w - 1) / 2, y, x + w / 2, y);
            gd.drawLine(x, y - (h - 1) / 2, x, y + h / 2);
        }
    }

    /**
   * Draw a line.
   */
    public void drawLine(int x1, int y1, int x2, int y2) {
        if (scaling) {
            Point p1 = getScaledPoint(x1, y1);
            Point p2 = getScaledPoint(x2, y2);
            gd.drawLine(p1.x, p1.y, p2.x, p2.y);
        } else gd.drawLine(x1, y1, x2, y2);
    }

    /**
   * Draw rectangle.
   */
    public void drawRect(int x, int y, int width, int height) {
        Point p1 = getScaledPoint(x, y);
        Point p2 = getScaledPoint(x + width - 1, y + height - 1);
        if (!correction) {
            p2.x++;
            p2.y++;
        }
        gd.drawRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
    }

    /**
   * Draw filled rectangle.
   */
    public void fillRect(int x, int y, int width, int height) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.fillRect(r.x, r.y, r.width, r.height);
    }

    /**
   * Draw cleared rectangle.
   */
    public void clearRect(int x, int y, int width, int height) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.clearRect(r.x, r.y, r.width, r.height);
    }

    /**
   * Draw rounded rectangle.
   */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (correction) {
            height--;
            width--;
        }
        Rectangle r = getScaledRectangle(x, y, width, height);
        Dimension d = getScaledDim(arcWidth, arcHeight);
        gd.drawRoundRect(r.x, r.y, r.width, r.height, d.width, d.height);
    }

    /**
   * Draw filled rounded rectangle.
   */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        Dimension d = getScaledDim(arcWidth, arcHeight);
        gd.fillRoundRect(r.x, r.y, r.width, r.height, d.width, d.height);
    }

    /**
   * Draw filled 3D rectangle.
   */
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.fill3DRect(r.x, r.y, r.width, r.height, raised);
    }

    /**
   * Draw oval.
   */
    public void drawOval(int x, int y, int width, int height) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.drawOval(r.x, r.y, r.width, r.height);
    }

    /**
   * Draw filled oval.
   */
    public void fillOval(int x, int y, int width, int height) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.fillOval(r.x, r.y, r.width, r.height);
    }

    /**
   * Draw arc.
   */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.drawArc(r.x, r.y, r.width, r.height, startAngle, arcAngle);
    }

    /**
   * Draw filled arc.
   */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        Rectangle r = getScaledRectangle(x, y, width, height);
        gd.fillArc(r.x, r.y, r.width, r.height, startAngle, arcAngle);
    }

    /**
   * Draw polyline.
   */
    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        gd.drawPolyline(getScaledX(xPoints, nPoints), getScaledY(yPoints, nPoints), nPoints);
    }

    /**
   * Draw polygon.
   */
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        gd.drawPolygon(getScaledX(xPoints, nPoints), getScaledY(yPoints, nPoints), nPoints);
    }

    /**
   * Draw filled polygon.
   */
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        gd.fillPolygon(getScaledX(xPoints, nPoints), getScaledY(yPoints, nPoints), nPoints);
    }

    /**
   * Draw string.
   */
    public void drawString(String str, int x, int y) {
        Point p = getScaledPoint(x, y);
        gd.drawString(str, p.x, p.y);
    }

    /**
   * Draw image.
   */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        if (scaling) {
            Rectangle r = getScaledRectangle(x, y, img.getWidth(observer), img.getHeight(observer));
            return gd.drawImage(img, r.x, r.y, r.width, r.height, observer);
        } else return gd.drawImage(img, x, y, observer);
    }

    /**
   * Draw image.
   */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        if (scaling) {
            Rectangle r = getScaledRectangle(x, y, width, height);
            return gd.drawImage(img, r.x, r.y, r.width, r.height, observer);
        } else return gd.drawImage(img, x, y, width, height, observer);
    }

    /**
   * Draw image.
   */
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        if (scaling) {
            Rectangle r = getScaledRectangle(x, y, img.getWidth(observer), img.getHeight(observer));
            return gd.drawImage(img, r.x, r.y, r.width, r.height, bgcolor, observer);
        } else return gd.drawImage(img, x, y, bgcolor, observer);
    }

    /**
   * Draw image.
   */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        if (scaling) {
            Point p = getScaledPoint(x, y);
            Dimension d = getScaledDim(width, height);
            return gd.drawImage(img, p.x, p.y, d.width, d.height, bgcolor, observer);
        } else return gd.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    /**
   * Draw image.
   */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        if (scaling) {
            Rectangle rd = getScaledRectangle(dx1, dy1, dx2 - dx1, dy2 - dy1);
            Rectangle rs = getScaledRectangle(sx1, sy1, sx2 - sx1, sy2 - sy1);
            return gd.drawImage(img, rd.x, rd.y, rd.x + rd.width, rd.y + rd.height, rs.x, rs.y, rs.x + rs.width, rs.y + rs.height, observer);
        } else return gd.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    /**
   * Draw image.
   */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        if (scaling) {
            Rectangle rd = getScaledRectangle(dx1, dy1, dx2 - dx1, dy2 - dy1);
            Rectangle rs = getScaledRectangle(sx1, sy1, sx2 - sx1, sy2 - sy1);
            return gd.drawImage(img, rd.x, rd.y, rd.x + rd.width, rd.y + rd.height, rs.x, rs.y, rs.x + rs.width, rs.y + rs.height, bgcolor, observer);
        } else return gd.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    /**
   * disposes graphics.
   */
    public void dispose() {
        gd.dispose();
    }
}

;
