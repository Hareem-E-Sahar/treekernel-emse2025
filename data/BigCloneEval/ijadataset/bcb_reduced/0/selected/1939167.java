package org.dyn4j.geometry.hull;

import java.util.Arrays;
import java.util.Comparator;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.resources.Messages;

/**
 * Implementation of the Divide and Conquer convex hull algorithm.
 * <p>
 * This implementation is not sensitive to colinear points and returns only
 * the points of the convex hull.
 * <p>
 * This algorithm is O(n log n) where n is the number of points.
 * <p>
 * If the input point array has a size of 1 or 2 the input point array is returned.
 * @author William Bittle
 * @version 2.2.3
 * @since 2.2.0
 */
public class DivideAndConquer implements HullGenerator {

    /**
	 * Represents a vertex on a Hull.
	 * @author William Bittle
	 * @version 2.2.0
	 * @since 2.2.0
	 */
    private class Vertex {

        /** The vertex point */
        public Vector2 point;

        /** The next vertex */
        public Vertex next;

        /** The previous vertex */
        public Vertex prev;
    }

    /**
	 * Represents a convex hull with CCW winding.
	 * @author William Bittle
	 * @version 2.2.0
	 * @since 2.2.0;
	 */
    private class Hull {

        /** The root vertex (this can be any vertex on the hull) */
        public Vertex root;

        /** The vertex that has the smallest x coordinate */
        public Vertex leftMost;

        /** The vertex that has the largest x coordinate */
        public Vertex rightMost;

        /** The total number of vertices on the hull */
        public int size;
    }

    /**
	 * Represents a comparator that sorts points by their x coordinate
	 * lowest to highest.
	 * @author William Bittle
	 * @version 2.2.0
	 * @since 2.2.0
	 */
    private class PointComparator implements Comparator<Vector2> {

        @Override
        public int compare(Vector2 p1, Vector2 p2) {
            return (int) Math.signum(p1.x - p2.x);
        }
    }

    @Override
    public Vector2[] generate(Vector2... points) {
        if (points == null) throw new NullPointerException(Messages.getString("geometry.hull.nullArray"));
        int size = points.length;
        if (size == 1 || size == 2) return points;
        try {
            Arrays.sort(points, new PointComparator());
        } catch (NullPointerException e) {
            throw new NullPointerException(Messages.getString("geometry.hull.nullPoints"));
        }
        Hull hull = divide(points, 0, size - 1);
        Vector2[] hullPoints = new Vector2[hull.size];
        Vertex v = hull.root;
        for (int i = 0; i < hull.size; i++) {
            hullPoints[i] = v.point;
            v = v.next;
        }
        return hullPoints;
    }

    /**
	 * Recursive method to subdivide and merge the points.
	 * @param points the array of points
	 * @param first the first index
	 * @param last the last index
	 * @return {@link Hull} the convex hull created
	 */
    private Hull divide(Vector2[] points, int first, int last) {
        int size = last - first;
        if (size == 0) {
            Vertex vertex = new Vertex();
            vertex.point = points[first];
            vertex.next = null;
            vertex.prev = null;
            Hull hull = new Hull();
            hull.root = vertex;
            hull.leftMost = vertex;
            hull.rightMost = vertex;
            hull.size = 1;
            return hull;
        } else {
            int mid = (first + last) / 2;
            Hull left = divide(points, first, mid);
            Hull right = divide(points, mid + 1, last);
            return merge(left, right);
        }
    }

    /**
	 * Merges the two given convex {@link Hull}s into one convex {@link Hull}.
	 * <p>
	 * The left {@link Hull} should contain only points whose x coordinates are
	 * less than all the points in the right {@link Hull}.
	 * @param left the left convex {@link Hull}
	 * @param right the right convex {@link Hull}
	 * @return {@link Hull} the merged convex hull
	 */
    private Hull merge(Hull left, Hull right) {
        if (left.size == 1 && right.size == 1) {
            Vertex leftRoot = left.root;
            Vertex rightRoot = right.root;
            leftRoot.next = rightRoot;
            leftRoot.prev = rightRoot;
            rightRoot.next = leftRoot;
            rightRoot.prev = leftRoot;
            Hull hull = new Hull();
            hull.root = leftRoot;
            hull.leftMost = leftRoot;
            hull.rightMost = rightRoot;
            hull.size = 2;
            return hull;
        } else if (left.size == 1 && right.size == 2) {
            Hull hull = new Hull();
            hull.leftMost = left.root;
            hull.rightMost = right.rightMost;
            hull.size = 3;
            this.mergeTriangle(left, right, hull);
            return hull;
        } else if (left.size == 2 && right.size == 1) {
            Hull hull = new Hull();
            hull.leftMost = left.leftMost;
            hull.rightMost = right.root;
            hull.size = 3;
            this.mergeTriangle(right, left, hull);
            return hull;
        } else {
            Hull hull = new Hull();
            hull.leftMost = left.leftMost;
            hull.rightMost = right.rightMost;
            Vertex lu = left.rightMost;
            Vertex ru = right.leftMost;
            Vector2 upper = lu.point.to(ru.point);
            for (int i = 0; i < left.size * right.size; i++) {
                Vector2 lv = lu.point.to(lu.next.point);
                Vector2 rv = ru.point.to(ru.prev.point);
                double crossR = rv.cross(upper);
                double crossL = upper.getNegative().cross(lv);
                if (crossR > 0.0 && crossL > 0.0) {
                    break;
                }
                if (crossR <= 0.0) {
                    ru = ru.prev;
                }
                if (crossL <= 0.0) {
                    lu = lu.next;
                }
                upper = lu.point.to(ru.point);
            }
            Vertex ll = left.rightMost;
            Vertex rl = right.leftMost;
            Vector2 lower = ll.point.to(rl.point);
            for (int i = 0; i < left.size * right.size; i++) {
                Vector2 lv = ll.point.to(ll.prev.point);
                Vector2 rv = rl.point.to(rl.next.point);
                double crossR = lower.cross(rv);
                double crossL = lv.cross(lower.getNegative());
                if (crossR > 0.0 && crossL > 0.0) {
                    break;
                }
                if (crossR <= 0.0) {
                    rl = rl.next;
                }
                if (crossL <= 0.0) {
                    ll = ll.prev;
                }
                lower = ll.point.to(rl.point);
            }
            lu.prev = ru;
            ru.next = lu;
            ll.next = rl;
            rl.prev = ll;
            hull.root = lu;
            Vertex v0 = hull.root;
            Vertex v = v0;
            int size = 0;
            do {
                size++;
                v = v.next;
            } while (v != v0);
            hull.size = size;
            return hull;
        }
    }

    /**
	 * Performs a merge of two convex hulls where one is a line segment
	 * and the other is a single point into a triangluar hull.
	 * @param one the {@link Hull} containing one point
	 * @param two the {@link Hull} containing two points
	 * @param hull the hull set the triangle to
	 */
    private void mergeTriangle(Hull one, Hull two, Hull hull) {
        Vector2 p1 = two.root.point;
        Vector2 p2 = two.root.next.point;
        Vector2 p = one.root.point;
        Vector2 v1 = p.to(p1);
        Vector2 v2 = p1.to(p2);
        double area = v1.cross(v2);
        if (area < 0.0) {
            hull.root = insert(one.root, two.root, two.root.next);
        } else {
            hull.root = prepend(one.root, two.root, two.root.next);
        }
    }

    /**
	 * Adds the given vertex v to the doubly linked list containing
	 * vertices v1 and v2 at the beginning of the list.
	 * <p>
	 * This method changes the linked list from: v1-v2-v1 to: 
	 * v-v1-v2-v.
	 * <p>
	 * The returned vertex is the new root vertex for the hull.  The
	 * actual vertex returned is arbitrary since the root vertex can
	 * be any vertex on the hull.
	 * @param v the vertex to add
	 * @param v1 the first vertex in the doubly linked list
	 * @param v2 the second (and last) vertex in the doubly linked list
	 * @return {@link Vertex} the root vertex
	 */
    private Vertex prepend(Vertex v, Vertex v1, Vertex v2) {
        v.next = v1;
        v.prev = v2;
        v1.prev = v;
        v2.next = v;
        return v;
    }

    /**
	 * Adds the given vertex v to the doubly linked list containing
	 * vertices v1 and v2 in the middle of the list.
	 * <p>
	 * This method changes the linked list from: v1-v2-v1 to: 
	 * v1-v-v2-v1.
	 * <p>
	 * The returned vertex is the new root vertex for the hull.  The
	 * actual vertex returned is arbitrary since the root vertex can
	 * be any vertex on the hull.
	 * @param v the vertex to add
	 * @param v1 the first vertex in the doubly linked list
	 * @param v2 the second (and last) vertex in the doubly linked list
	 * @return {@link Vertex} the root vertex
	 */
    private Vertex insert(Vertex v, Vertex v1, Vertex v2) {
        v.prev = v1;
        v.next = v2;
        v1.next = v;
        v2.prev = v;
        return v;
    }
}
