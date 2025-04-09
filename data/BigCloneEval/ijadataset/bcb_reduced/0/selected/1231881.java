package vue;

import javax.imageio.ImageIO;
import javax.swing.JWindow;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends JWindow {

    private static final long serialVersionUID = 1L;

    private BufferedImage image;

    public SplashScreen(String file, long time) {
        super();
        URL url = getClass().getResource(file);
        try {
            image = ImageIO.read(url);
            setSize(new Dimension(image.getWidth(), image.getHeight()));
            setLocationRelativeTo(null);
            setAlwaysOnTop(true);
            setVisible(true);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("probleme avec l URL du splashscreen");
        }
        if (time > 0) {
            TimerTask dispose = new TimerTask() {

                public void run() {
                    dispose();
                }
            };
            Timer timer = new Timer();
            timer.schedule(dispose, time);
            try {
                Thread.sleep(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SplashScreen(String file) {
        this(file, 300);
    }

    public void paint(Graphics g) {
        if (image.getColorModel().hasAlpha()) {
            try {
                Robot robot = new Robot();
                BufferedImage fond = robot.createScreenCapture(getBounds());
                MediaTracker tracker = new MediaTracker(this);
                tracker.addImage(fond, 0);
                tracker.waitForAll();
                g.drawImage(fond, 0, 0, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        g.drawImage(image, 0, 0, null);
    }
}
