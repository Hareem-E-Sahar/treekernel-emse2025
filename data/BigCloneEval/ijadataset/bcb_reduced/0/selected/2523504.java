package com.vividsolutions.jts.operation.union;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Provides an efficient method of unioning a collection of 
 * {@link Polygonal} geometrys.
 * This algorithm is faster and likely more robust than
 * the simple iterated approach of 
 * repeatedly unioning each polygon to a result geometry.
 * <p>
 * The <tt>buffer(0)</tt> trick is sometimes faster, but can be less robust and 
 * can sometimes take an exceptionally long time to complete.
 * This is particularly the case where there is a high degree of overlap
 * between the polygons.  In this case, <tt>buffer(0)</tt> is forced to compute
 * with <i>all</i> line segments from the outset, 
 * whereas cascading can eliminate many segments
 * at each stage of processing.
 * The best case for buffer(0) is the trivial case
 * where there is <i>no</i> overlap between the input geometries. 
 * However, this case is likely rare in practice.
 * 
 * @author Martin Davis
 *
 */
public class CascadedPolygonUnion {

    /**
	 * Computes the union of
	 * a collection of {@link Polygonal} {@link Geometry}s.
	 * 
	 * @param polys a collection of {@link Polygonal} {@link Geometry}s
	 */
    public static Geometry union(Collection polys) {
        CascadedPolygonUnion op = new CascadedPolygonUnion(polys);
        return op.union();
    }

    private Collection inputPolys;

    private GeometryFactory geomFactory = null;

    /**
	 * Creates a new instance to union
	 * the given collection of {@link Geometry}s.
	 * 
	 * @param geoms a collection of {@link Polygonal} {@link Geometry}s
	 */
    public CascadedPolygonUnion(Collection polys) {
        this.inputPolys = polys;
    }

    /**
   * The effectiveness of the index is somewhat sensitive
   * to the node capacity.  
   * Testing indicates that a smaller capacity is better.
   * For an STRtree, 4 is probably a good number (since
   * this produces 2x2 "squares").
   */
    private static final int STRTREE_NODE_CAPACITY = 4;

    /**
	 * Computes the union of the input geometries.
	 * 
	 * @return the union of the input geometries
	 * @return null if no input geometries were provided
	 */
    public Geometry union() {
        if (inputPolys.isEmpty()) return null;
        geomFactory = ((Geometry) inputPolys.iterator().next()).getFactory();
        STRtree index = new STRtree(STRTREE_NODE_CAPACITY);
        for (Iterator i = inputPolys.iterator(); i.hasNext(); ) {
            Geometry item = (Geometry) i.next();
            index.insert(item.getEnvelopeInternal(), item);
        }
        List itemTree = index.itemsTree();
        Geometry unionAll = unionTree(itemTree);
        return unionAll;
    }

    private Geometry unionTree(List geomTree) {
        List geoms = reduceToGeometries(geomTree);
        Geometry union = binaryUnion(geoms);
        return union;
    }

    private Geometry repeatedUnion(List geoms) {
        Geometry union = null;
        for (Iterator i = geoms.iterator(); i.hasNext(); ) {
            Geometry g = (Geometry) i.next();
            if (union == null) union = (Geometry) g.clone(); else union = union.union(g);
        }
        return union;
    }

    private Geometry bufferUnion(List geoms) {
        GeometryFactory factory = ((Geometry) geoms.get(0)).getFactory();
        Geometry gColl = factory.buildGeometry(geoms);
        Geometry unionAll = gColl.buffer(0.0);
        return unionAll;
    }

    private Geometry bufferUnion(Geometry g0, Geometry g1) {
        GeometryFactory factory = g0.getFactory();
        Geometry gColl = factory.createGeometryCollection(new Geometry[] { g0, g1 });
        Geometry unionAll = gColl.buffer(0.0);
        return unionAll;
    }

    /**
   * Unions a list of geometries 
   * by treating the list as a flattened binary tree,
   * and performing a cascaded union on the tree.
   */
    private Geometry binaryUnion(List geoms) {
        return binaryUnion(geoms, 0, geoms.size());
    }

    /**
   * Unions a section of a list using a recursive binary union on each half
   * of the section.
   * 
   * @param geoms
   * @param start
   * @param end
   * @return the union of the list section
   */
    private Geometry binaryUnion(List geoms, int start, int end) {
        if (end - start <= 1) {
            Geometry g0 = getGeometry(geoms, start);
            return unionSafe(g0, null);
        } else if (end - start == 2) {
            return unionSafe(getGeometry(geoms, start), getGeometry(geoms, start + 1));
        } else {
            int mid = (end + start) / 2;
            Geometry g0 = binaryUnion(geoms, start, mid);
            Geometry g1 = binaryUnion(geoms, mid, end);
            return unionSafe(g0, g1);
        }
    }

    private static Geometry getGeometry(List list, int index) {
        if (index >= list.size()) return null;
        return (Geometry) list.get(index);
    }

    /**
   * Reduces a tree of geometries to a list of geometries
   * by recursively unioning the subtrees in the list.
   * 
   * @param geomTree a tree-structured list of geometries
   * @return a list of Geometrys
   */
    private List reduceToGeometries(List geomTree) {
        List geoms = new ArrayList();
        for (Iterator i = geomTree.iterator(); i.hasNext(); ) {
            Object o = i.next();
            Geometry geom = null;
            if (o instanceof List) {
                geom = unionTree((List) o);
            } else if (o instanceof Geometry) {
                geom = (Geometry) o;
            }
            geoms.add(geom);
        }
        return geoms;
    }

    /**
   * Computes the union of two geometries, 
   * either of both of which may be null.
   * 
   * @param g0 a Geometry
   * @param g1 a Geometry
   * @return the union of the input(s)
   * @return null if both inputs are null
   */
    private Geometry unionSafe(Geometry g0, Geometry g1) {
        if (g0 == null && g1 == null) return null;
        if (g0 == null) return (Geometry) g1.clone();
        if (g1 == null) return (Geometry) g0.clone();
        return unionOptimized(g0, g1);
    }

    private Geometry unionOptimized(Geometry g0, Geometry g1) {
        Envelope g0Env = g0.getEnvelopeInternal();
        Envelope g1Env = g1.getEnvelopeInternal();
        if (!g0Env.intersects(g1Env)) {
            Geometry combo = GeometryCombiner.combine(g0, g1);
            return combo;
        }
        if (g0.getNumGeometries() <= 1 && g1.getNumGeometries() <= 1) return unionActual(g0, g1);
        Envelope commonEnv = g0Env.intersection(g1Env);
        return unionUsingEnvelopeIntersection(g0, g1, commonEnv);
    }

    /**
   * Unions two polygonal geometries.
   * The case of MultiPolygons is optimized to union only 
   * the polygons which lie in the intersection of the two geometry's envelopes.
   * Polygons outside this region can simply be combined with the union result,
   * which is potentially much faster.
   * This case is likely to occur often during cascaded union, and may also
   * occur in real world data (such as unioning data for parcels on different street blocks).
   * 
   * @param g0 a polygonal geometry
   * @param g1 a polygonal geometry
   * @param common the intersection of the envelopes of the inputs
   * @return the union of the inputs
   */
    private Geometry unionUsingEnvelopeIntersection(Geometry g0, Geometry g1, Envelope common) {
        List disjointPolys = new ArrayList();
        Geometry g0Int = extractByEnvelope(common, g0, disjointPolys);
        Geometry g1Int = extractByEnvelope(common, g1, disjointPolys);
        Geometry union = unionActual(g0Int, g1Int);
        disjointPolys.add(union);
        Geometry overallUnion = GeometryCombiner.combine(disjointPolys);
        return overallUnion;
    }

    private Geometry extractByEnvelope(Envelope env, Geometry geom, List disjointGeoms) {
        List intersectingGeoms = new ArrayList();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry elem = geom.getGeometryN(i);
            if (elem.getEnvelopeInternal().intersects(env)) intersectingGeoms.add(elem); else disjointGeoms.add(elem);
        }
        return geomFactory.buildGeometry(intersectingGeoms);
    }

    /**
   * Encapsulates the actual unioning of two polygonal geometries.
   * 
   * @param g0
   * @param g1
   * @return
   */
    private Geometry unionActual(Geometry g0, Geometry g1) {
        return g0.union(g1);
    }
}
