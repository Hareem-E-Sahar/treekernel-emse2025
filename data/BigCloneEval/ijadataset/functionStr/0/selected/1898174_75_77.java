public class Test {    public ChannelTree getChannelTree() throws SAPIException {
        return ChannelTree.createFromChannelMap(getChannelsMap());
    }
}