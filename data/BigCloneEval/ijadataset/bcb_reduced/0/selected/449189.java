package org.eledge.domain.importexport.eledge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ExportedCourse {

    private String xml;

    private List<ExportedFile> files;

    private HashMap<String, ExportedFile> fileHash;

    public static final String COURSE_DESCRIPTOR = "course.xml";

    public ExportedCourse(String xml, List<ExportedFile> files) {
        this.xml = xml;
        this.files = files;
        fileHash = new HashMap<String, ExportedFile>();
        for (ExportedFile file : files) {
            fileHash.put(file.getFilename(), file);
        }
    }

    public String getXML() {
        return xml;
    }

    public List<ExportedFile> getFiles() {
        return files;
    }

    public File createArchive(String filename) throws ArchiveFailedException {
        File f = new File(filename);
        try {
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(f));
            zout.putNextEntry(new ZipEntry(COURSE_DESCRIPTOR));
            zout.write(getXML().getBytes());
            zout.closeEntry();
            for (ExportedFile ef : getFiles()) {
                zout.putNextEntry(new ZipEntry(ef.getFilename()));
                zout.write(ef.getContent());
                zout.closeEntry();
            }
            zout.close();
        } catch (Exception e) {
            throw new ArchiveFailedException(e);
        }
        return f;
    }

    public ExportedFile getFile(String name) throws FileNotFoundException {
        if (fileHash.get(name) == null) {
            throw new FileNotFoundException(name);
        }
        return fileHash.get(name);
    }

    public static ExportedCourse createFromArchive(File archive) throws ArchiveFailedException {
        String xml = null;
        List<ExportedFile> files = new ArrayList<ExportedFile>();
        try {
            ZipFile zarchive = new ZipFile(archive);
            Enumeration<? extends ZipEntry> entries = zarchive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String currentEntry = entry.getName();
                InputStream zin = zarchive.getInputStream(entry);
                byte[] buffer = new byte[zin.available()];
                zin.read(buffer);
                if (ExportedCourse.COURSE_DESCRIPTOR.equals(currentEntry)) {
                    xml = new String(buffer);
                } else {
                    files.add(new ExportedFile(currentEntry, buffer));
                }
            }
            if (xml == null) {
                throw new ArchiveFailedException("Supplied archive " + archive.getName() + " is not an eledge course archive: lacks course.xml");
            }
            ExportedCourse ec = new ExportedCourse(xml, files);
            return ec;
        } catch (Exception e) {
            throw new ArchiveFailedException(e);
        }
    }
}
