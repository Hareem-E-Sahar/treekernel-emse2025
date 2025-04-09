import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * class for connecting to DB, singleton
 * @author anton
 *
 */
public class EWDataBase {

    /** current existing object */
    private static EWDataBase instance;

    /** db provider if db is not exist then reate new one */
    private String dbURL = "jdbc:derby:EW;create=true";

    /** current connection for this object*/
    public Connection conn = null;

    /**
     * constructor, establish new connection with db 
     */
    private EWDataBase() {
        createConnection();
    }

    /**
	 * create object if it's not create yet, else return created instance
	 * @return - object EWDatBase
	 */
    public static synchronized EWDataBase getInstance() {
        if (instance == null) {
            instance = new EWDataBase();
        }
        return instance;
    }

    /**
	 * do connection to db
	 */
    private void createConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(dbURL);
            createTables();
        } catch (Exception except) {
            except.printStackTrace();
        }
    }

    /**
	 * check for existing table, and create new tables if it's not exists
	 */
    private void createTables() {
        try {
            String[] tableTypes = { "TABLE" };
            ResultSet tables = conn.getMetaData().getTables(null, null, null, tableTypes);
            boolean isExists = false;
            while (tables.next()) {
                String tab = tables.getString("TABLE_NAME");
                if (tab.toUpperCase().equals("PROFILES")) isExists = true;
            }
            if (!isExists) {
                Statement stmt = conn.createStatement();
                InputStream stream = this.getClass().getResourceAsStream("resurse/createTables.sql");
                String sql = readFileAsString(stream);
                String[] query = sql.split("\n\n");
                for (int i = 0; i < query.length; i++) {
                    String sq = query[i];
                    stmt.executeUpdate(sq);
                }
                stmt.close();
            }
        } catch (Exception except) {
            except.printStackTrace();
        }
    }

    /**
	 * read file and return as string 
	 * @param filePath - path to file
	 * @return - file read in one string
	 * @throws java.io.IOException
	 */
    private String readFileAsString(InputStream filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) filePath.available()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(filePath);
            f.read(buffer);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
        return new String(buffer);
    }
}
