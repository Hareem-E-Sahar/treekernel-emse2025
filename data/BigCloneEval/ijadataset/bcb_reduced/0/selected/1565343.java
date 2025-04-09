package si.mk.k3.view3d;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.apache.log4j.Logger;
import si.mk.k3.Configuration;
import si.mk.k3.MainController;
import si.mk.k3.gui.I18N;
import si.mk.k3.kbrowser.SFFloat;
import si.mk.k3.kbrowser.SFTime;
import si.mk.k3.kbrowser.X3DFieldEvent;
import si.mk.k3.kbrowser.X3DFieldEventListener;
import si.mk.k3.kbrowser.X3DNode;
import si.mk.k3.model.K3Element;
import si.mk.k3.model.K3Model;
import si.mk.k3.util.K13DPrinter;
import si.mk.k3.util.K3Util;
import si.mk.k3.view3d.K3Browser.PrintAction;

public class Library3D {

    static Logger m_logger = Logger.getLogger(Library3D.class);

    private BrowserFrame m_browserFrame;

    protected SelectionEventListener m_selectionEventlistener;

    protected X3DNode m_lastSelectedMaterial;

    public static final float TRANSPARENCY_OF_SELECTED = 0.7F;

    public static final float TRANSPARENCY_OF_UNSELECTED = 1F;

    public static final String TOUCH_SENSOR_PREFIX = "TOUCH_SENSOR_";

    public static final String MATERIAL_PREFIX = "MATERIAL_";

    /**
     * Creates 3D window of the specified height and width.
     * @param width width of the window
     * @param height height of the window
     */
    public Library3D(Point location, Dimension size) {
        JMenuBar menuBar = createMenuBar();
        m_browserFrame = new BrowserFrame("Lib", menuBar, null, location, size, false);
        K3Util.setApplicationIcon(m_browserFrame, "icons/KEnaLibrary24.gif");
    }

    /**
     * Currently only one listener may be added. If we add more listeners, 
     * previous one will stop receiveing events.
     * 
     * @param listener
     */
    public void addSelectionListener(SelectionEventListener listener) {
        m_selectionEventlistener = listener;
    }

    public void loadLibrary3D(String fileName, float minX, float maxX, float minY, float maxY, float minZ, float maxZ, float initX, float initY, float initZ) {
        m_browserFrame.setTitle(I18N.str(I18N.K3_LIBRARY_LIBRARY3D_KEnaLib) + ":  " + fileName);
        m_browserFrame.loadWorld(fileName);
        m_browserFrame.setWorldLimits(minX, maxX, minY, maxY, minZ, maxZ);
        m_browserFrame.setViewPoint(initX, initY, initZ);
    }

    public void addTouchSensorsToObjects(K3Model k3Model) {
        k3Model.resetObjectIterator();
        while (k3Model.hasNextObject()) {
            K3Element k3Object = k3Model.nextObject();
            addTouchSensorToElement(k3Object.getId().asString());
        }
    }

    public void addTouchSensorToElement(String elementID) {
        String touchSensorName = TOUCH_SENSOR_PREFIX + elementID;
        X3DNode touchSensor = m_browserFrame.getMainScene().getNamedNode(touchSensorName);
        if (touchSensor != null) {
            SFTime touchTime = (SFTime) touchSensor.getField("touchTime");
            if (touchTime != null) {
                String materialNodeName = MATERIAL_PREFIX + elementID;
                X3DNode material = m_browserFrame.getMainScene().getNamedNode(materialNodeName);
                if (material != null) {
                    touchTime.addX3DEventListener(new TouchEventListener(elementID, material));
                } else {
                    m_logger.error("Couldn't find material node: " + materialNodeName);
                }
            } else {
                m_logger.error("Couldn't find 'touchTime' field for element: " + touchSensorName);
            }
        } else {
            m_logger.error("Couldn't find TouchSensor named: '" + touchSensorName + "'");
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("File operations");
        fileMenu.add(new JMenuItem(new CloseAction()));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(new PrintAction()));
        fileMenu.getPopupMenu().setLightWeightPopupEnabled(false);
        menuBar.add(fileMenu);
        return menuBar;
    }

    /**
     * This clas will implement menu buttons placed directly to the memu bar,
     * each button will move to a viewpoint predefined in the library files. For 
     * example in model properties, si.kena.std.viewPt_1, si.kena.std.viewPt_2,
     * si.kena.std.viewPt_3, ...
     */
    class MoveToPosAction extends AbstractAction {

        MoveToPosAction(double x, double y, double z, double theta, double phi) {
            super("Move");
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("HI!");
        }
    }

    class CloseAction extends AbstractAction {

        CloseAction() {
            super("Close");
        }

        public void actionPerformed(ActionEvent e) {
            m_browserFrame.dispose();
        }

        public boolean isEnabled() {
            return true;
        }
    }

    class PrintAction extends AbstractAction {

        PrintAction() {
            super("Print");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                Rectangle screenRect = m_browserFrame.getScreenLocation();
                Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(screenRect);
                ImageIO.write(image, "png", new File("screen.png"));
                K13DPrinter printer = new K13DPrinter("3D", Configuration.inst().getLogoImageFileName(), image);
                printer.start();
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        public boolean isEnabled() {
            return true;
        }
    }

    class TouchEventListener implements X3DFieldEventListener {

        private String m_id;

        private X3DNode m_material;

        TouchEventListener(String id, X3DNode material) {
            m_id = id;
            m_material = material;
        }

        public void readableFieldChanged(X3DFieldEvent evt) {
            m_logger.debug("Library3D: Touching: " + m_id);
            setTransparency(TRANSPARENCY_OF_SELECTED);
            m_selectionEventlistener.elementSelected(m_id);
        }

        void setTransparency(float t) {
            if (m_lastSelectedMaterial != null) {
                SFFloat transp = (SFFloat) m_lastSelectedMaterial.getField("transparency");
                transp.setValue(TRANSPARENCY_OF_UNSELECTED);
            }
            if (t == TRANSPARENCY_OF_SELECTED) {
                m_lastSelectedMaterial = m_material;
            }
            SFFloat transp = (SFFloat) m_material.getField("transparency");
            transp.setValue(t);
        }
    }
}
