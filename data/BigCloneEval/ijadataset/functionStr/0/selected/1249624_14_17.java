public class Test {    @Override
    public ChannelService get() {
        return ChannelServiceFactory.getChannelService();
    }
}