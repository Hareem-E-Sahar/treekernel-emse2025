package net.sf.mzmine.modules.projectmethods.projectsave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.impl.MainWindow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.projectmethods.projectload.ProjectLoaderParameters;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.project.impl.MZmineProjectImpl;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.ExceptionUtils;
import net.sf.mzmine.util.StreamCopy;
import org.xml.sax.SAXException;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * This class is using ZipEntry and ZipOutputStream from the truezip library, in
 * order to get Zip64 support to overcome the 4GB size limitation.
 * 
 */
public class ProjectSavingTask extends AbstractTask {

    public static final String VERSION_FILENAME = "MZMINE_VERSION";

    public static final String CONFIG_FILENAME = "configuration.xml";

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private File saveFile;

    private MZmineProjectImpl savedProject;

    private RawDataFileSaveHandler rawDataFileSaveHandler;

    private PeakListSaveHandler peakListSaveHandler;

    private UserParameterSaveHandler userParameterSaveHandler;

    private int currentStage;

    private String currentSavedObjectName;

    private Hashtable<RawDataFile, String> dataFilesIDMap;

    public ProjectSavingTask(ParameterSet parameters) {
        this.saveFile = parameters.getParameter(ProjectLoaderParameters.projectFile).getValue();
        dataFilesIDMap = new Hashtable<RawDataFile, String>();
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getTaskDescription()
     */
    public String getTaskDescription() {
        if (currentSavedObjectName == null) return "Saving project";
        return "Saving project (" + currentSavedObjectName + ")";
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
     */
    public double getFinishedPercentage() {
        switch(currentStage) {
            case 2:
                if (rawDataFileSaveHandler == null) return 0;
                return rawDataFileSaveHandler.getProgress();
            case 3:
                if (peakListSaveHandler == null) return 0;
                return peakListSaveHandler.getProgress();
            case 4:
                if (userParameterSaveHandler == null) return 0;
                return userParameterSaveHandler.getProgress();
            case 5:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#cancel()
     */
    public void cancel() {
        logger.info("Canceling saving of project to " + saveFile);
        setStatus(TaskStatus.CANCELED);
        if (rawDataFileSaveHandler != null) rawDataFileSaveHandler.cancel();
        if (peakListSaveHandler != null) peakListSaveHandler.cancel();
        if (userParameterSaveHandler != null) userParameterSaveHandler.cancel();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            logger.info("Saving project to " + saveFile);
            setStatus(TaskStatus.PROCESSING);
            savedProject = (MZmineProjectImpl) MZmineCore.getCurrentProject();
            File tempFile = File.createTempFile(saveFile.getName(), ".tmp", saveFile.getParentFile());
            tempFile.deleteOnExit();
            FileOutputStream tempStream = new FileOutputStream(tempFile);
            ZipOutputStream zipStream = new ZipOutputStream(tempStream);
            currentStage++;
            saveVersion(zipStream);
            saveConfiguration(zipStream);
            if (isCanceled()) {
                zipStream.close();
                tempFile.delete();
                return;
            }
            currentStage++;
            saveRawDataFiles(zipStream);
            if (isCanceled()) {
                zipStream.close();
                tempFile.delete();
                return;
            }
            currentStage++;
            savePeakLists(zipStream);
            if (isCanceled()) {
                zipStream.close();
                tempFile.delete();
                return;
            }
            currentStage++;
            saveUserParameters(zipStream);
            if (isCanceled()) {
                zipStream.close();
                tempFile.delete();
                return;
            }
            currentStage++;
            currentSavedObjectName = null;
            zipStream.close();
            if (isCanceled()) {
                tempFile.delete();
                return;
            }
            if (saveFile.exists() && !saveFile.delete()) {
                throw new IOException("Could not delete old file " + saveFile);
            }
            boolean renameOK = tempFile.renameTo(saveFile);
            if (!renameOK) {
                throw new IOException("Could not move the temporary file " + tempFile + " to the final location " + saveFile);
            }
            savedProject.setProjectFile(saveFile);
            if (MZmineCore.getDesktop() instanceof MainWindow) {
                MainWindow mainWindow = (MainWindow) MZmineCore.getDesktop();
                mainWindow.updateTitle();
            }
            logger.info("Finished saving the project to " + saveFile);
            setStatus(TaskStatus.FINISHED);
        } catch (Throwable e) {
            setStatus(TaskStatus.ERROR);
            if (currentSavedObjectName == null) {
                errorMessage = "Failed saving the project: " + ExceptionUtils.exceptionToString(e);
            } else {
                errorMessage = "Failed saving the project. Error while saving " + currentSavedObjectName + ": " + ExceptionUtils.exceptionToString(e);
            }
        }
    }

    /**
     * Save the version info
     * 
     * @throws java.io.IOException
     */
    private void saveVersion(ZipOutputStream zipStream) throws IOException {
        zipStream.putNextEntry(new ZipEntry(VERSION_FILENAME));
        String MZmineVersion = MZmineCore.getMZmineVersion();
        zipStream.write(MZmineVersion.getBytes());
    }

    /**
     * Save the configuration file.
     * 
     * @throws java.io.IOException
     */
    private void saveConfiguration(ZipOutputStream zipStream) throws IOException {
        logger.info("Saving configuration file");
        currentSavedObjectName = "configuration";
        zipStream.putNextEntry(new ZipEntry(CONFIG_FILENAME));
        File tempConfigFile = File.createTempFile("mzmineconfig", ".tmp");
        try {
            MZmineCore.getConfiguration().saveConfiguration(tempConfigFile);
        } catch (Exception e) {
            throw new IOException("Could not save configuration" + ExceptionUtils.exceptionToString(e));
        }
        FileInputStream fileStream = new FileInputStream(tempConfigFile);
        StreamCopy copyMachine = new StreamCopy();
        copyMachine.copy(fileStream, zipStream);
        fileStream.close();
        tempConfigFile.delete();
    }

    /**
     * Save the raw data files
     * 
     * @throws SAXException
     * @throws TransformerConfigurationException
     */
    private void saveRawDataFiles(ZipOutputStream zipStream) throws IOException, TransformerConfigurationException, SAXException {
        rawDataFileSaveHandler = new RawDataFileSaveHandler(zipStream);
        RawDataFile rawDataFiles[] = savedProject.getDataFiles();
        for (int i = 0; i < rawDataFiles.length; i++) {
            if (isCanceled()) return;
            currentSavedObjectName = rawDataFiles[i].getName();
            rawDataFileSaveHandler.writeRawDataFile((RawDataFileImpl) rawDataFiles[i], i + 1);
            dataFilesIDMap.put(rawDataFiles[i], String.valueOf(i + 1));
        }
    }

    /**
     * Save the peak lists
     * 
     * @throws SAXException
     * @throws TransformerConfigurationException
     */
    private void savePeakLists(ZipOutputStream zipStream) throws IOException, TransformerConfigurationException, SAXException {
        PeakList peakLists[] = savedProject.getPeakLists();
        for (int i = 0; i < peakLists.length; i++) {
            if (isCanceled()) return;
            logger.info("Saving peak list: " + peakLists[i].getName());
            String peakListSavedName = "Peak list #" + (i + 1) + " " + peakLists[i].getName();
            zipStream.putNextEntry(new ZipEntry(peakListSavedName + ".xml"));
            peakListSaveHandler = new PeakListSaveHandler(zipStream, dataFilesIDMap);
            currentSavedObjectName = peakLists[i].getName();
            peakListSaveHandler.savePeakList(peakLists[i]);
        }
    }

    /**
     * Save the peak lists
     * 
     * @throws SAXException
     * @throws TransformerConfigurationException
     */
    private void saveUserParameters(ZipOutputStream zipStream) throws IOException, TransformerConfigurationException, SAXException {
        if (isCanceled()) return;
        logger.info("Saving user parameters");
        zipStream.putNextEntry(new ZipEntry("User parameters.xml"));
        userParameterSaveHandler = new UserParameterSaveHandler(zipStream, savedProject, dataFilesIDMap);
        currentSavedObjectName = "User parameters";
        userParameterSaveHandler.saveParameters();
    }

    public Object[] getCreatedObjects() {
        return null;
    }
}
