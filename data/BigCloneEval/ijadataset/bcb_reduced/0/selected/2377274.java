package net.sf.escripts.utilities;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
* The class {@link ScreenDumper} takes a snapshot of a screen area and saves it in a file.
*
* @author raner
* @version $Revision: 70 $
**/
public class ScreenDumper implements Runnable {

    private static final int SECONDS = 1000;

    private Rectangle bounds;

    private File file;

    private int delay;

    private ScreenDumper(Rectangle bounds, File file, int delay) {
        this.bounds = bounds;
        this.delay = delay;
        this.file = file;
    }

    /**
    * Saves a screen shot. This method is asynchronous and will return immediately, even if there
    * was a delay specified. The actual snapshot will be executed in a separate thread.
    *
    * @param bounds the rectangular area of the screen that is to be saved
    * @param file the file in which the screen shot is saved (the extension determines the file
    * format)
    * @param delay the delay, in seconds, after which the screen shot should be taken
    *
    * @author raner
    **/
    public static void saveScreenShot(Rectangle bounds, File file, int delay) {
        new Thread(new ScreenDumper(bounds, file, delay)).start();
    }

    /**
    * Implements the separate thread in which the screen shot is actually taken. This method is
    * for internal use of the {@link ScreenDumper} class and cannot be called by client code
    * (because the {@link ScreenDumper} class cannot be instantiated by clients).
    *
    * @see java.lang.Runnable#run()
    *
    * @author raner
    **/
    public void run() {
        try {
            Thread.sleep(delay * SECONDS);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(bounds);
            String filename = file.getName();
            int lastDot = filename.lastIndexOf('.');
            String extension = filename.substring(lastDot + 1);
            ImageIO.write(image, extension, file);
        } catch (IOException exception) {
            throw new RuntimeException("IOException: " + exception.getMessage());
        } catch (AWTException exception) {
            throw new RuntimeException("AWTException: " + exception.getMessage());
        } catch (InterruptedException interrupt) {
            throw new RuntimeException("interrupt: " + interrupt.getMessage());
        }
    }
}
