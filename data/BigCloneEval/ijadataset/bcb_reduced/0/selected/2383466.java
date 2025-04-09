package org.proteomecommons.MSExpedite.Graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 *
 * @author takis
 */
public abstract class AnnotationGraph extends ObjectDrawerComponent implements Annotator {

    protected AnnotationStateManager annotationStateManager = new AnnotationStateManager(this);

    protected AnnotationState annotationState = new AnnotationState();

    protected boolean observedMassOn = false;

    protected boolean theoreticalMassOn = false;

    protected boolean ppmOn = false;

    protected boolean assignmentOn = true;

    protected boolean cursorObservedMassOn = false;

    protected boolean cursorTheoreticalMassOn = false;

    protected boolean cursorPpmOn = false;

    protected boolean cursorAssignmentOn = true;

    protected boolean cursorMassDiffOn = true;

    protected boolean massDiffOn = true;

    private ColorManager colorManager = new ColorManager();

    private LinkedList<IAnnotationListener> annotationListeners = new LinkedList<IAnnotationListener>();

    private Font annotFont = new Font("Times New Roman", Font.PLAIN, 10);

    /** Creates a new instance of AnnotationGraph */
    public AnnotationGraph() {
        super();
    }

    public AnnotationStateManager getAnnotationStateManager() {
        return annotationStateManager;
    }

    public void set(AnnotationState as) {
        annotationState = as;
    }

    public AnnotationState getAnnotationState() {
        return annotationState;
    }

    public Rectangle getBounds(AnnotationSegment segment) {
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        AnnotationSegment[] seg = getAnnotationSegments();
        for (int i = 0; i < seg.length; i++) {
            if (seg[i].equals(segment)) {
                return calculateSegmentRectangle(g2, seg[i].getSegmentId());
            }
        }
        return new Rectangle(0, 0, 0, 0);
    }

    public Rectangle getBounds(AnnotationSegment segment, AnnotationElement elem) {
        Rectangle r = getBounds(segment);
        Point2f next = segment.getNextLocation(elem);
        Point2f previous = segment.getPreviousLocation(elem);
        float x = (float) elem.getLocation().getX();
        float y = (float) elem.getLocation().getY();
        float nx = -1.0f;
        float ny = -1.0f;
        float fstart = -1, fend = -1;
        float rx = (float) segment.loc.getX();
        if (next != null) {
            nx = (float) next.getX();
            ny = (float) next.getY();
        }
        float px = -1.0f;
        float py = -1.0f;
        if (previous != null) {
            px = (float) previous.getX();
            py = (float) previous.getY();
        }
        if (previous == null && next != null) {
            if (rx < x && x < nx) {
                fstart = rx;
                fend = x;
            } else {
                fstart = x;
                fend = nx;
            }
        } else if (previous != null) {
            if (px < rx && rx < x) {
                fstart = rx;
                fend = x;
            } else {
                fstart = px;
                fend = x;
            }
        }
        if (previous == null && next == null) return new Rectangle(0, 0, 0, 0);
        Range2D r2d = new Range.Float(fstart, fend);
        r2d = this.clipToView(r2d, "X");
        if (r2d.equals(new Range.Float(-1, -1))) {
            return new Rectangle(0, 0, 0, 0);
        }
        int startX = getScreenXPosition((float) r2d.getStart());
        int endX = getScreenXPosition((float) r2d.getEnd());
        return new Rectangle(startX, r.y, endX - startX, r.height);
    }

    public void set(AnnotationSegment[] seg) {
    }

    public void clearAnnotation() {
        annotationState.reset();
        annotationStateManager.clear();
        notifyListenersAllAnnotationsRemoved();
        repaint();
    }

    public void add(IAnnotationListener al) {
        annotationListeners.add(al);
    }

    public void remove(IAnnotationListener al) {
        annotationListeners.remove(al);
    }

    public LinkedList<AnnotationSegment> getAnnotationSegmentsAsList() {
        return annotationStateManager.getMostRecentAnnotationSegAsList();
    }

    public void setAnnotationSegments(LinkedList<AnnotationSegment> segments) {
        for (int i = 0; i < segments.size(); i++) {
            AnnotationSegment seg = segments.get(i);
            AnnotationState state = new AnnotationState();
            state.setActiveSegment(seg);
            annotationStateManager.add(state);
        }
    }

    public void setAnnotationSegment(AnnotationSegment seg) {
        annotationStateManager.clear();
        AnnotationState state = new AnnotationState();
        state.setActiveSegment(seg);
        annotationStateManager.add(state);
    }

    public void notifyListenersActiveSegmentChanged(AnnotationSegment seg) {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).activeAnnotationSegmentChanged(seg);
        }
    }

    public AnnotationSegment[] getAnnotationSegments() {
        return annotationStateManager.getMostRecentActiveSegments();
    }

    public void notifyListenersAllAnnotationsRemoved() {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).allAnnotationsRemoved();
        }
    }

    public void notifyListenersAnnotationElementRemoved(AnnotationSegment seg, AnnotationElement ae) {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).annotationElementRemoved(seg, ae);
        }
    }

    public void notifyListenersAnnotationElementAdded(AnnotationSegment seg, AnnotationElement ae) {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).annotationElementAdded(seg, ae);
        }
    }

    public void notifyListenersAnnotationSegmentRemoved(AnnotationSegment seg) {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).annotationSegmentRemoved(seg);
        }
    }

    public void notifyListenersAnnotatorActivated() {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).annotatorActivated();
        }
    }

    public void notifyListenersAnnotationsLoadedFromFile() {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).annotationsLoadedFromFile();
        }
    }

    public void notifyListenersBeforeAnnotationsLoadedFromFile() {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).beforeAnnotationsLoadedFromFile();
        }
    }

    public void notifyListenersAnnotatorDeactivated() {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).annotatorDeactivated();
        }
    }

    public void notifyListenersAnnotationSegmentCreated(AnnotationSegment seg) {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).newAnnotationSegmentCreated(seg);
        }
    }

    private void notifyActiveAnnotationSegmentChangedListeners(AnnotationSegment seg) {
        for (int i = 0; i < annotationListeners.size(); i++) {
            annotationListeners.get(i).activeAnnotationSegmentChanged(seg);
        }
    }

    public void setAssignmentOn(boolean b) {
        assignmentOn = b;
    }

    public boolean getAssignmentOn() {
        return assignmentOn;
    }

    public void setObservedMassOn(boolean b) {
        observedMassOn = b;
    }

    public boolean getObservedMassOn() {
        return observedMassOn;
    }

    public void setTheoreticalMassOn(boolean b) {
        theoreticalMassOn = b;
    }

    public boolean getTheoreticalMassOn() {
        return theoreticalMassOn;
    }

    public void setPPMOn(boolean b) {
        ppmOn = b;
    }

    public boolean getPPMOn() {
        return ppmOn;
    }

    public void setCursorAssignmentOn(boolean b) {
        cursorAssignmentOn = b;
    }

    public boolean getCursorAssignmentOn() {
        return cursorAssignmentOn;
    }

    public void setCursorObservedMassOn(boolean b) {
        cursorObservedMassOn = b;
    }

    public boolean getCursorObservedMassOn() {
        return cursorObservedMassOn;
    }

    public void setCursorTheoreticalMassOn(boolean b) {
        cursorTheoreticalMassOn = b;
    }

    public boolean getCursorTheoreticalMassOn() {
        return cursorTheoreticalMassOn;
    }

    public void setCursorPPMOn(boolean b) {
        cursorPpmOn = b;
    }

    public boolean getCursorPPMOn() {
        return cursorPpmOn;
    }

    public void setMassDiffOn(boolean b) {
        massDiffOn = b;
    }

    public boolean getMassDiffOn() {
        return massDiffOn;
    }

    public void setCursorMassDiffOn(boolean b) {
        cursorMassDiffOn = b;
    }

    public boolean getCursorMassDiffOn() {
        return cursorMassDiffOn;
    }

    public abstract Point2f getCentroid(Integer index);

    protected synchronized void paintOverImage(Graphics2D g2) {
        super.paintOverImage(g2);
        Font savedFont = g2.getFont();
        g2.setFont(annotFont);
        paintAnnotation(g2);
        g2.setFont(savedFont);
    }

    private void paintAnnotation(Graphics2D g2) {
        Color savedColor = g2.getColor();
        AnnotationSegment seg[] = getAnnotationSegments();
        int offset = 0;
        for (int i = 0; i < seg.length; i++) {
            g2.setColor(colorManager.getColor(i));
            Rectangle r = calculateSegmentRectangle(g2, seg[i].getSegmentId());
            offset = r.y;
            paintSegmentHeadingLine(g2, seg[i], offset);
            paintSegment(g2, seg[i], offset);
        }
        g2.setColor(savedColor);
    }

    private void paintAnnotationSegments(Graphics2D g2) {
        AnnotationSegment seg[] = getAnnotationSegments();
        int offset = 0;
        Color savedColor = g2.getColor();
        for (int i = 0; i < seg.length; i++) {
            g2.setColor(colorManager.getColor(i));
            Rectangle r = calculateSegmentRectangle(g2, seg[i].getSegmentId());
            offset = r.y;
            paintSegmentHeadingLine(g2, seg[i], offset);
            paintSegment(g2, seg[i], offset);
        }
        g2.setColor(savedColor);
    }

    private void paintSegmentHeadingLine(Graphics2D g2, AnnotationSegment seg, int heightOffset) {
        int x0 = this.xMargin;
        int y0 = heightOffset;
        int x1 = x0 + w;
        int y1 = y0;
        Range r = getPixelRange(seg);
        AbstractShape sh = new LineShape();
        Color c = g2.getColor();
        sh.init(r.start, y1);
        sh.setCurrentPosition(new Point(r.end, y0));
        sh.setDrawingColor(c);
        sh = clip(sh);
        if (sh == null) return;
        sh.draw(g2);
    }

    private void paintSegment(Graphics2D g2, AnnotationSegment seg, int heightOffset) {
        Point2f refPeak = seg.getLocation();
        float x = (float) refPeak.getX();
        float y = (float) refPeak.getY();
        int size = seg.size();
        AnnotationElement annotElement = null;
        int x0 = this.xMargin;
        int y0 = heightOffset;
        int x1 = x0 + w;
        final int y1 = y0;
        Rectangle r = calculateSegmentRectangle(g2, seg);
        int h = r.y;
        AbstractShape refPeakLine = new DottedLineShape();
        Color savedColor = g2.getColor();
        refPeakLine.setDrawingColor(g2.getColor());
        x0 = getScreenXPosition(x);
        y0 = getScreenYPosition(y);
        refPeakLine.init(x0, y0);
        refPeakLine.setCurrentPosition(new Point(x0, y1));
        if (inView(x0)) {
            refPeakLine.draw((Graphics) g2);
            g2.fillOval(x0 - 1, y0 - 1, 2, 2);
        }
        if (size == 0) {
            g2.setColor(savedColor);
            return;
        }
        LinkedList sAnnot = new LinkedList();
        for (int i = 0; i < size; i++) {
            annotElement = (AnnotationElement) seg.getElement(i);
            sAnnot.add(getElementAnnotation(annotElement));
            Point2f loc = annotElement.getLocation();
            x = (float) loc.getX();
            y = (float) loc.getY();
            int ix = getScreenXPosition(x);
            int iy = getScreenYPosition(y);
            AbstractShape line = new DottedLineShape();
            line.init(ix, y1);
            line.setCurrentPosition(new Point(ix, iy));
            line.setDrawingColor(g2.getColor());
            if (inView(ix)) {
                line.draw((Graphics) g2);
                g2.fillOval(ix - 1, iy - 1, 2, 2);
            }
            x0 = ix;
        }
        AnnotationElement obj[] = seg.toArray();
        if (obj == null) {
            g2.setColor(savedColor);
            return;
        }
        x0 = getScreenXPosition((float) obj[0].getX());
        y0 = getScreenYPosition((float) obj[0].getY());
        for (int i = 1; i < obj.length; i++) {
            AnnotationElement currentObj = obj[i];
            int iCurrentX = getScreenXPosition((float) currentObj.getX());
            int loc = (iCurrentX + x0) / 2;
            if (inView(loc)) {
                paintSegmentText(g2, (String) sAnnot.get(i - 1), loc, y1);
            }
            x0 = iCurrentX;
        }
        g2.setColor(savedColor);
    }

    private String getElementAnnotation(AnnotationElement elem) {
        String name = "";
        float m = Float.MIN_VALUE;
        int type = elem.getType();
        if (assignmentOn) {
            name = elem.getIonName();
            if (name.trim().length() == 0) {
                name += "Xxx";
            }
        }
        if (observedMassOn && type != AminoAcids.UNDEFINED) {
            m = elem.getObservedMass();
            name += "\n" + state.getMassFormat(m) + " Da[O]";
        }
        if (massDiffOn || type == AminoAcids.UNDEFINED) {
            m = AAUtilities.getMass(elem.getIonName(), type);
            m = Math.abs(m - elem.getObservedMass());
            name += "\ndm=" + state.getMassFormat(m) + " Da";
        }
        if (theoreticalMassOn && type != AminoAcids.UNDEFINED) {
            m = AAUtilities.getMass(elem.getIonName(), type);
            name += "\n" + state.getMassFormat(m) + " Da [T]";
        }
        if (ppmOn && type != AminoAcids.UNDEFINED) {
            float observedMass = elem.getObservedMass();
            float theoreticalMass = AAUtilities.getMass(elem.getIonName(), type);
            float ppm = AAUtilities.getPPM(observedMass, theoreticalMass);
            name += "\n" + (int) (ppm + 0.5) + "  ppm";
        }
        return name;
    }

    private void paintSegmentText(Graphics g, String stext, int x0, int y0) {
        String sarr[] = stext.split("\n");
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < sarr.length; i++) {
            TextShape textObj = new TextShape("Halvetica", Font.PLAIN, 12, TextShape.HORIZONTAL, ' ');
            textObj.setDrawingColor(g.getColor());
            int y = (int) Math.round((g.getFont().getStringBounds(sarr[i], (g2.getFontRenderContext())).getHeight()));
            int x = (int) Math.round((g.getFont().getStringBounds(sarr[i], (g2.getFontRenderContext())).getWidth()));
            textObj.init(Math.abs((int) (x0 - (int) (x / 8 + 0.5))), y0 + (i + 1) * y);
            textObj.setCurrentString(sarr[i]);
            textObj.draw(g);
        }
    }

    public Rectangle calculateSegmentRectangle(AnnotationSegment seg) {
        Graphics2D g2 = getGraphics2D();
        return calculateSegmentRectangle(g2, seg);
    }

    private Rectangle calculateSegmentRectangle(Graphics2D g, AnnotationSegment seg) {
        if (seg == null) return new Rectangle(0, 0, 0, 0);
        AnnotationSegment segs[] = this.getAnnotationSegments();
        for (int i = 0; i < segs.length; i++) {
            if (segs[i].equals(seg)) {
                return calculateSegmentRectangle(g, i);
            }
        }
        return new Rectangle(0, 0, 0, 0);
    }

    private AbstractShape clip(AbstractShape sh) {
        Point p0 = sh.getStartPosition();
        Point p1 = sh.getCurrentPosition();
        if (p0.x > xMargin && p0.x < xMargin + w && p1.x > xMargin && p1.x < xMargin + w) return sh;
        if (p0.x < xMargin && p1.x < w + xMargin && p1.x > xMargin) {
            sh.setStartingPosition(new Point(xMargin, p0.y));
            sh.setCurrentPosition(p1);
        } else if (p0.x > xMargin && p0.x < w + xMargin && p1.x > xMargin + w) {
            sh = new LineShape();
            sh.init(p0.x, p0.y);
            sh.setCurrentPosition(new Point(xMargin + w, p1.y));
        } else if (p0.x < xMargin && p1.x < xMargin) {
            return null;
        } else if (p0.x > xMargin && p1.x < xMargin && p0.x < xMargin + w) {
            sh = new LineShape();
            sh.init(p0.x, p0.y);
            sh.setCurrentPosition(new Point(xMargin, p1.y));
        } else if (p0.x > (xMargin + w) && p1.x > xMargin && p1.x < (xMargin + w)) {
            sh.setStartingPosition(new Point(xMargin + w, p0.y));
            sh.setCurrentPosition(p1);
        } else if (p0.x > xMargin + w && p1.x > xMargin + w) return null;
        if (p0.x < xMargin) {
            p0.x = xMargin;
        }
        if (p1.x > xMargin + w) {
            p1.x = xMargin + w;
        }
        return sh;
    }

    public Rectangle calculateSegmentRectangle(int segIndex) {
        Graphics2D g2 = getGraphics2D();
        return calculateSegmentRectangle(g2, segIndex);
    }

    private Rectangle calculateSegmentRectangle(Graphics2D g, int segIndex) {
        int x0 = this.xMargin;
        int y0 = this.yMargin;
        int x1 = x0 + w;
        int y1 = y0;
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        AnnotationSegment segment[] = getAnnotationSegments();
        for (int i = 0; i < segment.length; i++) {
            Range r = getPixelRange(segment[i]);
            if (i == 0) {
                rect.x = r.start;
                rect.width = r.end;
                rect.height = calculateSegmentHeight(g);
                rect.y = y1;
            } else {
                rect.width = r.end;
                rect.y += calculateSegmentHeight(g);
            }
            if (segment[i].getSegmentId() == segIndex) return rect;
        }
        return rect;
    }

    private int calculateHeightOffset(int segIndex) {
        AnnotationSegment segment[] = getAnnotationSegments();
        AnnotationSegment seg = segment[segIndex];
        if (segment == null || segment.length == 0) return 0;
        int offset = 0;
        AnnotationElement elem;
        AnnotationSegment refSegment = segment[0];
        for (int i = 1; i < segment.length; i++) {
            if (segmentsOverlap(refSegment, segment[i])) {
                offset += 30;
                refSegment = segment[i];
            }
            if (seg.equals(segment[i])) {
                return offset;
            }
        }
        return offset;
    }

    private boolean segmentsOverlap(AnnotationSegment seg1, AnnotationSegment seg2) {
        final Range r0 = new Range(0, 0);
        if (r0.equals(seg1) || r0.equals(seg2)) return false;
        Range r1 = getPixelRange(seg1);
        Range r2 = getPixelRange(seg2);
        return r2.overlap(r1);
    }

    public Range getPixelRange(AnnotationSegment seg) {
        Point2f[] obj = seg.getEndPoints();
        if (obj == null) return new Range(0, 0);
        float dx = (float) obj[0].getX();
        int x0 = getScreenXPosition(dx);
        dx = (float) obj[1].getX();
        int x1 = getScreenXPosition(dx);
        return new Range(x0, x1);
    }

    public int calculateSegmentHeight() {
        Graphics2D g = getGraphics2D();
        return calculateSegmentHeight(g);
    }

    private int calculateSegmentHeight(Graphics2D g) {
        AnnotationSegment seg[] = getAnnotationSegments();
        int height = 0;
        if (seg == null || seg.length == 0) return 0;
        Font oldFont = g.getFont();
        g.setFont(annotFont);
        LinkedList al = new LinkedList();
        al.add("IonName");
        if (observedMassOn) {
            al.add("9.9999 Da");
        }
        if (theoreticalMassOn) {
            al.add("9.9999 Da");
        }
        if (ppmOn) {
            al.add("000 PPM");
        }
        if (assignmentOn) {
            al.add("XX");
        }
        String text[] = (String[]) al.toArray(new String[0]);
        for (int i = 0; i < text.length + 1; i++) {
            height += (int) Math.round((g.getFont().getStringBounds(text[0], (g.getFontRenderContext())).getHeight()));
        }
        g.setFont(oldFont);
        return height;
    }

    class ColorManager {

        public Color color[] = { Color.red.darker(), Color.blue.darker(), Color.green.darker(), Color.magenta.darker() };

        public ColorManager() {
        }

        void add(Color c) {
            Color newColor[] = new Color[color.length + 1];
            System.arraycopy(color, 0, newColor, 0, color.length);
            newColor[color.length] = c;
            color = newColor;
        }

        boolean hasColor(Color c) {
            for (int i = 0; i < color.length; i++) {
                if (color[i].equals(c)) return true;
            }
            return false;
        }

        Color getNextColor() {
            Color testColor = new Color(0);
            Class c = testColor.getClass();
            Field field[] = c.getFields();
            for (int i = 0; i < field.length; i++) {
                if (field[i].getType() == Color.class) {
                    try {
                        Color tmpColor = (Color) field[i].get(testColor);
                        if (!hasColor(tmpColor) && !tmpColor.equals(Color.white)) {
                            add(tmpColor);
                            return tmpColor;
                        }
                    } catch (IllegalAccessException iaEx) {
                        iaEx.printStackTrace();
                    }
                }
            }
            Color tmpColor = Color.black;
            add(tmpColor);
            return tmpColor;
        }

        public Color getColor(int index) {
            if (index >= color.length) {
                Color tmpColor = getNextColor();
                return tmpColor;
            }
            return color[index];
        }
    }
}
