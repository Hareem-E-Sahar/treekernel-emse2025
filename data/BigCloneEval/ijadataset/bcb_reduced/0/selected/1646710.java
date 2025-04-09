package com.germinus.xpression.cms.directory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.germinus.xpression.cms.web.TemporaryFilesHandler;

public class ZipUtils {

    public static final String JCLIC_FILE_EXTENSION = ".jclic";

    public static final String ZIP_FILE_EXTENSION = ".zip";

    private static final int DATA_BLOCK_SIZE = 1024;

    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(ZipUtils.class);

    public static FileInputStream getFileInputStreamFromZipSource(ZipInputStream source) throws IOException {
        String tempFileName = "/tmp/tempunzip";
        FileOutputStream fos = new FileOutputStream(tempFileName);
        BufferedOutputStream targetStream = new BufferedOutputStream(fos, DATA_BLOCK_SIZE);
        int byteCount;
        byte[] data = new byte[DATA_BLOCK_SIZE];
        while ((byteCount = source.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
            targetStream.write(data, 0, byteCount);
        }
        targetStream.flush();
        targetStream.close();
        return new FileInputStream(new File(tempFileName));
    }

    /**
     * Adds files and directories from a directory to a zip file. Directory
     * passed is not included in zip file.
     * @throws IOException
     *
     */
    public static void zipDirectoryFiles(String sourceDirectory, ZipOutputStream zos) throws IOException {
        File dir = new File(sourceDirectory);
        TemporaryFilesHandler.register(null, dir);
        String[] fileNames = dir.list();
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = new File(sourceDirectory + "/" + fileName);
            TemporaryFilesHandler.register(null, file);
            recurseFiles(file, sourceDirectory, zos);
        }
    }

    /**
     * Recurses down a directory and its subdirectories to look for
     * files to add to the Zip. If the current file being looked at
     * is not a directory, the method adds it to the Zip file.
     */
    private static void recurseFiles(File file, String baseDirectory, ZipOutputStream zos) throws IOException, FileNotFoundException {
        if (file.isDirectory()) {
            String[] fileNames = file.list();
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    recurseFiles(new File(file, fileNames[i]), baseDirectory, zos);
                }
            }
        } else {
            byte[] buf = new byte[1024];
            int len;
            String fileRelativePath = file.getPath().substring(baseDirectory.length() + 1);
            ZipEntry zipEntry = new ZipEntry(fileRelativePath);
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fin);
            zos.putNextEntry(zipEntry);
            while ((len = in.read(buf)) >= 0) {
                zos.write(buf, 0, len);
            }
            in.close();
            zos.closeEntry();
        }
    }

    public static boolean isZipContentType(String mimeType) {
        return true;
    }
}
