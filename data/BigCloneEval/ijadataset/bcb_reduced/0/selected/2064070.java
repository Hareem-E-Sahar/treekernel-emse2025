package org.xsocket.datagram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * connected endpoint implementation
 *
 * @author grro@xsocket.org
 */
public final class ConnectedEndpoint extends AbstractChannelBasedEndpoint implements IConnectedEndpoint {

    private static final Logger LOG = Logger.getLogger(ConnectedEndpoint.class.getName());

    private final SocketAddress remoteAddress;

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to any
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @param host    the remote host
     * @param port    the remote port
     * @throws IOException If some I/O error occurs
	 */
    public ConnectedEndpoint(String host, int port) throws IOException {
        this(new InetSocketAddress(host, port));
    }

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to any
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @throws IOException If some I/O error occurs
	 */
    public ConnectedEndpoint(SocketAddress remoteAddress) throws IOException {
        this(remoteAddress, -1);
    }

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to the given
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @param remoteAddress            the remote socket address
 	 * @param receivePacketSize        the receive packet size
     * @throws IOException If some I/O error occurs
   	 */
    public ConnectedEndpoint(SocketAddress remoteAddress, int receivePacketSize) throws IOException {
        this(remoteAddress, receivePacketSize, null);
    }

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to the given
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @param host                 the remote host
     * @param port                 the remote port
 	 * @param receivePacketSize    the receive packet size
     * @throws IOException If some I/O error occurs
   	 */
    public ConnectedEndpoint(String host, int port, int receivePacketSize) throws IOException {
        this(new InetSocketAddress(host, port), new HashMap<String, Object>(), receivePacketSize, null, getGlobalWorkerPool());
    }

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to the given
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @param remoteAddress            the remote socket address
 	 * @param receivePacketSize        the receive packet size
     * @param datagramHandler          the datagram handler
     * @throws IOException If some I/O error occurs
   	 */
    public ConnectedEndpoint(SocketAddress remoteAddress, int receivePacketSize, IDatagramHandler datagramHandler) throws IOException {
        this(remoteAddress, new HashMap<String, Object>(), receivePacketSize, datagramHandler, getGlobalWorkerPool());
    }

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to the given
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @param remoteAddress            the remote socket address
	 * @param socketOptions            the socket options
 	 * @param receivePacketSize        the receive packet size
     * @param datagramHandler          the datagram handler
     * @throws IOException If some I/O error occurs
   	 */
    public ConnectedEndpoint(SocketAddress remoteAddress, Map<String, Object> options, int receivePacketSize, IDatagramHandler datagramHandler) throws IOException {
        this(remoteAddress, options, receivePacketSize, datagramHandler, getGlobalWorkerPool());
    }

    /**
  	 * Constructs a <i>client/server</i> datagram socket and binds it to the given
	 * available port on the local host machine. The socket
	 * will be bound to the wildcard address, an IP address
	 * chosen by the kernel. The local socket will be connected
	 * to the server by using the passed over addresses
	 *
     * @param remoteAddress            the remote socket address
	 * @param socketOptions            the socket options
 	 * @param receivePacketSize        the receive packet size
     * @param datagramHandler          the datagram handler
     * @param workerPool               the worker pool
     * @throws IOException If some I/O error occurs
   	 */
    public ConnectedEndpoint(SocketAddress remoteAddress, Map<String, Object> options, int receivePacketSize, IDatagramHandler datagramHandler, Executor workerPool) throws IOException {
        super(new InetSocketAddress(0), options, datagramHandler, receivePacketSize, workerPool);
        this.remoteAddress = remoteAddress;
        getChannel().connect(remoteAddress);
    }

    @Override
    public void send(UserDatagram packet) throws IOException {
        if (LOG.isLoggable(Level.FINER) && (packet.getRemoteSocketAddress() != null)) {
            LOG.fine("remote address of given packet is already set with " + packet.getRemoteSocketAddress() + ". this value will be overriden by " + remoteAddress);
        }
        packet.setRemoteAddress(remoteAddress);
        super.send(packet);
    }

    /**
	 * {@inheritDoc}
	 */
    public SocketAddress getRemoteSocketAddress() {
        return remoteAddress;
    }

    /**
	 * {@inheritDoc}
	 */
    protected ConnectedEndpoint setOption(String name, Object value) throws IOException {
        return (ConnectedEndpoint) super.setOption(name, value);
    }
}
