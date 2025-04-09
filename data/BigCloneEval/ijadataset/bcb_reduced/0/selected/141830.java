package cn.houseout.snapscreen;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class SnapScreen extends JDialog {

    private static final long serialVersionUID = 51164061222292847L;

    Dimension screenSize;

    Rectangle screenRect;

    ImageArea ia = null;

    static Robot robot;

    Image snapedImage = null;

    SnapController controller = null;

    public SnapScreen(SnapController controller) {
        this.controller = controller;
        initialize();
    }

    public SnapScreen(JDialog parent, SnapController controller) {
        super(parent);
        this.controller = controller;
        initialize();
    }

    public SnapScreen(JFrame parent, SnapController controller) {
        super(parent);
        this.controller = controller;
        initialize();
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void setController(SnapController controller) {
        this.controller = controller;
    }

    private void initialize() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenRect = new Rectangle(screenSize);
        ia = new ImageArea(this);
        getContentPane().add(ia);
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);
        setUndecorated(true);
        installAction();
        resetSnapScreen();
        this.setAlwaysOnTop(true);
        this.setResizable(false);
    }

    private void installAction() {
        AbstractAction exitAction = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };
        ia.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitAction");
        ia.getActionMap().put("exitAction", exitAction);
    }

    /**
	 * exit SnapScreen.
	 */
    private void exit() {
        getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.dispose();
        this.setVisible(false);
    }

    public void captureAndExit() {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        snapedImage = ia.captureImage();
        if (controller != null) {
            controller.notify((BufferedImage) snapedImage);
        }
        this.dispose();
        this.setVisible(false);
    }

    public Image getSnapedImage() {
        return this.snapedImage;
    }

    /**
	 * snap the whole screen, and set it to image panel.
	 */
    public void resetSnapScreen() {
        setVisible(false);
        if (robot == null) {
            try {
                robot = new Robot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BufferedImage screenImage = robot.createScreenCapture(screenRect);
        setVisible(true);
        ia.setScreenImage(screenImage);
    }
}
