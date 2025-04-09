import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Beber
 */
public class ScreenShotTest {

    public ScreenShotTest() {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            ImageIO.write(image, "jpg", new File("out\\out.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(ScreenShotTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AWTException ex) {
            Logger.getLogger(ScreenShotTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String Args[]) {
        new ScreenShotTest();
    }
}
