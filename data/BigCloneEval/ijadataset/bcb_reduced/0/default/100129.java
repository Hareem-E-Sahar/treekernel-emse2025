import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import com.google.code.jholidays.core.DescriptorCollection;
import com.google.code.jholidays.core.EventBuilder;
import com.google.code.jholidays.events.IEvent;
import com.google.code.jholidays.exceptions.NotSupportedEventException;
import com.google.code.jholidays.io.IDescriptorReader;
import com.google.code.jholidays.io.jdbc.JdbcReader;

public class Main {

    /**
     * @param args
     * @throws NotSupportedEventException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void main(String[] args) throws IllegalArgumentException, NotSupportedEventException, ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test_db");
        IDescriptorReader reader = new JdbcReader(conn, "event_descriptors");
        DescriptorCollection coll = reader.read();
        System.out.println(coll.size());
        EventBuilder builder = new EventBuilder();
        List<IEvent> events = builder.buildEvents(coll);
        final int year = 2009;
        for (IEvent event : events) {
            System.out.println(event.getDate(year));
        }
    }
}
