public class Test {    public boolean isInChannel(Channel channel) {
        return channelsList.containsKey(channel.getChannelType());
    }
}