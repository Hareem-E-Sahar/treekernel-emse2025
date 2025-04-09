package syncclipboard.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import syncclipboard.properties.Properties;

public class FileUtil {

    private FileUtil() {
    }

    /**
     * getFileList returns a list with all file which are matched to the search
     * string
     * 
     * @param search
     *            is a String
     * @param directory
     *            is a File object with the directory
     * @return a List with all founded files
     * @throws IllegalArgumentException
     *             if the directory not exists
     */
    public static List<File> getFileList(final String search, final File directory) throws FileNotFoundException {
        if (!directory.isDirectory()) {
            throw new FileNotFoundException(FileUtil.class.getName() + ": not correct path");
        }
        final File[] files = directory.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.contains(search);
            }
        });
        List<File> fileList = new ArrayList<File>();
        for (File file : files) {
            if (file.isFile()) fileList.add(file);
        }
        return fileList;
    }

    /**
     * getDirectoryList returns a list with all directories which are matched to
     * the search string
     * 
     * @param search
     *            is a String
     * @param directory
     *            is a File object with the directory
     * @return a List with all founded files
     * @throws IllegalArgumentException
     *             if the directory not exists
     */
    public static List<File> getDirectoryList(final String search, final File directory) throws FileNotFoundException {
        if (!directory.isDirectory()) {
            throw new FileNotFoundException(FileUtil.class.getName() + ": not correct path");
        }
        final File[] files = directory.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.contains(search);
            }
        });
        List<File> directoryList = new ArrayList<File>();
        for (File file : files) {
            if (file.isDirectory()) directoryList.add(file);
        }
        return directoryList;
    }

    /**
     * Returns all files and directories from the given directory.
     * 
     * @param directory is the main directory
     * @return a list with all files and directories
     * @throws FileNotFoundException if directory not correct
     */
    public static List<File> getAllFilesAndDirectoriesAsList(final File directory) throws FileNotFoundException {
        if (!directory.isDirectory()) {
            throw new FileNotFoundException(FileUtil.class.getName() + ": not a correct path");
        }
        final File[] files = directory.listFiles();
        return Arrays.asList(files);
    }

    /**
     * Returns all files and directories from the given directory as String list.
     * 
     * @param directory is the main directory
     * @return a list as String list with all files and directories
     * @throws FileNotFoundException if directory not correct
     */
    public static List<String> getAllFilesAndDirectoriesAsStringList(final File directory) throws FileNotFoundException {
        List<File> fileList = getAllFilesAndDirectoriesAsList(directory);
        List<String> fileStringList = new ArrayList<String>();
        for (File f : fileList) {
            fileStringList.add(f.getAbsolutePath());
        }
        return fileStringList;
    }

    public static File loadFileFromClasspath(String filename) throws FileNotFoundException {
        URL propUrl = ClassLoader.getSystemResource(filename);
        if (propUrl != null) {
            File file = null;
            try {
                file = new File(propUrl.toURI().getPath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (file != null && file.exists()) {
                return file;
            } else {
                throw new FileNotFoundException("Could not find the file" + filename + " in classpath!");
            }
        } else {
            throw new FileNotFoundException("Could not find the file" + filename + " in classpath!");
        }
    }

    /**
     * Calculates the file or all sub files in the directory.
     * 
     * @param fileOrDir
     *            is the main file or directory to caluclate the file or all sub
     *            files
     * @param stopOnByteSize
     *            if the max size in bytes on which the function stops the
     *            calculation or -1 to calculate without aborting
     * @return size of all files in the directory and subdirectories
     * @throws Exception
     *             if the size greater than stopOnByteSize
     * @throws FileNotFoundException
     *             if incomming file or directory not found in file system
     */
    public static long getFileOrDirectorySize(File fileOrDir, long stopOnByteSize) throws FileNotFoundException, Exception {
        long size = 0L;
        if (!fileOrDir.exists()) {
            throw new FileNotFoundException("cannot find file or directory " + fileOrDir.getAbsolutePath());
        }
        if (fileOrDir.isFile()) {
            size = fileOrDir.length();
            if (stopOnByteSize > 0 && size > stopOnByteSize) {
                throw new Exception("Size is greater than stopOnByteSize");
            }
        } else {
            for (File fileOrDirectory : fileOrDir.listFiles()) {
                size += getFileOrDirectorySize(fileOrDirectory);
                if (stopOnByteSize > 0 && size > stopOnByteSize) {
                    throw new Exception("Size is greater than stopOnByteSize");
                }
            }
        }
        return size;
    }

    /**
     * Calculates all files an subfiles in the directory.
     * 
     * @param directory
     *            is the root directory to caluclate all files
     * @return size of all files in the directory and subdirectories
     * @throws FileNotFoundException
     *             if incomming file or directory not found in file system
     */
    public static long getFileOrDirectorySize(File directory) throws FileNotFoundException {
        try {
            return getFileOrDirectorySize(directory, -1);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Removes the file or all files and sub directories from the given
     * directory.
     * 
     * @param fileOrDir
     *            is an file or directory
     * @throws IOException
     *             if deleting not possible
     */
    public static void removeAllFilesAndDirectories(File fileOrDir) throws IOException {
        if (fileOrDir.isDirectory()) {
            for (File f : fileOrDir.listFiles()) {
                removeAllFilesAndDirectories(f);
            }
        }
        if (!fileOrDir.delete()) {
            throw new java.io.IOException("cannot delete " + fileOrDir.getPath() + "!");
        }
    }

    /**
     * Removes all files and directories from an directory without removing of
     * the given directory
     * 
     * @param directory
     *            is directory
     * @throws IOException
     *             if deleting not possible or argument directory not a
     *             directory
     */
    public static void removeAllFilesAndSubDirectoriesFromDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                removeAllFilesAndDirectories(f);
            }
        } else {
            throw new IOException("direcory " + directory + " is not a directory");
        }
    }

    /**
     * Delets all directories with the given prefix from the system temp
     * directory.
     * 
     * @param tmpDirPrefix
     *            is the prefix of temp directories as String
     * @throws IOException
     *             if deleting not possible
     */
    public static void deleteAllTempDirs(final String tmpDirPrefix) throws IOException {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        File[] dirList = new File(baseTempPath).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(tmpDirPrefix)) {
                    return true;
                }
                return false;
            }
        });
        for (File dir : dirList) {
            removeAllFilesAndDirectories(dir);
        }
        return;
    }

    /**
     * Creates a temp directory in the system temp. It starts with the given
     * prefix and ends with a random number.
     * 
     * @param tmpDirPrefix
     *            is a string the the start of temp directory name
     * @return the created temp directory as File object
     */
    public static File createTempDir(String tmpDirPrefix) {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        Random rand = new Random();
        int randomInt = -1 * rand.nextInt();
        randomInt = randomInt < 0 ? randomInt * -1 : randomInt;
        File tempDir = new File(baseTempPath + File.separator + tmpDirPrefix + randomInt);
        if (tempDir.exists() == false) {
            tempDir.mkdir();
        }
        tempDir.deleteOnExit();
        return tempDir;
    }

    private static final int BUFFER = 8192;

    public static byte[] compressFilesAndDirsToByteArray(File[] filesAndDirs) throws Exception {
        ByteArrayOutputStream bArrayOutStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(bArrayOutStream);
        for (File f : filesAndDirs) {
            recursiveCompress(f, f.getParentFile().toURI(), zipOutputStream);
        }
        zipOutputStream.close();
        bArrayOutStream.close();
        return bArrayOutStream.toByteArray();
    }

    public static void compressFilesAndDirsToZipFile(File[] filesAndDirs, File outputFile) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        for (File f : filesAndDirs) {
            recursiveCompress(f, f.getParentFile().toURI(), zipOutputStream);
        }
        zipOutputStream.close();
    }

    private static void recursiveCompress(File file, URI relateTo, ZipOutputStream zipOutputStream) throws Exception {
        byte[] buffer = new byte[BUFFER];
        int len = 0;
        if (!file.isDirectory()) {
            URI relativePath = relateTo.relativize(file.toURI());
            ZipEntry entry = new ZipEntry(relativePath.getPath());
            zipOutputStream.putNextEntry(entry);
            FileInputStream fis = new FileInputStream(file);
            while ((len = fis.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }
            fis.close();
            zipOutputStream.closeEntry();
        } else {
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                recursiveCompress(child, relateTo, zipOutputStream);
            }
        }
    }

    public static void decompressFilesAndDirsFromByteArray(byte[] bytes, File outputFolder) throws FileNotFoundException, IOException {
        BufferedOutputStream dest = null;
        ByteArrayInputStream bArrayInStream = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bArrayInStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            File targetFile = new File(outputFolder + "/" + entry.getName());
            targetFile.mkdirs();
            targetFile.delete();
            FileOutputStream fos = new FileOutputStream(targetFile);
            int count;
            byte data[] = new byte[BUFFER];
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
    }

    public static void decompressFilesAndDirsFromZipFile(File zipFile, File outputFolder) throws IOException {
        BufferedOutputStream dest = null;
        FileInputStream fileInputStream = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(fileInputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            File targetFile = new File(outputFolder + "/" + entry.getName());
            targetFile.mkdirs();
            targetFile.delete();
            FileOutputStream fos = new FileOutputStream(targetFile);
            int count;
            byte data[] = new byte[BUFFER];
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
    }

    public static void main(String[] args) throws Exception {
        File[] files = { new File("C:\\seb_home\\tmp\\dwt-samples-89de7ff0752c"), new File("C:\\seb_home\\tmp\\dfl_entice_test") };
        byte[] barr = FileUtil.compressFilesAndDirsToByteArray(files);
        System.out.println("ByteArray (" + barr.length + ")");
        File zipFile = new File("C:\\seb_home\\tmp\\jTestFile.zip");
        FileUtil.compressFilesAndDirsToZipFile(files, zipFile);
        FileUtil.deleteAllTempDirs(Properties.TEMP_DIR_PREFIX);
    }
}
