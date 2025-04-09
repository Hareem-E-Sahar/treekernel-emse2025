package xlion.maildisk.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import xlion.maildisk.file.ZipFileFilter;

public class ZipFileUtil {

    /**
	 * Logger for this class
	 */
    private static final Logger logger = Logger.getLogger(ZipFileUtil.class);

    public static void zipFile(String srcFile, String desFile) throws IOException {
        OutputStream outStream = new FileOutputStream(desFile);
        zipFile(srcFile, outStream);
        outStream.close();
    }

    public static void zipFile(String srcFile, OutputStream outStream) throws IOException {
        BufferedOutputStream bs = new BufferedOutputStream(outStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bs);
        zipFile("", new File(srcFile), zipOutputStream, new ZipFileFilter());
        zipOutputStream.close();
        bs.close();
    }

    private static void zipFile(String folder, File file, ZipOutputStream zipOutputStream, FileFilter filter) throws IOException {
        if (file.isFile()) {
            if (logger.isDebugEnabled()) {
                logger.debug("zipFile(File, ZipOutputStream, FileFilter) - handel file=" + file);
            }
            FileInputStream in = new FileInputStream(file.getPath());
            zipOutputStream.putNextEntry(new ZipEntry(folder + file.getName()));
            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, len);
            }
            zipOutputStream.closeEntry();
            in.close();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("zipFile(File, ZipOutputStream, FileFilter) - handel folder=" + file);
            }
            zipOutputStream.putNextEntry(new ZipEntry(folder + file.getName() + "/"));
            zipOutputStream.closeEntry();
            for (File subFile : file.listFiles(filter)) {
                zipFile(folder + file.getName() + "/", subFile, zipOutputStream, filter);
            }
        }
    }

    public static void unzipFile(String zipFile, String desFolder) throws IOException {
        unzipFile(new FileInputStream(zipFile), desFolder);
    }

    public static void unzipFile(InputStream inputStream, String desFolder) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) {
                new File(desFolder + "/" + zipEntry.getName()).mkdirs();
            } else {
                int index = zipEntry.getName().replace('\\', '/').indexOf("/");
                if (index != -1) {
                    new File(zipEntry.getName().substring(0, index)).mkdirs();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(desFolder + "/" + zipEntry.getName());
                byte[] bs = new byte[1024];
                int len = 0;
                while ((len = zipInputStream.read(bs)) > 0) {
                    fileOutputStream.write(bs, 0, len);
                }
                fileOutputStream.close();
            }
            zipInputStream.closeEntry();
        }
        zipInputStream.close();
    }
}
