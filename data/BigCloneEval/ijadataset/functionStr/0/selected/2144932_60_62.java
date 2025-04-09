public class Test {    public Channel getChannel() {
        return AppContext.getChannelManager().getChannel(getName());
    }
}