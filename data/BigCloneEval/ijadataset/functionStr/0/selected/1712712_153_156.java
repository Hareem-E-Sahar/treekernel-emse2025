public class Test {    public Integer[] getChannelIds() {
        Set<Channel> channels = getChannels();
        return Channel.fetchIds(channels);
    }
}