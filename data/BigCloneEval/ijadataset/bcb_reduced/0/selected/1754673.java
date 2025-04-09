package cube42.napkin.util;

import java.awt.*;

/**
 * This class is used as a repository for storing privitive shapes that need to
 * drawn in a number of diagrams.  Any shape that could be used in a different
 * drawing, game, or diagram should be added to this library.
 *
 * @author Matt Paulin
 * @version 1.0
 * @since JDK 1.2
 */
public class PrimitiveShapesLibrary {

    /**
  * Draws a Arrowhead with the specified dimensions.  The start of the arrow
  * is the tip or pointy part.  The end point marks the entire length.  The
  * width is determined by the length divided by the widthHeightRatio.
  * This arrowhead looks like a triangle.
  *
  * @param  g             the graphics object to draw this in.
  * @param  startX        the xCordinate to start the arrow head at
  * @param  startY        the yCordinate to start the arrow head at
  * @param  endX          the end of the arrow head, the flat end
  * @param  endY          the yCordinate of the arrow head, the flat end
  * @param  widthHeigthRation The length is divided by this to determine the
  *                           width of the triangle
  * @param  fill          if true then this will be solid, if false it will be
  *                       hollow
  * @since JDK 1.2
  */
    public static void drawHeavyArrowhead(Graphics g, int startX, int startY, int endX, int endY, int widthHeightRatio, boolean fill) {
        Polygon poly = PrimitiveShapesLibrary.makeArrowhead(startX, startY, endX, endY, widthHeightRatio);
        if (fill) {
            g.fillPolygon(poly);
        } else {
            g.drawPolygon(poly);
        }
    }

    /**
  * Draws a Arrowhead with the specified dimensions.  The start of the arrow
  * is the tip or pointy part.  The end point marks the entire length.  The
  * width is determined by the length divided by the widthHeightRatio.
  * This arrowhead looks like a two lines in a v formation.
  *
  * @param  g             the graphics object to draw this in.
  * @param  startX        the xCordinate to start the arrow head at
  * @param  startY        the yCordinate to start the arrow head at
  * @param  endX          the end of the arrow head, the flat end
  * @param  endY          the yCordinate of the arrow head, the flat end
  * @param  widthHeigthRation The length is divided by this to determine the
  *                           width of the triangle
  * @param  fill          if true then this will be solid, if false it will be
  *                       hollow
  * @since JDK 1.2
  */
    public static void drawLightArrowhead(Graphics g, int startX, int startY, int endX, int endY, int widthHeightRatio) {
        int yChange = 0;
        int xChange = 0;
        int x_verts[] = new int[3];
        int y_verts[] = new int[3];
        Polygon poly;
        xChange = startX - endX;
        yChange = startY - endY;
        x_verts[0] = startX;
        y_verts[0] = startY;
        x_verts[1] = endX + yChange / widthHeightRatio;
        y_verts[1] = endY - xChange / widthHeightRatio;
        x_verts[2] = endX - yChange / widthHeightRatio;
        y_verts[2] = endY + xChange / widthHeightRatio;
        poly = new Polygon(x_verts, y_verts, 3);
        g.drawLine(x_verts[0], y_verts[0], x_verts[1], y_verts[1]);
        g.drawLine(x_verts[0], y_verts[0], x_verts[2], y_verts[2]);
    }

    /**
  * Creates a diamond with the points on the start cordinates and end cordinates
  * the width is determined by the widthHeightRatio.  If it is to be drawn
  * filled in set fill as true.  False will make it hollow.
  *
  *
  * @param  g             the graphics object to draw this in.
  * @param  startX        the xCordinate to start the diamond
  * @param  startY        the yCordinate to start the diamond
  * @param  endX          the xCordinate to end the diamond
  * @param  endY          the yCordinate to end the diamond
  * @param  widthHeigthRation The length is divided by this to determine the
  *                           width of the diamond
  * @param  fill          if true then this will be solid, if false it will be
  *                       hollow
  * @since JDK 1.2
  */
    public static void drawDiamondHead(Graphics g, int startX, int startY, int endX, int endY, int widthHeightRatio, boolean fill) {
        Polygon poly = PrimitiveShapesLibrary.makeDiamonHead(startX, startY, endX, endY, widthHeightRatio);
        if (fill) {
            g.fillPolygon(poly);
        } else {
            g.drawPolygon(poly);
        }
    }

    /**
  * This command simply creates the polygon to represent a triangle representing
  * a arrowhead.  The start of the arrow the tip or pointy part.  The end point
  * marks the entire length.  The width is determined by the length divided by
  * the widthHeightRatio. This arrowhead looks like a triangle.
  *
  * @param  startX        the xCordinate to start the arrow head at
  * @param  startY        the yCordinate to start the arrow head at
  * @param  endX          the end of the arrow head, the flat end
  * @param  endY          the yCordinate of the arrow head, the flat end
  * @param  widthHeigthRation The length is divided by this to determine the
  *                           width of the triangle
  * @since JDK 1.2
  */
    public static Polygon makeArrowhead(int startX, int startY, int endX, int endY, int widthHeightRatio) {
        int yChange = 0;
        int xChange = 0;
        int x_verts[] = new int[3];
        int y_verts[] = new int[3];
        xChange = startX - endX;
        yChange = startY - endY;
        x_verts[0] = startX;
        y_verts[0] = startY;
        x_verts[1] = endX + yChange / widthHeightRatio;
        y_verts[1] = endY - xChange / widthHeightRatio;
        x_verts[2] = endX - yChange / widthHeightRatio;
        y_verts[2] = endY + xChange / widthHeightRatio;
        return new Polygon(x_verts, y_verts, 3);
    }

    /**
  * Creates a diamond with the points on the start cordinates and end cordinates
  * the width is determined by the widthHeightRatio, It simply returns the
  * polygon.
  *
  * @param  startX        the xCordinate to start the diamond
  * @param  startY        the yCordinate to start the diamond
  * @param  endX          the xCordinate to end the diamond
  * @param  endY          the yCordinate to end the diamond
  * @param  widthHeigthRation The length is divided by this to determine the
  *                           width of the diamond
  * @since JDK 1.2
  */
    public static Polygon makeDiamonHead(int startX, int startY, int endX, int endY, int widthHeightRatio) {
        int yChange = 0;
        int xChange = 0;
        int x_verts[] = new int[4];
        int y_verts[] = new int[4];
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        xChange = startX - endX;
        yChange = startY - endY;
        x_verts[0] = startX;
        y_verts[0] = startY;
        x_verts[1] = midX + yChange / widthHeightRatio;
        y_verts[1] = midY - xChange / widthHeightRatio;
        x_verts[2] = endX;
        y_verts[2] = endY;
        x_verts[3] = midX - yChange / widthHeightRatio;
        y_verts[3] = midY + xChange / widthHeightRatio;
        return new Polygon(x_verts, y_verts, 4);
    }

    /**
  * Used to make a box around a line.  You provide the start and end points of
  * the line.  Then the width of the box.  The box drawn will fit those dimensions
  *
  * @param  g             the graphics object to draw this in.
  * @param  startX        x Cordinate of the beginning of the line
  * @param  startY        y Cordinate of the beginning of the line
  * @param  endX          x Cordinate of the end of the line
  * @param  endY          y Cordinate of the end of the line
  * @param  boxWidth      the width of the box
  * @since JDK 1.2
  */
    public static Polygon makeBox(int startX, int startY, int endX, int endY, int boxWidth) {
        Polygon tempPoly = new Polygon();
        int height = startY - endY;
        int width = startX - endX;
        int distance = ((int) Math.sqrt((((double) height * (double) height) + ((double) width * (double) width))));
        int xChange = (boxWidth * height) / distance;
        int yChange = (boxWidth * width) / distance;
        tempPoly.addPoint(startX - xChange, startY + yChange);
        tempPoly.addPoint(endX - xChange, endY + yChange);
        tempPoly.addPoint(endX + xChange, endY - yChange);
        tempPoly.addPoint(startX + xChange, startY - yChange);
        return tempPoly;
    }

    /**
  * Draws a tick mark at the specified cordinate.  A tick mark is a small vertical
  * line
  *
  * @param  g             the graphics object to draw this in.
  * @param  centerX       the xCordinate at the center of the tick mark
  * @param  centerY       the yCordinate at the center of the tick mark
  * @param  tickLength    the length of the tick mark
  * @since JDK 1.2
  */
    public static void drawReturnIcon(Graphics g, int centerX, int centerY, int tickLength) {
        g.drawLine(centerX, centerY - tickLength / 2, centerX, centerY + tickLength / 2);
    }
}
