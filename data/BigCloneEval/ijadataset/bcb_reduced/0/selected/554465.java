package org.op.service.fileio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.ProgressMonitor;
import org.op.menu.NotebookController;

public class FileZipServiceImpl implements FileZipService {

    private NotebookController notebookController;

    public static final int BUFFER = 2048;

    private byte data[];

    private BufferedInputStream origin;

    int folderPathIdx;

    private String inputPath;

    private String outputPath;

    private ProgressMonitor monitor;

    private int count;

    public void zipFolder(String pathToFolder, String zipFilePath) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));
        data = new byte[BUFFER];
        folderPathIdx = pathToFolder.length();
        File folder = new File(pathToFolder);
        zipSubFolder(folder, out);
        out.close();
        if (monitor != null) {
            monitor.setProgress(100);
        }
        notebookController.zipComplete();
    }

    private void zipSubFolder(File folder, ZipOutputStream out) throws Exception {
        File children[] = folder.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                zipSubFolder(child, out);
            } else {
                String filePath = child.getAbsolutePath();
                String relPath = filePath.substring(folderPathIdx + 1);
                updateMonitor(child.getName());
                FileInputStream fi = new FileInputStream(child);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relPath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    private void updateMonitor(String fileName) {
        if (monitor != null && !monitor.isCanceled()) {
            count += 1;
            if (count > 99) {
                count = 1;
            }
            monitor.setProgress(count);
            monitor.setNote("Zipping file " + fileName);
        }
    }

    public void setInputPath(String workspaceFolderPath) {
        inputPath = workspaceFolderPath;
    }

    public void setOutputPath(String zipFilePath) {
        outputPath = zipFilePath;
    }

    public void setMonitor(ProgressMonitor pm) {
        monitor = pm;
    }

    public void run() {
        try {
            count = 0;
            zipFolder(inputPath, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NotebookController getNotebookController() {
        return notebookController;
    }

    public void setNotebookController(NotebookController notebookController) {
        this.notebookController = notebookController;
    }
}
