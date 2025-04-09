package jvc.util.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>Title :jvc�������</p>
 * <p>Description: �ļ�ѹ��, ��֧��·�����ܣ�Ҫ�����б�ѹ���ļ���Ψһ���ļ���</p>
 * <p>Created on 2004-3-4</p>
 * <p>Company :jvc</p>
 *  @author : rufujian
 *  @version : 1.0
 */
public class ZipUtils {

    private Collection zipFileList = new ArrayList();

    /**
	   * ������
	   */
    public ZipUtils() {
    }

    /**
	   * ���ѹ���ļ�
	   * @param fileName ��ѹ���ļ���ȫ·����
	   */
    public void addFile(String fileName) {
        File file = new File(fileName);
        this.zipFileList.add(file);
    }

    /**
	   * ���ѹ���ļ�
	   * @param file ��ѹ���ļ�
	   */
    public void addFile(File file) {
        this.zipFileList.add(file);
    }

    /**
	   * ѹ���ļ�
	   * @param out ��ѹ���������д���������
	   * @throws java.io.IOException ��IO����ʱ������
	   */
    public void zip(OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        zipOut.setMethod(ZipOutputStream.DEFLATED);
        for (Iterator it = this.zipFileList.iterator(); it.hasNext(); ) {
            File file = (File) it.next();
            byte[] rgb = new byte[1000];
            int n;
            CRC32 crc32 = new CRC32();
            FileInputStream fileIn = new FileInputStream(file);
            while ((n = fileIn.read(rgb)) > -1) {
                crc32.update(rgb, 0, n);
            }
            fileIn.close();
            ZipEntry zipentry = new ZipEntry(file.getName());
            zipentry.setMethod(ZipEntry.STORED);
            zipentry.setSize(file.length());
            zipentry.setTime(file.lastModified());
            zipentry.setCrc(crc32.getValue());
            zipOut.putNextEntry(zipentry);
            fileIn = new FileInputStream(file);
            while ((n = fileIn.read(rgb)) > -1) {
                zipOut.write(rgb, 0, n);
            }
            fileIn.close();
            zipOut.closeEntry();
        }
        zipOut.close();
    }

    /**
	   * ѹ���ļ����ļ�
	   * @param file ѹ���ļ�
	   * @throws java.io.IOException ��IO����ʱ������
	   */
    public void zip(File file) throws IOException {
        zip(new FileOutputStream(file));
    }

    public static void main(String[] args) {
        try {
            ZipUtils zip = new ZipUtils();
            zip.addFile(new File("D:/aa11.jpg"));
            File outFile = new File("D:/test.zip");
            zip.zip(outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
