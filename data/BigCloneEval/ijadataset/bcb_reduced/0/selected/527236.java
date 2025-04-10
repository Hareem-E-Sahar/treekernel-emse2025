package jacky.lanlan.song.io.stream;

import jacky.lanlan.song.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream which will retain data in memory until a specified threshold
 * is reached, and only then commit it to disk. If the stream is closed before
 * the threshold is reached, the data will not be written to disk at all.
 * <p>
 * This class originated in FileUpload processing. In this use case, you do not
 * know in advance the size of the file being uploaded. If the file is small you
 * want to store it in memory (for speed), but if the file is large you want to
 * store it to file (to avoid memory issues).
 * 
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author gaxzerow
 * 
 * @version $Id: DeferredFileOutputStream.java 437567 2006-08-28 06:39:07Z
 *          bayard $
 */
public class DeferredFileOutputStream extends ThresholdingOutputStream {

    /**
   * The output stream to which data will be written prior to the theshold being
   * reached.
   */
    private ByteArrayOutputStream memoryOutputStream;

    /**
   * The output stream to which data will be written at any given time. This
   * will always be one of <code>memoryOutputStream</code> or
   * <code>diskOutputStream</code>.
   */
    private OutputStream currentOutputStream;

    /**
   * The file to which output will be directed if the threshold is exceeded.
   */
    private File outputFile;

    /**
   * True when close() has been called successfully.
   */
    private boolean closed = false;

    /**
   * Constructs an instance of this class which will trigger an event at the
   * specified threshold, and save data to a file beyond that point.
   * 
   * @param threshold
   *          The number of bytes at which to trigger an event.
   * @param outputFile
   *          The file to which data is saved beyond the threshold.
   */
    public DeferredFileOutputStream(int threshold, File outputFile) {
        super(threshold);
        this.outputFile = outputFile;
        memoryOutputStream = new ByteArrayOutputStream();
        currentOutputStream = memoryOutputStream;
    }

    /**
   * Returns the current output stream. This may be memory based or disk based,
   * depending on the current state with respect to the threshold.
   * 
   * @return The underlying output stream.
   * 
   * @exception IOException
   *              if an error occurs.
   */
    protected OutputStream getStream() throws IOException {
        return currentOutputStream;
    }

    /**
   * Switches the underlying output stream from a memory based stream to one
   * that is backed by disk. This is the point at which we realise that too much
   * data is being written to keep in memory, so we elect to switch to
   * disk-based storage.
   * 
   * @exception IOException
   *              if an error occurs.
   */
    protected void thresholdReached() throws IOException {
        FileOutputStream fos = new FileOutputStream(outputFile);
        memoryOutputStream.writeTo(fos);
        currentOutputStream = fos;
        memoryOutputStream = null;
    }

    /**
   * Determines whether or not the data for this output stream has been retained
   * in memory.
   * 
   * @return <code>true</code> if the data is available in memory;
   *         <code>false</code> otherwise.
   */
    public boolean isInMemory() {
        return (!isThresholdExceeded());
    }

    /**
   * Returns the data for this output stream as an array of bytes, assuming that
   * the data has been retained in memory. If the data was written to disk, this
   * method returns <code>null</code>.
   * 
   * @return The data for this output stream, or <code>null</code> if no such
   *         data is available.
   */
    public byte[] getData() {
        if (memoryOutputStream != null) {
            return memoryOutputStream.toByteArray();
        }
        return null;
    }

    /**
   * Returns the same output file specified in the constructor, even when
   * threashold has not been reached.
   * 
   * @return The file for this output stream, or <code>null</code> if no such
   *         file exists.
   */
    public File getFile() {
        return outputFile;
    }

    /**
   * Closes underlying output stream, and mark this as closed
   * 
   * @exception IOException
   *              if an error occurs.
   */
    public void close() throws IOException {
        super.close();
        closed = true;
    }

    /**
   * Writes the data from this output stream to the specified output stream,
   * after it has been closed.
   * 
   * @param out
   *          output stream to write to.
   * @exception IOException
   *              if this stream is not yet closed or an error occurs.
   */
    public void writeTo(OutputStream out) throws IOException {
        if (!closed) {
            throw new IOException("Stream not closed");
        }
        if (isInMemory()) {
            memoryOutputStream.writeTo(out);
        } else {
            FileInputStream fis = new FileInputStream(outputFile);
            try {
                IOUtils.copy(fis, out);
            } finally {
                IOUtils.close(fis);
            }
        }
    }
}
