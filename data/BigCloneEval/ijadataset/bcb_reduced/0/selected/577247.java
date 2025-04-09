package common;

import common.log.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
   Provides a general purpose directory archive/unarchive capability as well
   a general zip archive create/extract capability.
*/
public final class ZipDirectory {

    private static final int BUFFER_SIZE = 50000;

    /** Base directory. */
    private File myDirectory;

    /** Base directory name. */
    private String myDirectoryName;

    /** Base directory name length. */
    private int myDirectoryNameLen;

    /** Archive file. */
    private File myArchive;

    /** Output archive comment. */
    private String myComment;

    /** Output archive zip stream. */
    private ZipOutputStream myOutput = null;

    /** Input archive zip stream. */
    private ZipInputStream myInput = null;

    /** Input/output buffer. */
    private byte[] myBuffer = null;

    /** Error has occurred. */
    private boolean myError = false;

    /**
     Constructor to create a zip archive.

     @param theDirectory the base directory.  All archive entries are in
     this directory tree and relative to this directory.  This constructor
     should be used with:
     <ul>
     <li> a subsequent call to {@link #archive()}, or
     <li> one or more calls to {@link #add(File)} followed by a call to
     {@link #close()}.
     </ul>

     @param theArchive the archive to be created.

     @param theComment the zip archive comment or null.
  */
    public ZipDirectory(File theDirectory, File theArchive, String theComment) {
        myDirectory = theDirectory;
        myArchive = theArchive;
        myDirectoryName = myDirectory.getAbsolutePath();
        myDirectoryNameLen = myDirectoryName.length() + 1;
        myComment = theComment;
        myBuffer = new byte[BUFFER_SIZE];
    }

    /**
     Constructor to extract a zip archive.

     @param theDirectory the base directory.  All archive entries are
     extracted into this directory tree and relative to this directory.
     This constructor should be used with:
     <ul>
     <li> a subsequent call to {@link #unarchive()}, or
     <li> one or more calls to {@link #add(File)} followed by a call to
     {@link #close()}.
     </ul>

     @param theArchive the archive to be extracted.
  */
    public ZipDirectory(File theDirectory, File theArchive) {
        myDirectory = theDirectory;
        myDirectoryName = myDirectory.getAbsolutePath();
        myDirectoryNameLen = myDirectoryName.length() + 1;
        myArchive = theArchive;
        myBuffer = new byte[BUFFER_SIZE];
    }

    /**
     Archives the entire directory tree specified in the constructor into
     the archive specified in the constructor.
  */
    public boolean archive() {
        boolean ok = false;
        if (!myDirectory.exists()) {
            error("directory not found: " + myDirectory.getAbsolutePath());
        } else if (!myDirectory.isDirectory()) {
            error("not a directory: " + myDirectory.getAbsolutePath());
        } else if (myArchive.exists()) {
            error("archive exists: " + myArchive.getAbsolutePath());
        } else if (openOutput()) {
            archive(myDirectory);
            close();
            ok = true;
        }
        return ok;
    }

    /**
     Unarchives the entire directory tree specified in the constructor from
     the archive specified in the constructor.
  */
    public boolean unarchive() {
        boolean ok = false;
        if (myDirectory.exists()) {
            error("directory exists: " + myDirectory.getAbsolutePath());
        } else if (!myArchive.exists()) {
            error("archive not found: " + myArchive.getAbsolutePath());
        } else if (!myArchive.isFile()) {
            error("archive not a file: " + myArchive.getAbsolutePath());
        } else if (openInput()) {
            try {
                myDirectory.mkdir();
                ZipEntry entry = null;
                while ((entry = myInput.getNextEntry()) != null) {
                    String entryName = entry.getName().replace('/', File.separatorChar);
                    File outFile = new File(myDirectoryName + File.separator + entryName);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        outFile.getParentFile().mkdirs();
                        FileOutputStream out = new FileOutputStream(outFile);
                        int numRead = 0;
                        while ((numRead = myInput.read(myBuffer, 0, BUFFER_SIZE)) > 0) {
                            out.write(myBuffer, 0, numRead);
                        }
                        out.close();
                    }
                }
                close();
                ok = true;
            } catch (Exception e) {
                error(e);
                ok = false;
            }
        }
        return ok;
    }

    /**
     Gets the list of entry names in the archive.

     @return the list of names or null if there is an error with the archive.
  */
    public ArrayList<String> getEntryNames() {
        ArrayList<String> list = null;
        if (openInput()) {
            list = new ArrayList<String>();
            try {
                ZipEntry entry = null;
                while ((entry = myInput.getNextEntry()) != null) {
                    list.add(entry.getName());
                    myInput.closeEntry();
                }
                close();
            } catch (Exception e) {
                error(e);
                list = null;
            }
        }
        return list;
    }

    /**
     Extracts the contents of the archive into the directory specified in
     the constructor.  If the target directory does not exist, it will be
     created.

     @return true if the archive was extracted, else return false.
  */
    public boolean extractAll() {
        boolean ok = false;
        if (myDirectory.exists() && !myDirectory.isDirectory()) {
            error("not a directory: " + myDirectory.getAbsolutePath());
        } else if (openInput()) {
            try {
                if (!myDirectory.exists()) {
                    myDirectory.mkdirs();
                }
                ZipEntry entry = null;
                while ((entry = myInput.getNextEntry()) != null) {
                    String entryName = entry.getName().replace('/', File.separatorChar);
                    File outFile = new File(myDirectoryName + File.separator + entryName);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        outFile.getParentFile().mkdirs();
                        FileOutputStream out = new FileOutputStream(outFile);
                        int numRead = 0;
                        while ((numRead = myInput.read(myBuffer, 0, BUFFER_SIZE)) > 0) {
                            out.write(myBuffer, 0, numRead);
                        }
                        out.close();
                    }
                }
                close();
                ok = true;
            } catch (Exception e) {
                error(e);
                ok = false;
            }
        }
        return ok;
    }

    public boolean extract(String theEntryName) {
        boolean ok = false;
        if (myDirectory.exists() && !myDirectory.isDirectory()) {
            error("not a directory: " + myDirectory.getAbsolutePath());
        } else if (openInput()) {
            try {
                if (!myDirectory.exists()) {
                    myDirectory.mkdirs();
                }
                ZipEntry entry = null;
                while ((entry = myInput.getNextEntry()) != null) {
                    boolean extract = false;
                    String name = entry.getName();
                    if (name.equals(theEntryName)) {
                        extract = true;
                    } else if (name.replace('/', File.separatorChar).equals(theEntryName)) {
                        extract = true;
                    }
                    if (!extract) {
                        myInput.closeEntry();
                    } else {
                        String entryName = name.replace('/', File.separatorChar);
                        File outFile = new File(myDirectoryName + File.separator + entryName);
                        if (entry.isDirectory()) {
                            outFile.mkdirs();
                        } else {
                            outFile.getParentFile().mkdirs();
                            FileOutputStream out = new FileOutputStream(outFile);
                            int numRead = 0;
                            while ((numRead = myInput.read(myBuffer, 0, BUFFER_SIZE)) > 0) {
                                out.write(myBuffer, 0, numRead);
                            }
                            out.close();
                        }
                        ok = true;
                    }
                }
                close();
            } catch (Exception e) {
                error(e);
                ok = false;
            }
        }
        return ok;
    }

    /**
     Closes the zip archive.
  */
    public void close() {
        if (myOutput != null) {
            try {
                myOutput.close();
            } catch (Exception e) {
                error(e);
            }
            myOutput = null;
        }
        if (myInput != null) {
            try {
                myInput.close();
            } catch (Exception e) {
                error(e);
            }
            myInput = null;
        }
    }

    public boolean add(File theFile) {
        boolean ok = true;
        if (!openOutput()) {
            ok = false;
        } else if (!theFile.exists()) {
            error("file not found: " + theFile.getAbsolutePath());
            ok = false;
        } else if (theFile.isFile()) {
            ok = addEntry(theFile);
        } else {
            File[] files = theFile.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    if (!addEntry(file)) {
                        ok = false;
                    }
                }
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!add(file)) {
                        ok = false;
                    }
                }
            }
        }
        return ok;
    }

    private boolean addEntry(File theFile) {
        boolean ok = true;
        String fileName = theFile.getAbsolutePath();
        if (IO.IS_WINDOWS) {
            if (!fileName.toUpperCase().startsWith(myDirectoryName.toUpperCase() + File.separator)) {
                ok = false;
            }
        } else if (!fileName.startsWith(myDirectoryName + File.separator)) {
            ok = false;
        }
        if (!ok) {
            error("can't add entry for " + fileName + " for directory " + myDirectoryName);
        } else {
            String entryName = fileName.substring(myDirectoryNameLen).replace(File.separatorChar, '/');
            try {
                ZipEntry entry = new ZipEntry(entryName);
                myOutput.putNextEntry(entry);
                FileInputStream in = new FileInputStream(theFile);
                int numRead = 0;
                while ((numRead = in.read(myBuffer, 0, BUFFER_SIZE)) > 0) {
                    myOutput.write(myBuffer, 0, numRead);
                }
                in.close();
                myOutput.closeEntry();
            } catch (Exception e) {
                error("can't add entry: " + entryName + ", e=" + e.getMessage());
            }
        }
        return ok;
    }

    private void archive(File theDirectory) {
        File[] files = theDirectory.listFiles();
        int numFiles = (files == null) ? 0 : files.length;
        for (int fileIndex = 0; fileIndex < numFiles; fileIndex++) {
            File file = files[fileIndex];
            if (file.isDirectory()) {
                archive(file);
            } else {
                addEntry(file);
            }
        }
    }

    /**
     Opens the output zip stream.

     @return true if the stream was opened or is already opened, else
     return false.
  */
    private boolean openOutput() {
        boolean ok = true;
        if (myError) {
            ok = false;
        } else if (myOutput == null) {
            try {
                myOutput = new ZipOutputStream(new FileOutputStream(myArchive));
                if (myComment != null) {
                    myOutput.setComment(myComment);
                }
            } catch (Exception e) {
                ok = false;
                error(e);
                myOutput = null;
            }
        }
        return ok;
    }

    /**
     Opens the input zip stream.

     @return true if the stream was opened or is already opened, else
     return false.
  */
    private boolean openInput() {
        boolean ok = true;
        if (myError) {
            ok = false;
        } else if (myInput == null) {
            if (!myArchive.exists()) {
                error("archive not found: " + myArchive.getAbsolutePath());
            } else if (!myArchive.isFile()) {
                error("archive not a file: " + myArchive.getAbsolutePath());
            } else {
                try {
                    myInput = new ZipInputStream(new FileInputStream(myArchive));
                } catch (Exception e) {
                    ok = false;
                    error(e);
                    myInput = null;
                }
            }
        }
        return ok;
    }

    private void error(String theMessage) {
        Log.main.println(Log.ERROR, "ZipDirectory: " + theMessage);
        myError = true;
    }

    private void error(Exception theException) {
        Log.main.println("ZipDirectory:", theException);
        myError = true;
    }
}
