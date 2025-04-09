package com.myJava.file.archive.zip64;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;
import com.myJava.file.multivolumes.VolumeOutputStream;
import com.myJava.file.multivolumes.VolumeStrategy;
import com.myJava.util.collections.SerializedCollection;
import com.myJava.util.log.Logger;

/**
 * <BR>This class was derived from the original java.util.zip.ZipOutputStream.
 * <BR>The following modifications were made :
 * <BR>- No more control over duplicates entries
 * <BR>- Zip64 / Zip32 support
 * <BR>- Uses SerializedCollections to ensure memory capacity
 * <BR>- Package and name change
 * <BR>- Splitting management
 * <BR>- STORE method was removed
 * <BR>- Use EXT blocks to store data
 * @author Olivier Petrucci 
 * <BR>
 * <BR>CAUTION :
 * <BR>This file has been integrated into Areca.
 * <BR>It is has also possibly been adapted to meet Areca's needs. If such modifications has been made, they are described above.
 * <BR>Thanks to the authors for their work.
 *
 */
public class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {

    private static int SIZE_LOC = SIZE_INT + 3 * SIZE_SHORT + 4 * SIZE_INT + 2 * SIZE_SHORT;

    private static int SIZE_EXT_32 = 4 * SIZE_INT;

    private static int SIZE_EXT_64 = 2 * SIZE_INT + 2 * SIZE_LONG;

    private static int SIZE_CEN = SIZE_INT + 4 * SIZE_SHORT + 4 * SIZE_INT + 5 * SIZE_SHORT + 2 * SIZE_INT;

    private static int SIZE_Z64_END = SIZE_INT + SIZE_LONG + 2 * SIZE_SHORT + 2 * SIZE_INT + 4 * SIZE_LONG + 2 * SIZE_INT + SIZE_LONG + SIZE_INT;

    private static int SIZE_END = SIZE_INT + 4 * SIZE_SHORT + 2 * SIZE_INT + SIZE_SHORT;

    private static long ZIP32_ENTRY_SIZE_LIMIT = 4294967295L;

    private static long ZIP32_OVERALL_SIZE_LIMIT = 4294967295L;

    private static long ZIP32_MAX_ENTRIES = 65535L;

    private static String ZIP32_OVERALL_SIZE_MESSAGE = "Archive too voluminous : Zip32 archives can't grow over " + (long) (ZIP32_OVERALL_SIZE_LIMIT / 1024) + " kbytes. Use Zip64 instead.";

    private ZipEntry entry;

    private SerializedCollection entries;

    private CRC32 crc = new CRC32();

    private long totalWritten;

    private String comment;

    private VolumeStrategy volumeStrategy;

    private int CENStart = 0;

    private int Z64EODRStart = 0;

    private boolean useZip64 = false;

    private List entryCountByDiskNumber = new ArrayList();

    private boolean disableSizeCheck = false;

    private boolean finished = false;

    private boolean opened = false;

    private boolean closed = false;

    private Charset charset = Charset.forName(DEFAULT_CHARSET);

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        if (!opened) {
            opened = true;
            if (this.volumeStrategy != null) {
            }
        }
    }

    public ZipOutputStream(OutputStream out, boolean useZip64) {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        usesDefaultDeflater = true;
        this.useZip64 = useZip64;
        try {
            this.entries = new ZipEntrySerializedCollection();
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalStateException(e);
        }
    }

    public ZipOutputStream(VolumeStrategy strategy, long volumeSize, boolean useZip64) {
        this(new VolumeOutputStream(strategy, volumeSize), useZip64);
        this.volumeStrategy = strategy;
    }

    private void addEntryCount() {
        int volume = this.volumeStrategy == null ? 0 : this.volumeStrategy.getCurrentVolumeNumber();
        while (volume >= this.entryCountByDiskNumber.size()) {
            this.entryCountByDiskNumber.add(new Long(0));
        }
        Long currentCount = (Long) this.entryCountByDiskNumber.get(volume);
        this.entryCountByDiskNumber.set(volume, new Long(currentCount.longValue() + 1));
    }

    private long getEntryCount() {
        int volume = this.volumeStrategy == null ? 0 : this.volumeStrategy.getCurrentVolumeNumber();
        if (volume >= this.entryCountByDiskNumber.size()) {
            return 0;
        } else {
            Long currentCount = (Long) this.entryCountByDiskNumber.get(volume);
            return currentCount.longValue();
        }
    }

    public void setComment(String comment) {
        if (comment != null && comment.length() > 0xffff / 3 && ZipStringEncoder.encode(comment, charset).length > 0xffff) {
            throw new IllegalArgumentException("ZIP file comment too long.");
        }
        this.comment = comment;
    }

    public boolean isUseZip64() {
        return useZip64;
    }

    public void setUseZip64(boolean useZip64) {
        this.useZip64 = useZip64;
    }

    public void setLevel(int level) {
        def.setLevel(level);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    public void putNextEntry(ZipEntry e) throws IOException {
        if ((!useZip64) && this.entries.size() >= ZIP32_MAX_ENTRIES) {
            throw new IOException("Too many files in archive. Zip32 archive format does not allow to store more than " + ZIP32_MAX_ENTRIES + " files.");
        }
        ensureOpen();
        if (entry != null) {
            closeEntry();
        }
        if (e.time == -1) {
            e.setTime(System.currentTimeMillis());
        }
        if (e.getMethod() == -1) {
            e.setMethod(ZipEntry.DEFLATED);
        }
        e.flag = 8;
        if (charset.name().equals(CHARSET_UTF8)) {
            e.flag += 2048;
        }
        if (useZip64) {
            e.version = ZIP64VERSION;
        } else {
            e.version = ZIPVERSION;
        }
        e.offset = volumeStrategy == null ? totalWritten : ((VolumeOutputStream) out).getWrittenInCurrentVolume();
        e.volumeNumber = volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber();
        writeLOC(e);
        entry = e;
    }

    public void closeEntry() throws IOException {
        ensureOpen();
        ZipEntry e = entry;
        if (e != null) {
            def.finish();
            while (!def.finished()) {
                deflate();
            }
            e.setSize(getTotalIn());
            e.csize = getTotalOut();
            e.crc = crc.getValue();
            if ((!useZip64) && e.getSize() > ZIP32_ENTRY_SIZE_LIMIT) {
                throw new IOException(e.name + " is too voluminous (" + (long) (e.getSize() / 1024) + " kbytes). Zip32 archives can't store files bigger than " + (long) (ZIP32_ENTRY_SIZE_LIMIT / 1024) + " kbytes.");
            }
            writeEXT(e);
            resetDeflater();
            totalWritten += e.csize;
            crc.reset();
            entry = null;
            entries.add(e);
        }
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if ((!useZip64) && (len + totalWritten) > ZIP32_OVERALL_SIZE_LIMIT) {
            this.disableSizeCheck = true;
            throw new IOException(ZIP32_OVERALL_SIZE_MESSAGE);
        }
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        if (entry == null) {
            throw new ZipException("no current ZIP entry");
        }
        super.write(b, off, len);
        crc.update(b, off, len);
    }

    public void finish() throws IOException {
        try {
            ensureOpen();
            if (finished) {
                return;
            }
            if (entry != null) {
                closeEntry();
            }
            entries.lock();
            this.CENStart = volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber();
            long offRelativeToCurrentDisk = volumeStrategy == null ? totalWritten : ((VolumeOutputStream) out).getWrittenInCurrentVolume();
            long off = totalWritten;
            Iterator e = entries.iterator();
            while (e.hasNext()) {
                writeCEN((ZipEntry) e.next());
            }
            long cenSize = totalWritten - off;
            writeEND(offRelativeToCurrentDisk, cenSize);
            if ((!disableSizeCheck) && (!useZip64) && totalWritten > ZIP32_OVERALL_SIZE_LIMIT) {
                throw new IOException(ZIP32_OVERALL_SIZE_MESSAGE);
            }
            finished = true;
        } finally {
            try {
                this.entries.clear();
            } catch (Throwable e) {
                Logger.defaultLogger().error(e);
            }
        }
    }

    public void close() throws IOException {
        if (!closed) {
            try {
                super.close();
                closed = true;
                if (this.volumeStrategy != null) {
                    this.volumeStrategy.close();
                }
            } catch (Throwable e) {
                out.close();
                if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
            }
        }
    }

    private long ensureCapacity(long size) throws IOException {
        if (this.volumeStrategy != null) {
            ((VolumeOutputStream) this.out).ensureCapacity(size);
        }
        return this.totalWritten;
    }

    private void checkWritten(long mark, int size) throws IOException {
        if (mark + size != totalWritten) {
            throw new IOException("Inconsistent reserved space : reserved " + size + " - used " + (totalWritten - mark));
        }
    }

    private void writeLOC(ZipEntry e) throws IOException {
        byte[] nameBytes = ZipStringEncoder.encode(e.getName(), charset);
        int size = SIZE_LOC + nameBytes.length;
        long mark = ensureCapacity(size);
        writeInt(LOCSIG);
        writeShort(e.version);
        writeShort(e.flag);
        writeShort(e.getMethod());
        writeInt(e.time);
        writeInt(0);
        if (useZip64) {
            writeInt(ZIP64SIZEFLAG);
            writeInt(ZIP64SIZEFLAG);
        } else {
            writeInt(0);
            writeInt(0);
        }
        writeShort(nameBytes.length);
        writeShort(0);
        writeBytes(nameBytes, 0, nameBytes.length);
        checkWritten(mark, size);
    }

    private void writeEXT(ZipEntry e) throws IOException {
        int size = useZip64 ? SIZE_EXT_64 : SIZE_EXT_32;
        long mark = ensureCapacity(size);
        writeInt(EXTSIG);
        writeInt(e.crc);
        if (useZip64) {
            writeLong(e.csize);
            writeLong(e.getSize());
        } else {
            writeInt(e.csize);
            writeInt(e.getSize());
        }
        checkWritten(mark, size);
    }

    private void writeCEN(ZipEntry e) throws IOException {
        byte[] nameBytes = ZipStringEncoder.encode(e.getName(), charset);
        byte[] commentBytes;
        if (e.getComment() != null) {
            commentBytes = ZipStringEncoder.encode(e.getComment(), charset);
        } else {
            commentBytes = null;
        }
        int size = SIZE_CEN + (nameBytes == null ? 0 : nameBytes.length) + (useZip64 ? 2 * SIZE_SHORT + 3 * SIZE_LONG + SIZE_INT : 0) + (commentBytes == null ? 0 : commentBytes.length);
        long mark = ensureCapacity(size);
        writeInt(CENSIG);
        writeShort(e.version);
        writeShort(e.version);
        writeShort(e.flag);
        writeShort(e.getMethod());
        writeInt(e.time);
        writeInt(e.getCrc());
        if (useZip64) {
            writeInt(ZIP64SIZEFLAG);
            writeInt(ZIP64SIZEFLAG);
        } else {
            writeInt(e.csize);
            writeInt(e.getSize());
        }
        writeShort(nameBytes.length);
        if (useZip64) {
            writeShort(ZIP64XTRALENGTH);
        } else {
            writeShort(0);
        }
        if (commentBytes != null) {
            writeShort(commentBytes.length);
        } else {
            writeShort(0);
        }
        if (useZip64) {
            writeShort(-1);
        } else {
            writeShort(e.volumeNumber);
        }
        writeShort(0);
        writeInt(0);
        if (useZip64) {
            writeInt(-1);
        } else {
            writeInt(e.offset);
        }
        writeBytes(nameBytes, 0, nameBytes.length);
        if (useZip64) {
            writeZip64ExtraField(e);
        }
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, commentBytes.length);
        }
        this.addEntryCount();
        checkWritten(mark, size);
    }

    private void writeZip64ExtraField(ZipEntry e) throws IOException {
        writeShort(ZIP64XTRAFIELD);
        writeShort(ZIP64XTRALENGTH - 4);
        writeLong(e.getSize());
        writeLong(e.csize);
        writeLong(e.offset);
        writeInt(e.volumeNumber);
    }

    private void writeZip64END(long off, long len) throws IOException {
        long cenOffset = volumeStrategy == null ? totalWritten : ((VolumeOutputStream) out).getWrittenInCurrentVolume();
        writeInt(ZIP64ENDSIG);
        writeLong(ZIP64ENDLENGTH);
        writeShort(getVersion());
        writeShort(getVersion());
        writeInt(volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber());
        writeInt(this.CENStart);
        writeLong(this.getEntryCount());
        writeLong(entries.size());
        writeLong(len);
        writeLong(off);
        writeInt(ZIP64ENDLOCSIG);
        writeInt(this.Z64EODRStart);
        writeLong(cenOffset);
        writeInt(volumeStrategy == null ? 1 : volumeStrategy.getVolumesCount());
    }

    private void writeEND(long off, long cenSize) throws IOException {
        byte[] commentBytes = null;
        if (comment != null) {
            commentBytes = ZipStringEncoder.encode(comment, charset);
        }
        int size = SIZE_END + (commentBytes == null ? 0 : commentBytes.length) + (useZip64 ? SIZE_Z64_END : 0);
        long mark = ensureCapacity(size);
        if (useZip64) {
            this.Z64EODRStart = volumeStrategy == null ? 0 : volumeStrategy.getCurrentVolumeNumber();
            writeZip64END(off, cenSize);
            cenSize += SIZE_Z64_END;
        }
        writeInt(ENDSIG);
        writeShort(volumeStrategy == null ? 0 : this.volumeStrategy.getCurrentVolumeNumber());
        writeShort(this.CENStart);
        if (useZip64) {
            writeShort(-1);
            writeShort(-1);
            writeInt(-1);
            writeInt(-1);
        } else {
            writeShort((short) this.getEntryCount());
            writeShort(entries.size());
            writeInt(cenSize);
            writeInt(off);
        }
        if (commentBytes != null) {
            writeShort(commentBytes.length);
            writeBytes(commentBytes, 0, commentBytes.length);
        } else {
            writeShort(0);
        }
        checkWritten(mark, size);
    }

    private int getVersion() throws ZipException {
        return 20;
    }

    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        totalWritten += 2;
    }

    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int) ((v >>> 0) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        totalWritten += 4;
    }

    private void writeLong(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int) ((v >>> 0) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        out.write((int) ((v >>> 32) & 0xff));
        out.write((int) ((v >>> 40) & 0xff));
        out.write((int) ((v >>> 48) & 0xff));
        out.write((int) ((v >>> 56) & 0xff));
        totalWritten += 8;
    }

    private void writeBytes(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        totalWritten += len;
    }
}
