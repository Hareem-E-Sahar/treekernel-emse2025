package jdbc_adapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.RubyObjectAdapter;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyNumeric;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.RubyTime;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.Java;
import org.jruby.javasupport.JavaObject;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;

/**
 * Part of our ActiveRecord::ConnectionAdapters::Connection impl.
 */
public class RubyJdbcConnection extends RubyObject {

    private static final String[] TABLE_TYPE = new String[] { "TABLE" };

    private static RubyObjectAdapter rubyApi;

    protected RubyJdbcConnection(Ruby runtime, RubyClass metaClass) {
        super(runtime, metaClass);
    }

    public static RubyClass createJdbcConnectionClass(Ruby runtime) {
        RubyClass jdbcConnection = getConnectionAdapters(runtime).defineClassUnder("JdbcConnection", runtime.getObject(), JDBCCONNECTION_ALLOCATOR);
        jdbcConnection.defineAnnotatedMethods(RubyJdbcConnection.class);
        rubyApi = JavaEmbedUtils.newObjectAdapter();
        return jdbcConnection;
    }

    private static ObjectAllocator JDBCCONNECTION_ALLOCATOR = new ObjectAllocator() {

        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RubyJdbcConnection(runtime, klass);
        }
    };

    protected static RubyModule getConnectionAdapters(Ruby runtime) {
        return (RubyModule) runtime.getModule("ActiveRecord").getConstant("ConnectionAdapters");
    }

    @JRubyMethod(name = "begin")
    public IRubyObject begin(ThreadContext context) throws SQLException {
        final Ruby runtime = context.getRuntime();
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                getConnection(true).setAutoCommit(false);
                return runtime.getNil();
            }
        });
    }

    @JRubyMethod(name = { "columns", "columns_internal" }, required = 1, optional = 2)
    public IRubyObject columns_internal(final ThreadContext context, final IRubyObject[] args) throws SQLException, IOException {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                ResultSet results = null;
                try {
                    String table_name = rubyApi.convertToRubyString(args[0]).getUnicodeValue();
                    String schemaName = null;
                    int index = table_name.indexOf(".");
                    if (index != -1) {
                        schemaName = table_name.substring(0, index);
                        table_name = table_name.substring(index + 1);
                    }
                    DatabaseMetaData metadata = c.getMetaData();
                    if (args.length > 2) schemaName = toStringOrNull(args[2]);
                    if (schemaName != null) schemaName = caseConvertIdentifierForJdbc(metadata, schemaName);
                    table_name = caseConvertIdentifierForJdbc(metadata, table_name);
                    String[] tableTypes = new String[] { "TABLE", "VIEW", "SYNONYM" };
                    RubyArray matchingTables = (RubyArray) tableLookupBlock(context.getRuntime(), c.getCatalog(), schemaName, table_name, tableTypes, false).call(c);
                    if (matchingTables.isEmpty()) {
                        throw new SQLException("Table " + table_name + " does not exist");
                    }
                    results = metadata.getColumns(c.getCatalog(), schemaName, table_name, null);
                    return unmarshal_columns(context, metadata, results);
                } finally {
                    close(results);
                }
            }
        });
    }

    @JRubyMethod(name = "commit")
    public IRubyObject commit(ThreadContext context) throws SQLException {
        Connection connection = getConnection(true);
        if (!connection.getAutoCommit()) {
            try {
                connection.commit();
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return context.getRuntime().getNil();
    }

    @JRubyMethod(name = "connection", frame = false)
    public IRubyObject connection() {
        if (getConnection() == null) reconnect();
        return getInstanceVariable("@connection");
    }

    @JRubyMethod(name = "database_name", frame = false)
    public IRubyObject database_name(ThreadContext context) throws SQLException {
        Connection connection = getConnection(true);
        String name = connection.getCatalog();
        if (null == name) {
            name = connection.getMetaData().getUserName();
            if (null == name) name = "db1";
        }
        return context.getRuntime().newString(name);
    }

    @JRubyMethod(name = "disconnect!", frame = false)
    public IRubyObject disconnect() {
        return setConnection(null);
    }

    @JRubyMethod(name = "execute_id_insert", required = 2)
    public IRubyObject execute_id_insert(ThreadContext context, final IRubyObject sql, final IRubyObject id) throws SQLException {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                PreparedStatement ps = c.prepareStatement(rubyApi.convertToRubyString(sql).getUnicodeValue());
                try {
                    ps.setLong(1, RubyNumeric.fix2long(id));
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
                return id;
            }
        });
    }

    @JRubyMethod(name = "execute_insert", required = 1)
    public IRubyObject execute_insert(final ThreadContext context, final IRubyObject sql) throws SQLException {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                Statement stmt = null;
                try {
                    stmt = c.createStatement();
                    stmt.executeUpdate(rubyApi.convertToRubyString(sql).getUnicodeValue(), Statement.RETURN_GENERATED_KEYS);
                    return unmarshal_id_result(context.getRuntime(), stmt.getGeneratedKeys());
                } finally {
                    close(stmt);
                }
            }
        });
    }

    @JRubyMethod(name = "execute_query", required = 1)
    public IRubyObject execute_query(final ThreadContext context, IRubyObject _sql) throws SQLException, IOException {
        String sql = rubyApi.convertToRubyString(_sql).getUnicodeValue();
        return executeQuery(context, sql, 0);
    }

    @JRubyMethod(name = "execute_query", required = 2)
    public IRubyObject execute_query(final ThreadContext context, IRubyObject _sql, IRubyObject _maxRows) throws SQLException, IOException {
        String sql = rubyApi.convertToRubyString(_sql).getUnicodeValue();
        int maxrows = RubyNumeric.fix2int(_maxRows);
        return executeQuery(context, sql, maxrows);
    }

    protected IRubyObject executeQuery(final ThreadContext context, final String query, final int maxRows) {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                Statement stmt = null;
                try {
                    DatabaseMetaData metadata = c.getMetaData();
                    stmt = c.createStatement();
                    stmt.setMaxRows(maxRows);
                    return unmarshalResult(context, metadata, stmt.executeQuery(query), false);
                } finally {
                    close(stmt);
                }
            }
        });
    }

    @JRubyMethod(name = "execute_update", required = 1)
    public IRubyObject execute_update(final ThreadContext context, final IRubyObject sql) throws SQLException {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                Statement stmt = null;
                try {
                    stmt = c.createStatement();
                    return context.getRuntime().newFixnum((long) stmt.executeUpdate(rubyApi.convertToRubyString(sql).getUnicodeValue()));
                } finally {
                    close(stmt);
                }
            }
        });
    }

    @JRubyMethod(name = "indexes")
    public IRubyObject indexes(ThreadContext context, IRubyObject tableName, IRubyObject name, IRubyObject schemaName) {
        return indexes(context, toStringOrNull(tableName), toStringOrNull(name), toStringOrNull(schemaName));
    }

    private static final int INDEX_TABLE_NAME = 3;

    private static final int INDEX_NON_UNIQUE = 4;

    private static final int INDEX_NAME = 6;

    private static final int INDEX_COLUMN_NAME = 9;

    /**
     * Default JDBC introspection for index metadata on the JdbcConnection.
     *
     * JDBC index metadata is denormalized (multiple rows may be returned for
     * one index, one row per column in the index), so a simple block-based
     * filter like that used for tables doesn't really work here.  Callers
     * should filter the return from this method instead.
     */
    protected IRubyObject indexes(final ThreadContext context, final String tableNameArg, final String name, final String schemaNameArg) {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                Ruby runtime = context.getRuntime();
                DatabaseMetaData metadata = c.getMetaData();
                String tableName = caseConvertIdentifierForJdbc(metadata, tableNameArg);
                String schemaName = caseConvertIdentifierForJdbc(metadata, schemaNameArg);
                ResultSet resultSet = null;
                List indexes = new ArrayList();
                try {
                    resultSet = metadata.getIndexInfo(null, schemaName, tableName, false, false);
                    List primaryKeys = primaryKeys(context, tableName);
                    String currentIndex = null;
                    RubyModule indexDefinitionClass = getConnectionAdapters(runtime).getClass("IndexDefinition");
                    while (resultSet.next()) {
                        String indexName = resultSet.getString(INDEX_NAME);
                        if (indexName == null) continue;
                        indexName = caseConvertIdentifierForRails(metadata, indexName);
                        RubyString columnName = RubyString.newUnicodeString(runtime, caseConvertIdentifierForRails(metadata, resultSet.getString(INDEX_COLUMN_NAME)));
                        if (primaryKeys.contains(columnName)) continue;
                        if (!indexName.equals(currentIndex)) {
                            currentIndex = indexName;
                            tableName = caseConvertIdentifierForRails(metadata, resultSet.getString(INDEX_TABLE_NAME));
                            boolean nonUnique = resultSet.getBoolean(INDEX_NON_UNIQUE);
                            IRubyObject indexDefinition = indexDefinitionClass.callMethod(context, "new", new IRubyObject[] { RubyString.newUnicodeString(runtime, tableName), RubyString.newUnicodeString(runtime, indexName), runtime.newBoolean(!nonUnique), runtime.newArray() });
                            indexes.add(indexDefinition);
                        }
                        IRubyObject lastIndex = (IRubyObject) indexes.get(indexes.size() - 1);
                        if (lastIndex != null) {
                            lastIndex.callMethod(context, "columns").callMethod(context, "<<", columnName);
                        }
                    }
                    return runtime.newArray(indexes);
                } finally {
                    close(resultSet);
                }
            }
        });
    }

    @JRubyMethod(name = "insert?", required = 1, meta = true, frame = false)
    public static IRubyObject insert_p(ThreadContext context, IRubyObject recv, IRubyObject _sql) {
        ByteList sql = rubyApi.convertToRubyString(_sql).getByteList();
        return context.getRuntime().newBoolean(startsWithNoCaseCmp(sql, INSERT));
    }

    @JRubyMethod(name = "insert_bind", required = 3, rest = true)
    public IRubyObject insert_bind(final ThreadContext context, final IRubyObject[] args) throws SQLException {
        final Ruby runtime = context.getRuntime();
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                PreparedStatement ps = null;
                try {
                    ps = c.prepareStatement(rubyApi.convertToRubyString(args[0]).toString(), Statement.RETURN_GENERATED_KEYS);
                    setValuesOnPS(ps, context, args[1], args[2]);
                    ps.executeUpdate();
                    return unmarshal_id_result(runtime, ps.getGeneratedKeys());
                } finally {
                    close(ps);
                }
            }
        });
    }

    @JRubyMethod(name = "native_database_types", frame = false)
    public IRubyObject native_database_types() {
        return getInstanceVariable("@native_database_types");
    }

    @JRubyMethod(name = "primary_keys", required = 1)
    public IRubyObject primary_keys(ThreadContext context, IRubyObject tableName) throws SQLException {
        return context.getRuntime().newArray(primaryKeys(context, tableName.toString()));
    }

    protected List primaryKeys(final ThreadContext context, final String tableNameArg) {
        return (List) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                Ruby runtime = context.getRuntime();
                DatabaseMetaData metadata = c.getMetaData();
                String tableName = caseConvertIdentifierForJdbc(metadata, tableNameArg);
                ResultSet resultSet = null;
                List keyNames = new ArrayList();
                try {
                    resultSet = metadata.getPrimaryKeys(null, null, tableName);
                    while (resultSet.next()) {
                        keyNames.add(RubyString.newUnicodeString(runtime, caseConvertIdentifierForRails(metadata, resultSet.getString(4))));
                    }
                } finally {
                    close(resultSet);
                }
                return keyNames;
            }
        });
    }

    @JRubyMethod(name = "reconnect!")
    public IRubyObject reconnect() {
        return setConnection(getConnectionFactory().newConnection());
    }

    @JRubyMethod(name = "rollback")
    public IRubyObject rollback(ThreadContext context) throws SQLException {
        final Ruby runtime = context.getRuntime();
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                Connection connection = getConnection(true);
                if (!connection.getAutoCommit()) {
                    try {
                        connection.rollback();
                    } finally {
                        connection.setAutoCommit(true);
                    }
                }
                return runtime.getNil();
            }
        });
    }

    @JRubyMethod(name = "select?", required = 1, meta = true, frame = false)
    public static IRubyObject select_p(ThreadContext context, IRubyObject recv, IRubyObject _sql) {
        ByteList sql = rubyApi.convertToRubyString(_sql).getByteList();
        return context.getRuntime().newBoolean(startsWithNoCaseCmp(sql, SELECT) || startsWithNoCaseCmp(sql, SHOW) || startsWithNoCaseCmp(sql, CALL));
    }

    @JRubyMethod(name = "set_native_database_types")
    public IRubyObject set_native_database_types(ThreadContext context) throws SQLException, IOException {
        Ruby runtime = context.getRuntime();
        DatabaseMetaData metadata = getConnection(true).getMetaData();
        IRubyObject types = unmarshalResult(context, metadata, metadata.getTypeInfo(), true);
        IRubyObject typeConverter = getConnectionAdapters(runtime).getConstant("JdbcTypeConverter");
        IRubyObject value = rubyApi.callMethod(rubyApi.callMethod(typeConverter, "new", types), "choose_best_types");
        setInstanceVariable("@native_types", value);
        return runtime.getNil();
    }

    @JRubyMethod(name = "tables")
    public IRubyObject tables(ThreadContext context) {
        return tables(context, null, null, null, TABLE_TYPE);
    }

    @JRubyMethod(name = "tables")
    public IRubyObject tables(ThreadContext context, IRubyObject catalog) {
        return tables(context, toStringOrNull(catalog), null, null, TABLE_TYPE);
    }

    @JRubyMethod(name = "tables")
    public IRubyObject tables(ThreadContext context, IRubyObject catalog, IRubyObject schemaPattern) {
        return tables(context, toStringOrNull(catalog), toStringOrNull(schemaPattern), null, TABLE_TYPE);
    }

    @JRubyMethod(name = "tables")
    public IRubyObject tables(ThreadContext context, IRubyObject catalog, IRubyObject schemaPattern, IRubyObject tablePattern) {
        return tables(context, toStringOrNull(catalog), toStringOrNull(schemaPattern), toStringOrNull(tablePattern), TABLE_TYPE);
    }

    @JRubyMethod(name = "tables", required = 4, rest = true)
    public IRubyObject tables(ThreadContext context, IRubyObject[] args) {
        return tables(context, toStringOrNull(args[0]), toStringOrNull(args[1]), toStringOrNull(args[2]), getTypes(args[3]));
    }

    protected IRubyObject tables(ThreadContext context, String catalog, String schemaPattern, String tablePattern, String[] types) {
        return (IRubyObject) withConnectionAndRetry(context, tableLookupBlock(context.getRuntime(), catalog, schemaPattern, tablePattern, types, false));
    }

    @JRubyMethod(name = "update_bind", required = 3, rest = true)
    public IRubyObject update_bind(final ThreadContext context, final IRubyObject[] args) throws SQLException {
        final Ruby runtime = context.getRuntime();
        Arity.checkArgumentCount(runtime, args, 3, 4);
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                PreparedStatement ps = null;
                try {
                    ps = c.prepareStatement(rubyApi.convertToRubyString(args[0]).toString());
                    setValuesOnPS(ps, context, args[1], args[2]);
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
                return runtime.getNil();
            }
        });
    }

    @JRubyMethod(name = "with_connection_retry_guard", frame = true)
    public IRubyObject with_connection_retry_guard(final ThreadContext context, final Block block) {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                return block.call(context, new IRubyObject[] { wrappedConnection(c) });
            }
        });
    }

    @JRubyMethod(name = "write_large_object", required = 6)
    public IRubyObject write_large_object(ThreadContext context, final IRubyObject[] args) throws SQLException, IOException {
        final Ruby runtime = context.getRuntime();
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                String sql = "UPDATE " + rubyApi.convertToRubyString(args[2]) + " SET " + rubyApi.convertToRubyString(args[1]) + " = ? WHERE " + rubyApi.convertToRubyString(args[3]) + "=" + rubyApi.convertToRubyString(args[4]);
                PreparedStatement ps = null;
                try {
                    ps = c.prepareStatement(sql);
                    if (args[0].isTrue()) {
                        ByteList outp = rubyApi.convertToRubyString(args[5]).getByteList();
                        ps.setBinaryStream(1, new ByteArrayInputStream(outp.bytes, outp.begin, outp.realSize), outp.realSize);
                    } else {
                        String ss = rubyApi.convertToRubyString(args[5]).getUnicodeValue();
                        ps.setCharacterStream(1, new StringReader(ss), ss.length());
                    }
                    ps.executeUpdate();
                } finally {
                    close(ps);
                }
                return runtime.getNil();
            }
        });
    }

    /**
     * Convert an identifier coming back from the database to a case which Rails is expecting.
     *
     * Assumption: Rails identifiers will be quoted for mixed or will stay mixed
     * as identifier names in Rails itself.  Otherwise, they expect identifiers to
     * be lower-case.  Databases which store identifiers uppercase should be made
     * lower-case.
     *
     * Assumption 2: It is always safe to convert all upper case names since it appears that
     * some adapters do not report StoresUpper/Lower/Mixed correctly (am I right postgres/mysql?).
     */
    public static String caseConvertIdentifierForRails(DatabaseMetaData metadata, String value) throws SQLException {
        if (value == null) return null;
        return metadata.storesUpperCaseIdentifiers() ? value.toLowerCase() : value;
    }

    /**
     * Convert an identifier destined for a method which cares about the databases internal
     * storage case.  Methods like DatabaseMetaData.getPrimaryKeys() needs the table name to match
     * the internal storage name.  Arbtrary queries and the like DO NOT need to do this.
     */
    public static String caseConvertIdentifierForJdbc(DatabaseMetaData metadata, String value) throws SQLException {
        if (value == null) return null;
        if (metadata.storesUpperCaseIdentifiers()) {
            return value.toUpperCase();
        } else if (metadata.storesLowerCaseIdentifiers()) {
            return value.toLowerCase();
        }
        return value;
    }

    protected static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
            }
        }
    }

    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
            }
        }
    }

    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
            }
        }
    }

    protected IRubyObject config_value(ThreadContext context, String key) {
        IRubyObject config_hash = getInstanceVariable("@config");
        return config_hash.callMethod(context, "[]", context.getRuntime().newSymbol(key));
    }

    private static String toStringOrNull(IRubyObject arg) {
        return arg.isNil() ? null : arg.toString();
    }

    protected IRubyObject floatToRuby(Ruby runtime, ResultSet resultSet, float floatValue) throws SQLException, IOException {
        if (floatValue == 0 && resultSet.wasNull()) return runtime.getNil();
        return runtime.newFloat(floatValue);
    }

    protected Connection getConnection() {
        return getConnection(false);
    }

    protected Connection getConnection(boolean error) {
        Connection conn = (Connection) dataGetStruct();
        if (error && conn == null) {
            RubyClass err = getRuntime().getModule("ActiveRecord").getClass("ConnectionNotEstablished");
            throw new RaiseException(getRuntime(), err, "no connection available", false);
        }
        return conn;
    }

    protected JdbcConnectionFactory getConnectionFactory() throws RaiseException {
        IRubyObject connection_factory = getInstanceVariable("@connection_factory");
        JdbcConnectionFactory factory = null;
        try {
            factory = (JdbcConnectionFactory) JavaEmbedUtils.rubyToJava(connection_factory.getRuntime(), connection_factory, JdbcConnectionFactory.class);
        } catch (Exception e) {
            factory = null;
        }
        if (factory == null) {
            throw getRuntime().newRuntimeError("@connection_factory not set properly");
        }
        return factory;
    }

    private static String[] getTypes(IRubyObject typeArg) {
        if (!(typeArg instanceof RubyArray)) return new String[] { typeArg.toString() };
        IRubyObject[] arr = rubyApi.convertToJavaArray(typeArg);
        String[] types = new String[arr.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = arr[i].toString();
        }
        return types;
    }

    private static int getTypeValueFor(Ruby runtime, IRubyObject type) throws SQLException {
        if (!(type instanceof RubySymbol)) type = rubyApi.callMethod(type, "class");
        String internedValue = type.asJavaString();
        if (internedValue == "string") {
            return Types.VARCHAR;
        } else if (internedValue == "text") {
            return Types.CLOB;
        } else if (internedValue == "integer") {
            return Types.INTEGER;
        } else if (internedValue == "decimal") {
            return Types.DECIMAL;
        } else if (internedValue == "float") {
            return Types.FLOAT;
        } else if (internedValue == "datetime") {
            return Types.TIMESTAMP;
        } else if (internedValue == "timestamp") {
            return Types.TIMESTAMP;
        } else if (internedValue == "time") {
            return Types.TIME;
        } else if (internedValue == "date") {
            return Types.DATE;
        } else if (internedValue == "binary") {
            return Types.BLOB;
        } else if (internedValue == "boolean") {
            return Types.BOOLEAN;
        } else {
            return -1;
        }
    }

    private boolean isConnectionBroken(ThreadContext context, Connection c) {
        try {
            IRubyObject alive = config_value(context, "connection_alive_sql");
            if (select_p(context, this, alive).isTrue()) {
                String connectionSQL = rubyApi.convertToRubyString(alive).toString();
                Statement s = c.createStatement();
                try {
                    s.execute(connectionSQL);
                } finally {
                    close(s);
                }
                return false;
            } else {
                return !c.isClosed();
            }
        } catch (Exception sx) {
            return true;
        }
    }

    protected IRubyObject integerToRuby(Ruby runtime, ResultSet resultSet, long longValue) throws SQLException, IOException {
        if (longValue == 0 && resultSet.wasNull()) return runtime.getNil();
        return runtime.newFixnum(longValue);
    }

    protected IRubyObject jdbcToRuby(Ruby runtime, int column, int type, ResultSet resultSet) throws SQLException {
        try {
            switch(type) {
                case Types.BINARY:
                case Types.BLOB:
                case Types.LONGVARBINARY:
                case Types.VARBINARY:
                case Types.LONGVARCHAR:
                    return streamToRuby(runtime, resultSet, resultSet.getBinaryStream(column));
                case Types.CLOB:
                    return readerToRuby(runtime, resultSet, resultSet.getCharacterStream(column));
                case Types.TIMESTAMP:
                    return timestampToRuby(runtime, resultSet, resultSet.getTimestamp(column));
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    return integerToRuby(runtime, resultSet, resultSet.getLong(column));
                case Types.REAL:
                    return floatToRuby(runtime, resultSet, resultSet.getFloat(column));
                default:
                    return stringToRuby(runtime, resultSet, resultSet.getString(column));
            }
        } catch (IOException ioe) {
            throw (SQLException) new SQLException(ioe.getMessage()).initCause(ioe);
        }
    }

    protected void populateFromResultSet(ThreadContext context, Ruby runtime, List results, ResultSet resultSet, ColumnData[] columns) throws SQLException {
        int columnCount = columns.length;
        while (resultSet.next()) {
            RubyHash row = RubyHash.newHash(runtime);
            for (int i = 0; i < columnCount; i++) {
                row.op_aset(context, columns[i].name, jdbcToRuby(runtime, i + 1, columns[i].type, resultSet));
            }
            results.add(row);
        }
    }

    protected IRubyObject readerToRuby(Ruby runtime, ResultSet resultSet, Reader reader) throws SQLException, IOException {
        if (reader == null && resultSet.wasNull()) return runtime.getNil();
        StringBuffer str = new StringBuffer(2048);
        try {
            char[] buf = new char[2048];
            for (int n = reader.read(buf); n != -1; n = reader.read(buf)) {
                str.append(buf, 0, n);
            }
        } finally {
            reader.close();
        }
        return RubyString.newUnicodeString(runtime, str.toString());
    }

    private IRubyObject setConnection(Connection c) {
        close(getConnection());
        IRubyObject rubyconn = c != null ? wrappedConnection(c) : getRuntime().getNil();
        setInstanceVariable("@connection", rubyconn);
        dataWrapStruct(c);
        return this;
    }

    private static final DateFormat FORMAT = new SimpleDateFormat("%y-%M-%d %H:%m:%s");

    private static void setValue(PreparedStatement ps, int index, ThreadContext context, IRubyObject value, IRubyObject type) throws SQLException {
        final int tp = getTypeValueFor(context.getRuntime(), type);
        if (value.isNil()) {
            ps.setNull(index, tp);
            return;
        }
        switch(tp) {
            case Types.VARCHAR:
            case Types.CLOB:
                ps.setString(index, RubyString.objAsString(context, value).toString());
                break;
            case Types.INTEGER:
                ps.setLong(index, RubyNumeric.fix2long(value));
                break;
            case Types.FLOAT:
                ps.setDouble(index, ((RubyNumeric) value).getDoubleValue());
                break;
            case Types.TIMESTAMP:
            case Types.TIME:
            case Types.DATE:
                if (!(value instanceof RubyTime)) {
                    try {
                        Date dd = FORMAT.parse(RubyString.objAsString(context, value).toString());
                        ps.setTimestamp(index, new java.sql.Timestamp(dd.getTime()), Calendar.getInstance());
                    } catch (Exception e) {
                        ps.setString(index, RubyString.objAsString(context, value).toString());
                    }
                } else {
                    RubyTime rubyTime = (RubyTime) value;
                    java.util.Date date = rubyTime.getJavaDate();
                    long millis = date.getTime();
                    long micros = rubyTime.microseconds() - millis / 1000;
                    java.sql.Timestamp ts = new java.sql.Timestamp(millis);
                    java.util.Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    ts.setNanos((int) (micros * 1000));
                    ps.setTimestamp(index, ts, cal);
                }
                break;
            case Types.BOOLEAN:
                ps.setBoolean(index, value.isTrue());
                break;
            default:
                throw new RuntimeException("type " + type + " not supported in _bind yet");
        }
    }

    private static void setValuesOnPS(PreparedStatement ps, ThreadContext context, IRubyObject valuesArg, IRubyObject typesArg) throws SQLException {
        RubyArray values = (RubyArray) valuesArg;
        RubyArray types = (RubyArray) typesArg;
        for (int i = 0, j = values.getLength(); i < j; i++) {
            setValue(ps, i + 1, context, values.eltInternal(i), types.eltInternal(i));
        }
    }

    protected IRubyObject streamToRuby(Ruby runtime, ResultSet resultSet, InputStream is) throws SQLException, IOException {
        if (is == null && resultSet.wasNull()) return runtime.getNil();
        ByteList str = new ByteList(2048);
        try {
            byte[] buf = new byte[2048];
            for (int n = is.read(buf); n != -1; n = is.read(buf)) {
                str.append(buf, 0, n);
            }
        } finally {
            is.close();
        }
        return runtime.newString(str);
    }

    protected IRubyObject stringToRuby(Ruby runtime, ResultSet resultSet, String string) throws SQLException, IOException {
        if (string == null && resultSet.wasNull()) return runtime.getNil();
        return RubyString.newUnicodeString(runtime, string);
    }

    private static final int TABLE_NAME = 3;

    protected SQLBlock tableLookupBlock(final Ruby runtime, final String catalog, final String schemapat, final String tablepat, final String[] types, final boolean downCase) {
        return new SQLBlock() {

            public Object call(Connection c) throws SQLException {
                ResultSet rs = null;
                try {
                    DatabaseMetaData metadata = c.getMetaData();
                    String clzName = metadata.getClass().getName().toLowerCase();
                    boolean isOracle = clzName.indexOf("oracle") != -1 || clzName.indexOf("oci") != -1;
                    String realschema = schemapat;
                    String realtablepat = tablepat;
                    if (realtablepat != null) realtablepat = caseConvertIdentifierForJdbc(metadata, realtablepat);
                    if (realschema != null) realschema = caseConvertIdentifierForJdbc(metadata, realschema);
                    rs = metadata.getTables(catalog, realschema, realtablepat, types);
                    List arr = new ArrayList();
                    while (rs.next()) {
                        String name;
                        if (downCase) {
                            name = rs.getString(TABLE_NAME).toLowerCase();
                        } else {
                            name = caseConvertIdentifierForRails(metadata, rs.getString(TABLE_NAME));
                        }
                        if (!isOracle || !name.startsWith("bin$")) {
                            arr.add(RubyString.newUnicodeString(runtime, name));
                        }
                    }
                    return runtime.newArray(arr);
                } finally {
                    close(rs);
                }
            }
        };
    }

    protected IRubyObject timestampToRuby(Ruby runtime, ResultSet resultSet, Timestamp time) throws SQLException, IOException {
        if (time == null && resultSet.wasNull()) return runtime.getNil();
        String str = time.toString();
        if (str.endsWith(" 00:00:00.0")) {
            str = str.substring(0, str.length() - (" 00:00:00.0".length()));
        }
        return RubyString.newUnicodeString(runtime, str);
    }

    private static final int COLUMN_NAME = 4;

    private static final int DATA_TYPE = 5;

    private static final int TYPE_NAME = 6;

    private static final int COLUMN_SIZE = 7;

    private static final int DECIMAL_DIGITS = 9;

    private static final int COLUMN_DEF = 13;

    private static final int IS_NULLABLE = 18;

    private int intFromResultSet(ResultSet resultSet, int column) throws SQLException {
        int precision = resultSet.getInt(column);
        return precision == 0 && resultSet.wasNull() ? -1 : precision;
    }

    /**
     * Create a string which represents a sql type usable by Rails from the resultSet column
     * metadata object.
     *
     * @param numberAsBoolean the database uses decimal as a boolean data type
     * because it does not support optional SQL92 type or mandatory SQL99
     * booleans.
     */
    private String typeFromResultSet(ResultSet resultSet, boolean numberAsBoolean) throws SQLException {
        int precision = intFromResultSet(resultSet, COLUMN_SIZE);
        int scale = intFromResultSet(resultSet, DECIMAL_DIGITS);
        if (numberAsBoolean && precision != 1 && resultSet.getInt(DATA_TYPE) == java.sql.Types.DECIMAL) precision = -1;
        String type = resultSet.getString(TYPE_NAME);
        if (precision > 0) {
            type += "(" + precision;
            if (scale > 0) type += "," + scale;
            type += ")";
        }
        return type;
    }

    private IRubyObject defaultValueFromResultSet(Ruby runtime, ResultSet resultSet) throws SQLException {
        String defaultValue = resultSet.getString(COLUMN_DEF);
        return defaultValue == null ? runtime.getNil() : RubyString.newUnicodeString(runtime, defaultValue);
    }

    private IRubyObject unmarshal_columns(ThreadContext context, DatabaseMetaData metadata, ResultSet rs) throws SQLException {
        try {
            Ruby runtime = context.getRuntime();
            List columns = new ArrayList();
            String clzName = metadata.getClass().getName().toLowerCase();
            boolean isOracle = clzName.indexOf("oracle") != -1 || clzName.indexOf("oci") != -1;
            RubyHash types = (RubyHash) native_database_types();
            IRubyObject jdbcCol = getConnectionAdapters(runtime).getConstant("JdbcColumn");
            while (rs.next()) {
                IRubyObject column = jdbcCol.callMethod(context, "new", new IRubyObject[] { getInstanceVariable("@config"), RubyString.newUnicodeString(runtime, caseConvertIdentifierForRails(metadata, rs.getString(COLUMN_NAME))), defaultValueFromResultSet(runtime, rs), RubyString.newUnicodeString(runtime, typeFromResultSet(rs, isOracle)), runtime.newBoolean(!rs.getString(IS_NULLABLE).trim().equals("NO")) });
                columns.add(column);
                IRubyObject tp = (IRubyObject) types.fastARef(column.callMethod(context, "type"));
                if (tp != null && !tp.isNil() && tp.callMethod(context, "[]", runtime.newSymbol("limit")).isNil()) {
                    column.callMethod(context, "limit=", runtime.getNil());
                    if (!column.callMethod(context, "type").equals(runtime.newSymbol("decimal"))) {
                        column.callMethod(context, "precision=", runtime.getNil());
                    }
                }
            }
            return runtime.newArray(columns);
        } finally {
            close(rs);
        }
    }

    public static IRubyObject unmarshal_id_result(Ruby runtime, ResultSet rs) throws SQLException {
        try {
            if (rs.next() && rs.getMetaData().getColumnCount() > 0) {
                return runtime.newFixnum(rs.getLong(1));
            }
            return runtime.getNil();
        } finally {
            close(rs);
        }
    }

    /**
     * Converts a jdbc resultset into an array (rows) of hashes (row) that AR expects.
     *
     * @param downCase should column names only be in lower case?
     */
    protected IRubyObject unmarshalResult(ThreadContext context, DatabaseMetaData metadata, ResultSet resultSet, boolean downCase) throws SQLException {
        Ruby runtime = context.getRuntime();
        List results = new ArrayList();
        try {
            ColumnData[] columns = ColumnData.setup(runtime, metadata, resultSet.getMetaData(), downCase);
            populateFromResultSet(context, runtime, results, resultSet, columns);
        } finally {
            close(resultSet);
        }
        return runtime.newArray(results);
    }

    protected Object withConnectionAndRetry(ThreadContext context, SQLBlock block) {
        int tries = 1;
        int i = 0;
        Throwable toWrap = null;
        boolean autoCommit = false;
        while (i < tries) {
            Connection c = getConnection(true);
            try {
                autoCommit = c.getAutoCommit();
                return block.call(c);
            } catch (Exception e) {
                toWrap = e;
                while (toWrap.getCause() != null && toWrap.getCause() != toWrap) {
                    toWrap = toWrap.getCause();
                }
                i++;
                if (autoCommit) {
                    if (i == 1) {
                        tries = (int) rubyApi.convertToRubyInteger(config_value(context, "retry_count")).getLongValue();
                        if (tries <= 0) {
                            tries = 1;
                        }
                    }
                    if (isConnectionBroken(context, c)) {
                        reconnect();
                    } else {
                        throw wrap(context, toWrap);
                    }
                }
            }
        }
        throw wrap(context, toWrap);
    }

    private static RuntimeException wrap(ThreadContext context, Throwable exception) {
        RubyClass err = context.getRuntime().getModule("ActiveRecord").getClass("ActiveRecordError");
        return (RuntimeException) new RaiseException(context.getRuntime(), err, exception.getMessage(), false).initCause(exception);
    }

    private IRubyObject wrappedConnection(Connection c) {
        return Java.java_to_ruby(this, JavaObject.wrap(getRuntime(), c), Block.NULL_BLOCK);
    }

    private static int whitespace(int start, ByteList bl) {
        int end = bl.begin + bl.realSize;
        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(bl.bytes[i])) return i;
        }
        return end;
    }

    private static byte[] CALL = new byte[] { 'c', 'a', 'l', 'l' };

    private static byte[] INSERT = new byte[] { 'i', 'n', 's', 'e', 'r', 't' };

    private static byte[] SELECT = new byte[] { 's', 'e', 'l', 'e', 'c', 't' };

    private static byte[] SHOW = new byte[] { 's', 'h', 'o', 'w' };

    private static boolean startsWithNoCaseCmp(ByteList bytelist, byte[] compare) {
        int p = whitespace(bytelist.begin, bytelist);
        if (bytelist.bytes[p] == '(') p = whitespace(p, bytelist);
        for (int i = 0; i < bytelist.realSize && i < compare.length; i++) {
            if (Character.toLowerCase(bytelist.bytes[p + i]) != compare[i]) return false;
        }
        return true;
    }

    public static class ColumnData {

        public IRubyObject name;

        public int type;

        public ColumnData(IRubyObject name, int type) {
            this.name = name;
            this.type = type;
        }

        public static ColumnData[] setup(Ruby runtime, DatabaseMetaData databaseMetadata, ResultSetMetaData metadata, boolean downCase) throws SQLException {
            int columnsCount = metadata.getColumnCount();
            ColumnData[] columns = new ColumnData[columnsCount];
            for (int i = 1; i <= columnsCount; i++) {
                String name;
                if (downCase) {
                    name = metadata.getColumnLabel(i).toLowerCase();
                } else {
                    name = RubyJdbcConnection.caseConvertIdentifierForRails(databaseMetadata, metadata.getColumnLabel(i));
                }
                columns[i - 1] = new ColumnData(RubyString.newUnicodeString(runtime, name), metadata.getColumnType(i));
            }
            return columns;
        }
    }
}
