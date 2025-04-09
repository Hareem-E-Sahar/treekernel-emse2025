package coyousoft.util;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class ImageHelper {

    /**
     * 获得当前桌面的截图。
     *
     * @return
     */
    public static BufferedImage getCurrentScreen() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.printf("width=%f, height=%f%n", d.getWidth(), d.getHeight());
        BufferedImage img = null;
        try {
            img = new Robot().createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return img;
    }

    public static void main(String[] args) {
    }
}
