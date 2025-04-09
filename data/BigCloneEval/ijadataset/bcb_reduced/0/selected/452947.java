package net.sourceforge.webcompmath.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import net.sourceforge.webcompmath.awt.Computable;
import net.sourceforge.webcompmath.awt.WcmWorker;
import net.sourceforge.webcompmath.data.Cases;
import net.sourceforge.webcompmath.data.CloneableFunction;
import net.sourceforge.webcompmath.data.DataUtils;
import net.sourceforge.webcompmath.data.Function;
import net.sourceforge.webcompmath.data.Value;
import net.sourceforge.webcompmath.data.Variable;

/**
 * A ParametricCurve is defined by two functions, x(t) and y(t) of a variable,
 * t, for t in a specified interval. The curve is simply defined as a sequence
 * of line segments connecting points of the form (x(t),y(t)), except where one
 * of the functions is undefined. Also, in some cases a discontinuity will be
 * detected and no line will be drawn between two of the points.
 */
public class ParametricCurve extends Drawable implements Computable {

    private static final long serialVersionUID = 3779595665672329088L;

    private Function xFunc, yFunc;

    private Color graphColor = Color.magenta;

    private boolean changed;

    private Value tmin, tmax;

    private Value intervals;

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

    private SetupData setupData = null;

    private ParametricCurveWorker worker;

    /**
	 * Create a ParametricCurve with nothing to graph. The functions and other
	 * values can be set later.
	 */
    public ParametricCurve() {
        this(null, null, null, null, null);
    }

    /**
	 * Create a parametric curve with x and y coordinates given by the specified
	 * functions of the parameter t. Defaults values are used for tmin, tmax,
	 * and the number of intervals. If either function is null, nothing is
	 * drawn.
	 * 
	 * @param xFunc
	 *            A Function of one variable giving the x-coordinate of points
	 *            on the curve. If this is null, then nothing will be drawn.
	 * @param yFunc
	 *            A Function of one variable giving the y-coordinate of points
	 *            on the curve. If this is null, then nothing will be drawn.
	 */
    public ParametricCurve(Function xFunc, Function yFunc) {
        this(xFunc, yFunc, null, null, null);
    }

    /**
	 * Create a parametric curve with the specified values.
	 * 
	 * @param xFunc
	 *            A Function of one variable giving the x-coordinate of points
	 *            on the curve. If this is null, then nothing will be drawn.
	 * @param yFunc
	 *            A Function of one variable giving the y-coordinate of points
	 *            on the curve. If this is null, then nothing will be drawn.
	 * @param tmin
	 *            A Value object giving one endpoint of the domain of the
	 *            parameter. If this is null, the default value -5 is used.
	 * @param tmax
	 *            A Value object giving the second endpoint of the domain of the
	 *            parameter. If this is null, the default value 5 is used. Note
	 *            that it is not required that tmax be greater than tmin.
	 * @param intervals
	 *            A Value object giving the number of intervals into which the
	 *            domain is subdivided. If this is null, the default value 200
	 *            is used. The number of points on the curve will be the number
	 *            of intervals plus one (unless a function is undefined at some
	 *            value of the parameter or if a discontinuity is detected). The
	 *            number of intervals is clamped to the range 1 to 10000. Values
	 *            outside this range would certainly be unreasonable.
	 */
    public ParametricCurve(Function xFunc, Function yFunc, Value tmin, Value tmax, Value intervals) {
        if ((xFunc != null && xFunc.getArity() != 1) || (yFunc != null && yFunc.getArity() != 1)) throw new IllegalArgumentException("Internal Error:  The functions that define a parametric curve must be functions of one variable.");
        this.xFunc = xFunc;
        this.yFunc = yFunc;
        this.tmin = tmin;
        this.tmax = tmax;
        this.intervals = intervals;
        changed = true;
    }

    /**
	 * Set the color to be used for drawing the graph.
	 * 
	 * @param c
	 *            graph color
	 */
    public void setColor(Color c) {
        if (c != null & !c.equals(graphColor)) {
            graphColor = c;
            needsRedraw();
        }
    }

    /**
	 * Get the color that is used to draw the graph.
	 * 
	 * @return graph color
	 */
    public Color getColor() {
        return graphColor;
    }

    /**
	 * Sets the functions that gives the coordinates of the curve to be graphed.
	 * If either function is null, then nothing is drawn. If non-null, each
	 * function must be a function of one variable.
	 * 
	 * @param x
	 *            function defining the x coord
	 * @param y
	 *            function defining the y coord
	 */
    public synchronized void setFunctions(Function x, Function y) {
        setXFunction(x);
        setYFunction(y);
    }

    /**
	 * Set the function that gives the x-coordinate of the curve to be graphed.
	 * If this is null, then nothing is drawn. If non-null, it must be a
	 * function of one variable.
	 * 
	 * @param x
	 *            function defining the x coord
	 */
    public synchronized void setXFunction(Function x) {
        if (x != null && x.getArity() != 1) throw new IllegalArgumentException("Internal Error:  ParametricCurve can only graph functions of one variable.");
        if (x != xFunc) {
            xFunc = x;
            changed = true;
            needsRedraw();
        }
    }

    /**
	 * Set the function that gives the y-coordinate of the curve to be graphed.
	 * If this is null, then nothing is drawn. If non-null, it must be a
	 * function of one variable.
	 * 
	 * @param y
	 *            function defining the yfunction defining the x coord coord
	 */
    public synchronized void setYFunction(Function y) {
        if (y != null && y.getArity() != 1) throw new IllegalArgumentException("Internal Error:  ParametricCurve can only graph functions of one variable.");
        if (y != yFunc) {
            yFunc = y;
            changed = true;
            needsRedraw();
        }
    }

    /**
	 * Get the (possibly null) function that gives the x-coordinate of the
	 * curve.
	 * 
	 * @return function defining the x coord
	 */
    public Function getXFunction() {
        return xFunc;
    }

    /**
	 * Get the (possibly null) function that gives the y-coordinate of the
	 * curve.
	 * 
	 * @return function defining the y coord
	 */
    public Function getYFunction() {
        return yFunc;
    }

    /**
	 * Specify the number of subintervals into which the domain of the
	 * parametric curve is divided. The interval (tmin,tmax) is divided into
	 * subintervals. X and y coordinates of the parametric curve are computed at
	 * each endpoint of these subintervals, and then the points are connected by
	 * lines. If the parameter of this function is null, or if no interval count
	 * is ever specified, then a default value of 200 is used.
	 * 
	 * @param intervalCount
	 *            number of subintervals
	 */
    public void setIntervals(Value intervalCount) {
        intervals = intervalCount;
        changed = true;
    }

    /**
	 * Get the value object, possibly null, that determines the number of points
	 * on the curve.
	 * 
	 * @return number of subintervals
	 */
    public Value getIntervals() {
        return intervals;
    }

    /**
	 * 
	 * Set the Value objects that specify the domain of the parameter.
	 * 
	 * @param tmin
	 *            min value of parameter
	 * @param tmax
	 *            max value of parameter
	 */
    public void setLimits(Value tmin, Value tmax) {
        setTMin(tmin);
        setTMax(tmax);
    }

    /**
	 * Get the Value object, possibly null, that gives the left endpoint of the
	 * domain of the parameter.
	 * 
	 * @return min value of parameter
	 */
    public Value getTMin() {
        return tmin;
    }

    /**
	 * Get the Value object, possibly null, that gives the right endpoint of the
	 * domain of the parameter.
	 * 
	 * @return max value of parameter
	 */
    public Value getTMax() {
        return tmax;
    }

    /**
	 * Set the Value object that gives the left endpoint of the domain of the
	 * parameter. If this is null, then a default value of -5 is used for the
	 * left endpoint. (Note: actually, it's not required that tmin be less than
	 * tmax, so this might really be the "right" endpoint.)
	 * 
	 * @param tmin
	 *            min value of parameter
	 */
    public void setTMin(Value tmin) {
        this.tmin = tmin;
        changed = true;
    }

    /**
	 * Set the Value object that gives the right endpoint of the domain of the
	 * parameter. If this is null, then a default value of 5 is used for the
	 * right endpoint. (Note: actually, it's not required that tmin be less than
	 * tmax, so this might really be the "left" endpoint.)
	 * 
	 * @param tmax
	 *            max value of parameter
	 */
    public void setTMax(Value tmax) {
        this.tmax = tmax;
        changed = true;
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
            } else if (lineWidth < 1) {
                lineWidth = 1;
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
	 * Recompute data for the graph and make sure that the area of the display
	 * canvas that shows the graph is redrawn. This method is ordinarily called
	 * by a Controller.
	 */
    public synchronized void compute() {
        changed = true;
        needsRedraw();
    }

    /**
	 * Draw the graph (possibly recomputing the data if the CoordinateRect has
	 * changed). This is not usually called directly.
	 * 
	 * @param g
	 *            graphics context
	 * @param coordsChanged
	 *            true or false
	 */
    @Override
    public synchronized void draw(Graphics g, boolean coordsChanged) {
        if (coords == null) {
            return;
        }
        SetupData newData = new SetupData(coords, xFunc, yFunc, tmin, tmax, intervals);
        if (changed || coordsChanged || setupData == null || setupData.xcoord == null || setupData.ycoord == null) {
            changed = false;
            if (!queue.isUseWorker()) {
                setupData = setup(newData);
            } else {
                if (worker != null) {
                    if (!worker.isDone() && !worker.isCancelled()) {
                        worker.cancel(true);
                    }
                }
                worker = new ParametricCurveWorker(this, newData);
                worker.start();
            }
        }
        if (setupData == null || setupData.xcoord == null || setupData.xcoord.length == 0) {
            return;
        }
        g.setColor(graphColor);
        Graphics2D g2 = (Graphics2D) g;
        Stroke oldStroke = g2.getStroke();
        RenderingHints oldHints = g2.getRenderingHints();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (lineStyle == DASHED_STYLE) {
            BasicStroke b = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 0.0f);
            g2.setStroke(b);
        } else {
            g2.setStroke(new BasicStroke(lineWidth));
        }
        GeneralPath path = new GeneralPath();
        float x = setupData.xcoord[0];
        float y = setupData.ycoord[0];
        path.moveTo(x, y);
        for (int i = 1; i < setupData.xcoord.length; i++) {
            if (setupData.xcoord[i] == Float.MIN_VALUE) {
                do {
                    i++;
                } while (i < setupData.xcoord.length && setupData.xcoord[i] == Float.MIN_VALUE);
                if (i < setupData.xcoord.length) {
                    x = setupData.xcoord[i];
                    y = setupData.ycoord[i];
                    path.moveTo(x, y);
                }
            } else {
                x = setupData.xcoord[i];
                y = setupData.ycoord[i];
                path.lineTo(x, y);
            }
        }
        g2.draw(path);
        g2.setStroke(oldStroke);
        g2.setRenderingHints(oldHints);
    }

    private double[] v = new double[1];

    private Cases case1x = new Cases();

    private Cases case2x = new Cases();

    private Cases case1y = new Cases();

    private Cases case2y = new Cases();

    private Cases case3x = new Cases();

    private Cases case3y = new Cases();

    private Vector points = new Vector(250);

    private Point2D.Float eval(double t, Cases xcases, Cases ycases, Function xFunc, Function yFunc) {
        v[0] = t;
        if (xcases != null) xcases.clear();
        if (ycases != null) ycases.clear();
        double x = xFunc.getValueWithCases(v, xcases);
        double y = yFunc.getValueWithCases(v, ycases);
        if (Double.isNaN(x) || Double.isNaN(y)) return null;
        float xInt = coords.xToPixelF(x);
        float yInt = coords.yToPixelF(y);
        if (Math.abs(xInt) > 10000 || Math.abs(yInt) > 10000) return null;
        return new Point2D.Float(xInt, yInt);
    }

    @SuppressWarnings("unchecked")
    private SetupData setup(SetupData data) {
        if (data == null || data.xFunc == null || data.yFunc == null) {
            return null;
        }
        if (Double.isNaN(data.tmin_val)) {
            return null;
        }
        Thread ct = Thread.currentThread();
        points.setSize(0);
        double delta = (data.tmax_val - data.tmin_val) / data.intervals_val;
        Point2D.Float point, prevpoint;
        double t = data.tmin_val;
        prevpoint = eval(t, case1x, case1y, data.xFunc, data.yFunc);
        if (prevpoint != null) points.addElement(prevpoint);
        for (int i = 1; i <= data.intervals_val && !ct.isInterrupted(); i++) {
            t = data.tmin_val + i * delta;
            point = eval(t, case2x, case2y, data.xFunc, data.yFunc);
            if (point != null && prevpoint != null) {
                if (!case1x.equals(case2x) || !case1y.equals(case2y)) discontinuity(prevpoint, data.tmin_val + (i - 1) * delta, point, t, 0, data.xFunc, data.yFunc); else points.addElement(point);
            } else if (prevpoint == null && point != null) {
                becomesDefined(prevpoint, data.tmin_val + (i - 1) * delta, point, t, 0, data.xFunc, data.yFunc);
            } else if (prevpoint != null && point == null) {
                becomesUndefined(prevpoint, data.tmin_val + (i - 1) * delta, point, t, 0, data.xFunc, data.yFunc);
            }
            prevpoint = point;
            Cases temp = case1x;
            case1x = case2x;
            case2x = temp;
            temp = case1y;
            case1y = case2y;
            case2y = temp;
        }
        if (!ct.isInterrupted()) {
            data.xcoord = new float[points.size()];
            data.ycoord = new float[points.size()];
            for (int i = 0; i < data.ycoord.length; i++) {
                Point2D.Float p = (Point2D.Float) points.elementAt(i);
                data.xcoord[i] = p.x;
                data.ycoord[i] = p.y;
            }
            return data;
        } else {
            return null;
        }
    }

    private static int MAXDEPTH = 10;

    @SuppressWarnings("unchecked")
    void discontinuity(Point2D.Float p1, double t1, Point2D.Float p2, double t2, int depth, Function xFunc, Function yFunc) {
        if (depth >= MAXDEPTH || (Math.abs(p1.x - p2.x) < 2 && Math.abs(p1.y - p2.y) < 2)) {
            if (points.elementAt(points.size() - 1) != p1) points.addElement(p1);
            if (depth >= MAXDEPTH) points.addElement(new Point2D.Float(Float.MIN_VALUE, 0));
            points.addElement(p2);
            return;
        }
        double t = (t1 + t2) / 2;
        Point2D.Float p = eval(t, case3x, case3y, xFunc, yFunc);
        if (p == null) {
            becomesUndefined(p1, t1, p, t, depth + 1, xFunc, yFunc);
            becomesDefined(p, t, p2, t2, depth + 1, xFunc, yFunc);
        } else if (case3x.equals(case1x) && case3y.equals(case1y)) {
            discontinuity(p, t, p2, t2, depth + 1, xFunc, yFunc);
        } else if (case3x.equals(case2x) && case3y.equals(case2y)) {
            discontinuity(p1, t1, p, t, depth + 1, xFunc, yFunc);
        } else {
            discontinuity(p1, t1, p, t, depth + 2, xFunc, yFunc);
            discontinuity(p, t, p2, t2, depth + 2, xFunc, yFunc);
        }
    }

    @SuppressWarnings("unchecked")
    void becomesUndefined(Point2D.Float p1, double t1, Point2D.Float p2, double t2, int depth, Function xFunc, Function yFunc) {
        if (depth >= MAXDEPTH) {
            if (points.elementAt(points.size() - 1) != p1) points.addElement(p1);
            points.addElement(new Point2D.Float(Float.MIN_VALUE, 0));
            return;
        }
        double t = (t1 + t2) / 2;
        Point2D.Float p = eval(t, null, null, xFunc, yFunc);
        if (p == null) becomesUndefined(p1, t1, p, t, depth + 1, xFunc, yFunc); else becomesUndefined(p, t, p2, t2, depth + 1, xFunc, yFunc);
    }

    @SuppressWarnings("unchecked")
    void becomesDefined(Point2D.Float p1, double t1, Point2D.Float p2, double t2, int depth, Function xFunc, Function yFunc) {
        if (depth >= MAXDEPTH) {
            if (points.size() > 0) points.addElement(new Point2D.Float(Float.MIN_VALUE, 0));
            points.addElement(p2);
            return;
        }
        double t = (t1 + t2) / 2;
        Point2D.Float p = eval(t, null, null, xFunc, yFunc);
        if (p != null) becomesDefined(p1, t1, p, t, depth + 1, xFunc, yFunc); else becomesDefined(p, t, p2, t2, depth + 1, xFunc, yFunc);
    }

    private class ParametricCurveWorker extends WcmWorker<SetupData> {

        ParametricCurve d;

        SetupData data;

        /**
		 * Construct a ParametricCurveWorker for the specified Drawable
		 * 
		 * @param d
		 *            the ParametricCurve
		 * @param data
		 *            the input data
		 */
        public ParametricCurveWorker(ParametricCurve d, SetupData data) {
            super();
            this.d = d;
            this.data = data;
        }

        /**
		 * @see net.sourceforge.webcompmath.awt.WcmWorker#construct()
		 */
        @Override
        protected SetupData construct() throws Exception {
            return d.setup(data);
        }

        /**
		 * Check if all of the drawables are done.
		 * 
		 * @see net.sourceforge.webcompmath.awt.WcmWorker#finished()
		 */
        @Override
        protected void finished() {
            try {
                SetupData data = get();
                if (data != null) {
                    d.setupData = data;
                    d.needsRedraw();
                }
            } catch (CancellationException e) {
            } catch (ExecutionException e) {
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private class SetupData {

        double xmin;

        double xmax;

        double ymin;

        double ymax;

        int height;

        int width;

        int top;

        int left;

        int gap;

        Function xFunc;

        Function yFunc;

        double tmax_val;

        double tmin_val;

        int intervals_val;

        private float[] xcoord;

        private float[] ycoord;

        SetupData(CoordinateRect c, Function xf, Function yf, Value tmin, Value tmax, Value intervals) {
            xmin = c.getXmin();
            xmax = c.getXmax();
            ymin = c.getYmin();
            ymax = c.getYmax();
            height = c.getHeight();
            width = c.getWidth();
            top = c.getTop();
            left = c.getLeft();
            gap = c.getGap();
            Variable[] clonedVars = null;
            if (queue.isUseWorker() && xf != null && xf instanceof CloneableFunction) {
                CloneableFunction cf = (CloneableFunction) xf;
                clonedVars = DataUtils.cloneVariables(cf.getVariables(), null);
                this.xFunc = cf.cloneFunction(clonedVars);
            } else {
                this.xFunc = xf;
            }
            if (queue.isUseWorker() && yf != null && yf instanceof CloneableFunction) {
                CloneableFunction cf = (CloneableFunction) yf;
                clonedVars = DataUtils.cloneVariables(cf.getVariables(), clonedVars);
                this.yFunc = cf.cloneFunction(clonedVars);
            } else {
                this.yFunc = yf;
            }
            double intervals_val_d;
            if (tmin == null) tmin_val = -5; else tmin_val = tmin.getVal();
            if (tmax == null) tmax_val = 5; else tmax_val = tmax.getVal();
            if (intervals == null) intervals_val_d = 200; else intervals_val_d = intervals.getVal();
            if (Double.isInfinite(tmin_val) || Double.isInfinite(tmax_val) || Double.isInfinite(intervals_val_d) || Double.isNaN(tmax_val) || Double.isNaN(intervals_val_d)) {
                tmin_val = Double.NaN;
            }
            if (intervals_val_d < 1) intervals_val = 1; else if (intervals_val > 10000) intervals_val = 10000; else intervals_val = (int) Math.round(intervals_val_d);
        }
    }
}
