package jbotrace.base;

/**
 * <p>Description: Curve is a subclass of part and descriptes a curve in the track.</p>
 */
public class Curve extends Segment {

    Vector2d center;

    double radius;

    double endAng;

    double endWidth;

    double startAng;

    double startWidth;

    /** Construcst a new curce with the given parameters. */
    public Curve(Vector2d center, double radius, double startWidth, double endWidth, double startAng, double endAng) {
        this.center = center;
        this.radius = radius;
        this.endAng = endAng;
        this.endWidth = endWidth;
        this.startWidth = startWidth;
        this.startAng = startAng;
    }

    /** Returns the box bounding of the segment */
    public Vector2d[] getBoundingBox() {
        Vector2d[] edges = new Vector2d[8];
        double width = startWidth;
        edges[0] = new Vector2d(center.x() + Math.cos(startAng) * (radius + width / 2), center.y() + Math.sin(startAng) * (radius + width / 2));
        edges[1] = new Vector2d(center.x() + Math.cos(startAng) * (radius - width / 2), center.y() + Math.sin(startAng) * (radius - width / 2));
        edges[2] = new Vector2d(center.x() + Math.cos(endAng) * (radius + width / 2), center.y() + Math.sin(endAng) * (radius + width / 2));
        edges[3] = new Vector2d(center.x() + Math.cos(endAng) * (radius - width / 2), center.y() + Math.sin(endAng) * (radius - width / 2));
        if (isOnSegment(new Vector2d(center.x() + radius, center.y()))) edges[4] = new Vector2d(center.x() + radius + width / 2, center.y());
        if (isOnSegment(new Vector2d(center.x() - radius, center.y()))) edges[5] = new Vector2d(center.x() - radius - width / 2, center.y());
        if (isOnSegment(new Vector2d(center.x(), center.y() + radius))) edges[6] = new Vector2d(center.x(), center.y() + radius + width / 2);
        if (isOnSegment(new Vector2d(center.x(), center.y() - radius))) edges[7] = new Vector2d(center.x(), center.y() - radius - width / 2);
        double minx = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = Double.MIN_VALUE;
        for (int i = 0; i < 8; i++) {
            if (edges[i] != null) {
                if (edges[i].x() < minx) minx = edges[i].x();
                if (edges[i].x() > maxx) maxx = edges[i].x();
                if (edges[i].y() < miny) miny = edges[i].y();
                if (edges[i].y() > maxy) maxy = edges[i].y();
            }
        }
        Vector2d[] boundingBox = new Vector2d[2];
        boundingBox[0] = new Vector2d(minx, miny);
        boundingBox[1] = new Vector2d(maxx, maxy);
        return boundingBox;
    }

    /** Returns the center of the curve */
    public Vector2d getCenter() {
        return center;
    }

    /** Returns the distance to the end of the curve from the given position */
    public double getDistanceToEnd(Vector2d pos) {
        double angle = new Vector2d(pos).sub(center).getDir();
        while (angle < startAng && angle < endAng) angle += Math.PI * 2;
        while (angle > startAng && angle > endAng) angle -= Math.PI * 2;
        if ((angle > startAng && angle > endAng) || (angle < startAng && angle < endAng)) return 0;
        double angleToEnd = Math.abs(angle - endAng);
        return (angleToEnd / (2 * Math.PI)) * 2 * Math.PI * radius;
    }

    /** Returns the distance to the left side of the curve at the given position */
    public double getDistanceToLeft(Vector2d pos) {
        double distanceToCenter = pos.distanceTo(center);
        if (startAng > endAng) {
            return distanceToCenter - radius + startWidth / 2;
        } else {
            return radius + startWidth / 2 - distanceToCenter;
        }
    }

    /** Returns the distance to the middle of the curve */
    public double getDistanceToMiddle(Vector2d pos) {
        return Math.abs(pos.distanceTo(center) - radius);
    }

    /** Returns the distance to the right side of the curve at the given position */
    public double getDistanceToRight(Vector2d pos) {
        double distanceToCenter = pos.distanceTo(center);
        if (startAng > endAng) {
            return radius + startWidth / 2 - distanceToCenter;
        } else {
            return distanceToCenter - radius + startWidth / 2;
        }
    }

    /** Returns the distance of the given point to the side of the curve or
   *  the maximum value for double, if the point is not beside the curve */
    public double getDistanceToSide(Vector2d pos) {
        double angle = new Vector2d(pos).sub(center).getDir();
        while (angle < startAng && angle < endAng) angle += Math.PI * 2;
        while (angle > startAng && angle > endAng) angle -= Math.PI * 2;
        if ((angle > startAng && angle > endAng) || (angle < startAng && angle < endAng)) return Double.MAX_VALUE;
        double distance = pos.distanceTo(center) - radius;
        if (distance < startWidth / 2) distance = 0; else distance -= startWidth / 2;
        return distance;
    }

    /** Returns the angle at the end of the curve */
    public double getEndAng() {
        return endAng;
    }

    /** Returns the width at the end of the curve*/
    public double getEndWidth() {
        return endWidth;
    }

    /** Calculates and returns the length of the curve */
    public double getLength() {
        return (Math.abs(endAng - startAng) / (2 * Math.PI)) * 2 * Math.PI * radius;
    }

    /** Returns the middle of the represented part of the track. */
    public Vector2d getMiddle() {
        Vector2d middle = new Vector2d();
        double endAng = this.endAng;
        double middleAng = (startAng + endAng) / 2;
        double middleX = center.x() + Math.cos(middleAng) * radius;
        double middleY = center.y() + Math.sin(middleAng) * radius;
        middle.set(middleX, middleY);
        return middle;
    }

    /** Returns the radius of the curve */
    public double getRadius() {
        return radius;
    }

    /** Returns the angle at the beginning of the curve */
    public double getStartAng() {
        return startAng;
    }

    /** Returns the width at the beginning of the curve */
    public double getStartWidth() {
        return startWidth;
    }

    /** Returns the type number of the segment. */
    public int getType() {
        return Segment.CURVE;
    }

    /** Return the width of the curve at the given position */
    public double getWidth(Vector2d pos) {
        System.out.println("Not implemented method getWidth, in Curve");
        return 0;
    }

    /** Returns true of the given position is on the part and false otherwise. */
    public boolean isOnSegment(Vector2d pos) {
        double dist = pos.distanceTo(center);
        double width = startWidth;
        double innerRad = radius - width / 2;
        double outerRad = radius + width / 2;
        if (dist < innerRad || dist > outerRad) return false;
        double angle = new Vector2d(pos).sub(center).getDir();
        while (angle < startAng && angle < endAng) angle += Math.PI * 2;
        while (angle > startAng && angle > endAng) angle -= Math.PI * 2;
        if ((angle > startAng && angle > endAng) || (angle < startAng && angle < endAng)) return false;
        return true;
    }

    /** Moves the segment */
    public void move(Vector2d moveVec) {
        center.add(moveVec);
    }
}
