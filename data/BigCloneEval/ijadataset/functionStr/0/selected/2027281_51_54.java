public class Test {    @Override
    public ServerSocketChannel getChannel() {
        return delegateSocket.getChannel();
    }
}