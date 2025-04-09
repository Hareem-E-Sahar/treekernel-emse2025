package net.woodstock.rockapi.database.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import net.woodstock.rockapi.database.util.SqlParameter;
import net.woodstock.rockapi.database.util.SqlParameterList;
import net.woodstock.rockapi.database.util.SqlType;

final class DBClientUtils {

    private DBClientUtils() {
        super();
    }

    public static PreparedStatement mountArgs(String sql, Connection c, final SqlParameter... args) throws SQLException {
        return DBClientUtils.mountArgs(sql, c, new SqlParameterList((Object[]) args));
    }

    public static PreparedStatement mountArgs(String sql, Connection c, final SqlParameterList args) throws SQLException {
        PreparedStatement ps = null;
        ps = c.prepareStatement(sql);
        if (args != null) {
            DBClientUtils.setParameters(1, ps, args);
        }
        return ps;
    }

    public static CallableStatement mountFunctionArgs(SqlType outType, String name, final Connection c, SqlParameter... args) throws SQLException {
        return DBClientUtils.mountFunctionArgs(outType, name, c, new SqlParameterList((Object[]) args));
    }

    public static CallableStatement mountFunctionArgs(SqlType outType, String name, final Connection c, SqlParameterList args) throws SQLException {
        return DBClientUtils.mountFunctionArgs(outType.type(), name, c, args);
    }

    public static CallableStatement mountFunctionArgs(int outType, String name, final Connection c, SqlParameter... args) throws SQLException {
        return DBClientUtils.mountFunctionArgs(outType, name, c, new SqlParameterList((Object[]) args));
    }

    public static CallableStatement mountFunctionArgs(int outType, String name, final Connection c, SqlParameterList args) throws SQLException {
        CallableStatement cs = null;
        StringBuilder sql = new StringBuilder("{ ? = call " + name + "(");
        if (args != null) {
            for (int cont = 0; cont < args.size(); cont++) {
                sql.append("?");
                if (cont + 1 < args.size()) {
                    sql.append(",");
                }
            }
        }
        sql.append(") }");
        cs = c.prepareCall(sql.toString());
        cs.registerOutParameter(1, outType);
        if (args != null) {
            DBClientUtils.setParameters(2, cs, args);
        }
        return cs;
    }

    public static CallableStatement mountProcedureArgs(String name, Connection c, final SqlParameter... args) throws SQLException {
        return DBClientUtils.mountProcedureArgs(name, c, new SqlParameterList((Object[]) args));
    }

    public static CallableStatement mountProcedureArgs(String name, Connection c, final SqlParameterList args) throws SQLException {
        CallableStatement cs = null;
        StringBuilder sql = new StringBuilder("{ call " + name + "(");
        if (args != null) {
            for (int cont = 0; cont < args.size(); cont++) {
                sql.append("?");
                if (cont + 1 < args.size()) {
                    sql.append(",");
                }
            }
        }
        sql.append(") }");
        cs = c.prepareCall(sql.toString());
        if (args != null) {
            DBClientUtils.setParameters(1, cs, args);
        }
        return cs;
    }

    public static Object getParameter(int index, SqlType outType, CallableStatement cs) throws SQLException {
        Object o = null;
        switch(outType) {
            case ARRAY:
                o = cs.getArray(index);
                break;
            case BIGINT:
                o = cs.getBigDecimal(index);
                break;
            case BLOB:
                o = cs.getBlob(index);
                break;
            case BOOLEAN:
                o = Boolean.valueOf(cs.getBoolean(index));
                break;
            case CHAR:
                o = cs.getString(index);
                break;
            case CLOB:
                o = cs.getClob(index);
                break;
            case DATE:
                o = cs.getDate(index);
                break;
            case DECIMAL:
                o = Integer.valueOf(cs.getInt(index));
                break;
            case DOUBLE:
                o = Double.valueOf(cs.getDouble(index));
                break;
            case FLOAT:
                o = Float.valueOf(cs.getFloat(index));
                break;
            case INTEGER:
                o = Integer.valueOf(cs.getInt(index));
                break;
            case NUMERIC:
                o = cs.getBigDecimal(index);
                break;
            case OBJECT:
                o = cs.getObject(index);
                break;
            case OTHER:
                o = cs.getObject(index);
                break;
            case REAL:
                o = Float.valueOf(cs.getFloat(index));
                break;
            case REF:
                o = cs.getRef(index);
                break;
            case RESULTSET:
                o = cs.getObject(index);
                break;
            case SMALLINT:
                o = Short.valueOf(cs.getShort(index));
                break;
            case STRUCT:
                o = cs.getObject(index);
                break;
            case TIME:
                o = cs.getTime(index);
                break;
            case TIMESTAMP:
                o = cs.getTimestamp(index);
                break;
            case TINYINT:
                o = Short.valueOf(cs.getShort(index));
                break;
            case VARCHAR:
                o = cs.getString(index);
                break;
            default:
                throw new SQLException("database.SqlType.type-unsupported");
        }
        return o;
    }

    public static void setParameter(int index, PreparedStatement cs, SqlParameter param) throws SQLException {
        Object value = param.getValue();
        SqlType type = param.getType();
        if (value == null) {
            cs.setNull(index, type.type());
            return;
        }
        switch(type) {
            case ARRAY:
                cs.setArray(index, (Array) value);
                break;
            case BIGINT:
                if (value instanceof String) {
                    value = Long.valueOf((String) value);
                }
                cs.setLong(index, ((Long) value).longValue());
                break;
            case BLOB:
                try {
                    if (value instanceof File) {
                        value = new FileInputStream((File) value);
                    } else if (value instanceof Reader) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        Reader reader = (Reader) value;
                        int i = -1;
                        while ((i = reader.read()) != -1) {
                            output.write(i);
                        }
                        value = new ByteArrayInputStream(output.toByteArray());
                    }
                    cs.setBinaryStream(index, (InputStream) value, ((InputStream) value).available());
                } catch (IOException e) {
                    throw new SQLException(e.getMessage());
                }
                break;
            case BOOLEAN:
                if (value instanceof String) {
                    value = Boolean.valueOf((String) value);
                }
                cs.setBoolean(index, ((Boolean) value).booleanValue());
                break;
            case CHAR:
                if (value instanceof Character) {
                    value = new String(((Character) value).toString());
                }
                cs.setString(index, (String) value);
                break;
            case CLOB:
                try {
                    if (value instanceof File) {
                        value = new FileInputStream((File) value);
                    } else if (value instanceof Reader) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        Reader reader = (Reader) value;
                        int i = -1;
                        while ((i = reader.read()) != -1) {
                            output.write(i);
                        }
                        value = new ByteArrayInputStream(output.toByteArray());
                    }
                    int size = ((InputStream) value).available();
                    value = new InputStreamReader((InputStream) value);
                    cs.setCharacterStream(index, (Reader) value, size);
                } catch (IOException e) {
                    throw new SQLException(e.getMessage());
                }
                break;
            case DATE:
                if (value instanceof Long) {
                    value = new Date(((Long) value).longValue());
                } else if (value instanceof String) {
                    value = Date.valueOf((String) value);
                } else if ((value instanceof java.util.Date) && (!(value instanceof Date))) {
                    value = new Date(((java.util.Date) value).getTime());
                }
                cs.setDate(index, (Date) value);
                break;
            case DECIMAL:
                if (value instanceof String) {
                    value = Integer.valueOf((String) value);
                }
                cs.setInt(index, ((Integer) value).intValue());
                break;
            case DOUBLE:
                if (value instanceof String) {
                    value = Double.valueOf((String) value);
                }
                cs.setDouble(index, ((Double) value).doubleValue());
                break;
            case FLOAT:
                if (value instanceof String) {
                    value = Float.valueOf((String) value);
                }
                cs.setFloat(index, ((Float) value).floatValue());
                break;
            case INTEGER:
                if (value instanceof String) {
                    value = Integer.valueOf((String) value);
                }
                cs.setInt(index, ((Integer) value).intValue());
                break;
            case NUMERIC:
                if (value instanceof String) {
                    value = Integer.valueOf((String) value);
                    value = new BigDecimal(((Integer) value).intValue());
                } else if (value instanceof Integer) {
                    value = new BigDecimal(((Integer) value).intValue());
                }
                cs.setBigDecimal(index, (BigDecimal) value);
                break;
            case OBJECT:
                cs.setObject(index, value);
                break;
            case OTHER:
                cs.setObject(index, value);
                break;
            case REAL:
                if (value instanceof String) {
                    value = Float.valueOf((String) value);
                }
                cs.setFloat(index, ((Float) value).floatValue());
                break;
            case REF:
                cs.setRef(index, (Ref) value);
                break;
            case STRUCT:
                cs.setObject(index, value);
                break;
            case SMALLINT:
                if (value instanceof String) {
                    value = Short.valueOf((String) value);
                }
                cs.setShort(index, ((Short) value).shortValue());
                break;
            case TIME:
                if (value instanceof Long) {
                    value = new Date(((Long) value).longValue());
                } else if (value instanceof String) {
                    value = Date.valueOf((String) value);
                }
                cs.setTime(index, (Time) value);
                break;
            case TIMESTAMP:
                if (value instanceof Long) {
                    value = new Timestamp(((Long) value).longValue());
                } else if (value instanceof String) {
                    value = Timestamp.valueOf((String) value);
                } else if ((value instanceof java.util.Date) && (!(value instanceof Timestamp))) {
                    value = new Timestamp(((java.util.Date) value).getTime());
                }
                cs.setTimestamp(index, (Timestamp) value);
                break;
            case TINYINT:
                if (value instanceof String) {
                    value = Byte.valueOf((String) value);
                }
                cs.setByte(index, ((Byte) value).byteValue());
                break;
            case VARCHAR:
                if (!(value instanceof String)) {
                    value = value.toString();
                }
                cs.setString(index, (String) value);
                break;
            default:
                throw new SQLException("database.SqlType.type-unsupported");
        }
    }

    public static void setParameters(int index, PreparedStatement ps, SqlParameter... args) throws SQLException {
        DBClientUtils.setParameters(index, ps, new SqlParameterList((Object[]) args));
    }

    public static void setParameters(int index, PreparedStatement ps, SqlParameterList args) throws SQLException {
        int i = index;
        if (args != null) {
            for (SqlParameter arg : args) {
                DBClientUtils.setParameter(i++, ps, arg);
            }
        }
    }
}
