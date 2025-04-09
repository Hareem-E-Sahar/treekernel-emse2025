package com.koozi;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * This class handles screenshots of the map
 * @author Sven-Ove Bjerkan
 */
public class Screenshot {

    private String folderpath;

    public Screenshot() {
        Settings settings = Settings.getInstance();
        this.folderpath = settings.getProjectPath() + "/Skjermdumper/";
    }

    /**
     * Multi-monitor support
     * @return number of monitors
     */
    public static int getNumScreens() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            GraphicsDevice[] gs = ge.getScreenDevices();
            int numScreens = gs.length;
            return numScreens;
        } catch (HeadlessException e) {
        }
        return -1;
    }

    /**
     * Take the screenshot
     * @param screenNo if multiple monitors, specify which one to use
     * @param filename filename to save to
     * @return true on success
     */
    public boolean takeScreenshot(int screenNo, String filename) {
        File f = new File(folderpath);
        if (!f.isDirectory() && !f.mkdir()) {
            return false;
        }
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            Robot robot = new Robot(screens[screenNo - 1]);
            Rectangle screenRectangle = screens[screenNo - 1].getDefaultConfiguration().getBounds();
            screenRectangle.x = 0;
            screenRectangle.y = 0;
            BufferedImage image = robot.createScreenCapture(screenRectangle);
            int height = (int) (screenRectangle.height * 0.86);
            BufferedImage dest = image.getSubimage(0, (int) (screenRectangle.height * 0.135), screenRectangle.width, height);
            return ImageIO.write(dest, "png", new File(folderpath + filename));
        } catch (Exception ex) {
            Logger.getLogger(Screenshot.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
