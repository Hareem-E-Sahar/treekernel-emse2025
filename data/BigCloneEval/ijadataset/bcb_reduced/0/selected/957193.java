package org.hypergraphdb.viewer.phoebe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import org.hypergraphdb.viewer.FEdge;
import org.hypergraphdb.viewer.GraphView;
import org.hypergraphdb.viewer.phoebe.event.PEdgeHandler;
import org.hypergraphdb.viewer.phoebe.util.*;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PBoundsLocator;
import edu.umd.cs.piccolox.util.PNodeLocator;

/**
 * This class extends a PNode and does most of what PPath would do but lets the
 * Painting get done by EdgePainter
 */
public class PEdgeView extends PPath implements PropertyChangeListener {

    /**
     * Draws splined curves for edges.
     */
    public static int CURVED_LINES = 1;

    /**
     * Draws straight lines for edges.
     */
    public static int STRAIGHT_LINES = 2;

    public static int NO_END = 0;

    public static int WHITE_DELTA = 1;

    public static int BLACK_DELTA = 2;

    public static int EDGE_COLOR_DELTA = 3;

    public static int WHITE_ARROW = 4;

    public static int BLACK_ARROW = 5;

    public static int EDGE_COLOR_ARROW = 6;

    public static int WHITE_DIAMOND = 7;

    public static int BLACK_DIAMOND = 8;

    public static int EDGE_COLOR_DIAMOND = 9;

    public static int WHITE_CIRCLE = 10;

    public static int BLACK_CIRCLE = 11;

    public static int EDGE_COLOR_CIRCLE = 12;

    public static int WHITE_T = 13;

    public static int BLACK_T = 14;

    public static int EDGE_COLOR_T = 15;

    /**
     * The FEdge Index that corresponds to the FEdge that we are a view on
     */
    protected FEdge edge;

    /**
     * The GraphView on which we are displayed
     */
    protected GraphView view;

    /**
     * The Point2D of where we are relative to our SourceNode
     */
    protected Point2D sourcePoint;

    /**
     * The Point2D of where we are relative to our TargetNode
     */
    protected Point2D targetPoint;

    /**
     * The list of points associated with the handles.
     */
    protected Vector handlePointList;

    /**
     * The PNodeView we are connected to
     */
    public PNodeView source;

    /**
     * The PNodeView we are connected to
     */
    public PNodeView target;

    /**
     * The locator used to find the center point of the node we are connected to
     */
    protected PNodeLocator targetLocator;

    /**
     * The locator used to find the center point of the node we are connected to
     */
    protected PNodeLocator sourceLocator;

    /**
     * The Icon that gets drawn at the end of us
     */
    protected PEdgeEndIcon sourceEdgeEnd;

    /**
     * The Icon that gets drawn at the end of us
     */
    protected PEdgeEndIcon targetEdgeEnd;

    /**
     * Our selection toggle <B>This is linked via
     * <code>FirePropertyChange</code> events to our FEdge</B>
     */
    protected boolean selected;

    /**
     * Our label TODO: more extendable
     */
    protected PLabel label;

    /**
     * The points that comprise the line drawn.
     */
    protected Point2D[] drawPoints;

    /**
     * The PEdgeView bend class.
     */
    protected Bend bend;

    /**
     * Render less complex if we are in a large graph
     */
    protected boolean inLargeGraph;

    protected boolean selfEdge = false;

    public static Paint DEFAULT_EDGE_STROKE_PAINT = java.awt.Color.black;

    public static Paint DEFAULT_EDGE_STROKE_PAINT_SELECTION = java.awt.Color.red;

    public static Paint DEFAULT_EDGE_END_PAINT = java.awt.Color.black;

    protected Paint selectedPaint = DEFAULT_EDGE_STROKE_PAINT_SELECTION;

    protected Paint unselectedPaint = DEFAULT_EDGE_STROKE_PAINT;

    protected Paint sourceEdgeEndPaint = DEFAULT_EDGE_END_PAINT;

    protected Paint targetEdgeEndPaint = DEFAULT_EDGE_END_PAINT;

    protected Paint sourceEdgeEndSelectedPaint = DEFAULT_EDGE_END_PAINT;

    protected Paint targetEdgeEndSelectedPaint = DEFAULT_EDGE_END_PAINT;

    protected int lineType = STRAIGHT_LINES;

    protected float strokeWidth = 1;

    protected int intSourceEdgeEnd = 3;

    protected int intTargetEdgeEnd = 3;

    /**
     * Create a new PEdgeView with default Attributes
     * 
     * @param edge
     *            The FEdge we are a view on
     * @param view
     *            the GraphView that we belong to
     */
    public PEdgeView(FEdge edge, GraphView view) {
        this.view = view;
        this.edge = edge;
        initializeEdgeView();
    }

    protected void initializeEdgeView() {
        source = view.getNodeView(edge.getSource());
        target = view.getNodeView(edge.getTarget());
        setStrokePaint(unselectedPaint);
        setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        selfEdge = source.equals(target);
        if (selfEdge) {
            targetLocator = PBoundsLocator.createSouthLocator((PNode) target);
            sourceLocator = PBoundsLocator.createSouthLocator((PNode) source);
        } else {
            targetLocator = new PNodeLocator((PNode) target);
            sourceLocator = new PNodeLocator((PNode) source);
        }
        targetPoint = targetLocator.locatePoint(targetPoint);
        sourcePoint = sourceLocator.locatePoint(sourcePoint);
        targetPoint = target.localToGlobal(targetPoint);
        sourcePoint = source.localToGlobal(sourcePoint);
        if (sourcePoint.equals(targetPoint)) {
            sourcePoint.setLocation(sourcePoint.getX(), sourcePoint.getY() + 1);
        }
        source.addPropertyChangeListener(this);
        target.addPropertyChangeListener(this);
        view.getEdgeLayer().addPropertyChangeListener(this);
        bend = new Bend(sourcePoint, targetPoint, this);
        setSourceEdgeEndType(this.intSourceEdgeEnd);
        sourceEdgeEnd.setPaint(this.sourceEdgeEndPaint);
        setTargetEdgeEndType(this.intTargetEdgeEnd);
        targetEdgeEnd.setPaint(this.targetEdgeEndPaint);
        sourceEdgeEnd.setStroke(null);
        targetEdgeEnd.setStroke(null);
        setPickable(true);
        drawPoints = bend.getDrawPoints();
        updateEdgeView();
    }

    /**
     * Returns the PLabel node which displays the label text
     * @return the label
     */
    public PLabel getLabel() {
        if (label == null) {
            label = new PLabel(null, this);
            label.updatePosition();
            label.setPickable(false);
            addChild(label);
        }
        return label;
    }

    public String toString() {
        if (source == null || target == null || source.getNode() == null || target.getNode() == null) {
            return ("edge = " + edge);
        }
        return (source.getNode() + "->" + target.getNode() + " (" + edge + ")");
    }

    /**
     * Returns the handler responsible for non-straight mutable edges
     */
    public PEdgeHandler getEdgeHandler() {
        return view.getEdgeHandler();
    }

    /**
     * Change the Source FNode
     */
    public void setSourceNode(PNodeView node_view) {
        source.removePropertyChangeListener(this);
        source = node_view;
        sourceLocator = new PNodeLocator(source);
        sourcePoint = sourceLocator.locatePoint(sourcePoint);
        sourcePoint = source.localToGlobal(sourcePoint);
        source.addPropertyChangeListener(this);
        updateEdgeView();
    }

    /**
     * Change the Target FNode
     */
    public void setTargetNode(PNodeView node_view) {
        target.removePropertyChangeListener(this);
        target = node_view;
        targetLocator = new PNodeLocator(target);
        targetPoint = targetLocator.locatePoint(targetPoint);
        targetPoint = target.localToGlobal(targetPoint);
        target.addPropertyChangeListener(this);
        updateEdgeView();
    }

    /**
     * @return the FEdge to which we are a view on
     */
    public FEdge getEdge() {
        return edge;
    }

    /**
     * @return the GraphView we are in
     */
    public GraphView getGraphView() {
        return view;
    }

    /**
     * @return the Bend used
     */
    public Bend getBend() {
        return bend;
    }

    public void clearBends() {
        bend.removeAllHandles();
    }

    /**
     * @param width
     *            set a new line width for this edge
     */
    public void setStrokeWidth(float width) {
        strokeWidth = width;
        setStroke(new BasicStroke(width));
    }

    /**
     * @return the currently set edge width
     */
    public float getsStrokeWidth() {
        return strokeWidth;
    }

    /**
     * @param line_type
     *            set a new line type for the edge
     */
    public void setLineType(int line_type) {
        lineType = line_type;
        updateEdgeView();
    }

    /**
     * @return the currently set edge line type
     */
    public int getLineType() {
        return lineType;
    }

    /**
     * This really refers to the <B>Stroke</B>, TODO: Make separte stroke
     * methods
     * 
     * @param paint
     *            the paint for this node
     */
    public void setUnselectedPaint(Paint paint) {
        unselectedPaint = paint;
        if (!selected) super.setStrokePaint(paint);
    }

    /**
     * This really refers to the <B>Stroke</B>, TODO: Make separte stroke
     * methods
     * 
     * @return the currently set edge Paint
     */
    public Paint getUnselectedPaint() {
        return unselectedPaint;
    }

    /**
     * This really refers to the <B>Stroke</B>, TODO: Make separte stroke
     * methods
     * 
     * @param paint
     *            the paint for this node
     */
    public void setSelectedPaint(Paint paint) {
        selectedPaint = paint;
        if (selected) super.setStrokePaint(paint);
    }

    /**
     * This really refers to the <B>Stroke</B>, TODO: Make separte stroke
     * methods
     * 
     * @return the currently set edge Selectionpaint
     */
    public Paint getSelectedPaint() {
        return selectedPaint;
    }

    /**
     * @return the currently set Source FEdge End Type
     */
    public Paint getSourceEdgeEndPaint() {
        return sourceEdgeEndPaint;
    }

    /**
     * @return the currently set Source FEdge End Type
     */
    public Paint getSourceEdgeEndSelectedPaint() {
        return sourceEdgeEndSelectedPaint;
    }

    /**
     * @return the currently set Target FEdge End Type
     */
    public Paint getTargetEdgeEndPaint() {
        return targetEdgeEndPaint;
    }

    /**
     * @return the currently set Target FEdge End Type
     */
    public Paint getTargetEdgeEndSelectedPaint() {
        return targetEdgeEndSelectedPaint;
    }

    /**
     * @param paint
     *            set the value for the source edge end when selected
     */
    public void setSourceEdgeEndSelectedPaint(Paint paint) {
        sourceEdgeEndSelectedPaint = paint;
        if (selected && sourceEdgeEnd != null) sourceEdgeEnd.setPaint(paint);
    }

    /**
     * @param paint
     *            the new paint for the stroke of the source eged end
     */
    public void setSourceEdgeEndStrokePaint(Paint paint) {
        sourceEdgeEnd.setStrokePaint(paint);
    }

    /**
     * @param paint
     *            set the value for the target edge end when selected
     */
    public void setTargetEdgeEndSelectedPaint(Paint paint) {
        targetEdgeEndSelectedPaint = paint;
        if (selected && targetEdgeEnd != null) targetEdgeEnd.setPaint(paint);
    }

    /**
     * @param paint
     *            the new paint for the stroke of the target eged end
     */
    public void setTargetEdgeEndStrokePaint(Paint paint) {
        targetEdgeEnd.setStrokePaint(paint);
    }

    /**
     * @param paint
     *            set the value for the source edge end
     */
    public void setSourceEdgeEndPaint(Paint paint) {
        sourceEdgeEndPaint = paint;
        if (!selected && sourceEdgeEnd != null) sourceEdgeEnd.setPaint(paint);
    }

    /**
     * @param paint
     *            set the value for the target edge end
     */
    public void setTargetEdgeEndPaint(Paint paint) {
        targetEdgeEndPaint = paint;
        if (!selected && targetEdgeEnd != null) targetEdgeEnd.setPaint(paint);
    }

    /**
     * Sets the Drawing style for the edge end.
     */
    public void setSourceEdgeEnd(int type) {
        setSourceEdgeEndType(type);
        updateEdgeView();
    }

    /**
     * Sets the Drawing style for the edge end.
     */
    public void setTargetEdgeEnd(int type) {
        setTargetEdgeEndType(type);
        updateEdgeView();
    }

    /**
     * When we are selected then we draw ourselves red, and draw any handles.
     */
    public boolean setSelected(boolean state) {
        if (state != selected) {
            selected = state;
            if (selected) {
                drawSelected();
                view.getEdgeSelectionHandler().select(this);
            } else {
                drawUnselected();
                view.getEdgeSelectionHandler().unselect(this);
            }
            updateEdgeView();
        }
        return state;
    }

    /**
     * @return selected state
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @return selcted state
     */
    public boolean getSelected() {
        return selected;
    }

    /**
     * This is the main method called to update the drawing of the edge.
     */
    public void updateEdgeView() {
        targetPoint = targetLocator.locatePoint(targetPoint);
        sourcePoint = sourceLocator.locatePoint(sourcePoint);
        targetPoint = target.localToGlobal(targetPoint);
        sourcePoint = source.localToGlobal(sourcePoint);
        if (selfEdge) {
            float width = (float) target.getWidth();
            float height = (float) target.getHeight();
            setPathToEllipse((float) (sourcePoint.getX() - .25 * width), (float) (sourcePoint.getY() - .25 * height), width * (float) .5, height * (float) .5);
            return;
        }
        updateTargetPointView();
        updateSourcePointView();
        FEdge[] same_edges = new FEdge[0];
        if (same_edges == null) same_edges = new FEdge[0];
        if (same_edges.length > 1) {
            System.out.println("Same Edges: " + same_edges.length);
            int count = 0;
            if (same_edges.length % 2 == 0) count++;
            if (count > 0) {
                double a = (sourcePoint.getX() + targetPoint.getX()) / 2;
                double b = (sourcePoint.getY() + targetPoint.getY()) / 2;
                double x;
                double y;
                boolean invertX = false;
                boolean invertY = false;
                if (true) {
                    x = sourcePoint.getX();
                    y = sourcePoint.getY();
                    if (y > targetPoint.getY()) {
                        invertX = true;
                    }
                    if (x < targetPoint.getX()) {
                        invertY = true;
                    }
                } else {
                    x = targetPoint.getX();
                    y = targetPoint.getY();
                    if (y > sourcePoint.getY()) {
                        invertX = true;
                    }
                    if (x < sourcePoint.getX()) {
                        invertY = true;
                    }
                }
                double H = Math.sqrt((x - a) * (x - a) + (y - b) * (y - b));
                double O = Math.abs(y - b);
                double theta = (Math.PI / 2) - Math.asin(O / H);
                double y_step = 10.0 * Math.sin(theta);
                double x_step = 10.0 * Math.cos(theta);
                if (invertX) {
                    x_step = -x_step;
                }
                if (invertY) {
                    y_step = -y_step;
                }
                int num = (count + 1) / 2;
                if (count % 2 == 0) {
                    num = -num;
                }
                x_step *= num;
                y_step *= num;
                a += x_step;
                b += y_step;
                bend.moveHandle_internal(0, new Point2D.Double(a, b));
            }
        }
        if (!inLargeGraph) {
            updateTargetArrow();
            updateSourceArrow();
        }
        updateLine();
    }

    /**
     * Draws the EdgeEnd, also sets the Source/Target Points to values such that
     * the edge does not "go through" the end
     */
    public void updateTargetArrow() {
        targetEdgeEnd.drawIcon(bend.getTargetHandlePoint(), targetPoint);
        targetPoint.setLocation(targetEdgeEnd.getNewX(), targetEdgeEnd.getNewY());
    }

    /**
     * Draws the EdgeEnd, also sets the Source/Target Points to values such that
     * the edge does not "go through" the end
     */
    public void updateSourceArrow() {
        sourceEdgeEnd.drawIcon(bend.getSourceHandlePoint(), sourcePoint);
        sourcePoint.setLocation(sourceEdgeEnd.getNewX(), sourceEdgeEnd.getNewY());
    }

    /**
     * Sets EdgeEnd for the given node, whether source or target
     */
    public void setThisEdgeEnd(PNodeView view, int type) {
        if (view == source) setSourceEdgeEnd(type); else if (view == target) setTargetEdgeEnd(type); else {
            setTargetEdgeEnd(type);
            setSourceEdgeEnd(type);
        }
    }

    /**
     * Return the Drawing style for the edge end.
     */
    public int getSourceEdgeEnd() {
        return intSourceEdgeEnd;
    }

    /**
     * REturn the Drawing style for the edge end.
     */
    public int getTargetEdgeEnd() {
        return intTargetEdgeEnd;
    }

    /**
     * Sets the Drawing style for the edge end.
     */
    protected void setSourceEdgeEndType(int type) {
        if (sourceEdgeEnd != null && indexOfChild(sourceEdgeEnd) != -1) removeChild(sourceEdgeEnd);
        sourceEdgeEnd = null;
        intSourceEdgeEnd = type;
        switch(type) {
            case 0:
                sourceEdgeEnd = new PNullIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                break;
            case 1:
                sourceEdgeEnd = new PDeltaIcon(bend.getSourceHandlePoint(), targetPoint, 8);
                sourceEdgeEnd.setPaint(Color.white);
                break;
            case 2:
                sourceEdgeEnd = new PDeltaIcon(bend.getSourceHandlePoint(), targetPoint, 8);
                sourceEdgeEnd.setPaint(Color.black);
                break;
            case 3:
                sourceEdgeEnd = new PDeltaIcon(bend.getSourceHandlePoint(), targetPoint, 8);
                sourceEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 4:
                sourceEdgeEnd = new PArrowIcon(bend.getSourceHandlePoint(), targetPoint, 8);
                sourceEdgeEnd.setPaint(Color.white);
                break;
            case 5:
                sourceEdgeEnd = new PArrowIcon(bend.getSourceHandlePoint(), targetPoint, 8);
                sourceEdgeEnd.setPaint(Color.black);
                break;
            case 6:
                sourceEdgeEnd = new PArrowIcon(bend.getSourceHandlePoint(), targetPoint, 8);
                sourceEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 7:
                sourceEdgeEnd = new PDiamondIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(Color.white);
                break;
            case 8:
                sourceEdgeEnd = new PDiamondIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(Color.black);
                break;
            case 9:
                sourceEdgeEnd = new PDiamondIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 10:
                sourceEdgeEnd = new PCircleIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(Color.white);
                break;
            case 11:
                sourceEdgeEnd = new PCircleIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(Color.black);
                break;
            case 12:
                sourceEdgeEnd = new PCircleIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 13:
                sourceEdgeEnd = new PTIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(Color.white);
                break;
            case 14:
                sourceEdgeEnd = new PTIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(Color.black);
                break;
            case 15:
                sourceEdgeEnd = new PTIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                sourceEdgeEnd.setPaint(getUnselectedPaint());
                break;
            default:
                sourceEdgeEnd = new PNullIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                break;
        }
        addChild(sourceEdgeEnd);
        sourcePoint.setLocation(sourceEdgeEnd.getNewX(), sourceEdgeEnd.getNewY());
    }

    /**
     * Sets the Drawing style for the edge end.
     */
    protected void setTargetEdgeEndType(int type) {
        if (targetEdgeEnd != null && indexOfChild(targetEdgeEnd) != -1) removeChild(targetEdgeEnd);
        targetEdgeEnd = null;
        intTargetEdgeEnd = type;
        switch(type) {
            case 0:
                targetEdgeEnd = new PNullIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                break;
            case 1:
                targetEdgeEnd = new PDeltaIcon(bend.getTargetHandlePoint(), sourcePoint, 8);
                targetEdgeEnd.setPaint(Color.white);
                break;
            case 2:
                targetEdgeEnd = new PDeltaIcon(bend.getTargetHandlePoint(), sourcePoint, 8);
                targetEdgeEnd.setPaint(Color.black);
                break;
            case 3:
                targetEdgeEnd = new PDeltaIcon(bend.getTargetHandlePoint(), sourcePoint, 8);
                targetEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 4:
                targetEdgeEnd = new PArrowIcon(bend.getTargetHandlePoint(), sourcePoint, 8);
                targetEdgeEnd.setPaint(Color.white);
                break;
            case 5:
                targetEdgeEnd = new PArrowIcon(bend.getTargetHandlePoint(), sourcePoint, 8);
                targetEdgeEnd.setPaint(Color.black);
                break;
            case 6:
                targetEdgeEnd = new PArrowIcon(bend.getTargetHandlePoint(), sourcePoint, 8);
                targetEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 7:
                targetEdgeEnd = new PDiamondIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(Color.white);
                break;
            case 8:
                targetEdgeEnd = new PDiamondIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(Color.black);
                break;
            case 9:
                targetEdgeEnd = new PDiamondIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 10:
                targetEdgeEnd = new PCircleIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(Color.white);
                break;
            case 11:
                targetEdgeEnd = new PCircleIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(Color.black);
                break;
            case 12:
                targetEdgeEnd = new PCircleIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(getUnselectedPaint());
                break;
            case 13:
                targetEdgeEnd = new PTIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(Color.white);
                break;
            case 14:
                targetEdgeEnd = new PTIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(Color.black);
                break;
            case 15:
                targetEdgeEnd = new PTIcon(bend.getTargetHandlePoint(), sourcePoint, 10);
                targetEdgeEnd.setPaint(getUnselectedPaint());
                break;
            default:
                targetEdgeEnd = new PNullIcon(bend.getSourceHandlePoint(), targetPoint, 10);
                break;
        }
        addChild(targetEdgeEnd);
        targetPoint.setLocation(targetEdgeEnd.getNewX(), targetEdgeEnd.getNewY());
    }

    /**
     * Update edge endpoint according to node boundary
     */
    protected static void updateEndPointView(Point2D bendEndPoint, int node_shape, Point2D endPoint, PNodeView endNodeView) {
        switch(node_shape) {
            case PNodeView.ELLIPSE:
                final double deltaX = endPoint.getX() - bendEndPoint.getX();
                final double deltaY = endPoint.getY() - bendEndPoint.getY();
                double nodeWidth = endNodeView.getWidth() / 2;
                double nodeHeight = endNodeView.getHeight() / 2;
                if (deltaX == 0) {
                    if (deltaY > 0) {
                        endPoint.setLocation(endPoint.getX(), endPoint.getY() - nodeHeight);
                    } else {
                        endPoint.setLocation(endPoint.getX(), endPoint.getY() + nodeHeight);
                    }
                } else {
                    double theta = Math.atan(deltaY / deltaX);
                    if (bendEndPoint.getX() < endPoint.getX()) {
                        theta += Math.PI;
                    }
                    endPoint.setLocation(endPoint.getX() + nodeWidth * Math.cos(theta), endPoint.getY() + nodeHeight * Math.sin(theta));
                }
                break;
            default:
                final double[] intersection = new double[2];
                final PathIterator border = ((PPath) endNodeView).getPathReference().getPathIterator(((PNode) endNodeView).getLocalToGlobalTransform(new PAffineTransform()));
                final double[] coords = new double[6];
                border.currentSegment(coords);
                double lastX = coords[0];
                double lastY = coords[1];
                border.next();
                while (!border.isDone()) {
                    border.currentSegment(coords);
                    final double currX = coords[0];
                    final double currY = coords[1];
                    if (segmentIntersection(intersection, bendEndPoint.getX(), bendEndPoint.getY(), endPoint.getX(), endPoint.getY(), lastX, lastY, currX, currY)) {
                        endPoint.setLocation(intersection[0], intersection[1]);
                        break;
                    } else {
                        lastX = currX;
                        lastY = currY;
                        border.next();
                    }
                }
                break;
        }
    }

    /**
     * Computes the intersection of the line segment from <code>(x1,y1)</code>
     * to <code>(x2,y2)</code> with the line segment from <code>(x3,y3)</code>
     * to <code>(x4,y4)</code>. If no intersection exists, returns
     * <code>false</code>. Otherwise returns <code>true</code>, and
     * <code>returnVal[0]</code> is set to be the X coordinate of the
     * intersection point and <code>returnVal[1]</code> is set to be the Y
     * coordinate of the intersection point. If more than one intersection point
     * exists, &quot;the intersection point&quot; is defined to be the
     * intersection point closest to <code>(x1,y1)</code>.
     * <p>
     * A note about overlapping line segments. Because of floating point
     * numbers' inability to be totally accurate, it is quite difficult to
     * represent overlapping line segments with floating point coordinates
     * without using an absolute-precision math package. Because of this, poorly
     * behaved outcome may result when computing the intersection of two
     * [nearly] overlapping line segments. The only way around this would be to
     * round intersection points to the nearest 32-bit floating point quantity.
     * But then dynamic range is greatly compromised.
     */
    public static boolean segmentIntersection(double[] returnVal, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        boolean s1reverse = false;
        if (y2 > y1) {
            s1reverse = !s1reverse;
            double temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (y4 > y3) {
            double temp = x3;
            x3 = x4;
            x4 = temp;
            temp = y3;
            y3 = y4;
            y4 = temp;
        }
        final double yMax = Math.min(y1, y3);
        final double yMin = Math.max(y2, y4);
        if (yMin > yMax) return false;
        if (y1 > yMax) {
            x1 = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
            y1 = yMax;
        }
        if (y3 > yMax) {
            x3 = x3 + (x4 - x3) * (yMax - y3) / (y4 - y3);
            y3 = yMax;
        }
        if (y2 < yMin) {
            x2 = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
            y2 = yMin;
        }
        if (y4 < yMin) {
            x4 = x3 + (x4 - x3) * (yMin - y3) / (y4 - y3);
            y4 = yMin;
        }
        if (yMin == yMax) {
            if (x2 < x1) {
                s1reverse = !s1reverse;
                double temp = x1;
                x1 = x2;
                x2 = temp;
                temp = y1;
                y1 = y2;
                y2 = temp;
            }
            if (x4 < x3) {
                double temp = x3;
                x3 = x4;
                x4 = temp;
                temp = y3;
                y3 = y4;
                y4 = temp;
            }
            final double xMin = Math.max(x1, x3);
            final double xMax = Math.min(x2, x4);
            if (xMin > xMax) return false; else {
                if (s1reverse) returnVal[0] = Math.max(xMin, xMax); else returnVal[0] = Math.min(xMin, xMax);
                returnVal[1] = yMin;
                return true;
            }
        }
        if ((x1 < x3 && x2 < x4) || (x3 < x1 && x4 < x2)) return false;
        if ((x1 == x3) && (x2 == x4)) {
            if (s1reverse) {
                returnVal[0] = x2;
                returnVal[1] = y2;
            } else {
                returnVal[0] = x1;
                returnVal[1] = y1;
            }
        }
        final double W = (x2 - x4) / ((x2 - x4) + (x3 - x1));
        returnVal[0] = x2 + W * (x1 - x2);
        returnVal[1] = yMin + W * (yMax - yMin);
        return true;
    }

    /**
     * Finds the exact point where the edge passes through the FNode surface
     */
    protected void updateTargetPointView() {
        if (!inLargeGraph) {
            Point2D bendTargetPoint = bend.getTargetHandlePoint();
            int node_shape = target.getShape();
            updateEndPointView(bendTargetPoint, node_shape, targetPoint, target);
        } else {
            targetPoint = targetLocator.locatePoint(targetPoint);
            targetPoint = target.localToGlobal(targetPoint);
            drawPoints[1] = targetPoint;
        }
    }

    /**
     * Finds the exact point where the edge passes through the FNode surface
     */
    protected void updateSourcePointView() {
        if (!inLargeGraph) {
            Point2D bendSourcePoint = bend.getSourceHandlePoint();
            int node_shape = source.getShape();
            updateEndPointView(bendSourcePoint, node_shape, sourcePoint, source);
        } else {
            sourcePoint = sourceLocator.locatePoint(sourcePoint);
            sourcePoint = source.localToGlobal(sourcePoint);
        }
    }

    /**
     * Draws the FEdge
     */
    public void updateLine() {
        if (!inLargeGraph) drawPoints = bend.getDrawPoints();
        setPathToPolyline(drawPoints);
    }

    /**
     * Listens for:<BR>
     * 1. Offset Messages from PNodeView <BR>
     * 2. BoundsChanged Messages from PNodeView <BR>
     * TODO: Listen for label changes?
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (getGraphView().updateEdges == false) return;
        if (evt.getPropertyName().equals("Offset")) {
            if (((PNodeView) evt.getNewValue()).node.equals(target.getNode())) {
                updateEdgeView();
            } else if (((PNodeView) evt.getNewValue()).node.getHandle().equals(source.getNode().getHandle())) {
                updateEdgeView();
            }
        } else if (evt.getPropertyName().equals("BoundsChanged")) {
            if (((PNodeView) evt.getNewValue()).node.equals(target.getNode())) {
                updateEdgeView();
            } else if (((PNodeView) evt.getNewValue()).node.equals(source.getNode())) {
                updateEdgeView();
            }
        } else if (evt.getPropertyName() == PNode.PROPERTY_TRANSFORM) {
            updateEdgeView();
        }
    }

    /**
     * A new (hacky) intersects.
     */
    public boolean intersects(Point2D pt) {
        double rectSize = 20.0;
        for (int i = 0; i < (drawPoints.length - 1); i++) {
            Rectangle2D rect = new Rectangle2D.Double(pt.getX() - (rectSize / 2), pt.getY() - (rectSize / 2), rectSize, rectSize);
            Line2D l = new Line2D.Double(drawPoints[i], drawPoints[i + 1]);
            if (l.intersects(rect)) return true;
        }
        return false;
    }

    protected void paint(PPaintContext paintContext) {
        super.paint(paintContext);
    }

    /**
     * Draws the edge as red and asks the bend to draw any handles.
     */
    public void drawSelected() {
        bend.drawSelected();
        setPaint(null);
        super.setStrokePaint(selectedPaint);
    }

    /**
     * Draws the edge as black.
     */
    public void drawUnselected() {
        bend.drawUnselected();
        setPaint(null);
        super.setStrokePaint(unselectedPaint);
    }

    /**
     * @see phoebe.PEdgeView0#addClientProperty(String, String ) setToolTip
     */
    public void setToolTip(String tip) {
        addClientProperty("tooltip", tip);
    }
}
