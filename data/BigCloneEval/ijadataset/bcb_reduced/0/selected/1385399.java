package geometry.objects;

import frames.controls.ToolEntityButton;
import geometry.base.SelectableEntity;
import geometry.base.ToolType;
import geometry.objects.editors.Resizer;
import geometry.objects.editors.Rotator;
import geometry.objects.editors.Resizer.ResizerTypen;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.util.ArrayList;
import property.Property;
import property.Property.PropertyType;
import util.UtilClass;

/**
 * Dieses Objekt ist ein Polygonobjekt und kann
 * mehrere Ecken besitzen. Bsp.: 3Eck, 4Eck, 5Eck 6Eck....
 * 
 * @author Etlstorfer Andreas
 *
 */
public class DPolygon extends SelectableEntity {

    static {
        SelectableEntity.registerToolEntity(new DPolygon(100, 100, 6, 30));
    }

    public static final long serialVersionUID = 0;

    /**
     * Anzahl der Ecken
     */
    protected Integer n;

    /**
     * Startwinkel
     */
    protected Double startangle;

    /**
     * Seitenlänge
     */
    protected Integer length;

    /**
     * Polygonobjekt zum zeichnen
     */
    protected Polygon polygon = new Polygon();

    /**
     * Startpunkt
     */
    protected Point S = new Point();

    /**
     * Minimalster x-Wert
     */
    protected int minx;

    /**
     * Minimalster y-Wert
     */
    protected int miny;

    /**
     * Maximalster x-Wert
     */
    protected int maxx;

    /**
     * Maximalster y-Wert
     */
    protected int maxy;

    /**
     * Konstruktor der Klasse DPolygon 
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Ecken
     * @param len Seitenlänge
     * @param line Linienfarbe
     * @param fill Hintergrundfarbe
     * @param strength Lininenstärke
     */
    public DPolygon(int x, int y, int n, int len, Color line, Color fill, int strength) {
        super(x, y, line, fill, strength);
        this.n = n;
        this.length = len;
        this.startangle = new Double(0);
        calculate();
    }

    /**
     * Konstruktor der Klasse DPolygon 
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Ecken
     * @param len Seitenlänge
     * @param line Linienfarbe
     * @param fill Hintergrundfarbe
     */
    public DPolygon(int x, int y, int n, int len, Color line, Color fill) {
        super(x, y, line, fill);
        this.n = n;
        this.length = len;
        this.startangle = new Double(0);
        calculate();
    }

    /**
     * Konstruktor der Klasse DPolygon 
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Ecken
     * @param len Seitenlänge
     * @param line Linienfarbe
     */
    public DPolygon(int x, int y, int n, int len, Color line) {
        super(x, y, line);
        this.n = n;
        this.length = len;
        this.startangle = new Double(0);
        calculate();
    }

    /**
     * Konstruktor der Klasse DPolygon 
     * 
     * @param x x-Weite
     * @param y y-Weite
     * @param n Anzahl der Ecken
     * @param len Seitenlänge
     */
    public DPolygon(int x, int y, int n, int len) {
        super(x, y);
        this.n = n;
        this.length = len;
        this.startangle = new Double(0);
        calculate();
    }

    protected void calculate() {
        polygon.reset();
        for (int i = 0; i < n; i++) {
            if (n == null) {
                System.out.println("lala1");
            }
            if (startangle == null) {
                System.out.println("lala2");
            }
            double angle = Math.toRadians(i * 360 / n + startangle);
            polygon.addPoint((int) (x + length * Math.cos(angle)), (int) (y + length * Math.sin(angle)));
        }
        minx = maxx = polygon.xpoints[0];
        miny = maxy = polygon.ypoints[0];
        for (int i = 1; i < n; i++) {
            if (minx > polygon.xpoints[i]) minx = polygon.xpoints[i];
            if (maxx < polygon.xpoints[i]) maxx = polygon.xpoints[i];
            if (miny > polygon.ypoints[i]) miny = polygon.ypoints[i];
            if (maxy < polygon.ypoints[i]) maxy = polygon.ypoints[i];
        }
        S.x = (maxx + minx) / 2;
        S.y = (maxy + miny) / 2;
    }

    public Object clone() {
        DPolygon dp = new DPolygon(x, y, n, length, line, fill, strength);
        dp.startangle = startangle;
        dp.transparent = transparent;
        return dp;
    }

    public void drawResizeObjects(Graphics g, Resizer r) {
        super.drawResizeObjects(g, r);
        switch(r.getType()) {
            case OTHER_RES:
                g.drawString("r: " + length, r.getX() + 15, r.getY() + 15);
                break;
            case ROTATOR_TYPE:
                DecimalFormat df = new DecimalFormat("00.00");
                g.drawString("winkel: " + df.format(startangle), r.getX() + 15, r.getY() + 15);
                break;
        }
    }

    public void ResizerAction(Resizer r) {
        if (r.getType() == ResizerTypen.ROTATOR_TYPE) {
            Line temp = new Line(x, y, r.getX(), r.getY());
            double angle = Math.toDegrees(temp.getAngle());
            this.startangle = Math.rint(angle * 100.0) / 100.0;
        } else {
            int dx = r.getX() - S.x;
            int dy = r.getY() - S.y;
            double diff = Math.sqrt(dx * dx + dy * dy);
            this.length = (int) diff;
        }
    }

    public ArrayList<Resizer> getResizers() {
        calculate();
        int xpts[] = polygon.xpoints;
        int ypts[] = polygon.ypoints;
        ArrayList<Resizer> temp = new ArrayList<Resizer>();
        for (int i = 0; i < polygon.npoints; i++) {
            temp.add(new Resizer(xpts[i], ypts[i], this, ResizerTypen.OTHER_RES));
        }
        temp.add(new Rotator(x, y, this));
        return temp;
    }

    public void setProperties(ArrayList<Property> properties) {
        n = (Integer) (properties.remove(2)).getRef();
        startangle = (Double) properties.remove(2).getRef();
        length = (Integer) properties.remove(2).getRef();
        super.setProperties(properties);
        calculate();
    }

    public ArrayList<Property> getProperties() {
        ArrayList<Property> temp = super.getProperties();
        temp.add(2, new Property("Anzahl Ecken", PropertyType.NUMBER_SPINNER, n));
        temp.add(3, new Property("Startwinkel", PropertyType.NUMBER_DOUBLE, startangle));
        temp.add(4, new Property("Radius", PropertyType.NUMBER_DOUBLE, length));
        return temp;
    }

    /**
     * Gibt die Länge zurück
     * 
	 * @return Länge
	 */
    public double getLength() {
        return length;
    }

    /**
     * Gibt den Startwinkel zurück
     * 
     * @return Startwinkel
     */
    public double getStartAngle() {
        return startangle;
    }

    /**
     * Setzt die Länge fest
     * 
	 * @param length Länge
	 */
    public void setLength(int length) {
        this.length = length;
        calculate();
    }

    /**
     * Setzt den Startwinkel fest
     * 
     * @param startangle Startwinkel
     */
    public void setStartangle(double startangle) {
        this.startangle = startangle;
        calculate();
    }

    /**
     * Gibt die Anzahl der Ecken zurück
     * 
	 * @return Anzahl der Ecken
	 */
    public int getN() {
        return n;
    }

    /**
     * Setzt die Anzahl der Ecken
     * 
	 * @param n Anzahl der Ecken
	 */
    public void setN(int n) {
        this.n = n;
        calculate();
    }

    /**
     * Gibt das Polygonobjekt zurück
     * 
     * @return Polygonobjekt
     */
    public Polygon getPolygon() {
        return polygon;
    }

    public boolean isMouseOver(int x, int y) {
        Circle temp = new Circle(S.x, S.y, length.intValue());
        return temp.isMouseOver(x, y);
    }

    public boolean isObjectInArea(int x1, int y1, int x2, int y2) {
        Circle temp = new Circle(S.x, S.y, length.intValue());
        return temp.isObjectInArea(x1, y1, x2, y2);
    }

    public void Draw(Graphics g) {
        super.Draw(g);
        if (transparent) {
            g.setColor(line);
            g.drawPolygon(polygon);
        } else {
            g.setColor(fill);
            g.fillPolygon(polygon);
            g.setColor(line);
            g.drawPolygon(polygon);
        }
    }

    public String toString() {
        return "Polygon (" + n + " Ecken, Radius=" + length + ",Startwinkel=" + startangle + "°) " + super.toString();
    }

    @Override
    public boolean canSplitToLines() {
        return true;
    }

    public ArrayList<SelectableEntity> getLines() {
        ArrayList<SelectableEntity> temp = new ArrayList<SelectableEntity>();
        int i;
        for (i = 0; i < polygon.npoints - 1; i++) {
            Point a = new Point(polygon.xpoints[i], polygon.ypoints[i]);
            Point b = new Point(polygon.xpoints[i + 1], polygon.ypoints[i + 1]);
            temp.add(new Line(a.x, a.y, b.x, b.y));
        }
        temp.add(new Line(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[0], polygon.ypoints[0]));
        return temp;
    }

    public Point getExtremPunktNW() {
        return new Point(minx, miny);
    }

    public Point getExtremPunktNO() {
        return new Point(maxx, miny);
    }

    public Point getExtremPunktSO() {
        return new Point(maxx, maxy);
    }

    public Point getExtremPunktSW() {
        return new Point(minx, maxy);
    }

    public void setX(int x) {
        super.setXY(x, this.y);
    }

    public void setY(int y) {
        super.setXY(this.x, y);
    }

    public void setXY(int x, int y) {
        super.setXY(x, y);
        calculate();
    }

    @Override
    public ToolType getToolType() {
        return ToolType.POLYGON;
    }

    @Override
    public ToolEntityButton getToolButton() {
        return new ToolEntityButton("Polygon", UtilClass.loadImageIconFromJAR("./img.jar", "buttons/polygon2.gif"), this);
    }
}
