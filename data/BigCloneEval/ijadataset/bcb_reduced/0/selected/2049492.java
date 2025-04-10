package org.xsocket.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xsocket.SSLTestContextFactory;
import org.xsocket.stream.IDataHandler;
import org.xsocket.stream.IServer;
import org.xsocket.stream.INonBlockingConnection;
import org.xsocket.stream.Server;
import org.xsocket.stream.IConnection.FlushMode;

/**
*
* @author grro@xsocket.org
*/
public final class SSLUserActivatedTest {

    private static final Logger LOG = Logger.getLogger(SSLUserActivatedTest.class.getName());

    private static final String SSL_ON = "SSL_ON";

    private static final String DELIMITER = System.getProperty("line.separator");

    private static final String GREETING = "HELO";

    @BeforeClass
    public static void setUp() {
        SSLContext.setDefault(new SSLTestContextFactory().getSSLContext());
    }

    @Test
    public void testRepeatedActivation() throws Exception {
        IServer sslTestServer = new Server(0, new SSLHandlerRepeatedActivation(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        INonBlockingConnection connection = new NonBlockingConnection("localhost", sslTestServer.getLocalPort(), new SSLTestContextFactory().getSSLContext(), false);
        connection.setAutoflush(true);
        connection.activateSecuredMode();
        connection.write("testi" + DELIMITER);
        String response = receive(connection, DELIMITER);
        Assert.assertEquals("testi", response);
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testActivateSslOnConnect() throws Exception {
        System.out.println("testActivateSslOnConnect...");
        IServer sslTestServer = new Server(0, new OnConnectSSLHandler(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        SocketFactory socketFactory = SSLContext.getDefault().getSocketFactory();
        Socket socket = socketFactory.createSocket("localhost", sslTestServer.getLocalPort());
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        for (int i = 0; i < 3; i++) {
            String req = "hello how are how sdfsfdsf sf sdf sf s sf sdf " + i;
            pw.write(req + DELIMITER);
            pw.flush();
            String res = lnr.readLine();
            if (!req.equals(res)) {
                System.out.println("response : " + res + " is not equals request: " + req);
                Assert.fail("request != response");
            }
        }
        lnr.close();
        pw.close();
        socket.close();
        sslTestServer.close();
    }

    @Test
    public void testActivateSslOnConnect2() throws Exception {
        System.out.println("testActivateSslOnConnect2...");
        IServer sslTestServer = new Server(0, new OnConnectSSLHandler2(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        IBlockingConnection connection = new BlockingConnection("localhost", sslTestServer.getLocalPort(), SSLContext.getDefault(), false);
        String greeting = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
        Assert.assertEquals(greeting, GREETING);
        connection.activateSecuredMode();
        for (int i = 0; i < 3; i++) {
            String req = "hello how are how sdfsfdsf sf sdf sf s sf sdf " + i;
            connection.write(req + DELIMITER);
            String res = connection.readStringByDelimiter(DELIMITER);
            if (!req.equals(res)) {
                System.out.println("response : " + res + " is not equals request: " + req);
                Assert.fail("request != response");
            }
        }
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testActivateSslOnConnect3() throws Exception {
        System.out.println("testActivateSslOnConnect3...");
        IServer sslTestServer = new Server(0, new OnConnectSSLHandler3(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        IBlockingConnection connection = new BlockingConnection("localhost", sslTestServer.getLocalPort(), SSLContext.getDefault(), false);
        String greeting = connection.readStringByDelimiter(DELIMITER);
        Assert.assertEquals(greeting, GREETING);
        connection.activateSecuredMode();
        for (int i = 0; i < 3; i++) {
            String req = "hello how are how sdfsfdsf sf sdf sf s sf sdf " + i;
            connection.write(req + DELIMITER);
            String res = connection.readStringByDelimiter(DELIMITER);
            if (!req.equals(res)) {
                System.out.println("response : " + res + " is not equals request: " + req);
                Assert.fail("request != response");
            }
        }
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testActivateSslOnConnect4() throws Exception {
        System.out.println("testActivateSslOnConnect4...");
        IServer sslTestServer = new Server(0, new OnConnectSSLHandler4(), false, new SSLTestContextFactory().getSSLContext());
        StreamUtils.start(sslTestServer);
        SocketFactory socketFactory = SSLContext.getDefault().getSocketFactory();
        LOG.info("creating socket");
        Socket socket = socketFactory.createSocket("localhost", sslTestServer.getLocalPort());
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        LOG.info("reading greeting");
        String greeting = lnr.readLine();
        Assert.assertEquals(greeting, GREETING);
        LOG.info("start write-read loop");
        for (int i = 0; i < 3; i++) {
            String req = "hello how are how sdfsfdsf sf sdf sf s sf sdf " + i;
            pw.write(req + DELIMITER);
            pw.flush();
            String res = lnr.readLine();
            if (!req.equals(res)) {
                System.out.println("response : " + res + " is not equals request: " + req);
                Assert.fail("request != response");
            }
        }
        lnr.close();
        pw.close();
        socket.close();
        sslTestServer.close();
    }

    @Test
    public void testActivateSslOnConnect5() throws Exception {
        System.out.println("testActivateSslOnConnect5...");
        IServer sslTestServer = new Server(0, new OnConnectSSLHandler4(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        IBlockingConnection connection = new BlockingConnection("localhost", sslTestServer.getLocalPort(), SSLContext.getDefault(), false);
        connection.activateSecuredMode();
        String encyptedGreeting = connection.readStringByDelimiter(DELIMITER);
        Assert.assertEquals(GREETING, encyptedGreeting);
        for (int i = 0; i < 3; i++) {
            String req = "hello how are how sdfsfdsf sf sdf sf s sf sdf " + i;
            connection.write(req + DELIMITER);
            String res = connection.readStringByDelimiter(DELIMITER);
            if (!req.equals(res)) {
                System.out.println("response : " + res + " is not equals request: " + req);
                Assert.fail("request != response");
            }
        }
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testBlocking() throws Exception {
        System.out.println("testblocking...");
        IServer sslTestServer = new Server(0, new AdHocSSLHandler(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        IBlockingConnection connection = new BlockingConnection("localhost", sslTestServer.getLocalPort(), SSLContext.getDefault(), false);
        connection.setAutoflush(true);
        connection.write("test" + DELIMITER);
        String response = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
        Assert.assertEquals("test", response);
        connection.write(SSL_ON + DELIMITER);
        response = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
        connection.activateSecuredMode();
        Assert.assertEquals(SSL_ON, response);
        connection.write("a protected text" + DELIMITER);
        response = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
        Assert.assertEquals("a protected text", response);
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testNonBlockingMissingSSLFactory() throws Exception {
        System.out.println("testNonBlockingMissingSSLFactory...");
        IServer sslTestServer = new Server(0, new AdHocSSLHandler(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        INonBlockingConnection connection = new NonBlockingConnection("localhost", sslTestServer.getLocalPort());
        connection.setAutoflush(true);
        connection.write(SSL_ON + DELIMITER);
        String response = receive(connection, DELIMITER);
        Assert.assertEquals(SSL_ON, response);
        try {
            connection.activateSecuredMode();
            connection.write("testi" + DELIMITER);
            receive(connection, DELIMITER);
            Assert.fail("exception should have been thrown");
        } catch (IOException ioe) {
        }
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testNonBlocking() throws Exception {
        System.out.println("testNonBlockingMissingSSLFactory...");
        IServer sslTestServer = new Server(0, new AdHocSSLHandler(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        INonBlockingConnection connection = new NonBlockingConnection("localhost", sslTestServer.getLocalPort(), SSLContext.getDefault(), false);
        connection.setAutoflush(true);
        connection.write(SSL_ON + DELIMITER);
        String response = receive(connection, DELIMITER);
        Assert.assertEquals(SSL_ON, response);
        connection.activateSecuredMode();
        connection.write("testi" + DELIMITER);
        response = receive(connection, DELIMITER);
        Assert.assertEquals("testi", response);
        connection.close();
        sslTestServer.close();
    }

    @Test
    public void testLengthField() throws Exception {
        IServer sslTestServer = new Server(0, new OnConnectLengthFieldHandler(), false, SSLContext.getDefault());
        StreamUtils.start(sslTestServer);
        SocketFactory socketFactory = SSLContext.getDefault().getSocketFactory();
        LOG.info("creating socket");
        Socket socket = socketFactory.createSocket("localhost", sslTestServer.getLocalPort());
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        for (int i = 0; i < 3; i++) {
            String req = "hello how are how sdfsfdsf sf sdf sf s sf sdf " + i;
            byte[] data = req.getBytes();
            ByteBuffer length = ByteBuffer.allocate(4);
            length.putInt(data.length);
            length.flip();
            byte[] lengthBytes = length.array();
            os.write(lengthBytes);
            os.write(data);
            byte[] l = new byte[4];
            is.read(l);
            int rs = ByteBuffer.wrap(l).getInt();
            byte[] responseBytes = new byte[rs];
            is.read(responseBytes);
            String res = new String(responseBytes);
            if (!req.equals(res)) {
                System.out.println("response : " + res + " is not equals request: " + req);
                Assert.fail("request != response");
            }
        }
        is.close();
        os.close();
        socket.close();
        sslTestServer.close();
    }

    private String receive(INonBlockingConnection connection, String delimiter) throws IOException {
        String response = null;
        do {
            try {
                response = connection.readStringByDelimiter(delimiter, Integer.MAX_VALUE);
            } catch (BufferUnderflowException bue) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignore) {
                }
            }
        } while (response == null);
        return response;
    }

    private static final class OnConnectSSLHandler implements IDataHandler, IConnectHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.activateSecuredMode();
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            String word = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
            connection.write(word + DELIMITER);
            return true;
        }
    }

    private static final class OnConnectSSLHandler2 implements IDataHandler, IConnectHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.write(GREETING + DELIMITER);
            connection.activateSecuredMode();
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            String word = connection.readStringByDelimiter(DELIMITER);
            connection.write(word + DELIMITER);
            return true;
        }
    }

    private static final class OnConnectSSLHandler3 implements IDataHandler, IConnectHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.setAutoflush(false);
            connection.setFlushmode(FlushMode.ASYNC);
            connection.write(GREETING + DELIMITER);
            connection.activateSecuredMode();
            connection.flush();
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            String word = connection.readStringByDelimiter(DELIMITER);
            connection.write(word + DELIMITER);
            connection.flush();
            return true;
        }
    }

    private static final class OnConnectSSLHandler4 implements IDataHandler, IConnectHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.activateSecuredMode();
            connection.write(GREETING + DELIMITER);
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            String word = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
            connection.write(word + DELIMITER);
            return true;
        }
    }

    private static final class OnConnectLengthFieldHandler implements IDataHandler, IConnectHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.setAutoflush(false);
            connection.setFlushmode(FlushMode.ASYNC);
            connection.activateSecuredMode();
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            int length = StreamUtils.validateSufficientDatasizeByIntLengthField(connection);
            String data = connection.readStringByLength(length);
            connection.markWritePosition();
            connection.write((int) 0);
            int written = connection.write(data);
            connection.resetToWriteMark();
            connection.write(written);
            connection.flush();
            return true;
        }
    }

    private static final class AdHocSSLHandler implements IDataHandler {

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            String word = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
            connection.write(word + DELIMITER);
            if (word.equals(SSL_ON)) {
                connection.activateSecuredMode();
            }
            return true;
        }
    }

    private static final class SSLHandlerRepeatedActivation implements IConnectHandler, IDataHandler {

        public boolean onConnect(INonBlockingConnection connection) throws IOException {
            connection.activateSecuredMode();
            return true;
        }

        public boolean onData(INonBlockingConnection connection) throws IOException, BufferUnderflowException {
            connection.activateSecuredMode();
            String word = connection.readStringByDelimiter(DELIMITER, Integer.MAX_VALUE);
            connection.write(word + DELIMITER);
            return true;
        }
    }
}
