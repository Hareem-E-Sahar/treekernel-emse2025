package org.base.apps.util.view.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides utility functions for creating <code>ImageIcon</code> s and
 * <code>BufferedImage</code> s for the GUI.
 * <p>
 * This class is an adapted version of the 
 * <a href="https://jaxb-workshop.dev.java.net/source/browse/jaxb-workshop/src/org/jvnet/jaxbw/util/graphics/ImageCreator.java?view=markup">ImageCreator</a> 
 * utility from the <a href="https://jaxb-workshop.dev.java.net/">JAXB-Workshop Project</a> 
 * (and originally found on 
 * <a href="http://weblogs.java.net/blog/kirillcool/archive/2005/02/how_to_create_y.html">Kirill Grouchnikov's blog</a>).
 * It is licensed under a <a href="http://creativecommons.org/licenses/by-nc-sa/2.0/">Creative Commons License</a>.
 * <p>
 * This class has been augmented with a couple additional utility functions to
 * indicate status and some javadoc tags for outdated libraries have been commented out.
 * 
 * @author Kirill Grouchnikov
 * @author Kevan Simpson
 */
public class ImageCreator {

    /**
     * The main light color for the backgrounds.
     */
    public static final Color mainUUltraLightColor = new Color(196, 220, 255);

    /**
     * The main light color for the backgrounds.
     */
    public static final Color mainUltraLightColor = new Color(128, 192, 255);

    /**
     * The main light color for the backgrounds.
     */
    public static final Color mainLightColor = new Color(0, 128, 255);

    /**
     * The main medium color for the backgrounds.
     */
    public static final Color mainMidColor = new Color(0, 64, 196);

    /**
     * The main dark color for the backgrounds.
     */
    public static final Color mainDarkColor = new Color(0, 32, 128);

    /**
     * The main ultra-dark color for the backgrounds.
     */
    public static final Color mainUltraDarkColor = new Color(0, 32, 64);

    /**
     * The color for icons that represent <code>Class</code> entities.
     */
    public static final Color iconClassColor = new Color(128, 255, 128);

    /**
     * The color for icons that represent <code>Annotation</code> entities.
     */
    public static final Color iconAnnotationColor = new Color(141, 112, 255);

    /**
     * The color for icons that represent <code>Field</code> or
     * <code>Method</code> entities.
     */
    public static final Color iconFieldMethodColor = new Color(32, 128, 255);

    /**
     * The color for arrows on icons.
     */
    public static final Color iconArrowColor = new Color(128, 32, 0);

    /**
     * The color for arrows on icons.
     */
    public static final Color errorLightColor = new Color(255, 128, 128);

    /**
     * The default dimension for icons (both width and height).
     */
    public static final int ICON_DIMENSION = 15;

    /**
     * The dimension of cube in
     * {@link #getGradientCubesImage(int, int, Color, Color, int, int)}
     */
    public static final int CUBE_DIMENSION = 5;

    public static final int CLOSE_DIMENSION = 4;

    /**
     * Returns a completely transparent image of specified dimensions.
     * 
     * @param width
     *            Image width.
     * @param height
     *            Image height.
     * @return Completely transparent image of specified dimensions.
     */
    public static BufferedImage getBlankImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                image.setRGB(col, row, 0x0);
            }
        }
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return image;
    }

    /**
     * Returns a colored image of specified dimensions.
     * 
     * @param width
     *            Image width.
     * @param height
     *            Image height.
     * @return Completely transparent image of specified dimensions.
     */
    public static Icon getColorIcon(int width, int height, Color c) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        String strColor = StringUtils.substringAfter(SwingUtil.encodeColor(c), "0x");
        int color = (c == null) ? 0x0 : Integer.parseInt(strColor, 16);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                image.setRGB(col, row, color);
            }
        }
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return new ImageIcon(image);
    }

    /**
     * Returns an image of specified dimensions that contains the specified
     * string in big letters. The resulting image will have gradient background
     * and semi-transparent background for the title. The title will be centered
     * in the center of the image.
     * 
     * @param width
     *            The width of the output image.
     * @param height
     *            The height of the output image.
     * @param title
     *            Title string to write on the output image.
     * @return The resulting image.
     */
    public static Icon getTitleImage(int width, int height, String title, Color foregroundColor, Color shadowColor) {
        BufferedImage image = ImageCreator.getBackgroundImage(width, height, false);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font font = new Font("Arial", Font.PLAIN, height - 8);
        graphics.setFont(font);
        FontMetrics fm = graphics.getFontMetrics();
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout(title, font, frc);
        Rectangle2D bounds = mLayout.getBounds();
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight() + fm.getHeight()) / 2 + 3;
        Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.8);
        graphics.setComposite(c);
        graphics.setColor(shadowColor);
        graphics.drawString(title, (int) x + 1, (int) y + 1);
        c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0);
        graphics.setComposite(c);
        graphics.setColor(foregroundColor);
        graphics.drawString(title, (int) x, (int) y);
        return new ImageIcon(image);
    }

    /**
     * Returns an image of specified dimensions that has gradient striped
     * background.
     * 
     * @param width
     *            The width of the output image.
     * @param height
     *            The height of the output image.
     * @param lightColor
     *            The color of the left side of the even stripes.
     * @param midColor
     *            The color of the right side of the even stripes and the left
     *            side of the odd stripes.
     * @param darkColor
     *            The color of the right side of the odd stripes.
     * @return The resulting image.
     */
    public static BufferedImage getBackgroundStripedImage(int width, int height, Color lightColor, Color midColor, Color darkColor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        GradientPaint gpLightLeft = new GradientPaint(0, 0, lightColor, width / 2, 0, midColor);
        GradientPaint gpLightRight = new GradientPaint(width / 2, 0, midColor, width, 0, lightColor);
        GradientPaint gpDarkLeft = new GradientPaint(0, 0, midColor, width / 2, 0, darkColor);
        GradientPaint gpDarkRight = new GradientPaint(width / 2, 0, darkColor, width, 0, midColor);
        for (int row = 0; row < height; row++) {
            GradientPaint gpLeft = (row % 2 == 0) ? gpLightLeft : gpDarkLeft;
            graphics.setPaint(gpLeft);
            graphics.drawLine(0, row, width / 2, row);
            GradientPaint gpRight = (row % 2 == 0) ? gpLightRight : gpDarkRight;
            graphics.setPaint(gpRight);
            graphics.drawLine(width / 2, row, width, row);
        }
        return image;
    }

    /**
     * Returns an image of specified dimensions that has gradient background and
     * an optional gradient separator stripe on its upper border.
     * 
     * @param width
     *            The width of the output image.
     * @param height
     *            The height of the output image.
     * @param hasStripeTop
     *            if <code>true</code>, a gradient stripe few pixels high is
     *            added on the upper border of this image.
     * @return The resulting image.
     */
    public static BufferedImage getBackgroundImage(int width, int height, boolean hasStripeTop) {
        BufferedImage image = getBlankImage(width, height);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        if (hasStripeTop) {
            for (int col = 0; col < width; col++) {
                float transp = Math.abs((float) (col - width / 2)) / (float) (width / 2);
                transp = Math.min((float) 1.0, (float) (transp * 1.25));
                Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0 - transp);
                graphics.setComposite(c);
                graphics.setColor(Color.black);
                graphics.drawLine(col, 0, col, 2);
            }
        }
        return image;
    }

    /**
     * Returns an image of specified width that contains (possibly) multilined
     * representation of the input string. The resulting image will have a
     * gradient background and semi-transparent background for the text. The
     * input string is broken into a sequence of lines. Each line does not
     * exceed the specified width of the resulting image. The height of the
     * resulting image is computed according to the number of lines.
     * 
     * @param str
     *            The input string to be shown,.
     * @param width
     *            The width of the output image.
     * @return The resulting image.
     */
    public static Icon getMultiline(String str, int width, Color foregroundColor, Color shadowColor) {
        BufferedImage tempImage = new BufferedImage(width, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempGraphics = (Graphics2D) tempImage.getGraphics();
        tempGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        Font font = new Font("Arial", Font.PLAIN, 12);
        tempGraphics.setFont(font);
        FontRenderContext frc = tempGraphics.getFontRenderContext();
        LinkedList<String> lines = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(str, " ", false);
        String currLine = "";
        while (tokenizer.hasMoreTokens()) {
            String currToken = tokenizer.nextToken() + " ";
            String newLine = currLine + currToken;
            TextLayout mLayout = new TextLayout(newLine, font, frc);
            Rectangle2D bounds = mLayout.getBounds();
            if (bounds.getWidth() > (width - 20)) {
                lines.addLast(currLine);
                currLine = currToken;
            } else {
                currLine = newLine;
            }
        }
        lines.addLast(currLine);
        int lineCount = lines.size();
        FontMetrics fm = tempGraphics.getFontMetrics();
        int height = lineCount * (fm.getHeight() + 5);
        BufferedImage image = ImageCreator.getBackgroundImage(width, height, false);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setFont(font);
        int ypos = fm.getHeight();
        for (String line : lines) {
            Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.8);
            graphics.setComposite(c);
            graphics.setColor(shadowColor);
            graphics.drawString(line, 6, ypos + 1);
            c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0);
            graphics.setComposite(c);
            graphics.setColor(foregroundColor);
            graphics.drawString(line, 5, ypos);
            ypos += (fm.getHeight() + 1);
        }
        return new ImageIcon(image);
    }

    /**
     * Returns an icon image with specified background that contains a single
     * letter in its center with optional marker sign that is shown when
     * <code>isCollection</code> parameter is <code>true</code>.
     * 
     * @param letter
     *            The letter to show in the center of the icon. This letter is
     *            capitalized if it was not capitalized.
     * @param isCollection
     *            If <code>true</code>, a special marker sign is shown in the
     *            upper-right portion of the resulting image icon.
     * @param backgroundColor
     *            The background color for the resulting image icon.
     * @return An icon image with specified background that contains a single
     *         letter in its center with optional marker sign that is shown when
     *         <code>isCollection</code> parameter is <code>true</code>.
     */
    public static BufferedImage getSingleLetterImage(char letter, boolean isCollection, Color backgroundColor) {
        return getSingleLetterImage(letter, Color.black, isCollection, backgroundColor);
    }

    /**
     * Returns an icon image with specified background that contains a single
     * letter in its center with optional marker sign that is shown when
     * <code>isCollection</code> parameter is <code>true</code>.
     * 
     * @param letter
     *            The letter to show in the center of the icon. This letter is
     *            capitalized if it was not capitalized.
     * @param foregroundColor
     *            The foreground color for drawing the letter.
     * @param isCollection
     *            If <code>true</code>, a special marker sign is shown in the
     *            upper-right portion of the resulting image icon.
     * @param backgroundColor
     *            The background color for the resulting image icon.
     * @return An icon image with specified background that contains a single
     *         letter in its center with optional marker sign that is shown when
     *         <code>isCollection</code> parameter is <code>true</code>.
     */
    public static BufferedImage getSingleLetterImage(char letter, Color foregroundColor, boolean isCollection, Color backgroundColor) {
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        letter = Character.toUpperCase(letter);
        graphics.setFont(new Font("Arial", Font.BOLD, 10));
        float x = (float) ((ICON_DIMENSION - Math.ceil(graphics.getFontMetrics().charWidth(letter))) / 2);
        float y = ICON_DIMENSION - (float) 2.0 - (float) ((ICON_DIMENSION - graphics.getFontMetrics().getStringBounds("" + letter, graphics).getHeight()) / 2);
        graphics.setColor(backgroundColor);
        graphics.fillOval(0, 0, ICON_DIMENSION - 1, ICON_DIMENSION - 1);
        double id4 = ICON_DIMENSION / 4.0;
        double spotX = id4;
        double spotY = id4;
        for (int col = 0; col < ICON_DIMENSION; col++) {
            for (int row = 0; row < ICON_DIMENSION; row++) {
                double dx = col - spotX;
                double dy = row - spotY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > ICON_DIMENSION) {
                    dist = ICON_DIMENSION;
                }
                int currColor = image.getRGB(col, row);
                int transp = (currColor >>> 24) & 0xFF;
                int oldR = (currColor >>> 16) & 0xFF;
                int oldG = (currColor >>> 8) & 0xFF;
                int oldB = (currColor >>> 0) & 0xFF;
                double coef = 0.9 - 0.9 * dist / ICON_DIMENSION;
                int dr = 255 - oldR;
                int dg = 255 - oldG;
                int db = 255 - oldB;
                int newR = (int) (oldR + coef * dr);
                int newG = (int) (oldG + coef * dg);
                int newB = (int) (oldB + coef * db);
                int newColor = (transp << 24) | (newR << 16) | (newG << 8) | newB;
                image.setRGB(col, row, newColor);
            }
        }
        graphics.drawOval(0, 0, ICON_DIMENSION - 1, ICON_DIMENSION - 1);
        graphics.setColor(foregroundColor);
        graphics.drawString("" + letter, x, y);
        if (isCollection) {
            int xp = ICON_DIMENSION - 6;
            int yp = 3;
            graphics.setColor((foregroundColor.equals(Color.white)) ? new Color(235, 235, 235, 128) : foregroundColor);
            graphics.drawLine(xp - 1, yp - 2, xp + 6, yp - 2);
            graphics.drawLine(xp - 1, yp - 1, xp + 6, yp - 1);
            graphics.drawLine(xp - 1, yp, xp + 6, yp);
            graphics.drawLine(xp - 1, yp + 1, xp + 6, yp + 1);
            graphics.drawLine(xp + 1, yp - 4, xp + 1, yp + 3);
            graphics.drawLine(xp + 2, yp - 4, xp + 2, yp + 3);
            graphics.drawLine(xp + 3, yp - 4, xp + 3, yp + 3);
            graphics.drawLine(xp + 4, yp - 4, xp + 4, yp + 3);
            graphics.setColor(foregroundColor);
            graphics.drawLine(xp, yp - 1, xp + 5, yp - 1);
            graphics.drawLine(xp, yp, xp + 5, yp);
            graphics.drawLine(xp + 2, yp - 3, xp + 2, yp + 2);
            graphics.drawLine(xp + 3, yp - 3, xp + 3, yp + 2);
        }
        return image;
    }

    /**
     * Returns an icon image with specified background that contains a single
     * letter in its center with optional marker sign that is shown when
     * <code>isCollection</code> parameter is <code>true</code>.
     * 
     * @param letter
     *            The letter to show in the center of the icon. This letter is
     *            capitalized if it was not capitalized.
     * @param isCollection
     *            If <code>true</code>, a special marker sign is shown in the
     *            upper-right portion of the resulting image icon.
     * @param backgroundColor
     *            The background color for the resulting image icon.
     * @return An icon image with specified background that contains a single
     *         letter in its center with optional marker sign that is shown when
     *         <code>isCollection</code> parameter is <code>true</code>.
     */
    public static Icon getSingleLetterIcon(char letter, boolean isCollection, Color backgroundColor) {
        return getSingleLetterIcon(letter, Color.black, isCollection, backgroundColor);
    }

    /**
     * Returns an icon image with specified background that contains a single
     * letter in its center with optional marker sign that is shown when
     * <code>isCollection</code> parameter is <code>true</code>.
     * 
     * @param letter
     *            The letter to show in the center of the icon. This letter is
     *            capitalized if it was not capitalized.
     * @param isCollection
     *            If <code>true</code>, a special marker sign is shown in the
     *            upper-right portion of the resulting image icon.
     * @param backgroundColor
     *            The background color for the resulting image icon.
     * @return An icon image with specified background that contains a single
     *         letter in its center with optional marker sign that is shown when
     *         <code>isCollection</code> parameter is <code>true</code>.
     */
    public static Icon getSingleLetterIcon(char letter, Color foregroundColor, boolean isCollection, Color backgroundColor) {
        return new ImageIcon(getSingleLetterImage(letter, foregroundColor, isCollection, backgroundColor));
    }

    /**
     * Returns an icon image with specified background that contains a single
     * letter in its center, an arrow in the bottom portion of the icon and an
     * optional marker sign that is shown when <code>isCollection</code>
     * parameter is <code>true</code>.
     * 
     * @param letter
     *            The letter to show in the center of the icon. This letter is
     *            capitalized if it was not capitalized.
     * @param isCollection
     *            If <code>true</code>, a special marker sign is shown in the
     *            upper-right portion of the resulting image icon.
     * @param backgroundColor
     *            The background color for the resulting image icon.
     * @param isArrowToRight
     *            If <code>true</code>, the arrow will point to the right,
     *            otherwise the arrow will point to the left.
     * @param arrowColor
     *            The color of the arrow.
     * @return An icon image with specified background that contains a single
     *         letter in its center, an arrow in the bottom portion of the icon
     *         and an optional marker sign that is shown when
     *         <code>isCollection</code> parameter is <code>true</code>.
     */
    public static Icon getLetterIconWithArrow(char letter, boolean isCollection, Color backgroundColor, boolean isArrowToRight, Color arrowColor) {
        BufferedImage image = getSingleLetterImage(letter, isCollection, backgroundColor);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        BufferedImage arrowImage = getArrowImage(arrowColor, new Color(255, 255, 255, 196), ICON_DIMENSION, isArrowToRight);
        graphics.drawImage(arrowImage, 0, ICON_DIMENSION - arrowImage.getHeight(), null);
        return new ImageIcon(image);
    }

    /**
     * Returns an image that contains a right-pointing arrow of specified width
     * and color.
     * 
     * @param color
     *            Arrow color.
     * @param width
     *            Arrow width.
     * @return An image that contains a right-pointing arrow of specified width
     *         and color.
     */
    public static BufferedImage getRightArrow(Color color, Color haloColor, int width) {
        int height = 6;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Polygon pol = new Polygon();
        int ya = 3;
        pol.addPoint(1, ya);
        pol.addPoint(width / 2 + 3, ya);
        pol.addPoint(width / 2 + 3, ya + 2);
        pol.addPoint(width - 1, ya);
        pol.addPoint(width / 2 + 3, ya - 2);
        pol.addPoint(width / 2 + 3, ya);
        graphics.setColor(color);
        graphics.drawPolygon(pol);
        BufferedImage fimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                fimage.setRGB(col, row, 0x0);
            }
        }
        Graphics2D fgraphics = (Graphics2D) fimage.getGraphics();
        int haloOpacity = haloColor.getAlpha();
        for (int col = 0; col < width; col++) {
            int xs = Math.max(0, col - 1);
            int xe = Math.min(width - 1, col + 1);
            for (int row = 0; row < height; row++) {
                int ys = Math.max(0, row - 1);
                int ye = Math.min(height - 1, row + 1);
                int currColor = image.getRGB(col, row);
                int opacity = (int) (haloOpacity * ((currColor >>> 24) & 0xFF) / 255.0);
                if (opacity > 0) {
                    for (int x = xs; x <= xe; x++) {
                        for (int y = ys; y <= ye; y++) {
                            int oldOpacity = (fimage.getRGB(x, y) >>> 24) & 0xFF;
                            int newOpacity = Math.max(oldOpacity, opacity);
                            int newColor = (newOpacity << 24) | (255 << 16) | (255 << 8) | 255;
                            fimage.setRGB(x, y, newColor);
                        }
                    }
                }
            }
        }
        fgraphics.drawImage(image, 0, 0, null);
        return fimage;
    }

    /**
     * Returns an image that contains a left-pointing arrow of specified width
     * and color.
     * 
     * @param arrowColor
     *            Arrow color.
     * @param width
     *            Arrow width.
     * @return An image that contains a left-pointing arrow of specified width
     *         and color.
     */
    public static BufferedImage getLeftArrow(Color arrowColor, Color haloColor, int width) {
        BufferedImage rimage = getRightArrow(arrowColor, haloColor, width);
        int height = rimage.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                image.setRGB(col, row, rimage.getRGB(width - 1 - col, row));
            }
        }
        return image;
    }

    /**
     * Returns an image that contains an arrow of specified width, direction and
     * color.
     * 
     * @param arrowColor
     *            Arrow color.
     * @param width
     *            Arrow width.
     * @param isArrowToRight
     *            If <code>true</code>, the arrow is pointing to the right,
     *            otherwise the arrow is pointing to the left.
     * @return An image that contains a left-pointing arrow of specified width
     *         and color.
     */
    public static BufferedImage getArrowImage(Color arrowColor, Color haloColor, int width, boolean isArrowToRight) {
        if (isArrowToRight) {
            return getRightArrow(arrowColor, haloColor, width);
        } else {
            return getLeftArrow(arrowColor, haloColor, width);
        }
    }

    /**
     * Creates a one-pixel high gradient image of specified width, opacity and
     * colors of the starting pixel and the ending pixel.
     * 
     * @param width
     *            The width of the resulting image.
     * @param leftColor
     *            The color of the first pixel of the resulting image.
     * @param rightColor
     *            The color of the last pixel of the resulting image.
     * @param opacity
     *            The opacity of the resulting image (in 0..1 range). The
     *            smaller the value, the more transparent the resulting image
     *            is.
     * @return A one-pixel high gradient image of specified width, opacity and
     *         colors of the starting pixel and the ending pixel.
     */
    public static BufferedImage createGradientLine(int width, Color leftColor, Color rightColor, double opacity) {
        BufferedImage image = new BufferedImage(width, 1, BufferedImage.TYPE_INT_ARGB);
        int iOpacity = (int) (255 * opacity);
        for (int col = 0; col < width; col++) {
            double coef = (double) col / (double) width;
            int r = (int) (leftColor.getRed() + coef * (rightColor.getRed() - leftColor.getRed()));
            int g = (int) (leftColor.getGreen() + coef * (rightColor.getGreen() - leftColor.getGreen()));
            int b = (int) (leftColor.getBlue() + coef * (rightColor.getBlue() - leftColor.getBlue()));
            int color = (iOpacity << 24) | (r << 16) | (g << 8) | b;
            image.setRGB(col, 0, color);
        }
        return image;
    }

    /**
     * Returns a gutter marker of specified dimensions and theme color. The
     * resulting image is a rectangle with dark border and light filling. The
     * colors of the border and the filling are created based on the input
     * color.
     * 
     * @param themeColor
     *            Base color for border and filling.
     * @param width
     *            Gutter marker width.
     * @param height
     *            Gutter marker height.
     * @return Gutter marker of specified dimensions and theme color.
     */
    public static BufferedImage getGutterMarker(Color themeColor, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        int rd = themeColor.getRed() / 2;
        int gd = themeColor.getGreen() / 2;
        int bd = themeColor.getBlue() / 2;
        Color darkColor = new Color(rd, gd, bd);
        int rl = 255 - (255 - themeColor.getRed()) / 4;
        int gl = 255 - (255 - themeColor.getGreen()) / 4;
        int bl = 255 - (255 - themeColor.getBlue()) / 4;
        Color lightColor = new Color(rl, gl, bl);
        graphics.setColor(lightColor);
        graphics.fillRect(0, 0, width - 1, height - 1);
        graphics.setColor(darkColor);
        graphics.drawRect(0, 0, width - 1, height - 1);
        return image;
    }

    /**
     * Returns a square gutter status image of specified dimensions and theme
     * color. The resulting image is a square with gradient filling and dark and
     * light borders.
     * 
     * @param themeColor
     *            Base color for borders and filling.
     * @param dimension
     *            Width and height of the resulting image.
     * @return Square gutter status image of specified dimensions and theme
     *         color
     */
    public static BufferedImage getGutterStatusImage(Color themeColor, int dimension) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        int rud = themeColor.getRed() / 4;
        int gud = themeColor.getGreen() / 4;
        int bud = themeColor.getBlue() / 4;
        Color ultraDarkColor = new Color(rud, gud, bud);
        int rd = themeColor.getRed() / 2;
        int gd = themeColor.getGreen() / 2;
        int bd = themeColor.getBlue() / 2;
        Color darkColor = new Color(rd, gd, bd);
        int rl = 255 - (255 - themeColor.getRed()) / 4;
        int gl = 255 - (255 - themeColor.getGreen()) / 4;
        int bl = 255 - (255 - themeColor.getBlue()) / 4;
        Color lightColor = new Color(rl, gl, bl);
        GradientPaint gradient = new GradientPaint(0, 0, lightColor, dimension, dimension, darkColor);
        graphics.setPaint(gradient);
        Rectangle rect = new Rectangle(dimension, dimension);
        graphics.fill(rect);
        graphics.setColor(ultraDarkColor);
        graphics.drawLine(0, 0, dimension - 1, 0);
        graphics.drawLine(0, 0, 0, dimension - 1);
        graphics.setColor(lightColor);
        graphics.drawLine(0, dimension - 1, dimension - 1, dimension - 1);
        graphics.drawLine(dimension - 1, 1, dimension - 1, dimension - 1);
        return image;
    }

    /**
     * Creates <code>close</code> image icon of specified dimension.
     * 
     * @param dimension
     *            The dimension of the resulting square icon.
     * @return The resulting icon.
     */
    public static Icon getCloseIcon(int dimension) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(0, 0, ImageCreator.mainUltraLightColor, dimension, dimension, Color.white);
        graphics.setPaint(gradient);
        Rectangle rect = new Rectangle(dimension, dimension);
        graphics.fill(rect);
        graphics.setColor(Color.black);
        graphics.drawRect(0, 0, dimension - 1, dimension - 1);
        graphics.setStroke(new BasicStroke(2.0f));
        graphics.setColor(ImageCreator.mainMidColor);
        graphics.drawLine(3, 3, dimension - 4, dimension - 4);
        graphics.drawLine(3, dimension - 4, dimension - 4, 3);
        return new ImageIcon(image);
    }

    /**
     * Creates <code>close</code> image icon of specified dimension.
     * 
     * @param dimension
     *            The dimension of the resulting square icon.
     * @return The resulting icon.
     */
    public static Icon getCloseIcon(int dimension, char letter) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(dimension, 0, ImageCreator.mainDarkColor, 0, dimension, ImageCreator.mainUltraDarkColor);
        graphics.setPaint(gradient);
        Font font = new Font("Arial", Font.BOLD, dimension - 2);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        letter = Character.toUpperCase(letter);
        int charWidth = graphics.getFontMetrics().charWidth(letter);
        int x = dimension - charWidth - 2;
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, font, frc);
        Rectangle2D bounds = mLayout.getBounds();
        graphics.drawString("" + letter, x, (int) bounds.getHeight() + 2);
        int xs = 1;
        int xe = xs + CLOSE_DIMENSION;
        int ys = ICON_DIMENSION - CLOSE_DIMENSION - 3;
        int ye = ys + CLOSE_DIMENSION;
        graphics.setStroke(new BasicStroke(3.5f));
        graphics.setColor(new Color(255, 255, 255, 196));
        graphics.drawLine(xs, ys, xe, ye);
        graphics.drawLine(xs, ye, xe, ys);
        graphics.setStroke(new BasicStroke(2.0f));
        graphics.setColor(ImageCreator.mainMidColor);
        graphics.drawLine(xs, ys, xe, ye);
        graphics.drawLine(xs, ye, xe, ys);
        return new ImageIcon(image);
    }

    /**
     * Creates <code>edit</code> image icon of specified dimension.
     * 
     * @param dimension
     *            The dimension of the resulting square icon.
     * @return The resulting icon.
     */
    public static Icon getEditIcon(int dimension, char letter) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(dimension, 0, ImageCreator.mainDarkColor, 0, dimension, ImageCreator.mainUltraDarkColor);
        graphics.setPaint(gradient);
        Font font = new Font("Arial", Font.BOLD, dimension - 2);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        letter = Character.toUpperCase(letter);
        int charWidth = graphics.getFontMetrics().charWidth(letter);
        int x = dimension - charWidth - 2;
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, font, frc);
        Rectangle2D bounds = mLayout.getBounds();
        graphics.drawString("" + letter, x, (int) bounds.getHeight() + 2);
        int xs = 1;
        int xe = xs + CLOSE_DIMENSION;
        int xm = (xs + xe) / 2;
        int ys = ICON_DIMENSION - CLOSE_DIMENSION - 3;
        int ye = ys + CLOSE_DIMENSION;
        int ym = (ys + ye) / 2;
        graphics.setStroke(new BasicStroke(3.5f));
        graphics.setColor(new Color(255, 255, 255, 196));
        graphics.drawLine(xs, ym, xe, ym);
        graphics.drawLine(xm, ys, xm, ye);
        graphics.setStroke(new BasicStroke(2.5f));
        graphics.setColor(ImageCreator.mainMidColor);
        graphics.drawLine(xs, ym, xe, ym);
        graphics.drawLine(xm, ys, xm, ye);
        return new ImageIcon(image);
    }

    /**
     * Creates an image with transition area between two colors. The transition
     * area is specified by the starting and ending columns (in pixel units).
     * The transition area is randomly covered by semi-randomly colored squares.
     * 
     * @param width
     *            The width of the resilting image.
     * @param height
     *            The height of the resilting image.
     * @param leftColor
     *            The color on the left side of the resulting image.
     * @param rightColor
     *            The color on the right side of the resulting image.
     * @param transitionStart
     *            The starting column of the transition area. All the pixels
     *            lying to the left of this column will be colored uniformly by
     *            the left-side color.
     * @param transitionEnd
     *            The ending column of the transition area. All the pixels lying
     *            to the right of this column will be colored uniformly by the
     *            right-side color.
     * @return The resulting image.
     */
    public static BufferedImage getGradientCubesImage(int width, int height, Color leftColor, Color rightColor, int transitionStart, int transitionEnd) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(transitionStart, 0, leftColor, transitionEnd, 0, rightColor);
        graphics.setPaint(gradient);
        graphics.fillRect(transitionStart, 0, transitionEnd - transitionStart, height);
        graphics.setColor(leftColor);
        graphics.fillRect(0, 0, transitionStart, height);
        graphics.setColor(rightColor);
        graphics.fillRect(transitionEnd, 0, width - transitionEnd, height);
        int cubeCountY = height / ImageCreator.CUBE_DIMENSION;
        int cubeCountX = 1 + (transitionEnd - transitionStart) / ImageCreator.CUBE_DIMENSION;
        int cubeStartY = (height % ImageCreator.CUBE_DIMENSION) / 2;
        int cubeStartX = transitionStart - (ImageCreator.CUBE_DIMENSION - ((transitionEnd - transitionStart) % ImageCreator.CUBE_DIMENSION));
        for (int col = 0; col < cubeCountX; col++) {
            for (int row = 0; row < cubeCountY; row++) {
                if (Math.random() < 0.5) {
                    continue;
                }
                double coef = 1.0 - (((double) col / (double) cubeCountX) + 0.9 * (Math.random() - 0.5));
                coef = Math.max(0.0, coef);
                coef = Math.min(1.0, coef);
                int r = (int) (coef * leftColor.getRed() + (1.0 - coef) * rightColor.getRed());
                int g = (int) (coef * leftColor.getGreen() + (1.0 - coef) * rightColor.getGreen());
                int b = (int) (coef * leftColor.getBlue() + (1.0 - coef) * rightColor.getBlue());
                graphics.setColor(new Color(r, g, b));
                graphics.fillRect(cubeStartX + col * ImageCreator.CUBE_DIMENSION, cubeStartY + row * ImageCreator.CUBE_DIMENSION, ImageCreator.CUBE_DIMENSION, ImageCreator.CUBE_DIMENSION);
                graphics.setColor(new Color(255 - (int) (0.95 * (255 - r)), 255 - (int) (0.9 * (255 - g)), 255 - (int) (0.9 * (255 - b))));
                graphics.drawRect(cubeStartX + col * ImageCreator.CUBE_DIMENSION, cubeStartY + row * ImageCreator.CUBE_DIMENSION, ImageCreator.CUBE_DIMENSION, ImageCreator.CUBE_DIMENSION);
            }
        }
        return image;
    }

    /**
     * Retrieves an icon for <code>Increase font size</code> menu item. Contains
     * a single letter with upwards arrow.
     * 
     * @param font
     *            The font of the letter.
     * @param letter
     *            The letter to show.
     * @return Icon for <code>Increase font size</code> menu item.
//     * @see org.jvnet.jaxbw.xjcad.MainFrame.MainFrameMenuBar#MainFrameMenuBar(org.jvnet.jaxbw.xjcad.MainFrame)
//     * @see org.jvnet.jaxbw.xjcad.ViewerPanel#showPopup(int, int)
     */
    public static Icon getIconFontBigger(Font font, char letter) {
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        letter = Character.toUpperCase(letter);
        Font dfont = font.deriveFont(Font.BOLD, ICON_DIMENSION);
        graphics.setFont(dfont);
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, dfont, frc);
        Rectangle2D bounds = mLayout.getBounds();
        int w = (int) (Math.ceil(bounds.getWidth()));
        int h = (int) (Math.ceil(bounds.getHeight()));
        int x = 0;
        int y = ICON_DIMENSION - (ICON_DIMENSION - h) / 2;
        graphics.setColor(ImageCreator.mainUltraDarkColor);
        graphics.drawString("" + letter, x, y);
        int xs = x + w - 1;
        int ys = y - h;
        GradientPaint gradient = new GradientPaint(xs, ys, ImageCreator.mainLightColor, xs + 4, ys + 2, ImageCreator.mainDarkColor);
        graphics.setPaint(gradient);
        graphics.drawLine(xs, ys + 2, xs + 5, ys + 2);
        graphics.drawLine(xs + 1, ys + 1, xs + 4, ys + 1);
        graphics.drawLine(xs + 2, ys, xs + 3, ys);
        return new ImageIcon(image);
    }

    /**
     * Retrieves an icon for <code>Decrease font size</code> menu item. Contains
     * a single letter with downwards arrow.
     * 
     * @param font
     *            The font of the letter.
     * @param letter
     *            The letter to show.
     * @return Icon for <code>Decrease font size</code> menu item.
//     * @see org.jvnet.jaxbw.xjcad.MainFrame.MainFrameMenuBar#MainFrameMenuBar(org.jvnet.jaxbw.xjcad.MainFrame)
//     * @see org.jvnet.jaxbw.xjcad.ViewerPanel#showPopup(int, int)
     */
    public static Icon getIconFontSmaller(Font font, char letter) {
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        letter = Character.toUpperCase(letter);
        Font dfont = font.deriveFont(Font.BOLD, ICON_DIMENSION - 2);
        graphics.setFont(dfont);
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, dfont, frc);
        Rectangle2D bounds = mLayout.getBounds();
        int w = (int) (Math.ceil(bounds.getWidth()));
        int h = (int) (Math.ceil(bounds.getHeight()));
        int x = 0;
        int y = ICON_DIMENSION - (ICON_DIMENSION - h) / 2;
        graphics.setColor(ImageCreator.mainUltraDarkColor);
        graphics.drawString("" + letter, x, y);
        int xs = x + w;
        int ys = y - h;
        GradientPaint gradient = new GradientPaint(xs, ys, ImageCreator.mainLightColor, xs + 4, ys + 2, ImageCreator.mainDarkColor);
        graphics.setPaint(gradient);
        graphics.drawLine(xs, ys, xs + 5, ys);
        graphics.drawLine(xs + 1, ys + 1, xs + 4, ys + 1);
        graphics.drawLine(xs + 2, ys + 2, xs + 3, ys + 2);
        return new ImageIcon(image);
    }

    /**
     * Retrieves an icon for <code>Paint in color</code> menu item. Contains two
     * letters, one in gray, another in colored gradient.
     * 
     * @param font
     *            The font of the letters.
     * @param letter
     *            The letter to show.
     * @return Icon for <code>Paint in color</code> menu item.
//     * @see org.jvnet.jaxbw.xjcad.MainFrame.MainFrameMenuBar#MainFrameMenuBar(org.jvnet.jaxbw.xjcad.MainFrame)
//     * @see org.jvnet.jaxbw.xjcad.ViewerPanel#showPopup(int, int)
     */
    public static Icon getIconFontPaint(Font font, char letter) {
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        letter = Character.toUpperCase(letter);
        Font dfont = font.deriveFont(Font.BOLD, ICON_DIMENSION);
        graphics.setFont(dfont);
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, dfont, frc);
        Rectangle2D bounds = mLayout.getBounds();
        int w = (int) (Math.ceil(bounds.getWidth()));
        int h = (int) (Math.ceil(bounds.getHeight()));
        int x = 0;
        int y = ICON_DIMENSION - (ICON_DIMENSION - h) / 2;
        graphics.setColor(Color.gray);
        graphics.drawString("" + letter, x, y);
        GradientPaint gradient = new GradientPaint(0, 0, ImageCreator.mainLightColor, 0, ICON_DIMENSION - 1, ImageCreator.mainDarkColor);
        graphics.setPaint(gradient);
        graphics.drawString("" + letter, ICON_DIMENSION - w - 1, h);
        return new ImageIcon(image);
    }

    /**
     * Retrieves an icon for <code>Change font</code> menu item. Contains two
     * letters, one in gray, another in black Serif font.
     * 
     * @param font1
     *            The font of the first letter.
     * @param letter
     *            The letter to show.
     * @return Icon for <code>Change font</code> menu item.
//     * @see org.jvnet.jaxbw.xjcad.MainFrame.MainFrameMenuBar#MainFrameMenuBar(org.jvnet.jaxbw.xjcad.MainFrame)
//     * @see org.jvnet.jaxbw.xjcad.ViewerPanel#showPopup(int, int)
     */
    public static Icon getIconChangeFont(Font font1, char letter) {
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        letter = Character.toUpperCase(letter);
        Font dfont1 = font1.deriveFont(Font.BOLD, ICON_DIMENSION);
        graphics.setFont(dfont1);
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, dfont1, frc);
        Rectangle2D bounds = mLayout.getBounds();
        int h = (int) (Math.ceil(bounds.getHeight()));
        int x = 0;
        int y = ICON_DIMENSION - (ICON_DIMENSION - h) / 2;
        graphics.setColor(Color.gray);
        graphics.drawString("" + letter, x, y);
        Font dfont2 = new Font("Serif", Font.BOLD, ICON_DIMENSION);
        graphics.setFont(dfont2);
        FontRenderContext frc2 = graphics.getFontRenderContext();
        TextLayout mLayout2 = new TextLayout("" + letter, dfont2, frc2);
        Rectangle2D bounds2 = mLayout2.getBounds();
        int w2 = (int) (Math.ceil(bounds2.getWidth()));
        graphics.setColor(ImageCreator.mainUltraDarkColor);
        graphics.drawString("" + letter, ICON_DIMENSION - w2, y - 1);
        return new ImageIcon(image);
    }

    /**
     * Computes rounded version of the specified rectangle.
     * 
     * @param rect
     *            Rectangle.
     * @return Rounded version of the specified rectangle.
     */
    public static Polygon getRoundedRectangle(Rectangle rect) {
        Polygon border = new Polygon();
        int offsetX = (int) rect.getX();
        int offsetY = (int) rect.getY();
        border.addPoint(offsetX, offsetY + 1);
        border.addPoint(offsetX + 1, offsetY);
        border.addPoint(offsetX + (int) rect.getWidth() - 1, offsetY);
        border.addPoint(offsetX + (int) rect.getWidth(), offsetY + 1);
        border.addPoint(offsetX + (int) rect.getWidth(), offsetY + (int) rect.getHeight() - 1);
        border.addPoint(offsetX + (int) rect.getWidth() - 1, offsetY + (int) rect.getHeight());
        border.addPoint(offsetX + 1, offsetY + (int) rect.getHeight());
        border.addPoint(offsetX, offsetY + (int) rect.getHeight() - 1);
        return border;
    }

    public static Icon overlay(Icon icon1, Icon icon2) {
        BufferedImage image = getBlankImage(icon2.getIconWidth() / 2 + icon1.getIconWidth(), icon1.getIconHeight());
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        icon1.paintIcon(null, graphics, icon2.getIconWidth() / 2, 0);
        icon2.paintIcon(null, graphics, 0, icon1.getIconHeight() - icon2.getIconHeight());
        return new ImageIcon(image);
    }

    public static Image getIconImage() {
        BufferedImage result = getBlankImage(16, 16);
        Graphics2D g2 = (Graphics2D) result.getGraphics().create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setColor(new Color(140, 72, 170));
        g2.setFont(new Font("Tahoma", Font.BOLD, 17));
        g2.drawString("J", 6.0f, 14.0f);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Tahoma", Font.BOLD, 11));
        g2.drawString("w", 2.0f, 11.0f);
        g2.dispose();
        return result;
    }

    /**
     * Draws a green check in an oval, to use to indicate OK status.
     * 
     * @param dimension the dimension of the icon.
     * @return a green check icon.
     */
    public static Icon getCheckIcon(int dimension) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(0, 0, ImageCreator.mainUltraLightColor, dimension, dimension, Color.white);
        g2.setPaint(gradient);
        Ellipse2D ell = new Ellipse2D.Double(0, 0, dimension, dimension);
        g2.fill(ell);
        g2.setColor(Color.lightGray);
        g2.drawOval(0, 0, dimension - 1, dimension - 1);
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(Color.green.darker());
        g2.drawLine(dimension / 2, dimension - 4, dimension - 4, 3);
        g2.drawLine(4, dimension / 2, dimension / 2, dimension - 4);
        return new ImageIcon(image);
    }

    /**
     * Draws a red error icon in an oval background.
     * @param dimension The dimension of the icon.
     * @return an error icon.
     */
    public static Icon getErrorIcon(int dimension) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(0, 0, ImageCreator.errorLightColor, dimension, dimension, Color.white);
        g2.setPaint(gradient);
        Ellipse2D ell = new Ellipse2D.Double(0, 0, dimension, dimension);
        g2.fill(ell);
        g2.setColor(Color.lightGray);
        g2.drawOval(0, 0, dimension - 1, dimension - 1);
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(Color.red);
        g2.drawLine(3, 3, dimension - 4, dimension - 4);
        g2.drawLine(3, dimension - 4, dimension - 4, 3);
        return new ImageIcon(image);
    }
}
