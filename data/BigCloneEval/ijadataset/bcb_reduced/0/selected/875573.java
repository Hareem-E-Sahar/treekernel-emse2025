package nij.qrfrp.extract.tools;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Class representing a collection of connected pixels with a center.
 * Implements the Comparable interface so that the Blobs can be sorted by 
 * their X-coordinate, then their Y-coordinate (similar to how the 
 * MINDTCT stores the minutia in the xyt files).  
 * 
 * <p>This software is distributed under the GNU General Public License, version 3.0.
 * A copy of the license should have been distributed along with the code. If not, 
 * please visit http://www.fsf.org to view the terms of the license.</p>
 * 
 * @author jmetzger 
 */
public class Blob implements Comparable<Blob> {

    private Set<Point> points = new HashSet<Point>();

    private Point center = new Point();

    /**
	 * Constructor:<br><br>
	 * Creates a blob based on a HashSet of points.
	 * 
	 * @param points - The points that make up the blob.
	 */
    public Blob(Set<Point> points) {
        this.points = points;
        calculateBlobCenter();
    }

    /**
	 * Adds a point to the blob.
	 * @param p - The point to be added to the blob.
	 */
    public void addPoint(Point p) {
        points.add(p);
        calculateBlobCenter();
    }

    /**
	 * Gets the blob size in pixels.
	 * @return The number of pixels in the blob.
	 */
    public int getBlobSize() {
        return points.size();
    }

    /**
	 * Gets the center of the blob.
	 * @return The center of the blob.
	 */
    public Point getBlobCenter() {
        return center;
    }

    /**
	 * Gets the points which compose the blob.
	 * @return A HashSet of points which make up the blob.
	 */
    public Set<Point> getBlobPoints() {
        return points;
    }

    /**
	 * Prints the center of the blob, number of points, and x,y coordinates of each point to the console.
	 */
    public void printBlobPoints() {
        System.out.println("Blob Center:  (" + center.x + "," + center.y + ")");
        System.out.print("  Points(" + points.size() + "):");
        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point tempPoint = iterator.next();
            System.out.print("(" + tempPoint.x + "," + tempPoint.y + ")");
        }
        System.out.println();
    }

    /**
	 * Calculates the center of the blob.  First attempt uses the min/max pixel locations in both the 
	 * x and y directions - the average is taken.  If this "center" is not a point in the blob,
	 * the blob point closest to the initially computed "center" is made the center.  
	 * @return the computed blob center
	 */
    public Point calculateBlobCenter() {
        int minX = 0x7FFFFFFF;
        int maxX = -0x7FFFFFFF;
        int minY = 0x7FFFFFFF;
        int maxY = -0x7FFFFFFF;
        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point tempPoint = iterator.next();
            int tempX = tempPoint.x;
            int tempY = tempPoint.y;
            if (tempX > maxX) maxX = tempX;
            if (tempX < minX) minX = tempX;
            if (tempY > maxY) maxY = tempY;
            if (tempY < minY) minY = tempY;
        }
        int centerX = (maxX + minX) / 2;
        int centerY = (maxY + minY) / 2;
        this.center = new Point(centerX, centerY);
        boolean centerFound = false;
        double minDistance = 0x7FFFFFFF;
        Point closestPoint = new Point(0, 0);
        iterator = points.iterator();
        while (iterator.hasNext() && centerFound == false) {
            Point tempPoint = iterator.next();
            if (tempPoint.x == center.x && tempPoint.y == center.y) return this.center; else {
                double tempDistance = Point2D.distance(tempPoint.x, tempPoint.y, center.x, center.y);
                if (tempDistance < minDistance) {
                    minDistance = tempDistance;
                    closestPoint = new Point(tempPoint.x, tempPoint.y);
                }
            }
        }
        this.center = closestPoint;
        return this.center;
    }

    public static void main(String[] args) {
    }

    public int compareTo(Blob o) {
        Blob otherBlob = (Blob) o;
        if (this.getBlobCenter().x > otherBlob.getBlobCenter().x) return 1; else if (this.getBlobCenter().x < otherBlob.getBlobCenter().x) return -1; else {
            if (this.getBlobCenter().y > otherBlob.getBlobCenter().y) return 1; else if (this.getBlobCenter().y < otherBlob.getBlobCenter().y) return -1; else return 0;
        }
    }
}
