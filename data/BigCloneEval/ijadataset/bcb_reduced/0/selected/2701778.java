package org.das2.event;

import org.das2.graph.DasColumn;
import org.das2.graph.DasRow;
import org.das2.system.DasLogger;
import org.das2.DasApplication;
import org.das2.graph.DasAxis;
import org.das2.graph.DasCanvas;
import org.das2.graph.DasCanvasComponent;
import org.das2.util.DasExceptionHandler;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.*;
import java.util.logging.Logger;
import org.das2.components.propertyeditor.Editable;

/**
 * DasMouseInputAdapter delegates mouse and key events to mouse modules, which
 * do something with the events.  Also, mouse events are promoted to MouseDragEvents
 * which conveniently store information about the entire drag gesture.
 *
 * The base class of MouseModule has do-nothing stubs for KeyListener, MouseListener,
 * MouseMotionListener, and MouseWheelListener, which can be implemented if the
 * module wants to do something with these events.  Also MouseDragEvents will be
 * sent to the module as its DragRenderer has requested: after the mouse release,
 * during the drag, or when keys are pressed.
 *
 * The module will first receive the low-level events before receiving the MouseDragEvents.
 *
 * @author  jbf
 */
public class DasMouseInputAdapter extends MouseInputAdapter implements Editable, MouseWheelListener {

    private MouseModule primary = null;

    private MouseModule secondary = null;

    private Vector active = null;

    private boolean pinned = false;

    private Vector modules;

    private HashMap primaryActionButtonMap;

    private HashMap secondaryActionButtonMap;

    protected JPopupMenu primaryPopup;

    protected JPopupMenu secondaryPopup;

    private Point secondaryPopupLocation;

    private JPanel pngFileNamePanel;

    private JTextField pngFileTextField;

    private JFileChooser pngFileChooser;

    JCheckBoxMenuItem primarySelectedItem;

    JCheckBoxMenuItem secondarySelectedItem;

    Rectangle[] dirtyBoundsList;

    Logger log = DasLogger.getLogger(DasLogger.GUI_LOG);

    /**
     * number of additional inserted popup menu items to the primary menu.
     */
    int numInserted;

    /**
     * number of additional inserted popup menu items to the secondary menu.
     * Components can be added to the primary menu, but not the secondary.
     */
    int numInsertedSecondary;

    protected ActionListener popupListener;

    protected DasCanvasComponent parent = null;

    private Point dSelectionStart;

    private Point dSelectionEnd;

    private MousePointSelectionEvent mousePointSelection;

    private int xOffset;

    private int yOffset;

    private int button = 0;

    private MouseMode mouseMode = MouseMode.idle;

    private boolean drawControlPoints = false;

    private DragRenderer resizeRenderer = null;

    private Point resizeStart = null;

    Vector hotSpots = null;

    Rectangle dirtyBounds = null;

    private boolean hasFocus = false;

    private Point pressPosition;

    private boolean headless;

    private static final class MouseMode {

        String s;

        boolean resizeTop = false;

        boolean resizeBottom = false;

        boolean resizeRight = false;

        boolean resizeLeft = false;

        Point moveStart = null;

        static final MouseMode idle = new MouseMode("idle");

        static final MouseMode resize = new MouseMode("resize");

        static final MouseMode move = new MouseMode("move");

        static final MouseMode moduleDrag = new MouseMode("moduleDrag");

        MouseMode(String s) {
            this.s = s;
        }

        public String toString() {
            return s;
        }
    }

    /** Creates a new instance of dasMouseInputAdapter */
    public DasMouseInputAdapter(DasCanvasComponent parent) {
        this.parent = parent;
        modules = new Vector();
        primaryActionButtonMap = new HashMap();
        secondaryActionButtonMap = new HashMap();
        this.headless = DasApplication.getDefaultApplication().isHeadless();
        if (!headless) {
            primaryPopup = new JPopupMenu();
            numInserted = createPopup(primaryPopup);
            secondaryPopup = new JPopupMenu();
            numInsertedSecondary = createPopup(secondaryPopup);
        }
        active = null;
        mousePointSelection = new MousePointSelectionEvent(this, 0, 0);
        resizeRenderer = new BoxRenderer(parent, false);
        dirtyBoundsList = new Rectangle[0];
    }

    public void replaceMouseModule(MouseModule oldModule, MouseModule newModule) {
        JCheckBoxMenuItem j = (JCheckBoxMenuItem) primaryActionButtonMap.get(oldModule);
        primaryActionButtonMap.put(newModule, j);
        primaryActionButtonMap.remove(oldModule);
        secondaryActionButtonMap.put(newModule, secondaryActionButtonMap.get(oldModule));
        secondaryActionButtonMap.remove(oldModule);
        modules.removeElement(oldModule);
        modules.addElement(newModule);
    }

    /**
     * add a mouse module to the list of available modules.  If a module with the same
     * label exists already, it will be replaced.
     */
    public void addMouseModule(MouseModule module) {
        if (headless) {
            DasLogger.getLogger(DasLogger.GUI_LOG).fine("not adding module since headless is true");
        } else {
            MouseModule preExisting = getModuleByLabel(module.getLabel());
            if (preExisting != null) {
                DasLogger.getLogger(DasLogger.GUI_LOG).fine("Replacing mouse module " + module.getLabel() + ".");
                replaceMouseModule(preExisting, module);
            } else {
                modules.add(module);
                String name = module.getLabel();
                JCheckBoxMenuItem primaryNewItem = new JCheckBoxMenuItem(name);
                JCheckBoxMenuItem secondaryNewItem = new JCheckBoxMenuItem(name);
                primaryNewItem.addActionListener(popupListener);
                primaryNewItem.setActionCommand("primary");
                secondaryNewItem.addActionListener(popupListener);
                secondaryNewItem.setActionCommand("secondary");
                primaryActionButtonMap.put(module, primaryNewItem);
                secondaryActionButtonMap.put(module, secondaryNewItem);
                try {
                    primaryPopup.add(primaryNewItem, numInserted + 1 + primaryActionButtonMap.size() - 1);
                    secondaryPopup.add(secondaryNewItem, numInsertedSecondary + 1 + secondaryActionButtonMap.size() - 1);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * added so ColumnColumnConnector could delegate to DasPlot's adapter.
     * @return
     */
    public JPopupMenu getPrimaryPopupMenu() {
        return this.primaryPopup;
    }

    public KeyAdapter getKeyAdapter() {
        return new KeyAdapter() {

            public void keyPressed(KeyEvent ev) {
                log.finest("keyPressed ");
                if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && active != null) {
                    active = null;
                    getGlassPane().setDragRenderer(null, null, null);
                    parent.getCanvas().paintImmediately(0, 0, parent.getCanvas().getWidth(), parent.getCanvas().getHeight());
                    refresh();
                    ev.consume();
                } else if (ev.getKeyCode() == KeyEvent.VK_SHIFT) {
                    drawControlPoints = true;
                    parent.repaint();
                } else if (ev.getKeyChar() == 'p') {
                    pinned = true;
                    ev.consume();
                } else {
                    if (active == null) {
                        return;
                    }
                    for (int i = 0; i < active.size(); i++) {
                        ((MouseModule) active.get(i)).keyPressed(ev);
                    }
                }
            }

            public void keyReleased(KeyEvent ev) {
                if (ev.getKeyCode() == KeyEvent.VK_SHIFT) {
                    drawControlPoints = false;
                    parent.repaint();
                }
                if (active == null) {
                    return;
                }
                for (int i = 0; i < active.size(); i++) {
                    ((MouseModule) active.get(i)).keyReleased(ev);
                }
            }

            public void keyTyped(KeyEvent ev) {
                if (active == null) {
                    return;
                }
                for (int i = 0; i < active.size(); i++) {
                    ((MouseModule) active.get(i)).keyTyped(ev);
                }
            }
        };
    }

    public MouseModule getPrimaryModule() {
        ArrayList activ = new ArrayList();
        for (int i = 0; i < modules.size(); i++) {
            JCheckBoxMenuItem j = (JCheckBoxMenuItem) primaryActionButtonMap.get(modules.get(i));
            if (j.isSelected()) {
                activ.add(modules.get(i));
            }
        }
        return (MouseModule) activ.get(0);
    }

    public MouseModule getSecondaryModule() {
        ArrayList activ = new ArrayList();
        for (int i = 0; i < modules.size(); i++) {
            JCheckBoxMenuItem j = (JCheckBoxMenuItem) secondaryActionButtonMap.get(modules.get(i));
            if (j.isSelected()) {
                activ.add(modules.get(i));
            }
        }
        return (MouseModule) activ.get(0);
    }

    /**
     * set the primary module, the module receiving left-button events, to the
     * module provided.  If the module is not already loaded, implicitly addMouseModule
     * is called.
     */
    public void setPrimaryModule(MouseModule module) {
        if (headless) {
            return;
        }
        JCheckBoxMenuItem j = (JCheckBoxMenuItem) primaryActionButtonMap.get(module);
        if (j == null) {
            addMouseModule(module);
        }
        for (Iterator i = primaryActionButtonMap.entrySet().iterator(); i.hasNext(); ) {
            try {
                Object ii = ((Map.Entry) i.next()).getValue();
                ((JCheckBoxMenuItem) ii).setSelected(false);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }
        j = (JCheckBoxMenuItem) primaryActionButtonMap.get(module);
        if (j != null) {
            j.setSelected(true);
        }
        primarySelectedItem = j;
        primary = module;
        parent.setCursor(primary.getCursor());
    }

    /**
     * set the secondary module, the module receiving middle-button events, to the
     * module provided.  If the module is not already loaded, implicitly addMouseModule
     * is called.
     */
    public void setSecondaryModule(MouseModule module) {
        if (headless) {
            return;
        }
        JCheckBoxMenuItem j = (JCheckBoxMenuItem) secondaryActionButtonMap.get(module);
        if (j == null) {
            addMouseModule(module);
        }
        for (Iterator i = secondaryActionButtonMap.entrySet().iterator(); i.hasNext(); ) {
            try {
                Object ii = ((Map.Entry) i.next()).getValue();
                ((JCheckBoxMenuItem) ii).setSelected(false);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }
        j = (JCheckBoxMenuItem) secondaryActionButtonMap.get(module);
        if (j != null) {
            j.setSelected(true);
        }
        secondarySelectedItem = j;
        secondary = module;
    }

    /**
     * create the popup for the component.  This popup has three
     * sections:
     * <pre>1. component actions
     *2. mouse modules
     *3. canvas actions</pre>
     * The variable numInserted is the number of actions inserted, and
     * is used to calculate the position of inserted mouse modules.
     */
    private int createPopup(JPopupMenu popup) {
        popupListener = createPopupMenuListener();
        Action[] componentActions = parent.getActions();
        for (int iaction = 0; iaction < componentActions.length; iaction++) {
            JMenuItem item = new JMenuItem();
            item.setAction(componentActions[iaction]);
            popup.add(item);
        }
        int numInsert = componentActions.length;
        popup.addSeparator();
        popup.addSeparator();
        Action[] canvasActions = DasCanvas.getActions();
        for (int iaction = 0; iaction < canvasActions.length; iaction++) {
            JMenuItem item = new JMenuItem();
            item.setAction(canvasActions[iaction]);
            popup.add(item);
        }
        return numInsert;
    }

    private ActionListener createPopupMenuListener() {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DasMouseInputAdapter outer = DasMouseInputAdapter.this;
                String command = e.getActionCommand();
                if (command.equals("properties")) {
                    parent.showProperties();
                } else if (command.equals("print")) {
                    Printable p = ((DasCanvas) parent.getParent()).getPrintable();
                    PrinterJob pj = PrinterJob.getPrinterJob();
                    pj.setPrintable(p);
                    if (pj.printDialog()) {
                        try {
                            pj.print();
                        } catch (PrinterException pe) {
                            Object[] message = { "Error printing", pe.getMessage() };
                            JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (command.equals("toPng")) {
                    if (pngFileNamePanel == null) {
                        pngFileNamePanel = new JPanel();
                        pngFileNamePanel.setLayout(new BoxLayout(pngFileNamePanel, BoxLayout.X_AXIS));
                        pngFileTextField = new JTextField(32);
                        pngFileTextField.setMaximumSize(pngFileTextField.getPreferredSize());
                        pngFileChooser = new JFileChooser();
                        pngFileChooser.setApproveButtonText("Select File");
                        pngFileChooser.setDialogTitle("Write to PNG");
                        JButton b = new JButton("Browse");
                        b.setActionCommand("pngBrowse");
                        b.addActionListener(this);
                        pngFileNamePanel.add(pngFileTextField);
                        pngFileNamePanel.add(b);
                    }
                    pngFileTextField.setText(pngFileChooser.getCurrentDirectory().getPath());
                    String[] options = { "Write to PNG", "Cancel" };
                    int choice = JOptionPane.showOptionDialog(parent, pngFileNamePanel, "Write to PNG", 0, JOptionPane.QUESTION_MESSAGE, null, options, "Ok");
                    if (choice == 0) {
                        DasCanvas canvas = (DasCanvas) parent.getParent();
                        try {
                            canvas.writeToPng(pngFileTextField.getText());
                        } catch (java.io.IOException ioe) {
                            org.das2.util.DasExceptionHandler.handle(ioe);
                        }
                    }
                } else if (command.equals("pngBrowse")) {
                    int choice = pngFileChooser.showDialog(parent, "Select File");
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        pngFileTextField.setText(pngFileChooser.getSelectedFile().getPath());
                    }
                } else if (command.equals("close")) {
                } else if (command.equals("primary")) {
                    if (primarySelectedItem != null) {
                        primarySelectedItem.setSelected(false);
                    }
                    for (int i = 0; i < modules.size(); i++) {
                        JCheckBoxMenuItem j = (JCheckBoxMenuItem) primaryActionButtonMap.get(modules.get(i));
                        if (j.isSelected()) {
                            primarySelectedItem = j;
                            break;
                        }
                    }
                    primarySelectedItem.setSelected(true);
                } else if (command.equals("secondary")) {
                    if (secondarySelectedItem != null) {
                        secondarySelectedItem.setSelected(false);
                    }
                    Point l = secondaryPopupLocation;
                    for (int i = 0; i < modules.size(); i++) {
                        JCheckBoxMenuItem j = (JCheckBoxMenuItem) secondaryActionButtonMap.get(modules.get(i));
                        if (j.isSelected()) {
                            secondarySelectedItem = j;
                            break;
                        }
                    }
                } else {
                    System.err.println("" + command);
                }
            }
        };
    }

    /**
     * call the renderDrag method of the active module's dragRenderer.  This method
     * returns an array of Rectangles, or null, indicating the affected regions.
     * It's also permissable for a array element to be null.
     */
    private void renderSelection(Graphics2D g2d) {
        try {
            for (int i = 0; i < active.size(); i++) {
                DragRenderer dr = ((MouseModule) active.get(i)).getDragRenderer();
                getGlassPane().setDragRenderer(dr, dSelectionStart, dSelectionEnd);
            }
        } catch (RuntimeException e) {
            DasExceptionHandler.handle(e);
        }
    }

    private synchronized void refresh() {
        if (dirtyBoundsList.length > 0) {
            Rectangle[] dd = new Rectangle[dirtyBoundsList.length];
            for (int i = 0; i < dd.length; i++) {
                if (dirtyBoundsList[i] != null) {
                    dd[i] = new Rectangle(dirtyBoundsList[i]);
                }
            }
            for (int i = 0; i < dd.length; i++) {
                if (dd[i] != null) {
                    parent.getCanvas().paintImmediately(dd[i]);
                }
            }
            for (int i = 0; i < dirtyBoundsList.length; i++) {
                if (dirtyBoundsList[i] != null) {
                    parent.getCanvas().paintImmediately(dirtyBoundsList[i]);
                }
            }
        } else {
            if (active != null) {
                parent.getCanvas().paintImmediately(0, 0, parent.getCanvas().getWidth(), parent.getCanvas().getHeight());
            }
        }
        if (active == null) {
            dirtyBoundsList = new Rectangle[0];
        }
    }

    public void paint(Graphics g1) {
        Graphics2D g = (Graphics2D) g1.create();
        g.translate(-parent.getX(), -parent.getY());
        if (active != null) {
            renderSelection(g);
        }
        if (hasFocus && hoverHighlite) {
            g.setColor(new Color(255, 0, 0, 10));
            g.setStroke(new BasicStroke(10));
            g.draw(parent.getBounds());
            g.dispose();
            return;
        }
        if (hasFocus && drawControlPoints) {
            drawControlPoints(g);
        }
        g.dispose();
    }

    private void drawControlPoints(Graphics2D g) {
        if (parent.getRow() != DasRow.NULL && parent.getColumn() != DasColumn.NULL) {
            int xLeft = parent.getColumn().getDMinimum();
            int xRight = parent.getColumn().getDMaximum();
            int xMid = (xLeft + xRight) / 2;
            int yTop = parent.getRow().getDMinimum();
            int yBottom = parent.getRow().getDMaximum();
            int yMid = (yTop + yBottom) / 2;
            Graphics2D gg = (Graphics2D) g.create();
            gg.setColor(new Color(0, 0, 0, 255));
            int ss = 9;
            gg.fillRect(xLeft + 1, yTop + 1, ss - 2, ss - 2);
            gg.fillRect(xRight - ss + 1, yTop + 1, ss - 2, ss - 2);
            gg.fillRect(xLeft + 1, yBottom - ss + 1, ss - 2, ss - 2);
            gg.fillRect(xRight - ss + 1, yBottom - ss + 1, ss - 2, ss - 2);
            gg.fillRect(xMid + 1 - ss / 2, yTop + 1, ss - 2, ss - 2);
            gg.fillRect(xRight - ss + 1, yMid + 1 - ss / 2, ss - 2, ss - 2);
            gg.fillRect(xMid + 1 - ss / 2, yBottom - ss + 1, ss - 2, ss - 2);
            gg.fillRect(xLeft + 1, yMid - ss / 2 + 1, ss - 2, ss - 2);
            gg.setColor(new Color(255, 255, 255, 100));
            gg.drawRect(xLeft, yTop, ss, ss);
            gg.drawRect(xRight - ss, yTop, ss, ss);
            gg.drawRect(xLeft, yBottom - ss, ss, ss);
            gg.drawRect(xRight - ss, yBottom - ss, ss, ss);
            gg.drawRect(xMid - ss / 2, yTop + 1, ss, ss);
            gg.drawRect(xRight - ss, yMid - ss / 2, ss, ss);
            gg.drawRect(xMid - ss / 2, yBottom - ss, ss, ss);
            gg.drawRect(xLeft, yMid - ss / 2, ss, ss);
            int xmid = (xLeft + xRight) / 2;
            int ymid = (yTop + yBottom) / 2;
            int rr = 4;
            g.setColor(new Color(255, 255, 255, 100));
            gg.fillOval(xmid - rr - 1, ymid - rr - 1, rr * 2 + 3, rr * 2 + 3);
            gg.setColor(new Color(0, 0, 0, 255));
            gg.drawOval(xmid - rr, ymid - rr, rr * 2, rr * 2);
            gg.fillOval(xmid - 1, ymid - 1, 3, 3);
            gg.dispose();
        }
    }

    private MouseMode activateMouseMode(MouseEvent e) {
        boolean xLeftSide = false;
        boolean xRightSide = false;
        boolean xMiddle = false;
        boolean yTopSide = false;
        boolean yBottomSide = false;
        boolean yMiddle = false;
        Point mousePoint = e.getPoint();
        mousePoint.translate(parent.getX(), parent.getY());
        if (parent.getRow() != DasRow.NULL && parent.getColumn() != DasColumn.NULL) {
            int xLeft = parent.getColumn().getDMinimum();
            int xRight = parent.getColumn().getDMaximum();
            int yTop = parent.getRow().getDMinimum();
            int yBottom = parent.getRow().getDMaximum();
            int xmid = (xLeft + xRight) / 2;
            int ymid = (yTop + yBottom) / 2;
            xLeftSide = mousePoint.getX() < xLeft + 10;
            xRightSide = mousePoint.getX() > xRight - 10;
            xMiddle = Math.abs(mousePoint.getX() - xmid) < 4;
            yTopSide = (mousePoint.getY() < yTop + 10) && (mousePoint.getY() >= yTop);
            yBottomSide = mousePoint.getY() > (yBottom - 10);
            yMiddle = Math.abs(mousePoint.getY() - ymid) < 4;
        }
        MouseMode result = MouseMode.idle;
        Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        if (!(parent instanceof DasAxis)) {
            if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
                if (xLeftSide) {
                    if (yTopSide) {
                        result = MouseMode.resize;
                        cursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
                    } else if (yBottomSide) {
                        result = MouseMode.resize;
                        cursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
                    } else if (yMiddle) {
                        result = MouseMode.resize;
                        cursor = new Cursor(Cursor.W_RESIZE_CURSOR);
                    }
                } else if (xRightSide) {
                    if (yTopSide) {
                        result = MouseMode.resize;
                        cursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
                    } else if (yBottomSide) {
                        result = MouseMode.resize;
                        cursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
                    } else if (yMiddle) {
                        result = MouseMode.resize;
                        cursor = new Cursor(Cursor.E_RESIZE_CURSOR);
                    }
                } else if (xMiddle && yMiddle) {
                    result = MouseMode.move;
                    cursor = new Cursor(Cursor.MOVE_CURSOR);
                } else if (xMiddle && yTopSide) {
                    result = MouseMode.resize;
                    cursor = new Cursor(Cursor.N_RESIZE_CURSOR);
                } else if (xMiddle && yBottomSide) {
                    result = MouseMode.resize;
                    cursor = new Cursor(Cursor.S_RESIZE_CURSOR);
                }
            }
        }
        if (result == MouseMode.resize) {
            result.resizeBottom = yBottomSide;
            result.resizeTop = yTopSide;
            result.resizeRight = xRightSide;
            result.resizeLeft = xLeftSide;
        } else if (result == MouseMode.move) {
            result.moveStart = e.getPoint();
            result.moveStart.translate(-parent.getX(), -parent.getY());
        }
        if (result != mouseMode) {
            getGlassPane().setCursor(cursor);
        }
        return result;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        log.finest("mouseMoved");
        Point l = parent.getLocation();
        xOffset = l.x;
        yOffset = l.y;
        boolean drawControlPoints0 = this.drawControlPoints;
        if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
            drawControlPoints = true;
        } else {
            drawControlPoints = false;
        }
        if (drawControlPoints0 != drawControlPoints) {
            parent.repaint();
        }
        MouseMode m;
        if ((m = activateMouseMode(e)) != null) {
            mouseMode = m;
        } else {
            mouseMode = MouseMode.idle;
        }
    }

    private void showPopup(JPopupMenu menu, MouseEvent ev) {
        log.finest("showPopup");
        if (menu != primaryPopup && menu != secondaryPopup) {
            throw new IllegalArgumentException("menu must be primary or secondary popup menu");
        }
        for (Iterator i = modules.iterator(); i.hasNext(); ) {
            MouseModule mm = (MouseModule) i.next();
            JCheckBoxMenuItem j = (JCheckBoxMenuItem) primaryActionButtonMap.get(mm);
            j.setText(mm.getLabel());
        }
        menu.show(ev.getComponent(), ev.getX(), ev.getY());
    }

    public void setPinned(boolean b) {
        pinned = b;
    }

    public boolean getPinned() {
        return pinned;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        log.finer("mousePressed " + mouseMode);
        if (pinned) {
            active = null;
            refresh();
        }
        pinned = false;
        Point l = parent.getLocation();
        parent.requestFocus();
        xOffset = l.x;
        yOffset = l.y;
        pressPosition = e.getPoint();
        Point cp = new Point(e.getPoint());
        cp.translate(xOffset, yOffset);
        if (!parent.acceptContext(cp.x, cp.y)) {
            return;
        }
        if (mouseMode == MouseMode.resize) {
            resizeStart = new Point(0, 0);
            if (mouseMode.resizeRight) {
                resizeStart.x = 0 + xOffset;
            } else if (mouseMode.resizeLeft) {
                resizeStart.x = parent.getWidth() + xOffset;
            } else {
                resizeStart.x = 0 + xOffset;
            }
            if (mouseMode.resizeTop) {
                resizeStart.y = parent.getHeight() + yOffset;
            } else if (mouseMode.resizeBottom) {
                resizeStart.y = 0 + yOffset;
            } else {
                resizeStart.y = 0 + yOffset;
            }
        } else if (mouseMode == MouseMode.move) {
            mouseMode.moveStart = e.getPoint();
            mouseMode.moveStart.translate(xOffset, yOffset);
        } else {
            if (active == null) {
                button = e.getButton();
                dSelectionStart = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), parent.getCanvas());
                dSelectionEnd = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), parent.getCanvas());
                if (e.isControlDown() || button == MouseEvent.BUTTON3) {
                    if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
                        showPopup(primaryPopup, e);
                    } else {
                        showPopup(secondaryPopup, e);
                    }
                } else {
                    active = new Vector();
                    if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
                        for (int i = 0; i < modules.size(); i++) {
                            JCheckBoxMenuItem j = (JCheckBoxMenuItem) primaryActionButtonMap.get(modules.get(i));
                            if (j.isSelected()) {
                                active.add(modules.get(i));
                            }
                        }
                    } else {
                        for (int i = 0; i < modules.size(); i++) {
                            JCheckBoxMenuItem j = (JCheckBoxMenuItem) secondaryActionButtonMap.get(modules.get(i));
                            if (j.isSelected()) {
                                active.add(modules.get(i));
                            }
                        }
                    }
                    mouseMode = MouseMode.moduleDrag;
                    mousePointSelection.set(e.getX() + xOffset, e.getY() + yOffset);
                    for (int i = 0; i < active.size(); i++) {
                        MouseModule j = (MouseModule) active.get(i);
                        j.mousePressed(e);
                        if (j.dragRenderer.isPointSelection()) {
                            mouseDragged(e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        log.finest("mouseDragged in " + mouseMode);
        if (mouseMode == MouseMode.resize) {
            Point p = e.getPoint();
            p.translate(parent.getX(), parent.getY());
            if (!(mouseMode.resizeBottom || mouseMode.resizeTop)) {
                p.y = parent.getRow().getDMaximum();
            }
            if (!(mouseMode.resizeRight || mouseMode.resizeLeft)) {
                p.x = parent.getColumn().getDMaximum();
            }
            getGlassPane().setDragRenderer(resizeRenderer, resizeStart, p);
            getGlassPane().repaint();
        } else if (mouseMode == MouseMode.move) {
            Point moveEnd = e.getPoint();
            moveEnd.translate(xOffset, yOffset);
            int dx = moveEnd.x - mouseMode.moveStart.x;
            int dy = moveEnd.y - mouseMode.moveStart.y;
            int xmin = parent.getColumn().getDMinimum();
            int xmax = parent.getColumn().getDMaximum();
            int ymin = parent.getRow().getDMinimum();
            int ymax = parent.getRow().getDMaximum();
            Point p1 = new Point(xmin + dx, ymin + dy);
            Point p2 = new Point(xmax + dx, ymax + dy);
            getGlassPane().setDragRenderer(resizeRenderer, p1, p2);
            getGlassPane().repaint();
        } else {
            if (active != null) {
                dSelectionEnd = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), parent.getCanvas());
                mousePointSelection.set((int) dSelectionEnd.getX(), (int) dSelectionEnd.getY());
                for (int i = 0; i < active.size(); i++) {
                    try {
                        MouseModule j = (MouseModule) active.get(i);
                        if (j.dragRenderer.isPointSelection()) {
                            log.finest("mousePointSelected");
                            j.mousePointSelected(mousePointSelection);
                        }
                        if (j.dragRenderer.isUpdatingDragSelection()) {
                            MouseDragEvent de = j.dragRenderer.getMouseDragEvent(parent, dSelectionStart, dSelectionEnd, e.isShiftDown());
                            log.finest("mouseRangeSelected");
                            j.mouseRangeSelected(de);
                        }
                        j.mouseDragged(e);
                    } catch (RuntimeException except) {
                        DasExceptionHandler.handle(except);
                    }
                }
                refresh();
            }
        }
    }

    private void performResize(MouseEvent e) {
        int dxLeft = parent.getColumn().getDMinimum();
        int dxRight = parent.getColumn().getDMaximum();
        int dyTop = parent.getRow().getDMinimum();
        int dyBottom = parent.getRow().getDMaximum();
        int dx = e.getX() + xOffset;
        int dy = e.getY() + yOffset;
        if (mouseMode.resizeRight) {
            dxRight = dx;
        } else if (mouseMode.resizeLeft) {
            dxLeft = dx;
        }
        if (mouseMode.resizeTop) {
            dyTop = dy;
        } else if (mouseMode.resizeBottom) {
            dyBottom = dy;
        }
        parent.getColumn().setDPosition(dxLeft, dxRight);
        parent.getRow().setDPosition(dyTop, dyBottom);
        xOffset += dx;
        yOffset += dy;
        parent.resize();
        getGlassPane().setDragRenderer(null, null, null);
        getGlassPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseReleased(MouseEvent e) {
        log.finest("mouseReleased");
        if (mouseMode == MouseMode.resize) {
            performResize(e);
            getGlassPane().setDragRenderer(null, null, null);
            parent.getCanvas().paintImmediately(0, 0, parent.getCanvas().getWidth(), parent.getCanvas().getHeight());
            refresh();
        } else if (mouseMode == MouseMode.move) {
            performMove(e);
            getGlassPane().setDragRenderer(null, null, null);
            parent.getCanvas().paintImmediately(0, 0, parent.getCanvas().getWidth(), parent.getCanvas().getHeight());
            refresh();
        } else {
            if (e.getButton() == button) {
                if (active != null) {
                    for (int i = 0; i < active.size(); i++) {
                        MouseModule j = (MouseModule) active.get(i);
                        try {
                            MouseDragEvent de = j.dragRenderer.getMouseDragEvent(parent, dSelectionStart, dSelectionEnd, e.isShiftDown());
                            j.mouseRangeSelected(de);
                        } catch (RuntimeException ex) {
                            DasExceptionHandler.handle(ex);
                        } finally {
                            button = 0;
                            try {
                                j.mouseReleased(e);
                            } catch (RuntimeException ex2) {
                                DasExceptionHandler.handle(ex2);
                            }
                        }
                    }
                    if (!pinned) {
                        active = null;
                        getGlassPane().setDragRenderer(null, null, null);
                        parent.getCanvas().paintImmediately(0, 0, parent.getCanvas().getWidth(), parent.getCanvas().getHeight());
                        refresh();
                    }
                }
            }
        }
    }

    public void removeMouseModule(MouseModule module) {
    }

    /**
     * Getter for property mouseModules.
     * @return Value of property mouseModules.
     */
    public MouseModule getMouseModule(int i) {
        return (MouseModule) modules.get(i);
    }

    public MouseModule[] getMouseModules() {
        MouseModule[] result = new MouseModule[modules.size()];
        modules.copyInto(result);
        return result;
    }

    /**
     * @deprecated use getPrimaryModuleByLabel
     * @return
     */
    public String getPrimaryModuleLabel() {
        MouseModule primary = getPrimaryModule();
        return primary == null ? "" : primary.getLabel();
    }

    public String getPrimaryModuleByLabel() {
        MouseModule primary = getPrimaryModule();
        return primary == null ? "" : primary.getLabel();
    }

    public void setPrimaryModuleByLabel(String label) {
        MouseModule mm = getModuleByLabel(label);
        if (mm != null) {
            setPrimaryModule(mm);
        }
    }

    public String getSecondaryModuleByLabel() {
        MouseModule secondary = getPrimaryModule();
        return secondary == null ? "" : secondary.getLabel();
    }

    public void setSecondaryModuleByLabel(String label) {
        MouseModule mm = getModuleByLabel(label);
        if (mm != null) {
            setSecondaryModule(mm);
        }
    }

    /**
     * //TODO: check this
     * Setter for property mouseModules.
     * @param mouseModule the new mouseModule to use.
     */
    public void setMouseModule(int i, MouseModule mouseModule) {
        this.modules.set(i, mouseModule);
    }

    public void mouseEntered(MouseEvent e) {
        hasFocus = true;
        if (e.isShiftDown()) {
            parent.repaint();
        }
        if (primary != null) {
            getGlassPane().setCursor(primary.getCursor());
        }
    }

    public void mouseExited(MouseEvent e) {
        hasFocus = false;
        if (e.isShiftDown()) {
            parent.repaint();
        }
        getGlassPane().setCursor(Cursor.getDefaultCursor());
    }

    /**
     * hack to provide way to get rid of "Dump Data".  
     * @param label string to search for.
     */
    public synchronized void removeMenuItem(String label) {
        if (headless) {
            return;
        }
        MenuElement[] ele = primaryPopup.getSubElements();
        int index = -1;
        for (int i = 0; i < numInserted; i++) {
            if (ele[i] instanceof JMenuItem) {
                if (((JMenuItem) ele[i]).getText().contains(label)) {
                    index = i;
                    break;
                }
            }
        }
        if (index != -1) {
            primaryPopup.remove(index);
            numInserted--;
        }
    }

    public synchronized void addMenuItem(final Component b) {
        if (headless) {
            return;
        }
        if (numInserted == 0) {
            primaryPopup.insert(new JPopupMenu.Separator(), 0);
            numInserted++;
        }
        if (b instanceof JPopupMenu) {
            if (numInserted > 1) {
                primaryPopup.insert(new JPopupMenu.Separator(), 0);
                numInserted++;
            }
            JPopupMenu c = (JPopupMenu) b;
            for (MenuElement me : c.getSubElements()) {
                if (me.getComponent() instanceof JCheckBoxMenuItem) continue;
                primaryPopup.insert(me.getComponent(), numInserted);
                numInserted++;
            }
        } else {
            primaryPopup.insert(b, numInserted);
            numInserted++;
        }
    }

    /**
     * return a menu with font to match LAF.
     * @param label
     * @return
     */
    public JMenu addMenu(String label) {
        JMenu result = new JMenu(label);
        addMenuItem(result);
        return result;
    }

    private DasCanvas.GlassPane getGlassPane() {
        DasCanvas.GlassPane r = (DasCanvas.GlassPane) ((DasCanvas) parent.getParent()).getGlassPane();
        if (r.isVisible() == false) {
            r.setVisible(true);
        }
        return r;
    }

    public MouseModule getModuleByLabel(java.lang.String label) {
        MouseModule result = null;
        for (int i = 0; i < modules.size(); i++) {
            if (label.equals(((MouseModule) modules.get(i)).getLabel())) {
                result = (MouseModule) modules.get(i);
            }
        }
        return result;
    }

    /**
     * Draws a faint box around the border when the mouse enters the component,
     * to help indicate what's going on.
     */
    private boolean hoverHighlite = false;

    public boolean isHoverHighlite() {
        return this.hoverHighlite;
    }

    public void setHoverHighlite(boolean value) {
        this.hoverHighlite = value;
    }

    /**
     * returns the position of the last mouse press.  This is a hack so that
     * the mouse position can be obtained to get the context of the press.
     * The result point is in the parent's coordinate system.
     */
    public Point getMousePressPosition() {
        return this.pressPosition;
    }

    private void performMove(MouseEvent e) {
        Point moveEnd = e.getPoint();
        moveEnd.translate(xOffset, yOffset);
        int dx = moveEnd.x - mouseMode.moveStart.x;
        int dy = moveEnd.y - mouseMode.moveStart.y;
        this.xOffset += dx;
        this.yOffset += dy;
        int min = parent.getColumn().getDMinimum();
        int max = parent.getColumn().getDMaximum();
        parent.getColumn().setDPosition(min + dx, max + dx);
        min = parent.getRow().getDMinimum();
        max = parent.getRow().getDMaximum();
        parent.getRow().setDPosition(min + dy, max + dy);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (secondary != null) {
            secondary.mouseWheelMoved(e);
        }
    }
}
