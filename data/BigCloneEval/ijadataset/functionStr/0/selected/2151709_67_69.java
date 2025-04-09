public class Test {    public List getNoNewsChannels() {
        return this.channelDAO.getChannels(IChannelDAO.NO_NEWS_CHANNELS);
    }
}