package com.cube42.tools.overview;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * Class used to draw connection
 *
 * @author  Matt Paulin
 * @version $Id: ConnectionGraphic.java,v 1.3 2003/03/12 00:27:52 zer0wing Exp $
 */
public class ConnectionGraphic extends OverviewGraphic {

    /**
     * The length of the arrowhead
     */
    private static final double AHEAD_LENGTH = 12;

    /**
     * The width of half the arrowhead
     */
    private static final double AHEAD_WIDTH = 5;

    /**
     * The graphic that the connection is coming from
     */
    private OverviewGraphic fromGraphic;

    /**
     * The graphic that the connection is going to
     */
    private OverviewGraphic toGraphic;

    /**
     * True if the connection is good
     */
    private boolean good;

    /**
     * Constructs the connection graphic
     *
     * @param   fromGraphic     The graphic that the connection is coming
     *                          from
     * @param   toGraphic       The graphic that the connection is going to
     * @param   good            True if the connection is good
     */
    public ConnectionGraphic(OverviewGraphic fromGraphic, OverviewGraphic toGraphic, boolean good) {
        this.fromGraphic = fromGraphic;
        this.toGraphic = toGraphic;
        this.good = good;
    }

    /**
     * Returns true if the graphic has been selected
     *
     * @param   xPos    The xPosition
     * @param   yPos    The yPosition
     * @return  true    if the graphic has been selected
     */
    public boolean isSelected(int xPos, int yPos) {
        return false;
    }

    /**
     * Forces the OverviewGraphic to calculate its dimensions
     *
     * @param   fm  The font metric to use when calculating
     */
    public void calcDimensions(FontMetrics fm) {
    }

    /**
     * Paints the image of the graphic
     *
     * @param   g   The graphics class used to paint the image
     * @param   focusPoint  The point for the center of the screen
     * @param   zoomMode    The current mode for zooming
     * @param   screenSize  The size of the screen
     */
    public void drawGraphic(Graphics2D g, Point focusPoint, int zoomMode, Dimension screenSize) {
        int xPoint1 = transilatePoint(fromGraphic.getXPos(), zoomMode, (int) focusPoint.getX(), (int) screenSize.getWidth());
        int yPoint1 = transilatePoint(fromGraphic.getYPos(), zoomMode, (int) focusPoint.getY(), (int) screenSize.getHeight());
        int xPoint2 = transilatePoint(toGraphic.getXPos(), zoomMode, (int) focusPoint.getX(), (int) screenSize.getWidth());
        int yPoint2 = transilatePoint(toGraphic.getYPos(), zoomMode, (int) focusPoint.getY(), (int) screenSize.getHeight());
        int fromX = xPoint1 + scale(zoomMode, fromGraphic.getWidth()) / 2;
        int fromY = yPoint1 + scale(zoomMode, fromGraphic.getHeight()) / 2;
        int toX = xPoint2 + scale(zoomMode, toGraphic.getWidth()) / 2;
        int toY = yPoint2 + scale(zoomMode, toGraphic.getHeight()) / 2;
        if (this.good) {
            g.setColor(GOOD_COLOR);
        } else {
            g.setColor(BAD_COLOR);
        }
        g.drawLine(fromX, fromY, toX, toY);
        if (zoomMode == DETAIL_ZOOM) {
            int midX = (fromX + toX) / 2;
            int midY = (fromY + toY) / 2;
            int xDiff = fromX - toX;
            int yDiff = fromY - toY;
            double angle;
            angle = Math.atan((double) (yDiff) / (double) (xDiff));
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            if (xDiff < 0) {
                xPoints[0] = midX + (int) (Math.cos(angle) * AHEAD_LENGTH);
                yPoints[0] = midY + (int) (Math.sin(angle) * AHEAD_LENGTH);
            } else {
                xPoints[0] = midX - (int) (Math.cos(angle) * AHEAD_LENGTH);
                yPoints[0] = midY - (int) (Math.sin(angle) * AHEAD_LENGTH);
            }
            int awidth = (int) (Math.sin(angle) * AHEAD_WIDTH);
            int aheight = (int) (Math.cos(angle) * AHEAD_WIDTH);
            xPoints[1] = midX + awidth;
            yPoints[1] = midY - aheight;
            xPoints[2] = midX - awidth;
            yPoints[2] = midY + aheight;
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }
}
