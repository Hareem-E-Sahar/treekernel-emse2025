public class Test {    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        this.channelService = ChannelServiceFactory.getChannelService();
        this.updateService.addChannel(this);
    }
}