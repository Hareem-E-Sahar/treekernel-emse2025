package reprojection;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import javax.swing.ImageIcon;

/**
 * This class make a screen shot of the Google Earth rendering windows, and put it on the image panel. 
 * @author Clement Oliva
 *
 */
public class GEImageLoader {

    private BufferedImage image = null;

    int mni = 0;

    int mnj = 0;

    int pixels[] = null;

    int originPixels[] = null;

    /**
     * Constructor.
     */
    public GEImageLoader() {
    }

    /**
	 * This method take a screen shot of a rectangle, and save it on the "image" field of the class.
	 * @param rectangle
	 */
    public void loadLeftImage(Rectangle rectangle) {
        try {
            Robot robot = new Robot();
            image = robot.createScreenCapture(rectangle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * Create the image from an array of integer.
	 * @param pixels
	 * @return Image
	 */
    public Image getImageFromInts(int pixels[]) {
        MemoryImageSource mr = new MemoryImageSource(mni, mnj, pixels, 0, mni);
        Image limage = Toolkit.getDefaultToolkit().createImage(mr);
        return limage;
    }

    /**
	 * Image getter
	 * @return BufferedImage
	 */
    public BufferedImage getImage() {
        return image;
    }

    /**
	 * Image setter
	 * @param image
	 */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
	 * originPixel getter
	 * @return int[]
	 */
    public int[] getOriginpixels() {
        return originPixels;
    }

    /**
	 * Get the pixel at "i" position on originPixel Array.
	 * @param i
	 * @return int
	 */
    public int getOriginPixel(int i) {
        return originPixels[i];
    }

    /**
	 * originPixels setter
	 * @param originpixels
	 */
    public void setOriginpixels(int[] originpixels) {
        this.originPixels = originpixels;
    }

    /**
	 * Get the pixel array
	 * @return int[]
	 */
    public int[] getPixels() {
        return pixels;
    }

    /**
	 * Set the pixel array
	 * @param pixels
	 */
    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }

    /**
	 * Set the value of the "i" pixel on the array.
	 * @param i
	 * @param value
	 */
    public void setPixel(int i, int value) {
        this.pixels[i] = value;
    }

    /**
	 * Convert an image to an array of integer.
	 * @param imc
	 */
    public void imageToInts(ImageIcon imc) {
        if (imc == null) {
            return;
        }
        imc.getImageLoadStatus();
        Image im = imc.getImage();
        mni = imc.getIconWidth();
        mnj = imc.getIconHeight();
        originPixels = new int[mni * mnj];
        pixels = new int[mni * mnj];
        PixelGrabber pr = new PixelGrabber(im, 0, 0, mni, mnj, originPixels, 0, mni);
        try {
            pr.grabPixels();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
