package omschaub.azcvsupdater.utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipUtils {

    /**
     * Makes a zip file from a given String[] of files to zip
     * @param dir - directory containing all of the files to be added to the zip
     * @param destinationdir
     * @param zipFileName
     * @return success - boolean
     */
    public boolean makeBackupToZip(String[] filenames, String destinationdir, String zipFileName) {
        boolean success = false;
        destinationdir = destinationdir + System.getProperty("file.separator");
        System.out.println(destinationdir);
        byte[] buf = new byte[1024];
        try {
            String outFilename = zipFileName;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destinationdir + outFilename));
            for (int i = 0; i < filenames.length; i++) {
                if (filenames[i] != null) {
                    FileInputStream in = new FileInputStream(filenames[i]);
                    out.putNextEntry(new ZipEntry(filenames[i].substring(filenames[i].lastIndexOf(System.getProperty("file.separator")), filenames[i].length())));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Lists all files in a ZIP
     * @param zipFile -- string of the name of the zipfile
     * @return String[] of all files in the zip
     */
    public String[] listFileInZip(String zipFile) {
        ArrayList arrayList = new ArrayList();
        try {
            ZipFile zf = new ZipFile(zipFile);
            for (Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
                String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
                arrayList.add(zipEntryName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String[]) arrayList.toArray();
    }

    /**
     * Decompresses All files from a zipFile
     * @param zipFile
     * @param outputDir
     */
    public void decompressAllfromZip(String zipFile, String outputDir) {
        try {
            if (!outputDir.endsWith(System.getProperty("file.spearator"))) {
                outputDir = outputDir + System.getProperty("file.separator");
            }
            ZipFile zf = new ZipFile(zipFile);
            ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
            for (Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
                String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
                OutputStream out = new FileOutputStream(outputDir + zipEntryName);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
