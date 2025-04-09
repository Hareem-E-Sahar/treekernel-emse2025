package org.openXpertya.model;

import org.openXpertya.util.CLogger;
import org.openXpertya.util.DB;
import org.openXpertya.util.Env;
import org.openXpertya.util.MimeType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *      Attachment Model.
 *      One Attachment can have multiple entries
 *
 *  @author Comunidad de Desarrollo openXpertya
 *         *Basado en Codigo Original Modificado, Revisado y Optimizado de:
 *         * Jorg Janke
 *  @version $Id: MAttachment.java,v 1.12 2005/03/11 20:28:32 jjanke Exp $
 */
public class M_D_File_Archive extends X_D_File_Archive {

    /** Static Logger */
    private static CLogger s_log = CLogger.getCLogger(M_D_File_Archive.class);

    /** Indicator for zip data */
    public static final String ZIP = "zip";

    /** Indicator for no data */
    public static final String NONE = ".";

    /** List of Entry Data */
    private ArrayList m_items = null;

    /**
     *      Standard Constructor
     *      @param ctx context
     *      @param AD_Attachment_ID id
     * @param trxName
     */
    public M_D_File_Archive(Properties ctx, int D_File_Archive_ID, String trxName) {
        super(ctx, D_File_Archive_ID, trxName);
        d_file_archive_id = D_File_Archive_ID;
    }

    /**
     *      Load Constructor
     *      @param ctx context
     *      @param rs result set
     * @param trxName
     */
    public M_D_File_Archive(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    /**
     *      New Constructor
     *      @param ctx context
     *      @param AD_Table_ID table
     *      @param Record_ID record
     * @param trxName
     */
    public M_D_File_Archive(Properties ctx, int AD_Table_ID, int Record_ID, String trxName) {
        this(ctx, 0, trxName);
    }

    /**
     *      Add new Data Entry
     *      @param file file
     *      @return true if added
     */
    private static int d_file_archive_id;

    public boolean addEntry(File file) {
        if (file == null) {
            log.warning("addEntry - No File");
            return false;
        }
        if (!file.exists() || file.isDirectory()) {
            log.warning("addEntry - not added - " + file + ", Exists=" + file.exists() + ", Directory=" + file.isDirectory());
            return false;
        }
        log.fine("addEntry - " + file);
        String name = file.getName();
        byte[] data = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 8];
            int length = -1;
            while ((length = fis.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            fis.close();
            data = os.toByteArray();
            os.close();
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "addEntry (file)", ioe);
        }
        return addEntry(name, data);
    }

    /**
     *      Add Entry
     *      @param item attachment entry
     *      @returns true if added
     *
     * @return
     */
    public boolean addEntry(MAttachmentEntry item) {
        if (item == null) {
            return false;
        }
        if (m_items == null) {
            loadLOBData();
        }
        boolean retValue = m_items.add(item);
        log.fine("addEntry - " + item.toStringX());
        addTextMsg(" ");
        return retValue;
    }

    /**
     *      Add new Data Entry
     *      @param name name
     *      @param data data
     *      @return true if added
     */
    public boolean addEntry(String name, byte[] data) {
        if ((name == null) || (data == null)) {
            return false;
        }
        return addEntry(new MAttachmentEntry(name, data));
    }

    /**
     *      Add to Text Msg
     *      @param added text
     */
    public void addTextMsg(String added) {
        String oldTextMsg = getTextMsg();
        if (oldTextMsg == null) {
        } else if (added != null) {
        }
    }

    /**
     *      Before Save
     *      @param newRecord new
     *      @return true if can be saved
     */
    protected boolean beforeSave(boolean newRecord) {
        return saveLOBData();
    }

    /**
     *      Delete Entry
     *      @param index index
     *      @return true if deleted
     */
    public boolean deleteEntry(int index) {
        if ((index >= 0) && (index < m_items.size())) {
            m_items.remove(index);
            log.config("Index=" + index + " - NewSize=" + m_items.size());
            return true;
        }
        log.warning("Not deleted Index=" + index + " - Size=" + m_items.size());
        return false;
    }

    /**
     *      Dump Entry Names
     */
    public void dumpEntryNames() {
        if (m_items == null) {
            loadLOBData();
        }
        if ((m_items == null) || (m_items.size() == 0)) {
            System.out.println("- no entries -");
            return;
        }
        System.out.println("- entries: " + m_items.size());
        for (int i = 0; i < m_items.size(); i++) {
            System.out.println("  - " + getEntryName(i));
        }
    }

    /**
     *      Load Data into local m_data
     *      @return true if success
     */
    private boolean loadLOBData() {
        m_items = new ArrayList();
        byte[] data = getBinartyfile();
        if (data == null) {
            return true;
        }
        log.fine("ZipSize=" + data.length);
        if (data.length == 0) {
            return true;
        }
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ZipInputStream zip = new ZipInputStream(in);
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int length = zip.read(buffer);
                while (length != -1) {
                    out.write(buffer, 0, length);
                    length = zip.read(buffer);
                }
                byte[] dataEntry = out.toByteArray();
                log.fine(name + " - size=" + dataEntry.length + " - zip=" + entry.getCompressedSize() + "(" + entry.getSize() + ") " + (entry.getCompressedSize() * 100 / entry.getSize()) + "%");
                m_items.add(new MAttachmentEntry(name, dataEntry, m_items.size() + 1));
                entry = zip.getNextEntry();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "loadLOBData", e);
            m_items = null;
            return false;
        }
        return true;
    }

    /**
     *      Test
     *      @param args ignored
     */
    public static void main(String[] args) {
        System.out.println(MimeType.getMimeType("data.xls"));
        System.out.println(MimeType.getMimeType("data.cvs"));
        System.out.println(MimeType.getMimeType("data.txt"));
        System.out.println(MimeType.getMimeType("data.log"));
        System.out.println(MimeType.getMimeType("data.html"));
        System.out.println(MimeType.getMimeType("data.htm"));
        System.out.println(MimeType.getMimeType("data.png"));
        System.out.println(MimeType.getMimeType("data.gif"));
        System.out.println(MimeType.getMimeType("data.jpg"));
        System.out.println(MimeType.getMimeType("data.xml"));
        System.out.println(MimeType.getMimeType("data.rtf"));
        System.exit(0);
        org.openXpertya.OpenXpertya.startupEnvironment(true);
        M_D_File_Archive att = new M_D_File_Archive(Env.getCtx(), 100, 0, null);
        att.addEntry(new File("C:\\OpenXpertya\\Dev.properties"));
        att.addEntry(new File("C:\\ServidorOXP\\index.html"));
        att.save();
        System.out.println(att);
        att.dumpEntryNames();
        int AD_Attachment_ID = att.getD_File_Archive_ID();
        System.out.println("===========================================");
        att = new M_D_File_Archive(Env.getCtx(), AD_Attachment_ID, null);
        System.out.println(att);
        att.dumpEntryNames();
        System.out.println("===========================================");
        MAttachmentEntry[] entries = att.getEntries();
        for (int i = 0; i < entries.length; i++) {
            MAttachmentEntry entry = entries[i];
            entry.dump();
        }
        System.out.println("===========================================");
        att.delete(true);
    }

    /**
     *      Save Entry Data in Zip File format
     *      @return true if saved
     */
    private boolean saveLOBData() {
        if ((m_items == null) || (m_items.size() == 0)) {
            setBinartyfile(null);
            return true;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out);
        zip.setMethod(ZipOutputStream.DEFLATED);
        zip.setLevel(Deflater.BEST_COMPRESSION);
        zip.setComment("openXpertya");
        try {
            for (int i = 0; i < m_items.size(); i++) {
                MAttachmentEntry item = getEntry(i);
                ZipEntry entry = new ZipEntry(item.getName());
                entry.setTime(System.currentTimeMillis());
                entry.setMethod(ZipEntry.DEFLATED);
                zip.putNextEntry(entry);
                byte[] data = item.getData();
                zip.write(data, 0, data.length);
                zip.closeEntry();
            }
            zip.close();
            byte[] zipData = out.toByteArray();
            log.fine("Length=" + zipData.length);
            setBinartyfile(zipData);
            return true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "saveLOBData", e);
        }
        setBinartyfile(null);
        return false;
    }

    /**
     *      String Representation
     *      @return info
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("M_D_File_Archive[");
        for (int i = 0; i < getEntryCount(); i++) {
            if (i == 0) {
                sb.append(":");
            } else {
                sb.append(",");
            }
            sb.append(getEntryName(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     *      Get Attachment
     *      @param ctx context
     *      @param AD_Table_ID table
     *      @param Record_ID record
     *      @return attachment or null
     */
    public static M_D_File_Archive get(Properties ctx, int D_File_ID) {
        M_D_File_Archive retValue = null;
        PreparedStatement pstmt = null;
        String sql = "SELECT * FROM D_File_Archive WHERE D_File_ID=? AND D_File_Archive_ID=?";
        try {
            pstmt = DB.prepareStatement(sql);
            pstmt.setInt(1, D_File_ID);
            pstmt.setInt(2, d_file_archive_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                retValue = new M_D_File_Archive(ctx, rs, null);
            }
            rs.close();
            pstmt.close();
            pstmt = null;
        } catch (Exception e) {
            s_log.log(Level.SEVERE, "MAttachment", e);
        }
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            pstmt = null;
        } catch (Exception e) {
            pstmt = null;
        }
        return retValue;
    }

    /**
     *      Get Attachment Entries as array
     *      @returns array or null
     *
     * @return
     */
    public MAttachmentEntry[] getEntries() {
        if (m_items == null) {
            loadLOBData();
        }
        MAttachmentEntry[] retValue = new MAttachmentEntry[m_items.size()];
        m_items.toArray(retValue);
        return retValue;
    }

    /**
     *      Get Attachment Entry
     *      @param index index of the item
     *      @returns Entry or null
     *
     * @return
     */
    public MAttachmentEntry getEntry(int index) {
        if (m_items == null) {
            loadLOBData();
        }
        if ((index < 0) || (index >= m_items.size())) {
            return null;
        }
        return (MAttachmentEntry) m_items.get(index);
    }

    /**
     *      Get Entry Count
     *      @return number of entries
     */
    public int getEntryCount() {
        if (m_items == null) {
            loadLOBData();
        }
        return m_items.size();
    }

    /**
     *      Get Entry Data
     *      @param index index
     *      @return data or null
     */
    public byte[] getEntryData(int index) {
        MAttachmentEntry item = getEntry(index);
        if (item != null) {
            return item.getData();
        }
        return null;
    }

    /**
     *      Get Entry File with name
     *      @param index index
     *      @param file file
     *      @return file
     */
    public File getEntryFile(int index, File file) {
        MAttachmentEntry item = getEntry(index);
        if (item != null) {
            return item.getFile(file);
        }
        return null;
    }

    /**
     *      Get Entry File with name
     *      @param index index
     *      @param fileName optional file name
     *      @return file
     */
    public File getEntryFile(int index, String fileName) {
        MAttachmentEntry item = getEntry(index);
        if (item != null) {
            return item.getFile(fileName);
        }
        return null;
    }

    /**
     *      Get Entry Name
     *      @param index index
     *      @return name or null
     */
    public String getEntryName(int index) {
        MAttachmentEntry item = getEntry(index);
        if (item != null) {
            return item.getName();
        }
        return null;
    }

    /**
     *      Get Text Msg
     *      @return trimmed message
     */
    public String getTextMsg() {
        String msg = null;
        if (msg == null) {
            return null;
        }
        return msg.trim();
    }
}
