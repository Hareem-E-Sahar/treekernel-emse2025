package com.tscribble.bitleech.ui.dialogs.downloaddlg;

import java.util.LinkedList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import com.tscribble.bitleech.core.download.Download;

/**
 * @author triston
 *
 * Created on Oct 1, 2008
 */
public class SpeedComposite extends Composite {

    int yAxisMinVal;

    int yAxisMaxVal;

    int yAxisMidVal;

    int yAxisMaxOffVal = 20;

    int yAxisDelta = yAxisMaxOffVal * 2;

    String yAxMaxValStr;

    String yAxMidValStr;

    String yAxMinValStr;

    private static final int TIMER_INTERVAL = 250;

    private int y = 0;

    int lastX = 0, lastY = 0;

    private int gridMoveX = 1;

    public int plotMoveX = 1;

    private int directionY = 1;

    private Download d;

    int speed;

    private int maxX;

    private int maxY;

    private int minX;

    private int halfX;

    private int halfY;

    int yAxisX = 36;

    private final int yAxisXleftOffset = yAxisX + 1;

    private int rightMostLineX;

    LinkedList<Line> vLines;

    LinkedList<Line> hLines;

    LinkedList<Line> pLines;

    private Path path;

    private boolean plotCreated = false;

    private LinkedList<SPoint> pPoints;

    private int canvasWidth;

    public SpeedComposite(Composite parent, int style) {
        this(parent);
    }

    /**
	 * Create the composite
	 * 
	 * @param parent
	 */
    public SpeedComposite(Composite parent) {
        super(parent, SWT.NO_BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent event) {
                render(event);
            }
        });
        addDisposeListener(new DisposeListener() {

            public void widgetDisposed(final DisposeEvent e) {
            }
        });
        Runnable timer = new Runnable() {

            public void run() {
                if (plotCreated) {
                }
                getDisplay().timerExec(TIMER_INTERVAL, this);
            }
        };
        addControlListener(new ControlAdapter() {

            public void controlResized(final ControlEvent e) {
            }
        });
    }

    private void createPlot() {
        if (plotCreated) return;
        if ((vLines == null)) {
            vLines = new LinkedList<Line>();
            for (int x = yAxisXleftOffset; x < maxX; x += 12) {
                vLines.add(new Line(x, 0, x, maxY));
            }
        }
        if ((hLines == null)) {
            hLines = new LinkedList<Line>();
            for (int y = maxY + 3; y >= 0; y -= 32) {
                hLines.add(new Line(yAxisXleftOffset, y, maxX, y));
            }
        }
        if ((pLines == null)) {
            pLines = new LinkedList<Line>();
        }
        plotCreated = true;
    }

    private void axisLogic() {
        boolean change1 = (yAxisMaxVal - speed) > yAxisMaxOffVal;
        boolean change2 = (speed - yAxisMinVal) < yAxisMaxOffVal;
        boolean change3 = (speed > yAxisMaxVal);
        if (change1 || change2 || change3) {
            yAxisMaxVal = speed + yAxisMaxOffVal;
            int tmpMin = yAxisMaxVal - (yAxisDelta);
            yAxisMinVal = (tmpMin) < 0 ? 0 : tmpMin;
            yAxisMidVal = (yAxisMaxVal + yAxisMinVal) / 2;
        }
    }

    /**
	 * Animates the next frame
	 */
    public void gridlogic() {
        if (vLines != null) {
            int newx1;
            boolean removeFirst = false;
            for (Line l : vLines) {
                newx1 = (int) (l.getX1() - gridMoveX);
                if (newx1 < minX) {
                    newx1 = minX;
                    removeFirst = true;
                }
                l.setX1(newx1);
                l.setX2(newx1);
                l.setY2(maxY);
            }
            if (removeFirst) {
                vLines.removeFirst();
            }
            Line last = vLines.getLast();
            for (int index = vLines.size() - 1; index > 0; index--) {
                Line line = vLines.get(index);
                if (line.x1 > maxX) ;
                vLines.remove(index);
            }
            last = vLines.getLast();
            int lastx1 = (int) last.x1;
            for (int x = lastx1 + 12; x < maxX; x += 12) {
                vLines.add(new Line(x, 0, x, maxY));
            }
        }
    }

    private void plotLogic() {
        if (pLines != null) {
            boolean removeFirst = false;
            for (Line l : pLines) {
                int x1 = (int) (l.x1 - plotMoveX);
                int x2 = l.x2 - plotMoveX;
                if (x1 < minX) {
                    removeFirst = true;
                }
                l.x1PercFrac = (float) (l.x1 / (maxX + minX));
                l.x2PercFrac = (float) l.x2 / (maxX + minX);
                float percentageY = (l.speed - yAxisMinVal) / (float) yAxisDelta;
                float y = (1f - percentageY) * maxY;
                l.x1 = (int) (maxX * l.x1PercFrac);
                l.x2 = (int) (maxX * l.x1PercFrac);
                l.x1 = x1 < minX ? minX : x1;
                l.x2 = x2 < minX ? minX : x2;
            }
            float percentageY = (speed - yAxisMinVal) / (float) yAxisDelta;
            float y = (1f - percentageY) * maxY;
            if (pLines.size() == 0) {
                Line newP = new Line(maxX, maxY, maxX, (int) y);
                pLines.add(newP);
            }
            Line last = pLines.getLast();
            Line nl = new Line(last.x2, last.y2, maxX, (int) y);
            nl.x1PercFrac = (float) (nl.x1 / (maxX + minX));
            nl.x2PercFrac = (float) nl.x2 / (maxX + minX);
            pLines.addLast(nl);
            if (removeFirst) {
                pLines.removeFirst();
            }
        }
    }

    private void plotLogic3() {
        int speed1 = 0;
        speed = 25;
        float y1PercFrac = (speed1 - yAxisMinVal) / (float) yAxisDelta;
        float y1 = (1f - y1PercFrac) * maxY;
        float y2PercFrac = (speed - yAxisMinVal) / (float) yAxisDelta;
        float y2 = (1f - y2PercFrac) * maxY;
        if (pLines.size() == 0) {
            Line newP = new Line(minX, maxY, halfX, halfY);
            pLines.add(newP);
        }
        for (Line l : pLines) {
            l.x1PercFrac = (float) (l.x1 / (float) (maxX + minX));
            l.x2PercFrac = (float) l.x2 / (maxX + minX);
            y1PercFrac = (float) (speed1 - yAxisMinVal) / yAxisDelta;
            y1 = (1f - y1PercFrac) * maxY;
            y2PercFrac = (float) (speed - yAxisMinVal) / yAxisDelta;
            y2 = (1f - y2PercFrac) * maxY;
            l.setY1PercFrac(y1PercFrac);
            l.setY2PercFrac(y2PercFrac);
            System.out.println("x1: " + l.x1);
        }
    }

    private void plotLogic2() {
        if (pPoints != null) {
            boolean removeFirst = false;
            for (SPoint p : pPoints) {
                int x1 = p.x - plotMoveX;
                if (x1 < minX) {
                    removeFirst = true;
                }
                p.xPercFrac = (float) x1 / (float) maxX;
                p.y = (int) ((1f - p.yPercFrac) * maxY);
                p.x = (int) (p.xPercFrac * maxX);
            }
            float yPercFrac = (speed - yAxisMinVal) / (float) yAxisDelta;
            float y = (1f - yPercFrac) * maxY;
            if (pPoints.size() == 0) {
                SPoint newP = new SPoint(maxX, (int) y, yPercFrac);
                pPoints.add(newP);
            }
            SPoint last = pPoints.getLast();
            last = pPoints.getLast();
            if (last.x < maxX) {
                SPoint newP = new SPoint(maxX, (int) y, yPercFrac);
                pPoints.addLast(newP);
            }
            for (int index = pPoints.size() - 1; index > 0; index--) {
                SPoint p = pPoints.get(index);
                if (p.x > maxX) {
                    pPoints.remove(index);
                }
            }
            if (removeFirst) {
                pPoints.removeFirst();
            }
        }
    }

    void render(PaintEvent event) {
        canvasWidth = getSize().x;
        maxX = canvasWidth;
        maxY = getSize().y;
        minX = yAxisX;
        halfX = (maxX / 2) + (yAxisX / 2);
        halfY = (int) maxY / 2;
        createPlot();
        Image image = new Image(getDisplay(), getBounds());
        GC gcOffscreen = new GC(image);
        GC gc = event.gc;
        gcOffscreen.setBackground(gc.getBackground());
        gcOffscreen.fillRectangle(image.getBounds());
        drawAxis(gcOffscreen);
        drawGrid(gcOffscreen);
        drawPlot(gcOffscreen);
        gc.drawImage(image, 0, 0);
        image.dispose();
        gcOffscreen.dispose();
    }

    private void drawAxis(GC gcbuffer) {
        gcbuffer.setForeground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
        gcbuffer.drawLine(yAxisX, 0, yAxisX, maxY);
        gcbuffer.drawLine(yAxisX - 4, 0, yAxisX, 0);
        gcbuffer.drawLine(yAxisX - 4, halfY, yAxisX, halfY);
        gcbuffer.drawLine(yAxisX - 4, maxY - 1, yAxisX, maxY - 1);
        yAxMaxValStr = String.valueOf(yAxisMaxVal);
        yAxMidValStr = String.valueOf(yAxisMidVal);
        yAxMinValStr = String.valueOf(yAxisMinVal);
        gcbuffer.drawString(yAxMaxValStr, 5, 2);
        gcbuffer.drawString(yAxMidValStr, 5, halfY - 7);
        gcbuffer.drawString(yAxMinValStr, 5, maxY - 15);
    }

    private void drawPlot(GC gcbuffer) {
        gcbuffer.setForeground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
        for (Line l : pLines) {
            gcbuffer.drawLine((int) l.x1, l.y1, l.x2, l.y2);
        }
    }

    private void drawPlot2(GC gcbuffer) {
        gcbuffer.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
        for (int i = 0; i < pPoints.size() - 1; i += 2) {
            SPoint p = pPoints.get(i);
            SPoint p2 = pPoints.get(i + 1);
            gcbuffer.drawLine(p.x, p.y, p2.x, p2.y);
        }
    }

    private void drawPlot3(GC gcbuffer) {
        gcbuffer.setForeground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
        for (Line l : pLines) {
            gcbuffer.drawLine(l.x1, l.y1, l.x2, l.y2);
        }
    }

    private void drawGrid(GC gcbuffer) {
        gcbuffer.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        for (Line l : vLines) {
            gcbuffer.drawLine((int) l.getX1(), l.getY1(), l.getX2(), l.getY2());
        }
        for (int y = maxY + 3; y >= 0; y -= 32) {
            gcbuffer.drawLine(yAxisXleftOffset, y, maxX, y);
        }
    }

    @Override
    protected void checkSubclass() {
    }

    public void updateView() {
        redraw();
        if (plotCreated) {
            if (d != null) {
                speed = (int) (d.getSpeed() / 1024);
            }
            gridlogic();
            axisLogic();
            plotLogic();
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setDownload(Download d) {
        this.d = d;
    }

    private class SPoint {

        public float xPercFrac, yPercFrac;

        int x, y, speed;

        /**
		 * @param x
		 * @param y
		 * @param speed
		 */
        public SPoint(int x, int y, float yPercFrac) {
            super();
            this.x = x;
            this.y = y;
            this.yPercFrac = yPercFrac;
        }
    }

    private class Line {

        private int x1;

        private int y1;

        private int x2;

        private int y2;

        private int speed;

        private float x1PercFrac;

        private float x2PercFrac;

        private float y1PercFrac;

        private float y2PercFrac;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public void setX1(int x1) {
            this.x1 = x1;
        }

        public void setY1(int y1) {
            this.y1 = y1;
        }

        public void setX2(int x1) {
            this.x2 = x1;
        }

        public void setY2(int y2) {
            this.y2 = y2;
        }

        public void setX1Perc(float perc) {
            this.x1PercFrac = perc;
        }

        public void setY1PercFrac(float percFrac) {
            y1PercFrac = percFrac;
        }

        public void setX2Perc(float perc) {
            this.x2PercFrac = perc;
        }

        public void setY2PercFrac(float percFrac) {
            y2PercFrac = percFrac;
        }

        public void setDataSpeed(int speed) {
            this.speed = speed;
        }

        public float getX1() {
            return x1;
        }

        public int getY1() {
            return y1;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        public void setCords(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public String toString() {
            return "Line { " + x1 + ", " + y2 + ", " + x2 + ", " + y2 + " }" + " x1PercFrac: " + x1PercFrac + ", x2PercFrac: " + x2PercFrac + ", y1PercFrac: " + y1PercFrac + ", y2PercFrac: " + y2PercFrac;
        }
    }

    /**
	 * @return
	 */
    public int getValAxisMin() {
        return yAxisMinVal;
    }

    /**
	 * @return
	 */
    public int getValAxisMax() {
        return yAxisMaxVal;
    }

    /**
	 * @return
	 */
    public int getSpeed() {
        return speed;
    }

    public void setPlotMovX(int plotMovX) {
        this.plotMoveX = plotMovX;
    }
}
