package net.sf.leechget.log4j.appenders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.leechget.log4j.exceptions.AppenderInitializationError;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;

/**
 * This class is appender that zips old file instead of appending it.<br>
 * File is recognized as old if it's lastModified() is < JVM startup time.<br>
 * So we can have per-run appending.
 * <p/>
 * <br>
 * 
 * Unfortunaltely, UNIX systems doesn't support file creation date, so we have
 * to use lastModified(), windows only solution is not good.
 * 
 * @author Rogiel Josias Sulzbach
 */
public class TruncateToZipFileAppender extends FileAppender {

    /**
	 * String that points to root directory for backups
	 */
    private String backupDir = "log/backup";

    /**
	 * String that represents date format for backup files
	 */
    private String backupDateFormat = "yyyy-MM-dd HH-mm-ss";

    /**
	 * <p>
	 * Sets and <i>opens</i> the file where the log output will go. The
	 * specified file must be writable.
	 * <p/>
	 * <p>
	 * If there was already an opened file, then the previous file is closed
	 * first.
	 * <p/>
	 * <p>
	 * <b>Do not use this method directly. To configure a FileAppender or one of
	 * its subclasses, set its properties one by one and then call
	 * activateOptions.</b>
	 * <p/>
	 * <br>
	 * Truncation is done by {@link #truncate(java.io.File)}
	 * 
	 * @param fileName
	 *            The path to the log file.
	 * @param append
	 *            If true will append to fileName. Otherwise will truncate
	 *            fileName.
	 */
    @Override
    public void setFile(final String fileName, final boolean append, final boolean bufferedIO, final int bufferSize) throws IOException {
        if (!append) {
            this.truncate(new File(fileName));
        }
        super.setFile(fileName, append, bufferedIO, bufferSize);
    }

    /**
	 * This method creates archive with file instead of deleting it.
	 * 
	 * @param file
	 *            file to truncate
	 */
    protected void truncate(final File file) {
        LogLog.debug("Compression of file: " + file.getAbsolutePath() + " started.");
        if (FileUtils.isFileOlder(file, ManagementFactory.getRuntimeMXBean().getStartTime())) {
            final File backupRoot = new File(this.getBackupDir());
            if (!backupRoot.exists() && !backupRoot.mkdirs()) {
                throw new AppenderInitializationError("Can't create backup dir for backup storage");
            }
            SimpleDateFormat df;
            try {
                df = new SimpleDateFormat(this.getBackupDateFormat());
            } catch (final Exception e) {
                throw new AppenderInitializationError("Invalid date formate for backup files: " + this.getBackupDateFormat(), e);
            }
            final String date = df.format(new Date(file.lastModified()));
            final File zipFile = new File(backupRoot, file.getName() + "." + date + ".zip");
            ZipOutputStream zos = null;
            FileInputStream fis = null;
            try {
                zos = new ZipOutputStream(new FileOutputStream(zipFile));
                final ZipEntry entry = new ZipEntry(file.getName());
                entry.setMethod(ZipEntry.DEFLATED);
                entry.setCrc(FileUtils.checksumCRC32(file));
                zos.putNextEntry(entry);
                fis = FileUtils.openInputStream(file);
                final byte[] buffer = new byte[1024];
                int readed;
                while ((readed = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, readed);
                }
            } catch (final Exception e) {
                throw new AppenderInitializationError("Can't create zip file", e);
            } finally {
                if (zos != null) {
                    try {
                        zos.close();
                    } catch (final IOException e) {
                        LogLog.warn("Can't close zip file", e);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (final IOException e) {
                        LogLog.warn("Can't close zipped file", e);
                    }
                }
            }
            if (!file.delete()) {
                throw new AppenderInitializationError("Can't delete old log file " + file.getAbsolutePath());
            }
        }
    }

    /**
	 * Returns root directory for backups
	 * 
	 * @return root directory for backups
	 */
    public String getBackupDir() {
        return this.backupDir;
    }

    /**
	 * Sets root directory for backups
	 * 
	 * @param backupDir
	 *            new root directory for backups
	 */
    public void setBackupDir(final String backupDir) {
        this.backupDir = backupDir;
    }

    /**
	 * Returns date format that should be used for backup files represented as
	 * string
	 * 
	 * @return date formate for backup files
	 */
    public String getBackupDateFormat() {
        return this.backupDateFormat;
    }

    /**
	 * Sets date format for bakcup files represented as string
	 * 
	 * @param backupDateFormat
	 *            date format for backup files
	 */
    public void setBackupDateFormat(final String backupDateFormat) {
        this.backupDateFormat = backupDateFormat;
    }
}
