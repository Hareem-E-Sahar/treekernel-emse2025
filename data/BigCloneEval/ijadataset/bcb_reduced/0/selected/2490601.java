package visual3d.datastruct;

import visual3d.util.wrapper.cgcwrapper.ConvexHull3D;

public abstract class Shape3d extends Shape {

    private double xmin, xmax, ymin, ymax, zmin, zmax;

    private java.util.Vector points;

    public Shape3d() {
    }

    protected void init() {
        points = new java.util.Vector();
    }

    public int getNumberOfPoints() {
        return points.size();
    }

    public abstract int getNumberOfPolygons();

    public abstract int getNumberOfEdges();

    public java.util.Vector getListOfPoints() {
        return points;
    }

    protected void clear() {
        points = new java.util.Vector();
        System.out.println("deleting points");
    }

    protected boolean existPoint(Point point) {
        for (int i = points.size() - 1; i >= 0; i--) {
            i = i == -1 ? 0 : i;
            if (points.get(i) == point) {
                return true;
            }
        }
        return false;
    }

    protected void addPoint(Point p) {
        points.addElement(p);
    }

    protected void deletePoint(Point p) {
        points.remove(p);
    }

    private Point getMin() {
        return new Point(xmin, ymin, zmin);
    }

    private Point getMax() {
        return new Point(xmax, ymax, zmax);
    }

    private void findMin() {
        double dMinX = ((Point) points.get(0)).x;
        double dMinY = ((Point) points.get(0)).y;
        double dMinZ = ((Point) points.get(0)).z;
        for (int i = 0; i < this.points.size(); i++) {
            if (dMinX > ((Point) points.get(i)).x) dMinX = ((Point) points.get(i)).x;
            if (dMinY > ((Point) points.get(i)).y) dMinY = ((Point) points.get(i)).y;
            if (dMinZ > ((Point) points.get(i)).z) dMinZ = ((Point) points.get(i)).z;
        }
        this.xmin = dMinX;
        this.ymin = dMinY;
        this.zmin = dMinZ;
    }

    private void findMax() {
        double dMaxX = ((Point) points.get(0)).x;
        double dMaxY = ((Point) points.get(0)).y;
        double dMaxZ = ((Point) points.get(0)).z;
        for (int i = 0; i < this.points.size(); i++) {
            if (dMaxX < ((Point) points.get(i)).x) dMaxX = ((Point) points.get(i)).x;
            if (dMaxY < ((Point) points.get(i)).y) dMaxY = ((Point) points.get(i)).y;
            if (dMaxZ < ((Point) points.get(i)).z) dMaxZ = ((Point) points.get(i)).z;
        }
        this.xmax = dMaxX;
        this.ymax = dMaxY;
        this.zmax = dMaxZ;
    }

    public void fitToScreen(int screenX, int screenZ) {
        if (getNumberOfPoints() < 2000) {
            fitToScreenUsingConvexHull(screenX, screenZ);
        } else {
            fitToScreenUsingQuader(screenX, screenZ);
        }
    }

    private void fitToScreenUsingConvexHull(int screenX, int screenZ) {
        findMin();
        findMax();
        double faktor = 1000 / getMin().sub(getMax()).magnitude();
        if (faktor > 2) for (int i = 0; i < this.points.size(); i++) {
            ((Point) points.get(i)).x *= faktor;
            ((Point) points.get(i)).y *= faktor;
            ((Point) points.get(i)).z *= faktor;
        }
        ConvexHull3D chull = null;
        chull = new ConvexHull3D(points);
        java.util.Vector hullPoints = chull.getHullPoints();
        Point center = new Point();
        for (int i = 0; i < hullPoints.size(); i++) {
            center = center.add((Point) hullPoints.get(i));
        }
        center.x /= hullPoints.size();
        center.y /= hullPoints.size();
        center.z /= hullPoints.size();
        for (int i = 0; i < this.points.size(); i++) {
            ((Point) points.get(i)).x -= center.x;
            ((Point) points.get(i)).y -= center.y;
            ((Point) points.get(i)).z -= center.z;
        }
        double currentMax = 0;
        for (int i = 0; i < this.points.size(); i++) {
            Point p1 = (Point) points.get(i);
            double current = p1.dotProduct(p1);
            if (current > currentMax) {
                currentMax = current;
            }
        }
        double diagonal = (Math.sqrt(currentMax)) * 2;
        diagonal *= 1.05D;
        faktor = 0;
        if (screenX >= screenZ) faktor = screenZ / diagonal; else faktor = screenX / diagonal;
        for (int i = 0; i < this.points.size(); i++) {
            ((Point) points.get(i)).x *= faktor;
            ((Point) points.get(i)).y *= faktor;
            ((Point) points.get(i)).z *= faktor;
        }
    }

    private void fitToScreenUsingQuader(int screenX, int screenZ) {
        findMin();
        findMax();
        double center_x = (xmax + xmin) / 2;
        double center_y = (ymax + ymin) / 2;
        double center_z = (zmax + zmin) / 2;
        for (int i = 0; i < this.points.size(); i++) {
            ((Point) points.get(i)).x -= center_x;
            ((Point) points.get(i)).y -= center_y;
            ((Point) points.get(i)).z -= center_z;
        }
        double currentMax = 0;
        for (int i = 0; i < this.points.size(); i++) {
            Point p1 = (Point) points.get(i);
            double current = p1.dotProduct(p1);
            if (current > currentMax) {
                currentMax = current;
            }
        }
        double diagonal = (Math.sqrt(currentMax)) * 2;
        diagonal *= 1.10D;
        double faktor = 0;
        if (screenX >= screenZ) faktor = screenZ / diagonal; else faktor = screenX / diagonal;
        for (int i = 0; i < this.points.size(); i++) {
            ((Point) points.get(i)).x *= faktor;
            ((Point) points.get(i)).y *= faktor;
            ((Point) points.get(i)).z *= faktor;
        }
    }
}
