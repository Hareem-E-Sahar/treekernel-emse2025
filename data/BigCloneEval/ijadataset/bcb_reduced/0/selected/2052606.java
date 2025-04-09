package action;

import java.io.*;
import java.util.zip.*;

public class DirectoryArchivageAction extends Action {

    /*************************************************************************/
    public DirectoryArchivageAction(String receivedMessage) {
        int inputIndex = receivedMessage.indexOf(";", 0);
        int outputIndex = receivedMessage.indexOf(";", inputIndex + 1);
        this.m_input = receivedMessage.substring(inputIndex + 1, outputIndex);
        this.m_output = receivedMessage.substring(outputIndex + 1);
    }

    /*************************************************************************/
    public void run() {
        File inputDirectory = new File(this.m_input);
        File outputDirectory = new File(this.m_output);
        System.out.println(">> Directory archivage starts");
        compress(inputDirectory, outputDirectory);
        System.out.println(">> Directory archivage succeeds");
    }

    private synchronized void compress(File inputDirectory, File outputZipFile) {
        final int BUFFER_SIZE = 5 * 1024;
        long handledSize = 0;
        long totalSize = getTotalSize(inputDirectory);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputZipFile.getPath());
            ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
            BufferedInputStream bufferedInputStream = null;
            for (File inputFile : inputDirectory.listFiles()) {
                if (inputFile.isDirectory()) throw new IOException("Un des fichiers de l'archive est un r�pertoire, et l'archivage ne g�re que les fichiers."); else {
                    ZipEntry zipEntry = new ZipEntry(inputFile.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    FileInputStream fileInputStream = new FileInputStream(inputFile);
                    bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER_SIZE);
                    byte buffer[] = new byte[BUFFER_SIZE];
                    int dataCount = 0;
                    while (true) {
                        dataCount = bufferedInputStream.read(buffer, 0, BUFFER_SIZE);
                        if (dataCount == -1) break; else {
                            zipOutputStream.write(buffer, 0, dataCount);
                            handledSize += dataCount;
                            this.dispatchMessageToClient("ArchivagePercent", String.valueOf(100 * handledSize / totalSize));
                        }
                    }
                }
            }
            bufferedInputStream.close();
            zipOutputStream.close();
        } catch (Exception e) {
            this.dispatchMessageToClient("Erreur", e.getMessage());
        }
    }

    /*************************************************************************/
    private long getTotalSize(File directory) {
        long size = 0;
        for (File file : directory.listFiles()) {
            size += file.length();
        }
        return size;
    }
}
