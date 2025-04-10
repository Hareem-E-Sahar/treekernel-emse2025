public class Test {    public void testMultipleConnectTimeout() throws Exception {
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
}