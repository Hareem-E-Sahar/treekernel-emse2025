package org.dueam.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private final int BUFSIZE = 4096;

    /**
	 * @param args
	 * @throws Exception 
	 */
    public static void main(String[] args) throws Exception {
        @SuppressWarnings("unused") ZipUtils zu = new ZipUtils();
        File f = new File("E:\\2\\4\\5\\6.txt");
        (new File(f.getParent())).mkdirs();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write("dd".getBytes());
    }

    /**
	 * ��ѹZIP 
	 * @param zipfile zip �ļ�·��
	 * @param extdir �ͷ�Ŀ¼ null ΪzipĿ¼
	 */
    public void unzipFile(String zipfile, String extdir) {
        File f = new File(zipfile);
        if (null == extdir) extdir = f.getParent();
        File ext = new File(extdir);
        if (!ext.exists()) ext.mkdirs();
        try {
            FileInputStream fi = new FileInputStream(f);
            ZipInputStream zipInput = new ZipInputStream(fi);
            ZipEntry zip = zipInput.getNextEntry();
            while (zip != null) {
                File fo = new File(extdir + zip.getName());
                this.makedirs(fo);
                FileOutputStream fout = new FileOutputStream(fo);
                byte inbuf[] = new byte[BUFSIZE];
                int n = 0;
                while ((n = zipInput.read(inbuf, 0, BUFSIZE)) != -1) {
                    fout.write(inbuf, 0, n);
                }
                fout.close();
                zip = zipInput.getNextEntry();
            }
            zipInput.close();
            fi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * �����ļ���s
	 * @param f
	 */
    private void makedirs(File f) {
        File ff = new File(f.getParent());
        System.out.println(f.getParent());
        if (!ff.exists()) ff.mkdirs();
    }

    /**
	 * ѹ���ļ�
	 * @param filedir ѹ��Ŀ¼
	 * @param zippath ѹ���ļ�·��
	 */
    public void zipFile(String filedir, String zippath) {
        List fl = this.getAllFilePath(filedir);
        File f = new File(zippath);
        try {
            FileOutputStream fo = new FileOutputStream(f);
            ZipOutputStream zo = new ZipOutputStream(fo);
            for (int i = 0; i < fl.size(); i++) {
                File ff = (File) fl.get(i);
                ZipEntry z = new ZipEntry(this.getZipEntryPath(ff.getPath(), filedir));
                zo.putNextEntry(z);
                FileInputStream fi = new FileInputStream(ff);
                byte inbuf[] = new byte[BUFSIZE];
                int n = 0;
                while ((n = fi.read(inbuf, 0, BUFSIZE)) != -1) {
                    zo.write(inbuf, 0, n);
                }
                fi.close();
            }
            zo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * ��ȡ��ѹ��������ļ��ṹ
	 * @param filepath
	 * @param filedir
	 * @return
	 */
    private String getZipEntryPath(String filepath, String filedir) {
        if (null == filedir) {
            return filepath;
        }
        if (filedir.substring(filedir.length() - 1).equals("\\") || filedir.substring(filedir.length() - 1).equals("/")) {
        } else {
            filedir += "/";
        }
        String zipEntryPath = filepath.substring(filedir.length());
        zipEntryPath = zipEntryPath.replace('\\', '/');
        return zipEntryPath;
    }

    /**
	 * ��ȡ���е��ļ�
	 * @param filedir �ļ�·��
	 * @return
	 */
    private List getAllFilePath(String filedir) {
        File f = new File(filedir);
        List<File> l = new ArrayList<File>();
        this.getFilePaths(f, l);
        return l;
    }

    /**
	 * ��ȡ��Ŀ¼�����е��ļ�(������Ŀ¼)
	 * @param dir
	 * @param l
	 */
    private void getFilePaths(File dir, List<File> l) {
        if (dir.isDirectory()) {
            File d[] = dir.listFiles();
            for (int i = 0; i < d.length; i++) {
                getFilePaths(d[i], l);
            }
        } else if (dir.isFile()) {
            l.add(dir);
        }
    }
}
