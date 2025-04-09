package be.vds.jtb.taskmanager.model.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import be.vds.jtb.taskmanager.model.Task;
import be.vds.jtb.taskmanager.utils.FileUtil;

/**
 * 
 * Configuration class that will copy a folder to another. The variable
 * deleteFilesNotInSource is set true if the file that are not in the source
 * folder must be deleted.
 */
public class CopyFileTask extends Task {

    private static final Logger logger = Logger.getLogger(CopyFileTask.class);

    private String fileSrc;

    private String fileDest;

    private boolean deleteFilesNotInSource;

    private int total;

    private int current;

    public CopyFileTask() {
    }

    public CopyFileTask(String folderSrc, String folderDest) {
        this.fileDest = folderDest;
        this.fileSrc = folderSrc;
    }

    public String getFolderSrc() {
        return fileSrc;
    }

    public void setFolderSrc(String folderSrc) {
        this.fileSrc = folderSrc;
    }

    public String getFolderDest() {
        return fileDest;
    }

    public void setFolderDest(String folderDest) {
        this.fileDest = folderDest;
    }

    private void execute(String folderSrc, String folderDest) {
        try {
            File folderSrcFile = new File(folderSrc);
            File[] originalFilesSrc = folderSrcFile.listFiles();
            File folderDestFile = new File(folderDest);
            File[] originalFilesDest = folderDestFile.listFiles();
            if (!folderDestFile.exists()) {
                folderDestFile.mkdirs();
                logger.debug("creating " + folderDestFile.getAbsolutePath());
            }
            for (int i = 0; i < originalFilesSrc.length; i++) {
                if (originalFilesSrc[i].isDirectory()) {
                    String newFolder = folderDestFile.getAbsolutePath() + File.separatorChar + originalFilesSrc[i].getName();
                    File toCreateFolder = new File(newFolder);
                    execute(originalFilesSrc[i].getAbsolutePath(), toCreateFolder.getAbsolutePath());
                } else {
                    File newFile = new File(folderDest + File.separatorChar + originalFilesSrc[i].getName());
                    if (!newFile.exists()) {
                        copyFile(originalFilesSrc[i], newFile);
                    } else {
                        if (newFile.lastModified() != originalFilesSrc[i].lastModified()) {
                            newFile.delete();
                            logger.info("deleting " + newFile.getAbsolutePath());
                            copyFile(originalFilesSrc[i], newFile);
                        }
                    }
                }
            }
            if (deleteFilesNotInSource && null != originalFilesDest) {
                List<Integer> toKeep = new ArrayList<Integer>();
                for (int i = 0; i < originalFilesDest.length; i++) {
                    File dest = originalFilesDest[i];
                    for (int j = 0; j < originalFilesSrc.length; j++) {
                        File src = originalFilesSrc[j];
                        if (src.getName().equals(dest.getName())) {
                            toKeep.add(i);
                        }
                    }
                }
                for (int i = 0; i < originalFilesDest.length; i++) {
                    if (!toKeep.contains(i)) {
                        if (originalFilesDest[i].isDirectory()) {
                            FileUtil.deleteFolder(originalFilesDest[i]);
                        } else {
                            originalFilesDest[i].delete();
                        }
                        logger.info("deleting " + originalFilesDest[i].getAbsolutePath());
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        setCurrentProgress(current++);
        fireTaskProgress("Copying file " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());
        long start = System.currentTimeMillis();
        FileUtil.copyFile(src, dest);
        dest.setLastModified(src.lastModified());
        logger.debug((System.currentTimeMillis() - start) + "ms to copy " + src + " to " + dest);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Copy from ").append(fileSrc);
        sb.append(" to ").append(fileDest);
        if (deleteFilesNotInSource) {
            sb.append(" (delete non original source files)");
        }
        return sb.toString();
    }

    public void setDeleteNonOriginalFiles(boolean deleteNonOriginalFiles) {
        this.deleteFilesNotInSource = deleteNonOriginalFiles;
    }

    public boolean getDeleteNonOriginalFiles() {
        return deleteFilesNotInSource;
    }

    private int getNumberOfFileToCopy(File src, File dest) {
        int result = 0;
        File[] originalFilesSrc = src.listFiles();
        if (running) {
            if (null == dest || !dest.exists()) {
                for (int i = 0; i < originalFilesSrc.length; i++) {
                    File f = originalFilesSrc[i];
                    if (f.isDirectory()) {
                        result += getNumberOfFileToCopy(f, null);
                    } else {
                        result += 1;
                    }
                }
                return result;
            }
        }
        for (int i = 0; i < originalFilesSrc.length; i++) {
            if (originalFilesSrc[i].isDirectory()) {
                String newFolder = dest.getAbsolutePath() + "/" + originalFilesSrc[i].getName();
                File newDest = new File(newFolder);
                result += getNumberOfFileToCopy(originalFilesSrc[i], newDest);
            } else {
                File newFile = new File(dest + "/" + originalFilesSrc[i].getName());
                if (!newFile.exists()) {
                    result += 1;
                } else {
                    if (newFile.lastModified() != originalFilesSrc[i].lastModified()) {
                        result += 1;
                    }
                }
            }
        }
        return result;
    }

    public int getNumberOfFileToCopy() {
        return getNumberOfFileToCopy(new File(fileSrc), new File(fileDest));
    }

    @Override
    public void doBeforeProcess() {
        fireTaskStarted("Process started. Counting number of files to copy.");
        total = getNumberOfFileToCopy();
        current = 0;
        setMaxProgress(total);
    }

    @Override
    public void processTask() {
        File src = new File(fileSrc);
        if (src.exists()) {
            execute(fileSrc, fileDest);
        }
        current = 0;
        total = 0;
    }

    @Override
    public void doAfterProcess() {
    }
}
