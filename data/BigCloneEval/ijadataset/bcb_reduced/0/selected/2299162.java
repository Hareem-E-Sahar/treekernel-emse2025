package spambuster.gui;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import spambuster.util.ResourceHandler;

/**
 * @author Marc Fritsche
 *
 * This class displays an about-window with information about the
 * version, programmer and thanks!
 * To make the illusion of e semi-transparent window we make a screenshot
 * of the background and then add a picture with alpha-blending on it.
 */
public class AboutView extends JWindow implements MouseListener {

    /**
	 * Image which will be added over the background
	 */
    private ImageIcon ghost;

    /**
	 * Backgroundimage of the screen
	 */
    private BufferedImage screen;

    /**
	 * AlphaComposite to add both pictures with alpha-blending
	 */
    private AlphaComposite composite;

    /**
	 * Constructor to create a new AboutView object.
	 * It creates the gui.
	 */
    public AboutView() {
        ghost = ResourceHandler.getImage("note.png");
        this.addMouseListener(this);
        this.getContentPane().setLayout(null);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle rect = gc.getBounds();
        this.setSize(ghost.getIconWidth(), ghost.getIconHeight());
        Toolkit kit = Toolkit.getDefaultToolkit();
        this.setLocation(((int) kit.getScreenSize().getWidth() - this.getWidth()) / 2, ((int) kit.getScreenSize().getHeight() - this.getHeight()) / 2);
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e1) {
        }
        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
        screen = robot.createScreenCapture(new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight()));
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
	 * When the user clicks on the window it disappears.
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
    public void mouseClicked(MouseEvent arg0) {
        this.dispose();
    }

    /**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
    public void mouseEntered(MouseEvent arg0) {
    }

    /**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
    public void mouseExited(MouseEvent arg0) {
    }

    /**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
    public void mousePressed(MouseEvent arg0) {
    }

    /**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
    public void mouseReleased(MouseEvent arg0) {
    }
}
