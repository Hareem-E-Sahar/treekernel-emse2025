import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

public class OpenRolapQuery {

    String server = "localhost";

    String rube = null;

    String user_name = "root";

    String password = "";

    boolean detail_direct = false;

    String client_description = "default client";

    String selected_table_name = "default table";

    private Connection conn = null;

    String ip = null;

    String mac = null;

    private FilterPathManager fpm = new FilterPathManager();

    boolean return_sql = true;

    boolean return_resultsets = false;

    boolean return_single_sql = true;

    boolean return_single_resultset = true;

    OpenRolapResultSet orrs = new OpenRolapResultSet();

    OpenRolapQuery() {
    }

    OpenRolapQuery(String db) {
        rube = db;
    }

    OpenRolapQuery(String db, String client_desc) {
        rube = db;
        client_description = client_desc;
    }

    OpenRolapQuery(String s, String db, String u, String p) {
        server = s;
        rube = db;
        user_name = u;
        password = p;
    }

    OpenRolapQuery(String db, Vector filter_path, boolean ret_sql, boolean return_rs) {
        rube = db;
        fpm.setFilterPath(filter_path);
        return_sql = ret_sql;
    }

    public void setDetailDirect(boolean dd) {
        detail_direct = dd;
    }

    public void addFilter(String dim) {
        fpm.addDimension(dim);
    }

    public String getFilter() {
        return fpm.toString();
    }

    public void addFilter(String dim_name, String dim_value) {
        fpm.addFilterPathLevel(dim_name, dim_value);
    }

    public void clearFilter(String dim_name) {
        fpm.clearDimValues(dim_name);
    }

    public void replaceFilter(String dim_name, String dim_value) {
        fpm.replaceFitlerPathLevel(dim_name, dim_value);
    }

    public void setFilterAllValues(String dim_name, boolean all_vals) {
        fpm.setFilterAllValues(dim_name, all_vals);
    }

    public OpenRolapResultSet getResultSet() {
        System.out.println("test10.java, 109: " + "FilterPathLevels is " + fpm.getFilterPathSize() + " long");
        orrs.initialize();
        orrs.start_time = new java.util.Date();
        getAdminConnection();
        getRubeConnection();
        String ip = getLocalIPAddress();
        logAction(ip);
        String request_id = getLastRequestID();
        logQuery(request_id);
        applyPrefilters();
        String query_id = getLastRequestID();
        logFilterPaths(query_id);
        fpm.validate();
        recurseFilterPaths(new OpenRolapResultSet(), -1);
        if (return_single_sql) orrs.setSingleSQL();
        if (return_single_resultset) {
            orrs.setSingleSQL();
            if ((orrs.sql_single != null) && (orrs.sql_single.length() > 0)) orrs.rs_single = issueSelectQuery(orrs.sql_single);
        }
        if (return_resultsets) {
            for (int i = 0; i < orrs.sql_statements.size(); i++) {
                String sql = (String) orrs.sql_statements.get(i);
                orrs.result_sets.add(issueSelectQuery(sql));
            }
        }
        orrs.end_time = new java.util.Date();
        orrs.query_time_milliseconds = orrs.end_time.getTime() - orrs.start_time.getTime();
        logQueryTime(orrs, query_id);
        return orrs;
    }

    private OpenRolapResultSet recurseFilterPaths(OpenRolapResultSet rsm, int cur_lvl) {
        cur_lvl++;
        System.out.println("test10.java, 176: " + "Current level is " + cur_lvl);
        if (cur_lvl == fpm.getFilterPathSize()) {
            System.out.println("test10.java, 181: " + "At bottom of recursion, getting result set, cur_lvl is " + cur_lvl);
            getResultSetSingle();
            System.out.println("test10.java, 183: " + "At bottom of recursion, got result set, returning");
        } else {
            System.out.println("test10.java, 187: " + "cur_lvl is " + cur_lvl + " and FPL size is " + fpm.getFilterPathSize());
            FilterPathLevel f = fpm.getFilterPathLevel(cur_lvl);
            String dm_nam = (String) f.dim_name;
            System.out.println("test10.java, 190: " + "dm_nam is " + dm_nam);
            if (f.all_values == true) {
                System.out.println("test10.java, 194: " + "all values TRUE");
                FilterPathLevel vx = null;
                Vector temp_f = new Vector();
                for (int i = 0; i < cur_lvl; i++) {
                    vx = fpm.getFilterPathLevel(i);
                    temp_f.add(new FilterPathLevel(vx.dim_name, vx.getCurrentDimValue()));
                }
                f.setDimValues(getAllDimValuesForFilterPath(temp_f, dm_nam));
                System.out.println("test10.java, 204: " + "f is " + f);
            }
            FilterPathLevel fpl = null;
            if ((f.noDimValues()) && (cur_lvl == fpm.getFilterPathSize() - 1)) {
                System.out.println("test10.java, 210: " + "dim_values.size is 0 and last FilterPathLevel");
                recurseFilterPaths(rsm, cur_lvl);
            } else {
                System.out.println("test10.java, 215: " + "found 1 or more dim values");
                for (int i = 0; i < f.countDimValues(); i++) {
                    String dm_val = (String) f.getDimValue(i);
                    f.idx = i;
                    System.out.println("test10.java, 222: " + "dm_val of " + dm_val);
                    recurseFilterPaths(rsm, cur_lvl);
                }
            }
        }
        cur_lvl--;
        return rsm;
    }

    private Vector getAllDimValuesForFilterPath(Vector temp_fpl, String dm_name) {
        Vector all_dim_values = new Vector();
        OpenRolapQuery orq = new OpenRolapQuery(rube);
        orq.client_description = client_description;
        temp_fpl.add(new FilterPathLevel(dm_name));
        orq.fpm.setFilterPath(temp_fpl);
        OpenRolapResultSet orrs = orq.getResultSet();
        try {
            orrs.rs_single.beforeFirst();
            while (orrs.rs_single.next()) {
                String valu = orrs.rs_single.getString(dm_name);
                System.out.println("test10.java, 258: " + "got dim valu of " + valu);
                all_dim_values.add(valu);
            }
        } catch (SQLException sex) {
            System.out.println("test10.java, 264: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 265: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 266: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 267: " + "unable to display resultset");
        }
        return all_dim_values;
    }

    private OpenRolapResultSet getResultSetSingle() {
        String sql = new String();
        if (detail_direct) {
            System.out.println("test10.java, 280: " + "detail_direct set true, going to detail table.");
            sql = new String(getDetailQuery());
            orrs.detail_query = new Boolean(true);
            selected_table_name = getDetailTableName();
            orrs.sql_statements.add(sql);
        } else if (fpm.getFilterPathSize() == 0) {
            sql = "select * from ora_cardinalities";
            selected_table_name = "ora_cardinalities";
            orrs.sql_statements.add(sql);
        } else if (fpm.getFilterPathSize() == 1) {
            FilterPathLevel fpl = fpm.getFilterPathLevel(0);
            if (fpl.getCurrentDimValue() == null) {
                sql = "select * from L1_" + fpl.dim_name + " order by " + fpl.dim_name;
            } else {
                sql = "select * from L1_" + fpl.dim_name + " where " + fpl.dim_name + " = '" + fpl.getCurrentDimValue() + "'";
            }
            selected_table_name = "L1_" + fpl.dim_name;
            orrs.sql_statements.add(sql);
        } else if (fpm.getFilterPathSize() == 2) {
            FilterPathLevel fpl1 = fpm.getFilterPathLevel(0);
            FilterPathLevel fpl2 = fpm.getFilterPathLevel(1);
            int row_count = getRowCount("select * from L1_" + fpl1.dim_name + " where " + fpl1.dim_name + "='" + fpl1.getCurrentDimValue() + "'");
            if (row_count > getGlobalRowLimit()) {
                System.out.println("test10.java, 328: " + "row_count above row limit, going to L2 table.");
                if (fpl2.getCurrentDimValue() == null) {
                    sql = "select * from L2_" + fpl1.dim_name + "_" + fpl2.dim_name + " " + "where " + fpl1.dim_name + "='" + fpl1.getCurrentDimValue() + "' " + "order by " + fpl2.dim_name;
                } else {
                    sql = "select * from L2_" + fpl1.dim_name + "_" + fpl2.dim_name + " " + "where " + fpl1.dim_name + "='" + fpl1.getCurrentDimValue() + "' " + "and " + fpl2.dim_name + "='" + fpl2.getCurrentDimValue() + "'";
                }
                selected_table_name = "L2_" + fpl1.dim_name + "_" + fpl2.dim_name;
            } else {
                System.out.println("test10.java, 345: " + "row_count less than row limit, going to detail table.");
                sql = new String(getDetailQuery());
                orrs.detail_query = new Boolean(true);
                selected_table_name = getDetailTableName();
            }
            orrs.sql_statements.add(sql);
        } else if (fpm.getFilterPathSize() >= 3) {
            orrs = deepQuery(orrs, 1);
        } else {
            System.out.println("test10.java, 359: " + "empty else");
        }
        return orrs;
    }

    public void tryClassForName() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            System.out.println("test10.java, 376: " + "Class.forName() call good");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("test10.java, 380: " + "Class not found, " + cnfe.getMessage());
        } catch (Exception ex) {
            System.out.println("test10.java, 384: " + "failed Class.forName() call");
            System.out.println("test10.java, 385: " + "Exception: " + ex.getMessage());
        }
    }

    public void getRubeConnection() {
        tryClassForName();
        try {
            System.out.println("test10.java, 395: " + "Trying:  jdbc:mysql://" + server + "/" + rube + "," + user_name + "," + password);
            conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + rube, user_name, password);
            System.out.println("test10.java, 399: " + "connection good");
        } catch (SQLException sex) {
            System.out.println("test10.java, 403: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 404: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 405: " + "VendorError: " + sex.getErrorCode());
        }
    }

    private void getAdminConnection() {
        String orig_db = rube;
        tryClassForName();
        try {
            System.out.println("test10.java, 416: " + "Trying:  jdbc:mysql://" + server + "/OpenRolapAdmin " + user_name + "," + password);
            conn = DriverManager.getConnection("jdbc:mysql://" + server + "/OpenRolapAdmin", user_name, password);
            System.out.println("test10.java, 420: " + "connection good");
        } catch (SQLException sex) {
            System.out.println("test10.java, 424: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 425: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 426: " + "VendorError: " + sex.getErrorCode());
        }
    }

    public ResultSet issueSelectQuery(String s) {
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(s);
        } catch (SQLException sex) {
            System.out.println("test10.java, 440: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 441: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 442: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 443: " + "sql is " + s);
        }
        return rs;
    }

    public void issueActionQuery(String s) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(s);
        } catch (SQLException sex) {
            System.out.println("test10.java, 458: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 459: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 460: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 461: " + "sql is " + s);
        }
    }

    private int getGlobalRowLimit() {
        int rli = -1;
        ResultSet temp = issueSelectQuery("select * from ora_rube_values where rube_key='row_limit'");
        try {
            temp.first();
            rli = temp.getInt("rube_value");
        } catch (SQLException sex) {
            System.out.println("test10.java, 477: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 478: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 479: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 480: " + "failure getting row limit");
        }
        System.out.println("test10.java, 483: " + "row limit is " + rli);
        return rli;
    }

    /**  Note this is NOT the count of rows from the sql statement,
	     but rather the value of column "row_count" in row 1.
	*/
    private int getRowCount(String s) {
        int row_count = -1;
        ResultSet temp = issueSelectQuery(s);
        try {
            temp.first();
            row_count = temp.getInt("Detail Row Count");
        } catch (SQLException sex) {
            System.out.println("test10.java, 503: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 504: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 505: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 506: " + "failure getting row_count");
        }
        System.out.println("test10.java, 509: " + "row_count is " + row_count);
        return row_count;
    }

    private String getDetailTableName() {
        String str = null;
        ResultSet temp = issueSelectQuery("select * from ora_rube_values where rube_key='detail_table_name'");
        try {
            temp.first();
            str = temp.getString("rube_value");
        } catch (SQLException sex) {
            System.out.println("test10.java, 527: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 528: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 529: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 530: " + "failure getting row_count");
        }
        System.out.println("test10.java, 533: " + "detail table name is " + str);
        selected_table_name = str;
        return str;
    }

    private String getDetailQuery() {
        StringBuffer sql = new StringBuffer("select distinct " + fpm.getDimNamesAsList(", "));
        sql.append(getCardinalityClause());
        sql.append(getSummaryClause());
        sql.append(", count(*) as 'Detail Row Count' ");
        sql.append("from " + getDetailTableName() + " ");
        FilterPathLevel fplast = fpm.getLast();
        if (fpm.getFilterPathSize() > 1) sql.append("where " + fpm.getDimValuesAsEqualsListMinus1());
        if (fplast.getCurrentDimValue() != null) sql.append("and " + fplast.dim_name + "='" + fplast.getCurrentDimValue() + "' ");
        sql.append("group by " + fpm.getDimNamesAsList(", ") + " ");
        sql.append("order by " + fpm.getDimNamesAsList(", "));
        return sql.toString();
    }

    private String getSummaryClause() {
        StringBuffer s = new StringBuffer();
        try {
            ResultSet temp = issueSelectQuery("select * from ora_column_properties " + "where column_type='summary' order by select_order");
            while (temp.next()) {
                s.append(", sum(" + temp.getString("column_name") + ") as " + "'Sum " + temp.getString("column_name") + "'");
            }
        } catch (SQLException sex) {
            System.out.println("test10.java, 594: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 595: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 596: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 597: " + "sum clause is " + s);
        }
        return s.toString();
    }

    private String getCardinalityClause() {
        StringBuffer s = new StringBuffer();
        try {
            ResultSet temp = issueSelectQuery("select * from ora_column_properties " + "where column_type='dimension' order by select_order");
            Vector dims_v = fpm.getDimNamesAsVector();
            while (temp.next()) {
                String d = temp.getString("column_name");
                if (!dims_v.contains(d)) {
                    s.append(", count(distinct " + d + ") as 'Uniq " + d + "'");
                }
            }
        } catch (SQLException sex) {
            System.out.println("test10.java, 624: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 625: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 626: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 627: " + "cardinality clause is " + s);
        }
        return s.toString();
    }

    private OpenRolapResultSet deepQuery(OpenRolapResultSet ors, int lvl) {
        ResultSet r_val = null;
        StringBuffer t = new StringBuffer("from L" + lvl + "_");
        for (int i = 0; i < lvl; i++) {
            t.append((fpm.getFilterPathLevel(i)).dim_name);
            if (i < lvl - 1) t.append("_");
        }
        StringBuffer w = new StringBuffer(" where ");
        for (int i = 0; i < lvl; i++) {
            w.append((fpm.getFilterPathLevel(i)).dim_name + "='" + (fpm.getFilterPathLevel(i)).getCurrentDimValue() + "'");
            if (i < lvl - 1) w.append(" and ");
        }
        System.out.println("test10.java, 661: " + "sql for row limit is: select * " + t + w);
        int row_count = getRowCount("select * " + t + w);
        if (row_count > getGlobalRowLimit()) {
            System.out.println("test10.java, 667: " + "row_count above row limit, going into L" + (lvl + 1) + " table.");
            if (lvl == fpm.getFilterPathSize() - 1) {
                FilterPathLevel fplast = fpm.getLast();
                System.out.println("test10.java, 674: " + "at last level, getting Lx table.");
                String sql = "select * from L" + (lvl + 1) + "_" + fpm.getDimNamesAsList("_") + " " + "where " + fpm.getDimValuesAsEqualsListMinus1();
                if (fplast.noDimValues()) {
                    sql = sql + " order by " + fplast.dim_name;
                } else {
                    sql = sql + " and " + fplast.dim_name + "='" + fplast.getCurrentDimValue() + "'";
                }
                System.out.println("test10.java, 688: " + "sql is " + sql);
                selected_table_name = "L" + (lvl + 1) + "_" + fpm.getDimNamesAsList("_");
                ors.sql_statements.add(sql);
            } else {
                System.out.println("test10.java, 694: " + "recursing, from level " + lvl + " to level " + (lvl + 1));
                ors = deepQuery(ors, lvl + 1);
            }
        } else {
            System.out.println("test10.java, 700: " + "row_count less than row limit, going to detail table.");
            String sql = new String(getDetailQuery());
            selected_table_name = getDetailTableName();
            ors.sql_statements.add(sql);
        }
        return ors;
    }

    String getLastRequestID() {
        String rid = null;
        ResultSet rst = issueSelectQuery("select LAST_INSERT_ID()");
        try {
            rst.first();
            rid = rst.getString(1);
            System.out.println("test10.java, 718: " + "got request ID of " + rid);
        } catch (SQLException sex) {
            System.out.println("test10.java, 722: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 723: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 724: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 725: " + "sql is select LAST_INSERT_ID()");
        }
        return rid;
    }

    String getLocalIPAddress() {
        Enumeration addrs = null;
        try {
            addrs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException se) {
            System.out.println("test10.java, 744: " + "caught socket exception");
        }
        while (addrs.hasMoreElements()) {
            NetworkInterface nw = (NetworkInterface) addrs.nextElement();
            String nws = nw.toString();
            nws = nws.substring(nws.indexOf("/") + 1);
            if (nws.indexOf("/") == -1) {
                mac = nws.substring(0, nws.length() - 2);
            } else {
                mac = nws.substring(0, nws.lastIndexOf("/") - 2);
                ip = nws.substring(nws.lastIndexOf("/") + 1, nws.length() - 2);
            }
            if ((ip == null) || !ip.equals("127.0.0.1")) {
                break;
            }
        }
        System.out.println("test10.java, 774: " + "found mac of " + mac);
        System.out.println("test10.java, 775: " + "found ip of " + ip);
        return ip;
    }

    private void logAction(String ip) {
        String sql = "insert into ora_request_action " + "(user, ip_addr, mac_addr, action, date_time) values ( '" + user_name + "', '" + ip + "', '" + mac + "', 'getResultSet', now())";
        issueActionQuery(sql);
    }

    private void logQuery(String request_id) {
        String sql = "insert into ora_queries " + "(request_id, api, client_info) values ('" + request_id + "'," + "'java_API', '" + client_description + "')";
        issueActionQuery(sql);
    }

    private void logFilterPaths(String query_id) {
        for (int i = 0; i < fpm.getFilterPathSize(); i++) {
            FilterPathLevel ff = fpm.getFilterPathLevel(i);
            boolean multi = false;
            if (ff.countDimValues() > 1) multi = true;
            for (int j = 0; j < ff.countDimValues(); j++) {
                String sql = "insert into ora_requested_filter_paths " + "(query_id,filter_path_level,dim_name,dim_value,multi,pre_filter) values (" + "'" + query_id + "'," + i + ",'" + ff.dim_name + "'," + "'" + ff.getDimValue(j) + "', '" + (multi ? "yes" : "no") + "', '" + (ff.prefilter ? "yes" : "no") + "')";
                issueActionQuery(sql);
            }
        }
    }

    private void logQueryTime(OpenRolapResultSet rrs, String query_id) {
        String sql = "update ora_queries set query_time_seconds = " + (((float) rrs.query_time_milliseconds) / 1000) + ", select_table_name = '" + selected_table_name + "'" + " where query_id = '" + query_id + "'";
        issueActionQuery(sql);
    }

    private void applyPrefilters() {
        ResultSet rr = issueSelectQuery("select * from ora_prefilters where user = '" + user_name + "'");
        try {
            Vector fp = fpm.getFilterPath();
            for (int i = 0; i < fp.size(); i++) {
                FilterPathLevel f = (FilterPathLevel) fp.get(i);
                String dim_name = f.dim_name;
                Vector dim_values = f.dim_values;
                for (int j = 0; j < dim_values.size(); j++) {
                    String dim_value = (String) dim_values.get(j);
                    rr.beforeFirst();
                    boolean dim_name_found = false;
                    boolean dim_valu_found = false;
                    while (rr.next()) {
                        if (dim_name.equals(rr.getString("dimension"))) {
                            dim_name_found = true;
                            if (dim_value.equals(rr.getString("value"))) {
                                dim_valu_found = true;
                            }
                        }
                    }
                    if (dim_name_found) {
                        if (!dim_valu_found) {
                            dim_values.remove(dim_value);
                            j--;
                        }
                    }
                }
            }
            rr.beforeFirst();
            Vector add_these = new Vector();
            while (rr.next()) {
                String dimension = rr.getString("dimension");
                String value = rr.getString("value");
                FilterPathLevel f = fpm.getFilterPathLevelByDimName(dimension);
                if ((f != null) && (f.dim_values.size() == 0)) {
                    if (!add_these.contains(dimension)) add_these.add(dimension);
                }
                if ((f == null) || add_these.contains(dimension)) {
                    if (!add_these.contains(dimension)) add_these.add(dimension);
                    System.out.println("test10.java, 923: " + "Adding prefilter: " + dimension + "=" + value);
                    fpm.addPreFilter(dimension, value);
                }
            }
        } catch (SQLException sex) {
            System.out.println("test10.java, 932: " + "SQLException: " + sex.getMessage());
            System.out.println("test10.java, 933: " + "SQLState: " + sex.getSQLState());
            System.out.println("test10.java, 934: " + "VendorError: " + sex.getErrorCode());
            System.out.println("test10.java, 935: " + "failure getting row_count");
        }
    }
}
