package org.ladybug.gui.toolbox;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.ladybug.utils.Constants;
import org.ladybug.utils.LadybugRuntimeException;
import org.ladybug.utils.validation.ValidateArgument;
import org.ladybug.utils.validation.ValidationTemplate;

/**
 * Offers a collection of image effects.
 * 
 * @author Aurelian Pop
 */
public final class ImageEffects {

    private ImageEffects() {
    }

    /**
     * Outlines the text for better readability
     * 
     * @param g2d
     *            graphical context
     * @param text
     *            the text whose contour to be drawn
     * @param fontMetrics
     *            details of the font
     * @param outlineThickness
     *            the outline thickness value
     * @param textPosition
     *            the position of the text
     */
    public static void outlineText(final Graphics2D g2d, final String text, final FontMetrics fontMetrics, final int outlineThickness, final Point textPosition) {
        ValidateArgument.isNotNull(g2d, ValidationTemplate.NOT_NULL, "g2d");
        ValidateArgument.isNotNull(fontMetrics, ValidationTemplate.NOT_NULL, "fontMetrics");
        ValidateArgument.isFalse(outlineThickness == 0, ValidationTemplate.NON_ZERO, "outlineThickness");
        if (text != null) {
            for (int x = -outlineThickness; x <= outlineThickness; ++x) {
                for (int y = -outlineThickness; y <= outlineThickness; ++y) {
                    if (x != 0 || y != 0) {
                        g2d.drawString(text, textPosition.x + outlineThickness + x, textPosition.y + outlineThickness + y + fontMetrics.getMaxAscent() - 1);
                    }
                }
            }
        }
    }

    /**
     * Creates a compatible image of the specified size and transparency model.
     * 
     * @param width
     *            the width of the created image. Has to be greater than zero.
     * @param height
     *            the height of the created image. Has to be greater than zero.
     * @param transparency
     *            the transparency mode. Has to be one of the OPAQUE, BITMASK or TRANSLUCENT values defined by the
     *            {@link Transparency} type.
     * @return A buffered image that is compatible with the screen
     */
    public static BufferedImage createCompatibleImage(final int width, final int height, final int transparency) {
        return Constants.GC.createCompatibleImage(width, height, transparency);
    }

    /**
     * Loads an image from the file
     * 
     * @param resource
     *            the file where the image is located
     * @return A BufferedImage compatible with the screen.
     */
    public static BufferedImage loadImage(final File resource) {
        try {
            return convertToCompatibleImage(ImageIO.read(resource));
        } catch (final IOException e) {
            throw new LadybugRuntimeException("Could not load image resource " + resource.getAbsolutePath(), e);
        }
    }

    /**
     * Returns an image containing the screen-shot for the specified boundaries
     * 
     * @param bounds
     *            the boundaries of the screen-shot
     * @return The on-screen image located within the provided boundaries.
     * @throws AWTException
     *             in case there are some problems when capturing the screen
     */
    public static BufferedImage getScreenCapture(final Rectangle bounds) throws AWTException {
        ValidateArgument.isNotNull(bounds, ValidationTemplate.NOT_NULL, "image");
        return convertToCompatibleImage(new Robot().createScreenCapture(bounds));
    }

    public static Color blendColors(final Color color1, final Color color2, final double ratio) {
        final int r = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
        final int g = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
        final int b = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
        final int a = (int) (color1.getAlpha() * (1 - ratio) + color2.getAlpha() * ratio);
        return new Color(r, g, b, a);
    }

    /**
     * Converts a {@link BufferedImage} to an RGBA array representation. Useful when serializing images.
     * 
     * @param image
     *            the image to be converted
     * @return An int array containing the RGBA image.
     */
    public static int[] imageToRgbaArray(final BufferedImage image) {
        ValidateArgument.isNotNull(image, ValidationTemplate.NOT_NULL, "image");
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    /**
     * Converts a byte array to a {@link BufferedImage}. Useful when de-serializing images.
     * 
     * @param rawImage
     *            the raw representation of the image
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @return The re-constructed image.
     */
    public static BufferedImage rgbaArrayToImage(final int[] rawImage, final int width, final int height) {
        ValidateArgument.isNotNull(rawImage, ValidationTemplate.NOT_NULL, "rawImage");
        final BufferedImage image = createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        image.setRGB(0, 0, width, height, rawImage, 0, width);
        return image;
    }

    /**
     * Converts an image to one compatible with the screen.
     * 
     * @param image
     *            the image to be converted
     * @return The converted image, compatible with the screen.
     */
    private static BufferedImage convertToCompatibleImage(final BufferedImage image) {
        ValidateArgument.isNotNull(image, ValidationTemplate.NOT_NULL, "image");
        if (Constants.GC.getColorModel().equals(image.getColorModel())) {
            return image;
        }
        final BufferedImage compatibleImage = createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
        final Graphics2D g2d = compatibleImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return compatibleImage;
    }
}
