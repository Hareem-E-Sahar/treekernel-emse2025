public class Test {            @Override
            public ChannelService get() {
                ChannelService channelService = ChannelServiceFactory.getChannelService();
                return channelService;
            }
}