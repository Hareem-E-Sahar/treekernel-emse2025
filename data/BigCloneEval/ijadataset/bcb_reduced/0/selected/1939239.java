package net.sourceforge.jtds.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.Properties;
import java.util.Enumeration;
import java.sql.*;
import net.sourceforge.jtds.jdbc.ConnectionJDBC2;
import net.sourceforge.jtds.jdbc.Driver;
import net.sourceforge.jtds.jdbc.DefaultProperties;
import net.sourceforge.jtds.jdbc.Messages;

/**
 * Unit test for the {@link ConnectionJDBC2} class.
 *
 * @author David Kilzer
 * @author Alin Sinpalean
 * @version $Id: ConnectionJDBC2UnitTest.java,v 1.1.1.1 2007/01/24 20:51:55 mwillett Exp $
 */
public class ConnectionJDBC2UnitTest extends UnitTestBase {

    /**
     * Construct a test suite for this class.
     * <p/>
     * The test suite includes the tests in this class, and adds tests
     * from {@link DefaultPropertiesTestLibrary} after creating an
     * anonymous {@link DefaultPropertiesTester} object.
     *
     * @return The test suite to run.
     */
    public static Test suite() {
        final TestSuite testSuite = new TestSuite(ConnectionJDBC2UnitTest.class);
        testSuite.addTest(ConnectionJDBC2UnitTest.Test_ConnectionJDBC2_unpackProperties.suite("test_unpackProperties_DefaultProperties"));
        return testSuite;
    }

    /**
     * Constructor.
     *
     * @param name The name of the test.
     */
    public ConnectionJDBC2UnitTest(String name) {
        super(name);
    }

    /**
     * Test that an {@link java.sql.SQLException} is thrown when
     * parsing invalid integer (and long) properties.
     */
    public void test_unpackProperties_invalidIntegerProperty() {
        assertSQLExceptionForBadWholeNumberProperty(Driver.PORTNUMBER);
        assertSQLExceptionForBadWholeNumberProperty(Driver.SERVERTYPE);
        assertSQLExceptionForBadWholeNumberProperty(Driver.PREPARESQL);
        assertSQLExceptionForBadWholeNumberProperty(Driver.PACKETSIZE);
        assertSQLExceptionForBadWholeNumberProperty(Driver.LOGINTIMEOUT);
        assertSQLExceptionForBadWholeNumberProperty(Driver.LOBBUFFER);
    }

    /**
     * Assert that an SQLException is thrown when
     * {@link ConnectionJDBC2#unpackProperties(Properties)} is called
     * with an invalid integer (or long) string set on a property.
     * <p/>
     * Note that because Java 1.3 is still supported, the
     * {@link RuntimeException} that is caught may not contain the
     * original {@link Throwable} cause, only the original message.
     *
     * @param key The message key used to retrieve the property name.
     */
    private void assertSQLExceptionForBadWholeNumberProperty(final String key) {
        final ConnectionJDBC2 instance = (ConnectionJDBC2) invokeConstructor(ConnectionJDBC2.class, new Class[] {}, new Object[] {});
        Properties properties = (Properties) invokeStaticMethod(Driver.class, "parseURL", new Class[] { String.class, Properties.class }, new Object[] { "jdbc:jtds:sqlserver://servername", new Properties() });
        properties = (Properties) invokeStaticMethod(DefaultProperties.class, "addDefaultProperties", new Class[] { Properties.class }, new Object[] { properties });
        properties.setProperty(Messages.get(key), "1.21 Gigawatts");
        try {
            invokeInstanceMethod(instance, "unpackProperties", new Class[] { Properties.class }, new Object[] { properties });
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertEquals("Unexpected exception message", Messages.get("error.connection.badprop", Messages.get(key)), e.getMessage());
        }
    }

    /**
     * Class used to test {@link net.sourceforge.jtds.jdbc.ConnectionJDBC2#unpackProperties(Properties)}.
     */
    public static class Test_ConnectionJDBC2_unpackProperties extends DefaultPropertiesTestLibrary {

        /**
         * Construct a test suite for this library.
         *
         * @param name The name of the tests.
         * @return The test suite.
         */
        public static Test suite(String name) {
            return new TestSuite(ConnectionJDBC2UnitTest.Test_ConnectionJDBC2_unpackProperties.class, name);
        }

        /**
         * Default constructor.
         */
        public Test_ConnectionJDBC2_unpackProperties() {
            setTester(new DefaultPropertiesTester() {

                public void assertDefaultProperty(String message, String url, Properties properties, String fieldName, String key, String expected) {
                    {
                        if ("sendStringParametersAsUnicode".equals(fieldName)) {
                            fieldName = "useUnicode";
                        }
                    }
                    Properties parsedProperties = (Properties) invokeStaticMethod(Driver.class, "parseURL", new Class[] { String.class, Properties.class }, new Object[] { url, properties });
                    parsedProperties = (Properties) invokeStaticMethod(DefaultProperties.class, "addDefaultProperties", new Class[] { Properties.class }, new Object[] { parsedProperties });
                    ConnectionJDBC2 instance = (ConnectionJDBC2) invokeConstructor(ConnectionJDBC2.class, new Class[] {}, new Object[] {});
                    invokeInstanceMethod(instance, "unpackProperties", new Class[] { Properties.class }, new Object[] { parsedProperties });
                    String actual = String.valueOf(invokeGetInstanceField(instance, fieldName));
                    {
                        if ("tdsVersion".equals(fieldName)) {
                            expected = String.valueOf(DefaultProperties.getTdsVersion(expected));
                        }
                    }
                    assertEquals(message, expected, actual);
                }
            });
        }
    }

    /**
     * Creates a <code>Connection</code>, overriding the default properties
     * with the ones provided.
     *
     * @param override the overriding properties
     * @return a <code>Connection</code> object
     */
    private Connection getConnectionOverrideProperties(Properties override) throws Exception {
        Properties props = (Properties) TestBase.props.clone();
        for (Enumeration e = override.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            props.setProperty(key, override.getProperty(key));
        }
        Class.forName(props.getProperty("driver"));
        String url = props.getProperty("url");
        return DriverManager.getConnection(url, props);
    }

    /**
     * Test correct behavior of the <code>charset</code> property.
     * Values should be stored and retrieved using the requested charset rather
     * than the server's as long as Unicode is not used.
     */
    public void testForceCharset1() throws Exception {
        Properties props = new Properties();
        props.setProperty(Messages.get(Driver.CHARSET), "Cp1251");
        props.setProperty(Messages.get(Driver.SENDSTRINGPARAMETERSASUNICODE), "false");
        Connection con = getConnectionOverrideProperties(props);
        try {
            String value = "АБВ";
            PreparedStatement pstmt = con.prepareStatement("select ?");
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            assertEquals(value, rs.getString(1));
            assertFalse(rs.next());
            rs.close();
            pstmt.close();
        } finally {
            con.close();
        }
    }

    /**
     * Test correct behavior of the <code>charset</code> property.
     * Stored procedure output parameters should be decoded using the specified
     * charset rather than the server's as long as they are non-Unicode.
     */
    public void testForceCharset2() throws Exception {
        Properties props = new Properties();
        props.setProperty(Messages.get(Driver.CHARSET), "Cp1251");
        props.setProperty(Messages.get(Driver.SENDSTRINGPARAMETERSASUNICODE), "false");
        Connection con = getConnectionOverrideProperties(props);
        try {
            Statement stmt = con.createStatement();
            assertEquals(0, stmt.executeUpdate("create procedure #testForceCharset2 " + "@inParam varchar(10), @outParam varchar(10) output as " + "set @outParam = @inParam"));
            stmt.close();
            String value = "АБВ";
            CallableStatement cstmt = con.prepareCall("{call #testForceCharset2(?, ?)}");
            cstmt.setString(1, value);
            cstmt.registerOutParameter(2, Types.VARCHAR);
            assertEquals(0, cstmt.executeUpdate());
            assertEquals(value, cstmt.getString(2));
            cstmt.close();
        } finally {
            con.close();
        }
    }

    /**
     * Test for bug [1296482] setAutoCommit() behaviour.
     * <p/>
     * The behaviour of setAutoCommit() on ConnectionJDBC2 is inconsistent with
     * the Sun JDBC 3.0 Specification. JDBC 3.0 Specification, section 10.1.1:
     * <blockquote>"If the value of auto-commit is changed in the middle of a
     * transaction, the current transaction is committed."</blockquote>
     */
    public void testAutoCommit() throws Exception {
        Connection con = getConnectionOverrideProperties(new Properties());
        try {
            Statement stmt = con.createStatement();
            assertEquals(0, stmt.executeUpdate("create table #testAutoCommit (i int)"));
            con.setAutoCommit(false);
            assertEquals(1, stmt.executeUpdate("insert into #testAutoCommit (i) values (0)"));
            con.setAutoCommit(false);
            con.rollback();
            assertEquals(1, stmt.executeUpdate("insert into #testAutoCommit (i) values (1)"));
            con.setAutoCommit(true);
            con.setAutoCommit(false);
            con.rollback();
            con.setAutoCommit(true);
            ResultSet rs = stmt.executeQuery("select i from #testAutoCommit");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            assertFalse(rs.next());
            rs.close();
            stmt.close();
        } finally {
            con.close();
        }
    }
}
