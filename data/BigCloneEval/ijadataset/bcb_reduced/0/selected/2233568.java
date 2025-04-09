package br.com.visualmidia.ui.wizard.backup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.swt.widgets.ProgressBar;
import br.com.visualmidia.system.GDServer;
import br.com.visualmidia.system.GDSystem;

/**
 * @author  Lucas
 */
public class CompactFilesFromFolders {

    private String destinationZipFile;

    private List<String> listNameFolders = new ArrayList<String>();

    private GDSystem system;

    private ZipOutputStream zipOutputStream;

    private FileOutputStream fileOutputStream;

    private ByteArrayOutputStream byteArrayOutputStream;

    private int sizeCurrentBufferFile = 0;

    private ProgressBar progressBar;

    private int cont = 0;

    public CompactFilesFromFolders(List<String> nameFolder, String destinationZipFile, ProgressBar progressBar) {
        system = GDSystem.getInstance();
        this.listNameFolders = nameFolder;
        this.destinationZipFile = destinationZipFile;
        this.progressBar = progressBar;
    }

    public void run() {
        takeSnapshot();
        zipFolder();
    }

    private void takeSnapshot() {
        progressBar.getDisplay().syncExec(new Runnable() {

            public void run() {
                progressBar.setSelection(cont++);
            }
        });
        system.takeSnapshot();
        progressBar.getDisplay().syncExec(new Runnable() {

            public void run() {
                progressBar.setSelection(cont++);
            }
        });
    }

    public void zipFolder() {
        fileOutputStream = null;
        zipOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(destinationZipFile);
            byteArrayOutputStream = new ByteArrayOutputStream();
            zipOutputStream = new ZipOutputStream(fileOutputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (String nameFolder : listNameFolders) {
            addFolderToZip("", nameFolder, zipOutputStream);
        }
        try {
            zipOutputStream.flush();
            zipOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addToZip(String path, String srcFile, ZipOutputStream zipOutputStream) {
        File file = new File(srcFile);
        if (file.isDirectory()) {
            addFolderToZip(path, srcFile, zipOutputStream);
        } else {
            byte[] lenghtBuffer = new byte[1024];
            int lenghtBytes;
            try {
                FileInputStream fileInputStream = new FileInputStream(srcFile);
                ZipEntry zipEntry = new ZipEntry(srcFile);
                new PrintStream(byteArrayOutputStream);
                zipOutputStream.putNextEntry(zipEntry);
                while ((lenghtBytes = fileInputStream.read(lenghtBuffer)) > 0) {
                    zipOutputStream.write(lenghtBuffer, 0, lenghtBytes);
                    sizeCurrentBufferFile += lenghtBytes;
                }
                zipOutputStream.closeEntry();
                progressBar.getDisplay().syncExec(new Runnable() {

                    public void run() {
                        progressBar.setSelection(cont++);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zipOutputStream) {
        File folder = new File(srcFolder);
        String fileList[] = folder.list();
        try {
            int i = 0;
            while (true) {
                if (fileList[i].endsWith(".snapshot") || fileList[i].endsWith(".transactionLog") || fileList[i].endsWith(".xml") || fileList[i].endsWith(".journal")) {
                    addToZip(((path.equals("")) ? "" : path + "/") + folder.getName(), srcFolder + "/" + fileList[i], zipOutputStream);
                }
                i++;
            }
        } catch (Exception ex) {
        }
    }
}
