package com.bonkey.filesystem.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import nu.xom.Element;
import com.bonkey.config.BonkeyConstants;
import com.bonkey.config.ConfigManager;
import com.bonkey.filesystem.browsable.BrowsableFile;
import com.bonkey.filesystem.browsable.BrowsableFileSystem;
import com.bonkey.filesystem.browsable.BrowsableItem;
import com.bonkey.filesystem.local.LocalFile;
import com.bonkey.filesystem.local.LocalTempFile;
import com.bonkey.filesystem.writable.WritableFileSystem;
import com.bonkey.report.BackupFileReport;
import com.bonkey.report.BackupReport;
import com.bonkey.restore.RestoreManager;
import com.bonkey.schedule.BackupManager;

/**
 *
 * Represents a file that can be backed up. One important custom is that the file should look
 * after all source-side filename adjustments (eg removing of encryption extension)
 * 
 * @author marcel
 */
public class BackupFile extends BackupItem implements BrowsableFile {

    private static final long serialVersionUID = -2246278526337692243L;

    /**
	 * Construct the file from an XML representation
	 * 
	 * @param e XML representation of this file
	 */
    public BackupFile(Element e) {
        super(e);
    }

    /**
	 * Construct a new backup file from parameters
	 * @param item the underlying item to be backed up
	 * @param fileSystem the filesystem this file (not the underlying item) belongs to
	 * @param uri the URI of this backup file
	 */
    public BackupFile(BrowsableItem item, String fileSystem, String uri) {
        super(item, fileSystem, uri, true);
    }

    public String getImageName() {
        if (isEnabled()) {
            BrowsableItem item = getItem();
            if (item != null) {
                return item.getImageName();
            }
        }
        return BonkeyConstants.ICON_FILE_EXCL;
    }

    public BackupReport doBackup(BackupManager monitor) {
        BackupReport report = new BackupFileReport(getName(), getItem().getURI());
        monitor.reportFile(getRelativeURI());
        if (!isEnabled()) {
            report.reportNotRun(Messages.getString("BackupFile.Disabled"));
            return report;
        }
        BackupGroup group = (BackupGroup) getFileSystem();
        WritableFileSystem target = monitor.getCurrentTarget();
        String compression = group.getCompress();
        monitor.subTask(Messages.getString("BackupFile.Copying") + getName() + Messages.getString("BackupFile.To") + target.getName());
        String relativePath = group.getFolderName() + '/' + getRelativeURI();
        if (compression.equals(BackupGroup.COMPRESS_ZIP)) {
            relativePath += BackupGroup.COMPRESS_ZIP;
        }
        String backupURI = new String(relativePath);
        try {
            BrowsableItem backupTo = target.getFile(backupURI);
            BrowsableItem fileSource = getItem();
            if (isModified(backupTo, fileSource)) {
                if (compression.equals(BackupGroup.COMPRESS_NONE)) {
                    backupFile(target, getItem(), relativePath, null, report);
                } else if (compression.equals(BackupGroup.COMPRESS_ZIP)) {
                    backupFileZip(target, getItem(), relativePath, report);
                }
            } else {
                if (backupTo != null && !(backupTo instanceof BrowsableFile)) {
                    report.reportNonCriticalFailure(Messages.getString("BackupFile.ErrorFolderExists"));
                } else {
                    report.reportNotRun();
                }
            }
        } catch (FileNotFoundException e) {
            ConfigManager.getConfigManager().logError(Messages.getString("BackupFile.File") + getName() + Messages.getString("BackupFile.NotFound") + e.getMessage());
            report.reportNonCriticalFailure(e.getMessage());
        } catch (IOException e) {
            ConfigManager.getConfigManager().logError(Messages.getString("BackupFile.ErrorBackingUp") + getName() + " to " + target.getName() + ": " + e.getMessage());
            report.reportCriticalFailure(e);
        }
        monitor.worked(work());
        return report;
    }

    public int work() {
        return (isEnabled() ? 1 : 0);
    }

    public long getSize() throws IOException {
        BrowsableItem item = getItem();
        if (item != null) {
            return item.getSize();
        }
        return 1;
    }

    public InputStream getInputStream() throws IOException {
        BrowsableItem item = getItem();
        if (item != null) {
            return ((BrowsableFile) item).getInputStream();
        }
        return null;
    }

    public Date getLastModified() throws IOException {
        BrowsableItem item = getItem();
        if (item != null) {
            return ((BrowsableFile) item).getLastModified();
        }
        return null;
    }

    public BrowsableItem[] getChildren() throws IOException {
        return new BrowsableItem[0];
    }

    public boolean hasChildren() throws IOException {
        return false;
    }

    /**
	 * Backup a file to a filesystem
	 * @param target the filesystem to backup to
	 * @param fileSource the source item
	 * @param relativeURI the location to backup the item to
	 * @param fileSourceCompressed the source file compressed if compression is being used; otherwise, null
	 * @param report report on the operation - should be filled by this method with success or skip, failures are thrown up to calling method as exceptions
	 * @throws IOException when the operation fails (eg disk error)
	 */
    protected void backupFile(WritableFileSystem target, BrowsableItem fileSource, String relativeURI, BrowsableFile fileSourceCompressed, BackupReport report) throws IOException {
        if (fileSourceCompressed != null) {
            target.putFile(fileSourceCompressed, relativeURI, null, 0);
            report.reportSuccess(target.getName(), fileSourceCompressed.getSize());
        } else {
            target.putFile(fileSource, relativeURI, null, 0);
            report.reportSuccess(target.getName(), fileSource.getSize());
        }
    }

    /**
	 * Backup a file, compressing on the way
	 * @param target the filesystem to backup to
	 * @param fileSource the source item
	 * @param relativeURI the location to backup the item to
	 * @param report report on the operation - should be filled by this method with success or skip, failures are thrown up to calling method as exceptions
	 * @throws IOException where an IO operation fails
	 */
    protected void backupFileZip(WritableFileSystem target, BrowsableItem item, String relativePath, BackupReport report) throws IOException {
        File zipFile = null;
        try {
            zipFile = getZipFile();
            LocalFile fileSourceCompressed = new LocalTempFile(zipFile, getItem().getFileSystemName(), null);
            backupFile(target, getItem(), relativePath, fileSourceCompressed, report);
            zipFile.delete();
        } catch (IOException e) {
            if (zipFile != null) {
                zipFile.delete();
            }
            throw e;
        }
    }

    /**
	 * Compress the file
	 * 
	 * @throws IOException where an IO operation fails
	 */
    protected File getZipFile() throws IOException {
        File tempFile = File.createTempFile("eb_" + getName(), BackupGroup.COMPRESS_ZIP);
        tempFile.deleteOnExit();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
        InputStream in = null;
        in = ((BrowsableFile) getItem()).getInputStream();
        String zipEntryName = getName();
        out.putNextEntry(new ZipEntry(zipEntryName));
        int length = 0;
        byte[] buffer = new byte[1024];
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.closeEntry();
        in.close();
        out.close();
        return tempFile;
    }

    /**
	 * Check if the file has been modified
	 * @param backupTo the file to check modification date against
	 * @param fileSource the file being backed up from
	 * @return true if has been modified; false if not
	 * @throws IOException
	 */
    protected boolean isModified(BrowsableItem backupTo, BrowsableItem fileSource) throws IOException {
        return (backupTo == null || (backupTo instanceof BrowsableFile && ((BrowsableFile) fileSource).getLastModified().after(((BrowsableFile) backupTo).getLastModified())));
    }

    protected String getRelativeRestoreURI(RestoreManager manager, BrowsableFileSystem restoreFS, BrowsableItem restoreRoot, String compress) {
        String resultingURI = super.getRelativeRestoreURI(manager, restoreFS, restoreRoot, compress);
        if (!compress.equals(BackupGroup.COMPRESS_NONE)) {
            resultingURI += compress;
        }
        return resultingURI;
    }
}
