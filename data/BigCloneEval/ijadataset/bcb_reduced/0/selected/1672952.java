package org.apache.hadoop.fs;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.permission.*;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Shell;

/****************************************************************
 * Implement the FileSystem API for the raw local filesystem.
 *
 *****************************************************************/
public class RawLocalFileSystem extends FileSystem {

    static final URI NAME = URI.create("file:///");

    private Path workingDir;

    TreeMap<File, FileInputStream> sharedLockDataSet = new TreeMap<File, FileInputStream>();

    TreeMap<File, FileOutputStream> nonsharedLockDataSet = new TreeMap<File, FileOutputStream>();

    TreeMap<File, FileLock> lockObjSet = new TreeMap<File, FileLock>();

    public RawLocalFileSystem() {
        workingDir = new Path(System.getProperty("user.dir")).makeQualified(this);
    }

    /** Convert a path to a File. */
    public File pathToFile(Path path) {
        checkPath(path);
        if (!path.isAbsolute()) {
            path = new Path(getWorkingDirectory(), path);
        }
        return new File(path.toUri().getPath());
    }

    /** @deprecated */
    public String getName() {
        return "local";
    }

    public URI getUri() {
        return NAME;
    }

    public void initialize(URI uri, Configuration conf) {
        setConf(conf);
    }

    class TrackingFileInputStream extends FileInputStream {

        public TrackingFileInputStream(File f) throws IOException {
            super(f);
        }

        public int read() throws IOException {
            int result = super.read();
            if (result != -1) {
                statistics.incrementBytesRead(1);
            }
            return result;
        }

        public int read(byte[] data) throws IOException {
            int result = super.read(data);
            if (result != -1) {
                statistics.incrementBytesRead(result);
            }
            return result;
        }

        public int read(byte[] data, int offset, int length) throws IOException {
            int result = super.read(data, offset, length);
            if (result != -1) {
                statistics.incrementBytesRead(result);
            }
            return result;
        }
    }

    /*******************************************************
   * For open()'s FSInputStream
   *******************************************************/
    class LocalFSFileInputStream extends FSInputStream {

        FileInputStream fis;

        private long position;

        public LocalFSFileInputStream(Path f) throws IOException {
            this.fis = new TrackingFileInputStream(pathToFile(f));
        }

        public void seek(long pos) throws IOException {
            fis.getChannel().position(pos);
            this.position = pos;
        }

        public long getPos() throws IOException {
            return this.position;
        }

        public boolean seekToNewSource(long targetPos) throws IOException {
            return false;
        }

        public int available() throws IOException {
            return fis.available();
        }

        public void close() throws IOException {
            fis.close();
        }

        public boolean markSupport() {
            return false;
        }

        public int read() throws IOException {
            try {
                int value = fis.read();
                if (value >= 0) {
                    this.position++;
                }
                return value;
            } catch (IOException e) {
                throw new FSError(e);
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            try {
                int value = fis.read(b, off, len);
                if (value > 0) {
                    this.position += value;
                }
                return value;
            } catch (IOException e) {
                throw new FSError(e);
            }
        }

        public int read(long position, byte[] b, int off, int len) throws IOException {
            ByteBuffer bb = ByteBuffer.wrap(b, off, len);
            try {
                return fis.getChannel().read(bb, position);
            } catch (IOException e) {
                throw new FSError(e);
            }
        }

        public long skip(long n) throws IOException {
            long value = fis.skip(n);
            if (value > 0) {
                this.position += value;
            }
            return value;
        }
    }

    public FSDataInputStream open(Path f, int bufferSize) throws IOException {
        if (!exists(f)) {
            throw new FileNotFoundException(f.toString());
        }
        return new FSDataInputStream(new BufferedFSInputStream(new LocalFSFileInputStream(f), bufferSize));
    }

    /*********************************************************
   * For create()'s FSOutputStream.
   *********************************************************/
    class LocalFSFileOutputStream extends OutputStream implements Syncable {

        FileOutputStream fos;

        private LocalFSFileOutputStream(Path f, boolean append) throws IOException {
            this.fos = new FileOutputStream(pathToFile(f), append);
        }

        public void close() throws IOException {
            fos.close();
        }

        public void flush() throws IOException {
            fos.flush();
        }

        public void write(byte[] b, int off, int len) throws IOException {
            try {
                fos.write(b, off, len);
            } catch (IOException e) {
                throw new FSError(e);
            }
        }

        public void write(int b) throws IOException {
            try {
                fos.write(b);
            } catch (IOException e) {
                throw new FSError(e);
            }
        }

        /** {@inheritDoc} */
        public void sync() throws IOException {
            fos.getFD().sync();
        }
    }

    /** {@inheritDoc} */
    public FSDataOutputStream append(Path f, int bufferSize, Progressable progress) throws IOException {
        if (!exists(f)) {
            throw new FileNotFoundException("File " + f + " not found.");
        }
        if (getFileStatus(f).isDir()) {
            throw new IOException("Cannot append to a diretory (=" + f + " ).");
        }
        return new FSDataOutputStream(new BufferedOutputStream(new LocalFSFileOutputStream(f, true), bufferSize), statistics);
    }

    /** {@inheritDoc} */
    public FSDataOutputStream create(Path f, boolean overwrite, int bufferSize, short replication, long blockSize, Progressable progress) throws IOException {
        if (exists(f) && !overwrite) {
            throw new IOException("File already exists:" + f);
        }
        Path parent = f.getParent();
        if (parent != null && !mkdirs(parent)) {
            throw new IOException("Mkdirs failed to create " + parent.toString());
        }
        return new FSDataOutputStream(new BufferedOutputStream(new LocalFSFileOutputStream(f, false), bufferSize), statistics);
    }

    /** {@inheritDoc} */
    @Override
    public FSDataOutputStream create(Path f, FsPermission permission, boolean overwrite, int bufferSize, short replication, long blockSize, Progressable progress) throws IOException {
        FSDataOutputStream out = create(f, overwrite, bufferSize, replication, blockSize, progress);
        setPermission(f, permission);
        return out;
    }

    public boolean rename(Path src, Path dst) throws IOException {
        if (pathToFile(src).renameTo(pathToFile(dst))) {
            return true;
        }
        return FileUtil.copy(this, src, this, dst, true, getConf());
    }

    @Deprecated
    public boolean delete(Path p) throws IOException {
        return delete(p, true);
    }

    public boolean delete(Path p, boolean recursive) throws IOException {
        File f = pathToFile(p);
        if (f.isFile()) {
            return f.delete();
        } else if ((!recursive) && f.isDirectory() && (f.listFiles().length != 0)) {
            throw new IOException("Directory " + f.toString() + " is not empty");
        }
        return FileUtil.fullyDelete(f);
    }

    public FileStatus[] listStatus(Path f) throws IOException {
        File localf = pathToFile(f);
        FileStatus[] results;
        if (!localf.exists()) {
            return null;
        }
        if (localf.isFile()) {
            return new FileStatus[] { new RawLocalFileStatus(localf, getDefaultBlockSize(), this) };
        }
        String[] names = localf.list();
        if (names == null) {
            return null;
        }
        results = new FileStatus[names.length];
        for (int i = 0; i < names.length; i++) {
            results[i] = getFileStatus(new Path(f, names[i]));
        }
        return results;
    }

    /**
   * Creates the specified directory hierarchy. Does not
   * treat existence as an error.
   */
    public boolean mkdirs(Path f) throws IOException {
        Path parent = f.getParent();
        File p2f = pathToFile(f);
        return (parent == null || mkdirs(parent)) && (p2f.mkdir() || p2f.isDirectory());
    }

    /** {@inheritDoc} */
    @Override
    public boolean mkdirs(Path f, FsPermission permission) throws IOException {
        boolean b = mkdirs(f);
        setPermission(f, permission);
        return b;
    }

    @Override
    public Path getHomeDirectory() {
        return new Path(System.getProperty("user.home")).makeQualified(this);
    }

    /**
   * Set the working directory to the given directory.
   */
    @Override
    public void setWorkingDirectory(Path newDir) {
        workingDir = newDir;
    }

    @Override
    public Path getWorkingDirectory() {
        return workingDir;
    }

    /** @deprecated */
    @Deprecated
    public void lock(Path p, boolean shared) throws IOException {
        File f = pathToFile(p);
        f.createNewFile();
        if (shared) {
            FileInputStream lockData = new FileInputStream(f);
            FileLock lockObj = lockData.getChannel().lock(0L, Long.MAX_VALUE, shared);
            synchronized (this) {
                sharedLockDataSet.put(f, lockData);
                lockObjSet.put(f, lockObj);
            }
        } else {
            FileOutputStream lockData = new FileOutputStream(f);
            FileLock lockObj = lockData.getChannel().lock(0L, Long.MAX_VALUE, shared);
            synchronized (this) {
                nonsharedLockDataSet.put(f, lockData);
                lockObjSet.put(f, lockObj);
            }
        }
    }

    /** @deprecated */
    @Deprecated
    public void release(Path p) throws IOException {
        File f = pathToFile(p);
        FileLock lockObj;
        FileInputStream sharedLockData;
        FileOutputStream nonsharedLockData;
        synchronized (this) {
            lockObj = lockObjSet.remove(f);
            sharedLockData = sharedLockDataSet.remove(f);
            nonsharedLockData = nonsharedLockDataSet.remove(f);
        }
        if (lockObj == null) {
            throw new IOException("Given target not held as lock");
        }
        if (sharedLockData == null && nonsharedLockData == null) {
            throw new IOException("Given target not held as lock");
        }
        lockObj.release();
        if (sharedLockData != null) {
            sharedLockData.close();
        } else {
            nonsharedLockData.close();
        }
    }

    public void moveFromLocalFile(Path src, Path dst) throws IOException {
        rename(src, dst);
    }

    public Path startLocalOutput(Path fsOutputFile, Path tmpLocalFile) throws IOException {
        return fsOutputFile;
    }

    public void completeLocalOutput(Path fsWorkingFile, Path tmpLocalFile) throws IOException {
    }

    public void close() throws IOException {
        super.close();
    }

    public String toString() {
        return "LocalFS";
    }

    public FileStatus getFileStatus(Path f) throws IOException {
        File path = pathToFile(f);
        if (path.exists()) {
            return new RawLocalFileStatus(pathToFile(f), getDefaultBlockSize(), this);
        } else {
            throw new FileNotFoundException("File " + f + " does not exist.");
        }
    }

    static class RawLocalFileStatus extends FileStatus {

        private boolean isPermissionLoaded() {
            return !super.getOwner().equals("");
        }

        RawLocalFileStatus(File f, long defaultBlockSize, FileSystem fs) {
            super(f.length(), f.isDirectory(), 1, defaultBlockSize, f.lastModified(), new Path(f.getPath()).makeQualified(fs));
        }

        @Override
        public FsPermission getPermission() {
            if (!isPermissionLoaded()) {
                loadPermissionInfo();
            }
            return super.getPermission();
        }

        @Override
        public String getOwner() {
            if (!isPermissionLoaded()) {
                loadPermissionInfo();
            }
            return super.getOwner();
        }

        @Override
        public String getGroup() {
            if (!isPermissionLoaded()) {
                loadPermissionInfo();
            }
            return super.getGroup();
        }

        private void loadPermissionInfo() {
            IOException e = null;
            try {
                StringTokenizer t = new StringTokenizer(execCommand(new File(getPath().toUri()), Shell.getGET_PERMISSION_COMMAND()));
                String permission = t.nextToken();
                if (permission.length() > 10) {
                    permission = permission.substring(0, 10);
                }
                setPermission(FsPermission.valueOf(permission));
                t.nextToken();
                setOwner(t.nextToken());
                setGroup(t.nextToken());
            } catch (Shell.ExitCodeException ioe) {
                if (ioe.getExitCode() != 1) {
                    e = ioe;
                } else {
                    setPermission(null);
                    setOwner(null);
                    setGroup(null);
                }
            } catch (IOException ioe) {
                e = ioe;
            } finally {
                if (e != null) {
                    throw new RuntimeException("Error while running command to get " + "file permissions : " + StringUtils.stringifyException(e));
                }
            }
        }

        @Override
        public void write(DataOutput out) throws IOException {
            if (!isPermissionLoaded()) {
                loadPermissionInfo();
            }
            super.write(out);
        }
    }

    /**
   * Use the command chown to set owner.
   */
    @Override
    public void setOwner(Path p, String username, String groupname) throws IOException {
        if (username == null && groupname == null) {
            throw new IOException("username == null && groupname == null");
        }
        if (username == null) {
            execCommand(pathToFile(p), Shell.SET_GROUP_COMMAND, groupname);
        } else {
            String s = username + (groupname == null ? "" : ":" + groupname);
            execCommand(pathToFile(p), Shell.SET_OWNER_COMMAND, s);
        }
    }

    /**
   * Use the command chmod to set permission.
   */
    @Override
    public void setPermission(Path p, FsPermission permission) throws IOException {
        execCommand(pathToFile(p), Shell.SET_PERMISSION_COMMAND, String.format("%04o", permission.toShort()));
    }

    private static String execCommand(File f, String... cmd) throws IOException {
        String[] args = new String[cmd.length + 1];
        System.arraycopy(cmd, 0, args, 0, cmd.length);
        args[cmd.length] = f.getCanonicalPath();
        String output = Shell.execCommand(args);
        return output;
    }
}
