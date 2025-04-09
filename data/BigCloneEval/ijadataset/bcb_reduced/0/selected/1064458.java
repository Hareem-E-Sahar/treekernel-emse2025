package opaqua.ui.components.splashScreen;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * @author Thomas Siegrist
 */
public class MySplashScreen implements ActionListener {

    private final BufferedImage background;

    private final BufferedImage image;

    private BufferedImage currentImage;

    private SplashPainter label;

    private final int speed = 1000 / 20;

    /** Duration in Time Mills: */
    public static final float duration = 1000.0f;

    private long startTime = 0;

    private boolean isBlendIn = true;

    private final Timer timer;

    public MySplashScreen(String path) throws IOException, AWTException {
        final URL url = this.getClass().getResource(path);
        image = ImageIO.read(url);
        currentImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(image.getWidth(null), image.getHeight(null));
        final Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (int) (screenDimension.getWidth() / 2 - image.getWidth(null) / 2);
        final int y = (int) (screenDimension.getHeight() / 2 - image.getHeight(null) / 2);
        final int w = image.getWidth(null);
        final int h = image.getHeight(null);
        final Robot robot = new Robot();
        final Rectangle rectangle = new Rectangle(x, y, w, h);
        background = robot.createScreenCapture(rectangle);
        drawImage(0f);
        label = new SplashPainter();
        label.setImage(background);
        final JWindow f = new JWindow(new JFrame());
        f.getContentPane().add(label);
        f.pack();
        f.setLocationRelativeTo(null);
        timer = new Timer(speed, this);
        timer.setCoalesce(true);
        timer.start();
        startTime = System.currentTimeMillis();
        f.setAlwaysOnTop(true);
        f.setVisible(true);
    }

    public synchronized void blendOut() {
        try {
            this.wait(2000);
        } catch (InterruptedException e) {
        } finally {
            isBlendIn = false;
            startTime = System.currentTimeMillis();
            timer.start();
        }
    }

    public void actionPerformed(ActionEvent e) {
        float percent;
        if (isBlendIn) {
            percent = (System.currentTimeMillis() - startTime) / duration;
            percent = Math.min(1.0f, percent);
        } else {
            percent = (System.currentTimeMillis() - startTime) / (duration * (1 / 10));
            percent = Math.min(1.0f, percent);
            percent = 1.0f - percent;
        }
        float alphaValue = percent;
        if (percent >= 1.0) {
            timer.stop();
        } else if (alphaValue <= 0.0f) {
            timer.stop();
            SwingUtilities.getWindowAncestor(label).dispose();
        }
        drawImage(alphaValue);
        label.setImage(currentImage);
        label.repaint();
    }

    /**
	 * Draws Background, then draws image over it
	 * 
	 * @param alphaValue
	 */
    private void drawImage(float alphaValue) {
        final Graphics2D g2d = (Graphics2D) currentImage.getGraphics();
        g2d.drawImage(background, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
    }

    public static void main(String[] args) throws Exception {
        new MySplashScreen("de/nicedezigns/musicbox/gui/resources/logo_big.png");
    }
}
