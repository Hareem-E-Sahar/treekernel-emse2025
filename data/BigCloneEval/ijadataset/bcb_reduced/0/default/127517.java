import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

/**
 * Handles all direct contact with the database
 * 
 * @author Sam Milton
 *s
 */
public class DB {

    private static String db = "dbclubs";

    private static String username = "srm2997";

    private static String passwd = "dbclass";

    private static String host = "SQL09.FREEMYSQL.NET";

    private static Connection cn;

    private static boolean initialized = false;

    public static int NUM_ATTEMPTS = 2;

    /**
	 * Initializes the Database connection
	 * 
	 * @throws DBError Thrown if there's an error when connecting
	 */
    public static void Initialize() throws DBError {
        if (!initialized) {
            try {
                cn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, username, passwd);
                initialized = true;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new DBError(e);
            }
        }
    }

    /**
	 * Reconnects to the database
	 * 
	 * @return True if connection succeeded, false if not
	 */
    private static boolean Reconnect() {
        try {
            cn.close();
        } catch (SQLException e) {
        }
        try {
            cn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, username, passwd);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
	 * Performs a query on the database
	 * 
	 * @param query SQL statement to be queried
	 * 
	 * @return      ResultSet of the query, or null on an error
	 */
    public static ResultSet Query(String query) {
        ResultSet rs = null;
        int attempt = 0;
        do {
            try {
                rs = cn.createStatement().executeQuery(query);
                attempt = NUM_ATTEMPTS;
            } catch (CommunicationsException ex) {
                while (!Reconnect() && attempt < NUM_ATTEMPTS) {
                    ++attempt;
                }
                ++attempt;
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                attempt = NUM_ATTEMPTS;
            }
        } while (attempt < NUM_ATTEMPTS);
        return rs;
    }

    /**
	 * Executes an update on the database
	 * 
	 * @param query SQL update statement
	 * @return      True on success, false on failure
	 */
    public static boolean Update(String query) {
        boolean success = false;
        int attempt = 0;
        do {
            try {
                cn.createStatement().executeUpdate(query);
                success = true;
                attempt = NUM_ATTEMPTS;
            } catch (CommunicationsException ex) {
                while (!Reconnect() && attempt < NUM_ATTEMPTS) {
                    ++attempt;
                }
                ++attempt;
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                attempt = NUM_ATTEMPTS;
            }
        } while (attempt < NUM_ATTEMPTS);
        return success;
    }

    /**
	 * Executes and update on the database with prepared statements
	 * 
	 * @param query  SQL update statement
	 * @param params Array of data members for the query
	 * @return       True on success, false on failure
	 */
    public static boolean SafeUpdate(String query, String[] params) {
        boolean success = false;
        int attempt = 0;
        do {
            try {
                PreparedStatement s = cn.prepareStatement(query);
                for (int i = 0; i < params.length; ++i) {
                    s.setString(i + 1, params[i]);
                }
                s.executeUpdate();
                success = true;
                attempt = NUM_ATTEMPTS;
            } catch (CommunicationsException ex) {
                while (!Reconnect() && attempt < NUM_ATTEMPTS) {
                    ++attempt;
                }
                ++attempt;
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                attempt = NUM_ATTEMPTS;
            }
        } while (attempt < NUM_ATTEMPTS);
        return success;
    }

    /**
	 * Executes a series of SQL statements as a transaction.
	 * A SELECT statement returning no results will cause the transaction to fail.
	 * 
	 * @param queries Array of SQL statements to run
	 * @return        True if transaction was successful, false if not
	 */
    public static boolean Transaction(String[] queries) {
        boolean success = false;
        int attempt = 0;
        LOOP: do {
            try {
                cn.setAutoCommit(false);
                Statement s = cn.createStatement();
                for (int i = 0; i < queries.length; i++) {
                    if (s.execute(queries[i])) {
                        ResultSet rs = s.getResultSet();
                        rs.last();
                        if (rs.getRow() == 0) {
                            break LOOP;
                        }
                    }
                }
                success = true;
                attempt = NUM_ATTEMPTS;
            } catch (CommunicationsException ex) {
                while (!Reconnect() && attempt < NUM_ATTEMPTS) {
                    ++attempt;
                }
                ++attempt;
            } catch (SQLException ex) {
                attempt = NUM_ATTEMPTS;
            }
        } while (attempt < NUM_ATTEMPTS);
        try {
            if (success) {
                cn.commit();
            } else {
                cn.rollback();
            }
        } catch (SQLException e) {
            success = false;
        }
        try {
            cn.setAutoCommit(true);
        } catch (SQLException e) {
        }
        return success;
    }

    /**
	 * Start a transaction
	 */
    public static boolean startTransaction() {
        try {
            cn.setAutoCommit(false);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static boolean rollback() {
        try {
            cn.rollback();
            cn.setAutoCommit(true);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static boolean commitTransaction() {
        try {
            cn.commit();
            cn.setAutoCommit(true);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
