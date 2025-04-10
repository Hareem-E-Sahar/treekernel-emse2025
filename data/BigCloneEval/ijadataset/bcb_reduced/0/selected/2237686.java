package org.xsocket.connection;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import org.junit.Assert;
import org.junit.Test;
import org.xsocket.Execution;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.QAUtil;
import org.xsocket.connection.BlockingConnection;
import org.xsocket.connection.IBlockingConnection;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;
import org.xsocket.connection.IConnection.FlushMode;

/**
*
* @author grro@xsocket.org
*/
public final class MixedThreadedTest {

    private static final String DELIMITER = "\r";

    @Test
    public void testMixThreaded() throws Exception {
        Handler serverHandler = new Handler();
        IServer server = new Server(serverHandler);
        server.setFlushmode(FlushMode.ASYNC);
        server.start();
        IBlockingConnection connection = new BlockingConnection("localhost", server.getLocalPort());
        connection.setAutoflush(true);
        QAUtil.sleep(2000);
        Assert.assertEquals(1, serverHandler.countOnConnect);
        Assert.assertEquals(0, serverHandler.countOnData);
        Assert.assertFalse(serverHandler.threadName.startsWith("xDispatcher"));
        connection.write("test" + DELIMITER);
        QAUtil.sleep(2000);
        Assert.assertEquals(1, serverHandler.countOnConnect);
        Assert.assertEquals(1, serverHandler.countOnData);
        Assert.assertTrue(serverHandler.threadName.startsWith("xDispatcher"));
        connection.close();
        server.close();
    }

    @Execution(Execution.NONTHREADED)
    private static final class Handler implements IConnectHandler, IDataHandler {

        private String threadName = null;

        private int countOnConnect = 0;

        private int countOnData = 0;

        @Execution(Execution.MULTITHREADED)
        public boolean onConnect(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            threadName = Thread.currentThread().getName();
            countOnConnect++;
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            threadName = Thread.currentThread().getName();
            countOnData++;
            connection.write(connection.readStringByDelimiter(DELIMITER) + DELIMITER);
            return true;
        }
    }
}
