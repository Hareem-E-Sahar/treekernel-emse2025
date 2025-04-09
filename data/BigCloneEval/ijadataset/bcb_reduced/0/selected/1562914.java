package geometry.objects;

import frames.controls.ToolEntityButton;
import geometry.base.SelectableEntity;
import geometry.base.ToolType;
import geometry.base.Vector2D;
import geometry.objects.editors.Resizer;
import geometry.objects.editors.Rotator;
import geometry.objects.editors.Resizer.ResizerTypen;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import property.Property;
import property.Property.PropertyType;
import util.UtilClass;

/**
 * @author Etzlstorfer Andreas
 *
 */
public class Star extends DPolygon {

    static {
        SelectableEntity.registerToolEntity(new Star(50, 100, 5, 20, 30d));
    }

    public static final long serialVersionUID = 1;

    /**
     * Zackenhöhe des Sterns
     */
    private double height;

    /**
     * Alle Mittelpunkte um einen Zacken zu erreichten auf einem
     * Polygon...
     */
    private ArrayList<Point> mps = new ArrayList<Point>();

    /**
     * Konstruktor der Klasse Star
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Seiten
     * @param len Seitenlänge
     * @param height Zackenhöhe
     * @param line Linienfarbe
     * @param fill Hintergrundfarbe
     * @param strength Linienfarbe
     */
    public Star(int x, int y, int n, int len, double height, Color line, Color fill, int strength) {
        super(x, y, n, len, line, fill, strength);
        this.height = height;
        this.startangle = 0d;
        calculate();
    }

    /**
     * Konstruktor der Klasse Star
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Seiten
     * @param len Seitenlänge
     * @param height Zackenhöhe
     * @param line Linienfarbe
     * @param fill Hintergrundfarbe
     */
    public Star(int x, int y, int n, int len, double height, Color line, Color fill) {
        super(x, y, n, len, line, fill);
        this.height = height;
        this.startangle = 0d;
        calculate();
    }

    /**
     * Konstruktor der Klasse Star
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Seiten
     * @param len Seitenlänge
     * @param height Zackenhöhe
     * @param line Linienfarbe
     */
    public Star(int x, int y, int n, int len, double height, Color line) {
        super(x, y, n, len, line);
        this.height = height;
        this.startangle = 0d;
        calculate();
    }

    /**
     * Konstruktor der Klasse Star
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Seiten
     * @param len Seitenlänge
     * @param height Zackenhöhe
     */
    public Star(int x, int y, int n, int len, double height) {
        super(x, y, n, len);
        this.height = height;
        this.startangle = 0d;
        calculate();
    }

    public void drawResizeObjects(Graphics g, Resizer r) {
        g.setColor(Color.BLACK);
        switch(r.getType()) {
            case STAR_IN:
                g.drawString("r: " + length, r.getX() + 15, r.getY() + 15);
                break;
            case STAR_OUT:
                g.drawString("h: " + height, r.getX() + 15, r.getY() + 15);
                break;
            default:
                super.drawResizeObjects(g, r);
        }
    }

    public ArrayList<Resizer> getResizers() {
        calculate();
        int xpts[] = polygon.xpoints;
        int ypts[] = polygon.ypoints;
        ArrayList<Resizer> temp = new ArrayList<Resizer>();
        for (int i = 0; i < n * 2; i++) {
            temp.add(new Resizer(xpts[i], ypts[i], this, (i % 2 == 0 ? ResizerTypen.STAR_IN : ResizerTypen.STAR_OUT)));
        }
        temp.add(new Rotator(x, y, this));
        return temp;
    }

    public Object clone() {
        Star dp = new Star(x, y, n, length, height, line, fill, strength);
        dp.startangle = startangle;
        dp.transparent = transparent;
        return dp;
    }

    protected void calculate() {
        polygon.reset();
        mps = new ArrayList<Point>();
        for (int i = 0; i < n; i++) {
            double angle1 = Math.toRadians(i * 360 / n + startangle);
            double angle2 = Math.toRadians((i + 1) * 360 / n + startangle);
            Vector2D OA = new Vector2D(x + length * Math.cos(angle1), y + length * Math.sin(angle1));
            Vector2D OB = new Vector2D(x + length * Math.cos(angle2), y + length * Math.sin(angle2));
            Vector2D OC = Vector2D.mitte(OA, OB);
            Vector2D AB = Vector2D.strecke(OA, OB);
            Vector2D nAB = AB.normal();
            Vector2D nAB0 = nAB.einheit();
            Vector2D CD = nAB0.verlaengern(height);
            Vector2D OD = Vector2D.add(OC, CD);
            polygon.addPoint(OA.toPoint().x, OA.toPoint().y);
            polygon.addPoint(OD.toPoint().x, OD.toPoint().y);
            mps.add(OD.toPoint());
        }
        minx = maxx = polygon.xpoints[0];
        miny = maxy = polygon.ypoints[0];
        for (int i = 1; i < polygon.npoints; i++) {
            if (minx > polygon.xpoints[i]) minx = polygon.xpoints[i];
            if (maxx < polygon.xpoints[i]) maxx = polygon.xpoints[i];
            if (miny > polygon.ypoints[i]) miny = polygon.ypoints[i];
            if (maxy < polygon.ypoints[i]) maxy = polygon.ypoints[i];
        }
        S.x = (maxx + minx) / 2;
        S.y = (maxy + miny) / 2;
    }

    public void setProperties(ArrayList<Property> properties) {
        height = (Double) properties.remove(5).getRef();
        super.setProperties(properties);
    }

    public ArrayList<Property> getProperties() {
        ArrayList<Property> temp = super.getProperties();
        temp.add(5, new Property("Zackenhöhe", PropertyType.NUMBER_DOUBLE, new Double(height)));
        return temp;
    }

    public boolean isMouseOver(int x, int y) {
        Rect temp = new Rect(minx, miny, maxx - minx, maxy - miny);
        return temp.isMouseOver(x, y);
    }

    public boolean isObjectInArea(int x1, int y1, int x2, int y2) {
        Rect temp = new Rect(minx, miny, maxx - minx, maxy - miny);
        return temp.isObjectInArea(x1, y1, x2, y2);
    }

    public void ResizerAction(Resizer r) {
        int dx = r.getX() - S.x;
        int dy = r.getY() - S.y;
        double diff = Math.sqrt(dx * dx + dy * dy);
        switch(r.getType()) {
            case STAR_OUT:
                this.height = diff - length;
                break;
            case STAR_IN:
                this.length = (int) diff;
                break;
            default:
                super.ResizerAction(r);
        }
    }

    public String toString() {
        return "Stern " + super.toString().substring(8);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.STAR;
    }

    public ToolEntityButton getToolButton() {
        return new ToolEntityButton("Stern", UtilClass.loadImageIconFromJAR("./img.jar", "buttons/stern2.gif"), this);
    }
}
