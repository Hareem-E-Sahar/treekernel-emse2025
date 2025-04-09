package org.dbe.composer.wfengine.bpel.server.engine.storage.sql;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.dbe.composer.wfengine.SdlException;
import org.dbe.composer.wfengine.bpel.server.engine.storage.SdlStorageException;
import org.dbe.composer.wfengine.util.SdlCloser;
import org.dbe.composer.wfengine.util.SdlUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class encapsulates the SQL statements used by the Active BPEL persistence
 * layer.  This class uses a SAX parser to first parse a "common" xml file that
 * contains the SQL statements.  It then parses a second xml file that is
 * specific to the database being used.  The values in the specific xml file
 * will override the values in the common file.
 */
public class SdlSQLConfig {

    private static final Logger logger = Logger.getLogger(SdlSQLConfig.class.getName());

    public static final String PARAMETER_HAS_CASCADING_DELETES = "HasCascadingDeletes";

    public static final String PARAMETER_SET_TRANSACTION_ISOLATION_LEVEL = "SetTransactionIsolationLevel";

    /** The message that is logged when parsing fails. */
    protected static final String ERROR_PARSING_DOCUMENT = "Error caught while parsing SQL config.";

    /** The specific type of database being used.  For example, "mysql". */
    protected String mType;

    /** The map of SQL statement names/keys to SQL statements. */
    private Map mSQLMap = new HashMap();

    /** map of constant names to values */
    private Properties mConstantsFromFile = new Properties();

    /** map of name value pairs used to override any constant values */
    private Properties mConstantOverrides = new Properties(mConstantsFromFile);

    /**
     * Creates a SQL config object that will use the given type as the specific
     * database configuration to use.
     *
     * @param aType A type of database configuration to load, such as "mysql".
     */
    public SdlSQLConfig(String aType) {
        this(aType, Collections.EMPTY_MAP);
    }

    /**
     * Creates a SQL config object with the given db type and a map of
     * constant overrides.
     *
     * @param aType
     * @param aConstantOverrides
     */
    public SdlSQLConfig(String aType, Map aConstantOverrides) {
        mType = aType;
        getConstantOverrides().putAll(aConstantOverrides);
        loadSQL();
    }

    /**
     * Reloads the config with the new overrides which will replace any previously
     * defined overrides.
     */
    public void reload(Map aOverrides) {
        if (!SdlUtil.compareObjects(aOverrides, getConstantOverrides())) {
            getConstantOverrides().clear();
            getConstantOverrides().putAll(aOverrides);
            reload();
        }
    }

    /**
     * Reloads the sql statements from the config's file. This should be overridden
     * by subclasses so they can reload from their own files.
     */
    protected void reload() {
        loadSQL();
    }

    /**
     * Returns the database type.
     */
    public String getDatabaseType() {
        return mType;
    }

    /**
     * Returns parameter value from SQL configuration.
     *
     * @param aKey
     */
    public String getParameter(String aKey) throws SdlStorageException {
        String value = (String) getSQLMap().get(aKey);
        if (value == null) {
            logger.error("No such SQL configuration parameter " + aKey);
            throw new SdlStorageException(MessageFormat.format("No such SQL configuration parameter '{0}'", new Object[] { aKey }));
        }
        return value;
    }

    /**
     * Returns <code>boolean</code> parameter value from SQL configuration.
     *
     * @param aKey A key that identifies the parameter value in the config file.
     */
    public boolean getParameterBoolean(String aKey) throws SdlStorageException {
        return Boolean.valueOf(getParameter(aKey)).booleanValue();
    }

    /**
     * Returns <code>int</code> parameter value from SQL configuration.
     *
     * @param aKey A key that identifies the parameter value in the config file.
     */
    public int getParameterInt(String aKey) throws SdlStorageException {
        try {
            return Integer.parseInt(getParameter(aKey));
        } catch (NumberFormatException e) {
            logger.error("Invalid integer value for parameter " + aKey);
            throw new SdlStorageException(MessageFormat.format("Invalid integer value for parameter '{0}'", new Object[] { aKey }), e);
        }
    }

    /**
     * Gets a SQL statement given a key (the name of the statement as configured
     * in the file).
     *
     * @param aKey A key that references a SQL statement in the config file.
     * @return A SQL statement.
     */
    public String getSQLStatement(String aKey) {
        return (String) getSQLMap().get(aKey);
    }

    /**
     * Gets a SQL statement fragment that limits the query to the specified number
     * of rows or an empty string if the db doesn't support the operation.
     * @param aLimitValue
     */
    public String getLimitStatement(int aLimitValue) {
        String stmt = getSQLStatement("Generic.Limit");
        if (!SdlUtil.isNullOrEmpty(stmt)) {
            return " " + stmt.replaceFirst("\\?", "" + aLimitValue);
        }
        return "";
    }

    /**
     * Loads the SQL statements into the hashmap.  This method loads two different
     * XML configuration files.  First it loads the common SQL configuration file,
     * followed by the database specific SQL configuration file.
     */
    protected void loadSQL() {
        String commonFileName = "common-sql.xml";
        String specificFileName = mType + "-sql.xml";
        addSQLStatements(loadSQL(commonFileName));
        addSQLStatements(loadSQL(specificFileName));
    }

    /**
     * Adds the map to the map of the statements and replaces any constants in the map added.
     * @param aMap
     */
    protected void addSQLStatements(Map aMap) {
        replaceConstants(aMap);
        getSQLMap().putAll(aMap);
    }

    /**
     * Convenience method that returns true if there are constants available for
     * substitution
     */
    private boolean hasConstants() {
        return !getConstantOverrides().isEmpty() || !getConstantsFromFile().isEmpty();
    }

    /**
     * Walks the map of statements replacing the values found within with the
     * declared constants.
     */
    private void replaceConstants(Map aMap) {
        if (hasConstants()) {
            Pattern pattern = Pattern.compile("%(\\w+)%");
            for (Iterator iter = aMap.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iter.next();
                String stmt = (String) entry.getValue();
                Matcher matcher = pattern.matcher(stmt);
                StringBuffer sb = new StringBuffer(stmt.length() * 2);
                int offset = 0;
                while (matcher.find()) {
                    sb.append(stmt.substring(offset, matcher.start()));
                    String constant = matcher.group(1);
                    String replacementValue = (String) resolveToken(constant);
                    if (replacementValue != null) {
                        sb.append(replacementValue);
                    } else {
                        sb.append(matcher.group());
                        Exception e = new Exception(MessageFormat.format("Missing constant'{0}' found in SQL configuration file.", new Object[] { constant }));
                        logger.error("Error parsing SQL configuration file: Missing constant " + constant + " found in SQL configuration file.");
                        SdlException.logError(e, "Error parsing SQL configuration file.");
                    }
                    offset = matcher.end();
                }
                if (offset < stmt.length()) sb.append(stmt.substring(offset));
                entry.setValue(sb.toString());
            }
        }
    }

    /**
     * This method does the work of parsing the configuration file and loading
     * all of the SQL statements into the hash map.
     *
     * @param aSQLResourceName The name of the SQL configuration file to load (as a resource).
     * @return A map of SQL statement names to SQL statements.
     */
    protected Map loadSQL(String aSQLResourceName) {
        InputStream iStream = null;
        try {
            iStream = getClassForSQLLoad().getResourceAsStream(aSQLResourceName);
            if (iStream == null) {
                logger.error("Error getting SQL config resource: " + aSQLResourceName);
                throw new SdlException("Error getting SQL config resource: " + aSQLResourceName);
            }
            return loadSQL(iStream);
        } catch (Exception e) {
            SdlException.logError(e, ERROR_PARSING_DOCUMENT);
            return Collections.EMPTY_MAP;
        } finally {
            SdlCloser.close(iStream);
        }
    }

    /**
     * A method to get the class to use when loading the SQL file resource.  This is a
     * protected method so it can be overridden by subclasses.
     */
    protected Class getClassForSQLLoad() {
        return SdlSQLConfig.class;
    }

    /**
     * Given an input stream, this method uses a SAX parser to parse through the
     * XML document to pull out the SQL statements.
     *
     * @param aStream An open input stream that points to the XML document.
     * @return A map of SQL statement names to SQL statements.
     * @throws Exception
     */
    protected Map loadSQL(InputStream aStream) throws Exception {
        SQLConfigHandler handler = new SQLConfigHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        parser.parse(new InputSource(aStream), handler);
        return handler.getMap();
    }

    /**
     * Resolves a token with its constant value. The override map is consulted first
     * for a value and then the constants map.
     *
     * @param aToken
     * @return String or null indicating that the no value was found
     */
    private String resolveToken(String aToken) {
        return getConstantOverrides().getProperty(aToken);
    }

    /**
     * @return Returns the constantOverrides.
     */
    public Properties getConstantOverrides() {
        return mConstantOverrides;
    }

    /**
     * @return Returns the constants.
     */
    protected Properties getConstantsFromFile() {
        return mConstantsFromFile;
    }

    /**
     * @return Returns the sQLMap.
     */
    protected Map getSQLMap() {
        return mSQLMap;
    }

    /**
     * This class implements a SAX parser handler.  It is used to parse the
     * SQL configuration file and then call back into the SQL config object
     * when it comes across a SQL statement.
     */
    protected class SQLConfigHandler extends DefaultHandler {

        /** The map from SQL statement name to SQL statement. */
        private Map mMap;

        /** State variable that is true when the SAX parser is within a sql-statement or parameter element. */
        private boolean mInSqlStatement = false;

        /** State variable that contains the current element being parsed. */
        private String mCurrElem;

        /** State variable that contains the last value of a sql-statement/name element. */
        private String mName;

        /** State variable that contains the last value of a sql-statement/sql or parameter/value element. */
        private String mSQL;

        /** State variable that is true when the SAX parser enters a constant element */
        private boolean mInConstant = false;

        /**
         * Constructs a SQLConfig handler given a parent SQL config object.
         */
        public SQLConfigHandler() {
            mMap = new HashMap();
        }

        /**
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            mCurrElem = localName;
            if ("sql-statement".equals(localName) || "parameter".equals(localName)) {
                mInSqlStatement = true;
            } else if ("name".equals(localName)) {
                mName = "";
            } else if ("sql".equals(localName) || "value".equals(localName)) {
                mSQL = "";
            } else if ("constant".equals(localName)) {
                mInConstant = true;
                mName = atts.getValue("name");
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            if ("sql-statement".equals(localName) || "parameter".equals(localName)) {
                mInSqlStatement = false;
                if (mMap.containsKey(mName)) {
                    Exception e = new Exception(MessageFormat.format("Duplicate key '{0}' found in SQL configuration file.", new Object[] { mName }));
                    logger.error("Error parsing SQL configuration file. Duplicate key " + mName + " found in SQL configuration file.");
                    SdlException.logError(e, "Error parsing SQL configuration file.");
                }
                mMap.put(mName, mSQL.trim());
                mName = "";
                mSQL = "";
            } else if (mInConstant) {
                mInConstant = false;
                getConstantsFromFile().setProperty(mName, mSQL);
                mName = "";
                mSQL = "";
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char ch[], int start, int length) throws SAXException {
            if (mInSqlStatement || mInConstant) {
                char[] buf = new char[length];
                System.arraycopy(ch, start, buf, 0, length);
                String bufStr = new String(buf);
                if (bufStr.trim().length() > 0) {
                    if ("name".equals(mCurrElem)) {
                        mName = SdlUtil.getSafeString(mName) + bufStr.trim();
                    } else if ("sql".equals(mCurrElem) || "value".equals(mCurrElem) || "constant".equals(mCurrElem)) {
                        mSQL = SdlUtil.getSafeString(mSQL) + bufStr;
                    }
                }
            }
        }

        /**
         * Gets the handler's internal map of sql names to sql statements.
         */
        public Map getMap() {
            return mMap;
        }
    }
}
