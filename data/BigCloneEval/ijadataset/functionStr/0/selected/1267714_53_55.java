public class Test {    protected boolean canConnect(Channel channel) {
        return channel.getClass() == getChannelClass();
    }
}