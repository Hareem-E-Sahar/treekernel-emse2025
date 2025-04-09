import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * The primary purpose of this program is to demonstrate how to obtain
 * client connections using DriverManager or a DataSource
 * and interact with Derby Network Server
 *
 * In particular,this sample program
 * 1)   loads the Derby Network Client driver
   (default is the derby network client driver)
 * 2)	obtains a client connection using the Driver Manager
 * 3)	obtains a client connection using a DataSource
 * 4)	tests the database connections by executing a sample query
 * and then exits the program
 *
 * Before running this program, please make sure that Derby Network Server is up
 * and running.
 *  <P>
 *  Usage: java SimpleNetworkClientSample
 *
 */
public class SimpleNetworkClientSample {

    private static String DBNAME = "NSSampleDB";

    /**
	 * Derby network server port ; default is 1527
	 */
    private static int NETWORKSERVER_PORT = 1527;

    /**
	 * Derby Network Client Driver class names
	 */
    public static final String DERBY_CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver";

    private static final String DERBY_CLIENT_DS = "org.apache.derby.jdbc.ClientDataSource";

    /**
	 * This URL is used to connect to Derby Network Server using the DriverManager.
	 * Notice that the properties may be established via the URL syntax
	 */
    private static final String CS_NS_DBURL = "jdbc:derby:net://localhost:" + NETWORKSERVER_PORT + "/" + DBNAME + ";retrieveMessagesFromServerOnGetMessage=true;deferPrepares=true;";

    private static final String DERBY_CLIENT_URL = "jdbc:derby://localhost:" + NETWORKSERVER_PORT + "/" + DBNAME + ";create=true";

    String url = DERBY_CLIENT_URL;

    String jdbcDriver = DERBY_CLIENT_DRIVER;

    String jdbcDataSource = DERBY_CLIENT_DS;

    public static void main(String[] args) throws Exception {
        new SimpleNetworkClientSample().startSample(args);
    }

    public void startSample(String[] args) throws Exception {
        DataSource clientDataSource = null;
        Connection clientConn1 = null;
        Connection clientConn2 = null;
        try {
            System.out.println("Starting Sample client program ");
            loadDriver();
            clientConn1 = getClientDriverManagerConnection();
            System.out.println("Got a client connection via the DriverManager.");
            javax.sql.DataSource myDataSource = getClientDataSource(DBNAME, null, null);
            clientConn2 = getClientDataSourceConn(myDataSource);
            System.out.println("Got a client connection via a DataSource.");
            System.out.println("Testing the connection obtained via DriverManager by executing a sample query ");
            test(clientConn1);
            System.out.println("Testing the connection obtained via a DataSource by executing a sample query ");
            test(clientConn2);
            System.out.println("Goodbye!");
        } catch (SQLException sqle) {
            System.out.println("Failure making connection: " + sqle);
            sqle.printStackTrace();
        } finally {
            if (clientConn1 != null) clientConn1.close();
            if (clientConn2 != null) clientConn2.close();
        }
    }

    /**
	 * Get a database connection from DataSource
	 * @pre Derby Network Server is started
	 * @param	ds	data source
	 * @return	returns database connection
	 * @throws Exception if there is any error
	 */
    public Connection getClientDataSourceConn(javax.sql.DataSource ds) throws Exception {
        Connection conn = ds.getConnection("usr2", "pass2");
        System.out.print("connection from datasource; getDriverName = ");
        System.out.println(conn.getMetaData().getDriverName());
        return conn;
    }

    /**
	 * Creates a client data source and sets all the necessary properties in order to
	 * connect to Derby Network Server
	 * The server is assumed to be running on 1527 and on the localhost
	 * @param	database	database name; can include Derby URL attributes
	 * @param	user		database user
	 * @param	password
	 * @return	returns DataSource
	 * @throws Exception if there is any error
	 */
    public javax.sql.DataSource getClientDataSource(String database, String user, String password) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class nsDataSource = Class.forName(jdbcDataSource);
        DataSource ds = (DataSource) nsDataSource.newInstance();
        Class[] methodParams = new Class[] { String.class };
        Method dbname = nsDataSource.getMethod("setDatabaseName", methodParams);
        Object[] args = new Object[] { database };
        dbname.invoke(ds, args);
        if (user != null) {
            Method setuser = nsDataSource.getMethod("setUser", methodParams);
            args = new Object[] { user };
            setuser.invoke(ds, args);
        }
        if (password != null) {
            Method setpw = nsDataSource.getMethod("setPassword", methodParams);
            args = new Object[] { password };
            setpw.invoke(ds, args);
        }
        Method servername = nsDataSource.getMethod("setServerName", methodParams);
        args = new Object[] { "localhost" };
        servername.invoke(ds, args);
        methodParams = new Class[] { int.class };
        Method portnumber = nsDataSource.getMethod("setPortNumber", methodParams);
        args = new Object[] { new Integer(1527) };
        portnumber.invoke(ds, args);
        return ds;
    }

    /**
	 * Load the appropriate JDBC driver
	 */
    public void loadDriver() throws Exception {
        Class.forName(jdbcDriver).newInstance();
    }

    /**
	 * Get a client connection using the DriverManager
	 * @pre The JDBC driver must have been loaded before calling this method
	 * @return Connection	client database connection
	 */
    public Connection getClientDriverManagerConnection() throws Exception {
        Properties properties = new java.util.Properties();
        properties.setProperty("user", "derbyuser");
        properties.setProperty("password", "pass");
        Connection conn = DriverManager.getConnection(url, properties);
        return conn;
    }

    /**
	 * Test a connection by executing a sample query
	 * @param	conn 	database connection
	 * @throws Exception if there is any error
	 */
    public void test(Connection conn) throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select count(*) from sys.systables");
            while (rs.next()) System.out.println("number of rows in sys.systables = " + rs.getInt(1));
        } catch (SQLException sqle) {
            System.out.println("SQLException when querying on the database connection; " + sqle);
            throw sqle;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
}
