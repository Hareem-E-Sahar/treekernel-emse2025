public class Test {    public FeedAppender appendAllItemsToEnd(List<ItemEntry> items) throws YarfrawException {
        ChannelFeed ch = readChannel();
        List<ItemEntry> old = ch.getItems();
        if (old == null) {
            old = new ArrayList<ItemEntry>();
            ch.setItems(old);
        }
        old.addAll(items);
        ch.setItems(trimItemsList(old));
        _writer = new FeedWriter(_reader._file);
        _writer.writeChannel(ch);
        return this;
    }
}