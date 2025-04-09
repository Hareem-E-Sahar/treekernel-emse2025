import java.awt.Point;
import java.awt.Polygon;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFileChooser;
import se.mushroomwars.mapeditor.model.BuildingModel;
import se.mushroomwars.mapeditor.model.ShapeModel;

/**
 * Opens file and creates paths between buildings
 * 
 * @author Torbj�rn S�rman
 * 
 */
public class PathFinder {

    /**
	 * Stores all shapes that are part of this model.
	 */
    private List<ShapeModel> allShapes = new ArrayList<ShapeModel>();

    private int[] allBuildings;

    private BuildingModel thisBuilding;

    private int noBuildings = 0;

    public static void main(final String[] args) {
        new PathFinder();
    }

    public PathFinder() {
        loadReq();
        allBuildings = setBuildings();
    }

    private Path calculatePath(Point end) {
        Point start = new Point(thisBuilding.getX(), thisBuilding.getY());
        return null;
    }

    /**
	 * Creates all paths from parameterbuilding to alla other buildings
	 * 
	 * @param building
	 *            specific building that needs paths...
	 */
    public PathFinder(BuildingModel building) {
        thisBuilding = building;
        loadReq();
        allBuildings = setBuildings();
    }

    /**
	 * return a hashmap with all paths, a point (other house) is used as key.
	 * 
	 * @return hashmap<point, path>
	 */
    public HashMap<Point, Path> getPaths() {
        HashMap<Point, Path> map = new HashMap<Point, Path>();
        for (int i = 0; i < noBuildings; i++) {
            Point endPoint = new Point(allShapes.get(allBuildings[i]).getX(), allShapes.get(allBuildings[i]).getX());
            map.put(endPoint, calculatePath(endPoint));
        }
        return map;
    }

    private int[] setBuildings() {
        int[] list = new int[allShapes.size()];
        int n = 0;
        for (int i = 0; i < allShapes.size(); i++) {
            if (allShapes.get(i).getLevel() != -1 && allShapes.get(i) != thisBuilding) list[++n] = i;
        }
        noBuildings = n;
        return list;
    }

    public void loadReq() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                load(chooser.getSelectedFile().getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void load(String filename) throws IOException, ClassNotFoundException {
        FileInputStream file = new FileInputStream(filename);
        ObjectInputStream stream = new ObjectInputStream(file);
        allShapes = (List<ShapeModel>) stream.readObject();
        stream.close();
    }

    boolean pointInPolygonSet(double testX, double testY) {
        boolean oddNodes = false;
        for (int polyl = 0; polyl < allShapes.size(); polyl++) {
            if (allShapes.get(polyl).findPoint(0, 0) != -1 && allShapes.get(polyl).getLevel() != -1) {
                Polygon p = allShapes.get(polyl).getPoly();
                for (int i = 0; i < p.npoints; i++) {
                    int j = i + 1;
                    if (j == p.npoints) j = 0;
                    if ((double) p.ypoints[i] < testY && (double) p.ypoints[j] >= testY || (double) p.ypoints[j] < testY && (double) p.ypoints[i] >= testY) {
                        if (p.xpoints[i] + (testY - p.ypoints[i]) / (p.ypoints[j] - p.ypoints[i]) * (p.xpoints[j] - p.xpoints[i]) < testX) {
                            oddNodes = !oddNodes;
                        }
                    }
                }
            }
        }
        return oddNodes;
    }

    private boolean lineInPolygonSet(double testSX, double testSY, double testEX, double testEY) {
        double theCos, theSin, dist, sX, sY, eX, eY, rotSX, rotSY, rotEX, rotEY, crossX;
        int i, j, polyI;
        testEX -= testSX;
        testEY -= testSY;
        dist = Math.sqrt(testEX * testEX + testEY * testEY);
        theCos = testEX / dist;
        theSin = testEY / dist;
        for (polyI = 0; polyI < allShapes.size(); polyI++) {
            if (allShapes.get(polyI).findPoint(0, 0) != -1 && allShapes.get(polyI).getLevel() != -1) {
                Polygon p = allShapes.get(polyI).getPoly();
                for (i = 0; i < p.npoints; i++) {
                    j = i + 1;
                    if (j == p.npoints) j = 0;
                    sX = p.xpoints[i] - testSX;
                    sY = p.ypoints[i] - testSY;
                    eX = p.xpoints[j] - testSX;
                    eY = p.ypoints[j] - testSY;
                    if ((sX == 0 && sY == 0 && eX == testEX && eY == testEY) || (eX == 0 && eY == 0 && sX == testEX && sY == testEY)) {
                        return true;
                    }
                    rotSX = sX * theCos + sY * theSin;
                    rotSY = sY * theCos + sX * theSin;
                    rotEX = eX * theCos + eY * theSin;
                    rotEY = eY * theCos + eX * theSin;
                    if (rotSY < 0 && rotEY > 0 || rotEY < 0 && rotSY > 0) {
                        crossX = rotSY + (rotEX - rotSY) * (-rotSY) / (rotEY - rotSY);
                        if (crossX >= 0 && crossX <= dist) return false;
                    }
                    if (rotSY == 0 && rotEY == 0 && (rotSX >= 0 || rotEX >= 0) && (rotSX <= dist || rotEX <= dist) && (rotSX < 0 || rotEX < 0 || rotSX > dist || rotEX > dist)) return false;
                }
            }
        }
        return pointInPolygonSet(testSX + testEX / 2, testSY + testEY / 2);
    }

    private double calcDist(double sX, double sY, double eX, double eY) {
        eX -= sX;
        eY -= sY;
        return Math.sqrt(eX * eX + eY * eY);
    }

    private void swapPoints(Point a, Point b) {
        Point swap = a;
        a = b;
        b = swap;
    }

    private boolean shortestPath(double sX, double sY, double eX, double eY, double[] solutionX, double[] solutionY, int solutionNodes) {
        return false;
    }
}
