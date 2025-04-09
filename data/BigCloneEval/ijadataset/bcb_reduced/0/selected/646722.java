package com.aqua.wireshark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * General file name and file transformation utility class. TODO should be
 * merged with jsystem.utils.FileUtils
 * 
 * @author Golan Derazon
 */
public class FileUtils {

    /**
	 * Zips <code>file</code> New zipped file is created in the same directory
	 * of the <code>file</code>. New file name is created by changing the
	 * suffux of <code>file</code> to .zip. If zip operation succeeds original
	 * file is deleted
	 */
    public static File fileToZipFile(File file) throws Exception {
        ZipOutputStream out = null;
        FileInputStream in = null;
        File retFile = changeFileNameSuffix(file, "zip");
        try {
            byte[] buf = new byte[1024];
            out = new ZipOutputStream(new FileOutputStream(retFile));
            in = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(file.getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        file.delete();
        return retFile;
    }

    /**
	 * Changes <code>f</code> suffix to <code>newSuffix</code> The method
	 * assums file name ends with ".somthing". The method replaces something
	 * with <code>newSuffix</code>
	 */
    public static File changeFileNameSuffix(File f, String newSuffix) {
        String name = f.getName();
        return new File(f.getParent(), changeFileNameSuffix(name, newSuffix));
    }

    /**
	 * Changes <code>name</code> suffix to <code>newSuffix</code> The method
	 * assums that name ends with ".somthing". The method replaces something
	 * with <code>newSuffix</code>
	 */
    public static String changeFileNameSuffix(String name, String newSuffix) {
        int indexOfSuffix = name.lastIndexOf('.');
        if (indexOfSuffix == -1) {
            return name;
        }
        name = name.substring(0, indexOfSuffix + 1);
        return name + newSuffix;
    }

    /**
	 * Wrapps path with ". This is needed when path has spaces in it.
	 */
    public static String wrapPathWithApostrophe(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("\"") || path.endsWith("\"")) {
            return path;
        }
        return "\"" + path + "\"";
    }
}
