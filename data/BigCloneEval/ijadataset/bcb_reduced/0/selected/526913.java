package gate;

import gate.config.ConfigDataProcessor;
import gate.creole.CreoleRegisterImpl;
import gate.creole.ResourceData;
import gate.creole.metadata.CreoleResource;
import gate.event.CreoleListener;
import gate.gui.creole.manager.PluginUpdateManager;
import gate.util.Benchmark;
import gate.util.Files;
import gate.util.GateClassLoader;
import gate.util.GateException;
import gate.util.GateRuntimeException;
import gate.util.LuckyException;
import gate.util.OptionsMap;
import gate.util.Strings;
import gate.util.asm.AnnotationVisitor;
import gate.util.asm.ClassReader;
import gate.util.asm.ClassVisitor;
import gate.util.asm.Opcodes;
import gate.util.asm.Type;
import gate.util.asm.commons.EmptyVisitor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * The class is responsible for initialising the GATE libraries, and providing
 * access to singleton utility objects, such as the GATE class loader, CREOLE
 * register and so on.
 */
public class Gate implements GateConstants {

    /** A logger to use instead of sending messages to Out or Err **/
    protected static final Logger log = Logger.getLogger(Gate.class);

    /**
   * The default StringBuffer size, it seems that we need longer string than the
   * StringBuffer class default because of the high number of buffer expansions
   */
    public static final int STRINGBUFFER_SIZE = 1024;

    /**
   * The default size to be used for Hashtable, HashMap and HashSet. The defualt
   * is 11 and it leads to big memory usage. Having a default load factor of
   * 0.75, table of size 4 can take 3 elements before being re-hashed - a values
   * that seems to be optimal for most of the cases.
   */
    public static final int HASH_STH_SIZE = 4;

    /** The list of builtin URLs to search for CREOLE resources. */
    private static String builtinCreoleDirectoryUrls[] = {};

    /** The GATE URI used to interpret custom GATE tags */
    public static final String URI = "http://www.gate.ac.uk";

    /** Minimum version of JDK we support */
    protected static final String MIN_JDK_VERSION = "1.5";

    /**
   * Feature name that should be used to set if the benchmarking logging should
   * be enabled or disabled.
   */
    protected static final String ENABLE_BENCHMARKING_FEATURE_NAME = "gate.enable.benchmark";

    /** Is true if GATE is to be run in a sandbox **/
    private static boolean sandboxed = false;

    /**
   * Find out if GATE is to be run in a sandbox or not. If true then
   * GATE will not attempt to load any local configuration information during
   * Initialisation making it possible to use GATE from within unsigned
   * applets and web start applications.
   * @return true if GATE is to be run in a sandbox, false otherwise
   */
    public static boolean isSandboxed() {
        return sandboxed;
    }

    /**
   * Method to tell GATE if it is being run inside a JVM sandbox. If true then
   * GATE will not attempt to load any local configuration information during
   * Initialisation making it possible to use GATE from within unsigned
   * applets and web start applications.
   * @param sandboxed true if GATE is to be run in a sandbox, false otherwise
   */
    public static void runInSandbox(boolean sandboxed) {
        if (initFinished) throw new IllegalStateException("Sandbox status cannot be changed after GATE has been initialised!");
        Gate.sandboxed = sandboxed;
    }

    /** Get the minimum supported version of the JDK */
    public static String getMinJdkVersion() {
        return MIN_JDK_VERSION;
    }

    /**
   * Initialisation - must be called by all clients before using any other parts
   * of the library. Also initialises the CREOLE register and reads config data (<TT>gate.xml</TT>
   * files).
   *
   * @see #initCreoleRegister
   */
    public static void init() throws GateException {
        if (!sandboxed) initLocalPaths();
        if (System.getProperty(ENABLE_BENCHMARKING_FEATURE_NAME) != null && System.getProperty(ENABLE_BENCHMARKING_FEATURE_NAME).equalsIgnoreCase("true")) {
            Benchmark.setBenchmarkingEnabled(true);
        }
        if (builtinCreoleDir == null) {
            String builtinCreoleDirPropertyValue = System.getProperty(BUILTIN_CREOLE_DIR_PROPERTY_NAME);
            if (builtinCreoleDirPropertyValue == null) {
                builtinCreoleDir = Files.getGateResource("/creole/");
            } else {
                String builtinCreoleDirPath = builtinCreoleDirPropertyValue;
                if (!builtinCreoleDirPath.endsWith("/")) {
                    builtinCreoleDirPath += "/";
                }
                try {
                    builtinCreoleDir = new URL(builtinCreoleDirPath);
                } catch (MalformedURLException mue) {
                    throw new GateRuntimeException(BUILTIN_CREOLE_DIR_PROPERTY_NAME + " value \"" + builtinCreoleDirPropertyValue + "\" could" + " not be parsed as either a URL or a file path.");
                }
                log.info("Using " + builtinCreoleDir + " as built-in CREOLE" + " directory URL");
            }
        }
        System.setProperty("java.protocol.handler.pkgs", System.getProperty("java.protocol.handler.pkgs") + "|" + "gate.util.protocols");
        lastSym = 0;
        if (classLoader == null) classLoader = new GateClassLoader("Top-Level GATE ClassLoader", Gate.class.getClassLoader());
        if (creoleRegister == null) creoleRegister = new CreoleRegisterImpl();
        if (knownPlugins == null) knownPlugins = new ArrayList<URL>();
        if (autoloadPlugins == null) autoloadPlugins = new ArrayList<URL>();
        if (pluginData == null) pluginData = new HashMap<URL, DirectoryInfo>();
        initCreoleRegister();
        initDataStoreRegister();
        if (!sandboxed) initConfigData();
        if (!sandboxed) initCreoleRepositories();
        dataStoreRegister.addCreoleListener(creoleRegister);
        Factory.addCreoleListener(creoleRegister);
        if (System.getProperty("java.version").compareTo(MIN_JDK_VERSION) < 0) {
            throw new GateException("GATE requires JDK " + MIN_JDK_VERSION + " or newer");
        }
        try {
            registerIREngine("gate.creole.ir.lucene.LuceneIREngine");
        } catch (ClassNotFoundException cnfe) {
            throw new GateRuntimeException(cnfe);
        }
        initFinished = true;
    }

    /** Have we successfully run {@link #init()} before? */
    public static boolean isInitialised() {
        return initFinished;
    }

    /** Records initialisation status. */
    protected static boolean initFinished = false;

    /**
   * Initialises the paths to local files of interest like the GATE home, the
   * installed plugins home and site and user configuration files.
   */
    protected static void initLocalPaths() {
        if (gateHome == null) {
            String gateHomeStr = System.getProperty(GATE_HOME_PROPERTY_NAME);
            if (gateHomeStr != null && gateHomeStr.length() > 0) {
                gateHome = new File(gateHomeStr);
            }
            if (gateHome == null || !gateHome.exists()) {
                log.warn("GATE home system property (\"" + GATE_HOME_PROPERTY_NAME + "\") not set.\nAttempting to guess...");
                URL gateURL = Thread.currentThread().getContextClassLoader().getResource("gate/Gate.class");
                try {
                    if (gateURL.getProtocol().equals("jar")) {
                        String gateURLStr = gateURL.getFile();
                        File gateJarFile = new File(new URI(gateURLStr.substring(0, gateURLStr.indexOf('!'))));
                        gateHome = gateJarFile.getParentFile().getParentFile();
                    } else if (gateURL.getProtocol().equals("file")) {
                        File gateClassFile = Files.fileFromURL(gateURL);
                        gateHome = gateClassFile.getParentFile().getParentFile().getParentFile();
                    }
                    log.warn("Using \"" + gateHome.getCanonicalPath() + "\" as GATE Home.\nIf this is not correct please set it manually" + " using the -D" + GATE_HOME_PROPERTY_NAME + " option in your start-up script");
                } catch (Throwable thr) {
                    throw new GateRuntimeException("Cannot guess GATE Home. Pease set it manually!", thr);
                }
            }
        }
        log.info("Using " + gateHome.toString() + " as GATE home");
        if (pluginsHome == null) {
            String pluginsHomeStr = System.getProperty(PLUGINS_HOME_PROPERTY_NAME);
            if (pluginsHomeStr != null && pluginsHomeStr.length() > 0) {
                File homeFile = new File(pluginsHomeStr);
                if (homeFile.exists() && homeFile.isDirectory()) {
                    pluginsHome = homeFile;
                }
            }
            if (pluginsHome == null) {
                File homeFile = new File(gateHome, PLUGINS);
                if (homeFile.exists() && homeFile.isDirectory()) {
                    pluginsHome = homeFile;
                }
            }
            if (pluginsHome == null) {
                throw new GateRuntimeException("Could not infer installed plug-ins home!\n" + "Please set it manually using the -D" + PLUGINS_HOME_PROPERTY_NAME + " option in your start-up script.");
            }
        }
        log.info("Using " + pluginsHome.toString() + " as installed plug-ins directory.");
        if (siteConfigFile == null) {
            String siteConfigStr = System.getProperty(SITE_CONFIG_PROPERTY_NAME);
            if (siteConfigStr != null && siteConfigStr.length() > 0) {
                File configFile = new File(siteConfigStr);
                if (configFile.exists()) siteConfigFile = configFile;
            }
            if (siteConfigFile == null) {
                File configFile = new File(gateHome, GATE_DOT_XML);
                if (configFile.exists()) siteConfigFile = configFile;
            }
            if (siteConfigFile == null) {
                throw new GateRuntimeException("Could not locate the site configuration file!\n" + "Please create it at " + new File(gateHome, GATE_DOT_XML).toString() + " or point to an existing one using the -D" + SITE_CONFIG_PROPERTY_NAME + " option in your start-up script!");
            }
        }
        log.info("Using " + siteConfigFile.toString() + " as site configuration file.");
        if (userConfigFile == null) {
            String userConfigStr = System.getProperty(USER_CONFIG_PROPERTY_NAME);
            if (userConfigStr != null && userConfigStr.length() > 0) {
                userConfigFile = new File(userConfigStr);
            } else {
                userConfigFile = new File(getDefaultUserConfigFileName());
            }
        }
        log.info("Using " + userConfigFile + " as user configuration file");
        if (userSessionFile == null) {
            String userSessionStr = System.getProperty(GATE_USER_SESSION_PROPERTY_NAME);
            if (userSessionStr != null && userSessionStr.length() > 0) {
                userSessionFile = new File(userSessionStr);
            } else {
                userSessionFile = new File(getDefaultUserSessionFileName());
            }
        }
        log.info("Using " + userSessionFile + " as user session file");
    }

    /**
   * Loads the CREOLE repositories (aka plugins) that the user has selected for
   * automatic loading. Loads the information about known plugins in memory.
   */
    protected static void initCreoleRepositories() {
        String knownPluginsPath = (String) getUserConfig().get(KNOWN_PLUGIN_PATH_KEY);
        if (knownPluginsPath != null && knownPluginsPath.length() > 0) {
            StringTokenizer strTok = new StringTokenizer(knownPluginsPath, ";", false);
            while (strTok.hasMoreTokens()) {
                String aKnownPluginPath = strTok.nextToken();
                try {
                    URL aPluginURL = new URL(aKnownPluginPath);
                    addKnownPlugin(aPluginURL);
                } catch (MalformedURLException mue) {
                    log.error("Plugin error: " + aKnownPluginPath + " is an invalid URL!");
                }
            }
        }
        File[] dirs = pluginsHome.listFiles();
        for (int i = 0; i < dirs.length; i++) {
            File creoleFile = new File(dirs[i], "creole.xml");
            if (creoleFile.exists()) {
                try {
                    URL pluginURL = dirs[i].toURI().toURL();
                    addKnownPlugin(pluginURL);
                } catch (MalformedURLException mue) {
                    throw new GateRuntimeException(mue);
                }
            }
        }
        File userPluginsHome = PluginUpdateManager.getUserPluginsHome();
        if (userPluginsHome != null && userPluginsHome.isDirectory()) {
            for (File dir : userPluginsHome.listFiles()) {
                File creoleFile = new File(dir, "creole.xml");
                if (creoleFile.exists()) {
                    try {
                        URL pluginURL = dir.toURI().toURL();
                        addKnownPlugin(pluginURL);
                    } catch (MalformedURLException mue) {
                        throw new GateRuntimeException(mue);
                    }
                }
            }
        }
        String pluginPath = getUserConfig().getString(AUTOLOAD_PLUGIN_PATH_KEY);
        String prop = System.getProperty(AUTOLOAD_PLUGIN_PATH_PROPERTY_NAME);
        if (prop != null && prop.length() > 0) pluginPath = prop;
        if (pluginPath == null || pluginPath.length() == 0) {
            return;
        }
        StringTokenizer strTok = new StringTokenizer(pluginPath, ";", false);
        while (strTok.hasMoreTokens()) {
            String aDir = strTok.nextToken();
            try {
                URL aPluginURL = new URL(aDir);
                addAutoloadPlugin(aPluginURL);
            } catch (MalformedURLException mue) {
                log.error("Cannot load " + aDir + " CREOLE repository.", mue);
            }
            try {
                Iterator<URL> loadPluginsIter = getAutoloadPlugins().iterator();
                while (loadPluginsIter.hasNext()) {
                    getCreoleRegister().registerDirectories(loadPluginsIter.next());
                }
            } catch (GateException ge) {
                log.error("Cannot load " + aDir + " CREOLE repository.", ge);
            }
        }
    }

    /** Initialise the CREOLE register. */
    public static void initCreoleRegister() throws GateException {
        for (int i = 0; i < builtinCreoleDirectoryUrls.length; i++) try {
            creoleRegister.addDirectory(new URL(builtinCreoleDirectoryUrls[i]));
        } catch (MalformedURLException e) {
            throw new GateException(e);
        }
        creoleRegister.registerBuiltins();
    }

    /** Initialise the DataStore register. */
    public static void initDataStoreRegister() {
        dataStoreRegister = new DataStoreRegister();
    }

    /**
   * Reads config data (<TT>gate.xml</TT> files). There are three sorts of
   * these files:
   * <UL>
   * <LI> The builtin file from GATE's resources - this is read first.
   * <LI> A site-wide init file given as a command-line argument or as a
   * <TT>gate.config</TT> property - this is read second.
   * <LI> The user's file from their home directory - this is read last.
   * </UL>
   * Settings from files read after some settings have already been made will
   * simply overwrite the previous settings.
   */
    public static void initConfigData() throws GateException {
        ConfigDataProcessor configProcessor = new ConfigDataProcessor();
        URL configURL;
        try {
            configURL = siteConfigFile.toURI().toURL();
        } catch (MalformedURLException mue) {
            throw new GateRuntimeException(mue);
        }
        try {
            InputStream configStream = new FileInputStream(siteConfigFile);
            configProcessor.parseConfigFile(configStream, configURL);
        } catch (IOException e) {
            throw new GateException("Couldn't open site configuration file: " + configURL + " " + e);
        }
        if (userConfigFile != null && userConfigFile.exists()) {
            try {
                configURL = userConfigFile.toURI().toURL();
            } catch (MalformedURLException mue) {
                throw new GateRuntimeException(mue);
            }
            try {
                InputStream configStream = new FileInputStream(userConfigFile);
                configProcessor.parseConfigFile(configStream, configURL);
            } catch (IOException e) {
                throw new GateException("Couldn't open user configuration file: " + configURL + " " + e);
            }
        }
        originalUserConfig.putAll(userConfig);
        log.debug("user config loaded; DBCONFIG=" + DataStoreRegister.getConfigData());
    }

    /**
   * Attempts to guess the Unicode font for the platform.
   */
    public static String guessUnicodeFont() {
        String[] fontNames = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String unicodeFontName = null;
        for (int i = 0; i < fontNames.length; i++) {
            if (fontNames[i].equalsIgnoreCase("Arial Unicode MS")) {
                unicodeFontName = fontNames[i];
                break;
            }
            if (fontNames[i].toLowerCase().indexOf("unicode") != -1) {
                unicodeFontName = fontNames[i];
            }
        }
        return unicodeFontName;
    }

    /**
   * Get a URL that points to either an HTTP server or a file system that
   * contains GATE files (such as test cases). The following locations are tried
   * in sequence:
   * <UL>
   * <LI> <TT>http://derwent.dcs.shef.ac.uk/gate.ac.uk/</TT>, a
   * Sheffield-internal development server (the gate.ac.uk affix is a copy of
   * the file system present on GATE's main public server - see next item);
   * <LI> <TT>http://gate.ac.uk/</TT>, GATE's main public server;
   * <LI> <TT>http://localhost/gate.ac.uk/</TT>, a Web server running on the
   * local machine;
   * <LI> the local file system where the binaries for the current invocation of
   * GATE are stored.
   * </UL>
   * In each case we assume that a Web server will be running on port 80, and
   * that if we can open a socket to that port then the server is running. (This
   * is a bit of a strong assumption, but this URL is used largely by the test
   * suite, so we're not betting anything too critical on it.)
   * <P>
   * Note that the value returned will only be calculated when the existing
   * value recorded by this class is null (which will be the case when neither
   * setUrlBase nor getUrlBase have been called, or if setUrlBase(null) has been
   * called).
   */
    public static URL getUrl() throws GateException {
        if (urlBase != null) return urlBase;
        try {
            if (isNetConnected()) {
                if (tryNetServer("gate.ac.uk", 80, "/")) {
                    log.debug("getUrl() returned " + urlBase);
                    return urlBase;
                }
            }
            if (isLocalWebServer() && tryNetServer(InetAddress.getLocalHost().getHostName(), 80, "/gate.ac.uk/")) {
                log.debug("getUrlBase() returned " + urlBase);
                return urlBase;
            }
            tryFileSystem();
        } catch (MalformedURLException e) {
            throw new GateException("Bad URL, getUrlBase(): " + urlBase + ": " + e);
        } catch (UnknownHostException e) {
            throw new GateException("No host, getUrlBase(): " + urlBase + ": " + e);
        }
        log.debug("getUrlBase() returned " + urlBase);
        return urlBase;
    }

    /**
   * Get a URL that points to either an HTTP server or a file system that
   * contains GATE files (such as test cases). Calls <TT>getUrl()</TT> then
   * adds the <TT>path</TT> parameter to the result.
   *
   * @param path
   *          a path to add to the base URL.
   * @see #getUrl()
   */
    public static URL getUrl(String path) throws GateException {
        getUrl();
        if (urlBase == null) return null;
        URL newUrl = null;
        try {
            newUrl = new URL(urlBase, path);
        } catch (MalformedURLException e) {
            throw new GateException("Bad URL, getUrl( " + path + "): " + e);
        }
        log.debug("getUrl(" + path + ") returned " + newUrl);
        return newUrl;
    }

    /**
   * Flag controlling whether we should try to access the net, e.g. when setting
   * up a base URL.
   */
    private static boolean netConnected = false;

    private static int lastSym;

    /**
   * A list of names of classes that implement {@link gate.creole.ir.IREngine}
   * that will be used as information retrieval engines.
   */
    private static Set<String> registeredIREngines = new HashSet<String>();

    /**
   * Registers a new IR engine. The class named should implement
   * {@link gate.creole.ir.IREngine} and be accessible via the GATE class
   * loader.
   *
   * @param className
   *          the fully qualified name of the class to be registered
   * @throws GateException
   *           if the class does not implement the
   *           {@link gate.creole.ir.IREngine} interface.
   * @throws ClassNotFoundException
   *           if the named class cannot be found.
   */
    public static void registerIREngine(String className) throws GateException, ClassNotFoundException {
        Class<?> aClass = Class.forName(className, true, Gate.getClassLoader());
        if (gate.creole.ir.IREngine.class.isAssignableFrom(aClass)) {
            registeredIREngines.add(className);
        } else {
            throw new GateException(className + " does not implement the " + gate.creole.ir.IREngine.class.getName() + " interface!");
        }
    }

    /**
   * Unregisters a previously registered IR engine.
   *
   * @param className
   *          the name of the class to be removed from the list of registered IR
   *          engines.
   * @return true if the class was found and removed.
   */
    public static boolean unregisterIREngine(String className) {
        return registeredIREngines.remove(className);
    }

    /**
   * Gets the set of registered IR engines.
   *
   * @return an unmodifiable {@link java.util.Set} value.
   */
    public static Set<String> getRegisteredIREngines() {
        return Collections.unmodifiableSet(registeredIREngines);
    }

    /**
   * Gets the GATE home location.
   *
   * @return a File value.
   */
    public static File getGateHome() {
        return gateHome;
    }

    /** Should we assume we're connected to the net? */
    public static boolean isNetConnected() {
        return netConnected;
    }

    /**
   * Tell GATE whether to assume we're connected to the net. Has to be called
   * <B>before</B> {@link #init()}.
   */
    public static void setNetConnected(boolean b) {
        netConnected = b;
    }

    /**
   * Flag controlling whether we should try to access a web server on localhost,
   * e.g. when setting up a base URL. Has to be called <B>before</B>
   * {@link #init()}.
   */
    private static boolean localWebServer = false;

    /** Should we assume there's a local web server? */
    public static boolean isLocalWebServer() {
        return localWebServer;
    }

    /** Tell GATE whether to assume there's a local web server. */
    public static void setLocalWebServer(boolean b) {
        localWebServer = b;
    }

    /**
   * Try to contact a network server. When sucessfull sets urlBase to an HTTP
   * URL for the server.
   *
   * @param hostName
   *          the name of the host to try and connect to
   * @param serverPort
   *          the port to try and connect to
   * @param path
   *          a path to append to the URL when we make a successfull connection.
   *          E.g. for host xyz, port 80, path /thing, the resultant URL would
   *          be <TT>http://xyz:80/thing</TT>.
   */
    public static boolean tryNetServer(String hostName, int serverPort, String path) throws MalformedURLException {
        log.debug("tryNetServer(hostName=" + hostName + ", serverPort=" + serverPort + ", path=" + path + ")");
        try {
            URL url = new URL("http://" + hostName + ":" + serverPort + "/");
            URLConnection uConn = url.openConnection();
            HttpURLConnection huConn = null;
            if (uConn instanceof HttpURLConnection) huConn = (HttpURLConnection) uConn;
            if (huConn.getResponseCode() == -1) return false;
        } catch (IOException e) {
            return false;
        }
        urlBase = new URL("http", hostName, serverPort, path);
        return true;
    }

    /** Try to find GATE files in the local file system */
    protected static boolean tryFileSystem() throws MalformedURLException {
        String urlBaseName = locateGateFiles();
        log.debug("tryFileSystem: " + urlBaseName);
        urlBase = new URL(urlBaseName + "gate/resources/gate.ac.uk/");
        return urlBase == null;
    }

    /**
   * Find the location of the GATE binaries (and resources) in the local file
   * system.
   */
    public static String locateGateFiles() {
        String aGateResourceName = "gate/resources/creole/creole.xml";
        URL resourcesUrl = Gate.getClassLoader().getResource(aGateResourceName);
        StringBuffer basePath = new StringBuffer(resourcesUrl.toExternalForm());
        String urlBaseName = basePath.substring(0, basePath.length() - aGateResourceName.length());
        return urlBaseName;
    }

    /**
   * Checks whether a particular class is a Gate defined type
   */
    public static boolean isGateType(String classname) {
        boolean res = getCreoleRegister().containsKey(classname);
        if (!res) {
            try {
                Class<?> aClass = Class.forName(classname, true, Gate.getClassLoader());
                res = Resource.class.isAssignableFrom(aClass);
            } catch (ClassNotFoundException cnfe) {
                return false;
            }
        }
        return res;
    }

    /** Returns the value for the HIDDEN attribute of a feature map */
    public static boolean getHiddenAttribute(FeatureMap fm) {
        if (fm == null) return false;
        Object value = fm.get(HIDDEN_FEATURE_KEY);
        return value != null && value instanceof String && ((String) value).equals("true");
    }

    /** Sets the value for the HIDDEN attribute of a feature map */
    public static void setHiddenAttribute(FeatureMap fm, boolean hidden) {
        if (hidden) {
            fm.put(HIDDEN_FEATURE_KEY, "true");
        } else {
            fm.remove(HIDDEN_FEATURE_KEY);
        }
    }

    /**
   * Registers a {@link gate.event.CreoleListener} with the Gate system
   */
    public static synchronized void addCreoleListener(CreoleListener l) {
        creoleRegister.addCreoleListener(l);
    }

    /** Set the URL base for GATE files, e.g. <TT>http://gate.ac.uk/</TT>. */
    public static void setUrlBase(URL urlBase) {
        Gate.urlBase = urlBase;
    }

    /** The URL base for GATE files, e.g. <TT>http://gate.ac.uk/</TT>. */
    private static URL urlBase = null;

    /**
   * Class loader used e.g. for loading CREOLE modules, of compiling JAPE rule
   * RHSs.
   */
    private static GateClassLoader classLoader = null;

    /** Get the GATE class loader. */
    public static GateClassLoader getClassLoader() {
        return classLoader;
    }

    /** The CREOLE register. */
    private static CreoleRegister creoleRegister = null;

    /** Get the CREOLE register. */
    public static CreoleRegister getCreoleRegister() {
        return creoleRegister;
    }

    /** The DataStore register */
    private static DataStoreRegister dataStoreRegister = null;

    /**
   * The current executable under execution.
   */
    private static gate.Executable currentExecutable;

    /** Get the DataStore register. */
    public static DataStoreRegister getDataStoreRegister() {
        return dataStoreRegister;
    }

    /**
   * Sets the {@link Executable} currently under execution. At a given time
   * there can be only one executable set. After the executable has finished its
   * execution this value should be set back to null. An attempt to set the
   * executable while this value is not null will result in the method call
   * waiting until the old executable is set to null.
   */
    public static synchronized void setExecutable(gate.Executable executable) {
        if (executable == null) currentExecutable = executable; else {
            while (getExecutable() != null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    throw new LuckyException(ie.toString());
                }
            }
            currentExecutable = executable;
        }
    }

    /**
   * Returns the curently set executable.
   *
   * @see #setExecutable(gate.Executable)
   */
    public static synchronized gate.Executable getExecutable() {
        return currentExecutable;
    }

    /**
   * Returns a new unique string
   */
    public static synchronized String genSym() {
        StringBuffer buff = new StringBuffer(Integer.toHexString(lastSym++).toUpperCase());
        for (int i = buff.length(); i <= 4; i++) buff.insert(0, '0');
        return buff.toString();
    }

    /** GATE development environment configuration data (stored in gate.xml). */
    private static OptionsMap userConfig = new OptionsMap();

    /**
   * This map stores the init-time config data in case we need it later. GATE
   * development environment configuration data (stored in gate.xml).
   */
    private static OptionsMap originalUserConfig = new OptionsMap();

    /** Name of the XML element for GATE development environment config data. */
    private static String userConfigElement = "GATECONFIG";

    /**
   * Gate the name of the XML element for GATE development environment config
   * data.
   */
    public static String getUserConfigElement() {
        return userConfigElement;
    }

    /**
   * Get the site config file (generally set during command-line processing or
   * as a <TT>gate.config</TT> property). If the config is null, this method
   * checks the <TT>gate.config</TT> property and uses it if non-null.
   */
    public static File getSiteConfigFile() {
        if (siteConfigFile == null) {
            String gateConfigProperty = System.getProperty(GATE_CONFIG_PROPERTY);
            if (gateConfigProperty != null) siteConfigFile = new File(gateConfigProperty);
        }
        return siteConfigFile;
    }

    /** Set the site config file (e.g. during command-line processing). */
    public static void setSiteConfigFile(File siteConfigFile) {
        Gate.siteConfigFile = siteConfigFile;
    }

    /** Shorthand for local newline */
    private static String nl = Strings.getNl();

    /** An empty config data file. */
    private static String emptyConfigFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + nl + "<!-- " + GATE_DOT_XML + ": GATE configuration data -->" + nl + "<GATE>" + nl + "" + nl + "<!-- NOTE: the next element may be overwritten by the GUI!!! -->" + nl + "<" + userConfigElement + "/>" + nl + "" + nl + "</GATE>" + nl;

    /**
   * Get an empty config file. <B>NOTE:</B> this method is intended only for
   * use by the test suite.
   */
    public static String getEmptyConfigFile() {
        return emptyConfigFile;
    }

    /**
   * Get the GATE development environment configuration data (initialised from
   * <TT>gate.xml</TT>).
   */
    public static OptionsMap getUserConfig() {
        return userConfig;
    }

    /**
   * Get the original, initialisation-time, GATE development environment
   * configuration data (initialised from <TT>gate.xml</TT>).
   */
    public static OptionsMap getOriginalUserConfig() {
        return originalUserConfig;
    }

    /**
   * Update the GATE development environment configuration data in the user's
   * <TT>gate.xml</TT> file (create one if it doesn't exist).
   */
    public static void writeUserConfig() throws GateException {
        if (sandboxed) return;
        String pluginsHomeStr;
        try {
            pluginsHomeStr = pluginsHome.getCanonicalPath();
        } catch (IOException ioe) {
            throw new GateRuntimeException("Problem while locating the plug-ins home!", ioe);
        }
        String userPluginHomeStr;
        try {
            File userPluginHome = PluginUpdateManager.getUserPluginsHome();
            userPluginHomeStr = (userPluginHome != null ? userPluginHome.getCanonicalPath() : null);
        } catch (IOException ioe) {
            throw new GateRuntimeException("Unable to access user plugin directory!", ioe);
        }
        String knownPluginPath = "";
        Iterator<URL> pluginIter = getKnownPlugins().iterator();
        while (pluginIter.hasNext()) {
            URL aPluginURL = pluginIter.next();
            if (aPluginURL.getProtocol().equals("file")) {
                File pluginDirectory = Files.fileFromURL(aPluginURL);
                try {
                    if (pluginDirectory.getCanonicalPath().startsWith(pluginsHomeStr)) continue;
                    if (userPluginHomeStr != null && pluginDirectory.getCanonicalPath().startsWith(userPluginHomeStr)) continue;
                } catch (IOException ioe) {
                    throw new GateRuntimeException("Problem while locating the plug-in" + aPluginURL.toString(), ioe);
                }
            }
            if (knownPluginPath.length() > 0) knownPluginPath += ";";
            knownPluginPath += aPluginURL.toExternalForm();
        }
        getUserConfig().put(KNOWN_PLUGIN_PATH_KEY, knownPluginPath);
        String loadPluginPath = "";
        pluginIter = getAutoloadPlugins().iterator();
        while (pluginIter.hasNext()) {
            URL aPluginURL = pluginIter.next();
            if (loadPluginPath.length() > 0) loadPluginPath += ";";
            loadPluginPath += aPluginURL.toExternalForm();
        }
        getUserConfig().put(AUTOLOAD_PLUGIN_PATH_KEY, loadPluginPath);
        File configFile = getUserConfigFile();
        try {
            if (!configFile.exists()) {
                FileOutputStream fos = new FileOutputStream(configFile);
                OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
                writer.write(emptyConfigFile);
                writer.close();
            }
            Files.updateXmlElement(configFile, userConfigElement, userConfig);
        } catch (IOException e) {
            throw new GateException("problem writing user " + GATE_DOT_XML + ": " + nl + e.toString());
        }
    }

    /**
   * Get the name of the user's <TT>gate.xml</TT> config file (this doesn't
   * guarantee that file exists!).
   *
   * @deprecated Use {@link #getUserConfigFile} instead.
   */
    public static String getUserConfigFileName() {
        return getDefaultUserConfigFileName();
    }

    /**
   * Get the default path to the user's config file, which is used unless an
   * alternative name has been specified via system properties or
   * {@link #setUserConfigFile}.
   *
   * @return the default user config file path.
   */
    public static String getDefaultUserConfigFileName() {
        String filePrefix = "";
        if (runningOnUnix()) filePrefix = ".";
        String userConfigName = System.getProperty("user.home") + Strings.getFileSep() + filePrefix + GATE_DOT_XML;
        return userConfigName;
    }

    /**
   * Get the default path to the user's session file, which is used unless an
   * alternative name has been specified via system properties or
   * {@link #setUserSessionFile(File)}
   *
   * @return the default user session file path.
   */
    public static String getDefaultUserSessionFileName() {
        String filePrefix = "";
        if (runningOnUnix()) filePrefix = ".";
        String userSessionName = System.getProperty("user.home") + Strings.getFileSep() + filePrefix + GATE_DOT_SER;
        return userSessionName;
    }

    /**
   * This method tries to guess if we are on a UNIX system. It does this by
   * checking the value of <TT>System.getProperty("file.separator")</TT>; if
   * this is "/" it concludes we are on UNIX. <B>This is obviously not a very
   * good idea in the general case, so nothing much should be made to depend on
   * this method (e.g. just naming of config file <TT>.gate.xml</TT> as
   * opposed to <TT>gate.xml</TT>)</B>.
   */
    public static boolean runningOnUnix() {
        return Strings.getFileSep().equals("/");
    }

    /**
   * This method tries to guess if we are on a Mac OS X system.  It does this
   * by checking the value of <TT>System.getProperty("os.name")</TT>.  Note
   * that if this method returns true, {@link #runningOnUnix()} will also
   * return true (i.e. Mac is considered a Unix platform) but the reverse is
   * not necessarily the case.
   */
    public static boolean runningOnMac() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }

    /**
   * Returns the list of CREOLE directories the system knows about (either
   * pre-installed plugins in the plugins directory or CREOLE directories that
   * have previously been loaded manually).
   *
   * @return a {@link List} of {@link URL}s.
   */
    public static List<URL> getKnownPlugins() {
        return knownPlugins;
    }

    /**
   * Adds the plugin to the list of known plugins.
   *
   * @param pluginURL
   *          the URL for the new plugin.
   */
    public static void addKnownPlugin(URL pluginURL) {
        pluginURL = normaliseCreoleUrl(pluginURL);
        if (knownPlugins.contains(pluginURL)) return;
        knownPlugins.add(pluginURL);
    }

    /**
   * Makes sure the provided URL ends with "/" (CREOLE URLs always point to
   * directories so thry should always end with a slash.
   *
   * @param url
   *          the URL to be normalised
   * @return the (maybe) corrected URL.
   */
    public static URL normaliseCreoleUrl(URL url) {
        String urlName = url.toExternalForm();
        String separator = "/";
        if (urlName.endsWith(separator)) {
            return url;
        } else {
            urlName += separator;
            try {
                return new URL(urlName);
            } catch (MalformedURLException mue) {
                throw new GateRuntimeException(mue);
            }
        }
    }

    /**
   * Returns the list of CREOLE directories the system loads automatically at
   * start-up.
   *
   * @return a {@link List} of {@link URL}s.
   */
    public static List<URL> getAutoloadPlugins() {
        return autoloadPlugins;
    }

    /**
   * Adds a new directory to the list of plugins that are loaded automatically
   * at start-up.
   *
   * @param pluginUrl
   *          the URL for the new plugin.
   */
    public static void addAutoloadPlugin(URL pluginUrl) {
        pluginUrl = normaliseCreoleUrl(pluginUrl);
        if (autoloadPlugins.contains(pluginUrl)) return;
        addKnownPlugin(pluginUrl);
        autoloadPlugins.add(pluginUrl);
    }

    /**
   * Gets the information about a known directory.
   *
   * @param directory
   *          the URL for the directory in question.
   * @return a {@link DirectoryInfo} value.
   */
    public static DirectoryInfo getDirectoryInfo(URL directory) {
        directory = normaliseCreoleUrl(directory);
        if (!knownPlugins.contains(directory)) return null;
        DirectoryInfo dInfo = pluginData.get(directory);
        if (dInfo == null) {
            dInfo = new DirectoryInfo(directory);
            pluginData.put(directory, dInfo);
        }
        return dInfo;
    }

    /**
   * Returns information about plugin directories which provide the requested
   * resource
   * 
   * @param resourceClassName
   *          the class name of the resource you are interested in
   * @return information about the directories which provide an implementation
   *         of the requested resource
   */
    public static Set<DirectoryInfo> getDirectoryInfo(String resourceClassName) {
        Set<DirectoryInfo> dirs = new HashSet<DirectoryInfo>();
        for (URL url : knownPlugins) {
            DirectoryInfo dInfo = getDirectoryInfo(url);
            for (ResourceInfo rInfo : dInfo.getResourceInfoList()) {
                if (rInfo.resourceClassName.equals(resourceClassName)) {
                    dirs.add(dInfo);
                }
            }
        }
        return dirs;
    }

    /**
   * Tells the system to &quot;forget&quot; about one previously known
   * directory. If the specified directory was loaded, it will be unloaded as
   * well - i.e. all the metadata relating to resources defined by this
   * directory will be removed from memory.
   *
   * @param pluginURL
   */
    public static void removeKnownPlugin(URL pluginURL) {
        pluginURL = normaliseCreoleUrl(pluginURL);
        knownPlugins.remove(pluginURL);
        autoloadPlugins.remove(pluginURL);
        creoleRegister.removeDirectory(pluginURL);
        pluginData.remove(pluginURL);
    }

    /**
   * Tells the system to remove a plugin URL from the list of plugins that are
   * loaded automatically at system start-up. This will be reflected in the
   * user's configuration data file.
   *
   * @param pluginURL
   *          the URL to be removed.
   */
    public static void removeAutoloadPlugin(URL pluginURL) {
        pluginURL = normaliseCreoleUrl(pluginURL);
        autoloadPlugins.remove(pluginURL);
    }

    /**
   * Stores information about the contents of a CREOLE directory.
   */
    public static class DirectoryInfo {

        private String name, html;

        private boolean core, remote;

        public boolean isCorePlugin() {
            return core;
        }

        public boolean isRemotePlugin() {
            return remote;
        }

        public boolean isUserPlugin() {
            File userPluginsHome = PluginUpdateManager.getUserPluginsHome();
            return (userPluginsHome != null && getUrl().toString().startsWith(userPluginsHome.toURI().toString()));
        }

        public String toHTMLString() {
            return html;
        }

        public DirectoryInfo(URL url) {
            this.url = normaliseCreoleUrl(url);
            valid = true;
            resourceInfoList = new ArrayList<ResourceInfo>();
            parseCreole();
            remote = !this.url.getProtocol().equalsIgnoreCase("file");
            if (Gate.isSandboxed() || Gate.getPluginsHome() == null) {
                core = false;
            } else {
                core = !remote && (url.toString().startsWith(Gate.getPluginsHome().toURI().toString()));
            }
            html = "<html><body>" + getName() + "<br><span style='font-size: 80%;'>" + (remote ? this.url.toString() : Files.fileFromURL(this.url).getAbsolutePath()) + "</span></body></html>";
        }

        public String getName() {
            if (name != null) return name;
            name = url.getPath();
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            int lastSlash = name.lastIndexOf("/");
            if (lastSlash != -1) {
                name = name.substring(lastSlash + 1);
            }
            try {
                name = new URI(name).getPath();
            } catch (URISyntaxException ex) {
            }
            return name;
        }

        /**
     * Performs a shallow parse of the creole.xml file to get the information
     * about the resources contained.
     */
        protected void parseCreole() {
            SAXBuilder builder = new SAXBuilder(false);
            try {
                URL creoleFileURL = new URL(url, "creole.xml");
                org.jdom.Document creoleDoc = builder.build(creoleFileURL);
                final Map<String, ResourceInfo> resInfos = new LinkedHashMap<String, ResourceInfo>();
                List<Element> jobsList = new ArrayList<Element>();
                List<String> jarsToScan = new ArrayList<String>();
                List<String> allJars = new ArrayList<String>();
                jobsList.add(creoleDoc.getRootElement());
                while (!jobsList.isEmpty()) {
                    Element currentElem = jobsList.remove(0);
                    if (currentElem.getName().equalsIgnoreCase("JAR")) {
                        List<Attribute> attrs = currentElem.getAttributes();
                        Iterator<Attribute> attrsIt = attrs.iterator();
                        while (attrsIt.hasNext()) {
                            Attribute attr = attrsIt.next();
                            if (attr.getName().equalsIgnoreCase("SCAN") && attr.getBooleanValue()) {
                                jarsToScan.add(currentElem.getTextTrim());
                                break;
                            }
                        }
                        allJars.add(currentElem.getTextTrim());
                    } else if (currentElem.getName().equalsIgnoreCase("RESOURCE")) {
                        String resName = currentElem.getChildTextTrim("NAME");
                        String resClass = currentElem.getChildTextTrim("CLASS");
                        String resComment = currentElem.getChildTextTrim("COMMENT");
                        if (!resInfos.containsKey(resClass)) {
                            ResourceInfo rHandler = new ResourceInfo(resName, resClass, resComment);
                            resInfos.put(resClass, rHandler);
                        }
                    } else {
                        List<Element> newJobsList = new ArrayList<Element>(currentElem.getChildren());
                        newJobsList.addAll(jobsList);
                        jobsList = newJobsList;
                    }
                }
                for (String jarFile : jarsToScan) {
                    URL jarUrl = new URL(url, jarFile);
                    scanJar(jarUrl, resInfos);
                }
                List<ResourceInfo> incompleteResInfos = new ArrayList<ResourceInfo>();
                for (ResourceInfo ri : resInfos.values()) {
                    if (ri.getResourceName() == null) {
                        incompleteResInfos.add(ri);
                    }
                }
                if (!incompleteResInfos.isEmpty()) {
                    fillInResInfos(incompleteResInfos, allJars);
                }
                for (ResourceInfo ri : incompleteResInfos) {
                    if (ri.getResourceName() == null) {
                        ri.resourceName = ri.resourceClassName.substring(ri.resourceClassName.lastIndexOf('.') + 1);
                    }
                }
                resourceInfoList.addAll(resInfos.values());
            } catch (IOException ioe) {
                valid = false;
                log.error("Problem while parsing plugin " + url.toExternalForm() + "!\n" + ioe.toString() + "\nPlugin not available!");
            } catch (JDOMException jde) {
                valid = false;
                log.error("Problem while parsing plugin " + url.toExternalForm() + "!\n" + jde.toString() + "\nPlugin not available!");
            }
        }

        protected void scanJar(URL jarUrl, Map<String, ResourceInfo> resInfos) throws IOException {
            JarInputStream jarInput = new JarInputStream(jarUrl.openStream(), false);
            JarEntry entry = null;
            while ((entry = jarInput.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName != null && entryName.endsWith(".class")) {
                    final String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    if (!resInfos.containsKey(className)) {
                        ClassReader classReader = new ClassReader(jarInput);
                        ResourceInfo resInfo = new ResourceInfo(null, className, null);
                        ResourceInfoVisitor visitor = new ResourceInfoVisitor(resInfo);
                        classReader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                        if (visitor.isCreoleResource()) {
                            resInfos.put(className, resInfo);
                        }
                    }
                }
            }
            jarInput.close();
        }

        protected void fillInResInfos(List<ResourceInfo> incompleteResInfos, List<String> allJars) throws IOException {
            URL[] jarUrls = new URL[allJars.size()];
            for (int i = 0; i < jarUrls.length; i++) {
                jarUrls[i] = new URL(url, allJars.get(i));
            }
            ClassLoader tempClassLoader = new URLClassLoader(jarUrls, Gate.class.getClassLoader());
            for (ResourceInfo ri : incompleteResInfos) {
                String classFile = ri.getResourceClassName().replace('.', '/') + ".class";
                InputStream classStream = tempClassLoader.getResourceAsStream(classFile);
                if (classStream != null) {
                    ClassReader classReader = new ClassReader(classStream);
                    ClassVisitor visitor = new ResourceInfoVisitor(ri);
                    classReader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    classStream.close();
                }
            }
        }

        /**
     * @return Returns the resourceInfoList.
     */
        public List<ResourceInfo> getResourceInfoList() {
            return resourceInfoList;
        }

        /**
     * @return Returns the url.
     */
        public URL getUrl() {
            return url;
        }

        /**
     * @return Returns the valid.
     */
        public boolean isValid() {
            return valid;
        }

        /**
     * The URL for the CREOLE directory.
     */
        protected URL url;

        /**
     * Is the directory valid (i.e. is the location reachable and the creole.xml
     * file parsable).
     */
        protected boolean valid;

        /**
     * The list of {@link Gate.ResourceInfo} objects.
     */
        protected List<ResourceInfo> resourceInfoList;
    }

    /**
   * Stores information about a resource defined by a CREOLE directory. The
   * resource might not have been loaded in the system so not all information
   * normally provided by the {@link ResourceData} class is available. This is
   * what makes this class different from {@link ResourceData}.
   */
    public static class ResourceInfo {

        public ResourceInfo(String name, String className, String comment) {
            this.resourceClassName = className;
            this.resourceName = name;
            this.resourceComment = comment;
        }

        /**
     * @return Returns the resourceClassName.
     */
        public String getResourceClassName() {
            return resourceClassName;
        }

        /**
     * @return Returns the resourceComment.
     */
        public String getResourceComment() {
            return resourceComment;
        }

        /**
     * @return Returns the resourceName.
     */
        public String getResourceName() {
            return resourceName;
        }

        /**
     * The class for the resource.
     */
        protected String resourceClassName;

        /**
     * The resource name.
     */
        protected String resourceName;

        /**
     * The comment for the resource.
     */
        protected String resourceComment;
    }

    /**
   * ClassVisitor that uses information from a CreoleResource annotation on the
   * visited class (if such exists) to fill in the name and comment in the
   * corresponding ResourceInfo.
   */
    private static class ResourceInfoVisitor extends EmptyVisitor {

        private ResourceInfo resInfo;

        private boolean foundCreoleResource = false;

        private boolean isAbstract = false;

        public ResourceInfoVisitor(ResourceInfo resInfo) {
            this.resInfo = resInfo;
        }

        public boolean isCreoleResource() {
            return foundCreoleResource && !isAbstract;
        }

        /**
     * Type descriptor for the CreoleResource annotation type.
     */
        private static final String CREOLE_RESOURCE_DESC = Type.getDescriptor(CreoleResource.class);

        /**
     * Visit the class header, checking whether this is an abstract class or
     * interface and setting the isAbstract flag appropriately.
     */
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isAbstract = ((access & (Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT)) != 0);
        }

        /**
     * Visit an annotation on the class.
     */
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(CREOLE_RESOURCE_DESC)) {
                foundCreoleResource = true;
                return new EmptyVisitor() {

                    public void visit(String name, Object value) {
                        if (name.equals("name") && resInfo.resourceName == null) {
                            resInfo.resourceName = (String) value;
                        } else if (name.equals("comment") && resInfo.resourceComment == null) {
                            resInfo.resourceComment = (String) value;
                        }
                    }

                    public AnnotationVisitor visitAnnotation(String name, String desc) {
                        return new EmptyVisitor();
                    }
                };
            } else {
                return super.visitAnnotation(desc, visible);
            }
        }
    }

    /**
   * The top level directory of the GATE installation.
   */
    protected static File gateHome;

    /** Site config file */
    private static File siteConfigFile;

    /** User config file */
    private static File userConfigFile;

    /**
   * The top level directory for GATE installed plugins.
   */
    protected static File pluginsHome;

    /**
   * The "builtin" creole directory URL, where the creole.xml that defines
   * things like DocumentImpl can be found.
   */
    protected static URL builtinCreoleDir;

    /**
   * The user session file to use.
   */
    protected static File userSessionFile;

    /**
   * Set the location of the GATE home directory.
   *
   * @throws IllegalStateException
   *           if the value has already been set.
   */
    public static void setGateHome(File gateHome) {
        if (Gate.gateHome != null) {
            throw new IllegalStateException("gateHome has already been set");
        }
        Gate.gateHome = gateHome;
    }

    /**
   * Set the location of the plugins directory.
   *
   * @throws IllegalStateException
   *           if the value has already been set.
   */
    public static void setPluginsHome(File pluginsHome) {
        if (Gate.pluginsHome != null) {
            throw new IllegalStateException("pluginsHome has already been set");
        }
        Gate.pluginsHome = pluginsHome;
    }

    /**
   * Get the location of the plugins directory.
   *
   * @return the plugins drectory, or null if this has not yet been set (i.e.
   *         <code>Gate.init()</code> has not yet been called).
   */
    public static File getPluginsHome() {
        return pluginsHome;
    }

    /**
   * Set the location of the user's config file.
   *
   * @throws IllegalStateException
   *           if the value has already been set.
   */
    public static void setUserConfigFile(File userConfigFile) {
        if (Gate.userConfigFile != null) {
            throw new IllegalStateException("userConfigFile has already been set");
        }
        Gate.userConfigFile = userConfigFile;
    }

    /**
   * Get the location of the user's config file.
   *
   * @return the user config file, or null if this has not yet been set (i.e.
   *         <code>Gate.init()</code> has not yet been called).
   */
    public static File getUserConfigFile() {
        return userConfigFile;
    }

    /**
   * Set the URL to the "builtin" creole directory. The URL must point to a
   * directory, and must end with a forward slash.
   *
   * @throws IllegalStateException
   *           if the value has already been set.
   */
    public static void setBuiltinCreoleDir(URL builtinCreoleDir) {
        if (Gate.builtinCreoleDir != null) {
            throw new IllegalStateException("builtinCreoleDir has already been set");
        }
        Gate.builtinCreoleDir = builtinCreoleDir;
    }

    /**
   * Get the URL to the "builtin" creole directory, i.e. the directory that
   * contains the creole.xml file that defines things like DocumentImpl, the
   * Controllers, etc.
   */
    public static URL getBuiltinCreoleDir() {
        return builtinCreoleDir;
    }

    /**
   * Set the user session file. This can only done prior to calling Gate.init()
   * which will set the file to either the OS-specific default or whatever has
   * been set by the property gate.user.session
   *
   * @throws IllegalStateException
   *           if the value has already been set.
   */
    public static void setUserSessionFile(File newUserSessionFile) {
        if (Gate.userSessionFile != null) {
            throw new IllegalStateException("userSessionFile has already been set");
        }
        Gate.userSessionFile = newUserSessionFile;
    }

    /**
   * Get the user session file.
   *
   * @return the file corresponding to the user session file or null, if not yet
   *         set.
   */
    public static File getUserSessionFile() {
        return userSessionFile;
    }

    /**
   * The list of plugins (aka CREOLE directories) the system knows about. This
   * list contains URL objects.
   */
    private static List<URL> knownPlugins;

    /**
   * The list of plugins (aka CREOLE directories) the system loads automatically
   * at start-up. This list contains URL objects.
   */
    protected static List<URL> autoloadPlugins;

    /**
   * Map from URL of directory to {@link DirectoryInfo}.
   */
    protected static Map<URL, DirectoryInfo> pluginData;

    /**
   * Flag for whether to use native serialization or xml serialization when
   * saving applications.
   */
    private static boolean useXMLSerialization = true;

    /**
   * Tell GATE whether to use XML serialization for applications.
   */
    public static void setUseXMLSerialization(boolean useXMLSerialization) {
        Gate.useXMLSerialization = useXMLSerialization;
    }

    /**
   * Should we use XML serialization for applications.
   */
    public static boolean getUseXMLSerialization() {
        return useXMLSerialization;
    }

    /**
   * Returns the listeners map, a map that holds all the listeners that
   * are singletons (e.g. the status listener that updates the status
   * bar on the main frame or the progress listener that updates the
   * progress bar on the main frame). The keys used are the class names
   * of the listener interface and the values are the actual listeners
   * (e.g "gate.event.StatusListener" -> this). The returned map is the
   * actual data member used to store the listeners so any changes in
   * this map will be visible to everyone.
   * @return the listeners map
   */
    public static java.util.Map<String, EventListener> getListeners() {
        return listeners;
    }

    /**
   * A Map which holds listeners that are singletons (e.g. the status
   * listener that updates the status bar on the main frame or the
   * progress listener that updates the progress bar on the main frame).
   * The keys used are the class names of the listener interface and the
   * values are the actual listeners (e.g "gate.event.StatusListener" ->
   * this).
   */
    private static final java.util.Map<String, EventListener> listeners = new HashMap<String, EventListener>();
}
