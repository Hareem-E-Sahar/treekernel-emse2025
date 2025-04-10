package testsuite.simple;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import testsuite.BaseTestCase;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXid;

/**
 * Unit tests for our XA implementation.
 * 
 * @version $Id: $
 */
public class XATest extends BaseTestCase {

    MysqlXADataSource xaDs;

    public XATest(String name) {
        super(name);
        this.xaDs = new MysqlXADataSource();
        this.xaDs.setUrl(BaseTestCase.dbUrl);
        this.xaDs.setRollbackOnPooledClose(true);
    }

    /**
	 * Tests that simple distributed transaction processing works as expected.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */
    public void testCoordination() throws Exception {
        if (!versionMeetsMinimum(5, 0)) {
            return;
        }
        createTable("testCoordination", "(field1 int) ENGINE=InnoDB");
        Connection conn1 = null;
        Connection conn2 = null;
        XAConnection xaConn1 = null;
        XAConnection xaConn2 = null;
        try {
            xaConn1 = getXAConnection();
            XAResource xaRes1 = xaConn1.getXAResource();
            conn1 = xaConn1.getConnection();
            xaConn2 = getXAConnection();
            XAResource xaRes2 = xaConn2.getXAResource();
            conn2 = xaConn2.getConnection();
            Xid xid1 = createXid();
            Xid xid2 = createXid(xid1);
            xaRes1.start(xid1, XAResource.TMNOFLAGS);
            xaRes2.start(xid2, XAResource.TMNOFLAGS);
            conn1.createStatement().executeUpdate("INSERT INTO testCoordination VALUES (1)");
            conn2.createStatement().executeUpdate("INSERT INTO testCoordination VALUES (2)");
            xaRes1.end(xid1, XAResource.TMSUCCESS);
            xaRes2.end(xid2, XAResource.TMSUCCESS);
            xaRes1.prepare(xid1);
            xaRes2.prepare(xid2);
            xaRes1.commit(xid1, false);
            xaRes2.commit(xid2, false);
            this.rs = this.stmt.executeQuery("SELECT field1 FROM testCoordination ORDER BY field1");
            assertTrue(this.rs.next());
            assertEquals(1, this.rs.getInt(1));
            assertTrue(this.rs.next());
            assertEquals(2, this.rs.getInt(1));
            this.stmt.executeUpdate("TRUNCATE TABLE testCoordination");
            xid1 = createXid();
            xid2 = createXid(xid1);
            xaRes1.start(xid1, XAResource.TMNOFLAGS);
            xaRes2.start(xid2, XAResource.TMNOFLAGS);
            conn1.createStatement().executeUpdate("INSERT INTO testCoordination VALUES (1)");
            assertEquals("1", getSingleIndexedValueWithQuery(conn1, 1, "SELECT field1 FROM testCoordination WHERE field1=1").toString());
            conn2.createStatement().executeUpdate("INSERT INTO testCoordination VALUES (2)");
            assertEquals("2", getSingleIndexedValueWithQuery(conn2, 1, "SELECT field1 FROM testCoordination WHERE field1=2").toString());
            xaRes1.end(xid1, XAResource.TMSUCCESS);
            xaRes2.end(xid2, XAResource.TMSUCCESS);
            xaRes1.prepare(xid1);
            xaRes2.prepare(xid2);
            xaRes1.rollback(xid1);
            xaRes2.rollback(xid2);
            this.rs = this.stmt.executeQuery("SELECT field1 FROM testCoordination ORDER BY field1");
            assertTrue(!this.rs.next());
        } finally {
            if (conn1 != null) {
                conn1.close();
            }
            if (conn2 != null) {
                conn2.close();
            }
            if (xaConn1 != null) {
                xaConn1.close();
            }
            if (xaConn2 != null) {
                xaConn2.close();
            }
        }
    }

    protected XAConnection getXAConnection() throws Exception {
        return this.xaDs.getXAConnection();
    }

    /**
	 * Tests that XA RECOVER works as expected.
	 * 
	 * @throws Exception
	 *             if test fails
	 */
    public void testRecover() throws Exception {
        if (!versionMeetsMinimum(5, 0)) {
            return;
        }
        XAConnection xaConn = null, recoverConn = null;
        try {
            xaConn = getXAConnection();
            Connection c = xaConn.getConnection();
            Xid xid = createXid();
            XAResource xaRes = xaConn.getXAResource();
            xaRes.start(xid, XAResource.TMNOFLAGS);
            c.createStatement().executeQuery("SELECT 1");
            xaRes.end(xid, XAResource.TMSUCCESS);
            xaRes.prepare(xid);
            recoverConn = getXAConnection();
            XAResource recoverRes = recoverConn.getXAResource();
            Xid[] recoveredXids = recoverRes.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);
            assertTrue(recoveredXids != null);
            assertTrue(recoveredXids.length > 0);
            boolean xidFound = false;
            for (int i = 0; i < recoveredXids.length; i++) {
                if (recoveredXids[i] != null && recoveredXids[i].equals(xid)) {
                    xidFound = true;
                    break;
                }
            }
            assertTrue(xidFound);
            recoverRes = recoverConn.getXAResource();
            recoveredXids = recoverRes.recover(XAResource.TMSTARTRSCAN);
            assertTrue(recoveredXids != null);
            assertTrue(recoveredXids.length > 0);
            xidFound = false;
            for (int i = 0; i < recoveredXids.length; i++) {
                if (recoveredXids[i] != null && recoveredXids[i].equals(xid)) {
                    xidFound = true;
                    break;
                }
            }
            assertTrue(xidFound);
            recoverRes.recover(XAResource.TMSTARTRSCAN);
            recoverRes.recover(XAResource.TMENDRSCAN);
            recoverRes.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);
            try {
                recoverRes.recover(XAResource.TMSUCCESS);
                fail("XAException should have been thrown");
            } catch (XAException xaEx) {
                assertEquals(XAException.XAER_INVAL, xaEx.errorCode);
            }
        } finally {
            if (xaConn != null) {
                xaConn.close();
            }
            if (recoverConn != null) {
                recoverConn.close();
            }
        }
    }

    /**
	 * Tests operation of local transactions on XAConnections when global
	 * transactions are in or not in progress (follows from BUG#17401).
	 * 
	 * @throws Exception
	 *             if the testcase fails
	 */
    public void testLocalTransaction() throws Exception {
        if (!versionMeetsMinimum(5, 0) || isRunningOnJdk131()) {
            return;
        }
        createTable("testLocalTransaction", "(field1 int) ENGINE=InnoDB");
        Connection conn1 = null;
        XAConnection xaConn1 = null;
        try {
            xaConn1 = getXAConnection();
            XAResource xaRes1 = xaConn1.getXAResource();
            conn1 = xaConn1.getConnection();
            assertEquals(true, conn1.getAutoCommit());
            conn1.setAutoCommit(true);
            conn1.createStatement().executeUpdate("INSERT INTO testLocalTransaction VALUES (1)");
            assertEquals("1", getSingleIndexedValueWithQuery(conn1, 1, "SELECT field1 FROM testLocalTransaction").toString());
            conn1.createStatement().executeUpdate("TRUNCATE TABLE testLocalTransaction");
            conn1.setAutoCommit(false);
            conn1.createStatement().executeUpdate("INSERT INTO testLocalTransaction VALUES (2)");
            assertEquals("2", getSingleIndexedValueWithQuery(conn1, 1, "SELECT field1 FROM testLocalTransaction").toString());
            conn1.rollback();
            assertEquals(0, getRowCount("testLocalTransaction"));
            conn1.createStatement().executeUpdate("INSERT INTO testLocalTransaction VALUES (3)");
            assertEquals("3", getSingleIndexedValueWithQuery(conn1, 1, "SELECT field1 FROM testLocalTransaction").toString());
            conn1.commit();
            assertEquals("3", getSingleIndexedValueWithQuery(conn1, 1, "SELECT field1 FROM testLocalTransaction").toString());
            conn1.commit();
            Savepoint sp = conn1.setSavepoint();
            conn1.rollback(sp);
            sp = conn1.setSavepoint("abcd");
            conn1.rollback(sp);
            Savepoint spSaved = sp;
            Xid xid = createXid();
            xaRes1.start(xid, XAResource.TMNOFLAGS);
            try {
                try {
                    conn1.setAutoCommit(true);
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
                try {
                    conn1.commit();
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
                try {
                    conn1.rollback();
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
                try {
                    sp = conn1.setSavepoint();
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
                try {
                    conn1.rollback(spSaved);
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
                try {
                    sp = conn1.setSavepoint("abcd");
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
                try {
                    conn1.rollback(spSaved);
                } catch (SQLException sqlEx) {
                    assertEquals("2D000", sqlEx.getSQLState());
                }
            } finally {
                xaRes1.forget(xid);
            }
        } finally {
            if (xaConn1 != null) {
                xaConn1.close();
            }
        }
    }

    public void testSuspendableTx() throws Exception {
        if (!versionMeetsMinimum(5, 0) || isRunningOnJdk131()) {
            return;
        }
        Connection conn1 = null;
        MysqlXADataSource suspXaDs = new MysqlXADataSource();
        suspXaDs.setUrl(BaseTestCase.dbUrl);
        suspXaDs.setPinGlobalTxToPhysicalConnection(true);
        suspXaDs.setRollbackOnPooledClose(true);
        XAConnection xaConn1 = null;
        Xid xid = createXid();
        try {
            xaConn1 = suspXaDs.getXAConnection();
            XAResource xaRes1 = xaConn1.getXAResource();
            conn1 = xaConn1.getConnection();
            xaRes1.start(xid, XAResource.TMNOFLAGS);
            conn1.createStatement().executeQuery("SELECT 1");
            xaRes1.end(xid, XAResource.TMSUCCESS);
            xaRes1.start(xid, XAResource.TMRESUME);
            conn1.createStatement().executeQuery("SELECT 1");
            xaRes1.end(xid, XAResource.TMSUCCESS);
            xaRes1.commit(xid, true);
            xaConn1.close();
            xaConn1 = suspXaDs.getXAConnection();
            xaRes1 = xaConn1.getXAResource();
            conn1 = xaConn1.getConnection();
            xaRes1.start(xid, XAResource.TMNOFLAGS);
            conn1.createStatement().executeQuery("SELECT 1");
            xaRes1.end(xid, XAResource.TMSUCCESS);
            xaRes1.start(xid, XAResource.TMJOIN);
            conn1.createStatement().executeQuery("SELECT 1");
            xaRes1.end(xid, XAResource.TMSUCCESS);
            xaRes1.commit(xid, true);
        } finally {
            if (xaConn1 != null) {
                xaConn1.close();
            }
        }
    }

    private Xid createXid() throws IOException {
        ByteArrayOutputStream gtridOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(gtridOut);
        new UID().write(dataOut);
        final byte[] gtrid = gtridOut.toByteArray();
        ByteArrayOutputStream bqualOut = new ByteArrayOutputStream();
        dataOut = new DataOutputStream(bqualOut);
        new UID().write(dataOut);
        final byte[] bqual = bqualOut.toByteArray();
        Xid xid = new MysqlXid(gtrid, bqual, 3306);
        return xid;
    }

    private Xid createXid(Xid xidToBranch) throws IOException {
        ByteArrayOutputStream bqualOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bqualOut);
        new UID().write(dataOut);
        final byte[] bqual = bqualOut.toByteArray();
        Xid xid = new MysqlXid(xidToBranch.getGlobalTransactionId(), bqual, 3306);
        return xid;
    }
}
