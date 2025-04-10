package edacc.model;

import edacc.EDACCApp;
import edacc.satinstances.PropertyValueTypeManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Properties;
import java.util.Vector;

/**
 * singleton class handling the database connection.
 * It is possible to get a notification of a change of the connection state by adding an Observer to this class.
 * @author daniel
 */
public class DatabaseConnector extends Observable {

    public static final int CONNECTION_TIMEOUT = 60000;

    private static DatabaseConnector instance = null;

    private int maxconnections;

    private LinkedList<ThreadConnection> connections;

    private String hostname;

    private int port;

    private String database;

    private String username;

    private String password;

    private Properties properties;

    private final Object sync = new Object();

    private ConnectionWatchDog watchDog;

    private Boolean isCompetitionDB;

    private Integer modelVersion;

    private DatabaseConnector() {
        connections = new LinkedList<ThreadConnection>();
    }

    public static DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    /**
     * Creates a connection to a specified DB.
     * @param hostname the hostname of the DB server.
     * @param port the port of the DB server.
     * @param username the username of the DB user.
     * @param database the name of the database containing the EDACC tables.
     * @param password the password of the DB user.
     * @param doCheckVersion whether to perform a database version check
     * @throws ClassNotFoundException if the driver couldn't be found.
     * @throws SQLException if an error occurs while trying to establish the connection.
     */
    public void connect(String hostname, int port, String username, String database, String password, boolean useSSL, boolean compress, int maxconnections, boolean doCheckVersion, boolean rewriteBatchStatements) throws ClassNotFoundException, SQLException, DBVersionException, DBVersionUnknownException, DBEmptyException {
        while (connections.size() > 0) {
            ThreadConnection tconn = connections.pop();
            tconn.conn.close();
        }
        if (watchDog != null) {
            watchDog.terminate();
        }
        try {
            this.isCompetitionDB = null;
            this.modelVersion = null;
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;
            this.database = database;
            properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            if (rewriteBatchStatements) {
                properties.put("rewriteBatchedStatements", "true");
            }
            if (useSSL) {
                properties.put("useSSL", "true");
                properties.put("requireSSL", "true");
            }
            if (compress) {
                properties.put("useCompression", "true");
            }
            Class.forName("com.mysql.jdbc.Driver");
            this.maxconnections = maxconnections;
            watchDog = new ConnectionWatchDog();
            connections.add(new ThreadConnection(Thread.currentThread(), getNewConnection(), System.currentTimeMillis()));
            watchDog.start();
            if (doCheckVersion) checkVersion();
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            this.setChanged();
            this.notifyObservers();
        }
    }

    /**
     * overloaded connect method with implicit version check, see connect() above.
     */
    public void connect(String hostname, int port, String username, String database, String password, boolean useSSL, boolean compress, int maxconnections) throws ClassNotFoundException, SQLException, DBVersionException, DBVersionUnknownException, DBEmptyException {
        connect(hostname, port, username, database, password, useSSL, compress, maxconnections, true, true);
    }

    private Connection getNewConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, properties);
    }

    public int getMaxconnections() {
        return maxconnections;
    }

    /**
     * Closes an existing connection. If no connection exists, this method does nothing.
     * @throws SQLException if an error occurs while trying to close the connection.
     */
    public void disconnect() {
        watchDog.terminate();
        synchronized (sync) {
            if (!connections.isEmpty()) {
                while (connections.size() > 0) {
                    ThreadConnection tconn = connections.pop();
                    try {
                        tconn.conn.rollback();
                        tconn.conn.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
        this.setChanged();
        this.notifyObservers("disconnect");
    }

    public void releaseConnection() {
        synchronized (sync) {
            for (ThreadConnection tconn : connections) {
                if (tconn.thread == Thread.currentThread()) {
                    tconn.thread = null;
                    tconn.time = System.currentTimeMillis();
                    break;
                }
            }
        }
    }

    public int freeConnectionCount() {
        int res;
        synchronized (sync) {
            res = maxconnections - connections.size();
            for (ThreadConnection tconn : connections) {
                if (tconn.thread == null || !tconn.thread.isAlive()) {
                    res++;
                }
            }
        }
        return res;
    }

    public Connection getConn() throws SQLException {
        if (connections.isEmpty()) {
            throw new NoConnectionToDBException();
        }
        return getConn(0);
    }

    private Connection getConn(int retryCount) throws SQLException {
        if (retryCount > 5) {
            throw new SQLException("No connections available.");
        }
        if (!isConnected()) {
            this.setChanged();
            this.notifyObservers();
            throw new NoConnectionToDBException();
        }
        try {
            synchronized (sync) {
                for (ThreadConnection tconn : connections) {
                    if (tconn.thread == Thread.currentThread()) {
                        if (tconn.conn.isValid(10)) {
                            tconn.time = System.currentTimeMillis();
                            return tconn.conn;
                        }
                    }
                }
                for (ThreadConnection tconn : connections) {
                    if (tconn.thread == null || !tconn.thread.isAlive()) {
                        tconn.thread = Thread.currentThread();
                        if (tconn.conn.isValid(10)) {
                            tconn.time = System.currentTimeMillis();
                            return tconn.conn;
                        }
                    }
                }
                if (connections.size() < maxconnections) {
                    Connection conn = getNewConnection();
                    connections.add(new ThreadConnection(Thread.currentThread(), conn, System.currentTimeMillis()));
                    return conn;
                }
                for (ThreadConnection tconn : connections) {
                    if (tconn.conn.getAutoCommit() && System.currentTimeMillis() - tconn.time > 500) {
                        tconn.thread = Thread.currentThread();
                        if (tconn.conn.isValid(10)) {
                            return tconn.conn;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            this.disconnect();
            throw new NoConnectionToDBException();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new SQLException("No connections available.");
        }
        return getConn(retryCount + 1);
    }

    /**
     *
     * @return if a valid connection exists.
     */
    public boolean isConnected() {
        synchronized (sync) {
            return connections.size() > 0;
        }
    }

    private class ConnectionWatchDog extends Thread {

        private boolean terminated = false;

        public void terminate() {
            this.terminated = true;
        }

        @Override
        public void run() {
            terminated = true;
            while (!terminated) {
                synchronized (DatabaseConnector.this.sync) {
                    for (int i = connections.size() - 1; i >= 0; i--) {
                        ThreadConnection tconn = connections.get(i);
                        if (tconn.thread == null) {
                            if (System.currentTimeMillis() - tconn.time > CONNECTION_TIMEOUT) {
                                try {
                                    tconn.conn.close();
                                    System.out.println("CLOSED CONNECTION!");
                                } catch (SQLException e) {
                                }
                                connections.remove(i);
                            }
                        } else if (!tconn.thread.isAlive()) {
                            tconn.thread = null;
                            tconn.time = System.currentTimeMillis();
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private class ThreadConnection {

        Thread thread;

        Connection conn;

        long time;

        public ThreadConnection(Thread thread, Connection conn, long time) {
            this.thread = thread;
            this.conn = conn;
            this.time = time;
        }
    }

    public static int getLocalModelVersion() {
        int cur_version = 0;
        while (EDACCApp.class.getClassLoader().getResource("edacc/resources/db_version/" + (cur_version + 1) + ".sql") != null) {
            cur_version++;
        }
        return cur_version;
    }

    private void checkVersion() throws DBVersionException, DBVersionUnknownException, DBEmptyException {
        int localVersion = getLocalModelVersion();
        try {
            int version = getModelVersion();
            if (version != localVersion) {
                throw new DBVersionException(version, localVersion);
            } else {
                return;
            }
        } catch (SQLException e) {
        }
        try {
            Statement st = getConn().createStatement();
            ResultSet rs = st.executeQuery("SHOW TABLES;");
            if (rs.next()) {
                throw new DBVersionUnknownException(localVersion);
            }
        } catch (SQLException e) {
        }
        throw new DBEmptyException();
    }

    public void updateDBModel(Tasks task) throws Exception {
        task.setOperationName("Updating DB Model..");
        int localVersion = getLocalModelVersion();
        int currentVersion = 0;
        try {
            currentVersion = getModelVersion();
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) {
                throw e;
            }
        }
        boolean autoCommit = getConn().getAutoCommit();
        try {
            getConn().setAutoCommit(false);
            for (int version = currentVersion + 1; version <= localVersion; version++) {
                task.setStatus("Updating to version " + version);
                InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/db_version/" + version + ".sql");
                if (in == null) {
                    throw new SQLQueryFileNotFoundException();
                }
                executeSqlScript(task, in);
                Statement st = getConn().createStatement();
                st.executeUpdate("INSERT INTO `Version` VALUES (" + version + ", NOW())");
                st.close();
            }
        } catch (Exception e) {
            getConn().rollback();
            throw e;
        } finally {
            getConn().setAutoCommit(autoCommit);
        }
    }

    /**
     * Creates the correct DB schema for EDACC using an already established connection.
     */
    public void createDBSchema(Tasks task) throws NoConnectionToDBException, SQLException, IOException {
        task.setOperationName("Database");
        task.setStatus("Generating tables");
        InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/edacc.sql");
        if (in == null) {
            throw new SQLQueryFileNotFoundException();
        }
        executeSqlScript(task, in);
        Statement st = getConn().createStatement();
        st.executeUpdate("INSERT INTO `Version` VALUES (" + getLocalModelVersion() + ", NOW())");
        st.close();
        task.setStatus("Adding default property value types");
        PropertyValueTypeManager.getInstance().addDefaultToDB();
    }

    public void executeSqlScript(Tasks task, InputStream in) throws IOException, SQLException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        String text = "";
        String l;
        while ((line = br.readLine()) != null) {
            if (!(l = line.replaceAll("\\s", "")).isEmpty() && !l.startsWith("--")) {
                text += line + " ";
            }
        }
        in.close();
        Vector<String> queries = new Vector<String>();
        String query = "";
        String delimiter = ";";
        int i = 0;
        while (i < text.length()) {
            if (text.toLowerCase().startsWith("delimiter", i)) {
                i += 10;
                delimiter = text.substring(i, text.indexOf(' ', i));
                i = text.indexOf(' ', i);
            } else if (text.startsWith(delimiter, i)) {
                queries.add(query);
                i += delimiter.length();
                query = "";
            } else {
                query += text.charAt(i);
                i++;
            }
        }
        if (!query.replaceAll(" ", "").equals("")) {
            queries.add(query);
        }
        boolean autoCommit = getConn().getAutoCommit();
        try {
            getConn().setAutoCommit(false);
            Statement st = getConn().createStatement();
            int current = 0;
            for (String q : queries) {
                task.setTaskProgress((float) ++current / (float) (queries.size()));
                st.execute(q);
            }
            st.close();
            task.setTaskProgress(0.f);
            getConn().commit();
        } catch (SQLException e) {
            getConn().rollback();
            throw e;
        } finally {
            getConn().setAutoCommit(autoCommit);
        }
    }

    public String getDatabase() {
        return database;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Returns whether the database is a competition database
     * @return
     * @throws NoConnectionToDBException
     * @throws SQLException
     */
    public boolean isCompetitionDB() throws NoConnectionToDBException, SQLException {
        if (isCompetitionDB != null) {
            return isCompetitionDB;
        }
        PreparedStatement ps = getConn().prepareStatement("SELECT competition FROM DBConfiguration");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            isCompetitionDB = rs.getBoolean("competition");
            rs.close();
            ps.close();
            return isCompetitionDB;
        }
        rs.close();
        ps.close();
        return false;
    }

    public int getModelVersion() throws SQLException {
        if (modelVersion != null) {
            return modelVersion;
        }
        Integer version = null;
        Statement st = getConn().createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(`version`) FROM `Version`");
        if (rs.next()) {
            version = rs.getInt(1);
        }
        rs.close();
        st.close();
        modelVersion = version;
        return modelVersion;
    }
}
