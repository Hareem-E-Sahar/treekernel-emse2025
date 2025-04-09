package supersync.fileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.application.ResourceMap;
import supersync.file.AbstractFile;
import supersync.sync.Logger;

/** This class can be used to quickly schedule a delete operation.
 *
 * @author Brandon Drake
 */
public class ScheduleDeleteOperationThread extends Thread {

    protected final FileManager fileManager;

    protected final List<AbstractFile> files;

    protected static final ResourceMap resMap = org.jdesktop.application.Application.getInstance(supersync.SynchronizerApp.class).getContext().getResourceMap(ScheduleDeleteOperationThread.class);

    /** Gets a delete operation for the folder and all sub items.
     */
    public static SimpleFileOp_Delete getDeleteOperation(AbstractFile l_file) throws IOException {
        SimpleFileOp_Delete[] subOperations = null;
        if (l_file.isDirectory()) {
            AbstractFile[] files = l_file.listFiles();
            subOperations = new SimpleFileOp_Delete[files.length];
            for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
                if (false == files[fileIndex].isDirectory()) {
                    subOperations[fileIndex] = new SimpleFileOp_Delete(files[fileIndex]);
                } else {
                    subOperations[fileIndex] = getDeleteOperation(files[fileIndex]);
                }
            }
        }
        SimpleFileOp_Delete result = new SimpleFileOp_Delete(l_file);
        result.childOperations = subOperations;
        return result;
    }

    @Override
    public void run() {
        scheduleDeleteOperation();
    }

    /** Schedules the delete operation and returns when done.  Use the start() method to have the thread schedule the operation.
     */
    public void scheduleDeleteOperation() {
        for (AbstractFile file : this.files) {
            SimpleFileOp_Delete operation;
            try {
                operation = getDeleteOperation(file);
            } catch (IOException ex) {
                Logger.defaultLogger.logDebugError(ex);
                Logger.defaultLogger.Log(resMap.getString("message.unableToSetupDeleteOperation.text", ex.getLocalizedMessage()), Logger.LogLevel.ERROR);
                return;
            }
            fileManager.addOperation(operation);
        }
    }

    /** Constructor.
     */
    public ScheduleDeleteOperationThread(AbstractFile l_file, FileManager l_fileManager) {
        this.fileManager = l_fileManager;
        this.files = new ArrayList<AbstractFile>();
        this.files.add(l_file);
    }

    /** Constructor.
     */
    public ScheduleDeleteOperationThread(List<AbstractFile> l_files, FileManager l_fileManager) {
        this.fileManager = l_fileManager;
        this.files = l_files;
    }
}
