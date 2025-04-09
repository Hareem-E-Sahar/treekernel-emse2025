package wizworld.util.zip;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.*;

/** Add and extract zip archive files
 * @author (c) Stephen Denham 2002
 * @version 0.1
 */
public final class Zip {

    private FileOutputStream fileOut;

    private ZipOutputStream zipOut;

    /** Opens archive for write
   * @param  archive	Archive to open
   * @return Zip out stream
   * @exception IOException if cannot access archive
   */
    public ZipOutputStream writeArchive(String archive) throws IOException {
        this.fileOut = new FileOutputStream(new File(archive));
        this.zipOut = new ZipOutputStream(fileOut);
        return this.zipOut;
    }

    /** Close archive
   * @exception IOException if cannot access archive
   */
    public void closeArchive() throws IOException {
        this.zipOut.flush();
        this.zipOut.close();
        this.fileOut.close();
    }

    /** List contents of archive
   * @param  archive	Archive to list
   * @param  list	Empty vector to add entries to
   * @exception IOException if cannot access archive
   */
    public void listArchive(String archive, Vector list) throws IOException {
        list.setSize(0);
        ZipFile zipFile = new ZipFile(archive);
        for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (!entry.isDirectory()) {
                list.addElement(entry);
            }
        }
    }

    /** Add path to archive
   * @param  pathName	Absolute pathname to directory or file to archive
   * @param  recurse	Recurse directories
   * @param  pathnames	Use files full path
   * @return Added or not added
   * @exception IOException if cannot access archive
   */
    public boolean putPath(String pathName, boolean recurse, boolean pathnames) throws IOException {
        boolean added = false;
        File dirObj = new File(pathName);
        if (dirObj.canRead() == true) {
            if (dirObj.isDirectory() == true) {
                String[] fileList = dirObj.list();
                for (int i = 0; i < fileList.length; i++) {
                    if (pathName.endsWith(File.separator)) {
                        fileList[i] = pathName + fileList[i];
                    } else {
                        fileList[i] = pathName + File.separator + fileList[i];
                    }
                    dirObj = new File(fileList[i]);
                    if (dirObj.isDirectory() && recurse) {
                        added = putPath(fileList[i], recurse, pathnames);
                    } else if (dirObj.isFile()) {
                        added = putFile(fileList[i], pathnames);
                    } else {
                        added = true;
                    }
                }
            } else if (dirObj.isFile()) {
                added = putFile(dirObj.getPath(), pathnames);
            }
            if (!added) {
                throw new IOException("Failed to add " + dirObj.getPath() + " to archive");
            }
        }
        return added;
    }

    /** Add single file to archive
   * @param  filePath Absolute pathname file to archive
   * @param  pathnames	Use files full path
   * @return Added or not added
   * @exception IOException if cannot access archive
   */
    private boolean putFile(String filePath, boolean pathnames) throws IOException {
        File file = new File(filePath);
        FileInputStream fIn = new FileInputStream(file);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        ZipEntry fileEntry = null;
        if (pathnames) {
            int rootSeparator = filePath.indexOf(System.getProperty("file.separator"));
            fileEntry = new ZipEntry(filePath.substring(rootSeparator + 1));
        } else {
            String fileName = file.getName();
            int rootSeparator = fileName.indexOf(System.getProperty("file.separator"));
            fileEntry = new ZipEntry(fileName.substring(rootSeparator + 1));
        }
        this.zipOut.putNextEntry(fileEntry);
        int bufferSize = 1024;
        byte[] data = new byte[bufferSize];
        int byteCount;
        while ((byteCount = bIn.read(data, 0, bufferSize)) > -1) {
            this.zipOut.write(data, 0, byteCount);
        }
        this.zipOut.closeEntry();
        return true;
    }

    /** Get single file from archive
   * @param  archive	Archive containing file
   * @param  filePath	File to extract
   * @param  pathnames	Use files full path
   * @param	destination	Destination directory
   * @return True if successful, else false
   * @exception IOException if cannot access archive
   */
    public boolean getFile(String archive, String filePath, String destination, boolean pathnames) throws IOException {
        ZipFile zipFile = new ZipFile(archive);
        String file = null;
        file = filePath;
        char localSeparator = System.getProperty("file.separator").charAt(0);
        if (file.indexOf(localSeparator) == -1) {
            if (localSeparator == '/') {
                file = filePath.replace('\\', '/');
            } else {
                file = filePath.replace('/', '\\');
            }
        }
        File path = new File(destination, file);
        if (!pathnames) {
            path = new File(destination, path.getName());
        } else {
            String parent = path.getParent();
            if (parent != null) {
                File directory = new File(parent);
                directory.mkdirs();
            }
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path);
        } catch (FileNotFoundException fne) {
            return false;
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream zis = zipFile.getInputStream(zipFile.getEntry(filePath));
        int bufferSize = 1024;
        byte[] data = new byte[bufferSize];
        int byteCount;
        try {
            while ((byteCount = zis.read(data, 0, bufferSize)) > -1) {
                fos.write(data, 0, byteCount);
            }
        } catch (IOException ioe) {
        } finally {
            bos.close();
            fos.flush();
            fos.close();
            zis.close();
        }
        return true;
    }
}
