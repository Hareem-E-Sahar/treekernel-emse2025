package lab.graphics;

import lab.ui.UIConfig;
import acm.graphics.GMath;
import acm.graphics.GPoint;
import acm.graphics.GPolygon;
import java.util.Vector;

/**
 * Note: All paint methods inherited without need of overriding
 */
public class Obstacle extends GPolygon {

    private Vector<GPoint> vertices;

    private boolean hollow;

    private int index = -1;

    public Obstacle() {
        super();
    }

    /**
     * Create the obstacle from a set of points.
     * Same as parent constructor: Points are absolute, such as mouse clicks.
     * Same as parent constructor: Finalizes the polygon immediately.
     */
    public Obstacle(GPoint[] pts) {
        super(pts);
        vertices = new Vector<GPoint>(pts.length);
        for (int i = 0; i < pts.length; i++) vertices.add(pts[i]);
        super.setColor(UIConfig.OBSTACLE_BOUND_COLOR);
        super.setFillColor(UIConfig.OBSTACLE_FILL_COLOR);
        setHollow(false);
    }

    public void setIndex(int idx) {
        index = idx;
    }

    public int getIndex() {
        return index;
    }

    public void setHollow(boolean h) {
        hollow = h;
        setFilled(!h);
    }

    public boolean getHollow() {
        return hollow;
    }

    public void addVertex(GPoint pt) {
        super.addVertex(pt.getX(), pt.getY());
        vertices.add(pt);
    }

    public void addVertex(double x, double y) {
        addVertex(new GPoint(x, y));
    }

    /**
     * Calculates the geometric center of the obstacle polygon.
     * @return center in absolute coordinates
     */
    public GPoint getCenter() {
        GPoint first = vertices.get(0);
        double xMin = first.getX(), xMax = first.getX(), yMin = first.getY(), yMax = first.getY();
        for (int i = 1; i < vertices.size(); i++) {
            GPoint v = vertices.get(i);
            xMin = Math.min(xMin, v.getX());
            xMax = Math.max(xMax, v.getX());
            yMin = Math.min(yMin, v.getY());
            yMax = Math.max(yMax, v.getX());
        }
        double xc = (xMin + xMax) / 2, yc = (yMin + yMax) / 2;
        return new GPoint(xc, yc);
    }

    /**
     * Returns an array of lines representing the edges of the obstacle polygon.
     * @return array of edges
     */
    public Line[] getEdges() {
        Line[] edges = new Line[vertices.size()];
        for (int i = 0, j = 1, len = vertices.size(); i < len; i++, j++) edges[i] = new Line(vertices.get(i), vertices.get(j % len));
        return edges;
    }

    /**
     * Returns an array of vertices of the obstacle polygon.
     * @return GPoint array of vertices.
     */
    public GPoint[] getVertices() {
        return vertices.toArray(new GPoint[vertices.size()]);
    }

    /**
     * Scales the obstacle relative to the absolute origin.
     */
    public void scale(double sx, double sy) {
        double x0 = getX(), y0 = getY();
        for (int i = 0; i < vertices.size(); i++) {
            GPoint v = vertices.get(i);
            v.setLocation(x0 + sx * v.getX(), y0 + sy * v.getY());
        }
        super.scale(sx, sy);
    }

    /**
     * Rotates the obstacle about the absolute origin.
     */
    public void rotate(double theta) {
        double x0 = getX(), y0 = getY();
        double sinTheta = GMath.sinDegrees(theta), cosTheta = GMath.cosDegrees(theta);
        for (int i = 0; i < vertices.size(); i++) {
            GPoint v = vertices.get(i);
            v.setLocation(x0 + cosTheta * v.getX() + sinTheta * v.getY(), y0 + cosTheta * v.getY() - sinTheta * v.getX());
        }
        super.rotate(theta);
    }

    public boolean intersectsLine(Line l) {
        if (!hollow) {
            if (contains(l.getEndPoint().getX(), l.getEndPoint().getY()) || contains(l.getStartPoint().getX(), l.getStartPoint().getY())) return true;
            GPoint mdpt = l.getMidpoint();
            if (contains(mdpt.getX(), mdpt.getY())) return true;
        }
        for (int i = 0, j = 1, len = vertices.size(); i < len; i++, j++) {
            Line s = new Line(vertices.get(i), vertices.get(j % len));
            if (s.intersects(l)) return true;
        }
        return false;
    }

    /**
     * Returns every intersection this obstacle finds between any of its sides and line l
     */
    public GPoint[] getIntersectionsWith(Line l) {
        Vector<GPoint> xs = new Vector<GPoint>();
        for (int i = 0, j = 1, len = vertices.size(); i < len; i++, j++) {
            Line s = new Line(vertices.get(i), vertices.get(j % len));
            GPoint pt;
            if ((pt = s.getIntersectionWith(l)) != null) xs.add(pt);
        }
        return xs.toArray(new GPoint[xs.size()]);
    }

    public GPoint getVertexNearestTo(GPoint pt) {
        GPoint vNearest = null;
        double dMin = Double.MAX_VALUE;
        for (int i = 0; i < vertices.size(); i++) {
            GPoint v = vertices.get(i);
            double d = Math.sqrt(Math.pow(v.getX() - pt.getX(), 2) + Math.pow(v.getY() - pt.getY(), 2));
            if (d < dMin) {
                vNearest = v;
                dMin = d;
            }
        }
        return vNearest;
    }

    public Obstacle clone() {
        Obstacle clone = new Obstacle(vertices.toArray(new GPoint[vertices.size()]));
        clone.setHollow(hollow);
        return clone;
    }

    /**
     * Overrides GPolygon's <code>markAsComplete()</code> because it's useless here and
     * prevents us from using the <code>GPoint[]</code> constructor.
     * @deprecated
     */
    protected void markAsComplete() {
    }

    /**
     * We want to keep all origins at (0, 0), so we don't allow move
     * @deprecated
     */
    public void move(double dx, double dy) {
    }

    /**
     * Keep everything relative to absolute origin!
     * @deprecated
     */
    public void setLocation(double x, double y) {
    }

    /**
     * @deprecated
     */
    public void recenter() {
    }

    /**
     * @deprecated
     */
    public void addEdge(double dx, double dy) {
    }

    /**
     * @deprecated
     */
    public void addArc(double arcWidth, double arcHeight, double start, double sweep) {
    }
}
