package net.sf.ngrease.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import fuse.Errno;
import fuse.FuseException;
import fuse.FuseStatfs;
import fuse.compat.Filesystem1;
import fuse.compat.FuseDirEnt;
import fuse.compat.FuseStat;

public class FuseNgreaseFilesystem1 extends NgreaseFilesystem implements Filesystem1 {

    private final PathResolver pathResolver;

    private File activeMntdir;

    private final File fusermount;

    private boolean getdirHasBeenCalled;

    private static void checkFusermount(File fusermount) {
        if (!fusermount.exists()) {
            throw new ValidFusermountNotFoundException("The given fusermount does not exist:" + fusermount);
        }
        if (!fusermount.isFile()) {
            throw new ValidFusermountNotFoundException("The given fusermount is not a file:" + fusermount);
        }
    }

    private static void checkMntdir(File mntdir) {
        if (!mntdir.exists()) {
            throw new CanOnlyMountToDirectoryException("The given mntdir does not exist:" + mntdir.getAbsolutePath());
        }
        if (!mntdir.isDirectory()) {
            throw new CanOnlyMountToDirectoryException("The given mntdir is not a directory:" + mntdir.getAbsolutePath());
        }
    }

    FuseNgreaseFilesystem1(FilesystemElementSource source, File mntdir, File fusermount) {
        checkMntdir(mntdir);
        checkFusermount(fusermount);
        this.pathResolver = new PathResolver(source);
        this.activeMntdir = mntdir;
        this.fusermount = fusermount;
    }

    /**
	 * @return the given mntdir if mounted and null if unmounted.
	 */
    public File getActiveMountdir() {
        return activeMntdir;
    }

    public void unmount() {
        if (activeMntdir == null) {
            throw new AlreadyUnmountedException("Already unmounted.");
        }
        System.out.println("unmounting " + activeMntdir);
        Process unmountProc;
        try {
            unmountProc = Runtime.getRuntime().exec(fusermount + " -u " + activeMntdir);
            InputStream err = unmountProc.getErrorStream();
            InputStream out = unmountProc.getInputStream();
            int unmountExitCode = unmountProc.waitFor();
            if (unmountExitCode != 0) {
                dump(System.out, out);
                dump(System.err, err);
                throw new UnmountFailedException(fusermount + " exited with exit code " + unmountExitCode);
            }
            activeMntdir = null;
        } catch (IOException e) {
            throw new UnmountFailedException(e);
        } catch (InterruptedException e) {
            throw new UnmountFailedException(e);
        }
    }

    private static void dump(PrintStream dest, InputStream src) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(src));
        String line;
        while ((line = reader.readLine()) != null) {
            dest.println("fusermount: " + line);
        }
    }

    synchronized boolean hasGetdirBeenCalled() {
        return getdirHasBeenCalled;
    }

    private synchronized void getdirHasBeenCalled() {
        getdirHasBeenCalled = true;
    }

    public void chmod(String path, int mode) throws FuseException {
    }

    public void chown(String path, int uid, int gid) throws FuseException {
    }

    public FuseStat getattr(String path) throws FuseException {
        System.out.println("getattr called for " + path);
        Node node = resolvePath(path);
        return node.getFuseStat();
    }

    public FuseDirEnt[] getdir(String path) throws FuseException {
        System.out.println("getdir called for " + path);
        getdirHasBeenCalled();
        Node node = resolvePath(path);
        if (!(node instanceof DirectoryNode)) {
            throw new FuseException("Not a directory:" + path).initErrno(Errno.ENOTDIR);
        }
        DirectoryNode directoryNode = (DirectoryNode) node;
        List children = directoryNode.getChildren();
        FuseDirEnt[] ents = new FuseDirEnt[children.size()];
        int i = 0;
        for (Iterator iterator = children.iterator(); iterator.hasNext(); i++) {
            Node child = (Node) iterator.next();
            FuseDirEnt childEnt = child.asFuseDirEnt();
            ents[i] = childEnt;
        }
        return ents;
    }

    private Node resolvePath(String path) throws FuseException {
        return pathResolver.resolve(path);
    }

    public void link(String from, String to) throws FuseException {
    }

    public void mkdir(String path, int mode) throws FuseException {
    }

    public void mknod(String path, int mode, int rdev) throws FuseException {
    }

    public void open(String path, int flags) throws FuseException {
    }

    public void read(String path, ByteBuffer buf, long offset) throws FuseException {
        System.out.println("read called for " + path);
        Node node = resolvePath(path);
        if (!(node instanceof FileNode)) {
            throw new FuseException("Not a file:" + path).initErrno(Errno.ENOENT);
        }
        FileNode fileNode = (FileNode) node;
        fileNode.read(buf, offset);
    }

    public String readlink(String path) throws FuseException {
        throw new FuseException("Not a link").initErrno(Errno.ENOENT);
    }

    public void release(String path, int flags) throws FuseException {
    }

    public void rename(String from, String to) throws FuseException {
    }

    public void rmdir(String path) throws FuseException {
    }

    public FuseStatfs statfs() throws FuseException {
        System.out.println("statfs");
        FuseStatfs statfs = new FuseStatfs();
        statfs.blocks = 0;
        statfs.blockSize = 512;
        statfs.blocksFree = 0;
        statfs.files = 0;
        statfs.filesFree = 0;
        statfs.namelen = 2048;
        return statfs;
    }

    public void symlink(String from, String to) throws FuseException {
    }

    public void truncate(String path, long size) throws FuseException {
    }

    public void unlink(String path) throws FuseException {
    }

    public void utime(String path, int atime, int mtime) throws FuseException {
    }

    public void write(String path, ByteBuffer buf, long offset) throws FuseException {
    }
}
