package com.limegroup.gnutella.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import junit.framework.Test;
import com.limegroup.gnutella.util.BaseTestCase;
import com.limegroup.gnutella.util.PrivilegedAccessor;

public class NIODispatcherTest extends BaseTestCase {

    private int LISTEN_PORT = 9999;

    private ServerSocket LISTEN_SOCKET;

    private InetSocketAddress LISTEN_ADDR;

    public NIODispatcherTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(NIODispatcherTest.class);
    }

    public void setUp() throws Exception {
        LISTEN_SOCKET = new ServerSocket(LISTEN_PORT, 0);
        LISTEN_SOCKET.setReuseAddress(true);
        LISTEN_ADDR = new InetSocketAddress("127.0.0.1", LISTEN_PORT);
    }

    public void tearDown() throws Exception {
        try {
            LISTEN_SOCKET.close();
        } catch (IOException ignored) {
        }
    }

    public void testSimpleConnectTimeout() throws Exception {
        StubConnectObserver observer = new StubConnectObserver();
        SocketChannel channel = observer.getChannel();
        NIODispatcher.instance().registerConnect(channel, observer, 3000);
        assertNull(observer.getIoException());
        assertNull(observer.getSocket());
        assertFalse(observer.isShutdown());
        Thread.sleep(3500);
        assertFalse(channel.isConnected());
        assertNull(observer.getSocket());
        Exception iox = observer.getIoException();
        assertNotNull(iox);
        assertInstanceof(SocketTimeoutException.class, iox);
        assertEquals("operation timed out (3000)", iox.getMessage());
        assertTrue(observer.isShutdown());
    }

    public void testMultipleConnectTimeout() throws Exception {
        StubConnectObserver o1 = new StubConnectObserver();
        SocketChannel c1 = o1.getChannel();
        StubConnectObserver o2 = new StubConnectObserver();
        SocketChannel c2 = o2.getChannel();
        NIODispatcher.instance().registerConnect(c1, o1, 3000);
        NIODispatcher.instance().registerConnect(c2, o2, 2000);
        Exception x1;
        Exception x2;
        Thread.sleep(2500);
        x2 = o2.getIoException();
        assertFalse(c2.isConnected());
        assertNotNull(x2);
        assertInstanceof(SocketTimeoutException.class, x2);
        assertEquals("operation timed out (2000)", x2.getMessage());
        assertNull(o2.getSocket());
        assertTrue(o2.isShutdown());
        assertFalse(o1.isShutdown());
        assertNull(o1.getIoException());
        assertNull(o1.getSocket());
        Thread.sleep(1000);
        x1 = o1.getIoException();
        assertFalse(c1.isConnected());
        assertNotNull(x1);
        assertInstanceof(SocketTimeoutException.class, x1);
        assertEquals("operation timed out (3000)", x1.getMessage());
        assertNull(o1.getSocket());
        assertTrue(o1.isShutdown());
        c1.close();
        c2.close();
    }

    public void testConnectPreventsTimeout() throws Exception {
        StubConnectObserver observer = new StubConnectObserver();
        SocketChannel channel = observer.getChannel();
        NIODispatcher.instance().registerConnect(channel, observer, 3000);
        channel.connect(LISTEN_ADDR);
        Thread.sleep(1000);
        assertTrue(channel.isConnected());
        assertNull(observer.getIoException());
        assertNotNull(observer.getSocket());
        assertFalse(observer.isShutdown());
        Thread.sleep(2500);
        assertTrue(channel.isConnected());
        assertEquals(0, interestOps(channel));
        Exception iox = observer.getIoException();
        assertNull(iox);
        channel.close();
    }

    public void testAcceptChannel() throws Exception {
        LISTEN_SOCKET.close();
        ServerSocketChannel channel = ServerSocketChannel.open();
        LISTEN_SOCKET = channel.socket();
        channel.configureBlocking(false);
        LISTEN_SOCKET.bind(new InetSocketAddress(LISTEN_PORT));
        LISTEN_SOCKET.setReuseAddress(true);
        StubAcceptChannelObserver observer = new StubAcceptChannelObserver();
        NIODispatcher.instance().registerAccept(channel, observer);
        SocketChannel c1 = new StubConnectObserver().getChannel();
        SocketChannel c2 = new StubConnectObserver().getChannel();
        SocketChannel c3 = new StubConnectObserver().getChannel();
        assertEquals(0, observer.getChannels().size());
        NIODispatcher.instance().registerConnect(c1, new StubConnectObserver(), 0);
        c1.connect(LISTEN_ADDR);
        Thread.sleep(300);
        assertEquals(1, observer.getChannels().size());
        SocketChannel r1 = observer.getNextSocketChannel();
        assertEquals(c1.socket().getLocalPort(), r1.socket().getPort());
        assertFalse(r1.isBlocking());
        assertTrue(r1.isConnected());
        c1.close();
        NIODispatcher.instance().registerConnect(c2, new StubConnectObserver(), 0);
        NIODispatcher.instance().registerConnect(c3, new StubConnectObserver(), 0);
        c2.connect(LISTEN_ADDR);
        c3.connect(LISTEN_ADDR);
        Thread.sleep(300);
        assertEquals(2, observer.getChannels().size());
        SocketChannel r2 = observer.getNextSocketChannel();
        SocketChannel r3 = observer.getNextSocketChannel();
        if (c2.socket().getLocalPort() != r2.socket().getPort()) {
            SocketChannel temp = r3;
            r3 = r2;
            r2 = temp;
        }
        assertEquals(c2.socket().getLocalPort(), r2.socket().getPort());
        assertFalse(r2.isBlocking());
        assertTrue(r2.isConnected());
        assertEquals(c3.socket().getLocalPort(), r3.socket().getPort());
        assertFalse(r3.isBlocking());
        assertTrue(r3.isConnected());
        c2.close();
        c3.close();
    }

    public void testSimpleReadTimeout() throws Exception {
        StubReadObserver o1 = new StubReadObserver();
        SelectableChannel c1 = o1.getChannel();
        o1.setReadTimeout(1023);
        NIODispatcher.instance().registerRead(c1, o1);
        o1.waitForIOException(2000);
        assertInstanceof(SocketTimeoutException.class, o1.getIox());
        assertEquals("operation timed out (1023)", o1.getIox().getMessage());
        assertTrue(o1.isShutdown());
        assertEquals(0, o1.getReadsHandled());
        c1.close();
    }

    public void testNoTimeoutIfInterestOff() throws Exception {
        StubReadObserver o1 = new StubReadObserver();
        SelectableChannel c1 = o1.getChannel();
        o1.setReadTimeout(1000);
        NIODispatcher.instance().registerRead(c1, o1);
        Thread.sleep(200);
        NIODispatcher.instance().interestRead(c1, false);
        o1.waitForIOException(2000);
        assertNull(o1.getIox());
        assertFalse(o1.isShutdown());
        assertEquals(0, o1.getReadsHandled());
        c1.close();
    }

    public void testChangingTimeoutByInterest() throws Exception {
        StubReadObserver o1 = new StubReadObserver();
        SelectableChannel c1 = o1.getChannel();
        o1.setReadTimeout(1000);
        NIODispatcher.instance().registerRead(c1, o1);
        Thread.sleep(200);
        o1.setReadTimeout(2005);
        NIODispatcher.instance().interestRead(c1, true);
        o1.waitForIOException(4000);
        assertInstanceof(SocketTimeoutException.class, o1.getIox());
        assertEquals("operation timed out (2005)", o1.getIox().getMessage());
        assertTrue(o1.isShutdown());
        assertEquals(0, o1.getReadsHandled());
        c1.close();
    }

    public void testTimeoutUpsAfterReads() throws Exception {
        StubReadConnectObserver o1 = new StubReadConnectObserver();
        SocketChannel c1 = o1.getChannel();
        o1.setReadTimeout(1000);
        NIODispatcher.instance().registerConnect(c1, o1, 1000);
        c1.connect(LISTEN_ADDR);
        o1.waitForEvent(1000);
        assertEquals(c1.socket(), o1.getSocket());
        assertTrue(c1.isConnected());
        LISTEN_SOCKET.setSoTimeout(5000);
        Socket accepted = LISTEN_SOCKET.accept();
        Thread.sleep(2000);
        assertTrue(c1.isConnected());
        assertFalse(o1.isShutdown());
        assertNull(o1.getIoException());
        assertEquals(0, o1.getReadsHandled());
        NIODispatcher.instance().interestRead(c1, true);
        o1.setReadTimeout(2000);
        accepted.getOutputStream().write(new byte[100]);
        Thread.sleep(500);
        assertGreaterThanOrEquals(1, o1.getReadsHandled());
        Thread.sleep(2500);
        assertInstanceof(SocketTimeoutException.class, o1.getIoException());
        assertEquals("operation timed out (2000)", o1.getIoException().getMessage());
        assertGreaterThanOrEquals(o1.getLastReadTime() + 1000, o1.getIoxTime());
        c1.close();
    }

    public void testTimeoutBecomesSmaller() throws Exception {
        StubReadConnectObserver o1 = new StubReadConnectObserver();
        SocketChannel c1 = o1.getChannel();
        NIODispatcher.instance().registerConnect(c1, o1, 1000);
        c1.connect(LISTEN_ADDR);
        o1.waitForEvent(1000);
        assertEquals(c1.socket(), o1.getSocket());
        assertTrue(c1.isConnected());
        LISTEN_SOCKET.setSoTimeout(5000);
        Socket accepted = LISTEN_SOCKET.accept();
        Thread.sleep(2000);
        assertTrue(c1.isConnected());
        assertFalse(o1.isShutdown());
        assertNull(o1.getIoException());
        assertEquals(0, o1.getReadsHandled());
        o1.setReadTimeout(5000);
        NIODispatcher.instance().interestRead(c1, true);
        o1.setReadTimeout(1000);
        accepted.getOutputStream().write(new byte[100]);
        Thread.sleep(500);
        assertGreaterThanOrEquals(1, o1.getReadsHandled());
        Thread.sleep(1500);
        assertInstanceof(SocketTimeoutException.class, o1.getIoException());
        assertEquals("operation timed out (1000)", o1.getIoException().getMessage());
        Thread.sleep(5500);
        assertEquals("operation timed out (1000)", o1.getIoException().getMessage());
        c1.close();
    }

    public void testOtherSelectors() throws Exception {
        Selector stub = new StubSelector();
        NIODispatcher.instance().registerSelector(stub, StubChannel.class);
        try {
            StubReadConnectObserver observer = new StubReadConnectObserver();
            StubChannel channel = new StubChannel();
            observer.setChannel(channel);
            Socket socket = new Socket();
            channel.setSocket(socket);
            NIODispatcher.instance().registerConnect(channel, observer, 0);
            Thread.sleep(300);
            assertEquals(SelectionKey.OP_CONNECT, interestOps(channel));
            channel.setReadyOps(SelectionKey.OP_CONNECT);
            observer.waitForEvent(1000);
            assertSame(socket, channel.socket());
            assertEquals(0, interestOps(channel));
            assertEquals(0, observer.getReadsHandled());
            NIODispatcher.instance().interestRead(channel, true);
            assertEquals(SelectionKey.OP_READ, interestOps(channel));
            channel.setReadyOps(SelectionKey.OP_READ);
            observer.waitForEvent(2000);
            assertEquals(1, observer.getReadsHandled());
        } finally {
            NIODispatcher.instance().removeSelector(stub);
        }
    }

    private int interestOps(SelectableChannel channel) throws Exception {
        Selector selector = (Selector) PrivilegedAccessor.invokeMethod(NIODispatcher.instance(), "getSelectorFor", new Object[] { channel }, new Class[] { SelectableChannel.class });
        return channel.keyFor(selector).interestOps();
    }
}
