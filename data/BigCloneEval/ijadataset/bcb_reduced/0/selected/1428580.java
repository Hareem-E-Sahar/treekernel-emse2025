package org.xith3d.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.xith3d.utility.logging.X3DLog;

/**
 * An archive is a flexible and high performance storage system for Scribable
 * objects. Objects can be stored and retrieved by name. The archive can grow as
 * needed. Objects can be deleted and/or replaced. You can also get an iterator
 * to step through the objects sequentially. Each object can be stored
 * compressed or uncompressed.
 * 
 * The file is organized as follows: The first entry in the file is a directory
 * block.
 * 
 * Each directory block is 20,000 bytes. We cram as many DirEntry as possible
 * into each 20k chunk. Assuming around an average of 30 bytes per entry this is
 * about 600 entries per block.
 * 
 * Each free block contains two longs, one is the length of the block and the
 * other is a link to the next free block.
 * 
 * @author David Yazel
 */
public class Archive implements Comparator<String> {

    class Block {

        public long loc;

        public long length;
    }

    private static final boolean USE_MAP = true;

    private static final long CLEAN = 876236278;

    private static final int BLOCK_SIZE = 20000;

    private static final long LONG_SIZE = 8;

    private static final long CLEAN_LOC = 0;

    private static final long DIR_START_LOC = CLEAN_LOC + LONG_SIZE;

    private static final long FREE_START_LOC = DIR_START_LOC + LONG_SIZE;

    private static final long EXPAND_START_LOC = FREE_START_LOC + LONG_SIZE;

    private static final long INITIAL_EXPAND_LOC = EXPAND_START_LOC + LONG_SIZE;

    private RandomAccessFile file;

    private TreeMap<String, DirEntry> index = new TreeMap<String, DirEntry>(this);

    private LinkedList<Block> free = new LinkedList<Block>();

    private long freelistLoc = 0;

    private long dirlistLoc = 0;

    private long expandLoc;

    private boolean readOnly;

    private MappedByteBuffer bb;

    /**
     * Creates an Archive object.
     * 
     * @param filename The name of the file
     * @param readonly True if the file should not be written to
     * @throws IOException
     */
    public Archive(String filename, boolean readonly) throws IOException {
        this.readOnly = readonly;
        File f = new File(filename);
        if (!f.exists()) {
            if (readonly) {
                throw new IOException("No such file " + filename);
            }
            file = new RandomAccessFile(filename, "rw");
            initializeFile();
        } else if (readonly && USE_MAP) {
            FileInputStream fs = new FileInputStream(f);
            FileChannel fc = fs.getChannel();
            bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, f.length());
        } else {
            file = new RandomAccessFile(filename, readonly ? "r" : "rw");
        }
        if (readOnly && USE_MAP) {
            readHeaderViaMap();
        } else {
            readHeader();
        }
        if (!readOnly) {
            readFreeList();
        }
        if (readOnly && USE_MAP) {
            readDirectoryViaMap();
        } else {
            readDirectory();
        }
    }

    public boolean exists(String name) {
        DirEntry d = index.get(name);
        X3DLog.debug("Archive search for ", name + " : ", (d != null));
        return (d != null);
    }

    /**
     * Writes the directory, free list and closes the file.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (!readOnly) {
            writeDirectory();
            writeFreeList();
            file.seek(EXPAND_START_LOC);
            file.writeLong(expandLoc);
            file.seek(CLEAN_LOC);
            file.writeLong(CLEAN);
        }
        file.close();
    }

    /**
     * Reads in the header information
     */
    private void readHeader() throws IOException {
        file.seek(CLEAN_LOC);
        long value = file.readLong();
        if (value != CLEAN) {
            throw new Error("Archive is corrupt");
        }
        if (!readOnly) {
            file.seek(CLEAN_LOC);
            file.writeLong(0);
        }
        file.seek(DIR_START_LOC);
        dirlistLoc = file.readLong();
        X3DLog.debug("Directory root = ", dirlistLoc);
        file.seek(FREE_START_LOC);
        freelistLoc = file.readLong();
        X3DLog.debug("Free root = ", freelistLoc);
        file.seek(EXPAND_START_LOC);
        expandLoc = file.readLong();
        X3DLog.debug("Expand loc = ", expandLoc);
    }

    private void readHeaderViaMap() {
        bb.position((int) CLEAN_LOC);
        long value = bb.getLong();
        if (value != CLEAN) {
            throw new Error("Archive is corrupt");
        }
        bb.position((int) DIR_START_LOC);
        dirlistLoc = bb.getLong();
        X3DLog.debug("Directory root = ", dirlistLoc);
        bb.position((int) FREE_START_LOC);
        freelistLoc = bb.getLong();
        X3DLog.debug("Free root = ", freelistLoc);
        bb.position((int) EXPAND_START_LOC);
        expandLoc = bb.getLong();
        X3DLog.debug("Expand loc = ", expandLoc);
    }

    /**
     * Allocates the specified amount of archive space specified
     * 
     * @param size The number of bytes to allocate
     * @throws IOException
     */
    private long allocateSpace(long size) {
        Block minBlock = null;
        for (Block b : free) {
            if (b.length == size) {
                free.remove(b);
                return b.loc;
            } else if (b.length > size) {
                if (minBlock == null) {
                    minBlock = b;
                } else if (minBlock.length > size) {
                    minBlock = b;
                }
            }
        }
        if (minBlock != null) {
            if (minBlock.length < (size * 1.2)) {
                free.remove(minBlock);
                return (minBlock.loc);
            }
        }
        long loc = expandLoc;
        expandLoc += size;
        return (loc);
    }

    /**
     * Initializes the file. This creates a single emptry directory block at the
     * beginning of the file.
     */
    private void initializeFile() throws IOException {
        file.setLength(0);
        file.seek(DIR_START_LOC);
        file.writeLong(0);
        file.seek(FREE_START_LOC);
        file.writeLong(0);
        file.seek(EXPAND_START_LOC);
        file.writeLong(INITIAL_EXPAND_LOC);
        file.seek(CLEAN_LOC);
        file.writeLong(CLEAN);
    }

    /**
     * Writes out the free list to the file. The free list is a linked list of
     * free blocks
     */
    private void writeFreeList() throws IOException {
        X3DLog.debug("Writing free list");
        if (free.size() == 0) {
            file.seek(FREE_START_LOC);
            file.writeLong(0);
        } else {
            Block first = free.getFirst();
            file.seek(FREE_START_LOC);
            file.writeLong(first.loc);
            int n = free.size();
            for (int i = 0; i < n; i++) {
                Block bcur = free.get(i);
                Block next = null;
                if (i < (n - 1)) {
                    next = free.get(i + 1);
                }
                file.seek(bcur.loc);
                file.writeLong(bcur.length);
                if (next == null) {
                    file.writeLong(0);
                } else {
                    file.writeLong(next.loc);
                }
                X3DLog.debug("   bfree block pos=", bcur.loc, ", len = ", bcur.length);
            }
        }
    }

    /**
     * reads the free list into memory. These represent blocks in the file which
     * are available for reuse.
     */
    private void readFreeList() throws IOException {
        X3DLog.debug("Reading free list");
        free.clear();
        long loc = freelistLoc;
        while (loc != 0) {
            file.seek(loc);
            Block b = new Block();
            b.length = file.readLong();
            b.loc = loc;
            loc = file.readLong();
            free.add(b);
            X3DLog.debug("   bfree block pos=", b.loc, ", len = ", b.length, ", next=", loc);
        }
    }

    /**
     * Writes out the directory to the file. The directory is written out in 20k
     * chunks so that many directory entries can be written
     * 
     * @throws IOException
     */
    private void writeDirectory() throws IOException {
        long pad = 10;
        if (index.size() == 0) {
            file.seek(DIR_START_LOC);
            file.writeLong(0);
        } else {
            long loc = allocateSpace(BLOCK_SIZE);
            file.seek(DIR_START_LOC);
            file.writeLong(loc);
            long stopLoc = loc + BLOCK_SIZE;
            file.seek(loc);
            file.writeLong(BLOCK_SIZE);
            for (DirEntry d : index.values()) {
                if ((d.estimateSize() + loc + pad) > stopLoc) {
                    loc = allocateSpace(BLOCK_SIZE);
                    file.writeByte(0);
                    file.writeLong(loc);
                    file.seek(loc);
                    file.writeLong(BLOCK_SIZE);
                    stopLoc = loc + BLOCK_SIZE;
                }
                file.writeByte(1);
                d.write();
                loc = file.getFilePointer();
                if (loc >= stopLoc) {
                    throw new Error("Writing directory exceeded block size");
                }
            }
            file.writeByte(0);
            file.writeLong(0);
        }
    }

    /**
     * Reads in the directory. This will also build a list of directory blocks
     * for using when we write out the directory.
     */
    private void readDirectory() throws IOException {
        X3DLog.debug("Reading directory");
        int num = 0;
        index.clear();
        long loc = dirlistLoc;
        while (loc != 0) {
            file.seek(loc);
            Block b = new Block();
            b.length = file.readLong();
            b.loc = loc;
            free.add(b);
            num++;
            byte marker = file.readByte();
            while (marker == 1) {
                DirEntry d = new DirEntry();
                d.read();
                marker = file.readByte();
                index.put(d.name, d);
            }
            loc = file.readLong();
        }
        X3DLog.debug("  Found ", index.size(), " items in ", num, " block");
    }

    private void readDirectoryViaMap() throws IOException {
        X3DLog.debug("Reading directory");
        int num = 0;
        index.clear();
        long loc = dirlistLoc;
        while (loc != 0) {
            bb.position((int) loc);
            Block b = new Block();
            b.length = bb.getLong();
            b.loc = loc;
            free.add(b);
            num++;
            byte marker = bb.get();
            while (marker == 1) {
                DirEntry d = new DirEntry();
                d.readViaMap();
                marker = bb.get();
                index.put(d.name, d);
            }
            loc = bb.getLong();
        }
        X3DLog.debug("  Found ", index.size(), " items in ", num, " block");
    }

    /**
     * Compares two keys together
     */
    public int compare(String a, String b) {
        return (a.compareTo(b));
    }

    /**
     * Writes out the object to the repository.
     * 
     * @param object
     */
    public void write(String name, Scribable object, boolean compress) throws IOException, UnscribableNodeEncountered {
        DirEntry d = index.get(name);
        if (d != null) {
            Block b = new Block();
            b.length = d.length;
            b.loc = d.pos;
            free.add(b);
        } else {
            d = new DirEntry();
            d.name = name;
            index.put(name, d);
        }
        d.compressed = compress;
        ZipOutputStream zip = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        OutputStream out = bout;
        if (compress) {
            zip = new ZipOutputStream(out);
            zip.putNextEntry(new ZipEntry("object"));
            out = zip;
        }
        ScribeOutputStream sout = new ScribeOutputStream(out);
        if (object == null) {
            System.out.println("Object is null");
        }
        sout.writeScribable(object);
        if (compress) {
            zip.closeEntry();
        }
        out.close();
        byte[] data = bout.toByteArray();
        d.pos = allocateSpace(data.length);
        d.length = data.length;
        file.seek(d.pos);
        file.write(data, 0, data.length);
        X3DLog.debug("Wrote out ", d.length, " bytes for ", name);
    }

    /**
     * Writes out the object to the repository.
     * 
     * @param name
     */
    public Scribable read(String name) throws IOException, InvalidFormat {
        DirEntry d = index.get(name);
        if (d == null) {
            return (null);
        }
        byte[] data = new byte[(int) d.length];
        if (readOnly && USE_MAP) {
            bb.position((int) d.pos);
            bb.get(data);
        } else {
            file.seek(d.pos);
            file.read(data, 0, data.length);
        }
        ZipInputStream zip = null;
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        InputStream in = bin;
        if (d.compressed) {
            zip = new ZipInputStream(in);
            zip.getNextEntry();
            in = zip;
        }
        ScribeInputStream sin = new ScribeInputStream(in);
        Scribable object = sin.readScribable();
        in.close();
        return (object);
    }

    /**
     * Removes an entry from the repository
     * 
     * @param name
     */
    public void remove(String name) {
        DirEntry d = index.get(name);
        if (d != null) {
            Block b = new Block();
            b.length = d.length;
            b.loc = d.pos;
            free.add(b);
            index.remove(d.name);
        }
    }

    private String readUTF() throws IOException {
        int utflen = bb.getShort();
        StringBuffer str = new StringBuffer(utflen);
        byte[] bytearr = new byte[utflen];
        int c;
        int char2;
        int char3;
        int count = 0;
        bb.get(bytearr, 0, utflen);
        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch(c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    count++;
                    str.append((char) c);
                    break;
                case 12:
                case 13:
                    count += 2;
                    if (count > utflen) {
                        UTFDataFormatException ex = new UTFDataFormatException();
                        X3DLog.print(ex);
                        throw ex;
                    }
                    char2 = (int) bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        UTFDataFormatException ex = new UTFDataFormatException();
                        X3DLog.print(ex);
                        throw ex;
                    }
                    str.append((char) (((c & 0x1F) << 6) | (char2 & 0x3F)));
                    break;
                case 14:
                    count += 3;
                    if (count > utflen) {
                        UTFDataFormatException ex = new UTFDataFormatException();
                        X3DLog.print(ex);
                        throw ex;
                    }
                    char2 = (int) bytearr[count - 2];
                    char3 = (int) bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        UTFDataFormatException ex = new UTFDataFormatException();
                        X3DLog.print(ex);
                        throw ex;
                    }
                    str.append((char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
                    break;
                default:
                    UTFDataFormatException ex = new UTFDataFormatException();
                    X3DLog.print(ex);
                    throw ex;
            }
        }
        return (new String(str));
    }

    /**
     * Defines a single directory entry in the file.
     */
    class DirEntry {

        long pos;

        String name;

        long length;

        boolean compressed;

        /**
         * Reads in a directory entry at the current location in the random
         * access file.
         * 
         * @throws IOException
         */
        void read() throws IOException {
            pos = file.readLong();
            name = file.readUTF();
            length = file.readLong();
            compressed = file.readBoolean();
        }

        void readViaMap() throws IOException {
            pos = bb.getLong();
            name = readUTF();
            length = bb.getLong();
            compressed = (bb.get() == 0) ? false : true;
        }

        /**
         * This method writes out the directory entry to the current location in
         * the random access file. It is up to the drectory writer to put the
         * header byte indicating that there is a directory entry.
         * 
         * @throws IOException
         */
        void write() throws IOException {
            file.writeLong(pos);
            file.writeUTF(name);
            file.writeLong(length);
            file.writeBoolean(compressed);
        }

        /**
         * 
         * @return the number of bytes needed to write this directory entry out
         *         to the file.
         */
        long estimateSize() {
            return (name.length() + 4 + 25);
        }
    }
}
