package org.restlet.engine.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.engine.Edition;
import org.restlet.representation.Representation;

/**
 * Utility methods for NIO processing.
 * 
 * @author Jerome Louvel
 */
public class NioUtils {

    /** The number of milliseconds after which NIO operation will time out. */
    public static final int NIO_TIMEOUT = 60000;

    /**
     * Writes the representation to a byte channel. Optimizes using the file
     * channel transferTo method.
     * 
     * @param fileChannel
     *            The readable file channel.
     * @param writableChannel
     *            A writable byte channel.
     */
    public static void copy(FileChannel fileChannel, WritableByteChannel writableChannel) throws IOException {
        long position = 0;
        long count = fileChannel.size();
        long written = 0;
        SelectableChannel selectableChannel = null;
        if (writableChannel instanceof SelectableChannel) {
            selectableChannel = (SelectableChannel) writableChannel;
        }
        while (count > 0) {
            NioUtils.waitForState(selectableChannel, SelectionKey.OP_WRITE);
            written = fileChannel.transferTo(position, count, writableChannel);
            position += written;
            count -= written;
        }
    }

    /**
     * Writes a readable channel to a writable channel.
     * 
     * @param readableChannel
     *            The readable channel.
     * @param writableChannel
     *            The writable channel.
     * @throws IOException
     */
    public static void copy(ReadableByteChannel readableChannel, WritableByteChannel writableChannel) throws IOException {
        if ((readableChannel != null) && (writableChannel != null)) {
            BioUtils.copy(new NbChannelInputStream(readableChannel), new NbChannelOutputStream(writableChannel));
        }
    }

    /**
     * Returns a readable byte channel based on a given inputstream. If it is
     * supported by a file a read-only instance of FileChannel is returned.
     * 
     * @param inputStream
     *            The input stream to convert.
     * @return A readable byte channel.
     */
    public static ReadableByteChannel getChannel(InputStream inputStream) {
        return (inputStream != null) ? Channels.newChannel(inputStream) : null;
    }

    /**
     * Returns a writable byte channel based on a given output stream.
     * 
     * @param outputStream
     *            The output stream.
     * @return A writable byte channel.
     */
    public static WritableByteChannel getChannel(OutputStream outputStream) {
        return (outputStream != null) ? Channels.newChannel(outputStream) : null;
    }

    /**
     * Returns a readable byte channel based on the given representation's
     * content and its write(WritableByteChannel) method. Internally, it uses a
     * writer thread and a pipe channel.
     * 
     * @param representation
     *            the representation to get the {@link OutputStream} from.
     * @return A readable byte channel.
     * @throws IOException
     */
    public static ReadableByteChannel getChannel(final Representation representation) throws IOException {
        ReadableByteChannel result = null;
        if (Edition.CURRENT != Edition.GAE) {
            final java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
            final org.restlet.Application application = org.restlet.Application.getCurrent();
            application.getTaskService().execute(new Runnable() {

                public void run() {
                    try {
                        WritableByteChannel wbc = pipe.sink();
                        representation.write(wbc);
                        wbc.close();
                    } catch (IOException ioe) {
                        Context.getCurrentLogger().log(Level.FINE, "Error while writing to the piped channel.", ioe);
                    }
                }
            });
            result = pipe.source();
        } else {
            Context.getCurrentLogger().log(Level.WARNING, "The GAE edition is unable to return a channel for a representation given its write(WritableByteChannel) method.");
        }
        return result;
    }

    /**
     * Returns an input stream based on a given readable byte channel.
     * 
     * @param readableChannel
     *            The readable byte channel.
     * @return An input stream based on a given readable byte channel.
     */
    public static InputStream getStream(ReadableByteChannel readableChannel) {
        InputStream result = null;
        if (readableChannel != null) {
            result = new NbChannelInputStream(readableChannel);
        }
        return result;
    }

    /**
     * Returns an output stream based on a given writable byte channel.
     * 
     * @param writableChannel
     *            The writable byte channel.
     * @return An output stream based on a given writable byte channel.
     */
    public static OutputStream getStream(WritableByteChannel writableChannel) {
        OutputStream result = null;
        if (writableChannel instanceof SelectableChannel) {
            SelectableChannel selectableChannel = (SelectableChannel) writableChannel;
            synchronized (selectableChannel.blockingLock()) {
                if (selectableChannel.isBlocking()) {
                    result = Channels.newOutputStream(writableChannel);
                } else {
                    result = new NbChannelOutputStream(writableChannel);
                }
            }
        } else {
            result = new NbChannelOutputStream(writableChannel);
        }
        return result;
    }

    /**
     * Release the selection key, working around for bug #6403933.
     * 
     * @param selector
     *            The associated selector.
     * @param selectionKey
     *            The used selection key.
     * @throws IOException
     */
    public static void release(Selector selector, SelectionKey selectionKey) throws IOException {
        if (selectionKey != null) {
            selectionKey.cancel();
            if (selector != null) {
                selector.selectNow();
                SelectorFactory.returnSelector(selector);
            }
        }
    }

    /**
     * Waits for the given channel to be ready for a specific operation.
     * 
     * @param selectableChannel
     *            The channel to monitor.
     * @param operations
     *            The operations to be ready to do.
     * @throws IOException
     */
    public static void waitForState(SelectableChannel selectableChannel, int operations) throws IOException {
        if (selectableChannel != null) {
            Selector selector = null;
            SelectionKey selectionKey = null;
            int selected = 0;
            try {
                selector = SelectorFactory.getSelector();
                while (selected == 0) {
                    selectionKey = selectableChannel.register(selector, operations);
                    selected = selector.select(NIO_TIMEOUT);
                }
            } finally {
                NioUtils.release(selector, selectionKey);
            }
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private NioUtils() {
    }
}
