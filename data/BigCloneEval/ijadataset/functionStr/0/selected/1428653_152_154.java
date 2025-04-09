public class Test {    public IRCChannel getChannelJoinedByChannelName(String channelName) {
        return channelNameCanonical2channel.get(channelName.toLowerCase());
    }
}