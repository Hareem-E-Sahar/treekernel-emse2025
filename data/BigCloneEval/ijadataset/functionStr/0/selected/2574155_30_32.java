public class Test {    public MessageChannel getChannel(String name) {
        return (MessageChannel) channelMap.get(name);
    }
}