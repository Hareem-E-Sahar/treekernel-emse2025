import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class CreateDB {

    private Connection conn;

    private Statement stmt;

    public static void main(String args[]) {
        new CreateDB();
    }

    public CreateDB() {
        try {
            loadJDBCDriver();
            conn = getConnection("F:/databases/sunpress");
            stmt = conn.createStatement();
            createTables(stmt);
            populateTables(stmt);
            stmt.close();
            conn.close();
            DriverManager.getConnection("jdbc:cloudscape:;shutdown=true");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createTables(Statement stmt) {
        try {
            stmt.execute("CREATE TABLE Customers (" + "Customer_ID  INTEGER, " + "Name         VARCHAR(25), " + "Phone_Number VARCHAR(30))");
            stmt.execute("CREATE TABLE Orders (" + "Customer_ID  INTEGER, " + "Order_ID     INTEGER, " + "Amount       FLOAT)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void populateTables(Statement stmt) {
        try {
            stmt.execute("INSERT INTO Customers VALUES " + "(1, 'William Dupont',     '(652)482-0931')," + "(2, 'Kris Cromwell',      '(652)482-0932')," + "(3, 'Susan Randor',       '(652)482-0933')," + "(4, 'Jim Wilson',         '(652)482-0934')," + "(5, 'Lynn Seckinger',     '(652)482-0935')," + "(6, 'Richard Tatersall',  '(652)482-0936')," + "(7, 'Gabriella Sarintia', '(652)482-0937')," + "(8, 'Lisa Hartwig',       '(652)482-0938')");
            stmt.execute("INSERT INTO Orders VALUES " + "(1, 1, 29.99)," + "(2, 2, 49.86)," + "(1, 3, 39.99)," + "(3, 4, 99.13)," + "(1, 5, 24.87)," + "(3, 6, 112.22)," + "(8, 7, 29.99)," + "(2, 8, 49.86)," + "(1, 9, 39.99)," + "(3, 10, 99.13)," + "(1, 11, 24.87)," + "(3, 12, 112.22)," + "(7, 13, 21.12)," + "(1, 14, 27.49)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadJDBCDriver() {
        try {
            Class.forName("COM.cloudscape.core.JDBCDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection(String dbName) {
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:cloudscape:" + dbName + ";create=true");
        } catch (SQLException sqe) {
            System.err.println("Couldn't access " + dbName);
        }
        return con;
    }
}
