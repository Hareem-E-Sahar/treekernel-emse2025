package com.cs.util.db.query;

import com.cs.util.binary.Base16;
import com.cs.util.db.Database;
import com.cs.util.db.DatabaseMetaInfo;
import com.cs.util.db.ResultSetMetaInfo;
import com.cs.util.db.SqlExceptionState;
import com.cs.util.db.sql.Column;
import com.cs.util.db.sql.Filter;
import com.cs.util.db.sql.Join;
import com.cs.util.db.sql.Order;
import com.cs.util.http.QueryString;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.MessagingException;

/**
 * RestfulQuery represents a user request via HTTP following the restful principles.
 * @author dimitris@jmike.gr
 */
public class RestfulQuery {

    private List<String> tables = new ArrayList<String>();

    private List<Join> joinOperations = new ArrayList<Join>();

    private Set<String> boundedColumns = new HashSet<String>();

    private List<Filter> filterBoundaries = new ArrayList<Filter>();

    private List<Column> columns = new ArrayList<Column>();

    private List<Filter> filters = new ArrayList<Filter>();

    private List<Order> orders = new ArrayList<Order>();

    private int pageIndex = 1;

    private int pageSize = 50;

    /**
     * Parses the designated collection and querystring and constructs a new RestfulQuery.
     * @param collection a String representing a combination of tables and join operations.
     * Symbol '+' in collection represents INNER JOIN operation.
     * Symbol '!+' in collection represents LEFT JOIN operation.
     * Symbol '+!' in collection represents RIGHT JOIN operation.
     * Symbol '!+!' in collection represents OUTER JOIN operation.
     * @throws java.sql.SQLException
     * @throws QueryException
     */
    public RestfulQuery(String collection) throws SQLException, QueryException {
        this.setTablesJoinsAndBoundaries(collection);
    }

    /**
     * Parses the designated collection and querystring and constructs a new RestfulQuery.
     * @param collection a String representing a combination of tables and join operations.
     * Symbol '+' in collection represents INNER JOIN operation.
     * Symbol '!+' in collection represents LEFT JOIN operation.
     * Symbol '+!' in collection represents RIGHT JOIN operation.
     * Symbol '!+!' in collection represents OUTER JOIN operation.
     * @param qs the supplied QueryString or the request URI.
     * @throws java.sql.SQLException
     * @throws QueryException
     */
    public RestfulQuery(String collection, QueryString qs) throws SQLException, QueryException {
        this.setTablesJoinsAndBoundaries(collection);
        this.setColumns(qs);
        this.setFilters(qs);
        this.setOrders(qs);
        this.setPageIndex(qs);
        this.setPageSize(qs);
    }

    /**
     * Parses the '_show' querystring parameters and sets the columns of the query.
     * @param qs the querystring.
     * @throws SQLException
     */
    private void setColumns(QueryString qs) throws SQLException {
        if (tables.size() > 0) {
            List<String> showList = qs.get("_show");
            if (showList != null) {
                for (int i = 0; i < showList.size(); i++) {
                    final String show = showList.get(i);
                    final String[] segments = show.split(",\\s*");
                    for (int j = 0; j < segments.length; j++) {
                        final String[] s = segments[j].split("\\.\\s*");
                        if (s.length > 1) {
                            if (tables.contains(s[0]) && isColumnAllowed(s[0], s[1])) {
                                columns.add(new Column(s[0], s[1]));
                            }
                        } else {
                            if (tables.size() == 1 && isColumnAllowed(tables.get(0), s[0])) {
                                columns.add(new Column(tables.get(0), s[0]));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the '_order' querystring parameters and sets the order statements of the query.
     * @param qs the querystring.
     * @throws SQLException
     */
    private void setOrders(QueryString qs) {
        if (tables.size() > 0) {
            List<String> orderList = qs.get("_order");
            if (orderList != null) {
                final Pattern pattern = Pattern.compile(":\\w+");
                for (int i = 0; i < orderList.size(); i++) {
                    final String order = orderList.get(i);
                    final String[] segments = order.split(",\\s*");
                    for (int j = 0; j < segments.length; j++) {
                        final String key;
                        final boolean asc;
                        final Matcher matcher = pattern.matcher(segments[j]);
                        if (matcher.find()) {
                            key = segments[j].substring(0, matcher.start());
                            asc = !segments[j].substring(matcher.start() + 1, matcher.end()).equalsIgnoreCase("DESC");
                        } else {
                            key = segments[j];
                            asc = true;
                        }
                        final String[] s = key.split("\\.\\s*");
                        if (s.length > 1) {
                            if (tables.contains(s[0])) {
                                orders.add(new Order(s[0], s[1], asc));
                            }
                        } else {
                            if (tables.size() == 1) {
                                orders.add(new Order(tables.get(0), s[0], asc));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the filter querystring parameters and sets the filters of the query.
     * @param qs the querystring.
     */
    private void setFilters(QueryString qs) {
        if (tables.size() > 0 && qs.size() > 0) {
            Iterator<String> iterator = qs.keySet().iterator();
            while (iterator.hasNext()) {
                final String k = iterator.next();
                if (!k.startsWith("_")) {
                    final String[] s = k.split("\\.\\s*", 2);
                    if (s.length > 1) {
                        if (tables.contains(s[0])) {
                            List<String> v = qs.get(k);
                            for (int i = 0; i < v.size(); i++) {
                                filters.add(new Filter(s[0], s[1], v.get(i)));
                            }
                        }
                    } else {
                        if (tables.size() == 1) {
                            final String table = tables.get(0);
                            List<String> v = qs.get(k);
                            for (int i = 0; i < v.size(); i++) {
                                filters.add(new Filter(table, s[0], v.get(i)));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the '_page' querystring parameters and sets the pageIndex of the query.
     * @param qs the querystring.
     */
    private void setPageIndex(QueryString qs) {
        List<String> indexList = qs.get("_page");
        if (indexList != null) {
            try {
                int n = Integer.parseInt(indexList.get(0));
                if (n > 0) {
                    pageIndex = n;
                }
            } catch (NumberFormatException ex) {
            }
        }
    }

    /**
     * Parses the '_pagesize' querystring parameters and sets pageSize of the query.
     * @param qs the querystring.
     */
    private void setPageSize(QueryString qs) {
        List<String> sizeList = qs.get("_pagesize");
        if (sizeList != null) {
            try {
                int n = Integer.parseInt(sizeList.get(0));
                if (n > 0) {
                    pageSize = n;
                }
            } catch (NumberFormatException ex) {
            }
        }
    }

    /**
     * Parses the specified collection and sets tables, joinOperations, boundedColumns and filterBoundaries.
     * @param collection a String representing a combination of tables and join operations.
     * Symbol '+' in collection represents INNER JOIN operation.
     * Symbol '!+' in collection represents LEFT JOIN operation.
     * Symbol '+!' in collection represents RIGHT JOIN operation.
     * Symbol '!+!' in collection represents OUTER JOIN operation.
     * @throws SQLException
     * @throws QueryException
     */
    private void setTablesJoinsAndBoundaries(String collection) throws SQLException, QueryException {
        if (collection != null) {
            final Pattern pattern = Pattern.compile("!?\\+!?");
            final Matcher matcher = pattern.matcher(collection);
            int pos = 0;
            while (matcher.find()) {
                if (matcher.start() != pos) {
                    final String[] s = collection.substring(pos, matcher.start()).split("\\?", 2);
                    tables.add(s[0]);
                    if (s.length > 1) {
                        QueryString qs = new QueryString(s[1]);
                        setBoundedColumns(s[0], qs);
                        setFilterBoundaries(s[0], qs);
                    }
                    switch(matcher.group().length()) {
                        case 1:
                            {
                                joinOperations.add(Join.INNER);
                                break;
                            }
                        case 2:
                            {
                                if (matcher.group().equals("|+")) {
                                    joinOperations.add(Join.LEFT);
                                } else {
                                    joinOperations.add(Join.RIGHT);
                                }
                                break;
                            }
                        case 3:
                            {
                                joinOperations.add(Join.OUTER);
                                break;
                            }
                    }
                } else {
                    throw new QueryException(QueryExceptionState.INVALID_COLLECTION, "Symbol " + matcher.group() + " is either set at the beggining of the collection or followed by another join symbol instead of a table.");
                }
                pos = matcher.end();
            }
            if (pos != collection.length()) {
                final String[] s = collection.substring(pos).split("\\?", 2);
                tables.add(s[0]);
                if (s.length > 1) {
                    QueryString qs = new QueryString(s[1]);
                    setBoundedColumns(s[0], qs);
                    setFilterBoundaries(s[0], qs);
                }
            } else {
                throw new QueryException(QueryExceptionState.INVALID_COLLECTION, "Symbol " + matcher.group() + " is located at the end of the collection string.");
            }
        } else {
            throw new QueryException(QueryExceptionState.INVALID_COLLECTION, "Collection string is null.");
        }
    }

    /**
     * Parses the specified table's querystring and sets the boundedColumns of the query.
     * @param table the name of the table.
     * @param qs the querystring.
     */
    private void setBoundedColumns(String table, QueryString qs) {
        final List<String> showList = qs.get("_show");
        if (showList != null) {
            for (int i = 0; i < showList.size(); i++) {
                final String show = showList.get(i);
                final String[] segments = show.split(",\\s*");
                for (int j = 0; j < segments.length; j++) {
                    final String[] s = segments[j].split("\\.\\s*", 2);
                    if (s.length > 1) {
                        if (s[0].equalsIgnoreCase(table)) {
                            boundedColumns.add(table + "." + s[1]);
                        }
                    } else {
                        boundedColumns.add(table + "." + s[0]);
                    }
                }
            }
        }
    }

    /**
     * Parses the specified table's querystring and sets the filterBoundaries of the query.
     * @param table the name of the table.
     * @param qs the querystring.
     */
    private void setFilterBoundaries(String table, QueryString qs) {
        if (qs.size() > 0) {
            Iterator<String> iterator = qs.keySet().iterator();
            while (iterator.hasNext()) {
                final String k = iterator.next();
                if (!k.startsWith("_")) {
                    final String[] s = k.split("\\.\\s*", 2);
                    if (s.length > 1) {
                        if (s[0].equalsIgnoreCase(table)) {
                            List<String> v = qs.get(k);
                            for (int i = 0; i < v.size(); i++) {
                                filterBoundaries.add(new Filter(table, s[1], v.get(i)));
                            }
                        }
                    } else {
                        List<String> v = qs.get(k);
                        for (int i = 0; i < v.size(); i++) {
                            filterBoundaries.add(new Filter(table, s[0], v.get(i)));
                        }
                    }
                }
            }
        }
    }

    /**
     * Indicates whether the designated column is allowed to be displayed.
     * @param table The name of the column's parent table.
     * @param column The name of the column.
     * @return
     */
    private boolean isColumnAllowed(String table, String column) {
        if (boundedColumns.isEmpty()) {
            return true;
        } else {
            return boundedColumns.contains(table + "." + column);
        }
    }

    /**
     * Returns the tables of the query.
     * @return
     */
    public String[] getTables() {
        return tables.toArray(new String[0]);
    }

    /**
     * Returns the number of tables in the query.
     * @return
     */
    public int getTableCount() {
        return tables.size();
    }

    /**
     * Returns the JOIN operations of the query.
     * @return
     */
    public Join[] getJoinOperations() {
        return joinOperations.toArray(new Join[0]);
    }

    /**
     * Returns the filterBoundaries of the query.
     * @return
     */
    public Filter[] getFilterBoundaries() {
        return filterBoundaries.toArray(new Filter[0]);
    }

    /**
     * Returns the boundedColumns of the query.
     * @return
     */
    public Set<String> getBoundedColumns() {
        return boundedColumns;
    }

    /**
     * Returns the columns of the query.
     * @return
     */
    public Column[] getColumns() {
        return columns.toArray(new Column[0]);
    }

    /**
     * Returns the name of the columns of the query that belong to the specified table.
     * @return
     */
    public String[] getColumns(String table) {
        List<String> cols = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            final Column column = columns.get(i);
            if (column.getTable().equalsIgnoreCase(table)) {
                cols.add(column.getName());
            }
        }
        return cols.toArray(new String[0]);
    }

    /**
     * Returns the filters of the query.
     * @return
     */
    public Filter[] getFilters() {
        return filters.toArray(new Filter[0]);
    }

    /**
     * Returns the order statements of the query.
     * @return
     */
    public Order[] getOrders() {
        return orders.toArray(new Order[0]);
    }

    /**
     * Returns the current page of the query.
     * @return
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * Returns the page size of the query.
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Validates the query against the designated database.
     * @param db
     * @throws SQLException
     * @return
     */
    public RestfulQuery validate(Database db) throws SQLException {
        DatabaseMetaInfo dbmi = db.getDatabaseMetaInfo();
        for (int i = tables.size() - 1; i > -1; i--) {
            final String table = tables.get(0);
            if (!dbmi.containsTable(table)) {
                tables.remove(i);
                if (i != 0) {
                    joinOperations.remove(i - 1);
                } else {
                    joinOperations.remove(0);
                }
            }
        }
        if (tables.isEmpty()) {
            final SqlExceptionState state = SqlExceptionState.TABLES_DO_NOT_EXIST;
            throw new SQLException("The request you made contains no valid tables.", state.name(), state.code());
        }
        for (int i = columns.size() - 1; i > -1; i--) {
            final String table = columns.get(i).getTable();
            final String column = columns.get(i).getName();
            final ResultSetMetaInfo rsmi = db.getResultSetMetaInfo(table);
            if (!dbmi.containsTable(table) || !rsmi.containsColumn(column)) {
                columns.remove(i);
            }
        }
        if (columns.isEmpty()) {
            if (boundedColumns.size() > 0) {
                final SqlExceptionState state = SqlExceptionState.COLUMNS_DO_NOT_EXIST;
                throw new SQLException("The request you made contains no valid columns.", state.name(), state.code());
            } else {
                for (int i = 0; i < tables.size(); i++) {
                    final String table = tables.get(i);
                    final ResultSetMetaInfo rsmi = db.getResultSetMetaInfo(table);
                    for (int j = 1; j <= rsmi.getColumnCount(); j++) {
                        columns.add(new Column(table, rsmi.getColumn(j)));
                    }
                }
            }
        }
        for (int i = filters.size() - 1; i > -1; i--) {
            final String table = filters.get(i).getTable();
            final String column = filters.get(i).getColumn();
            final ResultSetMetaInfo rsmi = db.getResultSetMetaInfo(table);
            if (!dbmi.containsTable(table) || !rsmi.containsColumn(column)) {
                filters.remove(i);
            }
        }
        for (int i = filterBoundaries.size() - 1; i > -1; i--) {
            final String table = filterBoundaries.get(i).getTable();
            final String column = filterBoundaries.get(i).getColumn();
            final ResultSetMetaInfo rsmi = db.getResultSetMetaInfo(table);
            if (!dbmi.containsTable(table) || !rsmi.containsColumn(column)) {
                filterBoundaries.remove(i);
            }
        }
        for (int i = orders.size() - 1; i > -1; i--) {
            final String table = orders.get(i).getTable();
            final String column = orders.get(i).getColumn();
            final ResultSetMetaInfo rsmi = db.getResultSetMetaInfo(table);
            if (!dbmi.containsTable(table) || !rsmi.containsColumn(column)) {
                orders.remove(i);
            }
        }
        return this;
    }
}
