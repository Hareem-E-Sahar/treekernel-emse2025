public class Test {    public void testLevelTriggeredness() throws Exception {
        StubReadConnectObserver o1 = new StubReadConnectObserver();
        SocketChannel c1 = o1.getChannel();
        connect(c1, LISTEN_ADDR, o1);
        Socket accepted = LISTEN_SOCKET.accept();
        accepted.getOutputStream().write("OUT".getBytes());
        accepted.getOutputStream().flush();
        NIODispatcher.instance().registerRead(c1, o1);
        NIOTestUtils.waitForNIO();
        NIOTestUtils.waitForNIO();
        assertGreaterThan(0, o1.getReadsHandled());
        assertLessThanOrEquals(3, o1.getReadsHandled());
        ByteBuffer buffer = o1.getReadBuffer();
        assertEquals(3, buffer.position());
        assertEquals("OUT", new String(buffer.array(), 0, 3));
        int priorReadsHandled = o1.getReadsHandled();
        NIOTestUtils.waitForNIO();
        NIOTestUtils.waitForNIO();
        assertEquals(priorReadsHandled, o1.getReadsHandled());
        o1.setIgnoreReadData(true);
        accepted.getOutputStream().write("A".getBytes());
        accepted.getOutputStream().flush();
        for (int i = 0; i < 10; i++) NIOTestUtils.waitForNIO();
        assertGreaterThan(priorReadsHandled + 5, o1.getReadsHandled());
        NIODispatcher.instance().interestRead(c1, false);
        NIOTestUtils.waitForNIO();
        priorReadsHandled = o1.getReadsHandled();
        for (int i = 0; i < 10; i++) NIOTestUtils.waitForNIO();
        assertEquals(priorReadsHandled, o1.getReadsHandled());
        NIODispatcher.instance().interestRead(c1, true);
        for (int i = 0; i < 10; i++) NIOTestUtils.waitForNIO();
        assertGreaterThan(priorReadsHandled + 5, o1.getReadsHandled());
        c1.close();
        accepted.close();
    }
}