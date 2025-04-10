import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Utility class for executing queries against the Dalmatian database.
 */
public class Database {

    private static Connection connection = null;

    /**
	 * Gets a new statement from the static connection.
	 * 
	 * @throws SQLException when a statement can't be made.
	 */
    public static Statement createStatement() throws SQLException {
        initConnection();
        return connection.createStatement();
    }

    /**
	 * Executes a query on the database.
	 */
    public static ResultSet executeQuery(String sql) throws SQLException {
        Statement statement;
        initConnection();
        statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    /**
	 * Updates, inserts, or deletes something in the database.
	 */
    public static int executeUpdate(String sql) throws SQLException {
        Statement statement;
        initConnection();
        statement = connection.createStatement();
        return statement.executeUpdate(sql);
    }

    /**
	 * Gets the connection to the database.
	 */
    public static Connection getConnection() throws SQLException {
        initConnection();
        return connection;
    }

    public static Timestamp getCurrentTimestamp() throws SQLException {
        ResultSet results;
        results = Database.executeQuery("SELECT CURRENT_TIMESTAMP");
        results.next();
        return results.getTimestamp(1);
    }

    /**
	 * Initializes the connection to the database.
	 * 
	 * @throws SQLException when there's a problem connecting.
	 */
    private static void initConnection() throws SQLException {
        String host, name, user, password, url;
        if (connection != null) return;
        try {
            host = Configuration.getOption("database", "host");
            name = Configuration.getOption("database", "name");
            user = Configuration.getOption("database", "user");
            password = Configuration.getOption("database", "password");
            url = String.format("jdbc:mysql://%s/%s", host, name);
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (IOException e) {
            throw new SQLException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        initConnection();
        return connection.prepareStatement(sql);
    }

    /**
	 * Tests %Database.
	 */
    public static void main(String[] args) {
        ResultSet rs;
        System.out.println();
        System.out.println("****************************************");
        System.out.println("Database");
        System.out.println("****************************************");
        System.out.println();
        try {
            rs = Database.executeQuery("SELECT * FROM sponsor");
            while (rs.next()) {
                System.out.println(rs.getString("name"));
                System.out.println(rs.getString("street"));
                System.out.printf("%s, %s %s\n", rs.getString("city"), rs.getString("state"), rs.getString("zip"));
                System.out.println(rs.getString("phone"));
                System.out.println();
            }
            System.out.println(Database.getCurrentTimestamp());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("****************************************");
        System.out.println("Database");
        System.out.println("****************************************");
        System.out.println();
    }
}
