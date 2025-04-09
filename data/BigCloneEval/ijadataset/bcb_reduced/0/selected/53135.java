package forteresce.portprofile.profiles.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import forteresce.portprofile.config.enums.SystemPropertiesEnum;

public class ZipUtil {

    private static Logger log = Logger.getLogger(ZipUtil.class);

    static final int BUFFER = 1024;

    /**
	 * Creates a zip file with a given source directory or a file
	 * @param src - source directory or file to be compresses
	 * @param dest - destination file name only
	 */
    public static boolean zip(String src, String dest) {
        try {
            File srcFile = new File(src);
            String root = srcFile.getParent();
            FileOutputStream destFOS = new FileOutputStream((dest.indexOf(SystemPropertiesEnum.FILE_SEPARATOR.get()) != -1) ? dest : root + SystemPropertiesEnum.FILE_SEPARATOR.get() + dest);
            CheckedOutputStream checksum = new CheckedOutputStream(destFOS, new Adler32());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
            if (srcFile.isDirectory()) {
                addFilesInDirToZip(root, srcFile.getAbsolutePath(), out);
            } else {
                addFileToZip(root, srcFile, out);
            }
            out.close();
            return true;
        } catch (Exception e) {
            log.error("Error zipping files.", e);
        }
        return false;
    }

    /**
	 * Unzips a zip file from a given source zip
	 * @param src - source file to be uncompressed
	 * @param dest - destination folder
	 */
    public static boolean unzip(String src, String dest) {
        try {
            BufferedOutputStream destBOS = null;
            FileInputStream fis = new FileInputStream(src);
            CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                String entryFileName = dest + entry.getName();
                int indexOfSeparator = entryFileName.lastIndexOf(SystemPropertiesEnum.FILE_SEPARATOR.get());
                if (indexOfSeparator > 1) {
                    (new File(entryFileName.substring(0, indexOfSeparator))).mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(entryFileName);
                destBOS = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    destBOS.write(data, 0, count);
                }
                destBOS.flush();
                destBOS.close();
            }
            zis.close();
            return true;
        } catch (Exception e) {
            log.error("Error unzipping files.", e);
        }
        return false;
    }

    public static String getParentFolder(String src) {
        ZipInputStream zis = null;
        try {
            CheckedInputStream checksum = new CheckedInputStream(new FileInputStream(src), new Adler32());
            zis = new ZipInputStream(checksum);
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                String fileEntryName = entry.getName();
                if (fileEntryName.indexOf(SystemPropertiesEnum.FILE_SEPARATOR.get(), 1) != -1) {
                    return fileEntryName.substring(1, fileEntryName.indexOf(SystemPropertiesEnum.FILE_SEPARATOR.get(), 1));
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Error finding parent folder in zip file: " + src, e);
        } catch (IOException e) {
            log.error("Error finding parent folder in zip file: " + src, e);
        } finally {
            try {
                if (null != zis) {
                    zis.close();
                }
            } catch (IOException e) {
                log.error("Error closing zip file: " + src, e);
            }
        }
        return null;
    }

    private static void addFilesInDirToZip(String base, String src, ZipOutputStream out) throws FileNotFoundException, IOException {
        File srcDir = new File(src);
        File files[] = srcDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addFilesInDirToZip(base, files[i].getAbsolutePath(), out);
            } else {
                addFileToZip(base, files[i], out);
            }
        }
    }

    private static void addFileToZip(String base, File file, ZipOutputStream out) throws FileNotFoundException, IOException {
        byte[] data = new byte[BUFFER];
        BufferedInputStream srcBIS = new BufferedInputStream(new FileInputStream(file), BUFFER);
        ZipEntry entry = new ZipEntry(file.getAbsolutePath().replace(base, ""));
        out.putNextEntry(entry);
        int count;
        while ((count = srcBIS.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
        }
        srcBIS.close();
    }
}
