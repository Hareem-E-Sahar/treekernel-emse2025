public class Test {    public String getChannelCacheKey(ChannelIF channel) {
        return "_channel_" + channel.getId();
    }
}