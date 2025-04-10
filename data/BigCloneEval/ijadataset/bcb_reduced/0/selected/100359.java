package org.exist.storage.journal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.config.annotation.ConfigurationClass;
import org.exist.config.annotation.ConfigurationFieldAsAttribute;
import org.exist.storage.BrokerPool;
import org.exist.storage.lock.FileLock;
import org.exist.storage.txn.Checkpoint;
import org.exist.storage.txn.TransactionException;
import org.exist.util.FileUtils;
import org.exist.util.ReadOnlyException;
import org.exist.util.sanity.SanityCheck;

/**
 * Manages the journalling log. The database uses one central journal for
 * all data files. If the journal exceeds the predefined maximum size, a new file is created.
 * Every journal file has a unique number, which keeps growing during the lifetime of the db.
 * The name of the file corresponds to the file number. The file with the highest
 * number will be used for recovery.
 * 
 * A buffer is used to temporarily buffer journal entries. To guarantee consistency, the buffer will be flushed
 * and the journal is synched after every commit or whenever a db page is written to disk.
 * 
 * Each entry has the structure:
 * 
 * <pre>[byte: entryType, long: transactionId, short length, byte[] data, short backLink]</pre>
 * 
 * <ul>
 *  <li>entryType is a unique id that identifies the log record. Entry types are registered via the 
 * {@link org.exist.storage.journal.LogEntryTypes} class.</li>
 *  <li>transactionId: the id of the transaction that created the record.</li>
 *  <li>length: the length of the log entry data.</li>
 *  <li>data: the payload data provided by the {@link org.exist.storage.journal.Loggable} object.</li>
 *  <li>backLink: offset to the start of the record. Used when scanning the log file backwards.</li>
 * </ul>
 * 
 * @author wolf
 */
@ConfigurationClass("journal")
public class Journal {

    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Journal.class);

    public static final String RECOVERY_SYNC_ON_COMMIT_ATTRIBUTE = "sync-on-commit";

    public static final String RECOVERY_JOURNAL_DIR_ATTRIBUTE = "journal-dir";

    public static final String RECOVERY_SIZE_LIMIT_ATTRIBUTE = "size";

    public static final String PROPERTY_RECOVERY_SIZE_LIMIT = "db-connection.recovery.size-limit";

    public static final String PROPERTY_RECOVERY_JOURNAL_DIR = "db-connection.recovery.journal-dir";

    public static final String PROPERTY_RECOVERY_SYNC_ON_COMMIT = "db-connection.recovery.sync-on-commit";

    public static final String LOG_FILE_SUFFIX = "log";

    public static final String BAK_FILE_SUFFIX = ".bak";

    public static final String LCK_FILE = "journal.lck";

    /** the length of the header of each entry: entryType + transactionId + length */
    public static final int LOG_ENTRY_HEADER_LEN = 11;

    /** header length + trailing back link */
    public static final int LOG_ENTRY_BASE_LEN = LOG_ENTRY_HEADER_LEN + 2;

    /** default maximum journal size */
    public static final int DEFAULT_MAX_SIZE = 10 * 1024 * 1024;

    /** minimal size the journal needs to have to be replaced by a new file during a checkpoint */
    private static final long MIN_REPLACE = 1024 * 1024;

    /** 
     * size limit for the journal file. A checkpoint will be triggered if the file
     * exceeds this size limit.
     */
    @ConfigurationFieldAsAttribute("size")
    private int journalSizeLimit = DEFAULT_MAX_SIZE;

    /** the current output channel */
    private FileChannel channel;

    /** Synching the journal is done by a background thread */
    private FileSyncThread syncThread;

    /** latch used to synchronize writes to the channel */
    private Object latch = new Object();

    /** the data directory where journal files are written to */
    @ConfigurationFieldAsAttribute("journal-dir")
    private File dir;

    private FileLock fileLock;

    /** the current file number */
    private int currentFile = 0;

    /** used to keep track of the current position in the file */
    private int inFilePos = 0;

    /** temp buffer */
    private ByteBuffer currentBuffer;

    /** the last LSN written by the JournalManager */
    private long currentLsn = Lsn.LSN_INVALID;

    /** the last LSN actually written to the file */
    private long lastLsnWritten = Lsn.LSN_INVALID;

    /** stores the current LSN of the last file sync on the file */
    private long lastSyncLsn = Lsn.LSN_INVALID;

    /** set to true while recovery is in progress */
    private boolean inRecovery = false;

    /** the {@link BrokerPool} that created this manager */
    private BrokerPool pool;

    /** if set to true, a sync will be triggered on the log file after every commit */
    @ConfigurationFieldAsAttribute("sync-on-commit")
    private boolean syncOnCommit = true;

    private File fsJournalDir;

    public Journal(BrokerPool pool, File directory) throws EXistException {
        this.dir = directory;
        this.pool = pool;
        this.fsJournalDir = new File(directory, "fs.journal");
        currentBuffer = ByteBuffer.allocateDirect(1024 * 1024);
        syncThread = new FileSyncThread(latch);
        syncThread.start();
        Boolean syncOpt = (Boolean) pool.getConfiguration().getProperty(PROPERTY_RECOVERY_SYNC_ON_COMMIT);
        if (syncOpt != null) {
            syncOnCommit = syncOpt.booleanValue();
            if (LOG.isDebugEnabled()) LOG.debug("SyncOnCommit = " + syncOnCommit);
        }
        String logDir = (String) pool.getConfiguration().getProperty(PROPERTY_RECOVERY_JOURNAL_DIR);
        if (logDir != null) {
            File f = new File(logDir);
            if (!f.isAbsolute()) {
                if (pool.getConfiguration().getExistHome() != null) {
                    f = new File(pool.getConfiguration().getExistHome(), logDir);
                } else if (pool.getConfiguration().getConfigFilePath() != null) {
                    File confFile = new File(pool.getConfiguration().getConfigFilePath());
                    f = new File(confFile.getParent(), logDir);
                }
            }
            if (!f.exists()) {
                if (LOG.isDebugEnabled()) LOG.debug("Output directory for journal files does not exist. Creating " + f.getAbsolutePath());
                try {
                    f.mkdirs();
                } catch (SecurityException e) {
                    throw new EXistException("Failed to create output directory: " + f.getAbsolutePath());
                }
            }
            if (!(f.canWrite())) {
                throw new EXistException("Cannot write to journal output directory: " + f.getAbsolutePath());
            }
            this.dir = f;
        }
        if (LOG.isDebugEnabled()) LOG.debug("Using directory for the journal: " + dir.getAbsolutePath());
        Integer sizeOpt = (Integer) pool.getConfiguration().getProperty(PROPERTY_RECOVERY_SIZE_LIMIT);
        if (sizeOpt != null) journalSizeLimit = sizeOpt.intValue() * 1024 * 1024;
    }

    public void initialize() throws EXistException, ReadOnlyException {
        File lck = new File(dir, LCK_FILE);
        fileLock = new FileLock(pool, lck.getAbsolutePath());
        boolean locked = fileLock.tryLock();
        if (!locked) {
            String lastHeartbeat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(fileLock.getLastHeartbeat());
            throw new EXistException("The journal log directory seems to be locked by another " + "eXist process. A lock file: " + lck.getAbsolutePath() + " is present in the " + "log directory. Last access to the lock file: " + lastHeartbeat);
        }
    }

    /**
     * Write a log entry to the journalling log.
     * 
     * @param loggable
     * @throws TransactionException
     */
    public synchronized void writeToLog(Loggable loggable) throws TransactionException {
        if (currentBuffer == null) throw new TransactionException("Database is shut down.");
        SanityCheck.ASSERT(!inRecovery, "Write to log during recovery. Should not happen!");
        final int size = loggable.getLogSize();
        final int required = size + LOG_ENTRY_BASE_LEN;
        if (required > currentBuffer.remaining()) flushToLog(false);
        currentLsn = Lsn.create(currentFile, inFilePos + currentBuffer.position() + 1);
        loggable.setLsn(currentLsn);
        try {
            currentBuffer.put(loggable.getLogType());
            currentBuffer.putLong(loggable.getTransactionId());
            currentBuffer.putShort((short) loggable.getLogSize());
            loggable.write(currentBuffer);
            currentBuffer.putShort((short) (size + LOG_ENTRY_HEADER_LEN));
        } catch (BufferOverflowException e) {
            throw new TransactionException("Buffer overflow while writing log record: " + loggable.dump(), e);
        }
    }

    /**
     * Returns the last LSN physically written to the journal.
     * 
     * @return last written LSN
     */
    public long lastWrittenLsn() {
        return lastLsnWritten;
    }

    /**
     * Flush the current buffer to disk. If fsync is true, a sync will
     * be called on the file to force all changes to disk.
     * 
     * @param fsync forces all changes to disk if true and syncMode is set to SYNC_ON_COMMIT.
     */
    public void flushToLog(boolean fsync) {
        flushToLog(fsync, false);
    }

    /**
     * Flush the current buffer to disk. If fsync is true, a sync will
     * be called on the file to force all changes to disk.
     * 
     * @param fsync forces all changes to disk if true and syncMode is set to SYNC_ON_COMMIT.
     * @param forceSync force changes to disk even if syncMode doesn't require it.
     */
    public synchronized void flushToLog(boolean fsync, boolean forceSync) {
        if (inRecovery) return;
        flushBuffer();
        if (forceSync || (fsync && syncOnCommit && currentLsn > lastSyncLsn)) {
            syncThread.triggerSync();
            lastSyncLsn = currentLsn;
        }
        try {
            if (channel.size() >= journalSizeLimit) pool.triggerCheckpoint();
        } catch (IOException e) {
            LOG.warn("Failed to trigger checkpoint!", e);
        }
    }

    /**
     * 
     */
    private void flushBuffer() {
        if (currentBuffer == null) return;
        synchronized (latch) {
            try {
                if (currentBuffer.position() > 0) {
                    currentBuffer.flip();
                    int size = currentBuffer.remaining();
                    while (currentBuffer.hasRemaining()) {
                        channel.write(currentBuffer);
                    }
                    inFilePos += size;
                    lastLsnWritten = currentLsn;
                }
            } catch (IOException e) {
                LOG.warn("Flushing log file failed!", e);
            } finally {
                currentBuffer.clear();
            }
        }
    }

    /**
     * Write a checkpoint record to the journal and flush it. If switchLogFiles is true,
     * a new journal will be started, but only if the file is larger than
     * {@link #MIN_REPLACE}. The old log is removed.
     * 
     * @param txnId
     * @param switchLogFiles
     * @throws TransactionException
     */
    public void checkpoint(long txnId, boolean switchLogFiles) throws TransactionException {
        LOG.debug("Checkpoint reached");
        writeToLog(new Checkpoint(txnId));
        if (switchLogFiles) flushBuffer(); else flushToLog(true, true);
        try {
            if (switchLogFiles && channel.position() > MIN_REPLACE) {
                File oldFile = getFile(currentFile);
                RemoveThread rt = new RemoveThread(channel, oldFile);
                try {
                    switchFiles();
                } catch (LogException e) {
                    LOG.warn("Failed to create new journal: " + e.getMessage(), e);
                }
                rt.start();
            }
            clearBackupFiles();
        } catch (IOException e) {
            LOG.warn("IOException while writing checkpoint", e);
        }
    }

    /**
     * Set the file number of the last file used.
     * 
     * @param fileNum the log file number
     */
    public void setCurrentFileNum(int fileNum) {
        currentFile = fileNum;
    }

    public void clearBackupFiles() {
        fsJournalDir.listFiles(new FileFilter() {

            public boolean accept(File file) {
                LOG.info("Checkpoint deleting " + file);
                if (!FileUtils.delete(file)) {
                    LOG.fatal("Cannot delete file " + file + " from backup journal.");
                }
                return false;
            }
        });
    }

    /**
     * Create a new journal with a larger file number
     * than the previous file.
     * 
     * @throws LogException
     */
    public void switchFiles() throws LogException {
        ++currentFile;
        String fname = getFileName(currentFile);
        File file = new File(dir, fname);
        if (file.exists()) {
            if (LOG.isDebugEnabled()) LOG.debug("Journal file " + file.getAbsolutePath() + " already exists. Copying it.");
            boolean renamed = file.renameTo(new File(file.getAbsolutePath() + BAK_FILE_SUFFIX));
            if (renamed && LOG.isDebugEnabled()) LOG.debug("Old file renamed to " + file.getAbsolutePath());
            file = new File(dir, fname);
        }
        if (LOG.isDebugEnabled()) LOG.debug("Creating new journal: " + file.getAbsolutePath());
        synchronized (latch) {
            close();
            try {
                FileOutputStream os = new FileOutputStream(file, true);
                channel = os.getChannel();
                syncThread.setChannel(channel);
            } catch (FileNotFoundException e) {
                throw new LogException("Failed to open new journal: " + file.getAbsolutePath(), e);
            }
        }
        inFilePos = 0;
    }

    public void close() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                LOG.warn("Failed to close journal", e);
            }
        }
    }

    /**
     * Find the journal file with the highest file number.
     * 
     * @param files
     */
    public static final int findLastFile(File files[]) {
        int max = -1;
        for (int i = 0; i < files.length; i++) {
            int p = files[i].getName().indexOf('.');
            String baseName = files[i].getName().substring(0, p);
            int num = Integer.parseInt(baseName, 16);
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    /**
     * Returns all journal files found in the data directory.
     * 
     * @return all journal files
     */
    public File[] getFiles() {
        final String suffix = '.' + LOG_FILE_SUFFIX;
        File files[] = dir.listFiles(new FileFilter() {

            public boolean accept(File file) {
                if (file.isDirectory()) return false;
                final String name = file.getName();
                return name.endsWith(suffix) && !name.endsWith("_index" + suffix);
            }
        });
        return files;
    }

    /**
     * Returns the file corresponding to the specified
     * file number.
     * 
     * @param fileNum
     */
    public File getFile(int fileNum) {
        return new File(dir, getFileName(fileNum));
    }

    /**
     * Shut down the journal. This will write a checkpoint record
     * to the log, so recovery manager knows the file has been
     * closed in a clean way.
     * 
     * @param txnId
     */
    public void shutdown(long txnId, boolean checkpoint) {
        if (currentBuffer == null) return;
        if (!BrokerPool.FORCE_CORRUPTION) {
            if (checkpoint) {
                try {
                    writeToLog(new Checkpoint(txnId));
                } catch (TransactionException e) {
                    LOG.error("An error occurred while closing the journal file: " + e.getMessage(), e);
                }
            }
            flushBuffer();
        }
        fileLock.release();
        syncThread.shutdown();
        try {
            syncThread.join();
        } catch (InterruptedException e) {
        }
        currentBuffer = null;
    }

    /**
     * Called to signal that the db is currently in
     * recovery phase, so no output should be written.
     * 
     * @param value
     */
    public void setInRecovery(boolean value) {
        inRecovery = value;
    }

    /**
     * Translate a file number into a file name.
     * 
     * @param fileNum
     * @return The file name
     */
    private static String getFileName(int fileNum) {
        String hex = Integer.toHexString(fileNum);
        hex = "0000000000".substring(hex.length()) + hex;
        return hex + '.' + LOG_FILE_SUFFIX;
    }

    private static class RemoveThread extends Thread {

        FileChannel channel;

        File file;

        RemoveThread(FileChannel channel, File file) {
            super("RemoveJournalThread");
            this.channel = channel;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                channel.close();
            } catch (IOException e) {
                LOG.warn("Exception while closing journal file: " + e.getMessage(), e);
            }
            file.delete();
        }
    }
}
