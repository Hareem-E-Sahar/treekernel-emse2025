public class Test {    private SocketChannel getChannel() {
        return this.getClient().getConnection().getChannel();
    }
}