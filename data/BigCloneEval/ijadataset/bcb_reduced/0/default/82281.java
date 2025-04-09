import java.util.*;
import java.io.*;
import java.util.zip.*;

public class ZipFileWriter {

    public ZipOutputStream zout = null;

    String sourceDir = null;

    public static void main(String[] args) {
        new ZipFileWriter(args);
    }

    public ZipFileWriter(String[] args) {
        System.out.println("ZipFileWriter started\r\n");
        if (args.length < 3) return;
        try {
            FileOutputStream fout = new FileOutputStream(args[1]);
            zout = new ZipOutputStream(fout);
            sourceDir = args[2];
            zipFiles(new File(sourceDir));
            zout.close();
            System.out.println("ZipFileWriter ok\r\n");
        } catch (IOException ioe) {
            System.out.println("Exception: " + ioe + "\r\n");
        }
    }

    public void zipFiles(File f) {
        try {
            if (f.isDirectory()) {
                File[] fList = f.listFiles();
                for (int i = 0; i < fList.length; i++) {
                    zipFiles(fList[i]);
                }
            } else {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
                int len = dis.available();
                byte[] data = new byte[len];
                dis.readFully(data);
                dis.close();
                String filePath = f.getPath();
                String entryName = filePath.substring(sourceDir.length(), filePath.length());
                ZipEntry ze = new ZipEntry(entryName);
                zout.putNextEntry(ze);
                zout.write(data, 0, len);
                zout.closeEntry();
            }
        } catch (IOException ioe) {
            System.out.println("Exception: " + ioe + "\r\n");
        }
    }
}
