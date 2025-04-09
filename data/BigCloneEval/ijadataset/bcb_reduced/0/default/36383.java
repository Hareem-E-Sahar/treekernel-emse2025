import java.sql.*;

public class Database {

    private Statement stmt;

    private Connection con;

    private ResultSet rs;

    private String MYSQL_PORT = "3306";

    private String MYSQL_IP = "192.168.1.40";

    private String MYSQL_DATABASE_NAME = "medyaindeksleme";

    private String MYSQL_USERNAME = "mysql";

    private String MYSQL_PASSWORD = "1qa2ws3ed";

    public boolean startConnection(String ip, String port, String databaseName, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(("jdbc:mysql://" + ip + ":" + port + "/" + databaseName), username, password);
            stmt = con.createStatement();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean startConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(("jdbc:mysql://" + MYSQL_IP + ":" + MYSQL_PORT + "/" + MYSQL_DATABASE_NAME), MYSQL_USERNAME, MYSQL_PASSWORD);
            stmt = con.createStatement();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet query(String query) {
        try {
            rs = stmt.executeQuery(query);
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean update(String update) {
        try {
            stmt.executeUpdate(update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void printLastQueryResult() {
        try {
            if (rs != null) {
                while (rs.next()) {
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int numberOfColumns = rsMetaData.getColumnCount() + 1;
                    for (int i = 1; i < numberOfColumns; i++) {
                        System.out.println("column: " + rsMetaData.getColumnName(i) + " value: " + rs.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean closeConnection() {
        try {
            con.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
