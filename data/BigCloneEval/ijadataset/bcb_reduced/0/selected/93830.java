package uk.nominet.dnsjnio;

import org.xbill.DNS.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * A nonblocking implementation of Resolver. Multiple concurrent sendAsync
 * queries can be run without increasing the number of threads.
 * 
 * 
 */
public class NonblockingResolver implements INonblockingResolver {

    /** The default port to send queries to */
    public static final int DEFAULT_PORT = 53;

    private InetSocketAddress remoteAddress = new InetSocketAddress(DEFAULT_PORT);

    private boolean useTCP = false, ignoreTruncation;

    private TSIG tsig;

    private int timeoutValue = 10 * 1000;

    /** The default EDNS payload size */
    public static final int DEFAULT_EDNS_PAYLOADSIZE = 1280;

    private static final short DEFAULT_UDPSIZE = 512;

    private OPTRecord queryOPT;

    private static String defaultResolver = "localhost";

    private static short uniqueID = 0;

    private static java.util.Random random = new java.util.Random();

    private SinglePortTransactionController transactionController;

    private boolean useSinglePort = false;

    private InetSocketAddress localAddress = new InetSocketAddress(0);

    /**
	 * Creates a SimpleResolver that will query the specified host
	 * 
	 * @exception UnknownHostException
	 *                Failure occurred while finding the host
	 */
    public NonblockingResolver(String hostname) throws UnknownHostException {
        if (hostname == null) {
            hostname = ResolverConfig.getCurrentConfig().server();
            if (hostname == null) hostname = defaultResolver;
        }
        InetAddress addr;
        if (hostname.equals("0")) addr = InetAddress.getLocalHost(); else addr = InetAddress.getByName(hostname);
        remoteAddress = new InetSocketAddress(addr, DEFAULT_PORT);
        transactionController = new SinglePortTransactionController(remoteAddress, localAddress);
    }

    /**
	 * Creates a SimpleResolver. The host to query is either found by using
	 * ResolverConfig, or the default host is used.
	 * 
	 * @see ResolverConfig
	 * @exception UnknownHostException
	 *                Failure occurred while finding the host
	 */
    public NonblockingResolver() throws UnknownHostException {
        this(null);
    }

    InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /** Sets the default host (initially localhost) to query */
    public static void setDefaultResolver(String hostname) {
        defaultResolver = hostname;
    }

    /**
	 * Sets the address of the server to communicate with.
	 * 
	 * @param addr
	 *            The address of the DNS server
	 */
    public void setRemoteAddress(InetSocketAddress addr) {
        remoteAddress = addr;
        transactionController.setRemoteAddress(remoteAddress);
    }

    /**
	 * Sets the address of the server to communicate with (on the default DNS
	 * port)
	 * 
	 * @param addr
	 *            The address of the DNS server
	 */
    public void setRemoteAddress(InetAddress addr) {
        remoteAddress = new InetSocketAddress(addr, remoteAddress.getPort());
        transactionController.setRemoteAddress(remoteAddress);
    }

    /**
	 * Sets the server port to communicate on.
	 * 
	 * @param port
	 *            The server DNS port
	 */
    public void setRemotePort(int port) {
        remoteAddress = new InetSocketAddress(remoteAddress.getAddress(), port);
        transactionController.setRemoteAddress(remoteAddress);
    }

    /**
	 * Sets the local address to bind to when sending messages. If useSinglePort
	 * is false then random ports will be used.
	 * 
	 * @param addr
	 *            The local address to send messages from.
	 */
    public void setLocalAddress(InetSocketAddress addr) {
        localAddress = addr;
        transactionController.setLocalAddress(localAddress);
    }

    /**
	 * Sets the local address to bind to when sending messages. A random port
	 * will be used.
	 * 
	 * @param addr
	 *            The local address to send messages from.
	 */
    public void setLocalAddress(InetAddress addr) {
        localAddress = new InetSocketAddress(addr, 0);
        transactionController.setLocalAddress(localAddress);
    }

    /**
	 * Sets the server DNS port
	 * 
	 * @param port
	 *            the server port
	 */
    public void setPort(int port) {
        setRemotePort(port);
    }

    /**
	 * Get the address we're sending queries from
	 * 
	 * @return the local address
	 */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setTCP(boolean flag) {
        this.useTCP = flag;
    }

    public boolean isTCP() {
        return useTCP;
    }

    public void setIgnoreTruncation(boolean flag) {
        this.ignoreTruncation = flag;
    }

    /**
	 * Set single port mode on or off
     * THIS ONLY WORKS FOR TCP-BASED QUERIES - UDP QUERIES WILL ALWAYS USE A RANDOM PORT
	 * 
	 * @param useSamePort
	 *            should same port be used for all the queries?
	 */
    public void setSingleTcpPort(boolean useSamePort) {
        this.useSinglePort = useSamePort;
    }

    /**
	 * In single port mode?
     * THIS ONLY WORKS FOR TCP-BASED QUERIES - UDP QUERIES WILL ALWAYS USE A RANDOM PORT
	 * 
	 * @return true if a single port should be used for all queries
	 */
    public boolean isSingleTcpPort() {
        return useSinglePort;
    }

    /**
	 * Sets the local port to bind to when sending messages. A random port will
	 * be used if useSinglePort is false.
     * THIS ONLY WORKS FOR TCP-BASED QUERIES - UDP QUERIES WILL ALWAYS USE A RANDOM PORT
	 * 
	 * @param port
	 *            The local port to send messages from.
	 */
    public void setLocalTcpPort(int port) {
        localAddress = new InetSocketAddress(localAddress.getHostName(), port);
        transactionController.setLocalAddress(localAddress);
    }

    public void setEDNS(int level, int payloadSize, int flags, List options) {
        if (level != 0 && level != -1) throw new IllegalArgumentException("invalid EDNS level - " + "must be 0 or -1");
        if (payloadSize == 0) payloadSize = DEFAULT_EDNS_PAYLOADSIZE;
        queryOPT = new OPTRecord(payloadSize, 0, level, flags, options);
    }

    public void setEDNS(int level) {
        setEDNS(level, 0, 0, null);
    }

    private void applyEDNS(Message query) {
        if (queryOPT == null || query.getOPT() != null) return;
        query.addRecord(queryOPT, Section.ADDITIONAL);
    }

    public void setTSIGKey(TSIG key) {
        tsig = key;
    }

    public void setTSIGKey(Name name, byte[] key) {
        tsig = new TSIG(name, key);
    }

    public void setTSIGKey(String name, String key) {
        tsig = new TSIG(name, key);
    }

    protected TSIG getTSIGKey() {
        return tsig;
    }

    public void setTimeout(int secs) {
        setTimeout(secs, 0);
    }

    public void setTimeout(int secs, int millisecs) {
        timeoutValue = (secs * 1000) + millisecs;
    }

    int getTimeout() {
        return timeoutValue / 1000;
    }

    public int getTimeoutMillis() {
        return timeoutValue;
    }

    private int maxUDPSize(Message query) {
        OPTRecord opt = query.getOPT();
        if (opt == null) return DEFAULT_UDPSIZE; else return opt.getPayloadSize();
    }

    /**
	 * Sends a message to a single server and waits for a response. No checking
	 * is done to ensure that the response is associated with the query (other
	 * than checking that the DNS packet IDs are equal, and that the IP address
	 * which sent the response is the IP address the query was sent to)
	 * The QID of the Message which is sent will be the QID of the Message which
	 * is returned. 
	 * 
	 * @param query
	 *            The query to send.
	 * @return The response.
	 * @throws IOException
	 *             An error occurred while sending or receiving.
	 */
    public Message send(Message query) throws IOException {
        ResponseQueue queue = new ResponseQueue();
        Object id = sendAsync(query, queue);
        Response response = queue.getItem();
        if (response.getId() != id) {
            throw new IllegalStateException("Wrong id (" + response.getId() + ", should be " + id + ") returned from sendAsync()!");
        }
        if (response.isException()) {
            if (response.getException() instanceof SocketTimeoutException) {
                throw new SocketTimeoutException();
            } else if (response.getException() instanceof IOException) {
                throw (IOException) (response.getException());
            } else {
                throw new IllegalStateException("Unexpected exception!\r\n" + response.getException().toString());
            }
        }
        return response.getMessage();
    }

    /**
	 * Old-style interface
	 * 
	 * @param message
	 *            message to send
	 * @param resolverListener
	 *            object to call back
	 * @return id of the query
	 */
    public Object sendAsync(Message message, ResolverListener resolverListener) {
        final Object id;
        synchronized (this) {
            id = new Integer(uniqueID++);
        }
        sendAsync(message, id, resolverListener);
        return id;
    }

    /**
	 * Old-style interface
	 * 
	 * @param message
	 *            message to send
	 * @param resolverListener
	 *            object to call back
	 */
    public void sendAsync(Message message, Object id, ResolverListener resolverListener) {
        sendAsync(message, id, timeoutValue, useTCP, null, false, resolverListener);
    }

    /**
	 * Asynchronously sends a message to a single nameserver, registering a
	 * ResponseQueue to buffer responses on success or exception. Multiple
	 * asynchronous lookups can be performed in parallel.
	 * 
	 * @param query
	 *            The query to send
	 * @param responseQueue
	 *            the queue for the responses
	 * @return An identifier, which is also a data member of the Response
	 */
    public Object sendAsync(final Message query, final ResponseQueue responseQueue) {
        final Object id;
        synchronized (this) {
            id = new Integer(uniqueID++);
        }
        sendAsync(query, id, responseQueue);
        return id;
    }

    /**
	 * Add the query to the queue for the NonblockingResolverEngine
	 * 
	 * @param query
	 *            The query to send
	 * @param id
	 *            The object to be used as the id in the callback
	 * @param responseQueue
	 *            The queue for the responses
	 */
    public void sendAsync(final Message query, Object id, final ResponseQueue responseQueue) {
        sendAsync(query, id, timeoutValue, useTCP, responseQueue);
    }

    public void sendAsync(final Message inQuery, Object id, int inQueryTimeout, boolean queryUseTCP, final ResponseQueue responseQueue) {
        sendAsync(inQuery, id, inQueryTimeout, queryUseTCP, responseQueue, true, null);
    }

    private void sendAsync(final Message inQuery, Object id, int inQueryTimeout, boolean queryUseTCP, final ResponseQueue responseQueue, boolean useResponseQueue, ResolverListener listener) {
        if (!useResponseQueue && (listener == null)) {
            throw new IllegalArgumentException("No ResolverListener supplied for callback when useResponsequeue = true!");
        }
        if (Options.check("verbose")) System.err.println("Sending to " + remoteAddress.getAddress() + ", from " + remoteAddress.getAddress());
        if (inQuery.getHeader().getOpcode() == Opcode.QUERY) {
            Record question = inQuery.getQuestion();
            if (question != null && question.getType() == Type.AXFR) {
                throw new UnsupportedOperationException("AXFR not implemented in NonblockingResolver");
            }
        }
        int queryTimeout = inQueryTimeout;
        Message query = (Message) inQuery.clone();
        applyEDNS(query);
        if (tsig != null) tsig.apply(query, null);
        byte[] out = query.toWire(Message.MAXLENGTH);
        int udpSize = maxUDPSize(query);
        boolean tcp = false;
        long endTime = System.currentTimeMillis() + queryTimeout;
        if (queryUseTCP || out.length > udpSize) {
            tcp = true;
        }
        if (useSinglePort && tcp && transactionController.headerIdNotInUse(query.getHeader().getID())) {
            QueryData qData = new QueryData();
            qData.setTcp(tcp);
            qData.setIgnoreTruncation(ignoreTruncation);
            qData.setTsig(tsig);
            qData.setQuery(query);
            if (!tcp) {
                qData.setUdpSize(udpSize);
            }
            if (useResponseQueue) {
                transactionController.sendQuery(qData, id, responseQueue, endTime);
            } else {
                transactionController.sendQuery(qData, id, listener, endTime);
            }
        } else {
            InetSocketAddress localAddr = getNewInetSocketAddressWithRandomPort(localAddress.getAddress());
            Transaction transaction = new Transaction(remoteAddress, localAddr, tsig, tcp, ignoreTruncation);
            if (!tcp) {
                transaction.setUdpSize(udpSize);
            }
            if (useResponseQueue) {
                transaction.sendQuery(query, id, responseQueue, endTime);
            } else {
                transaction.sendQuery(query, id, listener, endTime);
            }
        }
    }

    public static InetSocketAddress getNewInetSocketAddressWithRandomPort(InetAddress addr) {
        int portNum = 1024 + random.nextInt(65535 - 1024);
        InetSocketAddress localAddr = new InetSocketAddress(addr, portNum);
        return localAddr;
    }

    public static Message parseMessage(byte[] b) throws WireParseException {
        try {
            return (new Message(b));
        } catch (IOException e) {
            if (Options.check("verbose")) e.printStackTrace();
            if (!(e instanceof WireParseException)) e = new WireParseException("Error parsing message");
            throw (WireParseException) e;
        }
    }

    public static void verifyTSIG(Message query, Message response, byte[] b, TSIG tsig) {
        if (tsig == null) return;
        int error = tsig.verify(response, b, query.getTSIG());
        if (Options.check("verbose")) System.err.println("TSIG verify: " + Rcode.string(error));
    }

    /**
	 * Called by the Connection to check if the data received so far constitutes
	 * a complete packet.
	 * 
	 * @param in
	 * @return true if the packet is complete
	 */
    public static boolean isDataComplete(byte[] in) {
        try {
            if (in.length < Header.LENGTH) {
                return false;
            }
            Message message = parseMessage(in);
            int messLen = message.numBytes();
            boolean ready = (messLen == in.length);
            return (ready);
        } catch (IOException e) {
            return false;
        }
    }
}
