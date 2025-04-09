package com.angel.common.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.angel.architecture.exceptions.NonBusinessException;

/** The <code>FileHelper.class</code> helps you to manage files.
 *
 * @author William
 * @version
 */
public class FileHelper {

    public static final String DIRECTORY_SEPARATOR_1 = "/";

    public static final String DIRECTORY_SEPARATOR_2 = "\\";

    /** It is the logger for the class. */
    private static Logger logger = Logger.getLogger(FileHelper.class);

    protected FileHelper() {
        super();
    }

    public static void setLogLevel(Level logLevel) {
        logger.setLevel(logLevel);
    }

    public static File createFile(String fileName) {
        File file = new File(fileName);
        boolean wasCreated = false;
        try {
            wasCreated = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("wasCreated: " + wasCreated);
        return file;
    }

    public static File createFile(String fileName, String path) {
        File file = null;
        if (!StringHelper.isEmpty(fileName) && !StringHelper.isEmpty(path)) {
            file = createFile(fileName + path);
        } else {
            if (!StringHelper.isEmpty(fileName)) {
                file = createFile(fileName);
            } else {
                logger.warn("It couldn't create file because file name given is an empty string.");
            }
        }
        return file;
    }

    public static File readFile(String fileName) {
        File file = getFile(fileName);
        return file;
    }

    public static File getFile(String fileName) {
        File file = new File(fileName);
        if (!canBeRead(file)) {
            logger.error("It couldn't get file because file doesn't exist and couldn't be read.");
            file = null;
        }
        return file;
    }

    public static File getFile(String fileName, String path) {
        String fullFile = getPathAndFileName(fileName, path);
        return getFile(fullFile);
    }

    public static String getPathAndFileName(String fileName, String path) {
        String fullFile = null;
        if (!StringHelper.isEmpty(fileName) && !StringHelper.isEmpty(path)) {
            fullFile = new String(path + fileName);
        } else {
            if (!StringHelper.isEmpty(fileName)) {
                fullFile = new String(fileName);
            } else {
                logger.warn("It couldn't get full file name because file name given is an empty string.");
            }
        }
        return fullFile;
    }

    public static Boolean canBeRead(File file) {
        boolean canBeRead = false;
        if (file != null) {
            canBeRead = file.exists() && file.canRead();
        }
        return canBeRead;
    }

    public static File createFile(StringBuffer sb, String outputFile) {
        File newFile = new File(outputFile);
        try {
            FileWriter fileWriter = new FileWriter(newFile);
            fileWriter.write(sb.toString());
            fileWriter.flush();
            fileWriter.close();
            logger.debug("File with name '" + outputFile + "' was created.");
        } catch (IOException e) {
            logger.error("An several error at input / output has ocurred writing file '" + outputFile + "'.", e);
        }
        return newFile;
    }

    public static String getFileNameFrom(String fullName) {
        String fileName = null;
        if (!StringHelper.containsAtLeast(fullName, DIRECTORY_SEPARATOR_1) && !StringHelper.containsAtLeast(fullName, DIRECTORY_SEPARATOR_2)) {
            fileName = new String(fullName);
        } else {
            for (int i = fullName.length() - 1; i > 0; i--) {
                if (Character.toString(fullName.charAt(i)).equalsIgnoreCase(DIRECTORY_SEPARATOR_1) || Character.toString(fullName.charAt(i)).equalsIgnoreCase(DIRECTORY_SEPARATOR_2)) {
                    fileName = new String(fullName.substring(i, fullName.length()));
                    i = 0;
                }
            }
        }
        return fileName;
    }

    public static String getFilePathFrom(String fullName) {
        String fileName = getFileNameFrom(fullName);
        String filePath = fullName.substring(0, fullName.length() - fileName.length());
        return filePath;
    }

    protected static void deleteFile(File file) {
        boolean deleted = file.delete();
        if (deleted) {
            logger.debug("File '" + file.getName() + "' was deleted.");
        } else {
            logger.error("File '" + file.getName() + "' couldn't be deleted.");
        }
    }

    public static void deleteAllAt(String directoryName) {
        File directory = new File(directoryName);
        File[] listFiles = directory.listFiles();
        if (listFiles != null && listFiles.length > 0) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    deleteAllAt(file.getAbsolutePath());
                } else if (file.isFile()) {
                    deleteFile(file);
                }
            }
        }
        deleteFile(directory);
    }

    public static Boolean isDirectory(String fullPathName) {
        Boolean isDirectory = false;
        File file = getFile(fullPathName);
        if (file != null && file.isDirectory()) {
            isDirectory = true;
        }
        return isDirectory;
    }

    public static Boolean isAFile(String fullPathName) {
        Boolean isFile = false;
        File file = getFile(fullPathName);
        if (file != null && file.isFile()) {
            isFile = true;
        }
        return isFile;
    }

    protected static Boolean finishPathWithDirectorySeparator(String path) {
        Boolean finish = false;
        if (String.valueOf(path.charAt(path.length() - 1)).equalsIgnoreCase(DIRECTORY_SEPARATOR_1) || String.valueOf(path.charAt(path.length() - 1)).equalsIgnoreCase(DIRECTORY_SEPARATOR_2)) {
            finish = true;
        }
        return finish;
    }

    public static String getCurrentJavaClassPathProperty() {
        String directory = System.getProperty("user.dir");
        if (StringHelper.isNotEmpty(directory)) {
            List<String> properties = StringHelper.split(directory, ";");
            if (!properties.isEmpty()) {
                directory = StringHelper.split(directory, ";").get(0);
            } else {
                directory = "";
            }
        }
        return directory;
    }

    public static InputStream findInputStreamInPath(String absolutePathFile) throws FileNotFoundException {
        File file = new File(absolutePathFile);
        InputStream inputStream = new FileInputStream(file);
        return inputStream;
    }

    public static InputStream findInputStreamInClasspath(String fullPathFile) throws FileNotFoundException {
        String classPath = getSystemCurrentDirectory() + fullPathFile;
        File file = new File(classPath);
        InputStream inputStream = new FileInputStream(file);
        return inputStream;
    }

    private static final String getSystemCurrentDirectory() {
        String javaClassPath = System.getProperty("java.class.path");
        int pos = javaClassPath.indexOf(";");
        if (pos != -1) {
            javaClassPath = javaClassPath.substring(0, pos);
        }
        return javaClassPath;
    }

    public static int writeInOutputStream(OutputStream outputStream, InputStream inputStream) {
        try {
            byte[] buf = new byte[1024];
            int count;
            int totalLength = 0;
            while ((count = inputStream.read(buf)) >= 0) {
                outputStream.write(buf, 0, count);
                totalLength += count;
            }
            outputStream.flush();
            return totalLength;
        } catch (IOException e) {
            throw new NonBusinessException("Error during building output stream.", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new NonBusinessException("Error during closing input/output stream.", e);
            }
        }
    }
}
