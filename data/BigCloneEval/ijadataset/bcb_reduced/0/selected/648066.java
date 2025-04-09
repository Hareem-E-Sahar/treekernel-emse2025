package com.thyante.thelibrarian.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.Attributes;
import com.thyante.thelibrarian.Resources;
import com.thyante.thelibrarian.icons.Icon;
import com.thyante.thelibrarian.util.ImageUtil;
import com.thyante.thelibrarian.util.XMLUtil;
import com.thyante.thelibrarian.view.groupcollectionview.GroupCollectionView;

/**
 * This class stores meta data information about a collection. 
 * @author Matthias-M. Christen
 */
public class CollectionMetaData {

    public static final String PROP_CREATOR = "creator";

    public static final String PROP_DESCRIPTION = "description";

    public static final String PROP_REMARKS = "remarks";

    public static final String PROP_ICON = "icon";

    /**
	 * The collection's creator
	 */
    protected String m_strCreator;

    /**
	 * Description of the collection
	 */
    protected String m_strDescription;

    /**
	 * Collection remarks 
	 */
    protected String m_strRemarks;

    /**
	 * The image representing the collection's template, also
	 * used as default thumbnail image for items without an
	 * image assigned
	 */
    protected Image m_imgIcon;

    protected Image m_imgSmallIcon;

    protected Image m_imgReflectionIcon;

    protected String m_strImagePath;

    protected List<IConfigurationChangedListener> m_listChangeListeners;

    /**
	 * Constructs a meta data object with default values.
	 */
    public CollectionMetaData() {
        m_strCreator = "";
        m_strDescription = "";
        m_strRemarks = "";
        m_imgIcon = null;
        m_imgSmallIcon = null;
        m_imgReflectionIcon = null;
        m_strImagePath = "intern://record0";
        m_listChangeListeners = new LinkedList<IConfigurationChangedListener>();
    }

    /**
	 * Loads collection meta data from the XML attributes <code>attrs</code>.
	 * @param attrs The XML attributes from which the meta data is loaded
	 */
    public void fromNode(Attributes attrs) {
        m_strCreator = attrs.getValue("creator");
        m_strDescription = attrs.getValue("description");
        m_strRemarks = attrs.getValue("remarks");
        m_strImagePath = attrs.getValue("icon");
    }

    public void loadIcons(ZipFile fileIn) {
        try {
            ZipEntry zeIcon = fileIn.getEntry("icon.png");
            if (zeIcon != null) m_imgIcon = new Image(Display.getDefault(), fileIn.getInputStream(zeIcon));
        } catch (IOException e) {
        }
        try {
            ZipEntry zeIcon = fileIn.getEntry("icon-small.png");
            if (zeIcon != null) m_imgSmallIcon = new Image(Display.getDefault(), fileIn.getInputStream(zeIcon));
        } catch (IOException e) {
        }
        try {
            ZipEntry zeIcon = fileIn.getEntry("icon-reflection.png");
            if (zeIcon != null) m_imgReflectionIcon = new Image(Display.getDefault(), fileIn.getInputStream(zeIcon));
        } catch (IOException e) {
        }
    }

    /**
	 * Writes the meta data to the current XML node in the stream
	 * @param p The {@link PrintWriter} to write the data to
	 */
    public void toNode(PrintWriter p) {
        XMLUtil.printAttribute(p, "creator", m_strCreator);
        XMLUtil.printAttribute(p, "description", m_strDescription);
        XMLUtil.printAttribute(p, "remarks", m_strRemarks);
        if (m_strImagePath != null && m_strImagePath.startsWith("intern://")) XMLUtil.printAttribute(p, "icon", m_strImagePath);
    }

    /**
	 * Stores the icons in the output stream <code>out</code>.
	 * @param out The output stream to which the collection icons are written
	 */
    public void storeIcons(ZipOutputStream out) {
        if (m_strImagePath == null || (m_strImagePath != null && !m_strImagePath.startsWith("intern://"))) {
            try {
                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[] { getIcon().getImageData() };
                out.putNextEntry(new ZipEntry("icon.png"));
                loader.save(out, SWT.IMAGE_PNG);
                out.closeEntry();
                loader.data = new ImageData[] { getSmallIcon().getImageData() };
                out.putNextEntry(new ZipEntry("icon-small.png"));
                loader.save(out, SWT.IMAGE_PNG);
                out.closeEntry();
                loader.data = new ImageData[] { getDefaultReflectionImage().getImageData() };
                out.putNextEntry(new ZipEntry("icon-reflection.png"));
                loader.save(out, SWT.IMAGE_PNG);
                out.closeEntry();
            } catch (IOException e) {
            }
        }
    }

    /**
	 * Returns the creator of the collection
	 * @return The collection creator
	 */
    public String getCreator() {
        return m_strCreator;
    }

    /**
	 * Sets the creator of the collection
	 * @param strCreator The collection creator
	 */
    public void setCreator(String strCreator) {
        if (strCreator == null) {
            if (m_strCreator == null) return;
        } else if (strCreator.equals(m_strCreator)) return;
        m_strCreator = strCreator;
        fireDataChanged(PROP_CREATOR);
    }

    /**
	 * Returns the collection description.
	 * @return The collection description
	 */
    public String getDescription() {
        return m_strDescription;
    }

    /**
	 * Sets the collection description.
	 * @param strDescription The collection description
	 */
    public void setDescription(String strDescription) {
        if (strDescription == null) {
            if (m_strDescription == null) return;
        } else if (strDescription.equals(m_strDescription)) return;
        m_strDescription = strDescription;
        fireDataChanged(PROP_DESCRIPTION);
    }

    /**
	 * Returns the collection remarks.
	 * @return The collection remarks
	 */
    public String getRemarks() {
        return m_strRemarks;
    }

    /**
	 * Sets the collection remarks.
	 * @param strRemarks The collection remarks
	 */
    public void setRemarks(String strRemarks) {
        if (strRemarks == null) {
            if (m_strRemarks == null) return;
        } else if (strRemarks.equals(m_strRemarks)) return;
        m_strRemarks = strRemarks;
        fireDataChanged(PROP_REMARKS);
    }

    /**
	 * Returns the standard-sized (32x32 pixel) icon representing the collection. 
	 * @return The collection icon
	 */
    public Image getIcon() {
        if (m_imgIcon == null) {
            m_imgIcon = Icon.createCollectionIcon(Display.getDefault(), m_strImagePath, Icon.IconSize.STANDARD);
            if (m_strImagePath == null || "".equals(m_strImagePath)) m_strImagePath = "intern://record0";
        }
        return m_imgIcon;
    }

    /**
	 * Returns a small (16x16 pixel) icon representing the collection (e.g. for the tab items).
	 * @return The small collection item
	 */
    public Image getSmallIcon() {
        if (m_imgSmallIcon == null) {
            m_imgSmallIcon = Icon.createCollectionIcon(Display.getDefault(), m_strImagePath, Icon.IconSize.SMALL);
            if (m_strImagePath == null || "".equals(m_strImagePath)) m_strImagePath = "intern://record0";
        }
        return m_imgSmallIcon;
    }

    /**
	 * Returns the default reflection image if an internal image is chosen.
	 * If the icon path doesn't point to an internal image, <code>null</code>
	 * is returned.
	 * @return The default reflection image or <code>null</code> if the icon
	 * isn't an internal image
	 */
    public Image getDefaultReflectionImage() {
        if (m_imgReflectionIcon == null) {
            if (m_strImagePath == null || "".equals(m_strImagePath)) m_strImagePath = "intern://record0";
            if (m_strImagePath.startsWith("intern://")) m_imgReflectionIcon = Resources.getImage(m_strImagePath + Resources.IMAGE_SHADOW_SUFFIX); else {
                try {
                    m_imgReflectionIcon = ImageUtil.createReflectionImage(Display.getDefault(), new Image(Display.getDefault(), m_strImagePath), GroupCollectionView.ITEM_WIDTH, GroupCollectionView.ITEM_HEIGHT, ItemUI.REFLECTION_HEIGHT);
                } catch (Exception e) {
                    m_imgReflectionIcon = Resources.getImage("intern://record0" + Resources.IMAGE_SHADOW_SUFFIX);
                }
            }
        }
        return m_imgReflectionIcon;
    }

    /**
	 * Sets the standard sized (32x32 pixel) icon.
	 * @param strImagePath The path to the icon. For predefined icons use
	 * 	<code>"intern://record</code><i>n</i><code>"</code>
	 * 	where <i>n</i> is the number of the icon, 1 &lt;= <i>n</i> &lt;= {@link Resources#LAST_RECORD_ID}
	 */
    public void setIcon(String strImagePath) {
        if (strImagePath == null) {
            if (m_strImagePath == null) return;
        } else if (strImagePath.equals(m_strImagePath)) return;
        Image imgIconOld = m_imgIcon;
        Image imgSmallIconOld = m_imgSmallIcon;
        Image imgReflectionIconOld = m_imgReflectionIcon;
        String strImagePathOld = m_strImagePath;
        m_imgIcon = null;
        m_imgSmallIcon = null;
        m_imgReflectionIcon = null;
        m_strImagePath = strImagePath;
        fireDataChanged(PROP_ICON);
        disposeIcons(strImagePathOld, imgIconOld, imgSmallIconOld, imgReflectionIconOld);
    }

    public String getIconPath() {
        return m_strImagePath;
    }

    /**
	 * Disposes the icons that have been created for this collection.
	 */
    protected void disposeIcons(String strImagePath, Image imgIcon, Image imgSmallIcon, Image imgReflectionIcon) {
        if (strImagePath == null || !strImagePath.startsWith("intern://")) {
            if (imgIcon != null) imgIcon.dispose();
            if (imgSmallIcon != null) imgSmallIcon.dispose();
            if (imgReflectionIcon != null) imgReflectionIcon.dispose();
        }
    }

    /**
	 * Disposes used resources (icons).
	 */
    public void dispose() {
        disposeIcons(m_strImagePath, m_imgIcon, m_imgSmallIcon, m_imgReflectionIcon);
        m_imgIcon = null;
        m_imgSmallIcon = null;
        m_imgReflectionIcon = null;
    }

    /**
	 * Copies the values in <code>metaData</code> onto this object.
	 * @param metaData The meta data object containing the values to copy
	 */
    public void copyValuesFrom(CollectionMetaData metaData) {
        setCreator(metaData.getCreator());
        setDescription(metaData.getDescription());
        setRemarks(metaData.getRemarks());
        setIcon(metaData.getIconPath());
    }

    public CollectionMetaData clone() {
        CollectionMetaData metaData = new CollectionMetaData();
        metaData.setCreator(m_strCreator);
        metaData.setDescription(m_strDescription);
        metaData.setRemarks(m_strRemarks);
        metaData.setIcon(m_strImagePath);
        return metaData;
    }

    public void addChangeListener(IConfigurationChangedListener listener) {
        m_listChangeListeners.add(listener);
    }

    public void removeChangeListener(IConfigurationChangedListener listener) {
        m_listChangeListeners.remove(listener);
    }

    protected void fireDataChanged(String strProperty) {
        for (IConfigurationChangedListener l : m_listChangeListeners) l.onConfigurationChanged(strProperty);
    }
}
