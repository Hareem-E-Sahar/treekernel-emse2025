import com.allianz.weo.opus.Opus;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author korotindv
 */
public class OpusDBConnect {

    public static Connection getOpusDBConnect(HttpServletRequest request) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        String connStr = "jdbc:oracle:thin:jdbf/jdbf@msk02aldev2s:1521:TEST032";
        Connection conn = DriverManager.getConnection(connStr);
        return conn;
    }
}
