package com.bbn.vessel.author.graphEditor.views;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;
import com.bbn.vessel.author.models.Connection;
import com.bbn.vessel.author.models.GraphNode;
import com.bbn.vessel.author.models.Side;
import com.bbn.vessel.core.util.XMLHelper;

/**
 * <Enter the description of this type here>
 * 
 * @author RTomlinson
 */
public class SplineConnectionView extends ConnectionView {

    private class SplineAdder {

        private final AffineTransform transform = new AffineTransform();

        private final AffineTransform inverseTransform = new AffineTransform();

        private final Side toSide;

        private final Point2D fromPoint;

        private final Point2D toPoint;

        private final Rectangle2D fromBounds;

        private final Rectangle2D toBounds;

        private final double x1;

        private final double y1;

        private final double x2;

        private final double y2;

        private final double dx;

        private final double dy;

        private final double fromMaxX;

        private final double fromMaxY;

        private final double fromMinX;

        private final double fromMinY;

        private final double toMaxX;

        private final double toMaxY;

        private final double toMinX;

        private final double toMinY;

        private final boolean overlap;

        private final double ymax;

        private final double ymin;

        public SplineAdder(Point fromPoint, Side fromSide, GraphNode fromNode, Point toPoint, Side toSide, GraphNode toNode) {
            switch(fromSide) {
                case LEFT:
                    transform.quadrantRotate(2);
                    toSide = toSide.quadrantRotate(2);
                    fromSide = fromSide.quadrantRotate(2);
                    break;
                case TOP:
                    transform.quadrantRotate(1);
                    toSide = toSide.quadrantRotate(1);
                    fromSide = fromSide.quadrantRotate(1);
                    break;
                case BOTTOM:
                    transform.quadrantRotate(3);
                    toSide = toSide.quadrantRotate(3);
                    fromSide = fromSide.quadrantRotate(3);
                    break;
                default:
                    break;
            }
            if (toSide == Side.BOTTOM) {
                AffineTransform mirror = AffineTransform.getScaleInstance(1.0, -1.0);
                transform.preConcatenate(mirror);
                toSide = Side.TOP;
            }
            this.toSide = toSide;
            try {
                inverseTransform.setTransform(transform.createInverse());
            } catch (NoninvertibleTransformException e) {
            }
            this.fromPoint = transform.transform(fromPoint, null);
            this.toPoint = transform.transform(toPoint, null);
            x2 = this.toPoint.getX();
            x1 = this.fromPoint.getX();
            y2 = this.toPoint.getY();
            y1 = this.fromPoint.getY();
            dx = x2 - x1;
            dy = y2 - y1;
            toBounds = transformRectangle(toNode.getView().getBounds());
            fromBounds = transformRectangle(fromNode.getView().getBounds());
            fromMinY = fromBounds.getMinY();
            fromMinX = fromBounds.getMinX();
            fromMaxX = fromBounds.getMaxX();
            fromMaxY = fromBounds.getMaxY();
            toMaxY = toBounds.getMaxY();
            toMinY = toBounds.getMinY();
            toMaxX = toBounds.getMaxX();
            toMinX = toBounds.getMinX();
            overlap = toMaxY > fromMinY && toMinY < fromMaxY;
            ymax = max(toMaxY, fromMaxY) + SEP;
            ymin = min(toMinY, fromMinY) - SEP;
        }

        public void addPoints() {
            switch(toSide) {
                case RIGHT:
                    right2Right();
                    break;
                case LEFT:
                    right2Left();
                    break;
                case TOP:
                    right2Top();
                    break;
                case BOTTOM:
            }
        }

        /**
         * Add an S-shaped curve starting and ending horizontally and turning
         * vertical in the center.
         * 
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         */
        private void addXS(double x1, double y1, double x2, double y2) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            addPoint(new Point2D.Double(x1 + dx * 0.25, y1));
            addPoint(new Point2D.Double(x1 + dx * 0.50, y1));
            addPoint(new Point2D.Double(x1 + dx * 0.50, y1 + dy * 0.50));
            addPoint(new Point2D.Double(x1 + dx * 0.50, y1 + dy * 1.00));
            addPoint(new Point2D.Double(x1 + dx * 0.75, y1 + dy * 1.00));
            addPoint(new Point2D.Double(x1 + dx * 1.00, y1 + dy * 1.00));
        }

        /**
         * Add an U-shaped curve starting and ending horizontally and turning
         * vertical in the center.
         * 
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @param left
         *            the u points to the left
         */
        private void addXU(double x1, double y1, double x2, double y2, boolean left) {
            double xm = left ? min(x1, x2) - SEP * 2 : max(x1, x2) + SEP * 2;
            double ym = (y1 + y2) / 2;
            double f = 0.8;
            addPoint(new Point2D.Double(xm * f + x1 * (1 - f), y1));
            addPoint(new Point2D.Double(xm, y1 * f + ym * (1 - f)));
            addPoint(new Point2D.Double(xm, ym));
            addPoint(new Point2D.Double(xm, y2 * f + ym * (1 - f)));
            addPoint(new Point2D.Double(xm * f + x2 * (1 - f), y2));
            addPoint(new Point2D.Double(x2, y2));
        }

        /**
         * Add an U-shaped curve starting and ending horizontally and turning
         * vertical in the center.
         * 
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @param left
         *            the u points to the left
         */
        private void addXL(double x1, double y1, double x2, double y2) {
            double f = 0.8;
            addPoint(new Point2D.Double(x2 * f + x1 * (1 - f), y1));
            addPoint(new Point2D.Double(x2, y1 * f + y2 * (1 - f)));
            addPoint(new Point2D.Double(x2, y2));
        }

        /**
         * Add an U-shaped curve starting and ending horizontally and turning
         * vertical in the center.
         * 
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @param left
         *            the u points to the left
         */
        private void addYL(double x1, double y1, double x2, double y2) {
            double f = 0.8;
            addPoint(new Point2D.Double(x1, y2 * f + y1 * (1 - f)));
            addPoint(new Point2D.Double(x1 * f + x2 * (1 - f), y2));
            addPoint(new Point2D.Double(x2, y2));
        }

        /**
         * @param x2
         * @param x1
         * @param y2
         * @param y1
         * @param ymax
         * @param ymin
         * @param left
         */
        private void addXUU(double x2, double x1, double y2, double y1, double ymax, double ymin) {
            double dyu = ymax - min(y1, y2);
            double dyd = max(y1, y2) - ymin;
            double ym;
            if (dyu < dyd) {
                ym = ymax;
            } else {
                ym = ymin;
            }
            double xm = (x1 + x2) * 0.5;
            addXU(x1, y1, xm, ym, false);
            addXU(xm, ym, x2, y2, true);
        }

        /**
     * 
     */
        private void right2Right() {
            if (x2 > x1 && toMaxY + SEP > y1 && toMinY - SEP < y1) {
                double ym2;
                if (y2 > y1) {
                    ym2 = toMinY - SEP;
                } else {
                    ym2 = toMaxY + SEP;
                }
                double xm1 = (fromMaxX + toMinX) / 2.0;
                double ym1 = (y1 + ym2) / 2.0;
                double xm3 = x2 + SEP;
                double xm2 = (xm1 + xm3) / 2.0;
                double ym3 = (ym2 + y2) / 2.0;
                addXL(x1, y1, xm1, ym1);
                addYL(xm1, ym1, xm2, ym2);
                addXL(xm2, ym2, xm3, ym3);
                addYL(xm3, ym3, x2, y2);
            } else if (x2 < x1 && fromMaxY + SEP > y2 && fromMinY - SEP < y2) {
                double ym2;
                if (y1 > y2) {
                    ym2 = fromMinY - SEP;
                } else {
                    ym2 = fromMaxY + SEP;
                }
                double xm3 = (toMaxX + fromMinX) / 2.0;
                double ym3 = (y2 + ym2) / 2.0;
                double xm1 = x1 + SEP;
                double xm2 = (xm3 + xm1) / 2.0;
                double ym1 = (ym2 + y1) / 2.0;
                addXL(x1, y1, xm1, ym1);
                addYL(xm1, ym1, xm2, ym2);
                addXL(xm2, ym2, xm3, ym3);
                addYL(xm3, ym3, x2, y2);
            } else {
                addXU(x1, y1, x2, y2, false);
            }
        }

        /**
     * 
     */
        private void right2Top() {
            if (x2 > x1 + SEP) {
                if (y2 - SEP > y1) {
                    addXL(x1, y1, x2, y2);
                } else if (toMinX > x1 + 2 * SEP) {
                    double xm1 = (toMinX + x1) / 2;
                    double ym2 = y2 - SEP;
                    double ym1 = (2 * ym2 + y1) / 3.0;
                    double xm2 = (xm1 + x2) / 2.0;
                    addXL(x1, y1, xm1, ym1);
                    addYL(xm1, ym1, xm2, ym2);
                    addXL(xm2, y2 - SEP, x2, y2);
                } else if (x2 > x1) {
                    double ym = (fromMaxY + toMinY) * 0.5;
                    double xm = (fromMaxX + toMinX) * 0.5;
                    addXU(x1, y1, xm, ym, false);
                    addXU(xm, ym, toMinX, y2 - SEP, true);
                    addXL(x1, y2 - SEP, x2, y2);
                }
            } else if (y2 - SEP < fromMaxY + SEP) {
                double xm1 = Math.max(fromMaxX, toMaxX) + 2 * SEP;
                double ym1 = Math.min((y1 + y2) * 0.5, fromMinY);
                double xm2 = Math.max((x1 + x2) * 0.5, toMaxX);
                double ym2 = Math.min(fromMinY, toMinY) - 2 * SEP;
                addXL(x1, y1, xm1, ym1);
                addYL(xm1, ym1, xm2, ym2);
                addXL(xm2, ym2, x2, y2);
            } else {
                double ym1 = (2.0 * fromMinY + 1.0 * toMaxY) / 3.0;
                double ym2 = (ym1 + y2) / 2.0;
                double xm1 = fromMaxX + 2 * SEP;
                double xm2 = (xm1 + x2) / 2.0;
                addXL(x1, y1, xm1, ym1);
                addYL(xm1, ym1, xm2, ym2);
                addXL(xm2, ym2, x2, y2);
            }
        }

        /**
     * 
     */
        private void right2Left() {
            if (dx < 0 && overlap) {
                addXUU(x2, x1, y2, y1, ymax, ymin);
            } else if (dx > SEP) {
                addXS(x1, y1, x2, y2);
            } else {
                double ym1;
                double ym2;
                boolean uuCase;
                if (dy > 0) {
                    ym1 = fromMaxY + SEP;
                    ym2 = toMinY - SEP;
                    uuCase = ym1 < ym2;
                } else {
                    ym1 = fromMinY - SEP;
                    ym2 = toMaxY + SEP;
                    uuCase = ym1 > ym2;
                }
                if (uuCase) {
                    double ym = (ym1 + ym2) * 0.5;
                    double xm = (x1 + x2) * 0.5;
                    addXU(x1, y1, xm, ym, false);
                    addXU(xm, ym, x2, y2, true);
                } else {
                    double xm1 = x1 * 0.75 + x2 * 0.25;
                    double xm2 = x1 * 0.25 + x2 * 0.75;
                    addXU(x1, y1, xm1, ym1, false);
                    addXS(xm1, ym1, xm2, ym2);
                    addXU(xm2, ym2, x2, y2, true);
                }
            }
        }

        private void addPoint(Point2D p) {
            points.add(inverseTransform.transform(p, p));
        }

        /**
         * @param rect
         * @return transformed Rectangle
         */
        private Rectangle2D transformRectangle(Rectangle rect) {
            Point2D corner1 = new Point2D.Double(rect.getMinX(), rect.getMinY());
            Point2D corner2 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
            corner1 = transform.transform(corner1, corner1);
            corner2 = transform.transform(corner2, corner2);
            double minX = Math.min(corner1.getX(), corner2.getX());
            double maxX = Math.max(corner1.getX(), corner2.getX());
            double minY = Math.min(corner1.getY(), corner2.getY());
            double maxY = Math.max(corner1.getY(), corner2.getY());
            return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        }
    }

    private static Logger logger = Logger.getLogger(SplineConnectionView.class);

    private static final String ATTR_Y = "y";

    private static final String ATTR_X = "x";

    private static final String ATTR_INDEX = "index";

    private static final String TAG_POINT = "point";

    private static final String TAG_POINTS = "points";

    private static final double SEP = 10;

    private final List<Point2D> points = new ArrayList<Point2D>(0);

    private Shape basicShape = null;

    private Shape drawShape = null;

    private Shape selectedDrawShape = null;

    private Shape selectedSelectionShape = null;

    private boolean custom = false;

    /**
     * @param connection
     *            the Connection for which this is the View
     * @param graphViews
     */
    protected SplineConnectionView(Connection connection, GraphViews graphViews) {
        super(connection, graphViews);
    }

    /**
     * Used for temporary drawing of new connections
     * 
     * @param from
     *            the ConnectionEnd from which this is to be drawn
     * @param to
     *            the Connection to which this is to be drawn
     * @param graphViews
     */
    protected SplineConnectionView(ConnectionEnd from, ConnectionEnd to, GraphViews graphViews) {
        super(from, to, graphViews);
    }

    /**
     * Add intermediate points to control placement. The needed intermediate
     * points are a function of the sides of the terminals and the relative
     * position of the nodes. There are the following cases: The side faces away
     * from the other node: The other terminal is above (below): An intermediate
     * point is placed above (below) the node to draw the curve up far enough to
     * avoid the node. An intermediate point is placed on the same side as the
     * terminal to draw the curve away from the node.
     */
    private void addPoints() {
        if (logger.isDebugEnabled()) {
            logger.debug("addPoints");
        }
        flushShapeCache();
        ConnectionEnd from = getFrom();
        Side fromSide = Side.convertConstraint(from.getConstraint());
        Point fromPoint = from.getReferencePoint();
        points.clear();
        custom = false;
        points.add(new Point(fromPoint));
        ConnectionEnd to = getTo();
        Side toSide = Side.convertConstraint(to.getConstraint());
        Point toPoint = to.getReferencePoint();
        if (connection == null) {
            points.add(new Point(toPoint));
            return;
        }
        GraphNode fromNode = connection.getFrom().getGraphNode();
        GraphNode toNode = connection.getTo().getGraphNode();
        SplineAdder splineAdder = new SplineAdder(fromPoint, fromSide, fromNode, toPoint, toSide, toNode);
        splineAdder.addPoints();
    }

    /**
     * Override to flush additional cached shapes
     * 
     * @see com.bbn.vessel.author.graphEditor.views.ConnectionView#flushShapeCache()
     */
    @Override
    protected void flushShapeCache() {
        super.flushShapeCache();
        basicShape = null;
        drawShape = null;
        selectedDrawShape = null;
        selectedSelectionShape = null;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.ConnectionView#getSelectionShape(java.awt.Shape)
     */
    @Override
    protected Shape getSelectionShape() {
        if (isSelected()) {
            if (selectedSelectionShape == null) {
                Shape selectedDrawShape = getDrawShape();
                BasicStroke basicStroke = new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                selectedSelectionShape = basicStroke.createStrokedShape(selectedDrawShape);
            }
            return selectedSelectionShape;
        }
        return super.getSelectionShape();
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.AbstractView#getDraggable(Point)
     */
    @Override
    public Draggable getDraggable(Point viewPoint) {
        int ix = findControlPoint(viewPoint);
        if (ix < 0) {
            ix = insertControlPoint(viewPoint);
        }
        if (ix > 1 && ix < points.size() - 2) {
            return getDraggable(ix);
        }
        return null;
    }

    private Draggable getDraggable(final int pointIndex) {
        return new Draggable() {

            @Override
            public void drop(View nearestView) {
            }

            @Override
            public String getName() {
                return "drag";
            }

            @Override
            public void pickUp(Point pickupPoint) {
                if (logger.isDebugEnabled()) {
                    logger.debug("pickUp");
                }
            }

            @Override
            public void updateLocation(Point updatePoint, View bestView) {
                if (logger.isDebugEnabled()) {
                    logger.debug("updateLocation");
                }
                int wp = pointIndex % 3;
                switch(wp) {
                    case 0:
                        {
                            Point2D prevPoint = points.get(pointIndex - 1);
                            Point2D thisPoint = points.get(pointIndex);
                            Point2D nextPoint = points.get(pointIndex + 1);
                            double dx = updatePoint.getX() - thisPoint.getX();
                            double dy = updatePoint.getY() - thisPoint.getY();
                            movePoint(thisPoint, dx, dy);
                            movePoint(prevPoint, dx, dy);
                            movePoint(nextPoint, dx, dy);
                            break;
                        }
                    case 1:
                        {
                            Point2D prevPoint = points.get(pointIndex - 2);
                            Point2D thisPoint = points.get(pointIndex - 1);
                            Point2D nextPoint = points.get(pointIndex);
                            nextPoint.setLocation(updatePoint);
                            double y = thisPoint.getY();
                            double dy = nextPoint.getY() - y;
                            double x = thisPoint.getX();
                            double dx = nextPoint.getX() - x;
                            double angle = atan2(dy, dx);
                            double d = sqrt(dx * dx + dy * dy);
                            prevPoint.setLocation(x - d * cos(angle), y - d * sin(angle));
                            break;
                        }
                    case 2:
                        {
                            Point2D prevPoint = points.get(pointIndex);
                            Point2D thisPoint = points.get(pointIndex + 1);
                            Point2D nextPoint = points.get(pointIndex + 2);
                            prevPoint.setLocation(updatePoint);
                            double y = thisPoint.getY();
                            double dy = prevPoint.getY() - y;
                            double x = thisPoint.getX();
                            double dx = prevPoint.getX() - x;
                            double angle = atan2(dy, dx);
                            double d = sqrt(dx * dx + dy * dy);
                            nextPoint.setLocation(x - d * cos(angle), y - d * sin(angle));
                            break;
                        }
                }
                flushShapeCache();
                custom = true;
                graphViews.setModified(true);
                graphViews.repaint();
            }
        };
    }

    private int insertControlPoint(Point viewPoint) {
        return -1;
    }

    private int findControlPoint(Point viewPoint) {
        if (points.size() > 2) {
            for (int i = 1, n = points.size() - 1; i < n; i++) {
                Point2D point = points.get(i);
                if (point.distance(viewPoint) < 10) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @param point
     * @param dx
     * @param dy
     */
    protected void movePoint(Point2D point, double dx, double dy) {
        point.setLocation(point.getX() + dx, point.getY() + dy);
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.ConnectionView#getFillShape()
     */
    @Override
    public Shape getDrawShape() {
        if (isSelected()) {
            if (selectedDrawShape != null) {
                return selectedDrawShape;
            }
            GeneralPath path = new GeneralPath();
            path.append(super.getShape(), false);
            if (points.size() > 2) {
                for (int i = 1, n = points.size() - 1; i < n; i++) {
                    Point2D point = points.get(i);
                    double x = point.getX();
                    double y = point.getY();
                    path.moveTo(x + 3, y);
                    path.lineTo(x, y + 3);
                    path.lineTo(x - 3, y);
                    path.lineTo(x, y - 3);
                    path.closePath();
                }
                for (int i = 1, n = points.size() - 1; i < n; i += 1) {
                    Point2D point = points.get(i);
                    double x = point.getX();
                    double y = point.getY();
                    if (i == 1) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                selectedDrawShape = path;
                return selectedDrawShape;
            }
        }
        if (drawShape != null) {
            return drawShape;
        }
        drawShape = super.getShape();
        return drawShape;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.ConnectionView#getBasicShape()
     */
    @Override
    protected Shape getBasicShape() {
        synchronized (points) {
            String reason = null;
            if (reason == null && points.isEmpty()) {
                reason = "empty points";
            }
            if (reason == null && !points.get(0).equals(getFrom().getReferencePoint())) {
                reason = "from moved";
            }
            if (reason == null && !points.get(points.size() - 1).equals(getTo().getReferencePoint())) {
                reason = "to moved";
            }
            if (reason != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(reason);
                }
                addPoints();
                graphViews.repaint();
            }
            if (basicShape != null) {
                return basicShape;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Creating basic shape for " + System.identityHashCode(this));
            }
            if (points.size() == 2) {
                basicShape = new Line2D.Double(points.get(0), points.get(1));
                return basicShape;
            }
            GeneralPath path = new GeneralPath();
            Iterator<Point2D> iter = points.iterator();
            Point2D point = iter.next();
            path.moveTo(point.getX(), point.getY());
            while (iter.hasNext()) {
                Point2D[] points = { iter.next(), iter.next(), iter.next() };
                path.curveTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
            }
            basicShape = path;
            return basicShape;
        }
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.AbstractView#fromXML(org.jdom.Element)
     */
    @Override
    public void fromXML(Element viewElement) {
        points.clear();
        custom = false;
        Element pointsElement = viewElement.getChild(TAG_POINTS);
        if (pointsElement != null) {
            custom = true;
            List<Element> pointElements = XMLHelper.getChildren(pointsElement, TAG_POINT);
            Collections.sort(pointElements, new Comparator<Element>() {

                @Override
                public int compare(Element e1, Element e2) {
                    int index1 = getIntegerAttribute(e1, ATTR_INDEX);
                    int index2 = getIntegerAttribute(e2, ATTR_INDEX);
                    return index1 - index2;
                }
            });
            for (Element pointElement : pointElements) {
                double x = getDoubleAttribute(pointElement, ATTR_X);
                double y = getDoubleAttribute(pointElement, ATTR_Y);
                points.add(new Point2D.Double(x, y));
            }
        }
    }

    /**
     * @param element
     * @param attr
     * @return the integer value of the attribute
     */
    protected double getDoubleAttribute(Element element, String attr) {
        String attrString = element.getAttributeValue(attr);
        return Double.parseDouble(attrString);
    }

    /**
     * @param element
     * @param attr
     * @return the integer value of the attribute
     */
    protected int getIntegerAttribute(Element element, String attr) {
        String attrString = element.getAttributeValue(attr);
        return Integer.parseInt(attrString);
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.AbstractView#toXML()
     */
    @Override
    public Element toXML() {
        Element element = new Element(TAG_VIEW);
        if (custom) {
            Element pointsElement = new Element(TAG_POINTS);
            element.addContent(pointsElement);
            for (int i = 0, n = points.size(); i < n; i++) {
                Point2D point = points.get(i);
                Element pointElement = new Element(TAG_POINT);
                pointsElement.addContent(pointElement);
                pointElement.setAttribute(ATTR_INDEX, String.valueOf(i));
                pointElement.setAttribute(ATTR_X, String.valueOf(point.getX()));
                pointElement.setAttribute(ATTR_Y, String.valueOf(point.getY()));
            }
        }
        return element;
    }
}
