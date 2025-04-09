public class Test {    @Override
    protected IChannelService getChannelService() {
        return getCometdService();
    }
}