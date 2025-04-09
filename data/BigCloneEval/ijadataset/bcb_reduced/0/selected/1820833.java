package visitpc.distjar;

import java.io.*;
import java.util.zip.*;
import java.util.jar.*;
import java.util.*;

/**
 * Responsible for jar file manipulation.
 * 
 */
public class JarIt {

    private String fileToExtract;

    private String srcFilename;

    private String destinationPath;

    private boolean FileExtracted;

    private Vector fileList;

    private String allButThisFile;

    private long fileCount;

    private long compressedByteCount;

    private long uncompressedByteCount;

    private long totalCompressedByteCount;

    private long totalUncompressedByteCount;

    private Vector exceptions;

    /**
	 * Constructor
	 * 
	 * @param srcFilename
	 *            The Jar file
	 * @param destinationPath
	 *            The path to decompress the file
	 */
    public JarIt(String srcFilename, String destinationPath) {
        this.srcFilename = srcFilename;
        this.destinationPath = destinationPath;
        fileToExtract = null;
    }

    public JarIt(String srcFilename, String destinationPath, String fileToExtract) throws IOException {
        this.srcFilename = srcFilename;
        this.destinationPath = destinationPath;
        this.fileToExtract = fileToExtract;
    }

    public void decompress() throws IOException {
        FileInputStream fis;
        ZipInputStream zis;
        ZipEntry zEntry;
        StringTokenizer strtok;
        String fileToCreate;
        File absoluteDestPath;
        long totalByteCount;
        File file;
        byte ReadBuffer[] = new byte[32768];
        FileOutputStream fos;
        int bytesReadCount;
        exceptions = new Vector();
        fileCount = 0;
        compressedByteCount = 0;
        uncompressedByteCount = 0;
        totalCompressedByteCount = 0;
        totalUncompressedByteCount = 0;
        String relativeDestPath = "";
        fileList = new Vector();
        FileExtracted = false;
        ZipFile zipFile = new ZipFile(srcFilename);
        Enumeration enumeration = zipFile.entries();
        ZipEntry zipEntry;
        while (enumeration.hasMoreElements()) {
            try {
                zipEntry = (ZipEntry) enumeration.nextElement();
                totalCompressedByteCount += zipEntry.getCompressedSize();
                totalUncompressedByteCount += zipEntry.getSize();
            } catch (Exception e) {
                exceptions.addElement(e);
            }
        }
        zipFile.close();
        fis = new FileInputStream(srcFilename);
        zis = new ZipInputStream(fis);
        zEntry = zis.getNextEntry();
        while (zEntry != null) {
            absoluteDestPath = new File(destinationPath);
            relativeDestPath = "";
            strtok = new StringTokenizer(zEntry.getName(), "/\\");
            if ((zEntry.getName().lastIndexOf("/") == (zEntry.getName().length() - 1)) || (zEntry.getName().lastIndexOf("\\") == (zEntry.getName().length() - 1))) {
                zis.closeEntry();
                zEntry = zis.getNextEntry();
                continue;
            } else {
                if (strtok.countTokens() == 1) {
                    fileToCreate = strtok.nextToken();
                } else {
                    if (strtok.countTokens() > 1) {
                        int numberOfTokens = strtok.countTokens();
                        relativeDestPath = "";
                        for (int i = 0; i < numberOfTokens - 1; i++) {
                            relativeDestPath = relativeDestPath + System.getProperty("file.separator") + strtok.nextToken();
                        }
                        fileToCreate = strtok.nextToken();
                        absoluteDestPath = new File(destinationPath + relativeDestPath);
                    } else {
                        fileToCreate = null;
                    }
                }
            }
            if (((fileToCreate != null) && (fileToCreate.length() > 0)) && ((allButThisFile == null) || ((allButThisFile != null) && !allButThisFile.equals(fileToCreate))) && (fileToExtract == null) || ((fileToExtract != null) && (fileToCreate.equals(fileToExtract)))) {
                file = new File("" + absoluteDestPath);
                if (!file.exists() && file.mkdirs() == false) {
                    throw new IOException("Failed to create the " + file + " directory.");
                }
                file = new File(absoluteDestPath, fileToCreate);
                if (file.isFile()) {
                    throw new IOException("" + file + " already exists.");
                }
                fos = new FileOutputStream(file);
                totalByteCount = 0;
                do {
                    bytesReadCount = zis.read(ReadBuffer);
                    if (bytesReadCount > 0) {
                        fos.write(ReadBuffer, 0, bytesReadCount);
                        totalByteCount += bytesReadCount;
                    }
                } while (bytesReadCount != -1);
                fos.close();
                FileExtracted = true;
                if (relativeDestPath.length() > 0) {
                    fileList.addElement(new File(relativeDestPath, fileToCreate));
                } else {
                    fileList.addElement(new File(fileToCreate));
                }
            }
            zis.closeEntry();
            fileCount++;
            compressedByteCount += zEntry.getCompressedSize();
            uncompressedByteCount += zEntry.getSize();
            zEntry = zis.getNextEntry();
        }
        zis.close();
        fis.close();
        if ((fileToExtract != null) && (FileExtracted == false)) {
            throw new IOException(fileToExtract + " File Not Found in the " + srcFilename + " file.");
        }
    }

    public Vector getfileList() {
        return fileList;
    }

    public void setFilenameNotToExtract(String allButThisFile) {
        this.allButThisFile = allButThisFile;
    }

    public boolean filesExtracted() {
        return FileExtracted;
    }

    /**
	 * Get a list of all exception thrown during the last call to the process
	 * message.
	 * 
	 * @return A Vector containing all the exception objects.
	 */
    public Vector getProcessExceptions() {
        return exceptions;
    }

    /**
	 * Zip the contents of the directory, and save it in the jarfile
	 * 
	 * @param dir
	 * @param jarfile
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
    public void createJar(File dir, String jarfile) throws IOException, IllegalArgumentException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir + " directory not found");
        }
        JarOutputStream jaros = new JarOutputStream(new FileOutputStream(jarfile));
        zipSingleDirectory(dir, jaros, dir);
        jaros.close();
    }

    /**
	 * Zip the contents of a single directory.
	 * 
	 * @param dir
	 * @param out
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
    private void zipSingleDirectory(File dir, JarOutputStream jaros, File topLevelDir) throws IOException, IllegalArgumentException {
        byte[] buffer;
        int bytesRead;
        File[] entries = dir.listFiles();
        CRC32 crc = new CRC32();
        for (File f : entries) {
            if (f.isDirectory()) {
                if (!f.toString().equals(topLevelDir.toString())) {
                    zipSingleDirectory(f, jaros, topLevelDir);
                }
                continue;
            }
            buffer = new byte[(int) f.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            bytesRead = bis.read(buffer);
            bis.close();
            if (bytesRead != f.length()) {
                throw new IOException("%s: failed to read all " + f.length() + " bytes.");
            }
            crc.reset();
            crc.update(buffer, 0, bytesRead);
            String jarEntryName = f.getPath().substring(topLevelDir.toString().length() + 1);
            jarEntryName = jarEntryName.replace('\\', '/');
            JarEntry entry = new JarEntry(jarEntryName);
            entry.setSize(f.length());
            entry.setCrc(crc.getValue());
            entry.setTime(f.lastModified());
            jaros.putNextEntry(entry);
            jaros.write(buffer, 0, bytesRead);
        }
    }

    /**
	 * Get the VisitPC jar file
	 * 
	 * @return
	 */
    public static File GetVisitPCJarFile() throws IOException {
        String userDir = System.getProperty("user.dir");
        File visitPCJarFile = new File(userDir, "visitpc.jar");
        if (!visitPCJarFile.isFile()) {
            throw new IOException(visitPCJarFile + " file not found.");
        }
        return visitPCJarFile;
    }
}
