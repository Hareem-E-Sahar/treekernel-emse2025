package org.microskills.compress.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.lang.InterruptedException;

/**
 * Manages a zip file.
 * @since ZIPAnywhere 0.1
 */
public class Zip {

    private String strZipFile;

    private String currentArchive;

    protected File zipFile;

    private File baseDir;

    protected Hashtable entries = new Hashtable();

    private Vector groupfilesets = new Vector();

    private Vector filesetsFromGroupfilesets = new Vector();

    protected String duplicate = "add";

    private boolean doCompress = true;

    private boolean doUpdate = false;

    private boolean savedDoUpdate = false;

    private boolean doFilesonly = false;

    protected String archiveType = "zip";

    private static final long EMPTY_CRC = new CRC32().getValue();

    protected String emptyBehavior = "skip";

    private Vector filesets = new Vector();

    protected Hashtable addedDirs = new Hashtable();

    private Vector addedFiles = new Vector();

    /** true when we are adding new files into the Zip file, as opposed to adding back the unchanged files */
    private boolean addingNewFiles = false;

    /** Encoding to use for filenames, defaults to the platform's default encoding. */
    private String encoding;

    public void setCurrentArchive(String currentArchive) {
        this.currentArchive = currentArchive;
    }

    public String getCurrentArchive() {
        return (currentArchive);
    }

    public boolean fileExists() {
        return (new File(currentArchive).exists());
    }

    public void fileDelete() {
        new File(currentArchive).delete();
    }

    public Vector getZipFileData(File file) {
        this.setCurrentArchive(file.getAbsolutePath());
        return (this.getZipFileData());
    }

    /**
     * Retrieves the data ready to be displayed on a table
     * @return The data stored in a vector
     */
    public Vector getZipFileData() {
        Vector vRetVal = new Vector();
        try {
            ZipFile zipFile = new ZipFile(this.getCurrentArchive());
            if (zipFile == null) {
                return (null);
            }
            for (Enumeration eFiles = zipFile.entries(); eFiles.hasMoreElements(); ) {
                ZipEntry zipEntry = (ZipEntry) eFiles.nextElement();
                if (!zipEntry.isDirectory()) {
                    vRetVal.addElement(new ZipEntryInfo(zipEntry.getName(), zipEntry.getSize(), zipEntry.getCompressedSize(), zipEntry.getTime(), zipEntry.getComment(), zipEntry.getMethod(), zipEntry.getCrc(), zipEntry.getExtra()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (vRetVal);
    }

    /** Directory from which to archive files; optional. */
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }

    /** Whether we want to compress the files or only store them; optional, default=true; */
    public void setCompress(boolean c) {
        doCompress = c;
    }

    /** If true, emulate Sun's jar utility by not adding parent directories; optional, defaults to false. */
    public void setFilesonly(boolean f) {
        doFilesonly = f;
    }

    /** If true, updates an existing file, otherwise overwrite any existing one; optional defaults to false. */
    public void setUpdate(boolean c) {
        doUpdate = c;
        savedDoUpdate = c;
    }

    /** Are we updating an existing archive? */
    public boolean isInUpdateMode() {
        return doUpdate;
    }

    /**
     * Encoding to use for filenames, defaults to the platform's default encoding. <p>For a list of possible values see <a
     * href="http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html">http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html</a>.</p>
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Indicates if the task is adding new files into the archive as opposed to
     * copying back unchanged files from the backup copy
     */
    protected boolean isAddingNewFiles() {
        return addingNewFiles;
    }

    /**
     * Create an empty zip file
     * @return true if the file is then considered up to date.
     */
    protected boolean createEmptyZip(File zipFile) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(zipFile);
            byte[] empty = new byte[22];
            empty[0] = 80;
            empty[1] = 75;
            empty[2] = 5;
            empty[3] = 6;
            os.write(empty);
        } catch (IOException ioe) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    protected void zipDir(File dir, ZipOutputStream zOut, String vPath) throws IOException {
        if (addedDirs.get(vPath) != null) {
            return;
        }
        addedDirs.put(vPath, vPath);
        ZipEntry ze = new ZipEntry(vPath);
        if (dir != null && dir.exists()) {
            ze.setTime(dir.lastModified());
        } else {
            ze.setTime(System.currentTimeMillis());
        }
        ze.setSize(0);
        ze.setMethod(ZipEntry.STORED);
        ze.setCrc(EMPTY_CRC);
        zOut.putNextEntry(ze);
    }

    public void extractZipFile(File destination, File[] selectedFiles) {
        String[] sFiles = null;
        for (int i = 0; i < selectedFiles.length; i++) {
            sFiles[i] = selectedFiles[i].getAbsolutePath();
        }
        this.extractZipFile(destination, sFiles);
    }

    public void extractZipFile(File destination, String[] selectedFiles) {
        Extractor extractor = new Extractor(destination, selectedFiles);
        Thread thread = new Thread(extractor);
        thread.start();
    }

    public void createZipFile(File zipFile, File directory, File[] files) throws IOException {
        System.out.println("createZipFile method start");
        Adder adder = new Adder(zipFile, directory, files);
        Thread thread = new Thread(adder);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        System.out.println("createZipFile method end");
    }

    class Adder implements Runnable {

        int baseDirLength;

        int filesAdded;

        int compressMethod;

        int compressLevel;

        File[] files;

        private File zipFile;

        File tempArchiveFile;

        FileOutputStream stream;

        ZipOutputStream zipOutStream;

        byte[] buffer;

        public Adder(File zipFile, File directory, File[] fileList) {
            Zip.this.setCurrentArchive(zipFile.getAbsolutePath());
            this.zipFile = zipFile;
            buffer = new byte[1024];
            filesAdded = 0;
            files = fileList;
            try {
                baseDirLength = directory.getCanonicalPath().length();
                if (directory.getCanonicalPath().endsWith(File.separator)) {
                    baseDirLength--;
                }
            } catch (Exception _ex) {
                System.out.println("Failed in getting baseDir path length!");
            }
        }

        public synchronized void addDir(File directory) {
            try {
                String s = directory.getCanonicalPath().substring(baseDirLength + 1).replace(File.separatorChar, '/') + "/";
                ZipEntry zipentry = new ZipEntry(s);
                zipentry.setTime(directory.lastModified());
                try {
                    zipOutStream.putNextEntry(zipentry);
                } catch (Exception exception) {
                }
            } catch (Exception _ex) {
                System.out.println("Error in adding directory " + directory);
            }
            File[] afile = directory.listFiles();
            for (int i = 0; i < afile.length; i++) {
                if (afile[i].isDirectory()) {
                    addDir(afile[i]);
                } else {
                    addFile(afile[i]);
                }
            }
        }

        public synchronized void addFile(File file) {
            try {
                if (file == null || !file.exists() || file.isDirectory()) {
                    return;
                }
                filesAdded++;
                String s = file.getCanonicalPath().substring(baseDirLength + 1).replace(File.separatorChar, '/');
                ZipEntry zipentry = new ZipEntry(s);
                zipentry.setTime(file.lastModified());
                try {
                    zipOutStream.putNextEntry(zipentry);
                } catch (Exception exception1) {
                    System.out.println(exception1);
                }
                FileInputStream fileinputstream = new FileInputStream(file);
                do {
                    int i = fileinputstream.read(buffer, 0, buffer.length);
                    if (i <= 0) {
                        break;
                    }
                    zipOutStream.write(buffer, 0, i);
                } while (true);
                fileinputstream.close();
            } catch (Exception exception) {
                System.out.println("Error in adding " + file + ":" + exception);
            }
        }

        public void run() {
            try {
                File tempFile = File.createTempFile("ZIPAnywhere", ".tmp");
                zipOutStream = new ZipOutputStream(new FileOutputStream(tempFile));
                if (zipFile.length() != 0) {
                    ZipFile newZip = new ZipFile(zipFile);
                    for (Enumeration eZip = newZip.entries(); eZip.hasMoreElements(); ) {
                        ZipEntry entry = (ZipEntry) eZip.nextElement();
                        try {
                            zipOutStream.putNextEntry(entry);
                            InputStream inputStream = newZip.getInputStream(entry);
                            do {
                                int j = inputStream.read(buffer, 0, buffer.length);
                                if (j <= 0) {
                                    break;
                                }
                                zipOutStream.write(buffer, 0, j);
                            } while (true);
                            inputStream.close();
                        } catch (IOException e) {
                            continue;
                        }
                    }
                    newZip.close();
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        addDir(files[i]);
                    } else {
                        addFile(files[i]);
                    }
                }
                zipOutStream.close();
                String filename = zipFile.getAbsolutePath().replace(File.separatorChar, '/');
                if (zipFile.exists()) {
                    new File(filename).delete();
                }
                tempFile.renameTo(new File(filename));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    class Extractor implements Runnable {

        private File destination;

        private String[] selectedFiles;

        byte[] buf;

        public Extractor(File destination, String[] selectedFiles) {
            this.destination = destination;
            this.selectedFiles = selectedFiles;
            buf = new byte[1024];
        }

        public void run() {
            try {
                ZipFile zipfile = new ZipFile(currentArchive);
                for (int i = 0; i < selectedFiles.length; i++) {
                    ZipEntry entry = zipfile.getEntry(selectedFiles[i].replace(File.separatorChar, '/'));
                    InputStream inputstream = zipfile.getInputStream(entry);
                    FileOutputStream fileoutputstream = new FileOutputStream(destination.getAbsolutePath().replace(File.separatorChar, '/') + "/" + selectedFiles[i].replace(File.separatorChar, '/'));
                    do {
                        int j = inputstream.read(buf, 0, buf.length);
                        if (j <= 0) {
                            break;
                        }
                        fileoutputstream.write(buf, 0, j);
                    } while (true);
                    fileoutputstream.close();
                    zipfile.close();
                }
            } catch (Exception exception1) {
                exception1.printStackTrace();
            } finally {
            }
        }
    }
}
