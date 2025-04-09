public class Test {    public static ChannelService getChannelService(RemoteChannelServiceAsync api) {
        return new ChannelServiceImpl(api);
    }
}