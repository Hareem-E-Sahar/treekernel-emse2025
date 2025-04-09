package spambuster.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * This class displays an splashscreen with the logo of the application. To make the illusion of e
 * semi-transparent window we make a screenshot of the background and then add a picture with
 * alpha-blending on it.
 * 
 * @author Marc Fritsche
 */
public class SplashScreen extends JWindow implements MouseListener {

    /**
     * Backgroundimage of the screen
     */
    private BufferedImage screen;

    /**
     * Image which will be added over the background
     */
    private ImageIcon ghost;

    /**
     * width, height of source and window
     */
    private int w, h, scrW, scrH;

    /**
     * AlphaComposite to add both pictures with alpha-blending
     */
    private AlphaComposite composite;

    /**
     * Reference to the class implementing the <code>SplashCallable</code> interface
     */
    private SplashCallable callback;

    /**
     * Creates a new <code>SplashScreen</code> with the given icon as a ghost
     * 
     * @param ghost
     *            ImageIcon which will be shown
     * @param callback
     *            Callback interface which will be called
     */
    public SplashScreen(ImageIcon ghost, SplashCallable callback) {
        this.callback = callback;
        this.ghost = ghost;
        this.addMouseListener(this);
        w = ghost.getIconWidth();
        h = ghost.getIconHeight();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle rect = gc.getBounds();
        scrW = rect.width;
        scrH = rect.height;
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e1) {
        }
        int x = (scrW - w) / 2;
        int y = (scrH - h) / 2;
        this.setVisible(true);
        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
        screen = robot.createScreenCapture(new Rectangle(x, y, w, h));
        setLocation(x, y);
        pack();
        try {
            Thread.sleep(3000);
            callback.runApplication();
            this.dispose();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Returns the preferred size of this container.
     * 
     * @return an instance of Dimension that represents the preferred size of this container.
     */
    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    /**
     * Paint method to draw the captured image with its alphacomposite
     * 
     * @param Graphics
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(screen, 0, 0, null);
        g2.setComposite(composite);
        g2.drawImage(ghost.getImage(), 0, 0, null);
    }

    /**
     * This method is called when the user clicks on the screen
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        callback.runApplication();
        this.dispose();
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }
}
