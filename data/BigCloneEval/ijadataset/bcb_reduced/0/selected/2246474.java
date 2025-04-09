package com.limegroup.gnutella;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.Vector;
import junit.framework.Test;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.security.AddressSecurityToken;
import org.limewire.security.MACCalculatorRepositoryManager;
import com.google.inject.Injector;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.MessageFactory;
import com.limegroup.gnutella.messages.PingReply;
import com.limegroup.gnutella.messages.PingReplyFactory;
import com.limegroup.gnutella.messages.PingRequest;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.Message.Network;
import com.limegroup.gnutella.settings.ConnectionSettings;

@SuppressWarnings("unchecked")
public class QueryUnicasterTest extends com.limegroup.gnutella.util.LimeTestCase {

    private static final Log LOG = LogFactory.getLog(QueryUnicasterTest.class);

    /**
	 * Constant for the size of UDP messages to accept -- dependent upon
	 * IP-layer fragmentation.
	 */
    private final int BUFFER_SIZE = 8192;

    private final int NUM_UDP_LOOPS = 25;

    private boolean _shouldRun = true;

    private boolean shouldRun() {
        return _shouldRun;
    }

    private Vector _messages = new Vector();

    private QueryUnicaster queryUnicaster;

    private LifecycleManager lifecycleManager;

    private QueryRequestFactory queryRequestFactory;

    private ResponseFactory responseFactory;

    private QueryReplyFactory queryReplyFactory;

    private MessageFactory messageFactory;

    private PingReplyFactory pingReplyFactory;

    private MACCalculatorRepositoryManager macManager;

    public QueryUnicasterTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(QueryUnicasterTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void setUp() throws Exception {
        ConnectionSettings.DO_NOT_BOOTSTRAP.setValue(true);
        ConnectionSettings.CONNECT_ON_STARTUP.setValue(false);
        Injector injector = LimeTestUtils.createInjector();
        queryUnicaster = injector.getInstance(QueryUnicaster.class);
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        queryUnicaster = injector.getInstance(QueryUnicaster.class);
        queryRequestFactory = injector.getInstance(QueryRequestFactory.class);
        responseFactory = injector.getInstance(ResponseFactory.class);
        queryReplyFactory = injector.getInstance(QueryReplyFactory.class);
        messageFactory = injector.getInstance(MessageFactory.class);
        pingReplyFactory = injector.getInstance(PingReplyFactory.class);
        macManager = injector.getInstance(MACCalculatorRepositoryManager.class);
        lifecycleManager.start();
    }

    protected void tearDown() throws Exception {
        lifecycleManager.shutdown();
    }

    public void testConstruction() {
        QueryUnicaster qu = queryUnicaster;
        assertEquals("unexpected amount of unicast endpoints", 0, qu.getUnicastEndpoints().size());
    }

    public void testQueries() throws Exception {
        _messages.clear();
        _shouldRun = true;
        Thread[] udpLoopers = new Thread[NUM_UDP_LOOPS];
        for (int i = 0; i < NUM_UDP_LOOPS; i++) {
            final int index = i;
            udpLoopers[i] = new Thread() {

                public void run() {
                    udpLoop(5000 + index);
                }
            };
            udpLoopers[i].start();
            Thread.yield();
        }
        InetAddress addr = null;
        addr = InetAddress.getByName("127.0.0.1");
        for (int i = 0; i < NUM_UDP_LOOPS; i++) {
            queryUnicaster.addUnicastEndpoint(addr, 5000 + i);
            if (i % 5 == 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
            }
        }
        QueryRequest qr = queryRequestFactory.createQuery("susheel", (byte) 2);
        assertEquals("unexpected number of queries", 0, queryUnicaster.getQueryNumber());
        queryUnicaster.addQuery(qr, null);
        assertEquals("unexpected number of queries", 1, queryUnicaster.getQueryNumber());
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException ignored) {
        }
        int numMessages = 0, numQRs = 0, numPings = 0, numQKReqs = 0;
        while (!_messages.isEmpty()) {
            Message currMessage = (Message) _messages.remove(0);
            numMessages++;
            if (currMessage instanceof QueryRequest) {
                QueryRequest currQR = (QueryRequest) currMessage;
                assertEquals("unexpected query", "susheel", currQR.getQuery());
                numQRs++;
            } else if (currMessage instanceof PingRequest) {
                numPings++;
                if (((PingRequest) currMessage).isQueryKeyRequest()) numQKReqs++;
            } else fail("unexpected message: " + currMessage);
        }
        assertEquals("unexpected number of messages", numMessages, numPings + numQRs);
        assertLessThanOrEquals("unexpected number of QRs", numPings, numQRs);
        assertGreaterThan("unexpected number of QRs", 0, numQRs);
        assertLessThanOrEquals("unexpected number of QRs", numQKReqs, numQRs);
        if (LOG.isDebugEnabled()) {
            LOG.debug("QueryUnicasterTest.testQueries(): numMessages = " + numMessages);
            LOG.debug("QueryUnicasterTest.testQueries(): numQRs = " + numQRs);
            LOG.debug("QueryUnicasterTest.testQueries(): numPings = " + numPings);
        }
        _shouldRun = false;
        for (int i = 0; i < NUM_UDP_LOOPS; i++) udpLoopers[i].interrupt();
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ignored) {
        }
        QueryReply qRep = generateFakeReply(qr.getGUID(), 251);
        queryUnicaster.handleQueryReply(qRep);
    }

    public void testResultMaxOut() throws Exception {
        _messages.clear();
        _shouldRun = true;
        Thread[] udpLoopers = new Thread[NUM_UDP_LOOPS];
        for (int i = 0; i < NUM_UDP_LOOPS; i++) {
            final int index = i;
            udpLoopers[i] = new Thread() {

                public void run() {
                    udpLoop(5000 + index);
                }
            };
            udpLoopers[i].start();
            Thread.yield();
        }
        QueryRequest qr = queryRequestFactory.createQuery("Daswani", (byte) 2);
        queryUnicaster.addQuery(qr, null);
        InetAddress addr = null;
        addr = InetAddress.getByName("127.0.0.1");
        for (int i = 0; i < NUM_UDP_LOOPS; i++) {
            queryUnicaster.addUnicastEndpoint(addr, 5000 + i);
            if (i % 5 == 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
            }
            int low = 250 / NUM_UDP_LOOPS + 1;
            int hi = 254 / NUM_UDP_LOOPS + 1;
            if (low < 25) low = 25;
            if (hi < 35) hi = 35;
            QueryReply qRep = generateFakeReply(qr.getGUID(), getNumberBetween(low, hi));
            queryUnicaster.handleQueryReply(qRep);
        }
        try {
            Thread.sleep(30 * 1000);
            assertEquals("unexpected number of queries", 0, queryUnicaster.getQueryNumber());
        } catch (InterruptedException ignored) {
        }
        int numMessages = 0, numQRs = 0, numPings = 0;
        while (!_messages.isEmpty()) {
            Message currMessage = (Message) _messages.remove(0);
            numMessages++;
            if (currMessage instanceof QueryRequest) {
                QueryRequest currQR = (QueryRequest) currMessage;
                assertEquals("daswani", currQR.getQuery());
                numQRs++;
            } else if (currMessage instanceof PingRequest) {
                numPings++;
            } else fail("unexpected message: " + currMessage);
        }
        assertEquals("unexpected number of messages", numMessages, numPings + numQRs);
        assertLessThan("unexpected number of QRs", 11, numQRs);
        if (NUM_UDP_LOOPS > 10) assertGreaterThan("unexpected endpoint size", 0, queryUnicaster.getUnicastEndpoints().size());
        if (LOG.isDebugEnabled()) {
            LOG.debug("QueryUnicasterTest.testQueries(): numMessages = " + numMessages);
            LOG.debug("QueryUnicasterTest.testQueries(): numQRs = " + numQRs);
            LOG.debug("QueryUnicasterTest.testQueries(): numPings = " + numPings);
        }
        _shouldRun = false;
        for (int i = 0; i < NUM_UDP_LOOPS; i++) udpLoopers[i].interrupt();
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    private QueryReply generateFakeReply(byte[] guid, int numResponses) {
        Response[] resps = new Response[numResponses];
        for (int i = 0; i < resps.length; i++) resps[i] = responseFactory.createResponse(i, i, "" + i);
        byte[] ip = { (byte) 127, (byte) 0, (byte) 0, (byte) 1 };
        QueryReply toReturn = queryReplyFactory.createQueryReply(guid, (byte) 2, 1, ip, 0, resps, GUID.makeGuid(), false);
        return toReturn;
    }

    /** returns a number from low to high (both inclusive).
     */
    private int getNumberBetween(int low, int high) {
        Random rand = new Random();
        int retInt = low - 1;
        while (retInt < low) retInt = rand.nextInt(high + 1);
        return retInt;
    }

    private static byte[] localhost = { (byte) 127, (byte) 0, (byte) 0, (byte) 1 };

    /**
     * Busy loop that listens on a port for udp messages and then logs them.
	 */
    private void udpLoop(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);
            if (LOG.isDebugEnabled()) LOG.debug("QueryUnicasterTest.udpLoop(): listening on port " + port);
        } catch (SocketException e) {
            if (LOG.isDebugEnabled()) LOG.debug("QueryUnicasterTest.udpLoop(): couldn't listen on port " + port);
            return;
        } catch (RuntimeException e) {
            if (LOG.isDebugEnabled()) LOG.debug("QueryUnicasterTest.udpLoop(): couldn't listen on port " + port);
            return;
        }
        byte[] datagramBytes = new byte[BUFFER_SIZE];
        DatagramPacket datagram = new DatagramPacket(datagramBytes, BUFFER_SIZE);
        while (shouldRun()) {
            try {
                socket.receive(datagram);
                byte[] data = datagram.getData();
                int length = datagram.getLength();
                try {
                    InputStream in = new ByteArrayInputStream(data, 0, length);
                    Message message = messageFactory.read(in, Network.TCP);
                    if (message == null) continue;
                    if (message instanceof PingRequest) {
                        PingRequest pr = (PingRequest) message;
                        pr.hop();
                        if (pr.isQueryKeyRequest()) {
                            AddressSecurityToken qk = new AddressSecurityToken(datagram.getAddress(), datagram.getPort(), macManager);
                            PingReply pRep = pingReplyFactory.createQueryKeyReply(pr.getGUID(), (byte) 1, port, localhost, 2, 2, true, qk);
                            pRep.hop();
                            LOG.debug("QueryUnicasterTest.udpLoop(): sending QK.");
                            queryUnicaster.handleQueryKeyPong(pRep);
                        }
                    }
                    synchronized (_messages) {
                        if (LOG.isDebugEnabled()) LOG.debug(" ** Adding message to _messages queue (newSize=" + (_messages.size() + 1) + ") m=" + message);
                        _messages.add(message);
                        _messages.notify();
                    }
                } catch (BadPacketException e) {
                    continue;
                }
            } catch (InterruptedIOException e) {
                continue;
            } catch (IOException e) {
                continue;
            }
        }
        if (LOG.isDebugEnabled()) LOG.debug("QueryUnicasterTest.udpLoop(): closing down port " + port);
        socket.close();
    }
}
