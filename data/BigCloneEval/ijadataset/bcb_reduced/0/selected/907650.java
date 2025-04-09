package wotlas.libs.pathfinding;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import wotlas.utils.List;

/** A* algorithm finds the optimal path between 2 points 
 *
 * usage:
 * - create a AStar object
 * - initialize the mask with a buffered image
 *   AStar.initMask(BufferedImage maskBuffImg, int imgWidth, int imgHeight)
 * - start the search
 *   AStarObject.findPath(startPoint, goalPoint);
 *
 * @author Petrus
 * @see wotlas.libs.pathfinding.Node
 */
public class AStar {

    /** mask of the image : mask[i][j] is true if pixel(i,j) is not blocked
     */
    private boolean[][] map = new boolean[100][100];

    /** width of the map
     */
    private int mapWidth;

    /** height of the map
     */
    private int mapHeight;

    /** start of the path
     */
    private Point pointStart;

    /** goal of the path
     */
    private Point pointGoal;

    /** list of not visited {@link Node Nodes}
     */
    private Hashtable<Point, Node> open = new Hashtable<Point, Node>(500);

    /** list of visited {@link Node Nodes}
     */
    private Hashtable<Point, Node> closed = new Hashtable<Point, Node>(500);

    private List nodes = new List();

    /**
     * Estimates the distance between 2 points
     * 
     * @param poinFrom first point
     * @param pointTo second point
     * @return the distance between the 2 points
     */
    private int estimate(Point pointFrom, Point pointTo) {
        return (int) pointFrom.distanceSq(pointTo);
    }

    /**
     * begins optimal path search
     */
    private Node searchNode() {
        Node bestNode;
        List childPoints;
        int childCost;
        List children = new List();
        while (!this.nodes.isEmpty()) {
            bestNode = (Node) this.nodes.elementAt(0);
            if (this.closed.get(bestNode.point) != null) {
                this.nodes.removeFirstElement();
                continue;
            }
            if ((bestNode.point.x == this.pointGoal.x) && (bestNode.point.y == this.pointGoal.y)) {
                return bestNode;
            } else {
            }
            children.removeAllElements();
            childPoints = generateChildren(bestNode.point);
            for (int i = 0; i < childPoints.size(); i++) {
                Point childPoint;
                Node closedNode = null;
                Node openNode = null;
                Node oldNode = null;
                childPoint = (Point) childPoints.elementAt(i);
                childCost = bestNode.g + 1;
                if ((closedNode = this.closed.get(childPoint)) == null) {
                    openNode = this.open.get(childPoint);
                }
                oldNode = (openNode != null) ? openNode : closedNode;
                if (oldNode != null) {
                    if (childCost < oldNode.g) {
                        if (closedNode != null) {
                            this.open.put(childPoint, oldNode);
                            this.closed.remove(childPoint);
                        } else {
                            int estimation = oldNode.h;
                            oldNode = new Node();
                            oldNode.point = childPoint;
                            oldNode.parent = bestNode;
                            oldNode.g = childCost;
                            oldNode.h = estimation;
                            oldNode.f = childCost + estimation;
                            this.open.put(childPoint, oldNode);
                        }
                        oldNode.parent = bestNode;
                        oldNode.g = childCost;
                        oldNode.f = childCost + oldNode.h;
                        children.addElement(oldNode);
                    }
                } else {
                    int estimation;
                    Node newNode = new Node();
                    newNode.point = childPoint;
                    newNode.parent = bestNode;
                    estimation = estimate(childPoint, this.pointGoal);
                    newNode.h = estimation;
                    newNode.g = childCost;
                    newNode.f = childCost + estimation;
                    this.open.put(childPoint, newNode);
                    children.addElement(newNode);
                }
            }
            this.open.remove(bestNode.point);
            this.closed.put(bestNode.point, bestNode);
            this.nodes.removeFirstElement();
            addToNodes(children);
        }
        System.out.println("no path found");
        return null;
    }

    /**
     *
     */
    private int rbsearch(int l, int h, int tot, int costs) {
        if (l > h) {
            return l;
        }
        int cur = (l + h) / 2;
        int ot = ((Node) this.nodes.elementAt(cur)).f;
        if ((tot < ot) || (tot == ot && costs >= ((Node) this.nodes.elementAt(cur)).g)) {
            return rbsearch(l, cur - 1, tot, costs);
        }
        return rbsearch(cur + 1, h, tot, costs);
    }

    /**
     *
     */
    private int bsearch(int l, int h, int tot, int costs) {
        int lo = l;
        int hi = h;
        while (lo <= hi) {
            int cur = (lo + hi) / 2;
            int ot = ((Node) this.nodes.elementAt(cur)).f;
            if ((tot < ot) || (tot == ot && costs >= ((Node) this.nodes.elementAt(cur)).g)) {
                hi = cur - 1;
            } else {
                lo = cur + 1;
            }
        }
        return lo;
    }

    private void addToNodes(List children) {
        Node newNode;
        int idx;
        for (int i = 0; i < children.size(); i++) {
            newNode = (Node) children.elementAt(i);
            idx = bsearch(0, this.nodes.size() - 1, newNode.f, newNode.g);
            this.nodes.insertElementAt(newNode, idx);
        }
    }

    /**
     * test if a point is valid for the path
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if point is valid (not blocked) in the {@link #mask mask}
     */
    public boolean isNotBlock(int x, int y) {
        if ((x < 0) || (x == this.mapWidth) || (y < 0) || (y == this.mapHeight)) {
            return false;
        }
        return this.map[x][y];
    }

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

    public List findPath(Point pStart, Point pGoal) {
        Node firstNode = new Node();
        Node solution = new Node();
        int estimation;
        int cost;
        this.pointStart = pStart;
        this.pointGoal = pGoal;
        if ((!isNotBlock(pStart.x, pStart.y)) || (!isNotBlock(pGoal.x, pGoal.y))) {
            System.err.println("error : invalid point");
            return null;
        }
        firstNode.point = this.pointStart;
        cost = 0;
        estimation = estimate(this.pointStart, this.pointGoal);
        firstNode.g = cost;
        firstNode.h = estimation;
        firstNode.f = cost + estimation;
        firstNode.parent = null;
        this.open.put(this.pointStart, firstNode);
        this.nodes.addElement(firstNode);
        solution = searchNode();
        this.nodes.removeAllElements();
        this.open.clear();
        this.closed.clear();
        return getPath(solution);
    }

    private List getPath(Node n) {
        List result;
        if (n == null) {
            result = new List();
        } else {
            result = getPath(n.parent);
            result.addElement(n.point);
        }
        return result;
    }

    public void showPath(List path) {
        Point pathPoint;
        for (int i = 0; i < path.size(); i++) {
            pathPoint = (Point) path.elementAt(i);
            System.out.print("(" + pathPoint.x + "," + pathPoint.y + ") ");
        }
    }

    /**
     * initializes the array "map" with a BufferedImage
     */
    public void initMask(BufferedImage maskBuffImg, int imgWidth, int imgHeight) {
        this.mapWidth = imgWidth;
        this.mapHeight = imgHeight;
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                this.map[i][j] = (maskBuffImg.getRGB(i, j) == -1) ? false : true;
            }
        }
    }
}
