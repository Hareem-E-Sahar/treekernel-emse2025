package org.compiere.model;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import org.compiere.util.*;

/**
 *  Image Model
 *  (DisplayType = 32)
 *
 *  @author Jorg Janke
 *  @version $Id: MImage.java,v 1.5 2006/07/30 00:51:02 jjanke Exp $
 */
public class MImage extends X_AD_Image {

    /**
	 * 	Get MImage from Cache
	 *	@param ctx context
	 *	@param AD_Image_ID id
	 *	@return MImage
	 */
    public static MImage get(Properties ctx, int AD_Image_ID) {
        if (AD_Image_ID == 0) return new MImage(ctx, AD_Image_ID, null);
        Integer key = new Integer(AD_Image_ID);
        MImage retValue = (MImage) s_cache.get(key);
        if (retValue != null) return retValue;
        retValue = new MImage(ctx, AD_Image_ID, null);
        if (retValue.get_ID() != 0) s_cache.put(key, retValue);
        return retValue;
    }

    /**	Cache						*/
    private static CCache<Integer, MImage> s_cache = new CCache<Integer, MImage>("AD_Image", 20);

    /**
	 *  Constructor
	 *  @param ctx context
	 *  @param AD_Image_ID image
	 *  @param trxName transaction
	 */
    public MImage(Properties ctx, int AD_Image_ID, String trxName) {
        super(ctx, AD_Image_ID, trxName);
        if (AD_Image_ID < 1) setName("-");
    }

    /**
	 * 	Load Constructor
	 *	@param ctx
	 *	@param rs
	 *  @param trxName transaction
	 */
    public MImage(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    /** The Image                   */
    private Image m_image = null;

    /** The Icon                   */
    private Icon m_icon = null;

    /**
	 * 	Get Image
	 *	@return image or null
	 */
    public Image getImage() {
        if (m_image != null) return m_image;
        byte[] data = getBinaryData();
        if (data != null && data.length > 0) {
            try {
                Toolkit tk = Toolkit.getDefaultToolkit();
                m_image = tk.createImage(data);
                return m_image;
            } catch (Exception e) {
                log.log(Level.WARNING, "(byteArray)", e);
                return null;
            }
        }
        URL url = getURL();
        if (url == null) return null;
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            m_image = tk.getImage(url);
            return m_image;
        } catch (Exception e) {
            log.log(Level.WARNING, "(URL)", e);
        }
        return null;
    }

    /**
	 * 	Get Icon
	 *	@return icon or null
	 */
    public Icon getIcon() {
        if (m_icon != null) return m_icon;
        byte[] data = getBinaryData();
        if (data != null && data.length > 0) {
            try {
                m_icon = new ImageIcon(data, getName());
                return m_icon;
            } catch (Exception e) {
                log.log(Level.WARNING, "(byteArray)", e);
                return null;
            }
        }
        URL url = getURL();
        if (url == null) return null;
        try {
            m_icon = new ImageIcon(url, getName());
            return m_icon;
        } catch (Exception e) {
            log.log(Level.WARNING, "(URL)", e);
        }
        return null;
    }

    /**
	 * 	Get URL
	 *	@return url or null
	 */
    private URL getURL() {
        String str = getImageURL();
        if (str == null || str.length() == 0) return null;
        URL url = null;
        try {
            if (str.indexOf("://") != -1) url = new URL(str); else url = getClass().getResource(str);
            if (url == null) log.warning("Not found: " + str);
        } catch (Exception e) {
            log.warning("Not found: " + str + " - " + e.getMessage());
        }
        return url;
    }

    /**
	 * 	Set Image URL
	 *	@param ImageURL url
	 */
    public void setImageURL(String ImageURL) {
        m_image = null;
        m_icon = null;
        super.setImageURL(ImageURL);
    }

    /**
	 * 	Set Binary Data
	 *	@param BinaryData data
	 */
    public void setBinaryData(byte[] BinaryData) {
        m_image = null;
        m_icon = null;
        super.setBinaryData(BinaryData);
    }

    /**
	 * 	Get Data 
	 *	@return data
	 */
    public byte[] getData() {
        byte[] data = super.getBinaryData();
        if (data != null) return data;
        String str = getImageURL();
        if (str == null || str.length() == 0) {
            log.config("No Image URL");
            return null;
        }
        URL url = getURL();
        if (url == null) {
            log.config("No URL");
            return null;
        }
        try {
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[1024 * 8];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int length = -1;
            while ((length = is.read(buffer)) != -1) os.write(buffer, 0, length);
            is.close();
            data = os.toByteArray();
            os.close();
        } catch (Exception e) {
            log.config(e.toString());
        }
        return data;
    }

    /**
	 *  String Representation
	 *  @return String
	 */
    public String toString() {
        return "MImage[ID=" + get_ID() + ",Name=" + getName() + "]";
    }

    /**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
    protected boolean beforeSave(boolean newRecord) {
        if (getAD_Org_ID() != 0) setAD_Org_ID(0);
        return true;
    }
}
