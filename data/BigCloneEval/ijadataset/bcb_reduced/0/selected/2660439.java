package org.xsocket.stream;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xsocket.ClosedConnectionException;
import org.xsocket.IDispatcher;
import org.xsocket.IHandle;
import org.xsocket.DataConverter;

/**
 * Socket based io handler
 * 
 * @author grro@xsocket.org
 */
final class IoSocketHandler extends IoHandler implements IHandle {

    private static final Logger LOG = Logger.getLogger(IoSocketHandler.class.getName());

    public static final int DEFAULT_READ_MEMORY_MIN_SIZE = 128;

    private boolean isLogicalOpen = true;

    private boolean isDisconnectNotified = false;

    private SocketChannel channel = null;

    private IDispatcher<IoSocketHandler> dispatcher = null;

    private IIOEventHandler ioEventHandler = null;

    private IMemoryManager memoryManager = null;

    private final ByteBufferQueue sendQueue = new ByteBufferQueue();

    private final ByteBufferQueue receiveQueue = new ByteBufferQueue();

    private String id = null;

    private static long nextId = 0;

    private static String idPrefix = null;

    private long idleTimeout = Long.MAX_VALUE;

    private long connectionTimeout = Long.MAX_VALUE;

    private long openTime = -1;

    private long lastTimeReceived = System.currentTimeMillis();

    private long receivedBytes = 0;

    private long sendBytes = 0;

    static {
        String base = null;
        try {
            base = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception e) {
            base = "locale";
        }
        int random = 0;
        do {
            random = new Random().nextInt();
        } while (random < 0);
        idPrefix = Integer.toHexString(base.hashCode()) + "." + Long.toHexString(System.currentTimeMillis()) + "." + Integer.toHexString(random);
    }

    /**
	 * constructor 
	 * 
	 * @param channel        the underlying channel
	 * @param idLocalPrefix  the id namespace prefix
	 * @param memoryManager  the memory manager
	 * @throws IOException If some other I/O error occurs
	 */
    @SuppressWarnings("unchecked")
    IoSocketHandler(SocketChannel channel, String idLocalPrefix, IDispatcher<IoSocketHandler> dispatcher) throws IOException {
        super(null);
        assert (channel != null);
        this.channel = channel;
        openTime = System.currentTimeMillis();
        channel.configureBlocking(false);
        id = idPrefix + "." + idLocalPrefix + nextLocalId();
        this.dispatcher = dispatcher;
    }

    void setMemoryManager(IMemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    /**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    @Override
    void open() throws IOException {
        assert (ioEventHandler != null) : "ioHandler hasn't been set";
        dispatcher.register(this, SelectionKey.OP_READ);
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    void setIOEventHandler(IIOEventHandler ioEventHandler) {
        this.ioEventHandler = ioEventHandler;
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    IIOEventHandler getIOEventHandler() {
        return ioEventHandler;
    }

    private synchronized long nextLocalId() {
        nextId++;
        if (nextId < 0) {
            nextId = 1;
        }
        return nextId;
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    final String getId() {
        return id;
    }

    /**
	 * sets the idle timeout
	 * @param timeout the idle timeout
	 */
    public void setIdleTimeoutMillis(long timeout) {
        this.idleTimeout = timeout;
    }

    /**
	 * sets the connection timout
	 * 
	 * @param timeout the connection timeout
	 */
    public void setConnectionTimeoutMillis(long timeout) {
        this.connectionTimeout = timeout;
    }

    /**
	 * gets the connection timeout 
	 *  
	 * @return the connection timeout
	 */
    public long getConnectionTimeoutMillis() {
        return connectionTimeout;
    }

    /**
	 * gets the idle timeout
	 * @return the idle timeout
	 */
    public long getIdleTimeoutMillis() {
        return idleTimeout;
    }

    int getReceiveQueueSize() {
        return receiveQueue.getSize();
    }

    int getSendQueueSize() {
        return sendQueue.getSize();
    }

    /**
	 * check if the underyling connection is idle timed out
	 * 
	 * @param current   the current time
	 * @return true, if the connection has been timed out
	 */
    boolean checkIdleTimeout(Long current) {
        long maxTime = lastTimeReceived + idleTimeout;
        if (maxTime < 0) {
            maxTime = Long.MAX_VALUE;
        }
        boolean timeoutReached = maxTime < current;
        if (timeoutReached) {
            ioEventHandler.onIdleTimeout();
        }
        return timeoutReached;
    }

    /**
	 * check if the underyling connection is timed out
	 * 
	 * @param current   the current time
	 * @return true, if the connection has been timed out
	 */
    boolean checkConnectionTimeout(Long current) {
        long maxTime = openTime + connectionTimeout;
        if (maxTime < 0) {
            maxTime = Long.MAX_VALUE;
        }
        boolean timeoutReached = maxTime < current;
        if (timeoutReached) {
            ioEventHandler.onConnectionTimeout();
        }
        return timeoutReached;
    }

    /**
	 * return the size of the read queue
	 * 
	 * @return the read queue size
	 */
    int getIncomingQueueSize() {
        return receiveQueue.getSize();
    }

    synchronized void onDataEvent() {
        if (getReceiveQueueSize() > 0) {
            getIOEventHandler().onDataEvent();
        }
    }

    void onWrittenEvent() {
        if (getIOEventHandler().listenForWritten()) {
            getIOEventHandler().onWrittenEvent();
        }
    }

    void onWrittenExceptionEvent(IOException ioe) {
        if (getIOEventHandler().listenForWritten()) {
            getIOEventHandler().onWriteExceptionEvent(new IOException(ioe.toString()));
        }
    }

    synchronized void onDisconnectEvent() {
        if (getIOEventHandler().listenForDisconnect() & !isDisconnectNotified) {
            isDisconnectNotified = true;
            getIOEventHandler().onDisconnectEvent();
        }
    }

    boolean shouldClosedPhysically() {
        if (!isLogicalOpen) {
            if (sendQueue.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    @Override
    void writeOutgoing(ByteBuffer buffer) throws IOException {
        if (buffer != null) {
            sendQueue.append(buffer);
            dispatcher.updateInterestSet(this, SelectionKey.OP_WRITE);
        }
    }

    /**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    @Override
    void writeOutgoing(LinkedList<ByteBuffer> buffers) throws IOException {
        if (buffers != null) {
            sendQueue.append(buffers);
            dispatcher.updateInterestSet(this, SelectionKey.OP_WRITE);
        }
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    LinkedList<ByteBuffer> drainIncoming() {
        return receiveQueue.drain();
    }

    /**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    @Override
    void close() throws IOException {
        isLogicalOpen = false;
        try {
            dispatcher.updateInterestSet(this, SelectionKey.OP_WRITE);
        } catch (Exception e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("couldn't update interested set to write (for flushing) deregister without flushing. Reason: " + e.toString());
            }
            try {
                dispatcher.deregister(this);
            } catch (Exception ignore) {
            }
        }
    }

    void closePhysically() throws IOException {
        channel.close();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    boolean isChainSendBufferEmpty() {
        if (getSuccessor() != null) {
            return (sendQueue.isEmpty() && getSuccessor().isChainSendBufferEmpty());
        } else {
            return sendQueue.isEmpty();
        }
    }

    /**
	 * return the underlying channel
	 * 
	 * @return the underlying channel
	 */
    public SocketChannel getChannel() {
        return channel;
    }

    IDispatcher<IoSocketHandler> getDispatcher() {
        return dispatcher;
    }

    /**
	 * reads socket into read queue
	 * 
	 * @return the number of read bytes
	 * @throws IOException If some other I/O error occurs
	 * @throws ClosedConnectionException if the underlying channel is closed  
	 */
    protected final int readSocketIntoReceiveQueue() throws IOException {
        int read = 0;
        lastTimeReceived = System.currentTimeMillis();
        if (isOpen()) {
            ByteBuffer readBuffer = memoryManager.acquireMemory(DEFAULT_READ_MEMORY_MIN_SIZE);
            int pos = readBuffer.position();
            int limit = readBuffer.limit();
            try {
                read = channel.read(readBuffer);
            } catch (IOException ioe) {
                readBuffer.position(pos);
                readBuffer.limit(limit);
                memoryManager.recycleMemory(readBuffer);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("[" + getId() + "] error occured while reading channel: " + ioe.toString());
                }
                throw ioe;
            }
            switch(read) {
                case -1:
                    memoryManager.recycleMemory(readBuffer);
                    ClosedConnectionException cce = new ClosedConnectionException("[" + id + "] End of stream reached");
                    throw cce;
                case 0:
                    memoryManager.recycleMemory(readBuffer);
                    break;
                default:
                    int savePos = readBuffer.position();
                    int saveLimit = readBuffer.limit();
                    readBuffer.position(savePos - read);
                    readBuffer.limit(savePos);
                    ByteBuffer readData = readBuffer.slice();
                    receiveQueue.append(readData);
                    if (readBuffer.hasRemaining()) {
                        readBuffer.position(savePos);
                        readBuffer.limit(saveLimit);
                        memoryManager.recycleMemory(readBuffer);
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("[" + getId() + "] received (" + (readData.limit() - readData.position()) + " bytes): " + DataConverter.toTextOrHexString(new ByteBuffer[] { readData.duplicate() }, "UTF-8", 500));
                    }
                    break;
            }
        }
        receivedBytes += read;
        return read;
    }

    /**
	 * writes the content of the send queue to the socket
	 * 
	 * @throws IOException If some other I/O error occurs
	 * @throws ClosedConnectionException if the underlying channel is closed  
	 */
    @SuppressWarnings("unchecked")
    protected final void writeSendQueueDataToSocket() throws IOException {
        if (isOpen() && !sendQueue.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("[" + getId() + "] sending: " + sendQueue.toString());
            }
            LinkedList<ByteBuffer> data = sendQueue.drain();
            sendBytes += channel.write(data.toArray(new ByteBuffer[data.size()]));
            ListIterator<ByteBuffer> it = data.listIterator(data.size());
            while (it.hasPrevious()) {
                ByteBuffer buf = it.previous();
                if (buf.hasRemaining()) {
                    sendQueue.addFirst(buf);
                }
            }
            if (!sendQueue.isEmpty()) {
                dispatcher.updateInterestSet(this, SelectionKey.OP_WRITE);
            }
        }
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public final InetAddress getLocalAddress() {
        return channel.socket().getLocalAddress();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public final int getLocalPort() {
        return channel.socket().getLocalPort();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public final InetAddress getRemoteAddress() {
        return channel.socket().getInetAddress();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public final int getRemotePort() {
        return channel.socket().getPort();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    void flushOutgoing() {
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public String toString() {
        try {
            return channel.socket().getInetAddress().toString() + ":" + channel.socket().getPort() + " received=" + DataConverter.toFormatedBytesSize(receivedBytes) + ", sent=" + DataConverter.toFormatedBytesSize(sendBytes) + ", age=" + DataConverter.toFormatedDuration(System.currentTimeMillis() - openTime) + ", lastReceived=" + DataConverter.toFormatedDate(lastTimeReceived) + " [" + id + "]";
        } catch (Exception e) {
            return super.toString();
        }
    }
}
