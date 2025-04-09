package org.dcopolis.problem.gallery;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.dcopolis.DCOPolis;
import org.dcopolis.problem.DCRAgent;
import org.dcopolis.problem.Variable;
import org.dcopolis.util.SpringUtilities;

@SuppressWarnings("unchecked")
public class DistanceProblem extends ArtGalleryProblem {

    static ConfigurationPanel configPanel = null;

    private static final long serialVersionUID = -4480482764172637160L;

    DistanceProblemViz dpvGui;

    class GuardLocationComparator implements Comparator<GuardLocation> {

        public GuardLocationComparator() {
        }

        public int compare(GuardLocation g1, GuardLocation g2) {
            return g1.getVisiblePictures().size() - g2.getVisiblePictures().size();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GuardLocationComparator;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    public DistanceProblem() {
        this(Integer.parseInt(System.getProperty("NUM_GUARDS", "3")), Integer.parseInt(System.getProperty("NUM_PAINTINGS", "6")), Math.toRadians(Double.parseDouble(System.getProperty("VIEW_ANGLE", "45"))), Double.parseDouble(System.getProperty("VIEW_DISTANCE", "50")), Integer.parseInt(System.getProperty("GRID_SIZE", "20")), Integer.parseInt(System.getProperty("PRUNE_DUPLICATE_GUARD_LOCATIONS", "1")) != 0, Integer.parseInt(System.getProperty("PRUNE_IRRELEVANT_GUARD_LOCATIONS", "1")) != 0);
    }

    @Override
    public Component getGUI(DCOPolis dcopolis) {
        if (dpvGui == null) {
            dpvGui = new DistanceProblemViz(this, dcopolis.getAlgorithm());
        }
        return dpvGui;
    }

    int[][] dbg_points;

    public int[][] debugGetPoints() {
        return dbg_points;
    }

    boolean[] guardAtPoint;

    boolean[] getGuardAtPoint() {
        return guardAtPoint;
    }

    public static void main(String[] args) {
        for (int numPaintings = 1; numPaintings < 11; numPaintings++) {
            for (int viewDistance = 10; viewDistance < 55; viewDistance += 5) {
                for (int gridSize = 5; gridSize < 55; gridSize += 5) {
                    DistanceProblem d = new DistanceProblem(3, numPaintings, 45d, viewDistance, gridSize, true, true);
                }
            }
        }
    }

    public DistanceProblem(int numGuards, int numPaintings, double viewAngle, double viewDistance, int gridSize, boolean pruneDuplicateGuardLocations, boolean pruneIrrelevantGuardLocations) {
        super();
        HashSet<Variable> variables = new HashSet<Variable>();
        Variable vars[] = new Variable[numGuards];
        ArtGallery gallery = null;
        int points[][] = new int[0][];
        while (points.length == 0) {
            gallery = new RandomArtGallery(numPaintings);
            setGallery(gallery);
            points = gallery.getInteriorPoints(gridSize);
            if (points.length == 0) {
            }
        }
        dbg_points = points;
        int numPossibleGuardPositions = points.length;
        int guardsPlaced = 0;
        guardAtPoint = new boolean[points.length];
        int startingGuardLocation[][] = new int[numGuards][];
        while (guardsPlaced < numGuards) {
            int guardInd = Math.round((float) (Math.random() * (numPossibleGuardPositions - 1)));
            if (guardAtPoint[guardInd]) {
                continue;
            }
            guardAtPoint[guardInd] = true;
            guardsPlaced++;
        }
        PriorityQueue<GuardLocation> domain = new PriorityQueue<GuardLocation>(points.length * 8, new GuardLocationComparator());
        int dbg_numTotalConfigs = 0;
        int dbg_numOptimizedConfigs = 0;
        guardsPlaced = 0;
        for (int i = 0; i < points.length; i++) {
            GuardLocation newLocation = null;
            for (double heading = 0; heading < 2.0 * Math.PI; heading += Math.PI / 4.0) {
                dbg_numTotalConfigs++;
                HashSet<Point> visiblePictures = new HashSet<Point>();
                HashSet<Point> paintings = gallery.getPaintings();
                for (Point painting : paintings) {
                    double xdiff = painting.getX() - (double) points[i][0];
                    double ydiff = painting.getY() - (double) points[i][1];
                    double distance = Math.sqrt(xdiff * xdiff + ydiff * ydiff);
                    if (distance > viewDistance) continue;
                    if (!GuardLocation.getViewingArea(points[i][0], points[i][1], heading, viewAngle, viewDistance).contains(painting.getX(), painting.getY())) continue;
                    boolean hasLineOfSight = true;
                    for (int j = 1; j <= gallery.npoints && hasLineOfSight; j++) {
                        int x1 = gallery.xpoints[j - 1];
                        int y1 = gallery.ypoints[j - 1];
                        int x2, y2;
                        if (j == gallery.npoints) {
                            x2 = gallery.xpoints[0];
                            y2 = gallery.ypoints[0];
                        } else {
                            x2 = gallery.xpoints[j];
                            y2 = gallery.ypoints[j];
                        }
                        Line2D.Double l1 = new Line2D.Double(x1, y1, x2, y2);
                        Line2D.Double l2 = new Line2D.Double(points[i][0], points[i][1], painting.getX(), painting.getY());
                        if (l1.ptLineDist(painting.getX(), painting.getY()) <= 1.5) continue;
                        if (l1.intersectsLine(l2)) hasLineOfSight = false;
                    }
                    if (!hasLineOfSight) continue;
                    visiblePictures.add(painting);
                }
                if (visiblePictures.isEmpty()) continue;
                newLocation = new GuardLocation(points[i][0], points[i][1], heading, viewAngle, viewDistance, visiblePictures);
                dbg_numOptimizedConfigs++;
                domain.add(newLocation);
            }
            if (guardAtPoint[i]) {
                startingGuardLocation[guardsPlaced] = new int[] { points[i][0], points[i][1] };
                guardsPlaced++;
            }
        }
        System.out.println("numPaintings=" + numPaintings + " viewDistance=" + viewDistance + " gridSize=" + gridSize + " TOTALCONFIGS=" + dbg_numTotalConfigs + " OPTIMIZEDCONFIGS=" + dbg_numOptimizedConfigs);
        if (true) {
        }
        System.err.println("Actual domain size: " + domain.size());
        LinkedHashSet<GuardLocation> actualDomain = new LinkedHashSet<GuardLocation>();
        for (GuardLocation gl : domain) actualDomain.add(gl);
        for (int i = 0; i < numGuards; i++) {
            vars[i] = new LocationVariable("Guard" + i, new DCRAgent("Agent" + i, "Host" + i), actualDomain, this, startingGuardLocation[i][0], startingGuardLocation[i][1]);
            variables.add(vars[i]);
        }
        setVariables(variables);
        ArrayList<Line2D.Double> walls = calculateWallEdges(gallery);
        vertices = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            int x1 = points[i][0];
            int y1 = points[i][1];
            vertices[i] = new Point(x1, y1);
        }
        pathLengths = new float[points.length][points.length];
        float[][] edgeCosts = new float[points.length][points.length];
        shortestPaths = new ArrayList[points.length][points.length];
        calculateAllPairsShortestPaths(gridSize, vertices, walls, pathLengths, edgeCosts, shortestPaths);
    }

    float[][] pathLengths;

    ArrayList<Point>[][] shortestPaths;

    Point[] vertices;

    @Override
    void buildConstraints() {
        if (constraintsBuilt || gallery == null || getVariables().isEmpty()) {
            return;
        }
        constraintsBuilt = true;
        for (Point painting : gallery.getPaintings()) {
            new ArtGalleryConstraint(painting, getVariables());
        }
        DistanceProblemConstraint distanceProblemConstraint = new DistanceProblemConstraint(getVariables());
    }

    public float calculateEuclideanDistance(float x1, float y1, float x2, float y2) {
        float xDiff = (x1 - x2) * (x1 - x2);
        float yDiff = (y1 - y2) * (y1 - y2);
        float distance = (float) Math.sqrt(xDiff + yDiff);
        return distance;
    }

    public int findPointIndex(float x, float y) {
        for (int ind = 0; ind < vertices.length; ind++) {
            float currX = (float) vertices[ind].getX();
            float currY = (float) vertices[ind].getY();
            if (x == currX && y == currY) {
                return ind;
            }
        }
        return -1;
    }

    public float calculateGuardDistance(GuardLocation currentLocation, LocationVariable start) {
        int startInd = findPointIndex(start.getX(), start.getY());
        int endInd = findPointIndex(currentLocation.getX(), currentLocation.getY());
        float distance = pathLengths[startInd][endInd];
        System.out.println("started at (" + start.getX() + "," + start.getY() + ") currently at (" + currentLocation.getX() + "," + currentLocation.getY() + ") distance=" + distance);
        return distance;
    }

    public ArrayList<Point> getNodePath(GuardLocation currentLocation, LocationVariable start) {
        int startInd = findPointIndex(start.getX(), start.getY());
        int endInd = findPointIndex(currentLocation.getX(), currentLocation.getY());
        return shortestPaths[startInd][endInd];
    }

    protected ArrayList<Line2D.Double> calculateWallEdges(ArtGallery gallery) {
        ArrayList<Line2D.Double> walls = new ArrayList<Line2D.Double>();
        for (int j = 1; j <= gallery.npoints; j++) {
            int x1 = gallery.xpoints[j - 1];
            int y1 = gallery.ypoints[j - 1];
            int x2, y2;
            if (j == gallery.npoints) {
                x2 = gallery.xpoints[0];
                y2 = gallery.ypoints[0];
            } else {
                x2 = gallery.xpoints[j];
                y2 = gallery.ypoints[j];
            }
            walls.add(new Line2D.Double(x1, y1, x2, y2));
        }
        return walls;
    }

    protected void initializeShortestPaths(int gridSize, Point[] vertices, ArrayList<Line2D.Double> walls, float[][] pathLengths, float[][] edgeCosts, ArrayList<Point>[][] shortestPaths) {
        for (int i = 0; i < edgeCosts.length; i++) {
            int x1 = (int) vertices[i].getX();
            int y1 = (int) vertices[i].getY();
            Point startVertex = vertices[i];
            pathLengths[i][i] = 0;
            edgeCosts[i][i] = 0;
            shortestPaths[i][i] = new ArrayList<Point>();
            shortestPaths[i][i].add(startVertex);
            for (int j = i + 1; j < edgeCosts.length; j++) {
                int x2 = (int) vertices[j].getX();
                int y2 = (int) vertices[j].getY();
                int xDiff = Math.abs(x2 - x1);
                int yDiff = Math.abs(y2 - y1);
                if ((xDiff == gridSize && (yDiff == 0 || yDiff == gridSize)) || (xDiff == 0 && yDiff == gridSize)) {
                    Line2D.Double edgeLine = new Line2D.Double(x1, y1, x2, y2);
                    boolean edgeClear = true;
                    for (Line2D.Double wall : walls) {
                        if (wall.intersectsLine(edgeLine)) {
                            edgeClear = false;
                            break;
                        }
                    }
                    if (edgeClear) {
                        Point endVertex = vertices[j];
                        pathLengths[i][j] = calculateEuclideanDistance(x1, y1, x2, y2);
                        pathLengths[j][i] = pathLengths[i][j];
                        edgeCosts[i][j] = pathLengths[i][j];
                        edgeCosts[j][i] = pathLengths[i][j];
                        shortestPaths[i][j] = new ArrayList<Point>();
                        shortestPaths[i][j].add(startVertex);
                        shortestPaths[i][j].add(endVertex);
                        shortestPaths[j][i] = new ArrayList<Point>();
                        shortestPaths[j][i].add(endVertex);
                        shortestPaths[j][i].add(startVertex);
                    } else {
                        pathLengths[i][j] = Float.POSITIVE_INFINITY;
                        pathLengths[j][i] = Float.POSITIVE_INFINITY;
                        edgeCosts[i][j] = Float.POSITIVE_INFINITY;
                        edgeCosts[j][i] = Float.POSITIVE_INFINITY;
                        shortestPaths[i][j] = new ArrayList<Point>();
                        shortestPaths[j][i] = new ArrayList<Point>();
                    }
                } else {
                    pathLengths[i][j] = Float.POSITIVE_INFINITY;
                    pathLengths[j][i] = Float.POSITIVE_INFINITY;
                    edgeCosts[i][j] = Float.POSITIVE_INFINITY;
                    edgeCosts[j][i] = Float.POSITIVE_INFINITY;
                    shortestPaths[i][j] = new ArrayList<Point>();
                    shortestPaths[j][i] = new ArrayList<Point>();
                }
            }
        }
    }

    protected void calculateAllPairsShortestPaths(int gridSize, Point[] vertices, ArrayList<Line2D.Double> walls, float[][] pathLengths, float[][] edgeCosts, ArrayList<Point>[][] shortestPaths) {
        initializeShortestPaths(gridSize, vertices, walls, pathLengths, edgeCosts, shortestPaths);
        for (int k = 1; k < edgeCosts.length; k++) {
            for (int i = 0; i < edgeCosts.length; i++) {
                for (int j = i + 1; j < edgeCosts.length; j++) {
                    float ijCost = pathLengths[i][j];
                    float ikCost = pathLengths[i][k];
                    float kjCost = pathLengths[k][j];
                    if (ijCost > (ikCost + kjCost)) {
                        pathLengths[i][j] = pathLengths[j][i] = ikCost + kjCost;
                        shortestPaths[i][j].clear();
                        shortestPaths[i][j].addAll(shortestPaths[i][k]);
                        shortestPaths[i][j].remove(shortestPaths[i][j].size() - 1);
                        shortestPaths[i][j].addAll(shortestPaths[k][j]);
                        shortestPaths[j][i].clear();
                        for (int in = 0; in < shortestPaths[i][j].size(); in++) {
                            shortestPaths[j][i].add(shortestPaths[i][j].get(shortestPaths[i][j].size() - 1 - in));
                        }
                    }
                }
            }
        }
    }

    /**
	 * for the above what i really need is a graph with all points in it as nodes
	 * if two nodes can see each other with no line of the polygon coming between them,
	 * then they should be connected
	 * they should only be connected if they are in the 8 immediately surrounding the node
	 * all edges should be two way
	 * after the nodes/edges have been created, we should be able to run all pairs shortest paths
	 * algorithm
	 */
    private static class ConfigurationPanel extends JPanel implements ChangeListener, ActionListener {

        private static final long serialVersionUID = 8771363007307706337L;

        JSpinner paintings;

        JSpinner guards;

        JSpinner viewDistance;

        JSpinner viewAngle;

        JSpinner galleryWidth;

        JSpinner galleryHeight;

        JSpinner galleryWallsMin;

        JSpinner galleryWallsMax;

        JSpinner gridSize;

        JSpinner galleryMinDistanceBetweenWalls;

        JCheckBox pruneDuplicateGuardLocations;

        JCheckBox pruneIrrelevantGuardLocations;

        Hashtable<JSpinner, String> propertiesBySpinner;

        JSpinner setSpinner(JPanel panel, String labelName, String propName, String defaultValue, int min, int max, int stepSize) {
            JLabel l = new JLabel(labelName, JLabel.TRAILING);
            JSpinner s = new JSpinner(new SpinnerNumberModel(Integer.parseInt(System.getProperty(propName, defaultValue)), min, max, stepSize));
            l.setLabelFor(s);
            panel.add(l);
            panel.add(s);
            propertiesBySpinner.put(s, propName);
            s.addChangeListener(this);
            return s;
        }

        public ConfigurationPanel() {
            super(new SpringLayout());
            JPanel grid = new JPanel(new SpringLayout());
            propertiesBySpinner = new Hashtable<JSpinner, String>();
            paintings = setSpinner(grid, "Number of Paintings:", "NUM_PAINTINGS", "6", 1, 100, 1);
            guards = setSpinner(grid, "Number of Guards:", "NUM_GUARDS", "3", 1, 100, 1);
            viewDistance = setSpinner(grid, "View Distance (pixels):", "VIEW_DISTANCE", "50", 1, 1000, 1);
            viewAngle = setSpinner(grid, "View Angle (degrees):", "VIEW_ANGLE", "45", 1, 360, 1);
            galleryWidth = setSpinner(grid, "Gallery Width (pixels):", "GALLERY_WIDTH", "200", 10, 1000, 10);
            galleryHeight = setSpinner(grid, "Gallery Height (pixels):", "GALLERY_HEIGHT", "200", 10, 1000, 10);
            gridSize = setSpinner(grid, "Grid Size (pixels):", "GRID_SIZE", "20", 1, 100, 1);
            galleryMinDistanceBetweenWalls = setSpinner(grid, "Min. Distance Between Walls", "GALLERY_MIN_DIST_BETWEEN_WALLS", "40", 1, 1000, 1);
            galleryWallsMin = setSpinner(grid, "Min. Number of Walls:", "GALLERY_WALLS_MIN", "8", 3, 100, 1);
            galleryWallsMax = setSpinner(grid, "Max. Number of Walls", "GALLERY_WALLS_MAX", "12", 3, 100, 1);
            SpringUtilities.makeCompactGrid(grid, 5, 4, 6, 6, 6, 6);
            add(grid);
            pruneDuplicateGuardLocations = new JCheckBox("<html><i>Prune duplicate guard locations</i><br>(i.e. ignore guard locations that can see the same set of paintings as another location).<br>This drastically reduces the state space, making the problem much easier.</html>", Integer.parseInt(System.getProperty("PRUNE_DUPLICATE_GUARD_LOCATIONS", "1")) != 0);
            pruneDuplicateGuardLocations.addActionListener(this);
            add(pruneDuplicateGuardLocations);
            pruneIrrelevantGuardLocations = new JCheckBox("<html><i>Prune irrelevant guard locations</i><br>(i.e. ignore guard locations that are strictly dominated by another location).<br>This drastically reduces the state space, making the problem much easier.</html>", Integer.parseInt(System.getProperty("PRUNE_IRRELEVANT_GUARD_LOCATIONS", "1")) != 0);
            pruneIrrelevantGuardLocations.addActionListener(this);
            add(pruneIrrelevantGuardLocations);
            SpringUtilities.makeCompactGrid(this, 3, 1, 6, 6, 6, 6);
        }

        public void stateChanged(ChangeEvent ce) {
            if (ce.getSource() instanceof JSpinner) {
                JSpinner s = (JSpinner) ce.getSource();
                System.setProperty(propertiesBySpinner.get(s), s.getValue().toString());
            }
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == pruneDuplicateGuardLocations) System.setProperty("PRUNE_DUPLICATE_GUARD_LOCATIONS", (pruneDuplicateGuardLocations.isSelected() ? "1" : "0")); else if (ae.getSource() == pruneIrrelevantGuardLocations) System.setProperty("PRUNE_IRRELEVANT_GUARD_LOCATIONS", (pruneIrrelevantGuardLocations.isSelected() ? "1" : "0"));
        }
    }

    public static boolean isInstantiable() {
        return true;
    }
}
