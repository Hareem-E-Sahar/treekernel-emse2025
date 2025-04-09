package com.angel.common.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** The <code>JarFileHelper.class</code> helps you to manage jar files.
 *
 * @author William
 * @version
 */
public class JarFileHelper extends FileHelper {

    /** It is the logger for the class. */
    private static Logger logger = Logger.getLogger(JarFileHelper.class);

    private static final int COMPRESS_OUTPUTSTREAM_BUFFER_SIZE = 255;

    private JarFileHelper() {
        super();
    }

    public static void setLogLevel(Level logLevel) {
        logger.setLevel(logLevel);
    }

    public static JarEntry getEntryFor(String jarFileName, String jarFilePath, String entryName) {
        JarFile jarFile = getJarFile(jarFileName, jarFilePath);
        JarEntry jarEntry = getJarEntryFor(jarFile, entryName);
        return jarEntry;
    }

    public static JarFile getJarFile(String jarFullFile) {
        String fileName = getFileNameFrom(jarFullFile);
        String filePath = getFilePathFrom(jarFullFile);
        JarFile jarFile = getJarFile(fileName, filePath);
        return jarFile;
    }

    public static JarFile getJarFile(String jarFileName, String jarFilePath) {
        File file = getFile(jarFileName, jarFilePath);
        JarFile jarFile = getJarFile(file);
        return jarFile;
    }

    public static JarFile getJarFile(File file) {
        JarFile jarFile = null;
        if (file != null) {
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                logger.error("An input/output error has ocurred when it obtains jar file.", e);
                jarFile = null;
            }
        }
        return jarFile;
    }

    public static JarEntry getJarEntryFor(JarFile jarFile, String entryName) {
        return (jarFile != null) ? jarFile.getJarEntry(entryName) : null;
    }

    public static File getFileAtJar(String jarFileName, String jarFilePath, String entryName, String outputFile) {
        JarFile jarFile = getJarFile(jarFileName, jarFilePath);
        File output = getFileAtJar(jarFile, entryName, outputFile);
        return output;
    }

    public static File getFileAtJar(JarFile jarFile, String entryName, String outputFile) {
        JarEntry jarEntry = getJarEntryFor(jarFile, entryName);
        File output = getFileAtJar(jarFile, jarEntry, outputFile + jarEntry.getName());
        return output;
    }

    public static File getFileAtJar(JarFile jarFile, JarEntry jarEntry, String outputFile) {
        File output = null;
        try {
            if (jarFile != null && jarEntry != null) {
                InputStream in = jarFile.getInputStream(jarEntry);
                StringBuffer stringBuffer = getStringBufferFor(in);
                output = createFile(stringBuffer, outputFile);
            } else {
                if (jarFile != null) {
                    logger.error("It can't get file with entry name because its jarEntry doesn't exist (it is null).");
                } else {
                    logger.error("It can't get jar file because its jarFile doesn't exist (it is null).");
                }
            }
        } catch (IOException e) {
            logger.error("An input/output error has ocurred when it obtains a file from jar file.", e);
        }
        return output;
    }

    private static StringBuffer getStringBufferFor(InputStream in) {
        StringBuffer sb = new StringBuffer("");
        try {
            sb = new StringBuffer();
            int chr;
            while ((chr = in.read()) != -1) {
                sb.append((char) chr);
            }
            in.close();
        } catch (IOException e) {
            logger.error("An error has ocurred trying to read file.", e);
        }
        return sb;
    }

    public static void unjarFileAt(JarFile jarFile, String outputDirectory) {
        JarEntry entry = null;
        if (!finishPathWithDirectorySeparator(outputDirectory)) {
            outputDirectory = outputDirectory.concat(DIRECTORY_SEPARATOR_1);
        }
        verifyDirectory(outputDirectory);
        Enumeration<?> e = jarFile.entries();
        int counter = 0;
        for (; e.hasMoreElements(); ) {
            entry = (JarEntry) e.nextElement();
            verifyDirectory(outputDirectory + getFilePathFrom(entry.getName()) + DIRECTORY_SEPARATOR_1);
            getFileAtJar(jarFile, entry, outputDirectory + entry.getName());
            counter++;
        }
        logger.debug("Unjar process creates '" + counter + "' files at '" + outputDirectory + "' directory.");
    }

    @SuppressWarnings(value = "unchecked")
    public static File compressDirectoryAt(String outputJarFileName, File directory) {
        ZipOutputStream out = createZipOutputStream(outputJarFileName);
        List<File> files = (List<File>) CollectionHelper.convertTo(directory.listFiles());
        File outputFile = null;
        if (directory != null && directory.isDirectory()) {
            compressDirectoryFiles(out, files);
            try {
                out.close();
                logger.debug("Zip file was created and files was added succesfully.");
            } catch (IOException e) {
                logger.error("Zip file couldn't be closed correctly cause by a input / output error.");
            }
            outputFile = new File(outputJarFileName);
        } else {
            logger.error("Directory with name '" + directory.getAbsolutePath() + "' couldn't be found.");
        }
        return outputFile;
    }

    private static void compressDirectoryFiles(ZipOutputStream out, List<File> files) {
        byte[] data = new byte[COMPRESS_OUTPUTSTREAM_BUFFER_SIZE];
        BufferedInputStream origin = null;
        File file = null;
        int counter = 0;
        try {
            if (files.size() > 0) {
                for (int i = 0; i < files.size(); i++) {
                    file = files.get(i);
                    addFileToJar(origin, out, data, file.getPath());
                    counter++;
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("File '" + file.getPath() + "' couldn't be added to jar file because it doesn't exist.", e);
        } catch (IOException e) {
            logger.error("File '" + file.getPath() + "' couldn't be added to jar file cause of an input/ouput error.", e);
        }
    }

    public static File compressDirectoryAt(String outputJarFileName, String directoryPath) {
        File directory = new File(directoryPath);
        File file = compressDirectoryAt(outputJarFileName, directory);
        return file;
    }

    @SuppressWarnings("unchecked")
    private static void compressAllDirectory(ZipOutputStream out, List<File> files) {
        byte[] data = new byte[COMPRESS_OUTPUTSTREAM_BUFFER_SIZE];
        BufferedInputStream origin = null;
        File file = null;
        int counterFiles = 0, counterDirectories = 0;
        if (files.size() > 0) {
            for (int i = 0; i < files.size(); i++) {
                try {
                    file = files.get(i);
                    if (file.isFile()) {
                        addFileToJar(origin, out, data, file.getPath());
                        counterFiles++;
                    } else if (file.isDirectory()) {
                        List<File> newFiles = (List<File>) CollectionHelper.convertTo(file.listFiles());
                        compressAllDirectory(out, newFiles);
                        counterDirectories++;
                    }
                } catch (FileNotFoundException e) {
                    logger.error("File '" + file.getPath() + "' couldn't be added to jar file because it doesn't exist.", e);
                } catch (IOException e) {
                    logger.error("File '" + file.getPath() + "' couldn't be added to jar file cause of an input/ouput error.", e);
                }
            }
            logger.debug("File '" + file.getPath() + "' couldn't be added to jar file cause of an input/ouput error.");
        }
    }

    @SuppressWarnings("unchecked")
    public static File compressDirectoryAt(String outputJarFileName, String directoryPath, boolean withSubdirectories) {
        File directory = new File(directoryPath);
        ZipOutputStream out = createZipOutputStream(outputJarFileName);
        List<File> files = (List<File>) CollectionHelper.convertTo(directory.listFiles());
        File outputFile = null;
        if (directory != null && directory.isDirectory()) {
            try {
                if (files.size() > 0) {
                    compressAllDirectory(out, files);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                out.close();
                logger.debug("Zip file was created and files was added succesfully.");
            } catch (IOException e) {
                logger.error("Zip file couldn't be closed correctly cause by a input / output error.");
            }
            outputFile = new File(outputJarFileName);
        } else {
            logger.error("Directory with name '" + directory.getAbsolutePath() + "' couldn't be found.");
        }
        return outputFile;
    }

    public static File compressFilesAt(String outputJarFileName, List<String> filesFullPath) {
        ZipOutputStream out = createZipOutputStream(outputJarFileName);
        createJarFilesWith(out, filesFullPath);
        File file = getFile(outputJarFileName);
        return file;
    }

    private static void createJarFilesWith(ZipOutputStream out, List<String> filesFullPath) {
        byte[] data = new byte[COMPRESS_OUTPUTSTREAM_BUFFER_SIZE];
        BufferedInputStream origin = null;
        int counter = 0;
        for (Iterator<?> i = filesFullPath.iterator(); i.hasNext(); ) {
            String filename = (String) i.next();
            try {
                addFileToJar(origin, out, data, filename);
                counter++;
            } catch (FileNotFoundException e) {
                logger.error("File '" + filename + "' couldn't be added to jar file because it doesn't exist file.", e);
            } catch (IOException e1) {
                logger.error("File '" + filename + "' couldn't be added to jar file cause of an input/ouput error.", e1);
            }
        }
        try {
            out.close();
            logger.debug("Zip file was created and '" + counter + "' files was added succesfully.");
        } catch (IOException e) {
            logger.error("Zip file couldn't be closed correctly cause by a input / output error.");
        }
    }

    /**
	 * @param origin2
	 * @param out
	 * @param data
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    private static void addFileToJar(BufferedInputStream origin, ZipOutputStream out, byte[] data, String fillFullPath) throws FileNotFoundException, IOException {
        logger.debug("Begining to add file '" + fillFullPath + "' to jar file.");
        FileInputStream fi = new FileInputStream(fillFullPath);
        origin = new BufferedInputStream(fi, COMPRESS_OUTPUTSTREAM_BUFFER_SIZE);
        ZipEntry entry = new ZipEntry(fillFullPath);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, COMPRESS_OUTPUTSTREAM_BUFFER_SIZE)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();
        logger.debug("File '" + fillFullPath + "' added succesfully to jar file.");
    }

    private static ZipOutputStream createZipOutputStream(String outputJarFileName) {
        FileOutputStream dest = null;
        try {
            dest = new FileOutputStream(outputJarFileName);
        } catch (FileNotFoundException e) {
            logger.error("File '" + outputJarFileName + "' wasn't found at specificated path.", e);
        }
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        return out;
    }

    public static void unjarFileAt(String jarFileName, String jarFilePath, String outputDirectory) {
        JarFile jarFile = getJarFile(jarFileName, jarFilePath);
        unjarFileAt(jarFile, outputDirectory);
    }

    private static void verifyDirectory(String outputDirectory) {
        File directory = new File(outputDirectory);
        boolean createsDirs = directory.mkdirs();
        if (createsDirs) {
            logger.debug("Tree directories with name '" + outputDirectory + "' was created.");
        }
    }
}
