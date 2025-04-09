package net.sf.sbcc.image.selectionarea.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.sf.sbcc.image.layer.model.AbstractBinaryLayer;
import net.sf.sbcc.image.layer.model.LayerChangeEvent.LayerChangeReason;
import net.sf.sbcc.image.selectionarea.BinaryArrayLogic;

/**
 * Modifications:
 * <ul>
 * <li> Implemented new data format version 3 for serialisation: the boolean[][]
 * stream now is zipped. (2008-03-18, Christoph Bimminger)</li>
 * <li> Initialized the default version of the data format used for
 * serialization. (2008-03-18, Christoph Bimminger)</li>
 * </ul>

 * <br>
 * Modifications:
 * <ul>
 * <!--
 * <li> some text here (2008-mm-dd, Christoph Bimminger)</li>
 * -->
 * </ul>
 * <br>
 * <br>
 * <i>This class is part of the Swing Better Components Collection (SBCC), which is an open source project. 
 * The project is available at <a href="http://sbcc.sourceforge.net" >http://sbcc.sourceforge.net</a> and
 * is distributed under the conditions of the GNU Library or Lesser General Public License (LGPL).</i><br>
 * <br>
 * Filename: SelectionMask.java<br>
 * Last modified: 2008-04-19<br>
 * 
 * @author Christoph Bimminger

 
 */
public class SelectionMask extends AbstractBinaryLayer implements Serializable {

    /**
	 * Version info for serializable object
	 */
    private static final long serialVersionUID = 2L;

    private transient Color color;

    private transient BufferedImage smallImage;

    private int version;

    public SelectionMask() {
        super();
    }

    public SelectionMask(boolean[][] P_mask, String P_name, Color P_color) {
        super(P_mask, P_name);
        color = P_color;
    }

    public SelectionMask(int P_imgWidth, int P_imgHeight, String P_name, Color P_color) {
        super(P_imgWidth, P_imgHeight, P_name);
        color = P_color;
    }

    public SelectionMask subtract(SelectionMask other) {
        SelectionMask F_ret = new SelectionMask(getWidth(), getHeight(), getName(), color);
        F_ret.setAlpha(getAlpha());
        F_ret.setActivation(getActivation());
        F_ret.setVisible(isVisible());
        F_ret.mask = BinaryArrayLogic.process(mask, other.mask, new BinaryArrayLogic.SUBTRACT());
        return F_ret;
    }

    public SelectionMask extend(SelectionMask other) {
        SelectionMask F_ret = new SelectionMask(getWidth(), getHeight(), getName(), color);
        F_ret.setAlpha(getAlpha());
        F_ret.setActivation(getActivation());
        F_ret.setVisible(isVisible());
        F_ret.mask = BinaryArrayLogic.process(mask, other.mask, new BinaryArrayLogic.OR());
        return F_ret;
    }

    @Override
    public BufferedImage createImage() {
        BufferedImage F_ret = new BufferedImage(getMaskWidth(), getMaskHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = F_ret.createGraphics();
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), getAlpha());
        Color transparent = new Color(0, 0, 0, 0);
        int[] rgbArray = new int[getMaskWidth() * getMaskHeight()];
        for (int y = 0; y < getMaskHeight(); y++) for (int x = 0; x < getMaskWidth(); x++) {
            Color v;
            if (getMask(x, y)) {
                v = c;
                F_ret.setRGB(x, y, c.getRGB());
            } else {
                v = transparent;
            }
            rgbArray[x + y * getMaskWidth()] = v.getRGB();
        }
        return F_ret;
    }

    public void setColor(Color P_color) {
        color = P_color;
        fireLayerChanged(LayerChangeReason.DATA_NONPERSISTENT);
    }

    public BufferedImage getSmallImage() {
        if (smallImage == null) {
            smallImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics g = smallImage.getGraphics();
            g.setColor(color);
            g.fillRect(0, 0, 32, 32);
        }
        return smallImage;
    }

    public boolean[][] getMask() {
        return mask;
    }

    public void setMask(boolean[][] P_mask) {
        mask = P_mask;
        fireLayerChanged(LayerChangeReason.DATA);
    }

    /**
	 * Serialization of {@link SelectionMask}.<br>
	 * Version 1 did not serialize the mask.<br>
	 * Version 2 contained mask serialization.<br>
	 * Version 3 serializes the mask with ZIP compression.<br>
	 * 
	 * @param out
	 *            The output stream used as serialization stream.
	 * @throws IOException
	 * @see {@link Serializable}
	 */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        if (version == 0) {
            version = 3;
        }
        out.writeLong(version);
        switch(version) {
            case 2:
                out.writeObject(mask);
                break;
            case 3:
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ZipOutputStream zipStream = new ZipOutputStream(byteStream);
                zipStream.putNextEntry(new ZipEntry("mask"));
                ObjectOutputStream objStream = new ObjectOutputStream(zipStream);
                objStream.writeObject(mask);
                objStream.close();
                zipStream.close();
                byte[] compressed = byteStream.toByteArray();
                out.writeObject(compressed);
                byteStream.close();
                break;
            default:
                throw new RuntimeException("version not supported: " + version);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        version = (int) in.readLong();
        switch(version) {
            case 2:
                mask = (boolean[][]) in.readObject();
                version = 3;
                break;
            case 3:
                byte[] compressed = (byte[]) in.readObject();
                ByteArrayInputStream F_byteStream = new ByteArrayInputStream(compressed);
                ZipInputStream F_zipStream = new ZipInputStream(F_byteStream);
                F_zipStream.getNextEntry();
                ObjectInputStream F_objStream = new ObjectInputStream(F_zipStream);
                mask = (boolean[][]) F_objStream.readObject();
                F_objStream.close();
                F_zipStream.close();
                F_byteStream.close();
                break;
            default:
                throw new RuntimeException("version not supported: " + version);
        }
    }

    public void setVersion(int l) {
        version = l;
    }
}
