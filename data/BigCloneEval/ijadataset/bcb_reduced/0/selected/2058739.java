package br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import br.unb.unbiquitous.ubiquitos.network.cache.CacheController;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ChannelManager;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManagerListener;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.channelManager.EthernetTCPChannelManager;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetTCPServerConnection;
import br.unb.unbiquitous.ubiquitos.network.exceptions.NetworkException;
import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;

/**
 * Manage the ubiquitos-smartspace service interface.
 *
 * @author Passarinho
 */
public class EthernetTCPConnectionManager extends EthernetConnectionManager {

    /** The ResourceBundle to get some properties. */
    private ResourceBundle resource;

    /** Specify the number of the ethernet port to be used*/
    private static final String UBIQUITOS_ETH_TCP_PORT_KEY = "ubiquitos.eth.tcp.port";

    private static final String UBIQUITOS_ETH_TCP_CONTROL_PORT_KEY = "ubiquitos.eth.tcp.port.control";

    private int UBIQUITOS_ETH_TCP_PORT;

    private int UBIQUITOS_ETH_TCP_CONTROL_PORT;

    /** Specify the passive port range to be used*/
    private static final String UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE_KEY = "ubiquitos.eth.tcp.passivePortRange";

    String UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE;

    /** Object for logging registration.*/
    private static final Logger logger = Logger.getLogger(EthernetTCPConnectionManager.class.getName());

    /** A Connection Manager Listener (ConnectionManagerControlCenter) */
    private ConnectionManagerListener connectionManagerListener = null;

    /** Server Connection */
    private EthernetDevice serverDevice;

    private EthernetTCPServerConnection server;

    /** Attribute to control the closing of the Connection Manager */
    private boolean closingEthernetConnectionManager = false;

    /** The ChannelManager for new channels */
    private EthernetTCPChannelManager channelManager;

    /**
     * Controller responsible for the active connections cache. 
     */
    private CacheController cacheController = new CacheController();

    /**
	 * Constructor
	 * @throws UbiquitOSException
	 */
    public EthernetTCPConnectionManager() throws NetworkException {
    }

    /** 
     *  Sets the Listener who will be notified when a Connections is established.
     */
    public void setConnectionManagerListener(ConnectionManagerListener connectionManagerListener) {
        this.connectionManagerListener = connectionManagerListener;
    }

    /** 
     *  Sets the ResourceBundle to get some properties.
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        resource = resourceBundle;
        if (resource == null) {
            String msg = "ResourceBundle is null";
            logger.fatal(msg);
            throw new RuntimeException(msg);
        } else {
            try {
                UBIQUITOS_ETH_TCP_PORT = Integer.parseInt(resource.getString(UBIQUITOS_ETH_TCP_PORT_KEY));
                try {
                    UBIQUITOS_ETH_TCP_CONTROL_PORT = Integer.parseInt(resource.getString(UBIQUITOS_ETH_TCP_CONTROL_PORT_KEY));
                } catch (Exception e) {
                    logger.info("No Alternative TCP Port defined");
                }
                UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE = resource.getString(UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE_KEY);
            } catch (Exception e) {
                String msg = "Incorrect ethernet tcp port";
                logger.fatal(msg);
                throw new RuntimeException(msg);
            }
        }
    }

    /**
	 * Finalize the Connection Manager.
	 */
    public void tearDown() {
        try {
            closingEthernetConnectionManager = true;
            logger.debug("Closing Ethernet TCP Connection Manager...");
            server.closeConnection();
            if (channelManager != null) {
                channelManager.tearDown();
            }
            logger.debug("Ethernet TCP Connection Manager is closed.");
        } catch (Exception ex) {
            closingEthernetConnectionManager = false;
            String msg = "Error closing Ethernet TCP Connection Manager. ";
            logger.fatal(msg, ex);
            throw new RuntimeException(msg + ex);
        }
    }

    /**
	 * A method for retrieve the networkDevice of this connection.
	 * @return networkDevice
	 */
    public NetworkDevice getNetworkDevice() {
        if (serverDevice == null) {
            try {
                Enumeration<NetworkInterface> e1 = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
                while (e1.hasMoreElements()) {
                    NetworkInterface ni = e1.nextElement();
                    if (!ni.isLoopback() && !ni.isVirtual() && ni.isUp()) {
                        Enumeration<InetAddress> e2 = ni.getInetAddresses();
                        String addr = null;
                        while (e2.hasMoreElements()) {
                            InetAddress ia = e2.nextElement();
                            if (!ia.isLoopbackAddress() && !ia.isAnyLocalAddress() && !ia.isMulticastAddress()) {
                                if (!ia.toString().contains(":")) {
                                    addr = ia.toString();
                                }
                            }
                        }
                        if (addr != null) {
                            serverDevice = new EthernetDevice(addr.substring(1), UBIQUITOS_ETH_TCP_PORT, EthernetConnectionType.TCP);
                            return serverDevice;
                        }
                    }
                }
            } catch (SocketException e) {
                logger.error(e);
            }
        }
        return serverDevice;
    }

    /**
	 * A method for retrive the channel manager of this connection manager
	 * @return channel managar
	 */
    public ChannelManager getChannelManager() {
        if (channelManager == null) {
            channelManager = new EthernetTCPChannelManager(UBIQUITOS_ETH_TCP_PORT, UBIQUITOS_ETH_TCP_CONTROL_PORT, UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE, cacheController);
        }
        return channelManager;
    }

    /**
	 * Method extends from Runnable. Starts the connection Manager
	 */
    public void run() {
        logger.debug("Starting UbiquitOS Smart-Space Ethernet TCP Connection Manager.");
        logger.info("Starting Ethernet TCP Connection Manager...");
        int tries = 0;
        int max_retries = 5;
        long waiting_time = 300;
        IOException ex = null;
        while (tries < max_retries) {
            try {
                server = new EthernetTCPServerConnection((EthernetDevice) getNetworkDevice(), cacheController);
                tries = max_retries;
            } catch (IOException e) {
                String msg = "Error starting Ethernet TCP Connection Manager : " + e.getMessage();
                logger.fatal(msg);
                ex = e;
                tries++;
                try {
                    Thread.sleep(waiting_time);
                } catch (InterruptedException e1) {
                    logger.error(e1);
                }
            }
        }
        if (server == null) {
            String msg = "Error starting Ethernet TCP Connection Manager. ";
            logger.fatal(msg, ex);
            throw new RuntimeException(msg, ex);
        }
        logger.info("Ethernet TCP Connection Manager is started.");
        while (true) {
            try {
                connectionManagerListener.handleClientConnection(server.accept());
            } catch (IOException e) {
                if (!closingEthernetConnectionManager) {
                    String msg = "Error starting Ethernet TCP Connection Manager. ";
                    logger.fatal(msg, e);
                    throw new RuntimeException(msg + e);
                } else {
                    return;
                }
            }
        }
    }
}
