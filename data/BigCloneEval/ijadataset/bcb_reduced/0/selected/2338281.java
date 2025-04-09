package gui.mscItem;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * A Loop objektum reprezentalasara szolgalo osztaly
 * A Loop a Polygon oszt�lyb�l szarmazik.
 * 
 * @author Kiss G�bor
 *
 */
public class Loop extends Polygon {

    private static final long serialVersionUID = 1L;

    public static final int RIGHT = -1;

    public static final int LEFT = 1;

    public static final String STARTSERIAL = "start";

    public static final String STOPSERIAL = "stop";

    public static final String LONG = "long";

    public static final String RANDOM = "random";

    public static final String DELAY = "delay";

    public String ID;

    public String ParentID = "-1";

    public int startSerial;

    public int stopSerial;

    public int instanceSerial;

    public int _long;

    public Boolean _random;

    public String _delay;

    private int halfX;

    private int halfY;

    private int side;

    private Polygon upperArrow = new Polygon();

    private Polygon downerArrow = new Polygon();

    private Polygon externalRect = new Polygon();

    private Polygon internalRect = new Polygon();

    /**
	 * A Loop konstruktora.
	 * 
	 * @param lSide  -  megadja a rajzolasi oldalt
	 * @param instSer  -  melyik instance-on van a loop
	 * @param _startSer  -  melyik seial-nal kezdodik
	 * @param _stopSer  -  melyik serialnal vegzodik
	 */
    public Loop(int lSide, int instSer, int _startSer, int _stopSer) {
        super();
        side = lSide;
        startSerial = _startSer;
        stopSerial = _stopSer;
        instanceSerial = instSer;
        _long = 100;
        _random = false;
        _delay = "10";
        SetPoligonPoints();
    }

    /**
	 * Ez az eljaras allitja be a megfeleo meretu poligonokat a rajzolashoz
	 *
	 */
    public void SetPoligonPoints() {
        int leftX = 17 + (instanceSerial - 1) * 188;
        int rightX = 92 + (instanceSerial - 1) * 188;
        int upY = 58 + ((startSerial - 1) * 30) + ((instanceSerial - 1) * 7);
        int downY = 56 + ((stopSerial) * 30) + ((instanceSerial - 1) * 7);
        if (side == LEFT) {
            halfX = leftX + (rightX - leftX) / 2;
        } else if (side == RIGHT) {
            halfX = rightX + 8 + (rightX - leftX) / 2;
        }
        halfY = upY + (downY - upY) / 2;
        upperArrow.addPoint(halfX + (38 * side), upY);
        upperArrow.addPoint(halfX - (2 * side), upY);
        upperArrow.addPoint(halfX - (2 * side), halfY - 9);
        upperArrow.addPoint(halfX - (7 * side), halfY - 10);
        upperArrow.addPoint(halfX, halfY - 5);
        upperArrow.addPoint(halfX + (7 * side), halfY - 10);
        upperArrow.addPoint(halfX + (2 * side), halfY - 9);
        upperArrow.addPoint(halfX + (2 * side), upY + 4);
        upperArrow.addPoint(halfX + (38 * side), upY + 4);
        downerArrow.addPoint(halfX + (38 * side), downY);
        downerArrow.addPoint(halfX - (2 * side), downY);
        downerArrow.addPoint(halfX - (2 * side), halfY + 9);
        downerArrow.addPoint(halfX - (7 * side), halfY + 10);
        downerArrow.addPoint(halfX, halfY + 5);
        downerArrow.addPoint(halfX + (7 * side), halfY + 10);
        downerArrow.addPoint(halfX + (2 * side), halfY + 9);
        downerArrow.addPoint(halfX + (2 * side), downY - 4);
        downerArrow.addPoint(halfX + (38 * side), downY - 4);
        externalRect.addPoint(halfX - (30 * side), halfY - 7);
        externalRect.addPoint(halfX - (30 * side), halfY + 7);
        externalRect.addPoint(halfX + (30 * side), halfY + 7);
        externalRect.addPoint(halfX + (30 * side), halfY - 7);
        internalRect.addPoint(halfX - (27 * side), halfY - 5);
        internalRect.addPoint(halfX - (27 * side), halfY + 5);
        internalRect.addPoint(halfX + (27 * side), halfY + 5);
        internalRect.addPoint(halfX + (27 * side), halfY - 5);
    }

    /**
	 * Az eljaras visszadaja az adott loop objektum m�solat�t
	 */
    public Loop clone() {
        Loop _loop = new Loop(side, instanceSerial, startSerial, stopSerial);
        _loop.ID = ID;
        _loop.ParentID = ParentID;
        _loop._long = _long;
        _loop._random = _random;
        _loop._delay = _delay;
        return _loop;
    }

    /**
	 * Rajzolo eljaras a poligonhoz, A rajzolas csak akkor tortnik meg, ha az adott Loop
	 * egyetlen Loop-nak sem valamely kovetkezmen-loopja.
	 *  
	 * @param gr - A grafikus felulet a rajzol�shoz
	 * @param _c - sz�nparam�ter, az alo, es a felso nyilak szinet
	 *             allithatjuk vele.
	 */
    public void draw(Graphics gr, Color _c) {
        gr.setColor(Color.BLACK);
        gr.fillPolygon(externalRect);
        gr.setColor(Color.WHITE);
        gr.fillPolygon(internalRect);
        gr.setColor(_c);
        if (ParentID != "-1") {
            gr.setColor(Color.green);
        }
        gr.fillPolygon(upperArrow);
        gr.fillPolygon(downerArrow);
        gr.setColor(Color.BLACK);
        gr.setFont(new Font("Arial", Font.PLAIN, 8));
        String out = "<" + Integer.toString(_long) + ";" + _delay + ">";
        gr.drawString(out, halfX - ((out.length() * 4) / 2), halfY + 3);
    }

    /**
	 * Az eljaras a poligon osztaly letezo fuggvenyenek feluldefinialasa, mert a loop 
	 * tobb poligonbol epul fel.
	 * 
	 * @param x - az eg�rpozicio x koordinataja
	 * @param y - az egerpozicio y koordinataja
	 * 
	 * @return boolean - Igaz, ha a kattintas koordinatajat tartalmazza a loop poligon
	 */
    public boolean contains(int x, int y) {
        if (externalRect.contains(x, y) || upperArrow.contains(x, y) || downerArrow.contains(x, y)) {
            return true;
        }
        return false;
    }

    /**
	 * A felso nyil teruletet ellenorzi, hogy azon tortent-e egerkattintas
	 * 
	 * @param x - az eg�rpozicio x koordinataja
	 * @param y - az egerpozicio y koordinataja
	 * 
	 * @return boolean - Igaz, ha a kattintas koordinatajat tartalmazza az upperArrow
	 *                   poligon
	 */
    public boolean upperArrowContaines(int x, int y) {
        return upperArrow.contains(x, y);
    }

    /**
	 * Az aso nyil teruletet ellenorzi, hogy azon tortent-e egerkattintas
	 * 
	 * @param x - az eg�rpozicio x koordinataja
	 * @param y - az egerpozicio y koordinataja
	 * 
	 * @return boolean - Igaz, ha a kattintas koordinatajat tartalmazza a downerArrow
	 *                   poligon
	 */
    public boolean downerArrowContaines(int x, int y) {
        return downerArrow.contains(x, y);
    }
}
