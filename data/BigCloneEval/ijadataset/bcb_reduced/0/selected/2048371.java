package com.ibm.JikesRVM;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

/**
 * Sockets using Jikes RVM non-blocking I/O support
 *
 * @author Julian Dolby
 */
final class JikesRVMSocketImpl extends SocketImpl implements VM_SizeConstants {

    /**
     * Creates a new unconnected socket. If streaming is true,
     * create a stream socket, else a datagram socket.
     * The deprecated datagram usage is not supported and will throw 
     * an exception.
     *
     * @param		streaming	true, if the socket is type streaming
     * @exception	SocketException	if error while creating the socket
     */
    protected synchronized void create(boolean streaming) throws SocketException {
        this.streaming = streaming;
        if (streaming) {
            VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
            int ifd = VM_SysCall.call1(bootRecord.sysNetSocketCreateIP, 1);
            if (ifd < 0) throw new SocketException(); else {
                VM_FileSystem.onCreateFileDescriptor(ifd, false);
                native_fd = ifd;
            }
        } else {
            throw new VM_UnimplementedError("non-streaming sockets");
        }
    }

    /**
     * Connects to the remote hostname and port specified as arguments.
     *
     * @param hostname The remote hostname to connect to
     * @param port The remote port to connect to
     *
     * @exception IOException If an error occurs
     */
    protected synchronized void connect(String remoteHost, int remotePort) throws IOException {
        if (VM.VerifyAssertions) VM._assert(streaming);
        InetAddress remoteAddr = InetAddress.getByName(remoteHost);
        connectInternal(remoteAddr, remotePort, 0);
    }

    /**
     * Connects this socket to the specified remote host address/port.
     *
     * @author		OTI
     * @version		initial
     *
     * @param		address		the remote host address to connect to
     * @param		port		the remote port to connect to
     * @exception	IOException	if an error occurs while connecting
     */
    protected synchronized void connect(InetAddress remoteAddr, int remotePort) throws IOException {
        if (VM.VerifyAssertions) VM._assert(streaming);
        connectInternal(remoteAddr, remotePort, 0);
    }

    public synchronized void connect(SocketAddress iaddress, int timeout) throws IOException {
        InetSocketAddress address = (InetSocketAddress) iaddress;
        InetAddress remoteAddress = address.getAddress();
        int remotePort = address.getPort();
        connectInternal(remoteAddress, remotePort, timeout);
    }

    protected synchronized void bind(InetAddress localAddr, int localPort) throws IOException {
        byte[] ip = localAddr.getAddress();
        int family = java.net.JikesRVMSupport.getFamily(localAddr);
        int address;
        address = ip[3] & 0xff;
        address |= ((ip[2] << BITS_IN_BYTE) & 0xff00);
        address |= ((ip[1] << (2 * BITS_IN_BYTE)) & 0xff0000);
        address |= ((ip[0] << (3 * BITS_IN_BYTE)) & 0xff000000);
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        int rc = VM_SysCall.call4(bootRecord.sysNetSocketBindIP, native_fd, family, address, localPort);
        if (rc != 0) throw new IOException();
        this.localAddress = localAddr;
    }

    /**
     * Listen for connection requests on this stream socket.
     * Incoming connection requests are queued, up to the limit
     * nominated by backlog.  Additional requests are rejected.
     * listen() may only be invoked on stream sockets.
     *
     * @param		backlog		the max number of outstanding connection requests
     * @exception	IOException	thrown if an error occurs while listening
     *
     */
    protected synchronized void listen(int backlog) throws java.io.IOException {
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        int rc = VM_SysCall.call2(bootRecord.sysNetSocketListenIP, native_fd, backlog);
        if (rc == -1) throw new SocketException();
    }

    /**
     * Accepts a connection on the provided socket, by calling the IP stack.
     *
     * @param		newImpl	the socket to accept connections on
     * @exception	SocketException	if an error occurs while accepting
     */
    protected synchronized void accept(SocketImpl newImpl) throws IOException {
        JikesRVMSocketImpl newSocket = (JikesRVMSocketImpl) newImpl;
        newSocket.address = java.net.JikesRVMSupport.createInetAddress(0);
        boolean hasTimeout = (receiveTimeout > 0);
        double totalWaitTime = hasTimeout ? ((double) receiveTimeout) / 1000.0 : VM_ThreadEventConstants.WAIT_INFINITE;
        int connectionFd;
        double waitStartTime = hasTimeout ? VM_Time.now() : 0.0;
        while (true) {
            VM_ThreadIOQueue.selectInProgressMutex.lock();
            VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
            connectionFd = VM_SysCall.call_I_I_A(bootRecord.sysNetSocketAcceptIP, native_fd, VM_Magic.objectAsAddress(newSocket));
            VM_ThreadIOQueue.selectInProgressMutex.unlock();
            if (connectionFd >= 0) break;
            switch(connectionFd) {
                case -1:
                    continue;
                case -2:
                    {
                        if (VM.VerifyAssertions) VM._assert(!hasTimeout || totalWaitTime >= 0.0);
                        VM_ThreadIOWaitData waitData = VM_Wait.ioWaitRead(native_fd, totalWaitTime);
                        checkIoWaitRead(waitData);
                        if (hasTimeout) {
                            double nextWaitStartTime = VM_Time.now();
                            totalWaitTime -= (nextWaitStartTime - waitStartTime);
                            if (totalWaitTime < 0.0) throw new SocketTimeoutException("socket operation timed out");
                            waitStartTime = nextWaitStartTime;
                        }
                        continue;
                    }
                default:
                    throw new SocketException("accept failed");
            }
        }
        java.net.JikesRVMSupport.setHostName(newSocket.getInetAddress(), null);
        VM_FileSystem.onCreateFileDescriptor(connectionFd, false);
        newSocket.native_fd = connectionFd;
    }

    /**
     * Answer the socket input stream.
     *
     * @return		InputStream	an InputStream on the socket
     * @exception	IOException	thrown if an error occurs while accessing the stream
     */
    protected synchronized InputStream getInputStream() throws IOException {
        return new InputStream() {

            private boolean closed = false;

            public int available() throws IOException {
                if (closed) throw new IOException("stream closed");
                return JikesRVMSocketImpl.this.available();
            }

            public void close() throws IOException {
                closed = true;
            }

            public int read() throws IOException {
                if (closed) throw new IOException("stream closed");
                byte[] buffer = new byte[1];
                int result = JikesRVMSocketImpl.this.read(buffer, 0, 1);
                return (-1 == result) ? result : ((int) buffer[0]) & 0xFF;
            }

            public int read(byte[] buffer) throws IOException {
                if (closed) throw new IOException("stream closed");
                return JikesRVMSocketImpl.this.read(buffer, 0, buffer.length);
            }

            public int read(byte[] buf, int off, int len) throws IOException {
                if (closed) throw new IOException("stream closed");
                return JikesRVMSocketImpl.this.read(buf, off, len);
            }
        };
    }

    /**
     * Answer the socket output stream.
     *
     * @return		OutputStream	an OutputStream on the socket
     * @exception	IOException	thrown if an error occurs while accessing the stream
     */
    protected synchronized OutputStream getOutputStream() throws IOException {
        return new OutputStream() {

            private boolean closed = false;

            public void write(int b) throws IOException {
                if (closed) throw new IOException("stream closed");
                byte[] buffer = new byte[] { (byte) b };
                JikesRVMSocketImpl.this.write(buffer, 0, 1);
            }

            public void write(byte[] b) throws IOException {
                if (closed) throw new IOException("stream closed");
                JikesRVMSocketImpl.this.write(b, 0, b.length);
            }

            public void write(byte[] b, int off, int len) throws IOException {
                if (closed) throw new IOException("stream closed");
                JikesRVMSocketImpl.this.write(b, off, len);
            }

            public void flush() throws IOException {
                if (closed) throw new IOException("stream closed");
                VM_FileSystem.sync(native_fd);
            }

            public void close() throws IOException {
                closed = true;
            }
        };
    }

    /**
     * Answer the number of bytes that may be read from this
     * socket without blocking.  This call does not block.
     *
     * @return		int		the number of bytes that may be read without blocking
     * @exception	SocketException	if an error occurs while peeking
     */
    protected synchronized int available() throws IOException {
        return VM_FileSystem.bytesAvailable(native_fd);
    }

    /**
     * Close the socket.  Usage thereafter is invalid.
     *
     * @exception	IOException	if an error occurs while closing
     */
    protected synchronized void close() throws IOException {
        if (native_fd != -1) {
            int close_fd = native_fd;
            this.native_fd = -1;
            VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
            int rc = VM_SysCall.call1(bootRecord.sysNetSocketCloseIP, close_fd);
        }
    }

    /**
     * Close the input side of the socket.
     * The output side of the socket is unaffected.
     */
    protected synchronized void shutdownInput() throws IOException {
        if (native_fd == -1) throw new IOException("socket already closed");
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        if (VM_SysCall.call2(bootRecord.sysNetSocketShutdownIP, native_fd, CLOSE_INPUT) != 0) throw new IOException("could not close input side of socket");
    }

    /**
     * Close the output side of the socket.
     * The input side of the socket is unaffected.
     */
    protected synchronized void shutdownOutput() throws IOException {
        if (native_fd == -1) throw new IOException("socket already closed");
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        if (VM_SysCall.call2(bootRecord.sysNetSocketShutdownIP, native_fd, CLOSE_OUTPUT) != 0) throw new IOException("could not close input side of socket");
    }

    protected FileDescriptor getFileDescriptor() {
        throw new InternalError("no FDs for sockets");
    }

    protected boolean supportsUrgentData() {
        return false;
    }

    public void sendUrgentData(int data) {
        throw new VM_UnimplementedError("JikesRVMSocketImpl.sendUrgentData");
    }

    /**
     * Return the local port used by this socket impl.
     *
     */
    public synchronized int getLocalPort() {
        getLocalPortInternal();
        return localport;
    }

    /**
     * Set the nominated socket option.  Receive timeouts are maintained
     * in Java, rather than in the JNI code.
     *
     * @param		optID		the socket option to set
     * @param		val			the option value
     * @exception	SocketException	thrown if an error occurs while setting the option
     */
    public synchronized void setOption(int optID, Object val) throws SocketException {
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        switch(optID) {
            case SocketOptions.SO_LINGER:
                {
                    if (val instanceof Integer) {
                        int rc = VM_SysCall.call3(bootRecord.sysNetSocketLingerIP, native_fd, 1, ((Integer) val).intValue());
                        if (rc == -1) throw new SocketException("SO_LINGER");
                    } else {
                        int rc = VM_SysCall.call3(bootRecord.sysNetSocketLingerIP, native_fd, 0, 0);
                        if (rc == -1) throw new SocketException("SO_LINGER");
                    }
                }
                break;
            case SocketOptions.SO_KEEPALIVE:
                {
                }
                break;
            case SocketOptions.TCP_NODELAY:
                {
                    int rc = VM_SysCall.call2(bootRecord.sysNetSocketNoDelayIP, native_fd, ((Boolean) val).booleanValue() ? 1 : 0);
                    if (rc == -1) throw new SocketException("setTcpNoDelay");
                }
                break;
            case SocketOptions.SO_TIMEOUT:
                {
                    receiveTimeout = ((Integer) val).intValue();
                }
                break;
            default:
                VM._assert(VM.NOT_REACHED);
        }
    }

    /**
     * Answer the nominated socket option.  Receive timeouts are maintained
     * in Java, rather than in the JNI code.
     *
     * @param		optID		the socket option to retrieve
     * @return		Object		the option value
     * @exception	SocketException	thrown if an error occurs while accessing the option
     */
    public synchronized Object getOption(int optID) throws SocketException {
        if (optID == SocketOptions.SO_TIMEOUT) return new Integer(receiveTimeout); else throw new VM_UnimplementedError("JikesRVMSocketImpl.getOption");
    }

    private static final int CLOSE_INPUT = 0;

    private static final int CLOSE_OUTPUT = 1;

    private InetAddress localAddress = null;

    private boolean streaming = true;

    private int receiveTimeout = -1;

    private int native_fd = -1;

    /**
     * Initialize socket impl with invalid values; will really
     * be initialized bv .create, .bind, etc.
     */
    JikesRVMSocketImpl() {
        address = null;
        port = -1;
        localport = -1;
    }

    /**
     * Utility method to check the result of an ioWaitRead()
     * for possible exceptions.
     */
    private static void checkIoWaitRead(VM_ThreadIOWaitData waitData) throws SocketException, SocketTimeoutException {
        if (waitData.timedOut()) throw new SocketTimeoutException("socket operation timed out");
        if ((waitData.readFds[0] & VM_ThreadIOConstants.FD_INVALID_BIT) != 0) throw new SocketException("invalid socket file descriptor");
    }

    /**
     * Utility method to check the result of an ioWaitWrite()
     * for possible exceptions.
     */
    private static void checkIoWaitWrite(VM_ThreadIOWaitData waitData) throws SocketException, SocketTimeoutException {
        if (waitData.timedOut()) throw new SocketTimeoutException("socket operation timed out");
        if ((waitData.writeFds[0] & VM_ThreadIOConstants.FD_INVALID_BIT) != 0) throw new SocketException("invalid socket file descriptor");
    }

    /**
     * We do not store the local port by default, so this method makes sure
     * that we have it before we try to return it.
     */
    private int getLocalPortInternal() {
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        localport = VM_SysCall.call1(bootRecord.sysNetSocketPortIP, native_fd);
        return localport;
    }

    /**
     * Connects this socket to the specified remote host/port.
     * This method assumes the sender has verified the host with
     * the security policy.
     *
     * @param		host		the remote host to connect to
     * @param		port		the remote port to connect to
     * @param           timeout         a timeout in milliseconds
     * @exception	IOException	if an error occurs while connecting
     */
    private void connectInternal(InetAddress remoteAddr, int remotePort, int timeout) throws IOException {
        VM_BootRecord bootRecord = VM_BootRecord.the_boot_record;
        int rc = -1;
        double totalWaitTime = (timeout > 0) ? ((double) timeout) / 1000.0 : VM_ThreadEventConstants.WAIT_INFINITE;
        byte[] ip = remoteAddr.getAddress();
        int family = java.net.JikesRVMSupport.getFamily(remoteAddr);
        int address;
        address = ip[3] & 0xff;
        address |= ((ip[2] << BITS_IN_BYTE) & 0xff00);
        address |= ((ip[1] << (2 * BITS_IN_BYTE)) & 0xff0000);
        address |= ((ip[0] << (3 * BITS_IN_BYTE)) & 0xff000000);
        while (rc < 0) {
            VM_ThreadIOQueue.selectInProgressMutex.lock();
            rc = VM_SysCall.call4(bootRecord.sysNetSocketConnectIP, native_fd, family, address, remotePort);
            VM_ThreadIOQueue.selectInProgressMutex.unlock();
            switch(rc) {
                case 0:
                    this.address = remoteAddr;
                    this.port = remotePort;
                    break;
                case -1:
                    Thread.currentThread().yield();
                    break;
                case -2:
                    VM_ThreadIOWaitData waitData = VM_Wait.ioWaitWrite(native_fd, totalWaitTime);
                    checkIoWaitWrite(waitData);
                    break;
                case -4:
                    throw new ConnectException("Connection refused");
                case -5:
                    throw new NoRouteToHostException();
                case -3:
                default:
                    throw new IOException("rc=" + rc);
            }
        }
    }

    /**
     * In the IP stack, read at most <code>count</code> bytes off the socket into the <code>buffer</code>,
     * at the <code>offset</code>.  If the timeout is zero, block indefinitely waiting
     * for data, otherwise wait the specified period (in milliseconds).
     *
     * @param		buffer		the buffer to read into
     * @param		offset		the offset into the buffer
     * @param		count		the max number of bytes to read
     * @return		int			the actual number of bytes read
     * @exception	IOException	thrown if an error occurs while reading
     */
    synchronized int read(byte[] buffer, int offset, int count) throws IOException {
        if (count == 0) return 0;
        double totalWaitTime = (receiveTimeout > 0) ? ((double) receiveTimeout) / 1000.0 : VM_ThreadEventConstants.WAIT_INFINITE;
        int rc;
        try {
            rc = VM_FileSystem.readBytes(native_fd, buffer, offset, count, totalWaitTime);
        } catch (VM_TimeoutException e) {
            throw new SocketTimeoutException("socket receive timed out");
        }
        if (rc == 0) return -1; else return rc;
    }

    synchronized int write(byte[] buffer, int offset, int count) throws IOException {
        if (count == 0) return 0;
        int rc = VM_FileSystem.writeBytes(native_fd, buffer, offset, count);
        return rc;
    }

    protected void finalize() throws IOException {
        close();
    }
}
