public class Test {    public ChannelData getChannelData() {
        if (channelData == null) {
            return null;
        }
        return channelData.get(ChannelData.class);
    }
}