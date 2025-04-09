package edu.washington.mysms.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.washington.mysms.coding.ResultTable;
import edu.washington.mysms.security.SqlAccount;

public class DatabaseInterface {

    private Connection connection;

    /**
	 * Creates a new instance of a DatabaseInterface with a new connection using
	 * the connection information provided by the given SQL account object.  May
	 * throw an exception during initialization of this connection.  Uses the
	 * default MySQL JDBC driver.
	 * 
	 * @param sqlAccount The connection information.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
    public DatabaseInterface(SqlAccount sqlAccount) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        this(sqlAccount, "com.mysql.jdbc.Driver");
    }

    /**
	 * Creates a new instance of a DatabaseInterface with a new connection using
	 * the connection information provided by the given SQL account object and the
	 * specified JDBC driver.  May throw an exception during initialization of
	 * this connection.
	 * 
	 * @param sqlAccount The connection information.
	 * @param jdbcDriverName The full, qualified name of the JDBC driver to use.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
    public DatabaseInterface(SqlAccount sqlAccount, String jdbcDriverName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        if (sqlAccount == null) {
            throw new NullPointerException("Given account may not be null.");
        }
        Class.forName(jdbcDriverName).newInstance();
        String url = "jdbc:mysql://" + sqlAccount.getHostname() + "/" + sqlAccount.getDatabase();
        this.connection = DriverManager.getConnection(url, sqlAccount.getUsername(), sqlAccount.getPassword());
    }

    /**
	 * Close the connection on object destruction.
	 */
    @Override
    protected void finalize() {
        try {
            this.connection.close();
        } catch (Exception e) {
        }
    }

    public ResultTable executeQuery(String query) throws SQLException, NullPointerException, ClassNotFoundException {
        if (query == null) {
            throw new NullPointerException("Given query is null.");
        }
        Statement statement = this.connection.createStatement();
        String[] subqueries = getSubQueries(query.trim());
        for (String subquery : subqueries) {
            statement.execute(subquery);
        }
        ResultSet set = statement.getResultSet();
        ResultTable table = ResultTable.createResultTableFromSet(set);
        if (set != null) {
            set.close();
        }
        if (statement != null) {
            statement.close();
        }
        return table;
    }

    /**
	 * Parses a query string, which may contain multiple semicolon delimited
	 * sub-queries into individual query strings.  This method is safe against
	 * single-quoted semicolons.
	 * 
	 * @param query The original query string.
	 * @return A set of individual query strings split in order.
	 */
    private static String[] getSubQueries(String query) {
        StringBuffer buf = new StringBuffer(query);
        ArrayList<Integer> semicolons = new ArrayList<Integer>();
        Pattern pattern = Pattern.compile("(?:\\\\\\\\)*\\\\" + "'");
        Matcher matcher = pattern.matcher(buf);
        while (matcher.find()) {
            for (int i = matcher.start(); i < matcher.end(); i++) {
                buf.setCharAt(i, '_');
            }
        }
        pattern = Pattern.compile("'.*?'");
        matcher = pattern.matcher(buf);
        while (matcher.find()) {
            for (int i = matcher.start(); i < matcher.end(); i++) {
                buf.setCharAt(i, '_');
            }
        }
        pattern = Pattern.compile(";");
        matcher = pattern.matcher(buf);
        while (matcher.find()) {
            semicolons.add(matcher.start());
        }
        ArrayList<String> subqueries = new ArrayList<String>(semicolons.size() + 1);
        int i = 0;
        int beginIndex = 0;
        for (; i < semicolons.size(); i++) {
            int endIndex = semicolons.get(i);
            String subString = query.substring(beginIndex, endIndex + 1).trim();
            if (subString.length() > 0) {
                subqueries.add(subString);
            }
            beginIndex = endIndex + 1;
        }
        String subString = query.substring(beginIndex, query.length()).trim();
        if (subString.length() > 0) {
            subqueries.add(subString);
        }
        return subqueries.toArray(new String[subqueries.size()]);
    }

    /**
	 * Returns a copy of the string with unescaped single quotes replaced with
	 * escaped single quotes.  The caller should then surround the escaped string
	 * with single quotes in order to pass the string as a value within a SQL
	 * statement.
	 * 
	 * @param text
	 * @return
	 */
    public static String getEscapedString(String text) {
        if (text == null) {
            return null;
        }
        StringBuffer value = new StringBuffer(text);
        StringBuffer escaped = new StringBuffer();
        Pattern pattern = Pattern.compile("((\\\\)*)'");
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            String beginningBackslashes = matcher.group(1).replaceAll("\\\\", "\\\\\\\\");
            if ((matcher.group(1).length() % 2) == 1) {
                matcher.appendReplacement(escaped, beginningBackslashes + "'");
            } else {
                matcher.appendReplacement(escaped, beginningBackslashes + "\\\\'");
            }
        }
        matcher.appendTail(escaped);
        return escaped.toString();
    }

    /***
	 * Print the given result set to standard out.  This method iterates through
	 * the ResultSet using the next() method, and thus may "use up" the set as
	 * ResultSets do not have the ability to rewind their iterator.  This method
	 * will not print correct results if the set has already been iterated through.
	 * 
	 * This method has been depreciated.  The preferred way to print a ResultSet is
	 * to convert it to a ResultTable and print that instead.
	 * 
	 * @param rs The result set to print.
	 * @deprecated
	 */
    public static void printResults(ResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rsmd.getColumnCount() == 0) {
                return;
            }
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            System.out.println("===========================================");
            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    System.out.print(rs.getObject(i) + "\t");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
