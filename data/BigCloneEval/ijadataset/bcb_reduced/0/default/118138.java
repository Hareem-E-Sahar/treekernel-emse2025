import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AdvancedUserTest {

    private static Connection conn;

    private static Statement stmt;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
            String url = "jdbc:odbc:BearTracConn";
            conn = DriverManager.getConnection(url, "username", "password");
            stmt = conn.createStatement();
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        try {
            stmt.close();
            conn.close();
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAdvancedUser() {
    }

    @Test
    public void testAdvancedUserStatementInt() {
    }

    @Test
    public void testAdvancedUserStatementIntInt() {
    }

    @Test
    public void testIsAdvancedUser() {
        int enteredUserID = 900030001;
        Assert.assertTrue("Case 1: Advanced User, userID = 900030001", AdvancedUser.isAdvancedUser(stmt, enteredUserID));
        enteredUserID = 900077001;
        Assert.assertFalse("Case 1: Not Advanced User, userID = 900077001", AdvancedUser.isAdvancedUser(stmt, enteredUserID));
    }

    @Test
    public void testGetAdvancedUserGroupID() {
        int enteredUserID = 900030001;
        Assert.assertEquals("Case 1: Advanced User, userID = 900030001", 3, AdvancedUser.getAdvancedUserGroupID(stmt, enteredUserID), 0);
        enteredUserID = 900077001;
        Assert.assertEquals("Case 2: Advanced User, userID = 900077001", 1, AdvancedUser.getAdvancedUserGroupID(stmt, enteredUserID), 0);
    }

    @Test
    public void testAddAdvancedUser() {
        int enteredUserID = 900037699;
        int groupID = 2;
        AdvancedUser.addAdvancedUser(stmt, enteredUserID, groupID);
        Assert.assertEquals("Case 1: Advanced User, userID = 900037699, userGroup = 2", 2, AdvancedUser.getAdvancedUserGroupID(stmt, enteredUserID), 0);
    }

    @Test
    public void testDeleteAdvancedUser() {
        int enteredUserID = 900037699;
        AdvancedUser.deleteAdvancedUser(stmt, enteredUserID);
        Assert.assertFalse("Case 1: Not Advanced User, userID = 900037699", AdvancedUser.isAdvancedUser(stmt, enteredUserID));
    }

    @Test
    public void testUpdateGroupID() {
        int enteredUserID = 900030001;
        int groupID = 2;
        AdvancedUser.updateGroupID(stmt, enteredUserID, groupID);
        Assert.assertEquals("Case 1: Advanced User, userID = 900030001", 2, AdvancedUser.getAdvancedUserGroupID(stmt, enteredUserID), 0);
        groupID = 3;
        AdvancedUser.updateGroupID(stmt, enteredUserID, groupID);
        Assert.assertEquals("Case 1: Advanced User, userID = 900030001", 3, AdvancedUser.getAdvancedUserGroupID(stmt, enteredUserID), 0);
    }
}
