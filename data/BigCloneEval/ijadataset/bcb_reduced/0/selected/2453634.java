package org.xsocket.stream;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.QAUtil;
import org.xsocket.Synchronized;
import org.xsocket.stream.IServer;
import org.xsocket.stream.INonBlockingConnection;
import org.xsocket.stream.Server;
import org.xsocket.stream.IConnection.FlushMode;

/**
*
* @author grro@xsocket.org
*/
public final class DynamicHandlerTest {

    private static final String DELIMITER = "\r";

    @Test
    public void testInjextContext() throws Exception {
        DynamicHandler serverHandler = new DynamicHandler();
        IServer server = new Server(serverHandler);
        StreamUtils.start(server);
        IBlockingConnection connection = new BlockingConnection("localhost", server.getLocalPort());
        connection.setAutoflush(true);
        connection.write("test" + DELIMITER);
        String response = connection.readStringByDelimiter(DELIMITER);
        Assert.assertEquals(response, "test");
        Assert.assertNotNull(serverHandler.ctx);
        connection.close();
        server.close();
    }

    @Test
    public void testUnsync() throws Exception {
        List<String> errorList = new ArrayList<String>();
        final IServer server = new Server(new DynamicUnSyncHandler(errorList));
        StreamUtils.start(server);
        INonBlockingConnection connection = new NonBlockingConnection("localhost", server.getLocalPort());
        connection.setAutoflush(true);
        for (int j = 0; j < 30; j++) {
            connection.write("test" + DELIMITER);
            QAUtil.sleep(50);
        }
        QAUtil.sleep(500);
        connection.close();
        Assert.assertTrue(errorList.isEmpty());
        server.close();
    }

    private static final class EmptyHandler {
    }

    private static final class DynamicHandler {

        @Resource
        private IServerContext ctx = null;

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.setFlushmode(FlushMode.ASYNC);
            return false;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            connection.write(connection.readStringByDelimiter(DELIMITER) + DELIMITER);
            return true;
        }
    }

    @Synchronized(Synchronized.Mode.OFF)
    private static final class DynamicUnSyncHandler implements Cloneable {

        private int concurrent = 0;

        private int maxConcurrent = 0;

        private List<String> errorList = null;

        DynamicUnSyncHandler(List<String> errorList) {
            this.errorList = errorList;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            concurrent++;
            synchronized (this) {
                if (concurrent > maxConcurrent) {
                    maxConcurrent = concurrent;
                }
                connection.readAvailable();
            }
            QAUtil.sleep(200);
            concurrent--;
            return true;
        }

        public boolean onDisconnect(INonBlockingConnection connection) throws IOException {
            if (maxConcurrent < 2) {
                errorList.add(maxConcurrent + " connections");
            }
            return true;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            DynamicUnSyncHandler copy = (DynamicUnSyncHandler) super.clone();
            return copy;
        }
    }
}
