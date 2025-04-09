import java.sql.*;
import java.io.*;

/**
 * Default superclass for Ozymandias list-related functions and classes.
 */
public class Ozymandias {

    /**
	 * Returns a connection to the database.
	 */
    public static Connection getDBConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Driver MySQL = (Driver) Class.forName("org.gjt.mm.mysql.Driver").newInstance();
        Connection Conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ozymandias", "ozymandias", "desertsands");
        return Conn;
    }

    /**
	 * Returns an array of ints that references all lists in the system.
	 */
    public static int[] getAllLists() {
        int[] allLists = { 0 };
        try {
            Connection conn = getDBConnection();
            Statement stmt = conn.createStatement();
            String dbQuery = "SELECT COUNT(L_ID) FROM Lists WHERE L_ID > -1";
            ResultSet results = stmt.executeQuery(dbQuery);
            allLists = new int[results.getInt("COUNT(L_ID)")];
            dbQuery = "SELECT L_ID FROM Lists ORDER BY Name";
            results = stmt.executeQuery(dbQuery);
            for (int i = 0; results.next(); i++) {
                allLists[i] = results.getInt("L_ID");
            }
        } catch (SQLException e) {
            System.out.println(e + ": allLists ()");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        return allLists;
    }

    /**
	 * Prints standard HTML header.
	 */
    public static void printHeader(PrintWriter out, String title) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel=stylesheet href=ozymandias.css type=text/css>");
        out.println("</head>");
        out.println("<body>");
    }
}
