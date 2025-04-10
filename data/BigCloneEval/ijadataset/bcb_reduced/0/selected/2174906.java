package com.aelitis.azureus.core.dht.transport.udp.impl;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.*;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.ipfilter.IpFilter;
import org.gudy.azureus2.core3.ipfilter.IpFilterManagerFactory;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.AESemaphore;
import org.gudy.azureus2.core3.util.AEThread;
import org.gudy.azureus2.core3.util.Average;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.SimpleTimer;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TimerEvent;
import org.gudy.azureus2.core3.util.TimerEventPerformer;
import com.aelitis.azureus.core.dht.DHT;
import com.aelitis.azureus.core.dht.DHTLogger;
import com.aelitis.azureus.core.dht.impl.DHTLog;
import com.aelitis.azureus.core.dht.netcoords.DHTNetworkPosition;
import com.aelitis.azureus.core.dht.netcoords.DHTNetworkPositionManager;
import com.aelitis.azureus.core.dht.netcoords.DHTNetworkPositionProvider;
import com.aelitis.azureus.core.dht.transport.*;
import com.aelitis.azureus.core.dht.transport.udp.*;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketHandler;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketHandlerException;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketHandlerFactory;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketReceiver;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPRequestHandler;
import com.aelitis.azureus.core.dht.transport.util.DHTTransportRequestCounter;
import com.aelitis.azureus.core.util.bloom.BloomFilter;
import com.aelitis.azureus.core.util.bloom.BloomFilterFactory;
import com.aelitis.net.udp.uc.PRUDPPacketHandler;

/**
 * @author parg
 *
 */
public class DHTTransportUDPImpl implements DHTTransportUDP, DHTUDPRequestHandler {

    public static boolean TEST_EXTERNAL_IP = false;

    public static final int TRANSFER_QUEUE_MAX = 64;

    public static final long WRITE_XFER_RESEND_DELAY = 12500;

    public static final long READ_XFER_REREQUEST_DELAY = 5000;

    public static final long WRITE_REPLY_TIMEOUT = 60000;

    private static boolean XFER_TRACE = false;

    static {
        if (XFER_TRACE) {
            System.out.println("**** DHTTransportUDPImpl xfer trace on ****");
        }
    }

    private String external_address;

    private byte protocol_version;

    private int network;

    private String ip_override;

    private int port;

    private int max_fails_for_live;

    private int max_fails_for_unknown;

    private long request_timeout;

    private long store_timeout;

    private boolean reachable;

    private boolean reachable_accurate;

    private int dht_send_delay;

    private int dht_receive_delay;

    private DHTLogger logger;

    private DHTUDPPacketHandler packet_handler;

    private DHTTransportRequestHandler request_handler;

    private DHTTransportUDPContactImpl local_contact;

    private Map transfer_handlers = new HashMap();

    private Map read_transfers = new HashMap();

    private Map write_transfers = new HashMap();

    private Map call_transfers = new HashMap();

    private long last_address_change;

    private List listeners = new ArrayList();

    private IpFilter ip_filter = IpFilterManagerFactory.getSingleton().getIPFilter();

    private DHTTransportUDPStatsImpl stats;

    private boolean bootstrap_node = false;

    private static final int CONTACT_HISTORY_MAX = 32;

    private static final int CONTACT_HISTORY_PING_SIZE = 16;

    private Map contact_history = new LinkedHashMap(CONTACT_HISTORY_MAX, 0.75f, true) {

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > CONTACT_HISTORY_MAX;
        }
    };

    private static final int ROUTABLE_CONTACT_HISTORY_MAX = 32;

    private Map routable_contact_history = new LinkedHashMap(ROUTABLE_CONTACT_HISTORY_MAX, 0.75f, true) {

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > ROUTABLE_CONTACT_HISTORY_MAX;
        }
    };

    private long other_routable_total;

    private long other_non_routable_total;

    private static final int RECENT_REPORTS_HISTORY_MAX = 32;

    private Map recent_reports = new LinkedHashMap(RECENT_REPORTS_HISTORY_MAX, 0.75f, true) {

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > RECENT_REPORTS_HISTORY_MAX;
        }
    };

    private static final int STATS_PERIOD = 60 * 1000;

    private static final int STATS_DURATION_SECS = 600;

    private static final long STATS_INIT_PERIOD = 15 * 60 * 1000;

    private long stats_start_time = SystemTime.getCurrentTime();

    private long last_alien_count;

    private long last_alien_fv_count;

    private Average alien_average = Average.getInstance(STATS_PERIOD, STATS_DURATION_SECS);

    private Average alien_fv_average = Average.getInstance(STATS_PERIOD, STATS_DURATION_SECS);

    private Random random;

    private static final int BAD_IP_BLOOM_FILTER_SIZE = 32000;

    private BloomFilter bad_ip_bloom_filter;

    private static AEMonitor class_mon = new AEMonitor("DHTTransportUDP:class");

    private AEMonitor this_mon = new AEMonitor("DHTTransportUDP");

    public DHTTransportUDPImpl(byte _protocol_version, int _network, String _ip, String _default_ip, int _port, int _max_fails_for_live, int _max_fails_for_unknown, long _timeout, int _dht_send_delay, int _dht_receive_delay, boolean _bootstrap_node, boolean _initial_reachability, DHTLogger _logger) throws DHTTransportException {
        protocol_version = _protocol_version;
        network = _network;
        ip_override = _ip;
        port = _port;
        max_fails_for_live = _max_fails_for_live;
        max_fails_for_unknown = _max_fails_for_unknown;
        request_timeout = _timeout;
        dht_send_delay = _dht_send_delay;
        dht_receive_delay = _dht_receive_delay;
        bootstrap_node = _bootstrap_node;
        reachable = _initial_reachability;
        logger = _logger;
        store_timeout = request_timeout * 2;
        try {
            random = new SecureRandom();
        } catch (Throwable e) {
            random = new Random();
            logger.log(e);
        }
        createPacketHandler();
        SimpleTimer.addPeriodicEvent("DHTUDP:stats", STATS_PERIOD, new TimerEventPerformer() {

            public void perform(TimerEvent event) {
                updateStats();
            }
        });
        String default_ip = _default_ip == null ? "127.0.0.1" : _default_ip;
        getExternalAddress(default_ip, logger);
        InetSocketAddress address = new InetSocketAddress(external_address, port);
        logger.log("Initial external address: " + address);
        local_contact = new DHTTransportUDPContactImpl(true, this, address, address, protocol_version, random.nextInt(), 0);
    }

    protected void createPacketHandler() throws DHTTransportException {
        DHTUDPPacketHelper.registerCodecs();
        try {
            if (packet_handler != null) {
                packet_handler.destroy();
            }
            packet_handler = DHTUDPPacketHandlerFactory.getHandler(this, this);
        } catch (Throwable e) {
            throw (new DHTTransportException("Failed to get packet handler", e));
        }
        packet_handler.setDelays(dht_send_delay, dht_receive_delay, (int) request_timeout);
        stats_start_time = SystemTime.getCurrentTime();
        if (stats == null) {
            stats = new DHTTransportUDPStatsImpl(protocol_version, packet_handler.getStats());
        } else {
            stats.setStats(packet_handler.getStats());
        }
    }

    protected void updateStats() {
        long alien_count = 0;
        long[] aliens = stats.getAliens();
        for (int i = 0; i < aliens.length; i++) {
            alien_count += aliens[i];
        }
        long alien_fv_count = aliens[DHTTransportStats.AT_FIND_VALUE];
        alien_average.addValue((alien_count - last_alien_count) * STATS_PERIOD / 1000);
        alien_fv_average.addValue((alien_fv_count - last_alien_fv_count) * STATS_PERIOD / 1000);
        last_alien_count = alien_count;
        last_alien_fv_count = alien_fv_count;
        long now = SystemTime.getCurrentTime();
        if (now < stats_start_time) {
            stats_start_time = now;
        } else {
            if (now - stats_start_time > STATS_INIT_PERIOD) {
                reachable_accurate = true;
                boolean old_reachable = reachable;
                if (alien_fv_average.getAverage() > 0) {
                    reachable = true;
                } else if (alien_average.getAverage() > 3) {
                    reachable = true;
                } else {
                    reachable = false;
                }
                if (old_reachable != reachable) {
                    for (int i = 0; i < listeners.size(); i++) {
                        try {
                            ((DHTTransportListener) listeners.get(i)).reachabilityChanged(reachable);
                        } catch (Throwable e) {
                            Debug.printStackTrace(e);
                        }
                    }
                }
            }
        }
    }

    protected int getNodeStatus() {
        if (bootstrap_node) {
            return (0);
        }
        if (reachable_accurate) {
            int status = reachable ? DHTTransportUDPContactImpl.NODE_STATUS_ROUTABLE : 0;
            return (status);
        } else {
            return (DHTTransportUDPContactImpl.NODE_STATUS_UNKNOWN);
        }
    }

    public boolean isReachable() {
        return (reachable);
    }

    public byte getProtocolVersion() {
        return (protocol_version);
    }

    public int getPort() {
        return (port);
    }

    public void setPort(int new_port) throws DHTTransportException {
        if (new_port == port) {
            return;
        }
        port = new_port;
        createPacketHandler();
        setLocalContact();
    }

    public int getNetwork() {
        return (network);
    }

    public void testInstanceIDChange() throws DHTTransportException {
        local_contact = new DHTTransportUDPContactImpl(true, this, local_contact.getTransportAddress(), local_contact.getExternalAddress(), protocol_version, random.nextInt(), 0);
    }

    public void testTransportIDChange() throws DHTTransportException {
        if (external_address.equals("127.0.0.1")) {
            external_address = "192.168.0.2";
        } else {
            external_address = "127.0.0.1";
        }
        InetSocketAddress address = new InetSocketAddress(external_address, port);
        local_contact = new DHTTransportUDPContactImpl(true, this, address, address, protocol_version, local_contact.getInstanceID(), 0);
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((DHTTransportListener) listeners.get(i)).localContactChanged(local_contact);
            } catch (Throwable e) {
                Debug.printStackTrace(e);
            }
        }
    }

    public void testExternalAddressChange() {
        try {
            Iterator it = contact_history.values().iterator();
            DHTTransportUDPContactImpl c1 = (DHTTransportUDPContactImpl) it.next();
            DHTTransportUDPContactImpl c2 = (DHTTransportUDPContactImpl) it.next();
            externalAddressChange(c1, c2.getExternalAddress());
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
    }

    public void testNetworkAlive(boolean alive) {
        packet_handler.testNetworkAlive(alive);
    }

    protected void getExternalAddress(String default_address, final DHTLogger log) {
        try {
            class_mon.enter();
            String new_external_address = null;
            try {
                log.log("Obtaining external address");
                if (TEST_EXTERNAL_IP) {
                    new_external_address = "127.0.0.1";
                    log.log("    External IP address obtained from test data: " + new_external_address);
                }
                if (ip_override != null) {
                    new_external_address = ip_override;
                    log.log("    External IP address explicitly overridden: " + new_external_address);
                }
                if (new_external_address == null) {
                    List contacts;
                    try {
                        this_mon.enter();
                        contacts = new ArrayList(contact_history.values());
                    } finally {
                        this_mon.exit();
                    }
                    String returned_address = null;
                    int returned_matches = 0;
                    int search_lim = Math.min(CONTACT_HISTORY_PING_SIZE, contacts.size());
                    log.log("    Contacts to search = " + search_lim);
                    for (int i = 0; i < search_lim; i++) {
                        DHTTransportUDPContactImpl contact = (DHTTransportUDPContactImpl) contacts.remove((int) (contacts.size() * Math.random()));
                        InetSocketAddress a = askContactForExternalAddress(contact);
                        if (a != null && a.getAddress() != null) {
                            String ip = a.getAddress().getHostAddress();
                            if (returned_address == null) {
                                returned_address = ip;
                                log.log("    : contact " + contact.getString() + " reported external address as '" + ip + "'");
                                returned_matches++;
                            } else if (returned_address.equals(ip)) {
                                returned_matches++;
                                log.log("    : contact " + contact.getString() + " also reported external address as '" + ip + "'");
                                if (returned_matches == 3) {
                                    new_external_address = returned_address;
                                    log.log("    External IP address obtained from contacts: " + returned_address);
                                    break;
                                }
                            } else {
                                log.log("    : contact " + contact.getString() + " reported external address as '" + ip + "', abandoning due to mismatch");
                                break;
                            }
                        } else {
                            log.log("    : contact " + contact.getString() + " didn't reply");
                        }
                    }
                }
                if (new_external_address == null) {
                    InetAddress public_address = logger.getPluginInterface().getUtilities().getPublicAddress();
                    if (public_address != null) {
                        new_external_address = public_address.getHostAddress();
                        log.log("    External IP address obtained: " + new_external_address);
                    }
                }
            } catch (Throwable e) {
                Debug.printStackTrace(e);
            }
            if (new_external_address == null) {
                new_external_address = default_address;
                log.log("    External IP address defaulted:  " + new_external_address);
            }
            if (external_address == null || !external_address.equals(new_external_address)) {
                informLocalAddress(new_external_address);
            }
            external_address = new_external_address;
        } finally {
            class_mon.exit();
        }
    }

    protected void informLocalAddress(String address) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((DHTTransportListener) listeners.get(i)).currentAddress(address);
            } catch (Throwable e) {
                Debug.printStackTrace(e);
            }
        }
    }

    protected void externalAddressChange(DHTTransportUDPContactImpl reporter, InetSocketAddress new_address) throws DHTTransportException {
        InetAddress ia = new_address.getAddress();
        if (ia == null) {
            Debug.out("reported new external address '" + new_address + "' is unresolved");
            throw (new DHTTransportException("Address '" + new_address + "' is unresolved"));
        }
        final String new_ip = ia.getHostAddress();
        if (new_ip.equals(external_address)) {
            return;
        }
        try {
            this_mon.enter();
            long now = SystemTime.getCurrentTime();
            if (now - last_address_change < 5 * 60 * 1000) {
                return;
            }
            logger.log("Node " + reporter.getString() + " has reported that the external IP address is '" + new_address + "'");
            if (invalidExternalAddress(ia)) {
                logger.log("     This is invalid as it is a private address.");
                return;
            }
            if (reporter.getExternalAddress().getAddress().getHostAddress().equals(new_ip)) {
                logger.log("     This is invalid as it is the same as the reporter's address.");
                return;
            }
            last_address_change = now;
        } finally {
            this_mon.exit();
        }
        final String old_external_address = external_address;
        new AEThread("DHTTransportUDP:getAddress", true) {

            public void runSupport() {
                getExternalAddress(new_ip, logger);
                if (old_external_address.equals(external_address)) {
                    return;
                }
                setLocalContact();
            }
        }.start();
    }

    protected void contactAlive(DHTTransportUDPContactImpl contact) {
        try {
            this_mon.enter();
            contact_history.put(contact.getTransportAddress(), contact);
        } finally {
            this_mon.exit();
        }
    }

    public DHTTransportContact[] getReachableContacts() {
        try {
            this_mon.enter();
            Collection vals = routable_contact_history.values();
            DHTTransportContact[] res = new DHTTransportContact[vals.size()];
            vals.toArray(res);
            return (res);
        } finally {
            this_mon.exit();
        }
    }

    protected void updateContactStatus(DHTTransportUDPContactImpl contact, int status) {
        try {
            this_mon.enter();
            contact.setNodeStatus(status);
            if (contact.getProtocolVersion() >= DHTTransportUDP.PROTOCOL_VERSION_XFER_STATUS) {
                if (status != DHTTransportUDPContactImpl.NODE_STATUS_UNKNOWN) {
                    boolean other_routable = (status & DHTTransportUDPContactImpl.NODE_STATUS_ROUTABLE) != 0;
                    if (other_routable) {
                        other_routable_total++;
                        routable_contact_history.put(contact.getTransportAddress(), contact);
                    } else {
                        other_non_routable_total++;
                    }
                }
            }
        } finally {
            this_mon.exit();
        }
    }

    protected boolean invalidExternalAddress(InetAddress ia) {
        return (ia.isLinkLocalAddress() || ia.isLoopbackAddress() || ia.isSiteLocalAddress());
    }

    protected int getMaxFailForLiveCount() {
        return (max_fails_for_live);
    }

    protected int getMaxFailForUnknownCount() {
        return (max_fails_for_unknown);
    }

    public DHTTransportContact getLocalContact() {
        return (local_contact);
    }

    protected void setLocalContact() {
        InetSocketAddress s_address = new InetSocketAddress(external_address, port);
        try {
            local_contact = new DHTTransportUDPContactImpl(true, DHTTransportUDPImpl.this, s_address, s_address, protocol_version, random.nextInt(), 0);
            logger.log("External address changed: " + s_address);
            for (int i = 0; i < listeners.size(); i++) {
                try {
                    ((DHTTransportListener) listeners.get(i)).localContactChanged(local_contact);
                } catch (Throwable e) {
                    Debug.printStackTrace(e);
                }
            }
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
    }

    public DHTTransportContact importContact(DataInputStream is) throws IOException, DHTTransportException {
        DHTTransportUDPContactImpl contact = DHTUDPUtils.deserialiseContact(this, is);
        importContact(contact);
        return (contact);
    }

    public DHTTransportUDPContact importContact(InetSocketAddress _address, byte _protocol_version) throws DHTTransportException {
        DHTTransportUDPContactImpl contact = new DHTTransportUDPContactImpl(false, this, _address, _address, _protocol_version, 0, 0);
        importContact(contact);
        return (contact);
    }

    protected void importContact(DHTTransportUDPContactImpl contact) {
        try {
            this_mon.enter();
            if (contact_history.size() < CONTACT_HISTORY_MAX) {
                contact_history.put(contact.getTransportAddress(), contact);
            }
        } finally {
            this_mon.exit();
        }
        request_handler.contactImported(contact);
    }

    public void exportContact(DHTTransportContact contact, DataOutputStream os) throws IOException, DHTTransportException {
        DHTUDPUtils.serialiseContact(os, contact);
    }

    public void removeContact(DHTTransportContact contact) {
        request_handler.contactRemoved(contact);
    }

    public void setRequestHandler(DHTTransportRequestHandler _request_handler) {
        request_handler = new DHTTransportRequestCounter(_request_handler, stats);
    }

    public DHTTransportStats getStats() {
        return (stats);
    }

    protected void checkAddress(DHTTransportUDPContactImpl contact) throws DHTUDPPacketHandlerException {
        if (ip_filter.isEnabled()) {
            byte[] addr = contact.getTransportAddress().getAddress().getAddress();
            if (bad_ip_bloom_filter == null) {
                bad_ip_bloom_filter = BloomFilterFactory.createAddOnly(BAD_IP_BLOOM_FILTER_SIZE);
            } else {
                if (bad_ip_bloom_filter.contains(addr)) {
                    throw (new DHTUDPPacketHandlerException("IPFilter check fails (repeat)"));
                }
            }
            if (ip_filter.isInRange(contact.getTransportAddress().getAddress(), "DHT", logger.isEnabled(DHTLogger.LT_IP_FILTER))) {
                if (bad_ip_bloom_filter.getEntryCount() >= BAD_IP_BLOOM_FILTER_SIZE / 10) {
                    bad_ip_bloom_filter = BloomFilterFactory.createAddOnly(BAD_IP_BLOOM_FILTER_SIZE);
                }
                bad_ip_bloom_filter.add(addr);
                throw (new DHTUDPPacketHandlerException("IPFilter check fails"));
            }
        }
    }

    protected void sendPing(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler, long timeout, int priority) {
        try {
            checkAddress(contact);
            final long connection_id = getConnectionID();
            final DHTUDPPacketRequestPing request = new DHTUDPPacketRequestPing(this, connection_id, local_contact, contact);
            stats.pingSent(request);
            requestSendRequestProcessor(contact, request);
            packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                public void packetReceived(DHTUDPPacketReply packet, InetSocketAddress from_address, long elapsed_time) {
                    try {
                        if (packet.getConnectionId() != connection_id) {
                            throw (new Exception("connection id mismatch"));
                        }
                        contact.setInstanceIDAndVersion(packet.getTargetInstanceID(), packet.getProtocolVersion());
                        requestSendReplyProcessor(contact, handler, packet, elapsed_time);
                        stats.pingOK();
                        handler.pingReply(contact, (int) elapsed_time);
                    } catch (DHTUDPPacketHandlerException e) {
                        error(e);
                    } catch (Throwable e) {
                        Debug.printStackTrace(e);
                        error(new DHTUDPPacketHandlerException("ping failed", e));
                    }
                }

                public void error(DHTUDPPacketHandlerException e) {
                    stats.pingFailed();
                    handler.failed(contact, e);
                }
            }, timeout, priority);
        } catch (Throwable e) {
            stats.pingFailed();
            handler.failed(contact, e);
        }
    }

    protected void sendPing(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler) {
        sendPing(contact, handler, request_timeout, PRUDPPacketHandler.PRIORITY_MEDIUM);
    }

    protected void sendImmediatePing(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler, long timeout) {
        sendPing(contact, handler, timeout, PRUDPPacketHandler.PRIORITY_IMMEDIATE);
    }

    protected void sendKeyBlockRequest(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler, byte[] block_request, byte[] block_signature) {
        try {
            checkAddress(contact);
            final long connection_id = getConnectionID();
            final DHTUDPPacketRequestKeyBlock request = new DHTUDPPacketRequestKeyBlock(this, connection_id, local_contact, contact);
            request.setKeyBlockDetails(block_request, block_signature);
            stats.keyBlockSent(request);
            request.setRandomID(contact.getRandomID());
            requestSendRequestProcessor(contact, request);
            packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                public void packetReceived(DHTUDPPacketReply packet, InetSocketAddress from_address, long elapsed_time) {
                    try {
                        if (packet.getConnectionId() != connection_id) {
                            throw (new Exception("connection id mismatch"));
                        }
                        contact.setInstanceIDAndVersion(packet.getTargetInstanceID(), packet.getProtocolVersion());
                        requestSendReplyProcessor(contact, handler, packet, elapsed_time);
                        stats.keyBlockOK();
                        handler.keyBlockReply(contact);
                    } catch (DHTUDPPacketHandlerException e) {
                        error(e);
                    } catch (Throwable e) {
                        Debug.printStackTrace(e);
                        error(new DHTUDPPacketHandlerException("send key block failed", e));
                    }
                }

                public void error(DHTUDPPacketHandlerException e) {
                    stats.keyBlockFailed();
                    handler.failed(contact, e);
                }
            }, request_timeout, PRUDPPacketHandler.PRIORITY_MEDIUM);
        } catch (Throwable e) {
            stats.keyBlockFailed();
            handler.failed(contact, e);
        }
    }

    protected void sendStats(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler) {
        try {
            checkAddress(contact);
            final long connection_id = getConnectionID();
            final DHTUDPPacketRequestStats request = new DHTUDPPacketRequestStats(this, connection_id, local_contact, contact);
            stats.statsSent(request);
            requestSendRequestProcessor(contact, request);
            packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                public void packetReceived(DHTUDPPacketReply packet, InetSocketAddress from_address, long elapsed_time) {
                    try {
                        if (packet.getConnectionId() != connection_id) {
                            throw (new Exception("connection id mismatch"));
                        }
                        contact.setInstanceIDAndVersion(packet.getTargetInstanceID(), packet.getProtocolVersion());
                        requestSendReplyProcessor(contact, handler, packet, elapsed_time);
                        DHTUDPPacketReplyStats reply = (DHTUDPPacketReplyStats) packet;
                        stats.statsOK();
                        if (reply.getStatsType() == DHTUDPPacketRequestStats.STATS_TYPE_ORIGINAL) {
                            handler.statsReply(contact, reply.getOriginalStats());
                        } else {
                            System.out.println("new stats reply:" + reply.getString());
                        }
                    } catch (DHTUDPPacketHandlerException e) {
                        error(e);
                    } catch (Throwable e) {
                        Debug.printStackTrace(e);
                        error(new DHTUDPPacketHandlerException("stats failed", e));
                    }
                }

                public void error(DHTUDPPacketHandlerException e) {
                    stats.statsFailed();
                    handler.failed(contact, e);
                }
            }, request_timeout, PRUDPPacketHandler.PRIORITY_LOW);
        } catch (Throwable e) {
            stats.statsFailed();
            handler.failed(contact, e);
        }
    }

    protected InetSocketAddress askContactForExternalAddress(DHTTransportUDPContactImpl contact) {
        try {
            checkAddress(contact);
            final long connection_id = getConnectionID();
            final DHTUDPPacketRequestPing request = new DHTUDPPacketRequestPing(this, connection_id, local_contact, contact);
            stats.pingSent(request);
            final AESemaphore sem = new AESemaphore("DHTTransUDP:extping");
            final InetSocketAddress[] result = new InetSocketAddress[1];
            packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                public void packetReceived(DHTUDPPacketReply _packet, InetSocketAddress from_address, long elapsed_time) {
                    try {
                        if (_packet instanceof DHTUDPPacketReplyPing) {
                            result[0] = local_contact.getExternalAddress();
                        } else if (_packet instanceof DHTUDPPacketReplyError) {
                            DHTUDPPacketReplyError packet = (DHTUDPPacketReplyError) _packet;
                            if (packet.getErrorType() == DHTUDPPacketReplyError.ET_ORIGINATOR_ADDRESS_WRONG) {
                                result[0] = packet.getOriginatingAddress();
                            }
                        }
                    } finally {
                        sem.release();
                    }
                }

                public void error(DHTUDPPacketHandlerException e) {
                    try {
                        stats.pingFailed();
                    } finally {
                        sem.release();
                    }
                }
            }, 5000, PRUDPPacketHandler.PRIORITY_HIGH);
            sem.reserve(5000);
            return (result[0]);
        } catch (Throwable e) {
            stats.pingFailed();
            return (null);
        }
    }

    public void sendStore(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler, byte[][] keys, DHTTransportValue[][] value_sets) {
        final long connection_id = getConnectionID();
        if (false) {
            int total_values = 0;
            for (int i = 0; i < keys.length; i++) {
                total_values += value_sets[i].length;
            }
            System.out.println("store: keys = " + keys.length + ", values = " + total_values);
        }
        int packet_count = 0;
        try {
            checkAddress(contact);
            int current_key_index = 0;
            int current_value_index = 0;
            while (current_key_index < keys.length) {
                packet_count++;
                int space = DHTUDPPacketHelper.PACKET_MAX_BYTES - DHTUDPPacketRequest.DHT_HEADER_SIZE;
                List key_list = new ArrayList();
                List values_list = new ArrayList();
                key_list.add(keys[current_key_index]);
                space -= (keys[current_key_index].length + 1);
                values_list.add(new ArrayList());
                while (space > 0 && current_key_index < keys.length) {
                    if (current_value_index == value_sets[current_key_index].length) {
                        current_key_index++;
                        current_value_index = 0;
                        if (key_list.size() == DHTUDPPacketRequestStore.MAX_KEYS_PER_PACKET) {
                            break;
                        }
                        if (current_key_index == keys.length) {
                            break;
                        }
                        key_list.add(keys[current_key_index]);
                        space -= (keys[current_key_index].length + 1);
                        values_list.add(new ArrayList());
                    }
                    DHTTransportValue value = value_sets[current_key_index][current_value_index];
                    int entry_size = DHTUDPUtils.DHTTRANSPORTVALUE_SIZE_WITHOUT_VALUE + value.getValue().length + 1;
                    List values = (List) values_list.get(values_list.size() - 1);
                    if (space < entry_size || values.size() == DHTUDPPacketRequestStore.MAX_VALUES_PER_KEY) {
                        break;
                    }
                    values.add(value);
                    space -= entry_size;
                    current_value_index++;
                }
                int packet_entries = key_list.size();
                if (packet_entries > 0) {
                    if (((List) values_list.get(packet_entries - 1)).size() == 0) {
                        packet_entries--;
                    }
                }
                if (packet_entries == 0) {
                    break;
                }
                byte[][] packet_keys = new byte[packet_entries][];
                DHTTransportValue[][] packet_value_sets = new DHTTransportValue[packet_entries][];
                for (int i = 0; i < packet_entries; i++) {
                    packet_keys[i] = (byte[]) key_list.get(i);
                    List values = (List) values_list.get(i);
                    packet_value_sets[i] = new DHTTransportValue[values.size()];
                    for (int j = 0; j < values.size(); j++) {
                        packet_value_sets[i][j] = (DHTTransportValue) values.get(j);
                    }
                }
                final DHTUDPPacketRequestStore request = new DHTUDPPacketRequestStore(this, connection_id, local_contact, contact);
                stats.storeSent(request);
                request.setRandomID(contact.getRandomID());
                request.setKeys(packet_keys);
                request.setValueSets(packet_value_sets);
                final int f_packet_count = packet_count;
                requestSendRequestProcessor(contact, request);
                packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                    public void packetReceived(DHTUDPPacketReply packet, InetSocketAddress from_address, long elapsed_time) {
                        try {
                            if (packet.getConnectionId() != connection_id) {
                                throw (new Exception("connection id mismatch: sender=" + from_address + ",packet=" + packet.getString()));
                            }
                            contact.setInstanceIDAndVersion(packet.getTargetInstanceID(), packet.getProtocolVersion());
                            requestSendReplyProcessor(contact, handler, packet, elapsed_time);
                            DHTUDPPacketReplyStore reply = (DHTUDPPacketReplyStore) packet;
                            stats.storeOK();
                            if (f_packet_count == 1) {
                                handler.storeReply(contact, reply.getDiversificationTypes());
                            }
                        } catch (DHTUDPPacketHandlerException e) {
                            error(e);
                        } catch (Throwable e) {
                            Debug.printStackTrace(e);
                            error(new DHTUDPPacketHandlerException("store failed", e));
                        }
                    }

                    public void error(DHTUDPPacketHandlerException e) {
                        stats.storeFailed();
                        if (f_packet_count == 1) {
                            handler.failed(contact, e);
                        }
                    }
                }, store_timeout, PRUDPPacketHandler.PRIORITY_LOW);
            }
        } catch (Throwable e) {
            stats.storeFailed();
            if (packet_count <= 1) {
                handler.failed(contact, e);
            }
        }
    }

    public void sendFindNode(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler, byte[] nid) {
        try {
            checkAddress(contact);
            final long connection_id = getConnectionID();
            final DHTUDPPacketRequestFindNode request = new DHTUDPPacketRequestFindNode(this, connection_id, local_contact, contact);
            stats.findNodeSent(request);
            request.setID(nid);
            requestSendRequestProcessor(contact, request);
            packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                public void packetReceived(DHTUDPPacketReply packet, InetSocketAddress from_address, long elapsed_time) {
                    try {
                        if (packet.getConnectionId() != connection_id) {
                            throw (new Exception("connection id mismatch"));
                        }
                        contact.setInstanceIDAndVersion(packet.getTargetInstanceID(), packet.getProtocolVersion());
                        requestSendReplyProcessor(contact, handler, packet, elapsed_time);
                        DHTUDPPacketReplyFindNode reply = (DHTUDPPacketReplyFindNode) packet;
                        contact.setRandomID(reply.getRandomID());
                        updateContactStatus(contact, reply.getNodeStatus());
                        request_handler.setTransportEstimatedDHTSize(reply.getEstimatedDHTSize());
                        stats.findNodeOK();
                        handler.findNodeReply(contact, reply.getContacts());
                    } catch (DHTUDPPacketHandlerException e) {
                        error(e);
                    } catch (Throwable e) {
                        Debug.printStackTrace(e);
                        error(new DHTUDPPacketHandlerException("findNode failed", e));
                    }
                }

                public void error(DHTUDPPacketHandlerException e) {
                    stats.findNodeFailed();
                    handler.failed(contact, e);
                }
            }, request_timeout, PRUDPPacketHandler.PRIORITY_MEDIUM);
        } catch (Throwable e) {
            stats.findNodeFailed();
            handler.failed(contact, e);
        }
    }

    public void sendFindValue(final DHTTransportUDPContactImpl contact, final DHTTransportReplyHandler handler, byte[] key, int max_values, byte flags) {
        try {
            checkAddress(contact);
            final long connection_id = getConnectionID();
            final DHTUDPPacketRequestFindValue request = new DHTUDPPacketRequestFindValue(this, connection_id, local_contact, contact);
            stats.findValueSent(request);
            request.setID(key);
            request.setMaximumValues(max_values);
            request.setFlags(flags);
            requestSendRequestProcessor(contact, request);
            packet_handler.sendAndReceive(request, contact.getTransportAddress(), new DHTUDPPacketReceiver() {

                public void packetReceived(DHTUDPPacketReply packet, InetSocketAddress from_address, long elapsed_time) {
                    try {
                        if (packet.getConnectionId() != connection_id) {
                            throw (new Exception("connection id mismatch"));
                        }
                        contact.setInstanceIDAndVersion(packet.getTargetInstanceID(), packet.getProtocolVersion());
                        requestSendReplyProcessor(contact, handler, packet, elapsed_time);
                        DHTUDPPacketReplyFindValue reply = (DHTUDPPacketReplyFindValue) packet;
                        stats.findValueOK();
                        DHTTransportValue[] res = reply.getValues();
                        if (res != null) {
                            boolean continuation = reply.hasContinuation();
                            handler.findValueReply(contact, res, reply.getDiversificationType(), continuation);
                        } else {
                            handler.findValueReply(contact, reply.getContacts());
                        }
                    } catch (DHTUDPPacketHandlerException e) {
                        error(e);
                    } catch (Throwable e) {
                        Debug.printStackTrace(e);
                        error(new DHTUDPPacketHandlerException("findValue failed", e));
                    }
                }

                public void error(DHTUDPPacketHandlerException e) {
                    stats.findValueFailed();
                    handler.failed(contact, e);
                }
            }, request_timeout, PRUDPPacketHandler.PRIORITY_HIGH);
        } catch (Throwable e) {
            if (!(e instanceof DHTUDPPacketHandlerException)) {
                stats.findValueFailed();
                handler.failed(contact, e);
            }
        }
    }

    protected DHTTransportFullStats getFullStats(DHTTransportUDPContactImpl contact) {
        if (contact == local_contact) {
            return (request_handler.statsRequest(contact));
        }
        final DHTTransportFullStats[] res = { null };
        final AESemaphore sem = new AESemaphore("DHTTransportUDP:getFullStats");
        sendStats(contact, new DHTTransportReplyHandlerAdapter() {

            public void statsReply(DHTTransportContact _contact, DHTTransportFullStats _stats) {
                res[0] = _stats;
                sem.release();
            }

            public void failed(DHTTransportContact _contact, Throwable _error) {
                sem.release();
            }
        });
        sem.reserve();
        return (res[0]);
    }

    protected void sendReadRequest(long connection_id, DHTTransportUDPContactImpl contact, byte[] transfer_key, byte[] key) {
        sendReadRequest(connection_id, contact, transfer_key, key, 0, 0);
    }

    protected void sendReadRequest(long connection_id, DHTTransportUDPContactImpl contact, byte[] transfer_key, byte[] key, int start_pos, int len) {
        final DHTUDPPacketData request = new DHTUDPPacketData(this, connection_id, local_contact, contact);
        request.setDetails(DHTUDPPacketData.PT_READ_REQUEST, transfer_key, key, new byte[0], start_pos, len, 0);
        try {
            checkAddress(contact);
            if (XFER_TRACE) {
                logger.log("Transfer read request: key = " + DHTLog.getFullString(key) + ", contact = " + contact.getString());
            }
            stats.dataSent(request);
            packet_handler.send(request, contact.getTransportAddress());
        } catch (Throwable e) {
        }
    }

    protected void sendReadReply(long connection_id, DHTTransportUDPContactImpl contact, byte[] transfer_key, byte[] key, byte[] data, int start_position, int length, int total_length) {
        final DHTUDPPacketData request = new DHTUDPPacketData(this, connection_id, local_contact, contact);
        request.setDetails(DHTUDPPacketData.PT_READ_REPLY, transfer_key, key, data, start_position, length, total_length);
        try {
            checkAddress(contact);
            if (XFER_TRACE) {
                logger.log("Transfer read reply: key = " + DHTLog.getFullString(key) + ", contact = " + contact.getString());
            }
            stats.dataSent(request);
            packet_handler.send(request, contact.getTransportAddress());
        } catch (Throwable e) {
        }
    }

    protected void sendWriteRequest(long connection_id, DHTTransportUDPContactImpl contact, byte[] transfer_key, byte[] key, byte[] data, int start_position, int length, int total_length) {
        final DHTUDPPacketData request = new DHTUDPPacketData(this, connection_id, local_contact, contact);
        request.setDetails(DHTUDPPacketData.PT_WRITE_REQUEST, transfer_key, key, data, start_position, length, total_length);
        try {
            checkAddress(contact);
            if (XFER_TRACE) {
                logger.log("Transfer write request: key = " + DHTLog.getFullString(key) + ", contact = " + contact.getString());
            }
            stats.dataSent(request);
            packet_handler.send(request, contact.getTransportAddress());
        } catch (Throwable e) {
        }
    }

    protected void sendWriteReply(long connection_id, DHTTransportUDPContactImpl contact, byte[] transfer_key, byte[] key, int start_position, int length) {
        final DHTUDPPacketData request = new DHTUDPPacketData(this, connection_id, local_contact, contact);
        request.setDetails(DHTUDPPacketData.PT_WRITE_REPLY, transfer_key, key, new byte[0], start_position, length, 0);
        try {
            checkAddress(contact);
            if (XFER_TRACE) {
                logger.log("Transfer write reply: key = " + DHTLog.getFullString(key) + ", contact = " + contact.getString());
            }
            stats.dataSent(request);
            packet_handler.send(request, contact.getTransportAddress());
        } catch (Throwable e) {
        }
    }

    public void registerTransferHandler(byte[] handler_key, DHTTransportTransferHandler handler) {
        logger.log("Transfer handler (" + handler.getName() + ") registered for key '" + ByteFormatter.encodeString(handler_key));
        transfer_handlers.put(new HashWrapper(handler_key), new transferHandlerInterceptor(handler));
    }

    protected int handleTransferRequest(DHTTransportUDPContactImpl target, long connection_id, byte[] transfer_key, byte[] request_key, byte[] data, int start, int length, boolean write_request, boolean first_packet_only) throws DHTTransportException {
        DHTTransportTransferHandler handler = (DHTTransportTransferHandler) transfer_handlers.get(new HashWrapper(transfer_key));
        if (handler == null) {
            logger.log("No transfer handler registered for key '" + ByteFormatter.encodeString(transfer_key) + "'");
            throw (new DHTTransportException("No transfer handler registered"));
        }
        if (data == null) {
            data = handler.handleRead(target, request_key);
        }
        if (data == null) {
            return (-1);
        } else {
            if (data.length == 0) {
                if (write_request) {
                    sendWriteRequest(connection_id, target, transfer_key, request_key, data, 0, 0, 0);
                } else {
                    sendReadReply(connection_id, target, transfer_key, request_key, data, 0, 0, 0);
                }
            } else {
                if (start < 0) {
                    start = 0;
                } else if (start >= data.length) {
                    logger.log("dataRequest: invalid start position");
                    return (data.length);
                }
                if (length <= 0) {
                    length = data.length;
                } else if (start + length > data.length) {
                    logger.log("dataRequest: invalid length");
                    return (data.length);
                }
                int end = start + length;
                while (start < end) {
                    int chunk = end - start;
                    if (chunk > DHTUDPPacketData.MAX_DATA_SIZE) {
                        chunk = DHTUDPPacketData.MAX_DATA_SIZE;
                    }
                    if (write_request) {
                        sendWriteRequest(connection_id, target, transfer_key, request_key, data, start, chunk, data.length);
                        if (first_packet_only) {
                            break;
                        }
                    } else {
                        sendReadReply(connection_id, target, transfer_key, request_key, data, start, chunk, data.length);
                    }
                    start += chunk;
                }
            }
            return (data.length);
        }
    }

    protected void dataRequest(final DHTTransportUDPContactImpl originator, final DHTUDPPacketData req) {
        stats.dataReceived();
        byte packet_type = req.getPacketType();
        if (packet_type == DHTUDPPacketData.PT_READ_REPLY) {
            transferQueue queue = lookupTransferQueue(read_transfers, req.getConnectionId());
            if (queue != null) {
                queue.add(req);
            }
        } else if (packet_type == DHTUDPPacketData.PT_WRITE_REPLY) {
            transferQueue queue = lookupTransferQueue(write_transfers, req.getConnectionId());
            if (queue != null) {
                queue.add(req);
            }
        } else {
            byte[] transfer_key = req.getTransferKey();
            if (packet_type == DHTUDPPacketData.PT_READ_REQUEST) {
                try {
                    handleTransferRequest(originator, req.getConnectionId(), transfer_key, req.getRequestKey(), null, req.getStartPosition(), req.getLength(), false, false);
                } catch (DHTTransportException e) {
                    logger.log(e);
                }
            } else {
                transferQueue old_queue = lookupTransferQueue(read_transfers, req.getConnectionId());
                if (old_queue != null) {
                    old_queue.add(req);
                } else {
                    final DHTTransportTransferHandler handler = (DHTTransportTransferHandler) transfer_handlers.get(new HashWrapper(transfer_key));
                    if (handler == null) {
                        logger.log("No transfer handler registered for key '" + ByteFormatter.encodeString(transfer_key) + "'");
                    } else {
                        try {
                            final transferQueue new_queue = new transferQueue(read_transfers, req.getConnectionId());
                            new_queue.add(req);
                            new AEThread("DHTTransportUDP:writeQueueProcessor", true) {

                                public void runSupport() {
                                    try {
                                        byte[] write_data = runTransferQueue(new_queue, new DHTTransportProgressListener() {

                                            public void reportSize(long size) {
                                                if (XFER_TRACE) {
                                                    System.out.println("writeXfer: size=" + size);
                                                }
                                            }

                                            public void reportActivity(String str) {
                                                if (XFER_TRACE) {
                                                    System.out.println("writeXfer: act=" + str);
                                                }
                                            }

                                            public void reportCompleteness(int percent) {
                                                if (XFER_TRACE) {
                                                    System.out.println("writeXfer: %=" + percent);
                                                }
                                            }
                                        }, originator, req.getTransferKey(), req.getRequestKey(), 60000, false);
                                        if (write_data != null) {
                                            if (req.getStartPosition() != 0 || req.getLength() != req.getTotalLength()) {
                                                sendWriteReply(req.getConnectionId(), originator, req.getTransferKey(), req.getRequestKey(), 0, req.getTotalLength());
                                            }
                                            byte[] reply_data = handler.handleWrite(originator, req.getRequestKey(), write_data);
                                            if (reply_data != null) {
                                                writeTransfer(new DHTTransportProgressListener() {

                                                    public void reportSize(long size) {
                                                        if (XFER_TRACE) {
                                                            System.out.println("writeXferReply: size=" + size);
                                                        }
                                                    }

                                                    public void reportActivity(String str) {
                                                        if (XFER_TRACE) {
                                                            System.out.println("writeXferReply: act=" + str);
                                                        }
                                                    }

                                                    public void reportCompleteness(int percent) {
                                                        if (XFER_TRACE) {
                                                            System.out.println("writeXferReply: %=" + percent);
                                                        }
                                                    }
                                                }, originator, req.getTransferKey(), req.getRequestKey(), reply_data, WRITE_REPLY_TIMEOUT);
                                            }
                                        }
                                    } catch (DHTTransportException e) {
                                        logger.log("Failed to process transfer queue: " + Debug.getNestedExceptionMessage(e));
                                    }
                                }
                            }.start();
                            sendWriteReply(req.getConnectionId(), originator, req.getTransferKey(), req.getRequestKey(), req.getStartPosition(), req.getLength());
                        } catch (DHTTransportException e) {
                            logger.log("Faild to create transfer queue");
                            logger.log(e);
                        }
                    }
                }
            }
        }
    }

    public byte[] readTransfer(DHTTransportProgressListener listener, DHTTransportContact target, byte[] handler_key, byte[] key, long timeout) throws DHTTransportException {
        long connection_id = getConnectionID();
        transferQueue transfer_queue = new transferQueue(read_transfers, connection_id);
        return (runTransferQueue(transfer_queue, listener, target, handler_key, key, timeout, true));
    }

    protected byte[] runTransferQueue(transferQueue transfer_queue, DHTTransportProgressListener listener, DHTTransportContact target, byte[] handler_key, byte[] key, long timeout, boolean read_transfer) throws DHTTransportException {
        SortedSet packets = new TreeSet(new Comparator() {

            public int compare(Object o1, Object o2) {
                DHTUDPPacketData p1 = (DHTUDPPacketData) o1;
                DHTUDPPacketData p2 = (DHTUDPPacketData) o2;
                return (p1.getStartPosition() - p2.getStartPosition());
            }
        });
        int entire_request_count = 0;
        int transfer_size = -1;
        int transferred = 0;
        String target_name = DHTLog.getString2(target.getID());
        try {
            long start = SystemTime.getCurrentTime();
            if (read_transfer) {
                listener.reportActivity(getMessageText("request_all", target_name));
                entire_request_count++;
                sendReadRequest(transfer_queue.getID(), (DHTTransportUDPContactImpl) target, handler_key, key);
            } else {
                entire_request_count++;
            }
            while (SystemTime.getCurrentTime() - start <= timeout) {
                DHTUDPPacketData reply = transfer_queue.receive(READ_XFER_REREQUEST_DELAY);
                if (reply != null) {
                    if (transfer_size == -1) {
                        transfer_size = reply.getTotalLength();
                        listener.reportSize(transfer_size);
                    }
                    Iterator it = packets.iterator();
                    boolean duplicate = false;
                    while (it.hasNext()) {
                        DHTUDPPacketData p = (DHTUDPPacketData) it.next();
                        if (p.getStartPosition() < reply.getStartPosition() + reply.getLength() && p.getStartPosition() + p.getLength() > reply.getStartPosition()) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        listener.reportActivity(getMessageText("received_bit", new String[] { String.valueOf(reply.getStartPosition()), String.valueOf(reply.getStartPosition() + reply.getLength()), target_name }));
                        transferred += reply.getLength();
                        listener.reportCompleteness(transfer_size == 0 ? 100 : (100 * transferred / transfer_size));
                        packets.add(reply);
                        it = packets.iterator();
                        int pos = 0;
                        int actual_end = -1;
                        while (it.hasNext()) {
                            DHTUDPPacketData p = (DHTUDPPacketData) it.next();
                            if (actual_end == -1) {
                                actual_end = p.getTotalLength();
                            }
                            if (p.getStartPosition() != pos) {
                                break;
                            }
                            pos += p.getLength();
                            if (pos == actual_end) {
                                listener.reportActivity(getMessageText("complete"));
                                byte[] result = new byte[actual_end];
                                it = packets.iterator();
                                pos = 0;
                                while (it.hasNext()) {
                                    p = (DHTUDPPacketData) it.next();
                                    System.arraycopy(p.getData(), 0, result, pos, p.getLength());
                                    pos += p.getLength();
                                }
                                return (result);
                            }
                        }
                    }
                } else {
                    if (packets.size() == 0) {
                        if (entire_request_count == 2) {
                            listener.reportActivity(getMessageText("timeout", target_name));
                            return (null);
                        }
                        entire_request_count++;
                        listener.reportActivity(getMessageText("rerequest_all", target_name));
                        sendReadRequest(transfer_queue.getID(), (DHTTransportUDPContactImpl) target, handler_key, key);
                    } else {
                        Iterator it = packets.iterator();
                        int pos = 0;
                        int actual_end = -1;
                        while (it.hasNext()) {
                            DHTUDPPacketData p = (DHTUDPPacketData) it.next();
                            if (actual_end == -1) {
                                actual_end = p.getTotalLength();
                            }
                            if (p.getStartPosition() != pos) {
                                listener.reportActivity(getMessageText("rerequest_bit", new String[] { String.valueOf(pos), String.valueOf(p.getStartPosition()), target_name }));
                                sendReadRequest(transfer_queue.getID(), (DHTTransportUDPContactImpl) target, handler_key, key, pos, p.getStartPosition() - pos);
                            }
                            pos = p.getStartPosition() + p.getLength();
                        }
                        if (pos != actual_end) {
                            listener.reportActivity(getMessageText("rerequest_bit", new String[] { String.valueOf(pos), String.valueOf(actual_end), target_name }));
                            sendReadRequest(transfer_queue.getID(), (DHTTransportUDPContactImpl) target, handler_key, key, pos, actual_end - pos);
                        }
                    }
                }
            }
            if (packets.size() == 0) {
                listener.reportActivity(getMessageText("timeout", target_name));
            } else {
                listener.reportActivity(getMessageText("timeout_some", new String[] { String.valueOf(packets.size()), target_name }));
            }
            return (null);
        } finally {
            transfer_queue.destroy();
        }
    }

    public void writeTransfer(DHTTransportProgressListener listener, DHTTransportContact target, byte[] handler_key, byte[] key, byte[] data, long timeout) throws DHTTransportException {
        transferQueue transfer_queue = null;
        try {
            long connection_id = getConnectionID();
            transfer_queue = new transferQueue(write_transfers, connection_id);
            boolean ok = false;
            boolean reply_received = false;
            int loop = 0;
            int total_length = data.length;
            long start = SystemTime.getCurrentTime();
            long last_packet_time = 0;
            while (true) {
                long now = SystemTime.getCurrentTime();
                if (now < start) {
                    start = now;
                    last_packet_time = 0;
                } else {
                    if (now - start > timeout) {
                        break;
                    }
                }
                long time_since_last_packet = now - last_packet_time;
                if (time_since_last_packet >= WRITE_XFER_RESEND_DELAY) {
                    listener.reportActivity(getMessageText(loop == 0 ? "sending" : "resending"));
                    loop++;
                    total_length = handleTransferRequest((DHTTransportUDPContactImpl) target, connection_id, handler_key, key, data, -1, -1, true, reply_received);
                    last_packet_time = now;
                    time_since_last_packet = 0;
                }
                DHTUDPPacketData packet = transfer_queue.receive(WRITE_XFER_RESEND_DELAY - time_since_last_packet);
                if (packet != null) {
                    last_packet_time = now;
                    reply_received = true;
                    if (packet.getStartPosition() == 0 && packet.getLength() == total_length) {
                        ok = true;
                        break;
                    }
                }
            }
            if (ok) {
                listener.reportCompleteness(100);
                listener.reportActivity(getMessageText("send_complete"));
            } else {
                listener.reportActivity(getMessageText("send_timeout"));
                throw (new DHTTransportException("Timeout"));
            }
        } finally {
            if (transfer_queue != null) {
                transfer_queue.destroy();
            }
        }
    }

    public byte[] writeReadTransfer(DHTTransportProgressListener listener, DHTTransportContact target, byte[] handler_key, byte[] data, long timeout) throws DHTTransportException {
        byte[] call_key = new byte[20];
        random.nextBytes(call_key);
        AESemaphore call_sem = new AESemaphore("DHTTransportUDP:calSem");
        HashWrapper wrapped_key = new HashWrapper(call_key);
        try {
            this_mon.enter();
            call_transfers.put(wrapped_key, call_sem);
        } finally {
            this_mon.exit();
        }
        writeTransfer(listener, target, handler_key, call_key, data, timeout);
        if (call_sem.reserve(timeout)) {
            try {
                this_mon.enter();
                Object res = call_transfers.remove(wrapped_key);
                if (res instanceof byte[]) {
                    return ((byte[]) res);
                }
            } finally {
                this_mon.exit();
            }
        }
        throw (new DHTTransportException("timeout"));
    }

    public void process(DHTUDPPacketRequest request, boolean alien) {
        if (request_handler == null) {
            logger.log("Ignoring packet as not yet ready to process");
            return;
        }
        try {
            stats.incomingRequestReceived(request, alien);
            InetSocketAddress transport_address = request.getAddress();
            DHTTransportUDPContactImpl originating_contact = new DHTTransportUDPContactImpl(false, this, transport_address, request.getOriginatorAddress(), request.getOriginatorVersion(), request.getOriginatorInstanceID(), request.getClockSkew());
            try {
                checkAddress(originating_contact);
            } catch (DHTUDPPacketHandlerException e) {
                return;
            }
            requestReceiveRequestProcessor(originating_contact, request);
            if (!originating_contact.addressMatchesID()) {
                String contact_string = originating_contact.getString();
                if (recent_reports.get(contact_string) == null) {
                    recent_reports.put(contact_string, "");
                    logger.log("Node " + contact_string + " has incorrect ID, reporting it to them");
                }
                DHTUDPPacketReplyError reply = new DHTUDPPacketReplyError(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                reply.setErrorType(DHTUDPPacketReplyError.ET_ORIGINATOR_ADDRESS_WRONG);
                reply.setOriginatingAddress(originating_contact.getTransportAddress());
                requestReceiveReplyProcessor(originating_contact, reply);
                packet_handler.send(reply, request.getAddress());
            } else {
                contactAlive(originating_contact);
                if (request instanceof DHTUDPPacketRequestPing) {
                    if (!bootstrap_node) {
                        request_handler.pingRequest(originating_contact);
                        DHTUDPPacketReplyPing reply = new DHTUDPPacketReplyPing(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                        requestReceiveReplyProcessor(originating_contact, reply);
                        packet_handler.send(reply, request.getAddress());
                    }
                } else if (request instanceof DHTUDPPacketRequestKeyBlock) {
                    if (!bootstrap_node) {
                        DHTUDPPacketRequestKeyBlock kb_request = (DHTUDPPacketRequestKeyBlock) request;
                        originating_contact.setRandomID(kb_request.getRandomID());
                        request_handler.keyBlockRequest(originating_contact, kb_request.getKeyBlockRequest(), kb_request.getKeyBlockSignature());
                        DHTUDPPacketReplyKeyBlock reply = new DHTUDPPacketReplyKeyBlock(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                        requestReceiveReplyProcessor(originating_contact, reply);
                        packet_handler.send(reply, request.getAddress());
                    }
                } else if (request instanceof DHTUDPPacketRequestStats) {
                    DHTUDPPacketRequestStats stats_request = (DHTUDPPacketRequestStats) request;
                    DHTUDPPacketReplyStats reply = new DHTUDPPacketReplyStats(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                    int type = stats_request.getStatsType();
                    if (type == DHTUDPPacketRequestStats.STATS_TYPE_ORIGINAL) {
                        DHTTransportFullStats full_stats = request_handler.statsRequest(originating_contact);
                        reply.setOriginalStats(full_stats);
                    } else if (type == DHTUDPPacketRequestStats.STATS_TYPE_NP_VER2) {
                        DHTNetworkPositionProvider prov = DHTNetworkPositionManager.getProvider(DHTNetworkPosition.POSITION_TYPE_VIVALDI_V2);
                        byte[] data = new byte[0];
                        if (prov != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            DataOutputStream dos = new DataOutputStream(baos);
                            prov.serialiseStats(dos);
                            dos.flush();
                            data = baos.toByteArray();
                        }
                        reply.setNewStats(data, DHTNetworkPosition.POSITION_TYPE_VIVALDI_V2);
                    } else {
                        throw (new IOException("Uknown stats type '" + type + "'"));
                    }
                    requestReceiveReplyProcessor(originating_contact, reply);
                    packet_handler.send(reply, request.getAddress());
                } else if (request instanceof DHTUDPPacketRequestStore) {
                    if (!bootstrap_node) {
                        DHTUDPPacketRequestStore store_request = (DHTUDPPacketRequestStore) request;
                        originating_contact.setRandomID(store_request.getRandomID());
                        DHTTransportStoreReply res = request_handler.storeRequest(originating_contact, store_request.getKeys(), store_request.getValueSets());
                        if (res.blocked()) {
                            if (originating_contact.getProtocolVersion() >= DHTTransportUDP.PROTOCOL_VERSION_BLOCK_KEYS) {
                                DHTUDPPacketReplyError reply = new DHTUDPPacketReplyError(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                                reply.setErrorType(DHTUDPPacketReplyError.ET_KEY_BLOCKED);
                                reply.setKeyBlockDetails(res.getBlockRequest(), res.getBlockSignature());
                                requestReceiveReplyProcessor(originating_contact, reply);
                                packet_handler.send(reply, request.getAddress());
                            } else {
                                DHTUDPPacketReplyStore reply = new DHTUDPPacketReplyStore(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                                reply.setDiversificationTypes(new byte[store_request.getKeys().length]);
                                requestReceiveReplyProcessor(originating_contact, reply);
                                packet_handler.send(reply, request.getAddress());
                            }
                        } else {
                            DHTUDPPacketReplyStore reply = new DHTUDPPacketReplyStore(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                            reply.setDiversificationTypes(res.getDiversificationTypes());
                            requestReceiveReplyProcessor(originating_contact, reply);
                            packet_handler.send(reply, request.getAddress());
                        }
                    }
                } else if (request instanceof DHTUDPPacketRequestFindNode) {
                    DHTUDPPacketRequestFindNode find_request = (DHTUDPPacketRequestFindNode) request;
                    boolean acceptable;
                    if (bootstrap_node) {
                        acceptable = Arrays.equals(find_request.getID(), originating_contact.getID());
                    } else {
                        acceptable = true;
                    }
                    if (acceptable) {
                        DHTTransportContact[] res = request_handler.findNodeRequest(originating_contact, find_request.getID());
                        DHTUDPPacketReplyFindNode reply = new DHTUDPPacketReplyFindNode(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                        reply.setRandomID(originating_contact.getRandomID());
                        reply.setNodeStatus(getNodeStatus());
                        reply.setEstimatedDHTSize(request_handler.getTransportEstimatedDHTSize());
                        reply.setContacts(res);
                        requestReceiveReplyProcessor(originating_contact, reply);
                        packet_handler.send(reply, request.getAddress());
                    }
                } else if (request instanceof DHTUDPPacketRequestFindValue) {
                    if (!bootstrap_node) {
                        DHTUDPPacketRequestFindValue find_request = (DHTUDPPacketRequestFindValue) request;
                        DHTTransportFindValueReply res = request_handler.findValueRequest(originating_contact, find_request.getID(), find_request.getMaximumValues(), find_request.getFlags());
                        if (res.blocked()) {
                            if (originating_contact.getProtocolVersion() >= DHTTransportUDP.PROTOCOL_VERSION_BLOCK_KEYS) {
                                DHTUDPPacketReplyError reply = new DHTUDPPacketReplyError(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                                reply.setErrorType(DHTUDPPacketReplyError.ET_KEY_BLOCKED);
                                reply.setKeyBlockDetails(res.getBlockedKey(), res.getBlockedSignature());
                                requestReceiveReplyProcessor(originating_contact, reply);
                                packet_handler.send(reply, request.getAddress());
                            } else {
                                DHTUDPPacketReplyFindValue reply = new DHTUDPPacketReplyFindValue(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                                reply.setValues(new DHTTransportValue[0], DHT.DT_NONE, false);
                                requestReceiveReplyProcessor(originating_contact, reply);
                                packet_handler.send(reply, request.getAddress());
                            }
                        } else {
                            DHTUDPPacketReplyFindValue reply = new DHTUDPPacketReplyFindValue(this, request.getTransactionId(), request.getConnectionId(), local_contact, originating_contact);
                            if (res.hit()) {
                                DHTTransportValue[] res_values = res.getValues();
                                int max_size = DHTUDPPacketHelper.PACKET_MAX_BYTES - DHTUDPPacketReplyFindValue.DHT_FIND_VALUE_HEADER_SIZE;
                                List values = new ArrayList();
                                int values_size = 0;
                                int pos = 0;
                                while (pos < res_values.length) {
                                    DHTTransportValue v = res_values[pos];
                                    int v_len = v.getValue().length + DHTUDPPacketReplyFindValue.DHT_FIND_VALUE_TV_HEADER_SIZE;
                                    if (values_size > 0 && values_size + v_len > max_size) {
                                        DHTTransportValue[] x = new DHTTransportValue[values.size()];
                                        values.toArray(x);
                                        reply.setValues(x, res.getDiversificationType(), true);
                                        packet_handler.send(reply, request.getAddress());
                                        values_size = 0;
                                        values = new ArrayList();
                                    } else {
                                        values.add(v);
                                        values_size += v_len;
                                        pos++;
                                    }
                                }
                                DHTTransportValue[] x = new DHTTransportValue[values.size()];
                                values.toArray(x);
                                reply.setValues(x, res.getDiversificationType(), false);
                                requestReceiveReplyProcessor(originating_contact, reply);
                                packet_handler.send(reply, request.getAddress());
                            } else {
                                reply.setContacts(res.getContacts());
                                requestReceiveReplyProcessor(originating_contact, reply);
                                packet_handler.send(reply, request.getAddress());
                            }
                        }
                    }
                } else if (request instanceof DHTUDPPacketData) {
                    if (!bootstrap_node) {
                        dataRequest(originating_contact, (DHTUDPPacketData) request);
                    }
                } else {
                    Debug.out("Unexpected packet:" + request.toString());
                }
            }
        } catch (DHTUDPPacketHandlerException e) {
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
    }

    protected void requestReceiveRequestProcessor(DHTTransportUDPContactImpl contact, DHTUDPPacketRequest request) {
    }

    protected void requestReceiveReplyProcessor(DHTTransportUDPContactImpl contact, DHTUDPPacketReply reply) {
        int action = reply.getAction();
        if (action == DHTUDPPacketHelper.ACT_REPLY_PING || action == DHTUDPPacketHelper.ACT_REPLY_FIND_NODE) {
            reply.setNetworkPositions(local_contact.getNetworkPositions());
        }
    }

    protected void requestSendRequestProcessor(DHTTransportUDPContactImpl contact, DHTUDPPacketRequest request) {
    }

    /**
		 * Returns false if this isn't an error reply, true if it is and a retry can be
		 * performed, throws an exception otherwise
		 * @param reply
		 * @return
		 */
    protected void requestSendReplyProcessor(DHTTransportUDPContactImpl remote_contact, DHTTransportReplyHandler handler, DHTUDPPacketReply reply, long elapsed_time) throws DHTUDPPacketHandlerException {
        DHTNetworkPosition[] remote_nps = reply.getNetworkPositions();
        if (remote_nps != null) {
            remote_contact.setNetworkPositions(remote_nps);
            DHTNetworkPositionManager.update(local_contact.getNetworkPositions(), remote_contact.getID(), remote_nps, (float) elapsed_time);
        }
        if (reply.getAction() == DHTUDPPacketHelper.ACT_REPLY_ERROR) {
            DHTUDPPacketReplyError error = (DHTUDPPacketReplyError) reply;
            switch(error.getErrorType()) {
                case DHTUDPPacketReplyError.ET_ORIGINATOR_ADDRESS_WRONG:
                    {
                        try {
                            externalAddressChange(remote_contact, error.getOriginatingAddress());
                        } catch (DHTTransportException e) {
                            Debug.printStackTrace(e);
                        }
                        throw (new DHTUDPPacketHandlerException("address changed notification"));
                    }
                case DHTUDPPacketReplyError.ET_KEY_BLOCKED:
                    {
                        handler.keyBlockRequest(remote_contact, error.getKeyBlockRequest(), error.getKeyBlockSignature());
                        contactAlive(remote_contact);
                        throw (new DHTUDPPacketHandlerException("key blocked"));
                    }
            }
            throw (new DHTUDPPacketHandlerException("unknown error type " + error.getErrorType()));
        } else {
            contactAlive(remote_contact);
        }
    }

    protected long getConnectionID() {
        return (0x8000000000000000L | random.nextLong());
    }

    public boolean supportsStorage() {
        return (!bootstrap_node);
    }

    public void addListener(DHTTransportListener l) {
        listeners.add(l);
        if (external_address != null) {
            l.currentAddress(external_address);
        }
    }

    public void removeListener(DHTTransportListener l) {
        listeners.remove(l);
    }

    protected transferQueue lookupTransferQueue(Map transfers, long id) {
        try {
            this_mon.enter();
            return ((transferQueue) transfers.get(new Long(id)));
        } finally {
            this_mon.exit();
        }
    }

    protected String getMessageText(String resource) {
        return (MessageText.getString("DHTTransport.report." + resource));
    }

    protected String getMessageText(String resource, String param) {
        return (MessageText.getString("DHTTransport.report." + resource, new String[] { param }));
    }

    protected String getMessageText(String resource, String[] params) {
        return (MessageText.getString("DHTTransport.report." + resource, params));
    }

    protected class transferQueue {

        Map transfers;

        long id;

        List packets = new ArrayList();

        AESemaphore packets_sem = new AESemaphore("DHTUDPTransport:transferQueue");

        protected transferQueue(Map _transfers, long _id) throws DHTTransportException {
            transfers = _transfers;
            id = _id;
            try {
                this_mon.enter();
                if (transfers.size() > TRANSFER_QUEUE_MAX) {
                    throw (new DHTTransportException("Transfer queue limit exceeded"));
                }
                transfers.put(new Long(id), this);
            } finally {
                this_mon.exit();
            }
        }

        protected long getID() {
            return (id);
        }

        protected void add(DHTUDPPacketData packet) {
            try {
                this_mon.enter();
                packets.add(packet);
            } finally {
                this_mon.exit();
            }
            packets_sem.release();
        }

        protected DHTUDPPacketData receive(long timeout) {
            if (packets_sem.reserve(timeout)) {
                try {
                    this_mon.enter();
                    return ((DHTUDPPacketData) packets.remove(0));
                } finally {
                    this_mon.exit();
                }
            } else {
                return (null);
            }
        }

        protected void destroy() {
            try {
                this_mon.enter();
                transfers.remove(new Long(id));
            } finally {
                this_mon.exit();
            }
        }
    }

    protected class transferHandlerInterceptor implements DHTTransportTransferHandler {

        private DHTTransportTransferHandler handler;

        protected transferHandlerInterceptor(DHTTransportTransferHandler _handler) {
            handler = _handler;
        }

        public String getName() {
            return (handler.getName());
        }

        public byte[] handleRead(DHTTransportContact originator, byte[] key) {
            return (handler.handleRead(originator, key));
        }

        public byte[] handleWrite(DHTTransportContact originator, byte[] key, byte[] value) {
            HashWrapper key_wrapper = new HashWrapper(key);
            try {
                this_mon.enter();
                Object obj = call_transfers.get(key_wrapper);
                if (obj instanceof AESemaphore) {
                    AESemaphore sem = (AESemaphore) obj;
                    call_transfers.put(key_wrapper, value);
                    sem.release();
                    return (null);
                }
            } finally {
                this_mon.exit();
            }
            return (handler.handleWrite(originator, key, value));
        }
    }
}
