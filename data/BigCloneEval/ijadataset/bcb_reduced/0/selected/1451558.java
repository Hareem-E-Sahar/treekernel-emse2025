package org.jgroups.blocks;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jgroups.Address;
import org.jgroups.Global;
import org.jgroups.logging.Log;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.ShutdownRejectedExecutionHandler;

/**
 * Manages incoming and outgoing TCP connections. For each outgoing message to destination P, if there
 * is not yet a connection for P, one will be created. Subsequent outgoing messages will use this
 * connection.  For incoming messages, one server socket is created at startup. For each new incoming
 * client connecting, a new thread from a thread pool is allocated and listens for incoming messages
 * until the socket is closed by the peer.<br>Sockets/threads with no activity will be killed
 * after some time.
 * <p/>
 * Incoming messages from any of the sockets can be received by setting the message listener.
 *
 * @author Bela Ban, Scott Marlow, Alex Fu
 */
public class ConnectionTableNIO extends BasicConnectionTable implements Runnable {

    private ServerSocketChannel m_serverSocketChannel;

    private Selector m_acceptSelector;

    private WriteHandler[] m_writeHandlers;

    private int m_nextWriteHandler = 0;

    private final Object m_lockNextWriteHandler = new Object();

    private ReadHandler[] m_readHandlers;

    private int m_nextReadHandler = 0;

    private final Object m_lockNextReadHandler = new Object();

    private Executor m_requestProcessors;

    private volatile boolean serverStopping = false;

    private final List<Thread> m_backGroundThreads = new LinkedList<Thread>();

    private int m_reader_threads = 3;

    private int m_writer_threads = 3;

    private int m_processor_threads = 5;

    private int m_processor_minThreads = 5;

    private int m_processor_maxThreads = 5;

    private int m_processor_queueSize = 100;

    private long m_processor_keepAliveTime = Long.MAX_VALUE;

    /**
    * @param srv_port
    * @throws Exception
    */
    public ConnectionTableNIO(int srv_port) throws Exception {
        this.srv_port = srv_port;
        start();
    }

    /**
    * @param srv_port
    * @param reaper_interval
    * @param conn_expire_time
    * @throws Exception
    */
    public ConnectionTableNIO(int srv_port, long reaper_interval, long conn_expire_time) throws Exception {
        this.srv_port = srv_port;
        this.reaper_interval = reaper_interval;
        this.conn_expire_time = conn_expire_time;
        start();
    }

    /**
    * @param r
    * @param bind_addr
    * @param external_addr
    * @param srv_port
    * @param max_port
    * @throws Exception
    */
    public ConnectionTableNIO(Receiver r, InetAddress bind_addr, InetAddress external_addr, int srv_port, int max_port) throws Exception {
        setReceiver(r);
        this.external_addr = external_addr;
        this.bind_addr = bind_addr;
        this.srv_port = srv_port;
        this.max_port = max_port;
        use_reaper = true;
        start();
    }

    public ConnectionTableNIO(Receiver r, InetAddress bind_addr, InetAddress external_addr, int srv_port, int max_port, boolean doStart) throws Exception {
        setReceiver(r);
        this.external_addr = external_addr;
        this.bind_addr = bind_addr;
        this.srv_port = srv_port;
        this.max_port = max_port;
        use_reaper = true;
        if (doStart) start();
    }

    /**
    * @param r
    * @param bind_addr
    * @param external_addr
    * @param srv_port
    * @param max_port
    * @param reaper_interval
    * @param conn_expire_time
    * @throws Exception
    */
    public ConnectionTableNIO(Receiver r, InetAddress bind_addr, InetAddress external_addr, int srv_port, int max_port, long reaper_interval, long conn_expire_time) throws Exception {
        setReceiver(r);
        this.bind_addr = bind_addr;
        this.external_addr = external_addr;
        this.srv_port = srv_port;
        this.max_port = max_port;
        this.reaper_interval = reaper_interval;
        this.conn_expire_time = conn_expire_time;
        use_reaper = true;
        start();
    }

    public ConnectionTableNIO(Receiver r, InetAddress bind_addr, InetAddress external_addr, int srv_port, int max_port, long reaper_interval, long conn_expire_time, boolean doStart) throws Exception {
        setReceiver(r);
        this.bind_addr = bind_addr;
        this.external_addr = external_addr;
        this.srv_port = srv_port;
        this.max_port = max_port;
        this.reaper_interval = reaper_interval;
        this.conn_expire_time = conn_expire_time;
        use_reaper = true;
        if (doStart) start();
    }

    public int getReaderThreads() {
        return m_reader_threads;
    }

    public void setReaderThreads(int m_reader_threads) {
        this.m_reader_threads = m_reader_threads;
    }

    public int getWriterThreads() {
        return m_writer_threads;
    }

    public void setWriterThreads(int m_writer_threads) {
        this.m_writer_threads = m_writer_threads;
    }

    public int getProcessorThreads() {
        return m_processor_threads;
    }

    public void setProcessorThreads(int m_processor_threads) {
        this.m_processor_threads = m_processor_threads;
    }

    public int getProcessorMinThreads() {
        return m_processor_minThreads;
    }

    public void setProcessorMinThreads(int m_processor_minThreads) {
        this.m_processor_minThreads = m_processor_minThreads;
    }

    public int getProcessorMaxThreads() {
        return m_processor_maxThreads;
    }

    public void setProcessorMaxThreads(int m_processor_maxThreads) {
        this.m_processor_maxThreads = m_processor_maxThreads;
    }

    public int getProcessorQueueSize() {
        return m_processor_queueSize;
    }

    public void setProcessorQueueSize(int m_processor_queueSize) {
        this.m_processor_queueSize = m_processor_queueSize;
    }

    public long getProcessorKeepAliveTime() {
        return m_processor_keepAliveTime;
    }

    public void setProcessorKeepAliveTime(long m_processor_keepAliveTime) {
        this.m_processor_keepAliveTime = m_processor_keepAliveTime;
    }

    /**
    * Try to obtain correct Connection (or create one if not yet existent)
    */
    ConnectionTable.Connection getConnection(Address dest) throws Exception {
        Connection conn;
        SocketChannel sock_ch;
        synchronized (conns) {
            conn = (Connection) conns.get(dest);
            if (conn == null) {
                InetSocketAddress destAddress = new InetSocketAddress(((IpAddress) dest).getIpAddress(), ((IpAddress) dest).getPort());
                sock_ch = SocketChannel.open(destAddress);
                sock_ch.socket().setTcpNoDelay(tcp_nodelay);
                conn = new Connection(sock_ch, dest);
                conn.sendLocalAddress(local_addr);
                sock_ch.configureBlocking(false);
                try {
                    if (log.isTraceEnabled()) log.trace("About to change new connection send buff size from " + sock_ch.socket().getSendBufferSize() + " bytes");
                    sock_ch.socket().setSendBufferSize(send_buf_size);
                    if (log.isTraceEnabled()) log.trace("Changed new connection send buff size to " + sock_ch.socket().getSendBufferSize() + " bytes");
                } catch (IllegalArgumentException ex) {
                    if (log.isErrorEnabled()) log.error("exception setting send buffer size to " + send_buf_size + " bytes: " + ex);
                }
                try {
                    if (log.isTraceEnabled()) log.trace("About to change new connection receive buff size from " + sock_ch.socket().getReceiveBufferSize() + " bytes");
                    sock_ch.socket().setReceiveBufferSize(recv_buf_size);
                    if (log.isTraceEnabled()) log.trace("Changed new connection receive buff size to " + sock_ch.socket().getReceiveBufferSize() + " bytes");
                } catch (IllegalArgumentException ex) {
                    if (log.isErrorEnabled()) log.error("exception setting receive buffer size to " + send_buf_size + " bytes: " + ex);
                }
                int idx;
                synchronized (m_lockNextWriteHandler) {
                    idx = m_nextWriteHandler = (m_nextWriteHandler + 1) % m_writeHandlers.length;
                }
                conn.setupWriteHandler(m_writeHandlers[idx]);
                try {
                    synchronized (m_lockNextReadHandler) {
                        idx = m_nextReadHandler = (m_nextReadHandler + 1) % m_readHandlers.length;
                    }
                    m_readHandlers[idx].add(conn);
                } catch (InterruptedException e) {
                    if (log.isWarnEnabled()) log.warn("Thread (" + Thread.currentThread().getName() + ") was interrupted, closing connection", e);
                    conn.destroy();
                    throw e;
                }
                addConnection(dest, conn);
                notifyConnectionOpened(dest);
                if (log.isTraceEnabled()) log.trace("created socket to " + dest);
            }
            return conn;
        }
    }

    public final void start() throws Exception {
        super.start();
        init();
        srv_sock = createServerSocket(srv_port, max_port);
        if (external_addr != null) local_addr = new IpAddress(external_addr, srv_sock.getLocalPort()); else if (bind_addr != null) local_addr = new IpAddress(bind_addr, srv_sock.getLocalPort()); else local_addr = new IpAddress(srv_sock.getLocalPort());
        if (log.isDebugEnabled()) log.debug("server socket created on " + local_addr);
        acceptor = getThreadFactory().newThread(thread_group, this, "ConnectionTable.AcceptorThread");
        acceptor.setDaemon(true);
        acceptor.start();
        m_backGroundThreads.add(acceptor);
        if (use_reaper && reaper == null) {
            reaper = new Reaper();
            reaper.start();
        }
    }

    protected void init() throws Exception {
        if (getProcessorMaxThreads() <= 0) {
            m_requestProcessors = new Executor() {

                public void execute(Runnable command) {
                    command.run();
                }
            };
        } else {
            ThreadPoolExecutor requestProcessors = new ThreadPoolExecutor(getProcessorMinThreads(), getProcessorMaxThreads(), getProcessorKeepAliveTime(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(getProcessorQueueSize()));
            requestProcessors.setThreadFactory(new ThreadFactory() {

                public Thread newThread(Runnable runnable) {
                    Thread new_thread = new Thread(thread_group, runnable);
                    new_thread.setDaemon(true);
                    new_thread.setName("ConnectionTableNIO.Thread");
                    m_backGroundThreads.add(new_thread);
                    return new_thread;
                }
            });
            requestProcessors.setRejectedExecutionHandler(new ShutdownRejectedExecutionHandler(requestProcessors.getRejectedExecutionHandler()));
            m_requestProcessors = requestProcessors;
        }
        m_writeHandlers = WriteHandler.create(getThreadFactory(), getWriterThreads(), thread_group, m_backGroundThreads, log);
        m_readHandlers = ReadHandler.create(getThreadFactory(), getReaderThreads(), this, thread_group, m_backGroundThreads, log);
    }

    /**
    * Closes all open sockets, the server socket and all threads waiting for incoming messages
    */
    public void stop() {
        super.stop();
        serverStopping = true;
        if (reaper != null) reaper.stop();
        if (m_acceptSelector != null) m_acceptSelector.wakeup();
        if (m_readHandlers != null) {
            for (int i = 0; i < m_readHandlers.length; i++) {
                try {
                    m_readHandlers[i].add(new Shutdown());
                } catch (InterruptedException e) {
                    log.error("Thread (" + Thread.currentThread().getName() + ") was interrupted, failed to shutdown selector", e);
                }
            }
        }
        if (m_writeHandlers != null) {
            for (int i = 0; i < m_writeHandlers.length; i++) {
                try {
                    m_writeHandlers[i].queue.put(new Shutdown());
                    m_writeHandlers[i].selector.wakeup();
                } catch (InterruptedException e) {
                    log.error("Thread (" + Thread.currentThread().getName() + ") was interrupted, failed to shutdown selector", e);
                }
            }
        }
        if (m_requestProcessors instanceof ThreadPoolExecutor) ((ThreadPoolExecutor) m_requestProcessors).shutdownNow();
        if (m_requestProcessors instanceof ThreadPoolExecutor) {
            try {
                ((ThreadPoolExecutor) m_requestProcessors).awaitTermination(Global.THREADPOOL_SHUTDOWN_WAIT_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
        }
        synchronized (conns) {
            Iterator it = conns.values().iterator();
            while (it.hasNext()) {
                Connection conn = (Connection) it.next();
                conn.destroy();
            }
            conns.clear();
        }
        while (!m_backGroundThreads.isEmpty()) {
            Thread t = m_backGroundThreads.remove(0);
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("Thread (" + Thread.currentThread().getName() + ") was interrupted while waiting on thread " + t.getName() + " to finish.");
            }
        }
        m_backGroundThreads.clear();
    }

    /**
    * Acceptor thread. Continuously accept new connections and assign readhandler/writehandler
    * to them.
    */
    public void run() {
        Connection conn;
        while (m_serverSocketChannel.isOpen() && !serverStopping) {
            int num;
            try {
                num = m_acceptSelector.select();
            } catch (IOException e) {
                if (log.isWarnEnabled()) log.warn("Select operation on listening socket failed", e);
                continue;
            }
            if (num > 0) {
                Set<SelectionKey> readyKeys = m_acceptSelector.selectedKeys();
                for (Iterator<SelectionKey> i = readyKeys.iterator(); i.hasNext(); ) {
                    SelectionKey key = i.next();
                    i.remove();
                    ServerSocketChannel readyChannel = (ServerSocketChannel) key.channel();
                    SocketChannel client_sock_ch;
                    try {
                        client_sock_ch = readyChannel.accept();
                    } catch (IOException e) {
                        if (log.isWarnEnabled()) log.warn("Attempt to accept new connection from listening socket failed", e);
                        continue;
                    }
                    if (log.isTraceEnabled()) log.trace("accepted connection, client_sock=" + client_sock_ch.socket());
                    try {
                        client_sock_ch.socket().setSendBufferSize(send_buf_size);
                    } catch (IllegalArgumentException ex) {
                        if (log.isErrorEnabled()) log.error("exception setting send buffer size to " + send_buf_size + " bytes: ", ex);
                    } catch (SocketException e) {
                        if (log.isErrorEnabled()) log.error("exception setting send buffer size to " + send_buf_size + " bytes: ", e);
                    }
                    try {
                        client_sock_ch.socket().setReceiveBufferSize(recv_buf_size);
                    } catch (IllegalArgumentException ex) {
                        if (log.isErrorEnabled()) log.error("exception setting receive buffer size to " + send_buf_size + " bytes: ", ex);
                    } catch (SocketException e) {
                        if (log.isErrorEnabled()) log.error("exception setting receive buffer size to " + recv_buf_size + " bytes: ", e);
                    }
                    conn = new Connection(client_sock_ch, null);
                    try {
                        Address peer_addr = conn.readPeerAddress(client_sock_ch.socket());
                        conn.peer_addr = peer_addr;
                        synchronized (conns) {
                            Connection tmp = (Connection) conns.get(peer_addr);
                            if (tmp != null) {
                                if (peer_addr.compareTo(local_addr) > 0) {
                                    if (log.isTraceEnabled()) log.trace("peer's address (" + peer_addr + ") is greater than our local address (" + local_addr + "), replacing our existing connection");
                                    addConnection(peer_addr, conn);
                                    tmp.destroy();
                                    notifyConnectionOpened(peer_addr);
                                } else {
                                    if (log.isTraceEnabled()) log.trace("peer's address (" + peer_addr + ") is smaller than our local address (" + local_addr + "), rejecting peer connection request");
                                    conn.destroy();
                                    continue;
                                }
                            } else {
                                addConnection(peer_addr, conn);
                            }
                        }
                        notifyConnectionOpened(peer_addr);
                        client_sock_ch.configureBlocking(false);
                    } catch (IOException e) {
                        if (log.isWarnEnabled()) log.warn("Attempt to configure non-blocking mode failed", e);
                        conn.destroy();
                        continue;
                    } catch (Exception e) {
                        if (log.isWarnEnabled()) log.warn("Attempt to handshake with other peer failed", e);
                        conn.destroy();
                        continue;
                    }
                    int idx;
                    synchronized (m_lockNextWriteHandler) {
                        idx = m_nextWriteHandler = (m_nextWriteHandler + 1) % m_writeHandlers.length;
                    }
                    conn.setupWriteHandler(m_writeHandlers[idx]);
                    try {
                        synchronized (m_lockNextReadHandler) {
                            idx = m_nextReadHandler = (m_nextReadHandler + 1) % m_readHandlers.length;
                        }
                        m_readHandlers[idx].add(conn);
                    } catch (InterruptedException e) {
                        if (log.isWarnEnabled()) log.warn("Attempt to configure read handler for accepted connection failed", e);
                        conn.destroy();
                    }
                }
            }
        }
        if (m_serverSocketChannel.isOpen()) {
            try {
                m_serverSocketChannel.close();
            } catch (Exception e) {
                log.error("exception closing server listening socket", e);
            }
        }
        if (log.isTraceEnabled()) log.trace("acceptor thread terminated");
    }

    /**
    * Finds first available port starting at start_port and returns server socket. Sets srv_port
    */
    protected ServerSocket createServerSocket(int start_port, int end_port) throws Exception {
        this.m_acceptSelector = Selector.open();
        m_serverSocketChannel = ServerSocketChannel.open();
        m_serverSocketChannel.configureBlocking(false);
        while (true) {
            try {
                SocketAddress sockAddr;
                if (bind_addr == null) {
                    sockAddr = new InetSocketAddress(start_port);
                    m_serverSocketChannel.socket().bind(sockAddr);
                } else {
                    sockAddr = new InetSocketAddress(bind_addr, start_port);
                    m_serverSocketChannel.socket().bind(sockAddr, backlog);
                }
            } catch (BindException bind_ex) {
                if (start_port == end_port) throw (BindException) ((new BindException("No available port to bind to (start_port=" + start_port + ")")).initCause(bind_ex));
                start_port++;
                continue;
            } catch (SocketException bind_ex) {
                if (start_port == end_port) throw (BindException) ((new BindException("No available port to bind to  (start_port=" + start_port + ")")).initCause(bind_ex));
                start_port++;
                continue;
            } catch (IOException io_ex) {
                if (log.isErrorEnabled()) log.error("Attempt to bind serversocket failed, port=" + start_port + ", bind addr=" + bind_addr, io_ex);
                throw io_ex;
            }
            srv_port = start_port;
            break;
        }
        m_serverSocketChannel.register(this.m_acceptSelector, SelectionKey.OP_ACCEPT);
        return m_serverSocketChannel.socket();
    }

    protected void runRequest(Address addr, ByteBuffer buf) throws InterruptedException {
        m_requestProcessors.execute(new ExecuteTask(addr, buf));
    }

    private static class Shutdown {
    }

    private static class ReadHandler implements Runnable {

        private final Selector selector = initHandler();

        private final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();

        private final ConnectionTableNIO connectTable;

        private final Log log;

        ReadHandler(ConnectionTableNIO ct, Log log) {
            connectTable = ct;
            this.log = log;
        }

        public Selector initHandler() {
            try {
                return Selector.open();
            } catch (IOException e) {
                if (log.isErrorEnabled()) log.error(e.toString());
                throw new IllegalStateException(e.getMessage());
            }
        }

        /**
       * create instances of ReadHandler threads for receiving data.
       *
       * @param workerThreads is the number of threads to create.
       */
        private static ReadHandler[] create(org.jgroups.util.ThreadFactory f, int workerThreads, ConnectionTableNIO ct, ThreadGroup tg, List<Thread> backGroundThreads, Log log) {
            ReadHandler[] handlers = new ReadHandler[workerThreads];
            for (int looper = 0; looper < workerThreads; looper++) {
                handlers[looper] = new ReadHandler(ct, log);
                Thread thread = f.newThread(tg, handlers[looper], "nioReadHandlerThread");
                thread.setDaemon(true);
                thread.start();
                backGroundThreads.add(thread);
            }
            return handlers;
        }

        private void add(Object conn) throws InterruptedException {
            queue.put(conn);
            wakeup();
        }

        private void wakeup() {
            selector.wakeup();
        }

        public void run() {
            while (true) {
                int events;
                try {
                    events = selector.select();
                } catch (IOException e) {
                    if (log.isWarnEnabled()) log.warn("Select operation on socket failed", e);
                    continue;
                } catch (ClosedSelectorException e) {
                    if (log.isWarnEnabled()) log.warn("Select operation on socket failed", e);
                    return;
                }
                if (events > 0) {
                    Set readyKeys = selector.selectedKeys();
                    try {
                        for (Iterator i = readyKeys.iterator(); i.hasNext(); ) {
                            SelectionKey key = (SelectionKey) i.next();
                            i.remove();
                            Connection conn = (Connection) key.attachment();
                            if (conn != null && conn.getSocketChannel() != null) {
                                try {
                                    if (conn.getSocketChannel().isOpen()) readOnce(conn); else {
                                        conn.closed();
                                    }
                                } catch (IOException e) {
                                    if (log.isTraceEnabled()) log.trace("Read operation on socket failed", e);
                                    key.cancel();
                                    conn.destroy();
                                    conn.closed();
                                }
                            }
                        }
                    } catch (ConcurrentModificationException e) {
                        if (log.isTraceEnabled()) log.trace("Selection set changed", e);
                    }
                }
                Object o;
                try {
                    o = queue.poll(0L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (log.isTraceEnabled()) log.trace("Thread (" + Thread.currentThread().getName() + ") was interrupted while polling queue", e);
                    continue;
                }
                if (null == o) continue;
                if (o instanceof Shutdown) {
                    try {
                        selector.close();
                    } catch (IOException e) {
                        if (log.isTraceEnabled()) log.trace("Read selector close operation failed", e);
                    }
                    return;
                }
                Connection conn = (Connection) o;
                SocketChannel sc = conn.getSocketChannel();
                try {
                    sc.register(selector, SelectionKey.OP_READ, conn);
                } catch (ClosedChannelException e) {
                    if (log.isTraceEnabled()) log.trace("Socket channel was closed while we were trying to register it to selector", e);
                    conn.destroy();
                    conn.closed();
                }
            }
        }

        private void readOnce(Connection conn) throws IOException {
            ConnectionReadState readState = conn.getReadState();
            if (!readState.isHeadFinished()) {
                int size = readHeader(conn);
                if (0 == size) {
                    return;
                }
            }
            if (readBody(conn) > 0) {
                return;
            }
            Address addr = conn.getPeerAddress();
            ByteBuffer buf = readState.getReadBodyBuffer();
            readState.bodyFinished();
            try {
                connectTable.runRequest(addr, buf);
            } catch (InterruptedException e) {
                log.error("Thread (" + Thread.currentThread().getName() + ") was interrupted while assigning executor to process read request", e);
            }
        }

        /**
       * Read message header from channel. It doesn't try to complete. If there is nothing in
       * the channel, the method returns immediately.
       *
       * @param conn The connection
       * @return 0 if header hasn't been read completely, otherwise the size of message body
       * @throws IOException
       */
        private int readHeader(Connection conn) throws IOException {
            ConnectionReadState readState = conn.getReadState();
            ByteBuffer headBuf = readState.getReadHeadBuffer();
            SocketChannel sc = conn.getSocketChannel();
            while (headBuf.remaining() > 0) {
                int num = sc.read(headBuf);
                if (-1 == num) {
                    throw new IOException("Peer closed socket");
                }
                if (0 == num) return 0;
            }
            return readState.headFinished();
        }

        /**
       * Read message body from channel. It doesn't try to complete. If there is nothing in
       * the channel, the method returns immediately.
       *
       * @param conn The connection
       * @return remaining bytes for the message
       * @throws IOException
       */
        private int readBody(Connection conn) throws IOException {
            ByteBuffer bodyBuf = conn.getReadState().getReadBodyBuffer();
            SocketChannel sc = conn.getSocketChannel();
            while (bodyBuf.remaining() > 0) {
                int num = sc.read(bodyBuf);
                if (-1 == num) throw new IOException("Couldn't read from socket as peer closed the socket");
                if (0 == num) return bodyBuf.remaining();
            }
            bodyBuf.flip();
            return 0;
        }
    }

    private class ExecuteTask implements Runnable {

        Address m_addr = null;

        ByteBuffer m_buf = null;

        public ExecuteTask(Address addr, ByteBuffer buf) {
            m_addr = addr;
            m_buf = buf;
        }

        public void run() {
            receive(m_addr, m_buf.array(), m_buf.arrayOffset(), m_buf.limit());
        }
    }

    private class ConnectionReadState {

        private final Connection m_conn;

        private boolean m_headFinished = false;

        private ByteBuffer m_readBodyBuf = null;

        private final ByteBuffer m_readHeadBuf = ByteBuffer.allocate(Connection.HEADER_SIZE);

        public ConnectionReadState(Connection conn) {
            m_conn = conn;
        }

        ByteBuffer getReadBodyBuffer() {
            return m_readBodyBuf;
        }

        ByteBuffer getReadHeadBuffer() {
            return m_readHeadBuf;
        }

        void bodyFinished() {
            m_headFinished = false;
            m_readHeadBuf.clear();
            m_readBodyBuf = null;
            m_conn.updateLastAccessed();
        }

        /**
       * Status change for finishing reading the message header (data already in buffer)
       *
       * @return message size
       */
        int headFinished() {
            m_headFinished = true;
            m_readHeadBuf.flip();
            int messageSize = m_readHeadBuf.getInt();
            m_readBodyBuf = ByteBuffer.allocate(messageSize);
            m_conn.updateLastAccessed();
            return messageSize;
        }

        boolean isHeadFinished() {
            return m_headFinished;
        }
    }

    class Connection extends ConnectionTable.Connection {

        private SocketChannel sock_ch = null;

        private WriteHandler m_writeHandler;

        private SelectorWriteHandler m_selectorWriteHandler;

        private final ConnectionReadState m_readState;

        private static final int HEADER_SIZE = 4;

        final ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);

        Connection(SocketChannel s, Address peer_addr) {
            super(s.socket(), peer_addr);
            sock_ch = s;
            m_readState = new ConnectionReadState(this);
            is_running = true;
        }

        private ConnectionReadState getReadState() {
            return m_readState;
        }

        private void setupWriteHandler(WriteHandler hdlr) {
            m_writeHandler = hdlr;
            m_selectorWriteHandler = hdlr.add(sock_ch);
        }

        void doSend(byte[] buffie, int offset, int length) throws Exception {
            MyFuture result = new MyFuture();
            m_writeHandler.write(sock_ch, ByteBuffer.wrap(buffie, offset, length), result, m_selectorWriteHandler);
            Object ex = result.get();
            if (ex instanceof Exception) {
                if (log.isErrorEnabled()) log.error("failed sending message", (Exception) ex);
                if (((Exception) ex).getCause() instanceof IOException) throw (IOException) ((Exception) ex).getCause();
                throw (Exception) ex;
            }
            result.get();
        }

        SocketChannel getSocketChannel() {
            return sock_ch;
        }

        void closeSocket() {
            if (sock_ch != null) {
                try {
                    if (sock_ch.isConnected() && sock_ch.isOpen()) {
                        sock_ch.close();
                    }
                } catch (Exception e) {
                    log.error("error closing socket connection", e);
                }
                sock_ch = null;
            }
        }

        void closed() {
            Address peerAddr = getPeerAddress();
            synchronized (conns) {
                conns.remove(peerAddr);
            }
            notifyConnectionClosed(peerAddr);
        }
    }

    /**
    * Handle writing to non-blocking NIO connection.
    */
    private static class WriteHandler implements Runnable {

        private final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();

        private final Selector selector = initSelector();

        private int m_pendingChannels;

        private ByteBuffer m_headerBuffer = ByteBuffer.allocate(Connection.HEADER_SIZE);

        private final Log log;

        public WriteHandler(Log log) {
            this.log = log;
        }

        Selector initSelector() {
            try {
                return SelectorProvider.provider().openSelector();
            } catch (IOException e) {
                if (log.isErrorEnabled()) log.error(e.toString());
                throw new IllegalStateException(e.getMessage());
            }
        }

        /**
       * create instances of WriteHandler threads for sending data.
       *
       * @param workerThreads is the number of threads to create.
       */
        private static WriteHandler[] create(org.jgroups.util.ThreadFactory f, int workerThreads, ThreadGroup tg, List<Thread> backGroundThreads, Log log) {
            WriteHandler[] handlers = new WriteHandler[workerThreads];
            for (int looper = 0; looper < workerThreads; looper++) {
                handlers[looper] = new WriteHandler(log);
                Thread thread = f.newThread(tg, handlers[looper], "nioWriteHandlerThread");
                thread.setDaemon(true);
                thread.start();
                backGroundThreads.add(thread);
            }
            return handlers;
        }

        /**
       * Add a new channel to be handled.
       *
       * @param channel
       */
        private SelectorWriteHandler add(SocketChannel channel) {
            return new SelectorWriteHandler(channel, selector, m_headerBuffer);
        }

        /**
       * Writes buffer to the specified socket connection.  This is always performed asynchronously.  If you want
       * to perform a synchrounous write, call notification.`get() which will block until the write operation is complete.
       * Best practice is to call notification.getException() which may return any exceptions that occured during the write
       * operation.
       *
       * @param channel      is where the buffer is written to.
       * @param buffer       is what we write.
       * @param notification may be specified if you want to know how many bytes were written and know if an exception
       *                     occurred.
       */
        private void write(SocketChannel channel, ByteBuffer buffer, MyFuture notification, SelectorWriteHandler hdlr) throws InterruptedException {
            queue.put(new WriteRequest(channel, buffer, notification, hdlr));
        }

        private static void close(SelectorWriteHandler entry) {
            entry.cancel();
        }

        private static void handleChannelError(SelectorWriteHandler entry, Throwable error) {
            do {
                if (error != null) entry.notifyError(error);
            } while (entry.next());
            close(entry);
        }

        private void processWrite(Selector selector) {
            Set keys = selector.selectedKeys();
            Object arr[] = keys.toArray();
            for (Object anArr : arr) {
                SelectionKey key = (SelectionKey) anArr;
                SelectorWriteHandler entry = (SelectorWriteHandler) key.attachment();
                boolean needToDecrementPendingChannels = false;
                try {
                    if (0 == entry.write()) {
                        entry.notifyObject(entry.getBytesWritten());
                        if (!entry.next()) {
                            needToDecrementPendingChannels = true;
                        }
                    }
                } catch (IOException e) {
                    needToDecrementPendingChannels = true;
                    handleChannelError(entry, e);
                } finally {
                    if (needToDecrementPendingChannels) m_pendingChannels--;
                }
            }
            keys.clear();
        }

        public void run() {
            while (selector.isOpen()) {
                try {
                    WriteRequest queueEntry;
                    Object o;
                    while (null != (o = queue.poll(0L, TimeUnit.MILLISECONDS))) {
                        if (o instanceof Shutdown) {
                            try {
                                selector.close();
                            } catch (IOException e) {
                                if (log.isTraceEnabled()) log.trace("Write selector close operation failed", e);
                            }
                            return;
                        }
                        queueEntry = (WriteRequest) o;
                        if (queueEntry.getHandler().add(queueEntry)) {
                            m_pendingChannels++;
                        }
                        try {
                            if (selector.selectNow() > 0) {
                                processWrite(selector);
                            }
                        } catch (IOException e) {
                            if (log.isErrorEnabled()) log.error("SelectNow operation on write selector failed, didn't expect this to occur, please report this", e);
                            return;
                        }
                    }
                    if (m_pendingChannels == 0) {
                        o = queue.take();
                        if (o instanceof Shutdown) {
                            try {
                                selector.close();
                            } catch (IOException e) {
                                if (log.isTraceEnabled()) log.trace("Write selector close operation failed", e);
                            }
                            return;
                        }
                        queueEntry = (WriteRequest) o;
                        if (queueEntry.getHandler().add(queueEntry)) m_pendingChannels++;
                    } else {
                        try {
                            if ((selector.select()) > 0) {
                                processWrite(selector);
                            }
                        } catch (IOException e) {
                            if (log.isErrorEnabled()) log.error("Failure while writing to socket", e);
                        }
                    }
                } catch (InterruptedException e) {
                    if (log.isErrorEnabled()) log.error("Thread (" + Thread.currentThread().getName() + ") was interrupted", e);
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) log.error("Thread (" + Thread.currentThread().getName() + ") caught Throwable", e);
                }
            }
        }
    }

    public static class SelectorWriteHandler {

        private final List<WriteRequest> m_writeRequests = new LinkedList<WriteRequest>();

        private boolean m_headerSent = false;

        private SocketChannel m_channel;

        private SelectionKey m_key;

        private Selector m_selector;

        private int m_bytesWritten = 0;

        private boolean m_enabled = false;

        private ByteBuffer m_headerBuffer;

        SelectorWriteHandler(SocketChannel channel, Selector selector, ByteBuffer headerBuffer) {
            m_channel = channel;
            m_selector = selector;
            m_headerBuffer = headerBuffer;
        }

        private void register(Selector selector, SocketChannel channel) throws ClosedChannelException {
            m_key = channel.register(selector, 0, this);
        }

        private boolean enable() {
            boolean rc = false;
            try {
                if (m_key == null) {
                    register(m_selector, m_channel);
                }
            } catch (ClosedChannelException e) {
                return rc;
            }
            if (!m_enabled) {
                rc = true;
                try {
                    m_key.interestOps(SelectionKey.OP_WRITE);
                } catch (CancelledKeyException e) {
                    return false;
                }
                m_enabled = true;
            }
            return rc;
        }

        private void disable() {
            if (m_enabled) {
                try {
                    m_key.interestOps(0);
                } catch (CancelledKeyException eat) {
                }
                m_enabled = false;
            }
        }

        private void cancel() {
            m_key.cancel();
        }

        boolean add(WriteRequest entry) {
            m_writeRequests.add(entry);
            return enable();
        }

        WriteRequest getCurrentRequest() {
            return m_writeRequests.get(0);
        }

        SocketChannel getChannel() {
            return m_channel;
        }

        ByteBuffer getBuffer() {
            return getCurrentRequest().getBuffer();
        }

        MyFuture getCallback() {
            return getCurrentRequest().getCallback();
        }

        int getBytesWritten() {
            return m_bytesWritten;
        }

        void notifyError(Throwable error) {
            if (getCallback() != null) getCallback().setException(error);
        }

        void notifyObject(Object result) {
            if (getCallback() != null) getCallback().set(result);
        }

        /**
       * switch to next request or disable write interest bit if there are no more buffers.
       *
       * @return true if another request was found to be processed.
       */
        boolean next() {
            m_headerSent = false;
            m_bytesWritten = 0;
            m_writeRequests.remove(0);
            boolean rc = !m_writeRequests.isEmpty();
            if (!rc) disable();
            return rc;
        }

        /**
       * @return bytes remaining to write.  This function will only throw IOException, unchecked exceptions are not
       *         expected to be thrown from here.  It is very important for the caller to know if an unchecked exception can
       *         be thrown in here.  Please correct the following throws list to include any other exceptions and update
       *         caller to handle them.
       * @throws IOException
       */
        int write() throws IOException {
            if (!m_headerSent) {
                m_headerSent = true;
                m_headerBuffer.clear();
                m_headerBuffer.putInt(getBuffer().remaining());
                m_headerBuffer.flip();
                do {
                    getChannel().write(m_headerBuffer);
                } while (m_headerBuffer.remaining() > 0);
            }
            m_bytesWritten += (getChannel().write(getBuffer()));
            return getBuffer().remaining();
        }
    }

    public static class WriteRequest {

        private final SocketChannel m_channel;

        private final ByteBuffer m_buffer;

        private final MyFuture m_callback;

        private final SelectorWriteHandler m_hdlr;

        WriteRequest(SocketChannel channel, ByteBuffer buffer, MyFuture callback, SelectorWriteHandler hdlr) {
            m_channel = channel;
            m_buffer = buffer;
            m_callback = callback;
            m_hdlr = hdlr;
        }

        SelectorWriteHandler getHandler() {
            return m_hdlr;
        }

        SocketChannel getChannel() {
            return m_channel;
        }

        ByteBuffer getBuffer() {
            return m_buffer;
        }

        MyFuture getCallback() {
            return m_callback;
        }
    }

    private static class NullCallable implements Callable {

        public Object call() {
            System.out.println("nullCallable.call invoked");
            return null;
        }
    }

    private static final NullCallable NULLCALL = new NullCallable();

    public static class MyFuture extends FutureTask {

        public MyFuture() {
            super(NULLCALL);
        }

        protected void set(Object o) {
            super.set(o);
        }

        protected void setException(Throwable t) {
            super.setException(t);
        }
    }
}
