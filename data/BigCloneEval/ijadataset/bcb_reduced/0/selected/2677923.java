package eu.irreality.dai.world.gen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import eu.irreality.dai.gameplay.world.LevelDescriptor;
import eu.irreality.dai.ui.cell.DisplayableObject;
import eu.irreality.dai.util.Debug;
import eu.irreality.dai.util.GridUtilities;
import eu.irreality.dai.util.Roll;
import eu.irreality.dai.util.SortedList;
import eu.irreality.dai.world.gen.strategy.SimpleStairGenerationStrategy;
import eu.irreality.dai.world.gen.strategy.StairGenerationStrategy;
import eu.irreality.dai.world.level.DungeonLevel;
import eu.irreality.dai.world.level.Level;

class DungeonSection {

    private static Random rand = new Random();

    /**
     * Minimal dimensions of the section
     */
    private int MIN_W = 5;

    private int MIN_H = 5;

    /**
     * Minimal dimensions of a room
     */
    private int MIN_ROOM_W = 3;

    private int MIN_ROOM_H = 3;

    /**
     * Maximal dimensions of a room
     */
    private int MAX_ROOM_W = 10;

    private int MAX_ROOM_H = 10;

    /**
     * Coordinates of the section
     */
    private int x;

    private int y;

    /**
     * Dimensions of the section
     */
    private int w;

    private int h;

    /**
     * Section type
     */
    public static final int NOTHING = 0;

    public static final int CORRIDORS = 1;

    public static final int ROOM = 2;

    private int type;

    /**
     * Section representative point. If it's a room, it's any point in the room.
     * If it's a corridor section, it's any point in the section (randomly
     * chosen)
     */
    private int represX;

    private int represY;

    /**
     * Coordinates and dimensions of the room, if type = ROOM.
     */
    private int roomX;

    private int roomY;

    private int roomW;

    private int roomH;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString() {
        return "(" + x + "," + y + "," + w + "," + h + ")";
    }

    public DungeonSection() {
    }

    public DungeonSection(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int area() {
        return w * h;
    }

    public int getRepresX() {
        return represX;
    }

    public int getRepresY() {
        return represY;
    }

    public void digRoom() {
        type = ROOM;
        int minRoomW = MIN_ROOM_W;
        int minRoomH = MIN_ROOM_H;
        int maxRoomW = Math.min(MAX_ROOM_W, w);
        int maxRoomH = Math.min(MAX_ROOM_H, h);
        roomW = Roll.randomInt(minRoomW, maxRoomW);
        roomH = Roll.randomInt(minRoomH, maxRoomH);
        int maxXOffset = w - roomW;
        int maxYOffset = h - roomH;
        roomX = x + Roll.randomInt(maxXOffset);
        roomY = y + Roll.randomInt(maxYOffset);
    }

    public void chooseRepresentative() {
        if (type == NOTHING) {
            represX = x;
            represY = y;
        } else if (type == CORRIDORS) {
            represX = x + Roll.randomInt(w - 1);
            represY = y + Roll.randomInt(h - 1);
        } else if (type == ROOM) {
            represX = roomX + Roll.randomInt(roomW - 1);
            represY = roomY + Roll.randomInt(roomH - 1);
        }
    }

    public DungeonSection[] divideIntoTwo() {
        if ((w < (2 * MIN_W + 1)) && (h < (2 * MIN_H + 1))) return null; else if ((w >= (2 * MIN_W + 1)) && (h >= (2 * MIN_H + 1))) {
            if (Roll.chance(50)) return divideVertically(); else return divideHorizontally();
        } else if (w >= (2 * MIN_W + 1)) return divideVertically(); else return divideHorizontally();
    }

    public DungeonSection[] divideVertically() {
        int minDivisionPoint = MIN_W;
        int maxDivisionPoint = w - 1 - MIN_W;
        int divisionPoint = Roll.randomInt(minDivisionPoint, maxDivisionPoint);
        DungeonSection una = new DungeonSection();
        una.x = this.x;
        una.y = this.y;
        una.w = divisionPoint;
        una.h = this.h;
        DungeonSection dos = new DungeonSection();
        dos.x = this.x + divisionPoint + 1;
        dos.y = this.y;
        dos.w = this.w - divisionPoint - 1;
        dos.h = this.h;
        DungeonSection[] result = new DungeonSection[2];
        result[0] = una;
        result[1] = dos;
        return result;
    }

    public DungeonSection[] divideHorizontally() {
        int minDivisionPoint = MIN_H;
        int maxDivisionPoint = h - 1 - MIN_H;
        int divisionPoint = Roll.randomInt(minDivisionPoint, maxDivisionPoint);
        DungeonSection una = new DungeonSection();
        una.x = this.x;
        una.y = this.y;
        una.w = this.w;
        una.h = divisionPoint;
        DungeonSection dos = new DungeonSection();
        dos.x = this.x;
        dos.y = this.y + divisionPoint + 1;
        dos.w = this.w;
        dos.h = this.h - divisionPoint - 1;
        DungeonSection[] result = new DungeonSection[2];
        result[0] = una;
        result[1] = dos;
        return result;
    }

    public DisplayableObject getObjectAt(int atX, int atY) {
        if (atX < x || atY < y || atX > x + w - 1 || atY > y + h - 1) {
            return null;
        } else {
            if (type == NOTHING) {
                return DungeonGenerator.WALL;
            } else if (type == CORRIDORS) {
                if (atX == represX && atY == represY) return DungeonGenerator.GROUND; else return DungeonGenerator.WALL;
            } else if (type == ROOM) {
                if (atX >= roomX && atX < roomX + roomW && atY >= roomY && atY < roomY + roomH) return DungeonGenerator.GROUND; else return DungeonGenerator.WALL;
            } else {
                return null;
            }
        }
    }

    public void writeToGrid(DisplayableObject[][] grid) {
        if (type == CORRIDORS) grid[represY][represX] = DungeonGenerator.GROUND; else if (type == ROOM) {
            Debug.println("Paint room: " + x + "," + y + "," + w + "," + h + ":" + roomX + "," + roomY + "," + roomW + "," + roomH);
            for (int x = roomX; x < roomX + roomW; x++) for (int y = roomY; y < roomY + roomH; y++) grid[y][x] = DungeonGenerator.GROUND;
        }
    }

    /**
     * If the section is a room, this method marks the walls as
     * horizontally/vertically undiggable for the generator, as it corresponds:
     * 
     * *HHH* H = horizontally undiggable V...V V = vertically undiggable V...V *
     * = undiggable in any direction V...V . = room floor *HHH*
     * 
     * @param horizontallyDiggable
     * @param verticallyDiggable
     */
    public void markUndiggables(boolean[][] horizontallyDiggable, boolean[][] verticallyDiggable) {
        if (type == ROOM) {
            for (int x = roomX - 1; x < roomX + roomW + 1; x++) {
                for (int y = roomY - 1; y < roomY + roomH + 1; y++) {
                    if (x >= 0 && y >= 0 && x < verticallyDiggable[0].length && y < horizontallyDiggable.length) {
                        if (x == roomX - 1 || x == roomX + roomW) verticallyDiggable[y][x] = false;
                        if (y == roomY - 1 || y == roomY + roomH) horizontallyDiggable[y][x] = false;
                    }
                }
            }
        }
    }

    public void createRoomDoors(DisplayableObject[][] grid) {
        if (type == ROOM) {
            for (int x = roomX - 1; x < roomX + roomW + 1; x++) {
                for (int y = roomY - 1; y < roomY + roomH + 1; y++) {
                    if (x >= 0 && y >= 0 && x < grid[0].length && y < grid.length) {
                        if (x == roomX - 1 || x == roomX + roomW || y == roomY - 1 || y == roomY + roomH) if (grid[y][x] == DungeonGenerator.GROUND) createDoor(grid, x, y);
                    }
                }
            }
        }
    }

    public void createDoor(DisplayableObject[][] grid, int x, int y) {
        if (LevelGenerator.rand.nextInt(100) < 50) {
            grid[y][x] = LevelGenerator.OPEN_DOOR;
        } else {
            grid[y][x] = LevelGenerator.CLOSED_DOOR;
        }
    }

    /**
     * Manhattan distance between representative points.
     * 
     * @param another
     * @return
     */
    public int manhattan(DungeonSection another) {
        return Math.abs(this.represX - another.represX) + Math.abs(this.represY - another.represY);
    }
}

public class DungeonGenerator extends LevelGenerator {

    private static final int PERCENT_ROOM = 50;

    private static final int PERCENT_CORRIDORS = 40;

    private static final int PERCENT_NOTHING = 10;

    private static final boolean ELIMINATE_DEAD_ENDS = true;

    private DisplayableObject[][] grid;

    private boolean[][] horizontallyDiggable;

    private boolean[][] verticallyDiggable;

    private StairGenerationStrategy sgs = new SimpleStairGenerationStrategy();

    public void setStairGenerationStrategy(StairGenerationStrategy sgs) {
        this.sgs = sgs;
    }

    public Set<DungeonSection> generateSections(int rows, int cols) {
        DungeonSection bigSection = new DungeonSection(1, 1, cols - 2, rows - 2);
        Set<DungeonSection> sections = new LinkedHashSet<DungeonSection>();
        sections.add(bigSection);
        boolean done = false;
        while (!done) {
            done = true;
            Set<DungeonSection> nextSet = new LinkedHashSet<DungeonSection>();
            for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
                DungeonSection sec = (DungeonSection) iter.next();
                DungeonSection[] small = sec.divideIntoTwo();
                if (small == null) {
                    nextSet.add(sec);
                } else {
                    done = false;
                    nextSet.add(small[0]);
                    nextSet.add(small[1]);
                }
            }
            sections = nextSet;
        }
        return sections;
    }

    private void makeDungeonDiggable(int rows, int cols) {
        horizontallyDiggable = new boolean[rows][cols];
        verticallyDiggable = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i > 0 && j > 0 && i < rows - 1 && j < cols - 1) {
                    horizontallyDiggable[i][j] = true;
                    verticallyDiggable[i][j] = true;
                }
            }
        }
    }

    public DisplayableObject[][] generateDisconnectedRooms(int rows, int cols) {
        grid = new DisplayableObject[rows][cols];
        makeDungeonDiggable(rows, cols);
        Set<DungeonSection> sections = generateSections(rows, cols);
        for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) grid[i][j] = WALL;
        Debug.println(sections);
        for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
            DungeonSection sec = (DungeonSection) iter.next();
            for (int i = sec.getY(); i < sec.getY() + sec.getH(); i++) for (int j = sec.getX(); j < sec.getX() + sec.getW(); j++) {
                grid[i][j] = GROUND;
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == WALL) System.out.print("#"); else System.out.print(".");
            }
            System.out.println();
        }
        return grid;
    }

    public void digBestTunnel(DisplayableObject[][] grid, boolean[][] horizontallyDiggable, boolean[][] verticallyDiggable, int x1, int y1, int x2, int y2) {
        List<AStarNode> nodes = findBestPath(grid, horizontallyDiggable, verticallyDiggable, x1, y1, x2, y2);
        AStarNode last = null;
        for (Iterator<AStarNode> iter = nodes.iterator(); iter.hasNext(); ) {
            AStarNode node = iter.next();
            grid[node.getY()][node.getX()] = GROUND;
            if (last != null) {
                int dx = node.getX() - last.getX();
                int dy = node.getY() - last.getY();
                if (dx != 0 && dy == 0) {
                    if (node.getY() - 1 > 0) {
                        horizontallyDiggable[node.getY() - 1][node.getX()] = false;
                        horizontallyDiggable[last.getY() - 1][last.getX()] = false;
                    }
                    if (node.getY() + 1 < horizontallyDiggable.length) {
                        horizontallyDiggable[node.getY() + 1][node.getX()] = false;
                        horizontallyDiggable[last.getY() + 1][last.getX()] = false;
                    }
                }
                if (dx == 0 && dy != 0) {
                    if (node.getX() - 1 > 0) {
                        verticallyDiggable[node.getY()][node.getX() - 1] = false;
                        verticallyDiggable[last.getY()][last.getX() - 1] = false;
                    }
                    if (node.getX() + 1 < verticallyDiggable[0].length) {
                        verticallyDiggable[node.getY()][node.getX() + 1] = false;
                        verticallyDiggable[last.getY()][last.getX() + 1] = false;
                    }
                }
            }
            last = node;
        }
    }

    public void digTunnel(DisplayableObject[][] grid, int x1, int y1, int x2, int y2) {
        int xmean = (x1 + x2) / 2;
        int ymean = (y1 + y2) / 2;
        int mode = Roll.randomInt(3);
        switch(mode) {
            case 0:
                digTunnelXFirst(grid, x1, y1, x2, y2);
                break;
            case 1:
                digTunnelYFirst(grid, x1, y1, x2, y2);
                break;
            case 2:
                digTunnelXFirst(grid, x1, y1, xmean, ymean);
                digTunnelYFirst(grid, xmean, ymean, x2, y2);
                break;
            case 3:
                digTunnelYFirst(grid, x1, y1, xmean, ymean);
                digTunnelXFirst(grid, xmean, ymean, x2, y2);
                break;
        }
    }

    public void digTunnelXFirst(DisplayableObject[][] grid, int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
        for (int x = x1; x <= x2; x++) {
            grid[y1][x] = GROUND;
        }
        if (y1 <= y2) {
            for (int y = y1; y <= y2; y++) {
                grid[y][x2] = GROUND;
            }
        } else {
            for (int y = y1; y >= y2; y--) {
                grid[y][x2] = GROUND;
            }
        }
    }

    public void digTunnelYFirst(DisplayableObject[][] grid, int x1, int y1, int x2, int y2) {
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        for (int y = y1; y <= y2; y++) {
            grid[y][x1] = GROUND;
        }
        if (x1 <= x2) {
            for (int x = x1; x <= x2; x++) {
                grid[y2][x] = GROUND;
            }
        } else {
            for (int x = x1; x >= x2; x--) {
                grid[y2][x] = GROUND;
            }
        }
    }

    interface AStarHeuristic {

        public int calculate(AStarNode n);
    }

    class ManhattanAStarHeuristic implements AStarHeuristic {

        int destx, desty;

        public ManhattanAStarHeuristic(int destx, int desty) {
            this.destx = destx;
            this.desty = desty;
        }

        public int calculate(AStarNode n) {
            int origx = n.getX();
            int origy = n.getY();
            return Math.abs(destx - origx) + Math.abs(desty - origy);
        }
    }

    class HeuristicNodeComparator implements Comparator<AStarNode> {

        private AStarHeuristic heuristic;

        public HeuristicNodeComparator(AStarHeuristic heuristic) {
            this.heuristic = heuristic;
        }

        public int compare(AStarNode arg0, AStarNode arg1) {
            int valueFor0 = arg0.getAccumulatedCost() + heuristic.calculate(arg0);
            int valueFor1 = arg1.getAccumulatedCost() + heuristic.calculate(arg1);
            return valueFor0 - valueFor1;
        }
    }

    class AStarNode {

        public static final int HORIZONTAL = 0;

        public static final int VERTICAL = 1;

        public static final int NONE = 2;

        public static final int NORTH = 0;

        public static final int SOUTH = 1;

        public static final int WEST = 2;

        public static final int EAST = 3;

        public static final int OPERATOR_BOUND = 4;

        public static final int EASY = 1;

        public static final int MEDIUM = 3;

        public static final int HARD = 10;

        public static final int HUGE = 1000;

        public static final int INFINITE = 1000000;

        private int x;

        private int y;

        private int cameFromDirection;

        private int accumulatedCost;

        private AStarNode parent;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getAccumulatedCost() {
            return accumulatedCost;
        }

        public AStarNode getParent() {
            return parent;
        }

        public AStarNode(int x, int y) {
            this.x = x;
            this.y = y;
            this.accumulatedCost = 0;
        }

        private AStarNode(int x, int y, int cameFromDirection, AStarNode parent, int accumulatedCost) {
            this.x = x;
            this.y = y;
            this.cameFromDirection = cameFromDirection;
            this.parent = parent;
            this.accumulatedCost = accumulatedCost;
        }

        public int operatorCost(DisplayableObject[][] grid, boolean[][] horizontallyDiggable, boolean[][] verticallyDiggable, int operator) {
            int destX = x;
            int destY = y;
            if (operator == NORTH) destY = y - 1;
            if (operator == SOUTH) destY = y + 1;
            if (operator == WEST) destX = x - 1;
            if (operator == EAST) destX = x + 1;
            if (destX < 0 || destX >= grid[0].length || destY < 0 || destY >= grid.length) return INFINITE;
            int headingToDirection = NONE;
            if (operator == NORTH || operator == SOUTH) headingToDirection = VERTICAL;
            if (operator == WEST || operator == EAST) headingToDirection = HORIZONTAL;
            if ((!horizontallyDiggable[y][x] || !horizontallyDiggable[destY][destX]) && grid[destY][destX] == WALL) if (cameFromDirection == HORIZONTAL || headingToDirection == HORIZONTAL) return HUGE;
            if ((!verticallyDiggable[y][x] || !verticallyDiggable[destY][destX]) && grid[destY][destX] == WALL) if (cameFromDirection == VERTICAL || headingToDirection == VERTICAL) return HUGE;
            if (grid[destY][destX] == DungeonGenerator.GROUND) return HARD;
            if (cameFromDirection != headingToDirection) return MEDIUM;
            return EASY;
        }

        public AStarNode applyOperator(DisplayableObject[][] grid, boolean[][] horizontallyDiggable, boolean[][] verticallyDiggable, int operator) {
            int destX = x;
            int destY = y;
            if (operator == NORTH) destY = y - 1;
            if (operator == SOUTH) destY = y + 1;
            if (operator == WEST) destX = x - 1;
            if (operator == EAST) destX = x + 1;
            if (destX < 0 || destX >= grid[0].length || destY < 0 || destY >= grid.length) return null;
            int headingToDirection = NONE;
            if (operator == NORTH || operator == SOUTH) headingToDirection = VERTICAL;
            if (operator == WEST || operator == EAST) headingToDirection = HORIZONTAL;
            return new AStarNode(destX, destY, headingToDirection, this, this.accumulatedCost + this.operatorCost(grid, horizontallyDiggable, verticallyDiggable, operator));
        }

        public String toString() {
            return "node[x=" + x + ", y=" + y + ", cost=" + getAccumulatedCost() + "]";
        }

        public boolean isGoal(AStarHeuristic heu) {
            return heu.calculate(this) == 0;
        }

        public AStarNode findPath(AStarHeuristic heu, int destX, int destY, DisplayableObject[][] grid, boolean[][] horizontallyDiggable, boolean[][] verticallyDiggable, int maxAllowedCost) {
            boolean[][] visited = new boolean[grid.length][grid[0].length];
            Comparator<AStarNode> comparator = new HeuristicNodeComparator(heu);
            PriorityQueue<AStarNode> nodes = new PriorityQueue<AStarNode>(30, comparator);
            nodes.add(this);
            int iters = 0;
            while (!nodes.isEmpty()) {
                iters++;
                if (iters % 10000 == 0) {
                    Debug.println("A* iter " + iters);
                    Debug.println("sz " + nodes.size());
                    Debug.println(nodes);
                }
                AStarNode curNode = nodes.poll();
                if (curNode.getAccumulatedCost() > 1000) {
                    Debug.println("Acc cost > 1000 at node " + curNode + " on path from " + x + "," + y + " to " + destX + "," + destY);
                }
                if (visited[curNode.getY()][curNode.getX()]) continue;
                if (curNode.getAccumulatedCost() > maxAllowedCost) return null;
                visited[curNode.getY()][curNode.getX()] = true;
                if (curNode.isGoal(heu)) {
                    System.out.println("Goal for path from " + x + "," + y + " to " + destX + "," + destY + " is node " + curNode + " with acc cost " + curNode.getAccumulatedCost());
                    return curNode;
                } else {
                    for (int i = 0; i < OPERATOR_BOUND; i++) {
                        if (curNode.operatorCost(grid, horizontallyDiggable, verticallyDiggable, i) < INFINITE) {
                            AStarNode child = curNode.applyOperator(grid, horizontallyDiggable, verticallyDiggable, i);
                            if (child.isGoal(heu)) return child;
                            nodes.add(child);
                        }
                    }
                }
            }
            return null;
        }
    }

    public List<AStarNode> findBestPath(DisplayableObject[][] grid, boolean[][] horizontallyDiggable, boolean[][] verticallyDiggable, int x1, int y1, int x2, int y2) {
        AStarNode beginning = new AStarNode(x1, y1);
        AStarHeuristic heuristic = new ManhattanAStarHeuristic(x2, y2);
        AStarNode goalNode = beginning.findPath(heuristic, x2, y2, grid, horizontallyDiggable, verticallyDiggable, 2000);
        Debug.println("Best path cost: " + goalNode.getAccumulatedCost());
        LinkedList<AStarNode> path = new LinkedList<AStarNode>();
        AStarNode current = goalNode;
        while (current != null) {
            path.addFirst(current);
            current = current.getParent();
        }
        Debug.println("Path: " + path);
        return path;
    }

    public void eliminateDeadEnd(int x, int y, DisplayableObject[][] grid) {
        if (grid[y][x] != DungeonGenerator.GROUND) return;
        if (x == 0 || y == 0 || y == grid.length - 1 || x == grid[0].length - 1) return;
        DisplayableObject north = grid[y - 1][x];
        DisplayableObject south = grid[y + 1][x];
        DisplayableObject west = grid[y][x - 1];
        DisplayableObject east = grid[y][x + 1];
        if (north == DungeonGenerator.WALL && west == DungeonGenerator.WALL && east == DungeonGenerator.WALL && south != DungeonGenerator.WALL) {
            Debug.println("N dead end elim: " + "(" + x + "," + y + ")");
            grid[y][x] = DungeonGenerator.WALL;
            eliminateDeadEnd(x, y + 1, grid);
        }
        if (north == DungeonGenerator.WALL && west == DungeonGenerator.WALL && east != DungeonGenerator.WALL && south == DungeonGenerator.WALL) {
            Debug.println("W dead end elim: " + "(" + x + "," + y + ")");
            grid[y][x] = DungeonGenerator.WALL;
            eliminateDeadEnd(x + 1, y, grid);
        }
        if (north == DungeonGenerator.WALL && west != DungeonGenerator.WALL && east == DungeonGenerator.WALL && south == DungeonGenerator.WALL) {
            Debug.println("E dead end elim: " + "(" + x + "," + y + ")");
            grid[y][x] = DungeonGenerator.WALL;
            eliminateDeadEnd(x - 1, y, grid);
        }
        if (north != DungeonGenerator.WALL && west == DungeonGenerator.WALL && east == DungeonGenerator.WALL && south == DungeonGenerator.WALL) {
            Debug.println("S dead end elim: " + "(" + x + "," + y + ")");
            grid[y][x] = DungeonGenerator.WALL;
            eliminateDeadEnd(x, y - 1, grid);
        }
    }

    public void eliminateDeadEnds(DisplayableObject[][] grid) {
        for (int row = 0; row < grid.length; row++) for (int col = 0; col < grid[0].length; col++) eliminateDeadEnd(col, row, grid);
    }

    public void eliminateDoubleDoors(DisplayableObject[][] grid) {
        for (int row = 0; row < grid.length - 1; row++) for (int col = 0; col < grid[0].length - 1; col++) if (grid[row][col] == LevelGenerator.OPEN_DOOR || grid[row][col] == LevelGenerator.CLOSED_DOOR) {
            if (grid[row + 1][col] == LevelGenerator.OPEN_DOOR || grid[row + 1][col] == LevelGenerator.CLOSED_DOOR) {
                if (Roll.chance(50)) grid[row][col] = DungeonGenerator.GROUND; else grid[row + 1][col] = DungeonGenerator.GROUND;
            }
            if (grid[row][col + 1] == LevelGenerator.OPEN_DOOR || grid[row][col + 1] == LevelGenerator.CLOSED_DOOR) {
                if (Roll.chance(50)) grid[row][col] = DungeonGenerator.GROUND; else grid[row][col + 1] = DungeonGenerator.GROUND;
            }
        }
    }

    private int rows = 25;

    private int cols = 80;

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public Level generate(LevelDescriptor ld) {
        System.out.println("Generating rows " + rows + " cols " + cols);
        Level l = generate(rows, cols);
        System.out.println("Generated rows " + l.getRows() + " cols " + l.getCols());
        l.setDescriptor(ld);
        sgs.addStairs(l);
        return l;
    }

    public Level generate(int rows, int cols) {
        grid = new DisplayableObject[rows][cols];
        makeDungeonDiggable(rows, cols);
        Set<DungeonSection> sections = generateSections(rows, cols);
        for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) grid[i][j] = WALL;
        Debug.println(sections);
        for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
            DungeonSection sec = (DungeonSection) iter.next();
            int roll = Roll.percent();
            if (roll < PERCENT_ROOM) {
                sec.digRoom();
                sec.chooseRepresentative();
            } else if (roll < PERCENT_ROOM + PERCENT_CORRIDORS) {
                sec.setType(DungeonSection.CORRIDORS);
                sec.chooseRepresentative();
            } else {
                sec.setType(DungeonSection.NOTHING);
            }
        }
        DungeonSection[] sectionArray = sections.toArray(new DungeonSection[0]);
        List<DungeonSection> validSectionList = new ArrayList<DungeonSection>();
        for (int i = 0; i < sectionArray.length; i++) {
            if (sectionArray[i].getType() != DungeonSection.NOTHING) {
                validSectionList.add(sectionArray[i]);
            }
        }
        int nValidSections = validSectionList.size();
        int[][] sectionGraphWeight = new int[nValidSections][nValidSections];
        boolean[][] connection = new boolean[nValidSections][nValidSections];
        for (int i = 0; i < nValidSections; i++) {
            for (int j = 0; j < nValidSections; j++) {
                sectionGraphWeight[i][j] = validSectionList.get(i).manhattan(validSectionList.get(j));
                connection[i][j] = false;
            }
        }
        int[] conncomp = new int[nValidSections];
        for (int i = 0; i < nValidSections; i++) conncomp[i] = i;
        boolean done = false;
        while (!done) {
            int chosenIndex = Roll.randomInt(nValidSections - 1);
            int min = 99999;
            int anotherIndex = -1;
            for (int j = 0; j < nValidSections; j++) {
                Debug.println("cc " + conncomp[j]);
                if (j != chosenIndex) {
                    if (sectionGraphWeight[chosenIndex][j] < min && conncomp[chosenIndex] != conncomp[j]) {
                        min = sectionGraphWeight[chosenIndex][j];
                        anotherIndex = j;
                    }
                }
            }
            int yetAnotherIndex = -1;
            for (int j = 0; j < nValidSections; j++) {
                if (j != anotherIndex) {
                    if (sectionGraphWeight[anotherIndex][j] < min && conncomp[anotherIndex] != conncomp[j]) {
                        min = sectionGraphWeight[anotherIndex][j];
                        yetAnotherIndex = j;
                    }
                }
            }
            int connComp1, connComp2, newConnComp;
            if (yetAnotherIndex >= 0) {
                connection[anotherIndex][yetAnotherIndex] = true;
                connection[yetAnotherIndex][anotherIndex] = true;
                connComp1 = conncomp[anotherIndex];
                connComp2 = conncomp[yetAnotherIndex];
                newConnComp = Math.min(connComp1, connComp2);
            } else {
                connection[chosenIndex][anotherIndex] = true;
                connection[anotherIndex][chosenIndex] = true;
                connComp1 = conncomp[chosenIndex];
                connComp2 = conncomp[anotherIndex];
                newConnComp = Math.min(connComp1, connComp2);
            }
            done = true;
            for (int i = 0; i < nValidSections; i++) {
                if (conncomp[i] == connComp1 || conncomp[i] == connComp2) conncomp[i] = newConnComp;
                if (conncomp[i] > 0) done = false;
            }
        }
        int[] numConnectionsForSection = new int[nValidSections];
        for (int i = 0; i < nValidSections; i++) {
            for (int j = 0; j < nValidSections; j++) {
                if (connection[i][j]) {
                    numConnectionsForSection[i]++;
                    numConnectionsForSection[j]++;
                }
            }
        }
        for (int i = 0; i < nValidSections; i++) {
            if (validSectionList.get(i).getType() == DungeonSection.CORRIDORS) {
                Debug.println("Conns " + numConnectionsForSection[i]);
            }
            if (validSectionList.get(i).getType() == DungeonSection.CORRIDORS && numConnectionsForSection[i] == 2) {
                int min = 99999;
                int anotherIndex = -1;
                for (int j = 0; j < nValidSections; j++) {
                    if (j != i) {
                        if (connection[i][j] || connection[j][i]) Debug.println("Existing conn " + validSectionList.get(i) + " - " + validSectionList.get(j));
                        if (sectionGraphWeight[i][j] < min && !connection[i][j] && !connection[j][i]) {
                            min = sectionGraphWeight[i][j];
                            anotherIndex = j;
                        }
                    }
                }
                Debug.println("Extra conn " + validSectionList.get(i) + " - " + validSectionList.get(anotherIndex));
                connection[i][anotherIndex] = true;
                connection[anotherIndex][i] = true;
            }
        }
        for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
            DungeonSection sec = (DungeonSection) iter.next();
            sec.writeToGrid(grid);
        }
        for (int i = 0; i < nValidSections; i++) {
            if (validSectionList.get(i).getType() == DungeonSection.ROOM) {
                validSectionList.get(i).markUndiggables(horizontallyDiggable, verticallyDiggable);
            }
        }
        for (int i = 0; i < nValidSections; i++) {
            for (int j = i + 1; j < nValidSections; j++) {
                if (connection[i][j]) {
                    digBestTunnel(grid, horizontallyDiggable, verticallyDiggable, validSectionList.get(i).getRepresX(), validSectionList.get(i).getRepresY(), validSectionList.get(j).getRepresX(), validSectionList.get(j).getRepresY());
                }
            }
        }
        for (int i = 0; i < nValidSections; i++) {
            if (validSectionList.get(i).getType() == DungeonSection.ROOM) {
                validSectionList.get(i).createRoomDoors(grid);
            }
        }
        if (ELIMINATE_DEAD_ENDS) eliminateDeadEnds(grid);
        eliminateDoubleDoors(grid);
        SortedList[][] levelGrid = GridUtilities.simpleToComplexGrid(grid);
        DungeonLevel dl = new DungeonLevel(rows, cols);
        dl.setGrid(levelGrid);
        return dl;
    }

    public static void main(String[] args) {
        DungeonGenerator dg = new DungeonGenerator();
        for (int i = 0; i < 100; i++) {
            dg.generate(25, 80);
            System.out.println();
        }
    }
}
