package santa.jpaint.kernel.shapes;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import santa.jpaint.gui.MainFrame;

/**
 * The pencil and brush stroke
 * @author Santa
 *
 */
public class Stroke extends FloatingShape {

    /**
	 * The list of points
	 */
    ArrayList<Point> pointList;

    /**
	 * Default contstructor
	 */
    public Stroke() {
        super.editor = DefaultEditor.instance;
        super.lineThickness = 1.0f;
        pointList = new ArrayList<Point>();
    }

    /**
	 * Should only be called once, after the shape is done
	 */
    public void findCenterPoint() {
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
    }

    /**
	 * update the width, hegiht and other values
	 */
    public void updateGeometry() {
        if (pointList.size() == 0) return;
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
        super.width = xMax - xMin;
        super.height = yMax - yMin;
    }

    @Override
    public void paint(Graphics2D g2d, ImageObserver observer) {
        updateGeometry();
        AffineTransform transf = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(theta);
        g2d.scale(zoomX, zoomY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(MainFrame.getCurrentEnv().getFg());
        java.awt.Stroke s = g2d.getStroke();
        g2d.setStroke(new BasicStroke(super.lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pointList.size() - 1; i++) {
            Point p1 = pointList.get(i);
            Point p2 = pointList.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        g2d.setStroke(s);
        if (selected) editor.editShape(g2d, this);
        g2d.setTransform(transf);
    }

    /**
	 * Add a new point
	 * @param pt The new point
	 */
    public void addPoint(Point pt) {
        pointList.add(pt);
    }

    /**
	 * Sets the thickness of the stroke
	 * @param thickness
	 */
    public void setThickness(float thickness) {
        super.lineThickness = thickness;
    }
}
