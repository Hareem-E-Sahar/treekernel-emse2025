import java.sql.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.io.File;

/**
 * @author Dustin Howett
 */
public class Database {

    public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    private String dbName;

    private Connection conn = null;

    private int nextApptIndex = 0;

    private Statement stmt;

    private ArrayDeque<Transactions.Committable> transactions;

    /**
	 * Static initializer for the Derby connection
	 */
    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("There was an issue initializing Derby.");
        }
    }

    /**
	 * Database ctor (PRIVATE)
	 * 
	 * @param url JDBC Database URL
	 * @param ctrl Program controller instance (parent)
	 */
    private Database(String name, String url) throws SQLException {
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw e;
        }
        dbName = name;
        stmt = conn.createStatement();
        this.nextApptIndex = 0;
        transactions = new ArrayDeque<Transactions.Committable>();
    }

    /**
	 * Connect to (and create) a Database and return it.
	 */
    public static Database open(String dbName) {
        Database db;
        String dbURL = "jdbc:derby:DB/" + dbName + ";create=true";
        try {
            db = new Database(dbName, dbURL);
        } catch (SQLException e) {
            return null;
        }
        populateDatabase(db);
        return db;
    }

    /**
	 * Call open().
	 */
    public static Database create(String dbName) {
        return Database.open(dbName);
    }

    /**
	 * Make sure the database contains the required tables.
	 */
    private boolean containsTable() {
        try {
            query("UPDATE appointments SET id=6 where 1=3");
        } catch (SQLException e) {
            String state = e.getSQLState();
            if (state.equals("42X05")) {
                return false;
            } else {
                System.err.println("Unhandled SQL exception in containsTable()");
            }
        }
        return true;
    }

    /**
	 * Populate a new database with all the structures it needs to support
	 * calendaring.
	 */
    private static void populateDatabase(Database db) {
        if (db.containsTable()) return;
        try {
            db.query("CREATE TABLE appointments (id INT NOT NULL, " + "appt_start TIMESTAMP, appt_end TIMESTAMP, " + "frequency INT, name VARCHAR(256), " + "description VARCHAR(1024))");
            db.query("CREATE TABLE reminders (time TIMESTAMP, " + "leadtime INT, description VARCHAR(1024))");
        } catch (SQLException e) {
            System.err.println("I had a problem creating the tables ...");
        }
    }

    /**
	 * Flush the cache of queries. Block until they are flushed.
	 */
    public void commit() {
        for (Transactions.Committable c : transactions) {
            try {
                if (Controller.DEBUG) System.out.println("Committing transaction " + c);
                for (PreparedStatement ps : c.getSQL(conn)) {
                    query(ps);
                }
                transactions.remove(c);
            } catch (SQLException e) {
                System.err.println("Transaction \"" + c + "\" caused an SQL Exception: " + e.getMessage());
            }
        }
    }

    /**
	 * Returns whether/not there is data waiting to be written.
	 * 
	 * @return
	 */
    public boolean pending() {
        return (transactions.size() > 0);
    }

    /**
	 * Close single database connection.
	 */
    public void close() {
        boolean goodExit = false;
        if (!pending()) commit();
        try {
            DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ015")) {
                goodExit = true;
            }
        }
        if (!goodExit) {
            System.err.println("Database " + dbName + " did not shut down correctly.");
        }
    }

    /**
	 * Close all database connections.
	 */
    public static void shutdown() {
        boolean goodExit = false;
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ015")) {
                goodExit = true;
            }
        }
        if (!goodExit) {
            System.err.println("Database did not shut down correctly.");
        }
    }

    /**
	 * Queue or execute a prepared statement.
	 * 
	 * @param statement PreparedStatement to queue or execute
	 * @throws SQLException
	 */
    private void query(PreparedStatement statement) throws SQLException {
        statement.execute();
    }

    /**
	 * Add a query to the buffer for processing.
	 * 
	 * @param query Query to run
	 */
    private void query(String query) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(query);
        query(ps);
    }

    /**
	 * Serve as an UNDO function of sorts. Remove the last Transaction.
	 * 
	 * @return True if a transaction was removed.
	 */
    public boolean removeLastTransaction() {
        if (transactions.size() > 0) {
            transactions.removeLast();
            return true;
        }
        return false;
    }

    /**
	 * Add a transaction to the queue.
	 */
    public void queue(Transactions.Committable c) {
        if (Controller.DEBUG) System.out.println("Queueing transaction " + c);
        transactions.add(c);
    }

    /**
	 * Returns the sequential number to be used as appointmentID. Is typically
	 * one more than the number of appointments.
	 * 
	 * @return The next number to be used as appointmentID
	 */
    public int getNextAppointmentIndex() {
        return nextApptIndex++;
    }

    /**
	 * Read appointments from the database.
	 * 
	 * @return HashMap of the appointments (id mapped to appointment)
	 */
    public HashMap<Integer, Appointment> getAppointments() {
        HashMap<Integer, Appointment> map = new HashMap<Integer, Appointment>();
        ResultSet appointments;
        try {
            appointments = stmt.executeQuery("SELECT id, appt_start, appt_end, " + "frequency, name, description FROM appointments");
            while (appointments.next()) {
                int aID = appointments.getInt(1);
                Timestamp start = appointments.getTimestamp(2);
                Timestamp end = appointments.getTimestamp(3);
                int frequency = appointments.getInt(4);
                String name = appointments.getString(5);
                String desc = appointments.getString(6);
                GregorianCalendar gstart, gend;
                gstart = new GregorianCalendar(start.getYear() + 1900, start.getMonth(), start.getDate(), start.getHours(), start.getMinutes());
                gend = new GregorianCalendar(end.getYear() + 1900, end.getMonth(), end.getDate(), end.getHours(), end.getMinutes());
                map.put(aID, new Appointment(name, desc, gstart, gend, false, null, aID));
                if (aID >= nextApptIndex) nextApptIndex = aID + 1;
                if (Controller.DEBUG) System.out.println("nextApptIndex is now " + nextApptIndex);
            }
            appointments.close();
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
        return map;
    }

    /**
	 * Read all reminders from the database.
	 * 
	 * @return TreeSet of the reminders
	 */
    public TreeSet<Reminder> getReminders() {
        TreeSet<Reminder> set = new TreeSet<Reminder>();
        ResultSet reminders;
        try {
            reminders = stmt.executeQuery("SELECT time, leadtime, description FROM reminders");
            while (reminders.next()) {
                Timestamp time = reminders.getTimestamp(1);
                int leadtime = reminders.getInt(2);
                String desc = reminders.getString(3);
                GregorianCalendar gtime;
                gtime = new GregorianCalendar(time.getYear() + 1900, time.getMonth(), time.getDate(), time.getHours(), time.getMinutes());
                set.add(new Reminder(gtime, leadtime, desc));
            }
            reminders.close();
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
        return set;
    }

    /**
	 * Return a list of all existing databases.
	 * 
	 * @return
	 */
    public static ArrayList<String> getDatabases() {
        File f = new File("DB");
        String[] children;
        ArrayList<String> dirChildren = new ArrayList<String>();
        children = f.list();
        for (String c : children) {
            if (new File(f, c).isDirectory()) dirChildren.add(c);
        }
        return dirChildren;
    }

    /**
	 * Unit Testing
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            Database db = Database.create("Woot");
            Appointment a = new Appointment("TOBEDELETED", "TOBEDELETED", new GregorianCalendar(2008, 4, 29, 1, 15), new GregorianCalendar(2008, 4, 29, 1, 15), false, null, 1);
            db.queue(new Transactions.AddTransaction(new Appointment("Woot", "Yay", new GregorianCalendar(2007, 5, 2, 1, 15), new GregorianCalendar(2007, 5, 2, 3, 15), false, null, (int) (Math.random() * 100)), null));
            db.queue(new Transactions.AddTransaction(a));
            System.out.println("BEFORE COMMIT");
            System.out.println(db.getAppointments());
            db.commit();
            System.out.println("AFTER COMMIT, ALL");
            System.out.println(db.getAppointments());
            db.queue(new Transactions.DeleteTransaction(a));
            db.commit();
            System.out.println("AFTER DELETE, ALL");
            System.out.println(db.getAppointments());
            db = Database.open("Yay");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            Database.shutdown();
            System.out.println(Database.getDatabases());
        }
    }
}
