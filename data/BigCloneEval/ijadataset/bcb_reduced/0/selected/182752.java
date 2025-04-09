package net.xiaoxishu.util.sys;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import org.apache.log4j.Logger;

/**
 * 封装了是否支持抓屏,以及抓屏的方法
 * @author lu
 *
 */
public class ScreenCapture {

    public static final Logger logger = Logger.getLogger(ScreenCapture.class);

    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    public static boolean initFailed;

    private static Robot ROBOT;

    static {
        try {
            ROBOT = new Robot();
        } catch (AWTException e) {
            initFailed = true;
            logger.warn("Failed to init the Robot, so can't capture the screen", e);
        }
    }

    public static Image printScreen() {
        if (initFailed) return null;
        return ROBOT.createScreenCapture(new Rectangle(SCREEN_SIZE));
    }
}
