package com.rpc.core.utils.hibernate.datatype;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import com.rpc.core.utils.StringUtils;

/**
 * Maps a date type that converts '0000-00-00' to NULL.
 * 
 * @author ted stockwell
 */
public class LegacyDateType implements UserType {

    static DateFormat __inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    static DateFormat __formatter = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Convenience method that returns a value as a date Returns null if the
     * given value cannot be converted to a Date.
     */
    public static java.util.Date toDate(Object o) {
        if (o == null) return null;
        if (o instanceof Date) return (Date) o;
        if (o instanceof java.sql.Timestamp) return new Date(((java.sql.Timestamp) o).getTime());
        if (o instanceof java.sql.Date) return new Date(((java.sql.Date) o).getTime());
        if (o instanceof java.lang.Long) return new Date(((java.lang.Long) o).longValue());
        Date result = null;
        try {
            Method toDateMethod = o.getClass().getMethod("toDate", new Class[0]);
            return (Date) toDateMethod.invoke(o, new Object[0]);
        } catch (Throwable t) {
        }
        String value = o.toString();
        try {
            return DATETIME_FORMAT.parse(value);
        } catch (Throwable t) {
        }
        try {
            return DATE_FORMAT.parse(value);
        } catch (Throwable t) {
        }
        try {
            return SIMPLE_DATE_FORMAT.parse(value);
        } catch (Throwable t) {
        }
        try {
            return MYSQL_DATE_FORMAT.parse(value);
        } catch (Throwable t) {
        }
        try {
            return SIX_DATE_FORMAT.parse(value);
        } catch (Throwable t) {
        }
        try {
            return JAVA_DATE_FORMATTER.parse(value);
        } catch (Throwable t) {
        }
        try {
            return DATEFORMAT_DEFAULT_FORMAT.parse(value);
        } catch (Throwable t) {
        }
        long longValue = toSafeLong(o, -1);
        if (0 <= longValue) return new Date(longValue);
        return result;
    }

    public static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance();

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();

    public static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static final DateFormat MYSQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat SIX_DATE_FORMAT = new SimpleDateFormat("MMddyy");

    public static final DateFormat DATEFORMAT_DEFAULT_FORMAT = DateFormat.getInstance();

    public static final DateFormat JAVA_DATE_FORMATTER = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    public static long toSafeLong(Object obj, long defaultValue) {
        Long l = toLong(obj);
        if (l == null) return defaultValue;
        return l.longValue();
    }

    public static Long toLong(Object obj) {
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return new Long(((Number) obj).longValue());
        if (obj instanceof java.util.Date) return new Long(((java.util.Date) obj).getTime());
        if (obj instanceof java.sql.Timestamp) return new Long(((java.sql.Timestamp) obj).getTime());
        try {
            String s = obj.toString();
            s = StringUtils.replaceAll(s, ",", "");
            return new Long(Long.parseLong(s));
        } catch (Throwable t) {
        }
        return null;
    }

    public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
        return arg0;
    }

    public Serializable disassemble(Object arg0) throws HibernateException {
        return (String) arg0;
    }

    public int hashCode(Object arg0) throws HibernateException {
        return arg0.hashCode();
    }

    public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
        return arg0;
    }

    /**
     * Return the SQL type codes for the columns mapped by this type. The codes
     * are defined on <tt>java.sql.Types</tt>.
     * 
     * @see java.sql.Types
     * @return int[] the typecodes
     */
    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

    /**
     * The class returned by <tt>nullSafeGet()</tt>.
     * 
     * @return Class
     */
    public Class returnedClass() {
        return String.class;
    }

    /**
     * Compare two instances of the class mapped by this type for persistence
     * "equality". Equality of the persistent state.
     * 
     * @param x
     * @param y
     * @return boolean
     */
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null) return y == null;
        if (y == null) return false;
        return x.equals(y);
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset.
     * Implementors should handle possibility of null values.
     * 
     * @param rs
     *            a JDBC result set
     * @param names
     *            the column names
     * @param owner
     *            the containing entity
     * @return Object
     * @throws HibernateException
     * @throws SQLException
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        try {
            String mySqlDate = rs.getString(names[0]);
            String date = mySqlDate;
            try {
                if (mySqlDate == null || mySqlDate.length() <= 0 || mySqlDate.equals("0000-00-00")) {
                    return null;
                }
                __formatter.setLenient(false);
                java.util.Date date1 = __inputDateFormat.parse(mySqlDate);
                date = __formatter.format(date1);
            } catch (ParseException de) {
            } catch (NumberFormatException de) {
            }
            return date;
        } catch (SQLException x) {
            if ("S1009".equals(x.getSQLState())) return null;
            throw x;
        }
    }

    /**
     * Write an instance of the mapped class to a prepared statement.
     * Implementors should handle possibility of null values. A multi-column
     * type should be written to parameters starting from <tt>index</tt>.
     * 
     * @param st
     *            a JDBC prepared statement
     * @param value
     *            the object to write
     * @param index
     *            statement parameter index
     * @throws HibernateException
     * @throws SQLException
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        java.util.Date date = toDate(value);
        if (date == null) {
            st.setNull(index, Types.DATE);
            return;
        }
        String formattedDate = __inputDateFormat.format(date);
        st.setString(index, formattedDate);
    }

    /**
     * Return a deep copy of the persistent state, stopping at entities and at
     * collections.
     * 
     * @return Object a copy
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /**
     * Are objects of this type mutable?
     * 
     * @return boolean
     */
    public boolean isMutable() {
        return false;
    }
}
