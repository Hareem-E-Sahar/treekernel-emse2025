public class Test {    public boolean hasChannel(String id) {
        ChannelId cid = getChannelId(id);
        return _root.getChild(cid) != null;
    }
}