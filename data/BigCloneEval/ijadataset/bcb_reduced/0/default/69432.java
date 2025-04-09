import java.*;
import java.sql.Driver;
import org.apache.struts.config.ControllerConfig;

public class ConnectSqlServer {

    private static java.sql.Connection con = null;

    private static final String url = "jdbc:sqlserver://";

    private static final String serverName = "localhost";

    private static final String portNumber = "1433";

    private static final String databaseName = "sg_market10";

    private static final String userName = "sa";

    private static final String password = "sa123456";

    private static final String selectMethod = "cursor";

    public ConnectSqlServer() {
    }

    private static String getConnectionUrl() {
        return ConConfig.getInst().getUrl();
    }

    public static java.sql.Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String surl = getConnectionUrl();
            con = java.sql.DriverManager.getConnection(surl, ConConfig.getInst().getUserName(), ConConfig.getInst().getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    public void displayDbProperties() {
        java.sql.DatabaseMetaData dm = null;
        java.sql.ResultSet rs = null;
        try {
            con = this.getConnection();
            if (con != null) {
                dm = con.getMetaData();
                System.out.println("Driver Information");
                System.out.println("\tDriver Name: " + dm.getDriverName());
                System.out.println("\tDriver Version: " + dm.getDriverVersion());
                System.out.println("\nDatabase Information ");
                System.out.println("\tDatabase Name: " + dm.getDatabaseProductName());
                System.out.println("\tDatabase Version: " + dm.getDatabaseProductVersion());
                System.out.println("Avalilable Catalogs ");
                rs = dm.getCatalogs();
                while (rs.next()) {
                }
                rs.close();
                rs = null;
                closeConnection();
            } else System.out.println("Error: No active Connection");
        } catch (Exception e) {
            e.printStackTrace();
        }
        dm = null;
    }

    private void closeConnection() {
        try {
            if (con != null) con.close();
            con = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ConnectSqlServer myDbTest = new ConnectSqlServer();
        myDbTest.displayDbProperties();
    }
}
