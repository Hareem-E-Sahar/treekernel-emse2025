package com.limegroup.gnutella;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.limegroup.gnutella.guess.GUESSEndpoint;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.PingReply;
import com.limegroup.gnutella.messages.PingRequest;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessage;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.IpPort;
import com.limegroup.gnutella.util.NetworkUtils;
import com.limegroup.gnutella.io.ReadHandler;
import com.limegroup.gnutella.io.WriteHandler;
import com.limegroup.gnutella.io.NIODispatcher;

/**
 * This class handles UDP messaging services.  It both sends and
 * receives messages, routing received messages to their appropriate
 * handlers.  This also handles issues related to the GUESS proposal, 
 * such as making sure that the UDP and TCP port match and sending
 * UDP acks for queries.
 *
 * @see UDPReplyHandler
 * @see MessageRouter
 * @see QueryUnicaster
 *
 */
public class UDPService implements ReadHandler, WriteHandler {

    private static final Log LOG = LogFactory.getLog(UDPService.class);

    /**
	 * Constant for the single <tt>UDPService</tt> instance.
	 */
    private static final UDPService INSTANCE = new UDPService();

    /**
	 * The DatagramChannel we're reading from & writing to.
	 */
    private DatagramChannel _channel;

    /**
	 * The list of messages to be sent, as SendBundles.
	 */
    private final List OUTGOING_MSGS;

    /**
	 * The buffer that's re-used for reading incoming messages.
	 */
    private final ByteBuffer BUFFER;

    /**
	 * The maximum size of a UDP message we'll accept.
	 */
    private final int BUFFER_SIZE = 1024 * 2;

    /** True if the UDPService has ever received a solicited incoming UDP
     *  packet.
     */
    private boolean _acceptedSolicitedIncoming = false;

    /** True if the UDPService has ever received a unsolicited incoming UDP
     *  packet.
     */
    private boolean _acceptedUnsolicitedIncoming = false;

    /** The last time the _acceptedUnsolicitedIncoming was set.
     */
    private long _lastUnsolicitedIncomingTime = 0;

    /** The last time we sent a UDP Connect Back.
     */
    private long _lastConnectBackTime = System.currentTimeMillis();

    void resetLastConnectBackTime() {
        _lastConnectBackTime = System.currentTimeMillis() - Acceptor.INCOMING_EXPIRE_TIME;
    }

    /** Whether our NAT assigns stable ports for successive connections 
     * LOCKING: this
     */
    private boolean _portStable = true;

    /** The last reported port as seen from the outside
     *  LOCKING: this
     */
    private int _lastReportedPort;

    /**
     * The number of pongs carrying IP:Port info we have received.
     * LOCKING: this
     */
    private int _numReceivedIPPongs;

    /**
     * The GUID that we advertise out for UDPConnectBack requests.
     */
    private final GUID CONNECT_BACK_GUID = new GUID(GUID.makeGuid());

    /**
     * The GUID that we send for Pings, useful to test solicited support.
     */
    private final GUID SOLICITED_PING_GUID = new GUID(GUID.makeGuid());

    /**
     * The time between UDP pings.  Used by the PeriodicPinger.  This is
     * useful for nodes behind certain firewalls (notably the MS firewall).
     */
    private static final long PING_PERIOD = 85 * 1000;

    /**
     * A buffer used for reading the header of incoming messages.
     */
    private static final byte[] IN_HEADER_BUF = new byte[23];

    /**
     * A buffer used for reading the header of outgoing messages.
     */
    private static final byte[] OUT_HEADER_BUF = new byte[23];

    /**
	 * Instance accessor.
	 */
    public static UDPService instance() {
        return INSTANCE;
    }

    /**
	 * Constructs a new <tt>UDPAcceptor</tt>.
	 */
    protected UDPService() {
        OUTGOING_MSGS = new LinkedList();
        byte[] backing = new byte[BUFFER_SIZE];
        BUFFER = ByteBuffer.wrap(backing);
        scheduleServices();
    }

    /**
     * Schedules IncomingValidator & PeriodicPinger for periodic use.
     */
    protected void scheduleServices() {
        RouterService.schedule(new IncomingValidator(), Acceptor.TIME_BETWEEN_VALIDATES, Acceptor.TIME_BETWEEN_VALIDATES);
        RouterService.schedule(new PeriodicPinger(), 0, PING_PERIOD);
    }

    /** @return The GUID to send for UDPConnectBack attempts....
     */
    public GUID getConnectBackGUID() {
        return CONNECT_BACK_GUID;
    }

    /** @return The GUID to send for Solicited Ping attempts....
     */
    public GUID getSolicitedGUID() {
        return SOLICITED_PING_GUID;
    }

    /** 
     * Returns a new DatagramSocket that is bound to the given port.  This
     * value should be passed to setListeningSocket(DatagramSocket) to commit
     * to the new port.  If setListeningSocket is NOT called, you should close
     * the return socket.
     * @return a new DatagramSocket that is bound to the specified port.
     * @exception IOException Thrown if the DatagramSocket could not be
     * created.
     */
    DatagramSocket newListeningSocket(int port) throws IOException {
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            DatagramSocket s = channel.socket();
            s.setReceiveBufferSize(64 * 1024);
            s.setSendBufferSize(64 * 1024);
            s.bind(new InetSocketAddress(port));
            return s;
        } catch (SecurityException se) {
            throw new IOException("security exception on port: " + port);
        }
    }

    /** 
     * Changes the DatagramSocket used for sending/receiving.  Typically called
     * by Acceptor to commit to the new port.
     * @param datagramSocket the new listening socket, which must be be the
     *  return value of newListeningSocket(int).  A value of null disables 
     *  UDP sending and receiving.
	 */
    void setListeningSocket(DatagramSocket datagramSocket) {
        if (_channel != null) {
            try {
                _channel.close();
            } catch (IOException ignored) {
            }
        }
        if (datagramSocket != null) {
            _channel = datagramSocket.getChannel();
            if (_channel == null) throw new IllegalArgumentException("No channel!");
            NIODispatcher.instance().registerReadWrite(_channel, this);
            synchronized (this) {
                _lastReportedPort = _channel.socket().getLocalPort();
                _portStable = true;
            }
        }
    }

    /**
	 * Notification that a read can happen.
	 */
    public void handleRead() throws IOException {
        while (true) {
            BUFFER.clear();
            SocketAddress from;
            try {
                from = _channel.receive(BUFFER);
            } catch (IOException iox) {
                break;
            }
            if (from == null) break;
            if (!(from instanceof InetSocketAddress)) {
                Assert.silent(false, "non-inet SocketAddress: " + from);
                continue;
            }
            InetSocketAddress addr = (InetSocketAddress) from;
            if (!NetworkUtils.isValidAddress(addr.getAddress())) continue;
            if (!NetworkUtils.isValidPort(addr.getPort())) continue;
            byte[] data = BUFFER.array();
            int length = BUFFER.position();
            try {
                InputStream in = new ByteArrayInputStream(data, 0, length);
                Message message = Message.read(in, Message.N_UDP, IN_HEADER_BUF);
                if (message == null) continue;
                processMessage(message, addr);
            } catch (IOException ignored) {
            } catch (BadPacketException ignored) {
            }
        }
    }

    /**
	 * Notification that an IOException occurred while reading/writing.
	 */
    public void handleIOException(IOException iox) {
        ErrorService.error(iox, "UDP Error.");
    }

    /**
	 * Processes a single message.
	 */
    protected void processMessage(Message message, InetSocketAddress addr) {
        updateState(message, addr);
        MessageDispatcher.instance().dispatchUDP(message, addr);
    }

    /** Updates internal state of the UDP Service. */
    private void updateState(Message message, InetSocketAddress addr) {
        if (!isGUESSCapable()) {
            if (message instanceof PingRequest) {
                GUID guid = new GUID(message.getGUID());
                if (isValidForIncoming(CONNECT_BACK_GUID, guid, addr)) _acceptedUnsolicitedIncoming = true;
                _lastUnsolicitedIncomingTime = System.currentTimeMillis();
            } else if (message instanceof PingReply) {
                GUID guid = new GUID(message.getGUID());
                if (!isValidForIncoming(SOLICITED_PING_GUID, guid, addr)) return;
                _acceptedSolicitedIncoming = true;
                PingReply r = (PingReply) message;
                if (r.getMyPort() != 0) {
                    synchronized (this) {
                        _numReceivedIPPongs++;
                        if (_numReceivedIPPongs == 1) _lastReportedPort = r.getMyPort(); else if (_lastReportedPort != r.getMyPort()) {
                            _portStable = false;
                            _lastReportedPort = r.getMyPort();
                        }
                    }
                }
            }
        }
        if (message instanceof ReplyNumberVendorMessage) _lastUnsolicitedIncomingTime = System.currentTimeMillis();
    }

    /**
	 * Determines whether or not the specified message is valid for setting
	 * LimeWire as accepting UDP messages (solicited or unsolicited).
	 */
    private boolean isValidForIncoming(GUID match, GUID guidReceived, InetSocketAddress addr) {
        if (!match.equals(guidReceived)) return false;
        String host = addr.getAddress().getHostAddress();
        return !ConnectionSettings.LOCAL_IS_PRIVATE.getValue() || !RouterService.getConnectionManager().isConnectedTo(host);
    }

    /**
     * Sends the specified <tt>Message</tt> to the specified host.
     * 
     * @param msg the <tt>Message</tt> to send
     * @param host the host to send the message to
     */
    public void send(Message msg, IpPort host) {
        send(msg, host.getInetAddress(), host.getPort());
    }

    /**
	 * Sends the <tt>Message</tt> via UDP to the port and IP address specified.
     * This method should not be called if the client is not GUESS enabled.
     *
	 * @param msg  the <tt>Message</tt> to send
	 * @param ip   the <tt>InetAddress</tt> to send to
	 * @param port the port to send to
	 */
    public void send(Message msg, InetAddress ip, int port) throws IllegalArgumentException {
        send(msg, ip, port, ErrorService.getErrorCallback());
    }

    /**
	 * Sends the <tt>Message</tt> via UDP to the port and IP address specified.
     * This method should not be called if the client is not GUESS enabled.
     *
	 * @param msg  the <tt>Message</tt> to send
	 * @param ip   the <tt>InetAddress</tt> to send to
	 * @param port the port to send to
     * @param err  an <tt>ErrorCallback<tt> if you want to be notified errors
     * @throws IllegalArgumentException if msg, ip, or err is null.
	 */
    public void send(Message msg, InetAddress ip, int port, ErrorCallback err) throws IllegalArgumentException {
        if (err == null) throw new IllegalArgumentException("Null ErrorCallback");
        if (msg == null) throw new IllegalArgumentException("Null Message");
        if (ip == null) throw new IllegalArgumentException("Null InetAddress");
        if (!NetworkUtils.isValidPort(port)) throw new IllegalArgumentException("Invalid Port: " + port);
        if (_channel == null || _channel.socket().isClosed()) return;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(msg.getTotalLength());
        synchronized (OUT_HEADER_BUF) {
            try {
                msg.write(baos, OUT_HEADER_BUF);
            } catch (IOException e) {
                ErrorService.error(e);
                return;
            }
        }
        byte[] data = baos.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        synchronized (OUTGOING_MSGS) {
            OUTGOING_MSGS.add(new SendBundle(buffer, ip, port, err));
            if (_channel != null) NIODispatcher.instance().interestWrite(_channel, true);
        }
    }

    /**
	 * Notification that a write can happen.
	 */
    public void handleWrite() throws IOException {
        synchronized (OUTGOING_MSGS) {
            while (!OUTGOING_MSGS.isEmpty()) {
                SendBundle bundle = (SendBundle) OUTGOING_MSGS.remove(0);
                if (_channel.send(bundle.buffer, bundle.addr) == 0) {
                    OUTGOING_MSGS.add(0, bundle);
                    return;
                }
            }
            NIODispatcher.instance().interestWrite(_channel, false);
        }
    }

    /** Wrapper for outgoing data */
    private static class SendBundle {

        private final ByteBuffer buffer;

        private final SocketAddress addr;

        private final ErrorCallback callback;

        SendBundle(ByteBuffer b, InetAddress addr, int port, ErrorCallback c) {
            buffer = b;
            this.addr = new InetSocketAddress(addr, port);
            callback = c;
        }
    }

    /**
	 * Returns whether or not this node is capable of sending its own
	 * GUESS queries.  This would not be the case only if this node
	 * has not successfully received an incoming UDP packet.
	 *
	 * @return <tt>true</tt> if this node is capable of running its own
	 *  GUESS queries, <tt>false</tt> otherwise
	 */
    public boolean isGUESSCapable() {
        return canReceiveUnsolicited() && canReceiveSolicited();
    }

    /**
	 * Returns whether or not this node is capable of receiving UNSOLICITED
     * UDP packets.  It is false until a UDP ConnectBack ping has been received.
	 *
	 * @return <tt>true</tt> if this node has accepted a UNSOLICITED UDP packet.
	 */
    public boolean canReceiveUnsolicited() {
        return _acceptedUnsolicitedIncoming;
    }

    /**
	 * Returns whether or not this node is capable of receiving SOLICITED
     * UDP packets.  
	 *
	 * @return <tt>true</tt> if this node has accepted a SOLICITED UDP packet.
	 */
    public boolean canReceiveSolicited() {
        return _acceptedSolicitedIncoming;
    }

    /**
	 * 
	 * @return whether this node can do Firewall-to-firewall transfers.
	 *  Until we get back any udp packet, the answer is no.
	 *  If we have received an udp packet but are not connected, or haven't 
	 * received a pong carrying ip info yet, see if we ever disabled fwt in the 
	 * past.
	 *  If we are connected and have gotten a single ip pong, our port must be 
	 * the same as our tcp port or our forced tcp port.
	 *  If we have received more than one ip pong, they must all report the same
	 * port.
	 */
    public boolean canDoFWT() {
        if (!canReceiveSolicited()) return false;
        if (!RouterService.isConnected()) return !ConnectionSettings.LAST_FWT_STATE.getValue();
        boolean ret = true;
        synchronized (this) {
            if (_numReceivedIPPongs < 1) return !ConnectionSettings.LAST_FWT_STATE.getValue();
            if (LOG.isTraceEnabled()) {
                LOG.trace("stable " + _portStable + " last reported port " + _lastReportedPort + " our external port " + RouterService.getPort() + " our non-forced port " + RouterService.getAcceptor().getPort(false) + " number of received IP pongs " + _numReceivedIPPongs + " valid external addr " + NetworkUtils.isValidAddress(RouterService.getExternalAddress()));
            }
            ret = NetworkUtils.isValidAddress(RouterService.getExternalAddress()) && _portStable;
            if (_numReceivedIPPongs == 1) {
                ret = ret && (_lastReportedPort == RouterService.getAcceptor().getPort(false) || _lastReportedPort == RouterService.getPort());
            }
        }
        ConnectionSettings.LAST_FWT_STATE.setValue(!ret);
        return ret;
    }

    public boolean portStable() {
        return _portStable;
    }

    public int receivedIpPong() {
        return _numReceivedIPPongs;
    }

    public int lastReportedPort() {
        return _lastReportedPort;
    }

    /**
	 * @return the stable UDP port as seen from the outside.
	 *   If we have received more than one IPPongs and they report
	 * the same port, we return that.
	 *   If we have received just one IPpong, and if its address 
	 * matches either our local port or external port, return that.
	 *   If we have not received any IPpongs, return whatever 
	 * RouterService thinks our port is.
	 */
    public int getStableUDPPort() {
        int localPort = RouterService.getAcceptor().getPort(false);
        int forcedPort = RouterService.getPort();
        synchronized (this) {
            if (_portStable && _numReceivedIPPongs > 1) return _lastReportedPort;
            if (_numReceivedIPPongs == 1 && (localPort == _lastReportedPort || forcedPort == _lastReportedPort)) return _lastReportedPort;
        }
        return forcedPort;
    }

    /**
	 * Sets whether or not this node is capable of receiving SOLICITED
     * UDP packets.  This is useful for testing UDPConnections.
	 *
	 */
    public void setReceiveSolicited(boolean value) {
        _acceptedSolicitedIncoming = value;
    }

    /**
	 * Returns whether or not the UDP socket is listening for incoming
	 * messsages.
	 *
	 * @return <tt>true</tt> if the UDP socket is listening for incoming
	 *  UDP messages, <tt>false</tt> otherwise
	 */
    public boolean isListening() {
        if (_channel == null) return false;
        return (_channel.socket().getLocalPort() != -1);
    }

    /** 
	 * Overrides Object.toString to give more informative information
	 * about the class.
	 *
	 * @return the <tt>DatagramSocket</tt> data
	 */
    public String toString() {
        return "UDPAcceptor\r\nchannel: " + _channel;
    }

    private static class MLImpl implements MessageListener {

        public boolean _gotIncoming = false;

        public void processMessage(Message m, ReplyHandler handler) {
            if ((m instanceof PingRequest)) _gotIncoming = true;
        }

        public void registered(byte[] guid) {
        }

        public void unregistered(byte[] guid) {
        }
    }

    private class IncomingValidator implements Runnable {

        public IncomingValidator() {
        }

        public void run() {
            final long currTime = System.currentTimeMillis();
            final MessageRouter mr = RouterService.getMessageRouter();
            final ConnectionManager cm = RouterService.getConnectionManager();
            if (mr == null || cm == null) return;
            if ((_acceptedUnsolicitedIncoming && ((currTime - _lastUnsolicitedIncomingTime) > Acceptor.INCOMING_EXPIRE_TIME)) || (!_acceptedUnsolicitedIncoming && ((currTime - _lastConnectBackTime) > Acceptor.INCOMING_EXPIRE_TIME))) {
                final GUID cbGuid = new GUID(GUID.makeGuid());
                final MLImpl ml = new MLImpl();
                mr.registerMessageListener(cbGuid.bytes(), ml);
                if (cm.sendUDPConnectBackRequests(cbGuid)) {
                    _lastConnectBackTime = System.currentTimeMillis();
                    Runnable checkThread = new Runnable() {

                        public void run() {
                            if ((_acceptedUnsolicitedIncoming && (_lastUnsolicitedIncomingTime < currTime)) || (!_acceptedUnsolicitedIncoming)) _acceptedUnsolicitedIncoming = ml._gotIncoming;
                            mr.unregisterMessageListener(cbGuid.bytes(), ml);
                        }
                    };
                    RouterService.schedule(checkThread, Acceptor.WAIT_TIME_AFTER_REQUESTS, 0);
                } else mr.unregisterMessageListener(cbGuid.bytes(), ml);
            }
        }
    }

    private class PeriodicPinger implements Runnable {

        public void run() {
            GUESSEndpoint ep = QueryUnicaster.instance().getUnicastEndpoint();
            if (ep == null) return;
            if (!canReceiveSolicited() && !canReceiveUnsolicited()) return;
            PingRequest pr = new PingRequest(getSolicitedGUID().bytes(), (byte) 1, (byte) 0);
            pr.addIPRequest();
            send(pr, ep.getAddress(), ep.getPort());
        }
    }
}
