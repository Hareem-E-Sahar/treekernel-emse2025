package org.maveryx.util;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import sun.awt.image.ImageFormatException;

/**
 * Take the screenshot and save it as image file.
 * @author NCuomo
 *
 */
public class ScreenCapturer {

    /**
	 * Default constructor.
	 */
    public ScreenCapturer() {
        super();
    }

    /**
	 * Get and save the screenshot at the specified filepath.
	 * @param filepath - the image file path
	 * @param format - the image file format
	 * @return the image file 
	 * @throws ImageFormatException
	 */
    public File printScreeshot(String filepath) throws ImageFormatException {
        try {
            return printScreenshotAsJPG(filepath);
        } catch (ImageFormatException e) {
            try {
                return printScreenshotAsPNG(filepath);
            } catch (ImageFormatException e1) {
                try {
                    return printScreenshotAsGIF(filepath);
                } catch (ImageFormatException e2) {
                    return printScreenshotAsBitmap(filepath);
                }
            }
        }
    }

    /**
	 * Get and save the screenshot at the specified filepath.
	 * @param filepath - the image file path
	 * @param format - the image file format
	 * @return the image file 
	 * @throws ImageFormatException
	 */
    public File printScreeshot(String filepath, String format) throws ImageFormatException {
        try {
            return writeImageAs(filepath, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * Get and save the screenshot as JPG at the specified filepath.
	 * @param filepath - the image file path
	 * @return the image file
	 * @throws ImageFormatException 
	 */
    public File printScreenshotAsJPG(String filepath) throws ImageFormatException {
        return printScreeshot(filepath, "jpg");
    }

    /**
	 * Get and save the screenshot as BMP at the specified filepath.
	 * @param filepath - the image file path
	 * @return the image file
	 * @throws ImageFormatException 
	 */
    public File printScreenshotAsBitmap(String filepath) throws ImageFormatException {
        return printScreeshot(filepath, "bmp");
    }

    /**
	 * Get and save the screenshot as GIF at the specified filepath.
	 * @param filepath - the image file path
	 * @return the image file
	 * @throws ImageFormatException 
	 */
    public File printScreenshotAsGIF(String filepath) throws ImageFormatException {
        return printScreeshot(filepath, "gif");
    }

    /**
	 * Get and save the screenshot as PNG at the specified filepath.
	 * @param filepath - the image file path
	 * @return the image file
	 * @throws ImageFormatException 
	 */
    public File printScreenshotAsPNG(String filepath) throws ImageFormatException {
        return printScreeshot(filepath, "png");
    }

    /**
	 * Save a screenshot in the specified format at the given filepath.
	 * The pathname = {filepath + ImageFilePrefix + DateTime + .format}
	 * @param filepath - the image file path
	 * @param format - the image file format
	 * @return the image file
	 * @throws ImageFormatException 
	 * @throws IOException 
	 */
    private File writeImageAs(String filepath, String format) throws ImageFormatException, IOException {
        String fileName = buildFilepath(filepath, format);
        BufferedImage bufferedImage = getScreenImage(getScreenSize());
        File imageFile = new File(fileName);
        ImageIO.write(bufferedImage, format, imageFile);
        return imageFile;
    }

    /**
	 * Check whether the image file format is valid.
	 * @param format - the image file format (e.g. BMP, GIF, JPG, JPEG, ...)
	 * @return the file name with the coorrect extension 
	 * @throws ImageFormatException 
	 */
    private String buildFilepath(String filepath, String format) throws ImageFormatException {
        boolean found = false;
        String[] formatNames = ImageIO.getWriterFormatNames();
        String ext = "";
        for (int n = 0; n < formatNames.length; n++) {
            String formatName = formatNames[n].toLowerCase();
            String extTmp = format.toLowerCase();
            if (formatName.equals(extTmp)) {
                found = true;
                ext = extTmp;
            }
            if (filepath.toLowerCase().endsWith("." + extTmp)) {
                filepath = filepath.substring(0, filepath.lastIndexOf("."));
            }
        }
        if (!found) {
            throw new ImageFormatException("Invalid image format: " + format);
        }
        return filepath + "." + ext;
    }

    /**
	 * Get the current screen size.
	 * @return
	 */
    private Rectangle getScreenSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle rectangle = new Rectangle(0, 0, screenSize.width, screenSize.height);
        return rectangle;
    }

    /**
	 * Get the current screenshot.
	 * @param rectangle - the screen size
	 * @return
	 */
    private BufferedImage getScreenImage(Rectangle rectangle) {
        Robot robot = null;
        BufferedImage bufferedImage = null;
        try {
            robot = new Robot();
            bufferedImage = robot.createScreenCapture(rectangle);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }
}
