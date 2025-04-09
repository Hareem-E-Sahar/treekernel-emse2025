package unclej.utasks.archive;

import unclej.filepath.FileSpec;
import unclej.filepath.Filelike;
import unclej.filepath.WildcardSpec;
import unclej.log.ULog;
import unclej.util.Quote;
import unclej.validate.ValidationException;
import java.io.*;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author scottv
 */
public class ZipUTask extends AbstractArchive {

    public ZipUTask() {
        super();
    }

    public ZipUTask(File outputFile, WildcardSpec fileSpec) {
        super(outputFile, fileSpec);
    }

    protected ZipOutputStream getCompressedStream(OutputStream out) throws IOException {
        return new ZipOutputStream(out);
    }

    protected long writeArchive(ULog log) throws IOException, ValidationException {
        long totalBytes = 0;
        OutputStream out = new BufferedOutputStream(new FileOutputStream(getOutput()));
        ZipOutputStream zip = getCompressedStream(out);
        try {
            for (FileSpec spec : getSpecs()) {
                List<Filelike> matches = spec.listMatches();
                for (Filelike match : matches) {
                    if (match.isFile()) {
                        totalBytes += writeEntry(zip, match, log);
                    } else {
                        writeDirectory(zip, match, log);
                    }
                }
            }
            return totalBytes;
        } finally {
            zip.close();
        }
    }

    private void writeDirectory(ZipOutputStream zip, Filelike entry, ULog log) throws ValidationException, IOException {
        String archiveName = getNameStrategy().getArchiveName(log, entry);
        if (archiveName != null && archiveName.length() > 0) {
            if (!archiveName.endsWith("/")) {
                archiveName += "/";
            }
            ZipEntry ze = new ZipEntry(archiveName);
            log.fine("writing entry {0} (directory)", new Quote(ze.getName()));
            ze.setSize(0);
            ze.setMethod(ZipEntry.STORED);
            ze.setCompressedSize(0);
            ze.setCrc(0);
            zip.putNextEntry(ze);
            zip.closeEntry();
        }
    }

    protected int writeEntry(ZipOutputStream jar, Filelike entry, ULog log) throws IOException, ValidationException {
        String archiveName = getNameStrategy().getArchiveName(log, entry);
        if (archiveName != null && archiveName.length() > 0) {
            ZipEntry ze = new ZipEntry(archiveName);
            log.fine("writing entry {0} ({1} bytes)", new Quote(ze.getName()), SIZE_FORMAT.format(entry.getSize()));
            byte[] bytes = entry.readWhole();
            if (!compressed) {
                ze.setMethod(ZipEntry.STORED);
                ze.setSize(entry.getSize());
                ze.setCompressedSize(entry.getSize());
                CRC32 crc = new CRC32();
                crc.update(bytes);
                ze.setCrc(crc.getValue());
            }
            jar.putNextEntry(ze);
            jar.write(bytes);
            jar.closeEntry();
            return bytes.length;
        } else {
            return 0;
        }
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    private boolean compressed = true;
}
