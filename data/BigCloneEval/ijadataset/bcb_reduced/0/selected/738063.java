package er.extensions;

import java.sql.*;
import java.util.*;
import org.apache.log4j.Logger;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * @author david@cluster9.com<br/> <br/> Automatically generates Long primary
 *         keys for entities. Features a cache which reduces database roundtrips
 *         as well as optionally encoding Entity type in PK value.<br/> <br/>
 *         usage:<br/> <br/> override either the ERXGeneratesPrimaryKey
 *         interface like this:<br/> <code><pre>
 * private NSDictionary _primaryKeyDictionary = null;
 * 
 *                                                    public NSDictionary primaryKeyDictionary(boolean inTransaction) {
 *                                                        if (_primaryKeyDictionary == null) {
 *                                                            _primaryKeyDictionary = ERXLongPrimaryKeyFactory.primaryKeyDictionary(this);
 *                                                        }
 *                                                        return _primaryKeyDictionary;
 *                                                    }
 * </pre>
 * </code><br/> or manually call<br/>
 *         <code>ERXLongPrimaryKeyFactory.primaryKeyDictionary(EOEnterpriseObject eo);</code><br/>
 *         <br/> the necessary database table is generated on the fly.<br/>
 *         <br/> <b>Encoding Entity in PK values</b><br/> If the system
 *         property
 *         <code>ERXIntegerPrimaryKeyFactory.encodeEntityInPkValue</code> is
 *         set to <code>true</code> then the last 6 bits from the 64 bit
 *         primary key is used to encode the Subentity in the pk value. This
 *         speeds up inheritance with multiple tables. In order to support this
 *         you must add an entry to the userInfo from the Subentities:<br/>
 *         <br/> <code>key=entityCode</code><br/>
 *         <code>value= %lt;%lt; an unique integer, no longer than 6 bit - 1</code><br/>
 * 
 */
public class ERXLongPrimaryKeyFactory {

    public static final int CODE_LENGTH = 6;

    public static final int HOST_CODE_LENGTH = 10;

    public static final String HOST_CODE_KEY = "er.extensions.ERXLongPrimaryKeyFactory.hostCode";

    public static final Logger log = Logger.getLogger(ERXLongPrimaryKeyFactory.class);

    public static long MAX_PK_VALUE = (long) Math.pow(2, 48);

    public static Boolean encodeEntityInPkValue;

    public static Boolean encodeHostInPkValue;

    public static Integer hostCode;

    private static Hashtable pkCache = new Hashtable();

    private static int increaseBy = 0;

    private static Long getNextPkValueForEntity(String ename) {
        Long pk = cachedPkValue(ename);
        if (encodeHostInPkValue()) {
            long l = pk.longValue();
            if (l > MAX_PK_VALUE) {
                throw new IllegalStateException("max PK value reached for entity " + ename + " cannot continue!");
            }
            long realPk = l << HOST_CODE_LENGTH;
            realPk = realPk | hostCode();
            if (log.isDebugEnabled()) {
                log.debug("new pk value for " + ename + "(" + ((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(ename) + "), db value = " + pk + ", new value = " + realPk);
            }
            pk = new Long(realPk);
        }
        if (encodeEntityInPkValue()) {
            long l = pk.longValue();
            if (l > MAX_PK_VALUE) {
                throw new IllegalStateException("max PK value reached for entity " + ename + " cannot continue!");
            }
            long realPk = l << CODE_LENGTH;
            realPk = realPk | ((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(ename);
            if (log.isDebugEnabled()) {
                log.debug("new pk value for " + ename + "(" + ((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(ename) + "), db value = " + pk + ", new value = " + realPk);
            }
            pk = new Long(realPk);
        }
        return pk;
    }

    /**
     * @return
     */
    public static synchronized int hostCode() {
        if (hostCode == null) {
            hostCode = new Integer(ERXSystem.getProperty(HOST_CODE_KEY));
        }
        return hostCode.intValue();
    }

    public static synchronized boolean encodeEntityInPkValue() {
        if (encodeEntityInPkValue == null) {
            boolean b = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLongPrimaryKeyFactory.encodeEntityInPkValue"), false);
            encodeEntityInPkValue = b ? Boolean.TRUE : Boolean.FALSE;
        }
        return encodeEntityInPkValue.booleanValue();
    }

    public static synchronized boolean encodeHostInPkValue() {
        if (encodeHostInPkValue == null) {
            boolean b = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLongPrimaryKeyFactory.encodeHostInPkValue"), false);
            encodeHostInPkValue = b ? Boolean.TRUE : Boolean.FALSE;
        }
        return encodeHostInPkValue.booleanValue();
    }

    public static Object primaryKeyValue(EOEnterpriseObject eo) {
        return primaryKeyDictionary(eo).objectEnumerator().nextElement();
    }

    public static Object primaryKeyValue(String entityName) {
        return primaryKeyDictionary(entityName).objectEnumerator().nextElement();
    }

    public static NSDictionary primaryKeyDictionary(EOEnterpriseObject eo) {
        String entityName = eo.entityName();
        return primaryKeyDictionary(entityName);
    }

    public static synchronized NSDictionary primaryKeyDictionary(String entityName) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        while (entity.parentEntity() != null) {
            entity = entity.parentEntity();
        }
        entityName = entity.name();
        if (entity.primaryKeyAttributeNames().count() != 1) {
            throw new IllegalArgumentException("Can handle only entities with one PK: " + entityName + " has " + entity.primaryKeyAttributeNames());
        }
        Long pk = getNextPkValueForEntity(entityName);
        String pkName = (String) entity.primaryKeyAttributeNames().objectAtIndex(0);
        return new NSDictionary(new Object[] { pk }, new Object[] { pkName });
    }

    /**
     * returns a new primary key for the specified entity.
     * 
     * @param ename,
     *            the entity name for which this method should return a new
     *            primary key
     * @param count,
     *            the number of times the method should try to get a value from
     *            the database if something went wrong (a deadlock in the db for
     *            example -> high traffic with multiple instances)
     * @param increaseBy,
     *            if > 1 then the value in the database is increased by this
     *            factor. This is usefull to 'get' 10000 pk values at once for
     *            caching. Removes a lot of db roundtrips.
     * @return a new pk values for the specified entity.
     */
    private static Long getNextPkValueForEntityIncreaseBy(String ename, int count, int increaseBy) {
        if (increaseBy < 1) increaseBy = 1;
        String where = "where eoentity_name = '" + ename + "'";
        ERXJDBCConnectionBroker broker = ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename);
        Connection con = broker.getConnection();
        try {
            try {
                con.setAutoCommit(false);
                con.setReadOnly(false);
            } catch (SQLException e) {
                log.error(e, e);
            }
            for (int tries = 0; tries < count; tries++) {
                try {
                    ResultSet resultSet = con.createStatement().executeQuery("select pk_value from pk_table " + where);
                    con.commit();
                    boolean hasNext = resultSet.next();
                    long pk = 1;
                    if (hasNext) {
                        pk = resultSet.getLong("pk_value");
                        con.createStatement().executeUpdate("update pk_table set pk_value = " + (pk + increaseBy) + " " + where);
                    } else {
                        pk = maxIdFromTable(ename);
                        con.createStatement().executeUpdate("insert into pk_table (eoentity_name, pk_value) values ('" + ename + "', " + (pk + increaseBy) + ")");
                    }
                    con.commit();
                    return new Long(pk);
                } catch (SQLException ex) {
                    String s = ex.getMessage().toLowerCase();
                    boolean creationError = (s.indexOf("error code 116") != -1);
                    creationError |= (s.indexOf("pk_table") != -1 && s.indexOf("does not exist") != -1);
                    creationError |= s.indexOf("ora-00942") != -1;
                    if (creationError) {
                        try {
                            con.rollback();
                            log.info("creating pk table");
                            con.createStatement().executeUpdate("create table pk_table (eoentity_name varchar(100) not null, pk_value integer)");
                            con.createStatement().executeUpdate("alter table pk_table add primary key (eoentity_name)");
                            con.commit();
                        } catch (SQLException ee) {
                            throw new NSForwardException(ee, "could not create pk table");
                        }
                    } else {
                        throw new NSForwardException(ex, "Error fetching PK");
                    }
                }
            }
        } finally {
            broker.freeConnection(con);
        }
        throw new IllegalStateException("Couldn't get PK");
    }

    /**
     * Retrieves the maxValue from id from the specified entity. If 
     * hosts and entities are encoded, then these values are stripped
     * first
     * 
     * @param ename
     * @param pk
     * @return
     */
    private static long maxIdFromTable(String ename) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(ename);
        if (entity == null) throw new NullPointerException("could not find an entity named " + ename);
        String tableName = entity.externalName();
        String colName = ((EOAttribute) entity.primaryKeyAttributes().lastObject()).columnName();
        String sql = "select max(" + colName + ") from " + tableName;
        ERXJDBCConnectionBroker broker = ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename);
        Connection con = broker.getConnection();
        ResultSet resultSet;
        try {
            resultSet = con.createStatement().executeQuery(sql);
            con.commit();
            boolean hasNext = resultSet.next();
            long v = 1l;
            if (hasNext) {
                v = resultSet.getLong(1);
                if (log.isDebugEnabled()) log.debug("received max id from table " + tableName + ", setting value in PK_TABLE to " + v);
                if (encodeEntityInPkValue()) {
                    v = v >> CODE_LENGTH;
                }
                if (encodeHostInPkValue()) {
                    v = v >> HOST_CODE_LENGTH;
                }
            }
            return v + 1;
        } catch (SQLException e) {
            log.error("could not call database with sql " + sql, e);
            throw new IllegalStateException("could not get value from " + sql);
        } finally {
            broker.freeConnection(con);
        }
    }

    /**
     * Returns a new integer based PkValue for the specified entity. If the
     * cache is empty it is refilled again.
     * 
     * @param ename,
     *            the entity name for which this method should return a new
     *            primary key
     * 
     * @return a new Integer based primary key for the specified entity.
     */
    private static Long cachedPkValue(String ename) {
        Stack s = cacheStack(ename);
        if (s.empty()) {
            synchronized (s) {
                if (s.empty()) {
                    fillPkCache(s, ename);
                }
            }
        }
        Long pkValue = (Long) s.pop();
        return pkValue;
    }

    /**
     * looks in the cache hashtable if there is already an Stack for the
     * specified entity name. If there is no Stack a new Stack object will be
     * created.
     * 
     * @param ename,
     *            the name of the entity for which this method should return the
     *            Stack
     * @return the Stack with primary key values for the specified entity.
     */
    private static Stack cacheStack(String ename) {
        Stack s = (Stack) pkCache.get(ename);
        if (s == null) {
            s = new Stack();
            pkCache.put(ename, s);
        }
        return s;
    }

    /**
     * creates 1000 primary key values for the specified entity and updates the
     * database
     * 
     * @param s,
     *            the stack into which the pk values should be inserted
     * @param ename,
     *            the entity name for which the pk values should be generated
     */
    private static void fillPkCache(Stack s, String ename) {
        Long pkValueStart = getNextPkValueForEntityIncreaseBy(ename, 10, increaseBy());
        long value = pkValueStart.longValue();
        log.debug("filling pkCache for " + ename + ", starting at " + value);
        for (int i = increaseBy(); i-- > 1; ) {
            s.push(new Long(i + value));
        }
    }

    private static int increaseBy() {
        if (increaseBy == 0) {
            increaseBy = 1000;
        }
        return increaseBy;
    }
}
