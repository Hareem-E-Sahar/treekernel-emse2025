package dsp.grafix;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.JInternalFrame;
import javax.security.auth.Refreshable;
import com.sun.rsasign.ax;

public class GraphPlot extends Canvas implements MouseMotionListener {

    public static final int SIGNAL = 1;

    public static final int SPECTRUM = 2;

    public static final int SAMPLESIGNAL = 3;

    private Image offscreenImage;

    private Graphics offG;

    Color plotColor = Color.BLUE;

    Color axisColor = Color.black;

    Color gridColor = Color.cyan;

    Color bgColor = Color.white;

    int plotStyle = SIGNAL;

    boolean tracePlot = true;

    boolean logScale = false;

    int vertSpace = 40;

    int horzSpace = 40;

    int vertIntervals = 8;

    int horzIntervals = 10;

    int nPoints = 0;

    float xmax = 0.0f;

    float ymax = 0.0f;

    float xScale, yScale;

    private float[] plotValues;

    String titlu, denumireAxaX, denumireAxaY;

    PlotModel model;

    int lengthX;

    int lengthY;

    int mm = 25;

    String mode;

    int curX, curY, width, heigth, x, y, top, bottom, left, right, widthGraph;

    int fullHeight;

    int centru;

    int xAxisPos;

    int valSpectru;

    int sampleFreq;

    public GraphPlot(PlotModel pm, int w, int h, String titlus, String axaX, String axaY, int freq) {
        sampleFreq = freq;
        width = w;
        heigth = h;
        setSize(width, heigth);
        titlu = titlus;
        denumireAxaX = axaX;
        denumireAxaY = axaY;
        model = pm;
        addMouseMotionListener(this);
        offscreenImage = new BufferedImage(width, heigth, 1);
        offG = offscreenImage.getGraphics();
        setBackground(Color.white);
    }

    public PlotModel getModel() {
        return model;
    }

    public void setPlotColor(Color c) {
        if (c != null) plotColor = c;
    }

    public Color getPlotColor() {
        return plotColor;
    }

    public void setAxisColor(Color c) {
        if (c != null) axisColor = c;
    }

    public Color getAxisColor() {
        return axisColor;
    }

    public void setGridColor(Color c) {
        if (c != null) gridColor = c;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setBgColor(Color c) {
        if (c != null) bgColor = c;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setPlotStyle(int pst) {
        plotStyle = pst;
    }

    public int getPlotStyle() {
        return plotStyle;
    }

    public void setTracePlot(boolean b) {
        tracePlot = b;
    }

    public boolean isTracePlot() {
        return tracePlot;
    }

    public void setLogScale(boolean b) {
        logScale = b;
    }

    public boolean isLogScale() {
        return logScale;
    }

    public void setVertSpace(int v) {
        vertSpace = v;
    }

    public int getVertSpace() {
        return vertSpace;
    }

    public void setHorzSpace(int h) {
        horzSpace = h;
    }

    public int getHorzSpace() {
        return horzSpace;
    }

    public int getVertIntervals() {
        return vertIntervals;
    }

    public void setVertIntervals(int i) {
        vertIntervals = i;
    }

    public int getHorzIntervals() {
        return horzIntervals;
    }

    public void setHorzIntervals(int i) {
        horzIntervals = i;
    }

    public void setYmax(float m) {
        ymax = m;
    }

    public float getYmax() {
        return ymax;
    }

    public void setPlotValues(float[] values) {
        nPoints = values.length;
        plotValues = new float[nPoints];
        plotValues = values;
        repaint();
    }

    public void update(Graphics g) {
        offscreenImage = new BufferedImage(width, heigth, 1);
        offG = offscreenImage.getGraphics();
        paint(g);
    }

    public void paint(Graphics g) {
        offG.setColor(Color.white);
        offG.fillRect(0, 0, getWidth(), getHeight());
        drawGrafic(offG);
        drawCursor(offG);
        g.drawImage(offscreenImage, 0, 0, this);
    }

    public void drawCursor(Graphics g) {
        int mX = curX - horzSpace;
        int mY = -curY + centru;
        if (plotStyle == SPECTRUM) mY = bottom - curY;
        g.drawString(mX + "," + mY, curX, curY);
        repaint();
    }

    public void mouseMoved(MouseEvent arg0) {
        curX = arg0.getX();
        curY = arg0.getY();
    }

    public void mouseDragged(MouseEvent arg0) {
        System.out.println(arg0.getX());
    }

    public void drawGrafic(Graphics g) {
        double rezolutieFrecventa, q, domeniuFrecventa;
        int val = 0, pas, contor = 0;
        top = vertSpace;
        bottom = getSize().height - vertSpace;
        left = horzSpace;
        right = getSize().width - horzSpace;
        widthGraph = right - left;
        fullHeight = bottom - top;
        centru = (top + bottom) / 2;
        xAxisPos = centru;
        int yHeight = fullHeight / 2;
        xAxisPos = bottom;
        yHeight = fullHeight;
        bgColor = Color.WHITE;
        g.setColor(axisColor);
        g.drawString(denumireAxaX, right, bottom + 5);
        g.drawString(denumireAxaY, left - 23, top - 5);
        g.drawString(titlu, widthGraph / 2, top - 10);
        g.drawLine(left, top - 10, left, bottom);
        g.drawLine(left, xAxisPos, right, xAxisPos);
        g.setColor(plotColor);
        if (tracePlot) {
            nPoints = model.getCount();
        }
        if (nPoints != 0) {
            xScale = (widthGraph) / (float) (nPoints - 1);
            yScale = yHeight / ymax;
            int[] xCoords = new int[nPoints];
            int[] yCoords = new int[nPoints];
            if (tracePlot) {
                int oldx = left, oldy = getHeight() / 2;
                int newx, newy;
                xAxisPos = top;
                g.setColor(axisColor);
                g.drawString("0", left - 5, centru);
                g.drawString("" + (int) (ymax + 1), left - 5, (int) (centru - ymax));
                g.drawString("-" + (int) (ymax + 1), left - 5, (int) (centru + ymax));
                for (int i = 0; i <= vertIntervals; i++) {
                    x = left + i * width / vertIntervals;
                    g.drawLine(x, top, x, bottom);
                }
                for (int i = 0; i <= horzIntervals; i++) {
                    y = top + i * fullHeight / horzIntervals;
                    g.drawLine(left, y, right, y);
                }
                g.setColor(plotColor);
                for (int i = 0; i < model.getCount() - 1; i++) {
                    synchronized (model) {
                        newx = (model.getX(i, getWidth() - 2 * horzSpace)) % (getWidth());
                        newy = model.getY(i, getHeight());
                    }
                    newx += left;
                    if (newx != left) {
                        if ((oldx - newx) < 10) g.drawLine(oldx, oldy, newx, newy);
                    }
                    if (newy < 1) {
                        g.drawString("ss " + newx, newx, bottom + 10);
                        g.drawLine(newx, top, newx, bottom);
                    }
                    oldx = newx;
                    oldy = newy;
                }
            } else {
                domeniuFrecventa = sampleFreq / 2;
                pas = (int) domeniuFrecventa / 10;
                for (int j = pas; j < domeniuFrecventa; j += pas) {
                    g.setColor(axisColor);
                    rezolutieFrecventa = sampleFreq / 256.0;
                    q = j / rezolutieFrecventa;
                    if (q - (int) q > 0.5) q = (int) q + 1; else q = (int) q;
                    int pozX = (int) (left + q * xScale);
                    g.drawString(" " + j, pozX - 5, bottom + 10);
                    g.drawLine(pozX + 5, top, pozX + 5, bottom);
                }
                for (int i = 0; i <= horzIntervals; i++) {
                    y = top + i * fullHeight / horzIntervals;
                    g.drawLine(left, y, right, y);
                }
                g.drawString(" " + (int) (ymax + 1), left - 22, top + 5);
                g.setColor(plotColor);
                for (int i = 0; i < nPoints; i++) {
                    xCoords[i] = left + Math.round(i * xScale);
                    yCoords[i] = xAxisPos - Math.round(plotValues[i] * yScale);
                    g.drawLine(xCoords[i], xAxisPos, xCoords[i], yCoords[i]);
                }
            }
        }
    }
}
