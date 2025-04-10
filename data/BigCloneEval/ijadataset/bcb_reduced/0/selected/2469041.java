package org.hsqldb.auth;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;
import org.hsqldb.jdbc.JDBCArrayBasic;
import org.hsqldb.types.Type;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import org.hsqldb.lib.FrameworkLogger;

public class JaasAuthBeanTest extends junit.framework.TestCase {

    private static FrameworkLogger logger = FrameworkLogger.getLog(JaasAuthBeanTest.class);

    private static final String cfgResourcePath = "/org/hsqldb/resources/jaas.cfg";

    protected String appDbUrl = "jdbc:hsqldb:mem:appDb";

    private static File jaasCfgFile;

    protected Connection saCon;

    protected Statement saSt;

    private String savedLoginConfig;

    protected AuthBeanMultiplexer plexer;

    protected void setUp() throws SQLException {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception e) {
        }
        saCon = DriverManager.getConnection(appDbUrl, "SA", "");
        saCon.setAutoCommit(false);
        saSt = saCon.createStatement();
        saSt.executeUpdate("CREATE ROLE role1");
        saSt.executeUpdate("CREATE ROLE role2");
        saSt.executeUpdate("CREATE ROLE role3");
        saSt.executeUpdate("CREATE ROLE role4");
        saSt.executeUpdate("CREATE SCHEMA s1");
        saSt.executeUpdate("CREATE SCHEMA s2");
        saSt.executeUpdate("CREATE SCHEMA s3");
        saSt.executeUpdate("SET DATABASE AUTHENTICATION FUNCTION EXTERNAL NAME " + "'CLASSPATH:" + "org.hsqldb.auth.AuthBeanMultiplexer.authenticate'");
        plexer = AuthBeanMultiplexer.getSingleton();
        plexer.clear();
        if (jaasCfgFile == null) {
            int i;
            byte[] copyBuffer = new byte[512];
            InputStream iStream = null;
            OutputStream oStream = null;
            try {
                iStream = getClass().getResourceAsStream(cfgResourcePath);
                if (iStream == null) throw new IOException("Failed to read resource: " + cfgResourcePath);
                jaasCfgFile = File.createTempFile(getClass().getName().replaceFirst(".*\\.", ""), ".jaascfg");
                jaasCfgFile.deleteOnExit();
                oStream = new FileOutputStream(jaasCfgFile);
                while ((i = iStream.read(copyBuffer)) > -1) oStream.write(copyBuffer, 0, i);
            } catch (IOException ioe) {
                logger.severe("Failed to prepare JAAS config file in local " + "file system", ioe);
                throw new IllegalStateException("Failed to prepare JAAS " + "config file in local file system", ioe);
            } finally {
                try {
                    if (oStream != null) {
                        oStream.close();
                        oStream = null;
                    }
                    if (iStream != null) {
                        iStream.close();
                        iStream = null;
                    }
                } catch (IOException ioe) {
                    logger.error("Failed to clear file objects");
                }
            }
        }
        savedLoginConfig = System.getProperty("java.security.auth.login.config");
        System.setProperty("java.security.auth.login.config", jaasCfgFile.getAbsolutePath());
    }

    protected void tearDown() {
        if (savedLoginConfig == null) {
            System.getProperties().remove("java.security.auth.login.config");
        } else {
            System.setProperty("java.security.auth.login.config", savedLoginConfig);
        }
        if (saSt != null) try {
            saSt.executeUpdate("SHUTDOWN");
        } catch (SQLException se) {
            logger.error("Tear-down of setup Conn. failed:" + se);
        }
        if (saSt != null) try {
            saSt.close();
        } catch (SQLException se) {
            logger.error("Close of setup Statement failed:" + se);
        } finally {
            saSt = null;
        }
        if (saCon != null) try {
            saCon.close();
        } catch (SQLException se) {
            logger.error("Close of setup Conn. failed:" + se);
        } finally {
            saCon = null;
        }
    }

    public void testViaPrincipals() throws SQLException {
        JaasAuthBean jaasBean = new JaasAuthBean();
        jaasBean.setApplicationKey("test");
        jaasBean.setRoleSchemaValuePattern(Pattern.compile("RS:(.+)"));
        jaasBean.init();
        plexer.setAuthFunctionBeans(saCon, Arrays.asList(new AuthFunctionBean[] { jaasBean }));
        Connection authedCon = null;
        Statement st = null;
        boolean ok = false;
        try {
            try {
                authedCon = DriverManager.getConnection(appDbUrl, "alpha", "alpha");
            } catch (SQLException se) {
                ok = true;
            }
            if (!ok) {
                fail("Access allowed even though password starts with 'a'");
            }
            try {
                authedCon = DriverManager.getConnection(appDbUrl, "alpha", "beta");
            } catch (SQLException se) {
                fail("Access denied for alpha/beta: " + se);
            }
            st = authedCon.createStatement();
            assertEquals(new HashSet<String>(Arrays.asList(new String[] { "ROLE2" })), AuthUtils.getEnabledRoles(authedCon));
            assertEquals("S1", AuthUtils.getInitialSchema(authedCon));
        } finally {
            if (st != null) try {
                st.close();
            } catch (SQLException se) {
                logger.error("Close of Statement failed:" + se);
            } finally {
                st = null;
            }
            if (authedCon != null) try {
                authedCon.close();
            } catch (SQLException se) {
                logger.error("Close of Conn. failed:" + se);
            } finally {
                authedCon = null;
            }
        }
    }

    public void testViaCredentials() throws SQLException {
        JaasAuthBean jaasBean = new JaasAuthBean();
        jaasBean.setApplicationKey("test");
        jaasBean.setRoleSchemaValuePattern(Pattern.compile("RS:(.+)"));
        jaasBean.setRoleSchemaViaCredential(true);
        jaasBean.init();
        plexer.setAuthFunctionBeans(saCon, Arrays.asList(new AuthFunctionBean[] { jaasBean }));
        Connection authedCon = null;
        Statement st = null;
        boolean ok = false;
        try {
            try {
                authedCon = DriverManager.getConnection(appDbUrl, "alpha", "alpha");
            } catch (SQLException se) {
                ok = true;
            }
            if (!ok) {
                fail("Access allowed even though password starts with 'a'");
            }
            try {
                authedCon = DriverManager.getConnection(appDbUrl, "alpha", "beta");
            } catch (SQLException se) {
                fail("Access denied for alpha/beta: " + se);
            }
            st = authedCon.createStatement();
            assertEquals(new HashSet<String>(Arrays.asList(new String[] { "CHANGE_AUTHORIZATION", "ROLE1" })), AuthUtils.getEnabledRoles(authedCon));
            assertEquals(null, AuthUtils.getInitialSchema(authedCon));
        } finally {
            if (st != null) try {
                st.close();
            } catch (SQLException se) {
                logger.error("Close of Statement failed:" + se);
            } finally {
                st = null;
            }
            if (authedCon != null) try {
                authedCon.close();
            } catch (SQLException se) {
                logger.error("Close of Conn. failed:" + se);
            } finally {
                authedCon = null;
            }
        }
    }

    /**
     * This method allows to easily run this unit test independent of the other
     * unit tests, and without dealing with Ant or unrelated test suites.
     */
    public static void main(String[] sa) {
        junit.textui.TestRunner runner = new junit.textui.TestRunner();
        junit.framework.TestResult result = runner.run(runner.getTest(JaasAuthBeanTest.class.getName()));
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
