package org.hfbk.ui;

import java.awt.AWTException;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.TextField;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.hfbk.util.HTTPUtils;
import org.hfbk.vis.Prefs;

/** some UI themed utilities, eg. look-and-feel helpers and screenshots*/
public class UIUtils {

    /**
	 * give some UI the required vis look.
	 */
    public static void blackify(Component c) {
        if (System.getProperty("os.name").equals("Mac OS X") && (c instanceof TextField || c instanceof Button)) {
            c.setForeground(Color.BLACK);
            c.setBackground(Color.WHITE);
        } else {
            c.setForeground(Color.WHITE);
            c.setBackground(Color.BLACK);
        }
        if (c instanceof Container) for (Component c2 : ((Container) c).getComponents()) blackify(c2);
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        if (higherQuality) {
            w = img.getWidth();
            h = img.getHeight();
        } else {
            w = targetWidth;
            h = targetHeight;
        }
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }
            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();
            ret = tmp;
        } while (w != targetWidth || h != targetHeight);
        return ret;
    }

    /** 
	 * do a screen shot covering the given Component.
	 * */
    public static void screenshot(Component c) {
        Rectangle screenRect = c.getBounds();
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        long t = System.currentTimeMillis();
        String outFileName = Prefs.current.screenshotdir + "/shot" + t + ".png";
        BufferedImage image = robot.createScreenCapture(screenRect);
        try {
            ImageIO.write(image, "png", new File(outFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Saved screen shot (" + image.getWidth() + " x " + image.getHeight() + " pixels) to file \"" + outFileName + "\".");
        if (Prefs.current.screenshotupload) {
            int w = image.getWidth();
            int h = image.getHeight();
            if ((w > 800) | (h > 625)) {
                if (w > h) {
                    h = (int) Math.floor((800f / w) * h);
                    w = 800;
                } else {
                    w = (int) Math.floor((625f / h) * w);
                    h = 625;
                }
            }
            BufferedImage smallimage = getScaledInstance(image, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            String tempFileName = Prefs.current.screenshotdir + "/" + t + ".jpg";
            try {
                ImageIO.write(smallimage, "jpg", new File(tempFileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            HTTPUtils.SubmitPicture(tempFileName, "");
        }
    }
}
