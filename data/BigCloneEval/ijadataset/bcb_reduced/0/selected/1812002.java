package org.dspace.storage.bitstore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dspace.checker.BitstreamInfoDAO;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralFileOutputStream;
import edu.sdsc.grid.io.local.LocalFile;
import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileSystem;

/**
 * <P>
 * Stores, retrieves and deletes bitstreams.
 * </P>
 * 
 * <P>
 * Presently, asset stores are specified in <code>dspace.cfg</code>. Since
 * Java does not offer a way of detecting free disk space, the asset store to
 * use for new bitstreams is also specified in a configuration property. The
 * drawbacks to this are that the administrators are responsible for monitoring
 * available space in the asset stores, and DSpace (Tomcat) has to be restarted
 * when the asset store for new ('incoming') bitstreams is changed.
 * </P>
 * 
 * <P>
 * Mods by David Little, UCSD Libraries 12/21/04 to allow the registration of
 * files (bitstreams) into DSpace.
 * </P>
 * 
 * <p>Cleanup integration with checker package by Nate Sarr 2006-01. N.B. The 
 * dependency on the checker package isn't ideal - a Listener pattern would be 
 * better but was considered overkill for the purposes of integrating the checker.
 * It would be worth re-considering a Listener pattern if another package needs to 
 * be notified of BitstreamStorageManager actions.</p> 
 *
 * @author Peter Breton, Robert Tansley, David Little, Nathan Sarr
 * @version $Revision: 1593 $
 */
public class BitstreamStorageManager {

    /** log4j log */
    private static Logger log = Logger.getLogger(BitstreamStorageManager.class);

    /**
	 * The asset store locations. The information for each GeneralFile in the
	 * array comes from dspace.cfg, so see the comments in that file.
	 *
	 * If an array element refers to a conventional (non_SRB) asset store, the
	 * element will be a LocalFile object (similar to a java.io.File object)
	 * referencing a local directory under which the bitstreams are stored.
	 *
	 * If an array element refers to an SRB asset store, the element will be an
	 * SRBFile object referencing an SRB 'collection' (directory) under which
	 * the bitstreams are stored.
	 *
	 * An SRBFile object is obtained by (1) using dspace.cfg properties to
	 * create an SRBAccount object (2) using the account to create an
	 * SRBFileSystem object (similar to a connection) (3) using the
	 * SRBFileSystem object to create an SRBFile object
	 */
    private static GeneralFile[] assetStores;

    /** The asset store to use for new bitstreams */
    private static int incoming;

    private static final int digitsPerLevel = 2;

    private static final int directoryLevels = 3;

    /**
	 * This prefix string marks registered bitstreams in internal_id
	 */
    private static final String REGISTERED_FLAG = "-R";

    static {
        ArrayList stores = new ArrayList();
        String sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir");
        if (sAssetstoreDir != null) {
            stores.add(sAssetstoreDir);
        } else if (ConfigurationManager.getProperty("srb.host") != null) {
            stores.add(new SRBAccount(ConfigurationManager.getProperty("srb.host"), ConfigurationManager.getIntProperty("srb.port"), ConfigurationManager.getProperty("srb.username"), ConfigurationManager.getProperty("srb.password"), ConfigurationManager.getProperty("srb.homedirectory"), ConfigurationManager.getProperty("srb.mdasdomainname"), ConfigurationManager.getProperty("srb.defaultstorageresource"), ConfigurationManager.getProperty("srb.mcatzone")));
        } else {
            log.error("No default assetstore");
        }
        for (int i = 1; ; i++) {
            sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir." + i);
            if (sAssetstoreDir != null) {
                stores.add(sAssetstoreDir);
            } else if (ConfigurationManager.getProperty("srb.host." + i) != null) {
                stores.add(new SRBAccount(ConfigurationManager.getProperty("srb.host." + i), ConfigurationManager.getIntProperty("srb.port." + i), ConfigurationManager.getProperty("srb.username." + i), ConfigurationManager.getProperty("srb.password." + i), ConfigurationManager.getProperty("srb.homedirectory." + i), ConfigurationManager.getProperty("srb.mdasdomainname." + i), ConfigurationManager.getProperty("srb.defaultstorageresource." + i), ConfigurationManager.getProperty("srb.mcatzone." + i)));
            } else {
                break;
            }
        }
        assetStores = new GeneralFile[stores.size()];
        for (int i = 0; i < stores.size(); i++) {
            Object o = stores.get(i);
            if (o == null) {
                log.error("Problem with assetstore " + i);
            }
            if (o instanceof String) {
                assetStores[i] = new LocalFile((String) o);
            } else if (o instanceof SRBAccount) {
                SRBFileSystem srbFileSystem = null;
                try {
                    srbFileSystem = new SRBFileSystem((SRBAccount) o);
                } catch (NullPointerException e) {
                    log.error("No SRBAccount for assetstore " + i);
                } catch (IOException e) {
                    log.error("Problem getting SRBFileSystem for assetstore" + i);
                }
                if (srbFileSystem == null) {
                    log.error("SRB FileSystem is null for assetstore " + i);
                }
                String sSRBAssetstore = null;
                if (i == 0) {
                    sSRBAssetstore = ConfigurationManager.getProperty("srb.parentdir");
                } else {
                    sSRBAssetstore = ConfigurationManager.getProperty("srb.parentdir." + i);
                }
                if (sSRBAssetstore == null) {
                    log.error("srb.parentdir is undefined for assetstore " + i);
                }
                assetStores[i] = new SRBFile(srbFileSystem, sSRBAssetstore);
            } else {
                log.error("Unexpected " + o.getClass().toString() + " with assetstore " + i);
            }
        }
        incoming = ConfigurationManager.getIntProperty("assetstore.incoming");
    }

    /**
     * Store a stream of bits.
     * 
     * <p>
     * If this method returns successfully, the bits have been stored, and RDBMS
     * metadata entries are in place (the context still needs to be completed to
     * finalize the transaction).
     * </p>
     * 
     * <p>
     * If this method returns successfully and the context is aborted, then the
     * bits will be stored in the asset store and the RDBMS metadata entries
     * will exist, but with the deleted flag set.
     * </p>
     * 
     * If this method throws an exception, then any of the following may be
     * true:
     * 
     * <ul>
     * <li>Neither bits nor RDBMS metadata entries have been stored.
     * <li>RDBMS metadata entries with the deleted flag set have been stored,
     * but no bits.
     * <li>RDBMS metadata entries with the deleted flag set have been stored,
     * and some or all of the bits have also been stored.
     * </ul>
     * 
     * @param context
     *            The current context
     * @param is
     *            The stream of bits to store
     * @exception IOException
     *                If a problem occurs while storing the bits
     * @exception SQLException
     *                If a problem occurs accessing the RDBMS
     * 
     * @return The ID of the stored bitstream
     */
    public static int store(Context context, InputStream is) throws SQLException, IOException {
        String id = Utils.generateKey();
        TableRow bitstream;
        Context tempContext = null;
        try {
            tempContext = new Context();
            bitstream = DatabaseManager.create(tempContext, "Bitstream");
            bitstream.setColumn("deleted", true);
            bitstream.setColumn("internal_id", id);
            bitstream.setColumn("store_number", incoming);
            DatabaseManager.update(tempContext, bitstream);
            tempContext.complete();
        } catch (SQLException sqle) {
            if (tempContext != null) {
                tempContext.abort();
            }
            throw sqle;
        }
        GeneralFile file = getFile(bitstream);
        GeneralFile parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        file.createNewFile();
        GeneralFileOutputStream fos = FileFactory.newFileOutputStream(file);
        DigestInputStream dis = null;
        try {
            dis = new DigestInputStream(is, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException nsae) {
            log.warn("Caught NoSuchAlgorithmException", nsae);
        }
        Utils.bufferedCopy(dis, fos);
        fos.close();
        is.close();
        bitstream.setColumn("size_bytes", file.length());
        bitstream.setColumn("checksum", Utils.toHex(dis.getMessageDigest().digest()));
        bitstream.setColumn("checksum_algorithm", "MD5");
        bitstream.setColumn("deleted", false);
        DatabaseManager.update(context, bitstream);
        int bitstream_id = bitstream.getIntColumn("bitstream_id");
        if (log.isDebugEnabled()) {
            log.debug("Stored bitstream " + bitstream_id + " in file " + file.getAbsolutePath());
        }
        return bitstream_id;
    }

    /**
	 * Register a bitstream already in storage.
	 *
	 * @param context
	 *            The current context
	 * @param assetstore The assetstore number for the bitstream to be
	 * 			registered
	 * @param bitstreamPath The relative path of the bitstream to be registered.
	 * 		The path is relative to the path of ths assetstore.
	 * @return The ID of the registered bitstream
	 * @exception SQLException
	 *                If a problem occurs accessing the RDBMS
	 * @throws IOExeption
	 */
    public static int register(Context context, int assetstore, String bitstreamPath) throws SQLException, IOException {
        String sInternalId = REGISTERED_FLAG + bitstreamPath;
        TableRow bitstream;
        Context tempContext = null;
        try {
            tempContext = new Context();
            bitstream = DatabaseManager.create(tempContext, "Bitstream");
            bitstream.setColumn("deleted", true);
            bitstream.setColumn("internal_id", sInternalId);
            bitstream.setColumn("store_number", assetstore);
            DatabaseManager.update(tempContext, bitstream);
            tempContext.complete();
        } catch (SQLException sqle) {
            if (tempContext != null) {
                tempContext.abort();
            }
            throw sqle;
        }
        GeneralFile file = getFile(bitstream);
        if (file instanceof LocalFile) {
            DigestInputStream dis = null;
            try {
                dis = new DigestInputStream(FileFactory.newFileInputStream(file), MessageDigest.getInstance("MD5"));
            } catch (NoSuchAlgorithmException e) {
                log.warn("Caught NoSuchAlgorithmException", e);
                throw new IOException("Invalid checksum algorithm");
            } catch (IOException e) {
                log.error("File: " + file.getAbsolutePath() + " to be registered cannot be opened - is it " + "really there?");
                throw e;
            }
            final int BUFFER_SIZE = 1024 * 4;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                final int count = dis.read(buffer, 0, BUFFER_SIZE);
                if (count == -1) {
                    break;
                }
            }
            bitstream.setColumn("checksum", Utils.toHex(dis.getMessageDigest().digest()));
            dis.close();
        } else if (file instanceof SRBFile) {
            if (!file.exists()) {
                log.error("File: " + file.getAbsolutePath() + " is not in SRB MCAT");
                throw new IOException("File is not in SRB MCAT");
            }
            int iLastSlash = bitstreamPath.lastIndexOf('/');
            String sFilename = bitstreamPath.substring(iLastSlash + 1);
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                log.error("Caught NoSuchAlgorithmException", e);
                throw new IOException("Invalid checksum algorithm");
            }
            bitstream.setColumn("checksum", Utils.toHex(md.digest(sFilename.getBytes())));
        } else {
            throw new IOException("Unrecognized file type - " + "not local, not SRB");
        }
        bitstream.setColumn("checksum_algorithm", "MD5");
        bitstream.setColumn("size_bytes", file.length());
        bitstream.setColumn("deleted", false);
        DatabaseManager.update(context, bitstream);
        int bitstream_id = bitstream.getIntColumn("bitstream_id");
        if (log.isDebugEnabled()) {
            log.debug("Stored bitstream " + bitstream_id + " in file " + file.getAbsolutePath());
        }
        return bitstream_id;
    }

    /**
	 * Does the internal_id column in the bitstream row indicate the bitstream
	 * is a registered file
	 *
	 * @param internalId the value of the internal_id column
	 * @return true if the bitstream is a registered file
	 */
    public static boolean isRegisteredBitstream(String internalId) {
        if (internalId.substring(0, REGISTERED_FLAG.length()).equals(REGISTERED_FLAG)) {
            return true;
        }
        return false;
    }

    /**
     * Retrieve the bits for the bitstream with ID. If the bitstream does not
     * exist, or is marked deleted, returns null.
     * 
     * @param context
     *            The current context
     * @param id
     *            The ID of the bitstream to retrieve
     * @exception IOException
     *                If a problem occurs while retrieving the bits
     * @exception SQLException
     *                If a problem occurs accessing the RDBMS
     * 
     * @return The stream of bits, or null
     */
    public static InputStream retrieve(Context context, int id) throws SQLException, IOException {
        TableRow bitstream = DatabaseManager.find(context, "bitstream", id);
        GeneralFile file = getFile(bitstream);
        return (file != null) ? FileFactory.newFileInputStream(file) : null;
    }

    /**
     * <p>
     * Remove a bitstream from the asset store. This method does not delete any
     * bits, but simply marks the bitstreams as deleted (the context still needs
     * to be completed to finalize the transaction).
     * </p>
     * 
     * <p>
     * If the context is aborted, the bitstreams deletion status remains
     * unchanged.
     * </p>
     * 
     * @param context
     *            The current context
     * @param id
     *            The ID of the bitstream to delete
     * @exception SQLException
     *                If a problem occurs accessing the RDBMS
     */
    public static void delete(Context context, int id) throws SQLException {
        DatabaseManager.updateQuery(context, "update Bitstream set deleted = '1' where bitstream_id = ? ", id);
    }

    /**
     * Clean up the bitstream storage area. This method deletes any bitstreams
     * which are more than 1 hour old and marked deleted. The deletions cannot
     * be undone.
     * 
     * @param deleteDbRecords if true deletes the database records otherwise it
     * 	           only deletes the files and directories in the assetstore  
     * @exception IOException
     *                If a problem occurs while cleaning up
     * @exception SQLException
     *                If a problem occurs accessing the RDBMS
     */
    public static void cleanup(boolean deleteDbRecords) throws SQLException, IOException {
        Context context = null;
        BitstreamInfoDAO bitstreamInfoDAO = new BitstreamInfoDAO();
        int commit_counter = 0;
        try {
            context = new Context();
            String myQuery = "select * from Bitstream where deleted = '1'";
            List storage = DatabaseManager.queryTable(context, "Bitstream", myQuery).toList();
            for (Iterator iterator = storage.iterator(); iterator.hasNext(); ) {
                TableRow row = (TableRow) iterator.next();
                int bid = row.getIntColumn("bitstream_id");
                GeneralFile file = getFile(row);
                if (file == null || !file.exists()) {
                    log.debug("file is null");
                    if (deleteDbRecords) {
                        log.debug("deleting record");
                        bitstreamInfoDAO.deleteBitstreamInfoWithHistory(bid);
                        DatabaseManager.delete(context, "Bitstream", bid);
                    }
                    continue;
                }
                if (isRecent(file)) {
                    log.debug("file is recent");
                    continue;
                }
                if (deleteDbRecords) {
                    log.debug("deleting db record");
                    bitstreamInfoDAO.deleteBitstreamInfoWithHistory(bid);
                    DatabaseManager.delete(context, "Bitstream", bid);
                }
                if (isRegisteredBitstream(row.getStringColumn("internal_id"))) {
                    continue;
                }
                boolean success = file.delete();
                if (log.isDebugEnabled()) {
                    log.debug("Deleted bitstream " + bid + " (file " + file.getAbsolutePath() + ") with result " + success);
                }
                if (success) {
                    deleteParents(file);
                }
                commit_counter++;
                if (commit_counter % 100 == 0) {
                    context.commit();
                }
            }
            context.complete();
        } catch (SQLException sqle) {
            context.abort();
            throw sqle;
        } catch (IOException ioe) {
            context.abort();
            throw ioe;
        }
    }

    /**
     * Return true if this file is too recent to be deleted, false otherwise.
     * 
     * @param file
     *            The file to check
     * @return True if this file is too recent to be deleted
     */
    private static boolean isRecent(GeneralFile file) {
        long lastmod = file.lastModified();
        long now = new java.util.Date().getTime();
        if (lastmod >= now) {
            return true;
        }
        return (now - lastmod) < (1 * 60 * 1000);
    }

    /**
     * Delete empty parent directories.
     * 
     * @param file
     *            The file with parent directories to delete
     */
    private static synchronized void deleteParents(GeneralFile file) {
        if (file == null) {
            return;
        }
        GeneralFile tmp = file;
        for (int i = 0; i < directoryLevels; i++) {
            GeneralFile directory = tmp.getParentFile();
            GeneralFile[] files = directory.listFiles();
            if (files.length != 0) {
                break;
            }
            directory.delete();
            tmp = directory;
        }
    }

    /**
     * Return the file corresponding to a bitstream. It's safe to pass in
     * <code>null</code>.
     * 
     * @param bitstream
     *            the database table row for the bitstream. Can be
     *            <code>null</code>
     * 
     * @return The corresponding file in the file system, or <code>null</code>
     * 
     * @exception IOException
     *                If a problem occurs while determining the file
     */
    private static GeneralFile getFile(TableRow bitstream) throws IOException {
        if (bitstream == null) {
            return null;
        }
        int storeNumber = bitstream.getIntColumn("store_number");
        if (storeNumber == -1) {
            storeNumber = 0;
        }
        GeneralFile assetstore = assetStores[storeNumber];
        String sInternalId = bitstream.getStringColumn("internal_id");
        String sIntermediatePath = null;
        if (isRegisteredBitstream(sInternalId)) {
            sInternalId = sInternalId.substring(REGISTERED_FLAG.length());
            sIntermediatePath = "";
        } else {
            if (sInternalId.indexOf(File.separator) != -1) sInternalId = sInternalId.substring(sInternalId.lastIndexOf(File.separator) + 1);
            sIntermediatePath = getIntermediatePath(sInternalId);
        }
        StringBuffer bufFilename = new StringBuffer();
        if (assetstore instanceof LocalFile) {
            bufFilename.append(assetstore.getCanonicalPath());
            bufFilename.append(File.separator);
            bufFilename.append(sIntermediatePath);
            bufFilename.append(sInternalId);
            if (log.isDebugEnabled()) {
                log.debug("Local filename for " + sInternalId + " is " + bufFilename.toString());
            }
            return new LocalFile(bufFilename.toString());
        }
        if (assetstore instanceof SRBFile) {
            bufFilename.append(sIntermediatePath);
            bufFilename.append(sInternalId);
            if (log.isDebugEnabled()) {
                log.debug("SRB filename for " + sInternalId + " is " + ((SRBFile) assetstore).toString() + bufFilename.toString());
            }
            return new SRBFile((SRBFile) assetstore, bufFilename.toString());
        }
        return null;
    }

    /**
	 * Return the intermediate path derived from the internal_id. This method
	 * splits the id into groups which become subdirectories.
	 *
	 * @param iInternalId
	 *            The internal_id
	 * @return The path based on the id without leading or trailing separators
	 */
    private static String getIntermediatePath(String iInternalId) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < directoryLevels; i++) {
            int digits = i * digitsPerLevel;
            if (i > 0) {
                buf.append(File.separator);
            }
            buf.append(iInternalId.substring(digits, digits + digitsPerLevel));
        }
        buf.append(File.separator);
        return buf.toString();
    }
}
