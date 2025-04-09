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
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import si.mk.k3.Configuration;
import si.mk.k3.util.K13DPrinter;
import si.mk.k3.util.K3Util;

/**
 * This class can be used as a standalone SWING window, which displays 3D
 * worlds. There is also a main() method, which runs this class as a standalone 
 * program.
 *  
 * @author markok
 *
 */
public class K3Browser {

    protected BrowserFrame m_browserFrame;

    private String m_fileName;

    protected boolean m_isHeadlightOn;

    public K3Browser(Point location, Dimension size) {
        m_isHeadlightOn = true;
        JMenuBar menuBar = createMenuBar();
        m_browserFrame = new BrowserFrame("KEna 3D: ", menuBar, null, location, size, true);
        K3Util.setApplicationIcon(m_browserFrame, "icons/KEna3D24.gif");
    }

    public void loadWorld(String fileName) {
        m_browserFrame.setTitle("KEna 3D: " + fileName);
        m_fileName = fileName;
        m_browserFrame.loadWorld(fileName);
        m_browserFrame.setHeadlight(m_isHeadlightOn);
    }

    public void setWorldLimits(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        m_browserFrame.setWorldLimits(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public Point3d getViewPoint() {
        return m_browserFrame.getViewPoint();
    }

    public Vector2d getViewRotation() {
        return m_browserFrame.getViewRotation();
    }

    public void setViewPoint(double initX, double initY, double initZ) {
        m_browserFrame.setViewPoint(initX, initY, initZ);
    }

    public void setViewRotation(double rotX, double rotY) {
        m_browserFrame.setViewRotation(rotX, rotY);
    }

    public void setVisible(boolean flag) {
        m_browserFrame.setVisible(flag);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuOption = new JMenu("File");
        menuOption.setMnemonic(KeyEvent.VK_F);
        menuOption.getAccessibleContext().setAccessibleDescription("File operations");
        menuOption.add(new JMenuItem(new PrintAction()));
        menuOption.getPopupMenu().setLightWeightPopupEnabled(false);
        menuBar.add(menuOption);
        menuOption = new JMenu("Options");
        menuOption.setMnemonic(KeyEvent.VK_O);
        menuOption.getAccessibleContext().setAccessibleDescription("Options");
        menuOption.add(new JMenuItem(new ToggleHeadlightAction()));
        menuOption.getPopupMenu().setLightWeightPopupEnabled(false);
        menuBar.add(menuOption);
        return menuBar;
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

    class ReloadAction extends AbstractAction {

        ReloadAction() {
            super("Reload");
        }

        public void actionPerformed(ActionEvent e) {
            try {
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        public boolean isEnabled() {
            return true;
        }
    }

    class ToggleHeadlightAction extends AbstractAction {

        ToggleHeadlightAction() {
            super("Headlight On/Off: On");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                m_isHeadlightOn = !m_isHeadlightOn;
                m_browserFrame.setHeadlight(m_isHeadlightOn);
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Runs the browser in standalone mode.
     * 
     * @param args if specified, the first item is used as name of the file to 
     * be loaded.
     */
    public static void main(String[] args) {
        K3Browser browserWindow = new K3Browser(new Point(100, 100), new Dimension(1024, 786));
        if (args.length > 0) {
            browserWindow.loadWorld(args[0]);
        }
    }
}
