package org.gamenet.application.mm8leveleditor.lod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import org.gamenet.util.MonitoredTaskThread;
import com.mmbreakfast.unlod.lod.LodFile;

public class ImportManager {

    private ProgressDisplayer progressDisplayer = null;

    private MonitoredTaskThread importThread = null;

    public ImportManager(ProgressDisplayer progressDisplayer) {
        super();
        this.progressDisplayer = progressDisplayer;
    }

    public class ImportTask implements Runnable {

        private LodFile oldLodFile = null;

        private File newLodFileToCreate = null;

        private List filesToImportList = null;

        private LodResource resourceToImport = null;

        public ImportTask(LodFile oldLodFile, File newLodFileToCreate, List filesToImportList) {
            this.oldLodFile = oldLodFile;
            this.newLodFileToCreate = newLodFileToCreate;
            this.filesToImportList = filesToImportList;
        }

        public ImportTask(LodFile oldLodFile, File newLodFileToCreate, LodResource resourceToImport) {
            this.oldLodFile = oldLodFile;
            this.newLodFileToCreate = newLodFileToCreate;
            this.resourceToImport = resourceToImport;
        }

        public void run() {
            String error = importResources(oldLodFile, newLodFileToCreate, filesToImportList, resourceToImport);
            if (null != error) {
                throw new RuntimeException(error);
            }
        }
    }

    /**
     * @param oldLodFile original lod file
     * @param newLodFileToCreate new lod file based on old lod file + files to import.
     * @param filesToImportList list of File objects to update or add to new lod file.
     */
    public void importAndMonitorResources(LodFile oldLodFile, File newLodFileToCreate, List filesToImportList) {
        ImportTask importTask = new ImportTask(oldLodFile, newLodFileToCreate, filesToImportList);
        importThread = new MonitoredTaskThread(progressDisplayer, "Please wait.\nImporting...", "startup", importTask);
        importThread.start();
    }

    /**
     * @param oldLodFile original lod file
     * @param newLodFileToCreate new lod file based on old lod file + files to import.
     * @param filesToImportList list of File objects to update or add to new lod file.
     */
    public void importAndMonitorResource(LodFile oldLodFile, File newLodFileToCreate, LodResource resourceToReplace) {
        ImportTask importTask = new ImportTask(oldLodFile, newLodFileToCreate, resourceToReplace);
        importThread = new MonitoredTaskThread(progressDisplayer, "Please wait.\nImporting...", "startup", importTask);
        importThread.start();
    }

    public class UpdateTask implements Runnable {

        private LodFile oldLodFile = null;

        private LodResource resourceToImport = null;

        public UpdateTask(LodFile oldLodFile, LodResource resourceToImport) {
            this.oldLodFile = oldLodFile;
            this.resourceToImport = resourceToImport;
        }

        public void run() {
            String error = updateResource(oldLodFile, resourceToImport);
            if (null != error) {
                throw new RuntimeException(error);
            }
        }
    }

    public void updateAndMonitorResource(LodFile oldLodFile, LodResource resourceToReplace) {
        UpdateTask updateTask = new UpdateTask(oldLodFile, resourceToReplace);
        importThread = new MonitoredTaskThread(progressDisplayer, "Please wait.\nUpdating...", "startup", updateTask);
        importThread.start();
    }

    /**
     * @param oldLodFile original lod file
     * @param filesToImportList list of File objects to update or add to new lod file.
     */
    protected String updateResource(LodFile oldLodFile, LodResource resourceToImport) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(oldLodFile.getFile(), "rw");
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            return "Unable to update file '" + oldLodFile.getFile().getAbsolutePath() + "': File not found -- " + exception.getMessage();
        }
        try {
            oldLodFile.updateByAppendingData(importThread, randomAccessFile, resourceToImport);
        } catch (IOException exception) {
            exception.printStackTrace();
            return "Unable to update file '" + oldLodFile.getFile().getAbsolutePath() + "': IOException -- " + exception.getMessage();
        } catch (InterruptedException exception) {
            return "Cancelled by User.";
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param oldLodFile original lod file
     * @param newLodFileToCreate new lod file based on old lod file + files to import.
     * @param filesToImportList list of File objects to update or add to new lod file.
     */
    protected String importResources(LodFile oldLodFile, File newLodFileToCreate, List filesToImportList, LodResource resourceToImport) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(newLodFileToCreate, "rw");
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            return "Unable to create file '" + newLodFileToCreate.getAbsolutePath() + "': File not found -- " + exception.getMessage();
        }
        try {
            oldLodFile.write(importThread, randomAccessFile, filesToImportList, resourceToImport);
        } catch (IOException exception) {
            exception.printStackTrace();
            return "Unable to create file '" + newLodFileToCreate.getAbsolutePath() + "': IOException -- " + exception.getMessage();
        } catch (InterruptedException exception) {
            return "Cancelled by User.";
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
