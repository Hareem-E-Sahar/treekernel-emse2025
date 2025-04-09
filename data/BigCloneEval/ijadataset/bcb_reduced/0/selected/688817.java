package maplab.io.kml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import maplab.io.IOUtil;

/**
 *
 * @author arto
 */
public class Zipper {

    private static final Logger LOG = Logger.getLogger(Zipper.class.getName());

    private static final int BUFFER_SIZE = 4096;

    private byte[] dataBuffer = new byte[BUFFER_SIZE];

    private File pathToData;

    private List<String> filesToPackage;

    private String targetFileName;

    public static void doZip(File dataFolder, List<String> filesToPackage, String targetFileName) {
        new Zipper(dataFolder, filesToPackage, targetFileName).doZip();
    }

    private Zipper(File pathToData, List<String> filesToPackage, String targetFileName) {
        this.pathToData = pathToData;
        this.filesToPackage = filesToPackage;
        this.targetFileName = targetFileName;
    }

    private void doZip() {
        LOG.log(Level.INFO, "Zipping {0} to {1}{2}{3}", new Object[] { filesToPackage, pathToData.getAbsolutePath(), File.pathSeparator, targetFileName });
        try {
            FileOutputStream dest = new FileOutputStream(targetFileName);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream outputFileStream = new ZipOutputStream(new BufferedOutputStream(checksum));
            for (String fileToPackage : filesToPackage) {
                addFiletoPackage(fileToPackage, outputFileStream);
            }
            IOUtil.closeGracefully(outputFileStream);
            LOG.log(Level.INFO, "checksum: {0}", checksum.getChecksum().getValue());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void addFiletoPackage(String fileToAdd, ZipOutputStream packageOutputStream) throws FileNotFoundException, IOException {
        LOG.log(Level.INFO, "Adding: {0} to {1}", new Object[] { fileToAdd, targetFileName });
        File f = new File(pathToData, fileToAdd);
        if (!fileToAdd.startsWith(File.pathSeparator)) {
            fileToAdd = File.pathSeparator + fileToAdd;
        }
        BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE);
        ZipEntry entry = new ZipEntry(fileToAdd);
        packageOutputStream.putNextEntry(entry);
        int count;
        while ((count = fileInputStream.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
            packageOutputStream.write(dataBuffer, 0, count);
        }
        IOUtil.closeGracefully(fileInputStream);
    }
}
