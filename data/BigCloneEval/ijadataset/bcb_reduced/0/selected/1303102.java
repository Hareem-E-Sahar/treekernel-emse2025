package nk.services;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * @author <a href="mailto:nad7ir@yahoo.com">Alin NISTOR</a>
 * @version 1.0 
 *  date: 28.10.2008 
 */
public class ScreenShot {

    private static int ORDER = 1;

    public static File takeAPictureAndSaveIt(String path, int wait) {
        try {
            Thread.sleep(wait);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            String imagefile = "img" + ORDER++ + ".gif";
            File file = new File(path + "/" + imagefile);
            ImageIO.write(image, "gif", file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
