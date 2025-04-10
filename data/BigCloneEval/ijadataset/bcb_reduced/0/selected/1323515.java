package uk.ac.ed.rapid.vfs.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.util.FileObjectUtils;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.util.RandomAccessMode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * An SFTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2005-10-14 19:59:47 +0200 (Fr, 14 Okt
 *          2005) $
 */
public class SftpFileObject extends AbstractFileObject implements FileObject {

    private final SftpFileSystem fileSystem;

    private SftpATTRS attrs;

    private final String relPath;

    private boolean inRefresh;

    protected SftpFileObject(final FileName name, final SftpFileSystem fileSystem) throws FileSystemException {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        relPath = UriParser.decode(fileSystem.getRootName().getRelativeName(name));
    }

    protected void doDetach() throws Exception {
        attrs = null;
    }

    public void refresh() throws FileSystemException {
        if (!inRefresh) {
            try {
                inRefresh = true;
                super.refresh();
                try {
                    attrs = null;
                    getType();
                } catch (IOException e) {
                    throw new FileSystemException(e);
                }
            } finally {
                inRefresh = false;
            }
        }
    }

    /**
	 * Determines the type of this file, returns null if the file does not
	 * exist.
	 */
    protected FileType doGetType() throws Exception {
        if (attrs == null) {
            statSelf();
        }
        if (attrs == null) {
            return FileType.IMAGINARY;
        }
        if ((attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-permissions.error");
        }
        if (attrs.isDir()) {
            return FileType.FOLDER;
        } else {
            return FileType.FILE;
        }
    }

    /**
	 * Called when the type or content of this file changes.
	 */
    protected void onChange() throws Exception {
        statSelf();
    }

    /**
	 * Fetches file attrs from server.
	 */
    private void statSelf() throws Exception {
        ChannelSftp channel = fileSystem.getChannel();
        try {
            setStat(channel.stat(relPath));
        } catch (final SftpException e) {
            try {
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    channel.disconnect();
                    channel = fileSystem.getChannel();
                    setStat(channel.stat(relPath));
                } else {
                    attrs = null;
                }
            } catch (final SftpException e2) {
                attrs = null;
            }
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
	 * Set attrs from listChildrenResolved
	 */
    private void setStat(SftpATTRS attrs) {
        this.attrs = attrs;
    }

    /**
	 * Creates this file as a folder.
	 */
    protected void doCreateFolder() throws Exception {
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            channel.mkdir(relPath);
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    protected long doGetLastModifiedTime() throws Exception {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-modtime.error");
        }
        return attrs.getMTime() * 1000L;
    }

    /**
	 * Sets the last modified time of this file. Is only called if
	 * {@link #doGetType} does not return {@link FileType#IMAGINARY}. <p/>
	 *
	 * @param modtime
	 *            is modification time in milliseconds. SFTP protocol can send
	 *            times with nanosecond precision but at the moment jsch send
	 *            them with second precision.
	 */
    protected void doSetLastModifiedTime(final long modtime) throws Exception {
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            int newMTime = (int) (modtime / 1000L);
            attrs.setACMODTIME(attrs.getATime(), newMTime);
            channel.setStat(relPath, attrs);
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
	 * Deletes the file.
	 */
    protected void doDelete() throws Exception {
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            if (getType() == FileType.FILE) {
                channel.rm(relPath);
            } else {
                channel.rmdir(relPath);
            }
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
	 * Rename the file.
	 */
    protected void doRename(FileObject newfile) throws Exception {
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            channel.rename(relPath, ((SftpFileObject) newfile).relPath);
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
	 * Lists the children of this file.
	 */
    protected FileObject[] doListChildrenResolved() throws Exception {
        final Vector vector;
        final ChannelSftp channel = fileSystem.getChannel();
        String workingDirectory = null;
        try {
            try {
                if (relPath != null) {
                    workingDirectory = channel.pwd();
                    channel.cd(relPath);
                }
            } catch (SftpException e) {
                return null;
            }
            vector = channel.ls(".");
            try {
                if (relPath != null) {
                    channel.cd(workingDirectory);
                }
            } catch (SftpException e) {
                throw new FileSystemException("vfs.provider.sftp/change-work-directory-back.error", workingDirectory);
            }
        } finally {
            fileSystem.putChannel(channel);
        }
        if (vector == null) {
            throw new FileSystemException("vfs.provider.sftp/list-children.error");
        }
        final ArrayList children = new ArrayList();
        for (Iterator iterator = vector.iterator(); iterator.hasNext(); ) {
            final LsEntry stat = (LsEntry) iterator.next();
            String name = stat.getFilename();
            if (VFS.isUriStyle()) {
                if (stat.getAttrs().isDir() && name.charAt(name.length() - 1) != '/') {
                    name = name + "/";
                }
            }
            if (name.equals(".") || name.equals("..") || name.equals("./") || name.equals("../")) {
                continue;
            }
            FileObject fo = getFileSystem().resolveFile(getFileSystem().getFileSystemManager().resolveName(getName(), UriParser.encode(name), NameScope.CHILD));
            ((SftpFileObject) FileObjectUtils.getAbstractFileObject(fo)).setStat(stat.getAttrs());
            children.add(fo);
        }
        return (FileObject[]) children.toArray(new FileObject[children.size()]);
    }

    /**
	 * Lists the children of this file.
	 */
    protected String[] doListChildren() throws Exception {
        return null;
    }

    /**
	 * Returns the size of the file content (in bytes).
	 */
    protected long doGetContentSize() throws Exception {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-size.error");
        }
        return attrs.getSize();
    }

    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new SftpRandomAccessContent(this, mode);
    }

    /**
	 * Creates an input stream to read the file content from.
	 */
    InputStream getInputStream(long filePointer) throws IOException {
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            ByteArrayOutputStream outstr = new ByteArrayOutputStream();
            try {
                channel.get(getName().getPathDecoded(), outstr, null, ChannelSftp.RESUME, filePointer);
            } catch (SftpException e) {
                throw new FileSystemException(e);
            }
            outstr.close();
            return new ByteArrayInputStream(outstr.toByteArray());
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
	 * Creates an input stream to read the file content from.
	 */
    protected InputStream doGetInputStream() throws Exception {
        synchronized (fileSystem) {
            final ChannelSftp channel = fileSystem.getChannel();
            try {
                InputStream is;
                try {
                    if (!getType().hasContent()) {
                        throw new FileSystemException("vfs.provider/read-not-file.error", getName());
                    }
                    is = channel.get(relPath);
                } catch (SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    }
                    throw new FileSystemException(e);
                }
                return new SftpInputStream(channel, is);
            } finally {
            }
        }
    }

    /**
	 * Creates an output stream to write the file content to.
	 */
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
        final ChannelSftp channel = fileSystem.getChannel();
        return new SftpOutputStream(channel, channel.put(relPath));
    }

    /**
	 * An InputStream that monitors for end-of-file.
	 */
    private class SftpInputStream extends MonitorInputStream {

        private final ChannelSftp channel;

        public SftpInputStream(final ChannelSftp channel, final InputStream in) {
            super(in);
            this.channel = channel;
        }

        /**
		 * Called after the stream has been closed.
		 */
        protected void onClose() throws IOException {
            fileSystem.putChannel(channel);
        }
    }

    /**
	 * An OutputStream that wraps an sftp OutputStream, and closes the channel
	 * when the stream is closed.
	 */
    private class SftpOutputStream extends MonitorOutputStream {

        private final ChannelSftp channel;

        public SftpOutputStream(final ChannelSftp channel, OutputStream out) {
            super(out);
            this.channel = channel;
        }

        /**
		 * Called after this stream is closed.
		 */
        protected void onClose() throws IOException {
            {
                fileSystem.putChannel(channel);
            }
        }
    }
}
