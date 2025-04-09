package cn.houseout.jsnapscreen;

import java.awt.Cursor;
import java.awt.Dimension;
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

public class SnapScreenDialog extends JDialog {

    private static final long serialVersionUID = 4047852263991712885L;

    private Dimension screenSize;

    private Rectangle screenRect;

    private SnapPanel snapArea = null;

    private static Robot robot;

    public SnapScreenDialog(JFrame parent, SnapObservable observable) {
        super(parent);
        snapArea = new SnapPanel(this, observable);
        initialize();
        changeCursor(Cursor.CROSSHAIR_CURSOR);
    }

    private void changeCursor(int cursor) {
        setCursor(new Cursor(cursor));
    }

    private void initialize() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenRect = new Rectangle(screenSize);
        getContentPane().add(snapArea);
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
        snapArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitAction");
        snapArea.getActionMap().put("exitAction", exitAction);
    }

    /**
	 * exit SnapScreen.
	 */
    private void exit() {
        changeCursor(Cursor.DEFAULT_CURSOR);
        this.dispose();
        this.setVisible(false);
    }

    /**
	 * snap the whole screen, and set it to image panel.
	 */
    private void resetSnapScreen() {
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
        snapArea.setScreenImage(screenImage);
    }
}
