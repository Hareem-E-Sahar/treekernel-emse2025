package cdox.edit;

import cdox.*;
import cdox.gui.*;
import cdox.gui.action.*;
import cdox.gui.undo.*;
import cdox.util.*;
import cdox.util.image.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;

/**
 * This class represents an element that is contained within a document. An ImageElement
 * can contain an Image as data.
 * @author <a href="mailto:cdox@gmx.net">Rutger Bezema, Andreas Schmitz</a>
 */
public class ImageElement extends Element {

    private BufferedImage origImage, originalImage;

    private String sourceID = null;

    private static int counter = 0;

    private int ID = counter++;

    private Dimension offsetSinceLastResize = new Dimension();

    private float scaleFactor = 1f;

    /**
     * Constructs a new ImageElement with a scaledImage at position pos.
     * @param pos the position.
     * @param size the size.
     * @param imageSource the ID that can be used to get the image from the TempFiles
     * class.
     * @param c the cover which contains this image (used for calculating acpect ratio)
     * @exception IOException if an error occurs.
     */
    public ImageElement(Point pos, Dimension size, String imageSource, Cover c) throws IOException {
        super(size, pos);
        sourceID = imageSource;
        BufferedImage image = TempFiles.loadImage(TempFiles.get(imageSource));
        if (size == null) {
            if ((image.getWidth() < c.size[0]) && (image.getHeight() < c.size[1])) elementSize = new Dimension(image.getWidth(), image.getHeight()); else elementSize = new Dimension(c.size[0], c.size[1]);
            ratioX = elementSize.width;
            ratioY = elementSize.height;
        }
        if (image.getWidth() > image.getHeight()) {
            scaleFactor = ((float) c.size[1]) / ((float) image.getHeight());
        } else scaleFactor = ((float) c.size[0]) / ((float) image.getWidth());
        if (scaleFactor < 1f) {
            AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            c.setQuality(true);
            image = new AffineTransformOp(at, c.getRenderingHints()).filter(image, null);
            c.setQuality(false);
        }
        origImage = image;
        setResizeRects();
    }

    /**
     * This method sets the filteredImage to this ImageElement. If the scaleFactor is
     *smaller as 1.0f (the original image doesn't fit in the cover) a scaled instance is
     *calculated to fit in the cover, e.g. to work with.
     * @param bi the filtered BufferedImage to be drawn
     * @see ImageDialog#run
     */
    public void setPaintImage(BufferedImage bi) {
        try {
            sourceID = TempFiles.add(bi);
        } catch (IOException ioe) {
            CDoxFrame.handleError(ioe, true);
        }
        if (scaleFactor < 1f) {
            AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            cover.setQuality(true);
            bi = new AffineTransformOp(at, cover.getRenderingHints()).filter(bi, null);
            cover.setQuality(false);
        }
        origImage = bi;
        cachedImage = null;
    }

    /**
     * Returns the current key used to load the image of disk.
     * @return the String.
     */
    public String getKey() {
        return sourceID;
    }

    /**
     *Resets the oldImage to the image represented by the given key. This method is
     *called when the user redo's Undo's an Imagecorrection.
     *@param key the key of the oldFile.
     *@see BackgroundEditAction#undo
     *@see BackgroundEditAction#redo
     */
    public void resetOldImage(String key) {
        cachedImage = null;
        sourceID = key;
        BufferedImage bi = TempFiles.getImage(sourceID);
        if (scaleFactor < 1f) {
            AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            bi = new AffineTransformOp(at, cover.getRenderingHints()).filter(bi, null);
        }
        origImage = bi;
        bi = null;
        System.gc();
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor f) {
        return f.equals(DataFlavor.imageFlavor);
    }

    /**
     *Returns the scaled instance of the original Image. To use this correctly also call
     *the isZipFileUsed() method and the getBackgroundFile() or getZipFile()-getZipEntry()
     *combination.
     *@param f the kind of data which represent an image
     *@return the scaled instance of the original image.
     *@see EditAction#actionPerformed
     */
    public Object getTransferData(DataFlavor f) {
        return origImage;
    }

    /**
     *Returns the Original image with applied filters etc.
     *@return the original image with applied filters.
     *@see #getBufferedImage
     *@see ImageDialog
     */
    public BufferedImage getCurrentImage() {
        quality = true;
        BufferedImage bi = getBufferedImage();
        resetImage();
        quality = false;
        return bi;
    }

    /**
     *This method returns a bufferedImage. If quality is set to true, which means we need
     *to do some important things with the image, the image is read from disk anew, and
     *given back. That is if it (the image) is in it's original state larger as the Cover
     *it contains. Else the image will not occupy much memory and there is no use for
     *"caching" it from disk. If the image was filtered by the user the filters are
     *applied to the read-image. This is not nessecary for low quality Pictures, then the
     *paintimage contains the filterdImage allready.
     *@return the image representing this ImageElement.
     */
    protected BufferedImage getBufferedImage() {
        if (!quality) {
            return origImage;
        } else {
            if (scaleFactor <= 1f) {
                originalImage = CDoxFrame.loadImage(TempFiles.get(sourceID));
                if (originalImage == null) originalImage = origImage;
                return originalImage;
            } else {
                return origImage;
            }
        }
    }

    /**
     *This method can be called to set the original image to null, and than
     *call the garbageCollector. Memory savings I say.
     */
    public void resetImage() {
        originalImage = null;
        System.gc();
    }

    protected void saveMyself(org.w3c.dom.Element root, ZipOutputStream out) throws IOException {
        org.w3c.dom.Element elem = appendChild(root, "image");
        org.w3c.dom.Element cont = elem.getOwnerDocument().createElementNS("http://cdox.sf.net/schema/fileformat", "content");
        org.w3c.dom.Element img = elem.getOwnerDocument().createElementNS("http://cdox.sf.net/schema/fileformat", "file");
        img.setAttribute("name", ID + ".image.png");
        cont.appendChild(img);
        elem.appendChild(cont);
        out.putNextEntry(new ZipEntry(ID + ".image.png"));
        quality = true;
        ImageIO.write(getBufferedImage(), "png", out);
        quality = false;
        resetImage();
        out.closeEntry();
    }
}
