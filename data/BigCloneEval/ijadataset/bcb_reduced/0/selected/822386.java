package org.jnet.shape;

import org.jnet.g3d.*;
import org.jnet.modelset.Node;
import org.jnet.modelset.Edge;
import org.jnet.viewer.JnetConstants;
import org.jnet.viewer.Overlap;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Hashtable;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import org.jnet.g3d.Graphics3D;
import org.nbrowse.utils.ErrorUtil;
import org.nbrowse.views.JnetManager;

public final class SticksRenderer extends ShapeRenderer implements Serializable {

    static final long serialVersionUID = 1L;

    /** pixels away from edge for overlap */
    private static final int POINT_RADIUS = 3;

    protected boolean showMultipleEdges;

    protected byte modeMultipleEdge;

    protected byte endcaps;

    protected boolean ssedgesBackbone;

    protected boolean hedgesBackbone;

    protected boolean edgesBackbone;

    protected boolean hedgesSolid;

    protected Node nodeA, nodeB;

    private Edge edge;

    int xA, yA, zA;

    int xB, yB, zB;

    int dx, dy;

    int pos;

    int mag2d;

    protected short colixA, colixB;

    protected int width;

    protected int edgeOrder;

    private boolean renderWireframe;

    private boolean isAntialiased;

    /** iterates through all edges in model set and renders them */
    protected void render() {
        endcaps = Graphics3D.ENDCAPS_SPHERICAL;
        showMultipleEdges = viewer.getShowMultipleEdges();
        modeMultipleEdge = viewer.getModeMultipleEdge();
        renderWireframe = viewer.getInMotion() && viewer.getWireframeRotation();
        ssedgesBackbone = viewer.getSsedgesBackbone();
        hedgesBackbone = viewer.getHedgesBackbone();
        edgesBackbone = hedgesBackbone | ssedgesBackbone;
        hedgesSolid = viewer.getHedgesSolid();
        Hashtable hash = new Hashtable();
        isAntialiased = g3d.isAntialiased();
        Edge[] edges = modelSet.getEdges();
        for (int i = modelSet.getEdgeCount(); --i >= 0; ) {
            setEdge(edges[i]);
            if (edge.getOrder() == 5) {
                pos = 1;
                String key = new String(edge.getNode1().getInfo());
                String keyConv = new String(edge.getNode2().getInfo());
                key += new String(edge.getNode2().getInfo());
                keyConv += new String(edge.getNode1().getInfo());
                if (hash.containsKey(key) || hash.containsKey(keyConv)) {
                    if (!hash.containsKey(key)) key = keyConv;
                    Integer value = (Integer) hash.get(key);
                    pos = value.intValue();
                    pos++;
                    edge.setMultiOrder(pos - 1);
                    hash.put(key, new Integer(pos));
                } else {
                    hash.put(key, new Integer(1));
                    edge.setMultiOrder(pos - 1);
                }
            }
            if ((edge.getShapeVisibilityFlags() & myVisibilityFlag) != 0) renderEdge();
        }
    }

    /** render an edge(self,single,multi), called from render() edge iteration
   * render edge instance var currently set by render(), should probably 
   * just pass edge in for clarity purposes right? */
    private void renderEdge() {
        mad = edge.getMad();
        nodeA = edge.getNode1();
        nodeB = edge.getNode2();
        if (!nodeA.isModelVisible() || !nodeB.isModelVisible() || modelSet.isNodeHidden(nodeA.getNodeIndex()) || modelSet.isNodeHidden(nodeB.getNodeIndex())) return;
        colixA = nodeA.getColix();
        colixB = nodeB.getColix();
        if (((colix = edge.getColix()) & Graphics3D.OPAQUE_MASK) == Graphics3D.USE_PALETTE) {
            colix = (short) (colix & ~Graphics3D.OPAQUE_MASK);
            colixA = Graphics3D.getColixInherited((short) (colix | viewer.getColixNodePalette(nodeA, JnetConstants.PALETTE_CPK)), colixA);
            colixB = Graphics3D.getColixInherited((short) (colix | viewer.getColixNodePalette(nodeB, JnetConstants.PALETTE_CPK)), colixB);
        } else {
            colixA = Graphics3D.getColixInherited(colix, colixA);
            colixB = Graphics3D.getColixInherited(colix, colixB);
        }
        int order = edge.getOrder() & ~JnetConstants.EDGE_NEW;
        if (edgesBackbone) {
            if (ssedgesBackbone && (order & JnetConstants.EDGE_SULFUR_MASK) != 0) {
                nodeA = nodeA.getGroup().getLeadNode(nodeA);
                nodeB = nodeB.getGroup().getLeadNode(nodeB);
            } else if (hedgesBackbone && (order & JnetConstants.EDGE_HYDROGEN_MASK) != 0) {
                nodeA = nodeA.getGroup().getLeadNode(nodeA);
                nodeB = nodeB.getGroup().getLeadNode(nodeB);
            }
        }
        xA = nodeA.screenX;
        yA = nodeA.screenY;
        zA = nodeA.screenZ;
        xB = nodeB.screenX;
        yB = nodeB.screenY;
        zB = nodeB.screenZ;
        if (zA == 1 || zB == 1) return;
        dx = xB - xA;
        dy = yB - yA;
        int inc = JnetConstants.incNumberFromName(edge.getWidth());
        width = viewer.scaleToScreen((zA + zB) / 2, mad * inc);
        if (renderWireframe && width > 0) width = 1;
        edgeOrder = getRenderEdgeOrder(order);
        switch(edgeOrder) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                renderEdge2(viewer.getEdgeShape());
                break;
            case 8:
                renderSelfEdgeCircle();
                break;
            case JnetConstants.EDGE_ORDER_UNSPECIFIED:
            case JnetConstants.EDGE_AROMATIC_SINGLE:
                edgeOrder = 1;
                renderEdge(order == JnetConstants.EDGE_AROMATIC_SINGLE ? 0 : 1);
                break;
            case JnetConstants.EDGE_AROMATIC:
            case JnetConstants.EDGE_AROMATIC_DOUBLE:
                edgeOrder = 2;
                renderEdge(order == JnetConstants.EDGE_AROMATIC ? getAromaticDottedEdgeMask() : 0);
                break;
            case JnetConstants.EDGE_STEREO_NEAR:
            case JnetConstants.EDGE_STEREO_FAR:
                renderTriangle(edge);
                break;
            default:
                if ((edgeOrder & JnetConstants.EDGE_PARTIAL_MASK) != 0) {
                    edgeOrder = JnetConstants.getPartialEdgeOrder(order);
                    renderEdge(JnetConstants.getPartialEdgeDotted(order));
                } else if ((edgeOrder & JnetConstants.EDGE_HYDROGEN_MASK) != 0) {
                    if (hedgesSolid) {
                        edgeOrder = 1;
                        renderEdge(0);
                    } else {
                        renderHedgeDashed();
                    }
                    break;
                }
        }
    }

    int getRenderEdgeOrder(int order) {
        order &= ~JnetConstants.EDGE_NEW;
        if ((order & JnetConstants.EDGE_PARTIAL_MASK) != 0) return order;
        if ((order & JnetConstants.EDGE_SULFUR_MASK) != 0) order &= ~JnetConstants.EDGE_SULFUR_MASK;
        if ((order & JnetConstants.EDGE_COVALENT_MASK) != 0) {
            if (order == 1 || !showMultipleEdges || modeMultipleEdge == JnetConstants.MULTIEDGE_NEVER || (modeMultipleEdge == JnetConstants.MULTIEDGE_NOTSMALL && mad > JnetConstants.madMultipleEdgeSmallMaximum)) {
                return 1;
            }
        }
        return order;
    }

    private boolean lineEdge;

    /** this is the real workhorse */
    private void renderEdge(int mask) {
        lineEdge = (width <= 1);
        if (lineEdge && (isAntialiased || isGenerator)) {
            width = 3;
            lineEdge = false;
        }
        width = viewer.calculateStretch(width) + 1;
        if (dx == 0 && dy == 0 && edgeOrder != 8) {
            if (!lineEdge) {
                int space = width / 8 + 3;
                int step = width + space;
                int y = yA - (edgeOrder - 1) * step / 2;
                do {
                    fillCylinder(colixA, colixA, endcaps, width, xA, y, zA, xA, y, zA);
                    y += step;
                } while (--edgeOrder > 0);
            }
            return;
        }
        if (edgeOrder == 1) {
            if ((mask & 1) != 0) drawEdge(colixA, colixB, width, xA, yA, zA, xB, yB, zB); else fillCylinder(colixA, colixB, endcaps, width, xA, yA, zA, xB, yB, zB);
            return;
        } else if (edgeOrder == 5) {
            renderEdgeInMultiEdge((mask & 1) == 0);
            return;
        }
    }

    /** do we need the mask? for nb2 i dont think so*/
    private void renderSelfEdgeCircle() {
        SelfEdge se = new SelfEdge(edge);
        se.render();
    }

    private static final int MIN_SELF_DIAMETER = 28;

    private class SelfEdge {

        private Edge edge;

        private int diameter, radius, centerX, centerY;

        private SelfEdge(Edge e) {
            edge = e;
            init();
        }

        private void init() {
            diameter = viewer.calculateStretch(edge.getNode1().screenDiameter);
            if (diameter < MIN_SELF_DIAMETER) diameter = MIN_SELF_DIAMETER;
            radius = diameter / 2;
            int offset = (int) (0.8 * radius);
            centerX = edge.x1s() + offset;
            centerY = edge.y1s() + offset;
        }

        private void handleDuplicates() {
            Edge[] edges = modelSet.getEdges();
            boolean circleisValid = false;
            while (!circleisValid) {
                boolean isMatchFound = false;
                for (int i = modelSet.getEdgeCount(); --i >= 0; ) {
                    Edge e = edges[i];
                    if (!isVis(e)) continue;
                    if (!e.equals(edge) && e.isCircularSelfEdge()) {
                        int newdiameter = viewer.calculateStretch(e.getNode1().screenDiameter);
                        if (newdiameter < MIN_SELF_DIAMETER) newdiameter = MIN_SELF_DIAMETER;
                        int newradius = newdiameter / 2;
                        int offset = (int) (0.8 * radius);
                        int newcenterX = e.x1s() + offset;
                        int newcenterY = e.y1s() + offset;
                        if (diameter == newdiameter && centerX == newcenterX && centerY == newcenterY) {
                            isMatchFound = true;
                            break;
                        }
                    }
                }
                if (isMatchFound) {
                    diameter = diameter + 3;
                    radius = diameter / 2;
                    int offset = (int) (0.8 * radius);
                    centerX = edge.x1s() + offset;
                    centerY = edge.y1s() + offset;
                } else circleisValid = true;
            }
        }

        private void render() {
            g3d.setColix(edge.getColix());
            for (int i = 0; i < circWidth(); i++) g3d.drawCircleCentered(edge.getColix(), diameter + i, centerX, centerY, z(), false);
        }

        private int circWidth() {
            return width() / 4 == 0 ? 1 : width() / 4;
        }

        private boolean pointOverlap(int x, int y) {
            double dis = lineLength(x, y, centerX, centerY);
            return dis < radius + circWidth() + POINT_RADIUS && dis > radius - POINT_RADIUS;
        }

        private int z() {
            return edge.getNode1().screenZ;
        }
    }

    private int width() {
        return width;
    }

    private void renderEdgeInMultiEdge(boolean doCylinder) {
        Point3i scrnMidPt = getMidEdgePointScreen();
        int x1 = node1.screenX, y1 = node1.screenY, z1 = node1.screenZ;
        int x2 = node2.screenX, y2 = node2.screenY, z2 = node2.screenZ;
        g3d.setColix(edge.getColix());
        if (!doCylinder) {
            drawEdge(colixA, colixB, width, scrnMidPt.x, scrnMidPt.y, scrnMidPt.z, x1, y1, z1);
            drawEdge(colixA, colixB, width, scrnMidPt.x, scrnMidPt.y, scrnMidPt.z, x2, y2, z2);
        } else {
            fillCylinder(colixA, colixB, endcaps, width, scrnMidPt.x, scrnMidPt.y, scrnMidPt.z, x1, y1, z1);
            fillCylinder(colixA, colixB, endcaps, width, scrnMidPt.x, scrnMidPt.y, scrnMidPt.z, x2, y2, z2);
        }
    }

    private Point3i getMidEdgePointScreen() {
        return getMidMultiEdgePointScreen(edge);
    }

    /** Edge actually caches midpt from last calc - so uses that if still valid
    * (nodes in same place)
    * @return mid pt to use for bent 2-line multi edge
    * midPt is not the mid point between 2 nodes, but for multiedge is bent off
    * that so edges arent on top of each other. multi edge is 2 lines that meet
    * at midpt */
    private Point3i getMidMultiEdgePointScreen(Edge e) {
        if (e.hasValidMidEdgePoint()) return e.getMidEdgePoint();
        Point3i mp = jmolToScreen(getMidMultiEdgePoint(e));
        e.setMidEdgePoint(mp);
        return mp;
    }

    private Point3i jmolToScreen(Point3f jmol) {
        return viewer.jmolToScreen(jmol);
    }

    /** for multi edge - this is the off from center point that the 2
   * "bent" lines connect with - should cache this
   * @return
   */
    private Point3f getMidMultiEdgePoint(Edge e) {
        if (is2D()) return get2DMidMultiEdgePoint(e); else return get3DMidEdgePoint();
    }

    /** mid edge bend point for 2D edge - in jmol/angstroms
   * do this in screen coords? - should the edge cache this? why not right? */
    private Point3f get2DMidMultiEdgePoint(Edge e) {
        Point3f mp = e.exactMidPointJmol();
        float edgeSpacingAnstrom = edgeSpacingAngstrom();
        int edgeNumber = e.getMultiOrder();
        float xPerp = e.y1() - e.y2();
        float yPerp = e.x2() - e.x1();
        Vector2f perp = new Vector2f(xPerp, yPerp);
        perp.normalize();
        if (isOdd(edgeNumber)) perp.negate();
        mp.x += edgeSpacingAnstrom * edgeNumber * perp.x;
        mp.y += edgeSpacingAnstrom * edgeNumber * perp.y;
        return mp;
    }

    private boolean isOdd(int i) {
        return i % 2 != 0;
    }

    private void setEdge(Edge e) {
        edge = e;
        node1 = null;
        node2 = null;
        initNodes();
    }

    private Node node1, node2;

    private Node node1() {
        initNodes();
        return node1;
    }

    private Node node2() {
        initNodes();
        return node2;
    }

    /** Some edges of multi edge have from to nodes flipped from others which messes up math
   *  for putting edges on different sides of midline, so important to keep consistent */
    private void initNodes() {
        if (node1 != null && node2 != null) return;
        Node en1 = edge.getNode1();
        Node en2 = edge.getNode2();
        if (en1.getNodeIndex() < en2.getNodeIndex()) {
            node1 = en1;
            node2 = en2;
        } else {
            node1 = en2;
            node2 = en1;
        }
    }

    private float edgeSpacingAngstrom() {
        Node n1 = node1();
        Node n2 = node2();
        if (n1 == null || n2 == null) return .1f;
        float s = (float) ((viewer.calculateStretch(n2.getScreenRadius()) + viewer.calculateStretch(n1.getScreenRadius())) / (1.5 * viewer.getZoomPercentFloat()));
        if (s == 0) s = .001f;
        return s;
    }

    /** for mid edge point where edge bends for 3D edge */
    private Point3f get3DMidEdgePoint() {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        Vector3f vector12 = new Vector3f();
        Vector3f vectorNormalR = new Vector3f();
        Vector3f vectorNormalS = new Vector3f();
        Vector3f vector1P = new Vector3f();
        Point3f pt = new Point3f();
        vector12.set(Math.abs(node2.x - node1.x), Math.abs(node2.y - node1.y), Math.abs(node2.z - node1.z));
        vector1P.set(Math.abs(node2.x - 2 - node1.x), Math.abs(node2.y - 2 - node1.y), Math.abs(node2.z - 2 - node1.z));
        vectorNormalR.cross(vector1P, vector12);
        if (!is2D()) vectorNormalS.cross(vectorNormalR, vector12);
        vectorNormalR.normalize();
        if (!is2D()) vectorNormalS.normalize();
        float midPtx = node1.x + (node2.x - node1.x) / 2;
        float midPty = node1.y + (node2.y - node1.y) / 2;
        float midPtz = node1.z + (node2.z - node1.z) / 2;
        double b = (viewer.calculateStretch(node2.getScreenRadius()) + viewer.calculateStretch(node1.getScreenRadius())) / (1.5 * viewer.getZoomPercentFloat());
        int mo = edge.getMultiOrder();
        int cos = (int) JnetConstants.xIncr[mo];
        int sin = (int) JnetConstants.yIncr[mo];
        if (is2D()) midPtx += (float) (b * mo * vectorNormalR.x); else midPtx += (float) (b * cos * vectorNormalR.x + b * sin * vectorNormalS.x);
        if (is2D()) midPty += (float) (b * mo * vectorNormalR.y); else midPty += (float) (b * cos * vectorNormalR.y + b * sin * vectorNormalS.y);
        if (!is2D()) midPtz += (float) (b * cos * vectorNormalR.z + b * sin * vectorNormalS.z);
        pt.set(midPtx, midPty, midPtz);
        return pt;
    }

    /** i think this just calls renderEdge(int) so not sure why this exists??
   * i think this is jmol stuff not used by nbrowse */
    private void renderEdge2(int Mask) {
        double xt = 0;
        double yt = 0;
        double zt = 0;
        double segmentLength;
        double t = 1;
        int dmtr;
        if (Mask == JnetConstants.EDGE_SHAPE_STICK || Mask == JnetConstants.EDGE_SHAPE_BAR) {
            renderEdge(Mask);
            return;
        }
        lineEdge = (width <= 1);
        if (lineEdge && (isAntialiased || isGenerator)) {
            width = 3;
            lineEdge = false;
        }
        int x1 = edge.getNode1().screenX;
        int y1 = edge.getNode1().screenY;
        int z1 = edge.getNode1().screenZ;
        int x2 = edge.getNode2().screenX;
        int y2 = edge.getNode2().screenY;
        int z2 = edge.getNode2().screenZ;
        double screenEdgeLength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
        for (int i = 0; i < 50; i++) {
            t = 0.01 * i;
            xt = (x2 - x1) * t + x1;
            yt = (y2 - y1) * t + y1;
            zt = (z2 - z1) * t + z1;
            segmentLength = Math.sqrt((x1 - xt) * (x1 - xt) + (y1 - yt) * (y1 - yt) + (z1 - zt) * (z1 - zt));
            if (segmentLength >= viewer.calculateStretch(edge.getNode1().getScreenRadius()) - 1) break;
        }
        double xs = (x2 - x1) * t + x1;
        double ys = (y2 - y1) * t + y1;
        double zs = (z2 - z1) * t + z1;
        Point3f screenPtStart = new Point3f((int) xs, (int) ys, (int) zs);
        for (int i = 50; i < 100; i++) {
            t = 0.01 * i;
            xt = (x2 - x1) * t + x1;
            yt = (y2 - y1) * t + y1;
            zt = (z2 - z1) * t + z1;
            segmentLength = Math.sqrt((x1 - xt) * (x1 - xt) + (y1 - yt) * (y1 - yt) + (z1 - zt) * (z1 - zt));
            if (screenEdgeLength - segmentLength <= viewer.calculateStretch(edge.getNode2().getScreenRadius()) + viewer.calculateStretch(edge.getNode2().getScreenRadius())) break;
        }
        double xb = (x2 - x1) * t + x1;
        double yb = (y2 - y1) * t + y1;
        double zb = (z2 - z1) * t + z1;
        Point3f screenPtBegin = new Point3f((int) xb, (int) yb, (int) zb);
        for (int i = 50; i < 100; i++) {
            t = 0.01 * i;
            xt = (x2 - x1) * t + x1;
            yt = (y2 - y1) * t + y1;
            zt = (z2 - z1) * t + z1;
            segmentLength = Math.sqrt((x1 - xt) * (x1 - xt) + (y1 - yt) * (y1 - yt) + (z1 - zt) * (z1 - zt));
            if (screenEdgeLength - segmentLength <= viewer.calculateStretch(edge.getNode2().getScreenRadius()) + 1) break;
        }
        double xe = (x2 - x1) * t + x1;
        double ye = (y2 - y1) * t + y1;
        double ze = (z2 - z1) * t + z1;
        Point3f screenPtEnd = new Point3f((int) xe, (int) ye, (int) ze);
        if (dx == 0 && dy == 0 && edgeOrder != 8) {
            if (!lineEdge) {
                int space = width / 8 + 3;
                int step = width + space;
                int y = yA - (edgeOrder - 1) * step / 2;
                do {
                    fillCylinder(colixA, colixA, endcaps, width, xA, y, zA, xA, y, zA);
                    y += step;
                } while (--edgeOrder > 0);
            }
            return;
        }
        if (edgeOrder == 1) {
            if (Mask == JnetConstants.EDGE_SHAPE_CONE) {
                if (edge.getOrder() != 5 && edge.getOrder() != 8) renderCone(edge);
            } else if (Mask == JnetConstants.EDGE_SHAPE_TGO) {
                if (edge.getOrder() != 5 && edge.getOrder() != 8) renderTriangleEdge();
            } else if (Mask == JnetConstants.EDGE_SHAPE_ARROW_3D) {
                if (width > 1) fillCylinder(colixA, colixB, endcaps, width, (int) screenPtStart.x, (int) screenPtStart.y, (int) screenPtStart.z, (int) screenPtBegin.x, (int) screenPtBegin.y, (int) screenPtBegin.z); else fillCylinder(colixA, colixB, endcaps, width, (int) screenPtStart.x, (int) screenPtStart.y, (int) screenPtStart.z, (int) screenPtEnd.x, (int) screenPtEnd.y, (int) screenPtEnd.z);
                if (width < 10) dmtr = viewer.calculateStretch(edge.getNode2().getScreenRadius()) / 3; else dmtr = width + width / 2;
                if (dmtr == 0) dmtr = 7;
                if (width > 1) g3d.fillCone(Graphics3D.ENDCAPS_FLAT, dmtr, screenPtBegin, screenPtEnd);
            } else if (Mask == JnetConstants.EDGE_SHAPE_ARROW_2D) {
                if (width > 1) drawEdge(colixA, colixB, width, (int) screenPtStart.x, (int) screenPtStart.y, (int) screenPtStart.z, (int) screenPtBegin.x, (int) screenPtBegin.y, (int) screenPtBegin.z); else drawEdge(colixA, colixB, width, (int) screenPtStart.x, (int) screenPtStart.y, (int) screenPtStart.z, (int) screenPtEnd.x, (int) screenPtEnd.y, (int) screenPtEnd.z);
                if (width < 10) dmtr = viewer.calculateStretch(edge.getNode2().getScreenRadius()) / 3; else dmtr = width + width / 2;
                if (dmtr == 0) dmtr = 7;
                if (width > 1) renderArrow2D(dmtr, screenPtBegin, screenPtEnd);
            }
        }
        if (edgeOrder == 5) {
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();
            edge.getMultiOrder();
            int cos = (int) JnetConstants.xIncr[edge.getMultiOrder()];
            int sin = (int) JnetConstants.yIncr[edge.getMultiOrder()];
            Point3f v = new Point3f();
            Point3i ptXY1 = new Point3i();
            Point3i ptXY2 = new Point3i();
            v.set(node1);
            viewer.transformPoint(v, ptXY1);
            v.set(node2);
            viewer.transformPoint(v, ptXY2);
            Vector3f vector12 = new Vector3f();
            Vector3f vectorNormalR = new Vector3f();
            Vector3f vectorNormalS = new Vector3f();
            Vector3f vector1P = new Vector3f();
            Point3f midPt = new Point3f();
            vector12.set(Math.abs(node2.x - node1.x), Math.abs(node2.y - node1.y), Math.abs(node2.z - node1.z));
            vector1P.set(Math.abs(node2.x - 2 - node1.x), Math.abs(node2.y - 2 - node1.y), Math.abs(node2.z - 2 - node1.z));
            vectorNormalR.cross(vector1P, vector12);
            vectorNormalS.cross(vectorNormalR, vector12);
            vectorNormalR.normalize();
            vectorNormalS.normalize();
            float midPtx = node1.x + (node2.x - node1.x) / 2;
            float midPty = node1.y + (node2.y - node1.y) / 2;
            float midPtz = node1.z + (node2.z - node1.z) / 2;
            double b = (double) (viewer.calculateStretch(node2.getScreenRadius()) + viewer.calculateStretch(node1.getScreenRadius())) / (1.5 * viewer.getZoomPercentFloat());
            if (b == 0) b = 1;
            float Qx = (float) (midPtx + b * cos * vectorNormalR.x + b * sin * vectorNormalS.x);
            float Qy = (float) (midPty + b * cos * vectorNormalR.y + b * sin * vectorNormalS.y);
            float Qz = (float) (midPtz + b * cos * vectorNormalR.z + b * sin * vectorNormalS.z);
            midPt.set(Qx, Qy, Qz);
            Point3i ptXYZ = new Point3i();
            viewer.transformPoint(midPt, ptXYZ);
            x1 = ptXY1.x;
            y1 = ptXY1.y;
            z1 = ptXY1.z;
            x2 = ptXYZ.x;
            y2 = ptXYZ.y;
            z2 = ptXYZ.z;
            screenEdgeLength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
            for (int i = 0; i < 50; i++) {
                t = 0.01 * i;
                xt = (x2 - x1) * t + x1;
                yt = (y2 - y1) * t + y1;
                zt = (z2 - z1) * t + z1;
                segmentLength = Math.sqrt((x1 - xt) * (x1 - xt) + (y1 - yt) * (y1 - yt) + (z1 - zt) * (z1 - zt));
                if (segmentLength >= viewer.calculateStretch(edge.getNode1().getScreenRadius()) + 1) break;
            }
            xs = (x2 - x1) * t + x1;
            ys = (y2 - y1) * t + y1;
            zs = (z2 - z1) * t + z1;
            Point3i screenPtS = new Point3i((int) xs, (int) ys, (int) zs);
            x1 = ptXY2.x;
            y1 = ptXY2.y;
            z1 = ptXY2.z;
            x2 = ptXYZ.x;
            y2 = ptXYZ.y;
            z2 = ptXYZ.z;
            screenEdgeLength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
            for (int i = 0; i < 100; i++) {
                t = 0.01 * i;
                xt = (x2 - x1) * t + x1;
                yt = (y2 - y1) * t + y1;
                zt = (z2 - z1) * t + z1;
                segmentLength = Math.sqrt((x1 - xt) * (x1 - xt) + (y1 - yt) * (y1 - yt) + (z1 - zt) * (z1 - zt));
                int value = viewer.calculateStretch(edge.getNode2().getScreenRadius()) + viewer.calculateStretch(edge.getNode2().getScreenRadius());
                if (width == 1) value = viewer.calculateStretch(edge.getNode2().getScreenRadius());
                if (segmentLength >= value) break;
            }
            xs = (x2 - x1) * t + x1;
            ys = (y2 - y1) * t + y1;
            zs = (z2 - z1) * t + z1;
            Point3i screenPtE = new Point3i((int) xs, (int) ys, (int) zs);
            for (int i = 0; i < 50; i++) {
                t = 0.01 * i;
                xt = (x2 - x1) * t + x1;
                yt = (y2 - y1) * t + y1;
                zt = (z2 - z1) * t + z1;
                segmentLength = Math.sqrt((x1 - xt) * (x1 - xt) + (y1 - yt) * (y1 - yt) + (z1 - zt) * (z1 - zt));
                if (segmentLength >= viewer.calculateStretch(edge.getNode2().getScreenRadius())) break;
            }
            xs = (x2 - x1) * t + x1;
            ys = (y2 - y1) * t + y1;
            zs = (z2 - z1) * t + z1;
            Point3i screenPtA = new Point3i((int) xs, (int) ys, (int) zs);
            g3d.setColix(edge.getColix());
            if (Mask == JnetConstants.EDGE_SHAPE_CONE) {
                g3d.fillCone(Graphics3D.ENDCAPS_FLAT, width > 1 ? width / 2 : width, screenPtS, ptXYZ);
                fillCylinder(colixA, colixB, endcaps, 2, (int) ptXYZ.x, (int) ptXYZ.y, (int) ptXYZ.z, (int) screenPtA.x, (int) screenPtA.y, (int) screenPtA.z);
            } else if (Mask == JnetConstants.EDGE_SHAPE_TGO) {
                drawEdge(colixA, colixB, 2, (int) ptXYZ.x, (int) ptXYZ.y, (int) ptXYZ.z, (int) screenPtA.x, (int) screenPtA.y, (int) screenPtA.z);
                renderArrow2D(width, screenPtS, ptXYZ);
            } else if (Mask == JnetConstants.EDGE_SHAPE_ARROW_3D) {
                fillCylinder(colixA, colixB, endcaps, width, (int) ptXYZ.x, (int) ptXYZ.y, (int) ptXYZ.z, (int) screenPtS.x, (int) screenPtS.y, (int) screenPtS.z);
                fillCylinder(colixA, colixB, endcaps, width, (int) ptXYZ.x, (int) ptXYZ.y, (int) ptXYZ.z, (int) screenPtE.x, (int) screenPtE.y, (int) screenPtE.z);
                if (width < 10) dmtr = viewer.calculateStretch(edge.getNode2().getScreenRadius()) / 3; else dmtr = width + width / 2;
                if (dmtr == 0) dmtr = 7;
                if (width > 1) g3d.fillCone(Graphics3D.ENDCAPS_FLAT, dmtr, screenPtE, screenPtA);
            } else if (Mask == JnetConstants.EDGE_SHAPE_ARROW_2D) {
                drawEdge(colixA, colixB, width, (int) ptXYZ.x, (int) ptXYZ.y, (int) ptXYZ.z, (int) screenPtS.x, (int) screenPtS.y, (int) screenPtS.z);
                drawEdge(colixA, colixB, width, (int) ptXYZ.x, (int) ptXYZ.y, (int) ptXYZ.z, (int) screenPtE.x, (int) screenPtE.y, (int) screenPtE.z);
                if (width < 10) dmtr = viewer.calculateStretch(edge.getNode2().getScreenRadius()) / 3; else dmtr = width + width / 2;
                if (dmtr == 0) dmtr = 7;
                if (width > 1) renderArrow2D(dmtr, screenPtE, screenPtA);
            }
            return;
        }
        if (edgeOrder == 8) {
            if (Mask == JnetConstants.EDGE_SHAPE_TGO || Mask == JnetConstants.EDGE_SHAPE_ARROW_2D) {
                g3d.setColix(edge.getColix());
                int w = (int) (3 * width / 4);
                if (w == 0) w = viewer.getZoomPercent() / 150;
                if (w == 0) w = 1;
                for (int i = 0; i < 170; i++) {
                    g3d.drawCircleCentered(edge.getColix(), w, (int) (xA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.sin(0.0175 * i))), (int) (yA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.cos(0.0175 * i))), zA, true);
                }
                for (int i = 280; i < 360; i++) {
                    g3d.drawCircleCentered(edge.getColix(), w, (int) (xA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.sin(0.0175 * i))), (int) (yA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.cos(0.0175 * i))), zA, true);
                }
            } else {
                g3d.setColix(edge.getColix());
                int w = (int) (3 * width / 4);
                if (w == 0) w = viewer.getZoomPercent() / 150;
                if (w == 0) w = 1;
                for (int i = 0; i < 170; i++) {
                    g3d.fillSphereCentered(w, (int) (xA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.sin(0.0175 * i))), (int) (yA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.cos(0.0175 * i))), zA);
                }
                for (int i = 280; i < 360; i++) {
                    g3d.fillSphereCentered(w, (int) (xA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.sin(0.0175 * i))), (int) (yA + viewer.calculateStretch(edge.getNode1().screenDiameter) / 2 * (0.8 + Math.cos(0.0175 * i))), zA);
                }
            }
        }
    }

    int xAxis1, yAxis1, xAxis2, yAxis2, dxStep, dyStep;

    void resetAxisCoordinates() {
        int space = mag2d >> 3;
        int step = width + space;
        dxStep = step * dy / mag2d;
        dyStep = step * -dx / mag2d;
        xAxis1 = xA;
        yAxis1 = yA;
        xAxis2 = xB;
        yAxis2 = yB;
        if (edgeOrder > 1) {
            int f = (edgeOrder - 1);
            xAxis1 -= dxStep * f / 2;
            yAxis1 -= dyStep * f / 2;
            xAxis2 -= dxStep * f / 2;
            yAxis2 -= dyStep * f / 2;
        }
    }

    void stepAxisCoordinates() {
        xAxis1 += dxStep;
        yAxis1 += dyStep;
        xAxis2 += dxStep;
        yAxis2 += dyStep;
    }

    private static int wideWidthMilliAngstroms = 400;

    private void renderTriangle(Edge edge) {
        if (!g3d.checkTranslucent(false)) return;
        int mag2d = (int) Math.sqrt(dx * dx + dy * dy);
        int wideWidthPixels = viewer.scaleToScreen(zB, wideWidthMilliAngstroms);
        int dxWide, dyWide;
        if (mag2d == 0) {
            dxWide = 0;
            dyWide = wideWidthPixels;
        } else {
            dxWide = wideWidthPixels * -dy / mag2d;
            dyWide = wideWidthPixels * dx / mag2d;
        }
        int xWideUp = xB + dxWide / 2;
        int xWideDn = xWideUp - dxWide;
        int yWideUp = yB + dyWide / 2;
        int yWideDn = yWideUp - dyWide;
        g3d.setColix(colixA);
        if (colixA == colixB) {
            g3d.drawfillTriangle(xA, yA, zA, xWideUp, yWideUp, zB, xWideDn, yWideDn, zB);
        } else {
            int xMidUp = (xA + xWideUp) / 2;
            int yMidUp = (yA + yWideUp) / 2;
            int zMid = (zA + zB) / 2;
            int xMidDn = (xA + xWideDn) / 2;
            int yMidDn = (yA + yWideDn) / 2;
            g3d.drawfillTriangle(xA, yA, zA, xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid);
            g3d.setColix(colixB);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid, xWideDn, yWideDn, zB);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xWideUp, yWideUp, zB, xWideDn, yWideDn, zB);
        }
    }

    private int getAromaticDottedEdgeMask() {
        Node nodeC = nodeB.findAromaticNeighbor(nodeA.getNodeIndex());
        if (nodeC == null) return 1;
        int dxAC = nodeC.screenX - xA;
        int dyAC = nodeC.screenY - yA;
        return ((dx * dyAC - dy * dxAC) < 0 ? 2 : 1);
    }

    void drawDashed(int xA, int yA, int zA, int xB, int yB, int zB) {
        int dx = xB - xA;
        int dy = yB - yA;
        int dz = zB - zA;
        int i = 2;
        while (i <= 9) {
            int xS = xA + (dx * i) / 12;
            int yS = yA + (dy * i) / 12;
            int zS = zA + (dz * i) / 12;
            i += 3;
            int xE = xA + (dx * i) / 12;
            int yE = yA + (dy * i) / 12;
            int zE = zA + (dz * i) / 12;
            i += 2;
            fillCylinder(colixA, colixB, Graphics3D.ENDCAPS_FLAT, width, xS, yS, zS, xE, yE, zE);
        }
    }

    void renderHedgeDashed() {
        int dx = xB - xA;
        int dy = yB - yA;
        int dz = zB - zA;
        int i = 1;
        while (i < 10) {
            int xS = xA + (dx * i) / 10;
            int yS = yA + (dy * i) / 10;
            int zS = zA + (dz * i) / 10;
            short colixS = i < 5 ? colixA : colixB;
            i += 2;
            int xE = xA + (dx * i) / 10;
            int yE = yA + (dy * i) / 10;
            int zE = zA + (dz * i) / 10;
            short colixE = i < 5 ? colixA : colixB;
            ++i;
            fillCylinder(colixS, colixE, Graphics3D.ENDCAPS_FLAT, width, xS, yS, zS, xE, yE, zE);
        }
    }

    protected void fillCylinder(short colixA, short colixB, byte endcaps, int diameter, int xA, int yA, int zA, int xB, int yB, int zB) {
        if (lineEdge) g3d.drawLine(colixA, colixB, xA, yA, zA, xB, yB, zB); else g3d.fillCylinder(colixA, colixB, endcaps, diameter, xA, yA, zA, xB, yB, zB);
    }

    void renderCone(Edge edge) {
        lineEdge = (width <= 1);
        if (lineEdge && (isAntialiased || isGenerator)) {
            width = 3;
            lineEdge = false;
        }
        Point3f screenPtBegin = new Point3f(edge.getNode1().screenX, edge.getNode1().screenY, edge.getNode1().screenZ);
        Point3f screenPtEnd = new Point3f(edge.getNode2().screenX, edge.getNode2().screenY, edge.getNode2().screenZ);
        int coneDiameter = width > 1 ? width * 3 : width;
        g3d.setColix(edge.getColix());
        g3d.fillCone(Graphics3D.ENDCAPS_FLAT, coneDiameter, screenPtBegin, screenPtEnd);
    }

    protected void drawEdge(short colixA, short colixB, int diameter, int xA, int yA, int zA, int xB, int yB, int zB) {
        if (lineEdge || diameter == 1) g3d.drawLine(colixA, colixB, xA, yA, zA, xB, yB, zB); else g3d.fillCylinder2(colixA, colixB, Graphics3D.ENDCAPS_FLAT, diameter, xA, yA, zA, xB, yB, zB);
    }

    private void renderTriangleEdge() {
        if (!g3d.checkTranslucent(false)) return;
        int mag2d = (int) Math.sqrt(dx * dx + dy * dy);
        int wideWidthPixels = width * 9;
        int dxWide, dyWide;
        if (mag2d == 0) {
            dxWide = 0;
            dyWide = wideWidthPixels;
        } else {
            dxWide = wideWidthPixels / 3 * -dy / mag2d;
            dyWide = wideWidthPixels / 3 * dx / mag2d;
        }
        int xWideUp = xA + dxWide / 2;
        int xWideDn = xWideUp - dxWide;
        int yWideUp = yA + dyWide / 2;
        int yWideDn = yWideUp - dyWide;
        g3d.setColix(colixA);
        if (colixA == colixB) {
            g3d.drawfillTriangle(xB, yB, zB, xWideUp, yWideUp, zA, xWideDn, yWideDn, zA);
        } else {
            int xMidUp = (xB + xWideUp) / 2;
            int yMidUp = (yB + yWideUp) / 2;
            int zMid = (zB + zA) / 2;
            int xMidDn = (xB + xWideDn) / 2;
            int yMidDn = (yB + yWideDn) / 2;
            g3d.drawfillTriangle(xB, yB, zB, xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid);
            g3d.setColix(colixA);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid, xWideDn, yWideDn, zA);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xWideUp, yWideUp, zA, xWideDn, yWideDn, zA);
        }
    }

    private void renderArrow2D(int width, Point3f ptStart, Point3f ptEnd) {
        if (!g3d.checkTranslucent(false)) return;
        int dx = (int) (ptStart.x - ptEnd.x);
        int dy = (int) (ptStart.y - ptEnd.y);
        int mag2d = (int) Math.sqrt(dx * dx + dy * dy);
        int wideWidthPixels = width * 4;
        int dxWide, dyWide;
        if (mag2d == 0) {
            dxWide = 0;
            dyWide = wideWidthPixels;
        } else {
            dxWide = wideWidthPixels / 3 * -dy / mag2d;
            dyWide = wideWidthPixels / 3 * dx / mag2d;
        }
        int xWideUp = (int) ptStart.x + dxWide / 2;
        int xWideDn = xWideUp - dxWide;
        int yWideUp = (int) ptStart.y + dyWide / 2;
        int yWideDn = yWideUp - dyWide;
        g3d.setColix(colixA);
        if (colixA == colixB) {
            g3d.drawfillTriangle((int) ptEnd.x, (int) ptEnd.y, (int) ptEnd.z, xWideUp, yWideUp, (int) ptStart.z, xWideDn, yWideDn, (int) ptStart.z);
        } else {
            int xMidUp = ((int) ptEnd.x + xWideUp) / 2;
            int yMidUp = ((int) ptEnd.y + yWideUp) / 2;
            int zMid = ((int) ptEnd.z + (int) ptStart.z) / 2;
            int xMidDn = ((int) ptEnd.x + xWideDn) / 2;
            int yMidDn = ((int) ptEnd.y + yWideDn) / 2;
            g3d.drawfillTriangle((int) ptEnd.x, (int) ptEnd.y, (int) ptEnd.z, xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid);
            g3d.setColix(colixA);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid, xWideDn, yWideDn, (int) ptStart.z);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xWideUp, yWideUp, (int) ptStart.z, xWideDn, yWideDn, (int) ptStart.z);
        }
    }

    private void renderArrow2D(int width, Point3i ptStart, Point3i ptEnd) {
        if (!g3d.checkTranslucent(false)) return;
        int dx = ptStart.x - ptEnd.x;
        int dy = ptStart.y - ptEnd.y;
        int mag2d = (int) Math.sqrt(dx * dx + dy * dy);
        int wideWidthPixels = width * 4;
        int dxWide, dyWide;
        if (mag2d == 0) {
            dxWide = 0;
            dyWide = wideWidthPixels;
        } else {
            dxWide = wideWidthPixels / 3 * -dy / mag2d;
            dyWide = wideWidthPixels / 3 * dx / mag2d;
        }
        int xWideUp = ptStart.x + dxWide / 2;
        int xWideDn = xWideUp - dxWide;
        int yWideUp = ptStart.y + dyWide / 2;
        int yWideDn = yWideUp - dyWide;
        g3d.setColix(colixA);
        if (colixA == colixB) {
            g3d.drawfillTriangle(ptEnd.x, ptEnd.y, ptEnd.z, xWideUp, yWideUp, ptStart.z, xWideDn, yWideDn, ptStart.z);
        } else {
            int xMidUp = (ptEnd.x + xWideUp) / 2;
            int yMidUp = (ptEnd.y + yWideUp) / 2;
            int zMid = (ptEnd.z + ptStart.z) / 2;
            int xMidDn = (ptEnd.x + xWideDn) / 2;
            int yMidDn = (ptEnd.y + yWideDn) / 2;
            g3d.drawfillTriangle(ptEnd.x, ptEnd.y, ptEnd.z, xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid);
            g3d.setColix(colixA);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xMidDn, yMidDn, zMid, xWideDn, yWideDn, ptStart.z);
            g3d.drawfillTriangle(xMidUp, yMidUp, zMid, xWideUp, yWideUp, ptStart.z, xWideDn, yWideDn, ptStart.z);
        }
    }

    /**
   * isOverlap is the derived class version of overlap detection used if point overlaps with a stick shape (ie edge, etc).
   * currently invokes getEdgeAtPoint and creates Overlap object with edge object
   * @param x
   * @param y
   * @return Overlap;
   */
    public Overlap getOverlap(int i_x, int i_y) {
        Overlap overlap = null;
        Edge e = this.getEdgeAtPoint(i_x, i_y);
        if (e != null) overlap = Overlap.makeEdgeOverlap(e);
        return overlap;
    }

    /** iterates through edges and if one is near point x,y returns it
   * rename this edgeOverlap?
   * @param x x of pt to check for
   * @param y y of pt to check for
   */
    public Edge getEdgeAtPoint(int x, int y) {
        Edge[] edges = modelSet.getEdges();
        for (int i = modelSet.getEdgeCount(); --i >= 0; ) {
            Edge e = edges[i];
            if (!isVis(e)) continue;
            if (e.isSingleNonSelfEdge()) if (singleEdgeOverlap(e, x, y)) return e;
            if (e.isPartOfMultiEdge()) if (multiEdgeOverlap(e, x, y)) return e;
            if (e.isCircularSelfEdge()) if (selfEdgeOverlap(e, x, y)) return e;
        }
        return null;
    }

    /** edge method? */
    private boolean isVis(Edge e) {
        if (!e.isVisible()) return false;
        if (!e.getNode1().isVisible()) return false;
        return e.getNode2().isVisible();
    }

    /** @param e 
   * @param e edge to check for overlap
   * @param x,y point to check for overlap
   * @return true if x,y overlaps with edge */
    private boolean singleEdgeOverlap(Edge e, int x, int y) {
        if (!pointInEdgeRegion(e, x, y)) return false;
        return circleLineIntersect2D(x, y, nodePt(e.getNode1()), nodePt(e.getNode2()));
    }

    /** this works in screen space not angstroms/jmol
   * @param x,y are in screen coords (from mouse usually) */
    private boolean pointInEdgeRegion(Edge e, int x, int y) {
        Rectangle r = new Rectangle(nodePt(e.getNode1()));
        r.add(nodePt(e.getNode2()));
        r.grow(POINT_RADIUS, POINT_RADIUS);
        return pointInRectangle(x, y, r);
    }

    private Point nodePt(Node n) {
        return new Point(n.screenX, n.screenY);
    }

    private boolean pointInRectangle(int x, int y, Rectangle r) {
        return r.contains(new Point(x, y));
    }

    /** a line and a circle intersect if the disciminate is < 0
   * that is r-squared*dr-squared - Dsquared > 0
   * which can be rewritten r*sqrt(dx^2-dy^2) > x1y2-x2y1
   * this assumes circle at 0,0 - so points need to be shifted
   */
    private boolean circleLineIntersect2D(int circleX, int circleY, Point p1, Point p2) {
        float x1 = p1.x - circleX;
        float y1 = p1.y - circleY;
        float x2 = p2.x - circleX;
        float y2 = p2.y - circleY;
        float dx = x2 - x1;
        float dxe2 = dx * dx;
        float dy = y2 - y1;
        float dye2 = dy * dy;
        float dr = (float) Math.sqrt(dxe2 + dye2);
        float rdr = POINT_RADIUS * dr;
        float D = Math.abs(x1 * y2 - x2 * y1);
        return rdr > D;
    }

    private int disFromPtToLine(int xp, int yp, Point l1, Point l2) {
        return disFromPtToLine(xp, yp, l1.x, l1.y, l2.x, l2.y);
    }

    /**better than circleLineIntersect is distance from line to point and then
   * check that that is distance less than a fixed amount
   * (x2-x1)(y1-y0) - (x1-x0)(y2-y1) / sqrt((x2-x1)^2 + (y2-y1)^2)
   * pt is x0 y0, line is x1 y1 to x2 y2 
   * @param xp x of pt
   * @param yp y of pt
   * @param x1 x of node1
   * @param y1 y node1
   * @param x2 x node2
   * @param y2 y node2 - in screen coords!
   * @return distance  */
    private int disFromPtToLine(int xp, int yp, int x1, int y1, int x2, int y2) {
        int dis = (int) (((x2 - x1) * (y1 - yp) - (x1 - xp) * (y2 - y1)) / lineLength(x1, y1, x2, y2));
        return Math.abs(dis);
    }

    private double lineLength(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dx2 = dx * dx;
        int dy = y2 - y1;
        int dy2 = dy * dy;
        return Math.sqrt(dx2 + dy2);
    }

    /** edge is multi edge, check both lines of multi edge 
   * @param e */
    private boolean multiEdgeOverlap(Edge e, int x, int y) {
        Point p1 = e.screenPoint1();
        Rectangle r1 = halfMultiRect(e, p1);
        Point midPt = null;
        int dis;
        if (pointInRectangle(x, y, r1)) {
            midPt = midPointMulti(e);
            dis = disFromPtToLine(x, y, p1, midPt);
            if (dis <= POINT_RADIUS) return true;
        }
        Point p2 = e.screenPoint2();
        Rectangle r2 = halfMultiRect(e, p2);
        if (!pointInRectangle(x, y, r2)) return false;
        if (midPt == null) midPt = midPointMulti(e);
        dis = disFromPtToLine(x, y, p2, midPt);
        return dis <= POINT_RADIUS;
    }

    private boolean selfEdgeOverlap(Edge e, int x, int y) {
        SelfEdge se = new SelfEdge(e);
        return se.pointOverlap(x, y);
    }

    /** @param p node point to make rect with mid point with
   * @return rectangle from point p to midEdgePt, expand by POINT_RADIUS */
    private Rectangle halfMultiRect(Edge e, Point p) {
        Point3i midPt = getMidMultiEdgePointScreen(e);
        Rectangle r = new Rectangle(p);
        r.add(toPoint(midPt));
        r.grow(POINT_RADIUS, POINT_RADIUS);
        return r;
    }

    private Point toPoint(Point3i p) {
        return new Point(p.x, p.y);
    }

    private Point midPointMulti(Edge e) {
        return toPoint(getMidMultiEdgePointScreen(e));
    }
}
