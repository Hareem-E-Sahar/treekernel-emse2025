package org.brainypdm.modules.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.brainypdm.constants.ErrorCodes;
import org.brainypdm.exceptions.BaseException;
import org.brainypdm.modules.commons.log.BrainyLogger;

public class FileUtils {

    /**
	 * logger
	 */
    public static final BrainyLogger log = new BrainyLogger(FileUtils.class);

    public static String removeExeedsFileSeparator(String filename) {
        final String s1 = "\\";
        final String s2 = "/";
        String out = filename;
        while (out != null && (out.endsWith(s1) || out.endsWith(s2))) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    /****
	 * read file and put content into byte array
	 * @param String filename
	 * @return byte[]
	 * @throws BaseException
	 */
    public static byte[] getBytes(String filename) throws BaseException {
        return getBytes(new File(filename));
    }

    /****
	 * read file and put content into byte array
	 * @param File file
	 * @return byte[]
	 * @throws BaseException
	 */
    public static byte[] getBytes(File file) throws BaseException {
        byte[] bytes = null;
        InputStream is;
        try {
            is = new FileInputStream(file);
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                throw new BaseException(ErrorCodes.CODE_243, length, Integer.MAX_VALUE);
            }
            bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            is.close();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ErrorCodes.CODE_242, e.getLocalizedMessage());
        }
        return bytes;
    }

    /***
	 * write byte[] array into a new file. If file exists, delete it and
	 * create another one
	 * 
	 * @param filepath
	 * @param content
	 * @throws BaseException
	 */
    public static void writeBytes(String filepath, byte[] content) throws BaseException {
        try {
            File f = new File(filepath);
            if (f.exists()) {
                boolean delete = f.delete();
                if (!delete) {
                    BaseException b = new BaseException(ErrorCodes.CODE_246, filepath);
                    throw b;
                }
            }
            File dir = new File(f.getParent());
            if (!dir.exists()) {
                boolean makeDirs = dir.mkdirs();
                if (!makeDirs) {
                    BaseException b = new BaseException(ErrorCodes.CODE_247, filepath);
                    throw b;
                }
            }
            if (!f.exists()) {
                boolean createNew = f.createNewFile();
                if (!createNew) {
                    BaseException b = new BaseException(ErrorCodes.CODE_245, filepath);
                    throw b;
                }
            }
            FileOutputStream fs = new FileOutputStream(filepath);
            fs.write(content);
            fs.flush();
            fs.close();
        } catch (Exception e) {
            throw new BaseException(ErrorCodes.CODE_244, e.getLocalizedMessage());
        }
    }

    /***************************************************************************
	 * copy a file into another
	 * 
	 * @param s
	 * @param d
	 * @throws BaseException
	 */
    public static void copyFile(String s, String d) throws BaseException {
        File sf = new File(s);
        File sd = new File(d);
        copyFile(sf, sd);
    }

    /***
	 * copy a file into another
	 * @param s
	 * @param d
	 * @throws BaseException
	 */
    public static void copyFile(File s, File d) throws BaseException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (!d.exists()) {
                boolean create = d.createNewFile();
                if (!create) {
                    throw new BaseException(ErrorCodes.CODE_240, d);
                }
            }
            in = new FileInputStream(s);
            out = new FileOutputStream(d);
            int length = in.available();
            byte[] bytes = new byte[length];
            in.read(bytes);
            out.write(bytes);
        } catch (BaseException e) {
            throw e;
        } catch (IOException e) {
            throw new BaseException(ErrorCodes.CODE_239, e.getLocalizedMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                log.error("failed to close streams");
            }
        }
    }

    /***
	 * delete a file
	 * @param filepath
	 * @throws BaseException
	 */
    public static void deleteFile(String filepath) throws BaseException {
        deleteFile(new File(filepath));
    }

    /***
	 * delete a file
	 * @param filepath
	 * @throws BaseException
	 */
    public static void deleteFile(File filepath) throws BaseException {
        boolean out = filepath.delete();
        if (!out) {
            throw new BaseException(ErrorCodes.CODE_241, filepath.getName());
        }
    }

    /***
	 * delete all children of directory
	 * @param aFile - a file to delete
	 * @param aRoot - root directory, it'must not be deleted!
	 * @return
	 * @throws BaseException
	 */
    public static boolean deleteDirectoryChildren(File aFile, File aRoot) throws BaseException {
        boolean success = false;
        if (aFile.isDirectory()) {
            String[] children = aFile.list();
            for (int i = 0; i < children.length; i++) {
                File current = new File(aFile, children[i]);
                success = deleteDirectoryChildren(current, aRoot);
                if (!success) {
                    throw new BaseException(ErrorCodes.CODE_241, current.getName());
                }
            }
        }
        if (!aRoot.equals(aFile)) {
            success = aFile.delete();
            if (!success) {
                throw new BaseException(ErrorCodes.CODE_241, aFile.getName());
            }
        }
        return success;
    }

    /****
	 * zip a list of files
	 * 
	 * @param sources
	 * @param outputFile
	 * @throws BaseException
	 */
    public static void zipFile(String[] sources, String outputFile) throws BaseException {
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
            for (int i = 0; i < sources.length; i++) {
                FileInputStream in = new FileInputStream(sources[i]);
                out.putNextEntry(new ZipEntry(sources[i]));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception e) {
            throw new BaseException(ErrorCodes.CODE_248);
        }
    }

    public static void zipFile(String source, String outputFile) throws BaseException {
        zipFile(new String[] { source }, outputFile);
    }
}
