package org.compiere.dbPort;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.compiere.model.MSysConfig;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Ini;

/**
 *  Convert SQL to Target DB
 *
 *  @author     Jorg Janke, Victor Perez
 *  @version    $Id: Convert.java,v 1.3 2006/07/30 00:55:04 jjanke Exp $
 */
public abstract class Convert {

    /** RegEx: insensitive and dot to include line end characters   */
    public static final int REGEX_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

    /** Statement used                  */
    protected Statement m_stmt = null;

    /** Last Conversion Error           */
    protected String m_conversionError = null;

    /** Last Execution Error            */
    protected Exception m_exception = null;

    /** Verbose Messages                */
    protected boolean m_verbose = true;

    /**	Logger	*/
    private static CLogger log = CLogger.getCLogger(Convert.class);

    private static FileOutputStream tempFileOr = null;

    private static DataOutputStream osOr;

    private static FileOutputStream tempFilePg = null;

    private static DataOutputStream osPg;

    /**
	 *  Set Verbose
	 *  @param verbose
	 */
    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    /**************************************************************************
	 *  Execute SQL Statement (stops at first error).
	 *  If an error occured hadError() returns true.
	 *  You can get details via getConversionError() or getException()
	 *  @param sqlStatements
	 *  @param conn connection
	 *  @return true if success
	 *  @throws IllegalStateException if no connection
	 */
    public boolean execute(String sqlStatements, Connection conn) {
        if (conn == null) throw new IllegalStateException("Require connection");
        String[] sql = convert(sqlStatements);
        m_exception = null;
        if (m_conversionError != null || sql == null) return false;
        boolean ok = true;
        int i = 0;
        String statement = null;
        try {
            if (m_stmt == null) m_stmt = conn.createStatement();
            for (i = 0; ok && i < sql.length; i++) {
                statement = sql[i];
                if (statement.length() == 0) {
                    if (m_verbose) log.finer("Skipping empty (" + i + ")");
                } else {
                    if (m_verbose) log.info("Executing (" + i + ") <<" + statement + ">>"); else log.info("Executing " + i);
                    try {
                        m_stmt.clearWarnings();
                        int no = m_stmt.executeUpdate(statement);
                        SQLWarning warn = m_stmt.getWarnings();
                        if (warn != null) {
                            if (m_verbose) log.info("- " + warn); else {
                                log.info("Executing (" + i + ") <<" + statement + ">>");
                                log.info("- " + warn);
                            }
                        }
                        if (m_verbose) log.fine("- ok " + no);
                    } catch (SQLException ex) {
                        if (!statement.startsWith("DROP ")) {
                            ok = false;
                            m_exception = ex;
                        }
                        if (!m_verbose) log.info("Executing (" + i + ") <<" + statement + ">>");
                        log.info("Error executing " + i + "/" + sql.length + " = " + ex);
                    }
                }
            }
        } catch (SQLException e) {
            m_exception = e;
            if (!m_verbose) log.info("Executing (" + i + ") <<" + statement + ">>");
            log.info("Error executing " + i + "/" + sql.length + " = " + e);
            return false;
        }
        return ok;
    }

    /**
	 *  Return last execution exception
	 *  @return execution exception
	 */
    public Exception getException() {
        return m_exception;
    }

    /**
	 *  Returns true if a conversion or execution error had occured.
	 *  Get more details via getConversionError() or getException()
	 *  @return true if error had occured
	 */
    public boolean hasError() {
        return (m_exception != null) | (m_conversionError != null);
    }

    /**
	 *  Convert SQL Statement (stops at first error).
	 *  Statements are delimited by /
	 *  If an error occured hadError() returns true.
	 *  You can get details via getConversionError()
	 *  @param sqlStatements
	 *  @return converted statement as a string
	 */
    public String convertAll(String sqlStatements) {
        String[] sql = convert(sqlStatements);
        StringBuffer sb = new StringBuffer(sqlStatements.length() + 10);
        for (int i = 0; i < sql.length; i++) {
            sb.append(sql[i]).append("\n/\n");
            if (m_verbose) log.info("Statement " + i + ": " + sql[i]);
        }
        return sb.toString();
    }

    /**
	 *  Convert SQL Statement (stops at first error).
	 *  If an error occured hadError() returns true.
	 *  You can get details via getConversionError()
	 *  @param sqlStatements
	 *  @return Array of converted Statements
	 */
    public String[] convert(String sqlStatements) {
        m_conversionError = null;
        if (sqlStatements == null || sqlStatements.length() == 0) {
            m_conversionError = "SQL_Statement is null or has zero length";
            log.info(m_conversionError);
            return null;
        }
        return convertIt(sqlStatements);
    }

    /**
	 *  Return last conversion error or null.
	 *  @return lst conversion error
	 */
    public String getConversionError() {
        return m_conversionError;
    }

    /**************************************************************************
	 *  Conversion routine (stops at first error).
	 *  <pre>
	 *  - convertStatement
	 *      - convertWithConvertMap
	 *      - convertComplexStatement
	 *      - decode, sequence, exception
	 *  </pre>
	 *  @param sqlStatements
	 *  @return array of converted statements
	 */
    protected String[] convertIt(String sqlStatements) {
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(convertStatement(sqlStatements));
        String[] sql = new String[result.size()];
        result.toArray(sql);
        return sql;
    }

    /**
	 * Clean up Statement. Remove trailing spaces, carrige return and tab 
	 * 
	 * @param statement
	 * @return sql statement
	 */
    protected String cleanUpStatement(String statement) {
        String clean = statement.trim();
        Matcher m = Pattern.compile("\\s+").matcher(clean);
        clean = m.replaceAll(" ");
        clean = clean.trim();
        return clean;
    }

    /**
	 * Utility method to replace quoted string with a predefined marker
	 * @param retValue
	 * @param retVars
	 * @return string
	 */
    protected String replaceQuotedStrings(String inputValue, Vector<String> retVars) {
        retVars.clear();
        Pattern p = Pattern.compile("'[[^']*]*'");
        Matcher m = p.matcher(inputValue);
        int i = 0;
        StringBuffer retValue = new StringBuffer(inputValue.length());
        while (m.find()) {
            retVars.addElement(new String(inputValue.substring(m.start(), m.end())));
            m.appendReplacement(retValue, "<--" + i + "-->");
            i++;
        }
        m.appendTail(retValue);
        return retValue.toString();
    }

    /**
	 * Utility method to recover quoted string store in retVars
	 * @param retValue
	 * @param retVars
	 * @return string
	 */
    protected String recoverQuotedStrings(String retValue, Vector<String> retVars) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < retVars.size(); i++) {
            String replacement = (String) retVars.get(i);
            replacement = escapeQuotedString(replacement);
            retValue = retValue.replace("<--" + i + "-->", replacement);
        }
        return retValue;
    }

    /**
	 * hook for database specific escape of quoted string ( if needed )
	 * @param in
	 * @return string
	 */
    protected String escapeQuotedString(String in) {
        return in;
    }

    /**
	 * Convert simple SQL Statement. Based on ConvertMap
	 * 
	 * @param sqlStatement
	 * @return converted Statement
	 */
    private String applyConvertMap(String sqlStatement) {
        if (sqlStatement.toUpperCase().indexOf("EXCEPTION WHEN") != -1) {
            String error = "Exception clause needs to be converted: " + sqlStatement;
            log.info(error);
            m_conversionError = error;
            return sqlStatement;
        }
        String retValue = sqlStatement;
        Pattern p;
        Matcher m;
        Map convertMap = getConvertMap();
        if (convertMap != null) {
            Iterator iter = convertMap.keySet().iterator();
            while (iter.hasNext()) {
                String regex = (String) iter.next();
                String replacement = (String) convertMap.get(regex);
                try {
                    p = Pattern.compile(regex, REGEX_FLAGS);
                    m = p.matcher(retValue);
                    retValue = m.replaceAll(replacement);
                } catch (Exception e) {
                    String error = "Error expression: " + regex + " - " + e;
                    log.info(error);
                    m_conversionError = error;
                }
            }
        }
        return retValue;
    }

    /**
	 * do convert map base conversion
	 * @param sqlStatement
	 * @return string
	 */
    protected String convertWithConvertMap(String sqlStatement) {
        try {
            sqlStatement = applyConvertMap(cleanUpStatement(sqlStatement));
        } catch (RuntimeException e) {
            log.warning(e.getLocalizedMessage());
            m_exception = e;
        }
        return sqlStatement;
    }

    /**
	 * Get convert map for use in sql convertion
	 * @return map
	 */
    protected Map getConvertMap() {
        return null;
    }

    /**
	 *  Convert single Statements.
	 *  - remove comments
	 *      - process FUNCTION/TRIGGER/PROCEDURE
	 *      - process Statement
	 *  @param sqlStatement
	 *  @return converted statement
	 */
    protected abstract ArrayList<String> convertStatement(String sqlStatement);

    /**
	 * True if the database support native oracle dialect, false otherwise.
	 * @return boolean
	 */
    public abstract boolean isOracle();

    public static void logMigrationScript(String oraStatement, String pgStatement) {
        boolean logMigrationScript = Ini.isPropertyBool(Ini.P_LOGMIGRATIONSCRIPT);
        if (logMigrationScript) {
            if (dontLog(oraStatement)) return;
            try {
                if (tempFileOr == null) {
                    String fileNameOr = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "migration_script_oracle.sql";
                    tempFileOr = new FileOutputStream(fileNameOr, true);
                    osOr = new DataOutputStream(tempFileOr);
                }
                writeLogMigrationScript(osOr, oraStatement);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (pgStatement == null) {
                    Convert_PostgreSQL convert = new Convert_PostgreSQL();
                    String[] r = convert.convert(oraStatement);
                    pgStatement = r[0];
                }
                if (tempFilePg == null) {
                    String fileNamePg = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "migration_script_postgresql.sql";
                    tempFilePg = new FileOutputStream(fileNamePg, true);
                    osPg = new DataOutputStream(tempFilePg);
                }
                writeLogMigrationScript(osPg, pgStatement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean dontLog(String statement) {
        String[] exceptionTables = new String[] { "AD_ACCESSLOG", "AD_ALERTPROCESSORLOG", "AD_CHANGELOG", "AD_ISSUE", "AD_LDAPPROCESSORLOG", "AD_PACKAGE_IMP", "AD_PACKAGE_IMP_BACKUP", "AD_PACKAGE_IMP_DETAIL", "AD_PACKAGE_IMP_INST", "AD_PACKAGE_IMP_PROC", "AD_PINSTANCE", "AD_PINSTANCE_LOG", "AD_PINSTANCE_PARA", "AD_REPLICATION_LOG", "AD_SCHEDULERLOG", "AD_SESSION", "AD_WORKFLOWPROCESSORLOG", "CM_WEBACCESSLOG", "C_ACCTPROCESSORLOG", "K_INDEXLOG", "R_REQUESTPROCESSORLOG", "T_AGING", "T_ALTER_COLUMN", "T_DISTRIBUTIONRUNDETAIL", "T_INVENTORYVALUE", "T_INVOICEGL", "T_REPLENISH", "T_REPORT", "T_REPORTSTATEMENT", "T_SELECTION", "T_SELECTION2", "T_SPOOL", "T_TRANSACTION", "T_TRIALBALANCE" };
        String uppStmt = statement.toUpperCase();
        if (uppStmt.startsWith("SELECT ")) return true;
        if (uppStmt.startsWith("UPDATE AD_PROCESS SET STATISTIC_")) return true;
        for (int i = 0; i < exceptionTables.length; i++) {
            if (uppStmt.startsWith("INSERT INTO " + exceptionTables[i] + " ")) return true;
            if (uppStmt.startsWith("DELETE FROM " + exceptionTables[i] + " ")) return true;
            if (uppStmt.startsWith("DELETE " + exceptionTables[i] + " ")) return true;
            if (uppStmt.startsWith("UPDATE " + exceptionTables[i] + " ")) return true;
            if (uppStmt.startsWith("INSERT INTO " + exceptionTables[i] + "(")) return true;
        }
        return false;
    }

    private static void writeLogMigrationScript(DataOutputStream os, String statement) throws IOException {
        String prm_COMMENT = MSysConfig.getValue("DICTIONARY_ID_COMMENTS");
        SimpleDateFormat format = DisplayType.getDateFormat(DisplayType.DateTime);
        String dateTimeText = format.format(new Timestamp(System.currentTimeMillis()));
        os.writeBytes("-- ");
        os.writeBytes(dateTimeText);
        os.writeBytes("\n");
        os.writeBytes("-- ");
        os.writeBytes(prm_COMMENT);
        os.writeBytes("\n");
        os.writeBytes(statement);
        os.writeBytes("\n;\n\n");
        os.flush();
    }
}
