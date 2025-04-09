package jsynoptic.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import simtools.ui.MenuResourceBundle;
import simtools.ui.ResourceFinder;

/**
 * 
 * JSynoptic splash screen is created by JSynoptic Run class before the
 * JSynoptic Panel displays.
 * 
 * Only a single instance of this class can exist, and it may be obtained using the {@link #getSplashScreen()}
 * @author zxpletran007
 *
 */
public class JSynopticSplashSreen extends JWindow {

    public static MenuResourceBundle resources = ResourceFinder.getMenu(JSynopticSplashSreen.class);

    public static final String JSYNOPTIC_VERSION = "Version: " + Run.fullProductVersion;

    public static final String JSYNOPTIC_COPYRIGHT = "� 2009 EADS Astrium. All rights reserved";

    protected URL imageURL;

    protected BufferedImage splashBufferedImage;

    protected JProgressBar progressBar;

    protected String currentMessage;

    protected Font messageFont;

    protected SplashPanel jsSplashPanel;

    protected JSynopticSplashSreen() {
        URL url = resources.getClass().getResource(resources.getString("splash"));
        try {
            setImageURL(url);
            this.messageFont = new Font("Arial", 0, 11);
            this.progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            setProgressBarPosition(0);
            progressBar.setMaximum(100);
            progressBar.setBorder(null);
            jsSplashPanel = new SplashPanel();
            getContentPane().add(jsSplashPanel);
        } catch (IllegalStateException e) {
            System.err.print("Unable to load image " + url);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.print("Unable to load image " + url);
            e.printStackTrace();
        }
    }

    public void setProgressBarPosition(int position) {
        progressBar.setValue(position);
    }

    public void setMessage(String message) {
        currentMessage = message;
        jsSplashPanel.repaint();
    }

    public void setImageURL(URL imageURL) throws IOException, IllegalStateException {
        this.imageURL = imageURL;
        BufferedImage image;
        image = ImageIO.read(imageURL);
        int width = image.getWidth();
        int height = image.getHeight();
        int extra = 31;
        setSize(new Dimension(width, height + extra));
        setLocationRelativeTo(null);
        Rectangle windowRect = getBounds();
        splashBufferedImage = new BufferedImage(width, height + extra, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) splashBufferedImage.getGraphics();
        try {
            Robot robot = new Robot(getGraphicsConfiguration().getDevice());
            BufferedImage capture = robot.createScreenCapture(new Rectangle(windowRect.x, windowRect.y, windowRect.width + extra, windowRect.height + extra));
            g2.drawImage(capture, null, 0, 0);
        } catch (AWTException e) {
        }
        g2.drawImage(image, 0, 0, this);
    }

    public synchronized URL getImageURL() {
        return imageURL;
    }

    private class SplashPanel extends JPanel {

        public void paint(Graphics g) {
            if (splashBufferedImage != null) {
                g.drawImage(splashBufferedImage, 0, 0, null);
                g.setColor(new Color(255, 255, 255, 180));
                g.setFont(messageFont);
                if (currentMessage != null) {
                    g.drawString(currentMessage, 27, 315);
                }
                g.drawString(JSYNOPTIC_COPYRIGHT, 27, 330);
                g.drawString(JSYNOPTIC_VERSION, 500, 315);
            }
        }
    }
}
