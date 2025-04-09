package org.sss.applications;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.el.ValueExpression;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.el.ExpressionFactoryImpl;
import org.sss.dbutils.ConnectionUtils;
import org.sss.dbutils.ThreadManager;
import org.sss.dbutils.Work;
import org.sss.dbutils.WorkArgument;
import org.sss.el.ELContext;
import org.sss.el.FunctionMapper;
import org.sss.utils.ConfigurationUtils;
import org.sss.utils.DebugUtils;

/**
 * 数据库导出工具
 * @author Jason.Hoo (latest modification by $Author: hujianxin78728 $)
 * @version $Revision: 670 $ $Date: 2010-11-30 21:47:10 -0500 (Tue, 30 Nov 2010) $
 */
public final class DbTransfer {

    static final Log log = LogFactory.getLog(DbTransfer.class);

    static final ExpressionFactoryImpl factory = new ExpressionFactoryImpl();

    static final ELContext context = new ELContext(null);

    static final ThreadManager manager = new ThreadManager();

    private static final int getNextINR(Connection conn, String sql, int beginINR, int offset) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql + "INR>='" + FunctionMapper.str8(beginINR) + "' AND INR<?");
        int endINR = beginINR, minINR = beginINR, maxINR = 0, size = 0;
        ResultSet rs = null;
        while (Math.abs(size - offset) > 100) {
            if (size < offset) {
                minINR = endINR;
                if (maxINR == 0) endINR += +offset; else endINR = (minINR + maxINR) / 2;
            } else {
                maxINR = endINR;
                endINR = (minINR + maxINR) / 2;
            }
            stmt.setString(1, FunctionMapper.str8(endINR));
            rs = stmt.executeQuery();
            rs.next();
            size = rs.getInt(1);
            rs.close();
        }
        stmt.close();
        if (log.isDebugEnabled()) log.debug("size:" + size + "\tresult:" + endINR);
        return endINR;
    }

    private static final String getClause(String sql, String space, String name, String orderBy, String value1, String value2) {
        sql = sql + space + name + ">='" + value1 + "'";
        if (value2 != null) sql += " AND " + name + "<'" + value2 + "'";
        if (orderBy != null) sql += " ORDER BY " + orderBy;
        return sql;
    }

    private static final String getTaskName(String[] values) {
        String taskName = values[0];
        if (values.length > 1) taskName += "_" + values[1];
        if (values.length > 2) taskName += "_" + values[2];
        return taskName;
    }

    private static final void split(String[] values, ArrayList<String> depend) {
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<Integer> types = new ArrayList<Integer>();
        ArrayList<Object> arguments = new ArrayList<Object>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ValueExpression ve = null;
        String fileName = "";
        int index = 0;
        String line = "";
        try {
            String taskName = getTaskName(values);
            if (taskName.startsWith("!")) {
                if (log.isDebugEnabled()) log.debug("Internal task " + taskName);
                if (taskName.equals("!PTS")) Work.newUtils(taskName, "com.brilliance.utils.PTSUtils"); else if (taskName.equals("!OIT")) Work.newUtils(taskName, "com.brilliance.utils.OITUtils"); else if (taskName.equals("!CBB")) Work.newUtils(taskName, "com.brilliance.utils.CBBUtils"); else if (taskName.equals("!CBE")) Work.newUtils(taskName, "com.brilliance.utils.CBEUtils");
                manager.addTask(new WorkArgument(taskName, depend));
                manager.put(taskName, 1);
                return;
            }
            String targetName = values[0];
            String tableName = targetName;
            if (values.length > 1) tableName = values[1];
            fileName = "map/" + taskName + ".map";
            InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
            if (is == null) {
                log.warn("Map file " + fileName + " is missing.");
                fileName = "map/" + taskName.toLowerCase() + ".map";
                is = ClassLoader.getSystemResourceAsStream(fileName);
                if (is == null) {
                    log.error("Map file " + fileName + " is missing.");
                    return;
                }
            }
            if (log.isDebugEnabled()) log.debug("Use map file " + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String[] vs = br.readLine().split("\t");
            if (vs.length < 2) {
                log.error("First line of map file " + fileName + " have only " + vs.length + " blocks.");
                return;
            }
            int size = Integer.parseInt(vs[0]);
            int commitCount = Integer.parseInt(vs[1]);
            String splitColumnName = vs[2];
            String columnNames = "*";
            String filterClause = null;
            String tables = null;
            String orderBy = null;
            int times = 1;
            String timesVaryName = null;
            if (vs.length > 3 && !"".equals(vs[3].trim())) columnNames = vs[3];
            if (vs.length > 4 && !"".equals(vs[4].trim())) filterClause = vs[4];
            if (vs.length > 5 && !"".equals(vs[5].trim())) tables = "," + vs[5];
            if (vs.length > 6 && !"".equals(vs[6].trim())) orderBy = vs[6];
            if (vs.length > 7 && !"".equals(vs[7].trim())) {
                times = Integer.parseInt(vs[7]);
                if (vs.length > 8 && !"".equals(vs[8].trim())) timesVaryName = "#" + vs[8];
                if (size > 1) log.warn("Use times argument in map file " + fileName + " , cannot support split!");
            }
            ArrayList<String> tSqls = new ArrayList<String>();
            String tColumns = "", tParams = "", space = "";
            String[] clauses = new String[] {};
            line = br.readLine();
            if (!line.trim().equals("")) clauses = line.split(":");
            boolean skipGenTSql = false;
            index = 0;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                index++;
                if ((line = trimValue(line)) == null) continue;
                vs = line.split("\t");
                if (vs.length == 1) {
                    columns.add(vs[0]);
                    types.add(Work.DIRECT);
                    arguments.add(vs[0]);
                    tColumns += space + vs[0];
                    tParams += space + "?";
                    space = ",";
                } else {
                    if (vs[1].trim().equals("")) {
                        log.warn("Unsupported type : " + line);
                        log.warn(DebugUtils.debug(line.getBytes()));
                        continue;
                    }
                    switch(vs[1].charAt(0)) {
                        case '%':
                            if (!first) {
                                columns.add(vs[0]);
                                types.add(Work.NEXT);
                                arguments.add(vs[0]);
                            }
                            if (!skipGenTSql && !tColumns.equals("") && !tParams.equals("")) tSqls.add("INSERT INTO " + targetName + " (" + tColumns + ") VALUES (" + tParams + ")");
                            if (vs.length > 2) {
                                tSqls.add(vs[2]);
                                skipGenTSql = true;
                            } else {
                                targetName = vs[0];
                                skipGenTSql = false;
                            }
                            tColumns = "";
                            tParams = "";
                            space = "";
                            break;
                        case '-':
                            columns.add(vs[0]);
                            types.add(Work.DIRECT);
                            arguments.add(vs[2]);
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case 'd':
                        case 'D':
                            columns.add(vs[0]);
                            types.add(Work.DATE);
                            if (vs.length > 2 && vs[2] != null && !"".equals(vs[2])) arguments.add(vs[2]); else arguments.add(vs[0]);
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case '@':
                            columns.add(vs[0]);
                            types.add(Work.CODETABLE);
                            arguments.add(vs[2].split(":"));
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case '$':
                            columns.add(vs[0]);
                            types.add(Work.SQL);
                            arguments.add(vs[2].split(":"));
                            break;
                        case 's':
                        case 'S':
                            columns.add(vs[0]);
                            types.add(Work.SQL_TARGET);
                            arguments.add(vs[2].split(":"));
                            break;
                        case 'p':
                        case 'P':
                            columns.add(vs[0]);
                            types.add(Work.PRINT);
                            if (vs.length > 2) arguments.add(factory.createValueExpression(context, vs[2], Object.class)); else arguments.add(null);
                            break;
                        case '+':
                            columns.add(vs[0]);
                            types.add(Work.DATETIME);
                            arguments.add(vs[2].split(":"));
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case '=':
                            columns.add(vs[0]);
                            types.add(Work.EQUALS);
                            arguments.add(vs[2].split(":"));
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case '&':
                            columns.add(vs[0]);
                            types.add(Work.CATSTRING);
                            arguments.add(vs[2].split(":"));
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case '?':
                            columns.add(vs[0]);
                            types.add(Work.EXPRESSION);
                            arguments.add(factory.createValueExpression(context, vs[2], Object.class));
                            tColumns += space + vs[0];
                            tParams += space + "?";
                            space = ",";
                            break;
                        case '*':
                            columns.add(vs[0]);
                            types.add(Work.NEWRECORD);
                            arguments.add(vs[2].split(","));
                            break;
                        case '#':
                            columns.add("#" + vs[0]);
                            if (vs[0].endsWith("LIST")) types.add(Work.LISTVARIABLE); else if (vs[0].endsWith("MAP")) {
                                if (vs.length < 4) {
                                    log.warn("Map variable #" + vs[0] + " must have two expressions.");
                                    break;
                                }
                                types.add(Work.MAPVARIABLE);
                            } else types.add(Work.VARIABLE);
                            if (vs.length > 3) {
                                arguments.add(new ValueExpression[] { factory.createValueExpression(context, vs[2], Object.class), factory.createValueExpression(context, vs[3], Object.class) });
                            } else arguments.add(factory.createValueExpression(context, vs[2], Object.class));
                            break;
                        case '!':
                            columns.add("#" + vs[0]);
                            types.add(Work.SKIPFLAG);
                            ve = factory.createValueExpression(context, vs.length > 3 ? vs[3] : "#{false}", Object.class);
                            arguments.add(new ValueExpression[] { factory.createValueExpression(context, vs[2], Object.class), ve });
                            break;
                        case 'x':
                        case 'X':
                            columns.add("#" + vs[0]);
                            types.add(Work.IGNORE);
                            arguments.add(factory.createValueExpression(context, vs[2], Object.class));
                            break;
                        case 'c':
                        case 'C':
                            columns.add("#" + vs[0]);
                            types.add(Work.COMMIT);
                            arguments.add(factory.createValueExpression(context, vs[2], Object.class));
                            break;
                        default:
                            log.warn("Unsupported type : " + line);
                            log.warn(DebugUtils.debug(line.getBytes()));
                    }
                }
                if (first) first = false;
            }
            br.close();
            if (!skipGenTSql && !tColumns.equals("") && !tParams.equals("")) tSqls.add("INSERT INTO " + targetName + " (" + tColumns + ") VALUES (" + tParams + ")");
            String sSql = "SELECT " + columnNames + " FROM " + tableName;
            if (tables != null) {
                sSql += tables;
                splitColumnName = tableName + "." + splitColumnName;
            }
            space = " WHERE ";
            if (filterClause != null) {
                sSql += space + filterClause;
                space = " AND ";
            }
            if (log.isDebugEnabled()) {
                log.debug(sSql);
                for (int i = 0; i < tSqls.size(); i++) log.debug(tSqls.get(i));
            }
            if (clauses.length > 0) {
                manager.put(taskName, clauses.length);
                for (int i = 0; i < clauses.length - 1; i++) {
                    manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, getClause(sSql, space, splitColumnName, orderBy, clauses[i], clauses[i + 1]), commitCount));
                }
                manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, getClause(sSql, space, splitColumnName, orderBy, clauses[clauses.length - 1], null), commitCount));
            } else if (size < 0) {
                size = Math.abs(size);
                manager.put(taskName, size);
                conn = ConnectionUtils.getConnection(Work.sourceName);
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                String sql = "SELECT COUNT(*) FROM " + tableName;
                if (tables != null) sql += tables;
                space = " WHERE ";
                if (filterClause != null) {
                    sql += space + filterClause;
                    space = " AND ";
                }
                if (log.isDebugEnabled()) log.debug("Auto caculate split :" + sql);
                rs = stmt.executeQuery(sql);
                rs.next();
                int count = rs.getInt(1);
                stmt.close();
                rs.close();
                int offset = count / size;
                int beginINR = 0, endINR = 0;
                for (int i = 0; i < size - 1; i++) {
                    endINR = getNextINR(conn, sql + space, beginINR, offset);
                    manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, getClause(sSql, space, splitColumnName, orderBy, FunctionMapper.str8(beginINR), FunctionMapper.str8(endINR)), commitCount));
                    beginINR = endINR;
                }
                manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, getClause(sSql, space, splitColumnName, orderBy, FunctionMapper.str8(endINR), null), commitCount));
                conn.close();
            } else if (size > 1) {
                manager.put(taskName, size);
                conn = ConnectionUtils.getConnection(Work.sourceName);
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                String sql = "SELECT MAX(" + splitColumnName + ") FROM " + tableName;
                rs = stmt.executeQuery(sql);
                rs.next();
                int count = rs.getInt(1);
                DbUtils.closeQuietly(conn, stmt, rs);
                long offset = count / size;
                for (int i = 0; i < size - 1; i++) {
                    manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, getClause(sSql, space, splitColumnName, orderBy, FunctionMapper.str8(offset * i), FunctionMapper.str8(offset * i + offset)), commitCount));
                }
                manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, getClause(sSql, space, splitColumnName, orderBy, FunctionMapper.str8(offset * size - offset), null), commitCount));
            } else {
                manager.put(taskName, 1);
                if (orderBy != null) sSql += " ORDER BY " + orderBy;
                manager.addTask(new WorkArgument(taskName, times, timesVaryName, depend, columns, types, arguments, tSqls, sSql, commitCount));
            }
        } catch (Exception e) {
            log.error(String.format("File name:%s\tline:%d\targument:[%s]", fileName, index + 2, line), e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
    }

    public static final void executeSql(Statement stmt, String name, String charset, int deep, boolean debug) throws IOException, SQLException {
        BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("sql/" + name), "UTF-8"));
        String sql;
        while ((sql = br.readLine()) != null) {
            if ((sql = trimValue(sql)) == null) continue;
            if (sql.endsWith(";")) sql = sql.substring(0, sql.indexOf(';'));
            try {
                if (sql.endsWith(".sql")) {
                    log.info("begin call external script \"" + sql + "\".");
                    deep = deep + 1;
                    if (deep > 10) {
                        log.error("end call external script \"" + sql + "\" with deep size is 10.");
                        return;
                    }
                    executeSql(stmt, sql, charset, deep++, false);
                    log.info("end call external script \"" + sql + "\".");
                } else {
                    sql = new String(sql.getBytes(charset), charset);
                    if (deep == 1 || debug) log.info("start execute sql \"" + sql + "\".");
                    stmt.execute(sql);
                    if (deep == 1 || debug) log.info("end execute sql \"" + sql + "\".");
                }
            } catch (Exception e) {
                log.error("execute sql \"" + sql + "\" with error.");
                log.error("error message is :\t" + e.getMessage());
            }
        }
        br.close();
    }

    public static final String trimValue(String value) {
        if (value.startsWith("#")) return null;
        value = value.trim();
        if (value.equals("")) return null;
        return value;
    }

    public static final void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            String version = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("version.txt"));
            System.out.println(version);
            log.info(version);
            String listName = "transfer.lst";
            if (args.length > 0) listName = args[0];
            log.info(listName);
            BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(listName), "UTF-8"));
            String[] values = br.readLine().split("\t");
            Work.sourceName = values[0];
            Work.targetName = values[1];
            Work.MAX_THREADS = Integer.parseInt(values[2]);
            Work.COMMIT_COUNT = Integer.parseInt(values[3]);
            String line;
            while ((line = br.readLine()) != null) {
                if ((line = trimValue(line)) == null) continue;
                String[] vs = line.split("\t");
                ArrayList<String> depend = new ArrayList<String>();
                if (vs.length > 1) {
                    String[] ds = vs[1].split(",");
                    for (int i = 0; i < ds.length; i++) depend.add(getTaskName(ds[i].split(":")));
                }
                split(vs[0].split(":"), depend);
            }
            br.close();
            boolean debugSQL = ConfigurationUtils.getBoolean("dbtranser.debugSQL", false);
            if (values.length > 4) {
                conn = ConnectionUtils.getConnection(Work.targetName);
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                executeSql(stmt, values[4], ConnectionUtils.getCharset(Work.targetName), 1, debugSQL);
                DbUtils.closeQuietly(conn, stmt, null);
            }
            manager.runTask();
            if (values.length > 5) {
                conn = ConnectionUtils.getConnection(Work.targetName);
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                executeSql(stmt, values[5], ConnectionUtils.getCharset(Work.targetName), 1, debugSQL);
            }
        } catch (Exception e) {
            log.error("DbTransfer error.", e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, null);
        }
    }
}
