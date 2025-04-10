public class Test {    public void testMultipleRecipientsSendRequest() throws Exception {
        InputStream in = new ByteArrayInputStream(SEND_REQUEST_MULTIPLE_RECIPIENT.getBytes(UTF_8));
        SendRequest request = parser.readSendRequest(in);
        final Recipient[] recipients = request.getRecipients();
        assertEquals(4, recipients.length);
        Recipient alice = recipients[0];
        assertEquals("alice@volantis.com", alice.getAddress().getValue());
        assertEquals("smtp", alice.getChannel());
        assertEquals("Nokia-6600", alice.getDeviceName());
        assertEquals("Default type has been assigned", RecipientType.TO, alice.getRecipientType());
        Recipient bob = recipients[1];
        assertEquals("bob@volantis.com", bob.getAddress().getValue());
        assertEquals("smtp", bob.getChannel());
        assertEquals("Nokia-6800", bob.getDeviceName());
        assertEquals("Default type has been assigned", RecipientType.TO, bob.getRecipientType());
        Recipient carol = recipients[2];
        assertEquals("carol@volantis.com", carol.getAddress().getValue());
        assertEquals("smtp", carol.getChannel());
        assertEquals("SonyEriccson-973i", carol.getDeviceName());
        assertEquals("Default type has been assigned", RecipientType.TO, carol.getRecipientType());
        Recipient dave = recipients[3];
        assertEquals("dave@volantis.com", dave.getAddress().getValue());
        assertEquals("smtp", dave.getChannel());
        assertEquals("Samsung-D700", dave.getDeviceName());
        assertEquals("Default type has been assigned", RecipientType.TO, dave.getRecipientType());
        assertEquals("Goodbye", request.getMessage().getSubject());
        assertEquals(new URL("http://some.host.com:6000/volantis/welcome/welcome.xdime"), request.getMessage().getURL());
        assertNotNull("Default sender", request.getSender());
        assertNull(request.getSender().getMSISDN());
        assertNull(request.getSender().getSMTPAddress());
    }
}