package org.restlet.representation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import org.restlet.data.Disposition;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.io.NioUtils;

/**
 * Representation based on a static file. Note that in order for Web clients to
 * display a download box upon reception of a file representation, it needs an
 * additional call to {@link Disposition#setType(String)} with a
 * {@link Disposition#TYPE_ATTACHMENT} value.
 * 
 * @author Jerome Louvel
 */
public class FileRepresentation extends Representation {

    /**
     * Creates a new file by detecting if the name is a URI or a simple path
     * name.
     * 
     * @param path
     *            The path name or file URI of the represented file (either in
     *            system format or in 'file:///' format).
     * @return The associated File instance.
     */
    private static File createFile(String path) {
        if (path.startsWith("file://")) {
            return new LocalReference(path).getFile();
        }
        return new File(path);
    }

    /**
     * Indicates if this file should be automatically deleted on release of the
     * representation.
     */
    private volatile boolean autoDeleting;

    /** The file handle. */
    private volatile File file;

    /**
     * Constructor that does not set an expiration date for {@code file}
     * 
     * @param file
     *            The represented file.
     * @param mediaType
     *            The representation's media type.
     * @see #FileRepresentation(File, MediaType, int)
     */
    public FileRepresentation(File file, MediaType mediaType) {
        this(file, mediaType, -1);
    }

    /**
     * Constructor. If a positive "timeToLive" parameter is given, then the
     * expiration date is set accordingly. If "timeToLive" is equal to zero,
     * then the expiration date is set to the current date, meaning that it will
     * immediately expire on the client. If -1 is given, then no expiration date
     * is set.
     * 
     * @param file
     *            The represented file.
     * @param mediaType
     *            The representation's media type.
     * @param timeToLive
     *            The time to live before it expires (in seconds).
     */
    public FileRepresentation(File file, MediaType mediaType, int timeToLive) {
        super(mediaType);
        this.file = file;
        setModificationDate(new Date(file.lastModified()));
        if (timeToLive == 0) {
            setExpirationDate(null);
        } else if (timeToLive > 0) {
            setExpirationDate(new Date(System.currentTimeMillis() + (1000L * timeToLive)));
        }
        setMediaType(mediaType);
        Disposition disposition = new Disposition();
        disposition.setFilename(file.getName());
        this.setDisposition(disposition);
    }

    /**
     * Constructor that does not set an expiration date for {@code path}
     * 
     * @param path
     *            The path name or file URI of the represented file (either in
     *            system format or in 'file:///' format).
     * @param mediaType
     *            The representation's media type.
     * @see #FileRepresentation(String, MediaType, int)
     */
    public FileRepresentation(String path, MediaType mediaType) {
        this(path, mediaType, -1);
    }

    /**
     * Constructor.
     * 
     * @param path
     *            The path name or file URI of the represented file (either in
     *            system format or in 'file:///' format).
     * @param mediaType
     *            The representation's media type.
     * @param timeToLive
     *            The time to live before it expires (in seconds).
     * @see java.io.File#File(String)
     */
    public FileRepresentation(String path, MediaType mediaType, int timeToLive) {
        this(createFile(path), mediaType, timeToLive);
    }

    /**
     * Returns a readable byte channel. If it is supported by a file a read-only
     * instance of FileChannel is returned.
     * 
     * @return A readable byte channel.
     */
    @Override
    public FileChannel getChannel() throws IOException {
        try {
            return new FileInputStream(this.file).getChannel();
        } catch (FileNotFoundException fnfe) {
            throw new IOException("Couldn't get the channel. File not found");
        }
    }

    /**
     * Returns the file handle.
     * 
     * @return the file handle.
     */
    public File getFile() {
        return this.file;
    }

    @Override
    public Reader getReader() throws IOException {
        return new FileReader(this.file);
    }

    @Override
    public long getSize() {
        if (super.getSize() != UNKNOWN_SIZE) {
            return super.getSize();
        }
        return this.file.length();
    }

    @Override
    public FileInputStream getStream() throws IOException {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException fnfe) {
            throw new IOException("Couldn't get the stream. File not found");
        }
    }

    @Override
    public String getText() throws IOException {
        return BioUtils.toString(getStream(), getCharacterSet());
    }

    /**
     * Indicates if this file should be automatically deleted on release of the
     * representation.
     * 
     * @return True if this file should be automatically deleted on release of
     *         the representation.
     * @deprecated Use {@link #isAutoDeleting()} instead.
     */
    @Deprecated
    public boolean isAutoDelete() {
        return autoDeleting;
    }

    /**
     * Indicates if this file should be automatically deleted on release of the
     * representation.
     * 
     * @return True if this file should be automatically deleted on release of
     *         the representation.
     */
    public boolean isAutoDeleting() {
        return isAutoDelete();
    }

    /**
     * Releases the file handle.
     */
    @Override
    public void release() {
        if (isAutoDeleting() && getFile() != null) {
            try {
                BioUtils.delete(getFile(), true);
            } catch (Exception e) {
            }
        }
        setFile(null);
        super.release();
    }

    /**
     * Indicates if this file should be automatically deleted on release of the
     * representation.
     * 
     * @param autoDeleting
     *            True if this file should be automatically deleted on release
     *            of the representation.
     * @deprecated Use {@link #setAutoDeleting(boolean)} instead.
     */
    @Deprecated
    public void setAutoDelete(boolean autoDeleting) {
        this.autoDeleting = autoDeleting;
    }

    /**
     * Indicates if this file should be automatically deleted on release of the
     * representation.
     * 
     * @param autoDeleting
     *            True if this file should be automatically deleted on release
     *            of the representation.
     */
    public void setAutoDeleting(boolean autoDeleting) {
        setAutoDelete(autoDeleting);
    }

    /**
     * Sets the file handle.
     * 
     * @param file
     *            The file handle.
     */
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        BioUtils.copy(getStream(), outputStream);
    }

    /**
     * Writes the representation to a byte channel. Optimizes using the file
     * channel transferTo method.
     * 
     * @param writableChannel
     *            A writable byte channel.
     */
    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        NioUtils.copy(getChannel(), writableChannel);
    }

    @Override
    public void write(Writer writer) throws IOException {
        BioUtils.copy(getReader(), writer);
    }
}
