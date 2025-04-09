package ranab.server.ftp;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.StringTokenizer;
import ranab.io.LogFile;
import ranab.server.IpRestrictor;
import ranab.server.ftp.usermanager.PropertiesUserManager;
import ranab.server.ftp.usermanager.UserManager;
import ranab.util.AsyncMessageQueue;
import ranab.util.BaseProperties;

/**
 * Ftp configuration class. It has all ftp server configuration 
 * parameters. This is not hot-editable. Parameters will be loaded 
 * once during server startup. We can add our own config parameters.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class FtpConfig extends BaseProperties {

    /**
     * Config properties prefix.
     */
    public static final String PREFIX = "FtpServer.server.config.";

    private static final String LOG_FILE = "ftp.log";

    private static final String IP_PROP = "ip.properties";

    private File mCfgFile = null;

    private LogFile mFtpLog = null;

    private FtpStatus mStatus = null;

    private ConnectionService mConService = null;

    private IpRestrictor mIpRestrictor = null;

    private UserManager mUserManager = null;

    private InetAddress mServerAddress = null;

    private InetAddress mSelfAddress = null;

    private FtpStatistics mStatistics = null;

    private int miServerPort;

    private int miDataPort[][];

    private int miMaxLogin;

    private int miAnonLogin;

    private int miPollInterval;

    private int miDefaultIdle;

    private int miLogLevel;

    private long mlLogMaxSize;

    private boolean mbLogFlush;

    private boolean mbAnonAllowed;

    private boolean mbAllowIp;

    private boolean mbCreateHome;

    private File mDataDir;

    private File mDefaultRoot;

    private AsyncMessageQueue mQueue;

    /**
     * Constructor - read the configuration file.
     */
    public FtpConfig(File cfgFile) throws Exception {
        super(cfgFile);
        mCfgFile = cfgFile;
        mServerAddress = getInetAddress(PREFIX + "server.host", null);
        mSelfAddress = getInetAddress(PREFIX + "self.host", null);
        miServerPort = getInteger(PREFIX + "port", 21);
        miMaxLogin = getInteger(PREFIX + "login", 20);
        mbAnonAllowed = getBoolean(PREFIX + "anonymous", true);
        miAnonLogin = getInteger(PREFIX + "anonymous.login", 10);
        miPollInterval = getInteger(PREFIX + "poll.interval", 60);
        mlLogMaxSize = getLong(PREFIX + "log.size", 1024) * 1024;
        mbLogFlush = getBoolean(PREFIX + "log.flush", false);
        miLogLevel = getInteger(PREFIX + "log.level", 1);
        mDefaultRoot = getFile(PREFIX + "root.dir", new File("/"));
        miDefaultIdle = getInteger(PREFIX + "idle.time", 300);
        mDataDir = getFile(PREFIX + "data", new File("./data"));
        mbAllowIp = getBoolean(PREFIX + "ip.allow", false);
        mbCreateHome = getBoolean(PREFIX + "home.create", false);
        String s = getString(PREFIX + "data.port.pool", "0");
        StringTokenizer st = new StringTokenizer(s, ", \t\n\r\f");
        miDataPort = new int[st.countTokens()][2];
        for (int i = 0; i < miDataPort.length; i++) {
            miDataPort[i][0] = Integer.parseInt(st.nextToken());
            miDataPort[i][1] = 0;
        }
        if (mSelfAddress == null) {
            mSelfAddress = InetAddress.getLocalHost();
        }
        if (mServerAddress == null) {
            mServerAddress = mSelfAddress;
        }
        File logDir = new File(mDataDir, "log");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        mFtpLog = new LogFile(new File(logDir, LOG_FILE));
        mFtpLog.setMaxSize(mlLogMaxSize);
        mFtpLog.setAutoFlush(mbLogFlush);
        mFtpLog.setLogLevel(miLogLevel);
        File ipDat = new File(mDataDir, IP_PROP);
        if (!ipDat.exists()) {
            ipDat.createNewFile();
        }
        mIpRestrictor = new IpRestrictor(ipDat, mbAllowIp);
        Class managerClass = getClass(PREFIX + "user.manager", PropertiesUserManager.class);
        Constructor cons = managerClass.getConstructor(new Class[] { getClass() });
        mUserManager = (UserManager) cons.newInstance(new Object[] { this });
        mStatistics = new FtpStatistics(this);
        mConService = new ConnectionService(this);
        mStatus = new FtpStatus();
        mQueue = new AsyncMessageQueue();
        mQueue.setMaxSize(2048);
        mFtpLog.info("Configuration loaded " + mCfgFile.getAbsolutePath());
    }

    /**
     * Get data port. Data port number zero (0) means that 
     * any available port will be used.
     */
    public int getDataPort() {
        synchronized (miDataPort) {
            int dataPort = -1;
            int loopTimes = 2;
            Thread currThread = Thread.currentThread();
            while ((dataPort == -1) && (--loopTimes >= 0) && (!currThread.isInterrupted())) {
                for (int i = 0; i < miDataPort.length; i++) {
                    if (miDataPort[i][1] == 0) {
                        if (miDataPort[i][0] != 0) {
                            miDataPort[i][1] = 1;
                        }
                        dataPort = miDataPort[i][0];
                        break;
                    }
                }
                if (dataPort == -1) {
                    try {
                        miDataPort.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
            return dataPort;
        }
    }

    /**
     * Release data port
     */
    public void releaseDataPort(int port) {
        synchronized (miDataPort) {
            for (int i = 0; i < miDataPort.length; i++) {
                if (miDataPort[i][0] == port) {
                    miDataPort[i][1] = 0;
                    break;
                }
            }
            miDataPort.notify();
        }
    }

    /**
     * Get config file.
     */
    public File getConfigFile() {
        return mCfgFile;
    }

    /**
     * Get server port.
     */
    public int getServerPort() {
        return miServerPort;
    }

    /**
     * Get server bind address.
     */
    public InetAddress getServerAddress() {
        return mServerAddress;
    }

    /**
     * Get self address
     */
    public InetAddress getSelfAddress() {
        return mSelfAddress;
    }

    /**
     * Check annonymous login support.
     */
    public boolean isAnonymousLoginAllowed() {
        return mbAnonAllowed;
    }

    /**
     * Get resource directory.
     */
    public File getDataDir() {
        return mDataDir;
    }

    /**
     * Allow Ip
     */
    public boolean isAllowIp() {
        return mbAllowIp;
    }

    /**
     * Get ftp status resource.
     */
    public FtpStatus getStatus() {
        return mStatus;
    }

    /**
     * Get connection service.
     */
    public ConnectionService getConnectionService() {
        return mConService;
    }

    /**
     * Get user manager.
     */
    public UserManager getUserManager() {
        return mUserManager;
    }

    /**
     * Get maximum number of connections.
     */
    public int getMaxConnections() {
        return miMaxLogin;
    }

    /**
     * Get maximum number of anonymous connections.
     */
    public int getMaxAnonymousLogins() {
        if (!isAnonymousLoginAllowed()) {
            return 0;
        }
        return miAnonLogin;
    }

    /**
     * Get poll interval in seconds.
     */
    public int getSchedulerInterval() {
        return miPollInterval;
    }

    /**
     * Get default idle time in seconds.
     */
    public int getDefaultIdleTime() {
        return miDefaultIdle;
    }

    /**
     * Get default root directory
     */
    public File getDefaultRoot() {
        return mDefaultRoot;
    }

    /**
     * Create user home directory if not exist during login
     */
    public boolean isCreateHome() {
        return mbCreateHome;
    }

    /**
     * Get ftp log to write log entries.
     */
    public LogFile getLogger() {
        return mFtpLog;
    }

    /**
     * Get IP restrictor object.
     */
    public IpRestrictor getIpRestrictor() {
        return mIpRestrictor;
    }

    /**
     * Get global statistics object.
     */
    public FtpStatistics getStatistics() {
        return mStatistics;
    }

    /**
     * Get message queue
     */
    public AsyncMessageQueue getMessageQueue() {
        return mQueue;
    }

    /**
     * Get the system name.
     */
    public String getSystemName() {
        String systemName = System.getProperty("os.name");
        if (systemName == null) {
            systemName = "UNKNOWN";
        } else {
            systemName = systemName.toUpperCase();
            systemName = systemName.replace(' ', '-');
        }
        return systemName;
    }

    /**
     * Close this config and all the related resources. Ftp server
     * <code>FtpServer.dispose()</code> method will call this method.
     */
    public void dispose() {
        if (mConService != null) {
            mFtpLog.info("Closing connection service.");
            mConService.dispose();
            mConService = null;
        }
        if (mUserManager != null) {
            mFtpLog.info("Closing user manager.");
            mUserManager.dispose();
            mUserManager = null;
        }
        if (mQueue != null) {
            mFtpLog.info("Closing message queue.");
            mQueue.dispose();
            mQueue = null;
        }
        if (mFtpLog != null) {
            mFtpLog.info("Closing log file.");
            mFtpLog.info("======================================================================");
            mFtpLog.dispose();
            mFtpLog = null;
        }
    }
}
