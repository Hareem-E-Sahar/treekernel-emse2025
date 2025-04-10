package org.xhtmlrenderer.util;

import org.xhtmlrenderer.DefaultCSSMarker;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * <p>Stores runtime configuration information for application parameters that may
 * vary on restarting. This implements the Singleton pattern, but through static
 * methods. That is, the first time Configuration is used, the properties are
 * loaded into the Singleton instance. Subsequent calls to valueFor() retrieve
 * values from the Singleton. To look up a property, use
 * {@link Configuration#valueFor(String)}.
 * </p>
 * <p> Properties may be overridden using a second properties file, or individually
 * using System properties specified on the command line. To override using a
 * second properties file, specify the System property xr-conf. This should be
 * the location of the second file relative to the CLASSPATH, or else a file
 * path, e.g.</p>
 * <code>java -Dxr-conf=resources/conf/myprops.conf</code>
 * <p>
 * You can also place your override properties file in your user home directory,
 * in </p>
 * <code>${user.home}/.flyingsaucer/local.xhtmlrenderer.conf</code>
 * <p> To override a property using the System properties, just re-define the
 * property on the command line. e.g.</p>
 * <code>java -Dxr.property-name=new_value</code>
 * <p>The order in which these will be read is: default properties (bundled with
 * the core, in the jar; override configuration properties; properties file in
 * user.home; and system properties.</p>
 * <p>You can override as many properties as you like. </p> 
 * <p> Note that overrides are driven by the property names in the default
 * configuration file. Specifying a property name not in that file will have no
 * effect--the property will not be loaded or available for lookup.
 * Configuration is NOT used to control logging levels or output; see
 * LogStartupConfig.</p>
 * <p>
 * There are convenience converstion method for all the primitive types, in
 * methods like {@link #valueAsInt()}. A default must always be provided for these
 * methods. The default is returned if the value is not found, or if the
 * conversion from String fails. If the value is not present, or the conversion
 * fails, a warning message is written to the log.</p>
 *
 * @author Patrick Wright
 */
public class Configuration {

    /**
     * Our backing data store of properties.
     */
    private Properties properties;

    /**
     * The log Level for Configuration messages; taken from show-config System property.
     */
    private Level logLevel;

    /**
     * The Singleton instance of the class.
     */
    private static Configuration sInstance;

    /**
     * List of LogRecords for messages from Configuration startup; used to hold these
     * temporarily as we can't use XRLog while starting up, as it depends on Configuration.
     */
    private List startupLogRecords;

    /**
     * Logger we use internally related to configuration.
     */
    private Logger configLogger;

    /**
     * The location of our default properties file; must be on the CLASSPATH.
     */
    private static final String SF_FILE_NAME = "resources/conf/xhtmlrenderer.conf";

    /**
     * Default constructor. Will parse default configuration file, system properties, override properties, etc. and
     * result in a usable Configuration instance.
     *
     * @throws RuntimeException if any stage of loading configuration results in an Exception. This could happen,
     * for example, if the default configuration file was not readable.
     */
    private Configuration() {
        startupLogRecords = new ArrayList();
        try {
            try {
                String val = null;
                try {
                    val = System.getProperty("show-config");
                } catch (SecurityException ex) {
                    val = null;
                }
                logLevel = Level.OFF;
                if (val != null) {
                    logLevel = LoggerUtil.parseLogLevel(val, Level.OFF);
                }
            } catch (SecurityException e) {
                System.err.println(e.getLocalizedMessage());
            }
            loadDefaultProperties();
            String sysOverrideFile = getSystemPropertyOverrideFileName();
            if (sysOverrideFile != null) {
                loadOverrideProperties(sysOverrideFile);
            } else {
                String userHomeOverrideFileName = getUserHomeOverrideFileName();
                if (userHomeOverrideFileName != null) {
                    loadOverrideProperties(userHomeOverrideFileName);
                }
            }
            loadSystemProperties();
            logAfterLoad();
        } catch (RuntimeException e) {
            handleUnexpectedExceptionOnInit(e);
            throw e;
        } catch (Exception e) {
            handleUnexpectedExceptionOnInit(e);
            throw new RuntimeException(e);
        }
    }

    private void handleUnexpectedExceptionOnInit(Exception e) {
        System.err.println("Could not initialize configuration for Flying Saucer library. Message is: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Sets the logger which we use for Configuration-related logging. Before this is
     * called the first time, all internal log records are queued up; they are flushed to
     * the logger when this method is first called. Afterwards, all log events are written
     * to this logger. This queueing behavior helps avoid order-of-operations bugs
     * related to loading configuration information related to logging.
     *
     * @param logger Logger used for Configuration-related messages
     */
    public static void setConfigLogger(Logger logger) {
        Configuration config = instance();
        config.configLogger = logger;
        if (config.startupLogRecords != null) {
            Iterator iter = config.startupLogRecords.iterator();
            while (iter.hasNext()) {
                LogRecord lr = (LogRecord) iter.next();
                logger.log(lr.getLevel(), lr.getMessage());
            }
            config.startupLogRecords = null;
        }
    }

    /**
     * Used internally for logging status/info about the class.
     *
     * @param level the logging level to record the message at
     * @param msg the message to log
     */
    private void println(Level level, String msg) {
        if (logLevel != Level.OFF) {
            if (configLogger == null) {
                startupLogRecords.add(new LogRecord(level, msg));
            } else {
                configLogger.log(level, msg);
            }
        }
    }

    /**
     * Used internally to log a message about the class at level INFO
     *
     * @param msg message to log
     */
    private void info(String msg) {
        if (logLevel.intValue() <= Level.INFO.intValue()) {
            println(Level.INFO, msg);
        }
    }

    /**
     * Used internally to log a message about the class at level WARNING
     *
     * @param msg message to log
     */
    private void warning(String msg) {
        if (logLevel.intValue() <= Level.WARNING.intValue()) {
            println(Level.WARNING, msg);
        }
    }

    /**
     * Used internally to log a message about the class at level WARNING, in case an exception was thrown
     *
     * @param msg message to log
     * @param th  the exception to report
     */
    private void warning(String msg, Throwable th) {
        warning(msg);
        th.printStackTrace();
    }

    /**
     * Used internally to log a message about the class at level FINE
     *
     * @param msg message to log
     */
    private void fine(String msg) {
        if (logLevel.intValue() <= Level.FINE.intValue()) {
            println(Level.FINE, msg);
        }
    }

    /**
     * Used internally to log a message about the class at level FINER
     *
     * @param msg message to log
     */
    private void finer(String msg) {
        if (logLevel.intValue() <= Level.FINER.intValue()) {
            println(Level.FINER, msg);
        }
    }

    /**
     * Loads the default set of properties, which may be overridden.
     */
    private void loadDefaultProperties() {
        try {
            InputStream readStream = GeneralUtil.openStreamFromClasspath(new DefaultCSSMarker(), SF_FILE_NAME);
            if (readStream == null) {
                System.err.println("WARNING: Flying Saucer: No configuration files found in classpath using URL: " + SF_FILE_NAME + ", resorting to hard-coded fallback properties.");
                this.properties = newFallbackProperties();
            } else {
                try {
                    this.properties = new Properties();
                    this.properties.load(readStream);
                } finally {
                    readStream.close();
                }
            }
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException("Could not load properties file for configuration.", ex);
        }
        info("Configuration loaded from " + SF_FILE_NAME);
    }

    /**
     * Loads overriding property values from a second configuration file; this
     * is optional. See class documentation.
     *
     * @param uri Path to the file, or classpath URL, where properties are defined.
     */
    private void loadOverrideProperties(String uri) {
        try {
            File f = new File(uri);
            Properties temp = new Properties();
            if (f.exists()) {
                info("Found config override file " + f.getAbsolutePath());
                try {
                    InputStream readStream = new BufferedInputStream(new FileInputStream(f));
                    try {
                        temp.load(readStream);
                    } finally {
                        readStream.close();
                    }
                } catch (IOException iex) {
                    warning("Error while loading override properties file; skipping.", iex);
                    return;
                }
            } else {
                InputStream in = null;
                try {
                    URL url = new URL(uri);
                    in = new BufferedInputStream(url.openStream());
                    info("Found config override URI " + uri);
                    temp.load(in);
                } catch (MalformedURLException e) {
                    warning("URI for override properties is malformed, skipping: " + uri);
                    return;
                } catch (IOException e) {
                    warning("Overridden properties could not be loaded from URI: " + uri, e);
                    return;
                } finally {
                    if (in != null) try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
            Enumeration elem = this.properties.keys();
            List lp = Collections.list(elem);
            Collections.sort(lp);
            Iterator iter = lp.iterator();
            int cnt = 0;
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String val = temp.getProperty(key);
                if (val != null) {
                    this.properties.setProperty(key, val);
                    finer("  " + key + " -> " + val);
                    cnt++;
                }
            }
            finer("Configuration: " + cnt + " properties overridden from secondary properties file.");
            Enumeration allRead = temp.keys();
            List ap = Collections.list(allRead);
            Collections.sort(ap);
            iter = ap.iterator();
            cnt = 0;
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String val = temp.getProperty(key);
                if (val != null) {
                    this.properties.setProperty(key, val);
                    finer("  (+)" + key + " -> " + val);
                    cnt++;
                }
            }
            finer("Configuration: " + cnt + " properties added from secondary properties file.");
        } catch (SecurityException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    private String getSystemPropertyOverrideFileName() {
        try {
            return System.getProperty("xr.conf");
        } catch (SecurityException e) {
            return null;
        }
    }

    private String getUserHomeOverrideFileName() {
        try {
            String overrideName = System.getProperty("user.home") + File.separator + ".flyingsaucer" + File.separator + "local.xhtmlrenderer.conf";
            return overrideName;
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Loads overriding property values from a System properties; this is
     * optional. See class documentation.
     */
    private void loadSystemProperties() {
        Enumeration elem = properties.keys();
        List lp = Collections.list(elem);
        Collections.sort(lp);
        Iterator iter = lp.iterator();
        fine("Overriding loaded configuration from System properties.");
        int cnt = 0;
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (!key.startsWith("xr.")) {
                continue;
            }
            try {
                String val = System.getProperty(key);
                if (val != null) {
                    properties.setProperty(key, val);
                    finer("  Overrode value for " + key);
                    cnt++;
                }
            } catch (SecurityException e) {
            }
        }
        fine("Configuration: " + cnt + " properties overridden from System properties.");
        final Properties sysProps = System.getProperties();
        final Enumeration keys = sysProps.keys();
        cnt = 0;
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith("xr.") && !this.properties.containsKey(key)) {
                final Object val = sysProps.get(key);
                this.properties.put(key, val);
                finer("  (+) " + key);
                cnt++;
            }
        }
        fine("Configuration: " + cnt + " FS properties added from System properties.");
    }

    /**
     * Writes a log of loaded properties to the plumbing.init Logger.
     */
    private void logAfterLoad() {
        Enumeration elem = properties.keys();
        List lp = Collections.list(elem);
        Collections.sort(lp);
        Iterator iter = lp.iterator();
        finer("Configuration contains " + properties.size() + " keys.");
        finer("List of configuration properties, after override:");
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String val = properties.getProperty(key);
            finer("  " + key + " = " + val);
        }
        finer("Properties list complete.");
    }

    /**
     * Returns the value for key in the Configuration. A warning is issued to
     * the log if the property is not defined.
     *
     * @param key Name of the property.
     * @return Value assigned to the key, as a String.
     */
    public static String valueFor(String key) {
        Configuration conf = instance();
        String val = conf.properties.getProperty(key);
        if (val == null) {
            conf.warning("CONFIGURATION: no value found for key " + key);
        }
        return val;
    }

    public static boolean hasValue(String key) {
        Configuration conf = instance();
        String val = conf.properties.getProperty(key);
        if (val == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns the value for key in the Configuration as a byte, or the default
     * provided value if not found or if the value is not a valid byte. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static int valueAsByte(String key, byte defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        byte bval = -1;
        try {
            bval = Byte.valueOf(val).byteValue();
        } catch (NumberFormatException nex) {
            XRLog.exception("Property '" + key + "' was requested as a byte, but " + "value of '" + val + "' is not a byte. Check configuration.");
            bval = defaultVal;
        }
        return bval;
    }

    /**
     * Returns the value for key in the Configuration as a short, or the default
     * provided value if not found or if the value is not a valid short. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static int valueAsShort(String key, short defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        short sval = -1;
        try {
            sval = Short.valueOf(val).shortValue();
        } catch (NumberFormatException nex) {
            XRLog.exception("Property '" + key + "' was requested as a short, but " + "value of '" + val + "' is not a short. Check configuration.");
            sval = defaultVal;
        }
        return sval;
    }

    /**
     * Returns the value for key in the Configuration as an integer, or a
     * default value if not found or if the value is not a valid integer. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static int valueAsInt(String key, int defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        int ival = -1;
        try {
            ival = Integer.valueOf(val).intValue();
        } catch (NumberFormatException nex) {
            XRLog.exception("Property '" + key + "' was requested as an integer, but " + "value of '" + val + "' is not an integer. Check configuration.");
            ival = defaultVal;
        }
        return ival;
    }

    /**
     * Returns the value for key in the Configurationas a long, or the default
     * provided value if not found or if the value is not a valid long. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static long valueAsLong(String key, long defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        long lval = -1;
        try {
            lval = Long.valueOf(val).longValue();
        } catch (NumberFormatException nex) {
            XRLog.exception("Property '" + key + "' was requested as a long, but " + "value of '" + val + "' is not a long. Check configuration.");
            lval = defaultVal;
        }
        return lval;
    }

    /**
     * Returns the value for key in the Configuration as a float, or the default
     * provided value if not found or if the value is not a valid float. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static float valueAsFloat(String key, float defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        float fval = -1;
        try {
            fval = Float.valueOf(val).floatValue();
        } catch (NumberFormatException nex) {
            XRLog.exception("Property '" + key + "' was requested as a float, but " + "value of '" + val + "' is not a float. Check configuration.");
            fval = defaultVal;
        }
        return fval;
    }

    /**
     * Returns the value for key in the Configuration as a double, or the
     * default provided value if not found or if the value is not a valid
     * double. A warning is issued to the log if the property is not defined, or
     * if the conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static double valueAsDouble(String key, double defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        double dval = -1;
        try {
            dval = Double.valueOf(val).doubleValue();
        } catch (NumberFormatException nex) {
            XRLog.exception("Property '" + key + "' was requested as a double, but " + "value of '" + val + "' is not a double. Check configuration.");
            dval = defaultVal;
        }
        return dval;
    }

    /**
     * Returns the value for key in the Configuration, or the default provided
     * value if not found. A warning is issued to the log if the property is not
     * defined, and if the default is null.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static String valueFor(String key, String defaultVal) {
        Configuration conf = instance();
        String val = conf.properties.getProperty(key);
        val = (val == null ? defaultVal : val);
        if (val == null) {
            conf.warning("CONFIGURATION: no value found for key " + key + " and no default given.");
        }
        return val;
    }

    /**
     * Returns all configuration keys that start with prefix. Iterator will be
     * empty if no such keys are found.
     *
     * @param prefix Prefix to filter on. No regex.
     * @return Returns Iterator, see description.
     */
    public static Iterator keysByPrefix(String prefix) {
        Configuration conf = instance();
        Iterator iter = conf.properties.keySet().iterator();
        List l = new ArrayList();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (key.startsWith(prefix)) {
                l.add(key);
            }
        }
        return l.iterator();
    }

    /**
     * Command-line execution for testing. No arguments.
     *
     * @param args Ignored
     */
    public static void main(String args[]) {
        try {
            System.out.println("byte: " + String.valueOf(Configuration.valueAsByte("xr.test-config-byte", (byte) 15)));
            System.out.println("short: " + String.valueOf(Configuration.valueAsShort("xr.test-config-short", (short) 20)));
            System.out.println("int: " + String.valueOf(Configuration.valueAsInt("xr.test-config-int", 25)));
            System.out.println("long: " + String.valueOf(Configuration.valueAsLong("xr.test-config-long", 30L)));
            System.out.println("float: " + String.valueOf(Configuration.valueAsFloat("xr.test-config-float", 45.5F)));
            System.out.println("double: " + String.valueOf(Configuration.valueAsDouble("xr.test-config-double", 50.75D)));
            System.out.println("boolean: " + String.valueOf(Configuration.isTrue("xr.test-config-boolean", false)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns true if the value is "true" (ignores case), or the default
     * provided value if not found or if the value is not a valid boolean (true
     * or false, ignores case). A warning is issued to the log if the property
     * is not defined, and if the default is null.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static boolean isTrue(String key, boolean defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }
        if ("true|false".indexOf(val) == -1) {
            XRLog.exception("Property '" + key + "' was requested as a boolean, but " + "value of '" + val + "' is not a boolean. Check configuration.");
            return defaultVal;
        } else {
            return Boolean.valueOf(val).booleanValue();
        }
    }

    /**
     * Returns true if the value is not "true" (ignores case), or the default
     * provided value if not found or if the value is not a valid boolean (true
     * or false, ignores case). A warning is issued to the log if the property
     * is not defined, or the value is not a valid boolean.
     *
     * @param key        Name of the property.
     * @param defaultVal PARAM
     * @return Value assigned to the key, as a String.
     */
    public static boolean isFalse(String key, boolean defaultVal) {
        return !isTrue(key, defaultVal);
    }

    /**
     * @return The singleton instance of the class.
     */
    private static synchronized Configuration instance() {
        if (Configuration.sInstance == null) {
            Configuration.sInstance = new Configuration();
        }
        return Configuration.sInstance;
    }

    /**
     * Given a property, resolves the value to a public constant field on some class, where the field is of type Object.
     * The property value must the the FQN of the class and field, e.g.
     * aKey=java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR will return the value of the
     * VALUE_INTERPOLATION_NEAREST_NEIGHBOR constant on the RendingHints class.
     *
     * @param key Name of the property
     * @param defaultValue Returned in case of error.
     * @return Value of the constant, or defaultValue in case of error.
     */
    public static Object valueFromClassConstant(String key, Object defaultValue) {
        Configuration conf = instance();
        String val = valueFor(key);
        if (val == null) {
            return defaultValue;
        }
        int idx = val.lastIndexOf(".");
        String klassname;
        String cnst;
        try {
            klassname = val.substring(0, idx);
            cnst = val.substring(idx + 1);
        } catch (IndexOutOfBoundsException e) {
            conf.warning("Property key " + key + " for object value constant is not properly formatted; " + "should be FQN<dot>constant, is " + val);
            return defaultValue;
        }
        Class klass = null;
        try {
            klass = Class.forName(klassname);
        } catch (ClassNotFoundException e) {
            conf.warning("Property for object value constant " + key + " is not a FQN: " + klassname);
            return defaultValue;
        }
        Object cnstVal = null;
        try {
            Field fld = klass.getDeclaredField(cnst);
            try {
                cnstVal = fld.get(klass);
            } catch (IllegalAccessException e) {
                conf.warning("Property for object value constant " + key + ", field is not public: " + klassname + "." + cnst);
                return defaultValue;
            }
        } catch (NoSuchFieldException e) {
            conf.warning("Property for object value constant " + key + " is not a FQN: " + klassname);
            return defaultValue;
        }
        return cnstVal;
    }

    /**
     * Returns a Properties instance filled with values of last resort--in case we can't read default properties
     * file for some reason; this is to prevent Configuration init from throwing any exceptions, or ending up
     * with a completely empty configuration instance.
     */
    private Properties newFallbackProperties() {
        Properties props = new Properties();
        props.setProperty("xr.css.user-agent-default-css", "/resources/css/");
        props.setProperty("xr.test.files.hamlet", "/demos/browser/xhtml/hamlet.xhtml");
        props.setProperty("xr.simple-log-format", "{1} {2}:: {5}");
        props.setProperty("xr.simple-log-format-throwable", "{1} {2}:: {5}");
        props.setProperty("xr.test-config-byte", "8");
        props.setProperty("xr.test-config-short", "16");
        props.setProperty("xr.test-config-int", "100");
        props.setProperty("xr.test-config-long", "2000");
        props.setProperty("xr.test-config-float", "3000.25F");
        props.setProperty("xr.test-config-double", "4000.50D");
        props.setProperty("xr.test-config-boolean", "true");
        props.setProperty("xr.util-logging.loggingEnabled", "false");
        props.setProperty("xr.util-logging.handlers", "java.util.logging.ConsoleHandler");
        props.setProperty("xr.util-logging.use-parent-handler", "false");
        props.setProperty("xr.util-logging.java.util.logging.ConsoleHandler.level", "INFO");
        props.setProperty("xr.util-logging.java.util.logging.ConsoleHandler.formatter", "org.xhtmlrenderer.util.XRSimpleLogFormatter");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.config.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.exception.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.general.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.init.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.load.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.load.xml-entities.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.match.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.cascade.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.css-parse.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.layout.level", "ALL");
        props.setProperty("xr.util-logging.org.xhtmlrenderer.render.level", "ALL");
        props.setProperty("xr.load.xml-reader", "default");
        props.setProperty("xr.load.configure-features", "false");
        props.setProperty("xr.load.validation", "false");
        props.setProperty("xr.load.string-interning", "false");
        props.setProperty("xr.load.namespaces", "false");
        props.setProperty("xr.load.namespace-prefixes", "false");
        props.setProperty("xr.layout.whitespace.experimental", "true");
        props.setProperty("xr.layout.bad-sizing-hack", "false");
        props.setProperty("xr.renderer.viewport-repaint", "true");
        props.setProperty("xr.renderer.draw.backgrounds", "true");
        props.setProperty("xr.renderer.draw.borders", "true");
        props.setProperty("xr.renderer.debug.box-outlines", "false");
        props.setProperty("xr.text.scale", "1.0");
        props.setProperty("xr.text.aa-smoothing-level", "1");
        props.setProperty("xr.text.aa-fontsize-threshhold", "25");
        props.setProperty("xr.text.aa-rendering-hint", "RenderingHints.VALUE_TEXT_ANTIALIAS_HGRB");
        props.setProperty("xr.cache.stylesheets", "false");
        props.setProperty("xr.incremental.enabled", "false");
        props.setProperty("xr.incremental.lazyimage", "false");
        props.setProperty("xr.incremental.debug.layoutdelay", "0");
        props.setProperty("xr.incremental.repaint.print-timing", "false");
        props.setProperty("xr.use.threads", "false");
        props.setProperty("xr.use.listeners", "true");
        props.setProperty("xr.image.buffered", "false");
        props.setProperty("xr.image.scale", "LOW");
        props.setProperty("xr.image.render-quality", "java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR");
        return props;
    }
}
