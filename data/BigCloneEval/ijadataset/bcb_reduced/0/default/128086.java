import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

class Screenshot {

    public static void takeScreenshot(String fn) {
        try {
            System.out.println("about to take screenshot");
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            System.out.println("done, now writing to disk");
            ImageIO.write(image, "jpg", new File(fn));
            System.out.println("all done");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage:  java Screenshot filename.ext");
            System.out.println("        where ext can be:");
            String[] formatNames = ImageIO.getWriterFormatNames();
            for (int i = 0; i < formatNames.length; i++) {
                System.out.println("          " + formatNames[i]);
            }
        } else {
            String fn = args[0];
            takeScreenshot(fn);
        }
    }
}
