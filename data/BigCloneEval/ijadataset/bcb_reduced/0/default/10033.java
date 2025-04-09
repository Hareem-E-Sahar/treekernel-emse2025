import java.io.*;
import java.sql.*;

public class Errors implements Serializable {

    private Connection con = null;

    public Errors() throws ClassNotFoundException, SQLException {
        String url = "jdbc:odbc:StockTracker";
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        con = DriverManager.getConnection(url);
    }

    public byte[] serializeObj(Object obj) throws IOException {
        ByteArrayOutputStream baOStream = new ByteArrayOutputStream();
        ObjectOutputStream objOStream = new ObjectOutputStream(baOStream);
        objOStream.writeObject(obj);
        objOStream.flush();
        objOStream.close();
        return baOStream.toByteArray();
    }
}
