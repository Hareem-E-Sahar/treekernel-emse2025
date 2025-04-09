package mades.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michele Sama (m.sama@puzzledev.com)
 * 
 * Static class for convenience methods.
 *
 */
public class Files {

    private Files() {
    }

    public static boolean checkFileExist(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        return true;
    }

    public static void copyDir(File from, File to) throws IOException {
        copyDir(from, to, true);
    }

    public static void copyDir(File from, File to, boolean skipHiddenFolders) throws IOException {
        if (!from.exists() && !from.isDirectory()) {
            throw new IOException(from.getName() + " does not exist or it is not a directory.");
        }
        if (to.exists()) {
            if (!to.isDirectory()) {
                throw new IOException(from.getName() + " it is not a directory.");
            }
        } else {
            to.mkdir();
        }
        for (File f : from.listFiles()) {
            File tof = new File(to, f.getName());
            if (f.isDirectory()) {
                if (f.isHidden() && skipHiddenFolders) {
                    continue;
                } else {
                    copyDir(f, tof, skipHiddenFolders);
                }
            } else {
                com.google.common.io.Files.copy(f, new File(to, f.getName()));
            }
        }
    }

    public static void checkFileExistsOrThrow(String filename, Logger logger) {
        if (!checkFileExist(filename)) {
            String errorMsg = "File not found or is a directory: " + filename;
            logger.severe(errorMsg);
            throw new AssertionError(errorMsg);
        }
    }

    public static boolean checkFolderExist(String path) {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        return true;
    }

    public static void checkFolderExistOrThrow(String path, Logger logger) {
        if (!checkFolderExist(path)) {
            String errorMsg = "Folder not found or is a file: " + path;
            logger.severe(errorMsg);
            throw new AssertionError(errorMsg);
        }
    }

    public static boolean checkFolderExistAndIsWritable(String path) {
        if (!checkFolderExist(path)) {
            return false;
        } else {
            File folder = new File(path);
            if (!folder.canWrite()) {
                return false;
            }
        }
        return true;
    }

    public static void checkFolderExistAndIsWritableOrThrow(String path, Logger logger) {
        if (!checkFolderExistAndIsWritable(path)) {
            String errorMsg = "Folder does not exist or it is not writable: " + path;
            logger.severe(errorMsg);
            throw new AssertionError(errorMsg);
        }
    }

    public static File getCurrentPath(@SuppressWarnings("rawtypes") Class clazz) {
        File location = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
        if (location.isFile()) {
            return location.getParentFile();
        } else {
            return location;
        }
    }

    public static String compileTemplateFile(HashMap<String, String> variables, Reader source) throws IOException {
        String tag = "\\{%([\\w _-]+)%\\}";
        Pattern patternTag = Pattern.compile(tag);
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(source);
        String line = null;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = patternTag.matcher(line);
            int lastIndex = 0;
            while (matcher.find()) {
                String key = matcher.group(1).trim();
                builder.append(line.substring(lastIndex, matcher.start()));
                builder.append(variables.get(key));
                lastIndex = matcher.end();
            }
            builder.append(line.substring(lastIndex));
            builder.append("\n");
        }
        return builder.toString();
    }
}
