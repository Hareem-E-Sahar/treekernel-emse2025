package org.apache.harmony.nio.tests.java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import junit.framework.TestCase;
import tests.support.Support_PortManager;

public class ServerSocketChannelTest extends TestCase {

    private static final int CAPACITY_NORMAL = 200;

    private static final int CAPACITY_64KB = 65536;

    private static final int TIME_UNIT = 200;

    private InetSocketAddress localAddr1;

    private ServerSocketChannel serverChannel;

    private SocketChannel clientChannel;

    protected void setUp() throws Exception {
        super.setUp();
        this.localAddr1 = new InetSocketAddress("127.0.0.1", Support_PortManager.getNextPort());
        this.serverChannel = ServerSocketChannel.open();
        this.clientChannel = SocketChannel.open();
    }

    protected void tearDown() throws Exception {
        if (null != this.serverChannel) {
            try {
                this.serverChannel.close();
            } catch (Exception e) {
            }
        }
        if (null != this.clientChannel) {
            try {
                this.clientChannel.close();
            } catch (Exception e) {
            }
        }
        super.tearDown();
    }

    public void testValidOps() {
        MockServerSocketChannel testMSChnlnull = new MockServerSocketChannel(null);
        MockServerSocketChannel testMSChnl = new MockServerSocketChannel(SelectorProvider.provider());
        assertEquals(SelectionKey.OP_ACCEPT, this.serverChannel.validOps());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnl.validOps());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnlnull.validOps());
    }

    public void testOpen() {
        MockServerSocketChannel testMSChnl = new MockServerSocketChannel(null);
        MockServerSocketChannel testMSChnlnotnull = new MockServerSocketChannel(SelectorProvider.provider());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnlnotnull.validOps());
        assertNull(testMSChnl.provider());
        assertNotNull(testMSChnlnotnull.provider());
        assertNotNull(this.serverChannel.provider());
        assertEquals(testMSChnlnotnull.provider(), this.serverChannel.provider());
    }

    public void testSocket_Block_BeforeClose() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket s1 = this.serverChannel.socket();
        assertFalse(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
        s1.close();
        assertFalse(this.serverChannel.isOpen());
    }

    public void testSocket_NonBlock_BeforeClose() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.configureBlocking(false);
        ServerSocket s1 = this.serverChannel.socket();
        assertFalse(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
        s1.close();
        assertFalse(this.serverChannel.isOpen());
    }

    public void testSocket_Block_Closed() throws Exception {
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket s1 = this.serverChannel.socket();
        assertTrue(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
    }

    public void testSocket_NonBlock_Closed() throws Exception {
        this.serverChannel.configureBlocking(false);
        this.serverChannel.close();
        assertFalse(this.serverChannel.isBlocking());
        assertFalse(this.serverChannel.isOpen());
        ServerSocket s1 = this.serverChannel.socket();
        assertTrue(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
    }

    private void assertSocketNotAccepted(ServerSocket s) throws IOException {
        assertFalse(s.isBound());
        assertNull(s.getInetAddress());
        assertEquals(-1, s.getLocalPort());
        assertNull(s.getLocalSocketAddress());
        assertEquals(0, s.getSoTimeout());
    }

    public void testChannelBasicStatus() {
        ServerSocket gotSocket = this.serverChannel.socket();
        assertFalse(gotSocket.isClosed());
        assertTrue(this.serverChannel.isBlocking());
        assertFalse(this.serverChannel.isRegistered());
        assertEquals(SelectionKey.OP_ACCEPT, this.serverChannel.validOps());
        assertEquals(SelectorProvider.provider(), this.serverChannel.provider());
    }

    public void testAccept_Block_NotYetBound() throws IOException {
        assertTrue(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        try {
            this.serverChannel.accept();
            fail("Should throw NotYetBoundException");
        } catch (NotYetBoundException e) {
        }
    }

    public void testAccept_NonBlock_NotYetBound() throws IOException {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.configureBlocking(false);
        try {
            this.serverChannel.accept();
            fail("Should throw NotYetBoundException");
        } catch (NotYetBoundException e) {
        }
    }

    public void testAccept_ClosedChannel() throws Exception {
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
        try {
            this.serverChannel.accept();
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }

    public void testAccept_Block_NoConnect() throws IOException {
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket gotSocket = this.serverChannel.socket();
        gotSocket.bind(localAddr1);
        new Thread() {

            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    ServerSocketChannelTest.this.serverChannel.close();
                } catch (Exception e) {
                    fail("Fail to close the server channel because of" + e.getClass().getName());
                }
            }
        }.start();
        try {
            this.serverChannel.accept();
            fail("Should throw a AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }

    public void testAccept_NonBlock_NoConnect() throws IOException {
        ServerSocket gotSocket = this.serverChannel.socket();
        gotSocket.bind(localAddr1);
        this.serverChannel.configureBlocking(false);
        assertNull(this.serverChannel.accept());
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_Blocking_RealData() throws IOException {
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            buf.put((byte) i);
        }
        clientChannel.connect(localAddr1);
        Socket serverSocket = serverChannel.accept().socket();
        InputStream in = serverSocket.getInputStream();
        buf.flip();
        clientChannel.write(buf);
        clientChannel.close();
        assertReadResult(in, CAPACITY_NORMAL);
    }

    /**
     * Asserts read content. The read content should contain <code>size</code>
     * bytes, and the value should be a sequence from 0 to size-1
     * ([0,1,...size-1]). Otherwise, the method throws Exception.
     * 
     */
    private void assertReadResult(InputStream in, int size) throws IOException {
        byte[] readContent = new byte[size + 1];
        int count = 0;
        int total = 0;
        while ((count = in.read(readContent, total, size + 1 - total)) != -1) {
            total = total + count;
        }
        assertEquals(size, total);
        for (int i = 0; i < size; i++) {
            assertEquals((byte) i, readContent[i]);
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_NonBlocking_RealData() throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(localAddr1);
        Socket serverSocket = serverChannel.accept().socket();
        InputStream in = serverSocket.getInputStream();
        clientChannel.write(buf);
        clientChannel.close();
        assertReadResult(in, CAPACITY_NORMAL);
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_Blocking_RealData() throws IOException {
        assertTrue(serverChannel.isBlocking());
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1);
        Socket socket = serverChannel.accept().socket();
        OutputStream out = socket.getOutputStream();
        out.write(writeContent);
        out.flush();
        socket.close();
        assertWriteResult(CAPACITY_NORMAL);
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_NonBlocking_RealData() throws Exception {
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1);
        Socket clientSocket = serverChannel.accept().socket();
        OutputStream out = clientSocket.getOutputStream();
        out.write(writeContent);
        clientSocket.close();
        assertWriteResult(CAPACITY_NORMAL);
    }

    /**
     * @throws InterruptedException 
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_LByteBuffer_Blocking_ReadWriteRealLargeData() throws IOException, InterruptedException {
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_64KB);
        for (int i = 0; i < CAPACITY_64KB; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(localAddr1);
        WriteChannelThread writeThread = new WriteChannelThread(clientChannel, buf);
        writeThread.start();
        Socket socket = serverChannel.accept().socket();
        InputStream in = socket.getInputStream();
        assertReadResult(in, CAPACITY_64KB);
        writeThread.join();
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    class WriteChannelThread extends Thread {

        SocketChannel channel;

        ByteBuffer buffer;

        IOException exception;

        public WriteChannelThread(SocketChannel channel, ByteBuffer buffer) {
            this.channel = channel;
            this.buffer = buffer;
        }

        public void run() {
            try {
                channel.write(buffer);
                channel.close();
            } catch (IOException e) {
                exception = e;
            }
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_read_LByteBuffer_NonBlocking_ReadWriteRealLargeData() throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_64KB);
        for (int i = 0; i < CAPACITY_64KB; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(localAddr1);
        WriteChannelThread writeThread = new WriteChannelThread(clientChannel, buf);
        writeThread.start();
        Socket socket = serverChannel.accept().socket();
        InputStream in = socket.getInputStream();
        assertReadResult(in, CAPACITY_64KB);
        writeThread.join();
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_LByteBuffer_NonBlocking_ReadWriteRealLargeData() throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_64KB];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1);
        Socket socket = serverChannel.accept().socket();
        WriteSocketThread writeThread = new WriteSocketThread(socket, writeContent);
        writeThread.start();
        assertWriteResult(CAPACITY_64KB);
        writeThread.join();
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    class WriteSocketThread extends Thread {

        Socket socket;

        byte[] buffer;

        IOException exception;

        public WriteSocketThread(Socket socket, byte[] buffer) {
            this.socket = socket;
            this.buffer = buffer;
        }

        public void run() {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(buffer);
                socket.close();
            } catch (IOException e) {
                exception = e;
            }
        }
    }

    /**
     * @tests ServerSocketChannel#accept().socket()
     */
    public void test_write_LByteBuffer_Blocking_ReadWriteRealLargeData() throws Exception {
        serverChannel.socket().bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_64KB];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1);
        Socket socket = serverChannel.accept().socket();
        WriteSocketThread writeThread = new WriteSocketThread(socket, writeContent);
        writeThread.start();
        assertWriteResult(CAPACITY_64KB);
        writeThread.join();
        if (writeThread.exception != null) {
            throw writeThread.exception;
        }
    }

    /**
     * Uses SocketChannel.read(ByteBuffer) to verify write result.
     */
    private void assertWriteResult(int size) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(size + 1);
        int count = 0;
        int total = 0;
        long beginTime = System.currentTimeMillis();
        while ((count = clientChannel.read(buf)) != -1) {
            total = total + count;
            if (System.currentTimeMillis() - beginTime > 10000) {
                break;
            }
        }
        assertEquals(total, size);
        buf.flip();
        for (int i = 0; i < count; i++) {
            assertEquals((byte) i, buf.get(i));
        }
    }

    /**
     * @tests ServerSocketChannel#socket().getSoTimeout()
     */
    public void test_accept_SOTIMEOUT() throws IOException {
        final int SO_TIMEOUT = 10;
        ServerSocketChannel sc = ServerSocketChannel.open();
        try {
            ServerSocket ss = sc.socket();
            ss.bind(localAddr1);
            sc.configureBlocking(false);
            ss.setSoTimeout(SO_TIMEOUT);
            SocketChannel client = sc.accept();
            assertNull(client);
            int soTimeout = ss.getSoTimeout();
            assertEquals(SO_TIMEOUT, soTimeout);
        } finally {
            sc.close();
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Blocking_NotBound() throws IOException {
        ServerSocket gotSocket = serverChannel.socket();
        serverChannel.configureBlocking(true);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Nonblocking_NotBound() throws IOException {
        ServerSocket gotSocket = serverChannel.socket();
        serverChannel.configureBlocking(false);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Nonblocking_Bound() throws IOException {
        serverChannel.configureBlocking(false);
        ServerSocket gotSocket = serverChannel.socket();
        gotSocket.bind(localAddr1);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw a ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }

    /**
     * @tests ServerSocket#socket().accept()
     */
    public void test_socket_accept_Blocking_Bound() throws IOException {
        serverChannel.configureBlocking(true);
        ServerSocket gotSocket = serverChannel.socket();
        gotSocket.bind(localAddr1);
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw a ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }

    /**
     * Regression test for HARMONY-4961
     */
    public void test_socket_getLocalPort() throws IOException {
        serverChannel.socket().bind(localAddr1);
        clientChannel.connect(localAddr1);
        SocketChannel myChannel = serverChannel.accept();
        int port = myChannel.socket().getLocalPort();
        assertEquals(localAddr1.getPort(), port);
        myChannel.close();
        clientChannel.close();
        serverChannel.close();
    }

    /**
     * Regression test for HARMONY-6375
     */
    public void test_accept_configureBlocking() throws Exception {
        InetSocketAddress localAddr = new InetSocketAddress("localhost", 0);
        serverChannel.socket().bind(localAddr);
        new Thread() {

            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    serverChannel.configureBlocking(false);
                    serverChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        try {
            serverChannel.accept();
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
        serverChannel.close();
    }
}
