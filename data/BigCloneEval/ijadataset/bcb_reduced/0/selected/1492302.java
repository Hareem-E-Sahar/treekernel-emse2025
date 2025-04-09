package com.elitost.desktop;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ScreenCapture {

    public static void main(String[] args) throws AWTException {
        captureScreen();
    }

    /**
	 * Description :
	 * 
	 * Date : 25 f�vr. 07
	 * 
	 * @author eric
	 * 
	 * @throws AWTException
	 */
    private static void captureScreen() throws AWTException {
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(0, 0, 500, 500));
        File f = new File("image.jpg");
        try {
            ImageIO.write(image, "jpg", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
