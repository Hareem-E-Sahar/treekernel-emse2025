package com.sun.security.auth.login;

import javax.security.auth.AuthPermission;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import sun.security.util.Debug;
import sun.security.util.ResourcesMgr;
import sun.security.util.PropertyExpander;

/**
 * This class represents a default implementation for
 * <code>javax.security.auth.login.Configuration</code>.
 *
 * <p> This object stores the runtime login configuration representation,
 * and is the amalgamation of multiple static login
 * configurations that resides in files.
 * The algorithm for locating the login configuration file(s) and reading their
 * information into this <code>Configuration</code> object is:
 *
 * <ol>
 * <li>
 *   Loop through the <code>java.security.Security</code> properties,
 *   <i>login.config.url.1</i>, <i>login.config.url.2</i>, ...,
 *   <i>login.config.url.X</i>.  These properties are set
 *   in the Java security properties file, which is located in the file named
 *   &lt;JAVA_HOME&gt;/lib/security/java.security.
 *   &lt;JAVA_HOME&gt; refers to the value of the java.home system property,
 *   and specifies the directory where the JRE is installed.
 *   Each property value specifies a <code>URL</code> pointing to a
 *   login configuration file to be loaded.  Read in and load
 *   each configuration.
 *
 * <li>
 *   The <code>java.lang.System</code> property
 *   <i>java.security.auth.login.config</i>
 *   may also be set to a <code>URL</code> pointing to another
 *   login configuration file
 *   (which is the case when a user uses the -D switch at runtime).
 *   If this property is defined, and its use is allowed by the
 *   security property file (the Security property,
 *   <i>policy.allowSystemProperty</i> is set to <i>true</i>),
 *   also load that login configuration.
 *
 * <li>
 *   If the <i>java.security.auth.login.config</i> property is defined using
 *   "==" (rather than "="), then ignore all other specified
 *   login configurations and only load this configuration.
 *
 * <li>
 *   If no system or security properties were set, try to read from the file,
 *   ${user.home}/.java.login.config, where ${user.home} is the value
 *   represented by the "user.home" System property.
 * </ol>
 * 
 * <p> The configuration syntax supported by this implementation
 * is exactly that syntax specified in the
 * <code>javax.security.auth.login.Configuration</code> class.
 * 
 * @version 1.22, 11/17/05
 * @see javax.security.auth.login.LoginContext
 */
public class ConfigFile extends javax.security.auth.login.Configuration {

    private StreamTokenizer st;

    private int lookahead;

    private int linenum;

    private HashMap configuration;

    private boolean expandProp = true;

    private URL url;

    private static Debug debugConfig = Debug.getInstance("configfile");

    private static Debug debugParser = Debug.getInstance("configparser");

    /**
     * Create a new <code>Configuration</code> object.
     */
    public ConfigFile() {
        try {
            init(url);
        } catch (IOException ioe) {
            throw (SecurityException) new SecurityException(ioe.getMessage()).initCause(ioe);
        }
    }

    /**
     * Create a new <code>Configuration</code> object from the specified URI.
     *
     * @param uri Create a new Configuration object from this URI.
     */
    public ConfigFile(URI uri) {
        try {
            url = uri.toURL();
            init(url);
        } catch (MalformedURLException mue) {
            throw (SecurityException) new SecurityException(mue.getMessage()).initCause(mue);
        } catch (IOException ioe) {
            throw (SecurityException) new SecurityException(ioe.getMessage()).initCause(ioe);
        }
    }

    /**
     * Read and initialize the entire login Configuration.
     *
     * <p>
     *
     * @exception IOException if the Configuration can not be initialized. <p>
     * @exception SecurityException if the caller does not have permission
     *				to initialize the Configuration.
     */
    private void init(URL url) throws IOException {
        boolean initialized = false;
        FileReader fr = null;
        String sep = File.separator;
        if ("false".equals(System.getProperty("policy.expandProperties"))) {
            expandProp = false;
        }
        HashMap newConfig = new HashMap();
        if (url != null) {
            if (debugConfig != null) {
                debugConfig.println("reading " + url);
            }
            init(url, newConfig);
            configuration = newConfig;
            return;
        }
        String allowSys = java.security.Security.getProperty("policy.allowSystemProperty");
        if ("true".equalsIgnoreCase(allowSys)) {
            String extra_config = System.getProperty("java.security.auth.login.config");
            if (extra_config != null) {
                boolean overrideAll = false;
                if (extra_config.startsWith("=")) {
                    overrideAll = true;
                    extra_config = extra_config.substring(1);
                }
                try {
                    extra_config = PropertyExpander.expand(extra_config);
                } catch (PropertyExpander.ExpandException peee) {
                    MessageFormat form = new MessageFormat(ResourcesMgr.getString("Unable to properly expand config", "sun.security.util.AuthResources"));
                    Object[] source = { extra_config };
                    throw new IOException(form.format(source));
                }
                URL configURL = null;
                try {
                    configURL = new URL(extra_config);
                } catch (java.net.MalformedURLException mue) {
                    File configFile = new File(extra_config);
                    if (configFile.exists()) {
                        configURL = new URL("file:" + configFile.getCanonicalPath());
                    } else {
                        MessageFormat form = new MessageFormat(ResourcesMgr.getString("extra_config (No such file or directory)", "sun.security.util.AuthResources"));
                        Object[] source = { extra_config };
                        throw new IOException(form.format(source));
                    }
                }
                if (debugConfig != null) {
                    debugConfig.println("reading " + configURL);
                }
                init(configURL, newConfig);
                initialized = true;
                if (overrideAll) {
                    if (debugConfig != null) {
                        debugConfig.println("overriding other policies!");
                    }
                    configuration = newConfig;
                    return;
                }
            }
        }
        int n = 1;
        String config_url;
        while ((config_url = java.security.Security.getProperty("login.config.url." + n)) != null) {
            try {
                config_url = PropertyExpander.expand(config_url).replace(File.separatorChar, '/');
                if (debugConfig != null) {
                    debugConfig.println("\tReading config: " + config_url);
                }
                init(new URL(config_url), newConfig);
                initialized = true;
            } catch (PropertyExpander.ExpandException peee) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString("Unable to properly expand config", "sun.security.util.AuthResources"));
                Object[] source = { config_url };
                throw new IOException(form.format(source));
            }
            n++;
        }
        if (initialized == false && n == 1 && config_url == null) {
            if (debugConfig != null) {
                debugConfig.println("\tReading Policy " + "from ~/.java.login.config");
            }
            config_url = System.getProperty("user.home");
            try {
                init(new URL("file:" + config_url + File.separatorChar + ".java.login.config"), newConfig);
            } catch (IOException ioe) {
                throw new IOException(ResourcesMgr.getString("Unable to locate a login configuration", "sun.security.util.AuthResources"));
            }
        }
        configuration = newConfig;
    }

    private void init(URL config, HashMap newConfig) throws IOException {
        InputStreamReader isr = new InputStreamReader(getInputStream(config), "UTF-8");
        readConfig(isr, newConfig);
        isr.close();
    }

    /**
     * Retrieve an entry from the Configuration using an application name
     * as an index.
     *
     * <p>
     *
     * @param applicationName the name used to index the Configuration.
     * @return an array of AppConfigurationEntries which correspond to
     *		the stacked configuration of LoginModules for this
     *		application, or null if this application has no configured
     *		LoginModules.
     */
    public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
        LinkedList list = null;
        synchronized (configuration) {
            list = (LinkedList) configuration.get(applicationName);
        }
        if (list == null || list.size() == 0) return null;
        AppConfigurationEntry[] entries = new AppConfigurationEntry[list.size()];
        Iterator iterator = list.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            AppConfigurationEntry e = (AppConfigurationEntry) iterator.next();
            entries[i] = new AppConfigurationEntry(e.getLoginModuleName(), e.getControlFlag(), e.getOptions());
        }
        return entries;
    }

    /**
     * Refresh and reload the Configuration by re-reading all of the
     * login configurations.
     *
     * <p>
     *
     * @exception SecurityException if the caller does not have permission
     *				to refresh the Configuration.
     */
    public synchronized void refresh() {
        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new AuthPermission("refreshLoginConfiguration"));
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                try {
                    init(url);
                } catch (java.io.IOException ioe) {
                    throw (SecurityException) new SecurityException(ioe.getLocalizedMessage()).initCause(ioe);
                }
                return null;
            }
        });
    }

    private void readConfig(Reader reader, HashMap newConfig) throws IOException {
        int linenum = 1;
        if (!(reader instanceof BufferedReader)) reader = new BufferedReader(reader);
        st = new StreamTokenizer(reader);
        st.quoteChar('"');
        st.wordChars('$', '$');
        st.wordChars('_', '_');
        st.wordChars('-', '-');
        st.lowerCaseMode(false);
        st.slashSlashComments(true);
        st.slashStarComments(true);
        st.eolIsSignificant(true);
        lookahead = nextToken();
        while (lookahead != StreamTokenizer.TT_EOF) {
            parseLoginEntry(newConfig);
        }
    }

    private void parseLoginEntry(HashMap newConfig) throws IOException {
        String appName;
        String moduleClass;
        String sflag;
        AppConfigurationEntry.LoginModuleControlFlag controlFlag;
        LinkedList configEntries = new LinkedList();
        appName = st.sval;
        lookahead = nextToken();
        if (debugParser != null) {
            debugParser.println("\tReading next config entry: " + appName);
        }
        match("{");
        while (peek("}") == false) {
            HashSet argSet = new HashSet();
            moduleClass = match("module class name");
            sflag = match("controlFlag");
            if (sflag.equalsIgnoreCase("REQUIRED")) controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED; else if (sflag.equalsIgnoreCase("REQUISITE")) controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE; else if (sflag.equalsIgnoreCase("SUFFICIENT")) controlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT; else if (sflag.equalsIgnoreCase("OPTIONAL")) controlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL; else {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tInvalid control flag, flag", "sun.security.util.AuthResources"));
                Object[] source = { sflag };
                throw new IOException(form.format(source));
            }
            HashMap options = new HashMap();
            String key;
            String value;
            while (peek(";") == false) {
                key = match("option key");
                match("=");
                try {
                    value = expand(match("option value"));
                } catch (PropertyExpander.ExpandException peee) {
                    throw new IOException(peee.getLocalizedMessage());
                }
                options.put(key, value);
            }
            lookahead = nextToken();
            if (debugParser != null) {
                debugParser.println("\t\t" + moduleClass + ", " + sflag);
                java.util.Iterator i = options.keySet().iterator();
                while (i.hasNext()) {
                    key = (String) i.next();
                    debugParser.println("\t\t\t" + key + "=" + (String) options.get(key));
                }
            }
            AppConfigurationEntry entry = new AppConfigurationEntry(moduleClass, controlFlag, options);
            configEntries.add(entry);
        }
        match("}");
        match(";");
        if (newConfig.containsKey(appName)) {
            MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\t" + "Can not specify multiple entries for appName", "sun.security.util.AuthResources"));
            Object[] source = { appName };
            throw new IOException(form.format(source));
        }
        newConfig.put(appName, configEntries);
    }

    private String match(String expect) throws IOException {
        String value = null;
        switch(lookahead) {
            case StreamTokenizer.TT_EOF:
                MessageFormat form1 = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\texpected [expect], " + "read [end of file]", "sun.security.util.AuthResources"));
                Object[] source1 = { expect };
                throw new IOException(form1.format(source1));
            case '"':
            case StreamTokenizer.TT_WORD:
                if (expect.equalsIgnoreCase("module class name") || expect.equalsIgnoreCase("controlFlag") || expect.equalsIgnoreCase("option key") || expect.equalsIgnoreCase("option value")) {
                    value = st.sval;
                    lookahead = nextToken();
                } else {
                    MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: " + "expected [expect], found [value]", "sun.security.util.AuthResources"));
                    Object[] source = { new Integer(linenum), expect, st.sval };
                    throw new IOException(form.format(source));
                }
                break;
            case '{':
                if (expect.equalsIgnoreCase("{")) {
                    lookahead = nextToken();
                } else {
                    MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: expected [expect]", "sun.security.util.AuthResources"));
                    Object[] source = { new Integer(linenum), expect, st.sval };
                    throw new IOException(form.format(source));
                }
                break;
            case ';':
                if (expect.equalsIgnoreCase(";")) {
                    lookahead = nextToken();
                } else {
                    MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: expected [expect]", "sun.security.util.AuthResources"));
                    Object[] source = { new Integer(linenum), expect, st.sval };
                    throw new IOException(form.format(source));
                }
                break;
            case '}':
                if (expect.equalsIgnoreCase("}")) {
                    lookahead = nextToken();
                } else {
                    MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: expected [expect]", "sun.security.util.AuthResources"));
                    Object[] source = { new Integer(linenum), expect, st.sval };
                    throw new IOException(form.format(source));
                }
                break;
            case '=':
                if (expect.equalsIgnoreCase("=")) {
                    lookahead = nextToken();
                } else {
                    MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: expected [expect]", "sun.security.util.AuthResources"));
                    Object[] source = { new Integer(linenum), expect, st.sval };
                    throw new IOException(form.format(source));
                }
                break;
            default:
                MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: " + "expected [expect], found [value]", "sun.security.util.AuthResources"));
                Object[] source = { new Integer(linenum), expect, st.sval };
                throw new IOException(form.format(source));
        }
        return value;
    }

    private boolean peek(String expect) {
        boolean found = false;
        switch(lookahead) {
            case ',':
                if (expect.equalsIgnoreCase(",")) found = true;
                break;
            case ';':
                if (expect.equalsIgnoreCase(";")) found = true;
                break;
            case '{':
                if (expect.equalsIgnoreCase("{")) found = true;
                break;
            case '}':
                if (expect.equalsIgnoreCase("}")) found = true;
                break;
            default:
        }
        return found;
    }

    private int nextToken() throws IOException {
        int tok;
        while ((tok = st.nextToken()) == StreamTokenizer.TT_EOL) {
            linenum++;
        }
        return tok;
    }

    private InputStream getInputStream(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            String path = url.getFile().replace('/', File.separatorChar);
            return new FileInputStream(path);
        } else {
            return url.openStream();
        }
    }

    private String expand(String value) throws PropertyExpander.ExpandException, IOException {
        if ("".equals(value)) {
            return value;
        }
        if (expandProp) {
            String s = PropertyExpander.expand(value);
            if (s == null || s.length() == 0) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString("Configuration Error:\n\tLine line: " + "system property [value] expanded to empty value", "sun.security.util.AuthResources"));
                Object[] source = { new Integer(linenum), value };
                throw new IOException(form.format(source));
            }
            return s;
        } else {
            return value;
        }
    }
}
