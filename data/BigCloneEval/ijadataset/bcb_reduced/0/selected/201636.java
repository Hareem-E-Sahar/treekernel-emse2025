package cdox.edit;

import cdox.*;
import cdox.gui.*;
import cdox.gui.action.*;
import cdox.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.swing.*;

/**
 * This class is the rootPane for the later CDCover panes, it's constructed in the CDoxFrame,
 *and lives and dies with it.(It's constructed only one Time);
 * @author <a href="mailto:cdox@gmx.net">Rutger Bezema, Andreas Schmitz</a>
 * @version June 10th 2002
 * @see cdox.gui.CDoxFrame
 * @see CDCover
 */
public class EditPane extends JTabbedPane implements CDCoverStandards {

    private GridBagConstraints gb = new GridBagConstraints();

    private CDCover theCDCover;

    private CDCover activeCover;

    private CDoxFrame cdoxFrame;

    private EditPane itsme = this;

    private Rectangle scrollPaneBorderRect;

    private Cover[] covers;

    private Localizer lang = CDox.getLocalizer();

    /**
     * The Constructor just calls the super class(JPanel) to set the layoutmangager.
     * @param c the CDoxFrame.
     */
    public EditPane(CDoxFrame c) {
        super();
        cdoxFrame = c;
        scrollPaneBorderRect = new Rectangle();
    }

    /**
     * This Method iterates over the given array to see what CoverPanels should be added,
     *and adds them.
     * @param theCovers an Array (of lenth 4) containing the users choises.
     * @param f the Font standardly used on a TextPane in the CDDrawingpane.
     * @param bold true if bold is selected in the cdoxframe false otherwhise
     * @param italic true if italic is selected in the cdoxframe false otherwhise
     * @param underline true if underline is selected in the cdoxframe false otherwhise
     * @param tColor the <code>Color</code>.
     */
    public void addCoverTemplates(int[] theCovers, Font f, boolean bold, boolean italic, boolean underline, Color tColor) {
        int count = 0;
        if (covers != null) for (int i = 0; i < covers.length; i++) if (covers[i] != null) covers[i].destroyMe();
        for (int i = 0; i < theCovers.length; i++) if (theCovers[i] != -1) count++;
        covers = new Cover[count];
        count = 0;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < theCovers.length; i++) {
            gb.gridx = 0;
            if (theCovers[i] != -1) {
                theCDCover = new CDCover(cdoxFrame, theCovers[i], f, bold, italic, underline, tColor);
                MouseKlickHandler mHandler = new MouseKlickHandler();
                theCDCover.addMouseListener(mHandler);
                gb.insets = new Insets(5, 5, 5, 5);
                gb.anchor = gb.CENTER;
                if (count == 0) {
                    theCDCover.setActive(true);
                    activeCover = theCDCover;
                } else theCDCover.setActive(false);
                covers[count] = theCDCover.getDrawingPane().getCover();
                add(theCDCover.getLocalizedName(), theCDCover);
                count++;
                switch(theCovers[i]) {
                    case CDFRONT:
                        sb.append(lang.get("cdfront") + "/");
                        break;
                    case CDBACK_SIDE:
                        sb.append(lang.get("cdside") + "/");
                        break;
                    case CDBOOKLET:
                        sb.append(lang.get("cdbooklet") + "/");
                        break;
                }
            }
        }
        sb = new StringBuffer(sb.toString().substring(0, sb.length() - 1));
        sb.append(": " + lang.get("untitled"));
        cdoxFrame.setTitle(sb.toString());
    }

    /**
     * Sets the given covers as current project. The old covers will be lost, so make sure
     * they are saved before.
     * @param theCovers an Array containing the users choises.
     * @param f the Font standardly used on a TextPane in the CDDrawingpane.
     * @param bold true if bold is selected in the cdoxframe false otherwhise
     * @param italic true if italic is selected in the cdoxframe false otherwhise
     * @param underline true if underline is selected in the cdoxframe false otherwhise
     * @param tColor the <code>Color</code>.
     * @param title a <code>String</code> value.
     */
    public void addCoverTemplates(Cover[] theCovers, Font f, boolean bold, boolean italic, boolean underline, Color tColor, String title) {
        StringBuffer sb = new StringBuffer();
        if (covers != null) for (int i = 0; i < covers.length; i++) {
            if (covers[i] != null) covers[i].destroyMe();
        }
        covers = new Cover[theCovers.length];
        for (int i = 0; i < theCovers.length; i++) {
            gb.gridx = 0;
            theCDCover = new CDCover(cdoxFrame, theCovers[i], 0, f, bold, italic, underline, tColor);
            theCDCover.addMouseListener(new MouseKlickHandler());
            gb.insets = new Insets(5, 5, 5, 5);
            gb.anchor = gb.CENTER;
            if (i == 0) {
                theCDCover.setActive(true);
                activeCover = theCDCover;
            } else theCDCover.setActive(false);
            add(theCDCover, gb);
            covers[i] = theCovers[i];
            switch(theCovers[i].getType()) {
                case CDFRONT:
                    sb.append(lang.get("cdfront") + "/");
                    break;
                case CDBACK_SIDE:
                    sb.append(lang.get("cdside") + "/");
                    break;
                case CDBOOKLET:
                    sb.append(lang.get("cdbooklet") + "/");
                    break;
            }
        }
        sb = new StringBuffer(sb.toString().substring(0, sb.length() - 1));
        sb.append(": " + title);
        cdoxFrame.setTitle(sb.toString());
    }

    /**
     * Replaces the old cover with the new one, and draws the pane anew.
     * @param old the old Cover
     * @param newC the new Cover.
     */
    public void replaceCover(Cover old, Cover newC) {
        CDCover temp = null;
        for (int i = 0; i < covers.length; i++) {
            if (covers[i] == old) {
                covers[i] = newC;
            }
        }
        for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i) instanceof CDCover) {
                if (((CDCover) getComponent(i)).getDrawingPane().getCover() == old) temp = (CDCover) getComponent(i);
            }
        }
        temp.replaceCover(newC);
        old.destroyMe();
    }

    /**
     * Returns all covers that are used within this EditPane.
     *@return the covers.
     */
    public Cover[] getCovers() {
        return covers;
    }

    /**
     * Returns the active CDCover object.
     *@return the CDCover.
     */
    public CDCover getActiveCover() {
        return activeCover;
    }

    /**
     * Returns the active drawing pane.
     *@return the CDDrawingPane.
     */
    public CDDrawingPane getDrawingPane() {
        return activeCover.getDrawingPane();
    }

    /**
     *This Method can be called to check if there are any Objects in this Panel.
     *@return true if it contains Components false otherwise
     */
    public boolean hasComponents() {
        if (getComponentCount() == 0) return false; else return true;
    }

    /**
     *This Method removes All components contained in this Panel,
     *Afterwhich this Panel is set to invisible
     */
    public void removeCoverTemplates() {
        removeAll();
    }

    /**
     * This Method is called when on a CDDrawingpane is
     *pressed when it was not active, it sets the current
     *activeCover deaktivated, and activates the new CDCover
     * @param c a <code>CDCover</code> value
     */
    public void setActive(CDCover c) {
        if (activeCover != c) {
            if (activeCover.getDrawingPane().isTextEnabled()) {
                if (cdoxFrame.getTextEnabled()) {
                    cdoxFrame.updateTextBar("cover");
                }
            }
            if (!cdoxFrame.getTextEnabled()) {
                activeCover.setActive(false);
                activeCover = c;
                activeCover.setActive(true);
            }
        }
    }

    /**
     * Saves a preview of the covers into the zip file.
     *
     * @param out where to store itself.
     * @exception IOException if an error occurs.
     */
    public void saveMyPreviewSelf(ZipOutputStream out) throws IOException {
        BufferedImage bi = new BufferedImage(getMinimumSize().width, getMinimumSize().height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.createGraphics();
        g.setPaint(Color.BLACK);
        g.setStroke(new BasicStroke(3f));
        int drawHeight = 5;
        for (int i = 0; i < covers.length; i++) {
            if (getComponent(i) != null) {
                AffineTransform oldTransform = g.getTransform();
                float x = ((float) (getMinimumSize().width - ((JComponent) getComponent(i)).getPreferredSize().width)) / 2f;
                float y = (float) drawHeight;
                g.translate(x, y);
                covers[i].setStrokeSize(3f);
                boolean gr = covers[i].getGrayscale();
                covers[i].setGrayscale(false);
                g.drawImage(covers[i].getBufferedImage(), 0, 0, this);
                covers[i].setGrayscale(gr);
                covers[i].setStrokeSize(0.25f);
                g.drawRect(0, 0, covers[i].getBufferedImage().getWidth(), covers[i].getBufferedImage().getHeight());
                g.setTransform(oldTransform);
                drawHeight += ((JComponent) getComponent(i)).getPreferredSize().height + 10;
            }
        }
        float scaleFactor = (((float) 150) / ((float) bi.getWidth()) + ((float) 150) / ((float) bi.getHeight())) / 2f;
        AffineTransform scaled = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
        AffineTransformOp scaledTransform = new AffineTransformOp(scaled, new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
        bi = scaledTransform.filter(bi, null);
        out.putNextEntry(new ZipEntry("coverPreview_image.png"));
        ImageIO.write(bi, "png", out);
        out.closeEntry();
    }

    /**
     *This subclass handles the selected Panes. If a Pane is pressed,
     *it is turned on, all the others are turned off.
     */
    private class MouseKlickHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (!(activeCover == (CDCover) e.getSource())) {
                activeCover.getDrawingPane().getCover().setDragging(false);
                for (int i = 0; i < itsme.getComponentCount(); i++) {
                    if (e.getSource() instanceof CDCover) {
                        theCDCover = (CDCover) itsme.getComponent(i);
                        if (theCDCover == (CDCover) e.getSource()) {
                            theCDCover.setActive(true);
                            activeCover = theCDCover;
                            if (theCDCover.getDrawingPane().isTextEnabled()) if (cdoxFrame.getTextEnabled()) {
                                cdoxFrame.updateTextBar("cover");
                            }
                        } else {
                            theCDCover.setActive(false);
                        }
                    }
                }
                cdoxFrame.resetSaveFile();
            }
        }
    }

    /**
     *This Method gets a ComponentListener which will be registered with the JScrollPane,
     *this way we can notfiy the CDDrawingPane if the viewable size is to small.
     *@return the ComponentListener
     */
    public CompHandler addNewComponentHandler() {
        return new CompHandler();
    }

    /**
     *This Class iterates over the Components and changes the values of their ScrollPane width
     *and height.
     */
    public class CompHandler extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {
            scrollPaneBorderRect = ((JScrollPane) e.getSource()).getViewportBorderBounds();
            for (int i = 0; i < itsme.getComponentCount(); i++) {
                CDCover cdCov = (CDCover) itsme.getComponent(i);
                cdCov.getDrawingPane().setScrollPaneDimension(scrollPaneBorderRect.width, scrollPaneBorderRect.height);
            }
        }
    }

    public class TabListener extends CDoxAction {

        public TabListener() {
            super(cdoxFrame, "coverchange", null);
        }

        public void actionPerformed(ActionEvent ae) {
            if (getComponentCount() > 1) {
                for (int i = 0; i < getComponentCount(); i++) {
                    if (getComponent(i) instanceof CDCover) {
                        if (((CDCover) getComponent(i)) != itsme.getActiveCover()) {
                            itsme.setActive((CDCover) getComponent(i));
                            break;
                        }
                    }
                }
            }
        }
    }

    public TabListener getTab() {
        return new TabListener();
    }
}
