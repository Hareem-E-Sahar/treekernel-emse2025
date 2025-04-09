package innerbus.logtrap.util;

import java.io.*;
import java.util.zip.*;

public class Compress {

    public static final int BUFFER = 1024;

    /**
     * ZIP으로 압축하기
     * @param file String
     * @throws IOException
     */
    public static void zipCompress(final String file) throws IOException {
        new Thread() {

            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(file + ".zip");
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ZipOutputStream zos = new ZipOutputStream(bos);
                    File inFile = new File(file);
                    FileInputStream fis = new FileInputStream(inFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ZipEntry zipEntry = new ZipEntry(inFile.getName());
                    zipEntry.setMethod(ZipEntry.DEFLATED);
                    zos.putNextEntry(zipEntry);
                    byte[] buffer = new byte[BUFFER];
                    int length;
                    while ((length = bis.read(buffer)) != -1) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    zos.close();
                    bis.close();
                    if (new File(file).exists()) {
                        boolean isDel = new File(file).delete();
                        if (isDel) _LOG(file + " 파일을 삭제 했습니다."); else _LOG(file + " 파일을 삭제하지 못 했습니다.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * GZIP으로 압축하기
     * @param file String
     * @throws IOException
     */
    public static void gzCompress(final String file) throws IOException {
        new Thread() {

            public void run() {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(file + ".gz");
                    GZIPOutputStream gos = new GZIPOutputStream(fos);
                    byte[] buffer = new byte[BUFFER];
                    int l;
                    while ((l = fis.read(buffer)) >= 0) {
                        gos.write(buffer, 0, l);
                    }
                    gos.close();
                    fis.close();
                    if (new File(file).exists()) {
                        boolean isDel = new File(file).delete();
                        if (isDel) _LOG(file + " 파일을 삭제 했습니다."); else _LOG(file + " 파일을 삭제하지 못 했습니다.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    public static void _LOG(String msg) {
        System.err.println(msg);
    }
}
