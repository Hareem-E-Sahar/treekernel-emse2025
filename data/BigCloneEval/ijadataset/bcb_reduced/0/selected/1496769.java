package org.slasoi.studio.plugin.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;

public class AuxFiles {

    /**
	 * Chechs whether there is a file dir/filename.
	 * @param dir
	 * @param filename
	 * @return
	 */
    public static boolean doesDirContainFile(File dir, final String filename) {
        if (!dir.isDirectory()) return false;
        File dirContents[] = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                String platform = SWT.getPlatform();
                if (platform.equals("win32") || platform.equals("wpf")) return name.equalsIgnoreCase(filename); else return name.equals(filename);
            }
        });
        return dirContents.length > 0;
    }

    public static String findInPath(final String filename) {
        for (String path : System.getenv("PATH").split(File.pathSeparator)) {
            if (doesDirContainFile(new File(path), filename)) return path + File.separator + filename;
        }
        return null;
    }

    public static void copyFile(File src, File targetDir) throws IOException {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(new File(targetDir.getAbsoluteFile() + File.separator + src.getName()));
        System.out.println("Copying from " + src.getAbsolutePath() + " to " + targetDir.getAbsoluteFile() + File.separator + src.getName());
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }

    /**
	 * Deletes the given directory and its files, but NOT subdirectories
	 * @param dir
	 * @throws IOException
	 */
    public static void deleteDir(File dir) throws IOException {
        if (!dir.isDirectory() || !dir.canWrite()) throw new IOException("Not a directory, or not writable: " + dir.getAbsolutePath());
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) throw new IOException("Directory " + dir.getAbsolutePath() + " contains other directories; not deleted.");
        }
        for (File f : dir.listFiles()) {
            f.delete();
        }
        dir.delete();
    }

    public static IProject getProject(String projectName) {
        if (projectName == null || projectName.isEmpty()) return null;
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

    /**
	 * Does the same as "sed 's/pattern/replacement/g' srcFile >destFile".
	 * srcFile and destFile can be the same.
	 * @param srcFilename
	 * @param destFilename
	 * @param pattern
	 * @param replacement
	 * @throws IOException
	 */
    public static void sed(String srcFilename, String destFilename, String pattern, String replacement, int flags) throws IOException {
        sed(new FileInputStream(srcFilename), new File(destFilename), pattern, replacement, flags);
    }

    public static void sed(InputStream inputStream, File destFile, String pattern, String replacement, int flags) throws IOException {
        SedInputStream sedStream = new SedInputStream(inputStream, pattern, replacement, flags);
        sedStream.processContent();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(destFile));
            int c;
            while ((c = sedStream.read()) != -1) {
                writer.write(c);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (writer != null) writer.close();
        }
    }

    public static String copyFileToTmp(InputStream is) throws IOException {
        File temp = File.createTempFile("slasoi-studio", ".jar");
        temp.deleteOnExit();
        OutputStream os = new FileOutputStream(temp);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
        return temp.getAbsolutePath();
    }
}
