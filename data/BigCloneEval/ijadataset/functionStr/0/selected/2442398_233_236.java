public class Test {    @Override
    public RemoteSocketChannel getChannel() {
        return (RemoteSocketChannel) this.proxy.getChannel();
    }
}