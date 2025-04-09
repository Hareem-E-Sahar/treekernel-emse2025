package com.jspx.io.file;

import com.jspx.utils.FileUtil;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User:chenYuan (mail:cayurain@21cn.com)
 * Date: 2006-9-22
 * Time: 16:54:25
 */
public class MultiFile {

    public MultiFile() {
    }

    public static final int BUFFER_SIZE = 1024 * 2;

    /**
     * 删除指定目录及其中的所有内容。
     *
     * @param dirName 要删除的目录的目录名
     * @return 删除成功时返回true，否则返回false。
     * @since 0.1
     */
    public boolean deleteDirectory(String dirName) {
        return deleteDirectory(new File(dirName));
    }

    /**
     * 删除指定目录及其中的所有内容。
     *
     * @param dir 要删除的目录
     * @return 删除成功时返回true，否则返回false。
     * @since 0.1
     */
    public boolean deleteDirectory(File dir) {
        if ((dir == null) || !dir.isDirectory()) {
            throw new IllegalArgumentException("Argument " + dir + " is not a directory. ");
        }
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                deleteDirectory(entries[i]);
            } else {
                if (entries[i].canWrite()) {
                    if (!entries[i].delete()) {
                        entries[i].deleteOnExit();
                    }
                } else {
                    entries[i].deleteOnExit();
                }
            }
        }
        return dir.delete();
    }

    /**
     * 拷贝目录 inputDir 到 目录
     *
     * @param inputDir
     * @param outputDir
     * @return boolean
     */
    public boolean copyDirectoryMoveTo(String inputDir, String outputDir) {
        return copyDirectory(inputDir, outputDir) && deleteDirectory(inputDir);
    }

    /**
     * Copie un rpertoire dans un autre
     */
    public boolean copyDirectoryToOneDir(File inputDir, File outputDir) {
        if (!inputDir.isDirectory()) return false;
        if (!FileUtil.makeDirectory(outputDir.getAbsolutePath())) {
            return false;
        }
        File[] files = inputDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (!copy(file, new File(outputDir.getAbsolutePath() + File.separator + file.getName()))) {
                    return false;
                }
            } else if (file.isDirectory()) {
                if (!copyDirectoryToOneDir(file, outputDir)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean copyDirectoryToOneDir(String inputDir, String outputDir) {
        return copyDirectoryToOneDir(new File(inputDir), new File(outputDir));
    }

    /**
     * 移动到一个目录下
     *
     * @param inputDir
     * @param outputDir
     * @return int
     */
    public int moveToOneDirectory(String inputDir, String outputDir) {
        int result = 0;
        File f = new File(inputDir);
        File[] files = f.listFiles();
        if (files != null) {
            for (File fromfile : files) {
                if (fromfile.isDirectory()) {
                    if (copyDirectoryToOneDir(fromfile.getAbsolutePath(), outputDir)) {
                        deleteDirectory(fromfile);
                        result++;
                    }
                }
            }
        }
        return result;
    }

    public boolean copy(String inputFilename, String outputFilename) {
        return copy(new File(inputFilename), new File(outputFilename));
    }

    /**
     * Copie un fichier vers un autre fichier ou un rpertoire vers un autre rpertoire
     */
    public boolean copy(File input, File output) {
        if (input.isDirectory()) {
            if (!FileUtil.makeDirectory(output)) return false;
            if (!copyDirectory(input, output)) return false;
        } else {
            if (!copyFile(input, output)) return false;
        }
        return true;
    }

    /**
     * 拷贝文件
     * Copie un fichier vers un autre
     */
    public boolean copyFile(File inputFile, File outputFile) {
        if (outputFile.exists()) {
            String ftype = FileUtil.getTypePart(outputFile.getName());
            String fpath = FileUtil.getPathPart(outputFile.getAbsolutePath());
            String fileName = FileUtil.getNamePart(outputFile.getName());
            outputFile = new File(fpath + fileName + "_duplicate." + ftype);
        }
        BufferedInputStream fr = null;
        BufferedOutputStream fw = null;
        try {
            fr = new BufferedInputStream(new FileInputStream(inputFile));
            fw = new BufferedOutputStream(new FileOutputStream(outputFile));
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = fr.read(buf)) >= 0) {
                fw.write(buf, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean copyDirectory(String inputDir, String outputDir) {
        return copyDirectory(new File(inputDir), new File(outputDir));
    }

    /**
     * Copie un rpertoire dans un autre
     */
    public boolean copyDirectory(File inputDir, File outputDir) {
        if (!FileUtil.makeDirectory(outputDir.getAbsolutePath())) {
            return false;
        }
        File[] files = inputDir.listFiles();
        for (File file : files) {
            File destFile = new File(outputDir.getAbsolutePath() + File.separator + file.getName());
            if (!destFile.exists()) {
                if (file.isDirectory()) {
                    destFile.mkdir();
                }
            }
            if (!copy(file, destFile)) {
                return false;
            }
        }
        return true;
    }
}
