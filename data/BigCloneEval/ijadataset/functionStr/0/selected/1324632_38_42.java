public class Test {    public void testGetChannel() throws Exception {
        assertNull(m_rss20.getChannel());
        m_rss20.setChannel(new Rss20Channel());
        assertNotNull(m_rss20.getChannel());
    }
}