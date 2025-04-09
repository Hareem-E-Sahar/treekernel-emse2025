package org.j2eespider.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {

    /**
     * Copies all FILES under srcDir to dstDir (except velocity files .vm)
     * If dstDir does not exist, it will be created.
	 * @param srcDir
	 * @param dstDir
	 * @throws IOException
	 */
    public static void copyTemplateDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals("CVS") || children[i].equals(".svn")) {
                    continue;
                }
                copyTemplateDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            Pattern pattern = Pattern.compile("(\\\\fragment\\\\.*?\\.vm)|(/fragment/.*?\\.vm)|(\\.[\\w\\d]+\\.vm)|(\\..*?\\.groovy)|(\\..*?\\.ftl)|(.*?\\.zip)");
            Matcher matcher = pattern.matcher(srcDir.getPath());
            if (!matcher.find()) {
                copyFile(srcDir, dstDir);
            }
        }
    }

    /**
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        String dirWriter = dst.getAbsolutePath();
        if (src.exists() && src.isFile()) {
            int lastSeparator = dst.getAbsolutePath().lastIndexOf("/");
            if (lastSeparator == -1) {
                lastSeparator = dst.getAbsolutePath().lastIndexOf("\\");
            }
            dirWriter = dst.getAbsolutePath().substring(0, lastSeparator);
        }
        new File(dirWriter).mkdirs();
        if (!src.exists() || src.getAbsolutePath().equals("")) {
            return;
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Checks if exists the archive in the directory.
     * @param fileName
     * @param fileDir
     * @return
     */
    public static boolean existsFileInDir(String fileName, File fileDir) {
        if (fileDir.isDirectory()) {
            String[] childrens = fileDir.list();
            for (String children : childrens) {
                if (children.equals(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Write content to file.
     * @param filePath
     * @param content
     */
    public static void writeFile(String filePath, String content) {
        try {
            FileOutputStream os = new FileOutputStream(filePath);
            FileDescriptor fd = os.getFD();
            os.write(content.getBytes());
            os.flush();
            fd.sync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
