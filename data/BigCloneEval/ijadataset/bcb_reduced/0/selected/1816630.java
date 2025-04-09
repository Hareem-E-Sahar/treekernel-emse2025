package oss.jthinker.swingutils;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * Helper functions for computing and adjusting locations of
 * UI widgets.
 * 
 * @author iappel
 */
public class GeometryUtils {

    /**
     * Calculates location of area's center
     * 
     * @param r area 
     * @return area center location
     */
    public static Point computeCenterPoint(Rectangle r) {
        return new Point(r.x + r.width / 2, r.y + r.height / 2);
    }

    /**
     * Computes distance from a point on a plane to line given by two points.
     * 
     * @param p point, distance from which is to be calculated
     * @param endA one end of the line
     * @param endZ the other end of the line
     * @return distance from p to line, formed by endA and endZ
     */
    public static double distanceToLine(Point2D p, Point2D endA, Point2D endZ) {
        double AC = p.distance(endA);
        double BC = p.distance(endZ);
        double AB = endA.distance(endZ);
        if (AB == (AC + BC)) {
            return 0;
        }
        double ACs = AC * AC;
        double BCs = BC * BC;
        double AD_BD = (ACs - BCs) / AB;
        double AD = (AD_BD + AB) / 2;
        double CDs = ACs - (AD * AD);
        return Math.sqrt(CDs);
    }

    /**
     * Calculates the length of hypotenuse of right triangle from
     * given legs.
     * 
     * @param leg1 lenght of the first leg
     * @param leg2 length of the other leg
     * @return length of hypotenuse
     */
    public static double hypotenuse(double leg1, double leg2) {
        return Math.sqrt((leg1 * leg1) + (leg2 * leg2));
    }

    /**
     * Calculates sine of angle between first leg and hypotenuse
     * in right triangle with given legs.
     * 
     * @param leg1 length of the first leg (that forms the angle)
     * @param leg2 length of the other leg
     * @return sine of angle between first leg and hypotenuse.
     */
    public static double triangleSin(double leg1, double leg2) {
        return leg2 / hypotenuse(leg1, leg2);
    }

    /**
     * Calculates sinus of angle between first leg and hypotenuse
     * in right triangle with given legs.
     * 
     * @param p point, x coordinate of which is treated as length of
     * aligned leg and y coordinate - as the other leg
     * @return angle between first leg and hypotenuse
     */
    public static double triangleSin(Point p) {
        return triangleSin(p.x, p.y);
    }

    /**
     * Calculates cosine of angle between first leg and hypotenuse
     * in right triangle with given legs.
     * 
     * @param leg1 length of the first leg (that forms the angle)
     * @param leg2 length of the other leg
     * @return cosine of angle between first leg and hypotenuse.
     */
    public static double triangleCos(double leg1, double leg2) {
        return leg1 / hypotenuse(leg1, leg2);
    }

    /**
     * Calculates sinus of angle between first leg and hypotenuse
     * in right triangle with given legs.
     * 
     * @param p point, x coordinate of which is treated as length of
     * aligned leg and y coordinate - as the other leg
     * @return angle between first leg and hypotenuse
     */
    public static double triangleCos(Point p) {
        return triangleCos(p.x, p.y);
    }

    /**
     * Calculates the intersection point of rectangle's border and line
     * between rectangle's center and some outer point.
     * 
     * @param area rectangle's position
     * @param outPoint outer point location
     * @return intersection point of rectangle's border and line
     * between rectangle's center and some outer point.
     */
    public static Point rectangleIntersection(Rectangle area, Point outPoint) {
        Point bordA = new Point(area.x, area.y);
        Point bordZ = bordA.getLocation();
        bordZ.translate(area.width, area.height);
        if ((bordA.x < outPoint.x) && (outPoint.x < bordZ.x) && (bordA.y < outPoint.y) && (outPoint.y < bordZ.y)) {
            return outPoint;
        }
        Point base = new Point((bordA.x + bordZ.x) / 2, (bordA.y + bordZ.y) / 2);
        Point size = new Point(area.width, area.height);
        Point diff = new Point(outPoint.x - base.x, outPoint.y - base.y);
        double sizeSin = triangleSin(size);
        double diffSin = triangleSin(diff);
        double diffCos = triangleCos(diff);
        double retX, retY;
        if (sizeSin < Math.abs(diffSin)) {
            retY = (diff.y < 0) ? bordA.y : bordZ.y;
            double tmpY = retY - base.y;
            double tmpX = diffCos * tmpY / diffSin;
            retX = base.x + tmpX;
        } else {
            retX = (diff.x < 0) ? bordA.x : bordZ.x;
            double tmpX = retX - base.x;
            double tmpY = diffSin / diffCos * tmpX;
            retY = base.y + tmpY;
        }
        return new Point((int) retX, (int) retY);
    }

    /**
     * Calculates the intersection point of ellipse's border
     * and line between ellipse's center and some outer point.
     * 
     * @param area rectangle, that limits ellipse with axises aligned to
     * cartesian ones
     * @param outPoint outer point location
     * @return intersection point of ellipse's border and line between
     * ellipse's center and some outer point.
     */
    public static Point ellipseIntersection(Rectangle area, Point outPoint) {
        double radHorz = area.width / 2;
        double radVert = area.height / 2;
        double scaleX = radHorz / radVert;
        Point center = new Point(area.x + (int) radHorz, area.y + (int) radVert);
        Point diff = new Point(outPoint.x - center.x, outPoint.y - center.y);
        Point scaleDiff = new Point((int) (diff.getX() / scaleX), diff.y);
        double scaleSin = triangleSin(scaleDiff);
        double scaleCos = triangleCos(scaleDiff);
        double retX = radHorz * scaleCos + center.x;
        double retY = radVert * scaleSin + center.y;
        return new Point((int) retX, (int) retY);
    }

    /**
     * Calculates the intersection point of rectangle's border (border has
     * slightly softened corners) and line between rectangle's center and
     * some outer point.
     * 
     * @param area rectangle's position
     * @param outPoint outer point location
     * @return intersection point of rectangle's border and line
     * between rectangle's center and some outer point.
     */
    public static Point roundRectangleIntersection(Rectangle area, Point outPoint) {
        Point base = rectangleIntersection(area, outPoint);
        boolean onLeft = (base.x - area.x) < 6;
        boolean onRight = (area.x + area.width - base.x) < 6;
        boolean onTop = (base.y - area.y) < 6;
        boolean onBottom = (area.y + area.height - base.y) < 6;
        boolean onCorner = (onLeft || onRight) && (onTop || onBottom);
        if (onCorner) {
            base.x = onLeft ? area.x + 3 : area.x + area.width - 3;
            base.y = onTop ? area.y + 3 : area.y + area.height - 3;
        }
        return base;
    }

    /**
     * Calculates a hexagon, that should fit into a specified rectangle.
     * 
     * @param r rectangle to fit a hexagon
     * @return polygon that describes the required hexagon
     */
    public static Polygon computeHexagon(Rectangle r) {
        Polygon poly = new Polygon();
        int delta = Math.min(15, r.width / 4);
        final int x1 = r.x, x2 = r.x + delta, x3 = r.x + r.width - delta, x4 = r.x + r.width, y1 = r.y, y3 = r.y + r.height, y2 = (y1 + y3) / 2;
        poly.addPoint(x1, y2);
        poly.addPoint(x2, y1);
        poly.addPoint(x3, y1);
        poly.addPoint(x4, y2);
        poly.addPoint(x3, y3);
        poly.addPoint(x2, y3);
        return poly;
    }

    /**
     * Calculates intersection point between two lines.
     * 
     * @param p1 first line's start
     * @param p2 first line's end
     * @param q1 second line's start
     * @param q2 second line's end
     * @return intersection point or null if lines are parallel
     */
    public static Point lineIntersection(Point p1, Point p2, Point q1, Point q2) {
        int dx1 = p1.x - p2.x, dx2 = q1.x - q2.x, dy1 = p1.y - p2.y, dy2 = q1.y - q2.y;
        if (dx1 * dy2 == dx2 * dy1) {
            return null;
        }
        if (dx1 == 0) {
            int retx = p1.x;
            int rety = q1.y - ((q1.x - p1.x) * dy2) / dx2;
            return new Point(retx, rety);
        }
        if (dx2 == 0) {
            return lineIntersection(q1, q2, p1, p2);
        }
        double b = (dy1 * 1.0) / dx1;
        double a = p1.y - p1.x * b;
        double d = (dy2 * 1.0) / dx2;
        double c = q1.y - q1.x * d;
        double xdouble = (a - c) / (d - b);
        int retx = (int) xdouble;
        double ydouble = p1.y + dy1 * (xdouble - p1.x) / dx1;
        int rety = (int) ydouble;
        return new Point(retx, rety);
    }

    /**
     * Calculates the intersection point of rectangle's border (border has
     * slightly softened corners) and line between rectangle's center and
     * some outer point.
     * 
     * @param area rectangle's position
     * @param outPoint outer point location
     * @return intersection point of rectangle's border and line
     * between rectangle's center and some outer point.
     */
    public static Point hexagonIntersection(Rectangle area, Point outPoint) {
        Point base = rectangleIntersection(area, outPoint);
        int delta = Math.min(15, area.width / 4);
        boolean onVerticalBorder = (base.y == area.y) || (base.y == area.y + area.height);
        boolean onLeft = (base.x - area.x) < delta;
        boolean onRight = (area.x + area.width - base.x) < delta;
        boolean onUpper = (base.y - area.y) < area.height / 2;
        boolean onBottom = !onUpper;
        if (onVerticalBorder && !onLeft && !onRight) {
            return base;
        }
        Point p1 = GeometryUtils.computeCenterPoint(area);
        Point p2 = outPoint;
        Point q1 = new Point(), q2 = new Point();
        q1.y = area.y + (area.height / 2);
        if (onLeft) {
            q1.x = area.x;
            q2.x = area.x + delta;
        } else {
            q1.x = area.x + area.width;
            q2.x = area.x + area.width - delta;
        }
        q2.y = area.y + (onUpper ? 0 : area.height);
        Point result = lineIntersection(p1, p2, q1, q2);
        return result;
    }

    /**
     * Calculates a perpendicular line to a given line.
     * 
     * @param a start of the line
     * @param z end of the line
     * @param aDist distance from start point of the base line
     * to start point of the perpendicular
     * @param size length of the perpendicular
     * @param clockwise should it be placed clockwise or counter-clockwise
     * relative to the base line
     * @return
     */
    public static Point[] perpendicular(Point a, Point z, double aDist, double size, boolean clockwise) {
        double x = z.x - a.x, y = z.y - a.y;
        double lineLength = Math.hypot(x, y);
        double dx = x / lineLength, dy = y / lineLength;
        double x1 = a.x + dx * aDist, y1 = a.y + dy * aDist;
        if (!clockwise) {
            dx *= -1;
            dy *= -1;
        }
        double x2 = x1 + dy * size, y2 = y1 - dx * size;
        Point p1 = new Point((int) x1, (int) y1);
        Point p2 = new Point((int) x2, (int) y2);
        return new Point[] { p1, p2 };
    }
}
