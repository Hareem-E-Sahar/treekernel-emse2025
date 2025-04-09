package metier.outils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import metier.modele.Figure;

/**
 * Figure => Ellipse
 * @author Quentin, Vincent, Charlie
 *
 */
public class Tools_Ellipse extends Figure {

    private static final long serialVersionUID = 6807036935804479180L;

    /**
	 * Méthode pour tracer l'ellipse
	 */
    public void tracer(Graphics g, boolean couleursVraies, boolean edit, Color color) {
        Color cbg, cfg;
        if (couleursVraies == true) {
            cbg = getBg();
            cfg = getFg();
        } else {
            cbg = color;
            cfg = color;
        }
        if (getBg() != null) {
            g.setColor(cbg);
            g.fillOval(getX1(), getY1(), Math.abs(getX2() - getX1()), Math.abs(getY2() - getY1()));
        }
        g.setColor(cfg);
        g.drawOval(getX1(), getY1(), Math.abs(getX2() - getX1()), Math.abs(getY2() - getY1()));
        if (edit == true) addEditRect(g);
        if (getStatus() == Figure.SUPPRESSION) addDeleteRect((Graphics2D) g);
    }

    /**
	 * Ajout du rectangle de suppression
	 * @param g
	 */
    public void addDeleteRect(Graphics2D g) {
        int offset = Tools.EDIT_RECT_SIZE;
        float epaisseur = 1;
        float[] style = { 10, 5 };
        g.setStroke(new BasicStroke(epaisseur, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, style, 0));
        g.drawRect(getX1() - offset, getY1() - offset, Math.abs(getX2() - getX1()) + 2 * offset, Math.abs(getY2() - getY1()) + 2 * offset);
        g.setStroke(new BasicStroke(1.0f));
    }

    /**
	 * Ajout des petits rectangle de sélection pour le redimensionnement
	 * @param g
	 */
    public void addEditRect(Graphics g) {
        int offset = Tools.EDIT_RECT_SIZE;
        g.fillRect(getX1() - offset / 2, getY1() - offset / 2, offset, offset);
        g.fillRect(getX2() - offset / 2, getY2() - offset / 2, offset, offset);
    }

    public void setDonnees(int x1, int y1, int x2, int y2) {
        if (x1 < x2) {
            setX1(x1);
            setX2(x2);
        } else {
            setX1(x2);
            setX2(x1);
        }
        if (y1 < y2) {
            setY1(y1);
            setY2(y2);
        } else {
            setY1(y2);
            setY2(y1);
        }
    }

    /**
	 * Est-ce que le point 'p' est dans la figure ?
	 */
    public boolean pointInFigure(Point p) {
        int x = p.x;
        int y = p.y;
        int x1 = getX1(), x2 = getX2();
        int y1 = getY1(), y2 = getY2();
        int h = (x1 + x2) / 2, k = (y1 + y2) / 2;
        int a = Math.abs(x2 - x1) / 2, b = Math.abs(y2 - y1) / 2;
        double m1 = Math.pow((x - h) / a, 2), m2 = Math.pow((y - k) / b, 2);
        double equ = m1 + m2;
        if (equ < 1) return true; else return false;
    }

    /**
	 * Distance d'un point (x0, y0) à une Ellipse (Largeur a, Hauteur b)
	 * sqrt((x0-x)^2 + (y0-y)^2)
	 * Avec x = a*b*x0/sqrt((b*x0)^2 + (a*y0)^2)
	 * et y = a*b*y0/sqrt((b*x0)^2 + (a*y0)^2)
	 */
    public boolean pointOnFigure(Point p) {
        int px = p.x, py = p.y;
        int x2 = Math.abs(getX2() - getX1());
        int y2 = Math.abs(getY2() - getY1());
        double a = x2 / 2, b = y2 / 2;
        double cx = getX1() + x2 / 2, cy = getY1() + y2 / 2;
        px = (int) (px - cx);
        py = (int) (py - cy);
        double c11 = Math.pow(b * px, 2), c12 = Math.pow(a * py, 2);
        double denxy = Math.pow(c11 + c12, 0.5);
        double nomx = a * b * px, nomy = a * b * py;
        double x = nomx / denxy, y = nomy / denxy;
        double c21 = Math.pow(px - x, 2), c22 = Math.pow(py - y, 2);
        double dist = Math.round(Math.pow(c21 + c22, 0.5));
        if (dist < Tools.MIN_DISTANCE) return true;
        return false;
    }

    /**
	 * Est-ce que le point 'p' est dans les petits
	 * rectangle d'edit ?
	 */
    public int pointInEditFigure(Point p) {
        int x = p.x;
        int y = p.y;
        int offset = Tools.EDIT_RECT_SIZE / 2;
        if ((x >= getX1() - offset && x <= getX1() + offset) && (y >= getY1() - offset && y <= getY1() + offset)) return 2;
        if ((x >= getX2() - offset && x <= getX2() + offset) && (y >= getY2() - offset && y <= getY2() + offset)) return 1; else return 0;
    }
}
