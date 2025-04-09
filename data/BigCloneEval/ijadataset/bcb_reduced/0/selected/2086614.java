package remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ZipUtil
 */
public class ZipUtil {

    List<String> fileList;

    String OUTPUT_ZIP_FILE;

    String SOURCE_FOLDER;

    ZipUtil() {
        fileList = new ArrayList<String>();
    }

    public void zip(String sourcefolder, String outputfile) {
        SOURCE_FOLDER = sourcefolder;
        OUTPUT_ZIP_FILE = outputfile;
        generateFileList(new File(SOURCE_FOLDER));
        zipIt(OUTPUT_ZIP_FILE);
    }

    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile) {
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (String file : this.fileList) {
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                if (!ze.isDirectory()) {
                    FileInputStream in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    in.close();
                }
            }
            zos.closeEntry();
            zos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     * @param node file or directory
     */
    public void generateFileList(File node) {
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }
        if (node.isDirectory()) {
            if (!node.getAbsoluteFile().toString().equals(new String(SOURCE_FOLDER))) {
                fileList.add(generateZipEntry(node.getAbsoluteFile().toString()) + "/");
            }
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file) {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }

    public void extract(String filename, String destinationdir) throws Exception {
        File dFile = new File(destinationdir);
        if (!dFile.exists()) {
            dFile.mkdirs();
        }
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
        zipinputstream = new ZipInputStream(new FileInputStream(filename));
        zipentry = zipinputstream.getNextEntry();
        while (zipentry != null) {
            String entryName = zipentry.getName();
            if (zipentry.isDirectory()) {
                File newDir = new File(destinationdir + entryName);
                if (!newDir.exists()) {
                    newDir.mkdir();
                }
            } else {
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                fileoutputstream = new FileOutputStream(destinationdir + entryName);
                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }
                fileoutputstream.close();
            }
            zipinputstream.closeEntry();
            zipentry = zipinputstream.getNextEntry();
        }
        zipinputstream.close();
    }
}
