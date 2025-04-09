package org.jnet.shape;

import java.io.Serializable;
import org.jnet.g3d.*;
import org.jnet.modelset.Measurement;
import org.jnet.modelset.MeasurementPending;
import org.jnet.util.Point3fi;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Matrix3f;
import javax.vecmath.AxisAngle4f;

public class MeasuresRenderer extends FontLineShapeRenderer implements Serializable {

    private short measurementMad;

    private transient Font3D font3d;

    private Measurement measurement;

    private boolean doJustify;

    protected void render() {
        if (!g3d.checkTranslucent(false)) return;
        imageFontScaling = viewer.getImageFontScaling();
        Measures measures = (Measures) shape;
        doJustify = viewer.getJustifyMeasurements();
        measurementMad = measures.mad;
        font3d = g3d.getFont3DScaled(measures.getFont3D(), imageFontScaling);
        renderPendingMeasurement(measures.measurementPending);
        if (!viewer.getShowMeasurements()) return;
        boolean showMeasurementLabels = viewer.getShowMeasurementLabels();
        boolean dynamicMeasurements = viewer.getDynamicMeasurements();
        measures.setVisibilityInfo();
        for (int i = measures.measurementCount; --i >= 0; ) {
            Measurement m = measures.measurements[i];
            if (dynamicMeasurements || m.isDynamic()) m.refresh();
            if (!m.isVisible()) continue;
            colix = m.getColix();
            if (colix == 0) colix = measures.colix;
            if (colix == 0) colix = viewer.getColixBackgroundContrast();
            g3d.setColix(colix);
            renderMeasurement(m.getCount(), m, showMeasurementLabels);
        }
    }

    Point3fi nodeA, nodeB, nodeC, nodeD;

    private Point3fi getNode(int i) {
        Point3fi a = measurement.getNode(i);
        if (a.screenDiameter < 0) {
            viewer.transformPoint(a, ptA);
            a.screenX = ptA.x;
            a.screenY = ptA.y;
            a.screenZ = ptA.z;
        }
        return a;
    }

    /**
   * this method reset the font3d used
   */
    private void resetFont3D() {
        imageFontScaling = viewer.getImageFontScaling();
        Measures measures = (Measures) shape;
        font3d = g3d.getFont3DScaled(measures.getFont3D(), imageFontScaling);
    }

    private void renderMeasurement(int count, Measurement measurement, boolean renderLabel) {
        this.measurement = measurement;
        switch(count) {
            case 2:
                nodeA = getNode(1);
                nodeB = getNode(2);
                renderDistance(renderLabel);
                break;
            case 3:
                nodeA = getNode(1);
                nodeB = getNode(2);
                nodeC = getNode(3);
                renderAngle(renderLabel);
                break;
            case 4:
                nodeA = getNode(1);
                nodeB = getNode(2);
                nodeC = getNode(3);
                nodeD = getNode(4);
                renderTorsion(renderLabel);
                break;
        }
        nodeA = nodeB = nodeC = nodeD = null;
    }

    private Point3i ptA = new Point3i();

    private Point3i ptB = new Point3i();

    private int drawSegment(int x1, int y1, int z1, int x2, int y2, int z2) {
        ptA.set(x1, y1, z1);
        ptB.set(x2, y2, z2);
        if (measurementMad < 0) {
            g3d.drawDashedLine(4, 2, ptA, ptB);
            return 1;
        }
        int widthPixels = measurementMad;
        if (measurementMad >= 20) widthPixels = viewer.scaleToScreen((z1 + z2) / 2, measurementMad);
        g3d.fillCylinder(Graphics3D.ENDCAPS_FLAT, widthPixels, ptA, ptB);
        return (widthPixels + 1) / 2;
    }

    void renderDistance(boolean renderLabel) {
        int zA = nodeA.screenZ - nodeA.screenDiameter - 10;
        int zB = nodeB.screenZ - nodeB.screenDiameter - 10;
        int radius = drawSegment(nodeA.screenX, nodeA.screenY, zA, nodeB.screenX, nodeB.screenY, zB);
        if (!renderLabel) return;
        int z = (zA + zB) / 2;
        if (z < 1) z = 1;
        int x = (nodeA.screenX + nodeB.screenX) / 2;
        int y = (nodeA.screenY + nodeB.screenY) / 2;
        paintMeasurementString(x, y, z, radius, (x - nodeA.screenX) * (y - nodeA.screenY) > 0, 0);
    }

    private AxisAngle4f aaT = new AxisAngle4f();

    private Matrix3f matrixT = new Matrix3f();

    private Point3f pointT = new Point3f();

    private void renderAngle(boolean renderLabel) {
        int zOffset = nodeB.screenDiameter + 10;
        int zA = nodeA.screenZ - nodeA.screenDiameter - 10;
        int zB = nodeB.screenZ - zOffset;
        int zC = nodeC.screenZ - nodeC.screenDiameter - 10;
        int radius = drawSegment(nodeA.screenX, nodeA.screenY, zA, nodeB.screenX, nodeB.screenY, zB);
        radius += drawSegment(nodeB.screenX, nodeB.screenY, zB, nodeC.screenX, nodeC.screenY, zC);
        if (!renderLabel) return;
        radius = (radius + 1) / 2;
        AxisAngle4f aa = measurement.getAxisAngle();
        if (aa == null) {
            int offset = (int) (5 * imageFontScaling);
            paintMeasurementString(nodeB.screenX + offset, nodeB.screenY - offset, zB, radius, false, 0);
            return;
        }
        int dotCount = (int) ((aa.angle / (2 * Math.PI)) * 64);
        float stepAngle = aa.angle / dotCount;
        aaT.set(aa);
        int iMid = dotCount / 2;
        Point3f ptArc = measurement.getPointArc();
        for (int i = dotCount; --i >= 0; ) {
            aaT.angle = i * stepAngle;
            matrixT.set(aaT);
            pointT.set(ptArc);
            matrixT.transform(pointT);
            pointT.add(nodeB);
            Point3i point3iScreenTemp = viewer.transformPoint(pointT);
            int zArc = point3iScreenTemp.z - zOffset;
            if (zArc < 0) zArc = 0;
            g3d.drawPixel(point3iScreenTemp.x, point3iScreenTemp.y, zArc);
            if (i == iMid) {
                pointT.set(ptArc);
                pointT.scale(1.1f);
                matrixT.transform(pointT);
                pointT.add(nodeB);
                viewer.transformPoint(pointT);
                int zLabel = point3iScreenTemp.z - zOffset;
                paintMeasurementString(point3iScreenTemp.x, point3iScreenTemp.y, zLabel, radius, point3iScreenTemp.x < nodeB.screenX, nodeB.screenY);
            }
        }
    }

    private void renderTorsion(boolean renderLabel) {
        int zA = nodeA.screenZ - nodeA.screenDiameter - 10;
        int zB = nodeB.screenZ - nodeB.screenDiameter - 10;
        int zC = nodeC.screenZ - nodeC.screenDiameter - 10;
        int zD = nodeD.screenZ - nodeD.screenDiameter - 10;
        int radius = drawSegment(nodeA.screenX, nodeA.screenY, zA, nodeB.screenX, nodeB.screenY, zB);
        radius += drawSegment(nodeB.screenX, nodeB.screenY, zB, nodeC.screenX, nodeC.screenY, zC);
        radius += drawSegment(nodeC.screenX, nodeC.screenY, zC, nodeD.screenX, nodeD.screenY, zD);
        if (!renderLabel) return;
        radius /= 3;
        paintMeasurementString((nodeA.screenX + nodeB.screenX + nodeC.screenX + nodeD.screenX) / 4, (nodeA.screenY + nodeB.screenY + nodeC.screenY + nodeD.screenY) / 4, (zA + zB + zC + zD) / 4, radius, false, 0);
    }

    private void paintMeasurementString(int x, int y, int z, int radius, boolean rightJustify, int yRef) {
        if (!doJustify) {
            rightJustify = false;
            yRef = y;
        }
        String strMeasurement = measurement.getString();
        if (strMeasurement == null) return;
        if (font3d == null) resetFont3D();
        int width = font3d.fontMetrics.stringWidth(strMeasurement);
        int height = font3d.fontMetrics.getAscent();
        int xT = x;
        if (rightJustify) xT -= radius / 2 + 2 + width; else xT += radius / 2 + 2;
        int yT = y + (yRef == 0 || yRef < y ? height : -radius / 2);
        int zT = z - radius - 2;
        if (zT < 1) zT = 1;
        g3d.drawString(strMeasurement, font3d, xT, yT, zT, zT);
    }

    private void renderPendingMeasurement(MeasurementPending measurementPending) {
        if (isGenerator || measurementPending == null) return;
        int count = measurementPending.getCount();
        if (count == 0) return;
        g3d.setColix(viewer.getColixRubberband());
        measurementPending.refresh();
        if (measurementPending.haveTarget()) renderMeasurement(count, measurementPending, true); else renderPendingWithCursor(count, measurementPending);
    }

    private void renderPendingWithCursor(int count, MeasurementPending measurementPending) {
        if (count > 1) renderMeasurement(count, measurementPending, false);
        measurement = measurementPending;
        Point3fi nodeLast = getNode(count);
        int lastZ = nodeLast.screenZ - nodeLast.screenDiameter - 10;
        int x = viewer.getCursorX();
        int y = viewer.getCursorY();
        if (g3d.isAntialiased()) {
            x <<= 1;
            y <<= 1;
        }
        drawSegment(nodeLast.screenX, nodeLast.screenY, lastZ, x, y, 0);
    }
}
