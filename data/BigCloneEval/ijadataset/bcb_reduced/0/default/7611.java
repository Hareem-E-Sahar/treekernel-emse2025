import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.struts.sql.DomSql;

public class Conn {

    private static Connection con = null;

    public static Connection getCon() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433; DatabaseName=XuRi", "sa", "sa");
            System.out.println("调用连接方法");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    public static void closeCon() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
