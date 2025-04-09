package org.apps.butler.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
	 * Read a file into byte array without encoding specified
	 *
	 * @param filePath
	 *            the file path.
	 * @return the file content in bytes.
	 */
    public static byte[] readFile(String filePath) throws Exception {
        File file = new File(filePath.trim());
        return readFile(file);
    }

    /**
	 * Read a file into byte array without encoding specified
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
    public static byte[] readFile(File file) throws Exception {
        byte[] data = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        try {
            logger.debug("readFile: " + file.getAbsolutePath());
            fis = new FileInputStream(file);
            copyFile(fis, bos);
            data = bos.toByteArray();
        } catch (Exception e) {
            logger.error("readFile failed", e);
            throw e;
        } finally {
            closeFis(fis);
            closeFos(bos);
        }
        return data;
    }

    /**
	 * Read a file with encoding specified
	 *
	 * @param filePath
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
    public static String readFile(String filePath, String encoding) throws Exception {
        return readFile(new File(filePath.trim()), encoding);
    }

    /**
	 * Read a file with encoding specified
	 *
	 * @param pFile
	 * @param encoding
	 * @return String
	 * @throws Exception
	 */
    public static String readFile(File file, String encoding) throws Exception {
        String content = "";
        if (file == null) return content;
        FileInputStream fis = null;
        BufferedReader br = null;
        String line = "";
        try {
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis, encoding));
            while (((line = br.readLine()) != null)) {
                content += line;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeBr(br);
            closeFis(fis);
        }
        return content;
    }

    /**
	 * Writes byte[] content to the file
	 *
	 * @param content
	 * @param filePath
	 * @throws Exception
	 */
    public static void writeFile(String filePath, byte[] content) throws Exception {
        writeFile(new File(filePath.trim()), content);
    }

    /**
	 * Writes byte[] content to the file
	 *
	 * @param content
	 *            a <code>byte[]</code> value
	 * @param destFile
	 *            a <code>File</code> value
	 * @exception Exception
	 *                if an error occurs
	 */
    public static void writeFile(File destFile, byte[] content) throws Exception {
        FileOutputStream fos = null;
        try {
            checkAndCreateParentDir(destFile);
            fos = new FileOutputStream(destFile);
            fos.write(content);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            closeFos(fos);
        }
    }

    /**
	 * Writes String content to the file without encoding specified
	 *
	 * @param destFile
	 * @param content
	 * @throws Exception
	 */
    public static void writeFile(String destFile, String content) throws Exception {
        writeFile(new File(destFile), content);
    }

    /**
	 * Writes String content to the file without encoding specified
	 *
	 * @param destFile
	 *            a <code>File</code> value
	 * @param content
	 *            a <code>String</code> value
	 * @exception Exception
	 *                if an error occurs
	 */
    public static void writeFile(File destFile, String content) throws Exception {
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        try {
            checkAndCreateParentDir(destFile);
            fos = new FileOutputStream(destFile);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(content, 0, content.length());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            closeBw(bw);
            closeFos(fos);
        }
    }

    /**
	 * Writes String content to the file with encoding
	 *
	 * @param filePath
	 * @param content
	 * @param encoding
	 * @throws Exception
	 */
    public static void writeFile(String filePath, String content, String encoding) throws Exception {
        writeFile(new File(filePath.trim()), content, encoding);
    }

    /**
	 * Writes String content to the file with encoding
	 *
	 * @param content
	 *            a <code>String</code> value
	 * @param destFile
	 *            a <code>File</code> value
	 * @exception Exception
	 *                if an error occurs
	 */
    public static void writeFile(File destFile, String content, String encoding) throws Exception {
        BufferedWriter bw = null;
        FileOutputStream fos = null;
        try {
            checkAndCreateParentDir(destFile);
            fos = new FileOutputStream(destFile);
            bw = new BufferedWriter(new OutputStreamWriter(fos, encoding));
            bw.write(content, 0, content.length());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            closeBw(bw);
            closeFos(fos);
        }
    }

    /**
	 * This function list the file names of files under the input directory with
	 * names start with prefix.
	 */
    public static String[] listFiles(String dir, String prefix) throws Exception {
        return listFiles(dir, prefix, true);
    }

    public static String[] listFiles(String dir, String pattern, boolean prefix) throws Exception {
        File dirFile = new File(dir);
        List<String> list = new Vector<String>();
        String[] files = dirFile.list();
        for (int i = 0; i < files.length; i++) {
            boolean add = false;
            if (prefix) {
                add = files[i].startsWith(pattern);
            } else {
                add = files[i].endsWith(pattern);
            }
            if (add) {
                list.add(files[i]);
            }
        }
        files = new String[list.size()];
        list.toArray(files);
        return files;
    }

    public static String getSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
    }

    /**
	 * This function creates a temporary directory in the specified dir with the
	 * prefix.
	 *
	 * @param dir
	 *            the directory name where the tmp dir is to be created.
	 * @param prefix
	 *            the prefix of the tmp dir name.
	 * @return the temporary directory file
	 */
    public static File createTmpDir(String dir, String prefix) throws Exception {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File tempFile = File.createTempFile(prefix, "", dirFile);
        tempFile.delete();
        tempFile.mkdir();
        return tempFile;
    }

    /**
	 * overload for copyDir with default overwrite parameter = true
	 */
    public static boolean copyDir(File srcFile, File destFile) {
        return copyDir(srcFile, destFile, true);
    }

    /**
	 * Copy one dir to the dest dir. It overwrites any files if present in dest
	 * dir.
	 *
	 * @param srcFile
	 *            Source dir
	 * @param destFile
	 *            Destination dir
	 * @return <code>boolean</code> true if and only if all files were copied
	 * @throws IOException
	 *             on error
	 */
    public static boolean copyDir(File srcFile, File destFile, boolean overwrite) {
        logger.debug("Copy srcFile[" + srcFile.getAbsolutePath() + "] to destFile[" + destFile.getAbsolutePath() + "] and" + " overwrite file if present");
        boolean result = false;
        if (srcFile.isDirectory()) {
            if (!destFile.isDirectory() && !destFile.mkdirs()) {
                return false;
            }
            File[] listFiles = srcFile.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                File curFile = listFiles[i];
                if (curFile.isDirectory()) {
                    result = copyDir(curFile, new File(destFile, curFile.getName()), overwrite);
                } else {
                    result = copyFile(curFile, new File(destFile, curFile.getName()), overwrite);
                }
                if (result == false) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
	 * Copy one file to the other with optionally overwriting the dest file
	 *
	 * @param srcFile
	 *            Source file
	 * @param destFile
	 *            Destination file
	 * @param overwrite
	 *            whether to overwrite the dest file if it exists
	 * @return <code>boolean</code> true if and only if file was copied
	 * @throws IOException
	 *             on error
	 */
    public static boolean copyFile(File srcFile, File destFile, boolean overwrite) {
        logger.debug("Copy srcFile[" + srcFile.getAbsolutePath() + "] to destFile[" + destFile.getAbsolutePath() + "] and" + " overwrite[" + overwrite + "]");
        if (!srcFile.exists()) {
            logger.debug("copyFile: Source file[" + srcFile + "] not found");
            return false;
        }
        if (!destFile.exists()) {
            checkAndCreateParentDir(destFile);
        } else {
            if (!overwrite) {
                logger.debug("copyFile: overwrite == false");
                return false;
            }
        }
        FileInputStream fileIn = null;
        FileOutputStream fileOut = null;
        boolean success = false;
        try {
            fileIn = new FileInputStream(srcFile);
            fileOut = new FileOutputStream(destFile);
            success = copyFile(fileIn, fileOut);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            closeFis(fileIn);
            closeFos(fileOut);
        }
        return success;
    }

    /**
	 * Creates Directory for the given path.
	 *
	 * @param pDirPath
	 *            a <code>String</code> value
	 * @exception SecurityException
	 *                - If a security manager exists and its
	 *                SecurityManager.checkRead(java.lang.String) method does
	 *                not permit verification of the existence of the named
	 *                directory and all necessary parent directories; or if the
	 *                SecurityManager.checkWrite(java.lang.String) method does
	 *                not permit the named directory and all necessary parent
	 *                directories to be created
	 */
    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    /**
	 * Remove the directory, include all the files and directories under the
	 * directory
	 *
	 * @param dir
	 */
    public static void removeDir(String dir) {
        File f = new File(dir);
        removeDir(f);
    }

    public static void removeFile(String filePath) {
        File f = new File(filePath);
        f.delete();
    }

    /**
	 * Remove the directory, include all the files and directories under the
	 * directory
	 *
	 * @param f
	 */
    public static void removeDir(File f) {
        if (!f.exists()) return;
        Stack<File> sk = new Stack<File>();
        sk.push(f);
        delete(sk);
    }

    private static void delete(Stack<File> sk) {
        while (!sk.empty()) {
            File ff = sk.pop();
            if (ff.delete()) continue;
            sk.push(ff);
            File[] fs = ff.listFiles();
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].isDirectory()) sk.push(fs[i]);
                if (fs[i].isFile()) fs[i].delete();
            }
        }
    }

    /**
	 * Remove all the files and directories under the directory
	 *
	 * @param dir
	 */
    public static void clearDir(String dir) {
        File folder = new File(dir);
        clearDir(folder);
    }

    /**
	 * Remove all the files and directories under the directory
	 *
	 * @param f
	 */
    public static void clearDir(File f) {
        if (!f.exists()) return;
        Stack<File> sk = new Stack<File>();
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            sk.push(files[i]);
        }
        delete(sk);
    }

    /**
	 * If the parent of the file does not exists, the parent directory will be
	 * created
	 *
	 * @param file
	 * @return true if parent directory exists or created successfully
	 */
    public static boolean checkAndCreateParentDir(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            return parentFile.mkdirs();
        }
        return true;
    }

    private static boolean copyFile(InputStream from, OutputStream to) throws IOException {
        BufferedInputStream bIn = new BufferedInputStream(from);
        BufferedOutputStream bOut = new BufferedOutputStream(to);
        byte[] bContent = new byte[8192];
        int len = 0;
        while ((len = bIn.read(bContent)) > 0) {
            bOut.write(bContent, 0, len);
        }
        bOut.flush();
        return true;
    }

    private static void closeFis(InputStream fis) {
        if (fis == null) return;
        try {
            fis.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void closeFos(OutputStream fos) {
        if (fos == null) return;
        try {
            fos.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void closeBw(BufferedWriter bw) {
        if (bw == null) return;
        try {
            bw.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void closeBr(BufferedReader br) {
        if (br == null) return;
        try {
            br.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
	 * Returns whether the specified file exsits.
	 */
    public static boolean exists(String filePath) {
        File file = new File(filePath.trim());
        return file.exists();
    }

    /**
	 * Move source File to destination File path. If the parent directory of
	 * destination doesn't exist. It will create it automatically. This method
	 * can move both files or directories.
	 *
	 * @param srcFilePath
	 *            source file path
	 * @param destFilePath
	 *            destination file path
	 * @return
	 */
    public static boolean renameTo(String srcFilePath, String destFilePath) {
        return renameTo(new File(srcFilePath), new File(destFilePath));
    }

    /**
	 * Move source File to destination File path. If the parent directory of
	 * destination doesn't exist. It will create it automatically. This method
	 * can move both files or directories.
	 *
	 * @param src
	 *            Source file
	 * @param dest
	 *            Destination file
	 * @return
	 */
    public static boolean renameTo(File src, File dest) {
        File parentDir = dest.getParentFile();
        if ((parentDir != null) && (!parentDir.exists())) {
            dest.getParentFile().mkdirs();
        }
        return src.renameTo(dest);
    }
}
