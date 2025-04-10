package org.apache.mina.core.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.DefaultFileRegion;
import org.apache.mina.core.file.FilenameFileRegion;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.DefaultCloseFuture;
import org.apache.mina.core.future.DefaultReadFuture;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteException;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteTimeoutException;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.util.ExceptionMonitor;

/**
 * Base implementation of {@link IoSession}.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public abstract class AbstractIoSession implements IoSession {

    private static final AttributeKey READY_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "readyReadFutures");

    private static final AttributeKey WAITING_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "waitingReadFutures");

    private static final IoFutureListener<CloseFuture> SCHEDULED_COUNTER_RESETTER = new IoFutureListener<CloseFuture>() {

        public void operationComplete(CloseFuture future) {
            AbstractIoSession session = (AbstractIoSession) future.getSession();
            session.scheduledWriteBytes.set(0);
            session.scheduledWriteMessages.set(0);
            session.readBytesThroughput = 0;
            session.readMessagesThroughput = 0;
            session.writtenBytesThroughput = 0;
            session.writtenMessagesThroughput = 0;
        }
    };

    /**
     * An internal write request object that triggers session close.
     * @see #writeRequestQueue
     */
    private static final WriteRequest CLOSE_REQUEST = new DefaultWriteRequest(new Object());

    private final Object lock = new Object();

    private IoSessionAttributeMap attributes;

    private WriteRequestQueue writeRequestQueue;

    private WriteRequest currentWriteRequest;

    private final long creationTime;

    /** An id generator guaranteed to generate unique IDs for the session */
    private static AtomicLong idGenerator = new AtomicLong(0);

    /** The session ID */
    private long sessionId;

    /**
     * A future that will be set 'closed' when the connection is closed.
     */
    private final CloseFuture closeFuture = new DefaultCloseFuture(this);

    private volatile boolean closing;

    private boolean readSuspended = false;

    private boolean writeSuspended = false;

    private final AtomicBoolean scheduledForFlush = new AtomicBoolean();

    private final AtomicInteger scheduledWriteBytes = new AtomicInteger();

    private final AtomicInteger scheduledWriteMessages = new AtomicInteger();

    private long readBytes;

    private long writtenBytes;

    private long readMessages;

    private long writtenMessages;

    private long lastReadTime;

    private long lastWriteTime;

    private long lastThroughputCalculationTime;

    private long lastReadBytes;

    private long lastWrittenBytes;

    private long lastReadMessages;

    private long lastWrittenMessages;

    private double readBytesThroughput;

    private double writtenBytesThroughput;

    private double readMessagesThroughput;

    private double writtenMessagesThroughput;

    private AtomicInteger idleCountForBoth = new AtomicInteger();

    private AtomicInteger idleCountForRead = new AtomicInteger();

    private AtomicInteger idleCountForWrite = new AtomicInteger();

    private long lastIdleTimeForBoth;

    private long lastIdleTimeForRead;

    private long lastIdleTimeForWrite;

    private boolean deferDecreaseReadBuffer = true;

    /**
     * TODO Add method documentation
     */
    protected AbstractIoSession() {
        long currentTime = System.currentTimeMillis();
        creationTime = currentTime;
        lastThroughputCalculationTime = currentTime;
        lastReadTime = currentTime;
        lastWriteTime = currentTime;
        lastIdleTimeForBoth = currentTime;
        lastIdleTimeForRead = currentTime;
        lastIdleTimeForWrite = currentTime;
        closeFuture.addListener(SCHEDULED_COUNTER_RESETTER);
        sessionId = idGenerator.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     * 
     * We use an AtomicLong to guarantee that the session ID are
     * unique.
     */
    public final long getId() {
        return sessionId;
    }

    /**
     * @return The associated IoProcessor for this session
     */
    public abstract IoProcessor getProcessor();

    /**
     * {@inheritDoc}
     */
    public final boolean isConnected() {
        return !closeFuture.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isClosing() {
        return closing || closeFuture.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    public final CloseFuture getCloseFuture() {
        return closeFuture;
    }

    /**
     * Tells if the session is scheduled for flushed
     * @param true if the session is scheduled for flush
     */
    public final boolean isScheduledForFlush() {
        return scheduledForFlush.get();
    }

    /**
     * Schedule the session for flushed
     */
    public final void scheduledForFlush() {
        scheduledForFlush.set(true);
    }

    /**
     * Change the session's status : it's not anymore scheduled for flush
     */
    public final void unscheduledForFlush() {
        scheduledForFlush.set(false);
    }

    /**
     * Set the scheduledForFLush flag. As we may have concurrent access
     * to this flag, we compare and set it in one call.
     * @param schedule the new value to set if not already set.
     * @return true if the session flag has been set, and if 
     * it wasn't set already.
     */
    public final boolean setScheduledForFlush(boolean schedule) {
        if (schedule) {
            return scheduledForFlush.compareAndSet(false, schedule);
        }
        scheduledForFlush.set(schedule);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final CloseFuture close(boolean rightNow) {
        if (rightNow) {
            return close();
        }
        return closeOnFlush();
    }

    /**
     * {@inheritDoc}
     */
    public final CloseFuture close() {
        synchronized (lock) {
            if (isClosing()) {
                return closeFuture;
            }
            closing = true;
        }
        getFilterChain().fireFilterClose();
        return closeFuture;
    }

    private final CloseFuture closeOnFlush() {
        getWriteRequestQueue().offer(this, CLOSE_REQUEST);
        getProcessor().flush(this);
        return closeFuture;
    }

    /**
     * {@inheritDoc}
     */
    public final ReadFuture read() {
        if (!getConfig().isUseReadOperation()) {
            throw new IllegalStateException("useReadOperation is not enabled.");
        }
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        ReadFuture future;
        synchronized (readyReadFutures) {
            future = readyReadFutures.poll();
            if (future != null) {
                if (future.isClosed()) {
                    readyReadFutures.offer(future);
                }
            } else {
                future = new DefaultReadFuture(this);
                getWaitingReadFutures().offer(future);
            }
        }
        return future;
    }

    /**
     * TODO Add method documentation
     */
    public final void offerReadFuture(Object message) {
        newReadFuture().setRead(message);
    }

    /**
     * TODO Add method documentation
     */
    public final void offerFailedReadFuture(Throwable exception) {
        newReadFuture().setException(exception);
    }

    /**
     * TODO Add method documentation
     */
    public final void offerClosedReadFuture() {
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        synchronized (readyReadFutures) {
            newReadFuture().setClosed();
        }
    }

    /**
     * TODO Add method documentation
     */
    private ReadFuture newReadFuture() {
        Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
        Queue<ReadFuture> waitingReadFutures = getWaitingReadFutures();
        ReadFuture future;
        synchronized (readyReadFutures) {
            future = waitingReadFutures.poll();
            if (future == null) {
                future = new DefaultReadFuture(this);
                readyReadFutures.offer(future);
            }
        }
        return future;
    }

    /**
     * TODO Add method documentation
     */
    private Queue<ReadFuture> getReadyReadFutures() {
        Queue<ReadFuture> readyReadFutures = (Queue<ReadFuture>) getAttribute(READY_READ_FUTURES_KEY);
        if (readyReadFutures == null) {
            readyReadFutures = new ConcurrentLinkedQueue<ReadFuture>();
            Queue<ReadFuture> oldReadyReadFutures = (Queue<ReadFuture>) setAttributeIfAbsent(READY_READ_FUTURES_KEY, readyReadFutures);
            if (oldReadyReadFutures != null) {
                readyReadFutures = oldReadyReadFutures;
            }
        }
        return readyReadFutures;
    }

    /**
     * TODO Add method documentation
     */
    private Queue<ReadFuture> getWaitingReadFutures() {
        Queue<ReadFuture> waitingReadyReadFutures = (Queue<ReadFuture>) getAttribute(WAITING_READ_FUTURES_KEY);
        if (waitingReadyReadFutures == null) {
            waitingReadyReadFutures = new ConcurrentLinkedQueue<ReadFuture>();
            Queue<ReadFuture> oldWaitingReadyReadFutures = (Queue<ReadFuture>) setAttributeIfAbsent(WAITING_READ_FUTURES_KEY, waitingReadyReadFutures);
            if (oldWaitingReadyReadFutures != null) {
                waitingReadyReadFutures = oldWaitingReadyReadFutures;
            }
        }
        return waitingReadyReadFutures;
    }

    /**
     * {@inheritDoc}
     */
    public WriteFuture write(Object message) {
        return write(message, null);
    }

    /**
     * {@inheritDoc}
     */
    public WriteFuture write(Object message, SocketAddress remoteAddress) {
        if (message == null) {
            throw new IllegalArgumentException("message");
        }
        if (!getTransportMetadata().isConnectionless() && remoteAddress != null) {
            throw new UnsupportedOperationException();
        }
        if (isClosing() || !isConnected()) {
            WriteFuture future = new DefaultWriteFuture(this);
            WriteRequest request = new DefaultWriteRequest(message, future, remoteAddress);
            WriteException writeException = new WriteToClosedSessionException(request);
            future.setException(writeException);
            return future;
        }
        FileChannel openedFileChannel = null;
        try {
            if (message instanceof IoBuffer && !((IoBuffer) message).hasRemaining()) {
                throw new IllegalArgumentException("message is empty. Forgot to call flip()?");
            } else if (message instanceof FileChannel) {
                FileChannel fileChannel = (FileChannel) message;
                message = new DefaultFileRegion(fileChannel, 0, fileChannel.size());
            } else if (message instanceof File) {
                File file = (File) message;
                openedFileChannel = new FileInputStream(file).getChannel();
                message = new FilenameFileRegion(file, openedFileChannel, 0, openedFileChannel.size());
            }
        } catch (IOException e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
            return DefaultWriteFuture.newNotWrittenFuture(this, e);
        }
        WriteFuture writeFuture = new DefaultWriteFuture(this);
        WriteRequest writeRequest = new DefaultWriteRequest(message, writeFuture, remoteAddress);
        IoFilterChain filterChain = getFilterChain();
        filterChain.fireFilterWrite(writeRequest);
        if (openedFileChannel != null) {
            final FileChannel finalChannel = openedFileChannel;
            writeFuture.addListener(new IoFutureListener<WriteFuture>() {

                public void operationComplete(WriteFuture future) {
                    try {
                        finalChannel.close();
                    } catch (IOException e) {
                        ExceptionMonitor.getInstance().exceptionCaught(e);
                    }
                }
            });
        }
        return writeFuture;
    }

    /**
     * {@inheritDoc}
     */
    public final Object getAttachment() {
        return getAttribute("");
    }

    /**
     * {@inheritDoc}
     */
    public final Object setAttachment(Object attachment) {
        return setAttribute("", attachment);
    }

    /**
     * {@inheritDoc}
     */
    public final Object getAttribute(Object key) {
        return getAttribute(key, null);
    }

    /**
     * {@inheritDoc}
     */
    public final Object getAttribute(Object key, Object defaultValue) {
        return attributes.getAttribute(this, key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    public final Object setAttribute(Object key, Object value) {
        return attributes.setAttribute(this, key, value);
    }

    /**
     * {@inheritDoc}
     */
    public final Object setAttribute(Object key) {
        return setAttribute(key, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    public final Object setAttributeIfAbsent(Object key, Object value) {
        return attributes.setAttributeIfAbsent(this, key, value);
    }

    /**
     * {@inheritDoc}
     */
    public final Object setAttributeIfAbsent(Object key) {
        return setAttributeIfAbsent(key, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    public final Object removeAttribute(Object key) {
        return attributes.removeAttribute(this, key);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean removeAttribute(Object key, Object value) {
        return attributes.removeAttribute(this, key, value);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
        return attributes.replaceAttribute(this, key, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean containsAttribute(Object key) {
        return attributes.containsAttribute(this, key);
    }

    /**
     * {@inheritDoc}
     */
    public final Set<Object> getAttributeKeys() {
        return attributes.getAttributeKeys(this);
    }

    /**
     * TODO Add method documentation
     */
    public final IoSessionAttributeMap getAttributeMap() {
        return attributes;
    }

    /**
     * TODO Add method documentation
     */
    public final void setAttributeMap(IoSessionAttributeMap attributes) {
        this.attributes = attributes;
    }

    /**
     * Create a new close aware write queue, based on the given write queue.
     * 
     * @param writeRequestQueue The write request queue
     */
    public final void setWriteRequestQueue(WriteRequestQueue writeRequestQueue) {
        this.writeRequestQueue = new CloseAwareWriteQueue(writeRequestQueue);
    }

    /**
     * {@inheritDoc}
     */
    public final void suspendRead() {
        readSuspended = true;
        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateTrafficControl(this);
    }

    /**
     * {@inheritDoc}
     */
    public final void suspendWrite() {
        writeSuspended = true;
        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateTrafficControl(this);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public final void resumeRead() {
        readSuspended = false;
        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateTrafficControl(this);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public final void resumeWrite() {
        writeSuspended = false;
        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateTrafficControl(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadSuspended() {
        return readSuspended;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWriteSuspended() {
        return writeSuspended;
    }

    /**
     * {@inheritDoc}
     */
    public final long getReadBytes() {
        return readBytes;
    }

    /**
     * {@inheritDoc}
     */
    public final long getWrittenBytes() {
        return writtenBytes;
    }

    /**
     * {@inheritDoc}
     */
    public final long getReadMessages() {
        return readMessages;
    }

    /**
     * {@inheritDoc}
     */
    public final long getWrittenMessages() {
        return writtenMessages;
    }

    /**
     * {@inheritDoc}
     */
    public final double getReadBytesThroughput() {
        return readBytesThroughput;
    }

    /**
     * {@inheritDoc}
     */
    public final double getWrittenBytesThroughput() {
        return writtenBytesThroughput;
    }

    /**
     * {@inheritDoc}
     */
    public final double getReadMessagesThroughput() {
        return readMessagesThroughput;
    }

    /**
     * {@inheritDoc}
     */
    public final double getWrittenMessagesThroughput() {
        return writtenMessagesThroughput;
    }

    /**
     * {@inheritDoc}
     */
    public final void updateThroughput(long currentTime, boolean force) {
        int interval = (int) (currentTime - lastThroughputCalculationTime);
        long minInterval = getConfig().getThroughputCalculationIntervalInMillis();
        if (minInterval == 0 || interval < minInterval) {
            if (!force) {
                return;
            }
        }
        readBytesThroughput = (readBytes - lastReadBytes) * 1000.0 / interval;
        writtenBytesThroughput = (writtenBytes - lastWrittenBytes) * 1000.0 / interval;
        readMessagesThroughput = (readMessages - lastReadMessages) * 1000.0 / interval;
        writtenMessagesThroughput = (writtenMessages - lastWrittenMessages) * 1000.0 / interval;
        lastReadBytes = readBytes;
        lastWrittenBytes = writtenBytes;
        lastReadMessages = readMessages;
        lastWrittenMessages = writtenMessages;
        lastThroughputCalculationTime = currentTime;
    }

    /**
     * {@inheritDoc}
     */
    public final long getScheduledWriteBytes() {
        return scheduledWriteBytes.get();
    }

    /**
     * {@inheritDoc}
     */
    public final int getScheduledWriteMessages() {
        return scheduledWriteMessages.get();
    }

    /**
     * TODO Add method documentation
     */
    protected void setScheduledWriteBytes(int byteCount) {
        scheduledWriteBytes.set(byteCount);
    }

    /**
     * TODO Add method documentation
     */
    protected void setScheduledWriteMessages(int messages) {
        scheduledWriteMessages.set(messages);
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseReadBytes(long increment, long currentTime) {
        if (increment <= 0) {
            return;
        }
        readBytes += increment;
        lastReadTime = currentTime;
        idleCountForBoth.set(0);
        idleCountForRead.set(0);
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseReadBytes(increment, currentTime);
        }
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseReadMessages(long currentTime) {
        readMessages++;
        lastReadTime = currentTime;
        idleCountForBoth.set(0);
        idleCountForRead.set(0);
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseReadMessages(currentTime);
        }
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseWrittenBytes(int increment, long currentTime) {
        if (increment <= 0) {
            return;
        }
        writtenBytes += increment;
        lastWriteTime = currentTime;
        idleCountForBoth.set(0);
        idleCountForWrite.set(0);
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseWrittenBytes(increment, currentTime);
        }
        increaseScheduledWriteBytes(-increment);
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseWrittenMessages(WriteRequest request, long currentTime) {
        Object message = request.getMessage();
        if (message instanceof IoBuffer) {
            IoBuffer b = (IoBuffer) message;
            if (b.hasRemaining()) {
                return;
            }
        }
        writtenMessages++;
        lastWriteTime = currentTime;
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseWrittenMessages(currentTime);
        }
        decreaseScheduledWriteMessages();
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseScheduledWriteBytes(int increment) {
        scheduledWriteBytes.addAndGet(increment);
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseScheduledWriteBytes(increment);
        }
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseScheduledWriteMessages() {
        scheduledWriteMessages.incrementAndGet();
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().increaseScheduledWriteMessages();
        }
    }

    /**
     * TODO Add method documentation
     */
    private void decreaseScheduledWriteMessages() {
        scheduledWriteMessages.decrementAndGet();
        if (getService() instanceof AbstractIoService) {
            ((AbstractIoService) getService()).getStatistics().decreaseScheduledWriteMessages();
        }
    }

    /**
     * TODO Add method documentation
     */
    public final void decreaseScheduledBytesAndMessages(WriteRequest request) {
        Object message = request.getMessage();
        if (message instanceof IoBuffer) {
            IoBuffer b = (IoBuffer) message;
            if (b.hasRemaining()) {
                increaseScheduledWriteBytes(-((IoBuffer) message).remaining());
            } else {
                decreaseScheduledWriteMessages();
            }
        } else {
            decreaseScheduledWriteMessages();
        }
    }

    /**
     * {@inheritDoc}
     */
    public final WriteRequestQueue getWriteRequestQueue() {
        if (writeRequestQueue == null) {
            throw new IllegalStateException();
        }
        return writeRequestQueue;
    }

    /**
     * {@inheritDoc}
     */
    public final WriteRequest getCurrentWriteRequest() {
        return currentWriteRequest;
    }

    /**
     * {@inheritDoc}
     */
    public final Object getCurrentWriteMessage() {
        WriteRequest req = getCurrentWriteRequest();
        if (req == null) {
            return null;
        }
        return req.getMessage();
    }

    /**
     * {@inheritDoc}
     */
    public final void setCurrentWriteRequest(WriteRequest currentWriteRequest) {
        this.currentWriteRequest = currentWriteRequest;
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseReadBufferSize() {
        int newReadBufferSize = getConfig().getReadBufferSize() << 1;
        if (newReadBufferSize <= getConfig().getMaxReadBufferSize()) {
            getConfig().setReadBufferSize(newReadBufferSize);
        } else {
            getConfig().setReadBufferSize(getConfig().getMaxReadBufferSize());
        }
        deferDecreaseReadBuffer = true;
    }

    /**
     * TODO Add method documentation
     */
    public final void decreaseReadBufferSize() {
        if (deferDecreaseReadBuffer) {
            deferDecreaseReadBuffer = false;
            return;
        }
        if (getConfig().getReadBufferSize() > getConfig().getMinReadBufferSize()) {
            getConfig().setReadBufferSize(getConfig().getReadBufferSize() >>> 1);
        }
        deferDecreaseReadBuffer = true;
    }

    /**
     * {@inheritDoc}
     */
    public final long getCreationTime() {
        return creationTime;
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastIoTime() {
        return Math.max(lastReadTime, lastWriteTime);
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastReadTime() {
        return lastReadTime;
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastWriteTime() {
        return lastWriteTime;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isIdle(IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return idleCountForBoth.get() > 0;
        }
        if (status == IdleStatus.READER_IDLE) {
            return idleCountForRead.get() > 0;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return idleCountForWrite.get() > 0;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isBothIdle() {
        return isIdle(IdleStatus.BOTH_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isReaderIdle() {
        return isIdle(IdleStatus.READER_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isWriterIdle() {
        return isIdle(IdleStatus.WRITER_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final int getIdleCount(IdleStatus status) {
        if (getConfig().getIdleTime(status) == 0) {
            if (status == IdleStatus.BOTH_IDLE) {
                idleCountForBoth.set(0);
            }
            if (status == IdleStatus.READER_IDLE) {
                idleCountForRead.set(0);
            }
            if (status == IdleStatus.WRITER_IDLE) {
                idleCountForWrite.set(0);
            }
        }
        if (status == IdleStatus.BOTH_IDLE) {
            return idleCountForBoth.get();
        }
        if (status == IdleStatus.READER_IDLE) {
            return idleCountForRead.get();
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return idleCountForWrite.get();
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastIdleTime(IdleStatus status) {
        if (status == IdleStatus.BOTH_IDLE) {
            return lastIdleTimeForBoth;
        }
        if (status == IdleStatus.READER_IDLE) {
            return lastIdleTimeForRead;
        }
        if (status == IdleStatus.WRITER_IDLE) {
            return lastIdleTimeForWrite;
        }
        throw new IllegalArgumentException("Unknown idle status: " + status);
    }

    /**
     * TODO Add method documentation
     */
    public final void increaseIdleCount(IdleStatus status, long currentTime) {
        if (status == IdleStatus.BOTH_IDLE) {
            idleCountForBoth.incrementAndGet();
            lastIdleTimeForBoth = currentTime;
        } else if (status == IdleStatus.READER_IDLE) {
            idleCountForRead.incrementAndGet();
            lastIdleTimeForRead = currentTime;
        } else if (status == IdleStatus.WRITER_IDLE) {
            idleCountForWrite.incrementAndGet();
            lastIdleTimeForWrite = currentTime;
        } else {
            throw new IllegalArgumentException("Unknown idle status: " + status);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final int getBothIdleCount() {
        return getIdleCount(IdleStatus.BOTH_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastBothIdleTime() {
        return getLastIdleTime(IdleStatus.BOTH_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastReaderIdleTime() {
        return getLastIdleTime(IdleStatus.READER_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final long getLastWriterIdleTime() {
        return getLastIdleTime(IdleStatus.WRITER_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final int getReaderIdleCount() {
        return getIdleCount(IdleStatus.READER_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public final int getWriterIdleCount() {
        return getIdleCount(IdleStatus.WRITER_IDLE);
    }

    /**
     * {@inheritDoc}
     */
    public SocketAddress getServiceAddress() {
        IoService service = getService();
        if (service instanceof IoAcceptor) {
            return ((IoAcceptor) service).getLocalAddress();
        }
        return getRemoteAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     * TODO This is a ridiculous implementation. Need to be replaced.
     */
    @Override
    public final boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (isConnected() || isClosing()) {
            try {
                SocketAddress remote = getRemoteAddress();
                SocketAddress local = getLocalAddress();
                if (getService() instanceof IoAcceptor) {
                    return "(" + getIdAsString() + ": " + getServiceName() + ", server, " + remote + " => " + local + ')';
                }
                return "(" + getIdAsString() + ": " + getServiceName() + ", client, " + local + " => " + remote + ')';
            } catch (Exception e) {
                return "Session is disconnecting ...";
            }
        }
        return "Session disconnected ...";
    }

    /**
     * TODO Add method documentation
     */
    private String getIdAsString() {
        String id = Long.toHexString(getId()).toUpperCase();
        while (id.length() < 8) {
            id = '0' + id;
        }
        id = "0x" + id;
        return id;
    }

    /**
     * TODO Add method documentation
     */
    private String getServiceName() {
        TransportMetadata tm = getTransportMetadata();
        if (tm == null) {
            return "null";
        }
        return tm.getProviderName() + ' ' + tm.getName();
    }

    /**
     * Fires a {@link IoEventType#SESSION_IDLE} event to any applicable
     * sessions in the specified collection.
     *
     * @param currentTime the current time (i.e. {@link System#currentTimeMillis()})
     */
    public static void notifyIdleness(Iterator<? extends IoSession> sessions, long currentTime) {
        IoSession s = null;
        while (sessions.hasNext()) {
            s = sessions.next();
            notifyIdleSession(s, currentTime);
        }
    }

    /**
     * Fires a {@link IoEventType#SESSION_IDLE} event if applicable for the
     * specified {@code session}.
     *
     * @param currentTime the current time (i.e. {@link System#currentTimeMillis()})
     */
    public static void notifyIdleSession(IoSession session, long currentTime) {
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.BOTH_IDLE), IdleStatus.BOTH_IDLE, Math.max(session.getLastIoTime(), session.getLastIdleTime(IdleStatus.BOTH_IDLE)));
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.READER_IDLE), IdleStatus.READER_IDLE, Math.max(session.getLastReadTime(), session.getLastIdleTime(IdleStatus.READER_IDLE)));
        notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.WRITER_IDLE), IdleStatus.WRITER_IDLE, Math.max(session.getLastWriteTime(), session.getLastIdleTime(IdleStatus.WRITER_IDLE)));
        notifyWriteTimeout(session, currentTime);
    }

    private static void notifyIdleSession0(IoSession session, long currentTime, long idleTime, IdleStatus status, long lastIoTime) {
        if (idleTime > 0 && lastIoTime != 0 && currentTime - lastIoTime >= idleTime) {
            session.getFilterChain().fireSessionIdle(status);
        }
    }

    private static void notifyWriteTimeout(IoSession session, long currentTime) {
        long writeTimeout = session.getConfig().getWriteTimeoutInMillis();
        if (writeTimeout > 0 && currentTime - session.getLastWriteTime() >= writeTimeout && !session.getWriteRequestQueue().isEmpty(session)) {
            WriteRequest request = session.getCurrentWriteRequest();
            if (request != null) {
                session.setCurrentWriteRequest(null);
                WriteTimeoutException cause = new WriteTimeoutException(request);
                request.getFuture().setException(cause);
                session.getFilterChain().fireExceptionCaught(cause);
                session.close(true);
            }
        }
    }

    /**
     * A queue which handles the CLOSE request.
     * 
     * TODO : Check that when closing a session, all the pending
     * requests are correctly sent.
     */
    private class CloseAwareWriteQueue implements WriteRequestQueue {

        private final WriteRequestQueue queue;

        /**
         * {@inheritDoc}
         */
        public CloseAwareWriteQueue(WriteRequestQueue queue) {
            this.queue = queue;
        }

        /**
         * {@inheritDoc}
         */
        public synchronized WriteRequest poll(IoSession session) {
            WriteRequest answer = queue.poll(session);
            if (answer == CLOSE_REQUEST) {
                AbstractIoSession.this.close();
                dispose(session);
                answer = null;
            }
            return answer;
        }

        /**
         * {@inheritDoc}
         */
        public void offer(IoSession session, WriteRequest e) {
            queue.offer(session, e);
        }

        /**
         * {@inheritDoc}
         */
        public boolean isEmpty(IoSession session) {
            return queue.isEmpty(session);
        }

        /**
         * {@inheritDoc}
         */
        public void clear(IoSession session) {
            queue.clear(session);
        }

        /**
         * {@inheritDoc}
         */
        public void dispose(IoSession session) {
            queue.dispose(session);
        }
    }
}
