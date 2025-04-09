package ast.backup;

import ast.DocumentController;
import ast.common.error.ASTError;
import ast.common.error.BackupError;
import ast.common.error.BackupFileNotFoundError;
import ast.common.error.BackupIOError;
import ast.common.error.BackupInitError;
import ast.common.error.BackupRestoreError;
import ast.common.error.ConfigHandlerError;
import ast.common.error.DBError;
import ast.common.error.DispatchError;
import ast.common.error.FileError;
import ast.common.error.NoFileError;
import ast.common.util.ConverterUtils;
import ast.common.util.FileUtils;
import ast.common.util.PathUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class controls the backup functionality of AuthorSupportTool.
 *
 * @author bvollmer
 */
public class BackupController extends TimerTask {

    /**
     * User defined directory for backup save.
     */
    private String backupDir;

    /**
     * Instance of our main class {@link ast.DocumentController}.
     */
    private DocumentController docController;

    /**
     * Date format for naming backup files with date stamp.
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");

    /**
     * Timer for auto-backup mechanism.
     */
    private Timer scheduledTask;

    /**
     * Default constructor.
     *
     * @param aDocController Reference to the {@link ast.DocumentController}
     * @param sBackupDir User defined backup directory
     * @throws BackupInitError if initialization of backupController failed
     */
    public BackupController(final DocumentController aDocController, final String sBackupDir) throws BackupInitError {
        this.docController = aDocController;
        try {
            if (sBackupDir == null || sBackupDir.trim().isEmpty()) {
                this.backupDir = this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getPathUtils().getPath(PathUtils.WORK));
                this.docController.getConfigHandler().setOOoProperty("txtBackup.BackupDir", this.backupDir);
            } else {
                this.backupDir = sBackupDir;
            }
            if (!this.backupDir.endsWith(File.separator)) {
                this.backupDir = this.backupDir.concat(File.separator);
            }
            try {
                final long timePeriod = Long.parseLong(this.docController.getConfigHandler().getOOoProperty("txtBackup.AutoBackupTimer")) * 60 * 1000;
                if (timePeriod > 0) {
                    this.scheduledTask = new Timer();
                    this.scheduledTask.schedule(this, timePeriod, timePeriod);
                } else if (timePeriod < 0) {
                    this.docController.getConfigHandler().setOOoProperty("txtBackup.AutoBackupTimer", "0");
                    throw new BackupInitError("Auto backup timer had an invalid value. Automatically reset to 0.");
                }
            } catch (final NumberFormatException e) {
                this.docController.getConfigHandler().setOOoProperty("txtBackup.AutoBackupTimer", "0");
                throw new BackupInitError("Auto backup timer had an invalid value. Automatically reset to 0.", e);
            }
        } catch (final ConfigHandlerError e) {
            e.warning(false);
        }
        this.docController.getLogger().info("BackupController initialized!");
    }

    /**
     * Method to save a backup to backup directory.
     *
     * @param bFullBackup boolean flag for full backup
     * @throws BackupIOError if could not write to the backup directory
     * @throws BackupError configuration error
     * @throws NoFileError if document is new and not stored (no path available)
     */
    public void saveBackup(final boolean bFullBackup) throws BackupIOError, NoFileError, BackupError {
        try {
            String sBackupName = new File(this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getFileURL())).getName();
            if (sBackupName != null && !sBackupName.isEmpty()) {
                sBackupName = sBackupName.substring(0, sBackupName.lastIndexOf("."));
                Backup newBackupDir = new Backup(this.backupDir, sBackupName, new Date(), bFullBackup);
                String sFullFileName = this.backupDir + newBackupDir.getBackupName() + "-" + this.dateFormat.format(newBackupDir.getCreated()) + ".odt";
                if (!newBackupDir.exists()) {
                    newBackupDir.mkdirs();
                }
                if (newBackupDir.isDirectory()) {
                    this.storeToFile(sFullFileName);
                }
                if (bFullBackup) {
                    File documentSelf = new File(sFullFileName);
                    this.docController.exit(true);
                    File backupSrcDBDir = new File(newBackupDir.getAbsolutePath() + File.separator + "sourceDB");
                    File srcSrcDB = new File(this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getPathUtils().getPath(PathUtils.USER)) + File.separator + this.docController.getConfigHandler().getOOoProperty("txtSource.SourceDB"));
                    File backupSrcDB = new File(newBackupDir.getAbsolutePath() + File.separator + "sourceDB" + File.separator + srcSrcDB.getName());
                    if (backupSrcDBDir.mkdirs()) {
                        FileUtils.copy(srcSrcDB, backupSrcDB);
                    }
                    File backupQuotDBDir = new File(newBackupDir.getAbsolutePath() + File.separator + "quotDB");
                    File srcQuotDB = new File(this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getPathUtils().getPath(PathUtils.USER)) + File.separator + this.docController.getConfigHandler().getOOoProperty("txtSource.QuotationStyleDB"));
                    File backupQuotDB = new File(newBackupDir.getAbsolutePath() + File.separator + "quotDB" + File.separator + srcQuotDB.getName());
                    if (backupQuotDBDir.mkdirs()) {
                        FileUtils.copy(srcQuotDB, backupQuotDB);
                    }
                    List<File> filenames = new ArrayList<File>();
                    filenames.add(documentSelf);
                    filenames.add(backupSrcDBDir);
                    filenames.add(backupQuotDBDir);
                    String sSourceBackupDir = this.docController.getConfigHandler().getOOoProperty("txtSource.BackupDir");
                    if (!sSourceBackupDir.isEmpty()) {
                        File backupSrcDir = new File(newBackupDir.getAbsolutePath() + File.separator + "sources");
                        File srcSrc = new File(sSourceBackupDir);
                        if (backupSrcDir.mkdirs() && srcSrc.isDirectory() && FileUtils.getFiles(srcSrc, null) != null) {
                            for (final File curFile : FileUtils.getFiles(srcSrc, null)) {
                                FileUtils.copy(curFile.getAbsolutePath(), backupSrcDir + File.separator + curFile.getName());
                            }
                            filenames.add(backupSrcDir);
                        } else {
                            this.docController.getLogger().warning("Fullbackup: Skipped invalid source directory");
                        }
                    }
                    String sZipFileName = this.backupDir + sBackupName + "-" + this.dateFormat.format(newBackupDir.getCreated()) + ".zip";
                    FileUtils.zip((File[]) filenames.toArray(new File[] {}), sZipFileName, newBackupDir.toURI(), null);
                    this.docController.getSourceController();
                    for (File delFile : filenames) FileUtils.deleteDir(delFile, null);
                }
                this.docController.getLogger().fine("Backup was stored to " + sFullFileName);
            } else {
                throw new NoFileError("Backup could not be saved. A new document must be saved first time before backup.");
            }
        } catch (final ConfigHandlerError e) {
            throw new BackupError(e.getLogMessage(), e);
        } catch (final DBError e) {
            throw new BackupError(e.getLogMessage(), e);
        } catch (final FileError e) {
            throw new BackupIOError("Write error during backup process.", e);
        }
    }

    /**
     * Shows file choosing dialog for restoring a backup.
     *
     * @throws ASTError All backup related error and if file dialog couldn't be loaded
     * @throws NoFileError If document is new and not stored (no path available)
     */
    public void showChooseFileDlg() throws ASTError, NoFileError {
        try {
            final String[] sFileList = this.docController.getDialogUtils().chooseFileDialog(this.docController.getPathUtils().getFileURLFromSystemPath(this.backupDir, this.backupDir), false, new String[][] { new String[] { this.docController.getLanguageController().__("Backed up AST documents (*.odt, *.zip)"), "*.odt;*.zip" } }, 0);
            if (sFileList != null && sFileList.length == 1) {
                if (DocumentController.getStaticMessageBox().showQueryBox("Backup", this.docController.getLanguageController().__("All changes will be overwritten with the restored backup. Continue?"))) {
                    this.restoreBackup(new Backup(this.docController.getPathUtils().getSystemPathFromFileURL(sFileList[0])));
                }
            }
        } catch (final ConfigHandlerError e) {
            throw new BackupError(e.getLogMessage(), e);
        }
    }

    /**
     * This method restores the chosen backup file which is dereferenced with
     * the parameter.
     *
     * @param aFile the Backupfile which should be restored
     * @throws BackupIOError If IO error occured
     * @throws BackupError Configuration error
     * @throws BackupRestoreError BackupFile restoring failed
     * @throws BackupFileNotFoundError If referenced backup file was not found
     * @throws NoFileError If document is new and not stored (no path available)
     */
    public void restoreBackup(final Backup aFile) throws BackupIOError, BackupRestoreError, BackupFileNotFoundError, NoFileError, BackupError {
        try {
            if (!aFile.isFile() || !aFile.canRead() || !(aFile.getAbsolutePath().contains(".zip") || aFile.getAbsolutePath().contains(".odt"))) {
                throw new BackupIOError("No valid backup file!");
            } else if (this.docController.getFileURL().isEmpty()) {
                throw new NoFileError("No path information for current document - unsaved?");
            }
            if (aFile.isAllFiles()) {
                final String tmpDir = this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getPathUtils().getPath(PathUtils.TEMP));
                final String userDir = this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getPathUtils().getPath(PathUtils.USER));
                FileUtils.unzip(aFile.getAbsolutePath(), tmpDir);
                File tmpOdt = new File(tmpDir + File.separator + aFile.getBackupName() + "-" + this.dateFormat.format(aFile.getCreated()) + ".odt");
                File tmpSourceDB = FileUtils.getFiles(new File(tmpDir + File.separator + "sourceDB"), null)[0];
                File tmpQuotationDB = FileUtils.getFiles(new File(tmpDir + File.separator + "quotDB"), null)[0];
                File tmpSourcesDir = new File(tmpDir + File.separator + "sources");
                this.docController.exit(true);
                this.docController.dispatch(".uno:EditDoc");
                FileUtils.copy(tmpOdt.getAbsolutePath(), this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getFileURL()));
                this.docController.dispatch(".uno:EditDoc");
                if (!this.docController.getConfigHandler().getOOoProperty("txtSource.SourceDB").isEmpty() && !this.docController.getConfigHandler().getOOoProperty("txtSource.QuotationStyleDB").isEmpty()) {
                    FileUtils.copy(tmpSourceDB.getAbsolutePath(), userDir + File.separator + this.docController.getConfigHandler().getOOoProperty("txtSource.SourceDB"));
                    FileUtils.copy(tmpQuotationDB.getAbsolutePath(), userDir + File.separator + this.docController.getConfigHandler().getOOoProperty("txtSource.QuotationStyleDB"));
                    this.docController.getSourceController();
                }
                if (tmpSourcesDir.isDirectory() && !this.docController.getConfigHandler().getOOoProperty("txtSource.BackupDir").isEmpty()) {
                    for (final File curFile : FileUtils.getFiles(tmpSourcesDir, null)) {
                        FileUtils.copy(curFile.getAbsolutePath(), this.docController.getConfigHandler().getOOoProperty("txtSource.BackupDir") + File.separator + curFile.getName());
                    }
                }
                tmpOdt.delete();
                FileUtils.deleteDir(tmpSourceDB.getParentFile(), null);
                FileUtils.deleteDir(tmpQuotationDB.getParentFile(), null);
                FileUtils.deleteDir(tmpSourcesDir, null);
            } else {
                this.docController.dispatch(".uno:EditDoc");
                FileUtils.copy(aFile, new File(this.docController.getPathUtils().getSystemPathFromFileURL(this.docController.getFileURL())));
                this.docController.dispatch(".uno:EditDoc");
            }
            this.docController.dispatch(".uno:Reload");
            String msgVersion = "";
            if (aFile.getCreated() != null) {
                msgVersion = " " + this.dateFormat.format(aFile.getCreated());
            }
            DocumentController.getStaticMessageBox().showInfoBox("Backup", this.docController.getLanguageController().__("Your backup{0} was restored.", msgVersion));
        } catch (final FileError e) {
            throw new BackupFileNotFoundError(e.getLogMessage(), e);
        } catch (final ConfigHandlerError e) {
            throw new BackupError(e.getLogMessage(), e);
        } catch (final DBError e) {
            throw new BackupError(e.getLogMessage(), e);
        } catch (final DispatchError e) {
            e.warning(false);
            DocumentController.getStaticMessageBox().showWarningBox(this.docController.getLanguageController().__("Reload document"), this.docController.getLanguageController().__("Reloading document failed. Please perform it manually."));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            this.saveBackup(ConverterUtils.convertStringShortToBoolean(this.docController.getConfigHandler().getOOoProperty("chkBackup.FullBackup")));
        } catch (final ASTError e) {
            e.warning(false);
        }
        this.docController.getLogger().fine("Autobackup was saved successfully.");
    }

    /**
     * This method stops the current timer for the autobackup mechanism if one exists.
     */
    public void stopTimer() {
        if (this.scheduledTask != null) this.scheduledTask.cancel();
    }

    /**
     * This method stores the opened document to the given directory / path / fileurl.
     *
     * @param sFullFilePath path/directory or fileurl for storing the Backupfile
     * @throws ConfigHandlerError from PathUtils
     * @throws NoFileError From {@link ast.DocumentController#saveDoc(java.lang.String, boolean)}
     * @throws FileError From {@link ast.DocumentController#saveDoc(java.lang.String, boolean)}
     */
    private void storeToFile(String sFullFilePath) throws ConfigHandlerError, FileError, NoFileError {
        if (!sFullFilePath.contains("file://")) {
            sFullFilePath = this.docController.getPathUtils().getFileURLFromSystemPath(sFullFilePath, sFullFilePath);
        }
        this.docController.saveDoc(sFullFilePath, true);
    }
}
