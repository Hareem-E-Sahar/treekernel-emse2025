package com.restsql.db.sql;

import com.restsql.atom.util.EntryUtil;
import com.restsql.db.ColumnInfo;
import com.restsql.db.Database;
import com.restsql.db.SQLExceptionState;
import com.restsql.db.TableInfo;
import com.restsql.http.Parameters;
import com.restsql.util.collection.MultiHashValuedMap;
import com.restsql.util.collection.MultiValuedMap;
import com.sun.net.httpserver.HttpExchange;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;

/**
 * GenericSQLDesigner can be used for designing SQL queries.
 * @author dimitris@jmike.gr
 * @update Jiann.lu@yahoo.com (Updated for Restsql)
 */
public class GenericSQLDesigner implements SQLDesigner {

    protected Database db;

    protected List<String> tableCollection = new ArrayList<String>();

    protected List<JoinEnum> joinCollection = new ArrayList<JoinEnum>();

    protected Set<String> columnBoundaries = new HashSet<String>();

    protected List<Filter> filterBoundaries = new ArrayList<Filter>();

    protected List<Column> columnCollection = new ArrayList<Column>();

    protected List<Filter> filters = new ArrayList<Filter>();

    protected List<Order> orderCollection = new ArrayList<Order>();

    protected int pageIndex = 1;

    protected int pageSize = 50;

    protected MultiValuedMap values = new MultiValuedMap();

    protected String host;

    protected int port;

    protected String path;

    protected String query;

    protected String ragment;

    protected String mediatype;

    public GenericSQLDesigner(Database db) {
        this.db = db;
    }

    /**
     * Clear all collections contents
     * @author jiann.lu@yahoo.com
     */
    private void Clear() {
        tableCollection.clear();
        joinCollection.clear();
        columnBoundaries.clear();
        filterBoundaries.clear();
        columnCollection.clear();
        filters.clear();
        orderCollection.clear();
        pageIndex = 1;
        pageSize = 50;
        values = new MultiValuedMap();
    }

    /**
     * Parse the Uri address
     * @author jiann.lu@yahoo.com
     */
    public void parseForGet(HttpExchange httpExchange) throws SQLException, InvalidCollectionException {
        Clear();
        URI uri = httpExchange.getRequestURI();
        path = uri.getPath().split("\\.")[0];
        query = uri.getQuery();
        parseBasic(path);
        parseTableQueryParameters(query);
    }

    public void parseForPost(HttpExchange httpExchange) throws SQLException, InvalidCollectionException, XMLStreamException {
        Clear();
        URI uri = httpExchange.getRequestURI();
        path = uri.getPath().split("\\.")[0];
        parseBasic(path);
        values = EntryUtil.EntryStreamToValueSet(httpExchange.getRequestBody());
    }

    public void parseForPut(HttpExchange httpExchange) throws SQLException, InvalidCollectionException, XMLStreamException {
        this.parseForPost(httpExchange);
    }

    public void parseForDelete(HttpExchange httpExchange) throws SQLException, InvalidCollectionException {
        Clear();
        URI uri = httpExchange.getRequestURI();
        path = uri.getPath().split("\\.")[0];
        parseBasic(path);
    }

    /**
     * Parses the specified collection.
     * @param collection a String representing a combination of tables and join operations as specified in Restful SQL specification.
     * Symbol '/' in collection represents INNER JOIN operation.
     * Symbol '+/' in collection represents LEFT JOIN operation.
     * Symbol '/+' in collection represents RIGHT JOIN operation.
     * Symbol '+/+' in collection represents OUTER JOIN operation.
     * @throws java.sql.SQLException
     * @throws InvalidCollectionException
     */
    private void parseBasic(String uri_path) throws SQLException, InvalidCollectionException {
        if (uri_path != null) {
            String path_content = uri_path.substring(1);
            final TableInfo tableInfo = db.getTableInfo();
            final Pattern patternjoin = Pattern.compile("\\+?\\/\\+?");
            final Matcher matcher = patternjoin.matcher(path_content);
            int table_name_statrt_pos = 0;
            while (matcher.find()) {
                if (matcher.start() != table_name_statrt_pos) {
                    final String table_name = path_content.substring(table_name_statrt_pos, matcher.start());
                    this.parseTableName(table_name, table_name_statrt_pos, tableInfo);
                    switch(matcher.group().length()) {
                        case 1:
                            {
                                joinCollection.add(JoinEnum.INNER);
                                break;
                            }
                        case 2:
                            {
                                if (matcher.group().equals("/+")) {
                                    joinCollection.add(JoinEnum.LEFT);
                                } else {
                                    joinCollection.add(JoinEnum.RIGHT);
                                }
                                break;
                            }
                        case 3:
                            {
                                joinCollection.add(JoinEnum.OUTER);
                                break;
                            }
                    }
                } else {
                    throw new InvalidCollectionException("Symbol " + matcher.group() + " is either set at the beggining of the collection or followed by another join symbol instead of a table.");
                }
                table_name_statrt_pos = matcher.end();
            }
            if (table_name_statrt_pos != path_content.length()) {
                parseTableName(path_content, table_name_statrt_pos, tableInfo);
            } else {
                throw new InvalidCollectionException("Symbol " + matcher.group() + " is located at the end of the collection string.");
            }
        } else {
            throw new InvalidCollectionException("Collection string is null.");
        }
    }

    private void parstFilters(String filter, String table_name, int para_index) throws InvalidCollectionException, SQLException {
        final ColumnInfo columnInfo = db.getColumnInfo(table_name);
        final Pattern patternfilter = Pattern.compile(">=|<=|=|>|<");
        final Matcher matcherfilter = patternfilter.matcher(filter);
        int statrt_pos = 0;
        if (matcherfilter.find()) {
            if (matcherfilter.start() != statrt_pos) {
                String col_name = filter.substring(0, matcherfilter.start());
                String col_val;
                switch(matcherfilter.group().length()) {
                    case 1:
                        {
                            col_val = filter.substring(matcherfilter.start() + 1, filter.length());
                            if (matcherfilter.group().equals("=")) {
                                filterBoundaries.add(new Filter(table_name, col_name, col_val, "="));
                            } else if (matcherfilter.group().equals(">")) {
                                filterBoundaries.add(new Filter(table_name, col_name, col_val, ">"));
                            } else if (matcherfilter.group().equals("<")) {
                                filterBoundaries.add(new Filter(table_name, col_name, col_val, "<"));
                            }
                            break;
                        }
                    case 2:
                        {
                            col_val = filter.substring(matcherfilter.start() + 2, filter.length());
                            if (matcherfilter.group().equals(">=")) {
                                filterBoundaries.add(new Filter(table_name, col_name, col_val, ">="));
                            } else {
                                filterBoundaries.add(new Filter(table_name, col_name, col_val, "<="));
                            }
                            break;
                        }
                }
            }
        } else {
            int pri_col_index = Integer.parseInt(columnInfo.getPriColPosition().get(para_index).toString());
            filterBoundaries.add(new Filter(table_name, columnInfo.getColumn(pri_col_index), filter, "="));
        }
    }

    private void parseTableName(String path_table, int table_name_statrt_pos, TableInfo tableInfo) throws SQLException, InvalidCollectionException {
        String pre_table_name = path_table.substring(table_name_statrt_pos);
        final Pattern pattern_para = Pattern.compile("\\(.*\\)");
        Matcher pre_table_matcher = pattern_para.matcher(pre_table_name);
        String table_name;
        if (pre_table_matcher.find()) {
            String key_value = pre_table_name.substring(pre_table_matcher.start() + 1, pre_table_matcher.end() - 1);
            table_name = pre_table_name.substring(0, pre_table_matcher.start());
            if (tableInfo.exists(table_name)) {
                tableCollection.add(table_name);
                final ColumnInfo columnInfo = db.getColumnInfo(table_name);
                String[] filters_str = key_value.split(",");
                int paraIndex = 0;
                for (String filter : filters_str) {
                    parstFilters(filter, table_name, paraIndex);
                    paraIndex++;
                }
            } else {
                final SQLExceptionState state = SQLExceptionState.TABLE_NOT_FOUND;
                throw new SQLException("Table " + table_name + " does not exist in database.", state.name(), state.code());
            }
        } else {
            table_name = pre_table_name;
            if (tableInfo.exists(table_name)) {
                tableCollection.add(table_name);
            } else {
                final SQLExceptionState state = SQLExceptionState.TABLE_NOT_FOUND;
                throw new SQLException("Table " + table_name + " does not exist in database.", state.name(), state.code());
            }
        }
    }

    private void parseTableQueryParameters(String uri_queries) {
        if (uri_queries != null && !uri_queries.isEmpty()) {
            String[] queries_contents = uri_queries.split("\\&");
            for (int i = 0; i < queries_contents.length; i++) {
                String[] quries = queries_contents[i].split("\\=");
                if (queries_contents[i].indexOf("$") > -1) {
                    String element = quries[0];
                    if (element.equals(Parameters.Order)) {
                        String[] order_sets = quries[1].split("\\,");
                        for (String order_set : order_sets) {
                            String[] order_keys = order_set.split("\\.");
                            if (order_keys.length > 1) {
                                orderCollection.add(new Order(order_keys[0], order_keys[1].replaceAll("\\((DESC|desc)\\)", ""), !order_keys[1].matches("\\((DESC|desc)\\)")));
                            } else {
                                orderCollection.add(new Order(this.tableCollection.get(0), order_keys[0].replaceAll("\\((DESC|desc)\\)", ""), !order_keys[0].matches("\\((DESC|desc)\\)")));
                            }
                        }
                    } else if (element.equals(Parameters.Page)) {
                        this.pageIndex = Integer.parseInt(quries[1]);
                    } else if (element.equals(Parameters.PageSize)) {
                        this.pageSize = Integer.parseInt(quries[1]);
                    } else if (element.equals(Parameters.Show)) {
                        String[] show_sets = quries[1].split("\\,");
                        for (String show_set : show_sets) {
                            String[] show_keys = show_set.split("\\.");
                            if (show_keys.length > 1) {
                                columnCollection.add(new Column(show_keys[0], show_keys[1]));
                            } else {
                                columnCollection.add(new Column(this.tableCollection.get(0), show_keys[0]));
                            }
                        }
                    }
                } else {
                    String[] quries_names = quries[0].split("\\.");
                    if (quries_names.length > 1) {
                        filterBoundaries.add(new Filter(quries_names[0], quries_names[1], quries[1]));
                    } else {
                        filterBoundaries.add(new Filter(this.tableCollection.get(0), quries_names[0], quries[1]));
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
        if (columnBoundaries.isEmpty()) {
            return true;
        } else {
            return columnBoundaries.contains(table + "." + column);
        }
    }

    /**
     * Returns the filter boundaries of this query as a Multivalued Map containing the qualified name (table.column) of the column as a key.
     * @return
     */
    protected MultiHashValuedMap getFilterBoundariesMap() {
        MultiHashValuedMap map = new MultiHashValuedMap();
        for (int i = 0; i < filterBoundaries.size(); i++) {
            Filter f = filterBoundaries.get(i);
            map.add(f.getTable() + "." + f.getColumn(), f.getValue());
        }
        return map;
    }

    /**
     * Constructs a Select SQL command.
     * @return
     * @throws java.sql.SQLException
     * @throws InvalidDataException
     */
    public String getSelectSQL() throws SQLException, InvalidInputDataException {
        return null;
    }

    /**
     * Constructs an Insert SQL command.
     * @return
     * @throws java.sql.SQLException
     * @throws InvalidDataException
     */
    public String getInsertSQL() throws SQLException, InvalidInputDataException {
        return null;
    }

    /**
     * Constructs a Delete SQL command.
     * @return
     * @throws java.sql.SQLException
     * @throws InvalidDataException
     */
    public String getDeleteSQL() throws SQLException, InvalidInputDataException {
        return null;
    }

    /**
     * Constructs an Update SQL command.
     * @return
     * @throws java.sql.SQLException
     * @throws InvalidDataException 
     */
    public String getUpdateSQL() throws SQLException, InvalidInputDataException {
        return null;
    }

    /**
     * Inner class representing a column.
     */
    protected class Column {

        private final String table;

        private final String name;

        private final int index;

        public String getTable() {
            return table;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public Column(String name) {
            this.table = "";
            this.name = name;
            this.index = -1;
        }

        public Column(String table, String name) {
            this.table = table;
            this.name = name;
            this.index = -1;
        }

        public Column(String table, String name, int index) {
            this.table = table;
            this.name = name;
            this.index = index;
        }
    }

    /**
     * Inner class representing a filter.
     */
    protected class Filter {

        private final String table;

        private final String column;

        private final String value;

        private final String operator;

        public String getTable() {
            return table;
        }

        public String getOperator() {
            return operator;
        }

        public String getColumn() {
            return column;
        }

        public String getValue() {
            return value;
        }

        public Filter(String table, String column, String value, String operator) {
            this.table = table;
            this.column = column;
            this.value = value;
            this.operator = operator;
        }

        public Filter(String column, String value, String operator) {
            this.table = "";
            this.column = column;
            this.value = value;
            this.operator = operator;
        }
    }

    /**
     * Inner class representing an order.
     */
    protected class Order {

        private final String table;

        private final String column;

        private final boolean asc;

        public String getTable() {
            return table;
        }

        public String getColumn() {
            return column;
        }

        public boolean isAsc() {
            return asc;
        }

        public Order(String table, String column, boolean asc) {
            this.table = table;
            this.column = column;
            this.asc = asc;
        }

        public Order(String column, boolean asc) {
            this.table = "";
            this.column = column;
            this.asc = asc;
        }
    }
}
