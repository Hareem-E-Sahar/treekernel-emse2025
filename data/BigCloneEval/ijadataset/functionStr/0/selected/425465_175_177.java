public class Test {    public SocketChannel getChannel() {
        return this.getClient().getConnection().getChannel();
    }
}