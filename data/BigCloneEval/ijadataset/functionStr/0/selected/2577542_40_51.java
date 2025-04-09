public class Test {    @Override
    @SuppressWarnings("unchecked")
    public Provider<ChannelService> getProvider() {
        return new Provider<ChannelService>() {

            @Override
            public ChannelService get() {
                ChannelService channelService = ChannelServiceFactory.getChannelService();
                return channelService;
            }
        };
    }
}