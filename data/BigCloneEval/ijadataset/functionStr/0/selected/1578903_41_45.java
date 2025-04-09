public class Test {    @Override
    public Socket getSocket() {
        final SocketChannel channel = getChannel();
        return (null == channel) ? null : channel.socket();
    }
}