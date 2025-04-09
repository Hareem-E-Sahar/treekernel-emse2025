package vivace.helper;

import java.awt.*;
import vivace.model.*;
import vivace.view.Keyboard;
import java.util.*;
import java.util.List;

/**
 * Helper for drawing grids in the application
 *
 */
public class GridHelper implements Observer {

    private static GridHelper theInstance = new GridHelper();

    private static ArrayList<Integer> mainBarPositions = new ArrayList<Integer>();

    private static ArrayList<Integer> smallBarPositions = new ArrayList<Integer>();

    private static ArrayList<Integer> notePositions = new ArrayList<Integer>();

    /**
	 * Performs some needed initialization. Always call before you start to use the
	 * grid helper functions.
	 */
    public static void initialize() {
        App.addProjectObserver(theInstance, App.Source.MODEL);
        timeSignatures = null;
        initializeNotePositions();
    }

    /**
	 * Returns the needed dimension for the overview grid
	 */
    public static Dimension getOverviewDimension() {
        return new Dimension(tickToXPosition(Math.max(GUIHelper.MIN_TICKS, App.Project.getSequenceLength())), GUIHelper.TRACK_HEIGHT * App.Project.getTracks().length);
    }

    /**
	 * Returns the needed dimension for the piano roll grid
	 */
    public static Dimension getPianoRollDimension() {
        return new Dimension(tickToXPosition(App.Project.getSequenceLength()), notePositions.get(notePositions.size() - 1) + Keyboard.BIGNOTE_HEIGHT);
    }

    /**
	 * Returns the needed dimension for the time line grid
	 */
    public static Dimension getTimelineDimension() {
        return new Dimension(tickToXPosition(Math.max(GUIHelper.MIN_TICKS, App.Project.getSequenceLength())), GUIHelper.HEADER_HEIGHT);
    }

    /** 
	 * Returns the x-position of a tick
	 */
    public static int tickToXPosition(long tick) {
        double m = App.Project.getPPQ() * 4;
        int n = App.UI.getZoomLevel();
        return (int) Math.ceil(tick / m * n);
    }

    /**
	 * Returns a tick from a x-position. 
	 */
    public static int xPositionToTick(int xPos) {
        int m = App.Project.getPPQ() * 4;
        int n = App.UI.getZoomLevel();
        int tick = (int) Math.ceil(xPos * m / n);
        return tick;
    }

    /**
	 * Returns the y-position of a note
	 */
    public static int noteToYPosition(int note) {
        return notePositions.get(127 - note);
    }

    /**
	 * Returns a note value from an y-position.
	 */
    public static int yPositionToNote(int yPos) {
        return 127 - getClosestIndex(yPos, notePositions);
    }

    /**
	 * Returns the x-position of the closest main bar .
	 */
    public static int getClosestMainBarPosition(int xPosition) {
        return getClosestValue(xPosition, mainBarPositions);
    }

    /**
	 * Returns the x-position of the closest "snappable" position, in other words
	 * the closest vertical grid line.
	 */
    public static int getClosestSnapPosition(int xPosition) {
        return getClosestValue(xPosition, smallBarPositions);
    }

    /**
	 * Returns the y-position of the closest horizontal grid line in the piano roll
	 */
    public static int getClosestNotePosition(int yPosition) {
        return getClosestValue(yPosition, notePositions);
    }

    private static void initializeNotePositions() {
        notePositions.clear();
        notePositions.add(0);
        int currentY = Keyboard.BIGNOTE_HEIGHT;
        for (int i = 1; i < 128; i++) {
            notePositions.add(currentY);
            switch(i % 12) {
                case 2:
                case 3:
                case 7:
                case 8:
                    currentY += Keyboard.BIGNOTE_HEIGHT;
                    break;
                default:
                    currentY += Keyboard.SMALLNOTE_HEIGHT;
                    break;
            }
        }
    }

    /** Paints the grid for the piano roll */
    public static void paintPianoRollGrid(Graphics g) {
        Dimension d = getPianoRollDimension();
        int currentY = Keyboard.BIGNOTE_HEIGHT - Keyboard.SMALLNOTE_HEIGHT;
        g.setColor(Color.decode("#d6dde2"));
        for (int i = 1; i < notePositions.size(); i++) {
            currentY = notePositions.get(i);
            switch(i % 12) {
                case 2:
                case 3:
                case 7:
                case 8:
                    g.drawLine(0, currentY, d.width, currentY);
                    break;
                case 1:
                case 4:
                case 6:
                case 9:
                case 11:
                    g.fillRect(0, currentY, d.width, Keyboard.SMALLNOTE_HEIGHT);
                    break;
            }
        }
        paintBarPositions(g, Math.max(d.height, g.getClipBounds().height), 0);
    }

    /** Paints the grid for the overview */
    public static void paintOverviewGrid(Graphics g) {
        Dimension d = getOverviewDimension();
        if (d.width < 3000) {
            d.width = 3000;
        }
        int currentY = 0;
        g.setColor(Color.decode("#d6dde2"));
        for (int i = 0; i <= App.Project.getTracks().length; i++) {
            g.drawLine(0, currentY, d.width, currentY);
            currentY += GUIHelper.TRACK_HEIGHT;
        }
        paintBarPositions(g, Math.max(d.height, g.getClipBounds().height), 0);
    }

    /** Paints the grid for the histogram */
    public static void paintHistogramGrid(Graphics g, int height) {
        Dimension d = getOverviewDimension();
        paintBarPositions(g, height, 0);
    }

    private static TimeSignatureHelper[] timeSignatures;

    private static TimeSignatureHelper[] getTimeSignatures() {
        if (timeSignatures == null) {
            timeSignatures = App.Project.getTimeSignatures();
        }
        return timeSignatures;
    }

    private static void paintBarPositions(Graphics g, int height, int margin) {
        timeSignatures = getTimeSignatures();
        int ticksPerQuarter = App.Project.getPPQ() * 4;
        int tickCounter = 0, ticksPerCurrentQuarter = 0, timeSignaturesCounter = 0, currentX = 0, currentEvent = 0, nextX = 0;
        double smallWidthReal = (double) App.UI.getZoomLevel() / App.UI.getResolution();
        int smallWidth = (int) Math.round(smallWidthReal);
        int error = (int) (App.UI.getResolution() * smallWidth - App.UI.getResolution() * smallWidthReal);
        int timeToFixError = 0, fix = 0;
        if (error != 0) {
            timeToFixError = (int) Math.abs(Math.ceil(App.UI.getResolution() / error));
            fix = -(error / Math.abs(error));
        }
        mainBarPositions.clear();
        smallBarPositions.clear();
        while (tickCounter <= App.Project.getSequenceLength() || tickCounter <= GUIHelper.MIN_TICKS) {
            currentX = nextX;
            if (currentX > g.getClipBounds().getMaxX()) {
                return;
            }
            if (timeSignatures[currentEvent + 1].getPosition() == tickCounter && timeSignatures[currentEvent + 1].getPosition() != 0) {
                currentEvent++;
                if (timeSignaturesCounter == 0) {
                    ticksPerCurrentQuarter = ticksPerQuarter * timeSignatures[currentEvent - 1].getNumerator() / timeSignatures[currentEvent - 1].getDenominator();
                } else {
                    ticksPerCurrentQuarter = ticksPerQuarter * timeSignatures[currentEvent].getNumerator() / timeSignatures[currentEvent].getDenominator();
                }
            } else {
                ticksPerCurrentQuarter = ticksPerQuarter * timeSignatures[currentEvent].getNumerator() / timeSignatures[currentEvent].getDenominator();
            }
            tickCounter += ticksPerCurrentQuarter;
            timeSignaturesCounter++;
            nextX += App.UI.getZoomLevel() * timeSignatures[currentEvent].getNumerator() / timeSignatures[currentEvent].getDenominator();
            if (currentX < g.getClipBounds().getMinX() - App.UI.getZoomLevel()) {
                continue;
            }
            g.setColor(Color.decode("#d6dde2"));
            g.drawLine(currentX + margin, 0, currentX + margin, height);
            mainBarPositions.add(currentX);
            smallBarPositions.add(currentX);
            int smallCounter = 1;
            g.setColor(Color.decode("#cacfd3"));
            if (App.UI.getResolution() != 1) {
                while ((currentX += timeToFixError != 0 && smallCounter % timeToFixError == 0 ? smallWidth + fix : smallWidth) < nextX) {
                    smallCounter++;
                    g.drawLine(currentX + margin, 0, currentX + margin, height);
                    smallBarPositions.add(currentX);
                }
            }
        }
    }

    private static int getClosestValue(int k, List<Integer> ns) {
        return ns.get(getIndexOfClosestValue(k, ns, 0, ns.size() - 1));
    }

    private static int getClosestIndex(int k, List<Integer> ns) {
        return getIndexOfClosestValue(k, ns, 0, ns.size() - 1);
    }

    private static int getIndexOfClosestValue(int k, List<Integer> ns, int first, int last) {
        if (first == last) {
            return first;
        } else {
            int split = first + (last - first) / 2;
            if (Math.abs(k - ns.get(split)) < Math.abs(k - ns.get(split + 1))) {
                return getIndexOfClosestValue(k, ns, first, split);
            } else {
                return getIndexOfClosestValue(k, ns, split + 1, last);
            }
        }
    }

    public void update(Observable o, Object arg) {
        Action action = (Action) arg;
        switch(action) {
            case TIMESIGNATURE_ADDED:
            case TIMESIGNATRUE_EDIT:
            case TIMESIGNATURE_REMOVED:
                timeSignatures = App.Project.getTimeSignatures();
                break;
        }
    }
}
