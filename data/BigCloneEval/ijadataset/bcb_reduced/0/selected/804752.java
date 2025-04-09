package mya_dc.shared_classes.files_manipulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A class for compressing files to zip files and load compressed files.
 *
 * @author      Adam Levi
 * <br>			MYA
 */
public class ZipManager {

    /**
	 *	Compress file (could be either a directory or a file) to
	 *		 file.getName() + ".zip" at the workingDirectory.
	 *
	 * @param file - File Object to compress.
	 * @param workingDirectory - Root directory of the project.
	 * @param relativePath - Relative path of the file in the project.
	 * @param buildDirectories - Add directories inside the zip.
	 * 
	 * @return A file that points to the compressed file
	 */
    public static File fileToZip(File file, String workingDirectory, String relativePath, boolean buildDirectories) throws IOException {
        String outFilename = (workingDirectory + file.getName() + ".zip");
        FileOutputStream fos = new FileOutputStream(outFilename);
        ZipOutputStream out = new ZipOutputStream(fos);
        out = fileToZipAux(file, relativePath, out, outFilename, buildDirectories);
        out.close();
        return (new File(workingDirectory + file.getName() + ".zip"));
    }

    /**
	 *	Recursive function for FileToZip()
	 */
    private static ZipOutputStream fileToZipAux(File file, String relativePath, ZipOutputStream outStream, String outFileName, boolean buildDirectories) throws IOException {
        if (file.isFile()) {
            if (outFileName.compareTo(file.getAbsolutePath()) == 0) {
                return outStream;
            }
            byte[] buf = new byte[DATA_BLOCK_SIZE];
            FileInputStream in = new FileInputStream(file);
            ZipEntry entry = new ZipEntry((buildDirectories ? relativePath + "\\" : "") + file.getName());
            outStream.putNextEntry(entry);
            int len;
            while ((len = in.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }
            outStream.closeEntry();
            in.close();
            return outStream;
        } else {
            File[] children = file.listFiles();
            for (File child : children) {
                fileToZipAux(child, (relativePath == "" ? file.getName() : relativePath + "\\" + file.getName()), outStream, outFileName, buildDirectories);
            }
            return outStream;
        }
    }

    /**
	 *	Decompress the given file to the given path.
	 *
	 * @param file - File Object to decompress.
	 * @param path - Location of the path to save the file's contents in.
	 *
	 * @return A file that points to the decompressed file/directory
	 */
    public static File zipToFile(File file, String path) throws IOException {
        createDirectories(file, path);
        String inFilename = (path + file.getName());
        FileInputStream fis = new FileInputStream(inFilename);
        ZipInputStream in = new ZipInputStream(fis);
        in = zipToFileAux(in.getNextEntry(), path, in);
        in.close();
        return (new File(path + file.getName()));
    }

    /**
	 *	Recursive function for ZipToFile()
	 */
    private static ZipInputStream zipToFileAux(ZipEntry entry, String path, ZipInputStream inStream) throws IOException {
        if (entry == null) return inStream;
        if (entry.isDirectory() == false) {
            String outFilename = entry.getName();
            outFilename = outFilename.replace('/', '\\');
            outFilename = path + outFilename;
            OutputStream out = new FileOutputStream(outFilename);
            byte[] buf = new byte[DATA_BLOCK_SIZE];
            int len;
            while ((len = inStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            return zipToFileAux(inStream.getNextEntry(), path, inStream);
        } else {
            File f = new File(path + "\\" + entry.getName());
            f.mkdir();
            zipToFileAux(inStream.getNextEntry(), path, inStream);
            return inStream;
        }
    }

    /**
	 *	Adds all the required directories in the file in order to allow
	 *	the extraction of the file to the given path.
	 *
	 *	@param file - Zip File Object to add directories from.
	 *	@param path - Location of the path to create directories at.
	 */
    private static void createDirectories(File file, String path) throws IOException {
        String inFilename = (path + file.getName());
        FileInputStream fis = new FileInputStream(inFilename);
        ZipInputStream in = new ZipInputStream(fis);
        in = createDirectoriesAux(in.getNextEntry(), path, in);
        in.close();
    }

    /**
	 *	Recursive function for CreateDirectories()
	 */
    private static ZipInputStream createDirectoriesAux(ZipEntry entry, String path, ZipInputStream inStream) throws IOException {
        if (entry == null) return inStream;
        if (entry.isDirectory()) {
            return createDirectoriesAux(inStream.getNextEntry(), path, inStream);
        } else {
            String tmpPath = entry.getName();
            tmpPath = tmpPath.replace('/', '\\');
            int endIndex = tmpPath.indexOf('\\');
            while (endIndex != -1) {
                String dirToCreate = tmpPath.substring(0, endIndex);
                File f = new File(path + "\\" + dirToCreate);
                f.mkdir();
                endIndex = tmpPath.indexOf("\\", endIndex + 1);
                int x = 10;
                x = x + 1;
            }
            return createDirectoriesAux(inStream.getNextEntry(), path, inStream);
        }
    }

    /**
	 *	private constructor to disallow creation of objects of type ZipManager
	 */
    private ZipManager() {
        throw new AssertionError();
    }

    public static final int DATA_BLOCK_SIZE = 1024;
}
