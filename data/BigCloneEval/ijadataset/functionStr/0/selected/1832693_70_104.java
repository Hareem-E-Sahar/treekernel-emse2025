public class Test {    public void testDiscoOnBadResponder() throws Exception {
        ReadBufferChannel reader = new ReadBufferChannel(("GNUTELLA/0.6 200 OK DOKIE\r\n" + "ResponseHeader: ResponseValue\r\n" + "\r\n").getBytes());
        WriteBufferChannel writer = new WriteBufferChannel(2048);
        MultiplexingSocket socket = new MultiplexingSocket(reader, writer);
        Properties outRequestProps = new Properties();
        outRequestProps.put("OutRequest", "OutRequestValue");
        Properties outResponseProps = new Properties();
        outResponseProps.put("OutResponse", "OutResponseValue");
        StubHandshakeResponder responder = new StubHandshakeResponder(new StubHandshakeResponse(322, "AARGH!", outResponseProps));
        StubHandshakeObserver observer = new StubHandshakeObserver();
        Handshaker shaker = new AsyncOutgoingHandshaker(outRequestProps, responder, socket, observer);
        shaker.shake();
        socket.exchange();
        assertTrue(observer.isNoGOK());
        assertEquals(322, observer.getCode());
        assertFalse(observer.isBadHandshake());
        assertFalse(observer.isHandshakeFinished());
        assertNull(observer.getShaker());
        HandshakeResponse responseTo = responder.getRespondedTo();
        Map respondedTo = responder.getRespondedToProps();
        assertEquals(1, respondedTo.size());
        assertEquals("ResponseValue", respondedTo.get("ResponseHeader"));
        assertEquals(200, responseTo.getStatusCode());
        assertEquals("OK DOKIE", responseTo.getStatusMessage());
        assertTrue(responder.isOutgoing());
        HandshakeResponse read = shaker.getReadHeaders();
        assertEquals(1, read.props().size());
        assertEquals("ResponseValue", read.props().get("ResponseHeader"));
        HandshakeResponse written = shaker.getWrittenHeaders();
        assertEquals(2, written.props().size());
        assertEquals("OutRequestValue", written.props().get("OutRequest"));
        assertEquals("OutResponseValue", written.props().get("OutResponse"));
        ByteBuffer buffer = writer.getBuffer();
        assertEquals("GNUTELLA CONNECT/0.6\r\nOutRequest: OutRequestValue\r\n\r\n" + "GNUTELLA/0.6 322 AARGH!\r\nOutResponse: OutResponseValue\r\n\r\n", new String(buffer.array(), 0, buffer.limit()));
    }
}