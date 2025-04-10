package org.sqsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.digester.Digester;

/**
 *  The SQLDriverManager provides an interface for registering SQL drivers
 *  and grabbing connections from said drivers.
 */
public class SQLDriverManager {

    private static final Logger LOG = Logger.getLogger(SQLDriverManager.class.getName());

    /**
     * This is the map of logical driver names to driver defitions.
     */
    private Map<String, SQLDriver> drivers = new HashMap<String, SQLDriver>();

    /**
     * If not null, this controls the default database that connections 
     * will be placed in.
     */
    private String defaultDatabase = null;

    /**
     * This is the default autocommit setting for all connections 
     * created by the drivers.
     */
    private boolean defaultAutoCommit = true;

    /**
     * This is the class loader that is used to attempt to load our JDBC
     * drivers. Initially we will have no locations defined, but additional
     * locations can be added by the user by calling setClasspath();
     */
    private URLClassLoader classLoader = new URLClassLoader(new URL[0], getClass().getClassLoader());

    /**
     * Wrapper class around drivers that are to be loaded with my custom
     * class loader. This idea was taken from:
     * 
     *    http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
     */
    public static class DriverShim implements Driver {

        private Driver driver;

        public DriverShim(Driver driver) {
            this.driver = driver;
        }

        public boolean acceptsURL(String arg0) throws SQLException {
            return driver.acceptsURL(arg0);
        }

        public Connection connect(String arg0, Properties arg1) throws SQLException {
            return driver.connect(arg0, arg1);
        }

        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException {
            return driver.getPropertyInfo(arg0, arg1);
        }

        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }
    }

    public SQLDriverManager() {
        URL url = getClass().getClassLoader().getResource("org/sqsh/Drivers.xml");
        if (url == null) {
            System.err.println("ERROR: Unable to load internal drivers " + "file!!");
        } else {
            loadDrivers(url, true);
        }
    }

    /**
     * Returns the default autocommit setting for new connections created
     * using the manager.
     * 
     * @return The default autocommit setting for all connections created
     */
    public boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    /**
     * Changes the default autocommit setting for new connections
     * created using the manager.
     * @param autocommit The default autocommit setting
     */
    public void setDefaultAutoCommit(boolean autocommit) {
        this.defaultAutoCommit = autocommit;
    }

    /**
     * Sets the dafault database (catalog) that will be used by the 
     * connection when established.
     * 
     * @param db The database name or null.
     */
    public void setDefaultDatabase(String db) {
        if (db == null || "".equals(db) || "null".equals(db)) {
            this.defaultDatabase = null;
        } else {
            this.defaultDatabase = db;
        }
    }

    /**
     * Returns the default database.
     * 
     * @return The default database.
     */
    public String getDefaultDatabase() {
        return this.defaultDatabase;
    }

    /**
     * Sets a new classpath that will be used to search for JDBC drivers.
     * 
     * @param classpath A delimited list of jars or directories containing
     *   jars. The delimiter that is used should be your system's path
     *   delimiter.
     * 
     * @throws IOException if there is a problem
     */
    public void setClasspath(String classpath) throws IOException {
        List<URL> urls = new ArrayList<URL>();
        String[] locations = classpath.split(System.getProperty("path.separator"));
        for (String location : locations) {
            File file = new File(location);
            if (file.isDirectory()) {
                File[] dirContents = file.listFiles();
                for (File subFile : dirContents) {
                    if (subFile.isFile() && subFile.getName().endsWith(".jar")) {
                        urls.add(subFile.toURI().toURL());
                    }
                }
            } else if (file.exists()) {
                urls.add(file.toURI().toURL());
            }
        }
        classLoader = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
        checkDriverAvailability();
    }

    /**
     * Trys to load a driver class using the driver manager's
     * classpath.
     * 
     * @param name The name of the class to load.
     * @throws Exception Thrown if the class cannot be loaded.
     */
    public void loadClass(String name) throws Exception {
        Class.forName(name, true, classLoader);
    }

    /**
     * Used to check whether or not all of the currently registered
     * JDBC drivers is available in the current classloader.
     */
    private void checkDriverAvailability() {
        for (SQLDriver driver : drivers.values()) {
            try {
                Class<? extends Driver> driverClass = Class.forName(driver.getDriverClass(), true, classLoader).asSubclass(Driver.class);
                Driver d = driverClass.newInstance();
                DriverManager.registerDriver(new DriverShim(d));
                driver.setAvailable(true);
            } catch (Exception e) {
                LOG.fine("Unable to load " + driver.getDriverClass() + ": " + e.getMessage());
                driver.setAvailable(false);
            }
        }
    }

    /**
     * Returns the current classpath. The classpath will have been expanded
     * of all jars contained in the directories specified by the original call
     * to setClasspath().
     * 
     * @return the current classpath.
     */
    public String getClasspath() {
        URL[] urls = classLoader.getURLs();
        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("path.separator");
        for (int i = 0; i < urls.length; i++) {
            if (i > 0) {
                sb.append(sep);
            }
            sb.append(urls[i].toString());
        }
        return sb.toString();
    }

    /**
     * Attempts to connect to the database utilizing a driver.
     * 
     * @param driver The name of the driver to utilize. The special
     *    driver name "generic" can be provided if the manager does not
     *    have a registered driver that can be used. 
     * @param session The session for which the connection is being 
     *    established. The session is primarly used as a place to
     *    send error messages.
     * @param properties Connection properties to be utilized.
     * @return A newly created connection.
     * @throws SQLException Thrown if the connection could not be 
     *    established.
     */
    public SQLConnectionContext connect(Session session, ConnectionDescriptor connDesc) throws SQLException {
        SQLDriver sqlDriver = null;
        if (connDesc.getDriver() == null) {
            connDesc.setDriver(session.getVariable("dflt_driver"));
        }
        if (connDesc.getDriver() == null && connDesc.getUrl() == null) {
            throw new SQLException("Either an explicit JSqsh driver name must be supplied " + "(via --driver) or a JDBC fully qualified JDBC url " + "must be provided (via --jdbc-url). In most cases the " + "JDBC url must also be accompanied with the " + "name of the JDBC driver class (via --jdbc-class");
        }
        if (connDesc.getDriver() == null) {
            connDesc.setDriver("generic");
        }
        sqlDriver = getDriver(connDesc.getDriver());
        if (sqlDriver == null) {
            throw new SQLException("JSqsh has no driver registered under " + "the name '" + connDesc.getDriver() + "'. To see a list of available drivers, use the " + "\\drivers command");
        }
        if (connDesc.getJdbcClass() != null) {
            try {
                Class<? extends Driver> driverClass = Class.forName(connDesc.getJdbcClass(), true, classLoader).asSubclass(Driver.class);
                Driver d = driverClass.newInstance();
                DriverManager.registerDriver(new DriverShim(d));
            } catch (Exception e) {
                throw new SQLException("Cannot load JDBC driver class '" + connDesc.getJdbcClass() + "': " + e.getMessage());
            }
        }
        String url = connDesc.getUrl();
        if (url == null) {
            url = sqlDriver.getUrl();
        }
        Map<String, String> properties = toProperties(session, connDesc);
        url = getUrl(session, properties, sqlDriver.getVariables(), url);
        Connection conn = null;
        try {
            Driver jdbcDriver = DriverManager.getDriver(url);
            Properties props = new Properties();
            try {
                DriverPropertyInfo[] supportedProperties = jdbcDriver.getPropertyInfo(url, null);
                for (int i = 0; i < supportedProperties.length; i++) {
                    String name = supportedProperties[i].name;
                    String value = getProperty(properties, sqlDriver.getVariables(), name);
                    if (value != null) {
                        LOG.fine("Setting connection property '" + name + "' to '" + value + "'");
                        props.put(name, value);
                    }
                }
            } catch (Exception e) {
                session.err.println("WARNING: Failed to retrieve JDBC driver " + "supported connection property list (" + e.getMessage() + ")");
            }
            for (String name : sqlDriver.getPropertyNames()) {
                props.put(name, sqlDriver.getProperty(name));
            }
            String s = getProperty(properties, sqlDriver.getVariables(), SQLDriver.USER_PROPERTY);
            if (s == null) {
                s = promptInput("Username", false);
                connDesc.setUsername(s);
            }
            props.put(SQLDriver.USER_PROPERTY, s);
            s = getProperty(properties, sqlDriver.getVariables(), SQLDriver.PASSWORD_PROPERTY);
            if (s == null) {
                s = promptInput("Password", true);
            }
            props.put(SQLDriver.PASSWORD_PROPERTY, s);
            conn = DriverManager.getConnection(url, props);
            SQLTools.printWarnings(session, conn);
        } catch (SQLException e) {
            throw new SQLException("Unable to connect via JDBC url '" + url + "': " + e.getMessage(), e.getSQLState(), e.getErrorCode());
        }
        String database = getProperty(properties, sqlDriver.getVariables(), SQLDriver.DATABASE_PROPERTY);
        if (database == null && defaultDatabase != null) {
            database = defaultDatabase;
        }
        if (database != null) {
        }
        try {
            conn.setAutoCommit(defaultAutoCommit);
            SQLTools.printWarnings(session, conn);
        } catch (SQLException e) {
            session.err.println("WARNING: Unable to set auto-commit mode to " + defaultAutoCommit);
        }
        ConnectionContext oldContext = session.getConnectionContext();
        SQLConnectionContext newContext = new SQLConnectionContext(session, connDesc, conn, url, sqlDriver.getAnalyzer());
        session.setConnectionContext(newContext, false);
        try {
            Iterator<String> varIter = sqlDriver.getSessionVariables().keySet().iterator();
            while (varIter.hasNext()) {
                String name = varIter.next();
                session.setVariable(name, sqlDriver.getSessionVariable(name));
            }
        } finally {
            session.setConnectionContext(oldContext, false);
        }
        return newContext;
    }

    /**
     * Given the connection settings that the user provided, creates a map of
     * properties that are required by {@link SQLDriverManager#connect(String,
     * Session, Map)} in order to establish a connection. For a given 
     * property, such as {@link SQLDriver#SERVER_PROPERTY}, the value is
     * established by the first of the following that is available:
     * 
     * <ol>
     *   <li> Using the option provided in the {@link ConnectionDescriptor}
     *   <li> Looking the in the session for variable of the name
     *        "dflt_&lt;property&gt;" (e.g. "dflt_server"). 
     * </ol>
     * 
     * If none of those is available, then the property is not passed in 
     * and it is up to the {@link SQLDriver} to provide a default.
     * 
     * @param session The session used to look up the properties.
     * @param options The options the user provided.
     * @return A map of properties.
     */
    private Map<String, String> toProperties(Session session, ConnectionDescriptor connDesc) {
        Map<String, String> properties = new HashMap<String, String>();
        setProperty(properties, session, SQLDriver.SERVER_PROPERTY, connDesc.getServer());
        setProperty(properties, session, SQLDriver.PORT_PROPERTY, (connDesc.getPort() == -1 ? null : Integer.toString(connDesc.getPort())));
        setProperty(properties, session, SQLDriver.USER_PROPERTY, connDesc.getUsername());
        setProperty(properties, session, SQLDriver.PASSWORD_PROPERTY, connDesc.getPassword());
        setProperty(properties, session, SQLDriver.SID_PROPERTY, connDesc.getSid());
        setProperty(properties, session, SQLDriver.DATABASE_PROPERTY, connDesc.getCatalog());
        setProperty(properties, session, SQLDriver.DOMAIN_PROPERTY, connDesc.getDomain());
        return properties;
    }

    /**
     * Helper for toProperties() to utilize dflt_ variables to provide
     * a property name.
     * 
     * @param map The map to populate.
     * @param session The session from which dflt_ variables are fetched.
     * @param name The name of the property.
     * @param value The value of the property to set.
     */
    private void setProperty(Map<String, String> map, Session session, String name, String value) {
        if (value == null) {
            value = session.getVariable("dflt_" + name);
        }
        if (value != null) {
            map.put(name, value);
        }
    }

    /**
     * Similar to DriverManager.getDriver() except that it searches
     * through our shiny new classloader.
     * 
     * @param url
     * @return
     * @throws SQLException
     */
    private Driver getDriverFromUrl(String url) throws SQLException {
        for (SQLDriver driver : drivers.values()) {
            try {
                Driver d = (Driver) Class.forName(driver.getDriverClass(), true, classLoader).newInstance();
                if (d.acceptsURL(url)) {
                    return d;
                }
            } catch (Exception e) {
            }
        }
        return DriverManager.getDriver(url);
    }

    /**
     * Used to prompt input from a user.
     * 
     * @param prompt The prompt to use.
     * @param isMasked true if the input is to be masked.
     * @return The input
     */
    private static String promptInput(String prompt, boolean isMasked) {
        try {
            if (isMasked == false) {
                System.out.print(prompt);
                System.out.print(": ");
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                String results = input.readLine();
                return results;
            } else {
                char[] pwd = PasswordInput.getPassword(System.in, prompt + ": ");
                if (pwd == null) {
                    return null;
                }
                return new String(pwd);
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Used to expand a URL of any variables that may be defined in the 
     *  user supplied properties, session, or SQLDriver variables (in that
     *  order).
     *  
     * @param properties The user supplied properties.
     * @param vars SQLDriver variables (can be null).
     * @param url The URL to expand.
     * @return The URL expanded of all variables.
     */
    private String getUrl(Session session, Map<String, String> properties, Map<String, String> vars, String url) {
        Map<String, String> connProperties = new HashMap<String, String>();
        connProperties.putAll(properties);
        if (vars != null) {
            for (String name : vars.keySet()) {
                String value = getProperty(properties, vars, name);
                connProperties.put(name, value);
            }
        }
        connProperties.put(SQLDriver.USER_PROPERTY, getProperty(properties, vars, SQLDriver.USER_PROPERTY));
        connProperties.put(SQLDriver.PASSWORD_PROPERTY, getProperty(properties, vars, SQLDriver.PASSWORD_PROPERTY));
        return session.expand(connProperties, url);
    }

    /**
     * Used internally to look up a property value following an inheritence
     * scheme of (1) properties passed by user, (2) the variables 
     * associated with the driver.
     * 
     * @param vars Variables defined by a driver (can be null).
     * @param properties The properties passed by the user.
     * @param name Variable to look up.
     * @return The value of the property.
     */
    private String getProperty(Map<String, String> properties, Map<String, String> vars, String name) {
        String value = properties.get(name);
        if (value == null) {
            value = vars.get(name);
        }
        return value;
    }

    /**
     * Adds a driver to the manager.
     * @param driver The new driver.
     */
    public void addDriver(SQLDriver driver) {
        drivers.put(driver.getName(), driver);
        driver.setDriverManager(this);
    }

    /**
     * Fetches a driver by name
     * @param name
     * @return
     */
    public SQLDriver getDriver(String name) {
        return drivers.get(name);
    }

    /**
     * Returns the set of drivers currently registered.
     * @return the set of drivers currently registered.
     */
    public SQLDriver[] getDrivers() {
        return drivers.values().toArray(new SQLDriver[0]);
    }

    /**
     * Loads a driver configuration file.
     * @param file The file to load.
     */
    public void load(File file) {
        try {
            loadDrivers(file.toURI().toURL(), false);
        } catch (MalformedURLException e) {
        }
    }

    private void loadDrivers(URL url, boolean isInternal) {
        String path;
        Digester digester = new Digester();
        digester.setValidating(false);
        path = "Drivers/Driver";
        digester.addObjectCreate(path, "org.sqsh.SQLDriver");
        digester.addSetNext(path, "addDriver", "org.sqsh.SQLDriver");
        digester.addCallMethod(path, "setName", 1, new Class[] { java.lang.String.class });
        digester.addCallParam(path, 0, "name");
        digester.addCallMethod(path, "setUrl", 1, new Class[] { java.lang.String.class });
        digester.addCallParam(path, 0, "url");
        digester.addCallMethod(path, "setDriverClass", 1, new Class[] { java.lang.String.class });
        digester.addCallParam(path, 0, "class");
        digester.addCallMethod(path, "setTarget", 1, new Class[] { java.lang.String.class });
        digester.addCallParam(path, 0, "target");
        digester.addCallMethod(path, "setAnalyzer", 1, new Class[] { java.lang.String.class });
        digester.addCallParam(path, 0, "analyzer");
        path = "Drivers/Driver/Variable";
        digester.addCallMethod(path, "setVariable", 2, new Class[] { java.lang.String.class, java.lang.String.class });
        digester.addCallParam(path, 0, "name");
        digester.addCallParam(path, 1);
        path = "Drivers/Driver/SessionVariable";
        digester.addCallMethod(path, "setSessionVariable", 2, new Class[] { java.lang.String.class, java.lang.String.class });
        digester.addCallParam(path, 0, "name");
        digester.addCallParam(path, 1);
        path = "Drivers/Driver/Property";
        digester.addCallMethod(path, "setProperty", 2, new Class[] { java.lang.String.class, java.lang.String.class });
        digester.addCallParam(path, 0, "name");
        digester.addCallParam(path, 1);
        digester.push(this);
        InputStream in = null;
        try {
            in = url.openStream();
            digester.parse(in);
        } catch (Exception e) {
            System.err.println("Failed to parse driver file '" + url.toString() + "': " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }
}
