package mn.more.foundation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mn.more.foundation.lang.StringUtil;

/**
 * File-related operations are handled here; some of which can be applied to
 * directory as well.
 *
 * @author <a href="mailto:mike.liu@aptechmongolia.edu.mn">Mike Liu</a>
 * @version $Id: FileOperator.java 5 2008-09-01 12:08:42Z mikeliucc $
 */
public final class FileOperator {

    private static final int STREAM_BUFFER_SIZE = 4096;

    private static final String ZIP_EXTENSION = ".zip";

    private FileOperator() {
    }

    /**
	 * copy file/directory.  This method will determine if <code>from</code> is a
	 * file or directory, and then delegate to the appropriate method to perform
	 * the IO task.
	 */
    public static void copy(String from, String to) throws IOException {
        if (StringUtil.isBlank(from)) {
            throw new IllegalArgumentException("argument 'from' [" + from + "] is not valid.");
        }
        if (StringUtil.isBlank(to)) {
            throw new IllegalArgumentException("argument 'to' [" + to + "] is not valid.");
        }
        if (FileUtil.isDirectory(from)) {
            copyDirectory(from, to);
        } else {
            copyFile(from, to);
        }
    }

    /**
	 * non-intrusive copy.  Only copy files and subdirectories to destination, does
	 * not remove any elements from destination directory unless there's any name
	 * duplication.
	 *
	 * @see #copy(String,String)
	 */
    private static void copyDirectory(String from, String to) throws IOException {
        if (!FileUtil.isDirectory(from)) {
            throw new IllegalArgumentException("argument 'from' [" + from + "] is not a valid directory.");
        }
        if (!FileUtil.isDirectory(to)) {
            throw new IllegalArgumentException("cannot copy [" + from + "] to [" + to + "]");
        }
        File[] sourceMembers = new File(from).listFiles(FileUtil.DIR_ONLY_FILTER);
        if (sourceMembers == null || sourceMembers.length < 1) {
            return;
        }
        for (File sourceMember : sourceMembers) {
            if (sourceMember.isDirectory()) {
                copyDirectory(sourceMember.getAbsolutePath(), to);
            } else {
                copyFile(sourceMember.getAbsolutePath(), to + File.separator + sourceMember.getName());
            }
        }
    }

    /** @see #copy(String,String) */
    public static void copyFile(String fromFilename, String toFilename) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(fromFilename);
            out = new FileOutputStream(toFilename);
            byte[] buffer = new byte[STREAM_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void fileMove(String fromFilename, String toFilename) throws IOException {
        if (FileUtil.isFileExists(fromFilename)) {
            FileOperator.copyFile(fromFilename, toFilename);
            FileOperator.deleteFile(fromFilename);
        }
    }

    /**
	 * delete the inode reference <code>destination</code>, which can be either a
	 * file or a directory.<p>
	 * <p/>
	 * In case <code>destination</code> contains children, this method will
	 * forcefully delete all its members as well as itself.
	 */
    public static boolean delete(String destination) {
        if (FileUtil.isFileExists(destination)) {
            return deleteFile(destination);
        }
        if (FileUtil.isDirectory(destination)) {
            File d = new File(destination);
            File[] children = d.listFiles();
            for (File child : children) {
                delete(child.getAbsolutePath());
            }
            return d.delete();
        }
        return false;
    }

    /**
	 * delete the file specified by the <code>filename</code>.
	 *
	 * @return boolean
	 */
    public static boolean deleteFile(String filename) {
        return FileUtil.isFileExists(filename) && (new File(filename)).delete();
    }

    /**
	 * create directories recursively.
	 *
	 * @param directory java.lang.String the name of the directory to be created
	 */
    public static void createDirectory(String directory) {
        if (StringUtil.isBlank(directory)) {
            return;
        }
        try {
            File path = new File(directory);
            if (!path.exists()) {
                path.mkdirs();
            }
        } catch (NullPointerException e) {
        }
    }

    /**
	 * saves a file as <code>filename</code> and returns a File as filename. If
	 * <code>contentStream</code> is null, then the method just return a file
	 * handle based on <code>filename</code>.
	 *
	 * @return the created file
	 */
    public static File createFile(String filename, InputStream contentStream) throws IOException {
        if (StringUtil.isBlank(filename)) {
            throw new IOException("invalid filename " + filename);
        }
        File file = new File(filename);
        if (contentStream != null) {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file);
                FileOutputStream out = new FileOutputStream(file);
                byte[] buffer = new byte[STREAM_BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = contentStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw e;
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return file;
    }

    /**
	 * returns a <code>File</code> object with name as <code>filename</code> and
	 * content as indicated by <code>contentStream</code>.  This method is more of
	 * a convenience method to the <code>java.io.File.createTempFile()</code>
	 * method as provided by JDK.
	 * <p/>
	 * If <code>contentStream</code> is null, then the method just return the file
	 * object based on <code>filename</code>.
	 *
	 * @return java.io.File
	 * @throws IOException The exception description.
	 * @see File#createTempFile(String,String)
	 */
    public static File createTempFile(String filename, InputStream contentStream) throws IOException {
        if (StringUtil.isBlank(filename)) {
            throw new IOException("invalid filename " + filename);
        }
        File file = null;
        FileWriter fileWriter = null;
        try {
            file = File.createTempFile(filename, null);
            fileWriter = new FileWriter(file);
            FileOutputStream out = new FileOutputStream(file);
            if (contentStream != null) {
                int bytesRead;
                byte[] buffer = new byte[STREAM_BUFFER_SIZE];
                while ((bytesRead = contentStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e1) {
                }
            }
        }
        return file;
    }

    /**
	 * create a zip file containing the content of <code>filename</code>. The zip
	 * file will be located in the same location as <code>filename</code>, and
	 * named as <code>filename</code>.zip
	 *
	 * @param filename the filename of the zip file
	 */
    public static void createZipFile(String filename) throws IOException {
        if (filename == null || filename.length() < 1) {
            return;
        }
        String zipFileName = filename + ZIP_EXTENSION;
        ZipOutputStream zipStream = null;
        try {
            FileOutputStream outputStream = new FileOutputStream(zipFileName);
            zipStream = new ZipOutputStream(outputStream);
            StringBuffer content = FileUtil.getContent(filename);
            zipStream.setLevel(6);
            zipStream.putNextEntry(new ZipEntry(filename));
            zipStream.write(content.toString().getBytes(), 0, content.length());
            zipStream.closeEntry();
            zipStream.close();
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
