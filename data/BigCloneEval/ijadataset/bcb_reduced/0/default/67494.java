import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.Vector;
import java.util.Stack;
import java.util.Random;

/**
 * GPanel is capable of drawing,editing and executing algorithms on a graph.
 * It receives messages/commands from various directions requesting to
 * perform various actions on the graph.
 * The data type of the graph it handles is "Graph".
 *
 * @version 1.0
 * @author K.Vamsi Krishna
 * @author P.Jaya Krishna
 */
class GPanel extends JPanel implements Printable {

    GFrame gFrame;

    private Graph currGraph;

    private int prevVertex;

    private int currVertex;

    private int currEdgeFrom;

    private int currEdgeTo;

    private boolean showEdgeWeights;

    private boolean showVertexNames;

    private int currentAction;

    private Graph savedGraph;

    int animationDelay = 1000;

    private Stack<Graph> undoList;

    private Stack<Graph> redoList;

    private int lastUndoMoveAddedFor;

    /**
	 * Indicates that user may select vertex/edge of the GPanel.
	 */
    public static final int ACTION_SELECT = 0;

    /**
	 * Indicates that user may perform vertex addition/deletion.
	 */
    public static final int ACTION_VERTEX = 1;

    /**
	 * Indicates that user is executing an algorithm.
	 * All operations on GPanel are negelected while in this state.
	 */
    public static final int ACTION_RUN = 2;

    /**
	 * Indicates that user may perform edge addition/deletion.
	 */
    public static final int ACTION_EDGE = 3;

    private GPanelHandler gpHandler;

    /**
	 * Create a new GPanel object initialized to work on graph "g".
	 * @param g Refers to the graph on which this GPanel object operates, if g is null it creates a Graph object internally.
	 */
    public GPanel(GFrame gFrame, Graph g) {
        this.gFrame = gFrame;
        if (g == null) currGraph = new Graph(Graph.DIRECTED); else currGraph = g;
        prevVertex = -1;
        currVertex = -1;
        showEdgeWeights = true;
        showVertexNames = true;
        currentAction = ACTION_SELECT;
        savedGraph = null;
        gpHandler = new GPanelHandler(this);
        addMouseListener(gpHandler);
        addMouseMotionListener(gpHandler);
        GraphAlg.setMessagePanel(gFrame.messagePanel);
        GraphAlg.setHelpPanel(gFrame.helpPanel);
        undoList = new Stack<Graph>();
        redoList = new Stack<Graph>();
        lastUndoMoveAddedFor = -1;
    }

    /**
	 * Sets this GPanel object to work on "g".
	 */
    public void setGraph(Graph g) {
        currGraph = new Graph(g);
        prevVertex = -1;
        currVertex = -1;
        savedGraph = null;
        undoList.clear();
        redoList.clear();
        lastUndoMoveAddedFor = -1;
        gFrame.toolBar.TBUndo.setEnabled(false);
        gFrame.toolBar.TBRedo.setEnabled(false);
        gFrame.menuBar.menuEditUndo.setEnabled(false);
        gFrame.menuBar.menuEditRedo.setEnabled(false);
        repaint();
    }

    /**
	 * Returns the graph on which it is working.
	 */
    public Graph getGraph() {
        return currGraph;
    }

    /**
	 * Sets whether to show edge weights or not.
	 * @param flag True indicates to display weights.
	 */
    public void setShowEdgeWeights(boolean flag) {
        showEdgeWeights = flag;
        repaint();
    }

    /**
	 * Returns whether edge weights are being display or not.
	 * @return True indicates that it is displaying weights.
	 */
    public boolean getShowEdgeWeights() {
        return showEdgeWeights;
    }

    /**
	 * Sets whether to show vertex names or not.
	 * @param flag True indicates to display names.
	 */
    public void setShowVertexNames(boolean flag) {
        showVertexNames = flag;
        repaint();
    }

    /**
	 * Returns whether vertex names are being display or not.
	 * @return True indicates that it is displaying names.
	 */
    public boolean getShowVertexNames() {
        return showVertexNames;
    }

    /**
	 * Returns the vertex that was active before currVertex
	 */
    public int getCurrVertex() {
        return currVertex;
    }

    /**
	 * Returns the vertex that is currently selected
	 */
    public int getPrevVertex() {
        return prevVertex;
    }

    private synchronized void forceCurrentAction(int currentAction) {
        this.currentAction = currentAction;
        if (this.currentAction == ACTION_RUN) {
            gFrame.menuBar.disable();
            gFrame.toolBar.disable();
            disableEvents();
        } else {
            gFrame.menuBar.enable();
            gFrame.toolBar.enable();
            enableEvents();
        }
    }

    public synchronized void setCurrentAction(int currentAction) {
        if (this.currentAction != ACTION_RUN) this.currentAction = currentAction;
        if (this.currentAction == ACTION_RUN) {
            gFrame.menuBar.disable();
            gFrame.toolBar.disable();
            disableEvents();
        } else {
            gFrame.menuBar.enable();
            gFrame.toolBar.enable();
            enableEvents();
        }
    }

    public synchronized int getCurrentAction() {
        return this.currentAction;
    }

    public int getCurrEdgeFrom() {
        return currEdgeFrom;
    }

    public int getCurrEdgeTo() {
        return currEdgeTo;
    }

    public void addUndo() {
        if (undoList.size() == 10) undoList.remove(0);
        undoList.push(new Graph(currGraph));
        redoList.clear();
        gFrame.toolBar.TBRedo.setEnabled(false);
        gFrame.toolBar.TBUndo.setEnabled(true);
        gFrame.menuBar.menuEditRedo.setEnabled(false);
        gFrame.menuBar.menuEditUndo.setEnabled(true);
    }

    public void undo() {
        if (undoList.size() > 0) {
            redoList.push(new Graph(currGraph));
            currGraph = undoList.pop();
            repaint();
            lastUndoMoveAddedFor = -1;
        }
        if (undoList.size() == 0) {
            gFrame.toolBar.TBUndo.setEnabled(false);
            gFrame.menuBar.menuEditUndo.setEnabled(false);
        }
        if (redoList.size() > 0) {
            gFrame.toolBar.TBRedo.setEnabled(true);
            gFrame.menuBar.menuEditRedo.setEnabled(true);
        }
    }

    public void redo() {
        if (redoList.size() > 0) {
            undoList.push(new Graph(currGraph));
            currGraph = redoList.pop();
            repaint();
        }
        if (redoList.size() == 0) {
            gFrame.toolBar.TBRedo.setEnabled(false);
            gFrame.menuBar.menuEditRedo.setEnabled(false);
        }
        if (undoList.size() > 0) {
            gFrame.toolBar.TBUndo.setEnabled(true);
            gFrame.menuBar.menuEditUndo.setEnabled(true);
        }
    }

    /**
	 * Creates a copy of the currGraph, used for refresh.
	 * Should be called before a refresh.
	 */
    public void saveForRefresh() {
        savedGraph = new Graph(currGraph);
    }

    /**
	 * Refresh is done by restoring currGraph from savedGraph
	 */
    public void refresh() {
        if (savedGraph != null) {
            currGraph = savedGraph;
            savedGraph = null;
            repaint();
        }
    }

    /**
	 * Overrided so that it draws graph also.
	 */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        drawGraph(g);
    }

    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        if (pi >= 1) return Printable.NO_SUCH_PAGE;
        drawGraph(g);
        return Printable.PAGE_EXISTS;
    }

    private void drawGraph(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Paint oldPaint;
        Font oldFont;
        FontMetrics fm;
        int nv = currGraph.numVertices();
        gFrame.statusBar.setMessage(GStatusBar.STATUS_GRAPHTYPE, (currGraph.isDirected() ? "Directed" : "Undirected") + " V:" + currGraph.numVertices() + " E:" + currGraph.numEdges());
        try {
            oldPaint = g2.getPaint();
            oldFont = g2.getFont();
            g2.setFont(new Font(oldFont.getFontName(), oldFont.getStyle(), oldFont.getSize() - 2));
            fm = g2.getFontMetrics();
            if (currGraph.isDirected() == false) {
                int i, j;
                int x1, y1, x2, y2;
                int cx, cy;
                int r;
                EdgeAttr ea;
                VertexAttr va1, va2;
                for (i = 0; i < currGraph.numVertices(); i++) {
                    for (j = 0; j < i; j++) {
                        if (currGraph.edgeExists(i, j)) {
                            ea = currGraph.getEdge(i, j);
                            va1 = currGraph.getVertex(i);
                            va2 = currGraph.getVertex(j);
                            x1 = va1.getXpos();
                            y1 = va1.getYpos();
                            x2 = va2.getXpos();
                            y2 = va2.getYpos();
                            cx = (x1 + x2) / 2;
                            cy = (y1 + y2) / 2;
                            g2.setPaint(ea.getColor());
                            g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            if (getShowEdgeWeights()) r = Math.max(12, 2 + fm.stringWidth("" + ea.getWeight())); else r = 6;
                            g2.setPaint(Color.WHITE);
                            g2.fill(new Ellipse2D.Double(cx - r / 2, cy - r / 2, r, r));
                            g2.setPaint(ea.getColor());
                            g2.draw(new Ellipse2D.Double(cx - r / 2, cy - r / 2, r, r));
                            if (getShowEdgeWeights()) g2.drawString("" + ea.getWeight(), (int) (cx - fm.stringWidth("" + ea.getWeight()) / 2), (int) (cy + fm.getAscent() / 2));
                        }
                    }
                }
            } else {
                int i, j;
                int x1, y1, x2, y2;
                int w2, h2;
                int cx, cy;
                int r;
                double len, ang, ad;
                EdgeAttr ea;
                VertexAttr va1, va2;
                for (i = 0; i < currGraph.numVertices(); i++) {
                    for (j = 0; j < currGraph.numVertices(); j++) {
                        if (currGraph.edgeExists(i, j)) {
                            ea = currGraph.getEdge(i, j);
                            va1 = currGraph.getVertex(i);
                            va2 = currGraph.getVertex(j);
                            x1 = va1.getXpos();
                            y1 = va1.getYpos();
                            x2 = va2.getXpos();
                            y2 = va2.getYpos();
                            w2 = va2.getWidth();
                            h2 = va2.getHeight();
                            len = Math.hypot(x2 - x1, y2 - y1);
                            ang = Math.atan2(y2 - y1, x2 - x1);
                            ad = 0.5 * Math.sqrt(w2 * w2 + h2 * h2);
                            cx = (int) (x1 + (len - ad - 20) * Math.cos(ang));
                            cy = (int) (y1 + (len - ad - 20) * Math.sin(ang));
                            if ((i <= j) || (i > j && currGraph.edgeExists(j, i) == false)) {
                                g2.setPaint(ea.getColor());
                                g2.draw(new Line2D.Double(x1, y1, x2, y2));
                            }
                            if (getShowEdgeWeights()) r = Math.max(12, 2 + fm.stringWidth("" + ea.getWeight())); else r = 6;
                            g2.setPaint(Color.WHITE);
                            g2.fill(new Ellipse2D.Double(cx - r / 2, cy - r / 2, r, r));
                            g2.setPaint(ea.getColor());
                            g2.draw(new Ellipse2D.Double(cx - r / 2, cy - r / 2, r, r));
                            if (getShowEdgeWeights()) g2.drawString("" + ea.getWeight(), (int) (cx - fm.stringWidth("" + ea.getWeight()) / 2), (int) (cy + fm.getAscent() / 2));
                            double px = x1 + (len - ad) * Math.cos(ang);
                            double py = y1 + (len - ad) * Math.sin(ang);
                            double p1x = px - 10 * Math.cos(ang - (30 * Math.PI / 180));
                            double p1y = py - 10 * Math.sin(ang - (30 * Math.PI / 180));
                            double p2x = px - 10 * Math.cos(ang + (30 * Math.PI / 180));
                            double p2y = py - 10 * Math.sin(ang + (30 * Math.PI / 180));
                            g2.draw(new Line2D.Double(px, py, p1x, p1y));
                            g2.draw(new Line2D.Double(px, py, p2x, p2y));
                        }
                    }
                }
            }
            g2.setPaint(oldPaint);
            g2.setFont(oldFont);
            oldPaint = g2.getPaint();
            fm = g2.getFontMetrics();
            for (int i = 0; i < nv; i++) {
                VertexAttr va = currGraph.getVertex(i);
                int vx = va.getXpos();
                int vy = va.getYpos();
                int vw = va.getWidth();
                int vh = va.getHeight();
                if (va.getShape() == VertexAttr.SHAPE_CIRCLE) {
                    g2.setPaint(va.getFillColor());
                    g2.fill(new Ellipse2D.Double(vx - vw / 2, vy - vh / 2, vw, vh));
                    g2.setPaint(va.getColor());
                    g2.draw(new Ellipse2D.Double(vx - vw / 2, vy - vh / 2, vw, vh));
                } else {
                    g2.setPaint(va.getFillColor());
                    g2.fill(new Rectangle2D.Double(vx - vw / 2, vy - vh / 2, vw, vh));
                    g2.setPaint(va.getColor());
                    g2.draw(new Rectangle2D.Double(vx - vw / 2, vy - vh / 2, vw, vh));
                }
                if (getShowVertexNames()) g2.drawString(va.getName(), vx - fm.stringWidth(va.getName()) / 2, vy + fm.getAscent() / 2);
            }
            g2.setPaint(oldPaint);
            oldPaint = g2.getPaint();
            if (currVertex != -1) {
                VertexAttr va = currGraph.getVertex(currVertex);
                int vx = va.getXpos();
                int vy = va.getYpos();
                int vw = va.getWidth();
                int vh = va.getHeight();
                g2.setPaint(va.getColor());
                g2.draw(new Rectangle2D.Double(vx - vw / 2 - 4 - 2, vy - 2, 4, 4));
                g2.draw(new Rectangle2D.Double(vx + vw / 2 + 4 - 2, vy - 2, 4, 4));
                g2.draw(new Rectangle2D.Double(vx - 2, vy - vh / 2 - 4 - 2, 4, 4));
                g2.draw(new Rectangle2D.Double(vx - 2, vy + vh / 2 + 4 - 2, 4, 4));
            }
            g2.setPaint(oldPaint);
        } catch (IllegalVertexException e) {
            System.out.println("Check error in GPanel.drawGraph");
        }
    }

    /**
	 * Checkes if there is any vertex at the specified position.
	 * @return True if there is a vertex at the specified position
	 */
    public boolean checkVertex(int x, int y) {
        VertexAttr v1;
        int nv;
        nv = currGraph.numVertices();
        try {
            for (int i = 0; i < nv; i++) {
                v1 = currGraph.getVertex(i);
                if (x >= (v1.getXpos() - v1.getWidth() / 2) && x <= (v1.getXpos() + v1.getWidth() / 2) && y >= (v1.getYpos() - v1.getHeight() / 2) && y <= (v1.getYpos() + v1.getHeight() / 2)) {
                    prevVertex = currVertex;
                    currVertex = i;
                    repaint();
                    return true;
                }
            }
        } catch (IllegalVertexException e) {
            System.out.println("Check error in GPanel.checkVertex");
        }
        currVertex = -1;
        return false;
    }

    public boolean checkEdge(int x, int y) {
        Graphics2D g2 = (Graphics2D) getGraphics();
        Font oldFont;
        FontMetrics fm;
        try {
            oldFont = g2.getFont();
            g2.setFont(new Font(oldFont.getFontName(), oldFont.getStyle(), oldFont.getSize() - 2));
            fm = g2.getFontMetrics();
            if (currGraph.isDirected() == false) {
                int i, j;
                int x1, y1, x2, y2;
                int cx, cy;
                int r;
                EdgeAttr ea;
                VertexAttr va1, va2;
                for (i = 0; i < currGraph.numVertices(); i++) {
                    for (j = 0; j < i; j++) {
                        if (currGraph.edgeExists(i, j)) {
                            ea = currGraph.getEdge(i, j);
                            va1 = currGraph.getVertex(i);
                            va2 = currGraph.getVertex(j);
                            x1 = va1.getXpos();
                            y1 = va1.getYpos();
                            x2 = va2.getXpos();
                            y2 = va2.getYpos();
                            cx = (x1 + x2) / 2;
                            cy = (y1 + y2) / 2;
                            if (getShowEdgeWeights()) r = Math.max(12, 2 + fm.stringWidth("" + ea.getWeight())); else r = 6;
                            if (x >= cx - r / 2 && x <= cx + r / 2 && y >= cy - r / 2 && y <= cy + r / 2) {
                                currEdgeFrom = i;
                                currEdgeTo = j;
                                return true;
                            }
                        }
                    }
                }
            } else {
                int i, j;
                int x1, y1, x2, y2;
                int w2, h2;
                int cx, cy;
                int r;
                double len, ang, ad;
                EdgeAttr ea;
                VertexAttr va1, va2;
                for (i = 0; i < currGraph.numVertices(); i++) {
                    for (j = 0; j < currGraph.numVertices(); j++) {
                        if (currGraph.edgeExists(i, j)) {
                            ea = currGraph.getEdge(i, j);
                            va1 = currGraph.getVertex(i);
                            va2 = currGraph.getVertex(j);
                            x1 = va1.getXpos();
                            y1 = va1.getYpos();
                            x2 = va2.getXpos();
                            y2 = va2.getYpos();
                            w2 = va2.getWidth();
                            h2 = va2.getHeight();
                            len = Math.hypot(x2 - x1, y2 - y1);
                            ang = Math.atan2(y2 - y1, x2 - x1);
                            ad = 0.5 * Math.sqrt(w2 * w2 + h2 * h2);
                            cx = (int) (x1 + (len - ad - 20) * Math.cos(ang));
                            cy = (int) (y1 + (len - ad - 20) * Math.sin(ang));
                            if (getShowEdgeWeights()) r = Math.max(12, 2 + fm.stringWidth("" + ea.getWeight())); else r = 6;
                            if (x >= cx - r / 2 && x <= cx + r / 2 && y >= cy - r / 2 && y <= cy + r / 2) {
                                currEdgeFrom = i;
                                currEdgeTo = j;
                                return true;
                            }
                        }
                    }
                }
            }
            g2.setFont(oldFont);
        } catch (IllegalVertexException e) {
            System.out.println("Check error in GPanel.drawGraph");
        }
        currEdgeFrom = -1;
        currEdgeTo = -1;
        return false;
    }

    /**
	 * Moves the current vertex to a new position specified by x, y.
	 */
    public void doMoveVertex(int x, int y) {
        if (currVertex != -1) {
            try {
                VertexAttr v = (VertexAttr) currGraph.getVertex(currVertex);
                v.setXpos(x);
                v.setYpos(y);
                if (lastUndoMoveAddedFor != currVertex) {
                    lastUndoMoveAddedFor = currVertex;
                    addUndo();
                }
                currGraph.setVertex(currVertex, v);
                repaint();
            } catch (IllegalVertexException e) {
                System.out.println("Check error in GPanel.moveVertex");
            }
        }
    }

    /**
	 * Adds a new vertex at the specified position with name set to it number
	 * and default values for other parameters.
	 * It is successful only if there is no vertex near that position.
	 */
    public void doAddVertex(int x, int y) {
        if (currVertex == -1) {
            addUndo();
            currVertex = currGraph.addVertex(new VertexAttr("", x, y, 18, 18, VertexAttr.SHAPE_CIRCLE, Color.BLACK, VertexAttr.FILLCOLOR_DEFAULT));
            try {
                VertexAttr va = currGraph.getVertex(currVertex);
                va.setName(new Integer(currVertex).toString());
                currGraph.setVertex(currVertex, va);
            } catch (IllegalVertexException e) {
            }
            repaint();
        }
    }

    /**
	 * Removes the current vertex.
	 */
    public void doRemoveVertex() {
        if (currVertex != -1) {
            try {
                addUndo();
                currGraph.removeVertex(currVertex);
                currVertex = -1;
                repaint();
            } catch (IllegalVertexException e) {
                System.out.println("Check error in GPanel.doRemoveVertex");
            }
        }
    }

    public void doRemoveEdge() {
        int v1, v2;
        try {
            if (prevVertex != -1 && currVertex != -1) {
                v1 = prevVertex;
                v2 = currVertex;
                if (currGraph.edgeExists(v1, v2) == true) {
                    addUndo();
                    currGraph.removeEdge(v1, v2);
                    currVertex = v1;
                    repaint();
                }
            }
        } catch (IllegalVertexException e) {
            System.out.println("Check error in GPanel.doRemoveEdge");
        }
    }

    public void doAddEdge() {
        int v1, v2;
        try {
            if (prevVertex != -1 && currVertex != -1 && prevVertex != currVertex) {
                v1 = prevVertex;
                v2 = currVertex;
                if (currGraph.edgeExists(v1, v2) == false) {
                    addUndo();
                    currGraph.addEdge(v1, v2, new EdgeAttr());
                    currVertex = v1;
                    repaint();
                }
            }
        } catch (IllegalVertexException e) {
            System.out.println("Check error in GPanel.doAddEdge");
        }
    }

    /**
	 * Executes Hamilton Tour algorithm on the current graph.
	 */
    public void doHamiltonTour() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<String> as = new Vector<String>();
                Vector<Integer> t = new Vector<Integer>();
                String tStr = new String();
                boolean retValue;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (!GraphAlg.isConnected(currGraph)) {
                        JOptionPane.showMessageDialog(null, "Hamilton Tour doesnot exists for unconnected graphs");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    retValue = GraphAlg.HamiltonTour(currGraph, t, as, true);
                    if (retValue) {
                        for (int i = 0; i < t.size(); i++) tStr += currGraph.getVertex(t.get(i)).getName() + ",";
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Hamilton tour:" + tStr);
                        saveForRefresh();
                        Animator an = new Animator(curr, as);
                        an.setDelayPeriod(animationDelay);
                        an.start();
                        an.join();
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    } else {
                        JOptionPane.showMessageDialog(null, "Unable to find hamilton tour");
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    }
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doHamiltonTour");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doHamiltonTour");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doHamiltonTour");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Travelling salesman problem algorithm on the current graph.
	 */
    public void doTravellingSalesmanProblem() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<String> as = new Vector<String>();
                Vector<Integer> t = new Vector<Integer>();
                String tStr = new String();
                int v;
                boolean retValue;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (!GraphAlg.isConnected(currGraph)) {
                        JOptionPane.showMessageDialog(null, "TSP tour doesnot exists for unconnected graphs");
                        return;
                    }
                    v = vertexInputDialog("Enter the starting vertex");
                    if (v == -1) return;
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    retValue = GraphAlg.TravellingSalesmanProblem(currGraph, v, t, as, true);
                    if (retValue) {
                        for (int i = 0; i < t.size(); i++) tStr += currGraph.getVertex(t.get(i)).getName() + ",";
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "TSP tour: " + tStr);
                        saveForRefresh();
                        Animator an = new Animator(curr, as);
                        an.setDelayPeriod(animationDelay);
                        an.start();
                        an.join();
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    } else {
                        JOptionPane.showMessageDialog(null, "Unable to find TSP tour");
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    }
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doTravellingSalesmanProblem");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doTravellingSalesmanProblem");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doTravellingSalesmanProblem");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Graph Coloring algorithm on the current graph.
	 */
    public void doGraphColoring() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<String> as = new Vector<String>();
                int coloring[] = new int[currGraph.numVertices()];
                String coloringStr = new String();
                int maxColors;
                String maxColorsStr;
                boolean retValue;
                int oldAction = getCurrentAction();
                Color GCColorTable[];
                Random randomGen = new Random();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (currGraph.isDirected()) {
                        JOptionPane.showMessageDialog(null, "Graph coloring doesnot make sense with directed graphs");
                        return;
                    }
                    maxColorsStr = JOptionPane.showInputDialog(null, "Maximum number of colors to use");
                    maxColors = Integer.parseInt(maxColorsStr);
                    GCColorTable = new Color[maxColors];
                    for (int mi = 0; mi < maxColors; mi++) {
                        GCColorTable[mi] = new Color(randomGen.nextInt(256), randomGen.nextInt(256), randomGen.nextInt(256));
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    retValue = GraphAlg.GraphColoring(currGraph, maxColors, coloring, as, true);
                    if (retValue) {
                        for (int i = 0; i < currGraph.numVertices(); i++) coloringStr += currGraph.getVertex(i).getName() + "-" + coloring[i] + " ";
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Coloring: " + coloringStr);
                        saveForRefresh();
                        for (int i = 0; i < currGraph.numVertices(); i++) {
                            VertexAttr va = currGraph.getVertex(i);
                            va.setColor(GCColorTable[coloring[i]]);
                            currGraph.setVertex(i, va);
                        }
                        repaint();
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    } else {
                        JOptionPane.showMessageDialog(null, "Unable to find coloring");
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    }
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doGraphColoring");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doGraphColoring");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Critical path analysis on the current graph.
	 */
    public void doCriticalPathAnalysis() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Integer> cpath = new Vector<Integer>();
                int est[] = new int[currGraph.numVertices()];
                int lst[] = new int[currGraph.numVertices()];
                String cpathStr = new String();
                String estStr = new String();
                String lstStr = new String();
                Vector<String> as = new Vector<String>();
                int s, t;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (currGraph.isDirected() == false || GraphAlg.isAcyclic(currGraph) == false) {
                        JOptionPane.showMessageDialog(null, "Critical path can run on dags only");
                        return;
                    }
                    s = vertexInputDialog("Enter the source vertex");
                    if (s == -1) return;
                    t = vertexInputDialog("Enter the destination vertex");
                    if (t == -1) return;
                    if (s == t) {
                        JOptionPane.showMessageDialog(null, "Source and destination should be different");
                        return;
                    }
                    if (currGraph.inDegree(s) != 0) {
                        JOptionPane.showMessageDialog(null, "Invalid source");
                        return;
                    }
                    if (currGraph.outDegree(t) != 0) {
                        JOptionPane.showMessageDialog(null, "Invalid destination");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.CriticalPathAnalysis(currGraph, s, t, cpath, est, lst, as, true);
                    for (int i = 0; i < cpath.size(); i++) {
                        cpathStr += currGraph.getVertex(cpath.get(i)).getName();
                    }
                    for (int i = 0; i < currGraph.numVertices(); i++) {
                        estStr += est[i] + " ";
                        lstStr += lst[i] + " ";
                    }
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Critical path: " + cpathStr + "EST: " + estStr + "LST: " + lstStr);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doCriticalPathAnalysis");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doCriticalPathAnalysis");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Maximum flow algorithm on the current graph.
	 */
    public void doMaximumFlow() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                int flow[][] = new int[currGraph.numVertices()][currGraph.numVertices()];
                Vector<String> as = new Vector<String>();
                int s, t;
                int i, j;
                int oldAction = getCurrentAction();
                try {
                    if (currGraph.isDirected() == false) {
                        JOptionPane.showMessageDialog(null, "Maximum flow can run on directed graphs only");
                        return;
                    }
                    s = vertexInputDialog("Enter the source vertex");
                    if (s == -1) return;
                    t = vertexInputDialog("Enter the sink vertex");
                    if (t == -1) return;
                    if (s == t) {
                        JOptionPane.showMessageDialog(null, "Source and destination should be different");
                        return;
                    }
                    if (currGraph.inDegree(s) != 0) {
                        JOptionPane.showMessageDialog(null, "Invalid source");
                        return;
                    }
                    if (currGraph.outDegree(t) != 0) {
                        JOptionPane.showMessageDialog(null, "Invalid destination");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.MaximumFlow(currGraph, s, t, flow, as, true);
                    saveForRefresh();
                    for (i = 0; i < currGraph.numVertices(); i++) for (j = 0; j < currGraph.numVertices(); j++) if (currGraph.edgeExists(i, j)) {
                        EdgeAttr ea = currGraph.getEdge(i, j);
                        ea.setWeight(flow[i][j]);
                        currGraph.setEdge(i, j, ea);
                    }
                    repaint();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doMaximumFlow");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doMaximumFlow");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Longest Path algorithm on the current graph.
	 */
    public void doLongestPath() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Integer> path = new Vector<Integer>();
                Vector<String> as = new Vector<String>();
                String pathStr = new String();
                int s, t;
                int oldAction = getCurrentAction();
                boolean retValue;
                if (currGraph.numVertices() == 0) return;
                try {
                    if (GraphAlg.isAcyclic(currGraph) == false || currGraph.isDirected() == false) {
                        JOptionPane.showMessageDialog(null, "LongestPath can run on directed acyclic graphs only");
                        return;
                    }
                    s = vertexInputDialog("Enter source vertex");
                    if (s == -1) return;
                    t = vertexInputDialog("Enter destination vertex");
                    if (t == -1) return;
                    if (s == t) {
                        JOptionPane.showMessageDialog(null, "Source and destination should be different");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    retValue = GraphAlg.LongestPath(currGraph, s, t, path, as, true);
                    if (retValue) {
                        for (int i = 0; i < path.size(); i++) pathStr += currGraph.getVertex(path.get(i)).getName() + ",";
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Path: " + pathStr);
                        saveForRefresh();
                        Animator an = new Animator(curr, as);
                        an.setDelayPeriod(animationDelay);
                        an.start();
                        an.join();
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    } else {
                        JOptionPane.showMessageDialog(null, "Unable to find longest path.");
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    }
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doLongestPath");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doLongestPath");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doLongestPath");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Euler tour algorithm on the current graph.
	 */
    public void doEulerTour() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<String> as = new Vector<String>();
                Vector<Integer> t = new Vector<Integer>();
                String tStr = new String();
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (currGraph.isDirected() == true || GraphAlg.isConnected(currGraph) == false) {
                        JOptionPane.showMessageDialog(null, "Euler Tour exists for connected undirected graphs only");
                        return;
                    }
                    for (int i2 = 0; i2 < currGraph.numVertices(); i2++) if (currGraph.inDegree(i2) % 2 == 1) {
                        JOptionPane.showMessageDialog(null, "EulerTour doesnot exist in graph with odd degree vertices");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.EulerTour(currGraph, t, as, true);
                    for (int i = 0; i < t.size(); i++) tStr += currGraph.getVertex(t.get(i)).getName() + ",";
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Euler tour: " + tStr);
                    saveForRefresh();
                    Animator an = new Animator(curr, as);
                    an.setDelayPeriod(animationDelay);
                    an.start();
                    an.join();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doEulerTour");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doEulerTour");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doEulerTour");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes Topological sort algorithm on the current graph.
	 */
    public void doTopologicalSort() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<String> as = new Vector<String>();
                Vector<Integer> sl = new Vector<Integer>();
                String slStr = new String();
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (currGraph.isDirected() == false || GraphAlg.isAcyclic(currGraph) == false) {
                        JOptionPane.showMessageDialog(null, "Topological sort can run on dags only");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.TopologicalSort(currGraph, sl, as, true);
                    for (int i = 0; i < sl.size(); i++) slStr += currGraph.getVertex(sl.get(i)).getName() + ",";
                    slStr = new String((((new StringBuffer(slStr)).reverse()).deleteCharAt(0)).reverse());
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Sorted List: " + slStr);
                    saveForRefresh();
                    Animator an = new Animator(curr, as);
                    an.setDelayPeriod(animationDelay);
                    an.start();
                    an.join();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doTopologicalSort");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doTopologicalSort");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doTopologicalSort");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes PrimMST algorithm on the current graph.
	 */
    public void doPrimMST() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Edge> treeEdges = new Vector<Edge>();
                Vector<String> as = new Vector<String>();
                String treeEdgesStr = new String();
                int v;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (currGraph.isDirected()) {
                        JOptionPane.showMessageDialog(null, "Prim's algorithm cannot work on directed graphs.");
                        return;
                    }
                    v = vertexInputDialog("Enter the starting vertex");
                    if (v == -1) return;
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.PrimMST(currGraph, v, treeEdges, as, true);
                    for (int i = 0; i < treeEdges.size(); i++) treeEdgesStr += "(" + currGraph.getVertex(treeEdges.get(i).getFrom()).getName() + "," + currGraph.getVertex(treeEdges.get(i).getTo()).getName() + ")" + " ";
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "MST: " + treeEdgesStr);
                    saveForRefresh();
                    Animator an = new Animator(curr, as);
                    an.setDelayPeriod(animationDelay);
                    an.start();
                    an.join();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doPrimMST");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doPrimMST");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doPrimMST");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes KruskalMST algorithm on the current graph.
	 */
    public void doKruskalMST() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Edge> treeEdges = new Vector<Edge>();
                Vector<String> as = new Vector<String>();
                String treeEdgesStr = new String();
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    if (currGraph.isDirected()) {
                        JOptionPane.showMessageDialog(null, "Kruskal's algorithm cannot work on directed graphs.");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.KruskalMST(currGraph, treeEdges, as, true);
                    for (int i = 0; i < treeEdges.size(); i++) treeEdgesStr += "(" + currGraph.getVertex(treeEdges.get(i).getFrom()).getName() + "," + currGraph.getVertex(treeEdges.get(i).getTo()).getName() + ")" + " ";
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "MST: " + treeEdgesStr);
                    saveForRefresh();
                    Animator an = new Animator(curr, as);
                    an.setDelayPeriod(animationDelay);
                    an.start();
                    an.join();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doKrukalMST");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doKrukalMST");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doKrukalMST");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes DijkstraShortestPath algorithm on the current graph.
	 */
    public void doDijkstraShortestPath() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Integer> path = new Vector<Integer>();
                Vector<String> as = new Vector<String>();
                String pathStr = new String();
                int s, t;
                boolean retValue;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    s = vertexInputDialog("Enter source vertex");
                    if (s == -1) return;
                    t = vertexInputDialog("Enter destination vertex");
                    if (t == -1) return;
                    if (s == t) {
                        JOptionPane.showMessageDialog(null, "Source and destination should be different");
                        return;
                    }
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    retValue = GraphAlg.DijkstraShortestPath(currGraph, s, t, path, as, true);
                    if (retValue) {
                        int i;
                        for (i = 0; i < path.size() - 1; i++) pathStr += currGraph.getVertex(path.get(i)).getName() + ",";
                        pathStr += currGraph.getVertex(path.get(i)).getName();
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Path: " + pathStr);
                        saveForRefresh();
                        Animator an = new Animator(curr, as);
                        an.setDelayPeriod(animationDelay);
                        an.start();
                        an.join();
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    } else {
                        JOptionPane.showMessageDialog(null, "Unable to find shortest path.");
                        gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                    }
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.DijkstraShortestPath");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.DijkstraShortestPath");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.DijkstraShortestPath");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes DFS algorithm on the current graph.
	 */
    public void doDFS() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Integer> preOrder = new Vector<Integer>();
                Vector<Integer> postOrder = new Vector<Integer>();
                Vector<String> as = new Vector<String>();
                String preOrderStr = new String();
                String postOrderStr = new String();
                int v;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    v = vertexInputDialog("Enter the starting vertex");
                    if (v == -1) return;
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.DFS(currGraph, v, preOrder, postOrder, as, true);
                    for (int i = 0; i < preOrder.size(); i++) {
                        preOrderStr += currGraph.getVertex(preOrder.get(i)).getName() + ",";
                        postOrderStr += currGraph.getVertex(postOrder.get(i)).getName() + ",";
                    }
                    preOrderStr = new String((((new StringBuffer(preOrderStr)).reverse()).deleteCharAt(0)).reverse());
                    postOrderStr = new String((((new StringBuffer(postOrderStr)).reverse()).deleteCharAt(0)).reverse());
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Preorder: " + preOrderStr + " Postorder: " + postOrderStr);
                    saveForRefresh();
                    Animator an = new Animator(curr, as);
                    an.setDelayPeriod(animationDelay);
                    an.start();
                    an.join();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doDFS");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doDFS");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doDFS");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Executes BFS algorithm on the current graph.
	 */
    public void doBFS() {
        final GPanel curr = this;
        (new Thread() {

            public void run() {
                Vector<Integer> levelOrder = new Vector<Integer>();
                String levelOrderStr = new String();
                Vector<String> as = new Vector<String>();
                int v;
                int oldAction = getCurrentAction();
                if (currGraph.numVertices() == 0) return;
                try {
                    v = vertexInputDialog("Enter the starting vertex");
                    if (v == -1) return;
                    setCurrentAction(ACTION_RUN);
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Running");
                    GraphAlg.BFS(currGraph, v, levelOrder, as, true);
                    for (int i = 0; i < levelOrder.size(); i++) levelOrderStr += currGraph.getVertex(levelOrder.get(i)).getName() + ",";
                    levelOrderStr = new String((((new StringBuffer(levelOrderStr)).reverse()).deleteCharAt(0)).reverse());
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_OUT, "Levelorder: " + levelOrderStr);
                    saveForRefresh();
                    Animator an = new Animator(curr, as);
                    an.setDelayPeriod(animationDelay);
                    an.start();
                    an.join();
                    gFrame.statusBar.setMessage(GStatusBar.STATUS_ALGORITHM_EXE, "Done");
                } catch (GraphAlgException e) {
                    System.out.println("Check error in GPanel.doBFS");
                } catch (IllegalVertexException e) {
                    System.out.println("Check error in GPanel.doBFS");
                } catch (InterruptedException e) {
                    System.out.println("Check error in GPanel.doBFS");
                }
                forceCurrentAction(oldAction);
            }
        }).start();
    }

    /**
	 * Show a dialog for reading vertex input.
	 * @param prompt The prompt message for the input.
	 * @return The vertex number selected, or -1 if cancelled the dialog.
	 */
    public int vertexInputDialog(String prompt) {
        int initialVertex = (currVertex != -1 ? currVertex : 0);
        String[] possibleVertexNames = currGraph.getColNames();
        String returnedVertexName = new String();
        try {
            returnedVertexName = (String) JOptionPane.showInputDialog(null, prompt, "Vertex Input", JOptionPane.QUESTION_MESSAGE, null, possibleVertexNames, possibleVertexNames[initialVertex]);
            if (returnedVertexName == null) return -1; else {
                for (int i = 0; i < currGraph.numVertices(); i++) if (returnedVertexName.equals(possibleVertexNames[i])) return i;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public void disableEvents() {
        removeMouseListener(gpHandler);
        removeMouseMotionListener(gpHandler);
    }

    public void enableEvents() {
        if (this.getMouseListeners().length == 0) {
            addMouseListener(gpHandler);
            addMouseMotionListener(gpHandler);
        }
    }
}
