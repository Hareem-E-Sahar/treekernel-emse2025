import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Conexao {

    public static void main(String[] args) throws Exception {
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        Connection connection = DriverManager.getConnection("jdbc:odbc:DBNACIONAL");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select nomeBanco from bancos");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("nomeBanco"));
        }
        resultSet.close();
        statement.close();
        connection.close();
    }
}
