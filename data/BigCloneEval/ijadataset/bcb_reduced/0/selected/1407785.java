package net.sf.opengroove.common.proxystorage;

import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeListener;
import org.apache.commons.collections.map.LRUMap;
import net.sf.opengroove.common.utils.Progress;
import net.sf.opengroove.common.utils.StringUtils;

/**
 * The ProxyStorage class is a class used for storing simple java beans to disk, in such a
 * way that updating one instance of a particular persistant bean causes all other
 * instances within the JVM to immediately reflect the new state. ProxyStorage is intended
 * to be a (very simplified) replacement for Java Persistence API, when JPA is just too
 * heavyweight.<br/> <br/>
 * 
 * Each ProxyStorage instance has a root object. The root object is the single object to
 * be persisted. It should have various fields that other objects can be placed on to make
 * them persistant.<br/> <br/>
 * 
 * When an object is created, it is entered into the proxy storage system with no parent.
 * It can then be assigned to fields of other persistent objects as necessary. Objects
 * that are in the database but which do not have the storage root as an ancestor are
 * removed upon calling the vacuum method of ProxyStorage. The vacuum method generally
 * should only be called right after the proxy storage is created but before it goes into
 * use, as it will remove any objects that are not currently in the tree of objects, which
 * could include a newly-created object that hasn't been assigned to the tree yet.<br/>
 * <br/>
 * 
 * Although a file object is passed to the proxy storage instance, the underlying storage
 * is an embedded relational database. In the future, methods that take a
 * java.sql.Connection instead of a file will be added.<br/> <br/>
 * 
 * ProxyStorage requires the H2 embedded database be included on the classpath. You can
 * perform a google search for "h2 database" to find it. All you have to do is download H2
 * database and make sure h2.jar is on your classpath.<br/> <br/>
 * 
 * ProxyStorage's performance isn't anywhere near as good as JPA's, so it generally should
 * only be used in programs such as applications, where only one user at a time will be
 * using it.
 * 
 * @author Alexander Boyd
 * 
 * @param <E>
 *            The class of the root of the storage.
 */
public class ProxyStorage<E> {

    protected class PropertyChanged implements Runnable {

        private PropertyChangeListener listener;

        private PropertyChangeEvent event;

        public PropertyChanged(PropertyChangeListener listener, PropertyChangeEvent event) {
            this.listener = listener;
            this.event = event;
        }

        @Override
        public void run() {
            listener.propertyChange(event);
        }
    }

    /**
     * The connection to the database. This must be package-private instead of just
     * private since StoredList uses it.
     */
    Connection connection;

    protected static final Map<Class, Delegate> delegateSingletons = new HashMap<Class, Delegate>();

    protected static final Map<Class, ParameterFilter> parameterFilterSingletons = new HashMap<Class, ParameterFilter>();

    protected static final Map<Class, ResultFilter> resultFilterSingletons = new HashMap<Class, ResultFilter>();

    Map propertyCache;

    Map stringCache;

    long opcount = 0;

    /**
     * Gets this ProxyStorage's Opcount, or the number of operations that have been
     * executed. Right now, an operation is defined to be one SQL statement being
     * executed.
     * 
     * @return
     */
    public long getOpcount() {
        synchronized (lock) {
            return opcount;
        }
    }

    /**
     * Maps longs to objects (object ids to the objects themselves), but lrumap doesn't
     * support generics which is why the mapping isn't shown as generics
     */
    protected Map objectCache;

    /**
     * A map that maps proxy object ids to maps that map the object's property names to
     * lists of listeners registered on those propertiews
     */
    private final HashMap<Long, HashMap<String, ArrayList<PropertyChangeListener>>> beanListeners = new HashMap<Long, HashMap<String, ArrayList<PropertyChangeListener>>>();

    private final ThreadPoolExecutor listenerExecutor = new ThreadPoolExecutor(20, 20, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));

    private DatabaseMetaData dbInfo;

    private Class<E> rootClass;

    private ArrayList<Class> allClasses = new ArrayList<Class>();

    /**
     * Internal classes that execute sequences of queries to the database synchronize on
     * this object first to avoid getting corrupt data.<br/> <br/>
     * 
     * This class is package-private (instead of private or protected) because StoredList
     * acesses it.
     */
    final Object lock = new Object();

    /**
     * Calls <tt>this(rootClass, location, 800, 2000, 400);</tt>
     * 
     * @param rootClass
     * @param location
     */
    public ProxyStorage(Class<E> rootClass, File location) {
        this(rootClass, location, 800, 2000, 400);
    }

    public ProxyStorage(Class<E> rootClass, File location, int objectCacheSize, int propertyCacheSize, int stringCacheSize) {
        System.out.println("loading proxystorage on file " + location);
        objectCache = Collections.synchronizedMap(new LRUMap(objectCacheSize));
        propertyCache = Collections.synchronizedMap(new LRUMap(propertyCacheSize));
        stringCache = Collections.synchronizedMap(new LRUMap(stringCacheSize));
        listenerExecutor.allowCoreThreadTimeOut(true);
        this.rootClass = rootClass;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("The H2Database jar is not on your classpath. H2 is " + "used as the backend for ProxyStorage, " + "and so is required.", e);
        }
        try {
            System.out.println("connecting to proxystorage db");
            connection = DriverManager.getConnection("jdbc:h2:" + location.getPath() + ";FILE_LOCK=SOCKET", "sa", "");
            System.out.println("connected");
            dbInfo = connection.getMetaData();
            System.out.println("Hello world");
            checkSystemTables();
            checkTables(rootClass, allClasses);
        } catch (Exception e) {
            throw new RuntimeException("An exception occured while initializing the proxy storage", e);
        }
        System.out.println("performing initial vacuum...");
        vacuum(new Progress());
        System.out.println("initial vacuum performed.");
    }

    /**
     * Checks to make sure that the proxy storage system tables (such as
     * proxystorage_statics and proxystorage_collections) are present. If they are not,
     * they are created. Additionally, if the proxystorage_statics table does not contain
     * a "sequencer" static, one is created with an initial value of 1.
     * 
     * @throws SQLException
     */
    private void checkSystemTables() throws SQLException {
        ArrayList<String> tables = getTableNames();
        if (!tables.contains("proxystorage_statics")) {
            createEmptyTable("proxystorage_statics");
        }
        setTableColumns("proxystorage_statics", new TableColumn[] { new TableColumn("name", Types.VARCHAR, 256), new TableColumn("value", Types.BIGINT, 0) });
        if (!tables.contains("proxystorage_collections")) {
            createEmptyTable("proxystorage_collections");
        }
        setTableColumns("proxystorage_collections", new TableColumn[] { new TableColumn("id", Types.BIGINT, 0), new TableColumn("index", Types.INTEGER, 0), new TableColumn("value", Types.BIGINT, 0) });
        PreparedStatement st = connection.prepareStatement("select name from proxystorage_statics where name = 'sequencer'");
        opcount++;
        ResultSet rs = st.executeQuery();
        if (!rs.next()) {
            execute("insert into proxystorage_statics values (\'sequencer\', 1)");
        }
        rs.close();
        st.close();
    }

    /**
     * Creates a table with the name specified, and no columns.
     * 
     * @param name
     *            The name of the table to create
     */
    private void createEmptyTable(String name) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("create table " + name + " ()");
        opcount++;
        statement.execute();
        statement.close();
    }

    /**
     * Executes the string specified as an SQL statement, discarding it's result set if
     * one is made available.
     * 
     * @param sql
     * @return
     * @throws SQLException
     */
    void execute(String sql) throws SQLException {
        PreparedStatement st = connection.prepareStatement(sql);
        opcount++;
        try {
            st.execute();
        } finally {
            st.close();
        }
    }

    /**
     * Ensures that the table specified has exactly the columns specified. Currently, the
     * type of an existing column is not checked. If a column is not present on the table
     * specified but present in this list, then it will be added via an
     * "alter table add column" statement; if a column exists on the table but not in this
     * list, it will be removed via an "alter table drop column" statement.
     * 
     * @throws SQLException
     */
    private void setTableColumns(String tableName, TableColumn[] lc) throws SQLException {
        ArrayList<TableColumn> existing = getTableColumns(tableName);
        List<TableColumn> columns = Arrays.asList(lc);
        for (TableColumn column : existing) {
            if (!columns.contains(column)) {
                PreparedStatement st = connection.prepareStatement("alter table " + tableName + " drop column " + column.getName());
                opcount++;
                st.execute();
                st.close();
            }
        }
        for (TableColumn column : columns) {
            if (!existing.contains(column)) {
                PreparedStatement st = connection.prepareStatement("alter table " + tableName + " add column " + column.getName() + " " + getStringDataType(column.getType(), column.getSize()));
                opcount++;
                st.execute();
                st.close();
            }
        }
    }

    /**
     * Returns a string value that can be used to represent this type within an SQL
     * statement. For example, if type is {@link Types#BIGINT}, then the returned string
     * would be "bigint" (in this case size is not used), and if the type was
     * {@link Types#VARCHAR} and the size was 1234, then the returned string would be
     * "varchar(1234)".
     * 
     * @param type
     *            The type, as defined in {@link Types}
     * @param size
     *            The size of the data type, if the type is char or varchar
     * @return A string representing the data type
     * @throws SQLException
     *             if an sql exception occurs while accessing the database
     * @throws IllegalArgumentException
     *             if the type specified is not supported by the database
     */
    private String getStringDataType(int type, int size) throws SQLException {
        ResultSet rs = dbInfo.getTypeInfo();
        try {
            while (rs.next()) {
                if (rs.getInt("DATA_TYPE") == type) {
                    String typeName = rs.getString("TYPE_NAME");
                    if (type == Types.VARCHAR) typeName += "(" + size + ")";
                    return typeName;
                }
            }
            throw new IllegalArgumentException("That type is not supported by the db");
        } finally {
            rs.close();
        }
    }

    private ArrayList<TableColumn> getTableColumns(String tableName) throws SQLException {
        ResultSet rs = dbInfo.getColumns(null, null, tableName.toUpperCase(), null);
        ArrayList<TableColumn> results = new ArrayList<TableColumn>();
        while (rs.next()) {
            results.add(new TableColumn(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE")));
        }
        return results;
    }

    private ArrayList<String> getTableNames() throws SQLException {
        ResultSet rs = dbInfo.getTables(null, null, null, null);
        ArrayList<String> results = new CaseInsensitiveCheckList();
        while (rs.next()) {
            results.add(rs.getString("TABLE_NAME").toUpperCase());
        }
        rs.close();
        return results;
    }

    /**
     * Removes all objects that do not have the root as an ancestor. If there is no
     * current root, then this does nothing.
     * 
     * This method uses a mark-and-sweep algorithm to remove objects. It should therefore
     * not be called while the ProxyStorage object is in use to avoid losing data.
     * 
     * @param progress
     *            A progress object that will be updated as this vacuum operation runs
     */
    private void vacuum(Progress progress) {
        ProxyObject root = (ProxyObject) getRoot(false);
        if (root == null) {
            progress.set(1);
            return;
        }
        HashMap<Class, HashSet<Long>> refs = new HashMap<Class, HashSet<Long>>();
        try {
            buildReferenceList(refs, root.getProxyStorageId(), rootClass, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ArrayList<Class> toIterate = new ArrayList<Class>();
        toIterate.addAll(allClasses);
        toIterate.add(StoredList.class);
        for (Class c : toIterate) {
            Set<Long> set = refs.get(c);
            if (set == null) set = new HashSet<Long>();
            final String tableName;
            if (c == StoredList.class) tableName = "proxystorage_collections"; else tableName = getTargetTableName(c);
            final String idColumn = (c == StoredList.class) ? "id" : "proxystorage_id";
            try {
                PreparedStatement lst = connection.prepareStatement("delete from " + tableName + " where not " + idColumn + " in ( " + delimited(set.toArray(new Long[0]), new ToString<Long>() {

                    @Override
                    public String toString(Long object) {
                        return "" + object.longValue();
                    }
                }, ",") + ")");
                opcount++;
                lst.execute();
                lst.close();
            } catch (SQLException e) {
                RuntimeException exception = new RuntimeException("An error occured while scanning class table " + tableName);
            }
        }
    }

    /**
     * Builds a list of all referenced entities, beginning with the id specified. This
     * method is recursive. For list types, the class should be StoredList.
     * 
     * @param list
     * @param id
     * @param c
     * @param subtype
     *            The target class of the storedlist. If <code>c</code> is not
     *            StoredList.class, then this is unused.
     */
    private void buildReferenceList(HashMap<Class, HashSet<Long>> list, long id, Class c, Class subtype) throws SQLException {
        Object object;
        if (c != StoredList.class) object = getById(id, c); else object = new StoredList(ProxyStorage.this, subtype, id);
        HashSet<Long> set = list.get(c);
        if (set == null) {
            set = new HashSet();
            list.put(c, set);
        }
        if (set.contains(id)) return;
        set.add(id);
        if (c == StoredList.class) {
            StoredList storedList = (StoredList) object;
            int size = storedList.size();
            for (int i = 0; i < size; i++) {
                ProxyObject result = (ProxyObject) storedList.get(i);
                buildReferenceList(list, result.getProxyStorageId(), subtype, null);
            }
        } else {
            for (Method method : getGetterMethods(c)) {
                if (method.getReturnType().isAnnotationPresent(ProxyBean.class)) {
                    try {
                        ProxyObject result = (ProxyObject) method.invoke(object, new Object[0]);
                        if (result != null) buildReferenceList(list, result.getProxyStorageId(), method.getReturnType(), null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if (method.getReturnType() == StoredList.class) {
                    try {
                        StoredList result = (StoredList) method.invoke(object, new Object[0]);
                        buildReferenceList(list, result.getProxyStorageId(), StoredList.class, method.getAnnotation(ListType.class).value());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Checks to see if a table for the class specified is present, and checks to see if
     * tables are present for the classes's properties. If the tables aren't present, they
     * are created. If they are missing a column, the column is added. If they have extra
     * columns, the extra columns are removed.
     * 
     * @throws SQLException
     */
    private void checkTables(Class checkClass, ArrayList<Class> alreadyChecked) throws SQLException {
        if (alreadyChecked.contains(checkClass)) return;
        alreadyChecked.add(checkClass);
        String tableName = getTargetTableName(checkClass);
        checkTableExists(tableName);
        ArrayList<TableColumn> columns = getTargetColumns(checkClass);
        setTableColumns(tableName, columns.toArray(new TableColumn[0]));
        for (Method method : getGetterMethods(checkClass)) {
            if (method.getReturnType().isAnnotationPresent(ProxyBean.class)) checkTables(method.getReturnType(), alreadyChecked); else if (method.getReturnType() == StoredList.class) {
                if (!method.isAnnotationPresent(ListType.class)) {
                    throw new IllegalArgumentException("The property with the getter " + method.getName() + " on the class " + checkClass.getName() + " is a StoredList, but it's parameter type " + "is not specified with a ListType annotation.");
                }
                ListType listTypeAnnotation = method.getAnnotation(ListType.class);
                if (!listTypeAnnotation.value().isAnnotationPresent(ProxyBean.class)) throw new IllegalArgumentException("The property with the getter " + method.getName() + " on the class " + checkClass.getName() + " is a StoredList, but it's parameter type (" + listTypeAnnotation.value().getName() + ") does " + "not carry the ProxyBean annotation. If you were trying " + "to create a list of a primitive type wrapper or " + "a list of String, consider wrapping them in ProxyBean-annotated " + "objects that have only one property.");
                checkTables(listTypeAnnotation.value(), alreadyChecked);
            }
        }
    }

    private Method[] getGetterMethods(Class checkClass) {
        ArrayList<Method> results = new ArrayList<Method>();
        Method[] methods = checkClass.getMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Property.class)) continue;
            if (!(method.getName().startsWith("get") || method.getName().startsWith("is"))) continue;
            results.add(method);
        }
        return results.toArray(new Method[0]);
    }

    /**
     * Returns a list of columns that should be in the table for the class specified.
     * There is a column called proxystorage_id that will be included, and then one column
     * per property annotated with Property.
     * 
     * @param checkClass
     *            The class to check
     * @return A list of columns that should exist for the class
     */
    private ArrayList<TableColumn> getTargetColumns(Class checkClass) {
        ArrayList<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn("proxystorage_id", Types.BIGINT, 0));
        for (Method method : getGetterMethods(checkClass)) {
            String methodName = method.getName();
            String propertyName = propertyNameFromAccessor(methodName);
            Class propertyClass = method.getReturnType();
            int type;
            int size = 0;
            if (propertyClass == Long.TYPE || propertyClass == Long.class) type = Types.BIGINT; else if (propertyClass == Integer.TYPE || propertyClass == Integer.class) type = Types.INTEGER; else if (propertyClass == Double.TYPE || propertyClass == Double.class) type = Types.DOUBLE; else if (propertyClass == Boolean.TYPE || propertyClass == Boolean.class) type = Types.BOOLEAN; else if (propertyClass == String.class || propertyClass == BigInteger.class) {
                type = Types.VARCHAR;
                if (method.isAnnotationPresent(Length.class)) {
                    size = ((Length) method.getAnnotation(Length.class)).value();
                } else {
                    size = 1024;
                }
            } else if (propertyClass == StoredList.class) {
                type = Types.BIGINT;
            } else if (Collection.class.isAssignableFrom(propertyClass)) {
                throw new IllegalArgumentException("The class " + propertyClass.getName() + " contains a property (" + propertyName + ") which " + "is a Java Collection. Java Collection implementations " + "themselves aren't supported. You can, however, " + "use a " + StoredList.class.getName());
            } else if (propertyClass.isAnnotationPresent(ProxyBean.class)) {
                type = Types.BIGINT;
            } else throw new RuntimeException("The class " + propertyClass.getName() + " contains a property (" + propertyName + ") which is not of a valid type.");
            columns.add(new TableColumn(propertyName, type, size));
        }
        return columns;
    }

    private String propertyNameFromAccessor(String methodName) {
        String propertyName = methodName.startsWith("is") ? methodName.substring("is".length()) : methodName.substring("get".length());
        propertyName = Introspector.decapitalize(propertyName);
        return propertyName;
    }

    /**
     * If the table specified does not exist, creates it as a table with no columns. If
     * the table specified does exist, it is not modified.
     * 
     * @param tableName
     *            The name of the table to check for
     * @throws SQLException
     */
    private void checkTableExists(String tableName) throws SQLException {
        if (!getTableNames().contains(tableName)) createEmptyTable(tableName);
    }

    /**
     * Gets the name of the table that the class specified should store it's information
     * in. This is usually the non-qualified name of the class, unless it has a Table
     * annotation, in which case the table is the value of that annotation.
     * 
     * @param checkClass
     * @return
     */
    private String getTargetTableName(Class checkClass) {
        if (checkClass.getAnnotation(Table.class) != null) return ((Table) checkClass.getAnnotation(Table.class)).getValue();
        return checkClass.getSimpleName().toLowerCase();
    }

    /**
     * Creates a new instance of the proxy bean interface specified. The interface must be
     * annotated with {@link ProxyBean}, and must be a type that is part of the ownership
     * tree of which <code>E</code> must be the root.<br/> <br/>
     * 
     * The new instance will be persisted in the database, but will not be owned by
     * anything, so a call to vacuum() would remove it until it is assigned to another
     * object that is in the tree.
     * 
     * @param c
     *            The class of the interface to create a new instance of.
     * @return The new instance.
     */
    public <T> T create(Class<T> c) {
        if (!allClasses.contains(c)) throw new IllegalArgumentException("The class specified (" + c.getName() + ") is not a class in the root " + "tree of this proxy storage instance. " + "(the root class is " + rootClass.getName() + ")");
        try {
            ArrayList<TableColumn> columns = getTableColumns(getTargetTableName(c));
            String statement = "insert into " + getTargetTableName(c) + "( " + delimited(columns.toArray(new TableColumn[0]), new ToString<TableColumn>() {

                @Override
                public String toString(TableColumn object) {
                    return object.getName();
                }
            }, ",") + " ) values ( " + delimited(columns.toArray(new TableColumn[0]), new ToString<TableColumn>() {

                @Override
                public String toString(TableColumn object) {
                    return "?";
                }
            }, ",") + ")";
            PreparedStatement st = connection.prepareStatement(statement);
            int index = 0;
            long newId = nextId();
            for (TableColumn col : columns) {
                index += 1;
                if (col.getName().equalsIgnoreCase("proxystorage_id")) st.setLong(index, newId); else {
                    st.setNull(index, col.getType());
                }
            }
            opcount++;
            st.execute();
            st.close();
            return (T) getById(newId, c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String delimited(T[] items, ToString<T> generator, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i != 0) sb.append(delimiter);
            sb.append(generator.toString(items[i]));
        }
        return sb.toString();
    }

    public interface ToString<S> {

        public String toString(S object);
    }

    /**
     * Gets an object that has the specified id and is of the specified type. This method
     * doesn't handle StoredLists; it only handles proxy beans.
     * 
     * @param id
     *            The id of the object
     * @param c
     *            The class of the object
     * @return A new object that represents the id specified. If the object does not
     *         exist, null will be returned. The object returned implements the interface
     *         defined by <code>c</code>, as well as {@link ProxyObject}.
     * @throws SQLException
     */
    Object getById(long id, Class c) throws SQLException {
        synchronized (lock) {
            if (!isTargetIdPresent(id, c)) return null;
            if (objectCache.containsKey(id)) return objectCache.get(id);
            ObjectHandler handler = new ObjectHandler(c, id);
            Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { c, ProxyObject.class }, handler);
            handler.instance = proxy;
            objectCache.put(id, proxy);
            return proxy;
        }
    }

    /**
     * Checks to see if the id specified refers to a valid row in the table for the target
     * specified.
     * 
     * @param id
     * @param c
     * @return
     * @throws SQLException
     */
    private boolean isTargetIdPresent(long id, Class c) throws SQLException {
        synchronized (lock) {
            String statement = "select count(*) from " + getTargetTableName(c) + " where proxystorage_id = ?";
            PreparedStatement st = connection.prepareStatement(statement);
            opcount++;
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            rs.next();
            boolean exists = rs.getInt(1) > 0;
            rs.close();
            st.close();
            return exists;
        }
    }

    /**
     * Gets the root of this ProxyStorage instance. When this is called for the first time
     * in the life of a proxy storage location, a new instance will be created via
     * {@link #create(Class)}. All of the other invocations will return an object that
     * represents the root.
     * 
     * @return
     */
    public E getRoot() {
        return getRoot(true);
    }

    /**
     * Gets the root object. If createIfNonexistant is false, then null is returned if
     * this doesn't yet have a root. If it's true, then a new root is created and
     * returned.
     * 
     * @param createIfNonexistant
     * @return
     */
    public E getRoot(boolean createIfNonexistant) {
        try {
            PreparedStatement rst = connection.prepareStatement("select value from proxystorage_statics where name = ?");
            rst.setString(1, "root");
            opcount++;
            ResultSet rs = rst.executeQuery();
            boolean hasNext = rs.next();
            long existingId = 0;
            if (hasNext) existingId = rs.getLong("value");
            rs.close();
            rst.close();
            if (hasNext) {
                return (E) getById(existingId, rootClass);
            }
            if (!createIfNonexistant) return null;
            Object newObject = create(rootClass);
            long newId = ((ProxyObject) newObject).getProxyStorageId();
            PreparedStatement st = connection.prepareStatement("insert into proxystorage_statics values (?,?)");
            opcount++;
            st.setString(1, "root");
            st.setLong(2, newId);
            st.execute();
            st.close();
            return (E) newObject;
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while performing the requested operation.", e);
        }
    }

    /**
     * The class that actually handles calls to proxy bean methods. Instances of proxy
     * beans that are created use an instance of this as their invocation handler.
     * 
     * @author Alexander Boyd
     * 
     */
    private class ObjectHandler implements InvocationHandler {

        private Class targetClass;

        private long targetId;

        Object instance;

        public ObjectHandler(Class targetClass, long targetId) {
            super();
            this.targetClass = targetClass;
            this.targetId = targetId;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args == null) args = new Object[0];
            if (method.isAnnotationPresent(Ignore.class)) return null;
            synchronized (lock) {
                if (method.isAnnotationPresent(Filter.class)) {
                    Filter annotation = method.getAnnotation(Filter.class);
                    if (annotation.parameterFilter() != ParameterFilter.class) {
                        ParameterFilter filter = parameterFilterSingletons.get(annotation.parameterFilter());
                        if (filter == null) {
                            filter = annotation.parameterFilter().newInstance();
                            parameterFilterSingletons.put(annotation.parameterFilter(), filter);
                        }
                        args = filter.filter(instance, args);
                    }
                }
                if (method.getName().equalsIgnoreCase("addChangeListener") || method.getName().equalsIgnoreCase("removeChangeListener")) {
                    String property = (String) args[0];
                    PropertyChangeListener listener = (PropertyChangeListener) args[1];
                    HashMap<String, ArrayList<PropertyChangeListener>> beanMap = beanListeners.get(targetId);
                    if (beanMap == null) {
                        beanMap = new HashMap<String, ArrayList<PropertyChangeListener>>();
                        beanListeners.put(targetId, beanMap);
                    }
                    ArrayList listenerList = beanMap.get(property);
                    if (listenerList == null) {
                        listenerList = new ArrayList<PropertyChangeListener>();
                        beanMap.put(property, listenerList);
                    }
                    if (method.getName().equalsIgnoreCase("addChangeListener") && !listenerList.contains(listener)) listenerList.add(listener);
                    if (method.getName().equalsIgnoreCase("removeChangeListener") && listenerList.contains(listener)) listenerList.remove(listener);
                    return null;
                }
                if (method.getName().equalsIgnoreCase("getProxyStorageId") && method.getReturnType() == Long.TYPE) return targetId;
                if (method.getName().equalsIgnoreCase("getProxyStorageClass") && method.getReturnType() == Class.class) return targetClass;
                if (method.getName().equalsIgnoreCase("finalize")) {
                    System.out.println("proxystorage object " + targetId + " finalized.");
                    return null;
                }
                if (method.getName().equalsIgnoreCase("isProxyStoragePresent") && method.getReturnType() == Boolean.TYPE) return isTargetIdPresent(targetId, targetClass);
                if (method.getName().equalsIgnoreCase("equals") && args.length == 1) {
                    Object compare = args[0];
                    if (!(compare instanceof ProxyObject)) return false;
                    ProxyObject object = (ProxyObject) compare;
                    long objectId = object.getProxyStorageId();
                    return objectId == targetId;
                }
                if (method.isAnnotationPresent(Search.class)) {
                    Search annotation = method.getAnnotation(Search.class);
                    String listProperty = annotation.listProperty();
                    String searchProperty = annotation.searchProperty();
                    PreparedStatement lst = connection.prepareStatement("select " + listProperty + " from " + getTargetTableName(targetClass) + " where proxystorage_id = ?");
                    opcount++;
                    lst.setLong(1, targetId);
                    ResultSet lrs = lst.executeQuery();
                    if (!lrs.next()) throw new RuntimeException("mismatched object with id " + targetId + " and class " + targetClass.getName());
                    long listId = lrs.getLong(1);
                    if (lrs.wasNull()) {
                        lrs.close();
                        lst.close();
                        if (method.getReturnType().isArray()) return Array.newInstance(method.getReturnType().getComponentType(), 0); else return null;
                    }
                    lrs.close();
                    lst.close();
                    String capitalizedListProperty = listProperty.substring(0, 1).toUpperCase() + listProperty.substring(1);
                    String capitalizedSearchProperty = searchProperty.substring(0, 1).toUpperCase() + searchProperty.substring(1);
                    Method listGetterMethod = method.getDeclaringClass().getMethod("get" + capitalizedListProperty, new Class[0]);
                    ListType listTypeAnnotation = listGetterMethod.getAnnotation(ListType.class);
                    if (listTypeAnnotation == null) throw new RuntimeException("@ListType annotation is not present on stored list getter " + listGetterMethod.getName() + " for class " + listGetterMethod.getDeclaringClass().getName());
                    Class listType = listTypeAnnotation.value();
                    Method searchGetterMethod;
                    try {
                        searchGetterMethod = listType.getMethod("get" + capitalizedSearchProperty, new Class[0]);
                    } catch (NoSuchMethodException ex) {
                        searchGetterMethod = listType.getMethod("is" + capitalizedSearchProperty, new Class[0]);
                    }
                    PreparedStatement st = connection.prepareStatement("select value from proxystorage_collections " + "where id = ? and value in (select proxystorage_id from " + getTargetTableName(listType) + " where " + searchProperty + " " + (annotation.exact() ? "=" : "like") + " ?) order by index asc");
                    st.setLong(1, listId);
                    Object searchValue = args[0];
                    if (!annotation.exact()) {
                        searchValue = (annotation.anywhere() ? "%" : "") + ((String) searchValue).replace("*", "%") + (annotation.anywhere() ? "%" : "");
                    }
                    st.setObject(2, searchValue);
                    opcount++;
                    ResultSet rs = st.executeQuery();
                    ArrayList<Long> resultIds = new ArrayList<Long>();
                    while (rs.next()) {
                        resultIds.add(rs.getLong(1));
                    }
                    rs.close();
                    st.close();
                    Object[] results = new Object[resultIds.size()];
                    int index = 0;
                    for (long resultId : resultIds) {
                        results[index++] = getById(resultId, listType);
                    }
                    if (method.getReturnType().isArray()) {
                        Object resultArray = Array.newInstance(method.getReturnType().getComponentType(), results.length);
                        System.arraycopy(results, 0, resultArray, 0, results.length);
                        return resultArray;
                    } else {
                        if (results.length == 0) return null; else return results[0];
                    }
                }
                if (method.isAnnotationPresent(CompoundSearch.class)) {
                    CompoundSearch annotation = method.getAnnotation(CompoundSearch.class);
                    String listProperty = annotation.listProperty();
                    String[] searchProperties = annotation.searchProperties();
                    PreparedStatement lst = connection.prepareStatement("select " + listProperty + " from " + getTargetTableName(targetClass) + " where proxystorage_id = ?");
                    lst.setLong(1, targetId);
                    opcount++;
                    ResultSet lrs = lst.executeQuery();
                    if (!lrs.next()) throw new RuntimeException("mismatched object with id " + targetId + " and class " + targetClass.getName());
                    long listId = lrs.getLong(1);
                    if (lrs.wasNull()) {
                        lrs.close();
                        lst.close();
                        if (method.getReturnType().isArray()) return Array.newInstance(method.getReturnType().getComponentType(), 0); else return null;
                    }
                    lrs.close();
                    lst.close();
                    String capitalizedListProperty = listProperty.substring(0, 1).toUpperCase() + listProperty.substring(1);
                    Method listGetterMethod = method.getDeclaringClass().getMethod("get" + capitalizedListProperty, new Class[0]);
                    ListType listTypeAnnotation = listGetterMethod.getAnnotation(ListType.class);
                    if (listTypeAnnotation == null) throw new RuntimeException("@ListType annotation is not present on stored list getter " + listGetterMethod.getName() + " for class " + listGetterMethod.getDeclaringClass().getName());
                    Class listType = listTypeAnnotation.value();
                    String[] searchQueryStrings = new String[searchProperties.length];
                    Method[] searchQueryMethods = new Method[searchProperties.length];
                    for (int i = 0; i < searchProperties.length; i++) {
                        String capitalizedSearchProperty = searchProperties[i].substring(0, 1).toUpperCase() + searchProperties[i].substring(1);
                        Method searchGetterMethod;
                        try {
                            searchGetterMethod = listType.getMethod("get" + capitalizedSearchProperty, new Class[0]);
                        } catch (NoSuchMethodException ex) {
                            searchGetterMethod = listType.getMethod("is" + capitalizedSearchProperty, new Class[0]);
                        }
                        searchQueryMethods[i] = searchGetterMethod;
                        searchQueryStrings[i] = searchProperties[i] + " " + (annotation.exact()[i] ? "=" : "like") + " ?";
                    }
                    String searchQuery = StringUtils.delimited(searchQueryStrings, " and ");
                    String searchSql = "select value from proxystorage_collections " + "where id = ? and value in (select proxystorage_id from " + getTargetTableName(listType) + " where " + searchQuery + ") order by index asc";
                    PreparedStatement st = connection.prepareStatement(searchSql);
                    opcount++;
                    st.setLong(1, listId);
                    for (int i = 0; i < searchProperties.length; i++) {
                        Object searchValue = args[i];
                        if (!annotation.exact()[i]) {
                            searchValue = (annotation.anywhere()[i] ? "%" : "") + ((String) searchValue).replace("*", "%") + (annotation.anywhere()[i] ? "%" : "");
                        }
                        st.setObject(i + 2, searchValue);
                    }
                    ResultSet rs = st.executeQuery();
                    ArrayList<Long> resultIds = new ArrayList<Long>();
                    while (rs.next()) {
                        resultIds.add(rs.getLong(1));
                    }
                    rs.close();
                    st.close();
                    Object[] results = new Object[resultIds.size()];
                    int index = 0;
                    for (long resultId : resultIds) {
                        results[index++] = getById(resultId, listType);
                    }
                    if (method.getReturnType().isArray()) {
                        Object resultArray = Array.newInstance(method.getReturnType().getComponentType(), results.length);
                        System.arraycopy(results, 0, resultArray, 0, results.length);
                        return resultArray;
                    } else {
                        if (results.length == 0) return null; else return results[0];
                    }
                }
                if (method.getName().equalsIgnoreCase("hashCode") && args.length == 0) {
                    return (int) targetId * 31;
                }
                if (method.isAnnotationPresent(Constructor.class)) {
                    return ProxyStorage.this.create(method.getReturnType());
                }
                if (method.isAnnotationPresent(CustomProperty.class)) {
                    CustomProperty annotation = method.getAnnotation(CustomProperty.class);
                    Class<? extends Delegate> delegateClass = annotation.value();
                    Delegate delegate = delegateSingletons.get(delegateClass);
                    if (delegate == null) {
                        delegate = delegateClass.newInstance();
                        delegateSingletons.put(delegateClass, delegate);
                    }
                    return delegate.get(instance, method.getReturnType(), propertyNameFromAccessor(method.getName()));
                }
                if (method.getName().equals("toString")) {
                    return "ProxyStorage-id" + targetId;
                }
                if (isPropertyMethod(method)) {
                    if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                        String propertyName = propertyNameFromAccessor(method.getName());
                        BeanPropertyKey cacheKey = new BeanPropertyKey();
                        cacheKey.id = targetId;
                        cacheKey.property = propertyName;
                        Map cacheMap;
                        if (method.getReturnType().equals(String.class)) cacheMap = stringCache; else cacheMap = propertyCache;
                        Object cachedObject = cacheMap.get(cacheKey);
                        Object result;
                        if (cachedObject != null) result = cachedObject; else {
                            PreparedStatement st = connection.prepareStatement("select " + propertyName + " from " + getTargetTableName(targetClass) + " where proxystorage_id = ?");
                            opcount++;
                            st.setLong(1, targetId);
                            ResultSet rs = st.executeQuery();
                            boolean isPresent = rs.next();
                            if (!isPresent) {
                                rs.close();
                                st.close();
                                throw new IllegalStateException("The object that was queried has been deleted " + "from the database.");
                            }
                            result = rs.getObject(propertyName);
                            if (result != null) cacheMap.put(cacheKey, result);
                            rs.close();
                            st.close();
                        }
                        if (method.getReturnType() == Integer.TYPE || method.getReturnType() == Integer.class || method.getReturnType() == Long.TYPE || method.getReturnType() == Long.class || method.getReturnType() == Double.TYPE || method.getReturnType() == Double.class || method.getReturnType() == Boolean.TYPE || method.getReturnType() == Boolean.class || method.getReturnType() == String.class) {
                            if (result == null) {
                                if (method.isAnnotationPresent(Default.class)) {
                                    Default values = method.getAnnotation(Default.class);
                                    if (method.getReturnType() == Integer.TYPE) result = values.intValue();
                                    if (method.getReturnType() == Long.TYPE) result = values.longValue();
                                    if (method.getReturnType() == Double.TYPE) result = values.doubleValue();
                                    if (method.getReturnType() == Boolean.TYPE) result = values.booleanValue();
                                    if (method.getReturnType() == String.class) result = values.stringValue();
                                } else {
                                    if (method.getReturnType() == Integer.TYPE) result = (int) 0;
                                    if (method.getReturnType() == Long.TYPE) result = (long) 0;
                                    if (method.getReturnType() == Double.TYPE) result = (double) 0;
                                    if (method.getReturnType() == Boolean.TYPE) result = false;
                                }
                            }
                            return result;
                        }
                        if (method.getReturnType() == BigInteger.class) {
                            if (result == null) return null;
                            return new BigInteger(((String) result), 16);
                        }
                        if (method.getReturnType() == StoredList.class) {
                            if (result == null) {
                                result = new Long(nextId());
                                PreparedStatement ist = connection.prepareStatement("update " + getTargetTableName(targetClass) + " set " + propertyName + " = ? where proxystorage_id = ?");
                                opcount++;
                                ist.setLong(1, (Long) result);
                                ist.setLong(2, targetId);
                                ist.execute();
                                ist.close();
                            }
                            return new StoredList(ProxyStorage.this, ((ListType) method.getAnnotation(ListType.class)).value(), (Long) result);
                        }
                        if (method.getReturnType().isAnnotationPresent(ProxyBean.class)) {
                            boolean isRequired = method.isAnnotationPresent(Required.class);
                            if (result == null) {
                                if (!isRequired) return null;
                                ProxyObject newObject = (ProxyObject) create(method.getReturnType());
                                long newId = newObject.getProxyStorageId();
                                PreparedStatement ust = connection.prepareStatement("update " + getTargetTableName(targetClass) + " set " + propertyName + " = ? where proxystorage_id = ?");
                                opcount++;
                                ust.setLong(1, newId);
                                ust.setLong(2, targetId);
                                ust.executeUpdate();
                                ust.close();
                                result = newId;
                            }
                            return getById((Long) result, method.getReturnType());
                        }
                        throw new IllegalArgumentException("The method is a getter, but it's return " + "type is not a proper type.");
                    } else {
                        String propertyName = propertyNameFromAccessor(method.getName());
                        PreparedStatement st = connection.prepareStatement("update " + getTargetTableName(targetClass) + " set " + propertyName + " = ? where proxystorage_id = ?");
                        st.setLong(2, targetId);
                        Object inputObject = args[0];
                        if (inputObject != null) {
                            if (inputObject.getClass() == StoredList.class) {
                                throw new IllegalArgumentException("Setters for stored lists are not allowed.");
                            }
                            if (inputObject.getClass() == BigInteger.class) {
                                inputObject = ((BigInteger) inputObject).toString(16);
                            }
                            if (inputObject instanceof ProxyObject) {
                                inputObject = new Long(((ProxyObject) inputObject).getProxyStorageId());
                            }
                        }
                        st.setObject(1, inputObject);
                        opcount++;
                        st.execute();
                        st.close();
                        BeanPropertyKey key = new BeanPropertyKey();
                        key.id = targetId;
                        key.property = propertyName;
                        if (inputObject == null) {
                            stringCache.remove(key);
                            propertyCache.remove(key);
                        } else {
                            if (inputObject instanceof String) stringCache.put(key, inputObject); else propertyCache.put(key, inputObject);
                        }
                        HashMap<String, ArrayList<PropertyChangeListener>> beanMap = beanListeners.get(targetId);
                        if (beanMap != null) {
                            ArrayList<PropertyChangeListener> listenerList = beanMap.get(propertyName);
                            if (listenerList != null) {
                                PropertyChangeEvent event = new PropertyChangeEvent(instance, propertyName, null, null);
                                for (PropertyChangeListener listener : listenerList) {
                                    listenerExecutor.execute(new PropertyChanged(listener, event));
                                }
                            }
                        }
                        return null;
                    }
                }
                throw new UnsupportedOperationException("The method " + method.getName() + " is not supported for the proxy type " + targetClass.getName());
            }
        }
    }

    /**
     * Gets the next id to use. The sequencer row in proxystorage_statics is incremented,
     * and the new value returned.
     * 
     * @return A new id to use
     * @throws SQLException
     */
    long nextId() throws SQLException {
        synchronized (lock) {
            PreparedStatement ist = connection.prepareStatement("update proxystorage_statics set value = value + 1 where name = ?");
            ist.setString(1, "sequencer");
            opcount++;
            ist.execute();
            ist.close();
            PreparedStatement rst = connection.prepareStatement("select value from proxystorage_statics where name = ?");
            rst.setString(1, "sequencer");
            opcount++;
            ResultSet rs = rst.executeQuery();
            rs.next();
            long newId = rs.getLong(1);
            rs.close();
            rst.close();
            return newId;
        }
    }

    /**
     * Returns true if the method is annotated with Property, or if the method starts with
     * "set" and the corresponding "get" method is annotated with Property.
     * 
     * @param method
     * @return
     */
    private boolean isPropertyMethod(Method method) {
        if (method.isAnnotationPresent(Property.class)) return true;
        if (!(method.getName().startsWith("is") || method.getName().startsWith("get") || method.getName().startsWith("set"))) return false;
        String propertyName = propertyNameFromAccessor(method.getName());
        String capitalized = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method getter;
        try {
            getter = method.getDeclaringClass().getMethod("get" + capitalized, new Class[0]);
        } catch (NoSuchMethodException e) {
            try {
                getter = method.getDeclaringClass().getMethod("is" + capitalized, new Class[0]);
            } catch (NoSuchMethodException e2) {
                return false;
            }
        }
        if (!getter.isAnnotationPresent(Property.class)) {
            return false;
        }
        return true;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
