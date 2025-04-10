package ds.z;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import ds.z.exception.PointsAlreadyConnectedException;
import io.z.EdgeConverter;
import io.z.XMLConverter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XMLConverter(EdgeConverter.class)
public class Edge implements Serializable {

    /**
	 * The {@code LineIntersectionType} enumeration defines the type of
	 * intersection of two line segments.
	 */
    public enum LineIntersectionType {

        /** Two edges lie one upon the other with exactly the same coordinates. */
        Superposed, /** Two edges that lie (not neccessary completly) one on the other.*/
        Colinear, /** Two line segments that have the same start and end point respectively. */
        Connected, /** Two line segments that have a real intersection. */
        Intersects, /** The end point of one line segment lies on the other line segment. */
        IntersectsBorder, /** Two line segments do not intersect themselves in any manner. */
        NotIntersects
    }

    /** The associated polygon of this edge */
    private PlanPolygon associatedPolygon;

    /** The start point of this edge */
    @XStreamOmitField()
    private PlanPoint target;

    /** The end point of this edge */
    @XStreamOmitField()
    private PlanPoint source;

    /**
	 * Creates a new instance of {@code Edge} with two different ending points 
	 * and no associated polygon. The edge will try to defineByPoints itself to the ending
	 * points' list of incident edges.
	 *
	 * @param newSource one ending point
	 * @param newTarget the other ending point
	 */
    Edge(PlanPoint newSource, PlanPoint newTarget) {
        source = newSource;
        target = newTarget;
    }

    /**
	 * Creates a new instance of {@code Edge} with two different ending points.
	 * The edge will try to defineByPoints itself to the ending points' list of incident edges.
	 *
	 * @param newSource one ending point
	 * @param newTarget the other ending point
	 * @param p the polygon to which this edge is associated
	 */
    public Edge(PlanPoint newSource, PlanPoint newTarget, PlanPolygon p) {
        this(newSource, newTarget);
        setAssociatedPolygon(p);
    }

    /** @param e An arbitrary edge
	 * @return Whether the given edge e is a neighbour of this edge in this edge's polygon. The method
	 * checks for instance identity, so e must be the real neighbour object of this edge, not only a
	 * copy of the neightbour object, to let this method produce the output "true". */
    public boolean isNeighbour(Edge e) {
        return (source.getPreviousEdge() == e) || (target.getNextEdge() == e);
    }

    /** @param e A neighbour of this edge in this edge's polygon (may not be a copy of a neighbour)
	 * @return The other neighbour (not e) of this edge or null if e is the only neighbour
	 * @exception IllegalArgumentException If e is no neighbour of this edge.
	 */
    public Edge getOtherNeighbour(Edge e) throws IllegalArgumentException {
        if (source.getPreviousEdge() == e) return target.getNextEdge(); else if (target.getNextEdge() == e) return source.getPreviousEdge(); else throw new IllegalArgumentException(ZLocalization.getSingleton().getString("ds.z.EdgeException"));
    }

    /**
	 * Returns the leftmost coordinate value of this edge.
	 * @return the right bound
	 */
    public int boundLeft() {
        return (int) Math.min(source.getX(), target.getX());
    }

    /**
	 * Returns the lowermost coordinate value of this edge.
	 * @return the lower bound
	 */
    public int boundLower() {
        return (int) Math.max(source.getY(), target.getY());
    }

    /**
	 * Returns the rightmost coordinate value of this edge.
	 * @return the right bound
	 */
    public int boundRight() {
        return (int) Math.max(source.getX(), target.getX());
    }

    /**
	 * Returns the uppermost coordinate value of this edge.
	 * @return the upper bound
	 */
    public int boundUpper() {
        return (int) Math.min(source.getY(), target.getY());
    }

    /**
	 * Determines the common point of the {@code Edge} with another specified edge.
	 * @param e the edge
	 * @throws java.lang.IllegalArgumentException if the edges have no point in common
	 * @return the common point
	 */
    public PlanPoint commonPoint(Edge e) throws IllegalArgumentException {
        return commonPoint(this, e);
    }

    /**
	 * Determines the common point of two specified edges that fit together.
	 * @param e1 one edge
	 * @param e2 the other edge
	 * @return the common point
	 * @throws java.lang.IllegalArgumentException if the edges have no point in common
	 * @see #fits(Edge)
	 */
    public static PlanPoint commonPoint(Edge e1, Edge e2) throws IllegalArgumentException {
        if (e1.getSource().equals(e2.getSource())) return e1.getSource(); else if (e1.getSource().equals(e2.getTarget())) return e1.getSource(); else if (e1.getTarget().equals(e2.getSource())) return e1.getTarget(); else if (e1.getTarget().equals(e2.getTarget())) return e1.getTarget(); else throw new java.lang.IllegalArgumentException(ZLocalization.getSingleton().getString("ds.z.NoCommonPointException"));
    }

    /**
	 * Deletes this {@code Edge}. That means, the edge removes itself out of
	 * the list of edges in the associated polygon. After that all used references are
	 * setLocation to {@code null}.
	 * @throws java.lang.IllegalArgumentException sent from super-class, not supposed to occur
	 * @throws java.lang.IllegalStateException if the edge is not the first or last 
	 * edge in the polygon. first and last edges also occur in closed polygons.
	 */
    public void delete() throws IllegalArgumentException, IllegalStateException {
        associatedPolygon.removeEdge(this);
        associatedPolygon = null;
        target.setPreviousEdge(null);
        source.setNextEdge(null);
        target = null;
        source = null;
    }

    /**
	 * Tests if this edge is equal to any object. If the object is not an instance
	 * of edge, the two objects are considered as unequal. Two edges are equal if
	 * they have the same end points. It is not neccessary that the same points match.
	 * @param obj the object that is tested to be equal with this edge
	 * @return true if the points of e and this instance are the same
	 */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge e = (Edge) obj;
            final boolean val1 = e.getSource().equals(this.getSource()) && e.getTarget().equals(this.getTarget());
            final boolean val2 = e.getSource().equals(this.getTarget()) && e.getTarget().equals(this.getSource());
            return val1 | val2;
        } else return false;
    }

    /**
	 * Checks if a specified {@link PlanPoint} fits to the {@code Edge}. That
	 * means that the point has the same coordinates than one of the end points
	 * of the Edge.
	 * @param p the point
	 * @return true if the point is one of the end points
	 */
    public boolean fits(PlanPoint p) {
        return fits(p, this);
    }

    /**
	 * Checks if a specified {@link PlanPoint} fits a specified {@code Edge}.
	 * That means that the point has the same coordinates than one of the end points
	 * of the Edge.
	 * @param p the point
	 * @param e the edge
	 * @return true if the point is one of the end points of the edge
	 */
    public static boolean fits(PlanPoint p, Edge e) {
        return p.equals(e.getSource()) | p.equals(e.getTarget());
    }

    /**
	 * Checks if a specified {@code Edge} fits to the edge. That means that
	 * the edges have ending points with the same coordinates.
	 * @param e the edge
	 * @return true if the edges have an ending point in common
	 */
    public boolean fits(Edge e) {
        return fits(e.getSource()) | fits(e.getTarget());
    }

    /**
	 * Checks whether an {@link Edge} fits into another {@code Edge<&/code>. An
	 * edge is said to fit, if the coordinates of the edges are the same. That
	 * means that the edge are superposed.
	 * @param e1 the first edge
	 * @param e2 the second edge
	 * @return true if the edges fit together
	 */
    public static boolean fitsTogether(Edge e1, Edge e2) {
        return e1.fits(e2.getSource()) && e1.fits(e2.getTarget());
    }

    /**
	 * Returns the {@link PlanPolygon} that is associated to the{@code Edge}.
	 * A polygon is associated if it contains the edge.
	 * <p>Every edge must have exactly one associated polygon.</p>
	 * @return the associated polygon.
	 */
    public PlanPolygon getAssociatedPolygon() {
        return associatedPolygon;
    }

    /**
	 * Returns the end point of the {@code Edge} that is not the specified
	 * point.
	 * @param p the one end point (or equal to the end point)
	 * @return the other end point
	 * @throws java.lang.IllegalArgumentException
	 */
    public PlanPoint getOther(PlanPoint p) throws IllegalArgumentException {
        if (p == source || p.equals(source)) return getTarget();
        if (p == target || p.equals(target)) return getSource();
        throw new IllegalArgumentException(ZLocalization.getSingleton().getString("ds.z.NoCoindidesException"));
    }

    /**
	 * If target or source of the Edge equal the given point, then target or source are returned,
	 * otherwise null is returned.
	 */
    public PlanPoint getPoint(PlanPoint p) {
        return (target == p || (target != null && target.equals(p))) ? target : (source == p || (source != null && source.equals(p))) ? source : null;
    }

    /**
	 * Returns one of the bounding points of this edge.
	 * @return one bounding point as {@link PlanPoint}
	 */
    public PlanPoint getSource() {
        return source;
    }

    /**
	 * Returns one of the bounding points of this edge.
	 * @return one bounding point as {@link PlanPoint}
	 */
    public PlanPoint getTarget() {
        return target;
    }

    public int getMinX() {
        int mine1 = Math.min(getSource().getXInt(), getTarget().getXInt());
        return mine1;
    }

    public int getMinY() {
        int mine1 = Math.min(getSource().getYInt(), getTarget().getYInt());
        return mine1;
    }

    public int getMaxX() {
        int maxe1 = Math.max(getSource().getXInt(), getTarget().getXInt());
        return maxe1;
    }

    public int getMaxY() {
        int maxe1 = Math.max(getSource().getYInt(), getTarget().getYInt());
        return maxe1;
    }

    /**
	 * Tests whether two edges intersect each other, or not. The returned value is an
	 * {@link ds.z.Edge.LineIntersectionType} object, which indicates what type the
	 * intersection was, if any.
	 * @param e1 one of the edges
	 * @param e2 the other edge
	 * @return the intersection type
	 * @see ds.z.Edge.LineIntersectionType
	 */
    public static LineIntersectionType intersects(Edge e1, Edge e2) {
        if (e1.fits(e2)) if (fitsTogether(e1, e2)) return LineIntersectionType.Superposed; else return LineIntersectionType.Connected;
        int mine1 = Math.min(e1.getSource().getXInt(), e1.getTarget().getXInt());
        int maxe1 = Math.max(e1.getSource().getXInt(), e1.getTarget().getXInt());
        int mine2 = Math.min(e2.getSource().getXInt(), e2.getTarget().getXInt());
        int maxe2 = Math.max(e2.getSource().getXInt(), e2.getTarget().getXInt());
        if (mine1 > maxe2 | mine2 > maxe1) return LineIntersectionType.NotIntersects;
        mine1 = Math.min(e1.getSource().getYInt(), e1.getTarget().getYInt());
        maxe1 = Math.max(e1.getSource().getYInt(), e1.getTarget().getYInt());
        mine2 = Math.min(e2.getSource().getYInt(), e2.getTarget().getYInt());
        maxe2 = Math.max(e2.getSource().getYInt(), e2.getTarget().getYInt());
        if (mine1 > maxe2 | mine2 > maxe1) return LineIntersectionType.NotIntersects;
        int t1 = PlanPoint.orientation(e1.getSource(), e1.getTarget(), e2.getSource());
        int t2 = PlanPoint.orientation(e1.getSource(), e1.getTarget(), e2.getTarget());
        if (t1 == 0 & t2 == 0) return LineIntersectionType.Colinear;
        int ret1 = t1 * t2;
        t1 = PlanPoint.orientation(e2.getSource(), e2.getTarget(), e1.getSource());
        t2 = PlanPoint.orientation(e2.getSource(), e2.getTarget(), e1.getTarget());
        if (t1 == 0 & t2 == 0) return LineIntersectionType.Colinear;
        int ret2 = t1 * t2;
        if (ret1 < 0 & ret2 < 0) return LineIntersectionType.Intersects; else if (ret1 == 0 && ret2 == 0) throw new IllegalStateException("Two line segments are colinear but this should have been returned earlier."); else if (ret1 == 0 && ret2 == 1 || ret1 == 1 && ret2 == 0) return LineIntersectionType.NotIntersects; else if (ret1 == 0 && ret2 == -1 || ret1 == -1 && ret2 == 0) return LineIntersectionType.IntersectsBorder; else return LineIntersectionType.NotIntersects;
    }

    /**
	 * Checks whether the edge is horizontally aligned. The edge is considered
	 * horizontal if the {@code y}-coordinates of the two end points are equal.
	 * @return true if the edge is horizontal
	 */
    public boolean isHorizontal() {
        if (source.y == target.y) return true; else return false;
    }

    /**
	 * Checks whether the edge is vertically aligned. The edge is considered
	 * vertical if the {@code x}-coordinates of the two end points are equal.
	 * @return true if the edge is vertical
	 */
    public boolean isVertical() {
        if (source.x == target.x) return true; else return false;
    }

    /** Sets a new associated polygon. This should only be invoked when the edge
	 * is created. An edge should typically keep its polygon during its entire
	 * lifetime.
	 *
	 * @throws java.lang.NullPointerException if the passed {@code PlanPolygon} is null.
	 * @throws IllegalArgumentException if this edge cannot be added to the new polygon 
	 * because it does not fit to its start- and end points.
	 * @throws IllegalStateException if the edge is part of a polygon with more than one
	 * @param polygon the new polygon
	 */
    protected void setAssociatedPolygon(PlanPolygon polygon) throws NullPointerException, IllegalArgumentException, IllegalStateException {
        if (polygon == null) throw new NullPointerException(ZLocalization.getSingleton().getString("ds.z.PolygonIsNullException"));
        if (polygon.fits(this)) {
            if (associatedPolygon != null) associatedPolygon.removeEdge(this);
            polygon.addEdge(this);
            associatedPolygon = polygon;
        } else throw new IllegalArgumentException(ZLocalization.getSingleton().getString("ds.z.CanNotConnectException"));
    }

    /**
	 * Sets one specified end point of the edge to a new one.
	 *
	 * ONLY CALL THIS METHOD WHEN AWARE OF THE VALIDITY CONSTRAINTS FOR PLANPOLYGONS / ROOMS
	 *
	 * @param p the end point
	 * @param newPoint the new point
	 * @param overwrite Specify 'true' when you want the method to ignore the
	 * case that newSource/newTarget already have next/previous edges. If you specify 'false'
	 * an exception is thrown when the above desrcibed case happens.
	 * @throws PointsAlreadyConnectedException if the points are already connected to other 
	 * edges and overwrite is 'false'
	 * @throws IllegalArgumentException If p is not on the edge
	 */
    protected void setPoint(PlanPoint p, PlanPoint newPoint, boolean overwrite) throws PointsAlreadyConnectedException {
        if (source.equals(p)) setSource(newPoint, overwrite); else if (target.equals(p)) setTarget(newPoint, overwrite); else throw new IllegalArgumentException(ZLocalization.getSingleton().getString("ds.z.PlanPolygon.PointNotContainedInEdgeException"));
    }

    /**
	 * Sets the first point to a new position, it has to be different from the second point.
	 * @param newSource the new point
	 * @param overwrite Specify 'true' when you want the method to ignore the
	 * case that newSource already has a next edge. If you specify 'false'
	 * an exception is thrown when the above desrcibed case happens.
	 * @throws PointsAlreadyConnectedException if the points are already connected to other 
	 * edges and overwrite is 'false'
	 */
    protected void setSource(PlanPoint newSource, boolean overwrite) throws PointsAlreadyConnectedException {
        if (!overwrite && newSource != source && newSource.getNextEdge() != null) throw new PointsAlreadyConnectedException(newSource, ZLocalization.getSingleton().getString("ds.z.SourceAlreadyConnectedException"));
        if (source != null) {
            source.setNextEdge(null);
        }
        source = newSource;
        newSource.setNextEdge(this);
    }

    /**
	 * Sets the second point to a new position, it has to be different from the first point.
	 *
	 * ONLY CALL THIS METHOD WHEN AWARE OF THE VALIDITY CONSTRAINTS FOR PLANPOLYGONS / ROOMS
	 *
	 * @param newTarget the new point
	 * @param overwrite Specify 'true' when you want the method to ignore the
	 * case that newTarget already has a previous edge. If you specify 'false'
	 * an exception is thrown when the above desrcibed case happens.
	 * @throws PointsAlreadyConnectedException if the points are already connected to other 
	 * edges and overwrite is 'false'
	 */
    protected void setTarget(PlanPoint newTarget, boolean overwrite) throws PointsAlreadyConnectedException {
        if (!overwrite && newTarget != target && newTarget.getPreviousEdge() != null) throw new PointsAlreadyConnectedException(newTarget, ZLocalization.getSingleton().getString("ds.z.TargetAlreadyConnectedException"));
        if (target != null) {
            target.setPreviousEdge(null);
        }
        target = newTarget;
        newTarget.setPreviousEdge(this);
    }

    /**
	 * Sets both points at the same time. They have to be different.
	 *
	 * ONLY CALL THIS METHOD WHEN AWARE OF THE VALIDITY CONSTRAINTS FOR PLANPOLYGONS / ROOMS
	 *
	 * @param newSource one new point
	 * @param newTarget the other new point
	 * @param overwrite Specify 'true' when you want the method to ignore the
	 * case that newSource/newTarget already have next/previous edges. If you specify 'false'
	 * an exception is thrown when the above desrcibed case happens.
	 * @throws PointsAlreadyConnectedException if the points are already connected to other 
	 * edges and overwrite is 'false'
	 */
    protected void setPoints(PlanPoint newSource, PlanPoint newTarget, boolean overwrite) throws IllegalArgumentException, PointsAlreadyConnectedException {
        setSource(newSource, overwrite);
        setTarget(newTarget, overwrite);
    }

    /**
	 * Returns a string representation of the {@code Edge} that
	 * textually represents the edge.
	 * <p>An edge is represented as a tupel of its end points and will look
	 * as follows:
	 * </p>
	 * <blockquote>
	 * <pre>
	 * [(x1,y1)(x2,y2)]
	 * </pre></blockquote>
	 * <p>
	 * Here, (x1,y1) represents a point.
	 * </p>
	 * @return a string representation of the edge
	 */
    @Override
    public String toString() {
        return "[" + source.toString() + "," + target.toString() + "]";
    }

    /**
	 * Returns the length of the edge (in millimeters).
	 * @return The length of the edge (in millimeters).
	 */
    public int length() {
        return length(getSource(), getTarget());
    }

    /**
	 * Calculates the length of the line segment between two specified points with
	 * the euclidean norm. The length is rounded to millimeter.
	 * @param p1 one point
	 * @param p2 the other point
	 * @return the rounded length
	 * @see ds.z.PlanPoint
	 */
    public static int length(PlanPoint p1, PlanPoint p2) {
        return p1.equals(p2) ? 0 : (int) Math.round(PlanPoint.distance(p1.x, p1.y, p2.x, p2.y));
    }

    /** This is a convenience method that returns both PlanPoints of this Edge.
	 */
    public List<PlanPoint> getPlanPoints() {
        ArrayList<PlanPoint> planPoints = new ArrayList<PlanPoint>(2);
        getPlanPoints(planPoints);
        return planPoints;
    }

    /** This is a convenience method that adds both PlanPoints of this Edge to
	 * the given list.
	 *
	 * @param planPoints The method will defineByPoints all PlanPoints to the parameter list.
	 */
    public void getPlanPoints(List<PlanPoint> planPoints) {
        planPoints.add(source);
        planPoints.add(target);
    }

    /**
	 * @param p An arbitrary point
	 * @return A new PlanPoint, that lies on this Edge and that is close to p.
	 */
    public PlanPoint getPointOnEdge(PlanPoint p) {
        int width = boundRight() - boundLeft();
        int height = boundLower() - boundUpper();
        PlanPoint pLeft = (source.x <= target.x) ? source : target;
        PlanPoint pRight = (source.x <= target.x) ? target : source;
        PlanPoint pTop = (source.y <= target.y) ? source : target;
        PlanPoint pBottom = (source.y <= target.y) ? target : source;
        if (width > height) {
            double ascension = (double) (pRight.y - pLeft.y) / (double) (pRight.x - pLeft.x);
            int offset = pLeft.y;
            return new PlanPoint(p.x, (int) (ascension * (p.x - pLeft.x)) + offset);
        } else {
            double ascension = (double) (pTop.x - pBottom.x) / (double) (pTop.y - pBottom.y);
            int offset = pBottom.x;
            return new PlanPoint((int) (ascension * (p.y - pBottom.y)) + offset, p.y);
        }
    }

    /** Splits the edge in two parts. Each of the edges will have identic fields and the
	 * Point p will be the end of the first and the start of teh second edge, e.g. split them.
	 * @param p The splitting point (Is not modified in here)
	 */
    public void splitEdge(PlanPoint p) {
    }
}
