import com.bs.xdbms.datamodel.*;
import com.bs.xdbms.persistence.*;
import java.sql.*;

public class DataModelLoadTest extends DataModelTest {

    protected static final String JDBC_DRIVER = "org.gjt.mm.mysql.Driver";

    protected static final String DB_URL = "jdbc:mysql://khboo/MYXDB";

    protected static final String USER_ID = "root";

    protected static final String PASSWORD = "";

    public static void main(String[] args) throws XModelException {
        System.out.println("\n---DataModelLoadTest begins---");
        DataModelLoadTest dmlt = new DataModelLoadTest();
        try {
            dmlt.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DataModelLoadTest fails.");
            return;
        }
        System.out.println("---DataModelLoadTest finished successfully---");
    }

    public void run() throws XModelException {
        println("Building a model structure from the sample database...");
        XDocument xdoc = null;
        try {
            xdoc = load("http://www.example.com/partlist/part0001.xml");
            traverse(xdoc);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new XModelException(e.toString());
        } catch (XModelException me) {
            me.printStackTrace();
            throw me;
        }
    }

    protected XDocument load(String baseURI) throws SQLException, XModelException {
        Connection conn = null;
        try {
            println("Trying to get a JDBC connection...");
            conn = getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.close();
            } catch (SQLException se) {
            }
            throw new XModelException("Failed to create a JDBC connection");
        }
        ModelLoader loader = null;
        XDocument xdoc = null;
        try {
            loader = new ModelLoader(conn);
            println("Trying to load the model from the database tables...");
            xdoc = loader.loadDocument(baseURI);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.close();
            } catch (SQLException se) {
            }
            throw new XModelException("Failed loading from the database");
        }
        loader.commit();
        try {
            conn.close();
        } catch (SQLException e) {
        }
        return xdoc;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class driver = Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(DB_URL, USER_ID, PASSWORD);
        return conn;
    }
}
