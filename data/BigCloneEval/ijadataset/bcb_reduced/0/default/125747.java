import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class represents a connection to the database. It is designed to be
 * fault-tolerant and gives easy access to default SQL queries.
 * @version $Id: DBCImpl.java,v 1.1 2001/07/22 20:17:41 brightice Exp $
 * @author Matthias Juchem <matthias at konfido.de>
 */
public final class DBCImpl implements DBConnection {

    private Connection sqlConnection;

    private Configuration configuration;

    private boolean connectionOpened;

    public DBCImpl(Configuration configuration) throws UnavailableException {
        if (configuration != null) {
            this.configuration = configuration;
        } else {
            throw new NullPointerException("DBConnection.DBConnection(): configuration must not be null");
        }
        connectionOpened = false;
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("SQL driver not found");
        } catch (InstantiationException e) {
            throw new UnavailableException("SQL driver not available");
        } catch (IllegalAccessException e) {
            throw new UnavailableException("SQL driver not accessable");
        }
        try {
            connect();
        } catch (Exception e) {
            throw new UnavailableException("No connection to RDBMS", 900);
        }
    }

    public boolean isValid() {
        boolean result;
        try {
            result = connectionOpened && (!sqlConnection.isClosed());
        } catch (SQLException e) {
            result = false;
        }
        return (result);
    }

    public void connect() throws SQLException {
        if (this.isValid()) return;
        sqlConnection = DriverManager.getConnection("jdbc:postgresql://newton.math.uni-mannheim.de" + ":5432/bugflow", "bugflow", "bugflow");
        connectionOpened = true;
    }

    public void disconnect() {
        if (!this.isValid()) return;
        try {
            sqlConnection.close();
        } catch (SQLException e) {
        }
        connectionOpened = false;
    }

    public String getHTMLInfo() {
        StringBuffer result = new StringBuffer();
        try {
            result.append("<table cellpadding=\"4\">").append("<tr><td align=\"right\">Database:</td><td>" + sqlConnection.getMetaData().getDatabaseProductName() + " " + sqlConnection.getMetaData().getDatabaseProductVersion() + "</td></tr>").append("<tr><td align=\"right\">JDBC Driver:</td><td>" + sqlConnection.getMetaData().getDriverName() + " -  " + sqlConnection.getMetaData().getDriverVersion() + "</td></tr>").append("<tr><td align=\"right\">Database URL:</td><td>" + sqlConnection.getMetaData().getURL() + "</td></tr>").append("<tr><td align=\"right\">Database User:</td><td>" + sqlConnection.getMetaData().getUserName() + "</td></tr></table>");
        } catch (SQLException e) {
            result = null;
        }
        return (result != null ? result.toString() : null);
    }

    protected void finalize() {
        try {
            sqlConnection.close();
        } catch (SQLException e) {
        }
        sqlConnection = null;
        configuration = null;
    }

    public Statement createStatement() {
        Statement result = null;
        try {
            result = sqlConnection.createStatement();
        } catch (SQLException e) {
        }
        return (result);
    }

    public ResultSet select(String table, String columns, String where, String orderBy) {
        ResultSet result;
        try {
            Statement st = sqlConnection.createStatement();
            result = st.executeQuery("SELECT " + columns + " FROM " + table + (where == null ? "" : " WHERE " + where) + (orderBy == null ? "" : " ORDER BY " + orderBy));
        } catch (SQLException e) {
            result = null;
        }
        return (result);
    }

    public ResultSet preparedQuery(String prepared, String[] arguments) {
        ResultSet result;
        try {
            PreparedStatement st = sqlConnection.prepareStatement(prepared);
            if (arguments != null) {
                for (int i = 1; i <= arguments.length; i++) {
                    st.setString(i, arguments[i - 1]);
                }
            }
            result = st.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.toString());
            result = null;
        }
        return (result);
    }

    public ResultSet preparedQueryByKey(String moduleName, String key, String[] arguments) {
        ResultSet result;
        try {
            PreparedStatement st = sqlConnection.prepareStatement(configuration.getSQLQueryString(moduleName, key));
            if (arguments != null) {
                for (int i = 1; i <= arguments.length; i++) {
                    st.setString(i, arguments[i - 1]);
                }
            }
            result = st.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.toString());
            result = null;
        }
        return (result);
    }

    public int preparedUpdate(String prepared, String[] arguments) {
        int result;
        try {
            PreparedStatement st = sqlConnection.prepareStatement(prepared);
            for (int i = 1; i <= arguments.length; i++) {
                st.setString(i, arguments[i - 1]);
            }
            result = st.executeUpdate();
        } catch (SQLException e) {
            result = 0;
        }
        return (result);
    }

    public int preparedUpdateByKey(String moduleName, String key, String[] arguments) {
        int result;
        try {
            PreparedStatement st = sqlConnection.prepareStatement(configuration.getSQLUpdateString(moduleName, key));
            for (int i = 1; i <= arguments.length; i++) {
                st.setString(i, arguments[i - 1]);
            }
            result = st.executeUpdate();
        } catch (SQLException e) {
            result = 0;
        }
        return (result);
    }

    public String[] singleSelect(String table, String columns, String where) {
        String[] result = null;
        try {
            ResultSet resultSet = select(table, columns, where, null);
            int colCount = resultSet.getMetaData().getColumnCount();
            result = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                result[i] = resultSet.getString(i);
            }
        } catch (SQLException e) {
        }
        return (result);
    }
}
