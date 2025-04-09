package uk.ac.warwick.dcs.cokefolk.connections;

import uk.ac.warwick.dcs.cokefolk.SystemUser;
import uk.ac.warwick.dcs.cokefolk.UserManager;
import uk.ac.warwick.dcs.cokefolk.server.ServerConfig;
import uk.ac.warwick.dcs.cokefolk.server.ServerInteraction;
import uk.ac.warwick.dcs.cokefolk.server.SimpleServerInteraction;
import uk.ac.warwick.dcs.cokefolk.server.databaseconnectivity.DatabaseException;
import uk.ac.warwick.dcs.cokefolk.server.databaseconnectivity.SQLPersistence;
import uk.ac.warwick.dcs.cokefolk.ui.ConsoleInteraction;
import uk.ac.warwick.dcs.cokefolk.ui.Interaction;
import uk.ac.warwick.dcs.cokefolk.util.ServerUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

/**
 * Creates the server. Handles requests from clients.
 * @author Rachel, Tom, Sergey
 * @designer Sergey, Rachel
 */
public class MighTyDServer {

    private static final Logger LOG = Logger.getLogger(MighTyDServer.class.getName());

    private Vector<ClientHandler> clients;

    private Semaphore clientConnection = new Semaphore(ServerConfig.getInstance().getIntPref(ServerConfig.MAX_CONNECTIONS), true);

    private ServerSocket serverSocket;

    private ServerThread server;

    private int port;

    private InetAddress localAddress;

    private boolean listening;

    private boolean error = false;

    boolean serverCreated = false;

    private ServerInteraction io;

    String dbUsername;

    String dbPassword;

    /**
   * Create a server and a vector to hold all connected clients in, used for the terminal
   * server
   * @param port
   * The port number of the server
   * @param databaseUsername
   * The username for the underlying database
   * @param databasePassword
   * The password for the underlying database
   */
    public MighTyDServer(final int port, final ServerInteraction io, final String databaseUsername, final String databasePassword) {
        this.port = port;
        this.io = io;
        this.dbUsername = databaseUsername;
        this.dbPassword = databasePassword;
        this.clients = new Vector<ClientHandler>();
        this.listening = false;
        this.localAddress = selectNetworkConnection();
        if (!this.error) bind();
    }

    private InetAddress selectNetworkConnection() {
        InetAddress loopback;
        try {
            loopback = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            loopback = null;
        }
        String desiredConnection = ServerConfig.getInstance().getStringPref(ServerConfig.NETWORK_CONNECTION);
        if (desiredConnection.endsWith("127.0.0.1")) return loopback;
        if (desiredConnection.startsWith("Any ")) return null;
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
            InetAddress[] networks = InetAddress.getAllByName(localhost.getHostName());
            for (InetAddress network : networks) {
                if (desiredConnection.endsWith(network.getHostAddress())) return network;
            }
            for (InetAddress network : networks) {
                String name = null;
                try {
                    NetworkInterface netAdapter = NetworkInterface.getByInetAddress(network);
                    if (netAdapter != null) {
                        name = netAdapter.getDisplayName();
                        if (name == null) {
                            byte[] mac = netAdapter.getHardwareAddress();
                            if (mac != null) {
                                StringBuilder macAddress = new StringBuilder(21);
                                macAddress.append("MAC: ");
                                for (int b : mac) {
                                    if (b < 0) b = 255 + b;
                                    if (b < 16) macAddress.append("0");
                                    macAddress.append(Integer.toHexString(b).toUpperCase());
                                    macAddress.append("-");
                                }
                                macAddress.deleteCharAt(macAddress.length() - 1);
                                name = macAddress.toString();
                            }
                        }
                        if ((name != null) && desiredConnection.startsWith(name)) return network;
                    }
                } catch (SocketException e) {
                }
            }
        } catch (UnknownHostException e) {
            this.io.error("No network card supporting TCP/IP could be detected.  MighTyD requires TCP/IP networking capabilities.");
            return null;
        } catch (SecurityException e) {
            return localhost;
        }
        this.io.error("The configured network connection could not be found.");
        this.error = true;
        return null;
    }

    private void bind() {
        ServerConfig config = ServerConfig.getInstance();
        final int CONNECTION_QUEUE_LEN = Math.min(config.getIntPref(ServerConfig.MAX_CONNECTIONS), 30);
        do {
            this.error = false;
            try {
                if (config.getBoolPref(ServerConfig.USE_SECURE_PROTOCOL)) {
                    SSLContext sslc = initializeSSL();
                    if (sslc != null) {
                        this.serverSocket = sslc.getServerSocketFactory().createServerSocket(this.port, CONNECTION_QUEUE_LEN, this.localAddress);
                    } else {
                        this.io.error("Connections will not be encrypted.");
                        this.serverSocket = new ServerSocket(this.port, CONNECTION_QUEUE_LEN, this.localAddress);
                    }
                } else {
                    this.serverSocket = new ServerSocket(this.port, CONNECTION_QUEUE_LEN, this.localAddress);
                }
                this.serverCreated = true;
            } catch (BindException e) {
                LOG.info(e.toString());
                this.error = true;
                try {
                    InetAddress hostAddress;
                    if (this.localAddress == null) {
                        hostAddress = InetAddress.getLocalHost();
                    } else {
                        hostAddress = this.localAddress;
                    }
                    NetworkInterface netConnection = NetworkInterface.getByInetAddress(hostAddress);
                    if (!netConnection.isUp()) {
                        String adapterName = netConnection.getDisplayName();
                        String msg;
                        if (adapterName != null) msg = adapterName + "  is not functioning correctly."; else msg = "The configured network connection is unavailable.";
                        this.io.error(msg);
                        break;
                    }
                } catch (UnknownHostException e2) {
                    this.io.error("No network card supporting TCP/IP could be detected.  MighTyD requires TCP/IP networking capabilities.");
                    break;
                } catch (SocketException e2) {
                }
                String portText = this.io.input("", "MighTyD server could not listen for client connections on TCP port " + Integer.toString(this.port) + ".  This is due to one of the following reasons:\n" + "1) You have firewall software that is preventing MighTyD from communicating over the network.\n" + "   MighTyD server communicates with the MighTyD client using a network card and\n" + "   requires the ability to listen for incoming connections on one port.\n" + "2) You have already started another copy of the MighTyD server program that is using the same port number.\n" + "3) There is a conflict with another program that also wants to use port " + Integer.toString(this.port) + ".\n" + "   In this case you can change the configuration so that MighTyD uses a different port number.\n" + "4) Network access is currently unavailable because of a problem with the operating system or the networking hardware.\n" + "Enter the number of the preferred TCP port to use to try again or acknowledge this message without entering a number\n" + "to switch to the other instance (in the case of problem 2) or exit otherwise.");
                if (!portText.equals("")) {
                    int newPort = ServerUtils.parsePort(portText, this.io);
                    if (newPort > 0) {
                        this.port = newPort;
                        config.setNumericPref(ServerConfig.SERVER_PORT, newPort);
                        try {
                            config.save(ServerConfig.SERVER_PORT);
                        } catch (BackingStoreException e2) {
                            LOG.info(e2.toString());
                        }
                    } else {
                        this.error = true;
                    }
                } else {
                    Socket socket = null;
                    try {
                        socket = SocketFactory.getDefault().createSocket("127.0.0.1", this.port);
                        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        out.writeUTF("<?xml version=\"1.0\" encoding=\"UTF-8\"?><message><" + Message.USER_INTERFACE + " /></message>");
                        out.flush();
                    } catch (IOException e2) {
                        LOG.warning(e2.toString());
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e2) {
                            LOG.warning(e2.toString());
                        }
                    }
                    System.exit(0);
                }
            } catch (IOException e) {
                LOG.warning(e.toString());
                this.io.error("An I/O error occurred.");
            }
        } while (this.error == true);
    }

    boolean canConnect(final InetAddress client) {
        if (client.isMulticastAddress()) return false;
        if (!(client instanceof Inet4Address)) return (client.isSiteLocalAddress() || client.isLinkLocalAddress());
        byte[] clientBytes = client.getAddress();
        if (clientBytes[3] == 255) return false;
        if (this.localAddress == null) return true;
        if (clientBytes[0] == 10) return true;
        if ((clientBytes[0] == 169) && (clientBytes[1] == 254)) return true;
        if ((clientBytes[0] == 169) && ((clientBytes[1] & 240) == 16)) return true;
        if ((clientBytes[0] == 192) && (clientBytes[1] == 168)) return true;
        byte[] hostBytes = this.localAddress.getAddress();
        if (clientBytes[0] != hostBytes[0]) return false;
        if ((hostBytes[0] > 127) && (clientBytes[1] != hostBytes[1])) return false;
        if ((hostBytes[0] > 191) && (clientBytes[2] != hostBytes[2])) return false;
        return true;
    }

    private SSLContext initializeSSL() {
        boolean errorOccurred = false;
        ServerConfig config = ServerConfig.getInstance();
        String keyStoreFile = config.getStringPref(ServerConfig.KEYSTORE_FILE);
        if (keyStoreFile.equals("")) {
            this.io.error("You need to specify a filename where the encryption keys will be stored, called a key store, before you can use encrypted communications.");
            return null;
        }
        InputStream keyStream;
        try {
            keyStream = new FileInputStream(keyStoreFile);
        } catch (FileNotFoundException e) {
            this.io.error("The key store file needs to exist and contain the encryption key that will be used by the server before you can use encrypted communications.");
            return null;
        } catch (SecurityException e) {
            this.io.error("Permission was denied to access the key store file '" + keyStoreFile + "'.");
            return null;
        }
        KeyStore keyStore;
        String keyStoreType = KeyStore.getDefaultType();
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            LOG.warning(e.toString());
            this.io.error("Unable to create a '" + keyStoreType + "' key store for encrypted communications.");
            return null;
        }
        char[] keyStorePassword = null;
        try {
            if (config.getBoolPref(ServerConfig.VERIFY_KEYSTORE)) {
                keyStorePassword = this.io.secureInput("Encrypted Communications", "Please enter the password for the encryption keys file");
            }
            try {
                keyStore.load(keyStream, keyStorePassword);
            } catch (IOException e) {
                try {
                    keyStream.close();
                } catch (IOException e2) {
                    LOG.warning(e2.toString());
                }
                keyStorePassword = this.io.secureInput("Encrypted Communications", "Please enter the password for the encryption keys file");
                try {
                    keyStream = new FileInputStream(keyStoreFile);
                    keyStore.load(keyStream, keyStorePassword);
                } catch (IOException e2) {
                    errorOccurred = true;
                    LOG.info(e2.toString());
                    this.io.error("An error occurred while reading the key store file '" + keyStoreFile + "' or else the password supplied was incorrect.");
                }
            }
        } catch (CertificateException e) {
            errorOccurred = true;
            LOG.info(e.toString());
            this.io.error("The key store file '" + keyStoreFile + "' contains invalid data.");
        } catch (NoSuchAlgorithmException e) {
            errorOccurred = true;
            LOG.info(e.toString());
            this.io.error("The key store file could not be decryped.");
        } finally {
            if (keyStorePassword != null) Arrays.fill(keyStorePassword, '\0');
            try {
                keyStream.close();
            } catch (IOException e) {
                LOG.warning(e.toString());
            }
            if (errorOccurred) return null;
        }
        KeyManagerFactory kmf = null;
        char[] certificatePassword;
        if (config.prefExists(ServerConfig.CERTIFICATE_PASSWORD)) {
            certificatePassword = config.getStringPref(ServerConfig.CERTIFICATE_PASSWORD).toCharArray();
        } else {
            certificatePassword = this.io.secureInput("Encrypted Communications", "Please enter the password for the server authentication certificate");
        }
        try {
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, certificatePassword);
        } catch (NoSuchAlgorithmException e) {
            LOG.warning(e.toString());
            this.io.error("Unable to initialize encryption key management.");
            errorOccurred = true;
        } catch (UnrecoverableKeyException e) {
            this.io.error("The supplied certificate password was inccorect.");
            errorOccurred = true;
        } catch (KeyStoreException e) {
            LOG.warning(e.toString());
            this.io.error("Unable to initialize encryption key management.");
            errorOccurred = true;
        } finally {
            Arrays.fill(certificatePassword, '\0');
            if (errorOccurred) return null;
        }
        SSLContext sslc;
        String protocol = config.getStringPref(ServerConfig.SECURE_PROTOCOL);
        try {
            sslc = SSLContext.getInstance(protocol);
            if (kmf != null) sslc.init(kmf.getKeyManagers(), null, null);
            return sslc;
        } catch (NoSuchAlgorithmException e) {
            this.io.error("Your configuration specifies to use the '" + protocol + "' protocol but the system does not know how to implement the " + protocol + " protocol.");
            return null;
        } catch (KeyManagementException e) {
            LOG.warning(e.toString());
            this.io.error("Unable to initialize a secure protocol");
            return null;
        }
    }

    /**
   * Starts the server by spawning and starting a new ServerThread
   */
    public boolean startServer() {
        if (!this.listening && this.serverCreated) {
            this.server = new ServerThread();
            this.server.start();
            this.listening = true;
        }
        return this.serverCreated;
    }

    /**
   * Makes sure there exists an admin user and a default database, and if not, creates them
   * @return Whether the initialisation of the database was successful or not
   */
    public boolean init() {
        boolean success = true;
        ensureAdminUser();
        ensureDefaultDatabase();
        return success;
    }

    /**
   * Ensures an admin user exists for the system. If one does not exist, create one.
   */
    private void ensureAdminUser() {
        if (!UserManager.doesUserExist("admin")) {
            UserManager.createDefaultAdminUser();
        }
        SystemUser user = new SystemUser();
        user.setUsername(this.dbUsername);
        user.setDefaultSchema(this.dbUsername);
        user.setDatabaseUsername(this.dbUsername);
        user.setDatabasePassword(this.dbPassword);
        SQLPersistence p;
        try {
            p = SQLPersistence.getInstance(user, this.io);
            p.ensureAdminUser();
        } catch (DatabaseException e) {
        }
    }

    /**
   * Ensures a default database exists for the system. If one does not exist, create one.
   */
    private void ensureDefaultDatabase() {
        SystemUser user = new SystemUser();
        user.setUsername(this.dbUsername);
        user.setDefaultSchema(this.dbUsername);
        user.setDatabaseUsername(this.dbUsername);
        user.setDatabasePassword(this.dbPassword);
        SQLPersistence p;
        try {
            p = SQLPersistence.getInstance(user, this.io);
            p.ensureDefaultDatabase();
        } catch (DatabaseException e) {
        }
    }

    /**
   * Stops the server, by stopping the ServerThread and closing all connected clients
   */
    public boolean stopServer() {
        if (this.listening) {
            boolean portClosed = this.server.stopServerThread();
            this.listening = false;
            Enumeration<ClientHandler> e = this.clients.elements();
            while (e.hasMoreElements()) {
                ClientHandler ch = e.nextElement();
                ch.stopClient();
            }
            this.io.output("The server was stopped.");
            return portClosed;
        } else {
            return true;
        }
    }

    void disconnect(final ClientHandler client) {
        boolean removed = this.clients.removeElement(client);
        if (removed) this.clientConnection.release();
    }

    void connect(final ClientHandler client) {
        this.clients.addElement(client);
    }

    public boolean isErronous() {
        return this.error;
    }

    public boolean isRunning() {
        return this.listening;
    }

    public int getPort() {
        return this.port;
    }

    /**
   * The main method, which is used for the terminal server. Starts the server using the data
   * passed as arguments, using default values if no args given
   * @param argsv
   * The program arguments. [port number] [[database username] database password]
   */
    public static void main(final String[] argsv) {
        ServerConfig config = ServerConfig.getInstance();
        ServerInteraction io = new SimpleServerInteraction(new ConsoleInteraction(config));
        boolean optionsParsed = parseShellOptions(argsv, io, config);
        if (!optionsParsed) {
            io.error("Usage: mightydserver [port] [database username] [database password]");
            System.exit(1);
        }
        MighTyDServer server = new MighTyDServer(config.getIntPref(ServerConfig.SERVER_PORT), io, config.getStringPref(ServerConfig.DATABASE_USERNAME), config.getStringPref(ServerConfig.DATABASE_PASSWORD));
        if (!server.isErronous()) System.out.println(aboutMessage());
        server.init();
        server.startServer();
        if (server.isRunning()) {
            io.output("Type SHUTDOWN <ENTER> to shut down the server.");
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            boolean terminate = false;
            while (!terminate) {
                try {
                    String userInput = stdIn.readLine().trim();
                    if (userInput.equalsIgnoreCase("SHUTDOWN")) terminate = true;
                } catch (IOException e) {
                    System.err.println("An error occurred while reading input.");
                    terminate = true;
                }
            }
            System.out.println("The MighTyD server is shutting down...");
            server.stopServer();
            System.out.println("Done");
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    public static boolean parseShellOptions(final String[] argsv, final Interaction io, final ServerConfig config) {
        String password = null;
        if (config.prefExists(ServerConfig.DATABASE_PASSWORD)) {
            password = config.getStringPref(ServerConfig.DATABASE_PASSWORD);
        }
        int parsedParams = 0;
        if (argsv.length == 0) return true;
        int port;
        try {
            port = Integer.parseInt(argsv[parsedParams]);
            if ((port > 1024) && (port <= 65535)) {
                config.setNumericPref(ServerConfig.SERVER_PORT, port);
                parsedParams++;
            }
        } catch (NumberFormatException e) {
            port = 0;
        }
        if (argsv.length == parsedParams) return true;
        if ((password != null) || (parsedParams + 1 < argsv.length)) {
            config.setStringPref(ServerConfig.DATABASE_USERNAME, argsv[parsedParams]);
            config.setStringPref(ServerConfig.DATABASE_PASSWORD, null);
            parsedParams++;
        } else {
            config.setStringPref(ServerConfig.DATABASE_PASSWORD, argsv[parsedParams]);
            io.clear();
            parsedParams++;
        }
        if (argsv.length == parsedParams) return true;
        config.setStringPref(ServerConfig.DATABASE_PASSWORD, argsv[parsedParams]);
        io.clear();
        parsedParams++;
        if (argsv.length == parsedParams) return true;
        return false;
    }

    public static final String aboutMessage() {
        StringBuilder msg = new StringBuilder();
        msg.append("MighTyD Server ");
        String version = MighTyDServer.class.getPackage().getImplementationVersion();
        if (version != null) msg.append(version);
        msg.append("\n");
        msg.append("Copyright (C) 2006 Rachel Bowers, Adrian Hudnott, Sergey Petrov \n");
        msg.append("Thomas Pick and Issam Souilah \n");
        msg.append("Relvar declarations, U_KEY constraints, U_INSERT and U_DELETE added by Simon Pearson.");
        msg.append("MighTyD Server comes with ABSOLUTELY NO WARRANTY; without even the implied \n");
        msg.append("warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. \n");
        msg.append("This is free software, and you are welcome to redistribute it under certain \n");
        msg.append("conditions; See LICENCE.txt for details. \n");
        return msg.toString();
    }

    /**
   * This inner class listens to incoming connections, and initiates a ClientHandler object for
   * each connection.
   */
    private class ServerThread extends Thread {

        private boolean listen = false;

        @Override
        public void run() {
            this.listen = true;
            try {
                MighTyDServer.this.io.output("The server started up.");
                while (this.listen) {
                    boolean gainedResources = false;
                    try {
                        Socket incoming = MighTyDServer.this.serverSocket.accept();
                        if (incoming instanceof SSLSocket) {
                            ((SSLSocket) incoming).startHandshake();
                        }
                        ClientHandler client = new ClientHandler(MighTyDServer.this, incoming, MighTyDServer.this.io, MighTyDServer.this.dbUsername, MighTyDServer.this.dbPassword);
                        InetAddress sourceIP = incoming.getInetAddress();
                        if (!canConnect(sourceIP)) {
                            MighTyDServer.this.io.output("A connection request was rejected from " + sourceIP.getHostAddress() + " because the server is not accepting connections from that network..");
                            client.kill("Your connection request was rejected because the server does not allow connections from outside its own network.");
                            continue;
                        }
                        incoming.setKeepAlive(true);
                        gainedResources = MighTyDServer.this.clientConnection.tryAcquire(20, TimeUnit.SECONDS);
                        if (gainedResources) {
                            MighTyDServer.this.io.clientConnected(incoming);
                            if (!incoming.getInetAddress().isAnyLocalAddress()) {
                                incoming.setSoTimeout(ServerConfig.getInstance().getIntPref(ServerConfig.IDLE_TIMEOUT) * 60000);
                            } else {
                                incoming.setSoTimeout(Math.max(ServerConfig.getInstance().getIntPref(ServerConfig.IDLE_TIMEOUT), 60) * 60000);
                            }
                            Thread clientThread = new Thread(client);
                            connect(client);
                            clientThread.start();
                        } else {
                            MighTyDServer.this.io.output("A connection request was rejected to protect QoS because the connection limit has been reached.");
                            client.kill("Your connection was rejected because there are too many other users connected to the server.");
                        }
                    } catch (IOException e) {
                        if (this.listen) {
                            if (gainedResources) MighTyDServer.this.clientConnection.release();
                            LOG.info(e.toString());
                            MighTyDServer.this.io.error("A connection request from a client failed.");
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.fine(e.toString());
            } catch (SecurityException e) {
                MighTyDServer.this.io.error("Permission was denied to listen for incoming connections.");
                stopServerThread();
            }
        }

        /**
     * Stops the server thread, and displays a message to this effect either on console of the
     * server management GUI or to the terminal
     */
        public boolean stopServerThread() {
            this.listen = false;
            this.interrupt();
            try {
                MighTyDServer.this.serverSocket.close();
                return true;
            } catch (IOException e) {
                MighTyDServer.this.io.error("The network port could not be closed.");
                return false;
            }
        }
    }
}
