package test.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTest {

    private static final String sourceDir = "D:\\users\\sachingm\\OUT\\test1";

    private static final String destFile = "D:\\users\\sachingm\\OUT\\test.zip";

    private static final String destDir = "D:\\users\\sachingm\\OUT\\testunzip";

    private static final int BUFFER = 1024;

    private static final String SEPERATOR = "\n";

    public static void main(String[] args) {
        try {
            zip(sourceDir, new FileOutputStream(destFile));
            unzip(new FileInputStream(destFile), destDir);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void unzip(InputStream in, String destDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
        ZipEntry entry;
        BufferedOutputStream dest = null;
        while ((entry = zis.getNextEntry()) != null) {
            System.out.println("Extracting: " + entry);
            int count;
            byte data[] = new byte[BUFFER];
            if (entry.getName().endsWith("/")) {
                File file = new File(destDirectory + entry.getName());
                file.mkdir();
            } else {
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(destDirectory + entry.getName());
                } catch (FileNotFoundException e) {
                    File parent = new File(destDirectory + entry.getName()).getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    fos = new FileOutputStream(destDirectory + entry.getName());
                }
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
        System.out.println("Extraction complete.");
    }

    private static void zip(String sourceDirectory, OutputStream dest) throws FileNotFoundException, IOException {
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        StringBuffer fileList = new StringBuffer();
        getFileList(sourceDirectory, fileList);
        String[] files = fileList.toString().split("\n");
        System.out.println("Filecount:" + (files.length - 1));
        byte[] tmpBuf = new byte[BUFFER];
        for (int i = 1; i < files.length; i++) {
            ZipEntry entry = new ZipEntry(files[i].substring(new File(sourceDirectory).getAbsolutePath().length()));
            out.putNextEntry(entry);
            if (new File(files[i]).isFile()) {
                FileInputStream in = new FileInputStream(files[i]);
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    out.write(tmpBuf, 0, len);
                }
                in.close();
            }
            out.closeEntry();
        }
        out.close();
        System.out.println("Compression complete.");
    }

    /**
	 * All files and empty directories under path, are recursively added into list.<BR/>
	 * This list is intended to be used for compression (with java.util.zip)<BR/>
	 * Note: The list starts with the seperator.
	 * @param path
	 * @param list
	 */
    private static void getFileList(String path, StringBuffer list) {
        File current = new File(path);
        if (current.isDirectory()) {
            File[] files = current.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.list().length == 0) {
                        list.append(SEPERATOR).append(file.getAbsolutePath() + "/");
                    } else {
                        getFileList(file.getAbsolutePath(), list);
                    }
                } else {
                    list.append(SEPERATOR).append(file.getAbsolutePath());
                }
            }
        }
    }
}
