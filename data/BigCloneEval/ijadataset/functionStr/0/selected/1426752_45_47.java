public class Test {    public ChannelTreeNode getChannelTreeNode(ChannelIF channel) {
        return (ChannelTreeNode) channelMap.get(new Long(channel.getId()));
    }
}