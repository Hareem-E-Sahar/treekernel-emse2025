package uk.ac.warwick.dcs.cokefolk.server;

import uk.ac.warwick.dcs.cokefolk.ui.ConsoleInteraction;
import uk.ac.warwick.dcs.cokefolk.ui.SystemSounds;
import uk.ac.warwick.dcs.cokefolk.util.Config;
import uk.ac.warwick.dcs.cokefolk.util.ServerUtils;
import java.awt.GraphicsEnvironment;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.swing.JOptionPane;

/**
 * Contains the configurable parameters for the server. The current implementation stores them
 * using the Java Preferences API.
 * @see Config
 * @author Adrian
 * @designer Adrian
 */
public final class ServerConfig extends Config {

    private static final Logger LOG = Logger.getLogger(ServerConfig.class.getName());

    /**
   * If this setting is true then the server will start as soon as the program loads and the
   * main window won't be displayed. The program will still be accesible from the system tray.
   */
    public static final String RUN_DISCREET = "general.run_discreet";

    /**
   * If this setting is true then the code executes extra state consistency checks during
   * execution. If it is false then these checks are disabled, giving some extra performance.
   */
    public static final String EXTRA_ASSERTIONS = "advanced.extra_assertions";

    /**
   * If debugging is enabled then a stack trace will be displayed if an unhandled exception
   * occurs. If debugging is disabled then a standard message will be displayed.
   * <p>
   * Default: false (debugging off)
   * </p>
   */
    public static final String DEBUG = "advanced.debug";

    /**
   * If this setting is true then <b>Tutorial D</b> variables will be initialized to some
   * constant value, which is documented in their classes if no explicit initialization is
   * provided. These defaults are subject to change in future program versions if desired. If
   * this is false then initial values will be chosen randomly. Defaults to false but will
   * usually be overriden in the configuration file to be true in the release version of the
   * program. However, it should be left as false during program testing to detect bugs caused
   * by assuming the presence of the currently defined default values.
   */
    public static final String DETERMINISTIC_INITIALIZERS = "advanced.deterministic_initializers";

    /**
   * If this setting is false then all interaction with database storage is disabled. No
   * relation variables will be defined at startup except TABLE_DEE, TABLE_DUM and Catalog. Any
   * relation variables defined during program execution will be lost when the program
   * terminates. This parameter defaults to true, that is, database functionality is enabled.
   * Setting it to false is useful for demonstrating the software without the hassle of
   * configuring a DBMS.
   */
    public static final String DATABASE_ENABLED = "database.enabled";

    /**
   * This setting controls how many tuples of any partial result are displayed when a
   * computation that normally results in a relation value encounters an error.
   * <p>
   * Default: 5 tuples to demonstrate the cause of the error
   * </p>
   */
    public static final String ABBREVIATED_RELATION_LENGTH = "operations.abbreviated_relation_length";

    /**
   * This setting gives the DNS name or IP address to the computer running the DBMS software.
   * The default value is the local machine.
   */
    public static final String DATABASE_HOST = "database.host";

    /**
   * This parameter gives the TCP port that the DBMS software uses to accept connections. There
   * is no default value.
   */
    public static final String DATABASE_PORT = "database.port";

    /**
   * This setting gives the default (initial) username passed to the DBMS software. There is no
   * default value (the persistence layer prompts for a username).
   */
    public static final String DATABASE_USERNAME = "database.username";

    /**
   * This setting gives the default (initial) password passed to the DBMS software. There is no
   * default value (the persistence layer prompts for a password).
   */
    public static final String DATABASE_PASSWORD = "database.password";

    /**
   * This setting gives the class name for the JDBC driver used to interface with the DBMS
   * software.
   * <p>
   * Default: PostgreSQL JDBC driver
   * </p>
   */
    public static final String DATABASE_CLASS = "database.class";

    /**
   * This setting gives the protocol name for connecting to the JDBC driver.
   * <p>
   * Default: postgresql
   * </p>
   */
    public static final String DATABASE_PROTOCOL = "database.protocol";

    /**
   * This setting gives the URL for connecting to the JDBC driver (excluding the protocol
   * name). This setting can contain the following special codes:
   * <dl>
   * <dt>%h</dt>
   * <dd>The name of the database host machine</dd>
   * <dt>%d</dt>
   * <dd>The name of the database (schema) to access</dd>
   * <dt>% </dt>
   * <dd>The literal % symbol</dd>
   * </dl>
   */
    public static final String DATABASE_URL = "database.url";

    /**
   * This setting gives the command line to start the database software on demand if required.
   * <p>
   * Default: Attempts to start PostgreSQL on Windows or Unix
   * </p>
   */
    public static final String DATABASE_EXECUTABLE = "database.executable";

    /**
   * This setting enables or disables encrypted communications.
   * <p>
   * Default: No encryption
   * </p>
   */
    public static final String USE_SECURE_PROTOCOL = "connections.usesecureprotocol";

    /**
   * This setting determines which secure communications protocol to use if secured
   * communications are enabled.
   * <p>
   * Default: Transport Layer Security (TLS, any available version)
   * </p>
   */
    public static final String SECURE_PROTOCOL = "connections.secureprotocol";

    /**
   * This setting determines which network connection is used, if the machine has multiple
   * network connections.
   * <p>
   * Default: Any
   * </p>
   */
    public static final String NETWORK_CONNECTION = "connections.adapter";

    /**
   * This setting selects the TCP port that the MighTyD server uses to listen for incoming
   * connections from clients.
   * <p>
   * Default: 1235
   * <p>
   */
    public static final String SERVER_PORT = "connections.port";

    /**
   * This setting contains the filename used for storing the server identification certificate
   * for secure communications.
   * <p>
   * Default: None (must be set from the configuration dialog prior to use of secure
   * communications)
   * </p>
   */
    public static final String KEYSTORE_FILE = "connections.keystore";

    /**
   * This setting causes any client that is connected from another computer to be disconnected
   * automatically if it doesn't send any commands within a selected number of minutes.
   * <p>
   * Default: 10 minutes
   * </p>
   */
    public static final String IDLE_TIMEOUT = "connections.idletimeout";

    /**
   * This setting limits the number of clients that can be connected to the server at any one
   * time.
   * <p>
   * Default: 20 clients. This could be increased in the future if good performance testing
   * results are achieved.
   * </p>
   */
    public static final String MAX_CONNECTIONS = "connections.maxclients";

    /**
   * If this setting is set to "true" then the integrity of the server authentication
   * certificates and encryption keys will be checked.
   * <p>
   * Default: false
   * </p>
   */
    public static final String VERIFY_KEYSTORE = "connections.verifykeystore";

    /**
   * This setting contains the password used to access the server's identification certificate
   * and encryption key for secure communications.
   * <p>
   * Default: None (prompt for the password when required)
   * </p>
   */
    public static final String CERTIFICATE_PASSWORD = "connections.certificatepassword";

    private static final ServerConfig THE_INSTANCE;

    static {
        String os = ServerUtils.getOS();
        THE_INSTANCE = new ServerConfig();
        THE_INSTANCE.provideDefault(RUN_DISCREET, false);
        THE_INSTANCE.provideDefault(EXTRA_ASSERTIONS, true);
        THE_INSTANCE.provideDefault(DEBUG, false);
        THE_INSTANCE.provideDefault(DETERMINISTIC_INITIALIZERS, true);
        THE_INSTANCE.provideDefault(ABBREVIATED_RELATION_LENGTH, 5);
        THE_INSTANCE.provideDefault(DATABASE_ENABLED, true);
        THE_INSTANCE.provideDefault(DATABASE_HOST, "127.0.0.1");
        THE_INSTANCE.provideDefault(DATABASE_CLASS, "org.postgresql.Driver");
        THE_INSTANCE.provideDefault(DATABASE_PROTOCOL, "postgresql");
        THE_INSTANCE.provideDefault(DATABASE_URL, "//%h/%d");
        if ((os != null) && os.contains("windows")) {
            THE_INSTANCE.init();
            if (THE_INSTANCE.getStringPref(DATABASE_EXECUTABLE).equals("")) {
                StringBuilder exec = new StringBuilder("net start \"PostgreSQL Database Server");
                String version = null;
                if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
                    version = new ConsoleInteraction(THE_INSTANCE).input("MighTyD: Server Configuration", "PostgreSQL version number?");
                } else {
                    SystemSounds.defaultSound();
                    Object versionObj = JOptionPane.showInputDialog(null, "PostgreSQL version number?", "MighTyD: Server Configuration", JOptionPane.INFORMATION_MESSAGE, null, null, "8.4");
                    if (versionObj != null) version = versionObj.toString();
                }
                if ((version != null) && !version.equals("")) {
                    exec.append(" ");
                    exec.append(version);
                }
                exec.append("\"");
                String execStr = exec.toString();
                THE_INSTANCE.provideDefault(DATABASE_EXECUTABLE, execStr);
                THE_INSTANCE.setStringPref(DATABASE_EXECUTABLE, execStr);
                try {
                    THE_INSTANCE.save(DATABASE_EXECUTABLE);
                } catch (BackingStoreException e) {
                    LOG.warning(e.toString());
                }
            }
        } else {
            THE_INSTANCE.provideDefault(DATABASE_EXECUTABLE, "su -c postmaster postgres");
        }
        THE_INSTANCE.provideDefault(NETWORK_CONNECTION, "This machine only /127.0.0.1");
        THE_INSTANCE.provideDefault(SERVER_PORT, 1235);
        THE_INSTANCE.provideDefault(IDLE_TIMEOUT, 10);
        THE_INSTANCE.provideDefault(MAX_CONNECTIONS, 20);
        THE_INSTANCE.provideDefault(USE_SECURE_PROTOCOL, false);
        THE_INSTANCE.provideDefault(SECURE_PROTOCOL, "TLS");
        THE_INSTANCE.provideDefault(VERIFY_KEYSTORE, false);
    }

    @Override
    public List<MetaInformation> availableOptions() {
        List<MetaInformation> result = new LinkedList<MetaInformation>();
        result.add(new MetaInformation(Boolean.class, RUN_DISCREET, "&Run without opening the main window"));
        result.add(new MetaInformation(String.class, NETWORK_CONNECTION, "&Clients can connect from", getNetworkConnections()));
        result.add(new MetaInformation(Integer.class, SERVER_PORT, "&TCP Port"));
        result.add(new MetaInformation(Integer.class, MAX_CONNECTIONS, "&Maximum number of connected clients"));
        result.add(new MetaInformation(Integer.class, IDLE_TIMEOUT, "&Disconnect idle clients after (minutes)"));
        result.add(new MetaInformation(Boolean.class, USE_SECURE_PROTOCOL, "&Use a secure protocol"));
        result.add(new MetaInformation(String.class, SECURE_PROTOCOL, "&Secure protocol", 5, false, null));
        result.add(new MetaInformation(String.class, KEYSTORE_FILE, "&Key store", 20, false, null));
        result.add(new MetaInformation(Boolean.class, VERIFY_KEYSTORE, "&Verify key store integrity"));
        result.add(new MetaInformation(String.class, CERTIFICATE_PASSWORD, "Certificate &password", 16, true, null));
        result.add(new MetaInformation(Boolean.class, DATABASE_ENABLED, "&Enable database access"));
        result.add(new MetaInformation(String.class, DATABASE_PROTOCOL, "&Database system"));
        result.add(new MetaInformation(String.class, DATABASE_HOST, "Database &server"));
        result.add(new MetaInformation(String.class, DATABASE_USERNAME, "Initial database &username"));
        result.add(new MetaInformation(String.class, DATABASE_PASSWORD, "Initial database &password", 16, true, null));
        result.add(new MetaInformation(Integer.class, DATABASE_PORT, "Database server &TCP port"));
        result.add(new MetaInformation(String.class, DATABASE_CLASS, "Database d&river", 21, false, null));
        result.add(new MetaInformation(String.class, DATABASE_URL, "Connection URL &format"));
        result.add(new MetaInformation(String.class, DATABASE_EXECUTABLE, "Startup &command", 28, false, null));
        result.add(new MetaInformation(Boolean.class, EXTRA_ASSERTIONS, "&Enable software integrity checks"));
        result.add(new MetaInformation(Boolean.class, DEBUG, "Display &fault tracking information"));
        result.add(new MetaInformation(Boolean.class, DETERMINISTIC_INITIALIZERS, "&Deterministic default variable values"));
        return result;
    }

    private static Object[] getNetworkConnections() {
        ArrayList<String> netConnections = new ArrayList<String>();
        netConnections.add("This machine only /127.0.0.1");
        netConnections.add("Any Connected Network (including Internet)");
        try {
            InetAddress[] networks = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            int unknownConnections = 0;
            for (InetAddress network : networks) {
                if (network.isLoopbackAddress()) continue;
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
                    }
                } catch (SocketException e) {
                }
                if (name == null) {
                    unknownConnections++;
                    name = "Unknown Network Card " + Integer.toString(unknownConnections);
                }
                name = name + " /" + network.getHostAddress();
                netConnections.add(name);
            }
        } catch (UnknownHostException e) {
            if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
                System.err.println("No network card supporting TCP/IP could be detected.  MighTyD requires TCP/IP networking capabilities.");
            } else {
                SystemSounds.warning();
                JOptionPane.showMessageDialog(null, "No network card supporting TCP/IP could be detected.  MighTyD requires TCP/IP networking capabilities.", "MighTyD Server", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SecurityException e) {
        }
        return netConnections.toArray(new String[0]);
    }

    /**
   * Gets a reference to the server configuration parameters.
   * @return a reference to the server configuration parameters.
   */
    public static ServerConfig getInstance() {
        THE_INSTANCE.init();
        return THE_INSTANCE;
    }

    private ServerConfig() {
        super("mightyd", "server");
    }
}
