package gov.nasa.worldwindx.examples.shapebuilder;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.util.ShapeUtils;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import java.awt.*;

/**
 * @author ccrick
 * @version $Id: PyramidEditor.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class PyramidEditor extends RigidShapeEditor {

    @Override
    protected void assembleScaleControlPoints(DrawContext dc) {
        RigidShape shape = this.getShape();
        Matrix matrix = shape.computeRenderMatrix(dc);
        Vec4 refPt = shape.computeReferencePoint(dc);
        Position refPos = shape.getReferencePosition();
        double radius = ShapeUtils.getViewportScaleFactor(wwd) / 12;
        if (controlPoints.size() > 0) {
            for (RigidShape controlPoint : controlPoints) {
                controlPoint.setEastWestRadius(radius);
                controlPoint.setNorthSouthRadius(radius);
                controlPoint.setVerticalRadius(radius);
            }
        } else {
            Vec4 vert = matrix.transformBy3(matrix, 0.5, 0, 0).add3(refPt);
            Position vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            RigidShape controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_EAST_ACTION);
            this.controlPoints.add(controlPoint);
            Path rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
            vert = matrix.transformBy3(matrix, 0, 0.5, 0).add3(refPt);
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_NORTH_ACTION);
            this.controlPoints.add(controlPoint);
            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
            vert = matrix.transformBy3(matrix, 0, 0, 1).add3(refPt);
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_UP_ACTION);
            this.controlPoints.add(controlPoint);
            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
            vert = matrix.transformBy3(matrix, -0.5, 0, 0).add3(refPt);
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_WEST_ACTION);
            this.controlPoints.add(controlPoint);
            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
            vert = matrix.transformBy3(matrix, 0, -0.5, 0).add3(refPt);
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_SOUTH_ACTION);
            this.controlPoints.add(controlPoint);
            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
            vert = matrix.transformBy3(matrix, 0, 0, -1).add3(refPt);
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_DOWN_ACTION);
            this.controlPoints.add(controlPoint);
            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
            vert = matrix.transformBy3(matrix, 1, 1, -1).add3(refPt);
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_RADIUS_ACTION);
            this.controlPoints.add(controlPoint);
            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.radiusRodAttributes);
            this.controlPointRods.add(rod);
        }
    }

    @Override
    protected void assembleTextureControlPoints(DrawContext dc) {
        RigidShape shape = this.getShape();
        Matrix matrix = shape.computeRenderMatrix(dc);
        Vec4 refPt = shape.computeReferencePoint(dc);
        double radius = ShapeUtils.getViewportScaleFactor(wwd) / 12;
        Vec4 ULeft = new Vec4(0, 0, 0);
        Vec4 URight = new Vec4(0, 0, 0);
        Vec4 LLeft = new Vec4(0, 0, 0);
        Vec4 LRight = new Vec4(0, 0, 0);
        if (selectedFace == 0) {
            ULeft = new Vec4(1, -1 + shape.getOffsets(0, 0)[0], 1 + shape.getOffsets(0, 0)[1]);
            URight = new Vec4(1, 1 + shape.getOffsets(0, 1)[0], 1 + shape.getOffsets(0, 1)[1]);
            LLeft = new Vec4(1, -1 + shape.getOffsets(0, 2)[0], -1 + shape.getOffsets(0, 2)[1]);
            LRight = new Vec4(1, 1 + shape.getOffsets(0, 3)[0], -1 + shape.getOffsets(0, 3)[1]);
        } else if (selectedFace == 1) {
            ULeft = new Vec4(-1 + shape.getOffsets(1, 0)[0], -1, 1 + shape.getOffsets(1, 0)[1]);
            URight = new Vec4(1 + shape.getOffsets(1, 1)[0], -1, 1 + shape.getOffsets(1, 1)[1]);
            LLeft = new Vec4(-1 + shape.getOffsets(1, 2)[0], -1, -1 + shape.getOffsets(1, 2)[1]);
            LRight = new Vec4(1 + shape.getOffsets(1, 3)[0], -1, -1 + shape.getOffsets(1, 3)[1]);
        } else if (selectedFace == 2) {
            ULeft = new Vec4(-1, 1 - shape.getOffsets(2, 0)[0], 1 + shape.getOffsets(2, 0)[1]);
            URight = new Vec4(-1, -1 - shape.getOffsets(2, 1)[0], 1 + shape.getOffsets(2, 1)[1]);
            LLeft = new Vec4(-1, 1 - shape.getOffsets(2, 2)[0], -1 + shape.getOffsets(2, 2)[1]);
            LRight = new Vec4(-1, -1 - shape.getOffsets(2, 3)[0], -1 + shape.getOffsets(2, 3)[1]);
        } else if (selectedFace == 3) {
            ULeft = new Vec4(1 - shape.getOffsets(3, 0)[0], 1, 1 + shape.getOffsets(3, 0)[1]);
            URight = new Vec4(-1 - shape.getOffsets(3, 1)[0], 1, 1 + shape.getOffsets(3, 1)[1]);
            LLeft = new Vec4(1 - shape.getOffsets(3, 2)[0], 1, -1 + shape.getOffsets(3, 2)[1]);
            LRight = new Vec4(-1 - shape.getOffsets(3, 3)[0], 1, -1 + shape.getOffsets(3, 3)[1]);
        } else if (selectedFace == 4) {
            ULeft = new Vec4(1 - shape.getOffsets(4, 0)[0], 1 + shape.getOffsets(4, 0)[1], -1);
            URight = new Vec4(-1 - shape.getOffsets(4, 1)[0], 1 + shape.getOffsets(4, 1)[1], -1);
            LLeft = new Vec4(1 - shape.getOffsets(4, 2)[0], -1 + shape.getOffsets(4, 2)[1], -1);
            LRight = new Vec4(-1 - shape.getOffsets(4, 3)[0], -1 + shape.getOffsets(4, 3)[1], -1);
        }
        Vec4 top = ULeft.add3(URight).divide3(2);
        Vec4 bottom = LLeft.add3(LRight).divide3(2);
        Vec4 left = ULeft.add3(LLeft).divide3(2);
        Vec4 right = URight.add3(LRight).divide3(2);
        Vec4 center = left.add3(right).divide3(2);
        if (controlPoints.size() > 0) {
            for (RigidShape controlPoint : controlPoints) {
                controlPoint.setEastWestRadius(radius);
                controlPoint.setNorthSouthRadius(radius);
                controlPoint.setVerticalRadius(radius);
            }
        } else {
            Vec4 vert = matrix.transformBy3(matrix, ULeft.getX(), ULeft.getY(), ULeft.getZ()).add3(refPt);
            Position ULvertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            RigidShape controlPoint = new Ellipsoid(ULvertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.textureControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_UPPER_LEFT_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, URight.getX(), URight.getY(), URight.getZ()).add3(refPt);
            Position URvertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(URvertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.textureControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_UPPER_RIGHT_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, LLeft.getX(), LLeft.getY(), LLeft.getZ()).add3(refPt);
            Position LLvertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(LLvertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.textureControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_LOWER_LEFT_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, LRight.getX(), LRight.getY(), LRight.getZ()).add3(refPt);
            Position LRvertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(LRvertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.textureControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_LOWER_RIGHT_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, right.getX(), right.getY(), right.getZ()).add3(refPt);
            Position pos = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(pos, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_SCALE_RIGHT_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, left.getX(), left.getY(), left.getZ()).add3(refPt);
            pos = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(pos, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_SCALE_LEFT_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, top.getX(), top.getY(), top.getZ()).add3(refPt);
            pos = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(pos, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_SCALE_UP_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, bottom.getX(), bottom.getY(), bottom.getZ()).add3(refPt);
            pos = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(pos, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_SCALE_DOWN_ACTION);
            this.controlPoints.add(controlPoint);
            vert = matrix.transformBy3(matrix, center.getX(), center.getY(), center.getZ()).add3(refPt);
            pos = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(pos, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, TEXTURE_MOVE_ACTION);
            Path rod = new Path(ULvertexPosition, URvertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            rod.setVisible(true);
            this.controlPointRods.add(rod);
            rod = new Path(URvertexPosition, LRvertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            rod.setVisible(true);
            this.controlPointRods.add(rod);
            rod = new Path(LRvertexPosition, LLvertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            rod.setVisible(true);
            this.controlPointRods.add(rod);
            rod = new Path(LLvertexPosition, ULvertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            rod.setVisible(true);
            this.controlPointRods.add(rod);
        }
    }

    @Override
    protected void scaleShapeNorth(Point previousMousePoint, Point mousePoint) {
        scaleShapeNorthSouth(previousMousePoint, mousePoint, SCALE_NORTH_ACTION);
    }

    @Override
    protected void scaleShapeSouth(Point previousMousePoint, Point mousePoint) {
        scaleShapeNorthSouth(previousMousePoint, mousePoint, SCALE_SOUTH_ACTION);
    }

    protected void scaleShapeNorthSouth(Point previousMousePoint, Point mousePoint, String scaleDirection) {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);
        Position controlPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 scaleVector = controlPoint.subtract3(referencePoint).normalize3();
        if (scaleDirection.equals(SCALE_SOUTH_ACTION)) scaleVector = scaleVector.getNegative3();
        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;
        RigidShape shape = this.getShape();
        double radius = shape.getNorthSouthRadius();
        if (radius + radiusChange > 0) {
            this.shape.setNorthSouthRadius(radius + radiusChange / 2);
            Vec4 newCenterPt = referencePoint.add3(scaleVector.multiply3(radiusChange / 2));
            Position newCenterPos = this.wwd.getModel().getGlobe().computePositionFromPoint(newCenterPt);
            this.shape.setCenterPosition(newCenterPos);
        }
    }

    @Override
    protected void scaleShapeEast(Point previousMousePoint, Point mousePoint) {
        scaleShapeEastWest(previousMousePoint, mousePoint, SCALE_EAST_ACTION);
    }

    @Override
    protected void scaleShapeWest(Point previousMousePoint, Point mousePoint) {
        scaleShapeEastWest(previousMousePoint, mousePoint, SCALE_WEST_ACTION);
    }

    protected void scaleShapeEastWest(Point previousMousePoint, Point mousePoint, String scaleDirection) {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);
        Position controlPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 scaleVector = controlPoint.subtract3(referencePoint).normalize3();
        if (scaleDirection.equals(SCALE_WEST_ACTION)) scaleVector = scaleVector.getNegative3();
        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;
        RigidShape shape = this.getShape();
        double radius = shape.getEastWestRadius();
        if (radius + radiusChange > 0) {
            this.shape.setEastWestRadius(radius + radiusChange / 2);
            Vec4 newCenterPt = referencePoint.add3(scaleVector.multiply3(radiusChange / 2));
            Position newCenterPos = this.wwd.getModel().getGlobe().computePositionFromPoint(newCenterPt);
            this.shape.setCenterPosition(newCenterPos);
        }
    }

    @Override
    protected void scaleShapeUp(Point previousMousePoint, Point mousePoint) {
        scaleShapeVertical(previousMousePoint, mousePoint, SCALE_UP_ACTION);
    }

    @Override
    protected void scaleShapeDown(Point previousMousePoint, Point mousePoint) {
        scaleShapeVertical(previousMousePoint, mousePoint, SCALE_DOWN_ACTION);
    }

    protected void scaleShapeVertical(Point previousMousePoint, Point mousePoint, String scaleDirection) {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 scaleVector = controlPoint.subtract3(referencePoint).normalize3();
        if (scaleDirection.equals(SCALE_DOWN_ACTION)) scaleVector = scaleVector.getNegative3();
        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;
        RigidShape shape = this.getShape();
        double radius = shape.getVerticalRadius();
        if (radius + radiusChange > 0) {
            this.shape.setVerticalRadius(radius + radiusChange / 2);
            Vec4 newCenterPt = referencePoint.add3(scaleVector.multiply3(radiusChange / 2));
            Position newCenterPos = this.wwd.getModel().getGlobe().computePositionFromPoint(newCenterPt);
            this.shape.setCenterPosition(newCenterPos);
        }
    }

    @Override
    protected void scaleShapeRadius(Point previousMousePoint, Point mousePoint) {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;
        RigidShape shape = this.getShape();
        double eastWestRadius = shape.getEastWestRadius();
        double northSouthRadius = shape.getNorthSouthRadius();
        double average = (eastWestRadius + northSouthRadius) / 2;
        double scalingRatio = (radiusChange + average) / average;
        if (scalingRatio > 0) {
            this.shape.setEastWestRadius(eastWestRadius * scalingRatio);
            this.shape.setNorthSouthRadius(northSouthRadius * scalingRatio);
        }
    }

    @Override
    protected void scaleShape(Point previousMousePoint, Point mousePoint) {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 scaleVector = controlPoint.subtract3(referencePoint).normalize3();
        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;
        RigidShape shape = this.getShape();
        double eastWestRadius = shape.getEastWestRadius();
        double northSouthRadius = shape.getNorthSouthRadius();
        double verticalRadius = shape.getVerticalRadius();
        double average = (eastWestRadius + northSouthRadius + verticalRadius) / 3;
        double scalingRatio = (radiusChange + average) / average;
        if (scalingRatio > 0) {
            this.shape.setEastWestRadius(eastWestRadius * scalingRatio);
            this.shape.setNorthSouthRadius(northSouthRadius * scalingRatio);
            this.shape.setVerticalRadius(verticalRadius * scalingRatio);
            scaleVector = scaleVector.multiply3(this.shape.getVerticalRadius() - verticalRadius);
            Vec4 newCenterPt = referencePoint.add3(scaleVector);
            Position newCenterPos = this.wwd.getModel().getGlobe().computePositionFromPoint(newCenterPt);
            this.shape.setCenterPosition(newCenterPos);
        }
    }

    @Override
    protected void skewShapeEastWest(Point previousMousePoint, Point mousePoint) {
        RigidShape shape = this.getShape();
        double skew = shape.getSkewEastWest().getDegrees();
        double scale = ShapeUtils.getViewportScaleFactor(wwd);
        Matrix renderMatrix = this.shape.computeRenderMatrix(this.wwd.getModel().getGlobe(), this.wwd.getSceneController().getVerticalExaggeration());
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 controlVector = controlPoint.subtract3(referencePoint).normalize3();
        Position northPosition = this.controlPoints.get(3).getCenterPosition();
        Vec4 northPoint = this.wwd.getModel().getGlobe().computePointFromPosition(northPosition);
        Vec4 northVector = northPoint.subtract3(referencePoint).normalize3();
        Position frontPosition = this.controlPoints.get(4).getCenterPosition();
        Vec4 frontPoint = this.wwd.getModel().getGlobe().computePointFromPosition(frontPosition);
        Vec4 frontVector = frontPoint.subtract3(referencePoint).normalize3();
        Vec4 p1 = referencePoint.add3(controlVector.multiply3(this.shape.getEastWestRadius()));
        Vec4 p2 = p1.add3(northVector);
        Vec4 p3 = p1.add3(frontVector);
        Plane splitPlane = Plane.fromPoints(p1, p2, p3);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(p1);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(p1);
        double distance = nearestPointOnLine.distanceTo3(p1);
        double previousDistance = previousNearestPointOnLine.distanceTo3(p1);
        double skewChange = (distance - previousDistance) / scale;
        skewChange *= 1 - Math.abs(skew - 90) / 90;
        skewChange *= 50;
        int west = splitPlane.onSameSide(referencePoint, nearestPointOnLine);
        if (west != 0) skewChange *= -1;
        if (skew + skewChange >= 0 && skew + skewChange < 180) this.shape.setSkewEastWest(Angle.fromDegrees(skew + skewChange));
        Vec4 bottomPoint = renderMatrix.transformBy3(renderMatrix, 0, 0, -1);
        Matrix newRenderMatrix = this.shape.computeRenderMatrix(this.wwd.getModel().getGlobe(), this.wwd.getSceneController().getVerticalExaggeration());
        Vec4 newBottomPoint = newRenderMatrix.transformBy3(newRenderMatrix, 0, 0, -1);
        Vec4 delta = newBottomPoint.subtract3(bottomPoint);
        referencePoint = referencePoint.subtract3(delta);
        Position newReferencePosition = this.wwd.getModel().getGlobe().computePositionFromPoint(referencePoint);
        this.shape.setCenterPosition(newReferencePosition);
    }

    @Override
    protected void skewShapeNorthSouth(Point previousMousePoint, Point mousePoint) {
        RigidShape shape = this.getShape();
        double skew = shape.getSkewNorthSouth().getDegrees();
        double scale = ShapeUtils.getViewportScaleFactor(wwd);
        Matrix renderMatrix = this.shape.computeRenderMatrix(this.wwd.getModel().getGlobe(), this.wwd.getSceneController().getVerticalExaggeration());
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null) return;
        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);
        Position controlPosition = this.controlPoints.get(3).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 controlVector = controlPoint.subtract3(referencePoint).normalize3();
        Position eastPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 eastPoint = this.wwd.getModel().getGlobe().computePointFromPosition(eastPosition);
        Vec4 eastVector = eastPoint.subtract3(referencePoint).normalize3();
        Position frontPosition = this.controlPoints.get(4).getCenterPosition();
        Vec4 frontPoint = this.wwd.getModel().getGlobe().computePointFromPosition(frontPosition);
        Vec4 frontVector = frontPoint.subtract3(referencePoint).normalize3();
        Vec4 p1 = referencePoint.add3(controlVector.multiply3(this.shape.getNorthSouthRadius()));
        Vec4 p2 = p1.add3(eastVector);
        Vec4 p3 = p1.add3(frontVector);
        Plane splitPlane = Plane.fromPoints(p1, p2, p3);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Vec4 nearestPointOnLine = screenRay.nearestPointTo(p1);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(p1);
        double distance = nearestPointOnLine.distanceTo3(p1);
        double previousDistance = previousNearestPointOnLine.distanceTo3(p1);
        double skewChange = (distance - previousDistance) / scale;
        skewChange *= 1 - Math.abs(skew - 90) / 90;
        skewChange *= 50;
        int south = splitPlane.onSameSide(referencePoint, nearestPointOnLine);
        if (south != 0) skewChange *= -1;
        if (skew + skewChange >= 0 && skew + skewChange < 180) this.shape.setSkewNorthSouth(Angle.fromDegrees(skew + skewChange));
        Vec4 bottomPoint = renderMatrix.transformBy3(renderMatrix, 0, 0, -1);
        Matrix newRenderMatrix = this.shape.computeRenderMatrix(this.wwd.getModel().getGlobe(), this.wwd.getSceneController().getVerticalExaggeration());
        Vec4 newBottomPoint = newRenderMatrix.transformBy3(newRenderMatrix, 0, 0, -1);
        Vec4 delta = newBottomPoint.subtract3(bottomPoint);
        referencePoint = referencePoint.subtract3(delta);
        Position newReferencePosition = this.wwd.getModel().getGlobe().computePositionFromPoint(referencePoint);
        this.shape.setCenterPosition(newReferencePosition);
    }

    @Override
    protected void moveTextureCorner(Point previousMousePoint, Point mousePoint, Integer corner) {
        Vec4 rightRay = new Vec4(0, 0, 0);
        Vec4 upRay = new Vec4(0, 0, 0);
        if (this.selectedFace == 0) {
            rightRay = new Vec4(0, 1, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 1) {
            rightRay = new Vec4(1, 0, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 2) {
            rightRay = new Vec4(0, -1, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 3) {
            rightRay = new Vec4(-1, 0, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 4) {
            rightRay = new Vec4(-1, 0, 0);
            upRay = new Vec4(0, 1, 0);
        }
        Matrix inverseRenderMatrix = this.shape.computeRenderMatrixInverse(this.wwd.getModel().getGlobe(), this.wwd.getSceneController().getVerticalExaggeration());
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Position controlPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Position coplanarPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 coplanarPoint = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition);
        Position coplanarPosition2 = this.controlPoints.get(2).getCenterPosition();
        Vec4 coplanarPoint2 = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition2);
        Plane controlPlane = Plane.fromPoints(controlPoint, coplanarPoint, coplanarPoint2);
        Vec4 pointOnPlane = controlPlane.intersect(screenRay);
        Vec4 previousPointOnPlane = controlPlane.intersect(previousScreenRay);
        Vec4 changeVector = pointOnPlane.subtract3(previousPointOnPlane);
        Vec4 localChange = inverseRenderMatrix.transformBy3(inverseRenderMatrix, changeVector.getX(), changeVector.getY(), changeVector.getZ());
        float[] newOffset = { 0.0f, 0.0f };
        float[] prevOffset = shape.getOffsets(selectedFace, corner);
        newOffset[0] = (float) (prevOffset[0] + rightRay.dot3(localChange));
        newOffset[1] = (float) (prevOffset[1] + upRay.dot3(localChange));
        shape.setOffset(selectedFace, corner, newOffset[0], newOffset[1]);
        switch(corner) {
            case 0:
                corner = 3;
                break;
            case 1:
                corner = 2;
                break;
            case 2:
                corner = 1;
                break;
            case 3:
                corner = 0;
        }
        prevOffset = shape.getOffsets(selectedFace, corner);
        newOffset[0] = (float) (prevOffset[0] - rightRay.dot3(localChange));
        newOffset[1] = (float) (prevOffset[1] - upRay.dot3(localChange));
        shape.setOffset(selectedFace, corner, newOffset[0], newOffset[1]);
    }

    @Override
    protected void scaleTexture(Point previousMousePoint, Point mousePoint, Direction side) {
        Vec4 rightRay = new Vec4(0, 0, 0);
        Vec4 upRay = new Vec4(0, 0, 0);
        int corner1 = 0;
        int corner2 = 0;
        if (this.selectedFace == 0) {
            rightRay = new Vec4(0, 1, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 1) {
            rightRay = new Vec4(1, 0, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 2) {
            rightRay = new Vec4(0, -1, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 3) {
            rightRay = new Vec4(-1, 0, 0);
            upRay = new Vec4(0, 0, 1);
        } else if (this.selectedFace == 4) {
            rightRay = new Vec4(-1, 0, 0);
            upRay = new Vec4(0, 1, 0);
        }
        switch(side) {
            case RIGHT:
                corner1 = 1;
                corner2 = 3;
                break;
            case LEFT:
                corner1 = 2;
                corner2 = 0;
                break;
            case UP:
                corner1 = 0;
                corner2 = 1;
                break;
            case DOWN:
                corner1 = 3;
                corner2 = 2;
        }
        Matrix inverseRenderMatrix = this.shape.computeRenderMatrixInverse(this.wwd.getModel().getGlobe(), this.wwd.getSceneController().getVerticalExaggeration());
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Position controlPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Position coplanarPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 coplanarPoint = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition);
        Position coplanarPosition2 = this.controlPoints.get(2).getCenterPosition();
        Vec4 coplanarPoint2 = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition2);
        Plane controlPlane = Plane.fromPoints(controlPoint, coplanarPoint, coplanarPoint2);
        Vec4 pointOnPlane = controlPlane.intersect(screenRay);
        Vec4 previousPointOnPlane = controlPlane.intersect(previousScreenRay);
        Vec4 changeVector = pointOnPlane.subtract3(previousPointOnPlane);
        Vec4 localChange = inverseRenderMatrix.transformBy3(inverseRenderMatrix, changeVector.getX(), changeVector.getY(), changeVector.getZ());
        float[] newOffset = { 0.0f, 0.0f };
        double rightOffset = rightRay.dot3(localChange);
        double upOffset = upRay.dot3(localChange);
        float[] prevOffset = shape.getOffsets(selectedFace, corner1);
        if (side == Direction.RIGHT || side == Direction.LEFT) {
            newOffset[0] = (float) (prevOffset[0] + rightOffset);
            shape.setOffset(selectedFace, corner1, newOffset[0], prevOffset[1]);
        } else {
            newOffset[1] = (float) (prevOffset[1] + upOffset);
            shape.setOffset(selectedFace, corner1, prevOffset[0], newOffset[1]);
        }
        prevOffset = shape.getOffsets(selectedFace, corner2);
        if (side == Direction.RIGHT || side == Direction.LEFT) {
            newOffset[0] = (float) (prevOffset[0] + rightOffset);
            shape.setOffset(selectedFace, corner2, newOffset[0], prevOffset[1]);
        } else {
            newOffset[1] = (float) (prevOffset[1] + upOffset);
            shape.setOffset(selectedFace, corner2, prevOffset[0], newOffset[1]);
        }
    }
}
