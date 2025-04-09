package gnu.classpath.tools.jar;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

public class Creator extends Action {

    JarOutputStream outputStream;

    HashSet writtenItems = new HashSet();

    Manifest manifest;

    private long copyFile(CRC32 crc, InputStream is, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        long size = 0;
        while (true) {
            int len = is.read(buffer);
            if (len == -1) break;
            size += len;
            output.write(buffer, 0, len);
            crc.update(buffer, 0, len);
        }
        output.close();
        return size;
    }

    protected void writeFile(boolean isDirectory, InputStream inputFile, String filename, boolean verbose) throws IOException {
        if (writtenItems.contains(filename)) {
            if (verbose) {
                String msg = MessageFormat.format(Messages.getString("Creator.Ignoring"), new Object[] { filename });
                System.err.println(msg);
            }
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        long size;
        if (isDirectory) {
            size = 0;
        } else {
            size = copyFile(crc, inputFile, out);
        }
        ZipEntry entry = new ZipEntry(filename);
        entry.setCrc(crc.getValue());
        entry.setSize(size);
        outputStream.putNextEntry(entry);
        out.writeTo(outputStream);
        outputStream.closeEntry();
        writtenItems.add(filename);
        if (verbose) {
            long csize = entry.getCompressedSize();
            long perc;
            if (size == 0) perc = 0; else perc = 100 - (100 * csize) / size;
            String msg = MessageFormat.format(Messages.getString("Creator.Adding"), new Object[] { filename, Long.valueOf(size), Long.valueOf(entry.getSize()), Long.valueOf(perc) });
            System.err.println(msg);
        }
    }

    protected void writeFile(File file, String filename, boolean verbose) throws IOException {
        boolean isDirectory = file.isDirectory();
        InputStream inputStream = null;
        if (isDirectory) {
            if (filename.charAt(filename.length() - 1) != '/') filename += '/';
        } else inputStream = new FileInputStream(file);
        writeFile(isDirectory, inputStream, filename, verbose);
    }

    private void addEntries(ArrayList result, Entry entry) {
        if (entry.file.isDirectory()) {
            String name = entry.name;
            if (name.charAt(name.length() - 1) != '/') {
                name += '/';
                entry = new Entry(entry.file, name);
            }
            result.add(entry);
            String[] files = entry.file.list();
            for (int i = 0; i < files.length; ++i) addEntries(result, new Entry(new File(entry.file, files[i]), entry.name + files[i]));
        } else result.add(entry);
    }

    private ArrayList getAllEntries(Main parameters) {
        Iterator it = parameters.entries.iterator();
        ArrayList allEntries = new ArrayList();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            addEntries(allEntries, entry);
        }
        return allEntries;
    }

    private void writeCommandLineEntries(Main parameters) throws IOException {
        writtenItems.add("META-INF/");
        writtenItems.add(JarFile.MANIFEST_NAME);
        ArrayList allEntries = getAllEntries(parameters);
        Iterator it = allEntries.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            writeFile(entry.file, entry.name, parameters.verbose);
        }
    }

    protected Manifest createManifest(Main parameters) throws IOException {
        if (!parameters.wantManifest) return null;
        if (parameters.manifestFile != null) {
            InputStream contents = new FileInputStream(parameters.manifestFile);
            return new Manifest(contents);
        }
        return new Manifest();
    }

    protected void writeCommandLineEntries(Main parameters, OutputStream os) throws IOException {
        manifest = createManifest(parameters);
        outputStream = new JarOutputStream(os, manifest);
        outputStream.setMethod(parameters.storageMode);
        writeCommandLineEntries(parameters);
    }

    protected void close() throws IOException {
        outputStream.finish();
        outputStream.close();
    }

    public void run(Main parameters) throws IOException {
        if (parameters.archiveFile == null || parameters.archiveFile.equals("-")) writeCommandLineEntries(parameters, System.out); else {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(parameters.archiveFile));
            writeCommandLineEntries(parameters, os);
        }
        close();
    }
}
