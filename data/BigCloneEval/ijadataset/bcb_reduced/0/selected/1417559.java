package com.jdbwc.util;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jdbwc.core.WCConnection;
import com.ozdevworx.dtype.ObjectArray;

/**
 * This class is designed to parse an SQL <b>SELECT</b> String into table and field variables suitable for building ResultSetMetaData from.<br />
 * At the moment its a bit limited but should be ok for most traditional sql.<br />
 * Some SQL99 is also translated correctly.<br />
 * If we cant produce accurate metadata here we usually
 * throw an exception to ResultSetMetaData.<br />
 * <br />
 * This class can handle a variety of FUNCTIONS including nested functions and table JOINS.<br />
 * Nested functions will result in the first valid String parameter of
 * a multi-parameter function being used as the actual column name.<br />
 * The values alias (if any) will remain unchanged.
 *
 * @author Tim Gall
 * @version 2008-06
 * @version 2010-04-26
 */
public class SQLResultsParser {

    private transient SQLField[] fieldSet = new SQLField[0];

    private transient ObjectArray table2Column = null;

    private static final String MY_WS = "\\s+";

    private static final String MY_SELECT = "SELECT";

    private static final String MY_FROM = "FROM";

    private static final String MY_AS = "AS";

    private static final String MY_SEPERATOR = ",";

    private static final String MY_STRING_CLOSERS = "\\\"|\"|'|`";

    private static final String MY_DISTINCT = "DISTINCT";

    private static final Pattern mySelect = Pattern.compile(MY_SELECT + MY_WS, Pattern.CASE_INSENSITIVE);

    private static final Pattern myFrom = Pattern.compile(MY_WS + MY_FROM + MY_WS, Pattern.CASE_INSENSITIVE);

    private static final Pattern myFieldSeperator = Pattern.compile(MY_SEPERATOR, Pattern.CASE_INSENSITIVE);

    private static final Pattern myAlias = Pattern.compile(MY_WS + MY_AS + MY_WS, Pattern.CASE_INSENSITIVE);

    private static final Pattern myStringWrappers = Pattern.compile(MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);

    private static final Pattern myDistinct = Pattern.compile(MY_DISTINCT + MY_WS, Pattern.CASE_INSENSITIVE);

    public SQLResultsParser() {
    }

    /**
	 * Constructs a new SQLParser ready to return the ResultSetMetaData
	 * for the sqlString parameter.<br />
	 * <br />
	 * If the MySQL server is version 5.0.0 or greater
	 * INFORMATION_SCHEMA metaDataTables will be used to gather metaData in a single pass.<br />
	 * INFORMATION_SCHEMA metaDataTables will provide better performance.<br />
	 * <br />
	 * Otherwise, built-in MySQL query functions will be used to gather metaData in 2 passes.<br />
	 *
	 * @param connection Database Connection
	 * @param sqlString A valid SQL String
	 * @throws SQLException if the connection or sqlString
	 * are not valid for this Constructor.
	 */
    public SQLResultsParser(WCConnection connection, String sqlString) throws SQLException {
        String sql = SQLUtils.stripComments(sqlString);
        sql = SQLUtils.stripWhiteSpace(sql);
        processSQL(sql);
        Matcher matches = mySelect.matcher(sql);
        if (!matches.find()) {
            throw new SQLException("ResultSetMetaData only works with SELECT queries. SQL: " + sql, "S1009");
        }
        try {
            Class<?> metaClass = Class.forName(connection.getDbPackagePath() + "SQLMetaGetaImp");
            Constructor<?> ct = metaClass.getConstructor(new Class[] { connection.getClass() });
            SQLMetaGeta metaG = (SQLMetaGeta) ct.newInstance(new Object[] { connection });
            fieldSet = metaG.getResultSetMetaData(sql, table2Column);
        } catch (Throwable e) {
            throw new SQLException("Could not construct a SQLMetaGeta Object", e);
        }
    }

    public SQLField[] getFields() throws SQLException {
        return fieldSet;
    }

    private void processSQL(String sqlString) throws SQLException {
        table2Column = Util.getCaseSafeHandler(Util.CASE_MIXED);
        String workArea = sqlString.trim();
        Matcher matcher = myDistinct.matcher(workArea);
        if (matcher.find()) {
            workArea = matcher.replaceAll("");
        }
        String[] parts = SQLUtils.removeBlanks(mySelect.split(workArea));
        int selectsInQuery = parts.length;
        for (int i = 0; i < selectsInQuery; i++) {
            boolean firstSelect = false;
            String[] parts2 = SQLUtils.removeBlanks(myFrom.split(parts[i]));
            for (int j = 0; j < parts2.length; j++) {
                if (!firstSelect) {
                    firstSelect = true;
                    table2Column = seperateFieldEntries(parts2[j]);
                }
            }
        }
    }

    /**
	 *
	 *
	 * @param entries
	 * @return entries separated and stored in a ObjectArray.
	 */
    private ObjectArray seperateFieldEntries(String entries) {
        String fieldName = "";
        ObjectArray results = Util.getCaseSafeHandler(Util.CASE_MIXED);
        String[] myFields = SQLUtils.removeBlanks(myFieldSeperator.split(SQLUtils.stripWhiteSpace(entries)));
        String wholeField = "";
        for (int mf = 0; mf < myFields.length; mf++) {
            fieldName = myFields[mf];
            if (fieldName.contains("(")) {
                wholeField = fieldName;
                rebuilt: for (mf += 1; mf < myFields.length; mf++) {
                    wholeField += "," + myFields[mf];
                    if (myFields[mf].contains(")")) break rebuilt;
                }
            } else {
                wholeField = fieldName;
            }
            String[] nameNAlias = getFieldName(wholeField);
            results.addData(nameNAlias[0], nameNAlias[1]);
        }
        return results;
    }

    /**
	 * 0=alias, 1=name.
	 * Alias should always be unique if there is one
	 */
    private String[] getFieldName(String name) {
        String[] cleanName = new String[2];
        String fieldName = name.trim();
        String fieldAlias = "";
        Matcher matches = myStringWrappers.matcher(fieldName);
        if (matches.find()) {
            fieldName = matches.replaceAll("");
        }
        if (fieldName.indexOf('.') > -1) {
            fieldName = fieldName.substring(fieldName.indexOf('.') + 1);
        }
        Matcher fieldNAlias = myAlias.matcher(fieldName);
        if (fieldNAlias.find()) {
            String[] splitName = myAlias.split(fieldName);
            fieldName = splitName[0];
            fieldAlias = splitName[1];
        } else {
            fieldAlias = fieldName;
        }
        cleanName[0] = fieldAlias.trim();
        cleanName[1] = fieldName.trim();
        return cleanName;
    }
}
