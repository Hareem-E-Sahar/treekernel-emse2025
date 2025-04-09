public class Test {    public Channel getChatChannel() {
        return AppContext.getChannelManager().getChannel(chatPrefix + getName());
    }
}