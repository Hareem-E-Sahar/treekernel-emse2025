package core;

import data.Data;
import display.Display;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFrame;
import ui.ProgressMonitorDialog;

/**
 * This class provides a simple way to copy a file that exists.
 *
 * @author Brian Gibowski brian@brgib.com
 */
public class CopyFile {

    /**
     * The character used to seperate files and folders.  System dependent.
     */
    public static final String FILE_SEPERATOR = System.getProperty("file.separator");

    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    private Logger log = Logger.getLogger("global");

    private static int BUFFER_SIZE = 1024;

    private File originalFile;

    /**
     * Constructs a new CopyFile object with a known file to copy.
     * 
     * @param file The file that is to be copied elsewhere.
     */
    public CopyFile(File file) {
        setOriginalFile(file);
    }

    /**
     * Constructs a new CopyFile object without a file to copy.  A file
     * must be set before other methods can be called.
     */
    public CopyFile() {
    }

    /**
     * Returns the original file to copy
     * @return the original file to copy
     */
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * Sets the file to copied in the CopyFile object
     * @param originalFile the file to copy
     */
    public void setOriginalFile(File originalFile) {
        if (!originalFile.exists()) {
            IllegalArgumentException e = new IllegalArgumentException("File " + originalFile.toString() + " does not exist");
            log.log(Level.SEVERE, this.getClass().getName(), e);
            throw e;
        }
        if (originalFile == null) {
            NullPointerException e = new NullPointerException("File is null");
            log.log(Level.SEVERE, this.getClass().getName(), e);
            throw e;
        }
        if (originalFile.isDirectory()) {
            IllegalArgumentException e = new IllegalArgumentException("File can not be a directory");
            log.log(Level.SEVERE, this.getClass().getName(), e);
            throw e;
        }
        this.originalFile = originalFile;
    }

    /**
     * Creates a new copy of the original file without overwriting the file.
     * 
     * @param showProgress If true, a ProgressMonitorDialog box will appear showing the
     * copy progress.
     *
     * @return The copied file
     */
    public File copyFile(boolean showProgress) {
        return doWrite(getNewFile(originalFile), showProgress);
    }

    /**
     * Copies the original file to a new file, if the file exists in the directory
     * it will be given a name to prevent overwriting it.  If the file does not
     * exist in the destingation directory it will be given the same name as the original
     * file.
     * @param outputFile the file to output to.  If the file exists in that directory
     * it will be given a name that is unique.
     *
     * @param showProgress If true, a ProgressMonitorDialog box will appear showing the
     * copy progress.
     *
     * @return The copied file.  Null if there was a problem writing the file.
     */
    public File write(File outputFile, boolean showProgress) {
        if (originalFile == null) {
            NullPointerException e = new NullPointerException("File to write is null");
            log.log(Level.SEVERE, this.getClass().getName(), e);
            throw e;
        }
        if (outputFile == null) {
            NullPointerException e = new NullPointerException("Output file can not be null");
            log.log(Level.SEVERE, this.getClass().getName(), e);
            throw e;
        }
        if (outputFile.isDirectory()) {
            IllegalArgumentException e = new IllegalArgumentException("The output file can not be a directory");
            log.log(Level.SEVERE, this.getClass().getName(), e);
            throw e;
        }
        return doWrite(getNewFile(outputFile), showProgress);
    }

    /**
     * Does the copy file heavy lifting.
     *
     * @param outputFile The destination file.
     * @param showProgress If true, a ProgressMonitorDialog box is created
     * showing the progress of the file copy.
     *
     * @return The copied file.
     */
    private File doWrite(File outputFile, boolean showProgress) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        OutputStream out = null;
        InputStream in = null;
        ProgressMonitorDialog pmd = null;
        try {
            out = new FileOutputStream(outputFile);
            if (!checkFileDestinationSpace(outputFile, originalFile)) {
                throw new IOException("The destination drive does not contain enough space.");
            }
            if (showProgress) {
                pmd = new ProgressMonitorDialog(new JFrame(), "Copying File...", true);
                pmd.setText("Copying " + originalFile.getName());
            }
            in = new BufferedInputStream(new FileInputStream(originalFile));
            long bufferCount = calcBufferCount();
            long counter = 0;
            while ((length = in.read(buffer)) > 0) {
                counter++;
                if (pmd != null) {
                    pmd.setProgress(counter, bufferCount);
                }
                if (pmd != null && pmd.isCanceled()) {
                    out.close();
                    outputFile.delete();
                    break;
                }
                out.write(buffer, 0, length);
            }
            checkCopiedFile(outputFile);
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            log.log(Level.SEVERE, this.getClass().getName(), e);
            return null;
        } catch (IOException e) {
            log.log(Level.SEVERE, this.getClass().getName(), e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, this.getClass().getName(), e);
            }
        }
        return outputFile;
    }

    /**
     * Returns a file that is unique.  For example if file test1.txt exists,
     * getNewFile will iterate until it finds a unique file name that will
     * read Copy(i) filename.file extensions
     * @param file the file to check if it already exists
     * @return a file name that is unique that will not overwrite any other files
     * in the directory
     */
    private File getNewFile(File file) {
        String filePath = file.getParent();
        String fileName = file.getName();
        File newFile = new File(filePath + FILE_SEPERATOR + fileName);
        int copyIteration = 1;
        while (newFile.exists()) {
            fileName = "Copy(" + copyIteration + ") " + file.getName();
            newFile = new File(filePath + FILE_SEPERATOR + fileName);
            copyIteration++;
        }
        log.log(Level.INFO, this.getClass().getName() + "\nNew File Path:  " + newFile.getPath());
        return newFile;
    }

    /**
     * Checks that the outputfile has enough space on the destination volume to
     * copy the file to.
     *
     * @param fileToCopy The file that is to be copied.
     *
     * @param destination The destination to check if enough space is available.
     *

     *
     * @return If true, the volume has enough free space to write the file.  If
     * false the volume is out of space to write the file to.
     */
    private boolean checkFileDestinationSpace(File destination, File fileToCopy) {
        if (destination.getUsableSpace() < fileToCopy.length()) {
            log.log(Level.WARNING, this.getClass().getName() + "Destination drive does not have enough space" + " to copy the file.");
            Display.newWarningMessage("Hard Drive Space", "There is not enough free space to copy:\n" + fileToCopy.getPath() + "\nto\n" + destination.getPath());
            return false;
        } else {
            return true;
        }
    }

    private void checkCopiedFile(File outputFile) {
        try {
            if (outputFile.exists() && outputFile.length() != originalFile.length()) {
                Display.newWarningMessage("Error Copying File", "There was an error copying the file " + originalFile.getPath());
                IOException e = new IOException("Error copying file " + originalFile.getPath() + "\nto " + outputFile.getPath());
                log.log(Level.SEVERE, this.getClass().getName(), e);
                throw e;
            } else {
                log.log(Level.INFO, this.getClass().getName() + "\nSuccessfully copied file:  " + originalFile.getPath() + "\n" + "to destination file:  " + outputFile.getPath());
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, this.getClass().getName(), e);
        }
    }

    /**
     * Counts how many buffer reads are necessary to copy the file.
     *
     * @return bufferCount The number of reads necessary to copy the original file.
     */
    private long calcBufferCount() {
        long bufferCount = originalFile.length() / BUFFER_SIZE;
        if (((double) originalFile.length() / BUFFER_SIZE - bufferCount) > 0) {
            bufferCount++;
        }
        return bufferCount;
    }

    /**
     * Creates a zip to the Data folder containing the given files with a name
     * including the current time in milliseconds.
     *
     * @param filesToZip The array list of files to include in a backup zip file
     * in the user directory.
     */
    public void createZIPFileBackup(ArrayList<File> filesToZip) {
        try {
            byte[] buffer = new byte[1024];
            File zipFile = getNewFile(new File(Data.DATA_FOLDER_PATH + System.currentTimeMillis() + "added_files_backup.zip"));
            FileOutputStream fout = new FileOutputStream(zipFile);
            ZipOutputStream zout = new ZipOutputStream(fout);
            for (File a : filesToZip) {
                log.log(Level.INFO, this.getClass().getName(), "Added file " + a.getAbsolutePath() + " to zip file " + zipFile.getAbsolutePath());
                FileInputStream fin = new FileInputStream(a);
                zout.putNextEntry(new ZipEntry(a.getName()));
                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }
                zout.closeEntry();
                fin.close();
            }
            zout.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, this.getClass().getName(), e);
        }
    }

    public static void main(String[] args) {
        File a = new File(Data.DATA_FOLDER_PATH + "added_tracks.txt");
        File b = new File(Data.DATA_FOLDER_PATH + "exclusions.dat");
        ArrayList<File> filesToZip = new ArrayList<File>();
        filesToZip.add(a);
        filesToZip.add(b);
        CopyFile cf = new CopyFile();
        cf.createZIPFileBackup(filesToZip);
    }
}
