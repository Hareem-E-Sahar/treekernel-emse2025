package org.limewire.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.nio.channel.ChannelReadObserver;
import org.limewire.nio.channel.ChannelReader;
import org.limewire.nio.channel.ChannelWriter;
import org.limewire.nio.channel.InterestReadableByteChannel;
import org.limewire.nio.channel.InterestWritableByteChannel;
import org.limewire.nio.channel.NIOMultiplexor;
import org.limewire.nio.observer.ConnectObserver;
import org.limewire.nio.observer.ReadObserver;
import org.limewire.nio.observer.ReadWriteObserver;
import org.limewire.nio.observer.Shutdownable;
import org.limewire.nio.observer.WriteObserver;
import org.limewire.nio.timeout.ReadTimeout;
import org.limewire.nio.timeout.SoTimeout;
import org.limewire.util.VersionUtils;

/**
 * Implements all common functionality that a non-blocking socket must contain. 
 * Specifically, <code>AbstractNBSocket</code> handles 
 * the multiplexing aspect of handing off reading, writing and connecting to 
 * other Observers (<code>org.limewire.nio.observer</code>). 
 * <p>
 * Additionally, <code>AbstractNBSocket</code> traverses the chain of readers 
 * and writers to read leftover data and ensure remaining data is written. 
 * <p>
 * <code>AbstractNBSocket</code> also exposes a common blocking input and output
 * stream.
 */
public abstract class AbstractNBSocket extends NBSocket implements ConnectObserver, ReadWriteObserver, NIOMultiplexor, ReadTimeout, SoTimeout {

    private static final Log LOG = LogFactory.getLog(AbstractNBSocket.class);

    /** Lock for shutting down. */
    private final Object LOCK = new Object();

    /** The reader. */
    private volatile ReadObserver reader;

    /** The writer. */
    private volatile WriteObserver writer;

    /** The NIOOutputStream object, if we're using blocking writing. */
    private volatile NIOOutputStream nioOutputStream;

    /** The connecter. */
    private volatile ConnectObserver connecter;

    /** An observer for being shutdown. */
    private volatile Shutdownable shutdownObserver;

    /** Whether or not we've shutdown the socket. */
    private boolean shutdown = false;

    /**
     * Retrieves the channel which should be used as the base channel
     * for all reading operations.
     */
    protected abstract InterestReadableByteChannel getBaseReadChannel();

    /**
     * Retrieves the channel which should be used as the base channel
     * for all writing operations.
     * <p>
     * If the base write channel is chained (that is, if there are multiple
     * writing layers that will always be used) then this must return
     * the top-most layer.  That layer will be installed beneath the
     * bottom layer that is set on the Socket.  All layers except the last
     * must implement ChannelWriter, so they can be iterated over in order
     * to set the last writer.
     */
    protected abstract InterestWritableByteChannel getBaseWriteChannel();

    /**
     * Performs any operations required for shutting down this socket.
     * <code>shutdownImpl</code> method will only be called once per Socket.
     */
    protected abstract void shutdownImpl();

    /**
     * Sets the initial reader value.
     */
    public final void setInitialReader() {
        reader = new NIOInputStream(this, this, getBaseReadChannel());
    }

    /**
     * Sets the initial writer value.
     */
    public final void setInitialWriter() {
        InterestWritableByteChannel base = getBaseWriteChannel();
        writer = getBottomFromChain(base);
        nioOutputStream = new NIOOutputStream(this, base);
    }

    private InterestWritableByteChannel getBottomFromChain(InterestWritableByteChannel top) {
        if (top instanceof ChannelWriter) {
            ChannelWriter lastChannel = (ChannelWriter) top;
            while (lastChannel.getWriteChannel() instanceof ChannelWriter) lastChannel = (ChannelWriter) lastChannel.getWriteChannel();
            return (InterestWritableByteChannel) lastChannel;
        } else {
            return top;
        }
    }

    /**
     * Sets the <code>Shutdown</code> observer.
     * This observer is useful for when the Socket is created,
     * but connect has not been called yet.  This observer will be
     * notified when the socket is shutdown.
     */
    public final void setShutdownObserver(Shutdownable observer) {
        shutdownObserver = observer;
    }

    /**
     * Sets the new <code>ReadObserver</code>.
     * <p>
     * The deepest <code>ChannelReader</code> in the chain first has its source
     * set to the prior reader (assuming it implemented <code>ReadableByteChannel</code>)
     * and a read is notified, in order to read any buffered data.
     * The source is then set to the Socket's channel and interest
     * in reading is turned on.
     */
    public final void setReadObserver(final ChannelReadObserver newReader) {
        NIODispatcher.instance().getScheduledExecutorService().execute(new Runnable() {

            public void run() {
                ReadObserver oldReader = reader;
                try {
                    synchronized (LOCK) {
                        if (shutdown) {
                            newReader.shutdown();
                            return;
                        }
                        reader = newReader;
                    }
                    ChannelReader lastChannel = newReader;
                    while (lastChannel.getReadChannel() instanceof ChannelReader) lastChannel = (ChannelReader) lastChannel.getReadChannel();
                    if (lastChannel instanceof RequiresSelectionKeyAttachment) ((RequiresSelectionKeyAttachment) lastChannel).setAttachment(AbstractNBSocket.this);
                    if (oldReader instanceof InterestReadableByteChannel && oldReader != newReader) {
                        lastChannel.setReadChannel((InterestReadableByteChannel) oldReader);
                        reader.handleRead();
                        oldReader.shutdown();
                    }
                    InterestReadableByteChannel source = getBaseReadChannel();
                    lastChannel.setReadChannel(source);
                    source.interestRead(true);
                    if (isConnected() && !NIODispatcher.instance().isReadReadyThisIteration(getChannel())) reader.handleRead();
                } catch (IOException iox) {
                    shutdown();
                    oldReader.shutdown();
                }
            }
        });
    }

    /**
     * Sets the new <code>WriteObserver</code>.
     *<p>
     * If a <code>ThrottleWriter</code> is one of the <code>ChannelWriters</code>, 
     * the attachment of the <code>ThrottleWriter</code> is set to be this.
     * <p>
     * The deepest <code>ChannelWriter<code> in the chain has its source set to be
     * a new <code>InterestWriteChannel</code>, which will be used as the hub to receive
     * and forward interest events from/to the channel.
     * <p>
     * If this is called while the existing <code>WriteObserver</code> still has data left to
     * write, then an <code>IllegalStateException</code> is thrown.
     */
    public final void setWriteObserver(final ChannelWriter newWriter) {
        NIODispatcher.instance().getScheduledExecutorService().execute(new Runnable() {

            public void run() {
                try {
                    if (writer.handleWrite()) throw new IllegalStateException("data still in old writer!");
                    writer.shutdown();
                    if (nioOutputStream != null) nioOutputStream.shutdown();
                    ChannelWriter lastChannel = newWriter;
                    while (lastChannel.getWriteChannel() instanceof ChannelWriter) {
                        lastChannel = (ChannelWriter) lastChannel.getWriteChannel();
                        if (lastChannel instanceof RequiresSelectionKeyAttachment) ((RequiresSelectionKeyAttachment) lastChannel).setAttachment(AbstractNBSocket.this);
                    }
                    InterestWritableByteChannel source = getBaseWriteChannel();
                    synchronized (LOCK) {
                        lastChannel.setWriteChannel(source);
                        if (shutdown) {
                            source.shutdown();
                            return;
                        }
                        nioOutputStream = null;
                        writer = getBottomFromChain(source);
                    }
                } catch (IOException iox) {
                    shutdown();
                    newWriter.shutdown();
                }
            }
        });
    }

    /**
     * Notification that a connect can occur.
     * <p>
     * This passes it off on to the delegating connecter and then forgets the
     * connecter for the duration of the connection.
     */
    public final void handleConnect(Socket s) throws IOException {
        ConnectObserver observer = connecter;
        connecter = null;
        observer.handleConnect(this);
    }

    /**
     * Notification that a read can occur.
     * 
     * This passes it off to the delegating reader.
     */
    public final void handleRead() throws IOException {
        reader.handleRead();
    }

    /**
     * Notification that a write can occur.
     *
     * This passes it off to the delegating writer.
     */
    public final boolean handleWrite() throws IOException {
        return writer.handleWrite();
    }

    /** Closes the socket & all streams, waking up any waiting locks.  */
    public final void close() {
        shutdown();
    }

    /** Connects to <code>addr</code> with no timeout */
    public final void connect(SocketAddress addr) throws IOException {
        connect(addr, 0);
    }

    /** Connects to <code>addr</code> with the given timeout (in milliseconds) */
    public final void connect(SocketAddress addr, int timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must not be < 0");
        }
        final CountDownLatch connectLatch = new CountDownLatch(1);
        ConnectObserver connecter = new ConnectObserver() {

            public void handleConnect(Socket s) {
                connectLatch.countDown();
            }

            public void shutdown() {
                connectLatch.countDown();
            }

            public void handleIOException(IOException e) {
            }
        };
        if (!connect(addr, timeout, connecter)) {
            long then = System.currentTimeMillis();
            try {
                if (timeout == 0) {
                    connectLatch.await();
                } else {
                    connectLatch.await(timeout + 1000, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException ie) {
                shutdown();
                throw new InterruptedIOException(ie);
            }
            if (!isConnected()) {
                shutdown();
                long now = System.currentTimeMillis();
                if (timeout != 0 && now - then >= timeout) throw new SocketTimeoutException("operation timed out (" + timeout + ")"); else throw new ConnectException("Unable to connect!");
            }
        }
    }

    /**
     * Connects to the specified address within the given timeout (in milliseconds).
     * The given <code>ConnectObserver</code> will be notified of success or failure.
     * In the event of success, <code>observer.handleConnect</code> is called. In a failure,
     * <code>observer.shutdown</code> is called. <code>observer.handleIOException</code> 
     * is never called.
     * <p>
     * Returns true if this was able to connect immediately. The observer is still
     * notified about the success even it it was immediate.
     */
    public boolean connect(SocketAddress addr, int timeout, final ConnectObserver observer) {
        synchronized (LOCK) {
            if (shutdown) {
                observer.shutdown();
                return false;
            }
            this.connecter = observer;
        }
        try {
            InetSocketAddress iaddr = (InetSocketAddress) addr;
            if (iaddr.isUnresolved()) throw new IOException("unresolved: " + addr);
            if (getChannel().connect(addr)) {
                NIODispatcher.instance().getScheduledExecutorService().execute(new Runnable() {

                    public void run() {
                        NIODispatcher.instance().register(getChannel(), AbstractNBSocket.this);
                        try {
                            observer.handleConnect(AbstractNBSocket.this);
                        } catch (IOException iox) {
                            NIODispatcher.instance().executeLaterAlways(new Runnable() {

                                public void run() {
                                    shutdown();
                                }
                            });
                        }
                    }
                });
                return true;
            } else {
                NIODispatcher.instance().registerConnect(getChannel(), this, timeout);
                return false;
            }
        } catch (IOException failed) {
            NIODispatcher.instance().executeLaterAlways(new Runnable() {

                public void run() {
                    shutdown();
                }
            });
            return false;
        }
    }

    /**
     * Returns the <code>InputStream</code> from the <code>NIOInputStream</code>.
     *
     * Internally, this is a blocking Pipe from the non-blocking <code>SocketChannel</code>.
     */
    public final InputStream getInputStream() throws IOException {
        if (isClosed() || isShutdown()) throw new IOException("Socket closed.");
        ReadObserver localReader;
        synchronized (LOCK) {
            if (isShutdown()) throw new IOException("Socket closed.");
            localReader = reader;
        }
        if (localReader instanceof NIOInputStream) {
            NIOInputStream nis = (NIOInputStream) localReader;
            InputStream stream = nis.getInputStream();
            nis.interestRead(true);
            return stream;
        } else {
            Callable<InputStream> callable = new Callable<InputStream>() {

                public InputStream call() throws IOException {
                    NIOInputStream stream = new NIOInputStream(AbstractNBSocket.this, AbstractNBSocket.this, null).init();
                    setReadObserver(stream);
                    return stream.getInputStream();
                }
            };
            Future<InputStream> future = NIODispatcher.instance().getScheduledExecutorService().submit(callable);
            try {
                return future.get();
            } catch (ExecutionException ee) {
                throw (IOException) new IOException().initCause(ee.getCause());
            } catch (InterruptedException ie) {
                throw (IOException) new IOException().initCause(ie.getCause());
            }
        }
    }

    /**
     * Returns the <code>OutputStream</code> from the <code>NIOOutputStream</code>.
     *
     * Internally, this is a blocking Pipe from the non-blocking SocketChannel.
     */
    public final OutputStream getOutputStream() throws IOException {
        if (isClosed() || isShutdown()) throw new IOException("Socket closed.");
        NIOOutputStream output = nioOutputStream;
        if (output != null) return output.getOutputStream(); else throw new IllegalStateException("blocking I/O not in use!");
    }

    /** Gets the read timeout for this socket. */
    public long getReadTimeout() {
        if (reader instanceof NIOInputStream) {
            return 0;
        } else {
            try {
                return getSoTimeout();
            } catch (SocketException se) {
                return 0;
            }
        }
    }

    /**
     * Notification that an <code>IOException</code> occurred while processing a
     * read, connect, or write.
     */
    public final void handleIOException(IOException iox) {
        shutdown();
    }

    /**
     * Shuts down this socket & all its streams.
     */
    public final void shutdown() {
        synchronized (LOCK) {
            if (shutdown) return;
            shutdown = true;
        }
        if (LOG.isDebugEnabled()) LOG.debug("Shutting down socket & streams for: " + this);
        if (VersionUtils.isJavaVersionOrAbove("1.5.0_10") || NIODispatcher.instance().isDispatchThread()) {
            shutdownSocketAndChannels();
        } else {
            Future future = NIODispatcher.instance().getScheduledExecutorService().submit(new Runnable() {

                public void run() {
                    shutdownSocketAndChannels();
                }
            });
            while (true) {
                try {
                    future.get();
                    break;
                } catch (InterruptedException e) {
                    continue;
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        shutdownObservers();
        NIODispatcher.instance().getScheduledExecutorService().execute(new Runnable() {

            public void run() {
                if (nioOutputStream != null) nioOutputStream.shutdown();
                nioOutputStream = null;
                reader = new NoOpReader();
                writer = new NoOpWriter();
                connecter = null;
                shutdownObserver = null;
            }
        });
    }

    /** Shuts down all observers. */
    protected void shutdownObservers() {
        reader.shutdown();
        writer.shutdown();
        if (connecter != null) connecter.shutdown();
        if (shutdownObserver != null) shutdownObserver.shutdown();
    }

    /** Shuts down the socket and channels. */
    private void shutdownSocketAndChannels() {
        shutdownImpl();
        try {
            getChannel().close();
        } catch (IOException ignored) {
        }
    }

    private boolean isShutdown() {
        synchronized (LOCK) {
            return shutdown;
        }
    }
}
