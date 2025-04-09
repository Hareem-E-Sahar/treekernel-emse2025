package netblend.slave.files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** NOT IMPLEMENTED */
public class Archiver {

    public void archive(String[] fileNames, String archiveName) {
        byte[] buffer = new byte[1024];
        try {
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(archiveName));
            for (int i = 0; i < fileNames.length; i++) {
                FileInputStream fileIn = new FileInputStream(fileNames[i]);
                zipOut.putNextEntry(new ZipEntry(fileNames[i].replace('\\', '/')));
                int length;
                while ((length = fileIn.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                zipOut.closeEntry();
                fileIn.close();
            }
            zipOut.close();
        } catch (IOException e) {
        }
    }
}
