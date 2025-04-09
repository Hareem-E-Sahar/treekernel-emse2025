import java.sql.*;

public class mySQLODBC {

    public void ConnectionODBC() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String url = "jdbc:odbc:mydbodbc";
            Connection con = DriverManager.getConnection(url);
            System.out.println("MySQL ODBC ���Դ���ӳɹ�...");
            con.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new mySQLODBC().ConnectionODBC();
    }
}
