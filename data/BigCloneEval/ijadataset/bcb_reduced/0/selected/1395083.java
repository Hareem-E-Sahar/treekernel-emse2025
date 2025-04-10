package org.xsocket.stream;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.stream.BlockingConnection;
import org.xsocket.stream.IBlockingConnection;
import org.xsocket.stream.IConnectHandler;
import org.xsocket.stream.IConnectionScoped;
import org.xsocket.stream.IDataHandler;
import org.xsocket.stream.IServer;
import org.xsocket.stream.INonBlockingConnection;
import org.xsocket.stream.Server;

/**
*
* @author grro@xsocket.org
*/
public final class DataTypesTest {

    private static final Logger LOG = Logger.getLogger(DataTypesTest.class.getName());

    @Test
    public void testShort() throws Exception {
        IServer server = new Server(new ShortHandler());
        StreamUtils.start(server);
        IBlockingConnection connection = new BlockingConnection("localhost", server.getLocalPort());
        connection.write((short) 4);
        Assert.assertEquals(connection.readShort(), (short) 4);
        connection.close();
        server.close();
    }

    @Test
    public void testMixed() throws Exception {
        IServer server = new Server(new DataTypesTestServerHandler());
        StreamUtils.start(server);
        IBlockingConnection connection = new BlockingConnection("localhost", server.getLocalPort());
        connection.setAutoflush(false);
        double d = connection.readDouble();
        Assert.assertTrue("received value ist not excepted value ", d == 45.45);
        int i = connection.readInt();
        Assert.assertTrue("received value ist not excepted value ", i == 56);
        long l = connection.readLong();
        Assert.assertTrue("received value ist not excepted value ", l == 11);
        byte[] bytes = connection.readBytesByDelimiter("r", Integer.MAX_VALUE);
        byte[] expected = new byte[] { 2, 78, 45, 78, 23, 11, 45, 78, 12, 56 };
        for (int j = 0; j < bytes.length; j++) {
            if (bytes[j] != expected[j]) {
                Assert.fail("received value ist not excepted value ");
            }
        }
        String w = connection.readStringByLength(5);
        Assert.assertEquals(w, "hello");
        connection.write(33.33);
        connection.flush();
        connection.write((int) 11);
        connection.flush();
        connection.write((long) 33);
        connection.flush();
        connection.write("\r\n");
        connection.flush();
        connection.write("this is the other end tt");
        connection.flush();
        connection.write((byte) 34);
        connection.flush();
        connection.write(bytes);
        connection.flush();
        connection.write("r");
        connection.flush();
        connection.write("you");
        connection.flush();
        double d2 = connection.readDouble();
        Assert.assertTrue("received value ist not excepted value ", d2 == 65.65);
        server.close();
    }

    private static final class DataTypesTestServerHandler implements IDataHandler, IConnectHandler, IConnectionScoped {

        int state = 0;

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.setAutoflush(false);
            connection.write(45.45);
            connection.write((int) 56);
            connection.write((long) 11);
            connection.write(new byte[] { 2, 78, 45, 78, 23, 11, 45, 78, 12, 56 });
            connection.write("r");
            connection.write("hello");
            connection.flush();
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException {
            do {
                switch(state) {
                    case 0:
                        double d = connection.readDouble();
                        Assert.assertTrue("received value ist not excepted value ", d == 33.33);
                        LOG.fine("double received");
                        state = 1;
                        break;
                    case 1:
                        int i = connection.readInt();
                        Assert.assertTrue("received value ist not excepted value ", i == 11);
                        LOG.fine("int received");
                        state = 2;
                        break;
                    case 2:
                        long l = connection.readLong();
                        Assert.assertTrue("received value ist not excepted value ", l == 33);
                        LOG.fine("long received");
                        state = 3;
                        break;
                    case 3:
                        String s1 = connection.readStringByDelimiter("\r\n", Integer.MAX_VALUE);
                        Assert.assertEquals(s1, "");
                        state = 4;
                        break;
                    case 4:
                        String s2 = connection.readStringByDelimiter("tt", Integer.MAX_VALUE);
                        Assert.assertEquals(s2, "this is the other end ");
                        LOG.fine("word received");
                        state = 5;
                        break;
                    case 5:
                        byte b = connection.readByte();
                        Assert.assertEquals(b, (byte) 34);
                        LOG.fine("byte received");
                        state = 6;
                        break;
                    case 6:
                        byte[] bytes = connection.readBytesByDelimiter("r", Integer.MAX_VALUE);
                        byte[] expected = new byte[] { 2, 78, 45, 78, 23, 11, 45, 78, 12, 56 };
                        for (int j = 0; j < bytes.length; j++) {
                            if (bytes[j] != expected[j]) {
                                Assert.fail("received value ist not excepted value ");
                            }
                        }
                        state = 7;
                        break;
                    case 7:
                        String w = connection.readStringByLength(3);
                        Assert.assertEquals(w, "you");
                        state = 99;
                        connection.write(65.65);
                        LOG.fine("double send");
                        break;
                }
            } while (state != 99);
            connection.flush();
            return true;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    private static final class ShortHandler implements IDataHandler {

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            connection.write(connection.readShort());
            return true;
        }
    }
}
