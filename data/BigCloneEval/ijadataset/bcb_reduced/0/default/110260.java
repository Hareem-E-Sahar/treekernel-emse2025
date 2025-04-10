import java.sql.*;
import java.io.Serializable;
import java.io.*;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

/**
 *   Example context impl that uses a database to store stuff :)
 *
 *   yes, this is silly
 *
 *   expects a mysql db test with table
 *
 *  CREATE TABLE contextstore (
 *    k varchar(100),
 *    val blob
 *  );
 *
 *  very fragile, crappy code.... just a demo!
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: DBContext.java 463298 2006-10-12 16:10:32Z henning $
 */
public class DBContext extends AbstractContext {

    Connection conn = null;

    public DBContext() {
        super();
        setup();
    }

    public DBContext(Context inner) {
        super(inner);
        setup();
    }

    /**
     *  retrieves a serialized object from the db
     *  and returns the living instance to the
     *  caller.
     */
    public Object internalGet(String key) {
        try {
            String data = null;
            String sql = "SELECT k, val FROM contextstore WHERE k ='" + key + "'";
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) data = rs.getString("val");
            rs.close();
            s.close();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data.getBytes()));
            Object o = in.readObject();
            in.close();
            return o;
        } catch (Exception e) {
            System.out.println("internalGet() : " + e);
        }
        return null;
    }

    /**
     *  Serializes and stores an object in the database.
     *  This is really a hokey way to do it, and will
     *  cause problems.  The right way is to use a
     *  prepared statement...
     */
    public Object internalPut(String key, Object value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(value);
            String data = baos.toString();
            out.close();
            baos.close();
            Statement s = conn.createStatement();
            s.executeUpdate("DELETE FROM contextstore WHERE k = '" + key + "'");
            s.executeUpdate("INSERT INTO contextstore (k,val) values ('" + key + "','" + data + "')");
            s.close();
        } catch (Exception e) {
            System.out.println("internalGet() : " + e);
        }
        return null;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    public boolean internalContainsKey(Object key) {
        return false;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    public Object[] internalGetKeys() {
        return null;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    public Object internalRemove(Object key) {
        return null;
    }

    private void setup() {
        try {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root");
        } catch (Exception e) {
            System.out.println(e);
        }
        return;
    }
}
