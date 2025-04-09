public class Test {    public int getChannelOwner(String channel) {
        return channelOwners.get(Utilities.formatString(channel));
    }
}