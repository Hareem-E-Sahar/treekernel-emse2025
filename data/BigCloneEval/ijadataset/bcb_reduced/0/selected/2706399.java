package org.proteomecommons.MSExpedite.app;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author takis
 */
public class Dissolver extends JComponent implements Runnable {

    protected JFrame frame;

    protected JFrame fullScreenFrame = new JFrame();

    protected int count;

    protected int numOfFrames = 5;

    protected BufferedImage frameBuffer;

    protected BufferedImage screenBuffer;

    protected BufferedImage image;

    private boolean exit = true;

    /** Creates a new instance of Dissolver */
    public Dissolver() {
    }

    public void setNumberOfFrames(int num) {
        numOfFrames = num;
    }

    public int getNumberOfFrames() {
        return numOfFrames;
    }

    public void update(Graphics g) {
        paintComponent(g);
    }

    public void dissolve(JFrame frame, boolean bExit) throws Exception {
        this.frame = frame;
        this.exit = bExit;
        Robot robot = new Robot();
        Rectangle frameRect = frame.getBounds();
        Dimension frameSize = frame.getSize();
        Point p = frame.getLocationOnScreen();
        frameRect = new Rectangle(p.x, p.y, frameSize.width, frameSize.height);
        frameBuffer = robot.createScreenCapture(frameRect);
        frame.setVisible(false);
        frame.dispose();
        System.gc();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRect = new Rectangle(0, 0, screenSize.width, screenSize.height);
        screenBuffer = robot.createScreenCapture(screenRect);
        fullScreenFrame.getContentPane().setLayout(new BorderLayout());
        fullScreenFrame.getContentPane().add(this, BorderLayout.CENTER);
        fullScreenFrame.setUndecorated(true);
        fullScreenFrame.setBounds(0, 0, screenSize.width, screenSize.height);
        fullScreenFrame.setVisible(true);
        new Thread(this).start();
    }

    protected void drawOffScreen() {
        checkOffScreenImage();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.drawImage(screenBuffer, -fullScreenFrame.getX(), -fullScreenFrame.getY(), null);
        Composite oldComp = g2.getComposite();
        Composite fade = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - ((float) count) / numOfFrames);
        g2.setComposite(fade);
        g2.drawImage(frameBuffer, frame.getX(), frame.getY(), null);
        g2.setComposite(oldComp);
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;
        g.drawImage(image, 0, 0, null);
    }

    public void run() {
        try {
            count = 0;
            for (int i = 0; i < numOfFrames; i++) {
                count = i;
                drawOffScreen();
                Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException ex) {
        } finally {
            frame.dispose();
            fullScreenFrame.dispose();
            if (exit) System.exit(0);
        }
    }

    protected void checkOffScreenImage() {
        Dimension d = fullScreenFrame.getSize();
        if (image == null || image.getWidth(null) != d.width || image.getHeight(null) != d.getHeight()) {
            image = (BufferedImage) createImage(d.width, d.height);
            Graphics g = image.getGraphics();
            RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            rh.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
            ((Graphics2D) g).setRenderingHints(rh);
        }
    }
}
