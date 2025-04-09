package cn.myapps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    /**
	 * 
	 * @param newFileName
	 *            ѹ���ļ����
	 * @param inputFileName
	 *            Ҫѹ���ļ���·��
	 * @param destDir
	 *            ѹ���ļ����Ŀ¼
	 * @throws Exception
	 */
    public static void compressFiles(String newFileName, String inputFilePath, String destDir) throws Exception {
        compressFiles(newFileName, new String[] { inputFilePath }, destDir);
    }

    public static void compressFiles(String newFileName, String[] inputFilePaths, String destDir) throws Exception {
        String zipPathName = destDir + "/" + newFileName + ".zip";
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPathName));
        for (int i = 0; i < inputFilePaths.length; i++) {
            File inputFile = new File(inputFilePaths[i]);
            if (inputFile.exists() && inputFile.isFile()) {
                FileInputStream in = new FileInputStream(inputFile);
                zipOut.putNextEntry(new ZipEntry(inputFile.getName()));
                int nNumber;
                byte[] buffer = new byte[512];
                while ((nNumber = in.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, nNumber);
                }
                in.close();
            }
        }
        zipOut.close();
    }

    public static void decompress(String inputFileName) throws Exception {
        decompress(new File(inputFileName), "");
    }

    /**
	 * ��ѹzip�ļ���ָ��Ŀ¼
	 * 
	 * @param infile
	 *            Ҫ��ѹ���ļ�
	 * @param destDir
	 *            Ŀ��Ŀ¼
	 * @throws Exception
	 */
    public static void decompress(File infile, String destDir) throws Exception {
        ZipFile zip = new ZipFile(infile);
        if (zip != null) {
            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String fileName = entry.getName();
                InputStream in = zip.getInputStream(entry);
                File dir = new File(destDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String fullPath = destDir + "/" + fileName;
                FileOutputStream out = new FileOutputStream(fullPath);
                int nNumber;
                byte[] buffer = new byte[512];
                while ((nNumber = in.read(buffer)) != -1) {
                    out.write(buffer, 0, nNumber);
                }
                in.close();
                out.close();
            }
        }
    }

    /**
	 * �����չ���ȡzip�е��ļ�
	 * 
	 * @param extension
	 * @throws IOException
	 */
    public static File getFileByExtension(ZipFile zipFile, String extension) throws IOException {
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().indexOf("." + extension) != -1) {
                InputStream in = zipFile.getInputStream(entry);
                File rtn = new File(entry.getName());
                FileOutputStream out = new FileOutputStream(rtn);
                int nNumber;
                byte[] buffer = new byte[512];
                while ((nNumber = in.read(buffer)) != -1) {
                    out.write(buffer, 0, nNumber);
                }
                in.close();
                out.close();
                return rtn;
            }
        }
        return null;
    }

    public static void main(String[] args) {
    }
}
