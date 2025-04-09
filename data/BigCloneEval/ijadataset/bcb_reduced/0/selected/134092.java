package org.knopflerfish.bundle.desktop.swing.fwspin;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

public abstract class SpinItem {

    double x = 0;

    double y = 0;

    int sx;

    int sy;

    double fac = 1.0;

    double angle = 0;

    Color textColor = Color.gray.brighter().brighter().brighter().brighter();

    public void setPos(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setSPos(int sx, int sy, double fac) {
        this.sx = sx;
        this.sy = sy;
        this.fac = fac;
    }

    public void setAngle(double a) {
        this.angle = a;
    }

    ;

    public double getAngle() {
        return angle;
    }

    ;

    public double dist2(int x0, int y0) {
        double dx = sx - x0;
        double dy = sy - y0;
        return dx * dx + dy * dy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSX() {
        return sx;
    }

    public double getSY() {
        return sy;
    }

    public abstract void paint(Graphics g);

    public abstract void paintDependencies(Graphics g);

    public abstract void paintInfo(Graphics g, double x, double y);

    public abstract boolean isActive();

    /**
   * Draw cubic spline using de Casteljau's method.
   */
    public static void drawSpline(Graphics g, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y, double p4_x, double p4_y, int depth) {
        if (depth > 0) {
            double l1_x = (p1_x + p2_x) / 2;
            double l1_y = (p1_y + p2_y) / 2;
            double m_x = (p2_x + p3_x) / 2;
            double m_y = (p2_y + p3_y) / 2;
            double l2_x = (l1_x + m_x) / 2;
            double l2_y = (l1_y + m_y) / 2;
            double r1_x = (p3_x + p4_x) / 2;
            double r1_y = (p3_y + p4_y) / 2;
            double r2_x = (r1_x + m_x) / 2;
            double r2_y = (r1_y + m_y) / 2;
            double m2_x = (l2_x + r2_x) / 2;
            double m2_y = (l2_y + r2_y) / 2;
            drawSpline(g, p1_x, p1_y, l1_x, l1_y, l2_x, l2_y, m2_x, m2_y, depth - 1);
            drawSpline(g, m2_x, m2_y, r2_x, r2_y, r1_x, r1_y, p4_x, p4_y, depth - 1);
        } else {
            g.drawLine((int) p1_x, (int) p1_y, (int) p4_x, (int) p4_y);
        }
    }

    public static final int DIR_FROM = 1;

    public static final int DIR_TO = 2;

    Vector getNext(int dir) {
        return null;
    }

    ;
}
