package org.gnu.amSpacks.generate;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.gnu.amSpacks.DefaultLogger;
import org.gnu.amSpacks.Exclude;
import org.gnu.amSpacks.ILogger;
import org.gnu.amSpacks.exception.GeneratorException;
import org.gnu.amSpacks.io.CloseNowZipOutputStream;
import org.gnu.amSpacks.model.TargetVersion;
import org.gnu.amSpacks.model.UpdateInfo;

/** Generates a service pack. */
public class UpdateJarGenerator {

    private static ILogger log = new DefaultLogger();

    static String[] ZIP_EXTENSIONS = { ".zip", ".jar", ".war", ".ear" };

    final File oldApplicationDirectory;

    final File newApplicationDirectory;

    final File outputFile;

    final String oldApplicationDirectoryName;

    final String newApplicationDirectoryName;

    CloseNowZipOutputStream outputStream;

    TreeSet<String> oldFiles = new TreeSet<String>();

    TreeSet<String> unchanged = new TreeSet<String>();

    public UpdateJarGenerator(File _oldApplicationDirectory, File _newApplicationDirectory, File _outputFile) throws Exception {
        oldApplicationDirectory = _oldApplicationDirectory;
        newApplicationDirectory = _newApplicationDirectory;
        if (_outputFile.exists()) {
            _outputFile.delete();
            if (_outputFile.exists()) throw new GeneratorException("Unable to delete existing output file: " + _outputFile.getAbsolutePath());
        }
        outputFile = _outputFile;
        outputStream = new CloseNowZipOutputStream(new BufferedOutputStream(new FileOutputStream(_outputFile)));
        oldApplicationDirectoryName = oldApplicationDirectory.getAbsolutePath();
        newApplicationDirectoryName = newApplicationDirectory.getAbsolutePath();
        if (!oldApplicationDirectory.exists() || !newApplicationDirectory.exists() || !oldApplicationDirectory.isDirectory() || !newApplicationDirectory.isDirectory()) throw new GeneratorException("Old or new installation is missing.");
    }

    public synchronized boolean generate() throws GeneratorException {
        oldFiles.clear();
        try {
            log.log("Checking version information");
            UpdateInfo info = new UpdateInfo();
            info.setFrom(new TargetVersion(oldApplicationDirectory));
            info.setTo(new TargetVersion(newApplicationDirectory));
            log.log("Creating list of old files ");
            scanOld(oldApplicationDirectory);
            log.log("Creating upgrade");
            upgradeFolder(newApplicationDirectory);
            removeFilesFromDeletedArchives();
            int oldRoot = oldApplicationDirectory.getAbsolutePath().length();
            TreeSet<String> ofRelative = new TreeSet<String>();
            Iterator<String> iter = oldFiles.iterator();
            while (iter.hasNext()) {
                String item = iter.next();
                item = item.substring(oldRoot);
                ofRelative.add(item);
            }
            info.setDelete(ofRelative);
            info.writeTo(outputStream);
            outputStream.closeNOW();
            log.log("Files, marked for deletion:");
            iter = ofRelative.iterator();
            while (iter.hasNext()) {
                log.log(iter.next().toString());
            }
            log.log("Done, file size " + outputFile.length() + ", " + outputFile.getAbsolutePath());
            return true;
        } catch (Exception ex) {
            throw new GeneratorException("Unable to generate update jar.", ex);
        }
    }

    private void upgradeFolder(File folder) throws Exception {
        if (Exclude.matches(folder)) {
            log.log("Excluding folder " + folder.getAbsolutePath());
            return;
        }
        assert folder.exists() && folder.isDirectory() : folder.getAbsolutePath() + " expected to be an existing folder.";
        log.log("Upgrading folder " + folder.getAbsolutePath());
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                upgradeFolder(files[i]);
            } else {
                upgradeFile(files[i]);
            }
        }
    }

    private void upgradeFile(File f) throws Exception {
        if (!Exclude.matches(f)) {
            String relative = f.getAbsolutePath().substring(newApplicationDirectoryName.length());
            File old = new File(oldApplicationDirectory, relative);
            oldFiles.remove(old.getAbsolutePath());
            String n = f.getName().toLowerCase();
            if (old.exists() && isZip(n)) {
                upgradeArchive(f, old, relative);
            } else {
                upgradeSimple(f, old, relative);
            }
        } else {
            log.log("EXCLUDING " + f.getAbsolutePath());
        }
    }

    byte[] buf = new byte[80000];

    private void upgradeArchive(File newArchiveFile, File oldArchive, String relativePath) throws IOException {
        if (changed(newArchiveFile, oldArchive)) {
            log.log("Upgrading archive " + newArchiveFile.getAbsolutePath());
            ZipEntry diffEntry = new ZipEntry(relativePath);
            diffEntry.setSize(newArchiveFile.length());
            outputStream.putNextEntry(diffEntry);
            ZipOutputStream diffOutput = new ZipOutputStream(outputStream);
            diffOutput.setLevel(ZipOutputStream.STORED);
            ZipFile newFile = new ZipFile(newArchiveFile);
            ZipFile oldFile = new ZipFile(oldArchive);
            Enumeration<? extends ZipEntry> entries = newFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry newEntry = entries.nextElement();
                oldFiles.remove(archiveFile(oldArchive, newEntry.getName()));
                ZipEntry oldEntry = findInArchive(oldFile, newEntry.getName());
                byte[] newData = readEntry(newFile, newEntry);
                boolean writeToOutput = false;
                if (oldEntry == null) {
                    writeToOutput = true;
                } else {
                    byte[] oldData = readEntry(oldFile, oldEntry);
                    if (newData.length != oldData.length) {
                        writeToOutput = true;
                    } else {
                        for (int i = 0; i < newData.length; i++) {
                            if (newData[i] != oldData[i]) {
                                writeToOutput = true;
                            }
                        }
                    }
                }
                if (writeToOutput) {
                    writeEntryToStream(diffOutput, newEntry, newData);
                }
            }
            newFile.close();
            oldFile.close();
        } else {
            log.log("Unchanged archive " + newArchiveFile.getAbsolutePath());
            unchanged.add(oldArchive.getAbsolutePath());
        }
    }

    private void writeEntryToStream(ZipOutputStream output, ZipEntry entry, byte[] data) throws IOException {
        entry.setMethod(ZipOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        output.putNextEntry(entry);
        output.write(data);
    }

    private byte[] readEntry(ZipFile file, ZipEntry entry) throws IOException {
        InputStream in = file.getInputStream(entry);
        int size = entry.getSize() < 200 ? 200 : (int) entry.getSize();
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        copyInputStream(in, out);
        return out.toByteArray();
    }

    private ZipEntry findInArchive(ZipFile zipfile, String name) {
        Enumeration<? extends ZipEntry> entries = zipfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry z = entries.nextElement();
            if (z.getName().equals(name)) return z;
        }
        return null;
    }

    private void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    private void upgradeSimple(File newFile, File oldFile, String relativePath) throws IOException {
        if (changed(newFile, oldFile)) {
            log.log("Changed " + newFile.getAbsolutePath());
            writeJar(newFile, relativePath);
        } else {
            log.log("Same " + newFile.getAbsolutePath());
            unchanged.add(oldFile.getAbsolutePath());
        }
    }

    private void writeJar(File f, String relativePath) throws IOException {
        ZipEntry entry = new ZipEntry(relativePath);
        outputStream.putNextEntry(entry);
        FileInputStream in = new FileInputStream(f);
        int n;
        while (true) {
            n = in.read(buf);
            if (n <= 0) {
                break;
            }
            outputStream.write(buf, 0, n);
        }
        in.close();
        outputStream.flush();
        outputStream.closeEntry();
    }

    private boolean filesNotIdentical(File left, File right) throws IOException {
        assert left != null;
        assert right != null;
        if (!left.exists() || !right.exists()) return true;
        if (left.length() != right.length()) return true;
        FileInputStream lin = new FileInputStream(left);
        FileInputStream rin = new FileInputStream(right);
        try {
            byte[] lbuffer = new byte[4096];
            byte[] rbuffer = new byte[lbuffer.length];
            for (int lcount = 0; (lcount = lin.read(lbuffer)) > 0; ) {
                int bytesRead = 0;
                for (int rcount = 0; (rcount = rin.read(rbuffer, bytesRead, lcount - bytesRead)) > 0; ) {
                    bytesRead += rcount;
                }
                for (int byteIndex = 0; byteIndex < lcount; byteIndex++) {
                    if (lbuffer[byteIndex] != rbuffer[byteIndex]) return true;
                }
            }
        } finally {
            lin.close();
            rin.close();
        }
        return false;
    }

    private boolean changed(File n, File o) {
        try {
            return filesNotIdentical(n, o);
        } catch (IOException ex) {
            log.log(ex.getMessage());
        }
        return false;
    }

    private void scanOld(File old) throws Exception {
        File[] subFiles = old.listFiles();
        for (int i = 0; i < subFiles.length; i++) {
            File f = subFiles[i];
            if (!Exclude.matches(f)) {
                if (!f.isDirectory()) {
                    {
                        oldFiles.add(f.getAbsolutePath());
                        String n = f.getName().toLowerCase();
                        if (isZip(n)) {
                            scanArchive(f);
                        }
                    }
                } else {
                    scanOld(f);
                }
            }
        }
    }

    private void scanArchive(File archive) throws IOException {
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            oldFiles.add(archiveFile(archive, entry.getName()));
        }
        zipFile.close();
    }

    private String archiveFile(File archive, String name) {
        return archive.getAbsolutePath() + "!" + name;
    }

    private void removeFilesFromDeletedArchives() {
        oldFiles.removeAll(unchanged);
        Iterator<String> iter = oldFiles.iterator();
        int p;
        while (iter.hasNext()) {
            String item = iter.next();
            p = item.indexOf('!');
            if (p > 0) {
                String file = item.substring(0, p);
                if (oldFiles.contains(file) || unchanged.contains(file)) {
                    iter.remove();
                }
            }
        }
    }

    private boolean isZip(String t) {
        for (int i = 0; i < ZIP_EXTENSIONS.length; i++) {
            if (t.endsWith(ZIP_EXTENSIONS[i])) return true;
        }
        return false;
    }
}
