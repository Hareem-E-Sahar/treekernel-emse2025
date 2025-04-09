import java.sql.DriverManager;
import com.mysql.jdbc.Connection;

/**
 *Connect and disconnect database
 */
public class DatabaseConnection {

    static Connection conn;

    static Connection connCad;

    /**
	 * Gives the connection
	 * @return connection
	 */
    public static Connection getConnection() {
        return conn;
    }

    /**
	 * Gives the genocad connection
	 * @return connection
	 */
    public static Connection getConnectionCad() {
        return connCad;
    }

    /**
	 * begin the connection to genothreat database
	 */
    public static void connection() throws Exception {
        try {
            String userName = "<db_user>";
            String password = "<password>";
            String url = "jdbc:mysql://<hostname>/<databasename>";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = (Connection) DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            System.out.println("Cannot connect to database server");
            throw e;
        }
    }

    /**
	 * close the connection
	 */
    public static void disconnect() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }
}
