package proguard.io;

import java.io.*;
import java.util.jar.*;
import java.util.*;
import java.util.zip.*;

/**
 * This DataEntryWriter sends data entries to a given jar/zip file.
 * The manifest and comment properties can optionally be set.
 *
 * @author Eric Lafortune
 */
public class JarWriter implements DataEntryWriter, Finisher {

    private DataEntryWriter dataEntryWriter;

    private Manifest manifest;

    private String comment;

    private OutputStream currentParentOutputStream;

    private ZipOutputStream currentJarOutputStream;

    private Finisher currentFinisher;

    private String currentEntryName;

    private Set jarEntryNames = new HashSet();

    /**
     * Creates a new JarWriter without manifest or comment.
     */
    public JarWriter(DataEntryWriter dataEntryWriter) {
        this(dataEntryWriter, null, null);
    }

    /**
     * Creates a new JarWriter.
     */
    public JarWriter(DataEntryWriter dataEntryWriter, Manifest manifest, String comment) {
        this.dataEntryWriter = dataEntryWriter;
        this.manifest = manifest;
        this.comment = comment;
    }

    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException {
        return getOutputStream(dataEntry, null);
    }

    public OutputStream getOutputStream(DataEntry dataEntry, Finisher finisher) throws IOException {
        OutputStream parentOutputStream = dataEntryWriter.getOutputStream(dataEntry.getParent(), this);
        if (parentOutputStream == null) {
            return null;
        }
        if (currentParentOutputStream == null) {
            currentParentOutputStream = parentOutputStream;
            currentJarOutputStream = manifest != null ? new JarOutputStream(parentOutputStream, manifest) : new ZipOutputStream(parentOutputStream);
            if (comment != null) {
                currentJarOutputStream.setComment(comment);
            }
        }
        String name = dataEntry.getName();
        if (!name.equals(currentEntryName)) {
            closeEntry();
            if (!jarEntryNames.add(name)) {
                throw new IOException("Duplicate zip entry [" + dataEntry + "]");
            }
            currentJarOutputStream.putNextEntry(new ZipEntry(name));
            currentFinisher = finisher;
            currentEntryName = name;
        }
        return currentJarOutputStream;
    }

    public void finish() throws IOException {
        if (currentJarOutputStream != null) {
            closeEntry();
            currentJarOutputStream.finish();
            currentJarOutputStream = null;
            currentParentOutputStream = null;
            jarEntryNames.clear();
        }
    }

    public void close() throws IOException {
        dataEntryWriter.close();
    }

    /**
     * Closes the previous ZIP entry, if any.
     */
    private void closeEntry() throws IOException {
        if (currentEntryName != null) {
            if (currentFinisher != null) {
                currentFinisher.finish();
                currentFinisher = null;
            }
            currentJarOutputStream.closeEntry();
            currentEntryName = null;
        }
    }
}
