package net.sourceforge.omov.core.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.omov.core.BusinessException;
import net.sourceforge.omov.core.tools.scan.Scanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author christoph_pickl@users.sourceforge.net
 */
public final class FileUtil {

    private static final Log LOG = LogFactory.getLog(FileUtil.class);

    private static final Set<String> HIDDEN_FILE_NAMES = CollectionUtil.immutableSet(".DS_Store", "Icon\r", "Thumbs.db");

    private static final Set<String> DOT_PREFIXES;

    static {
        final Set<String> tmp = new TreeSet<String>();
        tmp.add("dr");
        tmp.add("mr");
        DOT_PREFIXES = Collections.unmodifiableSet(tmp);
    }

    private FileUtil() {
    }

    /**
     * @return null if no extension; otherwise lowercase suffix behind last "."
     */
    public static String extractExtension(File file) {
        return extractExtension(file.getName());
    }

    /**
     * @return null if no extension; otherwise lowercase suffix behind last "."
     */
    public static String extractExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return null;
    }

    private static long MB5 = 1024 * 1024 * 5;

    public static void copyFile(File sourceFile, File targetFile) throws BusinessException {
        if (sourceFile.exists() == false) {
            throw new IllegalArgumentException("Could not copy given file '" + sourceFile.getAbsolutePath() + "' becaus it does not exist!");
        }
        if (sourceFile.length() > MB5) {
            copyBigFile(sourceFile, targetFile);
        } else {
            copySmallFile(sourceFile, targetFile);
        }
    }

    private static void copyBigFile(final File sourceFile, final File targetFile) throws BusinessException {
        LOG.debug("Copying BIG file '" + sourceFile.getAbsolutePath() + "' to '" + targetFile.getAbsolutePath() + "'.");
        if (sourceFile.exists() == false) {
            throw new BusinessException("Could not copy sourcefile '" + sourceFile.getAbsolutePath() + "' because it does not exist!");
        }
        InputStream input = null;
        OutputStream output = null;
        if (targetFile.exists() == true) {
            LOG.info("Overwrting existing target file '" + targetFile.getAbsolutePath() + "'.");
            if (targetFile.delete() == false) {
                throw new BusinessException("Could not delete target file '" + targetFile.getAbsolutePath() + "'!");
            }
        }
        try {
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(targetFile);
            byte[] bytes = new byte[1024];
            while (input.read(bytes) >= 0) {
                output.write(bytes);
            }
        } catch (IOException e) {
            throw new BusinessException("Could not copy file from '" + sourceFile.getAbsolutePath() + "' to '" + targetFile.getAbsolutePath() + "'!", e);
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException e) {
                LOG.error("Could not close stream!", e);
            }
            try {
                if (output != null) output.close();
            } catch (IOException e) {
                LOG.error("Could not close stream!", e);
            }
        }
    }

    /**
     * @see {@link http://www.rgagnon.com/javadetails/java-0064.html}
     */
    private static void copySmallFile(File sourceFile, File targetFile) throws BusinessException {
        LOG.debug("Copying SMALL file '" + sourceFile.getAbsolutePath() + "' to '" + targetFile.getAbsolutePath() + "'.");
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(sourceFile).getChannel();
            outChannel = new FileOutputStream(targetFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new BusinessException("Could not copy file from '" + sourceFile.getAbsolutePath() + "' to '" + targetFile.getAbsolutePath() + "'!", e);
        } finally {
            try {
                if (inChannel != null) inChannel.close();
            } catch (IOException e) {
                LOG.error("Could not close input stream!", e);
            }
            try {
                if (outChannel != null) outChannel.close();
            } catch (IOException e) {
                LOG.error("Could not close output stream!", e);
            }
        }
    }

    public static File copyDirectoryRecursive(final File sourceDir, final File targetSuperDir) throws BusinessException {
        LOG.debug("Copying directory recursive '" + sourceDir.getAbsolutePath() + "' to '" + targetSuperDir.getAbsolutePath() + "'.");
        if (sourceDir.exists() == false) {
            throw new BusinessException("Could not copy sourcefile '" + sourceDir.getAbsolutePath() + "' because it does not exist!");
        }
        final File targetDir = new File(targetSuperDir, sourceDir.getName());
        if (targetDir.mkdir() == false) {
            throw new BusinessException("Could not create directory '" + targetDir.getAbsolutePath() + "'!");
        }
        for (File subFile : sourceDir.listFiles()) {
            if (subFile.isFile()) {
                copyFile(subFile, new File(targetDir, subFile.getName()));
            } else {
                copyDirectoryRecursive(subFile, targetDir);
            }
        }
        return targetDir;
    }

    private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("0.0");

    /**
     * @return something like "13.3 KB" or "3.1 GB"
     */
    public static String formatFileSize(final long inKiloByte) {
        if (inKiloByte < 1024) {
            return FILE_SIZE_FORMAT.format(inKiloByte) + " KB";
        }
        final long inMegaByte = inKiloByte / 1024;
        if (inMegaByte < 1024) {
            return FILE_SIZE_FORMAT.format(inKiloByte / 1024.) + " MB";
        }
        return FILE_SIZE_FORMAT.format(inMegaByte / 1024) + " GB";
    }

    public static String formatFileSizeGb(final long inKiloByte) {
        return FILE_SIZE_FORMAT.format(inKiloByte / 1024. / 1024.) + " GB";
    }

    public static double getGigaByteFromKiloByte(long kb) {
        double mb = kb / 1024.;
        double gb = mb / 1024. * 10;
        long gb10th = Math.round(gb);
        return gb10th / 10.;
    }

    public static void deleteDirectoryRecursive(final File directory) throws BusinessException {
        LOG.debug("Deleting directory '" + directory.getAbsolutePath() + "' recursive.");
        if (directory.exists() == false) throw new IllegalArgumentException("Directory '" + directory.getAbsolutePath() + "' does not exist!");
        if (directory.isDirectory() == false) throw new IllegalArgumentException("Given file '" + directory.getAbsolutePath() + "' is not a directory!");
        for (File subFile : directory.listFiles()) {
            if (subFile.isFile()) {
                LOG.debug("Deleting file '" + subFile.getAbsolutePath() + "'.");
                if (subFile.delete() == false) {
                    throw new BusinessException("Could not delete file '" + subFile.getAbsolutePath() + "' (existing=" + subFile.exists() + ")!");
                }
            } else {
                deleteDirectoryRecursive(subFile);
            }
        }
        if (directory.delete() == false) {
            throw new BusinessException("Could not delete directory '" + directory.getAbsolutePath() + "'!");
        }
    }

    /**
     * [Mantis 0000037] Removes additional dots if file is something like "The.Great.Adventure.of.Someone.avi"
     * @param fileWithDots file or folder, does not matter
     * @return given string "The.Great.Adventure.of.Someone.avi" returns "The Great Adventure of Someone.avi"
     * @see Scanner#scanMovieFolderInfo(File)
     * {@link http://omov.sourceforge.net/mantis/view.php?id=37}
     */
    public static String clearFileNameDots(final File fileWithDots) {
        final String nameWithDots = fileWithDots.getName();
        final int cntDots = nameWithDots.split("\\.").length - 1;
        if (cntDots <= (fileWithDots.isFile() ? 1 : 0)) {
            LOG.debug("No dots to clear for " + (fileWithDots.isFile() ? "file" : "directory") + " '" + fileWithDots.getAbsolutePath() + "'.");
            return nameWithDots;
        }
        final String nameToClear;
        final String extensionToUse;
        if (fileWithDots.isFile()) {
            final String extension = nameWithDots.substring(nameWithDots.lastIndexOf(".") + 1);
            nameToClear = nameWithDots.substring(0, nameWithDots.length() - (extension.length() + 1));
            extensionToUse = "." + extension;
        } else {
            nameToClear = nameWithDots;
            extensionToUse = "";
        }
        final StringBuilder sb = new StringBuilder();
        int lastDotIdx = -1;
        int dotIdx = nameToClear.indexOf(".");
        do {
            final String part = nameToClear.substring(lastDotIdx + 1, dotIdx);
            sb.append(part);
            if (isDotPrefix(part)) {
                sb.append(". ");
            } else {
                sb.append(" ");
            }
            lastDotIdx = dotIdx;
        } while ((dotIdx = nameToClear.indexOf(".", dotIdx + 1)) != -1);
        sb.append(nameToClear.substring(lastDotIdx + 1));
        final String result = sb.toString();
        return result.trim() + extensionToUse;
    }

    private static boolean isDotPrefix(String s) {
        final int lastWhitespace = s.lastIndexOf(" ");
        if (lastWhitespace != -1) {
            s = s.substring(lastWhitespace + 1);
        }
        return DOT_PREFIXES.contains(s.toLowerCase());
    }

    public static boolean isHiddenFile(File file) {
        return HIDDEN_FILE_NAMES.contains(file.getName());
    }

    public static String getFileContentsFromJar(final String jarFile, int initialCapacityOfStringBuilder) throws BusinessException {
        LOG.debug("Getting contents of file '" + jarFile + "' from jar.");
        final StringBuilder sb = new StringBuilder(initialCapacityOfStringBuilder);
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = FileUtil.class.getResourceAsStream(jarFile);
            reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new BusinessException("Could not get contents of resource '" + jarFile + "'!", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                LOG.error("Could not close input stream!", e);
            }
        }
    }

    public static String extractLastFolderName(final String path) {
        final int index = path.lastIndexOf(File.separator);
        if (index == -1) {
            LOG.warn("Could not get last folder name of path '" + path + "'!");
            return null;
        }
        return path.substring(index + 1);
    }

    public static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOG.warn("Could not close closeable.");
            }
        }
    }

    public static long getSizeRecursive(File file) {
        if (file.isFile()) {
            return file.length() / 1024;
        }
        long size = 0;
        for (File subFile : file.listFiles()) {
            size += getSizeRecursive(subFile);
        }
        return size;
    }

    public static File getParentByPath(final File file) {
        final String path = file.getAbsolutePath();
        return new File(path.substring(0, path.lastIndexOf(File.separator)));
    }

    public static void main(String[] args) {
    }
}
