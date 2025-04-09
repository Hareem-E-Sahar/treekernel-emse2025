public class Test {    public void test_getChannel() throws SocketException {
        assertNull(new DatagramSocket().getChannel());
    }
}