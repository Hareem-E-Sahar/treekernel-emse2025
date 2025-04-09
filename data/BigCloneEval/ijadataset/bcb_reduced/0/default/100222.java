import javax.sql.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import junit.framework.*;
import simplejda.sqlbeans.*;
import testoutput.*;

public class Test extends TestCase {

    public static final String JDBC_DRIVER_CLASS = "org.postgresql.Driver";

    public static final String JDBC_CONNECTION_STRING = "jdbc:postgresql://localhost/test/";

    public static final String JDBC_USER_NAME = "simplejda";

    public static final String JDBC_PASSWORD = "";

    public static void main(String[] args) {
        TestSuite testSuite = new TestSuite();
        testSuite.addTestSuite(Test.class);
        TestResult testResults = new TestResult();
        testSuite.run(testResults);
        log("\n\n\n");
        if (testResults.wasSuccessful()) {
            log("All tests were passed.  SHIPIT!");
        } else {
            log("Testing was unsuccessful");
            log("\tfailures:");
            Enumeration e = testResults.failures();
            while (e.hasMoreElements()) {
                TestFailure tf = (TestFailure) e.nextElement();
                log("\t\t" + tf + " : " + tf.thrownException());
            }
            log("\terrors:");
            e = testResults.errors();
            while (e.hasMoreElements()) {
                TestFailure tf = (TestFailure) e.nextElement();
                log("\t\t" + tf + " : " + tf.thrownException() + ":");
                tf.thrownException().printStackTrace();
            }
        }
    }

    public Test(String name) {
        super(name);
    }

    public void setUp() {
        UsersSQLBean userHook = new UsersSQLBean();
        userHook.plugFieldGenerator(UsersSQLBean.USER_ID, new TestUserIdGenerator());
        userHook.plugValidator(new TestUserValidator());
        userHook.plugNaturalOrder(UsersSQLBean.LAST_NAME);
        userHook.plugComparator(new TestUserIdBasedComparator());
        AddressesSQLBean addressHook = new AddressesSQLBean();
        addressHook.plugFieldGenerator(AddressesSQLBean.ADDRESS_ID, new TestAddressesIdGenerator());
        UserAddressesSQLBean userAddressHook = new UserAddressesSQLBean();
        userAddressHook.plugFieldGenerator(UserAddressesSQLBean.USER_ADDRESS_ID, new TestUserAddressesIdGenerator());
    }

    public void testEqualsOperator() {
        UsersSQLBean u1 = new UsersSQLBean();
        UsersSQLBean u2 = new UsersSQLBean();
        UsersSQLBean u3 = new UsersSQLBean();
        u1.setUserId(1);
        u2.setUserId(1);
        u3.setUserId(2);
        assertTrue(u1.equals(u1));
        assertTrue(u2.equals(u2));
        assertTrue(u3.equals(u3));
        assertTrue(u1.equals(u2));
        assertTrue(u2.equals(u1));
        assertTrue(!u1.equals(u3));
        assertTrue(!u3.equals(u1));
        assertTrue(!u2.equals(u3));
        assertTrue(!u3.equals(u2));
        EmailAddressesSQLBean a1 = new EmailAddressesSQLBean();
        EmailAddressesSQLBean a2 = new EmailAddressesSQLBean();
        EmailAddressesSQLBean a3 = new EmailAddressesSQLBean();
        assertTrue(a1.equals(a1));
        assertTrue(a2.equals(a2));
        assertTrue(a3.equals(a3));
        assertTrue(!a1.equals(a2));
        assertTrue(!a2.equals(a1));
        assertTrue(!a1.equals(a3));
        assertTrue(!a3.equals(a1));
        assertTrue(!a2.equals(a3));
        assertTrue(!a3.equals(a2));
    }

    public void testDataSourceUpdate() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean where = new UsersSQLBean();
        where.setCompany("GlobalOmniCorp");
        UsersSQLBean to = new UsersSQLBean();
        to.setCompany("GlobalOmniCorp Inc.");
        log("updating users where " + where);
        log("updating users to " + to);
        dataSource.update(to, where);
    }

    public void testDataSourceDelete() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean where = new UsersSQLBean();
        where.setCompany("Crappy Company");
        log("deleteing users where " + where);
        int deleted = dataSource.delete(where);
        log("deleted " + deleted + " users");
    }

    public static void testSimpleGetByFieldWithGenerator() {
        log("trying to find users with user id");
        UsersSQLBean where = new UsersSQLBean();
        where.setUserId(1);
        Collection users = null;
        Connection conn = null;
        UsersSQLBean aUser = null;
        try {
            conn = getConnection();
            aUser = (UsersSQLBean) SQLBeanManager.get(where, conn);
        } catch (Exception e) {
            e.printStackTrace();
            error(e);
            assertTrue(false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    error(e);
                }
            }
        }
        log(aUser);
    }

    public void testDataSourceGetByFieldWithGenerator() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean where = new UsersSQLBean();
        where.setUserId(1);
        log("Finding user where " + where);
        UsersSQLBean aUser = (UsersSQLBean) dataSource.get(where);
        log(aUser);
    }

    public void testDataSourceOrderByFeatureWithStartAndCount() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean where = new UsersSQLBean();
        int count = 10;
        int start = 20;
        where.setOrderBy(UsersSQLBean.FIRST_NAME);
        Collection usersFound = dataSource.find(where, count, start);
        log(usersFound);
    }

    public void testDataSourceOrderByFeature() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean where = new UsersSQLBean();
        where.setOrderBy(UsersSQLBean.FIRST_NAME);
        Collection usersFound = dataSource.find(where);
        log(usersFound);
    }

    public void testDataSourceFindUsersWithSQLString() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users with string");
        Collection usersFound = dataSource.find(UsersSQLBean.TYPE, "select * from users where first_name like '%a%'");
        log(usersFound);
    }

    public void testDataSourceFindUsersWithSQLJoin() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users with city in san francisco vi SQL");
        String sql = "SELECT * FROM users " + "JOIN user_addresses ON users.user_id=user_addresses.user_id " + "JOIN addresses ON user_addresses.address_id=addresses.address_id " + "WHERE addresses.city='San Francisco'";
        Collection usersFound = dataSource.find(UsersSQLBean.TYPE, sql);
        log(usersFound);
    }

    public void testDataSourceFindUsersWithSQLBeanJoin() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users with city in san francisco vi SQLBeans");
        UsersSQLBean user = new UsersSQLBean();
        UserAddressesSQLBean userAddress = new UserAddressesSQLBean();
        AddressesSQLBean address = new AddressesSQLBean();
        SQLBeanJoin userJoin = new SQLBeanJoin(user.USER_ID, userAddress.USER_ID);
        SQLBeanJoin addressJoin = new SQLBeanJoin(userAddress.ADDRESS_ID, address.ADDRESS_ID);
        SQLBean beans[] = { user, userAddress, address };
        SQLBeanJoin joins[] = { userJoin, addressJoin };
        address.setCity("San Francisco");
        Collection results = dataSource.join(beans, joins);
        log("Results from SQLBean join: " + results);
    }

    public static void testSimpleFindUsersWithSQLString() {
        log("trying to find users with string");
        Collection users = null;
        Connection conn = null;
        try {
            conn = getConnection();
            users = SQLBeanManager.find(UsersSQLBean.TYPE, "select * from users where first_name like '%a%'", conn);
        } catch (Exception e) {
            e.printStackTrace();
            error(e);
            assertTrue(false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    error(e);
                }
            }
        }
        log(users);
    }

    public void testDataSourceSimpleFindUsersInCompany() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users in GlobalOmniCorp");
        UsersSQLBean where = new UsersSQLBean();
        where.setCompany("GlobalOmniCorp");
        Collection usersFound = dataSource.find(where);
        log(usersFound);
    }

    public static void testSimpleFindUsersInCompany() {
        log("trying to find users in GlobalOmniCorp");
        UsersSQLBean where = new UsersSQLBean();
        where.setCompany("GlobalOmniCorp");
        Collection users = null;
        Connection conn = null;
        try {
            conn = getConnection();
            users = SQLBeanManager.find(where, conn);
        } catch (Exception e) {
            error(e);
            assertTrue(false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    error(e);
                }
            }
        }
        log(users);
    }

    public static void testDataSourceSQLBeanPreparedStatementFindUsersInCompany() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users in GlobalOmniCorp");
        SQLBeanPreparedStatement sqlbps = new SQLBeanPreparedStatement("SELECT * from users where company=?");
        sqlbps.setString(1, "GlobalOmniCorp");
        Collection users = dataSource.find(UsersSQLBean.TYPE, sqlbps);
        log(users);
    }

    public void testDataSourceFindAllUsers() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find all users");
        UsersSQLBean where = new UsersSQLBean();
        Collection usersFound = dataSource.find(where);
        log(usersFound);
    }

    public static void testSimpleFindAllUsers() {
        log("trying to find all users");
        UsersSQLBean where = new UsersSQLBean();
        Collection users = null;
        Connection conn = null;
        try {
            conn = getConnection();
            users = SQLBeanManager.find(where, conn);
        } catch (Exception e) {
            error(e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    error(e);
                }
            }
        }
        log(users);
    }

    public void testDataSourceFindUsersWithCollection() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users using a collection");
        UsersSQLBean where = new UsersSQLBean();
        Vector v = new Vector();
        v.add(new Integer(742059569));
        v.add(new Integer(742059349));
        v.add(new Integer(742059148));
        where.userIdIsIn(v);
        Vector v2 = new Vector();
        v2.add(new Integer(-1));
        v2.add(new Integer(-2));
        v2.add(new Integer(-3));
        where.userIdIsNotIn(v);
        Collection usersFound = dataSource.find(where);
        log(usersFound);
    }

    public void testDataSourceFindUsersWithLike() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users using like ");
        UsersSQLBean where = new UsersSQLBean();
        where.firstNameIsLike("%oh%");
        Collection usersFound = dataSource.find(where);
        log("Users found using like :/n/n" + usersFound);
    }

    public void testDataSourceFindUsersWithCollection2() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to find users using a collection");
        UsersSQLBean where = new UsersSQLBean();
        Vector v = new Vector();
        v.add(new Integer(742059569));
        v.add(new Integer(742059349));
        v.add(new Integer(742059148));
        where.userIdIsIn(v);
        where.setCompany("GlobalOmniCorp Inc.");
        Collection usersFound = dataSource.find(where);
        log(usersFound);
    }

    public void testDataSourceInvalidUserInsert() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to insert invalid user");
        UsersSQLBean user = new UsersSQLBean();
        user.setFirstName("nathan");
        user.setLastName("jones");
        user.setPassword("nothing special");
        user.setCompany("GlobalOmniCorp");
        log("created invalid user" + user);
        try {
            dataSource.insert(user);
            log("successfully insert invalid user");
            assertTrue(false);
        } catch (ValidateException ve) {
            error(ve.getMessage());
        }
    }

    public void testSimpleDataSourceUserInsert() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to insert user via datasource");
        UsersSQLBean user = new UsersSQLBean();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setPassword("nothing special");
        user.setCompany("GlobalOmniCorp");
        log("created user" + user);
        dataSource.insert(user);
    }

    public void testValidatedDataSourceUserInsert() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        log("trying to insert user with validation block via datasource");
        UsersSQLBean user = new UsersSQLBean();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setPassword("nothing special");
        user.setCompany("GlobalOmniCorp");
        log("created user" + user);
        try {
            dataSource.insert(user);
        } catch (ValidateException ve) {
            error(ve.getMessage());
            assertTrue(false);
        }
    }

    public void testUserInsert() {
        log("trying to insert user with validation");
        UsersSQLBean user = new UsersSQLBean();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setPassword("nothing special");
        user.setCompany("GlobalOmniCorp");
        log("created user" + user);
        Connection conn = null;
        try {
            conn = getConnection();
            SQLBeanManager.insert(user, conn);
        } catch (Exception e) {
            error(e);
            assertTrue(false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    error(e);
                }
            }
        }
    }

    public void testDataSourceAddressInsert() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean user = (UsersSQLBean) dataSource.get(UsersSQLBean.TYPE, "SELECT * FROM users");
        AddressesSQLBean address = new AddressesSQLBean();
        address.setAddress1("Some Street");
        address.setAddress2("Some Apartment");
        address.setCity("San Francisco");
        address.setState("CA");
        address.setCountry("USA");
        address.setZip("00000");
        dataSource.insert(address);
        log("Address is : " + address);
        UserAddressesSQLBean userAddress = new UserAddressesSQLBean();
        assertTrue(address.getAddressId() != null);
        userAddress.setUserId(user.getUserId());
        userAddress.setAddressId(address.getAddressId());
        dataSource.insert(userAddress);
        log("userAddress is " + userAddress);
        log("attempting to delete address, which should fail.");
        try {
            dataSource.delete(address);
            assertTrue(false);
        } catch (Exception e) {
            log("deleting address failed, as expected.");
        }
    }

    public void testDataSourceBadUserAddressInsert() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UserAddressesSQLBean userAddress = new UserAddressesSQLBean();
        userAddress.setUserId(-1);
        userAddress.setAddressId(-1);
        try {
            dataSource.insert(userAddress);
            assertTrue(false);
        } catch (Exception e) {
            log("inserting bad user address failed, as expected.");
        }
    }

    public void testTableBasedSequence() {
        TableBasedIntegerGenerator generator = new TableBasedIntegerGenerator((DataSource) new FakeDataSource(), "test");
        log("Generated value " + generator.getFieldValue());
        log("Generated value " + generator.getFieldValue());
        log("Generated value " + generator.getFieldValue());
    }

    public void testTransactionCommit() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean user = new UsersSQLBean();
        user.setFirstName("A");
        user.setLastName("Commit");
        user.setPassword("nothing special");
        user.setCompany("GlobalOmniCorp");
        SQLBeanTransaction transaction = dataSource.getTransaction();
        try {
            transaction.insert(user);
            transaction.insert(user);
            transaction.insert(user);
            assertTrue(transaction.commit());
        } catch (SQLBeanTransactionException sqlbte) {
            error("Inserts failed:");
            error(sqlbte.getMessage());
            transaction.rollback();
            fail();
        }
    }

    public void testTransactionRollback() {
        SQLBeanDataSource dataSource = new SQLBeanDataSource(new FakeDataSource(), true);
        UsersSQLBean user = new UsersSQLBean();
        user.setFirstName("A");
        user.setLastName("Rollback");
        user.setPassword("nothing special");
        user.setCompany("GlobalOmniCorp");
        SQLBeanTransaction transaction = dataSource.getTransaction();
        try {
            transaction.insert(user);
            transaction.insert(user);
            transaction.insert(user);
            assertTrue(transaction.rollback());
        } catch (SQLBeanTransactionException sqlbte) {
            error("Inserts failed:");
            error(sqlbte.getMessage());
            transaction.rollback();
            fail();
        }
    }

    public static Connection getConnection() throws Exception {
        Class.forName(JDBC_DRIVER_CLASS);
        return DriverManager.getConnection(JDBC_CONNECTION_STRING, JDBC_USER_NAME, JDBC_PASSWORD);
    }

    public static void log(Object o) {
        System.out.println("LOG: " + o);
    }

    public static void error(Object o) {
        System.out.println("ERROR: " + o);
    }

    public static class TestUserIdGenerator extends DateBasedIntegerGenerator {
    }

    public static class TestAddressesIdGenerator extends DateBasedIntegerGenerator {
    }

    public static class TestUserAddressesIdGenerator extends DateBasedIntegerGenerator {
    }

    public static class TestUserValidator implements SQLBeanValidator {

        public void validate(SQLBean sqlb) throws ValidateException {
            UsersSQLBean bean = null;
            try {
                bean = (UsersSQLBean) sqlb;
            } catch (ClassCastException e) {
                throw new ValidateException("Class cast exception during validation.  This is most likely a configuration problem.  Class recieved: " + sqlb.getClass().getName() + " Class expected: UsersSQLBean");
            }
            ValidateException ve = new ValidateException("Validation errors occured");
            if (bean.getFirstName().equals("nathan")) {
                ve.addError(bean.FIRST_NAME, "This is a silly validation rule which says no one named Nathan can be inserted to the users table.");
            }
            if (ve.hasErrors()) throw ve;
        }
    }

    public static class TestUserIdBasedComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            UsersSQLBean u1 = (UsersSQLBean) o1;
            UsersSQLBean u2 = (UsersSQLBean) o2;
            return u1.getUserId().intValue() - u2.getUserId().intValue();
        }
    }

    public static class FakeDataSource implements DataSource {

        public Connection getConnection() throws SQLException {
            try {
                Class.forName(JDBC_DRIVER_CLASS);
            } catch (Exception e) {
                throw new SQLException("Driver class " + JDBC_DRIVER_CLASS + " not found");
            }
            return DriverManager.getConnection(JDBC_CONNECTION_STRING, JDBC_USER_NAME, JDBC_PASSWORD);
        }

        public Connection getConnection(String username, String password) throws SQLException {
            try {
                Class.forName(JDBC_DRIVER_CLASS);
            } catch (Exception e) {
                throw new SQLException("Driver class " + JDBC_DRIVER_CLASS + " not found");
            }
            return DriverManager.getConnection(JDBC_CONNECTION_STRING, JDBC_USER_NAME, JDBC_PASSWORD);
        }

        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        public void setLoginTimeout(int seconds) throws SQLException {
        }

        public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        }
    }
}
