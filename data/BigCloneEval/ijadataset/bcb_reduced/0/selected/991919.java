package net.sourceforge.webcompmath.functions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import net.sourceforge.webcompmath.awt.Computable;
import net.sourceforge.webcompmath.draw.CoordinateRect;
import net.sourceforge.webcompmath.draw.DisplayCanvas;
import net.sourceforge.webcompmath.draw.Drawable;

/**
 * A TableFunctionGraph is a Drawable object that can be added to a
 * CoordinateRect (or DisplayCanvas). It draws the graph of a specified
 * TableFunction. A TableFunction is a function and can also be graphed by an
 * object of the class edu.hws.jcm.draw.Graph1D. However, a TableFunctionGraph
 * offers the option of showing the points from the table that defines the
 * function as small disks (true by default) and the option of making the graph
 * "interactive" so that the user can drag the points (false by default).
 */
public class TableFunctionGraph extends Drawable implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = -5305679032701765607L;

    private TableFunction function;

    private boolean showPoints;

    private boolean interactive;

    private Computable onDrag;

    private Computable onFinishDrag;

    private Color color;

    /**
	 * The width, in pixels, of lines. It is restricted to being an integer in
	 * the range from 1 to 10.
	 */
    protected int lineWidth = 1;

    /**
	 * Specifies that the shape should be drawn with solid line(s). This is the
	 * default.
	 */
    public static final int SOLID_STYLE = 0;

    /**
	 * Specifies that the shape should be drawn with dashed line(s).
	 */
    public static final int DASHED_STYLE = 1;

    /**
	 * The line style, SOLID_STYLE or DASHED_STYLE
	 */
    protected int lineStyle = SOLID_STYLE;

    private float dash[] = { 1, 3, 1, 3 };

    /**
	 * Create a TableFunctionGraph that initially draws no function. A function
	 * can be set later with setFunction.
	 */
    public TableFunctionGraph() {
        this(null);
    }

    /**
	 * Create a TableFunctionGraph to draw the specified TableFunction.
	 * 
	 * @param function
	 *            table function to graph
	 */
    public TableFunctionGraph(TableFunction function) {
        this.function = function;
        this.color = Color.magenta;
        showPoints = true;
    }

    /**
	 * Set the function whose graph is drawn by this TableFunctionGraph. If the
	 * value is null, nothing is drawn
	 * 
	 * @param function
	 *            table function to graph
	 */
    public void setFunction(TableFunction function) {
        this.function = function;
        needsRedraw();
    }

    /**
	 * Get the TableFunction whose graph is drawn by this TableFunctionGraph. If
	 * the value is null, then no graph is drawn.
	 * 
	 * @return table function being graphed
	 */
    public TableFunction getFunction() {
        return function;
    }

    /**
	 * Specify a controller whose compute() method will be called repeatedly as
	 * the user drags one of the points from the table function. This only
	 * applies if the "interactive" property is true.
	 * 
	 * @param c
	 *            computable to use (usually a controller)
	 */
    public void setOnDrag(Computable c) {
        onDrag = c;
    }

    /**
	 * Get the Computable that is notified as the user drags a point.
	 * 
	 * @return computable notified (usually a controller)
	 */
    public Computable getOnDrag() {
        return onDrag;
    }

    /**
	 * Specify a controller whose compute() method will be called once when the
	 * user finishes dragging one of the points from the table function. This
	 * only applies if the "interactive" property is true.
	 * 
	 * @param c
	 *            controller to use
	 */
    public void setOnFinishDrag(Computable c) {
        onFinishDrag = c;
    }

    /**
	 * Get the Computable that is notified when the user finishes dragging a
	 * point.
	 * 
	 * @return controller notified
	 */
    public Computable getOnFinishDrag() {
        return onFinishDrag;
    }

    /**
	 * Set the value of the interactive property, which is true if the user can
	 * modify the function by dragging the points from the table. The default is
	 * false.
	 * 
	 * @param interactive
	 *            true or false
	 */
    public void setInteractive(boolean interactive) {
        if (this.interactive == interactive) return;
        if (this.interactive && canvas != null) {
            canvas.removeMouseListener(this);
            canvas.removeMouseMotionListener(this);
        }
        this.interactive = interactive;
        if (this.interactive && canvas != null) {
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
        }
    }

    /**
	 * Get the value of the interactive property, which is true if the user can
	 * modify the function by dragging the points from the table.
	 * 
	 * @return true or false
	 */
    public boolean getInteractive() {
        return interactive;
    }

    /**
	 * Set the showPoints property, which determines whether the points from the
	 * table that defines the function are visible as little disks. The default
	 * is true;
	 * 
	 * @param show
	 *            true or false
	 */
    public void setShowPoints(boolean show) {
        showPoints = show;
        needsRedraw();
    }

    /**
	 * Get the showPoints property, which determines whether the points from the
	 * table that defines the function are visible as little disks.
	 * 
	 * @return true or false
	 */
    public boolean getShowPoints() {
        return showPoints;
    }

    /**
	 * Set the color that is used for drawing the graph. The defalt is magenta.
	 * If the specified Color value is null, the call to setColor is ignored.
	 * 
	 * @param c
	 *            color for drawing
	 */
    public void setColor(Color c) {
        if (c != null) {
            color = c;
            needsRedraw();
        }
    }

    /**
	 * Get the non-null color that is used for drawing the graph.
	 * 
	 * @return color for drawing
	 */
    public Color getColor() {
        return color;
    }

    /**
	 * Set the width, in pixels, of lines that are drawn. If set to 0, the
	 * thinnest possible line is drawn.
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
	 * Get the width, in pixels, of lines that are drawn.
	 * 
	 * @return line width
	 */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
	 * Get the line style, SOLID_STYLE or DASHED_STYLE
	 * 
	 * @return Returns the lineStyle.
	 */
    public int getLineStyle() {
        return lineStyle;
    }

    /**
	 * Set the line style, SOLID_STYLE or DASHED_STYLE
	 * 
	 * @param lineStyle
	 *            The lineStyle to set.
	 */
    public void setLineStyle(int lineStyle) {
        if (lineStyle < 0 || lineStyle > DASHED_STYLE) {
            throw new IllegalArgumentException("Internal error:  Illegal value for lineStyle of DrawGeometric object.");
        }
        this.lineStyle = lineStyle;
        needsRedraw();
    }

    /**
	 * Sets the values of member variables canvas and coords. This is designed
	 * to be called only by the CoordinateRect class. This overrides
	 * Drawable.setOwnerData();
	 * 
	 * @param canvas
	 *            the DisplayCanvas to use
	 * @param coords
	 *            the CoordinateRect to use
	 */
    @Override
    protected void setOwnerData(DisplayCanvas canvas, CoordinateRect coords) {
        if (interactive && this.canvas != null) {
            canvas.removeMouseListener(this);
            canvas.removeMouseMotionListener(this);
        }
        super.setOwnerData(canvas, coords);
        if (interactive && this.canvas != null) {
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
        }
    }

    /**
	 * Provided as a convenience. If the function for this TableFunctionGraph is
	 * non-null, its style is set to the specified style, and the graph is
	 * redrawn. The parameter should be one of the constants
	 * TableFunction.SMOOTH, TableFunction.PIECEWISE_LINEAR, TableFunction.STEP,
	 * TableFunction.STEP_LEFT, or TableFunction.STEP_RIGHT.
	 * 
	 * @param style
	 *            function style
	 */
    public void setFunctionStyle(int style) {
        if (function != null && function.getStyle() != style) {
            function.setStyle(style);
            needsRedraw();
        }
    }

    /**
	 * Override the draw() method from class Drawable to draw the function. This
	 * is not meant to be called directly.
	 * 
	 * @param g
	 *            graphics context to use
	 * @param coordsChanged
	 *            true or false
	 */
    @Override
    public void draw(Graphics g, boolean coordsChanged) {
        if (function == null || coords == null) return;
        int ct = function.getPointCount();
        if (ct == 0) return;
        int startPt;
        int endPt;
        double xmin = coords.pixelToX(coords.getLeft());
        double xmax = coords.pixelToX(coords.getLeft() + coords.getWidth());
        if (function.getX(0) > xmax || function.getX(ct - 1) < xmin) return;
        startPt = 0;
        while (startPt < ct - 1 && function.getX(startPt + 1) <= xmin) startPt++;
        endPt = ct - 1;
        while (endPt > 1 && function.getX(endPt - 1) >= xmax) endPt--;
        double x, y, a, b;
        int xInt, yInt, aInt, bInt;
        g.setColor(color);
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints oldHints = g2.getRenderingHints();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke oldStroke = g2.getStroke();
        if (lineStyle == DASHED_STYLE) {
            BasicStroke bs = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 0.0f);
            g2.setStroke(bs);
        } else {
            g2.setStroke(new BasicStroke(lineWidth));
        }
        switch(function.getStyle()) {
            case TableFunction.SMOOTH:
                {
                    if (endPt > startPt) {
                        x = function.getX(startPt);
                        y = function.getVal(x);
                        xInt = coords.xToPixel(x);
                        yInt = coords.yToPixel(y);
                        double limit = xmax;
                        if (function.getX(endPt) < limit) limit = function.getX(endPt);
                        coords.xToPixel(function.getX(ct - 1));
                        aInt = xInt;
                        while (x < limit) {
                            aInt += 3;
                            a = coords.pixelToX(aInt);
                            if (a > limit) a = limit;
                            b = function.getVal(a);
                            bInt = coords.yToPixel(b);
                            g2.drawLine(xInt, yInt, aInt, bInt);
                            x = a;
                            xInt = aInt;
                            yInt = bInt;
                        }
                    }
                    break;
                }
            case TableFunction.PIECEWISE_LINEAR:
                {
                    x = function.getX(startPt);
                    xInt = coords.xToPixel(x);
                    y = function.getY(startPt);
                    yInt = coords.yToPixel(y);
                    for (int i = startPt + 1; i <= endPt; i++) {
                        a = function.getX(i);
                        aInt = coords.xToPixel(a);
                        b = function.getY(i);
                        bInt = coords.yToPixel(b);
                        g2.drawLine(xInt, yInt, aInt, bInt);
                        xInt = aInt;
                        yInt = bInt;
                    }
                    break;
                }
            case TableFunction.STEP:
                {
                    x = function.getX(startPt);
                    xInt = coords.xToPixel(x);
                    for (int i = startPt; i <= endPt; i++) {
                        if (i < endPt) {
                            double nextX = function.getX(i + 1);
                            a = (x + nextX) / 2;
                            x = nextX;
                        } else a = x;
                        aInt = coords.xToPixel(a);
                        y = function.getY(i);
                        yInt = coords.yToPixel(y);
                        g2.drawLine(xInt, yInt, aInt, yInt);
                        xInt = aInt;
                    }
                    break;
                }
            case TableFunction.STEP_LEFT:
                {
                    x = function.getX(startPt);
                    xInt = coords.xToPixel(x);
                    for (int i = startPt + 1; i <= endPt; i++) {
                        a = function.getX(i);
                        aInt = coords.xToPixel(a);
                        y = function.getY(i - 1);
                        yInt = coords.yToPixel(y);
                        g2.drawLine(xInt, yInt, aInt, yInt);
                        xInt = aInt;
                    }
                    break;
                }
            case TableFunction.STEP_RIGHT:
                {
                    x = function.getX(startPt);
                    xInt = coords.xToPixel(x);
                    for (int i = startPt + 1; i <= endPt; i++) {
                        a = function.getX(i);
                        aInt = coords.xToPixel(a);
                        y = function.getY(i);
                        yInt = coords.yToPixel(y);
                        g2.drawLine(xInt, yInt, aInt, yInt);
                        xInt = aInt;
                    }
                    break;
                }
        }
        if (!showPoints) {
            g2.setRenderingHints(oldHints);
            g2.setStroke(oldStroke);
            return;
        }
        for (int i = startPt; i <= endPt; i++) {
            x = function.getX(i);
            y = function.getY(i);
            xInt = coords.xToPixel(x);
            yInt = coords.yToPixel(y);
            g2.fillOval(xInt - 2, yInt - 2, lineWidth + 4, lineWidth + 4);
        }
        g2.setRenderingHints(oldHints);
        g2.setStroke(oldStroke);
    }

    private int dragPoint = -1;

    private int startX, startY;

    private int prevY;

    private boolean moved;

    /**
	 * Method required by the MouseListener interface. Defined here to support
	 * dragging of points on the function's graph. Not meant to be called
	 * directly.
	 * 
	 * @param evt
	 *            event created when user clicks mouse
	 */
    public void mousePressed(MouseEvent evt) {
        dragPoint = -1;
        if (function == null || getVisible() == false || canvas == null || coords == null || evt.isConsumed()) return;
        if (evt.isShiftDown() || evt.isMetaDown() || evt.isControlDown() || evt.isAltDown()) return;
        moved = false;
        int ct = function.getPointCount();
        for (int i = 0; i < ct; i++) {
            int x = coords.xToPixel(function.getX(i));
            int y = coords.yToPixel(function.getY(i));
            if (evt.getX() >= x - 3 && evt.getX() <= x + 3 && evt.getY() >= y - 3 && evt.getY() <= y + 3) {
                startX = evt.getX();
                prevY = startY = evt.getY();
                dragPoint = i;
                evt.consume();
                return;
            }
        }
    }

    /**
	 * Method required by the MouseListener interface. Defined here to support
	 * dragging of points on the function's graph. Not meant to be called
	 * directly.
	 * 
	 * @param evt
	 *            event created when user releases mouse button
	 */
    public void mouseReleased(MouseEvent evt) {
        if (dragPoint == -1) return;
        evt.consume();
        if (!moved) {
            dragPoint = -1;
            return;
        }
        mouseDragged(evt);
        dragPoint = -1;
        if (onFinishDrag != null) onFinishDrag.compute();
    }

    /**
	 * Method required by the MouseListener interface. Defined here to support
	 * dragging of points on the function's graph. Not meant to be called
	 * directly.
	 * 
	 * @param evt
	 *            event created while mouse being dragged
	 */
    public void mouseDragged(MouseEvent evt) {
        if (dragPoint == -1 || prevY == evt.getY()) return;
        evt.consume();
        if (!moved && Math.abs(evt.getY() - startY) < 3) return;
        moved = true;
        int y = evt.getY();
        if (y < coords.getTop() + 4) y = coords.getTop() + 4; else if (y > coords.getTop() + coords.getHeight() - 4) y = coords.getTop() + coords.getHeight() - 4;
        if (Math.abs(evt.getX() - startX) > 72) y = startY;
        if (y == prevY) return;
        prevY = y;
        function.setY(dragPoint, coords.pixelToY(prevY));
        needsRedraw();
        if (onDrag != null) onDrag.compute();
    }

    /**
	 * Empty method, required by the MouseListener interface.
	 * 
	 * @param evt
	 *            the event
	 */
    public void mouseClicked(MouseEvent evt) {
    }

    /**
	 * Empty method, required by the MouseMotionListener interface.
	 * 
	 * @param evt
	 *            the event
	 */
    public void mouseEntered(MouseEvent evt) {
    }

    /**
	 * Empty method, required by the MouseMotionListener interface.
	 * 
	 * @param evt
	 *            the event
	 */
    public void mouseExited(MouseEvent evt) {
    }

    /**
	 * Empty method, required by the MouseMotionListener interface.
	 * 
	 * @param evt
	 *            the event
	 */
    public void mouseMoved(MouseEvent evt) {
    }
}
