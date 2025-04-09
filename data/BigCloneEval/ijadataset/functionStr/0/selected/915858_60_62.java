public class Test {    public Channel getChannel(String name) {
        return (name.equals(CHANNEL_ELEMENTS) ? myElementsChannel : null);
    }
}