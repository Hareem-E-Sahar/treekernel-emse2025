package Gui;

import static controller.Globals.TRIANGLE_WINDOW_HEIGHT;
import static controller.Globals.TRIANGLE_WINDOW_WIDTH;
import static controller.Globals.VERTEX_SIZE_HIGHLIGHTED;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import Triangulation.ConvexHullEdgeGenerator;
import Triangulation.EdgeGenerator;
import Triangulation.FastLMTTriangulator;
import Triangulation.LMTTriangulator;
import Triangulation.minimalityMetrics.ShortestEdgesMetric;
import controller.Globals;
import static controller.Globals.*;
import controller.Notification;
import controller.NotificationCenter;
import controller.NotificationObserver;
import datatypes.Circle;
import datatypes.Edge;
import datatypes.Vertex;

public class TriangleDisplay extends JPanel implements MouseListener, MouseMotionListener, KeyListener, Scrollable, NotificationObserver {

    private static final long serialVersionUID = -8006175897441108518L;

    MainWindow mainWindow;

    public List<Circle> circles = new ArrayList<Circle>();

    private boolean showCircles = false;

    private List<Edge> edges = null;

    private Object edgesLock = new Object();

    private Vector<Vertex> vertices = new Vector<Vertex>();

    private Set<Vertex> selected = new HashSet<Vertex>();

    private Set<Vertex> pasteBuffer = new HashSet<Vertex>();

    private Vertex vertexDisplayingCoordinates = null;

    private double zoom = 1.0;

    private Boolean computing = false;

    private Object computingLock = new Object();

    private Boolean needsComputing = true;

    private boolean wantsPolygonTriangulation = true;

    private EdgeGenerator triangulator;

    private boolean selectionDrag = false;

    private Vertex clickPoint;

    private Vertex dragPoint;

    private AffineTransform transform = new AffineTransform();

    private int windowWidth = TRIANGLE_WINDOW_WIDTH;

    private int windowHeight = TRIANGLE_WINDOW_HEIGHT;

    public TriangleDisplay(MainWindow main) {
        super();
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        NotificationCenter nc = NotificationCenter.defaultNotificationCenter();
        nc.addObserver(Notification.didFindEdgeIn, this);
        nc.addObserver(Notification.didFinishMWT, this);
        nc.addObserver(Notification.didTriangulatePolygon, this);
        nc.addObserver(Notification.didTerminateTriangulator, this);
        nc.addObserver(Notification.didFindCircles, this);
        mainWindow = main;
        this.setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.setBackground(Color.white);
    }

    public void setZoom(double z) {
        zoom = z;
        if (z > 1.0) setPreferredSize(new Dimension((int) (z * windowWidth), (int) (z * windowHeight)));
        repaint();
        doLayout();
        mainWindow.doLayout();
        revalidate();
    }

    public void spawnRandomPoints(int numPoints) {
        synchronized (vertices) {
            vertices.clear();
            if (edges != null) {
                edges.clear();
            }
            selected.clear();
            List<Vertex> genVer = tools.RandomVerticesGenerator.randomVertices(windowWidth, windowHeight, 5.0, numPoints);
            while (genVer == null) {
                System.out.println("Increasing canvas");
                windowWidth += windowWidth * 0.1;
                windowHeight += windowHeight * 0.1;
                System.out.println("New Dimensions: (" + windowWidth + ", " + windowHeight + ")\n");
                genVer = tools.RandomVerticesGenerator.randomVertices(windowWidth, windowHeight, 5.0, numPoints);
            }
            vertices.addAll(genVer);
            System.out.println(vertices.size());
            triangulate(true);
        }
    }

    public void spawnLoadPoints(String state) {
        synchronized (vertices) {
            vertices.clear();
            selected.clear();
            if (edges != null) {
                edges.clear();
            }
            try {
                vertices.addAll(tools.OpenSaveVertices.loadVertices(state));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(vertices.size());
            triangulate(true);
        }
    }

    public void spawnSavePoints(String state) {
        synchronized (vertices) {
            Vector<Vertex> vertsToSave = new Vector<Vertex>();
            for (int i = 0; i < vertices.size(); i++) vertsToSave.addElement(vertices.get(i));
            try {
                tools.OpenSaveVertices.saveVertices(vertsToSave, state);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void redraw() {
        this.repaint();
    }

    Vector<Vertex> getVertices() {
        return vertices;
    }

    public int getViewportWidth() {
        return this.getWidth();
    }

    public int getViewportHeight() {
        return this.getHeight();
    }

    public void showMesh(List<Edge> edges) {
        this.edges = edges;
        updateUI();
    }

    public void reset() {
        this.edges = null;
        this.vertices.clear();
        this.selected.clear();
        this.circles.clear();
        updateUI();
    }

    public void triangulate(boolean triangulatePolygons) {
        synchronized (computingLock) {
            needsComputing = true;
            wantsPolygonTriangulation = triangulatePolygons;
        }
        processChanges();
    }

    public void processChanges() {
        updateUI();
        if (vertices.size() < 2) {
            edges = null;
            return;
        }
        synchronized (computingLock) {
            if (!needsComputing) {
                return;
            }
            if (computing) {
                needsComputing = true;
                return;
            }
            this.circles.clear();
            computing = true;
            needsComputing = false;
        }
        if (vertices.size() < 4) {
            Vertex[] aVertices = new Vertex[vertices.size()];
            vertices.toArray(aVertices);
            edges = (new ConvexHullEdgeGenerator()).processVertices(aVertices, new ShortestEdgesMetric());
            return;
        }
        NotificationCenter nc = NotificationCenter.defaultNotificationCenter();
        nc.postNotification(Notification.willStartProcessing, this);
        ExecutorService executor = Globals.mainExecutor;
        executor.submit(new Callable<List<Edge>>() {

            public List<Edge> call() throws Exception {
                Vertex[] aVertices = new Vertex[vertices.size()];
                vertices.toArray(aVertices);
                triangulator = mainWindow.getActiveTriangulator();
                List<Edge> mwt;
                if (triangulator instanceof LMTTriangulator) {
                    LMTTriangulator t = (LMTTriangulator) triangulator;
                    mwt = t.processVertices(aVertices, mainWindow.getActiveMetric(), wantsPolygonTriangulation);
                } else {
                    mwt = triangulator.processVertices(aVertices, mainWindow.getActiveMetric());
                }
                synchronized (computingLock) {
                    computing = false;
                }
                return mwt;
            }
        });
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        AffineTransform savedTransform = g.getTransform();
        AffineTransform toCenterTransform = new AffineTransform();
        transform.setToScale(zoom, zoom);
        toCenterTransform.concatenate(transform);
        g.transform(toCenterTransform);
        int width = getWidth();
        int height = getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        if (showCircles && circles != null) {
            for (int i = 0; i < circles.size(); i++) {
                Circle circle = circles.get(i);
                circle.draw(g, this);
            }
        }
        if (edges != null) {
            List<Edge> edgesToDraw = this.edges;
            for (int i = 0; i < edgesToDraw.size(); i++) {
                Edge edge = edgesToDraw.get(i);
                if (edge != null) {
                    edge.draw(g, this);
                }
            }
        }
        if (vertices != null) {
            g.setColor(Color.black);
            for (Vertex v : vertices) {
                v.draw(g, this);
            }
        }
        if (selectionDrag) {
            g.setColor(Color.green.darker());
            float dash[] = { 5.0f };
            g.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            int w = (int) (dragPoint.getX() - clickPoint.getX());
            int h = (int) (dragPoint.getY() - clickPoint.getY());
            int x, y;
            if (w < 0) {
                w = -w;
                x = (int) dragPoint.getX();
            } else {
                x = (int) clickPoint.getX();
            }
            if (h < 0) {
                h = -h;
                y = (int) dragPoint.getY();
            } else {
                y = (int) clickPoint.getY();
            }
            g.drawRect(x, y, w, h);
        }
        g.setTransform(savedTransform);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        requestFocusInWindow();
        VERTEX_SIZE_STANDARD = 3;
        redraw();
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        requestFocusInWindow(false);
        VERTEX_SIZE_STANDARD = 1;
        redraw();
    }

    private Vertex location(MouseEvent event) {
        clickPoint = new Vertex((double) event.getX(), (double) event.getY());
        try {
            clickPoint.applyTransform(transform.createInverse());
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return clickPoint;
    }

    public void deselectVertices() {
        for (Vertex previouslySelected : selected) {
            previouslySelected.selected = false;
        }
        selected.clear();
    }

    private void select(Vertex vertex) {
        vertex.selected = true;
        selected.add(vertex);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        clickPoint = location(event);
        dragPoint = clickPoint.clone();
        if (event.isAltDown()) {
            selectionDrag = true;
            return;
        }
        if (!event.isShiftDown()) {
            deselectVertices();
        }
        boolean addVertex = true;
        for (Vertex tested : vertices) {
            boolean hit = tested.isInCircle(clickPoint, VERTEX_SIZE_HIGHLIGHTED);
            if (hit) {
                addVertex = false;
                select(tested);
                break;
            }
        }
        if (addVertex) {
            vertices.add(clickPoint.clone());
            triangulate(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        if (selectionDrag) {
            selectionDrag = false;
            int minX = (int) Math.min(clickPoint.getX(), dragPoint.getX());
            int maxX = (int) Math.max(clickPoint.getX(), dragPoint.getX());
            int minY = (int) Math.min(clickPoint.getY(), dragPoint.getY());
            int maxY = (int) Math.max(clickPoint.getY(), dragPoint.getY());
            int x, y;
            for (Vertex vertex : vertices) {
                x = (int) vertex.getX();
                y = (int) vertex.getY();
                if (x > minX && x < maxX && y > minY && y < maxY) {
                    select(vertex);
                }
            }
            redraw();
            return;
        }
        synchronized (computingLock) {
            computing = false;
        }
        if (triangulator != null && triangulator instanceof FastLMTTriangulator) {
            ((FastLMTTriangulator) triangulator).abort();
        }
        triangulate(true);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (event.getX() >= 0 && event.getY() >= 0) {
            Vertex delta = new Vertex((double) event.getX() / zoom, (double) event.getY() / zoom);
            delta.minus(dragPoint);
            dragPoint.plus(delta);
            if (selectionDrag) {
            } else {
                for (Vertex sel : selected) {
                    sel.plus(delta);
                    sel.round();
                }
                if (event.getX() > getWidth()) {
                    this.setPreferredSize(new Dimension(event.getX() + 10, getHeight()));
                }
                if (event.getY() > getHeight()) {
                    this.setPreferredSize(new Dimension(getWidth(), event.getY() + 10));
                }
                if (vertices.size() < MAXIMUM_SIZE_FOR_INTERACTIVE_MODE) {
                    triangulate(false);
                }
            }
            repaint();
        }
    }

    private void displayCoordinates(Vertex vertex) {
        if (vertexDisplayingCoordinates != null) {
            vertexDisplayingCoordinates.displayCoordinates = false;
        }
        if (vertex != null) {
            vertex.displayCoordinates = true;
        }
        vertexDisplayingCoordinates = vertex;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        clickPoint = location(event);
        for (Vertex tested : vertices) {
            boolean hit = tested.isInCircle(clickPoint, VERTEX_SIZE_HIGHLIGHTED);
            if (hit) {
                if (event.isShiftDown()) {
                    tested.selected = true;
                    selected.add(tested);
                }
                displayCoordinates(tested);
                break;
            } else {
                displayCoordinates(null);
            }
        }
        repaint();
    }

    /**
     * Toggles whether beta-skeleton circles are shown or not.
     * @return true if circles are shown, false otherwise.
     */
    public boolean toggleShowCircles() {
        System.out.println("toggling circles");
        if (showCircles) {
            showCircles = false;
        } else {
            showCircles = true;
        }
        redraw();
        return showCircles;
    }

    /**
     * Clears the current paste buffer, then copies the selected vertices to the paste buffer.
     */
    public void copySelectedVertices() {
        pasteBuffer.clear();
        pasteBuffer.addAll(selected);
    }

    /**
     * Deletes the selected vertices, then re-triangulates the remaining set.
     */
    public void deleteSelectedVertices() {
        for (Vertex sel : selected) {
            vertices.remove(sel);
        }
        int numSelected = selected.size();
        selected.clear();
        if (numSelected > 0) {
            triangulate(true);
        }
    }

    /**
     * Pastes the vertices from the paste buffer with an offset.
     * Pasted vertices will be selected, previous selection will be cleared.
     * Set will be re-triangulated.
     * @return true if vertices have been pasted. false if the paste buffer was empty.
     */
    public boolean pasteVertices() {
        if (pasteBuffer.size() > 0) {
            deselectVertices();
            Set<Vertex> newPasteBuffer = new HashSet<Vertex>();
            Vertex offset = new Vertex(10.0, 10.0);
            for (Vertex vertex : pasteBuffer) {
                Vertex copy = vertex.clone();
                copy.translate(offset);
                newPasteBuffer.add(copy);
            }
            vertices.addAll(newPasteBuffer);
            pasteBuffer = newPasteBuffer;
            for (Vertex vertex : newPasteBuffer) {
                select(vertex);
            }
            triangulate(true);
            return true;
        }
        return false;
    }

    /**
     * Mirrors the selected vertices on a horizontal line centered on the selection.
     * Set will be re-triangulated.
     */
    public void mirrorSelectedVerticesHorizontally() {
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        double y;
        for (Vertex vertex : selected) {
            y = vertex.getY();
            max = Math.max(max, y);
            min = Math.min(min, y);
        }
        double center = min + (max - min) / 2;
        for (Vertex vertex : selected) {
            y = vertex.getY();
            double offset = (center - y) * 2;
            vertex.setY(y + offset);
        }
        triangulate(true);
    }

    /**
     * Mirrors the selected vertices on a vertical line centered on the selection.
     * Set will be re-triangulated.
     */
    public void mirrorSelectedVerticesVertically() {
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        double x;
        for (Vertex vertex : selected) {
            x = vertex.getX();
            max = Math.max(max, x);
            min = Math.min(min, x);
        }
        double center = min + (max - min) / 2;
        for (Vertex vertex : selected) {
            x = vertex.getX();
            double offset = (center - x) * 2;
            vertex.setX(x + offset);
        }
        triangulate(true);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedVertices();
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            deselectVertices();
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_C) {
            System.out.println("not 'C'");
            if (event.isShiftDown() && event.isControlDown()) {
                toggleShowCircles();
            } else {
                copySelectedVertices();
            }
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_V) {
            pasteVertices();
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_UP) {
            if (event.isShiftDown()) {
                mirrorSelectedVerticesHorizontally();
            }
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            if (event.isShiftDown()) {
                mirrorSelectedVerticesVertically();
            }
            return;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(10, 10);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
        return 5;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
        return 5;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void receiveNotification(Notification notification) {
        if (notification.name == Notification.didFindEdgeIn) {
            if (!(notification.sender instanceof List<?>)) {
                return;
            }
            synchronized (this) {
                edges = (List<Edge>) notification.sender;
            }
        }
        if (notification.name == Notification.didFinishMWT) {
            this.redraw();
            mainWindow.getBottomPanel().updateProgress("", 100);
            if (!(notification.sender instanceof List<?>)) {
                return;
            }
            edges = (List<Edge>) notification.sender;
            this.computing = false;
            this.processChanges();
        }
        if (notification.name == Notification.didTriangulatePolygon) {
            if (!(notification.sender instanceof List<?>)) {
                return;
            }
            synchronized (edgesLock) {
                edges.addAll(0, (List<Edge>) notification.sender);
                this.redraw();
            }
        }
        if (notification.name == Notification.didFindCircles) {
            circles = (List<Circle>) notification.sender;
        }
    }
}
