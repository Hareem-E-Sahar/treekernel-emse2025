package org.ladybug.gui.utils;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.ladybug.utils.Constants;
import org.ladybug.utils.ValidateExpression;

/**
 * Offers a collection of image effects.
 * 
 * @author Aurelian Pop
 */
public class ImageEffects {

    /**
     * Blurs an image using the Gaussian distribution to achieve best results. It performs very well on small images
     * like icons or banners, however this method does not scale well, so for large images there is a performance
     * penalty. If you encounter performance issues with some large images, please consider
     * {@link #blurLargeImage(BufferedImage, int)} instead. This method provides the best possible blurring quality.
     * <p/>
     * The resulting image is slightly larger in dimensions than the original image. Therefore, extra attention needs to
     * be taken when displaying this image, if alignment is important. The resulted image's size is given by the
     * formula:
     * <p/>
     * <code>
     * resultWidth = originalWidth + 4 * radius;<br/>
     * resultHeight = originalHeight 4 * radius;
     * </code>
     * <p/>
     * The coordinates of the original picture in the blurred image, can be computed by following the algorithm below:
     * <p/>
     * <code>
     * resultPosX = 2 * radius;<br/>
     * resultPosY = 2 * radius;
     * </code>
     * 
     * @param image
     *            the image to be blurred
     * @param radius
     *            the radius to be used for the blurring effect. The bigger the radius, the more blurred the result is
     * @return The blurred image.
     */
    public static BufferedImage blurSmallImage(final BufferedImage image, final int radius) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        ValidateExpression.isFalse(radius < 1, "Radius has to be minimum 1");
        final BufferedImage tempImage = new BufferedImage(image.getWidth() + 4 * radius, image.getHeight() + 4 * radius, image.getType());
        final Graphics2D g2d = tempImage.createGraphics();
        g2d.drawImage(image, 2 * radius, 2 * radius, null);
        g2d.dispose();
        final BufferedImage horizontallyBlurredImage = createGaussianBlurFilter(radius, true).filter(tempImage, null);
        return createGaussianBlurFilter(radius, false).filter(horizontallyBlurredImage, null);
    }

    /**
     * Blurs an image using the Gaussian distribution, with a performance gain on large images. For small images (like
     * icons or banners), please use the {@link #blurSmallImage(BufferedImage, int)} method. This method has a small
     * penalty to image quality, however for large images it should be unnoticeable to the human eye.
     * <p/>
     * This method does a performance trick: it first scales down the image by 50 percent before the blurring operation
     * (by calling the {@link #scale(BufferedImage, int, int)} with a ratio of 1/2), then it blurs the reduced image by
     * calling {@link #blurSmallImage(BufferedImage, int)}, and then it scales back the blurred image to the original
     * size. This means that this call makes a scale-blur-scale operation.
     * <p/>
     * The performance comes from the fact the scaling is done on a 50% ratio (which the
     * {@link #scale(BufferedImage, int, int)} method can handle more efficiently), then it performs the expensive
     * Gaussian blur on an image a quarter of the size of the original image, and then it performs another relatively
     * cheap up-scaling operation by a factor of 2.
     * <p/>
     * This method should not be used for blurring small pictures, because the 2 scaling operations offset the
     * performance gain in blurring the reduced image, causing worst blurring effects with more CPU cycles spent. As
     * general rule, use the {@link #blurSmallImage(BufferedImage, int)} whenever possible, and only when necessary use
     * this method.
     * <p/>
     * The resulting image is slightly larger in dimensions than the original image (see
     * {@link #blurSmallImage(BufferedImage, int)}). However, because of the scaling operation, the size increase is
     * twice as much, and is given by the formula:
     * <p/>
     * <code>
     * resultWidth = originalWidth + 8 * radius;<br/>
     * resultHeight = originalHeight 8 * radius;
     * </code>
     * <p/>
     * The coordinates of the original picture in the blurred image, can be computed by following the algorithm below:
     * <p/>
     * <code>
     * resultPosX = 4 * radius;<br/>
     * resultPosY = 4 * radius;
     * </code>
     * 
     * @param image
     *            the image to be blurred
     * @param radius
     *            the radius to be used for the blurring effect. The bigger the radius, the more blurred the result is
     * @return The blurred image.
     */
    public static BufferedImage blurLargeImage(final BufferedImage image, final int radius) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        ValidateExpression.isFalse(radius < 1, "Radius has to be minimum 1");
        BufferedImage tempImage = scale(image, image.getWidth() / 2, image.getHeight() / 2);
        tempImage = blurSmallImage(tempImage, radius);
        return scale(tempImage, 2 * tempImage.getWidth(), 2 * tempImage.getHeight());
    }

    /**
     * Cropping an image. The returned image is a section of the provided image. This is useful when trying to extract a
     * subsection of an image.
     * 
     * @param image
     *            the image from which the crop operation will be performed
     * @param leftX
     *            the X coordinate of the top-left corner of the cropped section (in image coordinates)
     * @param topY
     *            the Y coordinate of the top-left corner of the cropped section (in image coordinates)
     * @param rightX
     *            the X coordinate of the bottom-right corner of the cropped section (in image coordinates)
     * @param bottomY
     *            the Y coordinate of the bottom-right corner of the cropped section (in image coordinates)
     * @return the cropped image
     */
    public static BufferedImage crop(final BufferedImage image, final int leftX, final int topY, final int rightX, final int bottomY) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        ValidateExpression.isFalse(leftX < 0, "The \"leftX\" parameter cannot be negative!");
        ValidateExpression.isFalse(topY < 0, "The \"topY\" parameter cannot be negative!");
        ValidateExpression.isFalse(rightX > image.getWidth() - 1, "The \"rightX\" parameter cannot be larger than the image width!");
        ValidateExpression.isFalse(bottomY > image.getHeight() - 1, "The \"bottomY\" parameter cannot be larger than the image height!");
        ValidateExpression.isTrue(leftX <= rightX, "The \"leftX\" parameter has to be less than or equal to the \"rightX\" parameter!");
        ValidateExpression.isTrue(topY <= bottomY, "The \"topY\" parameter has to be less than or equal to the \"bottomY\" parameter!");
        final BufferedImage result = new BufferedImage(rightX - leftX, bottomY - topY, image.getType());
        final Graphics2D g2d = result.createGraphics();
        g2d.drawImage(image, -leftX, -topY, null);
        g2d.dispose();
        return result;
    }

    /**
     * Changes the alpha index of an image. In other words, it can be considered that it "fades" the image.
     * 
     * @param image
     *            the image to be faded
     * @param alpha
     *            the alpha index to be applied. This has to be within the [0.0, 1.0] range. A value of 0 means
     *            invisible, and a value of 1 means perfectly visible.
     * @return The faded image.
     */
    public static BufferedImage fade(final BufferedImage image, final float alpha) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        ValidateExpression.isTrue((alpha >= 0.0f) && (alpha <= 1.0f), "The \"aplpha parameter has to be in the [0.0, 1.0] interval");
        final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        final Graphics2D g2d = result.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return result;
    }

    /**
     * Produces glow for an image. If displayed under the image, it gives an impression of glow to it. A "glowing image"
     * can be considered as a blurred version of the original image, with the color tinted to increase the impression of
     * glowing (light from within).
     * 
     * @param image
     *            the image that needs glowing
     * @param glowColor
     *            the color of the glow
     * @param glowRadius
     *            this is used for creating a blurred image
     * @return The glow of the image.
     */
    public static BufferedImage glow(final BufferedImage image, final Color glowColor, final int glowRadius) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        final BufferedImage result = blurSmallImage(image, glowRadius);
        return new ColorTintFilter(glowColor, 1.0f).filter(result, null);
    }

    /**
     * Outlines the text for better readability.
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
        ValidateExpression.isNotNull(g2d, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "g2d");
        ValidateExpression.isNotNull(fontMetrics, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "fontMetrics");
        ValidateExpression.isFalse(outlineThickness == 0, "The \"depthSize3D\" parameter cannot be zero to get any kind of contour effect.");
        for (int x = -outlineThickness; x <= outlineThickness; x++) {
            for (int y = -outlineThickness; y <= outlineThickness; y++) {
                if ((x != 0) || (y != 0)) {
                    g2d.drawString(text, textPosition.x + outlineThickness + x, textPosition.y + outlineThickness + y + fontMetrics.getMaxAscent() - 1);
                }
            }
        }
    }

    /**
     * Mirrors an image.
     * 
     * @param image
     *            the image to be mirrored
     * @param orientation
     *            the orientation of the mirroring
     * @return The mirrored image.
     */
    public static BufferedImage mirror(final BufferedImage image, final Orientation orientation) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        final Graphics2D g2d = result.createGraphics();
        double mirrorX = 1D;
        double mirrorY = 1D;
        int offsetX = 0;
        int offsetY = 0;
        switch(orientation) {
            case Horizontal:
                mirrorX = -1D;
                offsetX = image.getWidth();
                break;
            case Vertical:
                mirrorY = -1D;
                offsetY = image.getHeight();
                break;
        }
        g2d.translate(offsetX, offsetY);
        g2d.scale(mirrorX, mirrorY);
        g2d.drawImage(image, 0, 0, null);
        g2d.scale(mirrorX, mirrorY);
        g2d.dispose();
        return result;
    }

    /**
     * Reflects an image. The reflection can be blurred and may be faded to simulate different reflection surfaces.
     * Because of this, when blurred, the reflected image size will be greater than the original image. Please see
     * {@link ImageEffects#blurSmallImage(BufferedImage, int)} for more details.
     * 
     * @param image
     *            the source image
     * @param direction
     *            the direction where the reflective surface is located relative to the image
     * @param strongReflection
     *            the alpha index of the reflection closest to the image
     * @param weakReflection
     *            the alpha index of the reflection farthest away from the image
     * @param fadeRatio
     *            the fraction of the original image that should be reflected
     * @param glossiness
     *            a number specifying how glossy (or shiny) the reflective surface is. The less glossy it is, the more
     *            unclear (blurred) the reflection it makes is (0 means ideal glossiness, mirror quality)
     * @return The reflection of the image.
     */
    public static BufferedImage reflect(final BufferedImage image, final Direction direction, final float strongReflection, final float weakReflection, final float fadeRatio, final int glossiness) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        int sizeX = 0;
        int sizeY = 0;
        int gradX = 0;
        int gradY = 0;
        int transX = 0;
        int transY = 0;
        double scaleX = 0.0;
        double scaleY = 0.0;
        Orientation orientation = null;
        switch(direction) {
            case Up:
                sizeX = image.getWidth();
                sizeY = (int) (image.getHeight() * fadeRatio);
                gradX = 0;
                gradY = sizeY - 1;
                scaleX = 1.0;
                scaleY = -1.0;
                transX = 0;
                transY = -sizeY;
                orientation = Orientation.Vertical;
                break;
            case Down:
                sizeX = image.getWidth();
                sizeY = (int) (image.getHeight() * fadeRatio);
                gradX = 0;
                gradY = sizeY - 1;
                scaleX = 1.0;
                scaleY = 1.0;
                transX = 0;
                transY = 0;
                orientation = Orientation.Vertical;
                break;
            case Left:
                sizeX = (int) (image.getWidth() * fadeRatio);
                sizeY = image.getHeight();
                gradX = sizeX - 1;
                gradY = 0;
                scaleX = -1.0;
                scaleY = 1.0;
                transX = -sizeX;
                transY = 0;
                orientation = Orientation.Horizontal;
                break;
            case Right:
                sizeX = (int) (image.getWidth() * fadeRatio);
                sizeY = image.getHeight();
                gradX = sizeX - 1;
                gradY = 0;
                scaleX = 1.0;
                scaleY = 1.0;
                transX = 0;
                transY = 0;
                orientation = Orientation.Horizontal;
                break;
        }
        BufferedImage reflectedImage = mirror(image, orientation);
        if (glossiness > 0) {
            reflectedImage = blurSmallImage(reflectedImage, glossiness);
        }
        final BufferedImage result = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = result.createGraphics();
        g2d.scale(scaleX, scaleY);
        g2d.translate(transX, transY);
        g2d.drawImage(reflectedImage, 0, 0, null);
        g2d.setPaint(new GradientPaint(0, 0, new Color(1F, 1F, 1F, strongReflection), gradX, gradY, new Color(1F, 1F, 1F, weakReflection)));
        g2d.setComposite(AlphaComposite.DstIn);
        g2d.fillRect(0, 0, result.getWidth(), result.getHeight());
        g2d.dispose();
        return result;
    }

    /**
     * Scales images using bilinear interpolation progressively. If the desired size (on either X or Y axis) of the
     * image is smaller than 50%, then a 50% scale is done first on that particular axis, and the method is recursively
     * called (until doing a less than 50% scale). Apparently this scaling method has the best quality/performance
     * ratio.
     * 
     * @param image
     *            the image to be scaled
     * @param desiredWidth
     *            the desired width after scaling
     * @param desiredHeight
     *            the desired height after scaling
     * @return The scaled image.
     */
    public static BufferedImage scale(final BufferedImage image, final int desiredWidth, final int desiredHeight) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        int tempDesiredWidth = desiredWidth;
        int tempDesiredHeight = desiredHeight;
        if ((float) tempDesiredWidth / image.getWidth(null) < 0.5f) {
            tempDesiredWidth = image.getWidth(null) / 2;
        }
        if ((float) tempDesiredHeight / image.getHeight(null) < 0.5f) {
            tempDesiredHeight = image.getHeight(null) / 2;
        }
        final BufferedImage scaledBI = new BufferedImage(tempDesiredWidth, tempDesiredHeight, image.getType());
        final Graphics2D g2d = scaledBI.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(image, 0, 0, tempDesiredWidth, tempDesiredHeight, null);
        g2d.dispose();
        if ((tempDesiredWidth != desiredWidth) || (tempDesiredHeight != desiredHeight)) {
            return scale(scaledBI, desiredWidth, desiredHeight);
        }
        return scaledBI;
    }

    /**
     * Creates the shadow of an image. The shadow can be crisp or blurred. Please remember that in case the shadow is
     * blurred, the image size is bigger than the original. For more information, see
     * {@link #blurSmallImage(BufferedImage, int)}
     * 
     * @param image
     *            the original (non-shadowed image)
     * @param blurRadius
     *            the blurring factor of the shadow. The bigger this number is, the more diffuse the shadow is. When 0,
     *            the shadow is very sharp.
     * @return The image and its shadow.
     */
    public static BufferedImage shadow(final BufferedImage image, int blurRadius) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        if (blurRadius < 0) {
            blurRadius = 0;
        }
        BufferedImage shadow = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = shadow.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.drawImage(image, 0, 0, null);
        if (blurRadius > 0) {
            g2d.setComposite(AlphaComposite.SrcIn);
        } else {
            g2d.setComposite(AlphaComposite.SrcIn.derive(0.5F));
        }
        g2d.fillRect(0, 0, shadow.getWidth(), shadow.getHeight());
        g2d.dispose();
        if (blurRadius > 0) {
            shadow = blurSmallImage(shadow, blurRadius);
        }
        return shadow;
    }

    /**
     * Converts an image to a <code>BufferedImage</code>.
     * 
     * @param image
     *            the image to be converted
     * @param opaque
     *            whether the result image should be considered opaque or not
     * @return The converted image
     */
    public static BufferedImage toBufferedImage(final Image image, final boolean opaque) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        final BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null), opaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = result.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return result;
    }

    /**
     * Converts this image to a screen-compatible image.
     * @param image
     * @param opaque
     * @return
     */
    public static BufferedImage toCompatibleImage(final Image image, final boolean opaque) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        BufferedImage result = null;
        try {
            int transparency = Transparency.BITMASK;
            if (opaque) {
                transparency = Transparency.OPAQUE;
            }
            result = Constants.SCREEN.getDefaultConfiguration().createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (final HeadlessException e) {
        }
        if (result == null) {
            int type = BufferedImage.TYPE_INT_ARGB;
            if (opaque) {
                type = BufferedImage.TYPE_INT_RGB;
            }
            result = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        return result;
    }

    /**
     * <p>
     * Returns an array of pixels, stored as integers, from a <code>BufferedImage</code>. The pixels are grabbed from a
     * rectangular area defined by a location and two dimensions. Calling this method on an image of type different from
     * <code>BufferedImage.TYPE_INT_ARGB</code> and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.
     * </p>
     * 
     * @param image
     *            the source image
     * @param x
     *            the x location at which to start grabbing pixels
     * @param y
     *            the y location at which to start grabbing pixels
     * @param w
     *            the width of the rectangle of pixels to grab
     * @param h
     *            the height of the rectangle of pixels to grab
     * @param pixels
     *            a preallocated array of pixels of size w*h; can be null
     * @return <code>pixels</code> if non-null, a new array of integers otherwise
     * @throws IllegalArgumentException
     *             is <code>pixels</code> is non-null and of length &lt; w*h
     */
    public static int[] getPixels(final BufferedImage image, final int x, final int y, final int w, final int h, int[] pixels) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        if ((w == 0) || (h == 0)) {
            return new int[0];
        }
        if (pixels == null) {
            pixels = new int[w * h];
        } else {
            ValidateExpression.isFalse(pixels.length < w * h, "pixels array must have a length >= w*h");
        }
        final int imageType = image.getType();
        if ((imageType == BufferedImage.TYPE_INT_ARGB) || (imageType == BufferedImage.TYPE_INT_RGB)) {
            final Raster raster = image.getRaster();
            return (int[]) raster.getDataElements(x, y, w, h, pixels);
        }
        return image.getRGB(x, y, w, h, pixels, 0, w);
    }

    /**
     * <p>
     * Writes a rectangular area of pixels in the destination <code>BufferedImage</code>. Calling this method on an
     * image of type different from <code>BufferedImage.TYPE_INT_ARGB</code> and <code>BufferedImage.TYPE_INT_RGB</code>
     * will unmanage the image.
     * </p>
     * 
     * @param image
     *            the destination image
     * @param x
     *            the x location at which to start storing pixels
     * @param y
     *            the y location at which to start storing pixels
     * @param w
     *            the width of the rectangle of pixels to store
     * @param h
     *            the height of the rectangle of pixels to store
     * @param pixels
     *            an array of pixels, stored as integers
     * @throws IllegalArgumentException
     *             is <code>pixels</code> is non-null and of length &lt; w*h
     */
    public static void setPixels(final BufferedImage image, final int x, final int y, final int w, final int h, final int[] pixels) {
        ValidateExpression.isNotNull(image, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        if ((pixels == null) || (w == 0) || (h == 0)) {
            return;
        } else {
            ValidateExpression.isFalse(pixels.length < w * h, "pixels array must have a length >= w*h");
        }
        final int imageType = image.getType();
        if ((imageType == BufferedImage.TYPE_INT_ARGB) || (imageType == BufferedImage.TYPE_INT_RGB)) {
            final WritableRaster raster = image.getRaster();
            raster.setDataElements(x, y, w, h, pixels);
        } else {
            image.setRGB(x, y, w, h, pixels, 0, w);
        }
    }

    /**
     * Returns an image containing the screen-shot for the specified boundaries
     * 
     * @param bounds
     *            the boundaries of the screen-shot
     * @return The on-screen image located within the provided boundaries
     * @throws AWTException
     */
    public static BufferedImage getScreenCapture(final Rectangle bounds) throws AWTException {
        ValidateExpression.isNotNull(bounds, ValidateExpression.ARGUMENT_CANNOT_BE_NULL_TEMPLATE, "image");
        return new Robot().createScreenCapture(bounds);
    }

    /**
     * Creates a Gaussian blur filter based on a one-dimensional array.
     * 
     * @param radius
     *            the radius on which the blurring effect should be done
     * @param horizontal
     *            the direction on which the blurring should be done. If <tt>true</tt>, the direction is horizontal, if
     *            <tt>false</tt>, the direction will be vertical
     * @return A {@link ConvolveOp} object corresponding to the desired filtering.
     */
    private static ConvolveOp createGaussianBlurFilter(final int radius, final boolean horizontal) {
        final int size = radius * 2 + 1;
        final float[] array = new float[size];
        final float sigma = radius / 3.0F;
        final float twoSiqmaSquare = 2.0F * sigma * sigma;
        final float sigmaRoot = (float) Math.sqrt(twoSiqmaSquare * Math.PI);
        float total = 0.0f;
        for (int i = -radius; i <= radius; i++) {
            final float distance = i * i;
            final int index = i + radius;
            array[index] = (float) Math.exp(-distance / twoSiqmaSquare) / sigmaRoot;
            total += array[index];
        }
        for (float element : array) {
            element /= total;
        }
        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(array.length, 1, array);
        } else {
            kernel = new Kernel(1, array.length, array);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
}
