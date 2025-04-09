package mou;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import mou.core.starmap.StarSystem;
import burlov.util.CRC32Ex;

/**
 * Generiert Sternendaten abh�ngig von der Position des Sterns. Und so modeliert ein unendliches
 * Raum voller Sterne
 */
public strictfp class Universum {

    public static final int QUADRANT_SIZE = 100;

    public static final int GALAXY_RADIUS = 2000;

    private static final int MAX_STERNE = 70;

    private static final int RANDOM_STAR_POSITION_AREA = 1;

    private CRC32Ex crc32 = new CRC32Ex();

    private Random rnd = new Random();

    public Universum() {
    }

    /**
	 * Generiert jedes Mal die Sterne im Kartenabschnitt neu.
	 */
    public final List<StarSystem> getStarsInArea(Point upperLeft, Point downRight) {
        List<Point> points = getPointsInArea(upperLeft, downRight);
        List<StarSystem> ret = new ArrayList<StarSystem>(points.size());
        for (Point point : points) {
            ret.add(generateStar(point));
        }
        return ret;
    }

    /**
	 * Liefert eine zuf�llige Starmap Koordinate mit einem Stern
	 * 
	 * @return
	 */
    public synchronized Point getRandomStarPosition() {
        List<Point> points = getPointsInArea(new Point(-QUADRANT_SIZE * RANDOM_STAR_POSITION_AREA, QUADRANT_SIZE * RANDOM_STAR_POSITION_AREA), new Point(QUADRANT_SIZE * RANDOM_STAR_POSITION_AREA, -QUADRANT_SIZE * RANDOM_STAR_POSITION_AREA));
        rnd.setSeed(System.currentTimeMillis());
        int index = rnd.nextInt(points.size());
        return points.get(index);
    }

    /**
	 * Generiert StaticStarSystem Object f�r eine Position. ACHTUNG! Es wird auf jeden Fall ein Star
	 * generiert, obwohl die von der MEthode getStarsInArea(..) gelieferte Liste an dieser Position
	 * keinen Stern enthalten kann.
	 */
    public final synchronized StarSystem generateStar(Point pos) {
        StarSystem star = null;
        if (star != null) return star;
        long seed = generateSeed(pos.x, pos.y);
        star = mou.core.starmap.StarSystem.generateStarSystem(pos.x, pos.y, seed);
        return star;
    }

    /**
	 * Liefert eine Liste mit Point-Objecten, die Sterne in einem Area repr�sentieren
	 * 
	 * @param upperLeft
	 * @param downRight
	 * @return
	 */
    public final synchronized List<Point> getPointsInArea(Point upperLeft, Point downRight) {
        ArrayList<Point> ret = new ArrayList<Point>(100);
        for (Point quadrant : getQuadrantsForArea(upperLeft, downRight)) {
            Set<Point> starPoints = getStarPointsInQuadrant(quadrant);
            for (Point star : starPoints) {
                if (upperLeft.x <= star.x && downRight.x > star.x && upperLeft.y >= star.y && downRight.y < star.y) ret.add(star);
            }
        }
        return ret;
    }

    /**
	 * Methode generiert Set mit Positionen der Sternen in einem Quadrant.
	 * 
	 * @param quadrant
	 * @return
	 */
    public synchronized Set<Point> getStarPointsInQuadrant(Point quadrant) {
        HashMap<Point, Point> cachedQuadrant = null;
        rnd.setSeed(generateSeed(quadrant.x, quadrant.y));
        int sterne = computeNumberOfStars(quadrant);
        cachedQuadrant = new HashMap<Point, Point>(MAX_STERNE);
        for (; sterne > 0; sterne--) {
            Point point = new Point(rnd.nextInt(QUADRANT_SIZE) + quadrant.x, quadrant.y - rnd.nextInt(QUADRANT_SIZE));
            cachedQuadrant.put(point, null);
        }
        return cachedQuadrant.keySet();
    }

    private int computeNumberOfStars(Point pos) {
        int number = (int) (MAX_STERNE - ((Point2D.distance(0, 0, pos.getX(), pos.getY()) / GALAXY_RADIUS) * MAX_STERNE));
        if (number < 0) number = 0;
        return number;
    }

    private final long generateSeed(int x, int y) {
        return generateSeed_CRC32(x, y);
    }

    /**
	 * Generiert Seed f�r den Random Generator aus Kartekoordinaten mit Hilfe von CRC32 Checksum.
	 * Gute Kombination aus der Wertstreuung und der Geschwindigkeit
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
    private final long generateSeed_CRC32(int x, int y) {
        crc32.reset();
        crc32.updateInt(x);
        crc32.updateInt(y);
        return crc32.getValue();
    }

    public static Point getQuadrantForPosition(Point pos) {
        return getQuadrantForPosition(pos, 1);
    }

    /**
	 * Quadranten werden durch ihre linke obere Ecke addressiert
	 * 
	 * @param pos
	 *            beliebige Koordinate auf der Sternenkarte
	 * @param multiplikator
	 *            bestimmt Gro�e des zu beachtende Quadrantes. Dabei wird die QUADRANT_SIZE mit dem
	 *            Multiplikator multipliziert, und so wird die Kantenl�nge des Quadrantes ermittelt
	 * @return Koordinaten des Quadrantes wo sich dieser Punkt befindet
	 */
    public static Point getQuadrantForPosition(Point pos, int multiplikator) {
        if (multiplikator < 1) multiplikator = 1;
        int kante = QUADRANT_SIZE * multiplikator;
        int pX = pos.x;
        int pY = pos.y;
        if (pX < 0) pX++;
        int qX = (int) (pX / kante);
        if (pos.x < 0) qX--;
        qX = qX * kante;
        if (pY > 0) pY--;
        int qY = (int) (pY / kante);
        if (pos.y > 0) qY++;
        qY = qY * kante;
        return new Point(qX, qY);
    }

    public static List<Point> getQuadrantsForArea(Point leftUpper, Point rightDown) {
        return getQuadrantsForArea(leftUpper, rightDown, 1);
    }

    /**
	 * Liefert Liste mit Point-Objekten, die einzelne Quadranten addressieren die sich innerhalb der
	 * gegebenen Koordinaten befinden
	 * 
	 * @param leftUpper
	 * @param rightDown
	 * @param multiplikator
	 *            bestimmt die Gro�e der Quadranten. QUADRANT_SIZE wird mit dem Multiplikator
	 *            multipliziert, und so die Kantenl�nge des Quadrantes berechnet.
	 * @return List mit Point-Objekten
	 */
    public static List<Point> getQuadrantsForArea(Point leftUpper, Point rightDown, int multiplikator) {
        if (multiplikator < 1) multiplikator = 1;
        Point start = getQuadrantForPosition(leftUpper, multiplikator);
        Point end = getQuadrantForPosition(rightDown, multiplikator);
        List<Point> ret = new ArrayList<Point>();
        int kante = QUADRANT_SIZE * multiplikator;
        for (int x = start.x; x <= end.x; x = x + kante) {
            for (int y = start.y; y >= end.y; y = y - kante) {
                ret.add(new Point(x, y));
            }
        }
        return ret;
    }
}
