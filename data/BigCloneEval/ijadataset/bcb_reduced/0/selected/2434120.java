package org.apache.hadoop.hdfs.server.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hdfs.protocol.FSConstants;
import org.apache.hadoop.hdfs.server.common.HdfsConstants.NodeType;
import org.apache.hadoop.hdfs.server.common.HdfsConstants.StartupOption;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.VersionInfo;

/**
 * Storage information file.
 * <p>
 * Local storage information is stored in a separate file VERSION.
 * It contains type of the node, 
 * the storage layout version, the namespace id, and 
 * the fs state creation time.
 * <p>
 * Local storage can reside in multiple directories. 
 * Each directory should contain the same VERSION file as the others.
 * During startup Hadoop servers (name-node and data-nodes) read their local 
 * storage information from them.
 * <p>
 * The servers hold a lock for each storage directory while they run so that 
 * other nodes were not able to startup sharing the same storage.
 * The locks are released when the servers stop (normally or abnormally).
 * 
 */
public abstract class Storage extends StorageInfo {

    public static final Log LOG = LogFactory.getLog(Storage.class.getName());

    protected static final int LAST_PRE_UPGRADE_LAYOUT_VERSION = -3;

    public static final int LAST_UPGRADABLE_LAYOUT_VERSION = -7;

    protected static final String LAST_UPGRADABLE_HADOOP_VERSION = "Hadoop-0.14";

    public static final int PRE_GENERATIONSTAMP_LAYOUT_VERSION = -13;

    private static final String STORAGE_FILE_LOCK = "in_use.lock";

    protected static final String STORAGE_FILE_VERSION = "VERSION";

    public static final String STORAGE_DIR_CURRENT = "current";

    private static final String STORAGE_DIR_PREVIOUS = "previous";

    private static final String STORAGE_TMP_REMOVED = "removed.tmp";

    private static final String STORAGE_TMP_PREVIOUS = "previous.tmp";

    private static final String STORAGE_TMP_FINALIZED = "finalized.tmp";

    private static final String STORAGE_TMP_LAST_CKPT = "lastcheckpoint.tmp";

    private static final String STORAGE_PREVIOUS_CKPT = "previous.checkpoint";

    public enum StorageState {

        NON_EXISTENT, NOT_FORMATTED, COMPLETE_UPGRADE, RECOVER_UPGRADE, COMPLETE_FINALIZE, COMPLETE_ROLLBACK, RECOVER_ROLLBACK, COMPLETE_CHECKPOINT, RECOVER_CHECKPOINT, NORMAL
    }

    /**
   * An interface to denote storage directory type
   * Implementations can define a type for storage directory by implementing
   * this interface.
   */
    public interface StorageDirType {

        public StorageDirType getStorageDirType();

        public boolean isOfType(StorageDirType type);
    }

    private NodeType storageType;

    protected List<StorageDirectory> storageDirs = new ArrayList<StorageDirectory>();

    private class DirIterator implements Iterator<StorageDirectory> {

        StorageDirType dirType;

        int prevIndex;

        int nextIndex;

        DirIterator(StorageDirType dirType) {
            this.dirType = dirType;
            this.nextIndex = 0;
            this.prevIndex = 0;
        }

        public boolean hasNext() {
            if (storageDirs.isEmpty() || nextIndex >= storageDirs.size()) return false;
            if (dirType != null) {
                while (nextIndex < storageDirs.size()) {
                    if (getStorageDir(nextIndex).getStorageDirType().isOfType(dirType)) break;
                    nextIndex++;
                }
                if (nextIndex >= storageDirs.size()) return false;
            }
            return true;
        }

        public StorageDirectory next() {
            StorageDirectory sd = getStorageDir(nextIndex);
            prevIndex = nextIndex;
            nextIndex++;
            if (dirType != null) {
                while (nextIndex < storageDirs.size()) {
                    if (getStorageDir(nextIndex).getStorageDirType().isOfType(dirType)) break;
                    nextIndex++;
                }
            }
            return sd;
        }

        public void remove() {
            nextIndex = prevIndex;
            storageDirs.remove(prevIndex);
            hasNext();
        }
    }

    /**
   * Return default iterator
   * This iterator returns all entires of storageDirs
   */
    public Iterator<StorageDirectory> dirIterator() {
        return dirIterator(null);
    }

    /**
   * Return iterator based on Storage Directory Type
   * This iterator selects entires of storageDirs of type dirType and returns
   * them via the Iterator
   */
    public Iterator<StorageDirectory> dirIterator(StorageDirType dirType) {
        return new DirIterator(dirType);
    }

    /**
   * One of the storage directories.
   */
    public class StorageDirectory {

        File root;

        FileLock lock;

        StorageDirType dirType;

        public StorageDirectory(File dir) {
            this(dir, null);
        }

        public StorageDirectory(File dir, StorageDirType dirType) {
            this.root = dir;
            this.lock = null;
            this.dirType = dirType;
        }

        /**
     * Get root directory of this storage
     */
        public File getRoot() {
            return root;
        }

        /**
     * Get storage directory type
     */
        public StorageDirType getStorageDirType() {
            return dirType;
        }

        /**
     * Read version file.
     * 
     * @throws IOException if file cannot be read or contains inconsistent data
     */
        public void read() throws IOException {
            read(getVersionFile());
        }

        public void read(File from) throws IOException {
            RandomAccessFile file = new RandomAccessFile(from, "rws");
            FileInputStream in = null;
            try {
                in = new FileInputStream(file.getFD());
                file.seek(0);
                Properties props = new Properties();
                props.load(in);
                getFields(props, this);
            } finally {
                if (in != null) {
                    in.close();
                }
                file.close();
            }
        }

        /**
     * Write version file.
     * 
     * @throws IOException
     */
        public void write() throws IOException {
            corruptPreUpgradeStorage(root);
            write(getVersionFile());
        }

        public void write(File to) throws IOException {
            Properties props = new Properties();
            setFields(props, this);
            RandomAccessFile file = new RandomAccessFile(to, "rws");
            FileOutputStream out = null;
            try {
                file.seek(0);
                out = new FileOutputStream(file.getFD());
                props.store(out, null);
                file.setLength(out.getChannel().position());
            } finally {
                if (out != null) {
                    out.close();
                }
                file.close();
            }
        }

        /**
     * Clear and re-create storage directory.
     * <p>
     * Removes contents of the current directory and creates an empty directory.
     * 
     * This does not fully format storage directory. 
     * It cannot write the version file since it should be written last after  
     * all other storage type dependent files are written.
     * Derived storage is responsible for setting specific storage values and
     * writing the version file to disk.
     * 
     * @throws IOException
     */
        public void clearDirectory() throws IOException {
            File curDir = this.getCurrentDir();
            if (curDir.exists()) if (!(FileUtil.fullyDelete(curDir))) throw new IOException("Cannot remove current directory: " + curDir);
            if (!curDir.mkdirs()) throw new IOException("Cannot create directory " + curDir);
        }

        public File getCurrentDir() {
            return new File(root, STORAGE_DIR_CURRENT);
        }

        public File getVersionFile() {
            return new File(new File(root, STORAGE_DIR_CURRENT), STORAGE_FILE_VERSION);
        }

        public File getPreviousVersionFile() {
            return new File(new File(root, STORAGE_DIR_PREVIOUS), STORAGE_FILE_VERSION);
        }

        public File getPreviousDir() {
            return new File(root, STORAGE_DIR_PREVIOUS);
        }

        public File getPreviousTmp() {
            return new File(root, STORAGE_TMP_PREVIOUS);
        }

        public File getRemovedTmp() {
            return new File(root, STORAGE_TMP_REMOVED);
        }

        public File getFinalizedTmp() {
            return new File(root, STORAGE_TMP_FINALIZED);
        }

        public File getLastCheckpointTmp() {
            return new File(root, STORAGE_TMP_LAST_CKPT);
        }

        public File getPreviousCheckpoint() {
            return new File(root, STORAGE_PREVIOUS_CKPT);
        }

        /**
     * Check consistency of the storage directory
     * 
     * @param startOpt a startup option.
     *  
     * @return state {@link StorageState} of the storage directory 
     * @throws {@link InconsistentFSStateException} if directory state is not 
     * consistent and cannot be recovered 
     */
        public StorageState analyzeStorage(StartupOption startOpt) throws IOException {
            assert root != null : "root is null";
            String rootPath = root.getCanonicalPath();
            try {
                if (!root.exists()) {
                    if (startOpt != StartupOption.FORMAT) {
                        LOG.info("Storage directory " + rootPath + " does not exist.");
                        return StorageState.NON_EXISTENT;
                    }
                    LOG.info(rootPath + " does not exist. Creating ...");
                    if (!root.mkdirs()) throw new IOException("Cannot create directory " + rootPath);
                }
                if (!root.isDirectory()) {
                    LOG.info(rootPath + "is not a directory.");
                    return StorageState.NON_EXISTENT;
                }
                if (!root.canWrite()) {
                    LOG.info("Cannot access storage directory " + rootPath);
                    return StorageState.NON_EXISTENT;
                }
            } catch (SecurityException ex) {
                LOG.info("Cannot access storage directory " + rootPath, ex);
                return StorageState.NON_EXISTENT;
            }
            this.lock();
            if (startOpt == HdfsConstants.StartupOption.FORMAT) return StorageState.NOT_FORMATTED;
            if (startOpt != HdfsConstants.StartupOption.IMPORT) {
                checkConversionNeeded(this);
            }
            File versionFile = getVersionFile();
            boolean hasCurrent = versionFile.exists();
            boolean hasPrevious = getPreviousDir().exists();
            boolean hasPreviousTmp = getPreviousTmp().exists();
            boolean hasRemovedTmp = getRemovedTmp().exists();
            boolean hasFinalizedTmp = getFinalizedTmp().exists();
            boolean hasCheckpointTmp = getLastCheckpointTmp().exists();
            if (!(hasPreviousTmp || hasRemovedTmp || hasFinalizedTmp || hasCheckpointTmp)) {
                if (hasCurrent) return StorageState.NORMAL;
                if (hasPrevious) throw new InconsistentFSStateException(root, "version file in current directory is missing.");
                return StorageState.NOT_FORMATTED;
            }
            if ((hasPreviousTmp ? 1 : 0) + (hasRemovedTmp ? 1 : 0) + (hasFinalizedTmp ? 1 : 0) + (hasCheckpointTmp ? 1 : 0) > 1) throw new InconsistentFSStateException(root, "too many temporary directories.");
            if (hasCheckpointTmp) {
                return hasCurrent ? StorageState.COMPLETE_CHECKPOINT : StorageState.RECOVER_CHECKPOINT;
            }
            if (hasFinalizedTmp) {
                if (hasPrevious) throw new InconsistentFSStateException(root, STORAGE_DIR_PREVIOUS + " and " + STORAGE_TMP_FINALIZED + "cannot exist together.");
                return StorageState.COMPLETE_FINALIZE;
            }
            if (hasPreviousTmp) {
                if (hasPrevious) throw new InconsistentFSStateException(root, STORAGE_DIR_PREVIOUS + " and " + STORAGE_TMP_PREVIOUS + " cannot exist together.");
                if (hasCurrent) return StorageState.COMPLETE_UPGRADE;
                return StorageState.RECOVER_UPGRADE;
            }
            assert hasRemovedTmp : "hasRemovedTmp must be true";
            if (!(hasCurrent ^ hasPrevious)) throw new InconsistentFSStateException(root, "one and only one directory " + STORAGE_DIR_CURRENT + " or " + STORAGE_DIR_PREVIOUS + " must be present when " + STORAGE_TMP_REMOVED + " exists.");
            if (hasCurrent) return StorageState.COMPLETE_ROLLBACK;
            return StorageState.RECOVER_ROLLBACK;
        }

        /**
     * Complete or recover storage state from previously failed transition.
     * 
     * @param curState specifies what/how the state should be recovered
     * @throws IOException
     */
        public void doRecover(StorageState curState) throws IOException {
            File curDir = getCurrentDir();
            String rootPath = root.getCanonicalPath();
            switch(curState) {
                case COMPLETE_UPGRADE:
                    LOG.info("Completing previous upgrade for storage directory " + rootPath + ".");
                    rename(getPreviousTmp(), getPreviousDir());
                    return;
                case RECOVER_UPGRADE:
                    LOG.info("Recovering storage directory " + rootPath + " from previous upgrade.");
                    if (curDir.exists()) deleteDir(curDir);
                    rename(getPreviousTmp(), curDir);
                    return;
                case COMPLETE_ROLLBACK:
                    LOG.info("Completing previous rollback for storage directory " + rootPath + ".");
                    deleteDir(getRemovedTmp());
                    return;
                case RECOVER_ROLLBACK:
                    LOG.info("Recovering storage directory " + rootPath + " from previous rollback.");
                    rename(getRemovedTmp(), curDir);
                    return;
                case COMPLETE_FINALIZE:
                    LOG.info("Completing previous finalize for storage directory " + rootPath + ".");
                    deleteDir(getFinalizedTmp());
                    return;
                case COMPLETE_CHECKPOINT:
                    LOG.info("Completing previous checkpoint for storage directory " + rootPath + ".");
                    File prevCkptDir = getPreviousCheckpoint();
                    if (prevCkptDir.exists()) deleteDir(prevCkptDir);
                    rename(getLastCheckpointTmp(), prevCkptDir);
                    return;
                case RECOVER_CHECKPOINT:
                    LOG.info("Recovering storage directory " + rootPath + " from failed checkpoint.");
                    if (curDir.exists()) deleteDir(curDir);
                    rename(getLastCheckpointTmp(), curDir);
                    return;
                default:
                    throw new IOException("Unexpected FS state: " + curState);
            }
        }

        /**
     * Lock storage to provide exclusive access.
     * 
     * <p> Locking is not supported by all file systems.
     * E.g., NFS does not consistently support exclusive locks.
     * 
     * <p> If locking is supported we guarantee exculsive access to the
     * storage directory. Otherwise, no guarantee is given.
     * 
     * @throws IOException if locking fails
     */
        public void lock() throws IOException {
            this.lock = tryLock();
            if (lock == null) {
                String msg = "Cannot lock storage " + this.root + ". The directory is already locked.";
                LOG.info(msg);
                throw new IOException(msg);
            }
        }

        /**
     * Attempts to acquire an exclusive lock on the storage.
     * 
     * @return A lock object representing the newly-acquired lock or
     * <code>null</code> if storage is already locked.
     * @throws IOException if locking fails.
     */
        FileLock tryLock() throws IOException {
            File lockF = new File(root, STORAGE_FILE_LOCK);
            lockF.deleteOnExit();
            RandomAccessFile file = new RandomAccessFile(lockF, "rws");
            FileLock res = null;
            try {
                res = file.getChannel().tryLock();
            } catch (OverlappingFileLockException oe) {
                file.close();
                return null;
            } catch (IOException e) {
                LOG.info(StringUtils.stringifyException(e));
                file.close();
                throw e;
            }
            return res;
        }

        /**
     * Unlock storage.
     * 
     * @throws IOException
     */
        public void unlock() throws IOException {
            if (this.lock == null) return;
            this.lock.release();
            lock.channel().close();
            lock = null;
        }
    }

    /**
   * Create empty storage info of the specified type
   */
    protected Storage(NodeType type) {
        super();
        this.storageType = type;
    }

    protected Storage(NodeType type, int nsID, long cT) {
        super(FSConstants.LAYOUT_VERSION, nsID, cT);
        this.storageType = type;
    }

    protected Storage(NodeType type, StorageInfo storageInfo) {
        super(storageInfo);
        this.storageType = type;
    }

    public int getNumStorageDirs() {
        return storageDirs.size();
    }

    public StorageDirectory getStorageDir(int idx) {
        return storageDirs.get(idx);
    }

    protected void addStorageDir(StorageDirectory sd) {
        storageDirs.add(sd);
    }

    public abstract boolean isConversionNeeded(StorageDirectory sd) throws IOException;

    private void checkConversionNeeded(StorageDirectory sd) throws IOException {
        if (isConversionNeeded(sd)) {
            checkVersionUpgradable(0);
        }
    }

    /**
   * Checks if the upgrade from the given old version is supported. If
   * no upgrade is supported, it throws IncorrectVersionException.
   * 
   * @param oldVersion
   */
    protected static void checkVersionUpgradable(int oldVersion) throws IOException {
        if (oldVersion > LAST_UPGRADABLE_LAYOUT_VERSION) {
            String msg = "*********** Upgrade is not supported from this older" + " version of storage to the current version." + " Please upgrade to " + LAST_UPGRADABLE_HADOOP_VERSION + " or a later version and then upgrade to current" + " version. Old layout version is " + (oldVersion == 0 ? "'too old'" : ("" + oldVersion)) + " and latest layout version this software version can" + " upgrade from is " + LAST_UPGRADABLE_LAYOUT_VERSION + ". ************";
            LOG.error(msg);
            throw new IOException(msg);
        }
    }

    /**
   * Get common storage fields.
   * Should be overloaded if additional fields need to be get.
   * 
   * @param props
   * @throws IOException
   */
    protected void getFields(Properties props, StorageDirectory sd) throws IOException {
        String sv, st, sid, sct;
        sv = props.getProperty("layoutVersion");
        st = props.getProperty("storageType");
        sid = props.getProperty("namespaceID");
        sct = props.getProperty("cTime");
        if (sv == null || st == null || sid == null || sct == null) throw new InconsistentFSStateException(sd.root, "file " + STORAGE_FILE_VERSION + " is invalid.");
        int rv = Integer.parseInt(sv);
        NodeType rt = NodeType.valueOf(st);
        int rid = Integer.parseInt(sid);
        long rct = Long.parseLong(sct);
        if (!storageType.equals(rt) || !((namespaceID == 0) || (rid == 0) || namespaceID == rid)) throw new InconsistentFSStateException(sd.root, "is incompatible with others.");
        if (rv < FSConstants.LAYOUT_VERSION) throw new IncorrectVersionException(rv, "storage directory " + sd.root.getCanonicalPath());
        layoutVersion = rv;
        storageType = rt;
        namespaceID = rid;
        cTime = rct;
    }

    /**
   * Set common storage fields.
   * Should be overloaded if additional fields need to be set.
   * 
   * @param props
   * @throws IOException
   */
    protected void setFields(Properties props, StorageDirectory sd) throws IOException {
        props.setProperty("layoutVersion", String.valueOf(layoutVersion));
        props.setProperty("storageType", storageType.toString());
        props.setProperty("namespaceID", String.valueOf(namespaceID));
        props.setProperty("cTime", String.valueOf(cTime));
    }

    public static void rename(File from, File to) throws IOException {
        if (!from.renameTo(to)) throw new IOException("Failed to rename " + from.getCanonicalPath() + " to " + to.getCanonicalPath());
    }

    protected static void deleteDir(File dir) throws IOException {
        if (!FileUtil.fullyDelete(dir)) throw new IOException("Failed to delete " + dir.getCanonicalPath());
    }

    /**
   * Write all data storage files.
   * @throws IOException
   */
    public void writeAll() throws IOException {
        this.layoutVersion = FSConstants.LAYOUT_VERSION;
        for (Iterator<StorageDirectory> it = storageDirs.iterator(); it.hasNext(); ) {
            it.next().write();
        }
    }

    /**
   * Unlock all storage directories.
   * @throws IOException
   */
    public void unlockAll() throws IOException {
        for (Iterator<StorageDirectory> it = storageDirs.iterator(); it.hasNext(); ) {
            it.next().unlock();
        }
    }

    /**
   * Check whether underlying file system supports file locking.
   * 
   * @return <code>true</code> if exclusive locks are supported or
   *         <code>false</code> otherwise.
   * @throws IOException
   * @see StorageDirectory#lock()
   */
    public boolean isLockSupported(int idx) throws IOException {
        StorageDirectory sd = storageDirs.get(idx);
        FileLock firstLock = null;
        FileLock secondLock = null;
        try {
            firstLock = sd.lock;
            if (firstLock == null) {
                firstLock = sd.tryLock();
                if (firstLock == null) return true;
            }
            secondLock = sd.tryLock();
            if (secondLock == null) return true;
        } finally {
            if (firstLock != null && firstLock != sd.lock) {
                firstLock.release();
                firstLock.channel().close();
            }
            if (secondLock != null) {
                secondLock.release();
                secondLock.channel().close();
            }
        }
        return false;
    }

    public static String getBuildVersion() {
        return VersionInfo.getRevision();
    }

    public static String getRegistrationID(StorageInfo storage) {
        return "NS-" + Integer.toString(storage.getNamespaceID()) + "-" + Integer.toString(storage.getLayoutVersion()) + "-" + Long.toString(storage.getCTime());
    }

    protected abstract void corruptPreUpgradeStorage(File rootDir) throws IOException;

    protected void writeCorruptedData(RandomAccessFile file) throws IOException {
        final String messageForPreUpgradeVersion = "\nThis file is INTENTIONALLY CORRUPTED so that versions\n" + "of Hadoop prior to 0.13 (which are incompatible\n" + "with this directory layout) will fail to start.\n";
        file.seek(0);
        file.writeInt(FSConstants.LAYOUT_VERSION);
        org.apache.hadoop.io.UTF8.writeString(file, "");
        file.writeBytes(messageForPreUpgradeVersion);
        file.getFD().sync();
    }
}
