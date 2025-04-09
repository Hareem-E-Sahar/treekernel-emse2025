package au.vermilion.desktop.GUI;

import static au.vermilion.Vermilion.logger;
import static au.vermilion.Vermilion.document;
import au.vermilion.desktop.AudioRouter;
import au.vermilion.desktop.WireObj;
import au.vermilion.desktop.MachineObj;
import au.vermilion.PC.PCWindow;
import au.vermilion.PC.relativelayout.RelativeLayout;
import au.vermilion.PC.relativelayout.RelativePlacement;
import au.vermilion.utils.ExposedArrayList;
import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.Color;
import java.util.logging.Level;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * An extension of the JPanel class that shows machines and their
 * connections (wires) on the screen. Allows the user to manipulate
 * the machines and connections.
 *
 * This includes a custom drawn control that shows a 'dot' the user can
 * drag around to change the volume and panning of a wire.
 */
public class JWirePanel extends JPanel implements Runnable {

    /**
     * Stores the list of machine representations we are showing on the screen.
     * This will be a reflection of what is in the router.
     */
    public ArrayList<JMachine> machineBoxes = new ArrayList<JMachine>();

    /**
     * Stores the list of wire representations we are showing on the screen.
     * This will be a reflection of what is in the router.
     */
    public ArrayList<JWire> machineWires = new ArrayList<JWire>();

    /**
     * This is our reference to the router itself, which we call on to change
     * the audio chain.
     */
    public AudioRouter router;

    public JWire selectedWire = null;

    public int dotPanelX = 0;

    public int dotPanelY = 0;

    /**
     * The width at which we draw machines, shared with JMachineBox.
     */
    public static final int MACHINE_WIDTH = 120;

    /**
     * The height at which we draw machines, shared with JMachineBox.
     */
    public static final int MACHINE_HEIGHT = 50;

    private static final double DEG120 = Math.PI * 2.0 / 3.0;

    public JWire menuWire = null;

    public JMachine newWireMachine = null;

    private int newWireLocX = 0;

    private int newWireLocY = 0;

    private Color[] wireColors = new Color[] { new Color(0.6f, 0.6f, 0.9f), new Color(0.6f, 0.9f, 0.6f), new Color(0.9f, 0.6f, 0.6f) };

    private Color dotColor = new Color(0.6f, 0.6f, 0.9f);

    /**
     * The current size of the volume/panning panel.
     */
    public static int dotPanelSize = 50;

    public static final float dotPanelRange = 0.6f;

    /**
     * The scaling layout manager used to scale machine boxes and text.
     */
    public RelativeLayout panelLayout;

    public boolean isRunning = true;

    /**
     * The constructor simply sets the layout manager, event handlers and menus.
     */
    public JWirePanel(AudioRouter sysRouter) {
        router = sysRouter;
        panelLayout = new RelativeLayout(null);
        setLayout(panelLayout);
        JWirePanelMouseListener mouseListener = new JWirePanelMouseListener(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        Thread timer = new Thread(this);
        timer.setPriority(Thread.MIN_PRIORITY);
        timer.start();
    }

    /**
     * Called from the system router to pass the list of machines and draw them
     * on the screen. This method will be called whenever a plugin is added or
     * removed from the wiring.
     */
    @SuppressWarnings("unchecked")
    public void setupMachineBoxes() {
        machineBoxes.clear();
        machineWires.clear();
        removeAll();
        ExposedArrayList<Object> tMachines = document.getChildren("Machines");
        ExposedArrayList<Object> tWires = document.getChildren("Wires");
        if (tMachines != null) {
            for (int x = 0; x < tMachines.length; x++) {
                MachineObj machine = (MachineObj) tMachines.data[x];
                JMachine mbox = new JMachine(machine, this);
                machineBoxes.add(mbox);
                panelLayout.registerForFontSizeChanges(mbox);
                add(mbox, new RelativePlacement(mbox.xLoc, mbox.yLoc, MACHINE_WIDTH, MACHINE_HEIGHT));
            }
        }
        if (tWires != null) {
            for (int x = 0; x < tWires.length; x++) {
                WireObj wire = (WireObj) tWires.data[x];
                JWire wc = new JWire(wire);
                machineWires.add(wc);
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Paints the component by drawing the wires and then making the machine
     * boxes paint themselves over the top of them.
     * @param g The graphics class used for drawing.
     */
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        double dim = Math.max(getWidth(), getHeight());
        double arrowRadius = dim / 80;
        int dotRadius = (int) (dim / 200);
        dotPanelSize = (int) (dim / 20);
        for (int x = 0; x < machineWires.size(); x++) {
            JWire aWire = machineWires.get(x);
            JMachine b1 = findBox(aWire.wire.inputID);
            JMachine b2 = findBox(aWire.wire.outputID);
            if (b1 != null && b2 != null) {
                drawWire(b1, b2, g, arrowRadius, aWire);
            }
        }
        super.paintChildren(g);
        if (selectedWire != null) {
            g.setColor(getBackground());
            g.fillRect(dotPanelX - dotPanelSize, dotPanelY - dotPanelSize, dotPanelSize * 2, dotPanelSize * 2);
            g.setColor(getForeground());
            g.drawRect(dotPanelX - dotPanelSize - 1, dotPanelY - dotPanelSize - 1, dotPanelSize * 2 + 2, dotPanelSize * 2 + 2);
            g.drawLine(dotPanelX - dotPanelSize, dotPanelY, dotPanelX + dotPanelSize, dotPanelY);
            g.drawLine(dotPanelX, dotPanelY - dotPanelSize, dotPanelX, dotPanelY + dotPanelSize);
            Point dotPos = getDotPos(selectedWire.wire.amplitudeL, selectedWire.wire.amplitudeR);
            g.setColor(dotColor);
            g.fillOval(dotPanelX + dotPos.x - dotRadius, dotPanelY + dotPos.y - dotRadius, dotRadius * 2, dotRadius * 2);
        }
        if (newWireMachine != null) {
            int x1 = newWireMachine.getX() + newWireMachine.getWidth() / 2;
            int y1 = newWireMachine.getY() + newWireMachine.getHeight() / 2;
            int x2 = newWireLocX - getLocationOnScreen().x;
            int y2 = newWireLocY - getLocationOnScreen().y;
            g.setColor(getForeground());
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public Point getDotPos(float ampL, float ampR) {
        float biggest = Math.max(ampL, ampR);
        float smallest = Math.min(ampL, ampR);
        float dbAmount = (float) Math.log10(biggest);
        float panAmount = (biggest - smallest) / biggest;
        if (ampL > ampR) panAmount = 0.0f - panAmount;
        int posY = (int) (-dbAmount * dotPanelSize / JWirePanel.dotPanelRange);
        int posX = (int) (panAmount * dotPanelSize);
        return new Point(posX, posY);
    }

    /**
     * We override paintChildren and do nothing since we will call the superclass
     * to do this when it suits us.
     */
    @Override
    public void paintChildren(Graphics g) {
    }

    private void drawWire(JMachine b1, JMachine b2, Graphics g, double arrowRadius, JWire aWire) {
        int x1 = b1.getX() + b1.getWidth() / 2;
        int y1 = b1.getY() + b1.getHeight() / 2;
        int x2 = b2.getX() + b2.getWidth() / 2;
        int y2 = b2.getY() + b2.getHeight() / 2;
        g.setColor(getForeground());
        g.drawLine(x1, y1, x2, y2);
        int cx = (x1 + x2) / 2;
        int cy = (y1 + y2) / 2;
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angle = 0.0;
        angle = Math.atan2(dy, dx);
        int px1 = (int) (cx + Math.cos(angle) * arrowRadius);
        int py1 = (int) (cy + Math.sin(angle) * arrowRadius);
        int px2 = (int) (cx + Math.cos(angle + DEG120) * arrowRadius);
        int py2 = (int) (cy + Math.sin(angle + DEG120) * arrowRadius);
        int px3 = (int) (cx + Math.cos(angle - DEG120) * arrowRadius);
        int py3 = (int) (cy + Math.sin(angle - DEG120) * arrowRadius);
        Polygon p = new Polygon(new int[] { px1, px2, px3 }, new int[] { py1, py2, py3 }, 3);
        g.setColor(wireColors[aWire.wire.channel]);
        g.fillPolygon(p);
        aWire.poly = p;
    }

    /**
     * Called from the menu listener to add a machine to the composition.
     */
    public void addMachine(String newMachName, int xLoc, int yLoc) {
        int newID = (int) document.getNewID(true);
        setMachineLocation(newID, xLoc, yLoc, true);
        router.addMachine(newMachName, newMachName + " " + newID, newID, false, true, true);
    }

    /**
     * Called from JMachineBox to delete a machine from the composition.
     */
    public void removeMachine(MachineObj machine) {
        router.removeMachine(machine.machineID);
    }

    /**
     * Called from JMachineBox when the box is clicked and the mouse is dragged
     * while the user holds Ctrl. This causes a line to be drawn from the box
     * to the mouse indicating the creation of a new wire.
     */
    public void setNewWire(JMachine mb, int xOnScreen, int yOnScreen) {
        newWireMachine = mb;
        newWireLocX = xOnScreen;
        newWireLocY = yOnScreen;
        repaint();
    }

    /**
     * Called from JMachine when the user releases the mouse button
     * after having started drawing a new wire. If the mouse is released
     * over a different machine, the two machines will be connected.
     */
    public void unsetNewWire() {
        if (newWireMachine == null) return;
        for (int x = 0; x < machineBoxes.size(); x++) {
            JMachine jmb = machineBoxes.get(x);
            int x1 = jmb.getLocationOnScreen().x;
            int y1 = jmb.getLocationOnScreen().y;
            int x2 = x1 + jmb.getWidth();
            int y2 = y1 + jmb.getHeight();
            if (newWireLocX >= x1 && newWireLocX <= x2 && newWireLocY >= y1 && newWireLocY <= y2) {
                if (jmb != newWireMachine) router.addWire(newWireMachine.machine.machineID, jmb.machine.machineID, (int) document.getNewID(true), 1.0f, 1.0f, 0, true);
            }
        }
        newWireMachine = null;
        repaint();
    }

    /**
     * Called from the mouse listeners to remove a wire from the audio chain.
     */
    public void removeWire(WireObj wire) {
        router.removeWire(wire.wireID);
    }

    private JMachine findBox(int machineID) {
        for (int x = 0; x < machineBoxes.size(); x++) {
            JMachine t = machineBoxes.get(x);
            if (t.machine.machineID == machineID) return t;
        }
        return null;
    }

    private static final long serialVersionUID = -1L;

    public void setWireVolume(WireObj wire, float left, float right) {
        router.setWireVolume(wire.wireID, left, right);
        wire.amplitudeR = right;
        wire.amplitudeL = left;
    }

    public void setWireChannel(WireObj wire, int chan) {
        router.setWireChannel(wire.wireID, chan);
        wire.channel = chan;
    }

    public void setMachineLocation(int machineID, int xLoc, int yLoc, boolean setSaveFlag) {
        document.setValueImp("MachineBoxes/" + machineID + "/X", new Long(xLoc), setSaveFlag);
        document.setValueImp("MachineBoxes/" + machineID + "/Y", new Long(yLoc), setSaveFlag);
    }

    @Override
    public void run() {
        JWirePanelTask updateCursorTask = new JWirePanelTask(this);
        updateCursorTask.setPriority(Thread.MIN_PRIORITY);
        while (isRunning) {
            try {
                if (isVisible()) {
                    SwingUtilities.invokeAndWait(updateCursorTask);
                    Thread.sleep(PCWindow.SHORT_WAIT);
                } else {
                    Thread.sleep(PCWindow.LONG_WAIT);
                }
            } catch (Exception ex) {
            }
        }
        logger.log(Level.INFO, "WirePanel thread exiting");
    }

    protected void animateBoxes(boolean redraw) {
        for (int x = 0; x < machineBoxes.size(); x++) {
            JMachine mach = machineBoxes.get(x);
            mach.vuMeter(redraw);
        }
    }

    void soloMachine(MachineObj machine) {
        for (int x = 0; x < machineBoxes.size(); x++) {
            JMachine box = machineBoxes.get(x);
            box.setMute(true);
        }
        ArrayList<JMachine> tempMach = new ArrayList<JMachine>();
        recursiveWockyDoove(findMachine(machine.machineID), tempMach);
        for (int x = 0; x < tempMach.size(); x++) {
            JMachine box = tempMach.get(x);
            box.setMute(false);
        }
    }

    private void recursiveWockyDoove(JMachine machine, ArrayList<JMachine> tempMach) {
        if (tempMach.contains(machine)) return;
        tempMach.add(machine);
        for (int x = 0; x < machineWires.size(); x++) {
            JWire wire = machineWires.get(x);
            if (wire.wire.inputID == machine.machine.machineID) {
                recursiveWockyDoove(findMachine(wire.wire.outputID), tempMach);
            }
        }
    }

    private JMachine findMachine(int outputID) {
        for (int x = 0; x < machineBoxes.size(); x++) {
            JMachine box = machineBoxes.get(x);
            if (box.machine.machineID == outputID) return box;
        }
        return null;
    }

    public void unmuteAll() {
        for (int x = 0; x < machineBoxes.size(); x++) {
            JMachine box = machineBoxes.get(x);
            box.setMute(false);
        }
    }
}

/**
 * Used by our timer method as a way to invoke calls to the pTable on a regular
 * basis to update the playback cursor positions using the Swing thread.
 */
class JWirePanelTask extends Thread {

    private JWirePanel pTable;

    private int counter = 0;

    /**
     * Constructs the task, pointing back to the table.
     * @param table
     */
    JWirePanelTask(JWirePanel table) {
        pTable = table;
    }

    @Override
    public void run() {
        pTable.animateBoxes(counter++ % 2 == 0);
    }
}
