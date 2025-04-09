package santa.jpaint.kernel.shapes;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import santa.jpaint.gui.MainFrame;
import santa.jpaint.kernel.Environment;

/**
 * The rectangle class
 * @author Santa
 *
 */
public class Rect extends FloatingShape {

    /**
	 * Create an rectangle with corner points, the line thickness
	 * should also be given
	 * @param p1 A point in the corner
	 * @param p2 A point in the corner
	 * @param thickness Line thickness
	 */
    public Rect(Point p1, Point p2, float thickness) {
        double xMax = Math.max(p1.x, p2.x);
        double xMin = Math.min(p1.x, p2.x);
        double yMax = Math.max(p1.y, p2.y);
        double yMin = Math.min(p1.y, p2.y);
        super.width = xMax - xMin;
        super.height = yMax - yMin;
        super.centerX = (xMax + xMin) / 2;
        super.centerY = (yMax + yMin) / 2;
        super.lineThickness = thickness;
        super.editor = DefaultEditor.instance;
    }

    @Override
    public void paint(Graphics2D g2d, ImageObserver observer) {
        AffineTransform transf = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(theta);
        g2d.scale(zoomX, zoomY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Environment env = MainFrame.getCurrentEnv();
        java.awt.Stroke s = g2d.getStroke();
        if ((MainFrame.getCurrentEnv().getShapeOption() & Environment.SHAPE_INTERIOR) != 0) {
            g2d.setColor(env.getBg());
            g2d.fillRect((int) (-width / 2), (int) (-height / 2), (int) width, (int) height);
        }
        if ((MainFrame.getCurrentEnv().getShapeOption() & Environment.SHAPE_OUTLINE) != 0) {
            g2d.setStroke(new BasicStroke(super.lineThickness));
            g2d.setColor(env.getFg());
            g2d.drawRect((int) (-width / 2), (int) (-height / 2), (int) width, (int) height);
        }
        g2d.setStroke(s);
        if (selected) editor.editShape(g2d, this);
        g2d.setTransform(transf);
    }
}
