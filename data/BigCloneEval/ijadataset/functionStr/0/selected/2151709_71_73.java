public class Test {    public List getEmptyChannels() {
        return this.channelDAO.getChannels(IChannelDAO.EMPTY_CHANNELS);
    }
}