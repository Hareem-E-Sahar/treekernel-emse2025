package org.rhwlab.tree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import org.rhwlab.acetree.NucUtils;
import org.rhwlab.snight.Nucleus;
import org.rhwlab.snight.Parameters;

/**
 * Representation of a cell or nucleus in the embryo
 * 
 * @author biowolp
 * @version 1.0 January 3, 2005
 */
public class Cell extends DefaultMutableTreeNode {

    private String iName;

    private int iTimeIndex;

    private int iPlane;

    private int iX;

    private int iY;

    private double iDia;

    private int iEndTime;

    private int iEndFate;

    private String iHashKey;

    private Hashtable iCellXHash;

    public int iEndingIndex;

    private int iLateTime;

    private Vector iCellData;

    public double ysc;

    public int yStartUse;

    public int xUse;

    private int iXmax;

    private Vector iCellsDrawn;

    public static int cMin = 25000;

    public static int cMax = 35000;

    private static double cScale;

    private static double cHeight = 400;

    private static int iEndingIndexS;

    public static void setEndingIndexS(int endingIndex) {
        iEndingIndexS = endingIndex;
    }

    public static int getEndingIndex() {
        return iEndingIndexS;
    }

    public static int xsc;

    public static void setXScale(int xScale) {
        xsc = xScale;
    }

    public void setLateTime(int time) {
        iLateTime = time;
    }

    public static void setMinRed(int min) {
        cMin = min;
        setRedScale();
    }

    public static void setMaxRed(int max) {
        cMax = max;
        setRedScale();
    }

    private static void setRedScale() {
        cScale = CMAP.length / ((double) (cMax - cMin));
    }

    public static void setHeight(double height) {
        cHeight = height;
    }

    /**
     * @param name String name of Cell
     * also stored in the parent as userObject 
     */
    public Cell(String name) {
        super(name);
        iName = (String) userObject;
        iEndingIndex = iEndingIndexS;
        iLateTime = iEndingIndex;
        iCellData = new Vector();
        cScale = CMAP.length / ((double) (cMax - cMin));
    }

    public Cell(Cell c) {
        iName = c.iName;
        iEndingIndex = c.iEndingIndex;
        iLateTime = c.iLateTime;
        iCellData = c.iCellData;
        iTimeIndex = c.iTimeIndex;
        iPlane = c.iPlane;
        iX = c.iX;
        iY = c.iY;
        iDia = c.iDia;
        iEndTime = c.iEndTime;
        iEndFate = c.iEndFate;
        iHashKey = c.iHashKey;
        iCellXHash = c.iCellXHash;
        Cell p = (Cell) c.getParent();
        setParent(p);
        Cell d1 = (Cell) c.getChildAt(0);
        insert(d1, 0);
        println("Cell, " + this);
    }

    public Cell(String name, int endingIndex) {
        this(name);
        iEndingIndex = endingIndex;
    }

    public Cell(String name, int endingIndex, int startTime) {
        this(name, endingIndex);
        iTimeIndex = startTime;
    }

    public Color getColor(int i) {
        CellData cd = (CellData) iCellData.elementAt(i);
        int red = cd.iNucleus.rweight;
        return getTheColor(getDiscrete(red));
    }

    public Color getLastColor() {
        CellData cd = (CellData) iCellData.lastElement();
        int red = cd.iNucleus.rweight;
        return getTheColor(getDiscrete(red));
    }

    private Color getColor(int i, Vector v) {
        if (v.size() == 0) return getTheColor(getDiscrete(0));
        CellData cd = (CellData) v.elementAt(i);
        int red = cd.iNucleus.rweight;
        return getTheColor(getDiscrete(red));
    }

    public void paintLine(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = (Graphics2D) g;
        int width = LINEWIDTH;
        g2d.setStroke(new BasicStroke(width));
        g2d.drawLine(x1, y1, x2, y2);
    }

    private void drawColoredLine(Graphics g, Cell c, int x1, int y1, int x2, int y2) {
        Vector use;
        if (!iName.equals(c.getName())) {
            use = c.iCellData;
        } else {
            use = iCellData;
        }
        int useSize = use.size();
        if (x1 != x2) {
            Cell parent = (Cell) c.getParent();
            Color color = getColor(use.size() - 1, use);
            g.setColor(color);
            g.drawLine(x1, y1, x2, y2);
        } else {
            int range = (int) Math.round((y2 - y1) / ysc);
            int k = Math.min(range, useSize);
            if (k == 0) {
                k = range;
                useSize = k;
            }
            if (k > 0) {
                int y2i = y1;
                int y10 = y1;
                for (int i = 0; i < k; i++) {
                    Color color = c.getColor(i, use);
                    g.setColor(color);
                    y2i = y10 + (y2 - y10) * (i + 1) / k;
                    paintLine(g, x1, y1, x2, y2);
                    y1 = y2i;
                }
            }
        }
        g.setColor(Color.black);
    }

    public void draw(Graphics g, int w, int h, int frameWidth, Hashtable cHash) {
        iCellsDrawn = new Vector();
        iCellXHash = cHash;
        int rootStart = iTimeIndex;
        double height = (iLateTime - iTimeIndex);
        ysc = (h - START1 - BORDERS) / height;
        iXmax = xsc;
        yStartUse = START1;
        xUse = draw(g, h, xsc + 20, yStartUse, this, cHash, rootStart);
        g.fillOval(xUse - 2, START1 - 2, 4, 4);
        g.drawString(this.toString(), xUse + 5, START1);
        fillInHash(this, cHash);
        g.setColor(Color.yellow);
        g.drawLine(xUse, START0, xUse, START1);
        g.setColor(Color.black);
        showScale(g, h - START1 - BORDERS, frameWidth);
        drawCellNames(g);
        iLateTime = iEndingIndex;
    }

    private void drawCellNames(Graphics g) {
        g.setColor(Color.BLACK);
        Enumeration e = iCellsDrawn.elements();
        while (e.hasMoreElements()) {
            Cell c = (Cell) e.nextElement();
            boolean b = isLeaf(c);
            if (!b && c != this) {
            }
        }
    }

    private boolean isLeaf(Cell c) {
        boolean rtn = false;
        if (c.isLeaf()) rtn = true; else if (c.getEndTime() > iLateTime) rtn = true;
        return rtn;
    }

    private int draw(Graphics g, int h, int x, int ystart, Cell c, Hashtable cHash, int rootStart) {
        iCellsDrawn.add(c);
        boolean done = false;
        int lastTime = c.iEndTime;
        int lateTime = iLateTime;
        if (c.iEndTime > lateTime) {
            done = true;
            lastTime = lateTime;
        }
        int length = (int) ((lastTime - c.iTimeIndex) * ysc + .5);
        c.yStartUse = (int) ((c.iTimeIndex - iTimeIndex) * ysc) + START1;
        if (c.getChildCount() == 0 || done) {
            if (x < iXmax) x = iXmax + xsc;
            drawColoredLine(g, c, x, c.yStartUse, x, c.yStartUse + length);
            g.setColor(Color.black);
            drawRotatedText(g, c.getName(), x, c.yStartUse + length + 5, Math.PI / 2);
            if (x > iXmax) iXmax = x;
            c.xUse = x;
            fillInHash(c, cHash);
            g.fillOval(c.xUse - 2, c.yStartUse - 2, 4, 4);
            return x;
        } else {
            Cell cLeft = (Cell) c.getChildAt(0);
            Cell cRite = (Cell) c.getChildAt(1);
            int nl = cLeft.getChildCount() / 2;
            if (nl == 0) nl = 1;
            int nr = cRite.getChildCount() + 1;
            int x1 = draw(g, h, x, yStartUse + length, cLeft, cHash, rootStart);
            cLeft.xUse = x1;
            if (!isLeaf(cLeft)) {
                g.fillOval(cLeft.xUse - 2, cLeft.yStartUse - 2, 4, 4);
                fillInHash(cLeft, cHash);
                drawRotatedText(g, cLeft.getName(), cLeft.xUse, cLeft.yStartUse - 5, -Math.PI / 8);
            }
            int xx = x1 + xsc * nl;
            int x2 = draw(g, h, xx, yStartUse + length, cRite, cHash, rootStart);
            cRite.xUse = x2;
            if (!isLeaf(cRite)) {
                g.fillOval(cRite.xUse - 2, cRite.yStartUse - 2, 4, 4);
                fillInHash(cRite, cHash);
                drawRotatedText(g, cRite.getName(), cRite.xUse, cRite.yStartUse - 5, -Math.PI / 8);
            }
            drawColoredLine(g, c, cLeft.xUse, cLeft.yStartUse, cRite.xUse, cRite.yStartUse);
            x = (x1 + x2) / 2;
            drawColoredLine(g, c, x, c.yStartUse, x, cLeft.yStartUse);
            return x;
        }
    }

    private void fillInHash(Cell c, Hashtable cHash) {
        int k = c.xUse * 10000 + c.yStartUse;
        cHash.put(new Integer(k), c);
    }

    private void showScale(Graphics g, int y, int frameWidth) {
        int lateTime = iLateTime;
        int dy = (int) ((y - START1 - BORDERS) * ysc);
        int x = 5;
        Color colorNow = g.getColor();
        g.setColor(Color.blue);
        g.drawLine(x, START1, x, y + START1);
        int k = iLateTime - iTimeIndex;
        double fy = y;
        double fk = k;
        double incOne = fy / fk;
        double incTen = 10 * incOne;
        k = (k - (k % 10)) / 10;
        int inc = 5;
        for (int i = 0; i <= k; i++) {
            int y0 = START1 + (int) Math.round(incTen * i);
            g.drawLine(x, y0, x + inc, y0);
        }
        g.drawString(String.valueOf(iTimeIndex), x + inc, START1);
        g.drawString(String.valueOf(lateTime), x + inc, START1 + y + 15);
        g.setColor(colorNow);
    }

    private void drawRotatedText(Graphics g, String s, int x, int y, double angle) {
        Point p1 = new Point(x, y);
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(angle);
        try {
            g2d.getTransform().inverseTransform((Point2D) p1, (Point2D) p1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (angle > 1.5) g2d.drawString(s, y, -x); else {
            Point p = myRotate(x, y);
            g2d.drawString(s, (int) p.getX(), (int) p.getY());
        }
        Point p = myRotate(x, y);
        g2d.rotate(-angle);
    }

    private Point myRotate(int x, int y) {
        double h = Math.sqrt(x * x + y * y);
        double a = Math.atan((double) y / (double) x);
        double b = .375 * Math.PI - a;
        int yy = (int) Math.round(h * Math.cos(b));
        int xx = (int) Math.round(h * Math.sin(b));
        return new Point(xx, yy);
    }

    public void updateCellData(Nucleus n) {
        iCellData.add(new CellData(n));
    }

    public Vector getCellData() {
        return iCellData;
    }

    public Vector getCellData(int start, int end) {
        if (start <= iTimeIndex && end > iEndTime) return iCellData;
        if (start > iEndTime) return null;
        Vector v = new Vector();
        start = Math.max(iTimeIndex, start);
        int last = iTimeIndex + iCellData.size() - 1;
        end = Math.min(last, end);
        for (int i = start; i <= end; i++) {
            v.add(iCellData.elementAt(i - iTimeIndex));
        }
        return v;
    }

    public Vector getAllCellData(int start, int end) {
        Vector rtn = new Vector();
        Cell p = (Cell) getParent();
        while (p != null) {
            Vector parentCellData = p.getCellData(start, end);
            if (parentCellData == null) break;
            rtn.addAll(0, parentCellData);
            p = (Cell) p.getParent();
        }
        Vector v = getCellData(start, end);
        if (v != null) rtn.addAll(v);
        return rtn;
    }

    public String getRedDataString(int first, int last, int separator, int[] count) {
        int k = 0;
        String sep = SEPARATORS[separator];
        String s = "";
        Enumeration e = iCellData.elements();
        int i = 0;
        for (i = 0; i < iCellData.size(); i++) {
            int time = i + iTimeIndex;
            if (time < first) break;
            if (time > last) continue;
            CellData cd = (CellData) iCellData.elementAt(i);
            double d = cd.iNucleus.rweight - 35000;
            if (k == 0) s += sep + ONEDEC.format(d + 0.1); else s += sep + NODEC.format(d);
            k++;
        }
        System.out.println("Cell " + iName + CS + k + CS + i);
        count[0] = k;
        return s;
    }

    private String blank9 = "         ";

    private String blank7 = "       ";

    private String[] blanks = { "", " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          " };

    private static final String[] SEPARATORS = { ", ", "\t" };

    private static final int COMMASPACE = 0, TAB = 1;

    private StringBuffer iRedHeader;

    public String getReverseRedDataString(int first, int last, int separator, int[] count) {
        String sep = SEPARATORS[separator];
        int k = 0;
        String s = "";
        String s1 = "";
        StringBuffer sb = new StringBuffer();
        for (int i = iCellData.size() - 1; i >= 0; i--) {
            int time = i + iTimeIndex;
            if (time < first) break;
            if (time > last) continue;
            CellData cd = (CellData) iCellData.elementAt(i);
            double d = cd.iNucleus.rweight - 35000;
            if (k == 0) {
                s1 = ONEDEC.format(d + 0.1);
                s += sep + s1;
                sb.append(makeHeaderName(s1));
            } else {
                s1 = NODEC.format(d);
                s += sep + s1;
                sb.append(makeHeaderName(s1));
            }
            k++;
        }
        System.out.println("Cell: " + iName + CS + k);
        iRedHeader = sb;
        count[0] = k;
        return s;
    }

    private String makeHeaderName(String num) {
        String s = "";
        int n = num.length();
        int m = iName.length();
        if (n > m) s = iName + blanks[n - m]; else {
            int k = m - n;
            s = iName.substring(k);
        }
        return CS + s;
    }

    public String getReverseRedDataHeaderString() {
        return iRedHeader.toString();
    }

    private static final DecimalFormat NODEC = new DecimalFormat("#######"), ONEDEC = new DecimalFormat("#######.#");

    /**
     * this is where the real parameters of a cell are set
     * @param time birth time of cell
     * @param n Nucleus object for this cell
     */
    public void setParameters(int time, int endTime, Nucleus n) {
        iTimeIndex = time;
        iPlane = (int) (n.z + NucUtils.HALFROUND);
        iX = n.x;
        iY = n.y;
        iDia = n.size;
        iEndTime = endTime;
        iEndFate = ALIVE;
        iCellData.add(new CellData(n));
    }

    /**
     * used only by Canonical Tree
     * @param time is the start time from file lineage2.gtr
     */
    public void setStartTime(int time) {
        iTimeIndex = time;
        iEndTime = 0;
        iEndFate = ALIVE;
    }

    public void setTime(int time) {
        iTimeIndex = time;
    }

    /**
     * access function for cell name
     * @return String cell name
     */
    public String getName() {
        return iName;
    }

    public void setName(String newName) {
        iName = newName;
    }

    public String getHashKey() {
        return iHashKey;
    }

    public void setHashKey(String hashKey) {
        iHashKey = hashKey;
    }

    /**
     * access function for cell start time
     * 
     * @return int time
     */
    public int getTime() {
        return iTimeIndex;
    }

    public int getEndTime() {
        return iEndTime;
    }

    /**
     * access function for cell end time
     * 
     * @return int end time
     */
    public int getEnd() {
        return iEndTime;
    }

    /**
     * access function for cell fate
     * 
     * @return int iEndFate
     */
    public String getFate() {
        return fates[iEndFate];
    }

    public int getFateInt() {
        return iEndFate;
    }

    /**
     * access function for plane where cell identified rounded to an int
     * @return int nearest image plane for cell birth
     */
    public int getPlane() {
        return iPlane;
    }

    /**
     * access function for cell X position at birth
     * @return int x position
     */
    public int getX() {
        return iX;
    }

    /**
     * access function for cell Y position at birth
     * @return int y position
     */
    public int getY() {
        return iY;
    }

    /**
     * provide String for outputting the cell object
     * @return String name of cell
     */
    public String toString() {
        return iName;
    }

    public String toString(int x) {
        StringBuffer sb = new StringBuffer(iName);
        sb.append(CS + iTimeIndex);
        sb.append(CS + iPlane);
        return sb.toString();
    }

    /**
     * access function for setting end time (division or death) of cell
     * @param time int time of death
     */
    public void setEndTime(int time) {
        iEndTime = time;
    }

    public void setEndingIndex(int time) {
        iEndingIndex = time;
    }

    /**
     * access function for setting the ultimate fate of the cell
     * @param fate int from: 0 -> ALIVE; 1 -> DIVIDED; 2 -> DIED
     */
    public void setEndFate(int fate) {
        iEndFate = fate;
    }

    /**
     * lifetime is difference between end time and birth time
     * @return int difference
     */
    public int getLifeTime() {
        return iEndTime - iTimeIndex + 1;
    }

    public boolean isAnterior() {
        Cell x = (Cell) getParent().getChildAt(0);
        return x == this;
    }

    /**
     * debugging function 
     *
     */
    public void showParameters() {
        System.out.println("showParameters starting");
        System.out.println("name: " + iName);
        System.out.println("time: " + iTimeIndex);
        System.out.println("plane: " + iPlane);
        System.out.println("x: " + iX);
        System.out.println("y: " + iY);
        System.out.println("dia: " + iDia);
        System.out.println("endTime: " + iEndTime);
        System.out.println("endFate: " + fates[iEndFate]);
        System.out.println("hashKey: " + iHashKey);
        System.out.println("showParameters ending");
    }

    public String showStuff() {
        StringBuffer sb = new StringBuffer();
        sb.append(iName + CS);
        sb.append(iTimeIndex + CS);
        sb.append(iEndTime + CS);
        sb.append(iEndingIndex);
        return sb.toString();
    }

    public static int getDiscrete(int r) {
        int k = 0;
        k = (int) ((r - cMin) * cScale);
        return k;
    }

    private static final Color GRAYCOLOR = new Color(128, 128, 128);

    public static Color getTheColor(int index) {
        if (index < 0) {
            index = 0;
            return GRAYCOLOR;
        }
        if (index >= CMAP2.length) index = CMAP2.length - 1;
        return CMAP2[index];
    }

    private static final Color[] CMAP = { new Color(000, 30, 255), new Color(000, 55, 230), new Color(000, 80, 205), new Color(000, 105, 180), new Color(000, 130, 155), new Color(000, 155, 130), new Color(000, 180, 105), new Color(000, 205, 80), new Color(000, 230, 55), new Color(000, 255, 30), new Color(30, 230, 000), new Color(55, 205, 000), new Color(85, 180, 000), new Color(105, 155, 000), new Color(130, 130, 000), new Color(155, 105, 000), new Color(180, 80, 000), new Color(205, 55, 000), new Color(230, 30, 000), new Color(255, 000, 000) };

    private static final Color[] CMAP3 = { new Color(000, 000, 255), new Color(000, 051, 255), new Color(000, 103, 255), new Color(000, 153, 255), new Color(000, 204, 255), new Color(000, 255, 255), new Color(000, 255, 204), new Color(000, 255, 153), new Color(000, 255, 103), new Color(000, 255, 051), new Color(000, 255, 000), new Color(051, 255, 000), new Color(153, 255, 000), new Color(204, 255, 000), new Color(255, 255, 000), new Color(255, 204, 000), new Color(255, 153, 000), new Color(255, 102, 000), new Color(255, 051, 000), new Color(255, 000, 000) };

    private static final Color[] CMAP2 = { new Color(000, 255, 0), new Color(000, 230, 0), new Color(000, 205, 0), new Color(000, 180, 0), new Color(000, 155, 0), new Color(000, 130, 0), new Color(000, 105, 0), new Color(000, 80, 0), new Color(000, 55, 0), new Color(000, 30, 0), new Color(30, 000, 0), new Color(55, 000, 0), new Color(85, 000, 0), new Color(105, 000, 0), new Color(130, 000, 0), new Color(155, 000, 0), new Color(180, 000, 0), new Color(205, 000, 0), new Color(230, 000, 0), new Color(255, 000, 0) };

    public static final int NAME = 4, TIME = 0, PLANE = 3, X = 1, Y = 2, DIA = 5, PREV = 12, START0 = 10, START1 = 20, BORDERS = 90, LINEWIDTH = 5;

    public static final int ALIVE = 0, DIVIDED = 1, DIED = 2;

    public static final int LARGEENDTIME = 500;

    public static final String[] fates = { "alive", "divided", "died" };

    private static final String CS = ", ";

    private static void println(String s) {
        System.out.println(s);
    }
}
