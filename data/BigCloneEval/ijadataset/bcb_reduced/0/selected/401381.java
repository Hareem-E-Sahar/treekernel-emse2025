package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * A renderer that connects data points with natural cubic splines and/or
 * draws shapes at each data point.  This renderer is designed for use with
 * the {@link XYPlot} class.
 *
 * @since 1.0.7
 */
public class XYSplineRenderer extends XYLineAndShapeRenderer {

    /**
     * To collect data points for later splining.
     */
    private Vector points;

    /**
     * Resolution of splines (number of line segments between points)
     */
    private int precision;

    /**
     * Creates a new instance with the 'precision' attribute defaulting to
     * 5.
     */
    public XYSplineRenderer() {
        this(5);
    }

    /**
     * Creates a new renderer with the specified precision.
     *
     * @param precision  the number of points between data items.
     */
    public XYSplineRenderer(int precision) {
        super();
        if (precision <= 0) {
            throw new IllegalArgumentException("Requires precision > 0.");
        }
        this.precision = precision;
    }

    /**
     * Get the resolution of splines.
     *
     * @return Number of line segments between points.
     *
     * @see #setPrecision(int)
     */
    public int getPrecision() {
        return this.precision;
    }

    /**
     * Set the resolution of splines and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param p  number of line segments between points (must be > 0).
     *
     * @see #getPrecision()
     */
    public void setPrecision(int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("Requires p > 0.");
        }
        this.precision = p;
        fireChangeEvent();
    }

    /**
     * Initialises the renderer.
     * <P>
     * This method will be called before the first item is rendered, giving the
     * renderer an opportunity to initialise any state information it wants to
     * maintain.  The renderer can do nothing if it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return The renderer state.
     */
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data, PlotRenderingInfo info) {
        State state = (State) super.initialise(g2, dataArea, plot, data, info);
        state.setProcessVisibleItemsOnly(false);
        this.points = new Vector();
        setDrawSeriesLineAsPath(true);
        return state;
    }

    /**
     * Draws the item (first pass). This method draws the lines
     * connecting the items. Instead of drawing separate lines,
     * a GeneralPath is constructed and drawn at the end of
     * the series painting.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataArea  the area within which the data is being drawn.
     */
    protected void drawPrimaryLineAsPath(XYItemRendererState state, Graphics2D g2, XYPlot plot, XYDataset dataset, int pass, int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis, Rectangle2D dataArea) {
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            ControlPoint p = new ControlPoint(plot.getOrientation() == PlotOrientation.HORIZONTAL ? (float) transY1 : (float) transX1, plot.getOrientation() == PlotOrientation.HORIZONTAL ? (float) transX1 : (float) transY1);
            if (!this.points.contains(p)) {
                this.points.add(p);
            }
        }
        if (item == dataset.getItemCount(series) - 1) {
            State s = (State) state;
            if (this.points.size() > 1) {
                ControlPoint cp0 = (ControlPoint) this.points.get(0);
                s.seriesPath.moveTo(cp0.x, cp0.y);
                if (this.points.size() == 2) {
                    ControlPoint cp1 = (ControlPoint) this.points.get(1);
                    s.seriesPath.lineTo(cp1.x, cp1.y);
                } else {
                    int np = this.points.size();
                    float[] d = new float[np];
                    float[] x = new float[np];
                    float y;
                    float t;
                    float oldy = 0;
                    float oldt = 0;
                    float[] a = new float[np];
                    float t1;
                    float t2;
                    float[] h = new float[np];
                    for (int i = 0; i < np; i++) {
                        ControlPoint cpi = (ControlPoint) this.points.get(i);
                        x[i] = cpi.x;
                        d[i] = cpi.y;
                    }
                    for (int i = 1; i <= np - 1; i++) {
                        h[i] = x[i] - x[i - 1];
                    }
                    float[] sub = new float[np - 1];
                    float[] diag = new float[np - 1];
                    float[] sup = new float[np - 1];
                    for (int i = 1; i <= np - 2; i++) {
                        diag[i] = (h[i] + h[i + 1]) / 3;
                        sup[i] = h[i + 1] / 6;
                        sub[i] = h[i] / 6;
                        a[i] = (d[i + 1] - d[i]) / h[i + 1] - (d[i] - d[i - 1]) / h[i];
                    }
                    solveTridiag(sub, diag, sup, a, np - 2);
                    oldt = x[0];
                    oldy = d[0];
                    s.seriesPath.moveTo(oldt, oldy);
                    for (int i = 1; i <= np - 1; i++) {
                        for (int j = 1; j <= this.precision; j++) {
                            t1 = (h[i] * j) / this.precision;
                            t2 = h[i] - t1;
                            y = ((-a[i - 1] / 6 * (t2 + h[i]) * t1 + d[i - 1]) * t2 + (-a[i] / 6 * (t1 + h[i]) * t2 + d[i]) * t1) / h[i];
                            t = x[i - 1] + t1;
                            s.seriesPath.lineTo(t, y);
                            oldt = t;
                            oldy = y;
                        }
                    }
                }
                drawFirstPassShape(g2, pass, series, item, s.seriesPath);
            }
            this.points = new Vector();
        }
    }

    /**
     * Document me!
     *
     * @param sub
     * @param diag
     * @param sup
     * @param b
     * @param n
     */
    private void solveTridiag(float[] sub, float[] diag, float[] sup, float[] b, int n) {
        int i;
        for (i = 2; i <= n; i++) {
            sub[i] = sub[i] / diag[i - 1];
            diag[i] = diag[i] - sub[i] * sup[i - 1];
            b[i] = b[i] - sub[i] * b[i - 1];
        }
        b[n] = b[n] / diag[n];
        for (i = n - 1; i >= 1; i--) {
            b[i] = (b[i] - sup[i] * b[i + 1]) / diag[i];
        }
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYSplineRenderer)) {
            return false;
        }
        XYSplineRenderer that = (XYSplineRenderer) obj;
        if (this.precision != that.precision) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Represents a control point.
     */
    class ControlPoint {

        /** The x-coordinate. */
        public float x;

        /** The y-coordinate. */
        public float y;

        /**
         * Creates a new control point.
         *
         * @param x  the x-coordinate.
         * @param y  the y-coordinate.
         */
        public ControlPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Tests this point for equality with an arbitrary object.
         *
         * @param obj  the object (<code>null</code> permitted.
         *
         * @return A boolean.
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ControlPoint)) {
                return false;
            }
            ControlPoint that = (ControlPoint) obj;
            if (this.x != that.x) {
                return false;
            }
            ;
            return true;
        }
    }
}
