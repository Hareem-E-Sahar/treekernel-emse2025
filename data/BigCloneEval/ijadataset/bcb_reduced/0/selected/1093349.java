package net.jadoth.sqlengine;

import static net.jadoth.Jadoth.glue;
import static net.jadoth.lang.reflection.JaReflect.getMemberByLabel;
import static net.jadoth.sqlengine.SQL.Flag.ISBIG;
import static net.jadoth.sqlengine.SQL.Flag.ISBINARY;
import static net.jadoth.sqlengine.SQL.Flag.ISDECIMAL;
import static net.jadoth.sqlengine.SQL.Flag.ISINTEGER;
import static net.jadoth.sqlengine.SQL.Flag.ISLARGE;
import static net.jadoth.sqlengine.SQL.Flag.ISLENGTHED;
import static net.jadoth.sqlengine.SQL.Flag.ISLITERAL;
import static net.jadoth.sqlengine.SQL.Flag.ISNATIONAL;
import static net.jadoth.sqlengine.SQL.Flag.ISPRECISED;
import static net.jadoth.sqlengine.SQL.Flag.ISSCALED;
import static net.jadoth.sqlengine.SQL.Flag.ISTIME;
import static net.jadoth.sqlengine.SQL.Flag.ISTINY;
import static net.jadoth.sqlengine.SQL.Flag.ISVARYING;
import static net.jadoth.sqlengine.SQL.Punctuation.TAB;
import static net.jadoth.sqlengine.SQL.Punctuation._;
import static net.jadoth.sqlengine.SQL.Punctuation.apo;
import static net.jadoth.sqlengine.SQL.Punctuation.dot;
import static net.jadoth.sqlengine.internal.tables.SqlTable.Implementation.LABEL_METHOD_BitmapIndex;
import static net.jadoth.sqlengine.internal.tables.SqlTable.Implementation.LABEL_METHOD_ForeignKey;
import static net.jadoth.sqlengine.internal.tables.SqlTable.Implementation.LABEL_METHOD_Index;
import static net.jadoth.sqlengine.internal.tables.SqlTable.Implementation.LABEL_METHOD_PrimaryKey;
import static net.jadoth.sqlengine.internal.tables.SqlTable.Implementation.LABEL_METHOD_UniqueIndex;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jadoth.Jadoth;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.lang.reflection.annotations.Label;
import net.jadoth.meta.License;
import net.jadoth.sqlengine.dbms.DbmsAdaptor;
import net.jadoth.sqlengine.dbms.DbmsConnectionProvider;
import net.jadoth.sqlengine.dbms.DbmsDMLAssembler;
import net.jadoth.sqlengine.dbms.standard.StandardDbmsAdaptor;
import net.jadoth.sqlengine.exceptions.NoDatabaseConnectionFoundException;
import net.jadoth.sqlengine.exceptions.SQLEngineCouldNotConnectToDBException;
import net.jadoth.sqlengine.exceptions.SQLEngineException;
import net.jadoth.sqlengine.exceptions.SQLEngineRuntimeException;
import net.jadoth.sqlengine.interfaces.SqlExecutor;
import net.jadoth.sqlengine.internal.ColumnValueAssignment;
import net.jadoth.sqlengine.internal.ColumnValueTuple;
import net.jadoth.sqlengine.internal.DISTINCT;
import net.jadoth.sqlengine.internal.DatabaseGateway;
import net.jadoth.sqlengine.internal.DoubleQuotedExpression;
import net.jadoth.sqlengine.internal.QueryListeners;
import net.jadoth.sqlengine.internal.QuotedExpression;
import net.jadoth.sqlengine.internal.SqlAggregateAVG;
import net.jadoth.sqlengine.internal.SqlAggregateCOLLECT;
import net.jadoth.sqlengine.internal.SqlAggregateCOUNT;
import net.jadoth.sqlengine.internal.SqlAggregateFUSION;
import net.jadoth.sqlengine.internal.SqlAggregateINTERSECTION;
import net.jadoth.sqlengine.internal.SqlAggregateMAX;
import net.jadoth.sqlengine.internal.SqlAggregateMin;
import net.jadoth.sqlengine.internal.SqlAggregateSUM;
import net.jadoth.sqlengine.internal.SqlAsterisk;
import net.jadoth.sqlengine.internal.SqlBoolean;
import net.jadoth.sqlengine.internal.SqlBooleanTerm;
import net.jadoth.sqlengine.internal.SqlColumn;
import net.jadoth.sqlengine.internal.SqlComparison;
import net.jadoth.sqlengine.internal.SqlConcatenation;
import net.jadoth.sqlengine.internal.SqlCondition;
import net.jadoth.sqlengine.internal.SqlConditionEXISTS;
import net.jadoth.sqlengine.internal.SqlConditionNOT;
import net.jadoth.sqlengine.internal.SqlCustomFunction;
import net.jadoth.sqlengine.internal.SqlExpression;
import net.jadoth.sqlengine.internal.SqlFunctionABS;
import net.jadoth.sqlengine.internal.SqlFunctionCASE;
import net.jadoth.sqlengine.internal.SqlFunctionCOALESCE;
import net.jadoth.sqlengine.internal.SqlFunctionSUBSTRING;
import net.jadoth.sqlengine.internal.SqlTerm;
import net.jadoth.sqlengine.internal.SqlTimestamp;
import net.jadoth.sqlengine.internal.SqlxAggregateCOLLECT_asString;
import net.jadoth.sqlengine.internal.SqlxFunctionCOALESCE_dual;
import net.jadoth.sqlengine.internal.SqlxFunctionROUND;
import net.jadoth.sqlengine.internal.SqlxFunctionTO_CHAR;
import net.jadoth.sqlengine.internal.SqlxFunctionTO_NUMBER;
import net.jadoth.sqlengine.internal.interfaces.SqlTableReference;
import net.jadoth.sqlengine.internal.procedures.PROCEDURE;
import net.jadoth.sqlengine.internal.tables.SqlDdlTable;
import net.jadoth.sqlengine.internal.tables.SqlField;
import net.jadoth.sqlengine.internal.tables.SqlTable;
import net.jadoth.sqlengine.internal.tables.SqlTableIdentifier;
import net.jadoth.sqlengine.license.SqlEngineLicense;
import net.jadoth.sqlengine.util.SqlEngineLabels;
import net.jadoth.util.chars.VarChar;
import net.jadoth.util.logging.jul.LoggingAspect;

/**
 * The Class SQL.
 *
 * @author Thomas Muenz
 */
@License(name = SqlEngineLicense.LICENSE_NAME, licenseClass = SqlEngineLicense.class, declaringClass = SQL.class)
public abstract class SQL extends SqlEngineLabels {

    /**
	 * NULL {@link SqlExpression} constant.
	 */
    public static final SqlExpression NULL = new SqlExpression(LANG.NULL);

    /**
	 * Asterisk {@link SqlAsterisk} constant.
	 */
    public static final SqlAsterisk STAR = new SqlAsterisk();

    /**
	 * TRUE {@link SqlBoolean} constant.
	 */
    public static final SqlBoolean TRUE = new SqlBoolean(true);

    /**
	 * FALSE {@link SqlBoolean} constant.
	 */
    public static final SqlBoolean FALSE = new SqlBoolean(false);

    /**
	 * Common value 0 as {@link SqlExpression} constant.
	 */
    public static final SqlExpression ZERO = new SqlExpression(0);

    /**
	 * Common value 1 as {@link SqlExpression} constant.
	 */
    public static final SqlExpression ONE = new SqlExpression(1);

    public static final SqlExpression CURRENT_CATALOG = new SqlExpression(LANG.CURRENT_USER);

    public static final SqlExpression CURRENT_SCHEMA = new SqlExpression(LANG.CURRENT_SCHEMA);

    public static final SqlExpression CURRENT_TIME = new SqlExpression(LANG.CURRENT_TIME);

    public static final SqlExpression CURRENT_TIMESTAMP = new SqlExpression(LANG.CURRENT_TIMESTAMP);

    public static final SqlExpression CURRENT_USER = new SqlExpression(LANG.CURRENT_USER);

    private static DbmsAdaptor<?> defaultDBMS = null;

    private static DatabaseGateway<?> defaultDatabaseGateway = null;

    private static QueryListeners globalQueryListeners = new QueryListeners();

    public static QueryListeners getGlobalQueryListeners() {
        return globalQueryListeners;
    }

    public static QueryListeners globalQueryListeners() {
        return globalQueryListeners;
    }

    public static void setGlobalQueryListeners(final QueryListeners queryListeners) throws NullPointerException {
        if (queryListeners == null) {
            throw new NullPointerException("queryListeners may not be null");
        }
        globalQueryListeners = queryListeners;
    }

    public static DbmsAdaptor<?> getDefaultDBMS() {
        if (defaultDBMS == null) {
            defaultDBMS = StandardDbmsAdaptor.getSingletonStandardDbmsAdaptor();
        }
        return defaultDBMS;
    }

    public static DbmsDMLAssembler<?> getDefaultDMLAssembler() {
        return getDefaultDBMS().getDMLAssembler();
    }

    public static DatabaseGateway<?> getDefaultDatabaseGateway() {
        return defaultDatabaseGateway;
    }

    public static void setDefaultDBMS(final DbmsAdaptor<?> defaultDBMS) {
        SQL.defaultDBMS = defaultDBMS;
    }

    public static void setDefaultDatabaseGateway(final DatabaseGateway<?> defaultDatabaseGateway) {
        SQL.defaultDatabaseGateway = defaultDatabaseGateway;
        SQL.defaultDBMS = defaultDatabaseGateway.getDbmsAdaptor();
    }

    public static ResultSet executeQuery(final String query) throws SQLEngineException {
        if (defaultDatabaseGateway == null) {
            throw new NoDatabaseConnectionFoundException();
        }
        return defaultDatabaseGateway.execute(SqlExecutor.query, query);
    }

    public static Object executeQuerySingle(final String query) throws SQLEngineException {
        if (defaultDatabaseGateway == null) {
            throw new NoDatabaseConnectionFoundException();
        }
        return defaultDatabaseGateway.execute(SqlExecutor.singleResultQuery, query);
    }

    public static int executeUpdate(final String query) throws SQLEngineException {
        if (defaultDatabaseGateway == null) {
            throw new NoDatabaseConnectionFoundException();
        }
        return defaultDatabaseGateway.execute(SqlExecutor.update, query);
    }

    public static final SqlTableIdentifier.Implementation TABLE(final String tablename) {
        final String[] parts = util.parseFullQualifiedTablename(tablename.trim());
        return new SqlTableIdentifier.Implementation(parts[0], parts[1], parts[2] != null ? parts[2] : SQL.util.guessAlias(parts[1]));
    }

    /**
	 * TABLE.
	 *
	 * @param tablename the tablename
	 * @param alias the alias
	 * @return the sql table identity
	 */
    public static final SqlTableIdentifier.Implementation TABLE(final String tablename, final String alias) {
        final String[] parts = util.parseFullQualifiedTablename(tablename.trim());
        return new SqlTableIdentifier.Implementation(parts[0], parts[1], alias);
    }

    /**
	 * TABLE.
	 *
	 * @param schema the schema
	 * @param tablename the tablename
	 * @param alias the alias
	 * @return the sql table identity
	 */
    public static final SqlTableIdentifier.Implementation TABLE(final String schema, final String tablename, final String alias) {
        return new SqlTableIdentifier.Implementation(schema, tablename, alias != null ? alias : SQL.util.guessAlias(tablename));
    }

    /**
	 * SELECT.
	 *
	 * @param items the items
	 * @return the sELECT
	 */
    public static final SELECT SELECT(final Object... items) {
        return new SELECT().items(items);
    }

    public static final <R> CALL<R> CALL(final PROCEDURE<R> storedProcedure, final Object... paramters) {
        return new CALL<R>(storedProcedure).parameters(paramters);
    }

    public static ColumnValueTuple cv(final SqlColumn column, final Object value) {
        return new ColumnValueTuple() {

            @Override
            public SqlColumn getColumn() {
                return column;
            }

            @Override
            public Object getValue() {
                return value;
            }
        };
    }

    public static ColumnValueAssignment assign(final SqlColumn column, final Object value) {
        return new ColumnValueAssignment() {

            private Object val = value;

            @Override
            public SqlColumn getColumn() {
                return column;
            }

            @Override
            public Object getValue() {
                return this.val;
            }

            @Override
            public void setValue(final Object value) {
                this.val = value;
            }
        };
    }

    /**
	 * ABS.
	 *
	 * @param value the value
	 * @return the sql function abs
	 */
    public static final SqlFunctionABS ABS(final Object value) {
        return new SqlFunctionABS(value);
    }

    /**
	 * SUM.
	 *
	 * @param value the value
	 * @return the sql aggregate sum
	 */
    public static final SqlAggregateSUM SUM(final Object value) {
        return new SqlAggregateSUM(value);
    }

    /**
	 * MAX.
	 *
	 * @param value the value
	 * @return the sql aggregate max
	 */
    public static final SqlAggregateMAX MAX(final Object value) {
        return new SqlAggregateMAX(value);
    }

    /**
	 * MIN.
	 *
	 * @param value the value
	 * @return the sql aggregate min
	 */
    public static final SqlAggregateMin MIN(final Object value) {
        return new SqlAggregateMin(value);
    }

    /**
	 * AVG.
	 *
	 * @param value the value
	 * @return the sql aggregate avg
	 */
    public static final SqlAggregateAVG AVG(final Object value) {
        return new SqlAggregateAVG(value);
    }

    /**
	 * COUNT.
	 *
	 * @return the sql aggregate count
	 */
    public static final SqlAggregateCOUNT COUNT() {
        return COUNT(null);
    }

    /**
	 * COUNT.
	 *
	 * @param value the value
	 * @return the sql aggregate count
	 */
    public static final SqlAggregateCOUNT COUNT(final Object value) {
        if (value == null) {
            return new SqlAggregateCOUNT();
        }
        return new SqlAggregateCOUNT(value);
    }

    /**
	 * COLLECT.
	 *
	 * @param expression the expression
	 * @return the sql aggregate collect
	 */
    public static final SqlAggregateCOLLECT COLLECT(final Object expression) {
        return new SqlAggregateCOLLECT(expression);
    }

    public static final SqlxFunctionCOALESCE_dual COALESCE(final Object valueExpression1, final Object valueExpression2) {
        return new SqlxFunctionCOALESCE_dual(valueExpression1, valueExpression2);
    }

    public static final SqlFunctionCOALESCE COALESCE(final Object... valueExpressions) {
        return new SqlFunctionCOALESCE(valueExpressions);
    }

    /**
	 * xCollect_asString.
	 *
	 * @param expression the expression
	 * @return the sqlx aggregate collect as string
	 */
    public static final SqlxAggregateCOLLECT_asString xCOLLECT_asString(final Object expression) {
        return new SqlxAggregateCOLLECT_asString(expression);
    }

    /**
	 * xCollect_asString.
	 *
	 * @param expression the expression
	 * @param seperator the seperator
	 * @return the sqlx aggregate collect as string
	 */
    public static final SqlxAggregateCOLLECT_asString xCOLLECT_asString(final Object expression, final String seperator) {
        return new SqlxAggregateCOLLECT_asString(expression, seperator);
    }

    /**
	 * FUSION.
	 *
	 * @param expression the expression
	 * @return the sql aggregate fusion
	 */
    public static final SqlAggregateFUSION FUSION(final Object expression) {
        return new SqlAggregateFUSION(expression);
    }

    /**
	 * INTERSECTION.
	 *
	 * @param expression the expression
	 * @return the sql aggregate intersection
	 */
    public static final SqlAggregateINTERSECTION INTERSECTION(final Object expression) {
        return new SqlAggregateINTERSECTION(expression);
    }

    public static final DISTINCT DISTINCT(final Object expression) {
        return new DISTINCT(expression);
    }

    /**
	 * Concatenate.
	 *
	 * @param characterValueExpressions the character value expressions
	 * @return the sql concatenation
	 */
    public static final SqlConcatenation concatenate(final Object... characterValueExpressions) {
        return new SqlConcatenation(characterValueExpressions);
    }

    public static final SqlFunctionCASE booleanExpression(final Object condition) {
        return new SqlFunctionCASE().WHEN(condition, SQL.TRUE).ELSE(SQL.FALSE);
    }

    public static final SqlFunctionCASE CASE_WHEN(final Object whenExpression, final Object thenExpression, final Object elseExpression) {
        return new SqlFunctionCASE().WHEN(whenExpression, thenExpression).ELSE(elseExpression);
    }

    /**
	 * Resembles a "switch" in SQL.
	 *
	 * @param expression the expression
	 * @return the sql function case
	 * @see {@link SQL.CASE_WHEN} for the "if"/then/else equivalent
	 */
    public static final SqlFunctionCASE CASE(final Object expression) {
        return new SqlFunctionCASE(expression);
    }

    /**
	 * SUBSTRING.
	 *
	 * @param characterValueExpression the character value expression
	 * @param startPosition the start position
	 * @param stringLength the string length
	 * @param charLengthUnits the char length units
	 * @return the sql function substring
	 */
    public static final SqlFunctionSUBSTRING SUBSTRING(final Object characterValueExpression, final Integer startPosition, final Integer stringLength, final Integer charLengthUnits) {
        return new SqlFunctionSUBSTRING(characterValueExpression, startPosition, stringLength, charLengthUnits);
    }

    /**
	 * SUBSTRING.
	 *
	 * @param characterValueExpression the character value expression
	 * @param startPosition the start position
	 * @param stringLength the string length
	 * @return the sql function substring
	 */
    public static final SqlFunctionSUBSTRING SUBSTRING(final Object characterValueExpression, final Integer startPosition, final Integer stringLength) {
        return new SqlFunctionSUBSTRING(characterValueExpression, startPosition, stringLength, null);
    }

    /**
	 * SUBSTRING.
	 *
	 * @param characterValueExpression the character value expression
	 * @param startPosition the start position
	 * @return the sql function substring
	 */
    public static final SqlFunctionSUBSTRING SUBSTRING(final Object characterValueExpression, final Integer startPosition) {
        return new SqlFunctionSUBSTRING(characterValueExpression, startPosition, null, null);
    }

    /**
	 * NOT.
	 *
	 * @param condition the condition
	 * @return the sql condition not
	 */
    public static final SqlConditionNOT NOT(final SqlCondition condition) {
        return new SqlConditionNOT(null, condition);
    }

    /**
	 * EXISTS.
	 *
	 * @param subSelect the sub select
	 * @return the sql condition exists
	 */
    public static final SqlConditionEXISTS EXISTS(final SELECT subSelect) {
        return new SqlConditionEXISTS(subSelect);
    }

    /**
	 * T o_ number.
	 *
	 * @param value the value
	 * @return the sqlx function t o_ number
	 */
    public static final SqlxFunctionTO_NUMBER TO_NUMBER(final Object value) {
        return new SqlxFunctionTO_NUMBER(value);
    }

    /**
	 * T o_ char.
	 *
	 * @param value the value
	 * @param format the format
	 * @return the sqlx function t o_ char
	 */
    public static final SqlxFunctionTO_CHAR TO_CHAR(final Object value, final Object format) {
        return new SqlxFunctionTO_CHAR(value, format);
    }

    /**
	 * ROUND.
	 *
	 * @param value the value
	 * @param decimals the decimals
	 * @return the sqlx function round
	 */
    public static final SqlxFunctionROUND ROUND(final Object value, final int decimals) {
        return new SqlxFunctionROUND(value, decimals);
    }

    /**
	 * Quote.
	 *
	 * @param characterValueExpression the character value expression
	 * @return the quoted expression
	 */
    public static QuotedExpression quote(final Object characterValueExpression) {
        return new QuotedExpression(characterValueExpression);
    }

    /**
	 * Double quote.
	 *
	 * @param characterValueExpression the character value expression
	 * @return the double quoted expression
	 */
    public static DoubleQuotedExpression doubleQuote(final Object characterValueExpression) {
        return new DoubleQuotedExpression(characterValueExpression);
    }

    /**
	 * Wraps the given expression object as an {@link SqlBooleanTerm} instance that encloses it in <b>par</b>enthesis
	 * (round brackets).<br>
	 * Note that in contradiction to {@link #condition(Object)}, a new wrapper instance is <b>always</b> created to
	 * ensure that each call actually adds a pair of parenthesis (for whatever reason that might be needed).
	 *
	 * @param expression the expression to be wrapped as {@link SqlBooleanTerm}
	 * @return a {@link SqlBooleanTerm} instance wrapping the passed expression object.
	 */
    public static final SqlBooleanTerm par(final Object expression) {
        return new SqlBooleanTerm(expression, null, true);
    }

    public static final SqlTerm term(final Object expression) {
        return new SqlTerm(expression, true);
    }

    /**
	 * Wraps the given expression object as an {@link SqlCondition} instance.<br>
	 * If the object itself is already a {@link SqlCondition}, it is returned right away.
	 *
	 * @param expression the expression to be wrapped as {@link SqlCondition}
	 * @return a {@link SqlCondition} instance representing the passed expression object's content as a condition.
	 */
    public static final SqlCondition condition(final Object expression) {
        if (expression instanceof SqlCondition) {
            return (SqlCondition) expression;
        }
        return new SqlCondition(expression);
    }

    /**
	 * Exp.
	 *
	 * @param o the o
	 * @return the sql expression
	 */
    public static final SqlExpression exp(final Object o) {
        return o == null ? SQL.NULL : new SqlExpression(o);
    }

    public static final SqlCustomFunction function(final String functioName, final Object... params) {
        return new SqlCustomFunction(functioName, params);
    }

    public static final SqlComparison param(final SqlExpression expression) {
        return expression.eq(SQL.config.param);
    }

    /**
	 * Col.
	 *
	 * @param s the s
	 * @return the sql column
	 */
    public static final SqlColumn col(final String s) {
        return s == null ? null : new SqlColumn(s);
    }

    public static final SqlColumn column(final SqlTableReference owner, final Object column) {
        if (column instanceof SqlColumn) {
            return new SqlColumn(owner, ((SqlColumn) column).getColumnName());
        }
        return new SqlColumn(owner, column);
    }

    /**
	 * Timestamp.
	 *
	 * @param parts the parts
	 * @return the sql timestamp
	 */
    public static final SqlTimestamp timestamp(final int... parts) {
        return new SqlTimestamp(SQL.util.assembleTimestamp(parts));
    }

    public static final SqlTimestamp timestamp(final long timestamp) {
        return new SqlTimestamp(timestamp);
    }

    public static final SqlTimestamp timestamp(final java.util.Date javaUtilDate) {
        return new SqlTimestamp(javaUtilDate);
    }

    public static final SqlExpression booleanExp(final boolean b) {
        return b ? TRUE : FALSE;
    }

    public static final SqlExpression booleanExp(final Boolean b) {
        return b == null ? NULL : b ? TRUE : FALSE;
    }

    /**
	 * Sets the the passed {@link DbmsConnectionProvider}'s {@link DbmsAdaptor} and a newly created
	 * {@link DatabaseGateway} instance as the defaults and connects the database gateway to the associated database.
	 *
	 * @param connectionProvider the connection provider
	 * @return true, if successful
	 * @throws SQLEngineCouldNotConnectToDBException
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean connect(final DbmsConnectionProvider<?> connectionProvider) throws SQLEngineCouldNotConnectToDBException {
        return connect(new DatabaseGateway(connectionProvider), true);
    }

    /**
	 * Sets the passed {@link DatabaseGateway} and its {@link DbmsAdaptor} as the defaults.
	 * If <code>initialize</code> is {@code true}, the {@link DatabaseGateway} is initialized.
	 * @param databaseGateway the database gateway to be used as the new default
	 * @param initialize flag that causes the passed {@link DatabaseGateway} to be initialized.
	 * @return true, if successful
	 * @throws SQLEngineCouldNotConnectToDBException
	 */
    public static boolean connect(final DatabaseGateway<?> databaseGateway, final boolean initialize) throws SQLEngineCouldNotConnectToDBException {
        setDefaultDBMS(databaseGateway.getDbmsAdaptor());
        setDefaultDatabaseGateway(databaseGateway);
        if (initialize) {
            databaseGateway.initialize();
        }
        return true;
    }

    public static SELECT createStaticValueSelect(final Object[]... valueRows) {
        final SELECT head = SELECT(valueRows[0]);
        SELECT current, last = head;
        for (int i = 1, len = valueRows.length; i < len; i++) {
            current = SELECT(valueRows[i]);
            last.UNION_ALL(current);
            last = current;
        }
        return head;
    }

    /**
	 * Links a variable number of {@link SELECT} instances to form a chain where each element is connected
	 * via {@link SELECT#UNION_ALL(SELECT)} to its predecessor.
	 * <p>
	 * This is a mere convenience method to avoid heavy method nesting.<br>
	 * As an example, compare:<br>
	 * <code>SELECT s = s1.UNION_ALL(s2.UNION_ALL(s3.UNION_ALL(s4)));</code><br>
	 * to<br>
	 * <code>SELECT s = SQL.UNION_ALL(s1, s2, s3, s4);</code>
	 * <p>
	 * The returned {@link SELECT} instance is the first in the chain, just like {@link SELECT#UNION_ALL(SELECT)}
	 * of the first {@link SELECT} instance would return its instance.
	 *
	 * @param selects the {@link SELECT} instances to be connected as a {@code UNION ALL} chain.
	 * @return the first {@link SELECT} instance of the chain.
	 */
    public static final SELECT UNION_ALL(final SELECT... selects) {
        for (int i = 1; i < selects.length; i++) {
            selects[i - 1].UNION_ALL(selects[i]);
        }
        return selects[0];
    }

    /**
	 * Creates the.
	 *
	 * @param <T> the generic type
	 * @param tableClass the table class
	 * @param alias the alias
	 * @return the t
	 * @throws SQLEngineRuntimeException the sQL engine runtime exception
	 */
    public static <T extends SqlTable.Implementation> T create(final Class<T> tableClass, final String alias) throws SQLEngineRuntimeException {
        try {
            final T t = tableClass.getConstructor(String.class).newInstance(alias);
            t.util.initialize();
            return t;
        } catch (final Exception e) {
            throw new SQLEngineRuntimeException(e);
        }
    }

    /**
	 * Initialize in db.
	 *
	 * @param <T> the generic type
	 * @param tableClass the table class
	 * @param alias the alias
	 * @return the t
	 * @throws SQLEngineRuntimeException the sQL engine runtime exception
	 */
    public static <T extends SqlDdlTable.Implementation> T initializeInDb(final Class<T> tableClass, final String alias) throws SQLEngineRuntimeException {
        return initializeFor(defaultDatabaseGateway, tableClass, alias);
    }

    /**
	 * Initialize for.
	 *
	 * @param <T> the generic type
	 * @param dbc the dbc
	 * @param tableClass the table class
	 * @param alias the alias
	 * @return the t
	 * @throws SQLEngineRuntimeException the sQL engine runtime exception
	 */
    public static <T extends SqlDdlTable.Implementation> T initializeFor(final DatabaseGateway<?> dbc, final Class<T> tableClass, final String alias) throws SQLEngineRuntimeException {
        try {
            final T t = tableClass.getConstructor(String.class).newInstance(alias);
            t.db().initializeFor(dbc);
            return t;
        } catch (final Exception e) {
            throw new SQLEngineRuntimeException(e);
        }
    }

    /**
	 * Executes <code>update</code> with <code>parameters</code>.
	 * If 0 rows have been affected by the UPDATE, then an INSERT with the
	 * same column-value assignment and target table is derived from the UPDATE and executed.
	 * <p>
	 * A save is meant to update or insert values for a single row of data automatically as needed.
	 * Still it is possible, to update multiple rows via call of this method, if the conditions in <code>update</code>
	 * are set in the right way (or "wrong" way, if you intended to only update one row.)
	 * <p>
	 * If the UPDATE already affects rows, the performance of this method is the same as calling
	 * <code>UPDATE.execute()</code> directly. Only if an INSERT is really needed additional work is done.
	 *
	 * @param update
	 * @param parameters
	 * @return the number of affected rows
	 */
    public static int save(final UPDATE update, final Object... parameters) {
        int updateRowCount = update.execute(parameters);
        if (updateRowCount == 0) {
            updateRowCount = update.getLinkedINSERT().execute(parameters);
        }
        return updateRowCount;
    }

    /**
	 * The Class config.
	 */
    public static class config {

        /** The logging level execute. */
        private static Level loggingLevelExecute = Level.OFF;

        /** The global logging aspect. */
        private static LoggingAspect globalLoggingAspect = new LoggingAspect(null, Level.OFF);

        /** The Constant param. */
        public static String param = "?";

        /** The Constant prepareParamRegExp. */
        public static String prepareParamRegExp = "\\?";

        /** The Constant default_subSelectLevelSpace. */
        public static String default_subSelectLevelSpace = TAB;

        /** The Constant list_CommaNewLine. */
        public static boolean list_CommaNewLine = true;

        /** The Constant TablePrefix. */
        public static String TablePrefix = "Tbl";

        /** The Constant TableSuffix. */
        public static String TableSuffix = "";

        /** The Constant IndexPrefix. */
        public static String IndexPrefix = "Idx";

        /** The Constant IndexSuffix. */
        public static String IndexSuffix = "";

        /** The Constant DefaultSchema. */
        public static String DefaultSchema = null;

        /** The Constant SQLTRUE. */
        public static final String SQLTRUE = "SQLTRUE()";

        /** The Constant SQLFALSE. */
        public static final String SQLFALSE = "SQLFALSE()";

        /**
		 * Gets the logging level execute.
		 *
		 * @return the levelExecute
		 */
        public static Level getLoggingLevelExecute() {
            return loggingLevelExecute;
        }

        /**
		 * Gets the global logging aspect.
		 *
		 * @return the globalLoggingAspect
		 */
        public static LoggingAspect getGlobalLoggingAspect() {
            return globalLoggingAspect;
        }

        /**
		 * Sets the logging level execute.
		 *
		 * @param levelExecute the levelExecute to set
		 */
        public static void setGlobalLoggingLevelExecute(final Level levelExecute) {
            loggingLevelExecute = levelExecute;
        }

        /**
		 * Sets the logging default.
		 *
		 * @param globalLoggingAspect the globalLoggingAspect to set as default
		 */
        public static void setGlobalLogger(final LoggingAspect globalLoggingAspect) {
            config.globalLoggingAspect = globalLoggingAspect;
        }

        /**
		 * Sets the logging default.
		 *
		 * @param logger the new logging default
		 */
        public static void setGlobalLogger(final Logger logger) {
            config.globalLoggingAspect = new LoggingAspect(logger, Level.CONFIG);
        }

        /**
		 * Sets the logging default.
		 *
		 * @param logger the logger
		 * @param defaultLevel the default level
		 */
        public static void setGlobalLogger(final Logger logger, final Level defaultLevel) {
            config.globalLoggingAspect = new LoggingAspect(logger, defaultLevel);
        }
    }

    /**
	 * The Class Punctuation.
	 */
    public abstract static class Punctuation {

        /** The Constant n. */
        public static final char n = '\n';

        /** The Constant t. */
        public static final char t = '\t';

        /** The Constant qt. */
        public static final char qt = '"';

        /** The Constant apo. */
        public static final char apo = '\'';

        /** The Constant at. */
        public static final char at = '@';

        /** The Constant cma. */
        public static final char cma = ',';

        /** The Constant str. */
        public static final char str = '*';

        /** The Constant d. */
        public static final char d = '.';

        /** The Constant qM. */
        public static final char qM = '?';

        /** The Constant _. */
        public static final char _ = ' ';

        /** The Constant NEW_LINE. */
        public static final String NEW_LINE = "" + n;

        public static final String SPACE = "" + _;

        /** The Constant TAB. */
        public static final String TAB = "" + t;

        /** The Constant quote. */
        public static final String quote = "" + qt;

        /** The Constant scol. */
        public static final String scol = ";";

        /** The Constant par. */
        public static final String par = "(";

        /** The Constant rap. */
        public static final String rap = ")";

        /** The Constant comma. */
        public static final String comma = "" + cma;

        /** The Constant dot. */
        public static final String dot = "" + d;

        /** The Constant star. */
        public static final String star = "" + str;

        /** The Constant qMark. */
        public static final String qMark = "" + qM;

        /** The Constant is. */
        public static final String is = "=";

        /** The Constant ne1. */
        public static final String ne1 = "!=";

        /** The Constant ne2. */
        public static final String ne2 = "<>";

        /** The Constant gt. */
        public static final String gt = ">";

        /** The Constant lt. */
        public static final String lt = "<";

        /** The Constant gte. */
        public static final String gte = ">=";

        /** The Constant lte. */
        public static final String lte = "<=";

        /** The Constant comma_. */
        public static final String comma_ = cma + "" + _;

        /** The Constant eq. */
        public static final String eq = is;

        /** The Constant _eq_. */
        public static final String _eq_ = _ + eq + _;

        /** The Constant ne. */
        public static final String ne = ne1;

        /** The Constant selectItem_Comma_NEW_LINE. */
        public static final String selectItem_Comma_NEW_LINE = comma + NEW_LINE;

        /** The Constant selectItem_NEW_LINE_Comma. */
        public static final String selectItem_NEW_LINE_Comma = NEW_LINE + comma;

        /** The Constant singleLineComment. */
        public static final String singleLineComment = "--";

        /** The Constant blockCommentStart. */
        public static final String blockCommentStart = "/*";

        /** The Constant blockCommentEnd. */
        public static final String blockCommentEnd = "*/";
    }

    /**
	 * The Class LANG.
	 */
    public abstract static class LANG {

        /** The Constant ABS. */
        public static final String ABS = "ABS";

        /** The Constant ALL. */
        public static final String ALL = "ALL";

        /** The Constant ALTER. */
        public static final String ALTER = "ALTER";

        /** The Constant AND. */
        public static final String AND = "AND";

        /** The Constant AS. */
        public static final String AS = "AS";

        /** The Constant ASC. */
        public static final String ASC = "ASC";

        /** The Constant AVG. */
        public static final String AVG = "AVG";

        /** The Constant BEGIN_ATOMIC. */
        public static final String BEGIN_ATOMIC = "BEGIN ATOMIC";

        /** The Constant BETWEEN. */
        public static final String BETWEEN = "BETWEEN";

        /** The Constant BITMAP. */
        public static final String BITMAP = "BITMAP";

        /** The Constant BY. */
        public static final String BY = "BY";

        /** The Constant CALL. */
        public static final String CALL = "CALL";

        /** The Constant CASE. */
        public static final String CASE = "CASE";

        /** The Constant CAST. */
        public static final String CAST = "CAST";

        /** The Constant COLLECT. */
        public static final String COLLECT = "COLLECT";

        /** The Constant THEN. */
        public static final String THEN = "THEN";

        /** The Constant ELSE. */
        public static final String ELSE = "ELSE";

        /** The Constant CHAR. */
        public static final String CHAR = "CHAR";

        /** The Constant COALESCE. */
        public static final String COALESCE = "COALESCE";

        /** The Constant CONSTRAINT. */
        public static final String CONSTRAINT = "CONSTRAINT";

        /** The Constant CONTAINS. */
        public static final String CONTAINS = "CONTAINS";

        /** The Constant COUNT. */
        public static final String COUNT = "COUNT";

        /** The Constant CREATE. */
        public static final String CREATE = "CREATE";

        /** The Constant TABLE. */
        public static final String DATE = "DATE";

        /** The Constant DEFAULT. */
        public static final String DEFAULT = "DEFAULT";

        /** The Constant DELETE. */
        public static final String DELETE = "DELETE";

        /** The Constant DESC. */
        public static final String DESC = "DESC";

        /** The Constant DISTINCT. */
        public static final String DISTINCT = "DISTINCT";

        /** The Constant DROP. */
        public static final String DROP = "DROP";

        /** The Constant END. */
        public static final String END = "END";

        /** The Constant EXISTS. */
        public static final String EXISTS = "EXISTS";

        /** The Constant FETCH. */
        public static final String FETCH = "FETCH";

        /** The Constant FIRST. */
        public static final String FIRST = "FIRST";

        /** The Constant FOREIGN. */
        public static final String FOREIGN = "FOREIGN";

        /** The Constant FUSION. */
        public static final String FUSION = "FUSION";

        /** The Constant FROM. */
        public static final String FROM = "FROM";

        /** The Constant FULL. */
        public static final String FULL = "FULL";

        /** The Constant GLOBAL. */
        public static final String GLOBAL = "GLOBAL";

        /** The Constant GROUP. */
        public static final String GROUP = "GROUP";

        /** The Constant HAVING. */
        public static final String HAVING = "HAVING";

        /** The Constant IN. */
        public static final String IN = "IN";

        /** The Constant INDEX. */
        public static final String INDEX = "INDEX";

        /** The Constant INNER. */
        public static final String INNER = "INNER";

        /** The Constant INSERT. */
        public static final String INSERT = "INSERT";

        /** The Constant INTO. */
        public static final String INTO = "INTO";

        /** The Constant IS. */
        public static final String IS = "IS";

        /** The Constant JOIN. */
        public static final String JOIN = "JOIN";

        /** The Constant KEY. */
        public static final String KEY = "KEY";

        /** The Constant LEFT. */
        public static final String LEFT = "LEFT";

        /** The Constant LIKE. */
        public static final String LIKE = "LIKE";

        /** The Constant MOD. */
        public static final String MOD = "MOD";

        /** The Constant MAX. */
        public static final String MAX = "MAX";

        /** The Constant MIN. */
        public static final String MIN = "MIN";

        /** The Constant NATURAL. */
        public static final String NATURAL = "NATURAL";

        /** The Constant NEW. */
        public static final String NEW = "NEW";

        /** The Constant NOT. */
        public static final String NOT = "NOT";

        /** The Constant NULL. */
        public static final String NULL = "NULL";

        /** The Constant NUMBER. */
        public static final String NUMBER = "NUMBER";

        /** The Constant OFFSET. */
        public static final String OFFSET = "OFFSET";

        /** The Constant OLD. */
        public static final String OLD = "OLD";

        /** The Constant ON. */
        public static final String ON = "ON";

        /** The Constant ONLY. */
        public static final String ONLY = "ONLY";

        /** The Constant OR. */
        public static final String OR = "OR";

        /** The Constant ORDER. */
        public static final String ORDER = "ORDER";

        /** The Constant OUTER. */
        public static final String OUTER = "OUTER";

        /** The Constant PRIMARY. */
        public static final String PRIMARY = "PRIMARY";

        /** The Constant PROCEDURE. */
        public static final String PROCEDURE = "PROCEDURE";

        /** The Constant REFERENCING. */
        public static final String REFERENCING = "REFERENCING";

        /** The Constant RIGHT. */
        public static final String RIGHT = "RIGHT";

        /** The Constant ROUND. */
        public static final String ROUND = "ROUND";

        /** The Constant ROW. */
        public static final String ROW = "ROW";

        /** The Constant ROWS. */
        public static final String ROWS = "ROWS";

        /** The Constant SELECT. */
        public static final String SELECT = "SELECT";

        /** The Constant SET. */
        public static final String SET = "SET";

        /** The Constant SUBSTRING. */
        public static final String SUBSTRING = "SUBSTRING";

        /** The Constant SUBSTRING_REGEX. */
        public static final String SUBSTRING_REGEX = "SUBSTRING_REGEX";

        /** The Constant SUM. */
        public static final String SUM = "SUM";

        /** The Constant TABLE. */
        public static final String TABLE = "TABLE";

        /** The Constant TEMPORARY. */
        public static final String TEMPORARY = "TEMPORARY";

        /** The Constant TABLE. */
        public static final String TIME = "TIME";

        /** The Constant TABLE. */
        public static final String TIMESTAMP = "TIMESTAMP";

        /** The Constant TO. */
        public static final String TO = "TO";

        /** The Constant TOP. */
        public static final String TOP = "TOP";

        /** The Constant TRIGGER. */
        public static final String TRIGGER = "TRIGGER";

        /** The Constant TRUNCATE. */
        public static final String TRUNCATE = "TRUNCATE";

        /** The Constant UNION. */
        public static final String UNION = "UNION";

        /** The Constant INTERSECT. */
        public static final String INTERSECT = "INTERSECT";

        /** The Constant INTERSECTION. */
        public static final String INTERSECTION = "INTERSECTION";

        /** The Constant EXCEPT. */
        public static final String EXCEPT = "EXCEPT";

        /** The Constant UNIQUE. */
        @Label(SqlEngineLabels.LABEL_UNIQUE)
        public static final String UNIQUE = "UNIQUE";

        /** The Constant UPDATE. */
        public static final String UPDATE = "UPDATE";

        /** The Constant VALUES. */
        public static final String VALUES = "VALUES";

        /** The Constant WHEN. */
        public static final String WHEN = "WHEN";

        /** The Constant WHERE. */
        public static final String WHERE = "WHERE";

        /** The Constant CURRENT_CATALOG. */
        public static final String CURRENT_CATALOG = "CURRENT_USER";

        /** The Constant CURRENT_SCHEMA. */
        public static final String CURRENT_SCHEMA = "CURRENT_SCHEMA";

        /** The Constant CURRENT_TIME. */
        public static final String CURRENT_TIME = "CURRENT_TIME";

        /** The Constant CURRENT_TIMESTAMP. */
        public static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";

        /** The Constant CURRENT_USER. */
        public static final String CURRENT_USER = "CURRENT_USER";

        /** The Constant DEFAULT_VALUES. */
        public static final String DEFAULT_VALUES = glue(DEFAULT, VALUES);

        /** The Constant CREATE_TABLE. */
        public static final String CREATE_TABLE = glue(CREATE, TABLE);

        /** The Constant CREATE_GLOBAL_TEMPORARY_TABLE. */
        public static final String CREATE_GLOBAL_TEMPORARY_TABLE = glue(CREATE, GLOBAL, TEMPORARY, TABLE);

        /** The Constant CREATE_TRIGGER. */
        public static final String CREATE_TRIGGER = glue(CREATE, TRIGGER);

        /** The Constant CREATE_PROCEDURE. */
        public static final String CREATE_PROCEDURE = glue(CREATE, PROCEDURE);

        /** The Constant DROP_TABLE. */
        public static final String DROP_TABLE = glue(DROP, TABLE);

        /** The Constant CREATE_INDEX. */
        public static final String CREATE_INDEX = glue(CREATE, INDEX);

        /** The Constant DROP_INDEX. */
        public static final String DROP_INDEX = glue(DROP, INDEX);

        /** The Constant NOT_NULL. */
        @Label(LABEL_NOTNULL)
        public static final String NOT_NULL = glue(NOT, NULL);

        /** The Constant PRIMARY_KEY. */
        public static final String PRIMARY_KEY = glue(PRIMARY, KEY);

        /** The Constant FOREIGN_KEY. */
        public static final String FOREIGN_KEY = glue(FOREIGN, KEY);

        /** The Constant TRUNCATE_TABLE. */
        public static final String TRUNCATE_TABLE = glue(TRUNCATE, TABLE);

        /** The Constant ROW_NUMBER. */
        public static final String ROW_NUMBER = glue(ROW, NUMBER);

        /** The Constant INSERT_INTO. */
        public static final String INSERT_INTO = glue(INSERT, INTO);

        /** The Constant DISTINCT_BY. */
        public static final String DISTINCT_BY = glue(DISTINCT, BY);

        /** The Constant GROUP_BY. */
        public static final String GROUP_BY = glue(GROUP, BY);

        /** The Constant ORDER_BY. */
        public static final String ORDER_BY = glue(ORDER, BY);

        /** The Constant INNER_JOIN. */
        public static final String INNER_JOIN = glue(INNER, JOIN);

        /** The Constant LEFT_JOIN. */
        public static final String LEFT_JOIN = glue(LEFT, JOIN);

        /** The Constant RIGHT_JOIN. */
        public static final String RIGHT_JOIN = glue(RIGHT, JOIN);

        /** The Constant OUTER_JOIN. */
        public static final String OUTER_JOIN = glue(FULL, OUTER, JOIN);

        /** The Constant NATURAL_JOIN. */
        public static final String NATURAL_JOIN = glue(NATURAL, JOIN);

        /** The Constant IS_NOT. */
        public static final String IS_NOT = glue(IS, NOT);

        /** The Constant NOT_IN. */
        public static final String NOT_IN = glue(NOT, IN);

        /** The Constant IS_NULL. */
        public static final String IS_NULL = glue(IS, NULL);

        /** The Constant IS_NOT_NULL. */
        public static final String IS_NOT_NULL = glue(IS, NOT, NULL);

        /** The Constant TO_NUMBER. */
        public static final String TO_NUMBER = TO + '_' + NUMBER;

        /** The Constant TO_CHAR. */
        public static final String TO_CHAR = glue(CHAR);

        /** The Constant UNION_ALL. */
        public static final String UNION_ALL = glue(UNION, ALL);

        /** The Constant _AS_. */
        public static final String _AS_ = _ + AS + _;

        /** The Constant _ON_. */
        public static final String _ON_ = _ + ON + _;

        /** The Constant __AND. */
        public static final String __AND = "  " + AND;

        /** The Constant __OR_. */
        public static final String __OR_ = "  " + OR + _;

        /** The Constant _OR_. */
        public static final String _OR_ = _ + OR + _;

        /** The Constant _AND_. */
        public static final String _AND_ = _ + AND + _;
    }

    /**
	 * Lookup sql table definition method name.
	 *
	 * @param label the label
	 * @return the string
	 */
    static final String lookupSqlTableDefinitionMethodName(final String label) {
        try {
            return getMemberByLabel(label, SqlTable.Implementation.class.getDeclaredMethods()).getName();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
	 * The Enum INDEXTYPE.
	 */
    public static enum INDEXTYPE {

        /** The NORMAL. */
        NORMAL(null, lookupSqlTableDefinitionMethodName(LABEL_METHOD_Index)), /** The UNIQUE. */
        UNIQUE(SQL.LANG.UNIQUE, lookupSqlTableDefinitionMethodName(LABEL_METHOD_UniqueIndex)), /** The BITMAP. */
        BITMAP(SQL.LANG.BITMAP, lookupSqlTableDefinitionMethodName(LABEL_METHOD_BitmapIndex)), /** The PRIMARYKEY. */
        PRIMARYKEY(SQL.LANG.PRIMARY_KEY, lookupSqlTableDefinitionMethodName(LABEL_METHOD_PrimaryKey)), /** The FOREIGNKEY. */
        FOREIGNKEY(SQL.LANG.FOREIGN_KEY, lookupSqlTableDefinitionMethodName(LABEL_METHOD_ForeignKey));

        /** The ddl string. */
        private final String ddlString;

        /** The sqltable definition method name. */
        private final String sqltableDefinitionMethodName;

        /**
		 * Instantiates a new iNDEXTYPE.
		 *
		 * @param ddlString the ddl string
		 * @param sqltableDefinitionMethodName the sqltable definition method name
		 */
        private INDEXTYPE(final String ddlString, final String sqltableDefinitionMethodName) {
            this.ddlString = ddlString;
            this.sqltableDefinitionMethodName = sqltableDefinitionMethodName;
        }

        /**
		 * To ddl string.
		 *
		 * @return the ddlString
		 */
        public String toDdlString() {
            return this.ddlString;
        }

        /**
		 * Gets the sqltable definition method name.
		 *
		 * @return the sqltableDefinitionMethodName
		 */
        public String getSqltableDefinitionMethodName() {
            return this.sqltableDefinitionMethodName;
        }
    }

    static class Flag {

        static final int ISNUMBER = 1 << 0;

        static final int ISINTEGER = 1 << 1 | ISNUMBER;

        static final int ISDECIMAL = 1 << 2 | ISNUMBER;

        static final int ISTIME = 1 << 3;

        static final int ISLITERAL = 1 << 4;

        static final int ISNATIONAL = 1 << 5 | ISLITERAL;

        static final int ISBINARY = 1 << 6;

        static final int ISLARGE = 1 << 7;

        static final int ISVARYING = 1 << 8;

        static final int ISSCALED = 1 << 9;

        static final int ISPRECISED = 1 << 10;

        static final int ISLENGTHED = 1 << 11;

        static final int ISBIG = 1 << 12;

        static final int ISTINY = 1 << 13;
    }

    public static enum DATATYPE {

        BOOLEAN(java.sql.Types.BOOLEAN, ISTINY), TINYINT(java.sql.Types.TINYINT, ISINTEGER | ISTINY), SMALLINT(java.sql.Types.SMALLINT, ISINTEGER), INT(java.sql.Types.INTEGER, ISINTEGER), BIGINT(java.sql.Types.BIGINT, ISINTEGER | ISBIG), REAL(java.sql.Types.REAL, ISDECIMAL), FLOAT(java.sql.Types.FLOAT, ISDECIMAL), DOUBLE(java.sql.Types.DOUBLE, ISDECIMAL | ISBIG), NUMERIC(java.sql.Types.NUMERIC, ISDECIMAL | ISSCALED | ISPRECISED), DECIMAL(java.sql.Types.DECIMAL, ISDECIMAL | ISSCALED | ISPRECISED), DATE(java.sql.Types.DATE, ISTIME), TIME(java.sql.Types.TIME, ISTIME), TIMESTAMP(java.sql.Types.TIMESTAMP, ISTIME | ISBIG), CHAR(java.sql.Types.CHAR, ISLENGTHED | ISLITERAL), VARCHAR(java.sql.Types.VARCHAR, ISLENGTHED | ISLITERAL | ISVARYING), CLOB(java.sql.Types.CLOB, ISLENGTHED | ISLITERAL | ISLARGE), LONGVARCHAR(java.sql.Types.LONGVARCHAR, ISLENGTHED | ISLITERAL | ISVARYING, CLOB.toDdlString()), NCHAR(java.sql.Types.NCHAR, ISLENGTHED | ISNATIONAL), NVARCHAR(java.sql.Types.NVARCHAR, ISLENGTHED | ISNATIONAL | ISVARYING), NCLOB(java.sql.Types.NCLOB, ISLENGTHED | ISNATIONAL | ISLARGE), LONGNVARCHAR(java.sql.Types.LONGNVARCHAR, ISLENGTHED | ISNATIONAL | ISLARGE, NCLOB.toDdlString()), BINARY(java.sql.Types.BINARY, ISLENGTHED | ISBINARY), VARBINARY(java.sql.Types.VARBINARY, ISLENGTHED | ISBINARY | ISVARYING), BLOB(java.sql.Types.BLOB, ISLENGTHED | ISBINARY | ISLARGE), LONGVARBINARY(java.sql.Types.LONGVARBINARY, ISLENGTHED | ISBINARY | ISLARGE, BLOB.toDdlString()), OBJECT(java.sql.Types.JAVA_OBJECT, 0), ARRAY(java.sql.Types.ARRAY, 0);

        private int jdbcType = 0;

        private final int flags;

        private String ddlString = null;

        private DATATYPE(final int jdbcType, final int flags) {
            this(jdbcType, flags, null);
        }

        private DATATYPE(final int jdbcType, final int flags, final String ddlString) {
            this.jdbcType = jdbcType;
            this.ddlString = ddlString;
            this.flags = flags;
        }

        /**
		 * Returns the amount of attributes that match with the other data type.
		 *
		 * @param other
		 * @return the matching attributes count.
		 */
        public double similarity(final DATATYPE other) {
            if (this == other) {
                return 1.0D;
            }
            if (other == ARRAY || other == OBJECT) {
                return 0.0D;
            }
            return Math.max(0.0D, 1.0D - 0.1D * (Integer.bitCount(this.flags ^ other.flags) + 1));
        }

        public boolean isLengthed() {
            return (this.flags & Flag.ISLENGTHED) > 0;
        }

        public boolean isLiteral() {
            return (this.flags & Flag.ISLITERAL) > 0;
        }

        public int getJdbcType() {
            return this.jdbcType;
        }

        public String toDdlString() {
            return this.ddlString != null ? this.ddlString : this.name();
        }

        public boolean isPrecisioned() {
            return (this.flags & Flag.ISPRECISED) > 0;
        }

        public boolean isScaled() {
            return (this.flags & Flag.ISSCALED) > 0;
        }

        private static final String lengthedType(final DATATYPE type, final int length) {
            return new StringBuilder(64).append(type).append(Punctuation.par).append(length).append(Punctuation.rap).toString();
        }

        public static final String CHAR(final int length) {
            return lengthedType(CHAR, length);
        }

        public static final String VARCHAR(final int length) {
            return lengthedType(VARCHAR, length);
        }

        public static final String BINARY(final int length) {
            return lengthedType(BINARY, length);
        }

        public static final String VARBINARY(final int length) {
            return lengthedType(VARBINARY, length);
        }

        public static final String LONGVARCHAR(final int length) {
            return lengthedType(LONGVARCHAR, length);
        }

        public static final String LONGVARBINARY(final int length) {
            return lengthedType(LONGVARBINARY, length);
        }
    }

    public abstract static class FORMAT {

        public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";

        public static final String DATE = "yyyy-MM-dd";

        public static final String TIME = "HH:mm:ss.SSS";
    }

    public abstract static class util {

        public static final boolean needsEscaping(final DATATYPE columnType) {
            return columnType.ordinal() <= 6;
        }

        protected static final char c_apo = net.jadoth.sqlengine.SQL.Punctuation.apo;

        public static VarChar assembleEscape(VarChar sb, final String s) {
            if (s == null) return null;
            final int len = s.length();
            if (sb == null) {
                sb = new VarChar((int) (len * 1.1 + 4));
            }
            final char[] cs = new char[len];
            s.getChars(0, len, cs, 0);
            char c = 0;
            sb.append(c_apo);
            for (int i = 0; i < len; i++) {
                c = cs[i];
                sb.append(c);
                if (c == c_apo) sb.append(c);
            }
            sb.append(c_apo);
            return sb;
        }

        public static String escape(final String s) {
            return assembleEscape(null, s).toString();
        }

        @Label({ LABEL_SQL_util_guessAlias, LABEL_1Param })
        public static final String guessAlias(final String tablename) {
            return guessAlias(tablename, 2, 4);
        }

        @Label({ LABEL_SQL_util_guessAlias, LABEL_2Param })
        public static final String guessAlias(final String tablename, final int charCount) {
            return guessAlias(tablename, charCount, charCount);
        }

        public static final String escapeReservedWord(final String s) {
            if (getDefaultDBMS().getSyntax().isReservedWord(s.toUpperCase())) {
                return s + 'x';
            }
            return s;
        }

        @Label({ LABEL_SQL_util_guessAlias, LABEL_3Param })
        public static final String guessAlias(final String tablename, final int minChars, final int maxChars) {
            final int length = tablename.length();
            if (length <= minChars) {
                return tablename.toUpperCase();
            }
            final char[] chars = tablename.toCharArray();
            final char first = tablename.charAt(0);
            boolean currentUpper = Character.isUpperCase(first);
            final Vector<Character> significantChars = new Vector<Character>(maxChars);
            significantChars.add(first);
            char loopChar = first;
            boolean loopUpper = !currentUpper;
            for (int i = 0; i < chars.length; i++) {
                loopChar = tablename.charAt(i);
                loopUpper = Character.isUpperCase(loopChar);
                if (loopUpper != currentUpper) {
                    significantChars.add(loopChar);
                    currentUpper = !currentUpper;
                }
            }
            final StringBuilder sb = new StringBuilder(significantChars.size());
            for (final Character c : significantChars) {
                sb.append(c);
            }
            final String reducedName = sb.toString();
            String returnCandidate = reducedName;
            if (returnCandidate.length() < minChars) {
                returnCandidate = tablename.substring(0, Math.min(Math.min((minChars + maxChars) / 2 + 1, maxChars), length));
                return returnCandidate.toUpperCase();
            }
            if (returnCandidate.length() > maxChars) {
                returnCandidate = reducedName.replaceAll("[^a-zA-Z]", "");
            }
            if (returnCandidate.length() > maxChars) {
                returnCandidate = reducedName.replaceAll("[a-z]", "");
            }
            if (returnCandidate.length() > maxChars) {
                returnCandidate = reducedName.replaceAll("[^A-Z]", "");
            }
            if (returnCandidate.length() > maxChars) {
                returnCandidate = returnCandidate.substring(0, Math.min(maxChars, length));
            }
            return returnCandidate.toUpperCase();
        }

        /**
		 * Utility method than recognizes Boolean values.<br>
		 * <code>object</code> can have several values that are recognized as boolean values:
		 * - null value: returns null<br>
		 * - Boolean value: returns object<br>
		 * - Number value: return false for value 0, true otherwise<br>
		 * - String value (caseinsensitive): returns false for "N" or "NO" , true for "Y" or "YES", otherwise value is not recognized<br>
		 * <br>
		 * Any unrecognizable value will cause a <code>ClassCastException</code>.<br>
		 * <br>
		 * Notes:<br>
		 * - this method is inefficient if the type of <code>object</code> is already known.<br>
		 * - use this method only if you are sure that the provided value is meant to be a boolean value of some form
		 * - the main purpose of this method is to handle boolean and pseudo-boolean values returned from different
		 * DBMS generically, no matter if it's a true boolean or 0/1 or stupid 0/-1 or "YES/NO", etc.
		 *
		 * @param object the object
		 * @return the boolean
		 * @throws ClassCastException the class cast exception
		 */
        public static final Boolean recognizeBoolean(final Object object) throws ClassCastException {
            if (object == null) return null;
            if (object instanceof Boolean) {
                return (Boolean) object;
            } else if (object instanceof Number) {
                return ((Number) object).intValue() != 0;
            } else if (object instanceof String) {
                final String upperCaseValue = object.toString().toUpperCase();
                if (upperCaseValue.equals("Y") || upperCaseValue.equals("YES")) return true;
                if (upperCaseValue.equals("N") || upperCaseValue.equals("NO")) return false;
            }
            throw new ClassCastException("value " + object + " of " + object.getClass() + " cannot be recognized as boolean value");
        }

        /**
		 * Like <code>recognizeBoolean</code>, but maps {@code null} value to <code>false</code>.
		 *
		 * @param object the object
		 * @return true or false :-)
		 * @throws ClassCastException the class cast exception
		 */
        public static final boolean recognizeBooleanPrimitive(final Object object) throws ClassCastException {
            final Boolean b = recognizeBoolean(object);
            return b == null ? false : b;
        }

        /**
		 * Escape if necessary.
		 *
		 * @param s the s
		 * @param sb the sb
		 * @return the string builder
		 */
        public static final VarChar escapeIfNecessary(final String s, VarChar sb) {
            if (sb == null) {
                sb = new VarChar(s == null ? 4 : (int) (s.length() * 1.1 + 2));
            }
            if (s == null) {
                return sb.append(SQL.LANG.NULL);
            }
            if (s.isEmpty()) {
                return sb.append(apo).append(apo);
            }
            try {
                Double.parseDouble(s);
                sb.append(s);
            } catch (final NumberFormatException e) {
                assembleEscape(sb, s);
            }
            return sb;
        }

        /**
		 * Escape if necessary.
		 *
		 * @param s the s
		 * @return the string
		 */
        public static final String escapeIfNecessary(final String s) {
            return escapeIfNecessary(s, null).toString();
        }

        /**
		 * Parses the expression.
		 *
		 * @param obj the obj
		 * @return the sql expression
		 */
        public static final SqlExpression parseExpression(final Object obj) {
            if (obj == null) {
                return null;
            } else if (obj instanceof SqlExpression) {
                return (SqlExpression) obj;
            } else if (obj instanceof Boolean) {
                return (Boolean) obj ? SQL.TRUE : SQL.FALSE;
            } else {
                return new SqlExpression(obj);
            }
        }

        /**
		 * Parses the expression array.
		 *
		 * @param objects the objects
		 * @return the sql expression[]
		 */
        public static final SqlExpression[] parseExpressionArray(Object... objects) {
            if (objects == null) {
                return null;
            } else if (objects instanceof SqlExpression[]) {
                return (SqlExpression[]) objects;
            } else {
                if (objects.length == 1 && objects[0] instanceof Collection<?>) {
                    objects = ((Collection<?>) objects[0]).toArray();
                } else if (objects.length == 1 && objects[0] instanceof XGettingCollection<?>) {
                    objects = ((XGettingCollection<?>) objects[0]).toArray();
                }
                final SqlExpression[] exps = new SqlExpression[objects.length];
                for (int i = 0; i < exps.length; i++) {
                    exps[i] = parseExpression(objects[i]);
                }
                return exps;
            }
        }

        /**
		 * Parses the condition.
		 *
		 * @param object the object
		 * @return the sql condition
		 */
        public static final SqlCondition parseCondition(final Object object) {
            if (object == null) {
                return null;
            } else if (object instanceof SqlCondition) {
                return (SqlCondition) object;
            } else {
                return new SqlCondition(object);
            }
        }

        /**
		 * Parses the full qualified tablename.
		 *
		 * @param fullQualifiedTablename the full qualified tablename
		 * @return the string[]
		 */
        public static final String[] parseFullQualifiedTablename(final String fullQualifiedTablename) {
            if (fullQualifiedTablename == null) return null;
            final String[] parts = new String[3];
            final int dotIndex = fullQualifiedTablename.indexOf(dot);
            String unQualTablename;
            if (dotIndex > 0) {
                parts[0] = fullQualifiedTablename.substring(0, dotIndex);
                unQualTablename = fullQualifiedTablename.substring(dotIndex + 1);
            } else {
                unQualTablename = fullQualifiedTablename;
            }
            final int firstSpaceIndex = unQualTablename.indexOf(_);
            if (firstSpaceIndex > 0) {
                parts[1] = unQualTablename.substring(0, firstSpaceIndex);
                parts[2] = unQualTablename.substring(unQualTablename.lastIndexOf(_) + 1, unQualTablename.length());
            } else {
                parts[1] = unQualTablename;
                parts[2] = null;
            }
            return parts;
        }

        /**
		 * Assemble timestamp.
		 *
		 * @param parts the parts
		 * @return the string
		 */
        public static final String assembleTimestamp(final int... parts) {
            final StringBuilder sb = new StringBuilder(20);
            final int length = parts.length;
            sb.append(parts[0]);
            if (length > 1) {
                sb.append("-");
                sb.append(parts[1]);
            }
            if (length > 2) {
                sb.append("-");
                sb.append(parts[2]);
            }
            if (length > 3) {
                sb.append(" ");
                sb.append(parts[3]);
            }
            if (length > 4) {
                sb.append(":");
                sb.append(parts[4]);
            }
            if (length > 5) {
                sb.append(":");
                sb.append(parts[5]);
            }
            return sb.toString();
        }

        /**
		 * Qualify.
		 *
		 * @param parts the parts
		 * @return the string
		 */
        public static final String qualify(final Object... parts) {
            return Jadoth.concat(Punctuation.dot, parts);
        }

        /**
		 * Gets the column name.
		 *
		 * @param o the o
		 * @return the column name
		 */
        public static final String getColumnName(final Object o) {
            if (o == null) {
                return null;
            }
            if (o instanceof SqlField) {
                return ((SqlField) o).getName();
            } else if (o instanceof String) {
                return (String) o;
            } else {
                return o.toString();
            }
        }
    }
}
