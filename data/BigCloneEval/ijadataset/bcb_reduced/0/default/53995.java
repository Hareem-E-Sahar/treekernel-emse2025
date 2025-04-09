import java.util.Vector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class mysqlOutputDriver extends outputDriver {

    protected boolean connectionOpened;

    public mysqlOutputDriver(int burstSize) {
        connectionOpened = false;
        this.burstSize = burstSize;
        this.buffer = new Vector(burstSize);
    }

    public boolean connect(String settings) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=monty&password=greatsqldb");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } catch (Exception ex) {
        }
        return (false);
    }

    public boolean close() {
        if (!connectionOpened) {
            return (true);
        }
        try {
        } catch (Exception e) {
            return (false);
        }
        return (true);
    }

    protected void flushBuffer() {
    }
}
