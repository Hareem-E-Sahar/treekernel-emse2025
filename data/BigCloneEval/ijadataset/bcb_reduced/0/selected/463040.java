package org.proteomecommons.MSExpedite.Graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.JRootPane;

/**
 *
 * @author takis
 */
public abstract class AssistedAnnotationController extends AbstractAnnotationController {

    public static final int DEFAULT_SPACING = 60;

    public static final int UNKNOWN_WALKING_DIRECTION = -1;

    private int walkingDirection = UNKNOWN_WALKING_DIRECTION;

    private LinkedList<AnnotationElementShape> rhList = new LinkedList<AnnotationElementShape>();

    private LinkedList<AnnotationElementShape> lhList = new LinkedList<AnnotationElementShape>();

    private Point2f lhRefPeak = new Point2f();

    private Point2f rhRefPeak = new Point2f();

    /** Creates a new instance of AssistedAnnotationController */
    public AssistedAnnotationController(JRootPane rootPane, Annotator annotator, Graph graph, IObjectDrawer renderer) {
        super(rootPane, annotator, graph, renderer);
    }

    public int getMouseButtonUsed() {
        return InputEvent.BUTTON1_MASK;
    }

    public void mousePressed(MouseEvent e) {
        if (mode != WALKING) super.mousePressed(e); else {
            buttonPressed = false;
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (mode != WALKING) super.mouseReleased(e); else {
            buttonPressed = false;
            select(e);
        }
    }

    public void draw(Graphics g) {
        lineupCursors(null);
        super.draw(g);
    }

    public void allAnnotationsRemoved() {
        super.allAnnotationsRemoved();
        rhList.clear();
        lhList.clear();
    }

    public void activeAnnotationSegmentChanged(AnnotationSegment seg) {
        super.activeAnnotationSegmentChanged(seg);
        rhList.clear();
        lhList.clear();
        if (seg.getAnnotationAsList().size() == 0) {
            setWalkingDirection(UNKNOWN_WALKING_DIRECTION);
            reveal(seg.getLocation());
        } else {
            Point2f locFirst = seg.getFirst().getLocation();
            setWalkingDirection(AAUtilities.TRAVERSE_LEFT);
            reveal(locFirst);
            Point2f locLast = seg.getLast().getLocation();
            if (!locLast.equals(locFirst)) {
                setWalkingDirection(AAUtilities.TRAVERSE_LEFT);
                reveal(locLast);
            }
        }
    }

    protected void processMouseMovedEvent(MouseEvent e) {
        scroll(e);
    }

    protected void processSelectedPeak(Point2f lastPeak, Point2f peak, int indexIntoSpectrum, int start, int end) {
        setWalkingDirection(UNKNOWN_WALKING_DIRECTION);
        reveal(peak);
        rhRefPeak = (Point2f) peak.clone();
        lhRefPeak = (Point2f) peak.clone();
    }

    protected void setWalkingDirection(int direction) {
        this.walkingDirection = direction;
    }

    protected void rescindState() {
        super.rescindState();
        AnnotationState as = annotator.getAnnotationState();
        AnnotationElement ae = as.getSelectedElement();
        AnnotationSegment seg = as.getActiveSegment();
        if (seg == null || annotator.getAnnotationStateManager().size() == 0) {
            rhList.clear();
            lhList.clear();
            cursors.clear();
            toggleCursorsOff();
            return;
        }
        Point2f loc = null;
        if (ae != null) loc = (Point2f) ae.getLocation().clone(); else loc = (Point2f) seg.getLocation().clone();
        if (loc.equals(seg.getLocation())) {
            rhRefPeak = loc;
            lhRefPeak = loc;
        } else if (loc.getX() < seg.getLocation().getX()) {
            lhRefPeak = loc;
            walkingDirection = AAUtilities.TRAVERSE_LEFT;
        } else if (loc.getX() > seg.getLocation().getX()) {
            rhRefPeak = loc;
            walkingDirection = AAUtilities.TRAVERSE_RIGHT;
        }
        reveal(loc);
    }

    private void reveal(Point2f peak) {
        toggleCursorsOff();
        if (walkingDirection == UNKNOWN_WALKING_DIRECTION) {
            cursors.clear();
            rhList.clear();
            lhList.clear();
            buildRHAnnotations(peak);
            buildLHAnnotations(peak);
        } else if (walkingDirection == AAUtilities.TRAVERSE_RIGHT) {
            removeCommonEntries(rhList, cursors);
            rhList.clear();
            buildRHAnnotations(peak);
        } else if (walkingDirection == AAUtilities.TRAVERSE_LEFT) {
            removeCommonEntries(lhList, cursors);
            lhList.clear();
            buildLHAnnotations(peak);
        }
        lineupCursors(peak);
        toggleCursorsOn();
        annotator.repaint();
    }

    private void removeCommonEntries(LinkedList refList, LinkedList targList) {
        for (int i = 0; i < refList.size(); i++) {
            targList.remove(refList.get(i));
        }
    }

    private void buildRHAnnotations(Point2f peak) {
        AnnotationElement annot[];
        LinkedList lists[] = new LinkedList[2];
        lists[0] = rhList;
        lists[1] = cursors;
        annot = AAUtilities.getIons(peak, getPeaks(), AAUtilities.TRAVERSE_RIGHT);
        createShapes(lists, peak, annot, true);
        if (annot.length == 0) {
            annot = AAUtilities.getDipeptides(peak, getPeaks(), AAUtilities.TRAVERSE_RIGHT);
            createShapes(lists, peak, annot, true);
        }
        annot = AAUtilities.getPTMs(peak, getPeaks(), AAUtilities.TRAVERSE_RIGHT);
        createShapes(lists, peak, annot, true);
    }

    private void buildLHAnnotations(Point2f peak) {
        AnnotationElement annot[];
        LinkedList lists[] = new LinkedList[2];
        lists[0] = lhList;
        lists[1] = cursors;
        annot = AAUtilities.getIons(peak, getPeaks(), AAUtilities.TRAVERSE_LEFT);
        createShapes(lists, peak, annot, false);
        if (annot.length == 0) {
            annot = AAUtilities.getDipeptides(peak, getPeaks(), AAUtilities.TRAVERSE_LEFT);
            createShapes(lists, peak, annot, false);
        }
        annot = AAUtilities.getPTMs(peak, getPeaks(), AAUtilities.TRAVERSE_LEFT);
        createShapes(lists, peak, annot, false);
    }

    private void select(MouseEvent e) {
        AnnotationElement element = getClosestPossibleAnnotation(e.getX(), e.getY());
        if (element == null) return;
        annotator.getAnnotationState().setSelectedElement(element);
        annotator.getAnnotationState().getActiveSegment().add(element);
        Point2f peak = null;
        try {
            annotator.getAnnotationStateManager().saveAnnotationState(annotator);
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        AnnotationSegment seg = getActiveSegment();
        if (rightHandedPick(seg.getLocation(), element)) {
            walkingDirection = AAUtilities.TRAVERSE_RIGHT;
            rhRefPeak = (Point2f) element.getLocation().clone();
            peak = rhRefPeak;
        } else {
            walkingDirection = AAUtilities.TRAVERSE_LEFT;
            lhRefPeak = (Point2f) element.getLocation().clone();
            peak = lhRefPeak;
        }
        reveal(peak);
    }

    private AnnotationElement getClosestPossibleAnnotation(int x, int y) {
        AnnotationSegment seg = getActiveSegment();
        int size = Integer.MIN_VALUE;
        LinkedList<AnnotationElementShape> list = null;
        if (isRightHandedPick(x)) {
            list = rhList;
        } else {
            list = lhList;
        }
        size = list.size();
        if (size == 0) return null;
        float dist[] = new float[size];
        Point p = new Point(x, y);
        Rectangle r = annotator.calculateSegmentRectangle(seg);
        int h = r.y;
        for (int i = 0; i < size; i++) {
            ArrowShape arrow = list.get(i);
            int x0 = arrow.getStartPosition().x;
            int x1 = arrow.getCurrentPosition().x;
            float avg = (x0 + x1) / 2;
            float y0 = arrow.getStartPosition().y;
            dist[i] = (float) p.distance(avg, y0);
        }
        float ref = Float.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (ref > dist[i]) {
                index = i;
                ref = dist[i];
            }
        }
        return list.get(index).getAnnotationElement();
    }

    private void lineupCursors(Point2f peak) {
        AnnotationSegment seg = getActiveSegment();
        if (seg == null) return;
        Point2f segLoc = seg.getLocation();
        Point margins = graph.getMargins();
        Rectangle segRect = annotator.calculateSegmentRectangle(seg);
        Rectangle graphRect = graph.getGraphRectangle();
        int distanceFromXAxis = graphRect.y + graphRect.height - segRect.y;
        float rhRefMass = rhRefPeak.getX();
        float lhRefMass = lhRefPeak.getX();
        if (peak != null && !peak.equals(rhRefPeak) && !peak.equals(lhRefPeak)) {
            rhRefMass = peak.getX();
            lhRefMass = peak.getX();
        }
        int rhX0 = graph.getScreenXPosition(rhRefMass);
        int lhX0 = graph.getScreenXPosition(lhRefMass);
        int y = segRect.y;
        int dyLeft = DEFAULT_SPACING;
        int dyRight = DEFAULT_SPACING;
        if ((lhList.size() * DEFAULT_SPACING) > (distanceFromXAxis - 50)) {
            dyLeft = (int) (0.5f + distanceFromXAxis / lhList.size());
        }
        if ((rhList.size() * DEFAULT_SPACING) > (distanceFromXAxis - 50)) {
            dyRight = (int) (0.5f + distanceFromXAxis / rhList.size());
        }
        int x1 = Integer.MIN_VALUE;
        boolean isRHArrow = false;
        Graphics2D g2d = graph.getGraphics2D();
        int x0 = Integer.MIN_VALUE;
        int iRight = 0;
        int iLeft = 0;
        int increment = 0;
        for (int i = 0; i < cursors.size(); i++) {
            AnnotationElementShape arrow = (AnnotationElementShape) cursors.get(i);
            Point start = arrow.getStartPosition();
            Point end = arrow.getCurrentPosition();
            Point2f elementLoc = arrow.getAnnotationElement().getLocation();
            if (elementLoc.getX() > seg.getX()) {
                isRHArrow = true;
                x0 = rhX0;
                ++iRight;
                increment = iRight * dyRight;
            } else {
                isRHArrow = false;
                x0 = lhX0;
                ++iLeft;
                increment = iLeft * dyLeft;
            }
            y = segRect.y + increment;
            x1 = graph.getScreenXPosition(elementLoc.getX());
            if (isRHArrow) {
                if (x0 > graphRect.x && x0 < (graphRect.x + graphRect.width) && x1 > (graphRect.x + graphRect.width)) {
                    x1 = graphRect.x + graphRect.width;
                } else if (x0 < graphRect.x && x1 > graphRect.x && x1 < graphRect.x + graphRect.width) {
                    x0 = graphRect.x;
                } else if (x0 < graphRect.x && x1 < graphRect.x) {
                    x0 = 0;
                    x1 = 0;
                } else if (x0 > graphRect.x + graphRect.width && x1 > graphRect.x + graphRect.width) {
                    x0 = 0;
                    x1 = 0;
                }
            } else {
                if (x1 < graphRect.x && x0 > graphRect.x && x0 < graphRect.x + graphRect.width) {
                    x1 = graphRect.x;
                } else if (x1 > graphRect.x && x1 < graphRect.x + graphRect.width && x0 > graphRect.x + graphRect.width) {
                    x0 = graphRect.x + graphRect.width;
                } else if (x0 < graphRect.x && x1 < graphRect.x) {
                    x0 = 0;
                    x1 = 0;
                } else if (x0 > graphRect.x + graphRect.width && x1 > graphRect.x + graphRect.width) {
                    x0 = 0;
                    x1 = 0;
                }
            }
            arrow.init(x0, y);
            arrow.setCurrentPosition(x1, y);
            float fx0 = graph.screenToSpectrumX(x0);
            float fx1 = graph.screenToSpectrumX(x1);
            float fy = graph.screenToSpectrumY(y);
            arrow.init(fx0, fy);
            arrow.setCurrentPosition(fx1, fy);
            String text[] = getCursorText(arrow.getAnnotationElement());
            arrow.setText(text);
        }
    }

    private String formatDegenerateResidueNames(String s) {
        String str = AAUtilities.getDegenerateAminoAcidName(s);
        if (!str.equals("")) s += AAUtilities.RESIDUE_SEPARATOR + str;
        return s;
    }

    private void createShapes(LinkedList<AbstractShape> list[], Point2f refPeak, AnnotationElement[] ae, boolean rhArrows) {
        for (int i = 0; i < ae.length; i++) {
            AnnotationElementShape aes = new AnnotationElementShape(refPeak, ae[i]);
            for (int j = 0; j < list.length; j++) {
                list[j].add(aes);
                if (rhArrows) aes.setCurrentPosition(1000, 0); else aes.setCurrentPosition(-1000, 0);
            }
        }
    }

    private boolean rightHandedPick(Point2f refPoint, AnnotationElement element) {
        return refPoint.getX() < element.getX() ? true : false;
    }

    private boolean isRightHandedPick(int x) {
        AnnotationSegment seg = getActiveSegment();
        Point2f segLoc = seg.getLocation();
        int segLocX = graph.getScreenXPosition(segLoc.getX());
        return segLocX < x ? true : false;
    }

    class AnnotationElementShape extends DottedArrowShape {

        AnnotationElement annotationElement = null;

        Point2f refPeak = null;

        int x[] = new int[2];

        int y[] = new int[2];

        BasicStroke tmpStroke = DottedArrowShape.getBasicStroke();

        public AnnotationElementShape(Point2f refPeak, AnnotationElement ae) {
            super(false);
            annotationElement = ae;
            textOn(false);
            this.refPeak = refPeak;
            Point2f elemLoc = annotationElement.getLocation();
            init(refPeak.getX(), refPeak.getY());
            setCurrentPosition(elemLoc.getX(), elemLoc.getY());
        }

        public AnnotationElement getAnnotationElement() {
            return annotationElement;
        }

        public void draw(Graphics g) {
            Point startPoint = this.getStartPosition();
            Point endPoint = getCurrentPosition();
            if (startPoint.x == 0 && endPoint.x == 0) return;
            super.draw(g);
            Graphics2D g2 = (Graphics2D) g;
            Color savedColor = g2.getColor();
            Stroke savedStroke = g2.getStroke();
            g2.setColor(drawingColor);
            g2.setStroke(tmpStroke);
            x[0] = currentPosition.x;
            y[0] = currentPosition.y;
            x[1] = currentPosition.x;
            y[1] = graph.getScreenYPosition(annotationElement.getLocation().getY());
            drawPolygon(g2, x, y);
            if (text != null && text.length != 0) {
                int yOffset = 2;
                int lastY = 0;
                for (int i = 0; i < text.length; i++) {
                    int textHeight = (int) Math.round((g2.getFont().getStringBounds(text[i], (g2.getFontRenderContext())).getHeight()));
                    int textWidth = (int) Math.round((g2.getFont().getStringBounds(text[i], (g2.getFontRenderContext())).getWidth()));
                    int x0 = startPosition.x;
                    int y0 = lastY + startPosition.y - textHeight - yOffset;
                    if (i == 0) {
                        y0 = startPosition.y;
                    } else {
                        y0 = lastY + startPosition.y;
                    }
                    int x1 = currentPosition.x;
                    int loc = (int) (0.5 + (x0 + x1) / 2);
                    loc -= (int) (0.5 + textWidth / 2);
                    g.drawString(text[i], loc, y0);
                    lastY += yOffset + textHeight;
                }
            }
            g2.setStroke(savedStroke);
            g2.setColor(savedColor);
        }
    }

    public abstract Array2D getPeaks();
}
