package simulator;

import java.util.ArrayList;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Dual-space Expansion for Estimation Penetration depth.
 * <p>
 * This class implements the algorithm from the paper:
 * "<em>Incremental Penetration Depth Estimation Between Convex Polytopes Using 
 * Dual-space Expansion</em>" by Young J Kim, Ming C Lin, and Dinesh Manocha.
 * 
 * @author Yung-Chang Tan
 *
 */
class DEEP {

    public static float EPSILON = 1e-6f;

    public float tol = EPSILON;

    public static final int VF = 0, FV = 1, EE = 2, EF = 3, FE = 4, FF = 5;

    Convex[] g;

    int maxContact;

    ArrayList manifold;

    public DEEP(Convex[] g, int maxContact, ArrayList manifold) {
        this.g = g;
        for (int i = 0; i < 2; i++) g[i].update();
        this.maxContact = maxContact;
        this.manifold = manifold;
    }

    public int collide() {
        g[0].setInvR();
        g[1].setInvR();
        Vector3d v = new Vector3d();
        v.sub(g[1].getPosition(), g[0].getPosition());
        Vertex v1 = g[0].getSupportPoint(v);
        v.negate();
        Vertex v2 = g[1].getSupportPoint(v);
        tol = 1e-3f;
        findIncrementalPD(v1, v2);
        return manifold.size();
    }

    public void findIncrementalPD(Vertex v1, Vertex v2) {
        FeaturePair best = reportMinPair(new Vertex[] { v1, v2 });
        FeaturePair[] fp = new FeaturePair[2];
        FeaturePair T = best;
        Vertex[] v = new Vertex[2];
        Vertex[] vf;
        do {
            best = T;
            if (best.d == Float.POSITIVE_INFINITY) {
                System.err.println("Cannot find the feature pair with minimum distance!");
                return;
            }
            switch(T.id) {
                case EE:
                    v[0] = ((HalfEdge) T.feature[0]).pair.v;
                    fp[0] = reportMinPair(new Vertex[] { v[0], v2 });
                    v[1] = ((HalfEdge) T.feature[1]).pair.v;
                    fp[1] = reportMinPair(new Vertex[] { v1, v[1] });
                    if (fp[0].d < fp[1].d) {
                        T = fp[0];
                        v1 = v[0];
                    } else {
                        T = fp[1];
                        v2 = v[1];
                    }
                    break;
                case VF:
                    vf = ((Face) T.feature[1]).v;
                    for (int i = 0, j = 0; i < vf.length; i++) {
                        if (vf[i] != v2) {
                            v[j] = vf[i];
                            fp[j] = reportMinPair(new Vertex[] { v1, v[j] });
                            j++;
                        }
                    }
                    if (fp[0].d < fp[1].d) {
                        T = fp[0];
                        v2 = v[0];
                    } else {
                        T = fp[1];
                        v2 = v[1];
                    }
                    break;
                case FV:
                    vf = ((Face) T.feature[0]).v;
                    for (int i = 0, j = 0; i < vf.length; i++) {
                        if (vf[i] != v1) {
                            v[j] = vf[i];
                            fp[j] = reportMinPair(new Vertex[] { v[j], v2 });
                            j++;
                        }
                    }
                    if (fp[0].d < fp[1].d) {
                        T = fp[0];
                        v1 = v[0];
                    } else {
                        T = fp[1];
                        v1 = v[1];
                    }
            }
        } while (T.d < best.d);
        best.setNormal(g);
        ContactManifold c = new ContactManifold(g);
        int fid = c.updateFeatures(best);
        if (fid == EF || fid == FE) {
            c.edgeface(best, fid, maxContact);
            manifold.addAll(c.manifold);
        } else if (fid == FF) {
            c.faceface(best, maxContact);
            if (c.manifold.size() > maxContact) {
                int inc = c.manifold.size() / maxContact;
                if (c.manifold.size() % maxContact > maxContact / 2) inc++;
                for (int i = 0; i < maxContact; i++) {
                    manifold.add(c.manifold.get(inc * i));
                }
            } else manifold.addAll(c.manifold);
        } else manifold.add(best);
    }

    private FeaturePair reportMinPair(Vertex[] v) {
        FeaturePair T = new FeaturePair();
        for (int i = 0; i < 2; i++) {
            HalfEdge h = v[i].h;
            Point2f[] polygon = new Point2f[v[i].f.size()];
            HalfEdge[] edges = new HalfEdge[v[i].f.size()];
            Vector3f r = new Vector3f();
            Vector3f x = new Vector3f();
            Vector3f y = new Vector3f();
            int j = 0;
            Vector3f prevN = h.pair.f.plane.n;
            do {
                Vector3f n = h.f.plane.n;
                if (!prevN.equals(n)) {
                    edges[j] = h;
                    r.scale(1.0f / v[i].N.dot(n), n);
                    if (j == 0) {
                        x.set(r);
                        x.normalize();
                        y.cross(v[i].N, x);
                        polygon[j++] = new Point2f(r.dot(x), 0);
                    } else polygon[j++] = new Point2f(r.dot(x), r.dot(y));
                }
                prevN = n;
                h = h.next.next.pair;
            } while (h != v[i].h);
            h = v[1 - i].h;
            int p = 0, numPoints = v[1 - i].f.size();
            Point2f[] points = new Point2f[numPoints];
            boolean[] antipodal = new boolean[numPoints];
            Vector3f[] normals = new Vector3f[numPoints];
            Point3f pos = g[1 - i].pointWorldToBody(g[i].pointBodyToWorld(v[i].pos));
            do {
                normals[p] = g[i].vectorWorldToBody(g[1 - i].vectorBodyToWorld(h.f.plane.n));
                normals[p].negate();
                float t = v[i].N.dot(normals[p]);
                if (t > 0) {
                    r.scale(1 / t, normals[p]);
                    points[p] = new Point2f(r.dot(x), r.dot(y));
                    if (pointInConvexPolygon(points[p], j, polygon)) {
                        antipodal[p] = true;
                        float d = h.f.distanceSquared(pos);
                        if (d < T.d) {
                            T.d = d;
                            T.feature[i] = v[i];
                            T.feature[1 - i] = h.f;
                            T.cp[i] = g[1 - i].pointBodyToWorld(pos);
                            T.cp[1 - i] = g[1 - i].pointBodyToWorld(h.f.closestPt);
                            T.id = (i == 0 ? VF : FV);
                        }
                    } else antipodal[p] = false;
                } else {
                    antipodal[p] = false;
                    points[p] = null;
                }
                p++;
                h = h.next.next.pair;
            } while (h != v[1 - i].h);
            if (i == 0) {
                h = v[1 - i].h;
                Point2f prevPoint = points[p - 1];
                prevN = normals[p - 1];
                p = 0;
                do {
                    Point2f p1 = prevPoint, p2 = points[p];
                    if (!prevN.equals(normals[p]) && (p1 != null || p2 != null)) {
                        if (p1 == null || p2 == null) {
                            Vector3f n = new Vector3f(normals[p]);
                            float t = v[i].N.dot(n);
                            t = (t - tol) / (t - v[i].N.dot(prevN));
                            n.scale(1 - t);
                            n.scaleAdd(t, prevN, n);
                            n.scale(1 / v[i].N.dot(n));
                            if (p1 == null) p1 = new Point2f(n.dot(x), n.dot(y)); else p2 = new Point2f(n.dot(x), n.dot(y));
                        }
                        ArrayList intersecting = findEdge(p1, p2, j, polygon, edges);
                        for (int k = 0; k < intersecting.size(); k++) {
                            HalfEdge h1 = (HalfEdge) intersecting.get(k);
                            float d = h.distanceSquared(g[1 - i].pointWorldToBody(g[i].pointBodyToWorld(h1.v.pos)), g[1 - i].pointWorldToBody(g[i].pointBodyToWorld(h1.pair.v.pos)));
                            if (d < T.d) {
                                T.d = d;
                                T.feature[i] = h1;
                                T.feature[1 - i] = h;
                                T.cp[i] = g[1 - i].pointBodyToWorld(h.c2);
                                T.cp[1 - i] = g[1 - i].pointBodyToWorld(h.c1);
                                T.id = EE;
                            }
                        }
                    }
                    prevPoint = points[p];
                    prevN = normals[p];
                    p++;
                    h = h.next.next.pair;
                } while (h != v[1 - i].h);
            }
        }
        return T;
    }

    /**
   * A test on two edges in 3D. Test if the closest point on the other edge is
   * in the voronoi region. This test doesn't seem to be necessary after a more 
   * accurate central projection is implemented. 
   * @param h
   * @param h1
   * @param i
   * @return
   */
    boolean verify(HalfEdge h, HalfEdge h1, int i) {
        Vector3f n = new Vector3f();
        n.sub(h.c1, h.c2);
        Vector3f a = new Vector3f(), b = new Vector3f();
        a.cross(h.f.plane.n, n);
        b.cross(h.f.plane.n, h.pair.f.plane.n);
        if (a.dot(b) < 0) return false;
        Vector3f c = new Vector3f();
        c.cross(n, h.pair.f.plane.n);
        if (a.dot(c) < 0) return false;
        Vector3f n1 = g[1 - i].vectorWorldToBody(g[i].vectorBodyToWorld(h1.f.plane.n));
        Vector3f n2 = g[1 - i].vectorWorldToBody(g[i].vectorBodyToWorld(h1.pair.f.plane.n));
        n.negate();
        a.cross(n1, n);
        b.cross(n1, n2);
        if (a.dot(b) < 0) return false;
        c.cross(n, n2);
        if (a.dot(c) < 0) return false;
        return true;
    }

    private ArrayList findEdge(Point2f a, Point2f b, int n, Point2f[] v, HalfEdge[] h) {
        ArrayList edges = new ArrayList();
        if (test2DSegmentSegment(a, b, v[n - 1], v[0])) edges.add(h[0]);
        for (int i = 1; i < n; i++) {
            if (test2DSegmentSegment(a, b, v[i - 1], v[i])) {
                edges.add(h[i]);
            }
        }
        return edges;
    }

    static boolean pointInConvexPolygon(Point2f p, int n, Point2f[] v) {
        int low = 0, high = n;
        do {
            int mid = (low + high) / 2;
            float area = signed2DTriArea(v[0], v[mid], p);
            if (area > 0) low = mid; else if (area < 0) high = mid; else {
                if (mid == 1) {
                    low = 1;
                    high = 2;
                } else {
                    high = mid;
                    low = mid - 1;
                }
            }
        } while (low + 1 < high);
        if (low == 0 || high == n) return false;
        return signed2DTriArea(v[low], v[high], p) >= 0;
    }

    static float signed2DTriArea(Point2f a, Point2f b, Point2f c) {
        return (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x);
    }

    static boolean test2DSegmentSegment(Point2f a, Point2f b, Point2f c, Point2f d) {
        float a1 = signed2DTriArea(a, b, d);
        float a2 = signed2DTriArea(a, b, c);
        if (a1 * a2 <= 0.0f) {
            float a3 = signed2DTriArea(c, d, a);
            float a4 = a3 + a2 - a1;
            if (a3 * a4 <= 0.0f) {
                if (a3 == 0 && a4 == 0) {
                    Vector2f ab = new Vector2f();
                    ab.sub(b, a);
                    if (ab.x != 0) {
                        if ((c.x - a.x) > ab.x && (d.x - a.x) > ab.x) return false;
                        if (c.x < a.x && d.x < a.x) return false;
                    } else {
                        if ((c.y - a.y) > ab.y && (d.y - a.y) > ab.y) return false;
                        if (c.y < a.y && d.y < a.y) return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        test2DSegmentSegment(new Point2f(0, 0), new Point2f(0, 1), new Point2f(0, 2), new Point2f(0, 3));
        test2DSegmentSegment(new Point2f(0, 0), new Point2f(0, 1), new Point2f(0, 0.8f), new Point2f(0, 3));
        test2DSegmentSegment(new Point2f(0, 0), new Point2f(0, 1), new Point2f(0, 0.8f), new Point2f(1, 1));
        test2DSegmentSegment(new Point2f(0, 0), new Point2f(0, 1), new Point2f(0, 1.2f), new Point2f(1, 1));
    }
}
