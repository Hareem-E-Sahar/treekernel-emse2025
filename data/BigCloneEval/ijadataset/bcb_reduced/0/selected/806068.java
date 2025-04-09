package com.xinsdd.resUtil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZIPUtil {

    private ZIPUtil() {
    }

    private String saveDir = "";

    /**
	 * 创建压缩文件
	 * 
	 * @param filePath
	 *            文件目录
	 * @param zipFilePath
	 *            压缩后的文件目录
	 * @throws IOException
	 */
    public void createZipFile(String filePath, String zipFilePath) throws IOException {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        fos = new FileOutputStream(zipFilePath);
        zos = new ZipOutputStream(fos);
        writeZipFile(new File(filePath), zos, "");
        if (zos != null) zos.close();
        if (fos != null) fos.close();
    }

    /**
	 * 设置保存文件的目录
	 * @param path
	 */
    public void setSaveDir(String path) {
        this.saveDir = path.trim();
        if (!saveDir.substring(saveDir.length() - 1).equals("/") || !saveDir.substring(saveDir.length() - 1).equals("\\")) saveDir += "/";
        File dir = new File(saveDir);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
	 * 把一个路径下的所有文件分别压缩成ZIP包
	 * @param path
	 * @throws IOException 
	 */
    public List createAllZipFileFromPath(String path, List list) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            String fileName = file.getAbsolutePath();
            if (file.isFile()) {
                String saveName = file.getParent() + File.separator;
                if (!saveDir.equals("")) {
                    saveName = saveDir;
                }
                saveName += file.getName().substring(0, file.getName().lastIndexOf(".")) + ".res";
                createZipFile(fileName, saveName);
                System.out.println("成功压缩文件：" + fileName);
                list.add(saveName);
            } else {
                File[] childsFile = file.listFiles();
                for (int i = 0; i < childsFile.length; i++) {
                    createAllZipFileFromPath(childsFile[i].getAbsolutePath(), list);
                }
            }
        }
        return list;
    }

    /**
	 * 将文件写入压缩文件，并压缩
	 * 
	 * @param f
	 * @param zos
	 * @param hiberarchy
	 * @throws IOException
	 */
    private void writeZipFile(File f, ZipOutputStream zos, String hiberarchy) throws IOException {
        if (f.exists()) {
            if (f.isDirectory()) {
                hiberarchy += f.getName() + "/";
                File[] fif = f.listFiles();
                for (int i = 0; i < fif.length; i++) {
                    writeZipFile(fif[i], zos, hiberarchy);
                }
            } else {
                FileInputStream fis = null;
                fis = new FileInputStream(f);
                ZipEntry ze = new ZipEntry(hiberarchy + f.getName());
                zos.putNextEntry(ze);
                byte[] b = new byte[1024];
                while (fis.read(b) != -1) {
                    zos.write(b);
                    b = new byte[1024];
                }
                if (fis != null) fis.close();
            }
        }
    }

    private static ZIPUtil zu = null;

    public static ZIPUtil getInstance() {
        if (zu == null) zu = new ZIPUtil();
        return zu;
    }

    public static void main(String[] args) throws IOException {
        ZIPUtil.getInstance().createZipFile("D:/swf", "d:/swf.res");
    }
}
