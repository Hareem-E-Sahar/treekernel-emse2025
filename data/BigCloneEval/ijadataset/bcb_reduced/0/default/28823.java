import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author jtarnowski
 *
 */
public class DerbyProcs {

    public static void getFacilities(int lowest, ResultSet[] resultSet) throws Exception {
        final String sql = "select * from FACILITIES.FACILITY where FACILITYID >= ?";
        Connection conn = DriverManager.getConnection("jdbc:default:connection");
        conn.setAutoCommit(false);
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, lowest);
        ResultSet rs = resultSet[0] = stmt.executeQuery();
        resultSet[0] = rs;
    }
}
