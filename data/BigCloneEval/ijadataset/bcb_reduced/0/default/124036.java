import java.io.*;
import java.net.URL;
import java.sql.*;

class SimpleSelect {

    Connection con;

    public static boolean SetUp() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            return true;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void Connect() throws SQLException {
        String url = "jdbc:odbc:Purchase Orders";
        con = DriverManager.getConnection(url, "Admin", "");
        System.out.println("Trans  isol is: " + con.getTransactionIsolation());
        System.out.println("AutoCommit  is: " + con.getAutoCommit());
    }

    public void Disconnect() {
        try {
            con.close();
        } catch (SQLException e) {
        }
    }

    private static boolean checkForWarning(SQLWarning warn) throws SQLException {
        boolean rc = false;
        if (warn != null) {
            System.out.println("\n *** Warning ***\n");
            rc = true;
            while (warn != null) {
                System.out.println("SQLState: " + warn.getSQLState());
                System.out.println("Message:  " + warn.getMessage());
                System.out.println("Vendor:   " + warn.getErrorCode());
                System.out.println("");
                warn = warn.getNextWarning();
            }
        }
        return rc;
    }

    void ProcessRequests(DataInputStream in, PrintStream out) {
        String sQuery;
        while (true) {
            try {
                sQuery = in.readLine();
                if (sQuery == null) return;
                if (sQuery.startsWith("select")) {
                    ProcessQuery(sQuery, out);
                } else if (sQuery.startsWith("insert")) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sQuery);
                } else if (sQuery.startsWith("update")) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sQuery);
                } else if (sQuery.startsWith("commit")) {
                    con.commit();
                } else if (sQuery.startsWith("autocommit true")) {
                    con.setAutoCommit(true);
                } else if (sQuery.startsWith("autocommit false")) {
                    con.setAutoCommit(false);
                } else if (sQuery.startsWith("delete")) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sQuery);
                } else if (sQuery.startsWith("rollback")) {
                    con.rollback();
                } else {
                    out.println("What ?");
                }
                out.println(">");
            } catch (SQLException ex) {
                out.println("\n*** SQLException caught ***\n");
                while (ex != null) {
                    out.println("SQLState: " + ex.getSQLState());
                    out.println("Message:  " + ex.getMessage());
                    out.println("Vendor:   " + ex.getErrorCode());
                    ex = ex.getNextException();
                    out.println("");
                }
            } catch (java.lang.Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    void ProcessQuery(String sQuery, PrintStream out) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sQuery);
        dispResultSet(rs, out);
        rs.close();
        stmt.close();
    }

    private static void dispResultSet(ResultSet rs, PrintStream out) throws SQLException {
        int i;
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCols = rsmd.getColumnCount();
        out.println("BeginQuery");
        boolean more = rs.next();
        while (more) {
            out.println("BeginRow");
            for (i = 1; i <= numCols; i++) {
                out.print(rs.getString(i));
                out.println("<br>");
            }
            out.println("EndRow");
            out.println("");
            more = rs.next();
        }
        out.println("EndQuery");
    }
}
