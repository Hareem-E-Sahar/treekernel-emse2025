package eu.planets_project.pp.plato.util;

import java.io.File;

public class OS {

    public static final String getTmpPath() {
        String tempDir = System.getProperty("java.io.tmpdir").replace('\\', '/');
        if (!tempDir.endsWith("/")) {
            tempDir += "/";
        }
        return tempDir;
    }

    public static final String getJhoveTmpPath() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        int slashb = tmpPath.indexOf("\\");
        int slashf = tmpPath.indexOf("/");
        if (slashb > 0 && (slashf < 0 || slashb < slashf)) tmpPath += "\\"; else tmpPath += "/";
        return tmpPath;
    }

    public static final String completePathWithSeparator(String path) {
        if (!path.endsWith("/")) {
            return path + "/";
        } else {
            return path;
        }
    }

    /**
     * Deletes the given directory and its content recursively 
     * @param dir
     */
    public static void deleteDirectory(File dir) {
        FileUtils.log.debug("deleting directory ... " + dir.getAbsolutePath());
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
