public class Test {    @Override
    public DataChannel<?> getChannel() {
        return UpdatesDataChannel.getTagChannel(tag_name);
    }
}