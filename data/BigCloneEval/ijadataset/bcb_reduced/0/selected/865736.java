package com.memoire.dja;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

public class DjaBrokenArrow extends DjaLink {

    public DjaBrokenArrow(String _text) {
        super();
        if (_text != null) addText(_text);
    }

    public DjaBrokenArrow() {
        this(null);
    }

    public Rectangle getExtendedBounds() {
        int[][] t = compute();
        int x0, y0, x1, y1, x2, y2, x3, y3;
        x0 = t[0][0];
        x1 = t[0][1];
        x2 = t[0][2];
        x3 = t[0][3];
        y0 = t[1][0];
        y1 = t[1][1];
        y2 = t[1][2];
        y3 = t[1][3];
        int xmin = Math.min(x0, Math.min(x1, Math.min(x2, x3)));
        int xmax = Math.max(x0, Math.max(x1, Math.max(x2, x3)));
        int ymin = Math.min(y0, Math.min(y1, Math.min(y2, y3)));
        int ymax = Math.max(y0, Math.max(y1, Math.max(y2, y3)));
        return getExtendedBounds(new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin));
    }

    public boolean contains(int _x, int _y) {
        int[][] t = compute();
        int x0, y0, x1, y1, x2, y2, x3, y3;
        x0 = t[0][0];
        x1 = t[0][1];
        x2 = t[0][2];
        x3 = t[0][3];
        y0 = t[1][0];
        y1 = t[1][1];
        y2 = t[1][2];
        y3 = t[1][3];
        return (rectangle(x0, y0, x2, y0).contains(_x, _y)) || (rectangle(x2, y0, x2, y2).contains(_x, _y)) || (rectangle(x2, y2, x3, y3).contains(_x, _y)) || (rectangle(x3, y3, x1, y1).contains(_x, _y));
    }

    public DjaAnchor[] getAnchors() {
        int[][] t = compute();
        int x2, y2, x3, y3;
        x2 = t[0][2];
        x3 = t[0][3];
        y2 = t[1][2];
        y3 = t[1][3];
        DjaAnchor[] r = new DjaAnchor[1];
        r[0] = new DjaAnchor(this, 0, ANY, (x2 + x3) / 2, (y2 + y3) / 2);
        return r;
    }

    private int[][] compute() {
        int x0, y0, x1, y1, x2, y2, x3, y3;
        int o0, o1;
        updateXYO();
        x0 = xbegin_;
        y0 = ybegin_;
        o0 = obegin_;
        x1 = xend_;
        y1 = yend_;
        o1 = oend_;
        x2 = x3 = (x0 + x1) / 2;
        y2 = y3 = (y0 + y1) / 2;
        if ((o0 == NORTH) || (o0 == SOUTH)) x2 = x0;
        if ((o0 == EAST) || (o0 == WEST)) y2 = y0;
        if ((o1 == NORTH) || (o1 == SOUTH)) x3 = x1;
        if ((o1 == EAST) || (o1 == WEST)) y3 = y1;
        if ((o0 == NORTH) && (o1 == NORTH)) y3 = y2 = Math.min(y0 - 2 * deltaY, y1 - 2 * deltaY);
        if ((o0 == SOUTH) && (o1 == SOUTH)) y3 = y2 = Math.max(y0 + 2 * deltaY, y1 + 2 * deltaY);
        if ((o0 == EAST) && (o1 == EAST)) x3 = x2 = Math.max(x0 + 2 * deltaX, x1 + 2 * deltaX);
        if ((o0 == WEST) && (o1 == WEST)) x3 = x2 = Math.min(x0 - 2 * deltaX, x1 - 2 * deltaX);
        if ((o0 == NORTH) && (o1 == WEST)) {
            x3 = x2;
            y2 = y3;
        }
        if ((o0 == NORTH) && (o1 == EAST)) {
            x3 = x2;
            y2 = y3;
        }
        if ((o0 == SOUTH) && (o1 == WEST)) {
            x3 = x2;
            y2 = y3;
        }
        if ((o0 == SOUTH) && (o1 == EAST)) {
            x3 = x2;
            y2 = y3;
        }
        if ((o0 == EAST) && (o1 == NORTH)) {
            x2 = x3;
            y3 = y2;
        }
        if ((o0 == WEST) && (o1 == NORTH)) {
            x2 = x3;
            y3 = y2;
        }
        if ((o0 == EAST) && (o1 == SOUTH)) {
            x2 = x3;
            y3 = y2;
        }
        if ((o0 == WEST) && (o1 == SOUTH)) {
            x2 = x3;
            y3 = y2;
        }
        int rounded = 0;
        try {
            rounded = Integer.parseInt(getProperty("arrondi"));
        } catch (Exception ex) {
        }
        if ((rounded > 0) && (x2 == x3) && (y2 == y3)) {
            if (x0 == x2) {
                x3 += (x1 > x0 ? rounded : -rounded);
                y2 += (y0 > y1 ? rounded : -rounded);
            } else {
                y3 += (y1 > y0 ? rounded : -rounded);
                x2 += (x0 > x1 ? rounded : -rounded);
            }
        }
        return new int[][] { { x0, x1, x2, x3, o0 }, { y0, y1, y2, y3, o1 } };
    }

    public void paintObject(Graphics _g) {
        int[][] t = compute();
        int x0, y0, x1, y1, x2, y2, x3, y3, o0, o1;
        x0 = t[0][0];
        x1 = t[0][1];
        x2 = t[0][2];
        x3 = t[0][3];
        y0 = t[1][0];
        y1 = t[1][1];
        y2 = t[1][2];
        y3 = t[1][3];
        o0 = t[0][4];
        o1 = t[1][4];
        _g.setColor(getForeground());
        DjaGraphics.BresenhamParams bp = DjaGraphics.getBresenhamParams(this);
        Polygon p = new Polygon();
        p.addPoint(x0, y0);
        p.addPoint(x2, y0);
        p.addPoint(x2, y2);
        p.addPoint(x3, y3);
        p.addPoint(x3, y1);
        p.addPoint(x1, y1);
        DjaGraphics.drawPolyline(_g, p, bp);
        switch(o0) {
            case EAST:
                drawBracket(_g, tbegin_, o0, x0, y0 - 5, x0 + 10, y0 + 5);
                break;
            case WEST:
                drawBracket(_g, tbegin_, o0, x0, y0 - 5, x0 - 10, y0 + 5);
                break;
            case NORTH:
                drawBracket(_g, tbegin_, o0, x0 - 5, y0, x0 + 5, y0 - 10);
                break;
            case SOUTH:
                drawBracket(_g, tbegin_, o0, x0 - 5, y0, x0 + 5, y0 + 10);
                break;
        }
        switch(o1) {
            case EAST:
                drawBracket(_g, tend_, o1, x1, y1 - 5, x1 + 10, y1 + 5);
                break;
            case WEST:
                drawBracket(_g, tend_, o1, x1, y1 - 5, x1 - 10, y1 + 5);
                break;
            case NORTH:
                drawBracket(_g, tend_, o1, x1 - 5, y1, x1 + 5, y1 - 10);
                break;
            case SOUTH:
                drawBracket(_g, tend_, o1, x1 - 5, y1, x1 + 5, y1 + 10);
                break;
        }
        super.paintObject(_g);
    }
}
