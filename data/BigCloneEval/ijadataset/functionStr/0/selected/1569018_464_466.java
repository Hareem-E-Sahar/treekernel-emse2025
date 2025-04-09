public class Test {    public Channel getChannel(String channelName) {
        return channelMap.get(channelName.toLowerCase());
    }
}