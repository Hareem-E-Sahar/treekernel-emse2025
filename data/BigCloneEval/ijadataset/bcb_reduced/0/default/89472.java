import java.io.File;
import java.sql.*;

/**
 * Class to access SQLite database
 * @author ReNa2019 http://code.google.com/p/jantrunner/
 *
 */
public class Statistics {

    private static final String TABLE_DURATION = "execution";

    private static final String SEPARATOR = ";";

    private String dbFilename;

    public Statistics(String dbFilename) throws Exception {
        this.dbFilename = dbFilename;
        if (!new File(dbFilename).exists()) createTable();
    }

    /**
	 * main method to use the class from command line
	 * @param args db file name
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: filename [sql]\r\nsql e.g. select * FROM execution;");
            System.exit(1);
            return;
        }
        if (!new File(args[0]).exists()) {
            System.err.println(String.format("File '%s' not found", args[0]));
            System.exit(1);
            return;
        }
        Statistics stat = new Statistics(args[0]);
        String sqlQuery = "";
        if (args.length >= 2) {
            sqlQuery = args[1];
        }
        stat.dumpTable(sqlQuery);
    }

    /**
	 * Get last execution duration for the given ant file/target
	 * @param antFilename
	 * @param target
	 * @return
	 * @throws Exception
	 */
    public double getLastDuration(String antFilename, String target) throws Exception {
        double duration = 0;
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilename);
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from " + TABLE_DURATION + " where (file LIKE '" + antFilename + "' AND target LIKE '" + target + "') ORDER by date DESC;");
        duration = rs.getDouble("duration");
        rs.close();
        conn.close();
        return duration;
    }

    /**
	 * Dump the SQL table to the console.
	 * @throws Exception
	 */
    public void dumpTable(String query) throws Exception {
        Class.forName("org.sqlite.JDBC");
        String SEPARATOR = ";";
        String sqlQuery = "select * from " + TABLE_DURATION + " ORDER by date;";
        if (!query.equals("")) {
            sqlQuery = query;
        }
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilename);
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sqlQuery);
        ResultSetMetaData rsMetaData = rs.getMetaData();
        String columns = "";
        int columnCount = rsMetaData.getColumnCount();
        for (int c = 1; c <= columnCount; c++) {
            columns += rsMetaData.getColumnName(c);
            if (c < columnCount) columns += SEPARATOR;
        }
        System.out.println(columns);
        while (rs.next()) {
            columns = "";
            for (int c = 1; c <= columnCount; c++) {
                columns += rs.getString(c);
                if (c < rsMetaData.getColumnCount()) columns += SEPARATOR;
            }
            System.out.println(columns);
        }
    }

    public void createTable() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilename);
        Statement stat = conn.createStatement();
        stat.executeUpdate("drop table if exists " + TABLE_DURATION + ";");
        stat.executeUpdate("create table " + TABLE_DURATION + " (date, file, target, duration);");
    }

    public void addValues(String date, String antFilename, String target, double duration) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilename);
        Statement stat = conn.createStatement();
        PreparedStatement prep = conn.prepareStatement("insert into execution values (?, ?, ?, ?);");
        prep.setString(1, date);
        prep.setString(2, antFilename);
        prep.setString(3, target);
        prep.setDouble(4, duration);
        prep.addBatch();
        prep.executeBatch();
        conn.close();
    }

    /**
	 * Get the average execution time for the given ant file/task
	 * @param antFilename
	 * @param target
	 * @return duration in seconds
	 * @throws Exception
	 */
    public double getAverage(String antFilename, String target) throws Exception {
        int duration = 0;
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilename);
        Statement stat = conn.createStatement();
        String select = " WHERE (target = '" + target + "' AND file = '" + antFilename + "')";
        String func = "avg";
        String sql = "select " + func + "(duration) as '" + func + "' from " + TABLE_DURATION + " " + select + ";";
        ResultSet rs = stat.executeQuery(sql);
        double ret = rs.getDouble(1);
        rs.close();
        conn.close();
        return ret;
    }
}
