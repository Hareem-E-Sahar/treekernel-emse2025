package MyCommon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** This class can be used to zip or unzip folders.
 *
 * Note there are a few quirks in zip files:
 *     Zip files store the modification time with a resolution of 2 seconds.
 *     Zip files store the time in hours, minutes, etc. so no time zone is saved.
 *
 * To correct for timezone problems, you can set the timezone that the zip file times should be interpreted with.  If you do not set this all times are interpreted in your local time.
 *
 * @author Brandon Drake
 */
public class Zipper {

    protected TimeZone timeZone = null;

    /** Converts the time to the time to use in the zip entry.
     */
    public long convertToZipTime(long l_lastModified) {
        if (null == timeZone) {
            return l_lastModified;
        }
        Date lastModified = new Date();
        lastModified.setTime(l_lastModified);
        Calendar oldModTime = Calendar.getInstance(timeZone);
        oldModTime.setTime(lastModified);
        Calendar newModTime = Calendar.getInstance();
        newModTime.clear();
        newModTime.set(oldModTime.get(Calendar.YEAR), oldModTime.get(Calendar.MONTH), oldModTime.get(Calendar.DATE), oldModTime.get(Calendar.HOUR_OF_DAY), oldModTime.get(Calendar.MINUTE), oldModTime.get(Calendar.SECOND));
        return newModTime.getTime().getTime();
    }

    /** Converts the time in the zip file to local time.
     */
    public long convertFromZipTime(long l_lastModified) {
        if (null == timeZone) {
            return l_lastModified;
        }
        Date lastModified = new Date();
        lastModified.setTime(l_lastModified);
        Calendar oldModTime = Calendar.getInstance();
        oldModTime.setTime(lastModified);
        Calendar newModTime = Calendar.getInstance(timeZone);
        newModTime.clear();
        newModTime.set(oldModTime.get(Calendar.YEAR), oldModTime.get(Calendar.MONTH), oldModTime.get(Calendar.DATE), oldModTime.get(Calendar.HOUR_OF_DAY), oldModTime.get(Calendar.MINUTE), oldModTime.get(Calendar.SECOND));
        return newModTime.getTime().getTime();
    }

    /** Unzips the archive to the specified output directory.
     */
    public void unzipArchive(File archive, File outputDir) throws IOException {
        ZipFile zipfile = new ZipFile(archive);
        for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            unzipEntry(zipfile, entry, outputDir);
        }
    }

    /** Unzips the entry from the zip file.
     */
    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {
        if (entry.getName().startsWith("__MACOSX/")) {
            return;
        }
        if (entry.isDirectory()) {
            File dir = new File(outputDir, entry.getName());
            createDir(dir);
            dir.setLastModified(entry.getTime());
            return;
        }
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
        outputFile.setLastModified(this.convertFromZipTime(entry.getTime()));
    }

    /** Creates a directory.
     */
    private void createDir(File dir) {
        if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
    }

    /** Copies the input stream to the output stream.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /** Gets the timezone used for the zip file.  Times will be converted to this time zone when copied to the zip file.
     */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /** Adds a folder to the archive.
     */
    public void addFolderToArchive(ZipOutputStream l_zipStream, File l_folder, File l_baseFolder) throws IOException {
        File[] files = l_folder.listFiles();
        for (File file : files) {
            String zipPath = file.getCanonicalPath().substring(l_baseFolder.getParentFile().getCanonicalPath().length() + 1);
            if (file.isDirectory()) {
                ZipEntry zipEntry = new ZipEntry(zipPath + "/");
                zipEntry.setTime(this.convertToZipTime(file.lastModified()));
                l_zipStream.putNextEntry(zipEntry);
                this.addFolderToArchive(l_zipStream, file, l_baseFolder);
            } else {
                ZipEntry zipEntry = new ZipEntry(zipPath);
                zipEntry.setTime(this.convertToZipTime(file.lastModified()));
                l_zipStream.putNextEntry(zipEntry);
                InputStream inStream = new FileInputStream(file);
                this.copy(inStream, l_zipStream);
                inStream.close();
                l_zipStream.closeEntry();
            }
        }
    }

    /** Sets the timezone used for the zip file.  Times will be converted to this time zone when copied to the zip file.
     */
    public void setTimeZone(TimeZone l_value) {
        this.timeZone = l_value;
    }

    /** Zips the folder to the output zip file.
     */
    public void zipArchive(File archive, File outputDir) throws IOException {
        ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(archive));
        this.addFolderToArchive(outStream, outputDir, outputDir);
        outStream.close();
    }
}
