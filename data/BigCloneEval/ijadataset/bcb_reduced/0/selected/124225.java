package visual3d.util;

import visual3d.datastruct.Point;
import visual3d.datastruct.Polygon;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 18.07.2004
 * Time: 09:43:15
 * To change this template use File | Settings | File Templates.
 */
public class Sort {

    /**
     * sorts the polygons using QuickSort
     */
    public static Vector getListOfPolygonsQuickSort(Vector v) {
        return quickSort(0, v.size() - 1, v);
    }

    private static Vector quickSort(int left, int right, Vector v) {
        if (right > left) {
            Polygon o1 = (Polygon) v.elementAt(right);
            int i = left - 1;
            int j = right;
            while (true) {
                while (lessThan(o1, (Polygon) v.elementAt(++i))) ;
                while (j > 0) if (lessThanOrEqual(o1, (Polygon) v.elementAt(--j))) break;
                if (i >= j) break;
                swap(v, i, j);
            }
            swap(v, i, right);
            quickSort(left, i - 1, v);
            quickSort(i + 1, right, v);
        }
        return v;
    }

    private static boolean lessThan(Polygon lhs, Polygon rhs) {
        double d_lhs = (lhs.getMaxZPoint().z + lhs.getMinZPoint().z) / 2;
        double d_rhs = (rhs.getMaxZPoint().z + rhs.getMinZPoint().z) / 2;
        return d_lhs < d_rhs;
    }

    private static boolean lessThanOrEqual(Polygon lhs, Polygon rhs) {
        double d_lhs = (lhs.getMaxZPoint().z + lhs.getMinZPoint().z) / 2;
        double d_rhs = (rhs.getMaxZPoint().z + rhs.getMinZPoint().z) / 2;
        return d_lhs <= d_rhs;
    }

    private static boolean isGreaterOrEqual(Polygon lhs, Polygon rhs) {
        double d_lhs = (lhs.getMaxZPoint().z + lhs.getMinZPoint().z) / 2;
        double d_rhs = (rhs.getMaxZPoint().z + rhs.getMinZPoint().z) / 2;
        return d_lhs >= d_rhs;
    }

    private static void swap(Vector v, int loc1, int loc2) {
        Object tmp = v.elementAt(loc1);
        v.setElementAt(v.elementAt(loc2), loc1);
        v.setElementAt(tmp, loc2);
    }

    /**
     * sorts the polygons using MergeSort
     */
    public static Vector getListOfPolygonsMergeSort(Vector v) {
        mergeSort(v);
        return v;
    }

    /**
     * Sortiert den Vektor durch Merge Sort aufw�rts.
     */
    public static void mergeSort(Vector v) {
        Object[] help = new Object[v.size()];
        mergeSortRecursive(v, 0, v.size() - 1, help);
    }

    /**
     * Sortiert den Vektor rekursiv durch Merge Sort
     * von Position first bis Position last aufw�rts.
     */
    private static void mergeSortRecursive(Vector v, int first, int last, Object[] help) {
        if (first < last) {
            int mid = (first + last) / 2;
            mergeSortRecursive(v, first, mid, help);
            mergeSortRecursive(v, mid + 1, last, help);
            mergeRecursive(v, first, mid, last, help);
        }
        return;
    }

    /**
     * Mischt die in aufsteigender Reihenfolge vorsortierten Abschnitte
     * von Position first bis Position mid und
     * von Position mid+1 bis Position last des Vektors.
     */
    private static void mergeRecursive(Vector v, int first, int mid, int last, Object[] help) {
        int left = first;
        int right = mid + 1;
        int merged = first;
        while ((left <= mid) && (right <= last)) {
            if (lessThanOrEqual((Polygon) v.elementAt(left), (Polygon) v.elementAt(right))) {
                help[merged] = ((Object) v.elementAt(right));
                right++;
            } else {
                help[merged] = ((Object) v.elementAt(left));
                left++;
            }
            merged++;
        }
        while (right <= last) {
            help[merged] = ((Object) v.elementAt(right));
            merged++;
            right++;
        }
        while (left <= mid) {
            help[merged] = ((Object) v.elementAt(left));
            merged++;
            left++;
        }
        for (merged = first; merged <= last; merged++) {
            v.setElementAt(help[merged], merged);
        }
        return;
    }

    /**
     * sorts the polygons using BubbleSort
     */
    public static Vector getListOfPolygonsBubbleSort(Vector v) {
        for (int j = 0; j < v.size(); j++) {
            for (int i = 0; i < v.size(); i++) {
                Polygon p1 = (Polygon) v.get(j);
                Polygon p2 = (Polygon) v.get(i);
                if (positionOfPolygons(p1, p2) == 1) {
                    Polygon temp = p1;
                    v.setElementAt(p2, j);
                    v.setElementAt(temp, i);
                }
            }
        }
        return v;
    }

    private static int positionOfPolygons(Polygon lhs, Polygon rhs) {
        double rMinX = rhs.getMinXPoint().x;
        double rMaxX = rhs.getMaxXPoint().x;
        double lMinX = lhs.getMinXPoint().x;
        double lMaxX = lhs.getMaxXPoint().x;
        double rMinY = rhs.getMinYPoint().y;
        double rMaxY = rhs.getMaxYPoint().y;
        double lMinY = lhs.getMinYPoint().y;
        double lMaxY = lhs.getMaxYPoint().y;
        double rMinZ = rhs.getMinZPoint().z;
        double rMaxZ = rhs.getMaxZPoint().z;
        double lMinZ = lhs.getMinZPoint().z;
        double lMaxZ = lhs.getMaxZPoint().z;
        if (lMaxZ <= rMinZ) {
            return -1;
        }
        if (lMinZ >= rMaxZ) {
            return 1;
        }
        if (lMaxX <= rMinX) {
            return 2;
        }
        if (lMinX >= rMaxX) {
            return 3;
        }
        if (lMaxY <= rMinY) {
            return 4;
        }
        if (lMinY >= rMaxY) {
            return 5;
        }
        int numFront = 0;
        int numBack = 0;
        int numInc = 0;
        for (int i = 0; i < lhs.getNumberOfPoints(); i++) {
            Point p = lhs.getPoint(i);
            int iAttitude = getAttitude(rhs.getPoint(0), rhs.getPoint(1), rhs.getPoint(2), p);
            if (iAttitude == 0) {
                numInc++;
            }
            if (iAttitude < 0) {
                numBack++;
            }
            if (iAttitude > 0) {
                numFront++;
            }
        }
        if ((numBack + numInc) == lhs.getNumberOfPoints()) {
            return -1;
        }
        numFront = 0;
        numBack = 0;
        numInc = 0;
        for (int i = 0; i < rhs.getNumberOfPoints(); i++) {
            Point p = rhs.getPoint(i);
            int iAttitude = getAttitude(lhs.getPoint(0), lhs.getPoint(1), lhs.getPoint(2), p);
            if (iAttitude == 0) {
                numInc++;
            }
            if (iAttitude > 0) {
                numFront++;
            }
            if (iAttitude < 0) {
                numBack++;
            }
        }
        if ((numFront + numInc) == rhs.getNumberOfPoints()) {
            return -1;
        }
        return 1;
    }

    private static int getAttitude(Point p, Point q, Point r, Point s) {
        Point qp = new Point(p.x - q.x, p.y - q.y, p.z - q.z);
        Point rp = new Point(p.x - r.x, p.y - r.y, p.z - r.z);
        Point sp = new Point(p.x - s.x, p.y - s.y, p.z - s.z);
        Point normal = qp.cross(rp);
        if (normal.z < 0) {
            normal.x *= -1;
            normal.y *= -1;
            normal.z *= -1;
        }
        double d = normal.dotProduct(sp);
        d = (0.0000001 > Math.abs(d)) ? 0 : d;
        if (d < 0) {
            return 1;
        }
        if (d > 0) {
            return -1;
        }
        return 0;
    }
}
