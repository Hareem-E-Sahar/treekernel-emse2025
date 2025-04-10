public class Test {    public void testAddUserStringStringStringStringChannel() {
        uw.addUser("tester", "ludwig", "john doe", "sf.net", channel);
        assertEquals(UserChannelPermission.VOICE, user.getChannels().get(channel));
        assertEquals(UserChannelPermission.VOICE, uw.getUser("tester").getChannels().get(channel));
        assertNotNull(uw.getUser("tester"));
        assertEquals("tester", user.getNick());
        assertEquals("tester", uw.getUser("tester").getNick());
        assertEquals("ludwig", user.getName());
        assertEquals("john doe", user.getRealname());
        assertEquals("sf.net", user.getHost());
        assertEquals("sf.net", uw.getUser("tester").getHost());
    }
}