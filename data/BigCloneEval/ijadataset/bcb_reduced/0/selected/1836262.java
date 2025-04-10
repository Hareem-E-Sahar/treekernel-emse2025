package org.melati.poem;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.melati.util.PageEnumeration;
import org.melati.util.CountedDumbPageEnumeration;
import org.melati.util.ArrayEnumeration;
import org.melati.util.FlattenedEnumeration;
import org.melati.util.FilteredEnumeration;
import org.melati.util.MappedEnumeration;
import org.melati.util.EnumUtils;
import org.melati.util.StringUtils;
import org.melati.util.ArrayUtils;
import org.melati.util.SortUtils;
import org.melati.util.Order;
import org.melati.util.Procedure;
import org.melati.util.Transactioned;
import org.melati.util.TransactionedSerial;
import org.melati.util.Cache;
import org.melati.util.CachedIndexFactory;
import org.melati.poem.dbms.Dbms;

/**
 * A Table.
 *
 * @author WilliamC@paneris.org
 * @todo Allow selection based on null fields
 * @todo Write equals method
 * @todo See FIXMEs
 **/
public class Table implements Selectable {

    public static final int CACHE_LIMIT_DEFAULT = 100;

    private Table _this = this;

    private Database database;

    private String name;

    private String quotedName;

    private DefinitionSource definitionSource;

    TableInfo info = null;

    private TableListener[] listeners = {};

    private Column[] columns = {};

    private Hashtable columnsByName = new Hashtable();

    private Column troidColumn = null;

    private Column deletedColumn = null;

    private Column canReadColumn = null;

    private Column canSelectColumn = null;

    private Column canWriteColumn = null;

    private Column canDeleteColumn = null;

    private Column displayColumn = null;

    private Column searchColumn = null;

    private String defaultOrderByClause = null;

    private Column[][] displayColumns = new Column[DisplayLevel.count()][];

    private Column[] searchColumns = null;

    private TransactionedSerial serial;

    private CachedSelection allTroids = null;

    private Hashtable cachedSelections = new Hashtable();

    private Hashtable cachedCounts = new Hashtable();

    private Hashtable cachedExists = new Hashtable();

    public Table(Database database, String name, DefinitionSource definitionSource) {
        this.database = database;
        this.name = name;
        this.definitionSource = definitionSource;
        serial = new TransactionedSerial(database);
    }

    /**
   * Do stuff immediately after table initialisation.
   * <p>
   * This base method clears the column info caches and adds a listener
   * to the column info table to maintain the caches.
   * <p>
   * It may be overridden to perform other actions. For example to
   * ensure required rows exist in tables that define numeric ID's for
   * codes.
   *
   * @see #notifyColumnInfo(ColumnInfo)
   * @see #clearColumnInfoCaches()
   */
    protected void postInitialise() {
        clearColumnInfoCaches();
        database.getColumnInfoTable().addListener(new TableListener() {

            public void notifyTouched(PoemTransaction transaction, Table table, Persistent persistent) {
                _this.notifyColumnInfo((ColumnInfo) persistent);
            }

            public void notifyUncached(Table table) {
                _this.clearColumnInfoCaches();
            }
        });
    }

    /**
   * The database to which the table is attached.
   */
    public final Database getDatabase() {
        return database;
    }

    /**
   * The table's programmatic name.  Identical with its name in the DSD (if the
   * table was defined there), in its <TT>tableinfo</TT> entry, and in the
   * RDBMS itself.
   */
    public final String getName() {
        return name;
    }

    public final String quotedName() {
        if (quotedName == null) quotedName = database.quotedName(name);
        return quotedName;
    }

    /**
   * The human-readable name of the table.  POEM itself doesn't use this, but
   * it's available to applications and Melati's generic admin system as a
   * default label for the table and caption for its records.
   */
    public final String getDisplayName() {
        return info.getDisplayname();
    }

    /**
   * A brief description of the table's function.  POEM itself doesn't use
   * this, but it's available to applications and Melati's generic admin system
   * as a default label for the table and caption for its records.
   */
    public final String getDescription() {
        return info.getDescription();
    }

    /**
   * The category of this table.  POEM itself doesn't use
   * this, but it's available to applications and Melati's generic admin system
   * as a default label for the table and caption for its records.
   */
    public final TableCategory getCategory() {
        return info.getCategory();
    }

    /**
   * @see org.melati.admin.Admin#dsdTemplate
   */
    public final TableInfo getInfo() {
        return info;
    }

    /**
   * The troid (<TT>id</TT>) of the table's entry in the <TT>tableinfo</TT>
   * table.  It will always have one (except during initialisation, which the
   * application programmer will never see).
   */
    public final Integer tableInfoID() {
        return info == null ? null : info.troid();
    }

    /**
   * The table's column with a given name.  If the table is defined in the DSD
   * under the name <TT><I>foo</I></TT>, there will be an
   * application-specialised <TT>Table</TT> subclass, called
   * <TT><I>Foo</I>Table</TT> (and available as <TT>get<I>Foo</I>Table</TT>
   * from the application-specialised <TT>Database</TT> subclass) which has
   * extra named methods for accessing the table's predefined <TT>Column</TT>s.
   *
   * @exception NoSuchColumnPoemException if there is no column with that name
   */
    public final Column getColumn(String name) throws NoSuchColumnPoemException {
        Column column = (Column) columnsByName.get(name);
        if (column == null) throw new NoSuchColumnPoemException(this, name); else return column;
    }

    /**
   * All the table's columns.
   *
   * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
   * @see Column
   */
    public final Enumeration columns() {
        return new ArrayEnumeration(columns);
    }

    public final int getColumnsCount() {
        return columns.length;
    }

    Column columnWithColumnInfoID(int columnInfoID) {
        for (Enumeration c = columns(); c.hasMoreElements(); ) {
            Column column = (Column) c.nextElement();
            Integer id = column.columnInfoID();
            if (id != null && id.intValue() == columnInfoID) return column;
        }
        return null;
    }

    /**
   * The table's troid column.  Every table in a POEM database must have a
   * troid (table row ID, or table-unique non-nullable integer primary key),
   * often but not necessarily called <TT>id</TT>, so that it can be
   * conveniently `named'.
   *
   * @see #getObject(java.lang.Integer)
   */
    public final Column troidColumn() {
        return troidColumn;
    }

    /**
   * The table's deleted-flag column, if any.  FIXME.
   */
    public final Column deletedColumn() {
        return deletedColumn;
    }

    /**
   * The table's primary display column, if any.  This is the column used to
   * represent records from the table concisely in reports or whatever.  It is
   * determined at initialisation time by examining the <TT>Column</TT>s
   * <TT>getPrimaryDisplay()</TT> flags.
   *
   * @return the table's display column, or <TT>null</TT> if it hasn't got one
   *
   * @see Column#setColumnInfo
   * @see ReferencePoemType#_stringOfCooked
   * @see DisplayLevel#primary
   */
    public final Column displayColumn() {
        return displayColumn == null ? troidColumn : displayColumn;
    }

    final void setDisplayColumn(Column column) {
        displayColumn = column;
    }

    /**
  * In a similar manner to the primary display column, each table can have 
  * one primary criterion column.
  * <p>
  * The Primary Criterion is the main grouping field of the table, 
  * ie the most important non-unique type field.
  * <p>
  * For example the Primary Criterion for a User table might be Nationality.
  *
  * @see Searchability
  */
    public final Column primaryCriterionColumn() {
        return searchColumn;
    }

    void setSearchColumn(Column column) {
        searchColumn = column;
    }

    protected String defaultOrderByClause() {
        String clause = defaultOrderByClause;
        if (clause == null) {
            clause = EnumUtils.concatenated(", ", new MappedEnumeration(new ArrayEnumeration(SortUtils.sorted(new Order() {

                public boolean lessOrEqual(Object a, Object b) {
                    return ((Column) a).getDisplayOrderPriority().intValue() <= ((Column) b).getDisplayOrderPriority().intValue();
                }
            }, new FilteredEnumeration(columns()) {

                public boolean isIncluded(Object column) {
                    return ((Column) column).getDisplayOrderPriority() != null;
                }
            }))) {

                public Object mapped(Object column) {
                    String sort = ((Column) column).fullQuotedName();
                    if (((Column) column).getSortDescending()) sort += " desc";
                    return sort;
                }
            });
            if (clause.equals("")) clause = displayColumn().fullQuotedName();
            defaultOrderByClause = clause;
        }
        return clause;
    }

    private void clearColumnInfoCaches() {
        defaultOrderByClause = null;
        for (int i = 0; i < displayColumns.length; ++i) displayColumns[i] = null;
    }

    void notifyColumnInfo(ColumnInfo info) {
        if (info == null || info.getTableinfo_unsafe().equals(tableInfoID())) clearColumnInfoCaches();
    }

    private Column[] columnsWhere(String whereClause) {
        Enumeration colIDs = getDatabase().getColumnInfoTable().troidSelection(database.quotedName("tableinfo") + " = " + tableInfoID() + " AND (" + whereClause + ")", null, false, null);
        Vector them = new Vector();
        while (colIDs.hasMoreElements()) {
            Column column = columnWithColumnInfoID(((Integer) colIDs.nextElement()).intValue());
            if (column != null) them.addElement(column);
        }
        Column[] columns = new Column[them.size()];
        them.copyInto(columns);
        return columns;
    }

    /**
   * Return columns at a display level in display order.
   */
    public final Enumeration displayColumns(DisplayLevel level) {
        Column[] columns = displayColumns[level.index.intValue()];
        if (columns == null) displayColumns[level.index.intValue()] = columns = columnsWhere(database.quotedName("displaylevel") + " <= " + level.index);
        return new ArrayEnumeration(columns);
    }

    public final int displayColumnsCount(DisplayLevel level) {
        int l = level.index.intValue();
        if (displayColumns[l] == null) displayColumns(level);
        return displayColumns[l].length;
    }

    /**
   * The table's columns for detailed display in display order.
   *
   * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
   * @see Column
   * @see #displayColumns(DisplayLevel)
   * @see DisplayLevel#detail
   */
    public final Enumeration getDetailDisplayColumns() {
        return displayColumns(DisplayLevel.detail);
    }

    public final int getDetailDisplayColumnsCount() {
        return displayColumnsCount(DisplayLevel.detail);
    }

    /**
   * The table's columns designated for display in a record, in display order.
   *
   * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
   * @see Column
   * @see #displayColumns(DisplayLevel)
   * @see DisplayLevel#record
   */
    public final Enumeration getRecordDisplayColumns() {
        return displayColumns(DisplayLevel.record);
    }

    public final int getRecordDisplayColumnsCount() {
        return displayColumnsCount(DisplayLevel.record);
    }

    /**
   * The table's columns designated for display in a record summary, in display
   * order.
   *
   * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
   * @see Column
   * @see #displayColumns(DisplayLevel)
   * @see DisplayLevel#summary
   */
    public final Enumeration getSummaryDisplayColumns() {
        return displayColumns(DisplayLevel.summary);
    }

    /**
   * The table's columns designated for use as search criteria, in display
   * order.
   *
   * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
   * @see Column
   */
    public final Enumeration getSearchCriterionColumns() {
        Column[] columns = searchColumns;
        if (columns == null) searchColumns = columns = columnsWhere(database.quotedName("searchability") + " <= " + Searchability.yes.index);
        return new ArrayEnumeration(searchColumns);
    }

    public final int getSearchCriterionColumnsCount() {
        if (searchColumns == null) getSearchCriterionColumns();
        return searchColumns.length;
    }

    private Dbms dbms() {
        return getDatabase().getDbms();
    }

    private void dbModifyStructure(String sql) throws StructuralModificationFailedPoemException {
        if (PoemThread.inSession()) PoemThread.commit();
        try {
            database.getCommittedConnection().createStatement().executeUpdate(sql);
            database.log(new StructuralModificationLogEvent(sql));
        } catch (SQLException e) {
            throw new StructuralModificationFailedPoemException(sql, e);
        }
    }

    private void dbCreateTable() {
        StringBuffer sqb = new StringBuffer();
        sqb.append("CREATE TABLE " + quotedName() + " (");
        for (int c = 0; c < columns.length; ++c) {
            if (c != 0) sqb.append(", ");
            sqb.append(columns[c].quotedName() + " " + columns[c].getSQLType().sqlDefinition(dbms()));
        }
        sqb.append(")");
        dbModifyStructure(sqb.toString());
    }

    private void dbAddColumn(Column column) {
        dbModifyStructure("ALTER TABLE " + quotedName() + " ADD COLUMN " + column.quotedName() + " " + column.getSQLType().sqlDefinition(dbms()));
    }

    private void dbCreateIndex(Column column) {
        if (column.getIndexed()) {
            if (!dbms().canBeIndexed(column)) {
                database.log(new UnindexableLogEvent(column));
            } else {
                dbModifyStructure("CREATE " + (column.getUnique() ? "UNIQUE " : "") + "INDEX " + database.quotedName(name + "_" + column.getName() + "_index") + " ON " + quotedName() + " " + "(" + column.quotedName() + dbms().getIndexLength(column) + ")");
            }
        }
    }

    private PreparedStatement simpleInsert(Connection connection) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO " + quotedName() + " (");
        for (int c = 0; c < columns.length; ++c) {
            if (c > 0) sql.append(", ");
            sql.append(columns[c].quotedName());
        }
        sql.append(") VALUES (");
        for (int c = 0; c < columns.length; ++c) {
            if (c > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(")");
        try {
            return connection.prepareStatement(sql.toString());
        } catch (SQLException e) {
            throw new SimplePrepareFailedPoemException(sql.toString(), e);
        }
    }

    private PreparedStatement simpleGet(Connection connection) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        for (int c = 0; c < columns.length; ++c) {
            if (c > 0) sql.append(", ");
            sql.append(columns[c].quotedName());
        }
        sql.append(" FROM " + quotedName() + " WHERE " + troidColumn.quotedName() + " = ?");
        try {
            return connection.prepareStatement(sql.toString());
        } catch (SQLException e) {
            throw new SimplePrepareFailedPoemException(sql.toString(), e);
        }
    }

    private PreparedStatement simpleModify(Connection connection) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE " + quotedName() + " SET ");
        for (int c = 0; c < columns.length; ++c) {
            if (c > 0) sql.append(", ");
            sql.append(columns[c].quotedName());
            sql.append(" = ?");
        }
        sql.append(" WHERE " + troidColumn.quotedName() + " = ?");
        try {
            return connection.prepareStatement(sql.toString());
        } catch (SQLException e) {
            throw new SimplePrepareFailedPoemException(sql.toString(), e);
        }
    }

    private class TransactionStuff {

        PreparedStatement insert, modify, get;

        TransactionStuff(Connection connection) {
            insert = _this.simpleInsert(connection);
            modify = _this.simpleModify(connection);
            get = _this.simpleGet(connection);
        }
    }

    private CachedIndexFactory transactionStuffs = new CachedIndexFactory() {

        public Object reallyGet(int index) {
            return new TransactionStuff(Table.this.database.poemTransaction(index).getConnection());
        }
    };

    private TransactionStuff committedTransactionStuff = null;

    private synchronized TransactionStuff getCommittedTransactionStuff() {
        if (committedTransactionStuff == null) committedTransactionStuff = new TransactionStuff(database.getCommittedConnection());
        return committedTransactionStuff;
    }

    private void load(PreparedStatement select, Persistent persistent) {
        try {
            synchronized (select) {
                select.setInt(1, persistent.troid().intValue());
                ResultSet rs = select.executeQuery();
                try {
                    if (database.logSQL()) database.log(new SQLLogEvent(select.toString()));
                    if (!rs.next()) persistent.setStatusNonexistent(); else {
                        persistent.setStatusExistent();
                        for (int c = 0; c < columns.length; ++c) columns[c].load_unsafe(rs, c + 1, persistent);
                    }
                    persistent.dirty = false;
                    persistent.markValid();
                    if (rs.next()) throw new DuplicateTroidPoemException(this, persistent.troid());
                } finally {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        System.err.println("Cannot close resultset after exception.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new SimpleRetrievalFailedPoemException(e);
        } catch (ParsingPoemException e) {
            throw new UnexpectedParsingPoemException(e);
        } catch (ValidationPoemException e) {
            throw new UnexpectedValidationPoemException(e);
        }
    }

    void load(PoemTransaction transaction, Persistent persistent) {
        load(transaction == null ? getCommittedTransactionStuff().get : ((TransactionStuff) transactionStuffs.get(transaction.index)).get, persistent);
    }

    private void modify(PoemTransaction transaction, Persistent persistent) {
        PreparedStatement modify = ((TransactionStuff) transactionStuffs.get(transaction.index)).modify;
        synchronized (modify) {
            for (int c = 0; c < columns.length; ++c) columns[c].save_unsafe(persistent, modify, c + 1);
            try {
                modify.setInt(columns.length + 1, persistent.troid().intValue());
            } catch (SQLException e) {
                throw new SQLSeriousPoemException(e);
            }
            try {
                modify.executeUpdate();
            } catch (SQLException e) {
                throw dbms().exceptionForUpdate(this, modify, false, e);
            }
            if (database.logSQL()) database.log(new SQLLogEvent(modify.toString()));
        }
        persistent.postModify();
    }

    private void insert(PoemTransaction transaction, Persistent persistent) {
        PreparedStatement insert = ((TransactionStuff) transactionStuffs.get(transaction.index)).insert;
        synchronized (insert) {
            for (int c = 0; c < columns.length; ++c) columns[c].save_unsafe(persistent, insert, c + 1);
            try {
                insert.executeUpdate();
            } catch (SQLException e) {
                throw dbms().exceptionForUpdate(this, insert, true, e);
            }
            if (database.logSQL()) database.log(new SQLLogEvent(insert.toString()));
        }
        persistent.postInsert();
    }

    void delete(Integer troid, PoemTransaction transaction) {
        String sql = "DELETE FROM " + quotedName() + " WHERE " + troidColumn.quotedName() + " = " + troid.toString();
        try {
            Connection connection;
            if (transaction == null) connection = getDatabase().getCommittedConnection(); else {
                transaction.writeDown();
                connection = transaction.getConnection();
            }
            connection.createStatement().executeUpdate(sql);
            if (database.logSQL()) database.log(new SQLLogEvent(sql));
            cache.delete(troid);
        } catch (SQLException e) {
            throw new ExecutingSQLPoemException(sql, e);
        }
    }

    void writeDown(PoemTransaction transaction, Persistent persistent) {
        if (persistent.dirty) {
            troidColumn.setRaw_unsafe(persistent, persistent.troid());
            if (persistent.statusExistent()) {
                modify(transaction, persistent);
            } else if (persistent.statusNonexistent()) {
                insert(transaction, persistent);
                persistent.setStatusExistent();
            }
            persistent.dirty = false;
            persistent.postWrite();
        }
    }

    private Cache cache = new Cache(CACHE_LIMIT_DEFAULT);

    private static final Procedure invalidator = new Procedure() {

        public void apply(Object arg) {
            ((Transactioned) arg).invalidate();
        }
    };

    void uncacheContents() {
        cache.iterate(invalidator);
        serial.invalidate();
        TableListener[] listeners = this.listeners;
        for (int l = 0; l < listeners.length; ++l) listeners[l].notifyUncached(this);
    }

    void trimCache(int maxSize) {
        cache.trim(maxSize);
    }

    public Cache.Info getCacheInfo() {
        return cache.getInfo();
    }

    public void addListener(TableListener listener) {
        listeners = (TableListener[]) ArrayUtils.added(listeners, listener);
    }

    /**
   * Notify the table that one if its records is about to be changed in a
   * transaction.  You can (with care) use this to support cacheing of
   * frequently-used facts about the table's records.  For instance,
   * <TT>GroupMembershipTable</TT> and <TT>GroupCapabilityTable</TT> override
   * this to inform <TT>UserTable</TT> that its cache of users' capabilities
   * has become invalid.
   *
   * @param transaction the transaction in which the change will be made
   * @param persistent  the record to be changed
   *
   * @see GroupMembershipTable#notifyTouched
   */
    void notifyTouched(PoemTransaction transaction, Persistent persistent) {
        serial.increment(transaction);
        TableListener[] listeners = this.listeners;
        for (int l = 0; l < listeners.length; ++l) listeners[l].notifyTouched(transaction, this, persistent);
    }

    public long serial(PoemTransaction transaction) {
        return serial.current(transaction);
    }

    public void readLock() {
        serial(PoemThread.transaction());
    }

    /**
   * The object from the table with a given troid.
   *
   * @param troid       Every record (object) in a POEM database must have a
   *                    troid (table row ID, or table-unique non-nullable
   *                    integer primary key), often but not necessarily called
   *                    <TT>id</TT>, so that it can be conveniently `named' for
   *                    retrieval by this method.
   *
   * @return A <TT>Persistent</TT> of the record with the given troid;
   *         or, if the table was defined in the DSD under the name
   *         <TT><I>foo</I></TT>, an application-specialised subclass
   *         <TT><I>Foo</I></TT> of <TT>Persistent</TT>.  In that case, there
   *         will also be an application-specialised <TT>Table</TT> subclass,
   *         called <TT><I>Foo</I>Table</TT> (and available as
   *         <TT>get<I>Foo</I>Table</TT> from the application-specialised
   *         <TT>Database</TT> subclass), which has a matching method
   *         <TT>get<I>Foo</I>Object</TT> for obtaining the specialised object
   *         under its own type.  Note that no access checks are done at this
   *         stage: you may not be able to do anything with the object handle
   *         returned from this method without provoking a
   *         <TT>PoemAccessException</TT>.
   *
   * @exception NoSuchRowPoemException
   *                if there is no row in the table with the given troid
   *
   * @see Persistent#getTroid()
   */
    public Persistent getObject(Integer troid) throws NoSuchRowPoemException {
        Persistent persistent = (Persistent) cache.get(troid);
        if (persistent == null) {
            persistent = newPersistent();
            claim(persistent, troid);
            load(PoemThread.transaction(), persistent);
            if (persistent.statusExistent()) synchronized (cache) {
                Persistent tryAgain = (Persistent) cache.get(troid);
                if (tryAgain == null) cache.put(troid, persistent); else persistent = tryAgain;
            }
        }
        if (!persistent.statusExistent()) throw new NoSuchRowPoemException(this, troid);
        persistent.existenceLock(PoemThread.sessionToken());
        return persistent;
    }

    /**
   * The object from the table with a given troid.  See previous.
   *
   * @see #getObject(java.lang.Integer)
   */
    public Persistent getObject(int troid) throws NoSuchRowPoemException {
        return getObject(new Integer(troid));
    }

    /**
   * Return an SQL SELECT statement put together from the arguments,
   * and the default from and order by clauses. 
   * 
   * @deprecated Your app cannot call this but conceivably overrides
   * it, in which case it may not work with this release!
   */
    String selectionSQL(String whereClause, String orderByClause, boolean includeDeleted) {
        return selectionSQL(null, whereClause, orderByClause, includeDeleted, true);
    }

    /**
   * Return an SQL SELECT statement put together from the arguments and
   * default order by clause.
   * <p>
   * The from clause has been added as an argument because it is
   * inextricably linked to the when clause, but the default is 
   * {@link #quotedName()}.
   * <p>
   * This is public for debugging purposes (delete this line if you use it).
   *
   * @param fromClause Comma separated list of table names or null for default.
   * @todo Should work within some kind of limit
   */
    public String selectionSQL(String fromClause, String whereClause, String orderByClause, boolean includeDeleted, boolean cannotSelect) {
        return selectOrCountSQL(troidColumn().fullQuotedName(), fromClause, whereClause, orderByClause, includeDeleted, cannotSelect);
    }

    /**
   * @param transaction null now defaults to 
   *                    {@link PoemThread#transaction()} but
   *                    we do not rely on this much yet.
   */
    private ResultSet selectionResultSet(String fromClause, String whereClause, String orderByClause, boolean includeDeleted, boolean cannotDelete, PoemTransaction transaction) throws SQLPoemException {
        String sql = selectionSQL(fromClause, whereClause, orderByClause, includeDeleted, cannotDelete);
        if (transaction == null) {
            transaction = PoemThread.transaction();
        }
        try {
            Connection connection;
            if (transaction == null) connection = getDatabase().getCommittedConnection(); else {
                transaction.writeDown();
                connection = transaction.getConnection();
            }
            ResultSet rs = connection.createStatement().executeQuery(sql);
            SessionToken token = PoemThread._sessionToken();
            if (token != null) token.toTidy().add(rs);
            if (database.logSQL()) database.log(new SQLLogEvent(sql));
            return rs;
        } catch (SQLException e) {
            throw new ExecutingSQLPoemException(sql, e);
        }
    }

    public Enumeration troidSelection(String whereClause, String orderByClause, boolean includeDeleted, PoemTransaction transaction) {
        return troidsFrom(selectionResultSet(null, whereClause, orderByClause, includeDeleted, true, transaction));
    }

    /**
   * Return a selection of troids given arguments specifying a query.
   *
   * @see #troidSelection(String, String, boolean, PoemTransaction)
   * @param criteria Represents selection criteria possibly on joined tables
   * @param transaction A transaction or null for 
   *                    {@link PoemThread#transaction()}
   */
    public Enumeration troidSelection(Persistent criteria, String orderByClause, boolean includeDeleted, boolean cannotSelect, PoemTransaction transaction) {
        return troidsFrom(selectionResultSet(criteria.fromClause(), whereClause(criteria), orderByClause, includeDeleted, cannotSelect, transaction));
    }

    /**
   * Return an enumeration of troids given a result set.
   */
    private Enumeration troidsFrom(ResultSet them) {
        return new ResultSetEnumeration(them) {

            public Object mapped(ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        };
    }

    protected void rememberAllTroids(boolean flag) {
        if (flag) {
            if (allTroids == null) allTroids = new CachedSelection(this, null, null);
        } else allTroids = null;
    }

    protected void setCacheLimit(Integer limit) {
        cache.setSize(limit == null ? CACHE_LIMIT_DEFAULT : limit.intValue());
    }

    /**
   * A <TT>SELECT</TT>ion of troids of objects from the table meeting given
   * criteria.
   *
   * If the orderByClause is null, then the default order by clause is applied.
   * If the orderByClause is an empty string, ie "", then no ordering is 
   * applied.
   *
   * @return an <TT>Enumeration</TT> of <TT>Integer</TT>s, which can be mapped
   *         onto <TT>Persistent</TT> objects using <TT>getObject</TT>;
   *         or you can just use <TT>selection</TT>
   *
   * @see #getObject(java.lang.Integer)
   * @see #selection(java.lang.String, java.lang.String, boolean)
   */
    public Enumeration troidSelection(String whereClause, String orderByClause, boolean includeDeleted) throws SQLPoemException {
        CachedSelection allTroids = this.allTroids;
        if (allTroids != null && (whereClause == null || whereClause.equals("")) && (orderByClause == null || orderByClause.equals("") || orderByClause == defaultOrderByClause()) && !includeDeleted) return allTroids.troids(); else return troidSelection(whereClause, orderByClause, includeDeleted, PoemThread.transaction());
    }

    /**
   * All the objects in the table.
   *
   * @return An <TT>Enumeration</TT> of <TT>Persistent</TT>s, or, if the table
   *         was defined in the DSD under the name <TT><I>foo</I></TT>, of
   *         application-specialised subclasses <TT><I>Foo</I></TT>.  Note
   *         that no access checks are done at this stage: you may not be able
   *         to do anything with some of the object handles in the enumeration
   *         without provoking a <TT>PoemAccessException</TT>.  If the table
   *         has a <TT>deleted</TT> column, the objects flagged as deleted will
   *         be passed over.
   */
    public Enumeration selection() throws SQLPoemException {
        return selection((String) null, (String) null, false);
    }

    /**
   * A <TT>SELECT</TT>ion of objects from the table meeting given criteria.
   * This is one way to run a search against the database and return the
   * results as a series of typed POEM objects.
   *
   * @param whereClause         SQL <TT>SELECT</TT>ion criteria for the search:
   *                            the part that should appear after the
   *                            <TT>WHERE</TT> keyword
   *
   * @return An <TT>Enumeration</TT> of <TT>Persistent</TT>s, or, if the table
   *         was defined in the DSD under the name <TT><I>foo</I></TT>, of
   *         application-specialised subclasses <TT><I>Foo</I></TT>.  Note
   *         that no access checks are done at this stage: you may not be able
   *         to do anything with some of the object handles in the enumeration
   *         without provoking a <TT>PoemAccessException</TT>.  If the table
   *         has a <TT>deleted</TT> column, the objects flagged as deleted will
   *         be passed over.
   *
   * @see Column#selectionWhereEq(java.lang.Object)
   */
    public final Enumeration selection(String whereClause) throws SQLPoemException {
        return selection(whereClause, null, false);
    }

    public Persistent firstSelection(String whereClause) {
        Enumeration them = selection(whereClause);
        return them.hasMoreElements() ? (Persistent) them.nextElement() : null;
    }

    /**
   * A <TT>SELECT</TT>ion of objects from the table meeting given criteria,
   * possibly including those flagged as deleted.
   *
   * If the orderByClause is null, then the default order by clause is applied.
   * If the orderByClause is an empty string, ie "", then no ordering is 
   * applied.
   *
   * @param includeDeleted      whether to return objects flagged as deleted
   *                            (ignored if the table doesn't have a
   *                            <TT>deleted</TT> column)
   *
   * @see #selection(java.lang.String)
   */
    public Enumeration selection(String whereClause, String orderByClause, boolean includeDeleted) throws SQLPoemException {
        return objectsFromTroids(troidSelection(whereClause, orderByClause, includeDeleted));
    }

    /**
   * Return a selection of rows given arguments specifying a query.
   *
   * @see #selection(String, String, boolean)
   * @param criteria Represents selection criteria possibly on joined tables
   */
    public Enumeration selection(Persistent criteria, String orderByClause, boolean includeDeleted, boolean cannotSelect) throws SQLPoemException {
        return objectsFromTroids(troidSelection(criteria, orderByClause, includeDeleted, cannotSelect, null));
    }

    /**
   * Return an enumeration of objects given an enumeration of troids.
   */
    private Enumeration objectsFromTroids(Enumeration troids) {
        return new MappedEnumeration(troids) {

            public Object mapped(Object troid) {
                return getObject((Integer) troid);
            }
        };
    }

    /**
   * A <TT>SELECT</TT>ion of objects from the table meeting given criteria,
   * possibly including those flagged as deleted.  
   * The results are returned in 'pages'.
   *
   * If the orderByClause is null, then the default order by clause is applied.
   * If the orderByClause is an empty string, ie "" then no ordering is applied
   *
   * @param includeDeleted      whether to return objects flagged as deleted
   *                            (ignored if the table doesn't have a
   *                            <TT>deleted</TT> column)
   *
   * @see #selection(java.lang.String)
   */
    public PageEnumeration selection(String whereClause, String orderByClause, boolean includeDeleted, int pageStart, int pageSize) throws SQLPoemException {
        return new CountedDumbPageEnumeration(selection(whereClause, orderByClause, includeDeleted), pageStart, pageSize, cachedCount(whereClause, includeDeleted).count());
    }

    /**
   * Return pages of selected rows given arguments specifying a query.
   *
   * @see #selection(String, String, boolean, int, int)
   * @param criteria Represents selection criteria possibly on joined tables
   */
    public PageEnumeration selection(Persistent criteria, String orderByClause, boolean includeDeleted, boolean cannotSelect, int pageStart, int pageSize) throws SQLPoemException {
        return new CountedDumbPageEnumeration(selection(criteria, orderByClause, includeDeleted, cannotSelect), pageStart, pageSize, cachedCount(criteria, includeDeleted, cannotSelect).count());
    }

    String countSQL(String whereClause) {
        return countSQL(null, whereClause, false, true);
    }

    /**
   * Return an SQL statement to count rows put together from the arguments.
   * <p>
   * This is consistent with
   * {@link #selectionSQL(String, String, String, boolean, boolean)}.
   * <p>
   * Temporarily public for debugging purposes.
   *
   * @param fromClause Comma separated list of table names or null just this
   * table.
   */
    public String countSQL(String fromClause, String whereClause, boolean includeDeleted, boolean cannotSelect) {
        return selectOrCountSQL("count(*)", fromClause, whereClause, "", includeDeleted, cannotSelect);
    }

    /**
   * Return an SQL SELECT statement for selecting or counting rows.
   *
   * @param fromClause Comma separated list of table names or null for default.
   * @param orderByClause null for default, can be empty for counts
   */
    private String selectOrCountSQL(String selectClause, String fromClause, String whereClause, String orderByClause, boolean includeDeleted, boolean cannotSelect) {
        if (fromClause == null) {
            fromClause = quotedName();
        }
        String result = "SELECT " + selectClause + " FROM " + fromClause;
        whereClause = appendWhereClauseFilters(whereClause, includeDeleted, cannotSelect);
        if (whereClause.length() > 0) {
            result += " WHERE " + whereClause;
        }
        if (orderByClause == null) {
            orderByClause = defaultOrderByClause();
        }
        if (orderByClause.trim().length() > 0) {
            result += " ORDER BY " + orderByClause;
        }
        return result;
    }

    /**
   * Optionally add where clause expressions to filter out deleted/
   * unselectable rows and ensure an "empty" where clause is
   * indeed an empty string.
   * <p>
   * This is an attempt to treat "delete" and "can select" columns
   * consistently. But I believe that there is an important difference
   * in that unselectable rows must be considered when ensuring integrity.
   * So <code>cannotSelect</code> should default to <code>true</code>
   * and is only specified when selecting rows.
   * <p>
   * Despite the name this does not user a <code>StringBuffer</code>.
   * in the belief that the costs outweigh the benefits here.
   */
    private String appendWhereClauseFilters(String whereClause, boolean includeDeleted, boolean cannotSelect) {
        if (whereClause == null || whereClause.trim().length() == 0) {
            whereClause = "";
        } else {
            whereClause = "(" + whereClause + ")";
        }
        if (deletedColumn != null && !includeDeleted) {
            if (whereClause.length() > 0) {
                whereClause += " AND ";
            }
            whereClause += "NOT " + deletedColumn.getName();
        }
        if (!cannotSelect) {
            String s = whereCanSelectClause();
            if (s != null) {
                if (whereClause.length() > 0) {
                    whereClause += " AND ";
                }
                whereClause += s;
            }
        }
        return whereClause;
    }

    /**
   * Return a where clause suffix that filters out rows that cannot
   * be selected, or null.
   * <p>
   * By default the result is null unless there is a canselect column.
   * But in that case an SQL EXISTS() expression is used, which will
   * not yet work for all dbmses - sorry.
   *
   * @return null or a non-empty boolean SQL expression that can be
   * appended with AND to a parenthesised prefix.
   */
    private String whereCanSelectClause() {
        Column canSelect = canSelectColumn();
        AccessToken accessToken = PoemThread.sessionToken().accessToken;
        if (canSelect == null || accessToken instanceof RootAccessToken) {
            return null;
        } else if (accessToken instanceof User) {
            return "(" + canSelect.fullQuotedName() + " IS NULL OR EXISTS( SELECT 1 FROM " + quotedName() + ", " + database.getGroupCapabilityTable().quotedName() + ", " + database.getGroupMembershipTable().quotedName() + " WHERE " + database.getGroupMembershipTable().getUserColumn().fullQuotedName() + " = " + ((User) accessToken).getId() + " AND " + database.getGroupMembershipTable().getGroupColumn().fullQuotedName() + " = " + database.getGroupCapabilityTable().getGroupColumn().fullQuotedName() + " AND " + database.getGroupCapabilityTable().getCapabilityColumn().fullQuotedName() + " = " + canSelect.fullQuotedName() + "))";
        } else {
            return canSelect.fullQuotedName() + " IS NULL";
        }
    }

    public int count(String whereClause, boolean includeDeleted, boolean cannotSelect) throws SQLPoemException {
        return count(appendWhereClauseFilters(whereClause, includeDeleted, cannotSelect));
    }

    public int count(String whereClause, boolean includeDeleted) throws SQLPoemException {
        return count(whereClause, includeDeleted, true);
    }

    public int count(String whereClause) throws SQLPoemException {
        String sql = countSQL(whereClause);
        try {
            PoemTransaction transaction = PoemThread.transaction();
            Connection connection;
            if (transaction == null) connection = getDatabase().getCommittedConnection(); else {
                transaction.writeDown();
                connection = transaction.getConnection();
            }
            ResultSet rs = connection.createStatement().executeQuery(sql);
            if (database.logSQL()) database.log(new SQLLogEvent(sql));
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            return count;
        } catch (SQLException e) {
            throw new ExecutingSQLPoemException(sql, e);
        }
    }

    public boolean exists(String whereClause) throws SQLPoemException {
        return count(whereClause) > 0;
    }

    /**
   * Append an SQL logical expression to the given buffer to match rows
   * according to criteria represented by the given object.
   * <p>
   * This default selects rows for which the non-null fields in the
   * given object match, but subtypes may add other criteria.
   * <p>
   * The column names are now qualified with the table name so that
   * subtypes can append elements of a join but there is no filtering
   * by canselect columns.
   * 
   * @todo Add mechanism for searching for Nulls (that would be query
   * constructs as per SQL parse tree, but efferent not afferent JimW)
   * @todo Decide whether this and similar should be here or in
   * {@link Persistent} then deprecate etc.
   *
   * @see #notifyColumnInfo(ColumnInfo)
   * @see #clearColumnInfoCaches()
   */
    public void appendWhereClause(StringBuffer clause, Persistent persistent) {
        Column[] columns = this.columns;
        boolean hadOne = false;
        for (int c = 0; c < columns.length; ++c) {
            Column column = columns[c];
            Object raw = column.getRaw_unsafe(persistent);
            if (raw != null) {
                if (hadOne) clause.append(" AND "); else hadOne = true;
                String columnSQL = column.fullQuotedName();
                if (column.getType() instanceof StringPoemType) {
                    clause.append(dbms().caseInsensitiveRegExpSQL(columnSQL, column.getSQLType().quotedRaw(raw)));
                } else {
                    clause.append(columnSQL);
                    clause.append(" = ");
                    clause.append(column.getSQLType().quotedRaw(raw));
                }
            }
        }
    }

    /**
   * Return an SQL WHERE clause to select rows that match the non-null
   * fields of the given object.
   * <p>
   * This does not filter out any rows with a capability the user
   * does not have in a canselect column, nor did it ever filter
   * out rows deleted according to a "deleted" column.
   * But the caller usually gets a second chance to do both.
   */
    public String whereClause(Persistent criteria) {
        return whereClause(criteria, true, true);
    }

    /**
   * Return an SQL WHERE clause to select rows using the given object
   * as a selection criteria and optionally deleted rows or those
   * included rows the user is not capable of selecting.
   * <p>
   * This is currently implemented in terms of
   * {@link Table#appendWhereClause(StringBuffer, Persistent)}.
   */
    public String whereClause(Persistent criteria, boolean includeDeleted, boolean cannotSelect) {
        StringBuffer clause = new StringBuffer();
        appendWhereClause(clause, criteria);
        return appendWhereClauseFilters(clause.toString(), includeDeleted, cannotSelect);
    }

    /**
   * @see #cnfWhereClause(Enumeration, boolean, boolean)
   * @see #whereClause(Persistent)
   */
    public String cnfWhereClause(Enumeration persistents) {
        return cnfWhereClause(persistents, false, true);
    }

    /**
   * @todo Document this, particularly "cnf" and the fact that it does
   * not work if any of the persistents produces an empty where clause.
   */
    public String cnfWhereClause(Enumeration persistents, boolean includeDeleted, boolean cannotSelect) {
        StringBuffer clause = new StringBuffer();
        boolean hadOne = false;
        while (persistents.hasMoreElements()) {
            if (hadOne) clause.append(" OR "); else hadOne = true;
            clause.append("(");
            appendWhereClause(clause, (Persistent) persistents.nextElement());
            clause.append(")");
        }
        return appendWhereClauseFilters(clause.toString(), includeDeleted, cannotSelect);
    }

    public boolean exists(Persistent persistent) {
        return exists(whereClause(persistent));
    }

    /**
   * All the objects in the table which refer to a given object.  If none of
   * the table's columns are reference columns, the <TT>Enumeration</TT>
   * returned will obviously be empty.  This is used by
   * <TT>Persistent.delete()</TT> to determine whether deleting an object would
   * destroy the integrity of any references.  It is not guaranteed to be
   * quick to execute!
   *
   * @return an <TT>Enumeration</TT> of <TT>Persistent</TT>s
   */
    public Enumeration referencesTo(final Persistent object) {
        return new FlattenedEnumeration(new MappedEnumeration(columns()) {

            public Object mapped(Object column) {
                return ((Column) column).referencesTo(object);
            }
        });
    }

    public Enumeration referencesTo(final Table table) {
        return new FilteredEnumeration(columns()) {

            public boolean isIncluded(Object column) {
                PoemType type = ((Column) column).getType();
                return type instanceof ReferencePoemType && ((ReferencePoemType) type).targetTable() == table;
            }
        };
    }

    private void validate(Persistent persistent) throws FieldContentsPoemException {
        for (int c = 0; c < columns.length; ++c) {
            Column column = columns[c];
            try {
                column.getType().assertValidRaw(column.getRaw_unsafe(persistent));
            } catch (Exception e) {
                throw new FieldContentsPoemException(column, e);
            }
        }
    }

    private int nextTroid = -1;

    protected synchronized Integer troidFor(Persistent persistent) {
        if (nextTroid == -1) throw new PoemBugPoemException();
        return new Integer(nextTroid++);
    }

    /**
  * Thrown when an unauthorised attempt to create an object is made.
  */
    public static class AccessibleCreationException extends AccessPoemException {

        public AccessibleCreationException(AccessPoemException e) {
            super(e);
        }

        public String getActionDescription() {
            return "create an object which can only be accessed by users with the " + "capability " + capability;
        }

        public String getMessage() {
            return "You cannot " + getActionDescription() + " since your access " + "token " + token + " doesn't confer that capability";
        }
    }

    /**
   * Write a new row containing the given object.
   * <p>
   * The given object will be assigned the next troid and its internal
   * state will also be modified.
   *
   * @exception InitialisationPoemException The object failed validation
   *   (currently one of its field values failed).
   */
    public void create(Persistent persistent) throws AccessPoemException, ValidationPoemException, InitialisationPoemException {
        SessionToken sessionToken = PoemThread.sessionToken();
        if (persistent.getTable() == null) persistent.setTable(this, null);
        persistent.assertCanCreate(sessionToken.accessToken);
        claim(persistent, troidFor(persistent));
        persistent.setStatusNonexistent();
        try {
            validate(persistent);
        } catch (Exception e) {
            throw new InitialisationPoemException(this, e);
        }
        synchronized (cache) {
            persistent.dirty = true;
            writeDown(sessionToken.transaction, persistent);
            persistent.readLock(sessionToken.transaction);
            cache.put(persistent.troid(), persistent);
        }
        notifyTouched(sessionToken.transaction, persistent);
    }

    /**
   * Create a new object (record) in the table.
   *
   * @param initialiser         A piece of code for setting the new object's
   *                            initial values.  You'll probably want to define
   *                            it as an anonymous class.
   *
   * @return A <TT>Persistent</TT> representing the new object, or, if the
   *         table was defined in the DSD under the name <TT><I>foo</I></TT>,
   *         an application-specialised subclass <TT><I>Foo</I></TT> of
   *         <TT>Persistent</TT>.
   *
   * @exception CreationAccessPoemException
   *                if the calling thread's <TT>AccessToken</TT> doesn't allow
   *                you to create records in the table
   * @exception AccessPoemException
   *                if <TT>initialiser</TT> provokes one during its work (which
   *                is unlikely, since POEM's standard checks are disabled
   *                while it runs)
   * @exception ValidationPoemException
   *                if <TT>initialiser</TT> provokes one during its work
   * @exception InitialisationPoemException
   *                if the object is left by <TT>initialiser</TT> in a state in
   *                which not all of its fields have legal values, or in which
   *                the calling thread would not be allowed write access to the
   *                object under its <TT>AccessToken</TT>---<I>i.e.</I> you
   *                can't create objects you wouldn't be allowed to write to.
   *
   * @see Initialiser#init(org.melati.poem.Persistent)
   * @see PoemThread#accessToken()
   * @see #getCanCreate()
   */
    public Persistent create(Initialiser initialiser) throws AccessPoemException, ValidationPoemException, InitialisationPoemException {
        Persistent persistent = newPersistent();
        initialiser.init(persistent);
        create(persistent);
        return persistent;
    }

    private void claim(Persistent persistent, Integer troid) {
        if (cache.get(troid) != null) throw new DuplicateTroidPoemException(this, troid);
        if (persistent.troid() != null) throw new DoubleCreatePoemException(persistent);
        persistent.setTable(this, troid);
        troidColumn.setRaw_unsafe(persistent, troid);
        if (deletedColumn != null) deletedColumn.setRaw_unsafe(persistent, Boolean.FALSE);
    }

    /**
   * A freshly minted <TT>Persistent</TT> object for the table.
   */
    public Persistent newPersistent() {
        Persistent it = _newPersistent();
        it.setTable(this, null);
        return it;
    }

    /**
   * A freshly minted, and uninitialised, <TT>Persistent</TT> object for the
   * table.  You don't ever have to call this and there is no point in doing so
   * This method is overridden in application-specialised <TT>Table</TT>
   * subclasses derived from the Data Structure Definition.
   */
    protected Persistent _newPersistent() {
        return new Persistent();
    }

    public void delete_unsafe(String whereClause) {
        serial.increment(PoemThread.transaction());
        getDatabase().sqlUpdate("DELETE FROM " + quotedName + " WHERE " + whereClause);
        uncacheContents();
    }

    /**
   * The number of `extra' (non-DSD-defined) columns in the table.
   */
    int extrasCount() {
        return extrasIndex;
    }

    /**
   * The capability required for reading records from the table, unless
   * overridden in the record itself.  This simply comes from the table's
   * record in the <TT>tableinfo</TT> table.
   *
   * @see Persistent#getCanRead()
   */
    public final Capability getDefaultCanRead() {
        return info == null ? null : info.getDefaultcanread();
    }

    /**
   * The capability required for updating records in the table, unless
   * overridden in the record itself.  This simply comes from the table's
   * record in the <TT>tableinfo</TT> table.
   *
   * @see Persistent#getCanWrite()
   */
    public final Capability getDefaultCanWrite() {
        return info == null ? null : info.getDefaultcanwrite();
    }

    public final Capability getDefaultCanDelete() {
        return info == null ? null : info.getDefaultcandelete();
    }

    /**
   * The capability required for creating records in the table.  This simply
   * comes from the table's record in the <TT>tableinfo</TT> table.
   *
   * @see #create(org.melati.poem.Initialiser)
   */
    public final Capability getCanCreate() {
        return info == null ? null : info.getCancreate();
    }

    final Column canReadColumn() {
        return canReadColumn == null ? canSelectColumn() : canReadColumn;
    }

    final Column canSelectColumn() {
        return canSelectColumn;
    }

    final Column canWriteColumn() {
        return canWriteColumn;
    }

    final Column canDeleteColumn() {
        return canDeleteColumn;
    }

    public Column addColumnAndCommit(ColumnInfo info) throws PoemException {
        Column column = ExtraColumn.from(this, info, extrasIndex++, DefinitionSource.runtime);
        column.setColumnInfo(info);
        defineColumn(column, false);
        database.beginStructuralModification();
        dbAddColumn(column);
        try {
            synchronized (cache) {
                uncacheContents();
                transactionStuffs.invalidate();
                defineColumn(column, true);
            }
            PoemThread.commit();
        } finally {
            database.endStructuralModification();
        }
        return column;
    }

    /**
   * A concise string to stand in for the table.  The table's name and a
   * description of where it was defined (the DSD, the metadata tables or the
   * JDBC metadata).
   */
    public String toString() {
        return getName() + " (from " + definitionSource + ")";
    }

    /**
   * Print some diagnostic information about the contents and consistency of
   * POEM's cache for this table to stderr.
   */
    public void dumpCacheAnalysis() {
        System.err.println("\n-------- Analysis of " + name + "'s cache\n");
        cache.dumpAnalysis();
    }

    /**
   * Print information about the structure of the database to stdout.
   */
    public void dump() {
        System.out.println("=== table " + name + " (tableinfo id " + tableInfoID() + ")");
        for (int c = 0; c < columns.length; ++c) columns[c].dump();
    }

    /**
   * A mechanism for caching a selection of records.
   * 
   * @param whereClause raw SQL selection clause appropriate for this DBMS
   * @param orderByClause which field to order by or null
   * @return the results
   */
    public CachedSelection cachedSelection(String whereClause, String orderByClause) {
        String key = whereClause + "/" + orderByClause;
        CachedSelection them = (CachedSelection) cachedSelections.get(key);
        if (them == null || them.outOfDate()) {
            CachedSelection newThem = new CachedSelection(this, whereClause, orderByClause);
            synchronized (cachedSelections) {
                them = (CachedSelection) cachedSelections.get(key);
                if (them == null) cachedSelections.put(key, them = newThem);
            }
        }
        return them;
    }

    public CachedCount cachedCount(String whereClause, boolean includeDeleted) {
        return cachedCount(whereClause, includeDeleted, true);
    }

    public CachedCount cachedCount(String whereClause, boolean includeDeleted, boolean cannotSelect) {
        return cachedCount(appendWhereClauseFilters(whereClause, includeDeleted, cannotSelect));
    }

    public CachedCount cachedCount(Persistent criteria, boolean includeDeleted, boolean cannotSelect) {
        return cachedCount(whereClause(criteria, includeDeleted, cannotSelect), criteria);
    }

    public CachedCount cachedCount(String whereClause) {
        return cachedCount(whereClause, (Persistent) null);
    }

    private CachedCount cachedCount(String whereClause, Persistent criteria) {
        CachedCount it = (CachedCount) cachedCounts.get(whereClause);
        if (it == null) {
            CachedCount newIt;
            if (criteria == null) {
                newIt = new CachedCount(this, whereClause);
            } else {
                newIt = new CachedCount(criteria, false, true);
            }
            synchronized (cachedCounts) {
                it = (CachedCount) cachedCounts.get(whereClause);
                if (it == null) cachedCounts.put(whereClause, it = newIt);
            }
        }
        return it;
    }

    public CachedExists cachedExists(String whereClause) {
        CachedExists it = (CachedExists) cachedExists.get(whereClause);
        if (it == null) {
            CachedExists newIt = new CachedExists(this, whereClause);
            synchronized (cachedExists) {
                it = (CachedExists) cachedExists.get(whereClause);
                if (it == null) cachedExists.put(whereClause, it = newIt);
            }
        }
        return it;
    }

    public RestrictedReferencePoemType cachedSelectionType(String whereClause, String orderByClause, boolean nullable) {
        return new RestrictedReferencePoemType(cachedSelection(whereClause, orderByClause), nullable);
    }

    /**
   * Make up a <TT>Field</TT> object whose possible values are a selected
   * subset of the records in the table.  You can make a "dropdown" offering a
   * choice of your green customers by putting this in your handler
   *
   * <BLOCKQUOTE><PRE>
   * context.put("greens",
   *             melati.getDatabase().getCustomerTable().cachedSelectionField(
   *                 "colour = 'green'", null, true, null, "greens"));
   * </PRE></BLOCKQUOTE>
   *
   * and this in your template
   *
   * <BLOCKQUOTE><PRE>
   *   Select a customer: $ml.input($greens)
   * </PRE></BLOCKQUOTE>
   *
   * The list of member records is implicitly cached---permanently, and however
   * big it turns out to be.  So don't go mad with this.  It is recomputed on
   * demand if the contents of the table are changed.  The <TT>whereClause</TT>
   * and <TT>orderByClause</TT> you pass in are checked to see if you have
   * asked for the same list before, so however many times you call this
   * method, you should only trigger actual <TT>SELECT</TT>s when the table
   * contents have changed.  The list is also transaction-safe, in that it will
   * always reflect the state of affairs within your transaction even if you
   * haven't done a commit.
   *
   * @param whereClause         an SQL expression (the bit after the
   *                            <TT>SELECT</TT> ... <TT>WHERE</TT>) for picking
   *                            out the records you want
   *
   * @param orderByClause       a comma-separated list of column names which
   *                            determine the order in which the records are
   *                            presented; if this is <TT>null</TT>, the
   *                            <TT>displayorderpriority</TT> attributes of the
   *                            table's columns determine the order
   *
   * @param nullable            whether to allow a blank <TT>NULL</TT> option
   *                            as the first possibility
   *
   * @param selectedTroid       the troid of the record to which the
   *                            <TT>SELECT</TT> field should initially be set
   *
   * @param name                the HTML name attribute of the field,
   *                            <I>i.e.</I>
   *                            <TT>&lt;SELECT NAME=<I>name</I>&gt;</TT>
   */
    public Field cachedSelectionField(String whereClause, String orderByClause, boolean nullable, Integer selectedTroid, String name) {
        return new Field(selectedTroid, new BaseFieldAttributes(name, cachedSelectionType(whereClause, orderByClause, nullable)));
    }

    /**
   * Don't call this.  Columns should be defined either in the DSD (in which
   * case the boilerplate code generated by the preprocessor will call this
   * method) or directly in the RDBMS (in which case the initialisation code
   * will).
   */
    protected synchronized void defineColumn(Column column, boolean reallyDoIt) throws DuplicateColumnNamePoemException, DuplicateTroidColumnPoemException, DuplicateDeletedColumnPoemException {
        if (column.getTable() != this) throw new ColumnInUsePoemException(this, column);
        if (columnsByName.get(column.getName()) != null) throw new DuplicateColumnNamePoemException(this, column);
        if (column.isTroidColumn()) {
            if (troidColumn != null) throw new DuplicateTroidColumnPoemException(this, column);
            if (reallyDoIt) troidColumn = column;
        } else if (column.isDeletedColumn()) {
            if (deletedColumn != null) throw new DuplicateDeletedColumnPoemException(this, column);
            if (reallyDoIt) deletedColumn = column;
        } else {
            if (reallyDoIt) {
                PoemType type = column.getType();
                if (type instanceof ReferencePoemType && ((ReferencePoemType) type).targetTable() == database.getCapabilityTable()) {
                    if (column.getName().equals("canread")) canReadColumn = column; else if (column.getName().equals("canwrite")) canWriteColumn = column; else if (column.getName().equals("candelete")) canDeleteColumn = column; else if (column.getName().equals("canselect")) canSelectColumn = column;
                }
            }
        }
        if (reallyDoIt) {
            column.setTable(this);
            columns = (Column[]) ArrayUtils.added(columns, column);
            columnsByName.put(column.getName(), column);
        }
    }

    protected final void defineColumn(Column column) throws DuplicateColumnNamePoemException, DuplicateTroidColumnPoemException, DuplicateDeletedColumnPoemException {
        defineColumn(column, true);
    }

    private void _defineColumn(Column column) {
        try {
            defineColumn(column);
        } catch (DuplicateColumnNamePoemException e) {
            throw new UnexpectedExceptionPoemException(e);
        } catch (DuplicateTroidColumnPoemException e) {
            throw new UnexpectedExceptionPoemException(e);
        }
    }

    int extrasIndex = 0;

    void setTableInfo(TableInfo tableInfo) {
        info = tableInfo;
        rememberAllTroids(tableInfo.getSeqcached().booleanValue());
        setCacheLimit(tableInfo.getCachelimit());
    }

    public TableInfo getTableInfo() {
        return info;
    }

    /**
   * The `factory-default' display name for the table.  By default this is the
   * table's programmatic name, capitalised.  Application-specialised tables
   * override this to return any <TT>(displayname = </TT>...<TT>)</TT> provided
   * in the DSD.  This is only ever used at startup time when creating
   * <TT>columninfo</TT> records for tables that don't have them.
   */
    protected String defaultDisplayName() {
        return StringUtils.capitalised(getName());
    }

    protected int defaultDisplayOrder() {
        return 100;
    }

    /**
   * The `factory-default' description for the table, or <TT>null</TT> if it
   * doesn't have one.  Application-specialised tables override this to return
   * any <TT>(description = </TT>...<TT>)</TT> provided in the DSD.  This is
   * only ever used at startup time when creating <TT>columninfo</TT> records
   * for tables that don't have them.
   */
    protected String defaultDescription() {
        return null;
    }

    protected Integer defaultCacheLimit() {
        return new Integer(CACHE_LIMIT_DEFAULT);
    }

    protected boolean defaultRememberAllTroids() {
        return false;
    }

    protected String defaultCategory() {
        return "Normal";
    }

    TableInfo defaultTableInfo() {
        return new TableInfo(getName(), defaultDisplayName(), defaultDisplayOrder(), defaultDescription(), defaultCacheLimit(), defaultRememberAllTroids(), database.getTableCategoryTable().ensure(defaultCategory()));
    }

    void createTableInfo() throws PoemException {
        if (info == null) {
            info = defaultTableInfo();
            getDatabase().getTableInfoTable().create(info);
            setTableInfo(info);
        }
    }

    synchronized void unifyWithColumnInfo() throws PoemException {
        if (info == null) throw new PoemBugPoemException("Get the initialisation order right ...");
        for (Enumeration ci = database.getColumnInfoTable().getTableinfoColumn().selectionWhereEq(info.troid()); ci.hasMoreElements(); ) {
            ColumnInfo columnInfo = (ColumnInfo) ci.nextElement();
            Column column = (Column) columnsByName.get(columnInfo.getName());
            if (column == null) {
                column = ExtraColumn.from(this, columnInfo, extrasIndex++, DefinitionSource.infoTables);
                _defineColumn(column);
            }
            column.setColumnInfo(columnInfo);
        }
        for (Enumeration c = columns(); c.hasMoreElements(); ) ((Column) c.nextElement()).createColumnInfo();
    }

    public synchronized void unifyWithDB(ResultSet colDescs) throws SQLException, PoemException {
        Hashtable dbColumns = new Hashtable();
        int dbIndex = 0;
        if (colDescs != null) {
            for (; colDescs.next(); ++dbIndex) {
                String colName = colDescs.getString("COLUMN_NAME");
                Column column = (Column) columnsByName.get(dbms().melatiName(colName));
                if (column == null) {
                    SQLPoemType colType = database.defaultPoemTypeOfColumnMetaData(colDescs);
                    if (troidColumn == null && colName.equals("id") && dbms().canRepresent(colType, TroidPoemType.it) != null) colType = TroidPoemType.it;
                    if (deletedColumn == null && colName.equals("deleted") && dbms().canRepresent(colType, DeletedPoemType.it) != null) colType = DeletedPoemType.it;
                    column = new ExtraColumn(this, dbms().melatiName(colDescs.getString("COLUMN_NAME")), colType, DefinitionSource.sqlMetaData, extrasIndex++);
                    _defineColumn(column);
                    if (info != null) column.createColumnInfo();
                } else {
                    column.assertMatches(colDescs);
                }
                dbColumns.put(column, Boolean.TRUE);
            }
        }
        if (dbIndex == 0) {
            dbCreateTable();
        } else {
            for (int c = 0; c < columns.length; ++c) {
                if (dbColumns.get(columns[c]) == null) dbAddColumn(columns[c]);
            }
        }
        if (troidColumn == null) throw new NoTroidColumnException(this);
        if (info != null) {
            Hashtable dbHasIndexForColumn = new Hashtable();
            ResultSet index = getDatabase().getCommittedConnection().getMetaData().getIndexInfo(null, dbms().getSchema(), dbms().getQuotedName(dbms().unreservedName(getName())), false, true);
            while (index.next()) {
                try {
                    String columnName = dbms().melatiName(index.getString("COLUMN_NAME"));
                    if (columnName != null) {
                        Column column = getColumn(columnName);
                        column.unifyWithIndex(index);
                        dbHasIndexForColumn.put(column, Boolean.TRUE);
                    }
                } catch (NoSuchColumnPoemException e) {
                    throw new UnexpectedExceptionPoemException(e);
                }
            }
            for (int c = 0; c < columns.length; ++c) {
                if (dbHasIndexForColumn.get(columns[c]) != Boolean.TRUE) dbCreateIndex(columns[c]);
            }
        }
        if (PoemThread.inSession()) PoemThread.writeDown();
        String sql = "SELECT " + troidColumn.quotedName() + " FROM " + quotedName() + " ORDER BY " + troidColumn.quotedName() + " DESC";
        try {
            ResultSet maxTroid = getDatabase().getCommittedConnection().createStatement().executeQuery(sql);
            if (database.logSQL()) database.log(new SQLLogEvent(sql));
            if (maxTroid.next()) nextTroid = maxTroid.getInt(1) + 1; else nextTroid = 0;
        } catch (SQLException e) {
            throw new SQLSeriousPoemException(e);
        }
    }

    protected void init() {
    }

    /**
   * Ensure tables can be used as hashtable keys.
   * <p>
   * {@link Persistent#hashCode()} is defined in terms of this
   * but not used at the time of writing.
   */
    public final int hashCode() {
        return name.hashCode();
    }
}
