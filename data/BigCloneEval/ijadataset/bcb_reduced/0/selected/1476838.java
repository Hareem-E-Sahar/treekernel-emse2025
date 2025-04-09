package com.bbn.vessel.author.graphEditor.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.bbn.vessel.author.graphEditor.editor.NamedObject;
import com.bbn.vessel.author.graphEditor.views.AbstractView;
import com.bbn.vessel.author.graphEditor.views.ConnectionEnd;
import com.bbn.vessel.author.graphEditor.views.Draggable;
import com.bbn.vessel.author.graphEditor.views.GraphViews;
import com.bbn.vessel.author.graphEditor.views.ManhattanConnectionView;
import com.bbn.vessel.author.graphEditor.views.NodeView;
import com.bbn.vessel.author.graphEditor.views.SplineConnectionView;
import com.bbn.vessel.author.graphEditor.views.TerminalView;
import com.bbn.vessel.author.graphEditor.views.View;
import com.bbn.vessel.author.models.GraphElement;
import com.bbn.vessel.author.models.Side;

/**
 * <Enter the description of this type here>
 * 
 * @author RTomlinson
 */
public class ManhattanRouter extends AbstractView {

    /**
   *
   */
    private static final int EXTRA_STEPS = 10;

    static class ScoredRuns {

        List<Run> bestRuns = null;

        int bestScore;

        void update(List<Run> runs, int score) {
            if (bestRuns == null || score < bestScore) {
                bestScore = score;
                bestRuns = runs;
            }
        }
    }

    /**
   *
   */
    private static final int CORNER_PENALTY = 10;

    private static final int REVERSAL_PENALTY = 5;

    /** The grid size */
    public static final int grid = 20;

    private static final int STEPS_FACTOR = 2;

    private final Stroke stroke = new BasicStroke(1f);

    private final SortedSet<Run> horizontalRuns = new TreeSet<Run>();

    private final SortedSet<Run> verticalRuns = new TreeSet<Run>();

    /**
     * @param graphViews
     */
    public ManhattanRouter(GraphViews graphViews) {
        super(graphViews);
        for (View view : graphViews.getViews()) {
            addView(view);
        }
    }

    /**
     * Mark the grid points covered by view as occupied
     * 
     * @param view
     */
    private void addView(View view) {
        if (view instanceof SplineConnectionView) {
            return;
        }
        if (view instanceof ManhattanConnectionView) {
            ManhattanConnectionView mc = (ManhattanConnectionView) view;
            Route runs = mc.getRoute();
            for (Run run : runs.getRuns()) {
                addRun(run);
            }
            return;
        }
        if (view instanceof NodeView) {
            NodeView box = (NodeView) view;
            Point referencePoint = box.getReferencePoint();
            Rectangle bb = box.getFillShape().getBounds();
            int gridLowX = (bb.x + referencePoint.x) / grid;
            int gridHighX = (bb.x + referencePoint.x + bb.width + grid - 1) / grid;
            int gridLowY = (bb.y + referencePoint.y) / grid;
            int gridHighY = (bb.y + referencePoint.y + bb.height + grid - 1) / grid;
            for (int y = gridLowY; y <= gridHighY; y++) {
                addRun(new Run(y, gridLowX, gridHighX, Direction.RIGHT, view));
            }
            for (int x = gridLowX; x <= gridHighX; x++) {
                addRun(new Run(x, gridLowY, gridHighY, Direction.UP, view));
            }
            return;
        }
        if (view instanceof TerminalView) {
            TerminalView terminalView = (TerminalView) view;
            Object constraint = terminalView.getConstraint();
            Point referencePoint = view.getReferencePoint();
            Point gridPoint = toGrid(referencePoint, constraint);
            int y = gridPoint.y;
            int x = gridPoint.x;
            addRun(new Run(y, x, x, Direction.RIGHT, view));
            addRun(new Run(x, y, y, Direction.UP, view));
        }
    }

    /**
     * @param run
     */
    private synchronized void addRun(Run run) {
        if (isRunHorizontal(run)) {
            horizontalRuns.add(run);
        } else {
            verticalRuns.add(run);
        }
    }

    /**
     * @param run
     */
    private synchronized void removeRun(Run run) {
        if (isRunHorizontal(run)) {
            horizontalRuns.remove(run);
        } else {
            verticalRuns.remove(run);
        }
    }

    /**
     * @param from
     * @param to
     * @param owner
     * @return the route or null if none found
     */
    public synchronized Route findRoute(ConnectionEnd from, ConnectionEnd to, NamedObject owner) {
        Object fromConstraint = from.getConstraint();
        Point fromPoint = from.getReferencePoint();
        Object toConstraint = to.getConstraint();
        Point toPoint = to.getReferencePoint();
        double x1 = fromPoint.getX();
        double y1 = fromPoint.getY();
        double x2 = toPoint.getX();
        double y2 = toPoint.getY();
        List<Run> fromRuns = removeRuns(from);
        List<Run> toRuns = removeRuns(to);
        Route route = findRoute(owner, x1, y1, fromConstraint, x2, y2, toConstraint);
        if (route == null) {
            restoreRuns(fromRuns);
            restoreRuns(toRuns);
        }
        return route;
    }

    /**
     * @param owner
     * @param startx
     * @param starty
     * @param startConstraint
     * @param endx
     * @param endy
     * @param endConstraint
     * @return the list of Runs of the found route or null if no route was found
     */
    public synchronized Route findRoute(NamedObject owner, double startx, double starty, Object startConstraint, double endx, double endy, Object endConstraint) {
        Point p1 = toGrid(startx, starty, startConstraint);
        Point p2 = toGrid(endx, endy, endConstraint);
        boolean startIsHorizontal;
        boolean endIsHorizontal;
        Side startSide = Side.convertConstraint(startConstraint);
        switch(startSide) {
            case LEFT:
            case RIGHT:
                startIsHorizontal = true;
                break;
            default:
                startIsHorizontal = false;
                break;
        }
        Side endSide = Side.convertConstraint(endConstraint);
        switch(endSide) {
            case LEFT:
            case RIGHT:
                endIsHorizontal = true;
                break;
            default:
                endIsHorizontal = false;
                break;
        }
        long timeout = System.currentTimeMillis() + 1000;
        ScoredRuns scoredRuns = new ScoredRuns();
        int maxCorners = 9;
        for (int nCorners = 0; nCorners < maxCorners; nCorners++) {
            findRoute(owner, p1, p2, startIsHorizontal, endIsHorizontal, timeout, scoredRuns, nCorners, true);
            findRoute(owner, p1, p2, startIsHorizontal, endIsHorizontal, timeout, scoredRuns, nCorners, false);
            if (scoredRuns.bestRuns != null && maxCorners > nCorners + 2) {
                maxCorners = nCorners + 2;
            }
        }
        if (scoredRuns.bestRuns != null) {
            Route result = new Route(scoredRuns.bestRuns.size());
            for (Run run : scoredRuns.bestRuns) {
                addRun(run);
                result.add(run);
            }
            return result;
        }
        return null;
    }

    private void findRoute(NamedObject owner, Point p1, Point p2, boolean startIsHorizontal, boolean endIsHorizontal, long timeout, ScoredRuns scoredRuns, int nCorners, boolean firstIsHorizontal) {
        List<Run> runs1 = new ArrayList<Run>(nCorners + 1);
        boolean found1;
        found1 = findRouteTry(p1.x, p1.y, p2.x, p2.y, firstIsHorizontal, nCorners, owner, runs1, timeout);
        if (found1) {
            int score1 = getScore(runs1);
            if (startIsHorizontal == firstIsHorizontal) {
                score1 += 0;
            } else {
                score1 += CORNER_PENALTY;
            }
            boolean lastIsHorizontal = firstIsHorizontal == ((nCorners & 1) == 0);
            if (endIsHorizontal == lastIsHorizontal) {
                score1 += 0;
            } else {
                score1 += CORNER_PENALTY;
            }
            scoredRuns.update(runs1, score1);
        }
    }

    private boolean findRouteTry(int x1, int y1, int x2, int y2, boolean horizontal, int corners, NamedObject owner, List<Run> runs, long timeout) {
        int v1, v2, f1, f2;
        if (horizontal) {
            v1 = x1;
            v2 = x2;
            f1 = y1;
            f2 = y2;
        } else {
            v1 = y1;
            v2 = y2;
            f1 = x1;
            f2 = x2;
        }
        return findRouteInner(v1, f1, v2, f2, horizontal, corners, owner, runs, timeout);
    }

    private boolean findRouteInner(int v1, int f1, int v2, int f2, boolean horizontal, int corners, NamedObject owner, List<Run> runs, long timeout) {
        if (System.currentTimeMillis() > timeout) {
            return false;
        }
        if (corners == 0) {
            return findStraightRoute(v1, v2, f1, f2, horizontal, owner, runs);
        }
        Direction positiveDir, negativeDir;
        if (horizontal) {
            positiveDir = Direction.RIGHT;
            negativeDir = Direction.LEFT;
        } else {
            positiveDir = Direction.UP;
            negativeDir = Direction.DOWN;
        }
        List<Run> bestRuns = null;
        int bestScore = 0;
        int nSteps;
        int v;
        if (corners == 1) {
            nSteps = 0;
            v = v2;
        } else {
            nSteps = Math.abs(v2 - v1) * STEPS_FACTOR + EXTRA_STEPS;
            v = (v1 + v2) / 2;
        }
        for (int i = 0; i <= nSteps; i++, v += (((i & 1) == 1) ? 1 : -1) * i) {
            Direction dir = v > v1 ? positiveDir : negativeDir;
            Run run = new Run(f1, v1, v, dir, owner);
            if (isRunAvailable(run)) {
                List<Run> tail = new ArrayList<Run>(corners);
                tail.add(run);
                addRun(run);
                boolean found = findRouteInner(f1, v, f2, v2, !horizontal, corners - 1, owner, tail, timeout);
                removeRun(run);
                if (found) {
                    int thisScore = getScore(tail);
                    if (bestRuns == null || thisScore < bestScore) {
                        bestRuns = tail;
                        bestScore = thisScore;
                    }
                }
            }
        }
        if (bestRuns != null) {
            runs.addAll(bestRuns);
            return true;
        }
        return false;
    }

    /**
     * @param tail
     * @return
     */
    private int getScore(List<Run> tail) {
        Direction lastV = null;
        Direction lastH = null;
        int totalLength = 0;
        for (Run run : tail) {
            totalLength += run.getEnd() - run.getStart() + 1;
            Direction dir = run.getDirection();
            switch(dir) {
                case RIGHT:
                case LEFT:
                    if (lastH != null && lastH != dir) {
                        totalLength += REVERSAL_PENALTY;
                        lastH = dir;
                    }
                    break;
                case UP:
                case DOWN:
                    if (lastV != null && lastV != dir) {
                        totalLength += REVERSAL_PENALTY;
                    }
                    lastV = dir;
                    break;
            }
        }
        return totalLength;
    }

    private boolean findStraightRoute(int v1, int v2, int f1, int f2, boolean horizontal, NamedObject owner, List<Run> runs) {
        if (f1 != f2) {
            return false;
        }
        Direction positiveDir, negativeDir;
        if (horizontal) {
            positiveDir = Direction.RIGHT;
            negativeDir = Direction.LEFT;
        } else {
            positiveDir = Direction.UP;
            negativeDir = Direction.DOWN;
        }
        Direction dir;
        if (v2 > v1) {
            dir = positiveDir;
        } else {
            dir = negativeDir;
        }
        Run run = new Run(f1, v1, v2, dir, owner);
        if (isRunAvailable(run)) {
            runs.add(run);
            return true;
        }
        return false;
    }

    /**
     * @param p
     * @param constraint
     * @return the grid location closest to p that is farther away from the
     *         terminal as according to the constraint
     */
    public static Point toGrid(Point2D p, Object constraint) {
        return toGrid(p.getX(), p.getY(), constraint);
    }

    /**
     * @param x
     * @param y
     * @param constraint
     * @return the grid location closest to the given coordinates that is
     *         farther away from the terminal according to the constraint.
     */
    public static Point toGrid(double x, double y, Object constraint) {
        Side side = Side.convertConstraint(constraint);
        switch(side) {
            case LEFT:
                return new Point((int) Math.floor(x / grid), (int) Math.round(y / grid));
            case RIGHT:
                return new Point((int) Math.ceil(x / grid), (int) Math.round(y / grid));
            case TOP:
                return new Point((int) Math.round(x / grid), (int) Math.ceil(y / grid));
            case BOTTOM:
                return new Point((int) Math.round(x / grid), (int) Math.floor(y / grid));
            default:
                return new Point((int) Math.round(x / grid), (int) Math.round(y / grid));
        }
    }

    /**
     * @param q
     * @return the next (higher) grid coordinate
     */
    public static int toNextGrid(double q) {
        return ((int) Math.ceil(q / grid)) * grid;
    }

    /**
     * @param q
     * @return the next (higher) grid coordinate
     */
    public static int toNextEvenGrid(double q) {
        int grid2 = grid * 2;
        return ((int) Math.ceil(q / grid2)) * grid2;
    }

    /**
     * @param q
     * @return the previous (lower) grid coordinate
     */
    public static int toPreviousGrid(double q) {
        return ((int) Math.floor(q / grid)) * grid;
    }

    /**
     * @param q
     * @return the nearest grid coordinate
     */
    public static int toNearestGrid(double q) {
        return ((int) Math.round(q / grid)) * grid;
    }

    private boolean isRunAvailable(Run run) {
        SortedSet<Run> runs = isRunHorizontal(run) ? horizontalRuns : verticalRuns;
        SortedSet<Run> head = runs.headSet(run);
        if (!head.isEmpty()) {
            Run last = head.last();
            if (last.getOther() == run.getOther()) {
                if (run.getLow() <= last.getHigh()) {
                    return false;
                }
            }
        }
        SortedSet<Run> tail = runs.tailSet(run);
        if (!tail.isEmpty()) {
            Run first = tail.first();
            if (first.getOther() == run.getOther()) {
                if (run.getHigh() >= first.getLow()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param run
     * @return
     */
    private boolean isRunHorizontal(Run run) {
        Direction dir = run.getDirection();
        return dir == Direction.LEFT || dir == Direction.RIGHT;
    }

    /**
     * @return a Shape showing the covered grid locations
     */
    @Override
    public synchronized Shape getShape() {
        GeneralPath path = new GeneralPath();
        for (Run run : horizontalRuns) {
            float x1 = run.getStart() * grid - grid / 5;
            float x2 = run.getEnd() * grid + grid / 5;
            float y = run.getOther() * grid;
            path.moveTo(x1, y);
            path.lineTo(x2, y);
        }
        for (Run run : verticalRuns) {
            float y1 = run.getStart() * grid - grid / 5;
            float y2 = run.getEnd() * grid + grid / 5;
            float x = run.getOther() * grid;
            path.moveTo(x, y1);
            path.lineTo(x, y2);
        }
        return path;
    }

    /**
     * @param owner
     * @return the removed runs
     */
    private synchronized List<Run> removeRuns(Object owner) {
        List<Run> removedRuns = new ArrayList<Run>();
        removeRuns(horizontalRuns, removedRuns, owner);
        removeRuns(verticalRuns, removedRuns, owner);
        return removedRuns;
    }

    /**
     * @param runs
     * @param runs
     * @param owner
     */
    private void removeRuns(SortedSet<Run> runs, List<Run> removedRuns, Object owner) {
        for (Iterator<Run> iter = runs.iterator(); iter.hasNext(); ) {
            Run run = iter.next();
            Object runOwner = run.getOwner();
            if (runOwner == owner) {
                iter.remove();
                removedRuns.add(run);
            }
        }
    }

    /**
     * @param fromRuns
     */
    private void restoreRuns(List<Run> fromRuns) {
        for (Run run : fromRuns) {
            Direction dir = run.getDirection();
            switch(dir) {
                case UP:
                case DOWN:
                    verticalRuns.add(run);
                    break;
                default:
                    horizontalRuns.add(run);
            }
        }
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.AbstractView#getFillPaint()
     */
    @Override
    protected Paint getFillPaint() {
        return null;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.AbstractView#getDrawPaint()
     */
    @Override
    protected Paint getDrawPaint() {
        return Color.RED;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.AbstractView#getStroke()
     */
    @Override
    protected Stroke getStroke() {
        return stroke;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.View#getDraggable(Point)
     */
    @Override
    public Draggable getDraggable(Point viewPoint) {
        return null;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.views.View#getGraphElement()
     */
    @Override
    public GraphElement getGraphElement() {
        return null;
    }

    /**
     * @see com.bbn.vessel.author.graphEditor.editor.NamedObject#getName()
     */
    @Override
    public String getName() {
        return "Manhattan Router";
    }
}
