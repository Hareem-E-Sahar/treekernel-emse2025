package org.apache.tomcat.util.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.MutableInteger;
import org.apache.tomcat.util.net.NioEndpoint.KeyAttachment;
import java.util.concurrent.atomic.AtomicInteger;

public class NioBlockingSelector {

    protected static Log log = LogFactory.getLog(NioBlockingSelector.class);

    private static int threadCounter = 0;

    protected Selector sharedSelector;

    protected BlockPoller poller;

    public NioBlockingSelector() {
    }

    public void open(Selector selector) {
        sharedSelector = selector;
        poller = new BlockPoller();
        poller.selector = sharedSelector;
        poller.setDaemon(true);
        poller.setName("NioBlockingSelector.BlockPoller-" + (++threadCounter));
        poller.start();
    }

    public void close() {
        if (poller != null) {
            poller.disable();
            poller.interrupt();
            poller = null;
        }
    }

    /**
     * Performs a blocking write using the bytebuffer for data to be written
     * If the <code>selector</code> parameter is null, then it will perform a busy write that could
     * take up a lot of CPU cycles.
     * @param buf ByteBuffer - the buffer containing the data, we will write as long as <code>(buf.hasRemaining()==true)</code>
     * @param socket SocketChannel - the socket to write data to
     * @param writeTimeout long - the timeout for this write operation in milliseconds, -1 means no timeout
     * @return int - returns the number of bytes written
     * @throws EOFException if write returns -1
     * @throws SocketTimeoutException if the write times out
     * @throws IOException if an IO Exception occurs in the underlying socket logic
     */
    public int write(ByteBuffer buf, NioChannel socket, long writeTimeout, MutableInteger lastWrite) throws IOException {
        SelectionKey key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
        if (key == null) throw new IOException("Key no longer registered");
        KeyAttachment att = (KeyAttachment) key.attachment();
        int written = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ((!timedout) && buf.hasRemaining()) {
                if (keycount > 0) {
                    int cnt = socket.write(buf);
                    lastWrite.set(cnt);
                    if (cnt == -1) throw new EOFException();
                    written += cnt;
                    if (cnt > 0) {
                        time = System.currentTimeMillis();
                        continue;
                    }
                }
                try {
                    if (att.getWriteLatch() == null || att.getWriteLatch().getCount() == 0) att.startWriteLatch(1);
                    poller.add(att, SelectionKey.OP_WRITE);
                    att.awaitWriteLatch(writeTimeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignore) {
                    Thread.interrupted();
                }
                if (att.getWriteLatch() != null && att.getWriteLatch().getCount() > 0) {
                    keycount = 0;
                } else {
                    keycount = 1;
                    att.resetWriteLatch();
                }
                if (writeTimeout > 0 && (keycount == 0)) timedout = (System.currentTimeMillis() - time) >= writeTimeout;
            }
            if (timedout) throw new SocketTimeoutException();
        } finally {
            poller.remove(att, SelectionKey.OP_WRITE);
            if (timedout && key != null) {
                poller.cancelKey(socket, key);
            }
        }
        return written;
    }

    /**
     * Performs a blocking read using the bytebuffer for data to be read
     * If the <code>selector</code> parameter is null, then it will perform a busy read that could
     * take up a lot of CPU cycles.
     * @param buf ByteBuffer - the buffer containing the data, we will read as until we have read at least one byte or we timed out
     * @param socket SocketChannel - the socket to write data to
     * @param selector Selector - the selector to use for blocking, if null then a busy read will be initiated
     * @param readTimeout long - the timeout for this read operation in milliseconds, -1 means no timeout
     * @return int - returns the number of bytes read
     * @throws EOFException if read returns -1
     * @throws SocketTimeoutException if the read times out
     * @throws IOException if an IO Exception occurs in the underlying socket logic
     */
    public int read(ByteBuffer buf, NioChannel socket, long readTimeout) throws IOException {
        SelectionKey key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
        if (key == null) throw new IOException("Key no longer registered");
        KeyAttachment att = (KeyAttachment) key.attachment();
        int read = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ((!timedout) && read == 0) {
                if (keycount > 0) {
                    int cnt = socket.read(buf);
                    if (cnt == -1) throw new EOFException();
                    read += cnt;
                    if (cnt > 0) break;
                }
                try {
                    if (att.getReadLatch() == null || att.getReadLatch().getCount() == 0) att.startReadLatch(1);
                    poller.add(att, SelectionKey.OP_READ);
                    att.awaitReadLatch(readTimeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignore) {
                    Thread.interrupted();
                }
                if (att.getReadLatch() != null && att.getReadLatch().getCount() > 0) {
                    keycount = 0;
                } else {
                    keycount = 1;
                    att.resetReadLatch();
                }
                if (readTimeout > 0 && (keycount == 0)) timedout = (System.currentTimeMillis() - time) >= readTimeout;
            }
            if (timedout) throw new SocketTimeoutException();
        } finally {
            poller.remove(att, SelectionKey.OP_READ);
            if (timedout && key != null) {
                poller.cancelKey(socket, key);
            }
        }
        return read;
    }

    protected class BlockPoller extends Thread {

        protected boolean run = true;

        protected Selector selector = null;

        protected ConcurrentLinkedQueue events = new ConcurrentLinkedQueue();

        public void disable() {
            run = false;
            selector.wakeup();
        }

        protected AtomicInteger wakeupCounter = new AtomicInteger(0);

        public void cancelKey(final NioChannel socket, final SelectionKey key) {
            Runnable r = new Runnable() {

                public void run() {
                    key.cancel();
                }
            };
            events.offer(r);
            wakeup();
        }

        public void wakeup() {
            if (wakeupCounter.addAndGet(1) == 0) selector.wakeup();
        }

        public void cancel(SelectionKey sk, KeyAttachment key, int ops) {
            if (sk != null) {
                sk.cancel();
                sk.attach(null);
                if (SelectionKey.OP_WRITE == (ops & SelectionKey.OP_WRITE)) countDown(key.getWriteLatch());
                if (SelectionKey.OP_READ == (ops & SelectionKey.OP_READ)) countDown(key.getReadLatch());
            }
        }

        public void add(final KeyAttachment key, final int ops) {
            Runnable r = new Runnable() {

                public void run() {
                    if (key == null) return;
                    NioChannel nch = key.getChannel();
                    if (nch == null) return;
                    SocketChannel ch = nch.getIOChannel();
                    if (ch == null) return;
                    SelectionKey sk = ch.keyFor(selector);
                    try {
                        if (sk == null) {
                            sk = ch.register(selector, ops, key);
                        } else {
                            sk.interestOps(sk.interestOps() | ops);
                        }
                    } catch (CancelledKeyException cx) {
                        cancel(sk, key, ops);
                    } catch (ClosedChannelException cx) {
                        cancel(sk, key, ops);
                    }
                }
            };
            events.offer(r);
            wakeup();
        }

        public void remove(final KeyAttachment key, final int ops) {
            Runnable r = new Runnable() {

                public void run() {
                    if (key == null) return;
                    NioChannel nch = key.getChannel();
                    if (nch == null) return;
                    SocketChannel ch = nch.getIOChannel();
                    if (ch == null) return;
                    SelectionKey sk = ch.keyFor(selector);
                    try {
                        if (sk == null) {
                            if (SelectionKey.OP_WRITE == (ops & SelectionKey.OP_WRITE)) countDown(key.getWriteLatch());
                            if (SelectionKey.OP_READ == (ops & SelectionKey.OP_READ)) countDown(key.getReadLatch());
                        } else {
                            sk.interestOps(sk.interestOps() & (~ops));
                            if (SelectionKey.OP_WRITE == (ops & SelectionKey.OP_WRITE)) countDown(key.getWriteLatch());
                            if (SelectionKey.OP_READ == (ops & SelectionKey.OP_READ)) countDown(key.getReadLatch());
                            if (sk.interestOps() == 0) {
                                sk.cancel();
                                sk.attach(null);
                            }
                        }
                    } catch (CancelledKeyException cx) {
                        if (sk != null) {
                            sk.cancel();
                            sk.attach(null);
                        }
                    }
                }
            };
            events.offer(r);
            wakeup();
        }

        public boolean events() {
            boolean result = false;
            Runnable r = null;
            result = (events.size() > 0);
            while ((r = (Runnable) events.poll()) != null) {
                r.run();
                result = true;
            }
            return result;
        }

        public void run() {
            while (run) {
                try {
                    events();
                    int keyCount = 0;
                    try {
                        int i = wakeupCounter.get();
                        if (i > 0) keyCount = selector.selectNow(); else {
                            wakeupCounter.set(-1);
                            keyCount = selector.select(1000);
                        }
                        wakeupCounter.set(0);
                        if (!run) break;
                    } catch (NullPointerException x) {
                        if (selector == null) throw x;
                        if (log.isDebugEnabled()) log.debug("Possibly encountered sun bug 5076772 on windows JDK 1.5", x);
                        continue;
                    } catch (CancelledKeyException x) {
                        if (log.isDebugEnabled()) log.debug("Possibly encountered sun bug 5076772 on windows JDK 1.5", x);
                        continue;
                    } catch (Throwable x) {
                        log.error("", x);
                        continue;
                    }
                    Iterator iterator = keyCount > 0 ? selector.selectedKeys().iterator() : null;
                    while (run && iterator != null && iterator.hasNext()) {
                        SelectionKey sk = (SelectionKey) iterator.next();
                        KeyAttachment attachment = (KeyAttachment) sk.attachment();
                        try {
                            attachment.access();
                            iterator.remove();
                            ;
                            sk.interestOps(sk.interestOps() & (~sk.readyOps()));
                            if (sk.isReadable()) {
                                countDown(attachment.getReadLatch());
                            }
                            if (sk.isWritable()) {
                                countDown(attachment.getWriteLatch());
                            }
                        } catch (CancelledKeyException ckx) {
                            if (sk != null) sk.cancel();
                            countDown(attachment.getReadLatch());
                            countDown(attachment.getWriteLatch());
                        }
                    }
                } catch (Throwable t) {
                    log.error("", t);
                }
            }
            events.clear();
            try {
                selector.selectNow();
            } catch (Exception ignore) {
                if (log.isDebugEnabled()) log.debug("", ignore);
            }
        }

        public void countDown(CountDownLatch latch) {
            if (latch == null) return;
            latch.countDown();
        }
    }
}
