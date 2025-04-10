package org.xsocket.connection;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.QAUtil;
import org.xsocket.connection.INonBlockingConnection;

/**
*
* @author grro@xsocket.org
*/
public final class HandlerChainCallbackOrderTest {

    private final AtomicInteger running = new AtomicInteger();

    private final AtomicBoolean errorOccured = new AtomicBoolean(false);

    @Test
    public void testSimple() throws Exception {
        HandlerChain chain = new HandlerChain();
        chain.addLast(new ConnectHandler());
        DataHandler dh = new DataHandler();
        chain.addLast(dh);
        final IServer server = new Server(chain);
        server.start();
        for (int i = 0; i < 5; i++) {
            new Thread() {

                @Override
                public void run() {
                    running.incrementAndGet();
                    try {
                        for (int j = 0; j < 200; j++) {
                            IBlockingConnection con = new BlockingConnection("localhost", server.getLocalPort());
                            con.write("test1\r\n");
                            Assert.assertEquals("test1", con.readStringByDelimiter("\r\n"));
                            con.close();
                            System.out.print(".");
                        }
                    } catch (Throwable t) {
                        errorOccured.set(true);
                    } finally {
                        running.decrementAndGet();
                    }
                }
            }.start();
        }
        while (running.get() > 0) {
            QAUtil.sleep(100);
        }
        Assert.assertFalse(errorOccured.get());
        Assert.assertFalse(dh.isErrorOccured());
        server.close();
    }

    private static final class ConnectHandler implements IConnectHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            connection.setAttachment("Hello handler");
            return true;
        }
    }

    private static final class DataHandler implements IDataHandler {

        private final AtomicBoolean isErrorOccured = new AtomicBoolean(false);

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException, ClosedChannelException, MaxReadSizeExceededException {
            String attachment = (String) connection.getAttachment();
            if (attachment == null) {
                isErrorOccured.set(true);
                System.out.println("error");
            }
            int available = connection.available();
            if (available > 0) {
                connection.write(connection.readByteBufferByLength(available));
            }
            return true;
        }

        public boolean isErrorOccured() {
            return isErrorOccured.get();
        }
    }
}
