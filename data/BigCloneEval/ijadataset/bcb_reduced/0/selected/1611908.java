package ArianneEditor;

import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.Stroke;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Graphics;
import javolution.util.FastTable;
import javax.swing.JPanel;

/**
 * Group � la classe derivata da Shapes che implementa le regole di
 * visualizzazione di un raggruppamento (anche ricorsivo) di oggetti.
 * <p>
 *
 * @author      Andrea Annibali
 * @version     1.0
 */
class Group extends SimpleShape {

    private FastTable list;

    private String type;

    private double XoStretch;

    private double YoStretch;

    private double minXV = Integer.MAX_VALUE;

    private double maxXV = Integer.MIN_VALUE;

    private double minYV = Integer.MAX_VALUE;

    private double maxYV = Integer.MIN_VALUE;

    private String formatter = null;

    private int vertex;

    private int size;

    private double XoExtend;

    private double YoExtend;

    JMenuItem menuItem2;

    JMenuItem menuItem3;

    JMenuItem menuItem4;

    Group(int elId, Point ePoint, Point sPoint, Color sc, FastTable lst, String type, String imgN, boolean bck, int ovl, EditorDrawingPanel p) {
        super(imgN, p, elId, type, 4, 1, "Continuous", ovl, false, bck, ePoint, sPoint);
        list = lst;
        setShapeBorderColor(sc);
        this.inscribePoints(ePoint, sPoint);
        setIntCoord();
    }

    Group(int elId, double[] xPnt, double[] yPnt, Color sc, FastTable lst, String type, String imgN, boolean bck, int ovl, EditorDrawingPanel p) {
        super(imgN, p, elId, type, 4, xPnt, yPnt, 1, "Continuous", ovl, false, bck);
        list = lst;
        setShapeBorderColor(sc);
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (int j = 0; j < lst.size(); j++) {
            EditorShapes sh = (EditorShapes) lst.get(j);
            xPnt = sh.getXPoints();
            yPnt = sh.getYPoints();
            for (int i = 0; i < xPnt.length; i++) if (xPnt[i] < minX) minX = xPnt[i];
            for (int i = 0; i < xPnt.length; i++) if (xPnt[i] > maxX) maxX = xPnt[i];
            for (int i = 0; i < yPnt.length; i++) if (yPnt[i] < minY) minY = yPnt[i];
            for (int i = 0; i < yPnt.length; i++) if (yPnt[i] > maxY) maxY = yPnt[i];
        }
        this.inscribePoints(new Point((int) Math.round(maxX), (int) Math.round(maxY)), new Point((int) Math.round(minX), (int) Math.round(minY)));
        setIntCoord();
    }

    Group(int elId, Color c, int numV, int ovl, EditorDrawingPanel p) {
        super("", p, elId, "Group", numV, new double[numV], new double[numV], 1, "Continuous", ovl, false, false);
        setShapeBorderColor(c);
        setIntCoord();
    }

    /**
     * Inizializza i menu di pop-up che si attivano con il tasto destro
     * del mouse
     */
    public void initMenu() {
        super.initMenu();
        menuItem2 = new JMenuItem("Display rules");
        menuItem2.addActionListener(new Group_menuItem2_actionAdapter(this));
        popup.add(menuItem2);
        menuItem3 = new JMenuItem("Call Up Dialog");
        menuItem3.addActionListener(new Group_menuItem3_actionAdapter(this));
        popup.add(menuItem3);
        menuItem4 = new JMenuItem("Shape link Dialog");
        menuItem4.addActionListener(new Group_menuItem4_actionAdapter(this));
        popup.add(menuItem4);
    }

    public void trslY(double diff) {
        super.trslY(diff);
        for (int i = 0; i < getList().size(); i++) {
            ((EditorShapes) getList().get(i)).trslY(diff);
        }
    }

    public void trslX(double diff) {
        super.trslX(diff);
        for (int i = 0; i < getList().size(); i++) {
            ((EditorShapes) getList().get(i)).trslX(diff);
        }
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

    public void setXoStretch(double XoStretch) {
        this.XoStretch = XoStretch;
    }

    /**
     * Restituisce la coordinata X di riferimento per lo stretching
     * @return la coordinata X di riferimento per lo stretching
     */
    public double getXoStretch() {
        return this.XoStretch;
    }

    /**
     * Resituisce la coordinata Y di riferimento per lo stretching
     * @return la coordinata Y di riferimento per lo stretching
     */
    public double getYoStretch() {
        return this.YoStretch;
    }

    /**
     * Imposta la coordinata Y di riferimento per lo stretching
     * @param YoStretch la coordinata Y di riferimento per lo stretching
     */
    public void setYoStretch(double YoStretch) {
        this.YoStretch = YoStretch;
    }

    /**
     * Restituisce la coordinata X di riferimento per l'estensione
     * @return la coordinata X di riferimento per l'estensione
     */
    public double getXoExtend() {
        return this.XoExtend;
    }

    /**
     * Restituisce la coordinata Y di riferimento per l'estensione
     * @return la coordinata Y di riferimento per l'estensione
     */
    public double getYoExtend() {
        return this.YoExtend;
    }

    /**
     * Imposta la coordinata Y di riferimento per l'estensione
     * @param YoExtend la coordinata Y di riferimento per l'estensione
     */
    public void setYoExtend(double YoExtend) {
        this.YoExtend = YoExtend;
    }

    /**
     * Imposta la coordinata X di riferimento per lo stretching
     * @param XoStretch la coordinata X di riferimento per lo stretching
     */
    public void setXoExtend(double XoExtend) {
        this.XoExtend = XoExtend;
    }

    /**
     * Individua la regione di stretching
     * @param minXVal minimo tra le coordinate X del poligono di inscrizione
     * @param minYVal minimo tra le coordinate Y del poligono di inscrizione
     * @param maxXVal massimo tra le coordinate X del poligono di inscrizione
     * @param maxYVal massimo tra le coordinate Y del poligono di inscrizione
     */
    public void searchPoStretch(double minXVal, double minYVal, double maxXVal, double maxYVal) {
        if (getVertex() == 2) {
            setXoStretch(minXVal);
            setYoStretch(maxYVal);
        } else if (getVertex() == 3) {
            setXoStretch(maxXVal);
            setYoStretch(maxYVal);
        } else if (getVertex() == 1) {
            setXoStretch(minXVal);
            setYoStretch(minYVal);
        } else if (getVertex() == 0) {
            setXoStretch(maxXVal);
            setYoStretch(minYVal);
        }
        setIntCoord();
    }

    /**
     * Restituisce un intero che rappresenta il vertice interessato dal dragging
     * @return il vertice che sta subendo lo stretching
     */
    public int getVertex() {
        return this.vertex;
    }

    /**
     * Imposta il vertice interessato dal dragging
     * @param vertex l'intero che rappresenta il vertice che sta subendo lo stretching
     */
    public void setVertex(int vertex) {
        this.vertex = vertex;
    }

    /**
     * Ricerca ed imposta il vertice che � stato individuato tramite il mouse
     * @param ePoint punto del mouse
     * @param incX incremento lungo l'asse X
     * @param incY incremento lungo l'asse Y
     * @param minXVal valore minimo tra le X del poligono di inscrizione
     * @param minYVal valore minimo tra le Y del poligono di inscrizione
     * @param maxXVal valore massimo tra le X del poligono di inscrizione
     * @param maxYVal valore massimo tra le Y del poligono di inscrizione
     */
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

    /**
     * Effettua la rotazione del gruppo di oggetti di un angolo alfa rispetto al
     * punto p. Di fatto richiama ricorsivamente il metodo 'rotate' che ogni oggetto
     * componente implementa. Alla fine poi ricalcola le coordinate estreme del poligono
     * di inscrizione e richiama il metodo 'inscribePoints'.
     * @param alfa angolo di rotazione
     * @param p punto di rotazione
     */
    public void rotate(double alfa) {
        for (int i = 0; i < list.size(); i++) {
            EditorShapes actS = (EditorShapes) list.get(i);
            if (actS instanceof SimpleShape) {
                ((SimpleShape) actS).rotate(alfa);
            }
        }
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (int j = 0; j < list.size(); j++) {
            EditorShapes sh = (EditorShapes) list.get(j);
            double xPnt[] = sh.getXPoints();
            double yPnt[] = sh.getYPoints();
            for (int i = 0; i < xPnt.length; i++) if (xPnt[i] < minX) minX = xPnt[i];
            for (int i = 0; i < xPnt.length; i++) if (xPnt[i] > maxX) maxX = xPnt[i];
            for (int i = 0; i < yPnt.length; i++) if (yPnt[i] < minY) minY = yPnt[i];
            for (int i = 0; i < yPnt.length; i++) if (yPnt[i] > maxY) maxY = yPnt[i];
        }
        inscribeRotatePoints(new Point((int) Math.round(maxX), (int) Math.round(maxY)), new Point((int) Math.round(minX), (int) Math.round(minY)), type);
        setIntCoord();
    }

    /**
     * Imposta il colore di riempimento del gruppo di oggetti, richiamando
     * ricorsivamente il metodo 'fill' che implementano
     * @param c il colore di riempimento
     */
    public void fill(Color c) {
        for (int i = 0; i < list.size(); i++) {
            EditorShapes s = (EditorShapes) list.get(i);
            if (s instanceof FillableShape) {
                ((FillableShape) s).fill(c);
            }
        }
    }

    /**
     * Restituisce la lista di oggetti che compongono il gruppo
     * @return un oggetto di tipo FastTable contenente tutte le shape
     * che compongono il gruppo
     */
    public FastTable getList() {
        return list;
    }

    /**
     * Imposta la lista di oggetti che compongono il gruppo
     * @param lst oggetto di tipo FastTable contenente tutte le shape
     * che compongono il gruppo
     */
    public void setList(FastTable lst) {
        list = lst;
    }

    /**
     * Restituisce il formatter impostato
     * @return la stringa con cui � stato costruito il formatter
     */
    public String getFormatter() {
        return formatter;
    }

    public void drawOval(Graphics2D g, int x, int y, int w, int h, boolean toDraw) {
        if (toDraw && isInOverlay()) {
            g.drawOval(x, y, w, h);
        }
    }

    public void drawRect(Graphics2D g, int x, int y, int w, int h, boolean toDraw) {
        if (toDraw && isInOverlay()) {
            g.drawRect(x, y, w, h);
        }
    }

    public void drawPolygon(Graphics2D g, int x[], int y[], int v, boolean toDraw) {
        if (toDraw && isInOverlay()) {
            g.drawPolygon(x, y, v);
        }
    }

    /**
     * Visualizza il gruppo di oggetti implementandone le regole di rappresentazione.
     * Al suo interno vengono richiamati ricorsivamente i metodi draw che i singoli
     * oggetti implementano.
     * @param g l'oggetto graphics su cui disegnare l'oggetto testuale
     */
    public void draw(Graphics2D g, JPanel p, boolean toDraw) {
        Stroke drawingStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(getShapeBorderColor());
        ButtonShape lastButtonFnd = null;
        for (int i = 0; i < list.size(); i++) {
            EditorShapes actS = (EditorShapes) list.get(i);
            if (actS instanceof ButtonShape) {
                lastButtonFnd = (ButtonShape) actS;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            EditorShapes actS = (EditorShapes) list.get(i);
            if (this.executeQuery(sqlElementQuery[i])) {
                if (lastButtonFnd == null) {
                    actS.draw(g, p, isInOverlay());
                } else {
                    actS.draw(g, p, isInOverlay());
                }
            }
        }
        if (isSelected()) {
            int selWidth = ra * 2;
            int selHeight = ra * 2;
            int cX = ra;
            int cY = ra;
            g2d.setColor(Color.black);
            for (int i = 0; i < getNumVertex(); i++) {
                setXCoord(i, (int) Math.round(getXPoints()[i]));
                setYCoord(i, (int) Math.round(getYPoints()[i]));
            }
            for (int i = 0; i < getNumVertex(); i++) drawOval(g2d, getXCoordinates()[i] - cX, getYCoordinates()[i] - cY, selWidth, selHeight, toDraw);
            for (int i = 0; i < getNumVertex(); i++) {
                double midX = (getXPoints()[i] + getXPoints()[(i + 1) % getNumVertex()]) / 2;
                double midY = (getYPoints()[i] + getYPoints()[(i + 1) % getNumVertex()]) / 2;
                drawRect(g2d, (int) Math.round(midX) - ra, (int) Math.round(midY) - ra, selWidth, selHeight, toDraw);
            }
            g2d.setStroke(drawingStroke);
            drawPolygon(g2d, getXCoordinates(), getYCoordinates(), getNumVertex(), toDraw);
        }
        drawingStroke = new BasicStroke(1);
        g2d.setStroke(drawingStroke);
    }

    /**
     * Controlla se il punto p dato in ingresso � nell'area di selezione
     * del gruppo. Richiama ricorsivamente il metodo isInSelectArea dei singoli
     * oggetti componenti
     * @param p di cui controllare se le coordinate cadono nell'area di
     * selezione
     * @return true se il punto � nell'area di selezione
     */
    public boolean isInSelectArea(Point p) {
        boolean res = false;
        for (int i = 0; i < list.size(); i++) {
            EditorShapes actS = (EditorShapes) list.get(i);
            if (actS.isInSelectArea(p)) res = true;
        }
        return res;
    }

    /**
     * Controlla se il punto p dato in ingresso � all'interno dell'area di selezione
     * del gruppo. Richiama ricorsivamente il metodo isInsideArea dei singoli
     * oggetti componenti
     * @param p di cui controllare se le coordinate cadono nell'area di
     * selezione
     * @return true se il punto � nell'area di selezione
     */
    public boolean isInsideArea(Point p) {
        for (int i = 0; i < getNumVertex(); i++) {
            setXCoord(i, (int) Math.round(getXPoints()[i]));
            setYCoord(i, (int) Math.round(getYPoints()[i]));
        }
        if (p.x > getXCoordinates()[0] && p.x < getXCoordinates()[1] && p.y > getYCoordinates()[3] && p.y < getYCoordinates()[0]) return true; else return false;
    }

    /**
     * Dati i punti di inizio e fine dragging del mouse, che individuano il
     * rettangolo di inscrizione del gruppo di oggetti, setta le coordinate dei vertici
     * del poligono di inscrizione.
     * @param ePoint punto finale del dragging
     * @param sPoint punto iniziale del dragging
     */
    public void inscribePoints(Point ePoint, Point sPoint) {
        setMinXVal(Integer.MAX_VALUE);
        setMaxXVal(Integer.MIN_VALUE);
        setMinYVal(Integer.MAX_VALUE);
        setMaxYVal(Integer.MIN_VALUE);
        if (getList() != null) {
            int c = getList().size();
            for (int i = 0; i < c; i++) {
                EditorShapes actS = (EditorShapes) list.get(i);
                for (int j = 0; j < actS.getNumVertex(); j++) {
                    if (actS.getXPoints()[j] < getMinXVal()) setMinXVal(actS.getXPoints()[j]);
                    if (actS.getXPoints()[j] > getMaxXVal()) setMaxXVal(actS.getXPoints()[j]);
                    if (actS.getYPoints()[j] < getMinYVal()) setMinYVal(actS.getYPoints()[j]);
                    if (actS.getYPoints()[j] > getMaxYVal()) setMaxYVal(actS.getYPoints()[j]);
                }
            }
            setXPoint(0, getMinXVal());
            setYPoint(0, getMaxYVal());
            setXPoint(1, getMaxXVal());
            setYPoint(1, getMaxYVal());
            setXPoint(2, getMaxXVal());
            setYPoint(2, getMinYVal());
            setXPoint(3, getMinXVal());
            setYPoint(3, getMinYVal());
            setIntCoord();
        }
    }

    /**
     * Dopo una rotazione, dati i punti di inizio e fine dragging del mouse, che individuano il
     * rettangolo di inscrizione del gruppo di oggetti, setta le coordinate dei vertici
     * del poligono di inscrizione.
     * @param ePoint punto finale del dragging
     * @param sPoint punto iniziale del dragging
     * @param type usato
     */
    public void inscribeRotatePoints(Point ePoint, Point sPoint, String type) {
        setMinXVal(sPoint.x);
        setMaxXVal(ePoint.x);
        setMinYVal(sPoint.y);
        setMaxYVal(ePoint.y);
        setXPoint(0, getMinXVal());
        setYPoint(0, getMaxYVal());
        setXPoint(1, getMaxXVal());
        setYPoint(1, getMaxYVal());
        setXPoint(2, getMaxXVal());
        setYPoint(2, getMinYVal());
        setXPoint(3, getMinXVal());
        setYPoint(3, getMinYVal());
    }

    /**
     * Trova la minima coordinata X tra le coordinate X minime dei singoli oggetti
     * che compongono il gruppo
     * @return la minima coordinata X
     */
    public double searchMinXVal() {
        setMinXVal(Integer.MAX_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            EditorShapes actS = (EditorShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double minXAct = minXV;
                minXV = gr.searchMinXVal();
                if (minXV < getMinXVal()) {
                    setMinXVal(minXV);
                } else {
                    minXV = minXAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getXPoints()[l] < getMinXVal()) setMinXVal(actS.getXPoints()[l]);
                }
                minXV = getMinXVal();
            }
        }
        return minXV;
    }

    /**
     * Trova la massima coordinata X tra le coordinate X massime dei singoli oggetti
     * che compongono il gruppo
     * @return la massima coordinata X
     */
    public double searchMaxXVal() {
        setMaxXVal(Integer.MIN_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            EditorShapes actS = (EditorShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double maxXAct = maxXV;
                maxXV = gr.searchMaxXVal();
                if (maxXV > getMaxXVal()) {
                    setMaxXVal(maxXV);
                } else {
                    maxXV = maxXAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getXPoints()[l] > getMaxXVal()) setMaxXVal(actS.getXPoints()[l]);
                }
                maxXV = getMaxXVal();
            }
        }
        return maxXV;
    }

    /**
     * Trova la minima coordinata Y tra le coordinate Y minime dei singoli oggetti
     * che compongono il gruppo
     * @return la minima coordinata Y
     */
    public double searchMinYVal() {
        setMinYVal(Integer.MAX_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            EditorShapes actS = (EditorShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double minYAct = minYV;
                minYV = gr.searchMinYVal();
                if (minYV < getMinYVal()) {
                    setMinYVal(minYV);
                } else {
                    minYV = minYAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getYPoints()[l] < getMinYVal()) setMinYVal(actS.getYPoints()[l]);
                }
                minYV = getMinYVal();
            }
        }
        return minYV;
    }

    /**
     * Trova la massima coordinata Y tra le coordinate Y massime dei singoli oggetti
     * che compongono il gruppo
     * @return la massima coordinata Y
     */
    public double searchMaxYVal() {
        setMaxYVal(Integer.MIN_VALUE);
        for (int k = 0; k < getList().size(); k++) {
            EditorShapes actS = (EditorShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                double maxYAct = maxYV;
                maxYV = gr.searchMaxYVal();
                if (maxYV > getMaxYVal()) {
                    setMaxYVal(maxYV);
                } else {
                    maxYV = maxYAct;
                }
            } else {
                for (int l = 0; l < actS.getNumVertex(); l++) {
                    if (actS.getYPoints()[l] > getMaxYVal()) setMaxYVal(actS.getYPoints()[l]);
                }
                maxYV = getMaxYVal();
            }
        }
        return maxYV;
    }

    /**
     * Dati il punto di fine dragging del mouse e il vertice che sta subendo lo stretching,
     * effettua la deformazione secondo l'incremento determinato dai parametri incX e incY.
     * @param ePoint punto finale del dragging
     * @param vertex vertice su cui viene effettuato il dragging
     * @param incX incremento lungo l'asse X
     * @param incY incremento lungo l'asse Y
     * @param minXVal coordinata X minima del poligono entro cui � inscritto il gruppo
     * @param minYVal coordinata Y minima del poligono entro cui � inscritto il gruppo
     * @param maxXVal coordinata X massima del poligono entro cui � inscritto il gruppo
     * @param maxYVal coordinata Y massima del poligono entro cui � inscritto il gruppo
     * @param XoStretch coordinata X del punto di riferimento in base al quale effettuare la
     * deformazione
     * @param YoStretch coordinata Y del punto di riferimento in base al quale effettuare la
     * deformazione
     */
    public void stretch(Point ePoint, int vertex, double incX, double incY, double minXVal, double minYVal, double maxXVal, double maxYVal, double XoStretch, double YoStretch) {
        for (int k = 0; k < getList().size(); k++) {
            EditorShapes actS = (EditorShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                gr.stretch(ePoint, vertex, incX, incY, minXVal, minYVal, maxXVal, maxYVal, XoStretch, YoStretch);
            } else {
                actS.stretch(ePoint, vertex, incX, incY, minXVal, minYVal, maxXVal, maxYVal, XoStretch, YoStretch);
            }
        }
        setIntCoord();
    }

    /**
     * Dati il punto di fine dragging del mouse e il vertice che sta subendo lo stretching,
     * effettua la deformazione secondo l'incremento determinato dai parametri incX e incY.
     * @param ePoint Point punto finale del dragging
     * @param size int vertice su cui viene effettuato il dragging
     * @param incX double incremento lungo l'asse X
     * @param incY double incremento lungo l'asse Y
     * @param minXVal double coordinata X minima del poligono entro cui � inscritto il gruppo
     * @param minYVal double coordinata Y minima del poligono entro cui � inscritto il gruppo
     * @param maxXVal double coordinata X massima del poligono entro cui � inscritto il gruppo
     * @param maxYVal double coordinata Y massima del poligono entro cui � inscritto il gruppo
     * @param XoExtend double coordinata X del punto di riferimento in base al quale effettuare la
     * deformazione
     * @param YoExtend double coordinata Y del punto di riferimento in base al quale effettuare la
     * deformazione
     */
    public void extend(Point ePoint, int size, double incX, double incY, double minXVal, double minYVal, double maxXVal, double maxYVal, double XoExtend, double YoExtend) {
        for (int k = 0; k < getList().size(); k++) {
            EditorShapes actS = (EditorShapes) getList().get(k);
            if (actS instanceof Group) {
                Group gr = (Group) actS;
                gr.extend(ePoint, size, incX, incY, minXVal, minYVal, maxXVal, maxYVal, XoExtend, YoExtend);
            } else actS.extend(ePoint, size, incX, incY, minXVal, minYVal, maxXVal, maxYVal, XoExtend, YoExtend);
        }
        setIntCoord();
    }

    /**
     * Individua la regione di estensione
     * @param minXVal minimo tra le coordinate X del poligono di inscrizione
     * @param minYVal minimo tra le coordinate Y del poligono di inscrizione
     * @param maxXVal massimo tra le coordinate X del poligono di inscrizione
     * @param maxYVal massimo tra le coordinate Y del poligono di inscrizione
     */
    public void searchPoExtend(double minXVal, double minYVal, double maxXVal, double maxYVal) {
        if (getSize() == 0) {
            setXoExtend(maxXVal);
            setYoExtend(minYVal);
        } else if (getSize() == 1) {
            setXoExtend(minXVal);
            setYoExtend(minYVal);
        } else if (getSize() == 2) {
            setXoExtend(minXVal);
            setYoExtend(maxYVal);
        } else if (getSize() == 3) {
            setXoExtend(maxXVal);
            setYoExtend(maxYVal);
        }
    }

    /**
     * Richiama la dialog di impostazione delle regole di visualizzazione del gruppo di oggetti
     * @param e l'evento (selezione della seconda voce del menu di pop up)
     * che ha causato l'invocazione di questo metodo
     */
    void GroupmenuItem2_actionPerformed(ActionEvent e) {
        this.getFatherPanel().disableKeyListening();
        this.setQueriesAllowed(false);
        GroupDisplayDialog grd = new GroupDisplayDialog(this.getFatherFrame(), "Display rules", true, isLoggingEnabled());
        grd.init(this.getList(), this.sqlElementQuery);
        getFatherPanel().centerFrame(grd);
        grd.setVisible(true);
        this.sqlElementQuery = grd.selSqlElementQuery;
        this.setQueriesAllowed(true);
        this.getFatherPanel().enableKeyListening();
    }

    /**
     * Richiama la dialog di impostazione i comandi esterni da associare al gruppo
     * @param e l'evento (selezione della terza voce del menu di pop up)
     * che ha causato l'invocazione di questo metodo
     */
    void GroupmenuItem3_actionPerformed(ActionEvent e) {
        this.getFatherPanel().disableKeyListening();
        callUpDialog();
        this.getFatherPanel().enableKeyListening();
    }

    /**
     * Richiama la dialog di impostazione dei collegamenti della shape con le altre appartenenti
     * alla stessa immagine
     * @param e l'evento (selezione della quarta voce del menu di pop up)
     * che ha causato l'invocazione di questo metodo
     */
    void GroupmenuItem4_actionPerformed(ActionEvent e) {
        this.getFatherPanel().disableKeyListening();
        callUpAdiacencesDialog();
        this.getFatherPanel().enableKeyListening();
    }
}

class Group_menuItem2_actionAdapter implements java.awt.event.ActionListener {

    Group adaptee;

    Group_menuItem2_actionAdapter(Group adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.GroupmenuItem2_actionPerformed(e);
    }
}

class Group_menuItem3_actionAdapter implements java.awt.event.ActionListener {

    Group adaptee;

    Group_menuItem3_actionAdapter(Group adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.GroupmenuItem3_actionPerformed(e);
    }
}

class Group_menuItem4_actionAdapter implements java.awt.event.ActionListener {

    Group adaptee;

    Group_menuItem4_actionAdapter(Group adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.GroupmenuItem4_actionPerformed(e);
    }
}
