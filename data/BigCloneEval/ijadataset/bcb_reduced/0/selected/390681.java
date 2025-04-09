package ppsim.types;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;
import ppsim.client.*;
import ppsim.client.test.*;
import ppsim.corba.*;
import ppsim.exceptions.*;
import ppsim.server.core.*;
import ppsim.server.types.*;
import ppsim.utils.*;
import java.util.*;
import java.util.List;

/** This class serves for viewing the Production Process. */
public class ProcessImage extends JComponent implements MouseInputListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = -5899262063112336972L;

    /** The production process */
    private ProdProcess process = new ProdProcess();

    /** An example process */
    private AProcess aProcess = null;

    /** The scale for the view. */
    private double scale = 1;

    /** The snap for the view. */
    private double snap = 1;

    /** The graphics context. */
    private Graphics2D g2;

    /** The origin (the left bottom corner) of the process image (objects space). */
    private Point2D.Double origin = new Point2D.Double(0, 0);

    /** The mid point of the process image (objects space). */
    private Point2D.Double midPoint = new Point2D.Double(400, 250);

    /** The lowest X coordinate on OX axe (objects space). */
    private double lowX = 0;

    /** The highest X coordinate on OX axe (objects space). */
    private double highX = 800;

    /** The lowest Y coordinate on OY axe (objects space). */
    private double lowY = 0;

    /** The highest Y coordinate on OY axe (objects space). */
    private double highY = 500;

    /** The container for the production objects. */
    private List imgObjects = new ArrayList();

    /** The process console. */
    private ProcessConsole procConsole;

    /** The start point of dynamic zoom. */
    private Point startPointZoom;

    /** The end point of dynamic zoom. */
    private Point endPointZoom;

    private ProcessPersister pp = new ProcessPersister();

    /** The constructor. */
    public ProcessImage(ProcessConsole procConsole) {
        this.procConsole = procConsole;
        setBackground(Color.white);
        setForeground(Color.black);
        addMouseListener(this);
        addMouseMotionListener(this);
        scale = 1;
    }

    /** Paints the view. */
    public void paint(Graphics g) {
        g2 = (Graphics2D) g;
        g2.setPaint(Color.black);
        for (int i = 0; i < imgObjects.size(); i++) {
            ((IObjectImage) imgObjects.get(i)).draw();
        }
        if (!procConsole.getZoomDynState() || startPointZoom == null || endPointZoom == null) {
            return;
        }
        g2.draw(new Rectangle2D.Double(startPointZoom.x, startPointZoom.y, endPointZoom.x - startPointZoom.x, endPointZoom.y - startPointZoom.y));
    }

    /** Make screen coordonates for the point. */
    public Point2D.Double makeScreenCoords(Point2D.Double point) {
        Point2D.Double p = new Point2D.Double(0, 0);
        p.x = (point.x - origin.x + midPoint.x) * scale;
        p.y = -(point.y + origin.y - midPoint.y) * scale;
        return p;
    }

    /** Make space coordinates for the point. */
    public Point2D.Double makeSpaceCoords(Point point) {
        Point2D.Double p = new Point2D.Double(0, 0);
        p.x = point.x / scale - midPoint.x + origin.x;
        p.y = -(point.y / scale - midPoint.y + origin.y);
        p.x = ((int) (p.x / snap)) * snap;
        p.y = ((int) (p.y / snap)) * snap;
        return p;
    }

    /** Compute the mid point and the extreme coordinates of the process image. */
    private void compMidPoint() {
        if (imgObjects.size() > 0) {
            IObjectImage pObject = (IObjectImage) imgObjects.get(0);
            lowX = pObject.getOrgX() - pObject.getWidthX() / 2;
            highX = pObject.getOrgX() + pObject.getWidthX() / 2;
            lowY = pObject.getOrgY() - pObject.getWidthY() / 2;
            highY = pObject.getOrgY() + pObject.getWidthY() / 2;
        }
        if (imgObjects.size() > 1) for (int i = 0; i < imgObjects.size(); i++) {
            IObjectImage pObject = (IObjectImage) imgObjects.get(i);
            double lowXo = pObject.getOrgX() - pObject.getWidthX() / 2;
            double highXo = pObject.getOrgX() + pObject.getWidthX() / 2;
            double lowYo = pObject.getOrgY() - pObject.getWidthY() / 2;
            double highYo = pObject.getOrgY() + pObject.getWidthY() / 2;
            if (lowXo < lowX) lowX = lowXo;
            if (highXo > highX) highX = highXo;
            if (lowYo < lowY) lowY = lowYo;
            if (highYo > highY) highY = highYo;
        }
        midPoint.x = (lowX + highX) / 2;
        midPoint.y = (lowY + highY) / 2;
    }

    /** Compute the origin of the process image. */
    private void compOrigin() {
        origin.x = 2 * midPoint.x - getSize().width / scale / 2;
        origin.y = -getSize().height / scale / 2;
    }

    /** Zoom all. */
    public void zoomAll() {
        compMidPoint();
        double hView = highY - lowY;
        double wView = highX - lowX;
        double scaleY = getSize().height / hView;
        double scaleX = getSize().width / wView;
        if (scaleX < scaleY) scale = scaleX * 0.95; else scale = scaleY * 0.95;
        compOrigin();
        procConsole.updateScaleLabel();
        repaint();
    }

    /** Zoom in. */
    public void zoomIn() {
        scale = scale / 0.9;
        compOrigin();
        procConsole.updateScaleLabel();
        repaint();
    }

    /** Zoom out. */
    public void zoomOut() {
        scale = scale * 0.9;
        compOrigin();
        procConsole.updateScaleLabel();
        repaint();
    }

    public ProcObjectImage getSelectedObject(Point2D.Double p) {
        for (int i = 0; i < imgObjects.size(); i++) {
            ProcObjectImage pObject = (ProcObjectImage) imgObjects.get(i);
            if (pObject.contains(p)) return pObject;
        }
        return null;
    }

    /** Erase the selected objects. */
    public void eraseSelectedObjects() {
        Vector tempContainer = new Vector();
        for (int i = 0; i < imgObjects.size(); i++) {
            ProcObjectImage pObject = (ProcObjectImage) imgObjects.get(i);
            if (pObject.isSelected()) tempContainer.add(pObject);
        }
        if (tempContainer.size() == 0) {
            JOptionPane.showMessageDialog(this, "There is no object selected !", "Warning message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Do you realy want to delete the selected objects ?") == JOptionPane.YES_OPTION) {
            for (int i = 0; i < tempContainer.size(); i++) {
                ProcObjectImage pObject = (ProcObjectImage) tempContainer.elementAt(i);
                imgObjects.remove(pObject);
            }
            repaint();
        }
    }

    /**
	 * Make a process.
	 */
    public void makeAProcess() {
        aProcess = new AProcess();
        process = aProcess.create();
        ServerConstants.process = process;
        restoreImgObjects();
        zoomAll();
    }

    private void restoreImgObjects() {
        imgObjects = new ArrayList();
        if (process == null) {
            return;
        }
        Set objects = process.getPPSimObjects();
        for (Iterator iter = objects.iterator(); iter.hasNext(); ) {
            PPSimObject object = (PPSimObject) iter.next();
            if (object.getObjectType().equals(ServerConstants.WORKER_TYPE)) {
                imgObjects.add(new WorkerImage(object, this));
                continue;
            }
            if (object.getObjectType().equals(ServerConstants.TOOL_TYPE)) {
                imgObjects.add(new ProdToolImage(object, this));
                continue;
            }
            if (object.getObjectType().equals(ServerConstants.SECTORNAME_TYPE)) {
                imgObjects.add(new SectorNameImage(object, this));
                continue;
            }
            if (object.getObjectType().equals(ServerConstants.TRANSPORT_TYPE)) {
                imgObjects.add(new TransportUnitImage(object, this));
                continue;
            }
            if (object.getObjectType().equals(ServerConstants.BATCH_TYPE)) {
                imgObjects.add(new BatchImage(object, this));
                continue;
            }
            if (object.getObjectType().equals(ServerConstants.WALL_TYPE)) {
                imgObjects.add(new WallsImage(object, this));
                continue;
            }
            JOptionPane.showMessageDialog(this, "Cannot get image for " + object, "Error message", JOptionPane.ERROR_MESSAGE);
            break;
        }
        if (process.getGraph() != null) {
            imgObjects.add(new TechGraphImage(process.getGraph(), this));
        }
    }

    /**
	 * Starts the process for this image
	 */
    public void startProcess() {
        if (process == null) {
            JOptionPane.showMessageDialog(this, "There is no process loaded !" + "\nMake an instance of a process, first.", "Warning message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        process.startProcess();
    }

    /**
	 * Suspends the simulation process for this image.
	 */
    public void suspendProcess() {
        process.suspendProcess();
    }

    /**
	 * Stops the simulation process for this image.
	 */
    public void stopProcess() {
        process.stopProcess();
    }

    /**
	 * Resumes the simulation process for this image.
	 */
    public void resumeProcess() {
        process.resumeProcess();
    }

    /** Load the process. */
    public void loadProcess() {
        try {
            process = pp.loadProcess();
        } catch (PPSimException e) {
            MessageBox.showMessage(this, "Cannot load the process.", e);
            return;
        }
        ServerConstants.process = process;
        restoreImgObjects();
        zoomAll();
    }

    /**
	 * 
	 */
    public void saveProcess() {
        try {
            pp.saveProcess(process);
        } catch (PPSimException e) {
            MessageBox.showMessage(this, "Cannot save the process.", e);
            return;
        }
    }

    /** The legend - draw the symbols used on process image. */
    public void legend() {
    }

    /** Gets the graphics context. */
    public Graphics2D getGraphics2D() {
        return g2;
    }

    /** Gets the scale. */
    public double getScale() {
        return scale;
    }

    public void mouseMoved(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY());
        Point2D.Double pt = makeSpaceCoords(p);
        procConsole.updateCoordsLabels(pt);
    }

    public void mouseReleased(MouseEvent e) {
        if (procConsole.getZoomDynState()) {
            midPoint = makeSpaceCoords(new Point((startPointZoom.x + endPointZoom.x) / 2, (startPointZoom.y + endPointZoom.y) / 2));
            scale = scale * getDynZoomScale();
            compOrigin();
            startPointZoom = null;
            endPointZoom = null;
            repaint();
        }
    }

    private double getDynZoomScale() {
        double scaleX = getSize().getWidth() / (endPointZoom.x - startPointZoom.x);
        double scaleY = getSize().getHeight() / (endPointZoom.y - startPointZoom.y);
        return scaleX > scaleY ? scaleY : scaleX;
    }

    public void mousePressed(MouseEvent e) {
        if (procConsole.getSelectObjectState()) {
            Point2D.Double pt = makeSpaceCoords(new Point(e.getX(), e.getY()));
            ProcObjectImage pObject = getSelectedObject(pt);
            if (pObject != null) {
                pObject.setSelected(!pObject.isSelected());
                repaint();
            }
        }
        if (!procConsole.getZoomDynState()) {
            return;
        }
        startPointZoom = new Point(e.getX(), e.getY());
    }

    public void mouseDragged(MouseEvent e) {
        if (!procConsole.getZoomDynState()) {
            return;
        }
        endPointZoom = new Point(e.getX(), e.getY());
        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
