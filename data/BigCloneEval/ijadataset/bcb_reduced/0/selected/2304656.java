package sun.security.ssl;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.net.*;
import javax.net.ssl.*;

/**
 * Abstract base class for SSLSocketImpl. Its purpose is to house code with
 * no SSL related logic (or no logic at all). This makes SSLSocketImpl shorter
 * and easier to read. It contains a few constants and static methods plus
 * overridden java.net.Socket methods.
 *
 * Methods are defined final to ensure that they are not accidentally
 * overridden in SSLSocketImpl.
 *
 * @see javax.net.ssl.SSLSocket
 * @see SSLSocketImpl
 *
 */
abstract class BaseSSLSocketImpl extends SSLSocket {

    final Socket self;

    BaseSSLSocketImpl() {
        super();
        this.self = this;
    }

    BaseSSLSocketImpl(Socket socket) {
        super();
        this.self = socket;
    }

    /**
     * TLS requires that a close_notify warning alert is sent before the
     * connection is closed in order to avoid truncation attacks. Some
     * implementations (MS IIS and others) don't do that. The property
     * below controls whether we accept that or treat it as an error.
     *
     * The default is "false", i.e. tolerate the broken behavior.
     */
    private static final String PROP_NAME = "com.sun.net.ssl.requireCloseNotify";

    static final boolean requireCloseNotify = Debug.getBooleanProperty(PROP_NAME, false);

    /**
     * Returns the unique {@link java.nio.SocketChannel SocketChannel} object
     * associated with this socket, if any.
     * @see java.net.Socket#getChannel
     */
    public final SocketChannel getChannel() {
        if (self == this) {
            return super.getChannel();
        } else {
            return self.getChannel();
        }
    }

    /**
     * Binds the address to the socket.
     * @see java.net.Socket#bind
     */
    public void bind(SocketAddress bindpoint) throws IOException {
        if (self == this) {
            super.bind(bindpoint);
        } else {
            throw new IOException("Underlying socket should already be connected");
        }
    }

    /**
     * Returns the address of the endpoint this socket is connected to
     * @see java.net.Socket#getLocalSocketAddress
     */
    public SocketAddress getLocalSocketAddress() {
        if (self == this) {
            return super.getLocalSocketAddress();
        } else {
            return self.getLocalSocketAddress();
        }
    }

    /**
     * Returns the address of the endpoint this socket is connected to
     * @see java.net.Socket#getRemoteSocketAddress
     */
    public SocketAddress getRemoteSocketAddress() {
        if (self == this) {
            return super.getRemoteSocketAddress();
        } else {
            return self.getRemoteSocketAddress();
        }
    }

    /**
     * Connects this socket to the server.
     *
     * This method is either called on an unconnected SSLSocketImpl by the
     * application, or it is called in the constructor of a regular
     * SSLSocketImpl. If we are layering on top on another socket, then
     * this method should not be called, because we assume that the
     * underlying socket is already connected by the time it is passed to
     * us.
     *
     * @param   endpoint the <code>SocketAddress</code>
     * @throws  IOException if an error occurs during the connection
     */
    public final void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    /**
     * Returns the connection state of the socket.
     * @see java.net.Socket#isConnected
     */
    public final boolean isConnected() {
        if (self == this) {
            return super.isConnected();
        } else {
            return self.isConnected();
        }
    }

    /**
     * Returns the binding state of the socket.
     * @see java.net.Socket#isBound
     */
    public final boolean isBound() {
        if (self == this) {
            return super.isBound();
        } else {
            return self.isBound();
        }
    }

    /**
     * The semantics of shutdownInput is not supported in TLS 1.0
     * spec. Thus when the method is called on an SSL socket, an
     * UnsupportedOperationException will be thrown.
     *
     * @throws UnsupportedOperationException
     */
    public final void shutdownInput() throws IOException {
        throw new UnsupportedOperationException("The method shutdownInput()" + " is not supported in SSLSocket");
    }

    /**
     * The semantics of shutdownOutput is not supported in TLS 1.0
     * spec. Thus when the method is called on an SSL socket, an
     * UnsupportedOperationException will be thrown.
     *
     * @throws UnsupportedOperationException
     */
    public final void shutdownOutput() throws IOException {
        throw new UnsupportedOperationException("The method shutdownOutput()" + " is not supported in SSLSocket");
    }

    /**
     * Returns the input state of the socket
     * @see java.net.Socket#isInputShutdown
     */
    public final boolean isInputShutdown() {
        if (self == this) {
            return super.isInputShutdown();
        } else {
            return self.isInputShutdown();
        }
    }

    /**
     * Returns the output state of the socket
     * @see java.net.Socket#isOutputShutdown
     */
    public final boolean isOutputShutdown() {
        if (self == this) {
            return super.isOutputShutdown();
        } else {
            return self.isOutputShutdown();
        }
    }

    /**
     * Ensures that the SSL connection is closed down as cleanly
     * as possible, in case the application forgets to do so.
     * This allows SSL connections to be implicitly reclaimed,
     * rather than forcing them to be explicitly reclaimed at
     * the penalty of prematurly killing SSL sessions.
     */
    protected final void finalize() throws Throwable {
        try {
            close();
        } catch (IOException e1) {
            try {
                if (self == this) {
                    super.close();
                }
            } catch (IOException e2) {
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Returns the address of the remote peer for this connection.
     */
    public final InetAddress getInetAddress() {
        if (self == this) {
            return super.getInetAddress();
        } else {
            return self.getInetAddress();
        }
    }

    /**
     * Gets the local address to which the socket is bound.
     *
     * @return the local address to which the socket is bound.
     * @since   JDK1.1
     */
    public final InetAddress getLocalAddress() {
        if (self == this) {
            return super.getLocalAddress();
        } else {
            return self.getLocalAddress();
        }
    }

    /**
     * Returns the number of the remote port that this connection uses.
     */
    public final int getPort() {
        if (self == this) {
            return super.getPort();
        } else {
            return self.getPort();
        }
    }

    /**
     * Returns the number of the local port that this connection uses.
     */
    public final int getLocalPort() {
        if (self == this) {
            return super.getLocalPort();
        } else {
            return self.getLocalPort();
        }
    }

    /**
     * Enables or disables the Nagle optimization.
     * @see java.net.Socket#setTcpNoDelay
     */
    public final void setTcpNoDelay(boolean value) throws SocketException {
        if (self == this) {
            super.setTcpNoDelay(value);
        } else {
            self.setTcpNoDelay(value);
        }
    }

    /**
     * Returns true if the Nagle optimization is disabled.  This
     * relates to low-level buffering of TCP traffic, delaying the
     * traffic to promote better throughput.
     *
     * @see java.net.Socket#getTcpNoDelay
     */
    public final boolean getTcpNoDelay() throws SocketException {
        if (self == this) {
            return super.getTcpNoDelay();
        } else {
            return self.getTcpNoDelay();
        }
    }

    /**
     * Assigns the socket's linger timeout.
     * @see java.net.Socket#setSoLinger
     */
    public final void setSoLinger(boolean flag, int linger) throws SocketException {
        if (self == this) {
            super.setSoLinger(flag, linger);
        } else {
            self.setSoLinger(flag, linger);
        }
    }

    /**
     * Returns the socket's linger timeout.
     * @see java.net.Socket#getSoLinger
     */
    public final int getSoLinger() throws SocketException {
        if (self == this) {
            return super.getSoLinger();
        } else {
            return self.getSoLinger();
        }
    }

    /**
     * Send one byte of urgent data on the socket.
     * @see java.net.Socket#sendUrgentData
     * At this point, there seems to be no specific requirement to support
     * this for an SSLSocket. An implementation can be provided if a need
     * arises in future.
     */
    public final void sendUrgentData(int data) throws SocketException {
        throw new SocketException("This method is not supported " + "by SSLSockets");
    }

    /**
     * Enable/disable OOBINLINE (receipt of TCP urgent data) By default, this
     * option is disabled and TCP urgent data received on a socket is silently
     * discarded.
     * @see java.net.Socket#setOOBInline
     * Setting OOBInline does not have any effect on SSLSocket,
     * since currently we don't support sending urgent data.
     */
    public final void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("This method is ineffective, since" + " sending urgent data is not supported by SSLSockets");
    }

    /**
     * Tests if OOBINLINE is enabled.
     * @see java.net.Socket#getOOBInline
     */
    public final boolean getOOBInline() throws SocketException {
        throw new SocketException("This method is ineffective, since" + " sending urgent data is not supported by SSLSockets");
    }

    /**
     * Returns the socket timeout.
     * @see java.net.Socket#getSoTimeout
     */
    public final int getSoTimeout() throws SocketException {
        if (self == this) {
            return super.getSoTimeout();
        } else {
            return self.getSoTimeout();
        }
    }

    public final void setSendBufferSize(int size) throws SocketException {
        if (self == this) {
            super.setSendBufferSize(size);
        } else {
            self.setSendBufferSize(size);
        }
    }

    public final int getSendBufferSize() throws SocketException {
        if (self == this) {
            return super.getSendBufferSize();
        } else {
            return self.getSendBufferSize();
        }
    }

    public final void setReceiveBufferSize(int size) throws SocketException {
        if (self == this) {
            super.setReceiveBufferSize(size);
        } else {
            self.setReceiveBufferSize(size);
        }
    }

    public final int getReceiveBufferSize() throws SocketException {
        if (self == this) {
            return super.getReceiveBufferSize();
        } else {
            return self.getReceiveBufferSize();
        }
    }

    /**
     * Enable/disable SO_KEEPALIVE.
     * @see java.net.Socket#setKeepAlive
     */
    public final void setKeepAlive(boolean on) throws SocketException {
        if (self == this) {
            super.setKeepAlive(on);
        } else {
            self.setKeepAlive(on);
        }
    }

    /**
     * Tests if SO_KEEPALIVE is enabled.
     * @see java.net.Socket#getKeepAlive
     */
    public final boolean getKeepAlive() throws SocketException {
        if (self == this) {
            return super.getKeepAlive();
        } else {
            return self.getKeepAlive();
        }
    }

    /**
     * Sets traffic class or type-of-service octet in the IP header for
     * packets sent from this Socket.
     * @see java.net.Socket#setTrafficClass
     */
    public final void setTrafficClass(int tc) throws SocketException {
        if (self == this) {
            super.setTrafficClass(tc);
        } else {
            self.setTrafficClass(tc);
        }
    }

    /**
     * Gets traffic class or type-of-service in the IP header for packets
     * sent from this Socket.
     * @see java.net.Socket#getTrafficClass
     */
    public final int getTrafficClass() throws SocketException {
        if (self == this) {
            return super.getTrafficClass();
        } else {
            return self.getTrafficClass();
        }
    }

    /**
     * Enable/disable SO_REUSEADDR.
     * @see java.net.Socket#setReuseAddress
     */
    public final void setReuseAddress(boolean on) throws SocketException {
        if (self == this) {
            super.setReuseAddress(on);
        } else {
            self.setReuseAddress(on);
        }
    }

    /**
     * Tests if SO_REUSEADDR is enabled.
     * @see java.net.Socket#getReuseAddress
     */
    public final boolean getReuseAddress() throws SocketException {
        if (self == this) {
            return super.getReuseAddress();
        } else {
            return self.getReuseAddress();
        }
    }

    /**
     * Sets performance preferences for this socket.
     *
     * @see java.net.Socket#setPerformancePreferences(int, int, int)
     */
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        if (self == this) {
            super.setPerformancePreferences(connectionTime, latency, bandwidth);
        } else {
            self.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
    }
}
