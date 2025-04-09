package org.makagiga.commons;

import static org.makagiga.commons.UI._;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @since 2.0
 */
public class MZip implements Closeable, Iterable<ZipEntry> {

    public static final int UNPACK_VALIDATE_ENTRY = 1 << 0;

    protected ZipInputStream zipInput;

    protected ZipOutputStream zipOutput;

    public ZipEntry addEntry(final String name, final File file) throws IOException {
        FS.BufferedFileInput input = null;
        try {
            input = new FS.BufferedFileInput(file);
            return addEntry(name, input);
        } finally {
            FS.close(input);
        }
    }

    public ZipEntry addEntry(final String name, final InputStream input) throws IOException {
        ZipEntry result = beginEntry(name);
        copyToEntry(input);
        return result;
    }

    public ZipEntry addEntry(final String name, final String path) throws IOException {
        return addEntry(name, new File(path));
    }

    public ZipEntry beginEntry(final String name) throws IOException {
        ZipEntry result = new ZipEntry(name);
        zipOutput.putNextEntry(result);
        return result;
    }

    public void close() throws IOException {
        if (zipInput != null) zipInput.close();
        if (zipOutput != null) zipOutput.close();
    }

    public void copyEntryTo(final File file) throws IOException {
        FS.BufferedFileOutput output = null;
        try {
            output = new FS.BufferedFileOutput(file);
            copyEntryTo(output);
        } finally {
            FS.close(output);
        }
    }

    public void copyEntryTo(final OutputStream output) throws IOException {
        FS.copyStream(zipInput, output);
    }

    public void copyEntryTo(final String path) throws IOException {
        copyEntryTo(new File(path));
    }

    public void copyToEntry(final InputStream input) throws IOException {
        try {
            FS.copyStream(input, zipOutput);
        } finally {
            zipOutput.closeEntry();
        }
    }

    public ZipInputStream getInputStream() {
        return zipInput;
    }

    public ZipOutputStream getOutputStream() {
        return zipOutput;
    }

    public Iterator<ZipEntry> iterator() {
        return new Iterator<ZipEntry>() {

            private ZipEntry nextZipEntry;

            public boolean hasNext() {
                try {
                    nextZipEntry = zipInput.getNextEntry();
                    return nextZipEntry != null;
                } catch (IOException exception) {
                    MLogger.exception(exception);
                    return false;
                }
            }

            public ZipEntry next() {
                return nextZipEntry;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static MZip read(final File file) throws FileNotFoundException {
        return new MZip(new FS.BufferedFileInput(file));
    }

    public static MZip read(final InputStream input) {
        return new MZip(input);
    }

    public static MZip read(final String path) throws FileNotFoundException {
        return new MZip(new FS.BufferedFileInput(path));
    }

    public void unpackTo(final String dir) throws IOException {
        unpackTo(dir, 0);
    }

    public void unpackTo(final String dir, final int flags) throws IOException {
        Flags f = new Flags(flags);
        String entryName;
        String outputPath;
        for (ZipEntry i : this) {
            entryName = i.getName();
            entryName = entryName.replace('/', File.separatorChar);
            if (f.isSet(UNPACK_VALIDATE_ENTRY) && (entryName.startsWith(File.separator) || entryName.contains(".." + File.separator))) throw new IOException("Zip entry name contains unsafe characters");
            outputPath = FS.makePath(dir, entryName);
            if (i.isDirectory()) {
                if (!FS.mkdirs(outputPath) && !FS.exists(outputPath)) throw new IOException(_("Could not create \"{0}\" directory", outputPath));
            } else {
                copyEntryTo(outputPath);
            }
        }
    }

    public static MZip write(final File file) throws FileNotFoundException {
        return new MZip(new FS.BufferedFileOutput(file));
    }

    public static MZip write(final OutputStream output) {
        return new MZip(output);
    }

    public static MZip write(final String path) throws FileNotFoundException {
        return new MZip(new FS.BufferedFileOutput(path));
    }

    protected MZip(final InputStream input) {
        zipInput = new ZipInputStream(input);
    }

    protected MZip(final OutputStream output) {
        zipOutput = new ZipOutputStream(output);
    }
}
