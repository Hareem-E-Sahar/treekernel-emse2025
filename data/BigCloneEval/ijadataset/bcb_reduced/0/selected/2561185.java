package org.getopt.luke.plugins;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Random;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.lucene.store.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads a Lucene index stored in DFS. This is a modified version of a class
 * with the same purpose found in org.apache.nutch.indexer. */
public class FsDirectory extends Directory {

    private static final Logger LOG = LoggerFactory.getLogger(FsDirectory.class);

    private FileSystem fs;

    private Path directory;

    private int ioFileBufferSize;

    public static class NullReporter implements IOReporter {

        @Override
        public void reportIO(String name, long bytes, boolean read) {
        }

        @Override
        public void reportStatus(String msg) {
        }
    }

    public static final IOReporter NULL_REPORTER = new NullReporter();

    private IOReporter reporter;

    Cache cache;

    public FsDirectory(FileSystem fs, Path directory, boolean create, Configuration conf, IOReporter reporter, int bufSize) throws IOException {
        this.fs = fs;
        this.directory = directory;
        this.reporter = reporter;
        this.ioFileBufferSize = bufSize;
        String diskCacheDir = new File(conf.get("hadoop.tmp.dir"), "fsdir-cache").toString();
        LogManager.getLogger(FsDirectory.class).setLevel(Level.WARN);
        CacheManager mgr = CacheManager.create();
        synchronized (mgr) {
            cache = mgr.getCache("dfsin");
            if (cache == null) {
                cache = new Cache("dfsin", 2000, MemoryStoreEvictionPolicy.LFU, true, diskCacheDir, false, 1800, 600, false, 300, null);
                mgr.addCache(cache);
                cache = mgr.getCache("dfsin");
                LOG.info("Created cache: " + cache.toString());
            }
        }
        if (create) {
            create();
        }
        if (!fs.getFileStatus(directory).isDir()) throw new IOException(directory + " not a directory");
    }

    private void create() throws IOException {
        if (!fs.exists(directory)) {
            fs.mkdirs(directory);
            reporter.reportStatus("Created " + directory);
        }
        if (!fs.getFileStatus(directory).isDir()) throw new IOException(directory + " not a directory");
        FileStatus[] fstats = fs.listStatus(directory);
        Path[] files = FileUtil.stat2Paths(fstats);
        if (files.length > 0) {
            reporter.reportStatus("Cleaning " + files.length + " old files.");
        }
        for (int i = 0; i < files.length; i++) {
            if (!fs.delete(files[i], false)) throw new IOException("Cannot delete " + files[i]);
        }
    }

    public String[] listAll() throws IOException {
        FileStatus[] fstats = fs.listStatus(directory);
        Path[] files = FileUtil.stat2Paths(fstats);
        if (files == null) return null;
        String[] result = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = files[i].getName();
        }
        return result;
    }

    public boolean fileExists(String name) throws IOException {
        return fs.exists(new Path(directory, name));
    }

    public long fileModified(String name) throws IOException {
        return fs.getFileStatus(new Path(directory, name)).getModificationTime();
    }

    public void touchFile(String name) {
        throw new UnsupportedOperationException();
    }

    public long fileLength(String name) throws IOException {
        return fs.getFileStatus(new Path(directory, name)).getLen();
    }

    public void deleteFile(String name) throws IOException {
        if (!fs.delete(new Path(directory, name), false)) throw new IOException("Cannot delete " + name);
    }

    public void renameFile(String from, String to) throws IOException {
        Path target = new Path(directory, to);
        if (fs.exists(target)) {
            fs.delete(target, false);
        }
        fs.rename(new Path(directory, from), target);
    }

    public IndexOutput createOutput(String name, IOContext ctx) throws IOException {
        Path file = new Path(directory, name);
        if (fs.exists(file) && !fs.delete(file, false)) throw new IOException("Cannot overwrite: " + file);
        return new DfsIndexOutput(file, this.ioFileBufferSize);
    }

    public IndexInput openInput(String name, IOContext ctx) throws IOException {
        int bufSize = this.ioFileBufferSize;
        if (name.endsWith(".nrm")) {
            bufSize += bufSize;
        } else if (name.endsWith(".tii")) {
            if (bufSize < 65536) bufSize = 65536;
        }
        return new DfsIndexInput(new Path(directory, name), bufSize, reporter);
    }

    public Lock makeLock(final String name) {
        return new Lock() {

            public boolean obtain() {
                return true;
            }

            public void release() {
            }

            public boolean isLocked() {
                throw new UnsupportedOperationException();
            }

            public String toString() {
                return "Lock@" + new Path(directory, name);
            }
        };
    }

    public synchronized void close() throws IOException {
        fs.close();
    }

    public String toString() {
        return this.getClass().getName() + "@" + directory;
    }

    private class DfsIndexInput extends BufferedIndexInput {

        /** Shared by clones. */
        private class Descriptor {

            public FSDataInputStream in;

            public long position;

            public Descriptor(Path file, int ioFileBufferSize) throws IOException {
                this.in = fs.open(file);
            }
        }

        private final Descriptor descriptor;

        private final long length;

        private boolean isClone;

        private IOReporter reporter;

        private String name;

        private String keyPrefix;

        public DfsIndexInput(Path path, int ioFileBufferSize, IOReporter reporter) throws IOException {
            super(ioFileBufferSize);
            descriptor = new Descriptor(path, ioFileBufferSize);
            length = fs.getFileStatus(path).getLen();
            this.reporter = reporter;
            this.name = path.getName();
            keyPrefix = path.toString() + ":";
        }

        @Override
        public void setBufferSize(int newSize) {
            reporter.reportStatus(name + ": setBufferSize=" + newSize);
            super.setBufferSize(newSize);
        }

        protected void readInternal(byte[] b, int offset, int len) throws IOException {
            synchronized (descriptor) {
                long position = getFilePointer();
                String key = keyPrefix + position + "+" + len;
                Element el = cache.get(key);
                if (el != null) {
                    LOG.info(cache.getStatistics().toString());
                    byte[] data = (byte[]) el.getObjectValue();
                    assert data.length == len;
                    System.arraycopy(data, 0, b, offset, len);
                    reporter.reportIO(name, len, false);
                    return;
                }
                if (position != descriptor.position) {
                    descriptor.in.seek(position);
                    descriptor.position = position;
                }
                int total = 0;
                do {
                    int i = descriptor.in.read(b, offset + total, len - total);
                    reporter.reportIO(name, i, true);
                    if (i == -1) throw new IOException("read past EOF");
                    descriptor.position += i;
                    total += i;
                } while (total < len);
                byte[] data = new byte[len];
                System.arraycopy(b, offset, data, 0, len);
                cache.put(new Element(key, data));
            }
        }

        public void close() throws IOException {
            if (!isClone) {
                descriptor.in.close();
            }
        }

        protected void seekInternal(long position) {
        }

        public long length() {
            return length;
        }

        protected void finalize() throws IOException {
            close();
        }

        public Object clone() {
            DfsIndexInput clone = (DfsIndexInput) super.clone();
            clone.isClone = true;
            return clone;
        }
    }

    private class DfsIndexOutput extends BufferedIndexOutput {

        private FSDataOutputStream out;

        private RandomAccessFile local;

        private File localFile;

        public DfsIndexOutput(Path path, int ioFileBufferSize) throws IOException {
            String randStr = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
            localFile = File.createTempFile("index_" + randStr, ".tmp");
            localFile.deleteOnExit();
            local = new RandomAccessFile(localFile, "rw");
            out = fs.create(path);
        }

        public void flushBuffer(byte[] b, int offset, int size) throws IOException {
            local.write(b, offset, size);
        }

        public void close() throws IOException {
            super.close();
            byte[] buffer = new byte[4096];
            local.seek(0);
            int read = -1;
            while ((read = local.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
            local.close();
        }

        public void seek(long pos) throws IOException {
            super.seek(pos);
            local.seek(pos);
        }

        public long length() throws IOException {
            return local.length();
        }
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
    }
}
