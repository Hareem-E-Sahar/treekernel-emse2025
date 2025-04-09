package santa.jpaint.kernel.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import santa.jpaint.gui.MainFrame;
import santa.jpaint.kernel.Environment;

/**
 * The polygon class
 * @author Santa
 *
 */
public class Polygon extends FloatingShape {

    /**
	 * List of points
	 */
    ArrayList<Point> pointList = new ArrayList<Point>();

    /**
	 * The floating point, which is designed for easier drawing
	 */
    Point floatingPoint;

    /**
	 * Default constructor
	 */
    public Polygon() {
        super.editor = DefaultEditor.instance;
        super.lineThickness = MainFrame.getCurrentEnv().getThickness();
    }

    /**
	 * Sets the floating point
	 * @param p The new floating point
	 */
    public void setFloatingPoint(Point p) {
        floatingPoint = p;
    }

    /**
	 * Adds the floating point into the point list
	 * @param finished Whether the shape has bee finished
	 */
    public void acceptFloatingPoint(boolean finished) {
        pointList.add(floatingPoint);
        if (finished == true) {
            floatingPoint = null;
            double xMax = pointList.get(0).x;
            double yMax = pointList.get(0).y;
            double xMin = xMax;
            double yMin = yMax;
            for (int i = 1; i < pointList.size(); i++) {
                Point p = pointList.get(i);
                xMax = Math.max(p.x, xMax);
                yMax = Math.max(p.y, yMax);
                xMin = Math.min(p.x, xMin);
                yMin = Math.min(p.y, yMin);
            }
            super.centerX = (xMin + xMax) / 2;
            super.centerY = (yMin + yMax) / 2;
            for (Point p : pointList) {
                p.x -= centerX;
                p.y -= centerY;
            }
            super.width = xMax - xMin;
            super.height = yMax - yMin;
        }
    }

    @Override
    public void paint(Graphics2D g2d, ImageObserver observer) {
        Environment env = MainFrame.getCurrentEnv();
        AffineTransform transf = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(theta);
        g2d.scale(zoomX, zoomY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Stroke s = g2d.getStroke();
        g2d.setStroke(new BasicStroke(super.lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (floatingPoint == null) {
            int np = pointList.size();
            int[] xArray = new int[np];
            int[] yArray = new int[np];
            for (int i = 0; i < np; i++) {
                xArray[i] = pointList.get(i).x;
                yArray[i] = pointList.get(i).y;
            }
            if ((MainFrame.getCurrentEnv().getShapeOption() & Environment.SHAPE_INTERIOR) != 0) {
                g2d.setColor(env.getBg());
                g2d.fillPolygon(xArray, yArray, np);
            }
            if ((MainFrame.getCurrentEnv().getShapeOption() & Environment.SHAPE_OUTLINE) != 0) {
                g2d.setColor(env.getFg());
                g2d.drawPolygon(xArray, yArray, np);
            }
        } else if (pointList.size() > 0) {
            int[] xArray = new int[pointList.size() + 1];
            int[] yArray = new int[pointList.size() + 1];
            for (int i = 0; i < pointList.size(); i++) {
                xArray[i] = pointList.get(i).x;
                yArray[i] = pointList.get(i).y;
            }
            xArray[pointList.size()] = floatingPoint.x;
            yArray[pointList.size()] = floatingPoint.y;
            Color bg = env.getBg();
            Color c = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 100);
            g2d.setColor(c);
            if ((MainFrame.getCurrentEnv().getShapeOption() & Environment.SHAPE_INTERIOR) != 0) {
                g2d.fillPolygon(xArray, yArray, xArray.length);
            }
            if ((MainFrame.getCurrentEnv().getShapeOption() & Environment.SHAPE_OUTLINE) != 0) {
                g2d.setColor(env.getFg());
                for (int i = 0; i < pointList.size(); i++) {
                    g2d.drawLine(xArray[i], yArray[i], xArray[i + 1], yArray[i + 1]);
                }
            } else if (pointList.size() == 1) {
                g2d.drawLine(xArray[0], yArray[0], xArray[1], yArray[1]);
            }
        }
        g2d.setStroke(s);
        if (selected) editor.editShape(g2d, this);
        g2d.setTransform(transf);
    }
}
