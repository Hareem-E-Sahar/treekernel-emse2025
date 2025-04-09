package cdox.edit;

import cdox.*;
import cdox.gui.*;
import cdox.gui.action.*;
import cdox.gui.undo.*;
import cdox.util.*;
import cdox.util.image.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;
import org.w3c.dom.*;

/**
 * This class is the real implementation for the off-screen CDover. All elements are added
 * here, and the printing is done in this class also. Which means it implements the
 * Printable interface, and is called from cdox.print.PrintCover. The upperclass (the
 * class containing this class) is CDDrawingPane.
 * @author <a href="mailto:cdox@gmx.net">Rutger Bezema, Andreas Schmitz</a>
 *@version December 28th 2003
 *@see #print
 *@see PrintCover
 *@see CDDrawingPane
 */
public class Cover implements CDCoverStandards, Printable {

    private java.util.List elements = Collections.synchronizedList(new ArrayList());

    private int type;

    /**
     * Size of this cover (inherited of CDCoverStandards).
     *
     */
    protected int[] size;

    private GenericElement backgroundColor = null;

    private Color backColor = null;

    private BufferedImage origBackImage = null, backgroundImage = null;

    private float scaleFactorX = 1f, scaleFactorY = 1f;

    private String backgroundSource = null;

    private float strokeSize = 0.25f;

    private int backposition = 0;

    private boolean gray = false;

    private boolean quality = false;

    private Cover[] printCompanion = null;

    private boolean print = false;

    private boolean coverborders = true;

    private boolean dragging = false;

    private BufferedImage dragimage = null;

    private cdox.edit.Element dragelement = null;

    private BufferedImage image = null;

    private cdox.edit.Element selectedElement;

    /**
     * Creates a new cover with no elements. The type should be one of CDFRONT, CDCOVER,
     * CDBOOKLET and CDBACK_SIDE.
     *@param type the type of the new Cover.
     *@see CDCoverStandards
     */
    public Cover(int type) {
        this.type = type;
        switch(type) {
            case CDFRONT:
                size = CDFRONT_SIZE;
                break;
            case CDBACK_SIDE:
                size = CDBACK_SIDE_SIZE;
                break;
            case CDBOOKLET:
                size = CDBOOKLET_SIZE;
                break;
        }
    }

    /**
     * Returns a string containing debug information.
     * @return a <code>String</code> value.
     */
    public String toString() {
        switch(type) {
            case CDFRONT:
                return "CDFRONT";
            case CDBACK_SIDE:
                return "CDBACK_SIDE";
            case CDBOOKLET:
                return "CDBOOKLET";
        }
        return "Other type?";
    }

    /**
     *Returns the currentKey which loads the image of disk.
     *@return the key to load the image.
     */
    public String getKey() {
        return backgroundSource;
    }

    /**
     * Returns the size in an int array, size[0] = width, size[1] = height.
     * @param return the size in an array.
     */
    public int[] getSize() {
        return size;
    }

    /**
     * Returns the type of this cover.
     *@return the type.
     *@see CDCoverStandards
     */
    public int getType() {
        return type;
    }

    /**
     * Adds an element to the cover.
     *@param e the added element.
     */
    public void addElement(Element e) {
        elements.add(e);
        e.setCover(this);
        image = null;
    }

    /**
     * Returns the selected Element.
     *@return the element.
     */
    public Element getSelectedElement() {
        return selectedElement;
    }

    /**
     * Sets the currently selected Element.
     *@param e the element.
     */
    protected void setSelectedElement(Element e) {
        selectedElement = e;
    }

    /**
     * Returns the element that is at the top of the specified position.
     *@param p the position.
     *@return the element or null, if none was found.
     */
    public Element getElementAtPosition(Point p) {
        Iterator i = elements.iterator();
        Element e = null;
        while (i.hasNext()) {
            Element h = (Element) i.next();
            if (h.isInPlace(p)) e = h;
        }
        return e;
    }

    /**
     * Removes an element from this cover.
     *@param e the element to be removed.
     */
    public void removeElement(Element e) {
        if (elements.indexOf(e) != -1) {
            elements.remove(elements.indexOf(e));
        }
        image = null;
    }

    /**
     * Returns true if this cover has any elements.
     * @return true if has elements false otherwhise
     */
    public boolean hasElements() {
        return elements.size() != 0;
    }

    /**
     * Removes all elements from this cover, and destroys them properly.
     */
    public void removeAllElements() {
        Iterator i = elements.iterator();
        while (i.hasNext()) ((Element) i.next()).destroyMe();
        elements.clear();
        image = null;
    }

    /**
     * Moves an element to the top of the list.
     *@param e the element.
     */
    public void moveToTop(Element e) {
        elements.remove(elements.indexOf(e));
        elements.add(e);
        image = null;
    }

    /**
     * Moves an element to the bottom of the list.
     *@param e the element.
     */
    public void moveToBack(Element e) {
        elements.remove(elements.indexOf(e));
        elements.add(0, e);
        image = null;
    }

    /**
     * Sets the background color for this cover. This will delete a previously set background
     * image.
     *@param c the cover.
     */
    public void setBackground(Color c) {
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(size[0], size[1]));
        panel.setBackground(c);
        backColor = c;
        backgroundColor = new GenericElement(new Point(0, 0), new Dimension(size[0], size[1]), panel);
        backgroundColor.setCover(this);
        image = null;
    }

    /**
     * Returns the background color.
     *@return null, if no background was set or a background image has been set, the
     * background color otherwise.
     */
    public Color getBackgroundColor() {
        return backColor;
    }

    /**
     * Sets the background image.
     * @param source the TempFiles source id.
     * @param fit if true, the background image will occupy the whole cover. If false, and
     * the cover type is CDBACK_SIDE the side borders will be left free.
     * @exception IOException if an error occurs.
     */
    public void setBackground(String source, boolean fit) throws IOException {
        if (!((getType() != CDBACK_SIDE) || fit)) {
            backposition = CDSIDE_SIZE[0];
        } else backposition = 0;
        BufferedImage i = TempFiles.loadImage(TempFiles.get(source));
        backgroundSource = source;
        scaleFactorX = ((float) (size[0] - (2 * backposition))) / ((float) i.getWidth());
        scaleFactorY = ((float) size[1]) / ((float) i.getHeight());
        if ((scaleFactorX <= 1f) && (scaleFactorY <= 1f)) {
            AffineTransform at = AffineTransform.getScaleInstance(scaleFactorX, scaleFactorY);
            i = new AffineTransformOp(at, getRenderingHints()).filter(i, null);
        }
        backgroundImage = i;
        image = null;
        i = null;
        System.gc();
    }

    /**
     * Sets the background image.
     * @param bi the allready loaded backgroundImage
     * @param source the TempFiles source id.
     * @param fit if true, the background image will occupy the whole cover. If false, and
     * the cover type is CDBACK_SIDE the side borders will be left free.
     * @exception IOException if an error occurs.
     */
    public void setBackground(BufferedImage bi, String source, boolean fit) {
        if (!((getType() != CDBACK_SIDE) || fit)) {
            backposition = CDSIDE_SIZE[0];
        } else backposition = 0;
        backgroundSource = source;
        scaleFactorX = ((float) (size[0] - (2 * backposition))) / ((float) bi.getWidth());
        scaleFactorY = ((float) size[1]) / ((float) bi.getHeight());
        if ((scaleFactorX <= 1f) && (scaleFactorY <= 1f)) {
            AffineTransform at = AffineTransform.getScaleInstance(scaleFactorX, scaleFactorY);
            bi = new AffineTransformOp(at, getRenderingHints()).filter(bi, null);
        }
        backgroundImage = bi;
        image = null;
        bi = null;
        System.gc();
    }

    /**
     *Deletes the backgroundImage, and all of its sources.
     */
    public void deleteBackgroundImage() {
        origBackImage = null;
        backgroundImage = null;
        backgroundSource = null;
        image = null;
        dragimage = null;
        System.gc();
    }

    /**
     * Returns whether this cover has a backgroundImage set.
     *@return true, if it has one.
     */
    public boolean hasBackgroundImage() {
        return backgroundImage != null;
    }

    /**
     *Returns the backgroundimage or null im no backgroundImage is present.
     *@return the original backgroundImage or null if not present.
     */
    public BufferedImage getBackgroundImage() {
        if (backgroundSource != null) {
            if (scaleFactorX <= 1f || scaleFactorY <= 1f) {
                origBackImage = CDoxFrame.loadImage(TempFiles.get(backgroundSource));
                return origBackImage;
            } else return backgroundImage;
        }
        return null;
    }

    /**
     * Sets the backgroundImage anew, with the (filtered) bufferedImage. If the
     *scaleFactors are smaller as 1f the scaled-backgroundimage is recalculated, if not it
     *is just reseted to bi. The arrayList containing the correctors are for the case that
     *the scaleFactors are smaller as 1f.
     * @param bi the new filteredImage
     * @see #getBackgroundImage
     */
    public void setFilteredBackgroundImage(BufferedImage bi) {
        if (bi != null) {
            try {
                backgroundSource = TempFiles.add(bi);
            } catch (IOException ioe) {
                CDoxFrame.handleError(ioe, true);
            }
            if (scaleFactorX <= 1f || scaleFactorY <= 1f) {
                AffineTransform at = AffineTransform.getScaleInstance(scaleFactorX, scaleFactorY);
                bi = new AffineTransformOp(at, getRenderingHints()).filter(bi, null);
            }
            backgroundImage = bi;
            image = null;
            bi = null;
            System.gc();
        }
    }

    /**
     *Resets the backgroundImage to the image represented by the given key. This method is
     *called when the user redo's Undo's a backgroundImage correction.
     *@param key the key of the oldFile.
     *@see BackgroundEditAction#undo
     *@see BackgroundEditAction#redo
     */
    public void resetBackgroundImage(String key) {
        if (key == null) {
            backgroundImage = null;
            image = null;
            backgroundSource = null;
        } else {
            backgroundSource = key;
            BufferedImage bi = TempFiles.getImage(backgroundSource);
            if (scaleFactorX <= 1f || scaleFactorY <= 1f) {
                AffineTransform at = AffineTransform.getScaleInstance(scaleFactorX, scaleFactorY);
                bi = new AffineTransformOp(at, getRenderingHints()).filter(bi, null);
            }
            backgroundImage = bi;
            image = null;
            bi = null;
            System.gc();
        }
    }

    /**
     *This method sets a couple of rendering hints depending on the quality set. It
     *returns the quality-hints (containing: COLOR_RENDERING_ON, ANTIALIASING_ON,
     *TEXT_ANTIALIASING_ON, FRACTIONALMETRICS_ON, DITHERING_ENABLE, STROKE_CONTROL_PURE and
     *BICUBIC_INTERPOLATION) the not quality hints sets them all of resulting in quicker
     *movement e.d.
     *@return the brand new renderinghints.
     */
    public RenderingHints getRenderingHints() {
        if (quality) {
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            hints.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            hints.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
            hints.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON));
            hints.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE));
            hints.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE));
            hints.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            return hints;
        } else {
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            hints.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
            hints.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));
            hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
            hints.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF));
            hints.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE));
            return hints;
        }
    }

    /**
     * Sets the quality of the cover.
     *@param q if true, the quality will be high, but the performance will significantly
     * slow down.
     */
    public void setQuality(boolean q) {
        quality = q;
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            Element e = (Element) i.next();
            e.setQuality(q);
            if ((!q) && (e instanceof ImageElement)) ((ImageElement) e).resetImage();
        }
        image = null;
    }

    /**
     * Sets whether the image should be drawn gray or not.
     *@param gray true, if it should be gray. Default is false.
     */
    public void setGrayscale(boolean gray) {
        this.gray = gray;
        image = null;
    }

    /**
     *Returns the current gray value, gray is true if the Cover is drawn in gray.
     *@return true if this cover is drawn in gray false otherwise.
     */
    public boolean getGrayscale() {
        return gray;
    }

    /**
     * Causes the internal image to be repainted.
     */
    protected void fireChanged() {
        image = null;
    }

    /**
     * Sets whether the top element is being dragged. This method can end up in a better
     * performance while dragging a single element over thousands of others. Please note,
     * that you should not change anything in the cover while dragging, at least most
     * changes won't be drawn (only the dragged element will be drawn new). If no elements
     * exist and the parameter is true, nothing happens.
     *@param dragging whether we are dragging or not.
     */
    public void setDragging(boolean dragging) {
        if (elements.size() == 0) if (dragging) return;
        this.dragging = dragging;
        if (dragging) {
            Element e = (Element) elements.remove(elements.size() - 1);
            dragimage = getBufferedImage();
            elements.add(e);
            dragelement = e;
        } else dragimage = null;
    }

    /**
     * Determines whether the top element is dragged or not.
     *@return true, if we are dragging.
     */
    public boolean getDragging() {
        return dragging;
    }

    private void paintCoverBorders(Graphics2D g) {
        g.setPaint(Color.BLACK);
        g.setStroke(new BasicStroke(strokeSize));
        if (print) g.drawRect(0, 0, size[0], size[1]); else g.drawRect(0, 0, size[0] - 1, size[1] - 1);
        if (type == CDBOOKLET) {
            if (Preferences.userNodeForPackage(CDoxFrame.class).getBoolean("borders", true)) g.drawLine(size[0] / 2, 0, size[0] / 2, size[1]);
            g.drawLine(size[0] / 2, -10, size[0] / 2, -2);
            g.drawLine(size[0] / 2, size[1] + 2, size[0] / 2, size[1] + 10);
        }
        if (type == CDBACK_SIDE) {
            if (Preferences.userNodeForPackage(CDoxFrame.class).getBoolean("borders", true)) {
                g.drawLine(CDSIDE_SIZE[0], 0, CDSIDE_SIZE[0], CDSIDE_SIZE[1]);
                g.drawLine(CDBACK_SIDE_SIZE[0] - CDSIDE_SIZE[0] - 1, 0, CDBACK_SIDE_SIZE[0] - CDSIDE_SIZE[0] - 1, CDSIDE_SIZE[1]);
            }
            g.drawLine(CDSIDE_SIZE[0], -10, CDSIDE_SIZE[0], -2);
            g.drawLine(CDSIDE_SIZE[0], CDSIDE_SIZE[1] + 2, CDSIDE_SIZE[0], CDSIDE_SIZE[1] + 10);
            g.drawLine(CDBACK_SIDE_SIZE[0] - CDSIDE_SIZE[0] - 1, -10, CDBACK_SIDE_SIZE[0] - CDSIDE_SIZE[0] - 1, -2);
            g.drawLine(CDBACK_SIDE_SIZE[0] - CDSIDE_SIZE[0] - 1, CDSIDE_SIZE[1] + 2, CDBACK_SIDE_SIZE[0] - CDSIDE_SIZE[0] - 1, CDSIDE_SIZE[1] + 10);
        }
    }

    /**
     *Set the stroke size to draw the coverBorders with.
     *@param f the size of the stroke.
     */
    public void setStrokeSize(float f) {
        strokeSize = f;
    }

    /**
     * Returns an image with all elements painted.
     *@return the BufferedImage.
     */
    public BufferedImage getBufferedImage() {
        int itype = BufferedImage.TYPE_INT_RGB;
        if (gray) itype = BufferedImage.TYPE_USHORT_GRAY;
        BufferedImage img = new BufferedImage(size[0], size[1], itype);
        Graphics2D g = img.createGraphics();
        g.setRenderingHints(getRenderingHints());
        if (backgroundColor != null) {
            backgroundColor.drawMyself(g);
        }
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, backposition, 0, size[0] - (2 * backposition), size[1], null, null);
        }
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            Element e = (Element) i.next();
            e.drawMyself(g);
        }
        paintCoverBorders(g);
        return img;
    }

    /**
     * Draws the whole thing onto the given graphics.
     * @param gr the graphics to paint on.
     */
    public void drawMyself(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        if (dragging) {
            g.drawImage(dragimage, 0, 0, null);
            dragelement.drawMyself((Graphics2D) gr);
        } else {
            if (image == null) image = getBufferedImage();
            g.drawImage(image, 0, 0, null);
            if (gray) {
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
                g.setComposite(ac);
                g.setPaint(Color.GRAY);
                g.fillRect(0, 0, size[0], size[1]);
            }
        }
    }

    /**
     * Writes his properties into the DOM document and causes the elements to write their
     * files into the ZipOutputStream, if any.
     *@param doc the DOM document.
     *@param out the ZipOutputStream.
     *@throws IOException if writing to the zip failed.
     */
    public void saveMyself(Document doc, ZipOutputStream out) throws IOException {
        org.w3c.dom.Element cover = null;
        switch(getType()) {
            case CDFRONT:
                cover = doc.createElementNS("http://cdox.sf.net/schema/fileformat", "cdfront");
                break;
            case CDBOOKLET:
                cover = doc.createElementNS("http://cdox.sf.net/schema/fileformat", "cdbooklet");
                break;
            case CDBACK_SIDE:
                cover = doc.createElementNS("http://cdox.sf.net/schema/fileformat", "cdback");
                break;
        }
        if (backColor != null) cover.setAttribute("color", backColor.getRGB() + "");
        if (backgroundImage != null) {
            cover.setAttribute("image", this + ".back.image.png");
            out.putNextEntry(new ZipEntry(this + ".back.image.png"));
            if (origBackImage == null) {
                origBackImage = CDoxFrame.loadImage(TempFiles.get(backgroundSource));
            }
            ImageIO.write(origBackImage, "png", out);
            origBackImage = null;
            out.closeEntry();
            System.gc();
            if ((getType() == CDBACK_SIDE) && (backposition != 0)) cover.setAttribute("freesides", "true");
        }
        Iterator i = elements.iterator();
        while (i.hasNext()) ((cdox.edit.Element) i.next()).saveMyself(cover, out);
        doc.getDocumentElement().appendChild(cover);
    }

    /**
     *This method exports this Cover to an image. It renders all this covers Objects on a
     *buffered image, using the drawMySelfHighQuality-Method, and than writes the
     *bufferedImage in the given file.
     *@param f the file to export to
     *@see FileAction#actionPerformed
     */
    public void exportMySelf(File f) {
        setQuality(true);
        BufferedImage exportImage = new BufferedImage(size[0], size[1], (f.getName().endsWith("jpg")) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = exportImage.createGraphics();
        g.setRenderingHints(getRenderingHints());
        drawMySelfHighQuality(g);
        Iterator it = ImageIO.getImageWriters(new ImageTypeSpecifier(exportImage), (f.getName().endsWith("jpg")) ? "jpg" : "png");
        if (!it.hasNext()) {
            CDoxFrame.handleError(new Throwable(CDox.getLocalizer().get("nowriter")), false);
            setQuality(false);
            return;
        }
        ImageWriter iw = (ImageWriter) it.next();
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            iw.setOutput(ios);
            iw.write(exportImage);
            iw.dispose();
            ios.flush();
            ios.close();
            System.gc();
        } catch (IOException ioe) {
            setQuality(false);
            CDoxFrame.handleError(ioe, true);
        }
        setQuality(false);
    }

    /**
     *This method draws all objects in this Cover on the given Graphics Object. Doing so
     *it uses only the HighQualityGraphics conversions. So no rescaling on bufferedImage
     *etc. This results in a very-good quality, for example for printing and/or exporting
     *this cover.
     *@param g the Graphics object to draw on
     *@see #exportMySelf
     *@see #print
     */
    private synchronized void drawMySelfHighQuality(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform();
        if (backgroundColor != null) {
            g.setPaint(backColor);
            g.fillRect(0, 0, size[0], size[1]);
        }
        if (backgroundImage != null) {
            AffineTransform scaledTransform = AffineTransform.getScaleInstance(scaleFactorX, scaleFactorY);
            if (backposition != 0) g.translate(backposition, 0);
            g.transform(scaledTransform);
            origBackImage = CDoxFrame.loadImage(TempFiles.get(backgroundSource));
            if ((scaleFactorX < 0.3f) || (scaleFactorY < 0.3f)) {
                int count = 0;
                int x = origBackImage.getWidth();
                int widthx = x;
                int height = origBackImage.getHeight();
                int positionX = 0;
                if (((scaleFactorX < 0.3f) && (scaleFactorX > 0.2f)) || ((scaleFactorY < 0.3f) && (scaleFactorX > 0.2f))) {
                    widthx = x / 2;
                    count = 2;
                } else if (((scaleFactorX <= 0.2f) && (scaleFactorX > 0.1f)) || ((scaleFactorY <= 0.2f) && (scaleFactorX > 0.1f))) {
                    widthx = x / 3;
                    count = 3;
                } else if (((scaleFactorX <= 0.1f) && (scaleFactorX > 0.01f)) || ((scaleFactorY <= 0.1f) && (scaleFactorX > 0.01f))) {
                    widthx = x / 4;
                    count = 4;
                }
                BufferedImage temp = origBackImage.getSubimage(positionX, 0, widthx, height);
                for (int i = 0; i < count; i++) {
                    AffineTransform at = g.getTransform();
                    g.translate(positionX, 0);
                    g.drawImage(temp, 0, 0, null);
                    temp = null;
                    System.gc();
                    if (i < count - 1) {
                        positionX = (i + 1) * widthx;
                        temp = origBackImage.getSubimage(positionX, 0, widthx, height);
                    }
                    g.setTransform(at);
                }
            } else g.drawImage(origBackImage, 0, 0, null);
            origBackImage = null;
            System.gc();
            g.setTransform(oldTransform);
        }
        for (int i = 0; i < elements.size(); i++) {
            Element currentElement = (Element) elements.get(i);
            Point elementPosition = currentElement.getPosition();
            g.translate(elementPosition.x, elementPosition.y);
            currentElement.createCachedImage();
            g.transform(currentElement.getPrintableAffineTransform());
            g.drawImage(currentElement.getBufferedImage(), 0, 0, null);
            g.setTransform(oldTransform);
        }
        paintCoverBorders(g);
    }

    /**
     * Sets which covers will be printed together with this Cover on the same page.
     *@param c the other covers.
     */
    public void setPrintCompanion(Cover[] c) {
        printCompanion = c;
    }

    /**
     * Here the printing is done. First the quality rendering hints are added to the
     *Printer's graphics-object than the original background image is loaded out of a file
     *(or zipFile) after that all the elements are drawn on the the printer's graphics
     *object. Last of all if this cover has a print companinion and this is not a
     *backside-cover the companion is called.
     * @param gr the graphics object of the printer
     * @param p the pageformat,
     * @param index the x-side to be printed (we only print one side at the time).
     * @return the Printable standards NO_SUCH_PAGE or PAGE_EXISTS.
     * @exception PrinterException if an error occurs.
     * @see PrintCover#run
     */
    public int print(Graphics gr, PageFormat p, int index) throws PrinterException {
        Graphics2D g = (Graphics2D) gr;
        setQuality(true);
        print = true;
        g.setRenderingHints(getRenderingHints());
        AffineTransform theVeryOldTransform = g.getTransform();
        if (index != 0) {
            if (printCompanion == null) {
                setQuality(false);
                print = false;
                return NO_SUCH_PAGE;
            }
            if (index != 1) {
                setQuality(false);
                print = false;
                return NO_SUCH_PAGE;
            }
            if ((CDFRONT_SIZE[1] + CDBACK_SIDE_SIZE[1]) <= p.getImageableHeight() && (printCompanion[0].getType() != CDBOOKLET)) {
                setQuality(false);
                print = false;
                return NO_SUCH_PAGE;
            }
            if (this == printCompanion[0]) {
                return printCompanion[1].print(gr, p, index);
            }
        } else {
            if (printCompanion != null) {
                if (((CDFRONT_SIZE[1] + CDBACK_SIDE_SIZE[1]) > p.getImageableHeight()) || (printCompanion[0].getType() == CDBOOKLET)) {
                    if (this == printCompanion[1]) {
                        print = false;
                        setQuality(false);
                        return PAGE_EXISTS;
                    }
                }
            }
        }
        int offsetY = 0;
        if (getType() == CDBOOKLET) {
            g.rotate(Math.toRadians(90));
            g.translate(p.getImageableX(), -p.getImageableWidth());
        }
        if (printCompanion != null) {
            if (printCompanion[1] == this) {
                switch(printCompanion[1].getType()) {
                    case CDFRONT:
                        offsetY = 0;
                        break;
                    case CDBACK_SIDE:
                        if (index == 1) offsetY = 10; else offsetY = ((int) p.getImageableHeight()) - CDBACK_SIDE_SIZE[1] - 10;
                        break;
                }
            }
        } else {
            if (getType() == CDBACK_SIDE) offsetY = 10;
        }
        if (getType() != CDBOOKLET) g.translate(p.getImageableX(), p.getImageableY() + offsetY);
        drawMySelfHighQuality(g);
        if ((printCompanion != null) && (this == printCompanion[0])) {
            g.setTransform(theVeryOldTransform);
            printCompanion[1].print(g, p, index);
        }
        setQuality(false);
        print = false;
        g.dispose();
        return PAGE_EXISTS;
    }

    /**
     * This method can be invoked to indicate that this Cover is no longer needed. This
     * does also destroy its elements.
     */
    public void destroyMe() {
        Iterator i = elements.iterator();
        while (i.hasNext()) ((Element) i.next()).destroyMe();
        elements = null;
        dragelement = null;
        selectedElement = null;
        origBackImage = null;
    }

    /**
     * Sets the origBackImage to null and runs the GarbageCollector.
     */
    public void saveMemory() {
        if (origBackImage == null) return; else {
            origBackImage = null;
            System.gc();
        }
    }
}
