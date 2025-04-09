package wotlas.libs.pathfinding;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import wotlas.utils.Debug;
import wotlas.utils.List;

/** A* algorithm finds the optimal path between 2 points
 *
 * usage:
 * - create a AStar object
 *   AStar astar;
 * - initialize the mask with:
 *    a buffered image
 *   astar.initMask(BufferedImage maskBuffImg, int imgWidth, int imgHeight)
 *    or a boolean array
 *   aStar.setMask( boolean mask[][] )
 * - set the sprite size
 *   astar.setSpriteSize(int size)
 * - start the search
 *   AStarObject.findPath(startPoint, goalPoint);
 *
 * @author Petrus, Aldiss
 * @see wotlas.libs.pathfinding.NodeDouble
 */
public class AStarDouble {

    /** Our AStarDouble object.
     */
    private static AStarDouble aStar = null;

    /** mask of the image : mask[i][j] is true if pixel(i,j) is not blocked
     */
    private static boolean[][] map;

    /** width of the map
     */
    private static int mapWidth;

    /** height of the map
     */
    private static int mapHeight;

    /** size of the sprite (in CELL units)
     */
    private static int SPRITE_SIZE = 4;

    /** size of a mask's cell (in pixels)
     */
    private static int tileSize = -1;

    /** True if we show debug informations
     */
    public static boolean SHOW_DEBUG = false;

    /** To set sprite size
     */
    public static void setSpriteSize(int size) {
        AStarDouble.SPRITE_SIZE = size - 1;
    }

    /** To get sprite size
     */
    public static int getSpriteSize() {
        return (AStarDouble.SPRITE_SIZE + 1);
    }

    /** To set the tile size
     */
    public static void setTileSize(int tileSize) {
        AStarDouble.tileSize = tileSize;
    }

    /** To get the tile size
     */
    public static int getTileSize() {
        return AStarDouble.tileSize;
    }

    /** Empty constructor.
     */
    public AStarDouble() {
    }

    /** To get AStarDouble object
     */
    public static AStarDouble getAStar() {
        return AStarDouble.aStar;
    }

    /** Test to know if pathFollower is used by client (initialized) or server (not initialized)
     *
     * @returns true if AStarDouble have been initialized
     */
    public static boolean isInitialized() {
        return !(AStarDouble.aStar == null);
    }

    /**
     * Estimates the distance between 2 points
     *
     * @param poinFrom first point
     * @param pointTo second point
     * @return the distance between the 2 points
     */
    private double estimate(Point pointFrom, Point pointTo) {
        return pointFrom.distance(pointTo);
    }

    /**
     * begins optimal path search
     */
    private NodeDouble searchNode(Point pointStart, Point pointGoal) {
        Hashtable<Point, NodeDouble> open = new Hashtable<Point, NodeDouble>(300);
        Hashtable<Point, NodeDouble> closed = new Hashtable<Point, NodeDouble>(300);
        List nodes = new List();
        double cost = 0;
        double estimation = estimate(pointStart, pointGoal);
        NodeDouble firstNode = new NodeDouble();
        firstNode.point = pointStart;
        firstNode.g = cost;
        firstNode.h = estimation;
        firstNode.f = cost + estimation;
        firstNode.parent = null;
        open.put(pointStart, firstNode);
        nodes.addElement(firstNode);
        NodeDouble bestNode;
        List childPoints;
        List children = new List(8);
        while (!nodes.isEmpty()) {
            bestNode = (NodeDouble) nodes.elementAt(0);
            if (closed.get(bestNode.point) != null) {
                nodes.removeFirstElement();
                continue;
            }
            if (bestNode.point.equals(pointGoal)) {
                nodes.removeAllElements();
                open.clear();
                closed.clear();
                return bestNode;
            } else {
            }
            children.removeAllElements();
            childPoints = generateChildren(bestNode.point);
            Point childPoint;
            NodeDouble closedNode;
            NodeDouble openNode;
            NodeDouble oldNode;
            double childCost;
            for (int i = 0; i < childPoints.size(); i++) {
                closedNode = null;
                openNode = null;
                oldNode = null;
                childPoint = (Point) childPoints.elementAt(i);
                childCost = bestNode.g + 1;
                if ((closedNode = closed.get(childPoint)) == null) {
                    openNode = open.get(childPoint);
                }
                oldNode = (openNode != null) ? openNode : closedNode;
                if (oldNode != null) {
                    if (childCost < oldNode.g) {
                        if (closedNode != null) {
                            open.put(childPoint, oldNode);
                            closed.remove(childPoint);
                        } else {
                            estimation = oldNode.h;
                            oldNode = new NodeDouble();
                            oldNode.point = childPoint;
                            oldNode.parent = bestNode;
                            oldNode.g = childCost;
                            oldNode.h = estimation;
                            oldNode.f = childCost + estimation;
                            open.put(childPoint, oldNode);
                        }
                        oldNode.parent = bestNode;
                        oldNode.g = childCost;
                        oldNode.f = childCost + oldNode.h;
                        children.addElement(oldNode);
                    }
                } else {
                    NodeDouble newNode = new NodeDouble();
                    newNode.point = childPoint;
                    newNode.parent = bestNode;
                    estimation = estimate(childPoint, pointGoal);
                    newNode.h = estimation;
                    newNode.g = childCost;
                    newNode.f = childCost + estimation;
                    open.put(childPoint, newNode);
                    children.addElement(newNode);
                }
            }
            open.remove(bestNode.point);
            closed.put(bestNode.point, bestNode);
            nodes.removeFirstElement();
            addToNodes(children, nodes);
        }
        if (AStarDouble.SHOW_DEBUG) {
            System.out.println("no path found");
        }
        nodes.removeAllElements();
        open.clear();
        closed.clear();
        return null;
    }

    /**
     *
     */
    private int rbsearch(int l, int h, double tot, double costs, List nodes) {
        if (l > h) {
            return l;
        }
        int cur = (l + h) / 2;
        double ot = ((NodeDouble) nodes.elementAt(cur)).f;
        if ((tot < ot) || (tot == ot && costs >= ((NodeDouble) nodes.elementAt(cur)).g)) {
            return rbsearch(l, cur - 1, tot, costs, nodes);
        }
        return rbsearch(cur + 1, h, tot, costs, nodes);
    }

    /**
     *
     */
    private int bsearch(int l, int h, double tot, double costs, List nodes) {
        int lo = l;
        int hi = h;
        while (lo <= hi) {
            int cur = (lo + hi) / 2;
            double ot = ((NodeDouble) nodes.elementAt(cur)).f;
            if ((tot < ot) || (tot == ot && costs >= ((NodeDouble) nodes.elementAt(cur)).g)) {
                hi = cur - 1;
            } else {
                lo = cur + 1;
            }
        }
        return lo;
    }

    /**
     *
     */
    private void addToNodes(List children, List nodes) {
        NodeDouble newNode;
        int idx;
        int idxEnd = nodes.size() - 1;
        for (int i = 0; i < children.size(); i++) {
            newNode = (NodeDouble) children.elementAt(i);
            idx = bsearch(0, idxEnd, newNode.f, newNode.g, nodes);
            nodes.insertElementAt(newNode, idx);
        }
    }

    /**
     * test if a point is valid for the path
     * regarding the sprite size
     *
     * @param x the x coordinate (in CELL units)
     * @param y the y coordinate (in CELL units)
     * @return true if point is valid (not blocked) in the {@link #mask mask}
     */
    public boolean isNotBlock(int x, int y) {
        if ((x < 0) || (x + AStarDouble.SPRITE_SIZE >= AStarDouble.mapWidth) || (y < 0) || (y + AStarDouble.SPRITE_SIZE >= AStarDouble.mapHeight)) {
            return false;
        }
        return (AStarDouble.map[x][y] && AStarDouble.map[x][y + AStarDouble.SPRITE_SIZE] && AStarDouble.map[x + AStarDouble.SPRITE_SIZE][y] && AStarDouble.map[x + AStarDouble.SPRITE_SIZE][y + AStarDouble.SPRITE_SIZE] && AStarDouble.map[x + AStarDouble.SPRITE_SIZE / 2][y + AStarDouble.SPRITE_SIZE / 2]);
    }

    /** test if a point is valid for the path
     * regarding the sprite size
     *
     * @param pt the point (in CELL units)
     * @return true if point is valid (not blocked) in the {@link #mask mask}
     */
    public boolean isNotBlock(Point pt) {
        return isNotBlock(pt.x, pt.y);
    }

    /**
     * test if a point (in CELL units) is a valid goal,
     * and correct the position regarding the sprite size
     *
     * @param pointGoal the point (in CELL units)
     * @return true if point is valid (not blocked) in the {@link #mask mask}
     */
    public boolean isValidGoal(Point pointGoal) {
        int x = pointGoal.x;
        int y = pointGoal.y;
        if (isNotBlock(x, y)) {
            if (AStarDouble.SHOW_DEBUG) {
                System.out.println("AStarDouble \t (" + x + "," + y + ") is valid point");
            }
            return true;
        } else {
            if (AStarDouble.SHOW_DEBUG) {
                System.out.println("AStarDouble \t (" + x + "," + y + ") is not a valid point -> search a valid point");
                System.out.println("\ttileSize = " + AStarDouble.tileSize);
                System.out.println("\tSPRITE_SIZE = " + AStarDouble.SPRITE_SIZE);
                System.out.println("\tmapWidth = " + AStarDouble.mapWidth);
                System.out.println("\tmapHeight = " + AStarDouble.mapHeight);
            }
            Debug.signal(Debug.NOTICE, null, "not a valid goal point -> search a valid point");
        }
        if (x < 0) {
            pointGoal.x = 0;
        }
        if (y < 0) {
            pointGoal.y = 0;
        }
        if (x + AStarDouble.SPRITE_SIZE >= AStarDouble.mapWidth) {
            if (AStarDouble.SHOW_DEBUG) {
                System.out.print("player near border -> change x=mapWidth-SPRITE_SIZE-1");
            }
            pointGoal.x = AStarDouble.mapWidth - AStarDouble.SPRITE_SIZE - 1;
        }
        if (y + AStarDouble.SPRITE_SIZE >= AStarDouble.mapHeight) {
            if (AStarDouble.SHOW_DEBUG) {
                System.out.print("player near border -> change y=mapHeight-SPRITE_SIZE-1");
            }
            pointGoal.y = AStarDouble.mapHeight - AStarDouble.SPRITE_SIZE - 1;
        }
        if (x + AStarDouble.SPRITE_SIZE < AStarDouble.mapWidth) {
            if (isNotBlock(x + AStarDouble.SPRITE_SIZE, y)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change x+");
                }
                pointGoal.x += AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if ((x + AStarDouble.SPRITE_SIZE < AStarDouble.mapWidth) && (y + AStarDouble.SPRITE_SIZE < AStarDouble.mapHeight)) {
            if (isNotBlock(x + AStarDouble.SPRITE_SIZE, y + AStarDouble.SPRITE_SIZE)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change x+ y+");
                }
                pointGoal.x += AStarDouble.SPRITE_SIZE;
                pointGoal.y += AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if (y + AStarDouble.SPRITE_SIZE < AStarDouble.mapHeight) {
            if (isNotBlock(x, y + AStarDouble.SPRITE_SIZE)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change y+");
                }
                pointGoal.y += AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if ((x > AStarDouble.SPRITE_SIZE) && (y + AStarDouble.SPRITE_SIZE < AStarDouble.mapHeight)) {
            if (isNotBlock(x - AStarDouble.SPRITE_SIZE, y + AStarDouble.SPRITE_SIZE)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change x- y+");
                }
                pointGoal.x -= AStarDouble.SPRITE_SIZE;
                pointGoal.y += AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if (x > AStarDouble.SPRITE_SIZE) {
            if (isNotBlock(x - AStarDouble.SPRITE_SIZE, y)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change x-");
                }
                pointGoal.x -= AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if ((x > AStarDouble.SPRITE_SIZE) && (y > AStarDouble.SPRITE_SIZE)) {
            if (isNotBlock(x - AStarDouble.SPRITE_SIZE, y - AStarDouble.SPRITE_SIZE)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change x- y-");
                }
                pointGoal.x -= AStarDouble.SPRITE_SIZE;
                pointGoal.y -= AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if (y > AStarDouble.SPRITE_SIZE) {
            if (isNotBlock(x, y - AStarDouble.SPRITE_SIZE)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change y-");
                }
                pointGoal.y -= AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        if ((x + AStarDouble.SPRITE_SIZE < AStarDouble.mapWidth) && (y > AStarDouble.SPRITE_SIZE)) {
            if (isNotBlock(x + AStarDouble.SPRITE_SIZE, y - AStarDouble.SPRITE_SIZE)) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("change x+ y-");
                }
                pointGoal.x += AStarDouble.SPRITE_SIZE;
                pointGoal.y -= AStarDouble.SPRITE_SIZE;
                return true;
            }
        }
        return false;
    }

    /**
     * test if a point (in CELL units) is a valid start,
     * and correct the position regarding the sprite size
     *
     * @param pointGoal the point (in CELL units)
     * @return true if point is valid (not blocked) in the {@link #mask mask}
     */
    public static boolean isValidStart(Point pointGoal) {
        return AStarDouble.aStar.isValid(pointGoal);
    }

    /**
     * test if a point (in pixels coordinate) is a valid start.
     * If it's an invalid point, search a valid point near it,
     * and correct the position regarding the sprite size
     *
     * @param pointGoal the point
     * @return true if point is valid or has been corrected, false otherwise
     */
    private boolean isValid(Point pointGoal) {
        int radius = 3;
        int x = pointGoal.x / AStarDouble.tileSize;
        int y = pointGoal.y / AStarDouble.tileSize;
        if (isNotBlock(x, y)) {
            return true;
        } else {
            Debug.signal(Debug.NOTICE, null, "not a valid start point -> search a valid point");
        }
        if (x + AStarDouble.SPRITE_SIZE > AStarDouble.mapWidth) {
            if (AStarDouble.SHOW_DEBUG) {
                System.out.println("test x near border");
            }
            if (AStarDouble.map[x - AStarDouble.SPRITE_SIZE - 1][y]) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("player near border -> change x=mapWidth-SPRITE_SIZE-1");
                }
                pointGoal.x = AStarDouble.mapWidth - AStarDouble.SPRITE_SIZE - 1;
                return true;
            }
        }
        if (y + AStarDouble.SPRITE_SIZE > AStarDouble.mapHeight) {
            if (AStarDouble.SHOW_DEBUG) {
                System.out.println("test y near border");
            }
            if (AStarDouble.map[x][AStarDouble.mapHeight - AStarDouble.SPRITE_SIZE - 1]) {
                if (AStarDouble.SHOW_DEBUG) {
                    System.out.print("player near border -> change y=mapHeight-SPRITE_SIZE-1");
                }
                pointGoal.y = AStarDouble.mapHeight - AStarDouble.SPRITE_SIZE - 1;
                return true;
            }
        }
        if (isNotBlock(x, y - 1)) {
            pointGoal.y -= AStarDouble.tileSize;
            return true;
        }
        if (isNotBlock(x, y + 1)) {
            pointGoal.y += AStarDouble.tileSize;
            return true;
        }
        if (isNotBlock(x - 1, y - 1)) {
            pointGoal.x -= AStarDouble.tileSize;
            pointGoal.y -= AStarDouble.tileSize;
            return true;
        }
        if (isNotBlock(x - 1, y + 1)) {
            pointGoal.x -= AStarDouble.tileSize;
            pointGoal.y += AStarDouble.tileSize;
            return true;
        }
        if (isNotBlock(x + 1, y - 1)) {
            pointGoal.x += AStarDouble.tileSize;
            pointGoal.y -= AStarDouble.tileSize;
            return true;
        }
        if (isNotBlock(x + 1, y + 1)) {
            pointGoal.x += AStarDouble.tileSize;
            pointGoal.y += AStarDouble.tileSize;
            return true;
        }
        for (int step = -2; step < 3; step++) {
            if (isNotBlock(x - 2, y + step)) {
                pointGoal.x -= 2 * AStarDouble.tileSize;
                pointGoal.y += step * AStarDouble.tileSize;
                return true;
            }
        }
        for (int step = -1; step < 2; step++) {
            if (isNotBlock(x + step, y - 2)) {
                pointGoal.x += step * AStarDouble.tileSize;
                pointGoal.y -= 2 * AStarDouble.tileSize;
                return true;
            }
            if (isNotBlock(x + step, y + 2)) {
                pointGoal.x += step * AStarDouble.tileSize;
                pointGoal.y += 2 * AStarDouble.tileSize;
                return true;
            }
        }
        for (int step = -2; step < 3; step++) {
            if (isNotBlock(x + 2, y + step)) {
                pointGoal.x += 2 * AStarDouble.tileSize;
                pointGoal.y += step * AStarDouble.tileSize;
                return true;
            }
        }
        return false;
    }

    /**
     * Generates all the not blocked children of a Node
     *
     * @param p Node.point
     * returns a Vector of child Points of the point "p"
     */
    private List generateChildren(Point p) {
        List listChildren = new List(8);
        int x = p.x;
        int y = p.y;
        if (isNotBlock(x, y - 1)) {
            listChildren.addElement(new Point(x, y - 1));
        }
        if (isNotBlock(x + 1, y)) {
            listChildren.addElement(new Point(x + 1, y));
        }
        if (isNotBlock(x, y + 1)) {
            listChildren.addElement(new Point(x, y + 1));
        }
        if (isNotBlock(x - 1, y)) {
            listChildren.addElement(new Point(x - 1, y));
        }
        if (isNotBlock(x - 1, y - 1)) {
            listChildren.addElement(new Point(x - 1, y - 1));
        }
        if (isNotBlock(x - 1, y + 1)) {
            listChildren.addElement(new Point(x - 1, y + 1));
        }
        if (isNotBlock(x + 1, y + 1)) {
            listChildren.addElement(new Point(x + 1, y + 1));
        }
        if (isNotBlock(x + 1, y - 1)) {
            listChildren.addElement(new Point(x + 1, y - 1));
        }
        return listChildren;
    }

    /**
     * Finds the optimal path between 2 points.
     *
     * @param pointStart baginning of the path (in CELL units)
     * @param pointGoal end of the path (in CELL units)
     */
    public static List findPath(Point pointStart, Point pointGoal) {
        if (AStarDouble.SHOW_DEBUG) {
            System.out.println("AStarDouble::findPath");
        }
        if (AStarDouble.aStar == null) {
            AStarDouble.aStar = new AStarDouble();
        }
        if (AStarDouble.SHOW_DEBUG) {
            System.out.print(pointGoal);
        }
        if ((!AStarDouble.aStar.isValidGoal(pointGoal))) {
            if (AStarDouble.SHOW_DEBUG) {
                System.err.println("error : invalid point");
            }
            return null;
        }
        if (AStarDouble.SHOW_DEBUG) {
            System.out.println(" -> " + pointGoal);
        }
        NodeDouble solution = AStarDouble.aStar.searchNode(pointStart, pointGoal);
        return AStarDouble.aStar.getPath(solution);
    }

    /**
     * constructs the path from the start node to the node n
     */
    private List getPath(NodeDouble n) {
        List result;
        if (n == null) {
            result = new List();
        } else {
            result = getPath(n.parent);
            result.addElement(n.point);
        }
        return result;
    }

    /**
     * prints the AStar path
     */
    public String toString(List path) {
        Point pathPoint;
        String result = "";
        for (int i = 0; i < path.size(); i++) {
            pathPoint = (Point) path.elementAt(i);
            result += "path[" + i + "] = (" + pathPoint.x + "," + pathPoint.y + ")\n";
        }
        return result;
    }

    /**
     * initializes the array "map" with a BufferedImage
     */
    public void initMask(BufferedImage maskBuffImg, int myMapWidth, int myMapHeight) {
        AStarDouble.mapWidth = myMapWidth;
        AStarDouble.mapHeight = myMapHeight;
        AStarDouble.map = null;
        AStarDouble.map = new boolean[myMapWidth][myMapHeight];
        for (int i = 0; i < myMapWidth; i++) {
            for (int j = 0; j < myMapHeight; j++) {
                AStarDouble.map[i][j] = (maskBuffImg.getRGB(i, j) == -1) ? false : true;
            }
        }
    }

    /** To set the mask
     */
    public static void setMask(boolean mask[][]) {
        if (AStarDouble.aStar == null) {
            AStarDouble.aStar = new AStarDouble();
        }
        AStarDouble.map = mask;
        AStarDouble.mapWidth = mask.length;
        AStarDouble.mapHeight = mask[0].length;
    }

    public static List smoothPath(List path) {
        return AStarDouble.aStar.smoothPath1(path);
    }

    /** To smooth a path.
     * @param path a previously created path via Astar
     * @return smoothed path...
     */
    public List smoothPath1(List path) {
        if ((path == null) || (path.size() < 3)) {
            return path;
        }
        List smoothedPath = new List(path.size());
        Point checkPoint = (Point) path.elementAt(0);
        int index = 1;
        smoothedPath.addElement(path.elementAt(0));
        while (index + 1 < path.size()) {
            if (walkable(checkPoint, (Point) path.elementAt(index + 1))) {
                index++;
            } else {
                checkPoint = (Point) path.elementAt(index);
                smoothedPath.addElement(checkPoint);
                index++;
            }
        }
        smoothedPath.addElement(path.elementAt(index));
        return smoothedPath;
    }

    /** Returns true if we can walk directly from point A to point B.
     */
    private boolean walkable(Point a, Point b) {
        float cosinus = 0;
        float sinus = 0;
        if ((a.x == b.x) && (a.y == b.y)) {
            return true;
        }
        if (b.x == a.x) {
            if (b.y > a.y) {
                cosinus = 0;
                sinus = 1;
            } else if (b.y < a.y) {
                cosinus = 0;
                sinus = -1;
            }
        } else {
            double angle = Math.atan((double) (b.y - a.y) / (b.x - a.x));
            if (b.x < a.x) {
                cosinus = (float) -Math.cos(angle);
                sinus = (float) -Math.sin(angle);
            } else {
                cosinus = (float) Math.cos(angle);
                sinus = (float) Math.sin(angle);
            }
        }
        float rfin = (float) Math.sqrt((b.y - a.y) * (b.y - a.y) + (b.x - a.x) * (b.x - a.x));
        for (float r = 0.25f; r < rfin; r += 0.25f) {
            if (!isNotBlock((int) (a.x + r * cosinus), (int) (a.y + r * sinus))) {
                return false;
            }
        }
        return true;
    }

    /** To dynamically modify the mask setting all pixels of a rectangle to a boolean value
     *
     * @param r the rectangle to fill (in screen pixels coordinate)
     * @param maskTileSize the CELL size (in pixel units)
     * @param value new value of the pixels
     */
    private synchronized void changeRectangle(Rectangle r, int maskTileSize, boolean value) {
        int cx = (r.x / maskTileSize);
        int cy = (r.y / maskTileSize);
        int cWidth = (int) (r.getWidth() / maskTileSize);
        if (cWidth == 0) {
            cWidth = 1;
        }
        int cHeight = (int) (r.getHeight() / maskTileSize);
        if (cHeight == 0) {
            cHeight = 1;
        }
        if (AStarDouble.SHOW_DEBUG) {
            System.out.println("cWidth = " + cWidth + " cHeight = " + cHeight);
        }
        for (int i = 0; i < cWidth; i++) {
            for (int j = 0; j < cHeight; j++) {
                if (cx + i < AStarDouble.map.length && cy + j < AStarDouble.map[0].length) {
                    AStarDouble.map[cx + i][cy + j] = value;
                }
            }
        }
    }

    /** To dynamically modify the mask setting all pixels
     * of a rectangle to false
     *
     * @param r the rectangle to fill (in screen pixels coordinate)
     */
    public static void fillRectangle(Rectangle r) {
        AStarDouble.aStar.changeRectangle(r, AStarDouble.tileSize, false);
    }

    /** To dynamically modify the mask setting all pixels
     * of a rectangle to true
     *
     * @param r the rectangle to clean (in screen pixels coordinate)
     */
    public static void cleanRectangle(Rectangle r) {
        AStarDouble.aStar.changeRectangle(r, AStarDouble.tileSize, true);
    }
}
