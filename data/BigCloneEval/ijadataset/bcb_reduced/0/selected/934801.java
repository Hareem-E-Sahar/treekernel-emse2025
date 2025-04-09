package net.sourceforge.jbackupfw.core.util;

import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sourceforge.jbackupfw.core.data.BackUpInfoFileGroup;
import net.sourceforge.jbackupfw.core.data.BackupException;

/**
 * This class provides the basic system operation of serialization to the file the information
 * about all the files that were becakd up and putting that file in the backed up archive with
 * the files so that it can later be extracted with the files
 *
 * @author Boris Horvat and Dusan Guduric
 */
public class ExportData {

    /** this is constant that controls the size of the buffer */
    private static final byte[] BUFFER = new byte[2156];

    /**
     * This method provides the basic serialization to the file and putting
     * that file in the backed up archive in order to keep the information 
     * about the file that were backed up with the files themselves
     *
     * @param fileGroup - the objcet witch hold the information about the backed up files
     * @param zos - the stream that is used to write the serialize file into the archive
     * @param name - name of the file that holds the information
     *
     * @throws BackupException if IO error occures
     */
    public void execute(BackUpInfoFileGroup fileGroup, ZipOutputStream zos, String name) {
        int bytesIn = 0;
        try {
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
            out.writeObject(fileGroup);
            out.close();
            FileInputStream fis = new FileInputStream(new File(name));
            ZipEntry anEntry = new ZipEntry(name);
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(BUFFER)) != -1) {
                zos.write(BUFFER, 0, bytesIn);
            }
            fis.close();
            new File(name).delete();
        } catch (IOException e) {
            throw new BackupException(e.getMessage());
        }
    }
}
