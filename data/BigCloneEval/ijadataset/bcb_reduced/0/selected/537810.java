package org.sysolar.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class FileManager {

    private static final Log log = LogFactory.getLog(FileManager.class);

    private static final boolean DEBUG = log.isDebugEnabled();

    /**
     * 获得文件夹里的子文件夹，根据 isHidden 的值确定获得的子文件夹的类型：全部、隐藏、非隐藏。
     * 
     * @param dir 文件夹路径
     * @param isHidden  null 表示获得全部子文件夹，或者 
     *                  true 表示获得隐藏子文件夹，或者 
     *                  false 表示获得非隐藏子文件夹
     * @return 符合条件的子文件夹数组，或者 null 当 dir 为 null 或者不是文件夹时
     */
    public static File[] listDirs(File dir, Boolean isHidden) {
        return listChildren(dir, true, isHidden);
    }

    /**
     * 获得文件夹里的文件，根据 isHidden 的值确定获得的文件的类型：全部、隐藏、非隐藏。
     * 
     * @param dir 文件夹路径
     * @param isHidden  null 表示获得全部文件，或者 
     *                  true 表示获得隐藏文件，或者 
     *                  false 表示获得非隐藏文件
     * @return 符合条件的文件数组，或者 null 当 dir 为 null 或者不是文件夹时
     */
    public static File[] listFiles(File dir, Boolean isHidden) {
        return listChildren(dir, false, isHidden);
    }

    private static File[] listChildren(File dir, boolean isDir, Boolean isHidden) {
        if (DEBUG) {
            log.debug("dir=" + dir + "; isDir=" + isDir + "; isHidden=" + isHidden);
        }
        if (null == dir || !dir.isDirectory()) {
            log.error(dir + "is not a directory !");
            return null;
        }
        File[] dirArray = dir.listFiles(new FileIsDirIsHiddenFilter(isDir, isHidden));
        if (DEBUG) {
            for (File d : dirArray) {
                log.debug(d);
            }
        }
        return dirArray;
    }

    /**
     * 递归获得文件夹里的子文件夹，根据 isHidden 的值确定获得的子文件夹的类型：全部、隐藏、非隐藏。
     * 
     * @param dir 文件夹路径
     * @param isHidden  null 获得全部子文件夹，或者 
     *                  true 获得隐藏子文件夹，或者 
     *                  false 获得非隐藏子文件夹
     * @return 符合条件的子文件夹列表，或者 null 当 dir 为 null 或者不是文件夹时
     */
    public static List<File> listDescendDirs(File dir, Boolean isHidden) {
        return listDescendants(dir, true, isHidden);
    }

    /**
     * 递归获得文件夹里的文件，根据 isHidden 的值确定获得的文件的类型：全部、隐藏、非隐藏。
     * 
     * @param dir 文件夹路径
     * @param isHidden  null 获得全部文件，或者 
     *                  true 获得隐藏文件，或者 
     *                  false 获得非隐藏文件
     * @return 符合条件的文件列表，或者 null 当 dir 为 null 或者不是文件夹时
     */
    public static List<File> listDescendFiles(File dir, Boolean isHidden) {
        return listDescendants(dir, false, isHidden);
    }

    private static List<File> listDescendants(File dir, boolean isDir, Boolean isHidden) {
        if (null == dir || !dir.isDirectory()) {
            log.error(dir + "is not a directory !");
            return null;
        }
        List<File> fileList = new ArrayList<File>(100);
        listDescendants(dir, new FileIsDirIsHiddenFilter(isDir, isHidden), fileList);
        if (DEBUG) {
            for (File f : fileList) {
                log.debug(f);
            }
        }
        return fileList;
    }

    private static void listDescendants(File dir, FileFilter filter, List<File> fileList) {
        File[] fileArray = dir.listFiles();
        for (int i = 0; null != fileArray && i < fileArray.length; i++) {
            if (filter.accept(fileArray[i])) {
                fileList.add(fileArray[i]);
            }
            if (fileArray[i].isDirectory()) {
                listDescendants(fileArray[i], filter, fileList);
            }
        }
    }

    /**
     * 把 srcDir 文件夹里的子文件夹和文件递归拷贝到 destDir 文件夹里。
     * 
     * @param srcDir 源文件夹
     * @param destDir 目标文件夹
     * @return true 拷贝成功，或者 false 拷贝失败
     */
    public static boolean copyDir(File srcDir, File destDir) {
        if (null == srcDir || !srcDir.exists() || !srcDir.isDirectory()) {
            log.error("srcDir(" + srcDir + ") is invalid !");
            return false;
        }
        if (srcDir.equals(destDir)) {
            log.error("srcDir(" + srcDir + ") is the same as destDir(" + destDir + ") !");
            return false;
        }
        List<File> fileList = listDescendFiles(srcDir, null);
        File path = null;
        for (File file : fileList) {
            path = new File(file.getParent().replace(srcDir.getAbsolutePath(), destDir.getAbsolutePath()));
            if (!copyFile(file, path)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 把 srcFile 文件拷贝到 destDir 文件夹里。
     * 
     * @param srcFile 源文件
     * @param destDir 目标文件夹
     * @return true 拷贝成功，或者 false 拷贝失败
     */
    public static boolean copyFile(File srcFile, File destDir) {
        if (null == srcFile || !srcFile.isFile()) {
            log.error("srcFile(" + srcFile + ") is invalid !");
            return false;
        }
        File destFile = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            if (srcFile.getParentFile().equals(destDir)) {
                log.error("srcFile(" + srcFile + ") is also in destDir(" + destDir + ") !");
                return false;
            }
            destFile = new File(destDir, srcFile.getName());
            in = new BufferedInputStream(new FileInputStream(srcFile));
            out = new BufferedOutputStream(new FileOutputStream(destFile));
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception ex) {
            log.error("error: srcFile=" + srcFile + " --> destFile=" + destFile, ex);
            return false;
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
        if (DEBUG) {
            log.debug("succeeded: srcFile=" + srcFile + " --> destFile=" + destFile);
        }
        return true;
    }

    /**
     * 删除 srcDir 文件夹里的子文件夹，根据 isHidden 的值确定将被删除的子文件夹的类型：全部、隐藏、非隐藏。
     * 
     * @param srcDir 源文件夹
     * @param isHidden  null 表示删除全部子文件夹，或者 
     *                  true 表示删除隐藏子文件夹，或者 
     *                  false 表示删除非隐藏子文件夹
     * @return true 表示删除成功，或者 false 表示删除失败
     */
    public static boolean clearDirs(File srcDir, Boolean isHidden) {
        if (null == srcDir || !srcDir.exists() || !srcDir.isDirectory()) {
            log.error("srcDir(" + srcDir + ") is invalid !");
            return false;
        }
        for (File d : listDirs(srcDir, isHidden)) {
            for (File f : listDescendFiles(d, null)) {
                if (!f.delete()) {
                    log.error("Failed in deteting file(" + f + ") !");
                    return false;
                }
                if (DEBUG) {
                    log.debug("Succeeded in deteting file(" + f + ") !");
                }
            }
            List<File> subDirList = listDescendDirs(d, null);
            Collections.sort(subDirList);
            for (int i = subDirList.size() - 1; i >= 0; i--) {
                if (!subDirList.get(i).delete()) {
                    log.error("Failed in deteting dir(" + subDirList.get(i) + ") !");
                    return false;
                }
                if (DEBUG) {
                    log.debug("Succeeded in deteting dir(" + subDirList.get(i) + ") !");
                }
            }
            if (!d.delete()) {
                log.error("Failed in deteting dir(" + d + ") !");
                return false;
            }
            if (DEBUG) {
                log.debug("Succeeded in deteting dir(" + d + ") !");
            }
        }
        if (DEBUG) {
            log.debug("succeeded: srcDir=" + srcDir + "; isHidden=" + isHidden);
        }
        return true;
    }

    /**
     * 删除 srcDir 文件夹里的文件，根据 isHidden 的值确定将被删除的文件的类型：全部、隐藏、非隐藏。
     * 
     * @param srcDir 源文件夹
     * @param isHidden  null 表示删除全部文件，或者 
     *                  true 表示删除隐藏文件，或者 
     *                  false 表示删除非隐藏文件
     * @return true 表示删除成功，或者 false 表示删除失败
     */
    public static boolean clearFiles(File srcDir, Boolean isHidden) {
        if (null == srcDir || !srcDir.exists() || !srcDir.isDirectory()) {
            log.error("srcDir(" + srcDir + ") is invalid !");
            return false;
        }
        for (File f : listFiles(srcDir, isHidden)) {
            if (!f.delete()) {
                log.error("Failed in deteting file(" + f + ") !");
                return false;
            }
            if (DEBUG) {
                log.debug("Succeeded in deteting file(" + f + ") !");
            }
        }
        if (DEBUG) {
            log.debug("succeeded: dir=" + srcDir + "; isHidden=" + isHidden);
        }
        return true;
    }

    /**
     * 删除 srcDir 文件夹，该操作将同时删除 srcDir 文件夹里的所有子文件夹及所有文件。
     * 
     * @param srcDir 目标文件夹
     * @return true 表示删除成功，或者 false 表示删除失败
     */
    public static boolean deleteDir(File srcDir) {
        if (null == srcDir) {
            log.error("srcDir(" + srcDir + ") is null !");
            return false;
        }
        if (!srcDir.exists()) {
            log.error("srcDir(" + srcDir + ") does not exist !");
            return true;
        }
        if (!srcDir.isDirectory()) {
            log.error("srcDir(" + srcDir + ") is not a directory !");
            return true;
        }
        if (!clearFiles(srcDir, null) || !clearDirs(srcDir, null) || !srcDir.delete()) {
            return false;
        }
        if (DEBUG) {
            log.debug("succeeded: dir=" + srcDir);
        }
        return true;
    }

    public static void main(String[] args) {
        File file = new File("E:\\FileUpload");
        deleteDir(file);
        System.out.println(Arrays.asList("E:/temp/界面/1".split(":")));
        System.out.println("E:/temp/界面/1".replace("E:/temp", "E:/FileUpload"));
    }
}
