package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: MarkerRenderer.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class MarkerRenderer {

    private double elevation = 10d;

    private boolean overrideMarkerElevation = false;

    private boolean keepSeparated = true;

    private boolean enablePickSizeReturn = false;

    private long frameTimeStamp = 0;

    private ArrayList<Vec4> surfacePoints = new ArrayList<Vec4>();

    private MarkerAttributes previousAttributes;

    protected PickSupport pickSupport = new PickSupport();

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public boolean isOverrideMarkerElevation() {
        return overrideMarkerElevation;
    }

    public void setOverrideMarkerElevation(boolean overrideMarkerElevation) {
        this.overrideMarkerElevation = overrideMarkerElevation;
    }

    public boolean isKeepSeparated() {
        return keepSeparated;
    }

    public void setKeepSeparated(boolean keepSeparated) {
        this.keepSeparated = keepSeparated;
    }

    public boolean isEnablePickSizeReturn() {
        return enablePickSizeReturn;
    }

    public void setEnablePickSizeReturn(boolean enablePickSizeReturn) {
        this.enablePickSizeReturn = enablePickSizeReturn;
    }

    public void render(DrawContext dc, Iterable<Marker> markers) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (markers == null) {
            String message = Logging.getMessage("nullValue.MarkerListIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        this.draw(dc, markers);
    }

    protected void draw(DrawContext dc, Iterable<Marker> markers) {
        if (this.isKeepSeparated()) this.drawSeparated(dc, markers); else this.drawAll(dc, markers);
    }

    protected void drawSeparated(DrawContext dc, Iterable<Marker> markers) {
        List<Marker> markerList;
        if (markers instanceof List) {
            markerList = (List<Marker>) markers;
        } else {
            markerList = new ArrayList<Marker>();
            for (Marker m : markers) {
                markerList.add(m);
            }
        }
        if (markerList.size() == 0) return;
        Layer parentLayer = dc.getCurrentLayer();
        Vec4 eyePoint = dc.getView().getEyePoint();
        Marker m1 = markerList.get(0);
        Vec4 p1 = this.computeSurfacePoint(dc, m1.getPosition());
        double r1 = this.computeMarkerRadius(dc, p1, m1);
        if (this.intersectsFrustum(dc, p1, r1)) dc.addOrderedRenderable(new OrderedMarker(0, m1, p1, r1, parentLayer, eyePoint.distanceTo3(p1)));
        if (markerList.size() < 2) return;
        int im2 = markerList.size() - 1;
        Marker m2 = markerList.get(im2);
        Vec4 p2 = this.computeSurfacePoint(dc, m2.getPosition());
        double r2 = this.computeMarkerRadius(dc, p2, m2);
        if (this.intersectsFrustum(dc, p2, r2)) dc.addOrderedRenderable(new OrderedMarker(im2, m2, p2, r2, parentLayer, eyePoint.distanceTo3(p2)));
        if (markerList.size() < 3) return;
        this.drawInBetweenMarkers(dc, 0, p1, r1, im2, p2, r2, markerList, parentLayer, eyePoint);
    }

    private void drawInBetweenMarkers(DrawContext dc, int im1, Vec4 p1, double r1, int im2, Vec4 p2, double r2, List<Marker> markerList, Layer parentLayer, Vec4 eyePoint) {
        if (im2 == im1 + 1) return;
        if (p1.distanceTo3(p2) <= r1 + r2) return;
        int im = (im1 + im2) / 2;
        Marker m = markerList.get(im);
        Vec4 p = this.computeSurfacePoint(dc, m.getPosition());
        double r = this.computeMarkerRadius(dc, p, m);
        boolean b1 = false, b2 = false;
        if (p.distanceTo3(p1) > r + r1) {
            this.drawInBetweenMarkers(dc, im1, p1, r1, im, p, r, markerList, parentLayer, eyePoint);
            b1 = true;
        }
        if (p.distanceTo3(p2) > r + r2) {
            this.drawInBetweenMarkers(dc, im, p, r, im2, p2, r2, markerList, parentLayer, eyePoint);
            b2 = true;
        }
        if (b1 && b2 && this.intersectsFrustum(dc, p, r)) dc.addOrderedRenderable(new OrderedMarker(im, m, p, r, parentLayer, eyePoint.distanceTo3(p)));
    }

    private void drawMarker(DrawContext dc, int index, Marker marker, Vec4 point, double radius) {
        if (dc.isPickingMode()) {
            java.awt.Color color = dc.getUniquePickColor();
            int colorCode = color.getRGB();
            PickedObject po = new PickedObject(colorCode, marker, marker.getPosition(), false);
            po.setValue(AVKey.PICKED_OBJECT_ID, index);
            if (this.enablePickSizeReturn) po.setValue(AVKey.PICKED_OBJECT_SIZE, 2 * radius);
            this.pickSupport.addPickableObject(po);
            dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
        }
        MarkerAttributes attrs = marker.getAttributes();
        if (attrs != this.previousAttributes) {
            attrs.apply(dc);
            this.previousAttributes = attrs;
        }
        marker.render(dc, point, radius);
    }

    protected void computeSurfacePoints(DrawContext dc, Iterable<? extends Marker> markers) {
        surfacePoints.clear();
        for (Marker marker : markers) {
            if (marker == null) {
                surfacePoints.add(null);
                continue;
            }
            Position pos = marker.getPosition();
            Vec4 point = this.computeSurfacePoint(dc, pos);
            if (!dc.getView().getFrustumInModelCoordinates().contains(point)) {
                surfacePoints.add(null);
                continue;
            }
            surfacePoints.add(point);
        }
    }

    protected void drawAll(DrawContext dc, Iterable<Marker> markers) {
        Layer parentLayer = dc.getCurrentLayer();
        Vec4 eyePoint = dc.getView().getEyePoint();
        if (dc.getFrameTimeStamp() != this.frameTimeStamp) {
            this.frameTimeStamp = dc.getFrameTimeStamp();
            this.computeSurfacePoints(dc, markers);
        }
        Iterator<Marker> markerIterator = markers.iterator();
        for (int index = 0; markerIterator.hasNext(); index++) {
            Marker marker = markerIterator.next();
            Vec4 point = this.surfacePoints.get(index);
            if (point == null) continue;
            double radius = this.computeMarkerRadius(dc, point, marker);
            if (dc.isPickingMode() && !this.intersectsFrustum(dc, point, radius)) continue;
            dc.addOrderedRenderable(new OrderedMarker(index, marker, point, radius, parentLayer, eyePoint.distanceTo3(point)));
        }
    }

    protected void begin(DrawContext dc) {
        GL gl = dc.getGL();
        Vec4 cameraPosition = dc.getView().getEyePoint();
        if (dc.isPickingMode()) {
            this.pickSupport.beginPicking(dc);
            gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_TRANSFORM_BIT);
            gl.glDisable(GL.GL_COLOR_MATERIAL);
        } else {
            gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_TRANSFORM_BIT | GL.GL_COLOR_BUFFER_BIT);
            float[] lightPosition = { (float) (cameraPosition.x * 2), (float) (cameraPosition.y / 2), (float) (cameraPosition.z), 0.0f };
            float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
            float[] lightAmbient = { 1.0f, 1.0f, 1.0f, 1.0f };
            float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
            gl.glDisable(GL.GL_COLOR_MATERIAL);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
            gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);
            gl.glDisable(GL.GL_LIGHT0);
            gl.glEnable(GL.GL_LIGHT1);
            gl.glEnable(GL.GL_LIGHTING);
            gl.glEnable(GL.GL_NORMALIZE);
            dc.getGL().glEnable(GL.GL_BLEND);
            dc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        this.previousAttributes = null;
    }

    protected void end(DrawContext dc) {
        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        if (dc.isPickingMode()) {
            this.pickSupport.endPicking(dc);
        } else {
            gl.glDisable(GL.GL_LIGHT1);
            gl.glEnable(GL.GL_LIGHT0);
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_NORMALIZE);
        }
        gl.glPopAttrib();
    }

    protected boolean intersectsFrustum(DrawContext dc, Vec4 point, double radius) {
        if (dc.isPickingMode()) return dc.getPickFrustums().intersectsAny(new Sphere(point, radius));
        return dc.getView().getFrustumInModelCoordinates().contains(point);
    }

    protected Vec4 computeSurfacePoint(DrawContext dc, Position pos) {
        double ve = dc.getVerticalExaggeration();
        if (!this.overrideMarkerElevation) return dc.getGlobe().computePointFromPosition(pos, pos.getElevation() * ve);
        Vec4 point = dc.getSurfaceGeometry().getSurfacePoint(pos.getLatitude(), pos.getLongitude(), this.elevation * ve);
        if (point != null) return point;
        return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), this.elevation * ve);
    }

    protected double computeMarkerRadius(DrawContext dc, Vec4 point, Marker marker) {
        double d = point.distanceTo3(dc.getView().getEyePoint());
        double radius = marker.getAttributes().getMarkerPixels() * dc.getView().computePixelSizeAtDistance(d);
        if (radius < marker.getAttributes().getMinMarkerSize()) radius = marker.getAttributes().getMinMarkerSize(); else if (radius > marker.getAttributes().getMaxMarkerSize()) radius = marker.getAttributes().getMaxMarkerSize();
        return radius;
    }

    protected class OrderedMarker implements OrderedRenderable {

        protected int index;

        protected Marker marker;

        protected Vec4 point;

        protected double radius;

        protected Layer layer;

        protected double eyeDistance;

        public OrderedMarker(int index, Marker marker, Vec4 point, double radius, Layer layer, double eyeDistance) {
            this.index = index;
            this.marker = marker;
            this.point = point;
            this.radius = radius;
            this.layer = layer;
            this.eyeDistance = eyeDistance;
        }

        public MarkerRenderer getRenderer() {
            return MarkerRenderer.this;
        }

        public double getDistanceFromEye() {
            return this.eyeDistance;
        }

        public void pick(DrawContext dc, Point pickPoint) {
            MarkerRenderer.this.begin(dc);
            try {
                MarkerRenderer.this.pickOrderedMarkers(dc, this);
            } catch (Exception e) {
                Logging.logger().log(Level.SEVERE, Logging.getMessage("generic.ExceptionWhilePickingMarker", this), e);
            } finally {
                MarkerRenderer.this.end(dc);
                MarkerRenderer.this.pickSupport.resolvePick(dc, pickPoint, this.layer);
            }
        }

        public void render(DrawContext dc) {
            MarkerRenderer.this.begin(dc);
            try {
                MarkerRenderer.this.drawOrderedMarkers(dc, this);
            } catch (Exception e) {
                Logging.logger().log(Level.SEVERE, Logging.getMessage("generic.ExceptionWhileRenderingMarker", this), e);
            } finally {
                MarkerRenderer.this.end(dc);
            }
        }
    }

    protected void drawOrderedMarkers(DrawContext dc, OrderedMarker uMarker) {
        this.drawMarker(dc, uMarker.index, uMarker.marker, uMarker.point, uMarker.radius);
        Object next = dc.peekOrderedRenderables();
        while (next != null && next instanceof OrderedMarker && ((OrderedMarker) next).getRenderer() == this) {
            dc.pollOrderedRenderables();
            OrderedMarker om = (OrderedMarker) next;
            this.drawMarker(dc, om.index, om.marker, om.point, om.radius);
            next = dc.peekOrderedRenderables();
        }
    }

    protected void pickOrderedMarkers(DrawContext dc, OrderedMarker uMarker) {
        this.drawMarker(dc, uMarker.index, uMarker.marker, uMarker.point, uMarker.radius);
        Object next = dc.peekOrderedRenderables();
        while (next != null && next instanceof OrderedMarker && ((OrderedMarker) next).getRenderer() == this && ((OrderedMarker) next).layer == uMarker.layer) {
            dc.pollOrderedRenderables();
            OrderedMarker om = (OrderedMarker) next;
            this.drawMarker(dc, om.index, om.marker, om.point, om.radius);
            next = dc.peekOrderedRenderables();
        }
    }
}
