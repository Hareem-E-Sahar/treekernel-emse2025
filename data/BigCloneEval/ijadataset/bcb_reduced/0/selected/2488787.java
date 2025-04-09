package lopdsoft_uploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author luis
 */
public class Zipper {

    Integer bufferSize;

    byte[] buffer;

    String outFileName;

    String directory;

    String[] fileNamesToCompress;

    public Zipper() {
        this.bufferSize = 1024;
        this.buffer = new byte[1024];
        this.outFileName = "LOPDsoftUploaderFile.zip";
    }

    public Zipper(String[] filenames, String directory) {
        this.bufferSize = 1024;
        this.buffer = new byte[1024];
        this.outFileName = "LOPDsoftUploaderFile.zip";
        this.directory = directory;
        this.fileNamesToCompress = filenames;
    }

    public Zipper(Integer bufferSize, String outFileName, String[] fileNameToCompress) {
        this.bufferSize = bufferSize;
        this.buffer = new byte[this.bufferSize];
        this.outFileName = outFileName;
        this.fileNamesToCompress = fileNameToCompress;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String[] getFileNamesToCompress() {
        return fileNamesToCompress;
    }

    public void setFileNamesToCompress(String[] fileNamesToCompress) {
        this.fileNamesToCompress = fileNamesToCompress;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public boolean compressIt() {
        try {
            File f = new File(this.outFileName);
            f.createNewFile();
            f.deleteOnExit();
            ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(this.outFileName));
            System.out.println("Creating compressed file " + this.outFileName + "...");
            for (int i = 0; i < this.fileNamesToCompress.length; i++) {
                System.out.print("\tAdding " + this.fileNamesToCompress[i] + "... ");
                FileInputStream inStream = new FileInputStream(this.directory + File.separator + this.fileNamesToCompress[i]);
                outStream.putNextEntry(new ZipEntry(this.fileNamesToCompress[i]));
                Integer auxLength;
                while ((auxLength = inStream.read(this.buffer)) > 0) {
                    outStream.write(this.buffer, 0, auxLength);
                }
                inStream.close();
                System.out.println("DONE.");
            }
            outStream.close();
        } catch (java.io.FileNotFoundException ex) {
            System.out.println("ERROR :: File NOT found!!! " + ex.getMessage());
            return false;
        } catch (IOException ex) {
            System.out.println("ERRROR :: Input/Ouput error!!! " + ex.getMessage());
            return false;
        }
        return true;
    }
}
