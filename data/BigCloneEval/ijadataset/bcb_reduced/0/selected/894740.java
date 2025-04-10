package org.opendedup.sdfs.filestore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import org.bouncycastle.util.Arrays;
import org.opendedup.hashing.AbstractHashEngine;
import org.opendedup.hashing.Tiger16HashEngine;
import org.opendedup.hashing.TigerHashEngine;
import org.opendedup.sdfs.Main;
import org.opendedup.util.EncryptUtils;
import org.opendedup.util.SDFSLogger;
import org.w3c.dom.Element;

/**
 * 
 * @author Sam Silverberg This chunk store saves chunks into a single contiguous
 *         file. It performs well on most file systems. This chunkstore is used
 *         by default. The chunkstore is called by the hashstore to save or
 *         retrieve deduped chunks. Chunk block meta data is as follows: [mark
 *         of deletion (1 byte)|hash lenth(2 bytes)|hash(32 bytes)|date added (8
 *         bytes)|date last accessed (8 bytes)| chunk len (4 bytes)|chunk
 *         position (8 bytes)]
 **/
public class FileChunkStore implements AbstractChunkStore {

    private static final int pageSize = Main.chunkStorePageSize;

    private boolean closed = false;

    private FileChannel fc = null;

    private RandomAccessFile chunkDataWriter = null;

    private static File chunk_location = new File(Main.chunkStore);

    File f;

    Path p;

    private long currentLength = 0L;

    private String name;

    private byte[] FREE = new byte[pageSize];

    private FileChannel iterFC = null;

    private AbstractHashEngine hc = null;

    /**
	 * 
	 * @param name
	 *            the name of the chunk store.
	 */
    public FileChunkStore() {
        SDFSLogger.getLog().info("Opening Chunk Store");
        Arrays.fill(FREE, (byte) 0);
        try {
            if (!chunk_location.exists()) {
                chunk_location.mkdirs();
            }
            f = new File(chunk_location + File.separator + "chunks.chk");
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            this.name = "chunks";
            p = f.toPath();
            chunkDataWriter = new RandomAccessFile(f, "rw");
            this.currentLength = chunkDataWriter.length();
            this.closed = false;
            fc = chunkDataWriter.getChannel();
            SDFSLogger.getLog().info("ChunkStore " + f.getPath() + " created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeStore() {
        try {
            fc.force(true);
        } catch (Exception e) {
        }
        try {
            fc.close();
        } catch (Exception e) {
        }
        try {
            this.iterFC.close();
        } catch (Exception e) {
        }
        this.iterFC = null;
        try {
            this.hc.destroy();
        } catch (Exception e) {
        }
        try {
            this.chunkDataWriter.close();
        } catch (Exception e) {
        }
        this.chunkDataWriter = null;
        fc = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long size() {
        return this.currentLength;
    }

    public long bytesRead() {
        return this.bytesRead();
    }

    public long bytesWritten() {
        return this.bytesWritten();
    }

    private static ReentrantLock reservePositionlock = new ReentrantLock();

    public long reserveWritePosition(int len) throws IOException {
        if (this.closed) throw new IOException("ChunkStore is closed");
        reservePositionlock.lock();
        long pos = this.currentLength;
        this.currentLength = this.currentLength + pageSize;
        reservePositionlock.unlock();
        return pos;
    }

    public void claimChunk(byte[] hash, long pos) throws IOException {
    }

    public void writeChunk(byte[] hash, byte[] chunk, int len, long start) throws IOException {
        if (this.closed) throw new IOException("ChunkStore is closed");
        ByteBuffer buf = null;
        try {
            if (Main.chunkStoreEncryptionEnabled) chunk = EncryptUtils.encrypt(chunk);
            buf = ByteBuffer.wrap(chunk);
            fc.write(buf, start);
        } catch (Exception e) {
            SDFSLogger.getLog().fatal("unable to write data at position " + start, e);
            throw new IOException("unable to write data at position " + start);
        } finally {
            buf = null;
            hash = null;
            chunk = null;
            len = 0;
            start = 0;
        }
    }

    @Override
    public byte[] getChunk(byte[] hash, long start, int len) throws IOException {
        if (this.closed) throw new IOException("ChunkStore is closed");
        ByteBuffer fbuf = ByteBuffer.wrap(new byte[pageSize]);
        try {
            fc.read(fbuf, start);
        } catch (Exception e) {
            SDFSLogger.getLog().error("unable to fetch chunk at position " + start, e);
            throw new IOException(e);
        } finally {
            try {
            } catch (Exception e) {
            }
        }
        return fbuf.array();
    }

    @Override
    public void deleteChunk(byte[] hash, long start, int len) throws IOException {
        if (this.closed) throw new IOException("ChunkStore is closed");
    }

    public void close() {
        try {
            this.closed = true;
            this.closeStore();
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            raf.getChannel().force(true);
        } catch (Exception e) {
            SDFSLogger.getLog().warn("while closing filechunkstore ", e);
        }
    }

    @Override
    public boolean moveChunk(byte[] hash, long origLoc, long newLoc) throws IOException {
        if (this.closed) throw new IOException("ChunkStore is closed");
        byte[] buf = new byte[Main.chunkStorePageSize];
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        try {
            raf.seek(origLoc);
            raf.read(buf);
            if (Main.preAllocateChunkStore && Arrays.areEqual(FREE, buf)) return false;
            raf.seek(newLoc);
            raf.write(buf);
            if (!Main.preAllocateChunkStore && (origLoc + Main.chunkStorePageSize) == raf.length()) raf.setLength(origLoc);
            return true;
        } catch (Exception e) {
            SDFSLogger.getLog().fatal("could not move data from [" + origLoc + "] to [" + newLoc + "]", e);
            return false;
        } finally {
            raf.close();
            raf = null;
            buf = null;
        }
    }

    @Override
    public void init(Element config) {
    }

    @Override
    public void setSize(long size) {
    }

    @Override
    public ChunkData getNextChunck() throws IOException {
        if (iterFC.position() >= iterFC.size()) {
            iterFC.close();
            return null;
        }
        ByteBuffer fbuf = ByteBuffer.wrap(new byte[pageSize]);
        long pos = -1;
        try {
            pos = iterFC.position();
            iterFC.read(fbuf);
        } catch (Exception e) {
            iterFC.close();
            SDFSLogger.getLog().error("unable to fetch chunk at position " + iterFC.position(), e);
            throw new IOException(e);
        }
        if (pos != -1) {
            byte[] hash = hc.getHash(fbuf.array());
            return new ChunkData(hash, pos);
        } else {
            iterFC.close();
            return null;
        }
    }

    private ReentrantLock iterlock = new ReentrantLock();

    public void iterationInit() throws IOException {
        this.iterlock.lock();
        try {
            if (Main.hashLength == 16) {
                hc = new Tiger16HashEngine();
            } else {
                hc = new TigerHashEngine();
            }
            this.iterFC = new RandomAccessFile(f, "r").getChannel();
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            this.iterlock.unlock();
        }
    }

    @Override
    public void addChunkStoreListener(AbstractChunkStoreListener listener) {
    }
}
