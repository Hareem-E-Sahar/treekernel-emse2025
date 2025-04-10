package cn.jsprun.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    static final int BUFFER = 2048;

    public static void zipFile(File fileToZip, ZipOutputStream zos) throws IOException {
        if (fileToZip.isFile()) {
            String name = fileToZip.getName();
            zos.putNextEntry(new ZipEntry(name));
            FileInputStream fis = new FileInputStream(fileToZip);
            int c;
            while ((c = fis.read()) != -1) {
                zos.write(c);
            }
            fis.close();
            zos.closeEntry();
        }
    }

    public static List<String> unZipFile(File zipFile) throws IOException {
        List<String> fileNames = new ArrayList<String>();
        if (!zipFile.exists()) {
            throw new IOException("ϵͳ�Ҳ���ָ����ѹ���ļ���");
        }
        String path = zipFile.getPath();
        int index = path.lastIndexOf("\\");
        String folderName = null;
        if (index != -1) {
            folderName = path.substring(0, index);
        } else {
            folderName = "";
        }
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(zipFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            fileNames.add(entry.getName());
            int count;
            byte data[] = new byte[BUFFER];
            FileOutputStream fos = new FileOutputStream(folderName + "\\" + entry.getName());
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
        bis.close();
        fis.close();
        return fileNames.size() > 0 ? fileNames : null;
    }
}
