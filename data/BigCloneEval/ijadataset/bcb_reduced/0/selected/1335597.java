package computational.geometry.pointlocation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.*;
import computational.*;
import computational.geometry.*;
import computational.geometry.delaunaytriangulation.*;

/**
 * Class implements Chains method for solving
 * point location in planar triangulation with n points. 
 * Chains method has O(n log n) for preprocessing 
 * and O(log^2 n) query cost.
 */
public class ChainsMethod extends PointLocation {

    private Collection segments;

    private DelaunayTriangulation triangulation;

    private GraphPoint[] graph_points;

    private List[] chains;

    GraphEdge found_edge;

    List probed_chains;

    /**
	 * Default constructor
	 */
    public ChainsMethod() {
        super();
    }

    /**
	 * Constructor with collection of points
	 * @param ps collection of input points
	 */
    public ChainsMethod(Collection ps) {
        super(ps);
    }

    /**
	 * Constructor with collection of points and history
	 * @param ps collection of input points
	 * @param history log used for tracking changes
	 */
    public ChainsMethod(Collection ps, LogManager history) {
        super(ps, history);
    }

    /**
	 * Method used to verify if preconditions are satisfied
	 * @throws PreconditionViolatedException 
	 */
    public void checkPreconditions() throws PreconditionViolatedException {
    }

    /**
	 * Method executes query - localizes triangle that contains 
	 * point. 
	 * @param point point for which query is executed
	 */
    public Triangle getTriangle(Point point) {
        int N = points.size();
        int n_chains = graph_points[N - 1].W_in;
        found_edge = null;
        probed_chains = new ArrayList();
        if (point.getY() < graph_points[0].getY() || point.getY() > graph_points[N - 1].getY()) return null;
        int l = 0;
        int r = n_chains + 1;
        int cur_chain = ImplicitTree.predecessor(1, n_chains);
        int last_direction = 0;
        GraphEdge e = null;
        while (r - l > 1) {
            while (true) {
                if (cur_chain <= l) cur_chain = ImplicitTree.getRightSon(cur_chain); else if (cur_chain >= r) cur_chain = ImplicitTree.getLeftSon(cur_chain); else break;
            }
            e = searchInChain(chains[cur_chain], point);
            probed_chains.add(chains[cur_chain]);
            last_direction = BasicTests.turns(e.target, e.source, point);
            switch(last_direction) {
                case Turn.LEFT:
                    r = e.I_min;
                    break;
                case Turn.RIGHT:
                    l = e.I_max;
                    break;
                case Turn.COLLINEAR:
                    return null;
            }
        }
        if (l == 0 || r == n_chains + 1) return null;
        found_edge = e;
        GraphPoint p1 = e.source;
        GraphPoint p2 = e.target;
        GraphPoint p3 = null;
        switch(last_direction) {
            case Turn.LEFT:
                p3 = e.L;
                break;
            case Turn.RIGHT:
                p3 = e.R;
                break;
        }
        return new Triangle(p1, p2, p3);
    }

    /**
	 * Method executed every time when is needed to update
	 * the structure used in Chains method. 
	 */
    public void update() {
        if (isUpdated()) {
            return;
        }
        triangulation = new Knuth(points);
        segments = triangulation.getSegments();
        Collections.sort(points, PointComparator.COMPARATOR);
        createGraph();
        computeChains();
        setUpdated(true);
    }

    /**
	 * Returns list of points of triangulation.
	 * @return list of points in triangulation
	 */
    public List getPoints() {
        return points;
    }

    /**
	 * Returns collection of segments that compose the 
	 * triangulation. 
	 * @return collection of segments in triangulation
	 */
    public Collection getSegments() {
        if (!isUpdated()) {
            update();
        }
        return segments;
    }

    /**
	 * Draws triangulation to the graphics
	 * @param gfx
	 */
    public void draw(Graphics gfx) {
        Collection segments = getSegments();
        if (segments != null) {
            for (Iterator it = segments.iterator(); it.hasNext(); ) {
                Segment segment = (Segment) it.next();
                Point a = (Point) segment.getPoints().get(0);
                Point b = (Point) segment.getPoints().get(1);
                gfx.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
            }
        }
    }

    /**
	 * Draws point to the graphics
	 * @param point
	 * @param gfx
	 */
    public void draw(Point point, Graphics gfx) {
        Triangle triangle = getTriangle(point);
        Graphics2D g2 = (Graphics2D) gfx;
        Color old_color = g2.getColor();
        Stroke old_stroke = g2.getStroke();
        draw(gfx);
        if (triangle != null) {
            gfx.fillPolygon(getPolygon(triangle));
            g2.setColor(Color.BLUE);
            int n_pc = probed_chains.size();
            for (int c = 0; c < n_pc; ++c) {
                List chain = (List) probed_chains.get(c);
                g2.setStroke(new BasicStroke(n_pc - c));
                for (int j = 0; j < chain.size(); ++j) {
                    GraphEdge e = (GraphEdge) chain.get(j);
                    g2.drawLine(e.source.getX(), e.source.getY(), e.target.getX(), e.target.getY());
                }
            }
            g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(found_edge.source.getX(), found_edge.source.getY(), found_edge.target.getX(), found_edge.target.getY());
        }
        g2.setColor(old_color);
        g2.setStroke(old_stroke);
    }

    /**
	 * Creates graph of points in triangulation. There is an edge
	 * between two points if there is segment in triangulation
	 * that connects them. 
	 */
    private void createGraph() {
        int N = points.size();
        graph_points = new GraphPoint[N];
        for (int i = 0; i < N; i++) {
            graph_points[i] = new GraphPoint((Point) points.get(i));
        }
        for (Iterator it = segments.iterator(); it.hasNext(); ) {
            Segment segment = (Segment) it.next();
            List listPoints = segment.getPoints();
            Point pu = (Point) listPoints.get(0);
            Point pv = (Point) listPoints.get(1);
            int iu = Collections.binarySearch(points, pu, PointComparator.COMPARATOR);
            int iv = Collections.binarySearch(points, pv, PointComparator.COMPARATOR);
            if (PointComparator.COMPARATOR.compare(pu, pv) > 0) {
                int tmp = iu;
                iu = iv;
                iv = tmp;
            }
            GraphPoint gpu = graph_points[iu];
            GraphPoint gpv = graph_points[iv];
            GraphEdge new_edge = new GraphEdge(gpu, gpv);
            gpu.outgoing_edges.add(new_edge);
            gpv.ingoing_edges.add(new_edge);
        }
        for (int i = 0; i < N; i++) {
            GraphPoint p = graph_points[i];
            Comparator cw_cmp = computational.geometry.PointComparator.CLOCKWISE_ORDER(p);
            Comparator ccw_cmp = Collections.reverseOrder(cw_cmp);
            Collections.sort(p.outgoing_edges, new EdgeComparator(ccw_cmp, 1));
            Collections.sort(p.ingoing_edges, new EdgeComparator(cw_cmp, 0));
            p.W_in = p.ingoing_edges.size();
            p.W_out = p.outgoing_edges.size();
        }
    }

    /**
	 * Computes the chains.
	 */
    private void computeChains() {
        int N = points.size();
        for (int i = 1; i < N - 1; ++i) {
            GraphPoint p = graph_points[i];
            int v_out = p.outgoing_edges.size();
            if (p.W_in > v_out) {
                GraphEdge d = (GraphEdge) p.outgoing_edges.get(v_out - 1);
                int old_weight = d.weight;
                d.weight = p.W_in - v_out + 1;
                p.W_out += d.weight - old_weight;
                d.target.W_in += d.weight - old_weight;
            }
        }
        GraphPoint L = null, R = null;
        for (int i = N - 1; i >= 1; --i) {
            GraphPoint p = graph_points[i];
            if (p.W_out > p.W_in) {
                GraphEdge d = (GraphEdge) p.ingoing_edges.get(0);
                int old_weight = d.weight;
                d.weight = p.W_out - p.W_in + old_weight;
                p.W_in += d.weight - old_weight;
                d.source.W_out += d.weight - old_weight;
            }
            int cur_chain;
            if (i == N - 1) {
                cur_chain = 1;
                int n_chains = p.W_in;
                chains = new List[n_chains + 1];
                for (int c = 1; c <= n_chains; ++c) {
                    chains[c] = new ArrayList();
                }
            } else {
                GraphEdge first_outgoing = (GraphEdge) p.outgoing_edges.get(0);
                cur_chain = first_outgoing.I_min;
                L = first_outgoing.target;
                GraphEdge last_outgoing = (GraphEdge) p.outgoing_edges.get(p.outgoing_edges.size() - 1);
                R = last_outgoing.target;
            }
            int v_in = p.ingoing_edges.size();
            for (int j = 0; j < v_in; ++j) {
                GraphEdge e = (GraphEdge) p.ingoing_edges.get(j);
                e.I_min = cur_chain;
                e.I_max = cur_chain + e.weight - 1;
                cur_chain += e.weight;
                int c = ImplicitTree.predecessor(e.I_min, e.I_max);
                chains[c].add(e);
                if (j == 0) {
                    e.L = L;
                } else {
                    GraphEdge prev_edge = (GraphEdge) p.ingoing_edges.get(j - 1);
                    e.L = prev_edge.source;
                }
                if (j == v_in - 1) {
                    e.R = R;
                } else {
                    GraphEdge next_edge = (GraphEdge) p.ingoing_edges.get(j + 1);
                    e.R = next_edge.source;
                }
            }
        }
    }

    /**
	 * Performs binary search of a point within a chain.
	 * Preconditions:
	 * Chains are sorted on the y axis, greatest first
	 * The point is not outside the chain range
	 * @param chain The chain
	 * @param p The point to be searched
	 * @return The edge whose y range encloses y
	 */
    private GraphEdge searchInChain(List chain, Point p) {
        int r = 0;
        int l = chain.size();
        int py = p.getY();
        while (true) {
            int mid = (r + l) / 2;
            GraphEdge e = (GraphEdge) chain.get(mid);
            int ey1 = e.source.getY();
            int ey2 = e.target.getY();
            if (py >= ey1 && py <= ey2) return e; else if (py < ey1) r = mid; else l = mid;
        }
    }

    /**
	 * Implements a comparator for Point. The points are sorted
	 * using the y axis (decreasing) as first key and the x axis (increasing)
	 * as second key
	 */
    private static class PointComparator implements Comparator {

        public static PointComparator COMPARATOR = new PointComparator();

        private PointComparator() {
        }

        public int compare(Object o1, Object o2) {
            return compare_points((Point) o1, (Point) o2);
        }

        public int compare_points(Point p1, Point p2) {
            if (p1.getY() < p2.getY()) return -1;
            if (p1.getY() == p2.getY()) if (p1.getX() > p2.getX()) return -1; else if (p1.getX() == p2.getX()) return 0;
            return 1;
        }
    }

    /**
	 * This comparator adapts a PointComparator to compare GraphEdge,
	 * using one of the two endpoints as key.
	 */
    private static class EdgeComparator implements Comparator {

        EdgeComparator(Comparator cmp, int endpoint) {
            this.cmp = cmp;
            this.endpoint = endpoint;
        }

        public int compare(Object o1, Object o2) {
            GraphEdge e1 = (GraphEdge) o1;
            GraphEdge e2 = (GraphEdge) o2;
            if (endpoint == 0) return cmp.compare(e1.source, e2.source); else return cmp.compare(e1.target, e2.target);
        }

        private Comparator cmp;

        private int endpoint;
    }

    private static class GraphPoint extends Point {

        private static final long serialVersionUID = -1605415797941522934L;

        public GraphPoint(int x, int y) {
            super(x, y);
            outgoing_edges = new ArrayList();
            ingoing_edges = new ArrayList();
            W_in = 0;
            W_out = 0;
        }

        public GraphPoint(Point p) {
            this(p.getX(), p.getY());
        }

        List outgoing_edges;

        List ingoing_edges;

        int W_in;

        int W_out;
    }

    private static class GraphEdge {

        public GraphEdge(GraphPoint s, GraphPoint t) {
            source = s;
            target = t;
            weight = 1;
            I_min = -1;
            I_max = -1;
            L = R = null;
        }

        GraphPoint source;

        GraphPoint target;

        int weight;

        int I_min;

        int I_max;

        GraphPoint L;

        GraphPoint R;
    }

    /**
	 * This class implements the arithmetic needed for an 
	 * implicit binary search tree
	 */
    private static class ImplicitTree {

        public static int getLevel(int n) {
            int level = 0;
            while ((n & 1) == 0) {
                n >>= 1;
                ++level;
            }
            return level;
        }

        public static int getParent(int n) {
            int l = getLevel(n);
            n &= ~(3 << l);
            n |= 2 << l;
            return n;
        }

        public static int getRightSon(int n) {
            int l = getLevel(n) - 1;
            n &= ~(3 << l);
            n |= 3 << l;
            return n;
        }

        public static int getLeftSon(int n) {
            int l = getLevel(n) - 1;
            n &= ~(3 << l);
            n |= 1 << l;
            return n;
        }

        /**
		 * @param a
		 * @param b
		 * @return The lowest common ancestor of a and b
		 */
        public static int predecessor(int a, int b) {
            int l1 = getLevel(a);
            int l2 = getLevel(b);
            if (l1 > l2) {
                int tmp = a;
                a = b;
                b = tmp;
                tmp = l1;
                l1 = l2;
                l2 = tmp;
            }
            while (l1 < l2) {
                a = getParent(a);
                ++l1;
            }
            while (a != b) {
                a = getParent(a);
                b = getParent(b);
            }
            return a;
        }
    }
}
