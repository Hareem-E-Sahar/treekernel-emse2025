package javafx_20092010_reeks2.samsegers.Screencapturing.AVIpack;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * @author  Samjay
 */
public class Picture {

    /**
     * De rechthoek de ge meegeeft ga em ne jpg-file van
     * maken
     */
    public static File capture(Rectangle rect) throws Exception {
        Robot robot = new Robot();
        BufferedImage img = robot.createScreenCapture(rect);
        File file = File.createTempFile("jfx_screen_capture_retouched", ".jpg");
        ImageIO.write(img, "jpg", file);
        return file;
    }

    /**
 * Ne jpg file van gans u scherm maken
 */
    public static File capture() throws Exception {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRect);
        File file = File.createTempFile("jfx_screen_capture", ".jpg");
        ImageIO.write(image, "jpg", file);
        return file;
    }

    /**
     * Ne jpg file trug geven in de vorm van het scherm van de videoklasse
     */
    public static File capture(Video video) throws Exception {
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(video.getBounds());
        File file = File.createTempFile("jfx_screen_capture", ".jpg");
        ImageIO.write(image, "jpg", file);
        return file;
    }

    /**
     *  hier beslissen we hoe groot ons schermpje mag worden
     *  De volledige screenresolutie kan gebruikt worde..
     */
    public static Dimension getScreenDimension() throws Exception {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return new Dimension((int) toolkit.getScreenSize().getWidth() - 100, (int) toolkit.getScreenSize().getHeight() - 100);
    }
}
