package com.dukesoftware.utils.sig.fft.test;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

@SuppressWarnings("serial")
public class GraphPlot extends Canvas {

    public static final int SIGNAL = 1;

    public static final int SPECTRUM = 2;

    Color plotColor = Color.yellow;

    Color axisColor = Color.black;

    Color gridColor = Color.black;

    Color bgColor = Color.blue;

    int plotStyle = SIGNAL;

    boolean tracePlot = true;

    boolean logScale = false;

    int vertSpace = 20;

    int horzSpace = 20;

    int vertIntervals = 8;

    int horzIntervals = 10;

    int nPoints = 0;

    double xmax = 0.0f;

    double ymax = 0.0f;

    double xScale, yScale;

    private double[] plotValues;

    public GraphPlot() {
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

    public void setYmax(double m) {
        ymax = m;
    }

    public double getYmax() {
        return ymax;
    }

    public void setPlotValues(double[] values) {
        nPoints = values.length;
        plotValues = new double[nPoints];
        plotValues = values;
        repaint();
    }

    public void paint(Graphics g) {
        int x, y;
        int top = vertSpace;
        int bottom = getSize().height - vertSpace;
        int left = horzSpace;
        int right = getSize().width - horzSpace;
        int width = right - left;
        int fullHeight = bottom - top;
        int centre = (top + bottom) / 2;
        int xAxisPos = centre;
        int yHeight = fullHeight / 2;
        if (plotStyle == SPECTRUM) {
            xAxisPos = bottom;
            yHeight = fullHeight;
        }
        this.setBackground(bgColor);
        if (logScale) {
            xAxisPos = top;
            g.setColor(gridColor);
            for (int i = 0; i <= vertIntervals; i++) {
                x = left + i * width / vertIntervals;
                g.drawLine(x, top, x, bottom);
            }
            for (int i = 0; i <= horzIntervals; i++) {
                y = top + i * fullHeight / horzIntervals;
                g.drawLine(left, y, right, y);
            }
        }
        g.setColor(axisColor);
        g.drawLine(left, top, left, bottom);
        g.drawLine(left, xAxisPos, right, xAxisPos);
        if (nPoints != 0) {
            g.setColor(plotColor);
            xScale = width / (float) (nPoints - 1);
            yScale = yHeight / ymax;
            int[] xCoords = new int[nPoints];
            int[] yCoords = new int[nPoints];
            for (int i = 0; i < nPoints; i++) {
                xCoords[i] = (int) (left + Math.round(i * xScale));
                yCoords[i] = (int) (xAxisPos - Math.round(plotValues[i] * yScale));
            }
            if (tracePlot) for (int i = 0; i < nPoints - 1; i++) g.drawLine(xCoords[i], yCoords[i], xCoords[i + 1], yCoords[i + 1]); else {
                for (int i = 0; i < nPoints; i++) g.drawLine(xCoords[i], xAxisPos, xCoords[i], yCoords[i]);
            }
        }
    }
}
