package com.jrandrews.statediagram.cellview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

public class StartView extends VertexView {

    static CellViewRenderer s_renderer = new BulletRenderer();

    public StartView() {
        super();
    }

    public StartView(Object arg0) {
        super(arg0);
    }

    public CellViewRenderer getRenderer() {
        return s_renderer;
    }

    /**
     * From JGraphpad:JGraphRoundRectView
     */
    public Point2D getPerimeterPoint(EdgeView edge, Point2D source, Point2D p) {
        if (getRenderer() instanceof BulletRenderer) return ((BulletRenderer) getRenderer()).getPerimeterPoint(this, source, p);
        return super.getPerimeterPoint(edge, source, p);
    }

    /**
     * From JGraphpad:JGraphRoundRectView
     */
    public static class BulletRenderer extends VertexRenderer implements CellViewRenderer, Serializable {

        /** diameter of the start "bullet" */
        int dia = 25;

        public void paint(Graphics g) {
            int b = borderWidth;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = bordercolor;
            if (color == null) color = getBackground();
            g.setColor(color);
            g.fillOval(b / 2, b / 2, dia - b / 2, dia - b / 2);
            g.drawOval(0, 0, dia, dia);
            if (selected) {
                g2.setStroke(GraphConstants.SELECTION_STROKE);
                g.setColor(highlightColor);
                g.drawOval(0, 0, dia, dia);
            }
        }

        /** @see VertexRenderer#getPerimeterPoint(VertexView, Point2D, Point2D)  */
        public Point2D getPerimeterPoint(VertexView vertexView, Point2D source, Point2D p) {
            Rectangle2D r = vertexView.getBounds();
            double x = r.getX();
            double y = r.getY();
            if (source != null) {
                x = source.getX();
                y = source.getY();
            }
            double a = (dia + 1) / 2;
            double b = (dia + 1) / 2;
            double x0 = x + a;
            double y0 = y + b;
            double x1 = p.getX();
            double y1 = p.getY();
            double dx = x1 - x0;
            double dy = y1 - y0;
            if (dx == 0) return new Point((int) x0, (int) (y0 + b * dy / Math.abs(dy)));
            double d = dy / dx;
            double h = y0 - d * x0;
            double e = a * a * d * d + b * b;
            double f = -2 * x0 * e;
            double g = a * a * d * d * x0 * x0 + b * b * x0 * x0 - a * a * b * b;
            double det = Math.sqrt(f * f - 4 * e * g);
            double xout1 = (-f + det) / (2 * e);
            double xout2 = (-f - det) / (2 * e);
            double yout1 = d * xout1 + h;
            double yout2 = d * xout2 + h;
            double dist1 = Math.sqrt(Math.pow((xout1 - x1), 2) + Math.pow((yout1 - y1), 2));
            double dist2 = Math.sqrt(Math.pow((xout2 - x1), 2) + Math.pow((yout2 - y1), 2));
            double xout, yout;
            if (dist1 < dist2) {
                xout = xout1;
                yout = yout1;
            } else {
                xout = xout2;
                yout = yout2;
            }
            return new Point2D.Double(xout, yout);
        }
    }
}
