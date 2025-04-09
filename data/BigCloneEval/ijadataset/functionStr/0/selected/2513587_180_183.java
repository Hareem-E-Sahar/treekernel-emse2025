public class Test {    @Override
    public RemoteServerSocketChannel getChannel() {
        return (RemoteServerSocketChannel) proxy.getChannel();
    }
}