package org.databene.commons;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.databene.commons.file.DirectoryFileFilter;
import org.databene.commons.file.PatternFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Utility class.<br/>
 * <br/>
 * Created: 04.02.2007 08:22:52
 * @since 0.1
 * @author Volker Bergmann
 */
public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static void ensureDirectoryExists(File directory) {
        if (directory != null && !directory.exists()) {
            File parent = directory.getParentFile();
            if (parent != null) ensureDirectoryExists(parent);
            directory.mkdir();
        }
    }

    public static boolean hasSuffix(File file, String suffix, boolean caseSensitive) {
        if (caseSensitive) return file.getName().endsWith(suffix); else return file.getName().toLowerCase().endsWith(suffix.toLowerCase());
    }

    /** extracts the filename part after the last dot */
    public static String suffix(File file) {
        return suffix(file.getName());
    }

    /** extracts the filename part after the last dot */
    public static String suffix(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) return "";
        return filename.substring(dotIndex + 1);
    }

    public static String nativePath(String path) {
        return path.replace('/', SystemInfo.getFileSeparator());
    }

    public static boolean isEmptyFolder(File folder) {
        String[] list = folder.list();
        return list == null || list.length == 0;
    }

    public static void copy(File srcFile, File targetFile, boolean overwrite) throws FileNotFoundException, IOException {
        copy(srcFile, targetFile, overwrite, null);
    }

    public static void copy(File srcFile, File targetFile, boolean overwrite, FileFilter filter) throws FileNotFoundException, IOException {
        if (filter != null && !filter.accept(srcFile.getCanonicalFile())) return;
        if (!srcFile.exists()) throw new ConfigurationError("Source file not found: " + srcFile);
        if (!overwrite && targetFile.exists()) throw new ConfigurationError("Target file already exists: " + targetFile);
        if (srcFile.isFile()) copyFile(srcFile, targetFile); else copyDirectory(srcFile, targetFile, overwrite, filter);
    }

    public static String localFilename(String filePath) {
        if (filePath == null) return null;
        int i = filePath.lastIndexOf(File.separatorChar);
        if (File.separatorChar != '/') i = Math.max(i, filePath.lastIndexOf('/'));
        return (i >= 0 ? filePath.substring(i + 1) : filePath);
    }

    public static boolean equalContent(File file1, File file2) {
        long length = file1.length();
        if (length != file2.length()) return false;
        try {
            LOGGER.debug("Comparing content of " + file1 + " and " + file2);
            InputStream in1 = new BufferedInputStream(new FileInputStream(file1));
            InputStream in2 = new BufferedInputStream(new FileInputStream(file2));
            for (long i = 0; i < length; i++) {
                if (in1.read() != in2.read()) {
                    LOGGER.debug("files unequal");
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error comparing " + file1 + " with " + file2, e);
        }
        LOGGER.debug("files equal");
        return true;
    }

    private static void copyFile(File srcFile, File targetFile) throws FileNotFoundException, IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(srcFile));
        OutputStream out = null;
        try {
            out = new FileOutputStream(targetFile);
            IOUtil.transfer(in, out);
        } finally {
            IOUtil.close(out);
            IOUtil.close(in);
        }
    }

    private static void copyDirectory(File srcDirectory, File targetDirectory, boolean overwrite, FileFilter filter) throws FileNotFoundException, IOException {
        ensureDirectoryExists(targetDirectory);
        for (File src : srcDirectory.listFiles()) {
            File dstFile = new File(targetDirectory, src.getName());
            copy(src, dstFile, overwrite, filter);
        }
    }

    public static void deleteIfExists(File file) {
        if (file.exists()) {
            if (!file.delete()) file.deleteOnExit();
        }
    }

    public static void deleteDirectoryIfExists(File folder) {
        if (folder.exists()) deleteDirectory(folder);
    }

    public static void deleteDirectory(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) deleteDirectory(file); else file.delete();
        }
        folder.delete();
    }

    public static List<File> listFiles(File dir, String regex, boolean recursive, boolean acceptingFiles, boolean acceptingFolders) {
        PatternFileFilter filter = new PatternFileFilter(regex, acceptingFiles, acceptingFolders);
        return addFilenames(dir, filter, recursive, new ArrayList<File>());
    }

    public static String relativePath(File fromFile, File toFile) {
        return relativePath(fromFile, toFile, File.separatorChar);
    }

    public static String relativePath(File fromFile, File toFile, char separator) {
        File fromFolder = (fromFile.isDirectory() ? fromFile : fromFile.getParentFile());
        try {
            String[] from = StringUtil.tokenize(fromFolder.getCanonicalPath(), File.separatorChar);
            String[] to = StringUtil.tokenize(toFile.getCanonicalPath(), File.separatorChar);
            int i = 0;
            while (i < from.length && i < to.length && from[i].equals(to[i])) i++;
            StringBuilder builder = new StringBuilder();
            for (int j = from.length - 1; j >= i; j--) {
                if (builder.length() > 0) builder.append(separator);
                builder.append("..");
            }
            for (int j = i; j < to.length; j++) {
                if (builder.length() > 0) builder.append(separator);
                builder.append(to[j]);
            }
            if (builder.length() == 0) builder.append(".");
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while calculating relative path from " + fromFile + " to " + toFile + ": ", e);
        }
    }

    private static List<File> addFilenames(File dir, FileFilter filter, boolean recursive, List<File> buffer) {
        File[] matches = dir.listFiles(filter);
        if (matches != null) for (File match : matches) buffer.add(match);
        if (recursive) {
            File[] subDirs = dir.listFiles(DirectoryFileFilter.instance());
            if (subDirs != null) for (File subFolder : subDirs) addFilenames(subFolder, filter, recursive, buffer);
        }
        return buffer;
    }

    public static String normalizeFilename(String rawName) {
        StringBuilder builder = new StringBuilder(rawName.length());
        StringCharacterIterator iterator = new StringCharacterIterator(rawName);
        while (iterator.hasNext()) {
            char c = iterator.next();
            if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '*' || c == '_' || c == '+' || c == ' ') builder.append(c); else if (c == '@') builder.append("a"); else if (c == '$') builder.append("s");
        }
        return builder.toString().trim();
    }

    public static File fileOfLimitedPathLength(File directory, String name, String suffix, boolean warn) {
        return fileOfLimitedPathLength(directory, name, suffix, 255, warn);
    }

    public static File fileOfLimitedPathLength(File directory, String name, String suffix, int maxLength, boolean warn) {
        try {
            String parentPath;
            parentPath = directory.getCanonicalPath();
            int consumedLength = parentPath.length() + 1 + suffix.length();
            int availableLength = maxLength - consumedLength;
            if (availableLength <= 0) throw new IllegalArgumentException("Parent path name to long: " + parentPath);
            String prefix = name;
            if (availableLength < prefix.length()) {
                prefix = prefix.substring(0, availableLength);
                if (warn) LOGGER.warn("File name too long: {}, it was cut to {}", parentPath + SystemInfo.getFileSeparator() + name + suffix, parentPath + SystemInfo.getFileSeparator() + prefix + suffix);
            }
            return new File(directory, prefix + suffix);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error composing file path", e);
        }
    }
}
