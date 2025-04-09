package ArianneViewer;

import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import javolution.util.FastTable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.awt.Stroke;
import java.sql.ResultSet;
import java.sql.Statement;
import com.borland.dx.sql.dataset.Database;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Graphics;
import javolution.text.Text;
import javax.swing.JPanel;

/**
 * <p>Title: Guide Viewer</p>
 *
 * <p>Description: Visualizzatore per pagine create con Arianne Editor</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Itaco S.r.l.</p>
 *
 * @author not attributable
 * @version 1.0
 */
class Group extends FillableShape {

    private FastTable elemGroupList;

    private String type;

    private double minXVal = Integer.MAX_VALUE;

    private double maxXVal = Integer.MIN_VALUE;

    private double minYVal = Integer.MAX_VALUE;

    private double maxYVal = Integer.MIN_VALUE;

    private int vertex;

    private int size;

    private long elapsed = 0;

    Graphics2D g2d = null;

    Stroke drawingStroke = new BasicStroke(1);

    private boolean[] itemVisible;

    JMenuItem menuItemSysCall;

    Group(int elId, Point ePoint, Point sPoint, int lt, String ls, float alpha, Color sbc, FastTable lst, String typ, DrawingPanel p, boolean isOp, boolean bck, int ovl, String imgName, boolean polling, int pollMsec, int bckMsec, Color fC) {
        super(typ, 4, elId, p, imgName, isOp, bck, ovl, lt, ls, alpha, sbc, ePoint, sPoint, polling, pollMsec, bckMsec, fC, false);
        elemGroupList = lst;
        setItemVisible(new boolean[elemGroupList.size()]);
        initGroup();
    }

    Group(int elId, double xPnt[], double yPnt[], int lt, String ls, float alpha, Color sbc, FastTable lst, String type, String imgName, DrawingPanel p, boolean isOp, boolean bck, int ovl, boolean polling, int pollMsec, int bckMsec, Color fC) {
        super("Group", 4, elId, p, imgName, isOp, bck, ovl, lt, ls, alpha, sbc, xPnt, yPnt, polling, pollMsec, bckMsec, fC, false);
        elemGroupList = lst;
        setItemVisible(new boolean[elemGroupList.size()]);
        initGroup();
    }

    public void initGroup() {
        if (elemGroupList != null) {
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            for (int k = 0; k < elemGroupList.size(); k++) {
                ViewerShapes actS = (ViewerShapes) elemGroupList.get(k);
                actS.setFatherPanel(this.getFatherPanel());
                double xPnt[] = actS.getXPoints();
                double yPnt[] = actS.getYPoints();
                for (int i = 0; i < xPnt.length; i++) if (xPnt[i] < minX) minX = xPnt[i];
                for (int i = 0; i < xPnt.length; i++) if (xPnt[i] > maxX) maxX = xPnt[i];
                for (int i = 0; i < yPnt.length; i++) if (yPnt[i] < minY) minY = yPnt[i];
                for (int i = 0; i < yPnt.length; i++) if (yPnt[i] > maxY) maxY = yPnt[i];
            }
            inscribePoints(new Point((int) Math.round(maxX), (int) Math.round(maxY)), new Point((int) Math.round(minX), (int) Math.round(minY)));
        }
    }

    public double getCurVal() {
        return 0;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void doWhenSelected(Graphics g, boolean toDraw) {
    }

    public void enqueueTabularCommands(Hashtable commandHashTable, Hashtable commandsOrder) {
    }

    public void enqueueButtonCommands(Hashtable commandHashTable, Hashtable commandsOrder) {
    }

    public void enqueueNShapesCommands(Hashtable commandHashTable, Hashtable commandsOrder) {
    }

    public void enqueueSShapesCommands(Hashtable commandHashTable, Hashtable commandsOrder) {
    }

    public int getVertex() {
        return this.vertex;
    }

    public void setVertex(int vertex) {
        this.vertex = vertex;
    }

    public void searchVertex(Point ePoint, double incX, double incY, double minXVal, double minYVal, double maxXVal, double maxYVal) {
        double Xv, Yv, Xm, Ym;
        Xm = (minXVal + maxXVal) / 2;
        Ym = (minYVal + maxYVal) / 2;
        Xv = ePoint.getX() - incX;
        Yv = ePoint.getY() - incY;
        if (Xv >= Xm && Yv <= Ym) {
            setVertex(2);
        } else if (Xv <= Xm && Yv <= Ym) {
            setVertex(3);
        } else if (Xv >= Xm && Yv >= Ym) {
            setVertex(1);
        } else if (Xv <= Xm && Yv >= Ym) {
            setVertex(0);
        }
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void searchSize(Point ePoint, double incX, double incY, double minXVal, double minYVal, double maxXVal, double maxYVal) {
        double Xv, Yv, Xm, Ym;
        Xm = (minXVal + maxXVal) / 2;
        Ym = (minYVal + maxYVal) / 2;
        Xv = ePoint.getX() - incX;
        Yv = ePoint.getY() - incY;
        if (Xv >= Xm && incY == 0) {
            setSize(2);
        } else if (Xv <= Xm && incY == 0) {
            setSize(0);
        } else if (Yv >= Ym && incX == 0) {
            setSize(1);
        } else if (Yv <= Ym && incX == 0) {
            setSize(3);
        }
    }

    public FastTable getList() {
        return elemGroupList;
    }

    public void setList(FastTable lst) {
        elemGroupList = lst;
    }

    public void setZoomFactor(double z) {
        super.setZoomFactor(z);
        if (elemGroupList != null && isVisible()) {
            for (int k = 0; k < elemGroupList.size(); k++) {
                ViewerShapes actS = (ViewerShapes) elemGroupList.get(k);
                actS.setZoomFactor(z);
            }
        }
    }

    public boolean thereAreChanges() {
        boolean res = false;
        setChanges(false);
        resultOfFastCheckColouringRule();
        if (getChanges()) {
            setLastChangeDrawn(false);
            return true;
        }
        resultOfColouringRule();
        if (getChanges()) {
            setLastChangeDrawn(false);
            return true;
        }
        resultOfBorderColouringRule();
        if (getChanges()) {
            setLastChangeDrawn(false);
            return true;
        }
        resultOfFillColouringRule();
        if (getChanges()) {
            setLastChangeDrawn(false);
            return true;
        }
        if (elemGroupList != null) {
            for (int k = 0; !res && k < elemGroupList.size(); k++) {
                ViewerShapes actS = (ViewerShapes) elemGroupList.get(k);
                res |= actS.thereAreChanges();
            }
        }
        return res;
    }

    public void draw(Graphics2D g, JPanel p, boolean toDraw) {
        Text resolvedVisExpr = resolve(getVisualizationExpression());
        if (isVisualizationRuleValid(resolvedVisExpr)) if (!verifiedVisualizationRule(resolvedVisExpr)) return;
        elapsed = ((ArianneViewer.DrawingPanel) p).getTimeInMsec() - lastReadMsecElapsed;
        if (((ArianneViewer.DrawingPanel) p).getTimerButton()) {
            if (elapsed > this.getFatherPanel().getRefreshPeriod()) {
                lastReadMsecElapsed += elapsed;
                setShapeBorderColor(resultOfFastCheckColouringRule());
                if (getShapeBorderColor() == null) {
                    setShapeBorderColor(resultOfColouringRule());
                    if (getShapeBorderColor() == null) {
                        setShapeBorderColor(resultOfBorderColouringRule());
                        setShapeFillColor(resultOfFillColouringRule());
                    } else {
                        setShapeBorderColor(getShapeBorderColor());
                        setShapeFillColor(getShapeFillColor());
                    }
                } else {
                    setShapeBorderColor(getShapeBorderColor());
                    setShapeFillColor(getShapeFillColor());
                }
                if (getShapeBorderColor() != null) g.setColor(getShapeBorderColor());
                setLastChangeDrawn(true);
            }
        }
        g2d = (Graphics2D) g;
        if (getShapeBorderColor() != null) g2d.setColor(getShapeBorderColor());
        updateVal();
        if (elemGroupList != null && isVisible()) {
            for (int k = 0; k < elemGroupList.size(); k++) {
                ViewerShapes actS = (ViewerShapes) elemGroupList.get(k);
                if (itemVisible[k]) actS.draw(g2d, ((ArianneViewer.DrawingPanel) p), isInOverlay());
            }
        }
        g2d.setStroke(drawingStroke);
    }

    public String getQueryId(int i) {
        return getImgName() + "-" + getElemId() + "-" + i;
    }

    public void setSqlElementQuery(int i, String q, int pollMsec, int bckMsec) {
        sqlElementQuery[i] = q;
        if (getDataProvider() != null) getDataProvider().addQuery(getImgName() + "-" + getElemId() + "-" + i, q, isPolling(), pollMsec, bckMsec);
    }

    public void setItemVisible(boolean[] v) {
        itemVisible = v;
    }

    public void setSqlElementQueries(String[] q) {
        sqlElementQuery = q;
        if (getDataProvider() != null) for (int i = 0; i < sqlElementQuery.length; i++) {
            getDataProvider().addQuery(getQueryId(i), q[i], isPolling(), getPollInterval(), getBckCycle());
        }
    }

    public void updateVal() {
        if (getDataProvider() != null) {
            if (!getDataProvider().t.isRunning()) getDataProvider().startDataRetrieval();
            for (int i = 0; i < sqlElementQuery.length - 1; i++) {
                Object resItem = getDataProvider().getVal(getQueryId(i), ArianneUtil.Util.GROUP_ITEM_VISIBLE);
                itemVisible[i] = (resItem != null && ((Integer) resItem).intValue() == 1) ? true : false;
            }
        } else {
            for (int i = 0; i < itemVisible.length; i++) {
                itemVisible[i] = true;
            }
        }
    }

    public boolean isInSelectArea(Point p) {
        if (p.x >= getMinX() && p.x <= getMaxX() && p.y >= getMinY() && p.y <= getMaxY()) return true; else return false;
    }

    public boolean isInsideArea(Point p) {
        if (p.x > getXCoordinates()[0] && p.x < getXCoordinates()[1] && p.y > getYCoordinates()[3] && p.y < getYCoordinates()[0]) {
            return true;
        } else {
            return false;
        }
    }

    public void setEdges() {
        if (elemGroupList != null) {
            setMinXVal(Integer.MAX_VALUE);
            setMaxXVal(Integer.MIN_VALUE);
            setMinYVal(Integer.MAX_VALUE);
            setMaxYVal(Integer.MIN_VALUE);
            int c = elemGroupList.size();
            if (elemGroupList != null) {
                for (int k = 0; k < elemGroupList.size(); k++) {
                    ViewerShapes actS = (ViewerShapes) elemGroupList.get(k);
                    for (int l = 0; l < actS.getNumVertex(); l++) {
                        if (actS.getXPoints()[l] < getMinXVal()) setMinXVal(actS.getXPoints()[l]);
                        if (actS.getXPoints()[l] > getMaxXVal()) setMaxXVal(actS.getXPoints()[l]);
                        if (actS.getYPoints()[l] < getMinYVal()) setMinYVal(actS.getYPoints()[l]);
                        if (actS.getYPoints()[l] > getMaxYVal()) setMaxYVal(actS.getYPoints()[l]);
                    }
                }
            }
            getXPoints()[0] = getMinXVal();
            getYPoints()[0] = getMaxYVal();
            getXPoints()[1] = getMaxXVal();
            getYPoints()[1] = getMaxYVal();
            getXPoints()[2] = getMaxXVal();
            getYPoints()[2] = getMinYVal();
            getXPoints()[3] = getMinXVal();
            getYPoints()[3] = getMinYVal();
            setIntCoord();
        }
    }

    public void inscribePoints(Point ePoint, Point sPoint) {
        setEdges();
    }

    public void inscribeRotatePoints(Point ePoint, Point sPoint, String type) {
        setMinXVal(sPoint.x);
        setMaxXVal(ePoint.x);
        setMinYVal(sPoint.y);
        setMaxYVal(ePoint.y);
        getXPoints()[0] = getMinXVal();
        getYPoints()[0] = getMaxYVal();
        getXPoints()[1] = getMaxXVal();
        getYPoints()[1] = getMaxYVal();
        getXPoints()[2] = getMaxXVal();
        getYPoints()[2] = getMinYVal();
        getXPoints()[3] = getMinXVal();
        getYPoints()[3] = getMinYVal();
    }

    public double searchMinXVal() {
        setMinXVal(Integer.MAX_VALUE);
        for (int k = 0; k < elemGroupList.size(); k++) {
            ViewerShapes actS = (ViewerShapes) elemGroupList.get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double minXAct = minXVal;
                minXVal = gr.searchMinXVal();
                if (minXVal < getMinXVal()) {
                    setMinXVal(minXVal);
                } else {
                    minXVal = minXAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getXPoints()[l] < getMinXVal()) setMinXVal(actS.getXPoints()[l]);
                }
                minXVal = getMinXVal();
            }
        }
        return minXVal;
    }

    public double searchMaxXVal() {
        setMaxXVal(Integer.MIN_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            ViewerShapes actS = (ViewerShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double maxXAct = maxXVal;
                maxXVal = gr.searchMaxXVal();
                if (maxXVal > getMaxXVal()) {
                    setMaxXVal(maxXVal);
                } else {
                    maxXVal = maxXAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getXPoints()[l] > getMaxXVal()) setMaxXVal(actS.getXPoints()[l]);
                }
                maxXVal = getMaxXVal();
            }
        }
        return maxXVal;
    }

    public double searchMinYVal() {
        setMinYVal(Integer.MAX_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            ViewerShapes actS = (ViewerShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double minYAct = minYVal;
                minYVal = gr.searchMinYVal();
                if (minYVal < getMinYVal()) {
                    setMinYVal(minYVal);
                } else {
                    minYVal = minYAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getYPoints()[l] < getMinYVal()) setMinYVal(actS.getYPoints()[l]);
                }
                minYVal = getMinYVal();
            }
        }
        return minYVal;
    }

    public double searchMaxYVal() {
        setMaxYVal(Integer.MIN_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            ViewerShapes actS = (ViewerShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double maxYAct = maxYVal;
                maxYVal = gr.searchMaxYVal();
                if (maxYVal > getMaxYVal()) {
                    setMaxYVal(maxYVal);
                } else {
                    maxYVal = maxYAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getYPoints()[l] > getMaxYVal()) setMaxYVal(actS.getYPoints()[l]);
                }
                maxYVal = getMaxYVal();
            }
        }
        return maxYVal;
    }

    public void shape_menuItem_actionPerformed(ActionEvent e) {
        Database localDb = this.getFatherPanel().getLocalDb();
        try {
            String query = "SELECT SYS_CALL_NAME FROM SYSCALL " + "WHERE DESCR = '" + e.getActionCommand() + "'";
            Statement sp = localDb.createStatement();
            ResultSet rp = sp.executeQuery(query);
            while (rp.next()) {
                if (!rp.getString("SYS_CALL_NAME").equals("") && !("" + rp.getString("SYS_CALL_NAME")).equals("null")) {
                    try {
                        Runtime.getRuntime().exec(rp.getString("SYS_CALL_NAME"));
                        rp.getString(rp.getString("SYS_CALL_NAME"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.err.println("Errore nel tentativo di esecuzione di " + rp.getString("SYS_CALL_NAME"));
                    }
                    rp.close();
                    sp.close();
                }
            }
        } catch (java.sql.SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }
}
