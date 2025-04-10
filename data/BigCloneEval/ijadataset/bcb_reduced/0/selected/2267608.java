package org.perfmon4j.extras.tomcat55;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import org.mockito.Mockito;
import org.perfmon4j.util.JDBCHelper;

public class ThreadPoolMonitorImplTest extends SQLTest {

    final String DERBY_CREATE_1 = "CREATE TABLE p4j.P4JThreadPoolMonitor(\r\n" + "	ThreadPoolOwner VARCHAR(50) NOT NULL," + "	InstanceName VARCHAR(200) NOT NULL,\r\n" + "	StartTime TIMESTAMP NOT NULL,\r\n" + "	EndTime TIMESTAMP NOT NULL,\r\n" + "	Duration INT NOT NULL,\r\n" + "	CurrentThreadsBusy INT NOT NULL,\r\n" + "	CurrentThreadCount INT NOT NULL\r\n" + ")\r\n";

    final String DERBY_DROP_1 = "DROP TABLE p4j.P4JThreadPoolMonitor";

    private Connection conn;

    protected void setUp() throws Exception {
        super.setUp();
        conn = appender.getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(DERBY_CREATE_1);
        } finally {
            JDBCHelper.closeNoThrow(stmt);
        }
    }

    protected void tearDown() throws Exception {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(DERBY_DROP_1);
        } finally {
            JDBCHelper.closeNoThrow(stmt);
        }
        super.tearDown();
    }

    public void testDoIt() throws Exception {
        ThreadPoolMonitorImpl.SQLWriter writer = new ThreadPoolMonitorImpl.SQLWriter();
        ThreadPoolMonitor data = Mockito.mock(ThreadPoolMonitor.class);
        long start = System.currentTimeMillis();
        long end = start + 60000;
        Mockito.when(data.getStartTime()).thenReturn(new Long(start));
        Mockito.when(data.getStartTime()).thenReturn(new Long(end));
        Mockito.when(data.getInstanceName()).thenReturn("HTTP");
        Mockito.when(data.getCurrentThreadsBusy()).thenReturn(new Long(25));
        Mockito.when(data.getCurrentThreadCount()).thenReturn(new Long(125));
        writer.writeToSQL(conn, "p4j", data);
        final String VALIDATE_SQL = "SELECT COUNT(*) FROM p4j.P4JThreadPoolMonitor " + " WHERE ThreadPoolOwner=? " + " AND InstanceName=? " + " AND StartTime=? " + " AND EndTime=? " + " AND Duration=? " + " AND CurrentThreadsBusy=? " + " AND CurrentThreadCount=? ";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(VALIDATE_SQL);
            stmt.setString(1, "Apache/Tomcat");
            stmt.setString(2, "HTTP");
            stmt.setTimestamp(3, new Timestamp(data.getStartTime()));
            stmt.setTimestamp(4, new Timestamp(data.getEndTime()));
            stmt.setLong(5, data.getDuration());
            stmt.setLong(6, data.getCurrentThreadsBusy());
            stmt.setLong(7, data.getCurrentThreadCount());
            long resultCount = JDBCHelper.getQueryCount(stmt);
            assertEquals("Should have inserted row", 1, resultCount);
        } finally {
            JDBCHelper.closeNoThrow(stmt);
        }
    }
}
