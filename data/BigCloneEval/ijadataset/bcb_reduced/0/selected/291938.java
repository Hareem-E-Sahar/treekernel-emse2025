package jhomenet.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JWindow;
import javax.swing.*;

/**
 * @author $Author$
 * @version $Revision$
 * Filename: $Source$
 * Description:
 */
public class SplashScreen extends JWindow {

    /***
     * Serial version ID information - used for the serialization process.
     */
    private static final long serialVersionUID = 00001;

    private static SplashScreen instance;

    private BufferedImage splash = null;

    private static String filename;

    /**
     * Default constructor.
     *
     * @param parent Parent frame
     */
    private SplashScreen(JFrame parent) {
        super(parent);
        displaySplash();
    }

    /**
     * Opens the splash window.
     */
    public static void splash() {
        splash("splash.jpg");
    }

    /**
     * Opens the splash window given the image filename.
     *
     * @param imageName Image filename
     */
    public static void splash(String imageName) {
        SplashScreen.filename = imageName;
        JFrame parent = new JFrame();
        if (instance == null) {
            instance = new SplashScreen(parent);
        }
    }

    /**
     * Closes the splash window.
     */
    public static void disposeSplash() {
        if (instance != null) {
            instance.getOwner().dispose();
            instance = null;
        }
    }

    private void displaySplash() {
        try {
            BufferedImage image = ImageIO.read(new File("images/" + filename));
            createShadowPicture(image);
            setVisible(true);
        } catch (IOException ioe) {
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
        }
    }

    public void paint(Graphics g) {
        if (splash != null) {
            g.drawImage(splash, 0, 0, null);
        }
    }

    private void createShadowPicture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int extra = 14;
        setSize(new Dimension(width + extra, height + extra));
        setLocationRelativeTo(null);
        Rectangle windowRect = getBounds();
        splash = new BufferedImage(width + extra, height + extra, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) splash.getGraphics();
        try {
            Robot robot = new Robot(getGraphicsConfiguration().getDevice());
            BufferedImage capture = robot.createScreenCapture(new Rectangle(windowRect.x, windowRect.y, windowRect.width + extra, windowRect.height + extra));
            g2.drawImage(capture, null, 0, 0);
        } catch (AWTException e) {
        }
        BufferedImage shadow = new BufferedImage(width + extra, height + extra, BufferedImage.TYPE_INT_ARGB);
        Graphics g = shadow.getGraphics();
        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.3f));
        g.fillRoundRect(6, 6, width, height, 12, 12);
        g2.drawImage(shadow, getBlurOp(7), 0, 0);
        g2.drawImage(image, 0, 0, this);
    }

    private ConvolveOp getBlurOp(int size) {
        float[] data = new float[size * size];
        float value = 1 / (float) (size * size);
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }
        return new ConvolveOp(new Kernel(size, size, data));
    }
}
