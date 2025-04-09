package de.shandschuh.jaolt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JOptionPane;
import de.shandschuh.jaolt.core.auction.Attribute;
import de.shandschuh.jaolt.core.auction.AttributeSet;
import de.shandschuh.jaolt.core.auction.AttributeValue;
import de.shandschuh.jaolt.core.auction.Category;
import de.shandschuh.jaolt.core.auction.Condition;
import de.shandschuh.jaolt.core.auction.PaymentMethod;
import de.shandschuh.jaolt.core.auction.ShippingService;
import de.shandschuh.jaolt.core.auctionplatform.StaticData;
import de.shandschuh.jaolt.core.exception.CommonException;
import de.shandschuh.jaolt.core.exception.DatabaseInitiationException;
import de.shandschuh.jaolt.core.exception.IllegalDowngradeException;
import de.shandschuh.jaolt.core.exception.InconsistentDatabaseException;
import de.shandschuh.jaolt.gui.Lister;
import de.shandschuh.jaolt.tools.log.Logger;

public class Database {

    private enum Type {

        H2, HSQL, DERBY
    }

    private static String TABLE_CATEGORIES = "categories";

    private static String TABLE_PAYMENTMETHODS = "paymentmethods";

    private static String TABLE_SHIPPINGSERVICES = "shippingservices";

    private static String TABLE_ATTRIBUTES = "attributes";

    private static String TABLE_ATTRIBUTEVALUES = "attributevalues";

    private static String TABLE_STATICDATA = "staticdata";

    private static String TABLE_SHIPTOLOCATIONS = "shiptolocations";

    private static String TABLE_CATEGORYATTRIBUTESETS = "categoryattributesets";

    private static String TABLE_ITEMCONDITIONS = "itemconditions";

    private static String[] TABLES = { TABLE_CATEGORIES, TABLE_PAYMENTMETHODS, TABLE_SHIPPINGSERVICES, TABLE_ATTRIBUTES, TABLE_ATTRIBUTEVALUES, TABLE_STATICDATA, TABLE_SHIPTOLOCATIONS, TABLE_CATEGORYATTRIBUTESETS, TABLE_ITEMCONDITIONS };

    private static Connection PLATFORMDATACONNECTION;

    private static boolean isSetup;

    public static final int VERSION = 19;

    private static Type type;

    private static File dbFile;

    private static Exception initException;

    public static void setup() throws Exception {
        try {
            innerSetup();
        } catch (IllegalDowngradeException ide) {
            delete();
            innerSetup();
        } catch (InconsistentDatabaseException icde) {
            delete();
            innerSetup();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.log(exception);
            initException = exception;
            isSetup = false;
            throw exception;
        }
    }

    private static void innerSetup() throws Exception {
        synchronized (Database.class) {
            try {
                Class.forName("org.h2.Driver");
                type = Type.H2;
            } catch (Exception e) {
                try {
                    Class.forName("org.hsqldb.jdbcDriver");
                    type = Type.HSQL;
                } catch (Exception e1) {
                    try {
                        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                        type = Type.DERBY;
                    } catch (Exception e2) {
                        throw new CommonException(Language.translateStatic("ERROR_NODATABASELIBRARYFOUND"));
                    }
                }
            }
            dbFile = new File(Directory.DATA_DIR + File.separator + type + "staticdb");
            if (type == Type.H2) {
                try {
                    PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:h2:" + dbFile + ";FILE_LOCK=NO;TRACE_LEVEL_FILE=0;LOG=0;UNDO_LOG=0;DB_CLOSE_ON_EXIT=FALSE");
                } catch (SQLException jdbce) {
                    try {
                        PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:h2:" + dbFile + ";FILE_LOCK=NO;TRACE_LEVEL_FILE=0;LOG=0;UNDO_LOG=0;DB_CLOSE_ON_EXIT=FALSE;RECOVER=1");
                    } catch (Exception e0) {
                        if (JOptionPane.showConfirmDialog(Lister.getCurrentInstance(), Language.translateStatic("ERROR_DBCORRUPT"), Language.translateStatic("DIALOG_ERROR_TITLE"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            deleteDatabaseFiles();
                            PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:h2:" + dbFile + ";FILE_LOCK=NO;TRACE_LEVEL_FILE=0;LOG=0;UNDO_LOG=0;DB_CLOSE_ON_EXIT=FALSE");
                        } else {
                            throw new CommonException(Language.translateStatic("ERROR_NODBFILESCREATED"));
                        }
                    }
                }
            } else if (type == Type.HSQL) {
                try {
                    PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:hsqldb:file:" + dbFile + ";hsqldb.lock_file=false", "sa", "");
                } catch (SQLException jdbce) {
                    if (JOptionPane.showConfirmDialog(Lister.getCurrentInstance(), Language.translateStatic("ERROR_DBCORRUPT"), Language.translateStatic("DIALOG_ERROR_TITLE"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        deleteDatabaseFiles();
                        PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:hsqldb:file:" + dbFile + ";hsqldb.lock_file=false", "sa", "");
                    } else {
                        throw new CommonException(Language.translateStatic("ERROR_NODBFILESCREATED"));
                    }
                }
            } else if (type == Type.DERBY) {
                try {
                    PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:derby:" + dbFile + ";create=true");
                } catch (SQLException jdbce) {
                    if (JOptionPane.showConfirmDialog(Lister.getCurrentInstance(), Language.translateStatic("ERROR_DBCORRUPT"), Language.translateStatic("DIALOG_ERROR_TITLE"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        deleteDatabaseFiles();
                        PLATFORMDATACONNECTION = DriverManager.getConnection("jdbc:derby:" + dbFile + ";create=true");
                    } else {
                        throw new CommonException(Language.translateStatic("ERROR_NODBFILESCREATED"));
                    }
                }
            }
            File propertiesFile = new File(dbFile + (type == Type.HSQL ? ".version" : ".properties"));
            Properties properties = new Properties();
            try {
                FileInputStream stream = new FileInputStream(propertiesFile);
                properties.load(stream);
                stream.close();
            } catch (Exception exception) {
            }
            int currentVersion = Integer.parseInt(properties.getProperty("version", "0"));
            if (currentVersion < VERSION) {
                checkVersion(currentVersion);
                properties.setProperty("version", Integer.toString(VERSION));
                FileOutputStream stream = new FileOutputStream(propertiesFile);
                properties.store(stream, null);
                stream.close();
            } else if (currentVersion > VERSION) {
                properties.setProperty("version", Integer.toString(VERSION));
                FileOutputStream stream = new FileOutputStream(propertiesFile);
                properties.store(stream, null);
                stream.close();
                isSetup = true;
                throw new IllegalDowngradeException();
            } else {
                ResultSet resultSet = null;
                int column;
                if (type == Type.H2) {
                    resultSet = PLATFORMDATACONNECTION.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("show tables");
                    column = 1;
                } else {
                    resultSet = PLATFORMDATACONNECTION.getMetaData().getTables(null, null, null, null);
                    column = 3;
                }
                Vector<String> availableTables = new Vector<String>();
                while (resultSet.next()) {
                    availableTables.add(resultSet.getString(column).toUpperCase());
                }
                for (String table : TABLES) {
                    if (!availableTables.contains(table.toUpperCase())) {
                        PLATFORMDATACONNECTION.close();
                        throw new InconsistentDatabaseException();
                    }
                }
            }
            isSetup = true;
        }
    }

    public static void delete() throws Exception {
        if (isSetup) {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            for (String table : TABLES) {
                statement.execute("drop table " + table);
            }
            teardown();
            File propertiesFile = new File(dbFile + (type == Type.HSQL ? ".version" : ".properties"));
            if (propertiesFile.exists()) {
                propertiesFile.delete();
            }
        } else {
            deleteDatabaseFiles();
        }
    }

    private static void deleteDatabaseFiles() {
        if (!isSetup) {
            deleteFiles(Directory.DATA_DIR.listFiles(new FilenameFilter() {

                public boolean accept(File arg0, String name) {
                    return name.startsWith(type + "staticdb");
                }
            }));
        } else {
            throw new CommonException(Language.translateStatic("ERROR_DBISSETUP"));
        }
    }

    private static void deleteFiles(File[] files) {
        for (int n = 0, i = files != null ? files.length : 0; n < i; n++) {
            if (files[n].exists()) {
                if (files[n].isFile()) {
                    if (!files[n].delete()) {
                        throw new CommonException(Language.translateStatic("ERROR_COULDNOTDELETEFILE").replace("$1", files[n].toString()));
                    }
                } else if (files[n].isDirectory()) {
                    deleteFiles(files[n].listFiles());
                    if (!files[n].delete()) {
                        throw new CommonException(Language.translateStatic("ERROR_COULDNOTDELETEFILE").replace("$1", files[n].toString()));
                    }
                }
            }
        }
    }

    private static void checkVersion(int oldVersion) throws Exception {
        if (oldVersion == 0) {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("create table if not exists " + TABLE_ATTRIBUTEVALUES + " (apid int not null, apsid int not null, asid int not null, attributeid int not null, avid int not null, value varchar(170) not null)");
            statement.execute("create table if not exists " + TABLE_ATTRIBUTES + " (apid int not null, apsid int not null, asid int not null, attributeid integer not null, name varchar(70) not null, sidewide boolean)");
            statement.execute("create table if not exists " + TABLE_SHIPPINGSERVICES + " (apid int not null, apsid int not null, type varchar(70) not null, name varchar(100) not null, international boolean default false, carrier varchar(50))");
            statement.execute("create table if not exists " + TABLE_CATEGORIES + " (apid int not null, apsid int not null, categoryid bigint not null, parentid bigint default 0, name varchar(50) not null, leaf boolean, level int)");
            statement.execute("create table if not exists " + TABLE_PAYMENTMETHODS + " (apid int not null, apsid int not null, code int not null)");
            statement.execute("create table if not exists " + TABLE_STATICDATA + " (apid int not null, apsid int not null, type int not null, subtype bigint, vers varchar(50))");
            statement.execute("create table if not exists " + TABLE_SHIPTOLOCATIONS + " (apid int not null, apsid int not null, code int not null)");
            statement.execute("create table if not exists " + TABLE_CATEGORYATTRIBUTESETS + " (apid int not null, apsid int not null, asid int not null, categoryid bigint)");
            statement.execute("create table if not exists " + TABLE_ITEMCONDITIONS + " (apid int not null, apsid int not null, categoryid bigint, cid int not null, cname varchar(50))");
            statement.execute("create index if not exists idxcat on " + TABLE_CATEGORIES + "(apid, apsid, parentid)");
            statement.execute("create index if not exists idxattr on " + TABLE_ATTRIBUTES + "(apid, apsid, asid)");
            statement.execute("create index if not exists idxattrsidewide on " + TABLE_ATTRIBUTES + "(apid, apsid, sidewide)");
            statement.execute("create index if not exists idxattrval on " + TABLE_ATTRIBUTEVALUES + "(apid, apsid, asid, attributeid)");
            statement.execute("create index if not exists idxshipser on " + TABLE_SHIPPINGSERVICES + "(apid, apsid)");
            statement.execute("create index if not exists idxshiploc on " + TABLE_SHIPTOLOCATIONS + "(apid, apsid)");
            statement.execute("create index if not exists idxpay on " + TABLE_PAYMENTMETHODS + "(apid, apsid)");
            statement.execute("create index if not exists idxcatattr on " + TABLE_CATEGORYATTRIBUTESETS + "(apid, apsid, asid)");
            statement.close();
        } else {
            try {
                Statement statement = PLATFORMDATACONNECTION.createStatement();
                if (oldVersion < 12) {
                    statement.execute("alter table " + TABLE_STATICDATA + " add vers varchar(50)");
                }
                if (oldVersion < 13) {
                    statement.execute("create index if not exists idxcat on " + TABLE_CATEGORIES + "(apid, apsid, parentid)");
                    statement.execute("create index if not exists idxattr on " + TABLE_ATTRIBUTES + "(apid, apsid, asid)");
                    statement.execute("create index if not exists idxattrval on " + TABLE_ATTRIBUTEVALUES + "(apid, apsid, asid, attributeid)");
                    statement.execute("create index if not exists idxshipser on " + TABLE_SHIPPINGSERVICES + "(apid, apsid)");
                    statement.execute("create index if not exists idxshiploc on " + TABLE_SHIPTOLOCATIONS + "(apid, apsid)");
                    statement.execute("create index if not exists idxpay on " + TABLE_PAYMENTMETHODS + "(apid, apsid)");
                }
                if (oldVersion < 14) {
                    statement.execute("alter table " + TABLE_ATTRIBUTEVALUES + " alter value varchar(170)");
                    statement.execute("alter table " + TABLE_ATTRIBUTES + " add sidewide boolean");
                    statement.execute("create index if not exists idxattrsidewide on " + TABLE_ATTRIBUTES + "(apid, apsid, sidewide)");
                }
                if (oldVersion < 15) {
                    statement.execute("alter table " + TABLE_SHIPPINGSERVICES + " add carrier varchar(50)");
                }
                if (oldVersion < 16) {
                    statement.execute("alter table " + TABLE_CATEGORIES + " alter column categoryid bigint");
                    statement.execute("alter table " + TABLE_CATEGORIES + " alter column parentid bigint");
                }
                if (oldVersion < 17) {
                    statement.execute("alter table " + TABLE_STATICDATA + " add column subtype bigint");
                    statement.execute("alter table " + TABLE_CATEGORIES + " drop column catalog");
                    statement.execute("alter table " + TABLE_CATEGORIES + " drop column asid");
                    statement.execute("alter table " + TABLE_CATEGORIES + " drop column path");
                }
                if (oldVersion < 18) {
                    statement.execute("alter table " + TABLE_CATEGORIES + " add column level int");
                    statement.execute("create table if not exists " + TABLE_CATEGORYATTRIBUTESETS + " (apid int not null, apsid int not null, asid int not null, categoryid bigint)");
                    statement.execute("create index if not exists idxcatattr on " + TABLE_CATEGORYATTRIBUTESETS + "(apid, apsid, asid)");
                    statement.execute("drop table attributesets");
                }
                if (oldVersion < 19) {
                    statement.execute("create table if not exists " + TABLE_ITEMCONDITIONS + " (apid int not null, apsid int not null, categoryid bigint, cid int not null, cname varchar(50))");
                }
                statement.close();
            } catch (Exception e) {
                delete();
                setup();
            }
        }
    }

    public static void teardown() throws Exception {
        synchronized (Database.class) {
            if (PLATFORMDATACONNECTION != null && !PLATFORMDATACONNECTION.isClosed()) {
                PLATFORMDATACONNECTION.commit();
                PLATFORMDATACONNECTION.close();
            }
        }
        isSetup = false;
    }

    protected static Vector<Category> getSubCategories(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, Category parentCategory) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (hasStaticData(statement, ExistingStaticData.TYPE_CATEGORIES, parentCategory.getId(), auctionPlatform, auctionPlatformSite)) {
                Vector<Category> result = new Vector<Category>(15);
                if (statement.execute("select * from " + TABLE_CATEGORIES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and parentid=" + parentCategory.getId() + " and categoryid != 0")) {
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        Category category = fillCategoryData(new Category(), resultSet);
                        category.setPath(parentCategory.getCompletePath());
                        result.add(category);
                    }
                    resultSet.close();
                }
                statement.close();
                return result;
            } else {
                statement.close();
                return null;
            }
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
            return null;
        }
    }

    public static Category fillCategoryDatas(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, Category category) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (statement.execute("select * from " + TABLE_CATEGORIES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and categoryid=" + category.getId())) {
                ResultSet resultSet = statement.getResultSet();
                if (resultSet.next()) {
                    fillCategoryData(category, resultSet);
                }
                resultSet.close();
            }
            statement.close();
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
        return category;
    }

    private static Category fillCategoryData(Category category, ResultSet resultSet) throws SQLException {
        category.setId(resultSet.getLong("categoryid"));
        category.setName(resultSet.getString("name"));
        category.setLeaf(resultSet.getBoolean("leaf"));
        return category;
    }

    public static void setCategories(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, Category parentCategory, StaticData<Category> categoriesData) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("delete from " + TABLE_CATEGORIES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and parentid=" + parentCategory.getId());
            statement.execute("delete from " + TABLE_STATICDATA + " where type=" + ExistingStaticData.TYPE_CATEGORIES + " and subtype=" + parentCategory.getId() + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_CATEGORIES + " (apid, apsid, categoryid, parentid, name, leaf) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getId() + ", ?, ?, ?, ?)");
            Vector<Category> categories = categoriesData.getData();
            for (Category category : categories) {
                category.setParentId(parentCategory.getId());
                category.setPath(parentCategory.getCompletePath());
                addCategory(category, preparedStatement);
            }
            preparedStatement.close();
            statement.execute("insert into " + TABLE_STATICDATA + " (type, subtype, apid, apsid, vers) values (" + ExistingStaticData.TYPE_CATEGORIES + ", " + parentCategory.getId() + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + categoriesData.getVersion() + "')");
            statement.close();
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
    }

    private static void addCategory(Category category, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, category.getId());
        preparedStatement.setLong(2, category.getParentId());
        preparedStatement.setString(3, "" + category.getName());
        preparedStatement.setBoolean(4, category.isLeaf());
        preparedStatement.execute();
    }

    protected static Vector<PaymentMethod> getPaymentMethods(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (hasStaticData(statement, ExistingStaticData.TYPE_PAYMENTMETHODS, auctionPlatform, auctionPlatformSite)) {
                Vector<PaymentMethod> result = new Vector<PaymentMethod>();
                if (statement.execute("select code from " + TABLE_PAYMENTMETHODS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode())) {
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        result.add(PaymentMethod.getPaymentMethod(resultSet.getInt(1)));
                    }
                    resultSet.close();
                }
                statement.close();
                return result;
            } else {
                statement.close();
                return null;
            }
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
            return null;
        }
    }

    public static void setPaymentMethods(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, StaticData<PaymentMethod> paymentData) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("delete from " + TABLE_PAYMENTMETHODS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            statement.execute("delete from " + TABLE_STATICDATA + " where type=" + ExistingStaticData.TYPE_PAYMENTMETHODS + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_PAYMENTMETHODS + " (apid, apsid, code) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?)");
            Vector<PaymentMethod> paymentMethods = paymentData.getData();
            for (int n = 0, i = paymentMethods.size(); n < i; n++) {
                PaymentMethod paymentMethod = paymentMethods.get(n);
                if (paymentMethod != null) {
                    preparedStatement.setInt(1, paymentMethod.getCode());
                    preparedStatement.execute();
                }
            }
            preparedStatement.close();
            statement.execute("insert into " + TABLE_STATICDATA + " (type, apid, apsid, vers) values (" + ExistingStaticData.TYPE_PAYMENTMETHODS + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + paymentData.getVersion() + "')");
            statement.close();
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
    }

    protected static Vector<ShippingService> getShippingServices(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite) throws DatabaseInitiationException {
        insureStatus();
        Vector<ShippingService> result = new Vector<ShippingService>();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (hasStaticData(statement, ExistingStaticData.TYPE_SHIPPINGSERVICES, auctionPlatform, auctionPlatformSite)) {
                if (statement.execute("select type, name, international, carrier from " + TABLE_SHIPPINGSERVICES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode())) {
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        result.add(new ShippingService(resultSet.getString(1), resultSet.getString(2), resultSet.getBoolean(3), resultSet.getString(4)));
                    }
                    resultSet.close();
                }
                statement.close();
                return result;
            } else {
                statement.close();
                return null;
            }
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
            return null;
        }
    }

    public static void setShippingServices(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, StaticData<ShippingService> shippingData) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("delete from " + TABLE_SHIPPINGSERVICES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            statement.execute("delete from " + TABLE_STATICDATA + " where type=" + ExistingStaticData.TYPE_SHIPPINGSERVICES + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_SHIPPINGSERVICES + " (apid, apsid, type, name, international, carrier) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?, ?, ?, ?)");
            Vector<ShippingService> shippingServices = shippingData.getData();
            for (int n = 0, i = shippingServices.size(); n < i; n++) {
                ShippingService shippingService = shippingServices.get(n);
                if (shippingService != null) {
                    preparedStatement.setString(1, shippingService.getType());
                    preparedStatement.setString(2, shippingService.getName());
                    preparedStatement.setBoolean(3, shippingService.isInternational());
                    preparedStatement.setString(4, shippingService.getCarrier());
                    preparedStatement.execute();
                }
            }
            preparedStatement.close();
            statement.execute("insert into " + TABLE_STATICDATA + " (type, apid, apsid, vers) values (" + ExistingStaticData.TYPE_SHIPPINGSERVICES + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + shippingData.getVersion() + "')");
            statement.close();
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
    }

    protected static Vector<AttributeSet> getAttributeSets(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, long categoryId) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            Vector<AttributeSet> vector = new Vector<AttributeSet>();
            AttributeSet attributeSet = null;
            Vector<Attribute> attributes = new Vector<Attribute>();
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("select avid, value from " + TABLE_ATTRIBUTEVALUES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and asid=? and attributeid=?" + ("alphabetical".equalsIgnoreCase(System.getProperty(AuctionPlatform.DATABASE_SORT_ATTRIBUTEVALUES, "none")) ? " order by value" : ""));
            if (statement.execute("select distinct " + TABLE_ATTRIBUTES + ".attributeid, " + TABLE_ATTRIBUTES + ".name, " + TABLE_ATTRIBUTES + ".asid from " + TABLE_CATEGORYATTRIBUTESETS + " inner join " + TABLE_ATTRIBUTES + " on (" + TABLE_CATEGORYATTRIBUTESETS + ".apid=" + auctionPlatform.getId() + " and " + TABLE_ATTRIBUTES + ".apid=" + auctionPlatform.getId() + " and " + TABLE_CATEGORYATTRIBUTESETS + ".apsid=" + TABLE_ATTRIBUTES + ".apsid and " + TABLE_CATEGORYATTRIBUTESETS + ".asid=" + TABLE_ATTRIBUTES + ".asid and (" + TABLE_CATEGORYATTRIBUTESETS + ".categoryid=" + categoryId + " or " + TABLE_CATEGORYATTRIBUTESETS + ".categoryid=0)) order by " + TABLE_ATTRIBUTES + ".asid")) {
                ResultSet resultSet = statement.getResultSet();
                Vector<Integer> attributeIds = new Vector<Integer>();
                while (resultSet.next()) {
                    if (attributeSet == null || attributeSet.getId() != resultSet.getInt(3)) {
                        attributeSet = new AttributeSet(resultSet.getInt(3));
                        vector.add(attributeSet);
                    }
                    Attribute attribute = new Attribute(resultSet.getInt(1), resultSet.getString(2));
                    if (!attributeIds.contains(attribute.getId())) {
                        attributes.add(attribute);
                        preparedStatement.setInt(1, resultSet.getInt(3));
                        preparedStatement.setInt(2, attribute.getId());
                        if (preparedStatement.execute()) {
                            ResultSet attributeValuesResultSet = preparedStatement.getResultSet();
                            while (attributeValuesResultSet.next()) {
                                attribute.addAttributeValue(new AttributeValue(attributeValuesResultSet.getInt(1), attributeValuesResultSet.getString(2)));
                            }
                        }
                        attributeSet.addAttribute(attribute);
                        attributeIds.add(attribute.getId());
                    }
                }
                resultSet.close();
            }
            statement.close();
            return vector;
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
            return null;
        }
    }

    public static void setAttributeSets(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, long categoryId, StaticData<AttributeSet> attributeSetsCollection) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            Vector<AttributeSet> attributeSets = attributeSetsCollection.getData();
            StringBuilder builder = new StringBuilder("(");
            for (int n = 0, i = attributeSets.size(); n < i; n++) {
                if (n > 0) {
                    builder.append(", ");
                }
                builder.append(attributeSets.get(n).getId());
            }
            String inString = builder.toString() + ")";
            statement.execute("delete from " + TABLE_ATTRIBUTES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and asid in " + inString);
            statement.execute("delete from " + TABLE_ATTRIBUTEVALUES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and asid in " + inString);
            statement.execute("delete from " + TABLE_STATICDATA + " where type=" + ExistingStaticData.TYPE_ATTRIBUTESETS + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and subtype=" + categoryId);
            statement.execute("delete from " + TABLE_CATEGORYATTRIBUTESETS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and categoryid=" + categoryId);
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_ATTRIBUTES + " (apid, apsid, asid, attributeid, name, sidewide) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?, ?, ?, ?)");
            PreparedStatement preparedAttributeStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_ATTRIBUTEVALUES + " (apid, apsid, asid, attributeid, avid, value) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?, ?, ?, ?)");
            PreparedStatement preparedCategoryAttributeSetsStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_CATEGORYATTRIBUTESETS + " (apid, apsid, asid, categoryid) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?, " + categoryId + ")");
            AttributeSet attributeSet;
            int attributeSetId;
            Attribute attribute;
            int attributeId;
            AttributeValue attributeValue;
            for (int n = 0, i = attributeSets.size(); n < i; n++) {
                attributeSet = attributeSets.get(n);
                int length = attributeSet.countAttributes();
                if (length > 0) {
                    attributeSetId = attributeSet.getId();
                    preparedCategoryAttributeSetsStatement.setInt(1, attributeSetId);
                    preparedCategoryAttributeSetsStatement.execute();
                    for (int k = 0; k < length; k++) {
                        attribute = attributeSet.getAttributes()[k];
                        attributeId = attribute.getId();
                        preparedStatement.setInt(1, attributeSetId);
                        preparedStatement.setInt(2, attributeId);
                        preparedStatement.setString(3, attribute.getName());
                        preparedStatement.setBoolean(4, attributeSet.isSidewide());
                        preparedStatement.execute();
                        if (attribute.hasValues()) {
                            for (int t = 0, z = attribute.getAttributeValues().length; t < z; t++) {
                                attributeValue = attribute.getAttributeValues()[t];
                                preparedAttributeStatement.setInt(1, attributeSetId);
                                preparedAttributeStatement.setInt(2, attributeId);
                                preparedAttributeStatement.setInt(3, attributeValue.getId());
                                preparedAttributeStatement.setString(4, attributeValue.getValue());
                                preparedAttributeStatement.execute();
                            }
                        }
                    }
                }
            }
            preparedCategoryAttributeSetsStatement.close();
            preparedAttributeStatement.close();
            preparedStatement.close();
            statement.execute("insert into " + TABLE_STATICDATA + " (type, subtype, apid, apsid, vers) values (" + ExistingStaticData.TYPE_ATTRIBUTESETS + ", " + categoryId + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + attributeSetsCollection.getVersion() + "')");
            statement.close();
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
    }

    public static void setShipToLocations(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, StaticData<Country> countriesData) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("delete from " + TABLE_SHIPTOLOCATIONS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            statement.execute("delete from " + TABLE_STATICDATA + " where type=" + ExistingStaticData.TYPE_SHIPTOLOCATIONS + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_SHIPTOLOCATIONS + " (apid, apsid, code) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?)");
            Vector<Country> countries = countriesData.getData();
            for (int n = 0, i = countries.size(); n < i; n++) {
                Country country = countries.get(n);
                if (country != null) {
                    preparedStatement.setInt(1, country.getCode());
                    preparedStatement.execute();
                }
            }
            preparedStatement.close();
            statement.execute("insert into " + TABLE_STATICDATA + " (type, apid, apsid, vers) values (" + ExistingStaticData.TYPE_SHIPTOLOCATIONS + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + countriesData.getVersion() + "')");
            statement.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            Logger.log(sqlException);
        }
    }

    protected static Vector<Country> getShipToLocations(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite) throws DatabaseInitiationException {
        insureStatus();
        Vector<Country> result = new Vector<Country>();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (hasStaticData(statement, ExistingStaticData.TYPE_SHIPTOLOCATIONS, auctionPlatform, auctionPlatformSite)) {
                if (statement.execute("select code from " + TABLE_SHIPTOLOCATIONS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode())) {
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        result.add(new Country(resultSet.getInt(1)));
                    }
                    resultSet.close();
                }
                statement.close();
                return result;
            } else {
                statement.close();
                return null;
            }
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
            return null;
        }
    }

    public static ExistingStaticData getExistingStaticData(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite) throws DatabaseInitiationException {
        insureStatus();
        ExistingStaticData result = new ExistingStaticData(auctionPlatform, auctionPlatformSite);
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (statement.execute("select type, subtype, vers from " + TABLE_STATICDATA + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode())) {
                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    result.addType(resultSet.getInt(1), resultSet.getLong(2), resultSet.getString(3));
                }
                resultSet.close();
            }
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
        return result;
    }

    public static Vector<ExistingStaticData> getExistingStaticData() throws DatabaseInitiationException {
        insureStatus();
        Vector<ExistingStaticData> existingStaticDataVector = new Vector<ExistingStaticData>();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (statement.execute("select apid, apsid, type, subtype, vers from " + TABLE_STATICDATA + " order by apid, apsid")) {
                ResultSet resultSet = statement.getResultSet();
                AuctionPlatform auctionPlatform = null;
                AuctionPlatformSite auctionPlatformSite = null;
                ExistingStaticData existingStaticData = null;
                while (resultSet.next()) {
                    long auctionPlatformId = resultSet.getLong(1);
                    if (auctionPlatform == null || auctionPlatform.getId() != auctionPlatformId) {
                        auctionPlatform = AuctionPlatform.getInstance(auctionPlatformId);
                    }
                    int auctionPlatformSiteId = resultSet.getInt(2);
                    if (auctionPlatformSite == null || auctionPlatformSite.getId() != auctionPlatformSiteId) {
                        auctionPlatformSite = new AuctionPlatformSite(auctionPlatformSiteId);
                    }
                    if (existingStaticData == null || existingStaticData.getAuctionPlatform() != auctionPlatform || !existingStaticData.getAuctionPlatformSite().equals(auctionPlatformSite)) {
                        existingStaticData = new ExistingStaticData(auctionPlatform, auctionPlatformSite);
                        existingStaticDataVector.add(existingStaticData);
                    }
                    existingStaticData.addType(resultSet.getInt(3), resultSet.getLong(4), resultSet.getString(5));
                }
                resultSet.close();
            }
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
        }
        return existingStaticDataVector;
    }

    private static void insureStatus() throws DatabaseInitiationException {
        synchronized (Database.class) {
            if (PLATFORMDATACONNECTION == null || !isSetup) {
                throw new DatabaseInitiationException(initException);
            }
        }
    }

    private static boolean hasStaticData(Statement statement, int type, long subtype, AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select type from " + TABLE_STATICDATA + " where type=" + type + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + (subtype > 0 ? " and subtype=" + subtype : ""));
        if (resultSet.next()) {
            resultSet.close();
            return true;
        } else {
            resultSet.close();
            return false;
        }
    }

    private static boolean hasStaticData(Statement statement, int type, AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite) throws SQLException {
        return hasStaticData(statement, type, 0, auctionPlatform, auctionPlatformSite);
    }

    public static Vector<Integer> setCategoryAttributeSetIdsAndReturnSingle(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, long categoryId, StaticData<Pair<Long, Vector<Integer>>> data) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("delete from " + TABLE_CATEGORYATTRIBUTESETS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            statement.execute("delete from " + TABLE_STATICDATA + " where type=" + ExistingStaticData.TYPE_CATEGORYATTRIBUTESETS + " and apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_CATEGORYATTRIBUTESETS + " (apid, apsid, asid, categoryid) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?, ?)");
            Vector<Pair<Long, Vector<Integer>>> pairs = data.getData();
            Vector<Integer> result = new Vector<Integer>();
            Vector<Integer> addResult = new Vector<Integer>();
            for (Pair<Long, Vector<Integer>> pair : pairs) {
                Vector<Integer> vector = pair.getValue();
                preparedStatement.setLong(2, pair.getKey());
                for (int id : vector) {
                    preparedStatement.setInt(1, id);
                    preparedStatement.execute();
                }
                if (categoryId == pair.getKey()) {
                    result.addAll(pair.getValue());
                } else if (0l == pair.getKey()) {
                    addResult.addAll(pair.getValue());
                }
            }
            result.addAll(addResult);
            statement.execute("insert into " + TABLE_STATICDATA + " (type, apid, apsid, vers) values (" + ExistingStaticData.TYPE_CATEGORYATTRIBUTESETS + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + data.getVersion() + "')");
            statement.close();
            preparedStatement.close();
            return result;
        } catch (SQLException sqlException) {
            Logger.log(sqlException);
            return null;
        }
    }

    public static Vector<Integer> getCategoryAttributeSetIds(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, long categoryId) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (hasStaticData(statement, ExistingStaticData.TYPE_CATEGORYATTRIBUTESETS, auctionPlatform, auctionPlatformSite)) {
                Vector<Integer> result = new Vector<Integer>();
                if (statement.execute("select distinct asid from " + TABLE_CATEGORYATTRIBUTESETS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and (categoryid=" + categoryId + " or categoryid=0)")) {
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        result.add(resultSet.getInt(1));
                    }
                }
                statement.close();
                return result;
            } else {
                statement.close();
                return null;
            }
        } catch (SQLException e) {
            Logger.log(e);
            return null;
        }
    }

    private static long getCategoryParentId(Statement statement, AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, long categoryId) throws Exception {
        if (statement.execute("select parentid from " + TABLE_CATEGORIES + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and categoryid=" + categoryId)) {
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                long id = resultSet.getLong(1);
                if (id > 0 && id != categoryId) {
                    resultSet.close();
                    return id;
                }
            }
            resultSet.close();
        }
        return 0;
    }

    public static void setConditions(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, StaticData<Pair<Long, Vector<Condition>>> data) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            statement.execute("delete from " + TABLE_ITEMCONDITIONS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode());
            statement.execute("delete from " + TABLE_STATICDATA + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and type=" + ExistingStaticData.TYPE_CONDITIONS);
            PreparedStatement preparedStatement = PLATFORMDATACONNECTION.prepareStatement("insert into " + TABLE_ITEMCONDITIONS + " (apid, apsid, categoryid, cid, cname) values (" + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", ?, ?, ?)");
            for (Pair<Long, Vector<Condition>> pair : data.getData()) {
                preparedStatement.setLong(1, pair.getKey());
                Vector<Condition> conditions = pair.getValue();
                for (Condition condition : conditions) {
                    preparedStatement.setInt(2, condition.getId());
                    preparedStatement.setString(3, condition.getName());
                    preparedStatement.execute();
                }
            }
            preparedStatement.close();
            statement.execute("insert into " + TABLE_STATICDATA + " (type, apid, apsid, vers) values (" + ExistingStaticData.TYPE_CONDITIONS + ", " + auctionPlatform.getId() + ", " + auctionPlatformSite.getCode() + ", '" + data.getVersion() + "')");
            statement.close();
        } catch (Exception exception) {
        }
    }

    public static Vector<Condition> getConditions(AuctionPlatform auctionPlatform, AuctionPlatformSite auctionPlatformSite, long categoryId, long alternateCategoryId) throws DatabaseInitiationException {
        insureStatus();
        try {
            Statement statement = PLATFORMDATACONNECTION.createStatement();
            if (hasStaticData(statement, ExistingStaticData.TYPE_CONDITIONS, auctionPlatform, auctionPlatformSite)) {
                int count = 0;
                Vector<Condition> globalResult = new Vector<Condition>();
                while (categoryId != 0 && count < 8) {
                    count++;
                    globalResult.clear();
                    Vector<Condition> localResult = new Vector<Condition>();
                    if (statement.execute("select cid, cname, categoryid from " + TABLE_ITEMCONDITIONS + " where apid=" + auctionPlatform.getId() + " and apsid=" + auctionPlatformSite.getCode() + " and (categoryid=" + categoryId + " or categoryid=" + alternateCategoryId + ")")) {
                        ResultSet resultSet = statement.getResultSet();
                        while (resultSet.next()) {
                            if (resultSet.getLong(3) == 0) {
                                globalResult.add(new Condition(resultSet.getInt(1), resultSet.getString(2), 0));
                            } else {
                                localResult.add(new Condition(resultSet.getInt(1), resultSet.getString(2), resultSet.getLong(3)));
                            }
                        }
                    }
                    if (localResult.size() > 0) {
                        statement.close();
                        return localResult;
                    } else {
                        categoryId = getCategoryParentId(statement, auctionPlatform, auctionPlatformSite, categoryId);
                    }
                }
                statement.close();
                return globalResult;
            } else {
                statement.close();
                return null;
            }
        } catch (Exception exception) {
            return null;
        }
    }
}
