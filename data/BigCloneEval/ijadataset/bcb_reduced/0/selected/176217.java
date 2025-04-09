package net.sourceforge.jbackupfw.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.sourceforge.jbackupfw.core.Backup;
import net.sourceforge.jbackupfw.core.Restore;
import net.sourceforge.jbackupfw.core.Update;
import net.sourceforge.jbackupfw.core.data.BackUpInfo;
import net.sourceforge.jbackupfw.core.data.BackUpInfoFileGroup;
import net.sourceforge.jbackupfw.core.data.BackupException;
import net.sourceforge.jbackupfw.core.util.ExportData;
import net.sourceforge.jbackupfw.core.util.ExportProperties;
import net.sourceforge.jbackupfw.core.util.ImportData;
import net.sourceforge.jbackupfw.core.util.ImportProperties;
import net.sourceforge.jbackupfw.features.scheduling.JobScheduler;
import net.sourceforge.jbackupfw.features.scheduling.TaskManager;
import net.sourceforge.jbackupfw.features.scheduling.data.ScheduledJobs;
import net.sourceforge.jbackupfw.features.scheduling.util.JobWorker;
import org.dom4j.DocumentException;

/**
 * The class that controls all of the functionality of this framework
 *
 * @author Boris Horvat and Dusan Guduric
 */
public class OperationManager {

    /** Holds the referenc to the objcet that holds the information about
     * all of the backed up data */
    private BackUpInfo backUpInfo = null;

    /** Holds the referenc to the objcet that controles the functionaliy to back up */
    private Backup backUp = null;

    /** Holds the referenc to the objcet that controles the functionaliy to update */
    private Update update = null;

    /** Holds the referenc to the objcet that controles the functionaliy to restore */
    private Restore restore = null;

    /** Holds the referenc to the objcet that controles the functionaliy to export properties */
    private ExportProperties exportProperties = null;

    /** Holds the referenc to the objcet that controles the functionaliy to import properties */
    private ImportProperties importProperties = null;

    /** Holds the referenc to the objcet that controles the functionaliy to import information data */
    private ImportData importData = null;

    /** Holds the referenc to the objcet that controles the functionaliy to export information data*/
    private ExportData exportData = null;

    /** Holds the referenc to the objcet that controles the functionaliy to execute tasks*/
    private TaskManager taskManager = null;

    /** Holds the referenc to the objcet that controles the functionaliy to schedule taskes*/
    private JobScheduler jobSchedule = null;

    /** Holds the referenc to the objcet that controles the functionaliy to extract scheduled job information*/
    private JobWorker scheduledJobs = null;

    /** This constant holds the size of the buffer that is used to holds the bytes */
    private static final byte[] BUFFER = new byte[2156];

    /** This constant holds the name and location of the info file */
    private static final String FILE_INFO = "backUpExternalInfo.out";

    /** This constant holds the name and location of the folder that holds schedule informations */
    private static final String FOLDER_SCHEDULE = "schedule//";

    /** This constant holds the name and the location of the configuration file*/
    private static final String CONFIG_FILE = "settings.cfg";

    /**
     * The constructor that initilize objet that holds the information about
     * all of the backed up data
     */
    public OperationManager() {
        backUpInfo = new BackUpInfo();
    }

    /**
     * This method is used to back up the of data, if this is the new file archive
     * this method backups the files to the desired location, however if this is not
     * the new archive, but instead someone wants to add new files to the existing
     * archive then first all of the files are repacked and then the new files are
     * added to the archive
     *
     * @param fileBackupList holds the list of the paths to files that should be backed up
     * @param pathTo holds the name and the location where the back up file is to be located
     * @param groupName holds the name of the group of files that are being backed up
     * @param groupId holds the id of the group of files that are being backed up
     *
     * @return String that represents the id of the file group that have been backed up
     *
     * @throws BackupException if IO error occures, or if file that needs to be back up does not exist
     */
    public String backUp(LinkedList<String> fileBackupList, String pathTo, String groupName, String groupId) {
        if (!pathTo.endsWith("\\")) {
            groupId = pathTo.substring(pathTo.lastIndexOf("\\") + 1, pathTo.lastIndexOf("."));
            pathTo = pathTo.substring(0, pathTo.lastIndexOf("\\") + 1);
        }
        backUp = new Backup(groupName, groupId);
        if (!groupId.endsWith(".bk")) {
            groupId += ".bk";
        }
        File zipFile = new File(pathTo + groupId);
        ZipOutputStream zos;
        try {
            if (zipFile.exists()) {
                File tempFile = File.createTempFile(zipFile.getName(), null);
                tempFile.delete();
                zipFile.renameTo(tempFile);
                ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile));
                zos = new ZipOutputStream(new FileOutputStream(zipFile));
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    if (entry.getName().equals(FILE_INFO)) {
                        entry = zis.getNextEntry();
                        continue;
                    } else {
                        zos.putNextEntry(new ZipEntry(entry.getName()));
                        int length;
                        while ((length = zis.read(BUFFER)) > 0) {
                            zos.write(BUFFER, 0, length);
                        }
                        entry = zis.getNextEntry();
                    }
                }
                zis.close();
                backUp.execute(fileBackupList, zos);
                BackUpInfoFileGroup fileGroup = importData(tempFile);
                for (int i = 0; i < backUp.getFileGroup().getFileList().size(); i++) {
                    fileGroup.getFileList().add(backUp.getFileGroup().getFileList().get(i));
                }
                fileGroup.setSize(fileGroup.getSize() + backUp.getFileGroup().getSize());
                if (exportData == null) {
                    exportData = new ExportData();
                }
                exportData.execute(fileGroup, zos, FILE_INFO);
            } else {
                zos = new ZipOutputStream(new FileOutputStream(pathTo + groupId));
                backUp.execute(fileBackupList, zos);
                if (exportData == null) {
                    exportData = new ExportData();
                }
                exportData.execute(backUp.getFileGroup(), zos, FILE_INFO);
            }
            zos.close();
            return groupId;
        } catch (FileNotFoundException e) {
            throw new BackupException(e.getMessage());
        } catch (IOException e) {
            throw new BackupException(e.getMessage());
        }
    }

    /**
     * This is a method that is used to restore the data, it can restore to the
     * original location from which the date was originlly backed up or to the
     * new location where the user wants it. The method can also restore all the files
     * or only the chosen ones.
     *
     * @param archive - holds the path to a archive from which the files need to be restored
     * @param outputDir - the location where to files will be restored
     * @param restoreList - list of files that need to be restored
     *
     */
    public void restore(File archive, File outputDir, LinkedList<String> restoreList) {
        if (restore == null) {
            restore = new Restore();
        }
        restore.execute(archive, outputDir, importData(archive), restoreList);
    }

    /**
     * This is a method that is used updates the files that were selected for an update, it
     * first repacks the files that do not need to be updated, and then it throws away the
     * ones that do, and replaces them with the newer versions
     *
     * @param backedUpFiles - informations about all of the files that were previously backed up
     * @param updateFiles - informations about all of the files that are to be updated up
     * @param filePath holds the name and the location where the back up archive file is located
     *
     * @throws BackupException if IO error occures
     */
    public void update(BackUpInfoFileGroup backedUpFiles, BackUpInfoFileGroup updateFiles, String filePath) {
        if (update == null) {
            update = new Update();
        }
        try {
            File zipFile = new File(filePath);
            File tempFile = File.createTempFile(zipFile.getName(), null);
            tempFile.delete();
            zipFile.renameTo(tempFile);
            ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile));
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (entry.getName().equals(FILE_INFO)) {
                    entry = zis.getNextEntry();
                    continue;
                } else if (!update.isToUpdate(updateFiles, entry.getName())) {
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    int len;
                    while ((len = zis.read(BUFFER)) > 0) {
                        zos.write(BUFFER, 0, len);
                    }
                }
                entry = zis.getNextEntry();
            }
            zis.close();
            update.execute(updateFiles, zos);
            if (exportData == null) {
                exportData = new ExportData();
            }
            exportData.execute(update.updateInformationFile(backedUpFiles, updateFiles), zos, FILE_INFO);
            zos.close();
        } catch (IOException e) {
            throw new BackupException(e.getMessage());
        }
    }

    /**
     * This method initialize and scheduleds every task that need to be excuted.
     * Every task is done in its own thread
     */
    public void startTaskManager() {
        String[] schedule = new File(FOLDER_SCHEDULE).list();
        for (int i = 0; i < schedule.length; i++) {
            taskManager = new TaskManager(this, FOLDER_SCHEDULE + schedule[i]);
            taskManager.run();
        }
    }

    /**
     * Stops all of the tasks that are scheduled for the execution
     */
    public void stopTaskManager() {
        if (taskManager != null) {
            taskManager.stop();
        }
    }

    /**
     * Adds an back up job to the list of the scheduled jobs, and schedules it for
     * the execution if the task manager has beed started
     *
     * @param files list of paths to the files that needs to be back
     * @param time time when the back up should be performed
     * @param baseName the string which will help to create a name of the achieve
     * @param groupName the name of the group of file that are backed up
     * @param pathTo the location of the archive
     *
     */
    public void schedualBackUpJob(LinkedList<String> files, String time, String baseName, String groupName, String pathTo) {
        if (jobSchedule == null) {
            jobSchedule = new JobScheduler();
        }
        String name = FOLDER_SCHEDULE + "sc" + String.valueOf(Math.random());
        jobSchedule.executeScheduleBackUp(files, time, name, baseName, groupName, pathTo);
        scheduledJobs.addBackUpJob(jobSchedule.createName(name).substring(10), files, time, baseName, groupName, pathTo);
        if (taskManager != null) {
            taskManager = new TaskManager(this, jobSchedule.createName(name));
            taskManager.run();
        }
    }

    /**
     * Adds an update job to the list of the scheduled jobs, and schedules it for
     * the execution if the task manager has beed started
     * 
     * @param fileID list of files id's that needs to be update
     * @param time time when the update should be performed
     * @param period  time period when the update should be repeted
     * @param path the location of the archive
     *
     */
    public void schedualUpdateJob(LinkedList<String> fileId, String time, String period, String path) {
        if (jobSchedule == null) {
            jobSchedule = new JobScheduler();
        }
        String name = FOLDER_SCHEDULE + "sc" + String.valueOf(Math.random());
        jobSchedule.executeScheduleUpdate(fileId, time, name, period, path);
        scheduledJobs.addUpdateJob(jobSchedule.createName(name).substring(10), fileId, time, period, path);
        if (taskManager != null) {
            taskManager = new TaskManager(this, jobSchedule.createName(name));
            taskManager.run();
        }
    }

    /**
     * Deletes the file which has the information about scheduled back up job
     *
     * @param i the position of a file in the list
     */
    public void removeBackUpJob(int i) {
        if (scheduledJobs != null) {
            scheduledJobs.removeBackUpJob(i);
        }
    }

    /**
     * Deletes the file which has the information about scheduled back up job
     *
     * @param jobPath the name of the file that is to be deleted
     */
    public void removeBackUpJob(String jobPath) {
        if (scheduledJobs != null) {
            scheduledJobs.removeBackUpJob(jobPath);
        }
    }

    /**
     * Deletes the file which has the information about scheduled update job
     *
     * @param i the position of a file in the list
     */
    public void removeUpdateJob(int i) {
        if (scheduledJobs != null) {
            scheduledJobs.removeUpdateJob(i);
        }
    }

    /**
     * Deletes the file which has the information about scheduled update job
     *
     * @param jobPath the name of the file that is to be deleted
     */
    public void removeUpdateJob(String jobPath) {
        if (scheduledJobs != null) {
            scheduledJobs.removeUpdateJob(jobPath);
        }
    }

    /**
     * This method extracts information about the jobs that are scheduled for the execution
     *
     * @return ScheduledJobs object which contains the informations about all of the
     *         scheduled jobs
     *
     * @throws BackupException if no document that holds dose information can be found
     */
    public ScheduledJobs extractScheduledJobs() {
        if (scheduledJobs == null) {
            scheduledJobs = new JobWorker(FOLDER_SCHEDULE);
        }
        try {
            return scheduledJobs.execute();
        } catch (DocumentException e) {
            throw new BackupException(e.getMessage());
        }
    }

    /**
     * This method writes information into properties file in order to be able
     * to know back up specific
     *
     * @param properties list of the names of the properties that should be write into the file
     * @param propertieValue list of the values of the properties that should be write into the file
     */
    public void exportProperties(LinkedList<String> properties, LinkedList<String> propertieValue) {
        if (exportProperties == null) {
            exportProperties = new ExportProperties();
        }
        exportProperties.execute(properties, propertieValue, CONFIG_FILE);
    }

    /**
     * Addes another archive to the objet that holds the information about
     * all of the backed up data in order to be able to use the framwork with
     * that external archive
     *
     * @param archive location to the external archive
     */
    public void addExternalArchive(BackUpInfoFileGroup archive) {
        backUpInfo.getBackUpDataBase().add(archive);
    }

    /**
     * This method retrieves all the data about the files that were backed up and
     * adds the archive information to group information
     *
     * @param archive list of file archives
     */
    public void importDataBase(LinkedList<File> archive) {
        if (importData == null) {
            importData = new ImportData();
        }
        backUpInfo.getBackUpDataBase().clear();
        boolean error = false;
        for (int i = 0; i < archive.size(); i++) {
            BackUpInfoFileGroup fileGroup = importData.execute(archive.get(i));
            if (fileGroup != null) {
                backUpInfo.getBackUpDataBase().add(fileGroup);
            } else {
                error = true;
            }
        }
        if (error) {
            throw new BackupException("");
        }
    }

    /**
     * This method is used to extract all of the information about the data that
     * was backed up to some location. It first unpacks the file and then
     * it reads the information from it, after which the file is deleted.
     *
     * If some error happens and tha file is corrupted the file is removed,
     * so that the integrity is kept.
     *
     * @param archive - holds the file which has the informations backed up data
     *
     * @return the object of the type BackUpInfoFileGroup that holds all of the
     *         information about the files that were backed up
     *
     */
    public BackUpInfoFileGroup importData(File archive) {
        if (importData == null) {
            importData = new ImportData();
        }
        return importData.execute(archive);
    }

    /**
     * This method reads from the properties file in order to find out if the
     * location of the default back up folder
     *
     * @return String that represents the location of the default back up folder,
     *         empty string if the propertie dose not exist
     *
     * @throws DocumentException if the propertie file does not exist
     */
    public String getDefaultbackUpFolder() throws DocumentException {
        if (importProperties == null) {
            importProperties = new ImportProperties(CONFIG_FILE);
        }
        return importProperties.getDefaultbackUpFolder();
    }

    /**
     * This is a method that reads from the properties file in order to find out
     * the name of the date base
     *
     * @return the String that represents the name of the data base
     *
     * @throws DocumentException if the propertie file does not exist
     * @throws NullPointerException if the propertie dose not exist
     */
    public String getPropertieDataBaseName() throws DocumentException {
        if (importProperties == null) {
            importProperties = new ImportProperties(CONFIG_FILE);
        }
        return importProperties.getPropertieDataBaseName();
    }

    /**
     * This method reads from the properties file in order to find out if the
     * task menager is enabled
     *
     * @return String that represents if the task menager should be started,
     *         empty string if the propertie dose not exist
     *
     * @throws DocumentException if the propertie file does not exist
     */
    public String getPropertieTaskManagerEnabled() throws DocumentException {
        if (importProperties == null) {
            importProperties = new ImportProperties(CONFIG_FILE);
        }
        return importProperties.getPropertieTaskManagerEnabled();
    }

    /**
     * This is a method that reads from the properties file in order to find out
     * the location where the archive should be stored
     *
     * @return the location of the folder where the archive should be located
     *
     * @throws DocumentException if the propertie file does not exist
     * @throws NullPointerException if the propertie dose not exist
     */
    public String getPropertieDataBaseFolder() throws DocumentException {
        if (importProperties == null) {
            importProperties = new ImportProperties(CONFIG_FILE);
        }
        return importProperties.getPropertieDataBaseFolder();
    }

    /**
     * This is a method that reads from the properties file in order to find out
     * the number of back up copies that should be created one a back up is performing
     *
     * @return the String that represents the number of times a backUp should be performed,
     *         returns 1 if the propertie dose not exist
     *
     * @throws DocumentException if the propertie file does not exist
     */
    public String getPropertieBackUpNumberOfCopies() throws DocumentException {
        if (importProperties == null) {
            importProperties = new ImportProperties(CONFIG_FILE);
        }
        return importProperties.getPropertieBackUpNumberOfCopies();
    }

    /**
     * Returns objet that holds the information about all of the backed up data
     *
     * @return objet that holds the information about all of the backed up data
     */
    public BackUpInfo getBackUpInfo() {
        return backUpInfo;
    }

    /**
     * Returns the number of file that is currently being backed up
     *
     * @return the intager number of file that is currently being backed up
     *         0 if the process is begining, -1 if there is nothing to back up
     */
    public int getBackUpCounter() {
        if (backUp == null) {
            return 0;
        }
        return backUp.getCounter();
    }

    /**
     * Returns the number of file that is currently being backed up
     *
     * @return the intager number of file that is currently being backed up
     *         0 if the process is begining, -1 if there is nothing to back up
     */
    public int getRestoreCounter() {
        if (restore == null) {
            return 0;
        }
        return restore.getCounter();
    }

    /**
     * Sets the value that tells if the task is done or not to the given value
     *
     * @param over is the task over or not
     */
    public void setTaskOver(boolean over) {
        taskManager.setTaskOver(over);
    }

    /**
     * Cheks if some task is done, and if it is resets the value to false
     *
     * @return true if any of the scheduled tasks is done, false otherwise
     */
    public boolean isTaskOver() {
        if (taskManager == null) {
            return false;
        }
        return taskManager.isTaskOver();
    }
}
