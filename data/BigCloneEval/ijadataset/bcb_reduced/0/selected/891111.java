package blueprint4j.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import blueprint4j.db.annotations.EditRoles;
import blueprint4j.report.db.FieldReportGroup;
import blueprint4j.report.db.VectorFieldReportGroup;
import blueprint4j.utils.BindException;
import blueprint4j.utils.BindFieldInterface;
import blueprint4j.utils.Bindable;
import blueprint4j.utils.BindableProperties;
import blueprint4j.utils.Log;
import blueprint4j.utils.Utils;
import blueprint4j.utils.VectorBindable;
import blueprint4j.utils.VectorString;

public abstract class Entity extends Object implements Bindable, Serializable {

    private static final HashSet updated_entities = new HashSet();

    private static final SimpleDateFormat datetime_format = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd");

    private static final SimpleDateFormat time_format = new SimpleDateFormat("HHmmss");

    private static final Hashtable insert_field_strings = new Hashtable();

    private static final Hashtable entity_new_listeners = new Hashtable();

    private static final Hashtable entity_delete_listeners = new Hashtable();

    private static final Hashtable entity_change_listeners = new Hashtable();

    private static Object update_should_sync = new Object();

    private static Object update_do_sync = new Object();

    private String table_name = null;

    private String version = null;

    private VectorField fields = new VectorField();

    private VectorField name_fields = new VectorField(), name_fields_recursive = null;

    private VectorFieldAction field_action = new VectorFieldAction();

    private Index.VectorIndex indexes = new Index.VectorIndex();

    private FieldRename.VectorFieldRename rename_field = new FieldRename.VectorFieldRename();

    private Maintenance.VectorMaintenance maintenances = new Maintenance.VectorMaintenance();

    private java.lang.ref.WeakReference connection = new java.lang.ref.WeakReference(null);

    private transient ResultSet result_set = null;

    private boolean loaded = false, keep_connection_alive = false;

    private long last_time_connection_used = 0;

    private Entity next = null;

    private static boolean started = false;

    private Hashtable affiliated_entities = new Hashtable();

    private FieldGroup current_field_group = null;

    private FieldReportGroup current_field_report_group = null;

    private VectorFieldGroup field_groups = new VectorFieldGroup();

    private VectorFieldReportGroup field_report_groups = new VectorFieldReportGroup();

    private List<FieldGroupBar> fieldGroupBars = new ArrayList<FieldGroupBar>();

    private FieldGroupBar current_field_group_bar = null;

    private int current_field_group_bar_count = 0;

    private EntityChild.VectorEntityChild children = new EntityChild.VectorEntityChild();

    public FieldId id = null;

    public static final int ENTITY_CREATE = 0x2;

    public static final int ENTITY_DELETE = 0x4;

    public static final int ENTITY_SEARCH = 0x8;

    public static final int ENTITY_EDIT = 0x10;

    public static final int ENTITY_SORT_ASC = 0x20;

    public static final int ENTITY_SORT_DESC = 0x40;

    public static final int ENTITY_STORAGE_TRANSACTIONAL = 0x80;

    public static final int ENTITY_LOAD_ON_DEMAND_FIELD = 0x100;

    private int entity_type = (ENTITY_CREATE | ENTITY_DELETE | ENTITY_SEARCH | ENTITY_EDIT);

    private boolean STORAGE_TRANSACTIONAL = false;

    private static boolean DEBUG = true;

    private boolean message_processor_alive = false;

    /**
     * Entity Flags for Visual editing
     * ENTITY_CREATE, may editors create new entities
     * ENTITY_DELETE, may editors delete entities
     * ENTITY_SEARCH, may editors search entities
     * ENTITY_EDIT, may editors edit entities
     */
    public Entity(int entity_type) {
        this.entity_type = entity_type;
    }

    public Entity() {
        this(ENTITY_CREATE | ENTITY_DELETE | ENTITY_SEARCH | ENTITY_EDIT | ENTITY_SORT_DESC);
    }

    public Entity(DBConnection p_connection, int entity_type) throws DataException, SQLException {
        this(entity_type);
        connection = new java.lang.ref.WeakReference(p_connection);
        updateEntity();
    }

    public Entity(DBConnection p_connection) throws DataException, SQLException {
        this(p_connection, ENTITY_CREATE | ENTITY_DELETE | ENTITY_SEARCH | ENTITY_EDIT);
    }

    public void setEntityType(int entity_type) {
        this.entity_type = entity_type;
    }

    public boolean mayCreate() {
        return ((entity_type & ENTITY_CREATE) == ENTITY_CREATE);
    }

    public boolean mayDelete() {
        return ((entity_type & ENTITY_DELETE) == ENTITY_DELETE);
    }

    public boolean maySearch() {
        return ((entity_type & ENTITY_SEARCH) == ENTITY_SEARCH);
    }

    public boolean mayEdit() {
        return ((entity_type & ENTITY_EDIT) == ENTITY_EDIT);
    }

    public boolean shouldSort() {
        return (((entity_type & ENTITY_SORT_ASC) == ENTITY_SORT_ASC) || (entity_type & ENTITY_SORT_DESC) == ENTITY_SORT_DESC);
    }

    public boolean isStorageTranscational() {
        return ((entity_type & ENTITY_STORAGE_TRANSACTIONAL) == ENTITY_STORAGE_TRANSACTIONAL);
    }

    public boolean loadOnDemandField() {
        return ((entity_type & ENTITY_LOAD_ON_DEMAND_FIELD) == ENTITY_LOAD_ON_DEMAND_FIELD);
    }

    public Entity findByOrder(String search_string) throws SQLException {
        if (getDataBaseUtils() instanceof DataBaseUtilsFireBird) {
            search_string = Utils.replaceAll(search_string, "= null", "is null");
        }
        if ((entity_type & ENTITY_SORT_ASC) == ENTITY_SORT_ASC) {
            search_string += " order by " + id.getName() + " asc";
        } else {
            search_string += " order by " + id.getName() + " desc";
        }
        return rebuildFromResultSet("select " + getSelectFields() + " from " + table_name + " where " + search_string);
    }

    public void setFieldId(FieldId id_field) {
        if (id != null) {
            Log.critical.out(new Exception("Entity [" + getTableName() + "] already has a FieldId [" + id.getName() + "]. Trying to set a new one[" + id_field.getName() + "]"));
        }
        id = id_field;
    }

    public FieldId getFieldId() {
        return id;
    }

    public void addChild(EntityChild child) {
        children.add(child);
    }

    public EntityChild getChildByForeignKeyNameAndTable(String name, String table) {
        for (int t = 0; t < children.size(); t++) {
            if (children.get(t).getFieldForeignKey().getName().equals(name) && children.get(t).getFieldForeignKey().getEntity().getTableName().equals(table)) {
                return children.get(t);
            }
        }
        return null;
    }

    public EntityChild.VectorEntityChild getChildren() {
        return children;
    }

    void setFieldGroup(FieldGroup group) {
        if (current_field_group_bar != null && current_field_group_bar_count > 0) {
            current_field_group_bar.add(group);
        }
        field_groups.add(group);
        current_field_group = group;
    }

    void setFieldGroupBar(FieldGroupBar groupBar) {
        fieldGroupBars.add(groupBar);
        current_field_group_bar = groupBar;
        current_field_group_bar_count = groupBar.getColumns();
    }

    public void setFieldReportGroup(FieldReportGroup group) {
        field_report_groups.add(group);
        current_field_report_group = group;
    }

    public List<FieldGroupBar> getFieldGroupBars() {
        return fieldGroupBars;
    }

    public VectorFieldGroup getFieldGroups() {
        return field_groups;
    }

    public VectorFieldReportGroup getFieldReportGroups() {
        return field_report_groups;
    }

    public void updateEntity() {
        if (!updated_entities.contains(this.getClass())) {
            updated_entities.add(this.getClass());
            try {
                boolean may_update = false;
                Entity entity = getNewInstance();
                synchronized (update_do_sync) {
                    if (getConnection().getDataBaseUtils().mayUpdate(getConnection(), entity.getTableName())) {
                        getConnection().getDataBaseUtils().updateEntity(getConnection(), entity, false);
                        entity.setConnection(getConnection());
                        entity.startMaintenance();
                    }
                }
            } catch (Throwable exception) {
                exception.printStackTrace();
                Log.critical.out("ERROR INSTATIATING ENTITY [" + this.getClass() + "]. ALL ENTITIES MUST HAVE AN EMPTY SUPER CONSTRUCTOR()", exception);
            }
        }
    }

    /**
     * This will create a exact duplicate table of current
     * with the given name
     */
    public void duplicateTable(String tabname) throws SQLException {
        Entity entity = this.getNewInstance();
        entity.setTableName(tabname);
        getConnection().getDataBaseUtils().updateEntity(getConnection(), entity, false);
    }

    public void setConnection(DBConnection connection) throws DataException {
        setNewConnection(connection);
    }

    public void setNewConnection(DBConnection dbcon) {
        this.connection = new java.lang.ref.WeakReference(dbcon);
    }

    public void setNewConnection() throws SQLException {
        setNewConnection(getConnection().getNewInstance());
    }

    public void closeConnection() {
        try {
            getConnection().close();
        } catch (SQLException sqle) {
            Log.debug.out(sqle);
        }
    }

    public abstract Entity getNewInstance();

    private Entity getNewEntityInstance() throws DataException {
        try {
            return (Entity) Class.forName(getClass().getName()).getConstructor(new Class[] { getConnection().getClass() }).newInstance(new Object[] { ((Object) getConnection()) });
        } catch (Throwable th) {
            th.printStackTrace();
            Log.debug.out("Entity getNewEntityInstance[" + getClass().getName() + "] failed. Make sure that class has constructor that accepts a DBConnection");
            throw new DataException(th);
        }
    }

    private Map<String, Annotation[]> annotationMap = null;

    private Map<String, Set<String>> fieldEditRolesMap = new HashMap<String, Set<String>>();

    public void processAnnotation(String fieldName, Annotation annotation) {
        if (annotation instanceof EditRoles) {
            EditRoles editRoles = (EditRoles) annotation;
            Set<String> roles = new HashSet<String>();
            for (String role : editRoles.roles().split(",")) {
                roles.add(role);
            }
            fieldEditRolesMap.put(fieldName, roles);
        }
    }

    public void addField(BindFieldInterface p_field) {
        if (annotationMap == null) {
            annotationMap = new HashMap<String, Annotation[]>();
            java.lang.reflect.Field[] fields = getClass().getFields();
            for (java.lang.reflect.Field f : fields) {
                try {
                    if (f.get(this) instanceof Field) {
                        Field field = (Field) f.get(this);
                        Annotation[] annotations = f.getAnnotations();
                        annotationMap.put(field.getName(), annotations);
                        for (Annotation annotation : annotations) {
                            processAnnotation(field.getName(), annotation);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Log.critical.out(e);
                } catch (IllegalAccessException e) {
                    Log.critical.out(e);
                }
            }
        }
        if (!((Field) p_field).isFieldVirtual()) {
            fields.add((Field) p_field);
            if (((Field) p_field).isNameField()) {
                name_fields.add(((Field) p_field));
            }
        }
        if (current_field_group != null) {
            current_field_group.add((Field) p_field);
        }
    }

    public void addFieldAction(FieldAction field_action) {
        this.field_action.add(field_action);
    }

    public void disableFieldGroup() {
        current_field_group = null;
    }

    public Field getFieldByName(String fieldname) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals(fieldname)) {
                return fields.get(i);
            }
        }
        return null;
    }

    public void addIndex(Index index) {
        indexes.add(index);
    }

    public void addFieldRename(FieldRename field) {
        rename_field.add(field);
    }

    public void addMaintenance(Maintenance maintenance) {
        maintenances.add(maintenance);
    }

    private void startMaintenance() {
        maintenances.start();
    }

    public BindFieldInterface[] getBindFields() throws BindException {
        return (BindFieldInterface[]) fields.toArray();
    }

    public void setTableName(String p_table_name) {
        table_name = p_table_name;
    }

    protected void load(ResultSet p_result_set) throws SQLException, DataException {
        result_set = p_result_set;
        for (int i = 0; i < fields.size(); i++) {
            if (loadOnDemandField() && fields.get(i).shouldLoad()) {
                fields.get(i).setValue(result_set);
            } else {
                if (!fields.get(i).isLoadOnDemand() && fields.get(i).shouldLoad()) {
                    fields.get(i).setValue(result_set);
                }
            }
        }
        loaded = true;
        markAllFieldsAsNotChanged();
    }

    public void closeResultSet() throws SQLException {
        result_set.close();
    }

    public boolean find(BindFieldInterface bind_fields[], boolean allow_multiple_rows, boolean match_on_any) throws BindException {
        try {
            String sql = "select " + getSelectFields() + " from " + getTableName() + " where ";
            for (int i = 0; i < bind_fields.length; i++) {
                if (bind_fields[i].getObject() != null && !bind_fields[i].getSerializable().equalsIgnoreCase("null")) {
                    sql += bind_fields[i].getName() + " = " + bind_fields[i].getSerializable() + " and ";
                } else {
                    if (!match_on_any) {
                        return false;
                    }
                }
            }
            if (sql.indexOf("and") == -1) {
                sql = sql.substring(0, sql.length() - " where ".length());
            } else {
                sql = sql.substring(0, sql.length() - " and ".length());
            }
            Log.sql.out(sql);
            ResultSet rs = getConnection().createStatement().executeQuery(sql);
            if (rs.next()) {
                if (!allow_multiple_rows) {
                    if (rs.next()) {
                        return false;
                    } else {
                        rs.previous();
                        load(rs);
                        return true;
                    }
                } else {
                    load(rs);
                    return true;
                }
            } else {
                return false;
            }
        } catch (Exception exception) {
            throw new BindException(exception);
        }
    }

    protected void executeSQL(DBConnection connection, String sql) throws SQLException, DataException {
        Log.trace.out(sql);
        Statement smt = connection.createStatement();
        smt.execute(sql);
        smt.close();
    }

    /**
     * Has any of the fields changed
     * Is is necessary to save
     */
    public boolean shouldSave() {
        if (!isLoaded()) {
            return true;
        }
        boolean has_changed = false;
        for (int i = 0; i < getFields().size(); i++) {
            if (getFields().get(i).isChanged()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method should be overridden to force a customzied validation
     * Your entity might require to have either a telephone number and a first name
     * or a last name and a postal address
     * If the validation fails, throw a BindException with the appropriate message
     */
    public void validate() throws BindException, SQLException {
    }

    private StringBuffer save_buffer = new StringBuffer();

    public void save() throws BindException {
        try {
            if (!shouldSave()) {
                return;
            }
            for (int i = 0; i < getFields().size(); i++) {
                if (!(getFields().get(i) instanceof FieldUnique) && !(getFields().get(i) instanceof FieldThreadId) && !getFields().get(i).mayFieldBeNull() && getFields().get(i).isNull()) {
                    throw new DataException.ValidationException("Field " + getFields().get(i).getDescription() + " is not allowed to be null", getFields().get(i));
                }
                String invalidReason = getFields().get(i).isInvalid();
                if (invalidReason != null) {
                    throw new DataException.ValidationException("Field " + getFields().get(i).getDescription() + ":" + invalidReason, getFields().get(i));
                }
            }
            validate();
            if (!loaded) {
                newEvent();
            }
            for (int i = 0; i < getFields().size(); i++) {
                if (getFields().get(i).isChanged()) {
                    changeEvent(getFields().get(i));
                }
            }
            if (loaded) {
                if (getFields().isChangedExcludeBlob()) {
                    save_buffer.setLength(0);
                    save_buffer.append("update ");
                    save_buffer.append(getTableName());
                    save_buffer.append(" set ");
                    save_buffer.append(buildSQLFieldNameListUpdate());
                    save_buffer.append(" where ");
                    save_buffer.append(buildWhereClauseForUnique());
                    executeSQL(getConnection(), save_buffer.toString());
                }
            } else {
                if (id != null) {
                    id.set(getConnection() != null ? getConnection() : DBTools.getDLC());
                }
                for (int i = 0; i < getFields().size(); i++) {
                    getFields().get(i).runOnFirstSave();
                }
                String insertsql[] = buildSQLFieldNameListInsert();
                save_buffer.setLength(0);
                save_buffer.append("insert into ");
                save_buffer.append(getTableName());
                save_buffer.append(insertsql[0]);
                save_buffer.append(" values ");
                save_buffer.append(insertsql[1]);
                executeSQL(getConnection(), save_buffer.toString());
                if (id != null) {
                    id.loadAfterInsert(getConnection());
                }
                setLoaded(true);
            }
            getFields().getChangedBlob().update(getConnection(), getTableName(), buildWhereClauseForUnique());
            markAllFieldsAsNotChanged();
            children.save();
        } catch (Exception sqle) {
            if (sqle instanceof DataException.ValidationException) {
                throw (DataException.ValidationException) sqle;
            } else {
                throw new BindException(sqle);
            }
        }
    }

    /**
     * Only delete the current entity
     * Do not delete the children
     */
    public void deleteEntity() throws SQLException {
        if (!loaded) {
            return;
        }
        deleteEvent();
        String sql_where_clause = buildWhereClauseForUnique() + " and ";
        sql_where_clause = sql_where_clause.substring(0, sql_where_clause.length() - " and ".length());
        String delete_sql = "delete from " + table_name + " where " + sql_where_clause;
        executeSQL(getConnection(), delete_sql);
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) instanceof FieldStringSequence) {
                ((FieldStringSequence) fields.get(i)).insertSequence();
            }
        }
    }

    public void delete() throws BindException {
        try {
            if (!loaded) {
                return;
            }
            children.delete();
            deleteEntity();
        } catch (DataException de) {
            throw new BindException(de);
        } catch (SQLException sqle) {
            throw new BindException(sqle);
        }
    }

    public void commit() throws BindException {
        try {
            getConnection().commit();
        } catch (SQLException sqle) {
            throw new BindException(sqle);
        }
    }

    public String getTableName() {
        return table_name;
    }

    public String getSourceName() {
        return getTableName();
    }

    public VectorField getFields() {
        return fields;
    }

    public VectorFieldAction getFieldAction() {
        return field_action;
    }

    public Index.VectorIndex getIndexes() {
        return indexes;
    }

    public FieldRename.VectorFieldRename getRenameField() {
        return rename_field;
    }

    public DBConnection getConnection() {
        return (DBConnection) connection.get();
    }

    public void setLoaded(boolean p_loaded) {
        loaded = p_loaded;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * After save or load occurred all fields are marked as not changed
     */
    protected void markAllFieldsAsNotChanged() throws DataException {
        for (int i = 0; i < getFields().size(); i++) {
            getFields().get(i).setChanged(false);
        }
    }

    public Bindable getNextBindable() throws BindException {
        try {
            Entity next = getNextEntity();
            if (next == null) {
                if (result_set.getStatement() != null) result_set.getStatement().close();
            } else {
                result_set.close();
            }
            return next;
        } catch (Exception exception) {
            throw new BindException(exception);
        }
    }

    public Entity getNextEntity() throws DataException, SQLException {
        if (next == null) {
            next = rebuildFromResultSet(result_set);
        }
        return next;
    }

    public Vector getListOfAllEntities() throws BindException {
        Vector list = new Vector();
        for (Entity entity = this; loaded && entity != null; entity = (Entity) entity.getNextBindable()) {
            list.add(entity);
        }
        return list;
    }

    public VectorBindable getListOfAllBindables() throws BindException {
        return new VectorBindable(getListOfAllEntities());
    }

    /**
     * Reloads the data from the database.
     */
    public void reload() throws BindException {
        try {
            loadFromIdField();
        } catch (SQLException sqle) {
            throw new BindException(sqle);
        }
    }

    /**
     *
     */
    public void loadFromIdField() throws DataException, SQLException {
        Statement statement = getConnection().createStatement();
        String sql = "select " + getSelectFields() + " from " + getTableName() + " where " + id.getName() + " = " + id.getSQLValue();
        ResultSet result_set = statement.executeQuery(sql);
        if (!result_set.next()) {
            throw new DataException("Could not load [" + getTableName() + "] from id [" + id.getSQLValue() + "]");
        }
        load(result_set);
        statement.close();
    }

    /**
     * Creates a selection that loads all the rows
     * Returns true if any tuples were found
     *
     * public boolean loadAllRows()
     * throws DataException, SQLException {
     * ResultSet result_set = connection.createStatement().executeQuery("select * from " + table_name);
     * if (result_set.next()) {
     * load(result_set);
     * return true;
     * }
     * return false;
     * }*/
    private StringBuffer build_where_buffer = new StringBuffer();

    protected String buildWhereClauseForUnique() throws DataException {
        build_where_buffer.setLength(0);
        build_where_buffer.append(id.getName());
        build_where_buffer.append(" = ");
        build_where_buffer.append(id.getSQLValue());
        if (build_where_buffer.length() == 0) {
            throw new DataException("Entity does not contain any unique fields");
        }
        return build_where_buffer.toString();
    }

    private StringBuffer build_update_buffer = new StringBuffer();

    protected String buildSQLFieldNameListUpdate() throws DataException {
        build_update_buffer.setLength(0);
        boolean need_comma = false;
        for (int i = 0; i < getFields().size(); i++) {
            if ((getFields().get(i).isChanged() || getFields().get(i) instanceof FieldTimestamp) && !(getFields().get(i) instanceof FieldBlob)) {
                if (need_comma) {
                    build_update_buffer.append(",");
                }
                build_update_buffer.append(getFields().get(i).getName());
                build_update_buffer.append(" = ");
                build_update_buffer.append(getFields().get(i).getSQLValue());
                need_comma = true;
            }
        }
        if (build_update_buffer.length() == 0) {
            throw new DataException("No fields to update (no fields has been changed)");
        }
        return build_update_buffer.toString();
    }

    private StringBuffer build_insert_buffer = new StringBuffer();

    protected String[] buildSQLFieldNameListInsert() throws DataException {
        String fields = (String) insert_field_strings.get(getTableName());
        synchronized (getTableName()) {
            if (fields == null) {
                build_insert_buffer.setLength(0);
                build_insert_buffer.append("(");
                for (int i = 0; i < getFields().size(); i++) {
                    if (!(getFields().get(i) instanceof FieldAutoId)) {
                        if (build_insert_buffer.length() > 1) {
                            build_insert_buffer.append(",");
                        }
                        build_insert_buffer.append(getFields().get(i).getName());
                    }
                }
                build_insert_buffer.append(")");
                fields = build_insert_buffer.toString();
                insert_field_strings.put(getTableName(), fields);
            }
        }
        build_insert_buffer.setLength(0);
        build_insert_buffer.append("(");
        for (int i = 0; i < getFields().size(); i++) {
            if (!(getFields().get(i) instanceof FieldAutoId)) {
                if (build_insert_buffer.length() > 1) {
                    build_insert_buffer.append(",");
                }
                build_insert_buffer.append(getFields().get(i).getSQLValue());
            }
        }
        build_insert_buffer.append(")");
        return new String[] { fields, build_insert_buffer.toString() };
    }

    /**
     * Builds a search which searches for any column that matches value
     */
    public String buildSearchForAnyColumn(String value) throws DataException {
        String str = "";
        value = value.toLowerCase();
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            if (f instanceof FieldString) {
                str += "LOWER(" + fields.get(i).getName() + ") like '%" + value + "%' or ";
            } else if (f instanceof FieldBigDecimal) {
                str += fields.get(i).getName() + " like '%" + value + "%' or ";
            }
        }
        if (str.length() > 0) {
            return str.substring(0, str.length() - " or ".length());
        } else {
            return null;
        }
    }

    public Entity rebuildFromResultSet(ResultSet rs) throws SQLException, DataException {
        if (rs.next()) {
            Entity entity = createNew();
            entity.load(rs);
            return entity;
        } else {
            rs.getStatement().close();
        }
        return null;
    }

    public Entity rebuildFromResultSet(String original_sql) throws SQLException, DataException {
        try {
            String sql = original_sql;
            long start = System.currentTimeMillis();
            ResultSet rs;
            if (getConnection() == null) {
                rs = DBTools.getDLC().createStatement().executeQuery(sql);
            } else {
                rs = getConnection().createStatement().executeQuery(sql);
            }
            long end = System.currentTimeMillis();
            Entity entity = rebuildFromResultSet(rs);
            if (DEBUG && (end - start > 350)) {
                sql = sql.substring(sql.indexOf(" where ") + " where ".length());
                if (sql.indexOf(" order ") != -1) {
                    sql = sql.substring(0, sql.indexOf(" order"));
                }
                VectorString fields = new VectorString(sql, new VectorString(new String[] { " ", ">=", "<=", "=", ">", "<", "(", ")" }));
                fields.removeNull();
                fields.removeWordsWithIndexOf("'");
                fields.removeNumbers();
                fields.removeIgnoreCase(new VectorString(new String[] { "limit", "and", "or", "is", "not", "null" }));
                fields.removeDuplicates();
                if (!indexes.equals(fields)) {
                    Log.trace.out("DATABASE INFO", new Exception("SELECT WITH NO INDEX [" + getTableName() + "] [" + fields + "] THAT TOOK [" + (end - start) + "] MILLI SECONDS. SQL [" + original_sql + "]"));
                }
            }
            return entity;
        } catch (SQLException slqe) {
            Log.trace.out("SQLException occurred", "SQL [" + original_sql + "]");
            throw slqe;
        }
    }

    private String getSelectFields() {
        return getFields().getSelectable().getNames().toString(",");
    }

    public Entity find(String where_clause, Integer limit_sql) throws SQLException, DataException {
        if (getDataBaseUtils() instanceof DataBaseUtilsFireBird) {
            where_clause = Utils.replaceAll(where_clause, "= null", "is null");
        }
        if (limit_sql != null) {
            return rebuildFromResultSet(getDataBaseUtils().sqlFirstString("select " + getSelectFields() + " from " + table_name + " where " + where_clause, limit_sql.intValue()));
        } else {
            return rebuildFromResultSet("select " + getSelectFields() + " from " + table_name + " where " + where_clause);
        }
    }

    public Entity find(String where_clause) throws SQLException {
        return find(where_clause, null);
    }

    public Entity findOrderByNameFields(String where_clause) throws SQLException, DataException {
        if (getDataBaseUtils() instanceof DataBaseUtilsFireBird) {
            where_clause = Utils.replaceAll(where_clause, "= null", "is null");
        }
        String order = "";
        if (name_fields.size() > 0) {
            order = " order by ";
            for (int t = 0; t < name_fields.size(); t++) {
                if (t > 0) {
                    order = order + ",";
                }
                order = order + ((Field) name_fields.get(t)).getName();
            }
        }
        return rebuildFromResultSet("select " + getSelectFields() + " from " + table_name + " where " + where_clause + order);
    }

    /**
     * This limits the search to limit
     */
    public Entity find(String where_clause, int limit) throws SQLException, DataException {
        return rebuildFromResultSet(Entity.limitSQL(getConnection(), "select " + getSelectFields() + " from " + table_name + " where " + where_clause, limit));
    }

    public boolean equals(Entity entity, VectorField exclude) {
        if (entity == null || !entity.getTableName().equals(getTableName()) || getFields().size() != entity.getFields().size()) {
            return false;
        }
        if (exclude == null) {
            exclude = new VectorField();
        }
        for (int i = 0; i < getFields().size(); i++) {
            if (!getFields().get(i).isKey() && !exclude.contains(getFields().get(i).getName())) {
                if (getFields().get(i).getObject() == null && entity.getFields().get(i).getObject() != null) {
                    return false;
                }
                if (getFields().get(i).getObject() != null && !getFields().get(i).getObject().equals(entity.getFields().get(i).getObject())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean equals(Entity entity) {
        return equals(entity, null);
    }

    public Entity createNew() {
        try {
            Entity new_entity = getNewInstance();
            new_entity.setConnection(getConnection());
            new_entity.setEntityType(entity_type);
            return new_entity;
        } catch (Exception exception) {
            exception.printStackTrace();
            VectorString desc = new VectorString();
            for (int i = 0; i < getClass().getConstructors().length; i++) {
                desc.add(getClass().getConstructors()[i].toString());
            }
            Log.debug.out("Entity Init", "Could not construct entity width DBConnection parameter. Available constructors [" + desc.toString() + "]");
            Log.critical.out(exception);
        }
        System.exit(-1);
        return null;
    }

    public Bindable getNewBindable() throws BindException {
        return createNew();
    }

    private Field findFieldByName(String name) throws BindException {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals(name)) {
                return fields.get(i);
            }
        }
        throw new BindException("Could not find field [" + name + "] on entity [" + getTableName() + "]");
    }

    /**
     * There exists a relation between this and entity affilaited entity
     * overload this method
     */
    public Entity getAffiliatedEntity(Entity entity, Field field) throws BindException {
        try {
            if (affiliated_entities.containsKey(entity.getTableName())) {
                return (Entity) affiliated_entities.get(entity.getTableName());
            }
            Entity foreign = entity.getNewInstance();
            field = findFieldByName(field.getName());
            foreign.setConnection(getConnection());
            foreign.updateEntity();
            foreign = foreign.find("id = " + field.getSQLValue());
            if (foreign != null) {
                affiliated_entities.put(entity.getTableName(), foreign);
                return foreign;
            }
            return null;
        } catch (Exception exception) {
            throw new BindException(exception);
        }
    }

    public VectorField getNameFields() {
        if (name_fields.size() == 0) {
            name_fields.add((Field) id);
        }
        return name_fields;
    }

    public VectorField getNameFieldRecursive() throws SQLException {
        if (name_fields_recursive == null) {
            name_fields_recursive = new VectorField();
            for (int i = 0; i < getNameFields().size(); i++) {
                if (getNameFields().get(i) instanceof FieldForeignKey) {
                    name_fields_recursive.add(((FieldForeignKey) getNameFields().get(i)).retrieve().getNameFieldRecursive());
                } else {
                    name_fields_recursive.add(getNameFields().get(i));
                }
            }
        }
        return name_fields_recursive;
    }

    public String toString() {
        getNameFields();
        StringBuffer name = new StringBuffer();
        name.append(getTableName());
        name.append("(");
        name.append(name_fields.toString());
        name.append(")");
        return name.toString();
    }

    public DataBaseUtils getDataBaseUtils() {
        if (getConnection() == null) {
            try {
                return DBTools.getDLC().getDataBaseUtils();
            } catch (SQLException e) {
                return null;
            }
        }
        return getConnection().getDataBaseUtils();
    }

    public static String asSQL(Boolean value) {
        return FieldBoolean.getSQLForBoolean(value);
    }

    public static String asSQL(String value) {
        if (value == null) return "null"; else return "'" + value + "'";
    }

    public String asSQL(java.util.Date date) {
        return asSQL(getConnection(), date);
    }

    public String asSQL(java.util.Date date, java.util.Date time) throws ParseException {
        return asSQL(getConnection(), date, time);
    }

    public String dateSUB_HOUR(java.util.Date date, int hours) {
        return dateSUB_HOUR(getConnection(), date, hours);
    }

    public String dateSUB_MIN(java.util.Date date, int min) {
        return dateSUB_MIN(getConnection(), date, min);
    }

    public String dateSUB_SEC(java.util.Date date, int sec) {
        return dateSUB_SEC(getConnection(), date, sec);
    }

    public String currentDate() {
        return currentDate(getConnection());
    }

    public String currentDateTime() {
        return currentDateTime(getConnection());
    }

    public String limitSQL(String sql, int limit) {
        return limitSQL(getConnection(), sql, limit);
    }

    public static String asSQL(DBConnection connection, java.util.Date date) {
        return connection.getDataBaseUtils().convertDateTimeToSQLString(date);
    }

    public static String asSQLDate(DBConnection connection, java.util.Date date) {
        return connection.getDataBaseUtils().convertDateToSQLString(date);
    }

    public static String asSQL(DBConnection connection, java.util.Date date, java.util.Date time) throws ParseException {
        return connection.getDataBaseUtils().convertDateTimeToSQLString(datetime_format.parse(date_format.format(date) + time_format.format(time)));
    }

    public static String dateSUB_HOUR(DBConnection connection, java.util.Date date, int hours) {
        return connection.getDataBaseUtils().dateSUB_HOUR(date, hours);
    }

    public static String dateSUB_MIN(DBConnection connection, java.util.Date date, int min) {
        return connection.getDataBaseUtils().dateSUB_MIN(date, min);
    }

    public static String dateSUB_SEC(DBConnection connection, java.util.Date date, int sec) {
        return connection.getDataBaseUtils().dateSUB_SEC(date, sec);
    }

    public String currentDate(DBConnection connection) {
        return connection.getDataBaseUtils().currentDateSQLMethod();
    }

    public static String currentDateTime(DBConnection connection) {
        return connection.getDataBaseUtils().currentDateTimeSQLMethod();
    }

    public static String limitSQL(DBConnection connection, String sql, int limit) {
        return connection.getDataBaseUtils().sqlFirstString(sql, limit);
    }

    public static String asSQL(Double d) {
        return "" + d;
    }

    public static String asSQL(java.math.BigDecimal value) {
        return "'" + value.toString() + "'";
    }

    private Vector getEntityNewListers() {
        return (Vector) entity_new_listeners.get(getTableName());
    }

    public void addNewListener(NewEntityListener listener) {
        Vector v = getEntityNewListers();
        if (v == null) {
            v = new Vector();
            entity_new_listeners.put(getTableName(), v);
        }
        v.add(listener);
    }

    public void removeNewListener(NewEntityListener listener) {
        Vector v = getEntityNewListers();
        if (v != null) {
            v.remove(listener);
            if (v.size() == 0) {
                entity_new_listeners.remove(getTableName());
            }
        }
    }

    private void newEvent() throws SQLException {
        Vector v = getEntityNewListers();
        if (v != null) {
            for (int t = 0; t < v.size(); t++) {
                getConnection().addNewEvent((NewEntityListener) v.get(t), this);
            }
        }
    }

    private Vector getEntityDeleteListers() {
        return (Vector) entity_delete_listeners.get(getTableName());
    }

    public void addDeleteListener(DeleteEntityListener listener) {
        Vector v = getEntityDeleteListers();
        if (v == null) {
            v = new Vector();
            entity_delete_listeners.put(getTableName(), v);
        }
        v.add(listener);
    }

    public void removeDeleteListener(DeleteEntityListener listener) {
        Vector v = getEntityDeleteListers();
        if (v != null) {
            v.remove(listener);
            if (v.size() == 0) {
                entity_delete_listeners.remove(getTableName());
            }
        }
    }

    private void deleteEvent() throws SQLException {
        Vector v = getEntityDeleteListers();
        if (v != null) {
            for (int t = 0; t < v.size(); t++) {
                getConnection().addDeleteEvent((DeleteEntityListener) v.get(t), this);
            }
        }
    }

    private Hashtable getEntityChangeListers() {
        return (Hashtable) entity_change_listeners.get(getTableName());
    }

    private Vector getFieldChangeListers(Field f) {
        Hashtable h = getEntityChangeListers();
        if (h == null) {
            return null;
        }
        return (Vector) h.get(f.getName());
    }

    public void addChangeListener(ChangeFieldListener listener, Field field) {
        Hashtable h = getEntityChangeListers();
        if (h == null) {
            h = new Hashtable();
            entity_change_listeners.put(getTableName(), h);
        }
        Vector v = (Vector) h.get(field.getName());
        if (v == null) {
            v = new Vector();
            h.put(field.getName(), v);
        }
        v.add(listener);
    }

    public void removeChangeListener(ChangeFieldListener listener, Field field) {
        Hashtable h = getEntityChangeListers();
        if (h != null) {
            Vector v = (Vector) h.get(field.getName());
            if (v != null) {
                v.remove(listener);
                if (v.size() == 0) {
                    h.remove(field.getName());
                    if (h.size() == 0) {
                        entity_change_listeners.remove(getTableName());
                    }
                }
            }
        }
    }

    private void changeEvent(Field field) throws SQLException {
        Vector v = getFieldChangeListers(field);
        if (v != null) {
            for (int t = 0; t < v.size(); t++) {
                getConnection().addChangeEvent((ChangeFieldListener) v.get(t), field);
            }
        }
    }

    public Bindable addFieldToBindable(Bindable bindable) {
        for (int i = 0; i < getFields().size(); i++) {
            bindable.addField(getFields().get(i));
        }
        return bindable;
    }

    /**
     * This will copy field values form entity which is not virtual and id fields
     */
    public void copyDataFromEntity(Bindable bindable, VectorField exclude, boolean copyId) throws BindException {
        BindFieldInterface bind_field[] = bindable.getBindFields();
        if (!getTableName().equals(bindable.getSourceName()) || getFields().size() != bind_field.length) {
            throw new BindException("Can not copy from entity that is not the same type with same amount of fields");
        }
        for (int i = 0; i < getFields().size(); i++) {
            if (!getFields().get(i).isFieldVirtual() && (copyId || !(getFields().get(i) instanceof FieldId)) && !exclude.contains(getFields().get(i).getName())) {
                getFields().get(i).setSerializable(bind_field[i].getSerializable());
            }
        }
    }

    public void copyDataFromEntity(Bindable bindable, VectorField exclude) throws BindException {
        copyDataFromEntity(bindable, exclude, false);
    }

    public Entity copy(boolean copyId) throws DataException, BindException {
        Entity entity = getNewEntityInstance();
        entity.copyDataFromEntity(this, new VectorField(), copyId);
        return entity;
    }

    public Entity copy() throws DataException, BindException {
        return copy(false);
    }

    private static void exportEntity(ZipOutputStream zipstream, Entity entity) throws Exception {
        ZipEntry entry = new ZipEntry(entity.getClass().getName() + "." + entity.id.getAsString());
        zipstream.putNextEntry(entry);
        BindableProperties bindable = new BindableProperties();
        entity.addFieldToBindable(bindable);
        bindable.save();
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        bindable.getProperties().store(byteout, "");
        Utils.readWrite(new ByteArrayInputStream(byteout.toByteArray()), zipstream, false);
    }

    /**
     * This will export the current set to a zip file containing all files
     * of the entity
     * The files will start with table_name.id
     */
    public static File exportToFile(Entity entity) throws Exception {
        File temp = File.createTempFile("entity", "zip");
        ZipOutputStream zipstream = new ZipOutputStream(new FileOutputStream(temp));
        for (; entity != null; entity = entity.getNextEntity()) {
            exportEntity(zipstream, entity);
        }
        zipstream.finish();
        zipstream.close();
        return temp;
    }

    public static File exportToFile(VectorEntity entity) throws Exception {
        if (entity.size() == 0) {
            return null;
        }
        File temp = File.createTempFile("entity", "zip");
        ZipOutputStream zipstream = new ZipOutputStream(new FileOutputStream(temp));
        for (int i = 0; i < entity.size(); i++) {
            exportEntity(zipstream, entity.get(i));
        }
        zipstream.finish();
        zipstream.close();
        return temp;
    }

    public void copyFromProperties(Properties properties, VectorField exclude_field) throws DataException, BindException {
        for (int i = 0; i < getFields().size(); i++) {
            if (exclude_field == null || !exclude_field.contains(getFields().get(i))) {
                getFields().get(i).setSerializable(properties.getProperty(getFields().get(i).getName()));
            }
        }
    }

    public static Entity fromProperties(DBConnection dbcon, Properties properties) throws BindException {
        try {
            Entity entity = (Entity) (Class.forName(properties.getProperty("ENTITYNAME")).newInstance().getClass().getConstructor(new Class[] { dbcon.getClass() }).newInstance(new Object[] { dbcon }));
            entity.copyFromProperties(properties, null);
            return entity;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new BindException(exception);
        }
    }

    public Properties toProperties() throws BindException {
        BindableProperties bindable = new BindableProperties();
        addFieldToBindable(bindable);
        bindable.save();
        bindable.getProperties().put("ENTITYNAME", getClass().getName());
        return bindable.getProperties();
    }

    /**
     * This will initiate and load from the exported zip file
     */
    public static VectorEntity importFromFile(DBConnection dbcon, File file) throws Exception {
        ZipFile zipfile = new ZipFile(file);
        Enumeration entries = zipfile.entries();
        VectorEntity entities = new VectorEntity();
        if (entries.hasMoreElements()) {
            ZipEntry entry = null;
            do {
                entry = entry = (ZipEntry) entries.nextElement();
                File temp = File.createTempFile("entity", "zip");
                Utils.readWrite(zipfile.getInputStream(entry), new FileOutputStream(temp));
                Properties prop = new Properties();
                prop.load(new FileInputStream(temp));
                String enrty_name = entry.getName();
                Entity entity = (Entity) (Class.forName(entry.getName().substring(0, entry.getName().lastIndexOf("."))).newInstance().getClass().getConstructor(new Class[] { dbcon.getClass() }).newInstance(new Object[] { dbcon }));
                entity.copyFromProperties(prop, new VectorField());
                entities.add(entity);
                temp.delete();
            } while (entries.hasMoreElements());
        }
        return entities;
    }
}
